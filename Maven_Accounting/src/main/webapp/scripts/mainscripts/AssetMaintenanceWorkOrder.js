Wtf.account.AssetMaintenanceWorkOrder = function(config){

this.externalcurrencyrate=0;

this.isEdit = false;

if(config.scheduleEventRec.get('workJobId') !== null && config.scheduleEventRec.get('workJobId') !== undefined && config.scheduleEventRec.get('workJobId') !== '') {
    this.isEdit = true;
}

    Wtf.apply(this,{
        buttons:[this.saveButton = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                    minWidth: 50,
                    scope: this,
                    handler: this.saveData.createDelegate(this)
            }),this.closeButton = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
                    minWidth: 50,
                    scope: this,
                    handler: this.closeOpenWin.createDelegate(this)
            })]
    },config);
    
    Wtf.account.AssetMaintenanceWorkOrder.superclass.constructor.call(this, config);
    
    this.addEvents({
        'workOrderSaved':true
    });
}

Wtf.extend(Wtf.account.AssetMaintenanceWorkOrder,Wtf.Window, {

    onRender: function(config) {
        Wtf.account.AssetMaintenanceWorkOrder.superclass.onRender.call(this, config);
        
        // create form
        
        this.createWorkOrderForm();
        
        // Crete Work Order Product Details Grid
        
        this.createWorkOrderProductDetailsGrid();
        
        // Set Asset Value in Work Order Form
        
        this.setWorkValue();
        
        this.add({
                region : 'north',
                height : 230,
                border : false,
//                id:'work_order_form_id',
                bodyStyle : 'border-bottom:1px solid #bfbfbf;',
                baseCls:'bckgroundcolor',
                items:[this.workOrderForm]
            },{
                region : 'center',
                border : false,
                layout: 'fit',
                items:[this.Grid]
            }
        );
        
    },
    
    createWorkOrderProductDetailsGrid:function(){
        
        this.Grid=new Wtf.account.WorkOrderProductDetailsGrid({//DeliveryOrderGrid
            height: 200,
            cls:'gridFormat',
            layout:'fit',
            viewConfig:{
                forceFit:true
            },
            isCustomer:true,
            editTransaction:this.isEdit,
            disabled:false,
            disabledClass:"newtripcmbss",
            isCustBill:false,
            id:this.id+"createWorkOrderProductDetailsGrid",
            moduleid:Wtf.Acc_Delivery_Order_ModuleId,
            currencyid:this.Currency.getValue(),
            fromOrder:true,
            isOrder:this.isOrder,
            isEdit:this.isEdit,
            copyTrans:false, 
            forceFit:true,
            loadMask : true,
            heplmodeid:'hlpmodeid',
            parentid:this.id
        });
        
        this.Grid.on("datachanged", function(){
            this.applyCurrencySymbol();
        },this);
        
        this.Grid.getStore().on('load',function(store){            
            this.Grid.addBlank(store);
            this.updateFormCurrency();
        }.createDelegate(this),this);
    },
    
    closeOpenWin:function(){
        this.close();
    },
    
    saveData:function(){
        this.workOrderNo.setValue(this.workOrderNo.getValue().trim());
        this.remark.setValue(this.remark.getValue().trim());
        
        if(this.workOrderForm.getForm().isValid()){
            
            for(var i=0;i<this.Grid.getStore().getCount()-1;i++){// excluding last row

                var dquantity=this.Grid.getStore().getAt(i).data['dquantity'];
                if(dquantity == '' || dquantity == undefined || dquantity<=0){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.AQuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.shouldbegreaterthanZero")], 2);
                    return;
                }
                
                var rate=this.Grid.getStore().getAt(i).data['rate'];
                if(rate===""||rate==undefined||rate<0){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.RateforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.cannotbeempty")], 2);
                    return;
                }
            }
            
            
            var count=this.Grid.getStore().getCount();
            if(count<=1){
                WtfComMsgBox(33, 2);
                return;
            }
            
            var rec=this.workOrderForm.getForm().getValues();
            
            this.ajxurl = "ACCInvoice/saveAssetMaintenanceWorkOrder.do";
            
            
            var detail = this.Grid.getProductDetails();
            if(detail == undefined || detail == "[]"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
                return;
            }
            
            var prodLength=this.Grid.getStore().data.items.length;
            
            for(var i=0;i<prodLength-1;i++){ 
                var prodID=this.Grid.getStore().getAt(i).data['productid'];
                var prorec=this.Grid.productComboStore.getAt(this.Grid.productComboStore.find('productid',prodID));
                if (prorec == undefined) {
                    prorec = this.Grid.getStore().getAt(i);
                }
                if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory){ //if company level option is on then only check batch and serial details
                    if(prorec.data.isBatchForProduct || prorec.data.isSerialForProduct){ 
                        if(prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part'){
                            var batchDetail= this.Grid.getStore().getAt(i).data['batchdetails'];
                             var productQty= this.Grid.getStore().getAt(i).data['dquantity'];
                             var baseUOMRateQty= this.Grid.getStore().getAt(i).data['baseuomrate'];
                            if(batchDetail == undefined || batchDetail == "" || batchDetail=="[]"){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);   //Batch and serial no details are not valid.
                                return;
                            }else{
                                     var jsonBatchDetails= eval(batchDetail);
                                     var batchQty=0;
                                     for(var batchCnt=0;batchCnt<jsonBatchDetails.length;batchCnt++){
                                         if(jsonBatchDetails[batchCnt].quantity>0){
                                             batchQty=batchQty+ parseInt(jsonBatchDetails[batchCnt].quantity);
                                         }
                                     }
                                     
                                     if((batchQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) != (productQty*baseUOMRateQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)){
                                         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);
                                         return;
                                     }                       
                        }
                        }
                    }
                }
                var quantity=this.Grid.getStore().getAt(i).data['quantity'];
                if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') { // serial no for only inventory type of product
                    if (prorec.data.isSerialForProduct) {
                        var v = quantity;
                        v = String(v);
                        var ps = v.split('.');
                        var sub = ps[1];
                        if (sub!=undefined && sub.length > 0) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                            return;
                        }
                    }
                }
            
                var dquantity=this.Grid.getStore().getAt(i).data['dquantity'];
                if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') { // serial no for only inventory type of product
                    if (prorec.data.isSerialForProduct) {
                        var v = dquantity;
                        v = String(v);
                        var ps = v.split('.');
                        var sub = ps[1];
                        if (sub!=undefined && sub.length > 0) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                            return;
                        }
                    }
                }
            }
            
            if(this.startDate.getValue()>this.endDate.getValue()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.workorder.save.enddate")], 3);
                return;
            }
            
            
            this.showConfirmAndSave(rec,detail);
            
        }else{
            WtfComMsgBox(2, 2);
        }
        
    },
    
    showConfirmAndSave:function(rec,detail){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.invoice.msg7"),function(btn){
            if(btn!="yes") {return;}
            
            rec.detail=detail;
            rec.number=this.workOrderNo.getValue();
            rec.remark=this.remark.getValue();
            rec.batchDetails=this.Grid.batchDetails;
            rec.assignedTo=this.assignedTo.getValue();
            rec.schedulerId=this.scheduleEventRec.get('scheduleId');
            rec.workOrderId=this.scheduleEventRec.get('workOrderId');
            rec.currencyid=this.Currency.getValue();
            rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
            rec.startDate=WtfGlobal.convertToGenericDate(this.startDate.getValue());
            rec.endDate=WtfGlobal.convertToGenericDate(this.endDate.getValue());
            
            Wtf.Ajax.requestEx({
                    url:this.ajxurl,
                    params: rec                    
                },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
    },
    
    genSuccessResponse:function(response, request){
        WtfComMsgBox([this.titlel,response.msg],response.success*2+1);
        
        if(response.success){
            Wtf.productStoreSales.reload();
            Wtf.productStore.reload();
            
            this.Grid.getStore().removeAll();
            
            this.workOrderForm.getForm().reset();
            
            this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
            
            // Fire event after save
            this.fireEvent('workOrderSaved',this);
            
            this.close();
            
        }
    },
    
    setWorkValue : function(){
        if(this.scheduleEventRec){
            this.assetName.setValue(this.scheduleEventRec.get('assetName'));
            this.assetGroup.setValue(this.scheduleEventRec.get('assetGroupName'));
            this.startDate.setValue(this.scheduleEventRec.get('actualStartDate'));
            this.endDate.setValue(this.scheduleEventRec.get('actualEndDate'));
            
            if(this.scheduleEventRec.get('workJobId') !== null && this.scheduleEventRec.get('workJobId') !== undefined && this.scheduleEventRec.get('workJobId') !== '') {
                this.Grid.productComboStore.on("load", function() {
                    if (this.Grid.getStore().getCount() <= 1) {
                        this.loadEditableGrid();
                    }
                }, this);
                this.Grid.productComboStore.reload();
            }
            
        }
    },
    
    loadRecord:function(){
        this.workOrderNo.setValue(this.scheduleEventRec.get('workJobId'));
        this.billDate.setValue(this.scheduleEventRec.get('billdate'));
        this.assignedTo.setValue(this.scheduleEventRec.get('assignedToId'));//
        this.remark.setValue(this.scheduleEventRec.get('remark'));
        if(this.Grid){
            this.Grid.forCurrency =this.scheduleEventRec.get('currency');
            this.Grid.billDate=this.scheduleEventRec.get('billdate');
        }
},
    
loadEditableGrid:function(){
    
    this.subGridStoreUrl = "ACCInvoiceCMN/getAssetMaintenanceWorkOrderRows.do";
    this.billid=this.scheduleEventRec.get('workOrderId');
    this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;
    this.Grid.getStore().on("load", function(){
        this.loadRecord();
    }, this);
    
    this.Grid.getStore().load({
        params:{
            bills:this.billid
            }
        });
},
 createWorkOrderForm:function(){
        
        Wtf.assignedToStore.load();
        
        this.assignedTo= new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
//            selectOnFocus:true,
            valueField:'id',
            displayField:'name',
            id:"assignedTo"+this.id,
            allowBlank:false,
            store:Wtf.assignedToStore,
//            addNoneRecord: true,
//            anchor: '94%',
            width : 200,
//            typeAhead: true,
            forceSelection: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.Workorder.AssignedTo*"),
            emptyText: 'Select Assigned To...',
//            hideLabel:(this.isOrder && this.quotation),
//            hidden:( this.isOrder && this.quotation),
            name:'assignedTo',
            hiddenName:'assignedTo'            
        });
        
        this.assignedTo.addNewFn=this.addAssignedTo.createDelegate(this);
        
        this.currencyRec = new Wtf.data.Record.create([
            {name: 'currencyid',mapping:'tocurrencyid'},
            {name: 'symbol'},
            {name: 'currencyname',mapping:'tocurrency'},
            {name: 'exchangerate'},
            {name: 'htmlcode'}
         ]);
        
        this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
            url:"ACCCurrency/getCurrencyExchange.do"
        });
        
        this.currencyStore.on('load',function () {
            
            if(this.scheduleEventRec){
                this.Currency.setValue(this.scheduleEventRec.get('currencyid'));//
            }
            if(!this.isEdit){
                this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
            }
        //                this.updateFormCurrency();
        },this);
        
        this.currencyStore.load();
        
        this.Currency = new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.currency.cur"),  // 'Currency',
            hiddenName:'currencyid',
            id:"currency"+this.id,
            width : 200,
            store:this.currencyStore,
            valueField:'currencyid',
            allowBlank : false,
            forceSelection: true,
            displayField:'currencyname',
            scope:this,
            selectOnFocus:true
        });
        
        this.Currency.on('select', function(){
            this.currencychanged = true;
            this.onCurrencyChangeOnly();
            this.updateFormCurrency();
            if(this.Grid){
                this.Grid.forCurrency = this.Currency.getValue();
            }
        }, this);
        
        
        this.workOrderNo = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.Workorder.WorkOrderNumber*"),
//            readOnly:true,
            name:'workOrderNoId',
            disabled:this.isEdit,
            width:200,
            allowBlank:false
        });
        
        this.assetName = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.fixed.asset.id"),
            name:'assetName',
            readOnly:true,
            width:200
        });
        
        this.assetGroup = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.autoAssetGroup"),
            name:'assetGroupName',
            readOnly:true,
            width:200
        });
        
//        this.assignedTo = new Wtf.form.TextField({
//            fieldLabel:'Assigned To',
////            readOnly:true,
//            name:'AssignedToId',
//            width:200
//        });
        
        this.billDate= new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.workorder.workorderdate")+"*",
            id:"workOrderDate"+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'billdate',
//            maxValue:this.isOpeningBalanceOrder?this.getFinancialYRStartDatesMinOne(true):null,
//            anchor:'50%',
            width : 200,
//            listeners:{
//                'change':{
//                    fn:this.updateDueDate,
//                    scope:this
//                }
//            },
            allowBlank:false
        });
        
        this.startDate = new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.assetworkorder.StartDate"),
            id:"startDate"+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'startDate',
//            disabled:true,
            disabledClass:"newtripcmbss",
//            value:Wtf.serverDate,
            width : 200
        });
        
        
        this.startDate.on('change', this.startDateChanged,this);
        
        this.endDate = new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.assetworkorder.EndDate"),
            id:"endDate"+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'endDate',
//            disabled:true,
            disabledClass:"newtripcmbss",
//            value:Wtf.serverDate,
            width : 200
        });
        
        this.endDate.on('change', this.endDateChanged,this);
        
        this.remark=new Wtf.form.TextArea({
            fieldLabel:WtfGlobal.getLocaleText("acc.assetworkorder.Remark"),
            name: 'remark',
            id:"remark"+this.id,
            height:40,
//            anchor:'94%',
            width : 200,
//            allowBlank:false,
            maxLength:2048
        });
        
        
        this.workOrderForm = new Wtf.form.FormPanel({
            border:false,
            autoWidth:true,
            anchor:'100%',
            height:200,
            labelWidth:150,
            bodyStyle:'margin:40px 10px 10px 30px',
            items:[
            {
                layout:'column',
                border:false,
                items:[{
                    columnWidth:'.45',
                    layout:'form',
                    border:false,
                    items:[this.workOrderNo,this.Currency,this.billDate,this.assetName,this.assetGroup,this.assignedTo]
                },{
                    columnWidth:'.45',
                    layout:'form',
                    border:false,
                    items:[this.startDate,this.endDate,this.remark]
                }]
            }
            
            ]
        });
        
    },
    
    onCurrencyChangeOnly:function(){
        this.Grid.getStore().removeAll();
        this.Grid.addBlankRow();
    },
    
    updateFormCurrency:function(){
        this.applyCurrencySymbol();
    },
    
    applyCurrencySymbol:function() {
        var index = this.getCurrencySymbol();
        var rate = this.externalcurrencyrate;
        if(index >= 0){
           rate = (rate == "" ? this.currencyStore.getAt(index).data.exchangerate : rate);
           this.symbol =  this.currencyStore.getAt(index).data.symbol;
           this.Grid.setCurrencyid(this.currencyid,rate,this.symbol,index);
//           this.applyTemplate(this.currencyStore,index);
        }
        return this.symbol;
    },
    
    getCurrencySymbol:function(){
        var index=null;
//        this.currencyStore.clearFilter(true); //ERP-9962
        var FIND = this.Currency.getValue();
        if(FIND == "" || FIND == undefined || FIND == null) {
            FIND = WtfGlobal.getCurrencyID();
        }
        index=this.currencyStore.findBy( function(rec){
             var parentname=rec.data['currencyid'];
            if(parentname == FIND)
                return true;
             else
                return false
            })
       this.currencyid=this.Currency.getValue();
       return index;
    },
    
    addAssignedTo:function(){
        addMasterItemWindow('23')
    },
    
    startDateChanged:function(c,newVal,oldVal){
        var eventStartDate = this.scheduleEventRec.get('startDate');
        var eventEndDate = this.scheduleEventRec.get('endDate');
        
        if(!(newVal.between(eventStartDate, eventEndDate))){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.workorder.save.startdate")], 3);
            this.startDate.setValue(oldVal);
        }
        
    },
    
    endDateChanged:function(c,newVal,oldVal){
        var eventStartDate = this.scheduleEventRec.get('startDate');
        var eventEndDate = this.scheduleEventRec.get('endDate');
        
        if(!(newVal.between(eventStartDate, eventEndDate))){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.workorder.save.enddate")], 3);
            this.endDate.setValue(oldVal);
        }
        
    }
});