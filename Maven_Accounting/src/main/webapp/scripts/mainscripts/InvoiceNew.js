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

Wtf.account.InvoicePanel = function(config) {
    Wtf.apply(this, config);
    this.initValues(config);
    Wtf.account.InvoicePanel.superclass.constructor.call(this, config);
    this.addEvents({
        'update': true
    });
}

Wtf.extend(Wtf.account.InvoicePanel, Wtf.account.MainClosablePanel, {
    initComponent: function(config) {
        Wtf.account.InvoicePanel.superclass.initComponent.call(this, config);

        //Create all stores required either for comboboxes, grid, or any other component.
        this.createStores();
        //Add event to stores
        this.addStoreEvents();
        //Update store readers with Custom/Dimension fields
        this.updateStoreReaderForCustOrDim();
        //Create all fields
        this.createFields();
        //Add event to fields
        this.addFieldEvents();                      //took it after initialization of all
        //Create all buttons & put them in an array.
        this.createButtons();
        //Create custom or dimension fields section
        this.tagsFieldset = this.createCustOrDimFields(this.isViewTemplate, (this.isEdit||this.copyInv));//In WtfDocumentMain.js
        //Append north form fields in arrays
        this.appendNorthFormFields();
        //Create north form panel
        this.createNorthForm(170, 0.55, 0.45);//In WtfDocumentMain.js 
        //Add event to north form
        this.addNorthFormEvents();
        //Creates product grid
        this.createProductGrid();
        //Add event to product grid
        this.addProductGridEvents();
        //Create south form
        this.createSouthForm();
                //Create common components for south panel 
        this.createCommonSouthPanelFields();
                //Append south panel fields in arrays
        this.appendSouthPanelFields();
        //Create south panel by adding components to it
        this.createSouthPanel(this.isViewTemplate, ((Wtf.isIE?210:150) + (this.prodDetailSouthItems.length>2 ? 400 : 50)));//In WtfDocumentMain.js
        //Create recent transaction panel.
        this.createRecentTransPanel();
        //Set transaction no.
        this.setTransactionNumber();
        //Display message - We are processing your request. Please wait...
        this.displayMsg();
        //Ajax to get invoice creation json
        this.invoiceCreationJSON();
    },
    onRender: function(config) {
        Wtf.account.InvoicePanel.superclass.onRender.call(this, config);
        //Append module specific buttons
        this.appendButtons();
        this.addButtonsTobbar();//In WtfDocumentMain.js
        //Load all stores
        this.loadInitialStore();
        //Update field configs as per add, edit & view.
        this.updateFieldConfigs();
        this.initForClose();
        //Hide form fields
        this.hideFormFields();     //moving to Parent
        this.pmdata();
    },
    initValues: function(config) {
        this.id = config.id;
    this.isGeneratedRecurringInvoice = config.isGeneratedRecurringInvoice;
    this.onDate = config.onDate;
    this.isOpeningBalanceOrder = (config.isOpeningBalanceOrder != undefined ? config.isOpeningBalanceOrder : false);
    this.templateId = config.templateId;
    this.isExpenseInv = config.isExpenseInv?config.isExpenseInv:false;
    this.isEdit = config.isEdit;
    this.consignmentNoForGr = (config.consignmentNoForGr != undefined ? config.consignmentNoForGr : false);
    this.label = config.label;
    this.copyInv = config.copyInv;
    this.isInvoice = config.isInvoice;
    this.record = config.record;
    this.exchangeRateInRetainCase = false;
    this.heplmodeid = config.heplmodeid;
    this.isVersion = config.isVersion;
    this.help = getHelpButton(this, config.heplmodeid);
    this.isFromProjectStatusRep = (config.isFromProjectStatusRep != null && config.isFromProjectStatusRep != undefined) ? config.isFromProjectStatusRep : false;
    this.DOSettings = config.DOSettings;
    this.GRSettings = config.GRSettings;
    this.uPermType = ((config.isCustomer ? WtfGlobal.getUPermObj(Wtf.UPerm_invoice) : WtfGlobal.getUPermObj(Wtf.UPerm_vendorinvoice)));
    this.permType = ((config.isCustomer ? WtfGlobal.getPermObj(Wtf.Perm_invoice) : WtfGlobal.getPermObj(Wtf.Perm_vendorinvoice)));
    this.uPaymentPermType = (config.isCustomer ? WtfGlobal.getUPermObj(Wtf.UPerm_invoice) : WtfGlobal.getUPermObj(Wtf.UPerm_vendorinvoice));

    if (config.moduleid == Wtf.Acc_Invoice_ModuleId) {
        this.exportPermType = this.permType.exportdatainvoice;
        this.printPermType = this.permType.printinvoice;
        this.emailPermType = this.permType.emailinvoice;
        this.recurringPermType = this.permType.recurringinvoice;
    } else if (config.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) {
        this.exportPermType = this.permType.exportdatavendorinvoice;
        this.printPermType = this.permType.printvendorinvoice;
        this.emailPermType = this.permType.emailvendorinvoice;
    }
    this.exportPermType = this.exportPermType;
    this.printPermType = this.printPermType;
    var moduleId = WtfGlobal.getModuleId(this); // to show terms in all SO,CQ,VQ,PO
    this.IsInvoiceTerm = true;//(config.isCustomer && (config.moduleid == '2' || moduleId == 22)) || moduleId == 6 || moduleId == 23;
    this.modeName = config.modeName;
    this.viewGoodReceipt = config.viewGoodReceipt;      //view cases for all
    this.islockQuantityflag = config.islockQuantityflag;
    this.readOnly = config.readOnly;
    this.PR_IDS = config.PR_IDS;
    this.isCustomer=config.isCustomer?config.isCustomer:false;
    if (config.moduleid == 2) {
        this.tranType = Wtf.autoNum.Invoice;
    } else {
        this.tranType = Wtf.autoNum.GoodsReceipt;
    }
    this.nameFieldLabel = this.getNameLabel();
    this.shipDateFieldLabel = (this.moduleid == Wtf.Acc_Invoice_ModuleId) ? WtfGlobal.getLocaleText("acc.field.deliveryDate") : WtfGlobal.getLocaleText("acc.field.ShipDate");
        this.shipDateFieldLabelToolTip = (this.moduleid == Wtf.Acc_Invoice_ModuleId) ? WtfGlobal.getLocaleText("acc.field.deliveryDate.tip") : WtfGlobal.getLocaleText("acc.field.ShipDate.tip");
        this.businessPerson = (this.isCustomer ? 'Customer' : 'Vendor');
    },
    getNameLabel: function(){
        var name = '';
        if(this.isCustomer){
            name = "<span wtf:qtip='"+  WtfGlobal.getLocaleText("acc.invoiceList.cust.tt") +"'>"+ WtfGlobal.getLocaleText("acc.invoiceList.cust") +"</span>";
        }else{
            name = "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.invoiceList.ven.tt") +"'>"+ WtfGlobal.getLocaleText("acc.invoiceList.ven") +"</span>";
        }
        return name;
    },
    createStores: function() {
        this.GridRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'number'}
        ]);
        this.fromPOStore = new Wtf.data.SimpleStore({
            fields: [{name: 'name'}, {name: 'value', type: 'boolean'}],
            data: [['Yes', true], ['No', false]]
        });
        var arrfromLink = new Array();
        if (this.isCustomer) {
            arrfromLink.push(['Sales Order', '0']);
            if (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_withinvupdate)) {
                arrfromLink.push(['Delivery Order', '1']);
            }
            arrfromLink.push(['Customer Quotation', '2']);
        } else {
            arrfromLink.push(['Purchase Order', '0']);
            if (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_withinvupdate)) {
                arrfromLink.push(['Goods Receipt', '1']);
            }
            arrfromLink.push(['Vendor Quotation', '2']);
        }
        this.fromlinkStore = new Wtf.data.SimpleStore({
            fields: [{name: 'name'}, {name: 'value'}],
            data: arrfromLink
        });

        this.pmtRec = new Wtf.data.Record.create([
            {name: 'methodid'},
            {name: 'methodname'},
            {name: 'accountid'},
            {name: 'acccurrency'},
            {name: 'accountname'},
            {name: 'isIBGBankAccount', type: 'boolean'},
            {name: 'isdefault'},
            {name: 'detailtype', type: 'int'},
            {name: 'acccustminbudget'},
            {name: 'autopopulate'},
        ]);
        this.pmtStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.pmtRec),
            url: "ACCPaymentMethods/getPaymentMethods.do",
            baseParams: {
                populateincpcs: true
            }
        });
        this.sequenceFormatStoreRecDo = new Wtf.data.Record.create([
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
        this.sequenceFormatStoreDo = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, this.sequenceFormatStoreRecDo),
            url: "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams: {
                mode: this.isCustomer ? "autodo" : "autogro",
                isEdit: this.isEdit
            }
        });

        this.POStoreUrl = "";
        var closeFlag = true;
        if (this.businessPerson == "Customer") {
            this.POStoreUrl = "ACCSalesOrderCMN/getSalesOrders.do";
        } else if (this.businessPerson == "Vendor") {

            this.POStoreUrl = "ACCPurchaseOrderCMN/getPurchaseOrders.do";
        }
        this.POStore = new Wtf.data.Store({
            url: this.POStoreUrl,
            baseParams: {
                mode: (this.isCustBill ? 52 : 42),
                closeflag: closeFlag
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'count'
            }, this.PORec)
        });

        this.partialInvoiceStore = new Wtf.data.SimpleStore({
            fields: [{name: 'name'}, {name: 'value', type: 'boolean'}],
            data: [['Yes', true], ['No', false]]
        });
        this.InvoiceRec = Wtf.data.Record.create([
            {name: 'billid'},
            {name: 'billno'}
        ]);
        this.InvoiceStoreUrl = "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
        this.InvoiceStore = new Wtf.data.Store({
            url: this.InvoiceStoreUrl,
            baseParams: {
                cashonly: false,
                consolidateFlag: false,
                creditonly: true,
                isOutstanding: false,
                isfavourite: false,
                ispendingpayment: false,
                nondeleted: true,
                excludeInvoiceId: (this.isEdit && !this.copyInv) ? this.record.data.billno : "",
                excludeLinkedConsignments: true,
                report: true,
                companyids: companyids,
                gcurrencyid: gcurrencyid,
                userid: loginid,
                consignmentNoForGr: this.consignmentNoForGr,
                isInvoice: this.isInvoice

            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'count'
            }, this.InvoiceRec)
        });
        this.moduleTemplateRecord = new Wtf.data.Record.create([
            {
                name: 'templateId'
            },
            {
                name: 'templateName'
            },
            {
                name: 'moduleRecordId'
            }
        ]);

        this.moduleTemplateStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.moduleTemplateRecord),
            url: "ACCCommon/getModuleTemplate.do",
            baseParams: {
                moduleId: WtfGlobal.getModuleId(this)
            }
        });

        this.SelectedTemplateStoreUrl = "";
        this.SelectedTemplateStoreUrl = this.businessPerson == "Customer" ? "ACCInvoiceCMN/getInvoicesMerged.do" : "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
        this.SelectedTemplateStore = new Wtf.data.Store({
            url: this.SelectedTemplateStoreUrl,
            scope: this,
            baseParams: {
                archieve: 0,
                deleted: false,
                nondeleted: false,
                cashonly: (this.cash == undefined) ? false : this.cash,
                creditonly: false,
                consolidateFlag: false,
                companyids: companyids,
                enddate: '',
                gcurrencyid: gcurrencyid,
                userid: loginid,
                isfavourite: false,
                startdate: ''
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'count'
            }, this.SelectedTemplateRec)
        });
    },
    addStoreEvents: function() {
        this.POStore.on('load', this.updateSubtotal, this)
    },
    
    createFields: function() {
        this.TermConfig = {
            fieldLabel: (this.isCustomer ? "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.invoice.creditTerm.tip") + "'>" + WtfGlobal.getLocaleText("acc.invoice.creditTerm") + "</span>" : "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.invoice.debitTerm.tip") + "'>" + WtfGlobal.getLocaleText("acc.invoice.debitTerm") + "</span>") + ' *',
            itemCls: (this.cash) ? "hidden-from-item1" : "",
            hideLabel: this.cash,
            id: "creditTerm" + this.heplmodeid + this.id,
            hidden: this.cash,
            hiddenName: 'term',
            name: 'term',
            allowBlank: this.cash,
            emptyText: (this.isCustomer ? WtfGlobal.getLocaleText("acc.inv.ct") : WtfGlobal.getLocaleText("acc.inv.dt")),
            listeners: {
                'select': {
                    fn: this.updateDueDate,
                    scope: this
                }
            }
        };
        this.Term = WtfGlobal.createFnCombobox(this.TermConfig, this.termds, 'termid', 'termname', this);
        if (!WtfGlobal.EnableDisable(WtfGlobal.getUPermObj(Wtf.UPerm_creditterm), WtfGlobal.getPermObj(Wtf.Perm_creditterm_edit))) {
            this.Term.addNewFn = this.addCreditTerm.createDelegate(this);
        }
        var isShowOneTime = this.showOneTimeCustomer();     // show field in create form of customer Invoice
        this.ShowOnlyOneTime = new Wtf.form.Checkbox({
            name: 'ShowOnlyOneTime',
            hiddenName: 'ShowOnlyOneTime',
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.cust.ShowOnlyOneTime.tt") + "'>" + WtfGlobal.getLocaleText("acc.cust.ShowOnlyOneTime") + "</span>",
            id: 'ShowOnlyOneTime' + this.heplmodeid + this.id,
            checked: false,
            hideLabel: !isShowOneTime, // show only in new case
            hidden: !isShowOneTime,
            cls: 'custcheckbox',
            width: 10
        });
        this.fromPOConfig = {
            hideLabel: this.cash || this.isTemplate || (this.isViewTemplate && !this.readOnly),
            hidden: this.cash ||  this.isTemplate || (this.isViewTemplate && !this.readOnly),
            mode: 'local',
            disabled: this.isEdit ? false : true,
            id: "linkToOrder" + this.heplmodeid + this.id,
            fieldLabel: ((!this.cash) ? WtfGlobal.getLocaleText("acc.field.Link") : (this.isOrder && this.isCustomer) ? (this.quotation ? WtfGlobal.getLocaleText("acc.field.LinktoVendorQuotation") : WtfGlobal.getLocaleText("acc.field.Link")) : (this.isOrder && !this.isCustomer) ? WtfGlobal.getLocaleText("acc.field.Link") : (this.isCustomer ? WtfGlobal.getLocaleText("acc.invoice.linkToSO") : WtfGlobal.getLocaleText("acc.invoice.linkToPO"))), //"Link to "+(this.isCustomer?"Sales":"Purchase")+" Order",
            allowBlank: this.isOrder,
            value: false,
            width: 50,
            name: 'prdiscount',
            hiddenName: 'prdiscount',
            listeners: {
                'select': {
                    fn: this.enablePO,
                    scope: this
                }
            }
        };
        this.fromPO = WtfGlobal.createCombobox(this.fromPOConfig, this.fromPOStore, 'value', 'name', this);
        var emptyText = WtfGlobal.getLocaleText("acc.field.SelectVQ/SO");
        if (!this.isCustBill) {
            emptyText = WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_withinvupdate) ? (this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectSO/DO/CQ") : WtfGlobal.getLocaleText("acc.field.SelectPO/GR/VQ")) : (this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectSO/CQ") : WtfGlobal.getLocaleText("acc.field.SelectPO/VQ"));
        }
        this.fromLinkComboConfig = {
            name: 'fromLinkCombo',
            hiddenName: 'fromLinkCombo',
            hideLabel: (this.isCustBill || this.cash || (this.isCustomer && this.quotation) || this.isTemplate || (this.isViewTemplate && !this.readOnly)) ? true : false,
            hidden: (this.isCustBill || this.cash || (this.isCustomer && this.quotation) || this.isTemplate || (this.isViewTemplate && !this.readOnly)) ? true : false,
            mode: 'local',
            id: 'fromLinkComboId' + this.heplmodeid + this.id,
            disabled: true,
            emptyText: emptyText,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Linkto"),
            allowBlank: false,
            width: 130,
            listeners: {
                'select': {
                    fn: this.enableNumber,
                    scope: this
                }
            }
        };
        this.fromLinkCombo = WtfGlobal.createCombobox(this.fromLinkComboConfig, this.fromlinkStore, 'value', 'name', this);
        var emptyTextForPOCombo = "Select Transaction";
        if (this.moduleid == Wtf.Acc_Invoice_ModuleId) {
            emptyTextForPOCombo = (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_withinvupdate)) ? WtfGlobal.getLocaleText("acc.field.SelectSO/DO/CQ") : WtfGlobal.getLocaleText("acc.field.SelectSO/CQ");
        } else if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) {
            emptyTextForPOCombo = (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_withinvupdate)) ? WtfGlobal.getLocaleText("acc.field.SelectPO/GR/VQ") : WtfGlobal.getLocaleText("acc.field.SelectPO/VQ");
        }
        this.MSComboconfig = {
            hiddenName: "ordernumber",
            name: "ordernumber",
            store: this.POStore,
            valueField: 'billid',
            hideLabel: this.cash || (this.isOrder && this.isCustBill) || this.isTemplate || (this.isViewTemplate && !this.readOnly),
            hidden: this.cash || (this.isOrder && this.isCustBill) || this.isTemplate || (this.isViewTemplate && !this.readOnly),
            displayField: 'billno',
            disabled: true,
            clearTrigger: this.isEdit ? false : true,
            emptyText: emptyTextForPOCombo, //this.isOrder ? (( this.isCustomer)?(this.quotation?WtfGlobal.getLocaleText("acc.inv.QOe/MN"):WtfGlobal.getLocaleText("acc.field.SelectCQRN")) : WtfGlobal.getLocaleText("acc.field.SelectVQ/SO")) : (Wtf.account.companyAccountPref.withinvupdate ? (this.isCustomer?WtfGlobal.getLocaleText("acc.field.SelectSO/DO/CQ"):WtfGlobal.getLocaleText("acc.field.SelectPO/GR/VQ")) : (!this.isCustBill)?(this.isCustomer?WtfGlobal.getLocaleText("acc.field.SelectSO/CQ"):WtfGlobal.getLocaleText("acc.field.SelectPO/VQ")):(this.isCustomer?WtfGlobal.getLocaleText("acc.inv.SOe"):WtfGlobal.getLocaleText("acc.inv.POe"))),
            mode: 'local',
            typeAhead: true,
            selectOnFocus: true,
            allowBlank: false,
            triggerAction: 'all',
            scope: this

        };
        this.PO = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Number"),
            id: "poNumberID" + this.heplmodeid + this.id,
            forceSelection: true,
            width: 240
        }, this.MSComboconfig));
        if (!WtfGlobal.EnableDisable(this.soUPermType, this.soPermType)) {
            this.PO.addNewFn = this.addOrder.createDelegate(this, [false, null, this.businessPerson + "PO"], true)
        }
        this.DueDateConfig = {
            fieldLabel: WtfGlobal.getLocaleText("acc.invoice.dueDate"), //'Due Date*',
            name: 'duedate',
            hiddenName: 'duedate',
            id: "duedate" + this.heplmodeid + this.id,
            itemCls: (this.cash || this.isOrder) ? "hidden-from-item" : "",
            hideLabel: this.cash || this.isOrder,
            hidden: this.cash || this.isOrder
        };
        this.DueDate = WtfGlobal.createDatefield(this.DueDateConfig, ((this.cash || this.isOrder) ? true : false), this);
        
        this.deliveryTimeConfig = {
            fieldLabel: WtfGlobal.getLocaleText("acc.field.deliveryTime"), // "Delivery Time";
            name: 'deliveryTime',
            id: "deliveryTime" + this.heplmodeid + this.id,
            width: 240,
            hidden: !(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_deliveryPlanner) && this.moduleid == Wtf.Acc_Invoice_ModuleId),
            hideLabel: !(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_deliveryPlanner) && this.moduleid == Wtf.Acc_Invoice_ModuleId)
        };
        this.deliveryTime = WtfGlobal.createTextfield(this.deliveryTimeConfig, false, true, 255, this);
        this.invoiceListConfig = {
            fieldLabel: WtfGlobal.getLocaleText("acc.invoice.consignmentNumber"), // "Consignment Number"
            id: "consignmentnumber" + this.heplmodeid + this.id,
            emptyText: WtfGlobal.getLocaleText("acc.invoice.consignmentNumberEmptyText"), // 'Select Consignment Number',
            mode: 'local',
            width: 240,
            name: 'landedInvoiceID',
            hiddenName: 'landedInvoiceID',
            hidden: !(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash),
            hideLabel: !(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash),
            triggerAction: 'all',
            forceSelection: true,
            selectOnFocus: true,
            listeners: {
                'select': {
                    fn: this.handleProductTypeForConsignment,
                    scope: this
                }
            }
        };
        this.invoiceList = WtfGlobal.createCombobox(this.invoiceListConfig, this.InvoiceStore, 'billid', 'billno', this);
        this.CostCenterConfig = {
            typeAhead: true,
            selectOnFocus: true,
            isProductCombo: true,
            extraFields: ['ccid', 'name', 'description'],
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.costCenter.tip") + "'>" + WtfGlobal.getLocaleText("acc.common.costCenter") + "</span>", //WtfGlobal.getLocaleText("acc.common.costCenter"),//"Cost Center",
            hiddenName: "costcenter",
            id: "costcenter" + this.heplmodeid + this.id,
            mode: 'local',
            forceSelection: true,
            extraComparisionField: 'ccid',
            width: 240,
            editable: true,
            triggerAction: 'all',
            addNewFn: this.addCostCenter,
            hirarchical: true
        };
        this.CostCenter = WtfGlobal.createExtFnCombobox(this.CostCenterConfig, Wtf.FormCostCenterStore, 'id', 'ccid', this);
        this.partialInvoiceCmbConfig ={
            triggerAction: 'all',
            mode: 'local',
            hidden: (this.isCustomer && !this.isCustBill && !this.isOrder && !this.cash) ? false : true,
            hideLabel: (this.isCustomer && !this.isCustBill && !this.isOrder && !this.cash) ? false : true,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.IsPartialInvoice"),
            id: "isPartialInv" + this.heplmodeid + this.id,
            value: false,
            width: 240,
            disabled: true,
            typeAhead: true,
            forceSelection: true,
            name: 'partialinv',
            hiddenName: 'partialinv',
            listeners: {
                'select': {
                    fn: this.showPartialDiscount,
                    scope: this
                }
            }
        };
        this.partialInvoiceCmb = WtfGlobal.createCombobox(this.partialInvoiceCmbConfig, this.partialInvoiceStore, 'value', 'name', this);
        if (this.isExpenseInv) {
            this.DOSettings = false;
            this.GRSettings = false;
        }
        this.autoGenerateDO = new Wtf.form.Checkbox({
            name: 'autogenerateDO',
            id: "autogenerateDO" + this.heplmodeid + this.id,
            fieldLabel: this.isCustomer ? "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.cust.generateDO.tt") + "'>" + WtfGlobal.getLocaleText("acc.cust.generateDO") + "</span>" : "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.vend.generateGR.tt") + "'>" + WtfGlobal.getLocaleText("acc.vend.generateGR") + "</span>", //'Generate Delivery Order',
            checked: this.isCustomer ? (this.DOSettings != null ? this.DOSettings : false) : (this.DOSettings != null ? this.GRSettings : false),
            cls: 'custcheckbox',
            width: 10
        });
        this.capitalGoodsAcquired = new Wtf.form.Checkbox({
            name: 'isCapitalGoodsAcquired',
            id: "isCapitalGoodsAcquired" + this.heplmodeid + this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.capital.goods.acquired"), //'Capital Goods Acquired',
            checked: false,
            hideLabel: !(!this.isExpenseInv && this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) == '137'), // if country is Malasia and this is an vendor Invoice and not an expense invoice then only it will be showns
            hidden: !(this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) == '137'),
            cls: 'custcheckbox',
            width: 10
        });
        this.isRetailPurchase = new Wtf.form.Checkbox({
            name: 'isRetailPurchase',
            id: "isRetailPurchase" + this.heplmodeid + this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.invoice.retail.purchase"), //'Retail Purchase,
            checked: false,
            hideLabel: (!(!this.isExpenseInv && WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) == '137' && (this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId))) || this.isSelfBilledInvoice, // if country is Malasia and this is an vendor Invoice or Cash Purchase and not an expense invoice then only it will be showns
            hidden: (!(!this.isExpenseInv && WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) == '137' && (this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId))) || this.isSelfBilledInvoice, // if country is Malasia and this is an vendor Invoice or Cash Purchase and not an expense invoice then only it will be showns
            cls: 'custcheckbox',
            width: 10
        });
        this.importService = new Wtf.form.Checkbox({
            name: 'importService',
            id: "importService" + this.heplmodeid + this.id,
            fieldLabel: 'Import Service',
            disabled: true,
            checked: false,
            hideLabel: (this.cash || this.moduleid != Wtf.Acc_Vendor_Invoice_ModuleId || WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) != '137' || this.isSelfBilledInvoice), // if country is Malasia and this is an vendor Invoice and not an expense invoice then only it will be showns
            hidden: (this.cash || this.moduleid != Wtf.Acc_Vendor_Invoice_ModuleId || WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) != '137' || this.isSelfBilledInvoice), // if country is Malasia and this is an vendor Invoice and not an expense invoice then only it will be showns
            cls: 'custcheckbox',
            width: 10
        });
        this.lockQuantity = new Wtf.form.Checkbox({
            name: 'lockQuantity',
            id: 'lockQuantitySO' + this.heplmodeid + this.id,
            hiddeName: 'lockQuan',
            fieldLabel: WtfGlobal.getLocaleText("Block Quantity"),
            checked: false,
            hideLabel: (!this.isCustomer || (this.isCustomer)),
            hidden: (!this.isCustomer || (this.isCustomer)),
            cls: 'custcheckbox',
            width: 10
        });
        
        this.copyAddress = new Wtf.form.Checkbox({
            name: 'copyadress',
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.sameasbillingadd"), //'Copy Address',
            checked: false,
            hideLabel: (!this.isCustomer),
            hidden: (!this.isCustomer),
            cls: 'custcheckbox',
            width: 10
        });
        this.generateReceipt = new Wtf.form.Checkbox({
            name: 'generateReceipt',
            id: "generateReceipt" + this.heplmodeid + this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.generateReceipt"), //'Generate Receipt',
            checked: false,
            hideLabel: (!this.cash || !this.isCustomer),
            hidden: (!this.cash || !this.isCustomer),
            cls: 'custcheckbox',
            width: 10
        });
        this.RMCDApprovalNoConfig= {
            fieldLabel: WtfGlobal.getLocaleText("RMCD Approval No"), //RMCD Approval No
            name: 'RMCDApprovalNo',
            id: "RMCDApprovalNo" + this.heplmodeid + this.id,
            hideLabel: !this.isSelfBilledInvoice,
            hidden: !this.isSelfBilledInvoice,
            width: 240
        };
        
        this.RMCDApprovalNo = WtfGlobal.createTextfield(this.RMCDApprovalNoConfig, false, true, 50, this);
        this.sequenceFormatComboboxDoConfig ={
            triggerAction: 'all',
            mode: 'local',
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.Sequenceformat.tip") + "'>" + WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat") + "</span>", //WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
            width: 240,
            maxLength: 2048,
            typeAhead: true,
            forceSelection: true,
            name: 'sequenceformatDo',
            hiddenName: 'sequenceformatDo',
            listeners: {
                'select': {
                    fn: this.getNextSequenceNumberDo,
                    scope: this
                }
            }
        };
        this.sequenceFormatComboboxDo = WtfGlobal.createCombobox(this.sequenceFormatComboboxDoConfig, this.sequenceFormatStoreDo, 'id', 'value', this);
        this.noConfig = {
            fieldLabel: this.isCustomer ? "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.DeliveryOrderNumber*.tt") + "'>" + WtfGlobal.getLocaleText("acc.field.DeliveryOrderNumber*") + "</span>" : "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.GoodsReceiptNumber*.tt") + "'>" + WtfGlobal.getLocaleText("acc.field.GoodsReceiptNumber*") + "</span>", //this.isCustomer ?WtfGlobal.getLocaleText("acc.field.DeliveryOrderNumber*") :WtfGlobal.getLocaleText("acc.field.GoodsReceiptNumber*") ,
            name: this.isCustomer ? 'numberDo' : 'numberGR',
            width: 240,
            hiddenName: this.isCustomer ? 'numberDo' : 'numberGR'
        };
 this.no = WtfGlobal.createTextfield(this.noConfig, false, false, 45, this);
        this.PORefNoConfig ={
            fieldLabel: WtfGlobal.getLocaleText("acc.invoice.POrefNo"), //PO Reference Number',
            name: 'porefno',
            id: "porefno" + this.heplmodeid + this.id,
            hidden: this.isOrder || !this.isCustomer,
            hideLabel: !this.isCustomer,
            itemCls: (!this.isCustomer) ? "hidden-from-item" : "",
            width: 240
            
        };
        this.PORefNo = WtfGlobal.createTextfield(this.PORefNoConfig, false, true, 45, this);
        this.pmtMethodAccConfig ={
            name: "pmtmethodacc",
            hidden: !this.cash,
            hideLabel: !this.cash,
            id: "pmtmethodacc" + this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.PaymentAccount"),
            width: 240
        };
        this.pmtMethodAcc = WtfGlobal.createTextfield(this.pmtMethodAccConfig, true, false, 100, this);
        this.pmtMethodDoConfig = {
            fieldLabel: WtfGlobal.getLocaleText("acc.mp.payMethod"),
            name: "pmtmethod",
            id: 'paymentMethod' + this.id,
            allowBlank: this.cash ? false : true,
            hidden: !(this.cash),
            hideLabel: !this.cash,
            emptyText: WtfGlobal.getLocaleText("acc.mp.selpayacc"),
            width: 240,
            mode: 'local',
            triggerAction: 'all',
            typeAhead: true,
            forceSelection: true//,
        };
        this.pmtMethod = WtfGlobal.createCombobox(this.pmtMethodDoConfig, this.pmtStore, 'methodid', 'methodname', this);
        this.vendorInvoiceConfig = {
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.invno"), //'Vendor Invoice Number*',
            name: 'vendorinvoice',
            id: "vendorInvoiceNo" + this.heplmodeid + this.id,
            hidden: this.label == 'Vendor Invoice' ? false : true,
            width: 240
        };
        this.vendorInvoice = WtfGlobal.createTextfield(this.vendorInvoiceConfig, false, this.checkin, 50, this);
    },
    addFieldEvents: function() {
        this.Currency.on('select', this.onCurrencySelect, this);
        this.Name.on('select', this.onNameSelect, this);
        this.Name.on('beforeselect', this.onNameBeforeSelect, this);
        this.includingGST.on('focus', this.onIncludingGSTfocus, this);
        this.includingGST.on('check', this.onIncludingGSTCheck, this);
        this.PO.on("clearval", this.onPOClearVal, this);
        this.ShowOnlyOneTime.on('check', this.onShowOnlyOneTimeCheck, this);
        this.DueDate.on('blur', this.dueDateCheck, this);
        this.billDate.on('change', this.onDateChange, this);
        if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash && !this.isExpenseInv && !this.isEdit) {
            this.InvoiceStore.load();
        }
        chkFormCostCenterload();
        this.autoGenerateDO.on('check', this.onAutoGenerateDOcheck, this);
        this.isCustomer ? Wtf.salesPersonFilteredByCustomer.load() : Wtf.agentStore.load(); //new store is used for salesperson combo  
        this.sequenceFormatStoreDo.on('load', this.onsequenceFormatStoreDoLoad, this);
        this.sequenceFormatStoreDo.load();
        if (this.cash) {
            this.pmtStore.load();
        }
        this.pmtMethod.on('select', this.ShowCheckDetails.createDelegate(this), this);
        this.pmtStore.on('load', this.setPMData, this);
        this.sequenceFormatStore.load();
        var transdate = (this.isEdit || this.copyInv ? WtfGlobal.convertToGenericDate(this.record.data.date) : WtfGlobal.convertToGenericDate(new Date()));
        this.moduleTemplateSection();
        this.postText = (this.record) ? this.record.data.posttext : "";
        if (!this.custVenOptimizedFlag && !this.isFromProjectStatusRep) {
            if (this.isCustomer) {
                Wtf.customerAccStore.load();
            } else {
                Wtf.vendorAccStore.reload();
            }
        }
    },
    onAutoGenerateDOcheck : function(o, newval, oldval){
            if (newval) {
                Wtf.serialwindowflag = true;
            } else {
                Wtf.serialwindowflag = false;
            }
            if (Wtf.serialwindowflag && !this.isExpenseInv) {//checking in companypreferences
                this.showGridBatch(newval);
            }
            if (this.autoGenerateDO.getValue()) {
                this.showDO();
            } else {
                this.hideDO();
            }
    },
    onsequenceFormatStoreDoLoad : function(){
                  if (this.sequenceFormatStoreDo.getCount() > 0) {
                var seqRec = this.sequenceFormatStoreDo.getAt(0)
                this.sequenceFormatComboboxDo.setValue(seqRec.data.id);
                var count = this.sequenceFormatStoreDo.getCount();
                for (var i = 0; i < count; i++) {
                    seqRec = this.sequenceFormatStoreDo.getAt(i)
                    if (seqRec.json.isdefaultformat == "Yes") {
                        this.sequenceFormatComboboxDo.setValue(seqRec.data.id)
                        break;
                    }
                }
                this.getNextSequenceNumberDo(this.sequenceFormatComboboxDo);
            }  
    },
    createSouthForm: function() {
        this.SouthForm = new Wtf.account.PayMethodPanel({
            region: "center",
            hideMode: 'display',
            baseCls: 'bodyFormat',
            isReceipt: false,
            isCash: true,
            disabledClass: "newtripcmbss",
            autoHeight: true,
            disabled: this.readOnly,
            hidden: true,
            style: 'margin:10px 10px;',
            id: this.id + 'southform',
            border: false
        });
    },
    createButtons: function(config) {
        this.saveAsDraftBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveasdraft"), // 'Save as Draft',
            tooltip: WtfGlobal.getLocaleText("acc.rem.222"),
            id: "saveasdraft" + this.heplmodeid + this.id,
            hidden: this.moduleid != Wtf.Acc_Invoice_ModuleId || (this.isEdit && !this.copyInv && !this.record.data.isDraft) || this.viewGoodReceipt,
            scope: this,
            disabled: this.isTemplate, //refer ticket ERP-13119
            handler: function() {
                if (this.createTransactionAlso) {     //refer ticket 13609
                    this.transactionType = 1;
                }
                this.isDraft = true;
                this.saveOnlyFlag = true;
                this.disableSaveButtons();
                this.save();
            },
            iconCls: 'pwnd save'
        });
        this.pushToPlannerConfig= {
            text: WtfGlobal.getLocaleText("acc.field.pushToPlanner"), // "Push to Planner",
            tooltip: WtfGlobal.getLocaleText("acc.field.pushToPlanner"), // "Push to Planner",
            id: "pushtoplanner" + this.heplmodeid + this.id,
            style: " padding-left: 15px;",
            cls: 'pwnd add',
            hidden: !(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_deliveryPlanner) && this.moduleid == Wtf.Acc_Invoice_ModuleId),
            disabled: true,
            scope: this,
            handler: function() {
                this.pushToPlanner();
            }
        };
            this.emailbuttonConfig= {
                text: WtfGlobal.getLocaleText("acc.common.email"), // "Email",
                tooltip: WtfGlobal.getLocaleText("acc.common.emailTT"), //"Email",
                id: "emailbut" + this.id,
                hidden: this.isTemplate || this.isViewTemplate,
                scope: this,
                disabled: true,
                handler: function() {
                    this.callEmailWindowFunction(this.response, this.request)
                },
                iconCls: "accountingbase financialreport"
            };

            this.singlePDFtext = singlePDFtext = this.isCustomer ?WtfGlobal.getLocaleText("acc.accPref.autoInvoice"): WtfGlobal.getLocaleText("acc.accPref.autoVI");
            this.singleRowPrint = new Wtf.exportButton({
                obj: this,
                id: "printSingleRecord" + this.id,
                iconCls: 'pwnd printButtonIcon',
                text: WtfGlobal.getLocaleText("acc.rem.236"),
                tooltip: WtfGlobal.getLocaleText("acc.rem.236.single"), //'Print Single Record Details',
                disabled: this.isViewTemplate ? false : true,
                exportRecord: this.exportRecord,
                hidden: false,
                menuItem: {rowPrint: (this.isSalesCommissionStmt) ? false : true},
                get: this.tranType,
                moduleid: this.moduleid
            });
            this.setRecurringInvoiceConfig = {
                text: WtfGlobal.getLocaleText("acc.field.SetRecurringInvoice"),
                iconCls: getButtonIconCls(Wtf.etype.copy),
                id: 'RecurringSO',
                hidden: !(this.moduleid == Wtf.Acc_Invoice_ModuleId),
                tooltip:WtfGlobal.getLocaleText("acc.field.CreateRecurringInvoice"),
                style: " padding-left: 15px;",
                scope: this,
                disabled: true,
                handler: function() {
                    var termDays = "";
                    if (this.Term.getValue() != null && this.Term.getValue() != "") {
                        var rec = this.Term.store.getAt(this.Term.store.find('termid', this.Term.getValue()));
                        if (rec != null && rec != "" && rec != undefined)
                            termDays = rec.data.termdays;
                    }
                        callRepeatedInvoicesWindow(true, undefined, false, false, true, this.RecordID, termDays);//set Forth Variable to false for Invoice  and true for sales order 
                }
            };
        this.marginButtonConfig = {
            text: WtfGlobal.getLocaleText("acc.field.margin"), // "Margin",
            cls: 'pwnd add',
            id: "margin" + this.id,
            tooltip: WtfGlobal.getLocaleText("acc.field.useMarginOptionToViewMarginOfProducts"),
            style: "padding-left: 15px;",
            scope: this,
            handler: this.getCostAndMarginWindow
        };
    },
    appendButtons: function(){
        if (this.moduleid == Wtf.Acc_Invoice_ModuleId || (!this.isEdit && this.copyInv && this.record.data.isDraft) || !this.viewGoodReceipt) {
            this.buttonArray = WtfGlobal.addComponentAtIndex(this.buttonArray, 2, this.saveAsDraftBttn);
        }
                if (!WtfGlobal.EnableDisable(this.uPermType, this.emailPermType)) {
            this.buttonArray = WtfGlobal.addComponentAtIndex(this.buttonArray, 3, this.emailbuttonConfig);
        }
        if ((WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_deliveryPlanner) && this.moduleid == Wtf.Acc_Invoice_ModuleId)) {
            this.buttonArray = WtfGlobal.addComponentAtIndex(this.buttonArray, 4, this.pushToPlannerConfig);
        }

        if (!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)) {
            this.buttonArray = WtfGlobal.addComponentAtIndex(this.buttonArray, 5, this.singleRowPrint);
        }

        if (!WtfGlobal.EnableDisable(this.uPermType, this.recurringPermType)) {
            this.buttonArray = WtfGlobal.addComponentAtIndex(this.buttonArray, 7, this.setRecurringInvoiceConfig);
        }
        this.buttonArray.push(this.marginButtonConfig);
        if (!this.readOnly && !this.copyInv && !this.isEdit) {
            this.buttonArray.push('->');
            this.buttonArray.push(this.help);
        }
    },
    appendNorthFormFields: function() {
        this.leftItemArr = WtfGlobal.addComponentAtIndex(this.leftItemArr, 0, this.moduleTemplateName);
        this.leftItemArr = WtfGlobal.addComponentAtIndex(this.leftItemArr, 1, this.createAsTransactionChk);
        this.leftItemArr = WtfGlobal.addComponentAtIndex(this.leftItemArr, 2, this.templateModelCombo);
        this.leftItemArr = WtfGlobal.addComponentAtIndex(this.leftItemArr, 3, this.ShowOnlyOneTime);
        this.linkSec = {
            layout: 'column',
            border: false,
            defaults: {border: false},
            items: [{
                    layout: 'form',
                    ctCls: (this.cash) ? "hidden-from-item1" : "",
                    items: this.fromPO
                }, {
                    layout: 'form',
                    ctCls: (this.cash) ? "hidden-from-item1" : "",
                    labelWidth: 45,
                    bodyStyle: "padding-left:10px;", //    ERP-12877
                    items: this.fromLinkCombo
                }]
        };
        this.leftItemArr = WtfGlobal.addComponentAtIndex(this.leftItemArr, 6, this.linkSec);
        this.leftItemArr = WtfGlobal.addComponentAtIndex(this.leftItemArr, 8, this.PO);
        this.leftItemArr = WtfGlobal.addComponentAtIndex(this.leftItemArr, 11, this.PORefNo);
        this.leftItemArr = WtfGlobal.addComponentAtIndex(this.leftItemArr, 12, this.CostCenter);
        this.leftItemArr.push(this.deliveryTime, this.capitalGoodsAcquired, this.isRetailPurchase, 
                 this.RMCDApprovalNo);
        
        //Right Array
        if(!this.cash)
        this.rightItemArr = WtfGlobal.addComponentAtIndex(this.rightItemArr, 1, this.Term);
        this.rightItemArr = WtfGlobal.addComponentAtIndex(this.rightItemArr, 2, this.DueDate);
        this.rightItemArr = WtfGlobal.addComponentAtIndex(this.rightItemArr, 8, this.partialInvoiceCmb);
        this.rightItemArr = WtfGlobal.addComponentAtIndex(this.rightItemArr, 10, this.lockQuantity);
        this.rightItemArr.push(this.invoiceList, this.generateReceipt, this.autoGenerateDO, this.sequenceFormatComboboxDo, this.no,
                 this.pmtMethod, this.pmtMethodAcc);
    },

    addNorthFormEvents: function() {
        this.NorthForm.on('render', function() {
            this.termds.load({
                params: {
                    cash_Invoice: this.cash
                }
            });
            this.termds.on("load", function() {
                if (this.autoGenerateDO.getValue()) {
                    this.showDO();
                } else {
                    this.hideDO();
                }
                if (this.isTemplate && !this.createTransactionAlso) {
                    WtfGlobal.hideFormElement(this.sequenceFormatCombobox);
                    WtfGlobal.hideFormElement(this.Number);
                }
            }, this);
        }, this);
    },  
    
    createProductGrid: function() {
                    {
           this.ProductGrid=new Wtf.account.ProductDetailsGrid({
                height: 300,//region:'center',//Bug Fixed: 14871[SK]
                layout:'fit',
                title: WtfGlobal.getLocaleText("acc.invoice.inventory"),  //'Inventory',
                border:true,
                //cls:'gridFormat',
                helpedit:this.heplmodeid,
                moduleid: this.moduleid,
                id:this.id+"editproductdetailsgrid",
                viewConfig:{forceFit:false},
                isCustomer:this.isCustomer,
                currencyid:this.currencyid,
                disabledClass:"newtripcmbss",
                isFromGrORDO:this.isFromGrORDO,
                parentCmpID:this.id,
                fromOrder:true,
                editTransaction:this.isEdit,
                isOrder:this.isOrder,
                isInvoice:this.isInvoice,
                isQuotation:this.quotation,
                isRequisition:this.isRequisition,
                forceFit:true,
                isCash:this.cash,
                loadMask : true,
                viewGoodReceipt: this.viewGoodReceipt,
                readOnly:this.isViewTemplate ||this.readOnly,
                parentObj :this,
                copyInv:this.copyInv,
                disabled:!(this.isEdit ||this.copyInv)?true:false
            }); 
        }
        if(this.isCustBill){  //Without Inventory.[PS]

        }else{    //With Inventory[PS]
            if(this.isEdit && !this.isOrder){
                if(this.isExpenseInv){
                    this.ExpenseGrid=new Wtf.account.ExpenseInvoiceGrid({
                        height: 300,
                        //layout : 'fit',
                        border:true,
                        cls:'gridFormat',
//                        title: WtfGlobal.getLocaleText("acc.invoice.gridExpenseTab"),//'Expense',
                        viewConfig:{forceFit:false},
                        isCustomer:this.isCustomer,
                        parentCmpID:this.id,
                        editTransaction:this.isEdit,
                        moduleid: this.moduleid,
                        isCustBill:this.isCustBill,
                        disabledClass:"newtripcmbss",
                        readOnly:this.isViewTemplate ||this.readOnly,
                        id:this.id+"expensegrid",
                        currencyid:this.Currency.getValue(),
                        fromOrder:true,
                        isOrder:this.isOrder,
                        isInvoice:this.isInvoice,
                        forceFit:true,
                        loadMask : true,
                        parentObj :this
                    });
//                    this.ExpenseGrid.on('datachanged',this.updateSubtotal,this);
                    this.Grid = this.ExpenseGrid; 
                    
                }else{

                    {
                        this.Grid=new Wtf.account.ProductDetailsGrid({
                            //region:'center',//Bug Fixed: 14871[SK]
                            height: 300,//region:'center',//Bug Fixed: 14871[SK]
                            cls:'gridFormat',
                            layout:'fit',
                            moduleid: this.moduleid,
                            id:this.id+"productdetailsgrid",
                            isCash:this.cash,
                            viewConfig:{forceFit:false},
                            autoScroll:true,
                            editTransaction:true,
                            disabledClass:"newtripcmbss",
                            //disabled:this.isViewTemplate,
                            isFromGrORDO:this.isFromGrORDO,
                            record:this.record,
                            copyInv:this.copyInv,
                            parentCmpID:this.id,
                            fromPO:false,
                            readOnly: this.readOnly,
                            isViewTemplate: this.isViewTemplate,
                            isEdit:this.isEdit,
                            heplmodeid:this.heplmodeid,//ERP-11098 [SJ]
                            isCN:false,
                            isCustomer:this.isCustomer,
                            isOrder:this.isOrder,
                            isInvoice:this.isInvoice,
                            isQuotation:this.quotation,
                            loadMask : true,
                            parentObj :this,
                            viewGoodReceipt: this.viewGoodReceipt
                        });
                    }
                }
            }
            else{
                if (this.isCustomer) {

                    {
                        this.Grid = new Wtf.account.ProductDetailsGrid({
                            //region:'center',//Bug Fixed: 14871[SK]
                            height: 300, //region:'center',//Bug Fixed: 14871[SK]
                            cls: 'gridFormat',
                            layout: 'fit',
                            parentCmpID: this.id,
                            moduleid: this.moduleid,
                            id: this.id + "editproductdetailsgrid",
                            isCash: this.cash,
                            viewConfig: {forceFit: false},
                            record: this.record,
                            isQuotation: this.quotation,
                            isQuotationFromPR: this.isQuotationFromPR,
                            isCustomer: this.isCustomer,
                            currencyid: this.currencyid,
                            disabledClass: "newtripcmbss",
                            fromPO: this.isOrder,
                            fromOrder: true,
                            isEdit: this.isEdit,
                            isFromGrORDO: this.isFromGrORDO,
                            isOrder: this.isOrder,
                            isInvoice: this.isInvoice,
                            heplmodeid: this.heplmodeid, //ERP-11098 [SJ]
                            forceFit: true,
                            editTransaction: this.isEdit,
                            loadMask: true,
                            readOnly: this.readOnly || this.isViewTemplate,
                            viewGoodReceipt: this.viewGoodReceipt,
                            parentObj: this,
                            copyInv: this.copyInv,
                            disabled: !(this.isEdit || this.copyInv) ? true : false

                        });
                    }
//                    this.Grid.on("productselect", this.loadTransStore, this);
//                    this.Grid.on("productdeleted", this.removeTransStore, this);
                }
                else {
                   this.ExpenseGrid=new Wtf.account.ExpenseInvoiceGrid({
                        height: 300,
                        //layout : 'fit',
                        border:true,
                        title: WtfGlobal.getLocaleText("acc.invoice.gridExpenseTab"),//'Expense',
                        viewConfig:{forceFit:false},
                        isCustomer:this.isCustomer,
                        editTransaction:this.isEdit,
                        moduleid: this.moduleid,
                        isCustBill:this.isCustBill,
                        disabledClass:"newtripcmbss",
                        id:this.id+"expensegrid",
                        currencyid:this.Currency.getValue(),
                        fromOrder:true,
                        closable: false,
                        isOrder:this.isOrder,
                        isInvoice:this.isInvoice,
                        forceFit:true,
                        loadMask : true,
                        parentObj :this,
                        disabled:!(this.isEdit ||this.copyInv)?true:false
                       
                    });
                    this.GridPanel= new Wtf.TabPanel({
                        id : this.id+'invoicegrid',
                        iconCls:'accountingbase coa',
                        disabled:this.isViewTemplate,
                        border:false,
                        style:'padding:10px;',
                        disabledClass:"newtripcmbss",
                        cls:'invgrid',
                        activeTab:0,
                        height: 200,
                        items: [this.ProductGrid,this.ExpenseGrid]
                    });
                    if (this.symbol == undefined)
                        this.symbol = WtfGlobal.getCurrencySymbol();
                }
            }
        }
            
    },
    addProductGridEvents: function() {
        if (this.productOptimizedFlag != undefined && this.productOptimizedFlag == Wtf.Show_all_Products) {
            this.ProductGrid.productComboStore.load();
        }
        this.ProductGrid.on("productselect", this.loadTransStore, this);
        this.ProductGrid.on("productdeleted", this.removeTransStore, this);
        if(this.Grid){
            this.Grid.on("productselect", this.loadTransStore, this);
            this.Grid.on("productdeleted", this.removeTransStore, this);
        }
        if (!this.isEdit) {
            this.Grid = Wtf.getCmp(this.id + "editproductdetailsgrid");
            if(this.ExpenseGrid)
            this.ExpenseGrid.on('datachanged', this.updateSubtotal, this);
            this.ProductGrid.on('datachanged', this.updateSubtotal, this);
            if (this.GridPanel)
                this.GridPanel.on('beforetabchange', this.beforeTabChange, this);
            this.isExpenseInv = false; //work fine in case of 2 tabs
        }
        this.NorthForm.on('render', this.setDate, this);
        if (this.isViewTemplate) {
            this.setdisabledbutton();
        }
        this.Grid.on('datachanged', this.updateSubtotal, this);
        this.Grid.getStore().on('load', function(store, recArr) {
            if (!this.isOrder && !this.quotation && this.isCustomer && this.copyInv && !this.isViewTemplate) {
                this.confirmMsg = "";
                if(!WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_withinvupdate)){
                    for (var i = 0; i < recArr.length; i++) {
                        if (recArr[i].data.productid !== undefined) {
                            var index = this.ProductGrid.productComboStore.find('productid', recArr[i].data.productid);
                            var prorec = this.ProductGrid.productComboStore.getAt(index);
                            if (recArr[i].data['quantity'] > this.ProductGrid.productComboStore.getAt(index).data['quantity'] && prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') {
                                this.confirmMsg += WtfGlobal.getLocaleText("acc.field.MaximumavailableQuantityforProduct") + this.ProductGrid.productComboStore.getAt(index).data['productname'] + WtfGlobal.getLocaleText("acc.field.is") + this.ProductGrid.productComboStore.getAt(index).data['quantity'] + ".<br>";
                                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"), this.confirmMsg + WtfGlobal.getLocaleText("acc.ven.msg4"), function(btn) {
                                    if (btn == "yes") {

                                    } else {
                                        this.ownerCt.remove(this);
                                    }
                                }, this);
                                recArr[i].set('quantity', 0);
                                recArr[i].set('amount', 0);
                            }
                        }
                    }
                }
            }
            this.Grid.addBlank(store);//ERP-9944 [SJ]
            this.updateSubtotal();//ERP-9944[SJ]
        }.createDelegate(this), this);
        this.Grid.getStore().on('update', function(store, record, opr) {
            var isExpensive = (this.isExpenseInv != null && this.isExpenseInv != undefined)?this.isExpenseInv:false;
            if (!this.isCustBill && !isExpensive) {
                var index = this.Grid.productComboStore.findBy(function(rec) {
                    if (rec.data.productid == record.data.productid)
                        return true;
                    else
                        return false;
                });
                var prorec = this.Grid.productComboStore.getAt(index);
                if (prorec != undefined && prorec != -1 && prorec != "") {
                    var availableQuantityInBaseUOM = prorec.data['quantity'];
                    var isBlockLooseSell = prorec.data['blockLooseSell'];
                    var availableQuantityInSelectedUOM = availableQuantityInBaseUOM;
                    var pocountinselecteduom = prorec.data['pocount'];
                    var socountinselecteduom = prorec.data['socount'];
                    if (isBlockLooseSell && record.get('isAnotherUOMSelected')) {//
                        availableQuantityInSelectedUOM = record.get('availableQtyInSelectedUOM');
                        pocountinselecteduom = record.get('pocountinselecteduom');
                        socountinselecteduom = record.get('socountinselecteduom');
                    }
                    var selectedUOMName = '';
                    if (isBlockLooseSell) {
                        selectedUOMName = record.get('uomname');
                    }
                    if (selectedUOMName == undefined || selectedUOMName == null || selectedUOMName == '') {
                        selectedUOMName = prorec.data['uomname'];
                    }
                    this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body, {
                        productid: prorec.data['productid'],
                        productname: prorec.data['productname'],
                        qty: parseFloat(getRoundofValue(availableQuantityInSelectedUOM)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                        soqty: parseFloat(getRoundofValue(socountinselecteduom)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                        poqty: parseFloat(getRoundofValue(pocountinselecteduom)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName
                    });
                }
            }
        }, this);
        if ( !this.isCustomer && !this.isOrder && !this.isEdit && !this.copyInv) {
            this.ProductGrid.on('pricestoreload', function(arr) {//alert("1111"+arr.length)
                if (!this.isExpenseInv) {
                    this.datechange = 1;
                    this.changeCurrencyStore(arr);
                }
            }, this);//.createDelegate(this)
        } else if (!this.isExpenseInv) {//alert("2222"+arr.length)
            this.Grid.on('pricestoreload', function(arr) {
                this.datechange = 1;
                this.changeCurrencyStore(arr);
            }.createDelegate(this), this);
        }
    },

    createRecentTransPanel: function() {
        var lastTransPanelId = "";
        if (this.cash) {
            lastTransPanelId = this.isCustomer ? "cashsales" : "cashpurchase";
        } else {
            lastTransPanelId = this.isCustomer ? "CInvoiceList" : "VInvoiceList";
        }
        this.lastTransPanel = this.isCustomer ? getCustInvoiceTabView(false, lastTransPanelId, '', undefined, true) : getVendorInvoiceTabView(false, lastTransPanelId, '', undefined, true);
    },
    loadRecord: function() {
        if (this.record != null && !this.dataLoaded) {
            var data = this.record.data;
            this.NorthForm.getForm().loadRecord(this.record);
            if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash && !this.isExpenseInv) {
                this.InvoiceStore.on("load", function() {
                    var record = new Wtf.data.Record({
                        billid: data.landedInvoiceID,
                        billno: data.landedInvoiceNumber
                    });
                    this.InvoiceStore.insert(0, record);
                    this.invoiceList.setValue(data.landedInvoiceID);
                }, this);
                this.InvoiceStore.load();
            }
            if (data.termid != "" && data.termid != null && data.termid != undefined && !this.cash) {
                this.termds.on("load", function() {
                    this.Term.setValue(data.termid);
                }, this);
                this.termds.load();
            }
            if (!this.copyInv && !(this.quotation && !this.isCustomer && this.ispurchaseReq)) {
                this.Number.setValue(data.billno);
            } else if (this.copyInv) {
                this.Number.setValue("");//copy case assign ""
            }
            this.externalcurrencyrate = this.record.data.externalcurrencyrate;
            this.Grid.getStore().on("load", function() {
                if (this.Grid.getStore().data.items.length > 0) {
                    this.linkIDSFlag = false;
                    var linkType = -1;
                    var storeData = [], linkNumbers = [], linkIDS = [];
                    this.POStore.removeAll();
                    this.Grid.getStore().each(function(rec) {
                        if (!this.copyInv) {
                            if ((rec.data.linkto != "" && rec.data.linkto != undefined) && (rec.data.linktype != -1 && rec.data.linktype != undefined)) {
                                var isExistFlag = false;
                                for (var count = 0; count < linkNumbers.length; count++) {
                                    if (rec.data.linkto == linkNumbers[count]) {
                                        isExistFlag = true;
                                        break;
                                    }
                                }
                                if (isExistFlag == false) {
                                    linkNumbers.push(rec.data.linkto);
                                    linkIDS.push(rec.data.linkid);
                                }
                                linkType = rec.data.linktype;
                                var newRec = new this.PORec({
                                    billid: rec.data.linkid,
                                    billno: rec.data.linkto
                                });
                                storeData.push(newRec);
                            }
                        }
                    }, this);
                    if (storeData.length > 0) {
                        this.POStore.add(storeData);
                    }
                    if (linkIDS.length > 0) {
                        this.linkIDSFlag = true;
                        this.Name.disable();
                        this.Currency.disable();
                        this.fromPO.disable();
                        this.fromLinkCombo.disable();
                        this.PO.disable();
                        this.fromPO.setValue(true);
                        this.PO.setValue(linkIDS);
                        this.includingGST.disable();
                    }
                    if (linkType != -1) {
                        this.fromLinkCombo.setValue(linkType);
                    }
                    if (this.isEdit && linkType == 1) {
                        this.autoGenerateDO.setValue(false);
                        this.autoGenerateDO.disable();
                    }
                }
            }, this);
            if ((this.copyInv || this.isEdit)) { // enable address button in edit case
                if (Wtf.getCmp("showaddress" + this.id)) {
                    Wtf.getCmp("showaddress" + this.id).enable();
                }
            }
            if ((this.copyInv || this.isEdit)) {
                this.isCustomer ? Wtf.salesPersonFilteredByCustomer.load({
                    params: {//sending a customerid to fliter available masteritems for selected customer 
                        customerid: this.record.data.personid
                    }
                }) : Wtf.agentStore.load();
            }
            if (this.viewGoodReceipt) {
                if (Wtf.getCmp("exportpdf" + this.id)) {
                    Wtf.getCmp("exportpdf" + this.id).hide();
                }
                if (Wtf.getCmp("RecurringSO")) {
                    Wtf.getCmp("RecurringSO").hide();
                }
                if (Wtf.getCmp("posttext" + this.id)) {
                    Wtf.getCmp("posttext" + this.id).hide();
                }
                if (Wtf.getCmp('south' + this.id)) {
                    Wtf.getCmp('south' + this.id).hide();
                }
                if (Wtf.getCmp('productDetailsTpl' + this.id)) {
                    Wtf.getCmp('productDetailsTpl' + this.id).hide();
                }
            }
//            this.template.setValue(data.templateid);
            this.Currency.setValue(data.currencyid);
            if (data.islockQuantityflag)
            {
                this.lockQuantity.setValue(true);
            }
            else
            {
                this.lockQuantity.setValue(false);
            }
            if (!this.custVenOptimizedFlag) {
                var store = (this.isCustomer ? Wtf.customerAccStore : Wtf.vendorAccStore)
                var index = store.findBy(function(rec) {
                    var parentname = rec.data['accid'];
                    if (parentname == data.personid)
                        return true;
                    else
                        return false;
                })
                if (index >= 0) {
                    this.Name.setValue(data.personid);
                }
            } else {
                this.Name.setValForRemoteStore(data.personid, data.personname);
            }
            this.Memo.setValue(data.memo);
            this.postText = data.posttext;
            this.DueDate.setValue(data.duedate);
            if (this.isOrder && data.isOpeningBalanceTransaction) {
                this.isOpeningBalanceOrder = data.isOpeningBalanceTransaction;
                this.billDate.maxValue = this.getFinancialYRStartDatesMinOne(true);
            }
            this.billDate.setValue(data.date);
            this.isTaxable.setValue(data.taxincluded);
            this.PORefNo.setValue(data.porefno);
            this.CostCenter.setValue(data.costcenterid);
            this.editedBy.setValue(data.lasteditedby);
            this.dataLoaded = true;
            if (this.IsInvoiceTerm) {
                this.setTermValues(data.termdetails);
                if (data.termsincludegst !== "" && data.termsincludegst === true) {
                    this.TermsIncludeGST_YES.setValue(true);
                    this.TermsIncludeGST_NO.setValue(false);
                    this.termsincludegst = true;
                } else if (data.termsincludegst !== "" && data.termsincludegst === false) {
                    this.TermsIncludeGST_NO.setValue(true);
                    this.TermsIncludeGST_YES.setValue(false);
                    this.termsincludegst = false;
                }
            }
            if (this.isCustomer && this.record.data.partialinv) {
                var id = this.Grid.getId();
                var rowindex = this.Grid.getColumnModel().getIndexById(id + "partdisc");
                this.Grid.getColumnModel().setHidden(rowindex, false);
            }
            var gridID = this.Grid.getId();
            var taxColumnIndex = this.Grid.getColumnModel().getIndexById(gridID + "prtaxid");
            var taxAmtColumnIndex = this.Grid.getColumnModel().getIndexById(gridID + "taxamount");
            if (this.Grid) {
                this.Grid.forCurrency = data.currencyid;
                this.Grid.affecteduser = data.personid;
                this.Grid.billDate = data.date;
            }
            if (this.record.data.includeprotax) {
                this.Grid.getColumnModel().setHidden(taxColumnIndex, false);
                this.Grid.getColumnModel().setHidden(taxAmtColumnIndex, false);
                this.isTaxable.setValue(false);
                this.isTaxable.disable();
                this.Tax.setValue("");
                this.Tax.disable();
            } else {
                this.Grid.getColumnModel().setHidden(taxColumnIndex, true);
                this.Grid.getColumnModel().setHidden(taxAmtColumnIndex, true);
                if (!this.isEdit && !this.isCopy)           //In edit case no need to reset Transaction Tax. - Amol D.
                    this.isTaxable.reset();
                this.isTaxable.enable();
            }
            if (this.isEdit && !this.templateId) {
                this.templateModelCombo.disable();
            }
            this.loadTransStore();
            if (this.productOptimizedFlag != undefined && this.productOptimizedFlag == Wtf.Show_all_Products) {
                this.ProductGrid.productComboStore.load({
                    params: {
                        mappingProduct: true,
                        customerid: this.Name.getValue(),
                        common: '1',
                        loadPrice: true,
                        mode: 22
                    }
                });
            }
            if (this.isExpenseInv) {
                this.includingGST.setValue(false);
                this.includingGST.disable();
            }
            else {
                this.includingGST.reset();
                this.isViewTemplate == true ? this.includingGST.disable() : this.includingGST.enable();
                if (this.record.data.gstIncluded != undefined) {
                    this.includingGST.setValue(this.record.data.gstIncluded);
                }
            }
            if((data.taxid == "")){
                this.isTaxable.setValue(false);
                this.Tax.setValue("");
                this.Tax.disable();
            }else{
                this.Tax.setValue(data.taxid);
                 this.isTaxable.enable();
                this.Tax.enable();//enable the tax when taxid is present-for edit case it was not required but for copy its is required.
                this.isTaxable.setValue(true);
            }
            this.gstCurrencyRate = this.record.data.gstCurrencyRate && this.record.data.gstCurrencyRate != "" ? this.record.data.gstCurrencyRate : 0;
        }
    },
  
    hideFormFields: function() {
        if (this.isCustomer) {
            if (this.isInvoice) {
                this.hideTransactionFormFields(WtfGlobal.getHideFormFieldObj(Wtf.HideFormFieldProperty_customerInvoice));
            } else if (this.cash) {
                this.hideTransactionFormFields(WtfGlobal.getHideFormFieldObj(Wtf.HideFormFieldProperty_CS));
            }
        } else {
            if (this.isInvoice) {
                this.hideTransactionFormFields(WtfGlobal.getHideFormFieldObj(Wtf.HideFormFieldProperty_vendorInvoice));
            } else if (this.cash) {
                this.hideTransactionFormFields(WtfGlobal.getHideFormFieldObj(Wtf.HideFormFieldProperty_CP));
            }
        }
    },

    setNextNumber: function(config) {
        if (this.sequenceFormatStore.getCount() > 0) {
            if ((this.isEdit || this.copyInv) && !this.templateId) { //only edit case & copy
                var index = WtfGlobal.searchRecordIndex(this.sequenceFormatStore, this.record.data.sequenceformatid, "id");
                if (index != -1) {
                    this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid);
                    if (!this.copyInv) {//edit
                        this.sequenceFormatCombobox.disable();
                        this.Number.disable();
                    } else {//copy case if sequenceformat id hide number
                        this.Number.disable();
                        WtfGlobal.hideFormElement(this.Number);
                    }
                } else {
                    this.sequenceFormatCombobox.setValue("NA");
                    this.sequenceFormatCombobox.disable();
                    if (!this.isViewTemplate) {            // View mode- all fields should be disabled unconditionaly
                        this.Number.enable();
                    }
                    if (this.copyInv) {//copy case show number field 
                        this.getNextSequenceNumber(this.sequenceFormatCombobox);
                    }
                }
            } else if (this.templateId || !this.isEdit) {// create new,generate so and po case and 
                var seqRec = this.sequenceFormatStore.getAt(0)
                this.sequenceFormatCombobox.setValue(seqRec.data.id);
                var count = this.sequenceFormatStore.getCount();
                for (var i = 0; i < count; i++) {
                    var seqRec = this.sequenceFormatStore.getAt(i)
                    if (seqRec.json.isdefaultformat == "Yes") {
                        this.sequenceFormatCombobox.setValue(seqRec.data.id)
                        break;
                    }
                }
                this.getNextSequenceNumber(this.sequenceFormatCombobox);
            }
        }
    },
    setdisabledbutton: function() {
        if (this.saveAsDraftBttn)
            this.saveAsDraftBttn.setDisabled(true);     //refer ticket ERP-13113
        this.moduleTemplateName.setDisabled(true);
        this.templateModelCombo.setDisabled(true);
        this.ShowOnlyOneTime.enable();
        this.Currency.setDisabled(true);
        this.PO.setDisabled(true);
        this.sequenceFormatCombobox.setDisabled(true);
        this.Number.setDisabled(true);
        this.billDate.setDisabled(true);
        this.PORefNo.setDisabled(true);
        this.autoGenerateDO.setDisabled(true);
        this.CostCenter.setDisabled(true);
//        this.invoiceTotxt.setDisabled(true);
        this.shipDate.setDisabled(true);
        this.Term.setDisabled(true);
        this.DueDate.setDisabled(true);
        this.shipvia.setDisabled(true);
        this.fob.setDisabled(true);
        this.includeProTax.setDisabled(true);
//        this.validTillDate.setDisabled(true);
        this.partialInvoiceCmb.setDisabled(true);
//        this.template.setDisabled(true);
//        this.templateID.setDisabled(true);
        this.users.setDisabled(true);
        this.generateReceipt.setDisabled(true);
        this.autoGenerateDO.setDisabled(true);
        this.sequenceFormatComboboxDo.setDisabled(true);
        this.no.setDisabled(true);
        this.Name.setDisabled(true);
        this.pmtMethod.setDisabled(true);
        this.pmtMethodAcc.setDisabled(true);
        this.fromLinkCombo.setDisabled(true);
        this.fromPO.setDisabled(true);
        this.lockQuantity.setDisabled(true);
        this.invoiceList.setDisabled(true);
// need to confirm about checks 
        if (this.youtReftxt)
            this.youtReftxt.setDisabled(true);
        if (this.delytermtxt)
            this.delytermtxt.setDisabled(true);
        if (this.delydatetxt)this.delydatetxt.setDisabled(true);
        if (this.projecttxt)this.projecttxt.setDisabled(true);
        if (this.depttxt)this.depttxt.setDisabled(true);
        if (this.requestortxt)this.requestortxt.setDisabled(true);
        if (this.mernotxt)this.mernotxt.setDisabled(true);
    },
    onNameSelect: function(combo, rec, index) {
        this.singleLink = false;
        if (combo.getValue() == this.nameBeforeSelect) { //If same name selected no need to do any action 
            return;
        }
        if (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) == '137' && this.isSelfBilledInvoice && rec.get('rmcdApprovalNumber')) {// For Malasian Company
            this.RMCDApprovalNo.setValue(rec.get('rmcdApprovalNumber'));
        }
        if (this.isEdit || this.copyInv) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), this.isCustomer ? WtfGlobal.getLocaleText("acc.invoice.alertoncustomerchange") : WtfGlobal.getLocaleText("acc.invoice.alertonvendorchange"), function(btn) {
                if (btn == "yes") {
                    this.doOnNameSelect(combo, rec, index);
                    this.currencySelect(combo, rec, index);
                } else {
                    this.Name.setValue(combo.startValue);
                    return false;
                }
            }, this);
        } else {
            this.doOnNameSelect(combo, rec, index);
        }
    },
    currencySelect: function(combo, rec, index) {
        this.newCurrencyid = rec.data.currencyid;
    },
    doOnNameSelect: function(combo, rec, index) {
        var customer = this.Name.getValue();
        if (this.isCustomer) {
            Wtf.salesPersonFilteredByCustomer.load({
                params: {//sending a customerid to fliter available masteritems for selected customer 
                    customerid: customer
                }
            });
        }
        if (this.isEdit || this.copyInv) {  //edit case when user retain exchange rate setting is true
            this.loadStoreOnNameSelect();
        } else {
            this.loadStore();
        }
        this.Name.setValue(customer);
        this.setTerm(combo, rec, index);
        if (this.isCustomer) {              //refer ticket ERP-14000
            this.setSalesPerson(combo, rec, index);
        }
        this.updateData();
        this.autoPopulateProducts();
        this.tagsFieldset.resetCustomComponents();
        var moduleid = this.isCustomer ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId;
        this.tagsFieldset.setValuesForCustomer(moduleid, customer);
        this.currenctAddressDetailrec = "";//If customer/vendor change in this case,previously stored addresses in this.currenctAddressDetailrec will be clear    
        this.Grid.setDisabled(false);
        if (this.ExpenseGrid) {
            this.ExpenseGrid.setDisabled(false);
        }
    },
    autoPopulateProducts: function() {
        if (!this.isExpenseInv) {
            if (!this.isEdit && !this.copyInv && this.autoPopulateMappedProduct) {  // in edit and copy case dont autopopulate mapped product untill user change the product manually 
                    this.Grid.ProductMappedStore.on('beforeload', function(s, o) {
                        if (!o.params)
                            o.params = {};
                        var currentBaseParams = this.Grid.ProductMappedStore.baseParams;
                        currentBaseParams.mappedProductRequest = true;
                        currentBaseParams.startdate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true));
                        currentBaseParams.enddate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false));
                        currentBaseParams.moduleid = this.moduleid;         // Passing Moduleid
                        this.Grid.ProductMappedStore.baseParams = currentBaseParams;
                    }, this);
                    this.Grid.ProductMappedStore.load({
                        params: {
                            mappingProduct: true,
                            affecteduser: this.Name.getValue(),
                            common: '1',
                            loadPrice: true,
                            mode: 22
                        }
                    })
                    this.Grid.ProductMappedStore.on("load", function() {
                        this.Grid.affecteduser = this.Name.getValue();
                        this.Grid.loadMappedProduct(this.Grid.ProductMappedStore);
                    }, this);
            } else {//Normal Case
                if (this.cash || this.isInvoice) {
                    if (this.productOptimizedFlag != undefined && this.productOptimizedFlag == Wtf.Show_all_Products) {
                        this.Grid.productComboStore.load({
                            params: {
                                mappingProduct: true,
                                customerid: this.Name.getValue(),
                                common: '1',
                                loadPrice: true,
                                mode: 22
                            }
                        })
                    }
                } else {
                    this.ProductGrid.productComboStore.load({
                        params: {
                            mappingProduct: true,
                            customerid: this.Name.getValue(),
                            common: '1',
                            loadPrice: true,
                            mode: 22
                        }
                    })
                }
            }
        }
    },
    addSalesPerson: function() {
        this.isCustomer ? addMasterItemWindow('15') : addMasterItemWindow('20');
    },

    successCallback: function(response) {
        if (response.success) {
            if (!this.isCustBill && !this.isCustomer && !this.isOrder && !this.isEdit && !this.copyInv && this.isQuotation) {
                this.ProductGrid.taxStore.loadData(response.taxdata);
                this.ExpenseGrid.taxStore.loadData(response.taxdata);
            }
            else
                this.Grid.taxStore.loadData(response.taxdata);
            this.termds.loadData(response.termdata);
            this.currencyStore.loadData(response.currencydata);
            if (this.Currency.getValue() != "" && WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid") == null) {
                if (this.currencyStore.getCount() <= 1) {
                    callCurrencyExchangeWindow();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg1")], 2);
                }
            }
            else {
                this.isCurrencyLoad = true;
            }
            if (this.cash)
                this.Term.setValue(0);
            if (this.isEdit && this.record != null) {
                if (this.record.data.taxid != undefined && this.record.data.taxid != null && this.record.data.taxid != "") {
                    this.isTaxable.enable();
                    this.isTaxable.setValue(true);
                    this.Tax.setValue(this.record.data.taxid);
                } else {
                    this.isTaxable.setValue(false);
                    this.Tax.setValue("");
                }
            }
            if (this.isEdit || this.copyInv)
                this.loadRecord();
            this.hideLoading();
            if (this.isExpenseInv) {
                if (this.Grid.accountStore.getCount() <= 1) {
                    this.Grid.accountStore.on("load", function() {
                        this.loadDetailsGrid();
                    }, this);
                } else {
                    this.loadDetailsGrid();
                }
            } else {
                if (this.productOptimizedFlag != undefined && this.productOptimizedFlag == Wtf.Show_all_Products) {
                    this.Grid.productComboStore.on("load", function() {
                        if (!this.saveOnlyFlag) { //no need to load editablegrid after click on save button
                            this.loadDetailsGrid();
                        }
                    }, this);
                }
                if (this.productOptimizedFlag != undefined && this.productOptimizedFlag != Wtf.Show_all_Products) {
                    this.loadDetailsGrid();
                }
                var loadDetailsGrid = false;
                if (this.isCustomer) {
                    if (Wtf.StoreMgr.containsKey("productstoresales")) {
                        loadDetailsGrid = true;
                    }
                }
                else {
                    if (Wtf.StoreMgr.containsKey("productstore")) {
                        loadDetailsGrid = true;
                    }
                }
                if (loadDetailsGrid) {
                    this.loadDetailsGrid();
                }
            }

        }
    },
    loadDetailsGrid: function() {
        if (this.isEdit) {
            this.loadEditableGrid();
        }
        if (this.isEdit && (BCHLCompanyId.indexOf(companyid) != -1)) {
            this.loadOtherOrderdetails();
        }
    },
    failureCallback: function(response) {
        this.hideLoading();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Failtoloadtherecords") + " " + response.msg], 2);
    },
    hideLoading: function() {
        Wtf.MessageBox.hide();
    },

    loadOtherOrderdetails: function() {
        Wtf.Ajax.requestEx({
            url: "ACCPurchaseOrderCMN/getPurchaseOrderOtherDetails.do",
            params: {
                poid: this.record.data.billid
            }
        }, this, function(response) {
            if (response.data && response.data.length > 0) {
                this.youtReftxt.setValue(response.data[0].poyourref);
                this.delydatetxt.setValue(response.data[0].podelydate);
                this.delytermtxt.setValue(response.data[0].podelyterm);
                this.invoiceTotxt.setValue(response.data[0].poinvoiceto);
                this.depttxt.setValue(response.data[0].podept);
                this.projecttxt.setValue(response.data[0].poproject);
                this.requestortxt.setValue(response.data[0].porequestor);
                this.mernotxt.setValue(response.data[0].pomerno);
            }
        }, function(response) {
        });
    },

    addOrder: function() {
        var tabid = "ordertab";
        if (this.isCustomer) {
            if (this.fromLinkCombo.getValue() == 1) {
                callDeliveryOrder(false, null, "deliveryorder");
            } else {
                tabid = "salesorder";
                callSalesOrder(false, null, tabid);
            }
        } else {
            if (this.fromLinkCombo.getValue() == 1) {
                callGoodsReceiptDelivery(false, null, "goodsreceiptdelivery");
            } else {
                tabid = "purchaseorder";
                callPurchaseOrder(false, null, tabid);
            }
        }
        if (Wtf.getCmp(tabid) != undefined) {
            Wtf.getCmp(tabid).on('update', function() {
                this.POStore.reload();
            }, this);
        }
    },

    enableNumber: function(c, rec) {
        this.PO.clearValue();
        this.fromLinkCombo.enable();
        this.fromPO.setValue(true);
        if (this.Grid) {
            this.Grid.isFromGrORDO = false;
        }
        if (rec.data['value'] == 0 || rec.data['value'] == 4) {          // 0 for Sales Order And 4 for Purchase Order
            this.PO.multiSelect = true;
            this.isMultiSelectFlag = true;
            this.PO.removeListener("select", this.populateData, this);  // for selection of multiple sales order 
            this.PO.addListener("blur", this.populateData, this);
            if (rec.data['value'] == 0) {
                this.fromLinkCombo.setValue(0);
            } else if (rec.data['value'] == 4) {
                this.fromLinkCombo.setValue(4);
            }
            this.autoGenerateDO.enable();
            if (this.isOrder) {
                this.POStore.proxy.conn.url = this.isCustomer ? "ACCPurchaseOrderCMN/getPurchaseOrders.do" : "ACCSalesOrderCMN/getSalesOrders.do";
                this.POStore.load({params: {currencyfilterfortrans: this.Currency.getValue(), linkflag: true}});
            } else {
                this.POStore.proxy.conn.url = this.isCustomer ? "ACCSalesOrderCMN/getSalesOrders.do" : "ACCPurchaseOrderCMN/getPurchaseOrders.do";
                this.POStore.load({params: {id: this.Name.getValue(), exceptFlagINV: true, currencyfilterfortrans: this.Currency.getValue()}});
            }
            this.PO.enable();
            if (this.partialInvoiceCmb) {
                this.partialInvoiceCmb.disable();
                var id = this.Grid.getId();
                var rowindex = this.Grid.getColumnModel().getIndexById(id + "partdisc");
                this.Grid.getColumnModel().setHidden(rowindex, true);
            }
            this.includingGST.enable();
        }
        else if (rec.data['value'] == 1) {     // 1 for Delivery   
            this.isFromGrORDO = true;
            this.Grid.isFromGrORDO = true;
            this.PO.multiSelect = true;
            this.isMultiSelectFlag = true;
            this.PO.removeListener("select", this.populateData, this);
            this.PO.addListener("blur", this.populateData, this);
            this.fromLinkCombo.setValue(1);
            this.autoGenerateDO.setValue(false);
            this.autoGenerateDO.disable();
            this.POStore.proxy.conn.url = this.isCustomer ? "ACCInvoiceCMN/getDeliveryOrdersMerged.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";
            this.POStore.load({params: {id: this.Name.getValue(), nondeleted: true, currencyfilterfortrans: this.Currency.getValue(), linkFlag: true, dropDownData: true}});     // : truePassing param "currencyfilterfortrans" for populating DO in CI as per their currency.
            this.PO.enable();
            if (this.partialInvoiceCmb) {
                this.partialInvoiceCmb.disable();
                var id = this.Grid.getId();
                var rowindex = this.Grid.getColumnModel().getIndexById(id + "partdisc");
                this.Grid.getColumnModel().setHidden(rowindex, true);
            }
            this.includingGST.disable();
        } else if (rec.data['value'] == 2) { //2 for Quotation                        
            this.PO.multiSelect = true;
            this.isMultiSelectFlag = true;
            this.autoGenerateDO.enable();
            this.PO.removeListener("select", this.populateData, this);
            this.PO.addListener("blur", this.populateData, this);
            this.fromLinkCombo.setValue(2);
            this.POStore.proxy.conn.url = this.isCustomer ? (this.isVersion ? "ACCSalesOrderCMN/getVersionQuotations.do" : "ACCSalesOrderCMN/getQuotations.do") : "ACCPurchaseOrderCMN/getQuotations.do";
            if (this.isCustomer) {
                this.POStore.load({params: {newcustomerid: this.Name.getValue(), linkFlagInInv: true, currencyid: this.Currency.getValue(), validflag: true, billdate: WtfGlobal.convertToGenericDate(this.billDate.getValue()), nondeleted: true}});
            } else {
                this.POStore.load({params: {newvendorid: this.Name.getValue(), linkFlagInGR: true, currencyid: this.Currency.getValue(), validflag: true, billdate: WtfGlobal.convertToGenericDate(this.billDate.getValue()), nondeleted: true}});
            }
            this.PO.enable();
            this.includingGST.enable();
        }
    },
    enablePO: function(c, rec) {
        this.autoGenerateDO.enable();
        if (rec.data['value'] == true) {
            if (!this.isCustBill && !this.isCustomer && !this.isEdit && !this.copyInv && !(this.isOrder && (!this.isCustomer))) {//this.isExpenseInv=false;
                this.GridPanel.setActiveTab(this.ProductGrid);
                this.ExpenseGrid.disable();
            }
            if (!(this.isCustBill || (this.isOrder && this.isCustomer) || this.cash)) {
                this.fromLinkCombo.enable();
            } else {
                //loading for so and po in CI and VI in With/Without inventory mode and but not in trading flow
                this.POStore.load({params: {id: this.Name.getValue()}});
                this.PO.enable();
            }
            this.fromOrder = true;
                this.PO.multiSelect = false;
                this.isMultiSelectFlag = false;
                this.PO.removeListener("blur", this.populateData, this);
                this.PO.addListener("select", this.populateData, this);
        }
        else {
            if (!this.isOrder && !this.cash && this.isCustomer)
            {
                this.fromLinkCombo.disable();
                this.PO.disable();
            }
            this.loadStore();
            if (!this.isCustomer && !this.isEdit && !this.copyInv) {//this.isExpenseInv=false;
                this.ExpenseGrid.enable();
            }
            this.setDate();
            if (this.partialInvoiceCmb) {
                this.partialInvoiceCmb.disable();
                var id = this.Grid.getId();
                var rowindex = this.Grid.getColumnModel().getIndexById(id + "partdisc");
                this.Grid.getColumnModel().setHidden(rowindex, true);
            }
        }
        this.currencyStore.load(); 	       // Currency id issue 20018
    },

    populateData: function(c, rec) {
        this.singleLink = false;
        if (this.PO.getValue() != "") {
            var billid = this.PO.getValue();
            this.clearComponentValues();
            this.Grid.fromPO = true;
            //suhas
            if (this.isMultiSelectFlag) { //For MultiSelection 
                var selectedids = this.PO.getValue();
                var selectedValuesArr = selectedids.split(',');
                var crosslink = false;
                if (selectedValuesArr.length == 1) {  // Load value of Include product tax according to PO
                    rec = this.POStore.getAt(this.POStore.find('billid', selectedValuesArr[0]));
                        this.linkRecord = this.POStore.getAt(this.POStore.find('billid', selectedValuesArr[0]));
                        this.singleLink = true;
                    if (rec.data["includeprotax"]) {
                        this.includeProTax.setValue(true);
                        this.showGridTax(null, null, false);
                        this.isTaxable.setValue(false);//when selecting record with product tax.Tax should get disabled.
                        this.isTaxable.disable();
                        this.Tax.setValue("");
                        this.Tax.disable();
                    } else {
                        this.includeProTax.setValue(false);
                        this.showGridTax(null, null, true);
                        this.Tax.enable();//required because when selected multiple records & changing to select single record.Before it was getting disabled.
                        this.isTaxable.enable();
                    }
                    if (rec.data["gstIncluded"] && !this.includingGST.getValue()) {
                        this.includingGST.setValue(true);
                    } else if (!rec.data["gstIncluded"] && this.includingGST.getValue()) {
                        this.includingGST.setValue(false);
                    }
                    if (this.IsInvoiceTerm) {
                        this.setTermValues(rec.data.termdetails);
                    }
                    var linkedRecordExternalCurrencyRate = rec.data["externalcurrencyrate"];
                    if (this.Currency.getValue() != WtfGlobal.getCurrencyID && linkedRecordExternalCurrencyRate != "" && linkedRecordExternalCurrencyRate != undefined) { //If selected currency is foreign currency then currency exchange rate will be exchange rate of linked document 
                        this.externalcurrencyrate = linkedRecordExternalCurrencyRate;
                    }
                } else if (selectedValuesArr.length > 1) {
                    var productLevelTax = false;
                    var isGSTTax = false;
                    var isInvoiceLevelTax = false;
                    var withoutTax = false;
                    this.previusTaxId = "";
                    var isInvoiceTaxDiff = false;
                    var invoiceLevelTaxRecords = 0;
                    var reccustomerporefno = '';
                    for (var cnt = 0; cnt < selectedValuesArr.length; cnt++) {
                        rec = this.POStore.getAt(this.POStore.find('billid', selectedValuesArr[cnt]));
                        if (rec.data.contract != undefined && rec.data.contract != "") { // in case of multiple linking if linked transactions are containing different different contract ids or similar contract ids then we will not allow linking
                            var dataMsg = "";
                            if (this.moduleid == Wtf.Acc_Invoice_ModuleId) {
                                if (this.fromLinkCombo.getValue() == 0) {// linked from SO
                                    dataMsg = WtfGlobal.getLocaleText("acc.linking.so.selection.msg");
                                } else if (this.fromLinkCombo.getValue() == 1) {// linked from DO
                                    dataMsg = WtfGlobal.getLocaleText("acc.linking.do.selection.msg");
                                }
                            }
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), dataMsg], 2);
                            this.PO.clearValue();
                            return;
                        }
                        if (rec.data["gstIncluded"]) { //checks for GST Tax
                            isGSTTax = true;
                        } else if (rec.data["includeprotax"]) { //checks for product level tax
                            productLevelTax = true;
                        } else if (rec.data["taxid"] != "" && rec.data["taxid"] != undefined) { //checks for invoice level tax 
                            isInvoiceLevelTax = true;
                            if (invoiceLevelTaxRecords != 0 && this.previusTaxId != rec.data["taxid"]) {
                                isInvoiceTaxDiff = true;
                            }
                            this.previusTaxId = rec.data["taxid"];
                            this.includeProTax.setValue(false);
                            this.showGridTax(null, null, true);//updating include product tax
                            invoiceLevelTaxRecords++;
                        } else {
                            withoutTax = true;//applicable for both no tax and diff tax
                        }
                        if (rec.data["customerporefno"]) {   //    ERP-9886
                            if (reccustomerporefno != "")
                                reccustomerporefno += ',' + rec.data["customerporefno"];
                            else
                                reccustomerporefno += rec.data["customerporefno"]
                        } else {
                            reccustomerporefno += '';
                        }
                    }
                    if (isGSTTax) { //case when any linked record have GST Tax
                        var includeGstCount = 0;
                        var excludeGstCount = 0;
                        for (var cntGst = 0; cntGst < selectedValuesArr.length; cntGst++) {
                            rec = this.POStore.getAt(this.POStore.find('billid', selectedValuesArr[cntGst]));
                            if (rec.data["gstIncluded"]) {
                                includeGstCount++;
                            } else if (!rec.data["gstIncluded"]) {
                                excludeGstCount++;
                            }
                        }

                        if (!((selectedValuesArr.length == includeGstCount) || (selectedValuesArr.length == excludeGstCount))) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.alert.includingGST")], 2);
                            this.PO.clearValue();
                            return;
                        } else {
                            if (selectedValuesArr.length == includeGstCount) {
                                this.includeProTax.setValue(true);
                                this.includingGST.setValue(true);
                            } else if (selectedValuesArr.length == excludeGstCount) {
                                this.includeProTax.setValue(false);
                                this.includingGST.setValue(false);
                            }
                        }
                    } else if (productLevelTax) {//case when any linked record have product tax without GST Tax
                        if (isInvoiceLevelTax) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.alert.includingProductTax")], 2);
                            this.PO.clearValue();
                            return;
                        } else {//no tax and producttax
                            this.includeProTax.setValue(true);
                            this.showGridTax(null, null, false);
                            this.isTaxable.setValue(false);//when selcting record with product tax.Tax should get disabled.
                            this.isTaxable.disable();
                            this.Tax.setValue("");
                            this.Tax.disable();
                        }
                    } else if (isInvoiceLevelTax) {
                        if (withoutTax || isInvoiceTaxDiff) {//for different tax and empty tax
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.alert.includingDifferentTax")], 2);
                            this.PO.clearValue();
                            return;
                        } else {
                            this.Tax.enable();
                            this.isTaxable.enable();
                            this.isTaxable.setValue(true);
                            this.Tax.setValue(this.previusTaxId);
                        }
                        this.includeProTax.setValue(false); //update include product tax
                        this.showGridTax(null, null, true);
                    } else {//for goodsreceiptorder and deliveryorder
                        this.Tax.disable();
                        this.isTaxable.enable();
                        this.isTaxable.setValue(false);
                        this.Tax.setValue("");
                        this.includeProTax.setValue(false); //update include product tax
                        this.showGridTax(null, null, true);
                    }

                    var isLinkedDocumentHaveSameER = true;
                    var linkedExternalRate = 0;
                    if (this.Currency.getValue() != WtfGlobal.getCurrencyID) { // Foreign currency linking case. In this case we have to borrow Linked document Exchange Rate in current document.                  
                        for (var count = 0; count < selectedValuesArr.length; count++) {
                            var tempRec = WtfGlobal.searchRecord(this.POStore, selectedValuesArr[count], "billid");
                            if (count == 0) {
                                linkedExternalRate = tempRec.data["externalcurrencyrate"]; // taking externalcurrencyrate of first record and then comparing it with other records external currency rate
                            } else if (tempRec.data["externalcurrencyrate"] != linkedExternalRate) {
                                isLinkedDocumentHaveSameER = false;
                                break;
                            }
                        }
                        if (isLinkedDocumentHaveSameER) { //if exchange rate same for all linked document then applying it for current record by assigning here 
                            this.externalcurrencyrate = linkedExternalRate;
                        } else { //if exchange rate different then reassigning exchange rate of that date and giving below information message 
                            var index = this.getCurrencySymbol();
                            var exchangeRate = this.currencyStore.getAt(index).data['exchangerate'];
                            this.externalcurrencyrate = exchangeRate;
                            var msg = WtfGlobal.getLocaleText("acc.invoiceform.exchangeratemessage1") + "<b> " + this.externalcurrencyrate + " </b>" + WtfGlobal.getLocaleText("acc.invoiceform.exchangeratemessage2");
                            WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.information'), msg], 3);
                        }
                    }
                }
                this.setValues(billid);//In MultiSelection if the user select only one
                rec = this.PO.getValue();
                selectedValuesArr = rec.split(',');
                if (selectedValuesArr.length == 1) {
                    var record = this.POStore.getAt(this.POStore.find('billid', billid));
                    if (record.data['termid'] != undefined && record.data['termid'] != "") {
                        this.Term.setValue(record.data['termid']);
                    }
                    if (this.users != null && this.users != undefined) {
                        if (this.isCustomer) {
                            if (record.data['salesPerson'] != undefined && record.data['salesPerson'] != "") {
                                this.users.setValue(record.data['salesPerson'])
                            }
                        } else {
                            if (record.data['agent'] != undefined && record.data['agent'] != "") {
                                this.users.setValue(record.data['agent']);
                            }
                        }
                    }
                    if (this.partialInvoiceCmb) {
                        this.partialInvoiceCmb.enable();
                    }
                    // populate dimension data
                    this.populateDimensionData(record);
                } else {
                    var perstore = null;
                    if (this.custVenOptimizedFlag) {
                        perstore = this.isCustomer ? Wtf.customerAccRemoteStore : Wtf.vendorAccRemoteStore;
                    } else {
                        perstore = this.isCustomer ? Wtf.customerAccStore : Wtf.vendorAccStore;
                    }
                    var index = perstore.find('accid', this.Name.getValue());
                    if (index != -1) {
                        var storerec = perstore.getAt(index);
                        this.Term.setValue(storerec.data['termid']);
                    }
                    this.users.reset();
                    if (this.partialInvoiceCmb) {
                        this.partialInvoiceCmb.reset();
                        this.partialInvoiceCmb.disable();
                        var id = this.Grid.getId();
                        var rowindex = this.Grid.getColumnModel().getIndexById(id + "partdisc");
                        if (rowindex >= 0) {
                            this.Grid.getColumnModel().setHidden(rowindex, true);
                        }
                    }
                }
            } 
            this.updateDueDate();
            var url = "";
            var soLinkFlag = false;
            var VQtoCQ = false;
            var sopolinkflag = false;
            var isForLinking = true;
            var linkingFlag = false; //For removing cross reference of DO-CI or GR-VI
            if (!this.isCustBill && !this.isOrder && !this.cash) {
                if (this.fromLinkCombo.getValue() == 0) {
                    url = this.isCustomer ? 'ACCSalesOrderCMN/getSalesOrderRows.do' : 'ACCPurchaseOrderCMN/getPurchaseOrderRows.do';
                } else if (this.fromLinkCombo.getValue() == 1) {
                    url = this.isCustomer ? "ACCInvoiceCMN/getDeliveryOrderRows.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrderRows.do";
                    linkingFlag = true;
                } else if (this.fromLinkCombo.getValue() == 2) {
                    url = this.isCustomer ? "ACCSalesOrderCMN/getQuotationRows.do" : "ACCPurchaseOrderCMN/getQuotationRows.do";
                    VQtoCQ = true;//Linking Quotation when creating invoice, we need to display Unit Price excluding row discount
                }
            } else {
                if (this.isCustomer) {
                    url = 'ACCSalesOrderCMN/getSalesOrderRows.do';
                } else {
                    url = 'ACCPurchaseOrderCMN/getPurchaseOrderRows.do';
                }
            }
            this.Grid.getStore().proxy.conn.url = url;
            this.Grid.loadPOGridStore(rec, soLinkFlag, VQtoCQ, linkingFlag, sopolinkflag, isForLinking, this.isInvoice);
        }
    },
    loadDataForProjectStatusReport: function() {
        this.isFromProjectStatusRep = false;
        var url = "ACCSalesOrderCMN/getSalesOrderRows.do";
        var rec = "";
        for (var i = 0; i < this.SOLinkedArr.length; i++) {
            rec += this.SOLinkedArr[i] + ',';
        }
        if (rec != "") {
            rec = rec.substring(0, rec.length - 1);
        }
        this.Grid.getStore().proxy.conn.url = url;
        this.Grid.loadPOGridStore(rec, false, false, false);
    },

    setSalesPerson: function(c, rec, ind) {
        this.users.setValue(rec.data['masterSalesPerson']);
    },

    getNextSequenceNumber: function(a, val) {
        if (!(a.getValue() == "NA")) {
            WtfGlobal.hideFormElement(this.Number);
            this.setTransactionNumber(true);
            var rec = WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
            var oldflag = rec != null ? rec.get('oldflag') : true;
            Wtf.Ajax.requestEx({
                url: "ACCCompanyPref/getNextAutoNumber.do",
                params: {
                    from: this.fromnumber,
                    sequenceformat: a.getValue(),
                    oldflag: oldflag
                }
            }, this, function(resp) {
                if (resp.data == "NA") {
                    WtfGlobal.showFormElement(this.Number);
                    this.Number.reset();
                    if (!this.isViewTemplate) {             // View mode- all fields should be disabled unconditionaly
                        this.Number.enable();
                    }
                } else {
                    this.Number.setValue(resp.data);
                    this.Number.disable();
                    WtfGlobal.hideFormElement(this.Number);
                }

            });
        } else {
            WtfGlobal.showFormElement(this.Number);
            this.Number.reset();
            if (!this.isViewTemplate) {                 // View mode- all fields should be disabled unconditionaly
                this.Number.enable();
            }
        }
    },
    getNextSequenceNumberDo: function(a, val) {
        if (!(a.getValue() == "NA")) {
            this.setTransactionNumberDo(true);
            var rec = WtfGlobal.searchRecord(this.sequenceFormatStoreDo, a.getValue(), 'id');
            var oldflag = rec != null ? rec.get('oldflag') : true;
            Wtf.Ajax.requestEx({
                url: "ACCCompanyPref/getNextAutoNumber.do",
                params: {
                    from: this.fromnumberDo,
                    sequenceformat: a.getValue(),
                    oldflag: oldflag
                }
            }, this, function(resp) {
                if (resp.data == "NA") {
                    this.no.reset();
                    this.no.enable();
                } else {
                    this.no.setValue(resp.data);
                    this.no.disable();
                }
            });
        } else {
            this.no.reset();
            this.no.enable();
        }
    },

    calDiscount: function() {
            return false;
    },

    save: function() {
        var incash = false;
        if (this.checkBeforeProceed(this.Number.getValue()))
        {
            if (this.moduleid == Wtf.Acc_Invoice_ModuleId && this.isDraft && this.autoGenerateDO.getValue()) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.youCannotGenerateDOWhileSavingInvoiceAsDraft")], 2);
                this.enableSaveButtons();
                return;
            }
            this.Number.setValue(this.Number.getValue().trim());
            var isValidCustomFields = this.tagsFieldset.checkMendatoryCombo();
            var southFormValid = true;
            if (this.cash) {
                southFormValid = this.SouthForm.hidden ? true : this.SouthForm.getForm().isValid();
            }
            var isValidNorthForm = this.NorthForm.getForm().isValid();
            if (!isValidNorthForm || !southFormValid || !isValidCustomFields) {
                this.enableSaveButtons();
                WtfGlobal.dispalyErrorMessageDetails(this.id + 'requiredfieldmessagepanel', this.getInvalidFields());
                this.NorthForm.doLayout();
                return;
            } else {
                Wtf.getCmp(this.id + 'requiredfieldmessagepanel').hide();
            }

                if (Wtf.serialwindowflag) {
                    var checkSerialWindow=this.checkSerialWindow();
                    if(checkSerialWindow)
                        return;
                }
                var productCountQuantityZero = 0;
                var allProductQtyZeroFlag = true;
                for (var i = 0; i < this.Grid.getStore().getCount() - 1; i++) {// excluding last row
                    var quantity = this.Grid.getStore().getAt(i).data['quantity'];
                    var rate = this.Grid.getStore().getAt(i).data['rate'];
                    if (!this.isExpenseInv && quantity > 0) {
                        allProductQtyZeroFlag = false;
                    } else if (!this.isExpenseInv && quantity == 0) {//For Counting how many rows with zero quantity
                        productCountQuantityZero++;
                    }
                    if (!this.isExpenseInv && (quantity === "" || quantity == undefined || quantity < 0)) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.QuantityforProduct") + " " + this.Grid.getStore().getAt(i).data['productname'] + " " + WtfGlobal.getLocaleText("acc.field.shouldbegreaterthanZero")], 2);
                        this.enableSaveButtons();
                        return;
                    }
                    if (rate === "" || rate == undefined || rate < 0) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.RateforProduct") + " " + this.Grid.getStore().getAt(i).data['productname'] + " " + WtfGlobal.getLocaleText("acc.field.cannotbeempty")], 2);
                        this.enableSaveButtons();
                        return;
                    }
                }
                var count = this.Grid.getStore().getCount();
                if (count <= 1) {//For Normal Empty Check
                    this.isExpenseInv ? WtfComMsgBox(117, 2) : WtfComMsgBox(33, 2);  //for exoense invoice change the message
                    this.enableSaveButtons();
                    return;
                }
                if (allProductQtyZeroFlag && !this.isExpenseInv) { //for quantity Check in case of mapped products
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ZeroQuantityAllProduct")], 2);
                    this.enableSaveButtons();
                    return;
                }
                if (this.getDiscount() > this.Grid.calSubtotal()) {
                    WtfComMsgBox(12, 2);
                    this.enableSaveButtons();
                    return;
                }
                if (!(this.isExpenseInv == true)) {
                    if (!WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_allowZeroUntiPriceForProduct)) {
                        if (this.Grid.calSubtotal() <= 0) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.TotalamountshouldbegreaterthanZero")], 2);
                            this.enableSaveButtons();
                            return;
                        }
                    }
                }
                if (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) == '137' && this.isSelfBilledInvoice) {// For Malasian Company
                    var checkMalesianData =this.checkMalesianData();
                    if (checkMalesianData)
                        return;
                }
                //check is there duplicate product in transaction
                var confirmMsg=this.checkDuplicate(productCountQuantityZero);
                if (confirmMsg != "") {
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), confirmMsg + '</center>', function(btn) {
                        if (btn == "yes") {
                            this.createRecords(incash);
                        } else {
                            this.enableSaveButtons();
                            return;
                        }
                    }, this);
                } else {
                    if (this.productOptimizedFlag == Wtf.Products_on_Submit && !this.isExpenseInv) {
                        this.checklastproduct(incash, count);
                    } else {
                        this.createRecords(incash);
                    }
                }
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.fxexposure.warning"), WtfGlobal.getLocaleText("acc.field.PleaseTryothervalueinInvoiceNumber")], 1);
            this.enableSaveButtons();
        }
   },
    createRecords: function(incash) {
        incash = this.cash;
        var rec = this.NorthForm.getForm().getValues();
        rec.gstCurrencyRate = this.gstCurrencyRate;
        rec.isselfbilledinvoice = this.isSelfBilledInvoice;
        if (rec.vendor == undefined && this.linkIDSFlag != undefined && this.linkIDSFlag) {
            rec.vendor = this.Name.getValue();
        }
        if (rec.customer == undefined && this.linkIDSFlag != undefined && this.linkIDSFlag) {
            rec.customer = this.Name.getValue();
        }
        this.isGenerateReceipt = this.generateReceipt.getValue();
        this.isAutoCreateDO = this.autoGenerateDO.getValue();
        rec.islockQuantity = this.lockQuantity.getValue();
        rec.billdate = WtfGlobal.convertToGenericDate(this.billDate.getValue());
        if (this.cash)
        {
            this.termid = "";
        }
        else
            this.updateDueDate("", "", "", true);
        rec.termid = this.termid;
        this.ajxurl = "";
        if (this.businessPerson == "Customer") {
            this.ajxurl = "ACC" + "Invoice/saveInvoice" + ".do";
        } else if (this.businessPerson == "Vendor") {
            this.ajxurl = "ACC" + "GoodsReceipt/saveGoodsReceipt" + ".do";
        }
        var currencychange = this.Currency.getValue() != WtfGlobal.getCurrencyID() && this.Currency.getValue() != "" && !this.isOrder;
        var msg = currencychange ? WtfGlobal.getLocaleText("acc.field.Currencyrateyouhaveappliedcannotbechanged") : "";
        var detail = this.Grid.getProductDetails();
        var validLineItem = this.Grid.checkDetails(this.Grid);
        if (validLineItem != "" && validLineItem != undefined) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), (WtfGlobal.getLocaleText("acc.msgbox.lineitem") + validLineItem)], 2);
            this.enableSaveButtons();
            return;
        }
        if (detail == undefined || detail == "[]") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.msg12")], 2);   //"Product(s) details are not valid."
            this.enableSaveButtons();
            return;
        }
        if (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_memo) == true && (rec.memo == ""))    //memo related setting wether option is true
        {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"), WtfGlobal.getLocaleText({
                key: "acc.common.memoempty",
                params: [WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_descriptionType)]
            }), function(btn) {
                if (btn != "yes") {
                    this.enableSaveButtons();
                    return;
                }
                this.checkiflinkdo(rec, detail, incash);
            }, this);
        } else {
            this.checkiflinkdo(rec, detail, incash);
        }    },
    
    
    checkSerialWindow: function() {
        var prodLength = this.Grid.getStore().data.items.length;
        for (var i = 0; i < prodLength - 1; i++)
        {
            var prodID = this.Grid.getStore().getAt(i).data['productid'];
            var prorec = this.Grid.productComboStore.getAt(this.Grid.productComboStore.find('productid', prodID));
            if (prorec == undefined) {
                prorec = this.Grid.getStore().getAt(i);
            }
            if (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_isBatchCompulsory) || WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_isSerialCompulsory) || WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_isLocationCompulsory) 
                    || WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_isWarehouseCompulsory) || WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_isRowCompulsory) || WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_isRackCompulsory) 
                    || WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_isBinCompulsory)) { //if company level option is on then only check batch and serial details
                if (!this.quotation && (prorec.data.isBatchForProduct || prorec.data.isSerialForProduct || prorec.data.isLocationForProduct || prorec.data.isWarehouseForProduct || prorec.data.isRowForProduct || prorec.data.isRackForProduct || prorec.data.isBinForProduct)) {
                    if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') {
                        var batchDetail = this.Grid.getStore().getAt(i).data['batchdetails'];
                        var productQty = this.Grid.getStore().getAt(i).data['quantity'];
                        var baseUOMRateQty = this.Grid.getStore().getAt(i).data['baseuomrate'];
                        if (batchDetail == undefined || batchDetail == "" || batchDetail == "[]") {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.bsdetail")], 2);   //Batch and serial no details are not valid.
                            this.enableSaveButtons();
                            return true;
                        } else {
                            var jsonBatchDetails = eval(batchDetail);
                            var batchQty = 0;
                            for (var batchCnt = 0; batchCnt < jsonBatchDetails.length; batchCnt++) {
                                if (jsonBatchDetails[batchCnt].quantity > 0) {
                                    if (prorec.data.isSerialForProduct) {
                                        batchQty = batchQty + parseInt(jsonBatchDetails[batchCnt].quantity);
                                    } else {
                                        batchQty = batchQty + parseFloat(jsonBatchDetails[batchCnt].quantity);
                                    }
                                }
                            }

                            if (batchQty != productQty * baseUOMRateQty) {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.bsdetail")], 2);
                                this.enableSaveButtons();
                                return true;
                            }
                        }
                    }
                    var quantity = this.Grid.getStore().getAt(i).data['quantity'];
                    if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') { // serial no for only inventory type of product
                        if (prorec.data.isSerialForProduct) {
                            var v = quantity;
                            v = String(v);
                            var ps = v.split('.');
                            var sub = ps[1];
                            if (sub != undefined && sub.length > 0) {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                                this.enableSaveButtons();
                                return true;
                            }
                        }
                    }
                }
            }
        }
    },
    
    checkMalesianData: function() {
        var rec = WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
        var selfBilledFromDate = (rec.data.selfBilledFromDate);
        var selfBilledToDate = (rec.data.selfBilledToDate);
        var purchaseInvoiceDate = this.billDate.getValue();
        if ((selfBilledFromDate == null || selfBilledFromDate == "")) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                msg: "Self-Billed Invoice Dates are not set for vendor " + rec.data.accname + ". Please set Self-billed Approval Start Date and Self-billed Approval Expiry Date first.", //this.closeMsg,
                buttons: Wtf.MessageBox.OK,
                animEl: 'mb9',
                icon: Wtf.MessageBox.QUESTION
            });
            return true;
        } else if (((selfBilledFromDate != null && selfBilledFromDate != "") && (selfBilledToDate != null && selfBilledToDate != ""))) {
            if (!(new Date(purchaseInvoiceDate).between(selfBilledFromDate, selfBilledToDate))) {
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                    msg: "Purchase Invoice Date should be between Self-billed Approval Start Date " + WtfGlobal.convertToDateOnly(rec.data.selfBilledFromDate) + " and Self-billed Approval Expiry Date " + WtfGlobal.convertToDateOnly(rec.data.selfBilledToDate), //this.closeMsg,
                    buttons: Wtf.MessageBox.OK,
                    animEl: 'mb9',
                    icon: Wtf.MessageBox.QUESTION
                });
                this.enableSaveButtons();
                return true;
            }
        } else if (selfBilledToDate == null || selfBilledToDate == "") {
            if (!(new Date(purchaseInvoiceDate) >= (selfBilledFromDate))) {
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                    msg: "Purchase Invoice Date should be after Self-billed Approval Start Date " + WtfGlobal.convertToDateOnly(rec.data.selfBilledFromDate), //this.closeMsg,
                    buttons: Wtf.MessageBox.OK,
                    animEl: 'mb9',
                    icon: Wtf.MessageBox.QUESTION
                });
                this.enableSaveButtons();
                return true;
            }
        }
    },
    
    checkDuplicate: function(productCountQuantityZero) {
        var isDuplicate = false;
        var duplicateval = ", ";
        if (!this.isExpenseInv && WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_isDuplicateItems)) {
            var prodLength = this.Grid.getStore().data.items.length;
            for (var i = 0; i < prodLength - 1; i++)
            {
                var prodID = this.Grid.getStore().getAt(i).data['productid'];
                for (var j = i + 1; j < prodLength - 1; j++) {
                    var productid = this.Grid.getStore().getAt(j).data['productid'];
                    if (prodID == productid) {
                        isDuplicate = true;
                        var prorec = this.Grid.getStore().getAt(this.Grid.getStore().find('productid', prodID));//done for ERP-13480 ticket
                        if (duplicateval.indexOf(", " + prorec.data.pid + ",") == -1) {
                            duplicateval += prorec.data.pid + ", ";//Add duplicate product id 

                        }
                    }
                }
            }
        }
        if (isDuplicate == true) {
            duplicateval = duplicateval.substring(2, (duplicateval.length - 2));
        }
        var confirmMsg = "";
        if (productCountQuantityZero > 0 && isDuplicate == true) {
            confirmMsg = duplicateval + " " + WtfGlobal.getLocaleText("acc.field.duplicateproduct") + " and " + WtfGlobal.getLocaleText("acc.field.ZeroQuantitySomeProduct");
        } else if (productCountQuantityZero > 0) {
            confirmMsg = WtfGlobal.getLocaleText("acc.field.ZeroQuantitySomeProduct");
        } else if (isDuplicate == true) {//duplicate product case
            confirmMsg = duplicateval + " " + WtfGlobal.getLocaleText("acc.field.duplicateproduct") + ". " + WtfGlobal.getLocaleText("acc.msgbox.Doyouwanttoproceed");
        }
        return confirmMsg;
    },
    /****************************Generic code*********************************/
    showConfirmAndSave: function(rec, detail, incash) {
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.savdat"),
            msg: this.EditisAutoCreateDO ? (this.businessPerson == "Customer" ? WtfGlobal.getLocaleText("acc.invoice.msg16") : WtfGlobal.getLocaleText("acc.invoice.msg19")) : WtfGlobal.getLocaleText("acc.invoice.msg7"),
            buttons: Wtf.MessageBox.YESNO,
            icon: Wtf.MessageBox.INFO,
            width: 300,
            scope: {
                scopeObject: this
            },
            fn: function(btn) {
                if (btn != "yes") {
                    this.mailFlag = false;
                    this.saveOnlyFlag = false;
                    this.scopeObject.enableSaveButtons();
                    return;
                }
                this.scopeObject.finalSave(rec, detail, incash);
            }
        }, this);
    },

    /*************************************************************/

    finalSave: function(rec, detail, incash) {
        this.mailFlag = true;
        rec.taxid = this.Tax.getValue();
        rec.isfavourite = false;
        if (!this.copyInv) {
            if ((this.record && this.record !== undefined) && (this.record.get('isfavourite') !== null || this.record.get('isfavourite') !== undefined)) {
                rec.isfavourite = this.record.get('isfavourite');
            }
        }
        rec.taxamount = this.caltax();
        if (this.isExpenseInv) {
            rec.expensedetail = detail;
            rec.isExpenseInv = this.isExpenseInv;
            if (this.copyInv) {
                var expenseDetails = eval(detail);
                var hasAccessFlag = false;
                var accountsNotAccessList = "";
                for (var i = 0; i < expenseDetails.length; i++) {
                    var gridRec = expenseDetails[i];
                    var accRec = WtfGlobal.searchRecord(this.Grid.accountStore, gridRec.accountid, 'accountid');
                    if (accRec != null) {
                        var hasAccess = accRec.get('hasAccess');
                        if (!hasAccess) {
                            accountsNotAccessList = accountsNotAccessList + accRec.get('accountname') + ", ";
                            hasAccessFlag = true;
                        }
                    }
                }
                if (accountsNotAccessList != "") {
                    accountsNotAccessList = accountsNotAccessList.substring(0, accountsNotAccessList.length - 2);
                }
                if (hasAccessFlag) {
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                        msg: WtfGlobal.getLocaleText("acc.field.Accounts") + accountsNotAccessList + " " + WtfGlobal.getLocaleText("acc.field.iscurrentlydeactivated"),
                        width: 370,
                        buttons: Wtf.MessageBox.OK,
                        icon: Wtf.MessageBox.WARNING,
                        scope: this
                    });
                    this.enableSaveButtons();
                    return;
                }
            }
        }
        else
            rec.detail = detail;
        var custFieldArr = this.tagsFieldset.createFieldValuesArray();
        this.msg = WtfComMsgBox(27, 4, true);
        rec.subTotal = this.Grid.calSubtotal()
        this.applyCurrencySymbol();
        rec.isOpeningBalanceOrder = this.isOpeningBalanceOrder;
        rec.currencyid = this.Currency.getValue();
        rec.externalcurrencyrate = this.externalcurrencyrate;
        rec.posttext = this.postText;
        rec.istemplate = this.transactionType;
        rec.moduletempname = this.isTemplate;
        rec.templatename = this.moduleTemplateName.getValue();
        if (this.isGeneratedRecurringInvoice != undefined && this.isGeneratedRecurringInvoice == 1) {
            rec.Oldinvoiceid = this.record.data.billid;
            rec.isGeneratedRecurringInvoice = 1;
            rec.generatedDate = WtfGlobal.convertToGenericDate(this.onDate);//Use to Exclude Invoice from Outstanding order Report
        }
        if (this.copyInv && this.record && this.record.data.contract) {
            rec.contractId = this.record.data.contract;
        }
        if (custFieldArr.length > 0)
            rec.customfield = JSON.stringify(custFieldArr);
        rec.invoicetermsmap = this.getInvoiceTermDetails();
        if (this.Grid.deleteStore != undefined && this.Grid.deleteStore.data.length > 0)
            rec.deletedData = this.getJSONArray(this.Grid.deleteStore, false, 0);
        rec.number = this.Number.getValue();
        rec.linkNumber = (this.PO != undefined && this.PO.getValue() != "") ? this.PO.getValue() : "";
        if (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) == '137') {// For Malasian Company
            var isInvoiceLinkedWithTaxAppliedDO = false;

            if (this.PO != undefined && this.PO.getValue() != "") {
                var linkNumberArray = this.PO.getValue().split(",");
                for (var i = 0; i < linkNumberArray.length; i++) {
                    var porecord = this.POStore.getAt(this.POStore.find('billid', linkNumberArray[i]));
                    if (porecord.data.isAppliedForTax) {
                        isInvoiceLinkedWithTaxAppliedDO = true;
                    }
                }
            }
            rec.isInvoiceLinkedWithTaxAppliedDO = isInvoiceLinkedWithTaxAppliedDO;
        }
        rec.fromLinkCombo = this.fromLinkCombo.getRawValue();
        rec.duedate = WtfGlobal.convertToGenericDate(this.DueDate.getValue());
        rec.billdate = WtfGlobal.convertToGenericDate(this.billDate.getValue());
        rec.shipdate = WtfGlobal.convertToGenericDate(this.shipDate.getValue());
        rec.invoiceid = (this.copyInv || (this.quotation && !this.isCustomer && this.ispurchaseReq)) ? "" : this.billid;
        rec.doid = this.DeliveryOrderid;
        rec.mode = (this.isOrder ? (this.isCustBill ? 51 : 41) : (this.isCustBill ? 13 : 11));
        rec.incash = incash;
        rec.partialinv = (this.partialInvoiceCmb) ? this.partialInvoiceCmb.getValue() : false;
        this.totalAmount = rec.subTotal + rec.taxamount - this.getDiscount();
        rec.includeprotax = (this.includeProTax) ? this.includeProTax.getValue() : false;
        rec.includingGST = (this.includingGST) ? this.includingGST.getValue() : false;
        rec.landedInvoiceNumber = this.invoiceList.getValue();
        if (this.autoGenerateDO.getValue() || this.EditisAutoCreateDO) {
            var seqFormatRecDo = WtfGlobal.searchRecord(this.sequenceFormatStoreDo, this.sequenceFormatComboboxDo.getValue(), 'id');
            rec.seqformat_oldflagDo = seqFormatRecDo != null ? seqFormatRecDo.get('oldflag') : false;
            rec.numberDo = this.no.getValue();
            rec.sequenceformatDo = this.sequenceFormatComboboxDo.getValue();
        }
        var seqFormatRec = WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
        rec.sequenceformat = this.sequenceFormatCombobox.getValue();
        rec.seqformat_oldflag = seqFormatRec != null ? seqFormatRec.get('oldflag') : false;
        this.getAddressDetails(rec);
        if (this.isAutoCreateDO || this.EditisAutoCreateDO) {
            rec.isAutoCreateDO = this.EditisAutoCreateDO ? this.EditisAutoCreateDO : this.isAutoCreateDO;
            rec.fromLinkComboAutoDO = this.isCustomer ? "Customer Invoice" : "Vendor Invoice";
        }
        if (this.capitalGoodsAcquired) {
            rec.isCapitalGoodsAcquired = this.capitalGoodsAcquired.getValue();
        }
        if (this.isRetailPurchase) {
            rec.isRetailPurchase = this.isRetailPurchase.getValue();
        }
        if (this.importService) {
            rec.importService = this.importService.getValue();
        }
        if (this.deliveryTime) {
            rec.deliveryTime = this.deliveryTime.getValue();
        }
        if (incash) {
            rec.pmtmethod = this.pmtMethod.getValue();
            if (!this.SouthForm.hidden) {//when payment type bank or card
                var paydetail = this.SouthForm.GetPaymentFormData();
                rec.paydetail = paydetail;
                if (this.SouthForm.paymentStatus.getValue() == "Cleared") {
                    var index = this.pmtStore.findBy(function(rec) {
                        var parentname = rec.data['methodid'];
                        if (parentname == this.pmtMethod.getValue())
                            return true;
                        else
                            return false
                    })
                    rec.bankaccid = this.pmtStore.getAt(index).data.accountid;
                    rec.startdate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true));
                    rec.enddate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false));
                }
            }
        }
        rec['termsincludegst'] = this.termsincludegst;
        rec.isDraft = this.isDraft ? this.isDraft : false;
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url: this.ajxurl,
            params: rec
        }, this, this.genSuccessResponse, this.genFailureResponse);

    },

    handleProductTypeForConsignment: function() {
        // In Consignment link case select product of service type product reset and prompt msg
        if (this.invoiceList != undefined && this.invoiceList.getValue() != "") {
            var productid;
            if (this.Grid != undefined) {
                for (var i = 0; i < this.Grid.getStore().getCount(); i++) {
                    productid = this.Grid.getStore().getAt(i).get("productid");

                    if ((productid != undefined || productid != "") && (this.Grid != undefined && this.Grid.getStore().getCount() > 0)) {
                        var index = this.Grid.productComboStore.find('productid', productid);
                        if (index != -1) {
                            var productType = this.Grid.productComboStore.getAt(index).get("type");
                            if (productType == "Service") {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.consignmentCaseProductSelectMsg")], 2);

                                // For reset all related fields
                                var customer = this.Name.getValue();
                                this.loadStore();
                                this.Name.setValue(customer);
                                this.updateData();
                            }
                        }
                    }
                }
            }
        }
    },
    loadTransStore: function(productid) {
        if (this.Name.getValue() != "") {
            var customer = (this.businessPerson == "Vendor") ? "" : this.Name.getValue();
            var vendor = (this.businessPerson == "Vendor") ? this.Name.getValue() : "";
            if ((productid == undefined || productid == "") && this.Grid.getStore().getCount() > 0) {
                productid = this.Grid.getStore().getAt(0).get("productid");
            }
            // In Consignment link case select product of service type product reset and prompt msg
            this.handleProductTypeForConsignment();

            this.lastTransPanel.Store.on('load', function() {
                Wtf.getCmp('south' + this.id).doLayout();
            }, this);
            if (productid) {
                this.lastTransPanel.productid = productid;
                this.lastTransPanel.Store.load({
                    params: {
                        start: 0,
                        limit: 5,
                        prodfiltercustid: customer,
                        prodfilterventid: vendor,
                        productid: productid
                    }
                });
            }
        }
    },
    removeTransStore: function() {
        this.lastTransPanel.Store.removeAll();
        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body, {productname: "&nbsp;&nbsp;&nbsp;&nbsp;", productid: 0, qty: 0, soqty: 0, poqty: 0});
    },
    updateData: function() {
        var customer = this.Name.getValue();
        if (Wtf.getCmp("showaddress" + this.id)) {
            Wtf.getCmp("showaddress" + this.id).enable();
        }
        var currentTaxItem = WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
        var actualTaxId = currentTaxItem != null ? currentTaxItem.get('taxId') : "";

        if (actualTaxId == undefined || actualTaxId == "" || actualTaxId == null) {// if customer/vendor is not mapped with tax then check that is their mapping account is mapped with tax or not, if it is mapped take account tax
            actualTaxId = currentTaxItem != null ? currentTaxItem.get('mappedAccountTaxId') : "";
        }
        if (actualTaxId != undefined && actualTaxId != "" && actualTaxId != null) {
            this.isTaxable.setValue(true);
            this.Tax.enable();
            this.isTaxable.enable();
            this.Tax.setValue(actualTaxId);
        } else {
            this.isTaxable.setValue(false);
            this.Tax.setValue('');
            this.Tax.disable();
        }
        if (this.Grid) {
            this.Grid.affecteduser = this.Name.getValue();
        }
        this.loadTransStore();
        Wtf.Ajax.requestEx({
            url: "ACC" + this.businessPerson + "CMN/getCurrencyInfo.do",
            params: {
                mode: 4,
                customerid: customer,
                isBilling: this.isCustBill
            }
        }, this, this.setCurrencyInfo);
        if (this.fromPO) {
            this.fromPO.enable();
        }
    },

    updateDueDate: function(a, val, index, isSave) {
        var term = null;
        var rec = null;
        var validTillDate = null;
        if (this.quotation) {
            if (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_noOfDaysforValidTillField) != -1) {
                validTillDate = new Date(this.billDate.getValue()).add(Date.DAY, WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_noOfDaysforValidTillField));
            }
        }
        if (validTillDate != null && this.quotation) {
            this.validTillDate.setValue(validTillDate)
        }
        if (this.Term.getValue() != "" && this.Term.getValue() != null && this.Term.getValue() != undefined) {
            rec = this.Term.store.getAt(this.Term.store.find('termid', this.Term.getValue()));
            if (rec != null && rec != undefined) // Added null check (in cash case get null). For Cash transaction Term is not present.
                term = new Date(this.billDate.getValue()).add(Date.DAY, rec.data.termdays);
        }
        else
            term = this.billDate.getValue();

        if (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_shipDateConfiguration)) {
            if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) {
                if (this.shipDate.getValue() != "") {
                    if (this.Term.getValue() != "" && this.Term.getValue() != null && this.Term.getValue() != undefined) {
                        rec = this.Term.store.getAt(this.Term.store.find('termid', this.Term.getValue()));
                        term = new Date(this.shipDate.getValue()).add(Date.DAY, rec.data.termdays);
                    } else {
                        term = this.shipDate.getValue();
                    }
                } else {
                    term = null;
                }
            }
        }

        if (term != null) {
            if (!(isSave != undefined && isSave != "" && isSave == true)) {
                this.NorthForm.getForm().setValues({duedate: term});
            }
        }
        if (this.Grid) {
            this.Grid.billDate = this.billDate.getValue()
        }
        rec = this.Term.store.getAt(this.Term.store.find('termid', this.Term.getValue()));
        if (rec != null && rec != undefined)
            this.termid = rec.data.termid;
    },
    genSuccessResponse: function(response, request) {
        WtfGlobal.resetAjaxTimeOut();
        this.enableSaveButtons();
        this.RecordID = response.SOID != undefined ? response.SOID : response.invoiceid;
        if (this.moduleid == Wtf.Acc_Invoice_ModuleId && Wtf.getCmp("InvoiceListEntry") != undefined && response.success) {
            var msgTitle = this.titlel;//ERP-12682
            Wtf.getCmp("InvoiceListEntry").Store.on('load', function() {
                WtfComMsgBox([msgTitle, response.msg], response.success * 2 + 1);
            }, Wtf.getCmp("InvoiceListEntry").Store, {
                single: true
            });
        } else if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && Wtf.getCmp("GRListEntry") != undefined && response.success) {
            var msgTitle = this.titlel;
            Wtf.getCmp("GRListEntry").Store.on('load', function() {
                WtfComMsgBox([msgTitle, response.msg], response.success * 2 + 1);
            }, Wtf.getCmp("GRListEntry").Store, {
                single: true
            });
        }
        else {
            WtfComMsgBox([this.titlel, response.msg], response.success * 2 + 1);
        }
        var rec = this.NorthForm.getForm().getValues();
        if (!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType) || !WtfGlobal.EnableDisable(this.uPermType, this.printPermType)) {//after saving
            this.exportRecord = rec;
            this.exportRecord['billid'] = response.billid || response.invoiceid;
            this.exportRecord['billno'] = response.billno || response.invoiceNo;
            this.exportRecord['amount'] = (this.moduleid == 22 || this.moduleid == 23 || this.moduleid == 20) ? this.totalAmount : response.amount; //ERP-9467 Added SO module id.
            this.exportRecord['isexpenseinv'] = response.isExpenseInv != undefined ? response.isExpenseInv : false; //To export the good receipt of Expense Type.
            this.singlePrint.exportRecord = this.exportRecord;
            this.singleRowPrint.exportRecord = this.exportRecord;
        }
        if (response.success) {
            if (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_deliveryPlanner) && this.moduleid == Wtf.Acc_Invoice_ModuleId) {
                Wtf.getCmp("pushtoplanner" + this.heplmodeid + this.id).enable();
                this.fullShippingAddress = response.fullShippingAddress;
            }
            if (this.productOptimizedFlag == Wtf.Show_all_Products) {
                if (!this.isCustBill) {
                    Wtf.productStoreSales.reload();
                    Wtf.productStore.reload();   //Reload all product information to reflect new quantity, price etc                
                }
            }
            if (this.isGenerateReceipt) {
                var mode = "";
                if (this.businessPerson == "Customer") {
                    if (!this.quotation && !this.isOrder) {
                        mode = (this.isCustBill ? 23 : 22);
                    }
                }
                var fileName = "Cash Sales Payment Recieved " + response.invoiceNo;
                var selRec = "&amount=" + this.totalAmount + "&bills=" + response.invoiceid + "&customer=Cash&accname=" + response.accountName + "&personid=" + response.accountid;//+"&address="+recData.address;
                Wtf.get('downloadframe').dom.src = "ACCExportRecord/exportRecords.do?mode=" + mode + "&rec=" + selRec + "&personid=" + response.accountid + "&filename=" + fileName + "&filetype=pdf"
            }
            if (this.isTemplate) {
                this.ownerCt.remove(this);
            }
            if (this.saveOnlyFlag) {
                this.loadUserStoreForInvoice(response, request);
                this.disableComponent();
                this.response = response;
                this.request = request;
                return;
            }
            this.currenctAddressDetailrec = "";//after saveandcreatenew this variable need to clear it old values. 
            this.lastTransPanel.Store.removeAll();
            this.symbol = WtfGlobal.getCurrencySymbol();
            this.currencyid = WtfGlobal.getCurrencyID();
            this.loadStore();
            this.fromPO.disable();
            if (this.cash && !this.SouthForm.hidden) {
                this.SouthForm.hide();
                this.setCashMethod();
            }
            this.currencyStore.load();
            this.Currency.setValue(WtfGlobal.getCurrencyID()); // Reset to base currency 
            this.externalcurrencyrate = 0; //Reset external exchange rate for new Transaction.
            this.isClosable = true;       //Reset Closable flag to avoid unsaved Message.
            this.termStore.reload(); // Reset Purchase/Sales Term store when clicked "Save and create new" button          
            if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) {
                this.InvoiceStore.reload();
            }
            Wtf.dirtyStore.product = true;
            var currentTaxItem = WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
            var actualTaxId = currentTaxItem != null ? currentTaxItem.get('taxId') : "";
            if (actualTaxId != undefined && actualTaxId != "" && actualTaxId != null) {
                this.isTaxable.setValue(true);
                this.Tax.enable();
                this.Tax.setValue(actualTaxId);
            } else {
                this.isTaxable.setValue(false);
                this.Tax.setValue('');
                this.Tax.disable();
            }
            this.postText = "";
            WtfGlobal.resetCustomFields(this.tagsFieldset);
            this.fireEvent('update', this);
            this.amountdue = 0;
        }
    },
    genFailureResponse: function(response) {
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    callEmailWindowFunction: function(response, request) {
        if (response.pendingApproval) {
            var titleMsg = this.getLables();
            WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.information'), titleMsg + ' ' + WtfGlobal.getLocaleText("acc.field.ispendingforapprovalSoyoucannotsendmailrightnow")], 3);
            return;
        }
        if (this.CustomStore != null) {
            var rec = this.CustomStore.getAt(0);
            var label = "";
            if (this.cash) {
                if (this.isCustomer) {
                    label = WtfGlobal.getLocaleText("acc.field.CashSalesReceipt");
                    if (rec.data.withoutinventory) {
                        callEmailWin("emailwin", rec, label, 11, true, false, false, true);
                    } else {
                        callEmailWin("emailwin", rec, label, 2, true, false, false, true);
                    }
                } else {
                    label = WtfGlobal.getLocaleText("acc.field.CashPurchaseReceipt");
                    if (rec.data.withoutinventory) {
                        callEmailWin("emailwin", rec, label, 15, false, false, false, true);
                    } else {
                        callEmailWin("emailwin", rec, label, 6, false, false, false, true);
                    }
                }
            } else {
                if (this.isCustomer) {
                    label = WtfGlobal.getLocaleText("acc.field.CustomerInvoice");
                    if (rec.data.withoutinventory) {
                        callEmailWin("emailwin", rec, label, 11, true, true);
                    } else {
                        callEmailWin("emailwin", rec, label, 2, true, true);
                    }
                } else {
                    label = WtfGlobal.getLocaleText("acc.agedPay.venInv");
                    if (rec.data.withoutinventory) {
                        callEmailWin("emailwin", rec, label, 15, false, true);
                    } else {
                        callEmailWin("emailwin", rec, label, 6, false, true);
                    }
                }
            }
        }
    },
    getLables: function() {
        var label = "";
        if (this.cash) {
            if (this.isCustomer) {
                label = WtfGlobal.getLocaleText("acc.field.CashSalesReceipt");
            } else {
                label = WtfGlobal.getLocaleText("acc.field.CashPurchaseReceipt");
            }
        } else {
            if (this.isCustomer) {
                label = WtfGlobal.getLocaleText("acc.field.CustomerInvoice");
            } else {
                label = WtfGlobal.getLocaleText("acc.agedPay.venInv");
            }
        }
        return label;
    },
    disableComponent: function() { // disable following component in case of save button press.
        if (this.fromLinkCombo && this.fromLinkCombo.getValue() === '') {
            this.fromLinkCombo.clearValue();
        }
        if (this.PO && this.PO.getValue() === '') {
            this.handleEmptyText = true;
            this.PO.clearValue();
        }
        if (this.savencreateBttn) {
            this.savencreateBttn.disable();
        }
        if (this.saveBttn) {
            this.saveBttn.disable();
        }
        if (this.saveAsDraftBttn) {
            this.saveAsDraftBttn.disable();
        }
        if (Wtf.getCmp("posttext" + this.id)) {
            Wtf.getCmp("posttext" + this.id).disable();
        }
        if (Wtf.getCmp("showaddress" + this.id)) {
            Wtf.getCmp("showaddress" + this.id).disable();
        }
        if (this.Grid) {
            var GridStore = this.Grid.getStore();
            var count2 = GridStore.getCount();
            var lastRec2 = GridStore.getAt(count2 - 1);
            GridStore.remove(lastRec2);
        }
        if (this.GridPanel) {
            if (this.modeName == "autocashpurchase" || this.modeName == "autogoodsreceipt") {
                this.ProductGrid.purgeListeners();
            } else {
                this.GridPanel.disable();
            }
        } else {
            this.Grid.purgeListeners();
        }
        if (this.NorthForm) {
            this.NorthForm.disable();
        }
        if (this.southPanel) {
            this.southPanel.disable();
        }
        if (this.SouthForm) {
            this.SouthForm.disable();
        }
    },
    enableSaveButtons: function() {
        if(this.saveAsDraftBttn)
        this.saveAsDraftBttn.enable();
        this.savencreateBttn.enable();
        this.saveBttn.enable();
    },
    disableSaveButtons: function() {
        if(this.saveAsDraftBttn)
        this.saveAsDraftBttn.disable();
        this.savencreateBttn.disable();
        this.saveBttn.disable();
    },
    loadUserStoreForInvoice: function(response, request) {
        var customRec = Wtf.data.Record.create([
            {name: 'billid'},
            {name: 'journalentryid'},
            {name: 'entryno'},
            {name: 'billto'},
            {name: 'discount'},
            {name: 'currencysymbol'},
            {name: 'orderamount'},
            {name: 'isexpenseinv'},
            {name: 'currencyid'},
            {name: 'shipto'},
            {name: 'mode'},
            {name: 'billno'},
            {name: 'date', type: 'date'},
            {name: 'duedate', type: 'date'},
            {name: 'shipdate', type: 'date'},
            {name: 'personname'},
            {name: 'personemail'},
            {name: 'personid'},
            {name: 'shipping'},
            {name: 'othercharges'},
            {name: 'partialinv', type: 'boolean'},
            {name: 'amount'},
            {name: 'amountdue'},
            {name: 'termdays'},
            {name: 'termname'},
            {name: 'incash'},
            {name: 'taxamount'},
            {name: 'taxid'},
            {name: 'orderamountwithTax'},
            {name: 'taxincluded', type: 'boolean'},
            {name: 'taxname'},
            {name: 'deleted'},
            {name: 'amountinbase'},
            {name: 'memo'},
            {name: 'externalcurrencyrate'},
            {name: 'ispercentdiscount'},
            {name: 'discountval'},
            {name: 'crdraccid'},
            {name: 'creditDays'},
            {name: 'isRepeated'},
            {name: 'porefno'},
            {name: 'costcenterid'},
            {name: 'costcenterName'},
            {name: 'interval'},
            {name: 'intervalType'},
            {name: 'startDate', type: 'date'},
            {name: 'nextDate', type: 'date'},
            {name: 'expireDate', type: 'date'},
            {name: 'repeateid'},
            {name: 'status'},
            {name: 'archieve', type: 'int'},
            {name: 'withoutinventory', type: 'boolean'},
            {name: 'rowproductname'},
            {name: 'rowquantity'},
            {name: 'rowrate'},
            {name: 'rowprdiscount'},
            {name: 'rowprtaxpercent'},
            {name: 'agent'}
        ]);
        var customStoreUrl = "";
            customStoreUrl= this.businessPerson=="Customer" ? "ACCInvoiceCMN/getInvoicesMerged.do" : "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
        this.CustomStore = new Wtf.data.GroupingStore({
            url: customStoreUrl,
            scope: this,
            baseParams: {
                archieve: 0,
                deleted: false,
                nondeleted: false,
                cashonly: (this.cash == undefined) ? false : this.cash,
                creditonly: false,
                consolidateFlag: false,
                companyids: companyids,
                enddate: '',
                pendingapproval: response.pendingApproval,
                gcurrencyid: gcurrencyid,
                userid: loginid,
                isfavourite: false,
                startdate: '',
                ss: request.params.number
            },
            sortInfo: {
                field: 'companyname',
                direction: 'ASC'
            },
            groupField: 'companyname',
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'count'
            }, customRec)
        });
        this.CustomStore.on('load', this.enableButtons(), this);
        this.CustomStore.load();
    },
    enableButtons: function() {
        if (Wtf.getCmp("emailbut" + this.id)) {
            Wtf.getCmp("emailbut" + this.id).enable();
        }
        if (Wtf.getCmp("exportpdf" + this.id)) {
            Wtf.getCmp("exportpdf" + this.id).enable();
        }
        if (Wtf.getCmp("printSingleRecord" + this.id)) { //Enabling Print record button after saving
            Wtf.getCmp("printSingleRecord" + this.id).enable();
        }
        if (Wtf.getCmp("RecurringSO")) {
            Wtf.getCmp("RecurringSO").enable();
        }
    },
    loadStore: function() {
        if (!this.isEdit && !this.copyInv) {
            this.Grid.getStore().removeAll();
        }
        this.PO.setDisabled(true);
        this.fromLinkCombo.setDisabled(true);
        if (this.isTemplate) {
            this.createTransactionAlsoOldVal = this.createTransactionAlso;
            this.oldTempNameVal = this.moduleTemplateName.getValue();
        }
        if (this.isEdit) {//in edit case need to preserve some data befor resetall
            this.number = this.Number.getValue();
        }
        this.resetField();
        this.Term.clearValue();
        if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) {
            this.invoiceList.clearValue();
        }
        if (this.isExpenseInv) {
            this.autoGenerateDO.setValue(false);
            this.autoGenerateDO.disable();
            this.includingGST.setValue(false);
            this.includingGST.disable();
            WtfGlobal.hideFormElement(this.autoGenerateDO);
            WtfGlobal.hideFormElement(this.invoiceList);
        } else {
            this.autoGenerateDO.reset();
            if (!this.isTemplate) {
                this.autoGenerateDO.enable();
            }
//            this.includingGST.reset();   ***need to confirm
            this.includingGST.enable();
        }
        if (this.isEdit) {//in edit case need to preserve some data befor resetall
            this.billDate.setValue(Wtf.serverDate);
        }
        this.sequenceFormatStore.load();
        if (this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId)
            this.sequenceFormatStoreDo.load();
        if (this.isTemplate) {
            this.moduleTemplateName.setValue(this.oldTempNameVal);
            if (this.createTransactionAlsoOldVal) {
                this.createAsTransactionChk.setValue(true);
                if (!this.isViewTemplate) {               // View mode- all fields should be disabled unconditionaly
                    this.Number.enable();
                }
                this.sequenceFormatCombobox.enable();
            }
        }
        this.setTransactionNumber();
        if (this.fromPO) {
            this.fromPO.enable();
        }
        if (this.fromLinkCombo) {
            this.fromLinkCombo.setDisabled(true);
            this.fromLinkCombo.clearValue();
        }
        this.fromPO.setValue(false);
        if (!this.isEdit && !this.copyInv) {
            this.Grid.getStore().removeAll();
        }

        if (this.partialInvoiceCmb) {
            this.partialInvoiceCmb.disable();
            var id = this.Grid.getId();
            var rowindex = this.Grid.getColumnModel().getIndexById(id + "partdisc");
            if (rowindex != -1) {
                this.Grid.getColumnModel().setHidden(rowindex, true);
            }
        }
        this.showGridTax(null, null, true);
        this.Grid.symbol = undefined; // To reset currency symbol. BUG Fixed #16202
        this.Grid.updateRow(null);
        this.resetForm = true;
        var currentTaxItem = WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
        var actualTaxId = currentTaxItem != null ? currentTaxItem.get('taxId') : "";
        if (actualTaxId != undefined && actualTaxId != "" && actualTaxId != null) {
            this.isTaxable.setValue(true);
            this.Tax.enable();
            this.Tax.setValue(actualTaxId);
        } else {
            this.Tax.setValue("");
            this.Tax.setDisabled(true);				// 20148 fixed
            this.isTaxable.setValue(false);
        }
//        this.template.setValue(Wtf.Acc_Basic_Template_Id);
        this.currencyStore.load({params: {mode: 201, transactiondate: WtfGlobal.convertToGenericDate(new Date())}});
        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body, {productname: "&nbsp;&nbsp;&nbsp;&nbsp;", productid: 0, qty: 0, soqty: 0, poqty: 0});
        this.currencyStore.on("load", function(store) {
            if (this.resetForm) {
                if (this.Currency.getValue() != "" && WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid") == null) {
                    callCurrencyExchangeWindow();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg1")], 2); //"Please set Currency Exchange Rates"
                } else {
                    this.isCurrencyLoad = true;
                    this.Currency.setValue(WtfGlobal.getCurrencyID());
                    this.currencyid = WtfGlobal.getCurrencyID();
                    this.applyCurrencySymbol();
                    this.showGridTax(null, null, true);
                    if (this.isEdit) {
                        if (this.record.data.includeprotax) {
                            this.includeProTax.setValue(true);
                            this.showGridTax(null, null, false);
                        } else {
                            this.includeProTax.setValue(false);
                            this.showGridTax(null, null, true);
                        }
                    }
                    var currentTaxItem = WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
                    var actualTaxId = currentTaxItem != null ? currentTaxItem.get('taxId') : "";
                    if (actualTaxId != undefined && actualTaxId != "" && actualTaxId != null) {
                        this.isTaxable.setValue(true);
                        this.Tax.enable();
                        this.Tax.setValue(actualTaxId);
                    } else {
                        this.isTaxable.setValue(false);
                        this.Tax.setValue('');
                        this.Tax.disable();
                    }
                    if (this.isEdit) {
                        this.setProductAndTransactionTaxValues();
                    }
                    this.resetForm = false;
                }
            }
        }, this);
    },

    resetField: function() {
        this.moduleTemplateName.reset();
        this.templateModelCombo.reset();
        this.ShowOnlyOneTime.enable();
        this.Currency.reset();
        this.PO.reset();
        this.sequenceFormatCombobox.reset();
        this.Number.reset();
        this.billDate.reset();
        this.PORefNo.reset();
        this.autoGenerateDO.reset();
        this.CostCenter.reset();
//        this.youtReftxt.reset();
//        this.delytermtxt.reset();
//        this.invoiceTotxt.reset();
        this.shipDate.reset();
        this.Term.reset();
        this.DueDate.reset();
        this.Memo.reset();
        this.shipvia.reset();
        this.fob.reset();
        this.includeProTax.reset();
//        this.validTillDate.reset();
        this.partialInvoiceCmb.reset();
//        this.template.reset();
//        this.templateID.reset();
        this.users.reset();
        this.generateReceipt.reset();
        this.autoGenerateDO.reset();
        this.sequenceFormatComboboxDo.reset();
        this.no.reset();
//        this.delydatetxt.reset();
//        this.projecttxt.reset();
//        this.depttxt.reset();
//        this.requestortxt.reset();
//        this.mernotxt.reset();
        this.Name.reset();
        this.SouthForm.getForm().reset();
    },
    // moving code
    setDate: function() {
        var height = 0;
        if (!this.quotation && this.isOrder && !this.isCustomer && !this.isCustBill && ((BCHLCompanyId.indexOf(companyid) != -1)))
            height = 485;
        if (!this.isCustomer && !this.isCash && !this.isOrder && !this.quotation)
            height = 400;
        if (height >= 178)
            this.NorthForm.setHeight(height);
        if (!this.isEdit || this.isCopyFromTemplate) {
//            this.Discount.setValue(0);
            if (this.isOpeningBalanceOrder) {
                this.billDate.setValue(this.getFinancialYRStartDatesMinOne(true));
            } else {
                this.billDate.setValue(Wtf.serverDate);//(new Date());
            }
        }
    },
    addCreditTerm: function() {
        callCreditTerm('credittermwin');
        Wtf.getCmp('credittermwin').on('update', function() {
            this.termds.reload();
        }, this);
    },
    addPerson: function(isEdit, rec, winid, isCustomer) {
        callBusinessContactWindow(isEdit, rec, winid, isCustomer);
        var tabid = this.isCustomer ? 'contactDetailCustomerTab' : 'contactDetailVendorTab';
        Wtf.getCmp(tabid).on('update', function() {
            this.isCustomer ? Wtf.customerAccStore.load() : Wtf.vendorAccStore.reload();
        }, this);
    },
    setTransactionNumber: function(isSelectNoFromCombo) {
        this.quotation = false;
        if (!this.isEdit || this.copyInv) {
            var temp = this.isCustBill * 1000 + this.isCustomer * 100 + this.isOrder * 10 + this.cash * 1 + this.quotation * 1;
            var temp2 = 0;
            var format = "";
            switch (temp) {
                case 0:
                    format = WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_autogoodsreceipt);
                    temp2 = Wtf.autoNum.GoodsReceipt;
                    break;
                case 1:
                    format = WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_autocashpurchase);
                    temp2 = Wtf.autoNum.CashPurchase;
                    break;
                case 100:
                    format = WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_autoinvoice);
                    temp2 = Wtf.autoNum.Invoice;
                    break;
                case 101:
                    format = WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_autocashsales);
                    temp2 = Wtf.autoNum.CashSale;
                    break;
            }
            if (isSelectNoFromCombo) {
                this.fromnumber = temp2;
            } else if (format && format.length > 0 && (!this.isTemplate || this.createTransactionAlso)) {
                WtfGlobal.fetchAutoNumber(temp2, function(resp) {
                    if (this.isEdit) {
                        this.Number.setValue(resp.data)
                    }
                }, this);
            }
        }
    },
    displayMsg: function() {
        WtfComMsgBox(29, 4, true);
    },
    invoiceCreationJSON: function() {
        this.ajxUrl = "CommonFunctions/getInvoiceCreationJson.do";
        var params = {
            transactiondate: this.transdate,
            loadtaxstore: true,
            moduleid: this.moduleid,
            loadcurrencystore: true,
            loadtermstore: true
        }
        Wtf.Ajax.requestEx({url: this.ajxUrl, params: params}, this, this.successCallback, this.failureCallback);
    },
    loadInitialStore: function() {
        this.sequenceFormatStore.load();
        if (this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId)
            this.sequenceFormatStoreDo.load();
        this.isCustomer ? Wtf.salesPersonFilteredByCustomer.load() : Wtf.agentStore.load(); //new store is used for salesperson combo 
        if (!this.custVenOptimizedFlag) {
            if (this.isCustomer) {
                Wtf.customerAccStore.load();
            } else {
                Wtf.vendorAccStore.reload();
            }
        }
        if (!this.custVenOptimizedFlag) {
            this.isCustomer ? chkcustaccload() : chkvenaccload();
        }
    },
    dueDateCheck: function() {
        if (this.DueDate.getValue().getTime() < this.billDate.getValue().getTime()) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.msg11")], 2);    //"The Due Date should be greater than the Order Date."
            this.DueDate.setValue(this.billDate.getValue());
        }
    },

    setValues: function(billid) {
        if (billid.indexOf(",") == -1) {  //In MultiSelection if the user select only one                              
            var rec = this.POStore.getAt(this.POStore.find('billid', billid));
            if (!this.isCustBill && !this.isOrder && !this.cash && this.isCustomer) {
                if (this.fromLinkCombo.getValue() == 1 && !rec.data['includeprotax']) {// in case of CI creation if DO is being linked which is containing row level tax, following fields will not be reset
                    this.includeProTax.setValue(false);
                    this.showGridTax(null, null, true);
                }
            }
            if (this.users != null && this.users != undefined) {
                if (this.isCustomer) {
                    if (rec.data['salesPerson'] != undefined && rec.data['salesPerson'] != "") {
                        this.users.setValue(rec.data['salesPerson'])
                    }
                } else {
                    if (rec.data['agent'] != undefined && rec.data['agent'] != "") {
                        this.users.setValue(rec.data['agent']);
                    }
                }
            }
            this.Memo.setValue(rec.data['memo']);
            this.shipDate.setValue(rec.data['shipdate']);
//            this.validTillDate.setValue(rec.data['validdate']);
            if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) { // set value only in VI module
                this.invoiceList.setValue(rec.data['landedInvoiceID']);
            }
            if ((this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) || (this.moduleid == Wtf.Acc_Invoice_ModuleId && !this.cash)) {
                this.Term.setValue(rec.data['termid']);
            }
            this.postText = rec.data['posttext'];
            this.shipvia.setValue(rec.data['shipvia']);
            this.fob.setValue(rec.data['fob']);

//            if (rec.data["discounttotal"] && this.Discount) {
//                this.Discount.setValue(rec.data["discounttotal"]);
////                this.perDiscount.setValue(rec.data["discountispertotal"]);
//            }
            if (rec.data['taxid'] != "") {
                this.Tax.enable();
                this.isTaxable.setValue(true);
                this.Tax.setValue(rec.data['taxid']);
            } else {
                this.Tax.disable();
                this.isTaxable.reset();
                this.Tax.reset();
            }
            var perstore = null;
            if (this.custVenOptimizedFlag) {
                perstore = this.isCustomer ? Wtf.customerAccRemoteStore : Wtf.vendorAccRemoteStore;
            } else {
                perstore = this.isCustomer ? Wtf.customerAccStore : Wtf.vendorAccStore
            }
            var index = perstore.find('accid', this.Name.getValue());
            if (index != -1) {
                var storerec = perstore.getAt(index);
                this.Term.setValue(storerec.data['termid']);
            }
            this.CostCenter.setValue(rec.data.costcenterid);
        } else { //if the user select multiple values
            this.clearComponentValues();
        }
    },
    clearComponentValues: function() {
        this.Memo.setValue('');
        this.shipDate.setValue('');
//        this.validTillDate.setValue('');
        this.shipvia.setValue('');
        this.fob.setValue('');
        this.loadTransStore();
//        this.Discount.setValue(0);
//        this.perDiscount.setValue(false);
        this.CostCenter.setValue('');
        if (this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId && !this.cash) { // set value only in VI module
            this.invoiceList.setValue('');
        }
    },
    onCurrencyChangeOnly: function() {
        this.fromPO.reset();
        this.fromLinkCombo.reset();
        this.fromLinkCombo.setDisabled(true);
        this.PO.reset();
        this.PO.setDisabled(true);
        if (this.partialInvoiceCmb) {
            this.partialInvoiceCmb.reset();
            this.partialInvoiceCmb.disable();
            var id = this.Grid.getId();
            var rowindex = this.Grid.getColumnModel().getIndexById(id + "partdisc");
            if (rowindex != -1) {
                this.Grid.getColumnModel().setHidden(rowindex, true);
            }
        }
//        this.Discount.setValue(0);
//        this.perDiscount.setValue(false);
        var currentTaxItem = WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
        var actualTaxId = currentTaxItem != null ? currentTaxItem.get('taxId') : "";
        if (actualTaxId != undefined && actualTaxId != "" && actualTaxId != null) {
            this.isTaxable.setValue(true);
            this.Tax.enable();
            this.Tax.setValue(actualTaxId);
        } else {
            this.Tax.disable();
            this.isTaxable.reset();
            this.Tax.reset();
        }
        this.includeProTax.setValue(false);
        this.showGridTax(null, null, true);
        this.Grid.getStore().removeAll();
        this.Grid.addBlankRow();
        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body, {productname: "&nbsp;&nbsp;&nbsp;&nbsp;", qty: 0, soqty: 0, poqty: 0});
    },

    getAddressWindow: function() {
        var addressRecord = "";
        var isCopy = "";
        var isEdit = "";
        addressRecord = this.record;
        isCopy = this.copyInv;
        isEdit = this.isEdit;
        var custvendorid = this.Name.getValue();
        if (this.linkRecord && this.singleLink) {     //when user link single record
            addressRecord = this.linkRecord;
        }
        callAddressDetailWindow(addressRecord, isEdit, isCopy, custvendorid, this.currenctAddressDetailrec, this.isCustomer, this.viewGoodReceipt, this.isViewTemplate, this.singleLink);
        Wtf.getCmp('addressDetailWindow').on('update', function(config) {
            this.currenctAddressDetailrec = config.currentaddress;
        }, this);
    },
    getCostAndMarginWindow: function() {
        callCostAndMarginWindow(this.Grid.getStore(), this.productComboStore);
    },
    pushToPlanner: function() {
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.areYouSureYouWantToPushToPlanner"), function(btn) {
            if (btn == "yes") {
                this.remarkWin = new Wtf.Window({
                    height: 270,
                    width: 360,
                    maxLength: 1000,
                    title: WtfGlobal.getLocaleText("acc.field.RemarksBySales"), // "Remarks By Sales",
                    bodyStyle: 'padding:5px;background-color:#f1f1f1;',
                    iconCls: getButtonIconCls(Wtf.etype.deskera),
                    autoScroll: true,
                    allowBlank: false,
                    layout: 'border',
                    items: [{
                            region: 'north',
                            border: false,
                            height: 70,
                            bodyStyle: 'background-color:#ffffff;border-bottom:1px solid #bfbfbf;',
                            html: getTopHtml(WtfGlobal.getLocaleText("acc.field.RemarksBySales"), "", "../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
                        }, {
                            region: 'center',
                            border: false,
                            layout: 'form',
                            bodyStyle: 'padding:5px;',
                            items: [this.remarkField = new Wtf.form.TextArea({
                                    fieldLabel: WtfGlobal.getLocaleText("acc.field.AddRemark*"),
                                    width: 200,
                                    height: 100,
                                    allowBlank: false,
                                    maxLength: 1024
                                })]
                        }],
                    modal: true,
                    buttons: [{
                            text: WtfGlobal.getLocaleText("acc.OK"), // "OK",
                            id: 'savePushToDeliveryPlanner' + this.heplmodeid + this.id,
                            scope: this,
                            handler: function() {
                                Wtf.getCmp('savePushToDeliveryPlanner' + this.heplmodeid + this.id).disable();

                                if (this.remarkField.getValue().trim() == "") {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleaseenterremark")], 2);
                                    Wtf.getCmp('savePushToDeliveryPlanner' + this.heplmodeid + this.id).enable();  // ERP-10529
                                    return;
                                }

                                if (!this.remarkField.isValid()) {
                                    this.remarkField.markInvalid(WtfGlobal.getLocaleText("acc.field.Maximumlengthofthisfieldis1024"));
                                    Wtf.getCmp('savePushToDeliveryPlanner' + this.heplmodeid + this.id).enable();  // ERP-10529
                                    return;
                                }
                                Wtf.Ajax.requestEx({
                                    url: "ACCDeliveryPlanner/savePushToDeliveryPlanner.do",
                                    params: {
                                        invoiceID: this.RecordID,
                                        deliveryLocation: this.fullShippingAddress,
                                        pushTime: WtfGlobal.convertToGenericDate(new Date()),
                                        deliveryDate: WtfGlobal.convertToGenericDate(this.shipDate.getValue()),
                                        deliveryTime: this.deliveryTime.getValue(),
                                        remarksBySales: this.remarkField.getValue()
                                    }
                                }, this, function(response) {
                                    if (response.success) {
                                        this.remarkWin.close();
                                        Wtf.getCmp("pushtoplanner" + this.heplmodeid + this.id).disable();
                                        getDeliveryPlannerTabView();
                                    } else {
                                        Wtf.getCmp('savePushToDeliveryPlanner' + this.heplmodeid + this.id).enable();
                                        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
                                        if (response.msg) {
                                            msg = response.msg;
                                        }
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                                    }
                                }, function(response) {
                                    Wtf.getCmp('savePushToDeliveryPlanner' + this.heplmodeid + this.id).enable();
                                    var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
                                    if (response.msg) {
                                        msg = response.msg;
                                    }
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                                });
                            }
                        }, {
                            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                            scope: this,
                            handler: function() {
                                this.remarkWin.close();
                            }
                        }]
                });
                this.remarkWin.show();
            }
        }, this);
    },
    showDO: function() {
        WtfGlobal.showFormElement(this.sequenceFormatComboboxDo);
        WtfGlobal.showFormElement(this.no);
        this.no.allowBlank = false;

    },
    hideDO: function() {
        WtfGlobal.hideFormElement(this.sequenceFormatComboboxDo);
        WtfGlobal.hideFormElement(this.no);
        this.no.allowBlank = true;
    },
    setTransactionNumberDo: function(isSelectNoFromCombo) {
        var format = this.isCustomer ? WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_autodo) : WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_autogro);
        var temp2 = this.isCustomer ? Wtf.autoNum.DeliveryOrder : Wtf.autoNum.GoodsReceiptOrder;
        if (isSelectNoFromCombo) {
            this.fromnumberDo = temp2;
        } else if (format && format.length > 0) {
            WtfGlobal.fetchAutoNumber(temp2, function(resp) {
                if (this.isEdit)
                    this.no.setValue(resp.data)
            }, this);
        }
    },
    addCostCenter: function() {
        callCostCenter('addCostCenterWin');
    },
    setPMData: function() {
        if (this.cash) {
            if (this.isEdit || this.copyInv) {
                if (this.record != null && this.record.data.methodid != undefined && this.record.data.methodid != "") {//when we copy older record in which payment not available 
                    this.pmtMethod.setValue(this.record.data.methodid);                                          //methodid will be undefined so 
                    this.ShowCheckDetails(null, this.record);
                    var type = this.record.data.detailtype;
                    if (type == 1) { //card
                        this.SouthForm.refNo.setValue(this.record.data.cardrefno);
                        this.SouthForm.nameOnCard.setValue(this.record.data.nameoncard);
                        this.SouthForm.expDate.setValue(this.record.data.cardexpirydate);
                        this.SouthForm.cardNo.setValue(this.record.data.cardno);
                        this.SouthForm.cardType.setValue(this.record.data.cardtype);
                    } else if (type == 2) { //bank
                        this.SouthForm.checkNo.setValue(this.record.data.chequeno);
                        this.SouthForm.bank.setValue(this.record.data.bankname);
                        this.SouthForm.PostDate.setValue(this.record.data.chequedate);
                        this.SouthForm.description.setValue(this.record.data.chequedescription);
                    }
                }
            } else { //create new case setting cash as default payment method  
                this.setCashMethod();
            }
        }
    },
    setCashMethod: function() {
        if (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_cashaccount) != "" && WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_cashaccount) != undefined) {
            var index = this.pmtStore.findBy(function(rec) {
                var parentname = rec.data['accountid'];
                if (parentname == WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_cashaccount))
                    return true;
                else
                    return false
            })
            if (index != -1) {
                var rec = this.pmtStore.getAt(index);
                this.pmtMethod.setValue(rec.data.methodid);
                this.pmtMethodAcc.setValue(rec.data.accountname);
            }
        }
    },
    ShowCheckDetails: function(combo, rec) {
        var index = this.pmtStore.find('methodid', rec.data['methodid']);
        var mthAccounID = "";
        var mthAccountName = "";
        if (index != -1) {
            mthAccountName = this.pmtStore.getAt(index).get('accountname');
            mthAccounID = this.pmtStore.getAt(index).get('methodid');
        }
        rec.paydetail = this.SouthForm.GetPaymentFormData();
        this.pmtMethodAcc.setValue(mthAccountName);
        this.SouthForm.ShowCheckDetails(rec.data['detailtype']);
        if (rec.data['detailtype'] == 1) { //card
            this.SouthForm.hide();
            WtfGlobal.hideFormElement(this.SouthForm.checkNo);
            this.SouthForm.checkNo.allowBlank = true;
            WtfGlobal.hideFormElement(this.SouthForm.paymentStatus);
            this.SouthForm.paymentStatus.allowBlank = true;
            WtfGlobal.hideFormElement(this.SouthForm.bank);
            this.SouthForm.bank.allowBlank = true;
            WtfGlobal.hideFormElement(this.SouthForm.clearanceDate);
            this.SouthForm.clearanceDate.allowBlank = true;
            WtfGlobal.hideFormElement(this.SouthForm.description);
            this.SouthForm.description.allowBlank = true;
            WtfGlobal.hideFormElement(this.SouthForm.PostDate);//Cheque Date
            this.SouthForm.PostDate.allowBlank = true;
            WtfGlobal.showFormElement(this.SouthForm.expDate);
            WtfGlobal.showFormElement(this.SouthForm.nameOnCard);
            this.SouthForm.nameOnCard.allowBlank = false;
            WtfGlobal.showFormElement(this.SouthForm.cardType);
            this.SouthForm.cardType.allowBlank = false;
            WtfGlobal.showFormElement(this.SouthForm.refNo);
            this.SouthForm.refNo.allowBlank = false;
            WtfGlobal.showFormElement(this.SouthForm.cardNo);
            this.SouthForm.cardNo.allowBlank = false;
//            this.SouthForm.cheque.setTitle(WtfGlobal.getLocaleText("acc.nee.44"));
            this.SouthForm.show();
            this.SouthForm.getForm().items.items[1].disabled = false;
            this.SouthForm.cheque.doLayout();
            this.SouthForm.show();
        } else if (rec.data['detailtype'] == 2) {//bank
            if (!this.isEdit) {
                this.SouthForm.setNextChequeNumber(mthAccounID);
            }
            this.SouthForm.hide();
            WtfGlobal.showFormElement(this.SouthForm.checkNo);
            WtfGlobal.showFormElement(this.SouthForm.bank);
            this.SouthForm.bank.allowBlank = false;
            WtfGlobal.showFormElement(this.SouthForm.description);
            WtfGlobal.showFormElement(this.SouthForm.PostDate);
            this.SouthForm.PostDate.allowBlank = false;
            WtfGlobal.hideFormElement(this.SouthForm.paymentStatus);
            this.SouthForm.paymentStatus.allowBlank = true;
            WtfGlobal.hideFormElement(this.SouthForm.clearanceDate);
            this.SouthForm.clearanceDate.allowBlank = true;
            WtfGlobal.hideFormElement(this.SouthForm.expDate);
            this.SouthForm.expDate.allowBlank = true;
            WtfGlobal.hideFormElement(this.SouthForm.nameOnCard);
            this.SouthForm.nameOnCard.allowBlank = true;
            WtfGlobal.hideFormElement(this.SouthForm.cardType);
            this.SouthForm.cardType.allowBlank = true;
            WtfGlobal.hideFormElement(this.SouthForm.refNo);
            this.SouthForm.refNo.allowBlank = true;
            WtfGlobal.hideFormElement(this.SouthForm.cardNo);
            this.SouthForm.cardNo.allowBlank = true;
//            this.SouthForm.cheque.setTitle(WtfGlobal.getLocaleText("acc.nee.43"));
            this.SouthForm.show();
            this.SouthForm.cheque.doLayout();
            this.SouthForm.getForm().items.items[1].disabled = false;
            this.SouthForm.show();
        } else {
            this.SouthForm.hide();
        }
    },
    moduleTemplateSection: function() {
        this.moduleTemplateStore.on('load', function(store) {
            if (this.isCopyFromTemplate && this.templateId != undefined) {
                this.templateModelCombo.setValue(this.templateId);
            }
        }, this);
        this.templateModelCombo = new Wtf.form.FnComboBox({
            fieldLabel: (this.isViewTemplate ? WtfGlobal.getLocaleText("acc.designerTemplateName") : WtfGlobal.getLocaleText("acc.field.SelectTemplate")),
            id: "templateModelCombo" + this.heplmodeid + this.id,
            store: this.moduleTemplateStore,
            valueField: 'templateId',
            displayField: 'templateName',
            hideTrigger: this.isViewTemplate,
            hirarchical: true,
            emptyText: WtfGlobal.getLocaleText("acc.invoice.grid.template.emptyText"),
            mode: 'local',
            typeAhead: true,
            hidden: this.isTemplate || this.quotation,
            hideLabel: this.isTemplate || this.quotation,
            forceSelection: true,
            selectOnFocus: true,
            addNoneRecord: true,
            width: 240,
            triggerAction: 'all',
            scope: this,
            listeners: {
                'select': {
                    fn: function() {
                        if (this.templateModelCombo.getValue() != "") {
                            this.loadingMask = new Wtf.LoadMask(document.body, {
                                msg: WtfGlobal.getLocaleText("acc.msgbox.50")
                            });
                            this.loadingMask.show();
                            var templateId = this.templateModelCombo.getValue();
                            var recNo = this.moduleTemplateStore.find('templateId', templateId);
                            var rec = this.moduleTemplateStore.getAt(recNo);
                            var moduleId = rec.get('moduleRecordId');
                            this.SelectedTemplateStore.load({
                                params: {
                                    billid: moduleId,
                                    isForTemplate: true
                                }
                            });
                        } else {
                            WtfGlobal.resetCustomFields(this.tagsFieldset);
                            this.loadStore();
                            this.Grid.getStore().removeAll();
                            this.Grid.addBlankRow();
                            this.isEdit = false;
                            this.copyInv = false;
                        }
                    },
                    scope: this
                }
            }
        });

        this.moduleTemplateName = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.campaigndetails.campaigntemplate.templatename"),
            name: 'moduletempname',
            hidden: !this.isTemplate,
            hideLabel: !this.isTemplate,
            id: "moduletempname" + this.id,
            width: 240,
            maxLength: 50,
            scope: this,
            allowBlank: !this.isTemplate
        });
        this.createAsTransactionChk = new Wtf.form.Checkbox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.CreateTransactionAlso"),
            name: 'createAsTransactionChkbox',
            hidden: !this.isTemplate,
            hideLabel: !this.isTemplate,
            cls: 'custcheckbox',
            width: 10
        });
        this.createAsTransactionChk.on('check', function() {
            if (this.createAsTransactionChk.getValue()) {
                this.createTransactionAlso = true;
                WtfGlobal.showFormElement(this.sequenceFormatCombobox);
                WtfGlobal.showFormElement(this.Number);
                var seqRec = this.sequenceFormatStore.getAt(0)
                this.sequenceFormatCombobox.setValue(seqRec.data.id);
                var count = this.sequenceFormatStore.getCount();
                for (var i = 0; i < count; i++) {
                    seqRec = this.sequenceFormatStore.getAt(i)
                    if (seqRec.json.isdefaultformat == "Yes") {
                        this.sequenceFormatCombobox.setValue(seqRec.data.id)
                        break;
                    }
                }
                this.getNextSequenceNumber(this.sequenceFormatCombobox);
                this.Number.allowBlank = false;
                if (!this.isViewTemplate) {            // View mode- all fields should be disabled unconditionaly
                    this.Number.enable();
                }
                this.sequenceFormatCombobox.enable();
                this.billDate.enable();
                this.autoGenerateDO.enable();
                this.generateReceipt.enable();
                this.setTransactionNumber();
                this.billDate.setValue(Wtf.serverDate);
            } else {
                this.createTransactionAlso = false;
                this.Number.disable();
                this.sequenceFormatCombobox.disable();
                this.billDate.disable();
                this.autoGenerateDO.setValue(false);
                this.autoGenerateDO.disable();
                this.generateReceipt.setValue(false);
                this.generateReceipt.disable();
                this.sequenceFormatCombobox.reset();
                this.Number.setValue('');
                this.Number.allowBlank = true;
                WtfGlobal.hideFormElement(this.sequenceFormatCombobox);
                WtfGlobal.hideFormElement(this.Number);
            }
            if (this.isTemplate && this.createAsTransactionChk.getValue()) {
                if (this.saveAsDraftBttn)
                    this.saveAsDraftBttn.setDisabled(false);     //refer ticket ERP-13119
            } else if (this.isTemplate) {
                if (this.saveAsDraftBttn)
                    this.saveAsDraftBttn.setDisabled(true);     //refer ticket ERP-13119
            }
        }, this);
        this.SelectedTemplateStore.on('load', this.fillData, this);
        this.SelectedTemplateStore.on('loadexception', function() {
            this.loadingMask.hide();
        }, this);
        this.moduleTemplateStore.load();
    },
    loadEditableGrid: function() {
        this.StoreUrl = "";
        this.subGridStoreUrl = "";
        if (this.businessPerson == 'Customer') {
            this.storeMode =12;
            this.StoreUrl = "ACCInvoiceCMN/getInvoices.do";
            this.subGridStoreUrl = "ACCInvoiceCMN/getInvoiceRows.do";
        } else {
            this.storeMode =  12;
            this.StoreUrl ="ACCGoodsReceiptCMN/getGoodsReceipts.do";
            this.subGridStoreUrl = "ACCGoodsReceiptCMN/getGoodsReceiptRows.do";
        }
        if (this.Grid) {
            this.Grid.billDate = this.billDate.getValue()
        }
        this.billid = this.record.data.billid;
        var mode = this.isCustBill ? 17 : 14;
        this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;
        this.Grid.getStore().load({params: {bills: this.billid, mode: mode, isexpenseinv: this.isExpenseInv, isCopyInvoice: this.copyInv}});
        this.EditisAutoCreateDO = false;
            Wtf.Ajax.requestEx({
                url: "ACCInvoiceCMN/getDOFromInvoice.do",
                params: {
                    invoiceId: this.billid,
                    CallFromCI: this.isCustomer ? true : false//true:get DO from invoice and false : get GRO from Vendor Invoice
                }
            }, this, function(response) {
                if (response.data && response.data.length > 0) {
                    if (this.copyInv) {
                        this.autoGenerateDO.setValue(true);
                        var sequenceformatid = response.data[0].sequenceformatDo;
                        if (sequenceformatid == "NA" || sequenceformatid == undefined) {
                            this.sequenceFormatComboboxDo.setValue("NA");
                            this.sequenceFormatComboboxDo.disable();
                            this.getNextSequenceNumberDo(this.sequenceFormatComboboxDo);
                            this.no.setValue("");
                        } else {
                            var index = this.sequenceFormatStoreDo.find('id', sequenceformatid);
                            if (index != -1) {
                                this.sequenceFormatComboboxDo.setValue(sequenceformatid);
                            } else {  //sequence format get deleted then NA is set
                                this.sequenceFormatComboboxDo.setValue("NA");
                            }
                            this.getNextSequenceNumberDo(this.sequenceFormatComboboxDo);//need to show next number in number because it is not hidden.
                        }
                    } else if (this.isEdit) {
                        this.EditisAutoCreateDO = true;
                        this.sequenceFormatComboboxDo.setValue(response.data[0].sequenceformatDo)
                        this.no.setValue(response.data[0].SequenceNumDO);
                        this.DeliveryOrderid = response.data[0].DeliveryOrderID;
                        if (this.DeliveryOrderid != "" && this.DeliveryOrderid != null && this.DeliveryOrderid != undefined) {
                            this.autoGenerateDO.setValue(true);
                        } else {
                            this.autoGenerateDO.setValue(false);
                        }
                        this.sequenceFormatComboboxDo.disable();
                        this.autoGenerateDO.disable();
                        WtfGlobal.showFormElement(this.sequenceFormatComboboxDo);
                        WtfGlobal.showFormElement(this.no);
                    }
                } else {
                    if (this.isEdit) {
                        this.autoGenerateDO.setValue(false);
                        if (this.isExpenseInv) {
                            this.autoGenerateDO.disable();
                            WtfGlobal.hideFormElement(this.autoGenerateDO);
                            WtfGlobal.hideFormElement(this.invoiceList);
                        }
                    }
                }
            }, function(response) {
            });
    },

    setPOLinks: function() {
        this.fromPO.enable();
        this.fromPO.setValue(true);
        this.fromLinkCombo.enable();
        this.fromLinkCombo.setValue(0);
        this.PO.enable()
        this.POStore.load();
    },
    pmdata: function() {
        if (this.isFromProjectStatusRep) {
            this.selectedCustomerStore.on('load', function() {
                var rec = this.selectedCustomerStore.getAt(0);
                if (this.Name)
                    this.Name.setValue(rec.get('accid'));
            }, this);
            this.selectedCustomerStore.load({
                params: {
                    selectedCustomerIds: this.selectedCustomerIds
                }
            });
            this.setPOLinks();
        }
    },
    beforeTabChange: function(a, newTab, currentTab) {
        if (currentTab != null && newTab != currentTab) {
            if (!this.isExpenseInv) {
                this.autoGenerateDO.setValue(false);
                this.autoGenerateDO.disable();
                this.includingGST.setValue(false);
                this.includingGST.disable();
                WtfGlobal.hideFormElement(this.includingGST);
                WtfGlobal.hideFormElement(this.autoGenerateDO);
                WtfGlobal.hideFormElement(this.invoiceList);
            } else {
                this.autoGenerateDO.reset();
                this.autoGenerateDO.enable();
                this.includingGST.reset();
                this.includingGST.enable();
            }
            if (this.importService) {
                this.importService.reset();
            }
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"), this.isExpenseInv ? WtfGlobal.getLocaleText("acc.invoice.msg4") : WtfGlobal.getLocaleText("acc.invoice.msg5"), function(btn) { ///"Switching to "+(this.isExpenseInv?"Inventory":"Expense")+" section will empty the data filled so far in "+(this.isExpenseInv?"Expense":"Inventory")+" section. Do you wish to continue?",function(btn){
                if (btn == "yes") {
                    (this.productDetailsTpl.isVisible()) ? this.productDetailsTpl.setVisible(false) : this.productDetailsTpl.setVisible(true);
                    a.suspendEvents();
                    a.activate(newTab);
//                    this.Discount.setValue(0);
                    if (this.capitalGoodsAcquired) {
                        this.capitalGoodsAcquired.reset();
                        if (!this.isExpenseInv) {
                            WtfGlobal.hideFormElement(this.capitalGoodsAcquired);
                        } else {
                            WtfGlobal.showFormElement(this.capitalGoodsAcquired);
                        }
                    }
                    if (this.isRetailPurchase) {
                        this.isRetailPurchase.reset();
                        if (!this.isExpenseInv) {
                            WtfGlobal.hideFormElement(this.isRetailPurchase);
                        } else {
                            WtfGlobal.showFormElement(this.isRetailPurchase);
                        }
                    }
                    var currentTaxItem = WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
                    var actualTaxId = currentTaxItem != null ? currentTaxItem.get('taxId') : "";
                    if (actualTaxId != undefined && actualTaxId != "" && actualTaxId != null) {
                        this.isTaxable.setValue(true);
                        this.Tax.enable();
                        this.Tax.setValue(actualTaxId);
                    } else {
                        this.isTaxable.setValue(false);
                        this.Tax.setValue("");
                        this.Tax.disable();
                    }
                    a.resumeEvents();
                    this.onGridChange(newTab, currentTab);
                    this.showGridTax(null, null, !this.includeProTax.getValue());       // Show/hide Product tax and Tax Amount for Inventory/Expense Tab               
                }
            }.createDelegate(this), this)
            return false;
        }
        else {
            return true;
        }
    },
    onGridChange: function(newTab) {
        this.Grid.getStore().removeAll();
        this.Grid.addBlankRow();
        this.Grid = newTab;
        this.Tax.store = this.Grid.taxStore;
        this.isExpenseInv = !this.isExpenseInv; //work fine in case of 2 tabs
        this.applyCurrencySymbol();
        if (this.southCalTemp.body != undefined)
            var subtotal = 0.00;
        var tax = 0.00;
        var taxAndSubtotal = this.Grid.calLineLevelTax();
        if (this.includeProTax.getValue()) {
            subtotal = WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[0] - taxAndSubtotal[1], this.symbol)
            tax = WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[1], this.symbol);
        } else {
            subtotal = WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(), this.symbol)
            tax = WtfGlobal.addCurrencySymbolOnly(this.caltax(), this.symbol);
        }
        this.tplSummary.overwrite(this.southCalTemp.body, {subtotal: subtotal, discount: WtfGlobal.addCurrencySymbolOnly(this.getDiscount(), this.symbol), totalamount: WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(), this.symbol), tax: tax, aftertaxamt: WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount() + this.caltax(), this.symbol), totalAmtInBase: WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(), WtfGlobal.getCurrencySymbol()), amountdue: WtfGlobal.addCurrencySymbolOnly(this.amountdue, WtfGlobal.getCurrencySymbol())});
    },
    checkMemo: function(rec, detail, incash) {
        if (this.businessPerson == "Vendor" && !this.isOrder) {//Only for cash purchase and vendor invoice
            Wtf.Ajax.requestEx({
                url: "ACCReports/getAccountsExceedingBudget.do",
                params: {
                    detail: detail,
                    stdate: this.getDates(true).format("M d, Y h:i:s A"),
                    enddate: this.getDates(false).format("M d, Y h:i:s A"),
                    isExpenseInv: this.isExpenseInv,
                    billdate: WtfGlobal.convertToGenericDate(this.billDate.getValue())
                }
            }, this, function(response) {
                if (response.data && response.data.length > 0) {
                    var accMsg = WtfGlobal.getLocaleText("acc.field.FollowingAccountsareexceedingtheirMonthlyBudgetLimit") + "<br><br><center>";
                    var budgetMsg = "";
                    for (var i = 0; i < response.data.length; i++) {
                        var recTemp = response.data[i];
                        if (!this.isCustBill)
                            budgetMsg = (recTemp.productName == "" ? "" : "<b>" + WtfGlobal.getLocaleText("acc.field.Product") + "</b>" + recTemp.productName + ",") + " <b>" + WtfGlobal.getLocaleText("acc.field.Account") + " </b>" + recTemp.accountName + ", <b>" + WtfGlobal.getLocaleText("acc.field.Balance") + " </b>" + recTemp.accountBalance + ", <b>" + WtfGlobal.getLocaleText("acc.field.Budget") + "</b>" + recTemp.accountBudget;
                        else
                            budgetMsg = (recTemp.productName == "" ? "" : "<b>" + WtfGlobal.getLocaleText("acc.field.JobDescription") + " </b>" + recTemp.productName + ",") + " <b>" + WtfGlobal.getLocaleText("acc.field.Account") + " </b>" + recTemp.accountName + ", <b>" + WtfGlobal.getLocaleText("acc.field.Balance") + "</b>" + recTemp.accountBalance + ", <b>" + WtfGlobal.getLocaleText("acc.field.Budget") + " </b>" + recTemp.accountBudget;
                        accMsg += budgetMsg + "<br>";
                    }
                    accMsg += "<br>" + WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed") + "</center>";
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), accMsg, function(btn) {
                        if (btn != "yes") {
                            this.enableSaveButtons();
                            return;
                        }
                        this.checkLimit(rec, detail, incash);
                    }, this);
                } else {
                    this.checkLimit(rec, detail, incash);
                }
            }, function(response) {
                this.checkLimit(rec, detail, incash);
            });
        } else {
            this.checkLimit(rec, detail, incash);
        }
    },
    checkLimit: function(rec, detail, incash) {
        if (!this.quotation && !this.isOrder && !this.cash && !this.isDraft) {
            if (rec != null && rec != undefined && this.calTotalAmount() != null)
            {
                if (rec.customer != null && rec.customer != "")
                {
                    rec.customerid = rec.customer;
                }
                rec.totalSUM = this.calTotalAmount() + this.caltax();
                Wtf.Ajax.requestEx({
                    url: "ACC" + this.businessPerson + "CMN/get" + this.businessPerson + "Exceeding" + (this.businessPerson == "Vendor" ? "Debit" : "Credit") + "Limit.do",
                    params: rec
                }, this, function(response) {
                    if (response.data && response.data.length > 0) {
                        var msg = (this.businessPerson == "Vendor" ? "<center>" + WtfGlobal.getLocaleText("acc.cust.debitLimit") : "<center>" + WtfGlobal.getLocaleText("acc.cust.creditLimit")) + " " + WtfGlobal.getLocaleText("acc.field.forthis") + this.businessPerson + " " + WtfGlobal.getLocaleText("acc.field.hasreached") + "<center><br>";
                        var limitMsg = "";
                        for (var i = 0; i < response.data.length; i++) {
                            var recTemp = response.data[i];
                            limitMsg = (recTemp.name == "" ? "" : "<b>" + this.businessPerson + ": </b>" + recTemp.name + ", ") + "<b>" + WtfGlobal.getLocaleText("acc.customerList.gridAmountDue") + ": </b>" + WtfGlobal.conventInDecimalWithoutSymbol(recTemp.amountDue) + ", <b>" + (this.businessPerson == "Vendor" ? WtfGlobal.getLocaleText("acc.cust.debitLimit") : WtfGlobal.getLocaleText("acc.cust.creditLimit")) + ": </b>" + recTemp.limit;
                            msg += limitMsg + "<br>";
                        }
                        if (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_custcreditlimit) == '1') {//block
                            msg += "<br><center>" + WtfGlobal.getLocaleText("acc.field.Youcannotproceed") + "<center>";
                            WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.information'), msg], 3);
                            this.enableSaveButtons();
                            return;
                        } else if (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_custcreditlimit) == '2') {//warn
                            msg += "<br><center>" + WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed") + "<center>";
                            Wtf.MessageBox.show({
                                title: WtfGlobal.getLocaleText("acc.common.warning"),
                                msg: msg,
                                width: 500,
                                buttons: Wtf.MessageBox.YESNO,
                                scope: {
                                    scopeObject: this
                                },
                                icon: Wtf.MessageBox.INFO,
                                fn: function(btn) {
                                    if (btn != "yes") {
                                        this.scopeObject.enableSaveButtons();
                                        return;
                                    }
                                    this.scopeObject.showConfirmAndSave(rec, detail, incash);
                                }
                            }, this);
                        } else {//ignore
                            this.showConfirmAndSave(rec, detail, incash);
                        }
                    } else {
                        this.showConfirmAndSave(rec, detail, incash);
                    }
                }, function(response) {
                    this.showConfirmAndSave(rec, detail, incash);
                });
            } else {
                this.showConfirmAndSave(rec, detail, incash);
            }
        } else {
            this.showConfirmAndSave(rec, detail, incash);
        }
    },
    checkiflinkdo: function(rec, detail, incash) {  // Use to check Only for autogernerate DO that Product quantity exceeds limit or not
        var flag = false
        if (this.autoGenerateDO.getValue() == true && WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_negativestock) != 0 && (this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId)) {
            var prodLength = this.Grid.getStore().data.length;
            for (var i = 0; i < prodLength; i++)
            {
                var prodID = this.Grid.getStore().getAt(i).data['productid'];
                var prorec = this.Grid.productComboStore.getAt(this.Grid.productComboStore.find('productid', prodID));
                if (prorec == undefined) {
                    prorec = this.Grid.getStore().getAt(i);
                }
                if (prorec != undefined) {
                    var prodName = prorec.data.productname;
                    var availableQuantity = prorec.data.quantity;
                    var quantity = this.Grid.getStore().getAt(i).data['quantity'];
                    if (availableQuantity < quantity) {
                        if (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_negativestock) == 1) { // Block case
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"), WtfGlobal.getLocaleText("acc.field.QuantitygiveninAutoDoareexceedingthequantityavailable") + '<br>' + WtfGlobal.getLocaleText("acc.nee.54") + ' ' + prodName + " " + WtfGlobal.getLocaleText("acc.field.is") + (availableQuantity) + '<br><br><center>' + WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed") + '</center>'], 2);
                            this.enableSaveButtons();
                            return;
                        } else if (WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_negativestock) == 2) {     // Warn Case
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.QuantitygiveninAutoDoareexceedingthequantityavailable") + '<br>' + WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed") + '</center>', function(btn) {
                                if (btn == "yes") {
                                    this.checkMemo(rec, detail, incash);
                                    return;
                                } else {
                                    this.enableSaveButtons();
                                    return;
                                }
                            }, this);
                            return;
                        }
                    } else {
                        flag = true;
                    }
                }
            }
        } else {
            this.checkMemo(rec, detail, incash);
        }
        if (flag) {
            this.checkMemo(rec, detail, incash);
        }
    },
    saveTemplate: function() {
        if (this.createTransactionAlso) {
            this.transactionType = 1;
            this.save();
            Wtf.getCmp("emailbut" + this.id).show();
            Wtf.getCmp("exportpdf" + this.id).show();
        } else {
            if (this.Name.getValue() == '') {
                var fieldLabel = this.isCustomer ? WtfGlobal.getLocaleText("acc.invoiceList.cust") : WtfGlobal.getLocaleText("acc.invoiceList.ven"); //this.businessPerson+"*",
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Pleaseselect") + fieldLabel], 2);
                this.enableSaveButtons();
                return;
            }
            var southFormValid = true;
            if (this.cash) {
                southFormValid = this.SouthForm.hidden ? true : this.SouthForm.getForm().isValid();
            }
            var isValidCustomFields = this.tagsFieldset.checkMendatoryCombo();
            if (this.NorthForm.getForm().isValid() && isValidCustomFields && southFormValid) {

                var productCountQuantityZero = 0;
                var allProductQtyZeroFlag = true;
                for (var i = 0; i < this.Grid.getStore().getCount() - 1; i++) { // excluding last row
                    var quantity = this.Grid.getStore().getAt(i).data['quantity'];
                    var rate = this.Grid.getStore().getAt(i).data['rate'];
                    if (!this.isExpenseInv && (quantity === "" || quantity == undefined || quantity < 0)) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.QuantityforProduct") + " " + this.Grid.getStore().getAt(i).data['productname'] + " " + WtfGlobal.getLocaleText("acc.field.shouldbegreaterthanZero")], 2);
                        this.enableSaveButtons();
                        return;
                    }
                    if (!this.isExpenseInv && quantity > 0) {
                        allProductQtyZeroFlag = false;
                    } else if (!this.isExpenseInv && quantity == 0) {//For Counting how many rows with zero quantity
                        productCountQuantityZero++;
                    }
                    if (rate === "" || rate == undefined || rate < 0) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.RateforProduct") + " " + this.Grid.getStore().getAt(i).data['productname'] + " " + WtfGlobal.getLocaleText("acc.field.cannotbeempty")], 2);
                        this.enableSaveButtons();
                        return;
                    }
                }
                var count = this.Grid.getStore().getCount();
                if (count <= 1) {//For Normal Grid Empty Check
                    WtfComMsgBox(33, 2);
                    this.enableSaveButtons();
                    return;
                }
                if (allProductQtyZeroFlag) { //for quantity Check in case of mapped products
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ZeroQuantityAllProduct")], 2);
                    this.enableSaveButtons();
                    return;
                }
                if (productCountQuantityZero > 0) {
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.field.ZeroQuantitySomeProduct") + '</center>', function(btn) {
                        if (btn == "yes") {
                            this.createRecordswithTemplate();
                        } else {
                            this.enableSaveButtons();
                            return;
                        }
                    }, this);
                } else {
                    this.createRecordswithTemplate();
                }
            } else {
                WtfComMsgBox(2, 2);
                this.enableSaveButtons();
            }
        }
    },
    createRecordswithTemplate: function() {
        this.transactionType = 2;
        var rec = this.NorthForm.getForm().getValues();
        rec.isselfbilledinvoice = this.isSelfBilledInvoice;
        rec.billdate = WtfGlobal.convertToGenericDate(this.billDate.getValue());
        var detail = this.Grid.getProductDetails();
        var incash = this.cash;
        rec.termid = this.termid;
        this.ajxurl = "";
        if (this.businessPerson == "Customer") {
            this.ajxurl = "ACC" + "Invoice/saveInvoice" + ".do";
        } else if (this.businessPerson == "Vendor") {
            this.ajxurl = "ACC" + "GoodsReceipt/saveGoodsReceipt" + ".do";
        }
        this.showConfirmAndSave(rec, detail, incash);
    },
    fillData: function(store) {
        this.loadingMask.hide();
        var rec = store.getAt(0);
        this.openModuleTab(rec);
        this.ownerCt.remove(this);
    },
    openModuleTab: function(formrec) {
        var templateId = this.templateModelCombo.getValue();
        var copyInv = true;
        var isQuotation = false;
        var isQuotation = formrec.get("isQuotation");
        WtfGlobal.openModuleTab(this, this.isCustomer, isQuotation, this.isOrder, copyInv, templateId, formrec);
    },
    showOneTimeCustomer: function(){
        if((this.moduleid == Wtf.Acc_Invoice_ModuleId) && !(this.isEdit || this.copyInv || this.isCopyFromTemplate || this.isTemplate)){
           return true; 
        }else{
            return false;
        }
    }
});