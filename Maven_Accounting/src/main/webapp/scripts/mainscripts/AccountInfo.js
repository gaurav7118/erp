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
Wtf.account.AccountInfoWindow = function(config){
    this.isReceipt=config.isReceipt,
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.field.SearchbyAccountName") ,  //'Search by Account Name',
        width: 240,
        id:"quickSearch"+config.id
    });
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
    this.butnArr = new Array();
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"),   //'Submit',
        scope: this,
        handler: function() {
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
    Wtf.account.AccountInfoWindow.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.AccountInfoWindow, Wtf.Window, {
height: 460,
width: 800,
modal: true,
iconCls : 'pwnd deskeralogoposition',
title: WtfGlobal.getLocaleText("acc.GeneralLedgerAccounts.GeneralLedgerAccounts"),
onRender: function(config){
        Wtf.account.AccountInfoWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();  
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml('', WtfGlobal.getLocaleText("acc.GeneralLedgerAccounts.GeneralLedgerAccounts"), "../../images/accounting_image/price-list.gif", true)
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
            }); 
        this.cm= new Wtf.grid.ColumnModel([this.sm,
            {
                header:WtfGlobal.getLocaleText("acc.coa.gridAccountName"), // "Account Name""
                width:200,
                sortable:true,
                dataIndex:'documentno'
            },
            {
                header:WtfGlobal.getLocaleText("acc.coa.gridType"), // "Account Name""
                width:200,
                sortable:true,
                dataIndex:'groupname'
            },
            {
                header:WtfGlobal.getLocaleText("acc.coa.accCode"), // "Account Name""
                width:200,
                sortable:true,
                dataIndex:'acccode'
            }]);
       
       
        this.Rec = new Wtf.data.Record.create([
        {
            name: 'documentno',mapping:'accname'
        },{
            name:'acccode'
        },{
            name:'documentid' ,mapping: 'accid' 
        },{
            name:'groupname'
        },{
            name:'prtaxid'
        },{
            name:'masterTypeValue' 
        },{
            name:'isOneToManyTypeOfTaxAccount'
        },{
            name: 'appliedGst'
        },{
            name: 'hasAccess'
        },{
            name: 'haveToPostJe'
        },{
            name: 'usedIn'
        }]);
    
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'totalCount'
            },this.Rec),
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                grouper	:'paymentTrans',
                ignorecustomers	:true,
                ignorevendors	:true,
                mode            :2,
                nondeleted	:true,
                isForPaymentReceipt: true,
                requestModuleid: this.isReceipt ? Wtf.Acc_Receive_Payment_ModuleId : Wtf.Acc_Make_Payment_ModuleId
            }
    });
        this.store.on("loadexception", function() {
            WtfGlobal.resetAjaxTimeOut();
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.mp.unableToLoadData")],1);
        }, this);
        this.store.on("beforeload", function (store) {
            WtfGlobal.setAjaxTimeOut();
        }, this);
        this.store.on('load',this.storeLoaded,this);
        var GlobalColumnModelArr = GlobalColumnModelForReports[Wtf.Account_Statement_ModuleId];
        WtfGlobal.updateStoreConfig(GlobalColumnModelArr, this.store);
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
                emptyText:WtfGlobal.getLocaleText("acc.common.norec"),
                getRowClass: function(record) {
                    this.hasAccess = record.get('hasAccess');
                    if (!this.hasAccess) {
                        return 'hasAccessFalse'
                    } 
                }
            }
        });
    
    this.sm.on("selectionchange",function(){
        var selections=this.grid.getSelectionModel().getSelections();
        if(selections.length>0){
            for(var i=0;i<selections.length;i++){
                var accRec = selections[i];
                var haveToPostJe = accRec ? accRec.data.haveToPostJe : false;
                var key=null;
                key =this.isReceipt?"acc.canNotCreateRP":"acc.canNotCreateMP";
                if(haveToPostJe){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), 
                        WtfGlobal.getLocaleText({
                            key:key,
                            params:[accRec ? accRec.data.usedIn : ""]
                        })], 0);
                }                
            }
        }
    },this);
    },
    submitSelectedRecords : function(){
         
         var selections=this.grid.getSelectionModel().getSelections();
         if(selections.length==0){
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.alert.noaccselected")],2);// 'No Account selected. Select account first'
             return;
         }
         if(selections.length>0){
             var notAccessAccList="";
             var hasAccessFlag=false;
             for(var i=0;i<selections.length;i++){
                var accRec = selections[i];
                if(!accRec.get('hasAccess')){
                    hasAccessFlag=true;
                    notAccessAccList=notAccessAccList+accRec.get('documentno')+", ";
                }
                var haveToPostJe = accRec ? accRec.data.haveToPostJe : false;
                var key=null;
                key =this.isReceipt?"acc.canNotCreateRP":"acc.canNotCreateMP";
                if(haveToPostJe){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), 
                        WtfGlobal.getLocaleText({
                            key:key, 
                            params:[accRec ? accRec.data.usedIn : ""]
                        })], 0);
                    this.accountStore.remove(accRec);
                }
                
            }
            if(notAccessAccList!=""){
                notAccessAccList = notAccessAccList.substring(0, notAccessAccList.length-2);
            }
            if(hasAccessFlag){
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"), //'Warning',
                    msg: WtfGlobal.getLocaleText("acc.field.Inselectedaccountssomeaccountsaredeactivated")+
                    "<br>"+WtfGlobal.getLocaleText("acc.field.DeactivatedAccounts")+notAccessAccList,
                    width:370,
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.WARNING,
                    scope: this
                });
                return;
            }
         }
         this.isSubmitBtnClicked = true;
         this.close();            
     },
    getSelectedRecords: function() {
        var arr = [];
        var selectionArray=this.grid.getSelectionModel().getSelections();
        for(var i=0;i<selectionArray.length;i++){
                arr.push(this.store.indexOf(selectionArray[i]));
        }
        var jarray = WtfGlobal.getJSONArrayWithoutEncodingNew(this.grid, true, arr);   
        return jarray;
    },
    storeLoaded : function() {
        this.quickPanelSearch.StorageChanged(this.store);
        WtfGlobal.resetAjaxTimeOut();
    },
    handleResetClick:function() {
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.store.load();
    }  
    }
});  



