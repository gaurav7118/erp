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

Wtf.account.writeOffPaymentsReport = function(config) {
    Wtf.apply(this, config);
     this.panelID=config.id;
    this.isReceipt=config.isReceipt;
    
    this.GridRec = Wtf.data.Record.create([
        {name:'id'},
        {name:'billid'},
        {name:'billno'},
        {name:'currencysymbol'},
        {name:'currencyid'},
        {name:'externalcurrencyrate'},
        {name:'personname'},
        {name:'billdate', type:'date'},
        {name:'writeOffDate', type:'date'},
        {name:'receiptAmount'},
        {name:'receipttype'},
        {name:'paymentwindowtype'},
        {name:'detailtype'},
        {name:'jedate'},
        {name:'type'},
        {name:'amountWrittenOff'},
        {name:'jeno'},
        {name:'journalentryid'},
        {name:'reversejeno'},
        {name:'isrecovered'},
        {name:'memo'},
        {name:'deleted'}
    ]);
    
    this.userdsUrl = "";
    if(this.isReceipt){
        this.userdsUrl = "ACCWriteOff/getWrittenOffReceipts.do";
    }else{
        this.userdsUrl = "ACCWriteOff/getWrittenOffPayments.do";
    }
   
    this.userds = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        },this.GridRec),
        url : this.userdsUrl
    });
    
    this.userds.on('beforeload', function() {
        var currentBaseParams = this.userds.baseParams;
        this.userds.baseParams=currentBaseParams;
    },this);
    
    this.userds.on('load', function(store) {
        this.RecoverReceipt.disable();
        if(this.userds.getCount() < 1) {
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.quickPanelSearch.StorageChanged(store);
    }, this);
    
    this.userds.on('datachanged', function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    }, this);
    
    this.sm = new Wtf.grid.CheckboxSelectionModel(
    {
        singleSelect:true
    });
    
    
    this.grid = new Wtf.grid.GridPanel({    
        store:this.userds,
        border:false,
        layout:'fit',
        viewConfig: {
            forceFit:true
        },
        loadMask:true,
        columns:[this.sm,
            {
            header: this.isReceipt?WtfGlobal.getLocaleText("acc.prList.gridReceiptNo"):WtfGlobal.getLocaleText("acc.pmList.gridPaymentNo"),  //"Receipt No":"Payment No",
            dataIndex:'billno',
            autoWidth:true,
            renderer:WtfGlobal.linkDeletedRenderer
        },{
            header:this.isReceipt?WtfGlobal.getLocaleText("acc.agedPay.cus"):WtfGlobal.getLocaleText("acc.agedPay.ven "),  
            dataIndex:'personname',
            autoWidth:true
        },{
            header:this.isReceipt?WtfGlobal.getLocaleText("acc.prList.Date"):WtfGlobal.getLocaleText("acc.pmList.Date"),  //"Receipt Date":"Payment Date",
            dataIndex:'billdate',
            autoWidth : true,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.writeOff.writeOffDate"),
            dataIndex:'writeOffDate',
            autoWidth : true,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.het.53"), //"Amount"
            dataIndex:'receiptAmount',
            autoWidth : true,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.writeOff.Amount"),
            dataIndex:'amountWrittenOff',
            autoWidth : true,
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.agedPay.gridJEno"),
            dataIndex:'jeno',
            autoWidth : true,
            renderer:WtfGlobal.linkDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.writeOff.ReverseJENumber"),
            dataIndex:'reversejeno',
            autoWidth : true,
            renderer:WtfGlobal.linkDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.writeOff.isRecovered"),
            dataIndex:'isrecovered',
            autoWidth : true,
            renderer : function(val){
                if(val){
                    return 'Yes'
                }else {
                    return 'No'
                }
            }
        },{
            header:WtfGlobal.getLocaleText("acc.common.memo"),
            dataIndex:'memo',
            autoWidth : true,
           renderer: function(value) {
                    value = value.replace(/\'/g, "&#39;");
                    value = value.replace(/\"/g, "&#34");
                    return "<span class=memo_custom  wtf:qtip='" + value + "'>" + Wtf.util.Format.ellipsis(value, 60) + "</span>"
                }
        }]
    });
    this.grid.on('cellclick',this.onCellClick, this);
    this.grid.getSelectionModel().on('rowselect',this.onSelection,this);
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.writeOff.SearchByReceiptNoCustNameMemo"), // "Search by Document no, Description, Code, Party / Cost Center ...",
        width: 200,
        id:"quickSearch"+this.panelID,
        field: 'billno'
    });
    
    this.resetBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  // 'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  // 'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
    
    this.RecoverReceipt=new Wtf.Toolbar.Button({
        text:this.isReceipt?WtfGlobal.getLocaleText("acc.writeOff.RecoverReceipt"):WtfGlobal.getLocaleText("acc.writeOff.RecoverPayment") ,
        iconCls:'recoverSalesInvoiceIcon',
        id:'recoverInvoice',        
        tooltip :this.isReceipt?WtfGlobal.getLocaleText("acc.writeOff.RecoverReceipt"):WtfGlobal.getLocaleText("acc.writeOff.RecoverPayment") ,
        style:"padding-left:0px;",
        scope: this,
        disabled : true,  
        hidden: WtfGlobal.EnableDisable(Wtf.UPerm.writeOffReceipts, Wtf.Perm.writeOffReceipts.writeOffRecoverReceipts),
        handler: this.recoverHandler
    });
    
    Wtf.account.writeOffPaymentsReport.superclass.constructor.call(this,config);
    this.addEvents({
        'journalentry':true
    });
    this.on('journalentry',callJournalEntryDetails);
}
Wtf.extend(Wtf.account.writeOffPaymentsReport,Wtf.Panel, {
        onRender: function(config){
        this.userds.load({
            params:{
                start:0,
                limit:30
            }
        });
        
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: [this.quickPanelSearch, this.resetBttn, '-', this.RecoverReceipt],
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.userds,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                    })
                })
            }]
        }); 
        this.add(this.leadpan);
        
        Wtf.account.writeOffPaymentsReport.superclass.onRender.call(this,config);
    },
    
    handleResetClick:function() {
        if(this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchInvoices();
        }
    },
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="jeno"||header=="reversejeno"){
            var accid=this.userds.getAt(i).data['journalentryid'];
            var jestartdate= new Date(this.userds.getAt(i).data['jedate']);
            var jeentrydate=new Date(this.userds.getAt(i).data['jedate']);
            this.fireEvent('journalentry',accid,true,false,null,null,null,jestartdate, jeentrydate);
        }
        if(header=="billno"){
            this.viewTransection(g,i,j);
        }
    },
    viewTransection: function(grid, rowIndex, columnIndex){
        var formrec=null;
        if(rowIndex<0&&this.grid.getStore().getAt(rowIndex)==undefined ||this.grid.getStore().getAt(rowIndex)==null ){
            WtfComMsgBox(15,2);
            return;
        }
        formrec = this.grid.getStore().getAt(rowIndex);
        if (this.isReceipt) {
            callViewPaymentNew(formrec, 'ViewReceivePayment', true, this.grid);
        } else {
            callViewPaymentNew(formrec, 'ViewPaymentMade', false, this.grid,this.pendingapproval);
        }
    },
    
    fetchInvoices:function() {
        this.userds.load();
        this.RecoverReceipt.disable();
    },
    
    recoverHandler: function() {
        Wtf.MessageBox.show({
                    title:WtfGlobal.getLocaleText("acc.common.confirm"),
                    msg:this.isReceipt?WtfGlobal.getLocaleText("acc.writeOff.SureToRecoverReceipts"):WtfGlobal.getLocaleText("acc.writeOff.SureToRecoverPayments"),
                    width:500,
                    buttons: Wtf.MessageBox.YESNO,
                    scope:this,
                    icon: Wtf.MessageBox.INFO,
                    fn: function(btn){
                        if(btn =="yes") {              
                            this.finalSave(); 
                        }else{
                            return;
                        }
                    }
                }, this);   
    },
    finalSave : function(){
        var rec = this.grid.getSelectionModel().getSelected();
        var ajaxUrl = this.isReceipt ? "ACCWriteOff/recoverReceipt.do" : "ACCWriteOff/recoverPayment.do";
        Wtf.Ajax.requestEx({
            url: ajaxUrl,
            params:{
                writeOffId : rec.data.id
            }
        },this,this.genSuccessResponse,this.genFailureResponse);
    },
    genSuccessResponse : function(response,request){
        if(response.success){
            this.userds.reload();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], response.success * 2 + 1);
            var comp = null;
            comp = Wtf.getCmp('receiptReport');
            if(comp){
                comp.fireEvent('paymentupdate');
            }  
            return;
        } else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], response.success * 2 + 1);
            return;
        }
    },
    genFailureResponse: function(response,request){
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    onSelection: function(sm,rowIndex,record){
        if(sm.getSelections().length == 0 || record.data.isrecovered == true){
            this.RecoverReceipt.disable();
        } else {
            this.RecoverReceipt.enable();
        }
    }
    
});


