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
Wtf.account.JournalEntryDetailsPanel=function(config){
    this.exponly=false;
    this.id = config.id;
    this.costCenterId = "";
    this.extraFilters = config.extraFilters;
    if(config.extraFilters != undefined){//Cost Center Report View
        this.costCenterId = config.extraFilters.costcenter?config.extraFilters.costcenter:"";
        this.groupid = config.extraFilters.groupid?config.extraFilters.groupid:null;
        this.groupid = config.extraFilters.groupid==true?true:false;
    }
    this.entryID=config.entryID;
    this.appendID = true;
    this.nondeleted=false;
    this.deleted=false;
    this.cashtype="all";
    this.sDate= config.sDate;
    this.eDate = config.eDate;
    this.uPermType=Wtf.UPerm.coa;
    this.permType=Wtf.Perm.coa;
    this.reportbtnshwFlag=config.reportbtnshwFlag;
    this.tabTip = config.tabTip;
    this.pendingApproval=config.pendingApproval;
    this.print=false;
    this.isUserSummaryReportFlag=(config.isUserSummaryReportFlag != undefined || config.isUserSummaryReportFlag != "") ? config.isUserSummaryReportFlag : false;
    this.userid=config.userid;
    if(this.reportbtnshwFlag== undefined || this.reportbtnshwFlag == null)
       {
          this.reportbtnshwFlag=false;
       }

    var data = [[0, WtfGlobal.getLocaleText("acc.rem.105")], [3, WtfGlobal.getLocaleText("acc.field.CashReceiptsJournal")], [4, WtfGlobal.getLocaleText("acc.field.CashDisbursementJournal")], [5, WtfGlobal.getLocaleText("acc.field.Sales&ReceivableJournal")], [6, WtfGlobal.getLocaleText("acc.field.Purchase&PayableJournal")], [1, WtfGlobal.getLocaleText("acc.rem.106")], [2, WtfGlobal.getLocaleText("acc.rem.107")], [7, WtfGlobal.getLocaleText("acc.je.Type2")], [8, WtfGlobal.getLocaleText("acc.je.Type3")], [9, WtfGlobal.getLocaleText("acc.je.Type1")]];
    if (WtfGlobal.isIndiaCountryAndGSTApplied()) {
        data.push([10, Wtf.GSTRJETYPE.TDS]);
        data.push([11, Wtf.GSTRJETYPE.TCS]);
        data.push([12, Wtf.GSTRJETYPE.ITC]);
    }
    this.delTypeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'int'}, 'name'],
        data : data
    });

    this.typeEditor = new Wtf.form.ComboBox({
        store: this.delTypeStore,
        name:'typeid',
        displayField:'name',
        id:'view'+config.helpmodeid, //+this.id,
        valueField:'typeid',
        mode: 'local',
        value:0,
        width:80,
        listWidth:200,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    if(config.JETYpe!=undefined){
        this.typeEditor.setValue(config.JETYpe);
        this.cashtype="Party Journal";
    }
    this.GridRec = Wtf.data.Record.create ([
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'sequenceformatid'},
        {name:'companyname'},
        {name:'companyid'},
        {name:'eliminateflag'},
        {name:'deleted'},
        {name:'isOpeningDocument'},         //ERP-41455
        {name:'entrydate',type:'date'},
        {name:'memo'},
        {name:'jeDetails'},
        {name:'NoOfJEpost'}, 
        {name:'NoOfRemainJEpost'},  
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
        {name:'intervalType'},
        {name:'startDate', type:'date'},
        {name:'nextDate', type:'date'},
        {name:'expireDate', type:'date'},
        {name:'repeateid'},
        {name:'parentje'},
        {name:'isreverseje',type:'boolean'},
        {name:'reversejeno'},
        {name:'withoutinventory'},
        {name:'revaluationid'},
        {name:'externalcurrencyrate'},
        {name:'typeValue'},
        {name:'gstrType'},
        {name:'itctransactionids'},
        {name:'chequeNumber'},
        {name:'chequesequenceformatid'},
        {name:'bankName'},
        {name:'chequeDate',type:'date'},
        {name:'description'},
        {name:'partlyJeEntryWithCnDn'},
        {name:'DNsequenceformatid'},
        {name:'CNsequenceformatid'},
        {name:'dnNumber'},
        {name:'cnNumber'},
        {name:'iscnused'},
        {name:'isdnused'},
        {name : 'approvalstatus'},
        {name:'ischequeprinted', type:'boolean'},
        {name:'paidTo'},
        {name:'paidToCmb'},
        {name:'pmtmethodaccountname'},
        {name:'pmtmethod'},
        {name:'cntype'},
        {name:'isonetimereverse',type:'boolean'},
        {name:'isactivate',type:'boolean'},
        {name:'approver'},
        {name:'ispendingapproval',type:'boolean'},
        {name:'pmtmethodtype'},
        {name:'isBR',type:'boolean'},
        {name:'isBUR',type:'boolean'},
        {name:'BRID'},
        {name:'pmtmethodaccountid'},
        {name: 'chequeOption'},
        {name:'includeingstreport'},
        {name:'isConsignment'},
        {name:'isReconcilied'},
        {name:'isdisposalje'},
        {name:'isFromEclaim'},
        {name:'isLeaseFixedAsset'}
    ]);
    if(config.consolidateFlag) {
        this.Store = new Wtf.data.GroupingStore({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.GridRec),
            remoteSort: true,
            //autoLoad:true,
            url: "ACCReports/getJournalEntry.do",
            baseParams:{
                pendingApproval:this.pendingApproval,
                mode:54,
                costCenterId: this.costCenterId,
                groupid:this.groupid,
                withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                consolidateFlag:config.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                CashAndInvoice:true
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
            remoteSort: true,
            url: "ACCReports/getJournalEntry.do",
            baseParams:{
                pendingApproval:this.pendingApproval,
                mode:54,
                dtype : 'report',
                costCenterId: this.costCenterId,
                groupid:this.groupid,
                withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
                consolidateFlag:config.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                CashAndInvoice:true
            }
        });
    }
    this.Store.on('loadexception', function(){WtfGlobal.resetAjaxTimeOut();Wtf.MessageBox.hide();}, this);
    
    chkCostCenterload();
    if(Wtf.CostCenterStore.getCount()==0) Wtf.CostCenterStore.on("load", this.setCostCenter, this);
    this.costCenter = new Wtf.form.ComboBox({
        store: Wtf.CostCenterStore,
        name:'costCenterId',
        width:100,
        listWidth:100,
        displayField:'name',
        valueField:'id',
        triggerAction: 'all',
        mode: 'local',
        typeAhead:true,
        value: this.costCenterId,
        selectOnFocus:true,
        forceSelection: true
    });

    this.usersRec = new Wtf.data.Record.create([
        {name: 'userid'},
        {name: 'name', mapping: 'fullname'}
    ]);
    this.userds = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            totalProperty: 'count',
            root: "data"
        }, this.usersRec),
        url: "ProfileHandler/getAllUserDetails.do",
        baseParams: {
            mode: 11
        }
    });
    if (this.isUserSummaryReportFlag) {
        this.userds.load();
    }

    this.userds.on("load", function () {
        var record = new Wtf.data.Record({
            userid: '',
            name: 'All Records'
        });
        this.userds.insert(0, record);
        this.User.setValue(config.userid ? config.userid : '');
    }, this);
    this.User = new Wtf.form.ComboBox({
        store: this.userds,
        width: 100,
        listWidth: 100,
        displayField: 'name',
        valueField: 'userid',
        triggerAction: 'all',
        mode: 'local',
        value: config.userid ? config.userid : '',
        typeAhead: true,
        selectOnFocus: true,
        forceSelection: true
    });
    
    this.costCenter.on("focus", function(cmb, rec, ind){ 
        chkEmptyCmb(cmb.store.getCount(),'1');
    }); 
    this.costCenter.on("select", function(cmb, rec, ind){
        this.costCenterId = rec.data.id;

        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.costCenterId = this.costCenterId;
        this.Store.baseParams=currentBaseParams;

        //this.loadJEStore();
    },this);
    this.User.on("select", function(cmb, rec, ind){
        this.userid = rec.data.userid;
//        var currentBaseParams = this.Store.baseParams;
//        currentBaseParams.userid = this.userid;
//        this.Store.baseParams=currentBaseParams;
    },this);
    
    if(this.extraFilters != undefined){//Cost Center Report View
        var currentBaseParams = this.Store.baseParams;
        if(this.extraFilters.startdate != null && this.extraFilters.startdate != undefined){
            currentBaseParams.startdate = this.extraFilters.startdate;
        }
        if(this.extraFilters.enddate != null && this.extraFilters.enddate != undefined){
            currentBaseParams.enddate = this.extraFilters.enddate;
        }
        this.Store.baseParams=currentBaseParams;
    }
    this.expander = new Wtf.grid.RowExpander({});
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    
    var sdateSavedSearch;
    var edateSavedSearch;
    if(config.searchJson != undefined && config.searchJson != ""){
        sdateSavedSearch = JSON.parse(config.searchJson).data[0].sdate;
        edateSavedSearch = JSON.parse(config.searchJson).data[0].edate;
    }
    
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate' + this.id,
        format:WtfGlobal.getOnlyDateFormat(),
        value:this.sDate!=undefined? (typeof(this.sDate) == "string" ? Date.parseDate(this.sDate, "Y-m-d") : this.sDate):WtfGlobal.getDates(true, sdateSavedSearch) //need parsed 'startdate' as date comes in string type only from link information sales/purchase
    });
    
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate' + this.id,
        value:this.eDate!=undefined? (typeof(this.sDate) == "string" ? Date.parseDate(this.sDate, "Y-m-d") : this.eDate):WtfGlobal.getDates(false, edateSavedSearch)    //need parsed 'startdate' as date comes in string type only from link information sales/purchase
    });
    
    this.submitBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),
        tooltip :WtfGlobal.getLocaleText("acc.invReport.fetchTT"),  
        id: 'submitRec' + this.id,
        scope: this,
        iconCls:'accountingbase fetch',
        disabled :false
    });
        this.expandCollpseButton = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.Expand"),
        tooltip:WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls:'pwnd toggleButtonIcon',
        hidden: this.pendingApproval,
        scope:this,
        handler: function(){
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    });

    //    if(config.extraFilters == undefined){//Cost Center Report View - Don't show 'cost center' filter
    //        btnArr.push("->",WtfGlobal.getLocaleText("acc.common.costCenter"), this.costCenter);
    //    }
    this.RepeateJE=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.SetRecurringJE") ,
        iconCls:getButtonIconCls(Wtf.etype.copy),
        id:'RecurringJE',        
        tooltip :WtfGlobal.getLocaleText("acc.field.CreateRecurringJournalEntry"),
        style:"padding-left:0px;",
        scope: this,
        disabled : true,  
        hidden: this.pendingApproval,
        handler: this.repeateJEHandler
    });
        this.printCheck=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.chequePrint"),
        scope: this,
        disabled :true,
        hidden: this.pendingApproval,
        tooltip: WtfGlobal.getLocaleText("acc.common.chequePrint"),
        iconCls:'accountingbase pricelistbutton'
    });
        this.approvalHistoryBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.ApprovalHistory"), 
            scope: this,
            disabled : true,
            tooltip: WtfGlobal.getLocaleText("acc.field.ViewApprovalHistory"),
            handler: this.viewApprovalHistory,
            iconCls: "advanceSearchButton"
        });
    this.submitBttn.on("click", this.submitHandler, this);
    
    this.tbar2 = new Array();
    this.tbar2.push(WtfGlobal.getLocaleText("acc.common.from"));
    this.tbar2.push(this.startDate);
    this.tbar2.push(WtfGlobal.getLocaleText("acc.common.to"));
    this.tbar2.push(this.endDate);
    
    this.tbar2.push("-", WtfGlobal.getLocaleText("acc.common.costCenter"), this.costCenter);
    
    if (this.isUserSummaryReportFlag) {
        this.tbar2.push("-", WtfGlobal.getLocaleText("acc.field.User"), this.User);
    }
    
    this.tbar2.push("-");
    this.tbar2.push(this.submitBttn);
    if(!this.pendingApproval) {
        this.tbar2.push("-",this.expandCollpseButton);
         if(!this.isUserSummaryReportFlag){
            this.tbar2.push("-", this.RepeateJE);
            this.tbar2.push("-", this.printCheck);
            this.tbar2.push("-", this.approvalHistoryBtn);
    }
    }
      if (!this.isUserSummaryReportFlag) {
        this.tbar2.push("->", WtfGlobal.getLocaleText("acc.je.Type"));
        this.tbar2.push("->", this.typeEditor);
    }
    this.gridView1 = config.consolidateFlag?new Wtf.grid.GroupingView({
            forceFit:false,
            showGroupName: true,
            enableNoGroups:false, // REQUIRED!
            hideGroupedColumn: true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }):{
            forceFit:false,
            emptyText:'<div class="emptyGridText">' + WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText('acc.common.norec')  + ' <br>' + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"))+'</div>', //Message for user after opening report to fectch data
        };
// appening custom columns
    this.gridColumnModelArr=[];
    this.gridColumnModelArr.push(this.expander,this.sm,this.rowNo,{
            header:WtfGlobal.getLocaleText("acc.jeList.gridEntryDate"),  //"Entry Date",
            dataIndex:'entrydate',
            width:150,
            align:'center',
            pdfwidth:150,
            sortable:true,
            renderer:WtfGlobal.onlyDateDeletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.jeList.gridEntryNo"),  //"Entry Number",
            dataIndex:'entryno',
            width:150,
            pdfwidth:150,
            sortable:true,
            renderer: function (value, meta, rec) {
                 meta.attr = "Wtf:qtip='" + value + "' Wtf:qtitle='Entry Number' ";
                  if (!value){
                    return value;
                  }
                  if (rec.data.typeValue != 0 && rec.data.deleted == false) {  //if manual Journal enrty then provide link to view
                    value = WtfGlobal.linkRenderer(value, meta, rec)
                  } else {
                    value = "<span class='EntryNumPedding'>" + value + "</span>";
                  }
                  if (rec.data.deleted == true){
                    value = '<del>' + value + '</del>';
                  }

                return value;
        }
        },{
            header:WtfGlobal.getLocaleText("acc.field.TransactionID"),
            dataIndex:'transactionID',
            width:150,
            pdfwidth:50,
//            sortable:true,
            renderer:config.consolidateFlag?"":function(value,meta,rec){

                meta.attr = "Wtf:qtip='" + value + "' Wtf:qtitle='Transaction ID' ";
                if (!value){
                    return value;
                }
                
                if (rec.data.deleted == false && rec.data.type != "Work Order" && rec.data.isOpeningDocument != true) {       //If JE is realised / Unrelaised JE which is posted for opening document then we do not have to show link ERP-41455
                    value = WtfGlobal.linkRenderer(value, meta, rec)
                } else {
                    value = "<span class='EntryNumPedding'>" + value + "</span>";
                }
                if (rec.data.deleted == true){
                    value = '<del>' + value + '</del>';
                }
                
                return value;
        }
    },{
            header:WtfGlobal.getLocaleText("Description"),
            dataIndex:'transactionDetails',
            width:150,
            pdfwidth:70,
//            sortable:true,
        renderer:function(value,meta,rec){
            if(value==""){
                if(rec.data.isreverseje){
                    value=WtfGlobal.getLocaleText("acc.field.ReverseJEofManualJE")+rec.data.reversejeno;
                }else{
                    if(rec.data.typeValue==Wtf.normal_journal_entry){
                        value=WtfGlobal.getLocaleText("acc.je.Type1");      //Normal Journal Entry
                    }else if(rec.data.typeValue==Wtf.fund_transafer_journal_entry){
                        value=WtfGlobal.getLocaleText("acc.je.Type3");      //Funds Transfer
                    }else{
                        value=WtfGlobal.getLocaleText("acc.je.Type2");      //Party Journal Entry
                    }
                }                                        
                meta.attr = "Wtf:qtip='" + value + "' Wtf:qtitle='Description' ";
            }else{    
                meta.attr = "Wtf:qtip='" + value + "' Wtf:qtitle='Description' ";
            }
            if(!value) return value;
            if(rec.data.deleted)
                value='<del>'+value+'</del>';
            return value;
        }
        },{
            header:Wtf.account.companyAccountPref.descriptionType,  //"Memo",
            dataIndex:'memo',
            width:150,
            pdfwidth:150,
            sortable:true,
            //renderer:WtfGlobal.deletedRenderer
        renderer: function (value, meta, rec) {

            if (value !== "" || value !== undefined) {
                meta.attr = "Wtf:qtip='" + value + "' Wtf:qtitle='Memo' ";
            } else {
                meta.attr = "Wtf:qtip='" + "" + "' Wtf:qtitle='Memo' ";
            }
//            value = Wtf.util.Format.ellipsis(value, 60);// ERP-39526
            if (!value)
                return value;
            if (rec.data.deleted)
                value = '<del>' + value + '</del>';
            return value;

        }
        },{
            header:WtfGlobal.getLocaleText("acc.field.Company"),  
            dataIndex:'companyname',
            width:20,
            pdfwidth:150,
//            sortable:true,
            hidden:true,
            renderer: WtfGlobal.deletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.field.EliminateStatus"),  
            dataIndex:'eliminateflag',
            width:150,
            renderer:function(val,meta,rec){
                var value=null;
               value=(val?"Yes":"No");
               if (rec.data.deleted)
                value = '<del>' + value + '</del>';
               return value; 
            },
            pdfwidth:150,
            hidden:config.consolidateFlag?false:true
        },{
            header:WtfGlobal.getLocaleText("acc.field.bank.details"),
            dataIndex:'chequeNumber',
            width:150,
            pdfwidth:30,
            sortable:true,
            renderer:WtfGlobal.deletedRenderer
        },{
            header:WtfGlobal.getLocaleText("acc.field.Approval") +WtfGlobal.getLocaleText("acc.invoiceList.status"),
            dataIndex:'approvalstatus',
            width:150,
            pdfwidth:30,
            renderer: WtfGlobal.deletedRenderer,
            sortable:true
        });
        
    this.gridColumnModelArr = WtfGlobal.appendCustomColumn(this.gridColumnModelArr,GlobalColumnModelForReports[Wtf.Acc_GENERAL_LEDGER_ModuleId],true);
    
    this.grid = new Wtf.grid.GridPanel({
        id:"gridmsg"+config.helpmodeid+this.id,
        stripeRows :true,
        store:this.Store,
        tbar : this.tbar2,
        sm:this.sm,
        border:false,
        viewConfig: this.gridView1,
        forceFit:true,
        plugins: this.expander,
        loadMask : true,
        cm:new Wtf.grid.ColumnModel(this.gridColumnModelArr)
    });
    var colModelArray = GlobalColumnModelForReports[Wtf.Acc_GENERAL_LEDGER_ModuleId];
    WtfGlobal.updateStoreConfig(colModelArray,this.Store);
    //this.Store.load(); //Stop atuto load
    this.grid.on('render',function(){
        if(this.pendingApproval){
            this.Store.load({
                params:{
                    start:0,
                    limit:this.pagingToolbar.pageSize
                }
            });
        }
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    },this);
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });
    
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
        advSearch: false
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
            
    var btnArr=[];
    var bottombtnArr=[];
    btnArr.push(this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.dnList.searchText") + ' '+WtfGlobal.getLocaleText("acc.je.msg2"),  //'Quick Search by JE Number',
        id:"quickSearch"+config.helpmodeid, //+this.id,
        width: 200
     }));
     btnArr.push(this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset Search Results',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    }));
    
    this.resetBttn.on('click',this.handleResetClick,this);
    if (!this.isUserSummaryReportFlag) {
    btnArr.push(this.newTabButton=getCreateNewButton(config.consolidateFlag,this,WtfGlobal.getLocaleText("acc.WoutI.3"),this.reportbtnshwFlag));     
     this.newTabButton.on('click',this.openNewTab,this); 
    
    if( Wtf.account.companyAccountPref.editTransaction&&!WtfGlobal.EnableDisable(this.uPermType, this.permType.editje)){
        if(!this.pendingApproval){
            btnArr.push('-',this.editBtn=new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.field.EditJournalEntry"),  //'Delete Journal Entry',
                scope: this,
                disabled :true,
                hidden : config.consolidateFlag || this.reportbtnshwFlag,
                tooltip:WtfGlobal.getLocaleText("acc.field.EditJournalEntry"),  //{text:"Select a Journal Entry to delete.",dtext:"Select a Journal Entry to delete.", etext:"Delete selected Journal Entry details."},
                iconCls:getButtonIconCls(Wtf.etype.edit),
                handler:function(){
                    var rec = this.sm.getSelected();
                    if(!rec.data.isreverseje){
                        if((rec.data.reversejeno=="" && rec.data.reversejeno.length==0) && !rec.data.isonetimereverse && !rec.data.isRepeated){ //Source JE cannot edit, if it has reverse JE / Recurring JE. ERP-12921
                            var jetype = 1;
                            var arr=this.grid.getSelectionModel().getSelections();
                            if(arr!=undefined){
                                jetype=arr[0].data.typeValue;
                            }
                            if(jetype==2 && rec.data.partlyJeEntryWithCnDn==1){ //Party JE with CN&DN then checking whether cn or dn is used or not
                                if(rec.data.iscnused || rec.data.isdnused){
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.common.usedpartyjecndnalert")], 2);
                                    return;
                                }
                            } 
                         
                            // Do not allow user to edit JE if cheque is reconcilied
                            if(rec.data.isReconcilied){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.JE.checkStatusClearedCanNotEdit")], 2);
                                return;
                            }
                            if (rec.data.parentje != undefined && rec.data.parentje != "") {
                                /**
                                 * Allow to edit Recurred JE
                                 */
                                var isAllowedSpecificFields = true;
                                var alertMessage = WtfGlobal.getLocaleText("acc.invList.RecurredInvoice");
                                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), alertMessage, function(btn) {
                                    if (btn == "yes") {
                                        var comp = callJournalEntryTab(true, rec, rec.get('entryno'), jetype, undefined, undefined, undefined, isAllowedSpecificFields);
                                        if (comp) {
                                            comp.on('jeupdate', function() {
                                                this.Store.reload();
                                            }, this);
                                        }
                                    } else {
                                        return;
                                    }
                                }, this);
                            } else {
                                var comp = callJournalEntryTab(true, rec, rec.get('entryno'), jetype);
                                if (comp) {
                                    comp.on('jeupdate', function() {
                                        this.Store.reload();
                                    }, this);
                                }
                            }

                        }else if(rec.data.isRepeated){  //ERP-12921
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert")," <b>"+rec.data.entryno +"</b>  "+ WtfGlobal.getLocaleText("acc.recurring.journalntry.alreadyset")], 2);
                        }else {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.JournalEntryhasReverseJournalEntrypostedsocannotbeedited")], 2);
                        }
                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.YoucannoteditaReverseJournalEntry")], 2);
                    }               
                }
            }));
        }
    }
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.copyje)){
        btnArr.push('-',this.copyBtn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.CopyJournalEntry"),  //'Copy Journal Entry',
            scope: this,
            disabled :true,
            hidden : config.consolidateFlag || this.reportbtnshwFlag || this.pendingApproval,
            tooltip:WtfGlobal.getLocaleText("acc.field.CopyJournalEntry"),  
            iconCls:getButtonIconCls(Wtf.etype.copy),
            handler:function(){
                var rec = this.sm.getSelected();
                if(!rec.data.isreverseje){
                        var jetype = 1;
                        var arr=this.grid.getSelectionModel().getSelections();
                        if(arr!=undefined){
                            jetype=arr[0].data.typeValue;
                        }
//                        if(rec.data.parentje!=undefined && rec.data.parentje!=""){
//                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"<b>"+rec.data.entryno +"</b>  "+ WtfGlobal.getLocaleText("acc.recurred.child.copyjournalntry")], 2);
//                            return;
//                        }
                        var comp = callJournalEntryTab(false,rec, rec.get('entryno'), jetype,false,true);
                        if(comp){
                            comp.on('jeupdate', function(){
                                this.Store.reload();
                            }, this);
                        }                    
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.YoucannotcopyaReverseJournalEntry")], 2);
                }               
            }
        }));
    } 
      this.reverseBtn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.ReverseJournalEntry"),  //'Reverse Journal Entry',
            scope: this,
            disabled :true,
            hidden : (this.tabTip=="Journal Entry Report" && this.reportbtnshwFlag),    //Hide this button in case of Journal Book            
            tooltip:WtfGlobal.getLocaleText("acc.field.ReverseJournalEntry"),
            iconCls:getButtonIconCls(Wtf.etype.pricelistbutton),
            handler:this.reverseJEHandler
        })
    
    
//    if(config.extraFilters == undefined){//Cost Center Report View - Don't show 'Delete' Button
        var deletebtnArray=[];
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.removeje) && Wtf.account.companyAccountPref.deleteTransaction && !this.pendingApproval){
            deletebtnArray.push(this.deleteBtn=new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.jeList.deleteJE"),
                scope: this,
                tooltip:WtfGlobal.getLocaleText("acc.jeList.deleteJE"),  //{text:"Select a "+this.label+" to delete.",dtext:"Select a "+this.label+" to delete.", etext:"Delete selected "+this.label+" details."},
                iconCls:getButtonIconCls(Wtf.etype.menudelete),
                handler:this.deleteTransactionCheckBefore.createDelegate(this,this.del=["del"])
            }))
        }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.removeje) && Wtf.account.companyAccountPref.deleteTransaction){
            deletebtnArray.push(this.deleteBtnPerm=new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.jeList.deleteJE")+' '+WtfGlobal.getLocaleText("acc.field.Permanently"),
                scope: this,
                tooltip:WtfGlobal.getLocaleText("acc.jeList.deleteJE")+' '+WtfGlobal.getLocaleText("acc.field.Permanently"),  //{text:"Select a "+this.label+" to delete.",dtext:"Select a "+this.label+" to delete.", etext:"Delete selected "+this.label+" details."},
                iconCls:getButtonIconCls(Wtf.etype.menudelete),
                handler:this.deleteTransactionCheckBefore.createDelegate(this,this.del=["delp"])
            }))
        }
       if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.removeje) && Wtf.account.companyAccountPref.deleteTransaction){
        btnArr.push(this.deleteJe=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"), 
            scope: this,
            hidden : config.consolidateFlag || this.reportbtnshwFlag ,
            disabled :true,
            tooltip:WtfGlobal.getLocaleText("acc.field.allowsyoutodeletetherecord"), 
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            menu:deletebtnArray
        }))
        }
        if( Wtf.account.companyAccountPref.editTransaction){
        if(!this.pendingApproval){
           btnArr.push( this.reverseBtn);
        }
        }
//    }
//    if(config.extraFilters == undefined){//Cost Center Report View - Don't show 'Delete' Button
//        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.removeje) && Wtf.account.companyAccountPref.deleteTransaction){
            btnArr.push('-',this.eliminateBtn=new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.jeList.eliminateJE"),  //'Eliminate Journal Entry',
                scope: this,
                disabled :true,
                hidden : !config.consolidateFlag || this.pendingApproval,
                tooltip:WtfGlobal.getLocaleText("acc.jeList.eliminateJETT"),  //"Select a Journal Entry to Eliminate from consolidated reports.",
                iconCls:getButtonIconCls(Wtf.etype.deletebutton),
                handler:this.performEliminate.createDelegate(this)
            }));
//        }
//    }
        btnArr.push(this.pendingApprovalBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.ViewPendingApprovals"),
            tooltip: WtfGlobal.getLocaleText("acc.field.ViewPendingApprovals"),
        id: 'pendingApprovals' + this.id,
        scope: this, 
        hidden: this.pendingApproval,
        iconCls :getButtonIconCls(Wtf.etype.reorderreport)
    }));
        
    this.pendingApprovalBttn.on('click', function(){
        var panel = null;
        
        panel = Wtf.getCmp("JournalEntryDetailsPending");
        if(panel==null){
            panel = getJETab("JournalEntryDetailsPending", WtfGlobal.getLocaleText("acc.je.PendingApprovalJE(s)"), "",undefined, false,undefined,true);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);
        Wtf.getCmp('as').doLayout();   
    }, this);
    }
    if (!this.isUserSummaryReportFlag) {
    btnArr.push(this.AdvanceSearchBtn);
    }
    if(this.pendingApproval){
    btnArr.push(this.approveJEBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.cc.24"),
        tooltip : WtfGlobal.getLocaleText("acc.field.ApprovependingDeliveryOrder"), //Issue 31009 - [Pending Approval]Window name should be "Approve Pending Invoice" instead of "Approve Pending Approval". it should also have deskera logo
        id: 'approvepending' + this.id,
        scope: this,
        iconCls :this.isRequisition ? "accountingbase prapprove" : getButtonIconCls(Wtf.etype.add),
        disabled :true,
        // hidden:this.isFixedAsset || this.isLeaseFixedAsset,
        handler : this.approvePendingJE
    }))
    }
    if(this.pendingApproval){    
    btnArr.push(this.rejectJEBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.Reject"),
        tooltip : WtfGlobal.getLocaleText("acc.field.Rejectpending"),
        id: 'rejectpending' + this.id,
        scope: this,
        // hidden:this.isFixedAsset || this.isLeaseFixedAsset,
        iconCls:getButtonIconCls(Wtf.etype.deletebutton),
        disabled :true,
        handler : this.handleReject
    }));
    }
//        this.printCheck=new Wtf.Toolbar.Button({
//        text: WtfGlobal.getLocaleText("acc.common.chequePrint"),
//        scope: this,
//        disabled :true,
//     //   hidden: !(this.moduleid==Wtf.Acc_Make_Payment_ModuleId),
//        tooltip: WtfGlobal.getLocaleText("acc.common.chequePrint"),
//        iconCls:'accountingbase pricelistbutton'
//    });

    this.printCheck.on('click',this.printCkeck,this);
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportdataje)){
        var mnuBtns=[];
        var csvbtn=new Wtf.Action({
            iconCls:'pwnd '+'exportcsv',
            text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToCSVTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToCSV")+"</span>",
            scope: this,
            menu:{
                items:[{
                    iconCls:'pwnd '+'exportcsv',
                    text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.jeexport.summary")+"'>"+WtfGlobal.getLocaleText("acc.jeexport.summary")+"</span>",
                    scope: this,
                    handler:function(){
                        this.exportWithTemplate("csv",false,"")
                    }                        
                },
                {
                    iconCls:'pwnd '+'exportcsv',
                    text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.jeexport.detailed")+"'>"+WtfGlobal.getLocaleText("acc.jeexport.detailed")+"</span>",
                    scope: this,
                    handler:function(){
                        this.exportWithTemplate("detailedCSV",false);
                    }
                }]
            }
           
        });
        mnuBtns.push(csvbtn)
        var csvselectedbtn=new Wtf.Action({
            iconCls:'pwnd '+'exportcsv',
            text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToCSVTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportSelectedToCSV")+"</span>",
            scope: this,
            menu:{
                items:[{
                    iconCls:'pwnd '+'exportcsv',
                    text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.jeexport.summary")+"'>"+WtfGlobal.getLocaleText("acc.jeexport.summary")+"</span>",
                    scope: this,
                    handler:function(){
                        this.exportWithTemplate("csv",true)
                    }
                },{
                    iconCls:'pwnd '+'exportcsv',
                    text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.jeexport.detailed")+"'>"+WtfGlobal.getLocaleText("acc.jeexport.detailed")+"</span>",
                    scope: this,
                    handler:function(){
                        this.exportWithTemplate("detailedCSV",true);
                    } 
                }]
            }
           
        });
        mnuBtns.push(csvselectedbtn)
       var xlsbtn=new Wtf.Action({
            iconCls:'pwnd '+'exportcsv',
            text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"</span>",
            scope: this,
            menu:{
                items:[{
                    iconCls:'pwnd '+'exportcsv',
                    text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.jeexport.summary")+"'>"+WtfGlobal.getLocaleText("acc.jeexport.summary")+"</span>",
                    scope: this,
                    handler:function(){
                        this.exportWithTemplate("xls",false,"");
                    }                        
                },{
                    iconCls:'pwnd '+'exportcsv',
                    text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.jeexport.detailed")+"'>"+WtfGlobal.getLocaleText("acc.jeexport.detailed")+"</span>",
                    scope: this,
                    handler:function(){
                        this.exportWithTemplate("detailedXls",false);
                    } 
                }]
            }
           
        });
        mnuBtns.push(xlsbtn);
         var xlsselectedbtn=new Wtf.Action({
            iconCls:'pwnd '+'exportcsv',
            text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"'>"+WtfGlobal.getLocaleText("acc.common.exportSelectedToXLS")+"</span>",
            scope: this,
           menu:{
                items:[{
                    iconCls:'pwnd '+'exportcsv',
                    text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.jeexport.summary")+"'>"+WtfGlobal.getLocaleText("acc.jeexport.summary")+"</span>",
                    scope: this,
                    handler:function(){
                        this.exportWithTemplate("xls",true);
                    }
                },{
                    iconCls:'pwnd '+'exportcsv',
                    text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.jeexport.detailed")+"'>"+WtfGlobal.getLocaleText("acc.jeexport.detailed")+"</span>",
                    scope: this,
                    handler:function(){
                        this.exportWithTemplate("detailedXls",true);
                    }
                }]
            }
        });
        mnuBtns.push(xlsselectedbtn);
        var pdfbtnWithoutMenu=new Wtf.Action({
            iconCls:'pwnd '+'exportpdf',
            text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToPDFTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToPDF")+"</span>",
            scope: this,
                    handler:function(){
                        this.exportWithTemplate("pdf",false,false)
                    }
        });
        
        var pdfselectedbtnWithoutMenu=new Wtf.Action({
            iconCls:'pwnd '+'exportpdf',
            text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToPDFTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportSelectedToPDF")+"</span>",
            scope: this,
            handler:function(){
                this.exportWithTemplate("pdf",true,false)
            }
        });
        
        var pdfbtn=new Wtf.Action({
            iconCls:'pwnd '+'exportpdf',
            text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToPDFTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToPDF")+"</span>",
            scope: this,
            menu: {        
                items: [
                {
                    text: WtfGlobal.getLocaleText("acc.journalentry.exportpdf.portrait"),
                    iconCls:'pwnd '+'exportpdf',                
                    scope: this,
                    handler:function(){
                        this.exportWithTemplate("pdf",false,false)
                    }
                },{
                    text: WtfGlobal.getLocaleText("acc.journalentry.exportpdf.landscape"),
            iconCls:'pwnd '+'exportpdf',
            scope: this,
                    handler:function(){
                        this.exportWithTemplate("pdf",false,true)
                    }
                }
            ]
        }
        });
        
         var pdfbtnForPrimePartners=new Wtf.Action({
            iconCls:'pwnd '+'exportpdf',
            text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToPDFTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToPDF")+"</span>",
            scope: this,
            menu: {        
                items: [
                {
                    text: WtfGlobal.getLocaleText("acc.journalentry.exportpdf.portrait"),
                    iconCls:'pwnd '+'exportpdf',                
                    scope: this,
                    handler:function(){
                        this.exportWithTemplate("pdf",false,false)
                    }
                },{
                    text: WtfGlobal.getLocaleText("acc.journalentry.exportpdf.landscape"),
                    iconCls:'pwnd '+'exportpdf',
                    scope: this,
                    menu: {        
                            items: [
                            {
                                text: WtfGlobal.getLocaleText("GL Voucher"),
                                iconCls:'pwnd '+'exportpdf',                
                                scope: this,
                                handler:function(){
                                    this.exportWithTemplate("pdf",false,true,undefined,0)
                                }
                            },{
                                text: WtfGlobal.getLocaleText("GL Payment Voucher"),
                                iconCls:'pwnd '+'exportpdf',                
                                scope: this,
                                handler:function(){
                                    this.exportWithTemplate("pdf",false,true,undefined,1)
                                }
                            },{
                                text: WtfGlobal.getLocaleText("GL Receipt Voucher"),
                                iconCls:'pwnd '+'exportpdf',                
                                scope: this,
                                handler:function(){
                                    this.exportWithTemplate("pdf",false,true,undefined,2)
                                }
                            }
                            ]
                        }
                }
            ]
        }
        });
        
            if (Wtf.templateflag==Wtf.BIT_templateflag){
                mnuBtns.push(pdfbtnWithoutMenu)
            }else if(Wtf.templateflag==Wtf.PrimePartners_templateflag){
                mnuBtns.push(pdfbtnForPrimePartners)  //Added multiple designs for Prime Partners Journal Entry.
            }else{
                mnuBtns.push(pdfbtn)
            }
        
        var pdfselectedbtn=new Wtf.Action({
            iconCls:'pwnd '+'exportpdf',
            text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToPDFTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportSelectedToPDF")+"</span>",
            scope: this,
            menu: {        
                items: [
                {
                    text: WtfGlobal.getLocaleText("acc.journalentry.exportpdf.portrait"),
                    iconCls:'pwnd '+'exportpdf',                
                    scope: this,
                    handler:function(){
                        this.exportWithTemplate("pdf",true,false)
                    }
                },{
                    text: WtfGlobal.getLocaleText("acc.journalentry.exportpdf.landscape"),
                    iconCls:'pwnd '+'exportpdf',                
                    scope: this,
                    handler:function(){
                        this.exportWithTemplate("pdf",true,true)
                    }
                }
                ]
            }
        });       
        
        var pdfselectedbtnForPrimePartners=new Wtf.Action({
            iconCls:'pwnd '+'exportpdf',
            text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToPDFTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportSelectedToPDF")+"</span>",
            scope: this,
            menu: {        
                items: [
                {
                    text: WtfGlobal.getLocaleText("acc.journalentry.exportpdf.portrait"),
                    iconCls:'pwnd '+'exportpdf',                
                    scope: this,
                    handler:function(){
                        this.exportWithTemplate("pdf",true,false)
                    }
                },{
                    text: WtfGlobal.getLocaleText("acc.journalentry.exportpdf.landscape"),
                    iconCls:'pwnd '+'exportpdf',                
                    scope: this,
                    menu: {        
                            items: [
                            {
                                text: WtfGlobal.getLocaleText("GL Voucher"),
                                iconCls:'pwnd '+'exportpdf',                
                                scope: this,
                                handler:function(){
                                    this.exportWithTemplate("pdf",true,true,undefined,0)
                                }
                            },{
                                text: WtfGlobal.getLocaleText("GL Payment Voucher"),
                                iconCls:'pwnd '+'exportpdf',                
                                scope: this,
                                handler:function(){
                                    this.exportWithTemplate("pdf",true,true,undefined,1)
                                }
                            },{
                                text: WtfGlobal.getLocaleText("GL Receipt Voucher"),
                                iconCls:'pwnd '+'exportpdf',                
                                scope: this,
                                handler:function(){
                                    this.exportWithTemplate("pdf",true,true,undefined,2)
                                }
                            }
                            ]
                        }
                }
                ]
            }
        });            

        if (Wtf.templateflag==Wtf.BIT_templateflag){
            mnuBtns.push(pdfselectedbtnWithoutMenu)
        }else if(Wtf.templateflag==Wtf.PrimePartners_templateflag){
            mnuBtns.push(pdfselectedbtnForPrimePartners)  //Added multiple designs for Prime Partners Journal Entry.
        }else{
            mnuBtns.push(pdfselectedbtn)
        }
        
        if (!this.isUserSummaryReportFlag) {
            bottombtnArr.push('-', this.exportButton = new Wtf.Button({
                scope: this,
                id: (this.isUserSummaryReportFlag ? "userSummaryreport" : "exportReports") + config.helpmodeid, //+this.id,
                iconCls: (Wtf.isChrome ? 'pwnd exportChrome' : 'pwnd export'),
                text: WtfGlobal.getLocaleText("acc.common.export"),
                tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
                disabled: true,
                menu: mnuBtns
            }));
        }
      
       if(Wtf.templateflag == 1){ //template flag is 1 for only SMS Company.
            bottombtnArr.push('-',this.singlePrint=new Wtf.exportButton({
                    obj:this,
                    id:"printReports"+config.helpmodeid+config.id,
                    iconCls: 'pwnd exportpdfsingle',
                    text:WtfGlobal.getLocaleText("acc.rem.39.single"),
                    tooltip :WtfGlobal.getLocaleText("acc.rem.39.single"),  //'Print report details',
                    disabled :true,
                    menuItem:{rowPdf:true,rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39")},// + " "+ (config.isCNReport?WtfGlobal.getLocaleText("acc.accPref.autoCN"):WtfGlobal.getLocaleText("acc.accPref.autoDN"))},
                get:Wtf.autoNum.JournalEntry
            }));
      }      
     }
     if (!WtfGlobal.EnableDisable(this.uPermType, this.permType.printje)) {
        if (!this.isUserSummaryReportFlag) {
            bottombtnArr.push('-', this.printButton = new Wtf.Button({
                text: WtfGlobal.getLocaleText("acc.common.print"), //"Print",
                iconCls: 'pwnd printButtonIcon',
                scope: this,
                tooltip: WtfGlobal.getLocaleText("acc.common.printTT"), //'Print report details',
                disabled: true,
                label: WtfGlobal.getLocaleText("acc.je.tabTitle"),
                menu: {
                    items: [
                        {
                            text: WtfGlobal.getLocaleText("acc.print.selected"),
                            iconCls: 'pwnd printButtonIcon',
                            scope: this,
                            handler: function () {
                                this.exportWithTemplate("print", true)
                            }
                        }, {
                            text: WtfGlobal.getLocaleText("acc.print.All"),
                            iconCls: 'pwnd printButtonIcon',
                            scope: this,
                            handler: function () {
                                this.exportWithTemplate("print")
                            }
                        }
                    ]
                }
            }));
        }
    }

    var importBtnArray = [];
    if (Wtf.account.companyAccountPref.activateimportForJE) {
        
        var transJEBtnArray = [];
        var importCSVBttn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.import.csv"), // "Import CSV File",
            tooltip: {
                text: WtfGlobal.getLocaleText("acc.import.csv")
            },
            scope: this,
            iconCls: 'pwnd importcsv',
            handler: callProductImportWin.createDelegate(this,[false,true,false,false])
        });
        transJEBtnArray.push(importCSVBttn);
        
        var importXLSBttn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.import.excel"), // "Import XLS/XLSX File",
            tooltip: {
                text: WtfGlobal.getLocaleText("acc.import.excel")
            },
            scope: this,
            iconCls: 'pwnd importxls',
            handler: callProductImportWin.createDelegate(this,[false,true,false,true])
        });
        transJEBtnArray.push(importXLSBttn);
        
        this.importJournalEntryForTrans = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.field.importTransactionDataAsJournalEntry"), // "Import Transaction Data as Journal Entry",
            scope: this,
            iconCls: 'pwnd importJE',
            menu: transJEBtnArray
        });
        importBtnArray.push(this.importJournalEntryForTrans);
        
        importBtnArray.push(this.importRefundJournalEntryXLS = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.field.importTransactionDataAsRefundJournalEntry"), // "Import Transaction Data as Refund Journal Entry",
            scope: this,
            tooltip: {
                text: WtfGlobal.getLocaleText("acc.import.xls")
            },
            iconCls: 'pwnd importcsv',
            handler: callProductImportWin.createDelegate(this,[false,false,true,false])
        }));
    }
    
    var jeEtraConfig = {};
    jeEtraConfig.url= "ACCJournalCMN/importJournalEntryForAll.do";
    var importJEbtnArray = Wtf.importMenuArray(this, "Journal Entry", this.Store, "", jeEtraConfig);
    
    this.importJEForAllBtn = new Wtf.Action({
        text: WtfGlobal.getLocaleText("acc.field.importJournalEntry"), // "Import Journal Entry",
        tooltip: WtfGlobal.getLocaleText("acc.field.importJournalEntry"), // "Import Journal Entry",
        scope: this,
        iconCls:  'pwnd importJE',
        menu: importJEbtnArray
    });
    importBtnArray.push(this.importJEForAllBtn);
    if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.importje)) {
        if (!this.isUserSummaryReportFlag) {
            bottombtnArr.push('-', this.importBtn = new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.common.import"),
                scope: this,
                tooltip: WtfGlobal.getLocaleText("acc.common.import"),
                iconCls: (Wtf.isChrome ? 'pwnd importChrome' : 'pwnd import'),
                menu: importBtnArray
            }));
        }
    }
//        this.expandCollpseButton = new Wtf.Toolbar.Button({
//            text:WtfGlobal.getLocaleText("acc.field.Expand"),
//            tooltip:WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
//            iconCls:'pwnd toggleButtonIcon',
//            hidden: this.pendingApproval,
//            scope:this,
//            handler: function(){
//                this.expandCollapseGrid(this.expandCollpseButton.getText());
//            }
//        });
//
////    if(config.extraFilters == undefined){//Cost Center Report View - Don't show 'cost center' filter
////        btnArr.push("->",WtfGlobal.getLocaleText("acc.common.costCenter"), this.costCenter);
////    }
//this.RepeateJE=new Wtf.Toolbar.Button({
//        text:WtfGlobal.getLocaleText("acc.field.SetRecurringJE") ,
//        iconCls:getButtonIconCls(Wtf.etype.copy),
//        id:'RecurringJE',        
//        tooltip :WtfGlobal.getLocaleText("acc.field.CreateRecurringJournalEntry"),
//        style:" padding-left: 15px;",
//        scope: this,
//        disabled : true,  
//        hidden: this.pendingApproval,
//        handler: this.repeateJEHandler
//    });
  //  btnArr.push(this.RepeateJE);
   btnArr.push('->',getHelpButton(this,config.helpmodeid));
    this.leadpan = new Wtf.Panel({
        border:false,
        layout : "border",
        items:[this.objsearchComponent,{
            region: 'center',
            layout: 'fit',
            border: false,
            tbar:btnArr,
            items: [this.grid],
            bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.Store,
                searchField: this.quickPanelSearch,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
                plugins: this.pP = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                    }),
                items: bottombtnArr
            })
        }]
    });
    this.GrandTotalSummary=new Wtf.XTemplate(// to display the grand total Ref ERP-8925
        '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',            

        '<div>',
        '<table width="100%">'+
        '<tr>'+
        '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.common.totalcreditamount")+':</b></td><td style="width:55%;"><span><b>'+Wtf.util.Format.ellipsis('{creditTotal}',60)+'</b></span></td>'+                   
        '</tr>'+
         '<tr>'+
        '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.common.totaldebitamount")+':</b></td><td style="width:55%;"><span><b>'+Wtf.util.Format.ellipsis('{debitTotal}',60)+'</b></span></td>'+                   
        '</tr>'+
        '</table>'+
        '</div>',            
                                 
        '</div>'
        );
            
    this.GrandTotalSummaryTPL=new Wtf.Panel({
        id:this.isSummary?'GrandTotalSummaryTPL'+this.id:'GrandTotalReportTPL'+this.id,
        border:false,
        width:'95%',
        baseCls:'tempbackgroundview',
        html:this.GrandTotalSummary.apply({
            creditTotal:WtfGlobal.currencyRenderer(0)                    ,
            debitTotal:WtfGlobal.currencyRenderer(0)                    
        })
    }); 
    var summ = [];
    if(this.isUserSummaryReportFlag){
        summ.push("->")
        summ.push(this.GrandTotalSummaryTPL)
    }
    
    
    
    
    Wtf.apply(this,{
        border:false,
        layout : "fit",
        items:[this.leadpan],
        bbar: summ
    },config);
    this.Store.on('beforeload',function(s,o){
        WtfGlobal.setAjaxTimeOut();
        if(!o.params)o.params={};
        o.params.deleted=this.deleted;
        o.params.nondeleted=this.nondeleted;
        o.params.cashtype=this.cashtype
        o.params.costCenterId = this.costCenter.getValue();
        o.params.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),  //ERP-8487
        o.params.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue())         //ERP-8487
        if (this.isUserSummaryReportFlag) {
            o.params.isUserSummaryReportFlag=this.isUserSummaryReportFlag;
            var currentBaseParams = this.Store.baseParams;
            currentBaseParams.userid = this.userid;
            this.Store.baseParams=currentBaseParams;
        }
    },this);
    
    this.Store.on("load", function(store){
        WtfGlobal.resetAjaxTimeOut();
        if(store.getCount()==0){
            if(this.exportButton)this.exportButton.disable();
            if(this.printButton)this.printButton.disable();
            if(this.deleteJe)this.deleteJe.disable();
            if(this.eliminateBtn)this.eliminateBtn.disable();
        }else{
            if(this.exportButton)this.exportButton.enable();
            if(this.printButton)this.printButton.enable();
            if(this.deleteBtn)this.deleteBtn.enable();
            if(this.eliminateBtn)this.eliminateBtn.enable();            
        }
    },this);
    this.typeEditor.on('select',this.loadTypeStore,this);
    this.expander.on("expand",this.onRowexpand,this);
    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);

    this.Store.on('load',this.expandRow, this);
    this.Store.on('datachanged', this.dataChanged, this);
    this.Store.on('load',this.storeloaded,this);
    this.grid.on('cellclick',this.onCellClick, this);
    this.getMyConfig();
    

    Wtf.account.JournalEntryDetailsPanel.superclass.constructor.call(this,config);
}
 Wtf.extend(Wtf.account.JournalEntryDetailsPanel,Wtf.Panel,{
     submitHandler : function(){
         if(this.startDate.getValue()>this.endDate.getValue()){
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
             return;
         }
         this.loadJEStore();
     },
     exportWithTemplate:function(type,SelectedExportFlag,Landscape_Orientation,ExportAll,typeofPrime){
        var exportUrl = "ACCReports/exportJournalEntry.do";
        var nonDeleted = this.nondeleted==undefined ? false : this.nondeleted;
        var Landscape_Orientation = Landscape_Orientation==undefined ? false : Landscape_Orientation;
        var deleted = this.deleted==undefined ? false : this.deleted;
//        var costCenterId = this.costCenterId==undefined ? '' : this.costCenterId;
         var selectedIds=[];
        if(SelectedExportFlag!=undefined && SelectedExportFlag){
            this.recArr = this.grid.getSelectionModel().getSelections();
            if(this.recArr.length<=0){
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.common.exportSelectedBlankMessage")],2);
                 return;
            }
            for(var i=0;i<this.recArr.length;i++){
                    selectedIds.push(this.recArr[i].data.journalentryid);
            }
        }
        var ss = this.quickPanelSearch.getValue();
        var linkid = this.entryID==undefined ? '' : this.entryID;
        var fileName = "Journal Entry Report_v1";
        if (type == "print") {
            fileName = WtfGlobal.getLocaleText("acc.dashboard.consolidateJournalEntryLink");
        }
        var name = "Journal Entry Report";
        var header = "entrydate,entryno,transactionID,transactionDetails,memo,accountName,currencycode,debitamountintransactioncurrency,creditamountintransactioncurrency,debitAmount,creditAmount,description";
        var title =  WtfGlobal.getLocaleText("acc.jeList.gridEntryDate")+","+WtfGlobal.getLocaleText("acc.jeList.gridEntryNo")+","+WtfGlobal.getLocaleText("acc.field.TransactionID")+","+WtfGlobal.getLocaleText("acc.je.TransactionDetails")+","+WtfGlobal.getLocaleText("acc.common.memo")+","+WtfGlobal.getLocaleText("acc.paymentTerms.accountName")+","+WtfGlobal.getLocaleText("acc.common.currencyFilterLable")+","+WtfGlobal.getLocaleText("acc.je.debitAmt")+","+WtfGlobal.getLocaleText("acc.bankReconcile.gridCreditAmount")+","+WtfGlobal.getLocaleText("acc.bankReconcile.gridDebitAmount")+"("+WtfGlobal.getCurrencySymbol()+"),"+WtfGlobal.getLocaleText("acc.bankReconcile.gridCreditAmount")+"("+WtfGlobal.getCurrencySymbol()+"),"+WtfGlobal.getLocaleText("acc.contractActivityPanel.Description");
        var align = "date,none,none,none,none,none,none,none,none,none,none,none";
        if(type=="detailedXls" || type=="detailedCSV"){
            header = "entrydate,entryno,transactionID,transactionDetails,memo,accountCode,accountName,currencName,debitamountintransactioncurrency,creditamountintransactioncurrency,debitAmount,creditAmount,exchangerate,description";
            title = "Entry Date,Entry Number,Transaction ID,Transaction Details,Memo,Account Code,Account Name,Currency,Debit Amount,Credit Amount,Debit Amount("+WtfGlobal.getCurrencySymbol()+"),Credit Amount("+WtfGlobal.getCurrencySymbol()+"),Exchange Rate,Description";
            align = "date,none,none,none,none,none,none,none,none,none,none,none,none,none";
        }
        if (type == "detailedXls" || type == "detailedCSV" || type == "xls" || type == "csv" || type == "print") {
            var custArr = [];
            custArr = WtfGlobal.appendCustomColumn(custArr, GlobalColumnModelForReports[Wtf.Acc_GENERAL_LEDGER_ModuleId], true);
            if (type == "detailedXls" || type == "detailedCSV") {
                custArr = WtfGlobal.appendCustomColumn(custArr, GlobalColumnModel[Wtf.Acc_GENERAL_LEDGER_ModuleId]);
            }
            for (i = 0; i < custArr.length; i++) {
                if (custArr[i].header != undefined)
                    header += ',' + custArr[i].dataIndex;
                title += ',' + custArr[i].header;
                align += ',none';
            }
        }
        var typeforPrime = typeofPrime == undefined ? 0: typeofPrime ;
        var withoutinventory = Wtf.account.companyAccountPref.withoutinventory;
        var isLetterHead = Wtf.account.companyAccountPref.defaultTemplateLogoFlag;
        var cashtype = this.cashtype;
        var costCenterId = this.costCenter.getValue();
        var startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue()); //For Export PDF      //ERP-8487
        var enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());   //For Export PDF          //ERP-8487
        var searchJsonString = "";
        if(this.searchJson!= null && this.searchJson!= undefined && this.searchJson!= ""){
            searchJsonString = "&searchJson="+this.searchJson+"&flag=1&moduleid="+Wtf.Acc_GENERAL_LEDGER_ModuleId+"&filterConjuctionCriteria="+this.filterConjuctionCrit;
        }
        /*
         * 
         * The below code changes are made to convert GET method to POST
         */
        var urlExcludingParameter = exportUrl + "?";
        var parameters ="";
        if(this.consolidateFlag) { 
            parameters = "consolidateFlag="+this.consolidateFlag+"&companyids="+companyids+"&gcurrencyid="+gcurrencyid+"&userid="+loginid+"&filename="+encodeURIComponent(fileName)+ "&name="+encodeURIComponent(name) + "&cashtype=" + encodeURIComponent(cashtype) + "&withoutinventory=" +withoutinventory + "&filetype="+type+"&type="+type+"&nondeleted="+nonDeleted+"&deleted="+deleted+"&costCenterId="+costCenterId+"&selectedIds="+selectedIds+"&startdate="+startdate+"&enddate="+enddate+"&linkid="+linkid+"&header="+header+"&title="+encodeURIComponent(title)+"&width=150&get=27&align="+align+searchJsonString+"&Landscape_Orientation="+Landscape_Orientation+"&templateflag="+Wtf.templateflag+"&pendingFlag="+this.pendingApproval+"&isLetterHead="+isLetterHead+"&typeforPrime="+typeforPrime;
        } else {
            parameters = "filename="+encodeURIComponent(fileName)+ "&name="+encodeURIComponent(name) + "&cashtype=" + encodeURIComponent(cashtype) + "&withoutinventory=" +withoutinventory + "&filetype="+type+"&type="+type+"&nondeleted="+nonDeleted+"&deleted="+deleted+"&costCenterId="+costCenterId+"&selectedIds="+selectedIds+"&startdate="+startdate+"&enddate="+enddate+"&linkid="+linkid+"&header="+header+"&title="+encodeURIComponent(title)+"&width=150&get=27&align="+align+searchJsonString+"&Landscape_Orientation="+Landscape_Orientation+"&templateflag="+Wtf.templateflag+"&pendingFlag="+this.pendingApproval+"&ExportAll="+ExportAll+"&isLetterHead="+isLetterHead+"&typeforPrime="+typeforPrime+"&ss="+ encodeURIComponent(ss);
        }  
        url = urlExcludingParameter + parameters;
        if ((type == "detailedXls" || type == "xls") && (SelectedExportFlag != undefined && !SelectedExportFlag)) {
            /*
             * Ask user to Export Xls in Thread or Not
             */
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.info"), //'Warning',
                msg: WtfGlobal.getLocaleText("acc.generalLedger.msg.downloadTakeTime"),
                buttons: Wtf.MessageBox.YESNOCANCEL,
                closable: false,
                fn: function(btn) {
                    if (btn == "yes") {
                        url += "&threadflag=" + true;
                        Wtf.get('downloadframe').dom.src  = url;
                    } else if (btn == "no") {
                        url += "&threadflag=" + false;
                        Wtf.get('downloadframe').dom.src  = url;
                    }
                },
                scope: this,
                icon: Wtf.MessageBox.QUESTION
            });
        }else{
            if(type == "print") {
                  url+="&generatedOnTime="+WtfGlobal.getGeneratedOnTimestamp();
                  window.open(url, "mywindow","menubar=1,resizable=1,scrollbars=1");
            } else {
//                Wtf.get('downloadframe').dom.src  = url;
                  WtfGlobal.postData(urlExcludingParameter, parameters);
            }
        }
       // Wtf.get('downloadframe').dom.src = url;
    },

    genSuccessResponseStat: function (response) {
        if (response.success) {
            this.memoWin.close();
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function () {
                this.Store.reload();
            }, this);
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],2);
            Wtf.getCmp("Approvebtn"+this.id).enable();
        }
    },
    genFailureResponseStat : function(response){       
        this.memoWin.close(); 
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Erroroccurredwhilepostingreversejournalentry")],2);
    },
    
    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var dataindex=g.getColumnModel().getDataIndex(j);
        if(dataindex == "transactionID"){
            var formrec = this.Store.getAt(i);
            var type=formrec.data['type'];
            var withoutinventory=formrec.data['withoutinventory'];  
            var billid=formrec.data['billid'];
            if (type == "Debit Note" || type == "Credit Note") {
                var billid = formrec.data['noteid'];
            }
            if(type !='' && type != null && type != undefined){
                viewTransactionTemplate1(type, formrec,withoutinventory,billid);            
            }    
        }else if(dataindex=='entryno'){
            var formrec = this.Store.getAt(i);
            var type=formrec.data['type'];
            var withoutinventory=formrec.data['withoutinventory'];  
            var jeid=formrec.data['journalentryid'];
            viewTransactionTemplate1(type, formrec,withoutinventory,jeid);    
        }
    },
    openNewTab:function(){
        if(this.JETYpe!=undefined){
            callJournalEntryTab(undefined,undefined,undefined,this.winType);
        }else{
            callJournalEntry();
        }
    },  
    expandCollapseGrid : function(btntext){
        if(btntext == WtfGlobal.getLocaleText("acc.field.Collapse")){
            for(var i=0; i< this.grid.getStore().data.length; i++){
                this.expander.collapseRow(i)
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if(btntext == WtfGlobal.getLocaleText("acc.field.Expand")){
            for(var i=0; i< this.grid.getStore().data.length; i++){
                this.expander.expandRow(i)
            }
           this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    },
    repeateJEHandler:function(){
        var formrec=null;
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox(15,2);
            return;
        }
        formrec = this.grid.getSelectionModel().getSelected();
        if(this.grid.getSelectionModel().getCount()==1 && formrec.data.isRepeated){
            var msg = "<b>"+formrec.data.entryno + WtfGlobal.getLocaleText("</b>  has already set a recurring, so cannot be recurred.");
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2)
            return;
        }
        var isaddRecurringJE=false;
        var isEdit=false;
        callRepeatedJEWindow(formrec.data.withoutinventory, formrec ,isaddRecurringJE, isEdit);  
        
    },
    loadTypeStore:function(a,rec){
        this.deleted=false;
        this.nondeleted=false;
        this.cashtype = "all";
        var index=rec.data.typeid;
        if(this.deleteJe){this.deleteJe.enable();}
        if(this.eliminateBtn){this.eliminateBtn.enable();}
        if(index==2){
            this.nondeleted=true;
        }else if(index==1){
             this.deleted=true;
            if(this.deleteJe){this.deleteJe.disable();}
            if(this.eliminateBtn){this.eliminateBtn.disable();}
        } else {            
             if(index == 3) {
                 this.cashtype = "Cash Receipt Journal";
             } else if(index == 4) {
                 this.cashtype = "Cash Disbursement Journal";
             } else if(index == 5) {
                 this.cashtype = "Sales & Receivable Journal";
             } else if(index == 6) {
                 this.cashtype = "Purchase & Payable Journal";
             } else if(index == 7) {
                 this.cashtype = "Party Journal";
            } else if (index == 8) {
                this.cashtype = "Fund Transfer";
            } else if (index == 9) {
                this.cashtype = "Normal Journal";
            } else if (index == 10) {
                this.cashtype = Wtf.GSTJEType.TDS;
            } else if (index == 11) {
                this.cashtype = Wtf.GSTJEType.TCS;
            } else if (index == 12) {
                this.cashtype = Wtf.GSTJEType.ITC;
            }
        }
        this.Store.load({params:{
            start:0,
            limit:this.pP.combo.value,
            ss : this.quickPanelSearch.getValue()
        }});
       WtfComMsgBox(29,4,true);
        this.Store.on('load',this.storeloaded,this);
    },
    setCostCenter: function(){
        this.costCenter.setValue(this.costCenterId);//Select Default Cost Center
        Wtf.CostCenterStore.un("load", this.setCostCenter, this);
    },
    
    handleCallFromAuditTrail : function (userid) {
        this.userid = userid;
        if (this.User) {
            this.User.setValue(userid);
        }
      if (userid) {
            this.Store.load({params: {
                    start: 0,
                    limit: this.pP.combo.value,
                    ss: this.quickPanelSearch.getValue()
                }});
        }
    },
     enableDisableButtons:function(){
       var recs = this.grid.getSelectionModel().getSelections();
        if(this.deleteJe){            
            if(recs.length>=1){
                this.deleteJe.enable();
            }else{
                this.deleteJe.disable();
            }
        }
        if(this.deleteBtn) {
            this.deleteBtn.enable();
        }
        if(this.eliminateBtn){this.eliminateBtn.enable();}
        var arr=this.grid.getSelectionModel().getSelections();
        
        for(var i=0;i<arr.length;i++){
            if(arr[i]&&arr[i].data.deleted) {
                if(this.deleteBtn){this.deleteBtn.disable();}
                break;
            }
            if(arr[i]&&arr[i].data.eliminateflag) {
                if(this.eliminateBtn){this.eliminateBtn.disable();}
                break;
            }
        }
        
        if(arr!=undefined&&arr.length==1&&arr[0].data.typeValue==3 && Wtf.templateflag == 1){
//          alert(arr.length);
           if(this.singlePrint){this.singlePrint.enable();}
        }
        else{
            if(this.singlePrint){this.singlePrint.disable();}
        }
        
        if(this.editBtn){
            if(recs.length== 1 && recs[0].data!=null&&recs[0].data!=undefined && recs[0].data.isFromEclaim!=null && recs[0].data.isFromEclaim!=undefined && recs[0].data.isFromEclaim){
                 this.editBtn.disable();
            }else if(recs.length== 1 && recs[0].data.transactionID =="" && arr[0].data.typeValue==2 && (!recs[0].data.deleted)){//party journal entry
                this.editBtn.enable();
            }else if(recs.length== 1 &&recs[0].data.transactionID =="" &&recs[0].data.memo != 'Fixed Asset Entry' && (!recs[0].data.deleted)  && recs[0].data.transactionDetails !='Exchange Gains/Loss JE' && !(recs[0].data.isdisposalje) && recs[0].data.typeValue!=4){
                this.editBtn.enable();
                this.reverseBtn.enable();
                this.RepeateJE.enable();
            } else {
                this.editBtn.disable();
                this.reverseBtn.disable();
                 this.RepeateJE.disable();
            }
             if(recs.length== 1 &&recs[0].data.revaluationid >=1){
                this.editBtn.disable();
                this.reverseBtn.disable();
                this.RepeateJE.disable();
            } 
            
        }
        if(this.copyBtn){
            if(recs.length== 1 && (!recs[0].data.deleted) && (recs[0].data.typeValue)>0 && recs[0].data.typeValue!=4){ //ERP-24305
                this.copyBtn.enable();
            }else {
                this.copyBtn.disable();
            }
        }
        if(this.pendingApproval){
        if(this.approveJEBttn){
            if(recs.length==1 && !recs[0].data.deleted){
                this.approveJEBttn.enable();
            } else {
                this.approveJEBttn.disable();
            }
        }
        if(this.rejectJEBttn){
            if(recs.length!=0 ){
                this.rejectJEBttn.disable();
                for(var i=0;i<recs.length;i++){
                if(!recs[i].data.deleted){
                    this.rejectJEBttn.enable();  // Reject button will be enabled if atleast on of the selected record is non-rejected/non-deleted
                    this.deleteJe.disable();
                }
                }
            } else {
                this.rejectJEBttn.disable();
                    this.deleteJe.enable();
            }
        }
    } else {
        if(this.approvalHistoryBtn){
            if(recs.length==1){
                this.approvalHistoryBtn.enable();
            } else {
                this.approvalHistoryBtn.disable();
            }
        }
    }
    var rec = this.sm.getSelected();
    if(rec!=undefined && rec!="" && rec.data.typeValue==3 && this.sm.getCount()==1 &&rec.data.pmtmethod!="" && rec.data.pmtmethodtype==Wtf.bank_detail_type ) {//enabling the print cheque button for bank type
        if(this.printCheck)this.printCheck.enable();
    }else{
        if(this.printCheck)this.printCheck.disable();
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
        }
    },
    
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
        
    },
    filterStore: function(json, filterConjuctionCriteria) {
        /**
         * ERP-33751 - Start Date Required for saved Search
         */
        this.objsearchComponent.advGrid.sdate = this.startDate.getValue(); 
        this.objsearchComponent.advGrid.edate = this.endDate.getValue();
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.Store.baseParams = {
            pendingApproval:this.pendingApproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
                }
            });
    },
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            pendingApproval:this.pendingApproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(), 
                start: 0, 
                limit: this.pP.combo.value
                }
            });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    
    dataChanged:function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    },

    dateRenderer:function(v){
        if(v) return v.format('Y M d');
        return "";
    },

    onRowexpand:function(scope, record, body, rowIndex){
        this.expanderBody=body;
        this.fillExpanderBody(record);
    },

    fillExpanderBody:function(record){
        var disHtml = "";
        var arr=[];
        //ERP-9645
        arr=[WtfGlobal.getLocaleText("acc.jeList.expandJE.accName"),'&nbsp;','&nbsp;','<div align=right>'+WtfGlobal.getLocaleText("acc.jeList.expandJE.amtDebit")+'</div>','<div align=right>'+WtfGlobal.getLocaleText("acc.jeList.expandJE.amtCredit")+'</div>','<div align=center>'+WtfGlobal.getLocaleText("acc.jeList.expandJE.desc")+'</div>'];
        var header = "<span class='gridHeader'>"+WtfGlobal.getLocaleText("acc.jeList.expandJE.transList")+"</span>";
        var custArr = [];
//        if(record.data.typeValue!=0){           // add custom columns for manual,party,fund trasfer JE records only.
            custArr = WtfGlobal.appendCustomColumn(custArr,GlobalColumnModel[Wtf.Acc_GENERAL_LEDGER_ModuleId]);
//        } //commented for ERP-31122
        var arrayLength=arr.length;
        for(i=0;i<custArr.length;i++){
            if(custArr[i].header != undefined )
               arr[arrayLength+i]=custArr[i].header;
        }
        var count=0;
        for(var i=0;i<arr.length;i++){
            if(arr[i] != ""){
                count++;
            }
        }
        count=count+2;
        var widthInPercent=100/count;
        var minWidth = count*100;
        header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
        for(var i=0;i<arr.length;i++){
            header += "<span class='headerRow'  "+(i==0?"style='width:"+widthInPercent*3+"% ! important;'":"style='width:"+widthInPercent+"% ! important;'")+">" + arr[i] + "</span>";
        }
        header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLineforJE'></span></div>";   
        header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
        var jeDetails = record.data.jeDetails;
        for(i=0;i<jeDetails.length;i++){
            var rec=jeDetails[i];
            var accname="";
            if(rec["debit"]=='Debit'){
                accname= "<span class='gridRow' style='width: "+widthInPercent*3+"% ! important;'  wtf:qtip='"+rec['accountname']+((rec['customerVendorName']!=undefined)?("("+rec['customerVendorName']+")"):"")+"'>"+Wtf.util.Format.ellipsis(rec['accountname']+((rec['customerVendorName']!=undefined)?("("+rec['customerVendorName']+")"):""),35)+"&nbsp;</span>";
                accname += "<span class='gridRow'  style='width: "+widthInPercent+"% ! important;'>&nbsp;</span>";
                accname += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>Dr</span>";
            }else{
                accname="<span class='gridRow' style='padding-left:"+((widthInPercent*3)/5)+"% ; width: "+((widthInPercent*3)-((widthInPercent*3)/5))+"% ! important;'  wtf:qtip='"+rec['accountname']+((rec['customerVendorName']!=undefined)?("("+rec['customerVendorName']+")"):"")+"'>To "+Wtf.util.Format.ellipsis(rec['accountname']+((rec['customerVendorName']!=undefined)?("("+rec['customerVendorName']+")"):""),35)+"&nbsp;</span>";
                accname += "<span class='gridRow'  style='width: "+widthInPercent+"% ! important;'>&nbsp;</span>";
                accname += "<span class='gridRow'  style='width: "+widthInPercent+"% ! important;'>&nbsp;</span>";
            }
            header += accname;
            var desc = '';
            if(rec['description']){
                desc = rec['description'];
            }
            header += "<span class='gridRow'  style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.currencyRenderer((rec['d_amount']!=undefined)?rec['d_amount']:"")+"&nbsp;</span>";
            header += "<span class='gridRow'  style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.currencyRenderer((rec['c_amount']!=undefined)?rec['c_amount']:"")+"&nbsp;</span>";
            
            if(desc!="")
                //ERP-9645
                header += "<span class='gridRow' style='display:inline-block;text-align:center;width: "+widthInPercent+"% ! important;'  wtf:qtip='"+desc.replace(/['"]/g, '')+"'>"+Wtf.util.Format.ellipsis(WtfGlobal.HTMLStripper(desc),15)+"</span>";
            else
                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;</span>";
            
        for(var j=0;j<custArr.length;j++){
                    if(rec[custArr[j].dataIndex]!=undefined && rec[custArr[j].dataIndex]!="null" && rec[custArr[j].dataIndex]!="")
                        header += "<span class='gridRow'style='width: "+widthInPercent+"% ! important;'  wtf:qtip='"+rec[custArr[j].dataIndex]+"'>"+Wtf.util.Format.ellipsis(rec[custArr[j].dataIndex],15)+"&nbsp;</span>";
                   else
                        header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;&nbsp;</span>";
                }
            header +="<br>";
        }
        header += "</div>";
        disHtml += "<div class='expanderContainer1'>" + header + "</div>";
        this.expanderBody.innerHTML = disHtml;
    },
    expandJournalEntry:function(id,exponly,startDate, endDate){
        this.entryID=id;
        this.exponly=exponly;
        if(startDate!=null && startDate!=undefined)
            this.startDate.setValue(startDate);
        if(endDate!=null && endDate!=undefined)
            this.endDate.setValue(endDate);
        if(this.entryID==undefined){//Journal Entry Report View
            this.quickPanelSearch.show();
            this.resetBttn.show();
            this.typeEditor.show();
            this.pagingToolbar.show();
//            this.Store.load({params:{start:0,limit:this.pagingToolbar.pageSize}}); //Stop Auto Load
        } else {//Link to Journal Entry, Single JE View
            this.quickPanelSearch.hide();
            this.resetBttn.hide();
            this.typeEditor.hide();
            this.pagingToolbar.hide();
            this.Store.load({params:{linkid:this.entryID}});
                }
        this.grid.getView().applyEmptyText();// Show empty text on grid after opening report
//        WtfComMsgBox(29,4,true);
    },
    expandRow:function(id){
        Wtf.MessageBox.hide();
        this.Store.filter('journalentryid',this.entryID);
        if(this.exponly)
            this.expander.toggleRow(0);
    },
  deleteTransactionCheckBefore:function(del){                           //check whether record Mapped with BR OR BUR 
   this.recArr = this.grid.getSelectionModel().getSelections();
    var biilNo;
    var isFirst=true;
    for(var k=0;k< this.recArr.length;k++){
        if( (this.recArr[k].data.isBR || this.recArr[k].data.isBUR) && isFirst ){
            biilNo=this.recArr[k].data.entryno;
            isFirst=false;
        }else if(this.recArr[k].data.isBR ||this.recArr[k].data.isBUR){
            biilNo+=", "+this.recArr[k].data.entryno;
        }
    }
   if(biilNo !=undefined){
       Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.rem.251")+" "+biilNo+" "+WtfGlobal.getLocaleText("acc.ven.msg4"),function(btn){
           if(btn=="yes") {
               this.performDelete(del);
           }else{
               return;
           }
       },this);
   } else {
       this.performDelete(del);
   }
},
    performDelete:function(del){
        if(this.grid.getSelectionModel().hasSelection()==false){
            WtfComMsgBox(34,2);
            return;
        }
        var delFlag=del;
        var data=[];
        var arr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();        
        var checkHasReverseJeFlag=false;var jeNO="";
        var checkHasRecurringJeFlag=false, checkHasAssemblyTypeJE=false, checkHasDisassemblyTypeJE=false;
        var isRoundingJE = false;
        for(var i=0;i<this.recArr.length;i++){
                    var rec=this.recArr[i];
                    if(rec.data.revaluationid>=1 ){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.YoucannotdeleteRe-evaluationJournalentry")], 2);
                        return;
                        
                    }
                    if(rec.data.reversejeno!="" && rec.data.reversejeno.length>0 && !rec.data.isreverseje){
                        checkHasReverseJeFlag=true;
                        jeNO=rec.data.entryno;
                        break;
                    }
                    if(rec.data.isonetimereverse){  //Source JE cannot delete if it has reverse JE.
                        checkHasReverseJeFlag=true;
                        jeNO=rec.data.entryno;
                        break;
                    }
                    if(rec.data.isRepeated){  //Source JE cannot delete if it has recur JE.
                        checkHasRecurringJeFlag=true;
                        jeNO=rec.data.entryno;
                        break;
                    }
                    if(rec.data.type=="Product Assembly"){  //Build Assembly JE Cannot delete
                        checkHasAssemblyTypeJE=true;
                        jeNO=rec.data.entryno;
                        break;
                    }if(rec.data.type=="Product Disassembly"){  //Unbuild Assembly JE Cannot delete
                        checkHasDisassemblyTypeJE=true;
                        jeNO=rec.data.entryno;
                        break;
                    }
                    if(rec.data.typeValue === 4){// Rounding JE cannot be deleted by user 
                        isRoundingJE = true;
                        jeNO=rec.data.entryno;
                        break;
                    }
                }
                if(isRoundingJE){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.roundingje.cannotdeleteroundingje")+" "+jeNO], 2);
                }else if(checkHasRecurringJeFlag){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.je.tabTitle")+" <b>"+jeNO+"</b> "+WtfGlobal.getLocaleText("acc.field.hasrecurringJournalEntrypostedsocannotbedeleted")], 2);
                }else if(checkHasAssemblyTypeJE){   //Assembly JE
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.buildje.delete.msg")+" <b>"+jeNO+"</b> "+WtfGlobal.getLocaleText("acc.assemblyproduct.deletemsg")], 2);
                }else if(checkHasDisassemblyTypeJE){    //Disassembly JE
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.unbuildje.delete.msg")+" <b>"+jeNO+"</b> "+WtfGlobal.getLocaleText("acc.assemblyproduct.deletemsg")], 2);
                }else if(checkHasReverseJeFlag){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.je.tabTitle")+" <b>"+jeNO+"</b> "+WtfGlobal.getLocaleText("acc.field.hasreverseJournalEntrypostedsocannotbedeleted")], 2);
                }else {
                    WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.rem.13"),function(btn){
                        if(btn!="yes") {
                            for(var i=0;i<this.recArr.length;i++){
                                var ind=this.Store.indexOf(this.recArr[i])
                                var num= ind%2;
                                WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
                            }
                            return;
                        }
                        for(i=0;i<this.recArr.length;i++){
                            arr.push(this.Store.indexOf(this.recArr[i]));
                        }
                        data= WtfGlobal.getJSONArray(this.grid,true,arr);
                        var jeFlag=false;
                        if(delFlag=='del')
                            jeFlag=true;
                        WtfGlobal.setAjaxTimeOutFor30Minutes();
                        Wtf.Ajax.requestEx({
                            //                url: Wtf.req.account+'CompanyManager.jsp',
                               url: "ACCJournalCMN/deleteJournalEntries.do",
                            params:{
                                data:data,
                                jeFlag : jeFlag,//sending the flag true whenever you want to delete record permanently
                                mode:205
                            }
                        },this,this.genSuccessResponse,this.genFailureResponse);
                    },this);
                }
},
    
    performEliminate:function(){
        if(this.grid.getSelectionModel().hasSelection()==false){
            WtfComMsgBox(34,2);
            return;
        }
        var data=[];
        var arr=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.rem.219"),function(btn){
            if(btn!="yes") {
                for(var i=0;i<this.recArr.length;i++){
                    var ind=this.Store.indexOf(this.recArr[i])
                    var num= ind%2;
                    WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
                }
                return;
            }
            for(i=0;i<this.recArr.length;i++){
                arr.push(this.Store.indexOf(this.recArr[i]));
            }
            data= WtfGlobal.getJSONArray(this.grid,true,arr);
            Wtf.Ajax.requestEx({
                url: "ACCJournalCMN/eliminateJournalEntries.do",
                params:{
                    data:data,
                    mode:205
                }
            },this,this.genSuccessResponse,this.genFailureResponse);
        },this);
    },

    loadJEStore:function(){
        this.Store.load({
           params : {
               start : 0,
               limit : this.pP.combo.value,
               ss : this.quickPanelSearch.getValue(),
               startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
               enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
           }
       });
       this.Store.on('load',this.storeloaded,this);
    },

    storeloaded:function(store){
        Wtf.MessageBox.hide();
        this.quickPanelSearch.StorageChanged(store);
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        if(this.isUserSummaryReportFlag){
            var grandTotalinbase = store.reader.jsonData.grandTotal; //APPENDED TO RESOPNSE's LAST RECORD(SDP-1767)
            this.GrandTotalSummary.overwrite(this.GrandTotalSummaryTPL.body, {
                creditTotal: WtfGlobal.withoutRateCurrencySymbol(grandTotalinbase),
                debitTotal: WtfGlobal.withoutRateCurrencySymbol(grandTotalinbase)
            });
        }
    },
    approvePendingJE: function() {
    var formRecord = this.grid.getSelectionModel().getSelected();
    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected") +" "+ WtfGlobal.getLocaleText("acc.je.tabTitle") + "?", function(btn) {

        if (btn == "yes") {
            var URL = "ACCJournal/approveJournalEntry.do";
            var formRecord = this.grid.getSelectionModel().getSelected();
            this.remarkWin = new Wtf.Window({
                height: 270,
                width: 360,
                maxLength: 1000,
                title: WtfGlobal.getLocaleText("acc.field.ApprovependingJE"), // "Approve pending JE"
                bodyStyle: 'padding:5px;background-color:#f1f1f1;',
                autoScroll: true,
                allowBlank: false,
                layout: 'border',
                items: [{
                    region: 'north',
                    border: false,
                    height: 70,
                    bodyStyle: 'background-color:#ffffff;border-bottom:1px solid #bfbfbf;',
                    html: getTopHtml(WtfGlobal.getLocaleText("acc.field.ApprovePending") +" "+ WtfGlobal.getLocaleText("acc.je.tabTitle"), WtfGlobal.getLocaleText("acc.field.ApprovePending") +" "+ WtfGlobal.getLocaleText("acc.je.tabTitle") + " <b>" + formRecord.data.entryno + "</b>", "../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
                }, {
                    region: 'center',
                    border: false,
                    layout: 'form',
                    bodyStyle: 'padding:5px;',
                    items: [this.remarkField = new Wtf.form.TextArea({
                        fieldLabel: WtfGlobal.getLocaleText("acc.field.AddRemark*"),
                        width: 200,
                        height: 100,
                        allowBlank: false,
                        maxLength: 1024
                    })]
                }],
                modal: true,
                buttons: [{
                    text: WtfGlobal.getLocaleText("acc.cc.24"),
                    scope: this,
                    handler: function() {
//                        if (this.remarkField.getValue().trim() == "") {
//                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleaseenterremark")], 2);
//                            return;
//                        }
//
//                        if (!this.remarkField.isValid()) {
//                            this.remarkField.markInvalid(WtfGlobal.getLocaleText("acc.field.Maximumlengthofthisfieldis1024"));
//                            return;
//                        }

                        Wtf.Ajax.requestEx({
                            url: URL,
                            params: {
                                billid:formRecord.data.journalentryid,
                                remark: this.remarkField.getValue(),
                                totalorderamount:formRecord.data.jeDetails[0].d_amount  //Amout in base Currency
                            }
                        }, this, this.genSuccessResponseApproveInv, this.genFailureResponseApproveInv);
                    }
                }, {
                    text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                    scope: this,
                    handler: function() {
                        this.remarkWin.close();
                    }
                }]
            });
            this.remarkWin.show();
        }
    }, this)
},
    genSuccessResponseApproveInv: function(response) {
        this.remarkWin.close();
        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function(){
            this.loadStore();
        }, this);
    },
    genFailureResponseApproveInv: function(response) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")], 2);
    },
    handleReject: function() {
        var data = [];
        var arr = [];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid, this.recArr, true, 0, 2);
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttorejectselected") + WtfGlobal.getLocaleText("acc.je.tabTitle") + "?", function(btn) {
            if (btn != "yes") {
            for (var i = 0; i < this.recArr.length; i++) {
                var ind = this.Store.indexOf(this.recArr[i])
                var num = ind % 2;
                WtfGlobal.highLightRowColor(this.grid, this.recArr[i], false, num, 2, true);
            }
            return;
           }
            for (i = 0; i < this.recArr.length; i++) {
                arr.push(this.Store.indexOf(this.recArr[i]));
            }

            data = WtfGlobal.getJSONArray(this.grid, true, arr);
            Wtf.Ajax.requestEx({
                url: "ACCJournal/rejectPendingJE.do",
                params: {
                    data: data
                }
            }, this, this.genSuccessResponseReject, this.genFailureResponseReject);
        }, this);
    },
    viewApprovalHistory : function(){
        var rec = this.sm.getSelected();
        Wtf.Ajax.requestEx({
            url:"ACCReports/getApprovalhistory.do",
            params: {
                billid : rec.data.journalentryid
            }
        },this,function(response, request){
            var historyWin = new Wtf.Window({
                height : 300,
                width : 400,
                iconCls :getButtonIconCls(Wtf.etype.deskera),
                title : WtfGlobal.getLocaleText("acc.field.ApprovalHistory"),
                bodyStyle : 'padding:5px;background-color:#ffffff;',
                layout : 'border',
                items : [{
                    region : 'north',
                    border:false,
                    height:70,
                    bodyStyle : 'background-color:#ffffff;border-bottom:1px solid #bfbfbf;',
                    html:getTopHtml(WtfGlobal.getLocaleText("acc.field.ApprovalHistory"),WtfGlobal.getLocaleText("acc.field.ApproveHistoryof") +WtfGlobal.getLocaleText("acc.je.tabTitle") +" <b>"+rec.data.entryno+"</b>"  ,"../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
                }, {
                    region : 'center',
                    border : false,
                    autoScroll : true,
                    bodyStyle : 'padding:5px;background-color:#f1f1f1;',
                    html : WtfGlobal.getLocaleText(response.msg)
                }],
                buttons : [{
                    text : WtfGlobal.getLocaleText("acc.common.close"),
                    handler : function(){
                        historyWin.close();
                    }
                }],
                autoScroll : true,
                modal : true
            });

            historyWin.show();
        },function(response, request){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")],2);
        });
    },
    genSuccessResponseReject: function(response) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.je.tabTitle") + " "+ WtfGlobal.getLocaleText("acc.field.hasbeenrejectedsuccessfully")], response.success * 2 + 1);
        for (var i = 0; i < this.recArr.length; i++) {
            var ind = this.Store.indexOf(this.recArr[i])
            var num = ind % 2;
            WtfGlobal.highLightRowColor(this.grid, this.recArr[i], false, num, 2, true);
        }
        if (response.success) {
            (function() {
                this.loadStore();
            }).defer(WtfGlobal.gridReloadDelay(), this);
        }
    },
    genFailureResponseReject: function(response) {
        for (var i = 0; i < this.recArr.length; i++) {
            var ind = this.Store.indexOf(this.recArr[i])
            var num = ind % 2;
            WtfGlobal.highLightRowColor(this.grid, this.recArr[i], false, num, 2, true);
        }
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    genSuccessResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        var superThis = this;
        var msg = response.msg;
        var iconType=(response.warning)?2:response.success * 2 + 1; //  Warning:2 ,Success:3
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.je.tabTitle"), msg], iconType, "", "", function (btn){
            if((btn=="ok" || btn=="cancel")&& response.success){
                superThis.loadJEStore();
            }
        });
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.Store.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
//        if(response.success){
//            (function(){
//                this.loadJEStore();
//            }).defer(WtfGlobal.gridReloadDelay(),this);
//        }
    },

    genFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.Store.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    loadStore:function(){
       this.Store.load({
           params : {
               start : 0,
               limit : this.pP.combo.value,
               ss : this.quickPanelSearch.getValue()
           }
       });
       this.Store.on('load',this.storeloaded,this);
    },
    printCkeck:function(){
        this.print=true;
        var formRecord = null;  
        formRecord = this.grid.getSelectionModel().getSelected();
        if(formRecord.get("ischequeprinted")==true){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.printCheque"),WtfGlobal.getLocaleText("acc.je.printmsg"),function(btn){
            if(btn!="yes") {
                return;
            }              
            this.confirmPrint(formRecord);
            },this);
        }else{
            this.confirmPrint(formRecord);
        }
    }, 
    
    confirmPrint:function(formRecord){
        var amount=formRecord.data.jeDetails[0].d_amount_transactioncurrency ? formRecord.data.jeDetails[0].d_amount_transactioncurrency : formRecord.data.jeDetails[0].c_amount_transactioncurrency;
        var Printdate=WtfGlobal.convertToGenericDate(formRecord.get("chequeDate"));
        var paymentMethod=formRecord.get("pmtmethod");
        var name=formRecord.get("paidTo");
        var chequeno=formRecord.get("chequeNumber");    
        var jeid=formRecord.get("journalentryid");
        var jeno=formRecord.get("entryno");
        var currencyid=formRecord.get("currencyid");
        this.printUrl = "ACCJournal/printCheck.do";              
        if(formRecord.get("typeValue")=="3"){
            name=formRecord.get("paidTo");
            if(formRecord.get("paidToCmb")==undefined||formRecord.get("paidToCmb")==""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.PleaseEnterPaidto")],2);     
                return;
            } 
            if(formRecord.get("pmtmethod")==undefined||formRecord.get("pmtmethod")==""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.PleaseEnterPaymentmethod")],2);     
                return;
            } 
        }
        Wtf.Ajax.requestEx({
            url:this.printUrl,                
            params:{
                amount:amount,
                Printdate:Printdate,
                paymentMethod:paymentMethod,
                name:name,
                currencyid:currencyid,
                jeid:jeid,
                jeno:jeno,
                chequeno:chequeno
            }
        },this,this.genPrintSuccessResponse,this.genFailureResponse);  
    },
        genPrintSuccessResponse:function(response){
    if (response.data) {
        var resdata = response.data[0];
        this.printDetailswin=new Wtf.account.ReceiptEntry({
            });
        this.printDetailswin.printCheque(resdata);  //Passing Paramter as JSON Object
        this.print = false;
}
    },
    reverseJEHandler:function(){
                var record = this.grid.getSelectionModel().getSelected();
                var entrydate = record.data.entrydate;
                var activefromDate = new Date(Wtf.account.companyAccountPref.activeDateRangeFromDate);
                var minFromDate = ((activefromDate!=undefined && activefromDate!=null && activefromDate > entrydate) ? activefromDate : entrydate);
                var activeToDate = new Date(Wtf.account.companyAccountPref.activeDateRangeToDate);
                var maxToDate = (activeToDate!=undefined && activeToDate!=null && activeToDate > entrydate) ? activeToDate : undefined;
                
                if((!record.data.isreverseje) && (!record.data.isonetimereverse)){     //if(!record.data.isonetimereverse){ this check is to generate reverse a reverse journal entry.
                    var msg = WtfGlobal.getLocaleText("acc.field.AreyousureyouwanttoreversetheselectedJournalEntry");                
                    Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), msg,function(btn){
                        if(btn=="yes") {
                            this.memoWin = new Wtf.Window({
                                height : 300,
                                width : 360,
                                maxLength : 1000,
                                title : WtfGlobal.getLocaleText("acc.field.EnterMemo"),
                                bodyStyle : 'padding:5px;background-color:#f1f1f1;',
                                autoScroll : true,
                                allowBlank : false,
                                layout : 'border',
                                items : [
                                {
                                    region : 'north',
                                    border:false,
                                    height:70,
                                    bodyStyle : 'background-color:#ffffff;border-bottom:1px solid #bfbfbf;',
                                    html:getTopHtml(WtfGlobal.getLocaleText("acc.field.EnterMemo") ,WtfGlobal.getLocaleText("acc.field.EnterMemo"), "../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
                                },
                                {
                                    region : 'center',
                                    border:false,
                                    layout : 'form',
                                    bodyStyle : 'padding:5px;',
                                    items : [this.reverseEntryDate=new Wtf.form.DateField({  //SDP-11553
                                        fieldLabel : WtfGlobal.getLocaleText("acc.field.ReverseJEDate"),
                                        width : 200,
                                        height : 50,
                                        allowBlank : false,
                                        minValue : minFromDate,
                                        maxValue : maxToDate,
                                        format:WtfGlobal.getOnlyDateFormat()
                                    }), this.memoField =new Wtf.form.TextArea({
                                        fieldLabel : WtfGlobal.getLocaleText("acc.writeOff.AddMemo"),
                                        width : 200,
                                        height : 100,
                                        value:record.data.memo,
                                        allowBlank : true,
                                        maxLength : 1024
                                    })]
                                }],
                                modal : true,
                                buttons : [  {
                                    text : WtfGlobal.getLocaleText("acc.common.submit"),
                                    scope : this,
                                    id: "Approvebtn"+this.id,
                                    handler : function(){
                                        //           
                                        if (!this.reverseEntryDate.isValid()) {
                                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.PleaseEnterReverseJEDate")], 2);
                                            return;
                                        }
                                        Wtf.getCmp("Approvebtn"+this.id).disable();
                                        Wtf.Ajax.requestEx({
                                            url:"ACCJournal/saveReverseJournalEntry.do",
                                            params: {
                                                jeId :record.data.journalentryid,
                                                memo : this.memoField.getValue(),
                                                entrydate : (this.reverseEntryDate!=undefined || this.reverseEntryDate.getValue()!=null || this.reverseEntryDate.getValue()!="") ? WtfGlobal.convertToGenericStartDate(this.reverseEntryDate.getValue()) : WtfGlobal.convertToGenericStartDate(new Date())  //SDP-11553
                                            }
                                        },this,this.genSuccessResponseStat,this.genFailureResponseStat);
                                    }
                                },{
                                    text : WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                                    scope : this,
                                    handler : function(){
                                        this.memoWin.close();
                                    }
                                }]
                            });
                            this.memoWin.show();
                        }
                }, this)                
            }else{
                if(!record.data.isonetimereverse){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.YoucannotreverseaReverseJournalEntry")], 2);
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.reverserecurring.onlyonetime")], 2);
                }
            }//else
        },
    getMyConfig:function (){
        WtfGlobal.getGridConfig (this.grid, this.moduleid, false, false, true);

        var statusForCrossLinkage = this.grid.getColumnModel().findColumnIndex("statusforcrosslinkage");
        if (statusForCrossLinkage != -1) {
            this.grid.getColumnModel().setHidden(statusForCrossLinkage, true);
        }
    },
    saveMyStateHandler: function (grid,state){
        WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleid, grid.gridConfigId, false);
    }
});
Wtf.account.JournalEntryCustomerDetailPanel = function (config) {
    this.businessPerson = 'Customer';
    Wtf.apply(this, config);

    this.GridRec = Wtf.data.Record.create([
        {name: 'journalentryid'},
        {name: 'entryno'},
        {name: 'sequenceformatid'},
        {name: 'companyname'},
        {name: 'companyid'},
        {name: 'eliminateflag'},
        {name: 'deleted'},
        {name: 'entrydate', type: 'date'},
        {name: 'memo'},
        {name: 'jeDetails'},
        {name: 'NoOfJEpost'},
        {name: 'NoOfRemainJEpost'},
        {name: 'transactionID'},
        {name: 'billid'},
        {name: 'noteid'},
        {name: 'type'},
        {name: 'transactionDetails'},
        {name: 'costcenter'},
        {name: 'currencyid'},
        {name: 'creditDays'},
        {name: 'isRepeated'},
        {name: 'childCount'},
        {name: 'interval'},
        {name: 'intervalType'},
        {name: 'startDate', type: 'date'},
        {name: 'nextDate', type: 'date'},
        {name: 'expireDate', type: 'date'},
        {name: 'repeateid'},
        {name: 'parentje'},
        {name: 'isreverseje', type: 'boolean'},
        {name: 'reversejeno'},
        {name: 'withoutinventory'},
        {name: 'revaluationid'},
        {name: 'externalcurrencyrate'},
        {name: 'typeValue'},
        {name: 'chequeNumber'},
        {name: 'bankName'},
        {name: 'chequeDate', type: 'date'},
        {name: 'description'},
        {name: 'partlyJeEntryWithCnDn'},
        {name: 'DNsequenceformatid'},
        {name: 'CNsequenceformatid'},
        {name: 'dnNumber'},
        {name: 'cnNumber'},
        {name: 'iscnused'},
        {name: 'isdnused'},
        {name: 'approvalstatus'},
        {name: 'ischequeprinted', type: 'boolean'},
        {name: 'paidTo'},
        {name: 'paidToCmb'},
        {name: 'pmtmethodaccountname'},
        {name: 'pmtmethod'},
        {name: 'cntype'},
        {name: 'customerid'},
        {name: 'customername'},
        {name: 'amountinbase'},
    ]);
    this.StoreUrl = "ACCReports/getJournalEntry.do";

    this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
        name: 'stdate' + this.id,
        format: WtfGlobal.getOnlyDateFormat(),
        value: WtfGlobal.getDates(true)
    });

    this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), //'To',
        format: WtfGlobal.getOnlyDateFormat(),
        name: 'enddate' + this.id,
        value: WtfGlobal.getDates(false)
    });


    this.personRec = new Wtf.data.Record.create([
        {
            name: 'accid'
        }, {
            name: 'accname'
        }, {
            name: 'acccode'
        }, {
            name: 'termdays'
        }, {
            name: 'billto'
        }, {
            name: 'currencysymbol'
        }, {
            name: 'currencyname'
        }, {
            name: 'currencyid'
        }, {
            name: 'deleted'
        }
    ]);

    this.customerAccStore = new Wtf.data.Store({
        url: "ACCCustomer/getCustomersForCombo.do",
        baseParams: {
            mode: 2,
            group: 10,
            deleted: false,
            nondeleted: true,
            common: '1'
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            autoLoad: false
        }, this.personRec)
    });

    this.CustomerComboconfig = {
        hiddenName: this.businessPerson.toLowerCase(),
        store: this.customerAccStore,
        valueField: 'accid',
        hideLabel: true,
        displayField: 'accname',
        emptyText: WtfGlobal.getLocaleText("acc.inv.cus"),
        mode: 'local',
        typeAhead: true,
        selectOnFocus: true,
        triggerAction: 'all',
        scope: this
    };

    this.Name = new Wtf.common.Select(Wtf.applyIf({
        multiSelect: true,
        fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.cust") + '*',
        forceSelection: true,
        extraFields: Wtf.account.companyAccountPref.accountsWithCode ? ['acccode'] : [],
        extraComparisionField: 'acccode', // type ahead search on acccode as well.
        listWidth: Wtf.account.companyAccountPref.accountsWithCode ? 350 : 250,
        width: 240
    }, this.CustomerComboconfig));

    this.customerAccStore.on("load", function (store) {
        var record = new this.personRec({
            accid: "All",
            accname: "All Customers"
        });
        this.Name.store.insert(0, record);
        this.Name.setValue("All");
        this.loaddata();
    }, this);

    this.Name.on('select', function (combo, personRec) {
        if (personRec.get('accid') == 'All') {
            combo.clearValue();
            combo.setValue('All');
        } else if (combo.getValue().indexOf('All') >= 0) {
            combo.clearValue();
            combo.setValue(personRec.get('accid'));
        }
    }, this);


    this.customerAccStore.load();
    this.Store = new Wtf.data.Store({
        url: this.StoreUrl,
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'count'
        }, this.GridRec)
    });



    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText: WtfGlobal.getLocaleText("acc.field.QuickSearchbyInvoice") + (this.iscustreport ? WtfGlobal.getLocaleText("acc.product.gridProduct") : this.isSalesPersonName ? WtfGlobal.getLocaleText("acc.masterConfig.15 ") : WtfGlobal.getLocaleText("acc.up.3")),
        width: 150,
        field: 'billno',
        Store: this.Store,
        hidden: true
    });

    this.typeEditor = new Wtf.form.ComboBox({
        store: this.typeStore,
        name: 'typeid',
        displayField: 'name',
        id: 'view' + config.helpmodeid, //+config.id,
        valueField: 'typeid',
        mode: 'local',
        defaultValue: 0,
        width: 160,
        listWidth: 160,
        triggerAction: 'all',
        typeAhead: true,
        selectOnFocus: true
    });

    this.pagingToolbar = new Wtf.PagingSearchToolbar({
        pageSize: 15,
        id: "pagingtoolbar" + this.id,
        store: this.Store,
        searchField: this.quickPanelSearch,
        displayInfo: true,
        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
        plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
    });

    this.Store.on('datachanged', function () {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    }, this);

    this.Store.on('beforeload', function () {
        var currentBaseParams = this.Store.baseParams;
        currentBaseParams.ss = this.quickPanelSearch.getValue();
        currentBaseParams.isCustomerReport = true;
        currentBaseParams.startdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());   //For UI Report  //ERP-8487
        currentBaseParams.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());         //For UI Report  //ERP-8487
        this.Store.baseParams = currentBaseParams;
    }, this);

    this.summary = new Wtf.ux.grid.GridSummary();
    this.gridColumnModelArr = [];
    this.gridColumnModelArr.push(new Wtf.grid.RowNumberer(), {
        header: WtfGlobal.getLocaleText("acc.jeList.gridEntryNo"), //"Entry Number",
        dataIndex: 'entryno',
        width: 150,
        pdfwidth:100,
        sortable: true,
        renderer: function (value, meta, rec) {
            meta.attr = "Wtf:qtip='" + value + "' Wtf:qtitle='Entry Number' ";
            if (!value)
                return value;
            if (rec.data.typeValue != 0) {  //if manual Journal enrty then provide link to view
                value = WtfGlobal.linkRenderer(value, meta, rec)
            }
            return value;

        }}, {
        header: WtfGlobal.getLocaleText("acc.jeList.gridEntryDate"), //"Entry Date",
        dataIndex: 'entrydate',
        width: 150,
        align: 'center',
        pdfwidth:100,
        sortable: true,
        renderer: WtfGlobal.onlyDateDeletedRenderer
    }, {
        header: WtfGlobal.getLocaleText("acc.cust.name"),
        pdfwidth:100,
        dataIndex: 'customername',
        width: 150,
        renderer: WtfGlobal.linkDeletedRenderer,
        summaryRenderer: function () {
            return '<div class="grid-summary-common">' + WtfGlobal.getLocaleText("acc.common.total") + '</div>'
        }
    },
    {
        header: WtfGlobal.getLocaleText("acc.field.AmountInBase") + " (" + WtfGlobal.getCurrencyName() + ")",
        dataIndex: 'amountinbase',
        align: 'right',
        summaryType: 'sum',
        width: 150,
        hidecurrency: true,
        pdfwidth:100,
        renderer: WtfGlobal.currencyDeletedRenderer,
        summaryRenderer: WtfGlobal.currencySummaryRenderer
    });
    this.gridColumnModelArr = WtfGlobal.appendCustomColumn(this.gridColumnModelArr, GlobalColumnModelForReports[Wtf.Acc_GENERAL_LEDGER_ModuleId], true);
    this.grid = new Wtf.grid.GridPanel({
        stripeRows: true,
        store: this.Store,
        loadMask: true,
        id: "gridmsg" + config.id,
        border: false,
        plugins: [this.summary],
        layout: 'fit',
        trackMouseOver: true,
        viewConfig: {forceFit: true, emptyText: WtfGlobal.getLocaleText("acc.field.Norecordstodisplay.")},
        forceFit: true,
        cm: new Wtf.grid.ColumnModel(this.gridColumnModelArr)

    });

    var colModelArray = GlobalColumnModelForReports[this.moduleid];
    WtfGlobal.updateStoreConfig(colModelArray, this.Store);

    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton"
    });

    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
        advSearch: false
    });

    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    this.Store.on('load',this.storeloaded,this);

    this.grid.on("render", function (grid) {
        WtfGlobal.autoApplyHeaderQtip(grid);
        this.grid.getView().refresh();
    }, this);
    this.grid.on('cellclick', this.onCellClick, this);

    this.resetBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTTLMS"), // 'Allow you to clearing existing search data.' ERP-12428 [SJ],
        id: 'btnRec' + this.id,
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        disabled: false
    });
    this.resetBttn.on('click', this.handleResetClickNew, this);

    this.exportButton = new Wtf.exportButton({
        obj: this,
        id: "exportReports" + this.id,
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
        disabled: true,
        scope: this,
//        hidden: true,
        menuItem: {csv: true, pdf: true, rowPdf: false},
        get: Wtf.autoNum.LMSJournalEntry,
        params: {
            isCustomerReport: true,
            customer: this.Name.getValue()
        }
    });

        this.printButton = new Wtf.exportButton({
            obj: this,
            text: WtfGlobal.getLocaleText("acc.common.print"),
            tooltip: WtfGlobal.getLocaleText("acc.common.printTT"),
            label: WtfGlobal.getLocaleText("acc.field.SalesBy") + (this.iscustreport ? (this.isSalesPersonName ? WtfGlobal.getLocaleText("acc.masterConfig.15") : WtfGlobal.getLocaleText("acc.up.3")) : WtfGlobal.getLocaleText("acc.product.gridProduct")),
            menuItem: {print: true},
        get:Wtf.autoNum.LMSJournalEntry,
//        hidden: true,
            params: {
            isCustomerReport:true,
                customer: this.Name.getValue()
            }
        });

    this.leadpan = new Wtf.Panel({
        border: false,
        layout: "border",
        items: [this.objsearchComponent, {
                region: 'center',
                layout: 'fit',
                border: false,
                tbar: [this.quickPanelSearch, WtfGlobal.getLocaleText("acc.common.from"), this.startDate, WtfGlobal.getLocaleText("acc.common.to"), this.endDate, '-', WtfGlobal.getLocaleText("acc.field.Select") + " " + (WtfGlobal.getLocaleText("acc.up.3")), this.Name, '-', {
                        text: WtfGlobal.getLocaleText("acc.agedPay.fetch"),
                        iconCls: 'accountingbase fetch',
                        scope: this,
                        handler: this.loaddata
                    }, this.AdvanceSearchBtn, '-', this.resetBttn, '-', this.exportButton, '-', this.printButton],
                items: [this.grid],
                bbar: this.pagingToolbar
            }]
    });

    Wtf.apply(this, {
        border: false,
        layout: "fit",
        bodyStyle: "background-color:#ffffff;padding-right:10px;",
        items: [this.leadpan]
    }, config);


    Wtf.account.JournalEntryCustomerDetailPanel.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.JournalEntryCustomerDetailPanel, Wtf.Panel, {
    hideLoading: function () {
        Wtf.MessageBox.hide();
    },
    loaddata: function () {
        if (this.Name.getValue() == '') {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleaseselectacustomerfromdropdown")], 2);
            this.Store.removeAll();
            return;
        }
        if (this.Store.baseParams && this.Store.baseParams.searchJson) {
            this.Store.baseParams.searchJson = "";
        }
        this.Store.load({
            params: {
                start: 0,
               limit: 15,
                pagingFlag: true,
                customer: this.Name.getValue()

            }
        });
        this.exportButton.enable();
   },
    configurAdvancedSearch: function () {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();

    },
    filterStore: function (json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.Store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: this.pP.combo.value
            }
        });
    },
    clearStoreFilter: function () {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.Store.baseParams = {
            pendingApproval: this.pendingApproval,
            flag: 1,
            searchJson: this.searchJson,
            moduleid: Wtf.Acc_GENERAL_LEDGER_ModuleId,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.Store.load({
            params: {
                ss: this.quickPanelSearch.getValue(),
                start: 0,
                limit: this.pP.combo.value
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
    onCellClick: function (g, i, j, e) {
        var formrec = this.grid.getStore().getAt(i);
        var header = g.getColumnModel().getDataIndex(j);
        if (header == "billno") {
            var incash = formrec.get("incash");
            if (incash) {
                callViewCashReceipt(formrec, 'ViewInvoice');
            } else {
                if (formrec.data.fixedAssetInvoice || formrec.data.fixedAssetLeaseInvoice) {
                    callViewFixedAssetInvoice(formrec, formrec.data.billid + 'Invoice', false, undefined, false, formrec.data.fixedAssetInvoice, formrec.data.fixedAssetLeaseInvoice);
                } else if (formrec.data.isConsignment) {
                    callViewConsignmentInvoice(true, formrec, formrec.data.billid + 'ConsignmentInvoice', false, false, true);
                } else {
                    callViewInvoice(formrec, 'ViewCashReceipt');
                }
            }
        } else if (header == "customername") {
            openAccountStatement(formrec.data.customerid, "true");
        }
    },
    handleResetClickNew: function () {

        this.quickPanelSearch.reset();
        this.Name.reset();          //ERP-12427[SJ]
        this.Name.setValue(this.customerAccStore.getAt(0).data.accid); //ERP-12427[SJ]
        this.startDate.setValue(WtfGlobal.getDates(true));
        this.endDate.setValue(WtfGlobal.getDates(false));
        if (this.isSalesPersonName) {
            this.salesPersonName.setValue(this.userds.getAt(0).data.id);
            this.customerCategory.setValue(this.customerCategoryStore.getAt(0).data.id);
        } 
        this.Store.load({
            params: {
                start: 0,
                limit: this.pP.combo.value,
                pagingFlag: true,
                customer: this.Name.getValue()    //ERP-12427[SJ]
            }
        });

    },  
    storeloaded:function(store){
        Wtf.MessageBox.hide();
    }
});
