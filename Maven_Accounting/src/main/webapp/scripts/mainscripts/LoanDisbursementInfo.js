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
Wtf.account.LoanDisbursementWindow = function(config) {
    this.isCustomer= config.isCustomer,
    this.currencyfilterfortrans = config.currencyfilterfortrans,
    this.isReceipt = config.isReceipt,
    this.personInfo=config.personInfo,
    this.butnArr = [];
    this.isSubmitBtnClicked = false;
    this.isEdit=config.isEdit;
    this.isCopyReceipt = config.isCopyReceipt;
    this.billid=config.billid;
    this.isMulticurrency=config.isMulticurrency;
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.field.SearchbyInvoiceNoAccountName"),  //'Search by Invoice No, Account Name',
        width: 200,
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
    this.startDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'startdate',
        style:"margin-left: 15px;",
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true)
    });
    this.endDate=new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        style:"margin-left: 15px;",
        name:'enddate',
        value:new Date(this.personInfo.upperLimitDate)  

    });
    this.endDate.on('change',this.onEndDateChange,this);
    this.fetchBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
        tooltip:this.isCN?WtfGlobal.getLocaleText("acc.cn.fetchTT"):WtfGlobal.getLocaleText("acc.dn.fetchTT"),  //"Select a time period to view corresponding credit/debit note records.",
        style:"margin-left: 15px;",
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.loadStore                        
    }); 
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.submit"), //'Submit',
        scope: this,
        handler: function() {
            this.isSubmitBtnClicked = true;
            this.submitSelectedRecords();
        }
    }, {
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"), //'Cancel',
        scope: this,
        handler: function() {
            this.isSubmitBtnClicked = false;
            this.close();
        }
    });
    Wtf.apply(this, {
        buttons: this.butnArr
    }, config);
    Wtf.account.LoanDisbursementWindow.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.LoanDisbursementWindow, Wtf.Window, {
    height: 485,
    width: 800,
    modal: true,
    iconCls : 'pwnd deskeralogoposition',
    onRender: function(config) {
        Wtf.account.LoanDisbursementWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();
        var msg = (this.isCustomer ? "<b>Customer Name</b> : " : "<b>Vendor Name</b> : ") + this.personInfo.personName + (this.isCustomer ? "<br> <b>Customer Code</b> : " : "<br> <b>Vendor Code</b> : ") + this.personInfo.personCode;
        msg=msg+'<div style="font-size:14px; text-align:left; font-weight:bold; margin-top:1%;">Note : </div>'+'<div style="font-size:12px; text-align:left; margin-top:1%;">Only those loan reference numbers are displayed whose due are to be paid in the interval in which the payment date lies.</div>'
        var isgrid = true;
        this.add({
            region: 'north',
            height: 115,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml('', msg, "../../images/accounting_image/price-list.gif", isgrid)
        }, this.centerPanel = new Wtf.Panel({
            border: false,
            region: 'center',
            id: 'centerpan' + this.id,
            autoScroll: true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls: 'bckgroundcolor',
            layout: 'fit',
            items: [this.grid],
            tbar:[this.quickPanelSearch,this.resetBttn,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),this.endDate,this.fetchBttn],
            bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.store,
                searchField: this.quickPanelSearch,
                displayInfo: true,
                displayMsg: 'Displaying records {0} - {1} of {2}',
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                    })
            })
        }))
    },
    createDisplayGrid: function() {
        this.sm = new Wtf.grid.CheckboxSelectionModel({
        });
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "totalCount"
            }),
            url: this.isCustomer ? "ACCLoanCMN/getLoanRepaymentSchedulesForReceipt.do" : "ACCLoanCMN/getLoanRepaymentSchedulesForPayment.do",
            baseParams: {
                accid: this.personInfo.accid,
                currencyfilterfortrans: this.currencyfilterfortrans,
                upperLimitDate:this.personInfo.upperLimitDate,
                billId:this.billid,
                isEdit:this.isEdit && !this.isCopyReceipt,
                isMulticurrency:this.isMulticurrency,
                paymentStatus : 1
            }
        });
        this.store.on("loadexception", function() {
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.mp.unableToLoadData")],1);
        }, this)
        this.store.on('load',this.storeLoaded,this);
        this.store.on('beforeload',function(store,option){
            var currentBaseParams = this.store.baseParams;
            currentBaseParams.startdate=WtfGlobal.convertToGenericDate(this.startDate.getValue());
            currentBaseParams.enddate=WtfGlobal.convertToGenericDate(this.endDate.getValue());
            this.store.baseParams=currentBaseParams;
        },this);
        this.store.on('datachanged', function(){        
            var p = this.pP?this.pP.combo.value:30;        
            this.quickPanelSearch.setPage(p);
        }, this);
        var StoreParams = {ss: this.quickPanelSearch.getValue(),
                                  start:0,
                                  limit:30}
        this.store.load({params: StoreParams});
        this.grid = new Wtf.grid.GridPanel({
            store: this.store,
            height: 235,
            autoScroll: true,
            columns: [],
            sm: this.sm,
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.getLocaleText("acc.common.norec")
            }
        });

    },
    submitSelectedRecords: function() {
        var selections = this.grid.getSelectionModel().getSelections();
        if (selections.length == 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.msgbox.15")], 2);
            return;
        }
        this.close();
    },
    getSelectedRecords: function() {
        var arr = [];
        var selectionArray=this.grid.getSelectionModel().getSelections();
        for( var i=0;i<selectionArray.length;i++){
            arr.push(this.store.indexOf(selectionArray[i]));
        }
        var jarray = WtfGlobal.getJSONArrayWithoutEncoding(this.grid, true, arr);
        return jarray;
    },
    storeLoaded : function() {
        var columns = [];
        columns.push(this.sm);
            
        Wtf.each(this.store.reader.jsonData.columns, function(column) {
            if (column.dataIndex == "date") { 
                column.renderer = WtfGlobal.onlyDateDeletedRenderer;
            } else if (column.dataIndex == "amount" || column.dataIndex == "amountDueOriginal") {
                column.renderer = WtfGlobal.withoutRateCurrencySymbolTransaction;
            }
            columns.push(column);
        });
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();    
        if (this.store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.quickPanelSearch.StorageChanged(this.store);
    },
    handleResetClick:function() {
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.store.load();
        }
    },
    loadStore:function(){
        this.store.reload();
    },
    onEndDateChange: function(obj,val,oldVal){
        if(val.getTime()>new Date(this.personInfo.upperLimitDate).getTime()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rp.dateCanNotBeGreaterThanMonthEndDate")],2);
            this.endDate.setValue(oldVal);
        }
    }
});
