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

Wtf.account.GSTCodeInfoWindow = function(config){
    this.isReceipt=config.isReceipt,
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.field.SearchbyAccountName") ,  //'Search by Account Name',
        width: 240,
        id:"quickSearch"+config.id,
        hidden:true
    });
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false,
        hidden : true
    });
    this.resetBttn.on('click',this.handleResetClick,this);
    this.butnArr = new Array();
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        handler: function() {
            this.isSubmitBtnClicked = true;
            this.close();            
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
    Wtf.account.AccountInfoWindow.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.GSTCodeInfoWindow, Wtf.Window, {
height: 425,
width: 600,
modal: true,
iconCls : 'pwnd deskeralogoposition',
title: 'General Ledger Accounts',
onRender: function(config){
        Wtf.account.GSTCodeInfoWindow.superclass.onRender.call(this, config);
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
            autoScroll:true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:[this.grid],
            tbar:[this.quickPanelSearch,this.resetBttn],
            bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.store,
                searchField: this.quickPanelSearch,
                displayInfo: true,
//                displayMsg: 'Displaying records {0} - {1} of {2}',
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                    })
            })
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
                dataIndex:'documentno'
            },
            {
                header:WtfGlobal.getLocaleText("acc.taxReport.taxCode"), // "Tax Code""
                width:100,
                dataIndex:'taxcode'
            },
            {
                header:WtfGlobal.getLocaleText("acc.invoiceList.expand.tax"), // "Tax Code""
                width:100,
                align:'center',
                dataIndex:'percent'
            }
            ]);
       
       
       this.taxRec = new Wtf.data.Record.create([
           {name: 'documentid',mapping:'taxid'},
           {name: 'prtaxid',mapping:'taxid'},
           {name: 'documentno',mapping:'taxname'},
           {name: 'taxdescription'},
           {name: 'percent',type:'float'},
           {name: 'taxcode'},
           {name: 'accountid'},
           {name: 'accountname'},
           {name: 'applydate', type:'date'},
           {name: 'termid'}

        ]);
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.taxRec),
            url:"ACCTax/getTax.do",
           baseParams:{
                mode:33,
                moduleid: this.isReceipt?Wtf.Acc_Receive_Payment_ModuleId:Wtf.Acc_Make_Payment_ModuleId // moduleid is sent for loading taxes of either purchase side(MP) or sales side(RP)
            }
    });
        this.store.on("loadexception", function() {
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.mp.unableToLoadData")],1);
        }, this)
        this.store.on('load',this.storeLoaded,this);
        this.store.on('datachanged', function(){        
            var p = this.pP?this.pP.combo.value:30;     
            this.quickPanelSearch.setPage(p);
        }, this);
        this.store.load({params: {ss: this.quickPanelSearch.getValue(),start:0,limit:30}}); 
        this.grid = new Wtf.grid.GridPanel({
            store: this.store,
            height:230,
            width:500,
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
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.alert.noaccselected")],2);// 'No Account selected. Select account first'
             return;
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
    storeLoaded : function() {
        this.quickPanelSearch.StorageChanged(this.store);
    },
    handleResetClick:function() {
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.store.load();
        }
    }
});  
