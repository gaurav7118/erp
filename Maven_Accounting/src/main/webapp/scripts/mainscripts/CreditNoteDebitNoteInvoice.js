
Wtf.account.CreditNoteDebitNotePanel=function(config){	
    this.modeName = config.modeName;
    this.isNoteAlso = (config.isNoteAlso)?config.isNoteAlso:false;
    Wtf.apply(this, config);
    this.sendMailFlag = false;
    this.mailFlag = false;
    this.response="";
    this.request="";
    this.CustomStore="";
    this.moduleid=config.moduleid;
    this.salesPurchaseReturnflag = false;
    var help=getHelpButton(this,config.heplmodeid);
    this.businessPerson=this.isCustomer?'Customer':"Vendor";
    this.isLeaseFixedAsset = (config.isLeaseFixedAsset)?config.isLeaseFixedAsset:false;
    this.isCndnAgainstInvoice=config.isCndnAgainstInvoice;
    this.isCN=config.isCN;
    this.record=config.record;
    this.remainingQuantity=0;
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
    }
    ]);

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

    var isEditORisCopy=(this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv !=undefined ?this.copyInv:false); // Load All Customers in Edit and Copy case
    this.customerAccStore.on('beforeload', function(s,o){
            if(!o.params)o.params={};
            var currentBaseParams = this.customerAccStore.baseParams;
            if(isEditORisCopy){
                currentBaseParams.isPermOrOnetime=""; // Empty to Load all Customers.
            }else{
                if(this.ShowOnlyOneTime != undefined && this.ShowOnlyOneTime.getValue() == true){
                    currentBaseParams.isPermOrOnetime=true; // True to load One Time Customers
                }else{
                    currentBaseParams.isPermOrOnetime=false; // False To load Permanent Customers
                }
            }
            this.customerAccStore.baseParams=currentBaseParams;
        }, this);
    this.customerAccStore.load();
    
    Wtf.apply(this,{
        bbar:[{
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
            toolTip:WtfGlobal.getLocaleText("acc.rem.175"),
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
            toolTip:WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
            id:"savencreate"+config.heplmodeid+this.id,
            scope:this,
            disabled:(this.contractStatus==3),
            hidden : true, // this.isEdit || this.copyInv,
            handler:function(){
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
        hidden:true, //this.readOnly,
        handler: function(){this.callEmailWindowFunction(this.response, this.request)},
        iconCls: "accountingbase financialreport"
        },{
            text: WtfGlobal.getLocaleText("acc.field.ExportPDF"),
            tooltip : WtfGlobal.getLocaleText("acc.field.ExportPDFFile"),
            scope:this,
            id:"exportpdf" + this.id,
            iconCls: 'pwnd exportpdf1',
            hidden:true, //this.readOnly,
            disabled : true,
            handler: function(){this.exportPdfFunction()}
        },{
            text:  WtfGlobal.getLocaleText("acc.template.posttext") , //'<b>Post Text</b>',
            cls: 'pwnd add',
            disabled:(this.contractStatus==3),
            id: "posttext" + this.id,              // Post Text
            hidden:true, //this.readOnly,        
            tooltip : WtfGlobal.getLocaleText("acc.field.UsePostTextoptiontoinserttextafterSignature"),        
            style:" padding-left: 15px;",
            scope: this,
            handler: function() {
                this.getPostTextEditor(this.postText);
            } 
        },{
            text:  WtfGlobal.getLocaleText("acc.common.close") , //'<b>Close</b>',
            cls: 'pwnd add',
            id: "posttext" + this.id,              // Close
            hidden:true, //(this.contractStatus == null || this.contractStatus == undefined || this.contractStatus==2 ||this.contractStatus==4 ||this.readOnly),        
            tooltip : WtfGlobal.getLocaleText("acc.common.closebtn.Tooltip"),        
            style:" padding-left: 15px;",
            scope: this,
            handler: this.closeContract
        },'->']
      });
    Wtf.account.CreditNoteDebitNotePanel.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.CreditNoteDebitNotePanel,Wtf.account.ClosablePanel,{
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
            
             if(this.copyInv){//iF COPY AND SEQUENCE FORMAT IS NA
                this.Number.setValue("");                
            }else{
                this.Number.setValue(data.billno);
            }
            var store=this.isCustomer ? Wtf.customerAccStore : Wtf.vendorAccStore;
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
            
            this.Memo.setValue(data.memo);                        
            this.billDate.setValue(data.date);                                                            
            this.CostCenter.setValue(data.costcenterid);
            this.editedBy.setValue(data.lasteditedby);
            this.postText = this.record.json.posttext;
            
            this.dataLoaded=true;
            this.Grid.priceStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
            //this.bankTypeStore.load();            
            if(this.Grid){
                this.Grid.forCurrency =data.currencyid;
                this.Grid.affecteduser=data.personid;
                this.Grid.billDate=data.date;
            }
             if(this.copyInv && this.Grid){
                this.Grid.billDate=Wtf.serverDate;
            }
            if(this.copyInv){
            	this.billDate.setValue(Wtf.serverDate);
//            	this.updateDueDate();
            }
        }
    },
    onRender:function(config){              
        this.add(this.NorthForm,this.Grid);                       
        Wtf.account.CreditNoteDebitNotePanel.superclass.onRender.call(this, config);
        this.initForClose();
        // hide form fields
        if( this.isEdit ){
            this.isClosable=false          // Set Closable flag for edit and copy case
        }
        this.hideFormFields();
    },
    hideFormFields:function(){
        if(this.isCustomer){
            this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.CreditNote);
        }else{
            this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.DebitNote);
        }
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
                    Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).disabled = fieldArray.isReadOnly;
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
    initComponent:function(config){
        Wtf.account.CreditNoteDebitNotePanel.superclass.initComponent.call(this,config);
        
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
        if(this.isCustomer){
            Wtf.customerAccStore.load();
        }else{
            Wtf.vendorAccStore.load();
        }
        this.sequenceFormatStore.load();

     var isShowOneTime=(this.moduleid == Wtf.Acc_Sales_Return_ModuleId) && !((this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv !=undefined ?this.copyInv:false)); 
     this.ShowOnlyOneTime= new Wtf.form.Checkbox({
        name:'ShowOnlyOneTime',
          fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.cust.ShowOnlyOneTime.tt") +"'>"+ WtfGlobal.getLocaleText("acc.cust.ShowOnlyOneTime")  +"</span>",//WtfGlobal.getLocaleText("acc.cust.onetime"),
        // fieldLabel:WtfGlobal.getLocaleText("acc.cust.ShowOnlyOneTime"),
        id:'ShowOnlyOneTime'+this.heplmodeid+this.id,
        checked:false,
        hideLabel:!isShowOneTime, // Show Only in new case
        hidden:!isShowOneTime,
        cls : 'custcheckbox',
        width: 10
    });  
    
    this.ShowOnlyOneTime.on('check',function(obj,isChecked){
                this.Name.reset();
                this.customerAccStore.load();
    },this); 
    
        this.Name= new Wtf.form.ExtFnComboBox({
            fieldLabel:(this.isCustomer)?"<span wtf:qtip='"+  WtfGlobal.getLocaleText("acc.invoiceList.cust.tt") +"'>"+ WtfGlobal.getLocaleText("acc.invoiceList.cust") +"</span>":"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.invoiceList.ven.tt") +"'>"+ WtfGlobal.getLocaleText("acc.invoiceList.ven") +"</span>",//this.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven") , //this.businessPerson+"*",
            hiddenName:this.businessPerson.toLowerCase(),
            id:"customer"+this.heplmodeid+this.id,
            store: this.isCustomer? Wtf.customerAccStore : Wtf.vendorAccStore,
            valueField:'accid',
            displayField:'accname',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
            allowBlank:false,
            hirarchical:true,
            emptyText:this.isCustomer?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven") , //'Select a '+this.businessPerson+'...',
            mode: 'local',
            typeAheadDelay:30000,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            minChars:1,
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            anchor:"50%",
            triggerAction:'all',
            listeners:{
                'select':{
                    fn:function(obj,rec,index){
                        if(rec.data.currencyid!=this.Currency.getValue()){//update currency field with vendor currency if vendor currency is different
                          this.Currency.setValue(rec.data.currencyid);
                          this.currencychanged = true;
                          this.updateFormCurrency();   
                        } 
                        this.isClosable=false          // Set Closable flag after selecting Customer/Vendor
                        this.fromLinkCombo.clearValue();
                        this.PO.clearValue();
                        if(!this.isEdit && !this.copyInv){
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
                        if(this.fromPO)
                            this.fromPO.enable();
                       if(this.Grid){
                            this.Grid.affecteduser=this.Name.getValue();
                       }
                    },
                scope:this                    
                }
            }
        });
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
        fromLinkStoreRec.push([this.isCustomer?'Delivery Order':"Goods Receipt", '0']);
        fromLinkStoreRec.push([this.isCustomer?'Sales Invoice':"Purchase Invoice", '1']);
        
        
        
        
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
        fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.Sequenceformat.tip")+"'>"+ WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat")+"</span>",
        valueField:'id',
        displayField:'value',
        store:this.sequenceFormatStore,
        disabled:(this.isEdit&&!this.copyInv&&!this.isPOfromSO&&!this.isSOfromPO?true:false),  
        width:240,
        typeAhead: true,
        forceSelection: true,
        name:'sequenceformat',
        hiddenName:'sequenceformat',
        allowBlank:false,
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
            if(this.Grid){
                this.Grid.forCurrency = this.Currency.getValue();
            }
        }, this);
        
        this.Number=new Wtf.form.TextField({
            fieldLabel:this.label + " " + WtfGlobal.getLocaleText("acc.common.number"),  //,  //this.label+' Number*',
            name: 'number',
            disabled:(this.isEdit&&!this.copyInv?true:false),
            id:"invoiceNo"+this.heplmodeid+this.id,
            width:240,
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
            maxLength:2048
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
            {name:'shipvia'},
            {name:'fob'},
            {name:'amountdue'},
            {name:'costcenterid'},
            {name:'costcenterName'},
            {name:'lasteditedby'},
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
                linkingFlag :linkingFlag
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
        //                   sortDir:'ASC',
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
            disabled:this.isEdit?false:true,
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
            emptyText: this.isCustomer?WtfGlobal.getLocaleText("acc.field.SelectaCI/DO"):WtfGlobal.getLocaleText("acc.field.SelectaVI/GR"),
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
        
        this.PO= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Number") ,  //(this.isCustomer?"SO":"PO")+" Number",
            hiddenName:"ordernumber",
            //id:"orderNumber"+this.heplmodeid+this.id,
            id:"poNumberID"+this.heplmodeid+this.id,
            allowBlank:false, 
            store: this.POStore,
            valueField:'billid',
            hideLabel:false,
            hidden:false,
            displayField:'billno',
            disabled:true,
            emptyText: this.isCustomer?WtfGlobal.getLocaleText("acc.field.SelectaCI/DO"):WtfGlobal.getLocaleText("acc.field.SelectaVI/GR"),
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,            
            width:240,
            triggerAction:'all',
//            addNewFn:this.addOrder.createDelegate(this,[false,null,this.businessPerson+"PO"],true),
            scope:this,
            listeners:{
                'select':{
                    fn:this.populateData,
                    scope:this
                }
            }
        });
        
//            this.PO.addNewFn=this.addOrder.createDelegate(this,[false,null,this.businessPerson+"PO"],true)
            
           this.POSelected="";
        
        this.billDate= new Wtf.form.DateField({
            fieldLabel:this.label +' '+WtfGlobal.getLocaleText("acc.invoice.date"),
            id:"invoiceDate"+this.heplmodeid+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'billdate',
            width:240,
            allowBlank:false
        });
        this.shipDate= new Wtf.form.DateField({
           fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.ShipDate.tip")+"'>"+ WtfGlobal.getLocaleText("acc.field.ShipDate")+"</span>",
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
        this.CostCenter= new Wtf.form.FnComboBox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.costCenter.tip") +"'>"+ WtfGlobal.getLocaleText("acc.common.costCenter")+"</span>",//WtfGlobal.getLocaleText("acc.common.costCenter"),//"Cost Center",
            hiddenName:"costcenter",
            id:"costcenter"+this.heplmodeid+this.id,
            store: Wtf.FormCostCenterStore,
            valueField:'id',
            displayField:'name',
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            anchor:"50%",            
            triggerAction:'all',
            addNewFn:this.addCostCenter,
            scope:this,
            hidden: this.quotation,
            hideLabel: this.quotation
        }); 
        this.editedBy = new Wtf.form.TextField({
            //fieldLabel: WtfGlobal.getLocaleText("acc.field.ShipVia"),
             fieldLabel:  WtfGlobal.getLocaleText("acc.field.LastEditedBy"),
            name: 'lasteditedby',
            id:"lasteditedby"+this.heplmodeid+this.id,
//            anchor: '94%',
            disabled:true,
            width : 240,
            maxLength: 255,
            scope: this,
             hidden: this.isEdit||this.readOnly?false:true,
            hideLabel:this.isEdit||this.readOnly?false:true
        }); 
        var itemArr={};
            itemArr = [this.ShowOnlyOneTime,this.Name, this.Currency,{
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
           ]},this.PO,this.sequenceFormatCombobox,this.Number,this.billDate, this.CostCenter,this.editedBy];
   
   this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId:"northForm"+this.id,
            autoHeight: true,
            parentcompId:this.id,
            moduleid: this.moduleid,
            isEdit: this.isEdit,
            record: this.record
        });
   
        this.NorthForm=new Wtf.form.FormPanel({
            region:'north',
            autoHeight:true,
            id:"northForm"+this.id,
            disabled:this.readOnly,
            disabledClass:"newtripcmbss",
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
                        columnWidth:0.65,
                        border:false,
                        items:itemArr
                    },{
                        layout:'form',
                        columnWidth:0.35,
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
            if(Wtf.getCmp('normalcontractorderreport') && Wtf.getCmp('normalcontractorderreport').ContractStore){
                Wtf.getCmp('normalcontractorderreport').ContractStore.reload();
            }
        }, this);
    },
    genFailureResponseClosed : function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")],2);
    },
    
    onDateChange:function(a,val,oldval){
        this.val=val;
        this.oldval=oldval;
        if(this.Grid){
                 this.Grid.billDate=this.billDate.getValue();
        }
//        this.loadTax(val);
        this.externalcurrencyrate=0;
        this.custdatechange=true;
        this.Grid.loadPriceStoreOnly(val,this.Grid.priceStore);
        
   },
    hideLoading:function(){Wtf.MessageBox.hide();},
    
    addCostCenter:function(){
        callCostCenter('addCostCenterWin');
    },

    addGrid:function(){
              this.Grid=new Wtf.account.CreditNoteDebitNoteGrid({
                    height: 200,//region:'center',//Bug Fixed: 14871[SK]
                    cls:'gridFormat',
                    layout:'fit',
                    viewConfig:{forceFit:false},
                    isCustomer:this.isCustomer,
                    isNoteAlso:this.isNoteAlso,
                    editTransaction:this.isEdit,
                    isCndnAgainstInvoice:this.isCndnAgainstInvoice,
                    disabled:this.readOnly,
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
                    isCnDnForInvoice:true,
                    loadMask : true,
                    heplmodeid:this.heplmodeid,
                    parentid:this.id,
                    parentObj :this
                });
       // this.Name.on('select',this.setTerm,this)
        this.NorthForm.on('render',this.setDate,this);
        if(!this.isEdit && !this.copyInv){
            this.Grid.productComboStore.load();
        }
        this.Grid.getStore().on('load',function(store){            
            this.Grid.addBlank(store);
            this.updateFormCurrency();
        }.createDelegate(this),this);
        this.Grid.on("datachanged", function(){
            this.applyCurrencySymbol();
            this.isClosable=false          // Set Closable flag on grid data change
        },this);
    },

    addOrder:function(){
        var tabid = "deliveryorder";
        callDeliveryOrder(false,null, tabid);
        if(Wtf.getCmp(tabid)!=undefined) {
            Wtf.getCmp(tabid).on('update',function(){this.POStore.reload();},this);
        }
    },

    enablePO:function(c,rec){
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
                this.POStore.proxy.conn.url = (this.isCustomer)?"ACCInvoiceCMN/getDeliveryOrdersMerged.do":"ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";
                this.POStore.load({params:{id:this.Name.getValue(),currencyfilterfortrans:this.Currency.getValue(),nondeleted:true}});        
                this.PO.enable(); 
                this.POSelected=(this.isCustomer)?"sales":"purchase";
            }else if(rec.data['value']==1){
                this.POStore.proxy.conn.url = this.isCustomer ? "ACCInvoiceCMN/getInvoices.do" : "ACCGoodsReceiptCMN/getGoodsReceipts.do";
                var params={cashonly:false,creditonly:true, salesPurchaseReturnflag:true, doflag:true, currencyfilterfortrans:this.Currency.getValue(),nondeleted:true};
                if(this.isCustomer) {                        
                    params.customerid=this.Name.getValue();                    
                }else{
                    params.vendorid=this.Name.getValue();                    
                }
                this.salesPurchaseReturnflag = true,
                this.POStore.load({params:params});        
                this.PO.enable();       
                this.POSelected="invoice";
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
            this.Grid.getStore().load({params:{bills:this.billid}});
    },
    
    populateData:function(c,rec) {
        this.Grid.fromPO=true;                
        this.Memo.setValue(rec.data['memo']);
        this.postText=rec.data['posttext'];
        this.Name.setValue(rec.data['personid']); 
        this.shipDate.setValue(rec.data['shipdate']);
        this.shipvia.setValue(rec.data['shipvia']);
        this.fob.setValue(rec.data['fob']);
        this.Currency.setValue(rec.data['currencyid']);
        var perstore=this.isCustomer? Wtf.customerAccStore : Wtf.vendorAccStore;
        var storerec=perstore.getAt(perstore.find('accid',rec.data['personid']));        
        this.CostCenter.setValue(rec.data.costcenterid);
        //this.updateDueDate();
        var billid=this.PO.getValue();
        var record=this.POStore.getAt(this.POStore.find('billid',billid));
        this.resetCustomFields();
        var fieldArr = this.POStore.fields.items;
        for(var fieldCnt=0; fieldCnt < fieldArr.length; fieldCnt++) {
            var fieldN = fieldArr[fieldCnt];

            if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id) && record.data[fieldN.name] !="") {
                if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).getXType()=='datefield'){
                    Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                }else if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).getXType()=='fncombo'){
                    var ComboValue=record.data[fieldN.name];
                    var ComboValueID="";
                    var recCustomCombo =WtfGlobal.searchRecord(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).store,ComboValue,"name");
                    if(recCustomCombo){
                        ComboValueID=recCustomCombo.data.id;
                        Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(ComboValueID);
                        var  childid= Wtf.getCmp(fieldN.name+this.tagsFieldset.id).childid;
                        if(childid.length>0){
                            var childidArray=childid.split(",");
                            for(var i=0;i<childidArray.length;i++){
                                var currentBaseParams = Wtf.getCmp(childidArray[i]+this.tagsFieldset.id).store.baseParams;
                                currentBaseParams.parentid=ComboValueID;
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
                        var ComboValue=eval("record.json."+fieldN.name + "_Values");
                        var ComboValueArrya=ComboValue.split(',');
                        var ComboValueID="";
                        var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray; 
                        for(var i=0 ;i < ComboValueArrya.length ; i++){
                            for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                                if(checkListCheckBoxesArray[checkitemcnt].name == ComboValueArrya[i])
                                    if (Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id) != undefined) {
                                        Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id).setValue(true);
                                    }
                            } 
                        }
                    }else if(Wtf.getCmp(fieldname+this.tagsFieldset.id).getXType()=='select'){
                        var ComboValue=eval("record.json."+fieldN.name + "_Values");
                        var ComboValueArrya=ComboValue.split(',');
                        var ComboValueID="";
                        for(var i=0 ;i < ComboValueArrya.length ; i++){
                            var recCustomCombo =WtfGlobal.searchRecord(Wtf.getCmp(fieldname+this.tagsFieldset.id).store,ComboValueArrya[i],"name");
                            ComboValueID+=recCustomCombo.data.id+","; 
                        }
                        if(ComboValueID.length > 1){
                            ComboValueID=ComboValueID.substring(0,ComboValueID.length - 1);
                        }
                        Wtf.getCmp(fieldname+this.tagsFieldset.id).setValue(ComboValueID);
                    }

                }
            }
        }
        var url = "";
        var linkingFlag = false;
        if(this.fromLinkCombo.getValue()==0){
            url = this.isCustomer ? "ACCInvoiceCMN/getDeliveryOrderRows.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrderRows.do";
            linkingFlag=true;
        } else if(this.fromLinkCombo.getValue()==1){
            url = this.isCustomer ? "ACCInvoiceCMN/getInvoiceRows.do" : "ACCGoodsReceiptCMN/getGoodsReceiptRows.do";
            linkingFlag =true;
        }
        
//        var url = this.isCustomer ? "ACCInvoiceCMN/getDeliveryOrderRows.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrderRows.do";
		//(this.isCustBill?53:43)
        
	this.Grid.getStore().proxy.conn.url = url;
        if(this.salesPurchaseReturnflag){
            this.Grid.getStore().baseParams.salesPurchaseReturnflag = this.salesPurchaseReturnflag;
        }
        this.Grid.loadPOGridStore(rec,linkingFlag);
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
        if(this.loadCurrFlag && Wtf.account.companyAccountPref.currencyid){
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
        
    setNextNumber: function(){
        if(this.sequenceFormatStore.getCount()>0){
            if(this.isEdit || this.copyInv){
                var sequenceformatid=this.record.get("sequenceformatid");
                if(sequenceformatid=="" || sequenceformatid==undefined){
                    this.sequenceFormatCombobox.setValue("NA"); 
                    this.sequenceFormatCombobox.disable();
                    this.Number.enable();
                    if(this.copyInv){//for copy NA enable disable number field
                        this.getNextSequenceNumber(this.sequenceFormatCombobox);
                    }

                } else{
                    var index=this.sequenceFormatStore.find('id',sequenceformatid);
                    if(index!=-1){
                        this.sequenceFormatCombobox.setValue(sequenceformatid);                                               
                    }else{  //sequence format get deleted then NA is set
                        if(this.sequenceFormatStore.getCount()>1){
                            var count=this.sequenceFormatStore.getCount();
                            for(var i=0;i<count;i++){
                                var seqRec=this.sequenceFormatStore.getAt(i)
                                if(seqRec.json.isdefaultformat=="Yes"){
                                    this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                                    break;
                                }
                            }
                        }else{
                            this.sequenceFormatCombobox.setValue("NA"); 
                        }  
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
       var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
       if(this.NorthForm.getForm().isValid() && isValidCustomFields){
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
            for(var i=0;i<this.Grid.getStore().getCount()-1;i++){// excluding last row
                var quantity=this.Grid.getStore().getAt(i).data['quantity'];
                if(quantity==""||quantity==undefined||quantity<=0){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.AQuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.shouldbegreaterthanZero")], 2);
                    return;
                } 
                //ERP-10630
//                var dquantity=this.Grid.getStore().getAt(i).data['dquantity'];
//                if(dquantity==""||dquantity==undefined||dquantity<=0){
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.RQuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.shouldbegreaterthanZero")], 2);
//                    return;
//                }
                if(Wtf.account.companyAccountPref.unitPriceConfiguration){
                    var rate=this.Grid.getStore().getAt(i).data['rate'];
                    if(rate===""||rate==undefined||rate<0){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.RateforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.cannotbeempty")], 2);
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
            if(this.isCndnAgainstInvoice){
                this.ajxurl = this.isCN?"ACCCreditNote/saveCreditNoteAgainstInvoice.do":"ACCDebitNote/saveDebitNoteAgainstInvoice.do";            
            }

            var detail = this.Grid.getProductDetails();
            if(detail == undefined || detail == "[]"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
                return;
            }
            var prodLength=this.Grid.getStore().data.items.length;
            var i=0;
            for(;i<prodLength-1;i++)
            { 
                var prodID=this.Grid.getStore().getAt(i).data['productid'];
                var prorec=this.Grid.productComboStore.getAt(this.Grid.productComboStore.find('productid',prodID));
            if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.islocationcompulsory || Wtf.account.companyAccountPref.iswarehousecompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory){ //if company level option is on then only check batch and serial details
                 if(prorec.data.isBatchForProduct || prorec.data.isSerialForProduct || prorec.data.isLocationForProduct || prorec.data.isWarehouseForProduct || prorec.data.isRowForProduct || prorec.data.isRackForProduct  || prorec.data.isBinForProduct){ 
                    if(prorec.data.type!='Service'){
                        var batchDetail= this.Grid.getStore().getAt(i).data['batchdetails'];
                        var productQty= this.Grid.getStore().getAt(i).data['dquantity'];
                        var baseUOMRateQty= this.Grid.getStore().getAt(i).data['baseuomrate'];
                        if(batchDetail == undefined || batchDetail == "" || batchDetail=="[]"){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);   //Batch and serial no details are not valid.
                            return;
                        }else{
                                     var jsonBatchDetails= eval(batchDetail);
                                     var batchQty=0;
                                     for(var batchCnt=0;batchCnt<jsonBatchDetails.length;batchCnt++){
                                         if(jsonBatchDetails[batchCnt].quantity>0){
                                             batchQty=batchQty+ parseInt(jsonBatchDetails[batchCnt].quantity);
                                         }
                                     }
                                     
                                     if((batchQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) != (productQty*baseUOMRateQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)){
                                         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);
                                         return;
                                     }                       
                        }
                    }
                }
            }
                if(prorec != undefined) {
                    var prodName=prorec.data.productname;
                    var availableQuantity = prorec.data.quantity;
                    availableQuantity=availableQuantity
                    var lockQuantity = prorec.data.lockquantity; 
                    var quantity= this.Grid.getStore().getAt(i).data['dquantity'];
//                   
                   if((availableQuantity-lockQuantity)<quantity){  //for DO for linked with SO which is not linked and for Invoice
                    if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninPRareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prodName+WtfGlobal.getLocaleText("acc.field.is")+' '+(availableQuantity-lockQuantity)+' <br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                        return true;
                    }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninPRareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                            if(btn=="yes"){
                                this.showConfirmAndSave(rec,detail,incash);
                                return ;  //
                            }else{
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
            if(prodLength>0){  //in case of all product delivered quantity is available then directly save transaction
                    this.showConfirmAndSave(rec,detail,incash);  

                
                var quantity=this.Grid.getStore().getAt(i).data['quantity'];
                if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') { // serial no for only inventory type of product
                    if (prorec.data.isSerialForProduct) {
                        var v = quantity;
                        v = String(v);
                        var ps = v.split('.');
                        var sub = ps[1];
                       if (sub!=undefined && sub.length > 0) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                        return;
                      }
                    }

                }

          }else{

                
                var dquantity=this.Grid.getStore().getAt(i).data['dquantity'];
                if (prorec.data.type != 'Service' && prorec.data.type != 'Non-Inventory Part') { // serial no for only inventory type of product
                    if (prorec.data.isSerialForProduct) {
                        var v = dquantity;
                        v = String(v);
                        var ps = v.split('.');
                        var sub = ps[1];
                        if (sub!=undefined && sub.length > 0) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.msg.exceptionMsgForDecimalQty")], 2);
                        return;
                     }
                    }
                }
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
                rec.fromLinkCombo=this.fromLinkCombo.getRawValue();
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
                rec.isNoteAlso=this.isNoteAlso; 
                rec.batchDetails=this.Grid.batchDetails;
                rec.mode=(this.isOrder?41:11);
                rec.posttext=this.postText;
                rec.isfavourite=false;
                var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):true;
                rec.sequenceformat=this.sequenceFormatCombobox.getValue();
                if(!this.copyInv){
                    if((this.record && this.record !== undefined) && (this.record.get('isfavourite') !== null || this.record.get('isfavourite') !== undefined)){
                        rec.isfavourite = this.record.get('isfavourite');
                    }
                }
                rec.currencyid=this.Currency.getValue();
                rec.isEdit=this.isEdit;
                rec.copyInv=this.copyInv;
                rec.linkNumber = (this.PO != undefined && this.PO.getValue() != "")? this.PO.getValue() : "";
                
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
            this.sequenceFormatStore.load();
            this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
            this.fireEvent('update',this);
            this.isClosable= true;       //Reset Closable flag to avoid unsaved Message.
            this.postText="";
            if(!this.mailFlag){
                this.resetCustomFields();
            }
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
//            var rec = this.CustomStore.getAt(0);
            var rec = "";
            if (response.billid != undefined || response.billid != '') {
                rec = this.CustomStore.getAt(this.CustomStore.find('billid', response.billid));
            }
            var label = (this.isCustomer)?WtfGlobal.getLocaleText("acc.accPref.autoSR"):WtfGlobal.getLocaleText("acc.dimension.module.18");
            var mode=(this.isCustomer)?61:63;
             callEmailWin("emailwin",rec,this.label,mode,this.isCustomer,false,false,false,false,false, false, true);
         }
},

disableComponent: function(){  // disable following component in case of save button press.
    
    if(this.fromLinkCombo && this.fromLinkCombo.getValue() === ''){
     //   this.fromLinkCombo.emptyText = "";
        this.fromLinkCombo.clearValue();
    }
    
    if(this.PO && this.PO.getValue() === ''){
    //    this.PO.emptyText = "";
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
        
        this.CustomStore.load();
    
},

enableButtons : function(){
    Wtf.getCmp("emailbut" + this.id).enable();
    Wtf.getCmp("exportpdf" + this.id).enable();
},

exportPdfFunction : function(){
    if(this.CustomStore != null){
        var rec = this.CustomStore.getAt(0);
        var recData = rec.data;
        var selRec = "&amount="+0+"&bills="+recData.billid;
        var fileName = "";
        var mode = "";
        if(this.isCustomer){
            fileName = "Sales Return "+recData.billno;
            mode = 61;
        }else{
            fileName = "Purchase Return "+recData.billno;
            mode = 63;
        }
        if(mode == Wtf.autoNum.PurchaseReturn && (Wtf.templateflag==Wtf.BuildMate_templateflag||Wtf.templateflag==Wtf.Merlion_templateflag)) {//  Purchase return
            Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportPurchaseReturn.do? &mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&templateflag="+Wtf.templateflag;                  
        } else if(mode == Wtf.autoNum.SalesReturn &&(Wtf.templateflag==Wtf.BuildMate_templateflag||Wtf.templateflag==Wtf.Merlion_templateflag)) {//  sales return
            Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportSalesReturnJasper.do? &mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&templateflag="+Wtf.templateflag;                  
        }else if(mode == Wtf.autoNum.PurchaseReturn && Wtf.templateflag==Wtf.F1Recreation_templateflag) {//  sales return
            Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportF1RecreationPurchaseReturn.do? &mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&templateflag="+Wtf.templateflag;                  
        } else if(mode == Wtf.autoNum.SalesReturn && Wtf.templateflag==Wtf.F1Recreation_templateflag) {//  sales return
            Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportF1SalesReturnReport.do? &mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&templateflag="+Wtf.templateflag;                  
        }else{
            Wtf.get('downloadframe').dom.src = "ACCExportRecord/exportRecords.do?mode="+mode+"&rec="+selRec+"&personid="+recData.personid+"&filename="+fileName+"&filetype=pdf";
        }
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
    }
});
