/*
 * 
 * @Author Malhari Pawar/Pandurang Mukhekar
 * Vendor type=1
 * Custmor type=2
 * Bank type=3
 * 
 * 
 **/

Wtf.account.selectAccountwin = function(config) {
    this.okBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.setupWizard.next")  //'Send'
    });
    this.okBtn.on('click', this.handleSend, this);
    this.closeBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.close")  //'Close'
    });
    this.closeBtn.on('click', this.handleClose, this);

    Wtf.apply(this, {
        title: WtfGlobal.getLocaleText("acc.field.AccountsRe-evaluation"), //"Send Mail",
        buttons: [this.okBtn, this.closeBtn]
    }, config);

    Wtf.account.selectAccountwin.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.selectAccountwin, Wtf.Window, {
    getRecord: function() {
        Wtf.Ajax.requestEx({
            url: "ProfileHandler/getAllUserDetails.do",
            params: {
                mode: 11,
                lid: Wtf.userid
            }
        }, this, this.genSuccessResponse, this.genFailureResponse);

    },
    genFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 1);
    },
    onRender: function(config) {
        this.createForm();
        this.getRecord();

        this.add({
            region: 'center',
            border: false,
            baseCls: 'bckgroundcolor',
            items: this.Form
        });

        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText("acc.field.AccountsRe-evaluation"), WtfGlobal.getLocaleText("acc.field.SelectDetails"), "../../images/accounting_image/Chart-of-Accounts.gif")
        }, this.centerPanel = new Wtf.Panel({
            region: 'center',
            border: false,
            autoScroll: true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls: 'bckgroundcolor',
//            layout: 'border',
            items: [this.Form]
        }));
        Wtf.account.selectAccountwin.superclass.onRender.call(this, config);
    },
    createForm: function() {

         this.groupStore = new Wtf.data.SimpleStore({
            fields: [{name:'groupid',type:'int'}, 'groupname'],
            data :[[1,'Accounts Payable'],[2,'Accounts Receivable'],[3,'Bank'],[4,'Cash']]
        });
        
        this.accGroup = new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.rem.18") + '*',
            hiddenName: 'groupid',
            name: 'groupid',
            store: this.groupStore,
            valueField: 'groupid',
            displayField: 'groupname',
            typeAhead: true,
            forceSelection: true,
            width: 250,
            allowBlank: false,
            mode: 'local',
            emptyText: WtfGlobal.getLocaleText("acc.reval.accType"),
            disableKeyFilter: true,
            lastQuery: '',
            hirarchical: true,
            triggerAction: 'all'
        });

        this.monthStore = new Wtf.data.SimpleStore({
            fields: [{name:'monthid',type:'int'}, 'name'],
            data :[[0,'January'],[1,'February'],[2,'March'],[3,'April'],[4,'May'],[5,'June'],[6,'July'],[7,'August'],[8,'September'],[9,'October'],
                [10,'November'],[11,'December']]
        });

        var data=[[0,new Date().format('Y')],[1,new Date().format('Y')-1]];

        this.yearStore= new Wtf.data.SimpleStore({
                fields: [{name:'id',type:'int'}, 'yearid'],
                data :data
        });

//        this.selectedMonth = new Wtf.form.ComboBox({
//                store: this.monthStore,
//                fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month")+ "*",  //'Month',
//                name:'selectedMonth',
//                displayField:'name',
//                forceSelection: true,
//                 allowBlank: false,
//                //anchor:'95%',
//                 width: 250,
//                emptyText: WtfGlobal.getLocaleText("acc.reval.month"), 
//                valueField:'monthid',
//                mode: 'local',
//                triggerAction: 'all',
//                selectOnFocus:true
//        });  
//
//        this.selectedYear = new Wtf.form.ComboBox({
//                store: this.yearStore,
//                fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year")+ "*",  //'Year', 
//                name:'selectedYear',
//                displayField:'yearid',
//                //anchor:'95%',
//                 allowBlank: false,
//                 width: 250,
//                 emptyText: WtfGlobal.getLocaleText("acc.reval.year"), 
//                valueField:'yearid',
//                forceSelection: true,
//                mode: 'local',
//                triggerAction: 'all',
//                selectOnFocus:true
//        });  

         this.revalueDateConfig = {
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.reval.evaldate") + "'>" + WtfGlobal.getLocaleText("acc.reval.evaldate") + "</span>" + ' *',
            name: 'dob',
            emptyText: WtfGlobal.getLocaleText("acc.reval.date"), 
            hiddenName: 'revalueDate',
            id: "revalueDate" + this.id
        };
         this.revalueDate = WtfGlobal.createDatefield(this.revalueDateConfig, false, this);
         this.revalueDate. width= 250;

        this.currencyRec = new Wtf.data.Record.create([
            {name: 'currencyid', mapping: 'tocurrencyid'},
            {name: 'symbol'},
            {name: 'currencyname', mapping: 'tocurrency'},
            {name: 'exchangerate'},
            {name: 'currencycode'},
            {name: 'htmlcode'}
        ]);
        this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.currencyRec),
//        url:Wtf.req.account+'CompanyManager.jsp'
            url: "ACCCurrency/getCurrencyExchange.do"
        });
        
       this.currencyMSComboconfig={
            hiddenName:'currencyid',         
            store: this.currencyStore,
            valueField:'currencyid',
            displayField:'currencyname',
            emptyText: WtfGlobal.getLocaleText("acc.cust.currencyTT"), //'Please select Currency...',
            mode: 'local',
            typeAhead: true,
            allowBlank: false,
            selectOnFocus:true,
            triggerAction:'all',
            scope:this
        };
        
        this.Currency = new Wtf.common.Select(Wtf.applyIf({
            multiSelect:true,
            fieldLabel: WtfGlobal.getLocaleText("acc.coa.gridCurrency") + "*", //'Currency*',
            forceSelection:true,   
            extraFields:[],
            extraComparisionField:[],// type ahead search on acccode as well.
            listWidth:250,
            width: 250
        },this.currencyMSComboconfig));
            
        this.currencyStore.on('load',function(){ // Removing base currency from currency store
             var baseCurrencyRec = WtfGlobal.searchRecord(this, gcurrencyid, 'currencyid');
             this.remove(baseCurrencyRec); 
        });
        this.currencyStore.load({params: {mode: 201, transactiondate: WtfGlobal.convertToGenericDate(new Date()), isAll: true}});
        
        this.Form = new Wtf.form.FormPanel({
            height: 'auto',
            border: false,
            items: [{
                    layout: 'form',
                    bodyStyle: "background: transparent; padding: 20px;",
//                labelWidth:60,
                    border: false,
                    items: [this.accGroup,this.revalueDate, this.Currency  // this.startdate, this.enddate this.selectedMonth,this.selectedYear
                    ]
                }]
        });
    },
    handleClose: function() {
        this.fireEvent('cancel', this)
        this.close();
    },
     getDates:function(start){
        var d=Wtf.serverDate;
//        if(this.statementType=='BalanceSheet'&&start)
//             return new Date('January 1, 1970 00:00:00 AM');
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
    handleSend: function(bobj, edfd) {
         var valid=this.Form.getForm().isValid();
        if(valid==false){
            WtfComMsgBox(2, 2);
            return;
        }
        /*
         *Revaluation is not applicable for Base Currency
         */
        if(Wtf.account.companyAccountPref.currencyid==this.Currency.getValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.accountrevaluationmsg")], 2);
            return;
        }
        
//        this.okBtn.disable();
//        this.closeBtn.disable();30- 4 6 9 11  31- 1 3 5 7 8 10 12 28,29-2 
//          var month1=this.selectedMonth.getValue();
//          var month=month1+1;
//          var year=this.selectedYear.getValue();
          var endDate=Wtf.serverDate.clearTime();
          var startDate=Wtf.serverDate.clearTime();
////          startDate.setDate(1);
//         var startDay=1;
//         var endDay=31;
//         if(month==2){
//             if(year%4==0 ||year%400==0){
////                 endDate.setDate(29);
//                endDay=29;
//             }else{
////                 endDate.setDate(28);
//                endDay=28;
//             }
//         } else if(month==4 ||month==6||month==9||month==11){
////             endDate.setDate(30);
//                endDay=30;
//         }else{      //(month==1 || month==3|| month==5|| month==7|| month==8|| month==10|| month==12)
////             endDate.setDate(31);
//             endDay=31;
//         }       
////         endDate.setMonth(month1);
////         startDate.setMonth(month1);
////         endDate.setYear(year);
////         startDate.setYear(year);
        startDate=this.revalueDate.getValue(); //new Date(year,month1,startDay);
        endDate=this.revalueDate.getValue();  //new Date(year,month1,endDay);
         
//        if (this.sDate > this.eDate) {
//            WtfComMsgBox(1, 2);
//            return;
//        }
        Wtf.Ajax.requestEx({
                    url: "ACCJournal/getRevalMonthYearStatus.do",
                    params:{
                    revaldate:WtfGlobal.convertToGenericDate(this.revalueDate.getValue()),
                    accountType:this.accGroup.getValue(),
                    currencyID:this.Currency.getValue()
           
                    }
                },this,
                   function(responseObj) {
                        if (responseObj.success==true && responseObj.count==0) {
                                if ((this.Form.getForm().isValid())) {
                                this.handleClose();
                                panel = new Wtf.account.CurrencyExchangeWindow({
                    //            id:winid,
                                    closable: true,
                                    modal: true,
                                    iconCls: getButtonIconCls(Wtf.etype.deskera),
                                    width: 600,
                                    isRevaluation: true,
                                    revalueCurrenyId: this.Currency.getValue(),
                                    accTypeId: this.accGroup.getValue(),
                                    startDate: startDate,
                                    endDate: endDate,
                                    height: 400,
                                    resizable: false,
                                    layout: 'border',
                                    buttonAlign: 'right',
                                    renderTo: document.body
                                });
                                panel.show();
                            } else {
                                WtfComMsgBox(2, 2);
                            }

                        }else{
                            WtfComMsgBox(56, 2);
                        }         
                   }
      );          

    },
    success: function(response) {
        if (response.success) {
            WtfComMsgBox([this.label, response.msg], 3);
            this.handleClose();
        } else {
            if (response.msg && response.isMsgSizeException) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
            } else {
                WtfComMsgBox([this.label, WtfGlobal.getLocaleText("acc.rem.210")], 3);
            }
            this.sendBtn.enable();
            this.closeBtn.enable();
        }
    },
    failure: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        this.handleClose();
    },
    mailSuccessResponse: function(response) {
        this.close();
    },
    mailFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        this.close();
        }

});


Wtf.account.revalueateGrid = function(config) {
    this.startDate=config.startDate,
    this.endDate=config.endDate,
    this.filename= "Account Revaluation",
    this.okBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.fixedAssetList.post"),  //'POst'
        hidden:WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.post)
    });
    this.okBtn.on('click', this.saveData, this);
    this.exportButton=new Wtf.exportButton({
        obj:this,
        isEntrylevel:false,
        id:"exportReports"+this.id,   //+config.id,   //added this.id to avoid dislocation of Export button option i.e. CSV & PDF
        text: WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
        disabled :false,
        usePostMethod:false,
        filename : this.filename,
        excludeCustomHeaders:true,
        moduleId:this.moduleId,
//            menuItem:{csv:true,pdf:true,rowPdf:(this.isSalesCommissionStmt)?false:true,rowPdfTitle:WtfGlobal.getLocaleText("acc.rem.39") + " "+ singlePDFtext},
        menuItem:{
            csv:true,
            pdf: true , // For Customer Invoice Submenu: true and pdf: false
            xls:true,
            subMenu: false, // For Customer Invoice Submenu: true and pdf: false
////                summaryPDF:(Wtf.templateflag == Wtf.Monzone_templateflag && this.moduleid == Wtf.Acc_Invoice_ModuleId)?true:false,
            detailPDF:false,
            detailedXls: (config.accTypeId==1 ||config.accTypeId==2) ?true :false
        },  //Currently, Export to Excel available only in Purchase Order
        get:Wtf.autoNum.accountrevaluationReprot  // Mayur B - Also we have added the export Customer Register functionality for monzone. hence added two separate buttons to export detail and summaries view of template.
        });
    this.closeBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON")  //'Cancel'
    });
    this.closeBtn.on('click', this.handleClose, this);

    Wtf.apply(this, {
        title: WtfGlobal.getLocaleText("acc.field.AccountRe-evaluation"), //"Send Mail",
        buttons: [this.okBtn,this.exportButton, this.closeBtn]
    }, config);

    Wtf.account.revalueateGrid.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.revalueateGrid, Wtf.Window, {
    onRender: function(config) {
        this.createStore();
        this.createColumnModel();
        this.createExpanderStore();
        this.createGrid();
//        this.store.on('load', this.hideMsg, this)
         if(this.accTypeId==Wtf.Acc_AccountGroup_Bank ||this.accTypeId==4){
            this.grid.getColumnModel().setHidden(0,true);
        }
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText("acc.field.AccountRe-evaluation"), " ", "../../images/accounting_image/Chart-of-Accounts.gif")
        }, this.centerPanel = new Wtf.Panel({
            region: 'center',
            border: false,
            autoScroll: true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls: 'bckgroundcolor',
//            layout: 'border',
            items: [this.grid]
        }));
        Wtf.account.selectAccountwin.superclass.onRender.call(this, config);
    },
    hideMsg: function() {
        Wtf.MessageBox.hide();
    },
    createStore: function() {
        this.coaRec = new Wtf.data.Record.create([
            {name: 'accid'},
            {name: 'accname'},
            {name: 'acccode'},
            {name: 'accnamecode'},
            {name: 'currencysymbol'},
            {name: 'currencyname'},
            {name: 'currencyid'},
            {name: 'origianlamount'},
            {name: 'baseamount'},
            {name: 'revalamount'},
            {name: 'profitloss'},
            {name: 'revalid'},
            {name: 'preamount'},
            {name: 'oldRevalId'},
            {name: 'exchangeValue'}
        ]);
        this.msgLmt = 30;
        this.jReader = new Wtf.data.KwlJsonReader({
            totalProperty: 'totalCount',
            root: "data"
        }, this.coaRec);

        var baseParamsArr = {};
        baseParamsArr.mode = this.isDepreciation ? 4 : 2;

        baseParamsArr.reevalueData = this.reevalueData;
        baseParamsArr.accTypeId = this.accTypeId;
        //baseParamsArr.startdate = WtfGlobal.convertToGenericDate(this.startDate);
        baseParamsArr.enddate = WtfGlobal.convertToGenericEndDate(this.endDate);

        this.store = new Wtf.data.Store({
            reader: this.jReader,
            remoteSort: true,
//            url: Wtf.req.account +'CompanyManager.jsp',
            url: "ACCRevalReports/getAccountsForRevaluation.do",
            baseParams: baseParamsArr
        });
        this.store.on('beforeload', function(s, o) {
            WtfGlobal.setAjaxTimeOut();
            if (!o.params)
                o.params = {};
            o.params.deleted = false;
            o.params.nondeleted = this.nondeleted;

        }, this);
        this.store.on('loadexception',function(proxy, options, response){
            loadingMask.hide();
            WtfGlobal.resetAjaxTimeOut();
                if(options.reader.jsonData.success==false){
                    var msg = WtfGlobal.getLocaleText("acc.field.ErrorinTransaction....");
                    if (options.reader.jsonData.msg)
                        msg = options.reader.jsonData.msg;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                    this.close();
                }
        },this);
        var loadingMask = new Wtf.LoadMask(document.body,{
                msg : WtfGlobal.getLocaleText("acc.msgbox.50")
         });
        loadingMask.show();
        this.store.load();
        this.store.on('load', function(){
            loadingMask.hide();
            WtfGlobal.resetAjaxTimeOut();
            if (this.store.getCount() == 0) {
                this.okBtn.disable();
                this.exportButton.disable();
            }
        }, this);
         
         
    },
    createColumnModel: function() {
        this.expander = new Wtf.grid.RowExpander({});
        if(this.accTypeId!=Wtf.Acc_AccountGroup_Bank){//for Bank type expander is removed
            this.expander.on("expand",this.onRowexpand,this);
        }
        this.summary = new Wtf.ux.grid.GridSummary();
//        this.selectionModel = new Wtf.grid.CheckboxSelectionModel({
//            singleSelect: this.isFixedAsset,
//            header: (Wtf.isIE7) ? "" : '<div class="x-grid3-hd-checker"> </div>', // For IE 7 the all select option not available
//            hidden: this.isDepreciation
//        });
        var currencodeforReval=this.currencodeforReval;
        this.gridcm = new Wtf.grid.ColumnModel([this.expander,
            {
                header: WtfGlobal.getLocaleText("acc.coa.accCode"), //"Account Code",
                dataIndex: 'acccode',
                pdfwidth: 250,
                renderer: WtfGlobal.deletedRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.coa.gridAccountName"), //"Asset Name":"Account Name",
                dataIndex: 'accname',
                pdfwidth: 250,
                renderer: WtfGlobal.deletedRenderer
            },{
                header: WtfGlobal.getLocaleText("acc.coa.gridCurrency"), //'Currency',
                renderer: WtfGlobal.deletedRenderer,
                pdfwidth: 250,
                dataIndex: 'currencyname'
            }, {
                header: WtfGlobal.getLocaleText("acc.field.BalanceinForeignCurrency"),
                dataIndex: 'origianlamount',
                hidden:(this.accTypeId==1 ||this.accTypeId==2),  //[1,'Accounts Payable'],[2,'Accounts Recivable']
                pdfwidth: 250,
                align: 'right',
                scope:this,
                renderer: function(value,isCheckCenterAlign,rec) {
                    var v = parseFloat(value);
                    if (isNaN(v))
                        return value;
                    v = WtfGlobal.conventInDecimal(v,rec.data.currencysymbol);
                    v = '<div class="currency">' + v + '</div>';
                    return v;
                },
                pdfrenderer: "rowcurrency"
            },{
                header: WtfGlobal.getLocaleText("acc.field.BalanceinBaseCurrency"),
                dataIndex: 'baseamount',
                hidden:(this.accTypeId==1 ||this.accTypeId==2),  //[1,'Accounts Payable'],[2,'Accounts Recivable']
                pdfwidth: 250,
                align: 'right',
                renderer: WtfGlobal.currencyDeletedRenderer,
                pdfrenderer: "currency"
            },{
                header: WtfGlobal.getLocaleText("acc.field.BalanceafterRe-evalueationinBaseCurrency"),
                dataIndex: 'revalamount',
                hidden:(this.accTypeId==1 ||this.accTypeId==2),  //[1,'Accounts Payable'],[2,'Accounts Recivable']
                pdfwidth: 250,
                align: 'right',
                renderer: WtfGlobal.currencyDeletedRenderer,
                pdfrenderer: "currency"
            },{
                header: WtfGlobal.getLocaleText("acc.field.BalanceafterpreviousRe-evalueationinBaseCurrency"),
                dataIndex: 'preamount',
                hidden:(this.accTypeId==1 ||this.accTypeId==2),  //[1,'Accounts Payable'],[2,'Accounts Recivable']
                pdfwidth: 250,
                align: 'right',
                renderer: WtfGlobal.currencyDeletedRenderer,
                pdfrenderer: "currency"
            }, {
                header: WtfGlobal.getLocaleText("acc.field.Profit/LossafterRe-evalueationinBaseCurrency"),
                dataIndex: 'profitloss',
                pdfwidth: 250,
                renderer: WtfGlobal.currencyDeletedRenderer,
                pdfrenderer: "currency"
            }]);
        this.gridcm.defaultSortable = false;
    },
    showBalanceSummary: function() {
        return WtfGlobal.currencySummaryRenderer(this.BalanceSummary);
    },
    showEndingBalanceSummary: function() {
        return WtfGlobal.currencySummaryRenderer(this.endingBalanceSummary);
    },
    getUpdatedDetailsForRevalueate: function() {
        var arr = [];
        this.store.clearFilter();
        for (var i = 0; i < this.store.getCount(); i++) {
            /**
             * Commenting this check because we need to post Unrealised 
             * JE in case, even the summation of all the documents of 
             * Vendor or Customer is Zero. ERP-41603
             */
//            if (this.store.data.items[i].data.profitloss != 0) {
                arr.push(i);
//            }
        }
        return WtfGlobal.getJSONArray(this.grid, true, arr);
    },
    saveData: function() {
        var rec = [];
        rec.mode = 202;
        rec.accTypeId = this.accTypeId;
        rec.currencyId = this.revalueCurrenyId;
        rec.customfield =this.customfield;
        rec.lineleveldimensions =this.lineleveldimensions;
//        rec.entrydate =WtfGlobal.onlyDateRendererForGridHeader(new Date());
        rec.enddate = WtfGlobal.convertToGenericDate(this.endDate);
        rec.entrydate = WtfGlobal.convertToGenericDate(this.endDate);
        rec.data = this.getUpdatedDetailsForRevalueate();

        if (rec.data == "[]")
        {
            this.close();
            return;
        }
        this.okBtn.disable();
        this.closeBtn.disable();
        Wtf.Ajax.requestEx({
            url: "ACCRevalReports/saveRevaluationJournalEntry.do",
            params: rec
        }, this, this.genSuccessResponse, this.genFailureResponseforjepost);
        
    },
    genSuccessResponse: function(response) {
        var msg = "";
        if (response.msg)
            msg = response.msg;
        this.closeBtn.enable();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Success"), msg], 4);
        this.close();
    },
    genFailureResponseforjepost: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        this.closeBtn.enable();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 1);
        this.close();
    },
    showAdjustment: function(v, m, rec) {
        var amt = rec.get('perioddepreciation') - rec.get('firstperiodamt');
        if (Math.abs(amt) >= 0.001) {
            v = WtfGlobal.currencyRenderer(amt);
        }

        v = ""
        return WtfGlobal.deletedRenderer(v, m, rec)
    },
    createGrid: function() {
//        this.quickSearchTF = new Wtf.KWLTagSearch({
//            emptyText: this.isFixedAsset ? WtfGlobal.getLocaleText("acc.rem.8") : WtfGlobal.getLocaleText("acc.coa.accountSearchText"), //'Search by Name',
//            width: 130,
//            field: 'accname',
//            Store: this.store
//        });
//        this.resetBttn = new Wtf.Toolbar.Button({
//            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
//            hidden: this.isSummary,
//            tooltip: WtfGlobal.getLocaleText("acc.coa.resetTT"), //'Allows you to add a new search account name by clearing existing search account names.',
//            id: 'btnRec' + this.id,
//            scope: this,
//            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
//            disabled: false
//        });
//        this.resetBttn.on('click', this.handleResetClick, this);
        this.grid = new Wtf.grid.GridPanel({
            layout: 'fit',
            store: this.store,
            cm: this.gridcm,
            height: 540,
            sm: this.selectionModel,
            hirarchyColNumber: 2,
            plugins: [this.summary, this.expander],
            autoScroll: true,
            border: false,
            //loadMask : true,
            viewConfig: {
                forceFit: true,
                emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
                }
        });
        this.grid.on("render", function(grid) {
            WtfGlobal.autoApplyHeaderQtip(grid);
        }, this);
        this.pageLimit = new Wtf.forumpPageSize({
            ftree: this.grid
        });
    },
    handleClose: function() {

        var rec = [];        
        var revalId = "";
        this.store.clearFilter();
        for (var i = 0; i < this.store.getCount(); i++) {
            rec.revalId = this.store.data.items[i].data.revalid;
            rec.oldRevalId = this.store.data.items[i].data.oldRevalId;  //Passing old reval id because if user clicks cancel we need to restore the isrealised flag of old reval records.
            break;
        }

        Wtf.Ajax.requestEx({
            url: "ACCJournal/deleteRevalEntry.do",
            params: rec
        }, this, function() {


        }, function() {


        });
        this.fireEvent('cancel', this)
        this.close();
    },
    handleSend: function(bobj, edfd) {


    },
    createExpanderStore : function(){
        this.expandRec = Wtf.data.Record.create([
        {name:'journalentryid'},
        {name:'entryno'},
        {name:'companyid'},
        {name:'companyname'},
        {name:'currencysymbol'},
        {name:'currencyid'},
        {name:'billno'},
        {name:'date', type:'date'},
        {name:'duedate', type:'date'},
        {name:'amount'},
        {name:'amountdue'},
        {name:'amountinbase'},
        {name:'revalamount'},
        {name:'subprofitloss'},
        {name:'type'}, 
        {name:'revalrate'}, 
        {name:'currentrate'}, 
        {name:'revalcurrencycode'}, 
        {name:'documentamount'}, 
        {name:'externalcurrencyrate'},
        {name:'ispercentdiscount'},
        {name:'startDate', type:'date'},
        {name:'nextDate', type:'date'},
        {name:'expireDate', type:'date'},
        {name:'amountwithouttaxinbase'},   
        ]);
        
        
       
        this.expandStore = new Wtf.data.Store({
        remoteSort: true,
//            url: Wtf.req.account +'CompanyManager.jsp',
            url: "ACCRevalReports/getAccountsForRevaluation.do",
            baseParams:{
                        reevalueData : this.reevalueData,
                        accTypeId:this.accTypeId,
                        startDate:this.startDate,
                        endDate :this.endDate,
                        expanderrequest:true,
                        ignore : true
        
                       
            },
            reader: new Wtf.data.KwlJsonReader({
            totalProperty: 'totalCount',
            root: "data"
            },this.expandRec)  
        });
        this.expandStore.on('beforeload',function(){
            WtfGlobal.setAjaxTimeOut();
        },this);
        this.expandStore.on('loadexception',function(proxy, options, response){
            WtfGlobal.resetAjaxTimeOut();
        },this);
        this.expandStore.on('load',this.fillExpanderBody,this);
    },
    
    onRowexpand:function(scope, record, body){
        this.expanderBody=body;
        this.expandStore.load({
        
            params:{
                accountid:record.data.accid,
                enddate:WtfGlobal.convertToGenericDate(this.endDate)                                
            }
       });
    },
    
    fillExpanderBody: function(){
        WtfGlobal.resetAjaxTimeOut();
        var disHtml = "";
        var arr=[];
        var profitLoss = 0, totalDebitAmount=0, totalRevalAmountAmount = 0;//'Amount'
        arr = ['Transaction Id','Date', 'Journal Entry','Type','Currency','Current Rate','Revaluation Rate','Amount','Profit/loss'];  //'Amount in Base Before','Amount in Base After',
        var gridHeaderText = 'Transactions';
        var header = "<span class='gridHeader'>"+gridHeaderText+"</span>";
//        header += "<span class='gridNo' style='font-weight:bold;'>"+WtfGlobal.getLocaleText("acc.cnList.Sno")+"</span>";
        /**
         * Allignment for Invoice ID and Journal Entry No. 
         */
        for(var i=0;i<arr.length;i++){
            var width = '11%';
            if(arr[i] == 'Transaction Id') {
                width = '14%';
            } else if (arr[i] == 'Type') {
                width = '6%';
            } else if (arr[i] == 'Journal Entry') {
                width = '14%';
            } else if (arr[i] == 'Currency') {
                width = '7%';
            } else if (arr[i] == 'Date') {
                width = '14%';
            }

            header += "<span class='headerRow' style='width:" + width + " ! important;'>" + arr[i] + "</span>";
        }
//        header += "<span class='gridLine'></span>";
        header += "<div style='width: 95%'><span class='gridLine'></span></div>";
        for(i=0;i<this.expandStore.getCount();i++){
            var rec=this.expandStore.getAt(i);
            var revalCurrencyCode="";
             if(rec.data['revalcurrencycode'] != ''){
                revalCurrencyCode=rec.data['revalcurrencycode'];
            }
            //Column : S.No.
//            header += "<span class='gridNo'>"+(i+1)+".</span>";
             
             //Column :Invoice No
             header += "<span class='gridRow'  style='width: 14% !important;'  wtf:qtip='"+rec.data['billno']+"'>"+Wtf.util.Format.ellipsis(rec.data['billno'],15)+"&nbsp;</span>";
                 
            //Column : Date
            if(rec.data['date'] != ''){
                header += "<span class='gridRow' style='width: 14% !important;'  wtf:qtip='"+rec.data['date'].format(WtfGlobal.getOnlyDateFormat())+"'>"+Wtf.util.Format.ellipsis(rec.data['date'].format(WtfGlobal.getOnlyDateFormat()),15)+"&nbsp;</span>";
            }
            
            //Column : Journal entry
                header += "<span class='gridRow' style='white-space:nowrap; overflow: hidden; text-overflow: ellipsis; width: 14% ! important;'>"+rec.data['entryno']+"&nbsp;</span>";
            
            //Column : Type
                header += "<span class='gridRow' style='width: 6% ! important;'>"+rec.data['type']+"&nbsp;</span>";
            //Column : Amount
            
//            if(rec.data['amount'] != ''){
//                header += "<span class='gridRow' style='width: 14% ! important;'>"+WtfGlobal.getCurrencySymbol()+' '+(Math.round(rec.data['amount']*100)/100)+"&nbsp;</span>";
//                totalDebitAmount+=rec.data['amount'];
//            }
//            if(rec.data['revalamount'] != ''){
//                header += "<span class='gridRow' style='width: 14% ! important;'>"+WtfGlobal.getCurrencySymbol()+' '+(Math.round(rec.data['revalamount']*100)/100)+"&nbsp;</span>";
//                totalRevalAmountAmount+=rec.data['revalamount'];
//            }
        
         //Column : Curreny code
                header += "<span class='gridRow' style='width: 7% ! important;'>"+rec.data['revalcurrencycode']+"&nbsp;</span>";
            
         //Column : Current Rate
                header += "<span class='gridRow' style='width: 11% ! important;' wtf:qtip='"+rec.data['currentrate']+"'>"+(Math.round(rec.data['currentrate']*100)/100)+"&nbsp;</span>";
            
            //Column : Revaluation Rate
                header += "<span class='gridRow' style='width: 11% ! important;' wtf:qtip='"+rec.data['revalrate']+"'>"+(Math.round(rec.data['revalrate']*100)/100)+"&nbsp;</span>";
        
            //Column : document amount
            if(rec.data['documentamount'] != ''){
                header += "<span class='gridRow' style='width: 11% ! important;'>"+revalCurrencyCode+' '+(Math.round(rec.data['documentamount']*100)/100)+"&nbsp;</span>";  //+WtfGlobal.getCurrencySymbol()+' '
//                totalRevalAmountAmount+=rec.data['revalamount'];
            }
            
            if(rec.data['subprofitloss'] != undefined){
                 profitLoss=profitLoss+Number(rec.data['subprofitloss']);
                header += "<span class='gridRow' style='width: 11% ! important;'>"+WtfGlobal.conventInDecimal(rec.data['subprofitloss'],WtfGlobal.getCurrencySymbol())+"&nbsp;</span>";
            }
    
            header +="<br>";
                
        }
//        header += "<span class='gridLineBottom'></span>";
        header += "<div style='width: 95%'><span class='gridLine'></span></div>";
        header += "<span class='headerRow' style='width: 83% ! important;'>"+ "Total"+"</span>"; //old valur 48%
//        header += "<span class='headerRow' style='width: 16% ! important;'>"+ WtfGlobal.addCurrencySymbolOnly(totalDebitAmount,WtfGlobal.getCurrencySymbol(),[true])+"</span>";
//        header += "<span class='headerRow' style='width: 16% ! important;'>"+ WtfGlobal.addCurrencySymbolOnly(totalRevalAmountAmount,WtfGlobal.getCurrencySymbol(),[true])+"</span>";
        header += "<span class='headerRow' style='width: 14% ! important;'>"+ WtfGlobal.addCurrencySymbolOnly(profitLoss,WtfGlobal.getCurrencySymbol(),[true])+"</span>";
        disHtml += "<div class='expanderContainer' style='width:100%'>" + header + "</div>";
        this.expanderBody.innerHTML = disHtml;
    }
});




/**********************************************************************************************************
 *                Dimension Selection Window
 **********************************************************************************************************/
Wtf.account.SetDimensionTypeWin = function(config){
    
    this.butnArr = new Array();
    this.butnArr.push({
        text: WtfGlobal.getLocaleText("acc.setupWizard.next"),   //'Next',
        scope: this,
        handler: this.saveForm
    },{
        text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
        scope: this,
        handler: function() {
                 this.fireEvent('cancel',this);
                 var panel = this.parentId;
                 this.close();
                 if(panel!=null){
                         Wtf.getCmp('as').remove(panel);
                         panel=null;                 
                    }
            
                }   
    });

    Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.dimention.type"),
        buttons: this.butnArr
    },config);
    Wtf.account.SetDimensionTypeWin.superclass.constructor.call(this, config);
     this.addEvents({
        'update':true
//        'cancel':true
    });
}

Wtf.extend(Wtf.account.SetDimensionTypeWin, Wtf.Window, {
    
    draggable:false,
    onRender: function(config){
        Wtf.account.SetDimensionTypeWin.superclass.onRender.call(this, config);
        this.createForm();
       var title=WtfGlobal.getLocaleText("acc.dimention.type");// Dimension's Mapping Type
       var msg=WtfGlobal.getLocaleText("acc.dimention.selectType"); // Select Dimension's Mapping Type
       var isgrid=(this.isAccPref ?true:false);
        this.add(
        {
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(title,msg,"../../images/accounting_image/price-list.gif",isgrid)
        },
        {
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.TypeForm
        });
    },


    createForm:function(){
        
        this.mapped= new Wtf.form.Checkbox({
            boxLabel:" ",
            width: 50,
            inputType:'radio',
            inputValue:1,
            disabled:true,
            name:'selectedOption',            
            fieldLabel:WtfGlobal.getLocaleText("acc.dimentionoption.mapped")
        })
        
        this.selected= new Wtf.form.Checkbox({
            boxLabel: " ",
            inputType: 'radio',
            name: 'selectedOption',
            inputValue: 2,
            width: 50,
            fieldLabel: WtfGlobal.getLocaleText("acc.dimentionoption.selected")
        })
        
        this.none = new Wtf.form.Checkbox({
            boxLabel: " ",
            inputType: 'radio',
            name: 'selectedOption',
            inputValue: 3,
            checked:true,
            width: 50,
            fieldLabel:WtfGlobal.getLocaleText("acc.dimentionoption.none") 
        })
        
       this.TypeForm=new Wtf.form.FormPanel({
            region:'center',
            autoScroll:true,
            border:false,
            labelWidth:245,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
            items:[this.mapped,this.selected,this.none]
       });
   },

   saveForm:function(value){  
        this.value = this.none.getValue()? 3:( this.selected.getValue() ? 2 : (this.mapped.getValue() ? 1 : 3));

        this.close();
        if(this.value==2){
            new Wtf.account.journalEnteryDimensionWindow({
                closable: false,
                modal: true,
                id: 'setDimensionWindow',
                iconCls: getButtonIconCls(Wtf.etype.deskera),
                width: 950,
                height: 450,
                reevalueData:this.reevalueData,
               currencodeforReval:this.currencodeforReval,
                revalueCurrenyId: this.revalueCurrenyId,
                accTypeId: this.accTypeId,
                startDate: this.startDate,
                endDate: this.endDate,
                resizable: false,
                layout: 'border',
                buttonAlign: 'right'
            }).show();
            this.fireEvent('cancel',this)
            this.close();
        } else if(this.value==3){     
               new Wtf.account.revalueateGrid({
                closable: false,
                modal: true,
                id: 'accountRevalueGridwin',
                iconCls: getButtonIconCls(Wtf.etype.deskera),
                width: 1024,
                height: 700,
                reevalueData:this.reevalueData,
                currencodeforReval:this.currencodeforReval,
                revalueCurrenyId: this.revalueCurrenyId,
                accTypeId: this.accTypeId,
                startDate: this.startDate,
                endDate: this.endDate,
                resizable: false,
                layout: 'border',
                buttonAlign: 'right'
            }).show();
            
        }
    },
     closeWin:function(){this.fireEvent('update',this,this.value);this.close();}
}); 


/******************************************************************************************************
 *           Set Dimension to Revaluation JE 
 *****************************************************************************************************/
Wtf.account.journalEnteryDimensionWindow = function(config){
    var btnArr=[];
    this.moduleName = "Set Dimension Window"; //Wtf.Currency_Exchange
    this.moduleid=Wtf.Acc_GENERAL_LEDGER_ModuleId;
        btnArr.push(this.save=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.setupWizard.next"),  //'Next'
            scope: this,
            handler: this.saveData.createDelegate(this)
        }))
      btnArr.push(this.cancel=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.close"),//this.currencyhistory?:WtfGlobal.getLocaleText("acc.common.cancelBtn")
            scope: this,
            handler:this.closeWin.createDelegate(this)
        }))
    Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.revalue.dimension.title"), 
        buttons: btnArr
    },config);
    Wtf.account.journalEnteryDimensionWindow.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}
Wtf.extend( Wtf.account.journalEnteryDimensionWindow, Wtf.Window, {
    defaultCurreny:false,
    draggable:false,
    onRender: function(config){
        Wtf.account.journalEnteryDimensionWindow.superclass.onRender.call(this, config);
          this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            isRevaluationWin:true,
//            compId:this.id,
            autoHeight: true,
            isWindow:true,
            widthVal:90,
            parentcompId:this.id,
            style : 'margin-left:10px;',
//            baseCls:'bodyFormat',
            moduleid: 24,
//            layout:'form',
            disabledClass:"newtripcmbss",
            disabled: false,
            isEdit: false, 
            record: undefined 
        });


        this.add(
        {
            region: 'north',
            height:50,
            border: false,
            baseCls:'bckgroundcolor'
        },{
            region:'center',
            border: false,
            height:260, // :150),
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.tagsFieldset
        });
        
     
    },

    closeWin:function(){
        this.fireEvent('cancel',this)
        this.close();
    },

    setRateRenderer:function(val){
       return  WtfGlobal.conventCurrencyDecimal(val,"")
    },

     
    saveData:function(){
        var rec=[];
        
        var custFieldArr=this.tagsFieldset.createFieldValuesArray();
        if (custFieldArr.length > 0){
            rec.customfield = JSON.stringify(custFieldArr);
        }
        var lineleveldimensions=this.tagsFieldset.createDimensionValuesArrayForLineItem();
        if(lineleveldimensions.length > 0 ){
            rec.lineleveldimensions = JSON.stringify(lineleveldimensions);
        }
        this.close();
        new Wtf.account.revalueateGrid({
            closable: false,
            modal: true,
            id: 'accountRevalueGridwin',
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            width: 1024,
            height: 700,
            customfield:rec.customfield,
            lineleveldimensions:rec.lineleveldimensions,
            reevalueData:this.reevalueData,
            currencodeforReval:this.currencyCode,
            revalueCurrenyId: this.revalueCurrenyId,
            accTypeId: this.accTypeId,
            startDate: this.startDate,
            endDate: this.endDate,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right'
        }).show();
        this.fireEvent('cancel',this)
        this.close();
    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }
 
});

//**************************************************************
//                      Re-evaluation History Report
//**************************************************************
Wtf.account.AccountReevaluationHistory=function(config){
    this.ExportButtonVersion='_v1',
    this.ImportButtonVersion='_v1',
    this.GridRec = Wtf.data.Record.create ([
    {
        name:'billno'
    },{
        name:'evalrate'
    },{
        name:'amount'
    },{
        name:'entrydate',
        type:'date'
    },{
        name:'currencyid'
    },{
        name:'currencyname'
    },{
        name:'currencysymbol'
    }
    ]);
    
    this.startDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
        name:'stdate' + this.id,
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true)
    });
    this.endDate=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate' + this.id,
        value:WtfGlobal.getDates(false)
    });
    this.Store = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"
        },this.GridRec),
        url: "ACCRevalReports/ReevaluationHistoryReport.do",
        baseParams:{
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())

               
        }
        
    });
    this.pagingToolbar = new Wtf.PagingSearchToolbar({
        pageSize: 15,
        id: "pagingtoolbar" + this.id,
        store: this.Store,
        searchField: this.quickPanelSearch,
        displayInfo: true,
        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
        plugins: this.pP = new Wtf.common.pPageSize({
            id : "pPageSize_"+this.id
            })
    });
        
    this.Store.on('datachanged', function() {
        var p = this.pP.combo.value;
        //this.quickPanelSearch.setPage(p);
    }, this);
      
    this.Store.on('beforeload', function(){
        this.Store.baseParams = {
            //ss : this.quickPanelSearch.getValue(),
            startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue())
        }
        
    }, this);
    this.Store.load();
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();

    var gridSummary = new Wtf.ux.grid.GridSummary;
    this.grid = new Wtf.grid.GridPanel({
      //  stripeRows :true,
        store:this.Store,
        sm:this.sm,
        border:false,
        layout:'fit',
        viewConfig:{forceFit:true, emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))},
        forceFit:true,
        plugins: [gridSummary],
        loadMask : true,  
        columns:[this.sm,this.rowNo,{
        header: WtfGlobal.getLocaleText("acc.reval.transaction"),  //"Transaction Name",
        dataIndex: 'billno',
        width:100,
         renderer:WtfGlobal.deletedRenderer,
        pdfwidth:80
    },{ 
        header:WtfGlobal.getLocaleText("acc.reval.evaldate"),  //" Date",
        dataIndex:'entrydate',
        width:100,
        align:'center',
        pdfwidth:80,
        sortable:true,
        renderer:WtfGlobal.onlyDateDeletedRenderer
     },{
        header: WtfGlobal.getLocaleText("acc.reval.evalrate"),  //"Transaction Name",
        dataIndex: 'evalrate',
        renderer:WtfGlobal.deletedRenderer,
        pdfwidth:80,
        width:100
    },{
        header :WtfGlobal.getLocaleText("acc.coa.gridCurrency"), //'Currency',
        renderer:WtfGlobal.deletedRenderer,
        dataIndex: 'currencyname',
        pdfwidth:80
    },{
        header: WtfGlobal.getLocaleText("acc.reval.amount"),  //" Amount",
        align:'right',
        dataIndex:'amount',
        pdfwidth:75,
        width:100
//        renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol   //WtfGlobal.currencyRenderer     //renderer:WtfGlobal.withoutRateCurrencyDeletedSymbol
           
    }]         
    });
    
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClickNew,this);
                
    this.exportButton=new Wtf.exportButton({
        obj:this,
        tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details.',
        id:"exportCostCenterSummary",
        menuItem:{
            csv:true,
            pdf:true,
            rowPdf:false,
            xls:true
        },
        get:Wtf.autoNum.AccountsReEvaluationReport,
        label:WtfGlobal.getLocaleText("acc.ccReport.tab3"),
        filename:WtfGlobal.getLocaleText("acc.reval.history")+this.ExportButtonVersion
    });
    this.printButton=new Wtf.exportButton({
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        obj:this,
        id:"printAccountsReEvaluation",
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details.",   
        menuItem:{
            print:true
        },
        get:Wtf.autoNum.AccountsReEvaluationReport,
        label:WtfGlobal.getLocaleText("acc.reval.history"),
        filename:WtfGlobal.getLocaleText("acc.reval.history")
    });
    Wtf.apply(this,{
        items:[{
            region:'center',
            layout:'fit',
            border:false,
            items:this.grid
        }],
        tbar : [WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"), this.endDate,'-',{   //this.quickPanelSearch,
            text : WtfGlobal.getLocaleText("acc.agedPay.fetch"),
            iconCls:'accountingbase fetch',
            scope : this,
            handler : this.loaddata
        },'-',this.resetBttn,'-',this.exportButton,'-',this.printButton],                                    //,'-',this.exportButton, '-', this.printButton
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.Store,
           // searchField: this.quickPanelSearch,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
                })
        })
    });
    
    Wtf.account.AccountReevaluationHistory.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.AccountReevaluationHistory,Wtf.Panel,{
  
    hideLoading:function(){
        Wtf.MessageBox.hide();
    },
    loaddata : function(){
     
        if (this.Store.baseParams && this.Store.baseParams.searchJson) {
            this.Store.baseParams.searchJson = "";
        }
        this.Store.load({
            params : {
                start:0,
                limit:30
                            
            }
        });
    },
    handleResetClickNew:function(){ 

        //this.quickPanelSearch.reset();
        this.startDate.setValue(WtfGlobal.getDates(true));
        this.endDate.setValue(WtfGlobal.getDates(false));

        this.Store.load({
            params: {
                start:0,
                limit:this.pP.combo.value
            }
        });
       
    }
   
});


Wtf.account.setExchangeRate = function(config) {
    this.okBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.setupWizard.updateRate")  //'Send'
    });
    this.okBtn.on('click', this.handleSend, this);
    this.closeBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.close")  //'Close'
    });
    this.closeBtn.on('click', this.handleClose, this);

    Wtf.apply(this, {
        title: WtfGlobal.getLocaleText("acc.field.SetExchangeRate"), //"Send Mail",
        buttons: [this.okBtn, this.closeBtn]
    }, config);

    Wtf.account.setExchangeRate.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.setExchangeRate, Wtf.Window, {
    getRecord: function() {
        Wtf.Ajax.requestEx({
            url: "ProfileHandler/getAllUserDetails.do",
            params: {
                mode: 11,
                lid: Wtf.userid
            }
        }, this, this.genSuccessResponse, this.genFailureResponse);

    },
    genFailureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 1);
    },
    onRender: function(config) {
        this.createForm();
        this.getRecord();

        this.add({
            region: 'center',
            border: false,
            baseCls: 'bckgroundcolor',
            items: this.Form
        });

        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText("acc.field.SetExchangeRate"), WtfGlobal.getLocaleText("acc.field.SelectDetails"), "../../images/accounting_image/Chart-of-Accounts.gif")
        }, this.centerPanel = new Wtf.Panel({
            region: 'center',
            border: false,
            autoScroll: true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls: 'bckgroundcolor',
//            layout: 'border',
            items: [this.Form]
        }));
        Wtf.account.setExchangeRate.superclass.onRender.call(this, config);
    },
    createForm: function() {

//         this.groupStore = new Wtf.data.SimpleStore({
//            fields: [{name:'groupid',type:'int'}, 'groupname'],
//            data :[[1,'Accounts Payable'],[2,'Accounts Recivable'],[3,'Bank']]
//        });
//        
//        this.accGroup = new Wtf.form.FnComboBox({
//            fieldLabel: WtfGlobal.getLocaleText("acc.rem.18") + '*',
//            hiddenName: 'groupid',
//            name: 'groupid',
//            store: this.groupStore,
//            valueField: 'groupid',
//            displayField: 'groupname',
//            typeAhead: true,
//            forceSelection: true,
//            width: 250,
//            allowBlank: false,
//            mode: 'local',
//            emptyText: WtfGlobal.getLocaleText("acc.reval.accType"),
//            disableKeyFilter: true,
//            lastQuery: '',
//            hirarchical: true,
//            triggerAction: 'all'
//        });
//
//        this.monthStore = new Wtf.data.SimpleStore({
//            fields: [{name:'monthid',type:'int'}, 'name'],
//            data :[[0,'January'],[1,'February'],[2,'March'],[3,'April'],[4,'May'],[5,'June'],[6,'July'],[7,'August'],[8,'September'],[9,'October'],
//                [10,'November'],[11,'December']]
//        });
//
//        var data=[[0,new Date().format('Y')],[1,new Date().format('Y')-1]];
//
//        this.yearStore= new Wtf.data.SimpleStore({
//                fields: [{name:'id',type:'int'}, 'yearid'],
//                data :data
//        });
//
//        this.selectedMonth = new Wtf.form.ComboBox({
//                store: this.monthStore,
//                fieldLabel:WtfGlobal.getLocaleText("acc.accPref.month")+ "*",  //'Month',
//                name:'selectedMonth',
//                displayField:'name',
//                forceSelection: true,
//                 allowBlank: false,
//                //anchor:'95%',
//                 width: 250,
//                emptyText: WtfGlobal.getLocaleText("acc.reval.month"), 
//                valueField:'monthid',
//                mode: 'local',
//                triggerAction: 'all',
//                selectOnFocus:true
//        });  
//
//        this.selectedYear = new Wtf.form.ComboBox({
//                store: this.yearStore,
//                fieldLabel:WtfGlobal.getLocaleText("acc.accPref.year")+ "*",  //'Year', 
//                name:'selectedYear',
//                displayField:'yearid',
//                //anchor:'95%',
//                 allowBlank: false,
//                 width: 250,
//                 emptyText: WtfGlobal.getLocaleText("acc.reval.year"), 
//                valueField:'yearid',
//                forceSelection: true,
//                mode: 'local',
//                triggerAction: 'all',
//                selectOnFocus:true
//        });  
        this.exchangeRate=new Wtf.form.TextField({
        name:"exchangerate",
        fieldLabel:WtfGlobal.getLocaleText("acc.field.ExchangeRate"),
        width: 250,
         allowBlank: false
        });
        this.currencyRec = new Wtf.data.Record.create([
            {name: 'currencyid', mapping: 'tocurrencyid'},
            {name: 'symbol'},
            {name: 'currencyname', mapping: 'tocurrency'},
            {name: 'exchangerate'},
            {name: 'currencycode'},
            {name: 'htmlcode'}
        ]);
        this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: "count"
            }, this.currencyRec),
//        url:Wtf.req.account+'CompanyManager.jsp'
            url: "ACCCurrency/getCurrencyExchange.do"
        });
        this.Currency = new Wtf.form.FnComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.coa.gridCurrency") + "*", //'Currency*',
            hiddenName: 'currencyid',
            width: 250,
            allowBlank: false,
            store: this.currencyStore,
            valueField: 'currencyid',
            emptyText: WtfGlobal.getLocaleText("acc.cust.currencyTT"), //'Please select Currency...',
            forceSelection: true,
            displayField: 'currencyname',
            scope: this,
            selectOnFocus: true

        });
        this.currencyStore.load({params: {mode: 201, transactiondate: WtfGlobal.convertToGenericDate(new Date()), isAll: true}});
        this.Form = new Wtf.form.FormPanel({
            height: 'auto',
            border: false,
            items: [{
                    layout: 'form',
                    bodyStyle: "background: transparent; padding: 20px;",
//                labelWidth:60,
                    border: false,
                    items: [ this.Currency,this.exchangeRate  // this.startdate, this.enddate  this.accGroup,this.selectedMonth,this.selectedYear,
                    ]
                }]
        });
    },
    handleClose: function() {
        this.fireEvent('cancel', this)
        this.close();
    },   
    handleSend: function(bobj, edfd) {
         var valid=this.Form.getForm().isValid();
        if(valid==false){
            WtfComMsgBox(2, 2);
            return;
        }  
        
//        this.okBtn.disable();
//        this.closeBtn.disable();30- 4 6 9 11  31- 1 3 5 7 8 10 12 28,29-2 
//          var month1=this.selectedMonth.getValue();
//          var month=month1+1;
//          var year=this.selectedYear.getValue();
//          var endDate=Wtf.serverDate.clearTime();
//          var startDate=Wtf.serverDate.clearTime();
//          startDate.setDate(1);
//         if(month==2){
//             if(year%4==0 ||year%400==0){
//                 endDate.setDate(29);
//             }else{
//                 endDate.setDate(28);
//             }
//         } else if(month==4 ||month==6||month==9||month==11){
//             endDate.setDate(30);
//         }else{      //(month==1 || month==3|| month==5|| month==7|| month==8|| month==10|| month==12)
//             endDate.setDate(31);
//         }       
//         endDate.setMonth(month1);
//         startDate.setMonth(month1);
//         endDate.setYear(year);
//         startDate.setYear(year);
         
//        if (this.sDate > this.eDate) {
//            WtfComMsgBox(1, 2);
//            return;
//        }
        Wtf.Ajax.requestEx({
                    url: "ACCGoodsReceiptCMN/setExchangeRate.do",
                    params:{
                    exchangeRate:1/this.exchangeRate.getValue(),//changed for applying reverse exchange rate
                    deleted:false,
                    onlyAmountDue:true,
                    nondeleted:true,
                    currencyfilterfortrans:this.Currency.getValue()
           
                    }
                },this,this.success,this.failure);
//                   function(responseObj) {
//                        if (responseObj.success==true && responseObj.count==0) {
//                                if ((this.Form.getForm().isValid())) {
//                                this.handleClose();
//                                panel = new Wtf.account.CurrencyExchangeWindow({
//                    //            id:winid,
//                                    closable: true,
//                                    modal: true,
//                                    iconCls: getButtonIconCls(Wtf.etype.deskera),
//                                    width: 600,
//                                    isRevaluation: true,
//                                    revalueCurrenyId: this.Currency.getValue(),
//                                    accTypeId: this.accGroup.getValue(),
//                                    startDate: startDate,
//                                    endDate: endDate,
//                                    height: 400,
//                                    resizable: false,
//                                    layout: 'border',
//                                    buttonAlign: 'right',
//                                    renderTo: document.body
//                                });
//                                panel.show();
//                            } else {
//                                WtfComMsgBox(2, 2);
//                            }
//
//                        }else{
//                            WtfComMsgBox(56, 2);
//                        }         
//                   }
//      );          

    },
    success: function(response) {
        if (response.success) {
            WtfComMsgBox([this.label, response.msg], 3);
            this.handleClose();
        } else {
            if (response.msg && response.isMsgSizeException) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
            } else {
                WtfComMsgBox([this.label, WtfGlobal.getLocaleText("acc.rem.210")], 3);
            }
            this.sendBtn.enable();
            this.closeBtn.enable();
        }
    },
    failure: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
//        if (response.msg)
//            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        this.handleClose();
    }
   
});


/******************************************************************************************************
 *           Set Dimension to Revaluation JE 
 *****************************************************************************************************/
Wtf.account.journalEnteryCustomDimensionWindow = function(config){
    var btnArr=[];
    this.id="";
    this.moduleName = "Map Dimension/Custom Field Window"; 
    this.moduleid=Wtf.Acc_GENERAL_LEDGER_ModuleId;
        btnArr.push(this.save=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save'
            scope: this,
            handler: this.saveData.createDelegate(this)
        }))
      btnArr.push(this.cancel=new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.close"),//this.currencyhistory?:WtfGlobal.getLocaleText("acc.common.cancelBtn")
            scope: this,
            handler:this.closeWin.createDelegate(this)
        }))
    Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.revalue.dimension.title"), 
        buttons: btnArr
    },config);
    Wtf.account.journalEnteryCustomDimensionWindow.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}
Wtf.extend( Wtf.account.journalEnteryCustomDimensionWindow, Wtf.Window, {
    defaultCurreny:false,
    draggable:false,
    onRender: function(config){
        Wtf.account.journalEnteryCustomDimensionWindow.superclass.onRender.call(this, config);
          this.tagsFieldset = new Wtf.account.CreateCustomFields({
            border: false,
            isRevaluationWin:true,
//            compId:this.id,
            autoHeight: true,
            isWindow:true,
            widthVal:90,
            parentcompId:this.id,
            style : 'margin-left:10px;',
//            baseCls:'bodyFormat',
            moduleid: 24,
//            layout:'form',
            disabledClass:"newtripcmbss",
            disabled: false,
            isEdit: true, 
            record: undefined 
        });


        this.add(
        {
            region: 'north',
            height:50,
            border: false,
            baseCls:'bckgroundcolor'
        },{
            region:'center',
            border: false,
            height:260, // :150),
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:this.tagsFieldset
        });
    },

    closeWin:function(){
        this.fireEvent('cancel',this)
        this.close();
    },

    setRateRenderer:function(val){
       return  WtfGlobal.conventCurrencyDecimal(val,"")
    },

     
    saveData:function(){
        var rec=[];
        
        var custFieldArr=this.tagsFieldset.createFieldValuesArray();
        if (custFieldArr.length > 0){
            rec.customfield = JSON.stringify(custFieldArr);
        }
        var lineleveldimensions=this.tagsFieldset.createDimensionValuesArrayForLineItem();
        if(lineleveldimensions.length > 0 ){
            rec.lineleveldimensions = JSON.stringify(lineleveldimensions);
        }
        rec.id= this.id;
         if (rec.data == "[]")
        {
            this.close();
            return;
        }
        Wtf.Ajax.requestEx({
            url: "ACCRevalReports/saveRevaluationJECustomData.do",
            params:rec
        },this,this.success,this.failure);
    },
    success: function(response) {
        if (response.success) {
            WtfComMsgBox([this.moduleName, response.msg], 3);
            this.closeWin();
        } else {
            if (response.msg && response.isMsgSizeException) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
            } else {
                WtfComMsgBox([this.moduleName, WtfGlobal.getLocaleText("acc.rem.210")], 3);
            }
            this.sendBtn.enable();
            this.closeBtn.enable();
        }
    },
    failure: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
//        if (response.msg)
//            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        this.closeWin();
    },
    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
     getRecord: function() {
        Wtf.Ajax.requestEx({
            url: "ACCRevalReports/getRevaluationJECustomData.do",
            params: {}
        }, this, this.genSuccessResponsepopulateData, this.genFailureResponse);

    },
    genSuccessResponsepopulateData: function (responseObj) {
        if(responseObj!=undefined && responseObj.success){
         this.id=responseObj.data.id;
         if(responseObj.data.lineleveldimensions !=undefined && responseObj.data.lineleveldimensions.length>1){
            var lineleveldimensionsData= eval('(' + responseObj.data.lineleveldimensions + ')');
            for (var itemcnt = 0; itemcnt < this.tagsFieldset.dimensionFieldArray.length; itemcnt++) {
                var fieldId = this.tagsFieldset.dimensionFieldArray[itemcnt].id
                var fieldname = this.tagsFieldset.dimensionFieldArrayValues[itemcnt].fieldname;
                for (var custItem = 0; custItem < lineleveldimensionsData.length; custItem++) {
                    var custFieldLabel = lineleveldimensionsData[custItem].fieldname;
                    var columnData = lineleveldimensionsData[custItem][custFieldLabel];
                    var custValue = lineleveldimensionsData[custItem][columnData];
                    if (fieldname == custFieldLabel) {
                        if (custValue != "" && custValue != undefined) {
                            var record = WtfGlobal.searchRecord(Wtf.getCmp(fieldId).store, custValue, "id");
                            if (record != undefined)
                                var fieldValue = record.data.id;
                            if (fieldValue != "" && fieldValue != undefined) {
                                Wtf.getCmp(fieldId).setValue(fieldValue);
                                var index = Wtf.getCmp(fieldId).store.indexOf(record);
                                if (record != undefined && index != -1){
                                    Wtf.getCmp(fieldId).onSelect(record, index);
                                }
                            } else {
                                Wtf.getCmp(fieldId).setValue("1234");
                            }
                        }
                    }
                }

            }
        }
        
         if(responseObj.data.customfield !=undefined && responseObj.data.customfield.length>1){
            var customfieldData= eval('(' + responseObj.data.customfield + ')');
            for (var itemcnt = 0; itemcnt < this.tagsFieldset.customFieldArray.length; itemcnt++) {
                var CustomfieldId = this.tagsFieldset.customFieldArray[itemcnt].id;
                var xtype = this.tagsFieldset.customFieldArrayValues[itemcnt].fieldtype;
                var fieldname = this.tagsFieldset.customFieldArrayValues[itemcnt].fieldname;
           
                for (var custItem = 0; custItem < customfieldData.length; custItem++) {
                    var custFieldLabel = customfieldData[custItem].fieldname;
                    var columnData = customfieldData[custItem][custFieldLabel];
                    var custValue = customfieldData[custItem][columnData];
                    var fieldData = custValue ; // customfieldData[custItem]["fieldData"];
                    var CustomfieldValue = "";
                    if (fieldname == custFieldLabel) {
                        if ((custValue != "" && custValue != undefined) || (fieldData != "" && fieldData != undefined)) {
                            if (xtype == 4 || xtype == 7) {
                                custValue = custValue.split(",");
                                for (var i = 0; i < custValue.length; i++) {
                                    var temp = custValue[i];
                                    var record = WtfGlobal.searchRecord(Wtf.getCmp(CustomfieldId).store, temp, "id");
                                    if (record != undefined)
                                        CustomfieldValue = CustomfieldValue + record.data.id + ",";
                                }
                                CustomfieldValue = CustomfieldValue.substring(0, CustomfieldValue.length - 1);
                            } else if (xtype == 12) {
                                var checklist = this.tagsFieldset.customFieldArrayValues[itemcnt].checkList
                                if (checklist != undefined && checklist != "") {
                                    checklist = JSON.parse(checklist)
                                    custValue = custValue.split(",");
                                    for (var j = 0; j < checklist.length; j++) {
                                        for (var i = 0; i < custValue.length; i++) {
                                            var temp = custValue[i];
                                            if (temp == checklist[j].id) {
                                                CustomfieldValue = CustomfieldValue + checklist[j].id + ",";
                                            }
                                        }
                                    }
                                }
                                CustomfieldValue = CustomfieldValue.substring(0, CustomfieldValue.length - 1);
                            } else {
                                CustomfieldValue = fieldData
                            }
                            if (CustomfieldValue != "" && CustomfieldValue != undefined) {
                                if (this.tagsFieldset.customFieldArray[itemcnt].getXType() == 'datefield') {
                                    CustomfieldValue = new Date(CustomfieldValue);
                                    Wtf.getCmp(CustomfieldId).setValue(CustomfieldValue);
                                } else if (this.tagsFieldset.customFieldArray[itemcnt].getXType() == 'fieldset') {
                                    var checkBoxId = CustomfieldValue.split(',');
                                    for (var i = 0; i < checkBoxId.length; i++) {
                                        Wtf.getCmp(checkBoxId[i] + "_" + this.tagsFieldset.id).setValue(true);
                                    }
                                } else {
                                    Wtf.getCmp(CustomfieldId).setValue(CustomfieldValue);
                                }        
                            }

                        }

                    }
                }
            }
             for (var itemcnt = 0; itemcnt < this.tagsFieldset.dimensionFieldArray.length; itemcnt++) {
                var fieldId = this.tagsFieldset.dimensionFieldArray[itemcnt].id
                var fieldname = this.tagsFieldset.dimensionFieldArrayValues[itemcnt].fieldname;
                for (var custItem = 0; custItem < customfieldData.length; custItem++) {
                    var custFieldLabel = customfieldData[custItem].fieldname;
                    var columnData = customfieldData[custItem][custFieldLabel];
                    var custValue = customfieldData[custItem][columnData];
                    if (fieldname == custFieldLabel) {
                        if (custValue != "" && custValue != undefined) {
                            var record = WtfGlobal.searchRecord(Wtf.getCmp(fieldId).store, custValue, "id");
                            if (record != undefined)
                                var fieldValue = record.data.id;
                            if (fieldValue != "" && fieldValue != undefined) {
                                Wtf.getCmp(fieldId).setValue(fieldValue);
                                var index = Wtf.getCmp(fieldId).store.indexOf(record);
                                if (record != undefined && index != -1){
                                    Wtf.getCmp(fieldId).onSelect(record, index);
                                }
                            } else {
                                Wtf.getCmp(fieldId).setValue("1234");
                            }
                        }
                    }
                }

            }
        }
    }
    
    }
});
