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

Wtf.account.QuotationPanel=function(config){
    Wtf.apply(this, config);
    //Initialize values to some of the common variables.
    this.initCommonValues(config);//In WtfDocumentMain.js
    
    //Initialize values to some of the module specific variables.
    this.initValues(config);
    Wtf.account.QuotationPanel.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.QuotationPanel, Wtf.account.MainClosablePanel,{
    initComponent:function(config){
        Wtf.account.QuotationPanel.superclass.initComponent.call(this,config);
        
        //Create module specific stores required either for comboboxes, grid, or any other component.
        this.createStores();
        //Add event to module specific stores
        this.addStoreEvents();
        //Update this.POStore reader to fetch custom/dimension fields values in linking case
        this.updateStoreReaderForCustOrDim();//In WtfDocumentMain.js
        
        //Load initial stores
        this.loadInitialStore();
        
        //Create module specific fields with the help of generic methods
        this.createFields();
        //Add event to module specific fields
        this.addFieldEvents();
        
        //Create module specific buttons like Email, Print Record(s), etc
        this.createButtons();
        
        //Create global level custom or dimension fields 
        this.tagsFieldset = this.createCustOrDimFields(this.isViewTemplate, (this.isEdit||this.copyInv));//In WtfDocumentMain.js
        
        //Append form fields as per required sequence in different arrays added in north form
        this.appendNorthFormFields();
        /*Create north form panel. Parameters:
         *1. labelwidth
         *2. leftcolumnwidth, as column layout is used
         *3. rightcolumnwidth, as column layout is used
         **/
        var labelwidth = 170;
        var leftcolumnwidth = 0.55;
        var rightcolumnwidth = 0.45;
        this.createNorthForm(labelwidth, leftcolumnwidth, rightcolumnwidth);//In WtfDocumentMain.js
        //Add event to north form fields
        this.addNorthFormEvents();
        
        //Create product grid to capture product details
        this.createProductGrid();
        //Add event to product grid
        this.addProductGridEvents();
        
        //Create south form
        this.createSouthForm();
        
        //Create module specifc components for south panel
        this.createSouthPanelFields();
        //Create common components for south panel 
        this.createCommonSouthPanelFields();
        //Append fields in array for south panel
        this.appendSouthPanelFields();//In WtfDocumentMain.js
        /*Create south panel by adding components to it. Parameters,
         *1. disabled
         *2. height
         **/
        this.createSouthPanel(this.isViewTemplate, ((Wtf.isIE?210:150) + (this.prodDetailSouthItems.length>2 ? 400 : 50)));//In WtfDocumentMain.js

        //Create recent transaction panel to view recently created transactions.
        this.createRecentTransPanel();      
        
        //Set transaction no.
        this.setTransactionNumber();
        
        //Display message - We are processing your request. Please wait...
        this.displayMsg();

        //Ajax to get invoice creation json
        this.invoiceCreationJSON();
    },
    
    onRender:function(config){
        Wtf.account.QuotationPanel.superclass.onRender.call(this, config);
        
        //Append module specific buttons as per sequence
        this.appendButtons();
        //Add  buttons to center panel bbar after arranging them in sequence
        this.addButtonsTobbar();//In WtfDocumentMain.js
        
//        //Load initial stores
//        this.loadInitialStore();
        
        //Initiallize for close
        this.initForClose();
              
        //Hide form fields
        this.hideFormFields();
    },
    
    initValues: function(config){
        this.quotation = this.isQuotation(config);
        this.ispurchaseReq=config.ispurchaseReq;
        this.GENERATE_PO=config.isPOfromSO;//Need to remove
        this.GENERATE_SO=config.isSOfromPO;//Need to remove
        this.help=getHelpButton(this,config.heplmodeid);
        this.uPermType= this.getUPermType(config);
        this.permType= this.getPermType(config);
        if(this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId){
            this.exportPermType=this.permType.exportvendorquotation;
            this.printPermType=this.permType.printvendorquotation;
        }else if(this.moduleid==Wtf.Acc_Customer_Quotation_ModuleId){
            this.exportPermType=this.permType.exportsalesquotation;
            this.printPermType=this.permType.printsalesquotation;
        }
        this.IsInvoiceTerm = true;// to show terms in all CQ,VQ
        if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_activateProfitMargin)  && this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId){
            this.totalproductsellingprice = 0;
            this.totalproductcost = 0;
            this.totalproductprofitmargin = 0;
            this.totalprodcutprofitmarginpercent = 0;

            this.totalservicesellingprice = 0;
            this.totalservicecost = 0;
            this.totalserviceprofitmargin = 0;
            this.totalserviceprofitmarginpercent = 0;

            this.finalproductsellingprice = 0;
            this.finalproductcost = 0;
            this.finalproductprofitmargin = 0;
            this.finalproductprofitmarginpercent = 0;
        }
        if(this.moduleid==22){
            this.tranType=Wtf.autoNum.Quotation;
        }else if(this.moduleid==23){
            this.tranType=Wtf.autoNum.Venquotation;
        } 
        if(!this.record){
            this.getPostTextToSetPostText();
        }else{
            this.postText=this.record.data.posttext;
        }
        this.singlePDFtext = WtfGlobal.getLocaleText("acc.accPref.autoQN");
        this.nameFieldLabel = this.getNameLabel();
        this.shipDateFieldLabel = WtfGlobal.getLocaleText("acc.field.ShipDate");
        this.shipDateFieldLabelToolTip = WtfGlobal.getLocaleText("acc.field.ShipDate.tip");
    },
    
    isQuotation: function(config){
        if(config.quotation!=null && config.quotation!=undefined){
            return config.quotation;
        }else{
            return false;
        }
    },
    
    getUPermType: function(config){
        if(this.isCustomer){
            return WtfGlobal.getUPermObj(Wtf.UPerm_invoice);
        }else{
            return WtfGlobal.getUPermObj(Wtf.UPerm_vendorinvoice)
        }
    },
    
    getPermType: function(config){
        if(this.isCustomer){
            return WtfGlobal.getPermObj(Wtf.Perm_invoice);
        }else{
            return WtfGlobal.getPermObj(Wtf.Perm_vendorinvoice);
        }
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
    
    createStores: function(){
        this.allAccountRec = new Wtf.data.Record.create([
            {name: 'accid'},
            {name: 'accname'},
            {name: 'groupid'},
            {name: 'groupname'},
            {name: 'level'},
            {name: 'leaf'},
            {name: 'openbalance'},
            {name: 'parentid'},
            {name: 'parentname'}
        ]);
        this.allAccountStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.allAccountRec),
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,  
                ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true
            }
        });
        
        this.perDiscountStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Percentage',true],['Flat',false]]
        });
        
        this.fromPOStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Yes',true],['No',false]]
        });
        
        this.arrfromLink = new Array();
        if(this.isCustomer) {
            
        } else {
            this.arrfromLink.push(['Purchase Requisition','5']);
        }
        this.fromlinkStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value'}],
            data:this.arrfromLink
        });

        this.POStoreUrl = "";
        var closeFlag = true;
        if(this.businessPerson=="Customer"){
            this.POStoreUrl = "ACCPurchaseOrderCMN/getQuotations.do"
        }else if(this.businessPerson=="Vendor"){
            this.POStoreUrl="ACCPurchaseOrderCMN/getRequisitions.do";
        }
        this.POStore = new Wtf.data.Store({
            url:this.POStoreUrl,
            baseParams:{
                mode:(this.isCustBill?52:42),
                closeflag:closeFlag,
                requestModuleid:this.moduleid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.PORec)
        });
        
        this.maintenanceNumberComboRecord = new Wtf.data.Record.create([
            {
                name: 'billid'
            },
            {
                name: 'billno'
            }
        ]);
        this.maintenanceNumberComboStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.maintenanceNumberComboRecord),
            url : "ACCSalesOrderCMN/getMaintenanceRequests.do",
            baseParams:{
            },scope :this
        });
        
        this.templateRec = new Wtf.data.Record.create([
            {name: 'tempid'},
            {name: 'tempname'}
        ]);
        this.templateStore = new Wtf.data.Store({
            url : "ExportPDF/getAllReportTemplate.do",
            method: 'GET',
            baseParams : {
                templatetype : this.doctype
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.templateRec)
        });
    },
    
    addStoreEvents: function(){
        this.maintenanceNumberComboStore.on('beforeload',function(s,o){
            if(!o.params)o.params={};
            var currentBaseParams = this.maintenanceNumberComboStore.baseParams;
            currentBaseParams.id=this.Name.getValue();
            currentBaseParams.soId=this.isEdit && !this.copyInv ?this.record.json.billid:""; // SO Id is sent to java side if this is edit case
            this.maintenanceNumberComboStore.baseParams=currentBaseParams;        
        },this);
        
        this.templateStore.on("load", function() {
            if (!this.isEdit)
                this.template.setValue(Wtf.Acc_Basic_Template_Id);
        }, this);
        
        this.POStore.on('load',this.updateSubtotal,this);
        
        if (Wtf.account.companyAccountPref.enableLinkToSelWin) {
            this.POStore.on('load',function(){addMoreOptions(this.PO,this.PORec)}, this);     
            this.POStore.on('datachanged',function(){addMoreOptions(this.PO,this.PORec)}, this);
        }
    },
    
    createFields: function(){
        this.TermConfig = {
            fieldLabel:(this.isCustomer?"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.invoice.creditTerm.tip")+"'>"+ WtfGlobal.getLocaleText("acc.invoice.creditTerm")+"</span>":"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.invoice.debitTerm.tip")+"'>"+ WtfGlobal.getLocaleText("acc.invoice.debitTerm")+"</span>")+' *',
//            itemCls : (this.cash)?"hidden-from-item1":"",
//            hideLabel:this.cash,
            id:"creditTerm"+this.heplmodeid+this.id,
//            hidden:this.cash,
            hiddenName:'term',
            name:'term',
            allowBlank:false,
            emptyText:(this.isCustomer?WtfGlobal.getLocaleText("acc.inv.ct"):WtfGlobal.getLocaleText("acc.inv.dt")),
            listeners:{
                'select':{
                    fn:this.updateDueDate,
                    scope:this
                }
            }
        };
        this.Term = WtfGlobal.createFnCombobox(this.TermConfig, this.termds, 'termid', 'termname', this);
        if(!WtfGlobal.EnableDisable(WtfGlobal.getUPermObj(Wtf.UPerm_creditterm), WtfGlobal.getPermObj(Wtf.Perm_creditterm_edit))){
            this.Term.addNewFn=this.addCreditTerm.createDelegate(this);
        }
      
//        var isShowOneTime=(this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId) && !((this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv !=undefined ?this.copyInv:false) || (this.isCopyFromTemplate !=undefined ?this.isCopyFromTemplate:false) || (this.isTemplate !=undefined ?this.isTemplate:false));      
        var isShowOneTime=this.isHideShowOnlyOneTime();
        this.ShowOnlyOneTime= new Wtf.form.Checkbox({
            name:'ShowOnlyOneTime',
            hiddenName:'ShowOnlyOneTime',
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.cust.ShowOnlyOneTime.tt") +"'>"+ WtfGlobal.getLocaleText("acc.cust.ShowOnlyOneTime")  +"</span>", 
            id:'ShowOnlyOneTime'+this.heplmodeid+this.id,
            checked:false,
            hideLabel:!isShowOneTime, // show only in new case
            hidden:!isShowOneTime,
            cls : 'custcheckbox',
            width: 10
        });
        
        this.CustomerPORefNoConfig = {
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.CustomerPOrefNo"),  //Customer PO Reference No.',
            name: 'customerporefno',
            hiddenName: 'customerporefno',
            id:"customerporefno"+this.heplmodeid+this.id,
            hidden: !(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_activateProfitMargin) && (this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)) ,
            hideLabel:!(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_activateProfitMargin) && (this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)) ,
            labelWidth:160
        };
        this.CustomerPORefNo = WtfGlobal.createTextfield(this.CustomerPORefNoConfig, false, true, 2048, this);
        
        this.fromPOConfig = {
            hideLabel: (this.isOrder && this.isCustBill) || this.isTemplate || (this.isViewTemplate && !this.readOnly),
            hidden:(this.isOrder && this.isCustBill) || this.isTemplate || (this.isViewTemplate && !this.readOnly),
            mode: 'local',
            disabled:this.isEdit || this.copyInv?false:true,
            id: "linkToOrder"+this.heplmodeid +this.id,
//            fieldLabel:((!this.isCustBill && !this.isOrder)?WtfGlobal.getLocaleText("acc.field.Link"):(this.isOrder && this.isCustomer)? (this.quotation ? WtfGlobal.getLocaleText("acc.field.LinktoVendorQuotation") : WtfGlobal.getLocaleText("acc.field.Link")) :(this.isOrder && !this.isCustomer)?WtfGlobal.getLocaleText("acc.field.Link"): (this.isCustomer?WtfGlobal.getLocaleText("acc.invoice.linkToSO"):WtfGlobal.getLocaleText("acc.invoice.linkToPO"))) ,  //"Link to "+(this.isCustomer?"Sales":"Purchase")+" Order",
            fieldLabel:this.getfromPOLabel(),
            allowBlank:this.isOrder,
            value:false,
            width:50,
            name:'prdiscount',
            hiddenName:'prdiscount',
            listeners:{
                'select':{
                    fn:this.enablePO,
                    scope:this
                }
            }
        };
        this.fromPO = WtfGlobal.createCombobox(this.fromPOConfig, this.fromPOStore, 'value', 'name', this);
        
        this.maintenanceNumberComboConfig = {
            fieldLabel:WtfGlobal.getLocaleText("acc.maintenance.number"),
            id:"maintenanceNumberCombo"+this.heplmodeid+this.id,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.maintenance.number.select"),
            mode: 'local',
            typeAhead: true,
            addNoneRecord: true,
            triggerAction:'all',
            name: 'maintenanceNumberCombo',
            hiddenName: 'maintenanceNumberCombo',
            listeners:{
                'select':{
                    fn:function(){
                        if(this.maintenanceNumberCombo.getValue() != ""){
                            this.Grid.productComboStore.load({
                                params:{
                                    type:Wtf.producttype.service
                                }
                            });
                        }else{
                            this.Grid.productComboStore.load();
                        }
                    },
                    scope:this            
                }
            }
        };
        this.maintenanceNumberCombo = WtfGlobal.createFnCombobox(this.maintenanceNumberComboConfig, this.maintenanceNumberComboStore, 'billid', 'billno', this);

        this.templateConfig = {
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.grid.header.template")+"*",
            hiddenName:"template",
            name:"template",
            emptyText:WtfGlobal.getLocaleText("acc.invoice.grid.template.emptyText"),
            mode: 'local',
            typeAhead: true,
            allowBlank:true, 
            hidden:true, 
            hideLabel:true,
            triggerAction:'all',
            listeners:{
                'change':{
                    fn:this.setTemplateID,
                    scope:this
                }
            }
        };
        this.template = WtfGlobal.createFnCombobox(this.templateConfig, this.templateStore, 'tempid', 'tempname', this);
        this.templateID=new Wtf.form.Hidden({
            scope:this,
            value: this.isEdit ? this.record.data.templateid : ''
        });
        this.template.addNewFn=this.addInvoiceTemplate.createDelegate(this,[this.templateStore],true);
        
        this.validTillDateConfig = {
            fieldLabel : "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.common.validTill.tt") +"'>"+ WtfGlobal.getLocaleText("acc.common.validTill") +"</span>",
            name : 'validdate',
            hiddenName : 'validdate',
            id : "validdate"+this.heplmodeid+this.id
        };
        this.validTillDate = WtfGlobal.createDatefield(this.validTillDateConfig, true, this);
        
        var emptyText = WtfGlobal.getLocaleText("acc.field.SelectVQ/SO");
        if(!this.isCustBill){
            if(this.isOrder && !this.isCustomer) {
                emptyText = WtfGlobal.getLocaleText("acc.field.SelectPR");
            } else {
                emptyText = this.getFromLinkComboEmptyText();
            }
        }
        this.fromLinkComboConfig = {
            name:'fromLinkCombo',
            hiddenName:'fromLinkCombo',
            hideLabel:(this.isCustBill || (this.isCustomer) || this.isTemplate || (this.isViewTemplate && !this.readOnly))?true:false,
            hidden:(this.isCustBill || (this.isCustomer) || this.isTemplate || (this.isViewTemplate && !this.readOnly))?true:false,
            mode: 'local',
            id:'fromLinkComboId'+this.heplmodeid+this.id,
            disabled:true,
            emptyText: emptyText,
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Linkto"),
            allowBlank:false,   
            width:130,
            listeners:{
                'select':{
                    fn:this.enableNumber,
                    scope:this
                }
            }
        };
        this.fromLinkCombo = WtfGlobal.createCombobox(this.fromLinkComboConfig, this.fromlinkStore, 'value', 'name', this);
        
        var emptyTextForPOCombo = "Select Transaction";
        if(this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId){
            emptyTextForPOCombo = WtfGlobal.getLocaleText("acc.inv.QOe/MN");
        } else if(this.moduleid == Wtf.Acc_Vendor_Quotation_ModuleId){
            emptyTextForPOCombo = WtfGlobal.getLocaleText("acc.field.SelectPR");
        } 
        this.MSComboconfig = {
            hiddenName:"ordernumber",
            name:"ordernumber",
            store: this.POStore,
            valueField:'billid',
            hideLabel:(this.isOrder && this.isCustBill) || this.isTemplate || (this.isViewTemplate && !this.readOnly),
            hidden:(this.isOrder && this.isCustBill) || this.isTemplate || (this.isViewTemplate && !this.readOnly),
            displayField:'billno',
            disabled:true,
            clearTrigger:this.isEdit ? false : true,
            emptyText:emptyTextForPOCombo,
            mode: 'local',
            typeAhead: true,
            selectOnFocus:true,                            
            allowBlank:false,
            triggerAction:'all',
            scope:this

        };
        this.PO = new Wtf.common.Select(Wtf.applyIf({
             multiSelect:true,
             fieldLabel:WtfGlobal.getLocaleText("acc.field.Number") ,
             id:"poNumberID"+this.heplmodeid+this.id ,
             forceSelection:true,
             width:240
        },this.MSComboconfig));
        if(!WtfGlobal.EnableDisable(this.soUPermType, this.soPermType)){
            this.PO.addNewFn=this.addOrder.createDelegate(this,[false,null,this.businessPerson+"PO"],true)
        }
        
        this.DueDateConfig = {
            fieldLabel: WtfGlobal.getLocaleText("acc.invoice.dueDate"),//'Due Date*',
            name: 'duedate',
            hiddenName: 'duedate',
            id: "duedate"+this.heplmodeid+this.id,
            itemCls : (this.isOrder)?"hidden-from-item":"",
            hideLabel:this.isOrder,
            hidden:this.isOrder
        };
        this.DueDate = WtfGlobal.createDatefield(this.DueDateConfig, ((this.isOrder)?true:false), this);
        
        this.shippingTermConfig = {
            fieldLabel: "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.ShippingTerm.tt") +"'>"+ WtfGlobal.getLocaleText("acc.field.ShippingTerm") +"</span>",
            name: 'shippingterm',
            hiddenName: 'shippingterm',
            id:"shippingterm"+this.heplmodeid+this.id,
            hidden: !(this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId),
            hideLabel: !(this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)
        };
        this.shippingTerm = WtfGlobal.createTextfield(this.shippingTermConfig, false, true, 255, this);
    },
    
    isHideShowOnlyOneTime: function(){
        if(this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId){
            var flag=false;
            if(this.isEdit){
                flag=this.isEdit;
            }
            if(this.copyInv){
                flag=(flag||this.copyInv);
            }else{
                flag=(flag||false);
            }
            if(this.isCopyFromTemplate){
                flag=(flag||this.isCopyFromTemplate);
            }else{
                flag=(flag||false);
            }
            if(this.isTemplate){
                flag=(flag||this.isTemplate);
            }else{
                flag=(flag||false);
            }
            return !flag;
        }else{
            return false;
        }
    },
    
    getfromPOLabel: function(){
        if(this.isCustomer){
            return WtfGlobal.getLocaleText("acc.field.LinktoVendorQuotation");
        }else{
            return WtfGlobal.getLocaleText("acc.field.Link");
        }
    },
    
    getFromLinkComboEmptyText: function(){
        WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_withinvupdate)? (this.isCustomer? WtfGlobal.getLocaleText("acc.field.SelectSO/DO/CQ") : 
                WtfGlobal.getLocaleText("acc.field.SelectPO/GR/VQ")) : (this.isCustomer? WtfGlobal.getLocaleText("acc.field.SelectSO/CQ") : 
                WtfGlobal.getLocaleText("acc.field.SelectPO/VQ"))
        if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_withinvupdate)){
            if(this.isCustomer){
                return WtfGlobal.getLocaleText("acc.field.SelectSO/DO/CQ");
            }else{
                return WtfGlobal.getLocaleText("acc.field.SelectPO/GR/VQ");
            }
        }else{
            if(this.isCustomer){
                return WtfGlobal.getLocaleText("acc.field.SelectSO/CQ");
            }else{
                return WtfGlobal.getLocaleText("acc.field.SelectPO/VQ");
            }
        }
    },
    
    addFieldEvents: function(){
        this.Currency.on('select', this.onCurrencySelect, this);
        
        this.Name.on('select',this.onNameSelect,this);
        this.Name.on('beforeselect', this.onNameBeforeSelect,this);
        
        this.includingGST.on('focus', this.onIncludingGSTfocus, this);
        this.includingGST.on('check', this.onIncludingGSTCheck, this);
        
        this.PO.on("clearval", this.onPOClearVal, this);
        if (Wtf.account.companyAccountPref.enableLinkToSelWin) {
            this.PO.on("select", function () {
                var billid = this.PO.getValue();
                if (billid.indexOf("-1") != -1) {
                    var url = "";
                    if (this.businessPerson == "Customer"){ // loading vendor quotations in customer quotations
                        url = "ACCPurchaseOrderCMN/getQuotations.do"
                    }else if(this.isOrder){
                        if(this.fromLinkCombo.getValue() == 5){
                            url = "ACCPurchaseOrderCMN/getRequisitions.do"; 
                        }else if(this.fromLinkCombo.getValue() == 2){
                            url =this.isCustomer ?(this.isVersion?"ACCSalesOrderCMN/getVersionQuotations.do":"ACCSalesOrderCMN/getQuotations.do" ): "ACCPurchaseOrderCMN/getQuotations.do";
                    
                        }
                    }
            
                    this.PO.collapse();
                    this.PO.clearValue();
                    this.showPONumbersGrid(url);
                }
            }, this);
        }
        
        this.ShowOnlyOneTime.on('check', this.onShowOnlyOneTimeCheck, this);
        
        this.DueDate.on('blur',this.dueDateCheck,this);
        
        this.billDate.on('change',this.onDateChange,this);
        
        this.validTillDate.on('change',this.onValidTillDateChange,this);
    },
    
    createSouthForm: function(){
        this.SouthForm=new Wtf.account.PayMethodPanel({
            region : "center",
            hideMode:'display',
            baseCls:'bodyFormat',
            isReceipt:false,
            isCash:true,
            disabledClass:"newtripcmbss",
            autoHeight:true,
            disabled:this.readOnly,
            hidden:true,
            style:'margin:10px 10px;',
            id:this.id+'southform',
            border:false
        });
    },
    
    createButtons: function(){
        if(!WtfGlobal.EnableDisable(this.uPermType, this.emailPermType) ||this.isOrder){
            this.emailbuttonConfig = {
                text:WtfGlobal.getLocaleText("acc.common.email"),  // "Email",
                tooltip : WtfGlobal.getLocaleText("acc.common.emailTT"),  //"Email",
                id: "emailbut" + this.id,
                hidden : this.isTemplate||this.isViewTemplate,
                scope: this,
                disabled : true,
                handler: function(){this.callEmailWindowFunction(this.response, this.request)},
                iconCls: "accountingbase financialreport"
            };
        }
        
        if (!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)) {
            this.singleRowPrint = new Wtf.exportButton({
                obj: this,
                id: "printSingleRecord" + this.id,
                iconCls: 'pwnd printButtonIcon',
                text: WtfGlobal.getLocaleText("acc.rem.236"),
                tooltip: WtfGlobal.getLocaleText("acc.rem.236.single"), //'Print Single Record Details',
                disabled: this.isViewTemplate ? false : true,
                exportRecord: this.exportRecord,
                hidden:false,
                menuItem: {rowPrint: (this.isSalesCommissionStmt) ? false : true},
                get: this.tranType,
                moduleid: this.moduleid
            });
        }
        
        this.marginButtonConfig = {
            text: WtfGlobal.getLocaleText("acc.field.margin"), // "Margin",
            cls: 'pwnd add',
            id: "margin" + this.id,
            tooltip : WtfGlobal.getLocaleText("acc.field.useMarginOptionToViewMarginOfProducts"),
            style: "padding-left: 15px;",
            scope: this,
            hidden: !(this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId),
            handler: this.getCostAndMarginWindow
        };
    },
    
    appendButtons: function(){
        if(!WtfGlobal.EnableDisable(this.uPermType, this.emailPermType) ||this.isOrder){
            this.buttonArray = WtfGlobal.addComponentAtIndex(this.buttonArray, 2, this.emailbuttonConfig);
        }
        if (!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)) {
            this.buttonArray = WtfGlobal.addComponentAtIndex(this.buttonArray, 4, this.singleRowPrint);
        }
        this.buttonArray.push(this.marginButtonConfig);
        if(!this.readOnly&&!this.copyInv &&!this.isEdit){
            this.buttonArray.push('->');
            this.buttonArray.push(this.help);
        }
    },
    
    appendNorthFormFields: function(){
        //Left Array
        this.leftItemArr = WtfGlobal.addComponentAtIndex(this.leftItemArr, 0, this.ShowOnlyOneTime);
        this.linkSec = {
            layout:'column',
            border:false,
            defaults:{border:false},
            items:[ {
                layout:'form',
                ctCls : "",
                items:this.fromPO
            },{
                layout:'form',
                ctCls : "",
                labelWidth:45,
                bodyStyle:"padding-left:10px;",  //    ERP-12877
                items:this.fromLinkCombo
            }]
        };
        this.leftItemArr = WtfGlobal.addComponentAtIndex(this.leftItemArr, 3, this.linkSec);
        this.leftItemArr = WtfGlobal.addComponentAtIndex(this.leftItemArr, 4, this.PO);
        this.leftItemArr = WtfGlobal.addComponentAtIndex(this.leftItemArr, 8, this.shippingTerm);
        if(this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId){
            this.leftItemArr = WtfGlobal.addComponentAtIndex(this.leftItemArr, 9, this.CustomerPORefNo);
        }
        this.leftItemArr = WtfGlobal.addComponentAtIndex(this.leftItemArr, 10, this.maintenanceNumberCombo);

        //Right Array
        this.rightItemArr = WtfGlobal.addComponentAtIndex(this.rightItemArr, 1, this.Term);
        this.rightItemArr = WtfGlobal.addComponentAtIndex(this.rightItemArr, 2, this.DueDate);
        this.rightItemArr = WtfGlobal.addComponentAtIndex(this.rightItemArr, 8, this.validTillDate);
        this.rightItemArr = WtfGlobal.addComponentAtIndex(this.rightItemArr, 9, this.template);
        this.rightItemArr = WtfGlobal.addComponentAtIndex(this.rightItemArr, 10, this.templateID);
    },
    
    addNorthFormEvents: function(){
        this.NorthForm.on('render',function(){
            this.termds.load({
                params: {               
                    cash_Invoice:false
                }
            });
            this.termds.on("load",function(){
                if(this.maintenanceNumberCombo && !(this.isEdit && !this.copyInv && WtfGlobal.getModuleId(this)==20)){  //Exludes the case of editing the SO
                    WtfGlobal.hideFormElement(this.maintenanceNumberCombo);
                }
                if(this.isTemplate && !this.createTransactionAlso) {
                    WtfGlobal.hideFormElement(this.sequenceFormatCombobox);
                    WtfGlobal.hideFormElement(this.Number);
                }
            },this);
        },this);
    },
    
    createProductGrid: function(){
        this.ProductGrid=new Wtf.account.ProductDetailsGrid({
            height: 300,//region:'center',//Bug Fixed: 14871[SK]
            layout:'fit',
            title: WtfGlobal.getLocaleText("acc.invoice.inventory"),  //'Inventory',
            border:true,
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
            readOnly:this.isViewTemplate ||this.readOnly,
            editTransaction:this.isEdit,
            isOrder:this.isOrder,
            isInvoice:this.isInvoice,
            isQuotation:this.quotation,
            isRequisition:this.isRequisition,
            forceFit:true,
            isCash:false,
            loadMask : true,
            viewGoodReceipt: this.viewGoodReceipt,
            parentObj :this,
            copyInv:this.copyInv,
            disabled:!(this.isEdit ||this.copyInv)?true:false
        });
        
        if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){
            this.ProductGrid.productComboStore.load();
        }
        
        if(this.isCustBill){  //Without Inventory.[PS]

        }else{    //With Inventory[PS]
            this.Grid=new Wtf.account.ProductDetailsGrid({
                height: 300,//region:'center',//Bug Fixed: 14871[SK]
                cls:'gridFormat',
                layout:'fit',
                parentCmpID:this.id,
                moduleid: this.moduleid,
                id:this.id+"editproductdetailsgrid",
                isCash:false,
                viewConfig:{forceFit:false},
                record:this.record,
                isQuotation:this.quotation,
                isQuotationFromPR : this.isQuotationFromPR,
                isCustomer:this.isCustomer,
                currencyid:this.currencyid,
                disabledClass:"newtripcmbss",
                fromPO:this.isOrder,
                fromOrder:true,
                isEdit:this.isEdit,
                isFromGrORDO:this.isFromGrORDO,
                isOrder:this.isOrder,
                isInvoice:this.isInvoice,
                heplmodeid:this.heplmodeid,//ERP-11098 [SJ]
                forceFit:true,
                editTransaction: this.isEdit,
                loadMask : true,
                readOnly:this.readOnly||this.isViewTemplate,
                viewGoodReceipt: this.viewGoodReceipt,
                parentObj :this,
                copyInv:this.copyInv,
                disabled:!(this.isEdit ||this.copyInv)?true:false
            });
        }       
    },
    
    
    addProductGridEvents:function(){
        this.ProductGrid.on("productselect", this.loadTransStore, this);
        this.ProductGrid.on("productdeleted", this.removeTransStore, this);

        if(this.isCustBill){  //Without Inventory.[PS]

        }else{    //With Inventory[PS]
            this.Grid.on("productselect", this.loadTransStore, this);
            this.Grid.on("productdeleted", this.removeTransStore, this);
        }

        this.NorthForm.on('render',this.setDate,this);
        if(this.isViewTemplate){this.setdisabledbutton();}
        
        this.Grid.on('datachanged',this.updateSubtotal,this);
        
        this.Grid.getStore().on('load',function(store, recArr){
            if(!this.isOrder && !this.quotation && this.isCustomer && this.copyInv && !this.isViewTemplate){
                this.confirmMsg = "";
                if(!WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_withinvupdate)){
                    for(var i=0; i<recArr.length; i++){
                        if(recArr[i].data.productid !== undefined){
                            var index=this.ProductGrid.productComboStore.find('productid',recArr[i].data.productid);
                            var prorec=this.ProductGrid.productComboStore.getAt(index);
                            if(recArr[i].data['quantity'] > this.ProductGrid.productComboStore.getAt(index).data['quantity'] && prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part'){
                                this.confirmMsg += WtfGlobal.getLocaleText("acc.field.MaximumavailableQuantityforProduct")+this.ProductGrid.productComboStore.getAt(index).data['productname']+WtfGlobal.getLocaleText("acc.field.is")+this.ProductGrid.productComboStore.getAt(index).data['quantity']+".<br>";
                                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"),this.confirmMsg+WtfGlobal.getLocaleText("acc.ven.msg4"),function(btn){
                                    if(btn=="yes") {

                                    }else{
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
        }.createDelegate(this),this);
        
        this.Grid.getStore().on('update',function(store,record,opr){
            if(!this.isCustBill){
                var index=this.Grid.productComboStore.findBy(function(rec){
                    if(rec.data.productid==record.data.productid)
                        return true;
                    else
                        return false;
                });
                var prorec=this.Grid.productComboStore.getAt(index);
                if(prorec!=undefined&&prorec!=-1&&prorec!=""){
                    var availableQuantityInBaseUOM = prorec.data['quantity'];
                    var isBlockLooseSell = prorec.data['blockLooseSell'];
                    var availableQuantityInSelectedUOM = availableQuantityInBaseUOM;
                    var pocountinselecteduom = prorec.data['pocount'];
                    var socountinselecteduom = prorec.data['socount'];
                    if(isBlockLooseSell && record.get('isAnotherUOMSelected')){//
                        availableQuantityInSelectedUOM = record.get('availableQtyInSelectedUOM');
                        pocountinselecteduom = record.get('pocountinselecteduom');
                        socountinselecteduom = record.get('socountinselecteduom');
                    }
                    var selectedUOMName = '';
                    if(isBlockLooseSell){
                        selectedUOMName = record.get('uomname');
                    }
                    if(selectedUOMName == undefined || selectedUOMName == null || selectedUOMName == ''){
                        selectedUOMName = prorec.data['uomname'];
                    }
                    this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{
                        productid:prorec.data['productid'],
                        productname:prorec.data['productname'],
                        qty:parseFloat(getRoundofValue(availableQuantityInSelectedUOM)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"  "+selectedUOMName,
                        soqty:parseFloat(getRoundofValue(socountinselecteduom)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"  "+selectedUOMName,
                        poqty:parseFloat(getRoundofValue(pocountinselecteduom)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"  "+selectedUOMName
                        });
                }
            }                            
        },this);
        
        if(!this.isCustBill&&!this.isCustomer&&!this.isOrder&&!this.isEdit&&!this.copyInv){
           this.ProductGrid.on('pricestoreload',function(arr){//alert("1111"+arr.length)
//               if(!this.isExpenseInv){
                    this.datechange=1;
                    this.changeCurrencyStore(arr);
//               }
           },this);//.createDelegate(this)
       }else if(!this.isCustBill){//alert("2222"+arr.length)
           this.Grid.on('pricestoreload',function(arr){
                this.datechange=1;
                this.changeCurrencyStore(arr);
            }.createDelegate(this),this);
        }
   
       if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_activateProfitMargin)  && this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId){
           this.Grid.on('vendorselect',this.onVendorSelect,this);
       }
    },
    
    createSouthPanelFields: function(){
        if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_activateProfitMargin)  && this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId){
            this.productProfitMarginTplSummary=new Wtf.XTemplate(
                '<div> &nbsp;</div>',
                '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',            
                '<div><hr class="templineview"></div>',
                '<div>',
                '<table width="100%">'+
                '<tr>'+
                '<td style="width:20%;" text-align=left></td>'+
                '<td style="width:20%;" text-align=right><b>'+WtfGlobal.getLocaleText("acc.field.ProductSellingPrice")+'</b></td>'+
                '<td style="width:20%;" text-align=right><b>'+WtfGlobal.getLocaleText("acc.field.ProductCost")+'</b></td>'+
                '<td style="width:20%;" text-align=right><b>'+WtfGlobal.getLocaleText("acc.field.ProfitMargin")+'</b></td>'+
                '<td style="width:20%;" text-align=right><b>'+WtfGlobal.getLocaleText("acc.field.ProfitMargin(%)")+'</b></td>'+
                '</tr>'+
                '<tr>'+
                '<td style="width:20%;" text-align=left><b>'+WtfGlobal.getLocaleText("acc.field.TotalProfitMargin(ProductOnly)")+': </b></td>'+
                '<td style="width:20%;" text-align=right>{totalproductsellingprice}</td>'+  
                '<td style="width:20%;" text-align=right>{totalproductcost}</td>'+ 
                '<td style="width:20%;" text-align=right>{totalproductprofitmargin}</td>'+ 
                '<td style="width:20%;" text-align=right>{totalprodcutprofitmarginpercent}</td>'+ 
                '</tr>'+
                '<tr>'+
                '<td style="width:20%;" text-align=left><b>'+WtfGlobal.getLocaleText("acc.field.TotalProfitMargin(ServiceProduct)")+': </b></td>'+
                '<td style="width:20%;" text-align=right>{totalservicesellingprice}</td>'+  
                '<td style="width:20%;" text-align=right>{totalservicecost}</td>'+ 
                '<td style="width:20%;" text-align=right>{totalserviceprofitmargin}</td>'+ 
                '<td style="width:20%;" text-align=right>{totalserviceprofitmarginpercent}</td>'+ 
                '</table>'+
                '</div>', 
                '<div><hr class="templineview"></div>',
                '<div>',
                '<table width="100%">'+
                '<tr>'+
                '<td style="width:20%;" text-align=left><b>'+WtfGlobal.getLocaleText("acc.field.TOTALPROFITMARGIN(PRODUCT+SVE)")+': </b></td>'+
                '<td style="width:20%;" text-align=right>{finalproductsellingprice}</td>'+  
                '<td style="width:20%;" text-align=right>{finalproductcost}</td>'+ 
                '<td style="width:20%;" text-align=right>{finalproductprofitmargin}</td>'+ 
                '<td style="width:20%;" text-align=right>{finalproductprofitmarginpercent}</td>'+ 
                '</table>'+
                '</div>',            
                '<div><hr class="templineview"></div>',                        
                '</div>'
            );
        }

        if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_activateProfitMargin)  && this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId){
            this.productProfitMarginTpl=new Wtf.Panel({
                id:'productProfitMarginTpl'+this.id,
                border:false,
                width:'95%',
                baseCls:'tempbackgroundview',
                hidden:!(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_activateProfitMargin)  && this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId),
                html:this.productProfitMarginTplSummary.apply({
                    totalproductsellingprice:WtfGlobal.currencyRenderer(0),
                    totalproductcost:WtfGlobal.currencyRenderer(0),
                    totalproductprofitmargin:WtfGlobal.currencyRenderer(0),
                    totalprodcutprofitmarginpercent:'<div class="currency">NA</div>',

                    totalservicesellingprice:WtfGlobal.currencyRenderer(0),
                    totalservicecost:WtfGlobal.currencyRenderer(0),
                    totalserviceprofitmargin:WtfGlobal.currencyRenderer(0),
                    totalserviceprofitmarginpercent:'<div class="currency">NA</div>',

                    finalproductsellingprice:WtfGlobal.currencyRenderer(0),
                    finalproductcost:WtfGlobal.currencyRenderer(0),
                    finalproductprofitmargin:WtfGlobal.currencyRenderer(0),
                    finalproductprofitmarginpercent:'<div class="currency">NA</div>'

                })
            }); 
        }           
    },
    
    createRecentTransPanel: function(){
        this.toggleBtnPanel = new Wtf.Panel({
            style: 'padding: 10px 10px 0;',
            border : false,
            autoScroll: true,
            hidden:true,
            items : [{
                    xtype: 'button',
                    enableToggle: true,
                    id:"setButton"+this.heplmodeid+this.id,
                    hidden: this.readOnly,
                    disabled:true,
                    cls : 'setlocationwarehousebtn',
                    text: WtfGlobal.getLocaleText("acc.SetWarehouseLocation"),
                    toggleGroup: 'massupdate',
                    handler: this.SetLocationwarehouseWindow.createDelegate(this)
                }]
        });
        this.Grid.on("onselection", function(){
            if(this.Grid.sModel.getCount()>=1 && this.autoGenerateDO.getValue()){
                if(Wtf.getCmp("setButton"+this.heplmodeid+this.id))Wtf.getCmp("setButton"+this.heplmodeid+this.id).enable();
            }else{
                if(Wtf.getCmp("setButton"+this.heplmodeid+this.id))Wtf.getCmp("setButton"+this.heplmodeid+this.id).disable();
            }
        },this);
        var moduletoShow=(this.moduleid == Wtf.Acc_Cash_Sales_ModuleId || this.moduleid == Wtf.Acc_Cash_Purchase_ModuleId  || this.moduleid == Wtf.Acc_Invoice_ModuleId  ||this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId );
        if((Wtf.account.companyAccountPref.isWarehouseCompulsory && Wtf.account.companyAccountPref.isLocationCompulsory) && moduletoShow && !this.readOnly  ){
            this.toggleBtnPanel.show();
        }
        
        var lastTransPanelId = "quotation";
        this.lastTransPanel = this.isCustomer ? getCustInvoiceTabView(false, lastTransPanelId, '', undefined, true) : getVendorInvoiceTabView(false, lastTransPanelId, '', undefined, true) ;
    },
    
    loadRecord:function(){
        if(this.record!=null&&!this.dataLoaded){
            var data=this.record.data;
            this.NorthForm.getForm().loadRecord(this.record);
            
            if(data.termid!="" && data.termid!=null && data.termid!=undefined){
                this.termds.on("load", function(){
                    this.Term.setValue(data.termid);
                }, this);
                this.termds.load();
            }
            
            if(!this.copyInv && !(!this.isCustomer && this.ispurchaseReq)){
                    this.Number.setValue(data.billno);
            }else if(this.copyInv){
                this.Number.setValue("");//copy case assign ""
            }
            this.externalcurrencyrate=this.record.data.externalcurrencyrate;
            
            if(!this.isCustomer && this.ispurchaseReq){ // for showing link number in number field in case of VQ generated from Purchase Requisition
                this.fromPO.setValue(true);
                if (!this.isCustomer&&this.ispurchaseReq){  // IF VQ generated from Purchase Requisition
                    this.fromLinkCombo.setValue(5);   // 5 referes to 'Purchase Requisition'
                }else {
                    this.fromLinkCombo.setValue(0);                
                }
                
                this.POStore.proxy.conn.url ="ACCPurchaseOrderCMN/getRequisitions.do";
                this.POStore.on("load", function(){
                    if(!this.isCustomer && this.ispurchaseReq){
                        if(!(!this.isCustomer&&this.ispurchaseReq)){ // In case of 'Vendor quotation generated from purchase requisition' , this.po and this.fromPO will not be disabled
                            this.PO.disable();
                            this.fromPO.disable();
                        }
                        this.setTransactionNumber();
                        this.PO.setValue(this.PR_IDS);
                    }
                }, this);
                this.POStore.load();
                
            }else{ 
//            // for showing multiple link numbers in number field
                this.Grid.getStore().on("load", this.handleGridStoreLoadEventOnEdit, this);
            }
            if(this.copyInv || this.isEdit){ 
                if(Wtf.getCmp("showaddress" + this.id)){
                    Wtf.getCmp("showaddress" + this.id).enable(); 
                }
                this.isCustomer ? Wtf.salesPersonFilteredByCustomer.load({
                    params:{ //sending a customerid to fliter available masteritems for selected customer 
                        customerid: this.record.data.personid            
                    }
                }) : Wtf.agentStore.load(); 
            }
            if(this.viewGoodReceipt){
                if(Wtf.getCmp("exportpdf" + this.id)){
                    Wtf.getCmp("exportpdf" + this.id).hide(); 
                }
                if(Wtf.getCmp("posttext" + this.id)){
                    Wtf.getCmp("posttext" + this.id).hide();
                }
                if(Wtf.getCmp('south' + this.id)){
                    Wtf.getCmp('south' + this.id).hide();
                }
                if(Wtf.getCmp('productDetailsTpl'+this.id)){
                    Wtf.getCmp('productDetailsTpl'+this.id).hide();
                }
            }
            this.template.setValue(data.templateid);
            this.Currency.setValue(data.currencyid);
            
            if(!this.custVenOptimizedFlag) {
                var store=(this.isCustomer?Wtf.customerAccStore:Wtf.vendorAccStore)
                var index=store.findBy( function(rec){
                    var parentname=rec.data['accid'];
                    if(parentname==data.personid)
                        return true;
                    else
                        return false;
                })
                if(index>=0) {
                    this.Name.setValue(data.personid);
                }
            } else {
                this.Name.setValForRemoteStore(data.personid, data.personname);
            }
            
            this.Memo.setValue(data.memo);
            this.postText = data.posttext;
            this.DueDate.setValue(data.duedate);
            if(this.isOrder && data.isOpeningBalanceTransaction){
//                this.isOpeningBalanceOrder = data.isOpeningBalanceTransaction;
                this.billDate.maxValue=this.getFinancialYRStartDatesMinOne(true);
            }
            this.billDate.setValue(data.date);
            if(this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId){
                this.shippingTerm.setValue(data.shippingterm);
            }
            this.isTaxable.setValue(data.taxincluded);
            this.CustomerPORefNo.setValue(data.customerporefno)
            
            this.editedBy.setValue(data.lasteditedby);
            this.dataLoaded=true;
            if(this.IsInvoiceTerm) {
                this.setTermValues(data.termdetails);
                if(data.termsincludegst!=="" && data.termsincludegst===true) {
                    this.TermsIncludeGST_YES.setValue(true);
                    this.TermsIncludeGST_NO.setValue(false);
                    this.termsincludegst = true;
                } else if(data.termsincludegst!=="" && data.termsincludegst===false){
                    this.TermsIncludeGST_NO.setValue(true);
                    this.TermsIncludeGST_YES.setValue(false);
                    this.termsincludegst = false;
                }
            }
            if(this.isCustomer && this.record.data.partialinv){
                var id=this.Grid.getId();
                var rowindex=this.Grid.getColumnModel().findColumnIndex("partamount");
                this.Grid.getColumnModel().setHidden( rowindex,false) ;
            }
            var gridID=this.Grid.getId();
            var taxColumnIndex=this.Grid.getColumnModel().findColumnIndex("prtaxid");
            var taxAmtColumnIndex=this.Grid.getColumnModel().findColumnIndex("taxamount");
            if(this.Grid){
                this.Grid.forCurrency =data.currencyid;
                this.Grid.affecteduser=data.personid;
                this.Grid.billDate=data.date;
            }
            if(this.record.data.includeprotax){
                this.Grid.getColumnModel().setHidden( taxColumnIndex,false) ;
                this.Grid.getColumnModel().setHidden( taxAmtColumnIndex,false) ;
                this.isTaxable.setValue(false);
                this.isTaxable.disable();
                this.Tax.setValue("");
                this.Tax.disable();
            }else{
                this.Grid.getColumnModel().setHidden( taxColumnIndex,true) ;
                this.Grid.getColumnModel().setHidden( taxAmtColumnIndex,true) ;
                if(!this.isEdit && !this.isCopy){//In edit case no need to reset Transaction Tax. - Amol D.
                    this.isTaxable.reset();
                }
                this.isTaxable.enable();
            }
            this.loadTransStore();
            if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){
                this.ProductGrid.productComboStore.load({
                    params:{
                        mappingProduct:true,
                        customerid:this.Name.getValue(),
                        common:'1', 
                        loadPrice:true,
                        mode:22
                    }
                });           
            }
            this.includingGST.reset();
            this.isViewTemplate==true?this.includingGST.disable():this.includingGST.enable();
            if(this.record.data.gstIncluded!=undefined){
                this.includingGST.setValue(this.record.data.gstIncluded);
            }

            if(data.taxid == ""){
                this.isTaxable.setValue(false);
                this.Tax.setValue("");
                this.Tax.disable();
            }else{
                this.Tax.setValue(data.taxid);
                this.isTaxable.enable();
                this.Tax.enable();//enable the tax when taxid is present-for edit case it was not required but for copy its is required.
                this.isTaxable.setValue(true);
            }
            this.gstCurrencyRate = this.record.data.gstCurrencyRate && this.record.data.gstCurrencyRate!="" ? this.record.data.gstCurrencyRate : 0;
        }       
    },
    
    hideFormFields:function(){
        //Update field configs as per add, edit & view.
        this.updateFieldConfigs();//In WtfDocumentMain.js
        
        if(this.isCustomer){
            this.hideTransactionFormFields(WtfGlobal.getHideFormFieldObj(Wtf.HideFormFieldProperty_customerQuotation));
        }else{
            this.hideTransactionFormFields(WtfGlobal.getHideFormFieldObj(Wtf.HideFormFieldProperty_vendorQuotation));
        }  
    },
    
    setNextNumber:function(config){
        if(this.sequenceFormatStore.getCount()>0){
            if((this.isEdit || this.copyInv) && !this.templateId && !(!this.isCustomer&&this.ispurchaseReq)){ //only edit case & copy
                var index=WtfGlobal.searchRecordIndex(this.sequenceFormatStore,this.record.data.sequenceformatid,"id");
                if(index!=-1){
                    this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid); 

                    if(!this.copyInv){//edit
                        this.sequenceFormatCombobox.disable();
                        this.Number.disable(); 
                    }else{//copy case if sequenceformat id hide number
                        this.Number.disable();
                        WtfGlobal.hideFormElement(this.Number);
                    }
                } else {
                    this.sequenceFormatCombobox.setValue("NA"); 
                    this.sequenceFormatCombobox.disable();
                    if(!this.isViewTemplate){            // View mode- all fields should be disabled unconditionaly
                        this.Number.enable();  
                    }    
                    if(this.copyInv){//copy case show number field 
                        this.getNextSequenceNumber(this.sequenceFormatCombobox);
                    }
                }
            }else if(this.templateId || !this.isEdit || (!this.isCustomer&&this.ispurchaseReq)){// create new,generate so and po case and 
                var seqRec=this.sequenceFormatStore.getAt(0)
                this.sequenceFormatCombobox.setValue(seqRec.data.id);
                var count=this.sequenceFormatStore.getCount();
                for(var i=0;i<count;i++){
                    var seqRec=this.sequenceFormatStore.getAt(i)
                    if(seqRec.json.isdefaultformat=="Yes"){
                        this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                        break;
                    }
                }

                this.getNextSequenceNumber(this.sequenceFormatCombobox); 
            }
        }
    },
    
    setdisabledbutton:function(){
        this.ShowOnlyOneTime.enable();
        this.Currency.setDisabled(true);
        this.PO.setDisabled(true);
        this.sequenceFormatCombobox.setDisabled(true);
        this.Number.setDisabled(true);
        this.billDate.setDisabled(true);
        this.CustomerPORefNo.setDisabled(true);
        this.shipDate.setDisabled(true);
        this.Term.setDisabled(true);
        this.DueDate.setDisabled(true);
        this.shipvia.setDisabled(true);
        this.fob.setDisabled(true);
        this.includeProTax.setDisabled(true);
        this.validTillDate.setDisabled(true);
        this.template.setDisabled(true);
        this.templateID.setDisabled(true);
        this.users.setDisabled(true);
        this.Name.setDisabled(true);
        this.fromLinkCombo.setDisabled(true);    
        this.fromPO.setDisabled(true); 
    },
    
    onNameSelect:function(combo,rec,index){
        this.singleLink = false;
        if (this.isEdit || this.isCopy) {
            this.isVenOrCustSelect = true;
        }
        if(combo.getValue()==this.nameBeforeSelect){ //If same name selected no need to do any action 
            return;
        }
        if(this.isEdit || this.copyInv){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), this.getMsgOnNameSelect(), function(btn) {
                if(btn=="yes"){
                    if(this.GENERATE_PO || this.GENERATE_SO){
                        this.currencySelect(combo,rec,index);
                   }
                   this.doOnNameSelect(combo,rec,index); 
                } else{         
                    this.Name.setValue(combo.startValue);
                    return false;
                }
            },this);           
        } else {
            if (Wtf.productDetailsGridIsEmpty(this.Grid)) {
                this.Currency.setValue(rec.data['currencyid']);    //refer ticket ERP-14001
            }
            this.doOnNameSelect(combo,rec,index);
        }   
    },
    
    getMsgOnNameSelect: function(){
        var changedcurrency = (this.currencychanged? " " + WtfGlobal.getLocaleText("acc.invoice.alertOnVendorOrCustomerChangeIfCurrencyChanged") : "");
        if(this.isCustomer){
            return (WtfGlobal.getLocaleText("acc.invoice.alertoncustomerchange") + changedcurrency + " " + WtfGlobal.getLocaleText("acc.field.doYouWantToProceed"));
        }else{
            return (WtfGlobal.getLocaleText("acc.invoice.alertonvendorchange") + changedcurrency + " " +WtfGlobal.getLocaleText("acc.field.doYouWantToProceed"));
        }
    },
    
    currencySelect:function(combo,rec,index){
        var newCurrencyid = rec.data.currencyid;
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),this.isCustomer?WtfGlobal.getLocaleText("acc.invoice.alertoncustomerchangeforcurrency"):WtfGlobal.getLocaleText("acc.invoice.alertonvendorchangeforcurrency"),function(btn){
            if(btn=="yes" && this.Currency.getValue()!=newCurrencyid){
                this.externalcurrencyrate=0; 
                this.currencychanged = true;
                this.Currency.setValue(newCurrencyid);
                this.updateFormCurrency();
            } else{         
                return false; 
            }
        },this);
    },
    
    doOnNameSelect:function(combo,rec,index){
        var customer= this.Name.getValue();
        if(this.isCustomer){
            Wtf.salesPersonFilteredByCustomer.load({
                params:{ //sending a customerid to fliter available masteritems for selected customer 
                    customerid:customer            
                }
            });
        }
        if(this.ispurchaseReq){
            this.Name.setValue(customer);
        }else{
            if(this.isEdit || this.copyInv){  //edit case when user retain exchange rate setting is true
                this.loadStoreOnNameSelect();
            }else {
                this.loadStore();   
            }                                
            this.Name.setValue(customer);
        }
        this.setTerm(combo,rec,index);
        if (this.isCustomer) {              //refer ticket ERP-14000
            this.setSalesPerson(combo, rec, index);
        }
        this.updateData(); 
        this.autoPopulateProducts(); 
        this.tagsFieldset.resetCustomComponents();
        var moduleid = this.isCustomer ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId;
        this.tagsFieldset.setValuesForCustomer(moduleid, customer);
        this.currentAddressDetailrec="";//If customer/vendor change in this case,previously stored addresses in this.currentAddressDetailrec will be clear    
        this.Grid.setDisabled(false);
    },
    
    autoPopulateProducts:function(){
        if(!this.isExpenseInv) {
            if(!this.isEdit && !this.copyInv && this.autoPopulateMappedProduct  ){  // in edit and copy case dont autopopulate mapped product untill user change the product manually 
                if(this.moduleid==Wtf.Acc_Customer_Quotation_ModuleId||this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId){
                    this.Grid.ProductMappedStore.on('beforeload',function(s,o){
                        if(!o.params)o.params={};
                        var currentBaseParams = this.Grid.ProductMappedStore.baseParams;
                        currentBaseParams.mappedProductRequest=true;
                        currentBaseParams.startdate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true));
                        currentBaseParams.enddate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false));  
                        currentBaseParams.moduleid = this.moduleid;         // Passing Moduleid
                        this.Grid.ProductMappedStore.baseParams=currentBaseParams;        
                    },this);  
                    this.Grid.ProductMappedStore.load({
                        params:{
                            mappingProduct:true,
                            affecteduser:this.Name.getValue(),
                            common:'1', 
                            loadPrice:true,
                            mode:22
                        }
                    })
                    this.Grid.ProductMappedStore.on("load",function(){
                        this.Grid.affecteduser=this.Name.getValue();
                        this.Grid.loadMappedProduct(this.Grid.ProductMappedStore);
                    },this);                           
                }                         
            }else{//Normal Case
                if(this.isOrder ||this.isInvoice){
                    if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){  
                        this.Grid.productComboStore.load({
                            params:{
                                mappingProduct:true,
                                customerid:this.Name.getValue(),
                                common:'1', 
                                loadPrice:true,
                                mode:22
                            }
                        })
                    } 
                }
            } 
        }
    },
    
    addSalesPerson:function(){
        this.isCustomer ? addMasterItemWindow('15') : addMasterItemWindow('20');
    },
    
    getPostTextToSetPostText:function(){
     Wtf.Ajax.requestEx({
            url: "ACCCommon/getPDFTemplateRow.do",
            params:{
                module:this.moduleid
            }
        }, this, function(response) {
            if (response.success) {
                this.postText=response.posttext;
            }
        });
    },
    
    successCallback:function(response){
        if(response.success){
            if(!this.isCustBill && !this.isCustomer && !this.isOrder && !this.isEdit && !this.copyInv && this.isQuotation){
                this.ProductGrid.taxStore.loadData(response.taxdata);
                if(this.ExpenseGrid){
                    this.ExpenseGrid.taxStore.loadData(response.taxdata);
                }
            } else {
                this.Grid.taxStore.loadData(response.taxdata);
            }
            this.termds.loadData(response.termdata);
            this.currencyStore.loadData(response.currencydata);
            if(this.Currency.getValue() !="" && WtfGlobal.searchRecord(this.currencyStore,this.Currency.getValue(),"currencyid") == null){
                if(this.currencyStore.getCount()<=1){
                    callCurrencyExchangeWindow();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.msg1")],2);
                }
            } else {
                this.isCurrencyLoad=true;
            }
            if(this.isEdit && this.record!=null) {
                if(this.record.data.taxid!=undefined && this.record.data.taxid!=null && this.record.data.taxid!="" ){
                    this.isTaxable.enable();
                    this.isTaxable.setValue(true);
                    this.Tax.setValue(this.record.data.taxid);
                }else {
                    this.isTaxable.setValue(false);
                    this.Tax.setValue("");
                }
            }
            if(this.isEdit || this.copyInv){
                if(this.record.data.termid=="" || this.record.data.termid==null || this.record.data.termid==undefined){
                    this.getTerm();
                }
            }
            if(this.isEdit || this.copyInv){
                this.loadRecord();            
            }
            this.hideLoading();
            if(this.isExpenseInv){
                if(this.Grid.accountStore.getCount()<=1){
                    this.Grid.accountStore.on("load",function(){
                        this.loadDetailsGrid();
                    },this);
                }else{
                    this.loadDetailsGrid();
                }

            }else{
                if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){
                    this.Grid.productComboStore.on("load",function(){
                        if(!this.saveOnlyFlag){ //no need to load editablegrid after click on save button
                            this.loadDetailsGrid();
                        }
                    },this);
                }
                if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag!= Wtf.Show_all_Products ){
                    this.loadDetailsGrid();
                }
                var loadDetailsGrid=false;
                if(this.isCustomer){
                    if(Wtf.StoreMgr.containsKey("productstoresales")){
                        loadDetailsGrid=true;
                    }
                }else{
                    if(Wtf.StoreMgr.containsKey("productstore")){
                        loadDetailsGrid=true;
                    }
                }
                if(loadDetailsGrid){
                    this.loadDetailsGrid();
                }
            } 

        }
    },

    loadDetailsGrid:function(){
        this.loadEditableGridForQuotation();
        if(this.isEdit && this.isOrder && !this.isCustomer && (BCHLCompanyId.indexOf(companyid) != -1)){
            this.loadOtherOrderdetails();
        }
    },
    
    failureCallback:function(response){
         this.hideLoading();
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Failtoloadtherecords")+" "+response.msg], 2);
    },
    
    hideLoading:function(){Wtf.MessageBox.hide();},
    
    loadEditableGridForQuotation:function(){
        if (!this.isCustomer) {
            this.subGridStoreUrl = "ACCPurchaseOrderCMN/getQuotationRows.do";
        }else{
            this.subGridStoreUrl = this.isVersion ?"ACCSalesOrderCMN/getQuotationVersionRows.do": "ACCSalesOrderCMN/getQuotationRows.do";
        }
        if(!this.isCustomer && this.PR_IDS) {
            this.Grid.getStore().proxy.conn.url = "ACCPurchaseOrderCMN/getRequisitionRows.do";
            this.Grid.getStore().load({params:{bills:this.PR_IDS}});
            
            // reset to original config
            this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;  
            this.Grid.getStore().params = {bills:this.billid}
        } else {
            if(this.record != undefined)
            this.billid=this.record.data.billid;
            if(this.billid!=null){
                this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;  
                this.Grid.getStore().load({params:{bills:this.billid,copyInvoice:this.copyInv}});
            }
       }
    },
    
    loadOtherOrderdetails: function() {
        Wtf.Ajax.requestEx({
            url: "ACCPurchaseOrderCMN/getPurchaseOrderOtherDetails.do",
            params: {
                poid: this.record.data.billid
            }
        }, this, function(response) {
            if (response.data && response.data.length > 0) {

            }
        }, function(response) {
            
        });
    },

    addOrder:function(){
        var tabid = "ordertab";
        if(this.isCustomer){
            if(this.quotation){
                tabid = 'vendorquotation';
                callVendorQuotation(false, tabid);
            } else if(this.isOrder){
                tabid = 'quotation';
                callQuotation(false, tabid);
            }
        }else{
            if(this.isOrder){
                if(this.fromLinkCombo.getValue() == 2){
                    tabid = 'vendorquotation';
                    callVendorQuotation(false, tabid);
                }
            }            
        }
        if(Wtf.getCmp(tabid)!=undefined) {
            Wtf.getCmp(tabid).on('update',function(){
                this.POStore.reload();
            },this);
        }
    },
    
    enableNumber:function(c,rec){
        this.PO.clearValue();
        this.fromLinkCombo.enable();
        this.fromPO.setValue(true);
        if(this.Grid){
            this.Grid.isFromGrORDO=false;
        }
        if (rec.data['value']==5){  // For Linking Purchase Requisition in VQ
            this.PO.multiSelect=true;
            this.isMultiSelectFlag=true;
            this.PO.removeListener("select",this.populateData,this);
            this.PO.addListener("blur",this.populateData,this);                
            this.fromLinkCombo.setValue(5);
            this.POStore.proxy.conn.url = "ACCPurchaseOrderCMN/getRequisitions.do";
            this.POStore.load({
                params: {
                    currencyfilterfortrans:this.Currency.getValue(),
                    nondeleted : true,
                    onlyApprovedRecords :true,
                    prvqlinkflag:true
                }
            });
            this.PO.enable();
            this.fromPO.enable();
        }
    },
    
    enablePO:function(c,rec){
        this.singleLink = false;
        if(rec.data['value']==true){
            if(!this.isCustBill&&!this.isCustomer&&!this.isEdit&&!this.copyInv&&!(this.isOrder&&(!this.isCustomer))){//this.isExpenseInv=false;
                this.GridPanel.setActiveTab(this.ProductGrid);
                this.ExpenseGrid.disable();
            }
            if(!(this.isCustBill || (this.isOrder&&this.isCustomer))){
                this.fromLinkCombo.enable();
            }else{
                if(!this.isCustBill && this.isOrder && this.isCustomer){   //loading vendor Quotations in Customer Quotations
                    this.POStore.load({params:{validflag:true,isVQLinkInCQ:true,currencyid:this.Currency.getValue(),billdate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
                }
                this.PO.enable();
            }                                                      
            this.fromOrder=true;
            this.PO.multiSelect=false;
            this.isMultiSelectFlag=false;
            this.PO.removeListener("blur",this.populateData,this);
            this.PO.addListener("select",this.populateData,this);
        }else{
            if(!this.isCustBill && !this.isOrder && this.isCustomer){
                this.fromLinkCombo.disable();
                this.PO.disable();
            }    
            this.loadStore();    
            this.setDate(); 
            var id=this.Grid.getId();
            var rowindex=this.Grid.getColumnModel().findColumnIndex("partamount");
            if(rowindex>=0){
                this.Grid.getColumnModel().setHidden( rowindex,true) ;
            }
        }
        this.currencyStore.load(); 	       // Currency id issue 20018
    },
    
    populateData:function(c,rec) {
        this.singleLink = false;
        if(this.PO.getValue()!=""){
            var billid=this.PO.getValue();
            this.clearComponentValues();
            this.Grid.fromPO=true;  
            if(this.isMultiSelectFlag){ //For MultiSelection 
                var selectedids=this.PO.getValue();
                var selectedValuesArr = selectedids.split(',');
                var crosslink=false;
                if(selectedValuesArr.length==1){  // Load value of Include product tax according to PO
                    rec=this.POStore.getAt(this.POStore.find('billid',selectedValuesArr[0]));
                    //we can not fetch address when crosslined and when linking Purchase Req in vendor quotation. because Purchase requisition does not have any address.
                    if (!crosslink && WtfGlobal.getModuleId(this)!=Wtf.Acc_Vendor_Quotation_ModuleId) {
                        this.linkRecord = this.POStore.getAt(this.POStore.find('billid', selectedValuesArr[0]));
                        this.singleLink = true;
                    }
                    if(rec.data["includeprotax"]){
                        this.includeProTax.setValue(true);
                        this.showGridTax(null,null,false);
                        this.isTaxable.setValue(false);//when selecting record with product tax.Tax should get disabled.
                        this.isTaxable.disable();
                        this.Tax.setValue("");
                        this.Tax.disable();
                        if(crosslink) {           //ERP-13836
                            WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.alert'),WtfGlobal.getLocaleText('acc.invoiceform.linkTax')],2);
                        }
                    }else{
                        this.includeProTax.setValue(false);
                        this.showGridTax(null,null,true);
                        this.Tax.enable();//required because when selected multiple records & changing to select single record.Before it was getting disabled.
                        this.isTaxable.enable();
                    }
                    if (this.fromLinkCombo.getValue() !== 2 && ((!this.isCustomer && crosslink) ? this.fromLinkCombo.getValue() == 0 : this.fromLinkCombo.getValue() !== 0)) {
                        if (rec.data["taxid"] != "" && rec.data["taxid"] != undefined && rec.data["taxid"] != "None") { //checks for invoice level tax 
                            this.Tax.setValue(rec.data.taxid);
                            this.isTaxable.enable();
                            this.Tax.enable();//enable the tax when taxid is present-for edit case it was not required but for copy its is required.
                            this.isTaxable.setValue(true);
                        }
                    }
                    if(rec.data["gstIncluded"]&&!this.includingGST.getValue()){
                        this.includingGST.setValue(true);
                    }else if(!rec.data["gstIncluded"]&&this.includingGST.getValue()){
                        this.includingGST.setValue(false);
                    }

                    if(rec.data["customerporefno"]){//    ERP-9886
                        this.CustomerPORefNo.setValue(rec.data["customerporefno"]);
                    }else {
                        this.CustomerPORefNo.setValue("");
                    }

                    if(this.IsInvoiceTerm) {
                        this.setTermValues(rec.data.termdetails);
                    }
                    var linkedRecordExternalCurrencyRate=rec.data["externalcurrencyrate"];
                    if(this.Currency.getValue()!=WtfGlobal.getCurrencyID && linkedRecordExternalCurrencyRate!="" && linkedRecordExternalCurrencyRate!=undefined){ //If selected currency is foreign currency then currency exchange rate will be exchange rate of linked document 
                        this.externalcurrencyrate=linkedRecordExternalCurrencyRate;
                    }
                }else if(selectedValuesArr.length>1){
                    var productLevelTax=false;  
                    var isGSTTax=false;
                    var isInvoiceLevelTax=false;
                    var withoutTax=false;
                    this.previusTaxId="";
                    var isInvoiceTaxDiff=false;
                    var invoiceLevelTaxRecords=0;
                    var reccustomerporefno='';
                    for(var cnt=0;cnt<selectedValuesArr.length;cnt++){
                        rec=this.POStore.getAt(this.POStore.find('billid',selectedValuesArr[cnt]));
                        if(rec.data.contract!=undefined && rec.data.contract!=""){ // in case of multiple linking if linked transactions are containing different different contract ids or similar contract ids then we will not allow linking
                            var dataMsg = "";
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),dataMsg], 2);
                            this.PO.clearValue();
                            return; 
                        }
                        if(rec.data["gstIncluded"]){ //checks for GST Tax
                            isGSTTax=true;
                        }else if(rec.data["includeprotax"]){ //checks for product level tax
                            productLevelTax=true;
                        }else if(rec.data["taxid"]!="" && rec.data["taxid"]!=undefined){ //checks for invoice level tax 
                            isInvoiceLevelTax=true;                        
                            if(invoiceLevelTaxRecords!=0 && this.previusTaxId!=rec.data["taxid"]){
                                isInvoiceTaxDiff=true;
                            }
                            this.previusTaxId=rec.data["taxid"];
                            this.includeProTax.setValue(false);
                            this.showGridTax(null,null,true);//updating include product tax
                            invoiceLevelTaxRecords++;
                        }else{
                            withoutTax=true;//applicable for both no tax and diff tax
                        }
                        if(rec.data["customerporefno"]){   //    ERP-9886
                            if(reccustomerporefno!="")
                            reccustomerporefno+=','+rec.data["customerporefno"];
                            else
                            reccustomerporefno+=rec.data["customerporefno"]   
                        }else {
                            reccustomerporefno+='';
                        }
                    }
                    this.CustomerPORefNo.setValue(reccustomerporefno);
                    if(isGSTTax){ //case when any linked record have GST Tax
                        var includeGstCount=0;
                        var excludeGstCount=0;
                        for(var cntGst=0;cntGst<selectedValuesArr.length;cntGst++){
                            rec=this.POStore.getAt(this.POStore.find('billid',selectedValuesArr[cntGst]));
                            if(rec.data["gstIncluded"]){
                                includeGstCount++;
                            }else if(!rec.data["gstIncluded"]){
                                excludeGstCount++;
                            }
                        }

                        if(!((selectedValuesArr.length==includeGstCount)||(selectedValuesArr.length==excludeGstCount))){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.alert.includingGST")], 2);
                            this.PO.clearValue();
                            return;
                        }else{
                            if(selectedValuesArr.length==includeGstCount){
                                this.includeProTax.setValue(true);
                                this.includingGST.setValue(true);
                            }else if(selectedValuesArr.length==excludeGstCount){
                                this.includeProTax.setValue(false);
                                this.includingGST.setValue(false);
                            }
                        }
                    }else if(productLevelTax){//case when any linked record have product tax without GST Tax
                        if(isInvoiceLevelTax){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.alert.includingProductTax")], 2);
                            this.PO.clearValue();
                            return;
                        }else{//no tax and producttax
                            this.includeProTax.setValue(true);
                            this.showGridTax(null,null,false); 
                            this.isTaxable.setValue(false);//when selcting record with product tax.Tax should get disabled.
                            this.isTaxable.disable();
                            this.Tax.setValue("");
                            this.Tax.disable();
                        }                   
                    }else if(isInvoiceLevelTax && !crosslink){
                        if(withoutTax || isInvoiceTaxDiff){//for different tax and empty tax
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.alert.includingDifferentTax")], 2); 
                            this.PO.clearValue();
                            return;
                        }else{
                            if(this.fromLinkCombo.getValue()!==2 && ((!this.isCustomer && crosslink)?this.fromLinkCombo.getValue()==0:this.fromLinkCombo.getValue()!==0)){
                                WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.alert'),WtfGlobal.getLocaleText('acc.invoiceform.linkTax')],2);  
                            }
                            this.Tax.enable();
                            this.isTaxable.enable();
                            this.isTaxable.setValue(true);
                            this.Tax.setValue(this.previusTaxId);                            
                        }
                        this.includeProTax.setValue(false); //update include product tax
                        this.showGridTax(null,null,true);
                    }else {//for goodsreceiptorder and deliveryorder
                        this.Tax.disable();
                        this.isTaxable.enable();
                        this.isTaxable.setValue(false);
                        this.Tax.setValue("");
                        this.includeProTax.setValue(false); //update include product tax
                        this.showGridTax(null,null,true);
                    }

                    var isLinkedDocumentHaveSameER=true;           
                    var linkedExternalRate=0;
                    if(this.Currency.getValue()!=WtfGlobal.getCurrencyID){ // Foreign currency linking case. In this case we have to borrow Linked document Exchange Rate in current document.                  
                        for(var count=0;count<selectedValuesArr.length;count++){
                            var tempRec =WtfGlobal.searchRecord(this.POStore,selectedValuesArr[count],"billid");                        
                            if(count==0){
                                linkedExternalRate = tempRec.data["externalcurrencyrate"]; // taking externalcurrencyrate of first record and then comparing it with other records external currency rate
                            } else if(tempRec.data["externalcurrencyrate"]!=linkedExternalRate) {
                                isLinkedDocumentHaveSameER =false;  
                                break;
                            }
                        } 
                        if(isLinkedDocumentHaveSameER){ //if exchange rate same for all linked document then applying it for current record by assigning here 
                            this.externalcurrencyrate=linkedExternalRate;
                        } else { //if exchange rate different then reassigning exchange rate of that date and giving below information message 
                            var index=this.getCurrencySymbol();
                            var exchangeRate = this.currencyStore.getAt(index).data['exchangerate'];
                            this.externalcurrencyrate=exchangeRate;
                            var msg = WtfGlobal.getLocaleText("acc.invoiceform.exchangeratemessage1")+"<b> "+this.externalcurrencyrate+" </b>"+WtfGlobal.getLocaleText("acc.invoiceform.exchangeratemessage2");                        
                            WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.information'),msg],3);
                        }
                    }               
                }

                this.setValues(billid);//In MultiSelection if the user select only one
                rec=this.PO.getValue();
                selectedValuesArr = rec.split(',');
                if(selectedValuesArr.length==1){
                    var record=this.POStore.getAt(this.POStore.find('billid',billid));
                    if (record.data['termid'] != undefined && record.data['termid'] != "") {
                        this.Term.setValue(record.data['termid']);
                    }
                    if (this.users != null && this.users != undefined) {
                    if(this.isCustomer){
                        if(record.data['salesPerson'] != undefined && record.data['salesPerson'] != ""){
                            this.users.setValue(record.data['salesPerson']) 
                        }
                    }else{
                        if(record.data['agent'] != undefined && record.data['agent'] != ""){
                            this.users.setValue(record.data['agent']);
                        }
                    }                         
                }
//                    WtfGlobal.resetCustomFields(this.tagsFieldset);
//                    var fieldArr = this.POStore.fields.items;
//                    for(var fieldCnt=0; fieldCnt < fieldArr.length; fieldCnt++) {
//                        var fieldN = fieldArr[fieldCnt];
//                        if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id) && record.data[fieldN.name] !="") {
//                            if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).getXType()=='datefield'){
//                                Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(record.data[fieldN.name]);
//                            }else if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).getXType()=='fncombo'){
//                                    var ComboValue=record.data[fieldN.name];
//                                    if(ComboValue){
//                                        Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(ComboValue);
//                                        var  childid= Wtf.getCmp(fieldN.name+this.tagsFieldset.id).childid;
//                                        if(childid.length>0){
//                                            var childidArray=childid.split(",");
//                                            for(var i=0;i<childidArray.length;i++){
//                                                var currentBaseParams = Wtf.getCmp(childidArray[i]+this.tagsFieldset.id).store.baseParams;
//                                                currentBaseParams.parentid=ComboValue;
//                                                Wtf.getCmp(childidArray[i]+this.tagsFieldset.id).store.baseParams=currentBaseParams;
//                                                Wtf.getCmp(childidArray[i]+this.tagsFieldset.id).store.load();
//                                            }
//                                        }  
//                                    }
//                            }else{
//                                Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(record.data[fieldN.name]);
//                            }
//                        }
//                        if(fieldN.name.indexOf("Custom_")==0){
//                            var fieldname=fieldN.name.substring(7,fieldN.name.length);
//                            if(Wtf.getCmp(fieldname+this.tagsFieldset.id) && record.data[fieldN.name] !="") {
//                                if(Wtf.getCmp(fieldname+this.tagsFieldset.id).getXType()=='fieldset'){
//                                    var ComboValue=record.json[fieldN.name];
//                                    var ComboValueArrya=ComboValue.split(',');
//                                    var ComboValueID="";
//                                    var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray; 
//                                    for(var i=0 ;i < ComboValueArrya.length ; i++){
//                                        for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
//                                            if(checkListCheckBoxesArray[checkitemcnt].id.indexOf(ComboValueArrya[i]) != -1 ){
//                                                if (Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id) != undefined) {
//                                                    Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id).setValue(true);
//                                                }
//                                            }
//                                        } 
//                                    }
//                                }else if(Wtf.getCmp(fieldname+this.tagsFieldset.id).getXType()=='select'){
//                                    var ComboValue=record.json[fieldN.name];
//                                    if(ComboValue!="" && ComboValue!=undefined)
//                                    Wtf.getCmp(fieldname+this.tagsFieldset.id).setValue(ComboValueID);
//                                }
//                            }
//                        }
//                    }

                    this.populateDimensionData(record);
                }else{
                    var perstore = null;
                    if(this.custVenOptimizedFlag) {
                        perstore = this.isCustomer? Wtf.customerAccRemoteStore : Wtf.vendorAccRemoteStore;
                    } else {
                        perstore = this.isCustomer? Wtf.customerAccStore : Wtf.vendorAccStore;
                    }
                    var index = perstore.find('accid',this.Name.getValue());
                    if (index != -1) {
                        var storerec=perstore.getAt(index);
                        this.Term.setValue(storerec.data['termid']);
                    }
                
                    this.users.reset();
                    var id=this.Grid.getId();
                    var rowindex=this.Grid.getColumnModel().findColumnIndex("partamount");
                    if(rowindex>=0){    
                        this.Grid.getColumnModel().setHidden( rowindex,true) ;
                    }
                }
            }else{
                var record=this.POStore.getAt(this.POStore.find('billid',billid));
//                this.linkRecord = this.POStore.getAt(this.POStore.find('billid',billid));
//                this.singleLink = true;
                var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;  //Reset Check List
                for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                    var checkfieldId = checkListCheckBoxesArray[checkitemcnt].id
                    if (Wtf.getCmp(checkfieldId) != undefined) {
                        Wtf.getCmp(checkfieldId).reset();
                    }
                } 
                var fieldArr = this.POStore.fields.items;
                for(var fieldCnt=0; fieldCnt < fieldArr.length; fieldCnt++) {
                    var fieldN = fieldArr[fieldCnt];
                   
                    if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id) && record.data[fieldN.name] !="") {
                        if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).getXType()=='datefield'){
                            Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                        }else if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).getXType()=='fncombo'){
                            var ComboValue=record.data[fieldN.name];
                            if(ComboValue){
                                Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(ComboValue);
                                var  childid= Wtf.getCmp(fieldN.name+this.tagsFieldset.id).childid;
                                if(childid.length>0){
                                    var childidArray=childid.split(",");
                                    for(var i=0;i<childidArray.length;i++){
                                        var currentBaseParams = Wtf.getCmp(childidArray[i]+this.tagsFieldset.id).store.baseParams;
                                        currentBaseParams.parentid=ComboValue;
                                        Wtf.getCmp(childidArray[i]+this.tagsFieldset.id).store.baseParams=currentBaseParams;
                                        Wtf.getCmp(childidArray[i]+this.tagsFieldset.id).store.load();
                                    }
                                }  
                            }
                        }else{
                            Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                        }
                    }
                    if(fieldN.name.indexOf("Custom_") == 0){
                        var fieldname=fieldN.name.substring(7,fieldN.name.length);
                        if(Wtf.getCmp(fieldname+this.tagsFieldset.id) && record.data[fieldN.name] !="") {
                            if(Wtf.getCmp(fieldname+this.tagsFieldset.id).getXType()=='fieldset'){
                                var ComboValue=record.json[fieldN.name];
                                var ComboValueArrya=ComboValue.split(',');
                                var ComboValueID="";
                                var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray; 
                                for(var i=0 ;i < ComboValueArrya.length ; i++){
                                    for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                                        if(checkListCheckBoxesArray[checkitemcnt].id.indexOf(ComboValueArrya[i]) != -1 ){
                                            if (Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id) != undefined) {
                                                Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id).setValue(true);
                                            }
                                        }
                                    } 
                                }
                            }else if(Wtf.getCmp(fieldname+this.tagsFieldset.id).getXType()=='select'){
                                var ComboValue=record.json[fieldN.name];
                                if(ComboValue!="" && ComboValue!=undefined)
                                Wtf.getCmp(fieldname+this.tagsFieldset.id).setValue(ComboValueID);
                            }
                        }
                    }
                }
                rec=this.POStore.getAt(this.POStore.find('billid',billid));
                
                // if(this.isOrder && this.isCustomer && !this.isCustBill){//Temporary check to hide/display product tax for order. Need to fix for Invoices also
                if(rec.data["includeprotax"]){
                    this.includeProTax.setValue(true);
                    
                    this.isTaxable.setValue(false);
                    this.isTaxable.disable();
                    this.Tax.setValue("");
                    this.Tax.disable();
                    
                    this.showGridTax(null,null,false);
                    WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.alert'),WtfGlobal.getLocaleText('acc.invoiceform.linkTax')],2);
                } else {
                    this.includeProTax.setValue(false);
                    this.isTaxable.reset();
                    this.isTaxable.enable();
                    this.showGridTax(null,null,true);
                }
                if (rec.data["gstIncluded"]&&!this.includingGST.getValue()){
                    this.includingGST.setValue(true);
                }else if(!rec.data["gstIncluded"]&&this.includingGST.getValue()){
                    this.includingGST.setValue(false);
                }
                if(!this.isCustBill && !this.isOrder && this.isCustomer){
                    if(this.fromLinkCombo.getValue()==1){
                        this.includeProTax.setValue(false);
                        this.showGridTax(null,null,true);            
                    }
                }
                this.Memo.setValue(rec.data['memo']);
                this.shipDate.setValue(rec.data['shipdate']);
                this.validTillDate.setValue(rec.data['validdate']);
                this.postText=rec.data['posttext'];
                this.shipvia.setValue(rec.data['shipvia']);
                this.fob.setValue(rec.data['fob']);
                if(this.users != null && this.users != undefined){
                    this.isCustomer ? this.users.setValue(rec.data['salesPerson']) : this.users.setValue(rec.data['agent']);
                }
                this.loadTransStore();
           
                if(rec.data['taxid']!=""){
                    this.Tax.enable();
                    this.isTaxable.setValue(true);
                    this.Tax.setValue(rec.data['taxid']);
                    WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.alert'),WtfGlobal.getLocaleText('acc.invoiceform.linkTax')],2);
                }else{
                    this.Tax.disable();
                    this.isTaxable.reset();
                    this.Tax.reset();
                }
                if(WtfGlobal.getModuleId(this)==22){
                    if(WtfGlobal.getModuleId(this)==22){//if vendor quotation is linked in customer quotation then update tax details-erp2082
                        this.includeProTax.setValue(false);
                        this.showGridTax(null,null,true);
                        this.isTaxable.enable();
                        this.isTaxable.setValue(false);
                        this.Tax.disable();
                        this.Tax.setValue("");
                    }else{//for linkcombo
                        this.updateData();
                    }
                }else{
                    this.Currency.setValue(rec.data['currencyid']);
                }
            
                var perstore = null;
                if(this.custVenOptimizedFlag) {
                    perstore=this.isCustomer? Wtf.customerAccRemoteStore:Wtf.vendorAccRemoteStore;
                } else {
                    perstore=this.isCustomer? Wtf.customerAccStore:Wtf.vendorAccStore
                }
            
                var index = perstore.find('accid',this.Name.getValue());
                if(index != -1){
                    var storerec=perstore.getAt(index);
                    this.Term.setValue(storerec.data['termid']);
                }        
                rec=rec.data['billid'];
            }
            this.updateDueDate();
            var url = "";
            var soLinkFlag = false;        
            var VQtoCQ = false;
            var sopolinkflag=false;
            var isForLinking=true;
            var prvqlinkflag = false;
            var linkingFlag = false; //For removing cross reference of DO-CI or GR-VI
            if(!this.isCustBill && !this.isOrder){
                if(this.fromLinkCombo.getValue()==2){
                    url = this.isCustomer ? "ACCSalesOrderCMN/getQuotationRows.do" : "ACCPurchaseOrderCMN/getQuotationRows.do";
                    VQtoCQ = true;//Linking Quotation when creating invoice, we need to display Unit Price excluding row discount
                }
            } else {
                if(this.isCustomer){
                    soLinkFlag = true;
                    url = "ACCPurchaseOrderCMN/getQuotationRows.do";
                    VQtoCQ = true;
                } else {
                    url= 'ACCPurchaseOrderCMN/getRequisitionRows.do';
                    prvqlinkflag = true;
                    if(this.isCustBill) {
                        url = "ACCSalesOrderCMN/getBillingSalesOrderRows.do";
                    } else {
                        if(this.fromLinkCombo.getValue()==2){
                            url = 'ACCPurchaseOrderCMN/getQuotationRows.do';
                            sopolinkflag=true;
                            VQtoCQ = true;
                        }
                    }
                }
            }    
            this.Grid.getStore().proxy.conn.url = url;
            this.Grid.loadPOGridStore(rec, soLinkFlag, VQtoCQ,linkingFlag,sopolinkflag,isForLinking,this.isInvoice,prvqlinkflag);
        }
    },
    
    loadDataForProjectStatusReport:function(){
        var url = "ACCSalesOrderCMN/getSalesOrderRows.do";
        var rec = "";
        for(var i=0;i<this.SOLinkedArr.length;i++){
            rec+=this.SOLinkedArr[i]+',';
        }
        if(rec !=""){
            rec = rec.substring(0,rec.length-1);
        }
        this.Grid.getStore().proxy.conn.url = url;
        this.Grid.loadPOGridStore(rec, false, false,false); 
    },

    setSalesPerson:function(c,rec,ind){
        this.users.setValue(rec.data['masterSalesPerson']);
    },
    
    getNextSequenceNumber:function(a,val){
        if(!(a.getValue()=="NA")){
            WtfGlobal.hideFormElement(this.Number);
            this.setTransactionNumber(true);
            var rec=WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
            var oldflag=rec!=null?rec.get('oldflag'):true;
            Wtf.Ajax.requestEx({
                url:"ACCCompanyPref/getNextAutoNumber.do",
                params:{
                    from:this.fromnumber,
                    sequenceformat:a.getValue(),
                    oldflag:oldflag
                }
            }, this,function(resp){
                if(resp.data=="NA"){
                    WtfGlobal.showFormElement(this.Number);
                    this.Number.reset();
                    if(!this.isViewTemplate){             // View mode- all fields should be disabled unconditionaly
                        this.Number.enable();
                    }   
                }else {
                    this.Number.setValue(resp.data);  
                    this.Number.disable();
                    WtfGlobal.hideFormElement(this.Number);
                }

            });
        } else {
            WtfGlobal.showFormElement(this.Number);
            this.Number.reset();
            if(!this.isViewTemplate){                 // View mode- all fields should be disabled unconditionaly
                    this.Number.enable();
            }     
        }
    },
    
    calDiscount:function(){
        return false;
    },
    
    save: function() {
        var incash = false;
        if (this.checkBeforeProceed(this.Number.getValue())) {
            this.Number.setValue(this.Number.getValue().trim());
            var isValidCustomFields = this.tagsFieldset.checkMendatoryCombo();
            this.southFormValid = true;

            var isValidNorthForm = this.NorthForm.getForm().isValid();
            if (!isValidNorthForm || !isValidCustomFields) {
                this.enableSaveButtons();
                WtfGlobal.dispalyErrorMessageDetails(this.id + 'requiredfieldmessagepanel', this.getInvalidFields());
                this.NorthForm.doLayout();
                return;
            } else {
                Wtf.getCmp(this.id + 'requiredfieldmessagepanel').hide();
            }

            if (Wtf.serialwindowflag) {
                var prodLength = this.Grid.getStore().data.items.length;
                for (var i = 0; i < prodLength - 1; i++)
                {
                    var prodID = this.Grid.getStore().getAt(i).data['productid'];
                    var prorec = this.Grid.productComboStore.getAt(this.Grid.productComboStore.find('productid', prodID));
                    if (prorec == undefined) {
                        prorec = this.Grid.getStore().getAt(i);
                    }
                    if (Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory) { //if company level option is on then only check batch and serial details
                        if (!this.quotation && (prorec.data.isBatchForProduct || prorec.data.isSerialForProduct || prorec.data.isLocationForProduct || prorec.data.isWarehouseForProduct || prorec.data.isRowForProduct || prorec.data.isRackForProduct || prorec.data.isBinForProduct)) {
                            if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') {
                                var batchDetail = this.Grid.getStore().getAt(i).data['batchdetails'];
                                var productQty = this.Grid.getStore().getAt(i).data['quantity'];
                                var baseUOMRateQty = this.Grid.getStore().getAt(i).data['baseuomrate'];
                                if (batchDetail == undefined || batchDetail == "" || batchDetail == "[]") {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.bsdetail")], 2);   //Batch and serial no details are not valid.
                                    this.enableSaveButtons();
                                    return;
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
                                        return;
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
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (Wtf.account.companyAccountPref.countryid == '137' && this.isSelfBilledInvoice) {// For Malasian Company
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
                    return;
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
                        return;
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
                        return;
                    }
                }
            }
//            if(this.NorthForm.getForm().isValid() && isValidCustomFields && this.southFormValid){
            var count = this.Grid.getStore().getCount();
            var choice = "";
            var type = "";
            if (this.isExpenseInv) {//for exoense invoice change the message
                choice = 117;
                type = 2;
            } else {
                choice = 33;
                type = 2;
            }
            var flg = this.isProductQuantityZero(choice, type);//In WtfDocumentMain.js
            if (flg) {
                this.checkDiscount();//In WtfDocumentMain.js

                if (!(this.isExpenseInv == true)) {

                    this.checkZeroUnitPriceForProduct();//In WtfDocumentMain.js
                }

                var confirmMsg = "";
                confirmMsg = this.checkDuplicateProducts();//In WtfDocumentMain.js

                var detail = this.Grid.getProductDetails();

                if (this.isCustomer && !Wtf.account.companyAccountPref.isnegativestockforlocwar && (Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory)) {
                    Wtf.Ajax.requestEx({
                        url: "ACCInvoice/getBatchRemainingQuantity.do",
                        params: {
                            detail: detail,
                            transType: this.moduleid,
                            isEdit: this.isEdit,
                            fromSubmit: false,
                            isfromdo: true
                        }
                    }, this, function(res, req) {
                        this.AvailableQuantity = res.quantity;
                        if (res.prodname) {
                            this.prodname = res.prodname;
                        }
                        if (this.prodname == "" || this.prodname == undefined) {
                            this.Callfinalsavedetails(rec, incash, confirmMsg, count);
                            return;
                        } else {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.quantityforprod") + " <b>" + this.prodname + "</b> " + WtfGlobal.getLocaleText("acc.field.isnotvalid")], 2);
                            this.enableSaveButtons();
                            return false;
                        }

                    }, function(res, req) {
                        this.enableSaveButtons();
                        return false;
                    });
                } else {
                    this.Callfinalsavedetails(rec, incash, confirmMsg, count);
                }
            } else {
                WtfComMsgBox(2, 2);
                this.enableSaveButtons();
            }
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.fxexposure.warning"), WtfGlobal.getLocaleText("acc.field.PleaseTryothervalueinInvoiceNumber")], 1);
            this.enableSaveButtons();
        }
    },
    
    Callfinalsavedetails: function(rec, incash, confirmMsg, count) {
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
    },

    createRecords:function(incash){
        incash=false;
        var rec=this.NorthForm.getForm().getValues();
        rec.gstCurrencyRate=this.gstCurrencyRate;
        rec.isselfbilledinvoice=this.isSelfBilledInvoice;
        if(rec.vendor==undefined&&this.linkIDSFlag!=undefined&&this.linkIDSFlag){
            rec.vendor=this.Name.getValue();
        }
        if(rec.customer==undefined&&this.linkIDSFlag!=undefined&&this.linkIDSFlag){
            rec.customer=this.Name.getValue();
        }
        if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_activateProfitMargin)  && (this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId)){
            rec.profitMargin= this.finalproductprofitmargin ;
            rec.profitMarginPercent= this.finalproductprofitmarginpercent;
        }
        rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
//        if(this.cash){
//            this.termid="";
//        }else{
            this.updateDueDate("","","",true);
//        }
        rec.termid=this.termid;
        this.ajxurl = ""; 
        if(this.businessPerson=="Customer"){
            this.ajxurl = "ACCSalesOrder/saveQuotation.do";
        }else if(this.businessPerson=="Vendor"){
            this.ajxurl = "ACCPurchaseOrder/saveQuotation.do";
        }
        
        this.detail = this.checkProductDetails();//In WtfDocumentMain.js

        var message = this.EditisAutoCreateDO ? ( this.businessPerson=="Customer" ? WtfGlobal.getLocaleText("acc.invoice.msg16") : WtfGlobal.getLocaleText("acc.invoice.msg19")):WtfGlobal.getLocaleText("acc.invoice.msg7")
        
        if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_memo)== true && (rec.memo=="")){    //memo related setting wether option is true
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText({
                key:"acc.common.memoempty",
                params:[WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_descriptionType)]
            }),function(btn){
                if(btn!="yes") {
                    this.enableSaveButtons();
                    return;
                }
                this.showConfirmAndSave(rec,this.detail,incash, message);                              
            },this);  
        }else {
            this.showConfirmAndSave(rec,this.detail,incash, message);             
        }
    },
    

    /****************************Generic code*********************************/
    isProductQuantityZero: function(choice, type){
        this.productCountQuantityZero=0;
        this.allProductQtyZeroFlag = true;
        for(var i=0;i<this.Grid.getStore().getCount()-1;i++){// excluding last row
            var quantity=this.Grid.getStore().getAt(i).data['quantity'];
            var rate=this.Grid.getStore().getAt(i).data['rate'];
            if(!this.isExpenseInv && quantity > 0) {
            this.allProductQtyZeroFlag = false; 
            }else if(!this.isExpenseInv && quantity==0){//For Counting how many rows with zero quantity
                this.productCountQuantityZero++;
            }
            if(!this.isExpenseInv && (quantity===""||quantity==undefined||quantity<0)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.shouldbegreaterthanZero")], 2);
                this.enableSaveButtons();
                return false;
            } 
            if(rate===""||rate==undefined||rate<0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.RateforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.cannotbeempty")], 2);
                this.enableSaveButtons();
                return false;
            }
        }
        
        var count=this.Grid.getStore().getCount();
        if(count<=1){//For Normal Empty Check
            WtfComMsgBox(choice, type)
            this.enableSaveButtons();
            return false;
        }
        if(this.allProductQtyZeroFlag && !this.isExpenseInv){ //for quantity Check in case of mapped products
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.ZeroQuantityAllProduct")], 2);
            this.enableSaveButtons();
            return false;
        }
        
        return true;
    },
    
    checkDiscount: function(){
        if(this.getDiscount()>this.Grid.calSubtotal()){
            WtfComMsgBox(12, 2);
            this.enableSaveButtons();
            return;
        }
    },
    
    checkZeroUnitPriceForProduct: function(){
        if(!WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_allowZeroUntiPriceForProduct)){
            if(this.Grid.calSubtotal()<=0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.TotalamountshouldbegreaterthanZero")], 2);
                this.enableSaveButtons();
                return;
            }
        }
    },
    
    checkDuplicateProducts: function(){
        //check is there duplicate product in transaction
        var isDuplicate=false;
        var duplicateval=", ";
        if(!this.isExpenseInv && WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_isDuplicateItems)){
            var prodLength=this.Grid.getStore().data.items.length;
            for(var i=0;i<prodLength-1;i++){ 
                var prodID=this.Grid.getStore().getAt(i).data['productid'];
                for(var j=i+1;j<prodLength-1;j++){
                    var productid=this.Grid.getStore().getAt(j).data['productid'];
                    if(prodID==productid){
                        isDuplicate = true;
                        var prorec=this.Grid.getStore().getAt(this.Grid.getStore().find('productid',prodID));//done for ERP-13480 ticket
                        if(duplicateval.indexOf(", "+prorec.data.pid+",")==-1){
                            duplicateval += prorec.data.pid+", ";//Add duplicate product id 
                        }
                    }
                }
            }
        }
        if(isDuplicate == true){
            duplicateval = duplicateval.substring(2,(duplicateval.length - 2));
        }
        var confirmMsg ="";
        if(this.productCountQuantityZero>0 && isDuplicate==true){
            confirmMsg = duplicateval+" "+WtfGlobal.getLocaleText("acc.field.duplicateproduct")+" and "+WtfGlobal.getLocaleText("acc.field.ZeroQuantitySomeProduct");
        } else if(this.productCountQuantityZero>0) {
            confirmMsg = WtfGlobal.getLocaleText("acc.field.ZeroQuantitySomeProduct");
        } else if(isDuplicate==true) {//duplicate product case
            confirmMsg =duplicateval+" "+ WtfGlobal.getLocaleText("acc.field.duplicateproduct")+". "+WtfGlobal.getLocaleText("acc.msgbox.Doyouwanttoproceed");
        }
        
        return confirmMsg;
    },
    
    checkProductDetails: function(){
        var detail = this.Grid.getProductDetails();
        var validLineItem=this.Grid.checkDetails(this.Grid);
        if(validLineItem!="" && validLineItem!=undefined){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),(WtfGlobal.getLocaleText("acc.msgbox.lineitem")+validLineItem)],2); 
            this.enableSaveButtons();
            return;
        }
        if(detail == undefined || detail == "[]"){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
            this.enableSaveButtons();
            return;
        }
        
        return detail;
    },
    
    showConfirmAndSave: function(rec,detail,incash, msg){
        Wtf.MessageBox.show({
            title:WtfGlobal.getLocaleText("acc.common.savdat"),
            msg:msg,
            buttons: Wtf.MessageBox.YESNO,
            icon: Wtf.MessageBox.INFO,
            width:300,
            scope:{
                scopeObject:this
            },
            fn:function(btn){
                if(btn!="yes") {
                    this.mailFlag=false;
                    this.saveOnlyFlag=false;
                    this.scopeObject.enableSaveButtons();
                    return;
                }
                this.scopeObject.finalSave(rec,detail,incash);
            }
        },this);
    },
    /*************************************************************/
    
    finalSave: function (rec,detail,incash){
        this.mailFlag=true;
        rec.taxid=this.Tax.getValue();
        rec.isfavourite = false;
        if(!this.copyInv){
            if((this.record && this.record !== undefined) && (this.record.get('isfavourite') !== null || this.record.get('isfavourite') !== undefined)){
                rec.isfavourite = this.record.get('isfavourite');
            }
        }
        rec.taxamount=this.caltax();
        rec.detail=detail;
        var custFieldArr=this.tagsFieldset.createFieldValuesArray();
        this.msg= WtfComMsgBox(27,4,true);
        rec.subTotal=this.Grid.calSubtotal()
        this.applyCurrencySymbol();
        rec.currencyid=this.Currency.getValue();
        rec.externalcurrencyrate=this.externalcurrencyrate;
        rec.posttext=this.postText;
        rec.istemplate=this.transactionType;
        rec.moduletempname=this.isTemplate;

        if(this.copyInv && this.record && this.record.data.contract){
            rec.contractId=this.record.data.contract;
        }
        if (custFieldArr.length > 0){
            rec.customfield = JSON.stringify(custFieldArr);
        }
        rec.invoicetermsmap = this.getInvoiceTermDetails();
        if(this.Grid.deleteStore!=undefined && this.Grid.deleteStore.data.length>0){
            rec.deletedData=this.getJSONArray(this.Grid.deleteStore,false,0);
        }
        rec.number=this.Number.getValue();
        rec.linkNumber=(this.PO != undefined && this.PO.getValue()!="")?this.PO.getValue():"";
        
        if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_countryid) == '137'){// For Malasian Company
            var isInvoiceLinkedWithTaxAppliedDO = false;

            if(this.PO != undefined && this.PO.getValue()!=""){
                var linkNumberArray = this.PO.getValue().split(",");
                for(var i=0;i<linkNumberArray.length;i++){
                    var porecord=this.POStore.getAt(this.POStore.find('billid',linkNumberArray[i]));
                    if(porecord.data.isAppliedForTax){
                        isInvoiceLinkedWithTaxAppliedDO = true;
                    }
                }
            }
            rec.isInvoiceLinkedWithTaxAppliedDO=isInvoiceLinkedWithTaxAppliedDO;
        }
        
        rec.fromLinkCombo=this.fromLinkCombo.getRawValue();
        rec.duedate=WtfGlobal.convertToGenericDate(this.DueDate.getValue());
        rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
        rec.shipdate=WtfGlobal.convertToGenericDate(this.shipDate.getValue());
        rec.validdate=WtfGlobal.convertToGenericDate(this.validTillDate.getValue());
        rec.invoiceid=(this.copyInv || (!this.isCustomer && this.ispurchaseReq))?"":this.billid;
        rec.doid=this.DeliveryOrderid;
        rec.mode=(this.isOrder?(this.isCustBill?51:41):(this.isCustBill?13:11));
        rec.incash=incash;
        this.totalAmount = rec.subTotal + rec.taxamount - this.getDiscount();
        rec.includeprotax = (this.includeProTax)? this.includeProTax.getValue() : false;
        rec.includingGST = (this.includingGST)? this.includingGST.getValue() : false;
        var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
        rec.sequenceformat=this.sequenceFormatCombobox.getValue();
        rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):false;
        var isCopy = this.copyInv;
        var isEdit = this.isEdit;
        if (this.isVenOrCustSelect) {
            isEdit = false;
            isCopy = false;
        }
        rec=WtfGlobal.getAddressRecordsForSave(rec,this.record,this.linkRecord,this.currentAddressDetailrec,this.isCustomer,this.singleLink,isEdit,isCopy,this.GENERATE_PO,this.GENERATE_SO,this.isQuotationFromPR);
        rec.isEdit=this.isEdit;
        rec.copyInv=this.copyInv; 

        if(this.isAutoCreateDO ||  this.EditisAutoCreateDO){
            rec.isAutoCreateDO = this.EditisAutoCreateDO ? this.EditisAutoCreateDO : this.isAutoCreateDO;
            rec.fromLinkComboAutoDO =this.isCustomer ? "Customer Invoice" : "Vendor Invoice";
        } 
        if(this.isCustomer && this.fromPO!=undefined && this.fromPO.getValue()===true){
            rec.linkFrom='Vendor Quotation';  
        }
        if(incash){
            if(!this.SouthForm.hidden){//when payment type bank or card
              var paydetail=this.SouthForm.GetPaymentFormData();    
              rec.paydetail = paydetail;
              if(this.SouthForm.paymentStatus.getValue() == "Cleared"){
                  rec.startdate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true));
                  rec.enddate = WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false));
              }
            }                                  
        }
        rec['termsincludegst'] = this.termsincludegst;
        rec.isDraft = this.isDraft? this.isDraft : false;
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url:this.ajxurl,
            params: rec
        },this,this.genSuccessResponse,this.genFailureResponse);
    },

    handleProductTypeForConsignment: function(){
        // In Consignment link case select product of service type product reset and prompt msg
        if(this.invoiceList != undefined && this.invoiceList.getValue() != "") {
            var productid;
            if(this.Grid != undefined) {
                for(var i=0; i<this.Grid.getStore().getCount(); i++) {
                    productid = this.Grid.getStore().getAt(i).get("productid");

                    if((productid != undefined || productid != "") && (this.Grid != undefined && this.Grid.getStore().getCount() > 0)) {
                        var index = this.Grid.productComboStore.find('productid',productid);
                        if(index != -1) {
                            var productType = this.Grid.productComboStore.getAt(index).get("type");
                            if(productType == "Service") {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.consignmentCaseProductSelectMsg")], 2);

                                // For reset all related fields
                                var customer= this.Name.getValue();
                                if(!this.GENERATE_PO&&!this.GENERATE_SO){
                                    this.loadStore();
                                    this.Name.setValue(customer);
                                }
                                this.updateData();
                            }
                        }
                    }
                }
                
            }
        }
    },

    loadTransStore : function(productid){
        if(this.Name.getValue() != ""){
            var customer= (this.businessPerson=="Vendor")? "" : this.Name.getValue();
            var vendor= (this.businessPerson=="Vendor")? this.Name.getValue() : "" ;
            if((productid == undefined || productid == "") && this.Grid.getStore().getCount() > 0){
                productid = this.Grid.getStore().getAt(0).get("productid");
            }
            
            // In Consignment link case select product of service type product reset and prompt msg
            this.handleProductTypeForConsignment();
            
            this.lastTransPanel.Store.on('load', function(){
                Wtf.getCmp('south' + this.id).doLayout();
            }, this);
            if(productid) {
                this.lastTransPanel.productid = productid;
                this.lastTransPanel.Store.load({
                    params:{
                        start:0,
                        limit:5, 
                        prodfiltercustid:customer,
                        prodfilterventid:vendor,
                        productid : productid
                    }
                });
            }
        }
    },
    
    removeTransStore : function(){
        this.lastTransPanel.Store.removeAll();
        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{productname:"&nbsp;&nbsp;&nbsp;&nbsp;",productid:0,qty:0,soqty:0,poqty:0});
    },
    
    updateData:function(){
        var customer= this.Name.getValue();
        if(Wtf.getCmp("showaddress" + this.id)){
            Wtf.getCmp("showaddress" + this.id).enable(); 
        } 
        var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
        var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
         
        if(actualTaxId== undefined || actualTaxId == "" ||  actualTaxId == null){// if customer/vendor is not mapped with tax then check that is their mapping account is mapped with tax or not, if it is mapped take account tax
            actualTaxId=currentTaxItem!=null?currentTaxItem.get('mappedAccountTaxId'):"";
        }
         
        if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
            this.isTaxable.setValue(true);
            this.Tax.enable();
            this.isTaxable.enable();
            this.Tax.setValue(actualTaxId);
        } else {
            this.isTaxable.setValue(false);
            this.Tax.setValue('');
            this.Tax.disable();
        }
        if(this.Grid) {
            this.Grid.affecteduser = this.Name.getValue();
        }
        
        this.loadTransStore();
        Wtf.Ajax.requestEx({
            url:"ACC"+this.businessPerson+"CMN/getCurrencyInfo.do",
            params:{
                mode:4,
                customerid:customer,
                isBilling : this.isCustBill
            }
        }, this,this.setCurrencyInfo);
        if(this.fromPO){
            this.fromPO.enable();
        }
    },
    
    getTerm:function(val1,val2){
        val1=new Date(this.record.data.date);
        val2=new Date(this.record.data.duedate);
        var msPerDay = 24 * 60 * 60 * 1000
        var termdays = Math.floor((val2-val1)/ msPerDay) ;
        var FIND =termdays;
        var index=this.termds.findBy( function(rec){
            var parentname=rec.data.termdays;
            if(parentname==FIND){
                return true;
            }
            else{
                return false
            }
        });
        if(index>=0){
            var  rec=this.termds.getAt(index)
            this.Term.setValue(rec.data.termid);
        }
    },

    updateDueDate:function(a,val,index,isSave){
        var term=null;
        var rec=null;
        var validTillDate=null;
            if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_noOfDaysforValidTillField)!=-1){
                validTillDate=new Date(this.billDate.getValue()).add(Date.DAY, WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_noOfDaysforValidTillField));
            }
        if(validTillDate!=null && !isSave){
            this.validTillDate.setValue(validTillDate)
        }
        if(this.Term.getValue()!="" && this.Term.getValue()!=null && this.Term.getValue()!=undefined){
            rec = this.Term.store.getAt(this.Term.store.find('termid',this.Term.getValue()));
            if(rec != null && rec != undefined){ // Added null check (in cash case get null). For Cash transaction Term is not present.
                term=new Date(this.billDate.getValue()).add(Date.DAY, rec.data.termdays);
            }
        }else{
            term=this.billDate.getValue();
        }

        if(term != null) {
            if(!(isSave != undefined && isSave != "" && isSave==true) ){
                this.NorthForm.getForm().setValues({duedate:term});
            }
        }
        if(this.Grid){
            this.Grid.billDate = this.billDate.getValue()
        }
        rec = this.Term.store.getAt(this.Term.store.find('termid',this.Term.getValue()));
        if(rec != null && rec != undefined){
            this.termid=rec.data.termid;
        }
    },

    genSuccessResponse:function(response, request){
        WtfGlobal.resetAjaxTimeOut();
        this.enableSaveButtons();
        this.RecordID=response.SOID!=undefined?response.SOID : response.invoiceid;
        if(this.moduleid==Wtf.Acc_Vendor_Quotation_ModuleId && Wtf.getCmp("VendorQuotationList") != undefined && response.success){
            var msgTitle = this.titlel;
            Wtf.getCmp("VendorQuotationList").Store.on('load', function() {
                WtfComMsgBox([msgTitle,response.msg],response.success*2+1);
            }, Wtf.getCmp("VendorQuotationList").Store, {
                single : true
            });
        } else {
            WtfComMsgBox([this.titlel,response.msg],response.success*2+1);
        }
        
        var rec=this.NorthForm.getForm().getValues();
        if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)||!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){//after saving
            this.exportRecord=rec;
            this.exportRecord['billid']=response.billid||response.invoiceid;
            this.exportRecord['billno']=response.billno||response.invoiceNo;
            this.exportRecord['amount']=(this.moduleid==22||this.moduleid==23)?this.totalAmount:response.amount; //ERP-9467 Added SO module id.
            this.exportRecord['isexpenseinv']=response.isExpenseInv!=undefined? response.isExpenseInv:false; //To export the good receipt of Expense Type.
            this.singlePrint.exportRecord=this.exportRecord;
            this.singleRowPrint.exportRecord=this.exportRecord;
        }
        if(response.success){
            if(this.productOptimizedFlag==Wtf.Show_all_Products){
                if(!this.isCustBill){
                    Wtf.productStoreSales.reload();
                    Wtf.productStore.reload();   //Reload all product information to reflect new quantity, price etc                
               }            	
            }
            if(this.isTemplate){
                this.ownerCt.remove(this);
            }
            
            if(this.saveOnlyFlag){
                this.loadUserStoreForInvoice(response, request);
                this.disableComponent();
                this.response = response;
                this.request = request;
                return;
            }
            this.currentAddressDetailrec="";//after saveandcreatenew this variable need to clear it old values. 
            this.singleLink = false;
            this.isVenOrCustSelect=false;
            if(this.maintenanceNumberComboStore &&!this.isExpenseInv){
                this.maintenanceNumberComboStore.load();
            }
            this.lastTransPanel.Store.removeAll();
            this.symbol = WtfGlobal.getCurrencySymbol();
            this.currencyid = WtfGlobal.getCurrencyID();
            this.loadStore();
            this.fromPO.disable();
            if(!this.SouthForm.hidden){
               this.SouthForm.hide(); 
            }
            this.currencyStore.load();                  
            this.Currency.setValue(WtfGlobal.getCurrencyID()); // Reset to base currency 
            this.externalcurrencyrate=0; //Reset external exchange rate for new Transaction.
            this.isClosable= true;       //Reset Closable flag to avoid unsaved Message.
            this.termStore.reload(); // Reset Purchase/Sales Term store when clicked "Save and create new" button                 
            Wtf.dirtyStore.product = true;
            var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
            var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):""; 
            if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
                this.isTaxable.setValue(true);
                this.Tax.enable();
                this.Tax.setValue(actualTaxId);
            }else{
                this.isTaxable.setValue(false);
                this.Tax.setValue('');
                this.Tax.disable();
            }
            this.postText="";
            var customFieldArray = this.tagsFieldset.customFieldArray;  //Reset Custom Fields
            if(customFieldArray!=null && customFieldArray!=undefined && customFieldArray!="" ) {
                for (var itemcnt = 0; itemcnt < customFieldArray.length; itemcnt++) {
                    var fieldId = customFieldArray[itemcnt].id
                    if (Wtf.getCmp(fieldId) != undefined && customFieldArray[itemcnt].getXType()!='fieldset') {
                        Wtf.getCmp(fieldId).reset();
                    }
                } 
            }
            var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;  //Reset Check List
            if(checkListCheckBoxesArray!=null && checkListCheckBoxesArray!=undefined && checkListCheckBoxesArray!="" ) {
                for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                    var checkfieldId = checkListCheckBoxesArray[checkitemcnt].id
                    if (Wtf.getCmp(checkfieldId) != undefined) {
                        Wtf.getCmp(checkfieldId).reset();
                    }
                }
            }
            var customDimensionArray = this.tagsFieldset.dimensionFieldArray;  //Reset Custom Dimension
            if(customDimensionArray!=null && customDimensionArray!=undefined && customDimensionArray!="" ){
                for (var itemcnt1 = 0; itemcnt1 < customDimensionArray.length; itemcnt1++) {
                    var fieldId1 = customDimensionArray[itemcnt1].id
                    if (Wtf.getCmp(fieldId1) != undefined) {
                        Wtf.getCmp(fieldId1).reset();
                    }
                } 
                this.fireEvent('update',this);
                this.amountdue=0;
            }
        }
    },

    genFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    
    callEmailWindowFunction : function(response, request){
        if(response.pendingApproval){
            var titleMsg = this.getLables();
            WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.information'),titleMsg+' '+WtfGlobal.getLocaleText("acc.field.ispendingforapprovalSoyoucannotsendmailrightnow")],3);
            return;
        }
        if(this.CustomStore != null){
            var rec = this.CustomStore.getAt(0);
            var rec = "";
            if (response.billid != undefined || response.billid != '') {
                rec = this.CustomStore.getAt(this.CustomStore.find('billid', response.billid));
            }
            var label = "";
            if(this.isCustomer){
                label = WtfGlobal.getLocaleText("acc.dimension.module.12");
                callEmailWin("emailwin",rec,label,50,true,false,true);
            }else{
                label = WtfGlobal.getLocaleText("acc.vend.createvendQ");
                callEmailWin("emailwin",rec,label,57,false,false,true);
            }
        }
    },

    getLables : function(){
        var label = "";
        if(this.isCustomer){
            label = WtfGlobal.getLocaleText("acc.accPref.autoCQN");
        }else{
            label = WtfGlobal.getLocaleText("acc.dimension.module.11");
        }
        return label;
    },

    disableComponent: function(){ // disable following component in case of save button press.
        if(this.fromLinkCombo && this.fromLinkCombo.getValue() === ''){
            this.fromLinkCombo.clearValue();
        }

        if(this.PO && this.PO.getValue() === ''){
            this.handleEmptyText=true;
            this.PO.clearValue();        
        }

        if(this.savencreateBttn){
            this.savencreateBttn.disable();
        }
        if(this.saveBttn){
            this.saveBttn.disable();
        }

//        if(Wtf.getCmp("posttext" + this.id)){
//            Wtf.getCmp("posttext" + this.id).disable();
//        }

        if(Wtf.getCmp("showaddress" + this.id)){
            Wtf.getCmp("showaddress" + this.id).disable(); 
        } 

        if(this.Grid){
            var GridStore = this.Grid.getStore();
            var count2 = GridStore.getCount();
            var lastRec2 = GridStore.getAt(count2-1);
            GridStore.remove(lastRec2);
        }

        if(this.GridPanel){
            if(this.modeName=="autocashpurchase" || this.modeName=="autogoodsreceipt"){
                this.ProductGrid.purgeListeners();
            }else{
                this.GridPanel.disable();   
            }
        }else{
            this.Grid.purgeListeners();
        }

        if(this.NorthForm){
            this.NorthForm.disable();
        }

        if(this.southPanel){
            this.southPanel.disable();
        }
        if(this.SouthForm){
            this.SouthForm.disable(); 
        }
    },

    enableSaveButtons:function(){
        this.savencreateBttn.enable();
        this.saveBttn.enable();
    },
    
    disableSaveButtons:function(){
        this.savencreateBttn.disable();
        this.saveBttn.disable();
    },

    loadUserStoreForInvoice : function(response, request){
        var customRec = Wtf.data.Record.create ([
            {name:'billid'},
            {name:'journalentryid'},
            {name:'entryno'},
            {name:'billto'},
            {name:'discount'},
            {name:'currencysymbol'},
            {name:'orderamount'},
            {name:'isexpenseinv'},
            {name:'currencyid'},
            {name:'shipto'},
            {name:'mode'},
            {name:'billno'},
            {name:'date', type:'date'},
            {name:'duedate', type:'date'},
            {name:'shipdate', type:'date'},
            {name:'personname'},
            {name:'personemail'},
            {name:'personid'},
            {name:'shipping'},
            {name:'othercharges'},
            {name:'partialinv',type:'boolean'},
            {name:'amount'},
            {name:'amountdue'},
            {name:'termdays'},
            {name:'termname'},
            {name:'incash'},
            {name:'taxamount'},
            {name:'taxid'},
            {name:'orderamountwithTax'},
            {name:'taxincluded',type:'boolean'},
            {name:'taxname'},
            {name:'deleted'},
            {name:'amountinbase'},
            {name:'memo'},
            {name:'externalcurrencyrate'},
            {name:'ispercentdiscount'},
            {name:'discountval'},
            {name:'crdraccid'},
            {name:'creditDays'},
            {name:'isRepeated'},
            {name:'porefno'},
            {name:'costcenterid'},
            {name:'costcenterName'},
            {name:'interval'},
            {name:'intervalType'},
            {name:'startDate', type:'date'},
            {name:'nextDate', type:'date'},
            {name:'expireDate', type:'date'},
            {name:'repeateid'},
            {name:'status'},
            {name:'archieve', type:'int'},
            {name:'withoutinventory',type:'boolean'},
            {name:'rowproductname'},
            {name:'rowquantity'},
            {name:'rowrate'},
            {name:'rowprdiscount'},
            {name:'rowprtaxpercent'},
            {name:'agent'},
            {name:'includeprotax'}
        ]);

        var customStoreUrl = "";
        customStoreUrl = this.isCustomer? "ACCSalesOrderCMN/getQuotations.do" : "ACCPurchaseOrderCMN/getQuotations.do";
        this.CustomStore = new Wtf.data.GroupingStore({
            url:customStoreUrl,
            scope:this,
            baseParams:{
                archieve:0,
                deleted:false,
                nondeleted:false,
                cashonly:false,
                creditonly:false,
                consolidateFlag:false,
                companyids:companyids,
                enddate:'',
                pendingapproval:response.pendingApproval,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                isfavourite:false,
                startdate:'',
                ss:request.params.number
            },
            sortInfo : {
                field : 'companyname',
                direction : 'ASC'
            },
            groupField : 'companyname',
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },customRec)
        });

        this.CustomStore.on('load', this.enableButtons(), this);
        this.CustomStore.load();

    },

    enableButtons : function(){
        if(Wtf.getCmp("emailbut" + this.id)){
            Wtf.getCmp("emailbut" + this.id).enable();
        }
        if(Wtf.getCmp("exportpdf" + this.id)){
            Wtf.getCmp("exportpdf" + this.id).enable();
        }
        if (Wtf.getCmp("printSingleRecord" + this.id)) { //Enabling Print record button after saving
            Wtf.getCmp("printSingleRecord" + this.id).enable();
        }
    },
    
    loadStore:function(){
        if(!this.isEdit && !this.copyInv){
            this.Grid.getStore().removeAll();
        }
        this.PO.setDisabled(true);
        this.fromLinkCombo.setDisabled(true);
        if(this.isTemplate){
            this.createTransactionAlsoOldVal = this.createTransactionAlso;
        }
        if(this.isEdit){//in edit case need to preserve some data befor resetall
            this.number=this.Number.getValue();                
        }
        this.resetField();
        this.Term.clearValue();
        if(this.isExpenseInv){
            this.includingGST.setValue(false);
            this.includingGST.disable();
        }else{
            this.includingGST.reset();
            this.includingGST.enable();
        }
        if(this.isEdit){//in edit case need to preserve some data befor resetall
            this.billDate.setValue(Wtf.serverDate);              
        }
        this.sequenceFormatStore.load();
        if(this.isTemplate){
            if(this.createTransactionAlsoOldVal){
                this.createAsTransactionChk.setValue(true);
                if(!this.isViewTemplate){               // View mode- all fields should be disabled unconditionaly
                    this.Number.enable();
                }    
                this.sequenceFormatCombobox.enable();
            }
        }
        this.setTransactionNumber();
        if(this.fromPO){
            this.fromPO.enable();
        }
        if(this.fromLinkCombo){
            this.fromLinkCombo.setDisabled(true);
            this.fromLinkCombo.clearValue();
        }
        this.fromPO.setValue(false); 
        if(!this.isEdit && !this.copyInv){
            this.Grid.getStore().removeAll();
        }
        
        var id=this.Grid.getId();
        var rowindex=this.Grid.getColumnModel().findColumnIndex("partamount");
        if(rowindex != -1){
            this.Grid.getColumnModel().setHidden( rowindex,true);
        }

        this.showGridTax(null,null,true);
        this.Grid.symbol=undefined; // To reset currency symbol. BUG Fixed #16202
        this.Grid.updateRow(null);
        this.resetForm = true;
        var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
        var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
        if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
            this.isTaxable.setValue(true);
            this.Tax.enable();
            this.Tax.setValue(actualTaxId);
        }else{
            this.Tax.setValue("");
            this.Tax.setDisabled(true);				// 20148 fixed
            this.isTaxable.setValue(false);
        }
        
        this.template.setValue(Wtf.Acc_Basic_Template_Id);
        this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(new Date())}});   
        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{productname:"&nbsp;&nbsp;&nbsp;&nbsp;",productid:0,qty:0,soqty:0,poqty:0});
        this.currencyStore.on("load",function(store){
            if(this.resetForm){
                if(this.Currency.getValue() !="" && WtfGlobal.searchRecord(this.currencyStore,this.Currency.getValue(),"currencyid") == null){
                    callCurrencyExchangeWindow();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.msg1")],2); //"Please set Currency Exchange Rates"
                } else {
                    this.isCurrencyLoad=true;
                    if(Wtf.productDetailsGridIsEmpty(this.Grid)) {           //refer ticket ERP-14001
                        var personId = this.Name.getValue();
                        var personRec = WtfGlobal.searchRecord(this.Name.store, personId, this.Name.valueField);
                        var personCurrencyId = personRec ? (personRec.data ? personRec.data.currencyid : WtfGlobal.getCurrencyID()): WtfGlobal.getCurrencyID();
                        this.Currency.setValue(personCurrencyId);
                        var currencyRecord = WtfGlobal.searchRecord(this.currencyStore, personCurrencyId, this.Currency.valueField);
                        this.externalcurrencyrate = currencyRecord.data ? currencyRecord.data.exchangerate : "";//set because old value was company base currency's exchange rate i.e. '1'
                    } else {
                        this.Currency.setValue(WtfGlobal.getCurrencyID());
                    }
                    this.applyCurrencySymbol();
                    this.showGridTax(null,null,true);
                    if(this.isEdit){  
                        if(this.record.data.includeprotax){
                            this.includeProTax.setValue(true);
                            this.showGridTax(null,null,false);
                        }else{
                            this.includeProTax.setValue(false);
                            this.showGridTax(null,null,true);
                        }
                    }
                    var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
                    var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
                    if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
                        this.isTaxable.setValue(true);
                        this.Tax.enable();
                        this.Tax.setValue(actualTaxId);
                    } else {
                        this.isTaxable.setValue(false);
                        this.Tax.setValue('');
                        this.Tax.disable();
                    }
                    if(this.isEdit){
                        this.setProductAndTransactionTaxValues();
                    }
                    this.resetForm = false;
                }
            }
        },this);
    },

    resetField: function(){
        this.ShowOnlyOneTime.enable();
        this.Currency.reset();
        this.PO.reset();
        this.sequenceFormatCombobox.reset();
        this.Number.reset();
        this.billDate.reset();
        this.CustomerPORefNo.reset();
        this.shipDate.reset();
        this.Term.reset();
        this.DueDate.reset();
        this.Memo.reset();
        this.shipvia.reset();
        this.fob.reset();
        this.includeProTax.reset();
        this.validTillDate.reset();
        this.template.reset();
        this.templateID.reset();
        this.users.reset();
        this.Name.reset();
        this.SouthForm.getForm().reset();    
    },
    
    setDate:function(){
        var height = 0;
        if(this.isCustomer){
            height=430;
        } else {
            height=310;
        }
        if(height>=178) this.NorthForm.setHeight(height);

        if(!this.isEdit || this.isCopyFromTemplate){
            this.billDate.setValue(Wtf.serverDate);//(new Date());
        }
    },
    
    addCreditTerm:function(){
        callCreditTerm('credittermwin');
        Wtf.getCmp('credittermwin').on('update', function(){this.termds.reload();}, this);
    },
    
    addPerson:function(isEdit,rec,winid,isCustomer){
        callBusinessContactWindow(isEdit, rec, winid, isCustomer);
        var tabid=this.isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
        Wtf.getCmp(tabid).on('update', function(){
           this.isCustomer?Wtf.customerAccStore.load():Wtf.vendorAccStore.reload();
        }, this);
    },

    setTransactionNumber:function(isSelectNoFromCombo){
    	if(this.quotation==null || this.quotation==undefined){
            this.quotation = false;
        }
    	
        if(!this.isEdit||this.copyInv){
            var temp=this.isCustBill*1000+this.isCustomer*100+this.isOrder*10+false*1+this.quotation*1;
            var temp2=0;
            var format="";
            switch(temp){
                case 111:
                    format=WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_autoquotation);
                    temp2=Wtf.autoNum.Quotation;
                break;
                case 11:
                    format=WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_autovenquotation);
                    temp2=Wtf.autoNum.Venquotation;
                break;         
            }

            if(isSelectNoFromCombo){
                this.fromnumber = temp2;
            } else if(format&&format.length>0 && (!this.isTemplate || this.createTransactionAlso)){
                WtfGlobal.fetchAutoNumber(temp2, function(resp){if(this.isEdit){this.Number.setValue(resp.data)}}, this);
            }
        }
    },
    
    displayMsg : function(){
        WtfComMsgBox(29,4,true);
    },
    
    invoiceCreationJSON: function(){
        this.ajxUrl = "CommonFunctions/getInvoiceCreationJson.do";
        var params={
            transactiondate:this.transdate,
            loadtaxstore:true,
            moduleid :this.moduleid,
            loadcurrencystore:true,
            loadtermstore:true
        }
        Wtf.Ajax.requestEx({url:this.ajxUrl,params:params}, this, this.successCallback, this.failureCallback);
    },
    
    loadInitialStore: function(){
        this.sequenceFormatStore.load();
        this.isCustomer ? Wtf.salesPersonFilteredByCustomer.load() : Wtf.agentStore.load(); //new store is used for salesperson combo 
        this.templateStore.load();
        if(WtfGlobal.getCompanyAccountPrefObj(Wtf.companyAccountPref_activateProfitMargin) && this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId){
            this.productComboStore.load();
        }
        if(!this.custVenOptimizedFlag){
            if(this.isCustomer){
                Wtf.customerAccStore.load();
            }else{
                Wtf.vendorAccStore.reload();
            }
        }
        if (!this.custVenOptimizedFlag) {
            this.isCustomer?chkcustaccload():chkvenaccload();
        }
    },
    
    dueDateCheck:function(){
        if(this.DueDate.getValue().getTime()<this.billDate.getValue().getTime()){
           WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.msg11")], 2);    //"The Due Date should be greater than the Order Date."
           this.DueDate.setValue(this.billDate.getValue());
        }
    },
    
    loadTemplateStore : function() {
        this.templateStore.load();
    },
    
    addInvoiceTemplate : function(isCreatedNow,tempid) {
        if(isCreatedNow===true){
            this.loadTemplateStore();
            this.templateStore.on("load",function(){
            	this.template.setValue(tempid);
            	this.templateID.setValue(tempid);
            },this)
            	
        }else{
            new Wtf.selectNewTempWin({
                isreport : false,
                tabObj : this,
                templatetype : this.doctype 
            });
        }
    }, 
    
    onVendorSelect : function( vendorrec , rec){
        if(vendorrec != null){
            var currRec=WtfGlobal.searchRecord(this.currencyStore, vendorrec.data.currencyid, 'currencyid');
            if(currRec!=null){
                this.setVendorCurrExchangeRate(rec,currRec);
            }
        }
    },
    
    onDateChangeVendorCurrencyExchangeRate : function(){
        var count=this.Grid.store.getCount();
        for(var i=0;i< count;i++){
            var rec = this.Grid.store.getAt(i);
            if(rec!=null && rec.data.vendorcurrencyid!="" && rec.data.vendorcurrencyid!=null && rec.data.productid!=""){
                var currRec=WtfGlobal.searchRecord(this.currencyStore, rec.data.vendorcurrencyid, 'currencyid');
                if(currRec!=null){
                    this.setVendorCurrExchangeRate(rec,currRec);
                }
            }
        }
    },
    
    setVendorCurrExchangeRate : function(rec,currRec){
        if(currRec!=null){
            var baseToVenCurr = currRec.data.exchangerate;
            var revExchangeRate = 1/(baseToVenCurr-0);
            revExchangeRate = getRoundofValueWithValues(revExchangeRate,10);
            rec.data.vendorcurrexchangerate = revExchangeRate;
        }
    },
    
    setValues:function(billid){
        if(billid.indexOf(",")==-1){  //In MultiSelection if the user select only one                              
            var rec=this.POStore.getAt(this.POStore.find('billid',billid));
            if (this.users != null && this.users != undefined) {
                if(this.isCustomer){
                    if(rec.data['salesPerson'] != undefined && rec.data['salesPerson'] != ""){
                        this.users.setValue(rec.data['salesPerson']) 
                    }
                }else{
                    if(rec.data['agent'] != undefined && rec.data['agent'] != ""){
                        this.users.setValue(rec.data['agent']);
                    }
                }                         
            }
            this.Memo.setValue(rec.data['memo']);
            this.shipDate.setValue(rec.data['shipdate']);
            this.validTillDate.setValue(rec.data['validdate']);
            this.postText=rec.data['posttext'];
            this.shipvia.setValue(rec.data['shipvia']);
            this.fob.setValue(rec.data['fob']);  
            if(rec.data['taxid']!=""){
                this.Tax.enable();
                this.isTaxable.setValue(true);
                this.Tax.setValue(rec.data['taxid']);
            }else{
                this.Tax.disable();
                this.isTaxable.reset();
                this.Tax.reset();
            }     
            var perstore= null;
            if(this.custVenOptimizedFlag) {
                perstore = this.isCustomer? Wtf.customerAccRemoteStore:Wtf.vendorAccRemoteStore;
            } else {
                perstore=this.isCustomer? Wtf.customerAccStore:Wtf.vendorAccStore
            }
                    
            var index = perstore.find('accid',this.Name.getValue());
            if(index != -1){
                var storerec=perstore.getAt(index);                        
                this.Term.setValue(storerec.data['termid']);
            }        
        } else { //if the user select multiple values
            this.clearComponentValues();
        }
    },
    
    clearComponentValues:function(){
        this.Memo.setValue('');
        this.shipDate.setValue('');
        this.validTillDate.setValue('');
        this.shipvia.setValue('');
        this.fob.setValue('');
        this.loadTransStore();
    },
    
    onCurrencyChangeOnly:function(){
        this.fromPO.reset();
        this.fromLinkCombo.reset();this.fromLinkCombo.setDisabled(true);
        this.PO.reset();this.PO.setDisabled(true);                                       
        var id=this.Grid.getId();
        var rowindex=this.Grid.getColumnModel().findColumnIndex("partamount");
        if(rowindex != -1){
            this.Grid.getColumnModel().setHidden( rowindex,true);
        }                             
        var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
        var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";
        if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null ){
            this.isTaxable.setValue(true);
            this.Tax.enable();
            this.Tax.setValue(actualTaxId);
        }else{     
            this.Tax.disable();
            this.isTaxable.reset();
            this.Tax.reset();
        }
        this.includeProTax.setValue(false);
        this.showGridTax(null,null,true);
        this.Grid.getStore().removeAll();
        this.Grid.addBlankRow(); 
        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{productname:"&nbsp;&nbsp;&nbsp;&nbsp;",qty:0,soqty:0,poqty:0});                
    },
    
    getAddressWindow:function(){
        var addressRecord="";
        var isCopy="";
        var isEdit="";
        addressRecord=this.record; 
        isCopy=this.copyInv;
        isEdit=this.isEdit;
        /*this.isQuotationFromPR is true only when we Vendor Quotation from PR Report  by clicking on button Record Vendor Quotation
         *These all case is like create new case for addresses so initialising variable as they are in create new case 
         **/
        if(this.isQuotationFromPR){
           addressRecord=null; 
           isCopy=false;
           isEdit=false;
        } else {
           addressRecord=this.record; 
           isCopy=this.copyInv;
           isEdit=this.isEdit;
        }
        var custvendorid=this.Name.getValue();
        if (this.linkRecord && this.singleLink) {     //when user link single record
            addressRecord = this.linkRecord;
        }
        if (this.isVenOrCustSelect) {
            isEdit = false;
            isCopy = false;
        }
        callAddressDetailWindow(addressRecord,isEdit,isCopy,custvendorid,this.currentAddressDetailrec,this.isCustomer,this.viewGoodReceipt,this.isViewTemplate,this.singleLink); 
        Wtf.getCmp('addressDetailWindow').on('update',function(config){
                this.currentAddressDetailrec=config.currentaddress;
        },this);
    },
    
    getCostAndMarginWindow: function() {
        var parentObj = this;
        callCostAndMarginWindow(this.Grid.getStore(), this.productComboStore, this.getExchangeRate(), parentObj);
    },
    
    calProfitMargin : function(){
        var sellingPrice=0;
        var productCost=0;
        var productProfitMarginPer=0;
        var serviceProfitMarginPer=0;
        var totalSellingPrice=0;
        var totalCost=0;
        var totalProfitMarginPer=0;
        
        this.totalproductsellingprice = 0;
        this.totalproductcost = 0;
        this.totalproductprofitmargin = 0;
        this.totalprodcutprofitmarginpercent = 0;

        this.totalservicesellingprice = 0;
        this.totalservicecost = 0;
        this.totalserviceprofitmargin = 0;
        this.totalserviceprofitmarginpercent = 0;

        this.finalproductsellingprice = 0;
        this.finalproductcost = 0;
        this.finalproductprofitmargin = 0;
        this.finalproductprofitmarginpercent = 0;
        
        var count=this.Grid.store.getCount();
        for(var i=0;i<count;i++){
            sellingPrice=getRoundedAmountValue(parseFloat(this.Grid.store.getAt(i).data['amount']));
            productCost=getRoundedAmountValue(parseFloat(this.Grid.store.getAt(i).data['totalcost']));
            var prodRec = null;
            var productComboRecIndex=WtfGlobal.searchRecordIndex(this.productComboStore, this.Grid.store.getAt(i).data.productid, 'productid');
            if(productComboRecIndex >=0){
                prodRec = this.productComboStore.getAt(productComboRecIndex);
            }
            if(prodRec!=null && prodRec.data.type!='Service'){
                this.totalproductsellingprice+=this.calAmountInBase(sellingPrice);
                this.totalproductcost+=productCost;
            }else{
                this.totalservicesellingprice+=this.calAmountInBase(sellingPrice);
                this.totalservicecost+=productCost;
            }
        }
        this.totalproductsellingprice = getRoundedAmountValue(this.totalproductsellingprice);
        this.totalservicesellingprice = getRoundedAmountValue(this.totalservicesellingprice);
        
        this.totalproductcost = getRoundedAmountValue(this.totalproductcost);
        this.totalservicecost = getRoundedAmountValue(this.totalservicecost);
        
        this.totalproductprofitmargin = getRoundedAmountValue((this.totalproductsellingprice - this.totalproductcost));
        this.totalserviceprofitmargin = getRoundedAmountValue((this.totalservicesellingprice - this.totalservicecost));
        
        productProfitMarginPer = this.totalproductcost==0 ? (this.totalproductprofitmargin!=0 ? 100 : 0) : (this.totalproductprofitmargin/this.totalproductsellingprice)*100;
        this.totalprodcutprofitmarginpercent = getRoundedAmountValue(productProfitMarginPer);
        
        serviceProfitMarginPer = this.totalservicecost==0 ? (this.totalserviceprofitmargin!=0 ? 100 : 0) : (this.totalserviceprofitmargin/this.totalservicesellingprice)*100;
        this.totalserviceprofitmarginpercent = getRoundedAmountValue(serviceProfitMarginPer);
        
        totalSellingPrice = this.totalproductsellingprice + this.totalservicesellingprice;
        this.finalproductsellingprice = getRoundedAmountValue(totalSellingPrice);
        
        totalCost = this.totalproductcost + this.totalservicecost;
        this.finalproductcost = getRoundedAmountValue(totalCost);
        
        this.finalproductprofitmargin = getRoundedAmountValue((this.finalproductsellingprice - this.finalproductcost));
        
        totalProfitMarginPer = this.finalproductcost==0 ? (this.finalproductprofitmargin!=0 ? 100 : 0) : (this.finalproductprofitmargin/this.finalproductsellingprice)*100;
        this.finalproductprofitmarginpercent = getRoundedAmountValue(totalProfitMarginPer);
    },
    
    addMoreOptions:function(){
        var recordIndex = this.PO.store.findBy(
            function (record, id) {
                if (record.get('billid') === '-1') {
                    return true;  // a record with this data exists
                }
                return false;  // there is no record in the store with this data
            }
            );
        if (recordIndex == -1 && this.PO.store.getCount()) {
            this.PO.store.insert(this.PO.store.getCount(), new this.PORec({
                billno: "<a href=#>More</a>",
                billid: '-1'
            })); // created record for "More"
        }  
    },
    
    showPONumbersGrid: function (url) {
        this.PONumberSelectionWin = new Wtf.account.PONumberSelectionWindow({
            renderTo: document.body,
            height: 500,
            id: this.id + 'PONumbersSelectionWindowDO',
            width: 600,
            title: 'Document Selection Window',
            layout: 'fit',
            modal: true,
            resizable: false,
            url: url,
            columnHeader:this.fromLinkCombo.getRawValue(),
            moduleid: this.moduleid,
            singleSelect: (this.moduleid===Wtf.Acc_Customer_Quotation_ModuleId)?true:false,
            invoice: this,
            storeBaseParams: this.POStore.baseParams,
            storeParams:this.POStore.lastOptions.params,
            PORec: this.PORec
        });
        this.PONumberSelectionWin.show();
    },
    
    handleGridStoreLoadEventOnEdit: function() {
        if (this.Grid.getStore().data.items.length > 0) {
            this.linkIDSFlag = false;
            var linkType = -1;
            var storeData = [], linkNumbers = [], linkIDS = [];
            this.POStore.removeAll();
            this.Grid.getStore().each(function(rec) {
                if (!this.copyInv) {
                    if ((rec.data.linkto !="" && rec.data.linkto != undefined) && (rec.data.linktype != -1 && rec.data.linktype != undefined)) {
                        var isExistFlag = false;
                        for (var count = 0; count < linkNumbers.length; count++) {
                            if (rec.data.linkto == linkNumbers[count]) {
                                isExistFlag = true;
                                break;
                            }
                        }
                        if(isExistFlag == false) {
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
            
        }
        this.Grid.getStore().un("load", this.handleGridStoreLoadEventOnEdit, this);
    },
    
    onValidTillDateChange :function(a,val,oldval){
        if(this.billDate && this.validTillDate){
            if(this.validTillDate.getValue()<this.billDate.getValue()){
                this.validTillDate.setValue(oldval);
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.ValidTillDateShouldNotLessThanQuotationDate")], 2);
            }
        }
    },
    
    SetLocationwarehouseWindow: function(){
        this.recArr = this.Grid.getSelectionModel().getSelections();
        var quantityNonSetProductCount=0;
        var productwithLocationWarehouse=0;
        for(var k=0;k< this.recArr.length;k++){
           var proRecord=this.recArr[k];
           if(((proRecord.data.isLocationForProduct ) || (proRecord.data.isWarehouseForProduct )) && !proRecord.data.isSerialForProduct && !proRecord.data.isBatchForProduct){
                productwithLocationWarehouse++;
                if(proRecord.data.quantity ==""){
                    quantityNonSetProductCount++;
                }
            }
        }
        
        if(productwithLocationWarehouse ==0){
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.window.noRecordwithWarhouselocation")],2);
            return false;
        }else if (quantityNonSetProductCount > 0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.grid.noquanitysetforProduct")],2);
            return false;
        }else{
            this.SetLocationwarehouseWindow = new Wtf.account.SetLocationwarehouseWindow({
                id: 'setLocationwarehouseWindow'+this.id,
                title: WtfGlobal.getLocaleText("acc.SetWarehouseLocation"),
                border: false,
                isCustomer:this.isCustomer,
                grid:this.Grid,
                scope:this,
                closable: true,
                modal: true,
                iconCls :getButtonIconCls(Wtf.etype.deskera),
                resizable: false,
                renderTo: document.body
            });
            this.SetLocationwarehouseWindow.show(); 
        }
     }
});

var quotationPanel = new Wtf.account.QuotationPanel({
    id: 'quotationPanel'+this.mainPanel.activeTab.id,
    layout: 'fit',
    title: this.mainPanel.activeTab.title,
    closable: this.mainPanel.activeTab.closable,
    quotation: this.mainPanel.activeTab.quotation,
    isCustomer: this.mainPanel.activeTab.isCustomer,
    isOrder: this.mainPanel.activeTab.isOrder,
    isEdit: this.mainPanel.activeTab.isEdit,
    ispurchaseReq: this.mainPanel.activeTab.ispurchaseReq,
    copyInv: this.mainPanel.activeTab.copyInv,
    record: this.mainPanel.activeTab.record,
    PR_IDS: this.mainPanel.activeTab.PR_IDS,
    isQuotationFromPR: this.mainPanel.activeTab.isQuotationFromPR,
    label: this.mainPanel.activeTab.label,
    border: this.mainPanel.activeTab.border,
    isVersion: this.mainPanel.activeTab.isVersion,
    tabTip: this.mainPanel.activeTab.tabTip,
    heplmodeid: this.mainPanel.activeTab.heplmodeid,
    moduleid: this.mainPanel.activeTab.moduleid,
    iconCls: this.mainPanel.activeTab.iconCls,
    modeName: this.mainPanel.activeTab.modeName
}); 

Wtf.getCmp(this.mainPanel.activeTab.id).add(quotationPanel);
Wtf.getCmp(this.mainPanel.activeTab.id).doLayout();