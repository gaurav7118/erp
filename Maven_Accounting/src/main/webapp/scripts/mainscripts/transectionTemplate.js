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
/* <COMPONENT USED FOR>
 *      No. Label
 *          Calling Function
 *          [Options]
 * ---------------------------------------------------------------------------------------------
 *      1.Cash Sales
 *          callViewCashReceipt(rec,type,winid)
 *          [isCustomer:true, readOnly:true, rec: rec]
 *      2."Cash Sales"/"Invoice";
 *          callViewInvoice(rec,type,winid,inCash)
 *          [isCustomer:true, readOnly:true, rec: rec]
 *      3.Cash Sales
 *          callViewBillingCashReceipt(rec,type,winid)
 *          [inCash:true, isCustomer:true, isCustBill:true, readOnly:true, rec: rec]
 *      4.Invoice
 *          callViewBillingInvoice(rec,type,winid,inCash)
 *          [isCustomer:true, inCash:inCash, isCustBill:true, readOnly:true, rec: rec]
 *      5.Cash Purchase
 *          callViewPaymentReceipt(rec,type,winid)
 *          [isCustomer:false, readOnly:true, rec: rec]
 *      6."Cash Purchase"/"Goods Receipt"
 *          callViewGoodsReceipt(rec,type,winid,inCash)
 *          [isCustomer:false, readOnly:true, rec: rec]
 *      7.Credit Note
 *          callViewCreditNote(rec,type,winid)
 *          [isCustomer:true, readOnly:true, rec: rec]
 *      8.Debit Note
 *          callViewDebitNote(rec,type,winid)
 *          [isCustomer:false, readOnly:true, rec: rec]
 *      9.'Receive Payment'/'Payment Made'
 *          callViewPayment(rec,winid,typeCheck)
 *          [isCustomer:(typeCheck?true:false), readOnly:true, rec: rec]
 *      10.'Receive Payment'/'Payment Made'
 *          callViewBillPayment(rec,winid,typeCheck)
 *          [isCustomer:true,  readOnly:true, receiptTemp:true, isBillReceipt:true, rec: rec]
 * ---------------------------------------------------------------------------------------------
 */
Wtf.account.TransectionTemplate=function(config){
    this.isCustomer=config.isCustomer;
    this.isCustBill=config.isCustBill;
    this.inCash=config.inCash;
    this.isEdit=config.isEdit;
    this.isBillReceipt=config.isBillReceipt;
    this.businessPerson='Customer';
    if(config.isCustomer==false)
        this.businessPerson='Vendor';
    this.label=config.label;
    this.name = config.name;
    this.noteTemp=false;
    this.receiptTemp=false;
    this.isCard=false;
    this.isBank=false;
    this.isexpenseinv=false;
    this.cntype=config.cntype;
    /*
    this.storeMode=this.isCustBill?16:12;
    if(this.label=='Credit Note'){
        this.noteTemp=true;
        this.storeMode=this.isCustBill?62:27;
    }
    if(this.label=='Debit Note'){
        this.noteTemp=true;
        this.storeMode=this.isCustBill?62:28;
    }
    if(this.label=='Receive Payment' || this.label=='Payment Made'){
        this.receiptTemp=true;
        this.storeMode=this.isCustBill?35:32;
    }
*/
    this.StoreUrl = "";
    this.subGridStoreUrl = "";
    if (this.businessPerson=='Customer') {
        this.storeMode = this.isCustBill?16:12;
        this.StoreUrl = this.isCustBill?"ACCInvoiceCMN/getBillingInvoices.do":"ACCInvoiceCMN/getInvoices.do";
        this.subGridStoreUrl = this.isCustBill?"ACCInvoiceCMN/getBillingInvoiceRows.do":"ACCInvoiceCMN/getInvoiceRows.do";

        if(this.label=='Credit Note'){
            this.noteTemp=true;
            this.storeMode = this.isCustBill?62:27;
            this.StoreUrl = "ACCCreditNote/getCreditNoteMerged.do";
            this.subGridStoreUrl = this.isCustBill?"ACCCreditNote/getBillingCreditNoteRows.do":"ACCCreditNoteCMN/getCreditNoteRows.do";
        }

        if(this.label=='Receive Payment' || this.label=='Payment Made'){
            this.receiptTemp=true;
            this.storeMode=this.isCustBill?35:32;
            this.StoreUrl = "ACCReceipt/getReceipts.do";
            this.subGridStoreUrl = this.isCustBill?"ACCReceipt/getBillingReceiptRows.do":"ACCReceiptCMN/getReceiptRows.do";
        }    
    } else if (this.businessPerson=='Vendor') {
        this.storeMode = this.isCustBill?16:12;
        this.StoreUrl = this.isCustBill?"ACCGoodsReceiptCMN/getBillingGoodsReceipts.do":"ACCGoodsReceiptCMN/getGoodsReceipts.do";
        this.subGridStoreUrl = this.isCustBill?"ACCGoodsReceiptCMN/getBillingGoodsReceiptRows.do":"ACCGoodsReceiptCMN/getGoodsReceiptRows.do";

        if(this.label=='Debit Note'){
            this.noteTemp=true;
            this.storeMode = this.isCustBill?62:28;
            this.StoreUrl = "ACCDebitNote/getDebitNoteMerged.do";
            this.subGridStoreUrl = this.isCustBill?"ACCDebitNote/getBillingDebitNoteRows.do":"ACCDebitNote/getDebitNoteRows.do";
        }
        if(this.label=='Receive Payment' || this.label=='Payment Made'){
            this.receiptTemp=true;
            this.storeMode = this.isCustBill?35:32;
            this.StoreUrl = "ACCVendorPayment/getPayments.do";
            this.subGridStoreUrl = this.isCustBill?"ACCVendorPayment/getBillingPaymentRows.do":"ACCVendorPaymentCMN/getPaymentRows.do";
        }
    }
    this.GridRec = Wtf.data.Record.create ([
        {name:'id'},
        {name:'billid'},
        {name:'billno'},
        {name:'billdate', type:'date'},
        {name:'paymentmethod'},
        {name:'noteid'},
        {name:'noteno'},
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'billto'},
        {name:'discount'},
        {name:'shipto'},
        {name:'refname'},
        {name:'refno'},
        {name:'mode'},
        {name:'taxamount'},
        {name:'no'},
        {name:'creationdate', type:'date', mapping:'date'},
        {name:'duedate', type:'date'},
        {name:'shipdate', type:'date'},
        {name:'customername'},
        {name:'personid'},
        {name:'personname'},
        {name:'shipping'},
        {name:'othercharges'},
        {name:'amount'},
        {name:'oldcurrencyrate'},
        {name: 'currencysymbol'},
        {name: 'currencyrate'},
        {name:'amountdue'},
        {name:'companyaddress'},
        {name:'companyname'},
        {name:'memo'},
        {name:'prtaxid'},
        {name:'taxamount'},
        {name: 'isexpenseinv'},
        {name:'prtaxpercent'}
    ]);
    this.store = new Wtf.data.Store({
        url : this.StoreUrl,
        baseParams:{
            mode:this.storeMode,
            noteid:config.rec.data.noteid?config.rec.data.noteid:"",
            cntype:this.cntype
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.GridRec)
    });
    this.store.on('load',this.loadTemplate,this);
    Wtf.account.TransectionTemplate.superclass.constructor.call(this,config);
    this.addEvents({
        update:true
    });
}
Wtf.extend(Wtf.account.TransectionTemplate,Wtf.Panel,{
    layout: 'border',
    closable: true,
    style:'padding:10px',
    border:true,
    onRender:function(config){
        this.addtemplate();
        this.addCalTemp();
        this.addForm();
        this.add(this.NorthForm);
        this.add(this.grid);
        this.add(this.SouthForm);
        Wtf.account.TransectionTemplate.superclass.onRender.call(this,config);
    },
    addForm:function(){
        this.NorthForm=new Wtf.form.FormPanel({
            region : "north",
            height:170,
            border:false,
            style:'background:#F1F1F1;padding:20px 10px 0px 30px',
            defaults:{border:false},
            labelWidth:140,
            items:[{
                layout:'form',
                defaults:{border:false},
                items:[this.headerCalTemp]
            },{
                layout:'column',
                defaults:{border:false},
                items:[{
                    layout:'form',
                    columnWidth:0.7,
                    defaults:{border:false},
                    items:[this.leftCalTemp]
                },{
                    layout:'form',
                    columnWidth:0.3,
                    items:[this.rightCalTemp]
                }]
            }]
        });
        this.createGrid();
        this.SouthForm=new Wtf.Panel({
            region:'south',
            height:145,
            border:false,
            bodyStyle:'background:#F1F1F1;',
            layout:'border',
            defaults:{border:false},
            items:[{
                region:'center',
                bodyStyle:'padding:70px 0px 0px 40px',
                defaults:{border:false},
                items:[this.leftSouthTemp]
            },{
                region:'east',
                bodyStyle:'padding:10px 0px 20px 0px',
                width:320,
                defaults:{border:false},
                items:[this.rightSouthTemp]
            }]
        });
    },
    addCalTemp:function(){
        this.headerCalTemp=new Wtf.Panel({
            border:false,
            baseCls:'tempbackgroundview',
            html:this.headerTplSummary.apply({transectionno:""})
        });
        this.leftCalTemp=new Wtf.Panel({
            border:false,
            baseCls:'tempbackgroundview',
            html:this.leftTplSummary.apply({to:"",Address:"",totalamount:""})
        });
        // TODO       this.centerCalTemp=new Wtf.Panel({
        //            border:false,
        //            baseCls:'tempbackgroundview',
        //            html:this.centerTplSummary.apply({from:"",compaddress:""})
        //        });

        this.rightCalTemp=new Wtf.Panel({
            border:false,
            baseCls:'tempbackgroundview',
            html:this.rightTplSummary.apply({transectionno:"",createdon:"",duedate:"",total:0})
        });
        this.leftSouthTemp=new Wtf.Panel({
            border:false,
            baseCls:'tempbackgroundview',
            html:this.leftSouthTplSummary.apply({memo:""})
        });
        this.rightSouthTemp=new Wtf.Panel({
            border:false,
            baseCls:'tempbackgroundview',
            html:this.rightSouthTplSummary.apply({subtotal:0,discount:0,total:0,tax:0,aftertaxamt:0})
        });
    },
    addLeftTemp:function(){
        var paymentmethod='';
        if(this.receiptTemp){
            paymentmethod='<trclass="templabelview"><td class="tempdataview"><b>'+WtfGlobal.getLocaleText("acc.mp.payType")+':</b></td><td >{paymentmethod}  </td>';
            paymentmethod+='<tpl if="'+this.Bank+'">',
            paymentmethod+='<td class="tempdataview"><b>Check No:</b></td><td >{refno}  </td>';
            paymentmethod+='<td class="tempdataview"><b>Bank Name:</b></td><td >{refname}  </td></tr>';
            paymentmethod+='</tpl>',
            paymentmethod+='<tpl if="'+this.Card+'">',
            paymentmethod+='<td class="tempdataview"><b>Reference No:</b></td><td >{refno}</td>';
            paymentmethod+='<td class="tempdataview"><b>Card Name:</b></td><td >{refname}</td></tr>';
            paymentmethod+='</tpl>'
        }
        this.headerTplSummary=new Wtf.XTemplate(
            '<div class="currency-view tempheaderview" >'+this.name+' '+WtfGlobal.getLocaleText("acc.nee.50")+': {transectionno}</div>'
            );
        this.leftTplSummary=new Wtf.XTemplate(
            '<div class="currency-view temptextview" >',
            '<table >',
            '<tr><td class="templabelview"><b>'+WtfGlobal.getLocaleText("acc.common.to")+',</b></td></tr>',
            '<tr><td class="tempdataview">{to}</td></tr>',
            '<tpl if="'+!(this.noteTemp ||this.receiptTemp)+'">',
            '<tr><td class="tempdataview">{address}</td></tr>',
            '</tpl>','</table>',
            '<br ><br >',
            '<table >',
            paymentmethod,
            '</table>',
            '</div>'
            );
    },

    addtemplate:function(){

        this.addLeftTemp();

        this.rightTplSummary=new Wtf.XTemplate(
            '<div class="currency-view temptabledivview">',
            '<table class="template">',
            '<tr><td class="templabelview"><b>'+WtfGlobal.getLocaleText("acc.nee.51")+': </b></td><td class="templabelview">{createdon}</td></tr>',
            '<tpl if="'+!(this.noteTemp ||this.receiptTemp)+'">',
            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.agedPay.gridDueDate")+': </b></td><td >{duedate}</td></tr>',
            '<tr><td class="templabelview"><b>'+(!(this.noteTemp ||this.receiptTemp)?WtfGlobal.getLocaleText("acc.agedPay.gridAmtDue"):WtfGlobal.getLocaleText("acc.invoiceList.totAmt"))+':</b></td><td class="templabelview">{total}</td></tr>',
            '</tpl>',
            '</table>',
            '</div>'
            );
        this.rightSouthTplSummary=new Wtf.XTemplate(
            '<div class="currency-view temptextview">',
            '<table class="template">',
            '<tpl if="'+(!this.noteTemp && !this.receiptTemp)+'">',
            '<tr><td class="templabelview"><b>'+WtfGlobal.getLocaleText("acc.invoice.subTotal")+'</b></td><td class="templabelview">{subtotal}</td></tr>',
            '</tpl>',
            '<tpl if="'+!(this.noteTemp ||this.receiptTemp)+'">',
            '<tr><td><b>- '+WtfGlobal.getLocaleText("acc.invoiceList.discount")+': </b></td><td >{discount}</td></tr>',
            '</table>',
            '<table class="template">',
            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoiceList.expand.amt")+': </b></td><td align=right>{total}</td></tr>',
            '<tr><td><b>+ '+WtfGlobal.getLocaleText("acc.up.12")+': </b></td><td align=right>{tax}</td></tr>',
            '</table>',
            '</tpl>',
            '<tpl if="'+(this.noteTemp)+'">',
            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoice.subTotal")+' </b></td><td align=right>{noteSubTotal}</td></tr>',
            '<tr><td><b>- '+WtfGlobal.getLocaleText("acc.invoiceList.discount")+': </b></td><td >{totalDiscount}</td></tr>',
            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.up.12")+': </b></td><td align=right>{totalTax}</td></tr>',
            '</tpl>',
            '<table class="template"><tr><td class="templabelview"><b>'+(this.receiptTemp?"Total Amount Paid":WtfGlobal.getLocaleText("acc.invoiceList.totAmt"))+':</b></td><td class="templabelview">{aftertaxamt}</td></tr>',
            '</table>',
            '</div>'
            );
        this.leftSouthTplSummary=new Wtf.XTemplate(
            '<div class="currency-view tempmemoview">',
            '<b>'+Wtf.account.companyAccountPref.descriptionType+' : </b>{memo}',
            '</div>'
            );
    },
    loadTemplate:function(store){
        var data=this.rec.data;
        var id;
        var idlabel;
        var  mode;
        if(this.noteTemp){
            id= data.noteid;
            idlabel="noteid";
            if(this.label=='Debit Note'){
                mode=this.isCustBill?63:29;
            }else{
                mode=this.isCustBill?63:28;
            }
        }else if(this.receiptTemp){
            id= data.billid;
            idlabel="billid";
            mode=this.isCustBill?36:33;
        }else{
            id=data.billid;
            idlabel="billid";
            mode=this.isCustBill?17:14;
        }
        store.filter(idlabel,id);
        var fRec=store.getAt(0);
        this.Bank=fRec.data['paymentmethod']=="Cheque"?true:false;
        this.Card=fRec.data['paymentmethod']=="Card"?true:false;
        this.addtemplate();
        var no=this.noteTemp?fRec.data['noteno']:fRec.data['billno'];
        this.grid.getStore().proxy.conn.url = this.subGridStoreUrl;
        this.grid.getStore().load({params:{bills:id,mode:mode,isexpenseinv:this.isexpenseinv}});
    this.grid.getStore().on('load',this.getCurrencySymbol,this);
        this.headerTplSummary.overwrite(this.headerCalTemp.body,{transectionno:no});
        this.leftTplSummary.overwrite(this.leftCalTemp.body,{to:unescape(fRec.data['personname']).replace(/\+/g,' '),address:fRec.data['billto'],paymentmethod:fRec.data['paymentmethod'],refno:fRec.data['refno'],refname:fRec.data['refname']});
    // TODO  this.centerTplSummary.overwrite(this.centerCalTemp.body,{from:fRec.data['companyname'],compaddress:fRec.data['companyaddress'],paymentmethod:fRec.data['paymentmethod']});
        this.rightTplSummary.overwrite(this.rightCalTemp.body,{transectionno:fRec.data['no'],createdon:this.receiptTemp?WtfGlobal.onlyDateRightRenderer(fRec.data['billdate']):WtfGlobal.onlyDateRightRenderer(fRec.data['creationdate']),duedate:WtfGlobal.onlyDateRightRenderer(fRec.data['duedate']),total:WtfGlobal.addCurrencySymbolOnly((!(this.noteTemp ||this.receiptTemp)?fRec.data['amountdue']:fRec.data['amount']),fRec.data['currencysymbol'])});
        this.leftSouthTplSummary.overwrite(this.leftSouthTemp.body,{memo:fRec.data['memo']});
        this.rightSouthTplSummary.overwrite(this.rightSouthTemp.body,{subtotal:WtfGlobal.addCurrencySymbolOnly(fRec.data['amount']+fRec.data['discount']-fRec.data['taxamount'],fRec.data['currencysymbol']),discount:WtfGlobal.addCurrencySymbolOnly(fRec.data['discount'],fRec.data['currencysymbol']),total:WtfGlobal.addCurrencySymbolOnly(fRec.data['amount']-fRec.data['taxamount'],fRec.data['currencysymbol']),tax:WtfGlobal.addCurrencySymbolOnly(fRec.data['taxamount'],fRec.data['currencysymbol']),aftertaxamt:WtfGlobal.addCurrencySymbolOnly(fRec.data['amount'],fRec.data['currencysymbol']),noteTax:WtfGlobal.addCurrencySymbolOnly(fRec.json['notetax'],fRec.data['currencysymbol']),noteSubTotal:WtfGlobal.addCurrencySymbolOnly(fRec.json['noteSubTotal'],fRec.data['currencysymbol']),totalTax:WtfGlobal.addCurrencySymbolOnly(fRec.json['totalTax'],fRec.data['currencysymbol']),totalDiscount:WtfGlobal.addCurrencySymbolOnly(fRec.json['totalDiscount'],fRec.data['currencysymbol'])});
    this.Bank=false;
    this.Card=false;
},

getCurrencySymbol:function(){
    var symbol="";
    var subTot=0;
    if(this.grid.getStore().getCount()>0){
        var rec=this.grid.getStore().getAt(0);
        symbol=  rec.data["currencysymbol"];

        if(this.label=='Debit Note' || this.label=='Credit Note'){
            for(var i=0; i< this.grid.getStore().getCount();i++){
                var recTemp = this.grid.getStore().getAt(0);
                subTot += recTemp.data["discount"];
            }
            var fRec=this.store.getAt(0);
                this.rightSouthTplSummary.overwrite(this.rightSouthTemp.body,{noteSubTotal:WtfGlobal.addCurrencySymbolOnly(fRec.json['noteSubTotal'],symbol),aftertaxamt:WtfGlobal.addCurrencySymbolOnly(fRec.data['amount'],symbol),noteTax:WtfGlobal.addCurrencySymbolOnly(fRec.json['notetax'],symbol),totalTax:WtfGlobal.addCurrencySymbolOnly(fRec.json['totalTax'],symbol),totalDiscount:WtfGlobal.addCurrencySymbolOnly(fRec.json['totalDiscount'],symbol)});
        }
    }
    return symbol;

},
refreshView:function(rec){
    this.rec=rec;
    this.isexpenseinv=this.rec.data.isexpenseinv;
    this.store.load();
},
createGrid:function(){
    var isCN=this.label=='Credit Note'?true:false;
    if(!this.receiptTemp) {
        //            if(this.isCustBill){
        //                this.grid=new Wtf.account.BillingProductDetailsGrid({
        //                    region:'center',
        //                    style:'padding:0px 20px 10px 20px;background:#F1F1F1;',
        //                    readOnly:true,
        //                    autoScroll:true,
        //                    noteTemp:this.noteTemp,
        //                    isNote:this.noteTemp,
        //                    isCN:isCN,
        //                    isCustomer:this.isCustomer,
        //                    isCustBill:this.isCustBill,
        //                    border:false
        //                });
        //            }else 
        if(this.isexpenseinv){
            this.grid = new Wtf.account.ExpenseInvoiceGrid({
                height: 200,
                region:'center',
                style:'padding:0px 20px 10px 20px;background:#F1F1F1;',
                readOnly:true,
                border:true,
                title: 'Expense',
                        viewConfig:{forceFit:true},
                isCustomer:this.isCustomer,
                editTransaction:this.isEdit,
                isCustBill:this.isCustBill,
                id:this.id+"expensegrid",
                fromOrder:true,
                closable: false,
                isOrder:this.isOrder,
                forceFit:true,
                loadMask : true
            });
        }else{
        //                  if(Optimized_CompanyIds.indexOf(companyid)!= -1){
        //                    this.grid=new Wtf.account.ProductDetailsGridOptimized({
        //                        region:'center',
        //                        style:'padding:0px 20px 10px 20px;background:#F1F1F1;',
        //                        autoScroll:true,
        //                        noteTemp:this.noteTemp,
        //                        isNote:this.noteTemp,
        //                        readOnly:true,
        //                        isCN:isCN,
        //                        isViewCNDN:(this.label=='Credit Note' || this.label=='Debit Note')?true:false,
        //                        isCustomer:this.isCustomer,
        //                        layout:'fit',
        //                        viewConfig:{
        //                            forceFit:true
        //                        },
        //                        loadMask : true
        //                    });
        //                  }else
        {
            this.grid=new Wtf.account.ProductDetailsGrid({
                region:'center',
                style:'padding:0px 20px 10px 20px;background:#F1F1F1;',
                autoScroll:true,
                noteTemp:this.noteTemp,
                isNote:this.noteTemp,
                readOnly:true,
                isCN:isCN,
                isViewCNDN:(this.label=='Credit Note' || this.label=='Debit Note')?true:false,
                isCustomer:this.isCustomer,
                layout:'fit',
                viewConfig:{
                    forceFit:true
                },
                loadMask : true
            });
        }   
        }
    } else {
        this.grid = new Wtf.account.OSDetailGrid({
            region:'center',
            readOnly:true,
            border:false,
            isCustomer:this.isCustomer,
            isCustBill:this.isCustBill,
            viewConfig:{
                forceFit:true,
                emptyText:"<div class='grid-empty-text'>"+WtfGlobal.getLocaleText("acc.field.Nodatatodisplay")+"</div>"
            },
            isReceipt:this.label=='Receive Payment'?true:false,
            isTemplate:true,
            amount:0,
            id : this.label=='Receive Payment'?'viewcustomergrid':'viewvendorgrid',
            closable: true
        });

    }
}
});

/*Call delivery order report*/

function callDeliveryOrderList(params){
    if(params == undefined){
        params={};
    }
    var consolidateFlag=params.consolidateFlag || false;
    var searchStr=params.searchStr || "";
    var filterAppend=params.filterAppend|| "";
    var moduleid=params.moduleid || "";
    var reportbtnshwFlag=params.reportbtnshwFlag|| false;
    var pendingapproval=params.pendingapproval || false;
    var isFixedAsset=params.isFixedAsset || false;
    var isLeaseFixedAsset=params.isLeaseFixedAsset || false;
    var isUnInvoiceDOReport=params.isUnInvoiceDOReport || false;
    var isJobWorkOutReciever=params.isJobWorkOutReciever || false;
    var isCustomWidgetReport=params.isCustomWidgetReport||false;
    if((!WtfGlobal.EnableDisable(Wtf.UPerm.deliveryreport, Wtf.Perm.deliveryreport.viewdo)&&!isFixedAsset &&!isLeaseFixedAsset)||(!WtfGlobal.EnableDisable(Wtf.UPerm.assetsales, Wtf.Perm.assetsales.viewfado)&&isFixedAsset &&!isLeaseFixedAsset)||isLeaseFixedAsset) {
        var id = 'DeliveryOrderList';
        if(isFixedAsset){
            id = 'FixedAssetDeliveryOrderList';
        }else if(isLeaseFixedAsset){
            id = 'LeaseFixedAssetDeliveryOrderList';
        }else if(isUnInvoiceDOReport){
            id = 'UnInvoiceDeliveryOrderList';
        }
        id = consolidateFlag?id+'Merged':id; 
        id = pendingapproval ? id + 'Pending' : id;
        id = reportbtnshwFlag ? id : id +'Entry';
        var panel = Wtf.getCmp(id);
        if (params.isCustomWidgetReport) {
        /*
         *Implementation to add this report in custom widget report. 
         * */
        panel = new Wtf.account.DeliveryListPanel({
                border : false,
                consolidateFlag:consolidateFlag,
                isOrder:true,
                pendingapproval : pendingapproval,
                isCustomer:true,
                searchJson: searchStr,
                filterConjuctionCrit:filterAppend,
                isFixedAsset:isFixedAsset,
                isLeaseFixedAsset:isLeaseFixedAsset,
                reportbtnshwFlag:reportbtnshwFlag,
                helpmodeid:52,
                moduleid:isFixedAsset?Wtf.Acc_FixedAssets_DeliveryOrder_ModuleId:isLeaseFixedAsset?Wtf.Acc_Lease_DO:Wtf.Acc_Delivery_Order_ModuleId,
                label:isFixedAsset?WtfGlobal.getLocaleText("acc.fixedAssetDeliveryOrder"):WtfGlobal.getLocaleText("acc.accPref.autoDO"),  //'Sales Order',
                layout: 'fit',
                iconCls: 'accountingbase salesorderlist',
                isUnInvoiceDOReport:isUnInvoiceDOReport,
                isCustomWidgetReport:isCustomWidgetReport
            });
        if (params.callbackFn) {
            /*
             *call callback function to add this report to widget.
             **/
            params.callbackFn.call(this, panel);
        }
    }else{
        if(panel==null){
            panel = new Wtf.account.DeliveryListPanel({
                id : id,
                border : false,
                consolidateFlag:consolidateFlag,
                isOrder:true,
                pendingapproval : pendingapproval,
                isCustomer:true,
                searchJson: searchStr,
//                isJobWorkOutReciever:isJobWorkOutReciever,
                isFixedAsset:isFixedAsset,
                isLeaseFixedAsset:isLeaseFixedAsset,
                reportbtnshwFlag:reportbtnshwFlag,
                filterConjuctionCrit:filterAppend,
                //            moduleId:moduleid,
                helpmodeid:52,
                moduleid:isFixedAsset?Wtf.Acc_FixedAssets_DeliveryOrder_ModuleId:isLeaseFixedAsset?Wtf.Acc_Lease_DO:Wtf.Acc_Delivery_Order_ModuleId,
                //           title: pendingapproval ? WtfGlobal.getLocaleText("acc.field.PendingApprovalDOReport"): WtfGlobal.getLocaleText("acc.dashboard.consolidateDeliveryOrderReport"),//Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.soList.tabTitle"), Wtf.TAB_TITLE_LENGTH),
                //          tabTip: pendingapproval ? WtfGlobal.getLocaleText("acc.field.PendingApprovalDOReport"): WtfGlobal.getLocaleText("acc.dashboard.consolidateDeliveryOrderReport"),
                title: isUnInvoiceDOReport?"Unprinted Delivery Order Report":isFixedAsset?WtfGlobal.getLocaleText("acc.fixedAssetDeliveryOrderReport"):isLeaseFixedAsset? (WtfGlobal.getLocaleText("acc.lease.delivery.order.report")) : (pendingapproval ? WtfGlobal.getLocaleText("acc.field.PendingApprovalDOReport"): (reportbtnshwFlag?WtfGlobal.getLocaleText("acc.dashboard.consolidateDeliveryOrderReport"):WtfGlobal.getLocaleText("acc.delivery.orderreport"))),
                tabTip:isUnInvoiceDOReport?"Unprinted Delivery Order Report":isFixedAsset?WtfGlobal.getLocaleText("acc.fixedAssetDeliveryOrderReport"):isLeaseFixedAsset? (WtfGlobal.getLocaleText("acc.lease.delivery.order.report")) : (pendingapproval ? WtfGlobal.getLocaleText("acc.field.PendingApprovalDOReport"):(reportbtnshwFlag?WtfGlobal.getLocaleText("acc.dashboard.consolidateDeliveryOrderReport"):WtfGlobal.getLocaleText("acc.delivery.orderreport"))),
                label:isFixedAsset?WtfGlobal.getLocaleText("acc.fixedAssetDeliveryOrder"):WtfGlobal.getLocaleText("acc.accPref.autoDO"),  //'Sales Order',
                layout: 'fit',
                closable: true,
                iconCls: 'accountingbase salesorderlist',
                searchJson: searchStr,
                filterConjuctionCrit:filterAppend,
                isUnInvoiceDOReport:isUnInvoiceDOReport
            });
            Wtf.getCmp('as').add(panel);
            panel.on('journalentry', callJournalEntryDetails);
        }
    
        var channelName = "";
    
        if (!isFixedAsset && pendingapproval == undefined) {
            channelName = Wtf.ChannelName.DeliveryOrderReport;
        }
    
        panel.on('beforeclose', function() {
            beforeClose(channelName);
        }, this);
    
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
        showAdvanceSearch(panel,searchStr, filterAppend);
    }
}else{
        WtfComMsgBox(46,0,false,(isFixedAsset?WtfGlobal.getLocaleText("erp.permission.fado"):(isLeaseFixedAsset?WtfGlobal.getLocaleText("erp.permission.leasedo"):WtfGlobal.getLocaleText("erp.permission.do"))));
    }
}

/* Tab for SO/DO required to create bulk invoice*/
function callBulkInvoicesList(moduleid, gridStore,isfromReportList,isCustomer) {
    var id = 'BulkInvoices';
    var panel = Wtf.getCmp(id);
    if (panel == null) {
        panel = new Wtf.account.BulkInvoicesList({
            id: id,
            border: false,
            isOrder: true,
            isFromSO:(moduleid==Wtf.Acc_Sales_Order_ModuleId) ? true : false,
            isCustomer:(moduleid==Wtf.Acc_Sales_Order_ModuleId) ? true : false,
            helpmodeid: 52,
            moduleid:moduleid,
            isfromReportList:isfromReportList,
            isCustomer:isCustomer,
            title:isfromReportList ? (moduleid==Wtf.Acc_Sales_Order_ModuleId ? WtfGlobal.getLocaleText("acc.common.bulkDO") : WtfGlobal.getLocaleText("acc.common.bulkGR")): WtfGlobal.getLocaleText("acc.common.bulkInvoice"),
            tabTip: isfromReportList ? (moduleid==Wtf.Acc_Sales_Order_ModuleId ? WtfGlobal.getLocaleText("acc.common.bulkDO") : WtfGlobal.getLocaleText("Create Bulk GR")): WtfGlobal.getLocaleText("acc.common.bulkInvoice"),
            layout: 'fit',
            closable: true,
            iconCls: 'accountingbase salesorderlist'
        });
        Wtf.getCmp('as').add(panel);        
        panel.on("loadMainReportTab", function() {
            if (gridStore) {
                gridStore.reload();
            }
        });  
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();

}


/*Call Sales Return report*/

function callSalesReturnList(consolidateFlag,reportbtnshwFlag,isLeaseFixedAsset,titlelabel,searchStr, filterAppend){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.salesreturn, Wtf.Perm.salesreturn.viewsret)||isLeaseFixedAsset) {
        consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
        isLeaseFixedAsset = (isLeaseFixedAsset)?isLeaseFixedAsset:false;
        var id = 'SalesReturnList';
        if(isLeaseFixedAsset){
            id = 'LeaseSalesReturnList';
        }
        id = consolidateFlag?id+'Merged':id;   
        id = reportbtnshwFlag?id:id+'Entry';
        var panel = Wtf.getCmp(id);
        if(panel==null){
            panel = new Wtf.account.SalesReturnListPanel({
                id : id,
                border : false,
                consolidateFlag:consolidateFlag,
                isOrder:true,
                isCustomer:true,
                helpmodeid:53,
                isLeaseFixedAsset:isLeaseFixedAsset,
                reportbtnshwFlag:reportbtnshwFlag,
                moduleid:isLeaseFixedAsset?Wtf.Acc_Lease_Return:Wtf.Acc_Sales_Return_ModuleId,
                title: (isLeaseFixedAsset)?WtfGlobal.getLocaleText("acc.field.LeaseSalesReturnReport"):WtfGlobal.getLocaleText("acc.field.SalesReturnReport"),//Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.soList.tabTitle"), Wtf.TAB_TITLE_LENGTH),
                tabTip:(isLeaseFixedAsset)?WtfGlobal.getLocaleText("acc.field.LeaseSalesReturnReport"):WtfGlobal.getLocaleText("acc.field.SalesReturnReport"),
                label:(isLeaseFixedAsset)?WtfGlobal.getLocaleText("acc.accPref.leaseautoSR"):WtfGlobal.getLocaleText("acc.accPref.autoSR"),  //'Sales Order',
                layout: 'fit',
                closable: true,
                iconCls:'accountingbase salesorderlist',
                searchJson: searchStr,
            filterConjuctionCrit:filterAppend
            });
            Wtf.getCmp('as').add(panel);
            panel.on('journalentry',callJournalEntryDetails);
        }
        var channelName =Wtf.ChannelName.SalesReturnReport;
        panel.on('beforeclose', function(){
            beforeClose(channelName);
        },this);
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
        showAdvanceSearch(panel,searchStr, filterAppend);
    } else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.up.34"));
}
function callPurchaseReturnList(consolidateFlag,reportbtnshwFlag,titlelabel,searchStr, filterAppend){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.purchasereturn, Wtf.Perm.purchasereturn.viewpret)){
        consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
        var id = 'PurchaseReturnList';
        id = consolidateFlag?id+'Merged':id;    
        id = reportbtnshwFlag?id:id+'Entry';
        var panel = Wtf.getCmp(id);
        if(panel==null){
            panel = new Wtf.account.SalesReturnListPanel({
                id : id,
                border : false,
                consolidateFlag:consolidateFlag,
                isOrder:true,
                isCustomer:false,
                helpmodeid:54,
                reportbtnshwFlag:reportbtnshwFlag,
                moduleid:Wtf.Acc_Purchase_Return_ModuleId,
                title: (titlelabel!=undefined)?titlelabel:WtfGlobal.getLocaleText("acc.field.PurchaseReturnReport"),//Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.soList.tabTitle"), Wtf.TAB_TITLE_LENGTH),
                tabTip: (titlelabel!=undefined)?titlelabel:WtfGlobal.getLocaleText("acc.field.PurchaseReturnReport"),
                label:WtfGlobal.getLocaleText("acc.dimension.module.18"),  //'Sales Order',
                layout: 'fit',
                closable: true,
                iconCls:'accountingbase salesorderlist',
                searchJson: searchStr,
            filterConjuctionCrit:filterAppend
            });
            Wtf.getCmp('as').add(panel);
            panel.on('journalentry',callJournalEntryDetails);
        }
        var channelName =Wtf.ChannelName.PurchaseReturnReport;
        panel.on('beforeclose', function(){
            beforeClose(channelName);
        },this);
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
        showAdvanceSearch(panel,searchStr, filterAppend);
    } else
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.pi.PurchaseReturn"));
}

function callGoodsReceiptOrderList(consolidateFlag,reportbtnshwFlag,isFixedAsset,pendingapproval,titlelabel,searchStr, filterAppend,isJobWorkOutGRO){
    if((!WtfGlobal.EnableDisable(Wtf.UPerm.goodsreceiptreport, Wtf.Perm.goodsreceiptreport.viewgr)&&!isFixedAsset) ||(!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchase, Wtf.Perm.assetpurchase.veiwfagr)&&isFixedAsset)) {
        consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
        var id = 'GoodsReceiptDeliveryOrderList';
        isFixedAsset = (isFixedAsset==null || isFixedAsset==undefined)?false:isFixedAsset;
        id = consolidateFlag?id+'Merged':id;
        id = pendingapproval ? id + 'Pending' : id;
        id = reportbtnshwFlag ? id : id + 'Entry';
        id = isFixedAsset ? id+'FixedAsset' : id;
          id = isJobWorkOutGRO ? id+'isJobWorkOutGRO' : id;
        var panel = Wtf.getCmp(id);
        if(panel==null){
            panel = new Wtf.account.DeliveryListPanel({
                id : id,
                border : false,
                isOrder:true,
                pendingapproval : pendingapproval,
                consolidateFlag:consolidateFlag,
                isCustomer:false,
                helpmodeid:21,
                moduleid:isFixedAsset ? Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId : Wtf.Acc_Goods_Receipt_ModuleId,
                isFixedAsset:isFixedAsset,
                reportbtnshwFlag:reportbtnshwFlag,
                title: (titlelabel!=undefined)?titlelabel:isFixedAsset?WtfGlobal.getLocaleText("acc.fixedasset.reciptlist"):isJobWorkOutGRO ? WtfGlobal.getLocaleText("acc.JobWorkOut.GRN") :pendingapproval ? WtfGlobal.getLocaleText("acc.field.PendingApprovalGRReport") : WtfGlobal.getLocaleText("acc.dashboard.consolidateGoodsReceiptOrderReport"), // "Pending Approval GR Report" : "Goods Receipt Report", // Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.soList.tabTitle"), Wtf.TAB_TITLE_LENGTH),
                tabTip: (titlelabel!=undefined)?titlelabel:isFixedAsset?WtfGlobal.getLocaleText("acc.fixedasset.reciptlist"):isJobWorkOutGRO ? WtfGlobal.getLocaleText("acc.JobWorkOut.GRN") :pendingapproval ?  WtfGlobal.getLocaleText("acc.field.PendingApprovalGRReport") : WtfGlobal.getLocaleText("acc.dashboard.consolidateGoodsReceiptOrderReport"), // "Pending Approval GR Report" : "Goods Receipt Report",
                label:WtfGlobal.getLocaleText("acc.accPref.autoGRO"),  //'Goods Receipt',
                layout: 'fit',
                closable: true,
                isJobWorkOutGRO : isJobWorkOutGRO,
                iconCls:'accountingbase salesorderlist',
                                                                                                                                           searchJson: searchStr,
            filterConjuctionCrit:filterAppend
            });
            panel.on('journalentry', callJournalEntryDetails);
            Wtf.getCmp('as').add(panel);
        }
    
        var channelName = "";
    
        if (!isFixedAsset && pendingapproval == undefined) {
            channelName = Wtf.ChannelName.GoodsReceiptReport;
        }
    
        panel.on('beforeclose', function() {
            beforeClose(channelName);
        }, this);
    
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
        showAdvanceSearch(panel,searchStr, filterAppend);
    }else{
        WtfComMsgBox(46,0,false,isFixedAsset?WtfGlobal.getLocaleText("erp.permission.fagr"):WtfGlobal.getLocaleText("erp.permission.gr"));
    }
}

function callPackingDoListReport(consolidateFlag){
    consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
    var id = 'PackingDoListReport';
    var panel = Wtf.getCmp(id);
    if(panel==null){
        panel = new Wtf.account.PackingDoListReportPanel({
            id : id,
            border : false,
            consolidateFlag:consolidateFlag,
            isCustomer:true,
            title: WtfGlobal.getLocaleText("erp.PackingDoListReport"),
            tabTip:WtfGlobal.getLocaleText("erp.PackingDoListReport"),
            label:WtfGlobal.getLocaleText("erp.PackingDoListReport"),
            layout: 'fit',
            closable: true,
            iconCls:'accountingbase salesorderlist'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
/*
 * Function for packing report
 */
function callPackingReport(consolidateFlag){
    consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
    var id = 'PackingReport';
    var panel = Wtf.getCmp(id);
    if(panel==null){
        panel = new Wtf.account.PackingReportPanel({
            id : id,
            border : false,
            consolidateFlag:consolidateFlag,
            isCustomer:true,
            title: WtfGlobal.getLocaleText("erp.PackingReport"),
            tabTip:WtfGlobal.getLocaleText("erp.PackingReport"),
            label:WtfGlobal.getLocaleText("erp.PackingReport"),
            layout: 'fit',
            closable: true,
            iconCls:'accountingbase salesorderlist'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callBudgetingWin() {
    if (Wtf.account.companyAccountPref.activatebudgetingforPR) {
        var winID = Wtf.getCmp("budgetingWin");
        if (!winID) {
            new Wtf.account.Budgeting({
                title: WtfGlobal.getLocaleText("acc.field.budgeting"), // "Budgeting",
                iconCls: 'accountingbase pricelistbutton',
                scope: this,
                modal: true
            }).show();
        }
    } else {
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing") + " " + WtfGlobal.getLocaleText("acc.wtfTrans.vpBudgeting"));
    }
}

function callCostAndMarginWindow(gridStore, productComboStore, exchangeRate, parentObj) {
    var panel = Wtf.getCmp('costAndMarginWindow');
    if (!panel) {
        new Wtf.account.costAndMargin({
            title: WtfGlobal.getLocaleText("acc.field.margin"), // "Margin",
            id: 'costAndMarginWindow',
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            closable: true,
            modal: true,
            gridStore: gridStore,
            productComboStore: productComboStore,
            exchangeRate: exchangeRate,
            parentObj: parentObj,
            height: 340,
            width: 980,
            resizable:false
        }).show();
    }
}

function callSetPriceListForBandWindow(record,productStore) {
    var panel = Wtf.getCmp('setPriceForBandWin');
    if (!panel) {
        new Wtf.account.setPriceListForBandWindow({
            title: WtfGlobal.getLocaleText("acc.field.setPriceForBand"), // "Set Price for Band",
            id: 'setPriceForBandWin',
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            closable: true,
            modal: true,
            record: record,
            productStore: productStore,
            width: 1100,
            height: 615
        }).show();
    }
}

function callsetPriceForVolumeDiscountWindow(record,productStore) {
    var panel = Wtf.getCmp('setPriceForVolumeDiscountWin');
    if (!panel) {
        new Wtf.account.setPriceListForBandWindow({
            title: WtfGlobal.getLocaleText("acc.field.setPriceForPriceListVolumeDiscount"), // "Set Price for Price List - Volume Discount",
            id: 'setPriceForVolumeDiscountWin',
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            closable: true,
            modal: true,
            record: record,
            productStore: productStore,
            isFlatPriceListVolumeDiscount: true,
            width: 1100,
            height: 640
        }).show();
    }
}

function callFixedAssetPurchaseReq(isEdit, rec, winid, pendingapproval,thisObj,copyInv) {
    winid = (winid != undefined)? 'assetRequisition' + winid : 'assetRequisition';
    var panel = Wtf.getCmp(winid);
    if (copyInv == undefined || copyInv == "" || copyInv == null) {
        copyInv = false;
    }
    if((!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchasereq, Wtf.Perm.assetpurchasereq.createapreq)&&!copyInv&&!isEdit)||copyInv||isEdit){
        if (panel == null) {
            panel = new Wtf.account.FixedAssetPurchaseRequisitionPanel({
                quotation: true,
                id: winid,
                isRequisition: true,
                PRthisObj: thisObj,
                copyInv: copyInv,
                pendingapproval: pendingapproval,
                isCustomer: false, // Issue 32006 - [Inventory Assembly]Inventory Assembly type of product should not be there in combo on purchase side.
                moduleid: Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId,
                isOrder: (isEdit!==undefined && isEdit)? false : true,
                isFixedAsset: true,
                record: rec,
                heplmodeid:87,
                isEdit: isEdit!==undefined ? isEdit : false,
                label: WtfGlobal.getLocaleText("acc.accPref.autoPRequisition"),
                border: false,
                title: isEdit!==undefined ? (copyInv != undefined && copyInv == true)?WtfGlobal.getLocaleText("acc.field.copyAssetPurchaseRequisition"):WtfGlobal.getLocaleText("acc.field.editAssetPurchaseRequisition") : WtfGlobal.getLocaleText("acc.field.assetPurchaseRequisition"),
                tabTip: isEdit!==undefined ?(copyInv != undefined && copyInv == true)?WtfGlobal.getLocaleText("acc.field.copyAssetPurchaseRequisition"): WtfGlobal.getLocaleText("acc.field.editAssetPurchaseRequisition") : WtfGlobal.getLocaleText("acc.field.assetPurchaseRequisition"),
                closable: true,
                iconCls: 'accountingbase purchaseorder',
                modeName: 'autorequisition'
            });
            panel.on("activate", function() {
                panel.doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
       
        Wtf.getCmp('as').setActiveTab(panel);
        panel.on('update',  function() {
            if (isEdit == true) {
                Wtf.getCmp('as').remove(panel);
            }
        }, this);
        Wtf.getCmp('as').doLayout();
    } else {
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.lp.createapreq"));  
    }
}

function callFixedAssetPurchaseReqList(consolidateFlag,isEntry,isFixedAsset,reportbtnshwFlag) {
    consolidateFlag = consolidateFlag!=undefined? consolidateFlag : false;
    isEntry = isEntry!=undefined? isEntry : false;
    var panelID = "FixedAssetPurchaseRequisitionList";
    panelID = reportbtnshwFlag? panelID : panelID+'Entry';
    panelID = consolidateFlag? panelID+'Merged' : panelID;
    var panel = Wtf.getCmp(panelID);
    if (panel == null) {
        panel = getFixedAssetPRTab(false, panelID, (isEntry)?WtfGlobal.getLocaleText("acc.field.assetPurchaseRequisition"):WtfGlobal.getLocaleText("acc.field.assetPurchaseReqRepTabTitle"), undefined, consolidateFlag, false,isFixedAsset,reportbtnshwFlag);
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function getFixedAssetPRTab(isWithOutInventory, tabId, tabTitle, extraFilters, consolidateFlag, pendingapproval,isFixedAsset,reportbtnshwFlag) {
    var reportPanel = new Wtf.account.TransactionListPanel({
        id: tabId,
        moduleId: Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId,
        border: false,
        isOrder: true,
        isFixedAsset: isFixedAsset,
        consolidateFlag: consolidateFlag,
        isRequisition: true,
        isCustomer: false,
        isCustBill: isWithOutInventory,
        readOnly:false,
        pendingapproval: pendingapproval,
        helpmodeid:87,
        reportbtnshwFlag: reportbtnshwFlag,
        title: Wtf.util.Format.ellipsis(tabTitle, Wtf.TAB_TITLE_LENGTH),
        tabTip: tabTitle+" :<br>"+WtfGlobal.getLocaleText("acc.field.ViewcompletelistofPurchaseRequisitionsassociatedwithyourvendors"),
        extraFilters: extraFilters,
        label: WtfGlobal.getLocaleText("acc.field.assetPurchaseRequisition"),
        layout: 'fit',
        closable: true,
        iconCls: 'accountingbase purchaseorderlist'
    });
    return reportPanel;
}

function callViewFixedAssetPurchaseReq(isEdit, rec, winid, pendingapproval, isFixedAsset) {
    winid = winid!==undefined ? 'viewFixedAssetRequisition'+winid : 'fixedAssetRequisition';
    var panel = Wtf.getCmp(winid);
    if (panel==null) {
        panel = new Wtf.account.FixedAssetPurchaseRequisitionPanel({
            quotation: true,
            id: winid,
            isRequisition: true,
            pendingapproval: pendingapproval,
            isCustomer: false, // Issue 32006 - [Inventory Assembly]Inventory Assembly type of product should not be there in combo on purchase side.
            moduleid: Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId,
            isOrder: (isEdit!==undefined && isEdit)? false : true,
            record: rec,
            isEdit: isEdit!==undefined ? isEdit : false,
            label: WtfGlobal.getLocaleText("acc.accPref.autoPRequisition"),
            border : false,
            title: WtfGlobal.getLocaleText("acc.field.ViewAssetPurchaseRequisition"),
            tabTip: WtfGlobal.getLocaleText("acc.field.ViewAssetPurchaseRequisition"),
            closable: true,
            viewGoodReceipt: true,
            readOnly: true,
            isViewTemplate: true,
            isFixedAsset: isFixedAsset,
            iconCls: 'accountingbase purchaseorder',
            modeName: 'autorequisition'
        });
        panel.on("activate", function() {
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function() {
        if (isEdit == true) {
            Wtf.getCmp('as').remove(panel);
        }
    }, this);
    Wtf.getCmp('as').doLayout();
}

function callFixedAssetRequestForQuotation(isEdit, PR_IDS, PR_MEMOS, isFixedAsset,tabid, record,thisObj, copyInv) {
    var winid = 'assetrequestforquotation';
    if(tabid != undefined && tabid != null){
        winid += tabid;
    }
    var panel = Wtf.getCmp(winid);
    if (panel == null) {
        panel = new Wtf.account.FixedAssetPurchaseRequisitionPanel({
            quotation: true,
            id: winid,
            isRFQ: true,
            record:record,
            copyInv:copyInv,
            isCustomer: false,
            PR_IDS: PR_IDS,
            PR_MEMOS : PR_MEMOS,
            moduleid: Wtf.Acc_FixedAssets_RFQ_ModuleId,
            isOrder: true,
            isEdit: isEdit,
            POthisObj:thisObj,
            isFixedAsset: isFixedAsset,
            heplmodeid:88,
            label: WtfGlobal.getLocaleText("acc.accPref.autoRFQ"),
            border: false,
            readOnly:false,  //    ERP-15654 [Purchase Requisition/RFQ] "+" button is not working in Product grid. readonly was undedined
            title: isEdit?WtfGlobal.getLocaleText("acc.accPref.editAssetRFQ"):(copyInv?WtfGlobal.getLocaleText("acc.accPref.copyAssetRFQ"):WtfGlobal.getLocaleText("acc.accPref.InitiateAssetRFQ")),  //label,,
            tabTip: isEdit?WtfGlobal.getLocaleText("acc.accPref.editAssetRFQ"):(copyInv?WtfGlobal.getLocaleText("acc.accPref.copyAssetRFQ"):WtfGlobal.getLocaleText("acc.accPref.InitiateAssetRFQ")),  //label,,
            closable: true,
            iconCls: 'accountingbase purchaseorder',
            modeName: 'autorequestforquotation'
        });
        panel.on("activate", function() {
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update', function() {
        if (isEdit == true) {
            Wtf.getCmp('as').remove(panel);
        }
    }, this);
    Wtf.getCmp('as').doLayout();
}

function callFixedAssetReqForQuotationList(consolidateFlag,reportbtnshwFlag) {
    consolidateFlag = consolidateFlag!=undefined? consolidateFlag : false;
    reportbtnshwFlag = reportbtnshwFlag!=undefined? reportbtnshwFlag : false;
    var panelID = reportbtnshwFlag?"assetRequestForQuotationReport":"assetRequestForQuotation";
    panelID = consolidateFlag?panelID + 'Merged' : panelID;
    var panel = Wtf.getCmp(panelID);
    if (panel == null) {
        panel = getFixedAssetReqQuoteTab(false, panelID, reportbtnshwFlag? WtfGlobal.getLocaleText("acc.requestQuotationReportList.tabTitle") :WtfGlobal.getLocaleText("acc.requestQuotationList.tabTitle"), undefined, consolidateFlag, false, true, reportbtnshwFlag);
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function getFixedAssetReqQuoteTab(isWithOutInventory, tabId, tabTitle, extraFilters, consolidateFlag, pendingapproval, isFixedAsset,reportbtnshwFlag) {
    var reportPanel = new Wtf.account.TransactionListPanel({
        id: tabId,
        moduleId: Wtf.Acc_FixedAssets_RFQ_ModuleId,
        border: false,
        isOrder: false,
        consolidateFlag: consolidateFlag,
        isRFQ: true,
        isCustomer: false,
        isCustBill: isWithOutInventory,
        pendingapproval: pendingapproval,
        helpmodeid:88,
        isFixedAsset: isFixedAsset,
        reportbtnshwFlag: reportbtnshwFlag,
        title: Wtf.util.Format.ellipsis(tabTitle, Wtf.TAB_TITLE_LENGTH),
        tabTip: tabTitle,
        extraFilters: extraFilters,
        label: WtfGlobal.getLocaleText("acc.rfqList.tabTitle"),
        layout: 'fit',
        closable: true,
        iconCls: 'accountingbase purchaseorderlist'
    });
    return reportPanel;
}

function callViewFixedAssetRequestForQuotation(isEdit, PR_IDS, PR_MEMOS, rec, isFixedAsset) {
    var winid = 'viewassetrequestforquotation' + rec.data.billid;
    var panel = Wtf.getCmp(winid);
    if (panel == null) {
        panel = new Wtf.account.FixedAssetPurchaseRequisitionPanel({
            quotation: true,
            id: winid,
            isRFQ: true,
            isCustomer: false,
            PR_IDS: PR_IDS,
            PR_MEMOS: PR_MEMOS,
            moduleid: Wtf.Acc_FixedAssets_RFQ_ModuleId,
            isOrder: false,
            record: rec,
            viewGoodReceipt: true,
            readOnly: true,
            isEdit: isEdit,
            isFixedAsset: isFixedAsset,
            label: WtfGlobal.getLocaleText("acc.accPref.autoRFQ"),
            border: false,
            title: WtfGlobal.getLocaleText("acc.accPref.viewAssetRFQ"),
            closable: true,
            isViewTemplate: true,
            iconCls: 'accountingbase purchaseorder',
            modeName: 'autorequestforquotation'
        });
        panel.on("activate", function() {
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function() {
        if (isEdit == true) {
            Wtf.getCmp('as').remove(panel);
        }
    }, this);
    Wtf.getCmp('as').doLayout();
}

function callFixedAssetVendorQuotation(isEdit, tabid, rec, copyInv, PR_IDS, ispurchaseReq,pendingapproval) {
    var winid = 'assetvendorquotation' + tabid;
    var label = WtfGlobal.getLocaleText("acc.field.assetVendorQuotation");
    if (copyInv == undefined || copyInv == "" || copyInv == null) {
        copyInv = false;
    }
    if((!WtfGlobal.EnableDisable(Wtf.UPerm.assetpurchase, Wtf.Perm.assetpurchase.createavq)&&!isEdit&&!copyInv)||isEdit||copyInv){
        var panel = Wtf.getCmp(winid);
        if (panel == null) {
            panel = new Wtf.account.FixedAssetTransactionPanel({
                quotation: true,
                id: winid,
                isCustomer: false,
                isOrder: true,
                isEdit: isEdit,
                ispurchaseReq: ispurchaseReq,
                copyInv: copyInv,
                record: rec,
                PR_IDS: PR_IDS,
                isQuotationFromPR: PR_IDS ? true : false,
                isFixedAsset: true,
                label: label,
                heplmodeid:89,
                border: false,
                tabTip: (isEdit)? WtfGlobal.getLocaleText("acc.field.editAssetVendorQuotation") : ((copyInv)?WtfGlobal.getLocaleText("acc.field.copyAssetVendorQuotaion") : WtfGlobal.getLocaleText("acc.field.assetVendorQuotation")),
                moduleid: Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId,
                title: (isEdit)? WtfGlobal.getLocaleText("acc.field.editAssetVendorQuotation") : ((copyInv)?WtfGlobal.getLocaleText("acc.field.copyAssetVendorQuotaion") : WtfGlobal.getLocaleText("acc.field.assetVendorQuotation")),
                closable: true,
                iconCls: 'accountingbase purchaseorder',
                modeName: 'autovenquotation',
                pendingapproval:pendingapproval
            });
        
            panel.on("activate", function() {
                if (Wtf.isIE7) {
                    var northHt = (Wtf.isIE?150:180);
                    var southHt = (Wtf.isIE?210:150);
                    Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                    Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                    panel.NorthForm.setHeight(northHt);
                    panel.southPanel.setHeight(southHt);
                    panel.on("afterlayout", function(panel, lay) {
                        if (Wtf.isIE7) {
                            panel.Grid.setSize(panel.getInnerWidth() - 18,200);
                        }
                    },this);
                }
                panel.doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
    
        Wtf.getCmp('as').setActiveTab(panel);
    
        panel.on('update',  function() {
            //        if (isEdit == true) {
            //            Wtf.getCmp('as').remove(panel);
            //        }
            if(Wtf.getCmp('assetVendorQuotationList') != undefined){
                Wtf.getCmp('assetVendorQuotationList').loadStore();
            }
            if(Wtf.getCmp('assetVendorQuotationListEntry') != undefined){
                Wtf.getCmp('assetVendorQuotationListEntry').loadStore();
            }
            if(Wtf.getCmp('assetVendorQuotationListMerged')!= undefined){
                Wtf.getCmp('assetVendorQuotationListMerged').loadStore();
            }
        //        var selectedModeId = 'assetVendorQuotationList';
        //        Wtf.getCmp(selectedModeId).loadStore();
        }, this);
    
        Wtf.getCmp('as').doLayout();
    } else {
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.lp.createavq"));
    }
}

function callFixedAssetVendorQuotationList(consolidateFlag,reportbtnshwFlag,isFixedAsset) {
    consolidateFlag = consolidateFlag!=undefined? consolidateFlag : false;
    reportbtnshwFlag = reportbtnshwFlag!=undefined? reportbtnshwFlag : false;
    var panelID = "assetVendorQuotationList";
    panelID = consolidateFlag? panelID + 'Merged' : panelID;
    panelID = reportbtnshwFlag? panelID : panelID + 'Entry';
    reportbtnshwFlag = reportbtnshwFlag;
    var panel = Wtf.getCmp(panelID);
    if (panel == null) {
        panel = getFixedAssetQouteTab(false, panelID, WtfGlobal.getLocaleText("acc.asset.assetvendor")+" "+((!reportbtnshwFlag)?WtfGlobal.getLocaleText("acc.quote.quotation"):WtfGlobal.getLocaleText("acc.qnList.tabTitle")), undefined, consolidateFlag, false, reportbtnshwFlag, undefined, undefined, isFixedAsset);
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function getFixedAssetQouteTab(isWithOutInventory, tabId, tabTitle, extraFilters, consolidateFlag, isCustomer, reportbtnshwFlag, isLeaseFixedAsset, pendingapproval, isFixedAsset) {
    var label = isCustomer? WtfGlobal.getLocaleText("acc.agedPay.cus") : WtfGlobal.getLocaleText("acc.up.10") + " " + WtfGlobal.getLocaleText("acc.invoice.vendor");
    isLeaseFixedAsset = (isLeaseFixedAsset)? isLeaseFixedAsset : false;
    var reportPanel = new Wtf.account.TransactionListPanel({
        id: tabId,
        border: false,
        isQuotation: true,
        isCustomer: isCustomer,
        consolidateFlag: consolidateFlag,
        pendingapproval: pendingapproval,
        isCustBill: isWithOutInventory,
        reportbtnshwFlag: reportbtnshwFlag,
        isFixedAsset: isFixedAsset,
        title: Wtf.util.Format.ellipsis(tabTitle, Wtf.TAB_TITLE_LENGTH),
        tabTip: (isCustomer)? tabTitle+":<br>"+WtfGlobal.getLocaleText("acc.qnList.CtabTT"):tabTitle+":<br>"+WtfGlobal.getLocaleText("acc.qnList.VtabTT"),
        extraFilters: extraFilters,
        label: isLeaseFixedAsset? WtfGlobal.getLocaleText("acc.field.leaseQuotation"):label +" "+ WtfGlobal.getLocaleText("acc.accPref.autoQN"),  // 'Quotation',
        isLeaseFixedAsset: isLeaseFixedAsset,
        layout: 'fit',
        closable: true,
        helpmodeid:89,
        iconCls: 'accountingbase salesorderlist',
        moduleId: (isCustomer)? Wtf.Acc_Customer_Quotation_ModuleId : Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId
    });
    return reportPanel;
}

function callViewFixedAssetVendorQuotation(isEdit, tabid, rec, copyInv, PR_IDS,ispurchaseReq) {
    var winid = 'viewassetvendorquotation' + tabid;
    var label = WtfGlobal.getLocaleText("acc.field.assetVendorQuotation");
    if (copyInv == undefined || copyInv == "" || copyInv == null) {
        copyInv = false;
    }
    var panel = Wtf.getCmp(winid);
    if (panel==null) {
        panel = new Wtf.account.FixedAssetTransactionPanel({
            quotation: true,
            id: winid,
            isCustomer: false,
            isOrder: true,
            isEdit: true,
            ispurchaseReq: ispurchaseReq,
            copyInv: false,
            isCopyFromTemplate: true,
            record: rec,
            PR_IDS: PR_IDS,
            isQuotationFromPR: PR_IDS ? true : false,
            isFixedAsset: true,
            label: label,
            border: false,
            tabTip: WtfGlobal.getLocaleText("acc.field.ViewAssetVendorQuotaion"),
            moduleid: Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId,	            
            title: WtfGlobal.getLocaleText("acc.field.ViewAssetVendorQuotaion"),
            closable: true,
            isViewTemplate: true,
            readOnly: true,
            viewGoodReceipt: true,
            iconCls: 'accountingbase purchaseorder',
            modeName: 'autovenquotation'
        });
    }
    panel.on("activate", function() {
        if (Wtf.isIE7) {
            var northHt = (Wtf.isIE? 150 : 180);
            var southHt=(Wtf.isIE? 210 : 150);
            Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
            Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
            panel.NorthForm.setHeight(northHt);
            panel.southPanel.setHeight(southHt);
            panel.on("afterlayout", function(panel, lay) {
                if (Wtf.isIE7) {
                    panel.Grid.setSize(panel.getInnerWidth() - 18,200);
                }
            },this);
        }
        panel.doLayout();
    }, this);
    Wtf.getCmp('as').add(panel);
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update', function() {
        if (isEdit == true) {
            Wtf.getCmp('as').remove(panel);
        }
        var selectedModeId = 'VendorQuotationList';
        Wtf.getCmp(selectedModeId).loadStore();
    }, this);
    Wtf.getCmp('as').doLayout();
}

function callFixedAssetPurchaseOrderList(consolidateFlag,reportbtnshwFlag,outstandingreportflag,person,titlelabel,isFixedAsset) {
    consolidateFlag = consolidateFlag != undefined? consolidateFlag : false;
    outstandingreportflag = outstandingreportflag;
    var panelID = "assetPurchaseOrderList";
    panelID = consolidateFlag? panelID + 'Merged' : panelID;
    panelID = reportbtnshwFlag? panelID : panelID + 'Entry';
    reportbtnshwFlag = reportbtnshwFlag;
    var panel = Wtf.getCmp(panelID);
    if (panel == null) {
        panel = getFixedAssetPOTab(false, panelID, (titlelabel!=undefined)? titlelabel : WtfGlobal.getLocaleText("acc.field.assetPurchaseOrderReport"), undefined, consolidateFlag,undefined,outstandingreportflag,person,reportbtnshwFlag,isFixedAsset);
        Wtf.getCmp('as').add(panel);
        panel.on('journalentry',callJournalEntryDetails);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function getFixedAssetPOTab(isWithOutInventory, tabId, tabTitle, extraFilters, consolidateFlag, pendingapproval,outstandingreportflag,person,reportbtnshwFlag,isFixedAsset) {
    var reportPanel = Wtf.getCmp(tabId);
    if(reportPanel==null){
        reportPanel = new Wtf.account.TransactionListPanel({
            id: tabId,
            border: false,
            isOrder: true,
            consolidateFlag: consolidateFlag,
            isCustomer: false,
            reportbtnshwFlag: reportbtnshwFlag,
            moduleId: Wtf.Acc_FixedAssets_Purchase_Order_ModuleId,
            isCustBill: isWithOutInventory,
            pendingapproval: pendingapproval,
            outstandingreportflag: outstandingreportflag,
            person: person,
            helpmodeid:90,
            isFixedAsset: isFixedAsset,
            title: Wtf.util.Format.ellipsis(tabTitle, Wtf.TAB_TITLE_LENGTH),
            tabTip: (!pendingapproval)? (tabTitle + " :<br>" + WtfGlobal.getLocaleText("acc.field.assetPOTabToolTip")) : tabTitle,
            extraFilters: extraFilters,
            label: WtfGlobal.getLocaleText("acc.field.assetPurchaseOrder"),
            layout: 'fit',
            closable: true,
            iconCls: 'accountingbase purchaseorderlist'
        });
    }
    return reportPanel;
}

function callFixedAssetPurchaseOrder(isEdit,rec,winid,isOpeningBalanceOrder) {
    winid = (winid == null)? 'assetpurchaseorder' : winid;
    var label = WtfGlobal.getLocaleText("acc.field.assetPurchaseOrder");
    var panel = Wtf.getCmp(winid);
    if (panel == null) {
        panel = new Wtf.account.FixedAssetTransactionPanel({
            id: winid,
            isEdit: isEdit,
            isCustomer: false,
            isOrder: true,
            isOpeningBalanceOrder: isOpeningBalanceOrder,
            isFixedAsset: true,
            label: label,
            border: false,
            heplmodeid:90,
            moduleid: Wtf.Acc_FixedAssets_Purchase_Order_ModuleId,
            title: isOpeningBalanceOrder? WtfGlobal.getLocaleText("acc.field.PurchaseOrder-Opening") : WtfGlobal.getLocaleText("acc.field.assetPurchaseOrder"),
            tabTip: WtfGlobal.getLocaleText("acc.field.assetPurchaseOrder"),
            closable: true,
            iconCls: 'accountingbase purchaseorder',
            modeName: 'autopo'
        });
        
        panel.on("activate", function() {
            if (Wtf.isIE7) {
                var northHt = (Wtf.isIE? 150 : 180);
                var southHt = (Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
                panel.on("afterlayout", function(panel, lay){
                    if (Wtf.isIE7) {
                        panel.Grid.setSize(panel.getInnerWidth() - 18,200);
                    }
                },this);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function(){
        if (isEdit == true) {
            Wtf.getCmp('as').remove(panel);
        }
    }, this);
    Wtf.getCmp('as').doLayout();
}

function callEditFixedAssetPurchaseOrder(isEdit,rec,winid,isPOfromSO,thisObj,newtranType, copyInv,templateId,isViewTemplate,pendingapproval) {
    var billid = (rec != null || rec != undefined)? rec.data.billid : "";
    var isCopyFromTemplate = (templateId == null || templateId == undefined)? false : true;
    var isViewTemplate = (isViewTemplate == null || isViewTemplate == undefined)? false : isViewTemplate;
    var label = WtfGlobal.getLocaleText("acc.field.assetPurchaseOrder");  
    if (copyInv == undefined || copyInv == "" || copyInv == null) {
        copyInv = false;
    }
    winid = "PurchaseOrder" + winid;
    var panel = Wtf.getCmp(winid);
    if (panel == null) {
        panel = new Wtf.account.FixedAssetTransactionPanel({
            id: winid,
            isEdit: isEdit,
            record: rec,
            copyInv: copyInv,
            isCustomer: false,
            templateId: templateId,
            isCopyFromTemplate: isCopyFromTemplate,
            isViewTemplate: isViewTemplate,
            isFixedAsset: true,
            isOrder: true,
            moduleid: Wtf.Acc_FixedAssets_Purchase_Order_ModuleId,
            POthisObj: thisObj,
            POnewtranType: newtranType,
            label: label,
            border: false,
            isPOfromSO: isPOfromSO,
            title: (isViewTemplate? WtfGlobal.getLocaleText("acc.field.ViewPurchaseOrderTemplate") : ((isCopyFromTemplate? WtfGlobal.getLocaleText("acc.wtfTrans.po") : ((isPOfromSO)? WtfGlobal.getLocaleText("acc.wtfTrans.po") : Wtf.util.Format.ellipsis(((copyInv? WtfGlobal.getLocaleText("acc.wtfTrans.cpo") : (isEdit?WtfGlobal.getLocaleText("acc.wtfTrans.easpo") : WtfGlobal.getLocaleText("acc.wtfTrans.po")))+" "+rec.data.billno),Wtf.TAB_TITLE_LENGTH))))),
            tabTip: (isViewTemplate? WtfGlobal.getLocaleText("acc.field.ViewPurchaseOrderTemplate") : ((isCopyFromTemplate? WtfGlobal.getLocaleText("acc.wtfTrans.po") : ((isPOfromSO)? WtfGlobal.getLocaleText("acc.wtfTrans.po") : (copyInv? WtfGlobal.getLocaleText("acc.wtfTrans.cpo") : (isEdit? WtfGlobal.getLocaleText("acc.wtfTrans.easpo") : WtfGlobal.getLocaleText("acc.wtfTrans.po")))+" "+rec.data.billno)))),
            closable: true,
            iconCls: 'accountingbase purchaseorder',
            modeName: 'autopo',
            pendingapproval:pendingapproval
        });
        panel.on("activate", function() {
            if (Wtf.isIE7) {
                var northHt = (Wtf.isIE?150:180);
                var southHt = (Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
                panel.on("afterlayout", function(panel, lay) {
                    if (Wtf.isIE7) {
                        panel.Grid.setSize(panel.getInnerWidth() - 18,200);
                    }
                },this);
            }
            panel.doLayout();
            panel.billid = billid;
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function() {
        if (isEdit == true && !isCopyFromTemplate) {
            var selectedModeId = (Wtf.account.companyAccountPref.withoutinventory)? "bPurchaseOrderList" : "PurchaseOrderList";
            Wtf.getCmp(selectedModeId).loadStore();
            Wtf.getCmp('as').remove(panel);
        }
    }, this);
    Wtf.getCmp('as').doLayout();
}

function callViewFixedAssetPurchaseOrder(isEdit,rec,winid,isPOfromSO,thisObj,newtranType, copyInv,templateId,isViewTemplate) {
    var isCopyFromTemplate = (templateId == null || templateId == undefined)? false : true;
    var label = WtfGlobal.getLocaleText("acc.field.assetPurchaseOrder");  
    if (copyInv !== undefined && copyInv !== "" && copyInv !== null && copyInv) {
        winid = 'View' + winid;
    } else {
        copyInv = false;
    }
    var panel = Wtf.getCmp(winid);
    if (panel == null) {
        panel = new Wtf.account.FixedAssetTransactionPanel({
            id: winid,
            isEdit: true,
            record: rec,
            copyInv: false,
            isCustomer: false,
            templateId: templateId,
            isCopyFromTemplate: true,
            isViewTemplate: true,
            isOrder: true,
            readOnly: true,
            isInvoice: false,
            viewGoodReceipt: true,
            isFixedAsset: true,
            moduleid: Wtf.Acc_FixedAssets_Purchase_Order_ModuleId,
            POthisObj: thisObj,
            POnewtranType: newtranType,
            label: label,
            border: false,
            isPOfromSO: isPOfromSO,
            title: WtfGlobal.getLocaleText("acc.field.viewAssetPurchaseOrder"),
            tabTip: WtfGlobal.getLocaleText("acc.field.viewAssetPurchaseOrder"),
            closable: true,
            iconCls: 'accountingbase purchaseorder',
            modeName: 'autopo'
        });
        panel.on("activate", function() {
            if (Wtf.isIE7) {
                var northHt = (Wtf.isIE? 150:180);
                var southHt = (Wtf.isIE?210:150);
                Wtf.getCmp(winid + 'southEastPanel').setHeight(southHt);
                Wtf.getCmp(winid + 'southEastPanel').setWidth(650);
                panel.NorthForm.setHeight(northHt);
                panel.southPanel.setHeight(southHt);
                panel.on("afterlayout", function(panel, lay) {
                    if (Wtf.isIE7) {
                        panel.Grid.setSize(panel.getInnerWidth() - 18,200);
                    }
                },this);
            }
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function() {
        if (isEdit == true && !isCopyFromTemplate) {
            var selectedModeId = (Wtf.account.companyAccountPref.withoutinventory)? "bPurchaseOrderList" : "assetPurchaseOrderList";
            Wtf.getCmp(selectedModeId).loadStore();
            Wtf.getCmp('as').remove(panel);
        }
    }, this);
    Wtf.getCmp('as').doLayout();
}

function getConsignmentLoanTabView() {
    var reportPanel = Wtf.getCmp('consignmentLoanReport');
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentsales, Wtf.Perm.consignmentsales.viewconsignmentloan)) {
        if (reportPanel == null) {
            reportPanel = new Wtf.account.TransactionListPanelViewConsignmentLoan({
                id: 'consignmentLoanReport',
                border: false,
                title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.consignmentLoanReport"), Wtf.TAB_TITLE_LENGTH), // "Consignment Loan Report"
                tabTip: WtfGlobal.getLocaleText("acc.field.consignmentLoanReport"),
                layout: 'fit',
                closable: true,
                iconCls: 'accountingbase salesorder'
            });
            Wtf.getCmp('as').add(reportPanel);
        }
        Wtf.getCmp('as').setActiveTab(reportPanel);
        Wtf.getCmp('as').doLayout();
    } else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.lp.viewconsignmentloan"));
    }
}

function getConsignmentLoanOutstandingTabView() {
    var reportPanel = Wtf.getCmp('consignmentLoanOutstandingReport');
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.consignmentsales, Wtf.Perm.consignmentsales.viewconsignmentoutloan)) {
        if (reportPanel == null) {
            reportPanel = new Wtf.account.TransactionListPanelViewConsignmentLoan({
                id: 'consignmentLoanOutstandingReport',
                border: false,
                title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.consignmentLoanOutstandingReport"), Wtf.TAB_TITLE_LENGTH), // "Consignment Loan - Outstanding Report"
                tabTip: WtfGlobal.getLocaleText("acc.field.consignmentLoanOutstandingReport"),
                layout: 'fit',
                closable: true,
                isConsignmentLoanOutstadingReport: true,
                iconCls: 'accountingbase salesorder'
            });
            Wtf.getCmp('as').add(reportPanel);
        }
        Wtf.getCmp('as').setActiveTab(reportPanel);
        Wtf.getCmp('as').doLayout();
    }
    else
    {
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.lp.viewconsignmentoutloan")); 
    }
}


function callFixedAssetPurchaseReturnList(reportbtnshwFlag) {
    var id = 'assetPurchaseReturnList';
    id = reportbtnshwFlag? id : id + 'Entry';
    var panel = Wtf.getCmp(id);
    if (panel == null) {
        panel = new Wtf.account.SalesReturnListPanel({
            id: id,
            border: false,
            isOrder: true,
            isCustomer: false,
            isFixedAsset: true,
            //            helpmodeid: 54,
            reportbtnshwFlag: reportbtnshwFlag,
            moduleid: Wtf.Acc_FixedAssets_Purchase_Return_ModuleId,
            title: WtfGlobal.getLocaleText("acc.field.assetPurchaseReturnReport"),
            tabTip: WtfGlobal.getLocaleText("acc.field.assetPurchaseReturnReport"),
            label: WtfGlobal.getLocaleText("acc.field.assetPurchaseReturn"),
            layout: 'fit',
            closable: true,
            iconCls: 'accountingbase salesorderlist'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callFixedAssetPurchaseReturn(isEdit,rec,winid,isFixedAsset) {
    winid = (winid == null)? 'assetPurchaseReturn' : winid;
    isFixedAsset = (isFixedAsset)? isFixedAsset : false;
    var panel = Wtf.getCmp(winid);
    if (panel == null) {
        panel = new Wtf.account.FixedAssetSalesReturnPanel({
            id: winid,
            isEdit: isEdit,
            record: rec,
            isCustomer: false,
            isCustBill: false,
            isFixedAsset: isFixedAsset,
            label: WtfGlobal.getLocaleText("acc.field.assetPurchaseReturn"),
            border : false,
            //            heplmodeid: 11,
            moduleid: Wtf.Acc_FixedAssets_Purchase_Return_ModuleId,
            title: isEdit? WtfGlobal.getLocaleText("acc.field.editAssetPurchaseReturn") : WtfGlobal.getLocaleText("acc.field.assetPurchaseReturn"),
            tabTip:isEdit? WtfGlobal.getLocaleText("acc.field.editAssetPurchaseReturn") : WtfGlobal.getLocaleText("acc.field.assetPurchaseReturn"),
            closable: true,
            iconCls: 'accountingbase deliveryorder',
            modeName: 'autopr'
        });
        
        panel.on("activate", function(){
            panel.doLayout();
        }, this);
        
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);

    Wtf.getCmp('as').doLayout();
}

function callEditFixedAssetPurchaseReturn(isEdit,rec,winid,copyInv,isNoteAlso,isFixedAsset) {
    if (copyInv == undefined || copyInv == "" || copyInv == null ) {
        copyInv = false;
    }
    winid = "assetPurchaseReturn" + winid;
    var panel = Wtf.getCmp(winid);
    if (panel == null) {
        panel = new Wtf.account.FixedAssetSalesReturnPanel({
            id: winid,
            isEdit: isEdit,
            copyInv: copyInv,
            record: rec,
            isCustomer: false,
            isCustBill: false,      
            isNoteAlso: isNoteAlso,
            isFixedAsset: isFixedAsset,
            readOnly: false,
            label: WtfGlobal.getLocaleText("acc.field.assetPurchaseReturn"),
            border: false,
            //            heplmodeid: 11,
            moduleid: Wtf.Acc_FixedAssets_Purchase_Return_ModuleId,
            title: copyInv? WtfGlobal.getLocaleText("acc.field.copyAssetPurchaseReturn") : WtfGlobal.getLocaleText("acc.field.editAssetPurchaseReturn"),
            tabTip: copyInv? WtfGlobal.getLocaleText("acc.field.copyAssetPurchaseReturn") : WtfGlobal.getLocaleText("acc.field.editAssetPurchaseReturn"),
            closable: true,
            iconCls: 'accountingbase salesorder',
            modeName: "autopr"
        });
        panel.on("activate", function() {	       
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    
    Wtf.getCmp('as').setActiveTab(panel);
    
    panel.on('update',  function() {
        if (isEdit == true) {
            Wtf.getCmp("assetPurchaseReturnList").Store.load({
                params: {
                    start: 0,
                    limit: 30
                }
            });
            Wtf.getCmp('as').remove(panel);
        }
    }, this);
    Wtf.getCmp('as').doLayout();
}

function callViewFixedAssetPurchaseReturn(isEdit,rec,winid,copyInv,isFixedAsset) {
    winid = 'view' + winid;    
    var panel = Wtf.getCmp(winid);
    if (panel == null) {
        panel = new Wtf.account.FixedAssetSalesReturnPanel({
            id: winid,
            isEdit: isEdit,
            copyInv: copyInv,
            record: rec,
            readOnly: true,
            isViewTemplate: true,
            isCustomer: false,
            isCustBill: false,
            isFixedAsset: isFixedAsset,
            label: WtfGlobal.getLocaleText("acc.field.assetPurchaseReturn"),
            border: false,
            //            heplmodeid: 11,
            moduleid: Wtf.Acc_FixedAssets_Purchase_Return_ModuleId,
            title: WtfGlobal.getLocaleText("acc.field.viewAssetPurchaseReturn")+"-"+rec.data.billno,
            tabTip: WtfGlobal.getLocaleText("acc.field.viewAssetPurchaseReturn")+"-"+rec.data.billno,
            closable: true,
            iconCls: 'accountingbase salesorder',
            modeName: "autopr"
        });
        
        panel.on("activate", function(){	       
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function() {
        if (isEdit == true) {
            Wtf.getCmp("PurchaseReturnList").Store.load({
                params: {
                    start: 0,
                    limit: 30
                }
            });
            Wtf.getCmp('as').remove(panel);
        }
    }, this);
    Wtf.getCmp('as').doLayout();        
}

function callFixedAssetSalesReturnList(reportbtnshwFlag,isFixedAsset) {
    isFixedAsset = (isFixedAsset)? isFixedAsset : false;
    var id = 'assetSalesReturnList';
    id = reportbtnshwFlag? id : id + 'Entry';
    var panel = Wtf.getCmp(id);
    if (panel == null) {
        panel = new Wtf.account.SalesReturnListPanel({
            id: id,
            border: false,
            isOrder: true,
            isCustomer: true,
            //            helpmodeid: 53,
            isFixedAsset: isFixedAsset,
            reportbtnshwFlag: reportbtnshwFlag,
            moduleid: Wtf.Acc_FixedAssets_Sales_Return_ModuleId,
            title: WtfGlobal.getLocaleText("acc.field.assetSalesReturnList"),
            tabTip: WtfGlobal.getLocaleText("acc.field.assetSalesReturnList"),
            label: WtfGlobal.getLocaleText("acc.field.assetSalesReturn"),
            layout: 'fit',
            closable: true,
            iconCls: 'accountingbase salesorderlist'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callFixedAssetSalesReturn(isEdit,rec,winid,isFixedAsset) {
    winid = (winid==null? 'assetSalesReturn' : winid);
    isFixedAsset = (isFixedAsset)? isFixedAsset : false;
    var panel = Wtf.getCmp(winid);
    if (panel == null) {
        panel = new Wtf.account.FixedAssetSalesReturnPanel({
            id: winid,
            isEdit: isEdit,
            record: rec,
            isCustomer: true,
            isCustBill: false,
            label: WtfGlobal.getLocaleText("acc.field.assetSalesReturn"),
            border: false,
            //            heplmodeid: 11,
            isFixedAsset: isFixedAsset,
            moduleid: Wtf.Acc_FixedAssets_Sales_Return_ModuleId,
            title: WtfGlobal.getLocaleText("acc.field.assetSalesReturn"),
            tabTip: WtfGlobal.getLocaleText("acc.field.assetSalesReturn"),
            closable: true,
            iconCls: 'accountingbase deliveryorder',
            modeName: 'autosr'
        });
        panel.on("activate", function() {
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);

    Wtf.getCmp('as').doLayout();
}

function callEditFixedAssetSalesReturn(isEdit,rec,winid,copyInv,isNoteAlso,isFixedAsset) {
    if (copyInv == undefined || copyInv == "" || copyInv == null) {
        copyInv = false;
    }
    winid = 'assetSalesReturn' + winid;
    var panel = Wtf.getCmp(winid);
    if (panel == null) {
        panel = new Wtf.account.FixedAssetSalesReturnPanel({
            id: winid,
            isEdit: isEdit,
            copyInv: copyInv,
            isNoteAlso: isNoteAlso,
            isFixedAsset: isFixedAsset,
            record: rec,
            isCustomer: true,
            isCustBill: false,
            label: WtfGlobal.getLocaleText("acc.field.assetSalesReturn"),
            border: false,
            //            heplmodeid: 11,
            moduleid: Wtf.Acc_FixedAssets_Sales_Return_ModuleId,
            title: WtfGlobal.getLocaleText("acc.field.editAssetSalesReturn"),
            tabTip: WtfGlobal.getLocaleText("acc.field.editAssetSalesReturn"),
            closable: true,
            iconCls: 'accountingbase salesorder',
            modeName: 'autosr'
        });
        
        panel.on("activate", function() {
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    
    Wtf.getCmp('as').setActiveTab(panel);
    
    panel.on('update',  function() {
        if (isEdit == true) {
            Wtf.getCmp("assetSalesReturnList").Store.load({
                params: {
                    start: 0,
                    limit: 30
                }
            });
            Wtf.getCmp('as').remove(panel);
        }
    }, this);
    Wtf.getCmp('as').doLayout();
}


function manageEligibilityRules() {
    //    if (Wtf.account.companyAccountPref.requestApprovalFlow) {
    var p = Wtf.getCmp("manageeligibilityrules");
    if(!p){
        p= new Wtf.ManageEligibilityRules({
            id:'manageeligibilityrules',
            layout:'fit',
            border: false,
            tabTip : WtfGlobal.getLocaleText("acc.loan.ManageEligibilityRules"),
            iconCls: getButtonIconCls(Wtf.etype.customer),
            title:WtfGlobal.getLocaleText("acc.loan.ManageEligibilityRules"),
            closable:true
        });
        Wtf.getCmp('as').add(p);
    }
    Wtf.getCmp('as').setActiveTab(p);
    Wtf.getCmp('as').doLayout();
//    }else{
//        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Consignment Request Approval flow is disabled currently."],2);
//    }
}

function callLoanDisbursement(isEdit, rec, winid){
    winid = (isEdit)?"editnormaldisbursementtab":"normaldisbursementtab";
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.Disbursement({
            title:isEdit? WtfGlobal.getLocaleText("acc.loan.editdisbursement") : WtfGlobal.getLocaleText("acc.loan.disbursement"), // "Disbursement" : "Edit Disbursement",
            tabTip:isEdit? WtfGlobal.getLocaleText("acc.loan.editdisbursement") : WtfGlobal.getLocaleText("acc.loan.disbursement"), // "Disbursement" : "Edit Disbursement",
            id:winid,
            isEdit:isEdit,
            moduleid:Wtf.LONE_MANAGEMENT_MODULEID,  
            isClone:false,
            record: rec,
            iconCls :getButtonIconCls(Wtf.etype.product),
            layout:'fit',
            closable:true,
            border:false,
            modeName:'autoloanrefnumber'
        });

        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();

}
function callViewLoanDisbursement(isEdit, rec, winid){
    var billid = rec.data.billid;
    var billno = rec.data.loanrefno;
    winid = "Viewnormaldisbursementtab";
    winid+=billid;
    var panel = Wtf.getCmp(winid);
    if(panel==null){
        panel = new Wtf.account.Disbursement({
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.wtfTrans.viewLoanDisbursement")+' '+billno,Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.wtfTrans.viewLoanDisbursement")+' '+billno,
            id:winid,
            isEdit:isEdit,
            readOnly:true,
            moduleid:Wtf.LONE_MANAGEMENT_MODULEID,  
            isClone:false,
            record: rec,
            iconCls :getButtonIconCls(Wtf.etype.product),
            layout:'fit',
            closable:true,
            border:false,
            modeName:'autoloanrefnumber'
        });

        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();

}
function CallDisbursementReport(){
    var reportPanel = Wtf.getCmp('disbursementreport');
    if(reportPanel == null){
        reportPanel = new Wtf.account.DisbursementReport({
            id : 'disbursementreport',
            border : false,
            title: WtfGlobal.getLocaleText("acc.loan.disbursementreport.title"),
            tabTip: WtfGlobal.getLocaleText("acc.loan.disbursementreport.title"),
            layout: 'fit',
            closable : true,
            iconCls:'accountingbase viewreceivepayment'   //iconCls: getButtonIconCls(Wtf.etype.inventoryval)
        });
        Wtf.getCmp('as').add(reportPanel);
    }
    Wtf.getCmp('as').setActiveTab(reportPanel);
    Wtf.getCmp('as').doLayout();
}

function callViewFixedAssetSalesReturn(isEdit,rec,winid,isFixedAsset) {
    winid = 'view' + winid;
    var panel = Wtf.getCmp(winid);
    if (panel == null) {
        panel = new Wtf.account.FixedAssetSalesReturnPanel({
            id: winid,
            isEdit: isEdit,
            record: rec,
            isCustomer: true,
            isCustBill: false,
            isFixedAsset: isFixedAsset,
            label: WtfGlobal.getLocaleText("acc.field.assetSalesReturn"),
            border: false,
            //            heplmodeid: 11,
            readOnly: true,
            isViewTemplate: true,
            moduleid: Wtf.Acc_FixedAssets_Sales_Return_ModuleId,
            title: WtfGlobal.getLocaleText("acc.field.viewAssetSalesReturn")+"-"+rec.data.billno,
            tabTip: WtfGlobal.getLocaleText("acc.field.viewAssetSalesReturn")+"-"+rec.data.billno,
            closable: true,
            iconCls: 'accountingbase salesorder',
            modeName: 'autosr'
        });
        
        panel.on("activate", function() {
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').add(panel);
    }
    
    Wtf.getCmp('as').setActiveTab(panel);
    panel.on('update',  function() {
        if (isEdit == true) {
            Wtf.getCmp("assetSalesReturnList").Store.load({
                params: {
                    start: 0,
                    limit: 30
                }
            });
            Wtf.getCmp('as').remove(panel);
        }
    }, this);
    Wtf.getCmp('as').doLayout();
}

function callMonthlyCommissionOfSalesPersonReport() {
    var panel = Wtf.getCmp("monthlyCommissionOfSalesPerson");
    if (panel == null) {
        panel = new Wtf.account.monthlyCommissionOfSalesPerson({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.MonthlyCommissionOfSalesPersonReport"), Wtf.TAB_TITLE_LENGTH), // "Monthly Commission of Sales Person Report",
            tabTip: WtfGlobal.getLocaleText("acc.monthlyCommissionOfSalesPersonReport.toolTip"),
            id: "monthlyCommissionOfSalesPerson",
            iconCls: 'accountingbase financialreport',
            layout: 'fit',
            closable: true,
            border: false
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

//function callSalesByServiceProductDetailReport(searchStr, filterAppend) {
//    var panel = Wtf.getCmp("salesByServiceProductDetailReport");
//    if (panel == null) {
//        panel = new Wtf.account.salesByServiceProductDetailReport({
//            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.salesByServiceProductDetailReport"), Wtf.TAB_TITLE_LENGTH), // "Sales by Service Product Detail Report",
//            tabTip: WtfGlobal.getLocaleText("acc.salesByServiceProductDetailReport.toolTip"),
//            id: "salesByServiceProductDetailReport",
//            iconCls: 'accountingbase invoicelist',
//            layout: 'fit',
//            closable: true,
//            border: false
//        });
//        Wtf.getCmp('as').add(panel);
//    }
//    Wtf.getCmp('as').setActiveTab(panel);
//    showAdvanceSearch(panel, searchStr, filterAppend);
//    Wtf.getCmp('as').doLayout();
//}

function callSalesPersonCommissionDimensionReport(){
    var panel = Wtf.getCmp("salesPersonCommissionDimensionReport");
    if (panel == null) {
        panel = new Wtf.account.salesPersonCommissionDimensionReport({
            id: 'salesPersonCommissionDimensionReport',
            title: WtfGlobal.getLocaleText("acc.field.salesPersonCommissionDimensionReport"),
            tabTip: WtfGlobal.getLocaleText("acc.field.salesPersonCommissionDimensionReport"),
            topTitle: '<center><font size=4>' + WtfGlobal.getLocaleText("acc.field.salesPersonCommissionDimensionReport") + '</font></center>',
            statementType: 'BalanceSheet',
            border: false,
            closable: true,
            layout: 'fit',
            iconCls: 'accountingbase financialreport'
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callCreditNoteAccountDetailReport(searchStr, filterAppend) {
    var panel = Wtf.getCmp("creditNoteAccountDetailReport");
    if (panel == null) {
        panel = new Wtf.account.creditNoteAccountDetailReport({
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.creditNoteAccountDetailReport"), Wtf.TAB_TITLE_LENGTH), // "Sales by Service Product Detail Report",
            tabTip: WtfGlobal.getLocaleText("acc.creditNoteAccountDetailReport.toolTip"),
            id: "creditNoteAccountDetailReport",
            iconCls: 'accountingbase invoicelist',
            layout: 'fit',
            closable: true,
            moduleid:Wtf.Acc_Credit_Note_ModuleId,
            isCNReport:true,
            border: false
        });
        Wtf.getCmp('as').add(panel);
    }
    panel.on('journalentry',callJournalEntryDetails);
    Wtf.getCmp('as').setActiveTab(panel);
    showAdvanceSearch(panel, searchStr, filterAppend);
    Wtf.getCmp('as').doLayout();
}

function getPaymentTermSalesCommissionDetailReport() {
    var panel = Wtf.getCmp("paymentTermSalesCommissionDetailReport");
    if (panel == null) {
        panel = new Wtf.account.salesCommissionDetailReport({
            id: "paymentTermSalesCommissionDetailReport",
            border: false,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.salesComissionSales.ReportPmtTermDetail"), Wtf.TAB_TITLE_LENGTH), // "Sales Commission Detail Report on Credit Term",
            tabTip: WtfGlobal.getLocaleText("acc.salesComissionSales.ReportPmtTermDetail"),
            layout: 'fit',
            closable: true,
            iconCls: 'accountingbase invoicelist',
            isPaymentTermSalesCommissionDetailReport: true
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}

function callViewmarkout(isView,formrec,winid,type,isJobWorkInReciever){
//    if(!WtfGlobal.EnableDisable(Wtf.UPerm.stockadjustment, Wtf.Perm.stockadjustment.viewstockadj)) {
        winid = 'view'+winid;
        var demo=Wtf.getCmp(winid)
        var main=Wtf.getCmp("as");
        if(demo==null){
            demo = new Wtf.markout({
            id:winid,
            layout:'fit',
            tabtype:type,
            title:formrec.data.seqNumber,
            closable:true,
            type:"markout",
            isView:isView,
            isJobWorkInReciever:isJobWorkInReciever,
            rec:formrec,
            iconCls:getButtonIconCls(Wtf.etype.inventorysa),
            drafttype: 2,
            border:false
        })
         main.add(demo);
        }
        main.setActiveTab(demo);
        main.doLayout();
//    }else{
//        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
//    }
}


function getPaymentTermSalesCommissionDetailReportTest() {
    var panel = Wtf.getCmp("paymentTermSalesCommissionDetailReport");
    if (panel == null) {
        panel = new Wtf.account.salesCommissionDetailReport({
            id: "paymentTermSalesCommissionDetailReport",
            border: false,
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.salesComissionSales.ReportPmtTermDetail"), Wtf.TAB_TITLE_LENGTH), // "Sales Commission Detail Report on Credit Term",
            tabTip: WtfGlobal.getLocaleText("acc.salesComissionSales.ReportPmtTermDetail"),
            layout: 'fit',
            closable: true,
            iconCls: 'accountingbase invoicelist',
            isPaymentTermSalesCommissionDetailReport: true
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
