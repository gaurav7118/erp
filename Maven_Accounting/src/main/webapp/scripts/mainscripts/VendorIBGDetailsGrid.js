
Wtf.account.VendorIBGDetailsGrid = function(config){
    
    this.isFromMasterConfiguration = config.isFromMasterConfiguration?config.isFromMasterConfiguration:false;
    this.isFromMP = config.isFromMP? config.isFromMP : false;
    this.bankType = config.bankType?config.bankType:1;
    this.isCustomer = config.isCustomer;
    this.customerId = config.customerId;
    this.uPermType=config.isCustomer?Wtf.UPerm.customer:Wtf.UPerm.vendor;
    this.permType=config.isCustomer?Wtf.Perm.customer:Wtf.Perm.vendor;
    Wtf.apply(this,{
        buttons:[this.closeButton = new Wtf.Toolbar.Button({
                    text: this.isFromMP? WtfGlobal.getLocaleText("acc.common.saveBtn") : WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
                    minWidth: 50,
                    disabled:this.isFromMP? true : false,
                    scope: this,
                    handler: this.isFromMP? this.saveOpenBalanceWin.createDelegate(this) : this.closeOpenBalanceWin.createDelegate(this)
            })]
    },config);
    Wtf.account.VendorIBGDetailsGrid.superclass.constructor.call(this, config);
    
    this.addEvents({
        'update':true
    });
    
}

Wtf.extend(Wtf.account.VendorIBGDetailsGrid, Wtf.Window,{
    resizable:false,
    onRender:function(config){
        Wtf.account.VendorIBGDetailsGrid.superclass.onRender.call(this,config);
        
        //create account information form
        this.createAccountInformationForm();
        
        //this.create IBG Details Grid
        
        this.createGrid();
        
        // load customer Information
        this.setAccountInfo();
        
        
        //adding region
        
        this.add(
        this.northPanel= new Wtf.Panel({
            region:'north',
            bodyStyle:'background:#f1f1f1;border-bottom:1px solid #bfbfbf;',
            height:100,
            hidden:this.isFromMasterConfiguration || this.isFromMP,
            border:false,
            items:[this.accountInformationForm]
        }),this.centerPanel = new Wtf.Panel({
            region:'center',
            layout:'fit',
            border:false,
            items:[this.transactionGridContainerPanel]
        })
        );
    },
    
    setAccountInfo:function(){
        if(this.accRec!=null && this.accRec!=undefined){
            this.accountCode.setValue(this.accRec.get('acccode'));
            this.accountName.setValue(this.accRec.get('accname'));
        }
    },
    
    createAccountInformationForm:function(){
        this.accountInformationForm = new Wtf.form.FormPanel({
            border:false,
            autoWidth:true,
            height:100,
            bodyStyle:'margin:40px 10px 10px 30px',
//            labelWidth:100,
            items:[
                {
                    layout:'column',
                    border:false,
                    items:[{
                        columnWidth:'.45',
                        layout:'form',
                        border:false,
                        items:[
                            this.accountCode = new Wtf.form.TextField({
                                fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.dimension.module.14"):WtfGlobal.getLocaleText("acc.dimension.module.15"))+" "+WtfGlobal.getLocaleText("acc.field.Code"),
                                readOnly:true,
                                name:'accountCodeInOpenBalance',
                                width:150
                            })
                        ]
                    },{
                        columnWidth:'.45',
                        layout:'form',
                        border:false,
                        items:[
                            this.accountName = new Wtf.form.TextField({
                                fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.dimension.module.14"):WtfGlobal.getLocaleText("acc.dimension.module.15"))+" "+WtfGlobal.getLocaleText("acc.customerList.gridName"),
                                name:'accountNameInOpenBalance',
                                readOnly:true,
                                width:150
                            })
                        ]
                  }]
             }
            ]
        });
    },
    
    createGrid:function(){
        
        this.gridRec = new Wtf.data.Record.create([
            {name:'ibgId'},
            {name:'cimbReceivingBankDetailId'},
            {name:'receivingBankCode'},
            {name:'receivingBankName'},
            {name:'receivingBranchCode'},
            {name:'receivingAccountNumber'},
            {name:'receivingAccountName'},
            {name:'collectionAccNo'},
            {name:'collectionAccName'},
            {name:'giroBICCode'},
            {name:'refNumber'},
            {name:'emailForGiro'},
            {name:'usedInPayment'},
            {name:'customerBankAccountType'},
            {name:'customerBankAccountTypeValue'},
            {name:'UOBBICCode'},
            {name:'UOBReceivingBankAccNumber'},
            {name:'UOBReceivingAccName'},
            {name:'UOBEndToEndID'},
            {name:'UOBMandateId'},
            {name:'UOBPurposeCode'},
            {name:'UOBCustomerReference'},
            {name:'UOBUltimatePayerBeneficiaryName'},
            {name:'UOBCurrency'},
            {name:'UOBReceivingBankDetailId'},
            {name:'UOBBankName'},//UOBBankName ID
            {name:'UOBBankNameValue'},//UOBBankName Value
            {name:'UOBBankCode'},
            {name:'activated'},
            {name:'UOBBranchcode'},
            {name:'ocbcIBGDetailId'},
            {name:'ocbcBankCode'},
            {name:'ocbcVendorAccountNumber'},
            {name:'ocbcUltimateCreditorName'},
            {name:'ocbcUltimateDebtorName'},
            {name:'ocbcSendRemittanceAdviceVia'},
            {name:'ocbcRemittanceAdviceSendDetails'}
        ]);
        
        // Reader for grid store
    
        this.gridStoreReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.gridRec);
        var storeUrl = this.getURLForGridStore();
        this.gridStore = new Wtf.data.Store({
            url:storeUrl,
            baseParams:{
            },
            reader:this.gridStoreReader
        });
        
        this.loadMask = new Wtf.LoadMask(document.body,{
            msg : WtfGlobal.getLocaleText("acc.msgbox.50")
        });
        
        this.gridStore.on('loadexception',function(){
            var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
            this.loadMask.hide();
        },this);
        
        this.gridStore.on('beforeload',function(){
            this.loadMask.show();
            
            if(this.isFromMasterConfiguration){ // if viewing details from masteritem grid
                this.gridStore.baseParams.masterItem = this.accRec.get('id');
            } else {
                if(this.isCustomer){
                    this.gridStore.baseParams.customer = this.accRec.get('accid');
                } else {
                    this.gridStore.baseParams.vendor = this.accRec.get('accid');
                }    
            }
            
        },this);
        
        this.gridStore.on('load',function(){
            this.loadMask.hide();
        },this);
        
        this.gridStore.load()
        
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect:true
        });
        
        // Column model creation for transaction grid
    
        this.colModel = this.isCustomer ?(new Wtf.grid.ColumnModel([this.sm,{
                header:WtfGlobal.getLocaleText("acc.masterConfig.61"),
                dataIndex:'customerBankAccountTypeValue'
            },{
                header:WtfGlobal.getLocaleText("acc.field.BankName"),
                dataIndex:'UOBBankNameValue'
            },{
                header:WtfGlobal.getLocaleText("acc.uob.receivingBICCode"),
                dataIndex:'UOBBICCode'
            },{
                header:WtfGlobal.getLocaleText("acc.masterCoonfig.bankCode"),
                dataIndex:'UOBBankCode'
            },{
                header:WtfGlobal.getLocaleText("acc.vendorIBG.BranchCode"),
                dataIndex:'UOBBranchcode'
            },{
                header:WtfGlobal.getLocaleText("acc.uob.receivingBankAccNO"),
                dataIndex:'UOBReceivingBankAccNumber'
            },{
                header:WtfGlobal.getLocaleText("acc.uob.receivingAccName"),
                dataIndex:'UOBReceivingAccName'
            },{
                header:WtfGlobal.getLocaleText("acc.uob.endToEndId"),
                dataIndex:'UOBEndToEndID'
            },{
                header:WtfGlobal.getLocaleText("acc.uob.mandateId"),
                dataIndex:'UOBMandateId'
            },{
                header:WtfGlobal.getLocaleText("acc.cimb.purposeCode"),
                dataIndex:'UOBPurposeCode'
            },{
                header:WtfGlobal.getLocaleText("acc.uob.customerReference"),
                dataIndex:'UOBCustomerReference'
            },{
                header:WtfGlobal.getLocaleText("acc.uob.ultimatePayerName"),
                dataIndex:'UOBUltimatePayerBeneficiaryName'
            },{
                header:WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),
                dataIndex:'UOBCurrency'
            }, {
                header: WtfGlobal.getLocaleText("acc.repeatedJE.Gridcol6"),
                pdfwidth: 20,
                renderer: function (v, m, rec) {    //To show "Yes/No" in UI for "true/false" value
                    if (!v)
                        return "No";
                    if (rec.data.activated) {
                        v = 'Yes';
                    }
                    return v;
                },
                dataIndex: 'activated'
            }
        ])) :  (new Wtf.grid.ColumnModel([this.sm,{
                header:WtfGlobal.getLocaleText("acc.vendorIBG.IBGBankCode"),
                dataIndex:'receivingBankCode',
                align:'center'
            },{
                header:WtfGlobal.getLocaleText("acc.vendorIBG.IBGBankName"),
                dataIndex:'receivingBankName',
                align:'center'
            },{
                header:WtfGlobal.getLocaleText("acc.vendorIBG.BranchCode"),
                dataIndex:'receivingBranchCode',
                align:'center'
            },{
                header:WtfGlobal.getLocaleText("acc.vendorIBG.AccountNumber"),
                dataIndex:'receivingAccountNumber',
                align:'center'
            },{
                header:WtfGlobal.getLocaleText("Account Name"),
                dataIndex:'receivingAccountName',
                align:'center'
            }
        ])) ;
        this.colModelForCimb = new Wtf.grid.ColumnModel([this.sm,
        {
            header:WtfGlobal.getLocaleText("acc.cimb.collectionAccNo"),
            dataIndex:'collectionAccNo',
            align:'center',
            width:130
        },{
            header:WtfGlobal.getLocaleText("acc.cimb.collectionAccName"),
            dataIndex:'collectionAccName',
            align:'center',
            width:130
        },{
            header:WtfGlobal.getLocaleText("acc.cimb.giroBICCode"),
            dataIndex:'giroBICCode',
            align:'center',
            width:130
        },{
            header:WtfGlobal.getLocaleText("acc.cimb.refNo"),
            dataIndex:'refNumber',
            align:'center',
            width:130
        },{
            header:WtfGlobal.getLocaleText("acc.profile.email"),
            dataIndex:'emailForGiro',
            align:'center',
            width:130
        }
        ]);
        
        this.colModelForOCBC = new Wtf.grid.ColumnModel([this.sm, {
                header: WtfGlobal.getLocaleText("acc.ocbcBank.bankCode"),
                dataIndex: 'ocbcBankCode',
                align: 'center',
                width: 130
            }, {
                header: WtfGlobal.getLocaleText("acc.ocbcBank.emplopeeAccountNumber"),
                dataIndex: 'ocbcVendorAccountNumber',
                align: 'center',
                width: 130
            }, {
                header: WtfGlobal.getLocaleText("acc.ocbcBank.ultimateCreditorName"),
                dataIndex: 'ocbcUltimateCreditorName',
                align: 'center',
                width: 130
            }, {
                header: WtfGlobal.getLocaleText("acc.ocbcBank.ultimateDebtorName"),
                dataIndex: 'ocbcUltimateDebtorName',
                align: 'center',
                width: 130
            }, {
                header: WtfGlobal.getLocaleText("acc.ocbcBank.sendRemittanceAdviceVia"),
                dataIndex: 'ocbcSendRemittanceAdviceVia',
                align: 'center',
                width: 130,
                renderer: function (v, m, rec) {
                    if (!v)
                        return "";
                    v = v == "E" ? "Email" : "Fax";
                    return v;
                }
            }, {
                header: WtfGlobal.getLocaleText("acc.ocbcBank.sendDetails++"),
                dataIndex: 'ocbcRemittanceAdviceSendDetails',
                align: 'center',
                width: 130
            }
        ]);
        
        // Transaction Grid creation
    
        this.transactionGrid = new Wtf.grid.GridPanel({
            cm: this.isFromMP ? (this.bankType == Wtf.IBGBanks.DBSBank ? this.colModel : (this.bankType == Wtf.IBGBanks.CIMBBank ? this.colModelForCimb : this.colModelForOCBC)) : this.colModel,
            store:this.gridStore,
            sm:this.sm,
            stripeRows :true,
            border:false,
            viewConfig:{
                emptyText:'<center>'+WtfGlobal.getLocaleText("acc.field.NoRecordToDisplay")+'</center>',
                forceFit:true
            }

        });
        
        this.transactionGrid.getSelectionModel().on('selectionchange', this.rowSelectHandler,this);
        this.transactionGrid.on('render',function(){
            WtfGlobal.autoApplyHeaderQtip(this.transactionGrid);
        },this)
        
        this.createNewButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.rem.138"),
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.field.CreateNewTransaction"),
            iconCls :getButtonIconCls(Wtf.etype.add),
            handler:this.createNewHandler.createDelegate(this)
        });

        this.editButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.edit"),
            scope:this,
            hidden:this.isOrder || this.isFromMP,
            iconCls :getButtonIconCls(Wtf.etype.edit), 
            tooltip:WtfGlobal.getLocaleText("acc.field.EditTransaction"),
            handler:this.editHandler.createDelegate(this,[false])
        });
        this.viewButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.view"),
            scope: this,
//            hidden: !this.isCustomer,
            iconCls: getButtonIconCls(Wtf.etype.reorderreport),
            tooltip: WtfGlobal.getLocaleText("acc.lp.viewccd"),
            handler: this.editHandler.createDelegate(this, [true])
        });

        this.deleteButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.delete"),
            scope:this,
            hidden:this.isOrder || this.isFromMP,
            tooltip:WtfGlobal.getLocaleText("acc.field.DeleteTransaction"),
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            handler:this.deleteHandler.createDelegate(this)
        });
        this.menu = [];
        this.activateBtn = new Wtf.Action({
            text: "Activate",
            scope: this,
            disabled: true,
            tooltip: "Activate Customer Bank Account Type", 
            iconCls: getButtonIconCls(Wtf.etype.activate),
            handler: this.activateDeactivateBankAccount.createDelegate(this, ["activate"])
        });
        this.menu.push(this.activateBtn);
        this.deactivateBtn = new Wtf.Action({
            text: "Deactivate",
            scope: this,
            disabled: true,
            tooltip: "Deactivate Customer Bank Account Type", 
            iconCls: getButtonIconCls(Wtf.etype.deactivate),
            handler: this.activateDeactivateBankAccount.createDelegate(this, ["deactivate"])
        });
        this.menu.push(this.deactivateBtn);
        this.activateDeactivateMenu = new Wtf.Toolbar.Button({
            text: "Activate/ Deactivate",
            scope: this,
            tooltip: "Activate/Deactivate Customer Bank Account Type",
            hidden: !this.isCustomer,
            menu: this.menu
        });
        this.bankStore = new Wtf.data.SimpleStore({
            fields: [{
                name: "id"
            }, {
                name: "name"
            }],
            data: this.isCustomer?[[3, "United Overseas Bank"]]:[[1, "Development Bank Of Singapore"], [2, "Commerce International Merchant Bankers"],[4, "Oversea-Chinese Banking Corporation"]]
        });
        
        this.pmtBank= new Wtf.form.ExtFnComboBox({
        hideLabel:true,
        name:"bankType",
        width:300,
        store:this.bankStore,
        extraFields:[],
        valueField:'id',
        value:this.isCustomer?3:1,
        displayField:'name',
        emptyText:WtfGlobal.getLocaleText("acc.IBG.PleaseSelectBank"),
        mode: 'local',
        triggerAction: 'all',     
        typeAhead: true,
        forceSelection: true,
        hidden:this.isFromMP,
        listeners:{
            'select':{
                fn:function(obj,rec) {
                        this.bankType = obj.getValue();
                        var URL = this.isCustomer ? 'ACCCustomerCMN/getUOBReceivingBankDetails.do' : 'ACCVendorCMN/getIBGReceivingBankDetails.do';
                        if (this.bankType == 2) {
                            URL = 'ACCVendorCMN/getCIMBReceivingBankDetails.do';
                        }
                        if (this.bankType == 4) {
                            URL = 'ACCVendorCMN/getOCBCReceivingBankDetails.do';
                        }
                        this.gridStore.proxy.conn.url = URL;
                        if(this.bankType==1){
                              this.transactionGrid.reconfigure(this.gridStore, this.colModel);
                        }else if(this.bankType==2){
                              this.transactionGrid.reconfigure(this.gridStore, this.colModelForCimb);
                        }else if(this.bankType==4) {
                            this.transactionGrid.reconfigure(this.gridStore, this.colModelForOCBC);
                        }else {
                            this.transactionGrid.reconfigure(this.gridStore, this.colModelForCimb);
                        }
                        this.transactionGrid.getView().refresh(true);
                        this.gridStore.load();
                    },
                scope:this
            }
        }
        });
        
        this.fetchBtn = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.agedPay.fetch"),  //'Fetch',           
            scope: this,
            id:'fetchBtn'+this.id,
            tooltip: WtfGlobal.getLocaleText("acc.agedPay.fetch"), 
            handler: this.fetchData,
            iconCls:'accountingbase fetch',
            hidden:this.isFromMP
        });
    
        // Tbar Item Creation
    
        var buttonArr = [];
        
       
        if (!WtfGlobal.EnableDisable(this.uPermType, this.permType.editibgdetails) && !WtfGlobal.EnableDisable(this.uPermType, this.permType.viewibgdetails)) {
            buttonArr.push(this.createNewButton, this.editButton);
        }
        if (!WtfGlobal.EnableDisable(this.uPermType, this.permType.viewibgdetails)) {
            buttonArr.push(this.viewButton);
        }
        if (!WtfGlobal.EnableDisable(this.uPermType, this.permType.editibgdetails) && !WtfGlobal.EnableDisable(this.uPermType, this.permType.viewibgdetails)) {
            buttonArr.push(this.deleteButton, this.activateDeactivateMenu);
        }
        
        if (!WtfGlobal.EnableDisable(this.uPermType, this.permType.viewibgdetails)) {
            buttonArr.push(this.pmtBank, this.fetchBtn);
        }
        
        var selectBankIndex = (!WtfGlobal.EnableDisable(this.uPermType, this.permType.editibgdetails) && !WtfGlobal.EnableDisable(this.uPermType, this.permType.viewibgdetails)) ? 5 : 1;
        
        if(!this.isFromMP) {
            buttonArr.splice(selectBankIndex,0,'-', WtfGlobal.getLocaleText("acc.IBG.SelectBank"));
        }
        
        this.transactionGridContainerPanel = new Wtf.Panel({
            layout: 'border',
            border: false,
            items:[
                {
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    items: [this.transactionGrid],
                    tbar:buttonArr,
                    bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                        pageSize: 30,
                        id: "pagingtoolbar" + this.id,
                        store: this.gridStore,
                        searchField: this.quickPanelSearch,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                        plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
                    })
                }
            ]
        });
    },
    activateDeactivateBankAccount:function(activateDeactivate){
        var activateBankAccountType = false;
        if(activateDeactivate == "activate"){
            activateBankAccountType = true;
        }
        var rec = this.transactionGrid.getSelectionModel().getSelected();
        if (activateBankAccountType && rec !== undefined && rec.data !== undefined && rec.data.activated) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Selected record is already activated."], 2);
            return;
        } else if (!activateBankAccountType && rec !== undefined && rec.data !== undefined && !rec.data.activated) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Selected record is already deactivated."], 2);
            return;
        }
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"), //"Confirm",
            msg: activateBankAccountType ?"Are you sure you want to activate the selected bank account type?":"Are you sure you want to deactivate the selected bank account type?",
            width: 400,
            buttons: Wtf.MessageBox.OKCANCEL,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope: this,
            fn: function(btn) {
                 Wtf.Ajax.requestEx({
                    url: "ACCCustomerCMN/activateDeactivateUOBBankType.do",
                    params: {
                        detailID: rec.data.UOBReceivingBankDetailId,
                        activateDeactivateFlag: activateBankAccountType
                    }
                }, this, this.genActivateSuccessResponse, this.genActivateFailureResponse);
            }
        });
    },
    genActivateSuccessResponse: function(response) {
        if (response.success) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], 0);
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), response.msg], 2);
        }
        this.gridStore.reload();
    },
    genActivateFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    closeOpenBalanceWin:function(){
        this.close();
    },
    
    saveOpenBalanceWin : function() {
        this.fireEvent("update",this);
        this.close();
    },
    
    rowSelectHandler:function(){
        var rec = this.transactionGrid.getSelectionModel().getSelected();
        
        var smCount = this.transactionGrid.getSelectionModel().getCount();
        if (smCount == 1) {
            if (this.deactivateBtn) {
                this.deactivateBtn.enable();
            }
            if (this.activateBtn) {
                this.activateBtn.enable();
            }
        } else {
            if (this.deactivateBtn) {
                this.deactivateBtn.disable();
            }
            if (this.activateBtn) {
                this.activateBtn.disable();
            }
        }
        if(this.isFromMP && this.transactionGrid.getSelectionModel().hasSelection() == false && (smCount > 1 || smCount == 0)) {
            this.closeButton.disable();
        } else {
            this.closeButton.enable();
        }
    },
    
    editHandler:function(isView){
        var selectedRecordArray = this.transactionGrid.getSelectionModel().getSelections();

        if(selectedRecordArray.length>1 || selectedRecordArray.length<=0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
            return;
        }
        
        var record = "";
        
        if(selectedRecordArray.length == 1){
            record = selectedRecordArray[0];
        }
        if (this.isCustomer && !isView && record !== undefined && record.data !== undefined && !record.data.activated) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), "Cannot edit selected record as it is already deactivated."], 2);
            return;
        }
        /*
         * Check selected IBG details are used in transaction or not.
         */
        if (record != undefined && record != null && this.pmtBank.getValue() == Wtf.IBGBanks.UOBBank && !isView) {
            Wtf.Ajax.requestEx({
                url: 'ACCCustomerCMN/isIBGDetailsUsedInTransaction.do',
                params: {
                    customerBankAccountType: record.get('customerBankAccountType'),
                    customer: this.customerId,
                    isEdit: true
                }
            }, this,
                function (response, req) {
                    if (response.success) {
                        if (response.isIBGDetailsUsedInTransaction) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
                            return;
                        }else{
                            this.callVendorIBGDetailsWindow(record,isView);
                        }
                    }
                }, function (response) {
                if (!response.success) {
                    var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                }
            });
        }else{
            this.callVendorIBGDetailsWindow(record,isView);
        }
    },
    
    callVendorIBGDetailsWindow:function(record,isView){
        this.ibgDetails = new Wtf.account.VendorIBGDetails({
            title: isView ? WtfGlobal.getLocaleText("acc.ibg.receiving.details.view") : WtfGlobal.getLocaleText("acc.ibg.receiving.details.edit"),
            vendorId:this.accRec.get('accid'),
            masterItemId:this.accRec.get('id'),
            id:'editVendorIbgDetails',
            isFromReceivingBankDetailsGrid:true,
            isFromMasterConfiguration:this.isFromMasterConfiguration,
            isEdit:true,
            isView:isView,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            resizable:false,
            editRec:record,
            editRecCIMBbank:this.pmtBank.getValue()==Wtf.IBGBanks.CIMBBank,
            editRecDBSbank:this.pmtBank.getValue()==Wtf.IBGBanks.DBSBank,
            editRecUOBbank:this.pmtBank.getValue()==Wtf.IBGBanks.UOBBank,
            editRecOCBCBank:this.pmtBank.getValue()==Wtf.IBGBanks.OCBCBank,
            customerId:this.customerId,
            layout:'border',
            isCustomer : this.isCustomer,
            modal:true,
            height:500,
            width:550
        });
        
        this.ibgDetails.on('datasaved',function(){
            this.gridStore.reload();
        },this);
        
        this.ibgDetails.show();
        
    },
    
    createNewHandler:function(){
        this.ibgDetails = new Wtf.account.VendorIBGDetails({
            title:WtfGlobal.getLocaleText("acc.ibg.receiving.details"),
            vendorId:this.accRec.get('accid'),
            id:'createVendorIbgDetails',
            masterItemId:this.accRec.get('id'),
            isFromReceivingBankDetailsGrid:true,
            isFromMasterConfiguration:this.isFromMasterConfiguration,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            isCustomer : this.isCustomer,
            customerId:this.customerId,
            resizable:false,
            layout:'border',
            modal:true,
            height:500,
            width:550
        });
        
        this.ibgDetails.on('datasaved',function(ibgForm, ibgFormJsonObj){
            this.gridStore.reload();
        },this);
                    
        this.ibgDetails.show();
    },
    
    deleteHandler:function(){
        var selectedRecordArray = this.transactionGrid.getSelectionModel().getSelections();
        
        if(selectedRecordArray.length>1 || selectedRecordArray.length<=0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
            return;
        }
        
        var record = "";
        
        if(selectedRecordArray.length == 1){
            record = selectedRecordArray[0];
        }
        if(record.data.usedInPayment){  // Restrict user from deleting the detaisl if some payment is created with this detail
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.cimb.entryCanNotBeDeleted")],2);
            return;
        }
        var message = WtfGlobal.getLocaleText("acc.ibg.receiving.delete.confirm");
        
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"),message,function(btn){
            if(btn!="yes") {
                return;
            }
            
            var rec = new Wtf.data.Record({});
            rec.receivingBankDetailId = record.get('ibgId');
            rec.cimbReceivingBankDetailId = record.get('cimbReceivingBankDetailId');
            rec.UOBReceivingBankDetailId = record.get('UOBReceivingBankDetailId');
            rec.customerBankAccountType = record.get('customerBankAccountType');
            rec.customer = this.customerId;
            rec.ocbcIBGDetailId = record.get("ocbcIBGDetailId");
            
            if(this.isFromMasterConfiguration){
                rec.masterItem=this.accRec.get('id');
            }else{
                rec.vendor=this.accRec.get('accid');
            }
            
        
            Wtf.Ajax.requestEx({
                url:this.getURLForDeletingData(),
                
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
    },
    
    genSuccessResponse:function(response, request){
        if(response.success){
            WtfComMsgBox([this.title,response.msg],response.success*2+1);
            this.gridStore.reload();
        }else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg], 2);
        }
    },

    genFailureResponse:function(response){
        //        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    fetchData:function(){
        this.gridStore.load({
            url:this.getURLForGridStore()
        });
    },
    getURLForGridStore: function(){
        var url = 'ACCVendorCMN/getIBGReceivingBankDetails.do';
        if(this.bankType==Wtf.IBGBanks.DBSBank){
            url = 'ACCVendorCMN/getIBGReceivingBankDetails.do';
        } else if(this.bankType==Wtf.IBGBanks.CIMBBank){
            url = 'ACCVendorCMN/getCIMBReceivingBankDetails.do';
        } else if(this.bankType==Wtf.IBGBanks.UOBBank){
            url = 'ACCCustomerCMN/getUOBReceivingBankDetails.do';
        } else if(this.bankType==Wtf.IBGBanks.OCBCBank){
            url = 'ACCVendorCMN/getOCBCReceivingBankDetails.do';
        }
        return url;
    },
    getURLForDeletingData:function(){
        var url = 'ACCVendorCMN/deleteIBGReceivingBankDetails.do';
        if(this.bankType==Wtf.IBGBanks.DBSBank){
            url = 'ACCVendorCMN/deleteIBGReceivingBankDetails.do';
        } else if(this.bankType==Wtf.IBGBanks.CIMBBank){
            url = 'ACCVendorCMN/deleteCIMBReceivingBankDetails.do';
        } else if(this.bankType==Wtf.IBGBanks.UOBBank){
            url = 'ACCCustomerCMN/deleteUOBReceivingBankDetails.do';
        } else if(this.bankType==Wtf.IBGBanks.OCBCBank){
            url = 'ACCVendorCMN/deleteOCBCReceivingBankDetails.do';
        }
        return url;
    }
});
