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
Wtf.FormDetailsWindow= function(config){
    Wtf.FormDetailsWindow.superclass.constructor.call(this, config);
    this.records = config.records;
    this.isCustomer = config.isCustomer;
};
Wtf.extend(Wtf.FormDetailsWindow,Wtf.Window, {
    resizable: false,
    scope: this,
    autoScroll:true,
    layout: 'border',
    modal:true,
    width: 450,
    height: 350,
    iconCls: getButtonIconCls(Wtf.etype.deskera),  //'pwnd favwinIcon',
    id: 'acc_formdetails_header',
    title: WtfGlobal.getLocaleText("acc.invoiceList.formDetailsWindow"),//'Form Details Window',
    initComponent: function() {
        Wtf.FormDetailsWindow.superclass.initComponent.call(this);
        this.addButton({
            text : WtfGlobal.getLocaleText("acc.common.saveBtn"), 
            id:'savebtn'+this.id
            }, function(){
            this.saveColumnChanges();
        },this);
        this.addButton({
            text:WtfGlobal.getLocaleText("acc.common.close"), 
            id:'closebtn'+this.id
            }, function(e){
            this.close();
        },this);
    },
   
    saveColumnChanges : function() {
        var valid=this.moduleFormDetails.getForm().isValid();
        if(this.FormAmount.getValue()<=0){
            this.FormAmount.markInvalid();
            return;
        }
        if(valid==false){
            WtfComMsgBox(2, 2);
            return;
        }  
        var idsArray = [];
        for(var i=0;i< this.records.length;i++){
            var rec = this.records[i].data;
            idsArray.push(rec.billid);
        }
        Wtf.Ajax.requestEx({
            url: this.isCustomer? "ACCInvoice/UpdateInvoiceFormDetails.do" :"ACCGoodsReceipt/UpdateGoodsReceiptFormDetails.do",
            params: {
                FormSeriesNo: this.FormSeriesNo.getValue(),
                FormNo: this.FormNo.getValue(),
                FormDate: WtfGlobal.convertToGenericDate(this.FormDate.getValue()),
                FormAmount: this.FormAmount.getValue(),
                idsArray: JSON.stringify(idsArray),
                isFormDetails:true
            }
        }, this, function(response) {
            this.close();
        }, this);
    },
    
    onRender: function(config) {
        Wtf.FormDetailsWindow.superclass.onRender.call(this, config);
        this.add(
        {
            region : 'north',
            height : 100,
            border : false,
            id:'resolveConflictNorth_panel_Overrite',
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html : getTopHtml(WtfGlobal.getLocaleText("acc.invoiceList.formDetailsWindow"), WtfGlobal.getLocaleText("acc.invoiceList.formDetailsWindow"),"../../images/accounting_image/role-assign.gif")
        //                                                  :getTopHtml(WtfGlobal.getLocaleText("acc.field.CustomizeView"), WtfGlobal.getLocaleText("acc.field.CustomizetheViewbyHide/Show")+customizeMsg,"../../images/accounting_image/role-assign.gif")
        }
        );
        this.FormSeriesNo = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.formDetailsWindow.formSeriesNo") +"*",
            name: 'FormSeriesNo',
            allowBlank: false,
            width:200,
            maxLength:15,
            vtype : "alphanum"
        });
        this.FormNo = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.formDetailsWindow.formNo") +"*",
            name: 'FormNo',
            allowBlank: false,
            width:200,
            maxLength:15,
            vtype : "alphanum"
        });
        this.FormDate = new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.formDetailsWindow.formDate") +"*",
            format:WtfGlobal.getOnlyDateFormat(),
            allowBlank: false,
            maxValue:new Date(),
            name: 'FormDate',
            width:200,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        });
        this.FormAmount = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.formDetailsWindow.amount") +"*",
            name: 'FormAmount',
            allowBlank: false,
            allowNegative: false,
            defaultValue:0,
            width:200,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
        });
        this.moduleFormDetails = new Wtf.FormPanel({
            width:'90%',
            method :'POST',
            scope: this,
            border:false,
            waitMsgTarget: true,
            region:"center",
            height : 80,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:15px 0 0 60px;',
            layout: 'form',
            items:[
            this.FormSeriesNo,
            this.FormNo,
            this.FormDate,
            this.FormAmount
            ]
        });
        this.add(this.moduleFormDetails);
        this.FormDate.setValue(new Date());
    }
});