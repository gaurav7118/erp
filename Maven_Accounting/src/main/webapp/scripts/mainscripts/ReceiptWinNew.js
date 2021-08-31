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

Wtf.account.ReceiptWindowNew = function(config){
    this.isReceipt=config.isReceipt;
    this.isCustBill=config.isCustBill;
    this.isAccPref=config.isAccPref||false;
    this.businessPerson=(config.isReceipt?"Customer":"Vendor");
    this.transectionName=config.isReceipt?"Receipt":"Payment";
    this.direcyPayment=config.directPayment,
    this.invoiceRecord=config.invoiceRecord,
    this.sidepanelCheck=config.sidepanelCheck;
    this.butnArr = new Array();
    this.butnArr.push({
        text: this.isAccPref?'Continue':WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        handler: this.saveForm
    },{
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
        scope: this,
        handler: function() {
                 this.fireEvent('cancel',this);
                 var panel = this.parentId;
                 this.close();
                 if(panel!=null){
                         Wtf.getCmp('as').remove(panel);
                         //panel.destroy();
                         panel=null;                 
                    }
            
                }   
    });

    Wtf.apply(this,{
         buttons: this.butnArr
    },config);
    Wtf.account.ReceiptWindowNew.superclass.constructor.call(this, config);
     this.addEvents({
        'update':true
//        'cancel':true
    });
}

Wtf.extend(Wtf.account.ReceiptWindowNew, Wtf.Window, {
    
    draggable:false,
    onRender: function(config){
        Wtf.account.ReceiptWindowNew.superclass.onRender.call(this, config);
        this.createForm();
       var title=this.isReceipt?WtfGlobal.getLocaleText("acc.mp.rtype"):WtfGlobal.getLocaleText("acc.mp.payType");// Pyment type / Receipt type
       var msg=this.isReceipt?WtfGlobal.getLocaleText("acc.rp.selRec"):WtfGlobal.getLocaleText("acc.mp.sel"); // Select Pyment type / Receipt type
       var isgrid=(this.isAccPref ?true:false);
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
            inputValue:1,
            name:'rectype',            
            fieldLabel:this.isReceipt?WtfGlobal.getLocaleText("acc.rp.receivePaymentFromCustomer"):WtfGlobal.getLocaleText("acc.mp.makePaymentToVendor")
        })
        
        this.makeReceivePayType= new Wtf.form.Checkbox({
            boxLabel: " ",
            inputType: 'radio',
            name: 'rectype',
            inputValue: 2,
            width: 50,
            fieldLabel: this.isReceipt? WtfGlobal.getLocaleText("acc.field.ReceivepaymentfromVendor") : WtfGlobal.getLocaleText("acc.field.Makepaymenttocustomer")
        })
        
        this.makePaymentsGLCode = new Wtf.form.Checkbox({
            boxLabel: " ",
            inputType: 'radio',
            name: 'rectype',
            hidden:Wtf.account.companyAccountPref.withoutinventory,
            hideLabel:Wtf.account.companyAccountPref.withoutinventory,
            inputValue: 3,
            width: 50,
            fieldLabel: this.isReceipt? WtfGlobal.getLocaleText("acc.field.ReceivepaymentagainstGLCode") : WtfGlobal.getLocaleText("acc.field.MakepaymentagainstGLCode") 
        })
       this.TypeForm=new Wtf.form.FormPanel({
            region:'center',
            autoScroll:true,
            border:false,
            labelWidth:245,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
            items:[this.accountType,this.makeReceivePayType,this.makePaymentsGLCode]
       });
       if(this.sidepanelCheck){
           this.saveForm(3);           
       }else{
           this.accountType.setValue(true);
       }
   },

   saveForm:function(value){  
       
        if(this.sidepanelCheck){
            this.value=value;
        }else{
            this.value = this.makePaymentsGLCode.getValue()? 3:( this.makeReceivePayType.getValue() ? 2 : (this.accountType.getValue() ? 1 : 4));
        }
        this.close();
        var winid=(winid==null?this.isReceipt?"receiptwindow":"paymentwindow":winid);
        var modeName = this.isReceipt?"autoreceipt":"autopayment";
        var panel = Wtf.getCmp(winid);
        if(this.isReceipt){
             if(panel==null){
                panel=new Wtf.account.ReceiptEntry({
                id : winid,
                paymentType: this.value,
                border : false,
                isReceipt:this.isReceipt,
                isDirectCustomer:false,
                directPayment : this.directPayment,
                invoiceRecord : this.invoiceRecord,
                moduleId:this.isReceipt?Wtf.Acc_Receive_Payment_ModuleId:Wtf.Acc_Make_Payment_ModuleId,
                cls: 'paymentFormPayMthd',
                layout: 'border',
                helpmodeid: 9, //This is help mode id
                title:this.isReceipt?Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoRP"),Wtf.TAB_TITLE_LENGTH):Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoMP"),Wtf.TAB_TITLE_LENGTH),
                tabTip:this.isReceipt?WtfGlobal.getLocaleText("acc.accPref.autoRP"):WtfGlobal.getLocaleText("acc.accPref.autoMP"),  //'Receive Payments',
                iconCls:'accountingbase receivepayment',
                closable: true,
                isCustomer:this.value==1,
                modeName:modeName
            });
            panel.on("activate", function(){
                panel.doLayout();
                Wtf.getCmp(panel.id+"wrapperPanelNorth").doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
        } else{         
            if(panel==null){
            panel=new Wtf.account.PaymentEntry({
                id : winid,
                paymentType: this.value,
                border : false,
                isReceipt:this.isReceipt,
                isDirectCustomer:false,
                directPayment : this.directPayment,
                invoiceRecord : this.invoiceRecord,
                moduleId:this.isReceipt?Wtf.Acc_Receive_Payment_ModuleId:Wtf.Acc_Make_Payment_ModuleId,
                cls: 'paymentFormPayMthd',
                layout: 'border',
                helpmodeid: 10, //This is help mode id
                title:this.isReceipt?Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoRP"),Wtf.TAB_TITLE_LENGTH):Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.accPref.autoMP"),Wtf.TAB_TITLE_LENGTH),
                tabTip:this.isReceipt?WtfGlobal.getLocaleText("acc.accPref.autoRP"):WtfGlobal.getLocaleText("acc.accPref.autoMP"),  //'Receive Payments',
                iconCls:'accountingbase receivepayment',
                closable: true,
                isCustomer:this.isReceipt?(this.value==1):(this.value==2),
                modeName:modeName
            });
            panel.on("activate", function(){
                panel.doLayout();
                Wtf.getCmp(panel.id+"wrapperPanelNorth").doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();
        }
        panel.on('update',function(config) {  //Added an event for Import Bank reconciliation window.
            if(Wtf.getCmp('ImportBankReconciliationReport')){
            Wtf.getCmp('as').setActiveTab(Wtf.getCmp('ImportBankReconciliationReport'));
            Wtf.getCmp('ImportBankReconciliationReport').addPaymentEntryOnClickButton();
            }
        },this);
            
    },
     closeWin:function(){this.fireEvent('update',this,this.value);this.close();}
}); 
