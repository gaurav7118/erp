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
Wtf.account.ReconciliationDetails=function(config){
    this.total=[0,0];
    this.openingbalance=[0];
    this.clearingBalance=0;
    this.currencyData = [];
    this.currencyCount = 0;
    this.summaryL = new Wtf.ux.grid.GridSummary();
    this.lsm=new Wtf.grid.CheckboxSelectionModel();
    this.rsm=new Wtf.grid.CheckboxSelectionModel();
    this.summaryR = new Wtf.ux.grid.GridSummary();
    this.reconRec=config.reconRec;
    this.isConcileReport=config.isConcileReport;
    this.accountID=config.accountID;
    this.startdateParam=config.reconRec.startdate;  
    this.enddateParam=config.reconRec.statementdate;
    this.docID="";
    this.bankBookBalanceinAcc = 0;  //Balnce as per Bank Book in Account Currency.
    this.bankStmtBalanceinAcc = 0;      //Balnce as per Bank Statement in Account Currency.
    this.accRec = Wtf.data.Record.create ([
        {name:'accountname',mapping:'accname'},
        {name:'accountid',mapping:'accid'}
    ]);
    
    this.BANK_RECONCILIATION_GRID_TYPES={
        LGRID:1,
        RGRID:2
    };

    
    Wtf.reconcillationAccountCurrencySymbol = WtfGlobal.getCurrencySymbol();
    this.lastReconciledAmount=(this.reconRec.lastReconciledAmount!=null&& this.reconRec.lastReconciledAmount!=undefined)?this.reconRec.lastReconciledAmount:"-";//last reconciled amount
    this.reconcileAmount=(this.reconRec.reconcileAmount!=null&& this.reconRec.reconcileAmount!=undefined && this.reconRec.reconcileAmount!="")?this.reconRec.reconcileAmount:0;//reconcilation amount
    this.lastReconciledDate=(this.reconRec.lastReconciledDate!=null&& this.reconRec.lastReconciledDate!=undefined)?this.reconRec.lastReconciledDate:"-" ; //last reconciled date
    
    this.accStore = new Wtf.data.Store({
        url : "ACCAccountCMN/getAccountsForCombo.do",
        baseParams:{
          nondeleted:true,
          mode:2,
          ignoreGLAccounts:true,
          ignoreCashAccounts:true,
          ignoreGSTAccounts:true,  
          ignorecustomers:true,  
          ignorevendors:true
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.accRec)
    });

   this.LedgerRec = new Wtf.data.Record.create([
        {name: 'id'},
        {name: 'd_date',type:'date'},
        {name: 'd_accountname'},
        {name: 'd_entryno'},
        {name: 'd_journalentryid'},
        {name: 'd_amount'},
        {name: 'd_amountinacc'},
        {name: 'd_amountintransactioncurrency'},
        {name: 'd_reconciledate',type:'date'},
        {name: 'c_date',type:'date'},
        {name: 'c_accountname'},
        {name: 'c_entryno'},
        {name: 'c_journalentryid'},
        {name: 'c_amountintransactioncurrency'},
        {name: 'c_amount'},
        {name: 'c_amountinacc'},
        {name: 'c_reconciledate',type:'date'},
        {name: 'lastreconciledate',type:'date'},
        {name:'type'},
        {name:'billid'},
        {name:'moduleid'},
        {name:'isOpeningTransaction', type:'boolean'},
        {name:'transactionID'},
        {name:'chequeno'},
        {name: 'chequedate',type:'date'},
        {name: 'jeDate',type:'date'},
        {name:'description'},
        {name:'withoutinventory'},
        {name:'paidto'},
        {name:'currencysymbol'},
        {name:'accountcurrencysymbol'}
    
    ]);
    
    this.gridBbar=[];
    this.rgridBbar=[];
    
    this.debitTextValue_acc = new Wtf.Toolbar.TextItem(""); // debit value
    this.gridBbar.push('->',"<B>" +WtfGlobal.getLocaleText("acc.field.DebitAmountinAccountCurrency")+ " : </B>",this.debitTextValue_acc);
    this.debitTextValue_acc.getEl().innerHTML ="<B>" + WtfGlobal.conventInDecimal(0,WtfGlobal.getCurrencySymbol()) +"</B>"; // Setting Default value as 0
    this.gridBbar.push("-");
    this.debitTextValue = new Wtf.Toolbar.TextItem(""); // credit value
    this.gridBbar.push('->',"<B>" +WtfGlobal.getLocaleText("acc.field.DebitAmountinBaseCurrency")+ " : </B>",this.debitTextValue);
    this.debitTextValue.getEl().innerHTML ="<B>" + WtfGlobal.conventInDecimal(0,WtfGlobal.getCurrencySymbol()) +"</B>"; // Setting Default value as 0
    
    this.creditTextValue_acc = new Wtf.Toolbar.TextItem(""); // debit value
    this.rgridBbar.push('->',"<B>" +WtfGlobal.getLocaleText("acc.field.CreditAmountinAccountCurrency")+ " : </B>",this.creditTextValue_acc);
    this.creditTextValue_acc.getEl().innerHTML ="<B>" + WtfGlobal.conventInDecimal(0,WtfGlobal.getCurrencySymbol()) +"</B>"; // Setting Default value as 0
    this.rgridBbar.push("-");
    this.creditTextValue = new Wtf.Toolbar.TextItem(""); // credit value
    this.rgridBbar.push('->',"<B>" +WtfGlobal.getLocaleText("acc.field.CreditAmountinBaseCurrency")+ " : </B>",this.creditTextValue);
    this.creditTextValue.getEl().innerHTML ="<B>" + WtfGlobal.conventInDecimal(0,WtfGlobal.getCurrencySymbol()) +"</B>"; // Setting Default value as 0

   this.LStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "left"
        },this.LedgerRec),
        url:"ACCReports/getReconciliationData.do",
        baseParams:{
            mode:67
        }
    });
    this.exportRecord =new this.accRec;  
    this.LStore.on('beforeload', function(){
          var ss=this.quickPanelSearch1.getValue();
          ss=(ss==WtfGlobal.getLocaleText("acc.bankReconcile.SearchwithJENumberAndName")||ss==WtfGlobal.getLocaleText("acc.bankReconcile.SearchwithJENumber"))?"":ss;
          this.LStore.baseParams = {
                 isConcileReport:this.isConcileReport,
                 accountid:this.accountID,
                 ss:ss,
                 stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                 enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
        };
        
    }, this);

    this.RStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "right"
        },this.LedgerRec),
        url:"ACCReports/getReconciliationData.do",
        baseParams:{
            mode:67
        }
    });
    
    this.RStore.on('beforeload', function(){
        var ss=this.quickPanelSearch1.getValue();
        ss=(ss==WtfGlobal.getLocaleText("acc.bankReconcile.SearchwithJENumberAndName")||ss==WtfGlobal.getLocaleText("acc.bankReconcile.SearchwithJENumber"))?"":ss;
        this.RStore.baseParams = {
            isConcileReport:this.isConcileReport,
            accountid:this.accountID,
            ss:ss,
            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
        };
    }, this);
    
    this.printButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.agedPay.printTT"),  //'Print report details',
        disabled :true,
        hidden:true,
        filename:this.isConcileReport?WtfGlobal.getLocaleText("acc.bankReconcile.viewPrevious"):WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation"), 
        lable:this.isConcileReport?WtfGlobal.getLocaleText("acc.bankReconcile.viewPrevious"):WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation"), 
        menuItem:{
            print:true
        },
        get:Wtf.autoNum.BankReconcilation
    });
    //Print Record(s) button
    this.singleRowPrint=new Wtf.exportButton({
        obj:this,
        id:"printSingleRecord"+config.id,
        iconCls: 'pwnd printButtonIcon',
        text: WtfGlobal.getLocaleText("acc.rem.236"),
        tooltip :WtfGlobal.getLocaleText("acc.rem.236.single"),  //'Print Single Record Details',
        disabled :true,
        hidden:this.isSalesCommissionStmt||this.moduleid == Wtf.Acc_Security_Gate_Entry_ModuleId,
        menuItem:{rowPrint:(this.isSalesCommissionStmt)?false:true},
        get:Wtf.Bank_Reconciliation_ModuleId,
        moduleid:Wtf.Bank_Reconciliation_ModuleId
    });
    this.exportButton=new Wtf.exportButton({
        obj:this,
        isEntrylevel:false,
        id:"exportReports"+ this.id,   //+config.id,   //added this.id to avoid dislocation of Export button option i.e. CSV & PDF
        iconCls: 'pwnd exportcsv',
        text: WtfGlobal.getLocaleText("acc.common.exportToxls"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        disabled :true,
        filename:this.isConcileReport?WtfGlobal.getLocaleText("acc.bankReconcile.viewPrevious"):WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation"), 
        menuItem:{
            xls:true,
            csv:false
        },
        get:Wtf.autoNum.BankReconcilation
    });
    /*Setting params for import button*/
    //ERP-41871
    this.extraConfig = {};
    this.extraConfig.url= "ACCReconciliation/importBankReconciliation.do";
    this.extraConfig.urlForBank= "ACCOtherReports/importBankReconciliationFromBank.do";
    this.extraConfig.accountid = this.accountID;
    this.extraConfig.startdate = this.startdateParam;
    this.extraConfig.enddate = this.enddateParam;
    this.extraConfig.openingBalance = this.openingbalance[0];
    var extraParams = "";
    this.importBtnArray= Wtf.importMenuArray(this, Wtf.Bank_Reconciliation, this.LStore, extraParams, this.extraConfig);
    this.importButton= Wtf.importMenuButtonA(this.importBtnArray, this, "Bank Reconciliation");
         
    this.rowNo=new Wtf.grid.RowNumberer();
    this.rgridcm= new Wtf.grid.ColumnModel([this.lsm,{
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridDate"),  //"Date",
        dataIndex: 'c_date',
        align:'center',
        width:70,
        pdfwidth:125,
        sortable:true,
        renderer:WtfGlobal.onlyDateRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridAccountPayee"),  //"Account Receivable",
        dataIndex: 'c_accountname',
        renderer:this.formatAccountName,
        sortable:true,
        pdfwidth:125,
        width:150
        //        summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.common.total")+'</div>';}  //Total
    },{
        header: WtfGlobal.getLocaleText("acc.bankReconcile.reconciledate"),  //"Reconcile Date",
        dataIndex: 'c_reconciledate',
        align:'center',
        width:60,
        pdfwidth:125,
        hidden:this.isConcileReport?false:true,
        sortable:true,
        renderer:WtfGlobal.onlyDateRenderer
    }, {    
       
        header: WtfGlobal.getLocaleText("acc.mp.paidTo"),  //"Memo",
        dataIndex:'paidto',
        sortable:true,
        width:60,
        pdfwidth:125
    }
    ,{    
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridAccountchequeno"),  //"Cheque No",
        dataIndex: 'chequeno',
        sortable:true,
        pdfwidth:125,
        width:100
    },{
        header: WtfGlobal.getLocaleText("payment.date.postDate"),  //"Cheque Date",
        dataIndex: 'chequedate',
        align:'center',
        width:100,
        pdfwidth:125,
        sortable:true,
        renderer:WtfGlobal.onlyDateRenderer
    },{    
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridAccountdescription"),  //"Description",
        dataIndex: 'description',
        sortable:true,
        pdfwidth:125,
        width:80
    },{
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridJournalFolio"),  //"Journal Folio (J/F)",
        dataIndex: 'c_entryno',
        sortable:true,
        pdfwidth:125,
        width:100,
        renderer:WtfGlobal.linkRenderer
    },{
            header:WtfGlobal.getLocaleText("acc.field.TransactionID"),
            dataIndex:'transactionID',
            width:90,
            pdfwidth:50,
            sortable:true
//            ,
//            renderer:function(value,meta,rec){
//
//                meta.attr = "Wtf:qtip='" + value + "' Wtf:qtitle='Transaction ID' ";
//                if(!value) return value;
//
//                value = WtfGlobal.linkRenderer(value,meta,rec)
//
//                return value;
//            }
     },{
        header:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.CreditAmountinDocumentCurrency")+"'>"+WtfGlobal.getLocaleText("acc.field.CreditAmountinDocumentCurrency")+"</span>",
        align:'right',
        sortable:true,
        dataIndex: 'c_amountintransactioncurrency',
        renderer:WtfGlobal.currencyRendererSymbol,
        width:170,
        pdfwidth:125
    },{
        header:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.CreditAmountinAccountCurrency")+"'>"+WtfGlobal.getLocaleText("acc.field.CreditAmountinAccountCurrency")+"</span>",
        align:'right',
        dataIndex: 'c_amountinacc',
        renderer:WtfGlobal.withoutRateCurrencySymbolForAccountCurrency,
        width:170,
        sortable:true,     
        pdfwidth:125,
        summaryType:'sum',
        summaryRenderer:this.accountCurrencySummaryRenderer.createDelegate(this)
   },{
        header:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.CreditAmountinBaseCurrency")+"'>"+WtfGlobal.getLocaleText("acc.field.CreditAmountinBaseCurrency")+"</span>",
        align:'right',
        sortable:true,
        dataIndex: 'c_amount',
        renderer:WtfGlobal.currencyRenderer,
        width:170,
        summaryType:'sum',
        pdfwidth:125,
        summaryRenderer:WtfGlobal.currencySummaryRenderer
     },{
        header: WtfGlobal.getLocaleText("acc.bankReconcile.lastreconciledate"),  //"Last Reconsile Date",
        dataIndex: 'lastreconciledate',
        align:'center',
        width:100,
        pdfwidth:125,
        sortable:true,
        hidden:this.isConcileReport?false:true,
        renderer:WtfGlobal.onlyDateRenderer
    }]);

    this.lgridcm= new Wtf.grid.ColumnModel([this.rsm,{
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridDate"),  //"Date",
        dataIndex: 'd_date',
        align:'center',
        width:70,
        pdfwidth:125,
        sortable:true,
        renderer:WtfGlobal.onlyDateRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridAccountPayee"),  //"Account Payee",
        dataIndex: 'd_accountname',
        pdfwidth:125,
        renderer:this.formatAccountName,
        sortable:true,
        width:150
//        summaryRenderer:function(){return '<div class="grid-summary-common">'+ WtfGlobal.getLocaleText("acc.common.total") +'</div>';}
    },{
        header: WtfGlobal.getLocaleText("acc.bankReconcile.reconciledate"),  //"Reconcile Date",
        dataIndex: 'd_reconciledate',
        align:'center',
        width:60,
        pdfwidth:125,
        hidden:this.isConcileReport?false:true,
        sortable:true,
        renderer:WtfGlobal.onlyDateRenderer
    },{    
        header: WtfGlobal.getLocaleText("acc.masterConfig.18"),  //"Memo",
        dataIndex:'paidto',
        sortable:true,
        pdfwidth:125,
        width:60
    },
    {    
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridAccountchequeno"),  //"Cheque No",
        dataIndex: 'chequeno',
        sortable:true,
        pdfwidth:125,
        width:100
    },{
        header: WtfGlobal.getLocaleText("payment.date.postDate"),  //"Cheque Date",
        dataIndex: 'chequedate',
        align:'center',
        pdfwidth:125,
        width:100,
        sortable:true,
        renderer:WtfGlobal.onlyDateRenderer
    },{    
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridAccountdescription"),  //"Description",
        dataIndex: 'description',
        pdfwidth:125,
        sortable:true,
        width:80
    },{  
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridJournalFolio"),  //"Journal Folio (J/F)",
        dataIndex: 'd_entryno',
        width:100,
        pdfwidth:125,
        sortable:true,
        renderer:WtfGlobal.linkRenderer
    },{
            header:WtfGlobal.getLocaleText("acc.field.TransactionID"),
            dataIndex:'transactionID',
            width:90,
            pdfwidth:50,
            sortable:true
   },{
        header:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.DebitAmountinDocumentCurrency")+"'>"+WtfGlobal.getLocaleText("acc.field.DebitAmountinDocumentCurrency")+"</span>",
        align:'right',
        dataIndex: 'd_amountintransactioncurrency',
        renderer:WtfGlobal.currencyRendererSymbol,
        pdfheader:WtfGlobal.getLocaleText("acc.field.AmountinDocumentCurrencyPDF"),
        width:170,
        sortable:true,      
        pdfwidth:125
   },{
        header:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.DebitAmountinAccountCurrency")+"'>"+WtfGlobal.getLocaleText("acc.field.DebitAmountinAccountCurrency")+"</span>",
        align:'right',
        dataIndex: 'd_amountinacc',
        pdfheader:WtfGlobal.getLocaleText("acc.field.AmountinAccountCurrencyPDF"),
        width:170,
        summaryType:'sum',
        sortable:true,      
        pdfwidth:125,
        renderer : WtfGlobal.withoutRateCurrencySymbolForAccountCurrency,
        summaryRenderer:this.accountCurrencySummaryRenderer.createDelegate(this)
   },{
        header:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.DebitAmountinBaseCurrency")+"'>"+WtfGlobal.getLocaleText("acc.field.DebitAmountinBaseCurrency")+"</span>",
        align:'right',
        dataIndex: 'd_amount',
        renderer:WtfGlobal.currencyRenderer,
        pdfheader:WtfGlobal.getLocaleText("acc.field.AmountinBaseCurrencyPDF"),
        width:170,
        sortable:true,
        summaryType:'sum',
        pdfwidth:125,
        summaryRenderer:WtfGlobal.currencySummaryRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.bankReconcile.lastreconciledate"),  //"Last Reconcile Date",
        dataIndex: 'lastreconciledate',
        align:'center',
        width:100,
        hidden:this.isConcileReport?false:true,
        sortable:true,
        renderer:WtfGlobal.onlyDateRenderer
    }]);
    this.grid=this.lgrid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store: this.LStore,
        height:230,
        style:'padding:2px',
        cm: this.lgridcm,
        sm:this.lsm,
        loadMask : true,
        autoScroll:true,
        viewConfig: {
            forceFit:false,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        tbar:['<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.deposit&otherCredits")+'</b>'],  //Deposits and Other Credits</b>']
        bbar:this.gridBbar
    });
        
    this.rgrid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store: this.RStore,
        height:230,
        style:'padding:2px',
        cm: this.rgridcm,
        sm:this.rsm,
        loadMask : true,
        autoScroll:true,
//        plugins:[this.summaryR],
        viewConfig: {
            forceFit:false,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        tbar:['<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.checks&payments")+'</b>'],  //Checks and Payments</b>']
        bbar:this.rgridBbar
    });
    //should be declared after the lgrid and rgrid 
    this.getGridConfig(true);//for lgrid
    this.getGridConfig(false);//for rgird
    this.LStore.on('load', function(store) {
        if(this.reconcileButton){
            this.reconcileButton.enable();
        }
        if(this.LStore.getCount()<1) {
            this.lgrid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.lgrid.getView().refresh();
        }
        this.quickPanelSearch1.StorageChanged(store);
        this.printButton.enable();
        this.singleRowPrint.enable();
        this.exportButton.enable();
        var ss=this.quickPanelSearch1.getValue();
        ss=(ss==WtfGlobal.getLocaleText("acc.bankReconcile.SearchwithJENumberAndName")||ss==WtfGlobal.getLocaleText("acc.bankReconcile.SearchwithJENumber"))?"":ss;
        this.printButton.setParams({
            isConcileReport:this.isConcileReport,
            accountid:this.accountID,
            loadTransactionDetails:true,
            ss:ss,
            dateFilter:this.dateFilter != undefined ? this.dateFilter.getValue() : 0,
            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue())   
        });
        //set extra parametrs to button
        this.singleRowPrint.setParams({
            isConcileReport:this.isConcileReport,
            accountid:this.accountID,
            loadTransactionDetails:true,
            ss:ss,
            dateFilter:this.dateFilter != undefined ? this.dateFilter.getValue() : 0,
            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue())   
        });
        this.exportButton.setParams({
            isConcileReport:this.isConcileReport,
            accountid:this.accountID,
            loadTransactionDetails:true,
            ss:ss,
            dateFilter:this.dateFilter != undefined ? this.dateFilter.getValue() : 0,
            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue())   
        });
        this.updateDepositsAmount();
    }, this);
    this.RStore.on('load', function(store) {
        if(this.reconcileButton){
            this.reconcileButton.enable();
        }
        if(this.RStore.getCount()<1) {
            this.rgrid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.rgrid.getView().refresh();
        }
        this.updateCheckandPaymentsAmount();
        this.quickPanelSearch1.StorageChanged(store);
    }, this);
    
    this.grid.store.on({
        update: this.updateDepositsAmount,
        datachanged: this.updateDepositsAmount,
        scope: this
    });
    this.rgrid.store.on({
        update: this.updateCheckandPaymentsAmount,
        datachanged: this.updateCheckandPaymentsAmount,
        scope: this
    });

    this.cmbAccount=new Wtf.form.ComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.account"),  //'Bank Name',
        name:'accountid',
        store:this.accStore,
        valueField:'accountid',
        displayField:'accountname',
        mode: 'local',
        width:150,
        hiddenName:'accountid',
        emptyText:'Select Bank',
        allowBlank:true,
        forceSelection:true,
        triggerAction:'all'
    });   
    this.startDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
        value:(this.reconRec==""?'':this.reconRec.startdate)
    });
    this.endDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        value:(this.reconRec==""?this.getDates(false):this.reconRec.statementdate)
    });
    
    this.reconcileAmountField=new Wtf.form.NumberField({
        allowBlank: true,
        name: 'reconcileAmountFieldValue',
        allowNegative: false,
        defaultValue:0,
        value:this.reconcileAmount,
        decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
        scope:this,
        hidden:this.isConcileReport,
        listeners : {
            scope: this,
            change: this.setNewStatementBalance
        }
    });
    
    this.dateFilterStore = new Wtf.data.SimpleStore({
        fields: ['id', 'name'],
        data :[[0,WtfGlobal.getLocaleText("acc.bankReconcile.gridDate")],[1,WtfGlobal.getLocaleText("acc.bankReconcile.reconciledate")]]
    });
    this.dateFilter = new Wtf.form.ComboBox({
        store: this.dateFilterStore,
        name:'datefilter',
        displayField:'name',
        value:0,
        hidden:!this.isConcileReport,
        anchor:"50%",
        valueField:'id',
        mode: 'local',
        triggerAction: 'all'
    });
    
    this.reconcileButton= new Wtf.Toolbar.Button({
        text: this.isConcileReport?WtfGlobal.getLocaleText("acc.bankReconcile.unconcile&save"):WtfGlobal.getLocaleText("acc.bankReconcile.reconcile&save"), //'Reconcile and Save',			
        iconCls:'pwnd save',
        scope: this,
//        style: 'margin-left: 10px',
        handler: /*this.isConcileReport?this.deleteRows.createDelegate(this):*/this.saveData.createDelegate(this)
    });
    
    this.saveAsDraftBtn= new Wtf.Button({
        text: WtfGlobal.getLocaleText("acc.common.saveasdraft"), //'Save As Draft',			
        iconCls:'pwnd save',
        scope: this,
        hidden : (this.isConcileReport || !Wtf.account.companyAccountPref.columnPref.activateBankReconcilitaionDraft),
//        style: 'margin-left: 10px',
        handler: this.saveAsDraft.createDelegate(this)
    });
    
    this.viewDraftsBtn = new Wtf.Button({
        text: WtfGlobal.getLocaleText("acc.stock.Viewdraft"), //'Save As Draft',			
        iconCls: 'accountingbase fetch',
        scope: this,
        hidden : (this.isConcileReport || !Wtf.account.companyAccountPref.columnPref.activateBankReconcilitaionDraft),
//        style: 'margin-left: 10px',
        handler: function(){
            loadReconcilationDrafts();
        }
    });

    this.singlePrint=new Wtf.exportButton({		
	 text: WtfGlobal.getLocaleText("acc.field.ExportPDF"),   //Export PDF
	 tooltip : WtfGlobal.getLocaleText("acc.field.ExportPDFFile"),
	 //scope:this,
         //hidden:this.isConcileReport,
	 id:this.isConcileReport?"viewpdf" + this.id : "exportpdf" + this.id,   //id changed to avoid dislocation of Default Template option in Export To PDF
	 iconCls: 'pwnd exportpdf1',
	 hidden:(Wtf.templateflag!=Wtf.Sanxing_templateflag), //Hide this button other than Sanxing
	// disabled : true,
         obj:this,
         exportRecord:this.exportRecord,
         menuItem:this.isConcileReport?{rowPdf:true,rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39")}:{pdf:true,detailPDF:true},
         get:Wtf.autoNum.BankReconcilation
     }); 
     
    this.exportmenuBtn = new Wtf.Action({
        iconCls:'pwnd '+'exportpdf',
        text :WtfGlobal.getLocaleText("acc.field.ExportPDF"),
        tooltip : WtfGlobal.getLocaleText("acc.field.ExportPDF.WithAndWithout.MemoPayeeDetails"),  //Export PDF with Memo and without Memo.
        scope: this,
        hidden : (Wtf.templateflag==Wtf.Sanxing_templateflag),    //Hide this button if client is Sanxing
        menu: {        
            items: [{
                id : this.id+"pdfwithmemo",
                text: WtfGlobal.getLocaleText("acc.field.ExportPDF.Memo"),  //PDF with Memo Details
		tooltip : WtfGlobal.getLocaleText("acc.field.ExportPDF.MemoPayeeDetails"),  //Export PDF with Memo and Payee Details.
                iconCls:'pwnd '+'exportpdf',                
                scope: this,
                handler:function(){
                    var isMemoPDF = true;
                    this.exportWithTemplate(isMemoPDF,this.exportRecord,Wtf.autoNum.BankReconcilation, false)
                }
            },{
                id : this.id+"pdfwithpayee",
                text: WtfGlobal.getLocaleText("acc.field.ExportPDF.withoutMemo"),     //PDF without Memo Details
                tooltip : WtfGlobal.getLocaleText("acc.field.ExportPDF.WithoutMemoPayeeDetails"),  //Export PDF without Memo and Payee Details.
                iconCls:'pwnd '+'exportpdf',                
                scope: this,
                handler:function(){
                    var isMemoPDF = false;
                    this.exportWithTemplate(isMemoPDF,this.exportRecord,Wtf.autoNum.BankReconcilation, false)
              }
            },{
                id : this.id+"asofdateunreconciledmemopdf",
                text: WtfGlobal.getLocaleText("acc.field.ExportPDF.asofdate.unreconciledpdf.withmemo"),     //As of Date Un-reconciled PDF
                tooltip : WtfGlobal.getLocaleText("acc.field.ExportPDF.asofdate.unreconciledpdfDetails.withmemo"),  //As of Date Un-reconciled Details
                iconCls:'pwnd '+'exportpdf',                
                scope: this,
                hidden : this.isConcileReport,
                handler:function(){
                    var isasofdateunreconciledPDF = true;
                    this.exportWithTemplate(true,this.exportRecord,Wtf.autoNum.BankReconcilation, isasofdateunreconciledPDF)
              }
            },{
                id : this.id+"asofdateunreconciledpdf",
                text: WtfGlobal.getLocaleText("acc.field.ExportPDF.asofdate.unreconciledpdf"),     //As of Date Un-reconciled PDF
                tooltip : WtfGlobal.getLocaleText("acc.field.ExportPDF.asofdate.unreconciledpdfDetails"),  //As of Date Un-reconciled Details
                iconCls:'pwnd '+'exportpdf',                
                scope: this,
                hidden : this.isConcileReport,
                handler:function(){
                    var isasofdateunreconciledPDF = true;
                    this.exportWithTemplate(false,this.exportRecord,Wtf.autoNum.BankReconcilation, isasofdateunreconciledPDF)
              }
            }]
        }
    });
     
    this.clearDataButton= new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.bankReconcile.clearData"),  //'Clear Data',
        scope: this,
        iconCls:'accountingbase fetch',
        handler: this.clearData.createDelegate(this)
    });
    
    this.reconciledDetailsReportButton = new Wtf.Toolbar.Button({  
        text: WtfGlobal.getLocaleText("acc.bankReconcile.snapshotbutton"), //Reconciled Details
        tooltip: WtfGlobal.getLocaleText("acc.bankReconcile.snapshotbutton"), //Reconciled Details
        iconCls: 'accountingbase fetch',
        scope: this,
        hidden:this.isConcileReport,
        handler: this.viewBankReconciliationHistoryDetails
    });
    
    this.vendorInvoice=new Wtf.form.TextField({
        fieldLabel:WtfGlobal.getLocaleText("acc.inv.invno"),  //'Vendor Invoice Number*',
        name: 'vendorinvoice',
        id:"vendorInvoiceNo"+this.heplmodeid+this.id,       
        width : 240,
        maxLength:50,
        scope:this
    }); 
    
    this.tplSummary=new Wtf.XTemplate(
        '<div class="currency-view">',
        '<table width="100%" style="margin-left: 10px;margin-top: -4px;">',
        '<tr><td><b>'+ WtfGlobal.getLocaleText("acc.bankReconcile.summaryLabel1") +'</b></td><td align=right>{bankdatainacc}</td></tr>',
        '<tr><td><b>'+ WtfGlobal.getLocaleText("acc.bankReconcile.summaryLabel2")+ '*' +'</b></td><td align=right>{presentedchequesvaluesinacc}</td></tr>',
        '<tr><td><b>'+ WtfGlobal.getLocaleText("acc.bankReconcile.summaryLabel3") + '*' +'</b></td><td align=right>{clearedchequesvaluesinacc}</td></tr>',
        '</table>',
        "</div><div style='width:100%;'><span class='gridLineReconcile'></span></div>",
        '<table width="100%" style="margin-left: 10px;margin-top: -4px;">',
        '<tr><td><b>'+ WtfGlobal.getLocaleText("acc.bankReconcile.summaryLabel4") +'</b></td><td align=right>{endingbalanceinacc}</td></tr>',
        '<tr><td><b>'+ WtfGlobal.getLocaleText("acc.bankreconcileEndingBalance") +'</b></td><td align=right>{reconcileamount}</td></tr>',
        '</table>',
        "</div><div style='width:100%;'><span class='gridLineReconcile'></span></div>",
        '<table width="100%" style="margin-left: 10px;margin-top: -4px;">',
        '<tr><td><b>'+ WtfGlobal.getLocaleText("acc.bankreconcileDifference") +'</b></td><td align=right>{reconciledifference}</td></tr>',
        '</table>',
        "</div><div style='width:100%;'><span class='gridLineReconcile'></span></div>",
        "<span class='gridfooter' style='width: 95%; margin-top: -1px; margin-left: 10px;'>"+WtfGlobal.getLocaleText("acc.bankReconcile.summaryStatement") + "</span>",
        //        '</tpl>',
        '</div>'
        );
    this.tplSummaryforbank=new Wtf.XTemplate(
        '<div class="currency-view">',       
        '<div><u><b>Summary</b></u></div>',
        '<table width="100%">',
        '<tr><td><b>'+ WtfGlobal.getLocaleText("acc.bankReconcile.bankbookLabel") +'</b> = {bankdata}</td></tr>',
        '<tr><td><b>'+ WtfGlobal.getLocaleText("acc.bankReconcile.PresentedchequeLabel") +'</b> = {presentedcheques}</td><td align=right><b>Amount</b> = {presentedchequesvalues}</td></tr>',
        '<tr><td><b>'+ WtfGlobal.getLocaleText("acc.bankReconcile.ClearedchequeLabel") +'</b> = {clearedcheques}</td><td align=right><b>Amount</b> = {clearedchequesvalues}</td></tr>',
        '</table>',
        '</tpl>',
        '</div>'
    );

   
    //opening Balance Section
    //selected transaction will be reconciled
    var htmlConfig=this.isConcileReport ? "<b>" + WtfGlobal.getLocaleText("acc.bankReconcile.unreconcileDesc") + "</b>" : "<b>" + WtfGlobal.getLocaleText("acc.bankReconcile.reconcileDesc") + "</b>";  
    this.diffSummaryTemplate=new Wtf.XTemplate(
        '<div class="currency-view">',
        '<table width="100%" style="font-size:15px;width:395px">',
         '<tr><td align=left><b>'+ WtfGlobal.getLocaleText("acc.bankReconcile.openingBalance.summaryLabel") +'</b></td><td>:</td><td align=right>{openingbankdatainacc}</td></tr>',        
        '</table>',
        '</div>'
        ); 
    var diffhtmlConfigHtml=this.diffSummaryTemplate.apply({openingbankdatainacc:this.accountCurrencyRenderer(0)});
    this.southopeningDiffPanel=new Wtf.Panel({
        border:false,
        hidden:this.isConcileReport,
        style:'margin-right:32px',
        baseCls:'tempbackgroundview',
        html:diffhtmlConfigHtml
    });
    this.openingDiffPanel= new Wtf.Panel({
        border: false,
//        style:'margin-right: 15px',
        bbar: [htmlConfig, "->",
        this.southopeningDiffPanel
        ]
    });
    
    this.reconciletplSummary=new Wtf.XTemplate(
        '<div class="currency-view">',
        '<table width="100%" style="margin-top: -4px;">',
        '<tr><td><b>'+ WtfGlobal.getLocaleText("acc.banklastreconcileAmount") +'</b></td><td>:</td><td align=right>{lastreconcileAmount}</td></tr>',
        '<tr><td><b>'+ WtfGlobal.getLocaleText("acc.banklastreconcileDate")+'</b></td><td>:</td><td align=right>{lastreconcileDate}</td></tr>',
        '</table>',
        '</div>'
        );
        
    this.reconcileCenterCalTemp=new Wtf.Panel({
        border:false,
        baseCls:'tempbackgroundview',
        html:this.reconciletplSummary.apply({
            lastreconcileAmount:this.accountCurrencyRenderer(this.lastReconciledAmount),
            lastreconcileDate:this.accountDateRenderer(this.lastReconciledDate)
        })
    });

    this.southEastCalTemp=new Wtf.Panel({
        border:false,
        baseCls:'tempbackgroundview',
        html:this.tplSummary.apply({endingbalanceinacc:this.accountCurrencyRenderer(0),presentedchequesvaluesinacc:this.accountCurrencyRenderer(0),clearedchequesvaluesinacc:this.accountCurrencyRenderer(0),bankdatainacc:this.accountCurrencyRenderer(0),reconciledifference:this.accountCurrencyRenderer(0),reconcileamount:this.accountCurrencyRenderer(0)})
    });
    this.southEastCalTempBank=new Wtf.Panel({
        border:false,
        baseCls:'tempbackgroundview',
        html:this.tplSummaryforbank.apply({presentedcheques:0,clearedcheques:0,presentedchequesvalues:WtfGlobal.currencyRenderer(0),clearedchequesvalues:WtfGlobal.currencyRenderer(0),bankdata:WtfGlobal.currencyRenderer(0)}) 
    });
    
    this.disHtml = "";
    var arr=[WtfGlobal.getLocaleText("acc.common.currencyFilterLable") , WtfGlobal.getLocaleText("acc.je.debitAmt") ,WtfGlobal.getLocaleText("acc.je.creditAmt")];
    var header = "<span class='gridHeader' style='width: 95%;'>"+WtfGlobal.getLocaleText("acc.field.DifferentCurrencyAmount")+"</span>";   
    header += "<div style='width: 100%;'>";
    header += "<span class='gridRow' style='font-weight:bold; width: 16%'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
    for(var i=0;i<arr.length;i++){
        header += "<span class='headerRow' style='width:28%' ! important;'>" + arr[i] + "</span>";
    }
    header += "</div><div style='width:100%;'><span class='gridLine'></span></div>";  
    header += "<div style='width: 100%;'>";
    for(var i=0;i<this.currencyCount;i++){
        var currency= this.currencyData[i].currencysymbol!=undefined?this.currencyData[i].currencysymbol:"";
        var transcurname = this.currencyData[i].transcurname!=undefined?this.currencyData[i].transcurname:"";
        var c_amount= this.currencyData[i].c_amount!=undefined?this.currencyData[i].c_amount:0;
        var d_amount= this.currencyData[i].d_amount!=undefined?this.currencyData[i].d_amount:0;
        header += "<span class='gridRow' style='width: 10%'>"+(i+1)+".</span>";
        header += "<span class='gridRow' style='width: 30%;' ! important;'>"+transcurname+"&nbsp;</span>";
        header += "<span class='gridRow' style='width:30%;' ! important;'>"+WtfGlobal.addCurrencySymbolOnly(d_amount,currency,[true])+"</span>";
        header += "<span class='gridRow' style='width: 30%;' ! important;'>"+WtfGlobal.addCurrencySymbolOnly(c_amount,currency,[true])+"</span>";
        header +="<br>";
    }
    header += "</div>";
    this.disHtml += "<div class='expanderContainer1' style='overflow:auto; height:110px;' >" + header + "</div>";
        
    this.DifferentCurrencyAmount=new Wtf.Panel({
        border:true,
        autoScroll: true,
        baseCls:'tempbackgroundview',
        html:this.disHtml
    });
    
    this.centerPanel=new Wtf.Panel({
        region:'south',
        height:215,
        border:false,
//        style:'padding:10px',
        style:'margin:10px 10px 10px 0px',
        layout:'column',
        items:[ {
            columnWidth:0.33,
            height:160,
            border:false,
//            style:'margin-left: 5px',
            items:[new Wtf.Panel({
                layout:'form',
                 height: 160,
                cls:'bckgroundcolor',
                bodyStyle:'padding:2px',
                style:'margin-bottom: 0px',
                hidden:this.isConcileReport?true:false,
                items:[this.DifferentCurrencyAmount]
                   
            })]
        },
        {
            columnWidth:0.33,
            style:'margin-left: 5px',
            height:160,
            border:false,
            items:[new Wtf.Panel({
                layout:'form',
                height: 160,
                cls:'bckgroundcolor',
                bodyStyle:'padding:10px',
                style:'margin-bottom: 0px',
//                labelWidth:70,
                hidden:this.isConcileReport?true:false,
                items:[{
                    border: false,
                    hidden: this.isConcileReport ? true : false,
                    html:  "<span class='gridHeader' style='width: 95%; margin-top: -1px; margin-left:0px;'>"+WtfGlobal.getLocaleText("acc.banklastreconcileDetails") + "</span>"  //<>Note: Uncleared Cheques and Deposits are calculated on Payment / Receipt selections<b>"
                },this.reconcileCenterCalTemp]   
                   
            })]
        },{
            columnWidth:0.33,
            style:'margin-left: 5px',
            height:215,
            border:false,
            items:[new Wtf.Panel({
                layout:'form',
                height:215,
                cls:'bckgroundcolor',
                bodyStyle:'padding:10px',
//                labelWidth:70,
                hidden:this.isConcileReport?true:false,
                items:[{
                    region: 'north',
                    border: false,
                    hidden: this.isConcileReport ? true : false,
                    html:  "<span class='gridHeader' style='width: 95%; margin-top: -1px; margin-left: 10px;'>"+WtfGlobal.getLocaleText("acc.bankReconcile.unclearedAndDepositsamountcalculatedonselections") + "</span>"  //<>Note: Uncleared Cheques and Deposits are calculated on Payment / Receipt selections<b>"
                },this.southEastCalTemp]   
            })]
                
        },{
            columnWidth:0.1
        }]
    });
 
    this.mainPanel = new Wtf.Panel({
        autoScroll: true,
        layout: 'border',
        border: false,
        items: [{
                region: "center",
//                autoHeight: true,
                autoScroll: true,
                border: false,
                style: 'padding:10px',
                items: [this.lgrid, this.rgrid,this.openingDiffPanel,  this.centerPanel]
            }],
        tbar: [WtfGlobal.getLocaleText("acc.field.QuickSearch"), /*this.vendorInvoice*/
            this.quickPanelSearch1 = new Wtf.KWLTagSearch({
                width: 150,
                emptyText: this.isConcileReport ? WtfGlobal.getLocaleText("acc.bankReconcile.SearchwithJENumberAndName") : WtfGlobal.getLocaleText("acc.bankReconcile.SearchwithJENumber"),
                store: this.LStore,
                id: 'quickSearch1' + this.id

            })
                    , WtfGlobal.getLocaleText("acc.bankReconcile.account"), this.cmbAccount, WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate, '-', this.isConcileReport ? WtfGlobal.getLocaleText("acc.field.On") : WtfGlobal.getLocaleText(" "), this.dateFilter, '-', 
                    (!this.isConcileReport?WtfGlobal.getLocaleText("acc.bankreconcileAmount"):""),this.reconcileAmountField,'-',
                    
                    {
                                    xtype: 'button',
                text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
                tooltip: WtfGlobal.getLocaleText("acc.bankReconcile.fetchTT"), //"Select a time period to view corresponding ledger records.",
                iconCls: 'accountingbase fetch',
                scope: this,
                handler: this.onClick
            }, {
                xtype: 'button',
                text: WtfGlobal.getLocaleText("acc.bankReconcile.viewPrevious"), //'View Previous Reconcile(s)',
                tooltip: WtfGlobal.getLocaleText("acc.bankReconcile.viewPreviousTT"), //"Previous Reconcile",
                iconCls: 'accountingbase fetch',
                scope: this,
                hidden: this.isConcileReport,
                handler: this.viewReport
            },this.viewDraftsBtn
        ],
        bbar: [/*WtfGlobal.getLocaleText("acc.bankReconcile.reconciledate"),this.reconcileDate,'|',*/this.reconcileButton,this.saveAsDraftBtn, /*this.attachDocButton,*/'-', this.clearDataButton, {
                xtype: 'button',
//                hidden:true,
                text: WtfGlobal.getLocaleText("acc.bankReconcile.history"), //View Reconciliation History
                tooltip: WtfGlobal.getLocaleText("acc.bankReconcile.history"), //View Reconciliation History
                iconCls: 'accountingbase fetch',
                scope: this,
                hidden: this.isConcileReport,
                handler: this.viewHistoryReport
            }, this.singlePrint, this.exportmenuBtn, this.printButton, this.singleRowPrint, this.exportButton, (this.isConcileReport?"":this.importButton), this.reconciledDetailsReportButton]
    });
//   Wtf.apply(this,{
//        items:[{
//            layout:'border',
//            border:false,
//            scope:this,
//            autoScroll:true,
//            items:[{
//                region:'center',
////                autoScroll:true,
//                border:false,
//                style:'padding:10px',
////                defaults:{layout:'fit',split:true, border:false},
////                layout:'column',
//                items:[{
//                    layout:'fit',
////                    columnWidth:0.5,
//                    defaults:{border:false},
//                    items:this.lgrid
//                },{
//                    layout:'fit',
//                     style:"padding-bottom:20px",
////                    columnWidth:0.5,
//                   defaults:{border:false},
//                    items:[this.rgrid,{
////                                    region:'south',
//                                        layout:'fit',
//                                        width:350,
//                                        border:false,
//                                        html:this.isConcileReport ?"<b>" + WtfGlobal.getLocaleText("acc.bankReconcile.unreconcileDesc") + "</b>":"<b>" + WtfGlobal.getLocaleText("acc.bankReconcile.reconcileDesc") + "</b>" //*Selected transactions will be reconciled<b>"
//                                }]
//                }/*,this.southEastCalTempBank*/]
//            }, this.centerPanel],
//            tbar:[WtfGlobal.getLocaleText("acc.field.QuickSearch"),/*this.vendorInvoice*/
//                this.quickPanelSearch1 = new Wtf.KWLTagSearch({
//                width: 150,
////                field: "billid",
//                emptyText:this.isConcileReport?WtfGlobal.getLocaleText("acc.bankReconcile.SearchwithJENumberAndName"):WtfGlobal.getLocaleText("acc.bankReconcile.SearchwithJENumber"),
//                store: this.LStore,
//                id:'quickSearch1'+this.id
//                
//            })
//            ,WtfGlobal.getLocaleText("acc.bankReconcile.account"),this.cmbAccount,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),this.endDate,'-',this.isConcileReport?WtfGlobal.getLocaleText("acc.field.On"):WtfGlobal.getLocaleText(" "),this.dateFilter, '-',{
//                xtype:'button',
//                text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
//                tooltip:WtfGlobal.getLocaleText("acc.bankReconcile.fetchTT"),  //"Select a time period to view corresponding ledger records.",
//                iconCls:'accountingbase fetch',
//                scope:this,
//                handler:this.onClick
//            },{
//                xtype:'button',
////                hidden:true,
//                text:WtfGlobal.getLocaleText("acc.bankReconcile.viewPrevious"),  //'View Previous Reconcile(s)',
//                tooltip:WtfGlobal.getLocaleText("acc.bankReconcile.viewPreviousTT"),  //"Previous Reconcile",
//                iconCls:'accountingbase fetch',
//                scope:this,
//                hidden:this.isConcileReport,
//                handler:this.viewReport
//        }]
//        }]
//    },config)
    Wtf.apply(this, {
        items:[this.mainPanel]},config);
    
    
    Wtf.account.ReconciliationDetails.superclass.constructor.call(this,config);
    this.addEvents({
       'journalentry':true
    });
    this.lgrid.on('cellclick',this.onLCellClick, this);
    this.rgrid.on('cellclick',this.onRCellClick, this);
    this.lgrid.getSelectionModel().on('selectionchange',this.getSelectedRowAmount,this);
    this.rgrid.getSelectionModel().on('selectionchange',this.getSelectedRowAmount,this);
        this.quickPanelSearch1.on('SearchComplete',function(store){
        this.fetchLedger();
     },this);
     //Save configuration of Grid
    this.lgrid.on('render', function(){
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.lgrid.on('statesave', this.savelGridStateHandler, this);
        }, this);
    },this);
     
    this.rgrid.on('render', function(){
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.rgrid.on('statesave', this.saveRGridStateHandler, this);
        }, this);
    },this);
    
    this.accStore.on('load',function(){this.showLedger(this.accountID);},this);
    this.accStore.load();
    this.getDraftInfoForAllAccounts();
}

Wtf.extend( Wtf.account.ReconciliationDetails,Wtf.Panel,{
    autoScroll:true,
    exportWithTemplate:function(isMemoPDF,rec,moduleid, isasofdateunreconciledPDF){
        if(moduleid == Wtf.autoNum.BankReconcilation && isasofdateunreconciledPDF && isMemoPDF){       //PDF with As of Date Unreconciled Details with Memo
            Wtf.get('downloadframe').dom.src = "ACCReports/exportBankReconciliation.do?&accountid=" + rec.accountid + "&stdate=" + rec.stdate +"&enddate="+ rec.enddate + "&isConcileReport=" + rec.isConcileReport+"&ss=" + rec.ss+"&dateFilter=" +rec.dateFilter+"&isMaintainHistory="+isasofdateunreconciledPDF+"&templateflag="+"&isMemoPDF="+isMemoPDF+"&loadTransactionDetails="+true;
        }else if(moduleid == Wtf.autoNum.BankReconcilation && isasofdateunreconciledPDF){       //PDF with As of Date Unreconciled Details without Memo
            Wtf.get('downloadframe').dom.src = "ACCReports/exportBankReconciliation.do?&accountid=" + rec.accountid + "&stdate=" + rec.stdate +"&enddate="+ rec.enddate + "&isConcileReport=" + rec.isConcileReport+"&ss=" + rec.ss+"&dateFilter=" +rec.dateFilter+"&isMaintainHistory="+isasofdateunreconciledPDF+"&templateflag="+"&isMemoPDF="+isMemoPDF+"&loadTransactionDetails="+true;
        }else if(moduleid == Wtf.autoNum.BankReconcilation && isMemoPDF){     //PDF with Memo Details
            Wtf.get('downloadframe').dom.src = "ACCReports/exportBankReconciliation.do?&accountid=" + rec.accountid + "&stdate=" + rec.stdate +"&enddate="+ rec.enddate + "&isConcileReport=" + rec.isConcileReport+"&ss=" + rec.ss+"&dateFilter=" +rec.dateFilter+"&isMaintainHistory="+false+"&templateflag="+Wtf.templateflag+"&isMemoPDF="+isMemoPDF+"&loadTransactionDetails="+true;            
        }else if(moduleid == Wtf.autoNum.BankReconcilation){        //PDF with Payee Details
            Wtf.get('downloadframe').dom.src = "ACCReports/exportBankReconciliation.do?&accountid=" + rec.accountid + "&stdate=" + rec.stdate +"&enddate="+ rec.enddate + "&isConcileReport=" + rec.isConcileReport+"&ss=" + rec.ss+"&dateFilter=" +rec.dateFilter+"&isMaintainHistory="+false+"&templateflag="+"&isMemoPDF="+isMemoPDF+"&loadTransactionDetails="+true;
        }
    },
    getDraftInfoForAllAccounts : function(){
        this.accountsDraftInfo = {};
        Wtf.Ajax.requestEx({
            url:"ACCReconciliation/getBankReconcilationDrafts.do"
        }, this,function (response) {
            if (response.success) {
                var data = response.data || [];
                for(var i=0 ; i < data.length ; i++){
                    var rec = data[i] || {};
                    this.accountsDraftInfo[rec.accountid] = rec;
                }
            } 
        });  
    },
    setNewStatementBalance : function(lgridflag){
        this.reconcileAmount=this.reconcileAmountField.getValue();   
        this.updateLessUnclearedDepAndAddUnclearedCheq();
    },
    //ERM-734
    getGridConfig : function(lgridflag){
        var moduleid="br";
        var grid="";
        if(lgridflag){//for depost grid
            moduleid=moduleid+this.BANK_RECONCILIATION_GRID_TYPES.LGRID;
            grid=this.lgrid;
        }else{//for payment grid
            moduleid=moduleid+this.BANK_RECONCILIATION_GRID_TYPES.RGRID;
            grid=this.rgrid;
        }
        WtfGlobal.getGridConfig(grid,moduleid,false,true);
    },
    savelGridStateHandler: function(grid,state){
        WtfGlobal.saveGridStateHandler(this,grid,state,"br"+this.BANK_RECONCILIATION_GRID_TYPES.LGRID,grid.gridConfigId,false);
    },

    saveRGridStateHandler: function(grid,state){
        WtfGlobal.saveGridStateHandler(this,grid,state,"br"+this.BANK_RECONCILIATION_GRID_TYPES.RGRID,grid.gridConfigId,false);
    },
    
    getSelectedRowAmount:function(){
       this.clearingBalance=0;
       this.depositeTotalBalance=0;
       this.checkTotalBalance=0;
       var larr=this.lgrid.getSelectionModel().getSelections();
       for(var i=0;i<this.lsm.getCount();i++){
           this.clearingBalance+=larr[i].data["d_amount"];
           this.depositeTotalBalance+=larr[i].data["d_amount"];
       }
       var rarr=this.rgrid.getSelectionModel().getSelections();
       for(i=0;i<this.rsm.getCount();i++){
           this.clearingBalance-=rarr[i].data["c_amount"];
           this.checkTotalBalance-=rarr[i].data["c_amount"];
       }
       this.clearingBalance+=this.openingbalance[0];
       this.updateLessUnclearedDepAndAddUnclearedCheq();
    },
    updateLessUnclearedDepAndAddUnclearedCheq:function(){
      var lselectedArr=this.lsm.getSelections();
        var clearedchequevaluesinacc = this.totalinacc[0];
        var presentedchequevaluesinacc = this.totalinacc[1];
        if (lselectedArr.length > 0) {
            for (var i = 0; i < lselectedArr.length; i++) {
                if(lselectedArr[i].data!=undefined && lselectedArr[i].data.d_amountinacc!=undefined && lselectedArr[i].data.d_amountinacc!=""){
                    clearedchequevaluesinacc=parseFloat(clearedchequevaluesinacc) + parseFloat(lselectedArr[i].data.d_amountinacc);
                }
                
                }
            }
        var rselectedArr=this.rsm.getSelections();
        if (rselectedArr.length > 0) {
            for (var i = 0; i < rselectedArr.length; i++) {
                if (rselectedArr[i].data != undefined && rselectedArr[i].data.c_amountinacc != undefined && rselectedArr[i].data.c_amountinacc != "") {
                   presentedchequevaluesinacc  = parseFloat(presentedchequevaluesinacc) - parseFloat(rselectedArr[i].data.c_amountinacc);
                }

                }
            }
//        var lefttotalinacc = this.totalinacc[0];
//        var righttotalinacc = this.totalinacc[1]
        this.bankStmtBalanceinAcc = (this.bankdatainacc + presentedchequevaluesinacc + clearedchequevaluesinacc);   //Balance As per Bank Statement
        if(this.tplSummary!=null && this.tplSummary!=undefined){
            this.tplSummary.overwrite(this.southEastCalTemp.body, {
                presentedchequesvaluesinacc: this.accountCurrencyRenderer(presentedchequevaluesinacc), //(+) Add Uncleared Checks
                clearedchequesvaluesinacc: this.accountCurrencyRenderer(clearedchequevaluesinacc), //(-) Less Uncleared Deposits
                bankdatainacc: this.accountCurrencyRenderer(this.bankdatainacc),    
                endingbalanceinacc: this.accountCurrencyRenderer(this.bankdatainacc + presentedchequevaluesinacc + clearedchequevaluesinacc),
                reconcileamount: this.accountCurrencyRenderer(this.reconcileAmount),
                reconciledifference: this.accountCurrencyRenderer(this.bankdatainacc + presentedchequevaluesinacc + clearedchequevaluesinacc-this.reconcileAmount)
            }); 
        }
        //ERM-734
        if(this.diffSummaryTemplate!=null && this.diffSummaryTemplate!=undefined){//on clearing data show only reconcile amount
            this.diffSummaryTemplate.overwrite(this.southopeningDiffPanel.body,{
                openingbankdatainacc: this.accountCurrencyRenderer(this.openingbankdatainacc)        
            }); 
        }
    },
    clearData:function(){
//        this.startDate.setValue(this.getDates(true));
        this.startDate.setValue("");
//        this.endDate.setValue(this.getDates(false));    
        this.endDate.setValue(new Date());    
        this.cmbAccount.setValue("");
        this.lgrid.getStore().removeAll();
        this.rgrid.getStore().removeAll();
        //on clearing grid selection set reconcile amount to difference
        this.calculateReconcileAmount=0;
        if(this.diffSummaryTemplate!=null && this.diffSummaryTemplate!=undefined){//on clearing data show only reconcile amount
            this.diffSummaryTemplate.overwrite(this.southopeningDiffPanel.body,{
                openingbankdatainacc:WtfGlobal.currencyRenderer(0)            
            }); 
        }
        this.tplSummary.overwrite(this.southEastCalTemp.body,{endingbalanceinacc:WtfGlobal.currencyRenderer(0),presentedchequesvaluesinacc:WtfGlobal.currencyRenderer(0),clearedchequesvaluesinacc:WtfGlobal.currencyRenderer(0),bankdatainacc:WtfGlobal.currencyRenderer(0),reconciledifference:WtfGlobal.currencyRenderer(0-this.reconcileAmount),reconcileamount:WtfGlobal.currencyRenderer(this.reconcileAmount)}); 
        this.disHtml = "";
        var arr=[WtfGlobal.getLocaleText("acc.common.currencyFilterLable") , WtfGlobal.getLocaleText("acc.je.debitAmt") ,WtfGlobal.getLocaleText("acc.je.creditAmt")];
        var header = "<span class='gridHeader' style='width: 95%;'>" + WtfGlobal.getLocaleText("acc.field.DifferentCurrencyAmount") + "</span>";
        header += "<div style='width: 100%;'>";
        header += "<span class='gridRow' style='font-weight:bold; width: 16%'>" + WtfGlobal.getLocaleText("acc.cnList.Sno") + "</span>";
        for (var i = 0; i < arr.length; i++) {
            header += "<span class='headerRow' style='width:28%' ! important;'>" + arr[i] + "</span>";
        }
        header += "</div><div style='width:100%;'><span class='gridLine'></span></div>";
        header += "<div style='width: 100%;'>";
        header += "</div>";
        this.disHtml += "<div class='reconcileCurrencyContainer' >" + header + "</div>";
        this.DifferentCurrencyAmount.body.update(this.disHtml);
        this.lgrid.getView().refresh();
        this.rgrid.getView().refresh();
    },
    editReconciliationWindow:function(){
        callReconciliationWindow(null,this.reconRec)
    },
    viewHistoryReport:function(){
        callBankReconciliationHistoryReport('recwin'+this.cmbAccount.getValue(),this.cmbAccount.getValue(),this.startDate.getValue(),this.endDate.getValue())
    },
    viewBankReconciliationHistoryDetails:function(){
        callBankReconciliationHistoryDetails('recwin'+this.cmbAccount.getValue(),this.cmbAccount.getValue(),this.startDate.getValue(),this.endDate.getValue())
    },
    viewReport:function(){
//        callBankReconciliationReport('recwin'+this.cmbAccount.getValue(),this.cmbAccount.getValue())
        var rec={};
        rec.accountid=this.cmbAccount.getValue();
        rec.startdate=this.startDate.getValue();
        rec.statementdate=this.endDate.getValue();
        callReconciliationLedger(rec,true);
    },
    updateData:function(accid,startdate,enddate,endingbalance,reconcileAmount,reconRec){
        this.reconRec.endingbalance=endingbalance;
        this.cmbAccount.setValue(accid);
        this.startDate.setValue(startdate);
        this.endDate.setValue(enddate);
        this.reconcileAmountField.setValue(reconcileAmount);
        this.reconcileAmount=reconcileAmount;
        this.accountID = accid;
        this.reconRec = reconRec;
//        this.accStore.on('load',function(){this.showLedger(accid);},this);
        this.accStore.load();
    },
    formatAccountName:function(val,m,rec){
         if(val=="Total"){return "<b>"+val+"</b>";}
         else{return val}
    },
    onClick:function(){
        if(this.accountID != this.cmbAccount.getValue()){
            this.accountChanged = true;
        }
        this.accountID=this.cmbAccount.getValue();
        var ss=this.quickPanelSearch1.getValue();
        ss=(ss==WtfGlobal.getLocaleText("acc.bankReconcile.SearchwithJENumberAndName")||ss==WtfGlobal.getLocaleText("acc.bankReconcile.SearchwithJENumber"))?"":ss;
        this.exportRecord['ss']=ss;
        this.exportRecord['accountid']=this.accountID;
        this.exportRecord['enddate']=WtfGlobal.convertToGenericEndDate(this.endDate.getValue()); //Bank Reconciliation Export PDF  //ERP-8205,8316,8328
        this.exportRecord['stdate']=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        this.exportRecord['isConcileReport']=this.isConcileReport; 
        this.exportRecord['dateFilter'] = this.dateFilter != undefined ? this.dateFilter.getValue() : 0
        this.draftRec = null;
        if(this.accountChanged && this.accountsDraftInfo.hasOwnProperty(this.accountID) && Wtf.account.companyAccountPref.columnPref && Wtf.account.companyAccountPref.columnPref.activateBankReconcilitaionDraft){
            var rec = {
                accountid : this.accountID,
                draftRec : this.accountsDraftInfo[this.accountID],
                startdate : this.startDate.getValue(),
                newstatementbalance : this.reconcileAmountField.getValue(),
                isFromReconcilation : true, // used this flag to hide cancel button, when reconcilition window open on account change.
                statementdate : this.endDate.getValue()
            };
            callReconciliationWindow(null,rec);
            this.accountChanged = false;
        }else{
            this.fetchLedger();
            if(!this.isConcileReport){//updating reconcile details in case of reconcile case only
                this.updateReconcileDetails();
            }
        }
    },
    updateReconcileDetails:function(){
        Wtf.Ajax.requestEx({
            url:"ACCReconciliation/getLastReconcileAmountAndDate.do",
            params:{
                accountid:this.accountID
            }
        }, this,function (response) {
            if (response.success) {
                this.reconciletplSummary.overwrite(this.reconcileCenterCalTemp.body,{
                    lastreconcileAmount:this.accountCurrencyRenderer(response.lastReconciledAmount),
                    lastreconcileDate:this.accountDateRenderer(response.lastReconciledDate)
                })
            } 
        }, function (response) {
            });   

    },
    accountCurrencySummaryRenderer: function(value){
        if ( this.cmbAccount !=undefined && this.cmbAccount.getValue() == "") {
            return WtfGlobal.summaryRenderer(WtfGlobal.currencyRenderer(value));
        } else {
            return WtfGlobal.summaryRenderer(WtfGlobal.rateCurrencySymbolForAccountCurrency(value));
        }
    },
    storeloaded:function(store){
       this.hideLoading();
        this.quickPanelSearch1.StorageChanged(store);
    },
    fetchLedger:function(){
        var sDate=this.startDate.getValue();
        var eDate=this.endDate.getValue();
       
        if(sDate>eDate){
            WtfComMsgBox(1,2);
            return;
        }
        this.loadingMask = new Wtf.LoadMask(document.body,{
            msg : WtfGlobal.getLocaleText("acc.common.load1")
        });
        
        this.loadingMask.show();
        if(this.accountID&&this.accountID.length>0){
            var ss=this.quickPanelSearch1.getValue();
            ss=(ss==WtfGlobal.getLocaleText("acc.bankReconcile.SearchwithJENumberAndName")||ss==WtfGlobal.getLocaleText("acc.bankReconcile.SearchwithJENumber"))?"":ss;
            this.exportRecord['ss']=ss;
            this.exportRecord['dateFilter']=this.dateFilter != undefined ? this.dateFilter.getValue() : 0;
            var params={
                mode:67,
                ss:ss,
                isConcileReport:this.isConcileReport,
                accountid:this.accountID,
                stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()), //Bank Reconciliation Report
                dateFilter:this.dateFilter != undefined ? this.dateFilter.getValue() : 0,
                loadTransactionDetails:true
            }
            WtfGlobal.setAjaxTimeOut();
            Wtf.Ajax.requestEx({
                //url:Wtf.req.account+'CompanyManager.jsp',
                url:"ACCReports/getReconciliationData.do",
                params:params
            }, this, this.successCallback, this.failureCallback);
        }else{
            if (this.loadingMask) {
                this.loadingMask.hide();
            }
            this.hideLoading();
        }
    },
    successCallback:function(response){
        if(response.success){
            WtfGlobal.resetAjaxTimeOut();
            this.total=response.data.total;
            this.totalinacc=response.data.totalinacc;
            this.bankdata=response.data.bankdata;
            this.currencysymbol=response.data.currencysymbol;
            this.bankdatainacc=response.data.bankdatainacc;
            this.bankBookBalanceinAcc = response.data.bankdatainacc;    //Balance As per Bank Book in Account Currency
            this.openingbankdatainacc=response.data.openingbankdatainacc;
            this.openingbalance=response.data.openingbalance;
            this.lgrid.store.loadData(response.data);
            this.rgrid.store.loadData(response.data);
            this.currencyData = response.data.currencyData;
            this.currencyCount = response.data.currencyCount;
            if(this.loadingMask){
             this.loadingMask.hide();
            }
            this.upadateSummary();
            
            if(this.reconRec && this.reconRec.jeIds){
                var lgridRecs = [];
                var rgridRecs = [];
                var jeIds = this.reconRec.jeIds.split(",");
                for(var i=0 ; i < jeIds.length ; i++){
                    var rec = WtfGlobal.searchRecord(this.lgrid.store,jeIds[i], "d_journalentryid");
                    if(rec !=undefined ){
                        lgridRecs.push(rec);
                        continue;
                    }
                    rec = WtfGlobal.searchRecord(this.rgrid.store,jeIds[i], "c_journalentryid");
                    if(rec !=undefined ){
                        rgridRecs.push(rec);
                    }
                }
                var isDataChanged = false;
                if(lgridRecs.length > 0){
                    this.lgrid.getSelectionModel().selectRecords(lgridRecs);
                    isDataChanged = true;
                }
                if(rgridRecs.length > 0){
                    this.rgrid.getSelectionModel().selectRecords(rgridRecs);
                    isDataChanged = true;
                }
                if(isDataChanged){
                    this.lgrid.getSelectionModel().fireEvent("selectionchange",this);
                }
            }
        }
    },

    upadateSummary:function(){
        var lefttotalinacc=this.totalinacc[0];
        var righttotalinacc=this.totalinacc[1]
        this.tplSummary.overwrite(this.southEastCalTemp.body, {
            presentedchequesvaluesinacc: this.accountCurrencyRenderer(righttotalinacc),
            clearedchequesvaluesinacc: this.accountCurrencyRenderer(lefttotalinacc),
            bankdatainacc: this.accountCurrencyRenderer(this.bankdatainacc),    
//            openingbankdatainacc: this.accountCurrencyRenderer(this.openingbankdatainacc),
            endingbalanceinacc: this.accountCurrencyRenderer(this.bankdatainacc + lefttotalinacc + righttotalinacc),
            reconcileamount: this.accountCurrencyRenderer(this.reconcileAmount),
            reconciledifference: this.accountCurrencyRenderer(this.bankdatainacc + lefttotalinacc + righttotalinacc-this.reconcileAmount)
        }); 
        
        if (this.diffSummaryTemplate != null && this.diffSummaryTemplate != undefined) {    //Neeraj D. code for opening balance in Bank Reconciliation Summary
            this.diffSummaryTemplate.overwrite(this.southopeningDiffPanel.body, {
                openingbankdatainacc: this.accountCurrencyRenderer(this.openingbankdatainacc)
            });
        }
        
        this.disHtml = "";
        var arr=[WtfGlobal.getLocaleText("acc.common.currencyFilterLable") , WtfGlobal.getLocaleText("acc.je.debitAmt") ,WtfGlobal.getLocaleText("acc.je.creditAmt")];
        var header = "<span class='gridHeader' style='width: 95%;'>"+WtfGlobal.getLocaleText("acc.field.DifferentCurrencyAmount")+"</span>";   
        header += "<div style='width: 100%;'>";
        header += "<span class='gridRow' style='font-weight:bold; width: 16%'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
        for(var i=0;i<arr.length;i++){
            header += "<span class='headerRow' style='width:28%' ! important;'>" + arr[i] + "</span>";
        }
        header += "</div><div style='width:100%;'><span class='gridLine'></span></div>";  
        header += "<div style='width: 100%;'>";
        for(var i=0;i<this.currencyCount;i++){
            var currency=this.currencyData[i].currencysymbol!=undefined?this.currencyData[i].currencysymbol:"";
            var transcurname = this.currencyData[i].transcurname!=undefined?this.currencyData[i].transcurname:"";
            var c_amount= this.currencyData[i].c_amount!=undefined?this.currencyData[i].c_amount:0;
            var d_amount= this.currencyData[i].d_amount!=undefined?this.currencyData[i].d_amount:0;
            header += "<span class='gridRow' style='width: 16%'>"+(i+1)+".</span>";
            header += "<span class='gridRow' style='width: 28%;' ! important;'>"+transcurname+"&nbsp;</span>";
            header += "<span class='gridRow' style='width:28%;' ! important;'>"+WtfGlobal.addCurrencySymbolOnly(d_amount,currency,[true])+"</span>";
            header += "<span class='gridRow' style='width: 28%;' ! important;'>"+WtfGlobal.addCurrencySymbolOnly(c_amount,currency,[true])+"</span>";
            header +="<br>";
        }
        header += "</div>";
        this.disHtml += "<div class='reconcileCurrencyContainer'>" + header + "</div>";

        this.DifferentCurrencyAmount.body.update(this.disHtml);
    },

    failureCallback:function(response){
        if(this.loadingMask){
            this.loadingMask.hide();
        }
    },
    getDates:function(start){
        var d=Wtf.serverDate;
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
    showLedger:function(accid){
        var i=this.accStore.find("accountid",accid);
        if(i>=0){
            this.cmbAccount.setValue(accid);
            this.accountID=accid;
            this.exportRecord['accountid']=this.accountID;
            this.exportRecord['enddate']=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
            this.exportRecord['stdate']=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
            this.exportRecord['isConcileReport']=this.isConcileReport; 
            this.fetchLedger();
        }
    },

    onRender:function(config){
         Wtf.account.ReconciliationDetails.superclass.onRender.call(this,config);         
    },
    
    setSequenceFormat:function(config){//this method used to set default sequence format on combo when store loaded
        if(this.sequenceFormatStore.getCount()>0){
            var count=this.sequenceFormatStore.getCount();
            for(var i=0;i<count;i++){
                var seqRec=this.sequenceFormatStore.getAt(i);
                if(seqRec.json.isdefaultformat=="Yes"){
                    this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                    break;
                }
            }
            if(this.sequenceFormatCombobox.getValue()!="" || this.isViewMode){
                this.getNextSequenceNumber(this.sequenceFormatCombobox);
            } else{
                this.reconcileNumber.setValue("");
                this.reconcileNumber.disable();
            }
        }
    },
    
    getNextSequenceNumber:function(combo){
        if(this.isViewMode){//view mode
            this.sequenceFormatCombobox.disable();
            this.reconcileNumber.disable();  
            var index=WtfGlobal.searchRecordIndex(this.sequenceFormatStore,this.record.data.sequenceformatid,"id");
            if(index!=-1){
                this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid); 
            } else {
                this.sequenceFormatCombobox.setValue("NA"); 
            }
        } else{//create new case
            if(combo.getValue()=="NA"){
                this.reconcileNumber.enable();
                this.reconcileNumber.setValue("");
            } else {
                Wtf.Ajax.requestEx({
                    url:"ACCCompanyPref/getNextAutoNumber.do",
                    params:{
                        from:this.isConcileReport ? 1137 : 1136,
                        sequenceformat:combo.getValue(),
                        oldflag:false
                    }
                }, this,function(resp){
                    if(resp.data=="NA"){
                        this.reconcileNumber.enable();
                        this.reconcileNumber.setValue("");
                    }else {
                        this.currentBatch=resp.data;
                        this.reconcileNumber.setValue(resp.data);
                        this.reconcileNumber.disable();
                    }
                });
            }
        }
    },

    onLCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var dataindex=g.getColumnModel().getDataIndex(j);
        if(dataindex == "transactionID"){
            var rec=this.LStore.getAt(i);
            var type=rec.data['type'];
            var withoutinventory=rec.data['withoutinventory'];            
            viewTransactionTemplate1(type, rec,withoutinventory,rec.data.billid);
        } else if(dataindex == "d_entryno") {         
            var record=this.LStore.getAt(i);
            var jid=record.data['d_journalentryid'];
            this.fireEvent('journalentry',jid,true);
        }                
    },
    
    onRCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var dataindex=g.getColumnModel().getDataIndex(j);
        if(dataindex == "transactionID"){
            var rec=this.RStore.getAt(i);
            var type=rec.data['type'];
            var withoutinventory=rec.data['withoutinventory'];            
            viewTransactionTemplate1(type, rec,withoutinventory,rec.data.billid);
        } else if(dataindex == "c_entryno") {         
            var record=this.RStore.getAt(i);
            var jid=record.data['c_journalentryid'];
            this.fireEvent('journalentry',jid,true);
        }        
    },
 hideLoading:function(){Wtf.MessageBox.hide();},
    dateRenderer:function(v){
        if(v) return v.format('Y M d');
        return "";
    },
    getReconciliationDetails:function(isleft){
        var arr=[];
        var len=(isleft?this.lgrid.getStore().getCount():this.rgrid.getStore().getCount());
        var store=isleft?this.lgrid.getStore():this.rgrid.getStore();
        for(var i=0;i<len;i++){
            var rec=store.getAt(i);
            var d_accountname = '';
            if(rec.data['d_accountname']!=''){
//                d_accountname = encodeURI(rec.data['d_accountname']);
                  d_accountname = rec.data['d_accountname'];    //SDP-9051
            }
            rec.set("d_accountname",d_accountname);
            var c_accountname = '';
            if(rec.data['c_accountname']!=''){
//                c_accountname = encodeURI(rec.data['c_accountname']);
                  c_accountname = rec.data['c_accountname'];    //SDP-9051
            }
            rec.set("c_accountname",c_accountname);
            var isSelected=(isleft?this.lsm.isSelected(i):this.rsm.isSelected(i));
            if(isSelected)
                arr.push(i);
            }
            var grid=(isleft?this.lgrid:this.rgrid);
        return WtfGlobal.getJSONArray(grid,true,arr);

    },
    getUnReconciliationDetails:function(isleft){    //To get the details of Un-reconciled Records
        var arr=[];
        var len=(isleft?this.lgrid.getStore().getCount():this.rgrid.getStore().getCount());
        var store=isleft?this.lgrid.getStore():this.rgrid.getStore();
        for(var i=0;i<len;i++){
            var rec=store.getAt(i);
            var d_accountname = '';
            if(rec.data['d_accountname']!=''){
//                d_accountname = encodeURI(rec.data['d_accountname']);
                  d_accountname = rec.data['d_accountname'];    //SDP-9051
            }
            rec.set("d_accountname",d_accountname);
            var c_accountname = '';
            if(rec.data['c_accountname']!=''){
//                c_accountname = encodeURI(rec.data['c_accountname']);
                  c_accountname = rec.data['c_accountname'];    //SDP-9051
            }
            rec.set("c_accountname",c_accountname);
            var isSelected=(isleft?this.lsm.isSelected(i):this.rsm.isSelected(i));
            if(!isSelected)
                arr.push(i);
        }
        var grid=(isleft?this.lgrid:this.rgrid);
        return WtfGlobal.getJSONArray(grid,true,arr);

    },
    saveData:function(){
        if(this.reconcileButton){
            this.reconcileButton.disable();
        }
        var ldetails=this.getReconciliationDetails(true);
        var rdetails=this.getReconciliationDetails(false);
        var uldetails=this.getUnReconciliationDetails(true);  //Unreconciled Left Details
        var urdetails=this.getUnReconciliationDetails(false); //Unreconciled Right Details
        if(ldetails=="[]"&&rdetails=="[]"){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.bankReconcile.msg1")], 2);
            this.fetchLedger();
            return;
        }
        
            this.reconcileSave= new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),
            iconCls:'pwnd save',
            scope: this,            
            handler: function(){
                               if(!this.clearanceDate.isValid()){
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.PleaseEnterReconciliationDate")],2);
                                    return;
                                }
                                if(!this.reconcileNumber.isValid()){
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.bankReconcileNumber.alert.msg7")],2);
                                    return;
                                }
                                 /**
                                  * Checking clearance date with JE date of document if isPostingDateCheck() is true and clearanceDate is less than jeDate than show invalid message. 
                                  */
                                var invalidDate = false;
                                var invalidJEDate = false;
                                var invalidUnreconcilDate = false;
                                var jeIds = "";
                                if(ldetails!=[]){
                                    var templDetails = eval('('+ldetails+')');
                                    for(var i=0; i<templDetails.length; i++){   //ERP-9338 : Clearance date should be equal or greater than cheque date
                                        if(WtfGlobal.getDayMonthYearDate(this.clearanceDate.getValue()) < WtfGlobal.getDayMonthYearDate(new Date(templDetails[i].chequedate))||(this.isConcileReport && WtfGlobal.getDayMonthYearDate(this.clearanceDate.getValue()) < WtfGlobal.getDayMonthYearDate(new Date(templDetails[i].chequedate)))){
                                            invalidDate = true;
                                            jeIds += templDetails[i].d_entryno +", "
                                        }else if (CompanyPreferenceChecks.isPostingDateCheck() && !this.isConcileReport && (WtfGlobal.getDayMonthYearDate(this.clearanceDate.getValue()) < WtfGlobal.getDayMonthYearDate(new Date(templDetails[i].jeDate)))) {
                                            invalidJEDate = true;
                                            jeIds += templDetails[i].d_entryno + ", "
                                        }else if(CompanyPreferenceChecks.isPostingDateCheck() &&  (this.isConcileReport && WtfGlobal.getDayMonthYearDate(this.clearanceDate.getValue()) < WtfGlobal.getDayMonthYearDate(new Date(templDetails[i].lastreconciledate)))){
                                            invalidUnreconcilDate = true;
                                            jeIds += templDetails[i].d_entryno + ", "
                                        }
                                    }
                                }
                                if(rdetails!=[]){
                                    var templDetails = eval('('+rdetails+')');
                                    for(var i=0; i<templDetails.length; i++){   //ERP-9338 : Clearance date should be equal or greater than cheque date
                                        if(WtfGlobal.getDayMonthYearDate(this.clearanceDate.getValue()) < WtfGlobal.getDayMonthYearDate(new Date(templDetails[i].chequedate))||(this.isConcileReport && WtfGlobal.getDayMonthYearDate(this.clearanceDate.getValue()) < WtfGlobal.getDayMonthYearDate(new Date(templDetails[i].chequedate)))){
                                            invalidDate = true;
                                            jeIds += templDetails[i].c_entryno +", "
                                        }else if (CompanyPreferenceChecks.isPostingDateCheck() && !this.isConcileReport && (WtfGlobal.getDayMonthYearDate(this.clearanceDate.getValue()) < WtfGlobal.getDayMonthYearDate(new Date(templDetails[i].jeDate)))){
                                            invalidJEDate = true;
                                            jeIds += templDetails[i].c_entryno +", "
                                        }else if(CompanyPreferenceChecks.isPostingDateCheck() &&  (this.isConcileReport && WtfGlobal.getDayMonthYearDate(this.clearanceDate.getValue()) < WtfGlobal.getDayMonthYearDate(new Date(templDetails[i].lastreconciledate)))){
                                            invalidUnreconcilDate = true;
                                            jeIds += templDetails[i].c_entryno + ", "
                                        }
                                    }
                                }
                                if(invalidDate){
                                    jeIds =jeIds.substring(0,jeIds.length-2);
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),this.isConcileReport?WtfGlobal.getLocaleText("acc.field.UnreconcileDatecannotbebeforeanyrecordsClearencedateJEIds")+ jeIds:WtfGlobal.getLocaleText("acc.field.ClearanceDatecannotbebeforeanyrecordsdateJEIds") + jeIds],2);
                                    return;
                                }
                                if(invalidJEDate){
                                    jeIds =jeIds.substring(0,jeIds.length-2);
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.ClearanceDatecannotbebeforeanyJEPostingdateJEIds") + jeIds],2);
                                    return;
                                }
                                if(invalidUnreconcilDate){
                                    jeIds =jeIds.substring(0,jeIds.length-2);
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.UnreconcileDatecannotbebeforeanyrecordsReconciliationJEIds") + jeIds],2);
                                    return;
                                }
                                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.bankReconcile.save"),WtfGlobal.getLocaleText("acc.bankReconcile.msg2"),function(btn){
                                    if(btn!="yes") {return;}
                                    this.msg= WtfComMsgBox(27,4,true);
                                    var rec=[];
                                    rec.d_details=ldetails;
                                    rec.c_details=rdetails;
                                    rec.mode=56;
                                    rec.startdate=WtfGlobal.convertToGenericDate(this.startDate.getValue());
                                    rec.enddate=WtfGlobal.convertToGenericDate(this.endDate.getValue());
                                    
                                    rec.ud_details=uldetails;    //Un-reconciled left details
                                    rec.uc_details=urdetails;    //Un-reconciled right details
                                    rec.reconcilenumber=this.reconcileNumber.getValue();
                                    rec.sequenceformat=this.sequenceFormatCombobox.getValue();                                    
                                    rec.bankBookBalanceinAcc=this.bankBookBalanceinAcc;     //Balance As per Bank Book in Account Currency
                                    rec.bankStmtBalanceinAcc = this.bankStmtBalanceinAcc;   //Balance As per Bank Statement
                                        
                                    rec.clearanceDate=WtfGlobal.convertToGenericDate(this.clearanceDate.getValue());
                                    rec.endingbalance=this.reconRec.endingbalance;
                                    rec.clearingbalance=this.clearingBalance;
                                    rec.accid=this.cmbAccount.getValue();
                                    rec.isConcileReport=this.isConcileReport;
                                    rec.docID=this.docID;
//                                    rec.reconcileAmount=this.reconcileAmount;
                                    
                                    Wtf.Ajax.requestEx({
                                        url: "ACCReconciliation/saveBankReconciliation.do",
                                        params: rec
                                    },this,this.genSuccessResponse,this.genFailureResponse);
                            },this);
            }
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
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:this.isConcileReport ? "autounreconcilenumber" : "autoreconcilenumber",
                isEdit:false 
            }
        });
        this.sequenceFormatCombobox = new Wtf.form.ComboBox({      //Sequence Format No.      
            triggerAction:'all',
            mode: 'local',
            id:'sequenceFormatCombobox'+this.heplmodeid+this.id,
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.Sequenceformat.tip")+"'>"+ WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat")+"</span>",
            valueField:'id',
            displayField:'value',
            store:this.sequenceFormatStore,
            anchor:'70%',
            typeAhead: true,
            forceSelection: true,
            name:'sequenceformat',
            hiddenName:'sequenceformat',
            allowBlank:false
        });
        this.sequenceFormatStore.load();
        this.sequenceFormatStore.on('load',this.setSequenceFormat,this);
        this.sequenceFormatCombobox.on('select',this.getNextSequenceNumber,this);
        
        this.reconcileNumber= new Wtf.form.TextField({
            fieldLabel: this.isConcileReport ? WtfGlobal.getLocaleText("acc.bankReconcileNumber.msg8") : WtfGlobal.getLocaleText("acc.bankReconcileNumber.msg6")+"*",  // Un-Reconcile / Reconcile No.*',
            name: 'refno',
            //hidden :true,        //this.isConcileReport? true : false,    //Do not show the button in case of View Reconcile Report
            allowBlank:false,
            width : 200,
            anchor:'70%'
        //readOnly : this.isViewMode ? true : false
        });
        this.fileuploadwin = new Wtf.form.FormPanel(
                {                   
                    url : "ACCReconciliation/attachDocuments.do",
                    waitMsgTarget : true,
                    fileUpload : true,
                    method : 'POST',
                    border : false,
                    scope : this,
                    bodyStyle: "background: transparent;",
//                    style: "background: transparent;padding-top: 20px;",
                    style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
                    items : [this.clearanceDate=new Wtf.form.DateField({
                                name:"clearancedate",
                                width : 200,
                                fieldLabel:this.isConcileReport? WtfGlobal.getLocaleText("acc.bankReconcile.UnreconcileDate")+"*":WtfGlobal.getLocaleText("acc.bankReconcile.reconcileDate")+"*",
                                allowBlank:false,
                                format:WtfGlobal.getOnlyDateFormat()
                            }),
                    this.tName = new Wtf.form.TextField(
                    {
                        fieldLabel : WtfGlobal.getLocaleText("acc.invoiceList.attachDocuments"),
                        //allowBlank : false,
                        name : 'file',
                        inputType : 'file',
                        width : 200,
                        blankText:WtfGlobal.getLocaleText("acc.field.SelectFileFirst"),
                        msgTarget :'qtip'
                    }), this.sequenceFormatCombobox, this.reconcileNumber],
                    buttons : [{
                        anchor : '90%',
                        id : 'save',
                        text : WtfGlobal.getLocaleText("acc.invoiceList.bt.upload"),
                        scope : this,
                        handler : this.upfileHandler
                    }]
                
                });
//                if(!this.isConcileReport){  //Add Reconcile Field in Reconcile Report & not in View Reconcile Report
                    //this.fileuploadwin.add(this.sequenceFormatCombobox, this.reconcileNumber);
//                }                
            
        this.clearanceWindow = new Wtf.Window({
            modal: true,
            iconCls: 'accountingbase ledger',
            title: this.isConcileReport? WtfGlobal.getLocaleText("acc.bankReconcile.UnreconcileDate"):WtfGlobal.getLocaleText("acc.bankReconcile.reconcileDate"),//"Clearance Date",
            items: [this.fileuploadwin],
            buttons:[this.reconcileSave,{
                        text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                        scope:this,
                        handler:function(){
                            this.clearanceWindow.close();
                            this.fetchLedger();
                        }
               }]
        });
        this.clearanceWindow.show();
//    }
    },
    
    saveAsDraft : function(){
//        if(this.reconcileButton){
//            this.reconcileButton.disable();
//        }
        var ldetails=this.getReconciliationDetails(true);
        var rdetails=this.getReconciliationDetails(false);
        
        if(ldetails=="[]"&&rdetails=="[]"){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.bankReconcile.msg1")], 2);
            this.fetchLedger();
            return;
        }
        
        if(this.draftRec != null && this.draftRec != undefined && this.draftRec != ""){
            this.saveDraft(JSON.clone(this.draftRec));
            return;
        }
        
        var description= new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.product.description"),  // Description*',
            name: 'description',
//            maskRe : Wtf.HTMLRegex,
//            regex : Wtf.HTMLRegex,
            width : 290,
            maxLength : 255,
            value : (this.reconRec && this.reconRec.draftRec) ? this.reconRec.draftRec.description : "",
            anchor :"80%"
        });
        this.draftForm = new Wtf.form.FormPanel({                   
            url : "ACCReconciliation/attachDocuments.do",
            waitMsgTarget : true,
            fileUpload : true,
            method : 'POST',
            border : false,
            scope : this,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;",
            items : [description]
                
        });   
        
        var saveBtn = new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),
//            iconCls:'pwnd save',
            scope: this,            
            handler: function(){
                if(this.draftForm.getForm().isValid()){
                    var rec = this.draftForm.getForm().getValues();
                    this.saveDraft(rec);
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation"),WtfGlobal.getLocaleText("acc.common.providevalidData")]);
                }
            }
        });
        var cancelBtn = new Wtf.Button({
                text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope:this,
                handler:function(){
                    this.saveAsDraftWin.close();
                    this.fetchLedger();
                }
            });
            
        this.saveAsDraftWin = new Wtf.Window({
            modal: true,
            iconCls: 'accountingbase ledger',
            width : 500,
            height : 200,
            title: WtfGlobal.getLocaleText("acc.common.saveasdraft"),
            items: [this.draftForm],
            buttons:[saveBtn,cancelBtn]
        });
        this.saveAsDraftWin.show();
    },
    
    saveDraft : function(rec){
        rec.accountid = this.cmbAccount.getValue();
        rec.newstatementbalance = this.reconcileAmountField.getValue();
        rec.fromdate = WtfGlobal.convertToDateOnly(this.startDate.getValue());
        rec.todate = WtfGlobal.convertToDateOnly(this.endDate.getValue());
        rec.bankBookBalanceinAcc = this.bankBookBalanceinAcc;     //Balance As per Bank Book in Account Currency
        rec.bankStmtBalanceinAcc = this.bankStmtBalanceinAcc;   //Balance As per Bank Statement
        rec.clearingbalance=this.clearingBalance;
        
        var ldetails=this.getReconciliationDetails(true);
        var rdetails=this.getReconciliationDetails(false);
        var uldetails=this.getUnReconciliationDetails(true);  //Unreconciled Left Details
        var urdetails=this.getUnReconciliationDetails(false); //Unreconciled Right Details
                
        var selectedRecs = {
            "ldetails" : eval( "(" + ldetails + ")"),
            "rdetails" : eval( "(" + rdetails + ")"),
            "uldetails" : eval( "(" + uldetails + ")"),
            "urdetails" : eval( "(" + urdetails + ")")
        }
                        
        rec.selectedrec = JSON.stringify(selectedRecs);
        rec.jeIds = null;
                
        Wtf.Ajax.requestEx({
            url: "ACCReconciliation/saveBankReconciliationDraft.do",
            method : "POST",
            params: rec
        },this,function(response){
            if(response.success){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation"),response.msg],response.success*2+1);
                this.saveAsDraftWin.close();
                this.draftRec = {id : response.draftId};
                this.getDraftInfoForAllAccounts();
            } else {   //Fail Response
                var msg = "";
                if(response.msg)msg=response.msg;
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
            }
            var panel = Wtf.getCmp("reconcilationDrafts");
            if(panel){
                panel.reportList.store.reload();
            }
        },this.genFailureResponse);
    },
    
    upfileHandler : function() {
    if (this.fileuploadwin.form.isValid()) {
        Wtf.getCmp('save').disabled = true;
    }
        if (this.fileuploadwin.form.isValid()) {
            this.fileuploadwin.form.submit({
                scope : this,
                failure : function(frm, response) {
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.bankReconcile.import.msg11"));
                },
                success : function(frm, response) {
                    this.docID=response.result.docID;
                    if(this.docID!=null && this.docID!=undefined && this.docID!=""){
                        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.mrp.field.fileUploadedSuccess"));
                    } else {
                        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.select.file.to.upload"));
                    }
                }
            })
        }
    },
     genSuccessResponse:function(response){
            if(response.success){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation"),response.msg],response.success*2+1);
                    if(this.clearanceWindow)
                        this.clearanceWindow.close();
//                }
                this.recArr = this.lgrid.getSelectionModel().getSelections();
                this.recArr1 = this.rgrid.getSelectionModel().getSelections();
                for(var i=0;i<this.recArr.length;i++){
                    var ind=this.LStore.indexOf(this.recArr[i])
                    this.LStore.remove(this.recArr[i]);  
                }

                for(var i=0;i<this.recArr1.length;i++){
                    var ind=this.RStore.indexOf(this.recArr1[i])
                    this.RStore.remove(this.recArr1[i]);  
                }
                this.lgrid.getView().refresh();
                this.rgrid.getView().refresh();
//                this.upadateSummary();
                this.fetchLedger();
                if(!this.isConcileReport){//updating reconcile details in case of reconcile case only
                    this.updateReconcileDetails();
                }
                this.getDraftInfoForAllAccounts();
        } else {   //Fail Response
             var msg = "";
             if(response.msg)msg=response.msg;
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        }
    },

    genFailureResponse:function(response){
        var msg= WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        },
        
    accountCurrencyRenderer:function(val,m,rec){ //added forigin currency symbol for  Amount in Account Currency
        if (!(val==="")) {
            var symbol=((this.currencysymbol==null||this.currencysymbol==undefined||this.currencysymbol=="")?WtfGlobal.getCurrencySymbol():this.currencysymbol);        
            var v=parseFloat(val);
            if(isNaN(v)) return val;
                v=WtfGlobal.conventInDecimal(v,symbol);

            return '<div class="currency">'+v+'</div>';
        } else {
            return "";
        }
    },
    accountDateRenderer:function(val,m,rec){  // for last Reconcilied date  
        if (!(val==="-")){
            return '<div class="currency">'+val+'</div>';
        } else {
            return "-";
        }
    },
    updateCheckandPaymentsAmount: function () {
        var camount = 0, camount_acc = 0;
        this.RStore.each(function (rec) {
            if (rec.data['c_amountinacc'] != undefined) {
                camount_acc += rec.data['c_amountinacc'];
            }
            if (rec.data['c_amount'] != undefined) {
                camount += rec.data['c_amount'];
            }
        });
        
        if (this.currencysymbol != null || this.currencysymbol != undefined || this.currencysymbol != "") {
            this.creditTextValue_acc.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(camount_acc, this.currencysymbol) + "</B>";
        } else{
            this.creditTextValue_acc.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(camount_acc, WtfGlobal.getCurrencySymbol()) + "</B>";
        }
        this.creditTextValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(camount, WtfGlobal.getCurrencySymbol()) + "</B>";
        
    },
    updateDepositsAmount: function () {
        var damount_acc = 0, damount = 0;
        this.LStore.each(function (rec) {
            if (rec.data['d_amountinacc'] != undefined) {
                damount_acc += rec.data['d_amountinacc'];
            }
            if (rec.data['d_amount'] != undefined) {
                damount += rec.data['d_amount'];
            }
        });
        if (this.currencysymbol != null || this.currencysymbol != undefined || this.currencysymbol != "") {
            this.debitTextValue_acc.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(damount_acc, this.currencysymbol) + "</B>";
        } else{
            this.debitTextValue_acc.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(damount_acc, WtfGlobal.getCurrencySymbol()) + "</B>";
        }
        this.debitTextValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(damount, WtfGlobal.getCurrencySymbol()) + "</B>";
    },
    /**
     * matchBankRecords function is used to display import bank reconciliation screen and get 
     * imported records from bank and matched records from backend.ERP-41871
     * @returns {undefined}
     */
    matchBankRecords: function () {
        //Show Message
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText('acc.dashboard.bankReconciliation'),
            msg: WtfGlobal.getLocaleText("acc.import.bankWaitMsg"),
            width: 520,
            closable: false,
            scope: this,
            fn: function (btnText) {
                if (btnText) {
                    if (btnText == "ok") {
                        WtfGlobal.setAjaxTimeOutFor30Minutes();
                        Wtf.Ajax.requestEx({
                            url: this.extraConfig.urlForBank + ('?type=submit&do=importFromBank&fetchCustomFields=' + false),
                            waitMsg: 'importing...',
                            scope: this,
                            params: this.extraConfig
                        },
                        this,
                                function (res) {
                                    if (res.success) {
                                        callImportBankReconciliationReport(this.cmbAccount.getValue(), this.extraConfig.startdate, this.extraConfig.enddate, this.extraConfig.openingBalance, res, true);     //need to add more params
                                        WtfGlobal.resetAjaxTimeOut();
                                        closeImportWindow();
                                    } else { // Failure
                                        WtfImportMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), res.msg], 1);
                                    }
                                },
                                function (res) {
                                    //To change Alerts

                                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.import.msg8"), WtfGlobal.getLocaleText("acc.import.msg9") + '<br/>' + WtfGlobal.getLocaleText("acc.import.msg10"));
                                    WtfGlobal.resetAjaxTimeOut();
                                    closeImportWindow();
                                });
                    }
                }
            }.createDelegate(this),
            buttons: Wtf.MessageBox.OK,
            icon: Wtf.MessageBox.INFO
        });
//        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.import.bankWaitMsg")], 0);
    },

//    deleteRows: function(){
//       var larr=[];
//       var rarr=[];
//       var ldata=[];
//       var rdata=[];
//       this.recArr = this.lgrid.getSelectionModel().getSelections();
//       this.recArr1 = this.rgrid.getSelectionModel().getSelections();
//       if(!this.lgrid.getSelectionModel().hasSelection() && !this.rgrid.getSelectionModel().hasSelection()){
//           WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.bankReconcile.msg3")],1);
//           return;
//       }
//       this.lgrid.getSelectionModel().clearSelections();
//       this.rgrid.getSelectionModel().clearSelections();
//       WtfGlobal.highLightRowColor(this.lgrid,this.recArr,true,0,2);
//       WtfGlobal.highLightRowColor(this.rgrid,this.recArr1,true,0,2);
//       Wtf.MessageBox.show({
//       title: WtfGlobal.getLocaleText("acc.common.warning"), //"Warning",
//       msg: WtfGlobal.getLocaleText("acc.bankReconcile.msg4")+"<div><b>"+WtfGlobal.getLocaleText("acc.bankReconcile.msg5")+"</b></div>",
//       width: 500,
//       buttons: Wtf.MessageBox.OKCANCEL,
//       animEl: 'upbtn',
//       icon: Wtf.MessageBox.QUESTION,
//       scope:this,
//       fn:function(btn){
//            if(btn!="ok"){
//                for(var i=0;i<this.recArr.length;i++){
//                    var ind=this.Store.indexOf(this.recArr[i])
//                    var num= ind%2;
//                    WtfGlobal.highLightRowColor(this.lgrid,this.recArr[i],false,num,2,true);
//                }
//                for(var i=0;i<this.recArr1.length;i++){
//                    var ind=this.Store.indexOf(this.recArr1[i])
//                    var num= ind%2;
//                    WtfGlobal.highLightRowColor(this.rgrid,this.recArr1[i],false,num,2,true);
//                }
//                return;
//            }
//            for(i=0;i<this.recArr.length;i++){
//                larr.push(this.LStore.indexOf(this.recArr[i]));
//            }
//            for(i=0;i<this.recArr1.length;i++){
//                rarr.push(this.RStore.indexOf(this.recArr1[i]));
//            }
//
//            ldata= WtfGlobal.getJSONArray(this.lgrid,true,larr);
//            rdata= WtfGlobal.getJSONArray(this.rgrid,true,rarr);
//            Wtf.Ajax.requestEx({
////                url: Wtf.req.account+'CompanyManager.jsp',
//                url: "ACCReconciliation/deleteBankReconciliation.do",
//                params:{
//                   ldata:ldata,
//                   rdata:rdata,
//                   mode:58
//                }
//            },this,this.genSuccessResponse,this.genFailureResponse);
//       }});
//    }  
});

function loadReconcilationDrafts(id, check, isCash) {
    var reportList = Wtf.getCmp("reconcilationDrafts");
    if (reportList == null) {
        reportList = new Wtf.ReconcilationDraftList({
            closable: true,
            id : "reconcilationDrafts",
            title:"Reconciliation Drafts",
            tabTip:"Reconciliation Drafts",
            iconCls : "accountingbase widget-report-list",
            modal: true
        });

        Wtf.getCmp('as').add(reportList);
    }
    Wtf.getCmp('as').setActiveTab(reportList);
    Wtf.getCmp('as').doLayout();

}


Wtf.ReconcilationDraftList = Wtf.extend(Wtf.Panel, {
    initComponent:function() {
        this.createListGrid();

        Wtf.apply(this, {
            layout: 'fit',
            items :[this.reportList]
        }); 

        Wtf.ReconcilationDraftList.superclass.initComponent.apply(this, arguments);

    },
    createListGrid : function(){
        
        var selModel = new Wtf.grid.CheckboxSelectionModel({
            singleSelect:true
        });
        this.fetchBtn = new Wtf.Toolbar.Button({
            xtype: 'button',
            text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
            tooltip: WtfGlobal.getLocaleText("acc.bankReconcile.fetchTT"), //"Select a time period to view corresponding ledger records.",
            iconCls: 'accountingbase fetch',
            scope: this,
            handler: this.loadDrafts
        });
        
       this.allAccountRec = new Wtf.data.Record.create([
            {name: 'accid'},
            {name: 'accname'},
            {name: 'acccode'},
            {name: 'groupid'},
            {name: 'groupname'}
        ]);
        this.allAccountStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.allAccountRec),
            url : "ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreGLAccounts:true,
                ignoreCashAccounts:true,
                ignoreGSTAccounts:true,  
                ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true
            }
        });
        this.allAccountStore.on('load',function(){
            var rec = new Wtf.data.Record({
                accid : "",
                accname : "All"
            });
            this.allAccountStore.insert(0,rec);
        },this);        
        this.allAccountStore.load();
        
        this.Account = new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.account")+"*",
            hiddenName:"accountid",
            anchor:"100%",
            store: this.allAccountStore,
            valueField:'accid',
            displayField:'accname',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:250,
            allowBlank:false,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.mp.selAcc"),  //'Please select an Account...',
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            triggerAction:'all',
            scope:this
        });
        
        var tbarArray = [];
        tbarArray.push(this.Account,"-",this.fetchBtn);
        
         var record = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'accountname'},
            {name: 'accountid'},
            {name: 'description'},
            {name: 'currencysymbol'},
            {name: 'fromdate', type:'date'},
            {name: 'todate', type:'date'},
            {name: 'createdby'},
            {name: 'createdon'},
            {name: 'updatedon'},
            {name: 'jeIds'},
            {name: 'newstatementbalance'},
            {name: 'bankBookBalanceinAcc',type:'float'},
            {name: 'clearedChecksAmountinAcc',type:'float'},
            {name: 'clearedDepositsAmountinAcc',type:'float'},
            {name: 'unclearedChecksAmountinAcc',type:'float'},
            {name: 'unclearedDepositsAmountinAcc',type:'float'},
            {name: 'paymentsreconciled',type:'int'},
            {name: 'depositsreconciled',type:'int'},
            {name: 'clearingamount',type:'float'},
            {name: 'bankStmtBalanceinAcc'}
        ]);

        var jsonReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'count'
        }, record);

        var liststore = new Wtf.data.Store({
            reader: jsonReader,
            url: "ACCReconciliation/getBankReconcilationDrafts.do",
            baseParams: {
                companyid: companyid
            }
        });
        
        liststore.on("beforeload",function(){
            var baseParams = liststore.baseParams || {};
            if(this.Account.getValue()){
                baseParams.accountid = this.Account.getValue();
            }else{
                baseParams.accountid = null;
            }
        },this);
        liststore.load({
            start : 0,
            limit : 30
        });
    
        this.reportList = new Wtf.grid.GridPanel({
            layout:"fit",
            store : liststore,
            loadMask : true,
            tbar : tbarArray,
            columns: [selModel,
            {
                header: WtfGlobal.getLocaleText("acc.invoiceList.accName"), 
                dataIndex: 'accountname'
            },

            {
                header: WtfGlobal.getLocaleText("acc.invoiceList.expand.description"), 
                dataIndex: 'description',
                renderer:WtfGlobal.memoRenderer
            },

            {
                header: WtfGlobal.getLocaleText("acc.bankReconcile.startDate"), 
                dataIndex: 'fromdate',
                align:'center',
                renderer:WtfGlobal.onlyDateRenderer
            },
            {
                header: WtfGlobal.getLocaleText("acc.bankReconcile.statementDate"), 
                dataIndex: 'todate',
                align:'center',
                renderer:WtfGlobal.onlyDateRenderer
            },
            {
                header: WtfGlobal.getLocaleText("acc.bankreconcileAmount"), 
                dataIndex: 'newstatementbalance'
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.bankbook.balance") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.bankbook.balance") + "<div>",  //Bank Book Balance
                dataIndex:'bankBookBalanceinAcc',
                width:130,
                renderer:WtfGlobal.withoutRateCurrencySymbol,
                align:'right'
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.uncleared.checks") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.uncleared.checks") + "<div>",  //Total Uncleared Checks
                dataIndex:'unclearedChecksAmountinAcc',
                width:130,
                renderer:WtfGlobal.withoutRateCurrencySymbol,
                align:'right'
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.uncleared.deposits") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.uncleared.deposits") + "<div>",  //Total Uncleared Deposits
                dataIndex:'unclearedDepositsAmountinAcc',
                width:130,
                renderer:WtfGlobal.withoutRateCurrencySymbol,
                align:'right'
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.statement.balance") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.statement.balance") + "<div>",  //Bank Statement Balance
                dataIndex:'bankStmtBalanceinAcc',
                width:130,
                renderer:WtfGlobal.withoutRateCurrencySymbol,
                align:'right'
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.cleared.checks") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.cleared.checks") + "<div>",  //Total Cleared Checks
                dataIndex:'clearedChecksAmountinAcc',
                width:130,
                renderer:WtfGlobal.withoutRateCurrencySymbol,
                align:'right'
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.cleared.deposits") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.cleared.deposits") + "<div>",  //Total Cleared Deposits
                dataIndex:'clearedDepositsAmountinAcc',
                width:130,
                renderer:WtfGlobal.withoutRateCurrencySymbol,
                align:'right'
            },{
                header : WtfGlobal.getLocaleText("acc.wtfTrans.vvi"),
                dataIndex: 'status',
                width:60,
                renderer:function(){
                    return "<img id='AcceptImg' class='ViewR'  style='height:18px; width:18px;' src='images/report.gif' title="+WtfGlobal.getLocaleText("acc.field.ViewReport")+"></img>";
                }
            },
            {
                header : WtfGlobal.getLocaleText("acc.product.gridAction"),
                dataIndex: '',
                width:60,
                renderer: this.deleteRenderer.createDelegate(this)           
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcile.historyCheck") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcile.historyCheck") + "<div>",  //Total Cleared Deposits
                dataIndex:'paymentsreconciled',
                width:130,
                //                renderer:WtfGlobal.withoutRateCurrencySymbol,
                align:'right'
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcile.historyDep") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcile.historyDep") + "<div>",  //Total Cleared Deposits
                dataIndex:'depositsreconciled',
                width:130,
                //                renderer:WtfGlobal.withoutRateCurrencySymbol,
                align:'right'
//            },{
//                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcile.historyAmount") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcile.historyAmount") + "<div>",  //Total Cleared Deposits
//                dataIndex:'clearingamount',
//                width:130,
//                renderer:WtfGlobal.currencyDeletedRenderer,
//                align:'right'
            },
            {
                header: WtfGlobal.getLocaleText("acc.nee.69"), 
                dataIndex: 'createdby'
            }
            ],
            viewConfig: {
                //                forceFit: true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText('acc.common.norec'))
            },
            sm: selModel
        });
        
        this.reportList.on("render",function(){
            this.reportList.getView().applyEmptyText();
        },this);
        
        this.reportList.on("cellclick",this.viewReport, this);
        this.reportList.getSelectionModel().on("selectionchange", this.enableDisableButtons, this);
    },
    
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    
    loadDrafts : function(){
        this.reportList.store.reload();
    },
    
    viewReport: function(obj,row,col,event) {
        if(event.getTarget("img[class='ViewR']")) {
            var rec = this.reportList.store.getAt(row);
            if(rec !=undefined){
                var data = rec.data;
                data = JSON.clone(data);
                data.draftRec = JSON.clone(data);
                data.startdate = rec.data.fromdate;
                data.reconcileAmount = rec.data.newstatementbalance;
                data.statementdate = rec.data.todate;
                callReconciliationWindow(null,data);
            }
        }
        if(event.getTarget("div[class='pwnd delete-gridrow']")) {
            rec = this.reportList.store.getAt(row);
            if(rec !=undefined){
                data = rec.data;
                this.deleteReports(data.accountid);
            }
        }
    },
    
    enableDisableButtons: function(selModel, eOpts) {
        var selected = selModel.getSelections();
//        if (selected.length > 0) {
//            this.deleteReportBtn.enable();
//        } else {
//            this.deleteReportBtn.disable();
//        }
    },
    
    deleteReports: function(accountid) {
        
        Wtf.MessageBox.show({
            title : WtfGlobal.getLocaleText('acc.dashboard.bankReconciliation'),
            msg : WtfGlobal.getLocaleText("acc.reportbuilder.deletereportmsg") + "</br></br><b>" + WtfGlobal.getLocaleText('acc.customerList.delTT1') + "</b>",
            width : 520,
            closable : false,
            fn : function(btnText){
                if(btnText){
                    if(btnText == "yes"){
                        Wtf.Ajax.requestEx({
                            url:"ACCReconciliation/deleteBankReconcilationDrafts.do",
                            params:{
                                accountid : accountid
                            }
                        }, this,function (response) {
                            if (response.success) {
                                this.reportList.store.reload();
                                if(Wtf.getCmp("reconciliationledger")){
                                    Wtf.getCmp("reconciliationledger").getDraftInfoForAllAccounts();
                                }
                            } 
                        });
                    }
                }
            }.createDelegate(this),
            buttons: Wtf.MessageBox.YESNO,
            animEl: 'mb9',
            icon: Wtf.MessageBox.INFO
        });
        
    }
});
