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
/*      < COMPONENT USED FOR >
 *      1.Invoice details [isReceipt:true]
 *      2.Multiple Debit Account Payment [isReceipt:true][isMultiDebit:true]
 */
Wtf.account.OSDetailGrid=function(config){
    this.isReceipt=config.isReceipt; 
    this.isMultiDebit=config.isMultiDebit;
    this.advanceCnDnFlag=config.advanceCnDnFlag!=undefined?config.advanceCnDnFlag:false;
    this.isEdit=config.isEdit;
    this.symbol=""; 
    this.receiptObject=config.receiptObject; 
    this.isAdvPayment = false;
    this.winType=config.winType;
//    this.advancePayAcc = undefined;
    this.businessPerson=(config.isReceipt?'Customer':'Vendor');
    this.currrentAmount=config.amount;
    if(config.isTemplate!=undefined)
        this.isTemplate=config.isTemplate;
    else
        this.isTemplate=false;
    this.calMultiDebitFunc();
    this.cellClickView=config.cellClickView;
    this.autopopulate=false;
    this.gridRec = Wtf.data.Record.create ([
        {name:'select', type:'bool'},                                    
        {name:'billid'},
        {name:'journalentryid'},
        {name:'personid'},
        {name:'entryno'},
        {name:'billno'},
        {name:'transectionno'},
        {name:'creationdate',type:'date'},
        {name:'date',type:'date'},
        {name:'duedate',type:'date'},
        {name:'currencyid'},
        {name:'currencyidtransaction'},
        {name:'oldcurrencyrate'},
        {name:'currencyname'},
        {name:'currencysymbol'},
        {name:'currencynametransaction'},
        {name:'currencysymboltransaction'},
        {name:'oldcurrencysymbol'},
        {name:'vendorid'},
        {name:'vendorname'},
        {name:'personname'},
        {name: 'externalcurrencyrate'},
        {name:'amountdue', mapping:'amountduenonnegative'},
        {name:'amountDueOriginal'},
        {name:'amountDueOriginalSaved'},
//        {name:'amountdue'},
        {name:'taxpercent'},
        {name: 'taxamount'},
        {name:'isdebit'},
        {name:'prtaxid'},
        {name:'prpercent'},
        {name:'discount'},
        {name:'amount'},
        {name:'curamount'},
        {name:'memo'},
        {name:'payment'},
        {name:'accountid'},
        {name:'accountname'},
        {name:'amountpaid'},
        {name:'description'},
        {name:'costcenterid'},
        {name:'costcenterName'},
        {name:'customfield'},
        {name:'dramount'},
        {name:'deductDiscount'},
        {name: 'totalamount'},
        {name:'noteid'},
        {name:'noteno'},
        {name:'isClaimedInvoice'},
        {name:'exchangeratefortransaction'},
        {name:'gstCurrencyRate',defValue:'0.0'}
    ]);
    var store = new Wtf.data.Store({
        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.gridRec) 
    });
    
    this.typeStore=new Wtf.data.SimpleStore({
        fields:[{name:"typeid"},{name:"name"}],
        data:[[true,"Debit"],[false,"Credit"]]
    });
    
    this.cmbType=new Wtf.form.ComboBox({
        hiddenName:'isdebit',
        store:this.typeStore,
        valueField:'typeid',
        displayField:'name',
        mode: 'local',
        triggerAction:'all',
        forceSelection:true
    });
   
     this.taxRec = new Wtf.data.Record.create([
           {name: 'prtaxid',mapping:'taxid'},
           {name: 'prtaxname',mapping:'taxname'},
           {name: 'taxdescription'},
           {name: 'percent',type:'float'},
           {name: 'taxcode'},
           {name: 'accountid'},
           {name: 'accountname'},
           {name: 'applydate', type:'date'}

        ]);
        this.taxStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.taxRec),
    //        url: Wtf.req.account + 'CompanyManager.jsp',
            url : "ACCTax/getTax.do",
            baseParams:{
                mode:33
            }
        });
        this.transTax= new Wtf.form.FnComboBox({
            hiddenName:'prtaxid',
            //anchor: '100%',
            width:200,
            store:this.taxStore,
            //id:"paymenttaxcmb"+this.businessPerson,
            valueField:'prtaxid',
            forceSelection: true,
            displayField:'prtaxname',
            disabled:this.cellClickView,
            //addNewFn:this.calAmount.createDelegate(this),
            scope:this,
            displayDescrption:'taxdescription',
            selectOnFocus:true
           
        });
        
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.edit)){
            this.transTax.addNewFn=this.addTax.createDelegate(this);
        }
        
        this.transTaxAmount=new Wtf.form.NumberField({
            allowBlank: true,
            allowNegative: false,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            value:0
        });
        
        this.taxStore.on("load", function(store){
            var storeNewRecord=new this.taxRec({
                prtaxid:'-1',
                prtaxname:'None'
            });
            this.transTax.store.insert( 0,storeNewRecord);
         },this);
          this.taxStore.load();
    if(config.isNote){
        store.baseParams={onlyexpenseinv:false,nondeleted:true,deleted:false,ignorezero:true};}
    
    store.on('beforeload',function(){
        store.baseParams.includeFixedAssetInvoicesFlag = true;// in case of make/Receive Payments we need to include Fixed Asset Invoices also.
    },this)
    this.selectionModel =new Wtf.grid.CheckboxSelectionModel({singleSelect:true});
    
//    this.mulDebitCM=
    var mulDebitCMArr = [];      
    mulDebitCMArr.push({
        header:WtfGlobal.getLocaleText("acc.je.type"),  //"Type",
        editor: this.cmbType,
        renderer:Wtf.comboBoxRenderer(this.cmbType),
        width:200,
        disabled:this.cellClickView,
        dataIndex:'isdebit'
    },{
        header:'Account',//config.isReceipt? WtfGlobal.getLocaleText("acc.rem.217") : WtfGlobal.getLocaleText("acc.rem.30"),  //"Debit Account",
        dataIndex:'accountid',
        width:200,
        hidden : this.isAdvPayment,
        editor: this.cmbAccount=new Wtf.form.ExtFnComboBox({
                    hiddenName:'accountid',
                    store:this.accountStore,
                    minChars:1,
                    valueField:'accountid',
                    displayField:'accountname',
                    forceSelection:true,
                    hirarchical:true,
                    disabled:this.cellClickView,
                    addNewFn:this.openCOAWindow.createDelegate(this),
                    extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
                    mode: 'local',
                    typeAheadDelay:30000,
                    extraComparisionField:'acccode',// type ahead search on acccode as well.
                    listWidth:Wtf.account.companyAccountPref.accountsWithCode?750:450,
                    listeners: {
                               select: function(combo, selection) {
                                   Wtf.accountgridname=selection.data.accountname;
                                   Wtf.persongridid=selection.data.accountid;
                                 }
                          }
                }),
        renderer:Wtf.comboBoxRenderer(this.cmbAccount)
    },{
        header:WtfGlobal.getLocaleText("acc.saleByItem.gridAmount"),//config.isReceipt? WtfGlobal.getLocaleText("acc.rem.218") : WtfGlobal.getLocaleText("acc.rem.31"),  //"Debit Amount",
        dataIndex:'dramount',
        width:200,
        align:'right',
        renderer:this.drCurrencySymbol,
        summaryType:'sum',
        summaryRenderer:this.drCurrencySymbol.createDelegate(this),
        editor:new Wtf.form.NumberField({
            allowBlank: false,
            disabled:this.cellClickView,
            allowNegative:false,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        })
    },{
        header:WtfGlobal.getLocaleText("acc.invReport.desc"),  //"Description",
        dataIndex:"description",
        width:200,
        renderer:function(value,meta){
                   if(value) {                                 
                    meta.attr = "Wtf:qtip='"+value+"' Wtf:qtitle='Description' ";
                   }
                   return value;
            },
        editor:this.Description=new Wtf.form.TextArea({
            maxLength:200,
            disabled:this.cellClickView,
            allowBlank: false,
            xtype:'textarea'
        })
    },{
            header:WtfGlobal.getLocaleText("acc.rem.36"),  //"Tax (%)",
             dataIndex:"prtaxid",
           // id:this.id+"prtaxid",
            align:'right',
            width:200,  
            disabled:this.cellClickView,
            renderer:Wtf.comboBoxRenderer(this.transTax),
            editor:this.transTax
        },{
        header:WtfGlobal.getLocaleText("acc.taxReport.taxAmount"),
        dataIndex:"taxamount",
        align:'right',
        width:200,
        editor:this.transTaxAmount,
        renderer:this.setTaxAmount.createDelegate(this)
    },{
            header:WtfGlobal.getLocaleText("acc.mp.amtTaxTotal"),   //Amount with Tax
            dataIndex:'curamount',
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            width:200,
            disabled:this.cellClickView,
            align:'right',
            readOnly:true  
    });
    mulDebitCMArr = WtfGlobal.appendCustomColumn(mulDebitCMArr,GlobalColumnModel[config.moduleid],undefined,undefined,this.cellClickView );
    mulDebitCMArr.push({
       header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
        align:'center',
        hidden:this.cellClickView,
        width:100,
        renderer: this.deleteRenderer.createDelegate(this)
    });
    
    this.mulDebitCM = new Wtf.grid.ColumnModel(mulDebitCMArr);
    
    this.checkColumn = new Wtf.grid.CheckColumn({
        header: "",
        dataIndex: 'select',
        width: 20
     });
     
     
    this.DebitCreditNoteCM= new Wtf.grid.ColumnModel([this.checkColumn,{
        header:config.isReceipt? WtfGlobal.getLocaleText("acc.dimension.module.3") : WtfGlobal.getLocaleText("acc.dimension.module.4"),  //"Debit Account",
        dataIndex:'noteno',
        width:200
    },{
        header:WtfGlobal.getLocaleText("acc.saleByItem.gridAmount"),  //"Debit Amount",
        dataIndex:'amount',
        width:200,
        align:'right',
        renderer:this.drCurrencySymbol,
        summaryType:'sum',
        summaryRenderer:this.drCurrencySymbol.createDelegate(this)
    },{
        header:WtfGlobal.getLocaleText("acc.mp.amtDue"),  //"Amount Due",
        dataIndex:'amountdue',
        width:200,
        renderer:WtfGlobal.withoutRateCurrencySymbol,
        hidelabel:config.readOnly,
        hidden:config.readOnly
    },{
        header:(config.isNote?WtfGlobal.getLocaleText("acc.invoice.discount"):'<b>'+WtfGlobal.getLocaleText("acc.mp.entPay")+'</b>'),
        dataIndex:"payment",
        renderer:WtfGlobal.withoutRateCurrencySymbol,
        hidelabel:(config.isNote ||config.readOnly),
        hidden:(config.isNote ||config.readOnly || this.isTemplate),
        editor: this.payment=new Wtf.form.NumberField({
            name:'desc',
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        }),
        width:200
        },
     {
        header:WtfGlobal.getLocaleText("acc.invoice.gridAction"),//"Action",
        align:'center',
        width:100,
        renderer: this.deleteRenderer.createDelegate(this)
    }]);


    
        var columnArr =[];
        if(!config.readOnly){ //do not include in view only grid
            if(config.isNote) {            
                columnArr.push(this.selectionModel);				// For Credit Note and Debit Note              
            } else{            
                columnArr.push(this.checkColumn);					// For Make Payment and Recieve Payment                    	
            }
        }
        columnArr.push({ 
            header:(config.isReceipt?WtfGlobal.getLocaleText("acc.rem.32"):WtfGlobal.getLocaleText("acc.rem.33")),
            dataIndex:config.readOnly||config.isEdit?'transectionno':'billno',
            width: 300
        },{
            header:WtfGlobal.getLocaleText("acc.rem.34"),  //"Invoice Date",
            dataIndex:config.readOnly||config.isEdit?'creationdate':'date',
            align:'center',
            width: 150,
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.prList.dueDate"),  //"Due Date",
            dataIndex:'duedate',
            align:'center',
            width: 150,
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.rem.36"),  //"Tax (%)",
            dataIndex:'taxpercent',
            align:'right',
            disabled:this.cellClickView,
             width: 150,
            hidden: this.isTemplate,
            renderer:function(v){return'<div class="currency">'+v+'%</div>';}
         },{
            header:WtfGlobal.getLocaleText("acc.invoice.discount"),  //"Discount",
            dataIndex:'discount',
            align:'right',
            width: 150,
            hidden: this.isTemplate,
            renderer:WtfGlobal.withoutRateCurrencySymbol
        },{
            header:WtfGlobal.getLocaleText("acc.prList.invAmt"),
            dataIndex:'totalamount',
            align:'right',
            hidden: !this.isTemplate,
            width: 150,
            renderer:WtfGlobal.withoutRateCurrencySymbol
        },{
            header:config.readOnly?(config.isReceipt?WtfGlobal.getLocaleText("acc.prList.amtRec"):WtfGlobal.getLocaleText("acc.prList.amtPaid")):WtfGlobal.getLocaleText("acc.invoice.gridOriginalAmt"),
            dataIndex:'amount',
            align:'right',
            width: 150,
            renderer:!config.isNote?WtfGlobal.withoutRateCurrencySymbolTransaction:WtfGlobal.withoutRateCurrencySymbol
        },
        {
            header:WtfGlobal.getLocaleText("acc.field.OriginalAmountDue"),
            dataIndex:'amountDueOriginal',
            align:'right',
            width: 150,
            hidelabel:config.isNote!=undefined?(config.isNote):false,
            hidden: config.isNote!=undefined?(config.isNote):false,
            renderer:!config.isNote?WtfGlobal.withoutRateCurrencySymbolTransaction:WtfGlobal.withoutRateCurrencySymbol
        },
        {
            header:WtfGlobal.getLocaleText("acc.setupWizard.curEx"), 
            dataIndex:'exchangeratefortransaction',
            hidelabel:config.isNote!=undefined?(config.isNote):false,
            hidden: config.isNote!=undefined?(config.isNote):false,
            renderer:this.conversionFactorRenderer,
            width: 150,
//            renderer:WtfGlobal.withoutRateCurrencySymbol,
//            hidelabel:config.readOnly,
//            hidden:config.readOnly
            editor: this.exchangeratefortransaction=new Wtf.form.NumberField({
                name:'desc',
                decimalPrecision:10,
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
            header:WtfGlobal.getLocaleText("acc.mp.amtDue"),  //"Amount Due",
            dataIndex:'amountdue',
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            hidelabel:config.readOnly,
            width: 150,
            hidden:config.readOnly
        })
//        ,{
//            header:"Custom Field",
//            width:100,
//            hidden:false ,
//            dataIndex:'customfield',
//            name:'CustomField',
//            renderer:function(){
//                return "<img id='AddCust' class='AddCust'  style='height:18px; width:18px;' src='images/custom-fields.png' title='Custom Field '></img>";
//            }
//        },
        columnArr = WtfGlobal.appendCustomColumn(columnArr,GlobalColumnModel[config.moduleid],undefined,undefined,config.readOnly);
        columnArr.push({
            header:(config.isNote?WtfGlobal.getLocaleText("acc.invoice.discount"):'<b>'+WtfGlobal.getLocaleText("acc.mp.entPay")+'</b>'),
            dataIndex:'payment',
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            hidelabel:(config.isNote ||config.readOnly),
            hidden:(config.isNote ||config.readOnly || this.isTemplate),
            width: 150,
            editor: this.payment=new Wtf.form.NumberField({
                name:'desc',
                decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
            })
    });
    this.cm=new Wtf.grid.ColumnModel(columnArr);
   
    this.summary = new Wtf.ux.grid.GridSummary();
    Wtf.apply(this,{
        store:store,
        stripeRows :true,
        plugins:[this.checkColumn],
        cm:this.cm           
    });
    Wtf.account.OSDetailGrid.superclass.constructor.call(this,config);
    var colModelArray = GlobalColumnModel[this.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray, this.store);
    this.on('beforeedit',this.setRowAmount,this);
    this.on('validateedit',this.checkRowAmount,this);
    this.on('afteredit',this.fireAmountChange,this);
    this.on('afteredit',this.addNewRow,this);
//    this.on('validateedit',this.checkDuplicateAccount,this);
    this.on('rowclick',this.handleRowClick,this);
    this.store.on('load',this.loadPaymentAmount,this);
    this.addEvents({
        'datachanged':true
    });
}

Wtf.extend(Wtf.account.OSDetailGrid,Wtf.grid.EditorGridPanel,{ 
    clicksToEdit:1,
    disabledClass:"newtripcmbss",
    drCurrencySymbol:function(val,m,rec){
        val=WtfGlobal.withoutRateCurrencySymbol(val,m,rec)
        return val
    },
    
//    getDebitCreditNote:function(isReceipt){
//        this.noteRec = Wtf.data.Record.create([
//            {name:'noteid'},
//            {name:'notecode'},
//            {name:'amount'},
//            {name:'amountdue'}
////            {name:'level',type:'int'}
//        ]);
//        this.noteStore = new Wtf.data.Store({
//            url: isReceipt ? "ACCDebitNote/getDebitNoteMerged.do" : "ACCCreditNote/getCreditNoteMerged.do",
////            url: Wtf.req.account+'CompanyManager.jsp',
//            baseParams:{
//                mode:2,
//                nondeleted:true
//            },
//            reader: new Wtf.data.KwlJsonReader({
//                root: "data"
//            },this.noteRec)
//        });
////        this.accountStore.load();
//    },
//  
     setTaxAmount:function(v,m,rec){
        var taxamount= v;//Set user inputed amount 
        if(rec.data.prtaxid==null || rec.data.prtaxid == undefined || rec.data.prtaxid == ""){
            taxamount = 0;
        }else if(this.isEdit && rec.data.prtaxid != undefined && rec.data.prtaxid != "" ){
            taxamount=rec.data.taxamount; //in Edit Case tax value set
        }
        taxamount = WtfGlobal.conventInDecimalWithoutSymbol(taxamount);
        rec.set("taxamount",taxamount);
        return WtfGlobal.withoutRateCurrencySymbol(taxamount,v,rec);
    },
    addTax:function(){
         this.stopEditing();
         var p= callTax("taxwin");
         Wtf.getCmp("taxwin").on('update', function(){this.taxStore.reload();}, this);
    }, 
    calTaxAmount:function(rec){
        
        var val=rec.data.dramount;
        var taxpercent=0;
            var index=this.taxStore.find('prtaxid',rec.data.prtaxid);
            if(index>=0){
               var taxrec=this.taxStore.getAt(index);
                taxpercent=taxrec.data.percent;
            }
           
        return (val*taxpercent/100);

    },
    calOnlyTaxAmount:function(a,b,rec){
        var taxcmbCmp=this.transTax; //Wtf.getCmp("paymenttaxcmb"+this.businessPerson);
        var taxValue=0;
        if(rec!=undefined && taxcmbCmp!=undefined){
        var val=rec.data.dramount;
        var taxpercent=0;
            var index=taxcmbCmp.store.find('prtaxid',rec.data.prtaxid);
            if(index>=0){
               var taxrec=taxcmbCmp.store.getAt(index);
                taxpercent=taxrec.data.percent;
            }
             taxValue = (val*taxpercent/100);
        }   
       return  val=WtfGlobal.withoutRateCurrencySymbol(taxValue,a,rec);

    },

    loadDebitCreditNoteStore : function(objParams) {
        this.noteStore.proxy.conn.url = (objParams.isReceipt)? "ACCDebitNote/getDebitNoteMerged.do" : "ACCCreditNote/getCreditNoteMerged.do";
        this.noteStore.load({
            params: {
                 accid : objParams.accid,
                 transactiontype:this.val
            }
        });
    },
    
    calMultiDebitFunc:function(){
        this.accRec = Wtf.data.Record.create([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'},
            {name:'acccode'},
            {name:'groupname'}
//            {name:'level',type:'int'}
        ]);
        this.accountStore = new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
//            url: Wtf.req.account+'CompanyManager.jsp',
            baseParams:{
                mode:2,
                 ignorecustomers:true,  
                 ignorevendors:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
//        this.accountStore.load();
    },
    
    openCOAWindow:function(){
        this.stopEditing(); 
        callCOAWindow(false, null, "coaWin");
        Wtf.getCmp("coaWin").on("update",function(){this.accountStore.reload()},this);
    },

    addNewRow:function(obj){
        for(var i=0;i<this.store.getCount();i++){
            if(this.store.getAt(i).data.accountid==undefined||this.store.getAt(i).data.accountid.length<=0|| !(this.store.getAt(i).data.dramount))
                return;
        }
        if(this.isAdvPayment && this.store.getCount()==1)
            return;
        var newrec = new this.gridRec({
            accountid:'',
            currencysymbol:this.symbol, 
            description:'',
            isdebit:!this.isReceipt?true:false,
            dramount:0,
            curamount:0,
            prdiscount:0,
            taxpercent:0,
            taxamount:0
        });
//          newrec.beginEdit();
//        var fields=this.store.fields;
//        for(var x=0;x<fields.items.length;x++){
//           var value="";
//            if(!(fields.get(x).name == 'accountid' ||fields.get(x).name == 'isdebit' ||fields.get(x).name == 'description' ||fields.get(x).name == 'currencysymbol' ||  fields.get(x).name == 'dramount' || fields.get(x).name == 'taxamount'||fields.get(x).name=='curamount'||fields.get(x).name=='prdiscount'||fields.get(x).name=='taxpercent')){
//                newrec.set(fields.get(x).name, value);
//             }
//        }  
//         newrec.endEdit();
//        newrec.commit();
        newrec.beginEdit();
        var fields=this.store.fields;
        for(var x=0;x<fields.items.length;x++){
            var value='';
            if(fields.get(x).name.indexOf('Custom_') != -1){
                newrec.set(fields.get(x).name, value);
            }
        }      
        newrec.endEdit();
        newrec.commit();
        this.store.add(newrec);
    },

//    checkDuplicateAccount:function(obj){// check for only Account selection Column only 
//        if(!this.isAdvPayment && this.store.find("accountid",obj.value)>=0 && obj.ckeckProduct==undefined && obj.field =="accountid"){
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.37")], 2);
//            obj.cancel=true;
//        }
//    },

     reconfigureGrid:function(isMultiDebit, rec){
       this.isMultiDebit=isMultiDebit
           if(isMultiDebit){
            if(this.isAdvPayment) {
                this.mulDebitCM.config[0].hidden = true;
                this.mulDebitCM.config[1].header = WtfGlobal.getLocaleText("acc.field.ReceivedAmount");
            }
            this.reconfigure(this.getStore(),this.mulDebitCM);
            this.getView().forceFit=false;
            this.getView().refresh(); 
            if (rec != undefined && rec != null) {
                for(var i=0; i< rec.length; i++){
                    var recObj = {
                        accountid:rec[i].debitaccid,
                        currencysymbol:this.symbol,
                        description:rec[i].desc,
                        dramount:rec[i].debitamt,
                        isdebit:rec[i].isdebit,
                        prtaxid:rec[i].prtaxid,
                        curamount:rec[i].curamount,
                        prdiscount:0,
                        taxpercent:0,
                        taxamount:(rec[i].taxamount)?rec[i].taxamount:0
                    };
                    var GlobalcolumnModel=GlobalColumnModel[this.moduleid]; 
                    if(GlobalcolumnModel){
                        for(var cnt = 0;cnt<GlobalcolumnModel.length;cnt++){
                            recObj[GlobalcolumnModel[cnt].fieldname]="";//Remove Html code 
                        }
                    }
                    for (var key in rec[i]) {
                        if(key.indexOf('Custom')!=-1) { // 'Custom' prefixed already used for custom fields/ dimensions
                            recObj[key] = rec[i][key];
                        }
                    }
                    var newrec = new this.gridRec(recObj);
                    
                    this.store.add(newrec);
                    this.store.clearFilter();
                    }
                this.fireEvent('datachanged',this);
            }
            this.addNewRow();
        }
    },
    
    reconfigureCreditDebitNoteGrid:function(isMultiDebit, rec,actualReceiptType,cnwithadvanceinvoice){
       this.isMultiDebit=isMultiDebit;
//       this.winType=7;
        var noteType = "Credit Note";
        if ((this.isReceipt && (this.winType == "7"||actualReceiptType=="7")) || (!this.isReceipt && (this.winType == "8"||actualReceiptType=="8"))) {
            noteType = "Debit Note";
        }
        var emptyText=""
        if(!this.isReceipt){
           emptyText=WtfGlobal.getLocaleText("acc.field.No")+noteType+" "+WtfGlobal.getLocaleText("acc.field.ismadeagainstthisAccount");
        }else{
            emptyText=WtfGlobal.getLocaleText("acc.field.No")+noteType+" "+WtfGlobal.getLocaleText("acc.field.ismadeagainstthisAccount");
        }
        this.DebitCreditNoteCM.config[1].header=noteType;
	
       this.getView().emptyText = "<div class='grid-empty-text'>"+emptyText+"</div>";
       this.reconfigure(this.getStore(),this.DebitCreditNoteCM);
       this.getView().forceFit=false;
//        this.getView().refresh();   
//        if(isMultiDebit){
            if(this.isAdvPayment) {
                this.mulDebitCM.config[0].hidden = true;
                this.mulDebitCM.config[1].header = WtfGlobal.getLocaleText("acc.field.ReceivedAmount");
            }
            this.reconfigure(this.getStore(),this.DebitCreditNoteCM);
            this.getView().forceFit=false;
//            this.getView().refresh();                       
//            if (rec != undefined && rec != null) {
//                for(var i=0; i< rec.length; i++){
//                    var newrec = new this.gridRec({
//                        accountid:rec[i].debitaccid,
//                        currencysymbol:this.symbol,
//                        description:rec[i].desc,
//                        dramount:rec[i].debitamt,
//                        prdiscount:0,
//                        taxpercent:0
//                    });
//                    this.store.add(newrec);
//                }
//                this.fireEvent('datachanged',this);
//            }
//            this.addNewRow();
                
               if (rec != undefined && rec != null) {                              
                   Wtf.Ajax.requestEx({
                    url:"ACCVendorPayment/getVendorCnPayment.do",
                    params: {
                        bills: rec.billid,
                        noteType: noteType,
                        cnwithadvanceinvoice:cnwithadvanceinvoice!=undefined?cnwithadvanceinvoice:false
                  }
                },this,function(response){
                        if(response.data.length>0){
                            this.store.removeAll();
                        }
                    for(var i=0;i<response.data.length;i++){
                      var data=response.data[i];                        
                        var rec=new this.gridRec({
                            select:data.select!=undefined?true:false,
                            noteno:data.noteno,
                            noteid:data.noteid,
                            amount:data.amount,
                            amountdue:data.amountdue,
                            payment:data.payment,
                            advanceid:data.advanceid,
                            advanceamount:data.advanceamount,
                            currencysymbol:data.currencysymbol
                            
                        });
                    rec.beginEdit();
                    var fields=this.store.fields;
                    for(var x=0;x<fields.items.length;x++){
                        var value='';
                        if(fields.get(x).name.indexOf('Custom_') != -1){
                            rec.set(fields.get(x).name, value);
                        }
                    }      
                    rec.endEdit();
                    rec.commit();
                        if(rec!=undefined){
                            this.store.add(rec);
                            this.fireEvent('datachangedCN',this);
                        }
                    }
                   this.fireEvent('datachangedCNCheckAmount',this);
                },function(){});
        }
//         this.getView().refresh(true);   
    },

    getMultiDebitAmount:function(){       
       var amt=0;
       for(var i=0; i<this.store.getCount();i++){
            if(this.winType==7 || this.winType==8) {
                amt+=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['payment']);
            } else if(this.winType==2 && this.ismanydbcr) {
                if(!this.isReceipt) {//Make Payment and Debit Type
                    if(this.store.getAt(i).data['isdebit']) {
                        if(this.store.getAt(i).data['curamount']!=undefined) {
                            amt+=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['curamount']);
                        } else {
                            amt+=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['dramount']);
                        }
                    } else {
                        if(this.store.getAt(i).data['curamount']!=undefined) {
                            amt-=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['curamount']);
                        } else {
                            amt-=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['dramount']);
                        }
                    }
                } else { //Receive Payment and Credit Type
                    if(!this.store.getAt(i).data['isdebit']) {
                        if(this.store.getAt(i).data['curamount']!=undefined) {
                            amt+=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['curamount']);
                        } else {
                            amt+=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['dramount']);
                        }
                    } else {
                        if(this.store.getAt(i).data['curamount']!=undefined) {
                            amt-=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['curamount']);
                        } else {
                            amt-=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['dramount']);
                        }
                    }
                }
            } else if(this.winType==2){
                if(this.store.getAt(i).data['curamount']!=undefined)
                        amt+=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['curamount']);
                else
                        amt+=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['dramount']);
            } else {
                amt+=WtfGlobal.conventInDecimalWithoutSymbol(this.store.getAt(i).data['dramount']);
            }
        }
//        return WtfGlobal.conventInDecimalWithoutSymbol(amt);
        return WtfGlobal.conventInDecimalWithoutSymbol(amt);
    },

    getMultiDebitTaxAmount:function(){       
        var amt=0;
        var taxcmbCmp=this.transTax; //Wtf.getCmp("paymenttaxcmb"+this.businessPerson);
        if(this.winType==2) {
            for(var i=0; i<this.store.getCount();i++){
                var rec=this.store.getAt(i);
                if(rec!=undefined ){
                    var val=rec.data.dramount;
                    var taxamount=rec.data.taxamount;
                    if(taxamount!=undefined && taxamount!==""){
                        amt+=taxamount;
                    }else{
                        var taxpercent=0;
                        var index=taxcmbCmp.store.find('prtaxid',rec.data.prtaxid);
                        if(index>=0){
                            var taxrec=taxcmbCmp.store.getAt(index);
                            taxpercent=taxrec.data.percent;
                        }
                        amt+=WtfGlobal.conventInDecimalWithoutSymbol((val*taxpercent/100));
                    }
                    
                }                      
            }
        }
        return WtfGlobal.conventInDecimalWithoutSymbol(amt);
    },
   
    setCurrencyid:function(cuerencyid,symbol){        
        this.currencyid=cuerencyid;
        this.symbol=symbol;
        this.store.each(function(rec){
            rec.set('currencysymbol',symbol)
        },this)
    },
    createWindow:function(rowindex){
        this.tagsLineItem = new Wtf.account.CreateCustomFields({
            customcolumn:1,
            border: false,
            id:'XYZ',
            compId:this.id,
            autoHeight: true,
            autoWidth:true,
            parentcompId:this.id,
            moduleid:this.moduleid,
            isEdit: this.isEdit,
            record: this.record,
            rowindex : rowindex, // used to find out line level store index
            isWindow:true

        });
        this.CenterForm=new Wtf.form.FormPanel({
            region: 'center',
            layout: 'form',
            border: false,
            //                labelWidth: 70,
            bodyStyle: 'background:#f1f1f1;padding:15px',
            items:[this.tagsLineItem]
        });
        this.privWin = new Wtf.Window({
            title: WtfGlobal.getLocaleText("acc.field.SetCustomFieldValues"),
            resizable: false,
            width: 500,
            height: 400,
            modal: true,
            layout: 'border',
            scope: this,
            buttons: [{
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                scope: this,
                handler: function() {
                    this.SaveCustomLineItemData();
                }
            },{
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope: this,
                handler: function() {
                    this.privWin.close();
                }
            }],
            items: [this.CenterForm]
        });
        
        this.privWin.on('show', function() {
            var rec = this.store.getAt(this.tagsLineItem.rowindex);
            if(rec.customfield!="") {
                
            } else {
                if (this.tagsLineItem.loadCnt == this.tagsLineItem.count) {
                    for (var key in rec) {
                        if(key.indexOf('Custom')!=-1) { // 'Custom' prefixed already used for custom fields/ dimensions
//                            recObj[key] = rec[key];
                            Wtf.getCmp(key.replace('Custom_','')+this.tagsLineItem.id).setValue(rec[key]);
                        }
                    }
                }
            }
        }, this);
        this.privWin.show();
    },
    //    showCustomFieldWindow:function(obj,row,col,e){
    //       var event=e;
    //        var i=0;
    //        var flag=0;
    //        if(event.getTarget("img[class='AddCust']")) {
    ////          alert("at");
    //                this.createWindow();
    //        }
    //    },
    SaveCustomLineItemData:function(){
        var custFieldArr=this.tagsLineItem.createFieldValuesArray();
        var index = this.tagsLineItem.rowindex;
        //      this.tagsLineItem.fireEvent('update',this);
        if (custFieldArr.length > 0)
            this.customLineItemArray = JSON.stringify(custFieldArr);
        this.privWin.close();
//        this.store.each(function(rec){
            this.store.getAt(index).set('customfield',this.customLineItemArray)
//        },this)
    },
    loadPaymentAmount:function(a,b,c,isLifoFifo){        
        var i=0;
        if(this.store.getCount()>0 && this.amount>0){
            var rec;
            for(i=0; i<this.store.getCount();i++){
                rec=this.store.getAt(i)
                if(this.currentAmount>=rec.data['amountdue']){
                    rec.set('payment',rec.data['amountdue']);
                    this.currentAmount-=rec.data['amountdue'];
//                    if(this.currentAmount>=(rec.data['amountpaid']*1)){
//                          rec.set('payment',rec.data['amountpaid']*1);
//                          this.currentAmount-=(rec.data['amountpaid']*1);
                    if(isLifoFifo != undefined && isLifoFifo){
                          rec.data.select = true;
                          rec.commit();
                    }
                }
                else break;
            }
            if(this.currentAmount>0 && i<this.store.getCount()){
                this.store.getAt(i++).set('payment',this.currentAmount);
                if(isLifoFifo != undefined && isLifoFifo){
                          this.lifoFifoValueForCheckBox = 0;
                          rec.data.select = true;
                          rec.commit();
                }
            }
            for(var j=i; j<this.store.getCount();j++){
                this.store.getAt(j).set('payment',0);
            }
            }
        else{
            for(i=0; i<this.store.getCount();i++){
                this.store.getAt(i).set('payment',0);
            }
        }
       this.fireEvent('datachanged',this);
    }, 
    loadPaymentAmountUpdate:function(a,b,c,isLifoFifo){        
        var i=0;
        if(this.store.getCount()>0 && this.amount>0){
            var rec;
            for(i=0; i<this.store.getCount();i++){
                rec=this.store.getAt(i)
                    if(this.currentAmount>=(rec.data['amountpaid']*1)){
                          rec.set('payment',rec.data['amountpaid']*1);
                          this.currentAmount-=(rec.data['amountpaid']*1);
                    if(isLifoFifo != undefined && isLifoFifo){
                          rec.data.select = true;
                          rec.commit();
                    }
                }
                else break;
            }
            if(this.currentAmount>0 && i<this.store.getCount()){
                this.store.getAt(i++).set('payment',this.currentAmount);
                if(isLifoFifo != undefined && isLifoFifo){
                          this.lifoFifoValueForCheckBox = 0;
                          rec.data.select = true;
                          rec.commit();
                }
            }
            for(var j=i; j<this.store.getCount();j++){
                this.store.getAt(j).set('payment',0);
            }
            }
        else{
            for(i=0; i<this.store.getCount();i++){
                this.store.getAt(i).set('payment',0);
            }
        }
       this.fireEvent('datachanged',this);
    }, 
    setRowAmount:function(obj){ 
        if(!this.autopopulate){
            if(obj.field=='payment' && (this.winType !=7 || this.winType !=8)){
                if(obj.record.data['select']==false){
                    obj.cancel = true;
                }
            }
        }	
        if(obj.field=='isdebit' && !this.ismanydbcr){
            return false;
    	}
        if(obj.field=='exchangeratefortransaction' && obj.value==1){
            return false;
    	}
    	
//    	this.getCurrentAmount();											//  not used in new make payment logic
//        if(obj.field=='payment'){
//            this.currentAmount+=obj.record.data['payment'];
//            if(this.currentAmount>=(obj.record.data['amountdue'])){
//                var val=obj.record.data['amountdue'];
//                obj.record.set('payment',val);
//                this.currentAmount-=val;
//            }
//            else if(this.currentAmount>0){
//                obj.record.set('payment',this.currentAmount);
//                this.currentAmount=0;
//            }
//        }this.fireEvent('datachanged',this);
    },
    checkRowAmount:function(obj){
        if(obj.field=='payment'){
//            var checkamt=this.currentAmount+obj.originalValue;
//            if(checkamt<=0)
//                obj.cancel=true;
            if(obj.value<0)
            	obj.cancel=true;
//            if(obj.value>checkamt)
//                obj.cancel=true;
            if(obj.value>obj.record.data['amountdue']){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.38")],2);
                	obj.cancel=true;
                        return;
            }
            if (obj.grid.contraentryflag) {//Contra entry checks.
                var valueDiff=obj.value-obj.record.data['payment'];
                if (obj.grid.venCustAmount >= valueDiff) {
                     obj.grid.venCustAmount += (obj.record.data['payment'] - obj.value)
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Totalpaymentamountcannotbegreaterthanamountdueofselectedinvoiceintopgrid")],2);
                    obj.cancel = true;
                    return;
                }
            }
            
                if(this.autopopulate){
                    var valueDiff=obj.value-obj.record.data['amountdue'];
                        var payment = obj.value;
                                this.lifoAmountValue=payment;
                                if(this.sharedGrid!=undefined){
                                this.sharedGrid.lifoAmountValue=this.lifoAmountValue;
                                }
                                obj.record.set("select", true);

                        obj.record.set('payment', payment);
                        this.fireEvent('datachangedenterpaymentfield',this);
                }else{
                    if (obj.grid.onCheckLifoFifoFlage) {//LifoFifo entry checks.
                        var valueDiff=obj.value-obj.record.data['payment'];
                        if (obj.grid.lifoAmountValue >= valueDiff) {
                            obj.grid.lifoAmountValue += (obj.record.data['payment'] - obj.value)
                        } else {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.AmountgiveninEnterPaymentfieldcannotbegreaterthanamountenteredinAmount")],2);
                            obj.cancel = true;
                            return;
                        }
                }
            }
        }
    },
    
//    fireAmountChange:function(obj){
//        if(obj.field=='payment')
//            if(obj.value=="")
//                obj.record.set('payment',0);                    
//        this.fireEvent('datachanged',this); 
//    },
    
    fireAmountChange:function(obj){						//  Written for new make and recieve payment functionality         Neeraj
        if(obj.field=='payment'){
            if(obj.value==""){
                obj.record.set('payment',0);
            }
        }else if (obj.field == 'select') {
            if(this.autopopulate){
                        var valueDiff=obj.value-obj.record.data['amountdue'];
                                var amountdue = obj.record.data['amountdue'];
                                    if(obj.record.data['select']){
                                        this.lifoAmountValue+=amountdue;
                                        if(this.sharedGrid!=undefined){
                                            this.sharedGrid.lifoAmountValue=this.lifoAmountValue;
                                        }
                                        obj.record.set('payment', amountdue);
                                    }else{
                                        this.lifoAmountValue-=amountdue;
                                        if(this.sharedGrid!=undefined){
                                            this.sharedGrid.lifoAmountValue=this.lifoAmountValue;
                                        }
                                        obj.record.set('payment', 0);
                                    }
//                                     obj.record.set("select", true);

            }else{
                
            if (obj.originalValue == true) {
                var amountDue = obj.record.get('amountdue');
                if(obj.grid.contraentryflag ) {//Contra entry checks.
                    if (this.venCustAmount && this.venCustAmount != 0) {
                        if (this.venCustAmount < amountDue) {
                            if (this.venCustAmount != 0) {
                                amountDue = this.venCustAmount;
                                this.venCustAmount = 0;
                            } else if (this.venCustAmount == 0) {
                                obj.record.set("select", false);
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Totalpaymentamountcannotbegreaterthanamountdueofselectedinvoiceintopgrid")],2);
                                amountDue=0;
                            }
                        } else if (this.venCustAmount == 0) {
                            obj.record.set("select", false);
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Totalpaymentamountcannotbegreaterthanamountdueofselectedinvoiceintopgrid")],2);
                            amountDue=0;

                        } else if (this.venCustAmount > amountDue) {
                            this.venCustAmount = this.venCustAmount - amountDue;
                        } else {
                            amountDue = this.venCustAmount;
                            this.venCustAmount = 0;
                        }
                    } else if (this.venCustAmount == 0) {
                        obj.record.set("select", false);
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Totalpaymentamountcannotbegreaterthanamountdueofselectedinvoiceintopgrid")],2);
                        amountDue=0;
                    }
                }
                if(obj.grid.onCheckLifoFifoFlage||obj.grid.advanceCnDnFlag) {
                    if (this.lifoAmountValue && this.lifoAmountValue != 0) {
                        if (this.lifoAmountValue < amountDue) {
                            if (this.lifoAmountValue != 0) {
                                amountDue = this.lifoAmountValue;
                                this.lifoAmountValue = 0;
                                if(this.lifoAmountValue!=this.sharedGrid.lifoAmountValue){
                                    this.sharedGrid.lifoAmountValue=this.lifoAmountValue;
                                }
                            } else if (this.lifoAmountValue == 0) {
                                obj.record.set("select", false);
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.AmountgiveninEnterPaymentfieldcannotbegreaterthanamountenteredinAmount")],2);
                                amountDue=0;
                            }
                        } else if (this.lifoAmountValue == 0) {
                            obj.record.set("select", false);
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.AmountgiveninEnterPaymentfieldcannotbegreaterthanamountenteredinAmount")],2);
                            amountDue=0;

                        } else if (this.lifoAmountValue > amountDue) {
                            this.lifoAmountValue = this.lifoAmountValue - amountDue;
                        } else {
                            amountDue = this.lifoAmountValue;
                            this.lifoAmountValue = 0;
                        }
                        if(this.lifoAmountValue!=this.sharedGrid.lifoAmountValue){
                            this.sharedGrid.lifoAmountValue=this.lifoAmountValue;
                        }
                    } else if (this.lifoAmountValue == 0) {
                        obj.record.set("select", false);
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.AmountgiveninEnterPaymentfieldcannotbegreaterthanamountenteredinAmount")],2);
                        amountDue=0;
                    }
                }
                
                obj.record.set('payment', amountDue);
            } else if (obj.originalValue == false) {
                if(obj.grid.onCheckLifoFifoFlage){
                    this.lifoAmountValue += obj.record.get('payment');
                    if(this.lifoAmountValue!=this.sharedGrid.lifoAmountValue){
                            this.sharedGrid.lifoAmountValue=this.lifoAmountValue;
                    }
                } else if(obj.grid.contraentryflag){
                    this.venCustAmount += obj.record.get('payment');
                } else if(obj.grid.advanceCnDnFlag){
                    this.lifoAmountValue += obj.record.get('payment');
                    if(this.lifoAmountValue!=this.sharedGrid.lifoAmountValue){
                            this.sharedGrid.lifoAmountValue=this.lifoAmountValue;
                    }
                }
                obj.record.set('payment', 0);
            }
            }
        } else if(obj.field=='dramount'){
            if(obj.value==""){
                obj.record.set('dramount',0);
            }
            var val=0; 
            var taxamount=0;
            val=getRoundedAmountValue(obj.record.get("dramount"));
            if(obj.record.get("dramount")!=undefined ||(obj.record.get("dramount")!=undefined && obj.record.get("prtaxid")!='-1'))
                {
                    obj.record.set("curamount",val);
                }
            if(obj.record.get("prtaxid")!=undefined ||obj.record.get("prtaxid")!='-1'){
                  taxamount= this.calTaxAmount(obj.record);
                  var taxRoundAmount=getRoundedAmountValue(taxamount);
                val+=taxRoundAmount;
                if(val!==0 && (obj.record.get("dramount")!=undefined || obj.record.get("prtaxid")!=""))
                {
                    obj.record.set("curamount",val);
                    obj.record.set("taxamount",taxRoundAmount);
                    obj.record.set("prtaxid",obj.record.get("prtaxid"));
                }
            }
             if(obj.record.get("prtaxid")!=undefined && obj.record.get("prtaxid")=='-1'&& obj.record.get("dramount")!=undefined){
                     obj.record.set("curamount",obj.record.get("dramount"));
                     obj.record.set("taxamount",taxRoundAmount);
                }
                
        }else if (obj.field == 'prtaxid'){
               var val=0;
               var taxamount=0;
                val=getRoundedAmountValue(obj.record.get("dramount"));
                if(obj.record.get("prtaxid")!=undefined && obj.record.get("prtaxid")!='-1'){
                    taxamount= this.calTaxAmount(obj.record);
                }
                  var taxRoundAmount=getRoundedAmountValue(taxamount*1);
                  val+=taxRoundAmount;
//                val+=getRoundedAmountValue(taxamount*1);
                 val= (getRoundedAmountValue(val)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);
                if(val!==0 && (obj.record.get("dramount")!=undefined || obj.record.get("prtaxid")!="" ) && obj.record.get("prtaxid")!='-1')
                {
                    obj.record.set("curamount",val);
                    obj.record.set("taxamount",taxRoundAmount);
                    obj.record.set("prtaxid",obj.record.get("prtaxid"));
                }
                if(obj.record.get("prtaxid")!=undefined && obj.record.get("prtaxid")=='-1'&& obj.record.get("dramount")!=undefined){
                     obj.record.set("curamount",val);
                     obj.record.set("taxamount",taxRoundAmount);
                }
        }else if (obj.field == 'taxamount'){
                val=0;
                taxamount=0;
                val=getRoundedAmountValue(obj.record.get("dramount"));          
                taxRoundAmount=getRoundedAmountValue(obj.value);
                val+=taxRoundAmount;
                val= (getRoundedAmountValue(val)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL);		
                if(obj.record.get("prtaxid")!= undefined && val!==0 && (obj.record.get("dramount")!=undefined || obj.record.get("prtaxid")!="" ) && obj.record.get("prtaxid")!='-1')
                {
                    obj.record.set("curamount",val);
                    obj.record.set("taxamount",taxRoundAmount);
                }
        }else if (obj.field == 'exchangeratefortransaction'){
             var amountDueOriginal=0;
             var exchangeRate=0;
             var convertedAmount=0;
             amountDueOriginal=obj.record.get("amountDueOriginal");
             exchangeRate=obj.record.get("exchangeratefortransaction");
             if(exchangeRate!='')
                obj.record.set("amountdue",getRoundedAmountValue(amountDueOriginal*exchangeRate));
            else
                obj.record.set("amountdue",obj.record.get("amountDueOriginalSaved"));
//            if(obj.field=="exchangeratefortransaction"&&WtfGlobal.singaporecountry()&&WtfGlobal.getCurrencyID()!=Wtf.Currency.SGD){
//                    callGstCurrencyRateWin(this.id,WtfGlobal.getCurrencyName()+" ",obj,obj.record.get("gstCurrencyRate")*1);//obj.record.get("currencysymboltransaction")+" "
//            }//option to give GST rate if the transaction rate is changed 
        }
        if(this.autopopulate){
          this.fireEvent('datachangedenterpaymentfield',this);  
        }
        this.fireEvent('datachanged',this);
     },
    
    
    getAmount:function(isdue){
        var amt=0;
       for(var i=0; i<this.store.getCount();i++){
            var tempAmt=isdue?this.store.getAt(i).data['amountdue']:this.store.getAt(i).data['payment'];
            if(tempAmt==undefined)
                tempAmt=0;
            amt+=WtfGlobal.conventInDecimalWithoutSymbol(tempAmt);
        }
        return WtfGlobal.conventInDecimalWithoutSymbol(amt);
        
    },
    getAmountAmountPaid:function(isdue){
        var amt=0;
       for(var i=0; i<this.store.getCount();i++){
            var tempAmt=isdue?this.store.getAt(i).data['amountdue']:this.store.getAt(i).data['amountpaid'];
            amt+=WtfGlobal.conventInDecimalWithoutSymbol(tempAmt);
        }
        return WtfGlobal.conventInDecimalWithoutSymbol(amt);
        
    },
    getCurrentAmount:function(){
       var amt=this.getAmount(false);
        this.currentAmount=this.amount-amt;
        return this.currentAmount;
    },
    updateAmount:function(newamount,isLifoFifo){

//        if(this.isEdit){
//            for(var i=0; i<this.store.getCount();i++){
//                this.store.getAt(i).set('payment',this.store.getAt(i).get("amountpaid"));
//            }
//        } else {
        this.currentAmount=newamount;
        this.amount=newamount;
        for(var i=0; i<this.store.getCount();i++){
            this.store.getAt(i).set('payment',0);
            if(this.amount == 0 && isLifoFifo){
                this.store.getAt(i).data.select = false;
                this.store.getAt(i).commit();
            }
            }

            if(this.amount>0 || (isLifoFifo != undefined && isLifoFifo)){
                if(this.isEdit){
                    this.loadPaymentAmountUpdate(undefined,undefined,undefined,isLifoFifo);
                }else{
                    this.loadPaymentAmount(undefined,undefined,undefined,isLifoFifo);
                }
            }
//        }
    },
    
    getData:function(){
        var arr=this.getGridArray();
        return WtfGlobal.getJSONArray(this, true,arr);
    },
    getGridArray:function(){
         var arr=[];
            var len=this.store.getCount();
         for(var i=0;i<len;i++){
            if(this.winType==6 && !this.isVisible()) {//Erp-2405,2388 Patched as the issue is not reproducing and all the other times when it works payment is undefined
               this.store.getAt(i).data.payment = undefined;
            }
            if(this.store.getAt(i).data.payment==0||this.isMultiDebit&&(this.store.getAt(i).data.dramount==0||(this.store.getAt(i).data.accountid=="" && !this.isAdvPayment)))
                continue;
            else {
                var rec = this.store.getAt(i); 
                rec.data[CUSTOM_FIELD_KEY]=Wtf.decode(WtfGlobal.getCustomColumnData(rec.data, this.moduleid).substring(13));
                arr.push(i);
            }
            var desc = '';
            if(rec.data['description']!=''){
                desc = encodeURI(rec.data['description']);
            }
            rec.set("description",desc);
        }
        return arr;
    },

    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget("img[class='AddCust']")) {
//          alert("at");
                this.createWindow(rowindex);
        }
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn){
                if(btn!="yes") return;
                 Wtf.accountgridname=undefined;
                 Wtf.persongridid=undefined;
                var store=grid.getStore();
                var total=store.getCount();
                store.remove(store.getAt(rowindex));
                if(rowindex==total-1){
                    this.addNewRow();
                }
                this.fireEvent('datachanged',this);
            }, this);
        }
    },
    conversionFactorRenderer:function(value,meta,record) {
           var currencysymboltransaction=((record==undefined||record.data.currencysymboltransaction==null||record.data['currencysymboltransaction']==undefined||record.data['currencysymboltransaction']=="")?WtfGlobal.getCurrencySymbol():record.data['currencysymboltransaction']);
           var currencysymbol=((record==undefined||record.data.currencysymbol==null||record.data['currencysymbol']==undefined||record.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():record.data['currencysymbol']);
                var v=parseFloat(value);
                if(isNaN(v)) return value;
//                    v= WtfGlobal.conventInDecimal(v,currencysymboltransaction)
//                return '<div class="currency">'+v+'</div>';
            return "1 "+ currencysymboltransaction +" = " +value+" "+currencysymbol;
    }
});
