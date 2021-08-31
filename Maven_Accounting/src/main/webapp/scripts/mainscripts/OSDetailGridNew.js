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

Wtf.account.OSDetailGridNew = function(config) {
    this.isReceipt = config.isReceipt;
    this.id = config.id;
    this.paymentEntryObject=config.paymentEntryObject;
    this.isEdit = config.isEdit;
    this.isCopyReceipt = config.isCopyReceipt;
    this.accid = config.accid;
    /*
     * isbulkpayment is true if click on bulkpayment from invoice report
     */
    this.isBulkPayment=(config.isBulkPayment == null || config.isBulkPayment == undefined)? false : config.isBulkPayment;
    this.invObj=config.invObj;
    this.selectedRow = -1; // when clicked on document type and opened window it will set to selected row index. After populated documents in line level grid it will reset to -1    
    this.paymentType=config.paymentType;
    this.currencyid= config.currencyid;
    this.readOnly= (config.readOnly!=undefined && config.readOnly) ? true : false;
    this.paymentOption=config.paymentOption;
    this.isCustomer=config.isCustomer;
    this.businessPerson = (this.isCustomer ? 'Customer' : 'Vendor');    
    this.billid=config.billid;
    this.isAllowedSpecificFields=(config.isAllowedSpecificFields == null || config.isAllowedSpecificFields == undefined)? false : config.isAllowedSpecificFields;
    this.AllowToEditCopy=config.AllowToEditCopy
    this.pendingApproval=config.pendingApproval;
    this.gridRec = Wtf.data.Record.create([
    {name: 'type' , defValue:this.paymentType==this.paymentOption.AgainstGL?4:''}, 
    {name: 'debit' , defValue:true},  // it is used mainly for Many CN/DN
    {name: 'currencysymbol', defValue:config.symbol},
    {name: 'currencyidtransaction'},
    {name: 'currencynametransaction'},
    {name: 'currencysymboltransaction'},
    {name: 'currencyname'},
    {name: 'currencyid'},
    {name: 'payment'}, 
    {name: 'paymentWithSalesReturn'}, 
    {name: 'documentno'},
    {name: 'documentid'},
    {name: 'description', defValue: ''},
    {name: 'prtaxid'},
    {name:'taxamount', defValue: 0},
    {name: 'amountDueOriginal', defValue: 0},
    {name: 'amountDueOriginalSaved', defValue: 0},
    {name: 'amountdue', defValue: 0}, 
    {name: 'tdsamount', defValue: 0},
    {name: 'enteramount', defValue: 0},
    {name: 'exchangeratefortransaction', defValue: 1},
    {name:'customfield'},
    {name: 'date'},
    {name: 'jeDate'},
    {name: 'accountid'},
    {name:'accountnames'},
    {name:'isClaimedInvoice'},
    {name:'RCMApplicable' ,type:'boolean',defValue:false},
    {name:'invType'},
    {name:'gstCurrencyRate',defValue:'0.0'},
    {name:'srNoForRow',defValue: 0},
    {name:'masterTypeValue', defValue: 0},
    {name:'isOneToManyTypeOfTaxAccount', defValue:false},
    {name:'appliedGst'},
    {name:'claimedDate'},
    {name:'rowdetailid'},
    {name: 'recTermAmount'},
    {name: 'LineTermdetails'},
    {name: 'productid'},
    {name: 'productname'},
    {name:'taxclass'},
    {name:'taxclasshistoryid'},
    {name:'isOpeningBalanceTransaction'},
    {name:'appliedTDS', type:'string'},
    {name: 'transactionAmount', defValue: 0},
    {name: 'srno', isForSequence:true},
    {name: 'amount', convert: function (value) {
            if (value === undefined || value === null || value === "") {
                value = 0;
            } else if (typeof value === "string") {
                value = parseFloat(value);
            }
            return isNaN(value) ? 0 : value;
        }},
    {name: 'discountname', defValue: 0},            //discount amount
    {name: 'discountvalue', convert: function (value) {
            if (value === undefined || value === null || value === "") {
                value = 0;
            } else if (typeof value === "string") {
                value = parseFloat(value);
            }
            return isNaN(value) ? 0 : value;
        }},
    {name: 'discounttype', convert: function (value) {
            if (value === undefined || value === null || value === "") {
                value = false;
            } else if (typeof value === "string") {
                value = value=="true";
            }
            return value;
        }},           //type=percentage or flat
    {name: 'applicabledays', convert: function (value) {
            if (value === undefined || value === null || value === "") {
                value = -1;
            } else if (typeof value === "string") {
                value = parseInt(value);
            }
            return isNaN(value) ? -1 : value;
        }},
    {name: 'grcreationdate', type: 'date'},
    {name: 'isDiscountFieldChanged', convert: function (value) {
            if (value === undefined || value === null || value === "") {
                value = false;
            } else if (typeof value === "string") {
                value = value == "true";
            }
            return value;
        }},
    {name: 'termid'},
    {name: 'amountdueafterdiscount', defValue: 0}
    ]);
    this.store = new Wtf.data.Store({
        url: Wtf.req.account + this.businessPerson + 'Manager.jsp',
        sortInfo: {
            field: 'srno',
            direction: 'ASC'
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.gridRec)
    });
    this.store.on('beforeload', function() {
        this.store.baseParams.includeFixedAssetInvoicesFlag = true;// in case of make/Receive Payments we need to include Fixed Asset Invoices also.
    }, this)

    this.typeStoreArray = this.getTypeStoreArray(this.paymentType);
    this.transactionTypeStore = new Wtf.data.SimpleStore({
        fields: [{
            name: 'id'
        }, {
            name: 'Type'
        }],
        data: this.typeStoreArray
    });
    /*
     *  used this.typeStore for against GL case only 
     */
    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{name: "id"}, {name: "name"}],
        data: [[true, "Debit"], [false, "Credit"]]
    });
    this.taxRec = Wtf.data.Record.create([
    {name: 'prtaxid',
     mapping:'taxid'},
    {name: 'prtaxname',mapping:'taxname'},
    {name: 'taxdescription'},
    {name: 'percent',type:'float'},
    {name: 'taxcode'},
    {name: 'accountid'},
    {name: 'accountname'},
    {name: 'hasAccess'},
    {name: 'applydate',type:'date'}

    ]);
    this.taxStore = new Wtf.data.Store({
            
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            autoLoad:false
        },this.taxRec),
        url : "ACCTax/getTax.do",        
        baseParams:{
            mode:33,
            moduleid:Wtf.Acc_Make_Payment_ModuleId,
            includeDeactivatedTax: this.isEdit != undefined ? (this.isCopyReceipt ? false : this.isEdit) : false
        }
    });
    this.taxStore.on("load", function(store){
        if (this.transTax && this.transTax.getValue() != undefined && this.transTax.el && this.transTax.el.dom && this.transTax.el.dom.value == "") {	//SDP-12753
            var storeNewRecord=new this.taxRec({
                prtaxid:'-1',
                prtaxname:'None'
            });
            this.transTax.store.insert(0, storeNewRecord);
        }
    },this);
//    this.taxStore.load();
    this.selectionModel = new Wtf.grid.CheckboxSelectionModel({
        singleSelect: false
    });
   
   this.selectionModel.on('selectionchange',function(){this.fireEvent('onselection',this);},this);
   this.selectionModel.on("beforerowselect", this.checkSelections,this);
    var columnArr = [];
    this.rowno = (this.isNote) ? new Wtf.grid.CheckboxSelectionModel() : new Wtf.grid.RowNumberer();
    this.enteramountfield = new Wtf.form.FinanceNumberField({
        allowBlank: false,
        value: 0,
        maxLength: 15, //
        decimalPrecision: Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
    });
            
    if (!this.readOnly) {
        if(!this.isNote) {
            columnArr.push(this.selectionModel);
        }
        columnArr.push(this.rowno);
    }
    //added sequence arrows - refer ticket ERM-216
    columnArr.push({
        header: WtfGlobal.getLocaleText("acc.invoice.lineItemSequence"), //"Sequence",
        width: 100,
        align: 'center',
        dataIndex: 'srno',
        name: 'srno',
        renderer: Wtf.applySequenceRenderer
    });

    columnArr.push({
        header: WtfGlobal.getLocaleText("acc.field.DocumentType"),
        dataIndex: 'type',
        editor: this.typeCombo = new Wtf.form.ComboBox({
            store: this.transactionTypeStore,
            name: 'typeCombo',
            hiddenName: 'type',
            displayField: 'Type',
            valueField: 'id',
            mode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            forceSelection: true,
            allowBlank: true,
            emptyText:WtfGlobal.getLocaleText("acc.mp.selectDocumentType")
        }),
        renderer: Wtf.comboBoxRenderer(this.typeCombo),
        width:125
    });
    //Will work for India Country & for others, only when payment type is against GL.
    if(this.paymentType==this.paymentOption.AgainstGL) {
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.je.type"), //"Type",
            editor: this.cmbType = new Wtf.form.ComboBox({
                hiddenName: 'debit',
                store: this.typeStore,
                valueField: 'id',
                displayField: 'name',
                mode: 'local',
                triggerAction: 'all',
                forceSelection: true
                //                hidden: Wtf.Countryid != Wtf.Country.INDIA || !(this.paymentType==this.paymentOption.AgainstGL)
            }),
            renderer: Wtf.comboBoxRenderer(this.cmbType),
            dataIndex: 'debit',
            width: 100
        }) 
   }

    columnArr.push({
        header: (!(this.paymentType==this.paymentOption.AgainstGL) ? WtfGlobal.getLocaleText("acc.field.DocumentNumber") : WtfGlobal.getLocaleText("acc.payMethod.acc")), //Account Code    //SDP-12753
        dataIndex: 'documentno',
        width:150,
        editor: this.documentList = new Wtf.form.TextField({
            name: 'documentno',
            emptyText:(!(this.paymentType==this.paymentOption.AgainstGL) ? WtfGlobal.getLocaleText("acc.mp.selectDocumentNumber") : WtfGlobal.getLocaleText("acc.payMethod.acc"))
        }),
        renderer: function (val, meta) {
            var regex = /(<([^>]+)>)/ig;
            val = val.replace(/(<([^>]+)>)/ig, "");
            var tip = val.replace(/"/g, '&rdquo;');
            meta.attr = 'wtf:qtip="' + tip + '"';
            return val;
        }
    },    
    {
        header: WtfGlobal.getLocaleText("acc.field.GSTApplied"),
        dataIndex: 'appliedGst',
        hidden: !(this.paymentType==this.paymentOption.AgainstGL) || Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA,
        renderer: this.gstRenderer.createDelegate(this),
        width:100
    },
    {
        header: WtfGlobal.getLocaleText("acc.invReport.desc"),
        dataIndex: 'description',
        width:150,
        editor: this.Description = new Wtf.form.TextArea({
            //maxLength: 1000,
            allowBlank: true,
            xtype: 'textarea'
    })
    })
    columnArr = WtfGlobal.appendCustomColumn(columnArr, GlobalColumnModel[config.moduleid]);
    var CustomtotalStoreCount = 0;
    var CustomloadedStoreCount = 0;
    
    if(Wtf.account.companyAccountPref.isMultiEntity){
        var entityArr = GlobalDimensionCustomFieldModel[6];// Purchase Invoice ModuleID
        var multiEntityObj = entityArr.getIemtByParam({
            isformultientity:true
        });
        if(multiEntityObj){
        multiEntityObj = JSON.clone(multiEntityObj);
        multiEntityObj.iseditable = false;
        columnArr = WtfGlobal.appendCustomColumn(columnArr, [multiEntityObj],true);
        delete multiEntityObj["fieldData"];
        if(this.isEdit){
            multiEntityObj.mapping = multiEntityObj.fieldname + "_linkValue";
        }
        WtfGlobal.updateStoreConfig([multiEntityObj], this.store);
        }
    }

    for (var j = 0; j < columnArr.length; j++) {
        if (columnArr[j].dataIndex.indexOf('Custom_') != -1 && (columnArr[j].fieldtype === 4 || columnArr[j].fieldtype === 7)) {
            CustomtotalStoreCount++;
            if(columnArr[j].editor){
                columnArr[j].editor.store.on('load', function() {
                    CustomloadedStoreCount++;
                    if (CustomtotalStoreCount === CustomloadedStoreCount) {
                        this.fireEvent('updatecustomdata', this);
                        this.getView().refresh();
                    }
                }, this)
            }
        }

    }
    
    if (this.paymentType != this.paymentOption.AgainstGL) {     //SDP-12753
        columnArr.push({
            header:WtfGlobal.getLocaleText("acc.field.transactionAmount"),
            dataIndex:'transactionAmount',
            hidelabel:false,
            hidden: false,
            width:150,
            renderer:WtfGlobal.withoutRateCurrencySymbolTransaction
    },
    {
            header:WtfGlobal.getLocaleText("acc.field.transactionAmountDue"),
            dataIndex:'amountDueOriginal',
            hidelabel:false,
            hidden: false,
            width:150,
            renderer:WtfGlobal.withoutRateCurrencySymbolTransaction
    });
    }    
    columnArr.push(
    {
            header:WtfGlobal.getLocaleText("acc.setupWizard.curEx"), 
            dataIndex:'exchangeratefortransaction',
            hidelabel:false,
            hidden: false,
            renderer:this.conversionFactorRenderer,
            width:125,
            editor: this.exchangeratefortransaction=new Wtf.form.NumberField({
                decimalPrecision:10,
                allowNegative : false,
                validator: function(val) {
                    if (val!=0) {
                        return true;
                    } else {
                        return false;
                    }
                }
            })
        });
        if (this.paymentType != this.paymentOption.AgainstGL) {     //SDP-12753
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.invoiceList.amtDue"),
            dataIndex: 'amountdue',
            align:'right',
            renderer:WtfGlobal.withoutRateCurrencySymbol
        });
    }
    /**
     * If discount on Payment term is enabled in company preferences adding discount and Amount due after discount colomn in payment grid.
     * ERM-981.
     */
       if (CompanyPreferenceChecks.discountOnPaymentTerms()) {
           this.discountEditor = new Wtf.form.NumberField({
                value: 0,
                maxLength: 15,
                allowNegative: false,
                decimalPrecision: Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
            });
            columnArr.push({
                header: WtfGlobal.getLocaleText("acc.field.discount"), //"Discount",
                align: 'right',
                dataIndex: 'discountname',
                editor: this.discountEditor,
                hidden: (this.paymentType === this.paymentOption.AgainstGL || this.paymentType === this.paymentOption.AgainstCustomer),
                renderer: function (value, m, rec) {
                    if ((value == undefined || value == "") && (rec.discountname == 0 || rec.discountname == undefined)) {
                        value = 0;
                    }
                    return WtfGlobal.withoutRateCurrencySymbol(value, m, rec);
                }
            }, {
                header: WtfGlobal.getLocaleText("acc.receiptpayment.amountdueafterdiscount"), //"Amount Due after Discount",
                align: 'right',
                dataIndex: 'amountdueafterdiscount',
                hidden: (this.paymentType === this.paymentOption.AgainstGL || this.paymentType === this.paymentOption.AgainstCustomer),
                renderer: function (value, m, rec) {
                    if (value == undefined || value == "") {
                        value = 0;
                    }
                    return WtfGlobal.withoutRateCurrencySymbol(value, m, rec);
                }
            });
        }
        columnArr.push({
            header:WtfGlobal.getLocaleText("acc.taxReport.taxCode"),  //"Tax Code",     //SDP-12753
            dataIndex:"prtaxid",       
            hidelabel: (Wtf.account.companyAccountPref.isLineLevelTermFlag && WtfGlobal.isIndiaCountryAndGSTApplied()),
            hidden: (Wtf.account.companyAccountPref.isLineLevelTermFlag && WtfGlobal.isIndiaCountryAndGSTApplied()),
            editor:this.transTax= new Wtf.form.ExtFnComboBox({
                hiddenName:'prtaxid',
                store:this.taxStore,
                id:"paymenttaxcmb"+this.id,
                valueField:'prtaxid',
                forceSelection: true,
                displayField:'prtaxname',
                scope:this,
                //listClass:'tax-combo-list',	//ERP-37005 :  Drop Down is not squeezing as per the no of taxes loading
                displayDescrption:'taxdescription',                
                autoScroll: true,
                //mode: this.isEdit?'local':'remote',	//ERP-37005 - Type ahead is not working in case of edit and Copy cases
		mode:'remote',
                typeAhead: true,   
                minChars:0,
                selectOnFocus:true,
		addCreateNewRecord:false,
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
            }),
            renderer:Wtf.comboBoxRenderer(this.transTax),
            width:100
        },
        {
            header:WtfGlobal.getLocaleText("acc.taxReport.taxAmount"),
            dataIndex:"taxamount",
            hidelabel: (Wtf.account.companyAccountPref.isLineLevelTermFlag && WtfGlobal.isIndiaCountryAndGSTApplied()),
            hidden: (Wtf.account.companyAccountPref.isLineLevelTermFlag && WtfGlobal.isIndiaCountryAndGSTApplied()),
            editor:this.transTaxAmount=new Wtf.form.NumberField({
                allowBlank: true,
                allowNegative: false,
                decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
                value:0
            }),
            renderer:this.setTaxAmount.createDelegate(this),
            width:100
        },        
        {
            header: '<b>'+WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt")+'</b>',
            dataIndex: 'enteramount',                                      // Amount which is to be paid[Create new case] / which is paid[Edit case]
            align:'right',
            editor:(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && this.AllowToEditCopy==false)?'':(this.isAllowedSpecificFields?'':this.enteramountfield),
            renderer: WtfGlobal.amountWithoutCurrencyRender,
            width:125
        }
    );
    if(Wtf.Countryid == Wtf.Country.INDIA && Wtf.isTDSApplicable && this.paymentType==this.paymentOption.AgainstVendor){
        columnArr.push(        {
            header: "TDS Calculation", 
            align: 'center',
            width: 40,
            renderer: this.addRenderer.createDelegate(this),
            hidden:  (Wtf.Countryid != Wtf.Country.INDIA || !Wtf.isTDSApplicable || this.paymentType!=this.paymentOption.AgainstVendor)
        },{
            header: WtfGlobal.getLocaleText("acc.invoice.gridTDSAmt"),
            dataIndex: 'tdsamount', 
            fixed:true,
            hidden: (Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable || this.paymentType == this.paymentOption.AgainstCustomer),
            editor:this.tdsamount = new Wtf.form.NumberField({
                allowNegative: false,
                value:0,
                decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL  
            }),
            renderer: this.setTDSAmount.createDelegate(this),
            width:100
        })
    }
    if (Wtf.account.companyAccountPref.RCMApplicable != undefined && Wtf.account.companyAccountPref.RCMApplicable && Wtf.account.companyAccountPref.isLineLevelTermFlag && WtfGlobal.isIndiaCountryAndGSTApplied()) {
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.invoicegrid.TaxAmount"), //"Total Tax Amount",
            dataIndex: "recTermAmount",
            align: 'right',
            width: 100,
            renderer: WtfGlobal.withoutRateCurrencySymbol
        }, {
            header: WtfGlobal.getLocaleText("acc.invoicegrid.tax"),
            align: 'center',
            width: 40,
            dataIndex: "LineTermdetails",
            renderer: this.lineTermRenderer.createDelegate(this)
        });
    }    
    columnArr.push(        {
        header: WtfGlobal.getLocaleText("acc.invoice.gridAction"), //"Action",
        align: 'center',
        width: 55,
        renderer: this.deleteRenderer.createDelegate(this)
    });
        
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.edit)){
        this.transTax.addNewFn=this.addTax.createDelegate(this);
    }
    /*
     * Adding Product Name column in the grid for indian subdomain only ERM-1016
     */
    if(WtfGlobal.isIndiaCountryAndGSTApplied()){
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), //"Product Name",
            dataIndex: 'productname',
            id: "productname" + this.id
        });
    }       
    this.cm = new Wtf.grid.ColumnModel(columnArr);
   this.sm=this.selectionModel;
//    if(this.isEdit){
//            this.taxStore.load();
//    }
    this.summary = new Wtf.ux.grid.GridSummary();
    Wtf.apply(this, {
        store: this.store,
        stripeRows: true,
        cm: this.cm
    });
   
    Wtf.account.OSDetailGridNew.superclass.constructor.call(this, config);
    this.addEvents({
        'datachanged': true,
        'onselection':true,
        'updatecustomdata':true
    });
    var colModelArray = GlobalColumnModel[this.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray, this.store);
    this.on('rowclick', this.handleRowClick, this);
    this.on('cellclick', this.RitchTextBoxSetting, this);
    this.on('afteredit',this.fireAmountChange,this);
    this.on('beforeedit', this.beforeGridCellEdit, this);
    this.on('populateDimensionValue',this.populateDimensionValueingrid,this);
    if(this.isBulkPayment){
        this.addSelectedInvoicesForBulkPayment(this.invObj.getSelectedRecords());
    }
    this.enteramountfield.on('focus', function (field) {
        this.enteramountfield.selectText(); //ERP-37005 : "Enter amount" should be empty on one back space
    }, this);
}
 
Wtf.extend(Wtf.account.OSDetailGridNew, Wtf.grid.EditorGridPanel, {
    clicksToEdit: 1,    
    disabledClass: "newtripcmbss",
    layout: 'fit',
    viewConfig: {
        forceFit: false,
        emptyText: WtfGlobal.getLocaleText("acc.field.Norecordstodisplay.")
    },
    onRender: function(config) {
        Wtf.account.OSDetailGridNew.superclass.onRender.call(this, config);
        if (this.paymentType == this.paymentOption.AgainstGL) {              //    ERP-15992
            this.addBlankRow();
        }
        if (this.isAllowedSpecificFields) {
            this.disableField();
        }
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && this.AllowToEditCopy==false){
            this.disableField();
        }
        this.hideShowCustomizeLineFields();
    },
     checkSelections:function( scope, rowIndex, keepExisting, record){
        if(rowIndex== (this.store.getCount()-1)){
            return false;
        }else{
            return true;
        }
       
    },
    deleteRenderer: function(v, m, rec) {
        return "<div class='" + getButtonIconCls(Wtf.etype.deletegridrow) + "'></div>";
    },
    addRenderer: function(v, m, rec) {
        return "<div class='" + getButtonIconCls(Wtf.etype.addtdsgrid) + "'></div>";
    },
    lineTermRenderer: function (v, m, rec) {
        return getToolTipOfTermsfun(v, m, rec);
    },
    disableField: function() {
        this.typeCombo.disable();
        this.documentList.disable();
        this.transTaxAmount.disable();
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.isTDSApplicable && this.tdsamount){
            this.tdsamount.disable();
        }
        this.transTax.disable();
        this.exchangeratefortransaction.disable();
        if (this.cmbType) {
            this.cmbType.disable();
        }
    },
    addSelectedInvoicesForBulkPayment : function(jsonArray) {
        this.addSelectedRecords(jsonArray, 2);
    },
    hideShowCustomizeLineFields:function(){ 
        if(this.moduleid==Wtf.Acc_Make_Payment_ModuleId){
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
                            /*
                             *applied gst field is only display while make payment against GL
                             */
                            
                            if(cm.config[j].dataIndex==action.data[i].fieldDataIndex ){
                                if(action.data[i].fieldDataIndex=='appliedGst'){
                                    if(!(this.paymentType==this.paymentOption.AgainstGL) || Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                                        cm.setHidden(j,true);       
                                        continue;
                                    }
                                }
//                                if(action.data[i].fieldDataIndex=='debit'){
//                                    if(Wtf.Countryid != Wtf.Country.INDIA || !(this.paymentType==this.paymentOption.AgainstGL)){
//                                        cm.setHidden(j,true);       
//                                        continue;
//                                    }
//                                }
                                if(action.data[i].fieldDataIndex=='tdsamount'){
                                    if((Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA ||  !Wtf.isTDSApplicable || this.paymentType == this.paymentOption.AgainstCustomer)){
                                        cm.setHidden(j,true);       
                                        continue;
                                    }
                                }
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
    beforeGridCellEdit: function(e) {
        if(this.readOnly) {
            e.cancel=true;
            return;
        }
         if(Wtf.Countryid == Wtf.Country.INDIA  && e.field=="debit" && e.record.data.type!=this.GLType){
             e.cancel = true;
             return;
        }
        if(CompanyPreferenceChecks.discountOnPaymentTerms() && e.field == "discountname" && e.record.data.type!=2){
            e.cancel = true;
             return;
        }
        if ((this.isAllowedSpecificFields) && (e.field == "documentno" || e.field == "type" || e.field == "taxamount" || e.field=="prtaxid" || e.field=="exchangeratefortransaction" ) ) {
            e.cancel = true;
        }
        if(this.paymentType!=this.paymentOption.AgainstGL && this.paymentEntryObject.Name.getValue()=='' )
        {
            e.cancel=true;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), this.isCustomer?WtfGlobal.getLocaleText("acc.mp.selectCustFirst"):WtfGlobal.getLocaleText("acc.mp.selectVenFirst")], 2);// 'Select Customer/Vendor first'
        } else if(this.paymentEntryObject.pmtMethod.getValue()==''){
            e.cancel=true;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.selectPmtMethodFirst")], 2);// 'Select Payment method first''
        } else if (e.field == "documentno" && !(this.isAllowedSpecificFields)) {
            e.cancel = true;            
            if(e.record.data.type==''){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mp.selectDocTypeFirst")], 2);
                return;
            }
            if (e.record.data.type != "")
            this.openDocumentWindow(e);            
            return;
        
        } else if(Wtf.Countryid == Wtf.Country.INDIA && e.field=='tdsamount' ){            
            e.cancel = true;
            return;
        } else if(e.field=='enteramount' && !(this.isAllowedSpecificFields)){            
            if(e.value==""){
                e.record.set('enteramount',0);
            }
            if(e.record.data.type=='' && e.row === (e.grid.getStore().getCount()-1)){  // checked for blank row
                e.cancel=true;
                return;
            }
            if(e.record.data.type==''){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mp.selectDocTypeFirst")], 2);
                e.cancel=true;
                return;
            }
            /*
             *  If Refund/Deposit mode or Other than AdvanceType then documentno is mandatory field
             */
            if(e.record.data.documentno=='' && e.row === (e.grid.getStore().getCount()-1)) {
                e.cancel=true;
                return;
            }
            
            if(e.record.data.documentno==''){
                if(e.record.data.type != this.ADVType) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mp.selectDocNoFirst")], 2);
                    e.cancel=true;
                    return;
                } else if(e.record.data.type == this.ADVType && this.isAdvanceTypeOfRefundMode(e.record.data.type)) {
                    if(this.searchBlankDocumentNoRecord()>1) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mp.selectDocNoFirst")], 2);
                        e.cancel=true;
                        return;
                    }
                }
            }
        
        } else if(e.field=='exchangeratefortransaction'){
            if(!this.isExchangeRateEditableForSelectedDocumentType(e))
                e.cancel=true;
        } else if(e.field=='prtaxid'){
            if (Wtf.account.companyAccountPref.countryid === Wtf.CountryID.MALAYSIA) {
                var isTaxShouldBeEnable = WtfGlobal.isTaxShouldBeEnable(new Date(this.parentObj.creationDate.getValue()).clearTime());
                if (!isTaxShouldBeEnable) {
                    e.cancel = true;
                    return;
                }
            }
            if(Wtf.Countryid == Wtf.Country.INDIA && (this.parentObj!=undefined && this.parentObj.taxPaymentDataParams!=undefined && this.parentObj.taxPaymentDataParams.excisePaymentFlag)){
                e.cancel = true;
                return;
            }
            if(!this.isTaxEditableForSelectedDocumentType(e)){
                e.cancel=true;
            } else if(e.record.data.documentno==''){         // Restict user from selecting tax if Account is not selected.
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mp.selectDocNoFirst")], 2);
                e.cancel=true;
                return;
            }
        } else if(e.field=='taxamount'){
            if (Wtf.account.companyAccountPref.countryid === Wtf.CountryID.MALAYSIA) {
                var isTaxShouldBeEnable = WtfGlobal.isTaxShouldBeEnable(new Date(this.parentObj.creationDate.getValue()).clearTime());
                if (!isTaxShouldBeEnable) {
                    e.cancel = true;
                    return;
                }
            }
            if(Wtf.Countryid == Wtf.Country.INDIA && (this.parentObj!=undefined && this.parentObj.taxPaymentDataParams!=undefined && this.parentObj.taxPaymentDataParams.excisePaymentFlag)){
                e.cancel = true;
                return;
            }
            if(!this.isTaxEditableForSelectedDocumentType(e)){
                e.cancel=true;
            } else if(e.record.data.documentno==''){         // Restict user from entering tax amount if Account is not selected.
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mp.selectDocNoFirst")], 2);
                e.cancel=true;
                return;
            }
        }
    },
    
    searchBlankDocumentNoRecord : function() {
        var noOfRecords = 0;
        var totalRec = this.store.getCount();
        for(var cnt=0; cnt< totalRec; cnt++) {
            var rec = this.store.getAt(cnt);
            if(rec.data.type!='' && rec.data.documentno=='') {
                noOfRecords++;
            }
        }
       return noOfRecords;
    },
    
    fireAmountChange: function(obj) {
        if (obj.field == 'exchangeratefortransaction') {
            var rec = obj.record.data;
            var amountDueOriginal = 0;
            var exchangeRate = 0;
            amountDueOriginal = parseFloat(rec.amountDueOriginal);
            exchangeRate = rec.exchangeratefortransaction;
            var discount=0.0;
            if (exchangeRate != '') {
                obj.record.set("amountdue", getRoundedAmountValue(amountDueOriginal * exchangeRate));
                if (rec.amount == rec.amountDueOriginal) {
                    discount = this.calculateDiscount(rec);
                    if (getRoundedAmountValue(discount) > getRoundedAmountValue(amountDueOriginal * exchangeRate)) {
                        discount = getRoundedAmountValue(amountDueOriginal * exchangeRate)
                    }
                }
                obj.record.set("discountname", getRoundedAmountValue(discount));
                obj.record.set("amountdueafterdiscount", getRoundedAmountValue((amountDueOriginal * exchangeRate)) - discount);
            }
                obj.record.set("enteramount",0);
            /*
             * As per discussion held in SDP-16191, no need of gstCurrencyRate pop up
             * after changing exchange rate.
             */
//        if(obj.field=="exchangeratefortransaction"&&WtfGlobal.singaporecountry()&&WtfGlobal.getCurrencyID()!=Wtf.Currency.SGD){
////        if(obj.field=="exchangeratefortransaction"&&this.singaporeIdFlag&&obj.record.data.type==this.INVType){
//                callGstCurrencyRateWin(this.id,WtfGlobal.getCurrencyName()+" ",obj,obj.record.get("gstCurrencyRate")*1);//obj.record.get("currencysymboltransaction")+" "
//        }//option to give GST rate if the transaction rate is changed 
        }
        else if (obj.field == 'enteramount') {
            /*
             *  Allow negative amount in against GL case only
             */
            if(obj.value<0 && obj.record.data.type!=this.GLType){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.amountCanNotBeNegative")], 2)
                obj.record.set('enteramount',obj.originalValue);
                this.fireEvent('datachanged',this);
                return;
            }
            
            if(Wtf.Countryid == Wtf.Country.INDIA && obj.originalValue != obj.record.get("enteramount")){
                obj.record.set("appliedTDS","");
                obj.record.set("rowdetailid","");
                obj.record.set("tdsamount",0);
            }
            /*
             *  Check for entered amount. It should not be greater than amount due. Applicable for CN/DN case
             */
            if(obj.record.data.enteramount > obj.record.data.amountdue && 
                    (obj.record.data.type==this.NoteType ||
                    (this.isAdvanceTypeOfRefundMode(obj.record.data.type) && obj.record.data.documentid!=='')))
            {
                var amountDueOf="";
                if(obj.record.data.type==this.NoteType){
                    amountDueOf=this.isCustomer?WtfGlobal.getLocaleText("acc.mp.ofDebitNote"):WtfGlobal.getLocaleText("acc.mp.ofCreditNote");
                }
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Amountenteredcannotbegreaterthan")+WtfGlobal.getLocaleText("acc.customerList.gridAmountDue")+" "+amountDueOf], 2)
                if (CompanyPreferenceChecks.discountOnPaymentTerms() && obj.record.data.discountname != undefined && obj.record.data.discountname > 0.0) {
                    if (this.receiptEntryObject.LifoFifoMode) {
                        obj.record.set('enteramount', obj.originalValue); // In Lifo-Fifo mode, originaly populated value will be set to enter amount field
                    } else {
                        obj.record.set('enteramount', getRoundedAmountValue(obj.record.data['amountdue'] - obj.record.data['discountname']));
                        obj.record.set('amountdueafterdiscount', getRoundedAmountValue(obj.record.data['amountdue'] - obj.record.data['discountname']));
                    }
                } else {
                    if (this.paymentEntryObject.LifoFifoMode) {
                        obj.record.set('enteramount', obj.originalValue); // In Lifo-Fifo mode, originaly populated value will be set to enter amount field
                    } else {
                        obj.record.set('enteramount', obj.record.data['amountdue']);         // obj.originalValue will be set automatically
                    }
                }
                this.fireEvent('datachanged',this);
                return;
            }
            /*
             *  Check for entered (amount+discount). It should not be greater than amount due. Applicable for Invoice only
             */
            if (CompanyPreferenceChecks.discountOnPaymentTerms() && obj.record.data.discountname != undefined && obj.record.data.discountname > 0.0) {
                var amountDueOf = "";
                if (((obj.record.data.enteramount + obj.record.data.discountname) > obj.record.data.amountdue) && (obj.record.data.type == this.INVType)) {
                    amountDueOf = WtfGlobal.getLocaleText("acc.mp.ofInvoice");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.receiptpayment.amountdueafterdiscounterrormsg") + " " + amountDueOf], 2)
                    if (this.paymentEntryObject.LifoFifoMode) {
                        obj.record.set('enteramount', obj.originalValue); // In Lifo-Fifo mode, originaly populated value will be set to enter amount field
                    } else {
                        obj.record.set('enteramount', getRoundedAmountValue(obj.record.data['amountdue'] - obj.record.data['discountname']));
                        obj.record.set('amountdueafterdiscount', getRoundedAmountValue(obj.record.data['amountdue'] - obj.record.data['discountname']));
                    }
                    this.fireEvent('datachanged', this);
                    return;
                } 
                /**
                 * Commenting below code because as per new requirement in ERM-981 we need to allow user to add discount in case of partial payment.
                 */
//                else if (obj.record.data.enteramount != obj.record.data.amountdueafterdiscount) {
//                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.makepayment.discountresetmessage"), function (btn) {
//                        if (btn == "yes") {
//                            obj.record.set('discountname', 0);
//                            obj.record.set('amountdueafterdiscount', getRoundedAmountValue(obj.record.data['amountdue']));
//                            this.fireEvent('datachanged', this);
//                        } else {
//                            obj.record.set('enteramount', obj.originalValue);
//                        }
//                    }, this);
//                    return;
//                }
            } else if (obj.record.data.enteramount > obj.record.data.amountdue && (obj.record.data.type == this.INVType)) {
                var amountDueOf = "";
                amountDueOf = WtfGlobal.getLocaleText("acc.mp.ofInvoice");
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Amountenteredcannotbegreaterthan") + WtfGlobal.getLocaleText("acc.customerList.gridAmountDue") + " " + amountDueOf], 2)
                if (this.paymentEntryObject.LifoFifoMode) {
                    obj.record.set('enteramount', obj.originalValue); // In Lifo-Fifo mode, originaly populated value will be set to enter amount field
                } else {
                    obj.record.set('enteramount', getRoundedAmountValue(obj.record.data['amountdue']));         // obj.originalValue will be set automatically
                }
                this.fireEvent('datachanged', this);
                return;
            } else if (!CompanyPreferenceChecks.discountOnPaymentTerms() || obj.record.data.discountvalue == "") {
                obj.record.set('discountvalue', 0);
            }
            var val=0; 
            var taxamount=0;
            val=getRoundedAmountValue(obj.record.get("enteramount"));
            
            if(obj.record.get("prtaxid")!=undefined && obj.record.get("prtaxid")!='-1'){
                taxamount= this.calTaxAmount(obj.record);
                var taxRoundAmount=getRoundedAmountValue(taxamount);
                val+=taxRoundAmount;
                if(val!==0 && (obj.record.get("enteramount")!=undefined || obj.record.get("prtaxid")!=""))
                {
                    obj.record.set("taxamount",taxRoundAmount);
                    obj.record.set("prtaxid",obj.record.get("prtaxid"));
                }
            }
            if (WtfGlobal.isIndiaCountryAndGSTApplied() && obj.record.data.type == this.ADVType && this.productid != undefined && !this.isCustomer && this.parentObj!=undefined && this.parentObj.rcmApplicable.getValue()) {
                getLineTermDetailsAndCalculateGSTForAdvance(this.parentObj, this, this.productid);
            } else if(WtfGlobal.isIndiaCountryAndGSTApplied() && this.isAdvanceTypeOfRefundMode(obj.record.data.type)) {
                var LineTermdetails = obj.record.data.LineTermdetails;
                LineTermdetails = eval(LineTermdetails);
                var termStore =calculateTermLevelTaxesForPayment(LineTermdetails, obj.record, undefined, true);
                obj.record.set("LineTermdetails",Wtf.encode(termStore));
                this.fireEvent('datachanged', this);
            }
        } else if (obj.field == "discountname") {
            /*
             *  Calculating Enter amount when user manually changes the discount field enter amount = (amountdue of invoice - entried discount amount)
             */
            if (CompanyPreferenceChecks.discountOnPaymentTerms() && obj.record.data.discountname != undefined && obj.record.data.discountname > 0.0) {
                var amountDueOf = "";
                var isDiscountFieldChanged = false;
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.bulkpayment.discountresetmessage"), function (btn) {
                    if (btn == "yes") {
                        isDiscountFieldChanged = true;
                        if ((obj.record.data.discountname > obj.record.data.amountdue) && (obj.record.data.type == this.INVType)) {
                            amountDueOf = WtfGlobal.getLocaleText("acc.mp.ofInvoice");
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.receiptpayment.amountdueafterdiscounterrormsginbulk") + " " + amountDueOf], 2);
                            obj.record.set('discountname', obj.originalValue);
                        } else if (((obj.record.data.enteramount + obj.record.data.discountname) > obj.record.data.amountdue) && (obj.record.data.type == this.INVType)) {
//                            amountDueOf = WtfGlobal.getLocaleText("acc.mp.ofInvoice");
//                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.receiptpayment.amountdueafterdiscounterrormsginbulk") + " " + amountDueOf], 2);
                            var enterAmt = (obj.record.data['amountdue'] - obj.record.data['discountname']) > 0 ? (obj.record.data['amountdue'] - obj.record.data['discountname']) : 0.0;
                            obj.record.set('enteramount', getRoundedAmountValue(enterAmt));
                            obj.record.set('amountdueafterdiscount', getRoundedAmountValue(enterAmt));
                        } else {
                            obj.record.set('enteramount', getRoundedAmountValue(obj.record.data['amountdue'] - obj.record.data['discountname']));
                            obj.record.set('amountdueafterdiscount', getRoundedAmountValue(obj.record.data['amountdue'] - obj.record.data['discountname']));
                        }
                        obj.record.set('isDiscountFieldChanged', isDiscountFieldChanged);
                        this.fireEvent('datachanged', this);
                        return;
                    } else {
                        obj.record.set('discountname', obj.originalValue);
                        obj.record.set('isDiscountFieldChanged', isDiscountFieldChanged);
                    }
                }, this);
            }
        }
        else if (obj.field == "type") {
            var index=-1;
            /*
             *  System allow single record of Advance type. Block for second entry.
             *  In Refund/Deposite mode, we can't entry with blank documentid
             */
            if(obj.field=='type' && obj.value==this.All){
                var AddAllDocuments=true;
                this.addAllDocuments(AddAllDocuments,obj.row); 
            }
            
            if(obj.value==this.ADVType && !this.isAdvanceTypeOfRefundMode(obj.value)){
                /*
                 *  In Refund/Deposite type, we can allow only single entry for Advance Type with blank documentid 
                 */
//                if(this.paymentType==this.paymentEntryObject.paymentOption.AgainstCustomer) {
//                    index = WtfGlobal.searchRecordIndex(this.store, obj.record.data.documentid, "documentid");
//                } else {
                    index = WtfGlobal.searchRecordIndex(this.store, obj.value, "type");
//                }
            } 
            if(index!=-1 && index!=obj.row){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.pmtOfAdvTypeAlreadySelected")],2);
                var selectedRow=obj.row;
                this.store.remove(this.store.getAt(selectedRow));
                this.addBlankRow();
                return;
            }
            /*
             * 1. For Advance / All, we do not select Document / Account Code, so focus will move to description
             * 2. !this.isAdvanceTypeOfRefundMode(obj.record.data.type) : If type=Refund/Deposit and MP against Customer then we do not allow to focus on Description. So removed this check.
            */
//            if((obj.value==this.ADVType || obj.value==this.All)) {
//                this.startEditing(obj.row, WtfGlobal.getColumnIndex(this,"enteramount"));
              this.startEditing(obj.row, this.getColumnModel().findColumnIndex("description"));
//            }
            this.setOtherFieldsBlankForRow(obj);
        } else if(obj.field=='prtaxid'){
            var val=0;
                var taxamount=0;
                val=getRoundedAmountValue(obj.record.get("enteramount"));
                if(obj.record.get("prtaxid")!=undefined && obj.record.get("prtaxid")!='-1'){
                    taxamount= this.calTaxAmount(obj.record);
                }
                var taxRoundAmount=getRoundedAmountValue(taxamount*1);
                val+=taxRoundAmount;
                val= (getRoundedAmountValue(val)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
                if(val!==0 && (obj.record.get("enteramount")!=undefined || obj.record.get("prtaxid")!="" ) && obj.record.get("prtaxid")!='-1')
                {
                    obj.record.set("taxamount",taxRoundAmount);
                    obj.record.set("prtaxid",obj.record.get("prtaxid"));
                }
                if(obj.record.get("prtaxid")!=undefined && obj.record.get("prtaxid")=='-1'&& obj.record.get("enteramount")!=undefined){
                    obj.record.set("taxamount",taxRoundAmount);
                    //If tax is selected as none then appliedGst reset to blank
                    obj.record.set("appliedGst","");
                }
                if(obj.record.get("prtaxid")!=undefined && obj.record.get("prtaxid")!='-1'){
                    var newGst = obj.record.get("prtaxid");
                    obj.record.set("appliedGst",newGst)
                }
        } else if(obj.field=='taxamount'){
            var val=0;
            var taxamount=0;
            val=getRoundedAmountValue(obj.record.get("enteramount"));          
            var taxRoundAmount=getRoundedAmountValue(obj.value);
            val+=taxRoundAmount;
            val= (getRoundedAmountValue(val)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);		
            if(obj.record.get("prtaxid")!= undefined && val!==0 && (obj.record.get("enteramount")!=undefined || obj.record.get("prtaxid")!="" ) && obj.record.get("prtaxid")!='-1')
            {
                obj.record.set("taxamount",taxRoundAmount);
            }
        }else if(obj.field=='Custom_Product Tax Class' && (Wtf.account.companyAccountPref.isLineLevelTermFlag && WtfGlobal.isIndiaCountryAndGSTApplied())){
            this.hsncode=obj.value;
            getLineTermDetailsAndCalculateGSTForAdvance(this.parentObj, this, obj.value);  

        }
        if(obj.record.data.type==this.ADVType && this.store.getCount()-1==obj.row) // If edited row is last row of the grid. (Obj.row gives index of the selected row). If payment is advance then add blank row.
        {
            this.addBlankRow();
        }
        this.fireEvent('datachanged',this);
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
            if(fieldName == "description"){
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
                var val=record.data.description;
//                val = val.replace(/(<([^>]+)>)/ig,""); // Just comment this line to fix ERP-8675
                this.prodDescTextArea.setValue(val);
                if((record.data.documentid != undefined && record.data.documentid != "") || (record.data.type === 1)){//allowed to edit description in advanced/Deposit Document Type
                    var descWindow=Wtf.getCmp(this.id+'DescWindow')
                    if(descWindow==null){
                        var win = new Wtf.Window
                        ({
                            width: 560,
                            height:310,
                            title:WtfGlobal.getLocaleText("acc.gridproduct.discription"),
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
                                    record.set('description',  Wtf.get('descriptionRemarkTextAreaId').getValue());
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
    handleRowClick: function(grid, rowindex, e) {
        /*--Code will execute if we move sequence Up or Down -------- */
        if (!this.readOnly && e.target.className == "pwndBar2 shiftrowupIcon") {
            moveSelectedRowFormasterItems(grid,0,rowindex);
        }
        if (!this.readOnly && e.target.className == "pwndBar2 shiftrowdownIcon") {
            moveSelectedRowFormasterItems(grid,1,rowindex);
        }
        
        if (this.readOnly) {
            /*
             * If TDS check is ON (TDS check is ON only for INDIA Subdomain) and View case 
             * then enable to click TDS details Cell )
             * 
             */
            if (Wtf.isTDSApplicable && e.getTarget(".tdsCalc-gridrow")) { 
                //This will allow icon to be clicked by user
            }else{
                e.cancel = true;
                return;
            }
        }
        if (e.getTarget(".delete-gridrow") && !(this.isAllowedSpecificFields)) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn) {
                if (btn != "yes")
                    return;
                var store = grid.getStore();
                var total = store.getCount();
                store.remove(store.getAt(rowindex));
                this.getView().refresh();
                if (rowindex == total - 1) {
                    this.addBlankRow();
                }
                            this.fireEvent('datachanged',this);
            //                this.fireEvent('productdeleted',this);
            }, this);
        } else if(e.getTarget(".serialNo-gridrow")){
            var gridStore= grid.getStore();
            var record = gridStore.getAt(rowindex);
            if(record.data['masterTypeValue']==Wtf.masterTypeValueOfAccount['GSTTypeAccount'])
            {
               this.openWindowForSelectingGST(record,rowindex);                 
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.warn.msg")], 3);  //ERP-37005
            }
        }else if(e.getTarget(".tdsCalc-gridrow")){
            var gridStoreDetails= grid.getStore();
            var jsonrecord = gridStoreDetails.getAt(rowindex);
            if (jsonrecord.data['type'] != this.ADVType) {// TDS Calculation will happen only in case of Make Payment To Vendor against Advance.
                return false;
            }
            if(jsonrecord.data['type']!=""){
                if(jsonrecord.data['enteramount']!="0"){
                    if(!Wtf.isEmpty(this.paymentEntryObject.getPersonInformation())){
                        if(this.paymentEntryObject.getPersonInformation().isTDSapplicableonvendor != undefined && !this.paymentEntryObject.getPersonInformation().isTDSapplicableonvendor){  //Is TDS Applicable on Vendor or not
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.vendorpaymentcontrollercmn.TDSIsNotAppliedForSelectedVendor")], 2);
                        }else if(Wtf.isEmpty(this.paymentEntryObject.getPersonInformation().deducteetype)){  //Vendor's Deductee type
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.Deducteetypeisnotset")], 2);
                        }else if(Wtf.isEmpty(this.paymentEntryObject.getPersonInformation().residentialstatus)){   //Vendor belongs to Residential/Non-Residentia
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.Residentialstatusisnotset")], 2);
                        }else if(Wtf.isEmpty(this.paymentEntryObject.getPersonInformation().natureOfPayment)){   //Vendor belongs to Residential/Non-Residentia
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.NatureOfPaymentisnotset")], 2);
                        }else{ // Set Active Date Range(In preferences)
                            if(jsonrecord.data['type'] == this.ADVType && !this.readOnly){
                                Wtf.MessageBox.confirm("Advanced / Deposit", "Do you want to calculate TDS on Advance ?", function(btn) {
                                    if (btn != "yes"){
                                        return;
                                    }
                                    this.openWindowForSelectingTDS(jsonrecord, rowindex);
                                }, this);
                            } else {
                                this.openWindowForSelectingTDS(jsonrecord, rowindex);
                            }
                        }
                    } 
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Please Enter The Amount"], 2);
                }                
            }
    } else if (e.getTarget(".termCalc-gridrow")) {
            if (WtfGlobal.isIndiaCountryAndGSTApplied() && Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                this.showTermWindow(grid.getStore().getAt(rowindex), grid, rowindex);
            } else {
                return;
            }
        }
    },
    showTermWindow: function (record, grid, rowindex) {
//        return;
        var venderDetails = WtfGlobal.searchRecord(this.parentObj.Name.store, this.parentObj.Name.getValue(), 'accid');
        if (rowindex != -1) {
            this.TermGrid = new Wtf.account.TermSelGrid({
                id: 'TermSelGrid',
                isReceipt: false,
                border: false,
                layout: "fit",
                width: 900,
                height: 500,
                rowindex: rowindex,
                autoScroll: true,
                cls: 'gridFormat',
                region: 'center',
                viewConfig: {
                    forceFit: true
                },
                isEdit: this.isEdit,
                isLineLevel: true,
                invAmount: record.data.amount,
                parentObj: this.parentObj,
                isGST: this.isGST,
                gridObj: this,
                record: record,
                currencySymbol: this.symbol,
                venderDetails: venderDetails,
                scope: this
            });
            this.Termwindow = new Wtf.Window({
                modal: true,
                id: 'termselectionwindowtest',
                title: WtfGlobal.getLocaleText("acc.invoicegrid.TaxWindowTitle"),
                buttonAlign: 'right',
                border: false,
                layout: "fit",
                width: 900,
                height: 510,
                resizable: false,
                items: [this.TermGrid],
                buttons:
                        [{
                                text: 'Save',
                                iconCls: 'pwnd save',
                                hidden: true,
                                scope: this,
                                handler: function ()
                                {
                                    this.BeforeTermSave();
                                    this.Termwindow.close();
                                }
                            }, {
                                text: 'Close',
                                scope: this,
                                handler: function ()
                                {
                                    this.Termwindow.close();
                                }
                            }]
            });
            this.Termwindow.show();
        }
    },
    openDocumentWindow: function(event) {
        var paymentAgainst = event.record.data.type;
        this.selectedRow = event.row;
        var isMulticurrency = this.paymentEntryObject.isMultiCurrencyPayment();
        switch (paymentAgainst) {
            
            case this.ADVType : // Advance Payment
                if(this.isAdvanceTypeOfRefundMode(paymentAgainst)) {// Refund/Deposite Payment against Customer
                    this.advancePaymentWindow= new Wtf.account.AdvancePaymentWindow({
                        id:'advancepaymentwindow',
                        isReceipt: false,
                        title:WtfGlobal.getLocaleText("acc.rp.recPayAdv"),
                        border: false,
                        isCustomer: this.isCustomer,
                        currencyfilterfortrans: this.paymentEntryObject.Currency.getValue(),
                        personInfo:this.paymentEntryObject.getPersonInformation(),
                        isEdit:this.isEdit,
                        billid:this.billid,
                        isMulticurrency:isMulticurrency // ERP-40513 
                    });
                    this.advancePaymentWindow.on('beforeclose',function(winObj){
                        if(winObj.isSubmitBtnClicked) {
                            this.addSelectedAdvancePayments(winObj.getSelectedRecords());
                        }
                    },this);
                    
                    /*---- Refreshing the Grid after closing refund/deposit window -------*/
                    this.advancePaymentWindow.on('close', function(winObj) {
                        if (winObj.isSubmitBtnClicked) {
                            this.getView().refresh();
                        }
                    }, this);
                    
                    this.advancePaymentWindow.show();
                }
                if (Wtf.account.companyAccountPref.RCMApplicable != undefined && Wtf.account.companyAccountPref.RCMApplicable && Wtf.Countryid == Wtf.Country.INDIA && !this.isCustomer && this.parentObj != undefined && this.parentObj.rcmApplicable.getValue()) {
                    this.productSelWin = new Wtf.account.ProductSelectionWindow({
                        renderTo: document.body,
                        height: 600,
                        width: 700,
                        title: WtfGlobal.getLocaleText("acc.productselection.window.title"),
                        layout: 'fit',
                        modal: true,
                        resizable: false,
                        id: this.id + 'ProductSelectionWindow',
                        moduleid: this.moduleid,
                        heplmodeid: this.heplmodeid,
                        parentCmpID: this.parentObj,
                        invoiceGrid: this,
                        isCustomer : this.isCustomer,
                        isForAdvance: true
                    });
                    this.productSelWin.on('beforeclose', function (winObj) {
                        if (winObj.isSubmitBtnClicked) {
                            this.setHSNForAdvance(winObj.productgrid.getSelections());
                        }
                    }, this);
                    this.productSelWin.show();
                }
                break;
                
            case this.INVType: // Make/Receive payment against Invoice
                this.invoiceInfoWindow = new Wtf.account.InvoiceInfoWindow({
                    id: 'invoiceinfowindow',
                    title: this.isCustomer?WtfGlobal.getLocaleText("acc.lp.customerinvoiceapprovelevelone"):WtfGlobal.getLocaleText("acc.lp.vendorinvoiceapprovelevelone"),
                    border: false,
                    isReceipt: false,
                    isCustomer: this.isCustomer,
                    currencyfilterfortrans: this.paymentEntryObject.Currency.getValue(),
                    personInfo:this.paymentEntryObject.getPersonInformation(),
                    isEdit:this.isEdit,
                    isCopyReceipt:this.isCopyReceipt,
                    billid:this.billid,
                    isMulticurrency:isMulticurrency,
                    creationDate:new Date(this.parentObj.creationDate.getValue()).clearTime()
                });
                this.invoiceInfoWindow.on('beforeclose', function(winObj) {
                    if(winObj.isSubmitBtnClicked) {
                        this.addSelectedInvoices(winObj.getSelectedRecords());
                    }
                }, this);
                this.invoiceInfoWindow.show();
                break;

            case this.NoteType: // Receive Payment against Credit Note, Make Payment against Debit Note
                this.noteInfoWindow = new Wtf.account.NoteInfoWindow({
                    id:'noteinfowindow',
                    currencyfilterfortrans: this.paymentEntryObject.Currency.getValue(),
                    cntype: this.cntype,
                    isNoteForPayment: true,
                    onlyAmountDue: true,                    
                    personInfo:this.paymentEntryObject.getPersonInformation(),
                    isCN: !this.isReceipt,
                    isReceipt: this.isReceipt,
                    deleted: false,
                    nondeleted: true,
                    isCustomer:this.isCustomer,
                    isEdit:this.isEdit,
                    billid:this.billid,
                    isMulticurrency:isMulticurrency
                });
                this.noteInfoWindow.on('beforeclose', function(winObj) {
                    if(winObj.isSubmitBtnClicked) {
                        this.addSelectedNotes(this.noteInfoWindow.getSelectedRecords());
                    }
                }, this);
                this.noteInfoWindow.show();
                break;
                
            case this.GLType:
                this.accountInfoWindow= new Wtf.account.AccountInfoWindow({
                    id:'accountinfowindow'
//                    personInfo:this.paymentEntryObject.getPersonInformation()
                });
                this.accountInfoWindow.on('beforeclose', function(winObj) {
                    if(winObj.isSubmitBtnClicked){
                        this.addSelectedAccounts(this.accountInfoWindow.getSelectedRecords());                        
                    }
                }, this);
                this.accountInfoWindow.show();
                break;
        }
    },
    /**
     * set HSN for India Advance
     */
    setHSNForAdvance: function (jsonArray) {
        var jsonArrayObj = eval(jsonArray);
        if (jsonArrayObj.length == 1) {
            for (var cnt = 0; cnt < jsonArrayObj.length; cnt++) {                 
                if (jsonArrayObj[cnt].data["Custom_HSN/SAC Code"] != "" && jsonArrayObj[cnt].data["Custom_HSN/SAC Code"] != undefined) {
                    var value = jsonArrayObj[cnt].data["Custom_HSN/SAC Code"];
                    var globalname = "Custom_HSN/SAC Code";
                    var rcmApplicable = jsonArrayObj[cnt].data["rcmapplicable"];
                    if (!this.parentObj.purchaseFromURD && !rcmApplicable) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.common.rcmforproductnotactivated.cannotsave.makepayment")], 2);
                        this.productid = "";
                        var gridRecord = this.store.getAt(cnt);
                        gridRecord.set(globalname, "");
                        gridRecord.set('recTermAmount', 0.0);
                        gridRecord.set('taxamount', 0.0);
                        gridRecord.set('enteramount', 0.0);
                        gridRecord.set('productid',"");
                        this.fireEvent('datachanged',this);
                        return;
                    }
                    this.productname=jsonArrayObj[cnt].data["productname"]; // Adding Product Name ERM-1016
                    this.productid = jsonArrayObj[cnt].data["productid"];
                    var rec = this.selectionModel.getSelected();
                    if(rec!=undefined && rec!=''){
                    rec.set('productid',this.productid);
                    rec.set('productname',this.productname);}
                    for (var k = 0; k < this.colModel.config.length; k++) {
                        if (this.colModel.config[k].editor && this.colModel.config[k].editor.field.store && this.colModel.config[k].dataIndex == globalname) {
                            var store = this.colModel.config[k].editor.field.store;
                            var gridRecord = this.selectionModel.getSelected();
                            var recCustomCombo = WtfGlobal.searchRecord(store, value, "name");
                            if (recCustomCombo != undefined && recCustomCombo != null) {
                                var ComboValueID = recCustomCombo.data.id;
                                if (gridRecord != '' && gridRecord != undefined) {
                                    gridRecord.set(globalname, ComboValueID);
                                }
                            }
                        }
                    }
                }
            }
            getLineTermDetailsAndCalculateGSTForAdvance(this.parentObj, this, this.productid);
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.field.PleaseSelectOnlyoneProduct")], 2);
            return;
        }

    },
     addAllDocuments:function(AddAllDocuments,rowIndex){
        this.personInfo=this.paymentEntryObject.getPersonInformation();
        var isMulticurrency = this.paymentEntryObject.isMultiCurrencyPayment();

     
        var addAllDocsParameters = {
            paymentOption:this.paymentType,
            onlyAmountDue: true,
            accid: this.personInfo.accid,
            nondeleted: true,
            currencyfilterfortrans: this.paymentEntryObject.Currency.getValue(),//to convert amount from foreign to base currency.
            upperLimitDate:this.personInfo.upperLimitDate,//to fetch documents where date < paymentdate
            includeFixedAssetInvoicesFlag:true,
            billId:this.billid,
            isEdit:this.isEdit,
            filterForClaimedDateForPayment : true,
            cntype: 8,//for credit note
            isVendor: !this.isCustomer,
            customerid:this.personInfo.vendorId,
            vendorid: this.personInfo.vendorId,
            custVendorID: this.personInfo.accid,
            isNoteForPayment:!isMulticurrency,//this flag is used for :- when payment currency is other than base then get only selected currency's documents.
            requestModuleid: this.moduleid
        }
        if(!isMulticurrency){
            addAllDocsParameters.isReceipt = false;//if selected currency is other than base currency then select  documents of selecteds currency only for payment.
        }
        Wtf.Ajax.requestEx({
            url: "ACCVendorPaymentCMN/getAllPurchaseInvoicesAndCreditNotesForPayment.do",
            params:addAllDocsParameters
        }, this,
        function(result, req) {
            if(result.maxLimitReached){
                this.removeAllTypeROwAndAddBlankROw(rowIndex);
                WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.information'),WtfGlobal.getLocaleText('acc.maxLimitReached.inpayment')],3);
                return;
            }
            if(result.data.length > 0){
                this.addSelectedRecords(result.data,undefined,undefined,true,AddAllDocuments,rowIndex);
            }else{
                this.removeAllTypeROwAndAddBlankROw(rowIndex);
                WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.information'), this.isCustomer ? WtfGlobal.getLocaleText('acc.maxLimitReached.inpayment.alreadyselectedornodocument') : WtfGlobal.getLocaleText('acc.maxLimitReached.inpayment.alreadyselectedornodocumentforvendor')],3);
            }
        });
    },
    removeAllTypeROwAndAddBlankROw:function(rowIndex){
        if(rowIndex==this.store.getCount()-1){ //when last record is edited and type is selected as  "All" and if there is no documents for this customer then remove current row and add blank row
            this.store.remove(this.store.getAt(rowIndex));
            this.addBlankRow();
            this.getView().refresh();
        }else{
            this.store.remove(this.store.getAt(rowIndex));//if "All"" is selected other than last position and if there are no documents for that customer then clear the blank row.
        }
    },
    openWindowForSelectingGST:function(record,rowindex){
        var gstCodeSelected = record.data['documentid'];
        var appliedGst = record.data['appliedGst']?record.data['appliedGst']:'';
        this.GSTTaxesWindow= new Wtf.account.GSTTaxes({
            id:'gsttaxeswindow',
            isReceipt: false,                        
            border: false, 
            isEdit:this.isEdit,
            isCopy : this.isCopyReceipt,
            accountId:gstCodeSelected,
            appliedGst:appliedGst
        });
        this.GSTTaxesWindow.on('beforeclose',function(winObj){
            if(winObj.isSubmitBtnClicked) {
                this.setGstToSelectedRow(winObj.getSelectedRecords(),record,rowindex);
            }
        },this);
        this.GSTTaxesWindow.show();
    },
    getAmount: function(isdue) {
        var amt = 0;
        for (var i = 0; i < this.store.getCount(); i++) {
            var tempAmt = isdue ? this.store.getAt(i).data['amountdue'] : this.store.getAt(i).data['payment'];
            amt += WtfGlobal.conventInDecimalWithoutSymbol(tempAmt);
        }
        return WtfGlobal.conventInDecimalWithoutSymbol(amt);

    },
    getMultiDebitAmount:function(){       
        var amt=0;
        for(var i=0; i<this.store.getCount();i++){
            if(Wtf.Countryid == Wtf.Country.INDIA || (this.paymentType==3 && this.store.getAt(i).data['debit']!=undefined)){
               if(this.store.getAt(i).data['debit']==true) {
                amt+=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['enteramount']);
               }else{
                   amt-=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['enteramount']);
               }
            }else{
                amt+=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['enteramount']);
            }
                
        }
        return WtfGlobal.conventInDecimalWithoutSymbol(amt);
    },
    getMultiTDSAmount:function(){       
        var tdsamt=0;
        for(var i=0; i<this.store.getCount();i++){
            if(Wtf.Countryid == Wtf.Country.INDIA || (this.paymentType==3 && this.store.getAt(i).data['debit']!=undefined)){
               if(this.store.getAt(i).data['debit']==true) {
                tdsamt+=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['tdsamount']);
               }else{
                   tdsamt-=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['tdsamount']);
               }
            }else{
                tdsamt+=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['tdsamount']);
            }
        }
        return WtfGlobal.conventInDecimalWithoutSymbol(tdsamt);
    },
    getMultiDebitTaxAmount: function() {
        var amt = 0;
        var taxcmbCmp = Wtf.getCmp("paymenttaxcmb" + this.businessPerson);
        var totalCount=this.store.getCount();
        for (var i = 0; i <totalCount ; i++) {
            var rec = this.store.getAt(i);
            if (rec != undefined) {
                var val = rec.data.enteramount;
                var taxamount = rec.data.taxamount;
                if (taxamount != undefined && taxamount !== "") {
                    if (Wtf.Countryid == Wtf.Country.INDIA || (this.paymentType == this.paymentOption.AgainstGL && rec.data['debit'] != undefined)) {
                        if (rec.data['debit'] == true) {
                    amt += taxamount;
                } else {
                            amt -= taxamount;
                        }
                    } else {
                        amt += taxamount;
                    }

                } else {
                    var taxpercent = 0;
                    var index = taxcmbCmp.store.find('prtaxid', rec.data.prtaxid);
                    if (index >= 0) {
                        var taxrec = taxcmbCmp.store.getAt(index);
                        taxpercent = taxrec.data.percent;
                    }
                    if (Wtf.Countryid == Wtf.Country.INDIA || (this.paymentType == this.paymentOption.AgainstGL && rec.data['debit'] != undefined)) {
                        if (rec.data['debit']==true) {
                    amt += WtfGlobal.conventInDecimalWithoutSymbol((val * taxpercent / 100));
                        } else {
                            amt -= WtfGlobal.conventInDecimalWithoutSymbol((val * taxpercent / 100));
                }
                    } else {
                        amt += WtfGlobal.conventInDecimalWithoutSymbol((val * taxpercent / 100));
                    }

            }

        }
        }
        return WtfGlobal.conventInDecimalWithoutSymbol(amt);
    },
    setCurrencyid: function(cuerencyid, symbol) {
        this.currencyid = cuerencyid;
        this.symbol = symbol;
        this.store.each(function(rec) {
            rec.set('currencysymbol', symbol)
        }, this)
    },
    addBlankRow: function() {
        var Record = this.store.reader.recordType;
        var blankObj= this.getStoreEmptyRecord();
        var newrec = new Record(blankObj);
        this.store.add(newrec);
        if (this.view && this.view.grid != undefined) { //ERP-37456 sequence column of grid not updating
            this.view.refresh();   //SDP-12202
        }
    },
    addTax:function(){
         this.stopEditing();
         var p= callTax("taxwin");
         Wtf.getCmp("taxwin").on('update', function(){this.taxStore.reload();}, this);
    },
    
    setOtherFieldsBlankForRow: function(e) {
        if(e.originalValue!='' && e.value != e.originalValue){
            var selectedRecord= this.store.getAt(e.row);
            this.store.remove(selectedRecord);
            var Record = this.store.reader.recordType;
            var blankObj= this.getStoreEmptyRecord();
            var newrec = new Record(blankObj);
            newrec.data[e.field] = e.value;
            this.store.insert(e.row,newrec);
        }
    },
    getStoreEmptyRecord : function () {
        var Record = this.store.reader.recordType,f = Record.prototype.fields, fi = f.items, fl = f.length;
        var blankObj={};
        for(var j = 0; j < fl; j++){
            f = fi[j];
            if(f.name==='currencysymbol') {
                f.defValue = this.symbol
            }  
            blankObj[f.name]='';
            if(!Wtf.isEmpty(f.defValue))
                blankObj[f.name]=f.convert((typeof f.defValue == "function"?f.defValue.call():f.defValue));
            
        }
        return blankObj;
    },
    updateComponentVariables: function(obj) {
        for (var key in obj) {
            if (obj.hasOwnProperty(key)) {
                this[key] = obj[key];
            }
        }
    },
    
    addSelectedInvoices : function(jsonArray) {
        this.isBulkPayment=false;
        this.addSelectedRecords(jsonArray, this.INVType);
    },
    addSelectedInvoicesForBulkPayment : function(jsonArray) {
        this.addSelectedRecords(jsonArray, 2);
    },
    addSelectedNotes : function(jsonArray) {
        this.addSelectedRecords(jsonArray, this.NoteType);
    },
    addSelectedAccounts: function(jsonArray){
        this.addSelectedRecords(jsonArray, this.GLType);
    },
    
    addSelectedAdvancePayments: function(jsonArray){
        this.addSelectedRecords(jsonArray, this.ADVType);
    },
    
    /*
     * addBlankRow - will be false when clicked on LIFO/FIFO button. No need to add blank row in that case
     */
    addSelectedRecords: function(jsonArray, type, addBlankRow,appendRecord,AddAllDocuments,rowIndex,isForLifoFifo) { 
        var jsonArrayObj = eval(jsonArray);
        var duplicateRecordAdded=false;
        var isdebit=true;
        if(jsonArrayObj.length>0) {
            if(addBlankRow===undefined) {
                addBlankRow = true;
            }
            var currentRowIndex = this.selectedRow;
            if(this.selectedRow==-1) {
                currentRowIndex = 0;
            }
            if(appendRecord){
                if(AddAllDocuments && rowIndex!=undefined){
                  currentRowIndex = rowIndex;
                }else{
                    currentRowIndex=this.store.getCount();
                }
            }
            if(this.store.getAt(currentRowIndex)) {
                if (Wtf.Countryid == Wtf.Country.INDIA || this.paymentType == this.paymentOption.AgainstGL) {
                    isdebit = this.store.getAt(currentRowIndex).data['debit'];
                }
                this.store.remove(this.store.getAt(currentRowIndex));
            }
            if (!this.isBulkPayment) {
                this.getView().refresh();
            }
            for(var cnt=0; cnt < jsonArrayObj.length; cnt++) {
                    var storeRec = this.getStoreEmptyRecord();
                    /*
                     *if isBulkPaymentis true then setStorePaymentRecordValueFromJSONObject is used
                     */
                    if(this.isBulkPayment){
                    storeRec = this.setStorePaymentRecordValueFromJSONObject(storeRec, jsonArrayObj[cnt]);
                }else{
                    storeRec = this.setStoreRecordValueFromJSONObject(storeRec, jsonArrayObj[cnt]);
                }
                    var newrec = new this.store.reader.recordType(storeRec);
                    /**
                     * Add account code to account Name ERP-37263
                     */
                    if (jsonArrayObj[cnt]['acccode'] != undefined && jsonArrayObj[cnt]['acccode'] != "") {
                        newrec.data['documentno'] = '[' + jsonArrayObj[cnt]['acccode'] + '] ' + jsonArrayObj[cnt]['documentno'];
                    }
                    if(jsonArrayObj[cnt]['amount'] != undefined){
                        newrec.data['transactionAmount']=jsonArrayObj[cnt]['amount'];
                        newrec.data['amount']=jsonArrayObj[cnt]['amount'];
                    }else{
                        newrec.data['transactionAmount'] = 0;
                        newrec.data['amount'] = 0;
                    }
                    
                     if(AddAllDocuments && jsonArrayObj[cnt].documentType!=undefined){
                        type=jsonArrayObj[cnt].documentType;
                    }
                    if((type==this.INVType && !isForLifoFifo) || type==this.NoteType || this.isAdvanceTypeOfRefundMode(type)){
                     if(!this.isBulkPayment){
                        newrec.data['enteramount']=jsonArrayObj[cnt]['amountdue'];  // For payment against INvoice or CN/DN or Refund/Deposit, total amount due of that record will be set into enter amount initially
                    }
                    } else if (type==this.INVType && isForLifoFifo){
                        newrec.data['enteramount']=jsonArrayObj[cnt]['enteramount']; // ERP-16276 : available amount for invoice will be populated in entr amount column
                    }
                    
                    if(type==this.GLType)
                    {
                        if(jsonArrayObj[cnt]['masterTypeValue']==Wtf.masterTypeValueOfAccount['GSTTypeAccount']){
                            /* For singapore country, if individual tax is selected, 
                             * 'appliedGst' for that record will be same as that individual tax
                             */
                            if((jsonArrayObj[cnt]['isOneToManyTypeOfTaxAccount']!="true")){      
                                newrec.data['appliedGst']=jsonArrayObj[cnt]['appliedGst'];
                            }
                        }
                    }    
                    // Duplicate Document ID - Check selected document already exist in line level grid. It exist no need add/update
                    var index = -1;
                    if(type != this.ADVType || this.isAdvanceTypeOfRefundMode(type)) {
                        index = WtfGlobal.searchRecordIndex(this.store, newrec.data.documentid, "documentid");
                    }
                    var amountDueAfterDiscount = 0.0;
                    var discount = 0.0;
                    var exchangeRateForTransaction = jsonArrayObj[cnt]['exchangeratefortransaction'];
//                    if (CompanyPreferenceChecks.discountOnPaymentTerms() && !this.isCustomer && !this.isBulkPayment && (type == this.INVType) && jsonArrayObj[cnt]['amount'] != undefined &&
//                            (jsonArrayObj[cnt]['amount'] == jsonArrayObj[cnt]['amountDueOriginal'])) {
                    if (CompanyPreferenceChecks.discountOnPaymentTerms() && !this.isCustomer  && (type == this.INVType) && jsonArrayObj[cnt]['amount'] != undefined &&
                            (jsonArrayObj[cnt]['amount'] == jsonArrayObj[cnt]['amountDueOriginal'])) {
                        discount = this.calculateDiscount(jsonArrayObj[cnt]);
                        if (discount > (jsonArrayObj[cnt]['amountDueOriginal']*exchangeRateForTransaction)) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.receiptpayment.greaterdiscounterrormessage")], 1);
                            newrec.data['discountname'] = getRoundedAmountValue(jsonArrayObj[cnt]['amountDueOriginal'] * exchangeRateForTransaction);
                            newrec.data['enteramount'] = newrec.data['enteramount'] - newrec.data['discountname'];
                            amountDueAfterDiscount = getRoundedAmountValue((jsonArrayObj[cnt]['amount'] * exchangeRateForTransaction) - newrec.data['discountname']);
                        }else {
                            newrec.data['discountname'] = getRoundedAmountValue(discount);
                            newrec.data['enteramount'] = newrec.data['enteramount'] - newrec.data['discountname'];
                            amountDueAfterDiscount = getRoundedAmountValue((jsonArrayObj[cnt]['amount'] * exchangeRateForTransaction) - newrec.data['discountname']);
                        }
                    } else {
                        newrec.data['discountname'] = 0;
                        amountDueAfterDiscount = (jsonArrayObj[cnt]['amountdue'] != undefined && jsonArrayObj[cnt]['amountdue'] != "" && jsonArrayObj[cnt]['amountdue'] != null) ? jsonArrayObj[cnt]['amountdue'] : 0.0;
                    }
                    if (index == -1 || (index != -1 && type == this.GLType)) {        // i.e. Newly added record does not exists in already added records(For payment against Vendor/Customer.  Payment against GL has exception in this case).
                        this.store.insert(currentRowIndex, newrec);
                        this.store.getAt(currentRowIndex).set('type', type);
                        this.store.getAt(currentRowIndex).set('amountdueafterdiscount',amountDueAfterDiscount);
                        if (Wtf.Countryid == Wtf.Country.INDIA || this.paymentType == this.paymentOption.AgainstGL) {
                            this.store.getAt(currentRowIndex).set('debit', isdebit);
                        }
                        currentRowIndex++;
                    } else {
                        duplicateRecordAdded = true;
                    }
                }
                 if(AddAllDocuments){
                    this.getView().refresh();
                 }
            if(currentRowIndex === this.store.getCount() && addBlankRow) {
                this.addBlankRow();
            }
        }
        if(this.selectedRow!==-1 && !duplicateRecordAdded) {
//            this.startEditing(this.selectedRow, WtfGlobal.getColumnIndex(this,"enteramount"));
            this.startEditing(this.selectedRow, this.getColumnModel().findColumnIndex("description"));  //SDP-12753 : After Account Code / Document No. selection, focus will move to description
        }
        this.selectedRow = -1;
        this.fireEvent('datachanged',this);
    },
    
    setStorePaymentRecordValueFromJSONObject : function(rec, jsonobj) {
       var keyForRec="";
       for (var key in jsonobj) {
            if(key=="billno"){
                keyForRec="documentno";
                rec[keyForRec] = jsonobj[key];
            }else if(key=="billid"){
                keyForRec="documentid";
                rec[keyForRec] = jsonobj[key];
            }else if(key=="amountdueinbase"){
                keyForRec="enteramount";
                rec[keyForRec] = jsonobj[key];
                keyForRec="amountdue";
                rec[keyForRec] = jsonobj[key];
            }else if(key=="amountdue"){
                keyForRec="amountDueOriginal";
                rec[keyForRec] = jsonobj[key];
            }else if(key=="currencysymbol"){
                keyForRec="currencysymboltransaction";
                rec[keyForRec] = jsonobj[key];
            }else if (jsonobj.hasOwnProperty(key) && rec[key]!== undefined) {
                rec[key] = jsonobj[key];
            }
        }
        return rec;
    },
    calculateDiscount: function (jsonObj) {
        Date.prototype.addDays = function (days) {
            var dat = new Date(this.valueOf());
            dat.setDate(dat.getDate() + days);
            return dat;
        }
        var discount = 0;
        var exchangeRateForTransaction = jsonObj.exchangeratefortransaction;
        if (this.isBulkPayment && !Wtf.isEmpty(this.invObj) && !Wtf.isEmpty(this.invObj.discountMasterStore)) {
//            var discountMasterRecIndex = this.invObj.discountMasterStore.find('termid', jsonObj.termid);
            var discountMasterRecIndex = WtfGlobal.searchRecordIndex(this.invObj.discountMasterStore,jsonObj.termid,'termid');
            if (discountMasterRecIndex != -1) {
                var discountMasterRec = this.invObj.discountMasterStore.getAt(discountMasterRecIndex);
                if (discountMasterRec) {
                    jsonObj.applicabledays = discountMasterRec.get("applicabledays");
                    jsonObj.discounttype = discountMasterRec.get("discounttype");
                    jsonObj.discountvalue = discountMasterRec.get("discountvalue");
                }
            }
        }
        if (jsonObj != undefined && jsonObj != "" && jsonObj != null) {
            var invoicecreationdate = new Date(jsonObj.grcreationdate);
            var dateAfterAddingApplicableDays;
            if ((jsonObj.applicabledays != undefined) && (jsonObj.applicabledays !== "") && (jsonObj.applicabledays > -1)) {
                dateAfterAddingApplicableDays = invoicecreationdate.addDays(jsonObj.applicabledays);
                var isPercentDiscount = ((typeof jsonObj.discounttype === "string") ? ((jsonObj.discounttype == "1") ? true : false) : jsonObj.discounttype);
                if (dateAfterAddingApplicableDays >= this.parentObj.creationDate.getValue()) {
                    if (isPercentDiscount) {
                        discount = getRoundedAmountValue((jsonObj.amount * exchangeRateForTransaction) * (jsonObj.discountvalue / 100));
                    } else {
                        discount = getRoundedAmountValue(jsonObj.discountvalue);
                    }
                }
            }
        }
        return discount;
    },
    setStoreRecordValueFromJSONObject : function(rec, jsonobj) {
        for (var key in jsonobj) {
            if (jsonobj.hasOwnProperty(key) && rec[key]!== undefined) {
                if (this.store.fields.get(key).type == 'date' && !Wtf.isEmpty(jsonobj[key])) {
                    rec[key] = new Date(jsonobj[key]);
                } else {
                    rec[key] = jsonobj[key];
                }
            }
        }
        return rec;
    },
    getTypeStoreArray : function(paymentAgainst){
        this.ADVType = 1;
        this.INVType = 2;
        this.NoteType = 3;
        this.GLType = 4;
        this.All=5;
        if(paymentAgainst===this.paymentEntryObject.paymentOption.AgainstVendor){      // Against Vendor
            return [[this.ADVType, 'Advanced / Deposit'],
            [this.INVType, 'Invoice'],
            [this.NoteType, 'Credit Note'],
            [this.GLType, 'General Ledger Code'],
            [this.All, 'All']];
                
        } else if(paymentAgainst===this.paymentEntryObject.paymentOption.AgainstCustomer){  // Against Customer
            var retDocType=[];
            retDocType.push([this.NoteType, 'Credit Note']);
            retDocType.push([this.ADVType, 'Refund / Deposit']);
            if(this.readOnly){
                retDocType.push([this.GLType, 'General Ledger Code']);
            }
            retDocType.push([this.All, 'All']);
            return retDocType;
            
        } else {                     // Against GL Code
            return[
            [this.GLType, 'General Ledger Code']
            ]
        }
    },
    conversionFactorRenderer:function(value,meta,record) {
        var currencysymbol=((record==undefined||record.data.currencysymbol==null||record.data['currencysymbol']==undefined||record.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():record.data['currencysymbol']);
        var currencysymboltransaction=((record==undefined||record.data.currencysymboltransaction==null||record.data['currencysymboltransaction']==undefined||record.data['currencysymboltransaction']=="")?currencysymbol:record.data['currencysymboltransaction']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
        return "1 "+ currencysymboltransaction +" = " +value+" "+currencysymbol;
    }, 
    getData:function(){
        var arr=this.getGridArray();
        return WtfGlobal.getJSONArray(this, true,arr);
    },
    getJSONData:function(){
        var arr=this.getGridArray();
        return WtfGlobal.getJSONArrayWithEncoding(this, true,arr);
    },
    getGridArray:function(){
        var arr=[];
        var len=this.store.getCount()-1;    // At the time of saving data , last record will be an empty row in create new case, if lifo Fifo mode is false
        for(var i=0;i<len;i++){
            var rec = this.store.getAt(i);             
            rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
            rec.data["srNoForRow"]=i+1;
            arr.push(i);
        }
        return arr;
    },    
    setTDSAmount:function(v,m,rec){
        var tdsamount= v;//Set user inputed amount 
        tdsamount=rec.data.tdsamount; //in Edit Case tds value set
        tdsamount = WtfGlobal.conventInDecimalWithoutSymbol(tdsamount);
        rec.set("tdsamount",tdsamount);
        return WtfGlobal.withoutRateCurrencySymbol(tdsamount,v,rec);
    },
    setTaxAmount:function(v,m,rec){
        var taxamount= v;//Set user inputed amount
        /*
         * ERP-40242 : In copy case, deactivated tax not shown.Hence, empty taxid set in record.          
         */
        if (rec.data.prtaxid != '' && this.isCopyReceipt) {
            var taxActivatedRec = WtfGlobal.searchRecord(this.taxStore, rec.data.prtaxid, "prtaxid");
            if (taxActivatedRec == null || taxActivatedRec == undefined || taxActivatedRec == "") {
                rec.set("prtaxid", "");
            }
        }
        if(rec.data.prtaxid==null || rec.data.prtaxid == undefined || rec.data.prtaxid == ""){
                 taxamount = 0;
        }else if(this.isEdit && rec.data.prtaxid != undefined && rec.data.prtaxid != "" ){
            taxamount=rec.data.taxamount; //in Edit Case tax value set
        }
        if(rec.data.paymentWithSalesReturn!=undefined&&rec.data.paymentWithSalesReturn){
            taxamount=rec.data.taxamount
        }
        taxamount = WtfGlobal.conventInDecimalWithoutSymbol(taxamount);
        if(this.isBulkPayment){
            rec.data.taxamount = taxamount
        }else{
                rec.set("taxamount",taxamount);
            }
        return WtfGlobal.withoutRateCurrencySymbol(taxamount,v,rec);
    },
    calTaxAmount:function(rec){
        
        var val=rec.data.enteramount;
        var taxpercent=0;
        var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
        if(index>=0){
            var taxrec=this.taxStore.getAt(index);
            taxpercent=taxrec.data.percent;
        }
           
        return (val*taxpercent/100);

    },
    getTotalAmount: function(){
        var amount=0;
        for(var i=0 ; i<this.store.getCount() ; i++){
            if (Wtf.Countryid == Wtf.Country.INDIA || (this.paymentType == this.paymentOption.AgainstGL  && this.store.getAt(i).data['debit'] != undefined)) {
                if (this.store.getAt(i).data['debit']==true) {
                    amount += this.store.getAt(i).data.enteramount;
                } else {
                    amount -= this.store.getAt(i).data.enteramount;
        }
            } else {
                amount += this.store.getAt(i).data.enteramount;
            }
        }
        return getRoundedAmountValue(amount);
    },
    /*
     *    Ticket: SDP-952   
     *    getTotalAmountIncludingTax Function :
     *    The above function used for row level if Debit is TRUE then row level amount include Tax amount is add in Total amount.
     *    Else row level amount include Tax amount is substratct from Total amount (it treats like a credit).        
     *
     */
    getTotalAmountIncludingTax: function(){   
        var amount=0;
        for(var i=0 ; i<this.store.getCount() ; i++){
            if (Wtf.Countryid == Wtf.Country.INDIA || (this.paymentType == this.paymentOption.AgainstGL  && this.store.getAt(i).data['debit'] != undefined)) {
                if (this.store.getAt(i).data['debit']==true) {
                    amount += this.store.getAt(i).data.enteramount;
                    amount += this.store.getAt(i).data.taxamount;
                } else {
                    amount -= this.store.getAt(i).data.enteramount;
                    amount -= this.store.getAt(i).data.taxamount;
                }
            } else {
                amount += this.store.getAt(i).data.enteramount;
            }
        }
        return getRoundedAmountValue(amount);
    },
    getGLAccountNames: function(){
        var names='';
        for(var i=0;i<this.store.getCount();i++){
            names+=this.store.getAt(i).data.documentno+",";
        }
        if(names!='') {
            names = names.substring(0,names.length-1);
        }
        return names;
    },
    isTaxEditableForSelectedDocumentType:function(e){                                    // Tax is allowed in case of only 'Payment against GL''
        if(e.record.data.type==this.INVType || e.record.data.type==this.ADVType || e.record.data.type==this.NoteType || e.record.data.type=='' || e.record.data.masterTypeValue==Wtf.masterTypeValueOfAccount['GSTTypeAccount']){
            return false;
        } else {
            return true;
        }
    },
    isExchangeRateEditableForSelectedDocumentType:function(e){
        if(e.record.data.currencyidtransaction==this.paymentEntryObject.Currency.getValue() || e.record.data.type==this.GLType ){
                
                return false;
        } else {
            /*
//             * If Advance document type but with refund mode then documentno is mandatory and can edit exchange rate as 
//             * user is doing payment against advance receipt which may be in different currency.
//             */
            if(e.record.data.type==this.ADVType){
                if(this.isAdvanceTypeOfRefundMode(e.record.data.type)) {
                    return true
                } else {
                    return false;
                }
            } else {
                return true;
            }    
        }
    },
    isValidForAddingGlTypePayment: function(){
        var valid =false;
        var otherTransactionFlag=false;
        var GLFlag=false;
        var GLCount=0;
        for(var i=0;i<this.store.getCount()-1;i++){  // Check whethear grid contains any GL type payment(Last record in grid is excluded, as it is a blank row)
            var documenttype=this.store.getAt(i).data.type;
            if(documenttype==this.GLType){
                GLFlag=true;
                GLCount++;
            }
            if((documenttype==this.ADVType || documenttype==this.INVType || documenttype==this.NoteType) && this.store.getAt(i).data.enteramount!=0){
                otherTransactionFlag=true;
            }
        }
        if(GLCount==0 || (otherTransactionFlag && GLFlag)){  // If grid does not have GL type payment or grid has GL type payment along with other payment types
            valid=true;
        } 
        return valid;
    },
    isValidForMultipleAccounts: function(){
        var valid =true;
        var accountid='';
        var objectToReturn=[];
        var arrayOfDocuments=[];
        for(var i=0;i<this.store.getCount()-1;i++){  
           var rec=this.store.getAt(i);
           if(rec.data.type==this.INVType || rec.data.type==this.NoteType){     // Document type is Invoice or Credit/Debit note
               if(accountid==''){
                   if(rec.data.accountid!=''){
                       accountid=rec.data.accountid;
                   }
               } else {
                   if(rec.data.accountid!='' && accountid!=rec.data.accountid){
                       valid=false;                        
                   }
               }
               var object={
                    documentno:rec.data.documentno,
                    accountnames:rec.data.accountnames                           
                }
                arrayOfDocuments.push(object);   
           } 
        }
        objectToReturn['isValid']=valid;
        objectToReturn['arrayOfDocuments']=arrayOfDocuments;
        return objectToReturn;
    },
    checkDetails: function (grid) {
        var v = WtfGlobal.checkValidItems(this.moduleid, grid);
        return v;
    },
    populateDimensionValueingrid: function (rec) {
        WtfGlobal.populateDimensionValueingrid(this.moduleid, rec, this);
    },
    isAdvanceTypeOfRefundMode: function(documenttype) {
        return (documenttype==this.ADVType && this.paymentType==this.paymentEntryObject.paymentOption.AgainstCustomer)
    },
    filterDocumentTypeStore:function(currencyForSelectedPaymentMethod,paymentCurrency){        
        this.transactionTypeStore.removeAll();
        this.transactionTypeStore.loadData(this.typeStoreArray,false);
        this.store.removeAll();
        this.addBlankRow();
    },
    gstRenderer:function(v,m,rec){
            return "<div  wtf:qtip=\""+WtfGlobal.getLocaleText("acc.gst.desc")+"\" wtf:qtitle='"+WtfGlobal.getLocaleText("acc.gst.desc.title")+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";            
    },
    isValidForGSTTypeOfAccounts: function(){
        var isValid =true;
        for(var i=0;i<this.store.getCount();i++){
            var record = this.store.getAt(i);
            if(record.data['masterTypeValue']==Wtf.masterTypeValueOfAccount['GSTTypeAccount'] && (record.data['appliedGst']==undefined || record.data['appliedGst']=='')){
                isValid=false;
                break;
            }
        }
        return isValid;
    },    
    setGstToSelectedRow:function(jsonArray,record,rowindex){
         var jsonArrayObj = eval(jsonArray);
         var appliedGst = jsonArrayObj['0'].appliedGst;
         var recordToSet=this.store.getAt(rowindex);
         recordToSet.set('appliedGst',appliedGst);
    },
    isValidCreditAndDebitDetails:function(){
        var isValid=false;
        for(var i=0;i<this.store.getCount();i++){
            var record = this.store.getAt(i);
            if(record.data['debit']==true && (record.data['enteramount']!=0)){
                isValid=true;
                break;
            } else if (CompanyPreferenceChecks.discountOnPaymentTerms() && !this.isCustomer) {
                if (record.data.discountname > 0 && record.data.enteramount == 0) {
                    isValid = true;
                }
            }
        }
        return isValid
    },
    checkForValidLineLevelData:function(isCallFromReloadGridOnCurrencyChange){
        var invalidInvoices='';
        var invalidNotes='';
        var recordsToRemove=[];
        for(var i=0;i<this.store.getCount();i++){
            var record = this.store.getAt(i);
            if(record.data['type']==this.INVType && (record.data['amountdue']==0)){
                invalidInvoices+=record.data['documentno']+', ';
                recordsToRemove.push(i);
                this.store.remove(this.store.getAt(i));
                i=-1;
            } else if (record.data['type']==this.NoteType && (record.data['amountdue']==0)){
                invalidNotes+=record.data['documentno']+', ';
                recordsToRemove.push(i);
                this.store.remove(this.store.getAt(i));
                i=-1;
            }
        }
        if(invalidInvoices!='' || invalidNotes!=''){
            var alertMessage='';
            alertMessage+='<br>';
            if(invalidInvoices!=''){
                invalidInvoices = invalidInvoices.substring(0,invalidInvoices.length-2);
                alertMessage+= '<b>'+WtfGlobal.getLocaleText("acc.lp.vendorinvoiceapprovelevelone")+':</b> '+invalidInvoices+'<br>';
            }
            if(invalidNotes != ''){
                invalidNotes = invalidNotes.substring(0,invalidNotes.length-2);
                alertMessage+= '<b>'+WtfGlobal.getLocaleText("acc.mp.creditNotes")+':</b> '+invalidNotes;
            }
            if (!isCallFromReloadGridOnCurrencyChange) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.mprp.transactionsAlreadyFullyPaid") + alertMessage], 3);
            }
        }
        this.getView().refresh();
    },
    openWindowForSelectingTDS:function(record,rowindex){
        var gstCodeSelected = record.data['documentid'];
        var invType = "0";
        if(record!=undefined){
            invType = (record.data['invType']!=undefined&&record.data['invType']!="")?record.data['invType']:"0";
        }            
        var appliedTDS = record.data['appliedTDS']?record.data['appliedTDS']:'';
        this.TDSPaymentWindow= new Wtf.account.TDSPaymentWindow({
            id:'gsttaxeswindow',
            isReceipt: false,                        
            border: false, 
            readOnly : this.readOnly, // Send readOnly Proeprty to TDS payment window
            isEdit:this.isEdit,
            accountId:gstCodeSelected,
            appliedTDS:appliedTDS,
            personInfo:this.paymentEntryObject.getPersonInformation(),
            record:record,
            invType:invType,
            parentObj: this.parentObj
        });
        this.TDSPaymentWindow.on('beforeclose',function(winObj){
            if(winObj.isSubmitBtnClicked) {
                this.setTDSToSelectedRow(winObj.getSelectedRecords(),record,rowindex);
            }
        },this);
        
        this.TDSPaymentWindow.show();
    },    
    setTDSToSelectedRow:function(jsonArray,record,rowindex){
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
        recordToSet.set('tdsamount',totalTDSamount);
        recordToSet.set('appliedTDS',jsonArray);
        this.fireEvent('datachanged',this);
    },
    updateTermDetails: function(){
        var  LineTermdetails;
        var  LineTermTypeJson = {};
        if(Wtf.isEmpty(this.symbol)){
            this.parentObj.applyCurrencySymbol();
        }
        var lineLevelArray = [];
        var TotalTaxAmt = 0;
        for(var m=0 ; m < (this.store.data.length) ; m++){
            LineTermdetails = eval(this.store.data.itemAt(m).data.LineTermdetails);
            if(LineTermdetails != undefined && LineTermdetails != ""){
                //Already Defined. [[1,'VAT'],[2,'Excise Duty'],[3,'CST'],[4,'Service Tax'],[5,'Swachh Bharat Cess'],[6,'Krishi Kalyan Cess']]
                for(var n = 0 ; n < LineTermdetails.length ; n++){
                    var prevAmt = 0;
                    if(LineTermdetails[n].termtype ==Wtf.term.Others && (LineTermdetails[n].IsOtherTermTaxable!=undefined && !LineTermdetails[n].IsOtherTermTaxable)){
                        continue;
                    }
                    if(LineTermTypeJson.hasOwnProperty(LineTermdetails[n].termtype)){
                        prevAmt = LineTermTypeJson[LineTermdetails[n].termtype];
                    }
                    LineTermTypeJson[LineTermdetails[n].termtype] = prevAmt + LineTermdetails[n].termamount;
                    /**
                     * Add GST master details 
                     * ERP-32829
                     */
                    if (LineTermdetails[n].termtype == Wtf.term.GST) {
                        var isAlreadyexist=false;
                        for(var arr=0;arr<lineLevelArray.length;arr++){
                            var arrrec=lineLevelArray[arr];
                            if(arrrec.name==LineTermdetails[n].term){
                                var tempamt=arrrec.preamount;
                                arrrec.taxAmount=WtfGlobal.addCurrencySymbolOnly(tempamt+LineTermdetails[n].termamount, this.symbol);
                                arrrec.preamount=tempamt+LineTermdetails[n].termamount;
                                TotalTaxAmt += LineTermdetails[n].termamount;
                                isAlreadyexist=true;
                            }
                        }
                        if (!isAlreadyexist) {
                            var temp = {};
                            temp.name = LineTermdetails[n].term;
                            temp.preamount=LineTermdetails[n].termamount;
                            TotalTaxAmt += LineTermdetails[n].termamount;
                            temp.taxAmount = WtfGlobal.addCurrencySymbolOnly(LineTermdetails[n].termamount, this.symbol);
                            lineLevelArray.push(temp);
                        }
                    }
                }
            }
        }
        this.parentObj.LineLevelTermTplSummary.overwrite(this.parentObj.LineLevelTermTpl.body,{
            lineLevelArray : lineLevelArray,
            TotalTaxAmt : WtfGlobal.addCurrencySymbolOnly(TotalTaxAmt,this.symbol)
        });
    },
    getTotalDiscountOfAllLineLevelItems: function () {
        var totalDiscount = 0.0;
        for (var i = 0; i < this.store.getCount(); i++) {
            var record = this.store.getAt(i);
            if (record.data.discountname > 0 && record.data.enteramount == 0) {
                totalDiscount = totalDiscount + record.data.discountname;
            }
        }
        return totalDiscount;
    }
});
