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
Wtf.account.SelectTransactionTypeWindow = function(config){
     Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation"),  //"Bank Reconciliation",
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.common.continueBtn"),  //'Continue',
            scope: this,
            handler: this.saveData.createDelegate(this)
        },{
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            scope: this,
            handler:this.closeWin.createDelegate(this)
        }]
    },config);
    Wtf.account.SelectTransactionTypeWindow.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}
Wtf.extend( Wtf.account.SelectTransactionTypeWindow, Wtf.Window, {

    onRender: function(config){
        var image="../../images/accounting_image/bank-reconciliation.jpg";
        Wtf.account.SelectTransactionTypeWindow.superclass.onRender.call(this, config);
        this.createForm();
        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml('Create New Transaction','Select type of Transaction.',image,false)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.Form
        });
   },
  
    createForm:function(){
        this.paymentType= new Wtf.form.Checkbox({
            boxLabel:" ",
            width: 50,
            inputType:'radio',
            inputValue:1,
            name:'rectype',            
            fieldLabel:WtfGlobal.getLocaleText("acc.dimension.module.5") //Make Payment
        })
        
        this.receiptType= new Wtf.form.Checkbox({
            boxLabel: " ",
            inputType: 'radio',
            name: 'rectype',
            inputValue: 2,
            width: 50,
            fieldLabel: WtfGlobal.getLocaleText("acc.dimension.module.6")  //Receive Payment
        })
        
        this.Form=new Wtf.form.FormPanel({
            region:'center',
            autoScroll:true,
            border:false,
            labelWidth:245,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
            items:[this.paymentType,this.receiptType]
        });
        
        this.paymentType.setValue(true);
    },
    
    closeWin:function(){
         this.fireEvent('cancel',this)
         this.close();
     },

    saveData:function(){
        if(!this.Form.getForm().isValid())
                WtfComMsgBox(2,2);
        else{
            this.value = this.receiptType.getValue()? 2 : 1;
            if(this.value == 2){
                callReceiptNew();
            } else {
                callPaymentNew();
            }
            this.close();
        }
    }

});
