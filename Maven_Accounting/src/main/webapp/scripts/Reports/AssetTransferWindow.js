/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

function callAssetTransferWindow(record) {
    var panel = Wtf.getCmp("assetTransferWindowId");
    if (panel == null) {
        panel = new Wtf.account.AssetTransferWindow({
            title: WtfGlobal.getLocaleText("acc.fixedAsset.transfer.title"),
            id: "assetTransferWindowId",
            iconCls: 'accountingbase invoicelist',
            layout: 'fit',
            closable: true,
            border: false,
            modal:true,
            height:500,
            width:800,
            record: record
        });
    }
    panel.show();
}

Wtf.account.AssetTransferWindow = function(config) {

    this.record = config.record;
    Wtf.apply(this,{
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),  // 'Save',
            scope: this,
            handler:this.saveForm.createDelegate(this)
        }, {
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  // 'Cancel',
            scope: this,
            handler:this.closeForm.createDelegate(this)
        }]
    },config);
    
    Wtf.account.AssetTransferWindow.superclass.constructor.call(this, config);
    
    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.AssetTransferWindow, Wtf.Window, {

    
    
    onRender : function(config) {
        Wtf.account.AssetTransferWindow.superclass.onRender.call(this, config);
        
        this.createFields();
        this.createForm();
        
        this.add(this.centerPanel = new Wtf.Panel({
            region: 'center',
            border: false,
            autoScroll:true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls:'bckgroundcolor',
            layout: 'border',
            items:[this.mainForm]
        }));
        
        this.loadRecord();
    },
    
    loadRecord : function() {
        if(this.record.data.warehouse){
            this.oldWarehouse.setValue(this.record.data.warehouse);
        }
        if(this.record.data.location){
            this.oldLocation.setValue(this.record.data.location);
        }
    },
        
    createFields : function() {
        
        this.warehouseStoreRec = new Wtf.data.Record.create([//  warehouse record
        {
            name: 'id'
        },

        {
            name: 'name'
        },

        {
            name: 'parentid'
        },

        {
            name: 'company'
        },

        {
            name: 'parentname'
        },

        {
            name: 'warehouse'
        }
        ]);
        this.reqtestWareHouseStore = new Wtf.data.Store({  //  warehouse store for combo box
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.warehouseStoreRec),
            url:"ACCMaster/getWarehouseItems.do",
            baseParams:{
        }
        });
        
        this.locationRec = new Wtf.data.Record.create([
        {
            name:"id"
        },
        {
            name:"name"
        },
        {
            name: 'parentid'
        },
        {
            name: 'parentname'
        }
        ]);
        this.locationReader = new Wtf.data.KwlJsonReader({
            root:"data"
        },this.locationRec);
        var locationStoreUrl="ACCMaster/getLocationItemsFromStore.do";
        this.locationStore = new Wtf.data.Store({
            url:locationStoreUrl,
            reader:this.locationReader
        });
        if(this.record.data.isWarehouseApplicableToAsset){
            this.reqtestWareHouseStore.load();
        } else {
            this.locationStore.load();
        }
        this.oldData = new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.fixedAsset.transfer.currentDetails"),
            id:this.id+'currentDetails',
            autoHeight : true,
            width: 300,
            items:[
            this.oldWarehouse = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"),
                name: 'oldwarehouse',
                hiddenName: 'oldwarehouse',                
                readOnly:true
            }),
            
            this.oldLocation = new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.contractActivityPanel.Location"), 
                name: 'oldlocation',
                hiddenName: 'oldlocation',
                readOnly:true
            }),
//            this.oldCustomData = new Wtf.form.FieldSet({
//                title:WtfGlobal.getLocaleText("acc.field.OtherFields"),
//                id:this.id+'oldOtherDeatils',
//                autoHeight : true,
//                items:[]
//            })
            ]
        });
        this.newData = new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.fixedAsset.transfer.newDetails"),
            id:this.id+'newDeatils',
            autoHeight : true,
            width: 300,
            items:[
            this.newWarehouse = new Wtf.form.ComboBox({
                store: this.reqtestWareHouseStore,
                fieldLabel: WtfGlobal.getLocaleText("acc.inventorysetup.warehouse"), 
                name: 'newwarehouse',
                hiddenName: 'newwarehouse',
                displayField: 'name',
                disabled:!this.record.data.isWarehouseApplicableToAsset,
                valueField: 'id',
                mode: 'local',
                typeAhead: true,
                triggerAction: 'all',
                forceSelection: true,
                allowBlank: false,
                width : 150
            }),
            this.newLocation = new Wtf.form.ComboBox({
                store: this.locationStore,
                fieldLabel: WtfGlobal.getLocaleText("acc.contractActivityPanel.Location"), 
                name: 'newlocation',
                hiddenName: 'newlocation',
                displayField: 'name',
                disabled:!this.record.data.isLocationApplicableToAsset,
                valueField: 'id',
                mode: 'local',
                typeAhead: true,
                triggerAction: 'all',
                forceSelection: true,
                allowBlank: false,
                width : 150
            }),
//            this.newCustomData = new Wtf.form.FieldSet({
//                title:WtfGlobal.getLocaleText("acc.field.OtherFields"),
//                id:this.id+'newOtherDeatils',
//                autoHeight : true,
//                items:[]
//            })
            ]
        });
        this.newWarehouse.on('change',this.onWareHouseChange,this);
    },
    
    
    createForm : function() {
        this.mainForm = new Wtf.form.FormPanel({
            waitMsgTarget: true,
            method: 'POST',
            border: false,
            region: 'center',
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding:20px;",
            layout:'column',
            autoScroll:true,
            items: [
            {
                layout: 'form',
                border:false,
                columnWidth: 0.5,
                items: [this.oldData]
            },
            {
                layout: 'form',
                border:false,
                columnWidth: 0.5,
                items: [this.newData]
            }
            ]
        });
    },
    
    
    saveForm : function() {
        var isValidForm = this.mainForm.getForm().isValid();
        if(!isValidForm){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.msgbox.2")], 2);
            return;
        }
        this.createJsonDataAndSave();
    },
    
    saveFormData : function() {
        if(this.isView) {
            var rec = this.bankDetailsForm.getForm().getValues();
            rec.ibgbank = (this.radioIBGbank.getValue())? 1 : 2; // '1' for 'Development Bank Of Singapore'
            rec.bankCode = this.bankCode.getValue();
            rec.branchCode = this.branchCode.getValue();
            rec.accountNumber = this.accountNumber.getValue();
            rec.accountName = this.accountName.getValue();
            rec.sendersCompanyID = this.sendersCompanyID.getValue();
            rec.bankDailyLimit = this.bankDailyLimit.getValue();
            rec.ibgbankdetailid = this.record.data.ibgbankdetailid;
            rec.cimbbankdetailid = this.record.data.cimbbankdetailid;
            rec.AccountId = this.record.data.accid;
            
            Wtf.Ajax.requestEx({
                url:this.radioIBGbank.getValue()?"ACCAccount/saveIBGBankDetails.do":"ACCAccount/saveCIMBBankDetails.do",
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
            
        } else {
            this.ibgBankDetail = this.bankDetailsForm.getForm().getValues();
            this.ibgBankDetail.ibgbank = (this.radioIBGbank.getValue())? 1 : 0; // '1' for 'Development Bank Of Singapore'
            this.ibgBankDetail.ibgbanktype = (this.radioIBGbank.getValue())? 1 :(this.radioCIMBGbank.getValue()?2:0); // '1' for 'Development Bank Of Singapore'
            this.ibgBankDetail.settlementMode = this.settlementMode.getValue();
            this.ibgBankDetail.postingIndicator = this.postingIndicator.getValue();
            this.ibgBankDetail.cimbbankdetailid = this.cimbbankdetailid;
            this.ibgBankDetail.ibgbankdetailid = this.ibgbankdetailid
            if(this.radioIBGbank.getValue()){
                this.ibgbanktype = 1;
                this.cimbbankdetailid = "";
            } else {
                this.ibgbanktype = 2;
                this.ibgbankdetailid = ""
            }
            this.fireEvent("update",this);
            this.close();
        }
    },
    
    genSuccessResponse : function(response) {
        if(response.success){
           this.fireEvent("update",this);
           this.close();
           WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.msg?response.msg:''],response.success*2+1);
        }
    },
    
    genFailureResponse : function(response) {
        var msg = WtfGlobal.getLocaleText("acc.field.FailedtomakeconnectionwithWebServer");
        if(response.msg) {
            msg=response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    },
    closeForm : function() {
        this.close();
    },
    onWareHouseChange:function(){
        this.newLocation.clearValue();
        this.locationStore.load({
                params:{
                    storeid:this.newWarehouse.getValue()
                }
        });
    },
    createJsonDataAndSave:function(){
        var rec={};
        var data={};
        var finalJson = {};
        var oldData={};
        var newData={};
        
        oldData['warehouse']=this.record.data.warehouseId;
        oldData['warehousename']=this.oldWarehouse.getValue();
        oldData['location']=this.record.data.locationId;
        oldData['locationname']=this.oldLocation.getValue();
//        oldData['otherdetails']={};
        
        newData['warehouse']=this.newWarehouse.getValue();
        newData['warehousename']=this.newWarehouse.getRawValue();
        newData['location']=this.newLocation.getValue();
        newData['locationname']=this.newLocation.getRawValue();
//        newData['otherdetails']={};
        
        finalJson['old'] = oldData;
        finalJson['new'] = newData;
        finalJson['assetId'] = this.record.data.assetId;
        finalJson['id'] = this.record.data.assetdetailId;
        
        rec['data'] = JSON.stringify(finalJson);
        Wtf.Ajax.requestEx({
                url:"ACCProductCMN/transferAsset.do",
                params: rec
       },this,this.genSuccessResponse,this.genFailureResponse);
    }
});