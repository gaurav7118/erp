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

Wtf.account.NewContractReport = function(config) {
    this.reportbtnshwFlag = config.reportbtnshwFlag;
    this.isNormalContract = (config.isNormalContract)?config.isNormalContract:false;// contract which is not a Lease Contract is Normal Contract.
    this.moduleid= config.moduleId;
    Wtf.apply(this, config);
    
    this.expandRec = Wtf.data.Record.create ([
        {name:'pid'},
        {name:'productid'},
        {name:'productcode'},
        {name:'productname'},
        {name:'quantity'},
        {name:'unitname'},
        {name:'rate'},
        {name:'prdiscount'},
        {name:'discountispercent'},
        {name:'amount'},
        {name:'currencysymbol'},
        {name:'customfield'}
    ]);
    this.expandStoreUrl = "ACCContract/getContractOrderRows.do";
    
    this.expandStore = new Wtf.data.Store({
        url:this.expandStoreUrl,
        baseParams:{
            mode:14,
            dtype : 'report', // Display type report/transaction, used for quotation
            isNormalContract:this.isNormalContract
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.expandRec)
    });
    
    this.expander = new Wtf.grid.RowExpander({});
    
    this.expandStore.on('load',this.fillExpanderBody,this);
    this.expander.on("expand",this.onRowexpand,this);
     
    this.GridRec = Wtf.data.Record.create([
        {name:'cid'},
        {name:'billid'},
        {name:'sequenceformatid'},
        {name:'contractid'},
        {name:'salesorder'},
        {name:'salesorderid'},
        {name:'accname'},
        {name:'accid'},
        {name:'billingAddress1'},
        {name:'contactperson'},
        {name:'emailid'},
        {name:'agreedservices'},
        {name:'leaseamount'},
        {name:'securitydeposite'},
        {name:'currencysymbol'},
        {name:'currencyname'},
        {name:'currencyid'},
        {name:'contactperson'},
        {name:'attachdoc'}, //SJ[ERP-16428]
        {name:'attachment'},//SJ[ERP-16428]
        {name:'leaseterm'},
        {name:'termvalue'},
        {name:'leasestatus'},
        {name:'invoiceid'},
        {name:'doid'},
        {name:'dolinkto'},
        {name:'leaseamount'},
        {name:'frequencyType'},
        {name:'numberOfPeriods'},
        {name:'contractstatus'},
        {name:'goodsreturnststus'},
        {name:'startdate', type:'date'},
        {name:'enddate', type:'date'},
        {name:'orgenddate', type:'date'},
        {name:'signindate', type:'date'},
        {name:'moveindate', type:'date'},
        {name:'moveoutdate', type:'date'},
        {name:'memo'},
        {name:'scheduleId'},
        {name:'scheduleNumber'},
        {name:'scheduleStartDate',type:'date'},
        {name:'scheduleEndDate',type:'date'},
        {name:'frequency'},
        {name:'isAdhoc'},
        {name:'frequencyType'},
        {name:'totalEvents'},
        {name:'adHocEventDetails'},
        {name:'adHocEventDetailsEdit'},
        {name:'isScheduleEdit'},
        {name:'scheduleStopCondition'},
        {name:'eventDuration'},
        {name:'usedintransaction'},
        {name:'hasAccess'}
    ]);
    
    this.ContractStore = new Wtf.data.Store({
        url:"ACCContract/getContractOrders.do",
        baseParams:{
            isNormalContract:this.isNormalContract
        },
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:"totalCount",
            root: "data"
        },this.GridRec)
    });
    
    this.ContractStore.on('load', function(store) {
        if(this.ContractStore.getCount() < 1) {
            this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.grid.getView().refresh();
        }
        this.quickPanelSearch.StorageChanged(store);
//        if (this.isNormalContract) {
//            this.hideTransactionReportFields(Wtf.account.HideFormFieldProperty.salesContract, store);
//        } else {
//            this.hideTransactionReportFields(Wtf.account.HideFormFieldProperty.leaseContract, store);
//        }
    }, this);
    
    this.ContractStore.on('beforeload',this.contractStoreBeforeLoad,this);
    this.ContractStore.on('datachanged', function() {
        var p = this.pP.combo.value;
        this.quickPanelSearch.setPage(p);
    }, this);
    
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.LeaseReport.QuickSearchEmptyText"), // "Search by Contract ID, Customer, Contact Person ...",
        width: 300,
        id:"quickSearch"+config.helpmodeid,
        field: 'contractid'
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
    
    this.newTabButton=getCreateNewButton(config.consolidateFlag,this,WtfGlobal.getLocaleText("acc.lease.contract.createnewtooltip"),this.reportbtnshwFlag);
    this.newTabButton.on('click',this.openNewTab,this);
    
    this.viewContractDetailBtn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.LeaseReport.viewContractDetailBtnText"),  // 'View Contract Details',
        tooltip :WtfGlobal.getLocaleText("acc.LeaseReport.viewContractDetailBtnText"),  // 'View Contract Details',
        id: 'btnviewContractDetailBtn' + this.id,
                scope: this,
        iconCls :getButtonIconCls(Wtf.etype.reorderreport),
                disabled :true
    });
    
    this.viewCustomerContractDetailBtn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.cust.viewCustomerContracts"),  // 'View Customer Contracts',
        tooltip :WtfGlobal.getLocaleText("acc.cust.viewCustomerContracts"),  // 'View Customer Contracts',
        id: 'btnviewCustomerContractDetails' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.reorderreport),
        disabled :true
    });
    
    this.viewCustomerContractDetailBtn.on('click', function(){
        var rec = this.sm.getSelected();
        callCustomerContractDetailsTab(rec);
    }, this);
    
    this.editBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.edit"),  //'Edit',
        tooltip :WtfGlobal.getLocaleText("acc.contractReoprt.editO"), //acc.invoiceList.editO:-'Allows you to edit Order.':acc.invoiceList.editQ:-'Allows you to edit Quotation.'
        id: 'btnEdit' + this.id,
        scope: this,
//                hidden:this.isRFQ || this.pendingapproval || config.consolidateFlag,
        iconCls :getButtonIconCls(Wtf.etype.edit),
        disabled :true,
        hidden:this.reportbtnshwFlag
    });
        this.editBttn.on('click',this.editOrderTransaction.createDelegate(this,[false]),this);
    this.deleteBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.delete"), //'Edit',
        tooltip: WtfGlobal.getLocaleText("acc.contractReoprt.deletecontract"), //acc.invoiceList.editO:-'Allows you to edit Order.':acc.invoiceList.editQ:-'Allows you to edit Quotation.'
        id: 'btnDelete' + this.id,
        scope: this,
        iconCls:getButtonIconCls(Wtf.etype.deletebutton),
        disabled: true,
        hidden: this.reportbtnshwFlag,
        handler:this.deleteTransaction
    });
        
        
        this.TerminateBttn=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.contractReoprt.terminateContrat"),  //'Terminate',
                tooltip :WtfGlobal.getLocaleText("acc.contractReoprt.terminateContrat.TTip"), //acc.invoiceList.editO:-'Allows you to edit Order.':acc.invoiceList.editQ:-'Allows you to edit Quotation.'
                id: 'btnTerminate' + this.id,
                scope: this,
                handler:this.terminateContract,
                iconCls :getButtonIconCls(Wtf.etype.terminate),
                hidden:this.reportbtnshwFlag,
                disabled :true
        });
        this.RenewBttn=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.contractReoprt.renewContrat"),  //'Renew',
                tooltip :WtfGlobal.getLocaleText("acc.contractReoprt.renewContrat.TTip"),
                id: 'btnRenew' + this.id,
                scope: this,
                 handler:this.contractDatesWindow,
                iconCls :getButtonIconCls(Wtf.etype.renew),
                disabled :true,
                hidden:this.reportbtnshwFlag
        });
        this.salesReturnBttn=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.contractReoprt.SRContrat"),  //'Sales Return',
                tooltip :WtfGlobal.getLocaleText("acc.contractReoprt.SRContrat.TTip"), 
                id: 'btnSalesReturn' + this.id,
                scope: this,
                handler:this.getSalesReturn,
                iconCls :getButtonIconCls(Wtf.etype.srcontract),
                disabled :true,
                hidden:this.reportbtnshwFlag
        });        
        this.openSalesReturnBttn=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.contractReoprt.OpenSRContrat"),  //'Sales Return',
                tooltip :WtfGlobal.getLocaleText("acc.contractReoprt.OpenSRContrat.TTip"), 
                id: 'btnopenSalesReturn' + this.id,
                scope: this,
                handler:this.openContract,
                iconCls :getButtonIconCls(Wtf.etype.salesopen),
                disabled :true,
                hidden:this.reportbtnshwFlag
        });        
         this.singlePrint=new Wtf.exportButton({
         obj:this,
         id:"printReports"+config.helpmodeid+config.id,
         iconCls: 'pwnd exportpdfsingle',
         text: WtfGlobal.getLocaleText("acc.rem.39.single"),// + " "+ singlePDFtext,
         tooltip :WtfGlobal.getLocaleText("acc.rem.39.singletooltip"),  //'Export selected record(s)'
         disabled :true,
         hidden:this.isRFQ || this.isSalesCommissionStmt,
         menuItem:{rowPdf:(this.isSalesCommissionStmt)?false:true,rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39") + " "},
         get:Wtf.autoNum.Contract,
         moduleid:config.moduleId
     });
     this.exportButton=new Wtf.exportButton({
                obj:this,
                id:"exportReports"+config.helpmodeid+config.id,
                tooltip:WtfGlobal.getLocaleText("acc.common.exportTT"),  //"Export Report details.",  
                params:{
                    name:this.isNormalContract?WtfGlobal.getLocaleText("acc.ContractReport.SalesContractReport"):WtfGlobal.getLocaleText("acc.ContractReport.LeaseContractReport"),
                    isExport:true
                },
                isNormalContract:this.isNormalContract,
                menuItem:{csv:true,pdf:true,rowPdf:false,xls:true,detailedXls:true},
                get:Wtf.autoNum.ContractReport,
                filename: this.isNormalContract? WtfGlobal.getLocaleText("acc.ContractReport.SalesContractReport")+"_v1" : WtfGlobal.getLocaleText("acc.ContractReport.LeaseContractReport")+"_v1",
                label:this.isNormalContract?WtfGlobal.getLocaleText("acc.field.SalesContract"):WtfGlobal.getLocaleText("acc.field.LeaseContract"),
                moduleid:config.moduleId
        });
        this.printButton=new Wtf.exportButton({
            text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            obj:this,
            tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
            params:{
                name:this.isNormalContract?WtfGlobal.getLocaleText("acc.ContractReport.SalesContractReport"):WtfGlobal.getLocaleText("acc.ContractReport.LeaseContractReport"),
                isExport:true
            },
            isNormalContract:this.isNormalContract,            
            menuItem:{print:true},
            filename:this.isNormalContract?WtfGlobal.getLocaleText("acc.ContractReport.SalesContractReport"):WtfGlobal.getLocaleText("acc.ContractReport.LeaseContractReport"),
            get:Wtf.autoNum.ContractReport,
            label:this.isNormalContract?WtfGlobal.getLocaleText("acc.field.SalesContract"):WtfGlobal.getLocaleText("acc.field.LeaseContract"),
            moduleid:config.moduleId
        });
    this.viewContractDetailBtn.on('click', function(){
        var rec = this.sm.getSelected();
        callContractDetails(rec, this.isNormalContract);
    }, this);
    
    this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  // 'From',
        name:'startdate',
        format:WtfGlobal.getOnlyDateFormat(),
        readOnly:true,
        value:WtfGlobal.getDates(true)
    });
    
    this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  // 'To',
        format:WtfGlobal.getOnlyDateFormat(),
        readOnly:true,
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
    //View Filter
    this.viewStore = new Wtf.data.SimpleStore({
        fields: ['id', 'name'],
        data : [['0','All'],['1','Active'],['2','Terminated']]
    });
    this.viewCombo = new Wtf.form.ComboBox({
        store: this.viewStore,
        displayField:'name',
        valueField:'id',
        typeAhead: true,
        value:0,
        mode: 'local',
        triggerAction: 'all'
    });
    this.tbar2 = new Array();
    this.tbar2.push(
        WtfGlobal.getLocaleText("acc.common.from"), this.startDate,
        WtfGlobal.getLocaleText("acc.common.to"), this.endDate,
        this.fetchBttn,
        '->',
        WtfGlobal.getLocaleText("acc.common.view"), this.viewCombo 
        
    );
        
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    
     this.gridColumnModelArr=[];
     this.gridColumnModelArr.push(this.sm,this.expander,
//            { 
//                hidden:true,
//                dataIndex:'cid'   //ERP-31551 : This column has kept hidden, as it is showing uuid value in report
//            },
            {
                header:WtfGlobal.getLocaleText("acc.Lease.ContractID"), // "Contract ID",
                dataIndex:"contractid",
                pdfwidth:75,
                sortable:true,
                renderer:WtfGlobal.linkDeletedRenderer
            },{
                header:(this.isNormalContract)?WtfGlobal.getLocaleText("acc.contract.sales.order.number"):WtfGlobal.getLocaleText("acc.MailWin.somsg8"), // "Sales Order Number"/"Lease Order Number",
                dataIndex:"salesorder",
                pdfwidth:75,
                sortable:true,
                renderer:WtfGlobal.deletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.invoiceList.cust"), // "Customer",
                dataIndex:"accname",
                pdfwidth:75,
                sortable:true,
                renderer :WtfGlobal.deletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.address.ContactPerson"), // "Contact Person",
                dataIndex:"contactperson",
                pdfwidth:75,
                sortable:true,
                renderer:WtfGlobal.deletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.Contract.startDate"), // "Contact Person",
                dataIndex:"startdate",
                pdfwidth:75,
                sortable:true,
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.contractActivityPanel.EndDate"), // "Contact Person",
                dataIndex:"enddate",
                pdfwidth:75,
                sortable:true,
                renderer:WtfGlobal.onlyDateDeletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.profile.email"), // "E-Mail",
                dataIndex:"emailid",
                pdfwidth:75,
                renderer:WtfGlobal.deletedRenderer
            },{
                header:WtfGlobal.getLocaleText("acc.Lease.agreedservices"), // "No. of Agreed Services",
                dataIndex:"agreedservices",
                pdfwidth:75,
                align:"right"
            },{
                header:WtfGlobal.getLocaleText("acc.Lease.LeaseAmount"), // "Contract Amount",
                dataIndex:"leaseamount",
                pdfwidth:75,
                align:"right",
                renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
            },{
                header:WtfGlobal.getLocaleText("acc.Lease.Contractstatus"), // "Contract Status",
                dataIndex:"leasestatus",
                pdfwidth:75,
                sortable:true,
                align:"right",
                renderer:this.statusRender                
            },{
                header:WtfGlobal.getLocaleText("acc.Lease.GRstatus"), // "Goods Receipt Status",
                dataIndex:"goodsreturnststus",
                pdfwidth:75,
                align:"right",
                renderer:this.statusSRRender
            },{
                header:WtfGlobal.getLocaleText("acc.common.Term"),
                dataIndex:'termvalue',
                hidden:true,
                hideable:true,
                pdfwidth:85
            },{
                header:WtfGlobal.getLocaleText("acc.common.currencyFilterLable"),
                dataIndex:'currencysymbol',
                hidden:true,
                hideable:true,
                pdfwidth:85
            },{
                header:WtfGlobal.getLocaleText("acc.Lease.securitydeposite"), // security deposite",
                dataIndex:"securitydeposite",
                pdfwidth:75,
                align:"right",
                renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol     //ERP-30710
            },{
                header:WtfGlobal.getLocaleText("acc.contract.invoicefrequency"),
                dataIndex:'frequencyType',
                hidden:true,
                hideable:true,
                pdfwidth:85
            },{
                header:WtfGlobal.getLocaleText("acc.contract.numberOfPeriods"),
                dataIndex:'numberOfPeriods',
                hidden:true,
                hideable:true,
                pdfwidth:85
            },{
                header:WtfGlobal.getLocaleText("acc.Lease.orgenddate"),
                dataIndex:'orgenddate',
                hidden:true,
                hideable:true,
                pdfwidth:85,
                renderer:WtfGlobal.onlyDateDeletedRenderer
             },{
                header:WtfGlobal.getLocaleText("acc.Lease.signindate"),
                dataIndex:'signindate',
                hidden:true,
                hideable:true,
                pdfwidth:85,
                renderer:WtfGlobal.onlyDateDeletedRenderer
              },{
                header:WtfGlobal.getLocaleText("acc.Lease.moveindate"),
                dataIndex:'moveindate',
                hidden:true,
                hideable:true,
                pdfwidth:85,
                renderer:WtfGlobal.onlyDateDeletedRenderer
                },{
                header:WtfGlobal.getLocaleText("acc.Lease.moveoutdate"),
                dataIndex:'moveoutdate',
                hidden:true,
                hideable:true,
                pdfwidth:85,
                renderer:WtfGlobal.onlyDateDeletedRenderer
              },{
                header:Wtf.account.companyAccountPref.descriptionType,  //"Memo",
                dataIndex:'memo',
                pdfwidth:85,
                renderer: function(value) {
                value = value.replace(/\'/g, "&#39;");
                value = value.replace(/\"/g, "&#34");
                return "<span class=memo_custom  wtf:qtip='" + value + "'>" + Wtf.util.Format.ellipsis(value, 60) + "</span>"
            }
              });
    //* Attachment Document in grid report Column model   SJ[ERP-16428]   
       this.gridColumnModelArr.push({
            header:WtfGlobal.getLocaleText("acc.invoiceList.attachDocuments"),  //"Attach Documents",
            dataIndex:'attachdoc',
            width:150,
            align:'center',
            renderer : function(val) {
                        return "<div style='height:16px;width:16px;'><div class='pwndbar1 uploadDoc' style='cursor:pointer' wtf:qtitle='"
                        + WtfGlobal
                        .getLocaleText("acc.invoiceList.attachDocuments")
                        + "' wtf:qtip='"
                        + WtfGlobal
                        .getLocaleText("acc.invoiceList.clickToAttachDocuments")
                        +"'>&nbsp;</div></div>";
                    }
        },{
            header:WtfGlobal.getLocaleText("acc.invoiceList.attachments"),  //"Attachments",
            dataIndex:'attachment',
            width:150,
            renderer : Wtf.DownloadLink.createDelegate(this)
        });
     // appening custom columns
    this.gridColumnModelArr = WtfGlobal.appendCustomColumn(this.gridColumnModelArr,GlobalColumnModelForReports[this.moduleid],true);    
    
    this.grid = new Wtf.grid.GridPanel({    
        store:this.ContractStore,
        sm:this.sm,
        tbar: this.tbar2,
        border:false,
        layout:'fit',
        viewConfig: {
//            forceFit:true
        },
        loadMask:true,
        plugins: this.expander,
        cm:new Wtf.grid.ColumnModel(this.gridColumnModelArr)
    });
    
    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);
    // * Attachment document in Grid SJ[ERP-16428]
   this.grid.flag = 0;
   this.grid.on('rowclick', Wtf.callGobalDocFunction, this);
   this.grid.on('cellclick', this.handleCellClick, this);
   // * Attachment document in Grid SJ[ERP-16428]
    var colModelArray = GlobalColumnModelForReports[this.moduleid];
    WtfGlobal.updateStoreConfigStringDate(colModelArray,this.ContractStore);
    this.getMyConfig();
    this.grid.on('render', function() {
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    }, this);
    this.ContractStore.load();
    Wtf.account.NewContractReport.superclass.constructor.call(this,config);
}

Wtf.extend(Wtf.account.NewContractReport,Wtf.Panel, {
        onRender: function(config){
//        this.ContractStore.load({
//            params:{
//                start:0,
//                limit:30
//            }
//        });
    this.uPermType= Wtf.UPerm.leasecontract;
    this.permType= Wtf.Perm.leasecontract;
    this.createPermType=this.isNormalContract?this.permType.createscont:this.permType.createlcont;
    this.terminatePermType=this.isNormalContract?this.permType.terminatescont:this.permType.terminatelcont;
    this.editPermType= this.isNormalContract?this.permType.editscont:this.permType.editlcont;
    this.renewPermType= this.isNormalContract?this.permType.renewscont:this.permType.renewlcont;
    this.exportPermType= this.isNormalContract?this.permType.exportscont:this.permType.exportlcont;
        this.tbar1 = new Array();
        this.tbar1.push(
            this.quickPanelSearch,
            this.resetBttn,
           (!WtfGlobal.EnableDisable(this.uPermType, this.createPermType))? this.newTabButton:'',
           (!WtfGlobal.EnableDisable(this.uPermType, this.editPermType))?this.editBttn:'',
           (!WtfGlobal.EnableDisable(this.uPermType, this.terminatePermType))?this.TerminateBttn:'',
           (!WtfGlobal.EnableDisable(this.uPermType, this.renewPermType))?this.RenewBttn:'',
           this.deleteBttn,
            this.salesReturnBttn,
            this.openSalesReturnBttn,
            this.viewContractDetailBtn,
            this.viewCustomerContractDetailBtn
        );
         var bottombtnArr=[];
      
        bottombtnArr.push('-',(!WtfGlobal.EnableDisable(this.uPermType, this.exportPermType))?this.singlePrint:'');
        bottombtnArr.push('-', this.exportButton);
        bottombtnArr.push('-', this.printButton);
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            autoScroll : true,
            attachDetailTrigger: true,
            items: [{
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: this.tbar1,
                bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 30,
                    id: "pagingtoolbar" + this.id,
                    store: this.ContractStore,
                    searchField: this.quickPanelSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), // "No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({
                        id : "pPageSize_"+this.id
                }),
                items:bottombtnArr
               
            
                })
            }]
        }); 
        this.add(this.leadpan);
        
        Wtf.account.NewContractReport.superclass.onRender.call(this,config);
    },
      statusRender : function(value){
          if(value==1){
              return 'Active';
          }else if(value==2){
              return 'Terminated';
          }else if(value==3){
              return 'Expired';
          }else if(value==4){
              return 'Renew';
          }
             
      },
//    hideTransactionReportFields: function(array, store) {
//        if (array) {
//            var cm = this.grid.getColumnModel();
//            for (var i = 0; i < array.length; i++) {
//                for (var j = 0; j < cm.config.length; j++) {
//                    if (cm.config[j].header === array[i].dataHeader || (cm.config[j].dataIndex === array[i].fieldId)) {
//                       cm.setHidden(j,(cm.config[j].iscustomcolumn !== undefined && cm.config[j].iscustomcolumn) ? array[i].isHidden : array[i].isReportField);
//                    }
//                }
//            }
//            this.grid.reconfigure(store, cm);
//        }
//    },
      contractDatesWindow : function(value){
        this.contractstartdate= new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.Lease.contractstartDate"),  //'Date',            
            name: 'startdate',
            labelWidth:255,
            anchor: '100%',
            format:WtfGlobal.getOnlyDateFormat(),
            value: Wtf.serverDate.clearTime(true),
            allowBlank:false
        });
        this.contractenddate= new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.Lease.contractendDate"),  //'Date',            
            name: 'enddate',
            anchor: '100%',
            format:WtfGlobal.getOnlyDateFormat(),
            value: Wtf.serverDate.clearTime(true),
            allowBlank:false
        });
        this.PricelistForm=new Wtf.form.FormPanel({
            region:'center',
            autoScroll:true,
            border:false,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            items:[this.contractstartdate,this.contractenddate]
        });
  
        this.contractDatesWin = new Wtf.Window({
            resizable: false,
            scope: this,
            layout: 'border',
            modal:true,
            width: 400,
            height: 180,
            border : false,
            iconCls: 'pwnd deskeralogoposition',
            title: WtfGlobal.getLocaleText("acc.contractReoprt.ContractDates"),
            items: [{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.PricelistForm
        }],
            buttons: [ {
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
                scope: this,
                handler: this.saveForm
            },{
                text:WtfGlobal.getLocaleText("acc.common.cancelBtn"), //'Cancel',
                scope:this,
                handler:function() {
                    this.contractDatesWin.close();
                }
            }]
        },this);

        this.contractDatesWin.show();
  
  
  
  
    },
    saveForm:function(){
        this.record = this.grid.getSelectionModel().getSelected();
        this.isEdit=true
        if(!this.PricelistForm.getForm().isValid()){
            WtfComMsgBox(2,2);
        }
        else {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.contractReoprt.addMaintenanSchedue") + "?", function(btn) {
                if (btn == "yes") {
                    this.maintenanceScheduleButtonHandler();
                } else {
                    this.Cofirm();
                }
            }, this)
            
        }
    },
    maintenanceScheduleButtonHandler: function() {

        this.maintenanceScheduler = new Wtf.account.AssetMaintenanceSchedule({
            title: WtfGlobal.getLocaleText("acc.maintenance.scheduler"),
            layout: 'border',
            resizable: false,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            modal: true,
            assetRec: this.assetRec,
            isContract: true,
            contractScheduleInformation: this.schedulerFormInformation,
            isFromCreateButton: false,
            isFromCreationForm: true,
            contractStartDate: (this.contractstartdate) ? this.contractstartdate.getValue() : '',
            contractEndDate: (this.contractenddate) ? this.contractenddate.getValue() : '',
            isEdit: true,
            scheduleRecord: this.record,
            isFromSaveAndCreateNewButton: false,
            height: 700,
            width: 700
        });

        this.maintenanceScheduler.on('beforeclose', this.scheduleBeforeCloseHandler, this);
        this.maintenanceScheduler.on('update', function() {
            this.maintenanceScheduler.resetComponent();
        }, this);

        this.maintenanceScheduler.show();
    },

    scheduleBeforeCloseHandler: function(panel) {
        if (panel.fromSaveButtonClosed) {
            this.schedulerFormInformation = panel.schedulerFormInformation;
            this.Cofirm();
        }
    },
    Cofirm: function() {
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.je.msg1") + "?", function(btn) {
            if (btn == "yes") {
                this.addContractdate();
            } else {
                this.contractDatesWin.close();
            }
        }, this)
    },
addContractdate : function(){
        var gridRec = this.sm.getSelected();
        var rec = this.PricelistForm.getForm().getValues();
        rec.startdate = WtfGlobal.convertToGenericDate(this.contractstartdate.getValue());
        rec.enddate = WtfGlobal.convertToGenericDate(this.contractenddate.getValue());
        rec.contract = gridRec.data.cid;
        rec.customer = gridRec.data.accid;
        rec.isRenewContract = true;
        if (this.schedulerFormInformation) {
            rec.scheduleNumber = this.schedulerFormInformation.scheduleNumber;
            rec.scheduleStartDate = this.schedulerFormInformation.scheduleStartDate;
            rec.hiddenCurrentDate = this.schedulerFormInformation.hiddenCurrentDate;
            rec.scheduleEndDate = this.schedulerFormInformation.scheduleEndDate;
            rec.isAdHocSchedule = this.schedulerFormInformation.isAdHocSchedule;
            rec.repeatInterval = this.schedulerFormInformation.repeatInterval;
            rec.intervalType = this.schedulerFormInformation.intervalType;
            rec.totalEvents = this.schedulerFormInformation.totalEvents;
            rec.scheduleDuration = this.schedulerFormInformation.scheduleDuration;
            rec.adHocEventDetails = this.schedulerFormInformation.adHocEventDetails;
            rec.isScheduleBtn = this.schedulerFormInformation.isScheduleBtn;
            rec.totalEventsStopCondition = this.schedulerFormInformation.totalEventsStopCondition;
            rec.endDateStopCondition = this.schedulerFormInformation.endDateStopCondition;
            if (this.isEdit) {
                rec.scheduleId = this.schedulerFormInformation.scheduleId;
            }
            rec.isScheduleIncluded = true;
            if (!(new Date(this.schedulerFormInformation.scheduleStartDate).between(this.contractstartdate.getValue(), this.contractenddate.getValue()))) {// Maintenance Schedule Start date should be between Contract Start Date and Contract End Date
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.contract.scheduledate")], 3);
                return;
            }
        } else {
            /*
             * In case of edit if Contract From Date and End Date get Changed but Maintenance Schedule Start Date does not chhanged and it does not lie between Contract Start & End date
             */
            if (this.record && this.isEdit && !(this.record.get('scheduleStartDate') == null || this.record.get('scheduleStartDate') == undefined || this.record.get('scheduleStartDate') == '')) {
                if (!(this.record.get('scheduleStartDate').between(this.contractstartdate.getValue(), this.contractenddate.getValue()))) {// Maintenance Schedule Start date should be between Contract Start Date and Contract End Date
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.contract.scheduledate")], 3);
                    return;
                }
            }
            rec.isScheduleIncluded = false;
        }
            var billid=gridRec.data.billid;
            if(billid && billid!=""){
                Wtf.Ajax.requestEx({
                    url:"ACCContract/addContractDates.do",
                    params: rec
                },this,this.genSuccessResponse,this.genFailureResponse);
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.invoicenotcreate")],2); 
            }
    },
    statusSRRender : function(value){
        if(value==1){
            return 'Pending';
        }else if(value==2){
            return 'Pending & Closed';
        }else if(value==3){
            return 'Done';
        }else if(value==4){
            return 'Done & Closed';
        }
             
    },
      terminateContract : function(){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.contractReoprt.terminateContract")+"?",function(btn){
            if(btn=="yes") {
                var formRecord = this.grid.getSelectionModel().getSelected();
                Wtf.Ajax.requestEx({
                    url: "ACCContract/changeContractStatus.do",
                    params: {
                        contractid : formRecord.data.cid
                    }
                },this,this.genSuccessResponseTerminate,this.genFailureResponseTerminate);
            
            }
        }, this)
    },
    genSuccessResponseTerminate : function(response){
        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function(){
            this.ContractStore.reload();
        }, this);
    },
    genFailureResponseTerminate : function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")],2);
    },
    getSalesReturn : function(){
        var gridRec = this.sm.getSelected(); 
        var billid=gridRec.data.doid;
        var billlink=gridRec.data.dolinkto;
        var customerid=gridRec.data.accid;
        var contractStatus=gridRec.data.goodsreturnststus;
        var cid=gridRec.data.cid;
            if(billid && billid!=""){
                if(this.isNormalContract){
                    this.callSalesReturn(false,null,null,false,billid,billlink,customerid,cid,contractStatus);
                }else{
                    this.callLeaseSalesReturn(false,null,null,true,billid,billlink,customerid,cid,contractStatus);
                }
            }else{
               WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.deliveryordernotcreate")],2); 
            }
    },
    callLeaseSalesReturn:function(isEdit,rec,winid,isLeaseFixedAsset,billid,billlink,customerid,cid,contractStatus){
        winid=(winid==null?'leasesalesreturn':winid);
        isLeaseFixedAsset=(isLeaseFixedAsset)?isLeaseFixedAsset:false;
        var panel = Wtf.getCmp(winid);
        if(panel==null){
            panel = new Wtf.account.FixedAssetSalesReturnPanel({
                id : winid,
                isEdit: isEdit,
//                record: undefined,
                isCustomer:true,
                isSalesFromDo:true,
                dopersonid:customerid,
                contractStatus:contractStatus,
                contractid:cid,
                dolinkid:billid,
                billlink:billlink,
                isCustBill:false,            
                label:WtfGlobal.getLocaleText("acc.accPref.autoSR"),
                border : false,
                heplmodeid: 11,
                isLeaseFixedAsset:isLeaseFixedAsset,
                moduleid:Wtf.Acc_Sales_Return_ModuleId,
                //            layout: 'border',
                title:WtfGlobal.getLocaleText("acc.accPref.autoSR"),
                tabTip:WtfGlobal.getLocaleText("acc.accPref.autoSR"),
                closable: true,
                iconCls:'accountingbase deliveryorder',
                modeName:'autosr'
            });
            panel.on("activate", function(){
                panel.doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);

        Wtf.getCmp('as').doLayout();
    },
    
    callSalesReturn:function(isEdit,rec,winid,isLeaseFixedAsset,billid,billlink,customerid,cid,contractStatus){
        winid=(winid==null?'salesreturnpanel':winid);
        var panel = Wtf.getCmp(winid);
        
        if(panel==null){
            panel = new Wtf.account.SalesReturnPanel({
                id : winid,
                isEdit: isEdit,
                record: rec,
                isCustomer:true,
                
                isSalesFromDo:true,
                dopersonid:customerid,
                contractStatus:contractStatus,
                contractid:cid,
                dolinkid:billid,
                billlink:billlink,
                
                
                isCustBill:false,            
                label:WtfGlobal.getLocaleText("acc.accPref.autoSR"),
                border : false,
                heplmodeid: 11,
                moduleid:Wtf.Acc_Sales_Return_ModuleId,
                //            layout: 'border',
                title:WtfGlobal.getLocaleText("acc.accPref.autoSR"),
                tabTip:WtfGlobal.getLocaleText("acc.accPref.autoSR"),
                closable: true,
                iconCls:'accountingbase deliveryorder',
                modeName:'autosr'
            });
            panel.on("activate", function(){
                panel.doLayout();
            }, this);
            Wtf.getCmp('as').add(panel);
        }
        Wtf.getCmp('as').setActiveTab(panel);

        Wtf.getCmp('as').doLayout();
    },
    
    genSuccessResponse : function(response){
        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.block"), response.msg, function(){
            if(response.success){
                this.contractDatesWin.close();
                
                
                
                var leaseInvoiceRec = Wtf.data.Record.create ([
                        {
                            name:'billid'
                        },

                        {
                            name:'journalentryid'
                        },

                        {
                            name:'entryno'
                        },

                        {
                            name:'billto'
                        },

                        {
                            name:'companyid'
                        },

                        {
                            name:'companyname'
                        },

                        {
                            name:'discount'
                        },

                        {
                            name:'currencysymbol'
                        },

                        {
                            name:'orderamount'
                        },

                        {
                            name:'isexpenseinv'
                        },

                        {
                            name:'currencyid'
                        },

                        {
                            name:'shipto'
                        },

                        {
                            name:'mode'
                        },

                        {
                            name:'billno'
                        },

                        {
                            name:'date', 
                            type:'date'
                        },

                        {
                            name:'duedate', 
                            type:'date'
                        },

                        {
                            name:'shipdate', 
                            type:'date'
                        },

                        {
                            name:'personname'
                        },

                        {
                            name:'personemail'
                        },

                        {
                            name:'personid'
                        },

                        {
                            name:'shipping'
                        },

                        {
                            name:'othercharges'
                        },

                        {
                            name:'partialinv',
                            type:'boolean'
                        },

                        {
                            name:'includeprotax',
                            type:'boolean'
                        },

                        {
                            name:'amount'
                        },

                        {
                            name:'amountdue'
                        },

                        {
                            name:'termdays'
                        },

                        {
                            name:'termname'
                        },

                        {
                            name:'incash',
                            type:'boolean'
                        },

                        {
                            name:'taxamount'
                        },

                        {
                            name:'taxid'
                        },

                        {
                            name:'orderamountwithTax'
                        },

                        {
                            name:'taxincluded',
                            type:'boolean'
                        },

                        {
                            name:'taxname'
                        },

                        {
                            name:'deleted'
                        },

                        {
                            name:'termamount'
                        },

                        {
                            name:'amountinbase'
                        },

                        {
                            name:'memo'
                        },

                        {
                            name:'createdby'
                        },

                        {
                            name:'createdbyid'
                        },

                        {
                            name:'externalcurrencyrate'
                        },

                        {
                            name:'ispercentdiscount'
                        },

                        {
                            name:'discountval'
                        },

                        {
                            name:'crdraccid'
                        },

                        {
                            name:'creditDays'
                        },

                        {
                            name:'isRepeated'
                        },

                        {
                            name:'porefno'
                        },

                        {
                            name:'costcenterid'
                        },

                        {
                            name:'costcenterName'
                        },

                        {
                            name:'interval'
                        },

                        {
                            name:'intervalType'
                        },

                        {
                            name:'NoOfpost'
                        }, 

                        {
                            name:'NoOfRemainpost'
                        },  

                        {
                            name:'templateid'
                        },

                        {
                            name:'templatename'
                        },

                        {
                            name:'startDate', 
                            type:'date'
                        },

                        {
                            name:'nextDate', 
                            type:'date'
                        },

                        {
                            name:'expireDate', 
                            type:'date'
                        },

                        {
                            name:'repeateid'
                        },

                        {
                            name:'status'
                        },

                        {
                            name:'amountwithouttax'
                        },

                        {
                            name:'amountwithouttaxinbase'
                        },

                        {
                            name:'commission'
                        },

                        {
                            name:'commissioninbase'
                        },

                        {
                            name:'amountDueStatus'
                        },

                        {
                            name:'salesPerson'
                        },

                        {
                            name:'agent'
                        },

                        {
                            name:'shipvia'
                        },

                        {
                            name:'fob'
                        },

                        {
                            name:'approvalstatus'
                        },

                        {
                            name:'approvalstatusint', 
                            type:'int', 
                            defaultValue:-1
                        },

                        {
                            name:'archieve', 
                            type:'int'
                        },

                        {
                            name:'withoutinventory',
                            type:'boolean'
                        },

                        {
                            name:'isfavourite'
                        },

                        {
                            name:'othervendoremails'
                        },

                        {
                            name:'termdetails'
                        },

                        {
                            name:'approvestatuslevel'
                        },// for requisition

                        {
                            name:'posttext'
                        },

                        {
                            name:'isOpeningBalanceTransaction'
                        },

                        {
                            name:'isNormalTransaction'
                        },

                        {
                            name:'isreval'
                        },

                        {
                            name:'islockQuantityflag'
                        },

                        {
                            name:'isprinted'
                        },

                        {
                            name:'validdate', 
                            type:'date'
                        },

                        {
                            name:'cashtransaction',
                            type:'boolean'
                        },

                        {
                            name:'shiplengthval'
                        },

                        {
                            name:'invoicetype'
                        },

                        {
                            name:'landedInvoiceID'
                        },

                        {
                            name:'landedInvoiceNumber'
                        },

                        {
                            name:'termdays'
                        },

                        {
                            name:'billingAddress'
                        },

                        {
                            name:'billingCountry'
                        },

                        {
                            name:'billingState'
                        },

                        {
                            name:'billingPostal'
                        },

                        {
                            name:'billingEmail'
                        },

                        {
                            name:'billingFax'
                        },

                        {
                            name:'billingMobile'
                        },

                        {
                            name:'billingPhone'
                        },

                        {
                            name:'billingContactPerson'
                        },

                        {
                            name:'billingContactPersonNumber'
                        },
                        {
                            name:'billingContactPersonDesignation'
                        },
                        {
                            name:'billingWebsite'
                        },
                        {
                            name:'billingCounty'
                        },
                        {
                            name:'billingCity'
                        },

                        {
                            name:'billingAddressType'
                        },

                        {
                            name:'shippingAddress'
                        },

                        {
                            name:'shippingCountry'
                        },

                        {
                            name:'shippingState'
                        },

                        {
                            name:'shippingCounty'
                        },

                        {
                            name:'shippingCity'
                        },

                        {
                            name:'shippingEmail'
                        },

                        {
                            name:'shippingFax'
                        },

                        {
                            name:'shippingMobile'
                        },

                        {
                            name:'shippingPhone'
                        },

                        {
                            name:'shippingPostal'
                        },

                        {
                            name:'shippingContactPersonNumber'
                        },
                        {
                            name:'shippingContactPersonDesignation'
                        },
                        {
                            name:'shippingWebsite'
                        },
                        {
                            name:'shippingContactPerson'
                        },

                        {
                            name:'shippingAddressType'
                        },

                        {
                            name:'sequenceformatid'
                        },

                        {
                            name:'gstIncluded'
                        },

                        {
                            name:'salespersonname'
                        }
        
                        ]);
    
                this.leaseInvoice = new Wtf.data.Store({
                    url:"ACCInvoiceCMN/getInvoicesMerged.do",
                    scope:this,
                    baseParams:{
                        billid:billid,
                        consolidateFlag:false,
                        isLeaseFixedAsset:!this.isNormalContract
                    },
                    reader: new Wtf.data.KwlJsonReader({
                        root: "data",
                        totalProperty:'count'
                    },leaseInvoiceRec)
                });
                this.leaseInvoice.load();
                 this.leaseInvoice.on("load",function(){
                     if(this.leaseInvoice.getCount()==1){
                         var formrec=this.leaseInvoice.getAt(0);
                        formrec.data.partialinv=false;
                        formrec.data.contract=gridRec.data.cid;
                        if(this.isNormalContract){
                            callEditInvoice(formrec, "copy"+billid+'Invoice',true,undefined,false,false,false);
                        }else{
                            callEditFixedAssetInvoice(formrec, "copy"+billid+'Invoice',true,undefined,false,false,true,false);
                        }
                     }
                 },this)
            }
            this.maintenanceScheduleButtonHandler();
            this.ContractStore.reload();
            this.schedulerFormInformation="";//clear maintenance schedule when after saving data successfully on save and create new button
            if (this.maintenanceScheduler) {
                this.maintenanceScheduler.fireEvent("update", this);  //for reset of maintenance schedule form in case of save and create new option
            }
        }, this);
    },
    genFailureResponse : function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")],2);
    },
    enableDisableButtons:function() {
        var rec = this.sm.getSelected();
         if(this.sm.getCount()>=1){
            if(this.singlePrint)this.singlePrint.enable();
          
        }else{
            if(this.singlePrint)this.singlePrint.disable();
          
        }
        if(this.sm.getCount() == 1) {
            this.viewContractDetailBtn.enable();
            this.viewCustomerContractDetailBtn.enable();
            this.editBttn.enable();
            this.TerminateBttn.enable();
            this.deleteBttn.enable();
            this.RenewBttn.enable();
            if(rec.data.goodsreturnststus==4){
                this.salesReturnBttn.disable();
               
            }else{
                this.salesReturnBttn.enable();
                 if(rec.data.goodsreturnststus==2)
                    this.openSalesReturnBttn.enable();
                 else
                     this.openSalesReturnBttn.disable();
                     
            }
            
            if(rec.data.leasestatus == 2 || rec.data.leasestatus == 3){// if contrcat is terminated or expired then it can not be edit
                this.editBttn.disable();
                this.TerminateBttn.disable();
            }
            if(rec.data.leasestatus==1){  // if contract is Acive then disable Renew button
                this.RenewBttn.disable();
            }
            
        } else {
            this.viewContractDetailBtn.disable();
            this.viewCustomerContractDetailBtn.disable();
            this.editBttn.disable();
            this.TerminateBttn.disable();
            this.deleteBttn.disable();
            this.RenewBttn.disable();
            this.salesReturnBttn.disable();
            this.openSalesReturnBttn.disable();
        }
          
    },
    
    openContract:function() {
         var rec = this.sm.getSelected();
          Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.contractReoprt.openContract")+"?",function(btn){
        if(btn=="yes") {
            Wtf.Ajax.requestEx({
                url: "ACCContract/changeContractSRStatus.do",
                params: {
                    contractid : rec.data.cid,
                    status : 1
                }
            },this,this.genSuccessResponseTerminate,this.genFailureResponseTerminate);
            
        }
    }, this)
    },
    handleResetClick:function() {
        if(this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.fetchStatement();
        }
    },
    
    openNewTab:function() {
        if(this.isNormalContract){
            callContractOrder(false, null, null,this.isNormalContract);
        }else{
            callContractOrder();
        }
    },
    editOrderTransaction:function(){			// Editing Sales and Purchase Order with Inventory and Without Inventory
    	var formRecord = null;
    	if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox(15,2);
            return;
    	}
    	formRecord = this.grid.getSelectionModel().getSelected();
        var label=WtfGlobal.getLocaleText("acc.product.edit");
    	var billid=formRecord.get("billid");
        this.withInvMode = formRecord.get("withoutinventory");
        
        if(!formRecord.data.usedintransaction){
            if(this.isNormalContract){
                callContractOrder(true,formRecord, null,this.isNormalContract);
            }else{
                callContractOrder(true,formRecord);
            }
        }
        else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.editContract"),WtfGlobal.getLocaleText("acc.contractReoprt.editContract")],2);
        }
    },
        deleteTransaction:function(){			// Editing Sales and Purchase Order with Inventory and Without Inventory
    	var formRecord = null;
    	if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox(15,2);
            return;
    	}
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.contractReoprt.deleteContract")+"?",function(btn){
            if(btn=="yes") {
                var formRecord = this.grid.getSelectionModel().getSelected();
                Wtf.Ajax.requestEx({
                    url: "ACCContract/deleteContract.do",
                    params: {
                        contractid : formRecord.data.cid,
                        scheduleId:formRecord.data.scheduleId,
                        contractno:formRecord.data.contractid,
                        contractstatus:formRecord.data.leasestatus,
                        scheduleName:formRecord.data.scheduleNumber
                    }
                },this,this.genSuccessResponseDelete,this.genFailureResponseDelete);
            
            }
        }, this)
    },
    genSuccessResponseDelete: function(response) {
        if (response.success) {
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function() {
                this.ContractStore.reload();
            }, this);
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
        }

    },
    genFailureResponseDelete: function(response) {
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Erroroccurredwhiledeletingcontract")], 2);
    },
    fetchStatement:function() {
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();
        
        if(this.sDate > this.eDate){
            WtfComMsgBox(1,2);
            return;
        }
        this.ContractStore.load({
            params: {
                start:0,
                limit:(this.pP.combo == undefined) ? 30 : this.pP.combo.value
            }
        });
    },
     contractStoreBeforeLoad:function() {
        this.sDate=this.startDate.getValue();
        this.eDate=this.endDate.getValue();
        
        var fromdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        var todate = WtfGlobal.convertToGenericDate(this.endDate.getValue());
      
        var currentBaseParams = this.ContractStore.baseParams;
        currentBaseParams.startdate=fromdate,
        currentBaseParams.enddate=todate,
        currentBaseParams.viewfilter =this.viewCombo.getValue(),
        currentBaseParams.isForReport =true
         
        this.ContractStore.baseParams=currentBaseParams;
        this.exportButton.params=currentBaseParams;
    },   
    onRowexpand:function(scope, record, body){
        var colModelArray = GlobalColumnModel[this.moduleid];
        WtfGlobal.updateStoreConfig(colModelArray,this.expandStore);
        this.expanderBody=body;
        this.expandStore.load({
            params: {
                contractid:record.data.cid,
                isForReport:true
            }
        });
    },
    
    loadStore:function(){
        this.ContractStore.reload();
    },
    
    fillExpanderBody: function() {
        if(this.expandStore.getCount() > 0) {
            var arr = [
            WtfGlobal.getLocaleText("acc.invoiceList.expand.PID"), // "Product ID"
            WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), // "Product Name"
            WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"), // "Quantity"
            WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice"), // "Unit Price"
            WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"), // "Discount"
            WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"), // "Amount"
            "                  "];
           
            var gridHeaderText = WtfGlobal.getLocaleText("acc.invoiceList.expand.pList"); // "Product List";
        
            var header = "<span class='gridHeader'>"+gridHeaderText+"</span>"; // "Product List"
            var custArr = [];
            var custArr = WtfGlobal.appendCustomColumn(custArr, GlobalColumnModel[this.moduleid]);
            var arrayLength = arr.length;
            for (var cnt1 = 0; cnt1 < custArr.length; cnt1++) {
                if (custArr[cnt1].header != undefined)
                    arr[arrayLength + cnt1] = custArr[cnt1].header;
            }
            var count=0;
            for(var i=0;i<arr.length;i++){
                if(arr[i] != ""){
                    count++;
                }
            }
            var widthInPercent=100/count;
            var minWidth = count*100 + 40;
            header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
            header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
            for(i=0;i<arr.length;i++){
                header += "<span class='headerRow' style='width:"+widthInPercent+"% ! important;'>" + arr[i] + "</span>";
            }
            header += "</div><div style='width: 100%;min-width:"+minWidth+"px'><span class='gridLine'></span></div>";   
            header += "<div style='width: 100%;min-width:"+minWidth+"px'>";
        
            for(i=0; i<this.expandStore.getCount(); i++) {
                var rec=this.expandStore.getAt(i);
            
                // Column : S.No.
                header += "<span class='gridNo'>"+(i+1)+".</span>";           
                // Column : Product ID
                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;' wtf:qtip='"+rec.data['productcode']+"'><a class='jumplink' wtf:qtip='"+rec.data['productcode']+"' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\""+rec.data['productid']+"\","+this.isFixedAsset+")'>"+Wtf.util.Format.ellipsis(rec.data['productcode'],10)+"</a></span>";   // ERP-13247 [SJ]
                // Column : Product Name
                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;' wtf:qtip='"+rec.data['productname']+"'><a class='jumplink' wtf:qtip='"+rec.data['productname']+"' href='#' onClick='javascript:Wtf.onCellClickProductDetails(\""+rec.data['productid']+"\","+this.isFixedAsset+")'>"+Wtf.util.Format.ellipsis(rec.data['productname'],10)+"</a></span>";   // ERP-13247 [SJ]
                // Column : Quantity of Product
                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['quantity']+" "+rec.data['unitname']+"</span>";
                // Column : Unit Price of Product
                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['rate'],rec.data['currencysymbol'],[true])+"</span>";
                // Column : Discount
                if(rec.data.discountispercent == 0){
                    header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['prdiscount'],rec.data['currencysymbol'],[true])+"</span>";
                } else {
                    header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+rec.data['prdiscount']+"%"+"&nbsp;</span>";
                }
                // Column : Amount
                header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.addCurrencySymbolOnly(rec.data['amount'],rec.data['currencysymbol'],[true])+"</span>";
            
            for(var cnt2=0;cnt2<custArr.length;cnt2++){
                        if(rec.data[custArr[cnt2].dataIndex]!=undefined && rec.data[custArr[cnt2].dataIndex]!="null")
                        //    header += "<span class='gridRow' style='width: 7.5% ! important;'>"+rec.data[custArr[cnt2].dataIndex]+"&nbsp;</span>";
                           header += "<span class='gridRow' wtf:qtip='"+rec.data[custArr[cnt2].dataIndex]+"' style='width:"+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec.data[custArr[cnt2].dataIndex],15)+"&nbsp;</span>";
                         else
                            header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>&nbsp;&nbsp;</span>";
                   }
                   header +="<br>";
            
            }
        header += "</div>";
            var disHtml = "<div class='expanderContainer' style='width:100%'>" + header + "</div>";
            this.expanderBody.innerHTML = disHtml;
        } else {
            this.expanderBody.innerHTML = "<div class='expanderContainer' style='width:100%'>" + WtfGlobal.getLocaleText("acc.field.Nodatatodisplay") + "</div>"; // "No data to display"
        }
    },
    getMyConfig : function(){
        WtfGlobal.getGridConfig(this.grid, this.moduleId, false, false);
        
        var statusForCrossLinkageIndex = this.grid.getColumnModel().findColumnIndex("statusforcrosslinkage")
        if (statusForCrossLinkageIndex != -1) {
            this.grid.getColumnModel().setHidden(statusForCrossLinkageIndex, true);
        }
    },
    saveMyStateHandler: function(grid,state){
        WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleId, grid.gridConfigId, false);
    },
    handleCellClick: function (grid, rowNum, colNum, e) {
        if (e.target.className == 'jumplink') {
            if (grid.getStore() && grid.getStore().getAt(rowNum)) {
                callContractDetails(grid.getStore().getAt(rowNum), this.isNormalContract);
            }
        }
    }
});