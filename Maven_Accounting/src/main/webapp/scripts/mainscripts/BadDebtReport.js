Wtf.account.BadDebtReport=function(config){
    
    this.isClaim = (config.isClaim)?config.isClaim:false;
    this.isCustomer = (config.isCustomer)?config.isCustomer:false;
    
    this.gridRec = Wtf.data.Record.create ([
        {name:'invoiceid'},
        {name:'billid'},
        {name:'paymentid'},
        {name:'type'},
        {name:'transactionid'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'paymentno'},
        {name:'companyid'},
        {name:'companyname'},
        {name:'currencysymbol'},
        {name:'currencycode'},
        {name:'currencyid'},
        {name:'invoiceNumber'},
        {name:'date', type:'date'},
        {name:'personname'},
        {name:'amount'},
        {name:'taxAmount'},
        {name:'isPartiallyRecovered'}
    ]);
    
    // Reader for grid store
    
    this.gridStoreReader = new Wtf.data.KwlJsonReader({
        root: "data",
        totalProperty:'count'
    },this.gridRec);
    
    this.gridStoreUrl = "";
    if(this.isClaim && this.isCustomer){
        this.gridStoreUrl = "ACCInvoiceCMN/getClaimedBadDebtInvoices.do";
    }else if(!this.isClaim && this.isCustomer){
        this.gridStoreUrl = "ACCInvoiceCMN/getRecoveredBadDebtInvoices.do";
    }else if(this.isClaim && !this.isCustomer){
        this.gridStoreUrl = "ACCGoodsReceiptCMN/getClaimedBadDebtInvoices.do";
    }else if(!this.isClaim && !this.isCustomer){
        this.gridStoreUrl = "ACCGoodsReceiptCMN/getRecoveredBadDebtInvoices.do";
    }
    
    this.gridStore = new Wtf.data.Store({
        url:this.gridStoreUrl,
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
    
    
    
    this.gridStore.on('load',function(){
        this.loadMask.hide();
    },this);
    
    this.gridColumnModelArr=[];
    
    this.gridColumnModelArr.push(new Wtf.grid.RowNumberer(),{
        header:'Invoice Number',
        dataIndex:'invoiceNumber',
        width:150,
        pdfwidth:75,
//        sortable:this.RemoteSort,
        //renderer:(config.isQuotation||config.isOrder||config.consolidateFlag)?"":WtfGlobal.linkDeletedRenderer
        renderer:WtfGlobal.linkDeletedRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.field.TransactionID"),//ERP-28376 - Invoice Transaction ID
        dataIndex:'transactionid',
        width:150,
        hidden:!this.isClaim,
        pdfwidth:75,
        renderer:WtfGlobal.linkDeletedRenderer
    },{
        header:(this.isClaim)?"Claimed Journal Entry No":"Recovered Journal Entry No",
        dataIndex:'entryno',
//        sortable: this.RemoteSort,
        width:150,
        hidden:!this.isClaim,
        pdfwidth:75,
        renderer:WtfGlobal.linkDeletedRenderer
    },{
        header:(this.isCustomer)?WtfGlobal.getLocaleText("acc.het.371"):WtfGlobal.getLocaleText("acc.field.paymentNumber"),
        dataIndex:'paymentno',
//        sortable: this.RemoteSort,
        width:150,
        hidden:this.isClaim,
        pdfwidth:75,
        renderer:WtfGlobal.linkDeletedRenderer
    },{
        header:(this.isClaim)?"Claimed "+WtfGlobal.getLocaleText("acc.inventoryList.date"):"Recovered "+WtfGlobal.getLocaleText("acc.inventoryList.date"),
        dataIndex:'date',
        align:'center',
        width:150,
        pdfwidth:80,
        renderer:WtfGlobal.onlyDateDeletedRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),
        dataIndex:'currencycode',
        hidden:true,
        pdfwidth:85
    },{
        header:(this.isClaim)?"Claimed Amount":"Recovered Amount",
        dataIndex:'amount',
        align:'center',
        width:150,
        pdfwidth:80,
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
    },{
        header:(this.isClaim)?"Claimed GST":"Recovered GST",
        dataIndex:'taxAmount',
        align:'center',
        width:150,
        pdfwidth:80,
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
    });
    
    this.grid = new Wtf.grid.GridPanel({
        cm:new Wtf.grid.ColumnModel(this.gridColumnModelArr),
        store:this.gridStore,
        stripeRows :true,
        border:false,
        viewConfig:{
            emptyText:'<center>'+WtfGlobal.getLocaleText("acc.field.NoRecordToDisplay")+'</center>',
            forceFit:true
        }
    });
    
    this.grid.on('cellclick', this.onCellClick, this);
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleidarray: Wtf.MultiEntityReportsModuleIdArray.split(','),
        advSearch: false,
        parentPanelSearch: this,
        ignoreDefaultFields: true,
        isAvoidRedundent: true,
        isMultiEntity: true,
        hideRememberSerch:true
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    
    var buttonArr = [];
    
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate' + this.id,
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true)
    });
    
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate' + this.id,
        value:WtfGlobal.getDates(false)
    });
    this.startDate.on('change',function(field,newval,oldval){
        if(field.getValue()!='' && this.endDate.getValue()!=''){
            if(field.getValue().getTime()>this.endDate.getValue().getTime()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                field.setValue(oldval);                    
            }else {
                this.fetchHandler();
            }
        }
    },this);
        
    this.endDate.on('change',function(field,newval,oldval){
        if(field.getValue()!='' && this.startDate.getValue()!=''){
            if(field.getValue().getTime()<this.startDate.getValue().getTime()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
                field.setValue(oldval);
            }else{
                this.fetchHandler();
            }
        }
    },this);
    this.fetchBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),
        tooltip : WtfGlobal.getLocaleText("acc.invReport.fetchTT"),  
        //            id: 'submitRec' + this.id,
        scope: this,
        iconCls:'accountingbase fetch',
        disabled :false,
        handler:this.fetchHandler.createDelegate(this)
    });
    this.unclaimButton = new Wtf.Toolbar.Button({
            text:'Unclaim',
            scope:this,
            hidden:!this.isClaim,
            iconCls :getButtonIconCls(Wtf.etype.edit), 
            tooltip:'Unclaim the selected transactions',
            handler:this.unclaimHandler.createDelegate(this)
    });
        
    this.exporturlid = "";
    if(this.isClaim && this.isCustomer){
        this.exporturlid = Wtf.autoNum.salesBadDebtClaimReport;
    }else if(!this.isClaim && this.isCustomer){
        this.exporturlid = Wtf.autoNum.salesBadDebtRecoveredReport;
    }else if(this.isClaim && !this.isCustomer){
        this.exporturlid = Wtf.autoNum.purchaseBadDebtClaimReport;
    }else if(!this.isClaim && !this.isCustomer){
        this.exporturlid = Wtf.autoNum.purchaseBadDebtRecoveredReport;
    }
    
    var filename="";
      if(this.isCustomer){
        filename = (this.isClaim)?WtfGlobal.getLocaleText("acc.invoice.sales.claimed.invoices")+"_v1":WtfGlobal.getLocaleText("acc.invoice.sales.recovered.invoices")+"_v1";
    }else{
        filename = (this.isClaim)?WtfGlobal.getLocaleText("acc.invoice.purchase.claimed.invoices")+"_v1":WtfGlobal.getLocaleText("acc.invoice.purchase.recovered.invoices")+"_v1";
    }
    
    this.exportButton=new Wtf.exportButton({
        obj:this,
        isEntrylevel:false,
        filename:filename,
//        id:"exportReports"+this.id,   //+config.id,   //added this.id to avoid dislocation of Export button option i.e. CSV & PDF
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        disabled :false,
        scope : this,
        menuItem:{
            csv:true,
            pdf:true
        },
        get:this.exporturlid
    });
    
    this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details',
        disabled :false,
        label:(this.isClaim)?'Claimed Invoices':'Recovered Invoices',
        menuItem:{
            print:true
        },
        get:this.exporturlid
    });
    
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        hidden:false,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
        
    this.resetBttn.on('click',this.handleResetClick,this);
    
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.rem.5")+" Invoice Number",
        width: 150,
        id:"quickSearch"+this.id,
        field: 'invoiceNumber',
        Store:this.gridStore
    });
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), // "Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), // 'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton",
        hidden: !Wtf.account.companyAccountPref.isMultiEntity
    });
    
    buttonArr.push(this.quickPanelSearch,this.resetBttn,'-');
    buttonArr.push(WtfGlobal.getLocaleText("acc.common.from"));
    buttonArr.push(this.startDate);
    buttonArr.push(WtfGlobal.getLocaleText("acc.common.to"));
    buttonArr.push(this.endDate);
    buttonArr.push('-',this.fetchBttn);
    buttonArr.push('-',this.unclaimButton);
    buttonArr.push('-',this.exportButton);
    buttonArr.push('-',this.printButton);
    buttonArr.push('-',this.AdvanceSearchBtn);
    
    this.gridStore.on('beforeload',function(){
        this.loadMask.show();
        
        var currentBaseParams = this.gridStore.baseParams;
        currentBaseParams.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        currentBaseParams.enddate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
        
    },this);
    
    this.transactionGridContainerPanel = new Wtf.Panel({
        layout: 'border',
        border: false,
        items:[this.objsearchComponent,
        {
            region: 'center',
            layout: 'fit',
            border: false,
            items: [this.grid],
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
    
    this.gridStore.load();
    this.addEvents({
        'journalentry':true
    });
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[this.transactionGridContainerPanel]
    }, config);
    Wtf.account.BadDebtReport.superclass.constructor.call(this,config);
    
}

Wtf.extend(Wtf.account.BadDebtReport,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.BadDebtReport.superclass.onRender.call(this,config);
    },
    
    fetchHandler:function(){
        this.gridStore.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value,
                pagingFlag:true
            }
        });
    },
     onCellClick:function(g,i,j,e){  //
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="entryno"){
            var accid=this.gridStore.getAt(i).data['journalentryid'];
            this.fireEvent('journalentry',accid,true, null,null,null,null,this.startDate.getValue(),this.endDate.getValue());
        } else if ( header == "invoiceNumber" ) {
            var rec = this.gridStore.getAt(i);
            var type = rec.data.type;
            viewTransactionTemplate(type, rec);
        } else if(header == "paymentno" ){
            var rec = this.gridStore.getAt(i);
            var paymentid = rec.data.paymentid;
            var type = this.isCustomer ? 'Payment Received':'Payment Made';
            viewTransactionTemplate1(type, rec,false,paymentid);
        }
    },
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.fetchHandler();
            this.gridStore.on('load',this.storeloaded,this);
        }
    },
    
    storeloaded:function(store){
        this.quickPanelSearch.StorageChanged(store);
    },
    unclaimHandler:function(){
        
        var recArray=this.grid.getSelectionModel().getSelections();
        if(recArray.length == 0){
            WtfComMsgBox(['Information','Please select a record first.'],0);
            return;
        }
        
        
        var invoiceIds = '';
        var invalidInvoices = '';
        var partiallyRecoveredInvoices = '';
        
        for(var i=0; i<recArray.length; i++){
            if(!isFromActiveDateRange(recArray[i].get('date'))){
                invalidInvoices+='<b>'+recArray[i].get('invoiceNumber')+'</b>'+", ";
            }
            if(recArray[i].get('isPartiallyRecovered')){
                partiallyRecoveredInvoices+='<b>'+recArray[i].get('invoiceNumber')+'</b>'+", ";
            }
        }
        if(partiallyRecoveredInvoices != ''){
            partiallyRecoveredInvoices = partiallyRecoveredInvoices.substring(0, partiallyRecoveredInvoices.length-2);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.malaysiangst.blockInvoiceUnclaim")+'<br>'+partiallyRecoveredInvoices],2);
            return
        }
        if(invalidInvoices != ''){
            invalidInvoices = invalidInvoices.substring(0, invalidInvoices.length-2);
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.malaysiangst.cannotunclaim")+'<br>'+invalidInvoices],2);
            return
        }
        for(var i=0; i<recArray.length; i++){
            invoiceIds+=recArray[i].get('billid')+",";
        }
        
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"),"Do you want to unclaim selected invoice ?",function(btn){
            if(btn!="yes"){
                return;
            }
            invoiceIds = invoiceIds.substr(0, invoiceIds.length-1);
            Wtf.Ajax.timeout = 180000;
            Wtf.Ajax.requestEx({
                url:(this.isCustomer)?"ACCInvoice/unClaimBadDebtInvoices.do":"ACCGoodsReceipt/unClaimBadDebtInvoices.do",
                params: {
                    invoiceIds:invoiceIds
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
        
    },
    genSuccessResponse:function(response, request){
        if(response.success){
            WtfComMsgBox([WtfGlobal.getLocaleText('acc.common.success'),response.msg],response.success*2+1);
            this.gridStore.load();
        }else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],response.success*2+1);
        }
    },
    
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    configurAdvancedSearch: function () {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
    },
    filterStore: function (json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.gridStore.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            isFixedAsset: false,
            isLeaseFixedAsset: false,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.gridStore.load({
            params: {
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: this.pP.combo.value
            }
        });
    },
    clearStoreFilter: function () {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.gridStore.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.gridStore.load({
            params: {
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: this.pP.combo.value
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    }
});