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
Wtf.account.BankReconciliationHistoryReport=function(config){
    this.id = config.id;
    this.accid=config.accid;
    this.stdate=config.stdate;
    this.enddate=config.enddate;
    this.sm=new Wtf.grid.CheckboxSelectionModel();
    this.GridRec = Wtf.data.Record.create ([
        {name:'billid'},
        {name:'action'},
        {name:'clearanceDate',type:'date'},
        {name:'checkcount'},
        {name:'depositecount'},
        {name:'clearingamount'},
        {name:'createdby'},
        {name:'attachdoc'},
    ]);
    this.Store = new Wtf.data.Store({
        url: "ACCReconciliation/getBankReconciliationHistory.do",
        baseParams:{
            mode:57
        },
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"
        },this.GridRec)
    });
    
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.Store,
        border:false,
        id : "historyGrid" + this.id,
        viewConfig:{forceFit:true} ,
        forceFit:true,
        loadMask : true,
        sm:this.sm,
        columns:[this.sm,{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.historyAction"),  //"Action",
            dataIndex:'action',
            pdfwidth:100,
            sortable:true
        },{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.historyDate"),  //"Clearence/Unclearence Date"
            dataIndex:'clearanceDate',
            width:120,
            align:'center',
            pdfwidth:150,
            sortable:true,
            renderer:WtfGlobal.onlyDateRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.historyCheck"),  //"Payment Items Reconciled",
            dataIndex:'checkcount',
            pdfwidth:170,
            align:'right',
            sortable:true
        },{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.historyDep"),  //"Deposit Items Reconciled",
            dataIndex:'depositecount',
            pdfwidth:170,
            align:'right',
            sortable:true
        },{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.historyAmount"),  //"Clearing Amount",
            dataIndex:'clearingamount',
            pdfwidth:120,
            renderer:WtfGlobal.currencyRenderer,
            align:'right',
            sortable:true
        },{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.historyRecBy"),  //"Reconciled/Unreconciled By",
            dataIndex:'createdby',
            pdfwidth:180,
            align:'center',
            sortable:true
        },{
            header:WtfGlobal.getLocaleText("acc.bankReconcile.historyAtt"),  //"Attachment",
            dataIndex:'attachdoc',
            pdfwidth:150,
            sortable:true,
            renderer : this.DownloadLink.createDelegate(this)
        }]
    });
    Wtf.apply(this,{
        items:[{
            region:'center',
            layout:'fit',
            border:false,
            items:this.grid
        }]
    },config);
    Wtf.account.BankReconciliationHistoryReport.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry': true
    });
    this.grid.on('render',this.loadParmStore,this)
    this.grid.on('cellclick',this.onCellClick, this);
}
Wtf.extend(Wtf.account.BankReconciliationHistoryReport,Wtf.Panel,{

    loadParmStore:function(){
        if(this.entryID==null)
            this.Store.load({params:{accid:this.accid,stdate:this.stdate,enddate:this.enddate}});
},
    
loadStore:function(accid){
    if(this.entryID==null)
            this.Store.load({params:{accid:accid}});
},
    
onCellClick:function(g,i,j,e){
    e.stopEvent();
    var el=e.getTarget("a");
    if(el==null)return;
    var dataindex=g.getColumnModel().getDataIndex(j);
    if(dataindex == "transactionID"){
        var formrec = this.Store.getAt(i);
        var type=formrec.data['type'];
        var withoutinventory=formrec.data['withoutinventory'];            
        viewTransactionTemplate1(type, formrec,withoutinventory);
    } else if(dataindex == "d_entryno") {
        var formrec = this.Store.getAt(i);
        var jid = formrec.data['jeid'];
        this.fireEvent('journalentry', jid, true);
    }
},
    
    
DownloadLink : function(value,meta,record, rowIndex, colIndex, store) {        
    var msg = "";
    if(typeof value =="number" && value > 0){
        
        var url = "ACCReconciliation/getAttachDocuments.do";
        //if (c.data['doccount'])
    msg = '<div class = "pwnd downloadDoc" wtf:qtitle="'
            + WtfGlobal.getLocaleText("acc.invoiceList.attachments")
            + '" wtf:qtip="'
            + WtfGlobal.getLocaleText("acc.invoiceList.clickToDownloadAttachments")
            + '" onclick="displayDocList1(\''
            + record.data['billid']
            + '\',\''
            + url
            + '\',\''
            + 'br'
            + this.id
            + '\', event,\''
            + ""
            + '\',\''
            + ""
            + '\',\''
            + false
            + '\',\''
            + 0
            + '\',\''
            + 0
            + '\',\''
            + ""
            + '\',\''
            + 'historyGrid' + this.id
            + '\')" style="width: 16px; height: 16px;cursor:pointer" id=\''
            + record.data['leaveid'] + '\'>&nbsp;</div>';
    }
    //else
    //  msg = "";
    return msg;
}        
     
        
});

function displayDocList1(id, url, gridid, event,creatorid,approverid,docReq,cnt,statusid,showleaves,reportGridId){    
    if(Wtf.getCmp('DocListWindow'))
        Wtf.getCmp("DocListWindow").destroy();
    new Wtf.DocListWindow({
        wizard:false,
        closeAction : 'hide',
        layout: 'fit',
        title:WtfGlobal.getLocaleText("acc.invoiceList.attachments"),
        shadow:false,
        bodyStyle: "background-color: white",
        closable: true,
        width : 250,
        heigth:250,
        url: url,
        gridid: gridid,
        modal:true,
        autoScroll:true,
        recid:id,
        delurl: "ACCReconciliation/deleteDocument.do?docid=",
        id:"DocListWindow",
        docCount:cnt,
        isDocReq:docReq,  
        statusID:statusid,  
        showleaves:showleaves,  
        reportGridId : reportGridId,
        dispto:"pmtabpanel"
    });

    var docListWin = Wtf.getCmp("DocListWindow");
    var leftoffset =event.pageX-200;

    var topoffset = event.pageY+10;

    if (document.all) {
        xMousePos = window.event.x+document.body.scrollLeft;
        yMousePos = window.event.y+document.body.scrollTop;
        xMousePosMax = document.body.clientWidth+document.body.scrollLeft;
        yMousePosMax = document.body.clientHeight+document.body.scrollTop;
        leftoffset=xMousePos-200;//xMousePos;
        topoffset=yMousePos+120;//yMousePos;
        
    }
    if(docListWin.innerpanel==null||docListWin.hidden==true){
        docListWin.setPosition(leftoffset, topoffset);

        docListWin.show();
    }else{
        docListWin.hide();

    }
}

//====================== Reconciliation History Details Report =============================

function RecordLinkNew(billid,transType,withoutinventory,rec){
    viewTransactionTemplate1(transType, rec, false, billid); 
}
function JELinkNew(billid,id){
    var sdfsdff=Wtf.getCmp(id);
    sdfsdff.fireEvent('journalentry', billid, true);
}

Wtf.account.BankReconciliationHistoryDetails=function(config){
    this.accid=config.accid;
    this.stdate=config.stdate;
    this.enddate=config.enddate;
    this.expander = new Wtf.grid.RowExpander({});
    var btnArr=[];
    this.sm=new Wtf.grid.CheckboxSelectionModel();
    this.GridRec = Wtf.data.Record.create ([
        {
            name:'billid'
        },
        
        {
            name:'action'
        },
        {
            name:'brstartdate',
            type:'date'
        },
        {
            name:'brenddate',
            type:'date'
        },
        {
            name:'clearanceDate',
            type:'date'
        },
        
        {
            name:'checkcount'
        },
        
        {
            name:'depositecount'
        },
        
        {
            name:'clearingamount'
        },
        
        {
            name:'createdby'
        },        
        {
            name:'attachdoc'
        },        
        {
            name:'accountid'
        },
        {
            name:'accountname'
        },
        {
            name:'currencyid'
        },
        {
            name:'currencysymbol'
        },
        {
            name:'reconcileno'
        },
        {
            name:'bankBookBalanceinAcc'
        },
        {
            name:'clearedChecksAmountinAcc'
        },
        {
            name:'clearedDepositsAmountinAcc'
        },
        {
            name:'unclearedChecksAmountinAcc'
        },
        {
            name:'unclearedDepositsAmountinAcc'
        },{
            name:'bankStmtBalanceinAcc'
        }
    ]);
    this.Store = new Wtf.data.Store({
        url: "ACCReconciliation/getBankReconciliationHistory.do",
        baseParams:{
            mode:57,
            action : 1,  //Currently, we kept Reconcile / Unreconcile combo hidden. By default, we are fetching Reconciled Records only. So action=1.
            start : 0,
            limit : 100
        },
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"
        },this.GridRec)
    });
    this.actionStore = new Wtf.data.SimpleStore({
        fields: [{
                name:'id',
                type:'int'
            }, 'name'],
        data :[[0,'All'],[1,'Reconcile'],[2,'Un-Reconcile']]
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
        value:WtfGlobal.getDates(false)
    });    
    
    this.actionCombo = new Wtf.form.ComboBox({
        store: this.actionStore,
        fieldLabel:WtfGlobal.getLocaleText("acc.accPref.nature"),  //'Nature',
        name:'nature',
        displayField:'name',
        hidden:true,
        hideLabel:true,
        forceSelection: true,
        anchor:'95%',
        valueField:'id',
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus:true            
    });
    this.actionCombo.setValue(0);
    this.actionCombo.on('change', function(){        
        this.Store.load({
            params:{
                action : (this.actionCombo!=undefined) ? this.actionCombo.getValue() : 0,
                accid:this.accid,
                stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            }
        });
    }, this);
    //btnArr.push(WtfGlobal.getLocaleText("acc.bankReconcile.historyAction"), this.actionCombo);
    btnArr.push(WtfGlobal.getLocaleText("acc.common.from"), this.startDate);    //From : Startdate
    btnArr.push(WtfGlobal.getLocaleText("acc.common.to"), this.endDate);        //To : Enddate
    
    btnArr.push('-');
    btnArr.push({ 
        xtype: 'button',
        text: WtfGlobal.getLocaleText("acc.common.fetch"), //'Fetch',
        tooltip: WtfGlobal.getLocaleText("acc.bankReconcile.fetchTT"), //"Select a time period to view corresponding ledger records.",
        iconCls: 'accountingbase fetch',
        scope: this,
        handler: this.onFetchClick
    });
//    this.expandCollpseButton = new Wtf.Toolbar.Button({       //Expand All Button
//        text: WtfGlobal.getLocaleText("acc.field.Expand"),
//        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
//        iconCls: 'pwnd toggleButtonIcon',
//        //hidden:(this.moduleid == Wtf.Acc_Purchase_Order_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId || this.moduleid == Wtf.Acc_RFQ_ModuleId || this.moduleid == Wtf.Acc_Customer_Quotation_ModuleId || this.moduleid == Wtf.Acc_Vendor_Quotation_ModuleId)?false:true,
//        scope: this,
//        handler: function() {
//            if (this.expandCollpseButton.getText() == WtfGlobal.getLocaleText("acc.field.Expand")) {
//                this.expandButtonClicked = true;
//            }
//            this.expandCollapseGrid(this.expandCollpseButton.getText(), this.expandStore, this.grid.plugins[0], this);
//        }
//    });
//    btnArr.push('-');
//    btnArr.push(this.expandCollpseButton);
    
    this.expandRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },
        
        {
            name: 'd_date',
            type:'date'
        },
        
        {
            name: 'd_accountname'
        },
        
        {
            name: 'd_entryno'
        },
        
        {
            name: 'd_journalentryid'
        },
        
        {
            name: 'd_amount'
        },
        
        {
            name: 'd_amountinacc'
        },
        
        {
            name: 'd_amountintransactioncurrency'
        },
        
        {
            name: 'd_reconciledate',
            type:'date'
        },
        
        {
            name: 'c_date',
            type:'date'
        },
        
        {
            name: 'c_accountname'
        },
        
        {
            name: 'c_entryno'
        },
        
        {
            name: 'c_journalentryid'
        },
        
        {
            name: 'c_amountintransactioncurrency'
        },
        
        {
            name: 'c_amount'
        },
        
        {
            name: 'c_amountinacc'
        },
        
        {
            name: 'c_reconciledate',
            type:'date'
        },
        
        {
            name: 'lastreconciledate',
            type:'date'
        },
        
        {
            name: 'type'
        },
        
        {
            name: 'billid'
        },
        
        {
            name: 'moduleid'
        },
        
        {
            name: 'isOpeningTransaction', 
            type:'boolean'
        },
        
        {
            name: 'transactionID'
        },
        
        {
            name: 'chequeno'
        },
        
        {
            name: 'chequedate',
            type:'date'
        },
        
        {
            name: 'description'
        },
        
        {
            name: 'withoutinventory'
        },
        
        {
            name: 'paidto'
        },
        
        {
            name: 'currencysymbol'
        },
        
        {
            name: 'accountcurrencysymbol'
        },    
        
        {
            name: 'reconciletype'
        },    
        
        {
            name: 'payments'
        }    
    ]);
    this.expandStore = new Wtf.data.Store({
        url:"ACCReports/getBankReconciliationHistoryRows.do",
        baseParams:{
            mode:67,
            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericStartDate(this.endDate.getValue()),
            dateFilterON:1
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    //    this.expandStore.on("beforeload", function(store){
    //        store.proxy.conn.url = "ACCReports/getReconciliationData.do";
    //    }, this);
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.expander.on("expand",this.onRowexpand,this);
    
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.Store,
        border:false,
        viewConfig:{
            forceFit:true
        } ,
        forceFit:true,
        loadMask : true,
        sm:this.sm,
        layout:'auto',
        plugins:[this.expander],
        columns:[this.sm,this.expander,{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcile.historyAction") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcile.historyAction") + "<div>",  //"Action",
                dataIndex:'action',
                pdfwidth:100,
                sortable:true
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.msg6") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.msg6") + "<div>",  //Reconcile Number
                dataIndex:'reconcileno',
                pdfwidth:100,
                sortable:true
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcile.historyDate") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcile.historyDate") + "<div>",  //"Clearence/Unclearence Date"
                dataIndex:'clearanceDate',
                width:120,
                align:'center',
                pdfwidth:150,
                sortable:true,
                renderer:WtfGlobal.onlyDateRenderer
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.je.acc") + "\">" + WtfGlobal.getLocaleText("acc.je.acc") + "<div>",  //"Account Name",
                dataIndex:'accountname',
                pdfwidth:100,
                sortable:true
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.nee.FromDate") + "\">" + WtfGlobal.getLocaleText("acc.nee.FromDate") + "<div>",  //Bank Reconcile / unreconcile from date
                dataIndex:'brstartdate',
                width:120,
                align:'center',
                pdfwidth:150,
                sortable:true,
                renderer:WtfGlobal.onlyDateRenderer
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.nee.ToDate") + "\">" + WtfGlobal.getLocaleText("acc.nee.ToDate") + "<div>",  //Bank Reconcile / unreconcile To date
                dataIndex:'brenddate',
                width:120,
                align:'center',
                pdfwidth:150,
                sortable:true,
                renderer:WtfGlobal.onlyDateRenderer
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.bankbook.balance") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.bankbook.balance") + "<div>",  //Bank Book Balance
                dataIndex:'bankBookBalanceinAcc',
                pdfwidth:120,
                renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
                align:'right',
                sortable:true
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.uncleared.checks") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.uncleared.checks") + "<div>",  //Total Uncleared Checks
                dataIndex:'unclearedChecksAmountinAcc',
                pdfwidth:120,
                renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
                align:'right',
                sortable:true
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.uncleared.deposits") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.uncleared.deposits") + "<div>",  //Total Uncleared Deposits
                dataIndex:'unclearedDepositsAmountinAcc',
                pdfwidth:120,
                renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
                align:'right',
                sortable:true
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.statement.balance") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.statement.balance") + "<div>",  //Bank Statement Balance
                dataIndex:'bankStmtBalanceinAcc',
                pdfwidth:120,
                renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
                align:'right',
                sortable:true
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.cleared.checks") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.cleared.checks") + "<div>",  //Total Cleared Checks
                dataIndex:'clearedChecksAmountinAcc',
                pdfwidth:120,
                renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
                align:'right',
                sortable:true
            },{
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.cleared.deposits") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcileNumber.cleared.deposits") + "<div>",  //Total Cleared Deposits
                dataIndex:'clearedDepositsAmountinAcc',
                pdfwidth:120,
                renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol,
                align:'right',
                sortable:true
            },
            {
                header: WtfGlobal.getLocaleText("acc.invoiceList.attachments"), //"Attachments",
                dataIndex: 'attachdoc',
                id: "attachdoc" + this.id,
                pdfwidth:150,
                width:150,
                sortable:true,
                renderer : this.DownloadLink.createDelegate(this)
            },
            {
                header:"<div  wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.bankReconcile.historyRecBy") + "\">" + WtfGlobal.getLocaleText("acc.bankReconcile.historyRecBy") + "<div>",  //"Reconciled/Unreconciled By",
                dataIndex:'createdby',
                pdfwidth:180,
                align:'center',
                sortable:true
            }]
    });
    var bottombtnArr=[];
    var linelevelColArr = [];
    linelevelColArr.push(
    //                {
    //                header : 'srno',
    //                title : "Sr.No.",
    //                width : 75,
    //                align : 'none',
    //                index : 0
    //            },
    {
        header : 'date',
        title : WtfGlobal.getLocaleText("acc.jobworkin.create.Date"),        //"Date",
        width : 75,
        align : 'date',
        index : 0
    },{
        header : 'customername',
        title : WtfGlobal.getLocaleText("acc.salescomission.Customer/VendorName"),  //Customer/Vendor Name
        width : 75,
        align : 'none',
        index : 0
    },{
        header : 'paidto',
        title : WtfGlobal.getLocaleText("acc.bankReconcile.header.receive.paidto"),     //"Received From/Paid To",
        width : 75,
        align : 'none',
        index : 0
    },{
        header : 'chequeno',
        title : WtfGlobal.getLocaleText("acc.bankReconcile.header.ChqRef"),     //"Cheque/Reference No.",
        width : 75,
        align : 'none',
        index : 0
    },{
        header : 'chequedate',
        title : WtfGlobal.getLocaleText("payment.date.postDate"),       //"Cheque Date",
        width : 75,
        align : 'date',
        index : 0
    },{
        header : 'entryno',
        title : WtfGlobal.getLocaleText("view.pendingapproval.JournalEntryNo"),         //"Journal Entry No.",
        width : 75,
        align : 'none',
        index : 0
    },{
        header: 'transactionnumber',
        title : WtfGlobal.getLocaleText("acc.reval.transaction"), //Transaction No
        width : 75,
        align : 'none',
        index : 0
    },{
        header : 'amountintransactioncurrency',
        title : WtfGlobal.getLocaleText("acc.field.AmountinDocumentCurrencyPDF"),       //"Amount in Document Currency",
        width : 75,
        align : 'none',
        index : 0
    },{
        header : 'amountinacc',
        title : WtfGlobal.getLocaleText("acc.field.AmountinAccountCurrencyPDF"),        //"Amount in Account Currency",
        width : 75,
        align : 'none',
        index : 0
    },{
        header : 'amount',
        title : WtfGlobal.getLocaleText("acc.field.AmountinBaseCurrencyPDF"),       //"Amount in Base Currency",
        width : 75,
        align : 'currency',
        index : 0
    })
    this.exportButton=new Wtf.exportButton({
        obj:this,
        isEntrylevel:false,
        col:linelevelColArr,
        id:"exportReports"+ this.id,   
        iconCls: 'pwnd exportcsv',
        text: WtfGlobal.getLocaleText("acc.common.exportToxls"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        //disabled :true,
        filename:"Reconcliation History", 
        menuItem:{
            xls:true,
            csv:false
        },
        get:Wtf.autoNum.BankReconcilationHistoryDetails
    });
    this.exportButton.setParams({
        stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
        enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
        get:Wtf.autoNum.BankReconcilationHistoryDetails
    });
    bottombtnArr.push(this.exportButton);
    Wtf.apply(this,{
        items:[{
                region:'center',
                layout:'fit',
                border:false,
                items:this.grid,
                tbar: btnArr,
                bbar: bottombtnArr
            }]
    },config);
    Wtf.account.BankReconciliationHistoryDetails.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry': true
    });
    this.grid.on('render',this.loadParmStore,this)
    this.grid.on('cellclick',this.onCellClick, this);
}
Wtf.extend(Wtf.account.BankReconciliationHistoryDetails,Wtf.Panel,{
    
    loadParmStore:function(){
        if(this.entryID==null)
            this.Store.load({
                params:{
                    accid:this.accid,
                    stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),//SDP-13962
                    enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
                }
        });
    },
    
    loadStore:function(accid){
        if(this.entryID==null)
            this.Store.load({
                params:{
                    accid:accid
                }
        });
    },
    
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var dataindex=g.getColumnModel().getDataIndex(j);
        if(dataindex == "transactionID"){
            var formrec = this.Store.getAt(i);
            var type=formrec.data['type'];
            var withoutinventory=formrec.data['withoutinventory'];            
            viewTransactionTemplate1(type, formrec,withoutinventory);
        } else if(dataindex == "d_entryno") {
            var formrec = this.Store.getAt(i);
            var jid = formrec.data['jeid'];
            this.fireEvent('journalentry', jid, true);
        }
    },
    
    onFetchClick:function(){
        var sdate = this.startDate.getValue();
        var edate = this.endDate.getValue();
        if(sdate>edate){
            WtfComMsgBox(1,2);
            return;
        }
        this.Store.load({
            params:{
                action : (this.actionCombo!=undefined) ? this.actionCombo.getValue() : 0,
                accid:this.accid,
                stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            }
        });
    },
    
    onRowexpand:function(scope, record, body){
        this.expanderBody=body;
        this.expandStore.load({
            params:{
                billid:record.data.billid,
                action:record.data.action,
                accountid:record.data.accountid,
//                stdate:WtfGlobal.convertToGenericStartDate(record.data.clearanceDate),
//                enddate:WtfGlobal.convertToGenericStartDate(record.data.clearanceDate),
                dateFilter:"1",     //(record.data.action=="Reconcile") ? "0" : "1",
                isConcileReport : (record.data.action=="Reconcile") ? true : false,     //Reconciled records means View Reconcile Report & Un-reconciled record means Reconcile Report
                isReconciledHistoryDetails:true,
                loadTransactionDetails:true                
            }
        });
    },
    
    accountCurrencySummaryRenderer: function(value){
        if ( this.cmbAccount !=undefined && this.cmbAccount.getValue() == "") {
            return WtfGlobal.summaryRenderer(WtfGlobal.currencyRenderer(value));
        } else {
            return WtfGlobal.summaryRenderer(WtfGlobal.rateCurrencySymbolForAccountCurrency(value));
        }
    },
    
    fillExpanderBody:function(record,a,b,c,d,e,f,g){
        var disHtmld = "", disHtmlc = "";
        this.custArr = [];
        var ReconcileHeader = "";        
        var sameParent = false;
        //var action = record.data.action;
        for (var i = 0; i < this.expandStore.getCount(); i++) {
            var header = "";
            var body = "", prevBillid = "";
            var rec = this.expandStore.getAt(i);
            var basecurrency = rec.currencysymbol;
            
            //=================== Cleared Deposits & Checks ========================
            if(rec.json['reconciletype']=="Cleared Deposits"){
                var prevBillid = "";
                var currentBillid = rec.data['billid'];
                if (prevBillid != currentBillid) {             // Check if last record also has same 'billid'.  
                    prevBillid = currentBillid;
                    sameParent = false;
                } else {
                    sameParent = true;
                }
                var reconciletype = rec.json['reconciletype'];            
                ReconcileHeader = this.getReconcileHeader(rec, reconciletype);    
                var depositRecords = rec.json['payments'];
                if(depositRecords.length === 0){
                    continue;
                }
                var footerText = "Debit Amount in Base Currency : ";
                var header1 = this.getReconcileExpanderData(depositRecords, sameParent, ReconcileHeader[1], ReconcileHeader[2], basecurrency,this, footerText);
                var moreIndex = this.grid.getStore().findBy(
                function(record, id) {
                    if (record.get('billid') == rec.data['billid']) {
                        return true;  // a record with this data exists 
                    }
                    return false;  // there is no record in the store with this data
                }, this);
                if (moreIndex != -1) {
                    body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', this.grid.getView().getRow(moreIndex));
                    disHtmld = "<div class='expanderContainer3' style='margin-left:5em !important; width:114% !important'>" + ReconcileHeader[0] + header1 + "</div>";                
                }
            }
            if(rec.json['reconciletype']=="Cleared Checks"){
                var prevBillid = "";
                var currentBillid = rec.data['billid'];
                if (prevBillid != currentBillid) {             // Check if last record also has same 'billid'.  
                    prevBillid = currentBillid;
                    sameParent = false;
                } else {
                    sameParent = true;
                }
                var reconciletype = rec.json['reconciletype'];                
                var checkRecords = rec.json['payments'];
                if(checkRecords.length === 0){
                    continue;
                }
                ReconcileHeader = this.getReconcileHeader(rec, reconciletype); 
                var footerText = "Credit Amount in Base Currency : ";
                var header2 = this.getReconcileExpanderData(checkRecords, sameParent, ReconcileHeader[1], ReconcileHeader[2], basecurrency,this, footerText);
                var moreIndex = this.grid.getStore().findBy(
                function(record, id) {
                    if (record.get('billid') == rec.data['billid']) {
                        return true;  // a record with this data exists 
                    }
                    return false;  // there is no record in the store with this data
                }, this);
                if (moreIndex != -1) {
                    body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', this.grid.getView().getRow(moreIndex));
                    disHtmlc += "</br>"
                    disHtmlc = disHtmlc + "<div class='expanderContainer3' style='margin-left:5em !important; width:114% !important'>" + ReconcileHeader[0] + header2 + "</div>";
                }
            }
            
            //=================== Uncleared Deposits & Checks ========================        
            
            if(rec.json['reconciletype']=="Uncleared Deposits"){
                var prevBillid = "";
                var currentBillid = rec.data['billid'];
                if (prevBillid != currentBillid) {             // Check if last record also has same 'billid'.  
                    prevBillid = currentBillid;
                    sameParent = false;
                } else {
                    sameParent = true;
                }
                var reconciletype = rec.json['reconciletype'];                
                var checkRecords = rec.json['payments'];
                if(checkRecords.length === 0){
                    continue;
                }
                ReconcileHeader = this.getReconcileHeader(rec, reconciletype); 
                var footerText = "Debit Amount in Base Currency : ";
                var header2 = this.getReconcileExpanderData(checkRecords, sameParent, ReconcileHeader[1], ReconcileHeader[2], basecurrency,this, footerText);
                var moreIndex = this.grid.getStore().findBy(
                function(record, id) {
                    if (record.get('billid') == rec.data['billid']) {
                        return true;  // a record with this data exists 
                    }
                    return false;  // there is no record in the store with this data
                }, this);
                if (moreIndex != -1) {
                    body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', this.grid.getView().getRow(moreIndex));
                    disHtmlc += "</br>"
                    disHtmlc = disHtmlc + "<div class='expanderContainer3' style='margin-left:5em !important; width:114% !important'>" + ReconcileHeader[0] + header2 + "</div>";
                }
            }
            
            if(rec.json['reconciletype']=="Uncleared Checks"){
                var prevBillid = "";
                var currentBillid = rec.data['billid'];
                if (prevBillid != currentBillid) {             // Check if last record also has same 'billid'.  
                    prevBillid = currentBillid;
                    sameParent = false;
                } else {
                    sameParent = true;
                }
                var reconciletype = rec.json['reconciletype'];                
                var checkRecords = rec.json['payments'];
                if(checkRecords.length === 0){
                    continue;
                }
                ReconcileHeader = this.getReconcileHeader(rec, reconciletype); 
                var footerText = "Credit Amount in Base Currency : ";
                var header2 = this.getReconcileExpanderData(checkRecords, sameParent, ReconcileHeader[1], ReconcileHeader[2], basecurrency,this, footerText);
                var moreIndex = this.grid.getStore().findBy(
                function(record, id) {
                    if (record.get('billid') == rec.data['billid']) {
                        return true;  // a record with this data exists 
                    }
                    return false;  // there is no record in the store with this data
                }, this);
                if (moreIndex != -1) {
                    body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', this.grid.getView().getRow(moreIndex));
                    disHtmlc += "</br>"
                    disHtmlc = disHtmlc + "<div class='expanderContainer3' style='margin-left:5em !important; width:114% !important'>" + ReconcileHeader[0] + header2 + "</div>";
                }
            }
            
            disHtmlc += "</br>";
            if(rec.json['reconciletype']=="Currency"){
                var prevBillid = "";
                // TO DO HERE
            }            
            body.innerHTML = (disHtmld + disHtmlc);        
        }
    },
    
    getReconcileHeader: function(rec, reconciletype){
        var reconcileHeaderArray = [];
        var srno = "S.No.", TransactionNo = "Transaction No.", Customer = "Customer/Vendor Name", date ="Date", chqDate ="Cheque Date";
        var ChequeRefNumber = "Cheque/Reff Number", jenumber = "Journal Entry Number", lastReconDate = "Last Reconciled Date";
        //var reconDate = "Reconciled Date";
        
        
        var headerText = "";
        if(reconciletype=="Cleared Deposits"){
            headerText = "Cleared Deposits and Other Credits";
        } else if(reconciletype=="Cleared Checks"){
            headerText = "Cleared Checks and Payments";
        } else if(reconciletype=="Uncleared Deposits"){
            headerText = "Uncleared Deposits and Other Credits";
        } else if(reconciletype=="Uncleared Checks"){
            headerText = "Uncleared Checks and Payments";
        } 
        var receveidFrom = (reconciletype=="Uncleared Deposits" || reconciletype=="Cleared Deposits") ? "Received From" : "Paid to";
        var amountDocCurr = (reconciletype=="Uncleared Deposits" || reconciletype=="Cleared Deposits") ? "Debit Amount in Document Currency" : "Credit Amount in Document Currency";
        var amountAccCurr = (reconciletype=="Uncleared Deposits" || reconciletype=="Cleared Deposits") ? "Debit Amount in Account Currency" : "Credit Amount in Account Currency";
        var amountinbase = (reconciletype=="Uncleared Deposits" || reconciletype=="Cleared Deposits") ? "Debit Amount in Base Currency" : "Credit Amount in Base Currency";
        
        var arr=[date, Customer, receveidFrom, ChequeRefNumber, chqDate, jenumber, TransactionNo, amountDocCurr, amountAccCurr, amountinbase];
        var gridHeaderText = headerText;
        var header = "<span class='gridHeader'>"+gridHeaderText+"</span>";   
        
        var count=0;
        for(var i=0;i<arr.length;i++){
            if(arr[i] != ""){
                count++;
            }
        }
        count++; // from grid no
        var widthInPercent=100/count;
        var minWidth = count*110;
        header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
        header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
        for(var arrI=0;arrI<arr.length;arrI++){
            if(arr[arrI]!=undefined)
                header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[arrI] + "</span>";
        }
        header += "</div><div style='width: 100%; min-width:"+minWidth+"px'><span class='gridLine' style='height:26px !important;'></span></div>";  
        reconcileHeaderArray.push(header);
        reconcileHeaderArray.push(minWidth);
        reconcileHeaderArray.push(widthInPercent);
        return reconcileHeaderArray;
    },
    //JELinkNew:function(billid){
    //    this.fireEvent('journalentry', billid, true);
    //},
    
    getReconcileExpanderData: function(paymentrecord, sameParent, minWidth, widthInPercent, basecurrency,obj, footerText){
        if (!sameParent) {
            this.Repeatheader = "";
        }    
        this.Repeatheader += "<div style='width: 100%; display:inline-block; min-width:" + minWidth + "px'>";  
        //this.Repeatheader += "<span class='gridNo'>"+(++this.serialNumber)+".</span>";
        
        //Column : Sr.No.
        this.srNumber = 0;
        var amountinbase = 0;
        for(var k=0; k<paymentrecord.length; k++){ 
            var rec = paymentrecord[k];
            this.Repeatheader += "<span class='gridNo'>" + (++this.srNumber) + ".</span>";
            var transType = rec['type']!=undefined ? rec['type'] : "";
            
            //Column : Date
            //var date = (rec['c_dateinuserdf']!=undefined && rec['c_dateinuserdf']!="") ? rec['c_dateinuserdf'] : (rec['d_dateinuserdf']!=undefined && rec['d_dateinuserdf']!="" ? rec['d_dateinuserdf'] : "");
            var date = (rec['date']!=undefined && rec['date']!="") ? rec['date'] : "";
            //date = (date!="N/A") ? WtfGlobal.onlyDateLeftRenderer(new Date(date)):"N/A";
            this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + date + "'>" + Wtf.util.Format.ellipsis(date, 15) + "&nbsp;</span>";
            
            //Column : Customer/Vensor Name
            //var accountname = (rec['c_accountname']!=undefined && rec['c_accountname']!="") ? rec['c_accountname'] : ((rec['d_accountname']!=undefined && rec['d_accountname']!="") ? rec['d_accountname'] : "");
            var accountname = (rec['customername']!=undefined && rec['customername']!="") ? rec['customername'] : "";
            this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + accountname + "'>" + Wtf.util.Format.ellipsis(accountname, 15) + "&nbsp;</span>";
                        
            //Column : Received From / Paid To
            //var receiver = (rec['c_payee']!=undefined && rec['c_payee']!="") ? rec['c_payee'] : ((rec['d_payee']!=undefined && rec['d_payee']!="") ? rec['d_payee'] : "");
            var receiver = (rec['paidto']!=undefined && rec['paidto']!="") ? rec['paidto'] :"";
            this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + receiver + "'>" + Wtf.util.Format.ellipsis(receiver, 15) + "&nbsp;</span>";
            
            //Column : Cheque / Reference No.
            var chequeno = (rec['chequeno']!=undefined && rec['chequeno']!="") ? rec['chequeno'] : "";
            this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + chequeno + "'>" + Wtf.util.Format.ellipsis(chequeno, 15) + "&nbsp;</span>";
            
            //Column : Cheque Date
            //var chqDate = (rec['chequedateinuserdf']!=undefined && rec['chequedateinuserdf']!="") ? rec['chequedateinuserdf'] : "";
            var chqDate = (rec['chequedate']!=undefined && rec['chequedate']!="") ? rec['chequedate'] : "";
            //chqDate = (chqDate!="N/A") ? WtfGlobal.onlyDateLeftRenderer(new Date(chqDate)):"N/A";
            this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + chqDate + "'>" + Wtf.util.Format.ellipsis(chqDate, 15) + "&nbsp;</span>";
            
            //Column : JE Number            
//            var jenumber = (rec['c_entryno']!=undefined && rec['c_entryno']!="") ? rec['c_entryno'] : ((rec['d_entryno']!=undefined && rec['d_entryno']!="") ? rec['d_entryno'] : "");
//            var jeid = (rec['c_journalentryid']!=undefined && rec['c_journalentryid']!="") ? rec['c_journalentryid'] : ((rec['d_journalentryid']!=undefined && rec['d_journalentryid']!="") ? rec['d_journalentryid'] : "");
            var jenumber = (rec['entryno']!=undefined && rec['entryno']!="") ? rec['entryno'] :"";
            var jeid = (rec['jeid']!=undefined && rec['jeid']!="") ? rec['jeid'] : "";
            jenumber = "<a  class='jumplink' href='#' onClick='javascript:JELinkNew(\""+jeid+"\",\""+this.id+"\")'>"+jenumber+"</a>";
            //jenumber = (jenumber=="") ? jenumber+"&nbsp;" : (jenumber+"");
            this.Repeatheader += "<span class='gridRow '  style='width:"+widthInPercent+"% ! important;' >"+jenumber+ "&nbsp;</span>";
            
            //Column : transactionID
//            var transactionID = (rec['transactionID']!=undefined && rec['transactionID']!="") ? rec['transactionID'] : "";
            var transactionNo = (rec['transactionnumber']!=undefined && rec['transactionnumber']!="") ? rec['transactionnumber'] : "";
            this.Repeatheader += "<span class='gridRow '  style='width:"+widthInPercent+"% ! important;' >"+transactionNo+ "&nbsp;</span>";
            
            //Column : Reconcile Date
            //        var reconDate = (rec['c_reconciledate']!=undefined && rec['c_reconciledate']!="") ? rec['c_reconciledate'] : (rec['d_reconciledate']!=undefined && rec['d_reconciledate']!="" ? rec['d_reconciledate'] : "");
            //        //reconDate = (reconDate!="N/A") ? WtfGlobal.onlyDateLeftRenderer(new Date(reconDate)):"N/A";
            //        this.Repeatheader += "<span class='gridRow' style='width: " + widthInPercent + "% ! important;'  wtf:qtip='" + reconDate + "'>" + Wtf.util.Format.ellipsis(reconDate, 15) + "&nbsp;</span>";
            
            //Column : Amount In Document Currency
            var amountinDocCurr = 0;
            //amountinDocCurr = (rec['c_amountintransactioncurrency']!=undefined && rec['c_amountintransactioncurrency']!="") ? rec['c_amountintransactioncurrency'] : ((rec['d_amountintransactioncurrency']!=undefined && rec['d_amountintransactioncurrency']!="") ? rec['d_amountintransactioncurrency'] : "");
            amountinDocCurr = (rec['amountintransactioncurrency']!=undefined && rec['amountintransactioncurrency']!="") ? rec['amountintransactioncurrency'] : "";
            amountinDocCurr = (amountinDocCurr!="") ? parseFloat(getRoundedAmountValue(amountinDocCurr*1)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL) : "";
            var currency = (rec['transcurrsymbol']!=undefined && rec['transcurrsymbol']!="") ? rec['transcurrsymbol'] : "";
//            var currency = (rec['c_transCurrSymbol']!=undefined && rec['c_transCurrSymbol']!="") ? rec['c_transCurrSymbol'] : ((rec['d_transCurrSymbol']!=undefined && rec['d_transCurrSymbol']!="") ? rec['d_transCurrSymbol'] : "");
            amountinDocCurr = WtfGlobal.addCurrencySymbolOnly(amountinDocCurr,currency,[true]);
            this.Repeatheader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+amountinDocCurr+"</span>";
            
            //Column : Amount In Account Currency
            var amountinAccCurr = 0;
//            amountinAccCurr = (rec['c_amountinacc']!=undefined && rec['c_amountinacc']!="") ? rec['c_amountinacc'] : ((rec['d_amountinacc']!=undefined && rec['d_amountinacc']!="") ? rec['d_amountinacc'] : "");
            amountinAccCurr = (rec['amountinacc']!=undefined && rec['amountinacc']!="") ? rec['amountinacc']: "";
            amountinAccCurr = (amountinAccCurr!="") ? parseFloat(getRoundedAmountValue(amountinAccCurr*1)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL) : "";
            var currency = rec['accountcurrencysymbol'];
            amountinAccCurr = WtfGlobal.addCurrencySymbolOnly(amountinAccCurr,currency,[true]);
            this.Repeatheader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+amountinAccCurr+"</span>";
            
            //Column : Amount In Base Currency
            var amount = 0;
//            amount = (rec['c_amount']!=undefined && rec['c_amount']!="") ? rec['c_amount'] : ((rec['d_amount']!=undefined && rec['d_amount']!="") ? rec['d_amount'] : "");
            amount = (rec['amount']!=undefined && rec['amount']!="") ? rec['amount'] : "";
            amountinbase += amount;
            amount = (amount!="") ? parseFloat(getRoundedAmountValue(amount*1)).toFixed(Wtf.AMOUNT_DIGIT_AFTER_DECIMAL) : "";        
            var currency = rec['basecurrency'];
            amount = WtfGlobal.addCurrencySymbolOnly(amount,currency,[true]);
            this.Repeatheader += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+amount+"</span>";
            //    
            //    var lastrecondate = (rec.data['lastreconciledate']!=undefined && rec.data['lastreconciledate']!="") ? rec.data['lastreconciledate'] : "N/A";
            //    lastrecondate = (lastrecondate!="N/A") ? WtfGlobal.onlyDateLeftRenderer(new Date(lastrecondate)):"N/A";
            //    this.Repeatheader += "<span class='gridRow' style='width: "+widthInPercent+"% !important;'>"+lastrecondate+"</span>";
            //    
            this.Repeatheader += "<br>";    
        }
        var totalamountinbase = "<B>" + WtfGlobal.conventInDecimal(amountinbase, WtfGlobal.getCurrencySymbol()) + "</B>";
        //this.Repeatheader += "<br><span class='gridHeader'style='border-bottom:0px; color:black !important; padding-left: 75%;'>"+(footerText+totalamountinbase)+"</span>";   
        this.Repeatheader += "</div>";
        return this.Repeatheader; 
    },

    DownloadLink: function (a, b, c, d, e, f) {
    var msg = "";
    var url = "ACCReconciliation/getAttachDocuments.do";
    //if (c.data['doccount'])
    if (c.data['attachdoc']!=0)
        msg ='('+c.data['attachdoc']+')'+'<div class = "pwnd downloadDoc" wtf:qtitle="'
        + WtfGlobal.getLocaleText("acc.invoiceList.attachments")
        + '" wtf:qtip="'
        + WtfGlobal.getLocaleText("acc.invoiceList.clickToDownloadAttachments")
        + '" onclick="displayDocList1(\''
        + c.data['billid']
        + '\',\''
        + url
        + '\',\''
        + 'br'
        + this.id
        + '\', event,\''
        + ""
        + '\',\''
        + ""
        + '\',\''
        + false
        + '\',\''
        + 0
        + '\',\''
        + 0
        + '\',\''
        + ""
        + '\')" style="width: 16px; height: 16px;cursor:pointer; margin-left: 24px; margin-top: -15px;" id=\''
        + c.data['leaveid'] + '\'>&nbsp;</div>';
    return msg;
    }
    
    //Expand All
//    expandCollapseGrid : function(btntext, expandstore, expanderObj, scopeObject) {
//        var arr = "";
//        var store = scopeObject.grid.getStore();
//        if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
//            for (var i = 0; i < store.data.length; i++) {
//                expanderObj.collapseRow(i)
//            }
//            scopeObject.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
//        } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
//            store.each(function(rec) {
//                if (rec.data.billid != "" && rec.data.billid != undefined)
//                    arr += "'"+rec.data.billid + "',";
//            }, scopeObject);
//            if (arr.length != 0) {
//                var colModelArray = [];
//                colModelArray = GlobalColumnModel[scopeObject.moduleid];
//                WtfGlobal.updateStoreConfig(colModelArray, expandstore);
//                colModelArray = [];
//                colModelArray = GlobalColumnModelForProduct[scopeObject.moduleid];
//                WtfGlobal.updateStoreConfig(colModelArray, expandstore);
//                arr = arr.substring(0, arr.length - 1);
//                scopeObject.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
//            }
//            expandstore.load({
//                params: {
//                    isHistoryDetails:true,
//                    isexpand : true,
//                    stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
//                    enddate : WtfGlobal.convertToGenericStartDate(this.endDate.getValue()),
//                    dateFilterON:1
//                }});         
//        }
//    }        
});

//====================== Reconciliation History Snapshots =============================