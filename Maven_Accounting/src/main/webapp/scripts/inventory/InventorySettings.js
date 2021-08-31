
function getBatchSerialWindow(itemid, itemname, storeid, locationid, quantity, decrease){
    return new Wtf.account.SerialNoWindow({
        renderTo: document.body,
        title:WtfGlobal.getLocaleText("acc.field.SelectWarehouseBatchSerialNumber"),
        productName:itemname,
        quantity:quantity,
        //            billid:obj.data.billid,
        //            defaultLocation:locationid,
        productid:itemid,
        isSales:decrease,
        //            moduleid:this.moduleid,
        //            transactionid:(this.isCustomer)?4:5,
        //            isDO:this.isCustomer?true:false,
        //            defaultWarehouse:storeid,
        //            batchDetails:obj.data.batchdetails,
        //            warrantyperiod:prorec.data.warrantyperiod,
        //            warrantyperiodsal:prorec.data.warrantyperiodsal, 
        //            isLocationForProduct:true,
        //            isWarehouseForProduct:true,
        isBatchForProduct:true,
        isSerialForProduct:true,
        //            linkflag:obj.data.linkflag,
        isEdit:true,
        //            copyTrans:this.copyTrans,
        width:950,
        height:400,
        resizable : false,
        modal : true
    });
}
/*****  Company Administration   *****/
function openInventorySettingsTab(){
    var main = Wtf.getCmp("masterdatagrid");
    var CompanySettingsTab = Wtf.getCmp("inventorysettingsTabId");
    main.setActiveTab(CompanySettingsTab);
    Wtf.getCmp("masterdatagrid").doLayout();
//openDefaultFormatSetupWin();
}





Wtf.Company = function(config){
    Wtf.apply(this, config);
    Wtf.Company.superclass.constructor.call(this,config);
};

Wtf.extend(Wtf.Company,Wtf.Panel,{
    onRender: function(config) {
        Wtf.Company.superclass.onRender.call(this, config);
     
        var seperator = {
            border: false,
            html: '<hr style = "width: 75%;margin-left: 10px">'
        };
        var blankseperator = {
            border: false,
            html: ''
        };

       
        var defConf = {
            ctCls: 'fieldContainerClass',
            labelStyle: 'font-size: margin-left: -3px text-align: right;'
        };
        
        var stockReqFieldSet = {
            xtype : 'fieldset',
            cls: "companyFieldSet",                        
            title: 'Stock Request',
            defaults : defConf,
            autoHeight: true,
            items:[{
                xtype:'checkbox',    
                id: 'svenabledcheckbox',
                labelSeparator:"",
                labelWidth:0,                            
                style:'margin-right: 3px;',
                name: 'svenabledcheckbox',
                boxLabel: 'Stock Request Approval Flow. ',
                width:400
            }]
        };
        var stockAdjFieldSet = {
            xtype : 'fieldset',
            cls: "companyFieldSet",                        
            title: 'Stock Adjustment',
            // hidden:true,
            defaults : defConf,
            autoHeight: true,
            items:[{
                xtype:'checkbox',    
                id: 'stockoutcheckbox',
                labelSeparator:"",
                labelWidth:0,                            
                style:'margin-right: 3px;',
                name: 'stockoutcheckbox',
                disabled : (Wtf.account.companyAccountPref.activateQAApprovalFlow) ? false : true,
                boxLabel: 'Stock Adjustment QA Approval Flow',
                width:400
            }]
        }
        var stockOutFieldSet = {
            xtype : 'fieldset',
            //            cls: "companyFieldSet",
            style:'margin-left: 8px;',
            title: 'Stock Adjustment',
            defaults : defConf,
            autoHeight: true,
            items:[{
                xtype:'checkbox',    
                id: 'stockoutapprovalchkbox',
                fieldLabel:'Stock IN Approval Flow',
                //                labelSeparator:"",
                labelStyle:'width:300px;',                            
                style:'margin-left:-120px;',
                disabled : (Wtf.account.companyAccountPref.activateQAApprovalFlow) ? false : true,
                name: 'stockoutApproval',
                //                boxLabel: 'Stock Out Approval Flow. ',
                width:400
            }]
        };
        var interStoreFieldSet = {
            xtype : 'fieldset',
            //            cls: "companyFieldSet", 
            style:'margin-left: 8px;',
            title: 'Inter Store Transfer',
            // hidden:true,
            defaults : defConf,
            autoHeight: true,
            items:[{
                xtype:'checkbox',    
                id: 'interstoreapprovalchkbox',
                fieldLabel:'Inter Store Stock Return Approval Flow',
                labelStyle:'width:300px;',                          
                style:'margin-left:-120px;',
                disabled : (Wtf.account.companyAccountPref.activateQAApprovalFlow) ? false : true,
                name: 'interstoreApproval',
                //                boxLabel: 'Inter Store Stock Return Approval Flow',
                width:400
            }]
        }
        var stockRequestFieldSet = {
            xtype : 'fieldset',
            //            cls: "companyFieldSet",      
            style:'margin-left: 8px;',
            title: 'Stock Request',
            // hidden:true,
            defaults : defConf,
            autoHeight: true,
            items:[{
                xtype:'checkbox',    
                id: 'stockrequestapprovalchkbox',
                fieldLabel:'Stock Request Return Approval Flow',
                labelStyle:'width:300px;',                         
                style:'margin-left:-120px;',
                disabled : (Wtf.account.companyAccountPref.activateQAApprovalFlow) ? false : true,
                name: 'stockrequestApproval',
                //                boxLabel: 'Stock Request Return Approval Flow',
                width:400
            }]
        }
        var negativeCheckFieldSet = {
            xtype : 'fieldset',
            cls: "companyFieldSet",
            defaultType: 'textfield',
            title: 'Negative Inventory',
            defaults : defConf,
            autoHeight: true,
            items:[
            {
                xtype:'radio',
                boxLabel:'Allow',
                id: "checkInventory0",
                name:'checkInventory',
                labelSeparator:'',
                labelWidth:0,
                value:0
            },{
                xtype:'radio',
                boxLabel:'Warn',
                id: "checkInventory1",
                name:'checkInventory',
                labelSeparator:'',
                labelWidth:0,
                value:1
            },{
                xtype:'radio',
                boxLabel:'Block',
                id: "checkInventory2",
                name:'checkInventory',
                labelSeparator:'',
                labelWidth:0,
                value:2
            }                              
            ]  
        };
        
        var operationTypeFieldSet = {
            xtype : 'fieldset',
            cls: "companyFieldSet",
            defaultType: 'textfield',
            title: 'Inventory Update Types',
            defaults : defConf,
            autoHeight: true,
            items:[
            {
                xtype:'radio',
                boxLabel:'LIFO',
                id: "iluType0",
                name:'iluType',
                labelSeparator:'',
                labelWidth:0,
                value:0
            },{
                xtype:'radio',
                boxLabel:'FIFO',
                id: "iluType1",
                name:'iluType',
                labelSeparator:'',
                labelWidth:0,
                value:1
            }
            ,{
                xtype:'radio',
                boxLabel:'Average Weighted',
                id: "iluType2",
                name:'iluType',
                //                                hidden:true,
                labelSeparator:'',
                labelWidth:0,
                value:2
            }                              
            ]  
        }
        var leftItemArray = [];
        //        leftItemArray.push(stockReqFieldSet);
        //        leftItemArray.push(seperator, stockAdjFieldSet);
        leftItemArray.push(stockOutFieldSet);
        leftItemArray.push(interStoreFieldSet);
        leftItemArray.push(stockRequestFieldSet);
        
        this.companyDetailsPanel = new Wtf.form.FormPanel({
            id: 'companyDetailsForm',
            url:"INVConfig/addOrUpdateConfig.do",
            //fileUpload: true,
            cls: 'adminFormPanel',
            autoScroll: true,
            border: false,
            items: [{
                layout: 'column',
                border: false,
                items:[{
                    columnWidth: 0.49,
                    border: false,
                    items: leftItemArray
                }]
            }]
        });
       
        var detailPanel = new Wtf.Panel({
            layout: "border",
            border: false,
            bodyStyle: "background-color: #ffffff;",
            bbar:[{
                text: "Save",
                scope: this,
                tooltip : {
                    text:'Click to Update Company Settings'
                },
                handler: this.updateCompany,
                iconCls: getButtonIconCls(Wtf.etype.save)
            }
            //            
            ],
            items: [{
                border: false,
                region: 'center',
                autoScroll: true,
                items: [ this.companyDetailsPanel ]
            }]
        })
        this.add(detailPanel);
        Wtf.Ajax.requestEx({
            url: "INVConfig/getConfig.do",
            params:{
                // flag:13,
                cid: companyid
            }
        },
        this,
        function(request, response){
            //var res = eval('(' + request+ ')');
            if(request && request.data){
                this.doLayout();
                this.fillData(request.data);
                var hdate = Wtf.getCmp("holidayDateField");
                if(hdate)
                    hdate.destroy();
            } else {
                WtfComMsgBox(17, 1);
                this.companyDetailsPanel.disable();
            }
            bHasChanged=true;
        },
        function(){
            bHasChanged=false;
            WtfComMsgBox(17, 1);
            this.companyDetailsPanel.disable();
        }
        );
        
    },
    checkNegativeBlock : function(){
        var inventory = (Wtf.getCmp('checkInventory0').getValue() == true?0:Wtf.getCmp('checkInventory1').getValue() == true?1:2);
        if(inventory != 2){
            Wtf.MessageBox.show({
                title: 'Alert',
                msg: 'Please change Negative inventory option to BLOCK first to proceed.',
                buttons: Wtf.MessageBox.OK,
                animEl: 'ok',
                icon: Wtf.MessageBox.INFO
            });
            return false;
        }
        return true;
    },
    blockNegative : function(){
        var inventory = this.getNegativeInventoryValue();
        if(inventory != 2){
            Wtf.getCmp('checkInventory2').setValue(true);
        }
    },
    uncheckIluType : function(){
        for(var i=1 ; i<=3 ; i++){
            Wtf.getCmp('iluType'+i).setValue(false);
        }
    },

    getIlutypeValue : function(){
        return (Wtf.getCmp('iluType0').getValue() == true?"LIFO":Wtf.getCmp('iluType1').getValue() == true?"FIFO":Wtf.getCmp('iluType2').getValue()== true?"AVERAGE_WEIGHTED":3);
    },
    getNegativeInventoryValue : function(){
        return (Wtf.getCmp('checkInventory0').getValue() == true?"ALLOW":Wtf.getCmp('checkInventory1').getValue() == true?"WARN":"BLOCK");
    },
    updateCompany: function(){
 
        //        var iluType = this.getIlutypeValue();
            
        //        var inventory = this.getNegativeInventoryValue();
        this.loadUpdateCompanyMask = new Wtf.LoadMask(document.body,{
            msg:"Updating company settings..."
        });
        this.loadUpdateCompanyMask.show();
        this.companyDetailsPanel.form.submit({
            scope: this,
            params:{
            //                checkInv:inventory,
            //                iluTypeCode:iluType
            },
            success: function(result,action){
                this.loadUpdateCompanyMask.hide();
               
                //if(result.msg==null)
                // WtfComMsgBox(18, 0);
                //else
                WtfComMsgBox(["Inventory Configuration",action.result.data.msg],0);
            //                Wtf.iLevel = inventory;
            //trackStoreLocation = Wtf.getCmp("tracklocation").getValue();
            window.location.reload();
            },
            failure: function(frm, action){
                this.loadUpdateCompanyMask.hide();
                if(action.failureType == "client")
                    WtfComMsgBox(19, 1);
                else{
                    var resObj = eval( "(" + action.response.responseText + ")" );
                    WtfComMsgBox(20, 1);
                }
            }
        });
    // }
    },

    resetAll: function(){
        alert("reserAll");
    },

    fillData: function(resObj){
        // var imagePath = (resObj.image!="")?resObj.image:'images/deskeralogo.png';
        //  Wtf.get('domainField').dom.parentNode.innerHTML+="<span style='color:gray !important;font-size:11px !important;'>.deskera.com</span><br><span style='color:gray !important;font-size:10px !important;font-style:italic !important;'>Letters and numbers only — no spaces.<span>";
        //        Wtf.getCmp("checkInventory" + resObj.inventoryCheck).setValue(true);
        //        Wtf.getCmp("iluType" + ( resObj.stockBatchType)).setValue(true);
        //        Wtf.getCmp("svenabledcheckbox").setValue(resObj.enableStockRequestApprovalFlow);
        Wtf.getCmp("stockoutapprovalchkbox").setValue(resObj.enableStockOutApprovalFlow);
        Wtf.getCmp("interstoreapprovalchkbox").setValue(resObj.enableInterStoreApprovalFlow);
        Wtf.getCmp("stockrequestapprovalchkbox").setValue(resObj.enableStockRequestReturnApprovalFlow);
        //        Wtf.getCmp("stockoutcheckbox").setValue(resObj.enableStockAdjustmentApprovalFlow);
        //    Wtf.getCmp("compgst").setValue(resObj.gst);
        //  this.setConversionRate();
        document.getElementById('displaycompanylogo').src = "images/store/?company=true&"+Math.random();
    }
//    
});
/*****  Company Administration End  *****/




