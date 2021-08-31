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
Wtf.account.ImportBankReconciliation=function(config){
this.clearingBalance=0;
this.openingBalance=config.openingBalance;
this.accid=config.accid;
this.startdate=config.startdate;
this.enddate=config.enddate;
this.response=config.res;
this.ruleStartDate="";
this.ruleEndDate="";
this.ruleMinAmount="";
this.ruleMaxAmount="";
this.currencysymbol="";
this.totalAmountToMatch=0;
this.finalTransactionIDs = [];
this.isBankImport = config.isBankImport;    //Used when we import From Bank for Bank reconciliation ERP-41871
this.bankBookBalanceinAcc = 0;  //Balnce as per Bank Book in Account Currency.
/**
 * Balnce as per Bank Statement in Account Currency this value is calculated on java side formula is (bankBookBalanceinAcc)
 * bankStmtBalanceinAcc = bankBookBalanceinAcc + unclearedChecksAmountinAcc - unclearedDepositsAmountinAcc;
 */
this.bankStmtBalanceinAcc = 0;      
    
this.gridBbar=[];

this.TotalTextValue_acc = new Wtf.Toolbar.TextItem(""); // Amount in Account Currency
this.gridBbar.push('->',"<B>" +WtfGlobal.getLocaleText("acc.bankReconcile.import.AmountinAccountCurrency")+ " : </B>",this.TotalTextValue_acc);
this.TotalTextValue_acc.getEl().innerHTML ="<B>" + WtfGlobal.conventInDecimal(0,WtfGlobal.getCurrencySymbol()) +"</B>"; // Setting Default value as 0

this.gridBbar.push("-");

this.TotalTextValue = new Wtf.Toolbar.TextItem(""); // Amount in Base Currency
this.gridBbar.push('->',"<B>" +WtfGlobal.getLocaleText("acc.bankReconcile.import.AmountinBaseCurrency")+ " : </B>",this.TotalTextValue);
this.TotalTextValue.getEl().innerHTML ="<B>" + WtfGlobal.conventInDecimal(0,WtfGlobal.getCurrencySymbol()) +"</B>"; // Setting Default value as 0


this.gridBbarForRuleInfo=[];

    this.importRecordGridRec = Wtf.data.Record.create ([
        {name:'date',type:'date'},
        {name:'amount'},
        {name:'payee'},
        {name:'desc'},
        {name:'reference'},
        {name:'chequenumber'}
    ]);
    
    this.importRecordStore = new Wtf.data.Store({
        url: Wtf.req.account+'CompanyManager.jsp',
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:'totalcount',
            root: "data"
        },this.importRecordGridRec),
        sortInfo: {field:'date',direction:"DESC"}
    });
    
    this.importRecordStore.loadData(this.response);
    
    this.importRecordGridSM=new Wtf.grid.CheckboxSelectionModel({singleSelect:true});

    this.importRecordGrid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.importRecordStore,
        border:true,
        viewConfig: {
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        forceFit:true,
        loadMask : true,
        sm:this.importRecordGridSM,
        scrollable:true,
        height:270,
        style:'padding:8px',
        tbar:['<b>'+(this.isBankImport ? WtfGlobal.getLocaleText("acc.bankReconcile.importfrombank.msg1") : WtfGlobal.getLocaleText("acc.bankReconcile.import.msg1")) + '</b>'],   //Imported Records from File : or Imported Records from Bank :
        columns:[this.importRecordGridSM,{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.Date"),  //"Date",
            dataIndex:'date',
            width:70,
            align:'center',
            sortable:true,
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.Amount"), //'Amount',  
            dataIndex:'amount',
            width:80,
            align:'right',
            sortable:true
        },{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.Payee"), //'Payee', 
            dataIndex:'payee',
            align:'left',
            sortable:true
        },{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.Description"),  //"Description",
            dataIndex:'desc',
            align:'left',
            sortable:true
        },{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.Reference"), //'Reference',
            dataIndex:'reference',
            align:'left',
            sortable:true
        },{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.ChequeNumber"),  //'Cheque Number',
            dataIndex:'chequenumber',
            align:'left',
            sortable:true
        }]
    });
    
    this.existingRecordGridRec = new Wtf.data.Record.create([
        {name: 'id'},
        {name: 'd_date',type:'date'},
        {name: 'd_accountname'},
        {name: 'd_entryno'},
        {name: 'd_journalentryid'},
        {name: 'd_amount'},
        {name: 'd_amountinacc'},
        {name: 'd_amountintransactioncurrency'},
        {name: 'd_reconciledate',type:'date'},
        {name: 'lastreconciledate',type:'date'},
        {name: 'type'},
        {name: 'billid'},
        {name:'moduleid'},
        {name:'isOpeningTransaction', type:'boolean'},
        {name: 'transactionID'},
        {name: 'chequeno'},
        {name: 'chequedate',type:'date'},
        {name: 'jeDate',type:'date'},
        {name: 'description'},
        {name: 'withoutinventory'},
        {name: 'paidto'},
        {name: 'currencysymbol'},
        {name: 'accountcurrencysymbol'},
        {name: 'isdebit'},
        {name: 'c_date',type:'date'},
        {name: 'c_amountinacc'},
        {name: 'c_amountintransactioncurrency'}
    
    ]);
    
    this.existingRecordStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.existingRecordGridRec),
        url:"ACCOtherReports/getMatchedReconciliationData.do",
        baseParams:{
            mode:67,
            accountid:this.accid
//            stdate:WtfGlobal.convertToGenericStartDate(this.startdate),
//            enddate:WtfGlobal.convertToGenericEndDate(this.enddate)
        }
    });
    
    this.existingRecordGridSM=new Wtf.grid.CheckboxSelectionModel({});
     
    this.existingGridCM= new Wtf.grid.ColumnModel([this.existingRecordGridSM,{
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridDate"),  //"Date",
        dataIndex: 'd_date',
        align:'center',
        width:70,
        sortable:true,
        renderer:WtfGlobal.onlyDateRenderer
    },{
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridAccountPayee"),  //"Account Payee",
        dataIndex: 'd_accountname',
        width:100,
        sortable:true
    },{
        header:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.bankReconcile.import.AmountinDocumentCurrency")+"'>"+WtfGlobal.getLocaleText("acc.bankReconcile.import.AmountinDocumentCurrency")+"</span>",
        align:'right',
        dataIndex: 'd_amountintransactioncurrency',
        renderer:WtfGlobal.currencyRendererSymbol,
        width:130,
        sortable:true      
   },{
        header:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.bankReconcile.import.AmountinAccountCurrency")+"'>"+WtfGlobal.getLocaleText("acc.bankReconcile.import.AmountinAccountCurrency")+"</span>",
        align:'right',
        dataIndex: 'd_amountinacc',
        width:130,
        summaryType:'sum',
        sortable:true,      
        renderer : WtfGlobal.withoutRateCurrencySymbolForAccountCurrency,
        summaryRenderer:this.accountCurrencySummaryRenderer.createDelegate(this)
   },{
        header:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.bankReconcile.import.AmountinBaseCurrency")+"'>"+WtfGlobal.getLocaleText("acc.bankReconcile.import.AmountinBaseCurrency")+"</span>",
        align:'right',
        dataIndex: 'd_amount',
        renderer:WtfGlobal.currencyRenderer,
        width:130,
        sortable:true,
        summaryType:'sum',
        summaryRenderer:WtfGlobal.currencySummaryRenderer
    },{    
        header: WtfGlobal.getLocaleText("acc.masterConfig.18"),  //"Memo",
        dataIndex:'paidto',
        sortable:true,
        width:60
    },
    {    
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridAccountchequeno"),  //"Cheque No",
        dataIndex: 'chequeno',
        sortable:true,
        width:100
    },{
        header: WtfGlobal.getLocaleText("payment.date.postDate"),  //"Cheque Date",
        dataIndex: 'chequedate',
        align:'center',
        width:100,
        sortable:true,
        renderer:WtfGlobal.onlyDateRenderer
    },{    
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridAccountdescription"),  //"Description",
        dataIndex: 'description',
        sortable:true,
        width:80
    },{  
        header: WtfGlobal.getLocaleText("acc.bankReconcile.gridJournalFolio"),  //"Journal Folio (J/F)",
        dataIndex: 'd_entryno',
        width:100,
        sortable:true,
        renderer:WtfGlobal.linkRenderer
    },{
            header:WtfGlobal.getLocaleText("acc.field.TransactionID"),
            dataIndex:'transactionID',
            width:80,
            sortable:true,
            renderer:function(value,meta,rec){

                meta.attr = "Wtf:qtip='" + value + "' Wtf:qtitle='Transaction ID' ";
                if(!value) return value;

                value = WtfGlobal.linkRenderer(value,meta,rec)

                return value;
            }
   }]);


    this.existingRecordGrid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store: this.existingRecordStore,
        height:270,
        autoWidth:true,
        style:'padding:8px',
        cm: this.existingGridCM,
        sm:this.existingRecordGridSM,
        scrollable:true,
        tbar:['<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.import.msg2")+'</b>'], //Matching Records from System :
        viewConfig: {
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        bbar:this.gridBbar
    });


    this.matchButton = document.createElement('img');
    this.matchButton.src = "../../images/MatchRecord.png";
    this.matchButton.style.cursor = "pointer";
    this.matchButton.style.width = "40px";
    this.matchButton.style.height = "48px";
    this.matchButton.onclick = this.matchButtonClicked.createDelegate(this, []);
    
    this.createButton = document.createElement('img');
    this.createButton.src = "../../images/CreateRecord.png";
    this.createButton.style.cursor = "pointer";
    this.createButton.style.width = "40px";
    this.createButton.style.height = "48px";
    this.createButton.onclick = this.createButtonClicked.createDelegate(this, []);
        
    this.matchButtonDiv = document.createElement("div");
    this.matchButtonDiv.appendChild(this.matchButton);
    this.matchButtonDiv.style.padding = "60px 0px 0px 0px";
    
    this.createButtonDiv = document.createElement("div");
    this.createButtonDiv.appendChild(this.createButton);
    this.createButtonDiv.style.padding = "38px 0px 0px 0px";
    
    this.addRuleButton= new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.bankReconcile.import.msg13"), //'Set Rules to Match Records.',			
        iconCls:getButtonIconCls(Wtf.etype.menuadd),
        scope: this,
        handler: this.addRule.createDelegate(this)
    });
    
    
    this.gridBbarForRuleInfo.push(this.addRuleButton);

    this.DateRangeTextValue = new Wtf.Toolbar.TextItem(""); // Date Range Value
    this.gridBbarForRuleInfo.push('->',"<B>" + WtfGlobal.getLocaleText("acc.bankReconcile.import.matchrule.selectedDateRange") + " : </B>",this.DateRangeTextValue);  // Selected Date Range :
    this.DateRangeTextValue.getEl().innerHTML ="From " + (WtfGlobal.getDates(true)).format(WtfGlobal.getOnlyDateFormat()) + " to " + (WtfGlobal.getDates(false)).format(WtfGlobal.getOnlyDateFormat()) +" "; // dd1/mm1/yyyy1 to dd2/mm2/yyyy2

    this.gridBbarForRuleInfo.push("-");

    this.AmountRangeTextValue = new Wtf.Toolbar.TextItem(""); // Amount Range value
    this.gridBbarForRuleInfo.push('->',"<B>" + WtfGlobal.getLocaleText("acc.bankReconcile.import.matchrule.selectedAmountRange") + " : </B>",this.AmountRangeTextValue); // Selected Amount Range :
    this.AmountRangeTextValue.getEl().innerHTML = WtfGlobal.getLocaleText("acc.bankReconcile.import.matchrule.equalToAmount"); // Amount of Selected Record.

    
    this.NorthForm=new Wtf.form.FormPanel({
        layout:'form',
        region : 'center',
        autoHeight:true,
        split:true,
        baseCls:'northFormFormat',
        disabledClass:"newtripcmbss",
        hideMode:'display',
        cls:"visibleDisabled",
        tbar:["<div style='font-size:12px;font-style:bold;margin:-10px 0px 0px 7px''>"+'<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.import.msg3")+'</b></div>'
                +"<div style='font-size:9px;margin:3px 0px -10px 8px'>"+WtfGlobal.getLocaleText("acc.bankReconcile.import.matchrule.info")+'</div>'], //Please match the Imported Records with Existing Records present in the system or Create new Records :
        bbar:this.gridBbarForRuleInfo,
        items:[{
            layout:'column',
            defaults:{border:false},
            items:[{
                layout:'form',
                columnWidth:0.46,
                items:[this.importRecordGrid]
           },{
                layout:'form',
                columnWidth:0.045,
                height:270,
                style:'padding:8px',
                items:[{
                        layout:'form',
                        heightWidth:0.5,
                        border: false,
                        items:[{
                                region: 'center',
                                border: false,
                                contentEl: this.matchButtonDiv
                            }]
                       },{
                        layout:'form',
                        heightWidth:0.5,
                        border: false,
                        items:[{
                                region: 'center',
                                border: false,
                                contentEl: this.createButtonDiv
                        }]
                    }]
            },{
                layout:'form',
                columnWidth:0.485,
                items:[this.existingRecordGrid]
            }]
        }]
    
    });
    
    this.finalStore = new Wtf.data.SimpleStore({
        fields: [{name:'date',type:'date'},
        {name:'amount'},
        {name:'payee'},
        {name:'desc'},
        {name:'reference'},
        {name:'chequenumber'},
        {name: 'id'},
        {name: 'd_date',type:'date'},
        {name: 'd_accountname'},
        {name: 'd_entryno'},
        {name: 'd_journalentryid'},
        {name: 'd_amount'},
        {name: 'c_accountname'},
        {name: 'c_entryno'},
        {name: 'c_journalentryid'},
        {name: 'c_amount'},
        {name: 'd_amountinacc'},
        {name: 'd_amountintransactioncurrency'},
        {name: 'd_reconciledate',type:'date'},
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
        {name:'accountcurrencysymbol'},
        {name: 'isdebit'},
        {name: 'c_date', type: 'date'},
        {name: 'c_amountinacc'},
        {name: 'c_amountintransactioncurrency'}]
    });
    /**
     * Getting the matchedRecord data from response JSON and converting it to array and setting it in finalStore as it 
     * is a simple store we cannot directly load JSON in it as no reader is defined.ERP-41871
     */
    if (this.response && this.response.matchedRecord && this.response.matchedRecord.data) {
        var recArr = [];
        for (var i = 0; i < this.response.matchedRecord.data.length; i++) {
            var recData = this.response.matchedRecord.data[i];
            recArr.push([recData.date, recData.amount, recData.payee, recData.desc, recData.reference, recData.chequenumber,
                'temp' + i, recData.d_date, recData.d_accountname, recData.d_entryno, recData.d_journalentryid,
                recData.d_amount, recData.c_accountname, recData.c_entryno, recData.c_journalentryid,
                recData.c_amount, recData.d_amountinacc, recData.d_amountintransactioncurrency, recData.d_reconciledate,
                recData.lastreconciledate, recData.type, recData.billid, recData.moduleid, recData.isOpeningTransaction,
                recData.transactionID, recData.chequeno, recData.chequedate, recData.jeDate, recData.description, recData.withoutinventory,
                recData.paidto, recData.currencysymbol, recData.accountcurrencysymbol, recData.isdebit, recData.c_date, recData.c_amountinacc, recData.c_amountintransactioncurrency]);
        }
        this.finalStore.loadData(recArr, true);
    }
    
    
    this.finalGrid = new Wtf.grid.GridPanel({
        stripeRows :true,
        autoScroll:true,
        height:295,
        store: this.finalStore,
        plugins: [new Wtf.GroupHeaderGrid({
            rows: [
                [
                 {align:"center",header:"<b>"+(this.isBankImport ? WtfGlobal.getLocaleText("acc.bankReconcile.importfrombank.msg4") : WtfGlobal.getLocaleText("acc.bankReconcile.import.msg4"))+"</b>",colspan:7},  //Records Imported from File
                 {align:"center",header:"<b>"+WtfGlobal.getLocaleText("acc.bankReconcile.import.msg5")+"</b>",colspan:11}]  //Records from System
            ]
        })],
        columns: [{
            header:'<b>'+WtfGlobal.getLocaleText("acc.invoice.gridAction"),
            align:'center',
            width:50,
            renderer: this.deleteRenderer.createDelegate(this)
        },{
            header:'<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.Date"),  //"Date",
            dataIndex:'date',
            width:70,
            align:'center',
            pdfwidth:120,
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header:'<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.Amount"), //'Amount',  
            dataIndex:'amount',
            width:80,
            pdfwidth:120,
            align:'right'
        },{
            header:'<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.Payee"), //'Payee', 
            dataIndex:'payee',
            pdfwidth:170,
            align:'left'
        },{
            header:'<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.Description"),  //"Description",
            dataIndex:'desc',
            pdfwidth:170,
            align:'left'
        },{
            header:'<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.Reference"), //'Reference',
            dataIndex:'reference',
            pdfwidth:170,
            align:'left'
        },{
            header:'<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.ChequeNumber"),  //'Cheque Number',
            dataIndex:'chequenumber',
            pdfwidth:170,
            align:'left'
        },{
            header: '<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.gridDate"),  //"Date",
            dataIndex: 'd_date',
            align:'center',
            width:70,
            pdfwidth:125,
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header: '<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.gridAccountPayee"),  //"Account Payee",
            dataIndex: 'd_accountname',
            pdfwidth:125
        },{
            header:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.bankReconcile.import.AmountinDocumentCurrency")+"'>"+'<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.import.AmountinDocumentCurrency")+"</b></span>",
            align:'right',
            dataIndex: 'd_amountintransactioncurrency',
            renderer:WtfGlobal.currencyRendererSymbol,
            width:130,
            pdfwidth:125
        },{
            header:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.bankReconcile.import.AmountinAccountCurrency")+"'>"+'<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.import.AmountinAccountCurrency")+"</b></span>",
            align:'right',
            dataIndex: 'd_amountinacc',
            width:130,
            summaryType:'sum',
            pdfwidth:125,
            renderer : WtfGlobal.withoutRateCurrencySymbolForAccountCurrency,
            summaryRenderer:this.accountCurrencySummaryRenderer.createDelegate(this)
        },{
            header:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.bankReconcile.import.AmountinBaseCurrency")+"'>"+'<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.import.AmountinBaseCurrency")+"</b></span>",
            align:'right',
            dataIndex: 'd_amount',
            renderer:WtfGlobal.currencyRenderer,
            width:130,
            summaryType:'sum',
            pdfwidth:125,
            summaryRenderer:WtfGlobal.currencySummaryRenderer
        },{    
            header: '<b>'+WtfGlobal.getLocaleText("acc.masterConfig.18"),  //"Memo",
            dataIndex:'paidto',
            pdfwidth:125,
            width:60
        },
        {    
            header: '<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.gridAccountchequeno"),  //"Cheque No",
            dataIndex: 'chequeno',
            pdfwidth:125,
            width:100
        },{
            header: '<b>'+WtfGlobal.getLocaleText("payment.date.postDate"),  //"Cheque Date",
            dataIndex: 'chequedate',
            align:'center',
            pdfwidth:125,
            width:100,
            renderer:WtfGlobal.onlyDateRenderer
        },{    
            header: '<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.gridAccountdescription"),  //"Description",
            dataIndex: 'description',
            pdfwidth:125,
            width:80
        },{  
            header: '<b>'+WtfGlobal.getLocaleText("acc.bankReconcile.gridJournalFolio"),  //"Journal Folio (J/F)",
            dataIndex: 'd_entryno',
            width:100,
            pdfwidth:125
        },{
            header: '<b>'+WtfGlobal.getLocaleText("acc.field.TransactionID"),
            dataIndex:'transactionID',
            width:80,
            pdfwidth:125
        }],
        border : false,
        viewConfig: {
            stripeRows: true,
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }
    });
    
    this.SouthForm=new Wtf.form.FormPanel({
        region : 'center',
        height:320,
        style:'padding:10px',
        border:true,
        layout:'form',
        hideMode:'display',
        cls:"visibleDisabled",
        labelWidth:120,
        tbar:['<br><b>  '+WtfGlobal.getLocaleText("acc.bankReconcile.import.msg6")+'</b><br>'], //Final Combine Records to be Reconciled :
        items:[this.finalGrid]
        
    });
    
    this.reconcileButton= new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.bankReconcile.reconcile&save"), //'Reconcile and Save',			
        iconCls:'pwnd save',
        scope: this,
        handler: this.saveData.createDelegate(this)
    });
        
    this.mainPanel = new Wtf.Panel({
        border: false,
        region: 'center',
        //id: 'centerpan', 
        layout: 'border',
        items:[{
                region: "center",
                autoScroll: true,
                border: false,
                style: 'padding:8px',
                items: [this.NorthForm, this.SouthForm]
            }],
        bbar:[this.reconcileButton]
    })
    
    Wtf.apply(this,{
        items:[{
            region:'center',
            layout:'fit',
            border:false,
            items:[this.mainPanel]
        }]
    },config);
    Wtf.account.ImportBankReconciliation.superclass.constructor.call(this,config);
    this.addEvents({
       'journalentry': true
    });
    this.importRecordGrid.getSelectionModel().on('rowselect',this.onImportRecordGridCellClick,this);
    this.existingRecordGrid.on('cellclick',this.onExistingRecordsGridCellClick, this);
    this.finalGrid.on('rowclick',this.handleRowClick,this);
    this.existingRecordStore.on('load',this.removeDuplicateRecords,this);
}
 Wtf.extend(Wtf.account.ImportBankReconciliation,Wtf.Panel,{
    
    onImportRecordGridCellClick:function(sm,rowIndex,record){

        var date=record.data['date'];
        var amount=record.data['amount'];
        var payee=record.data['payee'];

        var startdate="";var enddate="";var minamt="";var maxamt="";

            if(this.ruleStartDate!="" && this.ruleEndDate!=""){
                startdate=new Date(this.ruleStartDate);
                enddate=new Date(this.ruleEndDate);
            }else{
                startdate=date;
                enddate=date;
            }

            if(this.ruleMaxAmount !="" && this.ruleMaxAmount!=0){
                minamt=this.ruleMinAmount;
                maxamt=this.ruleMaxAmount;
            }else{
                minamt=amount;
                maxamt=amount;
            }

            var params={
                accountid:this.accid,
                stdate:WtfGlobal.convertToGenericStartDate(startdate),
                enddate:WtfGlobal.convertToGenericEndDate(enddate),
                minamount:minamt,
                maxamount:maxamt,
                payee:payee
            };

         Wtf.Ajax.requestEx({
            url:"ACCOtherReports/getMatchedReconciliationData.do",
            params:params
        }, this, this.successCallback, this.failureCallback);
                     
    },
    
     successCallback:function(response){
        WtfGlobal.resetAjaxTimeOut();
        if(response.success){
            this.currencysymbol=response.currencysymbol; 
            this.bankdatainacc=response.bankdatainacc;
            this.bankBookBalanceinAcc = response.bankdatainacc;    
            this.existingRecordGrid.store.loadData(response);
            this.doLayout();
            this.existingRecordGrid.getView().refresh(true);
        }
    },

    failureCallback:function(response){
        WtfGlobal.resetAjaxTimeOut();
    },
    
    updateTotalAmount: function () {
        var damount_acc = 0, damount = 0;
        var selectedArr = this.existingRecordGridSM.getSelections();
        if (selectedArr.length > 0) {
            for (var i = 0; i < selectedArr.length; i++) {
                var rec = selectedArr[i];
                if (!Wtf.isEmpty(rec.data['d_amountinacc'])) {
                    damount_acc += rec.data['d_amountinacc'];
                }
                if (!Wtf.isEmpty(rec.data['c_amountinacc'])) {
                    damount_acc += rec.data['c_amountinacc'];
                }
                if (!Wtf.isEmpty(rec.data['d_amount'])) {
                    damount += rec.data['d_amount'];
                }
                if (!Wtf.isEmpty(rec.data['c_amount'])) {
                    damount += rec.data['c_amount'];
                }
            }
        }
        if (this.currencysymbol != null || this.currencysymbol != undefined || this.currencysymbol != "") {
            this.TotalTextValue_acc.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(damount_acc, this.currencysymbol) + "</B>";
        } else{
            this.TotalTextValue_acc.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(damount_acc, WtfGlobal.getCurrencySymbol()) + "</B>";
        }
        this.TotalTextValue.getEl().innerHTML = "<B>" + WtfGlobal.conventInDecimal(damount, WtfGlobal.getCurrencySymbol()) + "</B>";
        this.totalAmountToMatch=damount_acc;
    },
    
    removeDuplicateRecords:function(){
        if (this.finalTransactionIDs.length > 0) {
            for (var j = 0; j < this.finalTransactionIDs.length; j++) {
                for (var i = 0; i < this.existingRecordStore.getCount(); i++) {
                    var rec=this.existingRecordStore.getAt(i);
                    var jeID = rec.data['d_journalentryid'];
                    if(jeID== this.finalTransactionIDs[j]){
                        this.existingRecordStore.remove(rec);
                    }
                }
            }
        }
    },
    
    addRule: function(){
        
    this.rulesStartDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from")+" Date",  //'From',
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
        value:(this.ruleStartDate==""? WtfGlobal.getDates(true):new Date(this.ruleStartDate)),
        labelWidth:150,
        width:150
    });
    
    this.rulesEndDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to")+" Date",  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        value:(this.ruleEndDate==""? WtfGlobal.getDates(false):new Date(this.ruleEndDate)),
        labelWidth:150,
        width:150
    });
    
    this.dateFieldSet = new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.bankReconcile.import.msg16"), //'Select the Date Range to Match the Records',
            autoHeight: true,
            border: false,
            cls: "import-Wiz-fieldset",
            items: [{
                layout:'column',
                border:false,
                defaults:{border:false},
                items:[{
                        layout:'form',
                        columnWidth:0.50,
                        items:[this.rulesStartDate]
                },{
                        layout:'form',
                        columnWidth:0.50,
                        items:[this.rulesEndDate]
                    }]
            }]
    });
    
    this.minAmount=new Wtf.form.NumberField({
        fieldLabel:'Minimum Amount',
        allowNegative:false,
        defaultValue:0,
        allowBlank:true,
        maxLength: 10,
        width:150,
        labelWidth:150,
        value:0,
        id:'minAmt',
        name:'minAmount'
    });
    
    this.maxAmount=new Wtf.form.NumberField({
        fieldLabel:'Maximum Amount',
        allowNegative:false,
        defaultValue:0,
        allowBlank:true,
        maxLength: 10,
        width:150,
        labelWidth:150,
        value:0,
        id:'maxAmt',
        name:'maxAmount'
    });
    
    this.minAmount.setValue(this.ruleMinAmount==""?0:this.ruleMinAmount);
    this.maxAmount.setValue(this.ruleMaxAmount==""?0:this.ruleMaxAmount);

    this.AmountFieldSet = new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.bankReconcile.import.msg17"), //'Select the Amount Range to Match the Records',
            autoHeight: true,
            border: false,
            cls: "import-Wiz-fieldset",
            items: [{
                layout:'column',
                border:false,
                defaults:{border:false},
                items:[{
                        layout:'form',
                        columnWidth:0.50,
                        items:[this.minAmount]
                },{
                        layout:'form',
                        columnWidth:0.50,
                        items:[this.maxAmount]
                    }]
            }]
        });

    this.saveRule= new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save'
            iconCls:'pwnd save',
            scope: this,            
            handler: function(){

                if(WtfGlobal.getDayMonthYearDate(this.rulesEndDate.getValue()) < WtfGlobal.getDayMonthYearDate(this.rulesStartDate.getValue())){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.bankReconcile.import.matchrule.validation1")],2);
                    return;
                }else if(this.maxAmount.getValue() < this.minAmount.getValue()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.bankReconcile.import.matchrule.validation2")],2);
                    return;
                }else{
                    this.ruleStartDate=WtfGlobal.convertToGenericStartDate(this.rulesStartDate.getValue()),
                    this.ruleEndDate=WtfGlobal.convertToGenericEndDate(this.rulesEndDate.getValue()),
                    this.ruleMinAmount=this.minAmount.getValue();
                    this.ruleMaxAmount=this.maxAmount.getValue();
                    this.setRuleWindow.close();
                    
                    this.DateRangeTextValue.getEl().innerHTML = (this.rulesStartDate.getValue()).format(WtfGlobal.getOnlyDateFormat()) +" to "+ (this.rulesEndDate.getValue()).format(WtfGlobal.getOnlyDateFormat()) + " ";
                    if(this.ruleMaxAmount !="" && this.ruleMaxAmount!=0){
                         if (this.currencysymbol != null || this.currencysymbol != undefined || this.currencysymbol != "") {
                            this.AmountRangeTextValue.getEl().innerHTML = "Between Min : " + WtfGlobal.conventInDecimal(this.ruleMinAmount, this.currencysymbol) + " and Max :" + WtfGlobal.conventInDecimal(this.ruleMaxAmount, this.currencysymbol) + " ";
                         }else{
                            this.AmountRangeTextValue.getEl().innerHTML = "Between Min : " + WtfGlobal.conventInDecimal(this.ruleMinAmount, WtfGlobal.getCurrencySymbol()) + " and Max :" + WtfGlobal.conventInDecimal(this.ruleMaxAmount, WtfGlobal.getCurrencySymbol()) + " ";
                         }
                    }else{
                        this.AmountRangeTextValue.getEl().innerHTML = WtfGlobal.getLocaleText("acc.bankReconcile.import.matchrule.equalToAmount");
                    }
                }
                this.importRecordGridSM.clearSelections();
                this.existingRecordGridSM.clearSelections();
                this.updateTotalAmount();
            }
    });
    
    this.rulePanel = new Wtf.form.FormPanel({                   
        border : false,
        bodyStyle: 'background:#f1f1f1;font-size:10px;padding:15px 15px 15px 15px;',
        style: "background: transparent;",
        items : [this.dateFieldSet,this.AmountFieldSet]
    });
    
    this.setRuleWindow = new Wtf.Window({
        modal: true,
        iconCls: 'accountingbase ledger',
        title: WtfGlobal.getLocaleText("acc.bankReconcile.import.msg13"), //'Set Rules to Match Records.',
        items: [{
                    region:'north',
                    height:80,
                    border : false,
                    bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf; padding:2px;',
                    html: getImportTopHtml( WtfGlobal.getLocaleText("acc.bankReconcile.import.msg13"),
                                            "<ul style='list-style-type:disc;padding-left:15px;'><li>"+
                                            WtfGlobal.getLocaleText("acc.bankReconcile.import.msg14")+"</li><li>"+
                                            WtfGlobal.getLocaleText("acc.bankReconcile.import.msg15")+"</li></ul>",
                                            "../../images/PurchaseMangement/Create-Debite-Note.png", true, "5px 0px 0px 15px", "7px 0px 0px 10px")
                },{
                    region:'center',
                    border:false,
                    bodyStyle: 'background:#f1f1f1;font-size:10px;',
                    items:[this.rulePanel]
                }],
        buttons:[this.saveRule,{
                    text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                    scope:this,
                    handler:function(){
                        this.setRuleWindow.close();
                    }
            }]
        });
        this.setRuleWindow.show();
    },
    
    onExistingRecordsGridCellClick:function(g,i,j,e){
        e.stopEvent();
        this.updateTotalAmount();
        var el=e.getTarget("a");
        if(el==null)return;
        var dataindex=g.getColumnModel().getDataIndex(j);
        if(dataindex == "transactionID"){
            var rec=this.existingRecordGrid.getStore().getAt(i);
            var type=rec.data['type'];
            var withoutinventory=rec.data['withoutinventory'];            
            viewTransactionTemplate1(type, rec,withoutinventory,rec.data.billid);
        } else if(dataindex == "d_entryno") {         
            var record=this.existingRecordGrid.getStore().getAt(i);
            var jid=record.data['d_journalentryid'];
            this.fireEvent('journalentry',jid,true);
        }
    },
    
    accountCurrencySummaryRenderer: function(value){
        if ( this.cmbAccount !=undefined && this.cmbAccount.getValue() == "") {
            return WtfGlobal.summaryRenderer(WtfGlobal.currencyRenderer(value));
        } else {
            return WtfGlobal.summaryRenderer(WtfGlobal.rateCurrencySymbolForAccountCurrency(value));
        }
    },
    
    matchButtonClicked: function(){
        var selectedFromImportRecordGrid = this.importRecordGridSM.getSelections();
        var selectedFromExistingRecordGrid = this.existingRecordGridSM.getSelections();
      
        var impRecArr = selectedFromImportRecordGrid[0];
        var extRecArr = selectedFromExistingRecordGrid[0];
        
        if (impRecArr == undefined || impRecArr.length < 1) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.bankReconcile.import.msg7")], 2); //Please select a record from file to Match.
            return;
        }else if (extRecArr == undefined || extRecArr.length < 1) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.bankReconcile.import.msg8")], 2);  //Please select a record from system to Match.
            return;
        }else if(impRecArr.data.amount!=this.totalAmountToMatch){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.bankReconcile.import.matchrule.validation3")],2);
            return;
        }else{
            var tempID = this.generateID();
            var finalArr = [];
            finalArr.push([impRecArr.data.date,impRecArr.data.amount,impRecArr.data.payee,impRecArr.data.desc,impRecArr.data.reference,impRecArr.data.chequenumber,
                            tempID, extRecArr.data.d_date, extRecArr.data.d_accountname, extRecArr.data.d_entryno, extRecArr.data.d_journalentryid,
                            extRecArr.data.d_amount, extRecArr.data.c_accountname, extRecArr.data.c_entryno, extRecArr.data.c_journalentryid,
                            extRecArr.data.c_amount, extRecArr.data.d_amountinacc, extRecArr.data.d_amountintransactioncurrency, extRecArr.data.d_reconciledate, 
                            extRecArr.data.lastreconciledate, extRecArr.data.type, extRecArr.data.billid, extRecArr.data.moduleid, extRecArr.data.isOpeningTransaction,
                            extRecArr.data.transactionID, extRecArr.data.chequeno, extRecArr.data.chequedate, extRecArr.data.jeDate, extRecArr.data.description, extRecArr.data.withoutinventory,
                            extRecArr.data.paidto, extRecArr.data.currencysymbol, extRecArr.data.accountcurrencysymbol,extRecArr.data.isdebit, extRecArr.data.c_date, extRecArr.data.c_amountinacc, extRecArr.data.c_amountintransactioncurrency]);
            this.finalTransactionIDs.push(extRecArr.data.d_journalentryid);
            if(selectedFromExistingRecordGrid.length > 1){
                for (var i = 1; i < selectedFromExistingRecordGrid.length; i++) {
                    var rec = selectedFromExistingRecordGrid[i];
                    finalArr.push(["","","","","","",
                            tempID, rec.data.d_date, rec.data.d_accountname, rec.data.d_entryno, rec.data.d_journalentryid,
                            rec.data.d_amount, rec.data.c_accountname, rec.data.c_entryno, rec.data.c_journalentryid,
                            rec.data.c_amount, rec.data.d_amountinacc, rec.data.d_amountintransactioncurrency, rec.data.d_reconciledate, 
                            rec.data.lastreconciledate, rec.data.type, rec.data.billid, rec.data.moduleid, rec.data.isOpeningTransaction,
                            rec.data.transactionID, rec.data.chequeno, rec.data.chequedate, rec.data.jeDate, rec.data.description, rec.data.withoutinventory,
                            rec.data.paidto, rec.data.currencysymbol, rec.data.accountcurrencysymbol, rec.data.isdebit, rec.data.c_date, rec.data.c_amountinacc, rec.data.c_amountintransactioncurrency]);
                    this.finalTransactionIDs.push(rec.data.d_journalentryid);
                    this.existingRecordStore.remove(rec);
                }
            }            
            this.finalStore.loadData(finalArr, true);
            this.updateTotalAmount();
            
            this.importRecordStore.remove(impRecArr);
            this.existingRecordStore.remove(extRecArr);
            
            this.importRecordGrid.getView().refresh();
            this.existingRecordGrid.getView().refresh();
            
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.bankReconcile.import.msg9")], 2+1);  //Record is matched successfully.
            if(this.reconcileButton){
               this.reconcileButton.enable();
            }
        }
    },
    
    S4 : function() {
        return (((1+Math.random())*0x10000)|0).toString(16).substring(1); 
    },
    
    generateID : function(){
        var guid = (this.S4() + this.S4() + "-" + this.S4() + "-4" + this.S4().substr(0,3) + "-" + this.S4() + "-" + this.S4() + this.S4() + this.S4()).toLowerCase();
        return guid;
    },
    
    deleteRenderer:function(v,m,rec){
        if(rec.data.date=="" && rec.data.amount =="" && rec.data.payee ==""){
            return "";
        }else{
            return "<div style='margin: auto;' class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
        }
    },
    
    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            var store=grid.getStore();
            var record = store.getAt(rowindex);
            var tempArr = [];
            var tempID = record.data.id;
            tempArr.push([record.data.date,record.data.amount,record.data.payee,record.data.desc,record.data.reference,record.data.chequenumber]);
            if(record.data.date!="" && record.data.amount !="" && record.data.payee!=""){
                var rec = new this.importRecordGridRec({
                    date:record.data.date,
                    amount:record.data.amount,
                    payee:record.data.payee,
                    desc:record.data.desc,
                    reference:record.data.reference,
                    chequenumber:record.data.chequenumber
                });
                this.importRecordStore.insert(0,rec);
            }
            this.finalStore.remove(record);
            this.existingRecordStore.removeAll();
            
            /*This code is written to remove the record from final grid map.*/
            if (this.finalTransactionIDs.length > 0) {
                for (var j = 0; j < this.finalTransactionIDs.length; j++) {
                    var jeID = record.data.d_journalentryid;
                    if(jeID== this.finalTransactionIDs[j]){
                        this.finalTransactionIDs.remove(jeID);
                    }
                }
            }
            
            var deletedRecArray = [];
            var deletedJEArray = [];
            var totalRecords = this.finalStore.getCount();   
            for (var i = 0; i < totalRecords; i++) {
                var rec1=this.finalStore.getAt(i);
                var jeID1 = rec1.data['d_journalentryid'];
                var tmpID = rec1.data['id'];
                if(tmpID== tempID){
                      deletedRecArray.push(rec1);
                }
                    
                if (this.finalTransactionIDs.length > 0) {
                    for (j = 0; j < this.finalTransactionIDs.length; j++) {
                        if(jeID1== this.finalTransactionIDs[j]){
                            deletedJEArray.push(jeID1);
                        }
                    }
                }
            }
            
            for (var k = 0; k < deletedRecArray.length; k++) {
                this.finalStore.remove(deletedRecArray[k]);
            }
            for (var l = 0; l < deletedJEArray.length; l++) {
                this.finalTransactionIDs.remove(deletedJEArray[l]);
            }
            
            this.importRecordGridSM.clearSelections();
            this.existingRecordGridSM.clearSelections();
            this.updateTotalAmount();

            this.importRecordGrid.getView().refresh();
            this.existingRecordGrid.getView().refresh();
            this.finalGrid.getView().refresh();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.bankReconcile.import.msg10")], 2+1); //Record is deleted successfully.
        }
    },
        
    createSeparateData:function(){
        var len=this.finalGrid.getStore().getCount();
        var store=this.finalGrid.getStore();
        for(var i=0;i<len;i++){
            var rec=store.getAt(i);
            if(rec.data['isdebit']==false){
                rec.set("c_accountname",rec.data['d_accountname']);
                rec.set("c_amount",rec.data['d_amount']);
                rec.set("c_journalentryid",rec.data['d_journalentryid']);
                rec.set("c_entryno",rec.data['d_entryno']);
            }
        }
    },
    
    getReconciliationDetails:function(isleft){
        var arr=[];
        var len=this.finalGrid.getStore().getCount();
        for(var i=0;i<len;i++){
            arr.push(i);
        }
        this.sortStore(isleft);
        var grid = this.finalGrid;
        return WtfGlobal.getJSONArray(grid,true,arr);

    },
    
    sortStore:function(isleft){
      this.finalGrid.getStore().filterBy(function(rec){
            if(rec.data.isdebit==isleft)
                return true
            else
                return false
        },this);  
    },
    
    saveData:function(){

        this.createSeparateData();
        
        var ldetails=this.getReconciliationDetails(true);
        this.finalGrid.getStore().clearFilter(true);
        
        var rdetails=this.getReconciliationDetails(false);
        this.finalGrid.getStore().clearFilter(true);
        
        this.finalGrid.getView().refresh();
        
        if(ldetails=="[]" && rdetails=="[]"){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.bankReconcile.msg1")], 2);
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
                                /**
                                 * Validation of Reconcilation Number in case of Import.
                                 */
                                if(!this.reconcileNumber.isValid()){
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.bankReconcileNumber.alert.msg7")],2);
                                    return;
                                }
                                /**
                                 * Checking clearance date with JE date of documnet if isPostingDateCheck() is true and clearanceDate is less than jeDate than show invalid message. 
                                 */
                                var invalidDate = false;
                                var invalidJEDate = false;
                                var invalidUnreconcilDate = false;
                                var jeIds = "";
                                if(ldetails!=[]){
                                    var templDetails = eval('('+ldetails+')');
                                    for(var i=0; i<templDetails.length; i++){   //ERP-9338 : Clearance date should be equal or greater than cheque date
                                        if(WtfGlobal.getDayMonthYearDate(this.clearanceDate.getValue()) < WtfGlobal.getDayMonthYearDate(new Date(templDetails[i].chequedate))){
                                            invalidDate = true;
                                            jeIds += templDetails[i].d_entryno +", "
                                        }else if (CompanyPreferenceChecks.isPostingDateCheck() && (WtfGlobal.getDayMonthYearDate(this.clearanceDate.getValue()) < WtfGlobal.getDayMonthYearDate(new Date(templDetails[i].jeDate)) )) {
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
                                        if(WtfGlobal.getDayMonthYearDate(this.clearanceDate.getValue()) < WtfGlobal.getDayMonthYearDate(new Date(templDetails[i].chequedate))){
                                            invalidDate = true;
                                            jeIds += templDetails[i].d_entryno +", "
                                        }else if (CompanyPreferenceChecks.isPostingDateCheck() && (WtfGlobal.getDayMonthYearDate(this.clearanceDate.getValue()) < WtfGlobal.getDayMonthYearDate(new Date(templDetails[i].jeDate)))) {
                                            invalidJEDate = true;
                                            jeIds += templDetails[i].d_entryno + ", "
                                        }else if(CompanyPreferenceChecks.isPostingDateCheck() &&  (this.isConcileReport && WtfGlobal.getDayMonthYearDate(this.clearanceDate.getValue()) < WtfGlobal.getDayMonthYearDate(new Date(templDetails[i].lastreconciledate)))){
                                            invalidUnreconcilDate = true;
                                            jeIds += templDetails[i].c_entryno + ", "
                                        }
                                    }
                                }

                                if(invalidDate){
                                    jeIds =jeIds.substring(0,jeIds.length-2);
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.ClearanceDatecannotbebeforeanyrecordsdateJEIds") + jeIds],2);
                                    return;
                                }
                                if (invalidJEDate) {
                                    jeIds = jeIds.substring(0, jeIds.length - 2);
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ClearanceDatecannotbebeforeanyJEPostingdateJEIds") + jeIds], 2);
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
                                    if(this.reconcileButton){
                                        this.reconcileButton.disable();
                                    }
                                    this.calculateClearingBalance();
                                    rec.d_details=ldetails;
                                    rec.c_details=rdetails;
                                    rec.mode=56;
                                    
                                    var startDate = new Wtf.form.DateField({
                                        value:this.startdate,
                                        format:WtfGlobal.getOnlyDateFormat()
                                    }).getValue();
                                    
                                    var endDate = new Wtf.form.DateField({
                                        value:this.enddate,
                                        format:WtfGlobal.getOnlyDateFormat()
                                    }).getValue();
                                    
                                    rec.startdate=WtfGlobal.convertToGenericStartDate(startDate);
                                    rec.enddate=WtfGlobal.convertToGenericEndDate(endDate);
//                                    rec.startdate=WtfGlobal.convertToGenericStartDate(new Date(this.startdate));
//                                    rec.enddate=WtfGlobal.convertToGenericEndDate(new Date(this.enddate));
                                    rec.clearanceDate=WtfGlobal.convertToGenericDate(this.clearanceDate.getValue());
                                    
                                    rec.reconcilenumber = this.reconcileNumber.getValue();
                                    rec.sequenceformat = this.sequenceFormatCombobox.getValue();
                                    rec.bankBookBalanceinAcc = this.bankBookBalanceinAcc;     //Balance As per Bank Book in Account Currency
                                    rec.bankStmtBalanceinAcc = this.bankStmtBalanceinAcc;   //Balance As per Bank Statement
                                    //
//                                    rec.endingbalance=this.reconRec.endingbalance;
                                    rec.clearingbalance=this.clearingBalance;
                                    rec.accid=this.accid;
                                    rec.isConcileReport=false;
                                    rec.docID=this.docID;
                                    rec.isImport=true;
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
                totalProperty: 'count',
                root: "data"
            }, this.sequenceFormatStoreRec),
            url: "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams: {
                mode: this.isConcileReport ? "autounreconcilenumber" : "autoreconcilenumber",
                isEdit: false
            }
        });
        this.sequenceFormatCombobox = new Wtf.form.ComboBox({//Sequence Format No.      
            triggerAction: 'all',
            mode: 'local',
            id: 'sequenceFormatCombobox' + this.heplmodeid + this.id,
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.Sequenceformat.tip") + "'>" + WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat") + "</span>",
            valueField: 'id',
            displayField: 'value',
            store: this.sequenceFormatStore,
            anchor: '70%',
            typeAhead: true,
            forceSelection: true,
            name: 'sequenceformat',
            hiddenName: 'sequenceformat',
            allowBlank: false
        });
        this.sequenceFormatStore.load();
        this.sequenceFormatStore.on('load', this.setSequenceFormat, this);
        this.sequenceFormatCombobox.on('select', this.getNextSequenceNumber, this);

        this.reconcileNumber = new Wtf.form.TextField({
            fieldLabel: this.isConcileReport ? WtfGlobal.getLocaleText("acc.bankReconcileNumber.msg8") : WtfGlobal.getLocaleText("acc.bankReconcileNumber.msg6") + "*", // Un-Reconcile / Reconcile No.*',
            name: 'refno',
            //hidden :true,        //this.isConcileReport? true : false,    //Do not show the button in case of View Reconcile Report
            allowBlank: false,
            width : 200,
            anchor: '70%'
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
                    style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
                    items : [this.clearanceDate=new Wtf.form.DateField({
                                name:"clearancedate",
                                width : 200,
                                fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.reconcileDate")+"*",
                                allowBlank:false,
                                format:WtfGlobal.getOnlyDateFormat()
                            }),
                    this.tName = new Wtf.form.TextField(
                    {
                        fieldLabel : WtfGlobal.getLocaleText("acc.invoiceList.attachDocuments"),
                        name : 'file',
                        inputType : 'file',
                        width : 200,
                        blankText:WtfGlobal.getLocaleText("acc.field.SelectFileFirst"),
                        msgTarget :'qtip'
                    }),this.sequenceFormatCombobox,this.reconcileNumber],
                    buttons : [{
                        anchor : '90%',
                        id : 'save',
                        text : WtfGlobal.getLocaleText("acc.invoiceList.bt.upload"),
                        scope : this,
                        handler : this.upfileHandler
                    }]
                
                });
            
        this.clearanceWindow = new Wtf.Window({
            modal: true,
            iconCls: 'accountingbase ledger',
            title: WtfGlobal.getLocaleText("acc.bankReconcile.reconcileDate"),//"Clearance Date",
            items: [this.fileuploadwin],
            buttons:[this.reconcileSave,{
                        text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                        scope:this,
                        handler:function(){
                            this.clearanceWindow.close();
                        }
               }]
        });
        this.clearanceWindow.show();
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
    calculateClearingBalance:function(){
        var total=0;
        var len=this.finalGrid.getStore().getCount();
        var store=this.finalGrid.getStore();
        for(var i=0;i<len;i++){
            var rec=store.getAt(i);
            if(rec.data['isdebit']==true){
                total += rec.data['d_amount']
            }else{
                total -= rec.data['c_amount']
            }
        }
        this.clearingBalance = total + this.openingBalance;
    },
    
    upfileHandler : function() {
    if (this.fileuploadwin.form.isValid()) {
        Wtf.getCmp('save').disabled = true;
    }
        if (this.fileuploadwin.form.isValid()) {
            this.fileuploadwin.form.submit({
                scope : this,
                failure : function(frm, response) {
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.bankReconcile.import.msg11"));  //Error occurred while uploading File.
                },
                success : function(frm, response) {
                    this.docID=response.result.docID;
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.bankReconcile.import.msg12"));  //File uploaded successfully.
                }
            })
        }
    },
    genSuccessResponse: function (response) {
        if (response.success) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation"), response.msg], response.success * 2 + 1);
            if (this.clearanceWindow)
                this.clearanceWindow.close();
            this.finalStore.removeAll();
        } else {   //Fail Response
            var msg = "";
            if (response.msg) {
                msg = response.msg;
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
            }
        }
    },

    genFailureResponse:function(response){
        var msg= WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    
    updateData:function(accid, startdate, enddate, openingBalance, res){
        this.clearingBalance=0;
        this.openingBalance=openingBalance;
        this.accid=accid;
        this.startdate=startdate;
        this.enddate=enddate;
        this.response=res;
        
        this.importRecordStore.loadData(this.response);
        this.existingRecordStore.removeAll();
        this.finalStore.removeAll();
    },
    
    createButtonClicked: function(){
        callSelectTransactionTypeWindow(null);
    },
    
    addPaymentEntryOnClickButton: function(){
        var selectedFromImportRecordGrid = this.importRecordGridSM.getSelections();
        var impRecArr = selectedFromImportRecordGrid[0];
        if (impRecArr != undefined && impRecArr != '') {
            var date=impRecArr.data['date'];
            var amount=impRecArr.data['amount'];
            var payee=impRecArr.data['payee'];
            
            var startdate="";var enddate="";var minamt="";var maxamt="";
            
            if(this.ruleStartDate!="" && this.ruleEndDate!=""){
                startdate=new Date(this.ruleStartDate);
                enddate=new Date(this.ruleEndDate);
            }else{
                startdate=date;
                enddate=date;
            }
            
            if(this.ruleMinAmount !="" && this.ruleMaxAmount !=""){
                minamt=this.ruleMinAmount;
                maxamt=this.ruleMaxAmount;
            }else{
                minamt=amount;
                maxamt=amount;
            }
            
            var params={
                accountid:this.accid,
                stdate:WtfGlobal.convertToGenericStartDate(startdate),
                enddate:WtfGlobal.convertToGenericEndDate(enddate),
                minamount:minamt,
                maxamount:maxamt,
                payee:payee
            };
            Wtf.Ajax.requestEx({
                url:"ACCOtherReports/getMatchedReconciliationData.do",
                params:params
            }, this, this.successCallback, this.failureCallback);
        }
    }
});
