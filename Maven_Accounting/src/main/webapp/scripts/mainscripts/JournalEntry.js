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
/* <COMPONENT USED FOR>
 * 1.Journal Entry
 *      callJournalEntry() --- <Make a Journal Entry>
 *
 */

function editJEExchangeRates(winid,basecurrency,foreigncurrency,exchangerate,exchangeratetype){
    function showJEExternalExchangeRate(btn,txt){
        if(btn == 'ok'){
             if(txt.indexOf('.')!=-1)
                 var decLength=(txt.substring(txt.indexOf('.'),txt.length-1)).length;
            if(isNaN(txt)||txt.length>15||decLength>7||txt==0){
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.setupWizard.curEx"), //'Exchange Rate',
                    msg: WtfGlobal.getLocaleText("acc.nee.55")+
                    "<br>"+WtfGlobal.getLocaleText("acc.nee.56")+
                    "<br>"+WtfGlobal.getLocaleText("acc.nee.57"),
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.WARNING,
//                    width: 300,
                    scope: this,
                    fn: function(){
                        if(btn=="ok"){
                            editJEExchangeRates(winid,basecurrency,foreigncurrency,exchangerate,exchangeratetype);
                        }
                    }
                });
            }else{
                if(exchangeratetype!=undefined)
                    Wtf.getCmp(winid).exchangeratetype=exchangeratetype
                if(exchangeratetype!=undefined&&exchangeratetype=='foreigntobase'){
                    if((txt*1)>0) {
                        Wtf.getCmp(winid).revexternalcurrencyrate=txt;
                        var exchangeRateNormal = 1/((txt*1)-0);
                        exchangeRateNormal = (Math.round(exchangeRateNormal*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
                        Wtf.getCmp(winid).externalcurrencyrate=exchangeRateNormal;
                    } 
                }else{
                    Wtf.getCmp(winid).externalcurrencyrate=txt;
                }
                Wtf.getCmp(winid).updateFormCurrency();
            }
        }
    }
    Wtf.MessageBox.prompt(WtfGlobal.getLocaleText("acc.setupWizard.curEx"),'<b>'+WtfGlobal.getLocaleText("acc.nee.58")+'</b> 1 '+basecurrency+' = '+exchangerate+' '+foreigncurrency +
        '<br><b>'+WtfGlobal.getLocaleText("acc.nee.59")+'</b>', showJEExternalExchangeRate);
}

Wtf.account.JournalEntryPanel=function(config){
    this.modeName = config.modeName;
    this.typeValue=config.type;
    this.moduleid=Wtf.Acc_GENERAL_LEDGER_ModuleId;
    /**
     *isTemplate flag for vreate template case
     *isViewTemplate flag for view template case
     *isEditTemplate flag for edit template case
     */
    this.isViewTemplate = (config.isViewTemplate!=undefined?config.isViewTemplate:false);
    this.isITCJE = (config.isITCJE!=undefined?config.isITCJE:false);
    this.isEditTemplate = (config.isEditTemplate!=undefined?config.isEditTemplate:false);
    this.isAllowedSpecificFields=(config.isAllowedSpecificFields == null || config.isAllowedSpecificFields == undefined)? false : config.isAllowedSpecificFields;
    this.isTemplate = (config.isTemplate!=undefined?config.isTemplate:false);
    this.isCopyFromTemplate = (config.isCopyFromTemplate!=undefined?config.isCopyFromTemplate:false);
    this.templateId = config.templateId;
    this.templateName=(config.isTemplate!=undefined?config.isTemplate:"");
    this.isLoadAccountStoreInEditPartyJE=true;
    this.iscallsetInitVaueMethodOnLoad=true;
    Wtf.apply(this, config);
    this.symbol=null;
    this.requestBaseFlag = false; //request account balance in base currency
    this.requestFlag = false;//request account balance in document currency
    this.readOnly=config.readOnly;
    this.createTransactionAlso = false;
    this.saveAndCreateNewFlag = false;
    this.isWarnConfirm = false;
    this.entryDate=new Wtf.form.DateField({
        fieldLabel: WtfGlobal.getLocaleText("acc.je.jeDate"),  //'Journal Date*',
        format:WtfGlobal.getOnlyDateFormat(),
        name: 'entrydate',
        anchor:'90%',
        value:new Date(),
        //value:Wtf.serverDate.clearTime(),
        allowBlank:false,
        disabled:this.readOnly||this.isViewTemplate
    });  

    
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
                mode:this.modeName,
                isEdit:this.isEdit
            }
        });
        
    if(!this.isTemplate) { // this check is added due to avoiding issue of sequence number getting incremented in case of template not having create also transaction check
        this.sequenceFormatStore.on('load',function(){  
            
            if(this.sequenceFormatStore.getCount()>0){
                if(this.isEdit){ //only edit case  
                   var formatId = this.jeDetails.data.sequenceformatid;                    
                   var index= WtfGlobal.searchRecordIndex(this.sequenceFormatStore,formatId,"id");
                    if(index>=0){
                        this.sequenceFormatCombobox.setValue(formatId);                        
                        this.jeNo.disable(); 
                    } else if(this.jeDetails.data.parentje || this.jeDetails.data.isRepeated){    //ERP-12928 : Recurred JE do not have sequence id
                        this.sequenceFormatCombobox.setValue("NA");              
                        this.jeNo.disable(); 
                    }else{
                        this.sequenceFormatCombobox.setValue("NA");                        
                        this.jeNo.enable();  
                    }
                    this.sequenceFormatCombobox.disable();
                }else{// create new 
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
                        this.jeNo.setValue(""); 
                        WtfGlobal.hideFormElement(this.jeNo);
                    }
                }                                         
            }
                
        },this);
    }
     this.sequenceFormatStore.load();
  
    this.sequenceFormatCombobox = new Wtf.form.ComboBox({            
//        labelSeparator:'',
//        labelWidth:0,
        triggerAction:'all',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
        valueField:'id',
        displayField:'value',
        store:this.sequenceFormatStore,
        disabled:(this.isEdit&&!this.copyInv&&!this.isPOfromSO&&!this.isSOfromPO?true:false),  
        anchor: '90%',
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
        
    this.jeNo=new Wtf.form.TextField({
        name:'entryno',
        scope:this,
        allowBlank:false ,
        disabled : this.isEdit||this.readOnly||this.isViewTemplate,
        maxLength:45,
        fieldLabel:WtfGlobal.getLocaleText("acc.je.jeNo"),  //'Journal Entry Number*',
        anchor:'85%',
        validator:Wtf.ValidateJournalEntryNo,
        qtip:(this.jeDetails==undefined)?'':this.jeDetails.data.entryno,
        listeners: {
                render: function(c){
                    Wtf.QuickTips.register({
                        target: c.getEl(),
                        text: c.qtip
                    });
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
    //        url:Wtf.req.account+'CompanyManager.jsp'
        url:"ACCCurrency/getCurrencyExchange.do"
     });
    this.currencyStoreCMB = new Wtf.data.Store({
       reader: new Wtf.data.KwlJsonReader({
           root: "data",
           totalProperty:"count"
       },this.currencyRec),
//        url:Wtf.req.account+'CompanyManager.jsp'
       url:"ACCCurrency/getCurrencyExchange.do"
    });

    this.currencyStoreCMB.on('load', this.changeTemplateSymbol, this);
    this.currencyStoreCMB.load();
   
    this.Currency= new Wtf.form.FnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.currency.cur")+ "*",  //'Currency',
        hiddenName:'currencyid',
        id:"currency"+this.heplmodeid+this.id,
        anchor: '90%',
//        width : 240,
        //disabled:true,
        store:this.currencyStoreCMB,
        valueField:'currencyid',
        allowBlank : false,
        forceSelection: true,
        displayField:'currencyname',
        scope:this,
        disabled:this.type==3 ||this.readOnly||this.isViewTemplate,  // Type 3 referes to Fund transafer Type JE. For fund transafer JE, currency will be disable.
        selectOnFocus:true
    });
    if(this.type==3){
        Wtf.MPPaidToStore.load();
    }
     
        this.paidTo= new Wtf.form.ExtFnComboBox({
         fieldLabel:WtfGlobal.getLocaleText("acc.mp.paidTo"),  //'Received From':'Paid To'
         name:"paidToCmb",
         store:Wtf.MPPaidToStore,
         id:"paidto"+config.helpmodeid+this.id,
         valueField:'id',
         displayField:'name',
    //     allowBlank:true,
         disabled: this.readOnly,
         emptyText:WtfGlobal.getLocaleText("acc.mp.selpaidto"), //'Select Received From...':'Select Paid To...'
         minChars:1,
         extraFields:'',
         listWidth :500,
         extraComparisionField:'name',// type ahead search on acccode as well.
         anchor:'90%',
         mode: 'local',
         triggerAction: 'all',
         typeAhead: true,
         forceSelection: true,
        hideLabel:this.typeValue!=3||Wtf.account.companyAccountPref.withoutinventory,
        hidden:this.typeValue!=3||Wtf.account.companyAccountPref.withoutinventory
    });
    
    this.bankCharges = new Wtf.form.TextField({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.BankCharges"),  //Bank charges
        id: "bankCharges" + config.helpmodeid + this.id,
        hiddenName: 'bankCharges',
        name: 'bankCharges',
        maskRe: /[0-9.]/,
        disabled: this.readOnly||this.isViewTemplate,
        anchor: '90%',
        maxLength: 45,
        hideLabel:this.typeValue==1||this.typeValue==2,  //typeValue =1 for Normal JE, typeValue =2 for Partial JE, typeValue =3 for Fund Transfer JE
        hidden:this.typeValue==1||this.typeValue==2
    });
    this.bankCharges.on('blur', this.setroundedvalue, this);
    
    this.accRec = Wtf.data.Record.create([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'},
            {name:'acccode'},
            {name:'accountpersontype'},
            {name:'mappedaccountid'},
            {name:'masterTypeValue'},
            {name:'groupname'},
            {name: 'hasAccess'},
            {name:'haveToPostJe',type:'boolean'},
            {name:'usedIn'},
            {name:'currencysymbol'},
            {name:'currencyid'},
            {name:'mastertypevalue'},
            {name:'isOneToManyTypeOfTaxAccount'},
            {name:'appliedGst'}
    ]);

    this.bankChargesAccountStore = new Wtf.data.Store({
        url: "ACCAccountCMN/getAccountsForJE.do",
        baseParams: {
            mode: 2,
            nondeleted: true,
            ignorecustomers:true,
            ignorevendors : true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.accRec)
    }); 
    
    //In Edit case of JE Fund Transfer, to show Bank account & Bank charges in Upper form
    this.bankChargesAccountStore.on('load',function(){
        if(this.isEdit||this.copyInv){
            var jeDetails = this.jeDetails.data.jeDetails;
            for(var i =0; i < jeDetails.length; i++){   //typeValue =1 for Normal JE, typeValue =2 for Partial JE, typeValue =3 for Fund Transfer JE
                if (this.typeValue == 3 && (jeDetails[i].isbankcharge)) {    //In edit case, don't add bank charges in grid, remove such record
                    this.bankChargesAccount.setValue(jeDetails[i].accountid); //Bank Charges account ID
                    this.bankCharges.setValue(jeDetails[i].d_amount_transactioncurrency)    //Bank charges amount
//                    this.bankCharges.enable();
                }
            }
        }
    },this);   
    
    this.bankChargesAccount = new Wtf.form.ExtFnComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.field.BankChargesAccount"),    //Bank Charges Account
        hiddenName: 'bankChargesCmb',
        store: this.bankChargesAccountStore,
        minChars: 1,
        anchor: '90%',
        valueField: 'accountid',
        displayField: 'accountname',
        name: 'bankChargesCmb',
        forceSelection: true,
        hirarchical: true,
        isAccountCombo:true,
        hidden:true,
        emptyText: WtfGlobal.getLocaleText("acc.mp.selectBankChargesAccount"),
        disabled: this.readOnly,
        extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode', 'groupname'] : ['groupname'],
        mode: 'remote',
        extraComparisionField: 'acccode', // type ahead search on acccode as well.
        listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 500 : 400,
        hideLabel:this.typeValue==1||this.typeValue==2, //typeValue =1 for Normal JE, typeValue =2 for Partial JE, typeValue =3 for Fund Transfer JE
        hidden:this.typeValue==1||this.typeValue==2
    });
    this.bankChargesAccount.on('beforeselect',function(combo,record,index){
        return validateSelection(combo,record,index);
    },this);
    this.bankChargesAccount.on('select', function () {      //Enable Bank chrges only when Bank Charges Account is selected
        this.bankCharges.enable();
        this.bankCharges.allowBlank = false;
    }, this);
    this.bankChargesAccount.on('blur',function(){
        if(this.bankChargesAccount.getRawValue()==''){
            this.bankChargesAccount.clearValue();
            this.bankCharges.setValue('');
            this.bankCharges.disable();
            this.bankCharges.allowBlank=true;
            this.bankCharges.validate();
        }else{
            this.bankCharges.allowBlank=false;
        }
    },this);
        
    this.paidTo.addNewFn=this.addPaidTo.createDelegate(this);
        this.pmtRec = new Wtf.data.Record.create([
        {name: 'methodid'},
        {name: 'methodname'},
        {name: 'accountid'},
        {name: 'acccurrency'},
        {name: 'accountname'},
        {name: 'isIBGBankAccount', type:'boolean'},
        {name: 'isdefault'},
        {name: 'detailtype',type:'int'},
        {name: 'acccustminbudget'},
        {name: 'autopopulate'},
        {name: 'acccurrencysymbol'},
    ]);
    this.pmtStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.pmtRec),
        url : "ACCPaymentMethods/getPaymentMethods.do",
        baseParams:{
            mode:51
        }
    });
    
    this.pmtStore.load({params: {grouper: 'paymentTrans'}});
    
        this.pmtMethod= new Wtf.form.FnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.mp.payMethod"),
        name:"pmtmethod",
        store:this.pmtStore,
        id:"paymentMethod"+config.helpmodeid+this.id,
        valueField:'methodid',
        displayField:'methodname',
        allowBlank:this.type==3?false:true,
        disabled: this.readOnly,
        emptyText:(WtfGlobal.getLocaleText("acc.mp.selpayacc")),
        anchor:'90%',
        mode: 'local',
        triggerAction: 'all',
        typeAhead: true,
        forceSelection: true,
        hideLabel:this.typeValue!=3||Wtf.account.companyAccountPref.withoutinventory,
        hidden:this.typeValue!=3||Wtf.account.companyAccountPref.withoutinventory//,
    });
    this.pmtStore.on('load', this.setPMData, this);
    this.isChequePrint=false;
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.paymentmethod, Wtf.Perm.paymentmethod.edit))
    this.pmtMethod.addNewFn=this.addPaymentMethod.createDelegate(this)
    this.pmtMethod.on('select', this.addNewCreditRow1, this);
    this.pmtMethodAcc=new Wtf.form.TextField({
        name:"pmtmethodacc",
        disabled : true,
        id:"pmtmethodacc"+this.id,
        fieldLabel:WtfGlobal.getLocaleText("acc.field.PaymentAccount"),
        anchor:'90%',
        hideLabel:this.typeValue!=3||Wtf.account.companyAccountPref.withoutinventory,
        hidden:this.typeValue!=3||Wtf.account.companyAccountPref.withoutinventory
    });
    var typeArr = new Array();
    typeArr.push([Wtf.GSTJEType.None, 'None']);
    if(Wtf.istcsapplicable){
        typeArr.push([Wtf.GSTJEType.TCS, Wtf.GSTRJETYPE.TCS]);
    }
    if(Wtf.istdsapplicable){
        typeArr.push([Wtf.GSTJEType.TDS, Wtf.GSTRJETYPE.TDS]);
    }
    typeArr.push([Wtf.GSTJEType.ITC, Wtf.GSTRJETYPE.ITC]);
    
    this.gstrType = new Wtf.data.SimpleStore({
        fields: ['typeid', 'name'],
        data: typeArr
    });
    this.gstrTypeCmb = new Wtf.form.ComboBox({
        triggerAction: 'all',
        mode: 'local',
        valueField: 'typeid',
        hidden: true,//!WtfGlobal.isIndiaCountryAndGSTApplied() || this.type != 1,
        hideLabel: true,//!WtfGlobal.isIndiaCountryAndGSTApplied() || this.type != 1,
        displayField: 'name',
        store: this.gstrType,
        fieldLabel: WtfGlobal.getLocaleText("acc.gstrjetype"),
        id: "gstrType" + this.id,
        anchor:'90%',
        typeAhead: true,
        forceSelection: true,
        name: 'gstrType',
        hiddenName: 'gstrType'
    });
     this.southCenterTplSummary=new Wtf.XTemplate(
            "<div> &nbsp;</div>",  //Currency:
            '<tpl if="editable==true">',
            "<b>"+WtfGlobal.getLocaleText("acc.invoice.msg8")+"</b>",  //Applied Exchange Rate for the current transaction:
        "<div style='line-height:18px;padding-left:30px;'>1 {foreigncurrency} "+WtfGlobal.getLocaleText("acc.inv.for")+" = {revexchangerate} {basecurrency} "+WtfGlobal.getLocaleText("acc.inv.hom")+". <br/>",
        this.isViewTemplate?"</div>":WtfGlobal.getLocaleText("acc.invoice.msg9")+" </div> <div style='padding-left:30px;padding-top:5px;padding-bottom:10px;'><a class='tbar-link-text' href='#' onClick='javascript: editInvoiceExchangeRates(\""+this.id+"\",\"{foreigncurrency}\",\"{basecurrency}\",\"{revexchangerate}\",\"foreigntobase\")'wtf:qtip=''>{foreigncurrency} to {basecurrency}</a></div>",
        "<div style='line-height:18px;padding-left:30px;'>1 {basecurrency} "+WtfGlobal.getLocaleText("acc.inv.hom")+" = {exchangerate} {foreigncurrency} "+WtfGlobal.getLocaleText("acc.inv.for")+". <br/>",
        this.isViewTemplate?"</div>":WtfGlobal.getLocaleText("acc.invoice.msg9")+"  </div> <div style='padding-left:30px;padding-top:5px;'><a class='tbar-link-text' href='#' onClick='javascript: editInvoiceExchangeRates(\""+this.id+"\",\"{basecurrency}\",\"{foreigncurrency}\",\"{exchangerate}\",\"basetoforeign\")'wtf:qtip=''>{basecurrency} to {foreigncurrency}</a></div>",
            '</tpl>'
        );
    
    this.southCenterTpl=new Wtf.Panel({
        border:false,
        disabled:this.readOnly,
        disabledClass:"newtripcmbss",
        html:this.southCenterTplSummary.apply({basecurrency:WtfGlobal.getCurrencyName(),exchangerate:'x',foreigncurrency:"Foreign Currency", editable:false})
    });
    
    this.accountBalanceTplSummary=new Wtf.XTemplate(
        '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',            
        '<div><hr class="templineview"></div>',
        '<div>',
        '<table width="100%">'+
        '<tr>'+
        '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.invoiceList.accName")+': </b></td><td style="width:30%;"><span style="width: auto;float: left;display:block;">'+Wtf.util.Format.ellipsis('{accounatName}',20)+'</span></td>'+        
        '<td style="width:12%;"><b>'+WtfGlobal.getLocaleText("acc.field.Balance")+'</b></td><td style="width:30%;word-break: break-all;"><span style="width: auto;float: left;display:block;">'+Wtf.util.Format.ellipsis('{endingBalance}',20)+'</span></td>'+        
        '</tr>'+
        '</table>'+
        '</div>',            
        '<div><hr class="templineview"></div>',                        
        '</div>'
    );
        
    this.accountBalanceTpl=new Wtf.Panel({
        //id:'productDetailsTpl',
        border:false,
        baseCls:'tempbackgroundview',
        width:'55%',
        hidden:false,
        html:this.accountBalanceTplSummary.apply({accounatName:"&nbsp;&nbsp;&nbsp;&nbsp;",endingBalance:"&nbsp;&nbsp;&nbsp;&nbsp;"})
    }); 
    
    
    this.tplSummary=new Wtf.XTemplate(
        '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',            
        '<div><hr class="templineview"></div>',
        '<div>',
        '<table width="100%">'+
        '<tr>'+
        '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.field.AccountBalance")+'<br>'+WtfGlobal.getLocaleText("acc.field.InBaseCurrency")+'</b></td><td style="width:30%;"><span style="width: auto;float: left;display:block;">'+Wtf.util.Format.ellipsis('{balanceInBase}',20)+'</span></td>'+        
        '</tr>'+
        '</table>'+
        '</div>',            
        '<div><hr class="templineview"></div>',                        
        '</div>'
    );
        
    this.southCalTemp=new Wtf.Panel({
        border:false,
        baseCls:'tempbackgroundview',
        html:this.tplSummary.apply({balanceInBase:WtfGlobal.currencyRenderer(0)})
    });
    
    this.southPanel=new Wtf.Panel({
        region:'south',
        border:false,
        style:'padding:0px 10px 10px 10px',
        layout:'border',
        disabledClass:"newtripcmbss",
        autoScroll:true,
        height:200,
        items:[{
            region:'center',
            border:false,
            autoHeight:true,
            items:[this.accountBalanceTpl,this.southCenterTpl]
        },{
            region:'east',
            border:false,
            id: this.id + 'southEastPanel',
//            cls:'bckgroundcolor',
            bodyStyle:'padding:10px',
            width:350,
            items:[this.southCalTemp]
        }]
    });
    this.moduleTemplateSection();
    this.includeInGst= new Wtf.form.Checkbox({
        name:'includeingstreport',
        fieldLabel:(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA)?WtfGlobal.getLocaleText("acc.JE.includeInTax"):WtfGlobal.getLocaleText("acc.JE.includeInGST"),
        checked:false,
        disabled:this.typeValue!=1 || this.readOnly,
        hidden:(this.typeValue!=1 || Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA),
        hideLabel: (this.typeValue!=1 || Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA),
        cls : 'custcheckbox',
        width: 10
    });
    this.generateCNDN= new Wtf.form.Checkbox({
        name:'generateCNDN',
        fieldLabel:WtfGlobal.getLocaleText("acc.je.generateCNDN"),
        checked:false,
        disabled:this.readOnly,
        hideLabel:this.typeValue!=2||Wtf.account.companyAccountPref.withoutinventory,
        hidden:this.typeValue!=2||Wtf.account.companyAccountPref.withoutinventory,
        cls : 'custcheckbox',
        width: 10
    });
    
     this.generateCNDN.on('check', function(checkbox, checked){
        if (this.isLoadAccountStoreInEditPartyJE) { //This flag will be false once when editing party JE, Otherwise it will be true   
            var count = this.grid.getStore().getCount();
            var rec = this.grid.getStore().getAt(count - 1);
            if (count > 1 || (rec.data.accountid != "" && rec.data.accountid != undefined)) {
                if(this.typeValue == 2){            //Party journal entry
                    Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.field.ResetAccount"),
                    msg: this.generateCNDN.getValue()?WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoresetaccount"):WtfGlobal.getLocaleText("acc.field.resetAccount"),
                    buttons: Wtf.MessageBox.YESNO,
                    icon: Wtf.MessageBox.INFO,
                    width: 300,
                    scope: this,
                    fn: function (btn) {
                        if (btn == "yes") {
                            this.accountBalanceTplSummary.overwrite(this.accountBalanceTpl.body, {accounatName: "&nbsp;&nbsp;&nbsp;&nbsp;", endingBalance: "&nbsp;&nbsp;&nbsp;&nbsp;"});    //ERP-5075
                            this.tplSummary.overwrite(this.southCalTemp.body, {balanceInBase: "&nbsp;&nbsp;&nbsp;&nbsp;"}); //ERP-5075
                            if (this.generateCNDN.getValue()){
                                this.cmbAccount.store.baseParams.ignoreBankAccounts = true;
                            } else {
                                this.cmbAccount.store.baseParams.ignoreBankAccounts = ''; 
                            }                            
                            this.cmbAccount.store.baseParams.ignoreCashAccounts = true;
                            this.cmbAccount.store.baseParams.ignoreGLAccounts = true;
                            this.cmbAccount.store.baseParams.ignoreGSTAccounts = true;
                            this.grid.store.removeAll();
                            this.accountStore.removeAll();
                            this.addNewRow();
                            this.iscallsetInitVaueMethodOnLoad = false;
//                            this.accountStore.load();
                            if (checked) {
                                this.loadCNDNSequenceFormatStore();
                            this.showCNDN();
                            } else {
                                this.hideCNDN();
                            }
                        } else if (btn == "no") {
                            this.generateCNDN.setValue(!this.generateCNDN.getValue());
                            Wtf.MessageBox.hide();
                        }
                    }
                });
                }
             
            } else {
                if (this.generateCNDN.getValue()) {//This part of code will be executed when generate CN/DN is true
                    this.cmbAccount.store.baseParams.ignoreBankAccounts = true;
                    this.cmbAccount.store.baseParams.ignoreCashAccounts = true;
                    this.cmbAccount.store.baseParams.ignoreGLAccounts = true;
                    this.cmbAccount.store.baseParams.ignoreGSTAccounts = true;
                    this.iscallsetInitVaueMethodOnLoad = false;
//                    this.accountStore.load();
                    this.loadCNDNSequenceFormatStore();
                    this.showCNDN();
                } else {
                    if (this.type == 2) {
                        this.cmbAccount.store.baseParams.ignoreBankAccounts = '';
                        this.cmbAccount.store.baseParams.ignoreCashAccounts = true;
                        this.cmbAccount.store.baseParams.ignoreGSTAccounts = true;
                        this.cmbAccount.store.baseParams.ignoreGLAccounts = true;
                    }
                    if (this.typeValue == 3) {
                        this.cmbAccount.store.baseParams.ignoreGLAccounts = true;
                        this.cmbAccount.store.baseParams.ignoreGSTAccounts = true;
                        this.cmbAccount.store.baseParams.ignoreCustomer = true;
                        this.cmbAccount.store.baseParams.ignoreVendor = true;
                    }
                    if (this.type == 1) {
                        this.cmbAccount.store.baseParams.ignoreCustomer = true;
                        this.cmbAccount.store.baseParams.ignoreVendor = true;
                    }
                    this.iscallsetInitVaueMethodOnLoad = false;
//                    this.accountStore.load();
                    this.hideCNDN();
                    if (this.isEdit || this.copyInv) {
                        var totaldr = this.calGridTotal('dramount');
                        var totalcr = this.calGridTotal('cramount');
                        this.addNewRow(totalcr - totaldr);
                    }

                }
            }

        } else {
            this.isLoadAccountStoreInEditPartyJE = true;
            if (checked) {
                this.loadCNDNSequenceFormatStore();
            this.showCNDN();
            } else {
                this.hideCNDN();
        }
        }
    }, this);
    
    this.Currency.on('select', function(comb,rec,index){
        this.externalcurrencyrate=0; 
        this.currencyid = rec.data.currencyid;
        this.requestBaseFlag = false;//request account balance in base currency
        this.requestFlag = false;//request account balance in document currency
        this.setCurrencyid(rec.data.currencyid,1,rec.data.symbol);
//        this.externalcurrencyrate=rec.data.exchangerate; // Set selected currency exchange rate.
        this.currencychanged = true;
        this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.entryDate.getValue()),tocurrencyid:this.Currency.getValue()}});
        this.accountBalanceTplSummary.overwrite(this.accountBalanceTpl.body,{accounatName:"&nbsp;&nbsp;&nbsp;&nbsp;",endingBalance:"&nbsp;&nbsp;&nbsp;&nbsp;"});
        this.updateCNDNAmount();
    }, this);
      
    this.CostCenter= new Wtf.form.ExtFnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.costCenter"),  //"Cost Center",
        hiddenName:"costcenter",
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
        anchor:'90%',
        triggerAction:'all',
        addNewFn:this.addCostCenter,
        disabledClass:"newtripcmbss",
        disabled: this.readOnly||this.isViewTemplate,
        scope:this
    });


    this.chequeDetails = new Wtf.account.ChequeDetails({
        disabledClass:"newtripcmbss",
        baseCls:'bodyFormat',
        type:this.typeValue,
        autoHeight:true,
        disabled: this.readOnly||this.isViewTemplate,
        hidden:true,
        style:'margin:10px 10px;',
        isEdit:this.isEdit,
        isCopy:this.copyInv,
        readOnly: this.readOnly,    //ERP-38828
        isFromPaymentModule:true,
        chequeNumber:((this.isEdit||this.copyInv) && this.jeDetails.data.chequeNumber != undefined)?this.jeDetails.data.chequeNumber:"",
        paymentMethodAccountId:((this.isEdit||this.copyInv) && this.jeDetails.data.pmtmethodaccountid != undefined)?this.jeDetails.data.pmtmethodaccountid:"",
        chequeSequenceFormatID:((this.isEdit||this.copyInv) && this.jeDetails.data.chequesequenceformatid != undefined  )?this.jeDetails.data.chequesequenceformatid:"",
        id:this.id+'southform',
        border:false
    });
    
    this.chequeDetails.on('beforeshow',function(obj){
        // getting bank account id from grid store
        var bankAccountId = "";
        for(var i=0;i<this.gridStore.getCount();i++){
            var rec=this.gridStore.getAt(i);
            if(!rec.data['debit'] && rec.data['masterTypeValueOfAccount'] == 3){
                bankAccountId = rec.data['mappedaccountid'];
            }
        }
        
        if(!this.chequeDetails.isVisible() && this.newPanel && this.newPanel.items && this.newPanel.items.items[0] && this.newPanel.items.items[0].getInnerHeight()){
            this.newPanel.items.items[0].setHeight(this.newPanel.items.items[0].getInnerHeight()+210);
            this.newPanel.doLayout();
        }
    var bankName="";
        if ((this.cmbAccount.lastSelectionText == "" || this.cmbAccount.lastSelectionText == undefined) && this.jeDetails) {
            bankName = this.jeDetails.data.bankName;
        } else {
            bankName = this.cmbAccount.lastSelectionText;
    }
        obj.setNextChequeNumber(bankAccountId, bankName);
    },this);
    
this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            compId:"northForm"+this.id,
            autoHeight: true,
            parentcompId:this.id,
            style : 'margin-left:10px;',
//            baseCls:'bodyFormat',
            moduleid: 24,
//            layout:'form',
            disabledClass:"newtripcmbss",
            disabled: this.readOnly||this.isViewTemplate,
            isEdit:(this.isCopyFromTemplate!=undefined)?this.isCopyFromTemplate? true :this.isEdit:this.isEdit,
            record: this.jeDetails
        });

        this.sequenceFormatStoreCN = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            //        url: Wtf.req.account +'CompanyManager.jsp',
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:"autocreditmemo"
            }
        });
        this.sequenceFormatStoreCN.on('load',function(){
            if(this.sequenceFormatStoreCN.getCount()>0){
                if(this.isEdit && this.jeDetails.data.partlyJeEntryWithCnDn){//edit case of Party JE With CNDN
                    if(this.jeDetails.data.CNsequenceformatid!="" && this.jeDetails.data.CNsequenceformatid!=undefined){
                        this.sequenceFormatComboboxCN.setValue(this.jeDetails.data.CNsequenceformatid);
                        this.no.setValue(this.jeDetails.data.cnNumber);
                        this.no.disable();
                    } else {
                        this.sequenceFormatComboboxCN.setValue("NA");
                        this.no.setValue(this.jeDetails.data.cnNumber);
                    }    
                    this.sequenceFormatComboboxCN.disable();
                    if (this.readOnly) {
                        this.no.disable();
                    }
                } else {
                    var count=this.sequenceFormatStoreCN.getCount();
                    for(var i=0;i<count;i++){
                        var seqRec=this.sequenceFormatStoreCN.getAt(i)
                        if(seqRec.json.isdefaultformat=="Yes"){
                            this.sequenceFormatComboboxCN.setValue(seqRec.data.id) 
                            break;
                        }
                    }
                    if(this.sequenceFormatComboboxCN.getValue()!=""){
                        this.getNextSequenceNumberCN(this.sequenceFormatComboboxCN);
                    } else{
                        this.no.setValue("");
                        WtfGlobal.hideFormElement(this.no);
                    }
                }
            }
            this.isCNSequenceFormatStoreLoaded = true;
        },this);
         
       this.sequenceFormatComboboxCN = new Wtf.form.ComboBox({            
        triggerAction:'all',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormatCN"),
        valueField:'id',
        displayField:'value',
        store:this.sequenceFormatStoreCN,
//        hideLabel:this.typeValue!=2,
//        hidden:this.typeValue!=2,
         anchor:'90%',
        typeAhead: true,
        forceSelection: true,
        name:'sequenceformatCN',
        hiddenName:'sequenceformatCN',
        listeners:{
            'select':{
                fn:this.getNextSequenceNumberCN,
                scope:this
            }
        }
            
    });
    
     this.no=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.cnList.gridNoteNo")+"*",
            name: 'number',
            scope:this,
            maxLength:45,
//            hideLabel:this.typeValue!=2,
//            hidden:this.typeValue!=2,
            anchor:'90%',
            allowBlank:false
        });

        this.sequenceFormatStoreDN = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            //        url: Wtf.req.account +'CompanyManager.jsp',
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:"autodebitnote"
            }
        });
        this.sequenceFormatStoreDN.on('load',function(){
            if(this.sequenceFormatStoreDN.getCount()>0){
                if(this.isEdit && this.jeDetails.data.partlyJeEntryWithCnDn){//edit case of PartyJE With CNDN
                    if(this.jeDetails.data.DNsequenceformatid!="" && this.jeDetails.data.DNsequenceformatid!=undefined){
                        this.sequenceFormatComboboxDN.setValue(this.jeDetails.data.DNsequenceformatid);
                        this.noDN.setValue(this.jeDetails.data.dnNumber);
                        this.noDN.disable();
                    } else {
                        this.sequenceFormatComboboxDN.setValue("NA");
                        this.noDN.setValue(this.jeDetails.data.dnNumber);
                    }   
                    this.sequenceFormatComboboxDN.disable();
                    if (this.readOnly) {
                        this.noDN.disable();
                    }
                } else {            
                    var count=this.sequenceFormatStoreDN.getCount();
                    for(var i=0;i<count;i++){
                        var seqRec=this.sequenceFormatStoreDN.getAt(i)
                        if(seqRec.json.isdefaultformat=="Yes"){
                            this.sequenceFormatComboboxDN.setValue(seqRec.data.id) 
                            break;
                        }
                    }
                    if(this.sequenceFormatComboboxDN.getValue()!=""){
                        this.getNextSequenceNumberDN(this.sequenceFormatComboboxDN);
                    } else{
                        this.noDN.setValue("");
                        WtfGlobal.hideFormElement(this.noDN);
                    }
                }
            }
            this.isDNSequenceFormatStoreLoaded = true;
        },this);
       this.sequenceFormatComboboxDN = new Wtf.form.ComboBox({            
        triggerAction:'all',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormatDN"),
        valueField:'id',
        displayField:'value',
        store:this.sequenceFormatStoreDN,
//        hideLabel:this.typeValue!=2,
//        hidden:this.typeValue!=2,
        anchor:'90%',
        typeAhead: true,
        forceSelection: true,
        name:'sequenceformatDN',
        hiddenName:'sequenceformatDN',
        listeners:{
            'select':{
                fn:this.getNextSequenceNumberDN,
                scope:this
            }
        }
            
    });
    
     this.noDN=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.dnList.gridNoteNo")+"*",
            name: 'numberDN',
            scope:this,
            maxLength:45,
//            hideLabel:this.typeValue!=2,
//            hidden:this.typeValue!=2,
            anchor:'90%',
            allowBlank:false
        });
    this.Memo=new Wtf.form.TextArea({
            fieldLabel: Wtf.account.companyAccountPref.descriptionType,  //'Memo',
            name: 'memo',
            anchor:'90%',
//            heigth:20,
            readOnly :this.readOnly||this.isViewTemplate,
            maxLength:2048,
            disabled:this.readOnly||this.isViewTemplate,
            xtype:'textarea',
            qtip:(this.jeDetails==undefined)?' ':this.jeDetails.data.memo,
            listeners: {
                render: function(c){
                    Wtf.QuickTips.register({
                        target: c.getEl(),
                        text: c.qtip
                    });
                }
            }
      });
    var itemArr=[];
    /**
     *this.isEditTemplate - push the template name and create as transaction check box in array
     */
      if(this.isTemplate || this.isEditTemplate){
            itemArr.push(this.moduleTemplateName, this.createAsTransactionChk);
      }
    itemArr.push(this.templateModelCombo,this.sequenceFormatCombobox,this.jeNo,this.CostCenter,this.generateCNDN,this.includeInGst);
   
    var itemArrCol3=[]; // ERP-13575
      if(!(this.typeValue!=3||Wtf.account.companyAccountPref.withoutinventory)){
            itemArrCol3.push(this.pmtMethod);   
            itemArrCol3.push(this.pmtMethodAcc);   
      }
    itemArrCol3.push(this.Memo,this.sequenceFormatComboboxDN,this.noDN);     
    
    this.JournalNorthForm=new Wtf.form.FormPanel({
        disabledClass: "newtripcmbss",
        items: [{
                layout: 'form',
                baseCls: 'northFormFormat',
                cls: "visibleDisabled",
                items: [{
//                        cls:'northFormFormat',
                        labelWidth: 140,
                        layout: 'column',
                        border: false,
                        autoHeight: true,
                        defaults: {border: false},
                        itemCls: 'JEntryform',
                        disabledClass: "newtripcmbss",
                        items: [{
                                layout: 'form',
                                columnWidth: 0.33,
                                items: itemArr
                            }, {
                                layout: 'form',
                                columnWidth: 0.33,
                                items: [this.entryDate, this.Currency, this.paidTo, this.gstrTypeCmb,this.bankChargesAccount, this.bankCharges, this.sequenceFormatComboboxCN, this.no]
                            }, {
                                layout: 'form',
                                columnWidth: 0.3,
                                items: itemArrCol3
//            [this.pmtMethod,this.pmtMethodAcc,this.Memo=new Wtf.form.TextArea({
//                fieldLabel: Wtf.account.companyAccountPref.descriptionType,  //'Memo',
//                name: 'memo',
//                anchor:'90%',
//                heigth:20,
//                readOnly :this.readOnly||this.isViewTemplate,
//                maxLength:2048,
//                xtype:'textarea',
//              qtip:(this.jeDetails==undefined)?' ':this.jeDetails.data.memo,
//            listeners: {
//                render: function(c){
//                    Wtf.QuickTips.register({
//                        target: c.getEl(),
//                        text: c.qtip
//                    });
//                }
//            }
//            }),this.sequenceFormatComboboxDN,this.noDN]
                            }]
                    }, this.tagsFieldset]
            }]
    });
    this.JournalNorthForm.on('afterlayout', function () {
        if (this.generateCNDN.getValue()) {
            this.showCNDN();
        } else {
            this.hideCNDN();
        }
    }, this);
    this.createGrid();
     this.btnPanel = new Wtf.Panel({
        style: 'padding: 10px 10px 0;',
        border : false,
        autoScroll: true,
        items : [{
                 xtype: 'button',
                id: "deleteButton"+ this.id,
                disabled: true,
                cls: 'setlocationwarehousebtn',
                text: WtfGlobal.getLocaleText("acc.common.deleteselected"),
                hidden: this.readOnly || (this.typeValue==Wtf.fund_transafer_journal_entry),
                handler: this.deleteSelectedRecord.createDelegate(this)
              }] 
      });
    
    this.newPanel=new Wtf.Panel({
        border:false,
        autoScroll : true,
        autoHeight:true,
        region : 'center',
        items:[{
            region : "north",
            autoHeight:true,
            id : "northForm"+this.id,
            border:false,
            items:[this.JournalNorthForm,this.chequeDetails]
        },this.btnPanel,this.grid,this.southPanel]
    });
this.savePrintBttn=new Wtf.Toolbar.Button({
    text:WtfGlobal.getLocaleText("acc.common.savePrintBtn"),  //'Save',
    scope:this,
    id:"printsave"+config.helpmodeid+this.id,
    iconCls :getButtonIconCls(Wtf.etype.save),
    hidden:this.readOnly,
    handler: function () {
        this.saveAndCreateNewFlag = false;
        this.savePrintCheque();
    }
});
    this.savencreateBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNew"),
        tooltip: WtfGlobal.getLocaleText("acc.field.SaveAndCreateNewToolTip"),
        scope: this,
        hidden: (this.isEdit) || this.readOnly,
        id: "savencreate" + config.helpmodeid + this.id,
        iconCls: getButtonIconCls(Wtf.etype.save),
        handler: function () {
            this.saveAndCreateNewFlag = true;
            this.checkTotal();
        }
    });
    this.recurringBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.RecurringJE"), //'Recurring Payment',
        scope: this,
        id: "RecurringJE" +this.id,
        iconCls: getButtonIconCls(Wtf.etype.copy),
        hidden: !(this.typeValue==Wtf.normal_journal_entry||this.typeValue==Wtf.fund_transafer_journal_entry), //Type : 1-Normal JE, 2-Party JE, 3-Fund Transfer JE
        disabled : true,
        handler: function() {
            if(this.recordForRecurring){
                if(this.typeValue==Wtf.normal_journal_entry||this.typeValue==Wtf.fund_transafer_journal_entry) {
                    var moduleid = config.moduleid;
                    this.enabledisable=false;
                    callRepeatedJEWindow(false,this.recordForRecurring, false, this.isEdit, this.journalentryid, moduleid);
                    Wtf.getCmp('RepeatedJEWin').on('cancel',function(config){                        
                       this.recurringBtn.enable();
                    },this);   
                    this.recurringBtn.disable();
                }
            }
        }
    });
    this.recurringBtn.disable();
    this.saveBttn = new Wtf.Toolbar.Button({text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
        scope: this,
        hidden: this.isViewTemplate,
        iconCls: getButtonIconCls(Wtf.etype.save),
        handler: function(){
            this.saveAndCreateNewFlag = false;
            this.checkTotal();
        }
    })
    Wtf.apply(this,{
        items:[this.newPanel],
        bbar: [
            this.saveBttn,this.savencreateBttn,this.savePrintBttn, this.recurringBtn]
    });
    this.entryDate.on('change',this.onDateChange,this);
    this.currencyStore.on('load',this.changeTemplateSymbol,this);
    this.grid.on('drtotal',this.updateDrTotal,this);
    this.grid.on('crtotal',this.updateCrTotal,this);
    Wtf.account.JournalEntryPanel.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true,
        'jeupdate' : true
    });
    
    this.on('resize', function (panel) {
        panel.doLayout();
        if (panel.grid) {
            panel.grid.doLayout();
            panel.grid.getView().refresh();
        }
    }, this);
}
Wtf.extend(Wtf.account.JournalEntryPanel,Wtf.account.ClosablePanel,{
    onRender:function(config){
        Wtf.account.JournalEntryPanel.superclass.onRender.call(this,config);
        if(!((this.isEdit || this.copyInv) && ((this.typeValue==2 && this.jeDetails.data.partlyJeEntryWithCnDn) || this.typeValue==3) )){// in case of fund transfer edit case controll will not go in side
            this.grid.on('render',this.addNewRow.createDelegate(this,[0]),this);
        }
        this.grid.on('afteredit',this.updateRow,this);
        this.grid.on('beforeedit',this.checkEditable,this);
        this.grid.on('populateDimensionValue',this.populateDimensionValueingrid,this);
        this.currencyid = WtfGlobal.getCurrencyID();
        if(this.isEdit || this.copyInv || this.isCopyFromTemplate || this.vatPaymentFlag){
            this.isClosable=false          // Set Closable flag for edit and copy case
            this.currencyStore.load(); 
            if(Wtf.StoreMgr.containsKey("FormCostCenter")){
                this.CostCenter.setValue(this.jeDetails.data.costcenter);
            } else {
                Wtf.FormCostCenterStore.on("costcenterloaded", function(){
                    if(Wtf.getCmp(this.id)) {
                        this.CostCenter.setValue(this.jeDetails.data.costcenter);
                    }
                }, this);
                chkFormCostCenterload();
            }
            this.currencyStoreCMB.on('load', function(){
                var recordids = "";
                for (var i = 0; i < this.jeDetails.data.jeDetails.length; i++) {
                    var record = this.jeDetails.data.jeDetails[i];
                    if (!record.isbankcharge) {
                        if (record.customerVendorName != undefined && record.customerVendorName != "") {
                            recordids += "'" + record.customerVendorId + "',";
                } else {
                            recordids += "'" + record.accountid + "',";
                }                
                    }
                    
                    if (record.isbankcharge && this.bankChargesAccount && this.bankChargesAccountStore) {
                        this.bankChargesAccountStore.load({params: {recordids: "'" + record.accountid + "'"}, scope: this});
                    }
                }
                recordids = recordids.substr(0, recordids.length - 1);

                this.accountStore.on('load', this.initialAccountStoreLoad, this);
                
                this.accountStore.baseParams.ignoreBankAccounts = false;
                this.accountStore.baseParams.ignoreCashAccounts = false;
                this.accountStore.baseParams.ignoreGLAccounts = false;
                this.accountStore.baseParams.ignoreGSTAccounts = false;
                
                this.accountStore.load({params: {recordids: recordids}, scope: this});
                
                if (this.typeValue == 2 && this.jeDetails.data.partlyJeEntryWithCnDn) {
                    this.cmbAccount.store.baseParams.ignoreBankAccounts = true;
                    this.cmbAccount.store.baseParams.ignoreCashAccounts = true;
                    this.cmbAccount.store.baseParams.ignoreGLAccounts = true;
                    this.cmbAccount.store.baseParams.ignoreGSTAccounts = true;
                }
                
            }, this);
        } else {
            chkFormCostCenterload();
        }
        if (WtfGlobal.isIndiaCountryAndGSTApplied() && Wtf.isitcapplicable && this.isITCJE && !this.isEdit && this.itcjeconfig) {
            /**
             * If ITC JE then populate default account rows,memo,Invoice NO and Ids.
             */
            this.addNewRowsForITC();
            this.gstrTypeCmb.setValue(Wtf.GSTJEType.ITC);
            this.itctransactionids=this.itcjeconfig.itctransactionids;
            this.Memo.setValue(this.itcjeconfig.invoiceno);
            this.gstrTypeCmb.disable();
        }
        if(this.isTemplate){
            this.jeNo.setValue("");
            this.jeNo.disable();
            this.sequenceFormatCombobox.disable();
        }
          this.savePrintBttn.hide();      
    },
    setPMData:function(){
        if (this.isEdit || this.copyInv || this.readOnly) {
            if (this.jeDetails != undefined && this.jeDetails.data != undefined && this.jeDetails.data.typeValue == 3) {
                this.pmtMethod.setValue(this.jeDetails.data.pmtmethod);
            }
        }
    },
    // This function will be called only on view, edit and copy case
    initialAccountStoreLoad: function (store, recArr) {
        this.cmbAccount.store.add(recArr);
        this.setInitValues();
        this.accountStore.un('load', this.initialAccountStoreLoad, this);
    },
    
    loadCNDNSequenceFormatStore: function () {
        if (!this.isCNSequenceFormatStoreLoaded) {
            this.sequenceFormatStoreCN.load();
        }
        if (!this.isDNSequenceFormatStoreLoaded) {
            this.sequenceFormatStoreDN.load();
        }
    },
    
    moduleTemplateSection:function(){
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
            },this.moduleTemplateRecord),
            url : "ACCCommon/getModuleTemplate.do",
            baseParams:{
                moduleId:24
            }
        });
        
        this.moduleTemplateStore.on('load', function(store){
             /*
             isAfterSaveNCreateNew is set as true if save and create new button is clicked
              */
          this.isAfterSaveNCreateNew=( this.isAfterSaveNCreateNew!=undefined)? this.isAfterSaveNCreateNew:false;
              /*
             *Loads the default Template Id for the module  
             **/
            var defaultId=store.reader.jsonData.defaultId;
            if( this.templateModelCombo.getValue()== ""&&defaultId!=undefined&&!this.isCopyFromTemplate&&!this.isEdit&&!this.isView&&!this.isCopyInvoice&&!this.isTemplate){
                this.templateModelCombo.setValue(defaultId);
                this.templateId=defaultId;
                var templaterec = WtfGlobal.searchRecord(this.moduleTemplateStore, this.templateId, 'templateId');
                if(templaterec!=undefined ){
                    this.templatename =templaterec.data.templateName ;
                    this.moduleTemplateName.setValue(this.templatename);
                }
                this.templateModelCombo.fireEvent('select',this);
            }else  if(this.isAfterSaveNCreateNew==true)
            {this.templateId=defaultId;
                this.templateModelCombo.setValue(this.templateId);
                this.templateModelCombo.fireEvent('select',this);
                var templaterec = WtfGlobal.searchRecord(this.moduleTemplateStore, this.templateId, 'templateId');
                if(templaterec!=undefined ){
                    this.templatename =templaterec.data.templateName ;
                    this.moduleTemplateName.setValue(this.templatename);
                }
            }
           if(this.isCopyFromTemplate && this.templateId!= undefined &&!this.isAfterSaveNCreateNew){
                this.templateModelCombo.setValue(this.templateId);
                /** 
                 *get the template name from store using templateID
                 */
                var templaterec = WtfGlobal.searchRecord(this.moduleTemplateStore, this.templateId, 'templateId');
                if(templaterec!=undefined ){
                    this.templatename =templaterec.data.templateName ;
                }else{
                    this.templatename="";
                }
            }     
        },this);
        
        
        this.templateModelCombo= new Wtf.form.FnComboBox({
            fieldLabel:(this.isViewTemplate?'Template Name': WtfGlobal.getLocaleText("acc.field.SelectTemplate")),
            id:"templateModelCombo"+this.id,
            store: this.moduleTemplateStore,
            valueField:'templateId',
            displayField:'templateName',
            hideTrigger:this.isViewTemplate,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.invoice.grid.template.emptyText"),
            mode: 'local',
            typeAhead: true,
            hidden:this.isTemplate ||  this.type != 1 || this.isEdit ||  this.isEditTemplate ,//Hide the tempate combo in create, edit ans view case 
            hideLabel:this.isTemplate || this.type != 1 ||this.isEdit || this.isEditTemplate,
            forceSelection: true,
            selectOnFocus:true,
            addNoneRecord: true,
            anchor:'90%',
            triggerAction:'all',
            scope:this,
            listeners:{
                'select':{
                    fn:function(){
                        if(this.templateModelCombo.getValue() != ""){
                            this.loadingMask = new Wtf.LoadMask(document.body,{
                                msg : 'Loading...'
                            });
                            this.loadingMask.show();
                            var templateId = this.templateModelCombo.getValue();
                            var recNo = this.moduleTemplateStore.find('templateId', templateId);
                            var rec = this.moduleTemplateStore.getAt(recNo);
                            var moduleId = rec.get('moduleRecordId');
                            this.SelectedTemplateStore.load({
                                params:{
                                    billid:moduleId,
                                    isForTemplate:true
                                }
                            }); 
                      } else {
                          this.resetAll();
                      }
                    },
            scope:this            
                }
            }
        });
        
        this.moduleTemplateName = new Wtf.form.TextField({
            fieldLabel:'Template Name',
            name: 'moduletempname',
            hidden:!this.isTemplate,
            hideLabel:!this.isTemplate,
//            id:"moduletempname", // commented to resolve issue [ERP-1661]
            anchor:'90%',
            maxLength:50,
            scope:this,
            disabled:this.isViewTemplate,
            allowBlank:!this.isTemplate
        });
        
        this.createAsTransactionChk = new Wtf.form.Checkbox({
            fieldLabel : WtfGlobal.getLocaleText("acc.field.CreateTransactionAlso"),
            name:'createAsTransactionChkbox',
            hidden:!this.isTemplate || this.isViewTemplate || this.isEditTemplate, // for show only in template creation case and in edit template case
            hideLabel:!this.isTemplate || this.isViewTemplate || this.isEditTemplate,
            cls : 'custcheckbox',
            width : 10
        });
        
        this.createAsTransactionChk.on('check', function(){
            if(this.createAsTransactionChk.getValue()){
                this.createTransactionAlso = true;
                WtfGlobal.showFormElement(this.sequenceFormatCombobox);
                WtfGlobal.showFormElement(this.jeNo);
                var seqRec=this.sequenceFormatStore.getAt(0)
                this.sequenceFormatCombobox.setValue(seqRec.data.id);
                var count=this.sequenceFormatStore.getCount();
                for(var i=0;i<count;i++){
                    seqRec=this.sequenceFormatStore.getAt(i)
                    if(seqRec.json.isdefaultformat=="Yes"){
                        this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                        break;
                    }
                }
                this.getNextSequenceNumber(this.sequenceFormatCombobox);
                this.jeNo.allowBlank = false;
                this.jeNo.enable();
                this.sequenceFormatCombobox.enable();
            }else{
                this.createTransactionAlso = false;
                this.jeNo.disable();
                this.sequenceFormatCombobox.disable();
                this.sequenceFormatCombobox.reset();
                this.jeNo.setValue('');
                this.jeNo.allowBlank = true;
                WtfGlobal.hideFormElement(this.sequenceFormatCombobox);
                WtfGlobal.hideFormElement(this.jeNo);
            }
        },this);
        
        
        this.SelectedTemplateRec = Wtf.data.Record.create ([
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'companyname'},
        {name:'companyid'},
        {name:'eliminateflag'},
        {name:'deleted'},
        {name:'entrydate',type:'date'},
        {name:'memo'},
        {name:'jeDetails'},
        {name:'transactionID'},
        {name:'billid'},
        {name:'noteid'},
        {name:'NoOfJEpost'}, 
        {name:'NoOfRemainJEpost'},  
        {name:'type'},
        {name:'transactionDetails'},
        {name:'costcenter'},
        {name:'currencyid'},
        {name:'creditDays'},
        {name:'isRepeated'},
        {name:'childCount'},
        {name:'interval'},
        {name:'intervalType'},
        {name:'startDate', type:'date'},
        {name:'nextDate', type:'date'},
        {name:'expireDate', type:'date'},
        {name:'repeateid'},
        {name:'parentje'},
        {name:'isreverseje',type:'boolean'},
        {name:'reversejeno'},
        {name:'withoutinventory'},
        {name:'revaluationid'},
        {name:'externalcurrencyrate'},
        {name:'typeValue'},
        {name:'partlyJeEntryWithCnDn'}
    ]);
    
    this.SelectedTemplateStoreUrl = "ACCReports/getJournalEntry.do";
         
    this.SelectedTemplateStore = new Wtf.data.Store({
            url:this.SelectedTemplateStoreUrl,
            scope:this,
           baseParams:{
                deleted:false,
                nondeleted:false,
                consolidateFlag:false,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                isfavourite:false
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.SelectedTemplateRec)
        });
        
        this.SelectedTemplateStore.on('load', this.fillData,this);
        this.SelectedTemplateStore.on('loadexception', function(){
            this.loadingMask.hide();
        },this);
        this.moduleTemplateStore.load();
        
    },
    fillData : function(store){
    this.loadingMask.hide();
    var rec = store.getAt(0);
    this.openModuleTab(rec);
},

openModuleTab:function(formrec){
    var templateId = this.templateModelCombo.getValue();
    var recNo = this.moduleTemplateStore.find('templateId', templateId);
    var record = this.moduleTemplateStore.getAt(recNo);
    var moduleId = record.get('moduleRecordId');
    var billid=formrec.get("entryno");
    var label="From Template "+this.id;
    callJournalEntryTab(false, formrec,billid ,"1",templateId)
},


    onDateChange:function(a,val,oldval){
        this.val=val;
        this.oldval=oldval;
        this.externalcurrencyrate=0;
        this.exchangeratetype="";
        this.revexternalcurrencyrate=0;
        this.applyCurrencySymbol();
        if (this.Currency != undefined && this.Currency.getValue() != "") {
            this.externalcurrencyrate = 0;
            var rec = WtfGlobal.searchRecord(this.currencyStoreCMB, this.Currency.getValue(), 'currencyid');
            if (rec != undefined && rec.data != undefined) {
                this.currencyid = rec.data.currencyid;
                this.requestBaseFlag = false;//request account balance in base currency
                this.requestFlag = false;//request account balance in document currency
                this.setCurrencyid(rec.data.currencyid, 1, rec.data.symbol);
                this.currencychanged = true;
                this.currencyStore.load({params: {mode: 201, transactiondate: WtfGlobal.convertToGenericDate(this.entryDate.getValue()), tocurrencyid: this.Currency.getValue()}});
                this.accountBalanceTplSummary.overwrite(this.accountBalanceTpl.body, {accounatName: "&nbsp;&nbsp;&nbsp;&nbsp;", endingBalance: "&nbsp;&nbsp;&nbsp;&nbsp;"});
                this.updateCNDNAmount();
            }
        }
   },
   
    afterAccountSelection:function(cmb, rec, index){
        if (!WtfGlobal.searchRecord(this.accountStore, rec.data.accountid, 'accountid')) {
            this.accountStore.add(rec);
        }
        this.fetchAccountBaseAmount();
        var accRec=WtfGlobal.searchRecord(this.accountStore, this.cmbAccount.getValue(), 'accountid');
        var accid = accRec!=null ? accRec.get('mappedaccountid') : this.cmbAccount.getValue();
        var accName = this.cmbAccount.getRawValue();
        var currencyid = this.Currency.getValue();
        if((currencyid != undefined && currencyid != "") && (accid != undefined && accid != "")) {
            Wtf.Ajax.requestEx({
                url: "ACCReports/getAccountBalanceInSelectedCurrency.do",
                params: {
                    tocurrencyid:currencyid,
                    accountid:accid
                }
            },this,function(response, request){
                if(response.success) {
                    this.accountBalanceTplSummary.overwrite(this.accountBalanceTpl.body,{accounatName:accName,endingBalance:WtfGlobal.addCurrencySymbolOnly(getRoundedAmountValue(response.endingBalance).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL),this.symbol)});
        } else {
                    this.accountBalanceTplSummary.overwrite(this.accountBalanceTpl.body,{accounatName:accName,endingBalance:"&nbsp;&nbsp;&nbsp;&nbsp;"});
       }
            },function(response, request){
                this.accountBalanceTplSummary.overwrite(this.accountBalanceTpl.body,{accounatName:accName,endingBalance:"&nbsp;&nbsp;&nbsp;&nbsp;"});
    });
        } 
    },
    
     fetchAccountBalence:function(sm, rowIdx, rec){
        /* this.oldAccountid stores the account id which was previously selected.
         * If oldAccountid equals to newly selected account id then only request for account balance in document currency is sent*/ 
        if (this.oldAccountid === undefined && rec != undefined) {
            this.oldAccountid = rec.get('accountid');
        } else if (rec != undefined && this.oldAccountid !== rec.get('accountid')) {
            this.oldAccountid = rec.get('accountid');
            this.requestFlag = false;
        }
        this.fetchAccountBaseAmount(rec);
        var accId = rec.get('accountid');
        if(rec.get('mappedaccountid'))
            accId = rec.get('mappedaccountid');
        if(accId != undefined && accId != null && accId != ""){
            var accRec=WtfGlobal.searchRecord(this.accountStore, accId, 'accountid');
            var accName="";
            if(accRec)
                accName = accRec.get('accountname');
        }
        var currencyid = this.Currency.getValue();
        // this.requestFlag is used to check whether the request is already sent for selected accountid for document currency
        if((currencyid != undefined && currencyid != "") && (accId != undefined && accId != "") && !this.requestFlag) {
            Wtf.Ajax.requestEx({
                url: "ACCReports/getAccountBalanceInSelectedCurrency.do",
                params: {
                    tocurrencyid:currencyid,
                    accountid:accId
                }
            },this,function(response, request){
                if(response.success) {
                    this.accountBalanceTplSummary.overwrite(this.accountBalanceTpl.body,{accounatName:accName,endingBalance:WtfGlobal.addCurrencySymbolOnly(getRoundedAmountValue(response.endingBalance).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL),this.symbol)});
            } else {
                    this.accountBalanceTplSummary.overwrite(this.accountBalanceTpl.body,{accounatName:accName,endingBalance:"&nbsp;&nbsp;&nbsp;&nbsp;"});
            }
            },function(response, request){
                this.accountBalanceTplSummary.overwrite(this.accountBalanceTpl.body,{accounatName:accName,endingBalance:"&nbsp;&nbsp;&nbsp;&nbsp;"});
        });
        this.requestFlag=true; // if request sent for account balance in document currency then set flag =true as request is sent for the selected account id
        } 
    },
    
    fetchAccountBaseAmount:function(rec){
        var accId="";
        var accRec="";
        if(rec !=null && rec != undefined && rec != ""){
            accId = rec.get('accountid');
        }
        if(accId != undefined && accId != null && accId != ""){
            accRec=WtfGlobal.searchRecord(this.accountStore, accId, 'accountid');
        }else{
            accRec=WtfGlobal.searchRecord(this.accountStore, this.cmbAccount.getValue(), 'accountid');
        }
        /* this.oldAccountidbase stores the account id which was previously selected.
         * If oldAccountidbase equals to newly selected account id then only request for account balance in base currency is sent*/ 
        if (this.oldAccountidbase === undefined && accRec != undefined && accRec!="") {
            this.oldAccountidbase = accRec.get('accountid');
        } else if (accRec != undefined && accRec!="" && this.oldAccountidbase !== accRec.get('accountid')) {
            this.oldAccountidbase = accRec.get('accountid');
            this.requestBaseFlag = false;
        }
        var accid = accRec!=null ? accRec.get('mappedaccountid') : this.cmbAccount.getValue();
        var currencyid = WtfGlobal.getCurrencyID();
        /* Condition 1: accid!="" - Do not sent request if account is not selected
         * Condition 2: !this.requestFlag- this.requestFlag is used to check whether the request is already sent for selected accountid for document currency*/ 
        if(accid != undefined && accid != "" && !this.requestBaseFlag){
            Wtf.Ajax.requestEx({
                url: "ACCReports/getAccountBalanceInSelectedCurrency.do",
                params: {
                    tocurrencyid: currencyid,
                    accountid: accid
                }
            }, this, function (response, request) {
                if (response.success) {
                    this.tplSummary.overwrite(this.southCalTemp.body, {balanceInBase: WtfGlobal.addCurrencySymbolOnly(getRoundedAmountValue(response.endingBalance).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL), WtfGlobal.getCurrencySymbol())});
                } else {
                    this.tplSummary.overwrite(this.southCalTemp.body, {balanceInBase: "&nbsp;&nbsp;&nbsp;&nbsp;"});
                }
            }, function (response, request) {
                this.tplSummary.overwrite(this.southCalTemp.body, {balanceInBase: "&nbsp;&nbsp;&nbsp;&nbsp;"});
            });
            this.requestBaseFlag=true; // set flag=true as request for account balance in base is sent for selected account id
        }
    },

    changeTemplateSymbol: function() {
        if (this.currencychanged) {
            if (this.currencyStore.getCount() < 1 && WtfGlobal.convertToGenericDate(this.val) != undefined) {
                callCurrencyExchangeWindow();

                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3") + "<b>" + WtfGlobal.convertToGenericDate(this.val) + "</b>"], 0);
                this.Currency.setValue("");
            } else {
                this.updateFormCurrency();
            }
            this.currencychanged = false;
            /*Setting default currency to JE form at the time of creating any transaction */
        } else if (Wtf.account.companyAccountPref.currencyid && !this.isEdit && !this.isCopyFromTemplate && !this.isViewTemplate) {
            this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
        }else if(this.readOnly){
            this.applyCurrencySymbol();
        } 
    },
    
    updateFormCurrency:function(excludeUpdatingFlag){
       this.applyCurrencySymbol();
       if(this.typeValue==3){//Fund Transfer
            this.applyExchangeRateToGrid(excludeUpdatingFlag);
       }     
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
            if(parentname==FIND)
                return true;
             else
                return false
            })
       this.currencyid=this.Currency.getValue();
       return index;
    },
    
    applyCurrencySymbol:function(){
        var index=this.getCurrencySymbol();
        var rate=this.externalcurrencyrate;
        if(index>=0){
           rate=(rate==""?this.currencyStore.getAt(index).data.exchangerate:rate);
            this.symbol=  this.currencyStore.getAt(index).data.symbol;
//            this.Grid.setCurrencyid(this.currencyid,rate,this.symbol,index);
            this.applyTemplate(this.currencyStore,index);
       }
       return this.symbol;
    },
    
    applyTemplate:function(store,index){
        var editable=this.Currency.getValue()!=WtfGlobal.getCurrencyID()&&this.Currency.getValue()!=""//&&!this.isOrder;
        var exchangeRate = store.getAt(index).data['exchangerate'];
        if(this.externalcurrencyrate>0) {
            exchangeRate = this.externalcurrencyrate;
        } else if((this.isEdit || this.copyInv || this.isCopyFromTemplate) && this.jeDetails.data.externalcurrencyrate){
           // var externalCurrencyRate = this.jeDetails.data.externalcurrencyrate-0;//??[PS]
            if(this.externalCurrencyRate>0){
                exchangeRate = this.externalCurrencyRate;
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
    
    changeCurrencyStore:function(pronamearr){
        this.pronamearr=pronamearr;
        var currency=this.Currency.getValue();
        if(this.val=="")this.val=this.billDate.getValue();
        if(currency!=""||this.custChange)
            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.val),tocurrencyid:this.Currency.getValue()}});
        else
            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.val)}});
    },
    
    setInitValues:function(){
        if((this.isEdit ||this.copyInv || this.isCopyFromTemplate) && this.iscallsetInitVaueMethodOnLoad){
        if(this.templateModelCombo.getValue() != "" || this.isEdit ||this.copyInv){
            var currencyID = this.jeDetails.data.currencyid;
            var currencyRec=this.currencyStoreCMB.queryBy( function(rec){
                var parentname=rec.data['currencyid'];
                if(parentname==currencyID)
                    return true;
                else
                    return false
            });        
            if(this.isEdit)
                this.jeNo.setValue(this.jeDetails.data.entryno);
            if(this.isViewTemplate || this.isEditTemplate){
                this.moduleTemplateName.setValue(this.templateName);
//                this.moduleTemplateName.disable();
                this.jeNo.disable();
            }
            this.entryDate.setValue(this.jeDetails.data.entrydate);
            this.Memo.setValue(this.jeDetails.data.memo);
            if (WtfGlobal.isIndiaCountryAndGSTApplied() && Wtf.isitcapplicable && (this.isEdit || this.copyInv)) {
                this.gstrTypeCmb.setValue(this.jeDetails.data.gstrType);
                this.gstrTypeCmb.disable();
                this.itctransactionids=this.jeDetails.data.itctransactionids;
            }
            this.CostCenter.setValue(this.jeDetails.data.costcenter);
            this.Currency.setValue(this.jeDetails.data.currencyid);
            if(this.typeValue==2 && this.jeDetails.data.partlyJeEntryWithCnDn){
                this.isLoadAccountStoreInEditPartyJE=false;
                this.generateCNDN.setValue(true);   //here On check events fires in which  variable isLoadAccountStoreInEditPartyJE is used.              
            }
            var jeDetails = this.jeDetails.data.jeDetails;
            var excRate = 1;
            if(currencyRec.items.length!=0)
                excRate = currencyRec.items[0].data.exchangerate;
            if(this.jeDetails.data.externalcurrencyrate!=undefined && this.jeDetails.data.externalcurrencyrate != 0) {
                excRate = this.jeDetails.data.externalcurrencyrate;
            }

            var isCreditAccountIsBankTypeForFundTransfer = false;
            var jedcount = 0;
            for(var i =0; i < jeDetails.length; i++){   //typeValue =1 for Normal JE, typeValue =2 for Partial JE, typeValue =3 for Fund Transfer JE
                if (this.typeValue == 3 && jeDetails[i].isbankcharge) {    //In edit case, don't add bank charges in grid, remove such record
                    continue;
                }
                var dramount=0,cramount=0;
                if(jeDetails[i].d_amount_transactioncurrency) //no need to convert as amounts after edit are in original currency which can be directly brought. 
                    dramount= jeDetails[i].d_amount_transactioncurrency;
                //                        dramount= jeDetails[i].d_amount*excRate;
                if(jeDetails[i].c_amount_transactioncurrency)
                    cramount = jeDetails[i].c_amount_transactioncurrency;
                //                        cramount = jeDetails[i].c_amount*excRate;


                if(jeDetails[i].debit =="Credit" && jeDetails[i].isAccountIsBankType){
                    isCreditAccountIsBankTypeForFundTransfer = true;
                }
                var accountId="";
                if(jeDetails[i].customerVendorName!=undefined&&jeDetails[i].customerVendorName !=""){
                    accountId=jeDetails[i].customerVendorId
                }else{
                    accountId=jeDetails[i].mappedaccountid
                }
                var exchangeratefortransaction = jeDetails[i].exchangeratefortransaction;
                var appliedGst = jeDetails[i].appliedGst;
                var currencysymbolaccount = jeDetails[i].currencysymbolaccount;
                var recObj = {
                    debit:(jeDetails[i].debit =="Debit"),
                    accountid:accountId,
                    mappedaccountid:jeDetails[i].mappedaccountid,
                    masterTypeValueOfAccount:jeDetails[i].masterTypeValueOfAccount,
                    description:jeDetails[i].description,
                    dramount:dramount,//(amount>=0?amount:null),
                    cramount:cramount,
                    exchangeratefortransaction:exchangeratefortransaction,
                    currencysymbolaccount:currencysymbolaccount,
                    appliedGst:appliedGst
                };
                var GlobalcolumnModel=GlobalColumnModel[this.moduleid]; 
                if(GlobalcolumnModel){
                    for(var cnt = 0;cnt<GlobalcolumnModel.length;cnt++){
                        recObj[GlobalcolumnModel[cnt].fieldname]="";//Remove Html code 
                    }
                }
                for (var key in jeDetails[i]) {
                    if(key.indexOf('Custom')!=-1 && jeDetails[i][key+"_Value"] != undefined) { // 'Custom' prefixed already used for custom fields/ dimensions
                        recObj[key] = jeDetails[i][key+"_Value"];
                    }
                }
                var rec = new this.gridRec(recObj);
                this.gridStore.insert(jedcount,rec);
                jedcount++;
            }//for
            if (currencyRec && currencyRec.items[0] && currencyRec.items[0].data && currencyRec.items[0].data.symbol) {
                this.setCurrencyid(currencyID, 1, currencyRec.items[0].data.symbol);
            }

            if(this.jeDetails.data.externalcurrencyrate!=undefined){
                this.externalcurrencyrate=this.jeDetails.data.externalcurrencyrate;
                this.updateFormCurrency(true); 
            }
            if(this.chequeDetails && this.typeValue == 3 && isCreditAccountIsBankTypeForFundTransfer){// for fund transfer
                this.chequeDetails.setValuesOnEditandCopy(this.jeDetails);
                this.chequeDetails.show();
                /*
                 * Hide save and print cheque buttton om view mode
                 */
                if(!this.readOnly){
                    this.savePrintBttn.show();
                }
                if(this.newPanel){
                    this.newPanel.doLayout();
                }
            }
            if(this.jeDetails.data.typeValue==3){
                if(this.jeDetails.data.pmtmethod!=undefined && this.jeDetails.data.pmtmethod!="")                
                    this.pmtMethod.setValue(this.jeDetails.data.pmtmethod);//(this.jeDetails.data.paidToCmb);
                if(this.jeDetails.data.paidToCmb!=undefined && this.jeDetails.data.paidToCmb!="")
                    this.paidTo.setValue(this.jeDetails.data.paidToCmb);   
                if(this.jeDetails.data.pmtmethodaccountname && this.jeDetails.data.pmtmethodaccountname)                   	
                    this.pmtMethodAcc.setValue(this.jeDetails.data.pmtmethodaccountname)
                  
            }
            if(this.jeDetails.data.typeValue==1){
                if(this.jeDetails.data.includeingstreport!=undefined && this.jeDetails.data.includeingstreport!=""){                
                    this.includeInGst.setValue(this.jeDetails.data.includeingstreport);
                }    
            }
        }

    //        } else if(!this.isEdit){
    //            this.setJENumber();
    }else if (this.vatPaymentFlag){
        var currencyID = this.jeDetails.data.currencyid;
        var currencyRec=this.currencyStoreCMB.queryBy( function(rec){
            var parentname=rec.data['currencyid'];
            if(parentname==currencyID)
                return true;
            else
                return false
        });   
        this.Currency.setValue(this.jeDetails.data.currencyid);
        this.Memo.setValue(this.jeDetails.data.memo);
        var jeDetails = this.jeDetails.data.jeDetails;
        for(var i =0; i < jeDetails.length; i++){   //typeValue =1 for Normal JE, typeValue =2 for Partial JE, typeValue =3 for Fund Transfer JE
            if (this.typeValue == 3 && jeDetails[i].isbankcharge) {    //In edit case, don't add bank charges in grid, remove such record
                continue;
            }
            var dramount=0,cramount=0;
            if(jeDetails[i].d_amount_transactioncurrency) //no need to convert as amounts after edit are in original currency which can be directly brought. 
                dramount= jeDetails[i].d_amount_transactioncurrency;
            //                        dramount= jeDetails[i].d_amount*excRate;
            if(jeDetails[i].c_amount_transactioncurrency)
                cramount = jeDetails[i].c_amount_transactioncurrency;
            //                        cramount = jeDetails[i].c_amount*excRate;


            if(jeDetails[i].debit =="Credit" && jeDetails[i].isAccountIsBankType){
                isCreditAccountIsBankTypeForFundTransfer = true;
            }
            var accountId="";
            if(jeDetails[i].customerVendorName!=undefined&&jeDetails[i].customerVendorName !=""){
                accountId=jeDetails[i].customerVendorId
            }else{
                accountId=jeDetails[i].mappedaccountid
            }
            var exchangeratefortransaction = jeDetails[i].exchangeratefortransaction;
            var appliedGst = jeDetails[i].appliedGst;
            var currencysymbolaccount = jeDetails[i].currencysymbolaccount;
            var recObj = {
                debit:(jeDetails[i].debit =="Debit"),
                accountid:accountId,
                mappedaccountid:jeDetails[i].mappedaccountid,
                masterTypeValueOfAccount:jeDetails[i].masterTypeValueOfAccount,
                description:jeDetails[i].description,
                dramount:dramount,//(amount>=0?amount:null),
                cramount:cramount,
                exchangeratefortransaction:exchangeratefortransaction,
                currencysymbolaccount:currencysymbolaccount,
                appliedGst:appliedGst
            };
            var GlobalcolumnModel=GlobalColumnModel[this.moduleid]; 
            if(GlobalcolumnModel){
                for(var cnt = 0;cnt<GlobalcolumnModel.length;cnt++){
                    recObj[GlobalcolumnModel[cnt].fieldname]="";//Remove Html code 
                }
            }
            for (var key in jeDetails[i]) {
                if(key.indexOf('Custom')!=-1 && jeDetails[i][key+"_Value"] != undefined) { // 'Custom' prefixed already used for custom fields/ dimensions
                    recObj[key] = jeDetails[i][key+"_Value"];
                }
            }
            var rec = new this.gridRec(recObj);
            this.gridStore.insert(i,rec);
        }
        this.setCurrencyid(currencyID,1,currencyRec.items[0].data.symbol);
    }
    if(this.generateCNDN.getValue()){
        this.showCNDN();
    }else{
        this.hideCNDN();   
    }
        
    if(this.isTemplate && !this.createTransactionAlso) { // for template case hide sequence format combo box and je no.
        WtfGlobal.hideFormElement(this.sequenceFormatCombobox);
        WtfGlobal.hideFormElement(this.jeNo);
        }
        if (this.isAllowedSpecificFields) {
            this.disableField();
        }   
    //        this.hideCNDN();
    this.JournalNorthForm.doLayout();
},
/**
 * Disable field in Edit case of recurred JE
 */
    disableField: function() {
        if (this.JournalNorthForm.getForm().items != undefined && this.JournalNorthForm.getForm().items != null) {
            for (var i = 0; i < this.JournalNorthForm.getForm().items.length; i++) {
                this.JournalNorthForm.getForm().items.item(i).disable();
            }
        }
        if (this.chequeDetails) {
            this.chequeDetails.disable();
        }
        this.Memo.enable();
    },   
savePrintCheque: function(a, b) {
    this.isChequePrint = true;      
    if(this.paidTo.getValue()==""||this.paidTo.getValue()==undefined){
        this.paidTo.markInvalid();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.PleaseEnterPaidto")],2);
        this.isChequePrint=false;
        return;
    }   
    if(this.isEdit && this.jeDetails.data.ischequeprinted){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.printCheque"),WtfGlobal.getLocaleText("acc.je.printmsg"),function(btn){
            if(btn!="yes") {
                return;
            }              
            this.checkTotal();
        },this);
    }else{
        this.checkTotal();
    }     
    
},
    checkTotal:function(){
        var dramount = this.calGridTotal('dramount');
        var cramount = this.calGridTotal('cramount');
        if ((this.bankCharges.getValue() * 1) != undefined && dramount != 0) {
            // To prompt, when Credit Amount & Debit amount are not same while adding bank charges.
            dramount = dramount + (this.bankCharges.getValue() * 1);       // Also to prompt when Cr amount & Bank charges are same but Dr amount is zero
            dramount = getRoundofValueWithValues(dramount, Wtf.AMOUNT_DIGIT_AFTER_DECIMAL).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)
        }

        if (dramount != cramount) {
            if (this.symbol == null || this.symbol == undefined || this.symbol == "") {
                this.unbalancedTextValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(dramount - cramount, WtfGlobal.getCurrencySymbol()) + "</B>";
            } else {
                this.unbalancedTextValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(dramount - cramount, this.symbol) + "</B>";
            }
            
            WtfComMsgBox(25,2);
            return;
        }else if(dramount==0){
            WtfComMsgBox(26,2);
            return;
        }else
            this.saveJournal();                  
    },
    addCostCenter:function(){
        callCostCenter('addCostCenterWin');
    },
    saveJournal:function(){
        // check validation for template name in create and edit template case.
         if(this.isTemplate || this.isEditTemplate){
               var str = this.moduleTemplateName.getValue();
               var re = new RegExp("&nbsp", "g");
               str = str.replace(re, " ");
               if(str == '' || str.trim() == ''){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.PleaseEnterTemplateNameFirst")], 2);
                    this.moduleTemplateName.setValue('');
                    return;
               }           
         }
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo();
        var lastrec=this.gridStore.getAt(this.gridStore.getCount()-1)
        if((lastrec.data["dramount"]>0 || lastrec.data["cramount"]>0)&& this.typeValue!=3&&(this.type!=2 || (this.type==2 && !this.generateCNDN.getValue()))){
            WtfComMsgBox(30,2);
             this.saveAndCreateNewFlag = false;
            return;
        }
        for (var i = 0; i < this.gridStore.getCount() - 1; i++) {// excluding last row
            var rec = this.gridStore.getAt(i);
            var accId = rec.data['accountid'];
            if (accId == null || accId == undefined || accId == '') {
                WtfComMsgBox(30, 2);
                return;
            }
        }
        if (this.typeValue == 3 && (lastrec.data["accountid"] == undefined || lastrec.data["accountid"] == "")) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.journal.account")], 0);
            this.saveAndCreateNewFlag = false;
            return;
        }
        var isChequeDetailsFormValid = true;
        if(this.typeValue == 3 && this.chequeDetails && this.chequeDetails. isVisible()){// for fund transfer and if credit account is bank account
            isChequeDetailsFormValid = this.chequeDetails.getForm().isValid();
            if(this.chequeDetails.checkNo.getValue()=="" || this.chequeDetails.checkNo.getValue()==undefined|| (this.chequeDetails.sequenceFormatCombobox!=undefined && this.chequeDetails.sequenceFormatCombobox.getValue()!='NA' && this.chequeDetails.sequenceFormatCombobox.getRawValue().length!=this.chequeDetails.checkNo.getValue().length)){
                this.chequeDetails.checkNo.markInvalid();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.2")], 2);
                return;
            }
        }
               
        var validLineItem =this.checkDetails(this.grid);
        if (validLineItem != "" && validLineItem != undefined) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), (WtfGlobal.getLocaleText("acc.msgbox.lineitem") + validLineItem)], 2);           
            this.saveAndCreateNewFlag = false;
            return;
        }       
        if(this.typeValue == 1){
            var isValidForIncludingInGSTReport=this.isValidForIncludingInGSTReport();
            if(!isValidForIncludingInGSTReport){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.JE.selectAtLeastOneGstAccount")], 2);
                return;
            }
            var isValidForGSTTypeOfAccounts = this.isValidForGSTTypeOfAccounts();
            if(!isValidForGSTTypeOfAccounts){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mp.alertForSelectingGst")], 2);
                return;
            }
        }
        if(this.JournalNorthForm.getForm().isValid() && isValidCustomFields && isChequeDetailsFormValid){
            var rec=this.JournalNorthForm.getForm().getValues();
            if(this.copyInv){
                var notAccessAccountsList="";
                var accHasAccessFlag=false;
                if(!checkForAccountActivate(this.bankChargesAccountStore,this.bankChargesAccount.getValue(),"accountid")){
                    accHasAccessFlag=true;
                    notAccessAccountsList = WtfGlobal.getLocaleText("acc.field.BankChargesAccount");
                }
                if(accHasAccessFlag){
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                        msg: WtfGlobal.getLocaleText("acc.field.Pleaseselectactivatedaccount")+notAccessAccountsList+".",
                        width:370,
                        buttons: Wtf.MessageBox.OK,
                        icon: Wtf.MessageBox.WARNING,
                        scope: this
                    });
                    return;
                }
                var jeDetails = eval("["+this.getRecords().join(",")+"]");
                if(jeDetails!=null && jeDetails!=undefined){
                    var notAccessAccList="";
                    var hasAccessFlag=false;
                    for(var i=0;i<jeDetails.length;i++){
                        var jeDetailsRec= jeDetails[i];
                        if(!checkForAccountActivate(this.accountStore,jeDetailsRec.accountid,"accountid")){
                            hasAccessFlag=true;
                            var accRec=WtfGlobal.searchRecord(this.accountStore, jeDetailsRec.accountid, 'accountid');
                            if(accRec!=null && accRec!=undefined)
                                notAccessAccList=notAccessAccList+decodeURIComponent(accRec.data.accountname)+", ";
                        }
                    }
                    if(notAccessAccList!=""){
                        notAccessAccList = notAccessAccList.substring(0, notAccessAccList.length-2);
                    }
                    if(hasAccessFlag){
                        Wtf.MessageBox.show({
                            title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                            msg: WtfGlobal.getLocaleText("acc.field.Account")+" "+notAccessAccList+" "+WtfGlobal.getLocaleText("acc.field.jounalEntry.accountDeactivated")+
                            "<br>"+WtfGlobal.getLocaleText("acc.field.jounalEntry.cannotcopyalert"),
                            width:370,
                            buttons: Wtf.MessageBox.OK,
                            icon: Wtf.MessageBox.WARNING,
                            scope: this
                        });
                        return;
                    }
                }
            }
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"), WtfGlobal.getLocaleText("acc.je.msg1"),function(btn){
                if(btn!="yes"){
                    this.isWarnConfirm=false;
                    return;
                } 
                WtfComMsgBox(27,4,true);
                var rec=this.JournalNorthForm.getForm().getValues();
                var custFieldArr=this.tagsFieldset.createFieldValuesArray();
                if (custFieldArr.length > 0)
                    rec.customfield = JSON.stringify(custFieldArr);
                rec.entryno = (this.isEdit || this.sequenceFormatCombobox.getValue() == "NA") ?this.jeNo.getValue().trim():"";
                rec.memo=this.Memo.getValue();
                rec.currencyid=this.currencyid;//WtfGlobal.getCurrencyID();
                rec.mode=53;
                rec.entrydate=WtfGlobal.convertToGenericDate(this.entryDate.getValue());
                rec.jeid = this.isEdit ? this.jeDetails.data.journalentryid : '';
                rec.jeedit = this.isEdit ? true : false;
                rec.detail="["+this.getRecords().join(",")+"]";
                rec.typevalue=this.typeValue;
                if(this.isEdit){
                    rec.parentje=this.jeDetails.data.parentje;
                    rec.repeateid=this.jeDetails.data.repeateid;
                }
                rec.externalcurrencyrate=this.externalcurrencyrate;
                this.sequenceFormatStore.clearFilter(true);
                var seqFormatRec=WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
                rec.seqformat_oldflag=seqFormatRec!=null?seqFormatRec.get('oldflag'):true;
                this.disable();
                rec.entrynoCN = this.no.getValue();
                this.sequenceFormatStoreCN.clearFilter(true);
                var seqFormatRecCN=WtfGlobal.searchRecord(this.sequenceFormatStoreCN, this.sequenceFormatComboboxCN.getValue(), 'id');
                rec.seqformat_oldflagCN=seqFormatRecCN!=null?seqFormatRecCN.get('oldflag'):true;
                rec.entrynoDN = this.noDN.getValue();
                this.sequenceFormatStoreDN.clearFilter(true);
                var seqFormatRecDN=WtfGlobal.searchRecord(this.sequenceFormatStoreDN, this.sequenceFormatComboboxDN.getValue(), 'id');
                rec.seqformat_oldflagDN=seqFormatRecDN!=null?seqFormatRecDN.get('oldflag'):true;
                rec.sequenceformat=this.sequenceFormatCombobox.getValue();
                rec.sequenceformatCN=this.sequenceFormatComboboxCN.getValue();
                rec.sequenceformatDN=this.sequenceFormatComboboxDN.getValue();
                if(this.isEdit && this.typeValue==2 && this.jeDetails.data.partlyJeEntryWithCnDn==1 && this.generateCNDN.getValue()==false){//edit case of part JE with cn/dn
                  rec.isdeletecndnwithJE=true;                  
                }
                rec.templatename=this.moduleTemplateName.getValue();
                rec.moduletempname=this.isTemplate;
                rec.moduletemplateid=this.templateId;
                rec.printCheque=this.isChequePrint;

                if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
                    rec.gstrType = this.gstrTypeCmb.getValue();
                    rec.itctransactionids=this.itctransactionids;
                }
                if(this.pmtMethod && this.typeValue == 3){
                    rec.pmtmethod=this.pmtMethod.value;
                }
                if(this.paidTo && this.typeValue == 3){
                    rec.paidToCmb=this.paidTo.value;
                }
                if(this.typeValue == 3 && this.chequeDetails && this.chequeDetails. isVisible()){// for fund transfer and if credit account is bank account
                    rec.chequeDetail = this.chequeDetails.GetPaymentFormData(this.gridStore);
                }
                rec.istemplate=(this.isTemplate || this.isEditTemplate)? (this.createTransactionAlso? '1' : '2') : '0';  // istemplate : '0'-Only JE ; '1'-Template with JE ; '2'-Only Template
                if(this.typeValue == 1){
                    rec.includeingstreport = this.includeInGst.getValue();
                }
                if(this.typeValue == 3){
                    if(Wtf.account.companyAccountPref.chequeNoDuplicate==Wtf.ChequeNoWarn && !this.isWarnConfirm){
                        rec['isWarning'] = true;
                    }
                    if(this.isWarnConfirm){
                        rec['isWarning'] = false;
                    }
                }
                var url="";
                if(this.generateCNDN.getValue()){

                    url="ACCJournalCMN/saveJournalEntry.do";
                }else if (this.isAllowedSpecificFields && this.isAllowedSpecificFields==true){
                    url="ACCJournal/updateJournalEntry.do";
                }else{
                    url="ACCJournal/saveJournalEntry.do";
                }
                 Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
//                    url:"ACCJournal/saveJournalEntry.do",
//                    url:"ACCJournalCMN/saveJournalEntry.do",
                      url:url,
                    params: rec
                },this,this.genSuccessResponse,this.genFailureResponse);
            },this);
        }else{
                 WtfComMsgBox(2,2);
            }
    },
    genSuccessResponse:function(response){
        this.accountBalanceTplSummary.overwrite(this.accountBalanceTpl.body,{accounatName:"&nbsp;&nbsp;&nbsp;&nbsp;",endingBalance:"&nbsp;&nbsp;&nbsp;&nbsp;"});
        this.enable();       
        var chequeFlag=false; 
        if(response.success){
            this.fireEvent('update',this, response.id);
            if (this.saveAndCreateNewFlag) {
                this.tplSummary.overwrite(this.southCalTemp.body, {balanceInBase: WtfGlobal.currencyRenderer(0)});
                this.creditTextValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(0, WtfGlobal.getCurrencySymbol()) + "</B>"; // Setting Default value as 0
                this.debitTextValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(0, WtfGlobal.getCurrencySymbol()) + "</B>"; // Setting Default value as 0
                this.unbalancedTextValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(0, WtfGlobal.getCurrencySymbol()) + "</B>";
                this.isWarnConfirm=false;
                if(response.success ||!response.isWarning){
                var format = this.sequenceFormatCombobox.getValue(); //storing format vefore reset resetting of field
                this.resetAll();
                this.isClosable = true;       //Reset Closable flag to avoid unsaved Message.
                this.sequenceFormatCombobox.setValue(format);
                this.getNextSequenceNumber(this.sequenceFormatCombobox);
                this.symbol = WtfGlobal.getCurrencySymbol();
                this.currencyid = WtfGlobal.getCurrencyID();

                this.setCurrencyid(this.currencyid, 1, this.symbol);
                
                this.currencychanged = true;
                this.currencyStore.load({params: {mode: 201, transactiondate: WtfGlobal.convertToGenericDate(this.entryDate.getValue()), tocurrencyid: this.Currency.getValue()}});

                this.externalcurrencyrate = 0;
                }
                  this.isAfterSaveNCreateNew=true;
                        this.moduleTemplateStore.load();
                if (WtfGlobal.isIndiaCountryAndGSTApplied() && Wtf.isitcapplicable) {
                    this.gstrTypeCmb.enable();
                }
            } else {
                 if(response.success){
                    this.disableComponents();
                 }
                this.isClosable = true;
                if(this.recurringBtn){
                    this.recurringBtn.enable();
                }
                if(response.id!=undefined){
                    this.journalentryid=response.id;
                }
                if(response.success && (this.typeValue==Wtf.normal_journal_entry||this.typeValue==Wtf.fund_transafer_journal_entry)){
                 this.recordForRecurring = {};
                 this.recordForRecurring.data={};
                 this.recordForRecurring.data.entryno = response.billno;
                 this.recordForRecurring.data.journalentryid = response.id;
                 this.recordForRecurring.data.repeatedid = response.repeatedid;
                 this.recordForRecurring.data.nextDate = response.nextdate;
                 this.recordForRecurring.data.expireDate = response.expdate;
                 this.recordForRecurring.data.interval= response.intervalUnit;
                 this.recordForRecurring.data.intervalType = response.intervalType;
                 this.recordForRecurring.data.NoOfpost = '';
                 this.recordForRecurring.data.typeValue = response.typeValue;
                }
            }
            if(this.isEdit || this.isCopyFromTemplate){
                this.fireEvent('jeupdate', this);
//                Wtf.getCmp('as').remove(this);
            }
            
            if(this.isTemplate){
                this.ownerCt.remove(this);
            }
 
        }
        if(this.typeValue == 3 && !response.success && response.isWarning){
              Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.warning"), // 'Warning',
                msg: response.msg,
                buttons: Wtf.MessageBox.YESNO,
                width:450,
                fn: function(btn) {
                    if(btn =="yes") {
                         this.isWarnConfirm = true;
                         this.saveJournal(); 
                     }
                },
                scope: this,
                icon: Wtf.MessageBox.QUESTION
            });
        }else if (Wtf.getCmp("NormalJournalEntryDetails") != undefined && (this.copyInv || this.isEdit) && response.success) {
            chequeFlag=true;
            var scope=this;
            var msg = "";
            if(this.isEdit){
                msg = WtfGlobal.getLocaleText("acc.field.EditJournalEntry");
            } else if(this.copyInv){
                msg = WtfGlobal.getLocaleText("acc.field.CopyJournalEntry");
            } else {
                msg = WtfGlobal.getLocaleText("acc.je.tabTitle");
            }
            Wtf.getCmp("NormalJournalEntryDetails").Store.on('load', function() {
                WtfComMsgBox([msg, response.msg], response.success * 2 + 1);
                if (response.data && response.success) {
                    var resdata = response.data[0];
                    if(scope.isChequePrint){
                        this.printDetailswin=new Wtf.account.ReceiptEntry({
                            });
                        this.printDetailswin.printCheque(resdata);  //Passing Paramter as JSON Object
                        this.isChequePrint = false;
                    }    
                }   
//                WtfComMsgBox([msgTitle,response.msg],response.success*2+1);
            }, Wtf.getCmp("NormalJournalEntryDetails").Store, {
                single: true
            });
        } else {
            if (response.success) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.je.tabTitle"), response.msg], response.success * 2 + 1);
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), response.msg],2);
            }
        }
        if (!chequeFlag && response.success && response.data) {
            var resdata = response.data[0];
            if(this.isChequePrint){
                this.printDetailswin=new Wtf.account.ReceiptEntry({
                });
                this.printDetailswin.printCheque(resdata);  //Passing Paramter as JSON Object
                this.isChequePrint = false;
            }    
        }  
//        WtfComMsgBox([WtfGlobal.getLocaleText("acc.je.tabTitle"),response.msg],response.success*2+1);
    },
    genFailureResponse:function(response){
        this.enable();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    resetAll:function(){
        this.JournalNorthForm.getForm().reset();
        for (var i = 0; i < this.tagsFieldset.dimensionFieldArray.length; i++) {
            var isMandatory = this.tagsFieldset.dimensionFieldArray[i].isessential;
            if (isMandatory !== 1) {
                this.tagsFieldset.dimensionFieldArray[i].reset();
            }
        }
        
        for (var j = 0; j < this.tagsFieldset.customFieldArray.length; j++) {
            var isMandatory = this.tagsFieldset.customFieldArray[j].isessential;
            if (this.tagsFieldset.customFieldArray[j].getXType() == "fieldset") {
                continue;
            } else {
                if (isMandatory !== 1) {
                    this.tagsFieldset.customFieldArray[j].reset();
                }
            }
        }
       var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;  //Reset Check List
            for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                var checkfieldId = checkListCheckBoxesArray[checkitemcnt].id
                if (Wtf.getCmp(checkfieldId) != undefined) {
                    Wtf.getCmp(checkfieldId).reset();
                }     
        }
	
        this.grid.store.removeAll();
        this.addNewRow();
        this.Memo.setValue("");
        
        if(this.chequeDetails && this.chequeDetails.getForm()){
            this.chequeDetails.getForm().reset();
            if(this.chequeDetails.isVisible() && this.newPanel && this.newPanel.items && this.newPanel.items.items[0]){
                this.newPanel.items.items[0].setHeight(this.newPanel.items.items[0].getInnerHeight()-210);
                this.newPanel.doLayout();
            }
            this.chequeDetails.hide();
            this.savePrintBttn.hide();
        }
        this.sequenceFormatStore.load();
        if(this.sequenceFormatStore.getCount()>0){
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
            if(!this.isEdit){
                this.getNextSequenceNumber(this.sequenceFormatCombobox);
            }
        }
        this.CostCenter.setValue("");
        if(this.saveAndCreateNewFlag || (this.templateModelCombo && this.templateModelCombo.getValue()=="")){
            this.Currency.setValue(WtfGlobal.getCurrencyID());
        }
        
},
    disableComponents: function () {
        if (Wtf.getCmp("savencreate" + this.helpmodeid + this.id)) {
            Wtf.getCmp("savencreate" + this.helpmodeid + this.id).disable();
        }
        if (this.saveBttn) {
            this.saveBttn.disable();
        }
        if (this.savePrintBttn) {
            this.savePrintBttn.hide();
        }
        if (this.grid) {
            this.grid.disable();
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
        if (this.JournalNorthForm) {
            this.JournalNorthForm.disable();
        }
        if (this.tagsFieldset) {
            this.tagsFieldset.disable();
        }
        if (this.chequeDetails) {
            this.chequeDetails.disable();
        }
        
    },
    getNextSequenceNumber:function(a,val){
       if(!(a.getValue()=="NA")){ 
         WtfGlobal.hideFormElement(this.jeNo);
         this.setJENumber(true);  
         var rec=WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
         var oldflag=rec!=null?rec.get('oldflag'):true;
         Wtf.Ajax.requestEx({
            url:"ACCCompanyPref/getNextAutoNumber.do",
            params:{
                from:this.fromnumber,
                sequenceformat:a.getValue(),
                oldflag : oldflag
            }
        }, this,function(resp){
            if(resp.data=="NA"){
                WtfGlobal.showFormElement(this.jeNo);
                this.jeNo.reset();
                this.jeNo.enable();
            }else {
                this.jeNo.setValue(resp.data); 
                this.jeNo.disable();
                WtfGlobal.hideFormElement(this.jeNo);
            }
            
        });
        } else {
            WtfGlobal.showFormElement(this.jeNo);
            this.jeNo.reset();
            this.jeNo.enable();
        }
    },
    getNextSequenceNumberCN:function(a,val){
       if(!(a.getValue()=="NA")){
           WtfGlobal.hideFormElement(this.no);
        this.setTrNoteNumber(true);       
         var rec=WtfGlobal.searchRecord(this.sequenceFormatStoreCN, a.getValue(), 'id');
         var oldflag=rec!=null?rec.get('oldflag'):true;
         Wtf.Ajax.requestEx({
            url:"ACCCompanyPref/getNextAutoNumber.do",
            params:{
                from:this.fromnumberCN,
                sequenceformat:a.getValue(),
                oldflag:oldflag
            }
        }, this,function(resp){
            
            WtfGlobal.showFormElement(this.no);            
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
    updateCNDNAmount: function() {
        var camount = 0, damount = 0;
        this.gridStore.each(function(rec) {
            if (rec.data['cramount'] != undefined) {
                camount += rec.data['cramount'];
            }
            if (rec.data['dramount'] != undefined) {
                damount += rec.data['dramount'];
            }
        });
        
        /*In Case of Bank Charges
         * 
         * Debit Amount = Bank charges + Debit Amount entered at Line Level
         */
        
        if ((this.bankCharges.getValue() * 1) != undefined && damount != 0) {
            damount = damount + (this.bankCharges.getValue() * 1);
            damount = getRoundofValueWithValues(damount, Wtf.AMOUNT_DIGIT_AFTER_DECIMAL).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL)
        }
        if (this.symbol == null || this.symbol == undefined || this.symbol == "") {
            this.creditTextValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(camount, WtfGlobal.getCurrencySymbol()) + "</B>";
            this.debitTextValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(damount, WtfGlobal.getCurrencySymbol()) + "</B>";
            /* Showing Unbalanced Amount (Debit Amount-Credit Amount), if Credit & Debit Amount is not same*/
            this.unbalancedTextValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(damount - camount, WtfGlobal.getCurrencySymbol()) + "</B>";
        } else {
            this.creditTextValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(camount, this.symbol) + "</B>";
            this.debitTextValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(damount, this.symbol) + "</B>";
            /* Showing Unbalanced Amount (Debit Amount-Credit Amount), if Credit & Debit Amount is not same*/
            this.unbalancedTextValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(damount - camount, this.symbol) + "</B>";
        }

    },
    getNextSequenceNumberDN:function(a,val){
       if(!(a.getValue()=="NA")){
           WtfGlobal.hideFormElement(this.noDN);
        this.setTrNoteNumberDN(true);       
         var rec=WtfGlobal.searchRecord(this.sequenceFormatStoreDN, a.getValue(), 'id');
         var oldflag=rec!=null?rec.get('oldflag'):true;
         Wtf.Ajax.requestEx({
            url:"ACCCompanyPref/getNextAutoNumber.do",
            params:{
                from:this.fromnumberDN,
                sequenceformat:a.getValue(),
                oldflag:oldflag
            }
        }, this,function(resp){
            if(resp.data=="NA"){
                 WtfGlobal.showFormElement(this.noDN);
                this.noDN.reset();
                this.noDN.enable();
            }else {
                this.noDN.setValue(resp.data);
                this.noDN.disable();
                WtfGlobal.showFormElement(this.noDN);
            }
            
        });
       } else {
           WtfGlobal.showFormElement(this.noDN);
           this.noDN.reset();
           this.noDN.enable();
       }
    },
    createGrid:function(){
       this.Save=new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
            border:false,
            scope:this,
            hidden:this.isViewTemplate,
            iconCls :getButtonIconCls(Wtf.etype.save),
            handler: this.checkTotal.createDelegate(this)
       });       
       this.gridRec = Wtf.data.Record.create([
            {name:'debit',type:'boolean'},
            {name:'accountid'},
            {name:'mappedaccountid'},
            {name:'masterTypeValueOfAccount'},
            {name:'description'},
            {name: 'currencysymbol'},
            {name: 'customfield'},
            {name:'dramount',type:'float'},
            {name:'cramount',type:'float'},
            {name:'exchangeratefortransaction',defValue :1},
            {name:'currencysymbolaccount'},
            {name:'appliedGst'},
            {name: 'srno', isForSequence:true}
        ]);
        this.gridStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.gridRec)
        });
        
        var baseparam = {
            mode:2,
            deleted:false,
            nondeleted:true,
            ignoreAssets:true,
            isForJE:true
//            ignoreTaggedAccounts:true
        };
        
        if(this.type==2){
            //            this.accountStore.baseParams.group=[10,13,9]
            baseparam.ignoreCashAccounts=true;
            baseparam.ignoreGSTAccounts=true;
            baseparam.ignoreGLAccounts=true;
        }
        if(this.typeValue==3){
            baseparam.ignoreGLAccounts=true;  
            baseparam.ignoreGSTAccounts=true;  
            baseparam.ignorecustomers=true;  
            baseparam.ignorevendors=true;  
           }   
        if(this.typeValue==1){
            baseparam.ignorecustomers=true;  
            baseparam.ignorevendors=true;    
        }
            baseparam.controlAccounts=true;    
        
        this.accountStore = new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForJE.do",
            baseParams:baseparam,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        
          this.typeStore=new Wtf.data.SimpleStore({
            fields:[{name:"id"},{name:"name"}],
            data:[[true,"Debit"],[false,"Credit"]]
        });
        
        var extraFields = Wtf.account.companyAccountPref.accountsWithCode ? ['acccode', 'groupname'] : ['groupname'];
        var listWidth = Wtf.account.companyAccountPref.accountsWithCode ? 300 : 200;
                
        this.cmbAccount = CommonERPComponent.createAccountPagingComboBox(this, 100, listWidth, 300, 30, extraFields, Object.assign(new Object(), baseparam));
        
        if (this.cmbAccount.store && this.accountStore) {
            this.cmbAccount.store.on('load', function (store, recArr) {
                var insertIndex = 1;
                for (var i = 0; i < this.accountStore.data.length; i++) {
                    if (WtfGlobal.searchRecordIndex(store, this.accountStore.data.items[i].data.accountid, 'accountid') == -1) {
                        store.insert(insertIndex++, this.accountStore.data.items[i]);
                    }
                }
            }, this);
        }

        this.cmbAccount.on('render', function () {
            if (this.cmbAccount.trigger) {
                this.cmbAccount.trigger.on('click', function () {
                    this.cmbAccount.footer.setVisible(true);
                }, this);
            }
        }, this);
        
                this.cmbAccount.on('beforeselect',function(combo,record,index){
                    return validateSelection(combo,record,index);
                },this);
        
                this.cmbType=new Wtf.form.ComboBox({
                    hiddenName:'debit',
                    store:this.typeStore,
                    valueField:'id',
                    displayField:'name',
                    mode: 'local',
                    triggerAction:'all',
                    forceSelection:true
                });
                
//    this.cmbAccount.on("expand", function(){
//        if(this.generateCNDN.getValue()){
//            this.filterStore();
//        }else{
//            this.cmbAccount.store.clearFilter();
//        }
//    }, this);

      
        this.summary = new Wtf.ux.grid.GridSummary();
        this.selModel =new Wtf.grid.CheckboxSelectionModel({
            singleSelect:false
        });
        this.selModel.on("beforerowselect", this.checkSelections, this);
        var columnArr =[];
        if(!this.readOnly && !this.isViewTemplate){
            columnArr.push(this.selModel);
        }
        
         columnArr.push({
            header: WtfGlobal.getLocaleText("acc.invoice.lineItemSequence"),//"Sequence",
            width:65,
            align:'center',
            hidden:this.typeValue==3,
            name:'srno',
            renderer: Wtf.applySequenceRenderer
        },{
                header:WtfGlobal.getLocaleText("acc.je.type"),  //"Type",
                editor: this.readOnly?"":this.cmbType,
                width:80,
                renderer:Wtf.comboBoxRenderer(this.cmbType),
                dataIndex:'debit'
            });
            columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModel[this.moduleid],undefined,undefined,this.readOnly,this.isViewTemplate);
            columnArr.push({    
                header:WtfGlobal.getLocaleText("acc.je.acc"),  //"Account",
                dataIndex:'accountid',
                width:250,
                editor: this.readOnly?"":this.cmbAccount,
                renderer: Wtf.account.companyAccountPref.accountsWithCode ? this.cmbAccountComboRenderer.createDelegate(this) : Wtf.comboBoxRenderer(this.cmbAccount)
            },{
                header: (Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA)?WtfGlobal.getLocaleText("acc.field.TaxApplied"):WtfGlobal.getLocaleText("acc.field.GSTApplied"),
                dataIndex: 'appliedGst',
                hidden:this.typeValue!=1 || Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA || Wtf.account.companyAccountPref.countryid==Wtf.Country.SINGAPORE,
                renderer: this.gstRenderer.createDelegate(this)
            },{
                header:WtfGlobal.getLocaleText("acc.je.debitAmt"),  //"Debit Amount",
                dataIndex:'dramount',
                align:'right',
                width:300,
                renderer:WtfGlobal.currencyRendererSymbol,
                summaryType:'sum',
                summaryRenderer:function(value,m,rec) {
                    var symbol=((rec==undefined||rec.data['currencysymbol']==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
                    var rate=((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
                    var oldcurrencyrate=((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
                    var v;
                    if(rate!=0.0)
                        v=(parseFloat(value)*parseFloat(rate))/parseFloat(oldcurrencyrate);
                    else
                        v=(parseFloat(value)/parseFloat(oldcurrencyrate));
                    if(isNaN(v)) return value;
                    v= WtfGlobal.conventInDecimal(v,symbol)
                    return '<div class="currency" align="right"  style=\'width:1000px;height:10px;\'>'+'<b>'+WtfGlobal.getLocaleText("acc.je.debitAmt")+':  </b> '+v+'</div>';

             },
            editor:this.readOnly?"":new Wtf.form.NumberField({
            allowBlank: false,
            allowNegative:false,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        })
    },{
                header:WtfGlobal.getLocaleText("acc.je.creditAmt"),  //"Credit Amount",
                dataIndex:'cramount',
                align:'right',
                width:300,
                renderer:WtfGlobal.currencyRendererSymbol,
                summaryType:'sum',
                summaryRenderer:function(value,m,rec) {
                    var symbol=((rec==undefined||rec.data['currencysymbol']==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
                    var rate=((rec==undefined||rec.data['currencyrate']==undefined||rec.data['currencyrate']=="")?1:rec.data['currencyrate']);
                    var oldcurrencyrate=((rec==undefined||rec.data['oldcurrencyrate']==undefined||rec.data['oldcurrencyrate']=="")?1:rec.data['oldcurrencyrate']);
                    var v;
                    if(rate!=0.0)
                        v=(parseFloat(value)*parseFloat(rate))/parseFloat(oldcurrencyrate);
                    else
                        v=(parseFloat(value)/parseFloat(oldcurrencyrate));
                    if(isNaN(v)) return value;
                    v= WtfGlobal.conventInDecimal(v,symbol)
                    return '<div class="currency"  style=\'width:1000px;\'>'+'<b>'+WtfGlobal.getLocaleText("acc.je.creditAmt")+': </b>'+v+'</div>';

           },
                editor:this.readOnly?"":new Wtf.form.NumberField({
                    allowBlank: false,
                    allowNegative:false,
                    decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
                })           
            },{
            header:WtfGlobal.getLocaleText("acc.setupWizard.curEx"), 
            dataIndex:'exchangeratefortransaction',
            hidden: this.typeValue!=3,
            renderer:this.conversionFactorRenderer,
            editor: this.readOnly?"":(this.exchangeratefortransaction=new Wtf.form.NumberField({
                decimalPrecision:10,
                allowNegative : false,
                validator: function(val) {
                    if (val!=0) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }))
            },{
                header:WtfGlobal.getLocaleText("acc.je.desc"),  //"Description",
                dataIndex:"description",
                editor:this.readOnly?"":this.Description=new Wtf.form.TextArea({
                    //maxLength:200,
                    allowBlank: false,
                    xtype:'textarea'
                }),
                width:250,
                renderer:function(value,meta,rec){
                    var tip = value.replace(/['"]/g, '');
                    meta.attr = "Wtf:qtip='" + tip + "' Wtf:qtitle='Description' ";
                    value=value.replace(/<\/?[^>]+(>|$)/g, "").replace(/&nbsp;/g, ' '); //SDP-7622
                    rec.set('description',value);
                    return value;
                }
            },{
                header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
                align:'center',
                dataIndex:"",
                width:80,
                hidden:this.readOnly,
                renderer: this.deleteRenderer.createDelegate(this)
            });
                
        this.cm = new Wtf.grid.ColumnModel(columnArr);
        this.gridBbar = [];
        this.debitTextValue = new Wtf.Toolbar.TextItem(""); // debit value
        this.gridBbar.push('->', "<B>" + WtfGlobal.getLocaleText("acc.je.debitAmt") + ":</B>", this.debitTextValue);
        this.debitTextValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(0, WtfGlobal.getCurrencySymbol()) + "</B>"; // Setting Default value as 0
        this.gridBbar.push("-");
        this.creditTextValue = new Wtf.Toolbar.TextItem(""); // credit value
        this.gridBbar.push('->', "<B>" + WtfGlobal.getLocaleText("acc.je.creditAmt") + ":</B>", this.creditTextValue);
        this.creditTextValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(0, WtfGlobal.getCurrencySymbol()) + "</B>"; // Setting Default value as 0
        /* Showing Unbalanced Amount if Credit & Debit Amount is not same initially it is zero*/
        this.gridBbar.push("-");
        this.unbalancedTextValue = new Wtf.Toolbar.TextItem(""); // Unbalanced Amount Value
        this.gridBbar.push('->', "<B>" + WtfGlobal.getLocaleText("acc.je.unbalancedAmt") + ":</B>", this.unbalancedTextValue);
        this.unbalancedTextValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(0, WtfGlobal.getCurrencySymbol()) + "</B>";
                
        this.grid = new Wtf.grid.EditorGridPanel({
            region :"center",
        layout:'fit',
        store:this.gridStore,
        sm:this.selModel,
        cls:'gridFormat',
        clicksToEdit:1,
        stripeRows :true,
        height:240,
        disabledClass:"newtripcmbss",
        viewConfig:{
            forceFit:false
        },
        autoScroll:true,
        forceFit:true,  
//        plugins:[this.summary],
//        disabled:this.readOnly||this.isViewTemplate,
        cm: this.cm,
        bbar:this.gridBbar
    //columns:
    });
    var colModelArray = GlobalColumnModel[this.moduleid];
        this.grid.store.on({
            update: this.updateCNDNAmount,
            load: this.updateCNDNAmount,
            datachanged: this.updateCNDNAmount,
            scope: this
        });
        
    WtfGlobal.updateStoreConfig(colModelArray, this.gridStore)
        this.grid.on('rowclick',this.handleRowClick,this);
        this.grid.on('afteredit',this.gridAfterEdit,this);
        this.grid.on('cellclick',this.RitchTextBoxSetting,this);
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
        this.cmbAccount.addNewFn=this.openCOAWindow.createDelegate(this)
        this.cmbAccount.on('select',this.afterAccountSelection,this);
        this.cmbAccount.on('change', function (combo, newVal, oldVal) {
            if (newVal != oldVal && oldVal != '') {
                var rec = WtfGlobal.searchRecord(this.accountStore, oldVal, 'accountid')
                if (rec) {
                    this.accountStore.remove(rec);
                }
            }
        }, this);
        this.selModel.on('rowselect', this.fetchAccountBalence, this);
        this.selModel.on("selectionchange", function(){
            if (this.grid.selModel.getCount() >= 1 ) {
                if (Wtf.getCmp("deleteButton"  + this.id))
                    Wtf.getCmp("deleteButton"  + this.id).enable();
            } else {
                if (Wtf.getCmp("deleteButton"  + this.id))
                    Wtf.getCmp("deleteButton"  + this.id).disable();
            }
        },this);
    },

    setCurrencyid:function(currencyid,rate,symbol){
        this.symbol=symbol;
        this.currencyid=currencyid;
        this.rate=rate;
        for(var i=0;i<this.gridStore.getCount();i++){
            this.gridStore.getAt(i).set('currencysymbol',this.symbol);
//            this.store.getAt(i).set('currencyrate',this.rate);
        }
        this.grid.getView().refresh();
    //     this.store.commitChanges();

     },
    openCOAWindow:function(){
        this.grid.stopEditing();
        callCOAWindow(false, null, "coaWin");
        Wtf.getCmp("coaWin").on("update",function(){this.accountStore.reload()},this);
    },
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
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
     var val=record.data.description;
    if(Wtf.account.companyAccountPref.proddiscripritchtextboxflag!=0 && !this.readOnly){
        if(fieldName == "description"){
            if (Wtf.account.companyAccountPref.proddiscripritchtextboxflag==1) {
                this.prodDescTextArea = new Wtf.form.TextArea({// Just make new Wtf.form.TextArea to new Wtf.form.HtmlEditor to fix ERP-8675
                    name: 'remark',
                    id: 'descriptionRemarkTextAreaId'
                });
                 val= Wtf.util.Format.htmlDecode(record.data.description);
                 val=val.replace(/&nbsp;/g, ' ');
            } else if (Wtf.account.companyAccountPref.proddiscripritchtextboxflag==2){
                this.prodDescTextArea = new Wtf.form.HtmlEditor({// Just make new Wtf.form.TextArea to new Wtf.form.HtmlEditor to fix ERP-8675
                    name: 'remark',
                    id: 'descriptionRemarkTextAreaId'
                });
            }
            
          //  var val=record.data.description;
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
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                var rec = store.getAt(rowindex);
                var isDebit = true;
                store.remove(rec);
                if (rec) {
                    isDebit = rec.data.debit;
                    var rec = WtfGlobal.searchRecord(this.accountStore, rec.data.accountid, 'accountid');
                    if (rec) {
                        this.accountStore.remove(rec);
                    }
                }
                if(rowindex==total-1){
                    if (this.type == '3'){
                        this.addDebitRow=true;
                    }
                    this.addNewRow();
                }
                if(this.type == '2'){//Party JE
                    if(total-1 == 1 && rowindex == 0 && isDebit){
                        this.addDebitRow=true;
                    }
                    this.addNewRow();
                }
                this.fireEvent('datachanged',this);
                this.accountBalanceTplSummary.overwrite(this.accountBalanceTpl.body,{accounatName:"&nbsp;&nbsp;&nbsp;&nbsp;",endingBalance:"&nbsp;&nbsp;&nbsp;&nbsp;"});
            }, this);
        } else if (e.getTarget(".serialNo-gridrow")){
            var gridStore= grid.getStore();
            var record = gridStore.getAt(rowindex);
            if(record.data['masterTypeValueOfAccount']==Wtf.masterTypeValueOfAccount['GSTTypeAccount'])
            {
               this.openWindowForSelectingGST(record,rowindex);                 
            }
        }
        if(!(this.isViewTemplate || this.readOnly) && e.target.className == "pwndBar2 shiftrowupIcon") {
            moveSelectedRowFormasterItems(grid,0,rowindex);
        }
        if(!(this.isViewTemplate || this.readOnly) && e.target.className == "pwndBar2 shiftrowdownIcon") {
            moveSelectedRowFormasterItems(grid,1,rowindex);
        }
    },
    gridAfterEdit: function(obj) {
        if (obj.field == "accountid") {
            var accRec = WtfGlobal.searchRecord(this.accountStore, obj.value, 'accountid');
            var haveToPostJe = accRec ? accRec.data.haveToPostJe : false;
            if (haveToPostJe) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText({key: "acc.field.soyoucannotpostje", params: [accRec ? accRec.data.usedIn : ""]})], 0);
                this.accountStore.remove(accRec);
                var productComboIndex = WtfGlobal.searchRecordIndex(this.gridStore, obj.value, 'accountid');
                if (productComboIndex >= 0) {
                    obj.record.set("accountid", "");
                }
            }
        }
    },
    addNewRow:function(amount){
        for(var i=0;i<this.gridStore.getCount();i++){
            if(this.gridStore.getAt(i).data['accountid'].length<=0
                ||!(this.gridStore.getAt(i).data['dramount']
                    ||this.gridStore.getAt(i).data['cramount']))
                return;
        }
        var debit=(this.gridStore.getCount()==0)?true:amount>=0
        
        if ((this.type == '3' || this.type == '2') && this.addDebitRow) {
            debit=true
            this.addDebitRow=false;
        }
        
        var rec = new this.gridRec({
            debit:debit,
            accountid:'',
            description:'',
            currencysymbol:this.symbol,
            dramount:0,//(amount>=0?amount:null),
            cramount:0//(amount<0?-amount:null)
        });
        rec.beginEdit();
        var fields=this.gridStore.fields;
        for(var x=0;x<fields.items.length;x++){
            var value='';
            if(fields.get(x).name.indexOf('Custom_') != -1){
                rec.set(fields.get(x).name, value);
            }
        }      
        rec.endEdit();
        rec.commit();
    this.gridStore.add(rec);
    this.grid.getView().refresh();
    },
addPaidTo:function(){
    addMasterItemWindow('17');
    Wtf.getCmp("masterconfiguration").on('update', function(){
        this.paidTo.store.reload();
    }, this);
      
},
addNewCreditRow1: function (combo, record) {
    this.accountStore.baseParams.ignoreBankAccounts = false;
    this.accountStore.baseParams.ignoreCashAccounts = false;
    this.accountStore.baseParams.ignoreGLAccounts = false;
    this.accountStore.baseParams.ignoreGSTAccounts = false;

    this.accountStore.load({
        params: {
            recordids: "'" + record.data.accountid + "'"
},
        callback: this.onAccountStoreLoad,
        scope: this
    });
},
    /**
     * If user select ITC then load grid with Input GST accounts.
     */
    addNewRowsForITC: function(combo, record) {
        this.accountStore.baseParams.ignoreBankAccounts = true;
        this.accountStore.baseParams.ignoreCashAccounts = true;
        this.accountStore.baseParams.ignoreGLAccounts = true;
        this.accountStore.baseParams.ignorecustomers = true;
        this.accountStore.baseParams.ignorecustomers = true;
        this.accountStore.load({
            params: {
                defaultaccountid: "'" + Wtf.GSTDefaultAccount.InputCGST + "','" + Wtf.GSTDefaultAccount.InputSGST + "','"
                        + Wtf.GSTDefaultAccount.InputIGST + "','" + Wtf.GSTDefaultAccount.InputUTGST + "','"+Wtf.GSTDefaultAccount.CESS+"'"
            },
            callback: this.onITCAccountStoreLoad,
            scope: this
        });
    },
    onITCAccountStoreLoad: function(recArr) {
        if (this.grid.store.getCount() == 2 || this.isEdit) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.field.ResetAccount"), WtfGlobal.getLocaleText("acc.je.gridrefreshmsgforitc"), function(btn) {
                if (btn != "yes") {
                    return;
                }
                this.addNewITCAccountRow(recArr);
            }, this);

        } else {
            this.addNewITCAccountRow(recArr);
        }
    },
    addNewITCAccountRow: function(recArr) {
        this.cmbAccount.store.add(recArr);
        this.grid.store.removeAll();
        for (var k = 0; k < recArr.length; k++) {
            var record = recArr[k];
            var rec = new this.gridRec({
                debit: false,
                accountid: record.data['accountid'],
                description: '',
                currencysymbol: this.symbol,
                dramount: 0, //(amount>=0?amount:null),
                cramount: 0,
                exchangeratefortransaction: 1,
                currencysymbolaccount: this.symbol,
                mappedaccountid: record.data['mappedaccountid']
            });
            rec.beginEdit();
            var fields = this.gridStore.fields;
            for (var x = 0; x < fields.items.length; x++) {
                var value = '';
                if (fields.get(x).name.indexOf('Custom_') != -1) {
                    rec.set(fields.get(x).name, value);
                }
            }
            rec.endEdit();
            rec.commit();
            this.gridStore.add(rec);
        }
        this.addBlankRow();
    },
    /**
     * Add blank row in grid.
     */
    addBlankRow: function() {
        var rec = new this.gridRec({
            debit: true,
            accountid: '',
            description: '',
            currencysymbol: this.symbol,
            dramount: 0, //(amount>=0?amount:null),
            cramount: 0//(amount<0?-amount:null)
        });
        rec.beginEdit();
        var fields = this.gridStore.fields;
        for (var x = 0; x < fields.items.length; x++) {
            var value = '';
            if (fields.get(x).name.indexOf('Custom_') != -1) {
                rec.set(fields.get(x).name, value);
            }
        }
        rec.endEdit();
        rec.commit();
        this.gridStore.add(rec);
        this.grid.getView().refresh();
    },
onAccountStoreLoad: function (recArr) {
    var record = WtfGlobal.searchRecord(this.pmtMethod.store, this.pmtMethod.getValue(), 'methodid');
    this.cmbAccount.store.add(recArr);
    if (this.grid.store.getCount() == 2 || this.isEdit) {

        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.field.ResetAccount"), WtfGlobal.getLocaleText("acc.je.gridrefreshmsg"), function (btn) {
            if (btn != "yes") {
                return;
            }
            this.addNewCreditRow(record);
        }, this);

    } else {
        this.addNewCreditRow(record);
    }
},
addNewCreditRow:function(record){
    this.grid.store.removeAll();
    this.Currency.setValue(record.data['acccurrency']);
    this.Currency.disable();
    var currencyRec=WtfGlobal.searchRecord(this.currencyStoreCMB, this.Currency.getValue(), 'currencyid');
    this.symbol = currencyRec.data['symbol'];
    var accid = record.data['accountid'];
    //Set externalcurrencyrate on payment method change
    this.externalcurrencyrate=currencyRec.data.exchangerate;
    var isaccountpresent = 0;
    this.accountStore.each(function (rec) {
        var gridaccid = rec.data['accountid'];
        if (gridaccid == accid) {
            isaccountpresent = isaccountpresent + 1;
            isaccountpresent = isaccountpresent;
        }


    }, this);
    if (isaccountpresent == 0) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.journal.pmtaccount")], 0);
        return;
    } 
    var rec = new this.gridRec({
        debit:false,
        accountid:record.data['accountid'],
        description:'',
        currencysymbol:this.symbol,
        dramount:0,//(amount>=0?amount:null),
        cramount:0,
        exchangeratefortransaction:1,
        currencysymbolaccount:this.symbol
    });
    rec.beginEdit();
    var fields=this.gridStore.fields;
    for(var x=0;x<fields.items.length;x++){
        var value='';
        if(fields.get(x).name.indexOf('Custom_') != -1){
            rec.set(fields.get(x).name, value);
        }
    }      
    rec.endEdit();
    rec.commit();
    this.pmtMethodAcc.setValue(record.data['accountname'])
    this.gridStore.add(rec);
    var paymentMethodAccountID=record.data['accountid'];
    rec.set('mappedaccountid',paymentMethodAccountID);
    this.cmbAccount.setValue(paymentMethodAccountID);  
    if(this.type==3 && record.data['detailtype']==2){
      //  rec.set('mappedaccountid',record.data['accountid']);
        rec.set('masterTypeValueOfAccount','3');
       // this.cmbAccount.setValue(record.data['accountid']);  
        //   this.accountStore.load();
        if((this.isEdit || this.copyInv) && this.jeDetails!="" && this.jeDetails!=null){//Edit or copy case
            if(paymentMethodAccountID != this.jeDetails.data.pmtmethodaccountid){
                this.chequeDetails.loadChequeSequenceFormatOnMethodAccountChanged(paymentMethodAccountID,true);
            } else{
                this.chequeDetails.loadChequeSequenceFormatOnMethodAccountChanged(paymentMethodAccountID,false); 
            }
        } else{
            this.chequeDetails.loadChequeSequenceFormatOnMethodAccountChanged(paymentMethodAccountID,false); 
        }
        
        this.savePrintBttn.show();
        this.chequeDetails.show();
        if(this.newPanel){
            this.newPanel.doLayout();
        }
    }else{
        if(this.chequeDetails.isVisible() && this.newPanel && this.newPanel.items && this.newPanel.items.items[0]){// && this.newPanel.items.items[0].height){
            this.newPanel.items.items[0].setHeight(this.newPanel.items.items[0].getInnerHeight()-210);
            this.newPanel.doLayout();
        }
        this.chequeDetails.hide(); 
        this.savePrintBttn.hide();
    }
    
    this.currencyid = record.data['acccurrency'];
    this.setCurrencyid(record.data['acccurrency'],1,record.data['acccurrencysymbol']);
//    this.fetchAccountBalence(undefined,undefined,rec);
    this.currencychanged = true;
    this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.entryDate.getValue()),tocurrencyid:this.Currency.getValue()}});
    this.accountBalanceTplSummary.overwrite(this.accountBalanceTpl.body,{accounatName:"&nbsp;&nbsp;&nbsp;&nbsp;",endingBalance:"&nbsp;&nbsp;&nbsp;&nbsp;"});
}, 
    
    addPaymentMethod:function(){
        PaymentMethod('PaymentMethodWin');
        Wtf.getCmp('PaymentMethodWin').on('update', function(){
            this.pmtStore.reload();
        }, this);
    },
    checkmandatory:function(obj){
        //check mandatory fields
        if (Wtf.isEmpty(this.pmtMethod.getValue())) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.selectPmtMethodFirst")], 2);// 'Select Payment method first''
            return true;
        }
    },
    checkEditable:function(obj){
        /*
         * Restrict all field except description
         */
        if ((this.isAllowedSpecificFields) && (obj.field == "debit" || obj.field == "accountid" || obj.field == "dramount" || obj.field == "cramount"
                || obj.field == "exchangeratefortransaction" || obj.field == "appliedGst" || obj.field == "accountid")) {
                    obj.cancel = true;
        }
    if(this.type==3 && this.checkmandatory()) { //type 3 is for fund transfer 
        obj.cancel=true;
        return;
    }
    if(obj.record.data['debit']){
        if(obj.field=='cramount') obj.cancel=true;
    }else{
        if(obj.field=='dramount') obj.cancel=true;
    }
    if(!obj.record.data['debit']){
        if(this.typeValue == 3 ) {
            if(obj.field=='accountid') obj.cancel=true;  
        } 
    }
    if(this.typeValue == 3 && this.chequeDetails){
        if(obj.field=='debit') obj.cancel=true; 
    }
    if(obj.field=='exchangeratefortransaction'){
        var currencyRec=WtfGlobal.searchRecord(this.currencyStoreCMB, this.Currency.getValue(), 'currencyid');
        var JeCurrencySymbol = currencyRec.data['symbol'];
        if(obj.record.data['currencysymbolaccount'] == JeCurrencySymbol){
            obj.cancel=true;
        }
    }
    },

    calGridTotal:function(column){
        var total=0.0;
        for(var i=0;i<this.gridStore.getCount();i++){
            var temp=parseFloat(this.gridStore.getAt(i).data[column]);
            total=total+(isNaN(temp)?0:temp);
        }
        return WtfGlobal.conventInDecimalWithoutSymbol(total);
    },

    updateRow:function(obj){
        this.isClosable=false          // Set Closable flag after edit grid
        if(obj.field=="debit"){
            if(obj.value&&obj.record.data['cramount']!=null){
                obj.record.set('dramount',obj.record.data['cramount']);
                //obj.record.set('cramount',null);
                obj.record.set('cramount',0);   //in journal entry issue shows o 
            }else if(!obj.value&&obj.record.data['dramount']!=null){
                obj.record.set('cramount',obj.record.data['dramount']);
                //obj.record.set('dramount',null);
                obj.record.set('dramount',0);
            }
        }
    if(obj.field=="accountid"){
        var accountid= obj.record.data.accountid;
        var index=this.accountStore.find("accountid",accountid);
        if(index!=-1){
            obj.record.set('mappedaccountid',this.accountStore.getAt(index).data.mappedaccountid);
            obj.record.set('masterTypeValueOfAccount',this.accountStore.getAt(index).data.masterTypeValue);
                
            var accountMasterType = this.accountStore.getAt(index).data.masterTypeValue;
//            if(this.typeValue ==3 && accountMasterType && accountMasterType == 3){
//                if(!obj.record.data['debit']){
//                    this.chequeDetails.show();
//                    if(this.newPanel){
//                        this.newPanel.doLayout();
//                    } 
//                }
//            }else{
//                if(this.chequeDetails.isVisible() && this.newPanel && this.newPanel.items && this.newPanel.items.items[0] && this.newPanel.items.items[0].height){
//                    this.newPanel.items.items[0].setHeight(this.newPanel.items.items[0].getInnerHeight()-210);
//                    this.newPanel.doLayout();
//                }
//                this.chequeDetails.hide();
//            }
            if(this.typeValue==3){
                var currencyRec=WtfGlobal.searchRecord(this.currencyStoreCMB, this.Currency.getValue(), 'currencyid');
                var JeCurrencySymbol = currencyRec.data['symbol'];
                var JeCurrencyId = currencyRec.data['currencyid'];
            
                var selectedAccountRec = WtfGlobal.searchRecord(this.accountStore, accountid, 'accountid');
                var accountCurrencySymbol = selectedAccountRec!=null?selectedAccountRec.data['currencysymbol']:JeCurrencySymbol;
                var accountCurrencyId = selectedAccountRec!=null?selectedAccountRec.data['currencyid']:JeCurrencyId;
                obj.record.set('currencysymbolaccount',accountCurrencySymbol);
                if(JeCurrencySymbol==accountCurrencySymbol){
                    obj.record.set('exchangeratefortransaction',1);
                } else {
                    var exchangeRate = this.getOneCurrencyToOtherExchangeRate(JeCurrencyId,accountCurrencyId);
                    obj.record.set('exchangeratefortransaction',exchangeRate);
                }
            }
            if(this.typeValue == 1){
                obj.record.set('appliedGst',this.accountStore.getAt(index).data.appliedGst);
            }
        } 
    }
        var totaldr=this.calGridTotal('dramount');
        var totalcr=this.calGridTotal('cramount');
        if (this.type == 3 && this.gridStore.getCount() < 2) {   // type 3 is for fund transfer 
            this.addNewRow(totalcr - totaldr);
        } else if (this.type == 2 && this.generateCNDN.getValue() && this.gridStore.getCount() < 2) {
        /*
         * type 2 is party Journal Entry Genrate Credit and debit note 
         * Grid Should not have more than two entry.    
         */
            this.addNewRow(totalcr - totaldr);
        } else if (this.type != 3 && (this.type != 2 || (this.type == 2 && !this.generateCNDN.getValue()))) {
            this.addNewRow(totalcr - totaldr);
        }
        this.updateCNDNAmount();
    },

    getRecords:function(){
        var data=[];
        var amount;
        var accountid="";
        var exchangeratefortransaction=1;
        var appliedGst = '';
        for(var i=0;i<this.gridStore.getCount();i++){
            var rec=this.gridStore.getAt(i);
            accountid=rec.data['accountid'];
            if(accountid.length<=0||(rec.data['dramount']<=0&&rec.data['cramount']<=0))continue;
            if(rec.data['dramount'])
                amount=rec.data['dramount']
            else
                amount=rec.data['cramount']
            var accountpersontype="";
           var index=this.accountStore.findBy( function(record){
                 if(record.data['accountid']==accountid)
                     return true;
                 else
                     return false;
            });            
            if(index!=-1){
                accountpersontype=this.accountStore.getAt(index).data.accountpersontype;
            } 
            
            var desc = '';
            if(rec.data['description']!=''){
               desc = rec.data['description'];
               desc = desc.replace(/\%/ig,"%25");
               desc = desc.replace(/\+/ig, "%2b");
               desc = encodeURI(desc);  //Encoded for Special character like '%' New Line & Double quotes.
            }
            exchangeratefortransaction = rec.data['exchangeratefortransaction'];
            appliedGst = rec.data['appliedGst'];
            var rowCount=i+1;
            data.push('{debit:"'+rec.data['debit']+'",accountid:"'+rec.data['mappedaccountid']+'",customerVendorId:"'+rec.data['accountid']+'",description:"'+desc+'",accountpersontype:"'+accountpersontype+'",rowid:"'+rowCount+'",amount:"'+amount+'",exchangeratefortransaction:"'+exchangeratefortransaction+'",appliedGst:"'+appliedGst+'",customfield:'+WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13)+"}");
        }//for        
        if(this.bankChargesAccount.getValue()!='' && this.bankChargesAccount.getValue()!=undefined){  //Add this only when we select Bank charge account to save in JournalEntryDetails table
            var bankcharge = (this.bankCharges.getValue()*1)!=undefined ? (this.bankCharges.getValue()*1) : 0;
            data.push('{debit:"true", isbankcharge:"true", amount:"'+bankcharge+'",accountid:"'+this.bankChargesAccount.getValue()+'",description:"'+encodeURI("Bank Charges")+'"}');    //Added Bank Charges & Bank Charge Account ID in details array
        }
        return data;
    },

    setJENumber:function(isSelectNoFromCombo){
        if (isSelectNoFromCombo){
                this.fromnumber = Wtf.autoNum.JournalEntry;
         } else if(Wtf.account.companyAccountPref.autojournalentry&&Wtf.account.companyAccountPref.autojournalentry.length>0){            
                WtfGlobal.fetchAutoNumber(Wtf.autoNum.JournalEntry, function(resp){if(this.isEdit)this.jeNo.setValue(resp.data)}, this);
         }
        },
    setTrNoteNumber:function(isSelectNoFromCombo){
        var format="";var temp2="";
        var val=1;
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
            this.fromnumberCN = temp2;
        } else if(format&&format.length>0){
            WtfGlobal.fetchAutoNumber(temp2, function(resp){if(this.isEdit)this.no.setValue(resp.data)}, this);
        }
   },
   setTrNoteNumberDN:function(isSelectNoFromCombo){
        var format="";var temp2="";
        var val=0;
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
            this.fromnumberDN = temp2;
        } else if(format&&format.length>0){
            WtfGlobal.fetchAutoNumber(temp2, function(resp){if(this.isEdit)this.noDN.setValue(resp.data)}, this);
        }
   },
   
   filterStore:function(store){
         this.cmbAccount.store.filterBy(function(rec){
            if(rec.data.groupname=='Accounts Payable'||rec.data.groupname=='Accounts Receivable')
                return true
            else
                return false
        },this)
    }
    ,hideCNDN:function(){
        WtfGlobal.hideFormElement(this.sequenceFormatComboboxCN);
        this.sequenceFormatComboboxCN.allowBlank=true;
        WtfGlobal.hideFormElement(this.no);
        this.no.allowBlank = true;
        WtfGlobal.hideFormElement(this.sequenceFormatComboboxDN);
        this.sequenceFormatComboboxDN.allowBlank=true;
        this.noDN.allowBlank = true;
        WtfGlobal.hideFormElement(this.noDN);  
    },showCNDN:function(){
        WtfGlobal.showFormElement(this.sequenceFormatComboboxCN);
        this.sequenceFormatComboboxCN.allowBlank=false;
        WtfGlobal.showFormElement(this.no);
        this.no.allowBlank = false;
        WtfGlobal.showFormElement(this.sequenceFormatComboboxDN);
        this.sequenceFormatComboboxDN.allowBlank=false;
        this.noDN.allowBlank = false;
        WtfGlobal.showFormElement(this.noDN);
    },    
    setroundedvalue: function (obj) {    //Round the value of Bank charges.
        var bankcharges = obj.getValue();
        if (bankcharges != "" && bankcharges != undefined) {
        var roundedvalue = parseFloat(getRoundofValueWithValues(bankcharges, Wtf.AMOUNT_DIGIT_AFTER_DECIMAL).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
                obj.setValue(roundedvalue);
        }
    },
    setroundedvalue: function (obj) {    //Round the value of Bank charges.
        var bankcharges = obj.getValue();
        if (bankcharges != "" && bankcharges != undefined) {
        var roundedvalue = parseFloat(getRoundofValueWithValues(bankcharges, Wtf.AMOUNT_DIGIT_AFTER_DECIMAL).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL));
                obj.setValue(roundedvalue);
        }
    },
    checkDetails: function (grid) {
        var GlobalcolumnModel = GlobalColumnModel[this.moduleid];
        var data = "";
        if (GlobalcolumnModel) {
            for (var cnt = 0; cnt < GlobalcolumnModel.length; cnt++) {
                var fieldname = GlobalcolumnModel[cnt].fieldname;
                var fieldlabel = GlobalcolumnModel[cnt].fieldlabel;
                var fieldtype = GlobalcolumnModel[cnt].fieldtype;
                var isMendatory = GlobalcolumnModel[cnt].isessential;
                if (isMendatory)
                {
                    var array = grid.store.data.items;
                    var length=array.length - 1;
                    if(this.type==3){
                        length=array.length;
                    }
                    for (var i = 0; i < length; i++) {
//                        var customfield = array[i].get(fieldlabel);
                        var value = array[i].get(fieldname);

                        if (value == "" || value == "1234" || value==undefined) {
                            data += " " + fieldlabel + "(row" + (i + 1) + "),";
            }
                    }
                }

            }
        }
        if (data.length > 1) {
            data = data.substr(0, data.length - 1);
                }
                return data;
            },
    populateDimensionValueingrid: function (rec) {
        if(this.type==3){
            rec.modulename = Wtf.fund_transafer_journal_entry;
        }
        WtfGlobal.populateDimensionValueingrid(this.moduleid, rec, this.grid);
    },
    conversionFactorRenderer:function(value,meta,record) {
        var currencysymbol=((record==undefined||record.data.currencysymbol==null||record.data['currencysymbol']==undefined||record.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():record.data['currencysymbol']);
        var currencysymbolaccount=((record==undefined||record.data.currencysymbolaccount==null||record.data['currencysymbolaccount']==undefined||record.data['currencysymbolaccount']=="")?currencysymbol:record.data['currencysymbolaccount']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
        return "1 "+ currencysymbolaccount +" = " +value+" "+currencysymbol;
    },
    getOneCurrencyToOtherExchangeRate:function(fromCurrency,toCurrency){
    if(this.Currency.getValue()!=''){
        var fromCurrencyRecord = WtfGlobal.searchRecord(this.currencyStoreCMB, fromCurrency, "currencyid");
        var exRateForFromCurrency = fromCurrencyRecord.data['exchangerate'];
        
        var toCurrencyRecord = WtfGlobal.searchRecord(this.currencyStoreCMB, toCurrency, "currencyid");
        var exRateForToCurrency = toCurrencyRecord.data['exchangerate'];
        
        if(this.externalcurrencyrate!=undefined && this.externalcurrencyrate!=0){
            exRateForFromCurrency = this.externalcurrencyrate;
        }
        var exRateToReturn = 0;
        exRateToReturn = exRateForFromCurrency/exRateForToCurrency;
        return exRateToReturn;
    } else {
        return 1;
    }
    },
    applyExchangeRateToGrid:function(excludeFromUpdating){
        if(!excludeFromUpdating){
        var store = this.grid.getStore();
        var currencyRec=WtfGlobal.searchRecord(this.currencyStoreCMB, this.Currency.getValue(), 'currencyid');
        var JeCurrencyId = currencyRec.data['currencyid'];
        var rec= '',selectedAccountRec='',accountCurrencyId='',exRate=1;
        for(var x=0;x< store.getCount();x++){
            rec = store.getAt(x);
            selectedAccountRec = WtfGlobal.searchRecord(this.accountStore, rec.data['accountid'], 'accountid');
            accountCurrencyId = selectedAccountRec!=null?selectedAccountRec.data['currencyid']:JeCurrencyId;
            if(JeCurrencyId != accountCurrencyId){
                exRate = this.getOneCurrencyToOtherExchangeRate(JeCurrencyId,accountCurrencyId);
            }    
            rec.set('exchangeratefortransaction',exRate);
        }
        }
    },
    gstRenderer:function(v,m,rec){
        return "<div  wtf:qtip=\""+((Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA)?WtfGlobal.getLocaleText("acc.tax.desc"):WtfGlobal.getLocaleText("acc.gst.desc"))+"\" wtf:qtitle='"+((Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA)?WtfGlobal.getLocaleText("acc.tax.desc.title"):WtfGlobal.getLocaleText("acc.gst.desc.title"))+"' class='"+getButtonIconCls(Wtf.etype.serialgridrow)+"'></div>";
    },
    openWindowForSelectingGST:function(record,rowindex){
        var gstCodeSelected = record.data['accountid'];
        var appliedGst = record.data['appliedGst']?record.data['appliedGst']:'';
        this.GSTTaxesWindow= new Wtf.account.GSTTaxes({
            id:'gsttaxeswindow',
            border: false, 
            accountId:gstCodeSelected,
            appliedGst:appliedGst,
            isEdit : this.isEdit
        });
        this.GSTTaxesWindow.on('beforeclose',function(winObj){
            if(winObj.isSubmitBtnClicked) {
                this.setGstToSelectedRow(winObj.getSelectedRecords(),record,rowindex);
            }
        },this);
        this.GSTTaxesWindow.show();
    },
    setGstToSelectedRow:function(jsonArray,record,rowindex){
         var jsonArrayObj = eval(jsonArray);
         var appliedGst = jsonArrayObj['0'].appliedGst;
         var recordToSet=this.gridStore.getAt(rowindex);
         recordToSet.set('appliedGst',appliedGst);
    },
    isValidForIncludingInGSTReport: function(){        
        var valueToReturn = false;
        if(this.includeInGst && this.includeInGst.getValue()){
            for(var i=0;i<this.grid.getStore().getCount();i++){
                var rec = this.grid.getStore().getAt(i);
                var masterType = rec.data['masterTypeValueOfAccount'];
                if(masterType == Wtf.masterTypeValueOfAccount['GSTTypeAccount']){
                    valueToReturn=true;
                    break;
                }
            }
        } else {
            valueToReturn = true;
        }
        return valueToReturn;        
    },
    isValidForGSTTypeOfAccounts: function(){        
        var valueToReturn = true;
        if(this.includeInGst && this.includeInGst.getValue()){
            for(var i=0;i<this.grid.getStore().getCount();i++){
                var rec = this.grid.getStore().getAt(i);
                var masterType = rec.data['masterTypeValueOfAccount'];
                if(masterType == Wtf.masterTypeValueOfAccount['GSTTypeAccount'] && (rec.data['appliedGst']==undefined || rec.data['appliedGst']=='')){
                    valueToReturn=false;
                    break;
                }
            }
        }
        return valueToReturn;        
    },
    
    deleteSelectedRecord: function() {
    var arr = [];
    var store = this.grid.getStore();
    var selectedCount = this.grid.selModel.getCount();
    var index=0;
    var rowindex=0;
    var message = "";
    var total = store.getCount();// Total count of rows
    var isDebit = true;// check for debit/credit row identification
    if(this.isEdit){
         message += "</b> Selected record will be Removed. </br>" + WtfGlobal.getLocaleText("acc.nee.48")
         Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), message, function(btn) {
            if (btn != "yes")
                return;            
            for (rowindex = 0; rowindex < selectedCount; rowindex++) {
                arr[rowindex] = this.grid.selModel.getSelections()[index];
                if (arr[rowindex] != "") {
                    var id = arr[rowindex].id;
                    isDebit= arr[rowindex].data.debit;//get record type debit/credit
                    store.remove(store.getById(id));
                }
            }
            this.grid.fireEvent('datachanged', this);
            /**
             * If Party JE then add new row after delete selected rows
             */
            if(this.type == '2'){//Party JE
                if(total-1 == 1 && isDebit){
                    this.addDebitRow=true;
                }
                this.addNewRow();
            }
        },this);
    } else{
        for (rowindex = 0; rowindex < selectedCount; rowindex++) {
            arr[rowindex] = this.grid.selModel.getSelections()[index];
            if (arr[rowindex] != "") {
                var id = arr[rowindex].id;
                isDebit= arr[rowindex].data.debit;//get record type debit/credit
                store.remove(store.getById(id));
            }
        }
        this.grid.fireEvent('datachanged', this);
        /**
         * If Party JE then add new row after delete selected rows
         */
        if(this.type == '2'){//Party JE
            if(total-1 == 1 && isDebit){
                this.addDebitRow=true;
            }
            this.addNewRow();
        }
    }    
} ,
checkSelections:function( scope, rowIndex, keepExisting, record){
        if (this.grid && this.grid.getStore() && rowIndex == this.grid.getStore().getCount() - 1) {
            return false;
        } else {
            return true;
        }
       
},  /* SDP-15926 ERP-41103 to show account code with account name after selection of account from dropdown */
cmbAccountComboRenderer: function (value, metadata, record, rowIndex, colIdex, store) {
    if (this.cmbAccount && this.cmbAccount.store) {
        var rec = WtfGlobal.searchRecord(this.cmbAccount.store, value, this.cmbAccount.valueField);
        if (rec == undefined || rec == null) {
            rec = WtfGlobal.searchRecord(this.cmbAccount.store, value, this.cmbAccount.displayField);            
        }
        var accountcode = rec != undefined ? (rec.data['acccode'] ?  "[" + rec.data['acccode'] + "] " : "") : "";
        return rec != undefined ? accountcode + rec.data[this.cmbAccount.displayField] : "";
    }
    return "";
}
});