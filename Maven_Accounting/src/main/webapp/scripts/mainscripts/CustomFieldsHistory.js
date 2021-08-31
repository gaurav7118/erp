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

Wtf.account.CustomFieldHistoryWindow = function(config){
    this.fieldType="";
    this.selectedValueRecord = "";
    Wtf.apply(this,{
         buttons: [{
                    text: WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
                    scope: this,
                    handler: this.saveForm
                  },{
                    text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
                    scope: this,
                    handler: function(){this.close();}
         }]
    },config);
    Wtf.account.CustomFieldHistoryWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.CustomFieldHistoryWindow, Wtf.Window, {
    onRender: function(config){
        Wtf.account.CustomFieldHistoryWindow.superclass.onRender.call(this, config);
        this.createCombo();
        this.createFields();
        this.createForm();
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText("acc.field.CustomField"),WtfGlobal.getLocaleText("acc.field.SetAnyCustomFieldvalue") +' '+ WtfGlobal.getLocaleText("acc.field.forproduct")+'<b>'+' '+this.record.data.productname+'</b>',"../../images/accounting_image/price-list.gif")
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.setCustomFieldForm
        });   
        
    },
    
    createCombo : function(){
        this.customFieldComboStoreRec = new Wtf.data.Record.create ([
            {
                name:'fieldtype'
            },{
                name:'refcolumn_number'
            },{
                name: 'notificationdays'
            },{
                name: 'validationtype'
            },{
                name: 'comboname'
            },{
                name: 'fieldid'
            },{
                name: 'column_number'
            },{
                name:'moduleflag'
            },{
                name:'comboid'
            },{
                name:'isessential'
            },{
                name:'fieldname'
            },{
                name:'fieldlabel'
            },{
                name:'maxlength'
            },{
                name:'iscustomcolumn'
            },{
                name:'sendnotification'
            },{
                name:'iseditable'
            },{
                name:'moduleid'
            },{
                name:'iscustomfield'
            }
        ]);
        
        this.customFieldComboStore =  new Wtf.data.Store({
            url: "ACCAccountCMN/getFieldParams.do",
            baseParams:{
                moduleid: 30,
                isForProductCustomFieldHistoryCombo:true, 
                isActivated:1
            },  
            reader: new  Wtf.data.KwlJsonReader({
                root: "data",
                autoLoad:false
            },this.customFieldComboStoreRec)
        });
//        this.customFieldComboStore.on('beforeload',function(){
//            this.loadMask = new Wtf.LoadMask();
//            this.loadMask.show();
//        },this);
//        this.customFieldComboStore.on('load',function(){
//            this.loadMask.hide();
//        },this);
//        this.customFieldComboStore.on('loadexception',function(){
//            this.loadMask.hide();
//        },this);
        
        this.customFieldComboStore.load();
        
        this.customFieldCombo = new Wtf.form.ComboBox({
            store : this.customFieldComboStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.field.CustomField*"),
            typeAhead: true,
            selectOnFocus:true,
            displayField:'fieldlabel',
            valueField : 'fieldid',
            triggerAction: 'all',
            emptyText : WtfGlobal.getLocaleText("acc.field.SelectaField"),
            mode:'local',
            allowBlank:false
        });
        this.customFieldCombo.on('select',this.onSelection,this);
    },
    
    createFields:function(){
        this.NumberField = new Wtf.form.NumberField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Value*"),
            allowBlank:false,
            name:'customNumberField',
            emptyText:WtfGlobal.getLocaleText("acc.field.PleaseEnteraNumericValue")
        }),
        this.TextField = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Value*"),
            allowBlank:false,
            name:'customTextField',
            emptyText:WtfGlobal.getLocaleText("acc.field.PleaseEnteraTextValue")
        }),
        this.applydate= new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.taxes.gridApplyDate"),            
            name: 'applydate',
            //          minValue:new Date().format('Y-m-d'),
            format:WtfGlobal.getOnlyDateFormat(),
            value: Wtf.serverDate.clearTime(true),
            //          minValue: new Date().clearTime(true),
            allowBlank:false
        });
        this.applydate.on("render", function(){
            WtfGlobal.hideFormElement(this.NumberField);
            WtfGlobal.hideFormElement(this.TextField);
        }, this);
    },
    
    createForm:function(){
       this.setCustomFieldForm=new Wtf.form.FormPanel({
            region:'center',
            autoScroll:true,
            border:false,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
            defaults:{
                width:200
            },
             items:[this.customFieldCombo,this.NumberField,this.TextField,this.applydate]
       });
  
   },
   onSelection:function(combo,rec,index){
       this.selectedValueRecord = rec;
        this.fieldType = rec.get('fieldtype');
        switch(this.fieldType){
            case 1:
                this.TextField.reset();
                this.NumberField.reset();
                this.NumberField.allowBlank = true;
                WtfGlobal.showFormElement(this.TextField);
                WtfGlobal.hideFormElement(this.NumberField);
                break;
            case 2:
                this.TextField.reset();
                this.NumberField.reset();
                this.TextField.allowBlank = true;
                WtfGlobal.showFormElement(this.NumberField);
                WtfGlobal.hideFormElement(this.TextField);
                break;
        }

   },
   
   saveForm : function(){
       if(!this.setCustomFieldForm.getForm().isValid()){
            WtfComMsgBox(2,2);
        }else{
            var productId = this.record.get("productid");
            var fieldId = this.customFieldCombo.getValue();
            var applyDate = WtfGlobal.convertToGenericDate(this.applydate.getValue());
            var value = this.getFieldDataValue();
            var creationDate = WtfGlobal.convertToGenericDate(new Date());
            if (this.fieldType ==1 && value.replace(/\s+/g, '') == "") {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Pleaseenternew")+" data"], 2);
                return;
            }else if (value=="" || value ==undefined) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Pleaseenternew")+" data"], 2);
                return;
            }
            var customFieldRecord = this.selectedValueRecord;
            var customFieldArray = this.createCustomFieldArray();
            var customfield="";
            if (customFieldArray.length > 0)
                    customfield = JSON.stringify(customFieldArray);
            
            Wtf.Ajax.requestEx({
                url:"ACCProduct/maintainCustomFieldHistoryForProduct.do",
                params: {
                    productId:productId,
                    fieldId:fieldId,
                    applyDate:applyDate,
                    creationDate:creationDate,
                    value:value,
                    moduleId:30,
                    customfield:customfield
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
            
        }
   },
   genSuccessResponse:function(response){
       WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.CustomField"),response.msg],response.success*2+1);
       this.close();
   },
   genFailureResponse:function(response){
       var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
   },
   
   getFieldDataValue:function(){
        var value = "";
        switch(this.fieldType){
            case 1:
                value = this.TextField.getValue();
                break;
            case 2:
                value = this.NumberField.getValue();
                break;
        }
        return value;
    },
   
   createCustomFieldArray : function(){
       var column_number = this.selectedValueRecord.get('column_number');
       var fieldname = this.selectedValueRecord.get('fieldname');
       var returnArray = [];
       var temp={
                    refcolumn_name:this.selectedValueRecord.get('refcolumn_number'),
                    fieldname:fieldname,
                    xtype:this.selectedValueRecord.get('fieldtype')
                };
           temp[column_number] = this.getFieldDataValue();
           temp[fieldname] = column_number;
       returnArray.push(temp);   
       return returnArray;
   }
})