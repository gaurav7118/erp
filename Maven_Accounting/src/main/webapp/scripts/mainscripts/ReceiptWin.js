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
// Sales/Purchase Return Type

Wtf.account.SalesReturnTypeWindow = function(config){
    this.value="1",
    this.isCustBill=config.isCustBill;
    this.isSR=(config.isSR==null || config.isSR==undefined)?false:config.isSR;
    this.butnArr = new Array();
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        handler: this.saveForm
    },{
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
        scope: this,
        handler: function() {
            this.close();
        }
    });

    Wtf.apply(this,{
         constrainHeader :true,
         buttons: this.butnArr
    },config);
    Wtf.account.SalesReturnTypeWindow.superclass.constructor.call(this, config);
     this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.SalesReturnTypeWindow, Wtf.Window,{
    draggable:false,
    onRender: function(config){
        Wtf.account.SalesReturnTypeWindow.superclass.onRender.call(this, config);
        this.createForm();
        var title=this.isSR?WtfGlobal.getLocaleText("acc.pi.SalesReturnType"):WtfGlobal.getLocaleText("acc.pi.PurchaseReturnType");
        var msg=this.isSR?WtfGlobal.getLocaleText("acc.pi.SelectSalesReturnType"):WtfGlobal.getLocaleText("acc.pi.SelectPurchaseReturnType");
        var isgrid=true;
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title,msg,"../../images/accounting_image/price-list.gif",isgrid)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.TypeForm
        });
    },

    createForm:function(){
       this.salesReturnOnlyType= new Wtf.form.Checkbox({
            boxLabel:" ",
            width: 50,
            inputType:'radio',
            inputValue:'1',
            name:'rectype',
            fieldLabel:this.isSR?WtfGlobal.getLocaleText("acc.pi.CreateSalesReturnOnly"):WtfGlobal.getLocaleText("acc.pi.CreatePurchaseReturnOnly")
       });
       this.salesReturnWithNoteType= new Wtf.form.Checkbox({
            boxLabel:" ",
            inputType:'radio',
            name:'rectype',
            inputValue:'2',
            width: 50,
            fieldLabel:this.isSR?WtfGlobal.getLocaleText("acc.pi.CreateSalesReturnwithCreditNote"):WtfGlobal.getLocaleText("acc.pi.CreatePurchaseReturnwithDebitNote")
       });
       this.tbar3 = new Array();
       this.tbar3.push(this.salesReturnOnlyType);
       this.tbar3.push(this.salesReturnWithNoteType);
       
       this.salesReturnWithPaymentType= new Wtf.form.Checkbox({
            boxLabel:" ",
            inputType:'radio',
            name:'rectype',
            inputValue:'3',
            width: 50,
            fieldLabel:WtfGlobal.getLocaleText("acc.payment.sales.return.CashSales")
       });
       
        if(this.isSR){
            this.tbar3.push(this.salesReturnWithPaymentType);
        }
        
       this.TypeForm=new Wtf.form.FormPanel({
            region:'center',
            autoScroll:true,
            border:false,
            labelWidth:245,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
             items:this.tbar3
       });
       this.salesReturnOnlyType.setValue(true);
   },

   saveForm:function(){  
        var rec=this.TypeForm.getForm().getValues();
        this.value = rec.rectype;//this.accountType.getValue();
        this.close();
        if(this.value == 1 ) {// For Normal SR/PR
            if(this.isSR)
                callSalesReturn();
            else    
                callPurchaseReturn();
        }else if(this.value == 3 ) {
            /*
             * Cash Refund with payment
             */
            callSalesReturn(false,null,null,false,true);
        }else{// For SR/PR with CN/DN
            if(this.isSR)
                callSalesReturn(false,null,null,true);
            else
                callPurchaseReturn(false,null,null,true);
        }
    },
    closeWin:function(){this.close();}
    
}); 

// Purchase Invoice Type

Wtf.account.PurchaseInvoiceTypeWindow = function(config){
    this.value="1",
    this.isCustBill=config.isCustBill;
    this.readOnly=config.readOnly;
    this.butnArr = new Array();
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        handler: this.saveForm
    },{
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
        scope: this,
        handler: function() {
            this.close();
        }
    });

    Wtf.apply(this,{
         constrainHeader :true,
         buttons: this.butnArr
    },config);
    Wtf.account.PurchaseInvoiceTypeWindow.superclass.constructor.call(this, config);
     this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.PurchaseInvoiceTypeWindow, Wtf.Window,{
    draggable:false,
    onRender: function(config){
        Wtf.account.PurchaseInvoiceTypeWindow.superclass.onRender.call(this, config);
        this.createForm();
        var title=WtfGlobal.getLocaleText("acc.pi.PurchaseInvoiceType");
        var msg=WtfGlobal.getLocaleText("acc.pi.SelectPurchaseInvoiceType");
        var isgrid=true;
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title,msg,"../../images/accounting_image/price-list.gif",isgrid)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.TypeForm
        });
    },

    createForm:function(){
       this.purchaseInvoiceType= new Wtf.form.Checkbox({
            boxLabel:" ",
            width: 50,
            inputType:'radio',
            inputValue:'1',
            name:'rectype',
            fieldLabel:WtfGlobal.getLocaleText("acc.pi.PurchaseInvoice")
       });
       this.selfBilledInvoice= new Wtf.form.Checkbox({
            boxLabel:" ",
            inputType:'radio',
            name:'rectype',
            inputValue:'2',
            width: 50,
            fieldLabel:WtfGlobal.getLocaleText("acc.pi.SelfBilledInvoice")
       });
       this.TypeForm=new Wtf.form.FormPanel({
            region:'center',
            autoScroll:true,
            border:false,
            labelWidth:245,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
             items:[this.purchaseInvoiceType,this.selfBilledInvoice]
       });
       this.purchaseInvoiceType.setValue(true);
   },

   saveForm:function(){  
        var rec=this.TypeForm.getForm().getValues();
        this.value = rec.rectype;//this.accountType.getValue();
        this.close();
        if(this.value == 1 ) {// For Purchase invoice 
//            if(!Wtf.account.companyAccountPref.withoutinventory){
                callGoodsReceipt(false,null);
//            }else{
//                callBillingGoodsReceipt(false,null)
//            }
        }else{// For Self Billed invoice 
//            if(!Wtf.account.companyAccountPref.withoutinventory){
                callGoodsReceipt(false,null,null,undefined,undefined,true);
//            }else{
//                callBillingGoodsReceipt(false,null,null,true)
//            }
        }
    },
    closeWin:function(){this.close();}
    
}); 

//Credit Note / Debit Note Type Window

Wtf.account.CreditNoteTypeWindow = function(config){
    this.value="1",
    this.isCN=config.isCN;
    this.isCallFromPurchaseSaleEntryMenu = config.isCallFromPurchaseSaleEntryMenu;
    this.isCustBill=config.isCustBill;
    this.businessPerson=(config.isReceipt?"Customer":"Vendor");
    this.transectionName=config.isReceipt?"Receipt":"Payment";
    this.isCustomer = config.isCustomer;
    this.butnArr = new Array();
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        handler: this.saveForm
    },{
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
        scope: this,
        handler: function() {
            this.close();
        }
    });

    Wtf.apply(this,{
         constrainHeader :true,
         buttons: this.butnArr
    },config);
    Wtf.account.CreditNoteTypeWindow.superclass.constructor.call(this, config);
     this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.CreditNoteTypeWindow, Wtf.Window, {
    draggable:false,
    onRender: function(config){
        Wtf.account.CreditNoteTypeWindow.superclass.onRender.call(this, config);
        this.createForm();
        var title=this.isCN?WtfGlobal.getLocaleText("acc.cn.payType"):WtfGlobal.getLocaleText("acc.dn.payType");
        var msg=this.isCN?WtfGlobal.getLocaleText("acc.cn.sel"):WtfGlobal.getLocaleText("acc.dn.sel");
        var isgrid=true;
        this.width = 450;
        if(this.isCallFromPurchaseSaleEntryMenu) {
            this.height = 260;
        }
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title,msg,"../../images/accounting_image/price-list.gif",isgrid)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.TypeForm
        });
    },

    createForm:function(){
        var dataArr = new Array();
        this.accountType= new Wtf.form.Checkbox({// CN/DN against SI/PI
            boxLabel:" ",
            width: 50,
            inputType:'radio',
            inputValue:'1',
            name:'rectype',
            fieldLabel:this.isCN?WtfGlobal.getLocaleText("acc.cn.recCN"):WtfGlobal.getLocaleText("acc.dn.recDN")
        });
        this.accountTypePaid= new Wtf.form.Checkbox({// CN/DN against paid SI/PI
            boxLabel:" ",
            inputType:'radio',
            name:'rectype',
            inputValue:'3',
            width: 50,
            fieldLabel:this.isCN?WtfGlobal.getLocaleText("acc.cn.recCNPaid"):WtfGlobal.getLocaleText("acc.dn.recDNPaid")
        });
        this.customerType= new Wtf.form.Checkbox({// CN/DN otherwise
            boxLabel:" ",
            inputType:'radio',
            name:'rectype',
            inputValue:'2',
            width: 50,
            fieldLabel:this.isCN?WtfGlobal.getLocaleText("acc.cn.recCNOt"):WtfGlobal.getLocaleText("acc.dn.recDNOt")
        });
        this.customerTypeDN= new Wtf.form.Checkbox({// CN/DN againts Customer/Vendor
            boxLabel:" ",
            inputType:'radio',
            name:'rectype',
            inputValue:'4',
            width: 50,
            fieldLabel:this.isCN?WtfGlobal.getLocaleText("acc.cn.recCNDN"):WtfGlobal.getLocaleText("acc.dn.recDNCN")
        });
        this.vendorTypeCNForGst= new Wtf.form.Checkbox({// CN/DN againts Customer/Vendor
            boxLabel:" ",
            inputType:'radio',
            name:'rectype',
            inputValue:Wtf.CNDN_TYPE_FOR_MALAYSIA,
            width: 50,
//            fieldLabel:this.isCN?WtfGlobal.getLocaleText("acc.cn.recCNGst"):WtfGlobal.getLocaleText("acc.dn.recDNGst")
            fieldLabel:this.isCN?WtfGlobal.getLocaleText("acc.cn.undercharged.PI"):WtfGlobal.getLocaleText("acc.dn.undercharged.SI")//ERM-778
        });
        
        this.overchargeTypeCNDN = new Wtf.form.Checkbox({//CN against SI & DN against PI (Over Charge)
            fieldLabel: this.isCN ? WtfGlobal.getLocaleText("acc.cn.overcharged.SI") : WtfGlobal.getLocaleText("acc.dn.overcharged.PI"), //ERM-778
            boxLabel: " ",
            inputType: 'radio',
            name: 'rectype',
            inputValue: Wtf.NoteForOvercharge,
            width: 50
        });
        
        if(this.isCallFromPurchaseSaleEntryMenu) {
            if (Wtf.Countryid == Wtf.Country.US || Wtf.Countryid == Wtf.Country.SINGAPORE) {
                if ((!this.isCN && this.isCustomer) || (this.isCN && !this.isCustomer)) {
                    /**
                     * Customer DN /Vendor CN.
                     */
                    dataArr.push(this.customerTypeDN, this.vendorTypeCNForGst);
                    this.customerTypeDN.setValue(true);
                } else {
                    /**
                     * Vendor DN /Customer CN.
                     */
                    dataArr.push(this.accountType, this.customerType, this.overchargeTypeCNDN);
                }
            } else if (Wtf.Countryid == Wtf.Country.MALAYSIA) {
                if ((!this.isCN && this.isCustomer) || (this.isCN && !this.isCustomer)) {
                    /**
                     * Customer DN /Vendor CN.
                     */
                    dataArr.push(this.vendorTypeCNForGst);
                    this.vendorTypeCNForGst.setValue(true);
                } else {
                    /**
                     * Vendor DN /Customer CN.
                     */
                    dataArr.push(this.accountType, this.customerType, this.overchargeTypeCNDN);
                }
            } else {
                /**
                 * For india,Indonesia & other country no need to show over/under charge option till the implemenation.
                 */
                if ((!this.isCN && this.isCustomer) || (this.isCN && !this.isCustomer)) {
                    /**
                     * Customer DN /Vendor CN.
                     */
                    dataArr.push(this.customerTypeDN);
                    this.customerTypeDN.setValue(true);
                } else {
                    /**
                     * Vendor DN /Customer CN.
                     */
                    dataArr.push(this.accountType, this.customerType);
                }
            }
        } else {
            if(Wtf.account.companyAccountPref.countryid == Wtf.Country.MALAYSIA) { //for Malaysian company Added the CN/DN otherwise ERM-352
                dataArr.push(this.accountType, this.customerType, this.vendorTypeCNForGst,this.overchargeTypeCNDN);// ERP-10589,this.customerTypeDN
            } else if (Wtf.Countryid == Wtf.Country.SINGAPORE) {
                dataArr.push(this.accountType, this.customerType, this.customerTypeDN,this.vendorTypeCNForGst,this.overchargeTypeCNDN);
                /**
                 * For US country add Overcharge/ Undercharge option in CN/DN
                 */
            }else if (Wtf.Countryid == Wtf.Country.US || Wtf.Countryid == Wtf.Country.INDONESIA) {
                dataArr.push(this.accountType, this.customerType, this.customerTypeDN,this.vendorTypeCNForGst,this.overchargeTypeCNDN);
            }else {
                dataArr.push(this.accountType, this.customerType, this.customerTypeDN);
            }
        }
        
       this.TypeForm=new Wtf.form.FormPanel({
            region:'center',
            autoScroll:true,
            border:false,
            labelWidth:290,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
            items:dataArr
       });
       this.accountType.setValue(true);
   },

   saveForm:function(){  
        var rec=this.TypeForm.getForm().getValues();
        this.value = rec.rectype;//this.accountType.getValue();
        this.close();
        if(this.value == 1 || this.value == 3) {// For against invoice and against paid invoices.
            if(this.isCN) {
                this.openCNWindow(this.value);
            } else {
                this.openDNWindow(this.value);
            }
        } else if(this.value == 5 ) {// For Normal SR/PR
            winid=(this.isCN?'creditnoteagaintvendorformalaysia':'debitnoteagaintcustomerformalaysia');
            var panel = Wtf.getCmp(winid);
            if(panel==null){
                panel = new Wtf.account.SalesReturnPanel({
                    id : winid,
//                    record: rec,
                    isCustomer:this.isCN?false:true,
                    readOnly:false,
                    isNoteAlso:true,
                    inputValue:'5',
                    isCustBill:false,
                    label:this.isCN?WtfGlobal.getLocaleText("acc.module.name.12"):WtfGlobal.getLocaleText("acc.module.name.10"),//ERM-778
                    isCN:this.isCN?true:false,
                    border : false,
                    heplmodeid: 11,
                    moduleid:this.isCN?Wtf.Acc_Credit_Note_ModuleId:Wtf.Acc_Debit_Note_ModuleId,
                    title:this.isCN?WtfGlobal.getLocaleText("acc.cn.undercharge"):WtfGlobal.getLocaleText("acc.dn.undercharge"),
                    tabTip:this.isCN?WtfGlobal.getLocaleText("acc.cn.undercharge"):WtfGlobal.getLocaleText("acc.dn.undercharge"),
                    closable: true,
                    iconCls:'accountingbase deliveryorder',
                    modeName:this.isCN?'autocreditmemo':'autodebitnote'
                });
               panel.on("activate", function(){
                    panel.doLayout();
                }, this);
                Wtf.getCmp('as').add(panel);
            }
            Wtf.getCmp('as').setActiveTab(panel);
            Wtf.getCmp('as').doLayout();
        } else if(this.value == Wtf.NoteForOvercharge) {
            winid = (this.isCN ? 'creditnoteForOvercharge' : 'debitnoteForOvercharge');
            var panel = Wtf.getCmp(winid);
            if (panel == null) {
                panel = new Wtf.account.SalesReturnPanel({
                    id: winid,
                    isCustomer: this.isCN ? true : false,
                    readOnly: false,
                    isNoteAlso: true,
                    inputValue: Wtf.NoteForOvercharge,
                    isCustBill: false,
                    label: this.isCN ? WtfGlobal.getLocaleText("acc.module.name.12") : WtfGlobal.getLocaleText("acc.module.name.10"),
                    isCN: this.isCN ? true : false,
                    border: false,
                    heplmodeid: 11,
                    moduleid: this.isCN ? Wtf.Acc_Credit_Note_ModuleId : Wtf.Acc_Debit_Note_ModuleId,
                    title: this.isCN ? WtfGlobal.getLocaleText("acc.cn.overcharge") : WtfGlobal.getLocaleText("acc.dn.overcharge"),
                    tabTip: this.isCN ? WtfGlobal.getLocaleText("acc.cn.overcharge") : WtfGlobal.getLocaleText("acc.dn.overcharge"),
                    closable: true,
                    iconCls: 'accountingbase deliveryorder',
                    modeName: this.isCN ? 'autocreditmemo' : 'autodebitnote'
                });
                panel.on("activate", function () {
                    panel.doLayout();
                }, this);
                Wtf.getCmp('as').add(panel);
            }
            Wtf.getCmp('as').setActiveTab(panel);
            Wtf.getCmp('as').doLayout();
        }else {// CN/DN otherwise
            var winid=this.isCN?"creditnotepanel":"debitnotepanel";
            var panel = Wtf.getCmp(winid);   
            if(panel!=null){
                Wtf.getCmp('as').remove(panel);
                panel.destroy();
                panel=null; 
            }
            panel= new Wtf.account.NoteAgainsInvoice({
                title: this.isCN?WtfGlobal.getLocaleText("acc.cn.generate"):WtfGlobal.getLocaleText("acc.dn.generate"),  //"Receipt Type",
                tabTip: this.isCN?WtfGlobal.getLocaleText("acc.cn.generate"):WtfGlobal.getLocaleText("acc.dn.generate"),  //"Receipt Type",
                id: winid,
                isCustBill:this.isCustBill,
                closable: true,
                isCN:this.isCN,
                moduleid:this.isCN?Wtf.Acc_Credit_Note_ModuleId:Wtf.Acc_Debit_Note_ModuleId,
                cntype:this.value,
                modal: true,
                iconCls :this.isCN?'accountingbase creditnote':'accountingbase debitnote',
                autoScroll:true,
                resizable: false,
                layout: 'border',
                modeName:this.isCN?'autocreditmemo':'autodebitnote'
            });
            Wtf.getCmp('as').add(panel);
            Wtf.getCmp('as').setActiveTab(panel);
            Wtf.getCmp('as').doLayout();
        }
    },
    closeWin:function(){
        this.close();
    },
    openCNWindow:function(cntype){// CN Against CI.
        var winid="creditnotepanel";
        var panel = Wtf.getCmp(winid);  
        if(panel!=null){
            Wtf.getCmp('as').remove(panel);
            panel.destroy();
            panel=null; 
        }
        if(new Date(Wtf.account.companyAccountPref.activeDateRangeToDate).getTime() < new Date().getTime()){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.linkingDate.date.alert.ofActiveDateRange"),function(btn){
                if(btn=="no") {
                    return;
                }else{
                    panel=new Wtf.account.NoteAgainsInvoice({
                        title: WtfGlobal.getLocaleText("acc.cn.generate"),  //"Receipt Type",
                        tabTip: WtfGlobal.getLocaleText("acc.cn.generate"),  //"Receipt Type",
                        id: winid,
                        isCustBill:this.isCustBill,
                        closable: true,
                        isCN:this.isCN,
                        moduleid:Wtf.Acc_Credit_Note_ModuleId,
                        cntype:cntype,
                        iconCls :'accountingbase creditnote',
                        autoScroll:true,
                        layout: 'border',
                        modeName:'autocreditmemo'
                    });
                    Wtf.getCmp('as').add(panel);
                    Wtf.getCmp('as').setActiveTab(panel);
                    Wtf.getCmp('as').doLayout();
                }
            },this);
        }else{
            panel=new Wtf.account.NoteAgainsInvoice({
                title: WtfGlobal.getLocaleText("acc.cn.generate"),
                tabTip: WtfGlobal.getLocaleText("acc.cn.generate"),
                id: winid,
                isCustBill:this.isCustBill,
                closable: true,
                isCN:this.isCN,
                moduleid:Wtf.Acc_Credit_Note_ModuleId,
                cntype:cntype,
                iconCls :'accountingbase creditnote',
                autoScroll:true,
                layout: 'border',
                modeName:'autocreditmemo'
            });
            Wtf.getCmp('as').add(panel);
            Wtf.getCmp('as').setActiveTab(panel);
            Wtf.getCmp('as').doLayout();
        }
    },
    
    openDNWindow:function(cntype){// CN Against CI.
        var winid="debitnotepanel";
        var panel = Wtf.getCmp(winid);  
        if(panel!=null){
            Wtf.getCmp('as').remove(panel);
            panel.destroy();
            panel=null; 
        }
            if(new Date(Wtf.account.companyAccountPref.activeDateRangeToDate).getTime() < new Date().getTime()){
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.linkingDate.date.alert.ofActiveDateRange"),function(btn){
                    if(btn=="no") {
                        return;
                    }else{
                        panel=new Wtf.account.NoteAgainsInvoice({
                            title: WtfGlobal.getLocaleText("acc.dn.generate"),
                            tabTip: WtfGlobal.getLocaleText("acc.dn.generate"),
                            id: winid,
                            isCustBill:this.isCustBill,
                            closable: true,
                            isCN:this.isCN,
                            moduleid:Wtf.Acc_Debit_Note_ModuleId,
                            cntype:cntype,
                            iconCls :'accountingbase debitnote',
                            autoScroll:true,
                            layout: 'border',
                            buttonAlign: 'right',
                            modeName:'autodebitnote'
                        });
                        Wtf.getCmp('as').add(panel);
                        Wtf.getCmp('as').setActiveTab(panel);
                        Wtf.getCmp('as').doLayout();
                    }
                
                },this);
            }else {
                panel=new Wtf.account.NoteAgainsInvoice({
                    title: WtfGlobal.getLocaleText("acc.dn.generate"), 
                    tabTip: WtfGlobal.getLocaleText("acc.dn.generate"), 
                    id: winid,
                    isCustBill:this.isCustBill,
                    closable: true,
                    isCN:this.isCN,
                    moduleid:Wtf.Acc_Debit_Note_ModuleId,
                    cntype:cntype,
                    iconCls :'accountingbase debitnote',
                    autoScroll:true,
                    layout: 'border',
                    buttonAlign: 'right',
                    modeName:'autodebitnote'
                });  
                Wtf.getCmp('as').add(panel);
                Wtf.getCmp('as').setActiveTab(panel);
                Wtf.getCmp('as').doLayout();
            }
    }
}); 



//Journal Entry Type Window

Wtf.account.journalEntryTypeWindow = function(config){
    this.value="1",
    this.butnArr = new Array();
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        handler: this.saveForm
    },{
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
        scope: this,
        handler: function() {
            this.close();
        }
    });

    Wtf.apply(this,{
         buttons: this.butnArr
    },config);
    Wtf.account.journalEntryTypeWindow.superclass.constructor.call(this, config);
     this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.journalEntryTypeWindow, Wtf.Window, {

    onRender: function(config){
       Wtf.account.journalEntryTypeWindow.superclass.onRender.call(this, config);
       this.createForm();
       var title=WtfGlobal.getLocaleText("acc.je.Type");
       var msg=WtfGlobal.getLocaleText("acc.je.sel");
       var isgrid=true;
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title,msg,"../../images/accounting_image/price-list.gif",isgrid)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.TypeForm
     });
    },

    createForm:function(){
        this.accountType= new Wtf.form.Checkbox({
            boxLabel:" ",
            width: 50,
            inputType:'radio',
            inputValue:'1',
            name:'rectype',
            fieldLabel:WtfGlobal.getLocaleText("acc.je.Type1") //Normal JE
        });
        this.accountType1= new Wtf.form.Checkbox({
            boxLabel:" ",
            inputType:'radio',
            name:'rectype',
            inputValue:'2',
            width: 50,
            fieldLabel:WtfGlobal.getLocaleText("acc.je.Type2")  //Partly JE
        });
        this.accountType2= new Wtf.form.Checkbox({
            boxLabel:" ",
            inputType:'radio',
            name:'rectype',
            inputValue:'3',
            width: 50,
            fieldLabel:WtfGlobal.getLocaleText("acc.je.Type3")  // Fund Transfer
        });
       this.TypeForm=new Wtf.form.FormPanel({
            region:'center',
            autoScroll:true,
            border:false,
            labelWidth:245,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
             items:[this.accountType,this.accountType1,this.accountType2]
       });
       this.accountType.setValue(true);
   },

   saveForm:function(){  
        var rec=this.TypeForm.getForm().getValues();
        this.value = rec.rectype;//this.accountType.getValue();
        this.close();
        callJournalEntryTab(undefined,undefined,undefined,this.value);
    }
}); 

Wtf.account.commissionWindow = function(config){
    this.value="1",
    this.butnArr = new Array();
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.continueBtn"),   //'Submit',
        scope: this,
        handler: this.saveForm
    },{
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
        scope: this,
        handler: function() {
            this.close();
        }
    });

    Wtf.apply(this,{
         buttons: this.butnArr
    },config);
    Wtf.account.commissionWindow.superclass.constructor.call(this, config);
     this.addEvents({
        'update':true
    });
}

Wtf.extend(Wtf.account.commissionWindow, Wtf.Window, {

    onRender: function(config){
       Wtf.account.commissionWindow.superclass.onRender.call(this, config);
       this.createForm();
        var title=WtfGlobal.getLocaleText("acc.commission.type"); 
       var msg=WtfGlobal.getLocaleText("acc.commission.sel"); 
       var isgrid=true;
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title,msg,"../../images/accounting_image/price-list.gif",isgrid)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.TypeForm
     });
    },

    createForm:function(){
        this.InvoiceAmount= new Wtf.form.Checkbox({
            boxLabel:" ",
            width: 50,
            inputType:'radio',
            inputValue:'1',
            name:'rectype',
            fieldLabel:WtfGlobal.getLocaleText("acc.commission.Amount") //Amount
        });
        this.ProductCategoryType= new Wtf.form.Checkbox({
            boxLabel:" ",
            inputType:'radio',
            name:'rectype',
            inputValue:'2',
            width: 50,
            fieldLabel:WtfGlobal.getLocaleText("acc.commission.ProductCategory")  //Brand
        });
        this.paymentTerm= new Wtf.form.Checkbox({
            boxLabel:" ",
            inputType:'radio',
            name:'rectype',
            inputValue:'3',
            width: 50,
            fieldLabel:WtfGlobal.getLocaleText("acc.salesperson.paymentterm")  //Payment Term
        });
        this.Product= new Wtf.form.Checkbox({
            boxLabel:" ",
            inputType:'radio',
            name:'rectype',
            inputValue:'4',
            width: 50,
            fieldLabel:'Product'  // Specefic Product
        });
       
       this.TypeForm=new Wtf.form.FormPanel({
            region:'center',
            autoScroll:true,
            border:false,
            labelWidth:245,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
             items:[this.InvoiceAmount,this.ProductCategoryType,this.paymentTerm,this.Product]
       });
       this.InvoiceAmount.setValue(true);
   },

   saveForm:function(){  
        var rec=this.TypeForm.getForm().getValues();
        this.close();
        this.showsalesCommisionSchemaWindow = new Wtf.salesCommisionSchemaWindow({
            rec:this.rec,
            type:rec.rectype,
            scope:this
        })
        this.showsalesCommisionSchemaWindow.show();
    }
});