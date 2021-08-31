
CommonERPComponent= {
    createCustomerPagingComboBox: function (width, listwidth, pageSize, scope, config) {
        var pageSizeConfig=pageSize;
//        if(!CompanyPreferenceChecks.customerVendorPagingCheck()){
//            pageSizeConfig='';
//        }
        var personRec = new Wtf.data.Record.create([
            {
                name: 'accid'
            }, {
                name: 'accname'
            }, {
                name: 'acccode'
            },{
                name: 'taxId'
            },{
                name: 'groupname'
            },{
                name: 'hasAccess'
            }
        ]);
        var customerstore = new Wtf.data.Store({
            url: "ACCCustomer/getCustomersIdNameForCombo.do",
            baseParams: {
                deleted: false,
                nondeleted: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                autoLoad: false,
                totalProperty: 'totalCount'
            },personRec)
        });
        
        
        var comboConfig = {
            fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.cust"),
            hiddenName: 'customerid',
            id: "customer" + scope.id,
            store: customerstore,
            valueField: 'accid',
            displayField: 'accname',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth:listwidth,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            allowBlank: false,
            hirarchical: true,
            emptyText: WtfGlobal.getLocaleText("acc.inv.cus"),
            mode: 'remote',
            pageSize:pageSizeConfig,
            typeAheadDelay:30000,
            minChars:1,
            typeAhead: true,
            forceSelection: true,
            selectOnFocus: true,
            anchor: "50%",
            triggerAction: 'all',
            isCustomer: true,
            scope: scope,
            width:width
        };
        
        /**
         * Added config to customize product combo as per need.
         */
        config = config || {};
        Wtf.apply(comboConfig, config);

        return new Wtf.form.ExtFnComboBox(Wtf.applyIf(comboConfig));
    },
    
    createCustomerMultiselectPagingComboBox: function (scope, store, config) {
        var accComboConfig = {
            fieldLabel: WtfGlobal.getLocaleText("acc.agedPay.searchcus") + '*',
            multiSelect: true,
            valueField: 'accid',
            displayField: 'accname',
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
            extraComparisionField: 'acccode',
            store: store,
            mode: 'remote',
            forceSelection: true,
            selectOnFocus: true,
            triggerAction: 'all',
            isCustomer: true,
            hideLabel: true,
            emptyText: WtfGlobal.getLocaleText("acc.inv.cus"),
            pageSize: Wtf.ProductCombopageSize,
            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 350 : 250,
            width: 240,
            scope: scope
        }
        /**
         * Added config to customize product combo as per need.
         */
        config = config || {};
        Wtf.apply(accComboConfig,config);

        return new Wtf.common.SelectPaging(Wtf.applyIf(accComboConfig));
    },
    commonMultiselectPagingComboBox: function (scope, store, config) {
        var commonComboConfig = {
            multiSelect:true,
            id:"id"+this.id,
            store: store,
            mode: 'remote',
            pageSize:30,
            typeAhead: true,
            forceSelection: true,
            addCreateOpt:true,
            width:240,
            selectOnFocus:true,
            anchor:"50%",
            triggerAction:'all',
            scope:scope
        }
        /**
         * Added config to customize combo as per need.
         */
        config = config || {};
        Wtf.apply(commonComboConfig,config);
    
        return new Wtf.common.SelectPaging(Wtf.applyIf(commonComboConfig));
    },
    
    createVendorPagingComboBox: function (width, listwidth, pageSize, scope, config) {
        var pageSizeConfig=pageSize;
//        if(!CompanyPreferenceChecks.customerVendorPagingCheck()){
//            pageSizeConfig='';
//        }
        var personRec = new Wtf.data.Record.create([
            {
                name: 'accid'
            }, {
                name: 'accname'
            }, {
                name: 'acccode'
            },{
                name: 'taxId'
            },{
                name: 'groupname'
            },{
                name: 'hasAccess'
            }
        ]);
   
        var vendorAccStore = new Wtf.data.Store({
            url: "ACCVendor/getVendorsIdNameForCombo.do",
            baseParams: {
                deleted: false,
                nondeleted: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'totalCount',
                autoLoad: false
            }, personRec)
        });
       
       
        var comboConfig = {
            fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.ven"),
            hiddenName: "vendor",
            id: "vendor" + scope.id,
            store: vendorAccStore,
            valueField: 'accid',
            displayField: 'accname',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth:listwidth,
            allowBlank: false,
            hirarchical: true,
            emptyText: WtfGlobal.getLocaleText("acc.msgbox.19"),
            mode: 'remote',
            pageSize:pageSizeConfig,
            typeAheadDelay:30000,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            minChars:1,
            typeAhead: true,
            forceSelection: true,
            selectOnFocus: true,
            anchor: "50%",
            triggerAction: 'all',
            isVendor: true,
            scope: scope,
            width:width
        };
        
        /**
         * Added config to customize product combo as per need.
         */
        config = config || {};
        Wtf.apply(comboConfig, config);

        return new Wtf.form.ExtFnComboBox(Wtf.applyIf(comboConfig));
    },
    
    createVendorMultiselectPagingComboBox: function (scope, store, config) {
        var accComboConfig = {
            fieldLabel: WtfGlobal.getLocaleText("acc.agedPay.searchcus") + '*',
            multiSelect: true,
            valueField: 'accid',
            displayField: 'accname',
            extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
            extraComparisionField: 'acccode',
            store: store,
            mode: 'remote',
            forceSelection: true,
            selectOnFocus: true,
            triggerAction: 'all',
            isVendor: true,
            hideLabel: true,
            emptyText: WtfGlobal.getLocaleText("acc.inv.ven"),
            pageSize: Wtf.ProductCombopageSize,
            listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 350 : 250,
            width: 240,
            scope: scope
        }
        /**
         * Added config to customize product combo as per need.
         */
        config = config || {};
        Wtf.apply(accComboConfig,config);

        return new Wtf.common.SelectPaging(Wtf.applyIf(accComboConfig));
    },
    
    createCustomerVendorCategoryPagingCombobox:function(width,listwidth,pageSize,scope,isfromCustomerVendor){
        var url="ACCMaster/getMasterItems.do";
        var name='id';
        var fieldlabel= scope.isCustomer ? WtfGlobal.getLocaleText("acc.masterConfig.7") : WtfGlobal.getLocaleText("acc.masterConfig.8");
        var  baseParameters= {
            groupid:scope.isCustomer ? 7 : 8
        }
       
        if(isfromCustomerVendor){//From Customer/ Vendor Report
            name=scope.isCustomer? 'customercategoryview' : 'vendorcategoryview';
            if(scope.isBySalesPersonOrAgent){
                url="ACCMaster/getMasterItems.do";
            }else if(scope.isPricingBandGrouping){
                url="ACCMaster/getPricingBandItems.do";
            }
            
            if(scope.isBySalesPersonOrAgent){
                if(scope.isCustomer){
                    fieldlabel=WtfGlobal.getLocaleText("acc.masterConfig.15");
                }else{
                    fieldlabel=WtfGlobal.getLocaleText("acc.masterConfig.20");
                }
            }else{
                if(scope.isPricingBandGrouping){
                    fieldlabel=WtfGlobal.getLocaleText("acc.field.pricingBandMaster");
                }else{
                    if(scope.isCustomer){
                        fieldlabel=WtfGlobal.getLocaleText("acc.cust.Customercategory");
                    }else{
                        fieldlabel=WtfGlobal.getLocaleText("acc.cust.vendorcategory");
                    }
                }
            }
            
            baseParameters= {
                groupid: scope.isBySalesPersonOrAgent?(scope.isCustomer?15:20):(scope.isCustomer? 7:8)
            }
        }
       
        var custVendCategoryRec = Wtf.data.Record.create([
            {
                name: 'id'
            },
            {
                name: 'name'
            },
        ]);
        var custVendCategoryStore = new Wtf.data.Store({
            url: url,
            baseParams:baseParameters,
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'totalCount'
            }, custVendCategoryRec)
        });
    
        var custVendCategoryCombo = new Wtf.form.ExtFnComboBox({
            fieldLabel:fieldlabel ,
            hiddenName: 'id',
            name: name,
            store: custVendCategoryStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'remote',
            pageSize:pageSize,
            typeAhead: true,
            triggerAction: 'all',
            hideLabel: true,
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectacategory"),
            width:width,
            listWidth:listwidth,
            selectOnFocus:true,
            extraFields:[],
            minChars:1,
            scope:scope,
            hirarchical:true,
            forceSelection:true
        });
        return custVendCategoryCombo;
    },
    
    createProductPagingComboBox:function(width,listwidth,pageSize,scope,baseParams,istransactionFlag,isInventoryFlag,config){
       var pageSizeConfig=pageSize;
       if(!CompanyPreferenceChecks.productPagingCheck()&& (istransactionFlag||isInventoryFlag)){
           pageSizeConfig='';
       }
        var productRec= Wtf.data.Record.create([
            {name: 'productid'},
            {name: 'productname'},
            {name: 'pid'},
            {name: 'type'},
            {name: 'description'},
            {name: 'hasAccess'}
          ]);

        var productStore = new Wtf.data.Store({
            url:"ACCProductCMN/getProductsIdNameforCombo.do",
            baseParams:baseParams,
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'totalCount'
            }, productRec)
        });  
       
        var productComboBox="";
        var comboBoxConfig = {};
        
        if(istransactionFlag){
            comboBoxConfig = {
                name:'pid',
                store:productStore,       //Wtf.productStore Previously, now changed bcos of addition of Inventory Non sale product type
                typeAhead: true,
                selectOnFocus:true,
                isProductCombo: true,
                maxHeight:250,
                listAlign:"bl-tl?",//[ERP-5149] To expand list of combobox always on top. 
                valueField:'productid',//productid
                displayField:'pid',
                extraFields:CompanyPreferenceChecks. productComboDisplay()==Wtf.AccountProducttype ? ['productname','type'] :['productname','description'],
                listWidth:listwidth,
                extraComparisionField:'pid',// type ahead search on acccode as well.
                mode:'remote',
                //editable:false,
                hideTrigger:true,
                scope:scope,
                triggerAction : 'all',
                editable : true,
                minChars : 2,
                hirarchical:true,
                hideAddButton : true,//Added this Flag to hide AddNew  Button  
                addNewFn:scope.openProductWindow.createDelegate(scope),
                forceSelection:true,
                isProductCombo:true,
                pageSize:pageSizeConfig
            };
           
        }else if(isInventoryFlag){
            comboBoxConfig = {
                name:'itemcode',
                store:productStore,    
                typeAhead: true,
                selectOnFocus:true,
                isProductCombo: true,
                valueField:'productid',
                displayField:'pid',
                extraFields:CompanyPreferenceChecks. productComboDisplay()==Wtf.AccountProducttype ? ['productname','type'] :['productname','description'],
                listWidth:listwidth,
                extraComparisionField:'pid',// type ahead search on acccode as well.
                mode:'remote',
                hideTrigger:true,
                scope:scope,
                triggerAction : 'all',
                editable : true,
                minChars : 1,
                hirarchical:true,
                hideAddButton : true,//Added this Flag to hide AddNew  Button  
                forceSelection:true,
                pageSize:pageSizeConfig,
                disabled:scope.isView?true:false
            }
        }else{
            comboBoxConfig = {
                fieldLabel: WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*',
                hiddenName: 'productid',
                name: 'productid',
                hidden: scope.isCustBill,
                store: productStore,
                valueField: 'productid',
                displayField: 'productname',
                mode: 'remote',
                pageSize:pageSizeConfig,
                typeAhead: true,
                triggerAction: 'all',
                hideLabel: true,
                emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
                width:width,
                listWidth:listwidth,
                extraComparisionField:'productname',
                extraComparisionFieldArray:['productname','pid'], //search on both pid and name
                extraFields:CompanyPreferenceChecks. productComboDisplay()==Wtf.AccountProducttype ? ['pid','type'] :['pid','description'],
                isProductCombo: true,
                selectOnFocus:true,
                minChars:1,
                scope:scope,
                hirarchical:true,
                forceSelection:true
            }
        }
        
        /*
         Added config to customize product combo as per need.
          */
        config = config || {};
        Wtf.apply(comboBoxConfig,config);

        productComboBox = new Wtf.form.ExtFnComboBox(comboBoxConfig);
        return productComboBox;
    },
    /*
     *  Common ERP component of multiselection Paging combo for Product Master
     */
    createProductMultiselectPagingComboBox: function (width, listwidth, pageSize, scope, baseParams, config) {
        var pageSizeConfig = pageSize;
        var productRec = Wtf.data.Record.create([
            {name: 'productid'},
            {name: 'productname'},
            {name: 'pid'},
            {name: 'type'},
            {name: 'hasAccess'}
        ]);

        var productStore = new Wtf.data.Store({
            url: "ACCProductCMN/getProductsIdNameforCombo.do",
            baseParams: baseParams,
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'totalCount'
            }, productRec)
        });

        var comboBoxConfig = {
            fieldLabel: WtfGlobal.getLocaleText("acc.productList.gridProduct") + '*',
            hiddenName: 'productid',
            name: 'productid',
            store: productStore,
            valueField: 'productid',
            displayField: 'productname',
            mode: 'remote',
            pageSize: pageSizeConfig,
            triggerAction: 'all',
            emptyText: WtfGlobal.getLocaleText("acc.msgbox.17"),
            width: width,
            listWidth: listwidth,
            extraComparisionField: 'productname',
            extraComparisionFieldArray: ['productname', 'pid'], //search on both pid and name
            isProductCombo: true,
            selectOnFocus: true,
            extraFields: ['pid', 'type'],
            minChars: 1,
            scope: scope,
            forceSelection: true
        }
        /**
         * Added config to customize product combo as per need.
         */
        config = config || {};
        Wtf.apply(comboBoxConfig, config);

        return new Wtf.common.SelectPaging(Wtf.applyIf(comboBoxConfig));
    },
    createProductCategoryPagingComboBox:function(width,listwidth,pageSize,scope){
       
        var productCategoryRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
        ]);
        var productCategoryStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                groupid:19
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'totalCount'
            }, productCategoryRec)
        });
       
        var productCategory = new Wtf.form.ExtFnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.19"),
            hiddenName: 'id',
            name: 'id',
            hidden: scope.isCustBill,
            store: productCategoryStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'remote',
            pageSize:pageSize,
            typeAhead: true,
            triggerAction: 'all',
            hideLabel: true,
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseselectaproductcategory"),
            width:width,
            listWidth:listwidth,
            selectOnFocus:true,
            extraFields:[],
            minChars:1,
            scope:scope,
            hirarchical:true,
            forceSelection:true
        });
        return productCategory;
    }, 
    
    createCreditTermPagingComboBox:function(width,listwidth,pageSize,scope){
       
        var termRec = new Wtf.data.Record.create([
            {name: 'termid'},
            {name: 'termname'},
            {name: 'termdays'}
        ]);

        var termds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'count'
            },termRec),
            url : "ACCTerm/getTerm.do",
            baseParams:{
                mode:91
            }
        });
        
       var  creditTermComboBox = new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Term"),
            hiddenName: 'CreditTerm',
            name: 'CreditTerm',
            hidden: scope.isCustBill,
            store: termds,
            record:Wtf.termRec,
            valueField: 'termid',
            displayField: 'termname',
            mode: 'remote',
            pageSize:pageSize,
            typeAhead: true,
            triggerAction: 'all',
            hideLabel: true,
            minChars:1,
            emptyText: WtfGlobal.getLocaleText("acc.field.SelectaTerm"),
            width:width,
            listWidth:listwidth,
            selectOnFocus:true,
            extraFields:[],
            minChars:1,
            scope:scope,
            hirarchical:true,
            forceSelection:true
        });
        return creditTermComboBox;
    },
    
    createCostCenterPagingComboBox:function(width,listwidth,pageSize,scope){
        var CostCenterRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'ccid'},
            {name: 'name'},
            {name: 'description'}
        ]);
        var CostCenterStore=new Wtf.data.Store({
            url: "CostCenter/getCostCenter.do?forCombo=report",
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'count'
            },CostCenterRec)
        });
        
        var  costCenter = new Wtf.form.ExtFnComboBox({
            name:'costCenterId',
            hidden: scope.isCustBill,
            store: CostCenterStore,
            displayField:'name',
            valueField:'id',
            mode: 'remote',
            pageSize:pageSize,
            typeAhead: true,
            triggerAction: 'all',
            hideLabel: true,
            width:width,
            emptyText:WtfGlobal.getLocaleText("acc.rem.9"),  //"Select a Cost Center"
            listWidth:listwidth,
            selectOnFocus:true,
            extraFields:[],
            minChars:1,
            scope:scope,
            hirarchical:true,
            forceSelection:true
        });
        return costCenter;
    },
    
    createAccountPagingComboBox: function (scope, width, listwidth, maxHeight, pageSize, extraFields, baseParams, config,ajaxUrl) {
        var comboUrl="ACCAccountCMN/getAccountsForJE.do";
        if(ajaxUrl!=undefined && ajaxUrl!=null && ajaxUrl!=""){
            comboUrl=ajaxUrl;
        }
        var accRec = Wtf.data.Record.create([
            {name: 'accountid', mapping: 'accid'},
            {name: 'acccode'},
            {name: 'accountname', mapping: 'accname'},
            {name: 'mappedaccountid'},
            {name: 'groupname'},
            {name: 'masterTypeValue'},
            {name: 'mastertypevalue'},
            {name: 'currencysymbol'},
            {name: 'currencyid'},
            {name: 'hasAccess'},
            {name: 'usedIn'},
            {name: 'haveToPostJe', type: 'boolean'},
            {name: 'isOneToManyTypeOfTaxAccount'},
            {name: 'appliedGst'},
            {name: 'accountpersontype'}
        ]);
    
        var accountStore = new Wtf.data.Store({
            url: comboUrl,
            baseParams: baseParams,
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'totalCount'
            }, accRec)
        });
        
        var accComboConfig = {
            hiddenName: 'accountid',
            store: accountStore,
            valueField: 'accountid',
            mode: 'remote',
            pageSize:pageSize,
            typeAheadDelay: 30000,
            minChars: 1,
            displayField: 'accountname',
            extraComparisionField: 'acccode', // type ahead search on acccode as well.
            extraFields: extraFields,
            listWidth: listwidth,
            width: width,
            maxHeight: maxHeight,
            isAccountCombo: true,
            typeAhead: true,
            selectOnFocus: true,
            scope: scope,
            hirarchical: true,
            forceSelection: true,
            lazyInit: false
        };
    
        /*
         Added config to customize product combo as per need.
          */
        config = config || {};
        Wtf.apply(accComboConfig,config);

        return new Wtf.form.ExtFnComboBox(accComboConfig);
    },
    
    addInvoiceTermGrid : function(scope) {
        
        scope.selectedTaxPercentage = 0.0;
        
        var linkedTaxRec = new Wtf.data.Record.create([
            {name: 'termtax'},
            {name: 'linkedtaxname'},
            {name: 'linkedtaxpercentage'},
            {name: 'hasAccess'}
        ]);
        
        scope.linkedTaxStore=new Wtf.data.Store({
            url: 'ACCAccount/getLinkedTermTax.do',
            baseParams:{
                isSalesOrPurchase:scope.isCustomer?true:false,
                includeDeactivatedTax : scope.includeDeactivatedTax
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'count'
            },linkedTaxRec)
        });
        
        scope.linkedTaxDummyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'count'
            },linkedTaxRec)
        });
        
        scope.linkedTaxCombo = new Wtf.form.ExtFnComboBox({
            store:scope.linkedTaxStore,
            displayField:'linkedtaxname',
            valueField:'termtax',
            extraFields:[],
            listWidth: 50,
            mode: 'remote',
            isTax:true,
            listeners: {
                //ERP-40242 User should not able to select deactivated taxes.
                'beforeselect': {
                    fn: function (combo, record, index) {
                        return validateSelection(combo, record, index);
                    },
                    scope: this
                }
            }
        });
        
        var termcm = new Wtf.grid.ColumnModel([
            {
                header: WtfGlobal.getLocaleText("acc.field.Term"),
                dataIndex: 'term'
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.TermPercentage"),
                dataIndex: 'termpercentage',
                editor:new Wtf.form.NumberField({
                    xtype : "numberfield", 
                    maxLength : 15,
                    allowNegative : false,
                    minValue : 0,
                    maxValue: 100,
                    regexText:Wtf.MaxLengthText+"15"
                })
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.TermAmount"),
                dataIndex: 'termamount',
                renderer : function(val, meta, rec) {
                    if(typeof val=='number' && val>=0 && rec.data.sign==0) {
                        rec.set('termamount',val*(-1));
                        return val*(-1);
                    } else { 
                        return val;
                    }
                },
                editor:new Wtf.form.NumberField({
                    xtype : "numberfield", 
                    maxLength : 15,
                    allowNegative : true,
                    regexText:Wtf.MaxLengthText+"15"
                })
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.linkedTermTax"),
                dataIndex: 'termtax',
                hidden: true,
                renderer: !scope.readOnly ? function (value) {
                            var rec  = WtfGlobal.searchRecord(scope.linkedTaxDummyStore,value,scope.linkedTaxCombo.valueField);
                            return (rec != null && rec != undefined ) ? (rec.data[scope.linkedTaxCombo.displayField] || "" ) : "";
                        }.createDelegate(scope) : "",
                editor: !scope.readOnly ? scope.linkedTaxCombo : ""
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.termTaxAmount"),
                dataIndex: 'termtaxamount',
                hidden: true
            },
            {
                header: WtfGlobal.getLocaleText("acc.field.termAmountExcludingTax"),
                dataIndex: 'termAmountExcludingTax',
                hidden: true
            }
        ]);
                
        var termRec =new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'term'},
            {name: 'glaccount'},
            {name: 'sign'},
            {name: 'formula'},
            {name: 'formulaids'},
            {name: 'termpercentage'},
            {name: 'termtax'},
            {name: 'termamount'},
            {name: 'termamountinbase'},
            {name: 'termAmountExcludingTax'},
            {name: 'termAmountExcludingTaxInBase'},
            {name: 'termtaxamount'},
            {name: 'termtaxamountinbase'},
            {name: 'includeInTDSCalculation'},
            {name: 'linkedtaxpercentage'},
            {name: 'isTermActive'},
            {name: 'linkedTaxMapping'}
        ]);
        
        scope.termStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, termRec),
            url: 'ACCAccount/getInvoiceTermsSales.do',
            baseParams:{
                isSalesOrPurchase:scope.isCustomer?true:false,
                showActiveDeactiveTerms:scope.isEdit,
                isCopy:scope.moduleid==Wtf.Acc_Invoice_ModuleId||scope.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId||scope.moduleid==Wtf.Acc_FixedAssets_DisposalInvoice_ModuleId||scope.moduleid==Wtf.Acc_FixedAssets_PurchaseInvoice_ModuleId||scope.moduleid==Wtf.Acc_Purchase_Order_ModuleId ||scope.moduleid==Wtf.Acc_Purchase_Return_ModuleId||scope.moduleid==Wtf.Acc_Sales_Order_ModuleId||scope.moduleid==Wtf.Acc_Goods_Receipt_ModuleId||scope.moduleid==Wtf.Acc_Delivery_Order_ModuleId?scope.copyInv: scope.isCopy
            }
        });
        
        scope.termgrid = new Wtf.grid.EditorGridPanel({
            clicksToEdit:1,
            store: scope.termStore,
            height:100,
            autoScroll : true,
            disabledClass:"newtripcmbss",
            cm: termcm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        
        scope.termStore.load({
            scope: scope,
            callback: function() {
                if(scope.termStore.data.length==0){
                    scope.termgrid.hide();
                }
            }
        });
        /*
         *On 'check' event of applyTaxToTerm we Hide and Show Linked Tax Name and Term Tax Amount column ==> scope.HideShowColFromInvoiceTermGrid(val).
         *If "Auto Load Term Taxes" is true from company preferences in this case  we are going to set Linked Tax to Invoice Term,
         *Only when Invoice Term is mapped to single tax. ==> scope.setSingleMappedTaxToInvoiceTerm(val).
         *We are resetting 'Linked Tax Name' and 'Term Tax Amount' values on uncheck(false) of Apply Tax To Term ==> scope.resetLinkedTaxNameAndTermTaxAmount(val).
         */
        scope.applyTaxToTermsChk.on('check', function (obj, val) {
            if(val || scope.isTaxable.getValue()){
                scope.HideShowLinkedTermTaxAndTermTaxAmountCol(true);
                if(scope.includingGST.getValue()){
                    scope.HideShowTermAmountExcludingTaxCol(true);
                }else{
                    scope.HideShowTermAmountExcludingTaxCol(false);
                }
            }else{
                scope.HideShowLinkedTermTaxAndTermTaxAmountCol(false);
                scope.HideShowTermAmountExcludingTaxCol(false);
            }
            if(!scope.readOnly){
                scope.setSingleMappedTaxToInvoiceTerm(val);
            }
            if(!val && !scope.isTaxable.getValue()){
               scope.resetLinkedTaxNameAndTermTaxAmount(val); 
            }
            scope.updateSubtotal();
        }, scope);
        
        /*
         *On 'beforeselect' added record in dummy store to keep selected tax as it is in column.
         **/
        scope.linkedTaxCombo.on('beforeselect', function(combo,record,index) {
            if(record != null && record != undefined && record.data != undefined){
                scope.linkedTaxDummyStore.add(record);
                scope.selectedTaxPercentage = record.data.linkedtaxpercentage;
            }
        });
        
        scope.termgrid.on('afteredit',function(obj) {
            if(obj.field=='termamount') {
                obj.record.set('termpercentage','');
            } else if(obj.field=='termpercentage' && obj.value==0) {
                obj.record.set('termpercentage','');
            } else if(obj.field=='termtax'){
                obj.record.set('linkedtaxpercentage', scope.selectedTaxPercentage);   
            }
            scope.updateSubtotalOnTermChange(true,(obj.field=='termamount'),obj.record.data.termamount,obj.row);
            scope.selectedTaxPercentage = 0.0;
        }, scope);
        
        scope.termgrid.on('cellclick',function(grid, rowIndex, columnIndex, e) {
            var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name
            if(scope.isViewTemplate || (scope.isLinkedTransaction && scope.isEdit)){
                if(fieldName=='termamount' || fieldName=='termpercentage' || fieldName=='termtaxamount' || fieldName=='termtax') {
                    return false;
                }
          }
        },scope);
        
        scope.termgrid.on('beforeedit', function(obj) {
            /*
             * If term is deactivated user wil not be allowed to edit values 
             */
            if(!obj.record.data.isTermActive){
                obj.cancel=true;
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"<b> "+obj.record.data.term+"</b> "+WtfGlobal.getLocaleText("acc.master.invoiceterm.canNotUpdateTermAmount")],2);
            }else{
            if (obj.field == 'termtax') {
                var column = obj.grid.colModel.config[obj.column];
                if (column) {
                    column.editor.field.store.baseParams.termid = obj.record.data.id;
                }
            }
            }
        }, scope);
        
        /*For Hide and Show Term Amount Excluding Tax column in Including GST case only*/
        scope.HideShowTermAmountExcludingTaxCol = function (val) {
            if (scope.termgrid.getColumnModel().findColumnIndex("termAmountExcludingTax") != -1) {
                scope.termgrid.getColumnModel().setHidden(scope.termgrid.getColumnModel().findColumnIndex("termAmountExcludingTax"), !val);
            }
        }
        /*For Hide and Show Linked Tax Name and Term Tax Amount column*/
        scope.HideShowLinkedTermTaxAndTermTaxAmountCol = function (val) {
            if (scope.termgrid.getColumnModel().findColumnIndex("termtax") != -1) {
                scope.termgrid.getColumnModel().setHidden(scope.termgrid.getColumnModel().findColumnIndex("termtax"), !val);
            }
            if (scope.termgrid.getColumnModel().findColumnIndex("termtaxamount") != -1) {
                scope.termgrid.getColumnModel().setHidden(scope.termgrid.getColumnModel().findColumnIndex("termtaxamount"), !val);
            }
        }
        
        /*
        *If "Auto Load Invoice Term Taxes" is true from company preferences in this case  we are going to set Linked Tax to Invoice Term,
        *Only when Invoice Term is mapped to single tax.
        */
        scope.setSingleMappedTaxToInvoiceTerm = function (val) {
            if(CompanyPreferenceChecks.autoLoadInvoiceTermTaxes() && val){
                scope.termStore.each(function(rec){
                    if(rec != undefined && rec != null && rec.data != undefined){
                        var termData = rec.data;
                        var linkedTaxMappingArray = termData.linkedTaxMapping;
                        
                        if(linkedTaxMappingArray != undefined && linkedTaxMappingArray.count == 1 && linkedTaxMappingArray.data != undefined){
                            scope.linkedTaxDummyStore.insert(0, new scope.linkedTaxDummyStore.recordType({
                                termtax:linkedTaxMappingArray.data[0].termtax,
                                linkedtaxname:linkedTaxMappingArray.data[0].linkedtaxname,
                                linkedtaxpercentage:linkedTaxMappingArray.data[0].linkedtaxpercentage
                            }));
                            rec.set('linkedtaxpercentage',linkedTaxMappingArray.data[0].linkedtaxpercentage);
                            rec.set('termtax',linkedTaxMappingArray.data[0].termtax);
                        }
                    }
                });
            }
        }
        
        /*
         *We are resetting 'Linked Tax Name' and 'Term Tax Amount' values on uncheck(false) of Apply Tax To Term / uncheck of Including GST / 'Yes to No' of Global level tax.
         **/
        scope.resetLinkedTaxNameAndTermTaxAmount = function (val) {
            if(!val){
                scope.termStore.each(function(rec){
                    if(rec != undefined && rec.data != undefined){
                        var termData = rec.data;
                        if(termData.termtax != undefined && termData.termtaxamount != 0 && termData.termtaxamountinbase != 0 && termData.termtaxamount != "" && termData.termtaxamountinbase != ""){
                            rec.set('termtaxamount',0);
                            rec.set('termtaxamountinbase',0);
                            rec.set('linkedtaxpercentage',0);
                            rec.set('termtax','');
                        }
                    }
                });
            }
        }
                
        return scope.termgrid;
    },
    createStorePagingComboBox:function(scope,width,listwidth,pageSize,comboName,displayField,ajaxUrl,params,labelStyleConfigs,fieldlabel,allowBlankFlag){
        var inventoryStoreRec = new Wtf.data.Record.create([
            {name: 'storeid'},
            {name: 'storeName'},
            {name: 'abbr'},
            {name: 'fullname'},
            {name: 'defaultlocationid'},
            {name: 'defaultlocation'}
        ]);
        var inventoryStore=new Wtf.data.Store({
            url: ajaxUrl,
            baseParams:params,
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'count'
            },inventoryStoreRec)
        });
        
        var inventoryStoreCombo = new Wtf.form.ExtFnComboBox({
            name:comboName,
            fieldLabel:fieldlabel,
            store: inventoryStore,
            displayField:displayField,
            valueField:'storeid',
            mode: 'remote',
            pageSize:pageSize,
            typeAhead: true,
            labelStyle:labelStyleConfigs,
            triggerAction: 'all',
            width:width,
            emptyText:WtfGlobal.getLocaleText("acc.pos.store.emptyMsg"),  //"Select a Cost Center"
            listWidth:listwidth,
            selectOnFocus:true,
            allowBlank: allowBlankFlag,
            extraFields:[],
            minChars:1,
            scope:scope,
            hirarchical:true,
            forceSelection:true
        });
        return inventoryStoreCombo;
    },
    
    createPaymentMethodMultiPagingComboBox: function (scope,width,listwidth,pageSize,fieldlabel, store, config,displayField,ajaxUrl,params,labelStyleConfigs,allowBlankFlag) {
               var pmtRec = new Wtf.data.Record.create([
                {name: 'methodid'},
                {name: 'methodname'},
                {name: 'accountid'},
                {name: 'acccurrency'},
                {name: 'accountname'},
                {name: 'isIBGBankAccount', type:'boolean'},
                {name: 'isdefault'},
                {name: 'detailtype',type:'int'},
                {name: 'acccustminbudget'},
                {name: 'autopopulate'},
            ]);
            
        var pmtStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },pmtRec),
            url : ajaxUrl,
            baseParams:params
        });   
       
       
       var accComboConfig = {
            fieldLabel:fieldlabel,
            multiSelect: true,
            valueField:'methodid',
            displayField:displayField,
            labelStyle:labelStyleConfigs,
            extraFields: [],
            extraComparisionField: '',
            store: pmtStore,
            mode: 'remote',
            forceSelection: true,
            selectOnFocus: true,
            triggerAction: 'all',
            emptyText:'',
            pageSize: pageSize,
            listWidth:listwidth,
            width: width,
            allowBlank: allowBlankFlag,
            scope: scope
        }
        /**
         * Added config to customize product combo as per need.
         */
        config = config || {};
        Wtf.apply(accComboConfig,config);

        return new Wtf.common.SelectPaging(Wtf.applyIf(accComboConfig));
    },
    createPaymentMethodPagingComboBox:function(scope,width,listwidth,pageSize,extraFields,comboName,displayField,ajaxUrl,params,labelStyleConfigs,fieldlabel,allowBlankFlag){
        var pmtRec = new Wtf.data.Record.create([
                {name: 'methodid'},
                {name: 'methodname'},
                {name: 'accountid'},
                {name: 'acccurrency'},
                {name: 'accountname'},
                {name: 'isIBGBankAccount', type:'boolean'},
                {name: 'isdefault'},
                {name: 'detailtype',type:'int'},
                {name: 'acccustminbudget'},
                {name: 'autopopulate'},
            ]);
            
        var pmtStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },pmtRec),
            url : ajaxUrl,
            baseParams:params
        });       
        var paymentmethodCombo= new Wtf.form.ExtFnComboBox({
            fieldLabel:fieldlabel,
            name:comboName,
            labelStyle:labelStyleConfigs,
            store:pmtStore,
            valueField:'methodid',
            displayField:displayField,
            pageSize:pageSize,
            emptyText:'',
            width:width,
            mode: 'remote',
            triggerAction: 'all',
            typeAhead: true,
            listWidth:listwidth,
            selectOnFocus:true,
            allowBlank: allowBlankFlag,
            extraFields:extraFields,
            scope:scope,
            hirarchical:true,
            forceSelection: true
        });
        return paymentmethodCombo;
    },
    
     createSequenceFormatPagingComboBox:function(scope,width,listwidth,pageSize,extraFields,comboName,modeName,labelStyleConfigs,fieldlabel,allowBlankFlag,multiselect){
         var sequenceFormatStoreRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'value'
        },
        {
            name: 'oldflag'
        }
        ]);
        var sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },sequenceFormatStoreRec),
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:modeName,
                isEdit: false //If from shortfall need edit false
            }
        });    
        var seqFormatCombo= new Wtf.form.ExtFnComboBox({
            fieldLabel:fieldlabel,
            name:comboName,
            labelStyle:labelStyleConfigs,
            store:sequenceFormatStore,
            multiSelect: multiselect,
            valueField:'id',
            anchor:'85%',
            displayField:'value',
            pageSize:pageSize,
            emptyText:'Select Sequence Format',
            width:width,
            mode: 'remote',
            triggerAction: 'all',
            typeAhead: true,
            listWidth:listwidth,
            selectOnFocus:true,
            allowBlank: allowBlankFlag,
            extraFields:extraFields,
            scope:scope,
            hirarchical:true,
            forceSelection: true
        });
        return seqFormatCombo;
    },
   createSalesPersonAgentComboBox: function (width, listwidth, pageSizeConfig, scope, hiddenConfig,hideLabelConfig) {

//        var salesPersonRec=new Wtf.data.Record.create([
//        {
//            name: 'id'
//        },
//        {
//            name: 'name'
//        },{
//            name: 'userid'
//        },
//        {
//            name:'hasAccess'
//        }
//        ]
//        );
       var salesPersonArray =[
            {
                name: 'id'
            },
            {
                name: 'name'
            },
            {
                name:'hasAccess'
            }];  
        
       if(scope.isCustomer){
            salesPersonArray.push({
            name: 'userid'
        });
       }
       var salesPersonRec=new Wtf.data.Record.create(salesPersonArray);
   
        var salesPersonAgentFilteredByCustomerStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'totalCount'
            }, salesPersonRec),
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                groupid: 15
            }
        });
        
        //Purchase Side----Agent  
        if(!scope.isCustomer){
            salesPersonAgentFilteredByCustomerStore= new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                },Wtf.agentRec),
                url:"ACCMaster/getMasterItems.do",
                baseParams:{
                    groupid:20
                }
            });
        }
        
        var salespersonAgentCombo=new Wtf.form.ExtFnComboBox({
            triggerAction:'all',
            mode:'remote',
            valueField:'id',
            displayField:'name',
            extraFields:[],//it is required when  ExtFnComboBox component
//            id:"salesperson"+scope.heplmodeid+scope.id,
            store:salesPersonAgentFilteredByCustomerStore,
            addNoneRecord: true,
            hidden:hiddenConfig,
            hideLabel:hideLabelConfig,
            width : width,
            listWidth:listwidth,
            pageSize:pageSizeConfig,
            //            typeAhead: true,
            forceSelection: true,
            fieldLabel: scope.isCustomer ? WtfGlobal.getLocaleText("acc.masterConfig.15") : WtfGlobal.getLocaleText("acc.masterConfig.20"),
            emptyText: scope.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectSalesPerson") : WtfGlobal.getLocaleText("acc.field.SelectAgent"),
            name:scope.isCustomer ? 'salesPerson' : 'agent',
            hiddenName:scope.isCustomer ? 'salesPerson' : 'agent',
            activated:scope.isCustomer ? true : false
        });
        return salespersonAgentCombo;
    }
};
    
