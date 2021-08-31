/*
 * Folowing Component is used for Allow Zero quantity Setting product at company level From Company Preferences.
 */

Wtf.account.AllowZeroQuantityForProduct = function(config){
    Wtf.apply(this,{
        buttons:[this.saveButton = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                    minWidth: 50,
                    scope: this,
                    handler: this.saveForm.createDelegate(this)
                }),this.closeButton = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
                    minWidth: 50,
                    scope: this,
                    handler: this.closeOpenWin.createDelegate(this)
                })]
    },config);
    
    Wtf.account.AllowZeroQuantityForProduct.superclass.constructor.call(this, config);
    
}

Wtf.extend(Wtf.account.AllowZeroQuantityForProduct, Wtf.Window,{
    
    onRender:function(config){
        Wtf.account.AllowZeroQuantityForProduct.superclass.onRender.call(this,config);
        /**
         * method to create to form
         */
        this.createAllowZeroQuantityForProductForm();
        
        this.add(this.ProductQuantitySettingForm);
        
    },
    /**
     * Added ckeck boxes for SI,PI,SO,PO,VQ,CQ,DO,GR
     * 
     */
    createAllowZeroQuantityForProductForm:function(){
        this.AllowZeroQuantity=new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.allowzeroqtyforproductindo"),
            name:'aloowzeroqty',
            labelStyle: "width : 200px;",
            width: 300,
            checked:Wtf.account.companyAccountPref.allowZeroQuantityInDO
        });
        this.AllowZeroQuantityInQuotation=new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.allowzeroqtyforproductinquotation"),
            name:'allowzeroqtyquo',
            labelStyle: "width : 200px;",
            width: 300,
            checked:Wtf.account.companyAccountPref.allowZeroQuantityInCQ
        });
        this.AllowZeroQuantityInSI=new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.allowzeroqtyforproductinSI"),
            name:'allowzeroqtysi',
            labelStyle: "width : 200px;",
            width: 300,
            checked:Wtf.account.companyAccountPref.allowZeroQuantityInSI
        });
        this.AllowZeroQuantityInPI=new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.allowzeroqtyforproductinPI"),
            name:'allowzeroqtypi',
            labelStyle: "width : 200px;",
            width: 300,
            checked:Wtf.account.companyAccountPref.allowZeroQuantityInPI
        });
        this.AllowZeroQuantityInSO=new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.allowzeroqtyforproductinSO"),
            name:'allowzeroqtyso',
            labelStyle: "width : 200px;",
            width: 300,
            checked:Wtf.account.companyAccountPref.allowZeroQuantityInSO
        });
        this.AllowZeroQuantityInPO=new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.allowzeroqtyforproductinPO"),
            name:'allowzeroqtypo',
            labelStyle: "width : 200px;",
            width: 300,
            checked:Wtf.account.companyAccountPref.allowZeroQuantityInPO
        });
        this.AllowZeroQuantityInSR=new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.allowzeroqtyforproductinSR"),
            name:'allowzeroqtysr',
            labelStyle: "width : 200px;",
            width: 300,
            checked:Wtf.account.companyAccountPref.allowZeroQuantityInSR
        });
        this.AllowZeroQuantityInPR=new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.allowzeroqtyforproductinPR"),
            name:'allowzeroqtypr',
            labelStyle: "width : 200px;",             width: 300,
            checked:Wtf.account.companyAccountPref.allowZeroQuantityInPR
        });
        this.AllowZeroQuantityInGRO=new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.allowzeroqtyforproductinGRO"),
            name:'allowzeroqtygro',
            labelStyle: "width : 200px;",
            width: 300,
            checked:Wtf.account.companyAccountPref.AllowZeroQuantityInGRO
        });
        this.AllowZeroQuantityInVQ=new Wtf.form.Checkbox({
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.allowzeroqtyforproductinVQ"),
            name:'allowzeroqtyvq',
            labelStyle: "width : 200px;",
            width: 300,
            checked:Wtf.account.companyAccountPref.AllowZeroQuantityInVQ
        });
        
        this.salesFieldset = new Wtf.form.FieldSet({
            border:false,
            xtype:'fieldset',
            autoHeight:true,
            title:WtfGlobal.getLocaleText("acc.companypreferences.SalesModules"),
            defaults:{border:false},
            items:[this.AllowZeroQuantityInQuotation,this.AllowZeroQuantityInSO,this.AllowZeroQuantityInSI,this.AllowZeroQuantity,this.AllowZeroQuantityInSR]
        });
        this.purchaseFieldset = new Wtf.form.FieldSet({
            border:false,
            xtype:'fieldset',
            autoHeight:true,
            title:WtfGlobal.getLocaleText("acc.companypreferences.PurchaseModules"),
            defaults:{border:false},
            items:[this.AllowZeroQuantityInVQ,this.AllowZeroQuantityInPO,this.AllowZeroQuantityInPI,this.AllowZeroQuantityInGRO,this.AllowZeroQuantityInPR]
        });
        this.ProductQuantitySettingForm = new Wtf.form.FormPanel({
            waitMsgTarget: true,
            border: false,
            bodyStyle: "background: transparent;",
            style: "background:transparent; padding:20px;",
            defaultConfig:{
                labelWidth: 160
            },
            items: [this.salesFieldset,this.purchaseFieldset]
        });
    },
    
    closeOpenWin:function(){
        this.close();
    },
    /**
     * 
     * Save method for allow zero quantity setting
     */
    saveForm:function(){
        Wtf.Ajax.requestEx({
            url : "ACCCompanyPref/saveAllowZeroQtyForProduct.do",
            params:{
              AllowZeroQuantityForDO  :this.AllowZeroQuantity.getValue(),
              AllowZeroQuantityInQuotation  :this.AllowZeroQuantityInQuotation.getValue(),
              AllowZeroQuantityInSI  :this.AllowZeroQuantityInSI.getValue(),
              AllowZeroQuantityInPI  :this.AllowZeroQuantityInPI.getValue(),
              AllowZeroQuantityInSO  :this.AllowZeroQuantityInSO.getValue(),
              AllowZeroQuantityInPO  :this.AllowZeroQuantityInPO.getValue(),
              AllowZeroQuantityInSR  :this.AllowZeroQuantityInSR.getValue(),
              AllowZeroQuantityInPR  :this.AllowZeroQuantityInPR.getValue(),
              AllowZeroQuantityInGRO  :this.AllowZeroQuantityInGRO.getValue(),
              AllowZeroQuantityInVQ  :this.AllowZeroQuantityInVQ.getValue()
                
                
            }
        },this,
        function(req,res){
            var restext=req;
            if(restext.success){
                this.close();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),restext.msg],3);
            } else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),restext.msg],1); 
            }
                
        },
        function(response){
        });
    }
    
})



/*
 * Folowing Component is used for India GST settings
 */

Wtf.account.IndianGSTSettings = function(config) {
    Wtf.apply(this, {
        buttons: [this.saveButton = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                minWidth: 50,
                scope: this,
                handler: this.saveForm.createDelegate(this)
            }), this.closeButton = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
                minWidth: 50,
                scope: this,
                handler: this.closeOpenWin.createDelegate(this)
            })]
    }, config);

    Wtf.account.IndianGSTSettings.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.IndianGSTSettings, Wtf.Window, {
    onRender: function(config) {
        Wtf.account.IndianGSTSettings.superclass.onRender.call(this, config);
        /**
         * method to create to form
         */
        this.createIndianGSTSettingsForm();

        this.add(this.IndianGSTSettingsForm);

    },
    /**
     * 
     */
    createIndianGSTSettingsForm: function() {
        this.istaxonadvancereceipt = new Wtf.form.Checkbox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.IndianGSTSettings.istaxonadvancereceipttooltip")+"'>"+WtfGlobal.getLocaleText("acc.IndianGSTSettings.istaxonadvancereceipt")+"</span>",
            name: 'istaxonadvancereceipt',
            labelStyle: "width : 200px;",
            width: 300,
            checked: Wtf.istaxonadvancereceipt
        });
        this.istcsapplicable = new Wtf.form.Checkbox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.IndianGSTSettings.istcsapplicabletooltip")+"'>"+WtfGlobal.getLocaleText("acc.IndianGSTSettings.istcsapplicable")+"</span>",
            name: 'istcsapplicable',
            labelStyle: "width : 200px;",
            width: 300,
            checked: Wtf.istcsapplicable
        });
        this.istdsapplicable = new Wtf.form.Checkbox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.IndianGSTSettings.istdsapplicabletooltip")+"'>"+WtfGlobal.getLocaleText("acc.IndianGSTSettings.istdsapplicable")+"</span>",
            name: 'istdsapplicable',
            labelStyle: "width : 200px;",
            width: 300,
            checked: Wtf.istdsapplicable
        });
        this.AllowToMapAccounts = new Wtf.form.Checkbox({//For Allowing accounts in invoices at Products line level
            name: 'AllowToMapAccounts',
            id: 'AllowToMapAccounts',
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.AllowToMapAccountstooltip")+"'>"+WtfGlobal.getLocaleText("acc.field.AllowToMapAccounts")+"</span>",
            hidden: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA,
            hideLabel: Wtf.account.companyAccountPref.countryid != Wtf.Country.INDIA,
            checked: Wtf.account.companyAccountPref.AllowToMapAccounts,
            scope: this,
            width: 300,
            cls: 'custcheckbox'
        });
        this.amountInIndianWord = new Wtf.form.Checkbox({
                    name: 'amountInIndianWord',
                    id: 'amountInIndianWord',
                    fieldLabel: WtfGlobal.getLocaleText("acc.field.AmountInIndianWord"),
                    checked: Wtf.account.companyAccountPref.amountInIndianWord,
                    scope: this,
                    width: 300,
                    cls: 'custcheckbox'
        });
        this.isitcapplicable = new Wtf.form.Checkbox({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.IndianGSTSettings.isitcapplicabletooltip") + "'>" + WtfGlobal.getLocaleText("acc.IndianGSTSettings.isitcapplicable") + "</span>",
            name: 'isitcapplicable',
            labelStyle: "width : 200px;",
            width: 300,
            checked: Wtf.isitcapplicable
        });
        this.settingFieldset = new Wtf.form.FieldSet({
            border: false,
            xtype: 'fieldset',
            autoHeight: true,
//            title:WtfGlobal.getLocaleText("acc.IndianGSTSettings.gstmodule"),
            defaults: {border: false},
            items: [this.istaxonadvancereceipt,this.istcsapplicable,this.istdsapplicable,this.AllowToMapAccounts,this.amountInIndianWord,this.isitcapplicable]
        });
  
            this.tdstcsFieldset = new Wtf.form.FieldSet({
            border: false,
            xtype: 'fieldset',
            autoHeight: true,
            hidden:!(Wtf.istdsapplicable || Wtf.istcsapplicable),
            title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.TDSTCS.AccountSetting")+"'>"+WtfGlobal.getLocaleText("acc.TDSTCS.AccountSetting")+"</span>",
                        defaults:{
                            anchor:'80%'
                        },
                             items:[this.tdsAccount= new Wtf.form.ExtFnComboBox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.TDS.Account"),
                            fieldLabel: "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.TDS.Accounttooltip") +"'>"+ WtfGlobal.getLocaleText("acc.TDS.Account") +"</span>",
                            name:'tdsAccount',
                            store:this.parentObj.dgStore,
                            forceSelection: true,
                            allowBlank:true,
                            hideLabel:!Wtf.istdsapplicable,
                            hidden:!Wtf.istdsapplicable,
                            hiddenName:'tdsAccount',
                            displayField:'accountname',
                            valueField:'accountid',
                            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
                            extraComparisionField:'acccode',// type ahead search on acccode as well.
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        }),
                        this.tcsAccount= new Wtf.form.ExtFnComboBox({
                            fieldLabel: "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.TCS.Accounttooltip") +"'>"+ WtfGlobal.getLocaleText("acc.TCS.Account") +"</span>",
                            name:'tcsAccount',
                            store:this.parentObj.dgStore,
                            forceSelection: true,
                            hideLabel:!Wtf.istcsapplicable,
                            hidden:!Wtf.istcsapplicable,
                            allowBlank:true,
                            hiddenName:'tcsAccount',
                            displayField:'accountname',
                            valueField:'accountid',
                            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                            listWidth:Wtf.account.companyAccountPref.accountsWithCode?550:400,
                            extraComparisionField:'acccode',// type ahead search on acccode as well.
                            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
                        })]
        });
        if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
            this.tdsAccount.addNewFn = this.parentObj.addNewAccount.createDelegate(this, [false, null, "coaWin", false, true, Wtf.account.companyAccountPref.columnPref.tdsAccount, this.tdsAccount.store], true);
            this.tcsAccount.addNewFn = this.parentObj.addNewAccount.createDelegate(this, [false, null, "coaWin", false, true, Wtf.account.companyAccountPref.columnPref.tcsAccount, this.tcsAccount.store], true);
        }
        if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
            this.tdsAccount.setValue(Wtf.account.companyAccountPref.columnPref.tdsAccount);
            this.tcsAccount.setValue(Wtf.account.companyAccountPref.columnPref.tcsAccount);
        }
        this.IndianGSTSettingsForm = new Wtf.form.FormPanel({
            waitMsgTarget: true,
            border: false,
            bodyStyle: "background: transparent;",
            style: "background:transparent; padding:20px;",
            defaultConfig: {
                labelWidth: 160
            },
            items: [this.settingFieldset,this.tdstcsFieldset]
        });
    },
    closeOpenWin: function() {
        this.close();
    },
    /**
     * 
     * Save method for allow zero quantity setting
     */
    saveForm: function() {
        var columnPref = {};
        if (this.tdsAccount != undefined) {
            this.parentObj.tdsAccount = this.tdsAccount != undefined ? this.tdsAccount.getValue() : "";
        }
        if (this.tcsAccount != undefined) {
            this.parentObj.tcsAccount = this.tcsAccount != undefined ? this.tcsAccount.getValue() : "";
        }
        Wtf.Ajax.requestEx({
            url: "ACCCompanyPref/saveIndianGSTSettings.do",
            params: {
                istaxonadvancereceipt: this.istaxonadvancereceipt.getValue(),
                istcsapplicable: this.istcsapplicable.getValue(),
                isitcapplicable:this.isitcapplicable.getValue(),
                istdsapplicable: this.istdsapplicable.getValue(),
                AllowToMapAccounts :this.AllowToMapAccounts.getValue(),
                amountInIndianWord :this.amountInIndianWord.getValue()
                
            }
        }, this,
                function(req, res) {
                    var restext = req;
                    if (restext.success) {
                        this.close();
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), restext.msg], 3);
                    } else {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), restext.msg], 1);
                    }

                },
                function(response) {
                });
    }

})

