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



Wtf.account.LinkAdvancePayment = function(config) {
    Wtf.apply(this, config);
    this.accid = this.record.data.personid;
    this.accname = this.record.data.personname;
    this.paymentamount = this.record.data.amount;
    this.paymentdue = this.record.data.paymentamountdue;
    this.paymentNo = this.record.data.billno;
    this.paymentid = this.record.data.billid;
    this.currencyid = config.record.data.currencyid;
    // isRefundTransaction is used to identify that payment is of refund type
    this.isRefundTransaction = this.record.data.isRefundTransaction;
        
//    Wtf.apply(this,{
//        items: [this.newPanel],
//        bbar: [
//        this.saveBttn = new Wtf.Toolbar.Button({
//            text: this.islinkflag ? WtfGlobal.getLocaleText("acc.field.LinkTransaction") : WtfGlobal.getLocaleText("acc.common.saveBtn"),
//            tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
//            scope: this,
//            id: 'saveLinkingInformationButton',
//            iconCls: getButtonIconCls(Wtf.etype.save),
//            handler: function() {
//                this.save();
//            }
//        })]
//    },config);
//    
    Wtf.account.LinkAdvancePayment.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
    
}
Wtf.extend(Wtf.account.LinkAdvancePayment, Wtf.account.ClosablePanel, {
    layout: 'fit',
    initComponent:function (){
        Wtf.account.LinkAdvancePayment.superclass.initComponent.call(this);
        
        this.createNorthForm();
        this.createCenterPanel();
        this.createGrid();
        
        var panelItemsArray = []; 
        panelItemsArray.push(this.NorthForm, this.centerPanel,this.grid);     
    
        this.newPanel = new Wtf.Panel({
            autoScroll: true,
            region: 'center',
            items: panelItemsArray
        });
        this.newPanel.doLayout();
        this.add(this.newPanel);
        this.saveBttn = new Wtf.Toolbar.Button({
            text: this.islinkflag ? WtfGlobal.getLocaleText("acc.field.LinkTransaction") : WtfGlobal.getLocaleText("acc.field.UnLinkTransaction"),
            tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
            scope: this,
            disabled:this.islinkflag?false:true,
            id: 'saveLinkingInformationButton',
            iconCls: getButtonIconCls(Wtf.etype.save),
            handler: function() {
                    this.saveData();
                }
        })
       
        var bbar = [this.saveBttn];
        
        this.elements += ',bbar';
        this.bottomToolbar = bbar;
        this.doLayout();
        this.grid.on('rowclick',this.processRow,this);
        this.grid.on('afteredit',this.checkrecord,this);
        this.grid.on('beforeedit',this.beforeGridEdit,this);
        this.Store.on('load',this.checkIfRecordsExists,this)
        this.Store.on('beforeload',this.storeBeforeLoad,this)
        this.linktransactionfromdate.on('change',this.linkingDateChanged,this)
    },
    onRender: function(config) {
        Wtf.account.LinkAdvancePayment.superclass.onRender.call(this, config);
        this.NorthForm.doLayout();
        this.grid.doLayout();
        this.Store.load();
        this.invoicelinkingyearArr=[];
    },
    createNorthForm:function(){
        this.name=new Wtf.form.TextField({
            fieldLabel:(this.isReceipt?(this.isRefundTransaction?WtfGlobal.getLocaleText("acc.invoiceList.ven"):WtfGlobal.getLocaleText("acc.invoiceList.cust")):(this.isRefundTransaction?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven")))+"*",  //this.businessPerson +'*',
            id:"linkcusven"+this.id,
            hiddenName:'accname',
            scope:this,
            maxLength:45,
            anchor:'85%',
            disabled:true,
            allowBlank:false,
            value:this.accname
        });
        this.name.setValue(this.accname);
        this.no=new Wtf.form.TextField({
            fieldLabel:(this.isReceipt?WtfGlobal.getLocaleText("acc.accPref.autoReceipt"):WtfGlobal.getLocaleText("acc.accPref.autoPayment")) +" "+ WtfGlobal.getLocaleText("acc.cn.9") + "*",  //this.noteType+' Note No*',
            name: 'number',
            scope:this,
            maxLength:45,
            anchor:'85%',
            disabled:true,
            allowBlank:false,
            value:this.paymentNo
        });
        
        this.linktransactionfromdate=new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.linkingDate.date"),//'Linking Date
            name:'fromlinktransactiondate',
            format:WtfGlobal.getOnlyDateFormat(),
            hidden:this.islinkflag?false:true,
            hideLabel :this.islinkflag?false:true,
            scope:this,
            anchor:'85%',
            value:new Date()
//            maxValue:Wtf.account.companyAccountPref.activeDateRangeToDate!=""&& Wtf.account.companyAccountPref.activeDateRangeToDate!=null?new Date(Wtf.account.companyAccountPref.activeDateRangeToDate):""
        });

        this.AmountPayment=new Wtf.form.NumberField({
            name:"cnamount",
            allowBlank:false,
            fieldLabel:(this.isReceipt?WtfGlobal.getLocaleText("acc.field.receivePaymentAmount*"):WtfGlobal.getLocaleText("acc.field.makePaymentAmount*")),
            maxLength:15,
            decimalPrecision:2,
            disabled:true,
            value:this.paymentamount,
            emptyText:WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt"), //"enter amount",
            anchor:'85%'
        });
        
        this.AmountDuePayment=new Wtf.form.NumberField({
            name:"cnamountdue",
            allowBlank:false,
            fieldLabel:(this.isReceipt?WtfGlobal.getLocaleText("acc.field.receivePaymentAmountDue*"):WtfGlobal.getLocaleText("acc.field.makePaymentAmountDue*")),
            maxLength:15,
            decimalPrecision:2,
            disabled:true,
            value:this.paymentdue,
            emptyText:WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt"), //"enter amount",
            anchor:'85%'
        });
        
        this.NorthForm = new Wtf.form.FormPanel({
            region: "north",
            height: 150,
            border: false,
            defaults: {
                border: false
            },
            split: true,
            layout: 'form',
            baseCls: 'northFormFormat',
            disabledClass: "newtripcmbss",
            hideMode: 'display',
            id: this.id + 'Northform',
            cls: "visibleDisabled",
            labelWidth: 140,
            items: [
            {
                layout: 'column',
                defaults: {
                    border: false
                },
                items: [{
                    layout: 'form',
                    columnWidth: 0.5,
                    items: [this.name,this.no,this.linktransactionfromdate]
                }, {
                    layout: 'form',
                    columnWidth: 0.5,
                    items: [this.AmountPayment,this.AmountDuePayment]
                }]
            },{
                    /*Provided Note to Understand,How to Unlink transaction  */
                    xtype: 'panel',
                    style: 'padding-top:50px;',
                    html: this.islinkflag?"": "<span ><b>"+WtfGlobal.getLocaleText("acc.field.Note")+"</b>:"+WtfGlobal.getLocaleText("acc.linkpayment.note")+"</span>"
            }]
         
        });
    },
    createCenterPanel: function(){
        this.centerPanel = new Wtf.Panel({
            region: "center",
            layout: "fit",
            height: 80,
            border:false
        });
    },
    createGrid:function(){
        this.GridRec = Wtf.data.Record.create ([
        {
            name: 'type'
        },
        {
            name:'documentType'  
        },
        {
            name:'documentid'
        },
        {
            name:'linkdetailid'
        },
        {
            name:'date',
            type:'date'
        },
        {
            name:'currencysymbol'
        },
        {
            name:'currencyid'
        },

        {
            name:'documentno'
        },
        {
            name:'date', 
            type:'date'
        },
        {
            name:'invoicelinkingdate', 
            type:'date'
        },
        {
            name:'duedate', 
            type:'date'
        },
        {
            name:'amount'
        },
        {
            name:'linkamount', 
            defaultValue:0
        },
        {
            name:'amountdue'
        },
        {
            name:'amountDueOriginal'
        },
        {
            name:'externalcurrencyrate', 
            defValue: 1
        },
        {
            name:'exchangeratefortransaction'
        },                

        {
            name:'currencyidtransaction'
        },
        {
            name:'currencysymboltransaction'
        },                

        {
            name:'currencyidpayment'
        },                
        {
            name:'currencysymbolpayment'
        }                
        ]);
        
        var urlForStore='';
        if (this.isReceipt) {
            if (this.islinkflag) {
                if (this.isRefundTransaction) {
                    urlForStore = 'ACCVendorPaymentCMN/getAdvanceVendorPaymentForRefunds.do';
                } else {
                    urlForStore = 'ACCReceiptCMN/getDocumentsForLinkingWithAdvanceReceipt.do';
                }
            } else {
                if (this.isRefundTransaction) {
                    urlForStore = 'ACCReceiptCMN/getLinkedDocumentsAgainstRefundReceipt.do'
                } else {
                    urlForStore = 'ACCReceiptCMN/getLinkedDocumentsAgainstAdvance.do';
                }
            }
        } else {
            if (this.islinkflag) {
                if (this.isRefundTransaction) {
                    urlForStore = 'ACCReceiptCMN/getAdvanceCustomerPaymentForRefunds.do';
                } else {
                    urlForStore = 'ACCVendorPaymentCMN/getDocumentsForLinkingWithAdvancePayment.do';
                }
            } else {
                if (this.isRefundTransaction) {
                    urlForStore = 'ACCVendorPaymentCMN/getLinkedDocumentsAgainstRefundPayment.do';
                } else {
                    urlForStore = 'ACCVendorPaymentCMN/getLinkedDocumentsAgainstAdvance.do';
                }
            }
        }
        
        this.Store = new Wtf.data.Store({
            url:urlForStore,
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.GridRec)
        });
        
        
        this.mulDebitCM= new Wtf.grid.ColumnModel([        
        {
            header: WtfGlobal.getLocaleText("acc.field.DocumentType"),
            dataIndex: 'type',
            name:'type'
        },
        {
            header: WtfGlobal.getLocaleText("acc.field.DocumentNumber"),
            dataIndex: 'documentno'
        },{
            header:WtfGlobal.getLocaleText("acc.field.TransactionDate"),            //"Invoice Date",
            dataIndex:'date',
            align:'center',
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.field.OriginalAmountDue"),
            dataIndex:'amountDueOriginal',
            hidelabel:false,
            hidden: false,
            renderer:WtfGlobal.withoutRateCurrencySymbolTransaction
        },{
            header:WtfGlobal.getLocaleText("acc.setupWizard.curEx"), 
            dataIndex:'exchangeratefortransaction',
            hidelabel:false,
            hidden: false,
            renderer:this.conversionFactorRenderer,
            editor: this.islinkflag ? this.exchangeratefortransaction=new Wtf.form.NumberField({
                decimalPrecision:10,
                allowNegative : false,
                validator: function(val) {
                    if (val!=0) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }) : ''
        },{
            header: WtfGlobal.getLocaleText("acc.invoiceList.amtDue"),
            dataIndex: 'amountdue',
            align: 'right',
            sortable:true,
            renderer: function (value, m, rec) {
                var symbol = ((rec == undefined || rec.data.currencysymbolpayment == null || rec.data['currencysymbolpayment'] == undefined || rec.data['currencysymbolpayment'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbolpayment']);
                var v = parseFloat(value);
                if (isNaN(v))
                    return value;
                v = WtfGlobal.conventInDecimal(v, symbol)
                return '<div class="currency">' + v + '</div>';
            }
        },{
            header:WtfGlobal.getLocaleText("acc.dnList.gridAmt"),                                   //WtfGlobal.getLocaleText("acc.invReport.desc"),  //"Description",
            dataIndex:"linkamount",
            align:'center',
            summaryType:'sum',
            editor:this.islinkflag ? new Wtf.form.NumberField({
                allowBlank: false,
                allowNegative:false,
                maxLength:15,
                decimalPrecision:2
            }) : '',
            renderer:function(value,m,rec) {
                var symbol=((rec==undefined||rec.data.currencysymbolpayment==null||rec.data['currencysymbolpayment']==undefined||rec.data['currencysymbolpayment']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbolpayment']);
                var v=parseFloat(value);
                if(isNaN(v)) return value;
                v= WtfGlobal.conventInDecimal(v,symbol)
                return '<div class="currency">'+v+'</div>';
            }
        },{
            header:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
            dataIndex:"delete",
            align:'center',
            width:40,
            hidden: this.islinkflag,
            renderer:function(){
                return "<div class='pwnd delete-gridrow' style='margin-left:40px;'> </div>";
            }
        }]);
       
        this.grid = new Wtf.grid.EditorGridPanel({
            region: 'center',
            clicksToEdit:1,
            height:300,
            store: this.Store,
            cm: this.mulDebitCM,
            border : true,
            disabledClass: "newtripcmbss",
            layout: 'fit',
            cls: 'gridFormat',
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
    },
    processRow:function(grid,rowindex,e){
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                var record = store.getAt(rowindex);
                /**
                 * Push year of linked invoice documents in array to check the year is closed or not.
                 */
                if(record && record.data != undefined && record.data.invoicelinkingdate != undefined && record.data.invoicelinkingdate != "") {
                    this.invoicelinkingyearArr.push(record.data.invoicelinkingdate.getFullYear());
                }
               
                store.remove(store.getAt(rowindex));
                this.saveBttn.enable();
                if(rowindex==total-1){
                //this.addGridRec();
                }
            }, this);
        }
    },
    checkrecord:function(obj){
        var rec=obj.record;
        if(obj.field=="linkamount"){
            if(rec.data.amountdue < obj.value ){
                var msg = '';
                if(this.isReceipt){
                    if (rec.data['type'] == 'Advance Payment') {
                        msg = WtfGlobal.getLocaleText("acc.accPref.autoPayment");
                    } else {
                        msg = rec.data['type']=='Debit Note'?WtfGlobal.getLocaleText("acc.dimension.module.3"):WtfGlobal.getLocaleText("acc.field.CustomerInvoice");
                    }
                } else {
                    if (rec.data['type'] == 'Advance Payment') {
                        msg = WtfGlobal.getLocaleText("acc.accPref.autoReceipt");
                    } else {
                        msg = rec.data['type']=='Credit Note'?WtfGlobal.getLocaleText("acc.dimension.module.4"):WtfGlobal.getLocaleText("acc.agedPay.venInv");
                    }
                }
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Amountenteredcannotbegreaterthan")+" "+msg+" "+WtfGlobal.getLocaleText("acc.field.amountdue")],2);
                obj.cancel=true;
                rec.set("linkamount",0);
            }else{        
                rec.set("linkamount", obj.value);
            }
        }else if (obj.field == 'exchangeratefortransaction') {
            var amountDueOriginal = 0;
            var exchangeRate = 0;
            amountDueOriginal = parseFloat(rec.data.amountDueOriginal);
            exchangeRate = rec.data.exchangeratefortransaction;
            if (exchangeRate != '')
                obj.record.set("amountdue", getRoundedAmountValue(amountDueOriginal * exchangeRate));
        }
            
    },
    getDocumentAmounts:function(){
        var amt=0;
        for(var i=0; i<this.grid.store.getCount();i++){
            amt+=getRoundedAmountValue(this.grid.store.getAt(i).data['linkamount']);
        }
        return amt;
    },
    checkMaxDate:function(){//finding maximum date between document date and paymentdate
        if((this.islinkflag)) {
            var checkdateflag=true;
            var paymentbilldate=this.record.data.billdate;
            var linkingdate=this.linktransactionfromdate.getValue();
            
            //Payment Date is checked
            if(paymentbilldate.getTime()>linkingdate.getTime()){//comparing payment billid with Grids Linked Date
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.EnteredDocumentDategreaterthanLinkingDate")+" "+WtfGlobal.getLocaleText("acc.field.EnteredDocumentDatePaymentDate")+". "+WtfGlobal.getLocaleText("acc.field.changelinkingdate")],2);
                checkdateflag=false;
                return false;
            }            
            //Document Date is checked  
            for(var i=0; i<this.grid.store.getCount();i++){
                if(this.grid.store.getAt(i).data['linkamount']!=0){//if amount is not zero
                    var billdate=this.grid.store.getAt(i).data['date'];
                    if(billdate.getTime()>linkingdate.getTime()){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.EnteredDocumentDategreaterthanLinkingDate")+" "+WtfGlobal.getLocaleText("acc.field.EnteredDocumentDatePaymentDate")+". "+WtfGlobal.getLocaleText("acc.field.changelinkingdate")],2);
                        checkdateflag=false;
                        return false;
                    }
                }
            }
            if(checkdateflag){
                return true; 
            }                
        } 
    },
    saveData:function(){
        this.saveBttn.disable();
        if((this.islinkflag && this.grid.store.getCount()>0) || !this.islinkflag) {
            var rec=this.NorthForm.getForm().getValues();
            var documentids="";
            var amounts="";
            var documentnos="";
            if(this.islinkflag) {
                if (!isFromActiveDateRange(this.linktransactionfromdate.getValue())) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.activeDateRangePeriod.transactionCannotbeCompleted.alert")], 2);
                    this.saveBttn.enable();
                    return;
                }
                    
                var amt = this.getDocumentAmounts();                  
                amt = getRoundedAmountValue(amt);
                if(amt==0){
                    var alertmsg="";
                    if(this.isRefundTransaction){
                           alertmsg = WtfGlobal.getLocaleText("acc.field.EnteredAmountForadvanceshouldbegraeterthanzero")
                    } else {
                        if(this.isReceipt){
                           alertmsg = WtfGlobal.getLocaleText("acc.field.EnteredAmountForinvoiceordnshouldbegraeterthanzero")
                        } else {
                           alertmsg = WtfGlobal.getLocaleText("acc.field.EnteredAmountForinvoiceorcnshouldbegraeterthanzero")
                        }
                    }
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),alertmsg],2);
                    this.saveBttn.enable();
                    return;
                }
                
                var msg = this.isReceipt?WtfGlobal.getLocaleText("acc.accPref.autoRP"):WtfGlobal.getLocaleText("acc.accPref.autoMP");
                if(amt > this.AmountDuePayment.getValue()) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Amountenteredcannotbegreaterthan")+" "+msg+" "+WtfGlobal.getLocaleText("acc.field.amountdue")],2);
                    this.saveBttn.enable();
                    return;
                }
                /*Checking Document Date & Payment Date with Linking Date*/
                var validateflag=this.checkMaxDate();
                if(validateflag){
                    rec.linkingdate=WtfGlobal.convertToGenericDate(this.linktransactionfromdate.getValue());//maximum linking date
                }else{
                    this.saveBttn.enable();
                    return;
                }
            }
            var url = "";
            if(this.islinkflag) {
                if(this.isReceipt) {
                    url = "ACC" + (this.isCustBill?"Receipt/linkBillingReceipt":"ReceiptNew/linkReceiptToDocuments") + ".do";
                } else {
                    url = "ACC" + (this.isCustBill?"VendorPayment/linkBillingPayment":"VendorPaymentNew/linkPaymentToDocuments") + ".do";
                }
            } else {
                if (this.isReceipt)
                    url = "ACCReceiptNew/unlinkReceiptDocuments.do";
                else
                    url = "ACCVendorPaymentNew/unlinkPaymentDocuments.do";
            }
            var paymentid = this.record.data.billid;
            
            for(var i=0; i<this.grid.store.getCount();i++){
                if(i!=0){
                    documentids+=",";
                    amounts+=",";
                    documentnos+=",";
                }
                documentids+=this.grid.store.getAt(i).data['billid'];
                amounts+=this.grid.store.getAt(i).data['linkamount'];
                documentnos+=this.grid.store.getAt(i).data['documentno'];                
            }
            rec.documentids=documentids;
            rec.amounts=amounts;
            rec.paymentid=paymentid;
	    rec.paymentno=this.record.data.billno;    //SDP-13011
            rec.documentnos=documentnos;
            rec.linkdetails=this.getSelectedRecords();
            rec.isRefundTransaction = this.isRefundTransaction;
            rec.invoicelinkingyearArr = this.invoicelinkingyearArr.toString();
            Wtf.Ajax.requestEx({
                url:url,
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
        } else if(this.grid.store.getCount()==0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.recordsnotavailable")],2);
            this.saveBttn.enable();
            return;
        } 
    },
    genSuccessResponse:function(response){
        if(response.success){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.msg],response.success*2+1);
            this.linktransactionfromdate.disable();
            this.grid.disable();
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],2);
            this.grid.enable();
            this.saveBttn.enable();
            this.isClosable= true; 
        }
    },
    genFailureResponse:function(response){
        /*
         * If any exception occurs during linking, 'Save button' is enabled again
         */
        Wtf.getCmp('saveLinkingInformationButton').enable();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    getSelectedRecords: function() {
        var arr = [];
        this.Store.each(function(record){
            arr.push(this.Store.indexOf(record));
        },this);
        var jarray = WtfGlobal.getJSONArray(this.grid, true, arr);
        return jarray;
    },
    beforeGridEdit: function(obj){
        if(obj.field=='billid'){
            obj.cancel=true;          // If combobox is set disabled, it is getting shadowed when user is trying to change invoice. so we have restricted it form changing
        } else if(obj.field=='exchangeratefortransaction'){
            if(!this.isExchangeRateEditableForSelectedDocumentType(obj))
                obj.cancel=true;
        }
    },
    conversionFactorRenderer:function(value,meta,record) {
        /*
         * currencysymboltransaction = currency symbol of either invoice or DN
         * currencysymbolpayment = currency symbol of receipt
         */
        var currencysymboltransaction=((record==undefined||record.data.currencysymboltransaction==null||record.data['currencysymboltransaction']==undefined||record.data['currencysymboltransaction']=="")?WtfGlobal.getCurrencySymbol():record.data['currencysymboltransaction']);
        var currencysymbolpayment=((record==undefined||record.data.currencysymbolpayment==null||record.data['currencysymbolpayment']==undefined||record.data['currencysymbolpayment']=="")?currencysymbol:record.data['currencysymbolpayment']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
        return "1 "+ currencysymboltransaction +" = " +value+" "+currencysymbolpayment;
    },
    isExchangeRateEditableForSelectedDocumentType: function (e) {
        if (e.record.data.currencysymboltransaction == e.record.data.currencysymbolpayment) {
            return false;
        } else {
            return true;
        }
    },
    
    checkIfRecordsExists: function() {
        if (this.Store.getCount() == 0) {
            this.saveBttn.disable();
            if (this.islinkflag) {
                // if records are not exist for linking then show message
                if (this.isReceipt && this.isRefundTransaction) {
                    WtfComMsgBox(["Info", WtfGlobal.getLocaleText("acc.rpLinking.noAdvancePaymentsExistsForLinking")], 2);
                } else if (!this.isReceipt && this.isRefundTransaction) {
                    WtfComMsgBox(["Info", WtfGlobal.getLocaleText("acc.mpLinking.noAdvancePaymentsExistsForLinking")], 2);
                } else {
                    WtfComMsgBox(["Info", WtfGlobal.getLocaleText(this.isReceipt ? "acc.rpLinking.noInvoicesExistsForLinking" : "acc.mpLinking.noInvoicesExistsForLinking")], 2);
                }
            } else {
                // if records are not exist for unlinking then show message
                if (this.isReceipt && this.isRefundTransaction) {
                    WtfComMsgBox(["Info", WtfGlobal.getLocaleText("acc.field.noLinkedReceiptAdvancePayment")], 2);
                } else {
                    WtfComMsgBox(["Info", WtfGlobal.getLocaleText(this.isReceipt ? "acc.field.NoLinkedReceiptInvoices" : "acc.field.NoLinkedPaymentInvoices")], 2);
                }
            }
        }else{
            this.saveBttn.enable();
        }
    },
    
    storeBeforeLoad:function(){
        var currentBaseParams = this.Store.baseParams;
        //Parameters for fetching invoices

        currentBaseParams.deleted=false;
        currentBaseParams.nondeleted=true;
        currentBaseParams.cashonly=false;
        currentBaseParams.creditonly=false;
        currentBaseParams.onlyAmountDue=true;
        currentBaseParams.isReceipt=this.isReceipt;
        currentBaseParams.currencyfilterfortrans = this.currencyid;
        currentBaseParams.accid=this.accid;
        currentBaseParams.includeFixedAssetInvoicesFlag=true;
        currentBaseParams.bills = this.record!=undefined ? this.record.data.billid : "",
        currentBaseParams.upperLimitDate=WtfGlobal.convertToGenericDate(this.linktransactionfromdate.getValue()); // Form the Linking Date  Ref SDP-1915
        currentBaseParams.isEdit=false;
        //Parameters for fetching debit notes
        currentBaseParams.cntype = 8;
        currentBaseParams.isNewUI = true;
        currentBaseParams.isVendor = !this.isCustomer;
        currentBaseParams.vendorid = this.accid;
        currentBaseParams.customerid =  this.accid;
        currentBaseParams.custVendorID = this.accid;
        currentBaseParams.ignorecustomers = true;
        currentBaseParams.ignorevendors = true;
        currentBaseParams.billId = this.record!=undefined ? this.record.data.billid : "";
        currentBaseParams.isNoteForPayment = true;
        currentBaseParams.isRefundLinking = this.isRefundTransaction //Parameters for transaction for refund linking
        if( Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && !this.isReceipt && this.islinkflag && !this.isRefundTransaction && this.record.data.paymentwindowtype!=undefined && this.record.data.paymentwindowtype ===1 && this.record.data.rcmApplicable!=undefined){
            /**
             * Link Invoices with Advance Payment. 
             * If RCM applicable check is activated in payment 
             * then only RCM applicable invoices can be linked.
             */
            currentBaseParams.rcmApplicable = this.record.data.rcmApplicable;
        }
        this.Store.baseParams=currentBaseParams;
    },
    linkingDateChanged:function(){
        this.Store.load();
    }
});
        

