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
Wtf.RepeatedPaymentReport = function(config){
    this.isCustBill=config.isCustBill;
    this.isReceipt = config.isReceipt;
    this.GridRec = Wtf.data.Record.create ([
    {name: 'billid'},
    {name:'companyid'},
    {name:'companyname'},
    {name: 'billno'},
    {name: 'detailtype'},
    {name: 'expireDate',type:'date'},
    {name: 'journalentryid'},
    {name:'journalentrydate'},
    {name: 'entryno'},
    {name: 'currencysymbol'},
    {name: 'externalcurrencyrate'},
    {name: 'address'},
    {name: 'deleted'},
    {name: 'billdate',type:'date'},
    {name: 'memo'},
    {name: 'amount'},
    {name: 'receiptamount'},
    {name: 'currencyid'},        
    {name:'payee'},
    {name: 'interval'},
    {name:'intervalType'},
    {name:'startDate',type:'date'},
    {name:'childCount'},
    {name:'nextDate',type:'date'},
    {name:'isactivate'},
    {name:'repeateid'},
    {name:'NoOfPaymentpost'},
    {name:'NoOfRemainPaymentpost'},
    {name: 'chequeOption'},
    {name:'paymentaccountid'},
    {name:'ispendingapproval',type:'boolean'},
    ]);
    
    this.Store = new Wtf.data.Store({
        remoteSort:true,
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"
        },this.GridRec),
        url: this.isReceipt?"ACCReceiptCMN/getReceipts.do":"ACCVendorPaymentCMN/getPayments.do",
        baseParams:{                                
            getRepeatePayment: true,                
            companyids:companyids,
            gcurrencyid:gcurrencyid,
            userid:loginid,
            ispendingAproval:config.ispendingAproval
        }
    });    
    this.usersRec = new Wtf.data.Record.create([
    {name: 'userid'},
    {name: 'username'},
    {name: 'fname'},
    {name: 'lname'},
    {name: 'image'},
    {name: 'emailid'},
    {name: 'lastlogin',type: 'date'},
    {name: 'aboutuser'},
    {name: 'address'},
    {name: 'contactno'},
    {name: 'rolename'},
    {name: 'roleid'}
    
    ]);
    this.userds = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            totalProperty: 'count',
            root: "data"
        },this.usersRec),
        url : "ProfileHandler/getAllUserDetails.do",
        baseParams:{
            mode:11
        }
    });
    this.userds.load();


    this.Store.load({
        params:{
            start:0,
            limit:30
        }
    });
    
    this.expandRec = Wtf.data.Record.create ([
    {
        name:'parentPaymentId'
    },

    {
        name:'PaymentId'
    },

    {
        name:'paymentNo'
    },
    {
        name:'isPaymentGenarated'
    }
    ]);
    this.expandStoreUrl =this.isReceipt?"":"ACCVendorPaymentCMN/getRepeatePaymentDetails.do";
    this.expandStore = new Wtf.data.Store({
        url:this.expandStoreUrl,
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    
    
    this.Store.on('datachanged', function(store) {
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        this.expandButtonClicked = false;
        this.expander.resumeEvents('expand');           // event is suspended while expanding all records.
    }, this);

    var btnArr=[];
    btnArr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:this.isReceipt?WtfGlobal.getLocaleText("acc.RPList.repeatedRP.search"):WtfGlobal.getLocaleText("acc.MPList.repeatedMP.search"),
        width: 200,
        field: 'billno',
        Store:this.Store
    }));
    btnArr.push(this.resetBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        handler: this.handleResetClick,
        disabled: false
    }));
    btnArr.push(this.addRecurringPayment=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.add"),  // "Add",
        tooltip :this.isReceipt?WtfGlobal.getLocaleText("acc.field.AllowsyoutoaddRecurringMP"):WtfGlobal.getLocaleText("acc.field.AllowsyoutoaddRecurringRP"),
        scope: this,
        iconCls : getButtonIconCls(Wtf.etype.add),
        handler : this.addRecurringPaymentHandler
    }));
   
    btnArr.push(this.editBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.edit"),  //'Edit',
        tooltip : this.isReceipt?WtfGlobal.getLocaleText("acc.repeatedRP.EditToolTip"):WtfGlobal.getLocaleText("acc.repeatedMP.EditToolTip"),
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.edit),
        handler: this.repeateInvoice,
        hidden: config.consolidateFlag,
        disabled: true
    }));
    btnArr.push("-");
    
    btnArr.push(this.deleteBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.rem.7"),  //'Delete',
        tooltip : WtfGlobal.getLocaleText("acc.field.allowsyoutodeletetherecord"),
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.deletebutton),
        handler: this.deleteRepeateMP,
        hidden: (config.consolidateFlag || this.isLeaseFixedAsset),
        disabled: true
    }));
    btnArr.push("-");
        
    btnArr.push(this.activeDeactiveBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.levelsetting.activatelevel"),  //'Activate/Deactivate',
        tooltip : WtfGlobal.getLocaleText("acc.recurringMPActivate.tooltip"),
        scope: this,        
        handler: this.activateDeactivateRecurringInvoice,
        hidden: config.consolidateFlag,
        disabled: true
    }));
    
    
    btnArr.push("-");
    btnArr.push(this.pendingApprovalBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.ViewPendingApprovals"),
        tooltip : WtfGlobal.getLocaleText("acc.field.ViewPendingApprovals"),
        id: 'pendingApprovals' + this.id,
        scope: this, 
        isPendingApproval:true,
        //hidden: config.consolidateFlag,
        iconCls :getButtonIconCls(Wtf.etype.reorderreport)
    }));
    btnArr.push(this.approveJEBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.cc.24"),
        tooltip : WtfGlobal.getLocaleText("acc.field.Approvepending.Recurring.MP"), //Issue 31009 - [Pending Approval]Window name should be "Approve Pending Invoice" instead of "Approve Pending Approval". it should also have deskera logo
        id: 'approvepending' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.add),
        disabled :true,
        isApproveBtn:true,
        hidden:true,
        handler : this.approveRecurringPendingMP
    }));
    this.approveJEBttn.hide();
     btnArr.push( this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Expand"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        //        hidden:(this.moduleid == Wtf.Acc_Purchase_Order_ModuleId || this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId || this.moduleid == Wtf.Acc_Sales_Order_ModuleId || this.moduleid == Wtf.Acc_Vendor_Quotation_ModuleId)?false:true,
        scope: this,
        handler: function() {
            if (this.expandCollpseButton.getText() == WtfGlobal.getLocaleText("acc.field.Expand")) {
                this.expandButtonClicked = true;
            }
            expandCollapseGrid(this.expandCollpseButton.getText(), this.expandStore, this.grid.plugins, this);
        }
    }));
    
    this.pendingApprovalBttn.on('click', function(){
        var panel = null;
        var ispendingAproval = true;
        panel = Wtf.getCmp("RecurringMakePaymentDetailsPending");
        if(panel==null){
            panel = getPendingRecurringMakePaymentTab("", undefined,false,ispendingAproval);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();   
    }, this);
   
    if(config.ispendingAproval){    //Hide all buttons in Pending Approval window
        this.approveJEBttn.show();
        this.editBttn.hide();
        this.addRecurringPayment.hide();
        this.activeDeactiveBttn.hide();
        this.pendingApprovalBttn.hide();
    }
    
    this.expander = new Wtf.grid.RowExpander({});    
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);
    this.gridView1 = {
        forceFit:true,
        emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
    };
    this.grid = new Wtf.grid.GridPanel({
        stripeRows :true,
        store:this.Store,
        border:false,
        sm:this.sm,
        layout:'fit',
        plugins: this.expander,
        viewConfig:this.gridView1,
        columns:[
        this.expander,this.sm,
        {
            header:this.isReceipt?WtfGlobal.getLocaleText("acc.prList.gridReceiptNo"):WtfGlobal.getLocaleText("acc.pmList.gridPaymentNo"),
            dataIndex:'billno',
            pdfwidth:75,
            sortable:true,
            renderer: WtfGlobal.deletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.repeatedJE.Gridcol2"),  
            dataIndex:'companyname',
            width:20,
            pdfwidth:150,
            hidden:true
        },{
            header:WtfGlobal.getLocaleText("acc.repeatedJE.Gridcol3"),
            pdfwidth:75,
            sortable:true,
            renderer:WtfGlobal.deletedRenderer,
            dataIndex:'memo'
        },{
            header:WtfGlobal.getLocaleText("acc.invList.repeated.sched"),  //"Schedular Start Date",
            dataIndex:'startDate',
            pdfwidth:80,
            sortable:true,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.recurringDocs.expiryDate"),  //"Schedular Start Date",
            dataIndex:'expireDate',
            pdfwidth:80,
            sortable:true,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:this.isReceipt?WtfGlobal.getLocaleText("acc.repeatedMP.generated"):WtfGlobal.getLocaleText("acc.repeatedMP.generated"),
            dataIndex:'childCount',
            align: "right",
            pdfwidth:80
        },{
            header:WtfGlobal.getLocaleText("acc.recurringPayments.totalCount"),
            dataIndex:'NoOfPaymentpost',
            pdfwidth:80,
            align: "right",
            sortable:true
        },{
            header:WtfGlobal.getLocaleText("acc.invList.repeated.interval"),  //"Interval",
            dataIndex:'interval',
            pdfwidth:80,
            renderer: function(a,b,c){
                var idx = Wtf.intervalTypeStore.find("id", c.data.intervalType);
                if(idx == -1) {
                    return a+" "+c.data.intervalType;
                } else {
                    return a+" "+Wtf.intervalTypeStore.getAt(idx).data.name;
                }
            }
        },{
            header:this.isReceipt?WtfGlobal.getLocaleText("acc.repeatedRP.Gridcol5"):WtfGlobal.getLocaleText("acc.repeatedMP.Gridcol5"),
            dataIndex:'nextDate',
            pdfwidth:80,
            sortable:true,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.repeatedJE.Gridcol6"),
            pdfwidth:75,
            renderer:WtfGlobal.booleanRenderer,
            dataIndex:'isactivate'
        }],
        tbar:btnArr,
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.Store,
            displayInfo: true,
            // displayMsg: WtfGlobal.getLocaleText("acc.rem.116"),
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
                })
        })
    });
    this.items=[this.grid];

    
    this.expandStore.on("beforeload", function(store){
        this.expandStoreUrl = this.isReceipt?"":"ACCVendorPaymentCMN/getRepeatePaymentDetails.do";
        store.proxy.conn.url = this.expandStoreUrl;
    }, this);
    
    
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.expander.on("expand",this.onRowexpand,this);
    WtfGlobal.getGridConfig(this.grid,Wtf.Acc_Recurring_MakePayment_ModuleId,false,false);
    this.grid.on('render', function () {
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    }, this);


    Wtf.apply(this,config);
    Wtf.RepeatedPaymentReport.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.RepeatedPaymentReport, Wtf.Panel,{

    onRowexpand:function(scope, record, body){
        this.expanderBody=body;        
        this.expandStore.load({
            params:{
                parentPaymentId:record.data['billid']
                }
            });
},
expandRow:function(){
    Wtf.MessageBox.hide();
},

   fillExpanderBody:function(){
        var disHtml = "";
        var disHtmlNoGen = "";
        var header = "";
        var head="";
        var blkStyle="";
        var previous="";

        if(this.expandStore.getCount()==0){
            var head=this.isReceipt?WtfGlobal.getLocaleText("acc.repeatedRP.RowHead1"):WtfGlobal.getLocaleText("acc.repeatedMP.RowHead1");
            header = "<span style='color:#15428B;display:block;'>"+ head +"</span>"; 
            disHtml += "<div style='width:95%;margin-left:3%;'>" + header + "<br/></div>";
            this.expanderBody.innerHTML = disHtml;
        } else {
            head=this.isReceipt?WtfGlobal.getLocaleText("acc.repeatedRP.RowHead2"):WtfGlobal.getLocaleText("acc.repeatedMP.RowHead2");
            blkStyle = "display:block;float:left;width:150px;Height:14px;"
            header = "<span class='gridHeader'>"+head+"</span>"; 
            for(var i=0;i<this.expandStore.getCount();i++){
                var rec=this.expandStore.getAt(i);
                
                /*
                 *To set disHtml as blank after one record.
                 */
                if(i==0){
                    previous=rec.data['parentPaymentId'];
                }
                if(i>0){
                    if(previous!=rec.data['parentPaymentId']){
                        header="";
                        disHtml="";
                        head=this.isReceipt?WtfGlobal.getLocaleText("acc.repeatedRP.RowHead2"):WtfGlobal.getLocaleText("acc.repeatedMP.RowHead2");
                        blkStyle = "display:block;float:left;width:150px;Height:14px;"
                        header = "<span class='gridHeader'>"+head+"</span>";
                        previous=rec.data['parentPaymentId'];
                    }
                    
                }
                // It's for not generated payment
                if(!rec.data.isPaymentGenarated){
                    var headNoGen=this.isReceipt?WtfGlobal.getLocaleText("acc.repeatedRP.RowHead1"):WtfGlobal.getLocaleText("acc.repeatedMP.RowHead1");
                    var  headerNoGen = "<span style='color:#15428B;display:block;'>"+ headNoGen +"</span>"; 
                    disHtmlNoGen += "<div style='width:95%;margin-left:3%;'>" + headerNoGen + "<br/></div>";
                     /*
                     * check  parent payment id in record
                     *  if exist, then expand row with respective index.
                     */
                    var moreIndex = this.grid.getStore().findBy(
                        function(record, id) {
                            if (record.get('billid') === rec.data['parentPaymentId']&&!rec.data['isPaymentGenarated']) {
                                return true;  // a record with this data exists 
                            }
                            return false;  // there is no record in the store with this data
                        }, this);
                    if (moreIndex != -1) {
                        var body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', this.grid.getView().getRow(moreIndex));
                        body.innerHTML = disHtmlNoGen;
                        if (this.expandButtonClicked) {
                            this.expander.suspendEvents('expand');              //suspend 'expand' event of RowExpander only in case of ExpandAll.
                            this.expander.expandRow(moreIndex);                // After data set to Grid Row, expand row forcefully.
                        }
                    }
                }else{
                    /*
                     * check  parent payment id in record
                     *  if exist, then expand row with respective index.
                     */
                    
                    header += "<span style="+blkStyle+">"+
                    rec.data.paymentNo+"</a>"+
                    "</span>";
                    disHtml += "<div style='width:95%;margin-left:3%;'>" + header + "<br/></div>";
                    var moreIndex = this.grid.getStore().findBy(
                        function(record, id) {
                            if (record.get('billid') === rec.data['parentPaymentId']) {
                                return true;  // a record with this data exists 
                            }
                            return false;  // there is no record in the store with this data
                        }, this);
                    if (moreIndex != -1) {
                        var body = Wtf.DomQuery.selectNode('tr:nth(2) div.x-grid3-row-body', this.grid.getView().getRow(moreIndex));
                        body.innerHTML = disHtml;
                        if (this.expandButtonClicked) {
                            this.expander.suspendEvents('expand');              //suspend 'expand' event of RowExpander only in case of ExpandAll.
                            this.expander.expandRow(moreIndex);                // After data set to Grid Row, expand row forcefully.
                        }
                    }
                }
            }
        }
    },

handleResetClick:function(){
    if(this.quickPanelSearch.getValue()){
        this.quickPanelSearch.reset();
        this.Store.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
        this.Store.on('load',this.storeloaded,this);
    }
},
addRecurringPaymentHandler:function(){
    var formrec;
    var withoutinventory;
    var isaddRecurringJE = true;
    callRepeatedPaymentWindow(withoutinventory, formrec, isaddRecurringJE);
},
storeloaded:function(store){
    this.quickPanelSearch.StorageChanged(store);
    new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
        this.grid.on('statesave', this.saveMyStateHandler, this);
    }, this);
},
enableDisableButtons:function(){
    if(this.sm.getCount()==1){
        this.editBttn.enable();
    } else {
        this.editBttn.disable();
    }
    if(this.sm.getCount()>=1){
        this.approveJEBttn.enable();
        this.activeDeactiveBttn.enable();
        this.deleteBttn.enable();
    } else {
        this.approveJEBttn.disable();
        this.activeDeactiveBttn.disable();
        this.deleteBttn.disable();        
    }
},
repeateInvoice:function(){
    var formrec= this.grid.getSelectionModel().getSelected();
    var withoutinventory= this.grid.getSelectionModel().getSelected().data.withoutinventory;
    var isaddRecurringJE=false;
    callRepeatedPaymentWindow(withoutinventory, formrec, isaddRecurringJE);
},    
activateDeactivateRecurringInvoice:function(){
    var data=[];
    this.formrec = this.grid.getSelectionModel().getSelections();       
    for(var i=0;i<this.formrec.length;i++) {
        var rec = this.formrec[i];
        var rowObject = new Object();
        rowObject['repeatedid'] = rec.data.repeateid;
        rowObject['isactivate'] = rec.data.isactivate;   //If already activate then make it deactivate & vice versa            
        data.push(rowObject);
    }        
    var url="";
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.je.confirm"), WtfGlobal.getLocaleText("acc.je.msg3"),function(btn){
        if(btn!="yes") return;
        url="ACCVendorPaymentNew/activateDeactivateVendorPayment.do";
        Wtf.Ajax.requestEx({
            url:url,
            params: {
                data: JSON.stringify(data)
            }
        },this,this.genSuccessResponse,this.genFailureResponse);
    }, this)
},  
genSuccessResponse:function(response){
    if(response.success){
        var msg=this.isReceipt?WtfGlobal.getLocaleText("acc.recurringRPUpdate.approved"):WtfGlobal.getLocaleText("acc.recurringMPUpdate.approved");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("Update"),msg],3);            
        this.Store.load();
    }
},
genFailureResponse:function(response){
    this.enable();
    var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
    if(response.msg)msg=response.msg;
    WtfComMsgBox([WtfGlobal.getLocaleText("Failed"),msg],2);
},

deleteRepeateMP:function(){
    var data=[];
    var rulesInvoiceNo="";
    this.formrec = this.grid.getSelectionModel().getSelections(); 
    var selectedRecordCount=this.formrec.length;
    var rulesHavingChild=0;
    var rulesHavingNoChild=0;
    for(var i=0;i<selectedRecordCount;i++) {
        var recData = this.formrec[i].data;
        if(recData.childCount>0){
            rulesHavingChild++;
            rulesInvoiceNo+=recData.billno+","
        } else {
            rulesHavingNoChild++;
            var rowObject = new Object();
            rowObject['invoiceid'] = recData.billid;
            rowObject['invoicenumber'] = recData.billno;
            rowObject['repeatedid'] = recData.repeateid;
            data.push(rowObject);
        }
    }
    if(rulesInvoiceNo.length>1){
        rulesInvoiceNo=rulesInvoiceNo.substring(0,rulesInvoiceNo.length-1);
    }
    if(selectedRecordCount==rulesHavingChild){//It means all selected record having child, No need to go for deleted.
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.repeated.allSelectedRecordUsed")],2);
    } else {
        var confmsg="";
        if(rulesHavingChild>0){
            confmsg+=WtfGlobal.getLocaleText("acc.repeated.repeatedrecords")+" "+rulesInvoiceNo+" "+WtfGlobal.getLocaleText("acc.repeated.isbeingusedsocannotbedeleted");
        }else {
            confmsg+=WtfGlobal.getLocaleText("acc.rem.238");
        }
        var url="";
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), confmsg,function(btn){
            if(btn!="yes") return;
            url="ACCVendorPaymentNew/deleteRecurringMakePayment.do";
            Wtf.Ajax.requestEx({
                url:url,
                params: {
                    data: JSON.stringify(data),
                    isSalesInvoice:this.isCustomer?true:false
                }
            },this,this.genDeleteSuccessResponse,this.genDeleteFailureResponse);
        }, this)
    }
},  
genDeleteSuccessResponse:function(response){
    if(response.success){
        var msg=WtfGlobal.getLocaleText("acc.repeated.allrecordaredeleted");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.delete"),msg],0);
        this.Store.load();
    }
},
genDeleteFailureResponse:function(response){
    this.enable();
    var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
    if(response.msg)msg=response.msg;
    WtfComMsgBox([WtfGlobal.getLocaleText("Failed"),msg],1);
},

approveRecurringPendingMP:function(){
    var data=[];
    this.formRecord = this.grid.getSelectionModel().getSelections();
    for(var i=0;i<this.formRecord.length;i++) {
        var rec = this.formRecord[i];
        var rowObject = new Object();
        rowObject['repeatedid'] = rec.data.repeateid;
        rowObject['ispendingapproval'] = rec.data.ispendingapproval;   //If already activate then make it deactivate & vice versa            
        data.push(rowObject);
    }
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected") +" "+ WtfGlobal.getLocaleText("acc.invoiceList.mP") + "?", function(btn) {
        if (btn == "yes") {
            var url = "ACCVendorPaymentNew/activateDeactivateVendorPayment.do";
            Wtf.Ajax.requestEx({
                url:url,
                params: {
                    data: JSON.stringify(data)
                }
            },this,this.genSuccessResp,this.genFailureResp);
        }
    }, this)
},
genSuccessResp:function(response){
    if(response.success){
        var msg=WtfGlobal.getLocaleText("acc.recurringmpApproval.approved");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("Approved"),msg],0);
        this.Store.load();
    }
},
genFailureResp:function(response){
    this.enable();
    var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
    if(response.msg)msg=response.msg;
    WtfComMsgBox([WtfGlobal.getLocaleText("Failed"),msg],1);
},
saveMyStateHandler: function(grid,state){
    WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.Acc_Recurring_MakePayment_ModuleId, this.gridConfigId, false);
}
});


/*/////////////////////////////////////   FORM   ////////////////////////////////////////////////////*/

Wtf.RepeatePaymentForm = function(config){
    this.paymentRec=config.paymentRec;
    this.isaddRecurringPayment=config.isaddRecurringPayment;
    this.isReceipt = config.isReceipt;
    this.paymentMethodType = {
        Cash: 0,
        Card: 1,
        Bank: 2
    }
    Wtf.apply(this,{
        title:this.isReceipt?WtfGlobal.getLocaleText("acc.repeatedRP.recPayment"):WtfGlobal.getLocaleText("acc.repeatedMP.recPayment"),
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.repeated.savNclose"),  //'Save and Close',
            scope: this,
            handler: this.saveData.createDelegate(this,[false])
        },{
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            scope: this,
            handler:this.closeWin.createDelegate(this)
        }]
    },config);
    Wtf.RepeatePaymentForm.superclass.constructor.call(this, config);
    this.addEvents({
        'update': true,
        'cancel': true
    });
}
Wtf.extend( Wtf.RepeatePaymentForm, Wtf.Window, {
    defaultCurreny:false,
    onRender: function(config){
        Wtf.RepeatePaymentForm.superclass.onRender.call(this, config);
        this.isEdit = false;
        if(this.paymentRec!=undefined){
            if(this.paymentRec.data.repeateid){
                this.isEdit = true;
            }
            if(this.paymentRec.data.companyid){
                this.companyId = this.paymentRec.data.companyid;
            }
            if(this.paymentRec.data.paymentaccountid){
                this.paymentMethodAccountId = this.paymentRec.data.paymentaccountid;
            }
            
        }
        this.startDateValue = new Date();
        this.startDateValue = new Date(this.startDateValue.getFullYear(),this.startDateValue.getMonth(),this.startDateValue.getDate()+1);

        this.nextDateValue = this.startDateValue;
        if(this.isEdit){
            this.nextDateValue = this.paymentRec.data.nextDate;
        }
        this.creditTermDays = 0;
        if(this.paymentRec!=undefined){
            this.creditTermDays = this.paymentRec.data.creditDays?this.paymentRec.data.creditDays:0;
        }
        if(this.termdays)
        {
            this.creditTermDays=this.termdays;  
        }
        this.dueDateValue = this.calculateDueDate();

        this.paymentRecord = Wtf.data.Record.create ([
                {name:'billid'},
                {name:'billno'},
                {name:'creditDays'},
                {name:'repeateid'},
                {name:'nextDate', type:'date'},
                {name:'interval'},
                {name:'intervalType'},
                {name:'expireDate', type:'date'},
                {name: 'billid'},
                {name: 'detailtype'},
                {name :'chequeOption'},
                {name:'companyid'},
                {name:'paymentaccountid'}
        ]);

        this.paymentStoreUrl = "ACCVendorPaymentCMN/getPayments.do";
        this.paymentStore = new Wtf.data.Store({
            url:this.paymentStoreUrl,
            baseParams: {                        
                getRepeatePayment: false,
                isTemplate:true,                        
                consolidateFlag:config.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.paymentRecord)
        });
        if(this.isaddRecurringPayment) {
            this.paymentStore.baseParams.paymentWindowType= 3;            
            this.paymentStore.load();
        }
        this.MemoCM= new Wtf.grid.ColumnModel([{
            header:this.isReceipt?WtfGlobal.getLocaleText("acc.repeatedMP.MemoGridcol1"):WtfGlobal.getLocaleText("acc.repeatedMP.MemoGridcol1"),
            dataIndex:'no',
            width:120
          
        },{
            header:WtfGlobal.getLocaleText("acc.repeatedJE.Gridcol3"),
            dataIndex:"memo",
            width:310,
            editor:new Wtf.form.TextField({
                name:'memo'
            
            })
        }]);
       
       
        this.accRec = new Wtf.data.Record.create([
        {
            name: 'no'
        },{
            name: 'memo'
        }
        ]);
        this.Memostore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec),
            url : 'ACCJournal/getJERepeateMemoDetails.do'
        });
        if(this.paymentRec != undefined){
            if(this.paymentRec.data.repeateid){
                this.Memostore.load({
                    params:{
                        memofor:this.isReceipt?"RepeatedReceiptId":"RepeatedPaymentId", 
                        repeateid:this.paymentRec.data.repeateid
                    }
                });
            }
        }
        
        this.grid1 = new Wtf.grid.EditorGridPanel({
            plugins:this.gridPlugins,
            clicksToEdit:1,
            height:130,
            width:430,
            store: this.Memostore,
            cm: this.MemoCM,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.chequeRec = new Wtf.data.Record.create([
        {
            name: 'no'
        },{
            name: 'chequeno'
        },{
            name: 'chequedate',
            type:'date'
        }
        ]);
        this.chequeCM= new Wtf.grid.ColumnModel([{
            header:this.isReceipt?WtfGlobal.getLocaleText("acc.receipt.1"):WtfGlobal.getLocaleText("acc.receipt.2"),
            dataIndex:'no',
            width:90
        },
        
        {
            header:WtfGlobal.getLocaleText("payment.date.postDate"),  // Cheque Date
            dataIndex:"chequedate",
            width:110,
            renderer:WtfGlobal.onlyDateRenderer,
            editor: new Wtf.ServerDateField({
                name: "chequedate",   
                disabled: this.readOnly,
                anchor: '85%',
                allowBlank: false,
                format:  WtfGlobal.getOnlyDateFormat()
            })
        },
        {
            header:WtfGlobal.getLocaleText("acc.field.ChequeNumber"),  //Cheque number
            dataIndex:"chequeno",
            width:160,
            hidden:true,
            editor:this.chequeno = new Wtf.form.TextField({
                name:'chequeno',
                maxLength: 16,
                vtype: "alphanum",
                allowNegative: false,
                allowBlank:false
            
            })
        
        }
        ]);
       
        this.chequeno.on('blur',function(){
            var newValue= this.chequeno.getValue().trim();
            this.chequeno.setValue(newValue);
        },this);
        
        
        
        this.chequeStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.chequeRec),
            url : this.isReceipt?"ACCReceipt/saveReceipt.do":"ACCVendorPaymentNew/getChequeDetailsForRepeatedPayment.do"
        });
        this.chequeGrid = new Wtf.grid.EditorGridPanel({
            plugins:this.gridPlugins,
            clicksToEdit:1,
            height:130,
            autoWidth:true,
            store: this.chequeStore,
            cm: this.chequeCM,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.chequeGrid.on('afteredit',this.afterChequeGridEdit,this);
        if(this.paymentRec != undefined){
            if(this.paymentRec.data.repeateid){
                this.chequeStore.load({
                    params:{                             
                        repeateid:this.paymentRec.data.repeateid
                    }
                });
            }
        }
        
        this.repeateForm = new Wtf.form.FormPanel({
            border: false,
            labelWidth:230,
            autoScroll : true,
            items : [
            this.repeateId = new Wtf.form.Hidden({
                hidden:true,
                name:"repeateid"
            }),
            this.paymentList = new Wtf.form.ComboBox({
                fieldLabel:this.isReceipt?WtfGlobal.getLocaleText("acc.repeated.RPNo"):WtfGlobal.getLocaleText("acc.repeated.MPNo"), 
                store: this.paymentStore,
                displayField:'billno',
                valueField:'billid',
                mode: 'local',
                width: 200,
                name:'invoicelstno',
                hidden: !this.isaddRecurringPayment,
                hideLabel: !this.isaddRecurringPayment,
                disabled:!this.isaddRecurringPayment,
                triggerAction: 'all',
                typeAhead:true,
                selectOnFocus:true,
                allowBlank:false,
                forceSelection : true
            }),
            this.nextDate = new Wtf.form.DateField({
                fieldLabel:this.isReceipt?WtfGlobal.getLocaleText("acc.repeatedRP.nextDate"):WtfGlobal.getLocaleText("acc.repeatedMP.nextDate"),
                width:200,
                name:'startDate',
                readOnly:true,
                value: this.nextDateValue,
                format: "Y-m-d"
            }),
            this.dueDate = new Wtf.form.TextField({
                fieldLabel:this.isReceipt?WtfGlobal.getLocaleText("acc.repeatedRP.nextDueDate"):WtfGlobal.getLocaleText("acc.repeatedMP.nextDueDate"),
                width:200,
                maxLength:50,
                allowBlank: false,
                value: this.dueDateValue,
                disabled: true,
                readOnly: true,
                name:"dueDate"
            }),
            this.intervalPanel = new Wtf.Panel({
                layout: "column",
                border: false,
                items:[
                new Wtf.Panel({
                    columnWidth: 0.55,
                    layout: "form",
                    border: false,
                    anchor:'100%',
                    items : this.interval = new Wtf.form.NumberField({
                        fieldLabel:this.isReceipt?WtfGlobal.getLocaleText("acc.repeatedRP.repRP"):WtfGlobal.getLocaleText("acc.repeatedMP.repMP"),
                        width: 50,
                        allowBlank: false,
                        minValue: 1,
                        maxValue: 999,
                        allowNegative: false,
                        maxLength: 50,
                        name: "interval"
                    })
                }),
                new Wtf.Panel({
                    columnWidth: 0.3,
                    layout: "form",
                    border: false,
                    anchor:'100%',
                    items : this.intervalType = new Wtf.form.ComboBox({
                        store: Wtf.intervalTypeStore,
                        hiddenName:'intervalType',
                        displayField:'name',
                        valueField:'id',
                        mode: 'local',
                        value: "day",
                        triggerAction: 'all',
                        typeAhead:true,
                        hideLabel: true,
                        labelWidth: 5,
                        width: 125,
                        selectOnFocus:true
                    })
                })
                        
                ]
            }),
            this.NoOfJEPanel = new Wtf.Panel({
                layout: "column",
                border: false,
                items:[
                new Wtf.Panel({
                    columnWidth: 0.55,
                    layout: "form",
                    border: false,
                    anchor:'100%',
                    items : this.NoOfPaymentpost = new Wtf.form.NumberField({
                        fieldLabel:this.isReceipt?WtfGlobal.getLocaleText("acc.repeatedRP.RPPost"):WtfGlobal.getLocaleText("acc.repeatedMP.MPPost"),
                        width: 50,
                        allowBlank: false,
                        minValue: 1,
                        maxValue: 999,
                        allowNegative: false,
                        maxLength: 3, 
                        name:"NoOfPaymentpost"
                    })
                }),
                new Wtf.Panel({
                    columnWidth: 0.3,
                    layout: "form",
                    border: false,
                    anchor:'100%',
                    items : this.expireDate = new Wtf.form.DateField({
                        name:'expireDate',
                        format: "Y-m-d",
                        typeAhead:true,
                        //                                    disabled: true,
                        hideLabel: true,
                        labelWidth: 5,
                        width: 125
                    })
                })
                ]
            }),
                
            this.notifyRecurring = new Wtf.form.FieldSet({
                title:WtfGlobal.getLocaleText("acc.notify.mode"),
                id:this.id+'notifyme',
                //bodyStyle:'padding:5px',
                autoHeight : true,
                //autoWidth : true,
                width: 555,
                items:[
                this.notify0 = new Wtf.form.Radio({
                    hideLabel:true,
                    checked: true,//(this.paymentRec!=undefined && this.paymentRec.data.ispendingapproval!=undefined)?(this.paymentRec!=undefined && this.paymentRec.data.ispendingapproval?false:true):true,
                    fieldLabel: '',
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.recurring.autoentry"),  //Auto Entry
                    name: 'notifyme',
                    inputValue: 0
                }),
                this.notify1 = new Wtf.form.Radio({
                    //ctCls:"fieldset-item",                    
                    hideLabel:true,
                    fieldLabel: '',
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.field.Notifyme"),  //Remind me for confirmation
                    name: 'notifyme',
                    inputValue: 1
                }),
                this.approval=new Wtf.form.ComboBox({
                    fieldLabel:WtfGlobal.getLocaleText("acc.field.Approvers"), //Approver(s) 
                    store:Wtf.userds,
                    name:"approver",
                    displayField:'fname',
                    valueField:'userid',
                    forceSelection: true,
                    selectOnFocus:true,
                    triggerAction: 'all',
                    editable:false,
                    mode: 'local',
                    width: 193,
                    allowBlank:true
                    //hidden:true,
                    //hideLabel:true
                })
                ]
            }),
            
            this.chequeOption = new Wtf.form.FieldSet({
                title:WtfGlobal.getLocaleText("acc.recurringMP.chequeNumberSetting"),
                id:this.id+'chequeOption',                
                autoHeight : true,   
                hidden:!(this.paymentRec && this.paymentRec.data.detailtype==this.paymentMethodType.Bank),
                width: 555,
                items:[
                this.autoNumber = new Wtf.form.Radio({
                    hideLabel:true,
                    checked: this.isEdit?this.paymentRec.data.chequeOption:true,
                    fieldLabel: '',
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.field.autogeneratedchequenosetting"),  
                    name: 'chequeOption',
                    inputValue: 0
                }),
                this.manualNumber = new Wtf.form.Radio({                    
                    hideLabel:true,
                    fieldLabel: '',
                    labelSeparator: '',
                    checked:this.isEdit?!this.paymentRec.data.chequeOption:false,
                    boxLabel: WtfGlobal.getLocaleText("acc.recurringMP.manualChequeNumber"),  
                    name: 'chequeOption',
                    inputValue: 1
                }),
                ]
            }),
                
            this.GridPanel = new Wtf.Panel({
                layout: "fit",
                border: true,
                autoHeight:true,                    
                width: 555,                        
                items:[this.grid1]
            }),
                
            this.GridPanel1 = new Wtf.Panel({
                layout: "fit",
                border: true,
                autoHeight:true, 
                hidden:!(this.paymentRec && this.paymentRec.data.detailtype==this.paymentMethodType.Bank),
                width: 555,                        
                items:[this.chequeGrid]
            })
                
            ]
        });        
        this.autoNumber.on('change',this.onChequeSetting,this);
        this.manualNumber.on('change',this.onChequeSetting,this);
        this.approval.disable();
        this.notify0.on('change',this.onButtonCheck,this);
        this.notify1.on('change',this.onButtonCheck,this);
                    
        this.interval.on("change",function(df, nvalue, ovalue){
            this.endDateValue = this.calculateEndDate();
            this.expireDate.setValue(this.endDateValue);
            if(this.paymentRec && this.paymentRec.data.detailtype==this.paymentMethodType.Bank){
                this.setChequeNumbers();
            }
            if(this.paymentList && this.paymentList.getValue()!=""){
                var Rec = WtfGlobal.searchRecord(this.paymentStore,this.paymentList.getValue(),'billid');
                if(Rec.data.detailtype==this.paymentMethodType.Bank){
                    this.setChequeNumbers();
                }    
            }
             this.setMemoDetails();
        },this);
        this.nextDate.on("change",function(df, nvalue, ovalue){
            this.dueDateValue = this.calculateDueDate();
            this.dueDate.setValue(this.dueDateValue);
            this.endDateValue = this.calculateEndDate();
            this.expireDate.setValue(this.endDateValue);
            if(this.paymentRec && this.paymentRec.data.detailtype==this.paymentMethodType.Bank){
                this.setChequeNumbers();
            }
            if(this.paymentList && this.paymentList.getValue()!=""){
                var Rec = WtfGlobal.searchRecord(this.paymentStore,this.paymentList.getValue(),'billid');
                if(Rec.data.detailtype==this.paymentMethodType.Bank){
                    this.setChequeNumbers();
                }    
            }
            this.setMemoDetails();
        },this);
        this.NoOfPaymentpost.on("change",function(){
            this.endDateValue = this.calculateEndDate();
            this.expireDate.setValue(this.endDateValue);
            if( this.Memostore.getCount() < this.NoOfPaymentpost.getValue()){
                for(var i=this.Memostore.getCount()+1 ;i <=this.NoOfPaymentpost.getValue(); i++){    
                    this.addGridRec(i);
                    if(this.paymentRec && this.paymentRec.data.detailtype==this.paymentMethodType.Bank){                        
                        this.addGridRecForCheque(i);
                    }
                    if(this.paymentList && this.paymentList.getValue()!=""){
                        var Rec = WtfGlobal.searchRecord(this.paymentStore,this.paymentList.getValue(),'billid');
                        if(Rec.data.detailtype==this.paymentMethodType.Bank){
                            this.addGridRecForCheque(i);
                        }    
                    }
                }
            }else{
                var reccount=this.Memostore.getCount()-1;
                for(var j=this.NoOfPaymentpost.getValue() ;j <=reccount; j++){   
                    var rec=this.Memostore.getAt(this.NoOfPaymentpost.getValue());
                    this.Memostore.remove(rec);
                    if(this.paymentRec && this.paymentRec.data.detailtype==this.paymentMethodType.Bank){
                        rec =this.chequeStore.getAt(this.NoOfPaymentpost.getValue());
                        this.chequeStore.remove(rec);
                    }
                    if(this.paymentList && this.paymentList.getValue()!=""){
                        var Rec = WtfGlobal.searchRecord(this.paymentStore,this.paymentList.getValue(),'billid');
                        if(Rec.data.detailtype==this.paymentMethodType.Bank){
                            rec =this.chequeStore.getAt(this.NoOfPaymentpost.getValue());
                            this.chequeStore.remove(rec);
                        }    
                    }
                }
            }
            if(this.paymentRec && this.paymentRec.data.detailtype==this.paymentMethodType.Bank){
                this.setChequeNumbers();
            }
            if(this.paymentList && this.paymentList.getValue()!=""){
                var Rec = WtfGlobal.searchRecord(this.paymentStore,this.paymentList.getValue(),'billid');
                if(Rec.data.detailtype==this.paymentMethodType.Bank){
                    this.setChequeNumbers();
                }    
            }
            this.setMemoDetails();
        },this);
        this.intervalType.on("select",function(){
            this.endDateValue = this.calculateEndDate();
            this.expireDate.setValue(this.endDateValue);
            if(this.paymentRec && this.paymentRec.data.detailtype==this.paymentMethodType.Bank){
                this.setChequeNumbers();
            }
            if(this.paymentList && this.paymentList.getValue()!=""){
                var Rec = WtfGlobal.searchRecord(this.paymentStore,this.paymentList.getValue(),'billid');
                if(Rec.data.detailtype==this.paymentMethodType.Bank){
                    this.setChequeNumbers();
                }    
            }
            this.setMemoDetails();
        },this);
        this.paymentList.on("select",function(){
            var idx = this.paymentStore.find('billid',this.paymentList.getValue());
            if(idx!=-1){
                this.record = this.paymentStore.getAt(idx);
            }
            if(this.record.data.repeateid){ //Update
                this.nextDateValue = this.record.data.nextDate;
                this.dueDateValue = this.calculateDueDate();
                this.dueDate.setValue(this.dueDateValue);
                this.repeateId.setValue(this.record.data.repeateid);
                this.nextDate.setValue(this.nextDateValue);
                this.dueDate.setValue(this.dueDateValue);
                this.interval.setValue(this.record.data.interval);
                this.intervalType.setValue(this.record.data.intervalType);
                this.expireDate.setValue(this.record.data.expireDate);
                this.NoOfPaymentpost.setValue(this.paymentRec.data.NoOfPaymentpost);
                //  this.record.data.ispendingapproval?this.notify1.setValue(true):this.notify0.setValue(true);
                if(this.record.data.ispendingapproval){
                    this.approval.enable();
                    var pos=Wtf.userds.find("fname",this.record.data.approver)
                    if(pos!=-1)
                        this.approval.setValue(Wtf.userds.getAt(pos).data.userid);
                }
            }
            this.companyId = this.record.data.companyid;
            this.paymentMethodAccountId = this.record.data.paymentaccountid;
            if(this.record && this.record.data.detailtype==this.paymentMethodType.Bank){
                this.GridPanel1.show();
                this.chequeOption.show();
                this.doLayout();
            } else {
                this.GridPanel1.hide();
                this.chequeOption.hide();
                this.doLayout();
            }
        },this);

        if(this.paymentRec!=undefined){
            if(this.paymentRec.data.repeateid){ //Update
                this.nextDateValue = this.paymentRec.data.nextDate;
                this.dueDateValue = this.calculateDueDate();
                this.dueDate.setValue(this.dueDateValue);
                this.repeateId.setValue(this.paymentRec.data.repeateid);
                this.nextDate.setValue(this.nextDateValue);
                this.dueDate.setValue(this.dueDateValue);
                this.interval.setValue((this.paymentRec.data.interval=="" || this.paymentRec.data.interval==undefined)?1:this.paymentRec.data.interval);
                this.intervalType.setValue(this.paymentRec.data.intervalType);
                this.expireDate.setValue(this.paymentRec.data.expireDate);
                this.NoOfPaymentpost.setValue((this.paymentRec.data.NoOfPaymentpost=='' || this.paymentRec.data.NoOfPaymentpost==undefined)?1:this.paymentRec.data.NoOfPaymentpost);
                // this.paymentRec.data.ispendingapproval?this.notify1.setValue(true):this.notify0.setValue(true);
                if(this.paymentRec.data.ispendingapproval){
                    this.approval.enable();
                    var pos=Wtf.userds.find("fname",this.paymentRec.data.approver)
                    if(pos!=-1)
                        this.approval.setValue(Wtf.userds.getAt(pos).data.userid);
                }
                if(this.paymentRec.data.detailtype==this.paymentMethodType.Bank){
                    var indexId=this.chequeGrid.getColumnModel().getIndexById('2');
                    var chequeOption=this.paymentRec.data.chequeOption;
                    this.chequeGrid.getColumnModel().setHidden(indexId,chequeOption);
                }   
                this.setMemoDetails();
            } else {
                this.dueDateValue = this.calculateDueDate();
                this.dueDate.setValue(this.dueDateValue);
                this.repeateId.setValue(this.paymentRec.data.repeateid);
                this.nextDate.setValue(this.nextDateValue);
                this.dueDate.setValue(this.dueDateValue);
                this.interval.setValue((this.paymentRec.data.interval=="" || this.paymentRec.data.interval==undefined)?1:this.paymentRec.data.interval);
                this.intervalType.setValue(this.paymentRec.data.intervalType =="" || this.paymentRec.data.intervalType ==undefined ?"day":this.paymentRec.data.intervalType);
                this.expDateValue = this.calculateEndDate();
                this.expireDate.setValue(this.expDateValue);
                this.NoOfPaymentpost.setValue((this.paymentRec.data.NoOfPaymentpost=='' || this.paymentRec.data.NoOfPaymentpost==undefined)?1:this.paymentRec.data.NoOfPaymentpost);
                if( this.Memostore.getCount() < this.NoOfPaymentpost.getValue()){
                    for(var i=this.Memostore.getCount()+1 ;i <=this.NoOfPaymentpost.getValue(); i++){    
                        this.addGridRec(i);
                    }
                }else{
                    var reccount=this.Memostore.getCount()-1;
                    for(var j=this.NoOfPaymentpost.getValue() ;j <=reccount; j++){   
                        var rec=this.Memostore.getAt(this.NoOfPaymentpost.getValue());
                        this.Memostore.remove(rec);
                    }
                }
                if(this.paymentRec.data.detailtype==this.paymentMethodType.Bank){
                    if( this.chequeStore.getCount() < this.NoOfPaymentpost.getValue()){
                        for(var k=this.chequeStore.getCount()+1 ;k <=this.NoOfPaymentpost.getValue(); k++){    
                            this.addGridRecForCheque(k);
                        }
                    }else{
                        var account=this.chequeStore.getCount()-1;
                        for(var l=this.NoOfPaymentpost.getValue() ;l <=account; l++){   
                            var record=this.chequeStore.getAt(this.NoOfPaymentpost.getValue());
                            this.chequeStore.remove(record);
                        }
                    }
                    this.setChequeNumbers();
                }
                this.setMemoDetails();
            }
        }
        
        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml(this.isReceipt?WtfGlobal.getLocaleText("acc.repeatedRP.recRP"):WtfGlobal.getLocaleText("acc.repeatedMP.recMP"),this.isReceipt?WtfGlobal.getLocaleText("acc.repeatedRP.recRPInfo"):WtfGlobal.getLocaleText("acc.repeatedMP.recMPInfo"),"../../images/accounting_image/Chart-of-Accounts.gif", false)
        },new Wtf.Panel({
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            bodyStyle: 'border-bottom:1px solid #bfbfbf;padding:15px 0px 15px 15px',
            items: this.repeateForm
        }));
    },
    
    onButtonCheck:function(radio, value){
        if(radio.inputValue==0){
            this.approval.disable();
        } else if(radio.inputValue==1){ 
            Wtf.userds.load();
            this.approval.enable();
        }        
    },
    calculateDueDate: function() {
        var stdate = this.nextDate==undefined ? new Date(this.startDateValue): new Date(this.nextDate.getValue());
        stdate.setDate(stdate.getDate()+this.creditTermDays);
        return stdate.format("Y-m-d");
    },
    
    getRecords:function(){
        var data=[];
        for(var i=0;i<this.Memostore.getCount();i++){
            var rec=this.Memostore.getAt(i);
            data.push('{no:"'+rec.data['no']+'",memo:"'+rec.data['memo']+'"}');
        }
        return data;
    },
    getChequeDetails:function(){
        var data=[];
        for(var i=0;i<this.chequeStore.getCount();i++){
            var rec=this.chequeStore.getAt(i);
            data.push('{no:"'+rec.data['no']+'",date:"'+rec.data['chequedate'].format('Y-m-d')+'",chequeno:"'+rec.data['chequeno']+'"}');
        }
        return data;
    },
    calculateEndDate: function() {
        var noOfdays=1;
        var interval=1;       
        
        var currentDate = new Date();   
        var browserTimezoneOffset = currentDate.getTimezoneOffset();    
        var enddate = this.nextDate==undefined ? new Date(this.startDateValue): new Date(this.nextDate.getValue());
        enddate.setDate(enddate.getDate()+noOfdays);
        
        if(this.intervalType.getValue()=="month" && this.NoOfPaymentpost.getValue()!="" && this.interval.getValue()!=""){
            var monthsadd = (this.NoOfPaymentpost.getValue()-1) * this.interval.getValue();    
            enddate = new Date((enddate.setMonth(enddate.getMonth()+monthsadd))+browserTimezoneOffset); 
        }else{
            if(this.intervalType.getValue()=="week" && this.NoOfPaymentpost.getValue()!="" && this.interval.getValue()!="")
                noOfdays = (this.NoOfPaymentpost.getValue()-1) * 7 * this.interval.getValue();   
            if(this.intervalType.getValue()=="day" && this.NoOfPaymentpost.getValue()!="" && this.interval.getValue()!="")
                noOfdays = (this.NoOfPaymentpost.getValue()-1) * this.interval.getValue();
              
            enddate = new Date((enddate.setDate(enddate.getDate()+noOfdays))+browserTimezoneOffset);    
        }
        return enddate.format("Y-m-d");
    },
    
    closeWin:function(){
        this.fireEvent('cancel',this)
        this.close();
    },
     
    addGridRec:function(recno){ 
        var rec= this.accRec;
        rec = new rec({});
        rec.beginEdit();
        var fields=this.Memostore.fields;
        rec.set(fields.get('memo').name, "");
        rec.set(fields.get('no').name,recno);
        rec.endEdit();
        rec.commit();
        this.Memostore.add(rec);
    },
    addGridRecForCheque:function(recno){
        var rec= this.accRec;
        rec = new rec({});
        rec.beginEdit();
        var fields=this.chequeStore.fields;
        rec.set(fields.get('chequeno').name, "");
        rec.set(fields.get('no').name,recno);
        rec.set(fields.get('chequedate').name,"");
        rec.endEdit();
        rec.commit();
        this.chequeStore.add(rec);
    },
    saveData:function(dontCheckForDuplicate){
        if(this.autoNumber && this.autoNumber.getValue()){
            this.saveDataFinally();
        } else {
            if(this.record && this.record.data.detailtype==this.paymentMethodType.Bank && Wtf.account.companyAccountPref.chequeNoDuplicate!=Wtf.ChequeNoIngore &&!dontCheckForDuplicate){
                this.checkForDuplicateChequeNumbers();
            } else if(this.paymentRec && this.paymentRec.data.detailtype==this.paymentMethodType.Bank && Wtf.account.companyAccountPref.chequeNoDuplicate!=Wtf.ChequeNoIngore && !dontCheckForDuplicate){
                this.checkForDuplicateChequeNumbers();
            } else {
                this.checkForBlankChequeNumbers();
            }
        }
    },

    checkForBlankChequeNumbers : function(){
        var checkforBlankChequeNumbers = this.getBlankChequeNumbers();
        if(checkforBlankChequeNumbers != ''){ 
            Wtf.Msg.show({
                    title:WtfGlobal.getLocaleText("acc.common.confirm"),
                    msg: WtfGlobal.getLocaleText("acc.recurringMP.chequeNumbersAt")+" "+ checkforBlankChequeNumbers+" "+WtfGlobal.getLocaleText("acc.recurringMP.chequeNumbersAreBlankDoYouWantToContinue"),
                    buttons: Wtf.Msg.YESNO,
                    scope:this,
                    width:600,
                    fn: function(btn){
                        if(btn=="yes"){
                            this.saveDataFinally();
                        } else{
                            return;
                        }
                    },
                    icon: Wtf.MessageBox.QUESTION
                });
        } else {
            this.saveDataFinally();
        }
    },
    
    saveDataFinally: function(){
        
        var isaddRecurringJE = this.isaddRecurringPayment;
        var valid = this.repeateForm.getForm().isValid();
        var minNextDate = this.startDateValue<this.nextDateValue ? this.startDateValue : this.nextDateValue;
        if(this.nextDate.getValue()<minNextDate){
            var minDate = new Date(minNextDate);
            minDate.setDate(minDate.getDate()-1);
            this.nextDate.markInvalid(WtfGlobal.getLocaleText("acc.repeated.msg")+minDate.format("Y-m-d"));    //"Please select 'Next date' greater than "
            valid = false;
        }

        if(this.expireDate.getValue()!="" && this.expireDate.getValue()<this.nextDate.getValue()){
            this.expireDate.markInvalid(WtfGlobal.getLocaleText("acc.repeated.msg1"));    // "'End date' should be greater than 'Next date'"
            valid = false;
        }
       
        if(!valid){
            WtfComMsgBox(2,2);
            return;
        }
        var rec=[];
        rec = this.repeateForm.getForm().getValues();
       
        if(this.isaddRecurringPayment) {
            rec.MPId = this.paymentList.getValue();            
            rec.entryno=rec.invoicelstno;
        }else {
            rec.isCustBill =  this.isCustBill;
            rec.MPId = this.paymentRec.data.billid;
            rec.entryno=this.paymentRec.data.billno;
        }
        rec.detail="["+this.getRecords().join(",")+"]";  
        rec.chequedetail = "["+this.getChequeDetails().join(",")+"]";  
        Wtf.Ajax.requestEx({
            url:"ACCVendorPaymentNew/saveRepeatePaymentInfo.do",
            params: rec
        },this,this.genSuccessResponse,this.genFailureResponse);
    },
    
    genSuccessResponse:function(response){
        /*
      * Used Wtf.MessageBox.Show ,because after showing success message we want to refresh report grid.
      * While using WtfComMsgBox, messages does not stay on screen because while refreshing the report, scope get changed.
      * Therefore we have used the following component for sowing the alert.
      */
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.success"),
            msg: response.msg,
            width:370,
            scope: {
                scopeObj:this  
            },
            buttons: Wtf.MessageBox.OK,
            fn: function(btn ,text, option) {
                var comp = null;
                var comp1=null;
                if(this.isReceipt){
                    comp = Wtf.getCmp('receiptReport');
                    comp1 = Wtf.getCmp('RepeatReceiptList');
                }else {
                    comp = Wtf.getCmp('paymentReport') ;
                    comp1= Wtf.getCmp('RepeatPaymentList');
                }
                if(comp){
                    comp.fireEvent('paymentupdate');
                }
                if(comp1){
                    comp1.grid.store.reload();
                } 
            },
            animEl: 'mb9',
            icon: Wtf.MessageBox.INFO
        });    
        this.close();
    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
        
    setMemoDetails:function(){
        var interval=this.interval.getValue();
        var store = this.Memostore;        
        var browserTimezoneOffset = 0;//currentDate.getTimezoneOffset();    
        var nextDate=new Date(this.nextDate.getValue());
        var rec;
        var moduleName = "Make Payment as of ";
        var dateToSet=new Date();
        for(var i=0;i<store.getCount();i++){            
            rec=store.getAt(i); 
            if(i==0){
                var date = new Date(this.nextDate.getValue());
                date = date.format("Y-m-d");
                rec.set('memo',moduleName+date);
                continue;
            }          
            if(this.intervalType.getValue()=="month"){
                dateToSet = new Date(nextDate.setMonth(nextDate.getMonth()+(interval))+browserTimezoneOffset);
                dateToSet = dateToSet.format("Y-m-d");
                rec.set('memo',moduleName+dateToSet);
            } else {
                if(this.intervalType.getValue()=="week" ){
                    dateToSet = new Date(nextDate.setDate(nextDate.getDate()+(interval*7))+browserTimezoneOffset);
                    dateToSet = dateToSet.format("Y-m-d");
                    rec.set('memo',moduleName+dateToSet);
                }
                if(this.intervalType.getValue()=="day" ){
                    dateToSet = new Date(nextDate.setDate(nextDate.getDate()+(interval))+browserTimezoneOffset);
                    dateToSet = dateToSet.format("Y-m-d");
                    rec.set('memo',moduleName+dateToSet);
                }
            }
        }
    },
    
    setChequeNumbers:function(){
        var interval=this.interval.getValue();
        var store = this.chequeStore;        
        var browserTimezoneOffset = 0;//currentDate.getTimezoneOffset();    
        var nextDate=new Date(this.nextDate.getValue());
        var rec;
        var dateToSet=new Date();
        for(var i=0;i<store.getCount();i++){            
            rec=store.getAt(i); 
            if(i==0){
                rec.set('chequedate',new Date(this.nextDate.getValue()));
                continue;
            }          
            if(this.intervalType.getValue()=="month"){
                dateToSet = new Date(nextDate.setMonth(nextDate.getMonth()+(interval))+browserTimezoneOffset );
                rec.set('chequedate',dateToSet);
            } else {
                if(this.intervalType.getValue()=="week" ){
                    dateToSet = new Date(nextDate.setDate(nextDate.getDate()+(interval*7))+browserTimezoneOffset);
                    rec.set('chequedate',dateToSet);
                }
                if(this.intervalType.getValue()=="day" ){
                    dateToSet = new Date(nextDate.setDate(nextDate.getDate()+(interval))+browserTimezoneOffset);
                    rec.set('chequedate',dateToSet);
                }
            }
        }
    },
    onChequeSetting:function(radio,value){
        var id = this.chequeGrid.getId();
        var indexId=this.chequeGrid.getColumnModel().getIndexById('2');
        if(radio.inputValue==0){            
            this.chequeGrid.getColumnModel().setHidden(indexId,true);
        } else if(radio.inputValue==1){ 
            this.chequeGrid.getColumnModel().setColumnWidth(indexId,160);
            this.chequeGrid.getColumnModel().setHidden(indexId,false);
        } 
        this.doLayout();
    },
    checkForDuplicateChequeNumbers:function(){
        var chequeNumbers='';
        for(var x=0;x<this.chequeStore.getCount();x++){
            var chequeRec = this.chequeStore.getAt(x);
            var nextChequeNumber=chequeRec.data.chequeno;
            if(nextChequeNumber==undefined || nextChequeNumber==''){
                continue;
            }
            chequeNumbers+=nextChequeNumber+',';
        }
        if(chequeNumbers!=''){
            chequeNumbers = chequeNumbers.substring(0,chequeNumbers.length-1)
            
            Wtf.Ajax.requestEx({
            url:"ACCVendorPaymentNew/checkIfChequeNumberExists.do",
            params: {
                chequeNumbers:chequeNumbers,
                companyId:this.companyId,
                bankAccountId: this.paymentMethodAccountId
            }
        },this,function(response){
            if(response.isAtleastOneChequeNumberExists){
                var duplicateNumbers=response.duplicateChequeNumbers;
                if(Wtf.account.companyAccountPref.chequeNoDuplicate==Wtf.ChequeNoBlock){      
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.recurringMP.chequeNumbers")+" "+duplicateNumbers+" "+WtfGlobal.getLocaleText("acc.recurringMP.AlreadyExists")],2);
                    return;
                } else if(Wtf.account.companyAccountPref.chequeNoDuplicate==Wtf.ChequeNoWarn){
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.warning"), // 'Warning',
                        msg: WtfGlobal.getLocaleText("acc.recurringMP.chequeNumbers")+' '+'<b>'+duplicateNumbers+'</b>'+' '+WtfGlobal.getLocaleText("acc.recurringMP.alreadyExists")+' '+WtfGlobal.getLocaleText("acc.recurringMP.doYouWantToContinue"),
                        buttons: Wtf.MessageBox.YESNO,
                        fn: function(btn) {
                            if(btn =="yes") {
                                this.saveData(true);
                            }
                        },
                        scope: this,
                        icon: Wtf.MessageBox.QUESTION
                    });    
                } else {
                    this.saveData(true);
                }   
            } else{
                this.checkForReservedChequeNumbers();
            }
        },this.genFailureResponse);
   
        } else {
            this.checkForBlankChequeNumbers();
        }   
    },
    
    afterChequeGridEdit:function(obj){
        if(obj.field=='chequeno'){
            var val = obj.value;
            var arrayOfRecordsWithSameChequeNumbers = [];
            arrayOfRecordsWithSameChequeNumbers=WtfGlobal.queryBy(this.chequeStore, 'chequeno', val);
            if(arrayOfRecordsWithSameChequeNumbers.length>1){
                if(Wtf.account.companyAccountPref.chequeNoDuplicate==Wtf.ChequeNoBlock){
                    obj.record.set('chequeno','');
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.recurringpayment.chequeNoCanNotBeDuplicate")],2);    
                } else if(Wtf.account.companyAccountPref.chequeNoDuplicate==Wtf.ChequeNoWarn){
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.warning"), // 'Warning',
                        msg: WtfGlobal.getLocaleText("acc.field.ChequeNumber")+' '+'<b>'+val+'</b>'+' '+WtfGlobal.getLocaleText("acc.recurringMP.chequeNoAlreadyEntered")+' '+WtfGlobal.getLocaleText("acc.ven.msg4"),
                        buttons: Wtf.MessageBox.YESNO,
                        fn: function(btn) {
                            if(btn =="no") {
                                obj.record.set('chequeno','');
                            }
                        },
                        scope: this,
                        icon: Wtf.MessageBox.QUESTION
                    });    
                }
                
            }
        }
    },
    
    getBlankChequeNumbers: function(){
      var recordsWithBlankChequeNumbers= '';
      for(var i=0;i<this.chequeStore.getCount();i++){
          var rec = this.chequeStore.getAt(i);
          if(rec.data['chequeno'] == '' || rec.data['chequeno']==null){
              recordsWithBlankChequeNumbers+=(i+1)+",";
          }
      }
      if(recordsWithBlankChequeNumbers!=''){
            recordsWithBlankChequeNumbers = recordsWithBlankChequeNumbers.substring(0, recordsWithBlankChequeNumbers.length-1);
      }
      return recordsWithBlankChequeNumbers
    },
    
    checkForReservedChequeNumbers: function(){
        var chequeNumbers='';
        for(var x=0;x<this.chequeStore.getCount();x++){
            var chequeRec = this.chequeStore.getAt(x);
            var nextChequeNumber=chequeRec.data.chequeno;
            if(nextChequeNumber==undefined || nextChequeNumber==''){
                continue;
            }
            chequeNumbers+=nextChequeNumber+',';
        }
        
        if(chequeNumbers!=''){
            chequeNumbers = chequeNumbers.substring(0,chequeNumbers.length-1);
            Wtf.Ajax.requestEx({
                url:"ACCVendorPaymentNew/checkIfChequeNumberReservedForRecurringPayment.do",
                params: {
                    chequenumber:chequeNumbers,
                    companyId:this.companyId,
                    bankAccountId: this.paymentMethodAccountId,
                    repeatedPaymentId : (this.paymentRec)?this.paymentRec.data.repeateid:''
                }
            },this,function(response){
                if(response.success){
                    if(response.data && response.data.length>0){
                        
                        var data=[];
                        data = response.data;
                        var reservedChequeNumbers='';
                        for(var x=0;x<data.length;x++){
                            var chequeNumber = data[x].chequeNumber;
                            var paymentNumber = data[x].paymentNumber;
                            reservedChequeNumbers += WtfGlobal.getLocaleText("acc.field.ChequeNumber")+": "+"<b>"+chequeNumber+"</b>"+" "+WtfGlobal.getLocaleText("acc.field.For")+" "+WtfGlobal.getLocaleText("acc.field.paymentNumber")+": "+"<b>"+paymentNumber+"</b>"+"<br>";
                        }
                        
                        if(reservedChequeNumbers != ''){
                            if(Wtf.account.companyAccountPref.chequeNoDuplicate==Wtf.ChequeNoBlock){
                                this.callReservedchequenumbersWindow(reservedChequeNumbers,false);
                            } else if(Wtf.account.companyAccountPref.chequeNoDuplicate==Wtf.ChequeNoWarn){
                                this.callReservedchequenumbersWindow(reservedChequeNumbers,true);
                            } else {
                                this.saveData(true);
                            }
                        }
                        
                    } else{
                        this.checkForBlankChequeNumbers();
                    }
                }
            },this.genFailureResponse);
   
        } else {
            this.checkForBlankChequeNumbers();
        }
        
    },
     callReservedchequenumbersWindow:function(reservedChequeNumbers,isWarningCase){
         reservedChequeNumbers = reservedChequeNumbers.substring(0,reservedChequeNumbers.length-1);
                            var chequepanel = {                                         
                                region: 'center',
                                border: false,
                                bodyStyle: 'background-color:#f1f1f1',
                                html:reservedChequeNumbers ,//+'<font>' ,
                                autoScroll:true,
                                height:149
                            }
                            this.reservedChequeWindow= new Wtf.Window({
                                title : WtfGlobal.getLocaleText("acc.recurringMP.chequeNoReserverdWindow"),
                                iconCls :getButtonIconCls(Wtf.etype.deskera),
                                height : 285,
                                width : 400,
                                scope:this,
                                bodyStyle: 'background-color:#f1f1f1',
                                modal : true,
                                layout : 'fit',
                                buttons:[{
                                    scope:this,
                                    text:isWarningCase?"Continue":"OK" ,
                                    handler:function(){
                                        isWarningCase?this.saveData(true):this.reservedChequeWindow.close();  
                                    }
                                },{
                                    scope:this,
                                    text:WtfGlobal.getLocaleText("acc.common.cancelBtn") ,
                                    handler:function(){
                                        this.reservedChequeWindow.close();  
                                    }
                                }],
//                                    
                                items : [{
                                    region:'north',
                                    border: false,
                                    autoHeight:true,
                                    resizable:false,
                                    bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;padding:15px 0px 15px 15px',
                                    html:isWarningCase?WtfGlobal.getLocaleText("acc.recurringMp.someChequeNumberReservedWantToContinue"):WtfGlobal.getLocaleText("acc.recurringMp.someChequeNumberReserved")
                                },{
                                    region:'center',
                                    bodyStyle: 'padding:5px;background-color:#f1f1f1',
                                    items:[chequepanel]
                                }
                                ]
                            });
                            this.reservedChequeWindow.show();
     }
});
