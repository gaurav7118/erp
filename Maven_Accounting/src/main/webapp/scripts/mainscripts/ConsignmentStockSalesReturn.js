
Wtf.account.ConsignmentStockSalesReturnPanel=function(config){	
    this.modeName = config.modeName;
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
    this.isLeaseFixedAsset=config.isLeaseFixedAsset?config.isLeaseFixedAsset:false;
    this.isConsignment = (config.isConsignment)?config.isConsignment:false;
    this.isMovementWarehouseMapping=Wtf.account.companyAccountPref.isMovementWarehouseMapping;
    this.isLinkedTransaction = (config.isLinkedTransaction == null || config.isLinkedTransaction == undefined)? false : config.isLinkedTransaction;
    if( this.isConsignment){
        this.uPermType=config.isCustomer?Wtf.UPerm.consignmentsales:Wtf.UPerm.consignmentpurchase;
        this.permType= config.isCustomer?Wtf.Perm.consignmentsales:Wtf.Perm.consignmentpurchase;   
        this.exportPermType=config.isCustomer?this.permType.exportsalesconret:this.permType.exportpurchaseconret;
    //this.printPermType=config.isCustomer?this.permType.printsalesconret:this.permType.printpurchaseconret;
    }
    
    var tranType=null;//Required for ExportRecord & Print Button
    if(config.moduleid==Wtf.Acc_ConsignmentSalesReturn_ModuleId || config.moduleid==Wtf.Acc_ConsignmentPurchaseReturn_ModuleId){
        if(config.moduleid==Wtf.Acc_ConsignmentSalesReturn_ModuleId){
            tranType=Wtf.autoNum.SalesReturn;
        }else{
            tranType=Wtf.autoNum.PurchaseReturn;
        }   
    }
    
    this.record=config.record;
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    this.custVenOptimizedFlag = Wtf.account.companyAccountPref.custvenloadtype;
    this.readOnly=config.readOnly;
    this.originallyLinkedDocuments = '';
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
    
    this.singleRowPrint = new Wtf.exportButton({
        obj: this,
        id: "printSingleRecord"+ config.id,
        iconCls: 'pwnd printButtonIcon',
        text: WtfGlobal.getLocaleText("acc.rem.236"),
        tooltip: WtfGlobal.getLocaleText("acc.rem.236.single"), //'Print Single Record Details',
        disabled: this.readOnly?false:true,
        isEntrylevel: false,
        exportRecord:this.exportRecord,
        menuItem: {
            rowPrint: true
        },
        get:tranType,
        moduleid:Wtf.Acc_ConsignmentSalesReturn_ModuleId,
        hidden:config.moduleid==Wtf.Acc_ConsignmentSalesReturn_ModuleId?false:true
    });
    
    if(this.custVenOptimizedFlag==0){
//        this.customerAccStore.load();
        this.isCustomer?Wtf.customerAccStore.load():Wtf.vendorAccStore.load();
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
                if(this.isLinkedTransaction && this.businessPerson == "Customer") {
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
            disabled:(this.contractStatus==3), 
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
            disabled : true,
            hidden:this.readOnly,
            handler: function(){
                this.callEmailWindowFunction(this.response, this.request)
            },
            iconCls: "accountingbase financialreport"
        },!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)?{
            text: WtfGlobal.getLocaleText("acc.field.ExportPDF"),
            tooltip : WtfGlobal.getLocaleText("acc.field.ExportPDFFile"),
            scope:this,
            id:"exportpdf" + this.id,
            iconCls: 'pwnd exportpdf1',
            disabled : true,
            hidden:this.readOnly,
            handler: function(){
                this.exportPdfFunction()
            }
        }:"",this.singleRowPrint,{
            text:  WtfGlobal.getLocaleText("acc.template.posttext") , //'<b>Post Text</b>',
            cls: 'pwnd add',
            disabled:(this.contractStatus==3), 
            id: "posttext" + this.id,              // Post Text
            hidden:this.readOnly,      
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
            hidden:(this.contractStatus==2 ||this.contractStatus==4 || this.isConsignment) ||this.readOnly,        
            tooltip : WtfGlobal.getLocaleText("acc.common.closebtn.Tooltip"),        
            style:" padding-left: 15px;",
            scope: this,
            handler: this.closeContract
        },'->']
    });
    Wtf.account.ConsignmentStockSalesReturnPanel.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.ConsignmentStockSalesReturnPanel,Wtf.account.ClosablePanel,{
    autoScroll: true,// layout:'border',//Bug Fixed: 14871[SK]
    bodyStyle: {
        background:"#DFE8F6 none repeat scroll 0 0"
    },
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
            if(this.custVenOptimizedFlag==0){
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
                                 *ERM-1037
                                 *Adding date of linked document in rec to compare for restricting linking of future dated document.
                                 */
                                date: rec.data.invcreationdate
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
                }
                if(linkType!=-1){
                    this.fromLinkCombo.setValue(linkType);
                }
            }
            if(this.isConsignment) {    //in edit case assign warehouse
                this.wareHouseStore.on("load", function(){
                    this.warehouses.setValue(data.custWarehouse);
                    this.Grid.warehouseselcted=true;
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
            this.Memo.setValue(data.memo);                        
            this.billDate.setValue(data.date);                                                            
            this.CostCenter.setValue(data.costcenterid);
            this.postText = this.record.json.posttext;
            
            this.dataLoaded=true;
            if(!this.isEdit){
            this.Grid.priceStore.load({
                params:{
                    transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue())
                }
            });
            }
            //this.bankTypeStore.load();            
            
            if(this.copyInv){
                this.billDate.setValue(Wtf.serverDate);
            //            	this.updateDueDate();
            }
        }
    },
    onRender:function(config){              
        this.add(this.NorthForm,this.Grid,this.southPanel); 
        
        Wtf.account.ConsignmentStockSalesReturnPanel.superclass.onRender.call(this, config);
        this.initForClose();
        // hide form fields
        this.hideFormFields();
    },
    hideFormFields:function(){
        if(this.isCustomer){
            this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.salesReturn);
        }else{
            this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.purchaseReturn);
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
        if(this.isEdit || this.copyInv || this.readOnly){
            Wtf.customerAccStore.on("load",function(){
                this.Name.setValue(this.record.data.personid)
            },this);
        }
        Wtf.account.ConsignmentStockSalesReturnPanel.superclass.initComponent.call(this,config);
        
        //chkcustaccload();// Global Customer store  
        this.isCustomer ? chkproductSalesload() : chkproductload() ; // Global Product store for product sales
        
        
        this.loadCurrFlag = true;
        
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
//            '<table width="100%">',
//            '<tr><td><b>+ '+WtfGlobal.getLocaleText("acc.invoice.Tax")+': </b></td><td align=right>{tax}</td></tr>',
//            '</table>',
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
//            '<table width="100%">',
//            '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.inv.amountdue")+' </b></td><td align=right>{amountdue}</td></tr>',
//            '</table>',
            '<hr class="templineview">',
            '<hr class="templineview">',
            '</div>'
            );
            
        this.productDetailsTpl=new Wtf.Panel({
            border:false,
            baseCls:'tempbackgroundview',
            style:'float:left',
            width:'90%',            
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
            html:this.tplSummary.apply({
                subtotal:WtfGlobal.currencyRenderer(0),
//                tax:WtfGlobal.currencyRenderer(0),
                aftertaxamt:WtfGlobal.currencyRenderer(0),
                totalAmtInBase:WtfGlobal.currencyRenderer(0)
//                amountdue:WtfGlobal.currencyRenderer(0)
            })
        });
        
        this.GridRec = Wtf.data.Record.create ([
        {
            name:'id'
        },

        {
            name:'number'
        }
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
        this.sequenceFormatStore.on('load',function(){
            if(this.sequenceFormatStore.getCount()>0){
//                var seqRec=this.sequenceFormatStore.getAt(0)
//                this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid);
                if(this.isEdit || this.copyInv){
                    var sequenceformatid=(this.record && this.record.data && this.record.data.sequenceformatid)?this.record.data.sequenceformatid:"";
                    if(sequenceformatid==undefined || sequenceformatid=="" || sequenceformatid=='NA'){
                        this.sequenceFormatCombobox.setValue("NA"); 
                        this.sequenceFormatCombobox.disable();
                        if(this.readOnly!=undefined && !this.readOnly){
                            this.Number.enable();
                        }
                        if(this.copyInv){//for copy NA enable disable number field
                            this.getNextSequenceNumber(this.sequenceFormatCombobox);
                        }
                    }else{
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
        },this);
        this.sequenceFormatStore.load();
        
        this.Name= new Wtf.form.ExtFnComboBox({
            fieldLabel:this.isCustomer?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven") , //this.businessPerson+"*",
            hiddenName:this.businessPerson.toLowerCase(),
            id:"customer"+this.heplmodeid+this.id,
//            store: this.customerAccStore,
            store: this.isCustomer?Wtf.customerAccStore:Wtf.vendorAccStore,
            valueField:'accid',
            displayField:'accname',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
            allowBlank:false,
            hirarchical:true,
            hideTrigger:this.custVenOptimizedFlag==0?false:true,
            emptyText:this.isCustomer?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven") , //'Select a '+this.businessPerson+'...',
            mode: this.custVenOptimizedFlag==0?'local':'remote',
            typeAheadDelay:30000,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            minChars:1,
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            isVendor:!(this.isCustomer),
            isCustomer:this.isCustomer,
            //            anchor:"50%",
            width : 240,
            triggerAction:'all',
            listeners:{
                'select':{
                    fn:function(){
                        this.fromLinkCombo.clearValue();
                        this.PO.clearValue();
                        if(!this.isEdit && !this.copyInv){  //in edit and copy case do not remove record from store
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
                        this.warehouses.clearValue();
                        this.updateData();
                        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body,{productname:"&nbsp;&nbsp;&nbsp;&nbsp;",qty:0,soqty:0,poqty:0});
                        if(this.fromPO && !this.isCustomer)
                            this.fromPO.enable(); 
                        if(this.isCustomer)
                        {
                            this.wareHouseStore.load({
                                params:{
                                    customerid:this.Name.getValue()
                                }
                            })
                        };
//                        if(!this.isCustomer){
//                            this.populateAllConsignData();
//                        }
                        var customer = this.Name.getValue();
                        this.tagsFieldset.resetCustomComponents();
                        var moduleid = this.isCustomer ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId;
                        this.tagsFieldset.setValuesForCustomer(moduleid, customer);
                    },
                    scope:this          
                }
            }
        });
        
        this.Name.on('beforeselect', function(combo, record, index) {
                return validateSelection(combo, record, index);
        }, this);
        if(this.isSalesFromDo){
            var store1 = this.isCustomer? Wtf.customerAccStore : Wtf.vendorAccStore;
            store1.on("load",function(){
                this.Name.setValue(this.dopersonid)
                this.fromPO.setValue(true);
                this.fromLinkCombo.enable();                        
                this.fromOrder=true;
                this.fromLinkCombo.setValue(0);
                this.enableNumber(undefined,this.fromlinkStore.getAt(0));
                this.POStore.on("load",function(){
               
                    var index=this.POStore.find("billid",this.dolinkid);
               
                    if(index==-1){
                        var custindex=store1.find("accid",this.dopersonid);
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
            fields:[{
                name:'name'
            },{
                name:'value',
                type:'boolean'
            }],
            data:[['Yes',true],['No',false]]
        });
        
        
        var fromLinkStoreRec = new Array();
        fromLinkStoreRec.push([this.isCustomer?'Consignment Delivery Order':"Consignment Goods Receipt", 0]);
        //        fromLinkStoreRec.push([this.isCustomer?'Customer Invoice':"Vendor Invoice", 1]);
        
        
        
        
        this.fromlinkStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'name'
            },{
                name:'value'
            }],
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
            disabled:(this.isEdit&&!this.copyInv?true:false),
          //  anchor:'90%',
            width : 240,
            lastQuery:'',
            typeAhead: true,
            forceSelection: true,
            name:'movemettype',
            hiddenName:'movemettype'
//                        listeners:{
//                'select':{
//                    fn:function(){
//                        this.PO.clearValue();
//                        this.Grid.fromPO=false;
//                        if(this.isConsignment){
//                            this.enableNumber(this.fromLinkCombo,this.fromLinkCombo.store.getAt(0));
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
        {
            name: 'id'
        },

        {
            name: 'name'
        },

        {
            name: 'customer'
        },

        {
            name: 'company'
        },
        {
            name: 'warehouse'
        },

        {
            name: 'isdefault'
        }
        ]);
        this.wareHouseStore = new Wtf.data.Store({  //  warehouse store for combo box
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.warehouseRec),
//            url:"ACCCustomerCMN/getCustomerWarehouses.do",
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
            width : 240,
            //            typeAhead: true,
            forceSelection: true,
            fieldLabel: this.isCustomer ? WtfGlobal.getLocaleText("acc.Consignment.Warehouses"):"",
            emptyText:'Select Warehouses...',
            name:'custWarehouse',
            hiddenName:'custWarehouse',
            listeners:{
                'select':{
                    fn:function(){
                        this.fromLinkCombo.clearValue();
                       // this.movmentType.clearValue();
                        this.PO.clearValue();
                        if(!this.isEdit && !this.copyInv){  //in edit and copy case do not remove record from store
                            this.Grid.getStore().removeAll();
                            this.Grid.addBlankRow();
                        }
                        this.Grid.warehouseselcted=true;
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
            listeners:{
                'select':{
                    fn:this.getNextSequenceNumber,
                    scope:this
                }
            }
            
        });
        this.currencyRec = new Wtf.data.Record.create([
        {
            name: 'currencyid',
            mapping:'tocurrencyid'
        },

        {
            name: 'symbol'
        },

        {
            name: 'currencyname',
            mapping:'tocurrency'
        },

        {
            name: 'exchangerate'
        },

        {
            name: 'htmlcode'
        }
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
            fieldLabel:this.label + " " + WtfGlobal.getLocaleText("acc.common.number"),  //,  //this.label+' Number*',
            name: 'number',
            disabled:(this.isEdit&&!this.copyInv?true:false),
            id:"invoiceNo"+this.heplmodeid+this.id,
            //            anchor:'50%',
            width : 240,
            maxLength:50,
            scope:this,
            allowBlank:this.checkin
        });
        this.Memo=new Wtf.form.TextArea({
            fieldLabel:Wtf.account.companyAccountPref.descriptionType,  //'Memo',
            name: 'memo',
            id:"memo"+this.heplmodeid+this.id,
            height:40,
//            anchor:'94%',
            width : 240,
            maxLength:2048
        });
        
        this.PORec = Wtf.data.Record.create ([
        {
            name:'billid'
        },

        {
            name:'journalentryid'
        },

        {
            name:'entryno'
        },

        {
            name:'billto'
        },

        {
            name:'discount'
        },

        {
            name:'shipto'
        },

        {
            name:'mode'
        },

        {
            name:'billno'
        },

        {
            name:'date', 
            type:'date'
        },

        {
            name:'duedate', 
            type:'date'
        },

        {
            name:'shipdate', 
            type:'date'
        },

        {
            name:'personname'
        },

        {
            name:'creditoraccount'
        },

        {
            name:'personid'
        },

        {
            name:'shipping'
        },

        {
            name:'othercharges'
        },

        {
            name:'taxid'
        },

        {
            name:'currencyid'
        },

        {
            name:'amount'
        },
        {
            name:'amountinbase'
        },

        {
            name:'shipvia'
        },

        {
            name:'fob'
        },

        {
            name:'amountdue'
        },

        {
            name:'costcenterid'
        },

        {
            name:'costcenterName'
        },

        {
            name:'memo'
        },

        {
            name:'posttext'
        },
         {name:'movementtype'}
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
            store:this.fromPOStore,
            disabled:true,
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
//            hideLabel:(this.isConsignment)?true:false,///
//            hidden:(this.isConsignment)?true:false,//////gfbfg''
            mode: 'local',
            valueField:'value',
            displayField:'name',
            disabled:true,
            id:'fromLinkComboId'+this.heplmodeid+this.id,
            store:this.fromlinkStore,                        
            emptyText: this.isCustomer?WtfGlobal.getLocaleText("acc.field.SelectaCDO"):WtfGlobal.getLocaleText("acc.field.SelectaCGR"),
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
        
        this.PO= new Wtf.form.ExtFnComboBox({
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
            extraFields:[],
            disabled:this.isEdit?false:true,
            emptyText:this.isCustomer?WtfGlobal.getLocaleText("acc.field.SelectaCDO"):WtfGlobal.getLocaleText("acc.field.SelectaCGR"),
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
        if (Wtf.account.companyAccountPref.enableLinkToSelWin && (this.moduleid == Wtf.Acc_ConsignmentSalesReturn_ModuleId || this.moduleid==Wtf.Acc_ConsignmentPurchaseReturn_ModuleId)) {
            this.POStore.on('load', function(){addMoreOptions(this.PO,this.PORec)}, this);            
            this.POStore.on('datachanged', function(){addMoreOptions(this.PO,this.PORec)}, this);            
        }
        //            this.PO.addNewFn=this.addOrder.createDelegate(this,[false,null,this.businessPerson+"PO"],true)
            
        this.POSelected="";
        
        this.billDate= new Wtf.form.DateField({
            fieldLabel:this.label +' '+WtfGlobal.getLocaleText("acc.invoice.date"),
            id:"invoiceDate"+this.heplmodeid+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'billdate',
//            anchor:'50%',
            width : 240,
            allowBlank:false
        });
        this.shipDate= new Wtf.form.DateField({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.ShipDate.tip") +"'>"+ WtfGlobal.getLocaleText("acc.field.ShipDate")+"</span>",//WtfGlobal.getLocaleText("acc.field.ShipDate"),
            id:"shipdate"+this.heplmodeid+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'shipdate',
//            anchor:'94%'
            width : 240
        });
        this.shipvia = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.ShipVia"),
            id:"shipvia"+this.heplmodeid+this.id,
            name: 'shipvia',
//            anchor:'94%',
            width : 240,
            maxLength: 255,
            scope: this
        });
        
        this.fob = new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.field.fob.tip")+"'>"+WtfGlobal.getLocaleText("acc.field.FOB") +"</span>",
            name: 'fob',
            id:"fob"+this.heplmodeid+this.id,
//            anchor:'94%',
            width : 240,
            maxLength: 255,
            scope: this
        });        
        chkLineLevelCostCenterload();           //        chkFormCostCenterload();
        this.CostCenter= new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.costCenter.tip") +"'>"+ WtfGlobal.getLocaleText("acc.common.costCenter")+"</span>",//WtfGlobal.getLocaleText("acc.common.costCenter"),//"Cost Center",
            hiddenName:"costcenter",
            //            id:"costcenter"+this.heplmodeid+this.id,
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
//            anchor:"50%",
            width : 240,
            triggerAction:'all',
            addNewFn:this.addCostCenter,
            scope:this
        }); 
         
//        this.CostCenter.listWidth=300;
        var itemArr={};
        itemArr = [this.Name, this.Currency,this.warehouses,{
            layout:'column',
            border:false,
            defaults:{
                border:false
            },
            items:[ {
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
            ]
        },this.PO,this.movmentType,this.sequenceFormatCombobox,this.Number,this.billDate, this.CostCenter];
   
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
            //            height:300,
            autoHeight:true,
            id:"northForm"+this.id,
            disabledClass:"newtripcmbss",
            border:false,
            disabled:this.readOnly,
            items:[{
                layout:'form',
                baseCls:'northFormFormat',
                labelWidth:155,
                cls:"visibleDisabled",
                items:[{
                    layout:'column',
                    border:false,
                    defaults:{
                        border:false
                    },
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
        
        // this.POStore.on('load',this.updateSubtotal,this)
        this.billDate.on('change',this.onDateChange,this);
        
        this.setTransactionNumber();        
        if(this.isEdit) {
            //            this.loadRecord();
            this.loadEditableGrid();
        }
           
    },
    /*
    loading the respective currencies of the customer
     */
    updateData:function(){
        var customer= this.Name.getValue();
        Wtf.Ajax.requestEx({
            url:"ACC"+this.businessPerson+"CMN/getCurrencyInfo.do",
            params:{
                customerid:customer
            }
        }, this,this.setCurrencyInfo);       
        if(this.fromPO){
            this.fromPO.enable();
        }
    },
    setCurrencyInfo:function(response){
        if(response.success){
            this.Currency.setValue(response.currencyid);
            this.currencyid=response.currencyid;
            this.symbol = response.currencysymbol;
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
            this.ContractStore.reload();
        }, this);
    },
    genFailureResponseClosed : function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")],2);
    },
    onDateChange:function(a,val,oldval){
        /*
         *ERM-1037
         *On date change call function to compare date to restrict linking of future dated document
         */
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
        this.Grid.loadPriceStoreOnly(val,this.Grid.priceStore);
        
    },
    hideLoading:function(){
        Wtf.MessageBox.hide();
    },
    
    addCostCenter:function(){
        callCostCenter('addCostCenterWin');
    },

    addGrid:function(){
        this.Grid=new Wtf.account.ConsignmentStockSalesReturnGrid({
            height: 300,//region:'center',//Bug Fixed: 14871[SK]
            cls:'gridFormat',
            layout:'fit',
            viewConfig:{
                forceFit:false
                
            },
            autoScroll:true,
            isCustomer:this.isCustomer,
            editTransaction:this.isEdit,
            disabledClass:"newtripcmbss",
            isCustBill:false,
            id:this.id+"billingproductdetailsgrid",
            parentCmpID:this.id,
            moduleid:this.moduleid,
            currencyid:this.Currency.getValue(),
            isLeaseFixedAsset:this.isLeaseFixedAsset,
            warehouseid:"warehouse"+this.heplmodeid+this.id,
            isConsignment:this.isConsignment,
            isLinkedTransaction:this.isLinkedTransaction,
            fromOrder:true,
            isOrder:this.isOrder,
            isEdit:this.isEdit,
            copyTrans:this.copyInv, 
            readOnly:this.readOnly,
            parentObj:this,
            forceFit:true,
            heplmodeid:this.heplmodeid,
            parentid:this.id,
            loadMask : true
        });
        // this.Name.on('select',this.setTerm,this)
        this.NorthForm.on('render',this.setDate,this);        
        this.Grid.getStore().on('load',function(store){            
            this.Grid.addBlank(store);
            this.updateFormCurrency();
        }.createDelegate(this),this);
        this.Grid.on("datachanged", this.applyCurrencySymbol,this);
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
        var tabid = "deliveryorder";
        callDeliveryOrder(false,null, tabid);
        if(Wtf.getCmp(tabid)!=undefined) {
            Wtf.getCmp(tabid).on('update',function(){
                this.POStore.reload();
            },this);
        }
    },

    enablePO:function(c,rec){
        this.fromLinkCombo.clearValue();
        this.PO.clearValue();
        this.CostCenter.clearValue();
      //  this.movmentType.clearValue();
        
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
    //    this.movmentType.clearValue();
        //        this.Name.clearValue();
        this.Memo.setValue("");
        this.Grid.getStore().removeAll();            
        this.Grid.addBlankRow();
        this.updateFormCurrency();
        
        if(rec.data['value']==0){                        
            this.POStore.proxy.conn.url = (this.isCustomer)?"ACCInvoiceCMN/getDeliveryOrdersMerged.do":"ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";
            this.POStore.load({
                params:{
                    id:this.Name.getValue(),
                    isLeaseFixedAsset:this.isLeaseFixedAsset,
                    isConsignment:this.isConsignment, 
                    currencyfilterfortrans:this.Currency.getValue(),
                    custWarehouse:this.warehouses.getValue(),
                    closedStatusflag:Wtf.account.companyAccountPref.closedStatusforDo,
                    linkFlag:true
                }
            });        
            this.PO.enable(); 
            this.POSelected=(this.isCustomer)?"sales":"purchase";
        }else if(rec.data['value']==1){
            this.POStore.proxy.conn.url = this.isCustomer ? "ACCInvoiceCMN/getInvoices.do" : "ACCGoodsReceiptCMN/getGoodsReceipts.do";
            var params={
                cashonly:false,
                creditonly:true, 
                salesPurchaseReturnflag:true, 
                doflag:true, 
                currencyfilterfortrans:this.Currency.getValue()
            };
            if(this.isCustomer) {                        
                params.customerid=this.Name.getValue();                    
            }else{
                params.vendorid=this.Name.getValue();                    
            }
            this.salesPurchaseReturnflag = true,
            this.POStore.load({
                params:params
            });        
            this.PO.enable();       
            this.POSelected="invoice";
        }
    },
    
    loadEditableGrid:function(){
       
        this.subGridStoreUrl =  (this.isCustomer)?"ACCInvoiceCMN/getSalesReturnRows.do":"ACCGoodsReceiptCMN/getPurchaseReturnRows.do";
        //            	            
        this.billid=this.record.data.billid;
        this.isConsignment=this.record.data.isConsignment;
        this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;
        this.Grid.getStore().on("load", function(){
            this.loadRecord();
        }, this);
        if(this.custVenOptimizedFlag==1){
            this.Name.setValForRemoteStore(this.record.data.personid, this.record.data.personname);
        }
        
        this.Grid.getStore().load({
            params:{
                bills:this.billid,
                isConsignment:this.isConsignment,
                moduleid:this.moduleid,
                isEdit:this.isEdit
            }
        });
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
            PORec: this.PORec,
            singleSelect:true
        });
        this.PONumberSelectionWin.show();
    },
    populateData:function(c,rec) {
        this.Grid.fromPO=true;
        var billid=this.PO.getValue();
        if (billid.indexOf("-1") != -1) {
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
        var record = this.POStore.getAt(this.POStore.find('billid', billid));
        WtfGlobal.resetCustomFields(this.tagsFieldset);
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
                                if(checkListCheckBoxesArray[checkitemcnt].id.indexOf(ComboValueArrya[i]) != -1 )
                                    if (Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id) != undefined) {
                                        Wtf.getCmp(checkListCheckBoxesArray[checkitemcnt].id).setValue(true);
                                    }
                            } 
                        }
                    }else if(Wtf.getCmp(fieldname+this.tagsFieldset.id).getXType()=='select'){
                        var ComboValue=record.json[fieldN.name];
                                    if(ComboValue!="" && ComboValue!=undefined)
                                    Wtf.getCmp(fieldname+this.tagsFieldset.id).setValue(ComboValue);
                    }
                
                }
            }
        }
        rec = record;
        if (rec != undefined) {
            /*
             *ERM-1037
             *If rec is not undefined send id of selected linked document to restrict linking of future dated document
             */
            var isFutureDatedDocumentLinked = WtfGlobal.checkForFutureDate(this, billid.split(","));
            if (isFutureDatedDocumentLinked) {
                return;
            }
        }
        this.Memo.setValue(rec.data['memo']);
        this.postText=rec.data['posttext'];
        this.Name.setValue(rec.data['personid']); 
        this.shipDate.setValue(rec.data['shipdate']);
        this.shipvia.setValue(rec.data['shipvia']);
        this.fob.setValue(rec.data['fob']);
        this.Currency.setValue(rec.data['currencyid']);
//        var perstore=this.customerAccStore;
        var perstore=this.isCustomer?Wtf.customerAccStore:Wtf.vendorAccStore;
        var storerec=perstore.getAt(perstore.find('accid',rec.data['personid']));        
        this.CostCenter.setValue(rec.data.costcenterid);
        this.movmentType.setValue(rec.data.movementtype);
        //this.updateDueDate();
        
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
    populateAllData:function(){
        this.Grid.warehouseselcted=true;
        if(this.isConsignment){
            this.Grid.getStore().proxy.conn.url = this.isCustomer ? "ACCInvoiceCMN/getAllUninvoicedConsignmentDetails.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";
            this.Grid.getStore().load({
                params:{
                    customerid:this.Name.getValue(),
                    custWarehouse:this.warehouses.getValue(),
                    closeflag:true,
                    isConsignment:this.isConsignment
                }
            });
        //    this.Grid.loadAllGridStore("");          
        }
    },
    populateAllConsignData:function(){
        if(!this.isEdit && !this.copyInv){  //in edit and copy case do not remove record from store
            this.Grid.getStore().removeAll();
        }
        this.Grid.addBlankRow();
        if(this.warehouses.getValue()!="" && this.warehouses.getValue()!=undefined){
            this.Grid.warehouseselcted=true;
        }else{
            this.Grid.warehouseselcted=false;
        }
        if(this.isConsignment){
            this.Grid.getStore().proxy.conn.url = "ACCGoodsReceiptCMN/getAllUninvoicedConsignmentDetails.do"; 
            this.Grid.getStore().load({
                params:{
                    vendorid:this.Name.getValue(),
                    closeflag:true,
                    isConsignment:this.isConsignment
                }
            });

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
            //this.billDate.setValue(Wtf.serverDate);//(new Date());    
            this.billDate.setValue(new Date());
        }
    },        
    addPerson:function(isEdit,rec,winid,isCustomer){
        callBusinessContactWindow(isEdit, rec, winid, isCustomer);
        var tabid=isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
        Wtf.getCmp(tabid).on('update', function(){
//            this.customerAccStore.reload();
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
        this.tplSummary.overwrite(this.southCalTemp.body,{
            subtotal:WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),
//            tax:WtfGlobal.addCurrencySymbolOnly(0,this.symbol),
            aftertaxamt:(this.moduleid == 28 || this.moduleid ==27) ? WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol):WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),
            totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol())
//            amountdue:WtfGlobal.addCurrencySymbolOnly(this.amountdue,WtfGlobal.getCurrencySymbol())
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
            var format= Wtf.account.companyAccountPref.autosr ;
            var temp2=(this.isCustomer)? Wtf.autoNum.SalesReturn:Wtf.autoNum.PurchaseReturn ;
        }
        if(isSelectNoFromCombo){
            this.fromnumber = temp2;
        } else if(format&&format.length>0){
            WtfGlobal.fetchAutoNumber(temp2, function(resp){
                if(this.isEdit)this.Number.setValue(resp.data)
            }, this);
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
                comp.on('change', function(){
                    this.isClosable=false;
                },this);
            }
        },this);
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
            this.ajxurl = "ACCSalesReturnCMN/updateSalesReturn.do";
        } 
//        else {
//            this.ajxurl = "ACCSalesReturnCMN/savePurchaseReturn.do";
//        }
        var detail = this.Grid.getProductDetails();
        rec.detail = detail;
        rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
        rec.shipdate=WtfGlobal.convertToGenericDate(this.shipDate.getValue());
        rec.srid=this.copyInv?"":this.billid; 
        var custFieldArr = this.tagsFieldset.createFieldValuesArray();
        rec.posttext = this.postText;
        if (custFieldArr.length > 0) {
            rec.customfield = JSON.stringify(custFieldArr);
        }
        
        rec.incash = incash;
        rec.isEdit = true;
        rec.mode=(this.isOrder?41:11);
        rec.posttext=this.postText;
        rec.isfavourite=false;
        rec.isLeaseFixedAsset=this.isLeaseFixedAsset;
        rec.transType=this.moduleid;
        rec.isConsignment=this.isConsignment;
        rec.custWarehouse= this.warehouses.getValue();
        rec.movementtype= this.movmentType.getValue();
        if(!this.copyInv){
            if((this.record && this.record !== undefined) && (this.record.get('isfavourite') !== null || this.record.get('isfavourite') !== undefined)){
                rec.isfavourite = this.record.get('isfavourite');
            }
        }
        rec.currencyid=this.Currency.getValue();
        rec.linkNumber=this.PO.getValue(); 
        rec.fromLinkCombo=this.fromLinkCombo.lastSelectionText;

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
            incash=this.cash;
            var rec=this.NorthForm.getForm().getValues();
            
            this.ajxurl = this.isCustomer?"ACCSalesReturnCMN/saveSalesReturn.do":"ACCSalesReturnCMN/savePurchaseReturn.do";            
            
		
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
            this.showConfirmAndSave(rec,detail,incash);
        }else{
            WtfComMsgBox(2, 2);
        }
    },   
      
    reusableCount: function(rec,detail,incash){
        this.store=this.Grid.getStore();
        
        this.resusableStoreRec = Wtf.data.Record.create([
            {
                name:'productid'
            },
            {
                name:'productname'
            },
            {
                name:'totalissuecount'
            }
        ]);
    
        this.resusableStore = new Wtf.data.SimpleStore({
            fields : [{name:'productid'}, {name:'productname'},{name:'totalissuecount'}],
            data : []
        });
       var count=0;
        this.store.each(function(rec){
            if(rec.data.productid!="" && rec.data.isreusable==0){
                count++;
                var newrec = new this.resusableStoreRec({
                productid:rec.data.productid,
                productname:rec.data.productname,
                totalissuecount:this.isEdit?rec.data.totalissuecount:0
            });
            this.resusableStore.add(newrec);
            }
        }, this);
        if(count<=0){
            this.saveWithCount(rec,detail,incash);
            return;
        }
        var cm = new Wtf.grid.ColumnModel([
            this.rowNo=new Wtf.grid.RowNumberer(),
            {
                header: "Product Name",
                dataIndex: "productname"
            },
            {
                header: "Reusable Count",
                dataIndex: 'totalissuecount',
                minValue:0,
                allowNegative:false,
                editor : new Wtf.form.NumberField({
                    allowDecimals:false,
                    allowNegative:false
                })
            }
        ]);
        
        this.reusableGrid = new Wtf.grid.EditorGridPanel({
            clicksToEdit:1,
            autoScroll:true,
            autoWidth:true,
            store: this.resusableStore,
            cm:cm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:'No Record to display'
            }
        });  

        this.reusablePanel=new Wtf.Panel({
            border: false,
            region: 'center',
            autoScroll:true,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:[this.reusableGrid]
        })

        this.reusableWin = new Wtf.Window({
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            title: 'Set Reusable Count',
            buttonAlign: 'right',
            width: 600,
            height:400,
            layout:'fit',
            scope: this,
            items:this.reusablePanel,
            buttons: [{
                text: 'Save',
                scope: this,
                handler: function(){
                    this.saveReusableDetails(rec,detail,incash);
                    this.reusableWin.close();
                }   
            },{
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope: this,
                handler: function() {
                    this.reusableWin.close();
                }
            }]
        });
        this.reusableWin.show();
    },   

    saveReusableDetails: function(rec,detail,incash){
        var store=this.store;
        
        this.resusableStore.each(function(resusableRec){
            var index=store.findBy(function(innerrec){
                if(resusableRec.data.productid==innerrec.data.productid)
                    return true;
                else
                    return false;
            });
            if(index!=-1){
                var prorec=store.getAt(index);
                if(prorec){
                    prorec.set('totalissuecount', (resusableRec.data.totalissuecount!="" ? resusableRec.data.totalissuecount : 0));
                }
            }
        });
        var prorecDetails=this.Grid.getProductDetails();
        this.reusableWin.close();
        this.saveWithCount(rec,prorecDetails,incash);
    },
    
   saveWithCount :function(rec,detail,incash){
                rec.detail=detail;
                this.msg= WtfComMsgBox(27,4,true);
                rec.currencyid=this.Currency.getValue();
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
                rec.transType=this.moduleid;
                rec.isfavourite=false;
                rec.isEdit=this.isEdit;
                rec.isLeaseFixedAsset=this.isLeaseFixedAsset;
                rec.isConsignment=this.isConsignment;
                rec.linkNumber=this.PO.getValue(); 
                rec.fromLinkCombo=this.fromLinkCombo.lastSelectionText;
                rec.custWarehouse= this.warehouses.getValue();
                rec.movementtype= this.movmentType.getValue();
                rec.sequenceformat=this.sequenceFormatCombobox.getValue();
                var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):false;
                if(!this.copyInv){
                    if((this.record && this.record !== undefined) && (this.record.get('isfavourite') !== null || this.record.get('isfavourite') !== undefined)){
                        rec.isfavourite = this.record.get('isfavourite');
                    }    
            }
            Wtf.Ajax.requestEx({
                    url:this.ajxurl,
                    params: rec                    
                },this,this.genSuccessResponse,this.genFailureResponse);
   },
    showConfirmAndSave: function(rec,detail,incash){
        if(this.isConsignment && this.isCustomer){ //in consignment sales return proces
            if(Wtf.account.companyAccountPref.activateQAApprovalFlow){
            rec.isQAinspection=true; 
            Wtf.Msg.show({
            title:WtfGlobal.getLocaleText("acc.common.savdat"),
            closable:false,
            msg: WtfGlobal.getLocaleText("acc.consignment.returnmsg"),
            buttons: Wtf.Msg.YESNOCANCEL,
            scope:this,
            fn: function(btn){
                if(btn =="no") {
                        rec.isQAinspection=false;
                        this.saveWithCount(rec,detail,incash);
                    }else if(btn =="yes"){
                        this.saveWithCount(rec,detail,incash);
                   }else{
                       return;
                   } 
            },
            animEl: 'elId',
            icon: Wtf.MessageBox.QUESTION
            });
//                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.consignment.returnmsg"),function(btn){
//                    if(btn!="yes") {
//                        rec.isQAinspection=false;
//                        this.saveWithCount(rec,detail,incash);
//                    }else{
//                        this.saveWithCount(rec,detail,incash);
//                    }
//                },this);
            }else{
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.invoice.msg7"),function(btn){
                    if(btn=="yes") {
                        this.saveWithCount(rec,detail,incash);
                    }else{
                        return;
                    }
                },this);
            }
        }else{ //normal process means in consignment purchase return        
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),WtfGlobal.getLocaleText("acc.invoice.msg7"),function(btn){
                if(btn!="yes") {
                    return;
                }
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
                rec.transType=this.moduleid;
                rec.isConsignment=this.isConsignment;
                rec.custWarehouse= this.warehouses.getValue();
                rec.movementtype= this.movmentType.getValue();
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
        }
    },
    genSuccessResponse:function(response, request){
        WtfComMsgBox([this.title,response.msg],response.success*2+1);
        
        //Document Designer Print Button after saving the record
        var rec=this.NorthForm.getForm().getValues();
        this.exportRecord=rec;
        this.exportRecord['billid']=response.billid;
        this.exportRecord['billno']=response.billno;
        this.exportRecord['isConsignment']=this.isConsignment;
        this.exportRecord['moduleid']=this.moduleid;
        if (this.singleRowPrint) {
            this.singleRowPrint.exportRecord = this.exportRecord;      
        }
        
        if(response.success){
            Wtf.dupsrno.length=0;
            if(this.productOptimizedFlag==Wtf.Show_all_Products){
                Wtf.productStoreSales.reload();
                Wtf.productStore.reload(); //Reload all product information to reflect new quantity, price etc    
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
            if(this.productOptimizedFlag==Wtf.Show_all_Products){
                this.Grid.priceStore.purgeListeners();
                this.Grid.loadPriceStoreOnly(new Date(),this.Grid.priceStore);
            }
            this.setTransactionNumber();
            this.sequenceFormatStore.load();
            this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
            this.fireEvent('update',this);
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
                                fieldLabel: WtfGlobal.getLocaleText("acc.CR.newconsignmentreturnno"),
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
            var label="";
            if(rec.data.isConsignment){
                label=(this.isCustomer)?WtfGlobal.getLocaleText("acc.Consignment.salesreturn"):WtfGlobal.getLocaleText("acc.Consignment.purchasereturn");
            }else{
                label = (this.isCustomer)?WtfGlobal.getLocaleText("acc.accPref.autoSR"):WtfGlobal.getLocaleText("acc.dimension.module.18");
            }
            var mode=(this.isCustomer)?61:63;
            if(this.isCustomer){
                callEmailWin("emailwin",rec,this.label,mode,true,false,false,false,false,false, false, true,false,true);
            }else{
                callEmailWin("emailwin",rec,this.label,mode,false,false,false,false,false,false, false, true,false,true);
            }   
            
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
        {
            name:'billid'
        },

        {
            name:'companyid'
        },

        {
            name:'companyname'
        },

        {
            name:'journalentryid'
        },

        {
            name:'entryno'
        },

        {
            name:'billto'
        },

        {
            name:'orderamount'
        },

        {
            name:'shipto'
        },

        {
            name:'mode'
        },

        {
            name:'billno'
        },

        {
            name:'date', 
            type:'date'
        },

        {
            name:'shipdate', 
            type:'date'
        },

        {
            name:'personname'
        },

        {
            name:'personemail'
        },
        
        {
           name:'billingEmail' 
        },

        {
            name:'personid'
        },

        {
            name:'shipping'
        },

        {
            name:'deleted'
        },

        {
            name:'memo'
        },

        {
            name:'posttext'
        },

        {
            name:'costcenterid'
        },

        {
            name:'costcenterName'
        },

        {
            name:'statusID'
        },

        {
            name:'shipvia'
        },

        {
            name:'fob'
        },

        {
            name:'status'
        },

        {
            name:'withoutinventory',
            type:'boolean'
        },

        {
            name:'isfavourite'
        },

        {
            name:'isConsignment'
        },
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
                isLeaseFixedAsset:false,
                isConsignment:true,
                moduleid : this.moduleid
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
//            var rec = this.CustomStore.getAt(0);      Refer issue- ERP-9254
            var personid = this.Name.getValue();
            var recIndex = WtfGlobal.searchRecordIndex(this.CustomStore, personid, "personid");
            recIndex = recIndex==-1 ? 0 : recIndex;     //If record not found default first record taken
            var rec = this.CustomStore.getAt(recIndex);
            var recData = rec.data;
            var selRec = "&amount="+0+"&bills="+recData.billid;
            var fileName = "";
            var mode = "";
              
            if(this.isCustomer){
                if(recData.isConsignment){ 
                    fileName="Consignment Sales Return "+recData.billno;
                    mode = 61;
                }
                else {
                    fileName="Sales Return"+recData.billno;
                    mode = 61;
                }
            }else{
                fileName=recData.isConsignment?"Consignment Purchase Return "+recData.billno:"Purchase Return "+recData.billno;
                mode = 63;
            }
        
            Wtf.get('downloadframe').dom.src = "ACCExportRecord/exportRecords.do?mode="+mode+"&rec="+selRec+"&personid="+recData.personid+"&filename="+fileName+"_v1"+"&filetype=pdf";
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
                    case "auto":
                        if(value!=undefined){
                            value=(value+"").trim();
                        }
                        value=encodeURI(value);
                        value="\""+value+"\"";
                        break;
                    case "date":
                        value="'"+WtfGlobal.convertToGenericDate(value)+"'";
                        break;
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
        if(this.isEdit && !this.copyInv){       //Edit case  
            var linkedDocuments = this.PO.getValue();
            if(linkedDocuments != ''){
                if(this.originallyLinkedDocuments.indexOf(linkedDocuments) == -1){
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
        return inValidProducts; // List of deactivated products
    }
});