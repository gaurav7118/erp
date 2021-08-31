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
Wtf.account.InvoiceMonthWiseWindow = function(config) {
    this.isCustomer= config.isCustomer,
    this.currencyfilterfortrans = config.currencyfilterfortrans,
    this.isReceipt = config.isReceipt,
    this.personInfo=config.personInfo,
    this.butnArr = [];
    this.isSubmitBtnClicked = false;
    this.isEdit=config.isEdit;
    this.billid=config.billid;
    this.isMulticurrency=config.isMulticurrency;
    this.parentObject = config.parentObject;
    
    this.yearStoreArray = this.getYearStoreArray(this.paymentType);
        this.yearStore = new Wtf.data.SimpleStore({
        fields: [{
            name: 'yearid'
        }, {
            name: 'yearname'
        }],
        data: this.yearStoreArray
    });
    this.Year = new Wtf.form.FnComboBox({
        hiddenName: 'year',
        name:'year',
        id: "monthwiseinvoice-year"+ this.id,
        anchor: '85%',
        store: this.yearStore,
        valueField: 'yearid',
        forceSelection: true,
        displayField: 'yearname',
        scope: this,
        value:0,
        selectOnFocus: true
    });
    this.Year.on('change',function(){
        this.store.load();
    },this);
    this.fetchBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  
        tooltip:this.isReceipt?WtfGlobal.getLocaleText("acc.rp.fetchTT"):WtfGlobal.getLocaleText("acc.mp.fetchTT"),
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
    Wtf.account.InvoiceMonthWiseWindow.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.InvoiceMonthWiseWindow, Wtf.Window, {
    height: 430,
    width: 550,
    modal: true,
    iconCls : 'pwnd deskeralogoposition',
    onRender: function(config) {
        Wtf.account.InvoiceMonthWiseWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();
        var msg = (this.isReceipt ? "<b>Customer Name</b> : " : "<b>Vendor Name</b> : ") + this.personInfo.personName + (this.isReceipt ? "<br> <b>Customer Code</b> : " : "<br> <b>Vendor Code</b> : ") + this.personInfo.personCode;
        var isgrid = true;
        this.add({
            region: 'north',
            height: 85,
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
            tbar:[WtfGlobal.getLocaleText("acc.accPref.year")+" ",this.Year,this.fetchBttn]
        }))
    },
    createDisplayGrid: function() {
        this.sm = new Wtf.grid.CheckboxSelectionModel({
        });
        this.cm = new Wtf.grid.ColumnModel([this.sm, {
                header: WtfGlobal.getLocaleText("acc.accPref.month"), 
                dataIndex: 'month'
            }, 
            {
                header:WtfGlobal.getLocaleText("acc.customerList.gridAmountDue"), 
                dataIndex: 'balanceInPaymentCurrency',
                renderer:WtfGlobal.withoutRateCurrencySymbolTransaction,
                align :'center'
            },
            {
                header:WtfGlobal.getLocaleText("acc.invoice.amtDueInBase"), 
                dataIndex: 'amountdueinbase',
                renderer:WtfGlobal.currencyDeletedRenderer ,
                align :'center'
            }]);


        this.Rec = Wtf.data.Record.create([
            {name: 'monthid'},
            {name: 'month'},
            {name: 'balanceInPaymentCurrency'},
            {name:'currencysymboltransaction'},
            {name: 'amountdueinbase'}
            
        ]);
        
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.Rec),
            url: this.isReceipt ? "ACCInvoiceCMN/getMonthWiseInvoicesDue.do" : "ACCGoodsReceiptCMN/getMonthWiseGoodsReceiptsDue.do",
            baseParams: this.getBaseParamsForInvoices(false)
        });
        
        if(!this.isMulticurrency){
                this.store.baseParams['isReceipt']=this.isReceipt;  // 'isReceipt' flag is used at back-end to decide whether all currency invoices are to be returnde or only payment currency invoices to be returned
        }
        this.store.on("loadexception", function() {
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.mp.unableToLoadData")],1);
        }, this)
        this.store.on('beforeload',function(store,option){
            var currentBaseParams = this.store.baseParams;
            this.getStartAndEndDate(currentBaseParams);
            this.store.baseParams=currentBaseParams;
            this.store.baseParams.year= this.Year.getValue();
        },this);
        var InvoiceStoreParams = {start:0,
                                  limit:30}
          
        this.store.load({params: InvoiceStoreParams});
        this.grid = new Wtf.grid.GridPanel({
            store: this.store,
            height: 235,
            autoScroll: true,
            cm: this.cm,
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
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.mprp.noMonthIsSelected")], 2);
            return;
        }
        this.close();
    },
    getSelectedMonthsInvoices: function() {
        var arr = [];
        var selectionArray=this.grid.getSelectionModel().getSelections();
        for( var i=0;i<selectionArray.length;i++){
            arr.push(selectionArray[i]['data']['monthid']);
        }
        
        arr = this.getInvoicesForSelectedMonths(arr);
        var jarray = WtfGlobal.getJSONArrayWithoutEncoding(this.grid, true, arr);
        return jarray;
    },
    loadStore:function(){
        this.store.load();
    },
    getYearStoreArray:function(){
        var today = new Date();
        var currentYear = today.getFullYear();
        var returnArray=[];
        returnArray.push([0,currentYear]);
        for(var i=1;i<5;i++){
            returnArray.push([i,currentYear-i]);
        }
        return returnArray;
    },
    getStartAndEndDate:function(baseparams){
        var record = WtfGlobal.searchRecord(this.yearStore, this.Year.getValue(), "yearid");
        var year = record.data['yearname'];
        year = ''+year+'';
        
        var date = new Date();
        var startDate = new Date(date.setYear(year))
        startDate = new Date(startDate.setMonth(0, 1));
        startDate = new Date(startDate.setHours(00, 00, 00));
        
        date = new Date();
        var endDate = new Date(date.setYear(year));
        endDate = new Date(endDate.setMonth(11, 31));
        endDate = new Date(endDate.setHours(00, 00, 00));
        baseparams.startdate=WtfGlobal.convertToGenericDate(startDate);
        baseparams.enddate=WtfGlobal.convertToGenericDate(endDate);
    },
    getInvoicesForSelectedMonths:function(arr){
        Wtf.Ajax.requestEx({
            url: this.isReceipt ? "ACCInvoiceCMN/getMonthWiseInvoices.do" : "ACCGoodsReceiptCMN/getMonthWiseGoodsReceipts.do",
            params:this.getParamsForLoadingMonthwiseInvoices(arr)
        },this,this.genSuccessResponseOnFetchInvoices,this.genFailureResponse);
    },
    getBaseParamsForInvoices : function(forMonthWiseInvoices){
        var obj = {
            onlyAmountDue: true,
            accid: this.personInfo.accid,
            deleted: false,
            nondeleted: true,
            currencyfilterfortrans: this.currencyfilterfortrans,
            direction: this.direction,
            upperLimitDate:this.personInfo.upperLimitDate,
            includeFixedAssetInvoicesFlag:true,
            billId:this.billid,
            isEdit:this.isEdit,
            filterForClaimedDateForPayment : true,
            requestModuleid: this.isCustomer ? Wtf.Acc_Receive_Payment_ModuleId : Wtf.Acc_Make_Payment_ModuleId
        }
        if(forMonthWiseInvoices){
            obj['forMonthWiseInvoices']=true;
        } else {
            obj['forMonthWiseInvoices']=false;
        }
        if(!this.isMulticurrency){
            obj['isReceipt'] = this.isReceipt;
        }
        return obj;
    },
    getParamsForLoadingMonthwiseInvoices: function(arr){
        var obj = this.getBaseParamsForInvoices(true);
        var monthIds = '';
        for(var x=0;x<arr.length;x++){
            monthIds+=arr[x]+',';
        }
        if(monthIds != ''){
            monthIds = monthIds.substring(0, monthIds.length-1);
        }
        obj['months'] = monthIds;
        this.getStartAndEndDate(obj);
        return obj;
    },
    genFailureResponse: function(response,request){
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    genSuccessResponseOnFetchInvoices:function(response,request){
        if(response.success){
            var arr = response.data;
            this.addInvoicesToGrid(arr);            
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], response.success * 2 + 1);
            return;
        }
    },
    addInvoicesToGrid:function(arr){
        if(arr.length > 0){
            var grid = this.parentObject.grid;
            var store  = this.parentObject.grid.store;
            var storeSize = store.getCount();
            store.remove(store.getAt(storeSize-1));
        }
        var record = Wtf.data.Record.create([
        {name: 'type'}, 
        {name: 'debit' , defValue:true},
        {name: 'currencyidtransaction'},
        {name: 'currencynametransaction'},
        {name: 'currencysymboltransaction'},
        {name: 'currencyname'},
        {name: 'currencyid'},
        {name: 'documentno'},
        {name: 'documentid'},
        {name: 'amountDueOriginal', defValue: 0},
        {name: 'amountDueOriginalSaved', defValue: 0},
        {name: 'amountdue', defValue: 0}, 
        {name: 'enteramount', defValue: 0},
        {name: 'exchangeratefortransaction', defValue: 1},
        {name: 'accountid'},
        {name:'accountnames'},
        {name:'isClaimedInvoice'},
        {name:'gstCurrencyRate',defValue:'0.0'},
        {name:'claimedDate'},
        {name:'description'},
        {name:'tdsamount',defValue:0}
    ]);
    
        for(var x=0;x<arr.length;x++){
            var index = -1;
            index = WtfGlobal.searchRecordIndex(store, arr[x]['billid'], "documentid");
            var storeRec = this.getStoreEmptyRecord();
            storeRec = this.setStoreRecordValueFromJSONObject(storeRec, arr[x]);
            var newrec = new record(storeRec);
            if (index == -1) {
                store.add(newrec);
            }
        }
        grid.getView().refresh();
        if(arr.length>0){
            grid.addBlankRow();
            grid.fireEvent('datachanged',grid);
        }     
    },
    /*
     * To create record
     */
    getStoreEmptyRecord : function () {
        var Record = this.parentObject.grid.getStore().reader.recordType,f = Record.prototype.fields, fi = f.items, fl = f.length;
        var blankObj={};
        for(var j = 0; j < fl; j++){
            f = fi[j];
            if(f.name==='currencysymbol') {
                f.defValue = this.symbol
            }  
            blankObj[f.name]='';
            if(!Wtf.isEmpty(f.defValue))
                blankObj[f.name]=f.convert((typeof f.defValue == "function"?f.defValue.call():f.defValue));
        }
        return blankObj;
    },
    /*
     * Get value from Json assign with respective data index
     */
   
    setStoreRecordValueFromJSONObject : function(rec, jsonobj) {
        var keyForRec="";
        for (var key in jsonobj) {
            if(key=="billno"){
                keyForRec="documentno";
                rec[keyForRec] = jsonobj[key];
            }else if(key=="billid"){
                keyForRec="documentid";
                rec[keyForRec] = jsonobj[key];
            }else if(key=="invType"){
                keyForRec="type";
                rec[keyForRec] = this.parentObject.grid.INVType;
                rec[key] = jsonobj[key];
            }else if(key=="amountdue"){
                keyForRec="enteramount";
                rec[keyForRec] = jsonobj[key];
                rec[key] = jsonobj[key];
            }else if (jsonobj.hasOwnProperty(key) && rec[key]!== undefined) {
                rec[key] = jsonobj[key];
            }
        }
        return rec;
    }
});
