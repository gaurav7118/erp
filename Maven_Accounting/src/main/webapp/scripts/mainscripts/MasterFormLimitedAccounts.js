/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

Wtf.account.MasterFormAccountWindow  = Wtf.extend(Wtf.Window, {
    height: 100,
    width: 300,
    modal: true,
    iconCls : 'pwnd deskeralogoposition',
    title: WtfGlobal.getLocaleText("acc.field.LimitedAccounts"),
    initComponent : function(){
        this.butnArr = new Array();
        this.butnArr.push({
            text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
            scope: this,
            handler: function() {
                this.submitSelectedRecords();
            }
        },{
            text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
            scope: this,
            handler: function() {
                this.close();
            }
        });
        
        this.moduleStore = new Wtf.data.JsonStore({
            fields: ["moduleId", "moduleName"],
            data: [{
                moduleId: "customer",
                moduleName: "Customer"
            }, {
                moduleId: "vendor",
                moduleName: "Vendor"
            }, {
                moduleId: "product",
                moduleName: "Product"
            }]
        });
        
        this.moduleCombo = new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.designerTemplate.ModuleName")+"*",
            emptyText:WtfGlobal.getLocaleText("acc.module.selectmodule"),
            name:'moduleId',
            hiddenName:'moduleId',
            store:this.moduleStore,
            valueField:'moduleId',
            displayField:'moduleName',
            mode: 'local',
            allowBlank:false,
            triggerAction:'all',
            width:230,
            forceSelection:true,
            listeners:{
                scope: this,
                "change": function(obj, newVal, oldVal){
                    this.showAllCheckbox.setValue(false);
                    this.fieldCombo.reset();
                    this.accountCombo.reset();
                    this.accountCombo.clearValue();
                    var data = "";
                    if(newVal == "customer"){
                        data = [{fieldId: "customerAccounts", fieldName: "Accounts"}];
                    } else if(newVal == "vendor"){
                        data = [{fieldId: "vendorAccounts", fieldName: "Accounts"}];
                    } else if(newVal == "product"){
                        data = [{fieldId: "productPurchaseAccounts", fieldName: "Purchase/Purchase Return Accounts"}, {fieldId: "productSalesAccounts", fieldName: "Sales/Sales Return Accounts"}];
                    }
                    this.fieldStore.loadData(data, false);
                }
            }
        });
        
        this.fieldStore = new Wtf.data.JsonStore({
            fields: ["fieldId", "fieldName"],
            data: []
        });
        
//        this.masterFormStore = new Wtf.data.JsonStore({
//            fields: ["masterFormId", "masterFormName"],
//            data: [{
//                masterFormId: Wtf.MasterFormAccount.CustomerAccounts,
//                masterFormName: WtfGlobal.getLocaleText("acc.field.MasterFormAccount.customerAccounts")
//            }, {
//                masterFormId: Wtf.MasterFormAccount.VendorAccounts,
//                masterFormName: WtfGlobal.getLocaleText("acc.field.MasterFormAccount.vendorAccounts")
//            }, {
//                masterFormId: Wtf.MasterFormAccount.ProductPurchaseAccounts,
//                masterFormName: WtfGlobal.getLocaleText("acc.field.MasterFormAccount.productPurchaseAccounts")
//            }, {
//                masterFormId: Wtf.MasterFormAccount.ProductSalesAccounts,
//                masterFormName: WtfGlobal.getLocaleText("acc.field.MasterFormAccount.productSalesAccounts")
//            }]
//        });
        
        this.fieldCombo = new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.FieldName*"),
            emptyText:WtfGlobal.getLocaleText("acc.selectfield.combo"),
            name:'fieldId',
            hiddenName:'fieldId',
            store:this.fieldStore,
            valueField:'fieldId',
            displayField:'fieldName',
            mode: 'local',
            allowBlank:false,
            triggerAction:'all',
            width:230,
            forceSelection:true,
            typeAhead: true,
            listeners: {
                scope: this,
                "change": function(obj, oldVal, newVal){
                    if(oldVal != newVal){
                        this.accountCombo.reset();
                        this.accountCombo.clearValue();
                        this.accStore.load();
                        /**
                         * set activation flag of mapped accounts
                         */
                        this.showAllCheckbox.setValue(false);
                        var disableAccountsCombo = false;
                        if(Wtf.account.companyAccountPref.columnPref != undefined && Wtf.account.companyAccountPref.columnPref != null){
                            if(this.fieldCombo.getValue() == Wtf.MasterFormAccount.CustomerAccounts){
                                if(Wtf.account.companyAccountPref.columnPref.isLimitedCustomerAccounts != undefined && Wtf.account.companyAccountPref.columnPref.isLimitedCustomerAccounts != null){
                                    this.showAllCheckbox.setValue(Wtf.account.companyAccountPref.columnPref.isLimitedCustomerAccounts);
                                    disableAccountsCombo = Wtf.account.companyAccountPref.columnPref.isLimitedCustomerAccounts;
                                }
                            } else if(this.fieldCombo.getValue() == Wtf.MasterFormAccount.VendorAccounts){
                                if(Wtf.account.companyAccountPref.columnPref.isLimitedVendorAccounts != undefined && Wtf.account.companyAccountPref.columnPref.isLimitedVendorAccounts != null){
                                    this.showAllCheckbox.setValue(Wtf.account.companyAccountPref.columnPref.isLimitedVendorAccounts);
                                    disableAccountsCombo = Wtf.account.companyAccountPref.columnPref.isLimitedVendorAccounts;
                                }
                            } else if(this.fieldCombo.getValue() == Wtf.MasterFormAccount.ProductSalesAccounts){
                                if(Wtf.account.companyAccountPref.columnPref.isLimitedProductSalesAccounts != undefined && Wtf.account.companyAccountPref.columnPref.isLimitedProductSalesAccounts != null){
                                    this.showAllCheckbox.setValue(Wtf.account.companyAccountPref.columnPref.isLimitedProductSalesAccounts);
                                    disableAccountsCombo = Wtf.account.companyAccountPref.columnPref.isLimitedProductSalesAccounts;
                                }
                            } else if(this.fieldCombo.getValue() == Wtf.MasterFormAccount.ProductPurchaseAccounts){
                                if(Wtf.account.companyAccountPref.columnPref.isLimitedProductPurchaseAccounts != undefined && Wtf.account.companyAccountPref.columnPref.isLimitedProductPurchaseAccounts != null){
                                    this.showAllCheckbox.setValue(Wtf.account.companyAccountPref.columnPref.isLimitedProductPurchaseAccounts);
                                    disableAccountsCombo = Wtf.account.companyAccountPref.columnPref.isLimitedProductPurchaseAccounts;
                                }
                            }
                        }
                        this.accountCombo.setDisabled(disableAccountsCombo);
                    }
//                    this.cmbAccount.clearValue();
//                    this.accStore.load({
//                        params: {start:0, limit:Wtf.CustomerCombopageSize}
//                    });
                }
            }
        });
        
        this.accRec = new Wtf.data.Record.create([
            {name: 'accountid',mapping:'accid'},
            {name: 'acccode'},
            {name: 'accname'},
            {name: 'groupname'},
            {name: 'groupid'},
            {name: 'level'},
            {name: 'usedIn'}
        ]);
        
        this.accStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            },this.accRec),
            url : "ACCAccountCMN/getAccountsForCombo.do"
        });
        
        this.accStore.on("load", function(s, o){
//            this.accountCombo.setDisabled(false);
            Wtf.Ajax.requestEx({
                url: "ACCAccountCMN/getLimitedAccountsOfMasterForm.do",
                params: {
                        companyid: companyid,
                        fieldId: this.fieldCombo.getValue()
                    }
                }, this, function(response) {
                    if (response.success === true && response.totalCount > 0) {
                        this.accountCombo.setValue(response.accounts);
//                        this.cmbAccount.setValue(response.accounts);
                    }
                }, function(response) {
            });
        }, this);
        this.accStore.on("beforeLoad", function(s, o){
            var masterForm = this.fieldCombo.getValue();
            if(masterForm === Wtf.MasterFormAccount.CustomerAccounts){
                this.accStore.baseParams = {
                    mode:2,
                    nature:Wtf.account.nature.Asset,
                    ignoreCashAccounts:true,
                    ignoreBankAccounts:true,
                    ignoreGSTAccounts:true,
                    ignorecustomers:true,
                    ignorevendors:true,
                    ignore:true,
                    nondeleted:true
                };
            } else if(masterForm === Wtf.MasterFormAccount.VendorAccounts){
                this.accStore.baseParams = {
                    mode:2,
                    ignoreCashAccounts:true,
                    ignoreBankAccounts:true,
                    ignoreGSTAccounts:true,
                    ignorecustomers:true,
                    ignorevendors:true,
                    common:1,
                    nondeleted:true
                };
            } else if(masterForm === Wtf.MasterFormAccount.ProductPurchaseAccounts){
                this.accStore.baseParams = {
                    group:6,
                    mode:2,
                    ignorecustomers:true,
                    ignorevendors:true,
                    nondeleted:true,
                    controlAccounts:true
                };
            } else if(masterForm === Wtf.MasterFormAccount.ProductSalesAccounts){
                this.accStore.baseParams = {
                    mode:2,
                    ignoreCashAccounts:true,
                    ignoreBankAccounts:true,
                    ignoreGSTAccounts:true,
                    ignorecustomers:true,
                    ignorevendors:true,
                    common:1,
                    nondeleted:true
                };
            }
            
        }, this);
        
        this.accountCombo = new Wtf.common.Select({
            triggerAction: 'all',
            multiSelect: true,
            mode: 'local',
            valueField: 'accountid',
            displayField: 'accname',
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ?['acccode','groupname']:[],
            store: this.accStore,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.LimitedAccounts.accounts"),
            emptyText: WtfGlobal.getLocaleText("acc.1099.selAcc"),
            name: 'accountid',
            width:230,
            hiddenName: 'accountid',
            listeners: {
                scope: this,
                "unselect": function(oldVal, newVal, index){
                    if(newVal.data.usedIn != undefined && newVal.data.usedIn != ""){
                        if(this.fieldCombo.getValue() == Wtf.MasterFormAccount.CustomerAccounts && newVal.data.usedIn.indexOf("Customer Default Account") != -1){
                            this.accountCombo.select(index, true);
                            this.accountCombo.addValue(newVal.data.accountid, true);
                            this.accountCombo.collapse();
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.MasterFormAccount.usedInAlert")+'"'+this.fieldCombo.lastSelectionText+'"'],2);
                        } else if(this.fieldCombo.getValue() == Wtf.MasterFormAccount.VendorAccounts && newVal.data.usedIn.indexOf("Vendor Default Account") != -1){
                            this.accountCombo.select(index, true);
                            this.accountCombo.addValue(newVal.data.accountid, true);
                            this.accountCombo.collapse();
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.MasterFormAccount.usedInAlert")+'"'+this.fieldCombo.lastSelectionText+'"'],2);
                        } else if(this.fieldCombo.getValue() == Wtf.MasterFormAccount.ProductPurchaseAccounts && (newVal.data.usedIn.indexOf("Product Purchase Account") != -1 || Wtf.MasterFormAccount.ProductPurchaseAccounts && newVal.data.usedIn.indexOf("Product Purchase Return Account") != -1)){
                            this.accountCombo.select(index, true);
                            this.accountCombo.addValue(newVal.data.accountid, true);
                            this.accountCombo.collapse();
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.MasterFormAccount.usedInAlert")+'"'+this.fieldCombo.lastSelectionText+'"'],2);
                        } else if(this.fieldCombo.getValue() == Wtf.MasterFormAccount.ProductSalesAccounts && (newVal.data.usedIn.indexOf("Product Sales Account") != -1 || Wtf.MasterFormAccount.ProductSalesAccounts && newVal.data.usedIn.indexOf("Product Sales Return Account") != -1)){
                            this.accountCombo.select(index, true);
                            this.accountCombo.addValue(newVal.data.accountid, true);
                            this.accountCombo.collapse();
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.MasterFormAccount.usedInAlert")+'"'+this.fieldCombo.lastSelectionText+'"'],2);
                        }
                    }
                },
                "beforeclearval": function(a, b, c){
                    var values = a.valueArray;
                    var usedInValues = "";
                    for(var ind = 0; ind < values.length; ind++){
                        var rec = WtfGlobal.searchRecord(a.store, values[ind], "accountid");
                        if(rec != null && rec.data.usedIn != undefined && rec.data.usedIn != ""){
                            if(this.fieldCombo.getValue() == Wtf.MasterFormAccount.CustomerAccounts && rec.data.usedIn.indexOf("Customer Default Account") != -1){
                                usedInValues += rec.data.accname + ", ";
                            } else if(this.fieldCombo.getValue() == Wtf.MasterFormAccount.VendorAccounts && rec.data.usedIn.indexOf("Vendor Default Account") != -1){
                                usedInValues += rec.data.accname + ", ";
                            } else if(this.fieldCombo.getValue() == Wtf.MasterFormAccount.ProductPurchaseAccounts && (rec.data.usedIn.indexOf("Product Purchase Account") != -1 || Wtf.MasterFormAccount.ProductPurchaseAccounts && rec.data.usedIn.indexOf("Product Purchase Return Account") != -1)){
                                usedInValues += rec.data.accname + ", ";
                            } else if(this.fieldCombo.getValue() == Wtf.MasterFormAccount.ProductSalesAccounts && (rec.data.usedIn.indexOf("Product Sales Account") != -1 || Wtf.MasterFormAccount.ProductSalesAccounts && rec.data.usedIn.indexOf("Product Sales Return Account") != -1)){
                                usedInValues += rec.data.accname + ", ";
                            }
                        }
                    }
                    if(usedInValues != ""){
                        usedInValues = usedInValues.substr(0, usedInValues.length-2);
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), usedInValues + ' - ' +WtfGlobal.getLocaleText("acc.field.MasterFormAccount.usedInAlert")+ '"' +this.fieldCombo.lastSelectionText+'".'],2);
                        return false;
                    }
                }
            }
        });
        /**
         * Show All flag for mapped accounts to be shown in master form or not
         */
        this.showAllCheckbox=new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.MasterFormAccount.showAll"), //Show All
            name:'showAllFlag',
            checked:false,
            listeners: {
                scope: this,
                "check": function(obj, newVal, oldVal){
                    this.accountCombo.setDisabled(newVal);
                }
            }
        });
        
//        this.accountComboConfig={
//            hiddenName:'accountid',         
//            store: this.accStore,
//            valueField:'accountid',
//            hideLabel:false,
//            hidden : false,
//            displayField:'accname',
//            emptyText: WtfGlobal.getLocaleText("acc.1099.selAcc"),
//            mode: 'local',
//            typeAhead: true,
//            selectOnFocus:true,
//            triggerAction:'all',
//            scope:this
//        };
        
//        this.cmbAccount = new Wtf.common.SelectPaging(Wtf.applyIf({
//            multiSelect:true,
//            fieldLabel:WtfGlobal.getLocaleText("acc.ledger.accName"),  //'Account Name',,
//            forceSelection:true,   
//            extraComparisionField:'acccode',// type ahead search on acccode as well.
//            pageSize:Wtf.CustomerCombopageSize,
//            width:200
//        },this.accountComboConfig));
        
        this.formPanel = new Wtf.FormPanel({
            autoScroll: true,
            border: false,
            url:"ACCAccountCMN/saveLimitedAccounts.do",
            method: 'POST',
            scope: this,
            labelWidth: 100,
            region: 'center',
            style: 'padding:10px',
            baseParams:{companyid: companyid},
            items: [this.moduleCombo, this.fieldCombo, this.showAllCheckbox, this.accountCombo]
        });
        
        Wtf.apply(this,{
            items: this.formPanel,
            buttons: this.butnArr
        });
        
        Wtf.account.MasterFormAccountWindow.superclass.initComponent.apply(this, arguments);
    },
    submitSelectedRecords : function(){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.field.LimitedAccounts.saveConfirm"),function(btn){
            if(btn!="yes") { return; }

            this.formPanel.form.baseParams.accountValues = this.accountCombo.getValue();
    //        this.formPanel.form.baseParams.accountValues = this.cmbAccount.getValue();
            this.formPanel.form.submit({
                waitMsg:WtfGlobal.getLocaleText("acc.msgbox.49"),
                baseParams:{},
                scope:this,
                success:function(f,a){
                    if(a.result.data.success){
                        //show success message
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), "Accounts mapped successfully."], 3);
                        /**
                         * set activation flag of mapped accounts in preferences
                         */
                        var showAll = this.showAllCheckbox ? this.showAllCheckbox.getValue() : true;
                        if(this.fieldCombo.getValue() == Wtf.MasterFormAccount.CustomerAccounts){
                            LimitedAccountsWindow.isLimitedCustomerAccounts = showAll;
                        } else if(this.fieldCombo.getValue() == Wtf.MasterFormAccount.VendorAccounts){
                            LimitedAccountsWindow.isLimitedVendorAccounts = showAll;
                        } else if(this.fieldCombo.getValue() == Wtf.MasterFormAccount.ProductPurchaseAccounts){
                            LimitedAccountsWindow.isLimitedProductPurchaseAccounts = showAll;
                        } else if(this.fieldCombo.getValue() == Wtf.MasterFormAccount.ProductSalesAccounts){
                            LimitedAccountsWindow.isLimitedProductSalesAccounts = showAll;
                        }
                        this.close();
                    } else{
                        /**
                         * if account ids present in then show popup message to map that accounts
                         */
                        if(a.result.data.accountids != undefined && a.result.data.accountids != ""){
                            var msg = WtfGlobal.getLocaleText("acc.field.MasterFormAccount.mandatoryMapAlert.Start");
                            
                            var accids = a.result.data.accountids.split(",");
                            for(var ind = 0; ind < accids.length; ind++){
                                var rec = WtfGlobal.searchRecord(this.accStore, accids[ind], "accountid");
                                if(msg.indexOf("<li>" + rec.data.accname + "</li>") == -1){
                                    msg += "<li>" + rec.data.accname + "</li>";
                                }
                            }
                            msg += WtfGlobal.getLocaleText("acc.field.MasterFormAccount.mandatoryMapAlert.End");
                            
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.accPref.tabTitle"), msg], 3);
                        }
                    }
                    
                },
                failure:function(f,a){
                }
            });
        },this)
    }
});