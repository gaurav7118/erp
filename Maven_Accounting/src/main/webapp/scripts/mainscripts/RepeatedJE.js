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
Wtf.RepeatedJEReport = function(config){
    this.isCustBill=config.isCustBill;
    this.gridConfigId="";
     this.GridRec = Wtf.data.Record.create ([
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'companyname'},
        {name:'companyid'},
        {name:'eliminateflag'},
        {name:'deleted'},
        {name:'entrydate',type:'date'},
        {name:'memo'},
        {name:'jeDetails'},
        {name:'transactionID'},
        {name:'billid'},
        {name:'noteid'},
        {name:'type'},
        {name:'transactionDetails'},
        {name:'costcenter'},
        {name:'currencyid'},
        {name:'creditDays'},
        {name:'isRepeated'},
        {name:'childCount'},
        {name:'interval'},
        {name:'NoOfJEpost'}, 
        {name:'NoOfRemainJEpost'},  
        {name:'intervalType'},
        {name:'startDate', type:'date'},
        {name:'nextDate', type:'date'},
        {name:'expireDate', type:'date'},
        {name:'repeateid'},
        {name:'isreverseje',type:'boolean'},
        {name:'reversejeno'},
        {name:'withoutinventory'},
        {name:'revaluationid'},
        {name:'externalcurrencyrate'},
        {name:'typeValue'},
        {name:'partlyJeEntryWithCnDn'},
        {name:'isactivate',type:'boolean'},
        {name:'approver'},
        {name:'ispendingapproval',type:'boolean'},
        {name: 'chequeOption'},
        {name:'pmtmethodaccountid'},
        {name:'pmtmethodtype'}
        
    ]);
    if(config.consolidateFlag) {
        this.Store = new Wtf.data.GroupingStore({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.GridRec),
            url: "ACCJournal/getJournalEntry.do",
            baseParams:{
                mode:54,
                getRepeateInvoice: true,
                costCenterId: this.costCenterId,
                withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                consolidateFlag:config.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                ispendingAproval:config.ispendingAproval,
                nondeleted:true,
                deleted:false
            },
            sortInfo : {
                field : 'companyname',
                direction : 'ASC'
            },
            groupField : 'companyname'
        });
    } else {
        this.Store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.GridRec),
            url: "ACCJournal/getJournalEntry.do",
            baseParams:{
                mode:54,
                costCenterId: this.costCenterId,
                getRepeateInvoice: true,
                withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                consolidateFlag:config.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                ispendingAproval:config.ispendingAproval,
                nondeleted:true,
                deleted:false
            }
        });
    }
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


//    this.Store.load();
        this.Store.load({params:{start:0,limit:30}});
    var btnArr=[];
    btnArr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.repeatedJE.QuickSearch"),
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
    btnArr.push(this.addRecurringInvoice=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.common.add"),  // "Add",
                tooltip :WtfGlobal.getLocaleText("acc.field.AllowsyoutoaddRecurringJE"),
                scope: this,
                iconCls : getButtonIconCls(Wtf.etype.add),
                handler : this.addRecurringInvoiceHandler
            }));
   
    btnArr.push(this.editBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.edit"),  //'Edit',
            tooltip : WtfGlobal.getLocaleText("acc.repeatedJE.EditToolTip"),
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
        handler: this.deleteRepeateJE,
        hidden: (config.consolidateFlag || this.isLeaseFixedAsset),
        disabled: true
    }));
    btnArr.push("-");
        
    btnArr.push(this.activeDeactiveBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.levelsetting.activatelevel"),  //'Activate/Deactivate',
        tooltip : WtfGlobal.getLocaleText("acc.recurringJEActivate.tooltip"),
        scope: this,
        //iconCls: getButtonIconCls(Wtf.etype.edit),
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
        hidden: config.consolidateFlag,
        iconCls :getButtonIconCls(Wtf.etype.reorderreport)
    }));
    btnArr.push(this.approveJEBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.cc.24"),
        tooltip : WtfGlobal.getLocaleText("acc.field.Approvepending.Recurring.JE"), //Issue 31009 - [Pending Approval]Window name should be "Approve Pending Invoice" instead of "Approve Pending Approval". it should also have deskera logo
        id: 'approvepending' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.add),
        disabled :true,
        isApproveBtn:true,
        hidden:config.consolidateFlag,
        handler : this.approveRecurringPendingJE
    }));
    this.approveJEBttn.hide();
    
    this.pendingApprovalBttn.on('click', function(){
        var panel = null;
        var ispendingAproval = true;
        panel = Wtf.getCmp("RecurringJournalEntryDetailsPending");
        if(panel==null){
            panel = getPendingRecurringJETab("", undefined,false,ispendingAproval);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();   
    }, this);
   
    if(config.ispendingAproval){    //Hide all buttons in Pending Approval window
        this.approveJEBttn.show();
        this.editBttn.hide();
        this.addRecurringInvoice.hide();
        this.activeDeactiveBttn.hide();
        this.pendingApprovalBttn.hide();
    }
    
    this.expander = new Wtf.grid.RowExpander({});
    //this.sm = new Wtf.grid.RowSelectionModel({singleSelect: false});    //Enable multi selection
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);
    this.gridView1 = config.consolidateFlag?new Wtf.grid.GroupingView({
            forceFit:true,
            showGroupName: true,
            enableNoGroups:false, // REQUIRED!
            hideGroupedColumn: true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }):{
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
                header:WtfGlobal.getLocaleText("acc.repeatedJE.Gridcol1"),
                dataIndex:'entryno',
                pdfwidth:75,
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
                renderer:WtfGlobal.deletedRenderer,
                dataIndex:'memo'
            },{
                header:WtfGlobal.getLocaleText("acc.invList.repeated.sched"),  //"Schedular Start Date",
                dataIndex:'startDate',
                pdfwidth:80,
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.repeatedJE.Gridcol4"),
                dataIndex:'childCount',
                align: "right",
                pdfwidth:80
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
                header:WtfGlobal.getLocaleText("acc.repeatedJE.Gridcol5"),
                dataIndex:'nextDate',
                pdfwidth:80,
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
                displayInfo: (SATSCOMPANY_ID==companyid) ? false : true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
                plugins: this.pP = new Wtf.common.pPageSize({id : "pPageSize_"+this.id})
            })
        });
    this.items=[this.grid];

    this.expandRec = Wtf.data.Record.create ([
        {name:'parentJEId'},
        {name:'JEId'},
        {name:'JENo'}
    ]);
    this.expandStoreUrl ="ACCJournal/getJERepeateDetails.do";
    this.expandStore = new Wtf.data.Store({
        url:this.expandStoreUrl,
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    
    this.expandStore.on("beforeload", function(store){
        this.expandStoreUrl = "ACCJournal/getJERepeateDetails.do";
        store.proxy.conn.url = this.expandStoreUrl;
    }, this);
    
    
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.expander.on("expand",this.onRowexpand,this);
    
    WtfGlobal.getGridConfig(this.grid,Wtf.Acc_Recurring_JE_ModuleId,false,false);
    this.grid.on('render', function() {
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    }, this);
    
    Wtf.apply(this,config);
    Wtf.RepeatedJEReport.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.RepeatedJEReport, Wtf.Panel,{

    onRowexpand:function(scope, record, body){
        this.expanderBody=body;
        this.withInvMode = record.data.withoutinventory;
        this.expandStore.load({params:{parentid:record.data['journalentryid']}});
    },
    expandRow:function(){
        Wtf.MessageBox.hide();
    },

    fillExpanderBody:function(){
        var disHtml = "";
        var header = "";

        if(this.expandStore.getCount()==0){
             var head=WtfGlobal.getLocaleText("acc.repeatedJE.RowHead1");
            header = "<span style='color:#15428B;display:block;'>"+ head +"</span>";   
        } else {
            var head=WtfGlobal.getLocaleText("acc.repeatedJE.RowHead2");
            var blkStyle = "display:block;float:left;width:150px;Height:14px;"
            header = "<span class='gridHeader'>"+head+"</span>"; 
            for(var i=0;i<this.expandStore.getCount();i++){
                var rec=this.expandStore.getAt(i);
                header += "<span style="+blkStyle+">"+
                          rec.data.JENo+"</a>"+
                        "</span>";
            }
        }
        disHtml += "<div style='width:95%;margin-left:3%;'>" + header + "<br/></div>";
        this.expanderBody.innerHTML = disHtml;
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
    addRecurringInvoiceHandler:function(){
        var formrec;
        var withoutinventory;
        var isaddRecurringJE = true;
        callRepeatedJEWindow(withoutinventory, formrec, isaddRecurringJE);
    },
    storeloaded:function(store){
        this.quickPanelSearch.StorageChanged(store);
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
//        this.approval.setValue(formrec.data.approver);
//        this.notifyRecurring.disable(); //In edit case Notification mode ll be disable
        callRepeatedJEWindow(withoutinventory, formrec, isaddRecurringJE);
    },    
    activateDeactivateRecurringInvoice:function(){
//        if(this.grid.getSelectionModel().hasSelection()==false){
//            WtfComMsgBox(34,2);
//            return;
//        }
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
            url="ACCJournalCMN/activateDeactivateJournalEntries.do";
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
            var msg=WtfGlobal.getLocaleText("acc.recurringjeUpdate.approved");
            if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.update"),msg],0);
            this.Store.load();
        }
    },
    genFailureResponse:function(response){
        this.enable();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("Failed"),msg],1);
    },

deleteRepeateJE:function(){
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
            rowObject['invoiceid'] = recData.journalentryid;
            rowObject['invoicenumber'] = recData.entryno;
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
            url="ACCJournalCMN/deleteRecurringJournalEntryRule.do";
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
    
    approveRecurringPendingJE:function(){
        var data=[];
        this.formRecord = this.grid.getSelectionModel().getSelections();
        for(var i=0;i<this.formRecord.length;i++) {
            var rec = this.formRecord[i];
            var rowObject = new Object();
            rowObject['repeatedid'] = rec.data.repeateid;
            rowObject['ispendingapproval'] = rec.data.ispendingapproval;   //If already activate then make it deactivate & vice versa            
            data.push(rowObject);
        }
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected") +" "+ WtfGlobal.getLocaleText("acc.je.tabTitle") + "?", function(btn) {
            if (btn == "yes") {
            var url = "ACCJournalCMN/activateDeactivateJournalEntries.do";
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
            var msg=WtfGlobal.getLocaleText("acc.recurringjeApproval.approved");
            if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.Approved"),msg],0);
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
        WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.Acc_Recurring_JE_ModuleId, this.gridConfigId, false);
    }
});


/*/////////////////////////////////////   FORM   ////////////////////////////////////////////////////*/

Wtf.RepeateJEForm = function(config){
    this.invoiceRec=config.invoiceRec;
    this.isaddRecurringJE=config.isaddRecurringJE;
    Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.repeatedJE.recJE"),
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.repeated.savNclose"),  //'Save and Close',
            scope: this,
            id:"savencloserecurringbtn",
            handler: this.saveData.createDelegate(this)
        },{
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            scope: this,
            handler:this.closeWin.createDelegate(this)
        }]
    },config);
    Wtf.RepeateJEForm.superclass.constructor.call(this, config);
    this.addEvents({
        'update': true,
        'cancel': true
    });
    }
Wtf.extend( Wtf.RepeateJEForm, Wtf.Window, {
    defaultCurreny:false,
    onRender: function(config){
        Wtf.RepeateJEForm.superclass.onRender.call(this, config);
        this.isEdit = false;
        if(this.invoiceRec!=undefined){
            if(this.invoiceRec.data.repeateid){
                this.isEdit = true;
            }
            if(this.invoiceRec.data.companyid){
                this.companyId = this.invoiceRec.data.companyid;
            }
            if(this.invoiceRec.data.paymentaccountid){
                this.paymentMethodAccountId = this.invoiceRec.data.paymentaccountid;
            } 
        }
               
        this.startDateValue = new Date();
        this.startDateValue = new Date(this.startDateValue.getFullYear(),this.startDateValue.getMonth(),this.startDateValue.getDate()+1);

        this.nextDateValue = this.startDateValue;
        if(this.isEdit){
            this.nextDateValue = this.invoiceRec.data.nextDate;
        }
        this.creditTermDays = 0;
        if(this.invoiceRec!=undefined){
            this.creditTermDays = this.invoiceRec.data.creditDays?this.invoiceRec.data.creditDays:0;
        }
        if(this.termdays)
            {
            this.creditTermDays=this.termdays;  
            }
        this.dueDateValue = this.calculateDueDate();

        this.InvoiceRec = Wtf.data.Record.create ([
                {name:'journalentryid'},
                {name:'entryno'},
                {name:'creditDays'},
                {name:'repeateid'},
                {name:'nextDate', type:'date'},
                {name:'interval'},
                {name:'intervalType'},
                {name:'expireDate', type:'date'},
                {name:'withoutinventory',type:'boolean'},
                {name :'chequeOption'},
                {name:'companyid'},
                {name:'pmtmethodaccountid'},
                {name:'pmtmethodtype'},
                {name:'typeValue'},
            ]);

        this.JEStoreUrl = (SATSCOMPANY_ID==companyid) ? "ACCReports/getJournalEntry.do" : "ACCJournal/getJournalEntry.do";
        this.JEStore = new Wtf.data.Store({
            url:this.JEStoreUrl,
            baseParams: {
                        mode:54,
                        costCenterId: this.costCenterId,
                        getRepeateInvoice: false,
                        getPendingJEFlag: false,      //This flag is used to get only approved Journal Entry Number in Recurring Journal Entry Tab Add Button for set recurring JE 
                        isTemplate:true,
                        withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                        consolidateFlag:config.consolidateFlag,
                        companyids:companyids,
                        gcurrencyid:gcurrencyid,
                        userid:loginid
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.InvoiceRec)
        });
        if(this.isaddRecurringJE) {
            this.JEStore.baseParams.isTemplate=false;
            this.JEStore.baseParams.typeValue='1,3';
            this.JEStore.load();
        }
   this.MemoCM= new Wtf.grid.ColumnModel([{
            header:WtfGlobal.getLocaleText("acc.repeatedJE.MemoGridcol1"),
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
        if(this.invoiceRec != undefined){
            if(this.invoiceRec.data.repeateid){
                 this.Memostore.load({
                     params:{
                             memofor:"RepeatedJEID.id", 
                             repeateid:this.invoiceRec.data.repeateid
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
            header:WtfGlobal.getLocaleText("acc.dimension.module.8"),
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
            url : 'ACCJournal/getChequeDetailsForRepeatedJE.do'
        });
        this.chequeGrid = new Wtf.grid.EditorGridPanel({
            plugins:this.gridPlugins,
            clicksToEdit:1,
            height:120,
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
        
        if(this.invoiceRec != undefined){
            if(this.invoiceRec.data.repeateid){
                this.chequeStore.load({
                    params:{                             
                        repeateid:this.invoiceRec.data.repeateid
                    }
                });
            }
        }
        this.chequeOption = new Wtf.form.FieldSet({
                title:WtfGlobal.getLocaleText("acc.recurringMP.chequeNumberSetting"),
                id:this.id+'chequeOption',                
                autoHeight : true,   
                hidden:!(this.invoiceRec && this.invoiceRec.data.typeValue == Wtf.fund_transafer_journal_entry && this.invoiceRec.data.pmtmethodtype==Wtf.bank_detail_type),
                width: 555,
                items:[
                this.autoNumber = new Wtf.form.Radio({
                    hideLabel:true,
                    checked: this.isEdit?this.invoiceRec.data.chequeOption:true,
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
                    checked:this.isEdit?!this.invoiceRec.data.chequeOption:false,
                    boxLabel: WtfGlobal.getLocaleText("acc.recurringMP.manualChequeNumber"),  
                    name: 'chequeOption',
                    inputValue: 1
                }),
                ]
            }),
       this.repeateForm = new Wtf.form.FormPanel({
            border: false,
            labelWidth:230,
            autoScroll : true,
            items : [
                this.repeateId = new Wtf.form.Hidden({
                    hidden:true,
                    name:"repeateid"
                }),
                this.invoiceList = new Wtf.form.ComboBox({
                    fieldLabel:WtfGlobal.getLocaleText("acc.repeated.JENo"), 
                    store: this.JEStore,
                    displayField:'entryno',
                    valueField:'journalentryid',
                    mode: 'local',
                    width: 200,
                    name:'invoicelstno',
                    hidden: !this.isaddRecurringJE,
                    hideLabel: !this.isaddRecurringJE,
                    disabled:!this.isaddRecurringJE,
                    triggerAction: 'all',
                    typeAhead:true,
                    selectOnFocus:true,
                    allowBlank:false,
                    forceSelection : true
                }),
                this.nextDate = new Wtf.form.DateField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.repeatedJE.nextDate"),
                    width:200,
                    name:'startDate',
                    readOnly:true,
                    value: this.nextDateValue,
                    format: "Y-m-d"
                }),
                this.dueDate = new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.repeatedJE.nextDueDate"),
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
                            columnWidth: 0.54,
                            layout: "form",
                            border: false,
                            anchor:'100%',
                            items : this.interval = new Wtf.form.NumberField({
                                    fieldLabel:WtfGlobal.getLocaleText("acc.repeatedJE.repJE"),
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
                                        width: 128,
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
                            columnWidth: 0.54,
                            layout: "form",
                            border: false,
                            anchor:'100%',
                            items : this.NoOfJEpost = new Wtf.form.NumberField({
                                    fieldLabel:WtfGlobal.getLocaleText("acc.repeatedJE.JEPost"),
                                    width: 50,
                                    allowBlank: false,
                                    minValue: 1,
                                    maxValue: 999,
                                    allowNegative: false,
                                    maxLength: 3, //ERP-10073 : If max value is 999 then max length should be 3 else end user ll cross the max value limit which ll lead to form disable problem
                                    name:"NoOfJEpost"
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
                                    width: 128
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
                    checked: (this.invoiceRec!=undefined && this.invoiceRec.data.ispendingapproval!=undefined)?(this.invoiceRec!=undefined && this.invoiceRec.data.ispendingapproval?false:true):true,
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
                    //autoWidth:true,
                    allowBlank:true
                })
                ]
            }),
                this.chequeOption,
                this.GridPanel = new Wtf.Panel({
                    layout: "fit",
                    border: true,
                    autoHeight:true,
                    //height:200, //ERP-9625
                    width: 555,         //ERP-10072
                   // autoScroll:true,  //ERP-10072
                    items:[this.grid1]
                }),
                
                this.GridPanel1 = new Wtf.Panel({
                layout: "fit",
                border: true,
                autoHeight:true, 
                hidden:!(this.invoiceRec && this.invoiceRec.data.typeValue == Wtf.fund_transafer_journal_entry && this.invoiceRec.data.pmtmethodtype==Wtf.bank_detail_type),
                width: 555,                        
                items:[this.chequeGrid]
            })
                
            ]
        });
        this.approval.disable();
        this.notify0.on('change',this.onButtonCheck,this);
        this.notify1.on('change',this.onButtonCheck,this);
        this.autoNumber.on('change',this.onChequeSetting,this);
        this.manualNumber.on('change',this.onChequeSetting,this);
        this.interval.on("change",function(df, nvalue, ovalue){
            this.endDateValue = this.calculateEndDate();
            this.expireDate.setValue(this.endDateValue);
            if(this.invoiceRec && this.invoiceRec.data.typeValue == Wtf.fund_transafer_journal_entry && this.invoiceRec.data.pmtmethodtype==Wtf.bank_detail_type){
                this.setChequeNumbers();
            }
            if(this.invoiceList && this.invoiceList.getValue()!=""){
                var Rec = WtfGlobal.searchRecord(this.JEStore,this.invoiceList.getValue(),'journalentryid');
                if(Rec.data.typeValue == Wtf.fund_transafer_journal_entry && Rec.data.pmtmethodtype==Wtf.bank_detail_type){
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
            if(this.invoiceRec && this.invoiceRec.data.typeValue == Wtf.fund_transafer_journal_entry && this.invoiceRec.data.pmtmethodtype==Wtf.bank_detail_type){
                this.setChequeNumbers();
            }
            if(this.invoiceList && this.invoiceList.getValue()!=""){
                var Rec = WtfGlobal.searchRecord(this.JEStore,this.invoiceList.getValue(),'journalentryid');
                if(Rec.data.typeValue == Wtf.fund_transafer_journal_entry && Rec.data.pmtmethodtype==Wtf.bank_detail_type){
                    this.setChequeNumbers();
                }    
            }
            this.setMemoDetails();
        },this);
        this.NoOfJEpost.on("change",function(){
            this.endDateValue = this.calculateEndDate();
            this.expireDate.setValue(this.endDateValue);
            if( this.Memostore.getCount() < this.NoOfJEpost.getValue()){
                for(var i=this.Memostore.getCount()+1 ;i <=this.NoOfJEpost.getValue(); i++){    
                    this.addGridRec(i);
                    if(this.invoiceRec && this.invoiceRec.data.typeValue == Wtf.fund_transafer_journal_entry && this.invoiceRec.data.pmtmethodtype==Wtf.bank_detail_type){                        
                        this.addGridRecForCheque(i);
                    }
                    if(this.invoiceList && this.invoiceList.getValue()!=""){
                        var Rec = WtfGlobal.searchRecord(this.JEStore,this.invoiceList.getValue(),'journalentryid');
                        if(Rec.data.typeValue == Wtf.fund_transafer_journal_entry && Rec.data.pmtmethodtype==Wtf.bank_detail_type){
                            this.addGridRecForCheque(i);
                        }    
                    }
                }
            }else{
                  var reccount=this.Memostore.getCount()-1;
                  for(var j=this.NoOfJEpost.getValue() ;j <=reccount; j++){   
                      var rec=this.Memostore.getAt(this.NoOfJEpost.getValue());
                      this.Memostore.remove(rec);
                      if(this.invoiceRec && this.invoiceRec.data.typeValue == Wtf.fund_transafer_journal_entry && this.invoiceRec.data.pmtmethodtype==Wtf.bank_detail_type){
                        rec =this.chequeStore.getAt(this.NoOfJEpost.getValue());
                        this.chequeStore.remove(rec);
                    }
                    if(this.invoiceList && this.invoiceList.getValue()!=""){
                        var Rec = WtfGlobal.searchRecord(this.JEStore,this.invoiceList.getValue(),'journalentryid');
                        if(Rec.data.typeValue == Wtf.fund_transafer_journal_entry && Rec.data.pmtmethodtype==Wtf.bank_detail_type){
                            rec =this.chequeStore.getAt(this.NoOfJEpost.getValue());
                            this.chequeStore.remove(rec);
                        }    
                    }
                  }
            }
            if(this.invoiceRec && this.invoiceRec.data.typeValue == Wtf.fund_transafer_journal_entry && this.invoiceRec.data.pmtmethodtype==Wtf.bank_detail_type){
                this.setChequeNumbers();
            }
            if(this.invoiceList && this.invoiceList.getValue()!=""){
                var Rec = WtfGlobal.searchRecord(this.JEStore,this.invoiceList.getValue(),'journalentryid');
                if(Rec.data.typeValue == Wtf.fund_transafer_journal_entry && Rec.data.pmtmethodtype==Wtf.bank_detail_type){
                    this.setChequeNumbers();
                }    
            }
            this.setMemoDetails();
        },this);
        this.intervalType.on("select",function(){
             this.endDateValue = this.calculateEndDate();
              this.expireDate.setValue(this.endDateValue);
              if(this.invoiceRec && this.invoiceRec.data.typeValue == Wtf.fund_transafer_journal_entry && this.invoiceRec.data.pmtmethodtype==Wtf.bank_detail_type){
                this.setChequeNumbers();
            }
            if(this.invoiceList && this.invoiceList.getValue()!=""){
                var Rec = WtfGlobal.searchRecord(this.JEStore,this.invoiceList.getValue(),'journalentryid');
                if(Rec.data.typeValue == Wtf.fund_transafer_journal_entry && Rec.data.pmtmethodtype==Wtf.bank_detail_type){
                    this.setChequeNumbers();
                }    
            }
             this.setMemoDetails();
        },this);
        this.invoiceList.on("select",function(){
            var idx = this.JEStore.find('journalentryid',this.invoiceList.getValue());
            if(idx!=-1){
                this.record = this.JEStore.getAt(idx);
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
                this.NoOfJEpost.setValue(this.invoiceRec.data.NoOfJEpost);
                this.record.data.ispendingapproval?this.notify1.setValue(true):this.notify0.setValue(true);
                if(this.record.data.ispendingapproval){
                    this.approval.enable();
                    var pos=Wtf.userds.find("fname",this.record.data.approver)
                    if(pos!=-1)
                        this.approval.setValue(Wtf.userds.getAt(pos).data.userid);
                }
            }
            this.companyId = this.record.data.companyid;
            this.paymentMethodAccountId = this.record.data.pmtmethodaccountid;
            if(this.record && this.record.data.typeValue == Wtf.fund_transafer_journal_entry && this.record.data.pmtmethodtype==Wtf.bank_detail_type){
                this.GridPanel1.show();
                this.chequeOption.show();
                this.doLayout();
            } else {
                this.GridPanel1.hide();
                this.chequeOption.hide();
                this.doLayout();
            }
        },this);

        if(this.invoiceRec!=undefined){
            if(this.invoiceRec.data.repeateid){ //Update
                this.nextDateValue = this.invoiceRec.data.nextDate;
                this.dueDateValue = this.calculateDueDate();
                this.dueDate.setValue(this.dueDateValue);
                this.repeateId.setValue(this.invoiceRec.data.repeateid);
                this.nextDate.setValue(this.nextDateValue);
                this.dueDate.setValue(this.dueDateValue);
                this.interval.setValue(this.invoiceRec.data.interval);
                this.intervalType.setValue(this.invoiceRec.data.intervalType);
                this.expireDate.setValue(this.invoiceRec.data.expireDate);
                this.NoOfJEpost.setValue(this.invoiceRec.data.NoOfJEpost);
                this.invoiceRec.data.ispendingapproval?this.notify1.setValue(true):this.notify0.setValue(true);
                if(this.invoiceRec.data.ispendingapproval){
                    this.approval.enable();
                    var pos=Wtf.userds.find("fname",this.invoiceRec.data.approver)
                    if(pos!=-1)
                        this.approval.setValue(Wtf.userds.getAt(pos).data.userid);
                }
                if(this.invoiceRec.data.typeValue == Wtf.fund_transafer_journal_entry && this.invoiceRec.data.pmtmethodtype==Wtf.bank_detail_type){
                    var indexId=this.chequeGrid.getColumnModel().getIndexById('2');
                    var chequeOption=this.invoiceRec.data.chequeOption;
                    this.chequeGrid.getColumnModel().setHidden(indexId,chequeOption);
                } 
                this.setMemoDetails();
            } else {
                this.dueDateValue = this.calculateDueDate();
                this.dueDate.setValue(this.dueDateValue);
                this.repeateId.setValue(this.invoiceRec.data.repeateid);
                this.nextDate.setValue(this.nextDateValue);
                this.dueDate.setValue(this.dueDateValue);
                this.interval.setValue((this.invoiceRec.data.interval=="" || this.invoiceRec.data.interval==undefined)?1:this.invoiceRec.data.interval);
                this.intervalType.setValue(this.invoiceRec.data.intervalType =="" || this.invoiceRec.data.intervalType ==undefined ?"day":this.invoiceRec.data.intervalType);
                this.expDateValue = this.calculateEndDate();
                this.expireDate.setValue(this.expDateValue);
                this.NoOfJEpost.setValue((this.invoiceRec.data.NoOfJEpost=='' || this.invoiceRec.data.NoOfJEpost==undefined)?1:this.invoiceRec.data.NoOfJEpost);
                if( this.Memostore.getCount() < this.NoOfJEpost.getValue()){
                    for(var i=this.Memostore.getCount()+1 ;i <=this.NoOfJEpost.getValue(); i++){    
                        this.addGridRec(i);
                    }
                }else{
                    var reccount=this.Memostore.getCount()-1;
                    for(var j=this.NoOfJEpost.getValue() ;j <=reccount; j++){   
                        var rec=this.Memostore.getAt(this.NoOfJEpost.getValue());
                        this.Memostore.remove(rec);
                    }
                }
                if(this.invoiceRec.data.typeValue == Wtf.fund_transafer_journal_entry && this.invoiceRec.data.pmtmethodtype==Wtf.bank_detail_type){
                    if( this.chequeStore.getCount() < this.NoOfJEpost.getValue()){
                        for(var k=this.chequeStore.getCount()+1 ;k <=this.NoOfJEpost.getValue(); k++){    
                            this.addGridRecForCheque(k);
                        }
                    }else{
                        var account=this.chequeStore.getCount()-1;
                        for(var l=this.NoOfJEpost.getValue() ;l <=account; l++){   
                            var record=this.chequeStore.getAt(this.NoOfJEpost.getValue());
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
            html:getTopHtml(WtfGlobal.getLocaleText("acc.repeatedJE.recJE"),WtfGlobal.getLocaleText("acc.repeatedJE.recJEInfo"),"../../images/accounting_image/Chart-of-Accounts.gif", false)
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
        /*ERP-9539 : Here, we need not to worry about timezone. Because we are simply calculating next date based on provided
         days OR month OR year. The problem is geeting due to timezone. To avoid this, we ll subtract/add browser timeoffset
         from end date. Here we are calculating enddate in long. */
        
        var currentDate = new Date();   //ERP-9539
        var browserTimezoneOffset = currentDate.getTimezoneOffset();    //ERP-9539
        var enddate = this.nextDate==undefined ? new Date(this.startDateValue): new Date(this.nextDate.getValue());
        enddate.setDate(enddate.getDate()+noOfdays);
        //ERP-10069 : Subtracted 1 Invoice from Total No.of Invoices, because while calculating next date we are not considering next generation date.
        if(this.intervalType.getValue()=="month" && this.NoOfJEpost.getValue()!="" && this.interval.getValue()!=""){
            var monthsadd = (this.NoOfJEpost.getValue()-1) * this.interval.getValue();    //ERP-10069
            enddate = new Date((enddate.setMonth(enddate.getMonth()+monthsadd))+browserTimezoneOffset); //ERP-9539
        }else{
            if(this.intervalType.getValue()=="week" && this.NoOfJEpost.getValue()!="" && this.interval.getValue()!="")
                noOfdays = (this.NoOfJEpost.getValue()-1) * 7 * this.interval.getValue();   //ERP-10069
            if(this.intervalType.getValue()=="day" && this.NoOfJEpost.getValue()!="" && this.interval.getValue()!="")
                noOfdays = (this.NoOfJEpost.getValue()-1) * this.interval.getValue();
              
            enddate = new Date((enddate.setDate(enddate.getDate()+noOfdays))+browserTimezoneOffset);    //ERP-9539
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
    
    saveData:function(){
        if(this.autoNumber && this.autoNumber.getValue()){   // Auto -Generate cheque number
            this.saveDataFinally();
        } else {
            if(this.record && this.record.data.typeValue == Wtf.fund_transafer_journal_entry && this.record.data.pmtmethodtype==Wtf.bank_detail_type){
                this.checkForDuplicateChequeNumbers();
            } else if(this.invoiceRec && this.invoiceRec.data.typeValue == Wtf.fund_transafer_journal_entry && this.invoiceRec.data.pmtmethodtype==Wtf.bank_detail_type){
                this.checkForDuplicateChequeNumbers();
            } else {
                this.checkForBlankChequeNumbers();
            }
        }
    },
    
    saveDataFinally: function(){
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
        
        if(this.notify1.getValue()==true && this.approval.getValue()==""){
            this.approval.markInvalid(WtfGlobal.getLocaleText("acc.repeated.approvername"));
            valid = false;
        }
        
        if(!valid){
            WtfComMsgBox(2,2);
            return;
        }
        var rec=[];
        rec = this.repeateForm.getForm().getValues();
       
        if(this.isaddRecurringJE) {
            rec.JEId = this.invoiceList.getValue();
            rec.isCustBill = this.record.data.withoutinventory;
            rec.entryno=rec.invoicelstno;
        }else {
             rec.isCustBill =  this.isCustBill;
             rec.JEId = this.invoiceRec.data.journalentryid;
             rec.entryno=this.invoiceRec.data.entryno;
        }
        rec.detail="["+this.getRecords().join(",")+"]";
        rec.chequedetail = "["+this.getChequeDetails().join(",")+"]";  
        rec.isaddRecurringJE;
        Wtf.getCmp("savencloserecurringbtn").disable(); //To avoid double posting.
        Wtf.Ajax.requestEx({
            url:"ACCJournal/saveRepeateJEInfo.do",
            params: rec
        },this,this.genSuccessResponse,this.genFailureResponse);  
    },

 genSuccessResponse:function(response){
     Wtf.getCmp("savencloserecurringbtn").enable();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
        if(Wtf.getCmp("RepeatedJEWin"))
            Wtf.getCmp("RepeatedJEWin").close(); 
          
           this.close();
       
            if(Wtf.getCmp("JournalEntryDetails")){
                Wtf.getCmp("JournalEntryDetails").grid.getStore().reload();
                Wtf.getCmp("RepeateJEList").grid.getStore().reload();
            }
        
            
    },
    genFailureResponse:function(response){
        Wtf.getCmp("savencloserecurringbtn").enable();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
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
    
    setMemoDetails:function(){
        var interval=this.interval.getValue();
        var store = this.Memostore;        
        var browserTimezoneOffset = 0;//currentDate.getTimezoneOffset();    
        var nextDate=new Date(this.nextDate.getValue());
        var rec;
        var moduleName = WtfGlobal.getLocaleText("acc.je.Entryasof");
        var dateToSet=new Date();
        for(var i=0;i<store.getCount();i++){            
            rec=store.getAt(i); 
            if(i==0){
                var date = new Date(this.nextDate.getValue());
                date = date.format("Y-m-d");
                rec.set('memo',moduleName+" "+date);
                continue;
            }          
            if(this.intervalType.getValue()=="month"){
                dateToSet = new Date(nextDate.setMonth(nextDate.getMonth()+(interval))+browserTimezoneOffset);
                dateToSet = dateToSet.format("Y-m-d");
                rec.set('memo',moduleName+" "+dateToSet);
            } else {
                if(this.intervalType.getValue()=="week" ){
                    dateToSet = new Date(nextDate.setDate(nextDate.getDate()+(interval*7))+browserTimezoneOffset);
                    dateToSet = dateToSet.format("Y-m-d");
                    rec.set('memo',moduleName+" "+dateToSet);
                }
                if(this.intervalType.getValue()=="day" ){
                    dateToSet = new Date(nextDate.setDate(nextDate.getDate()+(interval))+browserTimezoneOffset);
                    dateToSet = dateToSet.format("Y-m-d");
                    rec.set('memo',moduleName+" "+dateToSet);
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
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.recurringMP.chequeNumbers")+" "+duplicateNumbers+" "+WtfGlobal.getLocaleText("acc.recurringMP.AlreadyExists")],2);
                return;
            } else{
                this.checkForBlankChequeNumbers();
            }
        },this.genFailureResponse);
   
        } else {
            this.checkForBlankChequeNumbers();
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
    }
});
