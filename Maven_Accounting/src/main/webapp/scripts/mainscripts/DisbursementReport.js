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

Wtf.account.DisbursementReport = function(config) {
    this.label='Disbursement';
    this.sm = new Wtf.grid.CheckboxSelectionModel({
    });
    Wtf.apply(this, config);
    this.Store = new Wtf.data.Store({  
        url:"ACCLoanCMN/getLoanDisbursements.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:"totalCount",
            root: "data"
        }) 
    });
    this.Store.on('beforeload', function() {
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
        this.Store.baseParams=currentBaseParams; 
        this.exportButton.enable()
        this.printButton.enable()
                 
    },this);
    WtfGlobal.setAjaxTimeOut();
    this.Store.on('load', this.handleStoreOnLoad, this);
    this.Store.on('datachanged', function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    }, this);
    
   
    this.grid = new Wtf.grid.GridPanel({    
        store:this.Store,
        border:false,
        columns: [],
        layout:'fit',
        sm:this.sm,
         viewConfig: {
            forceFit: true
        },
        loadMask:true
    });
    this.grid.flag = 0;
    this.grid.on('rowclick', Wtf.callGobalDocFunction, this);
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:"Search by Loan Ref no",                                //WtfGlobal.getLocaleText("acc.stockLedger.QuickSearchEmptyText"), // "Search by Document no, Description, Code, Party / Cost Center ...",
        width: 200,
        id:"quickSearch"+config.helpmodeid,
        field: 'transactionNumber'
    });
    
    this.resetBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  // 'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  // 'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
    
    this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  // 'From',
        name:'startdate',
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true)
    });
    
    this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  // 'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        value:WtfGlobal.getDates(false)
    });
   
    this.fetchBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  // 'Fetch',
        tooltip:WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
        style:"margin-left: 6px;",
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.fetchStatement                        
    });
     this.repaymentSheduleBttn = new Wtf.Toolbar.Button({
        disabled:true,
        text: WtfGlobal.getLocaleText("acc.loan.repaymentreport.repaymentDetails"),
        style:"margin-left: 6px;",
        iconCls :getButtonIconCls(Wtf.etype.add),
        scope:this,
        handler:this.repaymentshedule                        
     });
     
     this.editBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.edit"),  //'Edit',
        tooltip :WtfGlobal.getLocaleText("acc.disbursementreport.edit"),
        id: 'btnEdit' + this.id,
        scope: this,
        hidden:false,
        iconCls :getButtonIconCls(Wtf.etype.edit),
        disabled :true
    });
        
    this.editBttn.on('click',(this.editTransaction.createDelegate(this,[false])));

        this.deleteBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.rem.7"),  //+' '+this.label +' '+WtfGlobal.getLocaleText("acc.field.Permanently"),
            scope: this,
            tooltip:WtfGlobal.getLocaleText("acc.rem.6"), //+' '+WtfGlobal.getLocaleText("acc.field.Permanently"),  
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            hidden: false,
            disabled :true,
            handler:this.handleDelete.createDelegate(this)
        })
       
     this.exportButton=new Wtf.exportButton({
        obj:this,
        id:"exportReports"+this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        disabled :true,
        scope : this,
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
            //subMenu:true
        },
        params:{
            enddate :  WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue())
        },
        get:Wtf.autoNum.LoanDisbursementReport
    });
     this.exportButton.on("click",function(){
             this.exportButton.setParams({
                startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            });
    },this);
    
    this.printButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.sales.printTT"),  //'Print report details',
        disabled :true,
        params:{ 	
            stdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate:  WtfGlobal.convertToGenericDate(this.endDate.getValue()),		
            name: WtfGlobal.getLocaleText("acc.field.StockLedger"),
            isStockLedger : true
        },
        label: WtfGlobal.getLocaleText("acc.field.StockLedger"),
        menuItem:{
            print:true
        },
        get:Wtf.autoNum.LoanDisbursementReport
    })
    this.btnArr=[];
     
    this.btnArr.push(this.quickPanelSearch);
    this.btnArr.push(this.resetBttn);
    this.btnArr.push('-');
    this.btnArr.push(WtfGlobal.getLocaleText("acc.common.from"));
    this.btnArr.push(this.startDate);
    this.btnArr.push(WtfGlobal.getLocaleText("acc.common.to"));
    this.btnArr.push(this.endDate);
    this.btnArr.push(this.fetchBttn);
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.loanDisbursement, Wtf.Perm.loanDisbursement.reEdit)) {
        this.btnArr.push(this.editBttn);
    }
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.loanDisbursement, Wtf.Perm.loanDisbursement.reDelete)) {
        this.btnArr.push(this.deleteBttn);
    }
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.loanDisbursement, Wtf.Perm.loanDisbursement.rerepayment)) {
        this.btnArr.push(this.repaymentSheduleBttn);
    }    
    
    
    this.sm.on("selectionchange", this.enableDisableButtons,this);
    Wtf.account.DisbursementReport.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.DisbursementReport,Wtf.Panel, {
        onRender: function(config){
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar:this.btnArr,
//                tbar: [this.quickPanelSearch, this.resetBttn, '-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate, this.fetchBttn,this.editBttn,this.deleteBttn,this.repaymentSheduleBttn],
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.Store,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                    }),
                    items:[this.exportButton,this.printButton]
                })
            }]
        }); 
        this.add(this.leadpan);
        this.loadStore();
        Wtf.account.DisbursementReport.superclass.onRender.call(this,config);
    },
    
    handleResetClick:function() {
        if(this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
    },
    handleStoreOnLoad: function(store) {
        var columns = [];
        columns.push(this.sm);
        var scope =this;    
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.dataIndex == "applicationdate" || column.dataIndex == "firstpaymentdate" || column.dataIndex == "approveddate") { 
                column.renderer = WtfGlobal.onlyDateDeletedRenderer;
            } else if (column.dataIndex == "loanamount") {
                column.renderer = WtfGlobal.withoutRateCurrencyDeletedSymbol;
            } else if( column.dataIndex == "loanrefno" || column.dataIndex == "suretyname"){ 
                column.renderer = WtfGlobal.deletedRenderer; 
            } else if( column.dataIndex == "attachment"){ 
                column.renderer = Wtf.DownloadLink.createDelegate(scope); 
            } else if( column.dataIndex == "attachdoc"){ 
                column.renderer = WtfGlobal.attachmentRenderer;
            }
            columns.push(column);
        });
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();
            
        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.quickPanelSearch.StorageChanged(store);
    },
    editTransaction:function(){                          //check whether record from POS or not
        var formrec = this.grid.getSelectionModel().getSelected();
        if(formrec.data.isPaid){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.loan.disbursementedit")],2);
            return;
        }
        callLoanDisbursement(true, formrec, null);
    },
     loadStore:function(){
       this.Store.load({
           params : {
               start : 0,
               limit :  this.pP.combo!=undefined?this.pP.combo.value:30,
               ss : this.quickPanelSearch.getValue(),
               pagingFlag:true
           }
       });
       this.Store.on('load',this.storeloaded,this);
    },
     storeloaded:function(store){
        this.quickPanelSearch.StorageChanged(store);
    },
   
  handleDelete:function(){
      var formrec = this.grid.getSelectionModel().getSelected();
      if(formrec.data.isPaid){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.loan.disbursementdelete")],2);
            return;
        }
        if(this.grid.getSelectionModel().hasSelection()==false){
            WtfComMsgBox(34,2);
            return;
        }
        var data=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.rem.146")+" "+this.label+"?",function(btn){
            if(btn!="yes") {
                for(var i=0;i<this.recArr.length;i++){
                    var ind=this.Store.indexOf(this.recArr[i])
                    var num= ind%2;
                    WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
                }
                return;
            }
      
            var idData = "";
            for(i=0;i<this.recArr.length;i++){
                var rec = this.recArr[i];
                idData += "{\"disbursementid\":\""+rec.get('id')+"\"},";
            }
            if(idData.length>1){
                idData=idData.substring(0,idData.length-1);
            }
            data="["+idData+"]";
            WtfGlobal.setAjaxTimeOut();
            Wtf.Ajax.requestEx({
                url:"ACCLoanCMN/deleteDisbursementsPermanent.do",
                params:{
                    data:data
                }
            },this,this.genSuccessResponse,this.genFailureResponse);           
        },this);
    },
    genSuccessResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        var superThis = this;
        WtfComMsgBox([this.label,response.msg],response.success*2+1,"","",function(btn){
            if(btn=="ok"){
                for(var i=0;i<superThis.recArr.length;i++){
                    var ind=superThis.Store.indexOf(superThis.recArr[i])
                    var num= ind%2;
                    WtfGlobal.highLightRowColor(superThis.grid,superThis.recArr[i],false,num,2,true);
                }
                if(response.success){
                    (function(){
                        superThis.loadStore();
                    }).defer(WtfGlobal.gridReloadDelay(),superThis);
                }
            }
        });
    },
    genFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.Store.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    fetchStatement:function() {
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();
                 
        if(this.sDate > this.eDate){
            WtfComMsgBox(1,2);
            return;
        }
        
        var fromdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        var todate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        
        this.Store.load({
            params: {
                startdate:fromdate,
                enddate:todate,
                start:0,
                ss:this.quickPanelSearch.getValue(),
                limit:this.pP.combo.value
            }
        });
    },
    enableDisableButtons:function(){
        if(this.sm.getCount()==1){
            if(this.repaymentSheduleBttn)this.repaymentSheduleBttn.enable();
            if(this.editBttn)this.editBttn.enable();
        }else{
            if(this.repaymentSheduleBttn)this.repaymentSheduleBttn.disable();
            if(this.editBttn)this.editBttn.disable();
        }
        if(this.sm.getCount() > 0){
            if(this.deleteBttn)this.deleteBttn.enable();
        }else{
            if(this.deleteBttn)this.deleteBttn.disable();
        }
    },
    repaymentshedule:function() {
        this.rec = this.sm.getSelections();
        this.callRepaymentScheduleReport(this.rec[0]);
         
     },
    QuantityRender: function(v,m,rec){
        var val = WtfGlobal.withCurrencyUnitPriceRenderer(v,m,rec)
        if(rec.data.transactionNumber==""){
            return '<b>'+val+'</b>';
        }else{
            return val;
        }
    },
    unitRenderer: function(value,metadata,record) {
        if(value != '') {
            value = parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        }
        return value;
    },
   callRepaymentScheduleReport : function(rec){
        var reportPanel = Wtf.getCmp('repaymentShedule');
        if(reportPanel == null){
            reportPanel = new Wtf.account.LoanRepaymentShedule({
                id : 'repaymentShedule',
                border : false,
                rec:rec,
                title: WtfGlobal.getLocaleText("acc.loan.repaymentreport.repaymentDetails"),
                tabTip: WtfGlobal.getLocaleText("acc.loan.repaymentreport.repaymentDetails"),
                layout: 'fit',
                closable : true,
                iconCls:'accountingbase viewreceivepayment' //  getButtonIconCls(Wtf.etype.inventoryval)
            });
            Wtf.getCmp('as').add(reportPanel);
        }
        Wtf.getCmp('as').setActiveTab(reportPanel);
        Wtf.getCmp('as').doLayout();
    }
});


/********************************************************************************************************************************
 *                                           LOAN REPAYMENT SCHEDULE
 ********************************************************************************************************************************/
Wtf.account.LoanRepaymentShedule = function(config) {  
    this.rec=config.rec;
    Wtf.apply(this, config);
    
    this.Store = new Wtf.data.Store({ 
        url:"ACCLoanCMN/getRepaymentSheduleDetails.do",
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:"totalCount",
            root: "data"
        }) 
    });
    this.Store.on('beforeload', function() {
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
        currentBaseParams.disbursement=this.rec.get('id'),
        currentBaseParams.paymentStatus=this.paymentStatusCombo.getValue(),
        this.Store.baseParams=currentBaseParams; 
        this.exportButton.enable();
        this.printButton.enable();
                 
    },this);
    WtfGlobal.setAjaxTimeOut();
    this.Store.on('load', this.handleStoreOnLoad, this);
    this.Store.on('datachanged', function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    }, this);
    
    this.sm = new Wtf.grid.CheckboxSelectionModel({
        singleSelect :false
    });
    this.grid = new Wtf.grid.GridPanel({    
        store:this.Store,
        border:false,
        columns: [],
        sm:this.sm,
        layout:'fit',
         viewConfig: {
            forceFit: true
        },
        loadMask:true
    });
    
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:"Search by Loan Ref no",                                //WtfGlobal.getLocaleText("acc.stockLedger.QuickSearchEmptyText"), // "Search by Document no, Description, Code, Party / Cost Center ...",
        width: 200,
        id:"quickSearch"+config.helpmodeid,
        field: 'transactionNumber'
    });
    
    this.resetBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  // 'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  // 'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
    
    this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  // 'From',
        name:'startdate',
        format:WtfGlobal.getOnlyDateFormat(),
    //    readOnly:true,
        value:WtfGlobal.getDates(true)
    });
    
    this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  // 'To',
        format:WtfGlobal.getOnlyDateFormat(),
      //  readOnly:true,
        name:'enddate',
        value:WtfGlobal.getDates(false)
    });
   
    this.fetchBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  // 'Fetch',
        tooltip:WtfGlobal.getLocaleText("acc.stockLedger.FetchToolTip"), // "Select a time period to view corresponding transactions.",
        style:"margin-left: 6px;",
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.fetchStatement                        
    });
    
     this.receivePayment = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.invoiceList.recievePay"),  // 'Fetch',
        tooltip:WtfGlobal.getLocaleText("acc.invoiceList.recievePay"), // "Select a time period to view corresponding transactions.",
        style:"margin-left: 6px;",
        iconCls:'accountingbase receivepayment',
//        scope:this,
        handler:this.viewReceivePayment.createDelegate(this)                        
    });
    
    this.paymentstatusStore = new Wtf.data.SimpleStore({
        fields:[{
            name:'id'
        },{
            name:'name'
        }],
        data:[['0','Paid'],['1','UnPaid'],['All','All Records']]  
    }); 
    this.paymentStatusCombo=new Wtf.form.ExtFnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.loan.repaymentreport.Paymentstatus"),  //'Payment status',
        name:'paymentStatusCombo',
        store:this.paymentstatusStore,
        valueField:'id',
        displayField:'name',
        mode: 'local',
        width:150,
        listWidth:400,
        hiddenName:'paymentStatusCombo',
        allowBlank:false,
        forceSelection:true,
        extraFields:[],
        triggerAction:'all'
    });
     this.paymentStatusCombo.setValue("All");
     this.exportButton=new Wtf.exportButton({
        obj:this,
        id:"exportReports"+this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details (Repayment Details Report)',
        disabled :true,
        scope : this,
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
            //subMenu:false
        },
        params:{
            enddate :  WtfGlobal.convertToGenericDate(this.endDate.getValue()),
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue())
        },
        get:Wtf.autoNum.RepaymentScheduleReport
    });
     this.exportButton.on("click",function(){
             this.exportButton.setParams({
                startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
            });
    },this);
    
    this.printButton=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),
        tooltip :WtfGlobal.getLocaleText("acc.sales.printTT"),  //'Print report details',
        disabled :true,
        params:{ 	
            stdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate:  WtfGlobal.convertToGenericDate(this.endDate.getValue()),		
            name: WtfGlobal.getLocaleText("acc.field.StockLedger"),
            isStockLedger : true
        },
        label: WtfGlobal.getLocaleText("acc.field.StockLedger"),
        menuItem:{
            print:true
        },
        get:Wtf.autoNum.RepaymentScheduleReport
    })
    this.sm.on("selectionchange", this.enableDisableButtons,this);
    Wtf.account.LoanRepaymentShedule.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.LoanRepaymentShedule,Wtf.Panel, {
        onRender: function(config){
        this.Store.load({
            params:{
                start:0,
                limit:30,
                isprovalreport:true
            }
        });
        
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: [this.quickPanelSearch, this.resetBttn, '-', WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate, this.paymentStatusCombo, this.fetchBttn,this.receivePayment],
                
                bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.Store,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                    }),
                    items:[this.exportButton,this.printButton]
                })
            }]
        }); 
        this.add(this.leadpan);
        
        Wtf.account.LoanRepaymentShedule.superclass.onRender.call(this,config);
    },
    
    handleResetClick:function() {
        if(this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
    },
    
    fetchStatement:function() {
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();
                 
        if(this.sDate > this.eDate){
            WtfComMsgBox(1,2);
            return;
        }
        
        var fromdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        var todate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        
        this.Store.load({
            params: {
                startdate:fromdate,
                enddate:todate,
                start:0,
                ss:this.quickPanelSearch.getValue(),
                limit:this.pP.combo.value
            }
        });
    },
    enableDisableButtons:function(){
        var receivePayBtn=false;
        this.recArr =this.grid.getSelectionModel().getSelections();
        for(var i=0;i<this.recArr.length;i++){
            if(this.recArr[i].data.paymentstatus=="Paid"){
                receivePayBtn=true;    
            }
        }
        if(receivePayBtn){
            this.receivePayment.disable();
        }else{
            this.receivePayment.enable();
        }
    },
    viewReceivePayment : function(){
       
        var rec=this.rec;
        var receivePayBtn=false;
        this.isSelected=false;
        if(this.grid.getSelectionModel().getCount()>0){
            this.isSelected=true;
        }
        rec.jsonArray=this.getSelectedRecords();
        rec.isSelected=this.isSelected;
        callPaymentReceiptNew(1,true, undefined,undefined,false,undefined,rec,true);
    },
    getSelectedRecords: function() {
        var arr = [];
        var selectionArray=this.grid.getSelectionModel().getSelections();
        for( var i=0;i<selectionArray.length;i++){
            arr.push(this.Store.indexOf(selectionArray[i]));
        }
        var jarray = WtfGlobal.getJSONArrayWithoutEncoding(this.grid, true, arr);
        return jarray;
    },
    handleStoreOnLoad: function(store) {
        var columns = [];
        columns.push(this.sm);         
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.dataIndex == "startingbalance" || column.dataIndex == "monthlyinstalment" || column.dataIndex == "intrest" || column.dataIndex == "principal" ||  column.dataIndex == "endingbalance") { 
                column.renderer = WtfGlobal.withoutRateCurrencyDeletedSymbol;   
            } else if( column.dataIndex == "monthyear" || column.dataIndex == "paymentstatus"){ 
                column.renderer = WtfGlobal.deletedRenderer;
            }
            columns.push(column);
        });
        this.grid.getColumnModel().setConfig(columns);
        this.grid.getView().refresh();           
        if (this.Store.getCount() < 1) {
            this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.quickPanelSearch.StorageChanged(store);
    },
    
    QuantityRender: function(v,m,rec){
        var val = WtfGlobal.withCurrencyUnitPriceRenderer(v,m,rec)
        if(rec.data.transactionNumber==""){
            return '<b>'+val+'</b>';
        }else{
            return val;
        }
    },
    unitRenderer: function(value,metadata,record) {
        if(value != '') {
            value = parseFloat(getRoundofValue(value)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL);
        }
        return value;
    }
});
