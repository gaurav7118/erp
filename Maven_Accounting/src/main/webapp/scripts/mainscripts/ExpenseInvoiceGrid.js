/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

Wtf.account.ExpenseInvoiceGrid=function(config){
    config.enableColumnMove = !config.readOnly,
    config.enableColumnResize = !config.readOnly,
    this.isCustomer=config.isCustomer;
    this.isCustBill=config.isCustBill;
    this.id=config.id;
    this.heplmodeid=config.heplmodeid;
    this.fromPO=false;
    this.editTransaction=config.editTransaction;
    this.isLinkedTransaction= (config.isLinkedTransaction == null || config.isLinkedTransaction == undefined)? false : config.isLinkedTransaction;
    this.readOnly=config.readOnly;
    this.isOrder=config.isOrder;
    this.noteTemp=config.noteTemp;
    this.fromOrder=config.fromOrder;
    this.moduleid=config.moduleid;
    this.forCurrency="";
    this.isTemplate = config.isTemplate;
    if(config.isNote!=undefined)
        this.isNote=config.isNote;
    else
        this.isNote=false;
    this.isCN=config.isCN;
    this.sModel = new Wtf.grid.CheckboxSelectionModel({
        singleSelect :false
    }); 
    
    this.sModel.on('selectionchange',function(){this.fireEvent('onselection',this);},this);
    this.sModel.on("beforerowselect", this.checkSelections,this);
    this.createStore();
    this.createComboEditor();
    this.createColumnModel();
    var colModelArray = [];
    colModelArray = GlobalColumnModel[this.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray,this.store);
    Wtf.account.ExpenseInvoiceGrid.superclass.constructor.call(this,config);
    WtfGlobal.getGridConfig(this, this.moduleid + "_" + Wtf.Expense_Grid_ModuleId, true, true);
    this.on('populateDimensionValue',this.populateDimensionValueingrid,this);
    this.addEvents({
         'datachanged':true,
         'onselection':true,
         'gridconfigloaded':true    // Event fire when WtfGlobal.getGridConfig() called and config get applied to grid column
    });
}
Wtf.extend(Wtf.account.ExpenseInvoiceGrid,Wtf.grid.EditorGridPanel,{
    clicksToEdit:1,
    stripeRows :true,
    layout:'fit',
    autoScroll:true,
    viewConfig:{forceFit:true},
    disabledClass:"newtripcmbss",
    forceFit:true,
    loadMask:true,
    onRender:function(config){
        Wtf.account.ExpenseInvoiceGrid.superclass.onRender.call(this,config);
        this.on('render',this.addBlankRow,this);
        this.on('activate',function(){
            this.accountStore.load();
        },this);

        //         this.on('validateedit',this.checkRow,this);
        this.on('afteredit',this.updateRow,this);
        this.on('rowclick',this.handleRowClick,this);
        this.on('cellclick',this.RitchTextBoxSetting,this);
        this.on('columnmove', this.saveGridStateHandler, this);
        this.on('columnresize', this.saveGridStateHandler, this);
         
        this.on('beforeedit',function(obj){
            if(this.isLinkedTransaction && (obj.field == "accountid" || obj.field == "debit" || obj.field == "rateIncludingGstEx" || obj.field == "rate" || obj.field == "prdiscount" || obj.field=="taxamount" || obj.field=="prtaxid" || obj.field=="discountispercent")){
               obj.cancel=true;
            }
            if(obj.field == "taxamount" && this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue()){
                obj.cancel=true;
            }
            
            if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && Wtf.account.companyAccountPref.countryid == Wtf.CountryID.INDIA && Wtf.account.companyAccountPref.isActiveLandingCostOfItem) {
                if (this.parentObj != undefined && this.parentObj.landingCostCategoryCombo != undefined) {
                    var rec = WtfGlobal.searchRecord(this.parentObj.landingCostCategoryCombo.store, this.parentObj.landingCostCategoryCombo.getValue(), 'id');
                    if (rec != undefined && rec.data.allocationtype != undefined) {
                        /*
                         * If ALLOCATION_TYPE = CUSTOM_DUTY AND COUNTRY = INDIA then don't allow user to add/edit any field which will update cost. 
                         **/
                        if (rec.data.allocationtype == Wtf.landingCostAllocation.CUSTOMDUTY && (obj.field == "accountid" || obj.field == "debit" || obj.field == "rateIncludingGstEx" || obj.field == "rate" || obj.field == "prdiscount" || obj.field == "taxamount" || obj.field == "prtaxid" || obj.field == "discountispercent")) {
                            obj.cancel = true;
                            return
                        }
                    }
                }
            }
        },this);

        if (this.parentObj && this.parentObj.Currency != undefined) {
            this.forCurrency = this.parentObj.Currency.getValue();
        }
        if(this.isLinkedTransaction){
            this.cmbAccount.setDisabled(true);
            this.cmbType.setDisabled(true);
            this.editprice.setDisabled(true);
            this.editPriceIncludingGST.setDisabled(true);
            this.rowDiscountTypeCmb.setDisabled(true);
            this.Discount.setDisabled(true);
            this.transTax.setDisabled(true);
            this.transTaxAmount.setDisabled(true);
        }
        this.hideShowCustomizeLineFields();
    },
    saveGridStateHandler: function () {
        var grid = this;
        var state = grid.getState();
        if (!this.readOnly) {
            WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid + "_" + Wtf.Expense_Grid_ModuleId, this.gridConfigId, true);
        }
    },
     createStore:function(){
        this.storeRec = Wtf.data.Record.create([
            {name:'rowid'},
            {name:'accountid'},
            {name:'billid'},
            {name:'billno'},
            {name:'rate'},
            {name:'discamount'},
            {name: 'debit' , defValue:true},
            {name:'discount'},
            {name:'prdiscount'},
            {name:'prtaxid'},
            {name:'prtaxname'},
            {name:'prtaxpercent'},
            {name:'customfield'},
            {name:'taxamount'},
            {name:'calamount'},
            {name:'discountispercent',defValue:1},
            {name:'discountamount'},
            {name: 'currencysymbol'},
            {name:'taxpercent'},
            {name: 'accountid'},
            {name: 'accountname'},
            {name:'desc'},
            {name:'rateIncludingGstEx'},
            {name:'gstCurrencyRate',defValue:'0.0'},
            {name:'totalamount'},
            {name:'amount'},
            {name:'transectionno'},
            {name:'orignalamount'},
            {name:'lineleveltermamount',defValue:0},
            {name:'typeid'},
            {name:'rowTaxAmount'},
            {name:'isNewRecord'},
            {name:'linkto'},
            {name:'linkid'},
            {name:'billblockstatus'},
            {name:'linktype'},
            {name:'savedrowid'},
            {name:'manualLandedCostCategory'},
            {name: 'srno', isForSequence:true},
            {name:'appliedTDS', type:'string'},// Contain TDS details
            {name: 'tdsjemappingID'},
            {name:'tdsamount', defValue:0.0},// TDS amount at line level
            {name:'isUserModifiedTaxAmount', defValue:false},
            {name:'balanceAmount'}
            
        ]);
        var url=Wtf.req.account+((this.fromOrder||this.readOnly)?((this.isCustomer)?'CustomerManager.jsp':'VendorManager.jsp'):((this.isCN)?'CustomerManager.jsp':'VendorManager.jsp'));
        if(this.fromOrder)
           url=Wtf.req.account+(this.isCustomer?'CustomerManager.jsp':'VendorManager.jsp');
        this.store = new Wtf.data.Store({
              url:url,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.storeRec)
        });
        this.accRec = Wtf.data.Record.create([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'},
            {name:'desc',mapping:'accdesc'},
            {name:'level',type:'int'},
            {name:'acccode'},
            {name:'groupname'},
            {name: 'acctaxcode'},
            {name: 'hasAccess'},
            {name: 'haveToPostJe'},
            {name: 'usedIn'}
        ]);
        this.accountStore = new Wtf.data.Store({
//            url:"ACCAccountCMN/getAccountsForCombo.do",
//            url: Wtf.req.account+'CompanyManager.jsp',
              url: "ACCAccountCMN/getAccountsIdNameForCombo.do",
            baseParams:{
                mode:2,
                deleted:false,
                ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true,
                controlAccounts:true  // Added to load Control Accounts
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
//        this.accountStore.load();
    },
    createComboEditor:function(){
        this.typeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid',type:'int'}, 'name'],
            data :[[0,'Normal'],[1,'Defective']]
        });
        this.typeEditor = new Wtf.form.ComboBox({
            store: this.typeStore,
            name:'typeid',
            displayField:'name',
            valueField:'typeid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });
        this.taxRec = new Wtf.data.Record.create([
           {name: 'prtaxid',mapping:'taxid'},
           {name: 'prtaxname',mapping:'taxname'},
           {name: 'percent',type:'float'},
           {name: 'taxcode'},
           {name: 'accountid'},
           {name: 'accountname'},
           {name: 'applydate', type:'date'},
           {name: 'hasAccess'},
           {name: 'termid'}

        ]);
        this.taxStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.taxRec),
    //        url: Wtf.req.account + 'CompanyManager.jsp',
            url : "ACCAccountCMN/getTax.do",
            baseParams:{
                mode:33,
                moduleid :this.moduleid,
                includeDeactivatedTax: this.isEdit!=undefined? this.isEdit : false
            }
        });
        
        this.typeStore = new Wtf.data.SimpleStore({
            fields: [{
                name: "id"
            }, {
                name: "name"
            }],
            data: [[true, "Debit"], [false, "Credit"]]
        });
        this.taxStore.load();
        
         this.rowDiscountTypeStore = new Wtf.data.SimpleStore({
            fields: [{name:'typeid',type:'int'}, 'name'],
            data :[[1,'Percentage'],[0,'Flat']]
        });
        this.rowDiscountTypeCmb = new Wtf.form.ComboBox({
            store: this.rowDiscountTypeStore,
            name:'typeid',
            displayField:'name',
            valueField:'typeid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });
        this.editPriceIncludingGST=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            decimalPrecision:Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL,    
            maxLength:14
        });
        this.transTax= new Wtf.form.ExtFnComboBox({
            hiddenName:'prtaxid',
            anchor: '100%',
            store:this.taxStore,
            valueField:'prtaxid',
            forceSelection: true,
            displayField:'prtaxname',
//            addNewFn:this.addTax.createDelegate(this),
            scope:this,
            selectOnFocus:true,
            extraFields: [],
            isTax: true,
            listeners: {
                'beforeselect': {
                    fn: function (combo, record, index) {
                        return validateSelection(combo, record, index);
                    },
                    scope: this
                }
            }
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.edit))
            this.transTax.addNewFn=this.addTax.createDelegate(this);
        this.editprice=new Wtf.form.NumberField({
            maxLength:10,
            maskRe:/[0-9.]/
        });
        this.cmbType = new Wtf.form.ComboBox({
                hiddenName: 'debit',
                store: this.typeStore,
                valueField: 'id',
                displayField: 'name',
                mode: 'local',
                triggerAction: 'all',
                forceSelection: true
            });
        this.Discount=new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative: false,
            defaultValue:0
//            maxValue:100
        });
        this.Description= new Wtf.form.TextArea({
            name:'description'
//            readOnly:true
        });
        this.cmbAccount=new Wtf.form.ExtFnComboBox({
                    hiddenName:'accountid',
                    store:this.accountStore,
                    valueField:'accountid',
                    displayField:'accountname',
                    forceSelection:true,
                    isAccountCombo:true,
                    minChars:1,
                    mode: 'local',
                    typeAheadDelay:30000,
                    extraComparisionField:'acccode',// type ahead search on acccode as well.
                    hirarchical:true,
                    addNewFn:this.openCOAWindow.createDelegate(this),
                    extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
                    listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400
                })
        this.cmbAccount.on('beforeselect',function(combo,record,index){
            return validateSelection(combo,record,index);
        },this);
        this.transTaxAmount=new Wtf.form.NumberField({
            allowBlank: true,
            allowNegative: false,
            defaultValue:0,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        });
//                this.cmbAccount.on('change',function(){
//                    var accRec=WtfGlobal.searchRecord(this.accountStore, this.cmbAccount.getValue(), 'accountid');
//                    var haveToPostJe = accRec ? accRec.data.haveToPostJe : false;
//                    if(haveToPostJe){
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), 
//                            WtfGlobal.getLocaleText({
//                                key:"acc.canNotCreateExpensePIOrCP", 
//                                params:[accRec ? accRec.data.usedIn : ""]
//                                })], 0);
//                        this.accountStore.remove(accRec);
//                        this.cmbAccount.setValue("");
//                    }
//                },this);
        },
    addTax:function(){
         var p= callTax("taxwin");
         Wtf.getCmp("taxwin").on('update', function(){this.taxStore.reload();}, this);
    },
    createColumnModel:function(){
        this.summary = new Wtf.ux.grid.GridSummary();
        this.rowno=(this.isNote)?new Wtf.grid.CheckboxSelectionModel():new Wtf.grid.RowNumberer();
        var columnArr =[];
        if(!this.readOnly){
            columnArr.push(this.sModel);
            columnArr.push(this.rowno);
        }
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.invoice.lineItemSequence"),//"Sequence",
            width:65,
            align:'center',
            name:'srno',
            renderer: Wtf.applySequenceRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridaccount"),  //"Account",
            width:200,
            dataIndex:this.readOnly?'accountname':'accountid',
            editor: this.readOnly?"":this.cmbAccount,
            renderer:this.readOnly?"":Wtf.comboBoxRenderer(this.cmbAccount)
        },{
            header:this.isCN?WtfGlobal.getLocaleText("acc.invoice.gridInvNo"):WtfGlobal.getLocaleText("acc.invoice.gridVenInvNo"),
            width:150,
            dataIndex:this.noteTemp?'transectionno':'billno',
            hidden:!this.isNote
        },{
            header: WtfGlobal.getLocaleText("acc.je.type"), //"Type",
            editor:(this.isNote||this.readOnly||this.isViewTemplate)?"":this.cmbType,
            renderer: Wtf.comboBoxRenderer(this.cmbType),
            dataIndex: 'debit'
        } ,{
             header:WtfGlobal.getLocaleText("acc.invoice.gridDescription"),//"Description",
             dataIndex:"desc",
             width:250,
             editor:this.readOnly?"":this.Description,
             renderer:function(val){
                 var regex = /(<([^>]+)>)/ig;
                val = val.replace(/(<([^>]+)>)/ig,"");
                return val;   
//                if(val.length<50)
//                    return val;   
//                else
//                    return val.substring(0,50)+" ...";   
            }
        },{
            header: WtfGlobal.getLocaleText("acc.invoice.gridAmount"),  //"Amount",
            dataIndex:"rate",
            align:'right',
            width:200,
            /*
             * erp-39553
             * When permission not given to display unit price  then this  Field will be encrypted and will not be editable
             */
            renderer: function (value, m, rec) {
                
                /*
                 *if LOOP TO CHECK if it is vendor and it doesn't have permission to view purchase invoice price
                 *If Yes, SHow *** instead of price 
                 *If No, show price as it is
                 * */
                if (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase) {
                    return Wtf.UpriceAndAmountDisplayValue;
                } else {
                    var symbol = ((rec == undefined || rec.data.currencysymbol == null || rec.data['currencysymbol'] == undefined || rec.data['currencysymbol'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbol']);
                    var v = parseFloat(value);
                    if (isNaN(v))
                        return value;
                    v = WtfGlobal.conventInDecimal(v, symbol)
                    return '<div class="currency">' + v + '</div>';
                }
            },
            editor:(this.isNote||this.readOnly || (this.isCustomer?!Wtf.dispalyUnitPriceAmountInSales:!Wtf.dispalyUnitPriceAmountInPurchase))?"":this.editprice
        },{
             header:Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA ? WtfGlobal.getLocaleText("acc.invoice.gridamountIncludingTax") :WtfGlobal.getLocaleText("acc.invoice.gridamountIncludingGST"),// Amount Excluding GST",
             dataIndex: "rateIncludingGstEx",
             align:'right',
             fixed:true,
             width:150,
             /*
             * When permission not given to display then this  Field will be encrypted and  will not be editable
             */
             renderer: function (value, isCheckCenterAlign, rec) {
                if (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase) {
                    return Wtf.UpriceAndAmountDisplayValue;
                } else {
                    var symbol = ((rec == undefined || rec.data.currencysymbol == null || rec.data['currencysymbol'] == undefined || rec.data['currencysymbol'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbol']);
                    var isCenterAlign = (isCheckCenterAlign == undefined ? false : isCheckCenterAlign);
                    var v = parseFloat(value);
                    if (isNaN(v)) {
                        return value;
                    }
                    v = WtfGlobal.convertInDecimalWithDecimalDigit(v, symbol, Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
                    if (rec.data.deleted) {
                        v = '<del>' + v + '</del>';
                    } else if (isCenterAlign) {
                        v = '<div>' + v + '</div>';
                    } else {
                        v = '<div class="currency">' + v + '</div>';
                    }
                    return v;
                }
            },
             hidden:true,
             editor:(this.isNote||this.readOnly||this.isViewTemplate)?"":this.editPriceIncludingGST,
             editable:false
        },{
            header: WtfGlobal.getLocaleText("acc.field.DiscountType"),
            width:150,
            dataIndex:'discountispercent',
            hidden:SATSCOMPANY_ID==companyid?true:false,
            renderer:Wtf.comboBoxRenderer(this.rowDiscountTypeCmb),
            editor:(this.isNote||this.readOnly||this.isViewTemplate)?"":this.rowDiscountTypeCmb
        },
        {
            header: WtfGlobal.getLocaleText("acc.invoice.gridDiscount"),  //"Discount %",
            dataIndex:"prdiscount",
            align:'right',
//            hidden:this.isOrder,
            width:200,
           renderer:function(v,m,rec){
                 if(rec.data.discountispercent) {
                     v= v + "%";
                 } else {
                     var symbol = WtfGlobal.getCurrencySymbol();
                     if(rec.data['currencysymbol']!=undefined && rec.data['currencysymbol']!=""){
                         symbol = rec.data['currencysymbol'];
                     }
                     
                     v= WtfGlobal.conventInDecimal(v,symbol)
                 }
                 return'<div class="currency">'+v+'</div>';
             },
            editor:this.readOnly||this.isNote?"":this.Discount
         },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridDiscountAmt"),  //"Discounted Amount",
            dataIndex:'discountamount',
            align:'right',
            width:200,
//            hidden:this.isOrder,
            renderer:this.calDiscountAmount.createDelegate(this)
        },{
             header: WtfGlobal.getLocaleText("acc.invoice.Tax"),  //"Tax",
             dataIndex:"prtaxid",
             id:this.id+"prtaxid",
             //align:'right',
             width:150,
             hidden:!(this.editTransaction||this.readOnly),
             renderer:Wtf.comboBoxRenderer(this.transTax),
             editor:this.readOnly||this.isNote?"":this.transTax
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount"),  // "Tax Amount",
             dataIndex:"taxamount",
             id:this.id+"taxamount",
             //align:'right',
             width:150,
             hidden:!(this.editTransaction||this.readOnly),
             renderer:this.setTaxAmount.createDelegate(this),
             editor:this.readOnly?"":this.transTaxAmount
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridLineAmt"),  //"Line Amount",
            align:'right',
            dataIndex:"calamount",
            width:200,
            hidden:this.isNote,
            renderer:this.calDiscAmount.createDelegate(this)
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridOriginalAmt"),  //"Original Amount ",
            dataIndex:"orignalamount",
            align:'right',
            width:150,
            hidden:!this.isNote,
            renderer:(this.isNote||this.readOnly?WtfGlobal.withoutRateCurrencySymbol:WtfGlobal.currencyRendererSymbol)
        },{
             header: 'Line Level Term Amount',
             dataIndex:"lineleveltermamount",
             hidden: true,
             hideLabel : true,
             align:'right',
             width:200
        },{
            header:this.isNote?WtfGlobal.getLocaleText("acc.invoice.gridCurAmt"):WtfGlobal.getLocaleText("acc.invoice.gridAmount"),
            dataIndex:"amount",
            align:'right',
            hidden:!this.isNote,
            width:200,
            renderer:(this.isNote||this.readOnly?WtfGlobal.withoutRateCurrencySymbol:this.calAmount.createDelegate(this))
    },{
            header:WtfGlobal.getLocaleText("acc.invoice.Tax"),  //"Tax",
            dataIndex:"taxpercent",
            align:'right',
            hidden:!this.isNote,
            width:200,
            renderer:function(v){return'<div class="currency">'+v+'%</div>';}
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.Tax"),  //"Tax",
             dataIndex:"prtaxpercent",
             align:'right',
             hidden:!this.isNote,
             width:200,
             renderer:function(v){return'<div class="currency">'+v+'%</div>';}
        },{
            header:(this.readOnly)?WtfGlobal.getLocaleText("acc.invoice.gridAmount"):"<b>"+WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt")+"</b>",
            dataIndex:this.noteTemp?'discount':'discamount',
            align:'right',
            width:200,
            hidden:!this.isNote,
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            editor:this.readOnly?"":new Wtf.form.NumberField({
               allowBlank: false,
               allowNegative: false
           })
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridNoteType"),  //"Note Type",
            width:200,
            dataIndex:'typeid',
            hidden:(!this.isNote ||this.noteTemp),
            renderer:Wtf.comboBoxRenderer(this.typeEditor),
            editor:this.readOnly?"":this.typeEditor
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
            this.cmbAccount.addNewFn=this.openCOAWindow.createDelegate(this)
        
         columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModel[this.moduleid],undefined,undefined,this.readOnly);
        
        if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isTDSApplicable  && this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId && !this.isTemplate) {// TDS is only for INDIA Country and - On this amount TDS is Applied
            columnArr.push({
                header: "TDS Calculation",
                align: 'center',
                width: 40,
                renderer: this.addRenderer.createDelegate(this),
                hidden: !Wtf.isTDSApplicable
            });
        }
        if(!this.isNote && !this.readOnly && !this.isLinkedTransaction) {
            columnArr.push({
                header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),  //"Action",
                align:'center',
                width:80,
                renderer: this.deleteRenderer.createDelegate(this)
            });
        }
        this.cm=new Wtf.grid.ColumnModel(columnArr);
        this.sm=this.sModel;
    },
     checkSelections:function( scope, rowIndex, keepExisting, record){
        if(rowIndex== (this.store.getCount()-1)){
            return false;
        }else{
            return true;
        }
       
    },
//     checkRow:function(obj){
//         if(obj!=null){
//             var rec=obj.record;
//             if(obj.field=="accountid"){
//                var index=this.accountStore.find('accountid',obj.value)
//                if(index>0){
//                    rec=this.accountStore.getAt(index);
//                    if(this.store.find("accountid",obj.value)>=0){
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.76")+" "+rec.data['accountname']], 2);
//                        obj.cancel=true;
//                    }
//                }
//             }
//         }
//     },
    openCOAWindow:function(){
        this.stopEditing();
        callCOAWindow(false, null, "coaWin");
        Wtf.getCmp("coaWin").on("update",function(){this.accountStore.reload()},this);
    },
    deleteRenderer:function(v,m,rec){
        return "<div style='margin: auto;' class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    addRenderer: function(v, m, rec) { // Onclick icon TDS window will open
        return "<div class='" + getButtonIconCls(Wtf.etype.tdswinexpencegrid) + "'></div>";
    },
    RitchTextBoxSetting:function(grid, rowIndex, columnIndex, e){
        var record = grid.getStore().getAt(rowIndex);
        var fieldName= grid.getColumnModel().getDataIndex(columnIndex);
        if(e.getTarget(".richtext")){
            var value = record.get(fieldName);
            new Wtf.RichTextArea({
                rec:record,
                fieldName:fieldName,
                val: value?value:"",
                readOnly:this.readOnly
            });
        } 
        if(Wtf.account.companyAccountPref.proddiscripritchtextboxflag!=0 && !this.readOnly){
        if(fieldName == "desc"){
            if (Wtf.account.companyAccountPref.proddiscripritchtextboxflag==1) {
                this.prodDescTextArea = new Wtf.form.TextArea({// Just make new Wtf.form.TextArea to new Wtf.form.HtmlEditor to fix ERP-8675
                    name: 'remark',
                    id: 'descriptionRemarkTextAreaId'
                });
            } else if (Wtf.account.companyAccountPref.proddiscripritchtextboxflag==2){
                this.prodDescTextArea = new Wtf.form.HtmlEditor({// Just make new Wtf.form.TextArea to new Wtf.form.HtmlEditor to fix ERP-8675
                    name: 'remark',
                    id: 'descriptionRemarkTextAreaId'
                });
            }
    
                var val=record.data.desc;
//                val = val.replace(/(<([^>]+)>)/ig,""); // Just comment this line to fix ERP-8675
                this.prodDescTextArea.setValue(val);
            if(record.data.accountid !=undefined && record.data.accountid !=""){
                var descWindow=Wtf.getCmp(this.id+'DescWindow')
                if(descWindow==null){
                    var win = new Wtf.Window
                    ({
                        width: 560,
                        height:310,
                        title:record.data.accountname +" "+WtfGlobal.getLocaleText("acc.gridproduct.discription"),
                        layout: 'fit',
                        id:this.id+'DescWindow',
                        bodyBorder: false,
                        closable:   true,
                        resizable:  false,
                        modal:true,
                        items:[this.prodDescTextArea],
                        bbar:
                        [{
                            text: 'Save',
                            iconCls: 'pwnd save',
                            handler: function()
                            {
                                record.set('desc',  Wtf.get('descriptionRemarkTextAreaId').getValue());
                                win.close();   
                            }
                        },{
                            text: 'Cancel',
                             handler: function()
                            {
                                win.close();   
                            }
                        }]
                    });
                }
                win.show(); 
                }
                return false;
            }
        }    
    },
    handleRowClick:function(grid,rowindex,e){

        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                store.remove(store.getAt(rowindex));
                if(rowindex==total-1){
                    this.addBlankRow();
                }
                this.fireEvent('datachanged',this);
            }, this);
        }
        if(!(this.isViewTemplate || this.readOnly) && e.target.className == "pwndBar2 shiftrowupIcon") {
            moveSelectedRowFormasterItems(grid,0,rowindex);
        }
        if(!(this.isViewTemplate || this.readOnly) && e.target.className == "pwndBar2 shiftrowdownIcon") {
            moveSelectedRowFormasterItems(grid,1,rowindex);
        }
        if (e.getTarget(".tdsCalc-ecpancegridrow")) { // For Call TDS window
            var gridStoreDetails = grid.getStore();
            var jsonrecord = gridStoreDetails.getAt(rowindex);
            /*
             * if not TDS applicable on vendor we will not allowing to open TDS window
             * in edit case even if tds is not applicable but if data is present we will allow to open TDS calculation window
             */
            var businesspersoninfo = WtfGlobal.searchRecord(this.parentObj.personstore, this.parentObj.Name.getValue(), 'accid');
            if (!Wtf.isEmpty(businesspersoninfo) && !Wtf.isEmpty(businesspersoninfo.data.isTDSapplicableonvendor) && !businesspersoninfo.data.isTDSapplicableonvendor && Wtf.isEmpty(jsonrecord.data.appliedTDS)) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.vendorpaymentcontrollercmn.TDSIsNotAppliedForSelectedVendor")], 2);
                return false;
            } else if (!Wtf.isEmpty(businesspersoninfo) && Wtf.isEmpty(businesspersoninfo.data.deducteetypename) && Wtf.isEmpty(jsonrecord.data.appliedTDS)) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.vendorpaymentcontrollercmn.deducteeTypeNotSet")], 2);
                return false;
            }
            /*****/
            var alertMsg = "Do you want to calculate TDS ?";
            if (this.readOnly || this.isLinkedTransaction || !Wtf.isEmpty(jsonrecord.data.tdsjemappingID)) {
                if (!Wtf.isEmpty(jsonrecord.data.appliedTDS)) {
                    this.openWindowForSelectingTDS(jsonrecord, rowindex);
                    return true;
                } else {
                    return false;
                }
            } else if (this.editTransaction) {
                alertMsg = "Do you want to edit TDS ?";
            }
            Wtf.MessageBox.confirm("TDS Calculation", alertMsg, function (btn) {
                if (btn != "yes") {
                    return;
                }
                this.openWindowForSelectingTDS(jsonrecord, rowindex);
            }, this);
        }
    },

    calAmount:function(v,m,rec){
        var val=rec.data.rate-(rec.data.rate*rec.data.prdiscount/100);
        rec.set("amount",val);
        if(this.isNote||this.readOnly)
             return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
        return WtfGlobal.currencyRendererSymbol(val,m,rec);
    },

    addBlankRow:function(){
               
        var rec = this.storeRec;
        rec = new rec({});
        rec.beginEdit();
        var fields=this.store.fields;
        for(var x=0;x<fields.items.length;x++){
            var value="";
            rec.set(fields.get(x).name, value);
          
        }   
        value=0;
        rec.set(fields.get('rate').name, '');
        rec.set(fields.get('discountamount').name, value);
        rec.set(fields.get('totalamount').name, value);
        rec.set(fields.get('amount').name, value);
        rec.set(fields.get('taxamount').name, value);
        rec.set(fields.get('taxpercent').name, value);
        rec.set(fields.get('prdiscount').name, value);
        rec.set(fields.get('discountispercent').name, "1");
        rec.set(fields.get('calamount').name, value);
        rec.set(fields.get('typeid').name, value);
        rec.set(fields.get('currencysymbol').name, this.symbol);
        rec.set(fields.get('isNewRecord').name, "1");
        rec.set(fields.get('debit').name, true);
        rec.set(fields.get('rateIncludingGstEx').name, "0.0");
        rec.set(fields.get('appliedTDS').name, ""); // TDS details initially empty
        rec.set(fields.get('tdsamount').name, "0.0"); // TDS amount initially zero
        rec.endEdit();
        rec.commit();
        this.store.add(rec);
        this.getView().refresh();
    },
    addBlank:function(){
       this.setGridDiscValues();
        this.addBlankRow();
    },
    setGridDiscValues:function(){
            this.store.each(function(rec){
                   if(!this.editTransaction||this.fromPO)
                        rec.set('prdiscount',0)
                },this);
    }, 
    updateRow:function(obj){
        if(obj!=null){
             var rec=obj.record;
             if(obj.field=="prdiscount"){
                 rec=obj.record;
                 if(obj.value >100 && (rec.data.discountispercent == 1)){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.rem.118")], 2);
                        rec.set("prdiscount",0);
                  }
                if(obj.value=="")
                     rec.set("prdiscount",0);
                 var taxamount = this.setTaxAmountAfterSelection(rec);
                 rec.set("taxamount",taxamount);
                 obj.record.set("isUserModifiedTaxAmount",false);
             } else if(obj.field=="accountid"){
                if(this.parentObj && this.parentObj.includeProTax && this.parentObj.includeProTax.getValue() == true) {
                    /*SDP-7500/ERP-31656
                     *In Expense tab Auto map the Tax mapped in Venodor Master.
                     *Purchase Invoice,Purchase order,Cash Purchase Invoice
                     */
                    if(this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid==Wtf.Acc_Purchase_Order_ModuleId || this.moduleid== Wtf.Acc_Cash_Purchase_ModuleId){
                        var taxid = "";
                        var currentTaxItem=WtfGlobal.searchRecord(this.parentObj.personstore, this.parentObj.Name.getValue(), 'accid');
                        var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
                        if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null){
                            taxid = actualTaxId;
                        }
                        obj.record.set("prtaxid", taxid); 
                    }else{
                        var accountComboIndex = this.accountStore.find('accountid',obj.value);
                        if(accountComboIndex >=0) {
                            obj.record.set("prtaxid", this.accountStore.getAt(accountComboIndex).data["acctaxcode"]);
                        }
                    }
                } else {
                    obj.record.set("prtaxid", "");
                }
                var taxamount = this.setTaxAmountAfterSelection(obj.record);
                obj.record.set("taxamount",taxamount);
                obj.record.set("isUserModifiedTaxAmount",false);
                var accComboIndex = WtfGlobal.searchRecordIndex(this.accountStore, obj.value, 'accountid');
                if(accComboIndex >=0) {
                    obj.record.set("desc", this.accountStore.getAt(accComboIndex).data["desc"]);
                }
                var accRec = WtfGlobal.searchRecord(this.accountStore, obj.value, 'accountid');
                var haveToPostJe = accRec ? accRec.data.haveToPostJe : false;
                if (haveToPostJe) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText({key: "acc.canNotCreateExpensePIOrCP", params: [accRec ? accRec.data.usedIn : ""]})], 0);
                    var productComboIndex = WtfGlobal.searchRecordIndex(this.store, obj.value, 'accountid');
                    if (productComboIndex >= 0) {
                        obj.record.set("accountid", "");
                        obj.record.set("desc", "");
                    }
                }
                this.accountStore.clearFilter();                
            } else if(obj.field=="prtaxid"){
                taxamount = this.setTaxAmountAfterSelection(obj.record);
                obj.record.set("taxamount",taxamount);
                obj.record.set("isUserModifiedTaxAmount",false);
                if (WtfGlobal.singaporecountry() && WtfGlobal.getCurrencyID() != Wtf.Currency.SGD && this.forCurrency != Wtf.Currency.SGD) {
                    var record = WtfGlobal.searchRecord(this.parentObj.currencyStore, this.parentObj.Currency.getValue(), "currencyid");
                    callGstCurrencyRateWin(this.id, record.data.currencyname + " ", obj, obj.record.get("gstCurrencyRate") * 1);
                }
            } else if(obj.field=="rate"){
                taxamount = this.setTaxAmountAfterSelection(obj.record);
                obj.record.set("taxamount",taxamount);
                obj.record.set("isUserModifiedTaxAmount",false);
            }else if(obj.field=="discountispercent" && obj.value == 1 && (rec.data.prdiscount > 100)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Percentdiscountcannotbegreaterthan100")], 2);
                rec.set("discountispercent",0);
            }
            if (Wtf.Countryid == Wtf.Country.INDIA && obj.originalValue != obj.record.get("enteramount") && obj.field!="desc" && Wtf.isEmpty(obj.record.get("tdsjemappingID"))) { // Anything changed in line item reset TDS data
                obj.record.set("appliedTDS", "");
                obj.record.set("rowdetailid", "");
                obj.record.set("tdsamount", 0);
            }
            if (obj.field == "taxamount") {
            /*
             * If user changed the tax amount manually then isUserModifiedTaxAmount flag made true for Adaptive Rounding Algorithm calculataion else by default false.
             * ERM-1085
             */
            obj.record.set("isUserModifiedTaxAmount", true);
        }
        WtfGlobal.calculateTaxAmountUsingAdaptiveRoundingAlgo(this, true);
        }
        
        this.fireEvent('datachanged',this);
        if(this.store.getCount()>0&&(this.store.getAt(this.store.getCount()-1).data['accountid'].length<=0)){//||this.store.getAt(this.store.getCount()-1).data['rate']==0
            return;}
        if(!this.isNote)
            this.addBlankRow();
    },

    calDiscountAmount:function(v,m,rec){
        /*
         * When permission not given to display then this  Field will be encrypted
         */
        if (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase) {
            return Wtf.UpriceAndAmountDisplayValue;
        } else {
        var val=0;
        if(rec.data.discountispercent == 1){
            /*
             * when Discount Type is percentage 
             */
            val=(rec.data.rate*rec.data.prdiscount/100);
            rec.set("discountamount",(val));
        }else{
            /*
             * when Discount Type is flat 
             */
            rec.set("discountamount",(rec.data.prdiscount));
            val=rec.data.prdiscount;
        }
        return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
    }
},

    calDiscAmount:function(v,m,rec){
        /*
         * When permission not given to display then this  Field will be encrypted
         */
        if (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase) {
            return Wtf.UpriceAndAmountDisplayValue;
        } else {
        var val=0;
            /*
             * rec.data.discountispercent=1 (discountispercent is percentage)
             * rec.data.discountispercent=0 (discountispercent is flat)
             */
        if(rec.data.discountispercent == 1){
            val=rec.data.rate-getRoundedAmountValue((rec.data.rate*rec.data.prdiscount/100));
        }else{
            val=rec.data.rate-getRoundedAmountValue((rec.data.prdiscount));
        }
        
        var taxamount= rec.get('taxamount');//this.calTaxAmount(rec);
        /*
         * if includinging Gst  true 
         */
        if(this.parentObj.includingGST.getValue()){
            /*
             *(rec.data.rate=Entered amount)
             *rec.data.rate-taxamount=Amount (Excluding gst)
             *val+taxamount=Total Amount
             */
//            Commented for ERP-24202
//            val=getRoundedAmountValue(rec.data.rate-taxamount);
//            val+=getRoundedAmountValue(taxamount);
        }else{
         /*
         * if includinging Gst false 
         */
            val+=getRoundedAmountValue(taxamount);
        }
        
        rec.set("calamount",val);
        return WtfGlobal.withoutRateCurrencySymbol(val,m,rec);
    }
},
     calTaxAmount:function(rec){
        var discount=getRoundedAmountValue(rec.data.rate*rec.data.prdiscount/100);
        var val=rec.data.rate-discount;
        var taxpercent=0;
            var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
            if(index>=0){
               var taxrec=this.taxStore.getAt(index);
              // alert(taxrec.data.toSource())
                taxpercent=taxrec.data.percent;
            }
        return getRoundedAmountValue((val*taxpercent/100));

    },
    setTaxAmountAfterSelection:function(rec) {
        
        var rate=getRoundedAmountValue(rec.data.rate);
        var prdiscount=getRoundedAmountValue(rec.data.prdiscount);
        var discount=0;
//        var discount=getRoundedAmountValue(rate*prdiscount/100);
        if(rec.data.prdiscount > 0) {
            var prdiscount=getRoundedAmountValue(rec.data.prdiscount);
            if(rec.data.discountispercent == 1){
                discount = getRoundedAmountValue(rate * prdiscount/ 100);
            } else {
                discount = prdiscount;
            }
        }
        var lineTermAmount = (rec.data.lineleveltermamount == undefined || rec.data.lineleveltermamount == null || rec.data.lineleveltermamount == '')?0:rec.data.lineleveltermamount;
        var val=rate-discount+lineTermAmount;
        var taxpercent=0;
        var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
        if(index>=0){
            var taxrec=this.taxStore.getAt(index);
            // alert(taxrec.data.toSource())
            taxpercent=getRoundedAmountValue(taxrec.data.percent);
        }
        var taxamount=0.0;
        if(this.parentObj.includingGST.getValue()){
         taxamount=getRoundedAmountValue((val*taxpercent)/(taxpercent+100));//val*taxpercent/100
         val=val-taxamount;
         rec.set("rateIncludingGstEx",getRoundedAmountValue(val));
        }else{
          taxamount= getRoundedAmountValue(val*taxpercent/100);
        }
        // ERP-24202 - Recalculating Tax amountconsidering the discount in calculation
        if((this.parentObj.includingGST && this.parentObj.includingGST.getValue())){
            taxamount = this.recalculateTaxAmountWithDiscount(rec,taxamount,taxpercent);
        }
        return getRoundedAmountValue(taxamount);
//        rec.set("taxamount",taxamount);
    },
    setTaxAmount:function(v,m,rec){
       var taxamount= v;//this.calTaxAmount(rec);
       if(rec.data.prtaxid==null || rec.data.prtaxid == undefined || rec.data.prtaxid == ""){
           taxamount = 0;
       }
       rec.set("taxamount",taxamount);
        if(this.isNote||this.readOnly)
             return WtfGlobal.withoutRateCurrencySymbol(taxamount,m,rec);
        return WtfGlobal.currencyRendererSymbol(taxamount,m,rec);
    },

    loadPOGridStore:function(recids, flag, VQtoCQ,linkingFlag,sopolinkflag, isForLinking, isForInvoice, prvqlinkflag,isExplodeAssemblyPrd,prpolinkflag,isMRPJOBWORKIN,productid, sotopolinkflag,dtype,isForSGELinking,dimArr,transactiondate,isJobWorkOutLinkedWithPI){
        this.store.load({
            params:{
                bills:recids,
                mode:(this.isCustBill?53:43),
                closeflag:true,
                isForLinking:isForLinking
            }
        });
        this.store.on('load', function() {
            var index = this.store.indexOf(this.store.data.last());
            var rec = this.store.getAt(index);
            /* Adding only one blank row after last record at line level*/
            if (this.store.getAt(index + 1) == undefined && rec.data.accountid != "") {
                this.addBlankRow();
            }
            this.fireEvent('datachanged', this);
        }, this);
    },   
    
    calSubtotal:function(){
        var subtotal=0;
        for(var i=0;i<this.store.getCount();i++)
            if (this.store.getAt(i).data['debit']==true) {
            subtotal+=getRoundedAmountValue(this.store.getAt(i).data.calamount);        
            } else {
              subtotal-=getRoundedAmountValue(this.store.getAt(i).data.calamount);        
            }
        return getRoundedAmountValue(subtotal);
    },
    calTDSAssasableSubtotal:function(){ // calculate total TDS assessable amount
        var subtotal=0;
        for(var i=0;i<this.store.getCount();i++)
            if (this.store.getAt(i).data['debit']==true) {
                if (!Wtf.isEmpty(this.store.getAt(i).data['appliedTDS'])) {
                    subtotal += getRoundedAmountValue(this.store.getAt(i).data.tdsamount);
                }
            } else {
                if (!Wtf.isEmpty(this.store.getAt(i).data['appliedTDS'])) {
                    subtotal-=getRoundedAmountValue(this.store.getAt(i).data.tdsamount);        
                }
            }
        return getRoundedAmountValue(subtotal);
    },
    getTDSNOPArrayAppliedAtLineLevel: function (){
        var tdsNOPArray = [];
        var count=this.store.getCount();
        for (var i = 0; i < count; i++){
            if (!Wtf.isEmpty(this.store.getAt(i).data['appliedTDS'])) {
                var jsonArrayObj = eval(this.store.getAt(i).data.appliedTDS);
                for(var j=0;j<jsonArrayObj.length;j++){
                    var temp = {};
                    temp['natureofpayment'] = jsonArrayObj[j].natureofpaymentName;
                    temp['tdsrate'] = jsonArrayObj[j].tdspercentage + ' %';
                    /*
                     * If  in User Administration > Assign Permission > Display Unit Price & Amount in Purchase Document
                     * If it uncheck we will hide amount and show '*****',  
                    */
                    if(!Wtf.dispalyUnitPriceAmountInPurchase){
                        temp['tdsamount'] = Wtf.UpriceAndAmountDisplayValue;
                    }else{
                        temp['tdsamount'] = !Wtf.isEmpty(jsonArrayObj[j].tdsamount)?WtfGlobal.currencyRenderer(jsonArrayObj[j].tdsamount):WtfGlobal.currencyRenderer(0);
                    }
                    tdsNOPArray.push(temp);
                }
            }
        }
        return tdsNOPArray;
    },
     calLineLevelTax:function(){
        var subtotal=0;
        var total=0;
        var taxTotal=0;
        var taxAmount=0;
        var taxAndSubtotal=[];
        var count=this.store.getCount();
        for(var i=0;i<count;i++){
        if(this.store.getAt(i).data['debit']==true){
            total=parseFloat(this.store.getAt(i).data['calamount']);
            subtotal+=getRoundedAmountValue(total);
            taxAmount=parseFloat(this.store.getAt(i).data['taxamount']);
            taxTotal+=getRoundedAmountValue(taxAmount);
        }else{
            total=parseFloat(this.store.getAt(i).data['calamount']);
            subtotal-=getRoundedAmountValue(total);
            taxAmount=parseFloat(this.store.getAt(i).data['taxamount']);
            taxTotal-=getRoundedAmountValue(taxAmount);
        }
            
    }
        taxAndSubtotal[0]=getRoundedAmountValue(subtotal);
        taxAndSubtotal[1]=getRoundedAmountValue(taxTotal);
        return taxAndSubtotal;
    },
    
    calSubtotalInBase:function(){
        var subtotalinbase=0;
        var total=0;
        var count=this.store.getCount();
        for(var i=0;i<count;i++){
            total=getRoundedAmountValue(parseFloat(this.store.getAt(i).data.calamount));
            subtotalinbase+=getRoundedAmountValue(total*this.getExchangeRate());
        }
        return getRoundedAmountValue(subtotalinbase);
    },
    
    getExchangeRate:function(){
        var revExchangeRate = 0;
        if(this.parentObj!=null && this.parentObj!="" && this.parentObj!=undefined){
            var index=this.parentObj.getCurrencySymbol();
            var rate=this.parentObj.externalcurrencyrate;
            if(index>=0){
                var exchangeRate = this.parentObj.currencyStore.getAt(index).data['exchangerate'];
                if(this.parentObj.externalcurrencyrate>0) {
                    exchangeRate = this.parentObj.externalcurrencyrate;
                }
                revExchangeRate = 1/(exchangeRate);
                revExchangeRate = (Math.round(revExchangeRate*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
            }
        }
        return revExchangeRate;
    }, 

    setCurrencyid:function(cuerencyid,rate,symbol,record){
        this.symbol=symbol;
        this.store.each(function(rec){
            rec.set('currencysymbol',this.symbol)
        },this)
     },
    getProductDetails:function(){
        var arr=[];
        this.store.each(function(rec){
              rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
              arr.push(this.store.indexOf(rec));
                
        }, this);
        var jarray=WtfGlobal.getJSONArray(this,false,arr);
        return jarray;
//            return WtfGlobal.getJSONArray(this);
    },

    getCMProductDetails:function(){
        var arr=[];
        var selModel=  this.getSelectionModel();
        var len=this.store.getCount();
        for(var i=0;i<len;i++){
            if(selModel.isSelected(i))
                arr.push(i);
            }
        return WtfGlobal.getJSONArray(this,true,arr);
    },

    isAmountzero:function(store){
        var amount;
        var selModel=  this.getSelectionModel();
        var len=this.store.getCount();
        for(var i=0;i<len;i++){
            if(selModel.isSelected(i)){
                amount=store.getAt(i).data.discamount;
                if(amount<=0)
                    return true;
            }
        }
        return false;
    },
    populateDimensionValueingrid: function (rec) {
        WtfGlobal.populateDimensionValueingrid(this.moduleid, rec, this);
    },
    checkDetails:function(grid){
    var v=WtfGlobal.checkValidItems(this.moduleid,grid);
        return v;
    },
    recalculateTaxAmountWithDiscount:function(rec,taxamount,taxpercent){
        var discountFigure = (rec.data.prdiscount != null || rec.data.prdiscount != undefined || rec.data.prdiscount != '')?rec.data.prdiscount:0;
        var discountType = (rec.data.discountispercent != null || rec.data.discountispercent != undefined || rec.data.discountispercent != '') ? rec.data.discountispercent:null;
        var newTaxAmount=taxamount;
        var taxableAmount=0;
        var lineTermAmount = (rec.data.lineleveltermamount == undefined || rec.data.lineleveltermamount == null || rec.data.lineleveltermamount == '')?0:rec.data.lineleveltermamount;
        if(discountType != null){
            var rate=getRoundofValueWithValues(rec.data.rate,Wtf.UNITPRICE_DIGIT_AFTER_DECIMAL);
            var origionalAmount = getRoundedAmountValue(rate);
            var discountedAmt = 0;
            if(discountType == 1){ // Percentage
                discountedAmt =  origionalAmount - getRoundedAmountValue(origionalAmount * discountFigure/ 100);
            } else {  // Flat
                discountedAmt = origionalAmount - discountFigure;
            }
            discountedAmt = discountedAmt+lineTermAmount;
//            taxableAmount = getRoundedAmountValue((100*discountedAmt)/(100+taxpercent));
//            newTaxAmount = getRoundedAmountValue((taxableAmount*taxpercent)/100);
            newTaxAmount = getRoundedAmountValue((discountedAmt*taxpercent)/(taxpercent+100)); 
        }
        return newTaxAmount;
    },
    hideShowCustomizeLineFields:function(){ 
        if(this.moduleid==Wtf.Acc_Invoice_ModuleId || this.moduleid==Wtf.Acc_Vendor_Invoice_ModuleId ||this.moduleid==Wtf.Acc_Cash_Sales_ModuleId||this.moduleid==Wtf.Acc_Cash_Purchase_ModuleId||
            this.moduleid==Wtf.Acc_Purchase_Order_ModuleId||this.moduleid==Wtf.Acc_Sales_Order_ModuleId||this.moduleid==Wtf.Acc_Customer_Quotation_ModuleId||this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId){
            Wtf.Ajax.requestEx({
                url: "ACCAccountCMN/getCustomizedReportFields.do",
                params: {
                    flag: 34,
                    moduleid:this.moduleid,
                    reportId:1,
                    isFormField:true,
                    isLineField:true
                }
            }, this, function(action, response){
                if(action.success && action.data!=undefined){
                    this.customizeData=action.data;
                    var cm=this.getColumnModel();
                    for(var i=0;i<action.data.length;i++){
                        for(var j=0;j<cm.config.length;j++){
                            if(cm.config[j].dataIndex == 'desc'){
                                if(cm.config[j].header==action.data[i].fieldname ){
                                    cm.setHidden(j, action.data[i].hidecol);
                                    cm.setEditable(j, !action.data[i].isreadonlycol);
                                }
                            } else if(cm.config[j].dataIndex==action.data[i].fieldDataIndex ){
                                cm.setHidden(j,action.data[i].hidecol);       
                                cm.setEditable(j,!action.data[i].isreadonlycol);
                                if( action.data[i].fieldlabeltext!=null && action.data[i].fieldlabeltext!=undefined && action.data[i].fieldlabeltext!=""){
                                    cm.setColumnHeader(j,action.data[i].fieldlabeltext);
                                }
                            }
                        }
                    }
                    this.reconfigure( this.store, cm);
                } else {
                //                    Wtf.Msg.alert('Status', action.msg);
                }
            },function() {
                });
        }
    },
    openWindowForSelectingTDS: function (record, rowindex) { // call TDS window
        var gstCodeSelected = record.data['accountid'];
        var selectedAccountName = '';
        var selectedAccountinfo = WtfGlobal.searchRecord(this.accountStore, gstCodeSelected, 'accountid')
        if (!Wtf.isEmpty(selectedAccountinfo) && selectedAccountinfo != null) {
            selectedAccountName = selectedAccountinfo.data.accountname;
        }
        
        var businesspersoninfo = WtfGlobal.searchRecord(this.parentObj.personstore, this.parentObj.Name.getValue(), 'accid');
        if (!Wtf.isEmpty(businesspersoninfo) && businesspersoninfo != null) {
            businesspersoninfo = businesspersoninfo.data;
            var appliedTDS = record.data['appliedTDS']?record.data['appliedTDS']:'';
            var TDSJEMappring = !Wtf.isEmpty(record.data['tdsjemappingID']); // If TDS je Mapping you are not allow to edit TDS data
            var advancePaymentNop ="";
            var advancePaymentInfo = WtfGlobal.searchRecord(this.parentObj.AdjustAdvancePayments.store, this.parentObj.AdjustAdvancePayments.getValue(), 'AdvancePaymentID');
            if (!Wtf.isEmpty(advancePaymentInfo) && advancePaymentInfo != null) {
                advancePaymentNop = advancePaymentInfo.data.natureofpayment
            }
            this.TDSPaymentWindow = new Wtf.account.TDSPaymentWindow({
                id: 'tdstaxesexpancewindow',
                isReceipt: false,
                border: false,
                readOnly: this.readOnly || TDSJEMappring || this.isLinkedTransaction, // Send readOnly Proeprty to TDS payment window
                isEdit: this.editTransaction,
                accountId: gstCodeSelected,
                appliedTDS: appliedTDS,
                parentObj: this,
                personInfo: businesspersoninfo,
                record: record,
                callFrom: 'expense',// Call From Invoice
                basicExemptionExceeded: false,
                advancePaymentNop:advancePaymentNop,
                selectedAccountName : selectedAccountName
            });
            this.TDSPaymentWindow.on('beforeclose', function (winObj) {
                if (winObj.isSubmitBtnClicked) {
                    this.setTDSToSelectedRow(winObj.getSelectedRecords(), record, rowindex);
                }
            }, this);
            this.TDSPaymentWindow.show();
        }
    },
    setTDSToSelectedRow:function(jsonArray,record,rowindex){ // set TDS assessable amount on Submit TDS window
        var jsonArrayObj = eval(jsonArray);
        var totalTDSamount=0;
        for(var i=0;i<jsonArrayObj.length;i++){
            if(!Wtf.isEmpty(jsonArrayObj[i].tdsamount) && !Wtf.isEmpty(jsonArrayObj[i])){
                totalTDSamount+=parseFloat(jsonArrayObj[i].tdsamount);
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Please fill all the data"], 2);
                return false;
            }
        }
        var recordToSet=this.store.getAt(rowindex);
        var adjustedTotalAmount=0;
        for(var i=0;i<this.store.getCount();i++){
            var rec=this.store.getAt(i);
            if(i!=rowindex && !Wtf.isEmpty(rec.get("appliedTDS"))){
                var appliedTDS = eval(rec.get("appliedTDS"));
                if(!Wtf.isEmpty(appliedTDS[0].advancePaymentDetails)){
                    var advPayTDS = eval(appliedTDS[0].advancePaymentDetails);
                    adjustedTotalAmount += advPayTDS[0].adjustedAdvanceTDSamount;
                }
            }
        }
        var advPay= eval(jsonArrayObj[0].advancePaymentDetails);
        var advjsonArray = !Wtf.isEmpty(advPay)?advPay:[];
        var advjsonData = !Wtf.isEmpty(advPay)?advPay[0]:{};
        if(totalTDSamount>0 && this.parentObj.TotalAdvanceTDSAdjustmentAmt-adjustedTotalAmount>0){
            if(totalTDSamount>(this.parentObj.TotalAdvanceTDSAdjustmentAmt-adjustedTotalAmount)){
                advjsonData['adjustedAdvanceTDSamount'] = (this.parentObj.TotalAdvanceTDSAdjustmentAmt-adjustedTotalAmount);
            }else{
                advjsonData['adjustedAdvanceTDSamount'] = totalTDSamount;
            }
            advjsonData["goodsReceiptDetailsAdvancePaymentId"] = this.parentObj.AdjustAdvancePayments.getValue();
            advjsonData["paymentamount"] = this.parentObj.TotalAdvanceTDSAdjustmentAmt;
        }
        advjsonArray.push(advjsonData);
        jsonArrayObj[0].advancePaymentDetails = JSON.stringify(advjsonArray);
        jsonArray = JSON.stringify(jsonArrayObj);
        recordToSet.set('tdsamount',totalTDSamount);
        recordToSet.set('appliedTDS',jsonArray);
        this.fireEvent('datachanged',this);
    },
    updateGridState: function (state) {
        if (state && state.columns) {
            for (var i = 0; i < state.columns.length; i++) {
                var column = Object.assign(new Object(), state.columns[i]);
                if (column.id == 'accountid') {
                    column.id = 'accountname';
                } else {
                    continue;
                }
                state.columns.splice(i+1, 0, column);
            }
        }
        return state;
    }
});
