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

function openAccRecTab(isReceivable){
    if(isReceivable)
        callCustomerDetails();
    else
        callVendorDetails();
}

Wtf.account.COAReport = function(config){
    Wtf.apply(this, config);
    this.accID="";
    this.isAdd=false;
    this.recArr=[];
    this.costCenterId = config.costCenterId?config.costCenterId:"";
    this.isEdit=false;
    this.accid=config.accid;
    this.record=config.record;
    this.nondeleted=false;
    this.isForBS_PL_to_GL=config.isForBS_PL_to_GL;
    this.accountID=config.accountID;
    this.stDate=config.stDate;
    this.enDate=config.enDate;
    this.label = WtfGlobal.getLocaleText("acc.coa.account");
    this.deleted=false;
    this.moduleId=config.moduleId;
    this.isGeneralLedger = (config.isGeneralLedger != undefined || config.isGeneralLedger != null)?config.isGeneralLedger:false;
    this.BalanceSummary=0; //Summary Amount In Base Currency
    this.endingBalanceSummary=0;
    this.exponly=config.exponly;
    this.filterConjuctionCrit=config.filterConjuctionCriteria;
    this.uPermType = this.isGeneralLedger?Wtf.UPerm.fstatement:Wtf.UPerm.coa;
    this.permType = this.isGeneralLedger?Wtf.Perm.fstatement:Wtf.Perm.coa;
    this.removePermType=this.permType.remove;    
    this.ispropagatetochildcompanyflag=false;//this flag is used to delete propagated record in child companies. 
    if(this.isForBS_PL_to_GL=="" || this.isForBS_PL_to_GL==null || this.isForBS_PL_to_GL==undefined){
        this.isForBS_PL_to_GL=false;
    }
    if(this.isGeneralLedger){        
        this.currencyRec = Wtf.data.Record.create ([                                
        {name:'currencyname',  mapping:'name'},
        {name:'currencyid',    mapping:'currencyid'},
        {name:'accountid'},
        {name:'acccode'},
        {name:'groupname'}
        ]);
                
        this.currencyStore = new Wtf.data.Store({
            url : "ACCCurrency/getCurrency.do",
            baseParams:{
                mode:2,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.currencyRec)
        });
                
        this.currencyStore.load();                
                
        this.MSCurrencyComboconfig = {
            hiddenName:'currencymulselectcombo',         
            store: this.currencyStore,
            valueField:'currencyid',
            hideLabel:false,
            hidden : false,
            displayField:'currencyname',
            emptyText: WtfGlobal.getLocaleText("acc.fxexposure.all"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus:true,
            triggerAction:'all',
            scope:this
        };
                    
        this.MultiSelectCurrencyCombo = new Wtf.common.Select(Wtf.applyIf({      //Currency
            id:'mulcurrencycombo'+this.id,
            multiSelect:true,
            fieldLabel: WtfGlobal.getLocaleText("acc.fxexposure.currency") ,
            forceSelection:true,  
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:250,
            width:100
        },this.MSCurrencyComboconfig));    

    } 
    this.delTypeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'int'}, 'name'],
        data :[
            [0, WtfGlobal.getLocaleText("acc.rem.105")],
            [1, WtfGlobal.getLocaleText("acc.rem.106")],
            [2, WtfGlobal.getLocaleText("acc.rem.107")],
            [3, WtfGlobal.getLocaleText("acc.rem.216")]
        ]
    });
    
    this.typeEditor = new Wtf.form.ComboBox({
        store: this.delTypeStore,
        name:'typeid',
        displayField:'name',
        id:'view'+config.id,
        valueField:'typeid',
        mode: 'local',
        value:0,
        width:110,        
        listWidth:210,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    this.createStore();
    if(this.isGeneralLedger){
        this.createColumnModelForGeneralLedger();
        this.createExpanderStore(this.MultiSelectCurrencyCombo.getValue());
    }else{
        this.createColumnModel();
    }
    this.createGrid();
    this.colModelArray = GlobalColumnModelForReports[this.moduleId];
    WtfGlobal.updateStoreConfig(this.colModelArray, this.store);
    this.LineLevelcolModelArray=GlobalColumnModel[this.moduleId];
    WtfGlobal.updateStoreConfig(this.LineLevelcolModelArray, this.store);
    this.store.load({
            params: {
                isFirstTimeLoad : Wtf.isFirstTimeLoad, //To avoid data load when we open the GL Report (ERP-28938)
                start: 0,
                limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
            }
        });
    this.store.on('load',this.hideMsg,this);
    this.getMyConfig();
    this.grid.on("render", function(grid) {
        new Wtf.util.DelayedTask().delay(Wtf.GridStateSaveDelayTimeout, function () {
            this.grid.on('statesave', this.saveMyStateHandler, this);
        }, this);
    },this);
    Wtf.account.COAReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.COAReport, Wtf.Panel,{
       loadMask : true,
       forceFit:true,
       onRender: function(config){
           Wtf.account.COAReport.superclass.onRender.call(this, config);
//        this.add(this.objsearchComponent);
     var FirstTopToolaBr = new Wtf.Toolbar(this.btnArr);
        this.leadpan = new Wtf.Panel({
            layout: 'border',
            border: false,
            attachDetailTrigger: true,
            items: [this.objsearchComponent
            , {
                region: 'center',
                layout: 'fit',
                border: false,
                items: [this.grid],
                tbar: FirstTopToolaBr,
                bbar: this.pToolBar
            }]
        }); 
        this.add(this.leadpan);
        WtfGlobal.getReportMenu(FirstTopToolaBr, Wtf.Account_Statement_ModuleId, WtfGlobal.getModuleName(Wtf.Account_Statement_ModuleId));
    },
    hideMsg:function(){
        Wtf.MessageBox.hide();
    },
    createStore:function(){
        this.coaRec = new Wtf.data.Record.create([
            {name: 'accid'},
            {name: 'accname'},
            {name: 'groupid'},
            {name: 'groupname'},
            {name: 'level'},
            {name: 'leaf'},
            {name: 'openbalance'},
            {name: 'orignalopenbalance'},
            {name: 'purchasetype'},
            {name: 'salestype'},
            {name: 'openbalanceinbase'},
            {name: 'presentbalance'},
            {name: 'presentbalanceInBase'},
            {name: 'salvage'},
            {name: 'budget'},
            {name: 'acccode'},
//            {name: 'accnamecode'}, //Not in Use ERP-34368
            {name: 'taxid'},
            {name: 'life'},
            {name: 'parentid'},
            {name: 'currencysymbol'},
            {name: 'currencyname'},
            {name: 'currencyid'},
            {name: 'parentname'},
            {name: 'creationDate' ,type:'date'},
//            {name: 'creationDate' },//,type:'date' //This date is handled on JAVA side & sent as String only
            {name: 'deleted'},
            {name: 'categoryid'},
            {name: 'departmentid'},
            {name: 'installation'},
            {name: 'locationid'},
            {name: 'userid'},
            {name: 'costcenterid'},
            {name: 'costcenterName'},
            {name: 'id'},
            {name: 'isOnlyAccount'},
            {name: 'isHeaderAccount'},
            {name: 'accountHasJedTransaction'},
            {name: 'accountTypeTransaction'},
            {name: 'eliminateflag'},
            {name: 'endingBalance'},
            {name: 'endingBalanceSummary'},
            {name: 'periodBalance'},
            {name: 'accounttype'},
            {name: 'accounttypestring'},
            {name: 'mastertypevalue'},
            {name: 'mastertypevaluestring'},
            {name: 'custminbudget'},
            {name: 'ifsccode'},
            {name: 'accountHasOpeningTransactions'},
            {name: 'aliascode'},
            {name: 'controlAccounts'},
            {name: 'isibgbank'},
            {name: 'ibgbankdetailid'},
            {name: 'ibgbank'},
            {name: 'ibgbanktype'},
            {name: 'bankCode'},
            {name: 'branchCode'},
            {name: 'accountNumber'},
            {name: 'accountName'},
            {name: 'sendersCompanyID'},
            {name: 'bankDailyLimit'},
            {name: 'accdesc'},
            {name: 'isactivate'},
            {name: 'bankAccountNumber'},
            {name: 'serviceCode'},
            {name: 'ordererName'},
            {name: 'currencyCode'},
            {name: 'settlementMode'},
            {name: 'postingIndicator'},
            {name: 'cimbbankdetailid'},            
            {name: 'bankbranchname'},
            {name: 'accountno'},
            {name: 'bankbranchaddress'},
            {name: 'branchstate'},
            {name: 'bsrcode'},
            {name: 'mvatcode'},
            {name: 'pincode'},
            {name: 'uobOriginatingBICCode'},
            {name: 'uobCurrencyCode'},
            {name: 'uobOriginatingAccountNumber'},
            {name: 'uobOriginatingAccountName'},
            {name: 'uobUltimateOriginatingCustomer'},
            {name: 'uobbankdetailid'},
            {name: 'uobCompanyId'},
            {name: 'ocbcbankdetailid'},
            {name: 'ocbcOriginatingBankCode'},
            {name: 'ocbcAccountNumber'},
            {name: 'ocbcReferenceNumber'},
            {name: 'accountUsedAsInventoryAccountInProduct'},
            {name: 'nature'}
        ]);
        this.msgLmt = 30;
        this.jReader = new Wtf.data.KwlJsonReader({
            totalProperty: 'totalCount',
            root: "data"
        }, this.coaRec);

        var baseParamsArr = {};
        baseParamsArr.mode=2;

        baseParamsArr.ignore=true;
        baseParamsArr.group=[12];
        
        baseParamsArr.controlAccounts=true;
        baseParamsArr.isFromCOA = true;
        this.store = new Wtf.data.Store({
            reader: this.jReader,
            remoteSort:true,
//            url: Wtf.req.account +'CompanyManager.jsp',
            url: this.isGeneralLedger?"ACCReports/getGeneralLedger.do":"ACCReports/getAccounts.do",
            baseParams:baseParamsArr
        });
        this.store.on('loadexception', function(){
            Wtf.MessageBox.hide();
        }, this);
        this.store.on('beforeload',function(s,o){
            WtfGlobal.setAjaxTimeOut();
            if(!o.params)o.params={};
            o.params.deleted=this.deleted;
            o.params.nondeleted=this.nondeleted;
            o.params.acctypes=this.typeEditor.getValue();
//            if(this.isGeneralLedger)
//                o.params.currencytype=this.currencyType.getValue();            
            if(this.pToolBar){
                if(this.typeEditor.getValue() == "3"){
                    this.addClass("coaclass");
                } else {
                    this.removeClass("coaclass");
                }
            }
            o.params.includeExcludeChildBalances = (this.includeExcludeChildCmb != undefined || this.includeExcludeChildCmb != null) ? this.includeExcludeChildCmb.getValue() : true; 
            if(this.isGeneralLedger){
                if(this.startDate != undefined || this.startDate != null){
//                    this.sDate=this.startDate.getValue();
                    this.sDate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
                }else{
                    this.sDate=WtfGlobal.convertToGenericStartDate(this.getDates(true));
                }
                if(this.endDate != undefined || this.endDate != null){
//                    this.eDate=this.endDate.getValue();
                    this.eDate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
                }else{
                    this.eDate=WtfGlobal.convertToGenericEndDate(this.getDates(false));
                }
            
                o.params.startDate=this.sDate;
                o.params.stdate=this.sDate;
                o.params.endDate=this.eDate;
                o.params.isGeneralLedger=this.isGeneralLedger;
                o.params.isForBS_PL_to_GL = this.isForBS_PL_to_GL;
                o.params.accountIds=(this.MultiSelectAccCombo != undefined || this.MultiSelectAccCombo != null)?this.MultiSelectAccCombo.getValue():"";
                o.params.currencyIds=(this.MultiSelectCurrencyCombo != undefined || this.MultiSelectCurrencyCombo != null)?this.MultiSelectCurrencyCombo.getValue():"";
                o.params.excludePreviousYear = (this.excludeTypeCmb != undefined || this.excludeTypeCmb != null)?this.excludeTypeCmb.getValue():true;
//                o.params.includeExcludeChildBalances = (this.includeExcludeChildCmb != undefined || this.includeExcludeChildCmb != null) ? this.includeExcludeChildCmb.getValue() : true; 
                o.params.balPLId=(this.balPLTypeCombo != undefined || this.balPLTypeCombo != null)?this.balPLTypeCombo.getValue():"";
//                o.params.start=0;
                if(((this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt) != "All"){
                    o.params.limit = (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt;
                }else{
                    var count = this.store.getTotalCount();
                    var rem = count % 5;
                    if(rem == 0){
                        count = count;
                    }else{
                        count = count + (5 - rem);
                    }
                    o.params.limit = count; 
                }
                if(this.accountMasterTypeCombo!=undefined && this.accountMasterTypeCombo.getValue()==1){
                    o.params.ignoreBankAccounts=true;
                }else if(this.accountMasterTypeCombo!=undefined && this.accountMasterTypeCombo.getValue()==2){
                    o.params.ignoreCashAccounts=true;
                }
                if(this.searchJson != undefined && this.searchJson !=""){
                    o.params.searchJson=this.searchJson;
                }
                if (this.filterConjuctionCrit != undefined && this.filterConjuctionCrit != "") {
                    o.params.filterConjuctionCriteria = this.filterConjuctionCrit;
                }
                if (this.accountTransactionMasterTypeCombo != undefined  && this.accountTransactionMasterTypeCombo.getValue()!=0) {// when component present and transaction type not all.
                    o.params.accountTransactionType = this.accountTransactionMasterTypeCombo.getValue();
                }
            } else{
                 if(((this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt) != "All"){
                    o.params.limit = (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt;
                  }else{
                    var count = this.store.getTotalCount();
                    var rem = count % 5;
                    if(rem == 0){
                        count = count;
                    }else{
                        count = count + (5 - rem);
                    } 
                    o.params.limit = count; 
                }
            }
            if(!this.isGeneralLedger && this.MultiSelectGroupCombo){
                o.params.accgroupids=this.MultiSelectGroupCombo.getValue();
            }
        },this);
        
        if(!this.isGeneralLedger){// Below code does nor required for only in Accoount Report So here is check of !this.isGeneralLedger
            var groupRec=new Wtf.data.Record.create([
                {name: 'groupid'},
                {name: 'groupname'},
                {name: 'nature'},
                {name: 'mastergroupid'},
                {name: 'naturename'},
                {name: 'leaf',type:'boolean'},
                {name: 'level', type:'int'}
            ]);

            this.groupStore=new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                },groupRec),
                url : "ACCAccount/getGroups.do",
                baseParams:{
                    mode:1,
                    ignorevendors:false,
                    ignorecustomers:false
                }
            });
            this.groupStore.load();
        }
//        this.store.load({
//            params: {
//                start: 0,
//                limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt
//            }
//        });
       WtfComMsgBox(29,4,true);
    },

    createColumnModel:function(){
        this.summary = new Wtf.ux.grid.GridSummary();
        this.selectionModel = new Wtf.grid.CheckboxSelectionModel({
            	header: (Wtf.isIE7)?"":'<div class="x-grid3-hd-checker"> </div>',    // For IE 7 the all select option not available
        	hidden:this.isDepreciation
        });
        this.gridColumnModelArr=[];
        this.gridColumnModelArr.push(this.selectionModel,
        {
            header: WtfGlobal.getLocaleText("acc.coa.accCode"), //"Account Code",
            dataIndex: 'acccode',
            sortable:true,
            pdfwidth:150,
            renderer:WtfGlobal.deletedRenderer
        },
        {
            header: WtfGlobal.getLocaleText("acc.coa.aliasCode"), //"Alias Code",
            dataIndex: 'aliascode',
            sortable:true,
            pdfwidth:150,
            renderer:WtfGlobal.deletedRenderer  
        },
        {
            header: WtfGlobal.getLocaleText("acc.coa.gridAccountName"),  //"Asset Name":"Account Name",
            dataIndex: 'accname',
            renderer:WtfGlobal.deletedRenderer,
            sortable:true,
            pdfwidth:150
           // summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.coa.total")+'</div>'}
        },
        {
            header: WtfGlobal.getLocaleText("acc.field.AccountDescription"),  //"Account Description",
            dataIndex: 'accdesc',
            renderer:WtfGlobal.deletedRenderer,
            sortable:true,
            pdfwidth:150,
            renderer: function(value) {
                    value = value.replace(/\'/g, "&#39;");
                    value = value.replace(/\"/g, "&#34");
                    return "<span class=memo_custom  wtf:qtip='" + value + "'>" + Wtf.util.Format.ellipsis(value, 60) + "</span>"
                }
        },{
            header: WtfGlobal.getLocaleText("acc.coa.gridType"), //"Type",
            dataIndex: 'groupname',
            renderer:WtfGlobal.deletedRenderer,
            sortable:true,
            pdfwidth:150
        },{
            header:WtfGlobal.getLocaleText("acc.coa.gridAccType"),  // "Account Type",
            dataIndex: 'accounttypestring',
            sortable:true,
            pdfwidth:150
        },
        {
            header:WtfGlobal.getLocaleText("coa.masterType.title"),  // "Master Type",
            dataIndex: 'mastertypevaluestring',
//            sortable:true,
            pdfwidth:150
        },{
            header:WtfGlobal.getLocaleText("acc.accreport.Status"),  // "Status", Activate/Deactivate
            dataIndex: 'isactivate',
            pdfwidth:150,                                //ERP-38772-need for exporting files
            renderer:function(val,m,rec){                    
                if(val)     
                    return 'Active';
                else
                    return 'Dormant';  
            }
        },{
            header: WtfGlobal.getLocaleText("acc.coa.gridCreationDate"), //"Creation Date",
            dataIndex: "creationDate",
             align:'center',
            //This date is handled on JAVA side & sent as String only
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            sortable:true,
            pdfwidth:150,
            pdfrenderer : "date"
        },{
            header : WtfGlobal.getLocaleText("acc.coa.gridOpeningBalance"),  //'Asset Value':'Opening Balance',
            dataIndex: 'openbalance',
            align:'right',
            renderer:function(val,m,rec){                    
                    if(rec.data.isHeaderAccount && !(rec.data.groupid=="12")){      //Not a fixed Asset(groupid=12)
                        return '<div style="margin-right:15px;">-</div>';
                    }else{
                        return WtfGlobal.withoutRateCurrencyDeletedSymbol(Math.abs(val),m,rec);  
                    }
                },
//            summaryType:'sum',
//            summaryRenderer:this.opBalRenderer,
            pdfrenderer : "rowcurrency",
            pdfwidth:150
        },{
            header :WtfGlobal.getLocaleText("acc.coa.gridCurrency"), //'Currency',
            renderer:WtfGlobal.deletedRenderer,
            dataIndex: 'currencyname',
            pdfwidth:150,
            sortable:true
        },{
            header :WtfGlobal.getLocaleText("acc.coa.gridOpeningBalanceType"), //'Opening Balance Type',
            dataIndex: 'openbalance',
           // summaryType:'sum',
           // summaryRenderer:this.balTypeRenderer,
            pdfwidth:150,
            renderer:this.balTypeRenderer
        },{
            header :WtfGlobal.getLocaleText("acc.coa.gridOpeningBalance") +" "+ WtfGlobal.getLocaleText("acc.fixedAssetList.grid.homCur"),//'Asset Value':'Opening Balance')+" in Home Currency",
            dataIndex: 'openbalanceinbase',
            align:'right',
            renderer:WtfGlobal.currencyDeletedRenderer,
           // summaryRenderer: this.showBalanceSummary.createDelegate(this),
            pdfwidth:150,
            pdfrenderer : "currency"
        }
//        ,{
//            header :WtfGlobal.getLocaleText("acc.field.PeriodBalanceinBaseCurrency"),
//            dataIndex:'periodBalance',
//            align:'right',
//            hidden:!this.isGeneralLedger,
//            renderer:WtfGlobal.currencyDeletedRenderer,
////            summaryRenderer: this.showEndingBalanceSummary.createDelegate(this),
//            pdfwidth:150,
//            pdfrenderer : "currency"
//        }
        ,{
            header :WtfGlobal.getLocaleText("acc.field.EndingBalanceinBaseCurrency"),
            dataIndex:'endingBalance',
            align:'right',
            renderer:WtfGlobal.currencyDeletedRenderer,
           // summaryRenderer: this.showEndingBalanceSummary.createDelegate(this),
            pdfwidth:150,
            pdfrenderer : "currency"
        });
        /*Appending Custom Column in Grid of Customer Details*/
        this.gridColumnModelArr = WtfGlobal.appendCustomColumn(this.gridColumnModelArr,GlobalColumnModelForReports[this.moduleId],true);
        this.gridColumnModelArr = WtfGlobal.appendCustomColumn(this.gridColumnModelArr,GlobalColumnModel[this.moduleId],true,undefined,undefined,undefined,this.moduleId);
        this.gridcm = new Wtf.grid.ColumnModel(this.gridColumnModelArr);
        this.gridcm.defaultSortable = false;
    },
    
    createColumnModelForGeneralLedger:function(){
        this.expander = new Wtf.grid.RowExpander({});
        this.expander.on("expand",this.onRowexpand,this);
        this.summary = new Wtf.ux.grid.GridSummary();
        this.selectionModel = new Wtf.grid.CheckboxSelectionModel({
            	header: (Wtf.isIE7)?"":'<div class="x-grid3-hd-checker"> </div>'    // For IE 7 the all select option not available
        });
        this.gridcm= new Wtf.grid.ColumnModel([this.selectionModel,this.expander,
        {
            header: WtfGlobal.getLocaleText("acc.coa.accCode"), //"Account Code",
            dataIndex: 'acccode',
            pdfwidth:150,
            renderer:WtfGlobal.deletedRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.coa.aliasCode"), //"Alias Code",
            dataIndex: 'aliascode',
            pdfwidth:150,
            renderer:WtfGlobal.deletedRenderer  
        },{
            header: WtfGlobal.getLocaleText("acc.coa.gridAccountName"),  //"Asset Name":"Account Name",
            dataIndex: 'accname',
            renderer:WtfGlobal.deletedRenderer,
            sortable:true,
            pdfwidth:150,
            summaryRenderer:function(){return '<div class="grid-summary-common">'+WtfGlobal.getLocaleText("acc.coa.total")+'</div>'}
        },{
            header: WtfGlobal.getLocaleText("acc.coa.gridType"), //"Type",
            dataIndex: 'groupname',
            renderer:WtfGlobal.deletedRenderer,
            sortable:true,
            pdfwidth:150
        },{
            header: WtfGlobal.getLocaleText("acc.coa.gridCreationDate"), //"Creation Date",
            dataIndex: "creationDate",
             align:'center',
            //This date is handled on JAVA side & sent as String only
            renderer:WtfGlobal.onlyDateDeletedRenderer,
            pdfwidth:150,
            pdfrenderer : "date"
        },{
            header : WtfGlobal.getLocaleText("acc.coa.gridOpeningBalance"),  //'Opening Balance',
            dataIndex: 'openbalance',
            align:'right',
            renderer:function(val,m,rec){                    
                    if(rec.data.isHeaderAccount && !(rec.data.groupid=="12")){      //Not a fixed Asset(groupid=12)
                        return '<div style="margin-right:15px;">-</div>';
                    }else{
                        return WtfGlobal.withoutRateCurrencyDeletedSymbol(Math.abs(val),m,rec);  
                    }
                },
//            summaryType:'sum',
//            summaryRenderer:this.opBalRenderer,
            pdfrenderer : "rowcurrency",
            pdfwidth:150
        },{
            header :WtfGlobal.getLocaleText("acc.coa.gridCurrency"), //'Currency',
            renderer:WtfGlobal.deletedRenderer,
            dataIndex: 'currencyname',
            pdfwidth:150
        },{
            header :WtfGlobal.getLocaleText("acc.coa.gridOpeningBalanceType"), //'Opening Balance Type',
            dataIndex: 'openbalance',
            summaryType:'sum',
            summaryRenderer:this.balTypeRenderer,
            pdfwidth:150,
            renderer:this.balTypeRenderer
        },{
            header :WtfGlobal.getLocaleText("acc.coa.gridOpeningBalance") +" "+ WtfGlobal.getLocaleText("acc.fixedAssetList.grid.homCur"),//'Asset Value':'Opening Balance')+" in Home Currency",
            dataIndex: 'openbalanceinbase',
            align:'right',
            renderer:WtfGlobal.currencyDeletedRenderer,
            summaryRenderer: this.showBalanceSummary.createDelegate(this),
            pdfwidth:150,
            pdfrenderer : "currency"
        },{
            header :WtfGlobal.getLocaleText("acc.field.PeriodBalanceinBaseCurrency"),
            dataIndex:'periodBalance',
            align:'right',
            hidden:!this.isGeneralLedger,
            renderer:WtfGlobal.currencyDeletedRenderer,
//            summaryRenderer: this.showEndingBalanceSummary.createDelegate(this),
            pdfwidth:150,
            pdfrenderer : "currency"
        },{
            header :WtfGlobal.getLocaleText("acc.field.EndingBalanceinBaseCurrency"),
            dataIndex:'endingBalance',
            align:'right',
            renderer:WtfGlobal.currencyDeletedRenderer,
            summaryRenderer: this.showEndingBalanceSummary.createDelegate(this),
            pdfwidth:150,
            pdfrenderer : "currency"
        }]);
        this.gridcm.defaultSortable = false;
    },
    
    createExpanderStore : function(currencyIds){
        this.expandRec = Wtf.data.Record.create([
            {name: 'd_date',type:'date'},
            {name: 'd_accountname'},
            {name: 'd_acccode'},
            {name: 'd_entryno'},
	    {name: 'd_externalcurrencyrate'},
            {name: 'd_journalentryid'},
            {name: 'd_amount'},
            {name: 'd_transactionID'},
            {name: 'd_transactionDetails'},
            {name: 'd_transactionDetailsForExpander'},
            {name: 'c_date',type:'date'},
            {name: 'c_accountname'},
            {name: 'c_acccode'},
            {name: 'c_entryno'},
	    {name: 'c_externalcurrencyrate'},
            {name: 'c_journalentryid'},
            {name: 'c_amount'},
            {name: 'c_transactionID'},
            {name: 'c_transactionDetails'},
            {name: 'c_transactionDetailsForExpander'},
            {name: 'accountid'},
            {name: 'accountname'},
            {name: 'accCode'},
            {name: 'accCodeName'},
            {name: 'billid'},
            {name: 'type'},
            {name: 'noteid'},
            {name: 'c_amountAccountCurrency'},
            {name: 'd_amountAccountCurrency'},
            {name: 'currencysymbol'},
            {name: 'transactionCurrency'},
            {name: 'transactionAmount'},
            {name: 'transactionSymbol'},
            {name: 'transactionDateString'}
            
        ]);
        this.expandStore = new Wtf.data.Store({
            url:"ACCReports/getLedger.do",
            baseParams:{
//                mode:61,
                withoutinventory: Wtf.account.companyAccountPref.withoutinventory,
//                consolidateFlag:config.consolidateFlag,
                companyids:companyids,
                gcurrencyid:gcurrencyid,
                userid:loginid,
                generalLedgerFlag:true,
                currencyIds:currencyIds,
                isOpeningBal:""
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.expandRec)
        });
        this.expandStore.on('beforeload', function(){
            WtfGlobal.setAjaxTimeOutFor30Minutes();
        }, this);
        this.expandStore.on('loadexception', function(){
            WtfGlobal.resetAjaxTimeOut();
        }, this);
        this.expandStore.on('load', function(){
            WtfGlobal.resetAjaxTimeOut();
            this.fillExpanderBody();
        }, this);
    },
    
    onRowexpand:function(scope, record, body){
        this.expanderBody=body;
        if(this.startDate != undefined || this.startDate != null){
            this.sDate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        }else{
            this.sDate=WtfGlobal.convertToGenericStartDate(this.getDates(true));
        }
        if(this.endDate != undefined || this.endDate != null){
            this.eDate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());     //General Ledger Report //ERP-8205,8316,8328
        }else{
            this.eDate=WtfGlobal.convertToGenericEndDate(this.getDates(false));
        }
        this.expandStore.load({
            params:{
                accountid:record.data.accid,
                stdate:this.sDate,
                currencyIds:this.MultiSelectCurrencyCombo.getValue(),
                enddate:this.eDate,
                //currencytype:this.currencyType.getValue()
                searchJson: this.searchJson,
                filterConjuctionCriteria: this.filterConjuctionCrit,
                excludePreviousYear: this.excludeTypeCmb.getValue(),
                includeExcludeChildBalances : this.includeExcludeChildCmb.getValue(),
                accountTransactionType : this.accountTransactionMasterTypeCombo.getValue()
//                intercompanytypeid:this.cmbInterCompanyType.getValue(),
//                intercompanyflag:this.intercompanyFlag?true:false
            }
        });
    },
    
    fillExpanderBody: function(){
        var disHtml = "";
        var arr=[];
        var startDate=this.startDate.getValue();
        var endDate=this.endDate.getValue();
        var openingBalanceOfAccount = 0, totalDebitAmount=0, totalCreditAmount = 0;
        arr = [WtfGlobal.getLocaleText("acc.inventoryList.date"), WtfGlobal.getLocaleText("acc.coa.gridAccountName"), WtfGlobal.getLocaleText("acc.field.JournalFolio(J/F)"),WtfGlobal.getLocaleText("acc.product.description"),'Exchange Rate ('+WtfGlobal.getCurrencySymbol()+')',WtfGlobal.getLocaleText("acc.je.debitAmt"), WtfGlobal.getLocaleText("acc.je.creditAmt")];
        var gridHeaderText = WtfGlobal.getLocaleText("acc.ccReport.tab3");
        var header = "<span class='gridHeader'>"+gridHeaderText+"</span>";
//        header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
        
//        for(var i=0;i<arr.length;i++){
//            header += "<span class='headerRow' style='width: 14% ! important;'>" + arr[i] + "</span>";
//        }
        
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.inventoryList.date")+"'style='width: 6% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.inventoryList.date"),15)+ "</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.coa.gridDoubleEntryMovement")+"'style='width: 14% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.coa.gridDoubleEntryMovement"),25) + "</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.JournalFolio(J/F)")+"'style='width: 10% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.JournalFolio(J/F)"),20) + "</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.product.description")+"'style='width: 15% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.product.description"),40) + "</span>";
        header += "<span class='headerRow' wtf:qtip= 'Exchange Rate ("+WtfGlobal.getCurrencySymbol()+")"+"'style='width: 15% ! important;'>" + Wtf.util.Format.ellipsis('Exchange Rate ('+WtfGlobal.getCurrencySymbol()+')',22) + "&nbsp;</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.je.debitAmt")+"'style='width: 10% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.je.debitAmt"),16) + "</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.DebitAmountinBaseCurrency")+"'style='width: 10% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.DebitAmountinBaseCurrency"),16) + "</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.je.creditAmt")+"'style='width: 10% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.je.creditAmt"),16) + "</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.CreditAmountinBaseCurrency")+"'style='width: 10% ! important;'>" + Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.CreditAmountinBaseCurrency"),16) + "</span>";
        
        header += "<span class='gridLine'></span>";
        var continuedStoreCnt=0;
        for(i=0;i<this.expandStore.getCount();i++){
            header += "<span class='gridExpanderRow'>";
            var rec=this.expandStore.getAt(i);
            if(rec.data['d_accountname'] == 'Opening Balance' || rec.data['d_accountname'] == 'Balance c/f' || rec.data['d_accountname'] == 'Balance b/d' || rec.data['c_accountname'] == 'Opening Balance' || rec.data['c_accountname'] == 'Balance c/f' || rec.data['c_accountname'] == 'Balance b/d'){
                continuedStoreCnt++;
                continue;
            }  
            //Column : S.No.
//            header += "<span class='gridNo'>"+(i+1)+".</span>";
                
            //Column : Date
            if(rec.data['d_date'] != ''){
                header += "<span class='gridRow' wtf:qtip='"+rec.data['d_date'].format(WtfGlobal.getOnlyDateFormat())+"'style='width: 6% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['d_date'].format(WtfGlobal.getOnlyDateFormat()),15)+"&nbsp;</span>";
            }else{
                header += "<span class='gridRow' wtf:qtip='"+rec.data['c_date'].format(WtfGlobal.getOnlyDateFormat())+"'style='width: 6% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['c_date'].format(WtfGlobal.getOnlyDateFormat()),15)+"&nbsp;</span>";
            }
                
            //Column : Account Code
//            header += "<span class='gridRow' style='width: 9% ! important;'>"+rec.data['d_acccode']+"&nbsp;</span>";
  
            //Column : Account Name
            if(rec.data['d_accountname'] != ''){
                  header += "<span class='gridRow' wtf:qtip='"+rec.data['d_accountname']+"' style='width: 14% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['d_accountname'],20)+"&nbsp;</span>";
            }else{
                  header += "<span class='gridRow' wtf:qtip='"+rec.data['c_accountname']+"' style='width: 14% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['c_accountname'],20)+"&nbsp;</span>";
                 
            }
                
            //Column : Journal Folio
            if(rec.data['d_entryno'] != ''){
                var jid=rec.data['d_journalentryid'];
                header += "<a href='#' onClick=Wtf.onCellClick('"+jid+"',"+Wtf.encode(startDate)+","+Wtf.encode(endDate)+") >"+"<span class='gridRow' wtf:qtip='"+rec.data['d_entryno']+"'style='width: 10% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['d_entryno'],18)+"&nbsp;</span></a>";
            }else{
                var jid=rec.data['c_journalentryid'];
                header += "<a href='#' onClick=Wtf.onCellClick('"+jid+"',"+Wtf.encode(startDate)+","+Wtf.encode(endDate)+") >"+"<span class='gridRow' wtf:qtip='"+rec.data['c_entryno']+"'style='width: 10% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['c_entryno'],18)+"&nbsp;</span></a>";
            }
                
            //Column : Description
            if(rec.data['c_transactionDetails'] !=''){
                var value = rec.data['c_transactionDetailsForExpander'];
                value = value.replace(/\'/g, "&#39;");
                header += "<span class='gridRow' wtf:qtip='"+value+"' style='width: 15% ! important;'>"+Wtf.util.Format.ellipsis(value,30)+"&nbsp;</span>";
          }else{
                var value1 = rec.data['d_transactionDetailsForExpander'];
                value1 = value1.replace(/\'/g, "&#39;");
                header += "<span class='gridRow' wtf:qtip='" + value1 + "' style='width: 15% ! important;'>" + Wtf.util.Format.ellipsis(value1, 30) + "&nbsp;</span>";
             }
             //Column : Description
            if(rec.data['transactionCurrency']!=undefined&&rec.data['transactionCurrency'] !=''){
                if(rec.data['transactionCurrency']==WtfGlobal.getCurrencyID()){
                    header += "<span class='gridRow' wtf:qtip='1' style='width: 15% ! important;'>1 &nbsp;</span>";
                }else{
                    var value ;
                    if(rec.data['d_amount'] != '' && (rec.data['d_externalcurrencyrate']!='' && rec.data['d_externalcurrencyrate']>0)){
                        var value = 1 / ((rec.data['d_externalcurrencyrate']) - 0);
                        value = (Math.round(value * Wtf.Round_Off_Number)) / Wtf.Round_Off_Number;
                        //header += "<span class='gridRow' wtf:qtip='"+rec.data['transactionSymbol']+'('+rec.data['transactionDateString']+') '+value+"'style='width: 10% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['transactionSymbol']+'('+rec.data['transactionDateString']+') '+value,22)+"&nbsp;</span>";
                    }else if(rec.data['d_amount'] != ''){   //if externalcurrencyrate == 0
                        value = getRoundofValueWithValues((rec.data['d_amount']/rec.data['transactionAmount']), Wtf.CURRENCY_RATE_DIGIT_AFTER_DECIMAL);
                    }else if(rec.data['c_amount'] != '' && (rec.data['c_externalcurrencyrate']!='' && rec.data['c_externalcurrencyrate']>0)){
                        var value = 1 / ((rec.data['c_externalcurrencyrate']) - 0);
                        value = (Math.round(value * Wtf.Round_Off_Number)) / Wtf.Round_Off_Number;
                        //header += "<span class='gridRow' wtf:qtip='"+rec.data['transactionSymbol']+'('+rec.data['transactionDateString']+') '+(Math.round((rec.data['c_amount']/rec.data['transactionAmount'])*1000)/1000)+"'style='width: 10% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['transactionSymbol']+'('+rec.data['transactionDateString']+') '+(Math.round((rec.data['c_amount']/rec.data['transactionAmount'])*1000)/1000),22)+"&nbsp;</span>"; 
                    } else if(rec.data['c_amount'] != ''){  //if externalcurrencyrate == 0
                        value = getRoundofValueWithValues((rec.data['c_amount']/rec.data['transactionAmount']), Wtf.CURRENCY_RATE_DIGIT_AFTER_DECIMAL);
                    }
                    header += "<span class='gridRow' wtf:qtip='"+rec.data['transactionSymbol']+'('+rec.data['transactionDateString']+') '+value+"'style='width: 15% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['transactionSymbol']+'('+rec.data['transactionDateString']+') '+value,30)+"&nbsp;</span>";
                }
            }
            else{
                 header += "<span class='gridRow' wtf:qtip='' style='width: 10% ! important;'>&nbsp;</span>";
             }
              
            //Column : Transaction ID
//            header += "<span class='gridRow' style='width: 9% ! important;'>"+rec.data['d_transactionID']+"&nbsp;</span>";
                
            //Column : Debit Amount
            if(rec.data['d_amount'] != ''){
                var dAmtWitSymbol=WtfGlobal.conventInDecimal(rec.data['transactionAmount'],rec.data['transactionSymbol']);                          
                header += "<span class=\"gridRow\" wtf:qtip=\""+dAmtWitSymbol+"\"style=\"width: 10% ! important;\">"+dAmtWitSymbol+"&nbsp;</span>";
               
            }else{
                header += "<span class='gridRow'  style='width: 10% ! important;'>&nbsp;</span>";
            }
            //Debit Amount in BASE
            if(rec.data['d_amount'] != ''){
                var dAmtWitSymbol=WtfGlobal.conventInDecimal(rec.data['d_amount'],WtfGlobal.getCurrencySymbol());                          
                header += "<span class=\"gridRow\" wtf:qtip=\""+dAmtWitSymbol+"\"style=\"width: 10% ! important;\">"+dAmtWitSymbol+"&nbsp;</span>";
                totalDebitAmount+=rec.data['d_amount'];
            }else{
                header += "<span class='gridRow'  wtf:qtip='"+rec.data['d_amount']+"'style='width: 10% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['d_amount'],20)+"&nbsp;</span>";
            }
            
            
                
            //Column : Credit Amount
            
            if(rec.data['c_amount'] != ''){
                var cAmtWitSymbol=WtfGlobal.conventInDecimal(rec.data['transactionAmount'],rec.data['transactionSymbol']);                          
                header += "<span class=\"gridRow\" wtf:qtip=\""+cAmtWitSymbol+"\" style=\"width: 10% ! important;\">"+cAmtWitSymbol+"&nbsp;</span>";
            }else{
                header += "<span class='gridRow' style='width: 10% ! important;'>&nbsp;</span>";
            }
            
            //Credit amount in BASE
            if(rec.data['c_amount'] != ''){
                var cAmtWitSymbol=WtfGlobal.conventInDecimal(rec.data['c_amount'],WtfGlobal.getCurrencySymbol());                
                header += "<span class=\"gridRow\" wtf:qtip=\""+cAmtWitSymbol+"\" style=\"width: 10% ! important;\">"+cAmtWitSymbol+"&nbsp;</span>";
                totalCreditAmount+=rec.data['c_amount'];
            }else{
                header += "<span class='gridRow' wtf:qtip='"+rec.data['c_amount']+"'style='width: 10% ! important;'>"+Wtf.util.Format.ellipsis(rec.data['c_amount'],20)+"&nbsp;</span>";
            }
            
            
                
            header +="</span></br>";
                
        }
        /*
        header += "<span class='gridLineBottom'></span>";
        header += "<span class='headerRow' style='width: 70% ! important;'>"+ WtfGlobal.getLocaleText("acc.common.total")+"</span>";
        header += "<span class='headerRow' style='width: 14% ! important;'>"+ WtfGlobal.addCurrencySymbolOnly(totalDebitAmount,WtfGlobal.getCurrencySymbol(),[true])+"</span>";
        header += "<span class='headerRow' style='width: 12% ! important;'>"+ WtfGlobal.addCurrencySymbolOnly(totalCreditAmount,WtfGlobal.getCurrencySymbol(),[true])+"</span>";
        */
        header += "<span class='gridLineBottom'></span>";
        
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.total")+"'style='width: 10% ! important;'>" + WtfGlobal.getLocaleText("acc.common.total") + "</span>";
        header += "<span class='headerRow' style='width: 14% ! important;'>&nbsp;</span>";
        header += "<span class='headerRow' style='width: 10% ! important;'>&nbsp;</span>";
        header += "<span class='headerRow' style='width: 16% ! important;'>&nbsp;</span>";
        header += "<span class='headerRow' style='width: 10% ! important;'>&nbsp;</span>";
        header += "<span class='headerRow' style='width: 10% ! important;'>&nbsp;</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.addCurrencySymbolOnly(totalDebitAmount,WtfGlobal.getCurrencySymbol(),[true])+" 'style='width: 10% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(totalDebitAmount,WtfGlobal.getCurrencySymbol(),[true]) + "</span>";
        header += "<span class='headerRow' style='width: 10% ! important;'>&nbsp;</span>";
        header += "<span class='headerRow' wtf:qtip='"+WtfGlobal.addCurrencySymbolOnly(totalCreditAmount,WtfGlobal.getCurrencySymbol(),[true])+" 'style='width: 10% ! important;'>" + WtfGlobal.addCurrencySymbolOnly(totalCreditAmount,WtfGlobal.getCurrencySymbol(),[true]) + "</span>";
        
        /**/
        
        if(this.expandStore.getCount()==0 || this.expandStore.getCount()==continuedStoreCnt){
            header = "<span class='gridHeader'>"+gridHeaderText+"</span>";
            header += "<span class='headerRow'>"+WtfGlobal.getLocaleText("acc.field.Nodatatodisplay")+"</span>"
        }
        disHtml += "<div class='expanderContainer' style='min-width: 970px !important;'>" + header + "</div>";
        this.expanderBody.innerHTML = disHtml;
       },

    showBalanceSummary:function(){
        return WtfGlobal.currencySummaryRenderer(this.BalanceSummary);
    },

    showEndingBalanceSummary:function(){
        return WtfGlobal.currencySummaryRenderer(this.endingBalanceSummary);
    },

    createGrid:function(){
        this.quickSearchTF = new Wtf.KWLTagSearch({
            emptyText: WtfGlobal.getLocaleText("acc.coa.accountSearchText"), //'Search by Name',
            width: 130,
            field: 'accname',
            Store:this.store
        });
         this.resetBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            hidden:this.isSummary,
            tooltip :WtfGlobal.getLocaleText("acc.coa.resetTT"), //'Allows you to add a new search account name by clearing existing search account names.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
            disabled :false
        });
        this.resetBttn.on('click',this.handleResetClick,this);
        this.tbar2 = new Array();
        this.grid = new Wtf.grid.HirarchicalGridPanel({
            layout:'fit',
            store: this.store,
            cm: this.gridcm,
            tbar:this.tbar2,
            sm : this.selectionModel,
            hirarchyColNumber:2,
            plugins:this.isGeneralLedger?[this.summary, this.expander]:[this.summary],
            autoScroll:true,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:false,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"))
            }
        });
//        this.grid.on("render", function(grid) {
//            WtfGlobal.autoApplyHeaderQtip(grid);
//        },this);
        this.pageLimit = new Wtf.forumpPageSize({
            ftree:this.grid
        });
        this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid: this.isGeneralLedger ? 102 : this.moduleId,
        reportid: this.isGeneralLedger ? Wtf.autoNum.GeneralLedger : '',
        advSearch: false
    });
      this.bottombtnArr=[];
    
        this.trialbalButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.LedgerTReport"),
            tooltip:WtfGlobal.getLocaleText("acc.field.ShowsLedgerReport"),
            iconCls:'accountingbase fetch',
            scope:this,
            handler: function(){
                var rec=this.grid.getSelections();
                var acc="";
                for(var record=0;record<this.grid.getSelections().length;record++){
                    acc=acc+rec[record].data.accid+",";
                }
                if(acc!="" && acc!=undefined){
                    acc=acc.substring(0,acc.length-1);
                }
                callLedger(acc);
            }
        });
        if(!WtfGlobal.EnableDisable(this.uPermType, this.isGeneralLedger?this.permType.exportdataledger:this.permType.exportdata)){          
            this.exportselRec=new Wtf.exportButton({
                obj:this,
                id:"printReports",
                iconCls: 'pwnd exportpdfsingle',
                text: WtfGlobal.getLocaleText("acc.field.ExportSelRecord"),// + " "+ singlePDFtext,
                tooltip :WtfGlobal.getLocaleText("acc.field.ExportSelRecord"),  //'Export selected record(s)'
                disabled :true,
                hidden:this.isRequisition || this.isRFQ || this.isSalesCommissionStmt,
                menuItem:{csv:true,pdf:false,rowPdf:false,subMenu:true},
                params:this.isGeneralLedger?{
                    controlAccounts:true,
                    group:[12],
                    ignore:true,
                    isGeneralLedger:this.isGeneralLedger,
                    isExportingSelectedRecord:true,
                    generalLedgerFlag:true,
                    exportThreadFlagLedger : Wtf.account.companyAccountPref.downloadglprocessflag
                }:({group:[12],ignore:true})
         
            });
            this.exportselRec.on('click',function(){
                var accIDs="";
                for (var i=0;i<this.grid.getSelectionModel().getCount();i++){
                    accIDs+=this.grid.getSelectionModel().getSelections()[i].data.accid+",";
                 
                }
                if(accIDs.length > 1)
                    accIDs=accIDs.substring(0,accIDs.length-1);
                this.exportselRec.params.startDate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
                this.exportselRec.params.stdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
                this.exportselRec.params.endDate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
                this.exportselRec.params.enddate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
                this.exportselRec.params.balPLId=this.balPLTypeCombo.getValue();
                this.exportselRec.params.accountIds=accIDs;
                this.exportselRec.params.isExportingSelectedRecord=true;
                this.exportselRec.params.excludePreviousYear = this.excludeTypeCmb.getValue();
                this.exportselRec.params.includeExcludeChildBalances = this.includeExcludeChildCmb.getValue(),
                this.exportselRec.params.searchJson = this.searchJson,
                this.exportselRec.params.filterConjuctionCriteria = this.filterConjuctionCrit;
                if(this.accountTransactionMasterTypeCombo!=undefined){
                    this.exportselRec.params.accountTransactionType = this.accountTransactionMasterTypeCombo.getValue();
                }
            },this);
            if(this.isGeneralLedger){
                this.bottombtnArr.push('-',this.exportselRec);    
            }
        }
        if(this.isGeneralLedger){
            this.bottombtnArr.push('-',this.trialbalButton);
        }
        this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
        this.pToolBar = new Wtf.PagingSearchToolbar({
            id: 'pgTbar' + this.id,
            pageSize: this.msgLmt,
            store: this.store,
//            displayInfo: true,
            searchField: this.quickSearchTF,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No records to display",
            plugins: this.pageLimit,
            items : this.bottombtnArr 
        });
         this.store.on("load", this.setPageSize, this);
        this.store.on('datachanged', function() {
            if(this.pageLimit.combo) {
                var p = this.pageLimit.combo.value;
                this.quickSearchTF.setPage(p);
            }
        }, this);
        this.grid.getStore().on('load',function(store){
            Wtf.uncheckSelAllCheckbox(this.selectionModel);
            if(store.getCount()==0){
//                this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                this.grid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"));   //ERP-28938
                this.grid.getView().refresh();
                if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.print))
                    this.printData.disable();
                if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportdata))
                    this.exportData.disable(); 
            }else{
                if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.print))
                    this.printData.enable();
                if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportdata))
                    this.exportData.enable();
            }
        },this)

        this.btnArr=[];
        this.btnArrEDSingleS=[]; // Enable/Disable button's indexes on single select
        this.btnArrEDMultiS=[]; // Enable/Disable button's indexes on multi select

        this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), //"Advanced Search",
            //        id: 'advanced3', // In use, Do not delete
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), //'Search for multiple terms in multiple fields.',
            handler: this.configurAdvancedSearch,
            iconCls: "advanceSearchButton"
        });
        
        this.unclearedChqDepBtn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.account.AddUnclearedDepositWithdraw"), //"Advanced Search",
            scope: this,
            disabled :true,
            hidden:this.isGeneralLedger,
            tooltip: WtfGlobal.getLocaleText("acc.account.AddUnclearedDepositWithdraw"), //'Search for multiple terms in multiple fields.',
            handler: this.showOpeningBalanceTransactionWindow,
            iconCls: "bankReconciliationButton"
        });
    
        this.addAccount=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.coa.addNewAccount"), //'Add New Account',
            id:'maintainAccounts'+('4'),
            hidden:this.isGeneralLedger,
            iconCls:getButtonIconCls(Wtf.etype.add),
            tooltip:WtfGlobal.getLocaleText("acc.coa.addNewAccount"),  //"Add new Account")+" details. You may also add a sub-"+(this.isFixedAsset?"fixed asset":"account")+" to an existing "+(this.isFixedAsset?"fixed asset":"account")+".",
            handler:this.editCOA.createDelegate(this,[false])
        });
        
        this.editAccount=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.coa.editAccount"),  //'Edit Account',
            scope: this,
            hidden:this.isGeneralLedger,
            tooltip:{text:WtfGlobal.getLocaleText("acc.rem.97"),dtext:WtfGlobal.getLocaleText("acc.rem.97"), etext:WtfGlobal.getLocaleText("acc.rem.99")},
            iconCls:getButtonIconCls(Wtf.etype.edit),
            disabled:true,
            handler:this.editCOA.createDelegate(this,[true])
        });
        
    var deletebtnArray=[];
    if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType) && Wtf.account.companyAccountPref.deleteTransaction){
        deletebtnArray.push(this.deleteTrans=new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.rem.7")+' '+this.label,
            scope: this,
            tooltip:WtfGlobal.getLocaleText("acc.rem.6"),  //{text:"Select a "+this.label+" to delete.",dtext:"Select a "+this.label+" to delete.", etext:"Delete selected "+this.label+" details."},
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            handler:this.confirmBeforeDeleteAccount.createDelegate(this,this.del=["del"])
       }))
   }
   
   if(!WtfGlobal.EnableDisable(this.uPermType, this.removePermType) && Wtf.account.companyAccountPref.deleteTransaction){
        deletebtnArray.push(this.deleteTransPerm=new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.rem.7")+' '+this.label+' '+WtfGlobal.getLocaleText("acc.field.Permanently"),
            scope: this,
            tooltip:WtfGlobal.getLocaleText("acc.rem.6")+' '+WtfGlobal.getLocaleText("acc.field.Permanently"),  //{text:"Select a "+this.label+" to delete.",dtext:"Select a "+this.label+" to delete.", etext:"Delete selected "+this.label+" details."},
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            handler:this.confirmBeforeDeleteAccount.createDelegate(this,this.del=["delp"])
        }))
    }
    
   this.deleteAccount=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"), 
            scope: this,
            hidden:this.isGeneralLedger,
            disabled :true,
            tooltip:WtfGlobal.getLocaleText("acc.field.allowsyoutodeletetherecord"), 
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            menu:deletebtnArray
        });
        
        var activateDeactivateAccountBtnArray=[];
        activateDeactivateAccountBtnArray.push(this.activateAccount=new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.acc.ActivateAccount"),
            scope: this,
            tooltip:WtfGlobal.getLocaleText("acc.acc.ActivateAccount"),  //Activate Account'
            iconCls:getButtonIconCls(Wtf.etype.activate),
            handler:this.activateDeactivateAccount.createDelegate(this,this.activateDeactivate=["activate"])
        }))
        
        activateDeactivateAccountBtnArray.push(this.deactivateAccount=new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.acc.DeactivateAccount"),
            scope: this,
            tooltip:WtfGlobal.getLocaleText("acc.acc.DeactivateAccount"),
            iconCls:getButtonIconCls(Wtf.etype.deactivate),
            handler:this.activateDeactivateAccount.createDelegate(this,this.activateDeactivate=["deactivate"])
        }))
        
        this.activateDeactivateAccountBtn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.acc.ActivateDeactivate"),  //'Activate/Deactivate Account',
            scope: this,
            hidden:this.isGeneralLedger,
            tooltip:WtfGlobal.getLocaleText("acc.acc.ActivateDeactivate.tt"),
            disabled:true,
            iconCls:getButtonIconCls(Wtf.etype.activatedeactivate),
            menu:activateDeactivateAccountBtnArray
        });
    
        this.monthlyBudgetBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.SetMonthlyBudget"), //'Reset',            
            tooltip : WtfGlobal.getLocaleText("acc.field.SetMonthlyBudget"),//WtfGlobal.getLocaleText("acc.coa.resetTT"), //'Allows you to add a new search account name by clearing existing search account names.',
            id: 'btnMonthBud' + this.id,
            scope: this,
            hidden:this.isGeneralLedger,
            iconCls :'accountingbase pricelistbutton',
            handler:this.handleMonthlyBudget,
            disabled :false
        });
        
        this.monthlyForecastBttn=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.SetMonthlyForecast"), // "Set Monthly Forecast",
            tooltip : WtfGlobal.getLocaleText("acc.field.SetMonthlyForecast"), // "Set Monthly Forecast",
            id: 'btnMonthFor' + this.id,
            scope: this,
            hidden:this.isGeneralLedger,
            iconCls :'accountingbase pricelistbutton',
            handler:this.handleMonthlyForecast,
            disabled :false
        });
        if(!this.isGeneralLedger){// Below code does nor required for only in Accoount Report So here is check of !this.isGeneralLedger
            this.MSGroupComboconfig = {
                hiddenName:'groupmulselectcombo',         
                store: this.groupStore,
                valueField:'groupid',
                hideLabel:false,
                hidden : false,
                displayField:'groupname',
                emptyText:WtfGlobal.getLocaleText("acc.fxexposure.all"),
                mode: 'local',
                typeAhead: true,
                selectOnFocus:true,
                triggerAction:'all',
                scope:this
            };          
                
            this.MultiSelectGroupCombo = new Wtf.common.Select(Wtf.applyIf({
                id:'groupmulselectcombo'+this.id,
                multiSelect:true,
                fieldLabel:WtfGlobal.getLocaleText("acc.field.SelectAccounts") ,
                forceSelection:true,  
                //            extraFields:['companyname'],
                listWidth:400,
                width:125
            },this.MSGroupComboconfig));  
        }
        this.viewIBGBankDetails = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.viewIBGbankDetails"), // "View IBG Bank Details",
            tooltip : WtfGlobal.getLocaleText("acc.field.viewIBGbankDetails"), // "View IBG Bank Details",
            id: 'btnViewIBGDetails' + this.id,
            scope: this,
            hidden:this.isGeneralLedger || !Wtf.account.companyAccountPref.activateIBG,
            iconCls :'accountingbase pricelistbutton',
            handler:this.handleViewIBGBankDetails,
            disabled :true
        });
 
        this.btnArr.push(this.quickSearchTF);
        this.btnArr.push(this.resetBttn);
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.createcoa)){
            this.btnArr.push(this.addAccount);
        }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit)){
            this.btnArr.push(this.editAccount);
            this.btnArrEDSingleS.push(this.btnArr.length-1);
        }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.remove) && deletebtnArray.length>0){
            this.btnArr.push(this.deleteAccount);
            this.btnArrEDMultiS.push(this.btnArr.length-1);
        }
        this.btnArr.push(this.activateDeactivateAccountBtn);
        this.btnArrEDMultiS.push(this.btnArr.length-1);

        this.btnArr.push(this.AdvanceSearchBtn);
        this.btnArr.push(this.unclearedChqDepBtn);
           
        this.includeExcludeChildStore = new Wtf.data.SimpleStore({
            fields: [{
                    name: 'name'
                }, {
                    name: 'value',
                    type: 'boolean'
                }],
            data: [['Include Child Account Balances', true], ['Exclude Child Account Balances', false]]
        });

        this.includeExcludeChildCmb = new Wtf.form.ComboBox({
            labelSeparator: '',
            labelWidth: 0,
            triggerAction: 'all',
            mode: 'local',
            valueField: 'value',
            displayField: 'name',
            store: this.includeExcludeChildStore,
            value: false,
            width: 200,
            disabledClass: "newtripcmbss",
            name: 'includeExcludeChildBalances',
            hiddenName: 'includeExcludeChildBalances'
        });      
            if(this.isGeneralLedger){                
                this.accRec = Wtf.data.Record.create ([
                    {name:'accountname',mapping:'accname'},
                    {name:'accountid',mapping:'accid'},
                    {name:'currencyid',mapping:'currencyid'},
                    {name:'acccode'},
                    {name:'groupname'}
                ]);
                
                this.accStore = new Wtf.data.Store({
                    url : "ACCAccountCMN/getAccountsForCombo.do",
                    baseParams:{
                        mode:2,
                    ignorecustomers:true,  
                    ignorevendors:true,
                    nondeleted:true,
                    headerAdded:true,
                    controlAccounts:true,
                    sort : "acccccode",
                    dir : "asc",
                    isForBS_PL_to_GL:this.isForBS_PL_to_GL
                    },
                    reader: new Wtf.data.KwlJsonReader({
                        root: "data"
                    },this.accRec)
                });
                this.accStore.on('load',function(){
                    if(this.isGeneralLedger){
                        this.showGeneralLedger(this.accountID,this.startDate.getValue(),this.endDate.getValue());
                    }
                },this);
                this.accStore.load();
                
                this.MSComboconfig = {
                    hiddenName:'accountmulselectcombo',         
                    store: this.accStore,
                    valueField:'accountid',
                    hideLabel:false,
                    hidden : false,
                    displayField:'accountname',
                    emptyText:WtfGlobal.getLocaleText("acc.fxexposure.all"),
                    mode: 'local',
                    typeAhead: true,
                    selectOnFocus:true,
                    triggerAction:'all',
                    scope:this
                };
              
                this.balPLTypeStore = new Wtf.data.SimpleStore({
                    fields: [{name:'typeid',  type:'int'}, 'name'],
                    data :[
                                [0, "All"],
                                [1, "Balance Sheet"],
                                [2, "Profit & Loss"]                   
                    ]
                });
    
                this.balPLTypeCombo = new Wtf.form.ComboBox({     //All/Balance Sheet/Profit & Loss
                    store: this.balPLTypeStore,
                    name:'typeid',
                    displayField:'name',
                    id:'typeid',
                    valueField:'typeid',
                    mode: 'local',
                    value:0,
                    width:100,
                    listWidth:200,
                    triggerAction: 'all',
                    typeAhead:true,
                    selectOnFocus:true
                });      
               
                this.accountMasterTypeStore = new Wtf.data.SimpleStore({
                    fields: [{name:'mastertypeid',  type:'int'}, 'name'],
                    data :[
                                [0, "All"],
                                [1, "Exclude Bank Accounts"],
                                [2, "Exclude Cash Accounts"]                   
                    ]
                });
                
                this.accountMasterTypeCombo = new Wtf.form.ComboBox({     //All/Exclude Bank Accounts/Exclude Cash Accounts
                    store: this.accountMasterTypeStore,
                    name:'accountmastertype',
                    displayField:'name',
                    id:'mastertypeid',
                    valueField:'mastertypeid',
                    mode: 'local',
                    value:0,
                    width:100,
                    listWidth:200,
                    triggerAction: 'all',
                    typeAhead:true,
                    selectOnFocus:true
                });  
                
                this.accountTransactionTypeStore = new Wtf.data.SimpleStore({
                    fields: [{name:'transactionid',  type:'int'}, 'transactionname'],
                    data :[
                    [0, "All"],
                    [Wtf.Acc_Make_Payment_ModuleId, WtfGlobal.getLocaleText("acc.accPref.autoMP")+" "+WtfGlobal.getLocaleText("acc.repeated.repeatedrecords")],                   
                    [Wtf.Acc_Receive_Payment_ModuleId, WtfGlobal.getLocaleText("acc.accPref.autoRP")+" "+WtfGlobal.getLocaleText("acc.repeated.repeatedrecords")]
                    ]
                });

                this.accountTransactionMasterTypeCombo = new Wtf.form.ComboBox({     //All/Exclude Bank Accounts/Exclude Cash Accounts
                    store: this.accountTransactionTypeStore,
                    name:'accounttransactiontype',
                    displayField:'transactionname',
                    id:'transactionid',
                    valueField:'transactionid',
                    mode: 'local',
                    value:0,
                    width:155,
                    listWidth:200,
                    triggerAction: 'all',
                    typeAhead:true,
                    selectOnFocus:true
                });  
               
                this.MultiSelectAccCombo = new Wtf.common.Select(Wtf.applyIf({
                    id:'mulaccountcombo'+this.id,
                    multiSelect:true,
                    fieldLabel:WtfGlobal.getLocaleText("acc.field.SelectAccounts") ,
                    forceSelection:true,  
                    extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
                    extraComparisionField:'acccode',// type ahead search on acccode as well.
                    listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
                    width:125
                },this.MSComboconfig));
                                               
                this.typeStore = new Wtf.data.SimpleStore({
                    fields:[{
                        name:'name'
                    },{
                        name:'value',
                        type:'boolean'
                    }],
                    data:[['All',false],['Exclude Previous Year Balance',true]]
                });
        
                this.excludeTypeCmb= new Wtf.form.ComboBox({
                    labelSeparator:'',
                    labelWidth:0,
                    triggerAction:'all',
                    mode: 'local',
                    valueField:'value',
                    displayField:'name',
                    store:this.typeStore,
                    value:true,
                    width:200,
                    disabledClass:"newtripcmbss",
                    name:'excludePreviousYear',
                    hiddenName:'excludePreviousYear'
                });  
               
                
                this.balPLTypeCombo.on("select",function(){                    
                    if(this.balPLTypeCombo.getValue()==2 ||this.balPLTypeCombo.getValue()==0){
                        this.excludeTypeCmb.enable();
                        this.excludeTypeCmb.setValue(true);
                    }else{
                        this.excludeTypeCmb.disable();
                        this.excludeTypeCmb.setValue(false)
                    }
                },this);
              
                this.btnArr.push(WtfGlobal.getLocaleText("acc.1099.selAcc"));
                this.btnArr.push(" ");
                this.btnArr.push(this.MultiSelectAccCombo);
                this.startDate=new Wtf.ExDateFieldQtip({
                    fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
                    name:'stdate',
                    format:WtfGlobal.getOnlyDateFormat(),
                    //readOnly:true,
                    value:this.getDates(true)
                });
                this.endDate=new Wtf.ExDateFieldQtip({
                    fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
                    format:WtfGlobal.getOnlyDateFormat(),
                    name:'enddate',
                   // readOnly:true,
                    value:this.getDates(false)
                });
                if(this.stDate!="" && this.stDate!=undefined){
                    this.startDate.setValue(this.stDate);
                }
                if(this.enDate!="" && this.enDate!=undefined){
                    this.endDate.setValue(this.enDate);
                }
                this.btnArr.push("-");
                this.btnArr.push(WtfGlobal.getLocaleText("acc.common.from"));
                this.btnArr.push(this.startDate);
                this.btnArr.push(" ");
                this.btnArr.push(WtfGlobal.getLocaleText("acc.common.to"));
                this.btnArr.push(this.endDate);
                this.btnArr.push("-");
                
                this.btnArr.push(WtfGlobal.getLocaleText("acc.customerList.gridCurrency"));
                this.btnArr.push(" ");
                this.btnArr.push(this.MultiSelectCurrencyCombo);                
                //this.tbar2.push("-");                
                this.tbar2.push(WtfGlobal.getLocaleText("acc.product.gridType"))
                
                this.tbar2.push(this.balPLTypeCombo);
                this.tbar2.push(this.excludeTypeCmb);
                this.tbar2.push(this.includeExcludeChildCmb);
                this.tbar2.push(WtfGlobal.getLocaleText("acc.field.AccountType"))
                this.tbar2.push(this.accountMasterTypeCombo);
                this.tbar2.push(WtfGlobal.getLocaleText("acc.field.TransactionType"))
                this.tbar2.push(this.accountTransactionMasterTypeCombo);
                this.tbar2.push(" ");

                this.tbar2.push({
                    xtype:'button',
                    text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
                    tooltip:WtfGlobal.getLocaleText("acc.invReport.fetchTT"),
                    iconCls:'accountingbase fetch',
                    scope:this,
                    handler:this.fetchBtnHandler
                }) 
                this.tbar2.push({
                    xtype:'button',
                    text:WtfGlobal.getLocaleText("acc.field.subLedger"),  //'Sub Ledger',
                    tooltip:WtfGlobal.getLocaleText("acc.field.subLedger"),
                    scope:this,
                    iconCls:'pwnd exportpdf',
//                    hidden:true,
                    handler:this.subLedgerReport
                }) 
                if(!WtfGlobal.EnableDisable(this.uPermType, this.isGeneralLedger?this.permType.exportdataledger:this.permType.exportdata)){          
                    this.exportCustVenRec=new Wtf.exportButton({
                        obj:this,
                        id:"exportCustVenRec",
                        iconCls: 'pwnd exportpdfsingle',
                        text: 'Export Customer/Vendor wise Record(s)',
                        tooltip :'Export Customer/Vendor wise Record(s)',
                        //hidden:this.isRequisition || this.isRFQ || this.isSalesCommissionStmt,
                        hidden:true,
                        menuItem:{csv:false,xls:true,pdf:false,rowPdf:false,subMenu:true},
                        params:this.isGeneralLedger?{
                            isExportingCustomerVendorRecord:true,
                            group:[12],
                            ignore:true,
                            isGeneralLedger:this.isGeneralLedger,
                            generalLedgerFlag:true,
                            exportThreadFlagLedger : Wtf.account.companyAccountPref.downloadglprocessflag,
                            startDate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                            endDate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                            balPLId:this.balPLTypeCombo.getValue(),
                            accountIds:this.MultiSelectAccCombo.getValue(),
                            isExportingSelectedRecord:true,
                            excludePreviousYear: this.excludeTypeCmb.getValue(),
                            includeExcludeChildBalances: this.includeExcludeChildCmb.getValue(),
                            controlAccounts:true
                            }:({group:[12],ignore:true}),
                         get:112
                        
                    });
                this.tbar2.push(this.exportCustVenRec)
                
                this.exportCustVenRec.on('click',function(){
                    this.exportCustVenRec.params.isExportingCustomerVendorRecord= true;
                    this.exportCustVenRec.params.isExportingSelectedRecord=true;
                    this.exportCustVenRec.params.startDate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
                    this.exportCustVenRec.params.stdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
                    //In GL Report end date incremented by one day. so same as in export to handle time zone issue.  
                    this.exportCustVenRec.params.endDate=this.isGeneralLedger?WtfGlobal.convertToGenericEndDate(this.endDate.getValue()):WtfGlobal.convertToGenericDate(this.endDate.getValue());
                    this.exportCustVenRec.params.enddate=this.isGeneralLedger?WtfGlobal.convertToGenericEndDate(this.endDate.getValue()):WtfGlobal.convertToGenericDate(this.endDate.getValue());
                    this.exportCustVenRec.params.balPLId=this.balPLTypeCombo.getValue();
                    this.exportCustVenRec.params.accountIds=this.MultiSelectAccCombo.getValue();
                    this.exportCustVenRec.params.excludePreviousYear= this.excludeTypeCmb.getValue();
                    this.exportCustVenRec.params.includeExcludeChildBalances = this.includeExcludeChildCmb.getValue();
                    this.exportCustVenRec.params.searchJson = this.searchJson,
                    this.exportCustVenRec.params.filterConjuctionCriteria = this.filterConjuctionCrit
                    if(this.accountTransactionMasterTypeCombo!=undefined){
                        this.exportCustVenRec.params.accountTransactionType = this.accountTransactionMasterTypeCombo.getValue();
                    }
                    if(this.accountMasterTypeCombo!=undefined && this.accountMasterTypeCombo.getValue()==1){
                        this.exportCustVenRec.params.ignoreBankAccounts=true;
                        if(this.exportCustVenRec.params.ignoreCashAccounts)
                            delete this.exportCustVenRec.params.ignoreCashAccounts;
                    }else if(this.accountMasterTypeCombo!=undefined && this.accountMasterTypeCombo.getValue()==2){
                        this.exportCustVenRec.params.ignoreCashAccounts=true;
                        if(this.exportCustVenRec.params.ignoreBankAccounts)
                            delete this.exportCustVenRec.params.ignoreBankAccounts;
                    }else if(this.exportCustVenRec.params.ignoreBankAccounts || this.exportCustVenRec.params.ignoreCashAccounts){
                        delete this.exportCustVenRec.params.ignoreCashAccounts;
                        delete this.exportCustVenRec.params.ignoreBankAccounts;
                    }    
                },this);
            }
            }
                   
//            if(!this.isGeneralLedger){
            if(!WtfGlobal.EnableDisable(this.uPermType,  this.isGeneralLedger?this.permType.exportdataledger:this.permType.exportdata)){
                this.bottombtnArr.push(this.exportData=new Wtf.exportButton({
                    obj:this,
                    //                        hidden:this.isGeneralLedger,
                    text:WtfGlobal.getLocaleText("acc.common.export"),
                    tooltip:WtfGlobal.getLocaleText("acc.common.exportTT"),  //"Export "+(this.isFixedAsset?"Fixed Asset":"Accounts")+" details",
                    menuItem:{csv:true,pdf:this.isGeneralLedger?false:true,rowPdf:false,xls:true,subMenu:this.isGeneralLedger?true:false},
                    params:this.isGeneralLedger?{
                        group:[12],
                        ignore:true,
                        isGeneralLedger:this.isGeneralLedger,
                        generalLedgerFlag:true,
                        exportThreadFlagLedger : Wtf.account.companyAccountPref.downloadglprocessflag,
                        startDate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                        stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                        endDate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                        enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                        balPLId:this.balPLTypeCombo.getValue(),
                        accountIds:this.MultiSelectAccCombo.getValue(),
                        isExportingSelectedRecord:true,
                        excludePreviousYear: this.excludeTypeCmb.getValue(),
                        includeExcludeChildBalances: this.includeExcludeChildCmb.getValue(),
                        controlAccounts:true
                   }:({group:[12],ignore:true}),
                    //      params:{isGeneralLedger:this.isGeneralLedger},
                    get:112,
                    label:WtfGlobal.getLocaleText("acc.field.ChartofAccount")
                }));

                this.exportData.on('click',function(){
                    this.exportData.params.startDate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
                    this.exportData.params.stdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
                    //In GL Report end date incremented by one day. so same as in export to handle time zone issue.  
                    this.exportData.params.endDate=this.isGeneralLedger?WtfGlobal.convertToGenericEndDate(this.endDate.getValue()):WtfGlobal.convertToGenericDate(this.endDate.getValue());
                    this.exportData.params.enddate=this.isGeneralLedger?WtfGlobal.convertToGenericEndDate(this.endDate.getValue()):WtfGlobal.convertToGenericDate(this.endDate.getValue());
                    this.exportData.params.balPLId=this.balPLTypeCombo.getValue();
                    this.exportData.params.accountIds=this.MultiSelectAccCombo.getValue();
                    this.exportData.params.excludePreviousYear= this.excludeTypeCmb.getValue();
                    this.exportData.params.includeExcludeChildBalances = this.includeExcludeChildCmb.getValue();
                    this.exportData.params.searchJson = this.searchJson;
                    this.exportData.params.filterConjuctionCriteria = this.filterConjuctionCrit;
                    if(this.accountTransactionMasterTypeCombo!=undefined){
                        this.exportData.params.accountTransactionType = this.accountTransactionMasterTypeCombo.getValue();
                    }
                    if(this.accountMasterTypeCombo!=undefined && this.accountMasterTypeCombo.getValue()==1){
                        this.exportData.params.ignoreBankAccounts=true;
                        if(this.exportData.params.ignoreCashAccounts)
                            delete this.exportData.params.ignoreCashAccounts;
                    }else if(this.accountMasterTypeCombo!=undefined && this.accountMasterTypeCombo.getValue()==2){
                        this.exportData.params.ignoreCashAccounts=true;
                        if(this.exportData.params.ignoreBankAccounts)
                            delete this.exportData.params.ignoreBankAccounts;
                    }else if(this.exportData.params.ignoreBankAccounts || this.exportData.params.ignoreCashAccounts){
                        delete this.exportData.params.ignoreCashAccounts;
                        delete this.exportData.params.ignoreBankAccounts;
                    }
                },this);
                
            }    
                
                    var extraConfig = {};
                    extraConfig.url= "ACCAccount/importAccounts.do";
                    var extraParams = "{\"bookBeginningDate\":\""+WtfGlobal.convertToDateOnly(Wtf.account.companyAccountPref.bbfrom)+"\"}";;
                    extraConfig.isBookClosed = Wtf.isBookClosed;
                    this.importBtnArray= Wtf.importMenuArray(this, "Accounts", this.store, extraParams, extraConfig);
                    this.importButton= Wtf.importMenuButtonA(this.importBtnArray, this, "Accounts");
                    if(!this.isGeneralLedger){
                    if(!WtfGlobal.EnableDisable(this.uPermType,this.permType.importdata)){
                        this.bottombtnArr.push(this.importButton);
                    }
                    }

            if(!WtfGlobal.EnableDisable(this.uPermType, this.isGeneralLedger?this.permType.printledger:this.permType.print)){
                this.bottombtnArr.push(this.printData=new Wtf.exportButton({
                    obj:this,
                    //                        hidden:this.isGeneralLedger,
                    text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
                    tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Chart of Account Report details",
                    menuItem:{print:true},
                    params:(this.isGeneralLedger?{
                        group:[12],
                        ignore:true,
                        isGeneralLedger:this.isGeneralLedger,
                        generalLedgerFlag:true,
                        exportThreadFlagLedger : Wtf.account.companyAccountPref.downloadglprocessflag,
                        startDate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                        stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                        endDate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                        enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                        balPLId:this.balPLTypeCombo.getValue(),
                        accountIds:this.MultiSelectAccCombo.getValue(),
                        isExportingSelectedRecord:true,
                        excludePreviousYear: this.excludeTypeCmb.getValue(),
                        includeExcludeChildBalances: this.includeExcludeChildCmb.getValue(),
                        isLedgerPrintCSV: true
                    }:{group:[12], ignore:true, name:WtfGlobal.getLocaleText("acc.coa.tabTitle")}),
                    get:this.isGeneralLedger ? Wtf.autoNum.GeneralLedger : 112,
                    label:WtfGlobal.getLocaleText("acc.field.ChartofAccount")
                }));
                    }
//            }
             
            this.tbar2.push(this.monthlyBudgetBttn);
            this.tbar2.push(this.monthlyForecastBttn);
            this.tbar2.push(this.viewIBGBankDetails);
            if(!this.isGeneralLedger){
                this.tbar2.push(WtfGlobal.getLocaleText("acc.account.group.selectgroup"),'-',this.MultiSelectGroupCombo); 
                this.tbar2.push(this.includeExcludeChildCmb);
                this.tbar2.push({
                    xtype:'button',
                    text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
                    tooltip:WtfGlobal.getLocaleText("acc.invReport.fetchTT"),
                    iconCls:'accountingbase fetch',
                    scope:this,
                    handler:this.fetchAccounts
                }) 
            }
            this.tbar2.push("->");
            this.tbar2.push(WtfGlobal.getLocaleText("acc.field.GeneralLedgerType"));
            this.tbar2.push(this.typeEditor);
            
            if(!this.isGeneralLedger){
                var tooltip=WtfGlobal.getLocaleText("acc.coa.tip1");  //"Please click here to view Account Receivable/Customer account(s)";
                this.btnArr.push("<a class='tbar-link-text' href='#' onClick='javascript: openAccRecTab("+true+")'wtf:qtip='"+tooltip+"'>"+WtfGlobal.getLocaleText("acc.coa.accountReceivableLink")+"</a>");
                tooltip=WtfGlobal.getLocaleText("acc.coa.tip2");  //"Please click here to view Account Payable/Vendor account(s)";
                this.btnArr.push("<a class='tbar-link-text' href='#' onClick='javascript: openAccRecTab("+false+")'wtf:qtip='"+tooltip+"'>"+WtfGlobal.getLocaleText("acc.coa.accountPayableLink")+"</a>");
//                if(!WtfGlobal.EnableDisable(Wtf.UPerm.fixedasset, Wtf.Perm.fixedasset.view)){                    //ERP-12389
//                        tooltip=WtfGlobal.getLocaleText("acc.coa.tip3");  //here to view Fixed Asset(s)";
//                        this.btnArr.push("<a class='tbar-link-text' href='#' onClick='javascript: openFixedAssetTab()'wtf:qtip='"+tooltip+"'>"+WtfGlobal.getLocaleText("acc.coa.FixedAssetLink")+"</a>");
//                }
            }
            this.btnArr.push("  ");
            this.btnArr.push("->");
            if(!this.isGeneralLedger){
                this.btnArr.push(getHelpButton(this,4));
            }
            this.bbar = this.pToolBar;
        this.typeEditor.on('select',this.loadTypeStore,this);
        this.selectionModel.on("selectionchange",this.enableDisableButtons.createDelegate(this,[this.btnArr,this.exp="exp"]),this);
        this.selectionModel.on("selectionchange",this.enableDisableButtons.createDelegate(this,[this.bottombtnArr,this.exp="export"]),this);
    },

    setPageSize: function(store, rec, opt){
         WtfGlobal.resetAjaxTimeOut();
        var count = 0;
        for (var i = 0; i < store.getCount(); i++) {
            if (rec[i].data['level'] == 0 && (rec[i].data['parentid'] == "" || rec[i].data['parentid'] == undefined))
                count++;
        }
        this.pageLimit.totalSize = store.reader.jsonData['totalCount'];
        this.BalanceSummary = store.reader.jsonData['openbalanceSummary'];
        this.endingBalanceSummary = store.reader.jsonData['endingBalanceSummary'];
        this.grid.getView().refresh();
    },
//    importCSVRecords : function(type, extraParams, extraConfig){
//        this.impWin1 = Wtf.commonFileImportWindow(this, type, extraParams, extraConfig);
//        this.impWin1.show();
//    },
//    importXLSRecords :function(obj,moduleName,store,extraParams, extraConfig){
//        this.impWin1 = Wtf.xlsCommonFileImportWindow(obj,moduleName,store,extraParams, extraConfig);
//        this.impWin1.show();
//    },
//    mappingCSVInterface:function(Header, res, impWin1, delimiterType, extraParams, extraConfig) {
//        this.filename=res.FileName;
//
//        this.mapCSV=new Wtf.csvFileMappingInterface({
//            csvheaders:Header,
//            modName:"Accounts",
//            impWin1:impWin1,
//            delimiterType:delimiterType,
//            cm:this.gridcm,
//            extraParams: extraParams,
//            extraConfig: extraConfig
//        }).show();
//        Wtf.getCmp("csvMappingInterface").on('importfn',this.importCSVfunction, this);
//    },
//    importCSVfunction:function(response, delimiterType, extraParams, extraConfig) {
//        Wtf.importMappedRecords(this, response, "Accounts", this.filename, this.store, delimiterType, extraParams, extraConfig);
//    },
    loadTypeStore:function(a,rec){
        this.deleted=false;
        this.nondeleted=false;
        var index=rec.data.typeid;
//        this.deleteAccount.enable();
        if(index==1){
            this.deleted=true;
//            this.deleteAccount.disable();
        }
        else if(index==2)
            this.nondeleted=true;
        this.store.load({
            params: {
                start: 0,
                limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt,
                ss : this.quickSearchTF.getValue(),
                excludePreviousYear: (this.excludeTypeCmb)?this.excludeTypeCmb.getValue():false,
                includeExcludeChildBalances: (this.includeExcludeChildCmb)?this.includeExcludeChildCmb.getValue():false
            }
        });
        WtfComMsgBox(29,4,true);
        this.store.on('load',this.storeloaded,this);
    },
     handleResetClick:function(){
        if(this.quickSearchTF.getValue()){
            this.quickSearchTF.reset();
            this.store.load({
            params: {
                start: 0,
                limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt,
                excludePreviousYear: this.excludeTypeCmb ? this.excludeTypeCmb.getValue():"",
                includeExcludeChildBalances : (this.includeExcludeChildCmb!=undefined) ? this.includeExcludeChildCmb.getValue() : ""
            }
        });
        }
    },
    storeloaded:function(store){
        Wtf.MessageBox.hide();
        this.quickSearchTF.StorageChanged(store);
    },
    enableDisableButtons:function(btnArr,exp){
        Wtf.uncheckSelAllCheckbox(this.selectionModel);
        
        var rec=this.grid.getSelectionModel().getSelected();
        var expo=exp;
        if(expo=='exp'){
            if(rec&&((rec.data.groupid=='18'&&rec.data.accname=='Cash in hand')))
                WtfGlobal.enableDisableBtnArr(btnArr, this.grid, [3], []);
            else if(rec&&rec.data.groupid=='12'&&this.grid.getSelectionModel().isSelected(rec))
                WtfGlobal.enableDisableBtnArr(btnArr, this.grid, [3,5,6], [4]);						// neeraj
            else if(rec&&((rec.data.groupid=='10' && rec.data.isOnlyAccount=="false") || (rec.data.groupid=='13' && rec.data.isOnlyAccount=="false")||(rec.data.accname==Wtf.SundryCustomer||rec.data.accname==Wtf.SundryVendor)))  //here rec.data.isOnlyAccount=="false" means it can be customer or vendor
                WtfGlobal.enableDisableBtnArr(btnArr, this.grid, [3], [4]);
            else{
                if(!this.isGeneralLedger)
                    WtfGlobal.enableDisableBtnArr(btnArr, this.grid, this.btnArrEDSingleS, this.btnArrEDMultiS);
            }
            var arr=this.grid.getSelectionModel().getSelections();
            for(var i=0;i<arr.length;arr++){
                if(arr[i]&&arr[i].data.deleted) {
//                    this.deleteAccount.disable();
                    this.editAccount.disable();
                    this.viewIBGBankDetails.disable();
                }
            }
            
            var selectionArray = this.grid.getSelectionModel().getSelections();
            var activeAccs = false;
            var dormantAccs = false;
            var isDeleteAccTemp = false;
            if(selectionArray.length == 1) {
                if(rec && (rec.data.isibgbank) && !rec.data.deleted) {
                    this.viewIBGBankDetails.enable();
                } else {
                    this.viewIBGBankDetails.disable();
                }
               
            } else {
                this.viewIBGBankDetails.disable();
            }
            for(var i=0; i<selectionArray.length; i++){
                if(selectionArray[i].data.isactivate) {
                   activeAccs = true;
                } else {
                   dormantAccs = true;
                }
            }
            if(activeAccs && dormantAccs){
                /**
                 * ERP-32336 : [COA] After deleting GL account, Activate/Deactivate and Delete buttons are enabled.
                 * if selecting accounts are in Active and Dormant then
                 * Activate/Deacivate Button should be Disable 
                 */
                this.activateDeactivateAccountBtn.disable();
                this.deleteTrans.enable();
            } else if(activeAccs) {
                this.activateAccount.disable();
                this.deactivateAccount.enable();
                if (!WtfGlobal.EnableDisable(this.uPermType, this.removePermType) && Wtf.account.companyAccountPref.deleteTransaction) {
                    this.deleteTrans.enable();
                }
            } else if(dormantAccs) {
                this.activateAccount.enable();
                this.deactivateAccount.disable();
                if (!WtfGlobal.EnableDisable(this.uPermType, this.removePermType) && Wtf.account.companyAccountPref.deleteTransaction) {
                    this.deleteTrans.enable();
                }
            }
                if(rec && rec.data.controlAccounts){
//                    this.editAccount.disable();
                    this.deleteAccount.disable();
                }
//                else{
//                    this.editAccount.enable();
//                    this.deleteAccount.disable();
//                }

            for (var i = 0; i < selectionArray.length; i++) {
                if (selectionArray[i].data.deleted) {
                    isDeleteAccTemp = true;
                    break;
                }
            }
            if(isDeleteAccTemp) {
                /**
                 * ERP-32336 : [COA] After deleting GL account, Activate/Deactivate and Delete buttons are enabled.
                 * if one of the selected account is deleted then
                 * Activate/Deacivate Button and should be Disable 
                 */
                this.activateDeactivateAccountBtn.disable();
                if (!WtfGlobal.EnableDisable(this.uPermType, this.removePermType) && Wtf.account.companyAccountPref.deleteTransaction) {
                    this.deleteTrans.disable();
                }
            }
        }
        
        var selectionArr = this.grid.getSelectionModel().getSelections();
        if(selectionArr.length ==1){
            if(rec && rec.data.mastertypevalue=='3'){
                if(this.unclearedChqDepBtn)this.unclearedChqDepBtn.enable();
            }
        }else{
            if(this.unclearedChqDepBtn)this.unclearedChqDepBtn.disable();
        }
        
        if(selectionArr.length >=1){
            if(this.exportselRec)this.exportselRec.enable();
        }else{
            if(this.exportselRec)this.exportselRec.disable();
            
        }
    },

    editCOA:function(isEdit){
        this.recArr =[] ;
        this.isEdit=isEdit;
        if(isEdit){
            this.recArr = this.grid.getSelectionModel().getSelections();
            this.grid.getSelectionModel().clearSelections();
            WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,1);
        }
        var  rec=isEdit?this.recArr[0]:null;

        callCOAWindow(isEdit,rec,"coaWindow",false,false,false,false,false,false,false,true);
        Wtf.getCmp("coaWindow").on('update',this.updateGrid,this);
        Wtf.getCmp("coaWindow").on('cancel',function(){
            var num= (this.store.indexOf(this.recArr[0]))%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr,false,num,true);
        },this);        
    },
    
    updateGrid: function(obj,accID){
        this.accID=accID;
        this.store.reload();
        this.isAdd=true;
         if(accID!=undefined)
            this.store.on('load',this.colorRow,this)
    },
    
    colorRow: function(store){
        if(this.isAdd && (!this.isEdit)){
            this.recArr=[];
            if(store.find('accid',this.accID) != -1) {
                 this.recArr.push(store.getAt(store.find('accid',this.accID)));
                 WtfGlobal.highLightRowColor(this.grid,this.recArr[0],true,0,0);
            }
            this.isAdd=false;
        }
    },
    
    handleMonthlyBudget:function(){
        var arr = this.grid.getSelectionModel().getSelections();
        var accountId = "";
        if (arr.length > 0) {
            for (var i = 0; i < arr.length; i++) {
                accountId += arr[i].data.accid + ",";
            }
            AccountMonthlyBudget('AccountMonthlyBudgetWin', accountId,true);
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleaseselecttherecords")], 2);
        }        
    },
    
    handleMonthlyForecast:function(){
        var arr = this.grid.getSelectionModel().getSelections();
        var accountId = "";
        if (arr.length > 0) {
            for (var i = 0; i < arr.length; i++) {
                accountId += arr[i].data.accid + ",";
            }
            AccountMonthlyBudget('AccountMonthlyBudgetWin', accountId,false);
        }else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleaseselecttherecords")], 2); // "Please select the record(s)."
        }
    },
    
    handleViewIBGBankDetails : function() {
        var rec = this.selectionModel.getSelected();
        callIBGbankDetailsWin("ibgBankDetailsWin",false,true,rec);
        
        Wtf.getCmp('ibgBankDetailsWin').on('update',function(config) {
            this.store.reload();
        },this);
    },
    
    getDates:function(start){
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },
    
     showGeneralLedger:function(accid,startDate,endDate){
     var i=this.accStore.find("accountid",accid);
        if(i>=0){
            this.MultiSelectAccCombo.setValue(accid);
            if(startDate!="" && startDate!=undefined){
                this.startDate.setValue(startDate);
            }
            if(endDate!="" && endDate!=undefined){
                this.endDate.setValue(endDate);
            }
            if(accid!='None'){   
                this.accountID=accid;
                this.fetchBtnHandler();
            }
        }
    },   
    
    fetchBtnHandler:function(){
       this.sDate=this.startDate.getValue();
       this.eDate=this.endDate.getValue();

       if(this.sDate=="" || this.eDate=="") {
           WtfComMsgBox(42,2);
           return;
       }

       this.sdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
       this.edate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        if(this.sDate>this.eDate){
            WtfComMsgBox(1,2);
        return;
        }
        var accountIds =  this.MultiSelectAccCombo.getValue();
        var balPLId = this.balPLTypeCombo.getValue();
        var loadingMask = new Wtf.LoadMask(document.body,{
                msg : WtfGlobal.getLocaleText("acc.msgbox.50")
         });
         
       this.store.on('beforeload', function(){
            WtfGlobal.setAjaxTimeOut();
        }, this);
        this.store.on('loadexception', function(){
            loadingMask.hide();
            WtfGlobal.resetAjaxTimeOut();
        }, this);
        this.store.on('load', function(){
             if(this.exponly){
                
                for(var i=0; i< this.grid.getStore().data.length; i++){
                    this.expander.expandRow(i)
                    this.fillExpanderBody();
                }
                this.exponly=false;
            }
             
            loadingMask.hide();
            WtfGlobal.resetAjaxTimeOut();
        }, this);
    
        this.store.load({
            params:{
                  startDate:this.sdate,
                  endDate:this.edate,
                  accountIds:accountIds,           
                  balPLId:balPLId,
                  isGeneralLedger:this.isGeneralLedger,                  
                  isOpeningBal:"",
//                  isForBS_PL_to_GL:this.isForBS_PL_to_GL,
                  start:0,
                  excludePreviousYear: this.excludeTypeCmb.getValue(),
                  includeExcludeChildBalances : this.includeExcludeChildCmb.getValue()
            }
         });
         this.createExpanderStore(this.MultiSelectCurrencyCombo.getValue());
    },
    
    fetchAccounts:function(){
        this.store.load({
            params:{
                start:0,
                accgroupids:this.MultiSelectGroupCombo.getValue(),
                ss: this.quickSearchTF.getValue()
            }
        });
    },
    
    confirmBeforeDeleteAccount: function (delp) {
        if (Wtf.account.companyAccountPref.propagateToChildCompanies) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.confirm"),
                msg: WtfGlobal.getLocaleText("acc.coamaster.propagatedaccounts.delete.confirm"),
                buttons: Wtf.MessageBox.YESNO,
                icon: Wtf.MessageBox.QUESTION,
                width: 300,
                scope: {
                    scopeObject: this
                },
                fn: function (btn) {
                    if (btn == "yes") {
                        this.scopeObject.ispropagatetochildcompanyflag = true;
                    }
                    this.scopeObject.deleteCOA(delp);
                }
            }, this);
        } else {
            this.deleteCOA(delp);
        }
    },
   deleteCOA:function(delp){
        var delFlag=delp;
        var arr=[];
        var data=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
        WtfGlobal.highLightRowColor(this.grid,this.recArr,true,0,2);
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),  //"Confirm",
            msg: WtfGlobal.getLocaleText("acc.rem.10"),  //"Are you sure you want to delete the selected account(s) and all the associated sub account(s)?<div><b>Note: This data cannot be retrieved later.</b></div>",
            width: 560,
            buttons: Wtf.MessageBox.OKCANCEL,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope:this,
            fn:function(btn){
                if(btn!="ok"){
                    for(var i=0;i<this.recArr.length;i++){
                        var ind=this.store.indexOf(this.recArr[i])
                        var num= ind%2;
                        WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
                    }
                    return;
                }
                for(i=0;i<this.recArr.length;i++){
                    arr.push(this.store.indexOf(this.recArr[i]));
                }
                data= WtfGlobal.getJSONArray(this.grid,true,arr);
                var coaccdelflag=false;
                if(delFlag=='delp')
                  coaccdelflag=true; //Send this flag as true whenever you want to delete records permanently from database.       
                Wtf.Ajax.requestEx({
                    url: "ACCAccountCMN/deleteAccount.do",
    //                url: Wtf.req.account+'CompanyManager.jsp',
                    params:{
                        data:data,
                        coaaccdel : coaccdelflag, //Send this flag as true whenever you want to delete records permanently from database.
                        mode:6,
                        ispropagatetochildcompanyflag:this.ispropagatetochildcompanyflag
                    }
                },this,this.genSuccessResponse,this.genFailureResponse);
                
                this.ispropagatetochildcompanyflag=false;
                
            }
        });
    },

    genSuccessResponse:function(response){
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.store.indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        /*
         * ERP-41341 - [COA] Change Alert Symbol instead of cross symbol. 
         */
        if(response.success){
            WtfComMsgBox([(WtfGlobal.getLocaleText("acc.coa.tabTitle")), response.msg],response.success*2+1);
            (function(){
            	Wtf.salesAccStore.reload();	
            this.store.reload();
            }).defer(WtfGlobal.gridReloadDelay(),this);
        } else {
             WtfComMsgBox([(WtfGlobal.getLocaleText("acc.coa.tabTitle")), response.msg], 2); 
        }
    },

    genFailureResponse:function(response){
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.store.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    activateDeactivateAccount:function(activateDeactivate){
        var activateDeactivateFlag=activateDeactivate;
        var arr=[];
        var data=[];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
        var coaActivateDeactivateFlag=false;
        if(activateDeactivateFlag=='deactivate')
            coaActivateDeactivateFlag=true; //Send this flag as true whenever you want to activate or deactivate Accounts.  
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),  //"Confirm",
            msg: coaActivateDeactivateFlag ? WtfGlobal.getLocaleText("acc.rem.301") : WtfGlobal.getLocaleText("acc.rem.300"),  
            width: 560,
            buttons: Wtf.MessageBox.OKCANCEL,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope:this,
            fn:function(btn){
                if(btn!="ok"){
                    for(var i=0;i<this.recArr.length;i++){
                        var ind=this.store.indexOf(this.recArr[i])
                        var num= ind%2;
                        WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
                    }
                    return;
                }
                for(i=0;i<this.recArr.length;i++){
                    arr.push(this.store.indexOf(this.recArr[i]));
                }
                    data= WtfGlobal.getJSONArray(this.grid,true,arr);
                    
                    Wtf.Ajax.requestEx({
                        url: "ACCAccountCMN/activateDeactivateAccounts.do",
                        params:{
                            data:data,
                            coaActivateDeactivateFlag : coaActivateDeactivateFlag, //Send this flag as true whenever you want to Activate or Deactivate Accounts.
                            mode:6
                        }
                    },this,this.genActivateSuccessResponse,this.genActivateFailureResponse);
                }
        });
    },
    
     genActivateSuccessResponse:function(response){
         for(var i=0;i<this.recArr.length;i++){
             var ind=this.store.indexOf(this.recArr[i])
             var num= ind%2;
             WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var store = this.store;
        var fn = function (btn) {
            if (btn === "ok" && response.success) {
                    Wtf.salesAccStore.reload();
                    store.reload();
            }
        }
        WtfComMsgBox([(WtfGlobal.getLocaleText("acc.coa.tabTitle")), response.msg],response.success*2+1,undefined,undefined,fn);    //"Chart of Account"
    },

    genActivateFailureResponse:function(response){
        for(var i=0;i<this.recArr.length;i++){
            var ind=this.store.indexOf(this.recArr[i])
            var num= ind%2;
            WtfGlobal.highLightRowColor(this.grid,this.recArr[i],false,num,2,true);
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },


    opBalRenderer:function(val,m,rec){
        return WtfGlobal.withoutRateCurrencyDeletedSymbol(Math.abs(val),m,rec);
    },

    balTypeRenderer:function(val,m,rec){
        val=(val==0?WtfGlobal.getLocaleText("acc.field.N/A"):(val>0?WtfGlobal.getLocaleText("acc.common.debit"):WtfGlobal.getLocaleText("acc.common.credit")));
       return WtfGlobal.deletedRenderer(val,m,rec)
    },
    
    showAdvanceSearch: function() {
        showAdvanceSearch(this, this.searchparam, this.filterAppend);
    },
    
    showOpeningBalanceTransactionWindow: function(){
        this.recArr =this.grid.getSelectionModel().getSelections();
        if(this.grid.getSelectionModel().hasSelection()==false||this.grid.getSelectionModel().getCount()>1){
            WtfComMsgBox(19,2);
            return;
        }
        var rec=this.recArr[0];
        this.callOpeningBalaneWindow(rec);
    },
    
    callOpeningBalaneWindow:function(rec){
        var openingBalenceWindow = new Wtf.account.openingBalanceWindowForAccount({
            title:WtfGlobal.getLocaleText("acc.account.AddUnclearedDepositWithdraw"),
            layout:'border',
            accRec:rec,
            id:'openBalWinId',
            resizable:false,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            modal:true,
            height:600,
            width:700
        });
        openingBalenceWindow.show();
    },
    
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
    },
    
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.store.baseParams = {   
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleId,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.store.load({params: {ss: this.quickSearchTF.getValue(), start: 0, limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt}});
    },
    
    clearStoreFilter: function() {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.store.baseParams = {
            flag: 1,
            searchJson: this.searchJson,
            moduleid: this.moduleId,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.store.load({params: {ss: this.quickSearchTF.getValue(), start: 0, limit: (this.pageLimit && this.pageLimit.combo) ? (this.pageLimit.combo.getValue() || this.msgLmt) : this.msgLmt}});
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },

    subLedgerReport:function(){
       new Wtf.account.subLedgerWindow({
            title: WtfGlobal.getLocaleText("acc.field.subLedgerExport"),  
            id: 'subLedgerWindowID',
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            parentObj:this,
            closable: true,
            resizable: false,
            modal: true,
            scope :this,
            width: 840,
            height: 550
        }).show();
    },
    getMyConfig : function(){
        if (this.isGeneralLedger) {
            WtfGlobal.getGridConfig (this.grid, Wtf.ACC_GENERAL_LEDGER_REPORT_MODULEID, false, false);
        } else {
            WtfGlobal.getGridConfig (this.grid, this.moduleId, false, false);
        }
        
        var statusForCrossLinkage = this.grid.getColumnModel().findColumnIndex("statusforcrosslinkage");
        if (statusForCrossLinkage != -1) {
            this.grid.getColumnModel().setHidden(statusForCrossLinkage, true);
        }
    },
   
    saveMyStateHandler: function(grid,state){
        if (this.isGeneralLedger) {
            WtfGlobal.saveGridStateHandler(this, grid, state, Wtf.ACC_GENERAL_LEDGER_REPORT_MODULEID, grid.gridConfigId, false);
        } else {
            WtfGlobal.saveGridStateHandler(this, grid, state, this.moduleId, grid.gridConfigId, false);
        }
    }
});


//Component for set monthly budgets
Wtf.account.accMonthlyBudgetWindow = function(config){
    var btnArr=[];    
    this.isMonthlyBudget=config.isMonthlyBudget;
    btnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.update"),  //'Update',
        scope: this,
        handler:this.addArr.createDelegate(this)
    });

    btnArr.push({
        text: WtfGlobal.getLocaleText("acc.common.close"),  //'Close',
        scope: this,
        handler: function(){
            this.close();
        }
    });
    Wtf.apply(this,{
        buttons: btnArr
    },config);
    Wtf.account.accMonthlyBudgetWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.accMonthlyBudgetWindow, Wtf.Window, {
    closable: true,
    addDeleteCol: true,
    rowDeletedIndexArr:null,
    rowIndexArr:null,    
    modal: true,
    iconCls :getButtonIconCls(Wtf.etype.deskera),
    width: 1100,
    record:null,
    height: 620,
    resizable: false,
    layout: 'border',
    buttonAlign: 'right',
    initComponent: function(config){
        Wtf.account.accMonthlyBudgetWindow.superclass.initComponent.call(this, config);
        if(this.addDeleteCol){
            this.cm.push({
                width:50,
                header:WtfGlobal.getLocaleText("acc.masterConfig.costCenter.action"),  //'Action',
                renderer:this.deleteBudgetRenderer.createDelegate(this)
            });
        }
    },

    onRender: function(config){
        this.rowDeletedIndexArr=[];
        this.rowIndexArr=[];
        Wtf.account.accMonthlyBudgetWindow.superclass.onRender.call(this, config);
                
        var data=WtfGlobal.getBookBeginningYear(true);
    
        this.yearStore= new Wtf.data.SimpleStore({
            fields: [{
                name:'id',
                type:'int'
            }, 'yearid'],
            data :data
        });
    
    
    this.dimensionFlag=config.dimensionFlag?config.dimensionFlag:false; //flag to detect it is dimension flag
        this.dimensionListRec = new Wtf.data.Record.create ([
        {
            name: 'fieldid'
        },

        {
            name: 'fieldlabel'
        }
        ]);
        this.moduleid=Wtf.financialStatementsModuleIds.tradingProfitAndLoss;
        var params = {
            customcolumn:0 ,
            moduleid: this.moduleid,
            isAdvanceSearch:true,
            iscustomdimension:true,
            isActivated:1,
            excludeModule: (this.moduleid==100 || this.moduleid==Wtf.financialStatementsModuleIds.tradingProfitAndLoss || this.moduleid==102) ? Wtf.Account_Statement_ModuleId : "",
            isAvoidRedundent:true,//not allowing product custom field
            linelevelfields:false,
            globallevelfields:false,
            isCustomDetailReport: false,
            splitOpeningBalance:true  //Flag used to enable advance search on address fields
        }
        this.dimensionListStore =  new Wtf.data.Store({
            url: "ACCAccountCMN/getFieldParams.do",
            baseParams:params,
            reader: new  Wtf.data.KwlJsonReader({
                root: "data",
                autoLoad:false
            },this.dimensionListRec)
        });
        this.dimensionListStore.load();
        this.dimensionListStore.on('load', function(){
            this.dimensionListStore.remove(this.dimensionListStore.getAt(0));
            var re = new Wtf.data.Record({
                fieldid: "All",
                fieldlabel: "All"
            });
            this.dimensionListStore.insert(0, re);
        },this);
        
        this.dimensionList = new Wtf.form.ExtFnComboBox({
            store : this.dimensionListStore,
            fieldLabel: WtfGlobal.getLocaleText("acc.budgeting.dimension"), // "Dimension",
            emptyText: WtfGlobal.getLocaleText("acc.field.pleaseSelectDimension"), // "Please select dimension.",
            editable:false,
            typeAhead: true,
            selectOnFocus:true,
            displayField:'fieldlabel',
            valueField : 'fieldid',
            triggerAction: 'all',
            mode:'remote',
            extraComparisionField: "",
            extraFields: "",
            listWidth: 400,
            ctCls : 'widthforCmb'
        });
    
       this.dimensionList.on('select', this.loadDimensionValueList, this);
        
        this.dimensionValueListRec = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}
        ]);
        
        this.dimensionValueListStore = new Wtf.data.Store({
            url: "ACCAccountCMN/getDimensionValuesForCombo.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.dimensionValueListRec)
        });

        this.dimensionValueList = new Wtf.common.Select({
            fieldLabel: WtfGlobal.getLocaleText("acc.budgeting.dimensionValues"), // "Dimension Values",
            emptyText: WtfGlobal.getLocaleText("acc.field.pleaseSelectDimensionValues"), // "Please select dimension values.",
            store: this.dimensionValueListStore,
            valueField: 'id',
            displayField: 'name',
            clearTrigger: true,
            mode: 'local',
            width: 200,
            typeAhead: true,
            selectOnFocus: true,
            allowBlank: true,
            triggerAction: 'all',
            scope: this,
            multiSelect: true,
            forceSelection: true
        });
        
        this.dimensionValueList.on('collapse',function(combo){
            this.loadGridStore()
        },this);
        this.dimensionValueList.on('clearval', function (combo, rec, index) {
            this.loadGridStore();
        }, this);
        
        this.year = new Wtf.form.ComboBox({
            store: this.yearStore,
            fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year"),  //'Year',
            name:'startYear',
            displayField:'yearid',
            anchor:'95%',
            valueField:'yearid',
            forceSelection: true,
            mode: 'local',
            triggerAction: 'all',
            width:90,
            selectOnFocus:true
        });  

        this.year.setValue(this.yearStore.data.items[0].json[1]);
       
        this.year.on("change",function(){ 
            this.loadGridStore();
            this.grid.loadMask.hide();
        },this);
       
        this.createDisplayGrid();
       
        this.fs2=new Wtf.form.FieldSet({
            width:300,
            height:210,
            title:WtfGlobal.getLocaleText("acc.fieldsetheaderselectyear"), // 'Select Year',
            items:[       
            this.year,
            this.dimensionList,
            this.dimensionValueList
            ]
        }); 
       
        this.add({
            region: 'north',
            height: (!this.isMonthlyBudget) ? 75 : 210,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            items : [{
                columnWidth:.50,                
                border:false,
                layout:"fit",
                autoScroll:true,
                html: getTopHtml(this.title,WtfGlobal.getLocaleText("acc.rem.27")+" "+this.title,this.headerImage, true)
            },{
                columnWidth:.50,
                border:false,
                layout:"fit",
                hidden:!this.isMonthlyBudget,
                autoScroll:true,
                items:[this.fs2]
            }]    
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:[this.grid]

        });
        this.addEvents({
            'update':true
        });
       
       this.store.load({
            params:{
                year:this.year.getValue(),
                dimensionFlag:this.dimensionFlag
            }
        });
   },
  loadGridStore:function(){
      if(this.year!=null && this.year !=undefined){
            this.store.load({
                params:{
                    year:this.year.getValue(),
                    dimensionid:this.dimensionList.getValue(),
                    dimensionvalue:this.dimensionValueList.getValue(),
                    dimensionFlag:(this.dimensionList.getValue()!=null && this.dimensionList.getValue()!=undefined)?true:false
                }
            });
            this.grid.loadMask.hide();  
        }
    },
    loadDimensionValueList: function(combo, record, index) {
        this.dimensionFlag=true;
        this.dimensionValueList.reset();
        this.dimensionValueListStore.load({
            params: {
                groupid: this.dimensionList.getValue()
            }
        });
        this.loadGridStore();
        if(this.dimensionList.getValue()==="All"){
            this.dimensionValueList.disable();
        }else{
            this.dimensionValueList.enable();
        }
    },
    createDisplayGrid:function(){        
        this.grid = new Wtf.grid.EditorGridPanel({
            layout:'fit',
            clicksToEdit:1,
            store: this.store,
            cm: new Wtf.grid.ColumnModel(this.cm),
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"))
            }
        }); 
         this.grid.on('rowclick',this.processRow,this);
    },
    
    getdeletedArr:function(grid,index,rec){
        var store=grid.getStore();
        var fields=store.fields;
            var recarr=[];
            if(rec.data['taxid']!=""){
                for(var j=0;j<fields.length;j++){
                    var value=rec.data[fields.get(j).name];
                    switch(fields.get(j).type){
                        case "auto":value="'"+value+"'";break;
                        case "date":value="'"+WtfGlobal.convertToGenericDate(value)+"'";break;
                    }
                    recarr.push(fields.get(j).name+":"+value);
                }
                recarr.push("modified:"+rec.dirty);
                this.rowDeletedIndexArr.push("{"+recarr.join(",")+"}");
            }
    },
    
    processRow: function(grid, rowindex, e) {
        if (e.getTarget(".delete-gridrow")) {
            var rec = grid.getStore().getAt(rowindex);
            var id = 0;
            if (rec) {
                id = rec.data.id;
            }
            if (e.getTarget(".delete-gridrow")) {
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"),(this.isMonthlyBudget)?WtfGlobal.getLocaleText("acc.tax.msg5"): WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn) {
                    if (btn != "yes")
                        return;
                    else {
                        if (id == 0) {
                            var store = grid.getStore();
                            var total=store.getCount();
                            var rec = store.getAt(rowindex);
                            this.getdeletedArr(grid, rowindex, rec);
                            store.remove(store.getAt(rowindex));
                            if(rowindex==total-1){
                                this.store.load({
                                    params:{
                                        year:this.year.getValue()
                                    }
                                });
                                this.grid.loadMask.hide();
                                this.addGridRec();
                            }
                            return;
                        }
                        Wtf.Ajax.requestEx({
                            url: (this.isMonthlyBudget)?"ACCAccount/deleteMonthlyBudget.do":"ACCAccount/deleteMonthlyForecast.do",
                            params: {recordId: id}

                        }, this, function(response) {
                            var store = grid.getStore();
                            var rec = store.getAt(rowindex);
                            this.getdeletedArr(grid, rowindex, rec);
                            store.remove(store.getAt(rowindex));
                            //this.addGridRec();
                        },function(response) {});
                    }
                }, this);
            }             
        }
    },
    
    checkrecord:function(obj){
        if(this.istax){
            var idx = this.grid.getStore().find("taxid", obj.record.data["taxid"]);
            if(idx>=0)
                obj.cancel=true;
        }
    },

     checkDuplicate:function(obj){
        if(this.istax &&obj.field=="taxname"){
           var FIND = obj.value;
            FIND =FIND.replace(/\s+/g, '');
            var index=this.grid.getStore().findBy( function(rec){
            var taxname=rec.data['taxname'].trim();
            taxname=taxname.replace(/\s+/g, '');
            if(taxname==FIND)
                return true;
            else
                return false
        })
        if(index>=0){
                obj.cancel=true;
        }
        }
    },
    
    addArr:function(){
        var inValidRows = new Array();
        var cm = this.grid.getColumnModel();

        var editedarr=[];
         for(var i=0;i<this.store.getCount();i++){
             var   rec=this.store.getAt(i);
            if(rec.dirty){
                editedarr.push(i);

                for(var j=0;j<cm.getColumnCount();j++){
                    var editor = cm.getCellEditor(j,i);
                    var cellData = ""+rec.data[cm.getDataIndex(j)];
                    if(editor != undefined && editor.field.allowBlank !=undefined && !editor.field.allowBlank && cellData.trim().length == 0){
                        inValidRows.push(i+1);
                        break;
                    }
                }
            }
        }

        if(inValidRows.length>0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.tax.msg3")+" "+(inValidRows.join(","))], 2);
            return;
        }

        this.rowIndexArr=editedarr;
        this.update(editedarr);
    },
    
    addBlankRec:function(){ 
        var size=this.store.getCount();
        if(size==0){
            
            var newrec = Wtf.data.Record.create({
                id:"-1",
                accountid:"",
                jan:"",
                feb:"",
                march:""
            });
            this.store.add(newrec);
        }
    },
    
    addGridRec:function(){ 
        var size=this.store.getCount();
        if(size>0){
            var lastRec=this.store.getAt(size-1);
            var cm=this.grid.getColumnModel();
            var count = cm.getColumnCount();
            for(var i=0;i<count-1;i++){
                if(lastRec.data[cm.getDataIndex(i)].length<=0){
                    if(this.mode==34 && i==1){                    
                        continue;                    
                    }else{
                        return;
                    }
                }
            }
        }
        var rec=this.record;
        rec = new rec({});
        rec.beginEdit();
        var fields=this.store.fields;
        for(var x=0;x<fields.length;x++){
            var value="";
            rec.set(fields.get(x).name, value);
        }
        rec.endEdit();
        rec.commit();
        this.store.add(rec);
    },
   
    deleteRenderer:function(v,m,rec){
        var flag=true;
        var cm=this.grid.getColumnModel();
        var count = cm.getColumnCount();
        for(var i=0;i<count-1;i++){            
            if(rec.data[cm.getDataIndex(i)].length<=0){
                if(this.mode==34 && i==1){
                    flag=true;                
                }else{
                    flag=false;
                    break;
                }
            }
        }
        if(flag){
              var deletegriclass=getButtonIconCls(Wtf.etype.deletegridrow);
            return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
        }
        return "";
    }, 

    deleteBudgetRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },

    update:function(arr){
        var rec;
        var recData="";
        var finalarray=[];
        if(this.store.getCount()>0){
            for(var arrCount=0;arrCount<this.store.getCount();arrCount++)
            {
                var fields=this.store.fields;            
                var recarr=[];
                var record=null;            
                record=this.store.getAt(arrCount);
                if(record!=undefined)
                {    
                    for(var j=0;j<fields.length;j++){
                        var value=record.data[fields.get(j).name];                           
                            switch(fields.get(j).type){
                                case "auto":
                                    if(value!=undefined){
                                    value=(value+"").trim();
                                }
                                value=encodeURI(value);
                                    value="\""+value+"\"";
                                    break;
                                case "date":
                                    value="'"+WtfGlobal.convertToGenericDate(value)+"'";
                                    break;
                            }
                            recarr.push(fields.get(j).name+":"+value);                            
                    }
                }
                if(recarr.length>0)
                {
                    recarr.push("modified:"+record.dirty);                        
                    finalarray.push("{"+recarr.join(",")+"}");                
                }
            }
            recData="["+finalarray.join(',')+"]";                        
        
            
            rec={
                jsondata:recData,
                year:this.year.getValue(),
                dimensionid:this.dimensionList.getValue(),
                dimensionvalue:this.dimensionValueList.getValue(),
                dimensionFlag:(this.dimensionList.getValue()!=null && this.dimensionList.getValue()!=undefined)?true:false
            };

            
            Wtf.Ajax.requestEx({
                 url :(this.isMonthlyBudget)?"ACCAccount/saveUpdateMonthlyBudget.do":"ACCAccount/saveUpdateMonthlyForecast.do",
                params: rec
            },this,this.genSuccessResponse,this.genFailureResponse);
            
        }else{
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Norecordsavailabetoupdate")],2);
        }
       
    },

    getJSONArray:function(arr){
        return WtfGlobal.getJSONArray(this.grid,false,arr);
    },
    
    genSuccessResponse:function(response){
        WtfComMsgBox([this.title,response.msg],0);
        if(response.success){    
            this.fireEvent('update',this);
            this.store.reload();            
            this.close();
        }
    },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    }

}); 


/* Sub Ldger Window Component*/
Wtf.account.subLedgerWindow = function(config) {
 this.parentObj = config.parentObj;
 Wtf.apply(this,{
     
        buttons: [ this.exportData=new Wtf.exportButton({
                    obj:this.parentObj,
                    text:WtfGlobal.getLocaleText("acc.common.export"),
                    tooltip:WtfGlobal.getLocaleText("acc.common.exportTT"),   
                    menuItem:{pdf:true},
                    params:this.parentObj.isGeneralLedger?{
                        group:[12],
                        ignore:true,
                        isGeneralLedger:this.parentObj.isGeneralLedger,
                        isGroupDetailReport:config.isGroupDetailReport,
                        issubGeneralLedger:true,
                        generalLedgerFlag:true,
                        exportThreadFlagLedger : false,
                        startDate:WtfGlobal.convertToGenericStartDate(this.parentObj.startDate.getValue()),
                        stdate:WtfGlobal.convertToGenericStartDate(this.parentObj.startDate.getValue()),
                        endDate:WtfGlobal.convertToGenericEndDate(this.parentObj.endDate.getValue()),
                        enddate:WtfGlobal.convertToGenericEndDate(this.parentObj.endDate.getValue()),
                        balPLId:this.parentObj.balPLTypeCombo.getValue(),
                        accountIds:this.parentObj.MultiSelectAccCombo.getValue(),
                        isExportingSelectedRecord:true,
                        excludePreviousYear: this.parentObj.excludeTypeCmb.getValue(),
                        includeExcludeChildBalances: this.parentObj.includeExcludeChildCmb ? this.parentObj.includeExcludeChildCmb.getValue():""
                   }:({group:[12],ignore:true}),
                        
                    get : config.isGroupDetailReport ? Wtf.autoNum.GroupDetailReport : 112,
                    label:WtfGlobal.getLocaleText("acc.field.ChartofAccount")
                }),{
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            scope: this,
            handler:this.closeForm.createDelegate(this)
        }]
    },config);
    Wtf.account.subLedgerWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.subLedgerWindow, Wtf.Window, {

    onRender: function(config) {
        Wtf.account.subLedgerWindow.superclass.onRender.call(this, config);
      
        this.createFields();
        
        this.add(this.northPanel = new Wtf.Panel({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText("acc.field.subLedger"),WtfGlobal.getLocaleText("acc.field.subLedger"), "../../images/accounting_image/price-list.gif")
        }));
        
        this.add(this.centerPanel = new Wtf.Panel({
            region: 'center',
            border: false,
//            autoScroll: true,
//            autoWidth: true,
            bodyStyle: 'background:#f1f1f1; font-size:10px; padding:10px',
            baseCls: 'bckgroundcolor',
            layout: 'form',
            width: 830,
            items:[{
                xtype:'fieldset',
                autoHeight:true,
//                autoWidth: true,
                width: 800,
                title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.subLedgerMainGroup")+"'>"+WtfGlobal.getLocaleText("acc.field.subLedgerMainGroup")+"</span>" ,
                items: [this.mainGroupSearchComponent]                       
            }
            ,{
                xtype:'fieldset',
                autoHeight:true,
//                autoWidth: true,
                width: 800,
                title:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.field.subLedgerSunGroup")+"'>"+WtfGlobal.getLocaleText("acc.field.subLedgerSunGroup")+"</span>" ,
                items: [this.subGroupSearchComponent]                     
            } ]
        }));
        this.mainGroupSearchComponent.show();
        this.mainGroupSearchComponent.advGrid.advSearch = true;
        this.mainGroupSearchComponent.advGrid.getComboData();
        this.subGroupSearchComponent.show();
        this.subGroupSearchComponent.advGrid.advSearch = true;
        this.subGroupSearchComponent.advGrid.getComboData();
        this.doLayout();        
    },

    createFields: function() {  
        this.exportData.on('click',function(){
            if(this.mainGroupSearchComponent.advGrid.searchStore.getCount()<1 || this.subGroupSearchComponent.advGrid.searchStore.getCount()<1){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.subLedgerWarn") ], 2);
                return false;
            } else if(this.mainGroupSearchComponent.advGrid.searchStore.getAt(0).get("column")== (this.subGroupSearchComponent.advGrid.searchStore.getAt(0).get("column"))){
                 WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.subLedgerSameDimension") ], 2);
                return false;
            }
            var filterJson=[];
            this.exportData.params.startDate=WtfGlobal.convertToGenericStartDate(this.parentObj.startDate.getValue());
            this.exportData.params.stdate=WtfGlobal.convertToGenericStartDate(this.parentObj.startDate.getValue());
            this.exportData.params.endDate=this.parentObj.isGeneralLedger?WtfGlobal.convertToGenericEndDate(this.parentObj.endDate.getValue()):WtfGlobal.convertToGenericDate(this.parentObj.endDate.getValue());
            this.exportData.params.enddate=this.parentObj.isGeneralLedger?WtfGlobal.convertToGenericEndDate(this.parentObj.endDate.getValue()):WtfGlobal.convertToGenericDate(this.parentObj.endDate.getValue());
            this.exportData.params.balPLId=this.parentObj.balPLTypeCombo.getValue();
            this.exportData.params.accountIds=this.parentObj.MultiSelectAccCombo.getValue();
            this.exportData.params.excludePreviousYear= this.parentObj.excludeTypeCmb.getValue();
            this.exportData.params.includeExcludeChildBalances = this.parentObj.includeExcludeChildCmb ? this.parentObj.includeExcludeChildCmb.getValue():"";
            this.exportData.params.isGeneralLedger=this.parentObj.isGeneralLedger;
            this.exportData.params.isGroupDetailReport = this.isGroupDetailReport;
            this.exportData.params.issubGeneralLedger = true;
            this.exportData.params.filterConjuctionCriteria = 'AND';//this.parentObj.filterConjuctionCriteria,
            
            if(this.isGroupDetailReport){
                this.exportData.params.periodView = true  
            }
         
            this.exportData.params.mainGroupJSON = this.mainGroupSearchComponent.advGrid.getSearchJSON().columnheader;
            this.exportData.params.subGroupJSON = this.subGroupSearchComponent.advGrid.getSearchJSON().columnheader;
            filterJson.push(this.mainGroupSearchComponent.advGrid.getSearchJSON());
            filterJson.push(this.subGroupSearchComponent.advGrid.getSearchJSON());
            filterJson = {
                root:filterJson
            }
            this.exportData.params.searchJson = Wtf.encode(filterJson);
            if(this.parentObj.accountTransactionMasterTypeCombo!=undefined){
                this.exportData.params.accountTransactionType = this.parentObj.accountTransactionMasterTypeCombo.getValue();	//ERP-29770
            }
            if(this.parentObj.accountMasterTypeCombo!=undefined && this.parentObj.accountMasterTypeCombo.getValue()==1){
                this.exportData.params.ignoreBankAccounts=true;
                        if(this.exportData.params.ignoreCashAccounts)
                            delete this.exportData.params.ignoreCashAccounts;
                    }else if(this.parentObj.accountMasterTypeCombo!=undefined && this.parentObj.accountMasterTypeCombo.getValue()==2){
                        this.exportData.params.ignoreCashAccounts=true;
                        if(this.parentObj.exportData.params.ignoreBankAccounts)
                            delete this.exportData.params.ignoreBankAccounts;
                    }else if(this.exportData.params.ignoreBankAccounts || this.exportData.params.ignoreCashAccounts){
                        delete this.exportData.params.ignoreCashAccounts;
                        delete this.exportData.params.ignoreBankAccounts;
            }
        },this);
        this.mainGroupSearchComponent = new Wtf.advancedSearchComponent({
            moduleid: 102,
            height : 40,
            ignoreDefaultFields : true,
//            hideRememberSearchBttn:true,
            isSubLdgerExport:true,
//            hideFilterConjunction : true,
//            hideSearchBttn :true,
//            hideCloseBttn :true,
            advSearch: true
        });
        this.subGroupSearchComponent = new Wtf.advancedSearchComponent({
//            hideRememberSearchBttn:true,
//            hideFilterConjunction : true,
//            hideSearchBttn :true,
            isSubLdgerExport:true,
            ignoreDefaultFields : true,
//            hideCloseBttn :true,
            moduleid: 102,
              height : 40,
            advSearch: true
        });
    },
    
    filterStoreMainGroup: function(json, filterConjuctionCriteria) {
        this.mainGroupSearchJson = json;
    },
    
    filterStoreSubGroup: function(json, filterConjuctionCriteria) {
        this.subGroupSearchJson = json;
    },
    
    closeForm: function() {
        this.close();
    }
});
