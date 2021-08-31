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
/*
 * This component used for 
 * 1.Cash Book 
 * 2.Bank Book
 */
Wtf.account.FrequentLedger=function(config){
    this.uPermType=Wtf.UPerm.fstatement;
    this.permType=Wtf.Perm.fstatement;
     this.exportPermType=(config.cash?this.permType.exportdatacashbook:this.permType.exportdatabankbook);
     this.printPermType=(config.cash?this.permType.printcashbook:this.permType.printbankbook);
        this.group=config.group;
        this.isBankBook = !config.cash;
        this.accountID=(config.cash?Wtf.account.companyAccountPref.cashaccount:'');
        this.summary = new Wtf.ux.grid.GridSummary();
        this.accRec = Wtf.data.Record.create ([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'},
            {name:'currencyid',mapping:'currencyid'},
            {name:'currencysymbol'},
            {name:'currencycode'},
            {name:'acccode'}
        ]);

        this.accStore = new Wtf.data.Store({
//            url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                 mode:2,
                 group:config.group,
                 ignoreCashAccounts:(this.isBankBook?true:null),
                 ignoreBankAccounts:(this.isBankBook?null:true),
                 ignoreGLAccounts:true,
                 ignoreGSTAccounts:true, 
                 ignorecustomers:true,  
                 ignorevendors:true,
                 nondeleted: true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });

           this.Record = new Wtf.data.Record.create([
                {name: 'd_date',type:'date'},
                {name: 'd_accountname'},
                {name: 'd_entryno'},
                {name: 'd_transactionID'},
                {name: 'd_journalentryid'},
                {name: 'd_amount'},
                {name: 'd_checkno'},
                {name: 'd_description'},
                {name: 'd_transactionDetails'},
                {name: 'c_date',type:'date'},
                {name: 'c_accountname'},
                {name: 'c_entryno'},
                {name: 'c_transactionID'},
                {name: 'c_journalentryid'},
                {name: 'c_amount'},
                {name: 'c_checkno'},
                {name: 'c_description'},
                {name: 'c_transactionDetails'},
                {name: 'c_amountAccountCurrency'},
                {name: 'd_amountAccountCurrency'},
                {name: 'c_transactionAmount'},
                {name: 'd_transactionAmount'},
                {name: 'currencysymboltransaction',mapping:'transactionCurrencySymbol'},
                {name: 'currencysymbol'},
                {name: 'c_transactionDetailsBankBook'},
                {name: 'd_transactionDetailsBankBook'},
                {name: 'balanceAmount'},
                {name: 'balanceAmountAccountCurrency'},                
                {name: 'accountname'},
                {name:'billid'},
                {name:'type'},
                {name:'payee'},
                {name:'payer'},
                {name:'c_checkdate',type:'date'},
                {name:'d_checkdate',type:'date'},
                {name:'paymentstatus'},
                {name:'clearancedate',type:'date'}
            ]);
            
        this.Store = new Wtf.data.GroupingStore({
            url:"ACCReports/getLedger.do",
            baseParams:{
                mode:61,
                withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                consolidateFlag:config.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                bankBook:this.isBankBook?true:false
            },
            sortInfo : {
                field : 'accountname',
                direction : 'ASC'
            },
            groupField : 'accountname',
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.Record)
        });

            this.rowNo=new Wtf.KWLRowNumberer();
            this.gridcm= new Wtf.grid.ColumnModel([this.rowNo,{
                header: WtfGlobal.getLocaleText("acc.bankBook.date"),  //"Date",
                dataIndex: 'c_date',
                width:150,
                pdfwidth:100,
                align:'center',
                renderer:WtfGlobal.onlyDateRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.coa.gridAccountName"),  //"Account",
                dataIndex: 'accountname',
                width:150,
                pdfwidth:100,
                hidden:true,
                renderer:this.formatAccountName
            },{
                header: WtfGlobal.getLocaleText("acc.bankBook.acc"),  //"Account",
                dataIndex: 'c_accountname',
                width:150,
                pdfwidth:100,
                renderer:this.formatAccountName
            },{
                header: WtfGlobal.getLocaleText("acc.bankBook.transNo"),  //"Trnasavction Number",
                dataIndex: 'c_transactionID',
                width:150,
                pdfwidth:100,
                renderer:WtfGlobal.linkRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.bankBook.JF"),  //"Journal Folio (J/F)",
                dataIndex: 'c_entryno',
                width:150,
                pdfwidth:100,
                renderer:WtfGlobal.linkRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.field.ChequeNumber"),
                dataIndex: 'c_checkno',
                width:150,
                align:'left',
                hidden:!this.isBankBook,
                pdfwidth:100
            },{
                header: WtfGlobal.getLocaleText("payment.date.postDate"),
                dataIndex: 'c_checkdate',
                width:150,
                align:'center',
                hidden:!this.isBankBook,
                pdfwidth:100,
                renderer:WtfGlobal.onlyDateRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.bankReconcile.clearanceDate"),
                dataIndex: 'clearancedate',
                width:150,
                align:'center',
                hidden:!this.isBankBook,
                pdfwidth:100,
                renderer:WtfGlobal.onlyDateRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.payment.payee"),
                dataIndex: 'payee',
                width:150,
                width:120,
                hidden:!this.isBankBook,
                pdfwidth:125
            },{
                header: WtfGlobal.getLocaleText("acc.masterConfig.18"),
                dataIndex: 'payer',
                width:150,
                hidden:!this.isBankBook,
                pdfwidth:125
            },{
//                header: WtfGlobal.getLocaleText("acc.mp.pmtstatus"),
                header: WtfGlobal.getLocaleText("acc.recon.pmtstatus"),    //SDP-13962 : Reconciliation Status
                dataIndex: 'paymentstatus',
                width:150,
                hidden:!this.isBankBook,
                pdfwidth:125
            },{
                header: WtfGlobal.getLocaleText("acc.bankReconcile.gridAccountdescription"),
                dataIndex: 'c_description',
                align:'center',
                width:150,
                hidden:!this.isBankBook,
                pdfwidth:100
            },{
                header: WtfGlobal.getLocaleText("acc.product.description"),
                dataIndex: 'c_transactionDetailsBankBook',
                hidden:!this.isBankBook,
                width:150,
                pdfwidth:100,
                renderer:function(val,attr,rec){               
                    if(val.length == 0){
                        val=rec.data.d_transactionDetailsBankBook;
                    }
                    return val;
                }
            },{
                header: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.DebitAmountinDocumentCurrency")+"'>"+WtfGlobal.getLocaleText("acc.field.DebitAmountinDocumentCurrency")+"</span>", // Debit Amount in Document Currency
                dataIndex: 'd_transactionAmount',
                align:'right',
                width:150,
                pdfwidth:100,
                pdfrenderer : "transactioncurrency",
                renderer:WtfGlobal.withoutRateCurrencySymbolTransaction
            },{
                header: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.CreditAmountinDocumentCurrency")+"'>"+WtfGlobal.getLocaleText("acc.field.CreditAmountinDocumentCurrency")+"</span>",  //Credit Amount in Document Currency
                dataIndex: 'c_transactionAmount',
                align:'right',
                width:150,
                pdfwidth:100,
                pdfrenderer : "transactioncurrency",
                renderer:WtfGlobal.withoutRateCurrencySymbolTransaction
            },{            
                header: WtfGlobal.getLocaleText("acc.bankBook.debitAmt") +WtfGlobal.getLocaleText("acc.fixedAssetList.grid.homCur"),  //"Debit Amount",
                dataIndex: 'd_amount',
                align:'right',
                width:150,
                pdfwidth:100,
                hidden:true,
                renderer:WtfGlobal.currencyRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.bankBook.crebitAmt") +WtfGlobal.getLocaleText("acc.fixedAssetList.grid.homCur"),  //"Credit Amount",
                dataIndex: 'c_amount',
                align:'right',
                width:150,
                pdfwidth:100,
                hidden:true,
                renderer:WtfGlobal.currencyRenderer
            },{
                header: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.DebitAmountinAccountCurrency")+"'>"+WtfGlobal.getLocaleText("acc.field.DebitAmountinAccountCurrency")+"</span>",
                dataIndex: 'd_amountAccountCurrency',
                align:'right',
                width:150,
                pdfwidth:100,
                pdfrenderer : "rowcurrency",
                renderer : WtfGlobal.withoutRateCurrencyDeletedSymbol
            },{
                header: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.CreditAmountinAccountCurrency")+"'>"+WtfGlobal.getLocaleText("acc.field.CreditAmountinAccountCurrency")+"</span>",
                dataIndex: 'c_amountAccountCurrency',
                align:'right',
                width:150,
                pdfwidth:100,
                pdfrenderer : "rowcurrency",
                renderer : WtfGlobal.withoutRateCurrencyDeletedSymbol
            },{
                header: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.BalanceAmountinAccountCurrency")+"'>"+WtfGlobal.getLocaleText("acc.field.BalanceAmountinAccountCurrency")+"</span>",
                align:'right',
                dataIndex: 'balanceAmountAccountCurrency',
                pdfrenderer : "rowcurrency",
                renderer : WtfGlobal.withoutRateCurrencyDeletedSymbol,
                width:150,
                pdfwidth:125
             },{
                header: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.BalanceAmountinbasecurrency")+"'>"+WtfGlobal.getLocaleText("acc.field.BalanceAmountinbasecurrency")+"</span>", 
                align:'right',
                dataIndex: 'balanceAmount',
                renderer:WtfGlobal.currencyRenderer,
                pdfrenderer : "rowcurrency",
                width:150,
                pdfwidth:125
             }]);
             
            this.gridView1 = new Wtf.grid.GroupingView({
                    forceFit:false,
//                    showGroupName: true,
                    enableNoGroups:false, // REQUIRED!
                  //  hideGroupedColumn: true,
                    emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }); 
            this.grid = new Wtf.grid.GridPanel({
                stripeRows :true,
                store: this.Store,
                cm: this.gridcm,
               // title:(this.group==9?"<center>"+WtfGlobal.getLocaleText("acc.bankBook.msg1")+"</center>":""),
                border : false,
                loadMask : true,
                viewConfig:this.gridView1,
                forceFit:false
            });
            this.CustomerComboconfig = {
                //hiddenName:this.businessPerson.toLowerCase(),         
                store: this.accStore,
                valueField:'accountid',
                hideLabel:true,
                //hidden : iscustomer,
                displayField:'accountname',
                emptyText:this.isBankBook?WtfGlobal.getLocaleText("acc.bankBook.sel"):WtfGlobal.getLocaleText("acc.cashBook.sel"),  //'Select Bank Account',
                mode: 'local',
                typeAhead: true,
                selectOnFocus:true,
                triggerAction:'all',
                scope:this
            };
             this.cmbAccount = new Wtf.common.Select(Wtf.applyIf({
               multiSelect:true,
               fieldLabel:WtfGlobal.getLocaleText("acc.bankBook.bankAccName"),
               forceSelection:true,         
               extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
               extraComparisionField:'acccode',// type ahead search on acccode as well.
               listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:250,
               width:240
             },this.CustomerComboconfig));
            
            
            this.accStore.on("load", function(store,accRec){
                    var storeNewRecord=new this.accRec({
                        accountname:'All',
                        accountid:'All',
                        acccode:''
                    });
                    this.cmbAccount.store.insert( 0,storeNewRecord);
                     //this.showLedger(this.accountID);
                // this.cmbAccount.setValue("All");
                    if(config.cash==true || this.isBankBook){
                        var accId;
                        if(this.accStore.getCount()>1){
                        accId=this.accStore.getAt(1).data.accountid;
                        this.showAccountsOnly(accId);
                    }
                    }
                },this); 

                this.accStore.load();
                this.cmbAccount.on('select',function(combo,accRec,index){ //multiselection in case of all 
            if(accRec.get('accountid')=='All'){  //case of multiple record after all
                        combo.clearValue();
                        combo.setValue('All');
                    }else if(combo.getValue().indexOf('All')>=0){  // case of all after record
                        combo.clearValue();
                        combo.setValue(accRec.get('accountid'));
                    }
                } , this);

            
            
            this.startDate=new Wtf.ExDateFieldQtip({
                fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
                name:'stdate',
                format:WtfGlobal.getOnlyDateFormat(),
                value:this.getDates(true)
            });
            this.endDate=new Wtf.ExDateFieldQtip({
                fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
                format:WtfGlobal.getOnlyDateFormat(),
                name:'enddate',
                value:this.getDates(false)
            });
            this.startDate.on('change',function(field,newval,oldval){
                if(field.getValue()!='' && this.endDate.getValue()!=''){
                    if(field.getValue().getTime()>this.endDate.getValue().getTime()){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                        field.setValue(oldval);                    
                    }
                }
            },this);
        
            this.endDate.on('change',function(field,newval,oldval){
                if(field.getValue()!='' && this.startDate.getValue()!=''){
                    if(field.getValue().getTime()<this.startDate.getValue().getTime()){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
                        field.setValue(oldval);
                    }
                }
            },this);
    var tabTitle = config.title;
    var btnArr = [];
    var bottombtnArr=[];
    if(config.cash==false){
        btnArr.push(WtfGlobal.getLocaleText("acc.bankBook.bankAccName"));
    }else{
        btnArr.push(WtfGlobal.getLocaleText("acc.cashBook.cashAccName"));
    }
    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'int'}, 'name'],
        data :[[0,WtfGlobal.getLocaleText("acc.rem.105")],
               [1,WtfGlobal.getLocaleText("acc.invoiceList.mP")],
               [2,WtfGlobal.getLocaleText("acc.invoiceList.recPay")]]
    });
    this.typeEditor = new Wtf.form.ComboBox({
        store: this.typeStore,
        name:'typeid',
        displayField:'name',
        id: this.typeEditorComboId, //+config.id,
        valueField:'typeid',
        mode: 'local',
        width:200,
        value:0,
        listWidth:200,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    
        btnArr.push(this.cmbAccount,'-');
    btnArr.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),this.endDate,'-',{
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
        tooltip:config.cash?WtfGlobal.getLocaleText("acc.bankBook.fetchTT"):WtfGlobal.getLocaleText("acc.bankBook.fetchTT1"),
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.onClick
    });
    if(this.isBankBook){
        btnArr.push("->");
        btnArr.push(WtfGlobal.getLocaleText("acc.common.view"), this.typeEditor);
    }
//    if(config.cash==true){
    if(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType)){
      bottombtnArr.push(this.expButton=new Wtf.exportButton({
        obj:this,
        id:"exportReports"+config.helpmodeid,
        text:WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
        disabled :true,
        filename:config.cash?WtfGlobal.getLocaleText("acc.bankBook.tabTitle1"): WtfGlobal.getLocaleText("acc.bankBook.tabTitle"),
//        params:{
//                isBankBook:this.isBankBook,
//                viewFlag:this.typeEditor.getValue()
//        },
        menuItem:{csv:true,pdf:false,rowPdf:false,xls:true,subMenu:true},
        get:115
      }));
       this.expButton.on("click", function() {
        this.expButton.setParams({
            isBankBook:this.isBankBook,
            viewFlag:this.typeEditor.getValue()
        });
    },this);
    }

    if(!WtfGlobal.EnableDisable(this.uPermType, this.printPermType)){
      bottombtnArr.push("-",this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.bankBook.print"), //"Print",
        obj:this,
        tooltip :WtfGlobal.getLocaleText("acc.bankBook.printTT"), //'Print report details',
        disabled :true,
        filename:config.cash?WtfGlobal.getLocaleText("acc.bankBook.tabTitle1"): WtfGlobal.getLocaleText("acc.bankBook.tabTitle"),
        params:{stdate:WtfGlobal.convertToGenericDate(this.getDates(true)),
                 enddate:WtfGlobal.convertToGenericDate(this.getDates(false)),
                 accountid:this.accountID||config.accountID,
                 name: tabTitle
        },
        label: tabTitle,
        menuItem:{print:true},
        get:115
      }));
    }
    btnArr.push("->");
    btnArr.push(getHelpButton(this,config.helpmodeid));
//    }
    Wtf.apply(this,{
        items:[{
            layout:'border',
            border:false,
            scope:this,
            items:[{
                region:'center',
                layout:'fit',
                border:false,
                items:this.grid,
                tbar:btnArr,
                bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.Store,
                    searchField: this.cmbAccount.getValue(),
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id}),
                    items: bottombtnArr
                })
            }]
        }]

    },config)

    Wtf.account.FrequentLedger.superclass.constructor.call(this,config);
    this.addEvents({
       'journalentry':true
    });
    this.getMyConfig();
    this.grid.on('cellclick',this.onCellClick, this);
    this.grid.on('render', function() {
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    }, this);
    this.typeEditor.on('select',this.fetchLedger,this);
    this.Store.on('beforeload',function(s){
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        s.paramNames.start = 0;
        s.baseParams.stdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        s.baseParams.enddate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        s.baseParams.accountid=this.cmbAccount.getValue();
        s.baseParams.isBankBook=this.isBankBook;
        s.baseParams.ignoreCashAccounts=(this.isBankBook?true:null);
        s.baseParams.ignoreBankAccounts=(this.isBankBook?null:true);
        s.baseParams.ignoreGLAccounts=true;
        s.baseParams.viewFlag=this.typeEditor.getValue(),
        s.baseParams.ignoreGSTAccounts=true;       
        if(this.pP.combo!=undefined){
            if(this.pP.combo.value=="All"){
                var count = this.Store.getTotalCount();
                var rem = count % 5;
                if(rem == 0){
                    count = count;
                }else{
                    count = count + (5 - rem);
                }
                s.paramNames.limit = count;
            }
        }
    },this);
    this.Store.on('load',this.callOnLoad,this);
    if(SATSCOMPANY_ID==companyid){
        this.Store.on('load',function(){
            WtfGlobal.setAjaxTimeOut();
        },this);
    }
}

Wtf.extend( Wtf.account.FrequentLedger,Wtf.Panel,{
    formatAccountName:function(val,m,rec){
         if(val=="Total"){return "<b>"+val+"</b>";}
         else{return val}
    },

    onClick:function(){
        this.accountID=this.cmbAccount.getValue();
//        if(this.cash==false){
//        }
        this.fetchLedger();
        if(this.isBankBook) {
            for(var i=0;i<this.accStore.getCount();i++){
                if(this.accStore.getAt(i).data.accountid == this.accountID) {
                    if(this.accStore.getAt(i).data.currencyid == Wtf.pref.Currencyid) {
                        this.gridcm.setHidden(this.gridcm.getIndexById("d_amountAccountCurrency"), true);
                        this.gridcm.setHidden(this.gridcm.getIndexById("c_amountAccountCurrency"), true);
                    } else {
                        var currencycode = this.accStore.getAt(i).data['currencycode'];
                        var indexDebit = this.gridcm.getIndexById("d_amountAccountCurrency");
                        var indexCredit = this.gridcm.getIndexById("c_amountAccountCurrency");
                    
                        var debitHeader = this.gridcm.getColumnHeader(indexDebit);
                        var creditHeader = this.gridcm.getColumnHeader(indexCredit);
                    
                        this.gridcm.setColumnHeader(indexDebit, debitHeader + " ("+currencycode+")");
                        this.gridcm.setColumnHeader(indexCredit, creditHeader + " ("+currencycode+")");
                    
                        this.gridcm.setHidden(indexDebit, false);
                        this.gridcm.setHidden(indexCredit, false);
                    //                        this.gridcm.setHidden(this.gridcm.getIndexById("d_amountAccountCurrency"), false);
                    //                        this.gridcm.setHidden(this.gridcm.getIndexById("c_amountAccountCurrency"), false);
                    }
                }
            }
        }
    },

    fetchLedger:function(){
        var sDate=this.startDate.getValue();
        var eDate=this.endDate.getValue();
        if(sDate>eDate){
            WtfComMsgBox(1,2);
            return;
        }
        if(this.accountID&&this.accountID.length>0){
            var newFilterParams = {
                isBankBook:this.isBankBook,
                accountid:this.accountID,
                stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),  //Bank Book Report (UI)
                ignoreCashAccounts:(this.isBankBook?true:null),
                ignoreBankAccounts:(this.isBankBook?null:true),
                ignoreGLAccounts:true,
                ignoreGSTAccounts:true,
                start:0,
                limit:(this.pP.combo!=undefined) ? this.pP.combo.value:30
            };
            
            this.Store.load({
                params: newFilterParams
            });
//            if(this.cash==true)
            var ignoreCashAccounts="",ignoreBankAccounts="";
            if(this.isBankBook){
                ignoreCashAccounts=true;
            } else {
                ignoreBankAccounts=true;
            }
            var newFilterParamsForExport = {
                accountid:this.accountID,
                stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),  //Export Bank Book Report 
                ignoreCashAccounts:ignoreCashAccounts,
                ignoreBankAccounts:ignoreBankAccounts,
                isBankBook:this.isBankBook,
                ignoreGLAccounts:true,
                ignoreGSTAccounts:true
            };
            if(this.expButton) {
                this.expButton.setParams(newFilterParamsForExport);
            }
            if(this.printButton) {
                this.printButton.setParams(newFilterParamsForExport);
            }
        }
    },

    callOnLoad:function(){
        WtfGlobal.resetAjaxTimeOut();
        if(this.Store.getCount()==0){
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
            if(this.expButton)this.expButton.disable();
            if(this.printButton)this.printButton.disable();
        }else{
            if(this.expButton)this.expButton.enable();
            if(this.printButton)this.printButton.enable();
        }
//        if(this.group==9){
//            var i=this.accStore.find("accountid",this.accountID);
//            if(i>=0)
//              this.grid.setTitle(WtfGlobal.getLocaleText("acc.bankBook.bankBookOf")+" "+this.accStore.getAt(i).data["accountname"]);
//        }
        this.Store.each(function(record){
            if(record.data['d_accountname']!=''){
                record.set('c_date', record.data['d_date']);
                record.set('c_checkdate', record.data['d_checkdate']);
                record.set('c_accountname',record.data['d_accountname']);
                record.set('c_entryno', record.data['d_entryno']);
                record.set('c_transactionID', record.data['d_transactionID']);
                record.set('c_journalentryid', record.data['d_journalentryid']);
                record.set('c_transactionDetails', record.data['d_transactionDetails']);
                record.set('c_checkno', record.data['d_checkno']);
                record.set('c_description', record.data['d_description']);
                record.set('c_transactionDetailsBankBook', record.data['d_transactionDetailsBankBook']);
            }
        }, this);
        this.Store.commitChanges();
    },

    showLedger:function(accid){
        var i=this.accStore.find("accountid",accid);
        if(i>=0){
            this.cmbAccount.setValue(accid);
            this.accountID=accid;
            this.fetchLedger();
        }
    },
    showAccountsOnly:function(accid){
       
            this.cmbAccount.setValue(accid);
            this.accountID=accid;
            this.fetchLedger();
        
    },
    onRender:function(config){
         Wtf.account.FrequentLedger.superclass.onRender.call(this,config);
         this.accountID="";
         //this.fetchLedger();
    },

    
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var dataindex=g.getColumnModel().getDataIndex(j);
        var formrec = this.Store.getAt(i);
        var type=formrec.data['type'];
        if((dataindex == "c_transactionID" ||dataindex == "c_transactionID") && type!=undefined && type!=""){    //type will be undefined or empty in JE         
            var withoutinventory=formrec.data['withoutinventory'];  
            var billid=formrec.data['billid'];
            viewTransactionTemplate1(type, formrec,withoutinventory,billid);            
        }else{
            var el=e.getTarget("a");
            if(el==null)return;
            var rec=this.Store.getAt(i);
            var jid=rec.data['c_journalentryid'];
            this.fireEvent('journalentry',jid,true, this.consolidateFlag);
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

    accountCurrencyRenderer:function(val,m,rec){
        if (val!="")
            return WtfGlobal.withoutRateCurrencyDeletedSymbol(Math.abs(val),m,rec);
        else
            return "";
    }
    ,
    getMyConfig:function (){
        WtfGlobal.getGridConfig (this.grid, this.moduleid, false, false);
        
        var statusForCrossLinkage = this.grid.getColumnModel().findColumnIndex("statusforcrosslinkage");
        if (statusForCrossLinkage != -1) {
            this.grid.getColumnModel().setHidden(statusForCrossLinkage, true);
        }
        
    },
    saveMyStateHandler: function (grid,state){
        WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid, grid.gridConfigId, false);
    }

});
