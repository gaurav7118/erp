Wtf.account.NoteAgainsInvoice = function(config){
    Wtf.apply(this, config);
    this.isCN=config.isCN;
    this.heplmodeid=config.helpmodeid;
    this.isCustBill=config.isCustBill;
    this.moduleid = config.moduleid;
    //Flag to indicate whether Avalara integration is enabled and module is enabled for Avalara Integration or not
    this.isModuleForAvalara = (Wtf.account.companyAccountPref.avalaraIntegration && (config.moduleid == Wtf.Acc_Credit_Note_ModuleId)) ? true : false;
    this.reloadGrid = config.reloadGrid;
    this.cntype = config.cntype;
    this.gstCurrencyRate=0.0;
    this.currentAddressDetailrec="";
    this.linkRecord=null;
    this.singleLink=false;
    this.isEdit = config.isEdit;
    this.isReverseCNDN = (this.cntype==4?true:false);
    this.readOnly=config.readOnly;
    this.isCopy=config.isCopy;
    this.CustomerVendorTypeId="";
    this.GSTINRegistrationTypeId="";
    this.gstin="";
    this.gstdochistoryid="";
    this.ignoreHistory=false;
    this.isIndiaGST=WtfGlobal.isIndiaCountryAndGSTApplied(); 
    this.isShipping=CompanyPreferenceChecks.getGSTCalCulationType();
    this.isCreatedFromReturnForm=config.isCreatedFromReturnForm!=undefined?config.isCreatedFromReturnForm:false
    this.custVenOptimizedFlag = Wtf.account.companyAccountPref.custvenloadtype;
    this.invModuleId = this.isCN ? Wtf.Acc_Invoice_ModuleId : Wtf.Acc_Vendor_Invoice_ModuleId;
    if(this.isCN) {
        if(this.isReverseCNDN) {
            this.customerFlag = false;
        } else {
            this.customerFlag = true;
        }
    }else {
        if(this.isReverseCNDN) {
            this.customerFlag = true;
        } else {
            this.customerFlag = false;
        }
    }
    
    this.businessPerson=(this.customerFlag?"Customer":"Vendor");
    
    this.externalcurrencyrate=0;
    this.exchangeRateInRetainCase=false
    this.datechanged=false;
    this.noteType=config.isCN?'Credit Note':'Debit Note';
    this.custPermType=config.isCN?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.soUPermType=(config.isCN?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.isCustomer=config.isCN;
    this.typeval = false;
    this.currencyBeforeSelect="";
    this.nameBeforeSelect="";
    if (this.isCustomer) {
        this.typeval = true
    }
    this.isLinkedTransaction = (config.isLinkedTransaction == null || config.isLinkedTransaction == undefined)? false : config.isLinkedTransaction;
    this.butnArr = new Array();
    this.saveBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
        tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
        id: "save" + this.heplmodeid+ this.id,
        hidden:this.readOnly,
        iconCls: 'pwnd save',
        scope: this,
        handler: function(){
            if(this.isEdit && !this.isCopy){
                var documentNo=this.no.getValue();
                if(documentNo==null || documentNo==undefined || documentNo==""){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.NumberBlankAlert")], 2);
                    return ;
                }
            }
            if(this.isLinkedTransaction){
                this.updateform();
            }else{
                this.saveForm();
            }
        }
    });
    
    this.butnArr.push(this.saveBttn);
    this.showAddrress=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.ShowAddress"), 
        cls: 'pwnd add',
        id: "showaddress" + this.id,                
        tooltip : WtfGlobal.getLocaleText("acc.field.UseShowAddressoptiontoinsertAddresses"),       
        style:" padding-left: 15px;",
        scope: this,
        handler:this.getAddressWindow 
    });
    
    this.butnArr.push(this.showAddrress);
    
    this.tranType = null; //ERP-25258
    if(this.isCN){
        this.tranType = Wtf.autoNum.CreditNote;
    } else{
        this.tranType = Wtf.autoNum.DebitNote;
    }
    
    this.singleRowPrint = new Wtf.exportButton({ //Print Record(s) button : ERP-25258
        text: WtfGlobal.getLocaleText("acc.rem.236"),
        tooltip: WtfGlobal.getLocaleText("acc.rem.236.single"), //'Print Single Record Details',
        id: "printSingleRecord"+ this.id,
        iconCls: 'pwnd printButtonIcon',
        obj: this,
        disabled: this.readOnly?false:true,
        isEntrylevel: false,
        exportRecord:this.exportRecord,
        menuItem: {rowPrint: true},
        get: this.tranType,
        moduleid: this.moduleid
    });
    /*
     * Visible for Read Only Mode
     */
    if (this.readOnly) {
    this.butnArr.push(this.singleRowPrint); //Add Print Record(s) button
    }
            
    Wtf.apply(this, {
        bbar:this.butnArr
    });
    Wtf.account.NoteAgainsInvoice.superclass.constructor.call(this, config);
    /**
    * Account Grid and Invoice Grid not showing properly after Expanding/Collapsing Navigation Panel.
    */
    this.on('resize', function (panel) {
        panel.doLayout();
        if (panel.grid) {
            panel.grid.doLayout();
            panel.grid.getView().refresh();
            if (panel.InvGrid) {
                panel.InvGrid.doLayout();
                panel.InvGrid.getView().refresh();
            }
        }
    }, this);
}

Wtf.extend(Wtf.account.NoteAgainsInvoice, Wtf.account.ClosablePanel, {
    onRender: function(config){
        Wtf.account.NoteAgainsInvoice.superclass.onRender.call(this, config);
        this.isClosable=false   
        this.createDisplayGrid();
        if(this.cntype == 1 || this.cntype == 3){
            this.createInvoiceGrid();
        }
        
        this.createForm(); 
        if(this.readOnly)
        {
            this.isClosable=true
        }
        var colModelArray = GlobalColumnModel[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray, this.store);
        this.addGridRec();       
        if(this.cntype == 1 || this.cntype == 3){
            this.addInvoiceGridRec();
        }
        if(this.isEdit){
            this.loadRecord();
        }
       var title=this.noteType;
        var msg=this.title;
        var isgrid=true;
        
        var itemsArray = new Array();
        if(this.cntype == 1 || this.cntype == 3){
            itemsArray.push(this.TypeForm,this.grid,this.InvGrid);
        }else{
            itemsArray.push(this.TypeForm,this.grid);
        }
        var blockSpotRateLink_first = "";
        var blockSpotRateLink_second = "";
        if(!Wtf.account.companyAccountPref.activateToBlockSpotRate && !this.isLinkedTransaction){ // If activateToBlockSpotRate is set then block the Spot Rate Links and when transaction already linked in another transaction.){ // If activateToBlockSpotRate is set then block the Spot Rate Links
            blockSpotRateLink_first = "<br/>"+WtfGlobal.getLocaleText("acc.invoice.msg9")+"</div><div style='padding-left:30px;padding-top:5px;padding-bottom:10px;'><a class='tbar-link-text' href='#' onClick='javascript: editInvoiceExchangeRates(\""+this.id+"\",\"{foreigncurrency}\",\"{basecurrency}\",\"{revexchangerate}\",\"foreigntobase\")'wtf:qtip=''>{foreigncurrency} to {basecurrency}</a>";
            blockSpotRateLink_second = "<br/>"+WtfGlobal.getLocaleText("acc.invoice.msg9")+"</div> <div style='padding-left:30px;padding-top:5px;'><a class='tbar-link-text' href='#' onClick='javascript: editInvoiceExchangeRates(\""+this.id+"\",\"{basecurrency}\",\"{foreigncurrency}\",\"{exchangerate}\",\"basetoforeign\")'wtf:qtip=''>{basecurrency} to {foreigncurrency}</a></div>";
        }
        this.southCenterTplSummary=new Wtf.XTemplate(
            "<div> &nbsp;</div>",  //Currency:
            '<tpl if="editable==true">',
            "<b>"+WtfGlobal.getLocaleText("acc.invoice.msg8")+"</b>",  //Applied Exchange Rate for the current transaction:
            "<div style='line-height:18px;padding-left:30px;'>1 {foreigncurrency} "+WtfGlobal.getLocaleText("acc.inv.for")+" = {revexchangerate} {basecurrency} "+WtfGlobal.getLocaleText("acc.inv.hom")+". "+
            blockSpotRateLink_first,
            "</div><div style='line-height:18px;padding-left:30px;'>1 {basecurrency} "+WtfGlobal.getLocaleText("acc.inv.hom")+" = {exchangerate} {foreigncurrency} "+WtfGlobal.getLocaleText("acc.inv.for")+". "+    
            blockSpotRateLink_second,
            '</tpl>'
            );
       
        this.southCenterTpl = new Wtf.Panel({
            border: false,
            disabled: this.readOnly,
            disabledClass: "newtripcmbss",
            html: this.southCenterTplSummary.apply({basecurrency: WtfGlobal.getCurrencyName(), exchangerate: 'x', foreigncurrency: "Foreign Currency", editable: false})
        });
        this.southPanel = new Wtf.Panel({
            region: 'south',
            border: false,
            hidden:false,
            layout: 'border',
            disabledClass: "newtripcmbss",
            autoScroll: true,
            height: 200,
            items: [{
                    region: 'center',
                    border: false,
                    autoHeight: true,
                    items: [this.southCenterTpl]
                }]
        });
        itemsArray.push(this.southPanel);
        this.centerPanel=new Wtf.Panel({
            border: false,
            region: 'center',
            id: 'centerpan'+this.id,
            autoScroll:true,
            items:itemsArray
        })
       
        this.add(this.centerPanel);
        if (Wtf.account.companyAccountPref.countryid === Wtf.CountryID.MALAYSIA) {
            this.enableDisableTaxUsingGSTActivationDate(); // enable/disble tax
        } 
        /*
         *For hide form level fields
         */
        this.hideFormFields();
        this.getMyConfig();
        
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveGridStateHandler, this);
        }, this);
        
        if (this.readOnly) {
            this.grid.enableColumnMove = false;
            this.grid.enableColumnResize = false;
        }
        
        if (this.cntype == 1 || this.cntype == 3) {
            new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
                this.InvGrid.on('statesave', this.saveInvGridStateHandler, this);
            }, this);
            
            if (this.readOnly) {
                this.InvGrid.enableColumnMove = false;
                this.InvGrid.enableColumnResize = false;
            }
        }
    },
    hideFormFields:function(){
        if(this.moduleid==Wtf.Acc_Credit_Note_ModuleId ){
            this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.creditNote);
        }else if(this.moduleid==Wtf.Acc_Debit_Note_ModuleId){
            this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.debitNote);
        }
    },
    hideTransactionFormFields:function(array){
        if(array){
            for(var i=0;i<array.length;i++){
                var fieldArray = array[i];
                if(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id)){
                    if(fieldArray.fieldId=="ShowOnlyOneTime" && ((this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv !=undefined ?this.copyInv:false) || (this.isCopyFromTemplate !=undefined ?this.isCopyFromTemplate:false) || (this.isTemplate !=undefined ?this.isTemplate:false))){
                        continue;
                    }
                    /*
                     * If form level field is hidden 
                     */
                    if(fieldArray.isHidden){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hideLabel = fieldArray.isHidden;
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hidden = fieldArray.isHidden;
                    }
                    /*
                     * If form level field is idReadOnly 
                     */
                    if(fieldArray.isReadOnly){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).disabled = fieldArray.isReadOnly;
                    }
                    /*
                     * If form level field is Manadatory
                     */
                    if(fieldArray.isUserManadatoryField && Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel != undefined){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).allowBlank = !fieldArray.isUserManadatoryField;
                        var fieldLabel="";
                        if(fieldArray.fieldLabelText!="" && fieldArray.fieldLabelText!=null && fieldArray.fieldLabelText!=undefined){
                            fieldLabel= fieldArray.fieldLabelText+" *";
                        }else{
                            fieldLabel=(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel) + " *";
                        }
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel = fieldLabel;
                    }else{
                        if( fieldArray.fieldLabelText!=null && fieldArray.fieldLabelText!=undefined && fieldArray.fieldLabelText!=""){
                            if(fieldArray.isManadatoryField && fieldArray.isFormField )
                                Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel=fieldArray.fieldLabelText +"*";
                            else
                                Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel=fieldArray.fieldLabelText;
                        }
                    }
                }
            }
        }
    },
    
    loadRecord:function(){
        if(this.record != null && this.cntype != 4 && this.cntype != 2){
            this.InvGridComboStore.load({
                params:{
                    accid:(this.record.data && this.record.data.personid)?this.record.data.personid:"",
                    currencyfilterfortrans :(this.record.data && this.record.data.currencyid)?this.record.data.currencyid:"",
                    isReceipt:true,          //sending flag to avoid currency filter
                    invoicesForNoteEditMode:true,    // ERP-3689 : Parameter is sent to fetch only those invoices which are linked to particular CN or DN
                    noteId : this.record.data.noteid
                }
            })
        }
        if (this.record != null && this.custVenOptimizedFlag) {
            this.name.setValForRemoteStore(this.record.data.personid, this.record.data.personname,this.record.data.hasAccess); // create record and set value
        }
        if (Wtf.account.companyAccountPref.countryid === Wtf.CountryID.MALAYSIA) { // for malaysian company
            this.enableDisableFields();
        }
        if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
            if (this.record !== null && this.record !== undefined && this.record.data.linkInvoices !== null && this.record.data.linkInvoices !== undefined && this.record.data.linkInvoices !== "") {
                this.fromLinkCombo.setValue("1");
                this.POStore.on("load", function () {
                    this.PO.setValue(this.record.data.linkInvoices);
                }, this);
                this.POStore.load();
            }
            if (this.isIndiaGST) {
                if (this.record.data.CustomerVendorTypeId != undefined) {
                    this.CustomerVendorTypeId = this.record.data.CustomerVendorTypeId;
                }
                if (this.record.data.GSTINRegistrationTypeId != undefined) {
                    this.GSTINRegistrationTypeId = this.record.data.GSTINRegistrationTypeId;
                }
                if (this.record.data.gstin != undefined) {
                    this.gstin = this.record.data.gstin;
                }
                if (this.record.data.gstdochistoryid != undefined) {
                    this.gstdochistoryid = this.record.data.gstdochistoryid;
                }
            }
        }
        if (this.isEdit && this.isCreatedFromReturnForm) {
            this.enableInvAmountInEditCase();
        }
    },
    enableInvAmountInEditCase: function() {
        if (this.grid) {
            this.grid.disable();
        }
    },
    createDisplayGrid:function(){
        
        Wtf.reasonStore.load();
        
        this.reason= new Wtf.form.FnComboBox({
            triggerAction:'all',
            mode: 'local',
//            selectOnFocus:true,
            valueField:'id',
            displayField:'name',
            id:"reason"+this.id,
            allowBlank:true,
            store:Wtf.reasonStore,
//            addNoneRecord: true,
//            anchor: '94%',
            width : 200,
//            typeAhead: true,
            forceSelection: true,
            fieldLabel: 'Reason',
            emptyText: 'Select Reason',
            name:'reason',
            hiddenName:'reason'            
        });
        
        this.reason.addNewFn=this.addReason.createDelegate(this);
        
        this.taxRec = new Wtf.data.Record.create([
           {name: 'prtaxid',mapping:'taxid'},
           {name: 'prtaxname',mapping:'taxname'},
           {name: 'taxdescription'},
           {name: 'percent',type:'float'},
           {name: 'taxcode'},
           {name: 'accountid'},
           {name: 'accountname'},
           {name: 'hasAccess'},
           {name: 'applydate', type:'date'}

        ]);
        
        this.taxStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.taxRec),
    //        url: Wtf.req.account + 'CompanyManager.jsp',
            url : "ACCTax/getTax.do",
            baseParams:{
                mode:33,
                includeDeactivatedTax: this.isEdit != undefined ? (this.isCopy ? false : this.isEdit) : false
//                moduleid :this.moduleid
            }
        });
        this.taxStore.on("load", function() {
            var record = new Wtf.data.Record({
                prtaxid: 'None',
                prtaxname: 'None'
            });
            this.taxStore.insert(this.taxStore.getCount() + 1, record);
        }, this);  
        
        this.taxStore.load();
        
        this.transTax= new Wtf.form.ExtFnComboBox({
            hiddenName:'prtaxid',
            anchor: '100%',
            store:this.taxStore,
            valueField:'prtaxid',
//            alignTo:("bl-tl?"),
//            listAlign: 'l?',
            forceSelection: true,
            displayField:'prtaxname',
//            addNewFn:this.addTax.createDelegate(this),
            scope:this,
            displayDescrption:'taxdescription',
            selectOnFocus:true,
            typeAhead: true,
            mode: 'remote',
            minChars:0,
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
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.edit)){
            this.transTax.addNewFn=this.addTax.createDelegate(this);
        }
        this.editPriceIncludingGST=new Wtf.form.NumberField({
            allowBlank: true,
            allowNegative:false,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            value:0
        });
        
        this.transTaxAmount=new Wtf.form.NumberField({
            allowBlank: true,
            allowNegative: false,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            value:0
        });
        
        
        this.accRec = Wtf.data.Record.create([
        {
            name:'accountname',
            mapping:'accname'
        },{
            name:'accountid',
            mapping:'accid'
        },{
            name:'acccode'
        },{
            name:'groupname'
        },{
            name: 'hasAccess'
        },{
            name: 'haveToPostJe'
        },{
            name: 'usedIn'
        }]);
    
        this.accountStore = new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
//                group:[1,2,3,4,5,6,7,8,11,12,14,15,19,20,21,22],
              ignorecustomers:true,  
              ignorevendors:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        this.accountStore.on('load',function(){
            
            if(this.isEdit && this.record != null){
                this.store.load({
                    params:{
                        noteId:(this.record.data && this.record.data.noteid)?this.record.data.noteid:"",
                        isCopy:this.isCopy
                    }
                });
            }
        },this);
        /*
         *Check box for Including GST
         */
        this.includingGST= new Wtf.form.Checkbox({
            name:'includingGST',
            id:"includingGST"+this.heplmodeid+this.id,
            hideLabel:(SATSCOMPANY_ID==companyid || Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA)?true:(Wtf.account.companyAccountPref.countryid == Wtf.Country.MALAYSIA && !Wtf.account.companyAccountPref.enableGST),
            hidden:(SATSCOMPANY_ID==companyid || Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA)?true:(Wtf.account.companyAccountPref.countryid == Wtf.Country.MALAYSIA && !Wtf.account.companyAccountPref.enableGST),
            fieldLabel:(Wtf.account.companyAccountPref.countryid!= Wtf.Country.INDONESIA)?"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.includeGST.tooltip")+"'>"+WtfGlobal.getLocaleText("acc.cust.includingGST")+"</span>":"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.includeVAT.tooltip")+"'>"+WtfGlobal.getLocaleText("acc.cust.includingVAT")+"</span>",
            cls : 'custcheckbox',
            disabled:(this.readOnly||this.isLinkedTransaction),
            width: 10
        });  
        this.cmbAccount=new Wtf.form.ExtFnComboBox({
                hiddenName:'accountid',
                store:this.accountStore,
                minChars:1,
                listWidth :300,
                valueField:'accountid',
                displayField:'accountname',
                isAccountCombo:true,
                forceSelection:true,
                hirarchical:true,
                extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
                mode: 'local',
                typeAheadDelay:30000,
                extraComparisionField:'acccode'
            })
            this.cmbAccount.on('beforeselect',function(combo,record,index){
                return validateSelection(combo,record,index);
            },this);
            this.cmbAccount.on('select',function(combo,record,index){
                this.accountStore.clearFilter();
            },this);
            
            this.cmbAccount.on('change',function(combo, newValue,oldValue){
                var accRec=WtfGlobal.searchRecord(this.accountStore, this.cmbAccount.getValue(), 'accountid');
                var haveToPostJe = accRec ? accRec.data.haveToPostJe : false;
                if(haveToPostJe){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), 
                        WtfGlobal.getLocaleText({
                            key:this.isCN?"acc.canNotCreateCN":"acc.canNotCreateDN", 
                            params:[accRec ? accRec.data.usedIn : ""]
                        })], 0);
                    combo.setValue(oldValue);
                    this.accountStore.remove(accRec);
        }
            },this);
            
        this.accountStore.load();
            this.typeStore = new Wtf.data.SimpleStore({
        fields: [{name: "id"}, {name: "name"}],
        data: [[true, "Debit"], [false, "Credit"]]
    });
    
        this.cmbType = new Wtf.form.ComboBox({
            hiddenName: 'debit',
            store: this.typeStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            triggerAction: 'all',
            forceSelection: true,
            disabled: (this.readOnly||this.isLinkedTransaction),
            hidden: !Wtf.account.companyAccountPref.manyCreditDebit
        });
            
        var mulDebitCMArr = [];      
        mulDebitCMArr.push({
            header:WtfGlobal.getLocaleText("acc.je.acc"),//config.isReceipt? WtfGlobal.getLocaleText("acc.rem.217") : WtfGlobal.getLocaleText("acc.rem.30"),  //"Debit Account",
            dataIndex:'accountid',
            width:300,
//            hidden : this.isAdvPayment,
            editor:( this.readOnly || this.isLinkedTransaction || this.isEditToApprove)?"":this.cmbAccount,
            renderer:Wtf.comboBoxRenderer(this.cmbAccount)
        });
    
        if (Wtf.account.companyAccountPref.isLineLevelTermFlag && WtfGlobal.isIndiaCountryAndGSTApplied()) {
            mulDebitCMArr.push({
                header: WtfGlobal.getLocaleText("acc.field.DocumentNumber"),
                dataIndex: 'documentno'
            });
        }
        
     mulDebitCMArr.push({
            header: WtfGlobal.getLocaleText("acc.je.type"), //"Type",
            editor: ( this.readOnly || this.isLinkedTransaction || this.isEditToApprove)?"":this.cmbType ,
            renderer: Wtf.comboBoxRenderer(this.cmbType),
            dataIndex: 'debit',
            hidden:!Wtf.account.companyAccountPref.manyCreditDebit 
        },{
            header:WtfGlobal.getLocaleText("acc.1099.gridAmt"),//config.isReceipt? WtfGlobal.getLocaleText("acc.rem.218") : WtfGlobal.getLocaleText("acc.rem.31"),  //"Debit Amount",
            dataIndex:'dramount',
            width:200,
            align:'right',
            summaryType:'sum',
            editor:( this.readOnly || this.isLinkedTransaction || this.isEditToApprove)?"":this.editPriceIncludingGST, 
            renderer: WtfGlobal.amountWithoutCurrencyRender
        },{
            header: WtfGlobal.getLocaleText("acc.invoice.Tax"),
            dataIndex:"prtaxid",
            id:this.id+"prtaxid",
            fixed:true,
            width:200,
            hidden:((Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA || Wtf.account.companyAccountPref.countryid == '137') && !Wtf.account.companyAccountPref.enableGST),// hide if company is malaysian and GST is not enabled for it,
            renderer:Wtf.comboBoxRenderer(this.transTax),
            editor:( this.readOnly || this.isLinkedTransaction || this.isEditToApprove)?"":this.transTax 
        },{
             header:WtfGlobal.getLocaleText("acc.invoice.gridamountIncludingGST"),// "Column for rate Including GST",
             dataIndex: "rateIncludingGst",
             align:'right',
             fixed:true,
             width:150,
             renderer: WtfGlobal.amountWithoutCurrencyRender,
             hidden:true
        });
        
        if (Wtf.account.companyAccountPref.isLineLevelTermFlag && WtfGlobal.isIndiaCountryAndGSTApplied()) {
            mulDebitCMArr.push({
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
        
        mulDebitCMArr.push({
            header: WtfGlobal.getLocaleText("acc.invoice.gridTaxAmount"),//"Tax Amount",
            dataIndex:"taxamount",
            id:this.id+"taxamount",
            fixed:true,
            //align:'right',
            width:150,
            editor:( this.readOnly || this.isLinkedTransaction || this.isEditToApprove)?"":this.transTaxAmount,
            hideLabel:((Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST) || (Wtf.account.companyAccountPref.isLineLevelTermFlag && WtfGlobal.isIndiaCountryAndGSTApplied())) ,
            hidden:((Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST) || (Wtf.account.companyAccountPref.isLineLevelTermFlag && WtfGlobal.isIndiaCountryAndGSTApplied())) ,
//            hidden:(Wtf.account.companyAccountPref.countryid == '137' && (!Wtf.account.companyAccountPref.enableGST || this.cntype == '2')),// hide if company is malaysian and GST is not enabled for it or if CN/DN is otherwise,
//            renderer:this.setTaxAmountWithotExchangeRate.createDelegate(this)
            renderer:this.taxAmountRendererWithoutSymbol.createDelegate(this)
        },{
            header:  WtfGlobal.getLocaleText("acc.india.tds.calculation.in.dn.button"), 
            align: 'center',
            width: 40,
            renderer: function(v, m, rec) {
                return "<div class='" + getButtonIconCls(Wtf.etype.addtdsgrid) + "'></div>";
            },
            hidden:  (Wtf.Countryid != Wtf.Country.INDIA || !Wtf.isTDSApplicable || this.isCustomer || this.cntype == 4 ) ,
            disabled:( this.readOnly || this.isEditToApprove)
        },{
            header:WtfGlobal.getLocaleText("acc.mp.amtTaxTotal"),
            dataIndex:'amountwithtax',
            width:200,
            align:'right',
//            hidden:(Wtf.Countryid==Wtf.Country.MALAYSIA && this.cntype=='2'),//ERM-352
//            renderer:this.calAmountWithoutExchangeRate.createDelegate(this)
            renderer: this.amountWithTaxRendererWithoutSymbol.createDelegate(this)
        },{
            header:WtfGlobal.getLocaleText("acc.masterConfig.29"),   //Reason
            dataIndex:'reason',
            width:200,
             renderer:Wtf.comboBoxRenderer(this.reason),
             editor:(this.readOnly || this.isEditToApprove)?"": this.reason
        },{
            header:WtfGlobal.getLocaleText("acc.invReport.desc"),  //"Description",
            dataIndex:"description",
            width:260,
            editor:(this.readOnly || this.isEditToApprove)?"": this.Description=new Wtf.form.TextArea({
                maxLength:1024,
                xtype:'textarea'
            }),
            renderer:function(value,meta,rec){
                meta.attr = "Wtf:qtip='" + value + "' Wtf:qtitle='Description' ";
                return value;
            }
        });
        mulDebitCMArr = WtfGlobal.appendCustomColumn(mulDebitCMArr,GlobalColumnModel[this.moduleid],undefined,undefined,this.readOnly);
        mulDebitCMArr.push({
            header:WtfGlobal.getLocaleText("acc.rem.7"),
            dataIndex:"delete",
            align:'center',
            width:100,
            hidden:( this.readOnly || this.isLinkedTransaction ||this.isEditToApprove),
            renderer:function(){
                return "<div class='pwnd delete-gridrow' > </div>";
            }});
        this.mulDebitCM = new Wtf.grid.ColumnModel(mulDebitCMArr);
       
        this.ccRec = new Wtf.data.Record.create([
        {
            name: 'accountid'
        },
        {
            name: 'debit', defValue: this.typeval}, // it is used mainly for Many CN/DN
        {
            name: 'dramount'
        },
        {
            name: 'prtaxid'
        },
        {
            name: 'amountwithtax'
        },
        {
            name: 'taxamount'
        },
        {
            name:'rateIncludingGst'
        },
        {
            name:'istdsamount',type:'boolean'
        },
        {
            name:'customfield'
        },
        {
            name: 'description'
        },{
            name: 'reason'
        },{
            name:'gstCurrencyRate',defValue:0
        },
        {
            name:'srNoForRow',defValue: 0
        },
            {
            name: 'rowid'
        },
        {name:'taxclass'},
        {name:'taxclasshistoryid'},
        {name: 'productid'},
        {name: 'recTermAmount'},
        {name: 'LineTermdetails'}]);
        this.store = new Wtf.data.Store({
            url:(this.isReverseCNDN)?(this.customerFlag?'ACCDebitNote/getDebitNoteAccountsRows.do':'ACCCreditNote/getCreditNoteAccountsRows.do'):(this.customerFlag?'ACCCreditNote/getCreditNoteAccountsRows.do':'ACCDebitNote/getDebitNoteAccountsRows.do'),
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.ccRec)
        });
        
        this.store.on('load',this.processAfterStoreLoad,this);
        
        this.grid = new Wtf.grid.EditorGridPanel({
            plugins:this.gridPlugins,
            clicksToEdit:1,
            height:this.cntype == 1?175:200,
            style:'padding:10px;',
            store: this.store,
            cm: this.mulDebitCM,
            disabledClass:"newtripcmbss",
            readOnly:(this.readOnly|| this.isEditToApprove),
            border : true,
            loadMask : true,
            viewConfig: {
                forceFit:false
            }
        });
        this.grid.addEvents({
            'datachanged': true
        });
        this.grid.on('rowclick',this.processRow,this);
        this.grid.on('cellclick',this.RitchTextBoxSetting,this);
        this.grid.on('afteredit',this.updateAccountRow,this);
        this.grid.on('beforeedit',this.checkrecord,this);
        this.grid.on('populateDimensionValue', this.populateDimensionValueingrid, this);    
        if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
            this.grid.on('datachanged', this.onGridDataChanged, this);
        }
       
       
    },
    enableDisableTaxUsingGSTActivationDate: function() {
        var date = this.creationDate.getValue() === "" ? new Date() : new Date(this.creationDate.getValue());
        var isTaxShouldBeEnable = WtfGlobal.isTaxShouldBeEnable(date.clearTime());
        if (!isTaxShouldBeEnable) {
            if (this.transTax) { // tax
                this.transTax.setDisabled(true);
            }
            if (this.transTaxAmount) { // tax amount
                this.transTaxAmount.setDisabled(true);
            }
        } else {
            if (this.transTax) {// tax
                this.transTax.setDisabled(false);
            }
            if (this.transTaxAmount) { // tax amount 
                this.transTaxAmount.setDisabled(false);
            }
        }
    },
    taxAmountRendererWithoutSymbol: function (value, m, rec) {
        var taxamount = value;
        /*
         * ERP-40242 : In copy case, deactivated tax not shown.Hence, empty taxid set in record 
         * and taxamount=0.
         */
        if (rec.data.prtaxid != '' && this.isCopy) {
            var taxActivatedRec = WtfGlobal.searchRecord(this.taxStore, rec.data.prtaxid, "prtaxid");
            if (taxActivatedRec == null || taxActivatedRec == undefined || taxActivatedRec == "") {
                rec.data.prtaxid = "";
            }
        }
        var checkForIndia= (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Number(rec.data.taxamount) != 0)
        
        if(checkForIndia || (this.isModuleForAvalara)){//check to show tax amount when Avalara Intagration is enabled
            rec.set('taxamount',rec.data.taxamount);
        }else if (rec.data.prtaxid == null || rec.data.prtaxid == undefined || rec.data.prtaxid == "" || rec.data.prtaxid == "None") {
            rec.set('taxamount',0);
            taxamount = 0;
        }
        taxamount = WtfGlobal.conventInDecimal(taxamount, "");
        return '<div class="currency">' + taxamount + '</div>';
    },
    amountWithTaxRendererWithoutSymbol: function (value, m, rec) {
    var origionalAmount = 0;
    if (rec.data.dramount != '' || rec.data.dramount != undefined) {
        origionalAmount = rec.data.dramount;
    }
    var taxamount = 0;
    if (rec.data.taxamount != null || rec.data.taxamount != undefined || rec.data.taxamount != "" || rec.data.prtaxid != "None") {
        taxamount = rec.data.taxamount;
    }
        if (WtfGlobal.isIndiaCountryAndGSTApplied() && rec.data.recTermAmount !== undefined && rec.data.recTermAmount !== null && rec.data.recTermAmount !== "") {
            if (rec.data.recTermAmount > 0) {
                origionalAmount += rec.data.recTermAmount;
            }
        }
    /*
     *Calculate Amount with Tax and set to amounttax column
     */
    if(this.includingGST.getValue()){
        if (rec.data.rateIncludingGst != '' || rec.data.rateIncludingGst != undefined) {
            origionalAmount = rec.data.rateIncludingGst;
        }
        origionalAmount += taxamount;
    }else{
        origionalAmount += taxamount;
    }
    origionalAmount = WtfGlobal.conventInDecimal(origionalAmount, "");
    return '<div class="currency">' + origionalAmount + '</div>';
},
processAfterStoreLoad : function(){
    this.addGridRec()
    var totalAmt = this.getAccountsAmount();
    this.Amount.setValue(totalAmt);
},
RitchTextBoxSetting:function(grid, rowIndex, columnIndex, e){
    var record = grid.getStore().getAt(rowIndex);
    var fieldName= grid.getColumnModel().getDataIndex(columnIndex);
    if(e.getTarget(".richtext")){//ERP-8199 :
        var value = record.get(fieldName);
        new Wtf.RichTextArea({
            rec:record,
            fieldName:fieldName,
            val: value?value:"",
            readOnly:this.readOnly
        });
    }
    if(Wtf.account.companyAccountPref.proddiscripritchtextboxflag!=0 && !this.readOnly){
        if(fieldName == "description" && !this.readOnly){
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
//            val = val.replace(/(<([^>]+)>)/ig,""); // Just comment this line to fix ERP-8675
            this.prodDescTextArea.setValue(val);
            if(record.data.accountid !=undefined && record.data.accountid !=""){
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
                            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                            iconCls: 'pwnd save',
                            handler: function()
                            {
                                record.set('description',  Wtf.get('descriptionRemarkTextAreaId').getValue());
                                win.close();   
                            }
                        },{
                            text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
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

        if (WtfGlobal.isIndiaCountryAndGSTApplied() && fieldName == "documentno" && !this.readOnly) {
           this.openDocumentWindow(e,rowIndex);
            return;
        }
},
    processRow:function(grid,rowindex,e){        
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                var record = store.getAt(rowindex);
               
                store.remove(store.getAt(rowindex));
                if(rowindex==total-1){
                    this.addGridRec();
                }
                var totalAmt = this.getAccountsAmount();
                this.Amount.setValue(totalAmt);
            }, this);
        }
        if(e.getTarget(".tdsCalc-gridrow")){
            if(this.readOnly || this.isEditToApprove){
                return;
            }
            var store=grid.getStore();
            var total=store.getCount();
            var record = store.getAt(rowindex);
            if(record && record.data){
                if(!record.data.dramount || record.data.dramount <= 0){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleaseenteramount")], 2);
                    return;
                }else{
                    if(!Wtf.isEmpty(this.getPersonInformation())){
                        var vendor = this.getPersonInformation();
                        if(Wtf.isEmpty(vendor.deducteetype)){  //Vendor's Deductee type
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.Deducteetypeisnotset")], 2);
                            return;
                        }else if(Wtf.isEmpty(vendor.residentialstatus)){   //Vendor belongs to Residential/Non-Residentia
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.Residentialstatusisnotset")], 2);
                            return;
                        }else if(Wtf.isEmpty(vendor.natureOfPayment)){   //Vendor belongs to Residential/Non-Residentia
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.mp.NatureOfPaymentisnotset")], 2);
                            return;
                        }
                    } 
                } 
            }
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.india.tds.calculation.in.dn.alert"), function(btn){
                if(btn!="yes") return;
                if(record && record.data && record.data.dramount){
                    record.data.istdsamount= true;
                }
            }, this);
        } else if (e.getTarget(".termCalc-gridrow")) {
            if (WtfGlobal.isIndiaCountryAndGSTApplied() && Wtf.account.companyAccountPref.isLineLevelTermFlag) {
                this.showTermWindow(grid.getStore().getAt(rowindex), grid, rowindex);
            } else {
                return;
            }
        }
    },
    
    addReason:function(){
        addMasterItemWindow('29');
        if(Wtf.getCmp('masterconfigurationonly')!=undefined){
        Wtf.getCmp('masterconfigurationonly').on('update', function(){Wtf.reasonStore.reload();}, this);
        }
    },
    getPersonInformation: function(){
        var record = WtfGlobal.searchRecord(this.name.store, this.name.getValue(), "accid");
        var vendorId = "";
        var customerId = "";
        vendorId = record.data['accid'];
        customerId = record.data['accid'];
        var parameters = {
            vendorId: vendorId,
            customerId: customerId,
            currencyid: this.Currency.getValue(),
            accid: record.data['accid'],
            personName: record.data.accname,
            personCode: (record.data.acccode)?record.data.acccode:this.presonCode,
            upperLimitDate: WtfGlobal.convertToGenericDate(this.creationDate.getValue()),
            residentialstatus:record.data.residentialstatus,
            deducteetype:record.data.deducteetype,
            deducteetypename:record.data.deducteetypename,
            natureOfPayment:record.data.natureOfPayment
        }
        return parameters;
    },
        
    updateAccountRow:function(obj){
        var rec=obj.record;
        var originalAmount=rec.data.dramount;
        if(obj.field=="prtaxid" || obj.field=="dramount"){
            var taxamount = this.setTaxAmountAfterSelection(obj.record);
            rec.set("taxamount",taxamount);
            if(obj.field=="prtaxid"&&WtfGlobal.singaporecountry()&&WtfGlobal.getCurrencyID()!=Wtf.Currency.SGD&& this.currencyid!=Wtf.Currency.SGD){
                    var record = WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid");
                    callGstCurrencyRateWin(this.id,record.data.currencyname+" ",obj,obj.record.get("gstCurrencyRate")*1);
            }
        }
        
        if(obj.field=="dramount"){
            this.addGridRec();
        }
        /*
         *Set Value rate Including Gst Column
         */
         if(this.includingGST.getValue()&& rec.data.taxamount > originalAmount){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.dncn.promtmessage")],2);
            return;
        }
        rec.set("rateIncludingGst",(rec.data.dramount-rec.data.taxamount));
        if (WtfGlobal.isIndiaCountryAndGSTApplied() && rec.data['productid'] != undefined && rec.data['productid'] !== "" && obj.field === "dramount") {
            getLineTermDetailsAndCalculateGSTForAdvance(this, this.grid, rec.data['productid']);
        }
        var totalAmt = this.getAccountsAmount();
        this.Amount.setValue(totalAmt);
        
    },
    
    getAccountsAmount:function(){
       var amt=0;
       for(var i=0; i<this.grid.store.getCount()-1;i++){
        if ((this.isCustomer && this.grid.store.getAt(i).data['debit']) || (!this.isCustomer && !this.grid.store.getAt(i).data['debit'])) {
            if(this.includingGST.getValue()){
                /*
                 *If includingGST check is true then take amount from rateincludingGST column and calculate total amount
                 */
                amt += getRoundedAmountValue(this.grid.store.getAt(i).data['rateIncludingGst']);
            }else{
                amt += getRoundedAmountValue(this.grid.store.getAt(i).data['dramount']);
            }
            amt += getRoundedAmountValue(this.grid.store.getAt(i).data['taxamount']);
                if (WtfGlobal.isIndiaCountryAndGSTApplied() && this.grid.store.getAt(i).data['recTermAmount'] !== undefined && this.grid.store.getAt(i).data['recTermAmount'] !== null && this.grid.store.getAt(i).data['recTermAmount'] !== "") {
                    if (this.grid.store.getAt(i).data['recTermAmount'] > 0) {
                        amt += getRoundedAmountValue(this.grid.store.getAt(i).data['recTermAmount']);
                    }
                }
        } else {
            amt -= getRoundedAmountValue(this.grid.store.getAt(i).data['dramount']);
            amt -= getRoundedAmountValue(this.grid.store.getAt(i).data['taxamount']);
                if (WtfGlobal.isIndiaCountryAndGSTApplied() && this.grid.store.getAt(i).data['recTermAmount'] !== undefined && this.grid.store.getAt(i).data['recTermAmount'] !== null && this.grid.store.getAt(i).data['recTermAmount'] !== "") {
                    if (this.grid.store.getAt(i).data['recTermAmount'] > 0) {
                        amt -= getRoundedAmountValue(this.grid.store.getAt(i).data['recTermAmount']);
                    }
                }
        }
    }
       return WtfGlobal.conventInDecimalWithoutSymbol(amt);
    },

    addGridRec:function(obj){
        var size=this.store.getCount();
        if(size>0){
            var lastRec=this.store.getAt(size-1);
            if(lastRec.get('accountid') == ''){
                lastRec.set('accountid', '');
                return;
            }
            if(lastRec.get('dramount') == ''){
                lastRec.set('dramount', '');
                return;
            }
            }
        var rec= this.ccRec;
        rec = new rec({});
        rec.beginEdit();
        var fields=this.store.fields;
        for(var x=0;x<fields.items.length;x++){
            var value="";
            if(fields.get(x).name == 'dramount' || fields.get(x).name == 'amountwithtax' || fields.get(x).name == 'taxamount'){
                rec.set(fields.get(x).name, 0);
            }else{
                rec.set(fields.get(x).name, value);
            }
            if (fields.get(x).name == 'istdsamount') {
                rec.set(fields.get(x).name, false);
            }      
            if (fields.get(x).name == 'debit') {
                rec.set(fields.get(x).name, this.typeval);
            }      
        }      
        rec.endEdit();
        rec.commit();
        this.store.add(rec);
        
    },
    

    
    checkrecord:function(obj){
        if(this.istax){
            var idx = this.grid.getStore().find("taxid", obj.record.data["taxid"]);
            if(idx>=0)
                obj.cancel=true;
        }
        if(this.name.getValue()=='')
        {
            obj.cancel=true;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),this.cntype == 4?(this.isCustomer?WtfGlobal.getLocaleText("acc.mp.selectVenFirst"):WtfGlobal.getLocaleText("acc.mp.selectCustFirst")):this.isCustomer?WtfGlobal.getLocaleText("acc.mp.selectCustFirst"):WtfGlobal.getLocaleText("acc.mp.selectVenFirst")], 2);// 'Select Customer/Vendor first'
        } 
    },
   
    setTaxAmountWithotExchangeRate:function(v,m,rec){
        var taxamount= v;
        if(rec.data.prtaxid==null || rec.data.prtaxid == undefined || rec.data.prtaxid == ""){
            taxamount = 0;
        }
        
        taxamount = WtfGlobal.conventInDecimalWithoutSymbol(taxamount);
        
        rec.set("taxamount",taxamount);
//        rec.commit();
        return taxamount;
    },
    
    calAmountWithoutExchangeRate:function(v,m,rec){
        var origionalAmount = 0;
        if(rec.data.dramount !='' || rec.data.dramount != undefined){
            origionalAmount = rec.data.dramount;
        }
        
        var taxamount = 0;
        if(rec.data.taxamount != null || rec.data.taxamount != undefined || rec.data.taxamount != ""){
            taxamount= rec.data.taxamount;
        }
            origionalAmount+=taxamount;
        
        origionalAmount = WtfGlobal.conventInDecimalWithoutSymbol(origionalAmount);
        
        rec.set("amountwithtax",origionalAmount);
//        rec.commit();

        return origionalAmount;
    },
    
    setTaxAmountAfterSelection:function(rec) {
    var origionalAmount="";
    var val="";
    var taxamount="";
        
    var taxpercent=0;
    var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
    if(index>=0){
        var taxrec=this.taxStore.getAt(index);
        if (taxrec.data.prtaxid != "None") {
            taxpercent=taxrec.data.percent;
        }
    }
    /*
     *If includingGst check is enable then calculate Tax Amount 
     */
    if(this.includingGST.getValue()){
        origionalAmount = rec.data.dramount;
        val=origionalAmount;
        taxamount= getRoundedAmountValue((val*taxpercent)/(taxpercent+100)); 
    }else{
        origionalAmount = rec.data.dramount;
        val=origionalAmount;
        taxamount= getRoundedAmountValue(val*taxpercent/100); 
    }
    return taxamount;
},
    
    createInvoiceGrid:function(){
        
         this.InvGridRec = Wtf.data.Record.create ([
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
                {name:'jeDate', type:'date'},
                {name:'duedate', type:'date'},
                {name:'shipdate', type:'date'},
                {name:'personname'},
                {name:'personemail'},
                {name:'linkingdate', type:'date'},
                {name:'personid'},
                {name:'shipping'},
                {name:'othercharges'},
                {name:'amount'},
                {name:'invamount', defaultValue:0},
                {name:'amountdue'},
                {name:'termdays'},
                {name:'termname'},
                {name:'incash'},
                {name:'taxamount'},
                {name:'taxid'},
                {name:'orderamountwithTax'},
//                {name:'taxincluded',type:'boolean'},
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
                {name:'amountDueOriginal',defValue: 0},
                {name: 'exchangeratefortransaction', defValue: 1},
                {name: 'currencysymbolpayment'},
                {name: 'currencyidtransaction'}, //GR currency id
                {name: 'currencyidpayment'},
                {name:'billingAddress'},
                {name:'billingCountry'},
                {name:'billingState'},
                {name:'billingPostal'},
                {name:'billingEmail'},
                {name:'billingFax'},
                {name:'billingMobile'},
                {name:'billingPhone'},
                {name:'billingContactPerson'},
                {name:'billingRecipientName'},
                {name:'billingContactPersonNumber'},
                {name:'billingContactPersonDesignation'},
                {name:'billingWebsite'},
                {name:'billingCounty'},
                {name:'billingCity'},
                {name:'billingAddressType'},
                {name:'shippingAddress'},
                {name:'shippingCountry'},
                {name:'shippingState'},
                {name:'shippingCounty'},
                {name:'shippingCity'},
                {name:'shippingEmail'},
                {name:'shippingFax'},
                {name:'shippingMobile'},
                {name:'shippingPhone'},
                {name:'shippingPostal'},
                {name:'shippingContactPersonNumber'},
                {name:'shippingContactPersonDesignation'},
                {name:'shippingWebsite'},
                {name:'shippingContactPerson'},
                {name:'shippingRecipientName'},
                {name:'shippingRoute'},
                {name:'shippingAddressType'},
                {name:'vendcustShippingAddress'},
                {name:'vendcustShippingCountry'},
                {name:'vendcustShippingState'},
                {name:'vendcustShippingCounty'},
                {name:'vendcustShippingCity'},
                {name:'vendcustShippingEmail'},
                {name:'vendcustShippingFax'},
                {name:'vendcustShippingMobile'},
                {name:'vendcustShippingPhone'},
                {name:'vendcustShippingPostal'},
                {name:'vendcustShippingContactPersonNumber'},
                {name:'vendcustShippingContactPersonDesignation'},
                {name:'vendcustShippingWebsite'},
                {name:'vendcustShippingContactPerson'},
                {name:'vendcustShippingRecipientName'},
                {name:'vendcustShippingAddressType'},
                /**
                 * If Show Vendor Address in purchase side document and India country 
                 * then this Fields used to store Vendor Billing Address
                 * If CN/ DN against invoice then if address from invoice 
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
                {name: 'vendorbillingCityForINDIA'},
                {name:'isOpeningBalanceTransaction'},
                {name:'typeOfFigure',defValue:1},
                {name:'typeFigure',defValue:0},
                {name:'supplierinvoiceno'}
                
            ]);

            if(this.isCN)
                this.InvStoreUrl = "ACC" + (this.isCustBill?"InvoiceCMN/getBillingInvoices":"InvoiceCMN/getInvoices") + ".do";
            else
                this.InvStoreUrl = "ACC" + (this.isCustBill?"GoodsReceiptCMN/getBillingGoodsReceipts":"GoodsReceiptCMN/getGoodsReceipts") + ".do";

            if(this.isCN)
                this.InvGridStoreUrl = "ACCInvoiceCMN/getCreditNoteInvoices.do";
            else
                this.InvGridStoreUrl = "ACCGoodsReceiptCMN/getDebitNoteGR.do";

       this.InvGridStore = new Wtf.data.Store({
            url:this.InvGridStoreUrl,
            baseParams:{
                deleted:false,
                nondeleted:true,
                cashonly:false,
                creditonly:false,
//                onlyAmountDue:true,
//                notlinkCNFromInvoiceFlag:false,// opening balance invoice will come in invoice combo in case of linking invoice with CN.
                accid:this.accid,
                currencyfilterfortrans : this.currencyid,
                isReceipt:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.InvGridRec)
        });

       this.InvGridComboStore = new Wtf.data.Store({
            url:this.InvStoreUrl,
            baseParams:{
                deleted:false,
                nondeleted:true,
                cashonly:false,
                creditonly:false
//                onlyAmountDue:false,
//                notlinkCNFromInvoiceFlag:false// opening balance invoice will come in invoice combo in case of linking invoice with CN.
//                accid:this.accid,
//                currencyfilterfortrans : this.currencyid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.InvGridRec)
        });
        
        
        this.InvGridComboStore.on('beforeload',function(){
//        if(!this.isEdit){     // Commented for ERP-3689
             this.InvGridComboStore.baseParams.onlyAmountDue = true;
             if(this.linkingDate.getValue()!=''){
                this.InvGridComboStore.baseParams.upperLimitDate = WtfGlobal.convertToGenericDate(this.linkingDate.getValue());
             }else {
                 if(this.isEdit){
                     this.InvGridComboStore.baseParams.upperLimitDate = WtfGlobal.convertToGenericDate(this.record.data.date);
                 } else {
                    this.InvGridComboStore.baseParams.upperLimitDate = WtfGlobal.convertToGenericDate(this.creationDate.getValue());
                 }   
             }   
//         }
            this.InvGridComboStore.baseParams.includeFixedAssetInvoicesFlag=true;
        },this);
        
        this.InvGridComboStore.on('load',function(){
            if(this.isEdit && this.record != null){
                this.InvGridStore.load({
                    url:'ACCCreditNote/getCreditNoteInvoiceRows.do',
                    params:{
                        noteId:(this.record.data && this.record.data.noteid)?this.record.data.noteid:""
                    }
                });
            }
        },this);
        
        this.Invoices= new Wtf.form.ExtFnComboBox({
//                fieldLabel:(this.isCN?"Customer Invoice":"Vendor Invoice")+"*"+InvoiceHelp,
                hiddenName:"linkInvoice",
                minChars:1,
                listWidth :300,
    //            labelWidth:200,
                store: this.InvGridComboStore,
                valueField:'billid',
                disabledClass:"newtripcmbss",
                disabled:this.readOnly,
                displayField:'billno',
                allowBlank:false,
                hirarchical:true,
                extraFields:[],
    //            emptyText:this.isCN?'Select a Customer Invoice':'Select a Vendor Invoice',
                mode: 'local',
                typeAhead: true,
                typeAheadDelay:30000,
                forceSelection: true,
                selectOnFocus:true,
                triggerAction:'all',
                //value:'1',
                scope:this
             });
        
        this.typesOfFigureStore = new Wtf.data.SimpleStore({
        fields: [{
            name: 'id'
        }, {
            name: 'name'
        }],
        data: [[1,'Flat'],[2,'Percentage']]
        });
        this.TypesOfFigure = new Wtf.form.ComboBox({
                hiddenName:"typesOfFigure",
                store: this.typesOfFigureStore,
                valueField:'id',
                disabledClass:"newtripcmbss",
                disabled:this.readOnly,
                displayField:'name',
                allowBlank:false,
                mode: 'local',
                typeAhead: true,
                forceSelection: true,
                selectOnFocus:true,
                triggerAction:'all',
                scope:this
             });
//        this.InvGridComboStore.load();
        this.InvGridStore.on('load',this.addInvoiceGridRec,this);
        
        var colArr =[];
        colArr.push({
            header:this.isCN?WtfGlobal.getLocaleText("acc.field.CustomerInvoice"):WtfGlobal.getLocaleText("acc.agedPay.venInv"),//config.isReceipt? WtfGlobal.getLocaleText("acc.rem.217") : WtfGlobal.getLocaleText("acc.rem.30"),  //"Debit Account",
            dataIndex:'billid',
            width:150,
//            hidden : this.isAdvPayment,
            editor:( this.readOnly || this.isLinkedTransaction )?"": this.Invoices,
            renderer:Wtf.comboBoxRenderer(this.Invoices)
        },{
            
            header:WtfGlobal.getLocaleText("acc.invoice.SupplierInvoiceNumber"),
            dataIndex:'supplierinvoiceno',
            width:150,
            align:'right'

        },this.detailLinkingDate={
            header:WtfGlobal.getLocaleText("acc.linkingDate.date"),  //Linking Date
            dataIndex:'linkingdate',
            width:150,
            align:'center',
            hidden:this.cntype == 1?false:true,
            disabled:(this.isEdit||this.readOnly || this.isLinkedTransaction ),
            renderer: function(v, m, rec) {
                if (!v){
                    return v;
                }
                if (rec != undefined && rec.data.deleted){
                    v = '<del>' + v.format(WtfGlobal.getOnlyDateFormat()) + '</del>';
                }else{
                    v = v.format(WtfGlobal.getOnlyDateFormat());
                }

                v = '<div class="datecls" wtf:qtip="' + v + '">' + v + '</div>';
                return v;
            },
            editor:new Wtf.form.DateField({
                scope:this,
                allowBlank:(this.cntype != 1),
                maxLength:250,
                format:WtfGlobal.getOnlyDateFormat(),
                xtype:'datefield',
                disabled:(this.isEdit||this.readOnly || this.isLinkedTransaction ),
//                value:(new Date(Wtf.account.companyAccountPref.activeDateRangeToDate).getTime() < new Date().getTime())?new Date(Wtf.account.companyAccountPref.activeDateRangeToDate):new Date(),   
//                maxValue:Wtf.account.companyAccountPref.activeDateRangeToDate!=""&& Wtf.account.companyAccountPref.activeDateRangeToDate!=null?new Date(Wtf.account.companyAccountPref.activeDateRangeToDate):""
                listeners:{
                    'change':{
                        fn:this.GridcheckMaxDate,
                        scope:this
                    }
                }
            } )
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.gridInvAmt"),//config.isReceipt? WtfGlobal.getLocaleText("acc.rem.218") : WtfGlobal.getLocaleText("acc.rem.31"),  //"Debit Amount",
            dataIndex:'amount',
            width:100,
            align:'right',
//            renderer:WtfGlobal.conventInDecimalWithoutSymbol
            renderer:WtfGlobal.amountWithoutCurrencyRender
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.taxAmt"),//config.isReceipt? WtfGlobal.getLocaleText("acc.rem.218") : WtfGlobal.getLocaleText("acc.rem.31"),  //"Debit Amount",
            dataIndex:'taxamount',
            width:100,
            hidden:(Wtf.account.companyAccountPref.countryid == '137' && !Wtf.account.companyAccountPref.enableGST),// hide if company is malaysian and GST is not enabled for it
            align:'right',
//            renderer:WtfGlobal.conventInDecimalWithoutSymbol
            renderer: WtfGlobal.amountWithoutCurrencyRender
        },{
            header:WtfGlobal.getLocaleText("acc.field.InvoiceAmountDue"),//config.isReceipt? WtfGlobal.getLocaleText("acc.rem.218") : WtfGlobal.getLocaleText("acc.rem.31"),  //"Debit Amount",
            dataIndex:'amountDueOriginal',
            width:100,
            align:'right',
//            renderer:WtfGlobal.conventInDecimalWithoutSymbol
            renderer:WtfGlobal.withoutRateCurrencySymbolTransaction
        },
            {
            header:WtfGlobal.getLocaleText("acc.setupWizard.curEx"), 
            dataIndex:'exchangeratefortransaction',
            hidelabel:false,
            hidden: false,
            renderer:this.conversionFactorRenderer,
            editor: ( this.readOnly || this.isLinkedTransaction )?"":this.exchangeratefortransaction=new Wtf.form.NumberField({ //    ERP-18000
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
        },
        {
            header: WtfGlobal.getLocaleText("acc.invoiceList.amtDue"),
            dataIndex: 'amountdue',
            align:'right',
//            renderer:WtfGlobal.withoutRateCurrencySymbol
                renderer: function (value, m, rec) {
                    var symbol = ((rec == undefined || rec.data.currencysymbolpayment == null || rec.data['currencysymbolpayment'] == undefined || rec.data['currencysymbolpayment'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbolpayment']);
                    var v = parseFloat(value);
                    if (isNaN(v))
                        return value;
                    v = WtfGlobal.conventInDecimal(v, symbol)
                    return '<div class="currency">' + v + '</div>';
                }
        }, {
            header:WtfGlobal.getLocaleText("acc.product.gridType"),
            dataIndex:'typeOfFigure',
            width:150,
            editor:( this.readOnly || this.isLinkedTransaction )?"": this.TypesOfFigure,
            renderer:Wtf.comboBoxRenderer(this.TypesOfFigure)
        },
        {
            header:WtfGlobal.getLocaleText("acc.cndnEnterAmountOrPercentage"),
            dataIndex:"typeFigure",
            width:100,
            align:'right',
            editor:( this.readOnly || this.isLinkedTransaction )?"":new Wtf.form.NumberField({
                allowBlank: false,
                allowNegative:false,
                decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
            }),
            renderer:WtfGlobal.amountWithoutCurrencyRender
        },{
            header:WtfGlobal.getLocaleText("acc.dnList.gridAmt"),                                   //WtfGlobal.getLocaleText("acc.invReport.desc"),  //"Description",
            dataIndex:"invamount",
            width:100,
            align:'right',
            renderer:WtfGlobal.amountWithoutCurrencyRender
        });
        
        var invColModelArray = GlobalColumnModelForReports[this.invModuleId];
        colArr = WtfGlobal.appendCustomColumn(colArr, invColModelArray, undefined, undefined, false);
        colArr.push({
            header: WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
            dataIndex: "delete",
            width: 50,
            align: 'right',
            hidden: (this.readOnly || this.isLinkedTransaction),
            renderer: function () {
                return "<div class='pwnd delete-gridrow' > </div>";
            }
        });
        this.InvGridCM= new Wtf.grid.ColumnModel(colArr);
        WtfGlobal.updateStoreConfig(invColModelArray, this.InvGridStore);
        WtfGlobal.updateStoreConfig(invColModelArray, this.InvGridComboStore);
        
        this.InvGrid = new Wtf.grid.EditorGridPanel({
            plugins:this.gridPlugins,
            clicksToEdit:1,
            style:'padding:10px;',
            height:175,
            disabledClass:"newtripcmbss",
            readOnly:this.readOnly,
            store: this.InvGridStore,
            cm: this.InvGridCM,
            border : true,
            loadMask : true,
            disabled:!this.isEdit?true:false,
            viewConfig: {
                forceFit:false,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.InvGrid.on('rowclick',this.processInvGridRow,this);
        this.InvGrid.on('afteredit',this.checkInvGridrecord,this);
        this.InvGrid.on('beforeedit',this.beforeGridEdit,this);
        this.InvGrid.on('validateedit',this.checkInvrecord,this);
        
      },
      
      
      checkInvrecord:function(obj){
        
    var size=this.InvGridStore.getCount()-1;
       
    if(obj.field == 'billid'){ //ERP-10554
        //we cannot copy credit note or debit note
            for(var i=0;i<=size;i++){//written to check the duplicate invoiceno
                var singlegridrec=this.InvGridStore.getAt(i);
                if(singlegridrec.data['billid']==obj.value){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.warningofsameinvoice")], 2);
                    obj.cancel=true;
                    return false;
                }
            }
    }
},
      
      addInvoiceGridRec:function(e){ 
        var size=this.InvGridStore.getCount();
        
        var rec= this.InvGridRec;
        if(size>0){
            var lastRec=this.InvGridStore.getAt(size-1);
            if(lastRec.get('billid') == ''){
                lastRec.set('billid', '');
                return;
            }
//            if(lastRec.get('dramount') == ''){
//                lastRec.set('dramount', '');
//                return;
//            }
        } 
        rec = new rec({});
        rec.beginEdit();
        var items =this.InvGridStore.fields.items;
        /* 
         * WtfGlobal.updateStoreConfig() containing "store.fields.length" is not incremented.
         * so for loop iterated on items.length insted of fields.length
         */
        for(var x=0;x< items.length;x++){
            var value="";
            rec.set(items[x].name, value);
        }
        rec.endEdit();
        rec.commit();
        this.InvGridStore.add(rec);
    },
    
    processInvGridRow:function(grid,rowindex,e){        
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                var record = store.getAt(rowindex);
               
                store.remove(store.getAt(rowindex));
                if(rowindex==total-1){
                    this.addInvoiceGridRec();
                }
                /*calling Related method: It used for handling linking address in new case*/
                if(!(this.isCopy || this.isEdit)){
                    this.getLinkingInvoiceAddressInfo();
                                    }
            }, this);
        }
    },
    conversionFactorRenderer:function(value,meta,record) {
        var currencysymbol=((record==undefined||record.data.currencysymbol==null||record.data['currencysymbol']==undefined||record.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():record.data['currencysymbol']);
        var currencysymboltransaction=((record==undefined||record.data.currencysymbolpayment==null||record.data['currencysymbolpayment']==undefined||record.data['currencysymbolpayment']=="")?currencysymbol:record.data['currencysymbolpayment']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
        return "1 "+ currencysymbol +" = " +value+" "+currencysymboltransaction;
    }, 
    checkInvGridrecord:function(obj){
    var rec=obj.record;
    var msg = "";
    if (obj.field == 'exchangeratefortransaction') {
        var amountDueOriginal = 0;
        var exchangeRate = 0;
        amountDueOriginal = parseFloat(rec.data.amountDueOriginal);
        exchangeRate = rec.data.exchangeratefortransaction;
        if (exchangeRate != '')
            obj.record.set("amountdue", getRoundedAmountValue(amountDueOriginal * exchangeRate));
        obj.record.set("enteramount", 0);
    }
    if(obj.field=="billid"){
            
        var invoiceRec = "";
        var invoiceComboIndex = WtfGlobal.searchRecordIndex(this.InvGridComboStore, obj.value, 'billid');
        if(invoiceComboIndex>=0){
            invoiceRec = this.InvGridComboStore.getAt(invoiceComboIndex);
            rec.set("currencysymbol",invoiceRec.data.currencysymbol);
            rec.set("supplierinvoiceno",invoiceRec.data.supplierinvoiceno);
            rec.set("currencysymbolpayment",invoiceRec.data.currencysymbolpayment);
            rec.set("amount",parseFloat(getRoundedAmountValue(invoiceRec.data.amount)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1);
            rec.set("taxamount",parseFloat(getRoundedAmountValue(invoiceRec.data.taxamount)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1);
            rec.set("amountDueOriginal",parseFloat(getRoundedAmountValue(invoiceRec.data.amountDueOriginal)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)*1);
            rec.set("invamount",parseFloat(0).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
            rec.set("exchangeratefortransaction",parseFloat((invoiceRec.data.exchangeratefortransaction)));
            rec.set("amountdue",parseFloat(getRoundedAmountValue(invoiceRec.data.amountdue)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
            rec.set("date",invoiceRec.data.date);
            rec.set("duedate",invoiceRec.data.duedate);
            rec.set("duedate",invoiceRec.data.duedate);
            rec.set("isOpeningBalanceTransaction",invoiceRec.data.isOpeningBalanceTransaction);
            rec.set("typeOfFigure",1);
            rec.set("typeFigure",parseFloat(0).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
            rec.set("jeDate",invoiceRec.data.jeDate);
            
            if (!(this.isCopy || this.isEdit || this.readOnly)) {
                rec.set('linkingdate', this.linkingDate.getValue());
            } else {
                rec.set("linkingdate", invoiceRec.data.linkingdate);
            }
            
            /*
             * To set Custom fields/dimensions Data to InvoiceRec.
             */
            this.setCustomDataToInvoiceRec(rec, invoiceRec);
        }
        this.addInvoiceGridRec();

        /*calling Related method: It used for handling linking address in new case*/
        if(!(this.isCopy || this.isEdit)){
            this.getLinkingInvoiceAddressInfo();
        }
            
    }else if(obj.field=="invamount"){
            
        if(rec.data.billid == ''){
            rec.set("invamount",parseFloat(0).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
            return;
        }
            
        if(rec.data.amountdue < obj.value ){
            msg = this.isCN?WtfGlobal.getLocaleText("acc.field.CustomerInvoice"):WtfGlobal.getLocaleText("acc.agedPay.venInv");
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Amountenteredcannotbegreaterthan")+msg+" "+WtfGlobal.getLocaleText("acc.field.amountdue")],2);
            obj.cancel=true;
            rec.set("invamount",parseFloat(0).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
        }else{        
            rec.set("invamount", obj.value);
        }
    } else if(obj.field=="typeFigure"){
        if(rec.data['typeFigure']==''){
            rec.set("invamount", parseFloat(getRoundedAmountValue(0)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
        } else {            
            if(rec.data.typeOfFigure == 1){        
                if(rec.data['typeFigure']>rec.data['amountdue']){
                    // Entered amount grater than invoice amount due
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Amountenteredcannotbegreaterthan")+WtfGlobal.getLocaleText("acc.customerList.gridAmountDue")+" "+WtfGlobal.getLocaleText("acc.mp.ofInvoice")], 2)
                    rec.set('typeFigure',rec.data['amountdue']);
                } 
                rec.set("invamount", parseFloat(getRoundedAmountValue(rec.data['typeFigure'])).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));        
            } else {
                if(rec.data['typeFigure']>100){
                    // Percentage figure greater than 100
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.cnd.percentageCanNotBeGreaterThanHundred")], 2)
                    rec.set('typeFigure',100);
                }
                var amtDue = rec.data['amountdue'];
                var percentage = rec.data['typeFigure'];
                var amountToSet = (amtDue*percentage)/100;
                amountToSet = parseFloat(getRoundedAmountValue(amountToSet)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
                rec.set("invamount", amountToSet);
            }
            
        }       
    } else if(obj.field=="typeOfFigure"){
        rec.set("invamount", 0.0);
        rec.set("typeFigure", 0.0);
    }
},
    //To set Custom fields/dimensions Data to InvoiceRec.
    setCustomDataToInvoiceRec: function (rec, invoiceRec) {
        var colModelArray = GlobalColumnModelForReports[this.invModuleId];
        for (var columncount = 0; columncount < colModelArray.length; columncount++) {
            var customdatarec = colModelArray[columncount];
            if (invoiceRec.data[customdatarec.fieldname]) {
                rec.set(customdatarec.fieldname, invoiceRec.data[customdatarec.fieldname]);
            }
        }
    },
    
    getLinkingInvoiceAddressInfo:function(){
        var selectedRecordCount=this.InvGrid.store.getCount()-1;//subtraction one because of blank row
        if(selectedRecordCount==1){//When only one record is selected then we take linking address otherwise address came from master 
            var gridRecord=this.InvGrid.store.getAt(0);
            if(gridRecord){
                var billid=gridRecord.data.billid;//Selected invoice id
                this.linkRecord = WtfGlobal.searchRecord(this.InvGridComboStore, billid, 'billid');
                this.singleLink=true;
            }
        } else{
            this.linkRecord=null;
            this.singleLink=false;
        }
    },
    
    beforeGridEdit: function (obj) {
        var rec = obj.record;
        if (this.isCreatedFromReturnForm) {
            if (obj.field != 'invamount' && obj.field != 'typeFigure') {
                obj.cancel = true;
            }
        }
        if (obj.field == 'exchangeratefortransaction') {
            if (!this.isExchangeRateEditableForSelectedDocumentType(obj))
                obj.cancel = true;
        } else if(obj.field=="typeFigure"){
            // restrict user from entering amount before selecting the calculation criteria
            if(rec.data.typeOfFigure == null || rec.data.typeOfFigure == undefined || rec.data.typeOfFigure == ''){
                obj.cancel=true;
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.cndn.enterTypeOfFigure")],2);
                return;
            } 
        } else if(obj.field=='typeOfFigure'){
            // Restrict user from selecting the calculation criteria before selecting the invoice
            if(rec.data.billid == null || rec.data.billid == undefined || rec.data.billid == ''){
                obj.cancel=true;
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.PleaseselectanInvoice")],2);
                return;
            }
        }else{
            //Disable invoice grid editing for custom fields/dimensions.
            var index = WtfGlobal.getColIndexByDataIndex(this.InvGridCM, obj.field);
            if (index != -1 && this.InvGridCM.config[index].iscustomcolumn != undefined && this.InvGridCM.config[index].iscustomcolumn) {
                return false;
            }
        }
    },
    isExchangeRateEditableForSelectedDocumentType: function (e) {
        if (e.record.data.currencysymbol == e.record.data.currencysymbolpayment) {
            return false;
        } else {
            return true;
        }
    },
    
    createForm:function(){  
      this.tagsFieldset = new Wtf.account.CreateCustomFields({
        border: false,
        compId:"TypeForm"+this.id,
        autoHeight: true,
        autoWidth:true,
        parentcompId:this.id,
        moduleid:this.moduleid,
        disabledClass:"newtripcmbss",
        isEdit: this.isEdit,
        record: this.record,
        isViewMode:(this.readOnly || this.isEditToApprove)
    });
        
    this.sequenceFormatStoreRec = new Wtf.data.Record.create([
        {name: 'id'},
        {name: 'value'},
        {name: 'oldflag'}
    ]);
    
    this.sequenceFormatStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"
        },this.sequenceFormatStoreRec),
        //        url: Wtf.req.account +'CompanyManager.jsp',
        url : "ACCCompanyPref/getSequenceFormatStore.do",
        baseParams:{
            mode:this.modeName,
            isEdit:this.isEdit
        }
    });
    
    this.sequenceFormatStore.on('load',this.setNextNumber,this);
    this.sequenceFormatStore.load();
    
    this.sequenceFormatCombobox = new Wtf.form.ComboBox({
        triggerAction:'all',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
        valueField:'id',
        displayField:'value',
        id:"sequenceformat"+this.heplmodeid+this.id,
        store:this.sequenceFormatStore,
        disabled:((this.isEdit&&!this.isCopy) ||this.readOnly),  
        width:240,
       // anchor:'90%',
        typeAhead: true,
        allowBlank:false,
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
    
    this.no=new Wtf.form.TextField({
        id:"cndnNumber"+this.heplmodeid+this.id,
        fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.accPref.autoCN"):WtfGlobal.getLocaleText("acc.accPref.autoDN")) +" "+ WtfGlobal.getLocaleText("acc.cn.9") + "*",  //this.noteType+' Note No*',
        name: 'number',
        scope:this,
        maxLength:45,
        disabled:(this.isEdit && !this.isCopy)||this.readOnly,
        width:240,
        //anchor:'90%',
        allowBlank:false
    });
    
    if(this.isEdit && !this.isCopy){
        this.no.setValue((this.record.data && this.record.data.noteno)?this.record.data.noteno:"");
    }
    
        this.customerStore = new Wtf.data.Store({
            //    url:Wtf.req.account+'CustomerManager.jsp',
            url:"ACCCustomer/getCustomersForCombo.do",
            baseParams:{
                mode:2,
                group:10,
                deleted:false,
                nondeleted:true,
                common:'1'
            },
            reader: new  Wtf.data.KwlJsonReader({
                root: "data",
                autoLoad:false
            },Wtf.personRec)
        });
    
        this.vendorStore = new Wtf.data.Store({
            //    url:Wtf.req.account+'VendorManager.jsp',
            url:"ACCVendor/getVendorsForCombo.do",
            baseParams:{
                mode:2,
                group:13,
                deleted:false,
                nondeleted:true,
                common:'1'
            },
            reader: new  Wtf.data.KwlJsonReader({
                root: "data",
                autoLoad:false
            },Wtf.personRec)
        });
        
     var showOneTime=false;
     if((this.isCN && this.cntype == 4 ) || (!this.isCN && (this.cntype == 1 || this.cntype == 2 )) || this.isEdit){
          showOneTime=true;
     }
     this.ShowOnlyOneTime= new Wtf.form.Checkbox({
        name:'ShowOnlyOneTime',
        fieldLabel:WtfGlobal.getLocaleText("acc.cust.ShowOnlyOneTime"),
        id:'ShowOnlyOneTime'+this.heplmodeid+this.id,
        checked:false,
        hideLabel:showOneTime, // Show only in case Credit note Against Customer and otherwise and Debit Note Against Customer
        hidden:showOneTime ,
        cls : 'custcheckbox',
        width: 10,
        disabled:this.readOnly
    });  
      this.ShowOnlyOneTime.on('check',function(obj,isChecked){
                this.name.reset();
                this.customerStore.load();
    },this);     
        
      if(this.custVenOptimizedFlag){
            this.name=new  Wtf.form.ExtFnComboBox({
            fieldLabel:(this.customerFlag?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven"))+"*",  //this.businessPerson +'*',
            id:"selectCustomer"+this.helpmodeid+this.id,
            hiddenName:'accid',
            store:this.customerFlag?this.customerStore:this.vendorStore,
            valueField:'accid',
            minChars:1,
            extraComparisionField:'acccode',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth : Wtf.account.companyAccountPref.accountsWithCode?350:240,
            mode: 'remote',
            typeAheadDelay:30000,
            isVendor:!(this.customerFlag),
            isCustomer:this.customerFlag,
            scope:this,
            allowBlank:false,
            width:240,
            displayField:'accname',
            disabled:(this.isEdit && !this.isCopy)||this.readOnly,
            emptyText:this.customerFlag?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven"),  //'Please Select a '+this.businessPerson+'...',
            forceSelection: true,
            hirarchical:true,
            ctCls : 'optimizedclass',
            hideTrigger:true
        });
    }else{
        this.name=new  Wtf.form.ExtFnComboBox({
            fieldLabel:(this.customerFlag?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven"))+"*",  //this.businessPerson +'*',
            id:"selectCustomer"+this.helpmodeid+this.id,
            hiddenName:'accid',
            store:this.customerFlag?this.customerStore:this.vendorStore,
            valueField:'accid',
            minChars:1,
            extraComparisionField:'acccode',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            listWidth : Wtf.account.companyAccountPref.accountsWithCode?350:240,
            mode: 'local',
            typeAheadDelay:30000,
            isVendor:!(this.customerFlag),
            isCustomer:this.customerFlag,
            scope:this,
            allowBlank:false,
            //anchor:'90%',
            width:240,
            displayField:'accname',
            disabled:(this.isEdit && !this.isCopy)||this.readOnly,
    //            allowBlank:false, //Checked at a time of saving
            emptyText:this.customerFlag?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven"),  //'Please Select a '+this.businessPerson+'...',
            forceSelection: true,
            hirarchical:true
    //            addNewFn:this.callCustomer.createDelegate(this,[false, null,'custwin',this.isCN])
        });
    }
    var isEditORisCopy=(this.isEdit !=undefined ?this.isEdit:false); 
    this.customerStore.on('beforeload', function(s,o){
            WtfGlobal.setAjaxTimeOut();
            if(!o.params)o.params={};
            var currentBaseParams = this.customerStore.baseParams;
            if(isEditORisCopy){
                currentBaseParams.isPermOrOnetime="";// Empty to load all customers
            }else{
                if(this.ShowOnlyOneTime != undefined && this.ShowOnlyOneTime.getValue() == true){
                    currentBaseParams.isPermOrOnetime=true; // True to load one time customers
                }else{
                    currentBaseParams.isPermOrOnetime=false; // False to load Permanent customers
                }
            }
            this.customerStore.baseParams=currentBaseParams;
        }, this);
//    this.setTrNoteNumber(false);
    
        this.vendorStore.on('beforeload', function(s,o){
            WtfGlobal.setAjaxTimeOut();
        }, this);
        if(this.customerFlag){
            this.customerStore.on('load',function(){
                WtfGlobal.resetAjaxTimeOut();
                if(this.isEdit){
                    this.name.setValue((this.record.data && this.record.data.personid)?this.record.data.personid:"");
                }
            },this);
            
            if(!this.custVenOptimizedFlag)
            this.customerStore.load();
        }else{
            this.vendorStore.on('load',function(){
                WtfGlobal.resetAjaxTimeOut();
                if(this.isEdit){
                    this.name.setValue((this.record.data && this.record.data.personid)?this.record.data.personid:"");
                }
            },this);
            
            if(!this.custVenOptimizedFlag)
            this.vendorStore.load();
        }
    
//    this.customerFlag?chkcustaccload():chkvenaccload();
    
    this.creationDate=new Wtf.form.DateField({
        xtype:'datefield',
        name:'creationdate',
        allowBlank:false,
       // anchor:'90%',
        width:240,
        id:"creationdate"+this.heplmodeid+this.id,
        format:WtfGlobal.getOnlyDateFormat(),
        value:new Date(),
        fieldLabel:WtfGlobal.getLocaleText("acc.customer.date") + "*",  //'Creation Date*'
        disabled:( this.readOnly || this.isLinkedTransaction )
    });  
    this.creationDate.on('change',this.onDateChange,this);  
    if(this.isEdit){
        this.creationDate.setValue((this.record.data && this.record.data.date)?this.record.data.date:"");
    }
    
        
    
        this.currencyStore = new Wtf.data.Store({
            //    url:Wtf.req.base+"CompanyManager.jsp",
            url:"ACCCurrency/getCurrencyExchange.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },Wtf.currencyRec),
            baseParams:{
                mode:201,
                common:'1'
            },
            autoLoad:false
        });
    
    this.Currency= new Wtf.form.FnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.customer.currency") /*+ currencyHelp*/,  //'Currency',
        hiddenName:'currencyid',
       // anchor: '90%',
        width:240,
        allowBlank:false,
        id:"currency"+this.heplmodeid+this.id,
        store:this.currencyStore,
        disabled:(this.isEdit && !this.isCopy) ||this.readOnly,
        valueField:'currencyid',
        //   emptyText:'Please select Currency...',
        forceSelection: true,
        displayField:'currencyname',
        scope:this,
        selectOnFocus:true
    });
    
    this.Currency.on('beforeselect', function (combo, record, index) {
        this.currencyBeforeSelect = combo.getValue();
    }, this);
    
    this.Currency.on('select',function(combo, record, index){
        if (combo.getValue() == this.currencyBeforeSelect) {
            return;
        }
        
        if(Wtf.account.companyAccountPref.activateToDateforExchangeRates){
            checkForNearestExchangeRate(this,record,this.creationDate.getValue());
        }
        if (this.Currency.getValue() != "" && this.currencyBeforeSelect != undefined && this.currencyBeforeSelect != '' && !this.accountDetailsGridIsEmpty(this.grid)) {
            Wtf.MessageBox.confirm("Warning", WtfGlobal.getLocaleText("acc.wm.beforechange"), function (btn) {
                if (btn == 'yes') {
                    this.clearGridData();
                } else if (btn == 'no') {
                    combo.setValue(this.currencyBeforeSelect);
                    return;
                }
            }, this);
        } else {
            this.clearGridData();
        }
           this.onCurrencyChange();
    },this);
    
    this.currencyStore.on("load",function(store){
            //set currency while form open in Edit case
         if (this.isEdit && !this.datechanged) {
            this.Currency.setValue((this.record.data && this.record.data.currencyid)?this.record.data.currencyid:"");
                if (this.record.data.externalcurrencyrate != undefined) {
                    this.externalcurrencyrate = this.record.data.externalcurrencyrate;
                    this.updateFormCurrency();
                }
            } else if (!this.isEdit && !this.datechanged && Wtf.account.companyAccountPref.currencyid) {
                this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
            }
            // check wheather exchange rate is set for currency on selected date while retaining exchange rate.
            if (this.exchangeRateInRetainCase || (this.datechanged && this.isEdit)) {
                if (WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid") == null) {
                     callCurrencyExchangeWindow();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleasesetthecurrencyrate") + " " + WtfGlobal.getLocaleText("acc.field.fortheselecteddate") + "<b>" + WtfGlobal.convertToGenericDate(this.val) + "</b>"], 0);
                   
                    this.datechanged = false;
                    this.creationDate.setValue("");
//                    this.Currency.setValue("");
                }
                if (!this.exchangeRateInRetainCase)
                {
                    this.onCurrencyChange();
                }
                this.exchangeRateInRetainCase = false;
                return;
            }
        if(store.getCount()<1){
            callCurrencyExchangeWindow();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.common.currency")],2);
        }  
            //check whether exchange rate is set for currency on selected date
                var recResult = WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid");
                if (this.Currency.getValue() != "" && recResult == null) {
                    callCurrencyExchangeWindow();
                    var str = "";
                    str = WtfGlobal.getLocaleText("acc.field.andpriceof") + " <b>" + str + "</b>";
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleasesetthecurrencyrate") + " " + str + WtfGlobal.getLocaleText("acc.field.fortheselecteddate") + "<b>" + WtfGlobal.convertToGenericDate(this.val) + "</b>"], 0);
                    this.creationDate.setValue("");
                    this.Currency.setValue("");
                }
            // show exchange rate template on date change event after store loading
            if (this.datechanged) {
                this.onCurrencyChange();
                this.datechanged = false;
            }
       
    },this);
    
    this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.creationDate.getValue())}});
    
    this.Amount=new Wtf.form.NumberField({
        name:"amount",
        allowBlank:true,
        readOnly:true,
        fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.totAmt"),  //"Amount*",//in "+WtfGlobal.getCurrencySymbolForForm()+"*",
        id:"totalamount"+this.heplmodeid+this.id,
//        maxLength:15,
        hidden:false,
        hideLabel:false,
        decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
         value:0,
//            disabled:true,
//        emptyText:WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt"), //"enter amount",
        //anchor:'90%',
        width:240,
        disabled:( this.readOnly || this.isLinkedTransaction )
    });
    
    this.name.on('select',function(combo, record, index){
        if (combo.getValue() == this.nameBeforeSelect) { //If same name selected no need to do any action 
            return;
        }
        var showMsg = this.isCustomer ? WtfGlobal.getLocaleText("acc.invoice.customer"):WtfGlobal.getLocaleText("acc.invoice.vendor");
        showMsg = showMsg +" "+ WtfGlobal.getLocaleText("acc.wm.beforecustomervendorchange");
        if(this.name.getValue() != "" && this.nameBeforeSelect!=undefined && this.nameBeforeSelect!='' && !this.accountDetailsGridIsEmpty(this.grid)) {
            Wtf.MessageBox.confirm("Warning", showMsg, function (btn) {
                if (btn == 'yes') {
                    this.onNameChange(combo, record, index);
                } else if (btn == 'no') {
                    combo.setValue(this.nameBeforeSelect);
                    return;
            }
            }, this);
        }else{
            this.onNameChange(combo, record, index);
        }
        if(this.InvGrid){
            this.InvGrid.setDisabled(false);   
        }
    },this);
    this.name.on('beforeselect', function(combo, record, index) {
        this.nameBeforeSelect = combo.getValue();
        return validateSelection(combo, record, index);
    }, this);
//    chkFormCostCenterload();
   this.CostCenter= new Wtf.form.ExtFnComboBox({
        fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.costCenter.tip") +"'>"+ WtfGlobal.getLocaleText("acc.common.costCenter")+"</span>",//"Cost Center",
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
        width:240,
        triggerAction:'all',
        addNewFn:this.addCostCenter,
        scope:this,
        hidden: this.quotation,
        hideLabel: this.quotation,
        disabled:this.readOnly
    });
    if(this.isEdit){
        if(Wtf.StoreMgr.containsKey("FormCostCenter")){
            this.CostCenter.setValue((this.record.data && this.record.data.costcenterid)?this.record.data.costcenterid:"");
        } else {
            Wtf.FormCostCenterStore.on("costcenterloaded", function(){
                this.CostCenter.setValue((this.record.data && this.record.data.costcenterid)?this.record.data.costcenterid:"");
            }, this);
            chkFormCostCenterload();
        }   
    }else{
        chkFormCostCenterload();
    }   
    
    this.Memo=new Wtf.form.TextArea({
        fieldLabel:Wtf.account.companyAccountPref.descriptionType,  //'Memo/Note',
        name: 'memo',
        id:"memo"+this.heplmodeid+this.id,
        height:40,
        //anchor:'90%',
        width:240,
        maxLength:2048,
        readOnly:this.readOnly,
        disabled:this.readOnly,
        qtip:(this.record==undefined)?' ':this.record.data.memo,
            listeners: {
            render: function(c){
               if(c.qtip!="" && c.qtip.trim().length>0){
                    Wtf.QuickTips.register({
                        target: c.getEl(),
                        text: c.qtip
                    });                        
                }
            }
        }
    });
    
    
    var MVATAnnexureCodesRec=new Wtf.data.Record.create([
    {
        name: 'mvatannexurecode'
    },

    {
        name: 'mvatdescription'
    }
    ]);
    this.MVATAnnexureCodesStore=new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },MVATAnnexureCodesRec),
        url:"ACCAccount/getMVATAnnexureCodeForAccount.do",
        baseParams:{
            moduleid:this.moduleid,
            isNoteAlso:true
        }
    });   
    
     
    this.MVATAnnexureCodeCombo = new Wtf.form.ExtFnComboBox(Wtf.applyIf({       
        extraFields:['mvatannexurecode'],
        extraComparisionField:'mvatannexurecode', 
        extraComparisionFieldArray:['mvatdescription','mvatannexurecode'], 
        addNoneRecord: true,
        width:240,
        fieldLabel: this.isCN?WtfGlobal.getLocaleText('Transaction Code as per M-VAT Sales Annexure'):WtfGlobal.getLocaleText('acc.field.mvatannexurePurchasecode'),//Select MVAT Annexure Code
        hiddenName:'mvattransactionno',
        name: 'mvattransactionno',
        store: this.MVATAnnexureCodesStore,
        valueField:'mvatannexurecode',
        displayField:'mvatdescription',
        mode: 'local',
        allowBlank:true,
        //emptyText:'Select MVAT Annexure Code',
        typeAhead: true,                  
        triggerAction:'all',
        hideLabel: Wtf.Countryid != Wtf.Country.INDIA ?  true :( Wtf.Stateid != Wtf.StateName.MAHARASHTRA ? true : (Wtf.account.companyAccountPref.enablevatcst ? false : true)),
        hidden: Wtf.Countryid != Wtf.Country.INDIA ?  true :( Wtf.Stateid != Wtf.StateName.MAHARASHTRA ? true : (Wtf.account.companyAccountPref.enablevatcst ? false : true)),            
        scope:this
    }));
    if((Wtf.Countryid==Wtf.Country.INDIA && Wtf.Stateid==Wtf.StateName.MAHARASHTRA) &&  Wtf.account.companyAccountPref.enablevatcst){
        this.MVATAnnexureCodesStore.on('load',function(){            
            if(this.isEdit && this.record.data && this.record.data.mvattransactionno){
                this.MVATAnnexureCodeCombo.setValue(this.record.data.mvattransactionno);
            }
        },this);
        this.MVATAnnexureCodesStore.load();
    }
    chkSalesPersonload();
    chkAgentload();
    this.includingGST.on('check',function(o,newval,oldval){
        var amoutColumn = this.grid.colModel.config[2];
        var totalAmount=0;
        /*
         * Calulation of tax amount,rate excluding gst and total amount.
         */
        this.grid.getStore().each(function(record) {
            var taxamount="";
            var taxpercent=0;
            var index=this.taxStore.find('prtaxid',record.data.prtaxid);
            if(index>=0){
                var taxrec=this.taxStore.getAt(index);
                taxpercent=taxrec.data.percent;
            }
            if(this.includingGST.getValue()){
                taxamount= getRoundedAmountValue((record.data.dramount*taxpercent)/(taxpercent+100)); 
                record.set("taxamount",taxamount);
                record.set("rateIncludingGst",(record.data.dramount-record.data.taxamount));
                totalAmount+=(record.data.rateIncludingGst+record.data.taxamount);
                this.Amount.setValue(totalAmount);
            }else{
                taxamount= getRoundedAmountValue(record.data.dramount*taxpercent/100); 
                record.set("taxamount",taxamount);
                totalAmount+=(record.data.dramount+record.data.taxamount);
                this.Amount.setValue(totalAmount);
            }
        },this);
        
        var rowRateIncludingGstAmountIndex=this.grid.getColumnModel().findColumnIndex("rateIncludingGst");
        if(this.includingGST.getValue()){
            this.grid.getColumnModel().setHidden(rowRateIncludingGstAmountIndex,!this.includingGST.getValue());
        }else if(!this.grid.getColumnModel().config[rowRateIncludingGstAmountIndex].hidden){
            this.grid.getColumnModel().setHidden(rowRateIncludingGstAmountIndex,!this.includingGST.getValue());
        }
        if((!this.isEdit && !this.readOnly)){
            this.grid.getView().refresh(true);
        }
    },this);
    if(this.isEdit){
        this.Memo.setValue((this.record.data && this.record.data.memo)?this.record.data.memo:"");
        if(this.record.json!=undefined){
            this.includingGST.setValue(this.record.json.includingGST);
        }else{
            this.includingGST.setValue(this.record.data.includingGST);
        }
    }
   
    this.users = new Wtf.form.ExtFnComboBox({
        triggerAction: 'all',
        mode: 'local',
        valueField: 'id',
        displayField: 'name',
        id: this.cntype=="4"?"agent"+this.heplmodeid+this.id:"salesperson"+this.heplmodeid+this.id,
        store: this.cntype=="4"?Wtf.agentStore:Wtf.salesPersonStore,
        addNoneRecord: true,
        width: 240,
        forceSelection: true,
        fieldLabel: this.cntype=="4"?  WtfGlobal.getLocaleText("acc.masterConfig.20"):WtfGlobal.getLocaleText("acc.masterConfig.15"),
        emptyText: this.cntype=="4" ? WtfGlobal.getLocaleText("acc.field.SelectAgent"):WtfGlobal.getLocaleText("acc.field.SelectSalesPerson"),
        name:this.cntype=="4" ?'agent':'salesPerson',
        hiddenName:this.cntype=="4" ?'agent':'salesPerson', 
        hideLabel: !this.isCN,
        hidden: !this.isCN,
        addNewFn: this.addSalesPerson,
        disabled:this.readOnly,
        extraFields:[],//it is required when  ExtFnComboBox component
            activated: this.isCustomer ? true : false
        });



        this.users.store.on('load', function() {
            if (this.isEdit) {
                if (this.cntype == 4 && this.isCN) {
                    this.users.setValue((this.record && this.record.data && this.record.data.agentid) ? this.record.data.agentid : "");
                } else {
                    this.users.setValue((this.record && this.record.data && this.record.data.salesPersonID) ? this.record.data.salesPersonID : "");
                }
            }
        }, this);


        this.users.on('beforeselect', function(combo, record, index) {
            if (this.isCustomer) {
                return validateSelection(combo, record, index);
            } else {
                return true;
            }
        }, this);
        if (this.isEdit) {
            if (this.cntype == 4 && this.isCN) {
                this.users.setValue((this.record && this.record.data && this.record.data.agentid) ? this.record.data.agentid : "");
            } else {
                this.users.setValue((this.record && this.record.data && this.record.data.salesPersonID) ? this.record.data.salesPersonID : "");
            }
        }
        
       this.linkingDate = new Wtf.form.DateField({
        fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.linkingDate.date.tooltip") + "'>" + WtfGlobal.getLocaleText("acc.linkingDate.date") + "</span>",//'Linking Date
        name:'linkingdate',
        format:WtfGlobal.getOnlyDateFormat(),
        hidden:(this.isEdit||this.readOnly)?true:(this.cntype == 1?false:true),
        id:"linkingdate"+this.heplmodeid+this.id,
        hideLabel :(this.isEdit||this.readOnly)?true:(this.cntype == 1?false:true),
        disabled:( this.readOnly || this.isLinkedTransaction ),
        scope:this,
        width:240,
        allowBlank:(this.cntype != 1),
        value:(new Date(Wtf.account.companyAccountPref.activeDateRangeToDate).getTime() < new Date().getTime())?new Date(Wtf.account.companyAccountPref.activeDateRangeToDate):new Date(),   
        maxValue:Wtf.account.companyAccountPref.activeDateRangeToDate!=""&& Wtf.account.companyAccountPref.activeDateRangeToDate!=null?new Date(Wtf.account.companyAccountPref.activeDateRangeToDate):""
    });  
    if(this.isEdit){
        this.linkingDate.setValue((this.record.data && this.record.data.linkingdate)?this.record.data.linkingdate:new Date());
    }
    // Function for checking whether any of the invoice loaded in grid has transaction date less than linking date
    this.linkingDate.on('change',function(field,val,oldval){
        var enteredDate = val;
        var isValid = true;

            var transactiondate = this.creationDate.getValue();
            var linkingdate = this.linkingDate.getValue();
            linkingdate = linkingdate.setHours(0, 0, 0, 0)
            var msg = this.isCN ? WtfGlobal.getLocaleText("acc.field.EnteredDocumentDateCreditNoteDate") : WtfGlobal.getLocaleText("acc.field.EnteredDocumentDateDebitNoteDate");
            //Payment Date is checked
            if (transactiondate.getTime() > linkingdate) {//comparing Credit Note billid with Grids Linked Date

                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.EnteredDocumentDategreaterthanLinkingDate") + " " + msg + ". " + WtfGlobal.getLocaleText("acc.field.changelinkingdate")], 2);
                field.setValue(oldval);
            }   
        
        for(var i=0;i<this.InvGridStore.getCount();i++){
            var rec= this.InvGridStore.getAt(i);
            if(rec.data.date){
                var linkingdate=enteredDate.setHours(0, 0, 0, 0);
                var invoiceDate= new Date(rec.data.date).setHours(0, 0, 0, 0);
                var jeDate= new Date(rec.data.jeDate).setHours(0, 0, 0, 0);
                if((linkingdate < invoiceDate) && (!rec.data.isOpeningBalanceTransaction)){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mp.dateCanNotBeOlder")], 2);
                    field.setValue(oldval);
                    isValid = false;
                    break;
                }
                /**
                 * If isPostingDateCheck is true then checking wether linkingdate is less than jeDate if yes displaying "Date cannot be older than the JE posting date of transactions loaded in grid"
                 */
                if (CompanyPreferenceChecks.isPostingDateCheck()) {
                    if ((linkingdate < jeDate) && (!rec.data.isOpeningBalanceTransaction)) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mp.dateCanNotBeOlderThenJEPosting")], 2);
                        field.setValue(oldval);
                        isValid = false;
                        break;
                    }
                }
            }
        }
        // If changed link date is valid, then it may be future date or valid past date. Here store should be re-loaded
        if(isValid){
            this.InvGridStore.removeAll();
            this.addInvoiceGridRec();
            this.InvGridComboStore.reload();
        }
    },this);

    this.SupplierInvoiceNo = new Wtf.form.TextField({
        fieldLabel: "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.invoice.SupplierInvoiceNumber") +"'>"+ WtfGlobal.getLocaleText("acc.invoice.SupplierInvoiceNo") +"</span>",//Supplier Invoice No - SDP-4510
        name: 'supplierinvoiceno',
        hidden: this.isCN,
        hideLabel: this.isCN,
        disabled: this.readOnly,
        width: 240,
        id:"supplierinvoiceno"+this.heplmodeid+this.id,
        labelWidth: 160,
        maxLength: 50,
        scope: this
    });
    if (this.isEdit) {
        this.SupplierInvoiceNo.setValue((this.record.data && this.record.data.supplierinvoiceno) ? this.record.data.supplierinvoiceno : "");
    }
    
    this.WestFieldArray = [];
        this.WestFieldArray.push(this.sequenceFormatCombobox,this.no,this.ShowOnlyOneTime, this.name, this.Currency,this.linkingDate,this.SupplierInvoiceNo);
        if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
            this.createIndiaRelatedFields();
            if (this.fromLinkCombo !== undefined) {
                this.WestFieldArray.push(this.fromLinkCombo);
            }
            if (this.PO !== undefined) {
                this.WestFieldArray.push(this.PO);
            }
        }
    //Linking Date*:
    
    this.TypeForm=new Wtf.form.FormPanel({
        region:'center',
        border:false,
        id:"TypeForm"+this.id,
        autoHeight:true,
        disabledClass:"newtripcmbss",
        disabled:this.isEditToApprove,
        items:[{
            layout:'form',
            baseCls:'northFormFormat',
            defaults:{
                labelWidth:180
            },
            cls:"visibleDisabled",
            items:[{
                layout:'column',
                border:false,
                items:[{
                    columnWidth:0.50,
                    layout:'form',
                    border:false,
                    items:this.WestFieldArray
                },{
                    columnWidth:0.50,
                    layout:'form',
                    border:false,
                    items:[this.creationDate, this.Amount, this.CostCenter,this.Memo,this.includingGST, this.users,this.MVATAnnexureCodeCombo]  
                }]
              
            }, this.tagsFieldset]
        }]
    });
},
GridcheckMaxDate : function(field,newVal,oldval){
    if(newVal) {
        var enteredDate = newVal;
        var checkdatefalg=true;
        //Debit Note is checked
        var transactiondate=this.creationDate.getValue();
        var msg=this.isCN?WtfGlobal.getLocaleText("acc.field.EnteredDocumentDateCreditNoteDate"):WtfGlobal.getLocaleText("acc.field.EnteredDocumentDateDebitNoteDate");
        if(transactiondate.getTime()>newVal.getTime()){//comparing payment billid with Grids Linked Date
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.EnteredDocumentDategreaterthanLinkingDate")+" "+msg+". "+WtfGlobal.getLocaleText("acc.field.changelinkingdate")],2);
            field.setValue(oldval);
            checkdatefalg=false;
        }       
        
        for(var i=0;i<this.InvGridStore.getCount();i++){
            var rec= this.InvGridStore.getAt(i);
            if(rec.data.date){
                var linkingdate=enteredDate.setHours(0, 0, 0, 0);
                var invoiceDate= new Date(rec.data.date).setHours(0, 0, 0, 0);
                var jeDate= new Date(rec.data.jeDate).setHours(0, 0, 0, 0);
                if((linkingdate < invoiceDate) && (!rec.data.isOpeningBalanceTransaction)){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mp.dateCanNotBeOlder")], 2);
                    field.setValue(oldval);
                    checkdatefalg=false;
                    break;
                }
            }
        }
            /*If isPostingDateCheck is true then checking wether linkingdate is less than jeDate if yes displaying "Date cannot be older than the JE posting date of transactions loaded in grid */
            if (CompanyPreferenceChecks.isPostingDateCheck()) {
                if ((linkingdate < jeDate) && (!rec.data.isOpeningBalanceTransaction)) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mp.dateCanNotBeOlderThenJEPosting")], 2);
                    field.setValue(oldval);
                    checkdatefalg=false;
                }
            } 

            if(this.cntype == 1 && !checkdatefalg) {//For Linking case only
                this.enableSaveButtons();
                return;
            }
    } 
},
    getAddressWindow:function(){
        var addressRecord="";
        var isCopy=false;
        var isEdit=false;
        var custvendorid=this.name.getValue();
        
        if(this.isCopy || this.isEdit ){//edit or copy case
            isCopy=this.isCopy;
            isEdit=this.isEdit;
            addressRecord=this.record; 
        }if(this.singleLink){//new case when single invoice is linked
            addressRecord=this.linkRecord;
        }
        /*
         * To show State as Dropdown in Customer and Vendor Master
         */
        this.stateAsComboFlag = false;
        /*
         For India GST State As Combo in customer and vendor masters if Customer/Vendor type is Export (WPAY),Export (WOPAY),Import
         */
        if (WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied()) {
            this.stateAsComboFlag = true;
            if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
                this.custVenId = Wtf.GSTCUSTVENTYPE.NA;
                if(this.CustVenTypeDefaultMstrID != undefined && this.CustVenTypeDefaultMstrID !=''){
                   this.custVenId = this.CustVenTypeDefaultMstrID;
                }
                this.stateAsComboFlag = (this.custVenId == undefined || !(this.custVenId == Wtf.GSTCUSTVENTYPE.Export || this.custVenId == Wtf.GSTCUSTVENTYPE.ExportWOPAY || this.custVenId == Wtf.GSTCUSTVENTYPE.Import)) ? true : false
            }
        }
        callAddressDetailWindow(addressRecord,isEdit,isCopy,custvendorid,this.currentAddressDetailrec,this.customerFlag,this.readOnly,false,this.singleLink,undefined,WtfGlobal.getModuleId(this),null,null,null,null, this.stateAsComboFlag); 
        Wtf.getCmp('addressDetailWindow').on('update',function(config){
            this.currentAddressDetailrec=config.currentaddress;
            if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
                this.populateGSTDataOnAddressChange();
            }
        },this);
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
            var person = this.name.getValue();
            if(person!='' && this.addressMappingRec==undefined){
                var personIndex = this.name.store.find('accid',person);
                if(personIndex!=-1){
                    var personRec = this.name.store.getAt(personIndex);
                   this.addressMappingRec =  personRec.data && personRec.data.addressMappingRec ? personRec.data.addressMappingRec : "";
                }
            }
            /**
             * IF CN against Vendor & DN against Customer on address Changes send isCustomer flag properly
             */
            var isCustomerInCNDN = undefined;
            if (this.cntype != undefined && this.cntype == 4) {
                if (this.isCustomer == true && this.moduleid == Wtf.Acc_Credit_Note_ModuleId) {
                    isCustomerInCNDN = false;
                } else if (this.isCustomer == false && this.moduleid == Wtf.Acc_Debit_Note_ModuleId) {
                    isCustomerInCNDN = true;
                } 
            }
            obj.mappingRec = this.addressMappingRec;
            obj.isCustomer = isCustomerInCNDN == undefined ? this.isCustomer : isCustomerInCNDN;
            obj.isShipping = this.isShipping;
            obj.stateAsComboFlag = this.stateAsComboFlag;
            var invalid = populateGSTDimensionValues(obj)
            this.applyGSTFieldsBasedOnDate();
        }
    },
setNextNumber:function(){
    if(this.sequenceFormatStore.getCount()>0){            
        if(this.isEdit && !this.isCopy){
            var sequenceformatid=(this.record.data && this.record.data.sequenceformatid)?this.record.data.sequenceformatid:"";
            if(sequenceformatid=="" || sequenceformatid==undefined){
                this.sequenceFormatCombobox.setValue("NA"); 
                this.sequenceFormatCombobox.disable();
                if( this.readOnly){
                    this.no.disable();  
                } else {
                    this.no.enable();   
                }                   
            } else{
                var index=this.sequenceFormatStore.find('id',sequenceformatid);
                if(index!=-1){
                    this.sequenceFormatCombobox.setValue(sequenceformatid);                                               
                }else{//sequence format get deleted then NA is set
                    this.sequenceFormatCombobox.setValue("NA");  
                }                                              
                this.sequenceFormatCombobox.disable();
                this.no.disable(); 
            }
        } else{    
            if(this.isCopy && this.record){
                var sequenceformatid=(this.record.data && this.record.data.sequenceformatid)?this.record.data.sequenceformatid:"";
                if(sequenceformatid=="" || sequenceformatid==undefined){
                    this.sequenceFormatCombobox.setValue("NA");
                } else {
                    this.sequenceFormatCombobox.setValue(sequenceformatid); 
                }
            // this.sequenceFormatCombobox.enable();
            } else {
                var count=this.sequenceFormatStore.getCount();
                for(var i=0;i<count;i++){
                    var seqRec=this.sequenceFormatStore.getAt(i)
                    if(seqRec.json.isdefaultformat=="Yes"){
                        this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                        break;
                    }                   
                   
                }
            }
            if(this.sequenceFormatCombobox.getValue()!=""){
                this.getNextSequenceNumber(this.sequenceFormatCombobox);            
            } else{
                this.no.setValue("");
                WtfGlobal.hideFormElement(this.no);
            }
        }            
    }
},

getNextSequenceNumber:function(a,val){
       if(!(a.getValue()=="NA")){
        this.setTrNoteNumber(true);       
         WtfGlobal.hideFormElement(this.no);
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
                    this.no.reset();
                    this.no.enable();
                }else {
                    this.no.setValue(resp.data);
                    this.no.disable();
                }
            
            });
       } else {
           WtfGlobal.showFormElement(this.no);
           this.no.reset();
           this.no.enable();
       }
    },
    
    setTrNoteNumber:function(isSelectNoFromCombo){
        var format="";var temp2="";
        var val=this.isCN*1+this.isCustBill*10;
        switch(val){
            case 0:format=Wtf.account.companyAccountPref.autodebitnote;
                temp2=Wtf.autoNum.DebitNote;
                break;
            case 1:format=Wtf.account.companyAccountPref.autocreditmemo;
                temp2=Wtf.autoNum.CreditNote;
                break;
            case 10:format=Wtf.account.companyAccountPref.autobillingdebitnote;
                temp2=Wtf.autoNum.BillingDebitNote;
                break;
            case 11:format=Wtf.account.companyAccountPref.autobillingcreditmemo;
                temp2=Wtf.autoNum.BillingCreditNote;
                break;
        }
        if(isSelectNoFromCombo){
            this.fromnumber = temp2;
        } else if(format&&format.length>0){
            WtfGlobal.fetchAutoNumber(temp2, function(resp){if(this.isEdit)this.no.setValue(resp.data)}, this);
        }
   },
   
    getInvoiceAmounts:function(){
        var amt=0;
        for(var i=0; i<this.InvGrid.store.getCount();i++){
            amt+=getRoundedAmountValue(this.InvGrid.store.getAt(i).data['invamount']);
        }
        return amt;
    },
       
    updateform : function(){
        var rec = {};
        this.ajxurl = "";
        Wtf.MessageBox.confirm(this.isDraft ? WtfGlobal.getLocaleText("acc.common.saveasdraft") : WtfGlobal.getLocaleText("acc.common.savdat"),this.isDraft ? WtfGlobal.getLocaleText("acc.invoice.msg14") : WtfGlobal.getLocaleText("acc.invoice.msg7"), function(btn) {
            if (btn != "yes") {
                return;
            }
        var isValid = this.TypeForm.form.isValid();
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
        if(!isValid || !isValidCustomFields){
            WtfComMsgBox(2,2);
            return;
        }
        var custFieldArr=this.tagsFieldset.createFieldValuesArray();
        if (custFieldArr.length > 0){
            rec.customfield = JSON.stringify(custFieldArr);
        }
        if(this.isCN){
            this.ajxurl = this.isCustBill?"ACCCreditNote/updateBillingCreditNote.do":"ACCCreditNote/updateCreditNote.do";
        }else{
            this.ajxurl = this.isCustBill?"ACCDebitNote/updateBillingDebitNote.do":"ACCDebitNote/updateDebitNote.do";
        }
        var nameStore=this.name.store;
        var selIndex=nameStore.find("accid",this.name.getValue());
        rec.costCenterId = this.CostCenter.getValue();
        rec.memo= this.Memo.getValue();
        rec.salesPersonID = this.users.getValue();
        rec.noteid=(this.record.data && this.record.data.noteid)?this.record.data.noteid:"";
        var details=this.getAccountDetailsDetails();
        rec.cntype = this.cntype;
        if (this.PO!=undefined) {
            rec.linkinvoiceids =  this.PO.getValue();
        }
        rec.details = details;
        Wtf.Ajax.requestEx({
                url:this.ajxurl,
                params: rec
        },this,this.genSuccessResponse,this.genFailureResponse);
         }, this);
    },
    saveForm : function(){
        if(this.no){
            this.no.setValue(this.no.getValue().trim());
        }
        
        var isValid = this.TypeForm.form.isValid();
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
        if(!isValid || !isValidCustomFields){
            WtfComMsgBox(2,2);
            return;
        }
        
        var rec = {};
        var custFieldArr = this.tagsFieldset.createFieldValuesArray();
        var custFieldArrlength = custFieldArr.length;
        if (custFieldArrlength > 0) {
            rec.customfield = JSON.stringify(custFieldArr);
        }
        
        var invoiceids="";
        var amounts="";
        var totalAccountsAmt = this.getAccountsAmount();
            totalAccountsAmt = getRoundedAmountValue(totalAccountsAmt);
        if(this.cntype == 1 || this.cntype == 3){
            var invdetails=this.getInvoiceGridDetails();            
       
            if((invdetails == undefined || invdetails == "[]") && (this.cntype == 1 || this.cntype == 3)){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.PleaseselectanInvoice")],2); 
                return;
            }
          
            var amt = this.getInvoiceAmounts();  
            amt =getRoundedAmountValue(amt);
            var msg = this.isCN?WtfGlobal.getLocaleText("acc.accPref.autoCN"):WtfGlobal.getLocaleText("acc.accPref.autoDN");
            
            if (Wtf.account.companyAccountPref.manyCreditDebit && totalAccountsAmt < 0) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.EnteredAmountshouldbegraeterthanzero")], 2);
                return;
            }
            if(amt > totalAccountsAmt) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Amountenteredcannotbegreaterthan")+msg+" "+WtfGlobal.getLocaleText("acc.invoiceList.totAmt")],2);
                return;
            }
//            if (Wtf.account.companyAccountPref.countryid === Wtf.CountryID.MALAYSIA&&this.isCN) {
//            if(totalAccountsAmt>amt) {
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Amountenteredcannotbegreaterthan")+msg+" "+WtfGlobal.getLocaleText("acc.invoiceList.totAmt")],2);
//                return;
//            }
//        }
            
            for(var i=0; i<this.InvGrid.store.getCount()-1;i++){
                if(i!=0){
                    invoiceids+=",";
                    amounts+=",";
                }
                invoiceids+=this.InvGrid.store.getAt(i).data['billid'];
                amounts+=this.InvGrid.store.getAt(i).data['invamount'];
                if(this.InvGrid.store.getAt(i).data['invamount'] == 0){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.EnteredAmountForinvoiceshouldbegraeterthanzero")],2);
                    return;
                }
                
                var invoiceAmountdue = parseFloat(this.InvGrid.store.getAt(i).data['amountdue']);
                var invoiceReturnAmount = parseFloat(this.InvGrid.store.getAt(i).data['invamount']);
                if(invoiceReturnAmount>invoiceAmountdue){//if entered amount greater than amount due
                    msg = this.isCN?WtfGlobal.getLocaleText("acc.field.CustomerInvoice"):WtfGlobal.getLocaleText("acc.agedPay.venInv");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Amountenteredcannotbegreaterthan")+msg+" "+WtfGlobal.getLocaleText("acc.field.amountdue")],2);
                    return
                }
                if (custFieldArrlength > 0) {
                    var invoiceRec = this.InvGrid.store.getAt(i);
                    /*
                     * If knock off type custom fields/dimensions is created in CN/DN 
                     * then do not allow to select different knock off values invoices in CN/DN.
                     * Refer - ERP-33613
                     */
                    if (!WtfGlobal.validateKnockOffFieldsData(this.moduleid, this.tagsFieldset, invoiceRec)) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.common.validateKnockOffFieldsData.alert")], 2);
                        return;
                    }
                }
                                //Checking Mandatory field linking date-ERP-36411
                if(this.InvGrid.store.getAt(i).data['invamount']>0 && (this.InvGrid.store.getAt(i).data['linkingdate']===undefined ||this.InvGrid.store.getAt(i).data['linkingdate']===null|| this.InvGrid.store.getAt(i).data['linkingdate']==="")){
                    if (!isFromActiveDateRange(this.InvGrid.store.getAt(i).data['linkingdate'])) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.activeDateRangePeriod.transactionCannotbeCompleted.alert")],2);
                        return;
                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.linkingDateMandatoryMsg")],2);
                        return;
                    }
                }
            }
        }
        /*
         * Validate GST dimension values present or Not
         */
        if (WtfGlobal.isIndiaCountryAndGSTApplied() && Wtf.isShowAlertOnDimValueNotPresent.indexOf(parseInt(this.moduleid))> -1) {
            if (!isGSTDimensionValuePresent(this, this.grid)) {
                this.enableSaveButtons();
                return false;
            }
            /**
             * Show alert on Save document if GST details not presnet 
             * ERP-39257
             */
            if (!isGSTHistoryPresentOnDocumentCreation(this)) {
                this.enableSaveButtons();
                return false;
             }
        }
        if (totalAccountsAmt <= 0 || totalAccountsAmt==" ") {
           WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.EnteredAmountshouldbegraeterthanzero")], 2);
           return;
        }
        for(var i=0;i<this.grid.store.getCount()-1;i++){
             if(this.grid.store.getAt(i).data['dramount']=="0.00"){
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg15")],2);   //"Product(s) details are not valid."
            return;
        }
           if(this.grid.store.getAt(i).data['reason']=="" ||this.grid.store.getAt(i).data['reason']==undefined){
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.cndn.pleaseselectareason")],2);
                    return;
            }
    }
            
            
//        var rec=this.TypeForm.getForm().getValues();
        
        
        this.ajxUrl = "";
        if(this.isCN){
            this.ajxUrl = this.isCustBill?"ACCCreditNote/saveBillingCreditNote.do":"ACCCreditNote/saveCreditNote.do";
        }else{
            this.ajxUrl = this.isCustBill?"ACCDebitNote/saveBillingDebitNote.do":"ACCDebitNote/saveDebitNote.do";
        }
            
        if(this.isEdit && !this.isCopy){
            rec.noteid=(this.record.data && this.record.data.noteid)?this.record.data.noteid:"";
        }
        
        
        
        rec.invoiceids=invoiceids;
        rec.invoicedetails=invdetails;
        rec.amounts=amounts;

        var nameStore=this.name.store;
        var selIndex=nameStore.find("accid",this.name.getValue());
        var accountid = "";
        if(this.isEdit && this.record != null && this.custVenOptimizedFlag){
           accountid = this.record.data.personaccountid;
        }else{
           accountid=nameStore.getAt(selIndex).data.accountid
        }
       
        rec.accountid = accountid;
        rec.accid = this.name.getValue();
        rec.sequenceformat = this.sequenceFormatCombobox.getValue();
        rec.number = this.no.getValue();
        rec.supplierinvoiceno = this.SupplierInvoiceNo.getValue();
        if(this.cntype == 2){
            rec.otherwise = true;
        }
        
        var details=this.getAccountDetailsDetails();
        var validLineItem = this.checkDetails(this.grid);
        if (validLineItem != "" && validLineItem != undefined) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), (WtfGlobal.getLocaleText("acc.msgbox.lineitem") + validLineItem)], 2);
            return;
        }
       
        if(details == undefined || details == "[]"){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg15")],2);   //"Product(s) details are not valid."
            return;
        }
        if(this.isCopy){
            var accountDetails=eval(details);
            var hasAccessFlag=false;
            var accountsNotAccessList="";
            for(var i=0;i<accountDetails.length;i++){
                var gridRec = accountDetails[i];
                var accRec=WtfGlobal.searchRecord(this.accountStore, gridRec.accountid, 'accountid');
                if(accRec!=null){
                    var hasAccess=accRec.get('hasAccess');
                    if(!hasAccess){
                        accountsNotAccessList=accountsNotAccessList + accRec.get('accountname') +", ";
                        hasAccessFlag=true;
                    }
                }
            }
            if(accountsNotAccessList!=""){
                accountsNotAccessList = accountsNotAccessList.substring(0, accountsNotAccessList.length-2);
            }
            if(hasAccessFlag){
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                    msg: WtfGlobal.getLocaleText("acc.field.Inselectedaccountssomeaccountsaredeactivated")+
                    "<br>"+WtfGlobal.getLocaleText("acc.field.DeactivatedAccounts")+accountsNotAccessList,
                    width:370,
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.WARNING,
                    scope: this
                });
                return;
            }
        }
        
        if (this.isCopy) {
                var hasAccessFlag = false;
                var Name = "";
                var title = "";
                var personId = this.name.getValue();
                var personRec = WtfGlobal.searchRecord(this.name.store, personId, this.name.valueField);
                if (personRec != null) {
                    var hasAccess = (personRec.get('hasAccess')!=undefined)?personRec.get('hasAccess'):this.record.data.hasAccess;
                    if (hasAccess!=undefined && !hasAccess) {
                        Name = personRec.get('accname');
                        hasAccessFlag = true;
                    }
                }
                if (hasAccessFlag) {
                    if (this.businessPerson == "Customer") {
                    title = WtfGlobal.getLocaleText("acc.customer.customerName");
                } else {
                    title = WtfGlobal.getLocaleText("acc.vendor.vendorName");
                }
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                        msg: title + Name + " " + WtfGlobal.getLocaleText("acc.field.iscurrentlydeactivated"),
                        width: 370,
                        buttons: Wtf.MessageBox.OK,
                        icon: Wtf.MessageBox.WARNING,
                        scope: this
                    });
                    return;
                }
            }
            if (this.users!=undefined) {
                var isSaledpersonDeactivated = WtfGlobal.isSaledpersonDeactivated(this.users, this.businessPerson);
                if (isSaledpersonDeactivated) {
                    this.enableSaveButtons();
                    return;
                }
            }
            
        var amount=0.0;
        var detailsObject = eval('(' + details + ')');
        for(var jsonObj=0;jsonObj<detailsObject.length;jsonObj++){
        if ((this.isCustomer && detailsObject[jsonObj].debit=="true") ||(!this.isCustomer && detailsObject[jsonObj].debit=="false") ) {
            if(this.includingGST.getValue()){
                /*
                 *If includingGST check is true then take amount from rateincludingGST column and calculate total amount
                 */
                amount += getRoundedAmountValue(detailsObject[jsonObj].rateIncludingGst);
            }else{
                amount += getRoundedAmountValue(parseFloat(detailsObject[jsonObj].dramount));
            }
            amount += getRoundedAmountValue(parseFloat(detailsObject[jsonObj].taxamount));
                if (WtfGlobal.isIndiaCountryAndGSTApplied() && detailsObject[jsonObj].recTermAmount !== undefined && detailsObject[jsonObj].recTermAmount !== null && detailsObject[jsonObj].recTermAmount !== "") {
                    if (detailsObject[jsonObj].recTermAmount > 0) {
                        amount += getRoundedAmountValue(detailsObject[jsonObj].recTermAmount);
                    }
                }
            } else {
                if (this.includingGST.getValue()) {
                    /*
                     *If includingGST check is true then take amount from rateincludingGST column and calculate total amount
                     */
                    amount -= getRoundedAmountValue(detailsObject[jsonObj].rateIncludingGst);
                } else {
                    amount -= getRoundedAmountValue(parseFloat(detailsObject[jsonObj].dramount));
                }

                amount -= getRoundedAmountValue(parseFloat(detailsObject[jsonObj].taxamount));
                if (WtfGlobal.isIndiaCountryAndGSTApplied() && detailsObject[jsonObj].recTermAmount !== undefined && detailsObject[jsonObj].recTermAmount !== null && detailsObject[jsonObj].recTermAmount !== "") {
                    if (detailsObject[jsonObj].recTermAmount > 0) {
                        amount -= getRoundedAmountValue(detailsObject[jsonObj].recTermAmount);
                    }
                }
            }
        }
        if (Wtf.account.companyAccountPref.manyCreditDebit && amount < 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.EnteredAmountshouldbegraeterthanzero")], 2);
            return;
        }
        
        if(this.cntype == 1){//For Linking case only
        /*Checking Document Date & Payment Date with Linking Date*/
            var validateflag=this.checkMaxDate();
            if (!validateflag) {
                this.enableSaveButtons();
                return;
            }
                 
        }
        var confirmMsg = "";
        if(Wtf.Countryid == Wtf.Country.MALAYSIA && this.isNonZeroRatedTaxCodeUsedInTransaction()){
            confirmMsg = this.cntype == 2 ? (this.isCN ? WtfGlobal.getLocaleText("acc.save.cnotherwiseNote.confirm.msg1") : WtfGlobal.getLocaleText("acc.save.dnotherwiseNote.confirm.msg1")) : WtfGlobal.getLocaleText("acc.tax.nonZeroTaxcode.alert");
        } else if (this.cntype == 2 && Wtf.Countryid == Wtf.Country.MALAYSIA && this.isTaxApplied()) {
            confirmMsg = this.isCN ? WtfGlobal.getLocaleText("acc.save.cnotherwiseNote.confirm.msg") : WtfGlobal.getLocaleText("acc.save.dnotherwiseNote.confirm.msg");
        } else {
            confirmMsg = WtfGlobal.getLocaleText("acc.invoice.msg7");
        }
        
        // Getting confirmation before save.
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),confirmMsg,function(btn){
        if (btn != "yes") {
            return;
        } else {
            rec=WtfGlobal.getAddressRecordsForSave(rec,this.record,this.linkRecord,this.currentAddressDetailrec,this.customerFlag,this.singleLink,this.isEdit,this.isCopy);
            rec.amount = getRoundedAmountValue(amount);
            rec.details = details;
                if (this.isIndiaGST) {
                    rec.CustomerVendorTypeId = this.CustomerVendorTypeId;
                    rec.GSTINRegistrationTypeId = this.GSTINRegistrationTypeId;
                    rec.gstin = this.gstin;
                    if (this.isEdit && !this.isCopy) {
                        rec.gstdochistoryid = this.gstdochistoryid;
                    }
                }
          
            rec.creationdate = WtfGlobal.convertToGenericDate(this.creationDate.getValue());
            rec.cntype = this.cntype;
            rec.currencyid = this.Currency.getValue();
            rec.externalcurrencyrate=this.externalcurrencyrate;
            rec.exchangeratefortransaction=this.externalcurrencyrate;
            var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
            rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):false;
            if((Wtf.Countryid==Wtf.Country.INDIA && Wtf.Stateid==Wtf.StateName.MAHARASHTRA) &&  Wtf.account.companyAccountPref.enablevatcst){ 
                rec.mvattransactionno = this.MVATAnnexureCodeCombo.getValue();
            }
            rec.currencyid = this.Currency.getValue();
            rec.costCenterId = this.CostCenter.getValue();
            if (this.record != null) {
                rec.billid = this.record.data.billid;
                rec.isCopy = this.isCopy;
            }
            rec.memo= this.Memo.getValue();
            if (this.cntype == "4") {
             rec.masteragent = this.users.getValue();
             } else {
             rec.salesPersonID = this.users.getValue();
            }
            if (this.PO!=undefined) {
                rec.linkinvoiceids =  this.PO.getValue();
            }
            rec.isEditToApprove= this.isEditToApprove?true:false;
            this.saveBttn.disable();
            rec.includingGST = (this.includingGST)? this.includingGST.getValue() : false;
            Wtf.Ajax.requestEx({
                url:this.ajxUrl,
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
        }
    },this);    
    },
    
   genSuccessResponse:function(response){
         if (response.success) {
            if (this.moduleid == Wtf.Acc_Credit_Note_ModuleId && Wtf.getCmp("CreditNoteDetails") != undefined) {
                Wtf.getCmp("CreditNoteDetails").Store.on('load', function() {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], response.success * 2 + 1);
                }, Wtf.getCmp("CreditNoteDetails").Store, {
                    single: true
                });
            } else if (this.moduleid == Wtf.Acc_Credit_Note_ModuleId && Wtf.getCmp("Credit_Note_against_Customer_Invoice") != undefined) {
                Wtf.getCmp("Credit_Note_against_Customer_Invoice").Store.on('load', function() {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], response.success * 2 + 1);
                }, Wtf.getCmp("Credit_Note_against_Customer_Invoice").Store, {
                    single: true
                });
            } else if (this.moduleid == Wtf.Acc_Debit_Note_ModuleId && Wtf.getCmp("DebitNoteDetails") != undefined) {
                Wtf.getCmp("DebitNoteDetails").Store.on('load', function() {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], response.success * 2 + 1);
                }, Wtf.getCmp("DebitNoteDetails").Store, {
                    single: true
                });
            } else if (this.moduleid == Wtf.Acc_Debit_Note_ModuleId && Wtf.getCmp("DebitNoteagainstVendorInvoice") != undefined) {
                Wtf.getCmp("DebitNoteagainstVendorInvoice").Store.on('load', function() {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], response.success * 2 + 1);
                }, Wtf.getCmp("DebitNoteagainstVendorInvoice").Store, {
                    single: true
                });
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], response.success * 2 + 1);
            }
                this.disableComponent();
                this.southCenterTpl.addClass('inactive-link');
//                this.singleRowPrint.enable(); // Enable Print Record(s) button :  ERP-25258
                this.isClosable = true;
                
                if(Wtf.getCmp(this.reloadGrid)){
                    //(function(){
                    Wtf.getCmp(this.reloadGrid).store.reload();
                //}).defer(WtfGlobal.gridReloadDelay(),this);
                }
                
        } else if (response.accException) {
            Wtf.MessageBox.hide();
            var label = "";
            switch (this.moduleid) {
                case Wtf.Acc_Credit_Note_ModuleId:
                    label = WtfGlobal.getLocaleText("acc.CN.newcreditnoteno");
                    break;
                case Wtf.Acc_Debit_Note_ModuleId:
                    label = WtfGlobal.getLocaleText("acc.DN.newdebitnoteno");
                    break;
            }
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
                                fieldLabel: label,
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
                                        switch (this.moduleid) {
                                            case Wtf.Acc_Credit_Note_ModuleId:
                                            case Wtf.Acc_Debit_Note_ModuleId:
                                                 Wtf.getCmp("cndnNumber"+this.heplmodeid+this.id).setValue(this.newdono.getValue());
                                                 break;
                                        }
                                        this.saveForm();
                                        this.newnowin.close();
                                    }
                                },
                                scope: this
                            }, {
                                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //"Cancel",
                                scope: this,
                                handler: function () {
                                    this.newnowin.close();
                                    this.saveBttn.enable();
                                }
                            }]
                    })]
            });
            this.newnowin.show();
        }else{
            var msg = this.isCN?WtfGlobal.getLocaleText("acc.accPref.autoCN"):WtfGlobal.getLocaleText("acc.accPref.autoDN");
            WtfComMsgBox([msg,response.msg],(response.success?3:2));
             this.saveBttn.enable();
        }
    },
    
    genFailureResponse:function(response){
        this.saveBttn.enable();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    disableComponent:function(){
        if(this.saveBttn){
            this.saveBttn.disable();
        }
        if(this.showAddrress){
            this.showAddrress.disable(); 
        } 
        if(this.TypeForm){
            this.TypeForm.disable();
        }
        if(this.grid){
            this.grid.disable();
        }
        if(this.InvGrid){
            this.InvGrid.disable(); 
        }
    },
    checkMaxDate:function(){//finding maximum date
        if(this.cntype == 1) {
            var checkdateflag=true;
            var transactiondate=this.creationDate.getValue();
            var linkingdate=this.linkingDate.getValue();
            linkingdate=linkingdate.setHours(0, 0, 0, 0)
            var msg=this.isCN?WtfGlobal.getLocaleText("acc.field.EnteredDocumentDateCreditNoteDate"):WtfGlobal.getLocaleText("acc.field.EnteredDocumentDateDebitNoteDate");
            //Payment Date is checked
//            if(transactiondate.getTime() > linkingdate){//comparing Credit Note billid with Grids Linked Date
//                
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.EnteredDocumentDategreaterthanLinkingDate")+" "+msg+". "+WtfGlobal.getLocaleText("acc.field.changelinkingdate")],2);
//                checkdateflag=false;
//                return false;
//            }            
            //Document Date is checked  
            for(var i=0; i<this.InvGrid.getStore().getCount();i++){
                if(this.InvGrid.getStore().getAt(i).data['invamount']!=0){//if amount is not zero
                    var billdate=this.InvGrid.getStore().getAt(i).data['date'];
                    billdate=billdate.setHours(0, 0, 0, 0);
                    if(billdate>linkingdate){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.EnteredDocumentDategreaterthanLinkingDate")+" "+msg+". "+WtfGlobal.getLocaleText("acc.field.changelinkingdate")],2);
                        checkdateflag=false;
                        return false;
                    }
                }
            }
            if(checkdateflag){
                return true; 
            }                
        } 
    },
    getAccountDetailsDetails:function(){
        var arr=[];
        var number=0;
        this.store.each(function(rec){            
            number++;
             rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
             rec.data["srNoForRow"]=number;
             arr.push(this.store.indexOf(rec));
        }, this);
        var jarray=WtfGlobal.getJSONArray(this.grid,false,arr);
        return jarray;
    },
    
    getInvoiceGridDetails:function(){
        var arr=[];
        this.InvGridStore.each(function(rec){
             arr.push(this.InvGridStore.indexOf(rec));
        }, this);
        var jarray=WtfGlobal.getJSONArray(this.InvGrid,false,arr);
        return jarray;
    },
    
    addCostCenter:function(){
        callCostCenter('addCostCenterWin');
    },
    checkDetails: function (grid) {
        var v = WtfGlobal.checkValidItems(this.moduleid, grid);
        return v;
    },
    populateDimensionValueingrid: function (rec) {
        WtfGlobal.populateDimensionValueingrid(this.moduleid, rec, this.grid);
    },
     onDateChange:function(a,val,oldval){
        if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
            this.applyGSTFieldsBasedOnDate();
        }
        this.val = val;
        this.datechanged = true;
        if (this.Currency.getValue() == WtfGlobal.getCurrencyID()) { //when tranaction in base currency for all cases (edit,copy, create new)
            this.currencyStore.load({params: {mode: 201, transactiondate: WtfGlobal.convertToGenericDate(this.creationDate.getValue())}});
        } else if ((this.isEdit) && Wtf.account.companyAccountPref.retainExchangeRate) { //edit case: when user want to retain exchange rate        
            this.exchangeRateInRetainCase = true;
            this.currencyStore.load({params: {mode: 201, transactiondate: WtfGlobal.convertToGenericDate(this.creationDate.getValue())}});
        } else if (this.isEdit) { //1.Edit case when user do not want to retain exchange rate 2.copy case
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.invoice.exchangeRateMsg"), function (btn) {
                if (btn == "yes") {
                    this.currencyStore.load({params: {mode: 201, transactiondate: WtfGlobal.convertToGenericDate(this.creationDate.getValue())}});
                } else {
                    this.creationDate.setValue(oldval);
                    return;
                }
            }, this);
        } else { //Normal Create New Case           
            this.currencyStore.load({params: {mode: 201, transactiondate: WtfGlobal.convertToGenericDate(this.creationDate.getValue())}});
        }
        if (Wtf.account.companyAccountPref.countryid === Wtf.CountryID.MALAYSIA) { // for malaysian company
            this.enableDisableFields();
        }
//        //ERP-36411:-If changed link date is valid, then it may be future date or valid past date. Here store should be re-loaded
//        //The Invoices were loaded on change event of linking date.and previous records will be removed. 
        if (this.cntype == 1) {//Only for invoices
            this.linkingDate.setValue(val);
            this.InvGridStore.removeAll();
            this.addInvoiceGridRec();
        this.InvGridComboStore.reload();
        }
    },
    enableDisableFields:function(){
        if (Wtf.account.companyAccountPref.countryid === Wtf.CountryID.MALAYSIA) { // for malaysian company
            var isTaxShouldBeEnable = WtfGlobal.isTaxShouldBeEnable(new Date(this.creationDate.getValue()).clearTime());
            if (!isTaxShouldBeEnable) { // check if tax should be disable or not
                if (this.transTax) { // tax
                    this.transTax.setDisabled(true);
                }
                if (this.transTaxAmount) { // tax amount
                    this.transTaxAmount.setDisabled(true);
                }
                this.includingGST.setValue(false);
                this.includingGST.disable();
                var rowRateIncludingGstAmountIndex = this.grid.getColumnModel().findColumnIndex("rateIncludingGst");
                this.grid.getColumnModel().setHidden(rowRateIncludingGstAmountIndex, true);
            } else {
                if (this.transTax) {// tax
                    this.transTax.setDisabled(false);
                }
                if (this.transTaxAmount) { // tax amount 
                    this.transTaxAmount.setDisabled(false);
                }
                this.includingGST.enable();
            }
        }
    },
    onNameChange:function(){
        var rec = WtfGlobal.searchRecord(this.customerFlag?this.customerStore:this.vendorStore, this.name.getValue(), "accid");
        this.Currency.setValue(rec.data['currencyid']);
        this.users.setValue(rec.data['masterSalesPerson']);
        this.onCurrencyChange();
        var moduleid=this.isCustomer ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId;
        this.clearGridData();
        if (this.cntype == 4) {
            moduleid = this.isCustomer ? Wtf.Acc_Vendor_ModuleId : Wtf.Acc_Customer_ModuleId;
        }
        if (!(this.isEdit || this.copyInv)) {
            this.tagsFieldset.resetCustomComponents();
        }
        var customer = this.name.getValue();
        this.tagsFieldset.setValuesForCustomer(moduleid, customer);
        if(this.fromLinkCombo!=undefined){
            this.fromLinkCombo.enable();
        }
        if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
            this.addressMappingRec = rec.data.addressMappingRec;
            /**
             * ERP-32829 
             * code for New GST  i.e. populate dimension using dimension
             */
            if (rec.data.currentAddressDetailrec != undefined) {
                //this.applyGSTFieldsBasedOnDate();
            /**
             * IF CN against Vendor & DN against Customer on address Changes send isCustomer flag properly
             */
                var isCustomerInCNDN = undefined;
                if (this.cntype != undefined && this.cntype == 4) {
                    if (this.isCustomer == true && this.moduleid == Wtf.Acc_Credit_Note_ModuleId) {
                        isCustomerInCNDN = false;
                    } else if (this.isCustomer == false && this.moduleid == Wtf.Acc_Debit_Note_ModuleId) {
                        isCustomerInCNDN = true;
                    }
                }
                this.addressDetailRecForGST = rec.data.currentAddressDetailrec[0];
                var obj = {};
                obj.tagsFieldset = this.tagsFieldset;
                obj.currentAddressDetailrec = this.addressDetailRecForGST;
                obj.mappingRec = this.addressMappingRec;
                obj.isCustomer = isCustomerInCNDN == undefined ? this.isCustomer : isCustomerInCNDN;
                obj.isShipping = this.isShipping;
                populateGSTDimensionValues(obj);
                this.applyGSTFieldsBasedOnDate();
            }
            /**
             * Check and show alert GST details for Customer and Vendor if not available 
             */
            var cust_Vendparams = {};
            cust_Vendparams.rec = rec;
            cust_Vendparams.isCustomer = this.isCustomer;
            checkAndAlertCustomerVendor_GSTDetails(cust_Vendparams);
        }
    },
    applyGSTFieldsBasedOnDate: function() {
        var URL = this.isCustomer ? "ACCCustomerCMN/getCustomerGSTHistory.do" : "ACCVendorCMN/getVendorGSTHistory.do";
        if (this.cntype!=undefined && this.cntype == 4) {
            // moduleid = this.isCustomer ? Wtf.Acc_Vendor_ModuleId : Wtf.Acc_Customer_ModuleId;
            /**
             * IF Debit note Against Customer then Get Customer GST History
             * IF Credit note Against Vendor then Get Vendor GST History
             */
            URL = this.isCustomer ? "ACCVendorCMN/getVendorGSTHistory.do" : "ACCCustomerCMN/getCustomerGSTHistory.do";
        }
    if (this.name.getValue() == undefined || this.name.getValue() == ''){
           return;
        }        
        Wtf.Ajax.requestEx({
            url: URL,
            params: {
                customerid: this.name.getValue(),
                vendorid: this.name.getValue(),
                returnalldata: true,
                isfortransaction: true,
                transactiondate: WtfGlobal.convertToGenericDate(this.creationDate.getValue())

            }
        }, this, function(response) {
            if (response.success) {
                 /**
                 * Validate GST details
                 */
                isGSTDetailsPresnetOnTransactionDate(response,this,this.grid,this.name);
                this.ignoreHistory = true;
                this.GSTINRegistrationTypeId = response.data[0].GSTINRegistrationTypeId;
                this.gstin = response.data[0].gstin;
                this.CustomerVendorTypeId = response.data[0].CustomerVendorTypeId;
                this.uniqueCase = response.data[0].uniqueCase;
                this.transactiondateforgst = this.creationDate.getValue();
                this.CustVenTypeDefaultMstrID=response.data[0].CustVenTypeDefaultMstrID;
                this.GSTINRegTypeDefaultMstrID=response.data[0].GSTINRegTypeDefaultMstrID;
                if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA) {
                    if ((this.moduleid==10 && this.cntype != 4) || (this.moduleid==12 && this.cntype == 4)) {
                        /**
                         * DN other wise / Against invoice
                         * CN against vendor
                         */
                        if (response.data[0].GSTINRegTypeDefaultMstrID != undefined && response.data[0].GSTINRegTypeDefaultMstrID !== "" && response.data[0].GSTINRegTypeDefaultMstrID === Wtf.GSTRegMasterDefaultID.Unregistered) {
                            this.purchaseFromURD = true;
                        } else {
                            this.purchaseFromURD = false;
                        }
                    }
                }
                if (this.purchaseFromURD && this.purchaseFromURD == true) {
                    /*** If purchases is from Unregistered dealer ***/

                    /**
                     * Apply NO GST for URD
                     */
                    this.uniqueCase = Wtf.GSTCustVenStatus.NOGST;
                }
                var productIds = "";
                var array = this.grid.store.data.items;
                /**
                 * black product id record not considered before Process GST Request to apply tax rule,
                 * So need to check all reords, and added if condition for "productid" blank if last record is blank row in grid.
                 */
                for (var i = 0; i < array.length; i++) {
                    var gridRecord = this.grid.store.getAt(i);
                    if (gridRecord != undefined && gridRecord.data.productid != '') {
                        productIds += gridRecord.data.productid + ",";
                    }
                }
                getLineTermDetailsAndCalculateGSTForAdvance(this, this.grid, productIds);
            }
        });
    },
    accountDetailsGridIsEmpty: function (grid) {
        if (grid.getStore().getCount() == 0) {
            return true;
        } else {
            if (grid.getStore().getCount() > 1) {
                return false;
            } else {
                var rec = grid.getStore().getAt(0);
                return (rec.data.accountid == "" ? true : false);
            }
        }
    },
    clearGridData: function () {
        this.store.removeAll();
        this.addGridRec();
        if ((this.cntype == 1 || this.cntype == 3)) {
            this.InvGridStore.removeAll();
            this.addInvoiceGridRec();
            this.InvGridComboStore.removeAll();

            this.InvGridStore.removeAll();
            this.addInvoiceGridRec();
            this.InvGridComboStore.removeAll();

            this.InvGridComboStore.load({
                params: {
                    accid: this.name.getValue(),
                    currencyfilterfortrans: this.Currency.getValue(),
                    isReceipt: true,
                    getRecordBasedOnJEDate: true
                }
            });
        }
    },
    onCurrencyChange: function () {
        this.updateExternalCurrencyRateOnCurrencyChange();
        this.updateFormCurrency();

    },
    updateExternalCurrencyRateOnCurrencyChange: function () {
        var currencyRecord = WtfGlobal.searchRecord(this.currencyStore, this.Currency.getValue(), "currencyid");
        this.externalcurrencyrate = currencyRecord.data['exchangerate'];
    },
    updateFormCurrency: function () {
        this.applyCurrencySymbol();
    },
    getCurrencySymbol: function () {
        var index = null;
//        this.currencyStore.clearFilter(true); //ERP-9962
        var FIND = this.Currency.getValue();
        if (FIND == "" || FIND == undefined || FIND == null) {
            FIND = WtfGlobal.getCurrencyID();
        }
        index = this.currencyStore.findBy(function (rec) {
            var parentname = rec.data['currencyid'];
            if (parentname == FIND)
                return true;
            else
                return false
        })
        this.currencyid = this.Currency.getValue();
        return index;
    },
    applyCurrencySymbol: function () {
        var index = this.getCurrencySymbol();
        var rate = this.externalcurrencyrate;
        if (index >= 0) {
            rate = (rate == "" ? this.currencyStore.getAt(index).data.exchangerate : rate);
            this.symbol = this.currencyStore.getAt(index).data.symbol;
            this.applyTemplate(this.currencyStore, index);
        }
        return this.symbol;
    },
applyTemplate: function (store, index) {
    var editable = this.Currency.getValue() != WtfGlobal.getCurrencyID() && this.Currency.getValue() != ""//&&!this.isOrder;
    var exchangeRate = store.getAt(index).data['exchangerate'];
    if (this.externalcurrencyrate > 0) {
        exchangeRate = this.externalcurrencyrate;
    } else if (this.isEdit && this.record.data.externalcurrencyrate) {
        var externalCurrencyRate = this.record.data.externalcurrencyrate - 0;
        if (externalCurrencyRate > 0) {
            exchangeRate = externalCurrencyRate;
        }
    }
    var revExchangeRate = 1 / (exchangeRate - 0);
//    if(revExchangeRate!=1){ // SDP-5861 Rate must be shown if external currency rate is 1
//        this.southPanel.setVisible(true);
//    }else{
//        this.southPanel.setVisible(false);  
//    }
    if (this.exchangeratetype != undefined && this.exchangeratetype == "foreigntobase" && this.revexternalcurrencyrate != undefined && this.revexternalcurrencyrate != 0)
    {
        revExchangeRate = this.revexternalcurrencyrate
        this.revexternalcurrencyrate = 0;
    }
    revExchangeRate = (Math.round(revExchangeRate * Wtf.Round_Off_Number)) / Wtf.Round_Off_Number;
        this.southCenterTplSummary.overwrite(this.southCenterTpl.body, {foreigncurrency: store.getAt(index).data['currencyname'], exchangerate: exchangeRate, basecurrency: WtfGlobal.getCurrencyName(), editable: editable, revexchangerate: revExchangeRate
    });
    },
    
    addSalesPerson: function() {
        addMasterItemWindow('15');
    },
    
    addTax:function(){
        var p= callTax("taxwin");
        Wtf.getCmp("taxwin").on('update', function(){
            this.taxStore.reload();
        }, this);
    },
    addSelectedDocument:function(){
        var url="";
        if(this.customerFlag){
            url = "ACCGoodsReceiptCMN/getGoodsReceipts.do";
        } else {
            url = "ACCInvoiceCMN/getInvoices.do";
        }
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.DSW.WM"), function (btn) {
            if (btn == "yes") {
                Wtf.Ajax.abort(this.POStore.proxy.activeRequest);
                this.showPONumbersGrid(url);
            } else {
                return;
            }
        }, this);
    },
    enableNumber: function () {
        this.PO.clearValue();
        if (this.customerFlag) {
            this.POStore.proxy.conn.url = "ACCInvoiceCMN/getInvoices.do";
            this.POStore.load({
                params: {
                    accid: this.name.getValue(),
                    exceptFlagORD: true,
                    currencyfilterfortrans: this.Currency.getValue()
                }
            });
            this.PO.enable();
        } else {
            this.POStore.proxy.conn.url = "ACCGoodsReceiptCMN/getGoodsReceipts.do";
            this.POStore.load({
                params: {
                    accid: this.name.getValue(),
                    exceptFlagORD: true,
                    currencyfilterfortrans: this.Currency.getValue()
                }
            });
            this.PO.enable();
        }
    },
    showPONumbersGrid: function (url) {
        this.PONumberSelectionWin = new Wtf.account.PONumberSelectionWindow({
            renderTo: document.body,
            height: 500,
            id: this.id + 'PONumbersSelectionWindowDO',
            width: 1200,
            title: 'Document Selection Window',
            layout: 'fit',
            modal: true,
            resizable: false,
            url: url,
            inputValue: this.inputValue,
            moduleid: this.moduleid,
            doNotPopulateLineLevelData: true,
            columnHeader: this.fromLinkCombo.getRawValue(),
            invoice: this,
            storeBaseParams: this.POStore.baseParams,
            storeParams: this.POStore.lastOptions.params,
            PORec: this.PORec,
            fromLinkComboValue: this.fromLinkCombo.getValue()
        });
        this.PONumberSelectionWin.show();
    },
    createIndiaRelatedFields: function () {
        var fromLinkStoreRec = new Array();
        var emptyText = "";
        if (this.customerFlag) {
            fromLinkStoreRec.push(['Sales Invoice', '1']);
            emptyText = "Select Sales Invoice";
        } else {
            fromLinkStoreRec.push([WtfGlobal.getLocaleText("acc.pi.PurchaseInvoice"), '1']);
            emptyText = "Select Purchase Invoice";
        }

        this.fromlinkStore = new Wtf.data.SimpleStore({
            fields: [{
                    name: 'name'
                }, {
                    name: 'value'
                }],
            data: fromLinkStoreRec
        });
        this.fromLinkCombo = new Wtf.form.ComboBox({
            triggerAction: 'all',
            name: "fromLinkCombo",
            hideLabel: false,
            hidden: false,
            mode: 'local',
            valueField: 'value',
            displayField: 'name',
            disabled: this.isEdit && !this.readOnly ?false:true, 
            id: 'fromLinkComboId' + this.id,
            store: this.fromlinkStore,
            emptyText: emptyText,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Linkto"), //"Link to "+(this.isCustomer?"Sales":"Purchase")+" Order",
            allowBlank: true,
            typeAhead: true,
            width: 240,
            forceSelection: true,
            selectOnFocus: true,
            scope: this,
            listeners: {
                'select': {
                    fn: this.enableNumber,
                    scope: this
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
            {name:'productid'},
            {name:'discounttotal'},
            {name:'isAppliedForTax'},// in Malasian company if DO is applied for tax
            {name:'discountispertotal',type:'boolean'},
            {name:'currencyid'},
            {name:'currencysymbol'},
            {name:'amount'},
            {name:'amountinbase'},
            {name:'amountdue'},
            {name:'costcenterid'},
            {name:'lasteditedby'},
            {name:'costcenterName'},
            {name:'memo'},
            {name:'shipvia'},
            {name:'fob'},
            {name:'includeprotax',type:'boolean'},
            {name:'salesPerson'},
            {name:'islockQuantityflag'},
            {name:'agent'},
            {name:'termdetails'},
            {name:'LineTermdetails'},//Line Level Term Details
            {name:'shiplengthval'},
            {name:'gstIncluded'},
            {name:'quotationtype'},
            {name:'contract'},
            {name:'termid'},
            {name:'externalcurrencyrate'},//    ERP-9886
            {name:'customerporefno'},
            {name:'isexpenseinv'},
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
            {name: 'vendorbillingCityForINDIA'},
            {name: 'populateproducttemplate'},
            {name: 'populatecustomertemplate'},
            {name: 'vendcustShippingAddressType'},
            {name: 'gtaapplicable'},
            {name: 'gstapplicable'},
            {name:'supplierinvoiceno'},//SDP-4510
            {name: 'importexportdeclarationno'},//ERM-470
            {name:'landedInvoiceID'},
            {name:'landedInvoiceNumber'},
            {name: 'customerShippingAddressType'},
            {name: 'customerShippingAddress'},
            {name: 'customerShippingCountry'},
            {name: 'customerShippingState'},
            {name: 'customerShippingCounty'},
            {name: 'customerShippingCity'},
            {name: 'customerShippingEmail'},
            {name: 'customerShippingFax'},
            {name: 'customerShippingMobile'},
            {name: 'customerShippingPhone'},
            {name: 'customerShippingPostal'},
            {name: 'customerShippingContactPersonNumber'},
            {name: 'customerShippingContactPersonDesignation'},
            {name: 'customerShippingWebsite'},
            {name: 'customerShippingRecipientName'},
            {name: 'customerShippingContactPerson'},
            {name: 'customerShippingRoute'},
            {name: 'isapplytaxtoterms'},
            {name: 'isDisabledPOforSO'},
            {name: 'isRoundingAdjustmentApplied'}
            
        ]);
        this.POStoreUrl = (this.customerFlag) ? "ACCInvoiceCMN/getInvoices.do" : "ACCGoodsReceiptCMN/getGoodsReceipts.do";

        this.POStore = new Wtf.data.Store({
            url: this.POStoreUrl,
            baseParams: {
                mode: 42,
                closeflag: true,
                dropDownData: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'count'
            }, this.PORec)
        });

        this.POStore.on('beforeload', function () {
            WtfGlobal.setAjaxTimeOut();
        }, this);

        this.POStore.on('load', function () {
            WtfGlobal.resetAjaxTimeOut();
        }, this);

        this.POStore.on('loadexception', function () {
            WtfGlobal.resetAjaxTimeOut();
        }, this);


        this.MSComboconfig = {
            hiddenName: "linkinvoicei",
            allowBlank: true,
            store: this.POStore,
            valueField: 'billid',
            hideLabel: false,
            hidden: false,
            displayField: 'billno',
            disabled: true,
            clearTrigger: this.isEdit ? false : true,
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus: true,
            width: 240,
            triggerAction: 'all',
            scope: this
        };

        this.PO = new Wtf.common.Select(Wtf.applyIf({
            multiSelect: true,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Number"),
            id: "poNumberID" + this.id,
            forceSelection: true,
            disabled: this.isEdit && !this.readOnly ?false:true, 
            addCreateOpt: true,
            addNewFn: this.addSelectedDocument.createDelegate(this),
            width: 240
        }, this.MSComboconfig));
    },
    openDocumentWindow: function (e,rowIndex) {

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
            parentCmpID: this,
            invoiceGrid: this.grid,
            isCustomer: this.isCustomer,
            isForAdvance: true
        });
        this.productSelWin.on('beforeclose', function (winObj) {
            if (winObj.isSubmitBtnClicked) {
                this.setHSNForAdvance(winObj.productgrid.getSelections(),rowIndex);
            }
        }, this);
        this.productSelWin.show();
    },
    /**
     * set HSN for India Advance
     */
    setHSNForAdvance: function (jsonArray,rowIndex) {
        var jsonArrayObj = eval(jsonArray);
        this.productid = "";
        if (jsonArrayObj.length == 1) {
            for (var cnt = 0; cnt < jsonArrayObj.length; cnt++) {
                if (jsonArrayObj[cnt].data["Custom_HSN/SAC Code"] != "" && jsonArrayObj[cnt].data["Custom_HSN/SAC Code"] != undefined) {
                    var value = jsonArrayObj[cnt].data["Custom_HSN/SAC Code"];
                    var globalname = "Custom_HSN/SAC Code";
                    this.productid = jsonArrayObj[cnt].data["productid"];
                    for (var k = 0; k < this.mulDebitCM.config.length; k++) {
                        if (this.mulDebitCM.config[k].editor && this.mulDebitCM.config[k].editor.field.store && this.mulDebitCM.config[k].dataIndex == globalname) {
                            var store = this.mulDebitCM.config[k].editor.field.store;
                            var gridRecord = this.store.getAt(rowIndex);
                            var recCustomCombo = WtfGlobal.searchRecord(store, value, "name");
                            if (recCustomCombo !== null && recCustomCombo !== undefined) {
                                var ComboValueID = recCustomCombo.data.id;
                                gridRecord.set(globalname, ComboValueID);
                                gridRecord.set('productid', this.productid);
                            }
                        }
                    }
                }
            }
            getLineTermDetailsAndCalculateGSTForAdvance(this, this.grid, this.productid);
            var totalAmt = this.getAccountsAmount();
            this.Amount.setValue(totalAmt);
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.field.PleaseSelectOnlyoneProduct")], 2);
            return;
        }
    },
    lineTermRenderer: function (v, m, rec) {
        return getToolTipOfTermsfun(v, m, rec);
    },
    showTermWindow: function (record, grid, rowindex) {
//        return;
        var venderDetails = WtfGlobal.searchRecord(this.name.store, this.name.getValue(), 'accid');
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
                parentObj: this,
                isGST: WtfGlobal.isIndiaCountryAndGSTApplied(),
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
    onGridDataChanged: function(){  
        var totalAmt = this.getAccountsAmount();
        this.Amount.setValue(totalAmt);
    },
    
    isTaxApplied: function () {
        var isTaxApplied = false;
        var array = this.grid.store.getRange();
        for (var i = 0; i < array.length - 1; i++) {
            if (array[i].data.prtaxid != undefined && array[i].data.prtaxid != "" && array[i].data.prtaxid != "None") {
                isTaxApplied = true;
                break;
            }
        }
        return isTaxApplied;
    },
    
    isNonZeroRatedTaxCodeUsedInTransaction: function () {
        var isNonZeroRatedTaxCodeUsedInTransaction = false;
        if (this.creationDate != undefined && this.creationDate.getValue() != undefined && (new Date(this.creationDate.getValue()) >= new Date(Wtf.ZeroRatedTaxAppliedDateForMalasia))) {
            this.grid.getStore().each(function (rec) {
                if (!Wtf.isEmpty(rec.data, false) && !Wtf.isEmpty(rec.data.prtaxid, false)) {
                    var taxrec = WtfGlobal.searchRecord(this.taxStore, rec.data.prtaxid, 'prtaxid');
                    if (taxrec && taxrec.data && taxrec.data.percent > 0) {
                        isNonZeroRatedTaxCodeUsedInTransaction = true;
                        return;
                    }
                }
            }, this);
        }
        return isNonZeroRatedTaxCodeUsedInTransaction;
    },
    
    getMyConfig: function () {
        WtfGlobal.getGridConfig(this.grid, this.moduleid + "_" + this.cntype, true, false);
        if (this.cntype == 1 || this.cntype == 3) {
            WtfGlobal.getGridConfig(this.InvGrid, this.moduleid + "_" + this.cntype + "_1", true, false);
        }
    },
    
    saveGridStateHandler: function (grid, state) {
        WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid + "_" + this.cntype, grid.gridConfigId, true);
    },
    
    saveInvGridStateHandler: function (grid, state) {
        WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid + "_" + this.cntype + "_1", grid.gridConfigId, true);
    }
});