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
Wtf.account.WriteOffInvoicesWindow = function(config) {
    this.isCustomer= config.isCustomer,
    this.butnArr = [];
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.writeOff.SearchByInvNoCustNameAcName"),  //'Search by Invoice No, Customer Name, Account Name'
        width: 200,
        id:"quickSearch"+config.id
    });
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
    this.startDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'startdate',
        style:"margin-left: 15px;",
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true)
    });
    this.endDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        style:"margin-left: 15px;",
        name:'enddate',
        value:Wtf.serverDate

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
            if(newval.getTime()>Wtf.serverDate.getTime()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.writeOffInvoice.endDateCanNotBeFutureDate")],2);
                this.endDate.setValue(Wtf.serverDate);
            }
        }
    },this);
        
    this.fetchBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
        tooltip:this.isCustomer?WtfGlobal.getLocaleText("acc.writeOff.selectTimePeriodSaleInvoiceTT"):WtfGlobal.getLocaleText("acc.writeOff.selectTimePeriodPurchaseInvoiceTT"), 
        style:"margin-left: 15px;",
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.loadStore                        
    }); 
    this.writeOffInvoices = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.button.writeOffInvoices"),  //'Write Off Invoice',
        tooltip:WtfGlobal.getLocaleText("acc.button.writeOffInvoices"),
        style:"margin-left: 15px;",
        iconCls:'writeOffSelectedSalesInvoice',
        scope:this,
        disabled:true,
        handler:this.writeOffselectedInvoices                        
    });
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"), //'Cancel',
        scope: this,
        handler: function() {
            this.close();
        }
    });
    Wtf.apply(this, {
        buttons: this.butnArr
    }, config);
    Wtf.account.WriteOffInvoicesWindow.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.WriteOffInvoicesWindow, Wtf.Window, {
    height: 490,
    width: 800,
    modal: true,
    iconCls : 'writeOffSalesInvoice',
    onRender: function(config) {
        Wtf.account.WriteOffInvoicesWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();
        var msg = '';
        msg=msg+ this.isCustomer ? '<b>'+WtfGlobal.getLocaleText("acc.writeOff.salesInvoice")+'</b>':'<b>'+WtfGlobal.getLocaleText("acc.writeOff.purchaseInvoice")+'</b>';
        var isgrid = true;
        this.add({
            region: 'north',
            height: 115,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml('', msg, "../../images/Accounts_Receivable/Write-Off-icon-big.png", isgrid)
        }, this.centerPanel = new Wtf.Panel({
            border: false,
            region: 'center',
            id: 'centerpan' + this.id,
            autoScroll: true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls: 'bckgroundcolor',
            layout: 'fit',
            items: [this.grid],
            tbar:[this.quickPanelSearch,this.resetBttn,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),this.endDate,this.fetchBttn,this.writeOffInvoices],
            bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.store,
                searchField: this.quickPanelSearch,
                displayInfo: true,
                displayMsg: 'Displaying records {0} - {1} of {2}',
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                })
            })
        }))
    },
    createDisplayGrid: function() {
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            });
        this.sm.on('rowselect',this.onSelection,this)    ;
        this.sm.on('rowdeselect',this.onSelection,this)    ;
        this.cm = new Wtf.grid.ColumnModel([this.sm, {
            header: WtfGlobal.getLocaleText("acc.agedPay.gridIno"), //"Invoice Number"
            dataIndex: 'documentno'
        },{
            header: this.isCustomer?WtfGlobal.getLocaleText("acc.agedPay.cus"):WtfGlobal.getLocaleText("acc.agedPay.ven "),  
            dataIndex: 'personname',
            autoWidth:true
        },{
            header: WtfGlobal.getLocaleText("acc.rem.34"), //"Invoice Date"
            dataIndex: 'date',
            align:'center',
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },
        {
            header: WtfGlobal.getLocaleText("acc.het.53"), //"Amount"
            dataIndex: 'amount',
            align:'right',
            renderer:WtfGlobal.withoutRateCurrencySymbolTransaction
        },
        {
            header: WtfGlobal.getLocaleText("acc.mp.amtDue"), //"Amount Due"
            dataIndex: 'amountDueOriginal',
            align:'right',
            renderer:WtfGlobal.withoutRateCurrencySymbolTransaction
        }]);


        this.Rec = Wtf.data.Record.create([
        {name: 'documentno',mapping : 'billno'},
        {name: 'date',type: 'date'},
        {name: 'amount',type:'float'},
        {name: 'amountdue',type:'float'},
        {name: 'exchangeratefortransaction'},
        {name: 'currencysymboltransaction'},
        {name: 'currencyidtransaction'},
        {name: 'currencynametransaction'},
        {name: 'currencyid'},
        {name: 'billid'},
        {name: 'amountDueOriginal',type:'float'},
        {name: 'amountDueOriginalSaved',type:'float'},
        {name: 'accountid'},
        {name :'personname'}
        ]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.Rec),
            url: this.isCustomer ? "ACCInvoiceCMN/getInvoicesForWriteOff.do" : "ACCGoodsReceiptCMN/getInvoicesForWriteOff.do",
            baseParams: {
                onlyAmountDue: true,
                deleted: false,
                nondeleted: true,
                upperLimitDate:WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                includeFixedAssetInvoicesFlag:true
            }
        });
        
        this.store.on("loadexception", function() {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.mp.unableToLoadData")],1);
        }, this)
        this.store.on('load',this.storeLoaded,this);
        this.store.on('beforeload',function(store,option){
            var currentBaseParams = this.store.baseParams;
            currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());   
            currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
            this.store.baseParams=currentBaseParams;
        },this);
        this.store.on('datachanged', function(){        
            var p = this.pP?this.pP.combo.value:30;        
            this.quickPanelSearch.setPage(p);
        }, this);
        var InvoiceStoreParams = {
            ss: this.quickPanelSearch.getValue(),
            start:0,
            limit:30
        }
                              
        //      
        this.store.load({
            params: InvoiceStoreParams
        });
        this.grid = new Wtf.grid.GridPanel({
            store: this.store,
            height: 235,
            autoScroll: true,
            cm: this.cm,
            sm: this.sm,
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.getLocaleText("acc.common.norec")
            }
        });

    },
    getSelectedRecords: function() {
        var arr = [];
        var selectionArray=this.grid.getSelectionModel().getSelections();
        for( var i=0;i<selectionArray.length;i++){
            arr.push(this.store.indexOf(selectionArray[i]));
        }
        var jarray = WtfGlobal.getJSONArrayWithoutEncoding(this.grid, true, arr);
        return jarray;
    },
    handleResetClick:function() {
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.store.load();
        }
    },
    loadStore:function(){
        this.store.reload();
    },
    storeLoaded : function() {
        this.writeOffInvoices.disable();
        this.quickPanelSearch.StorageChanged(this.store);
    },
    writeOffselectedInvoices: function(){
         Wtf.MessageBox.show({
                    title:WtfGlobal.getLocaleText("acc.common.confirm"),
                    msg:WtfGlobal.getLocaleText("acc.writeOff.SureToWriteOff"),
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
        var dataToSave = this.getSelectedRecords();
        this.remarkWin = new Wtf.Window({
            height: 215,
            width: 360,
            title: WtfGlobal.getLocaleText("acc.common.memo"), 
            bodyStyle: 'padding:5px;background-color:#f1f1f1;',
            autoScroll: true,
            layout: 'fit',
            items: [{
                border: false,
                layout: 'form',
                autoScroll :true,
                bodyStyle: 'padding:5px;',
                items: [this.writeOffDate=new Wtf.form.DateField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.writeOff.writeOffDate"),  //'Write off date'
                    name:'writeOffDate',
                    format:WtfGlobal.getOnlyDateFormat(),
                    value:new Date()
                }),this.remarkField = new Wtf.form.TextArea({
                    fieldLabel: WtfGlobal.getLocaleText("acc.writeOff.AddMemo"),
                    width: 200,
                    height: 100,
                    maxLength: 2048
                })]
            }],
            modal: true,
            buttons: [{
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                scope: this,
                handler: function() {
                    var ajaxUrl = this.isCustomer ? "ACCWriteOff/writeOffSalesInvoices.do" : "ACCWriteOff/writreOffGoodsReceipts.do";
                    Wtf.Ajax.requestEx({
                        url: ajaxUrl,
                        params:{
                            invoiceDetails : dataToSave,
                            memo : this.remarkField.getValue(),
                            writeOffDate: WtfGlobal.convertToGenericDate(this.writeOffDate.getValue())
                        }
                    },this,this.genSuccessResponse,this.genFailureResponse);
                    this.remarkWin.close();
                }
            }, {
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope: this,
                handler: function() {
                    this.remarkWin.close();
                }
            }]
        });
        this.writeOffDate.on('change',this.onWriteOffDateChange,this);
        this.remarkWin.show();
    },
    genSuccessResponse : function(response,request){
        if(response.success){
            this.store.reload();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], response.success * 2 + 1);
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
        if(sm.getSelections().length == 0){
            this.writeOffInvoices.disable();
        } else {
            this.writeOffInvoices.enable();
        }
    },
    onWriteOffDateChange:function(field,val,oldVal){
        if(val==''){         // Write off date is mandatory
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.writeOff.writeOffDateCanNotBeEmpty")], 2);
            field.setValue(oldVal);
            return;
        } else {
            // Write off date can not be older than invoice date of any of the selected receipts.
            val = val.setHours(0,0,0,0);
            var invoiceDate=null;
            var rec=null;
            var today = new Date();
            today = today.setHours(0,0,0,0);
            // Write off can not be future date
            if(today<val){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.writeOffInvoiceAndReceipt.writeOffDateCanNotBeFutureDate")], 2);
                field.setValue(oldVal);
                return;
            }
            var selectedRecords = [];
            selectedRecords = this.grid.getSelectionModel().getSelections();
            for(var x=0;x<selectedRecords.length;x++){
                rec = selectedRecords[x];
                invoiceDate = rec.data.date;
                invoiceDate = invoiceDate.setHours(0, 0, 0, 0);
                if(val<invoiceDate){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.writeOffInvoice.writeOffDateNotValid")], 2);
                    field.setValue(oldVal);
                    break;
                }
            }
        }
    }
    
});
