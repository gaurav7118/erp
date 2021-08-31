/*
 * Folowing Component is used for Setting Asset Depreciation options at company level From Company Preferences.
 */

Wtf.account.AssetSetting = function(config){
    Wtf.apply(this,{
        buttons:[this.saveButton = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                    minWidth: 50,
                     scope: this,
                    /**
                     * Wtf.account.companyAccountPref.openingDepreciationPosted is true when opening depreciaion of any asset is posted on the system
                     * Wtf.account.companyAccountPref.openingDepreciationPosted : Removed because earlier action item was user
                     * should not change/save the asset setting caluation options etc frequently so save button was hide on this check.
                     * Now all setting are already disable so user will not able to change/save asset setting caluation options but user able
                     * to change the other setting.ERP-34953
                     */
//                    hidden: Wtf.account.companyAccountPref.openingDepreciationPosted,
                    handler: this.saveForm.createDelegate(this)
                }),this.closeButton = new Wtf.Toolbar.Button({
                    text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
                    minWidth: 50,
                    scope: this,
                    handler: this.closeOpenWin.createDelegate(this)
                })]
    },config);
    
    Wtf.account.AssetSetting.superclass.constructor.call(this, config);
    
}

Wtf.extend(Wtf.account.AssetSetting, Wtf.Window,{
    
    onRender:function(config){
        Wtf.account.AssetSetting.superclass.onRender.call(this,config);

        this.createAssetSettingForm();
        var panelArr = new Array();
        panelArr = [this.DepreciationBasedOnForm, this.assetSettingForm, this.DepreciationSettingForm]
        this.AssetSettingPanel = new Wtf.Panel({
            width: 450,
            frame: false,
            height: 500,
            title: WtfGlobal.getLocaleText("acc.field.activatefixedasset"),
            items: panelArr
        });
        this.otherAssetSettingPanel = new Wtf.Panel({
            width: 450,
            frame: false,
            height: 500,
            title: WtfGlobal.getLocaleText("acc.fixed.asset.otherSettings"),
            items: [this.otherAssetSettingFormPanel]
        });
        this.AssetSettingPanelTab = new Wtf.TabPanel({
            autoScroll: true,
            region: 'center',
            frame: false,
            scope: this,
            closable: false,
            items: [this.AssetSettingPanel, this.otherAssetSettingPanel]
        });
        this.otherAssetSettingPanel.doLayout();
        this.AssetSettingPanelTab.doLayout();
        this.add(this.AssetSettingPanelTab);
        this.AssetSettingPanelTab.setActiveTab(this.AssetSettingPanel);

        this.otherAssetSettingPanel.on('activate', function () {
            this.otherAssetSettingPanel.doLayout();
        }, this);
        this.otherAssetSettingPanel.on('render', function () {
            this.AssetSettingPanelTab.setActiveTab(this.otherAssetSettingPanel);
        }, this);

    },
    
    createAssetSettingForm:function(){
        
        this.DepreciationBasedOn_FFYD = new Wtf.form.Radio({
            name:'assetdepreciation',
            labelAlign : 'left',
            disabled: Wtf.account.companyAccountPref.freezDepreciation,
            inputValue :'0',
            labelSeparator:'',
            checked:(Wtf.account.companyAccountPref.depreciationCalculationBasedOn == 0),
            boxLabel:WtfGlobal.getLocaleText("acc.accPref.firstfinancialYearDate")
        });
        
        this.DepreciationBasedOn_BBD = new Wtf.form.Radio({
            name:'assetdepreciation',
            labelAlign : 'left',
            disabled: Wtf.account.companyAccountPref.freezDepreciation,
            inputValue :'1',
            labelSeparator:'',
            checked:(Wtf.account.companyAccountPref.depreciationCalculationBasedOn == 1),
            boxLabel:WtfGlobal.getLocaleText("acc.accPref.bookBeginingDate")
        });
        
        this.assetDepreciationBasedOn = new Wtf.form.FieldSet({
            border:false,
            xtype:'fieldset',
            autoHeight:true,
            title:WtfGlobal.getLocaleText("acc.field.DepreciationBasedOn"),
            defaults:{border:false},
            items:[this.DepreciationBasedOn_FFYD,this.DepreciationBasedOn_BBD]
        });
        
        this.highlightDepreciatedAssets = new Wtf.form.Checkbox({
            name:'highlightDepreciatedAssets',
            id:'highlightDepreciatedAssets'+this.id,
            hiddeName:'highlightDepreciatedAssets',
            fieldLabel:WtfGlobal.getLocaleText("acc.fixed.asset.highlightDepreciatedAssets"),
            checked:CompanyPreferenceChecks.highlightDepreciatedAssets(),
            cls : 'custcheckbox',
            width: 10
        });
        
        this.highlightDepreciatedAssets.on('check', this.highlightDepreciatedAssetsChanged,this);
        
        this.otherSettings = new Wtf.form.FieldSet({
            border:false,
            xtype:'fieldset',
            autoHeight:true,
            labelWidth:300,
            title:WtfGlobal.getLocaleText("acc.fixed.asset.otherSettings"),
            defaults:{
                border:false
            },
            items:[this.highlightDepreciatedAssets]
        });

        this.DepreciationBasedOnForm = new Wtf.form.FormPanel({
            border:false,
            autoWidth:true,
            autoHeight:true,
            labelWidth:0,
            anchor:'100%',
            bodyStyle:'margin:10px',
            items:[this.assetDepreciationBasedOn]
        });
        
        
        this.assetSetingActivation= new Wtf.form.Checkbox({
            name:'assetSetingActivation',
            id:'assetSetingActivation'+this.id,
            hiddeName:'assetSetingActivation',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.activateassetanddepr"),//Activate Assets Depreciation Calculation Method
            checked:Wtf.account.companyAccountPref.ActivateFixedAssetModule,
            disabled:Wtf.account.companyAccountPref.freezDepreciation,
            cls : 'custcheckbox',
            width: 10
        });
        
        this.assetSetingActivation.on('check', this.assetSetingActivationChanged,this);
        
        // create radio button group
        
        this.fullYear = new Wtf.form.Radio({
            name:'assetacquisition',
            labelAlign : 'left',
            disabled:Wtf.account.companyAccountPref.freezDepreciation||(!Wtf.account.companyAccountPref.ActivateFixedAssetModule),
            inputValue :'0',
            width:300,
            labelSeparator:'',
            checked:(Wtf.account.companyAccountPref.depreciationCalculationType==0),
            boxLabel:WtfGlobal.getLocaleText("acc.depreciation.method1")//Beginning of the Year of Acquisition (Yearwise)
        });
        
        this.actualDate = new Wtf.form.Radio({
            name:'assetacquisition',
            labelAlign : 'left',
            disabled:Wtf.account.companyAccountPref.freezDepreciation||(!Wtf.account.companyAccountPref.ActivateFixedAssetModule),
            inputValue :'1',
            labelSeparator:'',
            checked:(Wtf.account.companyAccountPref.depreciationCalculationType==1),
            boxLabel:WtfGlobal.getLocaleText("acc.depreciation.method2")//Beginning of the Date of Acquisition (Monthwise)
        });
        
        this.actualDateNoOfDays = new Wtf.form.Radio({
            name:'assetacquisition',
            labelAlign : 'left',
            disabled:Wtf.account.companyAccountPref.freezDepreciation||(!Wtf.account.companyAccountPref.ActivateFixedAssetModule),
            inputValue :'3',
            labelSeparator:'',
            checked:(Wtf.account.companyAccountPref.depreciationCalculationType==3),
            boxLabel:WtfGlobal.getLocaleText("acc.depreciation.method4")//Beginning of the Date of Acquisition (No. of days)
        });
        
        this.actualMonth = new Wtf.form.Radio({
            name:'assetacquisition',
            labelAlign : 'left',
            disabled:Wtf.account.companyAccountPref.freezDepreciation||(!Wtf.account.companyAccountPref.ActivateFixedAssetModule),
            inputValue :'2',
            labelSeparator:'',
            checked:(Wtf.account.companyAccountPref.depreciationCalculationType==2),
            boxLabel:WtfGlobal.getLocaleText("acc.depreciation.method3")//Beginning of the Month of Acquisition (Monthwise)
        });
        
        
        var assetAcquisitionItems = [];
        assetAcquisitionItems.push(this.fullYear,this.actualDate);
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
            assetAcquisitionItems.push(this.actualDateNoOfDays);
        }
        assetAcquisitionItems.push(this.actualMonth);
        this.assetAcquisition = new Wtf.form.FieldSet({
            border:false,
            xtype:'fieldset',
            autoHeight:true,
            title:WtfGlobal.getLocaleText("acc.field.DepreciationEffectiveFrom"),
            defaults:{border:false},
            items:assetAcquisitionItems
        });
        
        // Depreciation Calculation Method
        var items = [];
        items.push(this.assetSetingActivation);
        
        this.assetSettingForm = new Wtf.form.FormPanel({
            border:false,
            autoWidth:true,
            autoHeight:true,
            labelWidth:300,
            anchor:'100%',
            bodyStyle:'margin:10px; margin-top:5px;',
            items:items
        });
        
        this.DepreciationSettingForm = new Wtf.form.FormPanel({
            border:false,
            autoWidth:true,
            autoHeight:true,
            labelWidth:0,
            anchor:'100%',
            bodyStyle:'margin:10px',
            items:[this.assetAcquisition]
        });
        
         this.otherAssetSettingFormPanel = new Wtf.form.FormPanel({
            border: false,
            autoWidth: true,
            autoHeight: true,
            labelWidth: 0,
            anchor: '100%',
            bodyStyle: 'margin:10px',
            items: [this.otherSettings]
        });
        
    },
    
    closeOpenWin:function(){
        this.close();
    },
    
    saveForm:function(){
//        this.saveButton.disable();
        
        var depreciationCalculationType = 0;
        
        if (this.fullYear.getValue()){
            depreciationCalculationType = 0;
        } else if(this.actualDate.getValue()){
            depreciationCalculationType = 1;
        } else if(this.actualMonth.getValue()){
            depreciationCalculationType = 2;
        } else if(this.actualDateNoOfDays.getValue()){
            depreciationCalculationType = 3;
        }
        
        var depreciationCalculationBasedOn = 0;
        if (this.DepreciationBasedOn_FFYD.getValue()){
            depreciationCalculationBasedOn = 0;
        } else if(this.DepreciationBasedOn_BBD.getValue()){
            depreciationCalculationBasedOn = 1;
        }
        
//        var depreciationCalculationMethod
        
        Wtf.Ajax.requestEx({
            url : "ACCCompanyPref/saveFixedAssetSetting.do",
            params:{
                assetSetingActivation:this.assetSetingActivation.getValue(),
                highlightDepreciatedAssets:this.highlightDepreciatedAssets.getValue(),
                depreciationCalculationType:depreciationCalculationType,
                depreciationCalculationBasedOn: depreciationCalculationBasedOn
            }
        },this,
        function(req,res){
            var restext=req;
            if(restext.success){
                Wtf.account.companyAccountPref.ActivateFixedAssetModule = this.assetSetingActivation.getValue();
                Wtf.account.companyAccountPref.columnPref.highlightDepreciatedAssets = this.highlightDepreciatedAssets.getValue();
                Wtf.account.companyAccountPref.depreciationCalculationType=depreciationCalculationType;
                Wtf.account.companyAccountPref.depreciationCalculationBasedOn=depreciationCalculationBasedOn;
//                if(Wtf.account.companyAccountPref.ActivateFixedAssetModule != undefined && Wtf.account.companyAccountPref.ActivateFixedAssetModule != null){
//                    if(Wtf.getCmp('fixedAssetNavigationPanelID'))
//                        Wtf.getCmp('fixedAssetNavigationPanelID').setVisible(Wtf.account.companyAccountPref.ActivateFixedAssetModule);
//                }
                
                this.close();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),restext.msg],3);
            } else
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),restext.msg],1);
                
        },
        function(response){
            this.saveButton.enable();
            var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        });
    },
    
    assetSetingActivationChanged:function(c,val){
        if(val && !Wtf.account.companyAccountPref.freezDepreciation){
            this.fullYear.enable();
            this.actualDate.enable();
            this.actualDateNoOfDays.enable();
            this.actualMonth.enable();
        }else{
            this.fullYear.disable();
            this.actualDate.disable();
            this.actualDateNoOfDays.disable();
            this.actualMonth.disable();
       }
    },
    
    highlightDepreciatedAssetsChanged:function(c, val){
        if(!Wtf.account.companyAccountPref.openingDepreciationPosted){
            if(val){
                Wtf.account.companyAccountPref.columnPref.highlightDepreciatedAssets = true;
            }else{
                Wtf.account.companyAccountPref.columnPref.highlightDepreciatedAssets = false;
            }
        }
    }
})


// Control Accounts Settings

Wtf.account.ControlAccountsSettings = function(config){
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
    
    Wtf.account.ControlAccountsSettings.superclass.constructor.call(this, config); 
}

Wtf.extend(Wtf.account.ControlAccountsSettings, Wtf.Window,{
    
    onRender:function(config){
        Wtf.account.ControlAccountsSettings.superclass.onRender.call(this,config);
        
        this.accRec = Wtf.data.Record.create ([{
            name:'accountname',
            mapping:'accname'
        },{
            name:'accountid',
            mapping:'accid'
        }]);

        this.accountStore = new Wtf.data.Store({
            url : "ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{ 
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,  
                ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true,
                controlAccounts: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        this.accountStore.load();        
        
        // Create Combo-box to select the defaults Account
        this.createControlAccountsSettings();
        
        this.accountStore.on("load",function(){
            var storeNewRecord=new this.accRec({      // Create None Value record
                accountname:'None',
                accountid:'None'
            });
            this.accountStore.insert(0,storeNewRecord);  // inser None value record as first record in store
            if(Wtf.account.companyAccountPref.profitLossAccountId == "" || Wtf.account.companyAccountPref.profitLossAccountId==undefined){
                this.ProfitLossAccount.setValue(this.accountStore.getAt(0).data.accountid); // if no Account mapped then set as "None"
            }else{
                this.ProfitLossAccount.setValue(Wtf.account.companyAccountPref.profitLossAccountId);
            }
            if(Wtf.account.companyAccountPref.openingStockAccountId == "" ||Wtf.account.companyAccountPref.openingStockAccountId==undefined){
                this.OpeningStockAccount.setValue(this.accountStore.getAt(0).data.accountid); // if no Account mapped then set as "None"
            }else{
                this.OpeningStockAccount.setValue(Wtf.account.companyAccountPref.openingStockAccountId);
            }
            if(Wtf.account.companyAccountPref.closingStockAccountId == "" || Wtf.account.companyAccountPref.closingStockAccountId == undefined){
                this.ClosingStockAccount.setValue(this.accountStore.getAt(0).data.accountid); // if no Account mapped then set as "None"
            }else{
                this.ClosingStockAccount.setValue(Wtf.account.companyAccountPref.closingStockAccountId);
            }
            if(Wtf.account.companyAccountPref.stockInHandAccountId == "" || Wtf.account.companyAccountPref.stockInHandAccountId == undefined){
                this.StockInHandAccount.setValue(this.accountStore.getAt(0).data.accountid); // if no Account mapped then set as "None"
            }else{
                this.StockInHandAccount.setValue(Wtf.account.companyAccountPref.stockInHandAccountId);
            }
        },this);
        
        this.add({
            region: 'center',
            border: false,
            layout:'fit',
            baseCls:'bckgroundcolor',
            items:[this.ControlAccountsSettingsForm]
        }
        );
    },
    
    createControlAccountsSettings:function(){
        
        this.ProfitLossAccount= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.SelectAccountForProfitLoss"),
            name:'profitLossAccountId',
            store:this.accountStore,
            forceSelection: false,
            allowBlank:true,
            hiddenName:'profitLossAccountId',
            displayField:'accountname',
            valueField:'accountid',
            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
        });
        
        this.OpeningStockAccount= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.selecaccountforOpeningstock"),
            name:'openingStockAccountId',
            store:this.accountStore,
            forceSelection: false,
            allowBlank:true,
            hiddenName:'openingStockAccountId',
            displayField:'accountname',
            valueField:'accountid',
            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
        });
        
        this.ClosingStockAccount= new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.selectaccountforclosingstock"),
            name:'closingStockaccountId',
            store:this.accountStore,
            forceSelection: false,
            allowBlank:true,
            hiddenName:'closingStockaccountId',
            displayField:'accountname',
            valueField:'accountid',
            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
        });
        
        this.StockInHandAccount= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.filed.selectaccountforstockinhand"),
            name:'stockInHandAccountId',
            store:this.accountStore,
            forceSelection: false,
            allowBlank:true,
            hiddenName:'stockInHandAccountId',
            displayField:'accountname',
            valueField:'accountid',
            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText")
        })
        
        this.ControlAccountsSettingsForm = new Wtf.form.FormPanel({
            border:false,
            autoWidth:true,
            autoHeight:true,
            anchor:'100%',
            labelWidth:200,
            bodyStyle:'margin:10px',
            items:[this.ProfitLossAccount]
        });
        
    },    
    
    closeOpenWin:function(){
        this.close();
    },
    
    saveForm:function(){
        this.saveButton.disable();
               
        Wtf.Ajax.requestEx({
            url : "ACCCompanyPref/saveControlAccountsSettings.do",
            params:{
                profitLossAccountId: this.ProfitLossAccount.getValue(),
                openingStockAccountId: this.OpeningStockAccount.getValue(),
                closingStockAccountId: this.ClosingStockAccount.getValue(),
                stockInHandAccountId: this.StockInHandAccount.getValue()
            }
        },this,
        function(req,res){
            var restext=req;
            if(restext.success){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),restext.msg],3);
                getCompanyAccPref();
                this.close();
            }  else{
                        /* Show warning message */
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), restext.msg], 2);
                this.saveButton.enable();
            }
        },
        function(response){
            this.saveButton.enable();
            var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        });
    }
})