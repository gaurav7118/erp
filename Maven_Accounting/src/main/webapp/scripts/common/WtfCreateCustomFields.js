

Wtf.account.CreateCustomFields = function(config) {
    Wtf.account.CreateCustomFields.superclass.constructor.call(this, config);
    this.customcolumn=config.customcolumn!=undefined ? config.customcolumn:0;
    this.isforgstrulemapping=config.isforgstrulemapping!=undefined ? config.isforgstrulemapping:false;
    this.groupname=config.groupname != ""?config.groupname:"";
    this.dimvalue=config.dimvalue != ""?config.dimvalue:"";    
    this.customColumnFlag=this.customcolumn==1?true:false;
    this.fieldidsString='';
    this.isOpeningTransaction=config.isOpeningTransaction != undefined ? config.isOpeningTransaction : false ; 
    this.accountid = (Wtf.account.companyAccountPref.splitOpeningBalanceAmount && config.accountid != undefined) ? config.accountid : null;
    this.presentbalance = (Wtf.account.companyAccountPref.splitOpeningBalanceAmount && config.presentbalance != undefined) ? config.presentbalance : null;
    this.IsCOA = (Wtf.account.companyAccountPref.splitOpeningBalanceAmount && config.IsCOA != undefined) ? config.IsCOA : false;
    this.iscallFromTransactionsForm=config.iscallFromTransactionsForm!=undefined ? config.iscallFromTransactionsForm:false;
    this.isForMultiEntity = config.isForMultiEntity != undefined ? config.isForMultiEntity : false;
    this.isBulkPayment = config.isBulkPayment != undefined ? config.isBulkPayment : false;  // In case bulk payment request
    this.parentObjScope=config.parentObjScope;
    this.moduleid=config.moduleid;
    //Flag to indicate whether Avalara integration is enabled and module is enabled for Avalara Integration or not
    this.isModuleForAvalara = (Wtf.account.companyAccountPref.avalaraIntegration && (config.moduleid == Wtf.Acc_Invoice_ModuleId || config.moduleid == Wtf.Acc_Cash_Sales_ModuleId || config.moduleid == Wtf.Acc_Sales_Order_ModuleId || config.moduleid == Wtf.Acc_Customer_Quotation_ModuleId || config.moduleid == Wtf.Acc_Delivery_Order_ModuleId || config.moduleid == Wtf.Acc_Sales_Return_ModuleId)) ? true : false;
    this.addEvents({
        'populateDimensionValue': true,
        'populateGlobalDimensionValue': true,
        'populateGlobalDimensionValueInBulkPayment': true
    });
}
Wtf.extend(Wtf.account.CreateCustomFields, Wtf.Panel, {
    getFieldRecord: function() {
        this.dimensionFieldArray = [];
        this.customFieldArray = [];
        this.checkListArray = [];
        this.checkListArrayValues = [];
        this.checkListCheckBoxesArray = [];
        this.checkListCount = 0;
        this.customFieldArrayValues = [];
        this.dimensionFieldArrayValues  = [];
        if(Wtf.account.companyAccountPref.splitOpeningBalanceAmount && this.IsCOA){
            this.distributeOpeningBalanceValues = [];//To hold [0]-comboid, [1]-distributed opening balance, [2]-debit type, [3]-field id.
            this.distributeOpeningBalanceDeleteFields = [];//To hold fieldid who's mappings are to be deleted
            this.distributeOpeningBalanceFields = [];
            this.tempdistributeOpeningBalanceValues=[];
        }
        if (this.moduleid && this.moduleid != undefined) {
            var recJEID="";
            if(this.record){
                if(this.moduleid==Wtf.Acc_Product_Master_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_AssetsGroups_ModuleId){
                    recJEID=this.record.data.productid;
                }else if(this.moduleid==Wtf.Acc_Customer_ModuleId || this.moduleid==Wtf.Acc_Vendor_ModuleId || this.moduleid==Wtf.Account_Statement_ModuleId) {
                    recJEID=this.record.data.accid;
                }else if(this.isOpeningTransaction){
                    recJEID=this.record.data.transactionId;
                }else if(this.moduleid==Wtf.Acc_Purchase_Order_ModuleId || this.moduleid==Wtf.Acc_Sales_Order_ModuleId
                                        || this.moduleid==Wtf.Acc_FixedAssets_Purchase_Order_ModuleId || this.moduleid==Wtf.MRP_Job_Work_ORDER_REC){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.Acc_Delivery_Order_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_DeliveryOrder_ModuleId || this.moduleid==Wtf.Acc_Lease_DO){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.Acc_Goods_Receipt_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.Acc_Sales_Return_ModuleId || this.moduleid==Wtf.Acc_Lease_Return || this.moduleid==Wtf.Acc_FixedAssets_Sales_Return_ModuleId){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.Acc_Purchase_Return_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_Purchase_Return_ModuleId){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.Acc_Customer_Quotation_ModuleId || this.moduleid==Wtf.Acc_Lease_Quotation){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.Acc_Contract_ModuleId || this.moduleid==Wtf.Acc_Lease_Contract){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.Acc_Lease_Order){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.Acc_Purchase_Requisition_ModuleId || this.moduleid==Wtf.Acc_RFQ_ModuleId 
                                        || this.moduleid==Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_RFQ_ModuleId){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.Acc_ConsignmentDeliveryOrder_ModuleId){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.Acc_ConsignmentInvoice_ModuleId){
                    recJEID=this.record.data.journalentryid;
                }else if(this.moduleid==Wtf.Acc_ConsignmentSalesReturn_ModuleId){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.Acc_Consignment_GoodsReceiptOrder_ModuleId){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.Acc_Consignment_GoodsReceipt_ModuleId){
                    recJEID=this.record.data.journalentryid;
                }else if(this.moduleid==Wtf.Acc_ConsignmentPurchaseReturn_ModuleId){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.Acc_ConsignmentVendorRequest_ModuleId){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.Acc_ConsignmentRequest_ModuleId){
                    recJEID=this.record.data.billid;
//                    this.moduleid=Wtf.Acc_Sales_Order_ModuleId
                }else if(this.moduleid==Wtf.labourMaster){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.MRP_Work_Centre_ModuleID){
                    recJEID=this.record.data.id;
                }else if(this.moduleid==Wtf.MACHINE_MANAGEMENT_MODULE_ID){
                    recJEID=this.record.data.id;
                }else if(this.moduleid==Wtf.MRP_Work_Order_ModuleID){
                    recJEID=this.record.data.id;
                }else if(this.moduleid==Wtf.MRP_MASTER_CONTRACT_MODULE_ID){
                    recJEID=this.record.data.id;
                }else if(this.moduleid==Wtf.MRP_Route_Code_ModuleID){
                    recJEID=this.record.data.id;
                }else if(this.moduleid==Wtf.MRP_Job_Work_ModuleID){
                    recJEID=this.record.data.id;
                }else if(this.moduleid==Wtf.Acc_Security_Gate_Entry_ModuleId){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.Job_Work_Out_ORDER_REC){
                    recJEID=this.record.data.billid;
                }else if(this.moduleid==Wtf.Inventory_Stock_Adjustment_ModuleId){
                    recJEID=this.record.data.id;
                }
                else{
                    if(this.record.data.parentje != "" && this.record.data.parentje != undefined)
                        recJEID=this.record.data.parentje;
                    else
                        recJEID= this.record.data.journalentryid;
                }
                
            }
            Wtf.Ajax.requestEx({
                url: "ACCAccountCMN/getFieldParams.do",
                params: {
                    moduleid: this.moduleid,
                    jeId: recJEID,
                    isOpeningTransaction:this.isOpeningTransaction,
                    customcolumn:this.customcolumn,
                    isActivated:1,
                    isforgstrulemapping:this.isforgstrulemapping,
                    groupname:this.groupname,
                    dimvalue:this.dimvalue
                }
            }, this, this.genSuccessResponse, this.genFailureResponse);
        }
    },
    genSuccessResponse: function(responseObj) {
        this.sdate = "0";
        this.edate = "0";

        if (responseObj.data != '' && responseObj.data != null) {
            this.count = 0;//responseObj.data.length;
            var campaignTypeFlag = false;
            var moveToLeadFlag = false;
            for (var i = 0; i < responseObj.data.length; i++) {

                if (responseObj.data[i].fieldtype == "1") {   // TextField

                    if (this.callFrom == "MoveLead") {  // Create Lead form from Move Lead from View Campaign

                        if (!moveToLeadFlag) {
                            this.addMoveToLeadTextField();
                            moveToLeadFlag = true;
                        }
                        if (!(responseObj.data[i].recordname == "lastname") && !(responseObj.data[i].recordname == "firstname") && !(responseObj.data[i].recordname == "email")) {
                            this.addTextField(responseObj.data[i]);
                        }

                    } else {
                        var props = {};
                        if (responseObj.data[i].recordname == "email") {
                            props.vtype = 'email';
                        }
                        this.addTextField(responseObj.data[i], props);

                        if (this.configType == "Campaign" && responseObj.data[i].recordname == "campaignname") {
                            this.addCampaignTypeComboBox(campaignTypeFlag);
                        }
                    }

                } else if (responseObj.data[i].fieldtype == "2") {  // NumberField

                    this.addNumberField(responseObj.data[i]);

                } else if (responseObj.data[i].fieldtype == "3") {  // DateField

                    this.addDateField(responseObj.data[i]);

                } else if (responseObj.data[i].fieldtype == "4" || responseObj.data[i].fieldtype == "7") {  // ComboBox Or Multi-Select Combo Box
                    if (this.restrictFlagOnDropDownStoreLoanForPaymentModule(responseObj.data[i].fieldtype,responseObj.data[i].gstConfigType) && (responseObj.data[i].parentid=="" || responseObj.data[i].parentid==undefined || this.isEdit)) {
                        this.count++;
                    }
                    if (responseObj.data[i].comboname != undefined && responseObj.data[i].comboname[0] == "Campaign Type") {
                        campaignTypeFlag = true;
                    } else if(responseObj.data[i].iscustomcolumn == false || (responseObj.data[i].iscustomfield==false && responseObj.data[i].iscustomcolumn==true) || this.moduleid === Wtf.Account_Statement_ModuleId ||this.moduleid === Wtf.Acc_Vendor_ModuleId ||this.moduleid === Wtf.Acc_Customer_ModuleId || this.isOpeningTransaction ){
                        this.addComboBox(responseObj.data[i]);
                    }

                } else if (responseObj.data[i].fieldtype == "12") { 
                    this.count++;
                    this.addCheckList(responseObj.data[i]);
              
                } else if (responseObj.data[i].fieldtype == "5") {  // TimeField

                    this.addTimeField(responseObj.data[i]);

                } else if (responseObj.data[i].fieldtype == "6") { // CheckBox

                    this.addCheckBox(responseObj.data[i]);

                } else if (responseObj.data[i].fieldtype == "8") { // Reference Module

                    this.addReferenceComboBox(responseObj.data[i]);
                
                } else if (responseObj.data[i].fieldtype == "11") { // Reference Module

                    this.addCheckBoxField(responseObj.data[i]);

                } else if (responseObj.data[i].fieldtype == "13") { // Text Area

                    this.addTextAreaField(responseObj.data[i]);
                } else if (responseObj.data[i].fieldtype == "15") { // Rich Text Area

                    this.addRichTextAreaField(responseObj.data[i]);

                }
                Wtf.getCmp(this.id).doLayout();
            }

        } else {
            if(this.isRevaluationWin != undefined && this.isRevaluationWin==true)
            this.add({
                html :'<div style="font-size:15px; text-align:center; color:#808080; font-weight:bold; margin-top:7%;">'+ WtfGlobal.getLocaleText("erp.emptytext.norectodisplay")+'<br></div>',
                baseCls:'bckgroundcolor',
                border : false
            })
        }
        for(var CheckListCount=0;CheckListCount < this.checkListArrayValues.length;CheckListCount++){
            this.customFieldArray.push(this.checkListArray[CheckListCount]);
            this.customFieldArrayValues.push(this.checkListArrayValues[CheckListCount]);
        }
//        if (this.loadCnt == this.count) { 
//            this.setCmbValues();            // This method should be called after adding fields to fieldset.
//        }
        this.tagsFieldset = new Wtf.form.FieldSet({
            title:this.customColumnFlag ? WtfGlobal.getLocaleText("acc.field.DimentionsatLineItem"): WtfGlobal.getLocaleText("acc.inv.fieldset.title"), //"For missing entries in dropdown fields",
            autoHeight: true,
            width:(this.isWindow)?this.widthVal+'%':'97%',
            autoWidth:this.customColumnFlag,
            border: false
        });
        this.lineLevelDimensionFieldset = new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.field.LineLevelDimensions"), //"Line Level Dimensions",
            autoHeight: true,
            width:(this.isWindow)?this.widthVal+'%':'97%',
            autoWidth:this.customColumnFlag,
            border: false
        });
        this.customFieldset = new Wtf.form.FieldSet({
            title: this.customColumnFlag ? WtfGlobal.getLocaleText("acc.field.LineItems"): WtfGlobal.getLocaleText("acc.field.OtherFields"), //"For missing entries in dropdown fields",
            autoHeight: true,
            width:(this.isWindow)?this.widthVal+'%':'97%',
            autoWidth:this.customColumnFlag,
            border: false
        });
        this.dimensionItem = this.createDimensionColumnModel('dimensionFieldData'+this.id);
        if(this.moduleid != Wtf.Account_Statement_ModuleId && this.moduleid != Wtf.Acc_Vendor_ModuleId && this.moduleid != Wtf.Acc_Customer_ModuleId){
           if(!this.isOpeningTransaction){
                this.lineLevelDimensionItem = this.createDimensionColumnModel('lineLevelDimensionFieldData'+this.id);
           }
        }
        this.customItemColumns = this.createFieldColumnModel('customFieldData'+this.id);
        this.customCheckListItemColumns = this.createCheckListFieldColumnModel('customFieldCheckListData');
        //        this.dimensionColumns= this.createFieldColumnModel('dimensionFieldData');
        this.addFields(this.tagsFieldset,this.dimensionItem,this.dimensionFieldArray, 'dimensionFieldData'+this.id);
        this.addFields(this.customFieldset,this.customItemColumns,this.customFieldArray, 'customFieldData'+this.id+'_');
        this.addFields(this.customFieldset,this.customCheckListItemColumns,this.checkListArray,'customFieldCheckListData_');
        if(this.moduleid != Wtf.Account_Statement_ModuleId && this.moduleid != Wtf.Acc_Vendor_ModuleId && this.moduleid != Wtf.Acc_Customer_ModuleId){
           if(!this.isOpeningTransaction){
                this.addFields(this.lineLevelDimensionFieldset,this.lineLevelDimensionItem,this.dimensionFieldArray, 'lineLevelDimensionFieldData'+this.id);
           }
        }
        if (this.loadCnt == this.count) {
            this.setCmbValues();
        }
        if(this.compId!=undefined){
        var northFormHeight = Wtf.getCmp(this.compId).getSize().height;
        if (this.customFieldArray.length > 0) {
            if (this.moduleid == 6){
                northFormHeight += (this.customFieldArray.length/3)+100;
            } else if(this.isWindow){
                northFormHeight +=0;
            } else{
                northFormHeight += (this.customFieldArray.length/3)+70;
            }
           
                if (this.compId1 != undefined) {
                    var northFormHeight1 = Wtf.getCmp(this.compId1).height;
                    if(northFormHeight1==undefined && Wtf.getCmp(this.compId1) && Wtf.getCmp(this.compId1).ownerCt) {
                        Wtf.getCmp(this.compId1).ownerCt.doLayout();
                    } else {
                        northFormHeight1 += (this.customFieldArray.length/3)+100;
                        Wtf.getCmp(this.compId1).setHeight(northFormHeight1);
                        Wtf.getCmp(this.compId1).doLayout();
                    }
                }
            }
        }
        
        if (this.dimensionFieldArray.length > 0) {
            if (this.moduleid == 6)
                northFormHeight += (this.dimensionFieldArray.length/3)+100;
            else
                northFormHeight += (this.dimensionFieldArray.length/3)+70;
            if (this.compId1 != undefined) {
                var northFormHeight1 = Wtf.getCmp(this.compId1).height;
                if(northFormHeight1==undefined && Wtf.getCmp(this.compId1) && Wtf.getCmp(this.compId1).ownerCt) {
                    Wtf.getCmp(this.compId1).ownerCt.doLayout();
                } else {
                    northFormHeight1 += (this.dimensionFieldArray.length/3)+100;
                    Wtf.getCmp(this.compId1).setHeight(northFormHeight1);
                    Wtf.getCmp(this.compId1).doLayout();
                }
            }
        }
        
        if(Wtf.getCmp(this.compId)){
            Wtf.getCmp(this.compId).setHeight(northFormHeight);
            Wtf.getCmp(this.compId).doLayout();
        }   
        var globalLevelDimCount = this.getLineLevelDimensionFieldsArraySize(false);
        var lineLevelDimCount = this.getLineLevelDimensionFieldsArraySize(true);
        var globalLevelCustomFieldCount = this.getCustomFieldsArraySize(); // Returns field count ,which are present and not hidden
        
        if (this.customFieldArray.length > 0){
            if (globalLevelCustomFieldCount> 0) {
                this.add(this.customFieldset);
            }
        }
        if (globalLevelDimCount > 0){
            this.add(this.tagsFieldset);
        } else if (lineLevelDimCount > 0){
            if (this.moduleid === Wtf.Account_Statement_ModuleId || this.moduleid === Wtf.Acc_Vendor_ModuleId || this.moduleid === Wtf.Acc_Customer_ModuleId || this.isOpeningTransaction) {
                this.add(this.tagsFieldset);
            }
        }
        if (lineLevelDimCount > 0){
            if(this.moduleid != Wtf.Account_Statement_ModuleId && this.moduleid != Wtf.Acc_Vendor_ModuleId && this.moduleid != Wtf.Acc_Customer_ModuleId ){
                if(!this.isOpeningTransaction){
                    this.add(this.lineLevelDimensionFieldset);
                }
            }
        }
        if(this.isRevaluationWin != undefined && this.isRevaluationWin==true && this.isEdit == true){
            Wtf.getCmp(this.parentcompId).getRecord();
        }
        this.doLayout();
        if (this.parentcompId != undefined && Wtf.getCmp(this.parentcompId)!= undefined) {
            Wtf.getCmp(this.parentcompId).doLayout();
        }
        if(northFormHeight1==undefined && Wtf.getCmp(this.compId1) && Wtf.getCmp(this.compId1).ownerCt) {
            Wtf.getCmp(this.compId1).ownerCt.doLayout();
        }
            
    },
    /**
     * Function to check whether custom fields are present and not hidden
     * ERM-1108
     * @returns {Number}
     */
    getCustomFieldsArraySize : function() {
        if(this.customFieldArray.length==0){
            return 0;
        }else{
            var cnt=0;
            for (var i = 0; i < this.customFieldArray.length; i++) {           
                if((this.customFieldArray[i].hideField==undefined || this.customFieldArray[i].hideField==false))
                    cnt++;                            
            }
            return cnt;
        }
    },
    getLineLevelDimensionFieldsArraySize : function(isLineLevelDimension) {
        if(this.dimensionFieldArray.length==0){
            return 0;
        }else{
            var cnt=0;
            for (var i = 0; i < this.dimensionFieldArray.length; i++) {
            if(isLineLevelDimension){
                    if(this.dimensionFieldArray[i].iscustomcolumn && (this.dimensionFieldArray[i].hideField==undefined || this.dimensionFieldArray[i].hideField==false))
                        cnt++;
            }else{
                    if(!this.dimensionFieldArray[i].iscustomcolumn && (this.dimensionFieldArray[i].hideField==undefined || this.dimensionFieldArray[i].hideField==false))
                        cnt++;
                }
            }
            return cnt;
        }
    },
    
    createFieldColumnModel : function(componentId) {
        if(this.isWindow && Wtf.account.companyAccountPref.splitOpeningBalanceAmount && this.IsCOA){
            return (new Wtf.Panel({
                layout:'column',
                border: false,
                autoHeight: true,
                id: componentId,
                items:[{
                    columnWidth: 1,
                    border:false,
                    labelWidth:125,
                    id : componentId+'_0',
                    layout:'form'
                }]
            }));         
         
        }else if(this.isWindow){
            return (new Wtf.Panel({
                layout:'column',
                border: false,
                ctCls:'dimensionFieldData',
                autoHeight: true,
                id: componentId,
                items:[{
                    columnWidth: .49,
                    border:false,
                    labelWidth:125,
                    id : componentId+'_0',
                    layout:'form'
                },{
                    columnWidth: .49,
                    border:false,
                    labelWidth:125,
                    id : componentId+'_1',
                    layout:'form'   
                
                }]
            }));
        }else{
            return (new Wtf.Panel({
                layout:'column',
                border: false,
                autoHeight: true,
                ctCls:'dimensionFieldData',
                id: componentId,
                items:[{
                    columnWidth: .3,
                    border:false,
                    labelWidth:125,
                    id : componentId+'_0',
                    layout:'form'
                },{        
                    columnWidth: .3,
                    border:false,
                    labelWidth:125,
                    id : componentId+'_1',
                    layout:'form'
                },{
                    columnWidth: .3,
                    border:false,
                    labelWidth:125,
                    id : componentId+'_2',
                    layout:'form'
                }]
            }));
        }
    },
    
    createCheckListFieldColumnModel : function(componentId) {
        if(this.isWindow){
            return (new Wtf.Panel({
                layout:'column',
                border: false,
                autoHeight: true,
                id: componentId,
                items:[{
                    columnWidth: '.49',
                    border:false,
                    labelWidth:110,
                    id : componentId+'_0',
                    layout:'form'
                },{
                     columnWidth: '.49',
                    border:false,
                    style:"padding-left:10px;",
                    labelWidth:110,
                    id : componentId+'_1',
                    layout:'form'
                }]
            }));
        }else{
            return (new Wtf.Panel({
                layout:'column',
                border: false,
                autoHeight: true,
                id: componentId,
                items:[{
                    columnWidth:'.5',
                    border:false,
                    labelWidth:110,
                    id : componentId+'_0',
                    layout:'form'
                },{
                    columnWidth:'.5',
                    border:false,
                    style:"padding-left:10px;",
                    labelWidth:125,
                    id : componentId+'_1',
                    layout:'form'
                }]
            }));
        }
    },
    createDimensionColumnModel : function(componentId) {        
        if(this.isWindow && Wtf.account.companyAccountPref.splitOpeningBalanceAmount && this.IsCOA){
            return (new Wtf.Panel({
                layout:'column',
                border: false,
                autoHeight: true,
                id: componentId,
                ctCls:'dimensionFieldData',
                items:[new Wtf.Panel({
                    columnWidth: 1,
                    border:false,
                    labelWidth:125,
                    id : componentId+'_0',
                    layout:'form'
                })]
            }));
        }else if(this.isWindow){
            return (new Wtf.Panel({
                layout:'column',
                border: false,
                autoHeight: true,
                id: componentId,
                ctCls:'dimensionFieldData',
                items:[new Wtf.Panel({
                     columnWidth: .49,
                    layout: 'form',
                    border: false,
                    autoHeight: true,
                    id: componentId+'_0'
                }),
                new Wtf.Panel({
                     columnWidth: .49,
                    layout: 'form',
                    border: false,
                    autoHeight: true,
                    id: componentId+'_1'
                }),
                //                new Wtf.Panel({
                //                     columnWidth: .5,
                //                    layout: 'form',
                //                    border: false,
                //                    autoHeight: true,
                //                    id: componentId+'_2'
                //                }),
                //                new Wtf.Panel({
                //                     columnWidth: .5,
                //                    layout: 'form',
                //                    border: false,
                //                    autoHeight: true,
                //                    id: componentId+'_3'
                //                })
                ]
            }));
        }else{
            return (new Wtf.Panel({
                layout: 'column',
                border: false,
                autoHeight: true,
                id: componentId,
                ctCls:'dimensionFieldData'
            }));            
        }
    },
    
    addFields : function(container, columnLayoutObj,  arrayObj, componenetID) {
        if(componenetID=='dimensionFieldData'+this.id) {
            var columnCnt = 0;
            var itemCnt = 0;
            var rowPanelCnt = 4;
            var rowDimensionCnt = 4;
            for (var cntArr = 0; cntArr < arrayObj.length; cntArr++) {
                var itemObj; 
                if(this.isWindow){
                    itemObj = {
                        xtype: 'panel',
                        border: false,
                        //title: '<div wtf:qtip="' + arrayObj[cntArr].fieldLabel + '">' + Wtf.util.Format.ellipsis(arrayObj[cntArr].fieldLabel, Wtf.CUSTOM_PANEL_TITLE_LENGTH) + '</div>',
                        tabTip: arrayObj[cntArr].fieldLabel,
                        hidden: Wtf.account.companyAccountPref.hierarchicalDimensions ? ((arrayObj[cntArr].parentid == "" || arrayObj[cntArr].parentid == null || arrayObj[cntArr].parentid == undefined) ? false : true) : false,
                        bodyStyle: "padding-left:3px;",
                        columnWidth: .49,
                        border:false,
                        labelWidth: 125,
                        layout: 'form',
                        items: [arrayObj[cntArr]]
                    }
                } else {
                    itemObj = {
                        xtype: 'panel',
                        layout: 'form',
                        columnWidth: .3,
                        border: false,
                        // title: '<div wtf:qtip="' + arrayObj[cntArr].fieldLabel + '">' + Wtf.util.Format.ellipsis(arrayObj[cntArr].fieldLabel, Wtf.CUSTOM_PANEL_TITLE_LENGTH) + '</div>',
                        tabTip: arrayObj[cntArr].fieldLabel,
                        hidden: Wtf.account.companyAccountPref.hierarchicalDimensions ? ((arrayObj[cntArr].parentid == "" || arrayObj[cntArr].parentid == null || arrayObj[cntArr].parentid == undefined) ? false : true) : false,
                        bodyStyle: "padding-left:3px;",
                        border:false,
                        labelWidth: 125,
                        items: [arrayObj[cntArr]]
                    }
                }
               
                /**
                 * dont add GST related fields in Forms ERP-32829
                 */
                if (WtfGlobal.GSTApplicableForCompany() != Wtf.GSTStatus.NONE && arrayObj[cntArr].hideField && arrayObj[cntArr].hideField==true) {
                    continue;
                }
                
                /*
                 *  Hide custom fields for GST Applicable country ERM-1108
                 * 
                 */
                if(WtfGlobal.isIndiaCountryAndGSTApplied() && arrayObj[cntArr].gstConfigType != undefined && arrayObj[cntArr].gstConfigType == Wtf.EWAYFIELDS_GSTCONFIGTYPE){                   
                     if(this.parentObjScope!=undefined && this.parentObjScope.isEWayCheck!=undefined && !this.parentObjScope.isEWayCheck.getValue()){
                             arrayObj[cntArr].disable();                             
                     }else{
                             arrayObj[cntArr].enable();                             
                    }
                }
                if(this.isWindow && Wtf.account.companyAccountPref.splitOpeningBalanceAmount && this.IsCOA){
                    Wtf.getCmp(componenetID + "_" + columnCnt).add(itemObj);
                    columnCnt = 0;
                }else if(this.isWindow && this.moduleid===Wtf.Account_Statement_ModuleId) {
                    Wtf.getCmp(componenetID).add(itemObj);
                    //itemCnt++;
                    columnCnt++;
                    if (columnCnt == 2) {
                        columnCnt = 0;
                    }
                    //                //                    if(itemCnt == rowDimensionCnt) {
                    //                        itemCnt = 0;
                    //                        columnCnt++;                    
                    //                        if (columnCnt == rowPanelCnt) {
                    //                            columnCnt=0;
                    //                        }
                    //                    }
                } else if(this.moduleid===Wtf.Acc_Vendor_ModuleId || this.moduleid===Wtf.Acc_Customer_ModuleId || this.isOpeningTransaction){
                    //Add all dimensions to this fieldset in case of vendor and customer.
                     Wtf.getCmp(componenetID).add(itemObj);
                } else {
                    // Exclude Line level dimensions from this fieldset
                    if(arrayObj[cntArr].iscustomcolumn){
                        continue;
                    }
                    Wtf.getCmp(componenetID).add(itemObj);
                }
            }
        } else if(componenetID=='lineLevelDimensionFieldData'+this.id) {
            var columnCnt = 0;
            var itemCnt = 0;
            var rowPanelCnt = 4;
            var rowDimensionCnt = 4;
            for (var cntArr = 0; cntArr < arrayObj.length; cntArr++) {
               
                if(this.isWindow){
                    itemObj = {
                        xtype: 'panel',
                        border: false,
                        //title: '<div wtf:qtip="' + arrayObj[cntArr].fieldLabel + '">' + Wtf.util.Format.ellipsis(arrayObj[cntArr].fieldLabel, Wtf.CUSTOM_PANEL_TITLE_LENGTH) + '</div>',
                        tabTip: arrayObj[cntArr].fieldLabel,
                        hidden: Wtf.account.companyAccountPref.hierarchicalDimensions ? ((arrayObj[cntArr].parentid == "" || arrayObj[cntArr].parentid == null || arrayObj[cntArr].parentid == undefined) ? false : true) : false,
                        bodyStyle: "padding-left:3px;",
                        columnWidth: .49,
                        border:false,
                        labelWidth: 125,
                        layout: 'form',
                        items: [arrayObj[cntArr]]
                    }
                } else {
                    itemObj = {
                        xtype: 'panel',
                        layout: 'form',
                        columnWidth: .3,
                        border: false,
                        // title: '<div wtf:qtip="' + arrayObj[cntArr].fieldLabel + '">' + Wtf.util.Format.ellipsis(arrayObj[cntArr].fieldLabel, Wtf.CUSTOM_PANEL_TITLE_LENGTH) + '</div>',
                        tabTip: arrayObj[cntArr].fieldLabel,
                        hidden: Wtf.account.companyAccountPref.hierarchicalDimensions ? ((arrayObj[cntArr].parentid == "" || arrayObj[cntArr].parentid == null || arrayObj[cntArr].parentid == undefined) ? false : true) : false,
                        bodyStyle: "padding-left:3px;",
                        border:false,
                        labelWidth: 125,
                        items: [arrayObj[cntArr]]
                    }
                }
                //                if(this.isWindow) {
                //                    if(arrayObj[cntArr].iscustomcolumn){
                //                        Wtf.getCmp(componenetID+"_"+columnCnt).add(itemObj);
                //                        itemCnt++;
                //                        if(itemCnt == rowDimensionCnt) {
                //                            itemCnt = 0;
                //                            columnCnt++;                    
                //                            if (columnCnt == rowPanelCnt) {
                //                        columnCnt=0;
                //                    }
                //                    }
                //                    }
                //                } else {
                /**
                 * ERP-34235
                 */
                if (WtfGlobal.GSTApplicableForCompany() != Wtf.GSTStatus.NONE && arrayObj[cntArr].hideField && arrayObj[cntArr].hideField == true) {
                    continue;
                }
                if(arrayObj[cntArr].iscustomcolumn){
                    Wtf.getCmp(componenetID).add(itemObj);
                }
            //}
            }
        } else {
            var columnCnt = 0;
            for (var cntArr = 0; cntArr < arrayObj.length; cntArr++) {
                // Hide custom fields for GST Applicable country ERM-1108
                if (WtfGlobal.GSTApplicableForCompany() != Wtf.GSTStatus.NONE && arrayObj[cntArr].hideField && arrayObj[cntArr].hideField == true) { 
                    continue;
                }
                if(WtfGlobal.isIndiaCountryAndGSTApplied() && arrayObj[cntArr].gstConfigType != undefined && arrayObj[cntArr].gstConfigType == Wtf.EWAYFIELDS_GSTCONFIGTYPE){
                     if(this.parentObjScope!=undefined && this.parentObjScope.isEWayCheck!=undefined && !this.parentObjScope.isEWayCheck.getValue()){
                             arrayObj[cntArr].disable();                             
                     }else{
                             arrayObj[cntArr].enable(); 
                    }
                }
                Wtf.getCmp(componenetID+columnCnt).add(arrayObj[cntArr]);
                columnCnt++;
                if(this.isWindow && Wtf.account.companyAccountPref.splitOpeningBalanceAmount && this.IsCOA && (columnCnt==1)){
                    columnCnt=0; 
                }else if(this.isWindow && (columnCnt==2)){
                    columnCnt=0;
                }
                if (columnCnt == 3) {
                    columnCnt=0;
                }    
                if(columnCnt == 2 && componenetID.indexOf("customFieldCheckList")==0){
                    columnCnt=0;
                }
            }
        }
        container.add(columnLayoutObj);
        container.doLayout();
    },
    
    cloneRemoteComboStore: function(store) {
        return new Wtf.data.Store({
            proxy: store.proxy,
            reader: store.reader,
            baseParams: store.baseParams,
            autoLoad: store.autoLoad
        });
    },
    addComboBox: function(responseObjData) {
        var storedata = [];
        if (responseObjData.masterdata != null) {
            for (var ctr = 0; ctr < responseObjData.masterdata.length; ctr++) {
                var storerecord = [];
                storerecord.push(responseObjData.masterdata[ctr].id);
                storerecord.push(responseObjData.masterdata[ctr].data);
                storedata.push(storerecord);
            }
        }
        var url = "";
        var baseParams = {};
        var displayField = 'name';
        var valueField = 'id';
        var createStore = true;
        url = "ACCAccountCMN/getCustomCombodata.do";
        baseParams = {
            mode: 2,
            flag: 1,
            fieldid: responseObjData.fieldid,
            isFormPanel: true
        }

        baseParams.hierarchy = true;
        if (createStore) {
            this.ruleTypeStore = new Wtf.data.Store({
                url: url,
                baseParams: baseParams,
                reader: new Wtf.data.KwlJsonReader({
                    root: 'data'
                }, Wtf.ComboReader)
            //                autoLoad:(this.isEdit)?true:false
            });
            if(this.restrictFlagOnDropDownStoreLoanForPaymentModule(responseObjData.fieldtype,responseObjData.gstConfigType)) {
                this.ruleTypeStore.on("load", function() {
                    this.loadCnt++;
                    if (this.loadCnt == this.count) {
                        this.setCmbValues();
                         if (this.isBulkPayment) {
                             /**
                              * Populate custom - dimension data in bulk payment
                              */
                             if(this.parentcompId!=undefined && Wtf.getCmp(this.parentcompId)!=undefined){
                                 Wtf.getCmp(this.parentcompId).fireEvent('populateGlobalDimensionValueInBulkPayment', this);
                             }
                         }
                        if (this.iscallFromTransactionsForm) {
                            Wtf.getCmp(this.parentcompId).fireEvent('populateGlobalDimensionValue', this);
                        }
                    }
                   
                }, this);
            }
        }
        if ((responseObjData.parentid == "" || responseObjData.parentid == undefined || this.isEdit) || responseObjData.isforgstrulemapping || this.isBulkPayment) {
            /**
             * Load single select combo in MP,RP for bulk payment request
             */
            if (this.isBulkPayment) {
                responseObjData.comboremotemode = false;
            }
            /**
             * ERP-32829 isforgstrulemapping
             */
            if(!responseObjData.comboremotemode || responseObjData.fieldtype == "7" || this.isBulkPayment) { // combomode = true if combo is with remote mode
                this.ruleTypeStore.load();
            }
        }
        if (responseObjData.fieldtype == "4") {
            this.pushFormFieldValueData(responseObjData);
            var fieldtooltip = (responseObjData.fieldtooltip=="") ? responseObjData.fieldlabel : responseObjData.fieldtooltip ;
            var fieldlabel = responseObjData.iscustomfield ? (responseObjData.isessential==1) ?"<span wtf:qtip= '"+fieldtooltip+"'>"+responseObjData.fieldlabel+"* </span>" :"<span wtf:qtip= '"+fieldtooltip+"'>"+responseObjData.fieldlabel+"</span>" : (responseObjData.isessential==1) ?responseObjData.fieldlabel+"*" :responseObjData.fieldlabel;
            this.ruleTypeCombo = new Wtf.form.ExtFnComboBox({
                id: responseObjData.fieldname + this.id,
                triggerAction: 'all',
                store: this.ruleTypeStore,
                compid:this.id,
                mode: responseObjData.comboremotemode ? 'remote' : 'local',
                width: 73,
                anchor: '94%',
                xtype:'fncombo',
                childid:responseObjData.childstr,
                hirarchical:true,
                /**
                 * ERP-34339
                 * added itemdescription extra field for Product Tax Class Dimension (GST)
                 */
                extraFields: (WtfGlobal.GSTApplicableForCompany() != Wtf.GSTStatus.NONE && (Wtf.GSTProdCategory == fieldlabel|| Wtf.GSTProdCategory+'*'==fieldlabel)) ? ['itemdescription'] :[],
                displayField: displayField,
                fieldLabel: fieldlabel,
                /**
                 * In case of Avalara Integration, disbale exemption code dimension in case of linked transaction so that user can not change the value
                 */
                disabled:(this.isViewMode || (this.isModuleForAvalara && fieldlabel == Wtf.integration.avalaraExemptionCode && this.parentObjScope && this.parentObjScope.isLinkedTransaction)),
                /**
                 * ERP-32829 
                 * Hide dimension created for GST rule mapping purpose
                 */
                hideField:responseObjData.hideField,
                valueField: valueField,
                name: responseObjData.fieldname,
                hiddenName: responseObjData.fieldname, 
                iscustomcolumn: responseObjData.iscustomcolumn, 
                iscustomfield:responseObjData.iscustomfield, 
                commonFlagforDimAndCustomeField:(responseObjData.iscustomfield || !responseObjData.iscustomfield),//this.commonFlagforDimAndCustomeField flge used to activate or deactivate master items for custom field and dimension field
                parentid:responseObjData.parentid, 
                editable: true,//createStore ? false : true,
                minChars: 2, 
                extraComparisionField:'itemdescription',
                forceSelection:true,
                msgTarget:'qtip',
                triggerClass: createStore ? "" : "dttriggerForTeamLead",
                emptyText: (createStore ? WtfGlobal.getLocaleText("acc.field.Select") : WtfGlobal.getLocaleText("acc.field.Search")) +" "+ WtfGlobal.getLocaleText("acc.field.arecord"),
                allowBlank: (responseObjData.isessential===1)?false:true,
                isModuleForAvalara : this.isModuleForAvalara,
                parentObjScope : this.parentObjScope,
                gstConfigType : responseObjData.gstConfigType, // gstConfigType is only for INDIA and US GST custom dimension
                listWidth: 200,
                fieldtype:responseObjData.fieldtype,
                isessential:responseObjData.isessential,
                autopopulatedefaultvalue:responseObjData.autopopulatedefaultvalue,
                listConfig: {
                    getInnerTpl: function() {
                        return '<div data-qtip="{name}">{name}</div>';
                    }
                },
                listeners: {
                    "beforeselect": this.validateSelection,
                    select: { 
                        fn:function(combo, rec){
                            /**
                            * ERP-32829 
                            * restrict below code
                            */
                            if (responseObjData.isforgstrulemapping==undefined || !responseObjData.isforgstrulemapping) {
                                setRecursiveChildDimensionsValues(this.compid, combo.childid, combo.getValue());
                            }
                            /**
                             * If value of exemption code dimension changes, then calculate and update taxes again
                             * Used in case of Avalara Integration only
                             */
                            if (this.isModuleForAvalara && combo.fieldLabel == Wtf.integration.avalaraExemptionCode && this.parentObjScope) {
                                var productRecordsArr = [];
                                this.parentObjScope.Grid.store.each(function (record, recordIndex, totalRecordCount) {
                                    var tempObj = record.data;
                                    if ((tempObj.pid || tempObj.productid)  && tempObj.quantity) {
                                        tempObj.rowIndex = recordIndex;
                                        productRecordsArr.push(tempObj);
                                    }
                                });
                                getTaxFromAvalaraAndUpdateGrid(this.parentObjScope.Grid, undefined, productRecordsArr);
                            }
                            for (var itemcnt = 0; itemcnt < Wtf.getCmp(this.compid).dimensionFieldArray.length; itemcnt++) {
                                /**
                                 * * GST ERP-32829  = set isShipping flag based on entity selection
                                 */
                                
                                /* block should be executed if & only if
                                 * Selected dimension is "Entity"
                                 */
                                
                                if(WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.NONE && Wtf.getCmp(this.compid).dimensionFieldArrayValues[itemcnt].isMultiEntity && combo.name==Wtf.getCmp(this.compid).dimensionFieldArrayValues[itemcnt].fieldname){
                                    Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).isShipping = CompanyPreferenceChecks.getGSTCalCulationType();//rec.data.isshipping;
                                    if (Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).addressMappingRec != undefined &&
                                            Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).addressDetailRecForGST != undefined) {
                                        if (!this.isModuleForAvalara) {
                                            var obj = {};
                                            obj.tagsFieldset = Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).tagsFieldset;
                                            obj.currentAddressDetailrec = Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).addressDetailRecForGST;
                                            obj.mappingRec = Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).addressMappingRec;
                                            obj.isCustomer = Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).isCustomer;
                                            obj.isShipping = CompanyPreferenceChecks.getGSTCalCulationType();//rec.data.isshipping;
                                            populateGSTDimensionValues(obj);
                                              /**
                                             * IF Entity changed in Make and Receive paymentand payment type is Advanced
                                             */
                                            if (Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).moduleid == Wtf.Acc_Receive_Payment_ModuleId 
                                                    || Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).moduleid == Wtf.Acc_Make_Payment_ModuleId
                                                    || Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).moduleid == Wtf.Acc_Debit_Note_ModuleId
                                                    || Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).moduleid == Wtf.Acc_Credit_Note_ModuleId) {
                                                var productIds = '';
                                                var grid = Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).grid;
                                                if (grid) {
                                                    var array = grid.store.data.items;
                                                    for (var i = 0; i < array.length - 1; i++) {
                                                        var gridRecord = grid.store.getAt(i);
                                                        if (gridRecord.data.productid != undefined && gridRecord.data.productid != '') {
                                                            productIds += gridRecord.data.productid + ",";
                                                        }
                                                    }
                                                    if (productIds != '') {
                                                        getLineTermDetailsAndCalculateGSTForAdvance(Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId), grid , productIds);
                                                    }
                                                }
                                            } else {
                                                processGSTRequest(Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId), Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).Grid);
                                            }
                                        }
                                    }

                                }
                                var fieldId =Wtf.getCmp(this.compid).dimensionFieldArray[itemcnt].id
                                var fieldLabel = Wtf.getCmp(fieldId).fieldLabel
                                if (Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).Grid != undefined) {
                                    Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).Grid.fireEvent('populateDimensionValue', this);
                                } else if(Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).grid != undefined){
                                    Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).grid.fireEvent('populateDimensionValue', this);
                                } else if (Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).ItemDetailGrid != undefined) {
                                    if (Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).ItemDetailGrid.EditorGrid != undefined)
                                         Wtf.getCmp(Wtf.getCmp(this.compid).parentcompId).ItemDetailGrid.EditorGrid.fireEvent('populateDimensionValue', this);
                                }
                            } 
                        }
                    },
                    scope: this
                }
            });
            this.pushFormField(responseObjData,this.ruleTypeCombo);
            /**
             * ERM-210 Dimensions on transaction level
             */
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.view) && !WtfGlobal.EnableDisable(Wtf.UPerm.customFieldDimension, Wtf.Perm.customFieldDimension.addMasterItem) && !this.isForMultiEntity && !(responseObjData.gstConfigType==Wtf.GST_CONFIG_TYPE.UQC || (responseObjData.gstConfigType==Wtf.GST_CONFIG_TYPE.ISFORGST && Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA ))){
                /*
                 * !(responseObjData.gstConfigType==Wtf.GST_CONFIG_TYPE.UQC || (responseObjData.gstConfigType==Wtf.GST_CONFIG_TYPE.ISFORGST && Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA ))
                 *  these two conditions added for UQC and State (Indian Companies)
                 */
                
                this.ruleTypeCombo.mode = 'remote';                               
                this.ruleTypeCombo.addNewFn = this.addMasterItem.createDelegate(this, [responseObjData.fieldid, responseObjData.parentid,responseObjData.gstConfigType]);
                this.ruleTypeCombo.isAddnew = true;
            }            
            if(Wtf.account.companyAccountPref.splitOpeningBalanceAmount && this.IsCOA){
                this.createDistibuteOpeningBalFieldSet(responseObjData);
            }
        } else if (responseObjData.fieldtype == 7) {
            this.pushFormFieldValueData(responseObjData);
            var fieldtooltip = (responseObjData.fieldtooltip=="") ? responseObjData.fieldlabel : responseObjData.fieldtooltip ;
            var fieldlabel = (responseObjData.isessential == 1) ? responseObjData.fieldlabel + "*" : responseObjData.fieldlabel
            var obj = new Wtf.common.Select(Wtf.applyIf({
                multiSelect: true,
                forceSelection: true
            }, {
                id: responseObjData.fieldlabel + this.id,
                triggerAction: 'all',
                store: this.ruleTypeStore,
                mode: 'remote',
                anchor: '94%',
                disabled:this.isViewMode,
                clearTrigger:this.isViewMode ? false : true ,
                displayField: displayField,
                fieldLabel: "<span wtf:qtip= '"+fieldtooltip+"'>"+fieldlabel+"</span>",    
                valueField: 'id',
                hiddenName: responseObjData.fieldname,
                name: responseObjData.fieldname,
                emptyText:WtfGlobal.getLocaleText("acc.field.Select")+" "+ WtfGlobal.getLocaleText("acc.field.arecord"),
                scope: this,
                allowBlank: (responseObjData.isessential===1)?false:true,
                isessential:responseObjData.isessential,
                autopopulatedefaultvalue:responseObjData.autopopulatedefaultvalue,
                listConfig: {
                    getInnerTpl: function() {
                        return '<div data-qtip="{name}">{name}</div>';
                    }
                },
                listeners: {
                    "beforeselect": this.validateSelection,
                    scope: this
                }
            }));
            this.pushFormField(responseObjData,obj);
        }
    },
    
    addMasterItem: function (fieldid, parentid,gstConfigType) {
        addMasterItemWindow(fieldid, true, parentid,gstConfigType);
    },    
    createDistibuteOpeningBalFieldSet: function(responseObjData){
        this.fieldidsString=this.fieldidsString+responseObjData.fieldid+',';
        var recArr = [];
        recArr.push(responseObjData.fieldid);
        recArr.push(responseObjData.fieldlabel);
        var isDistributedOpeningBalnce=false;
        if(!this.isEdit){
            recArr.push(isDistributedOpeningBalnce);//Initially, Distributed Opening Balance checkbox is disabled
        } 
        
        this.distributeOpeningBalanceFields.push(recArr);
        
        this.dosValues = [];
        this.dosEditor = new Wtf.form.NumberField({
            name:'dosEditor',
            allowNegative:false,
            labelStyle:'width:150px;margin-left: 5px;',
            width : 120
        });
        
       this.balTypeStore = new Wtf.data.SimpleStore({
            fields: [{
                name:'typeid',
                type:'boolean'
            }, 'name'],
            data :[[true,'Debit'],[false,'Credit']]
        });
        
        this.balTypeEditor = new Wtf.form.ComboBox({
            store: this.balTypeStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.coa.gridOpeningBalanceType"),
            name:'debitType',
            displayField:'name',
            forceSelection: true,
            allowBlank:false,
            width: this.fieldwidth,
            valueField:'typeid',
            mode: 'local',
            value:true,
            triggerAction: 'all',
            typeAhead:true,
            selectOnFocus:true
        });
         
        this.distributeOpeningBalCM = new Wtf.grid.ColumnModel([
            {
                header: WtfGlobal.getLocaleText("acc.field.Value"),
                dataIndex: 'name',
                align: 'left',
                width: 210,
                renderer: function(a,b,c){
                    if(a!=undefined && a!=''){
                        return "<span wtf:qtip='"+a+"'>"+a+"</span>";
                    }else{
                        return "";
                    }
                }
            },{
                header:WtfGlobal.getLocaleText("acc.rem.193.Mixed"),
                dataIndex:'distributedopeningbalanace',
                align:'center',
                width: 130,
                decimalPrecision: Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
                editor: this.dosEditor,
                renderer:WtfGlobal.currencyDeletedRendererDefaultValueforCOA
            },{
                header:WtfGlobal.getLocaleText("acc.coa.gridOpeningBalanceType"),
                dataIndex:'debitType',
                align:'center',
                width: 125,
                renderer:Wtf.comboBoxRenderer(this.balTypeEditor),
                editor: this.balTypeEditor
            }
        ]);
        
        var url = "";
        var baseParams = {};
        url = "ACCAccountCMN/getCustomCombodata.do";
        baseParams = {
            mode: 2,
            flag: 1,
            fieldid: responseObjData.fieldid,
            accountid: this.accountid
        }
        baseParams.hierarchy = true;
        this.distributeOpeningBalStore = new Wtf.data.Store({
            url: url,
            id: 'store_'+responseObjData.fieldid,
            totalOpeningBalanceAmount:0,
            oldVal:0,
            oldTotalOpeningBalanceAmount:0,
            baseParams: baseParams,
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            }, Wtf.ComboReader)
        });
        
        this.distributeOpeningBalStore.on('load', function(store, record ,option){
            for(var i=0 ; i<store.data.length ; i++){
                var rec = store.data.items[i].data;
                if(rec.field_id!=''){
                    Wtf.getCmp('DistributeOpeningBalanceid_'+rec.field_id).expand(true);
                    var recordArr = [];
                    recordArr.push(rec.id);
                    recordArr.push(rec.distributedopeningbalanace);
                    recordArr.push(rec.debitType);
                    recordArr.push(rec.field_id);
                    this.distributeOpeningBalanceValues.push(recordArr);
                }
            }
        }, this);
        this.distributeOpeningBalStore.load();
        
        this.distributeOpeningBalGrid = new Wtf.grid.EditorGridPanel({
            id: 'distributeOpeningBalGrid_'+responseObjData.fieldid,
            layout: 'fit',
            store: this.distributeOpeningBalStore,//this.distributeOpeningBalStore,
            cm: this.distributeOpeningBalCM,
            clicksToEdit: 1,
            autoScroll: true,
            height: 110,
            width: 480,
            border: false,
            loadMask: true
        });

        this.distributeOpeningBalGrid.on("beforeedit",function(obj, a, b, c, d){
            var rec = obj.record;
            var store = obj.grid.store;
            if(rec.data.distributedopeningbalanace!=''){
                store.oldVal = rec.data.distributedopeningbalanace;
            }else{
                store.oldVal = 0;
            }
            store.oldTotalOpeningBalanceAmount = 0;
            for(var i=0 ; i<store.data.length ; i++){
                if(store.data.items[i].data.distributedopeningbalanace!=''){
                    if(store.data.items[i].data.distributedopeningbalanace<0){
                        store.oldTotalOpeningBalanceAmount += (0 - store.data.items[i].data.distributedopeningbalanace);
                    }else{
                        store.oldTotalOpeningBalanceAmount += store.data.items[i].data.distributedopeningbalanace;
                    }
                }
            }
            store.oldTotalOpeningBalanceAmount = getRoundedAmountValue(store.oldTotalOpeningBalanceAmount);
        }, this);
        
        this.distributeOpeningBalGrid.on("afteredit",function(obj){
            var rec=obj.record;
            var store = obj.grid.store;//this.distributeOpeningBalGrid.store;
            if(rec.data.distributedopeningbalanace!=undefined && rec.data.distributedopeningbalanace!=0){
                store.totalOpeningBalanceAmount=0;
                for(var i=0 ; i<store.data.length ; i++){
                    if(store.data.items[i].data.distributedopeningbalanace!=''){
                        if(store.data.items[i].data.distributedopeningbalanace<0){
                            store.totalOpeningBalanceAmount += (0 - store.data.items[i].data.distributedopeningbalanace);
                        }else{
                            store.totalOpeningBalanceAmount += store.data.items[i].data.distributedopeningbalanace;
                        }                        
                    }
                }
                store.totalOpeningBalanceAmount = getRoundedAmountValue(store.totalOpeningBalanceAmount);
                          
//              
//                var totalOpeningBalanceAmount='';
//                if (this.presentbalance < 0) {
//                    var accountopeningbalance = this.presentbalance * (-1);
//                    totalOpeningBalanceAmount=store.totalOpeningBalanceAmount;
//                } else {
//                    accountopeningbalance = this.presentbalance;
//                    totalOpeningBalanceAmount=store.totalOpeningBalanceAmount;
//                }
//                if (!(totalOpeningBalanceAmount <= accountopeningbalance)) {
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.DistributeOpeningBalanceAlert.msg")], 2);
//                    rec.data.distributedopeningbalanace = store.oldVal;
//                    rec.commit();
//                    store.totalOpeningBalanceAmount = store.oldTotalOpeningBalanceAmount;
//                    return;
//                }
            }    
                
//                if(!(store.totalOpeningBalanceAmount<=this.presentbalance)){
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.DistributeOpeningBalanceAlert.msg")], 2);
//                    rec.data.distributedopeningbalanace = store.oldVal;
//                    rec.commit();
//                    store.totalOpeningBalanceAmount = store.oldTotalOpeningBalanceAmount;
//                    return;
//                }
                
                for(var i=0 ; i<this.distributeOpeningBalanceValues.length ; i++){
                    if(rec.data.id==this.distributeOpeningBalanceValues[i][0]){
                        this.distributeOpeningBalanceValues.remove(this.distributeOpeningBalanceValues[i]);
                    }
                }
                var recordArr = [];
                recordArr.push(rec.data.id);
                recordArr.push(rec.data.distributedopeningbalanace);
                recordArr.push(rec.data.debitType);
                var storeid = store.id;
                storeid = storeid.replace("store_", "");
                recordArr.push(storeid);
                this.distributeOpeningBalanceValues.push(recordArr);
            
        },this);
        
        this.distributeOpeningBalField=new Wtf.form.FieldSet({
            id: 'DistributeOpeningBalanceid_'+responseObjData.fieldid,
            title: "<span wtf:qtip= '"+WtfGlobal.getLocaleText("acc.field.DistributeOpeningBalance")+" "+responseObjData.fieldlabel+"'>"+WtfGlobal.getLocaleText("acc.field.DistributeOpeningBalance")+" "+responseObjData.fieldlabel+"</span>",
            checkboxToggle: true,
            autoHeight: true,
            iscustomcolumn:responseObjData.iscustomcolumn,
            hidden:(responseObjData.parentid=="" || responseObjData.parentid==undefined )?false:true,
            border:false,
            disabledClass:"newtripcmbss",
            checkboxName: 'checkbox_'+responseObjData.fieldid,
            style: 'margin-left:5px',
            collapsed: true,
            items:[this.distributeOpeningBalGrid]
        });

        this.distributeOpeningBalField.on('expand',this.distributeOpeningBalExpand.createDelegate(this,[responseObjData, this.distributeOpeningBalStore, this.dosValues]),this);
        this.distributeOpeningBalField.on('collapse',this.distributeOpeningBalCollapse.createDelegate(this,[responseObjData, this.distributeOpeningBalStore, this.distributeOpeningBalGrid]),this);
        
        this.pushFormField(responseObjData, this.distributeOpeningBalField);
        this.pushFormFieldValueData(responseObjData);
    },
    
    setOBType:function(nature){
       if(this.balTypeEditor){
            this.balTypeEditor.setValue(nature==Wtf.account.nature.Asset||nature==Wtf.account.nature.Expences);        
        }
        var fieldArr = [];
        fieldArr = this.fieldidsString.split(',');
        for (var i = 0; i < fieldArr.length; i++) {
            if (Wtf.getCmp('distributeOpeningBalGrid_' + fieldArr[i]) != undefined) {
                Wtf.getCmp('distributeOpeningBalGrid_' + fieldArr[i]).getStore().each(function(rec) {
                    rec.set('debitType', nature==Wtf.account.nature.Asset||nature==Wtf.account.nature.Expences);
                    this.fireEvent('datachanged',this);
                }, this);
                Wtf.getCmp('distributeOpeningBalGrid_' + fieldArr[i]).getView().refresh(true);
            }
        }        
    },
    
    distributeOpeningBalExpand: function(responseObjData, store, dosValues){
        if(responseObjData.isessential!=1){//If mandaotry then donot reset Custom/Dimension Field's value
            Wtf.getCmp(responseObjData.fieldname + this.id).reset();
        }else{
            WtfGlobal.hideFormElement(Wtf.getCmp(responseObjData.fieldname + this.id));
            
            if(Wtf.getCmp(responseObjData.fieldname + this.id).getValue() == "1234" || Wtf.getCmp(responseObjData.fieldname + this.id).getValue() == ""){
                Wtf.getCmp(responseObjData.fieldname + this.id).setValue(responseObjData.fieldData);//Set default value.
            }
        }
        Wtf.getCmp(responseObjData.fieldname + this.id).setDisabled(true);
        if (responseObjData.childstr != "" && responseObjData.childstr != undefined) {
           hideAllChildDimensions(responseObjData.childstr,this.id);
        }
        
//        if(!this.isEdit){
            for(var j=0;j<this.distributeOpeningBalanceFields.length;j++){
                if(responseObjData.fieldid==this.distributeOpeningBalanceFields[j][0]){
                    this.distributeOpeningBalanceFields[j][2]=true;//If Distribute OPening Balance check box is enabled
                }
            }   
//        }
      
        for(var i=0 ; i<this.distributeOpeningBalanceDeleteFields.length ; i++){
           if(responseObjData.fieldid==this.distributeOpeningBalanceDeleteFields[i]){
                this.distributeOpeningBalanceDeleteFields.remove(this.distributeOpeningBalanceDeleteFields[i]);
            }
        }
        
        //Edit case: Unchecked distibute opening balance then again checked distibute opening balance 
        //this.distributeOpeningBalanceValues array was not containing distribued values
        for(var k=0 ; k<this.tempdistributeOpeningBalanceValues.length ; k++){
            var isPresent = false;
            for(var l=0 ; l<this.distributeOpeningBalanceValues.length ; l++){
                if(this.distributeOpeningBalanceValues[l][0]==this.tempdistributeOpeningBalanceValues[k][0]){
                    isPresent = true;
                    break;
                }
            }
            if(!isPresent){
                this.distributeOpeningBalanceValues.push(this.tempdistributeOpeningBalanceValues[k]);
            }
        }
    },
    setCurrencyid: function(cuerencyid, symbol) {
        this.currencyid = cuerencyid;
        this.symbol = symbol;
        var fieldArr = [];
        fieldArr = this.fieldidsString.split(',');
        for (var i = 0; i < fieldArr.length; i++) {
            if (Wtf.getCmp('distributeOpeningBalGrid_' + fieldArr[i]) != undefined) {
                Wtf.getCmp('distributeOpeningBalGrid_' + fieldArr[i]).getStore().each(function(rec) {
                    rec.set('currencysymbol', symbol)
                }, this)
            }
        }
    },
   
    distributeOpeningBalCollapse: function(responseObjData, store, grid){
        this.tempdistributeOpeningBalanceValues=[];
        for(var k=0 ; k<this.distributeOpeningBalanceValues.length ; k++){
            this.tempdistributeOpeningBalanceValues.push(this.distributeOpeningBalanceValues[k]);
        }
   
        for(var i=0 ; i<grid.store.data.items.length ; i++){
            for(var j=0 ; j<this.distributeOpeningBalanceValues.length ; j++){
                if(grid.store.data.items[i].data.id==this.distributeOpeningBalanceValues[j][0]){
                    this.distributeOpeningBalanceValues.remove(this.distributeOpeningBalanceValues[j]);
                }
            }
        }
        for(var j=0;j<this.distributeOpeningBalanceFields.length;j++){
            if(responseObjData.fieldid==this.distributeOpeningBalanceFields[j][0]){
                this.distributeOpeningBalanceFields[j][2]=false;//If Distribute OPening Balance check box is disabled
            }
        }
        for(var i=0 ; i<this.distributeOpeningBalanceDeleteFields.length ; i++){
            if(responseObjData.fieldid==this.distributeOpeningBalanceDeleteFields[i]){
                this.distributeOpeningBalanceDeleteFields.remove(this.distributeOpeningBalanceDeleteFields[i]);
            }
        }
        this.distributeOpeningBalanceDeleteFields.push(responseObjData.fieldid);
        
//        store.removeAll();
//        if(responseObjData.isMultiEntity){
            WtfGlobal.showFormElement(Wtf.getCmp(responseObjData.fieldname + this.id));
//        }
          Wtf.getCmp(responseObjData.fieldname + this.id).setDisabled(false);
    },
    
    getDistributedDeleteFieldIds: function(){
        var dataArr = [];
        for(var i=0 ; i<this.distributeOpeningBalanceDeleteFields.length ; i++){
            var fieldid = this.distributeOpeningBalanceDeleteFields[i];
            var temp = {
                fieldid: fieldid
            };
            dataArr.push(temp);
        }
        return dataArr;
    },
   
    getDistributedOpeningBalance : function(){
        var dataArr = [];
        for(var i=0 ; i<this.distributeOpeningBalanceValues.length ; i++){
            var comboid = this.distributeOpeningBalanceValues[i][0];
            var distributedopeningbalanace = this.distributeOpeningBalanceValues[i][1];
            var debitType = this.distributeOpeningBalanceValues[i][2];
            if(distributedopeningbalanace==''){
                distributedopeningbalanace=0;
            }
            var temp = {
                comboid: comboid,
                distributedopeningbalanace: distributedopeningbalanace,
                debitType: debitType
            };
            dataArr.push(temp);
        }
        return dataArr;
    },
    
   addCheckList: function(responseObjData) {
        var storedata = [];
        var checkListCheckBoxesArray=[];
        this.checkListCount++;
        var fieldtooltip = (responseObjData.fieldtooltip=="") ? responseObjData.fieldlabel : responseObjData.fieldtooltip ;
        var fieldlabel = responseObjData.fieldlabel
        if (responseObjData.masterdata != null) {
            for (var ctr = 0; ctr < responseObjData.masterdata.length; ctr++) {
                var storerecord = [];
                storerecord.push(responseObjData.masterdata[ctr].id);
                storerecord.push(responseObjData.masterdata[ctr].data);
                storedata.push(storerecord);
            }
        }
        this.checkListFieldSet = new Wtf.form.FieldSet({
            title:"<span wtf:qtip= '"+fieldtooltip+"'>"+fieldlabel+"</span>",
            id:responseObjData.fieldlabel + this.id,
            autoHeight: true,
            autoWidth:true,
            border: false
        });
        var checkListArray=responseObjData.checkList;
        var checkListJSONArray = JSON.parse(checkListArray);
        for(var i=0 ; i<checkListJSONArray.length ; i++){
            checkListCheckBoxesArray.push({
                xtype:"checkbox",
                boxLabel:checkListJSONArray[i].name,
                id:checkListJSONArray[i].id +"_"+ this.id,
                labelPad:10,
                disabled:this.isViewMode,
                parentId:responseObjData.fieldlabel + this.id,
                name:checkListJSONArray[i].name,
                hideLabel:true,
                inputValue:"1"
            });
        }
        for(var j=0 ;j<checkListCheckBoxesArray.length; j++){
            this.checkListCheckBoxesArray.push(checkListCheckBoxesArray[j]);
        }
        this.customCheckListCheckbox  = this.createCheckListFieldColumnModel('customFieldCheckListCheckBoxesData' + this.checkListCount);
        this.addFields(this.checkListFieldSet,this.customCheckListCheckbox,checkListCheckBoxesArray,'customFieldCheckListCheckBoxesData' + this.checkListCount + '_');

        this.loadCnt++;
        if (this.loadCnt == this.count) {
            this.setCmbValues();
        }

        this.checkListArrayValues.push(responseObjData);
        this.checkListArray.push(this.checkListFieldSet);
    },
    
    pushFormField : function(record, field) {
        if(record.iscustomfield)
            this.customFieldArray.push(field);
        else
            this.dimensionFieldArray.push(field);  
    },
    
    pushFormFieldValueData : function(record) {
        if(record.iscustomfield)
            this.customFieldArrayValues.push(record);
        else
            this.dimensionFieldArrayValues.push(record);
    },
    addTextField : function(responseObjData, props){
        this.pushFormFieldValueData(responseObjData);
        var fieldtooltip = (responseObjData.fieldtooltip=="") ? responseObjData.fieldlabel : responseObjData.fieldtooltip ;
        var fieldlabel = (responseObjData.isessential==1) ? responseObjData.fieldlabel+"*" : responseObjData.fieldlabel ;
        var text =new Wtf.ux.TextField(Wtf.applyIf(props||{},{
            id:responseObjData.fieldname+this.id,
            fieldLabel: "<span wtf:qtip= '"+fieldtooltip+"'>"+fieldlabel+"</span>" ,         
            name: responseObjData.fieldname,
            disabled:this.isViewMode,
            anchor: '94%',
            scope: this,
            //anchor:this.attributeAnchor,
            //style:this.attributeStyle,
            maxLength:(responseObjData.maxlength!="0"|| responseObjData.maxlength!=null)? responseObjData.maxlength : Number.MAX_VALUE,
            allowBlank:(responseObjData.isessential===1)?false:true,
            isessential:responseObjData.isessential,
            autopopulatedefaultvalue:responseObjData.autopopulatedefaultvalue,
            hideField:responseObjData.hideField
        }));
        this.pushFormField(responseObjData,text);
    //        this.dimensionFieldArray.push(this.text);
    },
    
    addTextAreaField :  function(responseObjData, props){
        this.pushFormFieldValueData(responseObjData);
        var fieldtooltip = (responseObjData.fieldtooltip=="") ? responseObjData.fieldlabel : responseObjData.fieldtooltip ;
        var fieldlabel = (responseObjData.isessential==1) ? responseObjData.fieldlabel+"*" : responseObjData.fieldlabel ;
        var text =new Wtf.form.TextArea(Wtf.applyIf(props||{},{
            id:responseObjData.fieldname+this.id,
            fieldLabel: "<span wtf:qtip= '"+fieldtooltip+"'>"+fieldlabel+"</span>" ,         
            name: responseObjData.fieldname,
            anchor: '94%',
            disabled:this.isViewMode,
            scope: this,
            //anchor:this.attributeAnchor,
            //style:this.attributeStyle,
//            maxLength:(responseObjData.maxlength!="0"|| responseObjData.maxlength!=null)? responseObjData.maxlength : Number.MAX_VALUE,
            allowBlank:(responseObjData.isessential===1)?false:true,
            qtip:(responseObjData.fieldData==undefined)?' ':responseObjData.fieldData,
            isessential:responseObjData.isessential,
            autopopulatedefaultvalue:responseObjData.autopopulatedefaultvalue,
            listeners: {
                render: function(c){
                    Wtf.QuickTips.register({
                        target: c.getEl(),
                        trackMouse: true,
                        text: c.qtip
                    });
                }
            }
        }));
        this.pushFormField(responseObjData,text);
//        this.dimensionFieldArray.push(this.text);
    },
    addRichTextAreaField :  function(responseObjData, props){
        this.pushFormFieldValueData(responseObjData);
        var fieldtooltip = (responseObjData.fieldtooltip=="") ? responseObjData.fieldlabel : responseObjData.fieldtooltip ;
        var fieldlabel = (responseObjData.isessential==1) ? responseObjData.fieldlabel+"*" : responseObjData.fieldlabel ;
        var text =new Wtf.form.RichPanel(Wtf.applyIf(props||{},{
            id:responseObjData.fieldname+this.id,
            fieldLabel: "<span wtf:qtip= '"+fieldtooltip+"'>"+fieldlabel+"</span>" ,         
            name: responseObjData.fieldname,
            border:false,
            anchor: '94%',
            disabled:this.isViewMode,
            height:60,
            scope: this,
            qtip:(responseObjData.fieldData==undefined)?' ':responseObjData.fieldData,
            autopopulatedefaultvalue:responseObjData.autopopulatedefaultvalue,
            listeners: {
                render: function(c){
                    Wtf.QuickTips.register({
                        target: c.getEl(),
                        trackMouse: true,
                        text: c.qtip
                    });
                }
            }
        }));
        this.pushFormField(responseObjData,text);
    },
    
    addNumberField : function(responseObjData){
        this.pushFormFieldValueData(responseObjData);
        var fieldtooltip = (responseObjData.fieldtooltip=="") ? responseObjData.fieldlabel : responseObjData.fieldtooltip ;
        var fieldlabel = (responseObjData.isessential==1)?responseObjData.fieldlabel+"*":responseObjData.fieldlabel ;
        var numfield = new Wtf.form.NumberField({
            id:responseObjData.fieldname+this.id,
            fieldLabel: "<span wtf:qtip= '"+fieldtooltip+"'>"+fieldlabel+"</span>" ,
            name: responseObjData.fieldname,
//            maxLength:15,
            maxLength: WtfGlobal.isIndiaCountryAndGSTApplied()? this.getMaxLengthForEWAYBillNo(responseObjData) : 15,
            disabled:this.isViewMode,
            anchor: '94%',
            scope: this,
            allowBlank:(responseObjData.isessential===1)?false:true,
            isessential:responseObjData.isessential,
            autopopulatedefaultvalue:responseObjData.autopopulatedefaultvalue,
            hideField:responseObjData.hideField,
            gstConfigType:responseObjData.gstConfigType
        });
        this.pushFormField(responseObjData,numfield);
    },
    addCheckBoxField : function(responseObjData){
        this.pushFormFieldValueData(responseObjData);
        var fieldtooltip = (responseObjData.fieldtooltip=="") ? responseObjData.fieldlabel : responseObjData.fieldtooltip ;
        var fieldlabel = (responseObjData.isessential==1)?responseObjData.fieldlabel+"*":responseObjData.fieldlabel ;
        var chkfield = new Wtf.form.Checkbox({
            id:responseObjData.fieldname+this.id,
            fieldLabel: "<span wtf:qtip= '"+fieldtooltip+"'>"+fieldlabel+"</span>",
            name: responseObjData.fieldname,
            disabled:this.isViewMode,
            maxLength:15,
            fieldType:responseObjData.fieldtype,
            //            anchor: '94%',
            scope: this,
            allowBlank:(responseObjData.isessential===1)?false:true
        });
        this.pushFormField(responseObjData,chkfield);
    },
    addDateField : function(responseObjData){
        this.pushFormFieldValueData(responseObjData);
        var fieldtooltip = (responseObjData.fieldtooltip=="") ? responseObjData.fieldlabel : responseObjData.fieldtooltip ;
        var fieldlabel = (responseObjData.isessential==1)?responseObjData.fieldlabel+"*":responseObjData.fieldlabel ;
        var recname = responseObjData.fieldname;
        var datevalue=undefined;
        if(recname.replace(" ","").toLowerCase()=='startdate'){ // Current Date for start date and end date
            this.sdate = recname;
            datevalue = new Date();
        }
        if(recname.replace(" ","").toLowerCase()=='enddate'){
            this.edate = recname;
        }
        var datefield = new Wtf.form.DateField({
            id:responseObjData.fieldname+this.id,
//            readOnly:true,
            anchor: '94%',
            emptyText:WtfGlobal.getLocaleText("acc.field.--SelectDate--"),
            format:WtfGlobal.getOnlyDateFormat(),
            recordname:responseObjData.fieldname,
            disabled:this.isViewMode,
            fieldLabel: "<span wtf:qtip= '"+fieldtooltip+"'>"+fieldlabel+"</span>",
            name: responseObjData.fieldname,
            allowBlank:(responseObjData.isessential===1)?false:true,
            renderer:WtfGlobal.onlyDateRendererTZ,//WtfGlobal.onlyDateDeletedRenderer,
            //            offset:Wtf.pref.tzoffset,
            value:datevalue,
            isessential:responseObjData.isessential,
            autopopulatedefaultvalue:responseObjData.autopopulatedefaultvalue,
            hideField:responseObjData.hideField,
            gstConfigType:responseObjData.gstConfigType
        });
        
        datefield.on("blur",function(field,newValue,oldValue){
            if(!field.isValid()){
                field.setValue("");
            }
        });
        this.pushFormField(responseObjData,datefield);
    },
    
    validateSelection: function(combo, record, index) {
        //hasaccess is true for activatate dimension value and false for deactivated dimensionvalue
        if(record!=null && record!=undefined){
            return record.get('hasAccess');
        }else{
            return true;
        }
    },
    genFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 1);
    },
    
    restrictFlagOnDropDownStoreLoanForPaymentModule : function(fieldtype, gstConfigType) {
         /**
          * ERP-38917
          * gstConfigType is for State/ City/ County dimension 
          */
        var isGSTRuleMappingColumn = false;
        if ((WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied()) && (gstConfigType != undefined && gstConfigType == 3) && (this.moduleid == Wtf.Acc_Make_Payment_ModuleId || this.moduleid == Wtf.Acc_Receive_Payment_ModuleId)) {
            isGSTRuleMappingColumn = true;
        }
        return (fieldtype == 7 || this.isBulkPayment || isGSTRuleMappingColumn ||(this.moduleid != Wtf.Acc_Make_Payment_ModuleId && this.moduleid != Wtf.Acc_Receive_Payment_ModuleId));
    },
    
    setCmbValues: function() {
//        if (this.isEdit || this.copyInv) {    // ERP-15992   check is removed to set Default values on entry form
            for (var itemcnt = 0; itemcnt < this.dimensionFieldArray.length; itemcnt++) {
                var fieldId = this.dimensionFieldArray[itemcnt].id;
                var fieldValue = this.dimensionFieldArrayValues[itemcnt].fieldData;
                if(Wtf.account.companyAccountPref.splitOpeningBalanceAmount && this.IsCOA && Wtf.getCmp(fieldId).getXType() == "fieldset"){
                    continue;
                }
                if (fieldValue != "" && fieldValue != undefined) {
                    if (this.moduleid == Wtf.Acc_Make_Payment_ModuleId || this.moduleid ==Wtf.Acc_Receive_Payment_ModuleId) {
                        /**
                         * For INDIA and US GST dimension State,City ,County store loaded in local mode
                         * ERP-38917
                         * gstConfigType is for State/ City/ County dimension 
                         */
                        if (WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied()) {// IF US and INDIA and Payment modules 
                            if(this.dimensionFieldArray[itemcnt].gstConfigType!=undefined && this.dimensionFieldArray[itemcnt].gstConfigType==3){
                                Wtf.getCmp(fieldId).setValue(fieldValue); // this condition for Set value in State/City/ County (this dimension store load mode is local)
                            }else{ // IF other than State/City/ County set value (this dimension store load mode is remote for ex. Entity)
                                Wtf.getCmp(fieldId).setValForRemoteStore(fieldValue, this.dimensionFieldArrayValues[itemcnt].fieldDisplayData); 
                            }
                        } else { // IF not US and INDIA and Payment modules 
                            Wtf.getCmp(fieldId).setValForRemoteStore(fieldValue, this.dimensionFieldArrayValues[itemcnt].fieldDisplayData);
                        }
                    } else {
                        Wtf.getCmp(fieldId).setValue(fieldValue);
                    }
                } else if(this.isEdit || this.copyInv){ // ERP-21434
                    if(Wtf.account.companyAccountPref.hierarchicalDimensions && Wtf.getCmp(fieldId).parentid !="" && Wtf.getCmp(fieldId).parentid !=null && Wtf.getCmp(fieldId).parentid!=undefined){
                        Wtf.getCmp(Wtf.getCmp(fieldId).ownerCt.id).hide();
                    }
                     if (this.moduleid == Wtf.Acc_Make_Payment_ModuleId || this.moduleid ==Wtf.Acc_Receive_Payment_ModuleId) {
                         Wtf.getCmp(fieldId).setValForRemoteStore("1234", "None");
                     } else{
                          Wtf.getCmp(fieldId).setValue("1234");
                     }
                }
                //To show child Dimensions if parent contains value.
                this.showChildDimensions(fieldId);
            }
        for (var itemcnt = 0; itemcnt < this.customFieldArray.length; itemcnt++) {
                var CustomfieldId = this.customFieldArray[itemcnt].id
            var CustomfieldValue = this.customFieldArrayValues[itemcnt].fieldData;
            var fieldtype = this.customFieldArrayValues[itemcnt].fieldtype;

            if (CustomfieldValue != "" && CustomfieldValue != undefined) {
                if (this.customFieldArray[itemcnt].getXType() == 'datefield') {
                    CustomfieldValue = new Date(CustomfieldValue);
                    Wtf.getCmp(CustomfieldId).setValue(CustomfieldValue);
                } else if (this.customFieldArray[itemcnt].getXType() == 'fieldset') {
                    var checkBoxId = CustomfieldValue.split(',');
                    for (var i = 0; i < checkBoxId.length; i++) {
                        if(Wtf.getCmp(checkBoxId[i] + "_" + this.id)){
                            Wtf.getCmp(checkBoxId[i] + "_" + this.id).setValue(true);
                        }
                    }
                    } else {
                        if (this.moduleid == Wtf.Acc_Make_Payment_ModuleId || this.moduleid == Wtf.Acc_Receive_Payment_ModuleId) {
                            if(this.customFieldArrayValues[itemcnt].fieldtype==4) {
                                Wtf.getCmp(CustomfieldId).setValForRemoteStore(CustomfieldValue, this.customFieldArrayValues[itemcnt].fieldDisplayData);
                            } 
//                            else if(this.customFieldArrayValues[itemcnt].fieldtype==7) {
//                                Wtf.getCmp(CustomfieldId).setValForRemoteStore(CustomfieldValue, this.customFieldArrayValues[itemcnt].fieldDisplayData);
//                            } 
                            else {
                    Wtf.getCmp(CustomfieldId).setValue(CustomfieldValue);
                }
                        } else {
                            Wtf.getCmp(CustomfieldId).setValue(CustomfieldValue);
            }
        }

            }else if ((this.isEdit || this.copyInv) && (fieldtype == 4 || fieldtype == 7)) {
                if (this.moduleid == Wtf.Acc_Make_Payment_ModuleId || this.moduleid == Wtf.Acc_Receive_Payment_ModuleId) {
                    Wtf.getCmp(CustomfieldId).setValForRemoteStore("1234", "None");
                } else {
                    Wtf.getCmp(CustomfieldId).setValue("1234");
                }
            }
        }
//        }

    },
    showChildDimensions: function(fieldId) {    
        var childIds = Wtf.getCmp(fieldId).childid;
        var compId = Wtf.getCmp(fieldId).compid;
        if(childIds.length>0){
            var childidArray=childIds.split(",");
            for(var i=0;i<childidArray.length;i++){
                if(Wtf.getCmp(childidArray[i]+compId).ownerCt){
                    // If parentChildDimensionRelation feature enable then only show the childs otherwise all dimensions are visible by default
                    if(Wtf.account.companyAccountPref.hierarchicalDimensions && Wtf.getCmp(fieldId).value!="1234" && Wtf.getCmp(fieldId).value!="" && Wtf.getCmp(fieldId).value!=undefined){
                       // Wtf.getCmp(Wtf.getCmp(childidArray[i]+compId).ownerCt.id).setWidth(100);
                        Wtf.getCmp(Wtf.getCmp(childidArray[i]+compId).ownerCt.id).show();
                    }
                    // Load Child Dimensions store in edit case according to its parent dimension values
                    var currentBaseParams = Wtf.getCmp(childidArray[i]+compId).store.baseParams;
                    currentBaseParams.parentid=Wtf.getCmp(fieldId).value;
                    Wtf.getCmp(childidArray[i]+compId).store.baseParams=currentBaseParams;
                    Wtf.getCmp(childidArray[i]+compId).store.load();
                }
            }
            Wtf.getCmp(this.id).doLayout();
        }
    },
    setCheckListValues: function(FieldName) {    
        for (var itemcnt = 0; itemcnt < this.checkListCheckBoxesArray.length; itemcnt++) {
            if(this.checkListCheckBoxesArray[itemcnt].name==FieldName){
                Wtf.getCmp(this.checkListCheckBoxesArray[itemcnt].id).setValue(true);
            }
        }  
    },
    createFieldValuesArray: function() {
        var datar = [];
        for (var itemcnt = 0; itemcnt < this.dimensionFieldArray.length; itemcnt++) {
            var fieldId = this.dimensionFieldArray[itemcnt].id
            if(Wtf.account.companyAccountPref.splitOpeningBalanceAmount && this.IsCOA && Wtf.getCmp(fieldId).getXType()=="fieldset"){
                continue;
            }
            var fieldValue = Wtf.getCmp(fieldId).getValue();
            var isLineItem = this.dimensionFieldArrayValues[itemcnt].iscustomcolumn;
            if (!isLineItem || this.moduleid === Wtf.Account_Statement_ModuleId ||this.moduleid === Wtf.Acc_Vendor_ModuleId ||this.moduleid === Wtf.Acc_Customer_ModuleId  || this.isOpeningTransaction || this.moduleid === Wtf.Acc_Product_Master_ModuleId
                    || this.moduleid === Wtf.Acc_FixedAssets_AssetsGroups_ModuleId) {
//                if (fieldValue != "") {
                    var column_number = this.dimensionFieldArrayValues[itemcnt].column_number;
                    var fieldName = this.dimensionFieldArrayValues[itemcnt].fieldname
                    var temp1 = {
                        filedid: this.dimensionFieldArrayValues[itemcnt].fieldid,
                        refcolumn_name: this.dimensionFieldArrayValues[itemcnt].refcolumn_number,
                        fieldname: this.dimensionFieldArrayValues[itemcnt].fieldname,
                        xtype: this.dimensionFieldArrayValues[itemcnt].fieldtype,
                        fieldid: this.dimensionFieldArrayValues[itemcnt].fieldid
                    }
                    temp1[column_number] = fieldValue;
                    if (fieldValue == "1234")
                        temp1[column_number] = "";
                    temp1[fieldName] = column_number;

                    datar.push(temp1);

//                }
            }
        }
              
        for (var itemcnt = 0; itemcnt < this.customFieldArray.length; itemcnt++) {
            var fieldId = this.customFieldArray[itemcnt].id
                var fieldValue ='' ;
                if(Wtf.account.companyAccountPref.splitOpeningBalanceAmount && this.IsCOA && Wtf.getCmp(fieldId).getXType()=="fieldset"){
                    continue;
                }
                if(Wtf.getCmp(fieldId).getXType()=="fieldset"){
                    for(var CheckListArrayCount = 0 ;CheckListArrayCount <this.checkListCheckBoxesArray.length;CheckListArrayCount ++ ){
                        if(this.checkListCheckBoxesArray[CheckListArrayCount].parentId == fieldId && Wtf.getCmp(this.checkListCheckBoxesArray[CheckListArrayCount].id).getValue() == true){
                            var fieldID=this.checkListCheckBoxesArray[CheckListArrayCount].id.split("_");
                            fieldValue +=fieldID[0]+ ',';
                        }
                    }
                    fieldValue=fieldValue.substring(0,fieldValue.length - 1);
                }else{
                    fieldValue=Wtf.getCmp(fieldId).getValue();
                }

//                if (fieldValue.toString() != "") {                            ERP-16586
                    var fieldObj = this.customFieldArrayValues[itemcnt];
                    var column_number = fieldObj.column_number;
                    var fieldName = fieldObj.fieldname;
                    var fieldxtype = fieldObj.fieldtype;
                    if(fieldObj.fieldtype=='3') {
                        //fieldValue = fieldValue.toString() == ""?'':new Date(fieldValue).getTime();
                        fieldValue = fieldValue.toString() == ""?'':WtfGlobal.convertToGenericDate(fieldValue);
                    }
                    var temp1 = {
                        fieldid: fieldObj.fieldid,
                        refcolumn_name: fieldObj.refcolumn_number,
                        fieldname: fieldObj.fieldname,
                        xtype: fieldObj.fieldtype,
                        fieldDataVal:fieldValue
                    }
                    temp1[column_number] = fieldValue;
                    if(fieldValue == "1234" && (fieldxtype == 4 || fieldxtype == 7))
                        temp1[column_number] = "";
                    temp1[fieldName] = column_number;

                    datar.push(temp1);

//                }
            }

        var settingsdata = {
            "customfield": datar
        };
        return datar;
    },
    /**
     * ERP-32829 
     * Function to create array for GST request
     */
    createGSTDimensionArray: function(obj) {
        var datar = [];
        for (var itemcnt = 0; itemcnt < this.dimensionFieldArray.length; itemcnt++) {
            var fieldId = this.dimensionFieldArray[itemcnt].id
            var fieldValue = Wtf.getCmp(fieldId).getValue();
            var dimvalue = Wtf.getCmp(fieldId).lastSelectionText;
            var column_number = this.dimensionFieldArrayValues[itemcnt].column_number;
            var fieldName = this.dimensionFieldArrayValues[itemcnt].fieldname
            
            /*While linking Purchase Requisition with PO or VQ then  
             * State should be State of Vendor
             */
            if(fieldName=="Custom_State" && obj!=undefined){
               dimvalue=obj.billingState; 
            }
            var temp1 = {
                fieldname: this.dimensionFieldArrayValues[itemcnt].fieldname,
                gstmappingcolnum: this.dimensionFieldArrayValues[itemcnt].gstmappingcolnum,
                dimvalue: dimvalue
            }
            datar.push(temp1);
        }
        var settingsdata = {
            "customfield": datar
        };
        return datar;
    },
    /**
     * Function to check whether
     */
    checkGSTDimensionValues: function(parentObj) {
        if(parentObj.uniqueCase!=Wtf.GSTCustVenStatus.APPLYGST){
            return true;
        }
        var datar = [];
        for (var itemcnt = 0; itemcnt < this.dimensionFieldArray.length; itemcnt++) {
            if (this.dimensionFieldArrayValues[itemcnt].isforgstrulemapping || this.dimensionFieldArrayValues[itemcnt].isformultientity) {
                var fieldId = this.dimensionFieldArray[itemcnt].id
                var dimvalue = Wtf.getCmp(fieldId).getRawValue()
                if (dimvalue == "" || dimvalue == undefined) {
                    return false;
                }
            }
        }
        return true;
    },
    createDimensionValuesArrayForLineItem: function() {
        var datar = [];
        for (var itemcnt = 0; itemcnt < this.dimensionFieldArray.length; itemcnt++) {
            var fieldId = this.dimensionFieldArray[itemcnt].id
            if(Wtf.account.companyAccountPref.splitOpeningBalanceAmount && this.IsCOA && Wtf.getCmp(fieldId).getXType()=="fieldset"){
                continue;
            }
            var fieldValue = Wtf.getCmp(fieldId).getValue();
            var isLineItem = this.dimensionFieldArrayValues[itemcnt].iscustomcolumn;
            if (isLineItem) {
//                if (fieldValue != "") {
                    var column_number = this.dimensionFieldArrayValues[itemcnt].column_number;
                    var fieldName = this.dimensionFieldArrayValues[itemcnt].fieldname
                    var temp1 = {
                        filedid: this.dimensionFieldArrayValues[itemcnt].fieldid,
                        refcolumn_name: this.dimensionFieldArrayValues[itemcnt].refcolumn_number,
                        fieldname: this.dimensionFieldArrayValues[itemcnt].fieldname,
                        xtype: this.dimensionFieldArrayValues[itemcnt].fieldtype,
                        fieldid: this.dimensionFieldArrayValues[itemcnt].fieldid
                    }
                    temp1[column_number] = fieldValue;
                    if (fieldValue == "1234")
                        temp1[column_number] = "";
                    temp1[fieldName] = column_number;

                    datar.push(temp1);

//                }
            }
        }
             
        var settingsdata = {
            "customfield": datar
        };
        return datar;
    },
    checkMendatoryCombo: function() {
        var isValid = true;
        for (var itemcnt = 0; itemcnt < this.dimensionFieldArray.length; itemcnt++) {
            var fieldId = this.dimensionFieldArray[itemcnt].id
            var isMendatory = this.dimensionFieldArrayValues[itemcnt].isessential;
            var iscustomcolumn= this.dimensionFieldArrayValues[itemcnt].iscustomcolumn;
            if(Wtf.account.companyAccountPref.splitOpeningBalanceAmount && this.IsCOA && Wtf.getCmp(fieldId).getXType() == "fieldset"){
                continue;
            }
            var cmbValue = Wtf.getCmp(fieldId).getValue();
            if ((cmbValue == "") && isMendatory === 1 &&!iscustomcolumn) {
                Wtf.getCmp(fieldId).setValue('');
                isValid = false;
            }
            if ((WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied()) && (this.moduleid == Wtf.Acc_FixedAssets_AssetsGroups_ModuleId || this.moduleid == Wtf.Acc_Product_Master_ModuleId)) {
                var fieldName = this.dimensionFieldArrayValues[itemcnt].fieldname;
                isValid = this.validateGSTDimensionForINDIA(cmbValue,fieldName,isValid ,fieldId, isMendatory);
            }// Validate GST Dimension
        }
        for (var itemcnt = 0; itemcnt < this.customFieldArray.length; itemcnt++) {
            var fieldId = this.customFieldArray[itemcnt].id
            var fieldXtype = this.customFieldArrayValues[itemcnt].fieldtype;
            var isMendatory = this.customFieldArrayValues[itemcnt].isessential;
            if(Wtf.getCmp(fieldId).getXType() == "fieldset"){
                continue;
            }
            var cmbValue = Wtf.getCmp(fieldId).getValue();
            if ((cmbValue == "") && isMendatory === 1 && (fieldXtype === 4 || fieldXtype === 7)) {
                Wtf.getCmp(fieldId).setValue('');
                isValid = false;
            }else if ((cmbValue == "") && isMendatory == 1) {
                 Wtf.getCmp(fieldId).setValue('');
                isValid = false;
             }

        }
        return isValid;
    },
    validateGSTDimensionForINDIA: function (cmbValue,fieldName,isValid ,fieldId ,isMendatory) {
        /**
         * Validate Product tax class and HSN/ SAC Code 
         * Both Dimension are Line level dimension and also activate for Asset and Product
         */
        if ((WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied()) && (this.moduleid == Wtf.Acc_FixedAssets_AssetsGroups_ModuleId || this.moduleid == Wtf.Acc_Product_Master_ModuleId)) {
            if (Wtf.getCmp(fieldId) && (cmbValue == "") && isMendatory === 1 && (fieldName == "Custom_" + Wtf.GSTHSN_SAC_Code || fieldName == "Custom_" + Wtf.GSTProdCategory)) {
                Wtf.getCmp(fieldId).setValue('');
                Wtf.getCmp(fieldId).validate();
                isValid = false;
            }//
        }//
        return isValid
     },
     
    checkMandatoryCustomFieldDimension: function () {
        var isValid = true;
        for (var itemcnt = 0; itemcnt < this.dimensionFieldArray.length; itemcnt++) {
            var fieldId = this.dimensionFieldArray[itemcnt].id;
            var isMandatory = this.dimensionFieldArrayValues[itemcnt].isessential;
            var iscustomcolumn = this.dimensionFieldArrayValues[itemcnt].iscustomcolumn;
            if (Wtf.account.companyAccountPref.splitOpeningBalanceAmount && this.IsCOA && Wtf.getCmp(fieldId).getXType() == "fieldset") {
                continue;
            }
            var cmbValue = Wtf.getCmp(fieldId).getValue();
                       if ((cmbValue == "" || cmbValue == "1234") && isMandatory === 1 && !iscustomcolumn) {
                Wtf.getCmp(fieldId).setValue('');
                isValid = false;
            }
            if ((WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied()) && (this.moduleid == Wtf.Acc_FixedAssets_AssetsGroups_ModuleId || this.moduleid == Wtf.Acc_Product_Master_ModuleId)) {
                var fieldName = this.dimensionFieldArrayValues[itemcnt].fieldname;
                isValid = this.validateGSTDimensionForINDIA(cmbValue,fieldName,isValid ,fieldId, isMandatory);
            }// Validate GST Dimension
        }
        for (var itemcnt = 0; itemcnt < this.customFieldArray.length; itemcnt++) {
            var fieldId = this.customFieldArray[itemcnt].id;
            var fieldXtype = this.customFieldArrayValues[itemcnt].fieldtype;
            var isMandatory = this.customFieldArrayValues[itemcnt].isessential;
            if (Wtf.getCmp(fieldId).getXType() == "fieldset") {
                continue;
            }
            var cmbValue = Wtf.getCmp(fieldId).getValue();
            if ((cmbValue == "") && isMandatory === 1 && (fieldXtype === 4 || fieldXtype === 7)) {
                Wtf.getCmp(fieldId).setValue('');
                isValid = false;
            }else if ((cmbValue == "") && isMandatory === 1) {
                 Wtf.getCmp(fieldId).setValue('');
                isValid = false;
             }
        }
        return isValid;
    },
    onRender: function(config) {
        this.loadCnt = 0;
        this.getFieldRecord();

        Wtf.account.CreateCustomFields.superclass.onRender.call(this, config);
    },
    
    resetCustomComponents: function(){
        //Reset Custom Feilds
        if(this.customFieldArray!=null && this.customFieldArray!=undefined && this.customFieldArray!="" ){
            for (var itemcnt = 0; itemcnt < this.customFieldArray.length; itemcnt++) {
                var fieldId = this.customFieldArray[itemcnt].id;
                var isMandatory = this.customFieldArrayValues[itemcnt].isessential;
                var fieldtype = this.customFieldArrayValues[itemcnt].fieldtype;
                var defaultValue = this.customFieldArrayValues[itemcnt].fieldData;
                if (Wtf.getCmp(fieldId) != undefined && this.customFieldArray[itemcnt].getXType() != 'fieldset') {
                    if (fieldtype == 15) {
                        if (isMandatory !== 1) {
                            Wtf.getCmp(fieldId).setValue("");
                        } else if (isMandatory === 1  && this.customFieldArrayValues[itemcnt].autopopulatedefaultvalue){
                            Wtf.getCmp(fieldId).setValue(defaultValue);
                        }
                    } else if (fieldtype == 3) {
                        if (isMandatory !== 1) {
                            Wtf.getCmp(fieldId).reset();
                        } else if(isMandatory === 1 && this.customFieldArrayValues[itemcnt].autopopulatedefaultvalue){
                            Wtf.getCmp(fieldId).setValue(new Date(defaultValue));
                        }
                    } else {
                        if (isMandatory !== 1) {
                            if (!Wtf.getCmp(fieldId).disabled) {
                                Wtf.getCmp(fieldId).reset();
                            }
                        } else if (isMandatory === 1  && this.customFieldArrayValues[itemcnt].autopopulatedefaultvalue){
                            Wtf.getCmp(fieldId).setValue(defaultValue);
                        }
                    }

                    if (Wtf.getCmp(fieldId) != undefined && this.customFieldArray[itemcnt].getXType() == 'checkbox') {
                        Wtf.getCmp(fieldId).setValue(false);
                    }
                }
            }
        }
        //Reset Check List
       if(this.checkListCheckBoxesArray!=null && this.checkListCheckBoxesArray!=undefined && this.checkListCheckBoxesArray!="" ){
        for (var checkitemcnt = 0; checkitemcnt < this.checkListCheckBoxesArray.length; checkitemcnt++) {
            var checkfieldId = this.checkListCheckBoxesArray[checkitemcnt].id
            if (Wtf.getCmp(checkfieldId) != undefined) {
                Wtf.getCmp(checkfieldId).reset();
            }
        }
       }
        //Reset Custom Dimensions
        if(this.dimensionFieldArray!=null && this.dimensionFieldArray!=undefined && this.dimensionFieldArray!="" ){
            for (var itemcnt1 = 0; itemcnt1 < this.dimensionFieldArray.length; itemcnt1++) {
                var fieldId1 = this.dimensionFieldArray[itemcnt1].id;
                var isMandatory = this.dimensionFieldArrayValues[itemcnt1].isessential;
                var defaultValue = this.dimensionFieldArrayValues[itemcnt1].fieldData;
                if (Wtf.getCmp(fieldId1) != undefined ) {
                    if (isMandatory !== 1) {
                        Wtf.getCmp(fieldId1).reset();
                    } else if (isMandatory === 1  && this.dimensionFieldArrayValues[itemcnt1].autopopulatedefaultvalue){
                        Wtf.getCmp(fieldId1).setValue(defaultValue);
                    }
                }
            }
        }
    },
    getInvalidCustomFields: function(){
        var invalidFields=[];
               for (var itemcnt = 0; itemcnt < this.dimensionFieldArray.length; itemcnt++) {
            var fieldId = this.dimensionFieldArray[itemcnt].id
            var isMendatory = this.dimensionFieldArrayValues[itemcnt].isessential;
            var cmbValue = Wtf.getCmp(fieldId).getValue();
            if ((cmbValue == "" || cmbValue == "1234") && isMendatory === 1) {
                invalidFields.push(this.dimensionFieldArray[itemcnt]);
            }
        }
        for (var itemcnt = 0; itemcnt < this.customFieldArray.length; itemcnt++) {
            var fieldId = this.customFieldArray[itemcnt].id
            var isMendatory = this.customFieldArrayValues[itemcnt].isessential;
            if(Wtf.getCmp(fieldId).getXType() == "fieldset"){
                continue;
            }
            var cmbValue = Wtf.getCmp(fieldId).getValue();
            if ((cmbValue == "" || cmbValue == "1234") && isMendatory === 1) {
                invalidFields.push(this.customFieldArray[itemcnt]);
            }
        }
        return invalidFields;
    },    
    setValuesForCustomer: function (module,recId,fetchdataid) {
        Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/getFieldParams.do",
            params: {
                moduleid: module,
                jeId: recId,
                isOpeningTransaction: false,
                isautopopulatedata:true,
                fetchdataid:fetchdataid
    }
        }, this, this.genSuccessResponsepopulateData, this.genFailureResponse);

    },
    genSuccessResponsepopulateData: function (responseObj) {
        for (var itemcnt = 0; itemcnt < this.dimensionFieldArray.length; itemcnt++) {
            var fieldId = this.dimensionFieldArray[itemcnt].id
            var fieldLabel = this.dimensionFieldArrayValues[itemcnt].fieldlabel;
            for (var custItem = 0; custItem < responseObj.data.length; custItem++) {
                var custFieldLabel = responseObj.data[custItem].fieldlabel
                if(responseObj.data[custItem].isforgstrulemapping!=undefined && responseObj.data[custItem].isforgstrulemapping==true){
                    continue;
                }
                var custValue = responseObj.data[custItem]._Values
                if (fieldLabel == custFieldLabel) {
                    if (custValue != "" && custValue != undefined) {
                        if (this.moduleid == Wtf.Acc_Make_Payment_ModuleId || this.moduleid == Wtf.Acc_Receive_Payment_ModuleId) {
                            fieldValue = responseObj.data[custItem].id_Values;
                        } else {
                        var record = WtfGlobal.searchRecord(Wtf.getCmp(fieldId).store, custValue, "name");
                        if (record != undefined)
                            var fieldValue = record.data.id;
                        }

                        if (fieldValue != "" && fieldValue != undefined) {
                            if (this.moduleid == Wtf.Acc_Make_Payment_ModuleId || this.moduleid == Wtf.Acc_Receive_Payment_ModuleId) {
                                Wtf.getCmp(fieldId).setValForRemoteStore(fieldValue, custValue);
                            } else {
                                /*
                                 * 'this.iscallFromTransactionsForm' -while Generating PO/SO Don't reset value if field already contains value 
                                 */
                                if (this.iscallFromTransactionsForm) {
                                    if (Wtf.getCmp(fieldId).getValue() == '' || Wtf.getCmp(fieldId).getValue() == null) {
                                        Wtf.getCmp(fieldId).setValue(fieldValue);
                                    }
                                } else {
                                    Wtf.getCmp(fieldId).setValue(fieldValue);
                                }
                                var index = Wtf.getCmp(fieldId).store.indexOf(record);
                                if (record != undefined && index != -1){
                                    Wtf.getCmp(fieldId).onSelect(record, index);
                                }
                            }
                        } else {
                            Wtf.getCmp(fieldId).setValue("1234");
                        }
                    }
                    /**
                     * ERM - 294
                     * Set Default Value Of Avalara Tax Exemption Code To None
                     */
                    else if(this.isModuleForAvalara){
                        Wtf.getCmp(fieldId).setValue("1234");
                    }
                }
            }

        }
        for (var itemcnt = 0; itemcnt < this.customFieldArray.length; itemcnt++) {
            var CustomfieldId = this.customFieldArray[itemcnt].id
            var xtype = this.customFieldArrayValues[itemcnt].fieldtype
            var fieldLabel = this.customFieldArrayValues[itemcnt].fieldlabel
            for (var custItem = 0; custItem < responseObj.data.length; custItem++) {
                var custFieldLabel = responseObj.data[custItem].fieldlabel
                var fieldData = responseObj.data[custItem].fieldData
                var custValue = responseObj.data[custItem]._Values
                var CustomfieldValue = "";
                if (fieldLabel == custFieldLabel) {
                    if ((custValue != "" && custValue != undefined) || (fieldData != "" && fieldData != undefined)) {
                        if (xtype == 4 || xtype == 7) {
                            if (xtype == 4 &&(this.moduleid == Wtf.Acc_Make_Payment_ModuleId || this.moduleid == Wtf.Acc_Receive_Payment_ModuleId)) {
                                CustomfieldValue=responseObj.data[custItem].id_Values
                            } else {
                            custValue = custValue.split(",");
                            for (var i = 0; i < custValue.length; i++) {
                                var temp = custValue[i];
                                var record = WtfGlobal.searchRecord(Wtf.getCmp(CustomfieldId).store, temp, "name");
                                if (record != undefined)
                                    CustomfieldValue = CustomfieldValue + record.data.id + ",";
                            }
                            CustomfieldValue = CustomfieldValue.substring(0, CustomfieldValue.length - 1);
                            }

                        } else if (xtype == 12) {
                            var checklist = this.customFieldArrayValues[itemcnt].checkList
                            if (checklist != undefined && checklist != "") {
                                checklist = JSON.parse(checklist)
                                custValue = custValue.split(",");
                                for (var j = 0; j < checklist.length; j++) {
                                    for (var i = 0; i < custValue.length; i++) {
                                        var temp = custValue[i];
                                        if (temp == checklist[j].name) {
                                            CustomfieldValue = CustomfieldValue + checklist[j].id + ",";
                                        }
                                    }
                                }
                            }
                            CustomfieldValue = CustomfieldValue.substring(0, CustomfieldValue.length - 1);
                        } else {
                            CustomfieldValue = fieldData
                        }
                        if (CustomfieldValue != "" && CustomfieldValue != undefined) {
                            if (this.customFieldArray[itemcnt].getXType() == 'datefield') {
                                CustomfieldValue = new Date(CustomfieldValue);
                                 /*
                                  * 'this.iscallFromTransactionsForm' -while Generating PO/SO Don't reset value if field already contains value 
                                 */
                                 if (this.iscallFromTransactionsForm) {
                                    if (Wtf.getCmp(CustomfieldId).getValue() == '' || Wtf.getCmp(CustomfieldId).getValue() == null) {
                                        Wtf.getCmp(CustomfieldId).setValue(CustomfieldValue);
                                    }
                                } else {
                                    Wtf.getCmp(CustomfieldId).setValue(CustomfieldValue);
                                }
                                
                            } else if (this.customFieldArray[itemcnt].getXType() == 'fieldset') {
                                var checkBoxId = CustomfieldValue.split(',');
                                for (var i = 0; i < checkBoxId.length; i++) {
                                    /*
                                     * 'this.iscallFromTransactionsForm' -while Generating PO/SO Don't reset value if field already contains value 
                                     */
                                    if (this.iscallFromTransactionsForm) {
                                        if (Wtf.getCmp(checkBoxId[i] + "_" + this.id).getValue()=='') {
                                            Wtf.getCmp(checkBoxId[i] + "_" + this.id).setValue(true);
                                        }
                                    } else {
                                        Wtf.getCmp(checkBoxId[i] + "_" + this.id).setValue(true);
                                    }
                                }
                            } else {
                                if (this.moduleid == Wtf.Acc_Make_Payment_ModuleId || this.moduleid == Wtf.Acc_Receive_Payment_ModuleId) {
                                    if (this.customFieldArrayValues[itemcnt].fieldtype == 4) {
                                        Wtf.getCmp(CustomfieldId).setValForRemoteStore(CustomfieldValue, custValue);
                                    }
                                    else {
                                        Wtf.getCmp(CustomfieldId).setValue(CustomfieldValue);
                                    }
                                } else {
                                    /*
                                     * 'this.iscallFromTransactionsForm' -while Generating PO/SO Don't reset value if field already contains value 
                                    */
                                    if (this.iscallFromTransactionsForm) {
                                        if (Wtf.getCmp(CustomfieldId).getValue() == '' || Wtf.getCmp(CustomfieldId).getValue() == null) {
                                            Wtf.getCmp(CustomfieldId).setValue(CustomfieldValue);
                                        }
                                    } else {
                                        Wtf.getCmp(CustomfieldId).setValue(CustomfieldValue);
                                    }
                                }
                            }

                        }

                    }

                }
            }
        }
    },
    /**
     * There will be either all E-way related fields empty or All fields should be filled up.
     * ERP-39530(ERM-1108)
     * E-Way unit fields activated in below modules (Module id's where E-Way Fields Present- 27, 28, 31, 29, 2, 6)
     */
    validateEwayRelatedFields: function (parentObj) {
        var returnArr=[];
        var invalid=false;
        var fieldsJSON={}; // created JSON to store {fieldName(key),fieldId(value)}
        var emptyCount=0;
        /**
         * Check Eway related Custom dimension data
         */
        for (var itemcnt = 0; itemcnt < this.dimensionFieldArray.length; itemcnt++) {
            var fieldId = this.dimensionFieldArray[itemcnt].id;
            if (Wtf.getCmp(fieldId).getXType() == "fieldset") {
                continue;
            }
            var gstConfigType = this.dimensionFieldArrayValues[itemcnt].gstConfigType;
            var dimName =this.dimensionFieldArrayValues[itemcnt].fieldname;// Getting fieldname    
            if (gstConfigType != undefined && gstConfigType == Wtf.EWAYFIELDS_GSTCONFIGTYPE) {
                fieldsJSON[dimName] = fieldId;  
                if(Wtf.getCmp(fieldId).getValue()!=undefined && Wtf.getCmp(fieldId).getValue()==''){
                    emptyCount++;
                }
            }
            }
        /**
         * Check Eway related Custom fileds data
         */
        for (var itemcnt = 0; itemcnt < this.customFieldArray.length; itemcnt++) {
            var fieldId = this.customFieldArray[itemcnt].id;
            if (Wtf.getCmp(fieldId).getXType() == "fieldset") {
                continue;
            }
            var gstConfigType = this.customFieldArrayValues[itemcnt].gstConfigType;
            if (gstConfigType != undefined && gstConfigType == Wtf.EWAYFIELDS_GSTCONFIGTYPE) {
               var fieldName = this.customFieldArrayValues[itemcnt].fieldname;
                fieldsJSON[fieldName] = fieldId;   
                if(Wtf.getCmp(fieldId).getValue()!=undefined && Wtf.getCmp(fieldId).getValue()==''){
                    emptyCount++;
                }
        }
        }
        
        if(this.validateTransporterIdAndVehicleNo(fieldsJSON) || this.validateTransDocNoTransDateVehicleType(fieldsJSON)||this.mandetoryEwayFields(fieldsJSON)||this.validateVehicleNoAndTransprterName(fieldsJSON)){ //  coditional validation on Transporter Doc ID,Vehicle No.,Transporter type,Transporter name,vehicle type
            invalid = true; 
        }        
        returnArr.push(invalid);        
        returnArr.push(emptyCount);        
        return returnArr;
    },
    /*
     * 
     * @param {type} fieldsJSON
     * @returns {Boolean}
     * Code for Transporter ID must be filled when Trasport Mode is 'Road' and  Vehicle Number is not mentioned.
     * as well as Vehicle No. must be filled when Trasport Mode is 'Road' and  Transporter Id is not mentioned
     * ERM-1108(ERP-40862)
     */
    validateTransporterIdAndVehicleNo:function(fieldsJSON){
      var transportModeComboId=fieldsJSON[Wtf.EWAYField_Transport_Mode]!=undefined?fieldsJSON[Wtf.EWAYField_Transport_Mode]:"";
      var vehicleNoId = fieldsJSON[Wtf.EWAYField_Vehicle_No]!=undefined?fieldsJSON[Wtf.EWAYField_Vehicle_No]:"";
      var transportationIdCombo = fieldsJSON[Wtf.EWAYField_Transporter_Id] !=undefined?fieldsJSON[Wtf.EWAYField_Transporter_Id]:"";
      var invalid=false;
        if (transportModeComboId != '' && Wtf.getCmp(transportModeComboId) != undefined && Wtf.getCmp(vehicleNoId) != undefined) {
            if (typeof Wtf.getCmp(transportModeComboId).getRawValue == 'function') {
                var TM_Value = Wtf.getCmp(transportModeComboId) != undefined ? Wtf.getCmp(transportModeComboId).getRawValue() : '';
                var VN_Value = Wtf.getCmp(vehicleNoId) != undefined ? Wtf.getCmp(vehicleNoId).getRawValue() : '';
                var TR_Id = Wtf.getCmp(transportationIdCombo) != undefined ? Wtf.getCmp(transportationIdCombo).getRawValue() : '';
                /*
                 * Field 'Transporter ID' must be filled when 'Trasport Mode' is 'Road' and  'Vehicle Number' is not mentioned.
                 */
                if ((TM_Value.trim().toUpperCase() == 'ROAD' && (VN_Value == undefined || VN_Value == '' || VN_Value.trim().toUpperCase() == 'NONE'))) {
                    if((TR_Id == undefined || TR_Id == '' || TR_Id.trim().toUpperCase() == 'NONE')){                        
                          WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.GST.eway.validation.transId")], 2);
                          invalid=true;
                   }
                } else {
                    //Field 'Vehicle No.' must be filled when 'Trasport Mode' is 'Road' and  'Transporter Id' is not mentioned.
                    if ((TM_Value.trim().toUpperCase() == 'ROAD' && (TR_Id == undefined || TR_Id == '' || TR_Id.trim().toUpperCase() == 'NONE'))) {
                        if((VN_Value == undefined || VN_Value == '' || VN_Value.trim().toUpperCase() == 'NONE')){                           
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.GST.eway.validation.vehicleNo")], 2);
                            invalid=true;                       
                        }
                    }
                }
            }
        }
        return invalid;
    },
    /*
     * 
     * @param {type} fieldsJSON
     * @returns {Boolean}
     * Code for - If Transportation mode is 'RAIL/AIR/SHIP' then 'Vehicle Type','Transporter Doc No' and 'Transportation Date' should be filled.
     * ERM-1108(ERP-40862)
     */
    validateTransDocNoTransDateVehicleType:function(fieldsJSON){
      var transportModeComboId=fieldsJSON[Wtf.EWAYField_Transport_Mode]!=undefined?fieldsJSON[Wtf.EWAYField_Transport_Mode]:"";
      var vehicleTypeId = fieldsJSON[Wtf.EWAYField_Vehicle_Type]!=undefined?fieldsJSON[Wtf.EWAYField_Vehicle_Type]:"";
      var transportationDocNo = fieldsJSON[Wtf.EWAYField_Transporter_Doc_No] !=undefined?fieldsJSON[Wtf.EWAYField_Transporter_Doc_No]:"";
      var transportationDate = fieldsJSON[Wtf.EWAYField_Transportation_date] !=undefined?fieldsJSON[Wtf.EWAYField_Transportation_date]:"";
      var msg='';
      var emptyFieldNames='';
      var invalid=false;
        if (Wtf.getCmp(transportModeComboId) != undefined && Wtf.getCmp(transportationDocNo) != undefined && Wtf.getCmp(transportationDate) != undefined) {
            if (typeof Wtf.getCmp(transportModeComboId).getRawValue == 'function') {
                var TM_Value = Wtf.getCmp(transportModeComboId) != undefined ? Wtf.getCmp(transportModeComboId).getRawValue() : '';
                var VT_Value = Wtf.getCmp(vehicleTypeId) != undefined ? Wtf.getCmp(vehicleTypeId).getRawValue() : '';
                var TDN_Value = Wtf.getCmp(transportationDocNo) != undefined ? Wtf.getCmp(transportationDocNo).getValue() : '';
                emptyFieldNames = TDN_Value == '' && Wtf.getCmp(transportationDocNo) ? Wtf.getCmp(transportationDocNo).fieldLabel + "," : '';
                var TD_Value = Wtf.getCmp(transportationDate) != undefined ? Wtf.getCmp(transportationDate).getValue() : '';
                emptyFieldNames += TD_Value == '' && Wtf.getCmp(transportationDate) != undefined ? Wtf.getCmp(transportationDate).fieldLabel : '';
                // If Transportation mode is 'RAIL/AIR/SHIP' then 'Vehicle Type','Transporter Doc No' and 'Transportation Date' should be filled.
                if (((TM_Value.trim().toUpperCase() == 'RAIL')||(TM_Value.trim().toUpperCase() == 'AIR')||(TM_Value.trim().toUpperCase() == 'SHIP')) && ((TDN_Value == undefined || TDN_Value == '') || (TD_Value == undefined || TD_Value == ''))){                    
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), emptyFieldNames+WtfGlobal.getLocaleText("acc.GST.eway.validation.transDocNoAndtransDate")], 2);
                    invalid = true;
                }
                if((TM_Value.trim().toUpperCase() == 'ROAD') && (VT_Value == undefined || VT_Value == '' || VT_Value.trim().toUpperCase()=='NONE')){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.GST.eway.validation.vehicleType")], 2);
                    invalid = true;
                }
            }
        }
        return invalid;
    },
    /*
     * 
     * @param {type} fieldsJSON
     * @returns {Boolean}
     * Code for - Make following fields mandetory 
     *            'Sub type','Document Type','Document No','Dispatch state','Ship to state','Distance level (Km).         
     * ERM-1108(ERP-40862)
     */
    mandetoryEwayFields: function (fieldsJSON) {
        var invalid = false;
        var msg='';
        var mandetoryFieldNames='';
        var mandetoryFieldsArr = [
            Wtf.EWAYField_Supply_type
                    , Wtf.EWAYField_Sub_Type
                    , Wtf.EWAYField_Document_Type                                      
                    , Wtf.EWAYField_Dispatch_State
                    , Wtf.EWAYField_Ship_To_State
                    , Wtf.EWAYField_Distance_level

        ]
        for (var field = 0; field < mandetoryFieldsArr.length; field++) {
            if (fieldsJSON[mandetoryFieldsArr[field]] != undefined) {
                var value = Wtf.getCmp(fieldsJSON[mandetoryFieldsArr[field]]) != undefined ?Wtf.getCmp(fieldsJSON[mandetoryFieldsArr[field]]).getValue():'';
                if (Wtf.getCmp(fieldsJSON[mandetoryFieldsArr[field]]) != undefined) {
                    if (value == "" || value == undefined || value=='1234' || value=='NONE') {
                        invalid = true;
                        mandetoryFieldNames += Wtf.getCmp(fieldsJSON[mandetoryFieldsArr[field]]).fieldLabel + ",";                             
                    }
                }
            }
    } 
        mandetoryFieldNames=mandetoryFieldNames.substring(0,(mandetoryFieldNames.length-1));
        if (invalid) {           
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.GST.eway.validation.mandetoryFields") + mandetoryFieldNames], 2);
        }
        return invalid;
    },
    /*
     * 
     * @param {type} fieldsJSON
     * @returns {Boolean}
     * Code for - Not any other special charactrers are allowed except A-Za-z0-9#/, &\n-   in field  'Transporter Name' and 'Vehicle Number'.
     * ERM-1108(ERP-40862)
     */
    validateVehicleNoAndTransprterName: function (fieldsJSON) {
        var invalid = false;
        var vehicleNoId = fieldsJSON[Wtf.EWAYField_Vehicle_No] != undefined ? fieldsJSON[Wtf.EWAYField_Vehicle_No] : "";
        var transporterNameId = fieldsJSON[Wtf.EWAYField_Transporter_Name] != undefined ? fieldsJSON[Wtf.EWAYField_Transporter_Name] : "";
        if (vehicleNoId != '') {
            var vehicleNo = Wtf.getCmp(vehicleNoId) != undefined ? Wtf.getCmp(vehicleNoId).getRawValue() : '';
            if (vehicleNo != '' || vehicleNo.trim().toUpperCase() == 'NONE') {
                var regex =/^[A-Za-z0-9\/#, &\n-]*$/;
                if (!regex.test(vehicleNo)) {// Regular expression check for special charaters A-Za-z0-9#/, &\n-
                    invalid = true;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.GST.eway.validation.invalidVehicleNo")], 2);
                }
            }
        }
        if (transporterNameId != '') {
            var transporterName = Wtf.getCmp(transporterNameId).getRawValue();
            if (transporterName != '' || transporterName.trim().toUpperCase() == 'NONE') {
                var regex = /^[A-Za-z0-9\/#, &\n-]*$/;
                if (!regex.test(transporterName)) {// Regular expression check for special charaters A-Za-z0-9#/, &\n-
                    invalid = true;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.GST.eway.validation.invalidTransporterName")], 2);
                }
            }
        }
        return invalid;
    },
    /*
     * For India country - Custom field "E-WAY Bill No." should have Max Length = 12.
     * ERP-40367
     */
    getMaxLengthForEWAYBillNo:function(responseObjData){
        var EWAYFieldMaxLength=15;       
        if (WtfGlobal.isIndiaCountryAndGSTApplied() && responseObjData.fieldname != undefined && responseObjData.fieldname == Wtf.EWAYField_Bill_No) {
            EWAYFieldMaxLength = Wtf.EWAYField_Bill_No_maxlength;
        }else if (WtfGlobal.isIndiaCountryAndGSTApplied() && responseObjData.fieldname != undefined && responseObjData.gstConfigType == Wtf.GST_CONFIG_TYPE.CUSTOM_TO_ENTITY && responseObjData.fieldname == Wtf.CUSTOM_PIN_CODE){
            /**
             * Pin Code Entity Custom fields value should be of 6 max length
             */
            EWAYFieldMaxLength = Wtf.CUSTOM_PIN_CODE_maxlength;
        }
        return EWAYFieldMaxLength;
    },
    EnableDisableEwayRelatedFields:function(parentObj){
        /**
         * Check Eway related Custom dimension data
         */
        for (var itemcnt = 0; itemcnt < this.dimensionFieldArray.length; itemcnt++) {
            var fieldId = this.dimensionFieldArray[itemcnt].id;
            if (Wtf.getCmp(fieldId).getXType() == "fieldset") {
                continue;
            }
            var gstConfigType = this.dimensionFieldArrayValues[itemcnt].gstConfigType;
            if (gstConfigType != undefined && gstConfigType == Wtf.EWAYFIELDS_GSTCONFIGTYPE) {               
                if(parentObj!=undefined && parentObj.isEWayCheck!=undefined && !parentObj.isEWayCheck.getValue()){
                    Wtf.getCmp(fieldId).disable();
                }else{
                    Wtf.getCmp(fieldId).enable();
                }
            }
         }
        /**
         * Check Eway related Custom fileds data
         */
        for (var itemcnt = 0; itemcnt < this.customFieldArray.length; itemcnt++) {
            var fieldId = this.customFieldArray[itemcnt].id;
            if (Wtf.getCmp(fieldId).getXType() == "fieldset") {
                continue;
            }
            var gstConfigType = this.customFieldArrayValues[itemcnt].gstConfigType;
            if (gstConfigType != undefined && gstConfigType == Wtf.EWAYFIELDS_GSTCONFIGTYPE) {               
                if(parentObj!=undefined && parentObj.isEWayCheck!=undefined && !parentObj.isEWayCheck.getValue()){
                    Wtf.getCmp(fieldId).disable();
                }else{
                    Wtf.getCmp(fieldId).enable();
                }
            }
        }
    }
});
