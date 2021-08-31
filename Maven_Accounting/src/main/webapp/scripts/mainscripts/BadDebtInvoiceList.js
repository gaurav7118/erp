
Wtf.account.BadDebtInvoiceListPanel=function(config){
    
    this.isClaim = (config.isClaim)?config.isClaim:false;
    this.isCustomer = (config.isCustomer)?config.isCustomer:false;
    
    //Record creation for grid
    
    this.gridRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'companyid'},
        {name:'companyname'},
        {name:'discount'},
        {name:'currencysymbol'},
        {name:'currencycode'},
        {name:'orderamount'},
        {name:'isexpenseinv'},
        {name:'currencyid'},
        {name:'shipto'},
        {name:'mode'},
        {name:'billno'},
        {name:'date', type:'date'},
        {name:'duedate', type:'date'},
        {name:'shipdate', type:'date'},
        {name:'personname'},
        {name:'personemail'},
        {name:'personid'},
        {name:'shipping'},
        {name:'othercharges'},
        {name:'partialinv',type:'boolean'},
        {name:'includeprotax',type:'boolean'},
        {name:'amount'},
        {name:'amountdue'},
        {name:'amountdueinbase'},
        {name:'termdays'},
        {name:'termname'},
        {name:'incash',type:'boolean'},
        {name:'taxamount'},
        {name:'taxid'},
        {name:'orderamountwithTax'},
        {name:'taxincluded',type:'boolean'},
        {name:'taxname'},
        {name:'deleted'},
        {name:'termamount'},
        {name:'amountinbase'},
        {name:'memo'},
        {name:'createdby'},
        {name:'createdbyid'},
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
        {name:'NoOfpost'}, 
        {name:'NoOfRemainpost'},  
        {name:'templateid'},
        {name:'templatename'},
        {name:'startDate', type:'date'},
        {name:'nextDate', type:'date'},
        {name:'expireDate', type:'date'},
        {name:'repeateid'},
        {name:'status'},
        {name:'amountwithouttax'},
        {name:'amountwithouttaxinbase'},
        {name:'commission'},
        {name:'commissioninbase'},
        {name:'amountDueStatus'},
        {name:'salesPerson'},
        {name:'agent'},
        {name:'shipvia'},
        {name:'fob'},
        {name:'approvalstatus'},
        {name:'approvalstatusint', type:'int', defaultValue:-1},
        {name:'archieve', type:'int'},
        {name:'withoutinventory',type:'boolean'},
        {name:'isfavourite'},
        {name:'isCapitalGoodsAcquired'},
        {name:'othervendoremails'},
        {name:'termdetails'},
        {name:'approvestatuslevel'},// for requisition
        {name:'posttext'},
        {name:'isOpeningBalanceTransaction'},
        {name:'isNormalTransaction'},
        {name:'isreval'},
        {name:'islockQuantityflag'},
        {name:'isprinted'},
        {name:'validdate', type:'date'},
        {name:'cashtransaction',type:'boolean'},
	{name:'shiplengthval'},
        {name:'invoicetype'},
        {name:'landedInvoiceID'},
        {name:'landedInvoiceNumber'},
        {name:'termdays'},
        {name:'billingAddress'},
        {name:'billingCountry'},
        {name:'billingState'},
        {name:'billingPostal'},
        {name:'billingEmail'},
        {name:'billingFax'},
        {name:'billingMobile'},
        {name:'billingPhone'},
        {name:'billingContactPerson'},
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
        {name:'shippingRoute'},
        {name:'shippingAddressType'},
        {name:'sequenceformatid'},
        {name:'gstIncluded'},
        {name:'lasteditedby'},
        {name:'salespersonname'},
        {name:'isConsignment'},
        {name:'custWarehouse'},
        {name:'deliveryTime'},
        {name:'gstclaimableamount'},
        {name:'paidAfterClaimed'},
        {name:'gstToRecover'},
        {name:'claimedPeriod'},
        {name:'badDebtClaimedDate', type:'date'},
        {name:'agingDays'},
        {name:'type'},
        {name:'getFullShippingAddress'}
    ]);
    
    
    // Reader for grid store
    
    this.gridStoreReader = new Wtf.data.KwlJsonReader({
        root: "data",
        totalProperty:'count'
    },this.gridRec);
    
    if(this.isCustomer){
        this.gridStoreUrl = "ACCInvoiceCMN/getInvoicesMerged.do";
    }else{
        this.gridStoreUrl = "ACCGoodsReceiptCMN/getGoodsReceiptsMerged.do";
    }
    
    
    this.gridStore = new Wtf.data.Store({
        url:this.gridStoreUrl,
        baseParams:{
            isBadDebtInvoices:true,
            nondeleted:true
        },
        reader:this.gridStoreReader
    });
    
    
    this.loadMask = new Wtf.LoadMask(document.body,{
        msg : WtfGlobal.getLocaleText("acc.msgbox.50")
    });
    
    this.gridStore.on('loadexception',function(){
        WtfGlobal.resetAjaxTimeOut();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.loadMask.hide();
    },this);
    
    this.gridStore.on('beforeload',function(){
        WtfGlobal.setAjaxTimeOut();
        this.gridStore.baseParams.baddebttype=(this.isClaim)?0:1;
        if(this.isClaim){
            this.gridStore.baseParams.badDebtCalculationDate=WtfGlobal.convertToGenericDate(this.getBadDebtCalculationDate());
            this.gridStore.baseParams.badDebtCriteria=this.dateFilter.getValue();
        }else{
            this.gridStore.baseParams.badDebtCalculationFromDate=WtfGlobal.convertToGenericDate(this.getBadDebtCalculationFromToDate(true));
            this.gridStore.baseParams.badDebtCalculationToDate=WtfGlobal.convertToGenericDate(this.getBadDebtCalculationFromToDate(false));
        }
        
        this.gridStore.baseParams.selectedCriteriaDate=WtfGlobal.convertToGenericDate(this.selectDate.getValue());
        
        this.loadMask.show();
    },this);
    
    this.gridStore.on('load',function(){
        WtfGlobal.resetAjaxTimeOut();
        this.loadMask.hide();
    },this);
    
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect:false
    });
    
    this.expander = new Wtf.grid.RowExpander({});
    
    this.gridColumnModelArr=[];
    
    this.gridColumnModelArr.push(this.sm,{
        header:'Invoice Number',
        dataIndex:'billno',
        width:150,
        pdfwidth:75,
//        sortable:this.RemoteSort,
        //renderer:(config.isQuotation||config.isOrder||config.consolidateFlag)?"":WtfGlobal.linkDeletedRenderer
        renderer:WtfGlobal.linkDeletedRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.jeno"),  //"Journal Entry No",
        dataIndex:'entryno',
        hidden:!this.isClaim,
//        sortable: this.RemoteSort,
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.linkDeletedRenderer
    },{
        header:"Invoice "+WtfGlobal.getLocaleText("acc.inventoryList.date"),
        dataIndex:'date',
        align:'center',
        hidden:!this.isClaim,
        sortable: this.RemoteSort,
        width:150,
        pdfwidth:80,
        renderer:WtfGlobal.onlyDateDeletedRenderer
    },{
        header:WtfGlobal.getLocaleText("acc.invoice.aging.days"),//"Aging (Days)",
        dataIndex:'agingDays',
        align:'center',
        hidden:!this.isClaim,
        sortable: this.RemoteSort,
        width:100,
        pdfwidth:80,
        renderer:function(v){
            var val = "-";
            if(v){
                val = v+' Days';
            }
            return val;
        }
    },{
        header:WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),
        dataIndex:'currencycode',
        hidden:true,
        pdfwidth:85
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.totAmt"),  //"Total Amount",
        align:'right',
        dataIndex:'amount',
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
        hidden:false
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.totAmtHome") + " ("+WtfGlobal.getCurrencyName()+")",  //"Total Amount (In Home Currency)",
        align:'right',
//        hidden:this.isQuotation?false:this.isSalesCommissionStmt || this.isRFQ, //this.quotation?false:this.isOrder,
        dataIndex:'amountinbase',
        width:150,
        pdfwidth:75,
        hidecurrency : true,
        renderer:WtfGlobal.currencyDeletedRenderer            
    },{
        header:WtfGlobal.getLocaleText("acc.invoiceList.amtDue"),  //"Amount Due",
        dataIndex:'amountdue',
        align:'right',
//        hidden:this.isOrder||this.isQuotation||this.isSalesCommissionStmt ||this.isRFQ,
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol            
    },{
        header:"Amount Due (In Base Currency)"+ " ("+WtfGlobal.getCurrencyName()+")",
        dataIndex:'amountdueinbase',
        align:'right',
//        hidden:this.isOrder||this.isQuotation||this.isSalesCommissionStmt ||this.isRFQ,
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.currencyDeletedRenderer            
    },{
        header:(this.isClaim)?"GST Amount To Claim":"Claimed GST",
        dataIndex:'gstclaimableamount',
        align:'right',
//        hidden:this.isOrder||this.isQuotation||this.isSalesCommissionStmt ||this.isRFQ,
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.currencyDeletedRenderer  
    },{
        header:"Claimed Period",
        dataIndex:'claimedPeriod',
        align:'right',
        hidden:true,
        width:150,
        pdfwidth:75,
        renderer:this.getClaimedPeriod 
    },{
        header:WtfGlobal.getLocaleText("acc.invoice.claimed.date"),//"Claimed Date",
        dataIndex:'badDebtClaimedDate',
        align:'right',
        hidden:this.isClaim,
        width:100,
        pdfwidth:75,
        renderer:WtfGlobal.onlyDateDeletedRenderer
    },{
        header:"Paid After Claimed",
        dataIndex:'paidAfterClaimed',
        align:'right',
        hidden:this.isClaim,
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.currencyDeletedRenderer  
    },{
        header:"GST To Recover",
        dataIndex:'gstToRecover',
        align:'right',
        hidden:this.isClaim,
        width:150,
        pdfwidth:75,
        renderer:WtfGlobal.currencyDeletedRenderer
    });
    
    // Transaction Grid creation
    
    this.transactionGrid = new Wtf.grid.GridPanel({
        cm:new Wtf.grid.ColumnModel(this.gridColumnModelArr),
        store:this.gridStore,
        sm:this.sm,
        stripeRows :true,
        border:false,
        viewConfig:{
            emptyText:'<center>'+WtfGlobal.getLocaleText("acc.field.NoRecordToDisplay")+'</center>',
            forceFit:true
        }
        
    });
    this.transactionGrid.on('cellclick',this.onCellClick, this);
    
    this.monthStore = new Wtf.data.SimpleStore({
            fields: [{name:'monthid',type:'int'}, 'name'],
            data :[[0,'January'],[1,'February'],[2,'March'],[3,'April'],[4,'May'],[5,'June'],[6,'July'],[7,'August'],[8,'September'],[9,'October'],
                [10,'November'],[11,'December']]
        });
    
//    this.fmonth = new Wtf.form.ComboBox({
//            store: this.monthStore,
//            fieldLabel:'Period',
//            name:'monthid',
//            displayField:'name',
//            forceSelection: true,
//            anchor:'95%',
//            valueField:'monthid',
//            mode: 'local',
//            triggerAction: 'all',
//            selectOnFocus:true,
//            listeners:{
//                scope:this,
//                select: function(combo, record, index) {
////                    var y1=combo.getValue(); 
////                    this.byear.setValue(this.finanyear.getValue());
////                    this.bmonth.setValue(y1); 
////                    this.bdays.setValue(this.fdays.getValue());
////                    
////                    var fdate=new Date(this.finanyear.getValue(),this.fmonth.getValue(),this.fdays.getValue());
////                    var bdate=new Date(this.byear.getValue(),this.bmonth.getValue(),this.bdays.getValue());
////                    var originalbdate=new Date(this.byear.originalValue,this.bmonth.originalValue,this.bdays.originalValue);
////                    var originalfdate=new Date(this.finanyear.originalValue,this.fmonth.originalValue,this.fdays.originalValue);
////                    if(new Date(fdate)>new Date(originalbdate)){
////                        this.checkanytransaction();
////                        this.checkpreviousyearlock();
////                    }else if(new Date(fdate)<new Date(originalfdate)){
////                        this.checkpreviousyearlock();
////                        this.gridsetvalue();
////                    }else{
////                        this.checkpreviousyearlock();
////                        this.gridsetvalue();
////                    }
////                    this.fmonth.collapse();
//                }
//            }
//        });
//        
//        this.fmonth.setValue(new Date().getMonth());
        
        // 
        
        var currentTime = new Date();
        var now = currentTime.getFullYear()+1;
        var years = [];
        var y = 2000;
        while(y<=now+2){
            years.push([y]);
            y++;
        }
        this.storeThn = new Wtf.data.SimpleStore({
            fields: [ 'financialyears' ],        
            data: years
        });
        this.finanyear = new Wtf.form.ComboBox({
            store: this.storeThn,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
            name:'yearid',
            xtype: 'combo',
            displayField:'financialyears',
            anchor:'95%',
            valueField:'financialyears',
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true,
            listClass: 'x-combo-list-small',
            typeAhead: false,
            allowBlank: false,
            listeners:{
                scope:this,
                beforequery: function() {
                    this.finanyear.store.loadData(years);
                },
                select: function(combo, record, index) {
//                    var y1=combo.getValue(); 
//                    this.byear.setValue(y1);
//                    var fdate=new Date(this.finanyear.getValue(),this.fmonth.getValue(),this.fdays.getValue());
//                    var bdate=new Date(this.byear.getValue(),this.bmonth.getValue(),this.bdays.getValue());
//                    var originalbdate=new Date(this.byear.originalValue,this.bmonth.originalValue,this.bdays.originalValue);
//                    var originalfdate=new Date(this.finanyear.originalValue,this.fmonth.originalValue,this.fdays.originalValue);
//                    if(new Date(fdate)>new Date(originalfdate)){
//                        this.checkanytransaction();//if transaction is made after old financial year date.  
//                        this.checkpreviousyearlock();
//                    }else if(new Date(fdate)<new Date(originalfdate)){
//                        this.checkpreviousyearlock();
//                        this.gridsetvalue();
//                    }else{
//                        this.gridsetvalue();
//                    }
//                    this.finanyear.collapse();
                }
            }
        });
        
        this.finanyear.setValue(new Date(Wtf.account.companyAccountPref.firstfyfrom).getFullYear());
        
        
        this.claimButton = new Wtf.Toolbar.Button({
            text:'Claim',
            scope:this,
            hidden:!this.isClaim,
            iconCls :getButtonIconCls(Wtf.etype.edit), 
            tooltip:'Claim to selected transactions',
            handler:this.claimHandler.createDelegate(this)
        });
        
        this.recoverButton = new Wtf.Toolbar.Button({
            text:'Recover',
            scope:this,
            hidden:this.isClaim,
            iconCls :getButtonIconCls(Wtf.etype.edit), 
            tooltip:'Recover to selected transactions',
            handler:this.recoverHandler.createDelegate(this)
        });
        
        this.fetchBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.fetch"),
            tooltip : WtfGlobal.getLocaleText("acc.invReport.fetchTT"),  
//            id: 'submitRec' + this.id,
            scope: this,
            iconCls:'accountingbase fetch',
            disabled :false,
            handler:this.fetchHandler.createDelegate(this)
        });
        
        this.dateFilterStore = new Wtf.data.SimpleStore({
            fields: ['id', 'name'],
            data :[[0,WtfGlobal.getLocaleText("acc.agedPay.dueDate")],[1,WtfGlobal.getLocaleText("acc.agedPay.invoiceDate")]]
        });
        
        this.exportButton = new Wtf.exportButton({
        obj: this,
        isEntrylevel: false,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
        filename:filename,
        disabled: false,
        scope: this,
        menuItem: {
            pdf: true,
            csv: true

        },
        get: this.exporturlid
        });
    
        /*
         * ERP-29045 : Below function is for adding the months name along with year dynamically.
         */
        this.getMonthDataArrayAndStore();
        this.monthFilter = new Wtf.form.ComboBox({
            store: this.monthStore,
            name:'monthfilter',
            displayField:'name',
//            value:1,
            anchor:"50%",
            valueField:'monthid',
            mode: 'local',
            triggerAction: 'all'
        });
        this.dateFilter = new Wtf.form.ComboBox({
            store: this.dateFilterStore,
            name:'datefilter',
            displayField:'name',
            value:1,
            anchor:"50%",
            valueField:'id',
            mode: 'local',
            triggerAction: 'all'
        });
        
        this.hiddenDate = new Wtf.ExDateFieldQtip({
            fieldLabel:'',
            format:WtfGlobal.getOnlyDateFormat(),
            hideLabel:true,
            hidden:true,
            name: 'hiddenCurrentDate',
            value:Wtf.serverDate,
            width : 130
        });
        
        this.selectDate = new Wtf.ExDateFieldQtip({
            fieldLabel:'Select Date',
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'selectDate',
            value:Wtf.serverDate
        });
        
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
            value:Wtf.serverDate
        });
        
    this.exporturlid = "";
    this.grid = this.transactionGrid;
    if (this.isCustomer) {
        this.exporturlid = Wtf.autoNum.SalesBadDebtReleifAdjustment;
    } else {
        this.exporturlid = Wtf.autoNum.PurchaseBadDebtReleifAdjustment;
    }
    var filename="";
     if(this.isCustomer){
        filename = (this.isClaim)?WtfGlobal.getLocaleText("acc.invoice.sales.claimed.invoices")+"_v1":WtfGlobal.getLocaleText("acc.invoice.sales.recovered.invoices")+"_v1";
    }else{
        filename = (this.isClaim)?WtfGlobal.getLocaleText("acc.invoice.purchase.claimed.invoices")+"_v1":WtfGlobal.getLocaleText("acc.invoice.purchase.recovered.invoices")+"_v1";
    }
    
    
    
    this.printButton = new Wtf.exportButton({
        text: WtfGlobal.getLocaleText("acc.common.print"), //"Print",
        obj: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.printTT"), //'Print report details',
        filename:(this.isCustomer)?WtfGlobal.getLocaleText("acc.invoice.sales.claimed.invoices"): WtfGlobal.getLocaleText("acc.invoice.purchase.claimed.invoices"),
        disabled: false,
        label: (this.isClaim) ? 'Claimed Invoices' : 'Recovered Invoices',
        menuItem: {
            print: true
        },     
        get: this.exporturlid
    });
        
    
    var buttonArr = [];
    
    
    if(this.isClaim){
        if(Wtf.account.companyAccountPref.badDebtProcessingPeriodType == Wtf.BadDebtProcessingType.Months){
            buttonArr.push('Select Month ',this.monthFilter);
        } else {
            buttonArr.push('Select Date ',this.selectDate);
            buttonArr.push(WtfGlobal.getLocaleText("acc.field.On"),'-',this.dateFilter);
        }    
    }else{
        buttonArr.push('Select Date ',this.endDate);
    }
    buttonArr.push('-',this.fetchBttn);
    buttonArr.push('-',this.claimButton);
    buttonArr.push('-',this.recoverButton);
    buttonArr.push(this.hiddenDate);
    buttonArr.push(this.exportButton);
    buttonArr.push(this.printButton);
    
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
    
    
    this.addEvents({
        'baddebtClaimed':true,
        'baddebtRecovered':true,
        'journalentry':true
    });
    this.monthFilter.on('change',this.fetchHandler,this);
    this.selectDate.on('change',this.fetchHandler,this);
    this.dateFilter.on('change',this.fetchHandler,this);
    
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[this.transactionGridContainerPanel]
    }, config);
    Wtf.account.BadDebtInvoiceListPanel.superclass.constructor.call(this,config);
     
    
}

Wtf.extend(Wtf.account.BadDebtInvoiceListPanel,Wtf.Panel,{
    onRender:function(config){
        Wtf.account.BadDebtInvoiceListPanel.superclass.onRender.call(this,config);
    },
    
    claimHandler:function(){
        
        var recArray=this.transactionGrid.getSelectionModel().getSelections();
        if(recArray.length == 0){
            WtfComMsgBox(['Information','Please select a record first.'],0);
            return;
        }
        
        if(Wtf.account.companyAccountPref.gstaccountforbaddebt == null || Wtf.account.companyAccountPref.gstaccountforbaddebt == undefined || Wtf.account.companyAccountPref.gstaccountforbaddebt == ""){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.gst.bad.debt.accounts.set")], 3);
            return;
        }
        
        if(Wtf.account.companyAccountPref.gstbaddebtreleifaccount == null || Wtf.account.companyAccountPref.gstbaddebtreleifaccount == undefined || Wtf.account.companyAccountPref.gstbaddebtreleifaccount == ""){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.gst.bad.debt.accounts.set")], 3);
            return;
        }
        
        if(Wtf.account.companyAccountPref.gstbaddebtrecoveraccount == null || Wtf.account.companyAccountPref.gstbaddebtrecoveraccount == undefined || Wtf.account.companyAccountPref.gstbaddebtrecoveraccount == ""){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.gst.bad.debt.accounts.set")], 3);
            return;
        }
        
        if(Wtf.account.companyAccountPref.gstbaddebtsuspenseaccount == null || Wtf.account.companyAccountPref.gstbaddebtsuspenseaccount == undefined || Wtf.account.companyAccountPref.gstbaddebtsuspenseaccount == ""){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.gst.bad.debt.accounts.set")], 3);
            return;
        }
        
        //
        
        var invoiceIds = '';
        
        for(var i=0; i<recArray.length; i++){
            invoiceIds+=recArray[i].get('billid')+",";
        }
        
        
        var arr="";
        var recStr = "";
        
        for(var i=0; i<recArray.length; i++){
            recStr += '{\"billId\":\"'+recArray[i].get('billid')+'\",\"paidAmtAfterClaimed\":'+recArray[i].get('paidAfterClaimed')+',\"gstToRecover":'+recArray[i].get('gstclaimableamount')+'},';
        }
        
        recStr = recStr.substr(0, recStr.length-1);
        arr = '['+recStr+']';
        
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"),"Do you want to claim?",function(btn){
            if(btn!="yes"){
                return;
            }
            invoiceIds = invoiceIds.substr(0, invoiceIds.length-1);
            Wtf.Ajax.timeout = 180000;
            Wtf.Ajax.requestEx({
                url:(this.isCustomer)?"ACCInvoice/claimBadDebtInvoices.do":"ACCGoodsReceipt/claimBadDebtInvoices.do",
                params: {
//                    billids:invoiceIds,
                    invoiceData:arr,
                    claimedDate:this.getClaimedDate(), // Logic of getting the claimable invoies
                    recoveredDate:WtfGlobal.convertToGenericDate(this.hiddenDate.getValue()),// to remove timezone problem
                    claimedPeriod:this.selectDate.getValue().getMonth()//this.fmonth.getValue()
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
        
    },
    
    genSuccessResponse:function(response, request){
        Wtf.Ajax.timeout = 30000;
        if(response.success){
            WtfComMsgBox([this.title,response.msg],response.success*2+1);
            if(this.isClaim){
                this.fireEvent('baddebtClaimed',this);
            }else{
                this.fireEvent('baddebtRecovered',this);
            }
            this.close();
        }else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],response.success*2+1);
        }
    },
    
    genFailureResponse:function(response){
        Wtf.Ajax.timeout = 30000;
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    getClaimedPeriod:function(value,m,rec){
        if(value == 0){
            return 'January';
        } else if(value == 1){
            return 'February';
        } else if(value == 2){
            return 'March';
        } else if(value == 3){
            return 'April';
        } else if(value == 4){
            return 'May';
        } else if(value == 5){
            return 'June';
        } else if(value == 6){
            return 'July';
        } else if(value == 7){
            return 'August';
        } else if(value == 8){
            return 'September';
        } else if(value == 9){
            return 'October';
        } else if(value == 10){
            return 'November';
        } else if(value == 11){
            return 'December';
        }
    },
    
    recoverHandler:function(){
        var recArray=this.transactionGrid.getSelectionModel().getSelections();
        if(recArray.length == 0){
            WtfComMsgBox(['Information','Please select a record first.'],0);
            return;
        }
        
        if(Wtf.account.companyAccountPref.gstaccountforbaddebt == null || Wtf.account.companyAccountPref.gstaccountforbaddebt == undefined || Wtf.account.companyAccountPref.gstaccountforbaddebt == ""){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.gst.bad.debt.accounts.set")], 3);
            return;
        }
        
        if(Wtf.account.companyAccountPref.gstbaddebtreleifaccount == null || Wtf.account.companyAccountPref.gstbaddebtreleifaccount == undefined || Wtf.account.companyAccountPref.gstbaddebtreleifaccount == ""){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.gst.bad.debt.accounts.set")], 3);
            return;
        }
        
        if(Wtf.account.companyAccountPref.gstbaddebtrecoveraccount == null || Wtf.account.companyAccountPref.gstbaddebtrecoveraccount == undefined || Wtf.account.companyAccountPref.gstbaddebtrecoveraccount == ""){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.gst.bad.debt.accounts.set")], 3);
            return;
        }
        
        if(Wtf.account.companyAccountPref.gstbaddebtsuspenseaccount == null || Wtf.account.companyAccountPref.gstbaddebtsuspenseaccount == undefined || Wtf.account.companyAccountPref.gstbaddebtsuspenseaccount == ""){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.gst.bad.debt.accounts.set")], 3);
            return;
        }
        
        // creating json array for selected invoices
        
        var arr="";
        var recStr = "";
        
        for(var i=0; i<recArray.length; i++){
            recStr += '{\"billId\":\"'+recArray[i].get('billid')+'\",\"paidAmtAfterClaimed\":'+recArray[i].get('paidAfterClaimed')+',\"gstToRecover":'+recArray[i].get('gstToRecover')+'},';
        }
        
        recStr = recStr.substr(0, recStr.length-1);
        arr = '['+recStr+']';
        
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"),"Do you want to recover?",function(btn){
            if(btn!="yes"){
                return;
            }
            Wtf.Ajax.timeout = 180000;
            Wtf.Ajax.requestEx({
                url:(this.isCustomer)?"ACCInvoice/recoverBadDebtInvoices.do":"ACCGoodsReceipt/recoverBadDebtInvoices.do",
                params: {
                    invoiceData:arr,
                    claimedDate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),// to remove timezone problem
                    recoveredDate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),// to remove timezone problem
                    claimedPeriod:this.endDate.getValue().getMonth()//this.fmonth.getValue()
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
    },
    
    fetchHandler:function(){
        this.gridStore.load();
        this.setParameterForExport();
    },
    
    getBadDebtCalculationDate:function(){
        var date='';
        var badDebtPeriod = Wtf.account.companyAccountPref.badDebtProcessingPeriod;
        if(Wtf.account.companyAccountPref.badDebtProcessingPeriodType == Wtf.BadDebtProcessingType.Months){  // Bad debt period type = Month
            date = new Date(this.globalDateArray[this.monthFilter.getValue()]);
            date = new Date(date.setFullYear(date.getFullYear(), date.getMonth(), 1));
            date = date.add(Date.MONTH, -badDebtPeriod);
        } else {                                     // Bad debt period type = Days
            date = this.selectDate.getValue();
            date = date.add(Date.DAY, -badDebtPeriod);
        }
        return date;
    },
    
    getBadDebtCalculationFromToDate:function(isFromDate){
        
        var date = '';
        
        if(isFromDate){
            date = this.getFinancialYrDate();//this.startDate.getValue();//new Date(this.finanyear.getValue(),this.fmonth.getValue(),1);
        }else{
            date = this.endDate.getValue();//new Date(this.finanyear.getValue(),this.fmonth.getValue()+1,0);
        }
        
        return date;
    },
    onCellClick:function(g,i,j,e){  //
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="entryno"){
            var accid=this.gridStore.getAt(i).data['journalentryid'];
            this.fireEvent('journalentry',accid,true, null,null,null,null,this.getFinancialYrDate(),this.endDate.getValue());
        } else if ( header=="billno" ) {
            var formRec = this.gridStore.getAt(i);
            var type = formRec.data.type;
            viewTransactionTemplate(type, formRec);
        }
    },
    getFinancialYrDate:function(){
        var date = WtfGlobal.getDates(true);
        return date;
    },
    setParameterForExport:function(){
        this.exportButton.setParams({
            isBadDebtInvoices: true,
            baddebttype: (this.isClaim) ? 0 : 1,
            badDebtCalculationDate: WtfGlobal.convertToGenericDate(this.getBadDebtCalculationDate()),
            badDebtCriteria: this.dateFilter.getValue(),
            badDebtCalculationFromDate: WtfGlobal.convertToGenericDate(this.getBadDebtCalculationFromToDate(true)),
            badDebtCalculationToDate: WtfGlobal.convertToGenericDate(this.getBadDebtCalculationFromToDate(false)),
            selectedCriteriaDate: WtfGlobal.convertToGenericDate(this.selectDate.getValue())
        });   
    },
    
    getMonthDataArrayAndStore:function(){
        var date = new Date();
        this.globalDateArray=[];
        //        var arrayToReturn=[];
        //        var temp = new Date(date.setMonth(date.getMonth()-1));
        //        this.globalDateArray.push(temp);
        //        arrayToReturn.push([0,monthNames[temp.getMonth()]+' '+temp.getFullYear()]);    // Previous Month
        //        date = new Date();
        //        temp = new Date(date.setMonth(date.getMonth()));
        //        this.globalDateArray.push(temp);
        //        arrayToReturn.push([1,monthNames[temp.getMonth()]+' '+temp.getFullYear()]);      // Current Month
        //        date = new Date();
        //        temp = new Date(date.setMonth(date.getMonth()+1));
        //        this.globalDateArray.push(temp);
        //        arrayToReturn.push([2,monthNames[temp.getMonth()]+' '+temp.getFullYear()]);       // Next Month
        
        /*
         * Request for getting latest date of form 03 generation
         */
        if(Wtf.account.companyAccountPref.bbfrom){                    
            Wtf.Ajax.requestEx({
                url:"AccGST/getLatestDateOfFileGeneration.do",
                params: {}
            },this,
            function(response,request){
                var arrayToReturn=[];
                var monthNames = ["January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
                ];
                if(response && response.success){
                    var today = new Date();
                    var todayMonth = today.getMonth();
                    var todayYear = today.getFullYear();
            
                    var lastMonthAllowedInCombo = todayMonth;
                    var lastYearAllowedInCombo = todayYear;
                    
                    /*
                     * Last month to be inserted in combobox will be the next month of current month.
                     * If current month is December, next month will be January of next year
                     */
                    if(todayMonth==11){
                        lastMonthAllowedInCombo = 0; // January
                        lastYearAllowedInCombo++; // Next Year
                    } else {
                        lastMonthAllowedInCombo++; // Next month
                    }
            
                    var bbDate = Wtf.account.companyAccountPref.bbfrom; // Book Begining date
                    var monthDateStr = bbDate.format('M d');
                    var bookBeginingDate = new Date(monthDateStr + ', ' + bbDate.getFullYear() + ' 12:00:00 AM');
                    var endDate = '';
                    var maxDate = '';
                    var maxDateMonth = '';
                    var maxDateYear = '';
                    var endDateMonth = 0;
                    /*
                     * System will add all months in drop-down from Book beginning date / last GST submission date (whichever is greater) to next month of current month
                     * If no GST is submitted yet, month of book begining date will be the first record else, next month of last file generation month will be the first record in array.
                     * e.g. -
                     * If book begining date is 01/01/15 and last GST is submitted for 01/04/2016 to 30/04/2016 and todays date is 09/11/2016 then combo from month will have records from May 2016 to Dec 2016.
                     *
                     */
                    if(response.enddate && response.enddate != undefined && response.enddate != '' && response.enddate != null){
                        endDate = new Date(response.enddate);
                        maxDate = bookBeginingDate.getTime()>endDate.getTime()?bookBeginingDate:new Date(endDate.add(Date.MONTH,1));
                    }else{
                        maxDate = bookBeginingDate;     
                    }
                    
                    //
                    maxDateYear = maxDate.getFullYear();
                    maxDateMonth = maxDate.getMonth();
                    var count = 0;
                    
                    /*
                     * Below mentioned condition is-
                     * Only those months will be added into drop down which lies either in same year as last month allowed in drop down or 
                     * which lies in previous year of year that is allowed in drop down
                     */
                    while(lastYearAllowedInCombo > maxDateYear || ( lastYearAllowedInCombo == maxDateYear && lastMonthAllowedInCombo >= maxDateMonth)){
                        var temp = new Date(date.setMonth(maxDateMonth));
                        temp = new Date(date.setFullYear(maxDateYear));
                        this.globalDateArray.push(temp);
                        date = new Date();
                        arrayToReturn.push([count,monthNames[maxDateMonth]+' '+maxDateYear]);
                        if(maxDateMonth==11){
                            maxDateMonth = 0;
                            maxDateYear++;
                        } else {
                            maxDateMonth++;
                        }
                        count++;
                    }
                    this.monthDataArray = arrayToReturn;                    
                }
                this.monthStore.removeAll();
                this.monthStore.loadData(this.monthDataArray);
                /*
                 *If only 1 month is there in drop down (i.e. next month of calendar) then that will be set by default
                 * Otherwise current month will be set by default.
                 */
                this.monthFilter.setValue(this.monthStore.data.length==1?this.monthStore.data.length-1:this.monthStore.data.length-2);
                this.setParameterForExport();
                this.gridStore.load();
            },      
            this.genFailureResponse);
        } 
    },
    getClaimedDate:function(){
        /*
         * If bad debt period criteria is Days, claimed date = date set into date picker
         * If bad debt period criteria is Months, claimed date = last date of month set into month filter
         */
        if(Wtf.account.companyAccountPref.badDebtProcessingPeriodType == Wtf.BadDebtProcessingType.Days){
            return WtfGlobal.convertToGenericDate(this.selectDate.getValue());
        } else {
            var date = this.globalDateArray[this.monthFilter.getValue()];
            date = date.getLastDateOfMonth();
            return WtfGlobal.convertToGenericDate(date);
        }    
    }
})