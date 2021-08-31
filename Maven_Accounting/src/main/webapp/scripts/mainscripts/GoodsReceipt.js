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


// TODO: This can be common code for different modules. If so need to be moved to Common file WtfDocumentMain.js
/**
 *Moved this function to wtfmain-ex.js
 */
//function editInvoiceExchangeRates(winid,basecurrency,foreigncurrency,exchangerate,exchangeratetype){
//    function showInvoiceExternalExchangeRate(btn,txt){
//        if(btn == 'ok'){
//             if(txt.indexOf('.')!=-1)
//                 var decLength=(txt.substring(txt.indexOf('.'),txt.length-1)).length;
//            if(isNaN(txt)||txt.length>15||decLength>7||txt==0){
//                Wtf.MessageBox.show({
//                    title: WtfGlobal.getLocaleText("acc.setupWizard.curEx"), //'Exchange Rate',
//                    msg: WtfGlobal.getLocaleText("acc.nee.55")+
//                    "<br>"+WtfGlobal.getLocaleText("acc.nee.56")+
//                    "<br>"+WtfGlobal.getLocaleText("acc.nee.57"),
//                    buttons: Wtf.MessageBox.OK,
//                    icon: Wtf.MessageBox.WARNING,
////                    width: 300,
//                    scope: this,
//                    fn: function(){
//                        if(btn=="ok"){
//                            editInvoiceExchangeRates(winid,basecurrency,foreigncurrency,exchangerate,exchangeratetype);
//                        }
//                    }
//                });
//            }else{
//                if(exchangeratetype!=undefined)
//                    Wtf.getCmp(winid).exchangeratetype=exchangeratetype
//                if(exchangeratetype!=undefined&&exchangeratetype=='foreigntobase'){
//                    if((txt*1)>0) {
//                        Wtf.getCmp(winid).revexternalcurrencyrate=txt;
//                        var exchangeRateNormal = 1/((txt*1)-0);
//                        exchangeRateNormal = (Math.round(exchangeRateNormal*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
//                        Wtf.getCmp(winid).externalcurrencyrate=exchangeRateNormal;
//                    } 
//                }else{
//                    Wtf.getCmp(winid).externalcurrencyrate=txt;
//                }
//                Wtf.getCmp(winid).updateFormCurrency();
//            }
//        }
//    }
//    Wtf.MessageBox.prompt(WtfGlobal.getLocaleText("acc.setupWizard.curEx"),'<b>'+WtfGlobal.getLocaleText("acc.nee.58")+'</b> 1 '+basecurrency+' = '+exchangerate+' '+foreigncurrency +
//        '<br><b>'+WtfGlobal.getLocaleText("acc.nee.59")+'</b>', showInvoiceExternalExchangeRate);
//}

Wtf.account.GoodsReceiptPanel=function(config){
    Wtf.apply(this, config);
     
    //Initialize values to some of the common variables.
    this.initCommonValues(config);//In WtfDocumentMain.js
    
    //Initialize values to some of the module specific variables.
    this.initValues(config);
    
    Wtf.account.GoodsReceiptPanel.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.GoodsReceiptPanel,Wtf.account.MainClosablePanel,{
    initComponent:function(config){
        Wtf.account.GoodsReceiptPanel.superclass.initComponent.call(this,config);
        
//        //Initialize values to some of the variables.
//        this.initValues();
        
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
        
        //Create module specific buttons 
        this.createButtons();
        
        //Create global level custom or dimension fields 
        this.tagsFieldset = this.createCustOrDimFields(this.readOnly, this.isEdit);//In WtfDocumentMain.js
        
        //Append north form fields in arrays
        this.appendNorthFormFields();
        /*Create north form panel. Parameters:
         *1. labelwidth
         *2. leftcolumnwidth, as column layout is used
         *3. rightcolumnwidth, as column layout is used
         **/
        var labelwidth = 155;
        var leftcolumnwidth = 0.65;
        var rightcolumnwidth = 0.35;
        this.createNorthForm(labelwidth, leftcolumnwidth, rightcolumnwidth);//In WtfDocumentMain.js
        
        //Create product grid to capture product details
        this.createProductGrid();
        //Add event to product grid
        this.addProductGridEvents();
        
        //Create components for south panel
        this.createSouthPanelFields();
        //Append south panel fields in arrays
        this.appendSouthPanelFields();
        //Create south panel by adding components to it
        this.createSouthPanel(this.readOnly, (Wtf.isIE?350:290));//In WtfDocumentMain.js
        
        //Set transaction no.
        this.setTransactionNumber();        
    },
    
    onRender:function(config){
        this.add(this.NorthForm,this.Grid,this.southPanel);                       
        Wtf.account.GoodsReceiptPanel.superclass.onRender.call(this, config);
        
//        //Load all stores
//        this.loadInitialStore();
        
        //Initiallize for close
        this.initForClose();   
        
        //Update field configs as per add, edit & view.
        this.updateFieldConfigs();
        
        // hide form fields
        this.hideFormFields();
    },
    
    loadRecord:function(){
        if(this.record!=null&&!this.dataLoaded){
            this.fromLinkCombo.disable();
            this.PO.disable();  
            var data=this.record.data;
            this.externalcurrencyrate=this.record.data.externalcurrencyrate;
            this.NorthForm.getForm().loadRecord(this.record);
            this.currencyStore.on('load',function () {
                this.Currency.setValue(data.currencyid);
                this.updateFormCurrency();
            },this);
                    
            if(this.copyInv){//iF COPY AND SEQUENCE FORMAT IS NA
                this.Number.setValue("");                
            }else{
                this.Number.setValue(data.billno);
            }
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
            
            if(this.Grid.getStore().data.items.length>0){  // for showing multiple link numbers in number field 
                var linkType=-1;
                var storeData = [],linkNumbers=[],linkIDS=[];
                this.POStore.removeAll();
                this.Grid.getStore().each(function(rec){
                    if(this.copyInv) { 
                        rec.data.linkid=""; 
                        rec.data.rowid=""; 
                        rec.data.linktype="";
                        rec.data.linkto="";
                        rec.data.batchdetails=""; //in copy case batchdetails made empty
                    } else {
                        if((rec.data.linkto!=""&&rec.data.linkto!=undefined) && (rec.data.linktype!=-1 && rec.data.linktype!=undefined)){
                            var isExistFlag=false;
                            for(var count=0;count<linkNumbers.length;count++){
                                if(rec.data.linkto==linkNumbers[count]){
                                    isExistFlag=true;
                                    break;
                                }
                            }
                            if(isExistFlag==false){
                                linkNumbers.push(rec.data.linkto);
                                linkIDS.push(rec.data.linkid);
                            }                                                        
                            linkType=rec.data.linktype;                            
                            var newRec=new this.PORec({
                                billid:rec.data.linkid,
                                billno:rec.data.linkto    
                            });
                            storeData.push(newRec);
                        }
                    }
                },this);
                if(storeData.length>0){
                    this.POStore.add(storeData);
                }
                if(linkIDS.length>0){
                    this.Name.disable();
                    this.fromPO.disable();
                    this.fromLinkCombo.disable();
                    this.PO.disable();
                    this.fromPO.setValue(true);                
                    this.PO.setValue(linkIDS);
                }
                if(linkType!=-1){
                    this.fromLinkCombo.setValue(linkType);
                }
            }            
            if((this.copyInv || this.isEdit)){
                this.isCustomer ? Wtf.salesPersonFilteredByCustomer.load({
                    params:{ //sending a customerid to fliter available masteritems for selected customer 
                        customerid: this.record.data.personid            
                    }
                }) : Wtf.agentStore.load(); 
            }
            this.Memo.setValue(data.memo);
            this.billDate.setValue(data.date);                                                            
            this.CostCenter.setValue(data.costcenterid);
            this.editedBy.setValue(data.lasteditedby);
            this.postText = this.record.json.posttext;
            this.DOStatusCombo.setValue(data.statusID)
            if (!Wtf.account.companyAccountPref.deliveryPlanner && this.moduleid == Wtf.Acc_Delivery_Order_ModuleId) {
                this.driverStore.on('load', function() {
                    this.driverNo.setValue(data.driver);
                }, this);
                this.driverStore.load();
            }
            if (data.includeprotax) {
                this.includeProTax.setValue(true);
                if(Wtf.account.companyAccountPref.unitPriceConfiguration){
                    this.showGridTax(null, null, false);
                }
                this.isTaxable.setValue(false);//when selecting record with product tax.Tax should get disabled.
                this.isTaxable.disable();
                this.Tax.setValue("");
                this.Tax.disable();
            } else {
                this.includeProTax.setValue(false);
                if(Wtf.account.companyAccountPref.unitPriceConfiguration) {
                    this.showGridTax(null, null, true);
                }
                this.Tax.enable();//required because when selected multiple records & changing to select single record.Before it was getting disabled.
                this.isTaxable.enable();
            }
            if ((data.taxid == "")) {//generate so or po it should not show taxid
                this.isTaxable.setValue(false);
                this.Tax.setValue("");
                this.Tax.disable();
            }else if(!data.includeprotax){
                this.Tax.setValue(data.taxid);
                 this.isTaxable.enable();
                this.Tax.enable();//enable the tax when taxid is present-for edit case it was not required but for copy its is required.
                this.isTaxable.setValue(true);
            }
            this.dataLoaded=true;
            if(this.Grid){
                this.Grid.forCurrency =data.currencyid;
                this.Grid.affecteduser=data.personid;
                this.Grid.billDate=data.date;
            }
            if(this.copyInv && this.Grid){
                this.Grid.billDate=Wtf.serverDate;
            }
//            if(this.copyInv){
//                this.billDate.setValue(Wtf.serverDate);
//            }
            if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){     
                this.Grid.productComboStore.load({
                    params:{
                        mappingProduct:true,
                        customerid:this.Name.getValue(),
                        common:'1', 
                        loadPrice:true,
                        mode:54
                    }
                }) ;           
            }
            this.updateSubtotal();
        }
    },
    
    initValues: function(){
        this.id=config.id;
        this.isEdit=config.isEdit;
        this.label=config.label;
        this.copyInv = config.copyInv;
        this.heplmodeid = config.heplmodeid;
        this.exchangeRateInRetainCase=false;
        this.record=config.record;
        this.modeName = config.modeName;
        this.moduleid=config.moduleid;
        this.readOnly=config.readOnly;
        this.uPermType=(config.isCustomer?Wtf.UPerm.deliveryreport:Wtf.UPerm.goodsreceiptreport);
        this.permType=(config.isCustomer?Wtf.Perm.deliveryreport:Wtf.Perm.goodsreceiptreport);
        this.isOrder=config.isOrder;
        this.printPermType=this.isConsignment?true:(config.isFixedAsset?(config.isCustomer?this.permType.printfado:this.permType.printfagr):(config.isCustomer?(this.isOrder?this.permType.printdo:false):(this.isOrder?this.permType.printgr:false)));
        this.exportPermType=(config.isCustomer?this.permType.exportdatado:this.permType.exportdatagr); 
        (this.businessPerson == "Customer")? Wtf.DOStatusStore.load() : Wtf.GROStatusStore.load();
        Wtf.apply(this, config);
        if(config.moduleid==28||config.moduleid==27){
            if(config.moduleid==27){
                this.tranType=Wtf.autoNum.DeliveryOrder;
            }else{
                this.tranType=Wtf.autoNum.GoodsReceiptOrder;
            }   
        }
        this.loadCurrFlag = true;
        this.POSelected="";
    },
    
    createStores: function(){
        this.sequenceFormatStoreRec = new Wtf.data.Record.create([
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
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:this.modeName,
                isEdit: this.copyInv ? false : this.isEdit
            }
        });
        
        this.currencyRec = new Wtf.data.Record.create([
        {name: 'currencyid',mapping:'tocurrencyid'},
        {name: 'symbol'},
        {name: 'currencyname',mapping:'tocurrency'},
        {name: 'exchangerate'},
        {name: 'htmlcode'}
        ]);
        this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
            url:"ACCCurrency/getCurrencyExchange.do"
        });
         
        this.fromPOStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Yes',true],['No',false]]
        });
        
        var fromLinkStoreRec = new Array();
        if(this.isCustomer){
            fromLinkStoreRec.push(['Sales Order','0']);
            fromLinkStoreRec.push(['Sales Invoice','1']);
        } else {
            fromLinkStoreRec.push(['Purchase Order','0']);
            fromLinkStoreRec.push(['Purchase Invoice','1']);
        }
        this.fromlinkStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value'}],
            data:fromLinkStoreRec
        });
        
        this.PORec = Wtf.data.Record.create ([
            {name:'billid'},
            {name:'journalentryid'},
            {name:'entryno'},
            {name:'billto'},
            {name:'discount'},
            {name:'shipto'},
            {name:'mode'},
            {name:'billno'},
            {name:'date', type:'date'},
            {name:'duedate', type:'date'},
            {name:'shipdate', type:'date'},
            {name:'personname'},
            {name:'creditoraccount'},
            {name:'personid'},
            {name:'shipping'},
            {name:'othercharges'},
            {name:'taxid'},
            {name:'currencyid'},
            {name:'amount'},
            {name:'amountinbase'},
            {name:'shipvia'},
            {name:'fob'},
            {name:'permitNumber'},
            {name:'amountdue'},
            {name:'contractstatus'},
            {name:'contract'},
            {name:'costcenterid'},
            {name:'costcenterName'},
            {name:'externalcurrencyrate'},
            {name:'memo'},
            {name:'posttext'},
            {name:'salesPerson'},
            {name:'agent'},
            {name: 'billingAddressType'},
            {name: 'billingAddress'},
            {name: 'billingCountry'},
            {name: 'billingState'},
            {name: 'billingPostal'},
            {name: 'billingEmail'},
            {name: 'billingFax'},
            {name: 'billingMobile'},
            {name: 'billingPhone'},
            {name: 'billingContactPerson'},
            {name: 'billingRecipientName'},
            {name: 'billingContactPersonNumber'},
            {name: 'billingContactPersonDesignation'},
            {name: 'billingWebsite'},
            {name: 'billingCounty'},
            {name: 'billingCity'},
            {name: 'shippingAddressType'},
            {name: 'shippingAddress'},
            {name: 'shippingCountry'},
            {name: 'shippingState'},
            {name: 'shippingCounty'},
            {name: 'shippingCity'},
            {name: 'shippingEmail'},
            {name: 'shippingFax'},
            {name: 'shippingMobile'},
            {name: 'shippingPhone'},
            {name: 'shippingPostal'},
            {name: 'shippingContactPersonNumber'},
            {name: 'shippingContactPersonDesignation'},
            {name: 'shippingWebsite'},
            {name: 'shippingRecipientName'},
            {name: 'shippingContactPerson'},
            {name: 'shippingRoute'},
            {name: 'taxid'} , 
            {name: 'includeprotax'},
            {name: 'vendcustShippingAddress'},
            {name: 'vendcustShippingCountry'},
            {name: 'vendcustShippingState'},
            {name: 'vendcustShippingCounty'},
            {name: 'vendcustShippingCity'},
            {name: 'vendcustShippingEmail'},
            {name: 'vendcustShippingFax'},
            {name: 'vendcustShippingMobile'},
            {name: 'vendcustShippingPhone'},
            {name: 'vendcustShippingPostal'},
            {name: 'vendcustShippingContactPersonNumber'},
            {name: 'vendcustShippingContactPersonDesignation'},
            {name: 'vendcustShippingWebsite'},
            {name: 'vendcustShippingContactPerson'},
            {name: 'vendcustShippingRecipientName'},
            {name: 'vendcustShippingAddressType'}
        ]);
        this.POStoreUrl = "";
        if(this.businessPerson=="Customer"){
            this.POStoreUrl = "ACCSalesOrderCMN/getSalesOrders.do";
        }else if(this.businessPerson=="Vendor"){
            this.POStoreUrl = "ACCPurchaseOrderCMN/getPurchaseOrders.do";
        }
        this.POStore = new Wtf.data.Store({
            url:this.POStoreUrl,
            baseParams:{
                mode:42,
                closeflag:true,
                doflag : true,
                requestModuleid:this.moduleid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.PORec)
        });
        
        this.driverRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        this.driverStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.driverRec),
            url: "ACCMaster/getMasterItems.do",
            baseParams: {
                mode:112,
                groupid: 26
            }
        });
    },
    
    addStoreEvents: function(){
        var isEditORisCopy=(this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv !=undefined ?this.copyInv:false);// Load All Customers in Edit and Copy case
        Wtf.customerAccStore.on('beforeload', function(s,o){
            if(!o.params)o.params={};
            var currentBaseParams = Wtf.customerAccStore.baseParams;
            if(isEditORisCopy){
                currentBaseParams.isPermOrOnetime=""; // Empty to Load all Customers.
            }else{
                if(this.ShowOnlyOneTime != undefined && this.ShowOnlyOneTime.getValue() == true){
                    currentBaseParams.isPermOrOnetime=true; // True to Load One Time Customers.
                }else{
                    currentBaseParams.isPermOrOnetime=false; // False to Load Permanent Customers
                }
            }
            Wtf.customerAccStore.baseParams=currentBaseParams;
        }, this);
        
        this.sequenceFormatStore.on('load',this.setNextNumber,this);
        
        this.currencyStore.on('load',this.changeTemplateSymbol,this);
    },
    
    createFields: function(){
        var isShowOneTime=(this.moduleid == Wtf.Acc_Delivery_Order_ModuleId) && !((this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv !=undefined ?this.copyInv:false));
        this.ShowOnlyOneTime= new Wtf.form.Checkbox({
            name:'ShowOnlyOneTime',
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.ShowOnlyOneTime"),
            id:'ShowOnlyOneTime'+this.heplmodeid+this.id,
            checked:false,
            hideLabel:!isShowOneTime, // Show only in new case
            hidden:!isShowOneTime,
            cls : 'custcheckbox',
            width: 10
        });

        this.NameConfig = {
            fieldLabel:(this.isCustomer)?"<span wtf:qtip='"+  WtfGlobal.getLocaleText("acc.invoiceList.cust.tt") +"'>"+ WtfGlobal.getLocaleText("acc.invoiceList.cust") +"</span>":"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.invoiceList.ven.tt") +"'>"+ WtfGlobal.getLocaleText("acc.invoiceList.ven") +"</span>",
            hiddenName:this.businessPerson.toLowerCase(),
            id:"customer"+this.heplmodeid+this.id,
            allowBlank:false,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
            hirarchical:true,
            emptyText:this.isCustomer?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven") , //'Select a '+this.businessPerson+'...',
            mode: 'local',
            minChars:1,
            listeners:{
                'select':{
                    fn:function(obj,rec,index){
                        this.singleLink = false;
                        if (this.isEdit || this.isCopy) {
                            this.isVenOrCustSelect = true;
                        }
                        this.currentAddressDetailrec="";//If customer/vendor change in this case,previously stored addresses in this.currentAddressDetailrec will be clear    
                        var customer= this.Name.getValue();
                        if(rec.data.currencyid!=this.Currency.getValue()){//update currency field with vendor currency if vendor currency is different
                            this.Currency.setValue(rec.data.currencyid);
                            this.currencychanged = true;
                            this.updateFormCurrency();   
                        }
                        this.isClosable=false          // Set Closable flag after selecting Customer/Vendor
                        this.fromLinkCombo.clearValue();
                        this.PO.clearValue();
                        this.CostCenter.clearValue();
                        if(!this.isEdit && !this.copyInv){
                            this.Grid.getStore().removeAll();
                            this.Grid.addBlankRow();
                        }    
                        this.showAddrress.enable();
                        this.fromLinkCombo.disable();
                        this.PO.disable();
                        this.PO.reset();
                        this.fromPO.setValue(false);
                        this.Memo.setValue('');
                        if(!this.record){
                            this.getPostTextToSetPostText();
                        }else{
                            this.postText=this.record.data.posttext;
                        }
                        this.shipDate.setValue('');
                        this.shipvia.setValue('');
                        this.fob.setValue('');                                                  
                        this.permitNumber.setValue('');                                                
                        this.CostCenter.setValue('');
                        this.Name.setValue(customer);
                        if(this.Grid){
                            this.Grid.affecteduser=this.Name.getValue();
                        }
                        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{productname:"&nbsp;&nbsp;&nbsp;&nbsp;",qty:0,soqty:0,poqty:0});
                        if(this.fromPO)
                            this.fromPO.enable();   
                        if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){     
                            this.Grid.productComboStore.load({params:{mappingProduct:true,customerid:this.Name.getValue(),common:'1', loadPrice:true,mode:54}}) ;           
                        } 
                        this.tagsFieldset.resetCustomComponents();
                        var moduleid = this.isCustomer ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId;
                        this.tagsFieldset.setValuesForCustomer(moduleid, customer);
                    },
                scope:this                    
                }
            }
        };
        this.Name = WtfGlobal.createExtFnCombobox(this.NameConfig, (this.isCustomer? Wtf.customerAccStore:Wtf.vendorAccStore), 'accid', 'accname', this);
        this.Name.addNewFn=this.addPerson.createDelegate(this,[false,null,this.businessPerson+"window",this.isCustomer],true);

        this.CurrencyConfig={
            fieldLabel: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.currency.tt")+"'>"+ WtfGlobal.getLocaleText("acc.currency.cur") +"</span>",
            hiddenName:'currencyid',
            id:"currency"+this.heplmodeid+this.id,
            allowBlank : false
        };
        this.Currency = WtfGlobal.createFnCombobox(this.CurrencyConfig, this.currencyStore, 'currencyid', 'currencyname', this);

        this.DOStatusComboConfig={
            fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.status"),
            name:"statuscombo",     
            id:'statuscomboId'+this.heplmodeid+this.id,
            anchor:"94%",
            allowBlank:true,
            mode: 'local',
            addNoneRecord: true
        };
        this.DOStatusCombo = WtfGlobal.createFnCombobox(this.DOStatusComboConfig, (this.businessPerson == "Customer"?Wtf.DOStatusStore:Wtf.GROStatusStore), 'id', 'name', this);
        this.DOStatusCombo.addNewFn=this.addDOStatus.createDelegate(this);

        this.sequenceFormatComboboxConfig = {
            mode: 'local',
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.Sequenceformat.tip")+"'>"+ WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat")+"</span>",
            id:'sequenceFormatCombobox'+this.heplmodeid+this.id,
            disabled:(this.isEdit&&!this.copyInv&&!this.isPOfromSO&&!this.isSOfromPO?true:false),  
            width:240,
            name:'sequenceformat',
            hiddenName:'sequenceformat',
            allowBlank:false,
            listeners:{
                'select':{
                    fn:this.getNextSequenceNumber,
                    scope:this
                }
            }
        };
        this.sequenceFormatCombobox = WtfGlobal.createCombobox(this.sequenceFormatComboboxConfig, this.sequenceFormatStore, 'id', 'value', this);
        
        this.NumberConfig = {
            fieldLabel:(this.isEdit?this.label:this.titlel) + " " + WtfGlobal.getLocaleText("acc.common.number"),  //,  //this.label+' Number*',
            name: 'number',
            id:"invoiceNo"+this.heplmodeid+this.id
        };
        this.Number = WtfGlobal.createTextfield(this.NumberConfig, (this.isEdit&&!this.copyInv?true:false), this.checkin, 50, this);
        
        this.includeTaxStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Yes',true],['No',false]]
        });
        this.includeProTax= new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            displayField:'name',
            store:this.includeTaxStore,
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.includeprodtax.tip")+"'>"+WtfGlobal.getLocaleText("acc.invoice.productTax") +"</span>",//"Include Product Tax",
            id:"includeprotax"+this.heplmodeid+this.id,
            value:(this.isEdit?true:false),
            width : 240,
            typeAhead: true,
            forceSelection: true,
            hideLabel:!Wtf.account.companyAccountPref.unitPriceConfiguration || (Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// hide if company is malaysian and GST is not enabled for it
            hidden:!Wtf.account.companyAccountPref.unitPriceConfiguration || (Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// hide if company is malaysian and GST is not enabled for it
            name:'includeprotax',
            hiddenName:'includeprotax',
            listeners:{
                'change':{
                    fn:this.includeProTaxHandler,
                    scope:this
                }
            }
        });
        
        this.Memo=new Wtf.form.TextArea({
            fieldLabel:Wtf.account.companyAccountPref.descriptionType,  //'Memo',
            name: 'memo',
            id:"memo"+this.heplmodeid+this.id,
            height:40,
            anchor:'94%',
            maxLength:2048,        
            readOnly:this.readOnly,
            qtip:(this.record==undefined)?' ':this.record.data.memo,
            listeners: {
                render: function(c){
                    Wtf.QuickTips.register({
                        target: c.getEl(),
                        text: c.qtip
                    });
                }
            }
        });

        this.fromPOConfig = {
            hideLabel:false,
            hidden:false,
            mode: 'local',
            disabled:this.isEdit?false:true,
            id: "linkToOrder"+this.heplmodeid+this.id,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Link"),
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

        this.fromLinkComboConfig = {
            name:"fromLinkCombo",
            hideLabel:false,
            hidden:false,
            mode: 'local',
            disabled:true,
            emptyText: this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectaSO/SI") : WtfGlobal.getLocaleText("acc.field.SelectaPO/PI"),
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Linkto"),  //"Link to "+(this.isCustomer?"Sales":"Purchase")+" Order",
            allowBlank:false,     
            id:'fromLinkComboId'+this.heplmodeid+this.id,
            width:135,
            listeners:{
                'select':{
                    fn:this.enableNumber,
                    scope:this
                }
            }
        };
        this.fromLinkCombo = WtfGlobal.createCombobox(this.fromLinkComboConfig, this.fromlinkStore, 'value', 'name', this);
        
        this.MSComboconfig = {  //multiselect combo
            hiddenName:"ordernumber",
            allowBlank:false, 
            store: this.POStore,
            valueField:'billid',
            hideLabel:false,
            hidden:false,
            displayField:'billno',
            disabled:true,
            clearTrigger:this.isEdit ? false : true,
            emptyText: this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectaSO/SI") : WtfGlobal.getLocaleText("acc.field.SelectaPO/PI"),
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,                        
            triggerAction:'all',
            scope:this
        };
        this.PO = new Wtf.common.Select(Wtf.applyIf({
             multiSelect:true,
             fieldLabel:WtfGlobal.getLocaleText("acc.field.Number") ,
             id:"poNumberID"+this.heplmodeid+this.id,
             forceSelection:true,
             hideTrigger1:true,
             width:240
        },this.MSComboconfig));
        this.PO.addNewFn=this.addOrder.createDelegate(this,[false,null,this.businessPerson+"PO"],true);

        this.billDateConfig = {
            fieldLabel:(this.isEdit?this.label:this.titlel) +' '+WtfGlobal.getLocaleText("acc.invoice.date"),
            id:"invoiceDate"+this.heplmodeid+this.id,
            name: 'billdate'
        };
        this.billDate = WtfGlobal.createDatefield(this.billDateConfig, false, this);

        this.shipDateConfig = {
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.ShipDate.tip")+"'>"+ WtfGlobal.getLocaleText("acc.field.ShipDate")+"</span>",
            id:"shipdate"+this.heplmodeid+this.id,
            name: 'shipdate',
            anchor:'94%'
        };
        this.shipDate = WtfGlobal.createDatefield(this.shipDateConfig, true, this);

        this.shipviaConfig = {
            fieldLabel: WtfGlobal.getLocaleText("acc.field.ShipVia"),
            name: 'shipvia',
            id:"shipvia"+this.heplmodeid+this.id,
            anchor:'94%'
        };
        this.shipvia = WtfGlobal.createTextfield(this.shipviaConfig, false, true, 255, this);

        this.fobConfig = {
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.fob.tip")+"'>"+WtfGlobal.getLocaleText("acc.field.FOB") +"</span>",
            name: 'fob',
            id:"fob"+this.heplmodeid+this.id,
            anchor:'94%'
        };
        this.fob = WtfGlobal.createTextfield(this.fobConfig, false, true, 255, this);

        this.permitNumberConfig = {
            fieldLabel: WtfGlobal.getLocaleText("acc.field.PermitNumber"),
            name: 'permitNumber',
            id:'permitNumberId'+this.heplmodeid+this.id,
            anchor:'94%',
            hidden:true,
            hideLabel:true
        };
        this.permitNumber = WtfGlobal.createTextfield(this.permitNumberConfig, false, true, 255, this);

        this.CostCenterConfig = {
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.costCenter.tip") +"'>"+ WtfGlobal.getLocaleText("acc.common.costCenter")+"</span>",
            hiddenName:"costcenter",
            id:"costcenter"+this.heplmodeid+this.id,
            mode: 'local',
            typeAhead: true,
            anchor:'94%',          
            triggerAction:'all',
            addNewFn:this.addCostCenter,
            scope:this,
            hidden: this.quotation,
            hideLabel: this.quotation
        };
        this.CostCenter = WtfGlobal.createFnCombobox(this.CostCenterConfig, Wtf.FormCostCenterStore, 'id', 'name', this);

        this.editedByConfig = {
            fieldLabel:  WtfGlobal.getLocaleText("acc.field.LastEditedBy"),
            name: 'lasteditedby',
            id:"lasteditedby"+this.heplmodeid+this.id,
            hidden: this.isEdit?false:true,
            hideLabel:this.isEdit?false:true
        };
        this.editedBy = WtfGlobal.createTextfield(this.editedByConfig, true, true, 255, this);

        this.driverConfig = {
            fieldLabel: WtfGlobal.getLocaleText("acc.field.driver"), // "Driver",
            name: 'name',
            hiddenName: 'driver',
            mode: 'local',
            anchor:'94%',
            hidden: !(!Wtf.account.companyAccountPref.deliveryPlanner && this.moduleid == Wtf.Acc_Delivery_Order_ModuleId),
            hideLabel: !(!Wtf.account.companyAccountPref.deliveryPlanner && this.moduleid == Wtf.Acc_Delivery_Order_ModuleId)
        };
        this.driver = WtfGlobal.createCombobox(this.driverConfig, this.driverStore, 'id', 'name', this);

        this.usersConfig = {
            triggerAction:'all',
            mode: 'local',
            id:"salesperson"+this.heplmodeid+this.id,
            addNoneRecord: true,
            anchor:'94%',
            fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.masterConfig.15") : WtfGlobal.getLocaleText("acc.masterConfig.20"),
            emptyText: this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectSalesPerson") : WtfGlobal.getLocaleText("acc.field.SelectAgent"),
            name:this.isCustomer ? 'salesPerson' : 'agent',
            hiddenName:this.isCustomer ? 'salesPerson' : 'agent'
        };
        this.users = WtfGlobal.createFnCombobox(this.usersConfig, (this.isCustomer ? Wtf.salesPersonFilteredByCustomer : Wtf.agentStore), 'id', 'name', this);
        this.users.addNewFn=this.addSalesPerson.createDelegate(this);
        
        
        if (Wtf.account.companyAccountPref.enableLinkToSelWin && (this.moduleid === Wtf.Acc_Delivery_Order_ModuleId || this.moduleid === Wtf.Acc_Goods_Receipt_ModuleId )) {
            
            this.POStore.on('load',function(){addMoreOptions(this.PO,this.PORec)}, this);
            
            this.POStore.on('datachanged',function(){addMoreOptions(this.PO,this.PORec)}, this);
            
            this.PO.on("select", function () {
                var billid = this.PO.getValue();
                if (billid.indexOf("-1") != -1) {
                    var url = "";
                    if (this.fromLinkCombo.getValue() == 0 && this.isOrder) {
                        url = this.isCustomer ?  "ACCSalesOrderCMN/getSalesOrders.do" :"ACCPurchaseOrderCMN/getPurchaseOrders.do" ;
                    }else if(this.fromLinkCombo.getValue() == 1){
                        url = this.isCustomer ? "ACCInvoiceCMN/getInvoices.do" : "ACCGoodsReceiptCMN/getGoodsReceipts.do"              
                    }
                    this.PO.collapse();
                    this.PO.clearValue();
                    this.showPONumbersGrid(url);
                }
            }, this);
        }
        
        
        
        this.isTaxable= new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            displayField:'name',
            id:"includetax"+this.heplmodeid+this.id,
            store:this.fromPOStore,
            listWidth:50,
            fieldLabel:WtfGlobal.getLocaleText("acc.inv.totax"),  //"Include Total Tax",
            allowBlank:this.isOrder,
            value:false,
//            hideLabel:(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// hide if company is malaysian and GST is not enabled for it
            hidden:(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST)|| Wtf.account.companyAccountPref.isLineLevelTermFlag==1,// hide if company is malaysian and GST is not enabled for it
            width:50,
            typeAhead: true,
            forceSelection: true,
            name:'includetax',
            hiddenName:'includetax',
            listeners:{
                'select':{
                    fn:this.enabletax,
                    scope:this
                }
            }
        });
        
        this.Tax= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.Tax"),  //'Tax',
            id:"tax"+this.heplmodeid+this.id,
            disabled:!this.isEdit,
            hiddenName:'tax',
            anchor: '97%',
            store:this.Grid.taxStore,
            hideLabel:(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// hide if company is malaysian and GST is not enabled for it
            hidden:(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// hide if company is malaysian and GST is not enabled for it
            valueField:'prtaxid',
            forceSelection: true,
//            labelWidth:50,
            displayField:'prtaxname',
//            addNewFn:this.addTax.createDelegate(this),
            scope:this,
            listeners:{
                'select':{
                    fn:this.callGSTCurrencyRateandUpdateSubtotal,
                    scope:this
                }
            },
            selectOnFocus:true
        });
    },
    
    addFieldEvents: function(){
        this.ShowOnlyOneTime.on('check',function(obj,isChecked){
            this.Name.reset();
            Wtf.customerAccStore.load();
        },this);
        
        this.Currency.on('select', function(){
            this.externalcurrencyrate=0;
            this.currencychanged = true;
            this.onCurrencyChangeOnly();
            this.updateFormCurrency();
            if(this.Grid){
                this.Grid.forCurrency = this.Currency.getValue();
            }
        },this);
        
        this.PO.on("clearval",function(){
            if(this.PO.getValue()=="" && !this.isEdit && !this.handleEmptyText){            
                this.Grid.getStore().removeAll();            
                this.Grid.addBlankRow();            
            }
            this.handleEmptyText=false;
        },this);                
    
        this.Name.on('select',this.onNameSelect,this);
        this.Name.on('beforeselect',function(combo){this.nameBeforeSelect=combo.getValue();},this);
        
        this.billDate.on('change',this.onDateChange,this);
    },
    
    createButtons: function(){
        this.saveBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
            tooltip:WtfGlobal.getLocaleText("acc.rem.175"),
            id:"save"+this.heplmodeid+this.id,
            hidden:this.readOnly,
            scope:this,
            handler:function(){
                this.mailFlag = true;  
                this.saveOnlyFlag = true;
                this.disableSaveButtons()
                this.beforeSave();
            },
            iconCls :'pwnd save'
        });
        this.buttonArray.push(this.saveBttn);
        
        this.savencreateBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
            tooltip:WtfGlobal.getLocaleText("acc.field.SaveAndCreateNewToolTip"),
            id:"savencreate"+this.heplmodeid+this.id,
            scope:this,
            hidden : this.isEdit || this.copyInv,
            handler:function(){
                this.mailFlag = false;
                this.saveOnlyFlag = false;
                this.disableSaveButtons()
                this.save();
            },
            iconCls :'pwnd save'
        });
        this.buttonArray.push(this.savencreateBttn);
        
        this.buttonArray.push({
            text:WtfGlobal.getLocaleText("acc.common.email"),  // "Email",
            tooltip : WtfGlobal.getLocaleText("acc.common.emailTT"),  //"Email",
            id: "emailbut" + this.id,
            scope: this,
            hidden:this.readOnly,
            disabled : true,
            handler: function(){this.callEmailWindowFunction(this.response, this.request)},
            iconCls: "accountingbase financialreport"
        });
        
        if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){    
            var singlePDFtext = null;
            singlePDFtext = this.isCustomer?(this.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoSO"):WtfGlobal.getLocaleText("acc.accPref.autoInvoice")):(this.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoPO"):WtfGlobal.getLocaleText("acc.accPref.autoVI"));
            this.singlePrint=new Wtf.exportButton({
                obj:this,
                id:"exportpdf" + this.id,
                iconCls: 'pwnd exportpdfsingle',
                text:WtfGlobal.getLocaleText("acc.field.ExportPDF"),// + " "+ singlePDFtext,
                tooltip :WtfGlobal.getLocaleText("acc.rem.39.singletooltip"),  //'Export selected record(s)',
                disabled :true,
                isEntrylevel:true,
                exportRecord:this.exportRecord,
                hidden:this.isRequisition || this.isRFQ || this.isSalesCommissionStmt||this.readOnly,
                menuItem:{
                    rowPdf:(this.isSalesCommissionStmt)?false:true,
                    rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39") + " "+ singlePDFtext
                    },
                get:this.tranType,
                moduleid:this.moduleid  
            });
            this.buttonArray.push(this.singlePrint);
        }
        
        if (!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)) {
            this.singleRowPrint = new Wtf.exportButton({
                obj: this,
                id: "printSingleRecord"+ this.id,
                iconCls: 'pwnd printButtonIcon',
                text: WtfGlobal.getLocaleText("acc.rem.236"),
                tooltip: WtfGlobal.getLocaleText("acc.rem.236.single"), //'Print Single Record Details',
                disabled: this.readOnly?false:true,
                isEntrylevel: false,
                exportRecord:this.exportRecord,
                menuItem: {rowPrint: true},
                get: this.tranType,
                moduleid: this.moduleid
            });
            this.buttonArray.push(this.singleRowPrint);
        }
        
        this.buttonArray.push({
            text:  WtfGlobal.getLocaleText("acc.template.posttext") , //'<b>Post Text</b>',
            cls: 'pwnd add',
            id: "posttext" + this.id,              // Post Text
            hidden:this.readOnly,
            tooltip : WtfGlobal.getLocaleText("acc.field.UsePostTextoptiontoinserttextafterSignature"),       
            style:" padding-left: 15px;",
            scope: this,
            handler: function() {
                this.getPostTextEditor(this.postText);
            }   
        });
        
        this.showAddrress=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.ShowAddress"),
            cls: 'pwnd add',
            id: "showaddress" + this.id,                
            tooltip : WtfGlobal.getLocaleText("acc.field.UseShowAddressoptiontoinsertAddresses"),       
            style:" padding-left: 15px;",
            scope: this,
            disabled : true,
            handler:this.getAddressWindow 
        });
        this.buttonArray.push(this.showAddrress);
    },
    
    appendNorthFormFields: function(){
        this.itemArr=[];
        this.itemArr.push(
            this.ShowOnlyOneTime,
            this.Name,
            this.Currency,
            {
                layout:'column',
                border:false,
                defaults:{border:false},
                items:[ {
                    layout:'form',
                    ctCls : "",
                    width:215,
                    items:this.fromPO             
                },{
                    width:250,
                    layout:'form',
                    labelWidth:45,
                    items:this.fromLinkCombo
               }]
            },
            this.PO,
            this.sequenceFormatCombobox,
            this.Number,
            this.billDate,
            this.editedBy
        );
            
        this.itemArray=[];
        this.itemArray.push(
            this.CostCenter, 
            this.DOStatusCombo, 
            this.Memo, 
            this.shipDate, 
            this.shipvia, 
            this.fob, 
            this.permitNumber, 
            this.driver, 
            this.users,
            this.includeProTax
        );
    },
    
    loadInitialStore: function(){
        WtfGlobal.loadpersonacc(this.isCustomer);

        this.sequenceFormatStore.load();

        this.currencyStore.load();
        
        chkFormCostCenterload();
        
        this.isCustomer ? Wtf.salesPersonFilteredByCustomer.load() : Wtf.agentStore.load();
        
        if (!Wtf.account.companyAccountPref.deliveryPlanner && this.moduleid == Wtf.Acc_Delivery_Order_ModuleId) {
            this.driverStore.load();
        }
        
        if(this.isEdit) {
            this.loadEditableGrid();
        }
    },
    
    updateFieldConfigs: function(){
        if(!this.isCustomer){
            this.permitNumber.hideLabel=false;
            this.permitNumber.hidden=false;
        }
        if( this.isEdit ){
            this.isClosable=false          // Set Closable flag for edit and copy case
        }
        if(this.isEdit || this.copyInv){
            this.showAddrress.enable();
        }
    },
    
    hideFormFields:function(){
        if(this.isCustomer){
            this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.deliveryOrder);
        }else{
            this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.goodsReceipt);
        }
    },
    
    addSalesPerson:function(){
        this.isCustomer ? addMasterItemWindow('15') : addMasterItemWindow('20');
    },
    
    onNameSelect:function(combo,rec,index){
       if(combo.getValue()==this.nameBeforeSelect){ //If same name selected no need to do any action 
           return;
       }
       
       this.doOnNameSelect(combo,rec,index);
          
    },
    
    doOnNameSelect:function(combo,rec,index){
        this.externalcurrencyrate=0;
        this.changeTemplateSymbol();
        var customer= this.Name.getValue();
        Wtf.salesPersonFilteredByCustomer.load({
            params:{ //sending a customerid to fliter available masteritems for selected customer 
                customerid:customer            
            }
        });
        if(Wtf.account.companyAccountPref.unitPriceConfiguration) {
            this.showGridTax(null,null,true);
        }
        this.updateData();
        this.setSalesPerson(combo,rec,index);
        this.Grid.setDisabled(false);
    },
    
    updateData:function(){
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
    },
    
    setSalesPerson:function(c,rec,ind){
        this.users.setValue(rec.data['masterSalesPerson']);
    },
    
    hideTransactionFormFields:function(array){
        if(array){
            for(var i=0;i<array.length;i++){
                var fieldArray = array[i];
                if(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id)){
                    if(fieldArray.fieldId=="ShowOnlyOneTime" && ((this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv !=undefined ?this.copyInv:false))){
                        continue;
                    }
                    Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hideLabel = fieldArray.isHidden;
                    Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hidden = fieldArray.isHidden;
                    if(fieldArray.isReadOnly){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).disabled = fieldArray.isReadOnly;
                    }
                    if(fieldArray.isUserManadatoryField && Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel != undefined){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).allowBlank = !fieldArray.isUserManadatoryField;
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel = Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel + " *";
                    }
                    if( fieldArray.fieldLabelText!=null && fieldArray.fieldLabelText!=undefined && fieldArray.fieldLabelText!=""){
                        if(fieldArray.isManadatoryField && fieldArray.isFormField )
                            Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel=fieldArray.fieldLabelText +"*";
                        else
                            Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel=fieldArray.fieldLabelText;
                    }
                }
            }
        }
    },
    
    getAddressWindow:function(){
       var custvendorid=this.Name.getValue();
       var addressRecord="";
       if(this.linkRecord && this.singleLink){      //when user link single record
           addressRecord=this.linkRecord;
       } else {
           addressRecord=this.record;
       }
        var isCopy = this.copyInv;
        var isEdit = this.isEdit;
        if (this.isVenOrCustSelect) {
            isEdit = false;
            isCopy = false;
        }

       callAddressDetailWindow(addressRecord,isEdit,isCopy,custvendorid,this.currentAddressDetailrec,this.isCustomer,this.readOnly,"",this.singleLink); 
       Wtf.getCmp('addressDetailWindow').on('update',function(config){
            this.currentAddressDetailrec=config.currentaddress;
       },this);
    },
    
    addDOStatus: function(){
        (this.businessPerson == "Customer")? addMasterItemWindow('10') : addMasterItemWindow('11');
    },
    
    onDateChange:function(a,val,oldval){
        this.val=val;
        this.oldval=oldval;
        if(this.Grid){
            this.Grid.billDate=this.billDate.getValue();
        }
        if(this.Currency.getValue()==WtfGlobal.getCurrencyID()){ //when tranaction in base currency for all cases (edit,copy, create new)
            this.doOnDateChanged(val,oldval);
        } else if((this.isEdit && !this.copyInv) && Wtf.account.companyAccountPref.retainExchangeRate){ //edit case: when user want to retain exchange rate        
            this.exchangeRateInRetainCase = true;
            this.currencyStore.load({params: {mode: 201, transactiondate: WtfGlobal.convertToGenericDate(this.billDate.getValue())}});                                 
        } else if(this.isEdit || this.copyInv) { //1.Edit case when user do not want to retain exchange rate 2.copy case
             Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.invoice.exchangeRateMsg"),function(btn){
                if(btn=="yes"){
                    this.doOnDateChanged(val,oldval); 
                } else{
                    this.billDate.setValue(oldval);
                    return;
                }
             },this);
        } else { //Normal Create New Case           
            this.doOnDateChanged(val,oldval);        
        }
        this.custdatechange=true;
    },
    
    doOnDateChanged:function(val,oldval){
       this.externalcurrencyrate=0;
       this.currencyStore.on('load',function(store){this.onDateChangeVendorCurrencyExchangeRate();},this);
        this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
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
   
    hideLoading:function(){Wtf.MessageBox.hide();},
    
    addCostCenter:function(){
        callCostCenter('addCostCenterWin');
    },
    
    getPostTextEditor: function(posttext)
    {
    	var _tw=new Wtf.EditorWindowQuotation({
    		val:this.postText
    	});
        
    	 _tw.on("okClicked", function(obj){
             this.postText = obj.getEditorVal().textVal;
             var styleExpression  =  new RegExp("<style.*?</style>");
             this.postText=this.postText.replace(styleExpression,"");
                 
             
         }, this);
         _tw.show();
        return this.postText;
    },

    createProductGrid:function(){
        this.Grid=new Wtf.account.DeliveryOrderGrid({
            height: 300,//region:'center',//Bug Fixed: 14871[SK]
            cls:'gridFormat',
            layout:'fit',
            viewConfig:{forceFit:false},
            isCustomer:this.isCustomer,
            editTransaction:this.isEdit,
            readOnly:this.isViewTemplate ||this.readOnly,
            disabledClass:"newtripcmbss",
            isCustBill:false,
            id:this.id+"billingproductdetailsgrid",
            moduleid:this.moduleid,
            currencyid:this.Currency.getValue(),
            fromOrder:true,
            isOrder:this.isOrder,
            isEdit:this.isEdit,
            copyTrans:this.copyInv, 
            forceFit:true,
            loadMask : true,
            heplmodeid:this.heplmodeid,
            parentid:this.id,
            parentObj :this,
            isViewTemplate: this.isViewTemplate,
            disabled:!(this.isEdit ||this.copyInv)?true:false
        });

        if(!this.isEdit && !this.copyInv){
            if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){        
            this.Grid.productComboStore.load();
            }
        }
        
    },
    
    addProductGridEvents: function(){
        this.Grid.on("productdeleted", this.removeTransStore, this);
        this.Grid.on("datachanged", function(){
//              this.applyCurrencySymbol();
            this.updateSubtotal();
            this.isClosable=false          // Set Closable flag on grid data change
        },this);
        this.Grid.productComboStore.on('load',function(store){            
            if(!this.saveOnlyFlag && this.isEdit && !(this.productOptimizedFlag!= undefined && (this.productOptimizedFlag==Wtf.Products_on_type_ahead || this.productOptimizedFlag==Wtf.Products_on_Submit))){ //no need to load editablegrid after click on save button
                this.loadEditableGrid();
            }
        },this);
        
        this.NorthForm.on('render',this.setDate,this);
        if(this.readOnly){
            this.disabledbutton();  //  disabled button in view case
        }
        this.Grid.getStore().on('load',function(store){            
            this.Grid.addBlank(store);
            this.updateFormCurrency();
        }.createDelegate(this),this);
        
        this.Grid.getStore().on('update',function(store,record,opr){            
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
                    productname:prorec.data['productname']+" ",
                    qty:parseFloat(getRoundofValue(availableQuantityInSelectedUOM)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"  "+selectedUOMName,
                    soqty:parseFloat(getRoundofValue(socountinselecteduom)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"  "+selectedUOMName,
                    poqty:parseFloat(getRoundofValue(pocountinselecteduom)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"  "+selectedUOMName               
                });
            }  
        },this);
    },
    
    createSouthPanelFields: function(){
        this.tplSummary=new Wtf.XTemplate(
            '<div class="currency-view">',
            '<table width="100%">',
            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoice.subTotal")+' </b></td><td text-align=right>{subtotal}</td></tr>',
            '</table>',
            '<table width="100%">',
            '<tr><td><b>+ '+WtfGlobal.getLocaleText("acc.invoice.Tax")+': </b></td><td align=right>{tax}</td></tr>',
            '</table>',
            '<table width="100%">',
            '</table>',
            '<hr class="templineview">',
            '<table width="100%">',
            '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.invoice.totalAmt")+' </b></td><td align=right>{aftertaxamt}</td></tr>',
            '</table>',
            '<table width="100%">',
            '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.invoice.totalAmtInBase")+' </b></td><td align=right>{totalAmtInBase}</td></tr>',
            '</table>',
            '<hr class="templineview">',
            '</table>',
            '<table width="100%">',
            '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.inv.amountdue")+' </b></td><td align=right>{amountdue}</td></tr>',
            '</table>',
            '<hr class="templineview">',
            '<hr class="templineview">',
            '</div>'
        );
            
        this.southCalTemp=new Wtf.Panel({  
            border:false,
            baseCls:'tempbackgroundview',
            html:this.tplSummary.apply({subtotal:WtfGlobal.currencyRenderer(0),tax:WtfGlobal.currencyRenderer(0),aftertaxamt:WtfGlobal.currencyRenderer(0),totalAmtInBase:WtfGlobal.currencyRenderer(0),amountdue:WtfGlobal.currencyRenderer(0)})
        });
        
        this.productDetailsTplSummary=new Wtf.XTemplate(
            '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',            
            '<div><hr class="templineview"></div>',
            '<div>',
            '<table width="100%">'+
            '<tr>'+
            '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.field.ProductName")+'</b></td><td style="width:55%;"><span wtf:qtip="{productname}">'+Wtf.util.Format.ellipsis('{productname}',60)+'</span></td>'+                   
            '</tr>'+
            '<tr>'+
            '<td><b>'+WtfGlobal.getLocaleText("acc.field.InStock")+': </b></td><td style="width:10%;">{qty}</td>'+
            '<td><b>'+WtfGlobal.getLocaleText("acc.field.OpenPO")+': </b></td><td style="width:10%;">{poqty}</td>'+
            '<td><b>'+WtfGlobal.getLocaleText("acc.field.OpenSO")+': </b></td><td style="width:40%;">{soqty}</td>'+                        
            '</tr>'+
            '</table>'+
            '</div>',            
            '<div><hr class="templineview"></div>',                        
            '</div>'
        );

        var blockSpotRateLink_first = "";
        var blockSpotRateLink_second = "";
        if(!Wtf.account.companyAccountPref.activateToBlockSpotRate){ // If activateToBlockSpotRate is set then block the Spot Rate Links
            blockSpotRateLink_first = WtfGlobal.getLocaleText("acc.invoice.msg9")+"</div><div style='padding-left:30px;padding-top:5px;padding-bottom:10px;'><a class='tbar-link-text' href='#' onClick='javascript: editInvoiceExchangeRates(\""+this.id+"\",\"{foreigncurrency}\",\"{basecurrency}\",\"{revexchangerate}\",\"foreigntobase\")'wtf:qtip=''>{foreigncurrency} to {basecurrency}</a>";
            blockSpotRateLink_second = WtfGlobal.getLocaleText("acc.invoice.msg9")+"</div> <div style='padding-left:30px;padding-top:5px;'><a class='tbar-link-text' href='#' onClick='javascript: editInvoiceExchangeRates(\""+this.id+"\",\"{basecurrency}\",\"{foreigncurrency}\",\"{exchangerate}\",\"basetoforeign\")'wtf:qtip=''>{basecurrency} to {foreigncurrency}</a></div>";
        }
        this.southCenterTplSummary=new Wtf.XTemplate(
            "<div> &nbsp;</div>",  //Currency:
            '<tpl if="editable==true">',
            "<b>"+WtfGlobal.getLocaleText("acc.invoice.msg8")+"</b>",  //Applied Exchange Rate for the current transaction:
            "<div style='line-height:18px;padding-left:30px;'>1 {foreigncurrency} "+WtfGlobal.getLocaleText("acc.inv.for")+" = {revexchangerate} {basecurrency} "+WtfGlobal.getLocaleText("acc.inv.hom")+". "+
            blockSpotRateLink_first,
            "</d   iv><div style='line-height:18px;padding-left:30px;'>1 {basecurrency} "+WtfGlobal.getLocaleText("acc.inv.hom")+" = {exchangerate} {foreigncurrency} "+WtfGlobal.getLocaleText("acc.inv.for")+". "+    
            blockSpotRateLink_second,
            '</tpl>'
        );    
            
        this.southCenterTpl=new Wtf.Panel({
            border:false,
            html:this.southCenterTplSummary.apply({basecurrency:WtfGlobal.getCurrencyName(),exchangerate:'x',foreigncurrency:"Foreign Currency", editable:false})
        });
            
        this.productDetailsTpl=new Wtf.Panel({
            border:false,
            baseCls:'tempbackgroundview',
            width:'95%',            
            html:this.productDetailsTplSummary.apply({productname:"&nbsp;&nbsp;&nbsp;&nbsp;",qty:0,soqty:0,poqty:0})
        });
    },
    
    appendSouthPanelFields: function(){
        this.southPanelItemArr = [];
        this.southPanelItemArr.push(
            {
                columnWidth: .45,// width: 570,//region:'center',
                border:false,
                items:[this.productDetailsTpl,this.southCenterTpl]
            },{
                columnWidth: .30,
                layout:'column',// width: 570,//region:'center',
                items:[
                    {
                        columnWidth:0.55,
                        layout:'form',
                        border:false,
                        items:[this.isTaxable]
                    }, {
                        columnWidth:0.43,
                        layout:'form',
                        labelWidth:30,
                        border:false,
                        items:[this.Tax]
                        
                    }], 
                border:false
            },{
                columnWidth:.25,
                layout:'form',
                cls:'bckgroundcolor',
                bodyStyle:'padding:10px',
                labelWidth:70,
                items:[this.southCalTemp]
            }
        );
    },
   
    disabledbutton:function(){
        this.CostCenter.setDisabled(true);
        this.DOStatusCombo.setDisabled(true);
        this.shipDate.setDisabled(true);
        this.shipvia.setDisabled(true);
        this.fob.setDisabled(true);
        this.permitNumber.setDisabled(true);
        this.driver.setDisabled(true); 
        this.ShowOnlyOneTime.setDisabled(true); 
        this.Name.setDisabled(true); 
        this.Currency.setDisabled(true); 
        this.fromPO.setDisabled(true);
        this.fromLinkCombo.setDisabled(true);
        this.PO.setDisabled(true);
        this.sequenceFormatCombobox.setDisabled(true);
        this.Number.setDisabled(true);
        this.billDate.setDisabled(true);
        this.editedBy.setDisabled(true);
        this.users.setDisabled(true);
        this.includeProTax.setDisabled(true);
    },
    
    addOrder:function(){
        var tabid = "ordertab";
        if(this.isCustomer){
                tabid = "salesorder";
                if (this.POSelected == 'sales')
                    callSalesOrder(false, null, tabid);
                else if (this.POSelected == 'invoice')
                    callInvoice();
            
        }else{
            
                tabid = "purchaseorder";
            if (this.POSelected == 'sales')
                callPurchaseOrder(false, null, tabid);
            else if (this.POSelected == 'invoice')
                    callGoodsReceipt(false,null);
                
            
        }
        if(Wtf.getCmp(tabid)!=undefined) {
            Wtf.getCmp(tabid).on('update',function(){this.POStore.reload();},this);
        }
    },

    enablePO:function(c,rec){
        this.fromLinkCombo.clearValue();
        this.PO.clearValue();
        this.CostCenter.clearValue();
        this.DOStatusCombo.clearValue();
        //this.Name.clearValue();
        this.Memo.setValue("");
        this.singleLink = false;

        if(rec.data['value']==true){                                                
            this.fromLinkCombo.enable();                        
            this.fromOrder=true;
        }
        else{
            this.Grid.getStore().removeAll();            
            this.Grid.addBlankRow();
            this.fromLinkCombo.disable();
            this.PO.disable();
            this.PO.reset();
        }
        //this.currencyStore.load(); 	       // Currency id issue 20018
    },

    enableNumber:function(c,rec){
        
        this.PO.clearValue();
        this.CostCenter.clearValue();
        this.DOStatusCombo.clearValue();
        this.Memo.setValue("");
        this.Grid.getStore().removeAll();            
        this.Grid.addBlankRow();
        
            if(rec.data['value']==0){
                this.PO.addListener("blur",this.populateData,this);
                this.POStore.proxy.conn.url = this.isCustomer ? "ACCSalesOrderCMN/getSalesOrders.do" : "ACCPurchaseOrderCMN/getPurchaseOrders.do";
                this.POStore.load({params:{id:this.Name.getValue(),exceptFlagORD:true, currencyfilterfortrans:this.Currency.getValue()}});        
                this.PO.enable(); 
                this.POSelected="sales";
            } else if(rec.data['value']==1){
                this.PO.addListener("blur",this.populateData,this);
                this.POStore.proxy.conn.url = this.isCustomer ? "ACCInvoiceCMN/getInvoices.do" : "ACCGoodsReceiptCMN/getGoodsReceipts.do";
                var params={cashonly:false,creditonly:true,currencyfilterfortrans:this.Currency.getValue(),nondeleted:true,avoidRecursiveLink:true};
                if(this.isCustomer) {                        
                    params.customerid=this.Name.getValue();                    
                }else{
                    params.vendorid=this.Name.getValue();                    
                }
                params.CashAndInvoice=true;//   CashAndInvoice  true to select both Cash Sale and Invoice/Cash Purchase and Vendor Invoice   
                this.POStore.load({params:params});        
                this.PO.enable();       
                this.POSelected="invoice";
            }
    },
    
    loadEditableGrid:function(){
        this.subGridStoreUrl = "";
        this.subGridStoreUrl = this.isCustomer ? "ACCInvoiceCMN/getDeliveryOrderRows.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrderRows.do";
        var colModelArrayProduct = GlobalColumnModelForProduct[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArrayProduct,this.Grid.getStore());    

        this.billid=this.record.data.billid;
        this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;
        this.Grid.getStore().on("load", function(){
            this.loadRecord();
        }, this);
        this.Grid.getStore().load({params:{bills:this.billid,moduleid:this.moduleid,isEdit:this.isEdit,copyInv:this.copyInv}});
    },

    populateData:function(c,rec) {
        this.singleLink = false;
        if(this.PO.getValue()!=""){
            if (Wtf.account.companyAccountPref.enableLinkToSelWin && (this.moduleid === Wtf.Acc_Delivery_Order_ModuleId || this.moduleid === Wtf.Acc_Goods_Receipt_ModuleId )) {
                var billid = this.PO.getValue();
                if (billid.indexOf("-1") != -1) {
                    var selectedValuesArr = billid.split(',');
                    var arr = [];
                    for (var cnt = 0; cnt < selectedValuesArr.length; cnt++) {
                        if (selectedValuesArr[cnt] != "-1") {
                            arr.push(selectedValuesArr[cnt]);
                        }
                    }
                    this.PO.setValue(arr);
                    var url = "";
                    if (this.fromLinkCombo.getValue() ==0 && this.isOrder) {
                        url = this.isCustomer ? "ACCSalesOrderCMN/getSalesOrders.do":"ACCPurchaseOrderCMN/getPurchaseOrders.do" ;
                    } else if(this.fromLinkCombo.getValue() == 1){
                        url = this.isCustomer ? "ACCInvoiceCMN/getInvoices.do" : "ACCGoodsReceiptCMN/getGoodsReceipts.do"              
                    }
                    this.showPONumbersGrid(url);
                }
            }
            this.Grid.fromPO=true;
            var billid=this.PO.getValue();
            var selectedValuesArr = billid.split(',');
            var taxDiff=false;
            if(billid.indexOf(",")==-1){  //In MultiSelection if the user select only one                            
                rec=this.POStore.getAt(this.POStore.find('billid',billid));
                this.Memo.setValue(rec.data['memo']);
                this.postText=rec.data['posttext'];
                //this.Name.setValue(rec.data['personid']);   
                this.shipDate.setValue(rec.data['shipdate']);
                this.shipvia.setValue(rec.data['shipvia']);
                this.fob.setValue(rec.data['fob']);
                this.permitNumber.setValue(rec.data['permitNumber']);
                this.Currency.setValue(rec.data['currencyid']);
                var perstore=this.isCustomer? Wtf.customerAccStore:Wtf.vendorAccStore
                var storerec=perstore.getAt(perstore.find('accid',rec.data['personid']));        
                this.CostCenter.setValue(rec.data.costcenterid);  
                
                var record=this.POStore.getAt(this.POStore.find('billid',billid));
                this.linkRecord=this.POStore.getAt(this.POStore.find('billid',billid));
                this.singleLink=true;
                WtfGlobal.resetCustomFields();
                var fieldArr = this.POStore.fields.items;
                for(var fieldCnt=0; fieldCnt < fieldArr.length; fieldCnt++) {
                    var fieldN = fieldArr[fieldCnt];
                   
                    if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id) && record.data[fieldN.name] !="") {
                          if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).getXType()=='datefield'){
                             Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                          }else if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).getXType()=='fncombo'){
                                  var ComboValue=record.data[fieldN.name];
//                                var ComboValueID="";
//                                var recCustomCombo =WtfGlobal.searchRecord(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).store,ComboValue,"name");
                                if(ComboValue){
//                                    ComboValueID=recCustomCombo.data.id;
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
                                    var ComboValue=eval("record.json."+fieldN.name);
                                    var ComboValueArrya=ComboValue.split(',');
                                    var ComboValueID="";
                                    var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray; 
                                    for(var i=0 ;i < ComboValueArrya.length ; i++){
                                        for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                                            if(checkListCheckBoxesArray[checkitemcnt].id.indexOf(ComboValueArrya[i]) != -1 )
                                                if (Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id) != undefined) {
                                                    Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id).setValue(true);
                                                }
                                        } 
                                    }
                            }else if(Wtf.getCmp(fieldname+this.tagsFieldset.id).getXType()=='select'){
                                    var ComboValue=eval("record.json."+fieldN.name);
//                                    var ComboValueArrya=ComboValue.split(',');
//                                    var ComboValueID="";
//                                    for(var i=0 ;i < ComboValueArrya.length ; i++){
//                                        var recCustomCombo =WtfGlobal.searchRecord(Wtf.getCmp(fieldname+this.tagsFieldset.id).store,ComboValueArrya[i],"name");
//                                        ComboValueID+=recCustomCombo.data.id+","; 
//                                    }
//                                    if(ComboValueID.length > 1){
//                                        ComboValueID=ComboValueID.substring(0,ComboValueID.length - 1);
//                                    }
                                    if(ComboValue!="" && ComboValue!=undefined)
                                    Wtf.getCmp(fieldname+this.tagsFieldset.id).setValue(ComboValue);
                            }

                        }
                    }
                    var linkedRecordExternalCurrencyRate=rec.data["externalcurrencyrate"];
                    if(this.Currency.getValue()!=WtfGlobal.getCurrencyID && linkedRecordExternalCurrencyRate!="" && linkedRecordExternalCurrencyRate!=undefined){ //If selected currency is foreign currency then currency exchange rate will be exchange rate of linked document 
                        this.externalcurrencyrate=linkedRecordExternalCurrencyRate;
                    }
                }
            }else{// for multiple selections
                var taxRecords=0;
                this.previusTaxId="";
                
                for(var cnt=0;cnt<selectedValuesArr.length;cnt++){
                    var rec=this.POStore.getAt(this.POStore.find('billid',selectedValuesArr[cnt]));
                    if(rec.data.contract!=undefined && rec.data.contract!=""){// in case of multiple linking if linked transactions are containing different different contract ids or similar contract ids then we will not allow linking
                        var dataMsg = "";
                        if(this.fromLinkCombo.getValue() == 0){// linked from SO
                            dataMsg = WtfGlobal.getLocaleText("acc.linking.so.selection.msg");
                        } else if(this.fromLinkCombo.getValue() == 1){// linked from CI
                            dataMsg = WtfGlobal.getLocaleText("acc.linking.ci.selection.msg");
                        }

                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),dataMsg], 2);
                        this.PO.clearValue();
                        return;
                    }
                    if(taxRecords!=0 && this.previusTaxId!=rec.data["taxid"]){
                        taxDiff=true;
                    }
                    taxRecords++;
                    this.previusTaxId=rec.data["taxid"];
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
                this.Memo.setValue('');                
                this.shipDate.setValue('');
                this.shipvia.setValue('');
                this.fob.setValue('');                                                
                this.permitNumber.setValue('');                                                
                this.CostCenter.setValue('');
            }
            if (rec.data["includeprotax"]) {
                this.includeProTax.setValue(true);
                if(Wtf.account.companyAccountPref.unitPriceConfiguration){
                    this.showGridTax(null, null, false);
                }
                this.isTaxable.setValue(false);//when selecting record with product tax.Tax should get disabled.
                this.isTaxable.disable();
                this.Tax.setValue("");
                this.Tax.disable();
            } else {
                this.includeProTax.setValue(false);
                if(Wtf.account.companyAccountPref.unitPriceConfiguration){
                    this.showGridTax(null, null, true);
                }
                this.Tax.enable();//required because when selected multiple records & changing to select single record.Before it was getting disabled.
                this.isTaxable.enable();
            }
            if ((rec.data.taxid == "")) {//generate so or po it should not show taxid
                if (taxDiff) {//for different tax
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.alert.includingDifferentTax")], 2);
                    this.PO.clearValue();
                    return;
                }
                this.isTaxable.setValue(false);
                this.Tax.setValue("");
                this.Tax.disable();
            }else{
//                this.includeProTax.setValue(false);
                if (taxDiff) {//for different tax
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.alert.includingDifferentTax")], 2);
                    this.PO.clearValue();
                    return;
                }
                this.Tax.setValue(rec.data.taxid);
                 this.isTaxable.enable();
                this.Tax.enable();//enable the tax when taxid is present-for edit case it was not required but for copy its is required.
                this.isTaxable.setValue(true);
            }
            if (rec!=undefined && this.users != null && this.users != undefined) {
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
            rec=this.PO.getValue();
            //this.updateDueDate();
            var url = "";
                    //(this.isCustBill?53:43)
            var linkingFlag = false;   //For removing cross reference of DO-CI or GR-VI     
            var isForDOGROLinking = true;// if DO/GRO is being create with Linking to SO/PO/CI/VI
            if(this.fromLinkCombo.getValue()==0){
                url = this.isCustomer ? 'ACCSalesOrderCMN/getSalesOrderRows.do' : "ACCPurchaseOrderCMN/getPurchaseOrderRows.do";
            } else if(this.fromLinkCombo.getValue()==1){
                url = this.isCustomer ? "ACCInvoiceCMN/getInvoiceRows.do" : "ACCGoodsReceiptCMN/getGoodsReceiptRows.do";
                var linkingFlag =true;
            }
            this.Grid.getStore().proxy.conn.url = url;
            this.Grid.loadPOGridStore(rec,linkingFlag,isForDOGROLinking);
        }   
    },

    getDates:function(start){
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;

        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },

    setDate:function(){
        var height = 0;
        if(this.isOrder)
            height=140;
        
        //if(height>=140) this.NorthForm.setHeight(height);

        if(!this.isEdit){            
            this.billDate.setValue(Wtf.serverDate);//(new Date());            
        }
    },    
    
    addPerson:function(isEdit,rec,winid,isCustomer){
        callBusinessContactWindow(isEdit, rec, winid, isCustomer);
        var tabid=isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
        Wtf.getCmp(tabid).on('update', function(){
           this.isCustomer?Wtf.customerAccStore.reload():Wtf.vendorAccStore.reload();
        }, this);
    },
    
    onCurrencyChangeOnly:function(){
        this.fromPO.reset();
        this.fromLinkCombo.reset();
        this.fromLinkCombo.setDisabled(true);
        this.PO.reset();
        this.PO.setDisabled(true);
        this.Grid.getStore().removeAll();
        this.Grid.addBlankRow();
    },
    
    changeTemplateSymbol:function(){
        // check wheather exchange rate is set for currency on selected date while retaining exchange rate.
        if (this.exchangeRateInRetainCase) {
            if (this.Currency.getValue() != "" && WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid") == null) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleasesetthecurrencyrate") + " " + WtfGlobal.getLocaleText("acc.field.fortheselecteddate") + "<b>" + WtfGlobal.convertToGenericDate(this.val) + "</b>"], 0);
                this.exchangeRateInRetainCase = false;
                this.billDate.setValue("");
            }
            return;
        }
        if(this.loadCurrFlag && Wtf.account.companyAccountPref.currencyid && !this.isEdit){
            this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
            this.loadCurrFlag = false;
        }
        
        if(this.currencyStore.getCount()==0){
            callCurrencyExchangeWindow();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
            this.billDate.setValue("");
        } else
            this.updateFormCurrency();
    },
    
    updateFormCurrency:function(){
//        this.applyCurrencySymbol();
//        this.tplSummary.overwrite(this.southCalTemp.body,{
//            subtotal:WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),
//            tax:(this.moduleid == 28 || this.moduleid ==27) ? WtfGlobal.addCurrencySymbolOnly(this.Grid.calLineLevelTaxNew(),this.symbol) : WtfGlobal.addCurrencySymbolOnly(this.Grid.calTaxtotal(),this.symbol),
//            aftertaxamt:(this.moduleid == 28 || this.moduleid ==27) ? WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal()+this.Grid.calLineLevelTaxNew(),this.symbol):WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal()+this.Grid.calTaxtotal(),this.symbol)
//        });
        this.updateSubtotal();
    },  
    
    getCurrencySymbol:function(){
        var index=null;
//        this.currencyStore.clearFilter(true); //ERP-9962
        var recordCurrencyid=this.record !=null?this.record.data.currencyid:"";//    ERP-10464
        var FIND = this.Currency.getValue()!=""?this.Currency.getValue():recordCurrencyid;//this.Currency.getValue();
        if(FIND == "" || FIND == undefined || FIND == null) {
            FIND = WtfGlobal.getCurrencyID();
        }
        index=this.currencyStore.findBy( function(rec){
             var parentname=rec.data['currencyid'];
            if(parentname == FIND)
                return true;
             else
                return false
            })
       this.currencyid=this.Currency.getValue();
       return index;
    },
    
    calTotalAmountInBase:function(){
        var subtotal=this.Grid.calSubtotalInBase(); 
        var taxVal = this.calAmountInBase(this.caltax());
        var returnValInOriginalCurr = subtotal + this.findTermsTotalInBase()+taxVal; //-discount;
        returnValInOriginalCurr = getRoundedAmountValue(returnValInOriginalCurr);
        return returnValInOriginalCurr; 
    },
    
    findTermsTotalInBase : function() {
        var termTotal = 0;
        if(this.termgrid) {
            var store = this.termgrid.store;
            var totalCnt = store.getCount();
            for(var cnt=0; cnt<totalCnt; cnt++) {
                var lineAmt = store.getAt(cnt).data.termamount;
                if(typeof lineAmt=='number'){
                    var termVal = getRoundedAmountValue(lineAmt);
                    termTotal += this.calAmountInBase(termVal);
                } 
            }
        }
        return getRoundedAmountValue(termTotal);
    },
    
    calAmountInBase:function(val){
        var returnVal = getRoundedAmountValue(val*this.getExchangeRate());
        return returnVal; 
    },
    
    getExchangeRate:function(){
        var index=this.getCurrencySymbol();
        var rate=this.externalcurrencyrate;
        var revExchangeRate = 0;
        if(index>=0){
            var exchangeRate = this.currencyStore.getAt(index).data['exchangerate'];
            if(this.externalcurrencyrate>0) {
                exchangeRate = this.externalcurrencyrate;
            }
            revExchangeRate = 1/(exchangeRate);
            revExchangeRate = (Math.round(revExchangeRate*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
        }
        return revExchangeRate;
    },
    
    applyTemplate:function(store,index){
        var editable=this.Currency.getValue()!=WtfGlobal.getCurrencyID()&&this.Currency.getValue()!=""//&&!this.isOrder;
        var exchangeRate = store.getAt(index).data['exchangerate'];
        if(this.externalcurrencyrate>0) {
            exchangeRate = this.externalcurrencyrate;
        } else if(this.isEdit && this.record.data.externalcurrencyrate && !(this.custdatechange || this.currencychanged)){
            var externalCurrencyRate = this.record.data.externalcurrencyrate-0;//??[PS]
            if(externalCurrencyRate>0){
                exchangeRate = externalCurrencyRate;
            }
        }
        this.externalcurrencyrate = exchangeRate;
        var revExchangeRate = 1/(exchangeRate-0);
        if(this.exchangeratetype!=undefined&&this.exchangeratetype=="foreigntobase"&&this.revexternalcurrencyrate!=undefined&&this.revexternalcurrencyrate!=0)
            {
                revExchangeRate=this.revexternalcurrencyrate
                this.revexternalcurrencyrate=0;
            }
        revExchangeRate = (Math.round(revExchangeRate*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
        this.southCenterTplSummary.overwrite(this.southCenterTpl.body,{foreigncurrency:store.getAt(index).data['currencyname'],exchangerate:exchangeRate,basecurrency:WtfGlobal.getCurrencyName(),editable:editable,revexchangerate:revExchangeRate         
            });
    },
    
    applyCurrencySymbol:function() {
        var index = this.getCurrencySymbol();
        var rate = this.externalcurrencyrate;
        if(index >= 0){
           rate = (rate == "" ? this.currencyStore.getAt(index).data.exchangerate : rate);
           this.symbol =  this.currencyStore.getAt(index).data.symbol;
           this.Grid.setCurrencyid(this.currencyid,rate,this.symbol,index);
           this.applyTemplate(this.currencyStore,index);
        }
        this.tplSummary.overwrite(this.southCalTemp.body,{
            subtotal:WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),
            tax:(this.moduleid == 28 || this.moduleid ==27)? WtfGlobal.addCurrencySymbolOnly(this.Grid.calLineLevelTaxNew(),this.symbol) : WtfGlobal.addCurrencySymbolOnly(this.Grid.calTaxtotal(),this.symbol),
            aftertaxamt:(this.moduleid == 28 || this.moduleid ==27) ? WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal()+this.Grid.calLineLevelTaxNew(),this.symbol):WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal()+this.Grid.calTaxtotal(),this.symbol),
            totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol()),
            amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())
        });
        return this.symbol;
    },
    
    setTransactionNumber:function(isSelectNoFromCombo){
    	if(this.isEdit && !this.copyInv)
            this.Number.setValue(this.record.data.billno);
        else{
            var format= this.isCustomer ? Wtf.account.companyAccountPref.autodo : Wtf.account.companyAccountPref.autogro;
            var temp2=this.isCustomer ? Wtf.autoNum.DeliveryOrder : Wtf.autoNum.GoodsReceiptOrder;
        }
        if(isSelectNoFromCombo){
            this.fromnumber = temp2;
        } else if(format&&format.length>0){
            WtfGlobal.fetchAutoNumber(temp2, function(resp){if(this.isEdit)this.Number.setValue(resp.data)}, this);
        }
    },   
   
    initForClose:function(){
        this.cascade(function(comp){
            if(comp.isXType('field')){
                comp.on('change', function(){this.isClosable=false;},this);
            }
        },this);
    },
    
    setNextNumber:function(){
        if(this.sequenceFormatStore.getCount()>0){
            if(this.isEdit || this.copyInv){
                var sequenceformatid=this.record.get("sequenceformatid");
                if(sequenceformatid=="" || sequenceformatid==undefined){
                    this.sequenceFormatCombobox.setValue("NA"); 
                    this.sequenceFormatCombobox.disable();
                    if(this.readOnly!=undefined && !this.readOnly){
                        this.Number.enable();
                    }
                    if(this.copyInv){//for copy NA enable disable number field
                        this.getNextSequenceNumber(this.sequenceFormatCombobox);
                    }
                
                } else{
                    var index=this.sequenceFormatStore.find('id',sequenceformatid);
                    if(index!=-1){
                        this.sequenceFormatCombobox.setValue(sequenceformatid);                                               
                    }else{  //sequence format get deleted then NA is set
                        this.sequenceFormatCombobox.setValue("NA");  
                    }    
                    
                        if(!this.copyInv){//edit case
                            this.sequenceFormatCombobox.disable();
                            this.Number.disable(); 
                        }else {//copy case if sequenceformatid present then hide number field
                            this.Number.setValue(sequenceformatid);
                            this.Number.disable();
                            WtfGlobal.hideFormElement(this.Number);
                        }
                        
                  }
                }else{
                    var count=this.sequenceFormatStore.getCount();
                    for(var i=0;i<count;i++){
                        var seqRec=this.sequenceFormatStore.getAt(i);
                        if(seqRec.json.isdefaultformat=="Yes"){
                            this.sequenceFormatCombobox.setValue(seqRec.data.id);
                            break;
                        }
                    }
                    if(this.sequenceFormatCombobox.getValue()!=""){
                        this.getNextSequenceNumber(this.sequenceFormatCombobox); 
                    } else{
                        this.Number.setValue("");
                        WtfGlobal.hideFormElement(this.Number);
                    }
            }                                 
        }
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
                    this.Number.enable();
                }else {
                    this.Number.setValue(resp.data);
                    //                this.Number.enable();
                    this.Number.disable();
                    WtfGlobal.hideFormElement(this.Number);
                }

            });
        } else {
            WtfGlobal.showFormElement(this.Number);
            this.Number.reset();
            this.Number.enable();
        }
    },
    
    beforeSave:function(){
        if(this.copyInv || this.isEdit){
            this.Grid.getStore().each(function(rec){
                if(rec.data.dquantity!="" && rec.data.isAutoAssembly && rec.data.type == "Inventory Assembly"){
                    Wtf.Ajax.requestEx({
                        url: "ACCReports/getPriceCalculationForAsseblyProduct.do",
                        params: {
                            productid: rec.data.productid,
                            buildquantity: rec.data.dquantity
                        }
                    }, this, function(res,req) {
                        var bomValuationArray = [];
                        for (var i=0; i<res.valuationArray.length; i++) {
                            var rowObject = new Object();
                            var bomRec = res.valuationArray[i];
                            rowObject['productid'] = bomRec.productid;
                            rowObject['buildcost'] = bomRec.buildcost;
                            bomValuationArray.push(rowObject);
                        }
                        rec.data.bomValuationArray = JSON.stringify(bomValuationArray);
                        this.save();
                    },function(res,req){

                        });
                }else{
                   this.save(); 
                }
            },this);
        }else{
            this.save();
        }
    },
    
    save:function(){
       var incash=false;
       this.Number.setValue(this.Number.getValue().trim());
       //this.billTo.setValue(this.billTo.getValue().trim());
       var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
       if(this.NorthForm.getForm().isValid() && isValidCustomFields){
            for(var i=0;i<this.Grid.getStore().getCount()-1;i++){// excluding last row
                var quantity=this.Grid.getStore().getAt(i).data['quantity'];
                if(quantity == '' || quantity == undefined || quantity<=0){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.AQuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.shouldbegreaterthanZero")], 2);
                    this.enableSaveButtons();
                    return;
                } 
                var dquantity=this.Grid.getStore().getAt(i).data['dquantity'];
                if(dquantity == '' || dquantity == undefined || dquantity<=0){
                    (this.isCustomer)?WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.DeliveredQuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.shouldbegreaterthanZero")], 2):WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.DQuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.shouldbegreaterthanZero")], 2);
                    this.enableSaveButtons();
                    return;
                }
                
                if(Wtf.account.companyAccountPref.unitPriceConfiguration){
                    var rate=this.Grid.getStore().getAt(i).data['rate'];
                    if(rate===""||rate==undefined||rate<0){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.RateforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.cannotbeempty")], 2);
                        this.enableSaveButtons();
                        return;
                    }
                }
            }
            var count=this.Grid.getStore().getCount();
            if(count<=1){
                WtfComMsgBox(33, 2);
                this.enableSaveButtons();
                return;
            }
//            if(this.getDiscount()>this.Grid.calSubtotal()){  ***************** Check for delivered quantity greater than actual Quantity *********8
//                WtfComMsgBox(12, 2);
//                return;
//            }
            incash=this.cash;
            var rec=this.NorthForm.getForm().getValues();
            rec.taxid=this.Tax.getValue();
            this.ajxurl = "";
            if(this.businessPerson=="Customer") {
                this.ajxurl = "ACCInvoice/saveDeliveryOrder.do";            
            } else {
                this.ajxurl = "ACCGoodsReceipt/saveGoodsReceiptOrder.do";            
            }
           
            var detail = this.Grid.getProductDetails();
            var validLineItem = this.Grid.checkDetails(this.Grid);
            if (validLineItem != "" && validLineItem != undefined) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), (WtfGlobal.getLocaleText("acc.msgbox.lineitem") + validLineItem)], 2);
                this.enableSaveButtons();
                return;
            }
            if(detail == undefined || detail == "[]"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
                this.enableSaveButtons();
                return;
            }
            var prodLength=this.Grid.getStore().data.items.length;
            for(var i=0;i<prodLength-1;i++)
            { 
                var prodID=this.Grid.getStore().getAt(i).data['productid'];
                var prorec=this.Grid.productComboStore.getAt(this.Grid.productComboStore.find('productid',prodID));
                  if(prorec==undefined){
                    prorec=this.Grid.getStore().getAt(i);
                }
                 if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory){ //if company level option is on then only check batch and serial details
                    if(prorec.data.isBatchForProduct || prorec.data.isSerialForProduct || prorec.data.isLocationForProduct || prorec.data.isWarehouseForProduct || prorec.data.isRowForProduct || prorec.data.isRackForProduct  || prorec.data.isBinForProduct){ 
                    if(prorec.data.type!='Service' && prorec.data.type!='Non-Inventory Part'){
                        var batchDetail= this.Grid.getStore().getAt(i).data['batchdetails'];
                        var productQty= this.Grid.getStore().getAt(i).data['dquantity'];
                        var baseUOMRateQty= this.Grid.getStore().getAt(i).data['baseuomrate'];
                        var isLooseSellingBlockedProduct= this.Grid.getStore().getAt(i).data['blockLooseSell'];
                       
                        if(batchDetail == undefined || batchDetail == "" || batchDetail=="[]"){
//                             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);   //Batch and serial no details are not valid.
//                             this.enableSaveButtons();
                            var validLineItem=this.Grid.checkbatchDetails(this.Grid);
                            if(validLineItem!="" && validLineItem!=undefined){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),(WtfGlobal.getLocaleText("acc.invoice.bsdetail")+validLineItem)],2); 
                                this.enableSaveButtons();
                                return;
                            }
                            return;
                        }else{
                                     var jsonBatchDetails= eval(batchDetail);
                                     var batchQty=0;
                                     for(var batchCnt=0;batchCnt<jsonBatchDetails.length;batchCnt++){
                                         if(jsonBatchDetails[batchCnt].quantity>0){
                                             if(prorec.data.isSerialForProduct){
                                              batchQty=batchQty+ parseInt(jsonBatchDetails[batchCnt].quantity);
                                           }else{
                                              batchQty=batchQty+ parseFloat(jsonBatchDetails[batchCnt].quantity);
                                          }
                                         }
                                     }
                                     
                                     var comparisionQty = 0;
                                     
                                     if(isLooseSellingBlockedProduct){
                                         comparisionQty = productQty;
                                     }else{
                                         comparisionQty = productQty*baseUOMRateQty;
                                     }
                                     
                                     if((batchQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) != (comparisionQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)){
                                //                                         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);
                                //                                         this.enableSaveButtons();
                                //                                         return;
                                var validLineItem=this.Grid.checkBatchDetailQty(this.Grid);
                                if(validLineItem!="" && validLineItem!=undefined){
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),("quantity does not match with batch quantity  "+validLineItem)],2); 
                                    this.enableSaveButtons();
                                    return;
                                }
                            }                       
                        }
                    }
                }
            }
            var quantity=this.Grid.getStore().getAt(i).data['quantity'];
            if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') { // serial no for only inventory type of product
                if (prorec.data.isSerialForProduct) {
                    var v = quantity;
                    v = String(v);
                    var ps = v.split('.');
                    var sub = ps[1];
                     if (sub!=undefined && sub.length > 0) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                        this.enableSaveButtons();
                        return;
                    }
                }
            }
            
            var dquantity=this.Grid.getStore().getAt(i).data['dquantity'];
                if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') { // serial no for only inventory type of product
                    if (prorec.data.isSerialForProduct) {
                        var v = dquantity;
                        v = String(v);
                        var ps = v.split('.');
                        var sub = ps[1];
                        if (sub!=undefined && sub.length > 0) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                            this.enableSaveButtons();
                            return;
                        }
                    }
                }
            }
            
            var soselectValue=this.PO.getValue();
            if(this.isCustomer && soselectValue!=undefined&& soselectValue!=""){// for delivery order
                
                var poindex=this.POStore.findBy( function(rec){
                    var parentname=rec.data['billid'];
                    if(parentname==soselectValue)
                        return true;
                    else
                        return false;
                });
                if(poindex>=0) {
                    var soRec= this.POStore.getAt(poindex);
                    var datamsg = "";
                    if(this.fromLinkCombo.getValue()==0){// if linked from so
                        datamsg = WtfGlobal.getLocaleText("acc.msgbox.salescontractalertso");
                    }else if(this.fromLinkCombo.getValue()==1){// if linked from ci
                        datamsg = WtfGlobal.getLocaleText("acc.msgbox.salescontractalertci");
                    }
                    if(soRec.data.contractstatus==2){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),datamsg], 3);
                        this.enableSaveButtons();
                        return false;
                    }
                }
            }
                //check if is there duplicate product in transaction
            var isDuplicate=false;
            var duplicateval=", ";
            if(Wtf.account.companyAccountPref.isDuplicateItems){
                var prodLength=this.Grid.getStore().data.items.length;
                for(var i=0;i<prodLength-1;i++)
                { 
                    var prodID=this.Grid.getStore().getAt(i).data['productid'];
                    for(var j=i+1;j<prodLength-1;j++){
                        var productid=this.Grid.getStore().getAt(j).data['productid'];
                        if(prodID==productid){
                            isDuplicate = true;
                               prorec=this.Grid.getStore().getAt(this.Grid.getStore().find('productid',prodID));
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
            if(Wtf.account.companyAccountPref.negativestock==2 && isDuplicate==true){//Warn case and duplicate product case
                confirmMsg = duplicateval+" "+WtfGlobal.getLocaleText("acc.field.duplicateproduct")+" and "+WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed");
            } else if(Wtf.account.companyAccountPref.negativestock==2) {//Warn case
                confirmMsg = WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed");
            } else if(isDuplicate==true) {//duplicate product case
                confirmMsg =duplicateval+" "+ WtfGlobal.getLocaleText("acc.field.duplicateproduct")+". "+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed");
            }
            if(rec.fromLinkCombo!=undefined  && this.moduleid==27)  //in link case check available quantity should be greater than delivered quantity
            {
                var prodLength=this.Grid.getStore().data.length;
                for(var i=0;i<prodLength;i++)
                {
                    var prodID=this.Grid.getStore().getAt(i).data['productid'];
                    var prorec=this.Grid.productComboStore.getAt(this.Grid.productComboStore.find('productid',prodID));
                    if(prorec != undefined)
                    {
                        var prodName=prorec.data.productname;
                        var availableQuantity = prorec.data.quantity;
                        var lockQuantity = prorec.data.lockquantity;
                        var quantity= this.Grid.getStore().getAt(i).data['dquantity'];
                        var soLockFlag=this.Grid.getStore().getAt(i).data['islockQuantityflag']
                        if(rec.fromLinkCombo="Sales Order" && soLockFlag==true)  ///for DO which is linked with salesorder which is locked
                        {  
                            var soLockQuantity=this.Grid.getStore().getAt(i).data['lockquantity'];
                            if((availableQuantity-(lockQuantity-soLockQuantity))<quantity){
                                if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prodName+WtfGlobal.getLocaleText("acc.field.is")+(availableQuantity-lockQuantity)+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                                    this.enableSaveButtons();
                                    return true;
                                }else if(confirmMsg!=""){     // Warn Case
                                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),confirmMsg+'</center>' , function(btn){
                                        if(btn=="yes"){
                                            this.showConfirmAndSave(rec,detail,incash);
                                            return;
                                        }else{
                                            this.enableSaveButtons();
                                            return true;
                                        }
                                    },this);
                                    return;
                                }       
                            }else{
                                this.showConfirmAndSave(rec,detail,incash); 
                                return
                            }
                        }else  if((availableQuantity-lockQuantity)<quantity){  //for DO for linked with SO which is not linked and for Invoice
                            if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prodName+WtfGlobal.getLocaleText("acc.field.is")+(availableQuantity-lockQuantity)+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                                this.enableSaveButtons();
                                return true;
                            }else if(confirmMsg!=""){     // Warn Case
                                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),confirmMsg+'</center>' , function(btn){
                                    if(btn=="yes"){
                                        this.showConfirmAndSave(rec,detail,incash);
                                        return ;  //
                                    }else{
                                        this.enableSaveButtons();
                                        return true;
                                    }
                                },this);
                            return;
                        }else{  //in ingnore case directly save the record
                                this.showConfirmAndSave(rec,detail,incash);  
                                return ;
                            }
                        }
                                }
                        }
                if(prodLength>0 || isDuplicate == true){  //in case of all product delivered quantity is available then directly save transaction
                if(isDuplicate == true){
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),duplicateval+" "+ WtfGlobal.getLocaleText("acc.field.duplicateproduct")+". "+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                        if(btn=="yes"){
                    this.showConfirmAndSave(rec,detail,incash);  
                            return ;  //
                        }else{
                            this.enableSaveButtons();
                            return true;
                }
                    },this);
                    return;
                }else if(prodLength>0){
                    this.showConfirmAndSave(rec,detail,incash);  
                }
            }
                
            }else   //if DO is made normal without linking
            {
            if(isDuplicate == true){
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),duplicateval+" "+ WtfGlobal.getLocaleText("acc.field.duplicateproduct")+". "+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                    if(btn=="yes"){
                        this.showConfirmAndSave(rec,detail,incash);
                        return ;  //
                }else{
                        this.enableSaveButtons();
                        return true;
                    }
                },this);
                return;
            }else{
                    this.showConfirmAndSave(rec,detail,incash);
                }
            }
            
        }else{
           this.enableSaveButtons();
           WtfComMsgBox(2, 2);
        } 
    },   
    
    checklastproduct:function(rec,detail,incash,count){
         if(this.Grid.getStore().getAt(count-1).data['pid']!="" && this.Grid.getStore().getAt(count-1).data['productid']==""){
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.common.productWithSpecifiedId")+" "+this.Grid.getStore().getAt(count-1).data['pid']+" "+WtfGlobal.getLocaleText("acc.common.productDoesNotExistsOrInDormantState")+". "+WtfGlobal.getLocaleText("acc.accPref.productnotFoundonSave")+'</center>' ,function(btn){
                    if(btn=="yes") {
                        this.showConfirmAndSave(rec,detail,incash);
                    }else{
                        this.enableSaveButtons();
                        return;
                    } 
                },this);                
         }else{
             this.showConfirmAndSave(rec,detail,incash);
         } 
         
    },
    
    showConfirmAndSave: function(rec,detail,incash){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.invoice.msg7"),function(btn){
                if(btn!="yes") {
                    this.saveOnlyFlag=false;
                    this.enableSaveButtons();
                    return;
                }
                rec.detail=detail;
                rec.externalcurrencyrate=this.externalcurrencyrate;
                this.msg= WtfComMsgBox(27,4,true);
                //rec.currencyid=this.Currency.getValue();
                rec.number=this.Number.getValue();
                rec.batchDetails=this.Grid.batchDetails;
                rec.statuscombo=this.DOStatusCombo.getValue();
                rec.fromLinkCombo=this.fromLinkCombo.getRawValue();
                if(this.Grid != undefined && this.Grid.deleteStore!=undefined && this.Grid.deleteStore.data.length>0){  //for geting store to delete.
                    rec.deletedData = this.getJSONArray(this.Grid.deleteStore,false,0);
                }
                var custFieldArr=this.tagsFieldset.createFieldValuesArray();
                if (custFieldArr.length > 0)
                    rec.customfield = JSON.stringify(custFieldArr);
                rec.linkNumber=(this.PO != undefined && this.PO.getValue()!="")?this.PO.getValue():"";
                rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
                rec.shipdate=WtfGlobal.convertToGenericDate(this.shipDate.getValue());
                rec.doid=this.copyInv?"":this.billid;                
                rec.mode=(this.isOrder?41:11);
                rec.incash=incash;
                rec.isfavourite=false;
                rec.posttext=this.postText;
                var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):false;
                rec.sequenceformat=this.sequenceFormatCombobox.getValue();
                if(!this.copyInv){
                    if((this.record && this.record !== undefined) && (this.record.get('isfavourite') !== null || this.record.get('isfavourite') !== undefined)){
                        rec.isfavourite = this.record.get('isfavourite');
                    }
                }
                rec.currencyid=this.Currency.getValue();
                if (!Wtf.account.companyAccountPref.deliveryPlanner && this.moduleid == Wtf.Acc_Delivery_Order_ModuleId) {
                    rec.driver = this.driver.getValue();
                }
                rec.isEdit=this.isEdit;
                rec.copyInv=this.copyInv;
                rec.moduleid=this.moduleid;
            var isCopy = this.copyInv;
            var isEdit = this.isEdit;
            if (this.isVenOrCustSelect) {
                isEdit = false;
                isCopy = false;
            }
                rec=WtfGlobal.getAddressRecordsForSave(rec,this.record,this.linkRecord,this.currentAddressDetailrec,this.isCustomer,this.singleLink,isEdit,isCopy);
                WtfGlobal.setAjaxTimeOut();
                Wtf.Ajax.requestEx({
                    url:this.ajxurl,
                    params: rec                    
                },this,this.genSuccessResponse,this.genFailureResponse);
                },this);
    },
    
    genSuccessResponse:function(response, request){
        WtfGlobal.resetAjaxTimeOut();
        this.enableSaveButtons();   
        if(response.success){
            if(this.moduleid==Wtf.Acc_Delivery_Order_ModuleId && Wtf.getCmp("DeliveryOrderListEntry") != undefined ){
                var title = this.titlel;//scope not available in on load function of store
                Wtf.getCmp("DeliveryOrderListEntry").Store.on('load', function() {
                    WtfComMsgBox([title,response.msg],response.success*2+1);
                }, Wtf.getCmp("DeliveryOrderListEntry").Store, {
                    single : true
                });

            }else {
                WtfComMsgBox([this.titlel,response.msg],response.success*2+1);
            }
            if(this.productOptimizedFlag==Wtf.Show_all_Products){
                Wtf.productStoreSales.reload();
                Wtf.productStore.reload(); 
            }  
            
            var rec=this.NorthForm.getForm().getValues();
            this.exportRecord=rec;
            this.exportRecord['billid']=response.billid||response.invoiceid;
            this.exportRecord['billno']=response.billno||response.invoiceNo;
            this.exportRecord['amount']=response.amount||"";
            this.exportRecord['isNoteAlso']=this.isNoteAlso; 
            if(this.singlePrint){
                this.singlePrint.exportRecord=this.exportRecord;//Reload all product information to reflect new quantity, price etc       
            }
            if (this.singleRowPrint) {
                this.singleRowPrint.exportRecord = this.exportRecord;      
            }
            if(this.mailFlag){
                this.loadUserStore(response, request);
                this.disableComponent();
//                Wtf.getCmp("emailbut" + this.id).enable();
//                Wtf.getCmp("exportpdf" + this.id).enable();
                this.response = response;
                this.request = request;
                return;
            }
            this.currentAddressDetailrec="";
            this.singleLink = false;
            this.isVenOrCustSelect=false;
            this.Grid.getStore().removeAll();
            this.fromLinkCombo.disable();
            this.fromPO.disable();
            this.PO.setDisabled(true);
            this.NorthForm.getForm().reset();
            this.setTransactionNumber();
            this.sequenceFormatStore.load();
            this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
            this.Grid.updateRow(null);
            this.fromPO.setValue(false); 
//            this.Grid.priceStore.purgeListeners();
//            this.Grid.loadPriceStoreOnly(new Date(),this.Grid.priceStore);
            this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{productname:"&nbsp;&nbsp;&nbsp;&nbsp;",qty:0,soqty:0,poqty:0});
            this.fireEvent('update',this);
            this.externalcurrencyrate=0; //Reset external exchange rate for new Transaction.
            this.isClosable= true;       //Reset Closable flag to avoid unsaved Message.
            this.postText="";
            if(!this.mailFlag){//Clear custom columns in save and create new case
                WtfGlobal.resetCustomFields();
            }
         }else{
             if(response.msg!=undefined && response.msg!=''){
                 WtfComMsgBox([this.titlel,response.msg],response.success*2+2);
             }else{
                 WtfComMsgBox([this.titlel,WtfGlobal.getLocaleText("acc.common.msg1")],response.success*2+1);
             }
         }
    },

    genFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        this.enableSaveButtons();
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    removeTransStore:function(){
        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{productname:"&nbsp;&nbsp;&nbsp;&nbsp;",qty:0,soqty:0,poqty:0});
    },
    
    callEmailWindowFunction : function(response, request){
        if(this.CustomStore != null){
            var rec = this.CustomStore.getAt(0);
             var rec = "";
            if (response.billid != undefined || response.billid != '') {
                rec = this.CustomStore.getAt(this.CustomStore.find('billid', response.billid));
            }
            var label = "";
            if(this.isCustomer){
                label = WtfGlobal.getLocaleText("acc.accPref.autoDO");
                callEmailWin("emailwin",rec,label,53,true,false,false,false,false,true);
            }else{
                label = WtfGlobal.getLocaleText("acc.accPref.autoGRO");
                callEmailWin("emailwin",rec,label,54,false,false,false,false,false,true);
            }
        }
    },

    disableComponent: function(){  // disable following component in case of save button press.

        if(this.fromLinkCombo && this.fromLinkCombo.getValue() === ''){
    //       this.fromLinkCombo.emptyText = "";
            this.fromLinkCombo.clearValue();
        }

        if(this.PO && this.PO.getValue() === ''){
            this.handleEmptyText=true;
        //  this.PO.emptyText = "";
            this.PO.clearValue();
        }

        if(this.saveBttn){
            this.saveBttn.disable();
        }
        if(this.savencreateBttn){
            this.savencreateBttn.disable();
        }
        if(this.showAddrress){
            this.showAddrress.disable();
        }
        if(Wtf.getCmp("posttext" + this.id)){
            Wtf.getCmp("posttext" + this.id).disable();
        }
        if(this.Grid){
            var GridStore = this.Grid.getStore();
            var count2 = GridStore.getCount();
            var lastRec2 = GridStore.getAt(count2-1);
            GridStore.remove(lastRec2);
            this.Grid.purgeListeners();
        }

        if(this.NorthForm){
            this.NorthForm.disable();
        }
        if(this.southPanel){
            this.southPanel.disable();
        }
    },

    loadUserStore : function(response, request){
        var GridRec = Wtf.data.Record.create ([
            {name:'billid'},
            {name:'companyid'},
            {name:'companyname'},
            {name:'journalentryid'},
            {name:'entryno'},
            {name:'billto'},
            {name:'orderamount'},
            {name:'shipto'},
            {name:'mode'},
            {name:'billno'},
            {name:'date', type:'date'},
            {name:'shipdate', type:'date'},
            {name:'personname'},
            {name:'personemail'},
            {name:'personid'},
            {name:'shipping'},
            {name:'deleted'},
            {name:'memo'},
            {name:'costcenterid'},
            {name:'costcenterName'},
            {name:'statusID'},
            {name:'shipvia'},
            {name:'fob'},
            {name:'status'},
            {name:'withoutinventory',type:'boolean'},
            {name:'isfavourite'},
            {name:'isprinted'},
            {name:'sequenceformatid'},
            {name:'isConsignment'},
            {name:'custWarehouse'},
            {name:'movementtype'},
        ]);

        var StoreUrl = "";
        if(this.businessPerson=="Customer"){
            StoreUrl = "ACCInvoiceCMN/getDeliveryOrdersMerged.do";
        } else {
            StoreUrl = "ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";
        }

        this.CustomStore = new Wtf.data.GroupingStore({
                url:StoreUrl,
                baseParams:{
                    costCenterId: this.CostCenter.getValue(),
                    deleted:false,
                    nondeleted:false,
                    consolidateFlag:false,
                    enddate:'',
                    pendingapproval:this.pendingapproval,
                    startdate:'',
                    companyids:companyids,
                    gcurrencyid:gcurrencyid,
                    isfavourite:false,
                    userid:loginid,
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
                },GridRec)
            });

            this.CustomStore.on('load', this.enableButtons(), this);

            this.CustomStore.load();

    },

    enableButtons : function(){
        Wtf.getCmp("emailbut" + this.id).enable();
        Wtf.getCmp("exportpdf" + this.id).enable();
        Wtf.getCmp("printSingleRecord" + this.id).enable();
    },

    enableSaveButtons:function(){
        this.saveBttn.enable();
        this.savencreateBttn.enable();
    },
    
    disableSaveButtons:function(){
        this.saveBttn.disable();
        this.savencreateBttn.disable(); 
    },
    
    exportPdfFunction : function(){
        if(this.CustomStore != null){
            var rec = this.CustomStore.getAt(0);
            var recData = rec.data;
            var selRec = "&amount="+0+"&bills="+recData.billid;
            var fileName = "";
            var mode = "";
            if(this.isCustomer){
                fileName = "Delivery Order "+recData.billno;
                mode = 53;
            }else{
                fileName="Goods Receipt "+recData.billno;
                mode = 54;
            }
            Wtf.get('downloadframe').dom.src = "ACCExportRecord/exportRecords.do?mode="+mode+"&rec="+selRec+"&personid="+recData.personid+"&filename="+fileName+"&filetype=pdf";
        }
    },

    getJSONArray:function(store, includeLast, idxArr){
        var indices="";
        if(idxArr)
            indices=":"+idxArr.join(":")+":";        
        var arr=[];
        var fields=store.fields;
        var len=store.getCount();
        //if(includeLast)len++;
        
        for(var i=0;i<len;i++){
            if(idxArr&&indices.indexOf(":"+i+":")<0) continue;
            var rec=store.getAt(i);
            var recarr=[];
            for(var j=0;j<fields.length;j++){
                var value=rec.data[fields.get(j).name];
                switch(fields.get(j).type){
                    case "auto":if(value!=undefined){value=(value+"").trim();}value=encodeURI(value);value="\""+value+"\"";break;
                    case "date":value="'"+WtfGlobal.convertToGenericDate(value)+"'";break;
    }
                recarr.push(fields.get(j).name+":"+value);
            }
            recarr.push("modified:"+rec.dirty);
            arr.push("{"+recarr.join(",")+"}");
        }
        return "["+arr.join(',')+"]";
    },
    
    getPostTextToSetPostText: function() {
        Wtf.Ajax.requestEx({
            url: "ACCCommon/getPDFTemplateRow.do",
            params: {
                module: this.moduleid
            }
        }, this, function(response) {
            if (response.success) {
                this.postText = response.posttext;
            }
        });
    },
    
    enabletax:function(c,rec){
        if(rec.data['value']==true)
            this.Tax.enable();   
        else{
            this.Tax.disable();
            this.Tax.setValue("");
        }
        this.updateSubtotal();
    },
    
    includeProTaxHandler : function(c,rec,val){
        if(this.includeProTax.getValue() == true){
            this.isTaxable.setValue(false);
            this.isTaxable.disable();
            this.Tax.setValue("");
            this.Tax.disable();
        }else{
            this.isTaxable.reset();
            this.isTaxable.enable();
        }
        if(Wtf.account.companyAccountPref.unitPriceConfiguration) {
            this.showGridTax(c,rec,val);
        }
    },
    
    showGridTax:function(c,rec,val){
        var hide=(val==null||undefined?!rec.data['value']:val) ;
        var id=this.Grid.getId()
        var rowtaxindex=this.Grid.getColumnModel().getIndexById(id+"prtaxid");
        var rowtaxamountindex=this.Grid.getColumnModel().getIndexById(id+"taxamount");
        this.Grid.getColumnModel().setHidden( rowtaxindex,hide) ;
        this.Grid.getColumnModel().setHidden( rowtaxamountindex,hide) ;
        var rowRateIncludingGstAmountIndex=this.Grid.getColumnModel().getIndexById(id+"rateIncludingGst");
        var rowprDiscountIndex=this.Grid.getColumnModel().getIndexById(id+"prdiscount");
        var rowDiscountIsPercentIndex=this.Grid.getColumnModel().getIndexById(id+"discountispercent");
        var rowRateAmountIndex=this.Grid.getColumnModel().getIndexById(id+"rate");
        if(rowprDiscountIndex!=-1&&rowDiscountIsPercentIndex!=-1&&rowRateIncludingGstAmountIndex!=-1){
            if(this.includingGST.getValue()){
                this.Grid.getColumnModel().setHidden(rowRateIncludingGstAmountIndex,!this.includingGST.getValue());
                this.Grid.getColumnModel().getColumnById(id+"rate").editable=false;
                this.Grid.getColumnModel().setHidden(rowprDiscountIndex,!hide);
                this.Grid.getColumnModel().setHidden(rowDiscountIsPercentIndex,!hide);
            }else if(!this.Grid.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden){
                this.Grid.getColumnModel().getColumnById(id+"rate").editable=true;
                this.Grid.getColumnModel().setHidden(rowRateIncludingGstAmountIndex,!this.includingGST.getValue());
                this.Grid.getColumnModel().setHidden(rowprDiscountIndex,hide);
                this.Grid.getColumnModel().setHidden(rowDiscountIsPercentIndex,hide);
            }
        }
        
        this.Grid.getStore().each(function(rec){
            if(this.includeProTax && this.includeProTax.getValue() == true
                && (rec.data.prtaxid == "" || rec.data.prtaxid == undefined)) {//In Edit, values are resetting after selection Product level Tax value as No
//                if(this.ExpenseGrid && this.ExpenseGrid.isVisible()) {//(!this.isCustBill && !(this.isEdit && !this.isOrder) && !(this.isCustomer||this.isOrder))
//                    var index=this.ExpenseGrid.accountStore.find('accountid',rec.data.accountid);
//                    var taxid = index > 0 ? this.ExpenseGrid.accountStore.getAt(index).data["acctaxcode"]:"";
//                    var taxamount = this.ExpenseGrid.setTaxAmountAfterSelection(rec);
//                    rec.set('prtaxid',taxid);
//                    rec.set('taxamount',taxamount);
//                } else {
//                    index=this.ProductGrid.productComboStore.find('productid',rec.data.productid);
//                    var acctaxcode = (this.isCustomer)?"salesacctaxcode":"purchaseacctaxcode";
//                    taxid = index > 0 ? this.ProductGrid.productComboStore.getAt(index).data[acctaxcode]:"";
//                    rec.set('prtaxid',taxid);
//                    taxamount = this.ProductGrid.setTaxAmountAfterSelection(rec);
//                    rec.set('taxamount',taxamount);
//                }
                var taxid = "";
                var taxamount = 0;
                if(!(rec.data.productid == "" || rec.data.productid == undefined)){// for excluding last empty row
                    if(taxid == ""){// if tax is mapped to customer or vendor then it will come default populated
                        var currentTaxItem=WtfGlobal.searchRecord(this.Name.store, this.Name.getValue(), 'accid');
                        var actualTaxId=currentTaxItem!=null?currentTaxItem.get('taxId'):"";

                        if(actualTaxId== undefined || actualTaxId == "" ||  actualTaxId == null){// if customer/vendor is not mapped with tax then check that is their mapping account is mapped with tax or not, if it is mapped take account tax
                            actualTaxId=currentTaxItem!=null?currentTaxItem.get('mappedAccountTaxId'):"";
                        }

                        if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null){
                            taxid = actualTaxId;
                            rec.set('prtaxid',taxid);
                            taxamount = this.Grid.setTaxAmountAfterSelection(rec);
                        }
                    }
                }
                        if(actualTaxId!= undefined && actualTaxId != "" &&  actualTaxId != null){
                            taxid = actualTaxId;
                            rec.set('prtaxid',taxid);
                            taxamount = this.Grid.setTaxAmountAfterSelection(rec);
                        }
                rec.set('prtaxid',taxid);
                rec.set('taxamount',taxamount);
            } else if(this.includeProTax && this.includeProTax.getValue() != true){
                rec.set('prtaxid','');
                rec.set('taxamount',0);
            }
            
            if(this.includingGST&&this.includingGST.getValue()){
                rec.set('discountispercent',1);
                rec.set('prdiscount',0);
                rec.set('rateIncludingGst',rec.get('rate'));
                rec.set('rateIncludingGst',rec.get('taxamount'));
                var taxamount= 0;
                var unitAmount= 0;
                var unitTax= 0;
                var unitVal= 0;
                var amount=rec.get('rate')!=null?getRoundedAmountValue(rec.get('rate')):0;
                var quantity=rec.get('quantity')!=null?getRoundofValue(rec.get('quantity')):0;
                var tax=rec.get('taxamount')!=null?getRoundofValue(rec.get('taxamount')):0;
                if(quantity!=0){
                    unitAmount=getRoundedAmountValue(amount);
                    unitTax=getRoundedAmountValue(tax/quantity);
                }
                if(unitAmount+unitTax!=0){
                      rec.set('rateIncludingGst',unitAmount+unitTax);
                }else{
                      rec.set('rateIncludingGst',rec.get('rate'));
                }
            }else if(rowRateIncludingGstAmountIndex!=-1&&this.Grid.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden && rec.data.prdiscount==0)//if column unit price column is hidden. Works for all case except when include gst is checked.
                {
                    rec.set('discountispercent',1);
                    rec.set('prdiscount',0);
                    rec.set('rateIncludingGst',0);
            }
        },this);
//         if(hide)
        this.updateSubtotal();
    },
    
    callGSTCurrencyRateandUpdateSubtotal:function(a,val){
        if(WtfGlobal.singaporecountry()&&WtfGlobal.getCurrencyID()!=Wtf.Currency.SGD&&this.isInvoice && this.Grid.forCurrency!=Wtf.Currency.SGD){
            callGstCurrencyRateWin(this.id,"SGD ",undefined,this.gstCurrencyRate);
        }
        this.updateSubtotal(a,val);
    },
    
    updateSubtotal: function(a,val) {
        this.applyCurrencySymbol();
        var aftertaxamt=0.00;
        var tax=0.00;
        var taxAndSubtotal=this.Grid.calLineLevelTax();
        if(this.includeProTax.getValue()){
            if (this.record && this.record.json && this.record.json.isTaxRowLvlAndFromTaxGlobalLvl) {
                tax = (this.moduleid == 28 || this.moduleid ==27)? WtfGlobal.addCurrencySymbolOnly(this.Grid.calLineLevelTaxNew(),this.symbol) : WtfGlobal.addCurrencySymbolOnly(this.Grid.calTaxtotal(),this.symbol);
                aftertaxamt = (this.moduleid == 28 || this.moduleid ==27) ? WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal()+this.Grid.calLineLevelTaxNew(),this.symbol):WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal()+this.Grid.calTaxtotal(),this.symbol);
            } else {
                aftertaxamt=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[0],this.symbol)
                tax=WtfGlobal.addCurrencySymbolOnly(taxAndSubtotal[1],this.symbol);
            }
        }else{
            aftertaxamt=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal() + this.caltax(),this.symbol)
            tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
        }
        this.tplSummary.overwrite(this.southCalTemp.body,{
            subtotal:WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),
            tax:tax,
            aftertaxamt:aftertaxamt,
            totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol()),
            amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())
        });
    },
    
    caltax:function(){
        var totalamount=this.calTotalAmount();
        var rec= this.Grid.taxStore.getAt(this.Grid.taxStore.find('prtaxid',this.Tax.getValue()));
        var totalterm = 0;
        
        var taxamount=0;
        if(rec!=null){
            totalamount=getRoundedAmountValue(this.calTotalAmount());
            
            taxamount=((totalamount+totalterm)*rec.data["percent"])/100;
        }
//        var taxamount=(rec==null?0:(totalamount*rec.data["percent"])/100);
        return getRoundedAmountValue(taxamount);
    },
     
    calTotalAmount:function(){
        var subtotal=this.Grid.calSubtotal();
//        var discount=this.getDiscount();
//        return subtotal-discount + this.findTermsTotal();
        return subtotal;
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
            moduleid: this.moduleid,
            columnHeader:this.fromLinkCombo.getRawValue(),
            invoice: this,
            storeBaseParams: this.POStore.baseParams,
            storeParams: this.POStore.lastOptions.params,
            PORec: this.PORec
        });
        this.PONumberSelectionWin.show();
    }
});