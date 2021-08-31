/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */


Wtf.account.openingBalanceWindowForAccount = function(config){
    
    this.shouldBushinessPersonGridUpdate = false;
    Wtf.apply(this,{
        buttons:[this.closeButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
            minWidth: 50,
            scope: this,
            handler: this.closeOpenBalanceWin.createDelegate(this)
        })]
    },config);
    Wtf.account.openingBalanceWindowForAccount.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.openingBalanceWindowForAccount, Wtf.Window,{
    onRender:function(config){
        Wtf.account.openingBalanceWindowForAccount.superclass.onRender.call(this,config);
        
        //create account information form
        this.createAccountInformationForm();
        //create Forms Tab Panel.
        this.createFormsTabPanel();
        // load customer Information
        this.setAccountInfo();
        
        //adding region
        
        this.add(this.northPanel= new Wtf.Panel({
            region:'north',
            bodyStyle:'background:#f1f1f1;border-bottom:1px solid #bfbfbf;',
            height:100,
            border:false,
            items:[this.accountInformationForm]
        }),this.centerPanel = new Wtf.Panel({
            region:'center',
            layout:'fit',
            border:false,
            items:[this.formsTabPanel]
        })
        );
    },
    
    setAccountInfo:function(){
        if(this.accRec!=null && this.accRec!=undefined){
            this.accountCode.setValue(this.accRec.get('acccode'));
            this.accountName.setValue(this.accRec.get('accname'));
        }
    },
    
    createAccountInformationForm:function(){
        this.accountInformationForm = new Wtf.form.FormPanel({
            border:false,
            autoWidth:true,
            height:100,
            bodyStyle:'margin:40px 10px 10px 30px',
            //            labelWidth:100,
            items:[
            {
                layout:'column',
                border:false,
                items:[{
                    columnWidth:'.45',
                    layout:'form',
                    border:false,
                    items:[
                    this.accountCode = new Wtf.form.TextField({
                        fieldLabel:WtfGlobal.getLocaleText("acc.je.acc")+' '+WtfGlobal.getLocaleText("acc.field.Code"),
                        readOnly:true,
                        name:'accountCodeInOpenBalance',
                        width:150
                    })
                    ]
                },{
                    columnWidth:'.45',
                    layout:'form',
                    border:false,
                    items:[
                    this.accountName = new Wtf.form.TextField({
                        fieldLabel:WtfGlobal.getLocaleText("acc.je.acc")+' '+WtfGlobal.getLocaleText("acc.customerList.gridName"),
                        name:'accountNameInOpenBalance',
                        readOnly:true,
                        width:150
                    })
                    ]
                }]
            }
            ]
        });
    },
    
    createFormsTabPanel:function(){
        this.createTabs();
        this.formsTabPanel = new Wtf.TabPanel({
            autoScroll: true,
            bodyStyle: {
                background:"#DFE8F6 none repeat scroll 0 0"
            },
            border:false,
            items:[this.paymentTab]
        });
        this.formsTabPanel.setActiveTab(this.paymentTab);
        this.formsTabPanel. doLayout();
    },
    
    createTabs:function(){
        
        this.paymentTab = new Wtf.account.openingBalancePanel({
            title:WtfGlobal.getLocaleText("acc.field.OpeningBalances"),
            accountId:this.accRec.get('accid'),
            border:false
        });
    },
    
    
    closeOpenBalanceWin:function(){
        this.close();
    }
    
});


Wtf.account.openingBalancePanel = function(config){
    this.isAccount = true;
    this.accountId = config.accountId;
    
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
        {name:'salesPerson'},
        {name:'drawnOn'},
        {name:'exchangeRateForOtherCurrency'},
        {name:'chequeNumber'},
        {name:'chequeId'},
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
        {name:'paymentStatus', type:'boolean'},
        {name:'clearanceDate',type:'date'},
        {name:'expirydate',type:'date'},
        {name:'transactiontype'}
    ]);
    
    // Reader for grid store
    
    this.gridStoreReader = new Wtf.data.KwlJsonReader({
        root: "data",
        totalProperty:'count'
    },this.gridRec);
    
    //Store creation for transaction grid
    
    this.gridStore = new Wtf.data.Store({
        url:"ACCVendorPaymentNew/getAccountOpeningBalanceTransaction.do",
        baseParams:{
            custVenId:this.accountId,
            isForOpeningTransactionTab : true
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
    
    this.gridStore.load();
    //Selection Model for Grid
    
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect:true
    });
    
    // Column model creation for transaction grid
    
    this.colModel = new Wtf.grid.ColumnModel([this.sm,
    {
        header:WtfGlobal.getLocaleText("acc.reval.transaction"),
        dataIndex:'transactionNo',
        align:'center'
    },{
        header:WtfGlobal.getLocaleText("acc.field.TransactionType"),
        dataIndex:'transactiontype',
        align:'center'
    },{
        header:WtfGlobal.getLocaleText('acc.mp.pmtstatus'),
        dataIndex:'paymentStatus',
        align:'center',
        renderer: function(val){
            if(val){
                return 'Cleared';
            }else{
                return 'Uncleared';
            }
        }
    },{
        header:WtfGlobal.getLocaleText("acc.reval.amount"),
        dataIndex:'transactionAmount',
        align:'right',
        renderer:WtfGlobal.currencyRendererSymbol
    },{
        header:WtfGlobal.getLocaleText("acc.field.TransactionDate"),
        dataIndex:'transactionDate',
        align:'center',
        renderer:WtfGlobal.onlyDateDeletedRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.het.303"),
        dataIndex:'memo',
        align:'left'
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
        hidden:this.isOrder,
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
    

    
    buttonArr.push(this.createNewButton,this.editButton,this.deleteButton);
    
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
                plugins: this.pP = new Wtf.common.pPageSize({
                    id: "pPageSize_" + this.id
                    })
            })
        }
        ]
    });
    
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[this.transactionGridContainerPanel]
    },config);
    Wtf.account.openingBalancePanel.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.openingBalancePanel,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.openingBalancePanel.superclass.onRender.call(this,config);
    },
    
    createNewHandler:function(){
        this.transactionForm = new Wtf.account.TransactionOpeningBalanceForm({
            title:WtfGlobal.getLocaleText("acc.account.AddUnclearedDepositWithdraw"),
            layout:'border',
            id:'createTransactionFormId',
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            resizable:false,
            isCustomer:false,
            isInvoice:false,
            isOrder:false,
            isPayment:false,
            isCreditNote:false,
            isDebitNote:false,
            isAccount:true,
            accountId:this.accountId,
            height:600,
            width:1000,
            modal:true
        });
        this.transactionForm.on('datasaved',this.reloadStore,this)
        this.transactionForm.show();
    },
    
    reloadStore:function(){
        this.gridStore.reload({
            params:{
                start:0,
                limit:30
            }
        });
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
    this.transactionForm = new Wtf.account.TransactionOpeningBalanceForm({
        title:WtfGlobal.getLocaleText("acc.account.EditUnclearedDepositWithdraw"),
        layout:'border',
        id:'editTransaction'+this.id,
        record:record,
        iconCls :getButtonIconCls(Wtf.etype.deskera),
        resizable:false,
        isCustomer:false,
        isInvoice:false,
        isOrder:false,
        isPayment:false,
        isCreditNote:false,
        isDebitNote:false,
        isAccount:true,
        accountId:this.accountId,
        height:600,
        width:1000,
        modal:true,
        isEdit:true
    });
    this.transactionForm.on('datasaved',this.reloadStore,this)
    this.transactionForm.show();
},

deleteHandler:function(){
    
    var selectedRecordArray = this.transactionGrid.getSelectionModel().getSelections();
    if(selectedRecordArray.length<=0){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
        return;
    }

    var record = "";
    if(selectedRecordArray.length == 1){
        record = selectedRecordArray[0];
    }
    if(record.data.paymentStatus){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.account.OpeningBalanceType.deleteCleared")],0);
        return;
    }

    var billidArray = [];
    var invoicenoArray= [];
    var chequeArray= [];

    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.rem.238"),function(btn){
        if(btn =="yes") {
            for(var count=0;count<selectedRecordArray.length;count++)
            {
                billidArray.push(selectedRecordArray[count].get('transactionId'));
                invoicenoArray.push(selectedRecordArray[count].get('transactionNo'));
                chequeArray.push(selectedRecordArray[count].get('chequeId'));
            }

            WtfGlobal.setAjaxTimeOut();
            this.loadMask.show();
        
            Wtf.Ajax.requestEx({
                url:"ACCVendorPaymentNew/deleteAccountOpeningBalanceTransaction.do",
                params:{
                    billidArray:billidArray,
                    invoicenoArray:invoicenoArray,
                    chequeArray:chequeArray
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
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