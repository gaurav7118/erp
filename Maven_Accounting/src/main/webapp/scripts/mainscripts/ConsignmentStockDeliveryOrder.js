
/*< COMPONENT USED FOR >
 *      1.Delivery Order
 *          callDeliveryOrder(isEdit,rec,winid) --- < Create delivery Order > 
 *          [isEdit=true/false, isCustomer=true, isOrder=true, record]
 *      Note -: callDeliveryOrder(isEdit,rec,winid) function defined in WtfTransactionManager.js    
 */

Wtf.account.ConsignmentStockDeliveryOrderPanel=function(config){	
    this.id=config.id;
    this.isFixedAsset = (config.isFixedAsset!=null && config.isFixedAsset!=undefined)?config.isFixedAsset:false;
    this.isLeaseFixedAsset = (config.isLeaseFixedAsset)?config.isLeaseFixedAsset:false;
    this.isConsignment = (config.isConsignment)?config.isConsignment:false;
    this.isMovementWarehouseMapping=Wtf.account.companyAccountPref.isMovementWarehouseMapping;
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
    this.currentAddressDetailrec="";
    this.singleLink=false;
    this.linkRecord="";
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    this.readOnly=config.readOnly;
    var help=getHelpButton(this,config.heplmodeid);
    this.businessPerson=(config.isCustomer?'Customer':'Vendor');
    this.modeName = config.modeName;
    this.moduleid=config.moduleid;
    this.linkflag=config.linkflag;
    this.isLinkedTransaction = (config.isLinkedTransaction == null || config.isLinkedTransaction == undefined)? false : config.isLinkedTransaction;
    this.originallyLinkedDocuments = '';
//    this.CUSTOM_KEY = "customfield";
    (this.businessPerson == "Customer")? Wtf.DOStatusStore.load() : Wtf.GROStatusStore.load();
    Wtf.apply(this, config);
//    this.custUPermType=config.isCustomer?Wtf.UPerm.customer:Wtf.UPerm.vendor;
//    this.custPermType=config.isCustomer?Wtf.Perm.customer:Wtf.Perm.vendor;
//    this.soUPermType=(config.isCustomer?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
//    this.soPermType=(config.isCustomer?Wtf.Perm.invoice.createso:Wtf.Perm.vendorinvoice.createpo);
    if( this.isConsignment){
        this.uPermType=config.isCustomer?Wtf.UPerm.consignmentsales:Wtf.UPerm.consignmentpurchase;
        this.permType= config.isCustomer?Wtf.Perm.consignmentsales:Wtf.Perm.consignmentpurchase;   
        this.exportPermType=config.isCustomer?this.permType.exportsalescondo:this.permType.exportpurchasecondo;
        this.printPermType=config.isCustomer?this.permType.printsalescondo:this.permType.printpurchasecondo;
    }
    var tranType=null;
    if(config.moduleid==28||config.moduleid==27 || config.moduleid==51 || config.moduleid==57 ){
        if(config.moduleid==27 || config.moduleid==51){
            tranType=Wtf.autoNum.DeliveryOrder;
        }else{
            tranType=Wtf.autoNum.GoodsReceiptOrder;
        }   
    }
    var singlePDFtext = null;
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
     
      this.singleRowPrint = new Wtf.exportButton({
        obj: this,
        id: "printSingleRecord"+ this.id,
        iconCls: 'pwnd printButtonIcon',
        text: WtfGlobal.getLocaleText("acc.rem.236"),
        tooltip: WtfGlobal.getLocaleText("acc.rem.236.single"), //'Print Single Record Details',
        disabled: this.readOnly?false:true,
        isEntrylevel: false,
        exportRecord:this.exportRecord,
        menuItem: {
            rowPrint: true
        },
        get: tranType,
        moduleid:Wtf.Acc_ConsignmentDeliveryOrder_ModuleId,
        hidden:config.moduleid==Wtf.Acc_ConsignmentDeliveryOrder_ModuleId?false:true
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
     
    Wtf.apply(this,{
        bbar:[{
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
            tooltip:WtfGlobal.getLocaleText("acc.rem.175"),
            id:"save"+config.heplmodeid+this.id,
            hidden:this.readOnly,
            scope:this,
            handler:function(){
                this.mailFlag = true;
                if(this.isLinkedTransaction && !this.isCopy) {
                    this.update();
                } else {
                    this.save();
                }
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
        },!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)?this.singlePrint:"",!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)?this.singleRowPrint:"",
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
    Wtf.account.ConsignmentStockDeliveryOrderPanel.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.ConsignmentStockDeliveryOrderPanel,Wtf.account.ClosablePanel,{
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
            this.NorthForm.getForm().loadRecord(this.record);
            this.currencyStore.on('load',function () {
                this.Currency.setValue(data.currencyid);
                this.updateFormCurrency();
            },this);
            if(!this.copyInv)
                this.Number.setValue(data.billno);            
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
                                billno:rec.data.linkto,
                                /*
                                     ERM-1037
                                     Field used for comparing dates to restrict linking of future doument date 
                                 */
                                date:rec.data.linkDate
                            });
                            storeData.push(newRec);
                        }
                    }
                },this);
                if(storeData.length>0){
                    this.POStore.add(storeData);
                }
                if(linkIDS.length>0){
                    this.originallyLinkedDocuments = linkIDS.toString();
                    if(this.Grid){      
                        this.Grid.fromPO=true;
                    }
                    this.Name.disable();
                    this.fromPO.disable();
                    this.fromLinkCombo.disable();
                    this.PO.disable();
                    this.fromPO.setValue(true);                
                    this.PO.setValue(linkIDS);
                    /*
                     *ERM-1037 
                     *Set the date of document before comparing date to restrict linking from future dated document
                     **/
                    this.billDate.setValue(data.date);
                    this.loadLinkNumbers(this.PO.getValue());
                    this.LinkMemoAddress.setValue(data.ordernoreferedformemoaddress);
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
            if(this.isConsignment && this.isCustomer) {    //in edit case assign warehouse
                this.wareHouseStore.on("load", function(){
                    this.warehouses.setValue(data.custWarehouse);
                }, this);
                this.wareHouseStore.load();
            }
            if(this.isConsignment && this.isCustomer && this.isMovementWarehouseMapping) {
                    var store1=Wtf.movmentTypeStore
                    var index1=store1.findBy( function(rec){
                        var id=rec.data['id'];
                        if(id==data.movementtype)
                            return true;
                        else
                            return false;
                    })
                    if(index1>=0)
                        this.movmentType.setValue(data.movementtype);
            }
            this.dataLoaded=true;
//            this.Grid.priceStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
            //this.bankTypeStore.load();
            //this.DOStatusCombo.setValue(data.statusID);
            if(this.copyInv){
            	this.billDate.setValue(Wtf.serverDate);
//            	this.updateDueDate();
            }
            if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){  
                this.Grid.productComboStore.load({params:{mappingProduct:true,customerid:this.Name.getValue(),common:'1', loadPrice:true,mode:54}}) ;           
            } 
        }
    },
    onRender:function(config){                
        this.add(this.NorthForm,this.Grid,this.southPanel);                       
        Wtf.account.ConsignmentStockDeliveryOrderPanel.superclass.onRender.call(this, config);
        if(!this.isCustomer){
            this.permitNumber.hideLabel=true;
            this.permitNumber.hidden=true;
        }
        this.initForClose();   
        if(this.isEdit || this.copyInv){
            this.showAddrress.enable();
        }
        // hide form fields
            this.hideFormFields();
    },
    
    hideFormFields:function(){
        if(this.isCustomer){
            this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.deliveryOrder);
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
        Wtf.account.ConsignmentStockDeliveryOrderPanel.superclass.initComponent.call(this,config);
        WtfGlobal.loadpersonacc(this.isCustomer);
        //chkcustaccload();// Global Customer store  
        
        //this.isCustomer ? chkproductSalesload() : chkproductload() ; // Global Product store for product sales
        
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
        this.sequenceFormatStore.on('load',function(){
            if(this.sequenceFormatStore.getCount()>0){
                var count=this.sequenceFormatStore.getCount();
                for(var i=0;i<count;i++){
                    var seqRec=this.sequenceFormatStore.getAt(i);
                    if(seqRec.json.isdefaultformat=="Yes"){
                        this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                        break;
                    }
                }
                if(!this.isEdit){
                    if(this.sequenceFormatCombobox.getValue()!=""){
                        this.getNextSequenceNumber(this.sequenceFormatCombobox);
                    } else{
                        this.Number.setValue("");
                        WtfGlobal.hideFormElement(this.Number);
                    }
                }
            }
        },this);
     this.sequenceFormatStore.load();
     
        this.Name= new Wtf.form.ExtFnComboBox({
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
            mode: 'local',
            typeAheadDelay:30000,
            minChars:1,
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            isVendor:!(this.isCustomer),
            isCustomer:this.isCustomer,
//            width:240,
             anchor:"80%",
            triggerAction:'all',
 //           addNewFn:this.addPerson.createDelegate(this,[false,null,this.businessPerson+"window",this.isCustomer],true),
          //  scope:this
            listeners:{
                'select':{
                    fn:function(obj,rec,index){
                        var customer= this.Name.getValue();
                        if(rec.data.currencyid!=this.Currency.getValue()){//update currency field with vendor currency if vendor currency is different
                          this.Currency.setValue(rec.data.currencyid);
                          this.currencychanged = true;
                          this.updateFormCurrency();   
                        }
                        this.fromLinkCombo.clearValue();
                        this.PO.clearValue();
                        this.CostCenter.clearValue();
                        this.movmentType.clearValue();
                        this.DOStatusCombo.clearValue();
                       if(!this.isEdit && !this.copyInv){   //in edit and copy case do not remove record from store
                        this.Grid.getStore().removeAll();
                        this.Grid.addBlankRow();
                       }    
                        this.fromLinkCombo.disable();
                        this.PO.disable();
                        this.PO.reset();
                        this.fromPO.setValue(false);
                        this.Memo.setValue('');
                        this.warehouses.clearValue();
                        this.postText=(this.record)?this.record.data.posttext:"";
                        this.singleLink = false;
                        if (this.isEdit || this.isCopy) {
                            this.isVenOrCustSelect = true;
                        }
                        this.currentAddressDetailrec="";//If customer/vendor change in this case,previously stored addresses in this.currentAddressDetailrec will be clear    
                        this.showAddrress.enable();
                        this.shipDate.setValue('');
                        this.shipvia.setValue('');
                        this.fob.setValue('');                                                
                        this.permitNumber.setValue('');                                                
                        this.CostCenter.setValue('');
                        this.movmentType.setValue('');
                        this.Name.setValue(customer);
                        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{productname:"&nbsp;&nbsp;&nbsp;&nbsp;",qty:0,soqty:0,poqty:0});
                        if(this.fromPO && !this.isCustomer)
                            this.fromPO.enable(); 
//                             this.fromPO.setValue(true);
                        if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){  
                          this.Grid.productComboStore.load({params:{mappingProduct:true,customerid:this.Name.getValue(),common:'1', loadPrice:true,mode:54}}) ;           
                        } 
                        if(this.isCustomer)
                        {this.wareHouseStore.load({params:{customerid:this.Name.getValue()}})};
                        var customer = this.Name.getValue();
                        this.tagsFieldset.resetCustomComponents();
                        var moduleid = this.isCustomer ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId;
                        this.tagsFieldset.setValuesForCustomer(moduleid, customer);
                    },
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
//            width : 240,
            anchor:"80%",
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
            }else if(this.isConsignment){
                fromLinkStoreRec.push([WtfGlobal.getLocaleText("acc.dimension.module.50"), 0]);
            }else{
                fromLinkStoreRec.push(['Sales Order', 0]);
                fromLinkStoreRec.push(['Customer Invoice', 1]);
            }
            
        } else {
            if(this.isFixedAsset){
                fromLinkStoreRec.push(['Purchase Invoice',1]);
              }else if(this.isConsignment){
                fromLinkStoreRec.push([WtfGlobal.getLocaleText("acc.dimension.module.50"), 0]);
            }else{
                fromLinkStoreRec.push(['Purchase Order',0]);
                fromLinkStoreRec.push(['Vendor Invoice',1]);
            } 
        }
        
        
        
        this.fromlinkStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value'}],
            data:fromLinkStoreRec
        });
        
        chkmovementtypeload();
        this.movmentType = new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:WtfGlobal.getLocaleText("acc.field.RequestType"),
            valueField:'id',
            displayField:'name',
            hideLabel:!this.isConsignment || !this.isMovementWarehouseMapping || !this.isCustomer,
            hidden:!this.isConsignment || !this.isMovementWarehouseMapping || !this.isCustomer,
            store:Wtf.movmentTypeStore,
            anchor:"80%",
//             width : 240,
            lastQuery:'',
            typeAhead: true,
            forceSelection: true,
            disabled:(this.isEdit&&!this.copyInv?true:false),
            name:'movemettype',
            hiddenName:'movemettype'
//              listeners:{
//                'select':{
//                    fn:function(){
//                        this.PO.clearValue();
//                        this.Grid.fromPO=false;
//                        if(this.isConsignment){
//                           this.enableNumber(this.fromLinkCombo,this.fromLinkCombo.store.getAt(0));
//                        }else{
//                             this.fromLinkCombo.clearValue();
//                             this.fromPO.setValue(false);
//                             this.fromPO.disable();  
//                              this.fromLinkCombo.disable();
//                        }
//                       if(this.fromPO && this.warehouses.getValue()!="")
//                            this.fromPO.enable();   
//                    },
//                scope:this                    
//                }
//            }

        }); 
        
        this.movmentType.on('select',function(){
            this.Grid.movmentType=this.movmentType.getValue();
        },this);
        
          this.warehouseRec = new Wtf.data.Record.create([//  warehouse record
            {name: 'id'},
            {name: 'name'},
            {name: 'customer'},
            {name: 'company'},
            {name: 'warehouse'},
            {name: 'isdefault'}
        ]);
        this.wareHouseStore = new Wtf.data.Store({  //  warehouse store for combo box
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"
        },this.warehouseRec),
        //              url:"ACCCustomerCMN/getCustomerWarehouses.do",
        url:"ACCCustomerCMN/getAllCustomerWarehouse.do",
        baseParams:{
            isForCustomer:true
        }
    });
    this.wareHouseStore.on('load',function(){
        var index=this.wareHouseStore.find('isdefault','T');
        var rec =this.wareHouseStore.getAt(index);
        if(rec != undefined && !this.isEdit){
            this.warehouses.setValue(rec.data.warehouse);
            this.warehouses.fireEvent('select',this);
        }
    },this)   
             
        this.warehouses= new Wtf.form.FnComboBox({     //  warehouse store
            triggerAction:'all',
            mode: 'local',
//            selectOnFocus:true,
            valueField:'warehouse',
            displayField:'name',
            hideLabel:!this.isConsignment || !this.isCustomer,
            disabled:this.isEdit,
            hidden:!this.isConsignment || !this.isCustomer,
            id:"warehouse"+this.heplmodeid+this.id,
//            store:this.isCustomer ? Wtf.salesPersonStore : Wtf.agentStore,
            store:this.wareHouseStore,
            addNoneRecord: true,
              anchor:"80%",
//            width : 240,
//            typeAhead: true,
            forceSelection: true,
            fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.Consignment.Warehouses"):"",
            emptyText:'Select Warehouses...',
            name:'custWarehouse',
            hiddenName:'custWarehouse',
           listeners:{
                'select':{
                    fn:function(){
//                        this.fromLinkCombo.clearValue();
                        this.PO.clearValue();
                        this.movmentType.clearValue();
//                        this.fromPO.reset();
//                        this.PO.reset();
//                        this.fromLinkCombo.reset();
                        this.Grid.fromPO=false;
                        if(this.isConsignment){
                            this.fromPO.setValue(true);
                            this.fromPO.enable();   
                            this.fromLinkCombo.enable();
                            this.fromLinkCombo.setValue(0);
                            this.enableNumber(this.fromLinkCombo,this.fromLinkCombo.store.getAt(0));
                        }else{
                             this.fromLinkCombo.clearValue();
                             this.fromPO.setValue(false);
                             this.fromPO.disable();  
                              this.fromLinkCombo.disable();
                        }
                       if(this.fromPO && this.warehouses.getValue()!="")
                            this.fromPO.enable();   
                    },
                scope:this                    
                }
            }
        });
//        this.wareHouseStore.load();
        
        this.DOStatusCombo =  new Wtf.form.FnComboBox({
                fieldLabel:WtfGlobal.getLocaleText("acc.field.Status*"),
                name:"statuscombo",     
                id:'statuscomboId'+this.heplmodeid+this.id,
                store:(this.businessPerson == "Customer")? Wtf.DOStatusStore : Wtf.GROStatusStore,
                anchor:"94%",
                allowBlank:false,
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
        anchor:"80%",
//        width:240,
        typeAhead: true,
        forceSelection: true,
        name:'sequenceformat',
        hiddenName:'sequenceformat',
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
//              width:240,
            anchor:"80%",
            maxLength:50,
            scope:this,
            allowBlank:this.checkin
        });
        this.Memo=new Wtf.form.TextArea({
            fieldLabel:Wtf.account.companyAccountPref.descriptionType,  //'Memo',
            name: 'memo',
            id:"memo"+this.heplmodeid+this.id,
            height:50,
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
            {name:'amountinbase'},
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
            {name:'movementtype'},
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
                    var newField = new Wtf.data.ExtField({
                        name:fieldname.replace(".",""),
        //                   sortDir:'ASC',
                        type:DimensionCustomFielsArray[cnt].fieldtype == 3 ?  'date' : (DimensionCustomFielsArray[cnt].fieldtype == 2?'float':'auto'),
                        dateFormat:DimensionCustomFielsArray[cnt].fieldtype == 3 ? 'time' : undefined
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
            //width:70,
            anchor:"100%",
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
//            emptyText: (this.isFixedAsset)?(this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectaSI") : WtfGlobal.getLocaleText("acc.field.SelectaPI")):(this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectaSO") : WtfGlobal.getLocaleText("acc.field.SelectaPO/VI")),
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectConsRequest"),
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Linkto"),  //"Link to "+(this.isCustomer?"Sales":"Purchase")+" Order",
            allowBlank:false,     
            id:'fromLinkComboId'+this.heplmodeid+this.id,
//            value:false,            
            typeAhead: true,            
//            width:207,
            anchor:"100%",
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
//            emptyText: (this.isFixedAsset)?(this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectaSI") : WtfGlobal.getLocaleText("acc.field.SelectaPI")):(this.isCustomer ? WtfGlobal.getLocaleText("acc.field.SelectaSO") : WtfGlobal.getLocaleText("acc.field.SelectaPO/VI")),
             emptyText:WtfGlobal.getLocaleText("acc.field.SelectConsRequest"),
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
//             width:240
              anchor:"80%"
        },this.MSComboconfig));
        
            this.LinkMemoAddressRec = Wtf.data.Record.create ([
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
                {name:'memo'},
                {name:'posttext'},
                {name:'movementtype'},
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

            this.LinkMemoAddressStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data",
                    totalProperty:'count'
                },this.LinkMemoAddressRec)
            });

        this.LinkMemoAddress = new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
            fieldLabel:"Request No. for Memo & Add",
            valueField:'billid',
            displayField:'billno',
            allowBlank:true,  
            hideLabel:!this.isCustomer,
            hidden: !this.isCustomer,
            store:this.LinkMemoAddressStore,
            anchor:"80%",
            lastQuery:'',
            typeAhead: true,
            forceSelection: true,
            disabled:true,
            name:'linkmemoaddress',
            hiddenName:'linkmemoaddress'

        }); 
        
        this.LinkMemoAddress.on("select", function () {
            var billid = this.LinkMemoAddress.getValue();
            var rec=this.POStore.getAt(this.POStore.find('billid',billid));
            this.Memo.setValue(rec.data['memo']);
            this.shipDate.setValue(rec.data['shipdate']);
            this.shipvia.setValue(rec.data['shipvia']);
            this.fob.setValue(rec.data['fob']);
            this.CostCenter.setValue(rec.data['costcenterid']); 
            this.linkRecord=rec;
        },this);          
    
        if (Wtf.account.companyAccountPref.enableLinkToSelWin && (this.moduleid == Wtf.Acc_ConsignmentDeliveryOrder_ModuleId || this.moduleid==Wtf.Acc_Consignment_GoodsReceiptOrder_ModuleId)) {
            this.POStore.on('load', function () {
                addMoreOptions(this.PO, this.PORec)
            }, this);
            this.POStore.on('datachanged', function () {
                addMoreOptions(this.PO, this.PORec)
            }, this);
            this.PO.on("select", function () {
                var billid = this.PO.getValue();
                if (billid.indexOf("-1") != -1) {
                    var url = "";
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
        this.PO.on("clearval",function(){
            if(this.PO.getValue()=="" && !this.isEdit && !this.handleEmptyText){            
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
            this.handleEmptyText=false;
            this.movmentType.reset();
        },this);                
    
            this.PO.addNewFn=this.addOrder.createDelegate(this,[false,null,this.businessPerson+"PO"],true)
            
           this.POSelected="";
        
        this.billDate= new Wtf.form.DateField({
            fieldLabel:(this.isEdit?this.label:this.titlel) +' '+WtfGlobal.getLocaleText("acc.invoice.date"),
            id:"invoiceDate"+this.heplmodeid+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'billdate',
//             width:240,
             anchor:"80%",
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
            hidden:!this.isCustomer,
            hideLabel:!this.isCustomer,
            name: 'shipdate',
            anchor:'94%'
        });
        this.shipvia = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.ShipVia"),
            name: 'shipvia',
            id:"shipvia"+this.heplmodeid+this.id,
            anchor:'94%',
            hidden:!this.isCustomer,
            hideLabel:!this.isCustomer,
            maxLength: 255,
            scope: this
        });
        
        this.fob = new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.fob.tip")+"'>"+WtfGlobal.getLocaleText("acc.field.FOB") +"</span>",
            name: 'fob',
            id:"fob"+this.heplmodeid+this.id,
            anchor:'94%',
            hidden:!this.isCustomer,
            hideLabel:!this.isCustomer,
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
            
        chkLineLevelCostCenterload(); //chkFormCostCenterload();
        this.CostCenter= new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.costCenter.tip") +"'>"+ WtfGlobal.getLocaleText("acc.common.costCenter")+"</span>",//WtfGlobal.getLocaleText("acc.common.costCenter"),//"Cost Center",
            hiddenName:"costcenter",
            id:"costcenter"+this.heplmodeid+this.id,
            store: Wtf.LineLevelCostCenterStore,
            valueField:'id',
            displayField:'name',
            extraComparisionField:'ccid', 
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['ccid']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
            isProductCombo: true,
            maxHeight:250,
            lastQuery:'',
            hirarchical:true,
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
        
//        this.CostCenter.listWidth=300;

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
    this.tplSummary=new Wtf.XTemplate(
        '<div class="currency-view">',
        '<table width="100%">',
        '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoice.subTotal")+' </b></td><td text-align=right>{subtotal}</td></tr>',
        '</table>',
//        '<table width="100%">',
//        '<tr><td><b>+ '+WtfGlobal.getLocaleText("acc.invoice.Tax")+': </b></td><td align=right>{tax}</td></tr>',
//        '</table>',
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
//        '<table width="100%">',
//        '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.inv.amountdue")+' </b></td><td align=right>{amountdue}</td></tr>',
//        '</table>',
        '<hr class="templineview">',
        '<hr class="templineview">',
        '</div>'
        );
            
    this.productDetailsTpl=new Wtf.Panel({
        border:false,
        baseCls:'tempbackgroundview',
        width:'95%',            
        html:this.productDetailsTplSummary.apply({
            productname:"&nbsp;&nbsp;&nbsp;&nbsp;",
            qty:0,
            soqty:0,
            poqty:0
        })
    });
        
   
    
    this.southCalTemp=new Wtf.Panel({  
        border:false,
        baseCls:'tempbackgroundview',
        //        style:'position: relative; left: 0px; top: 0px; width: 1590px',
        width:'95%',
        html:this.tplSummary.apply({
            subtotal:WtfGlobal.currencyRenderer(0),
//            tax:WtfGlobal.currencyRenderer(0),
            aftertaxamt:WtfGlobal.currencyRenderer(0),
            totalAmtInBase:WtfGlobal.currencyRenderer(0),
            amountdue:WtfGlobal.currencyRenderer(0)
        })
    });
        var itemArr={};
            itemArr = [this.Name,this.Currency,this.warehouses,{
                layout:'column',
                border:false,
                defaults:{border:false},items:[ {
                    layout:'form',
                    ctCls : "",
                    labelWidth:155,
//                    width:215,
                     columnWidth:0.42,
              
                    items:this.fromPO
                },
                {
//                    width:250,
                    layout:'form',
                    columnWidth:0.38,
                    bodyStyle:'padding-left:15px',
                    labelWidth:45,
                    items:this.fromLinkCombo
               }
           ]},this.PO,this.LinkMemoAddress,this.movmentType,this.sequenceFormatCombobox,this.Number,this.billDate];
   
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
//            height:250,
            autoHeight: true,
            id:"northForm"+this.id,
            disabledClass:"newtripcmbss",
            disabled:this.readOnly,
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
                        columnWidth:0.5,
                        border:false,
                        items:itemArr
                    },{
                        layout:'form',
                        columnWidth:0.5,
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
            tooltip: WtfGlobal.getLocaleText("acc.common.click"),  //'Click for help',
            iconCls: 'help'
        });
        this.addGrid();

        this.NorthForm.doLayout();
       // this.POStore.on('load',this.updateSubtotal,this)
        this.billDate.on('change',this.onDateChange,this);
        
    //        this.southPanel=new Wtf.Panel({
    //            region:'center',
    //            border:false,
    //            disabledClass:"newtripcmbss",
    //            disabled:this.isViewTemplate,
    //            style:'padding:0px 10px 10px 10px',
    //            layout:'column',//layout:'border',//Bug Fixed: 14871[SK] Scrolling issue : changed layout from border to column
    //            height:(Wtf.isIE?210:150),
    //            items:[{
    //                columnWidth: .45,// width: 570,//region:'center',
    //                border:false,
    //                items:[this.productDetailsTpl,this.southSummaryPanel]
    //            }]            
    //        });
        
           this.southPanel=new Wtf.Panel({
            region:'center',
            border:false,
            disabledClass:"newtripcmbss",
            disabled:this.isViewTemplate,
            style:'padding:0px 10px 10px 10px',
            layout:'column',
            autoHeight:true,
            items:[{
                columnWidth: .45,// width: 570,//region:'center',
                border:false,
                items:[this.productDetailsTpl]
            },{
                columnWidth: .20,
                layout:'column',// width: 570,//region:'center',
                items:[
                {
                    columnWidth:0.55,
                    layout:'form',
                    border:false
                }, {
                    columnWidth:0.43,
                    layout:'form',
                    labelWidth:30,
                    border:false
                        
                }], 
                border:false
            },{
                columnWidth:.35,
                layout:'form',
                cls:'bckgroundcolor',
                bodyStyle:'padding:10px',
                labelWidth:70,
                items:[this.southCalTemp]
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
    getAddressWindow:function(){
        var addressRecord="";
        var custvendorid=this.Name.getValue();
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
        if(this.linkRecord && this.LinkMemoAddress.getValue() != undefined && this.LinkMemoAddress.getValue() != ""){
            addressRecord=this.linkRecord;
            callAddressDetailWindow(addressRecord,isEdit,isCopy,custvendorid,this.currentAddressDetailrec,this.isCustomer,this.readOnly,"",true,this.LinkMemoAddress.getValue(),this.moduleid); 
        }else{
        callAddressDetailWindow(addressRecord,isEdit,isCopy,custvendorid,this.currentAddressDetailrec,this.isCustomer,this.readOnly,"",this.singleLink,this.PO.getValue(),this.moduleid); 
        }
        Wtf.getCmp('addressDetailWindow').on('update',function(config){
            this.currentAddressDetailrec=config.currentaddress;
        },this);
    },
    addDOStatus: function(){
        (this.businessPerson == "Customer")? addMasterItemWindow('10') : addMasterItemWindow('11');
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
    },
    
    onDateChange:function(a,val,oldval){
        /*
         *On changing date function called to compare the date for restrincting linking of future dated document. 
         **/
        var selectedBillIds=this.PO.getValue().toString();
        if (selectedBillIds!= ""){
            var selectedValuesArr = selectedBillIds.split(',');
            WtfGlobal.checkForFutureDate(this,selectedValuesArr);
        }
        this.val=val;
        this.oldval=oldval;
        this.loadTax(val);
        this.externalcurrencyrate=0;
        this.custdatechange=true;
        //this.Grid.loadPriceStoreOnly(val,this.Grid.priceStore);
        
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
//            tax:tax,
            aftertaxamt:aftertaxamt,
            totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol())
//            amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())
        });
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
              this.Grid=new Wtf.account.ConsignmentStockDeliveryorderGrid({
                    height: 300,//region:'center',//Bug Fixed: 14871[SK]
                    cls:'gridFormat',
                    layout:'fit',
                    viewConfig:{forceFit:true},
                    isCustomer:this.isCustomer,
                    editTransaction:this.isEdit,
                    disabledClass:"newtripcmbss",
                    isCustBill:false,
                    id:this.id+"billingproductdetailsgrid",
                    moduleid:this.moduleid,
                    parentCmpID:this.id,
                    isFixedAsset:this.isFixedAsset,
                    isLeaseFixedAsset:this.isLeaseFixedAsset,
                    isConsignment:this.isConsignment,
                    currencyid:this.Currency.getValue(),
                    fromOrder:true,
                    isOrder:this.isOrder,
                    isEdit:this.isEdit,
                    readOnly:this.readOnly,
                    isViewMode:this.readOnly,
                    copyTrans:this.copyInv, 
                    forceFit:true,
                    parentObj :this,
                     isLinkedTransaction:this.isLinkedTransaction,
                    loadMask : true
                });
                this.Grid.on("productdeleted", this.removeTransStore, this);
                this.Grid.on("datachanged", this.applyCurrencySymbol, this);
                if(this.productOptimizedFlag!= undefined && this.productOptimizedFlag==Wtf.Show_all_Products){  
                    if(!this.isEdit && !this.copyInv){
                        this.Grid.productComboStore.load();
                    }
                }
       // this.Name.on('select',this.setTerm,this)
       this.Name.on('beforeselect', function(combo, record, index) {
                return validateSelection(combo, record, index);
         
        }, this);
        this.NorthForm.on('render',this.setDate,this);        
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
        this.movmentType.clearValue();
        this.DOStatusCombo.clearValue();
        //this.Name.clearValue();
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
            this.PO.reset();
        }
        //this.currencyStore.load(); 	       // Currency id issue 20018
    },

    enableNumber:function(c,rec){
        
        this.PO.clearValue();
        this.CostCenter.clearValue();
        //this.movmentType.clearValue();
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
                this.POStore.load({params:{id:this.Name.getValue(),exceptFlagORD:true, currencyfilterfortrans:this.Currency.getValue(),isLeaseFixedAsset:this.isLeaseFixedAsset,isConsignment:this.isConsignment,custWarehouse:this.warehouses.getValue(),movementtype:this.movmentType.getValue()}});        
                this.PO.enable(); 
                this.POSelected="sales";
            } else if(rec.data['value']==1){
                //this.PO.multiSelect=false;
                //this.isMultiSelectFlag=false;
                //this.PO.removeListener("blur",this.populateData,this);
                this.PO.addListener("blur",this.populateData,this);
                //this.PO.addListener("select",this.populateData,this);                
                this.POStore.proxy.conn.url = this.isCustomer ? "ACCInvoiceCMN/getInvoices.do" : "ACCGoodsReceiptCMN/getGoodsReceipts.do";
                var params={cashonly:false,creditonly:true,currencyfilterfortrans:this.Currency.getValue(),isFixedAsset:this.isFixedAsset,isLeaseFixedAsset:this.isLeaseFixedAsset,isConsignment:this.isConsignment,nondeleted:true};
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
            this.isConsignment=this.record.data.isConsignment;
            this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;
            this.Grid.getStore().on("load", function(){
                this.loadRecord();
            }, this);
            this.Grid.getStore().load({params:{bills:this.billid,isConsignment:this.isConsignment,moduleid:this.moduleid,isEdit:this.isEdit}});
    },
    
    populateData:function(c,rec) {
        if(this.PO.getValue()!=""){
            this.singleLink=false;
            if(this.isLeaseFixedAsset && this.fromLinkCombo.getValue() == 0){
                var soIdsArray = this.PO.getValue().split(",");
                var isMultipleContractsSelected = WtfGlobal.isMultipleContractsSelected(soIdsArray,this.PO.store);

                if(isMultipleContractsSelected){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.linking.so.selection.msg") ], 3);
                    return;
                }
            }
                var isAnotherReuquestTypeSelected=false;
            if(this.isConsignment && this.fromLinkCombo.getValue() == 0){
                var soIdsArray = this.PO.getValue().split(",");
                 isAnotherReuquestTypeSelected = WtfGlobal.isAnotherReuquestTypeSelected(soIdsArray,this.PO.store);

                if(isAnotherReuquestTypeSelected){
                    this.PO.reset();
                     this.Grid.getStore().removeAll();
                     this.Grid.addBlankRow();
                      this.movmentType.clearValue();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.linking.conreq.selection.msg") ], 3);
                    return;
                }
            }
               if((this.isCustomer && this.warehouses.getValue()!="") || !this.isCustomer){
               this.Grid.fromPO=true;
           }
            var billid=this.PO.getValue();
            this.LinkMemoAddress.allowBlank=true;
            if(billid.indexOf(",")==-1){  //In MultiSelection if the user select only one                            
                rec=this.POStore.getAt(this.POStore.find('billid',billid));
                if (rec != undefined) {
                    /*
                     * ERM-1037
                     *If rec is not undefined send the selected linked document to compare date for restricting linking of future dated document 
                     */
                    var isFutureDatedDocumentLinked = WtfGlobal.checkForFutureDate(this, soIdsArray);
                    if (isFutureDatedDocumentLinked) {
                        return;
                    }
                }
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
                this.movmentType.setValue(rec.data.movementtype);
                this.Grid.movmentType=rec.data.movementtype;
                this.singleLink=true;
                this.linkRecord=rec;
            }else{                
                this.Memo.setValue('');                
                this.shipDate.setValue('');
                this.shipvia.setValue('');
                this.fob.setValue('');                                                
                this.permitNumber.setValue('');                                                
                this.CostCenter.setValue('');
                if(isAnotherReuquestTypeSelected){
                  this.movmentType.setValue('');
                }else{
		    var selectedRequestBillId = billid.substring(0,billid.indexOf(","));
                    var firstRect=this.POStore.getAt(this.POStore.find('billid',selectedRequestBillId));
                    if(firstRect!=null && firstRect !=undefined){
                    this.movmentType.setValue(firstRect.data.movementtype);
                    this.Grid.movmentType=firstRect.data.movementtype;
                }
               }
               /*
                *In case of Consignment Delivery Order it is mandatory refer ERP-28867
                **/
               if(this.moduleid==Wtf.Acc_ConsignmentDeliveryOrder_ModuleId){                     
                  this.LinkMemoAddress.allowBlank=false;                 
               }else{
                  this.LinkMemoAddress.allowBlank=true; 
               } 
                  this.LinkMemoAddress.enable(); 
               this.loadLinkNumbers(billid);
               this.linkRecord="";
               this.singleLink=false;
//               this.giveAddressMessage(); //due to synchonization writing seperate method for address message               
            }       
            rec=this.PO.getValue();
            //this.updateDueDate();
             var record=this.POStore.getAt(this.POStore.find('billid',billid));
             if(record!=undefined){
               this.resetCustomFields();
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
                    if(fieldN.name.indexOf("Custom_")==0){
                        var fieldname=fieldN.name.substring(7,fieldN.name.length);
                        if(Wtf.getCmp(fieldname+this.tagsFieldset.id) && record.data[fieldN.name] !="") {
                            if(Wtf.getCmp(fieldname+this.tagsFieldset.id).getXType()=='fieldset'){
                                    var ComboValue=record.json[fieldN.name];
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
                                    var ComboValue=record.json[fieldN.name];
//                                    var ComboValueArrya=ComboValue.split(',');
//                                    var ComboValueID="";
//                                    for(var i=0 ;i < ComboValueArrya.length ; i++){
//                                        var recCustomCombo =WtfGlobal.searchRecord(Wtf.getCmp(fieldname+this.tagsFieldset.id).store,ComboValueArrya[i],"name");
//                                        if(recCustomCombo){
//                                            ComboValueID+=recCustomCombo.data.id+","; 
//                                        }
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
             }
            var url = "";
                    //(this.isCustBill?53:43)
            var linkingFlag = false;   //For removing cross reference of DO-CI or GR-VI     
            var isForDOGROLinking = true;// if DO/GRO is being create with Linking to SO/PO/CI/VI
            if(this.fromLinkCombo.getValue()==0){
                url = this.isCustomer ? 'ACCSalesOrderCMN/getSalesOrderRows.do' : "ACCPurchaseOrderCMN/getPurchaseOrderRows.do";
                var linkingFlag =true;
            } else if(this.fromLinkCombo.getValue()==1){
                url = this.isCustomer ? "ACCInvoiceCMN/getInvoiceRows.do" : "ACCGoodsReceiptCMN/getGoodsReceiptRows.do";
                var linkingFlag =true;
            }
            this.Grid.getStore().proxy.conn.url = url;
            this.Grid.loadPOGridStore(rec,linkingFlag,isForDOGROLinking);
        }   
    },
  loadLinkNumbers : function(billid){
    this.LinkMemoAddressStore.removeAll();  
    var selectedRequestBillIds = billid.split(",");
    for(var numberCount=0;numberCount <= selectedRequestBillIds.length ; numberCount++ ){
        var refRecord=this.POStore.getAt(this.POStore.find('billid',selectedRequestBillIds[numberCount]));
        if(refRecord != null && refRecord != undefined){
            var record = this.LinkMemoAddressStore.reader.recordType, f = record.prototype.fields, fi = f.items, fl = f.length;
            var blankObj = {};
            for (var j = 0; j < fl; j++) {
                f = fi[j];
                if (f.name != 'id') {
                    blankObj[f.name] = '';
                }
            }
            var rec = new this.LinkMemoAddressRec(blankObj);
            rec.beginEdit();
            var fields = this.LinkMemoAddressStore.fields;
            for (var x=0; x<fields.items.length; x++) {
                var value = refRecord.data[fields.get(x).name];
                if (fields.get(x).name == 'type' && value && value != '') {
                    value = decodeURI(value);
                }
                if (fields.get(x).name == 'productname' && value && value != '') {
                    value = decodeURI(value);
                }
                if (fields.get(x).name == 'desc' && value && value != '') {
                    value = decodeURI(value);
                }
                rec.set(fields.get(x).name, value);
            }
            rec.endEdit();
            rec.commit();
            this.LinkMemoAddressStore.add(rec);
        }   
    }
    /*
     *ERM-1037
     *Send the linked documents id to compare date for restricting linking of future dated document.
     */
    WtfGlobal.checkForFutureDate(this,selectedRequestBillIds);
},  
    
giveAddressMessage:function(){
    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),this.isCustomer?WtfGlobal.getLocaleText("acc.linking.conreq.selection.addressmsgforcustomer"):WtfGlobal.getLocaleText("acc.linking.conreq.selection.addressmsgforvendor")], 3);   
},
resetCustomFields : function(){ // For reset Custom Fields, Check List and Custom Dimension
        var customFieldArray = this.tagsFieldset.customFieldArray;  // Reset Custom Fields
        if(customFieldArray!=null && customFieldArray!=undefined && customFieldArray!="" ) {
        for (var itemcnt = 0; itemcnt < customFieldArray.length; itemcnt++) {
            var fieldId = customFieldArray[itemcnt].id
            if (Wtf.getCmp(fieldId) != undefined && customFieldArray[itemcnt].getXType()!='fieldset') {
                Wtf.getCmp(fieldId).reset();
            }
        }
       }
        
        var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;  // Reset Check List
        if(checkListCheckBoxesArray!=null && checkListCheckBoxesArray!=undefined && checkListCheckBoxesArray!="" ) {
        for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
            var checkfieldId = checkListCheckBoxesArray[checkitemcnt].id
            if (Wtf.getCmp(checkfieldId) != undefined) {
                Wtf.getCmp(checkfieldId).reset();
            }
        }
        }
        
        var customDimensionArray = this.tagsFieldset.dimensionFieldArray;  // Reset Custom Dimension
        if(customDimensionArray!=null && customDimensionArray!=undefined && customDimensionArray!="" ) {
        for (var itemcnt1 = 0; itemcnt1 < customDimensionArray.length; itemcnt1++) {
            var fieldId1 = customDimensionArray[itemcnt1].id
            if (Wtf.getCmp(fieldId1) != undefined) {
                Wtf.getCmp(fieldId1).reset();
            }
        }
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
            //this.billDate.setValue(Wtf.serverDate);//(new Date());            
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
    this.tplSummary.overwrite(this.southCalTemp.body,{
        subtotal:WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),
//        tax:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),
        aftertaxamt:(this.moduleid == 28 || this.moduleid ==27) ? WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol):WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),
        totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol())
//        amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())
    });
    return this.symbol;
},
calTotalAmountInBase:function(){
    var subtotal=this.Grid.calSubtotalInBase(); 
    var taxVal = 0;//this.calAmountInBase(this.caltax());
    var returnValInOriginalCurr = subtotal +taxVal; //-discount;
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

calTotalAmount:function(){
    var subtotal=this.Grid.calSubtotal();
    //        var discount=this.getDiscount();
    //        return subtotal-discount + this.findTermsTotal();
    return subtotal;
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
    update: function() {
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.invoice.msg7"),function(btn){
        if(btn!="yes") {
            return;
        }
        this.mailFlag = true;
        var incash = this.cash;
        var rec = this.NorthForm.getForm().getValues();
        this.ajxurl = "";
        if (this.businessPerson == "Customer") {
            this.ajxurl = "ACCInvoice/updateDeliveryOrder.do";
        } else {
            this.ajxurl = "ACCGoodsReceipt/updateGoodsReceiptOrder.do";
        }
        var detail = this.Grid.getProductDetails();
        rec.detail = detail;
        rec.billdate = WtfGlobal.convertToGenericDate(this.billDate.getValue());
        rec.creationdate = WtfGlobal.convertToGenericDate(this.billDate.getValue());
        rec.cntype = this.inputValue;
        var custFieldArr = this.tagsFieldset.createFieldValuesArray();
        rec.posttext = this.postText;
        if (custFieldArr.length > 0) {
            rec.customfield = JSON.stringify(custFieldArr);
        }

        rec.shipdate = WtfGlobal.convertToGenericDate(this.shipDate.getValue());
        rec.doid = this.copyInv ? "" : this.billid;
        rec.mode = (this.isOrder ? 41 : 11);
        rec.incash = incash;
        rec.isfavourite = false;
        rec.statuscombo=this.DOStatusCombo.getValue();
        rec.posttext = this.postText;
        rec.isLinkedTransaction = this.isLinkedTransaction;
        rec.isEdit=this.isEdit;
        rec.copyInv=this.copyInv;
        var isCopy = this.copyInv;
        var isEdit = this.isEdit;
        rec.isFixedAsset=this.isFixedAsset;
        rec.isLeaseFixedAsset=this.isLeaseFixedAsset;
        rec.isConsignment=this.isConsignment;
        rec.custWarehouse= this.warehouses.getValue();
        rec.movementtype= this.movmentType.getValue();
        rec.posttext=this.postText;
        rec = WtfGlobal.getAddressRecordsForSave(rec, this.record, this.linkRecord, this.currentAddressDetailrec, this.isCustomer, this.singleLink, isEdit, isCopy, this.GENERATE_PO, this.GENERATE_SO, this.isQuotationFromPR);
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url: this.ajxurl,
            params: rec
        }, this, this.genSuccessResponse, this.genFailureResponse);
    },this);
},
     save:function(){
       var incash=false;
        this.Number.setValue(this.Number.getValue().trim());
        //this.billTo.setValue(this.billTo.getValue().trim());
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
                                this.enableSaveButtons();
                                return;
                            }
                        }
                    });
                    return;
            }
            // In Case of Fixed Asset Check external and internal quantities are equal or not
            if(this.isFixedAsset || this.isLeaseFixedAsset){
                for(var i=0;i<this.Grid.getStore().getCount()-1;i++){// exclude last row
                    var quantity=this.Grid.getStore().getAt(i).data['dquantity'];
                    var assetDetails = this.Grid.getStore().getAt(i).data['assetDetails'];
                    
                    var productId = this.Grid.getStore().getAt(i).data['productid'];
                    
                    var proRecord = WtfGlobal.searchRecord(this.Grid.productComboStore,productId,'productid');
                    if(proRecord.get('isAsset')){
                    
                        if(assetDetails == "" || assetDetails == undefined){
                            WtfComMsgBox(['Information','Please Provide Asset Details for Asset Group '+this.Grid.getStore().getAt(i).data['productname']],0);
                            return;
                        }

                        var assetDetailArray = eval('(' + assetDetails + ')');

                        if(assetDetailArray == null || assetDetailArray == undefined){
                            WtfComMsgBox(['Information','Please Provide Asset Details for Asset Group '+this.Grid.getStore().getAt(i).data['productname']],0);
                            return;
                        }

                        if(quantity != assetDetailArray.length){
                            WtfComMsgBox(['Information','Entered quantity does not match with the Asset Rows entered. Please give complete Asset Details for Asset Group '+this.Grid.getStore().getAt(i).data['productname']+'.'],0);
                            return;
                        }
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
            if(detail == undefined || detail == "[]"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
                return;
            }
            var validLineItem = this.Grid.checkDetails(this.Grid);
            if (validLineItem != "" && validLineItem != undefined) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), (WtfGlobal.getLocaleText("acc.msgbox.lineitem") + validLineItem)], 2);
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
                    if(prorec.data.type!='Service' && !prorec.get('isAsset') && prorec.data.type!='Non-Inventory Part'){
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
                    WtfComMsgBox(113, 2);
                    return false;
                } 
            }
                
        }
        if(this.isCustomer && !Wtf.account.companyAccountPref.isnegativestockforlocwar && (Wtf.account.companyAccountPref.isLocationCompulsory || Wtf.account.companyAccountPref.isWarehouseCompulsory)){
            Wtf.Ajax.requestEx({
                url: "ACCInvoice/getBatchRemainingQuantity.do",
                params: {
                    detail:detail,
                     transType:this.isCustomer?Wtf.Acc_Delivery_Order_ModuleId:this.moduleid,
                    isEdit:this.isEdit,
                    linkflag:(this.linkflag=="" ||this.prodname==undefined)?this.fromPO.getValue():this.linkflag,
                    fromSubmit:false,
                    isfromdo:true
                }
            },this,function(res,req){
                this.AvailableQuantity=res.quantity;   
                if(res.prodname){
                    this.prodname=res.prodname;
                }
                if(this.prodname=="" ||this.prodname==undefined){
                    this.Callfinalsavedetails(rec,detail,incash);
                    return;
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.quantityforprod")+" <b>"+this.prodname+"</b> " + WtfGlobal.getLocaleText("acc.field.isnotvalid")], 2);
                    this.enableSaveButtons();
                    return false;
                }
                
            },function(res,req){
                this.enableSaveButtons();
                return false;
            });
        }else{
            this.Callfinalsavedetails(rec,detail,incash);
        }
    }else{
        WtfComMsgBox(2, 2);
    }
},   
        Callfinalsavedetails:function(rec,detail,incash,confirmMsg,isDuplicate,duplicateval){
            if(rec.fromLinkCombo!=undefined  && (this.moduleid==27 || this.moduleid==51))  //in link case check available quantity should be greater than delivered quantity
            {
           var prodLength=this.Grid.getStore().data.length;
            for(var i=0;i<prodLength;i++)
            { 
               var prodID=this.Grid.getStore().getAt(i).data['productid'];
                var prorec=this.Grid.productComboStore.getAt(this.Grid.productComboStore.find('productid',prodID));
                 if(prorec==undefined){
                  prorec=this.Grid.getStore().getAt(i);
                }
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
                       WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.QuantitygiveninDoareexceedingthequantityavailable")+'<br>'+WtfGlobal.getLocaleText("acc.nee.54")+' '+prodName+WtfGlobal.getLocaleText("acc.field.is")+(availableQuantity-lockQuantity)+'<br><br><center>'+WtfGlobal.getLocaleText("acc.field.Soyoucannotproceed")+'</center>'], 2);
                        return true;
                    }else{
                        this.showConfirmAndSave(rec,detail,incash);   
                    }
               }
            }
        }else   //if DO is made noramal withaot linking
        {
            this.showConfirmAndSave(rec,detail,incash);
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
                rec.isConsignment=this.isConsignment;
                rec.custWarehouse= this.warehouses.getValue();
                rec.movementtype= this.movmentType.getValue();
                rec.isfavourite=false;
                rec.isEdit=this.isEdit;
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
                
                rec.ordernoreferedformemoaddress=this.LinkMemoAddress.getValue();
                if(this.LinkMemoAddress.getValue() != undefined && this.LinkMemoAddress.getValue() != "" && !this.isEdit){
                    var storeRec=this.POStore.getAt(this.POStore.find('billid',this.LinkMemoAddress.getValue()));
                    rec = WtfGlobal.getAddressRecordsForSave(rec, this.record, storeRec, this.currentAddressDetailrec, this.isCustomer, true, isEdit, isCopy);
                }else{
                    rec=WtfGlobal.getAddressRecordsForSave(rec,this.record,this.linkRecord,this.currentAddressDetailrec,this.isCustomer,this.singleLink,isEdit,isCopy);
                }
                Wtf.Ajax.requestEx({
                    url:this.ajxurl,
                    params: rec                    
                },this,this.genSuccessResponse,this.genFailureResponse);
                },this);
    },
    genSuccessResponse:function(response, request){
        WtfComMsgBox([this.titlel,response.msg],response.success*2+1);
         if(response.success){  
             Wtf.dupsrno.length=0;
             if(this.productOptimizedFlag==Wtf.Show_all_Products){
                Wtf.productStoreSales.reload();
                Wtf.productStore.reload(); 
            }  
            var rec=this.NorthForm.getForm().getValues();
            this.exportRecord=rec;
            this.exportRecord['billid']=response.billid||response.invoiceid;
            this.exportRecord['billno']=response.billno||response.invoiceNo;
            this.exportRecord['amount']=response.amount||"";
            this.exportRecord['isConsignment']=this.isConsignment;
            this.singlePrint.exportRecord=this.exportRecord;//Reload all product information to reflect new quantity, price etc    
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
                this.fireEvent('update',this);
                return;
            }
            this.currentAddressDetailrec="";//after saveandcreatenew this variable need to clear it old values. 
            this.singleLink = false;
            this.isVenOrCustSelect=false;
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
            this.Grid.updateRow(null);
            this.fromPO.setValue(false); 
            if(this.productOptimizedFlag==Wtf.Show_all_Products){
                this.Grid.priceStore.purgeListeners();
                this.Grid.loadPriceStoreOnly(new Date(),this.Grid.priceStore);
            }
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
            var rec = "";
            if (response.billid != undefined || response.billid != '') {
                rec = this.CustomStore.getAt(this.CustomStore.find('billid', response.billid));
            }
            var isConsignment=rec.data.isConsignment;
            var label = "";
            if(this.isCustomer){
                label = isConsignment?WtfGlobal.getLocaleText("acc.Consignment.DO"):WtfGlobal.getLocaleText("acc.accPref.autoDO");
                isConsignment?callEmailWin("emailwin",rec,label,53,true,false,false,false,false,true,false,false,false,true):callEmailWin("emailwin",rec,label,53,true,false,false,false,false,true,false,false,false,false);
            }else{
                 label = isConsignment?WtfGlobal.getLocaleText("acc.Consignment.GR"):WtfGlobal.getLocaleText("acc.accPref.autoGRO");
                 isConsignment?callEmailWin("emailwin",rec,label,54,false,false,false,false,false,true,false,false,false,true):callEmailWin("emailwin",rec,label,54,false,false,false,false,false,true,false,false,false,false);
             }
        }
},

disableComponent: function(){  // disable following component in case of save button press.
    
    if(this.fromLinkCombo && this.fromLinkCombo.getValue() === ''){
        this.fromLinkCombo.emptyText = "";
        this.fromLinkCombo.clearValue();
    }
    
    if(this.PO && this.PO.getValue() === ''){
        this.handleEmptyText=true;
        this.PO.emptyText = "";
        this.PO.clearValue();
    }
    
    if(Wtf.getCmp("save" + this.heplmodeid + this.id)){
        Wtf.getCmp("save" + this.heplmodeid + this.id).disable();
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
        {name:'billingEmail'},
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
        {name:'isConsignment'}
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
                isConsignment:this.isConsignment
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
    checkForDeActivatedProductsAdded:function(){
    var invalidProducts='';
    if(this.isEdit && !this.copyInv){ // Edit case
        var linkedDocuments = this.PO.getValue();
        if(linkedDocuments != ''){
                if(this.originallyLinkedDocuments.indexOf(linkedDocuments) == -1){
                    invalidProducts = this.checkDeactivatedProductsInGrid();
                }
        }
    } else { // Create New and copy case
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
