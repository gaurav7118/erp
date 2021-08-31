Wtf.account.openingBalanceTransactionListPanel = function(config){
    
    this.isCustomer = config.isCustomer;
    
    this.isInvoice = (config.isInvoice!=null ||config.isInvoice!=undefined)?config.isInvoice:false;  
    // Excise Opening Balance check from Vendor Master ERP-27108 : to hide JE POST
    this.isExciseInvoice = (config.isExciseInvoice!=null ||config.isExciseInvoice!=undefined)?config.isExciseInvoice:false;  
    this.isPayment = (config.isPayment!=null ||config.isPayment!=undefined)?config.isPayment:false;  
    this.isCreditNote = (config.isCreditNote!=null ||config.isCreditNote!=undefined)?config.isCreditNote:false; 
    this.isDebitNote = (config.isDebitNote!=null ||config.isDebitNote!=undefined)?config.isDebitNote:false; 
    this.isOrder = (config.isOrder!=null ||config.isOrder!=undefined)?config.isOrder:false; 
    this.accountId = config.accountId;
    this.isActivateFlag = (config.isActivateFlag != null || config.isActivateFlag != undefined) ? config.isActivateFlag : true;
    
    //Record creation for grid
    
    this.gridRec = new Wtf.data.Record.create([
        {name:'transactionId'},
        {name:'transactionNo'},
        {name:'transactionAmount'},
        {name:'isNormalTransaction'},
        {name:'transactionAmountDue'},
        {name:'transactionAmountInBase'},
        {name:'currencysymbol'},
        {name:'transactionDate',type:'date'},
        {name:'porefno'},
        {name:'currencyid'},
        {name:'termdays'},
        {name:'termid'},
        {name:'taxAmount'},
        {name:'excludingGstAmount'},
        {name:'salesPerson'},
        {name:'drawnOn'},
        {name:'exchangeRateForOtherCurrency'},
        {name:'chequeNumber'},
        {name:'narration'},
        {name:'isCurrencyToBaseExchangeRate'},
        {name:'dueDate',type:'date'},
        {name:'chequeDate',type:'date'},
        {name:'poRefDate',type:'date'},
        {name:'isPaymentLinked'},
        {name:'isWrittenOff'},
        {name:'isRecovered'},
        {name:'isreval'},
        {name:'isNoteLinkedToAdvancePayment'}, // Whether the CN/DN is linked to Advance MP/RP
        {name:'memo'},
        {name:'methodid'},
        {name:'detailtype'},
        {name:'refno'},
        {name:'refname'},
        {name:'refdetail'},
        {name:'refcardno'},
        {name:'paymentStatus'},
        {name:'clearanceDate',type:'date'},
        {name:'expirydate',type:'date'},
        {name:'isClaimedTransaction'},
        {name:'isLinkedInvoiceIsClaimed'}
        
    ]);
    
    // Reader for grid store
    
    this.gridStoreReader = new Wtf.data.KwlJsonReader({
        root: "data",
        totalProperty:'count'
    },this.gridRec);
    
    //Store creation for transaction grid
    if(this.isCustomer){
        if(this.isInvoice){
            this.gridStoreUrl = "ACCInvoiceCMN/getOpeningBalenceInvoices.do";
        }else if(this.isPayment){
            this.gridStoreUrl = "ACCReceipt/getOpeningBalanceReceipts.do";
        }else if(this.isCreditNote){
            this.gridStoreUrl = "ACCCreditNote/getOpeningBalanceCNs.do";
        }else if(this.isDebitNote){
            this.gridStoreUrl = "ACCDebitNote/getOpeningBalanceCustomerDNs.do";
        }else if(this.isOrder){
            this.gridStoreUrl = "ACCSalesOrderCMN/getOpeningBalanceSalesOrders.do";
        }
        else if(this.isExciseInvoice){ // Excise Opening Balance check from Vendor Master ERP-27108 : to hide JE POST
            this.gridStoreUrl = "ACCInvoiceCMN/getOpeningBalenceInvoices.do";
        }
    }else{
        if(this.isInvoice){
            this.gridStoreUrl = "ACCGoodsReceiptCMN/getOpeningBalenceInvoices.do";
        }else if(this.isPayment){
            this.gridStoreUrl = "ACCVendorPayment/getOpeningBalancePayments.do";
        }else if(this.isCreditNote){
            this.gridStoreUrl = "ACCCreditNote/getOpeningBalanceVendorCNs.do";
        }else if(this.isDebitNote){
            this.gridStoreUrl = "ACCDebitNote/getOpeningBalanceDNs.do";
        }else if(this.isOrder){
            this.gridStoreUrl = "ACCPurchaseOrderCMN/getOpeningBalancePurchaseOrders.do";
        }else if(this.isExciseInvoice){ // Excise Opening Balance check from Vendor Master ERP-27108 : to hide JE POST
            this.gridStoreUrl = "ACCGoodsReceiptCMN/getOpeningBalenceInvoices.do";
        }
    }
    
    this.gridStore = new Wtf.data.Store({
        url:this.gridStoreUrl,
        baseParams:{
            custVenId:this.accountId,
            isForOpeningTransactionTab : true,// Excise Opening Balance check from Vendor Master ERP-27108 : to hide JE POST
            isExciseInvoice:this.isExciseInvoice
        },
        reader:this.gridStoreReader
    });
    
    this.loadMask = new Wtf.LoadMask(document.body,{
                msg : WtfGlobal.getLocaleText("acc.msgbox.50")
    });
    
    this.gridStore.on('loadexception',function(){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.loadMask.hide();
    },this);
    
    this.gridStore.on('beforeload',function(){
        this.loadMask.show();
    },this);
    
    this.gridStore.on('load',function(){
        this.loadMask.hide();
    },this);
    
//    this.gridStore.load();
    
//    this.gridStore.load();
    
    //Selection Model for Grid
    
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect:false
    });
    
    // Column model creation for transaction grid
    
    this.colModel = new Wtf.grid.ColumnModel([this.sm,{
            header:WtfGlobal.getLocaleText("acc.reval.transaction"),
            dataIndex:'transactionNo',
            align:'center'
        },{
            header:WtfGlobal.getLocaleText("acc.reval.amount"),
            dataIndex:'transactionAmount',
            align:'center',
            renderer:WtfGlobal.currencyRendererSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.customerList.gridAmountDue"),
            dataIndex:'transactionAmountDue',
            align:'center',
            hidden:this.isOrder,
            renderer:WtfGlobal.currencyRendererSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.field.TransactionDate"),
            dataIndex:'transactionDate',
            align:'center',
            renderer:WtfGlobal.onlyDateDeletedRenderer
    }]);
    
    // Transaction Grid creation
    
    this.transactionGrid = new Wtf.grid.GridPanel({
        cm:this.colModel,
        store:this.gridStore,
        sm:this.sm,
        stripeRows :true,
        border:false,
        viewConfig:{
            emptyText:'<center>'+WtfGlobal.getLocaleText("acc.field.NoRecordToDisplay")+'</center>',
            forceFit:true
        }
        
    });
    
    this.transactionGrid.getSelectionModel().on('rowselect', this.rowSelectHandler,this);
    
    // Tbar Item Creation
    
    var buttonArr = [];
    this.createNewButton = new Wtf.Toolbar.Button({
         text:WtfGlobal.getLocaleText("acc.rem.138"),
         scope:this,
         tooltip:WtfGlobal.getLocaleText("acc.field.CreateNewTransaction"),
         iconCls :getButtonIconCls(Wtf.etype.add),
         handler:this.createNewHandler.createDelegate(this)
    });
    
    this.editButton = new Wtf.Toolbar.Button({
         text:WtfGlobal.getLocaleText("acc.common.edit"),
         scope:this,
         hidden:this.isOrder || this.isExciseInvoice,// Excise Opening Balance check from Vendor Master ERP-27108 : to hide Edit Button
         iconCls :getButtonIconCls(Wtf.etype.edit), 
         tooltip:WtfGlobal.getLocaleText("acc.field.EditTransaction"),
         handler:this.editHandler.createDelegate(this)
    });
    
    this.deleteButton = new Wtf.Toolbar.Button({
         text:WtfGlobal.getLocaleText("acc.common.delete"),
         scope:this,
         hidden:this.isOrder,
         tooltip:WtfGlobal.getLocaleText("acc.field.DeleteTransaction"),
         iconCls:getButtonIconCls(Wtf.etype.deletebutton),
         handler:this.deleteHandler.createDelegate(this)
    });
    
//    this.importButton = new Wtf.Toolbar.Button({
//         text:'Import',
//         iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
//         scope:this,
//         tooltip:'Import CSV file.',
//         handler:callOpeningBalanceTransactionsImportWin.createDelegate(this,[this.isInvoice,this.isPayment,this.isCreditNote,this.isDebitNote,true])
//    });
    
    
    buttonArr.push(this.createNewButton,this.editButton,this.deleteButton);
    /**
     * If Customer/Vendor is Deactivate then create button and edit button 
     * should be disable in opening balance transaction.
     */
    if(!this.isActivateFlag){
        this.createNewButton.hide();
        this.editButton.hide();
    }
    
    
    // creating container panel
    
    this.transactionGridContainerPanel = new Wtf.Panel({
        layout: 'border',
        border: false,
        items:[
            {
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.transactionGrid],
                tbar:buttonArr,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.gridStore,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
                })
            }
        ]
    });
    
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[this.transactionGridContainerPanel]
    },config);
    Wtf.account.openingBalanceTransactionListPanel.superclass.constructor.call(this,config);
    
    this.addEvents({
        'openingBalanceUpdated':true//event will be fire for updating flag to refresh customer/vendor grid.
    });
}

Wtf.extend(Wtf.account.openingBalanceTransactionListPanel,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.openingBalanceTransactionListPanel.superclass.onRender.call(this,config);
    },
    
    createNewHandler:function(){
        var title = "";
        if(this.isCustomer){
            title = this.isInvoice?WtfGlobal.getLocaleText("acc.field.CustomerInvoice"):(this.isPayment?WtfGlobal.getLocaleText("acc.receipt.1"):this.isCreditNote?WtfGlobal.getLocaleText("acc.dimension.module.4"):(this.isDebitNote?WtfGlobal.getLocaleText("acc.accPref.autoDN"):(this.isExciseInvoice?WtfGlobal.getLocaleText("acc.field.india.excise.invoice"):WtfGlobal.getLocaleText("acc.field.Order"))));
        }else{
            title = this.isInvoice?WtfGlobal.getLocaleText("acc.agedPay.venInv"):(this.isPayment?WtfGlobal.getLocaleText("acc.field.Payments"):this.isCreditNote?WtfGlobal.getLocaleText("acc.accPref.autoCN"):(this.isDebitNote?WtfGlobal.getLocaleText("acc.accPref.autoDN"):(this.isExciseInvoice?WtfGlobal.getLocaleText("acc.field.india.excise.invoice.purchase"):WtfGlobal.getLocaleText("acc.field.Order"))));
        }
        this.transactionForm = new Wtf.account.TransactionOpeningBalanceForm({
            title:title,
            layout:'border',
            id:'createTransactionFormId',
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            resizable:false,
            isCustomer:this.isCustomer,
            isInvoice:this.isInvoice,           
            isOrder:this.isOrder,
            isPayment:this.isPayment,
            isCreditNote:this.isCreditNote,
            isDebitNote:this.isDebitNote,
            isExciseInvoice:this.isExciseInvoice,            
            accountId:this.accountId,
            height:600,
            width:1000,
            modal:true
        });
        this.transactionForm.on('datasaved',this.reloadStore,this)
        this.transactionForm.show();
    },
    
    reloadStore:function(){
        this.gridStore.reload({params:{start:0,limit:30}});
        this.fireEvent('openingBalanceUpdated',this);
    },
    
    rowSelectHandler:function(){
        var rec = this.transactionGrid.getSelectionModel().getSelected();    
        if(rec && rec.get('isNormalTransaction')){
            this.editButton.disable();
        }else{
            this.editButton.enable();
        }
    },
    
    editHandler:function(){
        
        var selectedRecordArray = this.transactionGrid.getSelectionModel().getSelections();
        if(selectedRecordArray.length<=0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
            return;
        }
        if(selectedRecordArray.length>1){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.account.OpeningBalanceType.editMulti")],0);
            return;
        }
        var record = "";
        if(selectedRecordArray.length == 1){
            record = selectedRecordArray[0];
        }
        if(record.data.paymentStatus){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.account.OpeningBalanceType.editCleared")],0);
            return;
        }
        /*
         * Check if invoice is claimed as bad debt. If claimed, block the editing.
         */
        if(record.data.isClaimedTransaction){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invList.canNotEditClaimedOpeningInvoice")],0);
            return;
        }
        if(this.isPayment){
            if(record.data.isPaymentLinked!=undefined && record.data.isPaymentLinked){
               WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.msgbox.alreadyLinked")],2);
              return; 
            }
            if(record.data.isWrittenOff){    
               WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),(this.isCustomer?WtfGlobal.getLocaleText("acc.writeOff.receiptCanNotBeEdited"):WtfGlobal.getLocaleText("acc.writeOff.paymentCanNotBeEdited"))],2);
               return;
            }
        }
        if(this.isInvoice){
            if(record.data.isWrittenOff && !record.data.isRecovered){    
                   WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.writeOff.invoiceCanNotBeEdited")],2);
                   return;
             }
            if(this.checkIfInvoiceIsPaidFullyOrPartially(record)){    // check whether the invoice is paid fully/partially.
                   WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invList.canNotEditOpeningInvoice")],2);
                   return;
             }
        }
        if(this.isDebitNote && record.data['isNoteLinkedToAdvancePayment']){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.dn.linkedSoCanNotbeEdited")],2);
            return;
        }
        if(this.isCreditNote && record.data['isNoteLinkedToAdvancePayment']){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.cn.linkedSoCanNotbeEdited")],2);
            return;
        }
        
        /*
         *Applicable for Malaysian Country
         */
        if(this.isDebitNote && record.data['isLinkedInvoiceIsClaimed']){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.malaysiangst.blockDNEdit")],2);
            return;
        }
        if(this.isCreditNote && record.data['isLinkedInvoiceIsClaimed']){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.malaysiangst.blockCNEdit")],2);
            return;
        }
        var isreval=record.get('isreval');
        if(isreval!=undefined && isreval > 0){
             WtfComMsgBox(58,2);
               return; 
        }
        if(this.isCustomer){
          var title = 'Edit '+(this.isInvoice?WtfGlobal.getLocaleText("acc.field.CustomerInvoice"):(this.isPayment?WtfGlobal.getLocaleText("acc.receipt.1"):(this.isCreditNote?WtfGlobal.getLocaleText("acc.accPref.autoCN"):(this.isDebitNote?WtfGlobal.getLocaleText("acc.accPref.autoDN"):''))));
        } else{
          title = 'Edit '+(this.isInvoice?WtfGlobal.getLocaleText("acc.agedPay.venInv"):(this.isPayment?WtfGlobal.getLocaleText("acc.field.Payments"):(this.isCreditNote?WtfGlobal.getLocaleText("acc.accPref.autoCN"):(this.isDebitNote?WtfGlobal.getLocaleText("acc.accPref.autoDN"):'')))); 
        }
        this.transactionForm = new Wtf.account.TransactionOpeningBalanceForm({
            title:title,
            layout:'border',
            id:'editTransaction'+this.id,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            resizable:false,
            isCustomer:this.isCustomer,
            isInvoice:this.isInvoice,
            isPayment:this.isPayment,
            isCreditNote:this.isCreditNote,
            isDebitNote:this.isDebitNote,
            isEdit:true,
            record:record,
            accountId:this.accountId,
            height:600,
            width:1000,
            modal:true
        });
        this.transactionForm.on('datasaved',this.reloadStore,this)
        this.transactionForm.show();
    },
    checkIfInvoiceIsPaidFullyOrPartially : function(formrec){
        if(formrec.data.transactionAmount==formrec.data.transactionAmountDue){      // If amount of invoice == amount due then there is no payment/receipt/debit note/credit note is made against invoice
            return false;
        } else {
            return true;
        }
    },
deleteHandler:function(){
    
    var selectedRecordArray = this.transactionGrid.getSelectionModel().getSelections();
    if(selectedRecordArray.length<=0){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
        return;
    }
      var rec="";
      var data="";
      var failToDeleteDueToWriteOff=false;
    if(this.isInvoice && this.isCustomer){
        var invoicesNotDeletedDueToWriteOff='';
        for(var i=0;i<selectedRecordArray.length;i++){
            rec = selectedRecordArray[i];
            data = rec.data;
            if(data.isWrittenOff && !data.isRecovered){
                failToDeleteDueToWriteOff =true;
                invoicesNotDeletedDueToWriteOff+= data.transactionNo+",";
            }
        }
        if(failToDeleteDueToWriteOff && invoicesNotDeletedDueToWriteOff!=''){
            invoicesNotDeletedDueToWriteOff=invoicesNotDeletedDueToWriteOff.substring(0,invoicesNotDeletedDueToWriteOff.length-1);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.writeOff.invoices")+" "+invoicesNotDeletedDueToWriteOff+" "+WtfGlobal.getLocaleText("acc.writeOff.invoiceCanNotBeDeleted")], 2);
            return;
        }
    }
    if(this.isPayment && this.isCustomer){
        var paymentsNotDeletedDueToWriteOff='';
        for(var i=0;i<selectedRecordArray.length;i++){
            rec = selectedRecordArray[i];
            data = rec.data;
            if(data.isWrittenOff){
                failToDeleteDueToWriteOff =true;
                paymentsNotDeletedDueToWriteOff+= '<b>'+data.transactionNo+'</b>'+",";
            }
        }
        if(failToDeleteDueToWriteOff && paymentsNotDeletedDueToWriteOff!=''){
            paymentsNotDeletedDueToWriteOff=paymentsNotDeletedDueToWriteOff.substring(0,paymentsNotDeletedDueToWriteOff.length-1);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.writeOff.receipts")+" "+paymentsNotDeletedDueToWriteOff+" "+WtfGlobal.getLocaleText("acc.writeOff.receiptsCanNotBeDeleted")], 2);
            return;
        }
    }
    /*
     * Applicable for Malaysian Country
     */
    var failToDeleteCNDueToClaimedInvoices =false;
    if(this.isCreditNote){
        var cnNotDeletedDueToClaimedInvoices='';
        for(var i=0;i<selectedRecordArray.length;i++){
            rec = selectedRecordArray[i];
            data = rec.data;
            if(data.isLinkedInvoiceIsClaimed){
                failToDeleteCNDueToClaimedInvoices =true;
                cnNotDeletedDueToClaimedInvoices+= '<b>'+data.transactionNo+'</b>'+",";
            }
        }
        if(failToDeleteCNDueToClaimedInvoices && cnNotDeletedDueToClaimedInvoices!=''){
            cnNotDeletedDueToClaimedInvoices=cnNotDeletedDueToClaimedInvoices.substring(0,cnNotDeletedDueToClaimedInvoices.length-2);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.malaysiangst.blockCNDelete")+"</br>"+cnNotDeletedDueToClaimedInvoices], 2);
            return;
        }
    }
    var failToDeleteDNDueToClaimedInvoices =false;
    if(this.isDebitNote){
        var dnNotDeletedDueToClaimedInvoices='';
        for(var i=0;i<selectedRecordArray.length;i++){
            rec = selectedRecordArray[i];
            data = rec.data;
            if(data.isLinkedInvoiceIsClaimed){
                failToDeleteDNDueToClaimedInvoices =true;
                dnNotDeletedDueToClaimedInvoices+= '<b>'+data.transactionNo+'</b>'+",";
            }
        }
        if(failToDeleteDNDueToClaimedInvoices && dnNotDeletedDueToClaimedInvoices!=''){
            dnNotDeletedDueToClaimedInvoices=dnNotDeletedDueToClaimedInvoices.substring(0,dnNotDeletedDueToClaimedInvoices.length-2);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.malaysiangst.blockDNDelete")+"</br>"+dnNotDeletedDueToClaimedInvoices], 2);
            return;
        }
    }
//    var record = "";
//    if(selectedRecordArray.length == 1){
        var billidArray = [];
        var invoicenoArray= [];
    //}
    
    
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.rem.238"),function(btn){
        if(btn =="yes") {
            var deleteUrl = "";
            if(this.isCustomer){
                if(this.isInvoice){
                    deleteUrl = "ACCInvoiceCMN/deleteOpeningInvoicePermanent.do";
                }else if(this.isPayment){
                    deleteUrl = "ACCReceipt/deleteOpeningReceiptPermanent.do";
                }else if(this.isCreditNote){
                    deleteUrl = "ACCCreditNote/deleteOpeningCNPermanent.do";;
                }else if(this.isDebitNote){
                    deleteUrl = "ACCDebitNote/deleteOpeningDNPermanent.do";
                }else if(this.isExciseInvoice){ // Excise Opening Balance check from Vendor Master ERP-27108 : to hide JE POST
                    deleteUrl = "ACCInvoiceCMN/deleteOpeningInvoicePermanent.do";
                }
            }else{
                if(this.isInvoice){
                    deleteUrl = "ACCGoodsReceiptCMN/deleteOpeningBalanceGoodsReceiptPermanent.do";
                }else if(this.isPayment){
                    deleteUrl = "ACCVendorPayment/deleteOpeningPaymentPermanent.do";
                }else if(this.isCreditNote){
                    deleteUrl = "ACCCreditNote/deleteOpeningCNPermanent.do";
                }else if(this.isDebitNote){
                    deleteUrl = "ACCDebitNote/deleteOpeningDNPermanent.do";
                }else if(this.isExciseInvoice){ // Excise Opening Balance check from Vendor Master ERP-27108 : to hide JE POST
                    deleteUrl = "ACCGoodsReceiptCMN/deleteOpeningBalanceGoodsReceiptPermanent.do";
                }
            }
            for(var count=0;count<selectedRecordArray.length;count++)
                {
                    billidArray.push(selectedRecordArray[count].get('transactionId'));
                    invoicenoArray.push(selectedRecordArray[count].get('transactionNo'));
                }
        //    var billid = record.get('transactionId');
        //    var invoiceno=record.get('transactionNo');
        WtfGlobal.setAjaxTimeOut();
        this.loadMask.show();
        
            Wtf.Ajax.requestEx({
                url:deleteUrl,
                params:{
                    billidArray:billidArray,
                invoicenoArray:invoicenoArray,
                isExciseInvoice:this.isExciseInvoice
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
            var invoiceno=record.get('transactionNo');
        }
    },this);
    
},

genSuccessResponse:function(response){
    WtfGlobal.resetAjaxTimeOut();
    this.loadMask.hide();
    WtfComMsgBox([this.title,response.msg],response.success*2+1);
        
    if(response.success){
        this.reloadStore();
    }
},

genFailureResponse:function(response){
    WtfGlobal.resetAjaxTimeOut(); 
    this.loadMask.hide();
    var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
    if(response.msg)msg=response.msg;
    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
}

});
