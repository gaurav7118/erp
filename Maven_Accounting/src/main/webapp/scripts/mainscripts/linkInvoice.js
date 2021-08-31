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

//Component to link make / receive payment with customer / vendor invoice
Wtf.account.linkInvoice = function(config){
    this.reconRec=(config.reconRec==undefined?"":config.reconRec);
    this.isGST = (config.isGST==undefined?false:config.isGST);
    this.record = config.record;
    this.currencyid = config.record.data.currencyid;
    this.isReceipt = config.isReceipt;
    this.isCustBill=config.isCustBill;
    this.reloadGrid = config.reloadGrid;
     Wtf.apply(this,{
        title: this.isReceipt?WtfGlobal.getLocaleText("acc.field.LinkReceipttoCustomerInvoice"):WtfGlobal.getLocaleText("acc.field.LinkPaymenttoVendorInvoice"),
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.field.LinkTransaction"),
            scope: this,
            handler: this.saveData.createDelegate(this)
        },{
            text: WtfGlobal.getLocaleText("acc.common.close"),
            scope: this,
            handler:this.closeWin.createDelegate(this)
        }]
    },config);
    Wtf.account.linkInvoice.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}
Wtf.extend( Wtf.account.linkInvoice, Wtf.Window, {

    onRender: function(config){
        var image="../../images/accounting_image/bank-reconciliation.jpg";
        Wtf.account.linkInvoice.superclass.onRender.call(this, config);
        this.createStore();
        this.createForm();
        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:this.isReceipt?getTopHtml(WtfGlobal.getLocaleText("acc.field.LinkReceipttoCustomerInvoice"),'',image,false):getTopHtml(WtfGlobal.getLocaleText("acc.field.LinkPaymenttoVendorInvoice"),'',image,false)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
//            layout: 'fit',
            height:100,
            items:this.Form
        });
   },
   createStore:function(){
	   
           this.GridRec = Wtf.data.Record.create ([
                {name:'billid'},
                {name:'journalentryid'},
                {name:'entryno'},
                {name:'billto'},
                {name:'discount'},
                {name:'currencysymbol'},
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
                {name:'amount'},
                {name:'amountdue'},
                {name:'termdays'},
                {name:'termname'},
                {name:'incash'},
                {name:'taxamount'},
                {name:'taxid'},
                {name:'orderamountwithTax'},
                {name:'taxincluded',type:'boolean'},
                {name:'taxname'},
                {name:'deleted'},
                {name:'amountinbase'},
                {name:'memo'},
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
                {name:'startDate', type:'date'},
                {name:'nextDate', type:'date'},
                {name:'expireDate', type:'date'},
                {name:'repeateid'},
                {name:'status'}
            ]);

            if(this.isReceipt)
                this.StoreUrl = "ACC" + (this.isCustBill?"InvoiceCMN/getBillingInvoices":"InvoiceCMN/getInvoices") + ".do";
            else
                this.StoreUrl = "ACC" + (this.isCustBill?"GoodsReceiptCMN/getBillingGoodsReceipts":"GoodsReceiptCMN/getGoodsReceipts") + ".do";

       this.Store = new Wtf.data.Store({
            url:this.StoreUrl,
            baseParams:{
                deleted:false,
                nondeleted:true,
                cashonly:false,
                creditonly:false
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.GridRec)
        });
        
        
        if(this.isReceipt){
            Wtf.customerAccStore.removeAll();
            Wtf.customerAccStore.load({params:{comboCurrencyid:this.record.data.currencyid}});
        } else {
            Wtf.vendorAccStore.removeAll();
            Wtf.vendorAccStore.load({params:{comboCurrencyid:this.record.data.currencyid}});
        }
        var a = Wtf.vendorAccStore.getAt(0);
   },
   
   createForm:function(){

       this.name=new Wtf.form.FnComboBox({
            fieldLabel:(this.isReceipt?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven"))+"*",  //this.businessPerson +'*',
            id:"linkcusven",
            hiddenName:'accid',
            store:this.isReceipt?Wtf.customerAccStore:Wtf.vendorAccStore,
            valueField:'accid',
            scope:this,
            noAddNew: true,
            anchor:'85%',
            displayField:'accname',
            hidden : this.record.data.isadvancepayment,
            hideLabel : this.record.data.isadvancepayment,
            emptyText:this.isReceipt?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven"),  //'Please Select a '+this.businessPerson+'...',
            forceSelection: true,
            allowBlank:this.record.data.isadvancepayment//,
            //hirarchical:true
        });
        if(this.record.data.isadvancepayment){           
            if(this.isReceipt) {
                Wtf.customerAccStore.on("load",function(){
                    this.Store.load({params:{accid:this.record.data.personid,minimumAmountDue:this.record.data.amount,currencyfilterfortrans : this.currencyid}});this.Account.setDisabled(false);
                },this)
            } else {
                Wtf.vendorAccStore.on("load",function(){
                    this.Store.load({params:{accid:this.record.data.personid,minimumAmountDue:this.record.data.amount,currencyfilterfortrans : this.currencyid}});this.Account.setDisabled(false);
                },this)
            }
        }
        this.name.on('select',function(){this.Store.load({params:{accid:this.name.getValue(),minimumAmountDue:this.record.data.amount,currencyfilterfortrans : this.currencyid}});this.Account.setDisabled(false);},this)
    	
        this.Account= new Wtf.form.ComboBox({
            fieldLabel:(this.isReceipt?WtfGlobal.getLocaleText("acc.field.CustomerInvoice"):WtfGlobal.getLocaleText("acc.agedPay.venInv"))+"*",
            hiddenName:"linkInvoice",
            anchor:"85%",
            labelWidth:200,
            store: this.Store,
            valueField:'billid',
            disabled:true,
            displayField:'billno',
            allowBlank:false,
            hirarchical:true,
            emptyText:this.isReceipt?WtfGlobal.getLocaleText("acc.field.SelectaCustomerInvoice"):WtfGlobal.getLocaleText("acc.field.SelectaVendorInvoice"),
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            triggerAction:'all',
            //value:'1',
            scope:this
        });
       
        this.Form=new Wtf.form.FormPanel({
            region:'north',
            height:100, //(Wtf.isIE)?220:190,
            border:false,
            bodyStyle: 'overflow: hidden',
            items:[{
                layout:'form',
                bodyStyle: "background: transparent; padding: 20px;",
                labelWidth:100,
                border:false,
                height:100,
                items:[this.name,this.Account]
           }]
        });
    },


    closeWin:function(){
         this.fireEvent('cancel',this)
         this.close();
         if(this.isReceipt){
                Wtf.customerAccStore.load();
            } else {
                Wtf.vendorAccStore.load();
            }
     },

    saveData:function(){
    	if(this.Form.getForm().isValid()) {
            var url = "";
            if(this.isReceipt)
                url = "ACC" + (this.isCustBill?"Receipt/linkBillingReceipt":"Receipt/linkReceipt") + ".do";
            else
                url = "ACC" + (this.isCustBill?"VendorPayment/linkBillingPayment":"VendorPayment/linkPayment") + ".do";

            var paymentid = this.record.data.billid;
            var invoiceid = this.Account.getValue();
            var paymentno = this.record.data.billno;
            var invoiceno = this.Account.lastSelectionText;
            this.Store.findBy( function(rec){
                        var invoiceidtemp=rec.data['billid'];
                        if(invoiceidtemp==invoiceid)
                            this.creationDate = rec.data['date'];
                    }, this);
            Wtf.Ajax.requestEx({
                   url:url,
                   params: {paymentid : paymentid,
                            invoiceid : invoiceid,
                            invoiceno : invoiceno,
                            paymentno : paymentno,
                            creationdate : WtfGlobal.convertToGenericDate(this.creationDate)}
            },this,this.genSuccessResponse,this.genFailureResponse);

    	}
    },

    genSuccessResponse:function(response){
        this.close();
         
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.msg],response.success*2+1);
          
        (function(){
            Wtf.getCmp(this.reloadGrid).store.reload();
            if(this.isReceipt){
                Wtf.customerAccStore.load();
            } else {
                Wtf.vendorAccStore.load();
            }
        }).defer(WtfGlobal.gridReloadDelay(),this);
    },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }
    
});

Wtf.account.linkInvoicePayment = function(config){
    this.reconRec=(config.reconRec==undefined?"":config.reconRec);
    this.isGST = (config.isGST==undefined?false:config.isGST);
    this.record = config.record;
    this.currencyid = config.record.data.currencyid;
    this.isReceipt = config.isReceipt;
    this.isCustBill=config.isCustBill;
    this.reloadGrid = config.reloadGrid;
    this.accid = this.record.data.personid;
    this.accname = this.record.data.personname;
    this.paymentamount = this.record.data.amount;
    this.paymentdue = this.record.data.paymentamountdue;
    this.paymentNo = this.record.data.billno;
    this.paymentid = this.record.data.billid;
    
     Wtf.apply(this,{
        title: this.isReceipt?WtfGlobal.getLocaleText("acc.field.LinkReceipttoCustomerInvoice"):WtfGlobal.getLocaleText("acc.field.LinkPaymenttoVendorInvoice"),
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.field.LinkTransaction"),
            scope: this,
            handler: this.saveData.createDelegate(this)
        },{
            text: WtfGlobal.getLocaleText("acc.common.close"),
            scope: this,
            handler:this.closeWin.createDelegate(this)
        }]
    },config);
    Wtf.account.linkInvoicePayment.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}
Wtf.extend( Wtf.account.linkInvoicePayment, Wtf.Window, {

    onRender: function(config){
        var image="../../images/accounting_image/bank-reconciliation.jpg";
        Wtf.account.linkInvoice.superclass.onRender.call(this, config);
        if(this.isReceipt){
                Wtf.customerAccStore.load();
            } else {
                Wtf.vendorAccStore.load();
            }
        this.createForm();
        this.createDisplayGrid();
        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:this.isReceipt?getTopHtml(WtfGlobal.getLocaleText("acc.field.LinkReceipttoCustomerInvoice"),'',image,false):getTopHtml(WtfGlobal.getLocaleText("acc.field.LinkPaymenttoVendorInvoice"),'',image,false)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
//            layout: 'fit',
            height:100,
             items:[this.Form,this.grid]
        });
   },
   
   createForm:function(){      
       this.name=new Wtf.form.FnComboBox({
            fieldLabel:(this.isReceipt?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven"))+"*",  //this.businessPerson +'*',
            id:"linkcusven",
            hiddenName:'accid',
            store:this.isReceipt?Wtf.customerAccStore:Wtf.vendorAccStore,
            valueField:'accid',
            scope:this,
            noAddNew: true,
            anchor:'85%',
            displayField:'accname',
            disabled:this.record.data.isadvancepayment?true:false,
//            hidden : this.record.data.isadvancepayment,
//            hideLabel : this.record.data.isadvancepayment,
            emptyText:this.isReceipt?WtfGlobal.getLocaleText("acc.inv.cus"):WtfGlobal.getLocaleText("acc.inv.ven"),  //'Please Select a '+this.businessPerson+'...',
            forceSelection: true,
            allowBlank:this.record.data.isadvancepayment//,
            //hirarchical:true
        });
        if(this.record.data.isadvancepayment){
            this.name.setValue(this.accid);
        if(this.isReceipt) {
                Wtf.customerAccStore.on("load",function(){
                    this.name.setValue(this.accid);
                    this.Store.load({params:{accid:this.record.data.personid,currencyfilterfortrans : this.currencyid}});
                    this.Account.setDisabled(false);
                },this);
        } else {
                Wtf.vendorAccStore.on("load",function(){
                    this.name.setValue(this.accid);
//                    this.Store.load({params:{accid:this.record.data.personid,minimumAmountDue:this.record.data.amount,currencyfilterfortrans : this.currencyid}});
                    this.Store.load({params:{accid:this.record.data.personid,currencyfilterfortrans : this.currencyid}});
                    this.Account.setDisabled(false);
                },this);
        }
        }
        this.name.on('select',function(){
            this.Store.load({params:{accid:this.name.getValue(),currencyfilterfortrans : this.currencyid}});
            this.Account.setDisabled(false);
        },this);
        
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
        
        this.AmountPayment=new Wtf.form.NumberField({
            name:"cnamount",
            allowBlank:false,
            fieldLabel:(this.isReceipt?WtfGlobal.getLocaleText("acc.field.receivePaymentAmount*"):WtfGlobal.getLocaleText("acc.field.makePaymentAmount*")),//WtfGlobal.getLocaleText("acc.mp.11"),  //"Amount*",//in "+WtfGlobal.getCurrencySymbolForForm()+"*",
//            id:"amount"+this.id,
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
            fieldLabel:(this.isReceipt?WtfGlobal.getLocaleText("acc.field.receivePaymentAmountDue*"):WtfGlobal.getLocaleText("acc.field.makePaymentAmountDue*")),//WtfGlobal.getLocaleText("acc.mp.11"),  //"Amount*",//in "+WtfGlobal.getCurrencySymbolForForm()+"*",
//            id:"amount"+this.id,
            maxLength:15,
            decimalPrecision:2,
            disabled:true,
            value:this.paymentdue,
            emptyText:WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt"), //"enter amount",
            anchor:'85%'
        });
    	
        var InvoiceHelp = this.isReceipt?WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.field.receivePaymentcanbeapplytotheinvoiceswithsamecurrency")):WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.field.makePaymentcanbeapplytotheinvoiceswithsamecurrency"));
       
        this.Form=new Wtf.form.FormPanel({
            region:'north',
            border:false,
//            bodyStyle: 'overflow: hidden',
            items:[{
                layout:'form',
                bodyStyle: "background: transparent; padding: 20px;",
                labelWidth:100,
                border:false,
                items:[this.name,this.no,this.AmountPayment,this.AmountDuePayment]  //this.Account,this.AmountInv,this.Amount
           }]
        });
    },

      createDisplayGrid:function(){
        
         this.GridRec = Wtf.data.Record.create ([
                {name:'billid'},
                {name:'invoicedate',type:'date'},
                {name:'journalentryid'},
                {name:'entryno'},
                {name:'billto'},
                {name:'discount'},
                {name:'currencysymbol'},
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
                {name:'amount'},
                {name:'invamount', defaultValue:0},
                {name:'amountdue'},
                {name:'termdays'},
                {name:'termname'},
                {name:'incash'},
                {name:'taxamount'},
                {name:'taxid'},
                {name:'orderamountwithTax'},
                {name:'taxincluded',type:'boolean'},
                {name:'taxname'},
                {name:'deleted'},
                {name:'amountinbase'},
                {name:'memo'},
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
                {name:'startDate', type:'date'},
                {name:'nextDate', type:'date'},
                {name:'expireDate', type:'date'},
                {name:'repeateid'},
                {name:'status'}                
            ]);

            if(this.isReceipt)
                this.StoreUrl = "ACC" + (this.isCustBill?"InvoiceCMN/getBillingInvoices":"InvoiceCMN/getInvoices") + ".do";
            else
                this.StoreUrl = "ACC" + (this.isCustBill?"GoodsReceiptCMN/getBillingGoodsReceipts":"GoodsReceiptCMN/getGoodsReceipts") + ".do";

       this.Store = new Wtf.data.Store({
            url:this.StoreUrl,
            baseParams:{
                deleted:false,
                nondeleted:true,
                cashonly:false,
                creditonly:false,
                onlyAmountDue:true,
                currencyfilterfortrans : this.currencyid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.GridRec)
        });
        
//        this.Store.load();
          if(this.record.data.isadvancepayment){
              this.Store.load({params:{accid:this.record.data.personid,currencyfilterfortrans : this.currencyid}});
          }      

//        this.Store.on('load',this.addGridRec,this);
        this.mulDebitCM= new Wtf.grid.ColumnModel([{
            header:this.isReceipt?WtfGlobal.getLocaleText("acc.field.CustomerInvoice"):WtfGlobal.getLocaleText("acc.agedPay.venInv"),
            dataIndex:'billid',
            width:150,
            hidden : this.isAdvPayment,
            align:"center",
            editor: this.Account= new Wtf.form.ComboBox({
//                fieldLabel:(this.isReceipt?"Customer Invoice":"Vendor Invoice")+"*"+InvoiceHelp,
                hiddenName:"linkInvoice",
                minChars:1,
                listWidth :300,
    //            labelWidth:200,
                store: this.Store,
                valueField:'billid',
    //            disabled:true,
                displayField:'billno',
                allowBlank:false,
                hirarchical:true,
    //            emptyText:this.isReceipt?'Select a Customer Invoice':'Select a Vendor Invoice',
                mode: 'local',
                typeAhead: true,
                typeAheadDelay:30000,
                align:"center",
                forceSelection: true,
                selectOnFocus:true,
                triggerAction:'all',
                //value:'1',
                scope:this
             }),
            renderer:Wtf.comboBoxRenderer(this.Account)
        },{
            header:WtfGlobal.getLocaleText("acc.agedPay.invoiceDate"),            //"Invoice Date",
            dataIndex:'invoicedate',
            width:150,
            align:'center',
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.field.InvoiceAmountDue"),//config.isReceipt? WtfGlobal.getLocaleText("acc.rem.218") : WtfGlobal.getLocaleText("acc.rem.31"),  //"Debit Amount",
            dataIndex:'amountdue',
            width:100,
            align:'center',
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
          
        },{
            header:WtfGlobal.getLocaleText("acc.dnList.gridAmt"),                                   //WtfGlobal.getLocaleText("acc.invReport.desc"),  //"Description",
            dataIndex:"invamount",
            width:100,
            align:'center',
            summaryType:'sum',
            editor:new Wtf.form.NumberField({
                allowBlank: false,
                allowNegative:false,
                maxLength:15,
                decimalPrecision:2
            }),
            renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
            dataIndex:"delete",
            align:'center',
            renderer:function(){
                return "<div class='pwnd delete-gridrow' style='margin-left:40px;'> </div>";
            }
        }]);
       
        this.grid = new Wtf.grid.EditorGridPanel({
            plugins:this.gridPlugins,
//            layout:'fit',
            clicksToEdit:1,
            height:270,
            width:'99%',
            store: this.Store,
            cm: this.mulDebitCM,
            border : false,
            loadMask : true,
            align:'center',
            autoScroll:true,
            viewConfig: {
//                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.grid.on('rowclick',this.processRow,this);
        this.grid.on('afteredit',this.checkrecord,this);
        //this.grid.on('beforeedit',this.checkrecord,this);
       
      },
      processRow:function(grid,rowindex,e){        
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                var record = store.getAt(rowindex);
               
                store.remove(store.getAt(rowindex));
                if(rowindex==total-1){
                    //this.addGridRec();
                }
            }, this);
        }
    },
    checkrecord:function(obj){
        var rec=obj.record;
        if(obj.field=="invamount"){
            if(rec.data.amountdue < obj.value ){
                var msg = this.isReceipt?WtfGlobal.getLocaleText("acc.field.CustomerInvoice"):WtfGlobal.getLocaleText("acc.agedPay.venInv");
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Amountenteredcannotbegreaterthan")+msg+WtfGlobal.getLocaleText("acc.field.amountdue")],2);
                obj.cancel=true;
                rec.set("invamount",0);
            }else{        
                rec.set("invamount", obj.value);
            }
        }
    },
    closeWin:function(){
        this.fireEvent('cancel',this)
        this.close();
         
        if(this.isReceipt){
            Wtf.customerAccStore.load();
        } else {
            Wtf.vendorAccStore.load();
        }
     },
     getInvoiceAmounts:function(){
        var amt=0;
       for(var i=0; i<this.grid.store.getCount();i++){
            amt+=getRoundedAmountValue(this.grid.store.getAt(i).data['invamount']);
       }
       return amt;
     },
    saveData:function(obj){
        if(this.Form.getForm().isValid()&&this.grid.store.getCount()>0) {
            var rec=this.Form.getForm().getValues();
            var invoiceids="";
            var amounts="";
            var invoicenos="";
            var amt = this.getInvoiceAmounts();                  //this.cnamountdue<this.AmountInv.getValue()?this.cnamountdue:this.AmountInv.getValue()
            amt = getRoundedAmountValue(amt);
            var msg = this.isReceipt?WtfGlobal.getLocaleText("acc.accPref.autoRP"):WtfGlobal.getLocaleText("acc.accPref.autoMP");
            if(amt==0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.EnteredAmountForinvoiceshouldbegraeterthanzero")],2);
                return;
            }
            if(amt > this.AmountDuePayment.getValue()) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Amountenteredcannotbegreaterthan")+" "+msg+" "+WtfGlobal.getLocaleText("acc.field.amountdue")],2);
                return;
            }
            var url = "";
            if(this.isReceipt)
                url = "ACC" + (this.isCustBill?"Receipt/linkBillingReceipt":"Receipt/linkReceipt") + ".do";
            else
                url = "ACC" + (this.isCustBill?"VendorPayment/linkBillingPayment":"VendorPayment/linkPayment") + ".do";
            var paymentid = this.record.data.billid;
            var paymentno = this.record.data.billno;
            
            for(var i=0; i<this.grid.store.getCount();i++){
                if(i!=0){
                    invoiceids+=",";
                    amounts+=",";
                    invoicenos+=",";
                }
                invoiceids+=this.grid.store.getAt(i).data['billid'];
                amounts+=this.grid.store.getAt(i).data['invamount'];
                invoicenos+=this.grid.store.getAt(i).data['billno'];
                
                //check it need to see the usage of date
                this.creationDate = this.grid.store.getAt(i).data['date'];
                
            }
            rec.invoiceids=invoiceids;
            rec.amounts=amounts;
            rec.paymentid=paymentid;
            rec.invoicenos=invoicenos;
            rec.creationdate=WtfGlobal.convertToGenericDate(this.creationDate);
            Wtf.Ajax.requestEx({
                   url:url,
                   params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
    	} else if(this.grid.store.getCount()==0) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.recordsnotavailable")],2);
                return;
            } 
    },
    genSuccessResponse:function(response){
        this.close();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.msg],response.success*2+1);
        (function(){
            Wtf.getCmp(this.reloadGrid).store.reload();
            if(this.isReceipt){
                Wtf.customerAccStore.load();
            } else {
                Wtf.vendorAccStore.load();
            }
        }).defer(WtfGlobal.gridReloadDelay(),this);
    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }
});
//Component to link CN / DN with customer / vendor invoice
Wtf.account.linkInvoiceCN = function(config){
    this.reconRec=(config.reconRec==undefined?"":config.reconRec);
    this.isGST = (config.isGST==undefined?false:config.isGST);
    this.record = config.record;
    this.isCN = config.isCN;
    this.isCustBill=config.isCustBill;
    this.reloadGrid = config.reloadGrid;
    this.accid = this.record.data.personid;
    this.accname = this.record.data.personname;
    this.cnamount = this.record.data.amount;
    this.cnamountdue = this.record.data.amountdue;
    this.currencyid = this.record.data.currencyid;
    this.CNNo = this.record.data.noteno;
    this.cnid = this.record.data.noteid;
    this.cndate = this.record.data.date;
    this.isLinking = config.isLinking;
     Wtf.apply(this,{
        constrainHeader :true, 
        title: this.isCN? (this.isLinking ? WtfGlobal.getLocaleText("acc.field.LinkCreditNotetoCustomerInvoice") :WtfGlobal.getLocaleText("acc.cn.unlinkCnFromSalesInvoice")):(this.isLinking?WtfGlobal.getLocaleText("acc.field.LinkDebitNotetoVendorInvoice"):WtfGlobal.getLocaleText("acc.dn.unlinkDnFromPurchaseInvoice")),
        buttons: [this.linkTRansactionButton = new Wtf.Button({
            text: this.isLinking?WtfGlobal.getLocaleText("acc.field.LinkTransaction"):WtfGlobal.getLocaleText("acc.common.saveBtn"),
            scope: this,
            minWidth:100,
            disabled:this.isLinking?false:true,//disabled in case of Unlinked window
            handler: this.saveData.createDelegate(this)
        }),this.closeButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.close"),
            scope: this,
            minWidth:100,
            handler:this.closeWin.createDelegate(this)
        })]
    },config);
    Wtf.account.linkInvoiceCN.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}
Wtf.extend( Wtf.account.linkInvoiceCN, Wtf.Window, {

    onRender: function(config) {
        var image = "../../images/accounting_image/bank-reconciliation.jpg";
        Wtf.account.linkInvoiceCN.superclass.onRender.call(this, config);
        this.createForm();
        this.createDisplayGrid();
        this.add({
            region: 'north',
            height: 82,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            items: [{
                    border: false,
                    html: this.isCN ? (this.isLinking ? getTopHtml(WtfGlobal.getLocaleText("acc.field.LinkCreditNotetoCustomerInvoice"), '', image, false) : getTopHtml(WtfGlobal.getLocaleText("acc.cn.unlinkCnFromSalesInvoice"), '', image, false)) : (this.isLinking ? getTopHtml(WtfGlobal.getLocaleText("acc.field.LinkDebitNotetoVendorInvoice"), '', image, false) : getTopHtml(WtfGlobal.getLocaleText("acc.dn.unlinkDnFromPurchaseInvoice"), '', image, false))
                }, {
                    /*Provided Note to Understand,How to Unlink transaction  */

                    style: 'margin-left:5px;',
                    border: false,
                    html: this.isLinking ? "" : "<span ><b>Note</b>: Please delete the records you want to Unlink.</span>"
                }]
        }, {
            region: 'center',
            border: false,
            baseCls: 'bckgroundcolor',
//            layout: 'fit',
            height: 100,
            items: [this.Form, this.grid]
        });
    },
   
   createForm:function(){
        this.name=new Wtf.form.TextField({
            fieldLabel:(this.isCN?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven"))+"*",  //this.businessPerson +'*',
            name: 'name',
            scope:this,
            maxLength:45,
            anchor:'85%',
            disabled:true,
            allowBlank:false,
            value:this.accname
        });
        
        this.no=new Wtf.form.TextField({
            fieldLabel:(this.isCN?WtfGlobal.getLocaleText("acc.accPref.autoCN"):WtfGlobal.getLocaleText("acc.accPref.autoDN")) +" "+ WtfGlobal.getLocaleText("acc.cn.9") + "*",  //this.noteType+' Note No*',
            name: 'number',
            scope:this,
            maxLength:45,
            anchor:'85%',
            disabled:true,
            allowBlank:false,
            value:this.CNNo
        });
        
        this.AmountCN=new Wtf.form.NumberField({
            name:"cnamount",
            allowBlank:false,
            fieldLabel:(this.isCN?WtfGlobal.getLocaleText("acc.field.CreditNoteAmount*"):WtfGlobal.getLocaleText("acc.field.DebitNoteAmount*")),//WtfGlobal.getLocaleText("acc.mp.11"),  //"Amount*",//in "+WtfGlobal.getCurrencySymbolForForm()+"*",
//            id:"amount"+this.id,
            maxLength:15,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            disabled:true,
            value: getRoundedAmountValue(this.cnamount),
            emptyText:WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt"), //"enter amount",
            anchor:'85%'
        });
        
        this.AmountDueCN=new Wtf.form.NumberField({
            name:"cnamountdue",
            allowBlank:false,
            fieldLabel:(this.isCN?WtfGlobal.getLocaleText("acc.field.CreditNoteAmountDue*"):WtfGlobal.getLocaleText("acc.field.DebitNoteAmountDue*")),//WtfGlobal.getLocaleText("acc.mp.11"),  //"Amount*",//in "+WtfGlobal.getCurrencySymbolForForm()+"*",
//            id:"amount"+this.id,
            maxLength:15,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            disabled:true,
            value:getRoundedAmountValue(this.cnamountdue),
            emptyText:WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt"), //"enter amount",
            anchor:'85%'
        });
        
    	 this.linktransactionfromdate=new Wtf.form.DateField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.linkingDate.date.tooltip") + "'>" + WtfGlobal.getLocaleText("acc.linkingDate.date") + "</span>",//'Linking Date
            name:'fromlinktransactiondate',
            format:WtfGlobal.getOnlyDateFormat(),
            hidden:this.isLinking?false:true,
            hideLabel :this.isLinking?false:true,
            scope:this,
            anchor:'85%',
            value:new Date(this.cndate)
//            maxValue:Wtf.account.companyAccountPref.activeDateRangeToDate!=""&& Wtf.account.companyAccountPref.activeDateRangeToDate!=null?new Date(Wtf.account.companyAccountPref.activeDateRangeToDate):""
        });
    	 this.cndndate=new Wtf.form.DateField({
            fieldLabel:(this.isCN?WtfGlobal.getLocaleText("acc.cnList.gridDate"):WtfGlobal.getLocaleText("acc.dnList.gridDate")),//'CN/DN Date
            name:'fromlinktransactiondate',
            format:WtfGlobal.getOnlyDateFormat(),
            hidden:this.isLinking?false:true,
            hideLabel :this.isLinking?false:true,
            scope:this,
            anchor:'85%',
            value:new Date(this.cndate),
            disabled:true
        });
        // Function for checking whether any of the invoice loaded in grid has transaction date less than linking date
        this.linktransactionfromdate.on('change',function(field,val,oldval){
            var enteredDate = val;
            var isValid = true;
            //            for(var i=0;i<this.Store.getCount();i++){
            //                var rec= this.Store.getAt(i);
            //                if(rec.data.date){
            enteredDate = enteredDate.setHours(0, 0, 0, 0);
            var invoiceDate= new Date(this.cndate).setHours(0, 0, 0, 0); // Done Changes for SDP-2598
            if(enteredDate<invoiceDate){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), this.isCN?WtfGlobal.getLocaleText("acc.linking.cndate"):WtfGlobal.getLocaleText("acc.linking.dndate")], 2);
                field.setValue(oldval);
                isValid = false;
            //                        break;
            }
            //                }
//            }
            // If changed link date is valid, then it may be future date or valid past date. Here store should be re-loaded
            if(isValid){
                this.Store.reload();
            }
        },this);
       
        this.Form=new Wtf.form.FormPanel({
            region:'north',
            border:false,
            hidden : !this.isLinking,
//            bodyStyle: 'overflow: hidden',
            items:[{
                layout:'form',
                bodyStyle: "background: transparent; padding: 20px;",
                labelWidth:100,
                border:false,
                items:[this.name,this.no,this.AmountCN,this.cndndate,this.AmountDueCN,this.linktransactionfromdate] 
           }]
        });
    },

      createDisplayGrid:function(){
        
         this.GridRec = Wtf.data.Record.create ([
                {name: 'type'},
                {name:'documentType'},
                {name:'documentno'},
                {name:'documentid'},
                {name:'linkdetailid'},
                {name:'billid'},
                {name:'invoicedate'},
                {name:'journalentryid'},
                {name:'entryno'},
                {name:'billto'},
                {name:'discount'},
                {name:'currencysymbol'},
                {name:'orderamount'},
                {name:'isexpenseinv'},
                {name:'currencyid'},
                {name:'shipto'},
                {name:'mode'},
                {name:'billno'},
                {name:'date', type:'date'},
                {name:'duedate', type:'date'},
                {name:'linkingdate', type:'date'},
                {name:'shipdate', type:'date'},
                {name:'personname'},
                {name:'personemail'},
                {name:'personid'},
                {name:'shipping'},
                {name:'othercharges'},
                {name:'amount'},
                {name:'linkamount', defaultValue:0},
                {name:'amountdue'},
                {name:'termdays'},
                {name:'termname'},
                {name:'incash'},
                {name:'taxamount'},
                {name:'taxid'},
                {name:'orderamountwithTax'},
                {name:'taxincluded',type:'boolean'},
                {name:'taxname'},
                {name:'deleted'},
                {name:'amountinbase'},
                {name:'memo'},
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
                {name:'startDate', type:'date'},
                {name:'nextDate', type:'date'},
                {name:'expireDate', type:'date'},
                {name:'repeateid'},
                {name:'status'},
                {name:'amountDueOriginal'},
                {name: 'exchangeratefortransaction'},
                {name: 'currencysymbolpayment'},
                {name: 'currencysymboltransaction'},
                {name: 'currencyidtransaction'}, //GR currency id
                {name: 'currencyidpayment'},
                {name: 'linkdetailid'},
                {name:'isOpeningBalanceTransaction'},
                {name:'supplierinvoiceno'}
            ]);

            if(this.isCN)
                this.StoreUrl = this.isLinking ? ("ACC" + (this.isCustBill?"InvoiceCMN/getBillingInvoices":"ReceiptCMN/getDocumentsForLinkingWithCreditNote") + ".do"):"ACCCreditNoteCMN/getCreditNoteLinkedDocumnets.do";
            else
                this.StoreUrl = this.isLinking ? "ACC" + (this.isCustBill?"GoodsReceiptCMN/getBillingGoodsReceipts":"VendorPaymentCMN/getDocumentsForLinkingWithDebitNote") + ".do":"ACCDebitNoteCMN/getDebitNoteLinkedDocumnets.do";

       this.Store = new Wtf.data.Store({
            url:this.StoreUrl,
            baseParams:{
                deleted:false,
                nondeleted:true,
                cashonly:false,
                creditonly:false,
                onlyAmountDue:true,
                notlinkCNFromInvoiceFlag:false,// opening balance invoice will come in invoice combo in case of linking invoice with CN.
//                minimumAmountDue:this.cnamount,
                accid:this.accid,
                isEdit:false,
                cntype: 8,
                isVendor: !this.isCN,
                customerid: this.accid,
                custVendorID: this.accid,
                ignorecustomers : true,
                ignorevendors : true,
                billId:this.record!=undefined ? this.record.data.billid : "",
                currencyfilterfortrans : this.currencyid,
                isNoteForPayment:true, //sending flag to avoid currency filter
                isReceipt:true,     //sending flag to avoid currency filter
                includeFixedAssetInvoicesFlag:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.GridRec)
        });
        if(!this.isLinking){
            this.Store.baseParams={
                deleted:false,
                nondeleted:true,
                cashonly:false,
                creditonly:false,
                accid:this.accid,
                currencyfilterfortrans : this.currencyid,
                isReceipt:true,
                isNoteForPayment:true, //sending flag to avoid currency filter
                noteId:this.cnid
            }
        }
        this.fetchInvoicesButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.reloadDocuments"),
            scope: this,
            minWidth:100,
            hidden:!this.isLinking,
            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
            handler: this.fetchInvoicesForLinking.createDelegate(this)
        })
        this.tbarForGrid = new Array();
        this.tbarForGrid.push(this.fetchInvoicesButton);
        this.Store.on('beforeload',function(){
            //Commented because of ERP-36411
            if(this.linktransactionfromdate.getValue()!=''){
                this.Store.baseParams.upperLimitDate = WtfGlobal.convertToGenericDate(this.linktransactionfromdate.getValue());
            } else {
                this.Store.baseParams.upperLimitDate = WtfGlobal.convertToGenericDate(this.record.data.date);
            }
//            //previously it was on linking date. Now it is on creation date
//            if(this.cndndate.getValue()!=''){
//                this.Store.baseParams.upperLimitDate = WtfGlobal.convertToGenericDate(this.cndndate.getValue());
//            }
           
        },this);
        
        this.Store.on('load', function (store) {
            for (var i = 0; i < store.data.length; i++) {
                var rec = store.getAt(i);
                if (rec) {
                    rec.set('linkingdate', this.linktransactionfromdate.getValue());
                }
            }
        }, this);
        
        this.Store.load();
        this.mulDebitCM= new Wtf.grid.ColumnModel([{
                header: WtfGlobal.getLocaleText("acc.field.DocumentType"),
                dataIndex: 'type',
                name:'type',
                width:150
            },{
                header: WtfGlobal.getLocaleText("acc.invoice.SupplierInvoiceNumber"),
                dataIndex: 'supplierinvoiceno',
                name:'supplierinvoiceno',
                width:150
            },{
                header: WtfGlobal.getLocaleText("acc.field.DocumentNumber"),
                dataIndex: 'documentno',
                width:150,
                renderer: function(val, m, rec) {
                    val = val.replace(/(<([^>]+)>)/ig, "");
                    if (rec.data.deleted)
                        val = '<del>' + val + '</del>';
                    return "<div wtf:qtip=\"" + val + "\">" + val + "</div>";
                }
            },{
            header:WtfGlobal.getLocaleText("acc.field.TransactionDate"),            //"Invoice Date", acc.agedPay.invoiceDate
            dataIndex:'date',
            width:150,
            align:'center',
            renderer: function(v, m, rec) {
                if (!v)
                    return v;
                if (rec != undefined && rec.data.deleted)
                    v = '<del>' + v.format(WtfGlobal.getOnlyDateFormat()) + '</del>';
                else
                    v = v.format(WtfGlobal.getOnlyDateFormat());
                v = '<div class="datecls" wtf:qtip="' + v + '">' + v + '</div>';
                return v;
            }
        } ,this.detailLinkingDate={
            header:WtfGlobal.getLocaleText("acc.linkingDate.date"),  //Linking Date
            dataIndex:'linkingdate',
            width:150,
            align:'center',
            disabled:( this.readOnly),
            hidden:this.isLinking?false:true,
            renderer: function(v, m, rec) {
                if (!v){
                    return v;
                }
                if (rec != undefined && rec.data.deleted){
                    v = '<del>' + v.format(WtfGlobal.getOnlyDateFormat()) + '</del>';
                }else{
                    v = v.format(WtfGlobal.getOnlyDateFormat());
                }
                    
                v = '<div class="datecls" wtf:qtip="' + v + '">' + v + '</div>';
                return v;
            },
            editor:new Wtf.form.DateField({
                maxLength:250,
                format:WtfGlobal.getOnlyDateFormat(),
                xtype:'datefield',
//                value:new Date(this.cndate),   
//                maxValue:Wtf.account.companyAccountPref.activeDateRangeToDate!=""&& Wtf.account.companyAccountPref.activeDateRangeToDate!=null?new Date(Wtf.account.companyAccountPref.activeDateRangeToDate):""
                listeners:{
                    'change':{
                        fn:this.GridcheckMaxDate,
                        scope:this
                    }
                }
            } )
        },{
            header:WtfGlobal.getLocaleText("acc.field.OriginalAmountDue"),
            dataIndex:'amountDueOriginal',
            hidelabel:false,
            hidden: false,
            width:180,
            renderer: function(value, m, rec) {
                var currencysymbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
                var symbol=((rec==undefined||rec.data.currencysymboltransaction==null||rec.data['currencysymboltransaction']==undefined||rec.data['currencysymboltransaction']=="")?currencysymbol:rec.data['currencysymboltransaction']);
                var v = parseFloat(value);
                if (isNaN(v))
                    return value;
                v = WtfGlobal.conventInDecimal(v, symbol)
                return '<div class="currency" wtf:qtip="' + v + '">' + v + '</div>';
            }
        },{
//            header:WtfGlobal.getLocaleText("acc.setupWizard.curEx"), 
            header:WtfGlobal.getLocaleText("acc.field.adjustedExchangeRate"), 
            dataIndex:'exchangeratefortransaction',
            hidelabel:false,
            hidden: false,
            width:150,
            renderer: function(value, meta, record) {
                var currencysymboltransaction = ((record == undefined || record.data.currencysymboltransaction == null || record.data['currencysymboltransaction'] == undefined || record.data['currencysymboltransaction'] == "") ? WtfGlobal.getCurrencySymbol() : record.data['currencysymboltransaction']);
                var currencysymbolpayment = ((record == undefined || record.data.currencysymbolpayment == null || record.data['currencysymbolpayment'] == undefined || record.data['currencysymbolpayment'] == "") ? currencysymboltransaction : record.data['currencysymbolpayment']);
                var v = parseFloat(value);
                if (isNaN(v))
                    return value;
                return '<div wtf:qtip="' + "1 " + currencysymboltransaction + " = " + value + " " + currencysymbolpayment + '">' + "1 " + currencysymboltransaction + " = " + value + " " + currencysymbolpayment + '</div>';
                ;
            },
            editor: this.exchangeratefortransaction=new Wtf.form.NumberField({
                decimalPrecision:10,
                allowNegative : false,
                validator: function(val) {
                    if (val!=0) {
                        return true;
                    } else {
                        return false;
                    }
                }
            })
        },{
            header: WtfGlobal.getLocaleText("acc.invoiceList.amtDue"),
            dataIndex: 'amountdue',
            align: 'right',
            width:130,
            renderer: function (value, m, rec) {
                var symbol = ((rec == undefined || rec.data.currencysymbolpayment == null || rec.data['currencysymbolpayment'] == undefined || rec.data['currencysymbolpayment'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbolpayment']);
                var v = parseFloat(value);
                if (isNaN(v))
                    return value;
                v = WtfGlobal.conventInDecimal(v, symbol)
                return '<div class="currency" wtf:qtip="' + v + '">' + v + '</div>';
            }
        },{
            header:WtfGlobal.getLocaleText("acc.dnList.gridAmt"),                                   //WtfGlobal.getLocaleText("acc.invReport.desc"),  //"Description",
            dataIndex:"linkamount",
            width:100,
            align:'center',
            summaryType:'sum',
            editor:new Wtf.form.NumberField({
                allowBlank: false,
                allowNegative:false,
                maxLength:15,
                decimalPrecision:2
            }),
            renderer: function(value, m, rec) {
                var symbol = ((rec == undefined || rec.data.currencysymbolpayment == null || rec.data['currencysymbolpayment'] == undefined || rec.data['currencysymbolpayment'] == "") ? WtfGlobal.getCurrencySymbol() : rec.data['currencysymbolpayment']);
                var v = parseFloat(value);
                if (isNaN(v)){
                    return value;
                }
                v = WtfGlobal.conventInDecimal(v, symbol)
                return '<div class="currency" wtf:qtip="' + v + '">' + v + '</div>';
            }
        },{
            header:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
            dataIndex:"delete",
            align:'center',
            renderer:function(){
                return "<div class='pwnd delete-gridrow' style='margin-left:auto;margin-right:auto;display:block;'> </div>";
            }
        }]);
       
        this.grid = new Wtf.grid.EditorGridPanel({
            plugins:this.gridPlugins,
            clicksToEdit:1,
            height:260,
            width:'99%',
            store: this.Store,
            cm: this.mulDebitCM,
            align:'center',
            border : false,
            loadMask : true,
            layout:'fit',
            tbar:this.tbarForGrid,
            autoScroll: true,
            viewConfig: {
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.grid.on('rowclick',this.processRow,this);
        this.grid.on('afteredit',this.checkrecord,this);
        this.grid.on('beforeedit',this.beforeGridEdit,this);
       
      },
      processRow:function(grid,rowindex,e){        
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                var record = store.getAt(rowindex);              
               this.linkTRansactionButton.enable();
                store.remove(store.getAt(rowindex));
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
                    var msg = this.isCN?WtfGlobal.getLocaleText("acc.field.CustomerInvoice"):WtfGlobal.getLocaleText("acc.agedPay.venInv");
                     WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Amountenteredcannotbegreaterthan")+msg+" "+WtfGlobal.getLocaleText("acc.field.amountdue")],2);
                     obj.cancel=true;
                       rec.set("linkamount",0);
                }else{        
                rec.set("linkamount", obj.value);
                }
            } else if (obj.field == 'exchangeratefortransaction') {
                var amountDueOriginal = 0;
                var exchangeRate = 0;
                amountDueOriginal = parseFloat(rec.data.amountDueOriginal);
                exchangeRate = rec.data.exchangeratefortransaction;
                if (exchangeRate != '')
                    obj.record.set("amountdue", getRoundedAmountValue(amountDueOriginal * exchangeRate));
            }
    },
    closeWin:function(){
         this.fireEvent('cancel',this)
         this.close();
     },
     getInvoiceAmounts:function(){
        var amt=0;
       for(var i=0; i<this.grid.store.getCount();i++){
            amt+=getRoundedAmountValue(this.grid.store.getAt(i).data['linkamount']);
       }
       return amt;
     },
    conversionFactorRenderer: function (value, meta, record) {
        var currencysymbol = ((record == undefined || record.data.currencysymbol == null || record.data['currencysymbol'] == undefined || record.data['currencysymbol'] == "") ? WtfGlobal.getCurrencySymbol() : record.data['currencysymbol']);
        var currencysymboltransaction = ((record == undefined || record.data.currencysymboltransaction == null || record.data['currencysymbolpayment'] == undefined || record.data['currencysymbolpayment'] == "") ? currencysymbol : record.data['currencysymbolpayment']);
        var v = parseFloat(value);
        if (isNaN(v))
            return value;
        return "1 " + currencysymbol + " = " + value + " " + currencysymboltransaction;
    },
    beforeGridEdit: function (obj) {
        if(obj.field=='billid'){
            obj.cancel=true;          // If combobox is set disabled, it is getting shadowed when user is trying to change invoice. so we have restricted it form changing
        } else if(obj.field == 'linkamount' && !this.isLinking){
            obj.cancel=true
        }else if (obj.field == 'exchangeratefortransaction') {
            if (!this.isExchangeRateEditableForSelectedDocumentType(obj))
                obj.cancel = true;
            }
    },
    isExchangeRateEditableForSelectedDocumentType: function (e) {
        if (!this.isLinking || (e.record.data.currencysymbolpayment== e.record.data.currencysymboltransaction)) {
            return false;
        } else {
            return true;
        }
    },
  
    checkMaxDate:function(){//finding maximum date
        if(this.isLinking) {
            var checkdateflag=true;
            var transactiondate=this.record.data.date;
            var linkingdate=this.linktransactionfromdate.getValue();
            var msg=this.isCN?WtfGlobal.getLocaleText("acc.field.EnteredDocumentDateCreditNoteDate"):WtfGlobal.getLocaleText("acc.field.EnteredDocumentDateDebitNoteDate");
            //Payment Date is checked
            if(transactiondate.getTime()>linkingdate.getTime()){//comparing payment billid with Grids Linked Date
                
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.EnteredDocumentDategreaterthanLinkingDate")+" "+msg+". "+WtfGlobal.getLocaleText("acc.field.changelinkingdate")],2);
                checkdateflag=false;
                return false;
            }            
            //Document Date is checked  
            for(var i=0; i<this.grid.store.getCount();i++){
                if(this.grid.store.getAt(i).data['linkamount']!=0){//if amount is not zero
                    var billdate=this.grid.store.getAt(i).data['date'];
                    if(billdate.getTime()>linkingdate.getTime()){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.EnteredDocumentDategreaterthanLinkingDate")+" "+msg+". "+WtfGlobal.getLocaleText("acc.field.changelinkingdate")],2);
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
//ERP-36411
    GridcheckMaxDate : function(field,newVal,oldval){
        if(this.isLinking && newVal) {
            var checkdateflag=true;
            var transactiondate=this.record.data.date;
            var linkingdate=newVal;
            
            var msg=this.isCN?WtfGlobal.getLocaleText("acc.field.EnteredDocumentDateCreditNoteDate"):WtfGlobal.getLocaleText("acc.field.EnteredDocumentDateDebitNoteDate");
            //Payment Date is checked
            if(transactiondate.getTime()>linkingdate.getTime()){//comparing payment billid with Grids Linked Date
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.EnteredDocumentDategreaterthanLinkingDate")+" "+msg+". "+WtfGlobal.getLocaleText("acc.field.changelinkingdate")],2);
                checkdateflag=false;
                field.setValue(oldval);
            }            
            //Document Date is checked  
            for(var i=0; i<this.grid.store.getCount();i++){
                if(this.grid.store.getAt(i).data['linkamount']!=0){//if amount is not zero
                    var billdate=this.grid.store.getAt(i).data['date'];
                    if(billdate.getTime()>linkingdate.getTime()){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.EnteredDocumentDategreaterthanLinkingDate")+" "+msg+". "+WtfGlobal.getLocaleText("acc.field.changelinkingdate")],2);
                        checkdateflag=false;
                        field.setValue(oldval);
                    }
                }
            }
            if(!checkdateflag){
                this.enableButtons();
                return;
            }
        } 
    },
    enableButtons:function(){//enabling Buttons
        if (this.linkTRansactionButton){
            this.linkTRansactionButton.enable();
        }
        if (this.closeButton){
            this.closeButton.enable();
        }
    },
    saveData:function(obj){
    	if(this.Form.getForm().isValid() && ((this.grid.store.getCount()>0 && this.isLinking) || !this.isLinking)) {
            
            if (!isFromActiveDateRange(this.linktransactionfromdate.getValue())) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.activeDateRangePeriod.transactionCannotbeCompleted.alert")],2);
                return;
            }
            var rec=this.Form.getForm().getValues();
            var amt = this.getInvoiceAmounts();                  //this.cnamountdue<this.AmountInv.getValue()?this.cnamountdue:this.AmountInv.getValue()
            amt = getRoundedAmountValue(amt);
//            var type = this.cnamountdue<this.AmountInv.getValue()?(this.isCN?"Credit Note":"Debit Note"):"Invoice";
            var msg = this.isCN?WtfGlobal.getLocaleText("acc.accPref.autoCN"):WtfGlobal.getLocaleText("acc.accPref.autoDN");
             // While linking CN/DN to invoice, system will check whether total amount used in linking is exceeding the amount due of Cn/ Dn or not.
            if(amt > this.AmountDueCN.getValue() && this.isLinking) {         
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Amountenteredcannotbegreaterthan")+" "+msg+" "+WtfGlobal.getLocaleText("acc.field.amountdue")],2);
                return;
            }
            if(amt==0 && this.isLinking){//if amount is zero give message amount cannot be zero 
                this.isCN?WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.EnteredAmountForinvoiceshouldbegreaterthanzero")],2):WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.EnteredAmountForinvoiceorcnshouldbegraeterthanzero")],2);
                this.enableButtons();
                return;
            }
            
            if (this.closeButton)
                this.closeButton.disable();
            if (this.linkTRansactionButton)
                this.linkTRansactionButton.disable();
            
            if(this.isLinking){//For Linking case only
                /*Checking Document Date & Payment Date with Linking Date*/
                var validateflag=this.checkMaxDate();
                if(validateflag){//if dates are valid set maxlinkingdate 
                    rec.linkingdate=WtfGlobal.convertToGenericDate(this.linktransactionfromdate.getValue());//maximum linking date
                }else{
                    this.enableButtons();
                    return;
                }
            }
            var url = "";
            if(this.isCN)
                url = this.isLinking ? "ACC" + (this.isCustBill?"CreditNote/linkBillingCreditNote":"CreditNoteCMN/linkCreditNote") + ".do" : "ACCCreditNoteCMN/unlinkCreditNote.do";
            else
                url = this.isLinking ? "ACC" + (this.isCustBill?"DebitNote/linkBillingDreditNote":"DebitNoteCMN/linkDebitNote") + ".do" :"ACCDebitNoteCMN/unlinkDebitNote.do" ;
            
            //var invoiceids = this.Account.getValue();
            var cnid = this.cnid;
            rec.cnid=this.cnid;
            var documentid="";
            var amounts="";
            for(var i=0; i<this.grid.store.getCount();i++){
                if(i!=0){
                    documentid+=",";
                    amounts+=",";
                }
                //FOr linking only required to check
                if (this.isLinking) {
                    //Checking Mandatory field linking date-ERP-36411
                    if (this.grid.store.getAt(i).data['linkamount'] > 0 && (this.grid.store.getAt(i).data['linkingdate'] === undefined || this.grid.store.getAt(i).data['linkingdate'] === null || this.grid.store.getAt(i).data['linkingdate'] === "")) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.linkingDateMandatoryMsg")], 2);
                        this.enableButtons();
                        return;
                    }

                    if (this.grid.store.getAt(i).data['linkamount'] > 0 && (this.grid.store.getAt(i).data['linkingdate'] != undefined || this.grid.store.getAt(i).data['linkingdate'] != null || this.grid.store.getAt(i).data['linkingdate'] != "")) {
                        if (!isFromActiveDateRange(this.grid.store.getAt(i).data['linkingdate'])) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.activeDateRangePeriod.transactionCannotbeCompleted.alert")], 2);
                            this.enableButtons();
                            return;
                        }
                    }
                }
                
                documentid+=this.grid.store.getAt(i).data['documentid'];
                amounts+=this.grid.store.getAt(i).data['linkamount'];
                
                
            }
            rec.invoiceids=documentid;
            rec.amounts=amounts;
            rec.linkdetails=this.getSelectedRecords();
            Wtf.Ajax.requestEx({
                   url:url,
                   params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);

    	}
    },
    getSelectedRecords: function () {
        var arr = [];
        this.Store.each(function (record) {
            arr.push(this.Store.indexOf(record));
        }, this);
        var jarray = WtfGlobal.getJSONArray(this.grid, true, arr);
        return jarray;
    },
    genSuccessResponse:function(response){
        this.close();
         
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.msg],(response.success?3:2));
          
        (function(){
            Wtf.getCmp(this.reloadGrid).store.reload();
        }).defer(WtfGlobal.gridReloadDelay(),this);
    },

    genFailureResponse:function(response){
        if(this.closeButton)
            this.closeButton.enable();
        if(this.linkTRansactionButton)
            this.linkTRansactionButton.enable();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    fetchInvoicesForLinking:function(){
        this.Store.load();
    }
});



Wtf.account.linkInvoicePaymentNew = function(config){
    this.reconRec=(config.reconRec==undefined?"":config.reconRec);
    this.isGST = (config.isGST==undefined?false:config.isGST);
    this.record = config.record;
    this.currencyid = config.record.data.currencyid;
    this.isReceipt = config.isReceipt;
    this.isCustBill=config.isCustBill;
    this.reloadGrid = config.reloadGrid;
    this.accid = this.record.data.personid;
    this.accname = this.record.data.personname;
    this.paymentamount = this.record.data.amount;
    this.paymentdue = this.record.data.paymentamountdue;
    this.paymentNo = this.record.data.billno;
    this.paymentid = this.record.data.billid;
    this.title = "";
    if(config.islinkflag) {
        this.title = this.isReceipt?WtfGlobal.getLocaleText("acc.field.LinkReceipttoCustomerInvoice"):WtfGlobal.getLocaleText("acc.field.LinkPaymenttoVendorInvoice")
    } else {
        this.title = this.isReceipt?WtfGlobal.getLocaleText("acc.field.UnLinkReceipttoCustomerInvoice"):WtfGlobal.getLocaleText("acc.field.UnLinkPaymenttoVendorInvoice") 
    }
     Wtf.apply(this,{
        title: this.title,
        buttons: [{
            text: this.islinkflag ? WtfGlobal.getLocaleText("acc.field.LinkTransaction") : WtfGlobal.getLocaleText("acc.common.saveBtn"),
            scope: this,
            id:'saveLinkingInformationButton',
            handler: this.saveData.createDelegate(this)
        },{
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            scope: this,
            handler:this.closeWin.createDelegate(this)
        }]
    },config);
    Wtf.account.linkInvoicePaymentNew.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}
Wtf.extend( Wtf.account.linkInvoicePaymentNew, Wtf.Window, {

    onRender: function(config){
        var image="../../images/accounting_image/bank-reconciliation.jpg";
        Wtf.account.linkInvoicePaymentNew.superclass.onRender.call(this, config);
        if(this.isReceipt){
                Wtf.customerAccStore.load();
            } else {
                Wtf.vendorAccStore.load();
            }
        this.createForm();
        this.createDisplayGrid();
        var msg=this.islinkflag ? "":WtfGlobal.getLocaleText("acc.field.DeleteTransactionYouWantToDelete");
        msg+=this.islinkflag?'<div style="font-size:14px; text-align:left; font-weight:bold; margin-top:1%;">Note : </div>'+'<div style="font-size:12px; text-align:left; margin-top:1%;">Only those invoices are displayed whose creation date is before todays date.</div>':"";
        this.add({
            region: 'north',
            height:100,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml(this.title,msg,image,false)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
//            layout: 'fit',
            height:100,
             items:[this.Form,this.grid]
        });
   },
   
   createForm:function(){      
        this.name=new Wtf.form.TextField({
            fieldLabel:(this.isReceipt?WtfGlobal.getLocaleText("acc.invoiceList.cust"):WtfGlobal.getLocaleText("acc.invoiceList.ven"))+"*",  //this.businessPerson +'*',
            id:"linkcusven",
            hiddenName:'accname',
            scope:this,
            maxLength:45,
            anchor:'85%',
            disabled:true,
            allowBlank:false,
            value:this.accname
        });
        if(this.isReceipt) {
                this.name.setValue(this.record.data.personname);
        } else {
                this.name.setValue(this.record.data.personname);
        }
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
        
        this.AmountPayment=new Wtf.form.NumberField({
            name:"cnamount",
            allowBlank:false,
            fieldLabel:(this.isReceipt?WtfGlobal.getLocaleText("acc.field.receivePaymentAmount*"):WtfGlobal.getLocaleText("acc.field.makePaymentAmount*")),//WtfGlobal.getLocaleText("acc.mp.11"),  //"Amount*",//in "+WtfGlobal.getCurrencySymbolForForm()+"*",
//            id:"amount"+this.id,
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
            fieldLabel:(this.isReceipt?WtfGlobal.getLocaleText("acc.field.receivePaymentAmountDue*"):WtfGlobal.getLocaleText("acc.field.makePaymentAmountDue*")),//WtfGlobal.getLocaleText("acc.mp.11"),  //"Amount*",//in "+WtfGlobal.getCurrencySymbolForForm()+"*",
//            id:"amount"+this.id,
            maxLength:15,
            decimalPrecision:2,
            disabled:true,
            value:this.paymentdue,
            emptyText:WtfGlobal.getLocaleText("acc.invoice.gridEnterAmt"), //"enter amount",
            anchor:'85%'
        });
    	
        var InvoiceHelp = this.isReceipt?WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.field.receivePaymentcanbeapplytotheinvoiceswithsamecurrency")):WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.field.makePaymentcanbeapplytotheinvoiceswithsamecurrency"));
       
        this.Form=new Wtf.form.FormPanel({
            region:'north',
            border:false,
            hidden : !this.islinkflag,
//            bodyStyle: 'overflow: hidden',
            items:[{
                layout:'form',
                bodyStyle: "background: transparent; padding: 20px;",
                labelWidth:100,
                border:false,
                items:[this.name,this.no,this.AmountPayment,this.AmountDuePayment]  //this.Account,this.AmountInv,this.Amount
           }]
        });
    },

      createDisplayGrid:function(){
        
         this.GridRec = Wtf.data.Record.create ([
                {name:'billid'},
                {name:'linkdetailid'},
                {name:'invoicedate',type:'date'},
                {name:'journalentryid'},
                {name:'entryno'},
                {name:'billto'},
                {name:'discount'},
                {name:'currencysymbol'},
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
                {name:'amount'},
                {name:'invamount', defaultValue:0},
                {name:'amountdue'},
                {name:'termdays'},
                {name:'termname'},
                {name:'incash'},
                {name:'taxamount'},
                {name:'taxid'},
                {name:'orderamountwithTax'},
                {name:'taxincluded',type:'boolean'},
                {name:'taxname'},
                {name:'deleted'},
                {name:'amountinbase'},
                {name:'memo'},
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
                {name:'startDate', type:'date'},
                {name:'nextDate', type:'date'},
                {name:'expireDate', type:'date'},
                {name:'repeateid'},
                {name:'status'},
                {name:'amountDueOriginal'},
                {name:'exchangeratefortransaction'},                
                {name:'currencyidtransaction'},//GR currency id
                {name:'currencysymboltransaction'},                
                {name:'currencyidpayment'},                
                {name:'currencysymbolpayment'}                
            ]);

        if (this.islinkflag) {
            if (this.isReceipt)
                this.StoreUrl = "ACC" + (this.isCustBill ? "InvoiceCMN/getBillingInvoices" : "InvoiceCMN/getInvoices") + ".do";
            else
                this.StoreUrl = "ACC" + (this.isCustBill ? "GoodsReceiptCMN/getBillingGoodsReceipts" : "GoodsReceiptCMN/getGoodsReceipts") + ".do";
        } else {
            if (this.isReceipt)
                this.StoreUrl = "ACCReceiptCMN/getLinkedInvoicesAgainstAdvance.do";
            else
                this.StoreUrl = "ACCVendorPaymentCMN/getLinkedInvoicesAgainstAdvance.do";
        }

       this.Store = new Wtf.data.Store({
            url:this.StoreUrl,
            baseParams:{
                deleted:false,
                nondeleted:true,
                cashonly:false,
                creditonly:false,
                onlyAmountDue:true,
                isReceipt:this.isReceipt,
                currencyfilterfortrans : this.currencyid,
                accid:this.record.data.personid,
                includeFixedAssetInvoicesFlag:true,
                bills:  this.record!==undefined ? this.record.data.billid : "",
                upperLimitDate: WtfGlobal.convertToGenericDate(new Date())
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.GridRec)
        });
        
        this.Store.load();
//          if(this.record.data.isadvancepayment){
//              this.Store.load({params:{accid:this.record.data.personid,currencyfilterfortrans : this.currencyid}});
//          }      

        this.Store.on('load',this.CheckLinkedInvoicesPresent,this);
        this.mulDebitCM= new Wtf.grid.ColumnModel([{
            header:this.isReceipt?WtfGlobal.getLocaleText("acc.field.CustomerInvoice"):WtfGlobal.getLocaleText("acc.agedPay.venInv"),
            dataIndex:'billid',
            width:150,
            hidden : this.isAdvPayment,
            align:"center",
            editor: this.Account= new Wtf.form.ComboBox({
//                fieldLabel:(this.isReceipt?"Customer Invoice":"Vendor Invoice")+"*"+InvoiceHelp,
                hiddenName:"linkInvoice",
                minChars:1,
                listWidth :300,
    //            labelWidth:200,
                store: this.Store,
                valueField:'billid',
                disabled:true,
                displayField:'billno',
                allowBlank:false,
                hirarchical:true,
    //            emptyText:this.isReceipt?'Select a Customer Invoice':'Select a Vendor Invoice',
                mode: 'local',
                typeAhead: true,
                typeAheadDelay:30000,
                align:"center",
                forceSelection: true,
                selectOnFocus:true,
                triggerAction:'all',
                //value:'1',
                scope:this
             }),
            renderer:Wtf.comboBoxRenderer(this.Account)
        },{
            header:WtfGlobal.getLocaleText("acc.agedPay.invoiceDate"),            //"Invoice Date",
            dataIndex:'invoicedate',
            width:150,
            align:'center',
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.field.OriginalAmountDue"),
            dataIndex:'amountDueOriginal',
            hidelabel:false,
            hidden: false,
            width:150,
            renderer:WtfGlobal.withoutRateCurrencySymbolTransaction
        },{
            header:WtfGlobal.getLocaleText("acc.setupWizard.curEx"), 
            dataIndex:'exchangeratefortransaction',
            hidelabel:false,
            hidden: false,
            width:200,
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
            dataIndex:"invamount",
            width:100,
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
            hidden: this.islinkflag,
            renderer:function(){
                return "<div class='pwnd delete-gridrow' style='margin-left:40px;'> </div>";
            }
        }]);
       
        this.grid = new Wtf.grid.EditorGridPanel({
            plugins:this.gridPlugins,
//            layout:'fit',
            clicksToEdit:1,
            height:245,
            width:'99%',
            store: this.Store,
            cm: this.mulDebitCM,
            border : false,
            loadMask : true,
            align:'center',
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.grid.on('rowclick',this.processRow,this);
        this.grid.on('afteredit',this.checkrecord,this);
        this.grid.on('beforeedit',this.beforeGridEdit,this);
        //this.grid.on('beforeedit',this.checkrecord,this);
       
      },
      processRow:function(grid,rowindex,e){        
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn){
                if(btn!="yes") return;
                var store=grid.getStore();
                var total=store.getCount();
                var record = store.getAt(rowindex);
               
                store.remove(store.getAt(rowindex));
                if(rowindex==total-1){
                    //this.addGridRec();
                }
            }, this);
        }
    },
    checkrecord:function(obj){
        var rec=obj.record;
            if(obj.field=="invamount"){
                if(rec.data.amountdue < obj.value ){
                    var msg = this.isReceipt?WtfGlobal.getLocaleText("acc.field.CustomerInvoice"):WtfGlobal.getLocaleText("acc.agedPay.venInv");
                     WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Amountenteredcannotbegreaterthan")+" "+msg+" "+WtfGlobal.getLocaleText("acc.field.amountdue")],2);
                     obj.cancel=true;
                       rec.set("invamount",0);
                }else{        
                rec.set("invamount", obj.value);
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
    closeWin:function(){
        this.fireEvent('cancel',this)
        this.close();
         
     },
     getInvoiceAmounts:function(){
        var amt=0;
       for(var i=0; i<this.grid.store.getCount();i++){
            amt+=getRoundedAmountValue(this.grid.store.getAt(i).data['invamount']);
       }
       return amt;
     },
  checkMaxDate:function(){//finding maximum date
        if((this.islinkflag)) {
            var checkdateflag=true;
            var paymentbilldate=this.record.data.billdate;
            var linkingdate=this.linktransactionfromdate.getValue();
            
            //Payment Date is checked
            if(paymentbilldate.getTime()>linkingdate.getTime()){//comparing payment billid with Grids Linked Date
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.EnteredDocumentDategreaterthanLinkingDate")+" "+WtfGlobal.getLocaleText("acc.field.EnteredPaymentDate")+". "+WtfGlobal.getLocaleText("acc.field.changelinkingdate")],2);
                checkdateflag=false;
                return false;
            }            
            //Document Date is checked  
            for(var i=0; i<this.grid.store.getCount();i++){
                if(this.grid.store.getAt(i).data['linkamount']!=0){//if amount is not zero
                    var billdate=this.grid.store.getAt(i).data['date'];
                    if(billdate.getTime()>linkingdate.getTime()){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.EnteredDocumentDategreaterthanLinkingDate")+" "+WtfGlobal.getLocaleText("acc.field.EnteredDocumentDate")+". "+WtfGlobal.getLocaleText("acc.field.changelinkingdate")],2);
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
    saveData:function(obj){
        obj.disable();
        if(this.Form.getForm().isValid() && ((this.islinkflag && this.grid.store.getCount()>0) || !this.islinkflag)) {
            var rec=this.Form.getForm().getValues();
            var invoiceids="";
            var amounts="";
            var invoicenos="";
            if(this.islinkflag) {
            var amt = this.getInvoiceAmounts();                  //this.cnamountdue<this.AmountInv.getValue()?this.cnamountdue:this.AmountInv.getValue()
            amt = getRoundedAmountValue(amt);
            var msg = this.isReceipt?WtfGlobal.getLocaleText("acc.accPref.autoRP"):WtfGlobal.getLocaleText("acc.accPref.autoMP");
            if(amt==0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.EnteredAmountForinvoiceshouldbegraeterthanzero")],2);
                    obj.enable();
                return;
            }
            if(amt > this.AmountDuePayment.getValue()) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Amountenteredcannotbegreaterthan")+" "+msg+" "+WtfGlobal.getLocaleText("acc.field.amountdue")],2);
                    obj.enable();
                return;
            }
            }
            var url = "";
            if(this.islinkflag) {
                if(this.isReceipt) {
                    url = "ACC" + (this.isCustBill?"Receipt/linkBillingReceipt":"ReceiptNew/linkReceipt") + ".do";
                } else {
                    url = "ACC" + (this.isCustBill?"VendorPayment/linkBillingPayment":"VendorPaymentNew/linkPaymentNew") + ".do";
                }
            } else {
                if (this.isReceipt)
                    url = "ACCReceiptNew/unlinkReceiptInvoices.do";
                else
                    url = "ACCVendorPaymentNew/unlinkPaymentInvoices.do";
            }
            var paymentid = this.record.data.billid;
            var paymentno = this.record.data.billno;
            
            for(var i=0; i<this.grid.store.getCount();i++){
                if(i!=0){
                    invoiceids+=",";
                    amounts+=",";
                    invoicenos+=",";
                }
                invoiceids+=this.grid.store.getAt(i).data['billid'];
                amounts+=this.grid.store.getAt(i).data['invamount'];
                invoicenos+=this.grid.store.getAt(i).data['billno'];
                
                //check it need to see the usage of date
                this.creationDate = this.grid.store.getAt(i).data['date'];
                
            }
            rec.invoiceids=invoiceids;
            rec.amounts=amounts;
            rec.paymentid=paymentid;
            rec.invoicenos=invoicenos;
            rec.linkdetails=this.getSelectedRecords();
            rec.creationdate=WtfGlobal.convertToGenericDate(this.creationDate);
            Wtf.Ajax.requestEx({
                   url:url,
                   params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
    	} else if(this.grid.store.getCount()==0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.recordsnotavailable")],2);
            obj.enable();
            return;
        } 
    },
    genSuccessResponse:function(response){
        this.close();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.msg],response.success*2+1);
        (function(){
            Wtf.getCmp(this.reloadGrid).store.reload();
            if(this.isReceipt){
                Wtf.customerAccStore.load();
            } else {
                Wtf.vendorAccStore.load();
            }
        }).defer(WtfGlobal.gridReloadDelay(),this);
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
        var currencysymbol=((record==undefined||record.data.currencysymbol==null||record.data['currencysymbol']==undefined||record.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():record.data['currencysymbol']);
        var currencysymboltransaction=((record==undefined||record.data.currencysymboltransaction==null||record.data['currencysymbolpayment']==undefined||record.data['currencysymbolpayment']=="")?currencysymbol:record.data['currencysymbolpayment']);
        var v=parseFloat(value);
        if(isNaN(v)) return value;
        return "1 "+ currencysymbol +" = " +value+" "+currencysymboltransaction;
    },
    isExchangeRateEditableForSelectedDocumentType: function (e) {
        if (e.record.data.currencyidtransaction == e.record.data.currencyidpayment) {
            return false;
        } else {
            return true;
        }
    },
    CheckLinkedInvoicesPresent: function () {
        if (this.Store.getCount() == 0) {
            WtfComMsgBox(["Info", WtfGlobal.getLocaleText(this.isReceipt ? "acc.field.NoLinkedReceiptInvoices" : "acc.field.NoLinkedPaymentInvoices")], 2);
        }
    }
});
