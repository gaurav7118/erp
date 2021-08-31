
/*< COMPONENT USED FOR >
 *      1.Delivery Order
 *          callDeliveryOrder(isEdit,rec,winid) --- < Create delivery Order > 
 *          [isEdit=true/false, isCustomer=true, isOrder=true, record]
 *      Note -: callDeliveryOrder(isEdit,rec,winid) function defined in WtfTransactionManager.js    
 */

Wtf.account.FixedAssetDeliveryOrderPanel=function(config){	
    this.version='_v1';
    this.id=config.id;
    this.isFixedAsset = (config.isFixedAsset!=null && config.isFixedAsset!=undefined)?config.isFixedAsset:false;
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    this.isLeaseFixedAsset = (config.isLeaseFixedAsset)?config.isLeaseFixedAsset:false;
    this.titlel = config.title!=undefined?config.title:"null";
    this.isEdit=config.isEdit;
    this.label=config.label;
    this.copyInv = config.copyInv;
    this.sendMailFlag = false;
    this.mailFlag = false;
    this.handleEmptyText=false; //To handle empty text after clicking on save button
    this.heplmodeid = config.heplmodeid;
    this.response="";
    this.request="";
    this.CustomStore="";
    this.record=config.record;
    var help=getHelpButton(this,config.heplmodeid);
    this.businessPerson=(config.isCustomer?'Customer':'Vendor');
    this.modeName = config.modeName;
    this.moduleid=config.moduleid;
    this.readOnly=config.readOnly;
    this.uPermType=config.isLeaseFixedAsset?Wtf.UPerm.leaseorder:config.isCustomer?Wtf.UPerm.assetsales:Wtf.UPerm.assetpurchase;
    this.permType=config.isLeaseFixedAsset?Wtf.Perm.leaseorder:config.isCustomer?Wtf.Perm.assetsales:Wtf.Perm.assetpurchase;
    this.exportPermType=config.isLeaseFixedAsset?this.permType.exportldo:(config.isCustomer?this.permType.exportfado:this.permType.exportfagr);
    this.currentAddressDetailrec="";
    this.originallyLinkedDocuments = '';
    this.isGST=WtfGlobal.GSTApplicableForCompany()==Wtf.GSTStatus.NEW?true:false;
    this.keepTermDataInLinkCase=false; // Used to keep term term details as it is i.e. dont recalculate
    this.isbilldateChanged = false;
//    this.CUSTOM_KEY = "customfield";
    (this.businessPerson == "Customer")? Wtf.DOStatusStore.load() : Wtf.GROStatusStore.load();
    Wtf.apply(this, config);
//    this.custUPermType=config.isCustomer?Wtf.UPerm.customer:Wtf.UPerm.vendor;
//    this.custPermType=config.isCustomer?Wtf.Perm.customer:Wtf.Perm.vendor;
//    this.soUPermType=(config.isCustomer?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
//    this.soPermType=(config.isCustomer?Wtf.Perm.invoice.createso:Wtf.Perm.vendorinvoice.createpo);
    var tranType=null;
    if(config.moduleid==28||config.moduleid==27 ||config.moduleid == Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId ||config.moduleid == Wtf.Acc_FixedAssets_DeliveryOrder_ModuleId||config.moduleid==Wtf.Acc_Lease_DO){
        if(config.moduleid==27 ||config.moduleid == Wtf.Acc_FixedAssets_DeliveryOrder_ModuleId ||config.moduleid==Wtf.Acc_Lease_DO){
            tranType=Wtf.autoNum.DeliveryOrder;
        }else{
            tranType=Wtf.autoNum.GoodsReceiptOrder;
        }   
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
    var singlePDFtext = null;
    this.isPILinkedInGR = false;
    if(this.isQuotation)
    	singlePDFtext = WtfGlobal.getLocaleText("acc.accPref.autoQN");
    else
    	singlePDFtext = config.isCustomer?(config.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoSO"):WtfGlobal.getLocaleText("acc.accPref.autoInvoice")):(config.isOrder?WtfGlobal.getLocaleText("acc.accPref.autoPO"):WtfGlobal.getLocaleText("acc.accPref.autoVI"));
  
   this.singlePrint=new Wtf.exportButton({
         obj:this,
         id:"exportpdf" + this.id,
         iconCls: 'pwnd exportpdfsingle',
         text:WtfGlobal.getLocaleText("acc.field.ExportPDF"),// + " "+ singlePDFtext,
         tooltip :WtfGlobal.getLocaleText("acc.rem.39.singletooltip"),  //'Export selected record(s)',
         disabled :true,
         isEntrylevel:true,
         exportRecord:this.exportRecord,
         hidden:this.isRequisition || this.isRFQ || this.isSalesCommissionStmt ||this.readOnly,
         menuItem:{rowPdf:(this.isSalesCommissionStmt)?false:true,rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39") + " "+ singlePDFtext},
         get:tranType,
         moduleid:config.moduleid
         
     });  
    }
    this.printRecords = new Wtf.exportButton({
        obj: this,
        id: "printSingleRecord"+ this.id,
        iconCls: 'pwnd printButtonIcon',
        text: WtfGlobal.getLocaleText("acc.rem.236"),
        tooltip: WtfGlobal.getLocaleText("acc.rem.236.single"), //'Print Single Record Details',
        disabled: true,
        hidden:this.isRequisition || this.isRFQ || this.isSalesCommissionStmt ||this.readOnly,
        isEntrylevel: false,
        exportRecord:this.exportRecord,
        menuItem: {
            rowPrint: true
        },
        get: tranType,
        moduleid:config.moduleid
    });
    this.showAddrress=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.ShowAddress"),  //WtfGlobal.getLocaleText("acc.template.posttext") , //'<b>Post Text</b>',
        cls: 'pwnd add',
        id: "showaddress" + this.id,                
        tooltip : WtfGlobal.getLocaleText("acc.field.UseShowAddressoptiontoinsertAddresses"),       
        style:" padding-left: 15px;",
        scope: this,
        disabled : true,
        handler:this.getAddressWindow 
    });
    
    /*
     * Assign the value to the optimized flag as per System preferences.
     */
    this.custVenOptimizedFlag = Wtf.account.companyAccountPref.custvenloadtype;
     
    Wtf.apply(this,{
        bbar:[{
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
            tooltip:WtfGlobal.getLocaleText("acc.rem.175"),
            id:"save"+config.heplmodeid+this.id,
            scope:this,
            hidden:this.readOnly,
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
            hidden : this.isEdit || this.copyInv,
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
        hidden:this.readOnly,
        disabled : true,
        handler: function(){this.callEmailWindowFunction(this.response, this.request)},
        iconCls: "accountingbase financialreport"
        },(this.singlePrint)?this.singlePrint:"",this.printRecords,
        {
            text:  WtfGlobal.getLocaleText("acc.template.posttext") , //'<b>Post Text</b>',
            cls: 'pwnd add',
            id: "posttext" + this.id,              // Post Text
            tooltip : WtfGlobal.getLocaleText("acc.field.UsePostTextoptiontoinserttextafterSignature"),       
            style:" padding-left: 15px;",
            scope: this,
            hidden:this.readOnly,
            handler: function() {
                this.getPostTextEditor(this.postText);
            }   
        },this.showAddrress,'->']
      });
    Wtf.account.FixedAssetDeliveryOrderPanel.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.FixedAssetDeliveryOrderPanel,Wtf.account.ClosablePanel,{
    autoScroll: true,// layout:'border',//Bug Fixed: 14871[SK]
    bodyStyle: {background:"#DFE8F6 none repeat scroll 0 0"},
    border:'false',
    externalcurrencyrate:0,
    isCurrencyLoad:false,
    currencyid:null,
    //isMultiSelectFlag:false,
    custdatechange:false,
    closable : true,
    cash:false,
    
//    fromOrder:false,
    loadRecord:function(){
        if(this.record!=null&&!this.dataLoaded){
            var data=this.record.data;
            this.externalcurrencyrate=this.record.data.externalcurrencyrate;
            this.updateFormCurrency();
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
                this.Name.setValForRemoteStore(data.personid, data.personname, data.hasAccess);
            
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
                                billno:rec.data.linkto,
                                /*
                                *	ERM-1037
                                *	For date of linked document
                                */
                                date:rec.data.linkDate    ,
                                amount:rec.data.amount    
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
                    this.isPILinkedInGR=true;
                }
                if(linkType!=-1){
                    this.fromLinkCombo.setValue(linkType);
                }
            }            
            
            this.Memo.setValue(data.memo);                        
            this.billDate.setValue(data.date);                                                            
            this.CostCenter.setValue(data.costcenterid);
            this.postText = this.record.json.posttext;
            this.DOStatusCombo.setValue(data.statusID)
            this.dataLoaded=true;
            this.Grid.priceStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
            //this.bankTypeStore.load();
            //this.DOStatusCombo.setValue(data.statusID);
//            if(this.copyInv){
//            	this.billDate.setValue(Wtf.serverDate);
////            	this.updateDueDate();
//            }
            if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){
                this.Grid.productComboStore.load({params:{mappingProduct:true,customerid:this.Name.getValue(),common:'1', loadPrice:true,mode:54}}) ;           
            } 
             
            if (this.Grid) {
                this.Grid.affecteduser = data.personid;
            }
        }
        /**
         * Populate Customer/ Vendor GST details in Edit/Copy Case for lease module ERM-886
         */
        if (this.isLeaseFixedAsset && (this.isEdit || this.isCopy)){
            this.populateGSTDataOnEditCopy(); 
       }
    },
    onRender:function(config){                
        this.add(this.NorthForm,this.Grid,this.southPanel);                       
        Wtf.account.FixedAssetDeliveryOrderPanel.superclass.onRender.call(this, config);
        if(!this.isCustomer){
            this.permitNumber.hideLabel=true;
            this.permitNumber.hidden=true;
        }
        this.initForClose();   
        // hide form fields
            this.hideFormFields();
        if(this.isEdit || this.copyInv){
            this.showAddrress.enable();
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
    hideFormFields:function(){
        if(this.isCustomer){
            if(this.moduleid == Wtf.Acc_Lease_DO) { 
                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.leaseDeliveryOrder);
            } else {
                this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.deliveryOrder);
            }
            
        }else{
            this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.goodsReceipt);
        }
    },
    
    hideTransactionFormFields:function(array){
        if(array){
            for(var i=0;i<array.length;i++){
                var fieldArray = array[i];
                if(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id)){
                    Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hideLabel = fieldArray.isHidden;
                    Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hidden = fieldArray.isHidden;
                    if(fieldArray.isUserManadatoryField && Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel != undefined){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).allowBlank = !fieldArray.isUserManadatoryField;
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel = Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel + " *";
                    }
                }
            }
        }
    },
    
    initComponent:function(config){
        Wtf.account.FixedAssetDeliveryOrderPanel.superclass.initComponent.call(this,config);
        if (!this.custVenOptimizedFlag) {
            this.isCustomer ? chkcustaccload() : chkvenaccload();
        }
        this.loadCurrFlag = true;
        
        this.GridRec = Wtf.data.Record.create ([
            {name:'id'},
            {name:'number'}
        ]);
        
//        this.currencyRec = new Wtf.data.Record.create([
//            {name: 'currencyid',mapping:'tocurrencyid'},
//            {name: 'symbol'},
//            {name: 'currencyname',mapping:'tocurrency'},
//            {name: 'exchangerate'},
//            {name: 'htmlcode'}
//         ]);
//         this.currencyStore = new Wtf.data.Store({
//            reader: new Wtf.data.KwlJsonReader({
//                root: "data",
//                totalProperty:"count"
//            },this.currencyRec),
//    //        url:Wtf.req.account+'CompanyManager.jsp'
//            url:"ACCCurrency/getCurrencyExchange.do"
//         });

         //var transdate=(this.isEdit?WtfGlobal.convertToGenericDate(this.record.data.date):WtfGlobal.convertToGenericDate(new Date()));

//         this.Currency= new Wtf.form.FnComboBox({
//            fieldLabel:WtfGlobal.getLocaleText("acc.currency.cur"),  //'Currency',
//            hiddenName:'currencyid',
//            id:"currency"+this.heplmodeid+this.id,
//            anchor: '94%',
//            disabled:true,
//            store:this.currencyStore,
//            valueField:'currencyid',
//            forceSelection: true,
//            displayField:'currencyname',
//            scope:this,
//            selectOnFocus:true
//        });
        
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
        this.sequenceFormatStore.on('load',this.setNextNumber,this); // Set Sequence format in create/edit/copy case
        this.sequenceFormatStore.load();
     
        var comboConfig = {
            fieldLabel:this.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven") , //this.businessPerson+"*",
            hiddenName:this.businessPerson.toLowerCase(),
            id:"customer"+this.heplmodeid+this.id,
            store: this.isCustomer? Wtf.customerAccStore:Wtf.vendorAccStore,
            valueField:'accid',
            displayField:'accname',
            allowBlank:false,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            hirarchical:true,
            emptyText:this.isCustomer?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven") , //'Select a '+this.businessPerson+'...',
            mode: 'remote',
            typeAheadDelay:30000,
            minChars:1,
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            isVendor:!(this.isCustomer),
            isCustomer:this.isCustomer,
            width:240,
            triggerAction:'all',
 //           addNewFn:this.addPerson.createDelegate(this,[false,null,this.businessPerson+"window",this.isCustomer],true),
          //  scope:this
            listeners:{
                'select':{
                    fn:function(obj,rec,index){
                        this.singleLink = false;
                        if (this.isEdit || this.isCopy) {
                            this.isVenOrCustSelect = true;
                        }
                        /*
                         * Populate customer/Vendor currency
                         */
                        if (rec.data.currencyid != this.Currency.getValue()) {//update currency field with vendor currency if vendor currency is different
                            this.Currency.setValue(rec.data.currencyid);
                            this.currencychanged = true;
                            this.updateFormCurrency();
                        }
                        var customer= this.Name.getValue();
                        this.fromLinkCombo.clearValue();
                        this.PO.clearValue();
                        this.CostCenter.clearValue();
                        this.DOStatusCombo.clearValue();
                       if(!this.isEdit && !this.copyInv){   //in edit and copy case do not remove record from store
                        this.Grid.getStore().removeAll();
                        this.Grid.addBlankRow();
                       }    
                        this.showAddrress.enable();
                        this.currentAddressDetailrec="";
                        this.fromLinkCombo.disable();
                        this.PO.disable();
                        this.PO.reset();
                        this.fromPO.setValue(false);
                        this.Memo.setValue('');
                        this.postText=(this.record)?this.record.data.posttext:"";
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
        
        if (this.custVenOptimizedFlag) {
            comboConfig['ctCls'] = 'optimizedclass';
            comboConfig['hideTrigger'] = true;
        } 
        this.Name = new Wtf.form.ExtFnComboBox(comboConfig);
        
        this.Name.on('beforeselect', function(combo, record, index) {
                return validateSelection(combo, record, index);
        }, this);
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
        
        //if(!WtfGlobal.EnableDisable(this.custUPermType,this.custPermType.create))
            this.Name.addNewFn=this.addPerson.createDelegate(this,[false,null,this.businessPerson+"window",this.isCustomer],true);
        
        this.fromPOStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
             data:[['Yes',true],['No',false]]
        });
        
        
        var fromLinkStoreRec = new Array();
        if(this.isCustomer){
            if(this.isFixedAsset){
                fromLinkStoreRec.push(['Sales Invoice', 1]);
            }else if(this.isLeaseFixedAsset){
                fromLinkStoreRec.push(['Lease Order', 0]);
            }else{
                fromLinkStoreRec.push(['Sales Order', 0]);
                fromLinkStoreRec.push(['Customer Invoice', 1]);
            }
            
        } else {
            if(this.isFixedAsset){
                fromLinkStoreRec.push(['Purchase Invoice',1]);
            }else{
                fromLinkStoreRec.push(['Purchase Order',0]);
                fromLinkStoreRec.push(['Vendor Invoice',1]);
            } 
        }
        
        
        
        this.fromlinkStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value'}],
            data:fromLinkStoreRec
        });
        
        
        this.DOStatusCombo =  new Wtf.form.FnComboBox({
                fieldLabel:WtfGlobal.getLocaleText("acc.GIRO.Status"),
                name:"statuscombo",     
                id:'statuscomboId'+this.heplmodeid+this.id,
                store:(this.businessPerson == "Customer")? Wtf.DOStatusStore : Wtf.GROStatusStore,
                anchor:"94%",
//                allowBlank:false,
                valueField:'id',
                displayField:'name',
                mode: 'local',
                triggerAction:'all',
                forceSelection:true
            });
                
       this.DOStatusCombo.addNewFn=this.addDOStatus.createDelegate(this);
            
           
       this.sequenceFormatCombobox = new Wtf.form.ComboBox({            
//        labelSeparator:'',
//        labelWidth:0,
        triggerAction:'all',
        mode: 'local',
        fieldLabel: WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
        id:'sequenceFormatCombobox'+this.heplmodeid+this.id,
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
        
        this.Number=new Wtf.form.TextField({
            fieldLabel:(this.isEdit?this.label:this.titlel) + " " + WtfGlobal.getLocaleText("acc.common.number"),  //,  //this.label+' Number*',
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
            maxLength:2048,
            readOnly:this.readOnly, 
            disabled:this.readOnly, 
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
            {name:'externalcurrencyrate'},
            {name:'shipvia'},
            {name:'fob'},
            {name:'permitNumber'},
            {name:'amountdue'},
            {name:'contractstatus'},
            {name:'contract'},
            {name:'costcenterid'},
            {name:'costcenterName'},
            {name:'memo'},
            {name:'posttext'},
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
            {name: 'vendcustShippingAddressType'},
            /**
             * If Show Vendor Address in purchase side document and India country 
             * then this Fields used to store Vendor Billing Address
             */
            {name: 'vendorbillingAddressTypeForINDIA'},
            {name: 'vendorbillingAddressForINDIA'},
            {name: 'vendorbillingCountryForINDIA'},
            {name: 'vendorbillingStateForINDIA'},
            {name: 'vendorbillingPostalForINDIA'},
            {name: 'vendorbillingEmailForINDIA'},
            {name: 'vendorbillingFaxForINDIA'},
            {name: 'vendorbillingMobileForINDIA'},
            {name: 'vendorbillingPhoneForINDIA'},
            {name: 'vendorbillingContactPersonForINDIA'},
            {name: 'vendorbillingRecipientNameForINDIA'},
            {name: 'vendorbillingContactPersonNumberForINDIA'},
            {name: 'vendorbillingContactPersonDesignationForINDIA'},
            {name: 'vendorbillingWebsiteForINDIA'},
            {name: 'vendorbillingCountyForINDIA'},
            {name: 'vendorbillingCityForINDIA'}
        ]);
        this.POStoreUrl = "";
        if(this.businessPerson=="Customer"){
            //mode:(this.isCustBill?52:42)
            this.POStoreUrl = "ACCSalesOrderCMN/getSalesOrders.do";
        }else if(this.businessPerson=="Vendor"){
            this.POStoreUrl = "ACCPurchaseOrderCMN/getPurchaseOrders.do";
        }
        this.POStore = new Wtf.data.Store({
            url:this.POStoreUrl,
    //        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
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
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Link"),  //"Link to "+(this.isCustomer?"Sales":"Purchase")+" Order",
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
            store:this.fromlinkStore,                        
            emptyText: (this.isFixedAsset)?(this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectaSI") : WtfGlobal.getLocaleText("acc.field.SelectaPI")):(this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectaSO") : WtfGlobal.getLocaleText("acc.field.SelectaPO/VI")),
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Linkto"),  //"Link to "+(this.isCustomer?"Sales":"Purchase")+" Order",
            allowBlank:false,     
            id:'fromLinkComboId'+this.heplmodeid+this.id,
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
        
        this.fromLinkCombo.on('select', function(){
            if (this.fromLinkCombo.getValue() == 1 && !this.isCustomer) {
                this.isPILinkedInGR = true;
                this.Grid.isPILinkedInGR = true
            }
        },this);
        
//        this.PO= new Wtf.form.FnComboBox({
//            fieldLabel:WtfGlobal.getLocaleText("acc.field.Number") ,  //(this.isCustomer?"SO":"PO")+" Number",
//            hiddenName:"ordernumber",
//            id:"orderNumber"+this.heplmodeid+this.id,
//            allowBlank:false, 
//            store: this.POStore,
//            valueField:'billid',
//            hideLabel:false,
//            hidden:false,
//            displayField:'billno',
//            disabled:true,
//            emptyText: this.isCustomer ? "Select a SO/CI" : "Select a PO/VI",
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

        this.MSComboconfig = {  //multiselect combo
            hiddenName:"ordernumber",
            //id:"orderNumber"+this.heplmodeid+this.id,
            allowBlank:false, 
            store: this.POStore,
            valueField:'billid',
            hideLabel:false,
            hidden:false,
            displayField:'billno',
            disabled:true,
            clearTrigger:this.isEdit ? false : true,
            emptyText: (this.isFixedAsset)?(this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectaSI") : WtfGlobal.getLocaleText("acc.field.SelectaPI")):(this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectaSO") : WtfGlobal.getLocaleText("acc.field.SelectaPO/VI")),
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,                        
            triggerAction:'all',
            scope:this
//            listeners:{                       
//                'blur':{
//                    fn:this.populateData,
//                    scope:this
//                }
//            }
        };

        this.PO = new Wtf.common.Select(Wtf.applyIf({
             multiSelect:true,
             fieldLabel:WtfGlobal.getLocaleText("acc.field.Number") ,
             id:"poNumberID"+this.heplmodeid+this.id,
             forceSelection:true,
             hideTrigger1:true,
             width:240
        },this.MSComboconfig));
        
        this.PO.on("clearval",function(){
            if(this.PO.getValue()=="" && !this.isEdit && !this.handleEmptyText){            
                this.Grid.getStore().removeAll();            
                this.Grid.addBlankRow();            
            }
            this.handleEmptyText=false;
        },this);                
    
            this.PO.addNewFn=this.addOrder.createDelegate(this,[false,null,this.businessPerson+"PO"],true)
            
           this.POSelected="";
        if (Wtf.account.companyAccountPref.enableLinkToSelWin && (this.moduleid == Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_DeliveryOrder_ModuleId || this.moduleid==Wtf.Acc_Lease_DO)) {
            this.POStore.on('load',function(){addMoreOptions(this.PO,this.PORec)}, this);            
            this.POStore.on('datachanged', function(){addMoreOptions(this.PO,this.PORec)}, this);            
            this.PO.on("select", function () {
                var billid = this.PO.getValue();
                if (billid.indexOf("-1") != -1) {
                    var url="";
                    if (this.fromLinkCombo.getValue() == 0) {
                        url = this.isCustomer ? "ACCSalesOrderCMN/getSalesOrders.do" : "ACCPurchaseOrderCMN/getPurchaseOrders.do";
                    } else if (this.fromLinkCombo.getValue() == 1) {
                        url = this.isCustomer ? "ACCInvoiceCMN/getInvoices.do" : "ACCGoodsReceiptCMN/getGoodsReceipts.do";
                    }
                    this.PO.collapse();
                    this.PO.clearValue();
                    this.showPONumbersGrid(url);
                }
            }, this);
        }
        this.billDate= new Wtf.form.DateField({
            fieldLabel:(this.isLeaseFixedAsset? WtfGlobal.getLocaleText("acc.lease.DO") : WtfGlobal.getLocaleText("acc.dimension.module.40")) +' '+WtfGlobal.getLocaleText("acc.invoice.date"),
            id:"invoiceDate"+this.heplmodeid+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'billdate',
            width:240,
//            listeners:{
//                'change':{
//                    fn:this.updateDueDate,
//                    scope:this
//                }
            //},
            allowBlank:false
////            disabled: true
//            maxValue: new Date(Wtf.serverDate),
//            minValue: new Date(Wtf.account.companyAccountPref.fyfrom)
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
            name: 'shipvia',
            id:"shipvia"+this.heplmodeid+this.id,
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
        
        this.permitNumber = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.PermitNumber"),
            name: 'permitNumber',
            id:'permitNumberId'+this.heplmodeid+this.id,
            anchor:'94%',
            hidden:true,
            hideLabel:true,
            maxLength: 255,
            scope: this
        });
//       if(this.isCustomer){        
//          WtfGlobal.hideFormElement(this.NumberField);
//       }
        
        chkFormCostCenterload();
        this.CostCenter= new Wtf.form.ExtFnComboBox({
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
            anchor:'94%',          
            triggerAction:'all',
            addNewFn:this.addCostCenter,
            scope:this,
            hidden: this.quotation,
            hideLabel: this.quotation
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
            '<td><b>In Stock: </b></td><td style="width:10%;">{qty}</td>'+
            '<td><b>Open PO: </b></td><td style="width:10%;">{poqty}</td>'+
            '<td><b>Open SO: </b></td><td style="width:40%;">{soqty}</td>'+                        
            '</tr>'+
            '</table>'+
            '</div>',            
            '<div><hr class="templineview"></div>',                        
            '</div>'
        );   
            
        this.productDetailsTpl=new Wtf.Panel({
            border:false,
            baseCls:'tempbackgroundview',
            width:'95%',            
            html:this.productDetailsTplSummary.apply({productname:"&nbsp;&nbsp;&nbsp;&nbsp;",qty:0,soqty:0,poqty:0})
        });
        if(this.isFixedAsset){
        this.productDetailsTpl.hide();}
        var itemArr={};
            itemArr = [this.Name,this.Currency,{
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
           ]},this.PO,this.sequenceFormatCombobox,this.Number,this.billDate];
   
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
//            height:250,
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
                        columnWidth:0.65,
                        border:false,
                        items:itemArr
                    },{
                        layout:'form',
                        columnWidth:0.35,
                        border:false,
                        items:[this.CostCenter,this.DOStatusCombo,this.Memo, this.shipDate, this.shipvia, this.fob,this.permitNumber]
                    }]
                },this.tagsFieldset]
            }]
        });
        this.helpMessage= new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.dashboard.help"),  //'Help',
            handler:this.helpmessage,
            scope:this,
            hidden:this.readOnly,
            tooltip: WtfGlobal.getLocaleText("acc.common.click"),  //'Click for help',
            iconCls: 'help'
        });
        this.addGrid();

        this.NorthForm.doLayout();
       // this.POStore.on('load',this.updateSubtotal,this)
        this.billDate.on('change',this.onDateChange,this);
        
        this.southPanel=new Wtf.Panel({
            region:'center',
            border:false,
            disabledClass:"newtripcmbss",
            style:'padding:0px 10px 10px 10px',
            layout:'column',//layout:'border',//Bug Fixed: 14871[SK] Scrolling issue : changed layout from border to column
            height:(Wtf.isIE?210:150),
            items:[{
                columnWidth: .45,// width: 570,//region:'center',
                border:false,
                items:[this.productDetailsTpl]
            }]
    });              
        
        this.NorthForm.doLayout();
        this.southPanel.doLayout();
        
        this.setTransactionNumber();        
        if(this.isEdit) {
//            this.loadRecord();
            this.loadEditableGrid();
        }
           
    },

    addDOStatus: function(){
        (this.businessPerson == "Customer")? addMasterItemWindow('10') : addMasterItemWindow('11');
    },
    onDateChange:function(a,val,oldval){
        this.val=val;
        this.oldval=oldval;
        if(val != oldval){
            this.isbilldateChanged = true;
            /*
            *	ERM-1037
            *	On date change send id of selected document to function to restrict linking of future dated document
            */
            var selectedBillIds = this.PO.getValue().toString();
            if (selectedBillIds != "") {
                var selectedValuesArr = selectedBillIds.split(',');
                WtfGlobal.checkForFutureDate(this, selectedValuesArr)
            }
        }
        /*
         *On date change we are setting Purchase Date and Installation Date.
         *Only for without link cases.
         */
        if(!(this.isPILinkedInGR || this.moduleid == Wtf.Acc_FixedAssets_DeliveryOrder_ModuleId)){
            for(var i=0;i<this.Grid.getStore().getCount()-1;i++){
                var rec =  this.Grid.getStore().getAt(i);
                var assetDetails =rec.get('assetDetails');
                
                if(assetDetails != "" && assetDetails != undefined){
                    var assetDetailArray = eval('(' + assetDetails + ')');
                    for(var j=0;j<assetDetailArray.length;j++){
                        if( assetDetailArray[j].purchaseDate != ""){
                            assetDetailArray[j].purchaseDate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
                        }
                        if( assetDetailArray[j].installationDate != ""){
                            if(Wtf.account.companyAccountPref.depreciationCalculationType != 0){
                                assetDetailArray[j].installationDate = WtfGlobal.convertToGenericDate(this.billDate.getValue());
                            }
                        }
                    }   
                    rec.set('assetDetails', JSON.stringify(assetDetailArray));
                    rec.commit();
                }
            }
        }
        this.externalcurrencyrate=0;
        this.custdatechange=true;
        this.Grid.loadPriceStoreOnly(val,this.Grid.priceStore);
        if(this.Grid){
                 this.Grid.billDate=this.billDate.getValue();
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

    addGrid:function(){
              this.Grid=new Wtf.account.FixedAssetDeliveryOrderGrid({
                    height: 300,//region:'center',//Bug Fixed: 14871[SK]
                    cls:'gridFormat',
                    layout:'fit',
                    viewConfig:{forceFit:false},
                    isCustomer:this.isCustomer,
                    editTransaction:this.isEdit,
                    disabledClass:"newtripcmbss",
                    isCustBill:false,
                    id:this.id+"billingproductdetailsgrid",
                    moduleid:this.moduleid,
                    parentCmpID:this.id,
                    isFixedAsset:this.isFixedAsset,
                    isLeaseFixedAsset:this.isLeaseFixedAsset,
                    currencyid:this.Currency.getValue(),
                    fromOrder:true,
                    isOrder:this.isOrder,
                    isEdit:this.isEdit,
                    copyTrans:this.copyInv, 
                    forceFit:true,
                    readOnly:this.readOnly,
                    parentObj :this,
                    record:this.record,
                    isViewTemplate : this.isViewTemplate,
                    isPILinkedInGR: this.isPILinkedInGR,
                    loadMask : true,
                    isGST : this.isGST
                });
                this.Grid.on("productdeleted", this.removeTransStore, this);
                this.Grid.on("datachanged", this.applyCurrencySymbol, this);
                if(!this.isEdit && !this.copyInv && this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){
                    this.Grid.productComboStore.load();                    
                }
       // this.Name.on('select',this.setTerm,this)
        this.NorthForm.on('render',this.setDate,this);  
          if(this.readOnly) {
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
                    this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{
                        productname:prorec.data['productname']+" ",
                        qty:parseFloat(getRoundofValue(prorec.data['quantity'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"  "+prorec.data['uomname'],
                        soqty:parseFloat(getRoundofValue(prorec.data['socount'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"  "+prorec.data['uomname'],
                        poqty:parseFloat(getRoundofValue(prorec.data['pocount'])).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)+"  "+prorec.data['uomname']               
                    });
                }                                        
        },this);
    },
     disabledbutton:function()
   {
      this.CostCenter.setDisabled(true);
      this.DOStatusCombo.setDisabled(true);
      this.shipDate.setDisabled(true);
      this.shipvia.setDisabled(true);
      this.fob.setDisabled(true);
      this.permitNumber.setDisabled(true);
      this.billDate.setDisabled(true);
      this.Name.setDisabled(true); 
      this.Currency.setDisabled(true); 
      this.fromPO.setDisabled(true);
      this.fromLinkCombo.setDisabled(true);
      this.PO.setDisabled(true);
      this.sequenceFormatCombobox.setDisabled(true);
      this.Number.setDisabled(true);
      this.billDate.setDisabled(true);
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
        
        if(this.Grid){      
            this.Grid.fromPO=false;
        }
        this.fromLinkCombo.clearValue();
        this.PO.clearValue();
        this.CostCenter.clearValue();
        this.DOStatusCombo.clearValue();
        //this.Name.clearValue();
        this.Memo.setValue("");

        if(rec.data['value']==true){                                                
            this.fromLinkCombo.enable();                        
            this.fromOrder=true;
            this.isPILinkedInGR=true;
        }
        else{
            this.Grid.getStore().removeAll();            
            this.Grid.addBlankRow();
            this.fromLinkCombo.disable();
            this.PO.disable();
            this.PO.reset();
            this.isPILinkedInGR=false;
        }
        //this.currencyStore.load(); 	       // Currency id issue 20018
    },

    enableNumber:function(c,rec){
        
        this.PO.clearValue();
        this.CostCenter.clearValue();
        this.DOStatusCombo.clearValue();
        //this.Name.clearValue();
        this.Memo.setValue("");
        this.Grid.getStore().removeAll();            
        this.Grid.addBlankRow();
        
            if(rec.data['value']==0){
                //this.PO.multiSelect=true;
                //this.isMultiSelectFlag=true;
                //this.PO.removeListener("select",this.populateData,this);
                this.PO.addListener("blur",this.populateData,this);
                this.POStore.proxy.conn.url = this.isCustomer ? "ACCSalesOrderCMN/getSalesOrders.do" : "ACCPurchaseOrderCMN/getPurchaseOrders.do";
                this.POStore.load({params:{id:this.Name.getValue(),exceptFlagORD:true, currencyfilterfortrans:this.Currency.getValue(),isLeaseFixedAsset:this.isLeaseFixedAsset}});        
                this.PO.enable(); 
                this.POSelected="sales";
            } else if(rec.data['value']==1){
                //this.PO.multiSelect=false;
                //this.isMultiSelectFlag=false;
                //this.PO.removeListener("blur",this.populateData,this);
                this.PO.addListener("blur",this.populateData,this);
                //this.PO.addListener("select",this.populateData,this);                
                this.POStore.proxy.conn.url = this.isCustomer ? "ACCInvoiceCMN/getInvoices.do" : "ACCGoodsReceiptCMN/getGoodsReceipts.do";
                var params={cashonly:false,creditonly:true,currencyfilterfortrans:this.Currency.getValue(),isFixedAsset:this.isFixedAsset,isLeaseFixedAsset:this.isLeaseFixedAsset,nondeleted:true};
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
//            	            
            this.billid=this.record.data.billid;
            this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;
            this.Grid.getStore().on("load", function(){
                this.loadRecord();
            }, this);
            this.Grid.getStore().load({params:{bills:this.billid,isFixedAsset:this.isFixedAsset,moduleid:this.moduleid,isEdit:this.isEdit,isLeaseFixedAsset:this.isLeaseFixedAsset}});
    },
    
    populateData:function(c,rec) {
        this.singleLink = false;
        if(this.PO.getValue()!=""){
            
            if(this.isLeaseFixedAsset && this.fromLinkCombo.getValue() == 0){
                var soIdsArray = this.PO.getValue().split(",");
                var isMultipleContractsSelected = WtfGlobal.isMultipleContractsSelected(soIdsArray,this.PO.store);

                if(isMultipleContractsSelected){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.linking.so.selection.msg") ], 3);
                    return;
                }
            }
            
            this.Grid.fromPO=true;
            var billid=this.PO.getValue();
            var selectedids=this.PO.getValue();
            var selectedValuesArr = selectedids.split(',');
            if(billid.indexOf(",")==-1){  //In MultiSelection if the user select only one                            
                rec=this.POStore.getAt(this.POStore.find('billid',billid));
                this.linkRecord = this.POStore.getAt(this.POStore.find('billid',billid));
                this.singleLink = true;
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
                if (rec != undefined) {
                    /*
                    *	ERM-1037
                    *	If rec is not undefined send id of selected linked document to restrict linking of future dated document
                    */
                   var isFutureDatedDocumentLinked=WtfGlobal.checkForFutureDate(this, selectedValuesArr);
                   if(isFutureDatedDocumentLinked){
                       return;
                   }
                }
                this.resetCustomFields();
                var fieldArr = this.POStore.fields.items;
                for(var fieldCnt=0; fieldCnt < fieldArr.length; fieldCnt++) {
                    var fieldN = fieldArr[fieldCnt];
                   
                    if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id) && record.data[fieldN.name] !="") {
                          if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).getXType()=='datefield'){
                             Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(record.data[fieldN.name]);
                          }else if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).xtype=='fncombo' || Wtf.getCmp(fieldN.name+this.tagsFieldset.id).getXType()=='fncombo'){
                                var ComboValue=record.data[fieldN.name];
//                                var ComboValueID="";
//                                var recCustomCombo =WtfGlobal.searchRecord(Wtf.getCmp(fieldN.name+this.tagsFieldset.id).store,ComboValue,"name");
                                if(ComboValue){
//                                    ComboValueID=recCustomCombo.data.id;
                                    Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue(ComboValue);
                                    var  parent= Wtf.getCmp(fieldN.name+this.tagsFieldset.id).parentid;
                                    var displayValue = record.json[fieldN.name+ "_linkValue"];
                                    if (parent != undefined && displayValue != undefined && parent.length > 0) {
                                        if (displayValue) {
                                            Wtf.getCmp(fieldN.name + this.tagsFieldset.id).setValForChildComboStore(ComboValue, displayValue); // create record and set value
                                        }
                                    } 
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
                                    var ComboValue=record.data[fieldN.name];
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
                                    var ComboValue=record.data[fieldN.name];
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
                }
                var linkedRecordExternalCurrencyRate=rec.data["externalcurrencyrate"];
                if (this.Currency.getValue()!=WtfGlobal.getCurrencyID && linkedRecordExternalCurrencyRate!="" && linkedRecordExternalCurrencyRate!=undefined) { // If selected currency is foreign currency then currency exchange rate will be exchange rate of linked document except in cross link case
                    this.externalcurrencyrate=linkedRecordExternalCurrencyRate;
                }
            }else{
                this.Memo.setValue('');                
                this.shipDate.setValue('');
                this.shipvia.setValue('');
                this.fob.setValue('');                                                
                this.permitNumber.setValue('');                                                
                this.CostCenter.setValue('');
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
            /*
            *	ERM-1037
            *	Send id of selected document to function to restrict linking of future dated document
            */
            WtfGlobal.checkForFutureDate(this,selectedValuesArr);
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
            this.billDate.setValue(new Date());
//            this.billDate.setValue(Wtf.serverDate);//(new Date());            
        }
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
        if(this.loadCurrFlag && Wtf.account.companyAccountPref.currencyid && !this.isEdit){
            this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
            this.loadCurrFlag = false;
        }
        if(this.isEdit){
            var data=this.record.data;
            this.Currency.setValue(data.currencyid);
            this.updateFormCurrency();
        }
        
        if(this.currencyStore.getCount()==0){
            callCurrencyExchangeWindow();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
            this.billDate.setValue("");
        } else
            this.updateFormCurrency();
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
                var arr=[];
                var key="";
                var isDuplicateAssetPresent=false;
                var checkTotalAmtEqualForDO=false;
                var checkTotalAmtEqualForGR=false;
                if (this.isFixedAsset) {
                    if (Wtf.account.companyAccountPref.unitPriceInGR) {
                        checkTotalAmtEqualForGR = true;
                    }
                    if (Wtf.account.companyAccountPref.unitPriceInDO) {
                        checkTotalAmtEqualForDO = true;
                    }
                }
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
                        //while creating 'Asset GR' Amount mismatch in 'Asset Window' and Grid
                        var rate=this.Grid.getStore().getAt(i).data['rate'];
                        
                        if (checkTotalAmtEqualForDO ||checkTotalAmtEqualForGR) {
                            var assetDetailTotalCost = 0;
                            for (var j = 0; j < assetDetailArray.length; j++) {
                                if (checkTotalAmtEqualForDO &&this.moduleid == Wtf.Acc_FixedAssets_DeliveryOrder_ModuleId) {
                                    if (assetDetailArray[j].sellAmount !== "") {
                                        assetDetailTotalCost += parseFloat(assetDetailArray[j].sellAmount);
                                    }
                                } else if (checkTotalAmtEqualForGR && this.moduleid == Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId) {
                                    if (assetDetailArray[j].costInForeignCurrency !== "") {
                                        assetDetailTotalCost += parseFloat(assetDetailArray[j].costInForeignCurrency);
                                    }
                                }

                            }
                            var rateQuantityVal = rate * quantity - assetDetailTotalCost;
                            rateQuantityVal = (rateQuantityVal < 0) ? (-1) * rateQuantityVal : rateQuantityVal;
                            
                            if (rateQuantityVal > Wtf.decimalLimiterValue) {// due to java script rounding off problem
                                var groupName = this.Grid.getStore().getAt(i).data['productname'] + '</b>';
                                var warnMessage = WtfGlobal.getLocaleText("acc.field.RateenteredisnotequaltoAssetDetailstotalCostvalueforAssetGroup") + groupName;
                                if (checkTotalAmtEqualForGR && this.moduleid == Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId) {
                                    warnMessage = WtfGlobal.getLocaleText("acc.field.RateenteredisnotequaltoAssetDetailstotalCostvalueforAssetGroup") + groupName;
                                    WtfComMsgBox(['Information', warnMessage], 0);
                                    return;
                                }
                                if (checkTotalAmtEqualForDO && this.moduleid == Wtf.Acc_FixedAssets_DeliveryOrder_ModuleId) {
                                    warnMessage = WtfGlobal.getLocaleText("acc.field.RateenteredisnotequaltoAssetDetailstotalSellAmountvalueforAssetGroup") + '<b>' + groupName;
                                    WtfComMsgBox(['Information', warnMessage], 0);
                                    return;
                                }
                            }
                        }
                        if (this.isFixedAsset && this.moduleid == Wtf.Acc_FixedAssets_DeliveryOrder_ModuleId) {
                            for (var j = 0; j < assetDetailArray.length; j++) {
                                if (assetDetailArray[j].assetId !== "" && assetDetailArray[j].assetId !== undefined) {
                                    if (arr.indexOf(key + assetDetailArray[j].assetId) >= 0) {
                                        isDuplicateAssetPresent = true;
                                        break;
                                    } else {
                                        arr.push(key + assetDetailArray[j].assetId);
                                    }
                                }
                            }
                        }
                        var batchDetails = assetDetailArray[assetDetailArray.length-1].batchdetails;        //checking batch details for all quantity of respective product
                        if (Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory) { //if company level option is on then only check batch and serial details
                            if (proRecord.get('isBatchForProduct') || proRecord.get('isSerialForProduct') || proRecord.get('isLocationForProduct') || proRecord.get('isWarehouseForProduct') || proRecord.get('isRowForProduct') || proRecord.get('isRackForProduct') || proRecord.get('isBinForProduct')) {
                                if (batchDetails == undefined || batchDetails == "" || batchDetails == "[]") {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.bsdetailforassetgroup") + ' for Asset Group ' + this.Grid.getStore().getAt(i).data['productname']], 2);   //Batch and serial no details are not valid.
                                    return;
                                }
                            }
                        }
                }
            }
             if (isDuplicateAssetPresent) {
                    WtfComMsgBox(["Warning", "Duplicate Asset ID's are given for same Asset Group."], 2);
                    return;
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
//            if(this.getDiscount()>this.Grid.calSubtotal()){  ***************** Check for delivered quantity greater than actual Quantity *********8
//                WtfComMsgBox(12, 2);
//                return;
//            }
            incash=this.cash;
            var rec=this.NorthForm.getForm().getValues();
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
                return;
            }
            if(detail == undefined || detail == "[]"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
                return;
            }
            var prodLength=this.Grid.getStore().data.items.length;
        for(var i=0;i<prodLength-1;i++)
{ 
            var prodID=this.Grid.getStore().getAt(i).data['productid'];
//            var prorec=this.Grid.productComboStore.getAt(this.Grid.productComboStore.find('productid',prodID));
            var prorec = (WtfGlobal.searchRecord(this.Grid.productComboStore,prodID,'productid')!=null)?WtfGlobal.searchRecord(this.Grid.productComboStore,prodID,'productid'):WtfGlobal.searchRecord(this.Grid.getStore(),prodID,'productid');
            if(Wtf.account.companyAccountPref.isBatchCompulsory || Wtf.account.companyAccountPref.isSerialCompulsory || Wtf.account.companyAccountPref.islocationcompulsory || Wtf.account.companyAccountPref.iswarehousecompulsory || Wtf.account.companyAccountPref.isRowCompulsory || Wtf.account.companyAccountPref.isRackCompulsory || Wtf.account.companyAccountPref.isBinCompulsory){ //if company level option is on then only check batch and serial details
                if(prorec.data.isBatchForProduct || prorec.data.isSerialForProduct || prorec.data.isLocationForProduct || prorec.data.isWarehouseForProduct || prorec.data.isRowForProduct || prorec.data.isRackForProduct  || prorec.data.isBinForProduct){ 
                    if(prorec.data.type!='Service' && !prorec.get('isAsset')){
                        var batchDetail= this.Grid.getStore().getAt(i).data['batchdetails'];
                        var productQty= this.Grid.getStore().getAt(i).data['dquantity'];
                        var baseUOMRateQty= this.Grid.getStore().getAt(i).data['baseuomrate'];
                       
                        if(batchDetail == undefined || batchDetail == ""){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);   //Batch and serial no details are not valid.
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
                            if((batchQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) != (productQty*baseUOMRateQty).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL)){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.bsdetail")],2);
                                return;
                            }                       
                        }
                    }
                }
            }                       
        }
            var leaselectValue=this.PO.getValue();
        if(leaselectValue!=undefined&& leaselectValue!=""){
            var poindex=this.POStore.findBy( function(rec){
                var parentname=rec.data['billid'];
                if(parentname==leaselectValue)
                    return true;
                else
                    return false;
            })
            if(poindex>=0) {
                var leaseRec= this.POStore.getAt(poindex);
                if(leaseRec.data.contractstatus==2){
                    WtfComMsgBox(116, 2);
                    return false;
                } 
            }
                
        }
          if(rec.fromLinkCombo!=undefined  && (this.moduleid==27 ||this.moduleid == Wtf.Acc_FixedAssets_DeliveryOrder_ModuleId))  //in link case check available quantity should be greater than delivered quantity
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
                                return true;
                            }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                                    if(btn=="yes"){
                                        this.showConfirmAndSave(rec,detail,incash);
                                    }else{
                              
                                        return true;
                                    }
                                },this); 
                            }        
                        }else{
                            this.showConfirmAndSave(rec,detail,incash);   
                        }
                   }else  if((availableQuantity-lockQuantity)<quantity){  //for DO for linked with SO which is not linked and for Invoice
                    if(Wtf.account.companyAccountPref.negativestock==1){ // Block case
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prodName+WtfGlobal.getLocaleText("acc.field.is")+(availableQuantity-lockQuantity)+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                        return true;
                    }else if(Wtf.account.companyAccountPref.negativestock==2){     // Warn Case
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+'</center>' , function(btn){
                                if(btn=="yes"){
                                    this.showConfirmAndSave(rec,detail,incash);
                                }else{
                              
                                    return true;
                                }
                            },this); 
                        } else{//ignore
                            this.showConfirmAndSave(rec,detail,incash);
                        }                
                    }else{
                        this.showConfirmAndSave(rec,detail,incash);   
                    }
               }
            }
        }else   //if DO is made noramal withaot linking
        {
            this.showConfirmAndSave(rec,detail,incash);
        }
             
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
                rec.isFixedAsset=this.isFixedAsset;
                rec.isLeaseFixedAsset=this.isLeaseFixedAsset;
                rec.externalcurrencyrate=this.externalcurrencyrate;
                rec.transType=this.moduleid;
                rec.isfavourite=false;
                rec.posttext=this.postText;
                rec.sequenceformat=this.sequenceFormatCombobox.getValue();
                var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):false;
                if(!this.copyInv){
                    if((this.record && this.record !== undefined) && (this.record.get('isfavourite') !== null || this.record.get('isfavourite') !== undefined)){
                        rec.isfavourite = this.record.get('isfavourite');
                    }
                }
                rec.currencyid=this.Currency.getValue();
            var isCopy = this.copyInv;
            var isEdit = this.isEdit;
            if (this.isVenOrCustSelect) {
                isEdit = false;
                isCopy = false;
            }
                rec=WtfGlobal.getAddressRecordsForSave(rec,this.record,this.linkRecord,this.currentAddressDetailrec,this.isCustomer,this.singleLink,isEdit,isCopy);
                rec.isEdit=this.isEdit;
                Wtf.Ajax.requestEx({
                    url:this.ajxurl,
                    params: rec                    
                },this,this.genSuccessResponse,this.genFailureResponse);
                },this);
    },
    genSuccessResponse:function(response, request){
        if (this.isEdit && (this.moduleid == Wtf.Acc_Lease_DO && !response.accException)) {
            WtfComMsgBox([this.label, response.msg], !response.success * 2 + 0);
        } else if((this.moduleid == Wtf.Acc_FixedAssets_DeliveryOrder_ModuleId || this.moduleid == Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId)&& response.accException) { //if any accounting exeception throws then we are showing alert message.
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],2);
        } else if (response.isTaxDeactivated) {
            WtfComMsgBox([this.title, "You cannot save " + this.title + " with deacivated tax(s)."], 2);
        } else if(this.moduleid==Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId   && Wtf.getCmp("GoodsReceiptDeliveryOrderListEntryFixedAsset") != undefined && Wtf.isAutoRefershReportonDocumentSave){
            var title = this.titlel;//scope not available in on load function of store
            Wtf.getCmp("GoodsReceiptDeliveryOrderListEntryFixedAsset").Store.on('load', function() {
                WtfComMsgBox([title,response.msg],response.success*2+1);
            }, Wtf.getCmp("GoodsReceiptDeliveryOrderListEntryFixedAsset").Store, {
                single : true
            });
        }  else if(this.moduleid==Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId && Wtf.getCmp("GoodsReceiptDeliveryOrderListEntry") != undefined && Wtf.isAutoRefershReportonDocumentSave){
            var title = this.titlel;//scope not available in on load function of store
            Wtf.getCmp("GoodsReceiptDeliveryOrderListEntry").Store.on('load', function() {
                WtfComMsgBox([title,response.msg],response.success*2+1);
            }, Wtf.getCmp("GoodsReceiptDeliveryOrderListEntry").Store, {
                single : true
            });    
        } else{
            WtfComMsgBox([this.titlel, response.msg], response.success * 2 + 1);
         }
         if(response.success){  
            while(Wtf.dupsrno.length>0){
                Wtf.dupsrno.pop();
            } 
            Wtf.productStoreSales.reload();
            Wtf.productStore.reload(); 
            var rec=this.NorthForm.getForm().getValues();
            this.exportRecord=rec;
            this.exportRecord['billid']=response.billid||response.invoiceid;
            this.exportRecord['billno']=response.billno||response.invoiceNo;
            this.exportRecord['amount']=response.amount||"";
            if(this.singlePrint){
                this.singlePrint.exportRecord=this.exportRecord;//Reload all product information to reflect new quantity, price etc  
            }
            if(this.printRecords){
                this.printRecords.exportRecord=this.exportRecord;//Reload all product information to reflect new quantity, price etc  
            }
            if(this.mailFlag){
                this.loadUserStore(response, request);
                this.disableComponent();
//                Wtf.getCmp("emailbut" + this.id).enable();
//                Wtf.getCmp("exportpdf" + this.id).enable();
                this.response = response;
                this.request = request;
//                this.fireEvent('update',this);
                return;
            }
            this.Grid.getStore().removeAll();
            this.setTransactionNumber();
            this.fromLinkCombo.disable();
            this.fromPO.disable();
            this.PO.setDisabled(true);
            this.NorthForm.getForm().reset();
            this.sequenceFormatStore.load();
            this.currencyStore.load();      
            this.Currency.setValue(WtfGlobal.getCurrencyID()); // Reset to base currency 
            this.externalcurrencyrate=0; //Reset external exchange rate for new Transaction.
            this.currentAddressDetailrec="";
            this.singleLink = false;
            this.isVenOrCustSelect=false;
            this.Grid.updateRow(null);
            this.fromPO.setValue(false); 
            this.Grid.priceStore.purgeListeners();
            this.Grid.loadPriceStoreOnly(new Date(),this.Grid.priceStore);
            this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{productname:"&nbsp;&nbsp;&nbsp;&nbsp;",qty:0,soqty:0,poqty:0});
            this.fireEvent('update',this);
            this.postText="";
            if(!this.mailFlag){//Clear custom columns in save and create new case
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
         }
    },

    genFailureResponse:function(response){
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
//            var rec = this.CustomStore.getAt(0);
            var rec = "";
            if (response.billid != undefined || response.billid != '') {
                rec = this.CustomStore.getAt(this.CustomStore.find('billid', response.billid));
            }
            var label = "";
            if(this.isCustomer){
                label = WtfGlobal.getLocaleText("acc.fixedAssetDeliveryOrder");
                callEmailWin("emailwin",rec,label,53,true,false,false,false,false,true);
            }else{
                label = WtfGlobal.getLocaleText("acc.accPref.autoGRO");
                callEmailWin("emailwin",rec,label,54,false,false,false,false,false,true);
            }
        }
},

disableComponent: function(){  // disable following component in case of save button press.
    
    if(this.fromLinkCombo && this.fromLinkCombo.getValue() === ''){
//        this.fromLinkCombo.emptyText = "";
        this.fromLinkCombo.clearValue();
    }
    
    if(this.PO && this.PO.getValue() === ''){
        this.handleEmptyText=true;
//        this.PO.emptyText = "";
        this.PO.clearValue();
    }
    
    if(Wtf.getCmp("save" + this.heplmodeid + this.id)){
        Wtf.getCmp("save" + this.heplmodeid + this.id).disable();
    }
    if(Wtf.getCmp("savencreate" + this.heplmodeid + this.id)){
        Wtf.getCmp("savencreate" + this.heplmodeid + this.id).disable();
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
//        this.Grid.disable();
    }
    
    if(this.NorthForm){
        this.NorthForm.disable();
    }
    if(this.southPanel){
        this.southPanel.disable();
    }
},
    getAddressWindow:function(){
       var custvendorid=this.Name.getValue();
       var addressRecord="";
        if (this.linkRecord && this.singleLink) {            //when user link single record
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
        /*
             For India GST State As Combo in customer and vendor masters if Customer/Vendor type is Export (WPAY),Export (WOPAY),Import
             */
            if (WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied()) {
                this.stateAsComboFlag = true;
                if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
                    this.custVenId = Wtf.GSTCUSTVENTYPE.NA;
                    var index = this.Name.store.find('accid', this.Name.getValue());
                    if (index != -1) {
                        var storerec = this.Name.store.getAt(index);
                        this.custVenId = storerec.data.CustVenTypeDefaultMstrID;
                    }
                    this.stateAsComboFlag = (this.custVenId == undefined || !(this.custVenId == Wtf.GSTCUSTVENTYPE.Export || this.custVenId == Wtf.GSTCUSTVENTYPE.ExportWOPAY || this.custVenId == Wtf.GSTCUSTVENTYPE.Import)) ? true : false
                }
            }
       callAddressDetailWindow(addressRecord,isEdit,isCopy,custvendorid,this.currentAddressDetailrec,this.isCustomer,this.readOnly,"",this.singleLink,undefined,WtfGlobal.getModuleId(this),null,null,null,null,this.stateAsComboFlag); 
       Wtf.getCmp('addressDetailWindow').on('update',function(config){
            this.currentAddressDetailrec=config.currentaddress;
            if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
                this.populateGSTDataOnAddressChange();
            }
        },this);
    },
     /**
     * Function to get GST fields data based on date.
     */
    applyGSTFieldsBasedOnDate: function (isAddressChanged) {
        if (this.Name.getValue() == undefined || this.Name.getValue() == ''){
           return;
        }
        Wtf.Ajax.requestEx({
            url: this.isCustomer ? "ACCCustomerCMN/getCustomerGSTHistory.do" : "ACCVendorCMN/getVendorGSTHistory.do",
            params: {
                customerid: this.Name.getValue(),
                vendorid: this.Name.getValue(),
                returnalldata: true,
                isfortransaction: true,
                transactiondate: WtfGlobal.convertToGenericDate(this.billDate.getValue())
            }
        }, this, function (response) {
            if (response.success) {
                /**
                 * Validate GST details
                 */
                isGSTDetailsPresnetOnTransactionDate(response, this, this.Grid, this.Name);
                this.ignoreHistory = true;
                this.GSTINRegistrationTypeId = response.data[0].GSTINRegistrationTypeId;
                this.gstin = response.data[0].gstin;
                this.CustomerVendorTypeId = response.data[0].CustomerVendorTypeId;
                this.uniqueCase = response.data[0].uniqueCase;
                this.transactiondateforgst = this.billDate.getValue();
                this.CustVenTypeDefaultMstrID=response.data[0].CustVenTypeDefaultMstrID;
                this.GSTINRegTypeDefaultMstrID=response.data[0].GSTINRegTypeDefaultMstrID;
                var cust_Vendparams = {};
                var record = {};
                record.data = response.data[0];
                cust_Vendparams.rec = record;
                cust_Vendparams.isCustomer = this.isCustomer;
                checkAndAlertCustomerVendor_GSTDetails(cust_Vendparams);
                if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA) {
                    if (!this.isCustomer) {
                        if (response.data[0].GSTINRegTypeDefaultMstrID != undefined && response.data[0].GSTINRegTypeDefaultMstrID !== "" && response.data[0].GSTINRegTypeDefaultMstrID === Wtf.GSTRegMasterDefaultID.Unregistered) {
                            this.purchaseFromURD = true;
                        } else {
                            this.purchaseFromURD = false;
                        }
                    }
                }
                /**
                 * On Address Changes done then need to make getGSTForProduct Request to update tax Details
                 */
                if(this.keepTermDataInLinkCase==false || isAddressChanged){
                    processGSTRequest(this, this.Grid);
                }
            }
        });
    },
    /**
      * ERM-886
      * On Edit/ Copy case set GST details
      */
    populateGSTDataOnEditCopy: function () {
        // this.copyInv - in VQ  copy 
        if (this.isGST && (this.isEdit || this.copyInv)) {
            this.individualPersonDetails = new Wtf.data.Store({
                url: this.isCustomer ? "ACCCustomer/getCustomersForCombo.do" : "ACCVendor/getVendorsForCombo.do",
                baseParams: {
                    mode: 2,
                    group: this.isCustomer ? 10 : 13,
                    deleted: false,
                    nondeleted: true,
                    common: '1'
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                }, Wtf.personRec)
            });
            this.individualPersonDetails.on('load', function (storeObj, recArr) {
                var index = this.individualPersonDetails.find('accid', this.record.data.personid);
                if (index != -1) {
                    var record = this.individualPersonDetails.getAt(index);
                    this.setGSTDetailsOnEditCase(record);
                }
            }, this);
            if (this.isCustomer) {
                this.individualPersonDetails.load({
                    params: {
                        selectedCustomerIds: this.record.data.personid
                    },
                    scope: this
                });
            } else {
                this.individualPersonDetails.load({
                    params: {
                        vendorid: this.record.data.personid
                    },
                    scope: this
                });
            }
        }
    },
/**
 * ERM-886
 * On Edit/ Copy case set GST details
 */    
    setGSTDetailsOnEditCase: function (record) {
        this.addressMappingRec = record.data.addressMappingRec;
    },
      populateGSTDataOnAddressChange: function () {
        /**
         * auto poulate dimension values
         */
        if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
            var obj = {};
            obj.tagsFieldset = this.tagsFieldset;
            this.addressDetailRecForGST=this.currentAddressDetailrec;
            obj.currentAddressDetailrec = this.addressDetailRecForGST;
            /**
             * On Edit case this.addressMappingRec not defined 
             */
            var person = this.Name.getValue();
            if(person!='' && this.addressMappingRec==undefined){
                var personIndex = this.Name.store.find('accid',person);
                if(personIndex!=-1){
                    var personRec = this.Name.store.getAt(personIndex);
                   this.addressMappingRec =  personRec.data && personRec.data.addressMappingRec ? personRec.data.addressMappingRec : "";
                }
            }
            obj.mappingRec = this.addressMappingRec;
            obj.isCustomer = this.isCustomer;
            obj.isShipping = this.isShipping;
            obj.stateAsComboFlag = this.stateAsComboFlag;
            var invalid = populateGSTDimensionValues(obj);
            /**
             * On Address Changes done then need to make getGSTForProduct Request to update tax Details
             */
            var isAddressChanged = true;
            this.applyGSTFieldsBasedOnDate(isAddressChanged);
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
        {name:'isprinted'}
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
                ss:request.params.number,
                isFixedAsset:this.isFixedAsset,
                isLeaseFixedAsset:this.isLeaseFixedAsset
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

exportPdfFunction : function(){
    if(this.CustomStore != null){
        var rec = this.CustomStore.getAt(0);
        var recData = rec.data;
        var selRec = "&amount="+0+"&bills="+recData.billid;
        var fileName = "";
        var mode = "";
        if(this.isCustomer){
            fileName = "Delivery Order "+recData.billno+this.version;
            mode = 53;
        }else{
            fileName="Goods Receipt "+recData.billno+this.version;
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
    checkForDeActivatedProductsAdded:function(){
    var invalidProducts='';
    if(this.isEdit && !this.copyInv){ //Edit Case
        var linkedDocuments = this.PO.getValue();
        var linkedDocsArray=[];
        if(linkedDocuments != ''){
            linkedDocsArray = linkedDocuments.split(',');
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
    } else { // Create New and copy
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
        return inValidProducts; // List of deactivated products
    }
});
