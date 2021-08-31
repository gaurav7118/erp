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
Wtf.account.Tax1099Window = function(config){
    this.currencyhistory=config.currencyhistory||false;
    Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.1099.winTitle"),  //"1099 Account Setting",
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.common.update"),  //'Update',
            scope: this,
            hidden:this.currencyhistory,
            handler: this.saveData.createDelegate(this)
        },{
            text: WtfGlobal.getLocaleText("acc.common.close"),  //'Close',
            scope: this,
            handler:this.closeWin.createDelegate(this)
        }]
    },config);
    Wtf.account.Tax1099Window.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}
Wtf.extend( Wtf.account.Tax1099Window, Wtf.Window, {
    defaultCurreny:false,
    rowIndex:0,
    onRender: function(config){
        Wtf.account.Tax1099Window.superclass.onRender.call(this, config);
        this.createStore();
        this.createGrid();

        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml(WtfGlobal.getLocaleText("acc.1099.winTitle"),WtfGlobal.getLocaleText("acc.1099.winTitle"),"../../images/accounting_image/tax.gif",true)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.grid
        });
    },

    createStore:function(){
        this.storeRec = Wtf.data.Record.create ([
            {name:'categoryid'},
            {name:'categoryname'},
            {name:'thresholdvalue'},
            {name:'account'},
            {name:'accountid'},
            {name:'accountname'}
        ]);
        this.store = new Wtf.data.Store({
            //url:Wtf.req.account+(this.isCustomer?'CustomerManager.jsp':'VendorManager.jsp'),
            url:'ACCTax/getTax1099Category.do',
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.storeRec)
        });
       this.loadStore();

    },

    loadStore:function(){
        
        this.store.load();
    },

    createGrid:function(){        
        this.accRec = Wtf.data.Record.create ([
            {name:'accountname',mapping:'accname'},
            {name:'accountid',mapping:'accid'}
//            {name:'level', type:'int'}
        ]);
         this.accStore = new Wtf.data.Store({
//            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                nature:[Wtf.account.nature.Expences]
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });


        this.accountEditor= new Wtf.common.Select({
            width:150,
            name:'accountid',
            store:this.accStore,
            hiddenName:'accountid',
            xtype:'select',
            selectOnFocus:true,
            forceSelection:true,
            multiSelect:true,
            displayField:'accountname',
            valueField:'accountid',
            mode: 'local',
            triggerAction:'all',
            typeAhead: true
        })
         this.accStore.on('load',function(){
             this.grid.reconfigure(this.store,this.gridcm)},this)  
        this.accStore.load();
        this.accountEditor.on('select',this.getAccIDArr,this)
        this.gridcm= new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(),{
            dataIndex:'categoryid',
            hidden : true
        },{
            header:WtfGlobal.getLocaleText("acc.1099.category"),  //"1099 Category",
            dataIndex:'categoryname',            
            autoWidth : true
        },{
            header:WtfGlobal.getLocaleText("acc.1099.selAcc"),  //"Select Accounts",
            dataIndex:'accountid',
            id:"taxacc"+this.id,
            renderer:WtfGlobal.getSelectComboRenderer(this.accountEditor),//this.setSelectRenderer.createDelegate(this),//
            editor:this.accountEditor
        },{
            header: WtfGlobal.getLocaleText("acc.1099.setThreshVal"),  //"Set Threshold Value",
            dataIndex: 'thresholdvalue',
            editor:this.exchangeRate=new Wtf.form.NumberField({
                allowBlank: false,
                decimalPrecision:5,
                allowNegative: false,
                minValue:0
            })
        }
        ]);
        this.grid = new Wtf.grid.EditorGridPanel({
            cls:'vline-on',
            layout:'fit',
            autoScroll:true,
            height:200,
            id:(this.currencyhistory?'currencyhistory':'defaultCurrencygrid'),
            store: this.store,
            cm: this.gridcm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true
            }
        });

        this.grid.on('beforeedit',this.getRecord,this)
    },
   setSelectRenderer:function(value){

//            this.store.each(function(rec){
//               // alert(rec.data.accountid)
//                rec.set('accountid',val)
//            },this)

           var idx;
           var rec;
           var valStr="";
           if (value != undefined && value != "") {
               var valArray = value.split(",");
               for (var i=0;i < valArray.length;i++ ){
                   idx = this.accountEditor.store.find(this.accountEditor.valueField, valArray[i]);
                   if(idx != -1){
                       rec = this.accountEditor.store.getAt(idx);
                       valStr+=rec.get(this.accountEditor.displayField)+", ";
                   }
               }
               if(valStr != ""){
                   valStr=valStr.substring(0, valStr.length -2);
                   valStr="<div wtf:qtip=\""+valStr+"\">"+Wtf.util.Format.ellipsis(valStr,27)+"</div>";
               }

           }


   //   var val=WtfGlobal.getSelectComboRenderer.createDelegate([this.accountEditor],this)//(this.accountEditor)
//alert(valStr)
      return valStr
  },
    clearAccRec:function(obj){
    var rec=obj.record;
    if(obj.field=='accountid')
       rec.set('accountid',"")
    },
getRecord:function(o){
//    alert(o.row+"----"+this.store.indexOf(o.record));
   this.rowIndex=o.row;
},
    getAccIDArr:function(){
        var combo=this.accountEditor;
        var value1=combo.getValue();
        var idx;
        if (value1 != undefined && value1 != "") {
            var valArray = value1.split(",");
            for (var i=0;i < valArray.length;i++ ){
                for(var j=0;j<this.store.getCount();j++){
                    if(j==this.rowIndex)continue;
         //           alert(this.store.getAt(j).data.toSource())
                    var value2=this.store.getAt(j).data.accountid
                        if (value2!=undefined && value2!= "") {
     //                       alert(this.store.getAt(j).data.accountid)
                           var valArray2 = value2.split(",");
                            for (var k=0;k < valArray2.length;k++ ){
                                    if(valArray[i]==valArray2[k]){
                                        combo.setValue("");
                                        combo.collapse();
                                           WtfComMsgBox([WtfGlobal.getLocaleText("acc.1099.warn"),WtfGlobal.getLocaleText("acc.1099.msg1")],2);
                            }
                        }
                    }
                }
               if(idx >0){
                   WtfComMsgBox([WtfGlobal.getLocaleText("acc.1099.warn"),WtfGlobal.getLocaleText("acc.1099.msg2")],2);
                   combo.setValue("");
                }
            }
        }
    },

    closeWin:function(){
        this.fireEvent('cancel',this)
        this.close();
    },

    saveData:function(){
        var rec=[];
 //       rec.mode=202;
        rec.data=WtfGlobal.getJSONArray(this.grid,true);
        if(rec.data=="[]"){
            this.close();
            return;
        }
        Wtf.Ajax.requestEx({
//            url:Wtf.req.account+'CompanyManager.jsp',
            url:"ACCTax/saveTax1099Category.do",
            params: rec
        },this,this.genSuccessResponse,this.genFailureResponse);
    },

    genSuccessResponse:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
        if(response.success) this.fireEvent('update');
     //   this.loadStore();
     //   Wtf.currencyStore.load();
      //  this.close();
    },

    genUpdateSuccessResponse:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
        if(response.success) this.fireEvent('update');
     //   this.loadStore();
     //   Wtf.currencyStore.load();
     //   this.close();
    },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }
    
});
