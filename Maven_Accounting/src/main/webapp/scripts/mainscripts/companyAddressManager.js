/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

function showAddressWindowForCompany(addAddressFromTransactions){
    //Fetching data from server side while fetching company data
    WtfGlobal.setAjaxTimeOut();
    Wtf.Ajax.requestEx({
        url:"ACCCompanyPref/getCompanyAddressDetails.do",
        params : {
            defaultparam : "param"
        }
    },this,function(response){
        if(response.success){  
            WtfGlobal.resetAjaxTimeOut();
            var record=new Wtf.data.Record({
                addressDetails:response.data                      
            });   
            createAddressWindowForCompany(record,addAddressFromTransactions);
        } else {
            WtfGlobal.resetAjaxTimeOut();
            createAddressWindowForCompany("",addAddressFromTransactions);
        }
    },function(response){
        WtfGlobal.resetAjaxTimeOut();
        createAddressWindowForCompany("",addAddressFromTransactions);
    });
}

function createAddressWindowForCompany(record,addAddressFromTransactions){
    var window = Wtf.getCmp('companyAddressManagerWindow');
    if(!window){
        var config = {
            title:WtfGlobal.getLocaleText("acc.field.manageCompanyAddress"),
            id:'companyAddressManagerWindow',                                   
            iconCls :getButtonIconCls(Wtf.etype.deskera),            
            width:1125,
            height:645,
            resizable:false,
            closable: true,
            layout:'fit',
            renderTo: document.body,
            modal: true,
            record:record,
            constrainHeader :true,
            addAddressFromTransactions:addAddressFromTransactions
        };
        //If Avalara Integration is enabled and address validation is enabled, then we add a button for user to validate addresses
        if (Wtf.account.companyAccountPref.avalaraIntegration && Wtf.account.companyAccountPref.avalaraAddressValidation) {
            config.avalaraAddressValidation = true;//Add Avalara Address Validation flag's value equal to true in config if address validation is on
        }
        new Wtf.account.companyAddressManagerWindow(config).show();        
    } else {
        window.show();
    }
}

Wtf.account.companyAddressManagerWindow=function(config){
    this.record=config.record;
    this.addAddressFromTransactions=config.addAddressFromTransactions;
    this.loadMask = new Wtf.LoadMask(document.body, {
        msg: WtfGlobal.getLocaleText("acc.msgbox.50")//loadMask Message -> Loading...
    });
    Wtf.apply(this,config);
    var buttonArray = new Array();
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
        disabled: this.viewGoodReceipt || this.isViewTemplate,
        scope: this,
        handler: config.avalaraAddressValidation ? this.validateAddressWithAvalara.createDelegate(this, [true]) : this.saveData.createDelegate(this)
    });
    
    buttonArray.push(this.saveButton);
    buttonArray.push(this.closeButton);
    
    if (config.avalaraAddressValidation) {
        this.validateAddressBttn = new Wtf.Toolbar.Button({//Button to validate address with Avalara REST service
            text: WtfGlobal.getLocaleText("acc.common.validateAddresses"),
            tooltip: WtfGlobal.getLocaleText("acc.integration.validateAddressesWithAvalara"),
            scope: this,
            handler: function () {
                this.validateAddressWithAvalara(false);
            },
            iconCls: 'pwnd validate'
        });
        buttonArray.push(this.validateAddressBttn);
    }
    
    Wtf.apply(this,{
        buttons:buttonArray 
    });
    Wtf.account.companyAddressManagerWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.companyAddressManagerWindow, Wtf.Window ,{
    onRender:function(config){
        Wtf.account.companyAddressManagerWindow.superclass.onRender.call(this,config);
        this.addressPanel = new Wtf.account.CusVenAddressDetail({
            id:'companyaddressdetails'+this.id,
            isCompany:true,
            isEdit:false,                    
            isCustomer:false,
            iconCls :getButtonIconCls(Wtf.etype.customer),
            layout: 'fit',
            buttonAlign: 'right',
            record:this.record,
            addAddressFromTransactions:this.addAddressFromTransactions,
            scope:this
        });
        this.add(this.addressPanel);
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
                addressesForValidationWithAvalara: JSON.stringify(this.addressPanel.addressForm.getForm().getValues()),
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
    saveData:function(){
        this.addressPanel.saveForm();
        this.saveButton.disable();
        if (this.validateAddressBttn) {
            this.validateAddressBttn.disable();
        }
    }
});



