
Wtf.account.FixedAssetSalesReturnPanel=function(config){	
    this.modeName = config.modeName;
    Wtf.apply(this, config);
    this.sendMailFlag = false;
    this.mailFlag = false;
    this.saveandCreateFlag = false;
    this.response="";
    this.exportRecord="";
    this.request="";
    this.CustomStore="";
    this.moduleid=config.moduleid;
    this.salesPurchaseReturnflag = false;
    var help=getHelpButton(this,config.heplmodeid);
    this.businessPerson=this.isCustomer?'Customer':"Vendor";
    this.isFixedAsset = (config.isFixedAsset)? config.isFixedAsset : false;
    this.isLeaseFixedAsset=config.isLeaseFixedAsset?config.isLeaseFixedAsset:false;
    this.uPermType=this.isFixedAsset?(config.isCustomer?Wtf.UPerm.assetsalesreturn:Wtf.UPerm.assetpurchasereturn):config.isLeaseFixedAsset?Wtf.UPerm.leaseorder:config.isCustomer?Wtf.UPerm.assetsales:Wtf.UPerm.assetpurchase;
    this.permType=this.isFixedAsset?(config.isCustomer?Wtf.Perm.assetsalesreturn:Wtf.Perm.assetpurchasereturn):config.isLeaseFixedAsset?Wtf.Perm.leaseorder:config.isCustomer?Wtf.Perm.assetsales:Wtf.Perm.assetpurchase;
    this.exportPermType=this.isFixedAsset?(config.isCustomer?this.permType.exportasret:this.permType.exportapret):config.isLeaseFixedAsset?this.permType.exportlret:true;
    this.printPermType=this.isFixedAsset?(config.isCustomer?this.permType.exportasret:this.permType.exportapret):config.isLeaseFixedAsset?this.permType.exportlret:true;
    this.record=config.record;
    this.readOnly=config.readOnly;
    this.originallyLinkedDocuments = '';
    this.nameBeforeSelect="";
     /*
     * Assign the value to the optimized flag as per System preferences.
     */
    this.custVenOptimizedFlag = Wtf.account.companyAccountPref.custvenloadtype;
    this.personRec = new Wtf.data.Record.create ([
    {
        name:'accid'
    },

    {
        name:'accname'
    },
    {
        name:'acccode'
    },

    //        {name: 'level'},

    {
        name: 'termdays'
    },

    {
        name: 'billto'
    },

    {
        name: 'currencysymbol'
    },

    {
        name: 'currencyname'
    },

    {
        name: 'currencyid'
    },

    {
        name:'deleted'
    },
    {
        name:'hasAccess'
    }
    ]);
    var tranType= (this.isCustomer)?Wtf.autoNum.SalesReturn:Wtf.autoNum.PurchaseReturn;
    this.customerAccStore =  new Wtf.data.Store({
        //    url:Wtf.req.account+'CustomerManager.jsp',        
        url:this.isCustomer?"ACCCustomer/getCustomersForCombo.do":"ACCVendor/getVendorsForCombo.do",
        baseParams:{
            mode:2,
            group:this.isCustomer?10:13,
            deleted:false,
            nondeleted:true,
            combineData:this.isCustomer?1:-1  //Send For Seprate Request
        },
        reader: new  Wtf.data.KwlJsonReader({
            root: "data",
            autoLoad:false
        },this.personRec)
    });
if (!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)) {
    this.printRecords = new Wtf.exportButton({
        obj: this,
        id: "printSingleRecord"+ this.id,
        iconCls: 'pwnd printButtonIcon',
        text: WtfGlobal.getLocaleText("acc.rem.236"),
        tooltip: WtfGlobal.getLocaleText("acc.rem.236.single"), //'Print Single Record Details',
        disabled: true,
        hidden:this.readOnly,
        isEntrylevel: false,
        exportRecord:this.exportRecord,
        menuItem: {
            rowPrint: true
        },
        get: tranType,
        moduleid:Wtf.Acc_Lease_Return
    });
}
    if (!this.custVenOptimizedFlag) {
        this.isCustomer ? chkcustaccload() : chkvenaccload();
    }
    
    if(this.isSalesFromDo){
        this.customerAccStore.load();
    }
    Wtf.apply(this,{
        bbar:[{
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
            tooltip:WtfGlobal.getLocaleText("acc.rem.175"),
            id:"save"+this.id,
            scope:this,
            hidden:this.readOnly,
            disabled:(this.contractStatus==3), 
            handler:function(){
                this.mailFlag = true;
                this.save();
            },
            iconCls :'pwnd save'
        },{
            text:WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
            tooltip:WtfGlobal.getLocaleText("acc.field.SaveAndCreateNewToolTip"),
            id:"savencreate"+config.heplmodeid+this.id,
            scope:this,
             disabled:(this.contractStatus==3), 
            hidden : this.isEdit || this.copyInv,
            handler:function(){
                this.saveandCreateFlag=true;
                this.mailFlag = false;
                this.save();
            },
            iconCls :'pwnd save'
        },{
        text:WtfGlobal.getLocaleText("acc.common.email"),  // "Email",
        tooltip : WtfGlobal.getLocaleText("acc.common.emailTT"),  //"Email",
        id: "emailbut" + this.id,
        scope: this,
        disabled : true,
        hidden:this.readOnly,
        handler: function(){this.callEmailWindowFunction(this.response, this.request)},
        iconCls: "accountingbase financialreport"
        },  !WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)?{
            text: WtfGlobal.getLocaleText("acc.field.ExportPDF"),
            tooltip : WtfGlobal.getLocaleText("acc.field.ExportPDFFile"),
            scope:this,
            id:"exportpdf" + this.id,
            iconCls: 'pwnd exportpdf1',
            hidden:this.readOnly,
            disabled : true,
            handler: function(){this.exportPdfFunction()}
        }:"",this.printRecords,{
            text:  WtfGlobal.getLocaleText("acc.template.posttext") , //'<b>Post Text</b>',
            cls: 'pwnd add',
             disabled:(this.contractStatus==3), 
            id: "posttext" + this.id,              // Post Text
            //hidden:(config.moduleid!=Wtf.Acc_Invoice_ModuleId && config.moduleid!=Wtf.Acc_Vendor_Invoice_ModuleId),        
            tooltip : WtfGlobal.getLocaleText("acc.field.UsePostTextoptiontoinserttextafterSignature"),        
            style:" padding-left: 15px;",
            scope: this,
            hidden:this.readOnly,
            handler: function() {
                this.getPostTextEditor(this.postText);
            } 
        },{
            text:  WtfGlobal.getLocaleText("acc.common.close") , //'<b>Close</b>',
            cls: 'pwnd add',
            id: "posttext" + this.id,              // Close
            hidden:(this.contractStatus==2 ||this.contractStatus==4 || this.contractStatus == undefined || this.contractid == undefined) ||this.readOnly,        
            tooltip : WtfGlobal.getLocaleText("acc.common.closebtn.Tooltip"),        
            style:" padding-left: 15px;",
            scope: this,
            handler: this.closeContract
        },'->']
      });
    Wtf.account.FixedAssetSalesReturnPanel.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.FixedAssetSalesReturnPanel,Wtf.account.ClosablePanel,{
    autoScroll: true,// layout:'border',//Bug Fixed: 14871[SK]
    bodyStyle: {background:"#DFE8F6 none repeat scroll 0 0"},
    border:'false',
    externalcurrencyrate:0,
    isCurrencyLoad:false,
    currencyid:null,
    custdatechange:false,
    closable : true,
    cash:false,

    loadRecord:function(){
        if(this.record!=null&&!this.dataLoaded){
            var data=this.record.data;
            this.NorthForm.getForm().loadRecord(this.record);
            this.currencyStore.on('load',function () {
                this.Currency.setValue(data.currencyid);
                this.updateFormCurrency();
            },this);
            if(!this.copyInv)
                this.Number.setValue(data.billno); 
            /*
              * In Edit and view case set value to the customer combo-No need to load store 
             */  
            this.Name.setValForRemoteStore(data.personid, data.personname,data.hasAccess); // create record and set value
            
            //Need to check this.
            if(this.Grid.getStore().data.items.length>0){  // for showing multiple link numbers in number field 
                var linkType=-1;
                var storeData = [],linkNumbers=[],linkIDS=[];
                this.POStore.removeAll();
                this.Grid.getStore().each(function(rec){
                    if(this.copyInv) { 
                        rec.data.linkid=""; 
                        rec.data.rowid=""; 
                        rec.data.linktype=linkType;
                        rec.data.linkto="";
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
                                billno:rec.data.linkto,
                                amount:rec.data.amount,    
                                /*
                                *	ERM-1037
                                *	for getting date of linked document to restrict linking of future dated document
                                */
                                date:rec.data.invcreationdate
                            });
                            storeData.push(newRec);
                        }
                    }
                },this);
                if(storeData.length>0){
                    this.POStore.add(storeData);
                }
                if(linkIDS.length>0){
                    
                    if(this.Grid){
                        this.Grid.fromPO=true;
                    }
                    
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

            this.Currency.disable();
            this.Memo.setValue(data.memo);                        
            this.billDate.setValue(data.date);                                                            
            this.CostCenter.setValue(data.costcenterid);
            this.postText = this.record.json.posttext;
            
            this.dataLoaded=true;
            this.Grid.priceStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
            //this.bankTypeStore.load();            
            
//            if(this.copyInv){
//            	this.billDate.setValue(Wtf.serverDate);
////            	this.updateDueDate();
//            }
            
            if (this.Grid) {
                this.Grid.affecteduser = data.personid;
            }
        }
    },
    onRender:function(config){              
        this.add(this.NorthForm,this.Grid);                       
        Wtf.account.FixedAssetSalesReturnPanel.superclass.onRender.call(this, config);
        this.initForClose();
        // hide form fields
            this.hideFormFields();
    },
    hideFormFields:function(){
        if (this.isCustomer) {
            if (this.moduleid == Wtf.Acc_Lease_Return) {
                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.leaseReturn);
            } else {
                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.salesReturn);
            }
        } else {
            this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.purchaseReturn);
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
            columnHeader: this.fromLinkCombo.getRawValue(),
            moduleid: this.moduleid,
            columnHeader:this.fromLinkCombo.getRawValue(),
            invoice: this,
            storeBaseParams: this.POStore.baseParams,
            storeParams: this.POStore.lastOptions.params,
            PORec: this.PORec
        });
        this.PONumberSelectionWin.show();
    },
    hideTransactionFormFields:function(array){
        if(array){
            for(var i=0;i<array.length;i++){
                var fieldArray = array[i];
                if(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id)){
                    Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hideLabel = fieldArray.isHidden;
                    Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hidden = fieldArray.isHidden;
                    
                    if(fieldArray.isUserManadatoryField  && Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel != undefined){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).allowBlank = !fieldArray.isUserManadatoryField;
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel = Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel + " *";
                    }
                }
            }
        }
    },
    initComponent:function(config){
        Wtf.account.FixedAssetSalesReturnPanel.superclass.initComponent.call(this,config);
        
        //chkcustaccload();// Global Customer store  
        this.isCustomer ? chkproductSalesload() : chkproductload() ; // Global Product store for product sales
        
        
        this.loadCurrFlag = true;
        
        this.GridRec = Wtf.data.Record.create ([
            {name:'id'},
            {name:'number'}
        ]);
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
            //        url: Wtf.req.account +'CompanyManager.jsp',
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:this.modeName
            }
        });
        this.sequenceFormatStore.on('load',this.setNextNumber,this);
        this.sequenceFormatStore.load();
     
        var comboConfig = {
            fieldLabel:this.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven") , //this.businessPerson+"*",
            hiddenName:this.businessPerson.toLowerCase(),
            id:"customer"+this.heplmodeid+this.id,
            store: this.isCustomer?Wtf.customerAccStore:Wtf.vendorAccStore,
            valueField:'accid',
            displayField:'accname',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            allowBlank:false,
            hirarchical:true,
            emptyText:this.isCustomer?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven") , //'Select a '+this.businessPerson+'...',
            mode: 'remote',
            typeAheadDelay:30000,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            minChars:1,
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            isVendor:!(this.isCustomer),
            isCustomer:this.isCustomer,
            width:240,
            triggerAction:'all'
        };
        
        if (this.custVenOptimizedFlag) {
            comboConfig['ctCls'] = 'optimizedclass';
            comboConfig['hideTrigger'] = true;
        } 
        this.Name = new Wtf.form.ExtFnComboBox(comboConfig);
        /*
         *setting the respective currency of the customer
         */
        this.Name.on('beforeselect', function(combo, record, index) {
             this.nameBeforeSelect = combo.getValue();
                return validateSelection(combo, record, index);
        }, this);
        this.Name.on('select',this.onNameSelect,this);
        if(this.isSalesFromDo){
        this.customerAccStore.on("load",function(){
            this.Name.setValue(this.dopersonid)
            this.fromPO.setValue(true);
            this.fromLinkCombo.enable();                        
            this.fromOrder=true;
            this.fromLinkCombo.setValue(0);
            this.enableNumber(undefined,this.fromlinkStore.getAt(0));
            this.POStore.on("load",function(){
               
                var index=this.POStore.find("billid",this.dolinkid);
               
                if(index==-1){
                     var custindex=this.customerAccStore.find("accid",this.dopersonid);
                    var custrec=this.POStore.getAt(custindex);
                    var newRec=new this.PORec({
                        billid:this.dolinkid,
                        billno:this.billlink,    
                        personid:this.dopersonid,
                        currencyid:custrec.data.currencyid   
                    });
                    this.POStore.add(newRec);    
                    index=this.POStore.getCount()-1;
                }
                 this.PO.setValue(this.dolinkid);
                this.populateData(undefined,this.POStore.getAt(index));
            },this);
        },this)
    }

            this.Name.addNewFn=this.addPerson.createDelegate(this,[false,null,this.businessPerson+"window",this.isCustomer],true);
        
        this.fromPOStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Yes',true],['No',false]]
        });
        
        
        var fromLinkStoreRec = new Array();
        fromLinkStoreRec.push([this.isCustomer?(this.isLeaseFixedAsset?'Lease Delivery Order':'Asset Delivery Order'):"Asset Goods Receipt", 0]);
//        fromLinkStoreRec.push([this.isCustomer?'Customer Invoice':"Vendor Invoice", 1]);
        
        
        
        
        this.fromlinkStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value'}],
            data:fromLinkStoreRec
        });

this.sequenceFormatCombobox = new Wtf.form.ComboBox({            
//        labelSeparator:'',
//        labelWidth:0,
        triggerAction:'all',
        mode: 'local',
        id:'sequenceFormatCombobox'+this.heplmodeid+this.id,
        fieldLabel:WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
        valueField:'id',
        displayField:'value',
        store:this.sequenceFormatStore,
        disabled:(this.isEdit&&!this.copyInv&&!this.isPOfromSO&&!this.isSOfromPO?true:false),  
        width:240,
        typeAhead: true,
        forceSelection: true,
        name:'sequenceformat',
        hiddenName:'sequenceformat',
        allowBlank : false,
        listeners:{
            'select':{
                fn:this.getNextSequenceNumber,
                scope:this
            }
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
         this.currencyStore.load();

        this.Currency = new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.currency.cur"),  // 'Currency',
            hiddenName:'currencyid',
            id:"currency"+this.heplmodeid+this.id,
            width : 240,
            store:this.currencyStore,
            valueField:'currencyid',
            allowBlank : false,
            forceSelection: true,
            displayField:'currencyname',
            scope:this,
            selectOnFocus:true
        });

        this.currencyStore.on('load',this.changeTemplateSymbol,this);

        this.Currency.on('select', function(){
            this.currencychanged = true;
            this.onCurrencyChangeOnly();
            this.updateFormCurrency();
        }, this);
        
        this.Number=new Wtf.form.TextField({
            fieldLabel:this.isLeaseFixedAsset ? this.label + " " + WtfGlobal.getLocaleText("acc.common.number") : this.label + " " + WtfGlobal.getLocaleText("acc.common.number"),  //,  //this.label+' Number*',
            name: 'number',
            disabled:(this.isEdit&&!this.copyInv?true:false),
            id:"invoiceNo"+this.heplmodeid+this.id,
            width:240,
            //anchor:'50%',
            maxLength:50,
            scope:this,
            allowBlank:this.checkin
        });
        this.Memo=new Wtf.form.TextArea({
            fieldLabel:Wtf.account.companyAccountPref.descriptionType,  //'Memo',
            name: 'memo',
            id:"memo"+this.heplmodeid+this.id,
            height:40,
            anchor:'94%',
            readOnly: this.readOnly,
            disabled:this.readOnly, 
            maxLength:2048,
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
            {name:'amountdue'},
            {name:'costcenterid'},
            {name:'costcenterName'},
            {name:'memo'},
            {name:'posttext'}   
        ]);
        this.POStoreUrl =(this.isCustomer)?"ACCInvoiceCMN/getDeliveryOrdersMerged.do":"ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";
          
          var linkingFlag = false;
        this.POStore = new Wtf.data.Store({
            url:this.POStoreUrl,
            baseParams:{
                mode:42,
                closeflag:true,
                srflag : true,
                linkingFlag :linkingFlag,
                requestModuleid:this.moduleid
                },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.PORec)
        });            
        var DimensionCustomFielsArray = GlobalDimensionCustomFieldModel[this.moduleid];
        if( DimensionCustomFielsArray){
            for(var cnt = 0;cnt < DimensionCustomFielsArray.length;cnt++){
                if(DimensionCustomFielsArray[cnt] != undefined){
                    var fieldname = DimensionCustomFielsArray[cnt].fieldname;
                    var newField = new Wtf.data.Field({
                        name:fieldname.replace(".",""),
                        type:DimensionCustomFielsArray[cnt].fieldtype == 3 ?  'date' : (DimensionCustomFielsArray[cnt].fieldtype == 2?'float':'auto'),
                        format:DimensionCustomFielsArray[cnt].fieldtype == 3 ?  'y-m-d' : undefined
                    });
                    this.POStore.fields.items.push(newField);
                    this.POStore.fields.map[fieldname]=newField;
                    this.POStore.fields.keys.push(fieldname);
                }
            }
            this.POStore.reader = new Wtf.data.KwlJsonReader(this.POStore.reader.meta, this.POStore.fields.items);
        }
        
        this.fromPO= new Wtf.form.ComboBox({
            triggerAction:'all',
            hideLabel:false,
            hidden:false,
            mode: 'local',
            valueField:'value',
            displayField:'name',
            store:this.fromPOStore,
            id: "linkToOrder"+this.heplmodeid+this.id,
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Link"),  //"Link to "+(this.isCustomer?"Sales":"Purchase")+" Order",
            allowBlank:this.isOrder,
            value:false,
            width:50,
            typeAhead: true,
            forceSelection: true,
            name:'prdiscount',
            hiddenName:'prdiscount',
            listeners:{
                'select':{
                    fn:this.enablePO,
                    scope:this
                }
            }
        });
        this.fromLinkCombo= new Wtf.form.ComboBox({
            triggerAction:'all',
            name:"fromLinkCombo",
            hideLabel:false,
            hidden:false,
            mode: 'local',
            valueField:'value',
            displayField:'name',
            disabled:true,
            id:'fromLinkComboId'+this.heplmodeid+this.id,
            store:this.fromlinkStore,                        
            emptyText: (this.isFixedAsset && !this.isCustomer)? WtfGlobal.getLocaleText("acc.field.SelectaGR") : WtfGlobal.getLocaleText("acc.field.SelectaDO"),//this.isCustomer?WtfGlobal.getLocaleText("acc.field.SelectaCI/DO"):WtfGlobal.getLocaleText("acc.field.SelectaVI/GR"),
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Linkto"),  //"Link to "+(this.isCustomer?"Sales":"Purchase")+" Order",
            allowBlank:false,            
//            value:false,            
            typeAhead: true,            
            width:135,
            forceSelection: true,                        
            selectOnFocus:true,           
            scope:this,
            listeners:{
                'select':{
                    fn:this.enableNumber,
                    scope:this
                }
            }
        });
        
//        this.PO= new Wtf.form.FnComboBox({
//            fieldLabel:WtfGlobal.getLocaleText("acc.field.Number") ,  //(this.isCustomer?"SO":"PO")+" Number",
//            hiddenName:"ordernumber",
//            //id:"orderNumber"+this.heplmodeid+this.id,
//            id:"poNumberID"+this.heplmodeid+this.id,
//            allowBlank:false, 
//            store: this.POStore,
//            valueField:'billid',
//            hideLabel:false,
//            hidden:false,
//            displayField:'billno',
//            disabled:true,
//            emptyText: (this.isFixedAsset && !this.isCustomer)? WtfGlobal.getLocaleText("acc.field.SelectaGR") : WtfGlobal.getLocaleText("acc.field.SelectaDO"), //this.isCustomer?WtfGlobal.getLocaleText("acc.field.SelectaCI/DO"):WtfGlobal.getLocaleText("acc.field.SelectaVI/GR"),
//            mode: 'local',
//            typeAhead: true,
//            forceSelection: true,
//            selectOnFocus:true,            
//            width:240,
//            triggerAction:'all',
////            addNewFn:this.addOrder.createDelegate(this,[false,null,this.businessPerson+"PO"],true),
//            scope:this,
//            listeners:{
//                'select':{
//                    fn:this.populateData,
//                    scope:this
//                }
//            }
//        });

         this.MSComboconfig= {
            hiddenName:"ordernumber",
            allowBlank:false, 
            store: this.POStore,
            valueField:'billid',
            hideLabel:false,
            hidden:false,
            displayField:'billno',
            disabled:true,
            clearTrigger:this.isEdit ? false : true,
            emptyText: (this.isFixedAsset && !this.isCustomer)? WtfGlobal.getLocaleText("acc.field.SelectaGR") : WtfGlobal.getLocaleText("acc.field.SelectaDO"),
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,            
            width:240,
            triggerAction:'all',
            scope:this
        };
        
        this.PO = new Wtf.common.Select(Wtf.applyIf({
             multiSelect:true,
             fieldLabel:WtfGlobal.getLocaleText("acc.field.Number") ,
             id:"poNumberID"+this.heplmodeid+this.id ,
             forceSelection:true,
             addCreateOpt:true,
             addNewFn:this.addSelectedDocument.createDelegate(this),
             width:240
        },this.MSComboconfig));
        
        this.PO.on("clearval",function(){
            if(this.PO.getValue()=="" && !this.isEdit){            
                this.Grid.getStore().removeAll(); 
                this.Grid.addBlankRow();      
                var fieldArr = this.POStore.fields.items;
                for(var fieldCnt=0; fieldCnt < fieldArr.length; fieldCnt++) {
                    var fieldN = fieldArr[fieldCnt];
                    if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id)) {
                        Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue('');
                    }
                }
            }
        },this);
        
        if (Wtf.account.companyAccountPref.enableLinkToSelWin && (this.moduleid == Wtf.Acc_FixedAssets_Purchase_Return_ModuleId  || this.moduleid == Wtf.Acc_FixedAssets_Sales_Return_ModuleId || this.moduleid==Wtf.Acc_Lease_Return)) {
            this.POStore.on('load', function(){addMoreOptions(this.PO,this.PORec)}, this);            
            this.POStore.on('datachanged', function(){addMoreOptions(this.PO,this.PORec)}, this);            
        }
        this.POSelected="";
        this.billDate= new Wtf.form.DateField({
            fieldLabel:this.isLeaseFixedAsset ? this.label +' '+WtfGlobal.getLocaleText("acc.invoice.date") : this.label +' '+WtfGlobal.getLocaleText("acc.invoice.date"),
            id:"invoiceDate"+this.heplmodeid+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'billdate',
//            anchor:'50%',
            width:240,
            allowBlank:false
        });
        this.shipDate= new Wtf.form.DateField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.ShipDate.tip") +"'>"+ WtfGlobal.getLocaleText("acc.field.ShipDate")+"</span>",//WtfGlobal.getLocaleText("acc.field.ShipDate"),
            id:"shipdate"+this.heplmodeid+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'shipdate',
            anchor:'94%'
        });
        this.shipvia = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.ShipVia"),
            id:"shipvia"+this.heplmodeid+this.id,
            name: 'shipvia',
            anchor:'94%',
            maxLength: 255,
            scope: this
        });
        
        this.fob = new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.fob.tip")+"'>"+WtfGlobal.getLocaleText("acc.field.FOB") +"</span>",
            name: 'fob',
            id:"fob"+this.heplmodeid+this.id,
            anchor:'94%',
            maxLength: 255,
            scope: this
        });        
        chkFormCostCenterload();
        this.CostCenter=  new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.costCenter.tip") +"'>"+ WtfGlobal.getLocaleText("acc.common.costCenter")+"</span>",//WtfGlobal.getLocaleText("acc.common.costCenter"),//"Cost Center",
            hiddenName:"costcenter",
            id:"costcenter"+this.heplmodeid+this.id,
            store: Wtf.FormCostCenterStore,
            valueField:'id',
            displayField:'name',
            extraComparisionField:'ccid', 
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['ccid']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
            isProductCombo:true,
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
//            anchor:"50%",   
            width:240,
            triggerAction:'all',
            addNewFn:this.addCostCenter,
            scope:this,
            hidden: this.quotation,
            hideLabel: this.quotation
        }); 
         
        var itemArr={};
            itemArr = [this.Name, this.Currency,{
                layout:'column',
                border:false,
                defaults:{border:false},items:[ {
                    layout:'form',
                    ctCls : "",
                    width:215,
                    items:this.fromPO
                },
                {
                    width:250,
                    layout:'form',
                    labelWidth:45,
                    items:this.fromLinkCombo
               }
           ]},this.PO,this.sequenceFormatCombobox,this.Number,this.billDate, this.CostCenter];
   
   this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId:"northForm"+this.id,
            autoHeight: true,
            parentcompId:this.id,
            moduleid: this.moduleid,
            isEdit: this.isEdit,
            record: this.record,
            isViewMode:this.readOnly
        });
      
        this.NorthForm=new Wtf.form.FormPanel({
            region:'north',
            autoHeight: true,
            id:"northForm"+this.id,
            disabledClass:"newtripcmbss",
            //disabled:this.readOnly,
            border:false,
            items:[{
                layout:'form',
                baseCls:'northFormFormat',
                labelWidth:155,
                cls:"visibleDisabled",
                items:[{
                    layout:'column',
                    border:false,
                    defaults:{border:false},
                    items:[{
                        layout:'form',
                        columnWidth:0.55,
                        border:false,
                        items:itemArr
                    },{
                        layout:'form',
                        columnWidth:0.45,
                        border:false,
                        items:[this.Memo, this.shipDate, this.shipvia, this.fob]
                    }]
                },this.tagsFieldset]
            }]
        });
        this.helpMessage= new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.dashboard.help"),  //'Help',
            handler:this.helpmessage,
            scope:this,
            tooltip: WtfGlobal.getLocaleText("acc.common.click"),  //'Click for help',
            iconCls: 'help'
        });
        this.addGrid();

        this.NorthForm.doLayout();
       // this.POStore.on('load',this.updateSubtotal,this)
        this.billDate.on('change',this.onDateChange,this);
        
        this.setTransactionNumber();        
        if(this.isEdit) {
//            this.loadRecord();
            this.loadEditableGrid();
        }
           
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
    setCurrencyInfo:function(currencyId, currencySymbol){
            this.custdatechange=true;
            if(!(this.isEdit || this.copyInv )){
                this.externalcurrencyrate=0;
                this.Currency.setValue(currencyId);
                this.currencyid=currencyId;
                this.symbol = currencySymbol;
                this.changeCurrencyStore();
            }
   },
   changeCurrencyStore:function(){
    if(this.val=="")this.val=this.billDate.getValue();
    this.currencyStore.load({
        params:{
            mode:201,
            transactiondate:WtfGlobal.convertToGenericDate(this.val)
            }
        });
},
   closeContract : function(){
       var contractStatus=1;
//       ['1','Pending'],['2','Pending & Closed'],['3','Done'],['4','Done & Closed']] 
   if(this.contractStatus==1)
       contractStatus=2;
    else if(this.contractStatus==3)
       contractStatus=4;
       
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.contractReoprt.closeContract")+"?",function(btn){
        if(btn=="yes") {
            Wtf.Ajax.requestEx({
                url: "ACCContract/changeContractSRStatus.do",
                params: {
                    contractid : this.contractid,
                    status : contractStatus
                }
            },this,this.genSuccessResponseClosed,this.genFailureResponseClosed);
            
        }
    }, this)
},
genSuccessResponseClosed : function(response){
    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function(){
        this.ContractStore.reload();
    }, this);
},
genFailureResponseClosed : function(response){
    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")],2);
},
    onDateChange:function(a,val,oldval){
        /*
        *	ERM-1037
        *	On date change send id of linked document to function to restrict linking of future dated document
        */
        var selectedBillIds=this.PO.getValue().toString();
        if (selectedBillIds!= ""){
            var selectedValuesArr = selectedBillIds.split(',');
            WtfGlobal.checkForFutureDate(this,selectedValuesArr)
        }
        this.val=val;
        this.oldval=oldval;
        this.loadTax(val);
        this.externalcurrencyrate=0;
        this.custdatechange=true;
        this.Grid.loadPriceStoreOnly(val,this.Grid.priceStore);
        
   },
    hideLoading:function(){Wtf.MessageBox.hide();},
    
    addCostCenter:function(){
        callCostCenter('addCostCenterWin');
    },

    addGrid:function(){
              this.Grid=new Wtf.account.FixedAssetSalesReturnGrid({
                    height: 300,//region:'center',//Bug Fixed: 14871[SK]
                    cls:'gridFormat',
                    layout:'fit',
                    viewConfig:{forceFit:false},//ERP-8628 [SJ]
                    isCustomer:this.isCustomer,
                    editTransaction:this.isEdit,
                    disabledClass:"newtripcmbss",
                    isCustBill:false,
                    id:this.id+"billingproductdetailsgrid",
                    moduleid:this.moduleid,
                    currencyid:this.Currency.getValue(),
                    isFixedAsset: this.isFixedAsset,
                    isLeaseFixedAsset:this.isLeaseFixedAsset,
                    fromOrder:true,
                    isOrder:this.isOrder,
                    isEdit:this.isEdit,
                    copyTrans:this.copyInv, 
                    forceFit:true,
                    readOnly:this.readOnly,
                    parentObj :this,
                    isViewTemplate : this.isViewTemplate,
                    loadMask : true
                });
       // this.Name.on('select',this.setTerm,this)
        this.NorthForm.on('render',this.setDate,this);  
         if(this.readOnly) {
             this.disabledbutton();  //  disabled button in view case
         }  
        this.Grid.getStore().on('load',function(store){            
            this.Grid.addBlank(store);
            this.updateFormCurrency();
        }.createDelegate(this),this);
        this.Grid.on("datachanged", this.applyCurrencySymbol,this);
    },
  disabledbutton:function(){
     this.shipDate.setDisabled(true);
     this.shipvia.setDisabled(true);
     this.fob.setDisabled(true);
     this.fromLinkCombo.setDisabled(true);
     this.fromPO.setDisabled(true); 
     this.PO.setDisabled(true);
     this.sequenceFormatCombobox.setDisabled(true);
     this.Number.setDisabled(true);
     this.billDate.setDisabled(true);
     this.CostCenter.setDisabled(true);
     this.Currency.setDisabled(true);
     this.Name.setDisabled(true);
    },
    addOrder:function(){
        var tabid = "deliveryorder";
        callDeliveryOrder(false,null, tabid);
        if(Wtf.getCmp(tabid)!=undefined) {
            Wtf.getCmp(tabid).on('update',function(){this.POStore.reload();},this);
        }
    },

    enablePO:function(c,rec){
        if (this.Grid) {
            this.Grid.fromPO = false;
        }
        this.fromLinkCombo.clearValue();
        this.PO.clearValue();
        this.CostCenter.clearValue();
        
//        this.Name.clearValue();
        this.Memo.setValue("");

        if(rec.data['value']==true){                                                
            this.fromLinkCombo.enable();                        
            this.fromOrder=true;
        }
        else{
            this.Grid.getStore().removeAll();            
            this.Grid.addBlankRow();
            this.fromLinkCombo.disable();
            this.PO.disable();            
        }
        //this.currencyStore.load(); 	       // Currency id issue 20018
    },

    enableNumber:function(c,rec){        
        
        this.PO.clearValue();
        this.CostCenter.clearValue();
        
//        this.Name.clearValue();
        this.Memo.setValue("");
        this.Grid.getStore().removeAll();            
        this.Grid.addBlankRow();
        this.updateFormCurrency();
        
            if(rec.data['value']==0){
                this.PO.multiSelect=true;
                this.isMultiSelectFlag=true;
                this.POStore.proxy.conn.url = (this.isCustomer)?"ACCInvoiceCMN/getDeliveryOrdersMerged.do":"ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";
                this.POStore.load({params:{id:this.Name.getValue(),isLeaseFixedAsset:this.isLeaseFixedAsset, currencyfilterfortrans:this.Currency.getValue(),linkFlag:true, isFixedAsset:this.isFixedAsset, FA_DOGRlinkFlag:true}});        
                this.PO.enable(); 
                this.POSelected=(this.isCustomer)?"sales":"purchase";
                this.PO.removeListener("select",this.populateData,this);  // for selection of multiple sales order 
                this.PO.addListener("blur",this.populateData,this);
            }else if(rec.data['value']==1){
                this.POStore.proxy.conn.url = this.isCustomer ? "ACCInvoiceCMN/getInvoices.do" : "ACCGoodsReceiptCMN/getGoodsReceipts.do";
                var params={cashonly:false,creditonly:true, salesPurchaseReturnflag:true, doflag:true, currencyfilterfortrans:this.Currency.getValue(),linkFlag:true};
                this.isMultiSelectFlag=true;
                this.PO.multiSelect=true;
                if(this.isCustomer) {                        
                    params.customerid=this.Name.getValue();                    
                }else{
                    params.vendorid=this.Name.getValue();                    
                }
                this.salesPurchaseReturnflag = true,
                this.POStore.load({params:params});        
                this.PO.enable();       
                this.POSelected="invoice";
                this.PO.removeListener("select",this.populateData,this);  // for selection of multiple sales order 
                this.PO.addListener("blur",this.populateData,this);
            }
    },
    setNextNumber: function(){
       if(this.sequenceFormatStore.getCount()>0){
        var sequenceformatid=(this.record && this.record.data && this.record.data.sequenceformatid)?this.record.data.sequenceformatid:"";
        if(this.isEdit || this.copyInv){
            if(sequenceformatid==undefined || sequenceformatid=="" ||  sequenceformatid=='NA'){
                this.sequenceFormatCombobox.setValue("NA"); 
                this.sequenceFormatCombobox.disable();
                if(this.readOnly!=undefined && !this.readOnly){
                    this.Number.enable();
                }
//                if(this.copyInv){//for copy NA enable disable number field
//                    this.getNextSequenceNumber(this.sequenceFormatCombobox);
//                }
                
            }else{
                var index=this.sequenceFormatStore.find('id',sequenceformatid);
                if(index!=-1){
                    this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid); 
                        
                    if(!this.copyInv){
                        this.sequenceFormatCombobox.disable();
                        this.Number.disable(); 
                    }else{
                        this.Number.disable();
                        WtfGlobal.hideFormElement(this.Number);
                    }
                }else {
                    this.sequenceFormatCombobox.setValue("NA"); 
                    this.sequenceFormatCombobox.disable();
                    this.Number.enable();  

                }
            }
        }else{
            /*
             *Set Defalult sequence format to sequence format combo box
             */
            var count=this.sequenceFormatStore.getCount();
            for(var i=0;i<count;i++){
                var seqRec=this.sequenceFormatStore.getAt(i)
                if(seqRec.json.isdefaultformat=="Yes"){
                    this.sequenceFormatCombobox.setValue(seqRec.data.id) 
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
    
     loadEditableGrid:function(){
       
        this.subGridStoreUrl =  (this.isCustomer)?"ACCInvoiceCMN/getSalesReturnRows.do":"ACCGoodsReceiptCMN/getPurchaseReturnRows.do";
//            	            
            this.billid=this.record.data.billid;
            this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;
            this.Grid.getStore().on("load", function(){
                this.loadRecord();
            }, this);
            this.Grid.getStore().load({params:{bills:this.billid,isLeaseFixedAsset:this.isLeaseFixedAsset,isFixedAsset: this.isFixedAsset,isEdit:this.isEdit,moduleid:this.moduleid}});
    },
    
    populateData:function(c,rec) {
        this.Grid.fromPO=true;         
        var billid = this.PO.getValue(); // ERP-5417
        if (billid.indexOf("-1") != -1) {
            var url="";
            if (this.fromLinkCombo.getValue() == 0) {
                url = (this.isCustomer) ? "ACCInvoiceCMN/getDeliveryOrdersMerged.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";
            } else if (this.fromLinkCombo.getValue() == 1) {
                url = this.isCustomer ? "ACCInvoiceCMN/getInvoices.do" : "ACCGoodsReceiptCMN/getGoodsReceipts.do";
            }
            this.PO.reset();
            this.PO.collapse();
            this.PO.clearValue();
            this.showPONumbersGrid(url);
            return;
        }
        if (this.isMultiSelectFlag && billid!=-1) {
            var selectedids = this.PO.getValue();
            var selectedValuesArr = selectedids.split(',');
            for (var cnt = 0; cnt < selectedValuesArr.length; cnt++) {
                rec = this.POStore.getAt(this.POStore.find('billid', selectedValuesArr[cnt]));
                this.Memo.setValue(rec.data['memo']);
                this.postText = rec.data['posttext'];
                this.Name.setValue(rec.data['personid']);
                this.shipDate.setValue(rec.data['shipdate']);
                this.shipvia.setValue(rec.data['shipvia']);
                this.fob.setValue(rec.data['fob']);
                this.Currency.setValue(rec.data['currencyid']);
                var perstore = this.customerAccStore;
                this.CostCenter.setValue(rec.data.costcenterid);
                var record = this.POStore.getAt(this.POStore.find('billid', selectedValuesArr[cnt]));
                this.resetCustomFields();
                var fieldArr = this.POStore.fields.items;
                for (var fieldCnt = 0; fieldCnt < fieldArr.length; fieldCnt++) {
                    var fieldN = fieldArr[fieldCnt];
                    if (Wtf.getCmp(fieldN.name + this.tagsFieldset.id) && record.data[fieldN.name] != "") {
                        if (Wtf.getCmp(fieldN.name + this.tagsFieldset.id).getXType() == 'datefield') {
                            Wtf.getCmp(fieldN.name + this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                        } else if (Wtf.getCmp(fieldN.name + this.tagsFieldset.id).getXType() == 'fncombo') {
                            var ComboValue = record.data[fieldN.name];
                            if (ComboValue) {
                                Wtf.getCmp(fieldN.name + this.tagsFieldset.id).setValue(ComboValue);
                                var childid = Wtf.getCmp(fieldN.name + this.tagsFieldset.id).childid;
                                if (childid.length > 0) {
                                    var childidArray = childid.split(",");
                                    for (var i = 0; i < childidArray.length; i++) {
                                        var currentBaseParams = Wtf.getCmp(childidArray[i] + this.tagsFieldset.id).store.baseParams;
                                        currentBaseParams.parentid = ComboValue;
                                        Wtf.getCmp(childidArray[i] + this.tagsFieldset.id).store.baseParams = currentBaseParams;
                                        Wtf.getCmp(childidArray[i] + this.tagsFieldset.id).store.load();
                                    }
                                }
                            }
                        } else {
                            Wtf.getCmp(fieldN.name + this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                        }
                    }
                    if (fieldN.name.indexOf("Custom_") == 0) {
                        var fieldname = fieldN.name.substring(7, fieldN.name.length);
                        if (Wtf.getCmp(fieldname + this.tagsFieldset.id) && record.data[fieldN.name] != "") {
                            if (Wtf.getCmp(fieldname + this.tagsFieldset.id).getXType() == 'fieldset') {
                                var ComboValue = record.data[fieldN.name];
                                var ComboValueArrya = ComboValue.split(',');
                                var ComboValueID = "";
                                var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;
                                for (var i = 0; i < ComboValueArrya.length; i++) {
                                    for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                                        if (checkListCheckBoxesArray[checkitemcnt].id.indexOf(ComboValueArrya[i]) != -1)
                                            if (Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id) != undefined) {
                                                Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id).setValue(true);
                                            }
                                    }
                                }
                            } else if (Wtf.getCmp(fieldname + this.tagsFieldset.id).getXType() == 'select') {
                                var ComboValue = record.data[fieldN.name];
                                if (ComboValue != "" && ComboValue != undefined)
                                    Wtf.getCmp(fieldname + this.tagsFieldset.id).setValue(ComboValue);
                            }
                        }
                    }
                }
                var url = "";
                var linkingFlag = false;
                var FA_DOGRlinkFlag = false;
                if (this.fromLinkCombo.getValue() == 0) {
                    url = this.isCustomer ? "ACCInvoiceCMN/getDeliveryOrderRows.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrderRows.do";
                    linkingFlag = true;
                    FA_DOGRlinkFlag = true;
                } else if (this.fromLinkCombo.getValue() == 1) {
                    url = this.isCustomer ? "ACCInvoiceCMN/getInvoiceRows.do" : "ACCGoodsReceiptCMN/getGoodsReceiptRows.do";
                    linkingFlag = true;
                }
                rec=this.PO.getValue();
                this.Grid.getStore().proxy.conn.url = url;
                if (this.salesPurchaseReturnflag) {
                    this.Grid.getStore().baseParams.salesPurchaseReturnflag = this.salesPurchaseReturnflag;
                }
            }
            /*
            *	ERM-1037
            *	Send id of linked document to function to restrict linking of future dated document
            */
           var isFutureDatedDocumentLinked = WtfGlobal.checkForFutureDate(this, selectedValuesArr);
            if (isFutureDatedDocumentLinked) {
                return;
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
            this.Grid.loadPOGridStore(rec, linkingFlag, FA_DOGRlinkFlag);
        }
    },
    onNameSelect: function (combo, record, index) {
        if (combo.getValue() == this.nameBeforeSelect) { //If same name selected no need to do any action 
            return;
        }
        var currencyId = record.get("currencyid");
        var currencySymbol = record.get("currencysymbol");
        this.setCurrencyInfo(currencyId, currencySymbol);
        this.fromLinkCombo.clearValue();
        this.PO.clearValue();
        if (!this.isEdit && !this.copyInv) {  //in edit and copy case do not remove record from store
            this.Grid.getStore().removeAll();
            this.Grid.addBlankRow();
        }
        this.fromLinkCombo.disable();
        this.PO.disable();
        this.PO.reset();
        this.fromPO.setValue(false);
        this.Memo.setValue('');
        this.shipDate.setValue('');
        this.shipvia.setValue('');
        this.fob.setValue('');
        this.CostCenter.setValue('');
        if (this.fromPO)
            this.fromPO.enable();
        this.tagsFieldset.resetCustomComponents();
        var customer = this.Name.getValue();
        var moduleid = this.isCustomer ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId;
        this.tagsFieldset.setValuesForCustomer(moduleid, customer);

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
            this.billDate.setValue(new Date());//(new Date());            
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
        if(this.loadCurrFlag && Wtf.account.companyAccountPref.currencyid){
            this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
            this.loadCurrFlag = false;
        }
        if(this.isEdit){
            var data=this.record.data;
            this.Currency.setValue(data.currencyid);
          }
        
        if(this.currencyStore.getCount()==0){
            callCurrencyExchangeWindow();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
            this.billDate.setValue("");
        } else
            this.updateFormCurrency();
    },
    updateFormCurrency:function(){
       this.applyCurrencySymbol();
//       var calTermTotal = WtfGlobal.addCurrencySymbolOnly(this.findTermsTotal(),this.symbol);
//       this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol),termtotal:calTermTotal,aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax()+this.findTermsTotal(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol()),amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())});
    },
    getCurrencySymbol:function(){
        var index=null;
//        this.currencyStore.clearFilter(true); //ERP-9962
        var FIND = this.Currency.getValue();
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
    applyCurrencySymbol:function() {
        var index = this.getCurrencySymbol();
        var rate = this.externalcurrencyrate;
        if(index >= 0){
           rate = (rate == "" ? this.currencyStore.getAt(index).data.exchangerate : rate);
           this.symbol =  this.currencyStore.getAt(index).data.symbol;
           this.Grid.setCurrencyid(this.currencyid,rate,this.symbol,index);
//           this.applyTemplate(this.currencyStore,index);
        }
        return this.symbol;
    },
    setTransactionNumber:function(isSelectNoFromCombo){
    	if(this.isEdit && !this.copyInv)
            this.Number.setValue(this.record.data.billno);
        else{
                    var format= Wtf.account.companyAccountPref.autosr ;
                    var temp2=(this.isCustomer)? Wtf.autoNum.SalesReturn:Wtf.autoNum.PurchaseReturn ;
            }
            if(isSelectNoFromCombo){
                this.fromnumber = temp2;
            } else if(format&&format.length>0){
                WtfGlobal.fetchAutoNumber(temp2, function(resp){if(this.isEdit)this.Number.setValue(resp.data)}, this);
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
    initForClose:function(){
        this.cascade(function(comp){
            if(comp.isXType('field')){
                comp.on('change', function(){this.isClosable=false;},this);
            }
        },this);
    },
    
     save:function(){
       var incash=false;
        this.Number.setValue(this.Number.getValue().trim());
        //this.billTo.setValue(this.billTo.getValue().trim());
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
        if(this.NorthForm.getForm().isValid() && isValidCustomFields){
            // Checking for deactivated products
            var inValidProducts=this.checkForDeActivatedProductsAdded();
            if(inValidProducts!=''){
                inValidProducts = inValidProducts.substring(0, inValidProducts.length-2);
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"), 
                    msg: WtfGlobal.getLocaleText("acc.common.followingProductsAreDeactivated")+'</br>'+'<b>'+inValidProducts+'<b>',
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.WARNING,
                    scope: this,
                    scopeObj :this,
                    fn: function(btn){
                        if(btn=="ok"){
                            return;
                        }
                    }
                });
                return;
           }
            if(Wtf.account.companyAccountPref.invAccIntegration && Wtf.account.companyAccountPref.isUpdateInvLevel){
                var validstore=WtfGlobal.isValidInventoryInfo(this.Grid.getStore(),'invstore');
                if(!validstore){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Pleaseselectvalidinventorystore")], 2);
                    return;
                }
            
                var validloc=WtfGlobal.isValidInventoryInfo(this.Grid.getStore(),'invlocation');
                if(!validloc){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.Pleaseselectvalidinventorylocation")], 2);
                    return;
                }
            }
            
            
            // In Case of Fixed Asset Check external and internal quantities are equal or not
            if(this.isFixedAsset || this.isLeaseFixedAsset){
                for(var i=0;i<this.Grid.getStore().getCount()-1;i++){// exclude last row
                    var quantity=this.Grid.getStore().getAt(i).data['dquantity'];
                    var assetDetails = this.Grid.getStore().getAt(i).data['assetDetails'];
                    
                    var productId = this.Grid.getStore().getAt(i).data['productid'];
                    
//                    var proRecord = WtfGlobal.searchRecord(this.Grid.productComboStore,productId,'productid');
                    var proRecord = (WtfGlobal.searchRecord(this.Grid.productComboStore,productId,'productid')!=null)?WtfGlobal.searchRecord(this.Grid.productComboStore,productId,'productid'):WtfGlobal.searchRecord(this.Grid.getStore(),productId,'productid');
                    if(proRecord.get('isAsset')){
                    
                        if(assetDetails == "" || assetDetails == undefined){
                            WtfComMsgBox(['Information',WtfGlobal.getLocaleText("acc.field.PleaseProvideAssetDetailsforAssetGroup")+" "+"<b>"+this.Grid.getStore().getAt(i).data['productname']+"</b>"+"."],0);
                            return;
                        }

                        var assetDetailArray = eval('(' + assetDetails + ')');

                        if(assetDetailArray == null || assetDetailArray == undefined){
                            WtfComMsgBox(['Information',WtfGlobal.getLocaleText("acc.field.PleaseProvideAssetDetailsforAssetGroup")+" "+"<b>"+this.Grid.getStore().getAt(i).data['productname']+"</b>"+"."],0);
                            return;
                        }

                        if(quantity != assetDetailArray.length){
                            WtfComMsgBox(['Information','Entered quantity does not match with the Asset Rows entered. Please give complete Asset Details for Asset Group '+this.Grid.getStore().getAt(i).data['productname']+'.'],0);
                            return;
                        }
                        
                        var isDepreciationPosted = false;
                        var depriceatedAssetIDs = "";
                        for (var j=0; j<assetDetailArray.length; j++) {
                            if (assetDetailArray[j].isDepreciationPosted) {
                                isDepreciationPosted = true;
                                depriceatedAssetIDs += assetDetailArray[j].assetName + ", ";
                            }
                        }
                        
                        if (isDepreciationPosted && this.isFixedAsset && this.moduleid == Wtf.Acc_FixedAssets_Purchase_Return_ModuleId) {
                            WtfComMsgBox(['Information','Depreciation for Asset ID(s) ' + depriceatedAssetIDs.substring(0, depriceatedAssetIDs.length-2) + ' has been already posted. So Purchase Return cannot be made.'],0);
                            return;
                        }
                        
                    }
                }
                // ERM-616 Below if condition is for checking 'Allow zero unit price in Lease Module' setting activated or not in system controls.
                var billid=this.PO.getValue();
                this.linkRecord = this.POStore.getAt(this.POStore.find('billid',billid));
                var amount = this.Grid.editprice.getValue()==="" ? (this.linkRecord!=undefined ? this.linkRecord.data.amount : 0) : this.Grid.editprice.getValue();
                if ((!CompanyPreferenceChecks.allowZeroUntiPriceInLeaseModule() && this.isLeaseFixedAsset)) { 
                    if (amount === "" || amount == undefined || amount <= 0) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.TotalamountshouldbegreaterthanZero")], 2);
                        return;
                    }
                }
            }
            
            var count=this.Grid.getStore().getCount();
            if(count<=1){
                WtfComMsgBox(33, 2);
                return;
            }
            incash=this.cash;
            var rec=this.NorthForm.getForm().getValues();
            
            this.ajxurl = this.isCustomer?"ACCSalesReturnCMN/saveSalesReturn.do":"ACCSalesReturnCMN/savePurchaseReturn.do";            
            
		
            var detail = this.Grid.getProductDetails();
            var validLineItem = this.Grid.checkDetails(this.Grid);
            if (validLineItem != "" && validLineItem != undefined) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), (WtfGlobal.getLocaleText("acc.msgbox.lineitem") + validLineItem)], 2);
                this.enableSaveButtons();
                return;
            }
            if(detail == undefined || detail == "[]"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
                return;
            }
             this.showConfirmAndSave(rec,detail,incash);
        }else{
            WtfComMsgBox(2, 2);
        }
    },   
       
    showConfirmAndSave: function(rec,detail,incash){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.invoice.msg7"),function(btn){
                if(btn!="yes") {return;}
                rec.detail=detail;
                this.msg= WtfComMsgBox(27,4,true);
                //rec.currencyid=this.Currency.getValue();
                rec.number=this.Number.getValue();
                var custFieldArr=this.tagsFieldset.createFieldValuesArray();
                if (custFieldArr.length > 0)
                    rec.customfield = JSON.stringify(custFieldArr);        
                if(this.Grid != undefined && this.Grid.deleteStore!=undefined && this.Grid.deleteStore.data.length>0){  //for geting store to delete.
                    rec.deletedData = this.getJSONArray(this.Grid.deleteStore,false,0);
                }
                if(this.contractid && this.contractid!=undefined){
                     rec.contractid=this.contractid;
                }
                rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
                rec.shipdate=WtfGlobal.convertToGenericDate(this.shipDate.getValue());
                rec.srid=this.copyInv?"":this.billid; 
                rec.batchDetails=this.Grid.batchDetails;
                rec.mode=(this.isOrder?41:11);
                rec.posttext=this.postText;
                rec.isfavourite=false;
                rec.isLeaseFixedAsset=this.isLeaseFixedAsset;
                rec.isFixedAsset=this.isFixedAsset;
                rec.transType=this.moduleid;
                rec.sequenceformat=this.sequenceFormatCombobox.getValue();
                var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):false;
                if(!this.copyInv){
                    if((this.record && this.record !== undefined) && (this.record.get('isfavourite') !== null || this.record.get('isfavourite') !== undefined)){
                        rec.isfavourite = this.record.get('isfavourite');
                    }
                }
                rec.currencyid=this.Currency.getValue();
                rec.linkNumber=this.PO.getValue(); 
                rec.fromLinkCombo=this.fromLinkCombo.lastSelectionText;
                Wtf.Ajax.requestEx({
                    url:this.ajxurl,
                    params: rec                    
                },this,this.genSuccessResponse,this.genFailureResponse);
                },this);
    },
    genSuccessResponse:function(response, request){
        WtfComMsgBox([this.title,response.msg],response.success*2+1);
         if(response.success){
            Wtf.productStoreSales.reload();
            Wtf.productStore.reload();   //Reload all product information to reflect new quantity, price etc          
            var rec=this.NorthForm.getForm().getValues();
            this.exportRecord=rec;
            this.exportRecord['billid']=response.billid;
            this.exportRecord['billno']=response.billno;
            if(this.printRecords){
            this.printRecords.exportRecord=this.exportRecord;
            }
            if(this.mailFlag){
                this.loadUserStore(response, request);
                this.disableComponent();
                this.response = response;
                this.request = request;
                return;
            }
            this.Grid.getStore().removeAll();
            this.fromLinkCombo.disable();
            this.PO.setDisabled(true);
            this.NorthForm.getForm().reset();
            this.Grid.updateRow(null);
            this.fromPO.setValue(false); 
            this.Grid.priceStore.purgeListeners();
            this.Grid.loadPriceStoreOnly(new Date(),this.Grid.priceStore);
            this.setTransactionNumber();
            this.fireEvent('update',this);
            if(this.saveandCreateFlag){
            this.symbol = WtfGlobal.getCurrencySymbol();
            this.currencyStore.load();                  
            this.Currency.setValue(WtfGlobal.getCurrencyID());
            this.sequenceFormatStore.load();
            if(this.sequenceFormatStore.getCount()>0){
                 var seqRec=this.sequenceFormatStore.getAt(0);
                this.sequenceFormatCombobox.setValue(seqRec.data.id);
             }
             this.saveandCreateFlag=false;
            }
            this.postText="";
            if(!this.mailFlag){
                var customFieldArray = this.tagsFieldset.customFieldArray; //Reset Custom Fields
                for (var itemcnt = 0; itemcnt < customFieldArray.length; itemcnt++) {
                    var fieldId = customFieldArray[itemcnt].id
                    if (Wtf.getCmp(fieldId) != undefined && customFieldArray[itemcnt].getXType()!='fieldset') {
                       Wtf.getCmp(fieldId).reset();
                 }
                }    
                var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;  //Reset Check List
                for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                    var checkfieldId = checkListCheckBoxesArray[checkitemcnt].id
                    if (Wtf.getCmp(checkfieldId) != undefined) {
                        Wtf.getCmp(checkfieldId).reset();
                    }
                } 
                var customDimensionArray = this.tagsFieldset.dimensionFieldArray;  //Reset Custom Dimensions
                for (var itemcnt1 = 0; itemcnt1 < customDimensionArray.length; itemcnt1++) {
                    var fieldId1 = customDimensionArray[itemcnt1].id
                    if (Wtf.getCmp(fieldId1) != undefined) {
                        Wtf.getCmp(fieldId1).reset();
                    }
                } 
            }
         }else if (response.accException) {
             /*
              * For display window of duplicate record
              */
            Wtf.MessageBox.hide();
            this.newnowin = new Wtf.Window({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                closable: true,
                iconCls: getButtonIconCls(Wtf.etype.deskera),
                width: 330,
                autoHeight: true,
                modal: true,
                bodyStyle: "background-color:#f1f1f1;",
                closable:false,
                        buttonAlign: 'right',
                items: [new Wtf.Panel({
                        border: false,
                        html: (response.msg.length > 60) ? response.msg : "<br>" + response.msg,
                        height: 50,
                        bodyStyle: "background-color:white; padding: 7px; font-size: 11px; border-bottom: 1px solid #bfbfbf;"
                    }),
                    this.newdoForm = new Wtf.form.FormPanel({
                        labelWidth: 190,
                        border: false,
                        autoHeight: true,
                        bodyStyle: 'padding:10px 5px 3px; ',
                        autoWidth: true,
                        defaultType: 'textfield',
                        items: [this.newdono = new Wtf.form.TextField({
                                fieldLabel: this.isLeaseFixedAsset ? WtfGlobal.getLocaleText("acc.LR.newassetreturnno") : WtfGlobal.getLocaleText("acc.AP.newassetreturnno"),
                                allowBlank: false,
                                labelSeparator: '',
                                width: 90,
                                itemCls: 'nextlinetextfield',
                                name: 'newdono',
                                id: 'newdono'
                            })],
                        buttons: [{
                                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                                handler: function () {
                                    if (this.newdono.validate()) {
                                        Wtf.getCmp("invoiceNo"+this.heplmodeid+this.id).setValue(this.newdono.getValue());
                                        this.save();
                                        this.newnowin.close();
                                    }
                                },
                                scope: this
                            }, {
                                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //"Cancel",
                                scope: this,
                                handler: function () {
                                    this.newnowin.close();
                                }
                            }]
                    })]
            });
            this.newnowin.show();
        } else if (response.isTaxDeactivated) {
            WtfComMsgBox([this.title, "You cannot save " + this.title + " with deacivated tax(s)."], 2);
        }
    },

    genFailureResponse:function(response){
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    
    callEmailWindowFunction : function(response, request){
        if(this.CustomStore != null){
            var rec = this.CustomStore.getAt(0);
            var label = (this.isCustomer)?WtfGlobal.getLocaleText("acc.accPref.autoSR"):WtfGlobal.getLocaleText("acc.dimension.module.18");
            var mode=(this.isCustomer)?61:63;
            callEmailWin("emailwin",rec,this.label,mode,true,false,false,false,false,false, false, true);
            
        }
},

disableComponent: function(){  // disable following component in case of save button press.
    
    if(this.fromLinkCombo && this.fromLinkCombo.getValue() === ''){
        this.fromLinkCombo.emptyText = "";
        this.fromLinkCombo.clearValue();
    }
    
    if(this.PO && this.PO.getValue() === ''){
        this.PO.emptyText = "";
        this.PO.clearValue();
    }
    
    if(Wtf.getCmp("save"+this.id)){
        Wtf.getCmp("save"+this.id).disable();
    }
    if(Wtf.getCmp("savencreate" + this.heplmodeid + this.id)){
        Wtf.getCmp("savencreate" + this.heplmodeid + this.id).disable();
    }
    if(Wtf.getCmp("posttext" + this.id)){
        Wtf.getCmp("posttext" + this.id).disable();
    }

    if(this.Grid){
        var GridStore = this.Grid.getStore();
        var count2 = GridStore.getCount();
        var lastRec2 = GridStore.getAt(count2-1);
        GridStore.remove(lastRec2);
        this.Grid.disable();
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
        {name:'posttext'},
        {name:'costcenterid'},
        {name:'costcenterName'},
        {name:'statusID'},
        {name:'shipvia'},
        {name:'fob'},
        {name:'status'},
        {name:'withoutinventory',type:'boolean'},
        {name:'isfavourite'},
    ]);
    
    var StoreUrl = (this.isCustomer)?"ACCInvoiceCMN/getSalesReturn.do":"ACCGoodsReceiptCMN/getPurchaseReturn.do";
    
    
    this.CustomStore = new Wtf.data.GroupingStore({
            url:StoreUrl,
            remoteSort:true,
            baseParams:{
                costCenterId: this.CostCenter.getValue(),
                deleted:false,
                nondeleted:false,
                consolidateFlag:false,
                enddate:'',
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
        
        this.CustomStore.load({
            params:{
                isLeaseFixedAsset:true,
                moduleid:this.moduleid
            }
        });
    
},

enableButtons : function(){
    Wtf.getCmp("emailbut" + this.id).enable();
    Wtf.getCmp("exportpdf" + this.id).enable();
    Wtf.getCmp("printSingleRecord" + this.id).enable();
},

exportPdfFunction : function(){
    if(this.CustomStore != null){
        //var rec = this.CustomStore.getAt(0);
        //var recData = rec.data;
        var selRec = "&amount="+0+"&bills="+ this.exportRecord['billid'];//recData.billid;
        var fileName = "";
        var mode = "";
        if(this.isCustomer){
            fileName = "Sales Return "+this.exportRecord['billno'];
            mode = 61;
        }else{
            fileName = "Purchase Return "+this.exportRecord['billno'];
            mode = 63;
        }
        
        Wtf.get('downloadframe').dom.src = "ACCExportRecord/exportRecords.do?mode="+mode+"&rec="+selRec+"&personid="+this.exportRecord['personid']+"&filename="+fileName+"&filetype=pdf";
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
    resetCustomFields : function(){ // For reset Custom Fields, Check List and Custom Dimension
    var customFieldArray = this.tagsFieldset.customFieldArray;  // Reset Custom Fields
    for (var itemcnt = 0; itemcnt < customFieldArray.length; itemcnt++) {
        var fieldId = customFieldArray[itemcnt].id
        if (Wtf.getCmp(fieldId) != undefined && customFieldArray[itemcnt].getXType()!='fieldset') {
            Wtf.getCmp(fieldId).reset();
        }
    }
    
    var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;  // Reset Check List
    for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
        var checkfieldId = checkListCheckBoxesArray[checkitemcnt].id
        if (Wtf.getCmp(checkfieldId) != undefined) {
            Wtf.getCmp(checkfieldId).reset();
        }
    }
    
    var customDimensionArray = this.tagsFieldset.dimensionFieldArray;  // Reset Custom Dimension
    for (var itemcnt1 = 0; itemcnt1 < customDimensionArray.length; itemcnt1++) {
        var fieldId1 = customDimensionArray[itemcnt1].id
        if (Wtf.getCmp(fieldId1) != undefined) {
            Wtf.getCmp(fieldId1).reset();
        }
    }
    },
    checkForDeActivatedProductsAdded:function(){
    var invalidProducts='';
    if(this.isEdit && !this.copyInv){ //Edit Case
        var linkedDocuments = this.PO.getValue();
        var linkedDocsArray=[];
        if(linkedDocuments != ''){
            if(linkedDocuments instanceof String){
                linkedDocsArray = linkedDocuments.split(',');
            }else{
                linkedDocsArray = linkedDocuments;
            }
            var areDocumentsChanged = false;
            for(var x=0;x<linkedDocsArray.length;x++){
                var docId = linkedDocsArray[x];
                if(this.originallyLinkedDocuments.indexOf(docId) == -1){
                    areDocumentsChanged = true;
                    break;
                }
            }
            if(areDocumentsChanged){
                invalidProducts = this.checkDeactivatedProductsInGrid();
            }
        }
    } else { // Create New and Copy
        invalidProducts = this.checkDeactivatedProductsInGrid();
    }
    return invalidProducts;
},
    checkDeactivatedProductsInGrid :function(){
        var inValidProducts=''
        var rec = null;
        var productId = null;
        var productRec = null;
        for(var count=0;count<this.Grid.store.getCount();count++){
            rec = this.Grid.store.getAt(count);
            productId = rec.data.productid;
            if(productId!= undefined && productId != null && productId != ''){
                if(!this.fromPO.getValue() && !this.copyInv){
                    productRec = WtfGlobal.searchRecord(this.Grid.productComboStore, productId, "productid");
                } else {
                    productRec = rec;
                }
                if(productRec && (productRec.data.hasAccess === false)){
                    inValidProducts+=productRec.data.productname+', ';
                }
            }    
        }
        return inValidProducts;
    },
    addSelectedDocument:function(){
        var url="";
        if(this.moduleid==Wtf.Acc_FixedAssets_Purchase_Return_ModuleId){
                url = "ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";  ;
        }else if(this.moduleid==Wtf.Acc_FixedAssets_Sales_Return_ModuleId){
                url = "ACCInvoiceCMN/getDeliveryOrdersMerged.do"  ;
        }
        this.showPONumbersGrid(url);
    }
});