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
Wtf.account.GSTTaxes = function(config){
    this.isReceipt=config.isReceipt,   
    this.accountId=config.accountId,
    this.isEdit=config.isEdit;
    this.isCopy = config.isCopy;
    this.appliedGst=config.appliedGst;
    this.butnArr = new Array();
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        handler: function() {
            this.isSubmitBtnClicked = true;
            this.submitSelectedRecords();           
        }
    },{
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
        scope: this,
        handler: function() {
            this.isSubmitBtnClicked = false;
            this.close();
        }
    });
    Wtf.apply(this,{
        buttons: this.butnArr
    },config);
    Wtf.account.GSTTaxes.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.GSTTaxes, Wtf.Window, {
height: 460,
width: 800,
modal: true,
resizable: false,
iconCls : 'pwnd deskeralogoposition',
title: 'GST Taxes',
onRender: function(config){
        Wtf.account.GSTTaxes.superclass.onRender.call(this, config);
        this.createDisplayGrid();  
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml('', "GST Codes", "../../images/accounting_image/price-list.gif", true)
        },  this.centerPanel=new Wtf.Panel({
            border: false,
            region: 'center',
            id: 'centerpan'+this.id,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls:'bckgroundcolor',
            layout: 'fit',
            height: 320,
            items:[this.grid]            
        }))
    },
    createDisplayGrid:function(){
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            singleSelect:true
            });
        this.cm= new Wtf.grid.ColumnModel([this.sm,
            {
                header:WtfGlobal.getLocaleText("acc.invoiceList.taxName"), // "Tax Name""
                width:100,
                dataIndex:'taxname'
            },
            {
                header:WtfGlobal.getLocaleText("acc.taxReport.taxCode"), // "Tax Code""
                width:100,
                dataIndex:'taxcode'
            }
        ]);
       
       
        this.Rec = new Wtf.data.Record.create([
        {
            name: 'taxtype'
        },
        {
            name:'taxname'
        },
        {
            name:'taxid' 
        },
        {
            name:'taxcode'
        },
        {
            name:'percent'
        },
        {
            name: 'appliedGst' , mapping:'taxid'
        },
        {
            name: 'hasAccess'
        }
        ]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'totalCount'
            },this.Rec),
            url:"ACCAccountCMN/getTaxesFromAccountId.do",
            baseParams:{
                accountId:this.accountId,
                includeDeactivatedTax: this.isEdit != undefined ? (this.isCopy ? false : this.isEdit) : false
            }
    });
        this.store.on("loadexception", function() {
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.mp.unableToLoadData")],1);
        }, this)               
        this.store.on('load',this.setRecordForEditCase,this);
        this.store.load();
        
   this.grid = new Wtf.grid.GridPanel({
            store: this.store,
            height:420,
            width:400,
            scope:this,
            cm: this.cm,
            sm:this.sm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
      
    },
    submitSelectedRecords : function(){
         
         var selections=this.grid.getSelectionModel().getSelections();
         if(selections.length==0){
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.alert.noGstSelected")],2);// 'No Account selected. Select account first'
             return;
         }
        for (var count = 0; count < selections.length; count++) {
            if (selections[count].data && !selections[count].data.hasAccess) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.tax.deactivated.select.alert")], 2);//You cannot select deactivated GST code. Please select activated GST code.
                return;
            }
        }
         this.close(); 
     },
    getSelectedRecords: function() {
        var arr = [];
        var selectionArray=this.grid.getSelectionModel().getSelections();
        for(var i=0;i<selectionArray.length;i++){
                arr.push(this.store.indexOf(selectionArray[i]));
        }
        var jarray = WtfGlobal.getJSONArrayWithoutEncoding(this.grid, true, arr);   
        return jarray;
    },    
    setRecordForEditCase: function() {
        if(this.appliedGst){
            var index = WtfGlobal.searchRecordIndex(this.store,this.appliedGst, "taxid");
            this.grid.getSelectionModel().selectRow(index);
        }
    } 
   
});  





