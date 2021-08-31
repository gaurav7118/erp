
function callCommonReportDynamicLoad(reportid, reportname){
    var panel = Wtf.getCmp('commonReport'+reportid);
      if(reportid===11){        
        if(!panel) {
            panel = new Wtf.TabPanel({
                id:'commonReport'+reportid,
                title:reportname,
                tabTip:reportname,
                repId:reportid,
                closable:true,
                border:false,
                iconCls:'accountingbase vendor',
                activeTab:0
            });
            Wtf.getCmp('as').add(panel);
            callVatAndCstComputationReport(reportid,reportname);
            callOutPutVATAndCSTCalculationReportLoad('commonReport'+reportid);
            callInputVATAndCSTCalculationReportLoad('commonReport'+reportid);  
        }        
    }else{
        if(!panel) {
        panel = new Wtf.Common_Report({
            id:'commonReport'+reportid,
            title:reportname,
            tabTip:reportname,
            repId:reportid
        });
        Wtf.getCmp('as').add(panel);
        }
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
function callVatAndCstComputationReport(reportid,reportname){
    var panel = Wtf.getCmp('VatAndCstComputationReport'+reportid);
    if(!panel) {
        panel = new Wtf.Common_Report({
            id:'VatAndCstComputationReport'+reportid,
            title:reportname,
            tabTip:reportname,
            repId:reportid,
            closable: false
        });
        Wtf.getCmp('commonReport'+reportid).add(panel);
        
    }
    Wtf.getCmp('commonReport'+reportid).setActiveTab(panel);
    Wtf.getCmp('commonReport'+reportid).doLayout();
}
Wtf.Common_Report= function(config){
    Wtf.apply(this,config);
    
    Wtf.Ajax.requestEx({            // request to fetch json of particular report, by ID
        url: "ACCProduct/getReportSchema.do",
        params: {
            reportId: this.repId
        }
    },this,function(resp){
        if(resp!=""){
            this.obj = eval('('+resp.data[0].reportschema+')');
            this.rec = new Wtf.data.Record.create(this.createFieldsForGrouping(this.obj.data));
            
            this.summary = new Wtf.grid.GroupSummary();
            this.GridSummary = new Wtf.ux.grid.GridSummary({});
            
            this.startDate = new Wtf.ExDateFieldQtip({
                fieldLabel: WtfGlobal.getLocaleText("acc.common.from"), //'From',
                name: 'stdate' + this.id,
                format: WtfGlobal.getOnlyDateFormat(),
                value: WtfGlobal.getDates(true)
            });
            this.startDate.on('change',function(obj){
                if (this.startDate.getValue() > this.endDate.getValue()) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 2); // "From Date can not be greater than To Date."
                    this.startDate.reset();
                }
            },this);
            
            this.endDate = new Wtf.ExDateFieldQtip({
                fieldLabel: WtfGlobal.getLocaleText("acc.common.to"), //'To',
                format: WtfGlobal.getOnlyDateFormat(),
                name: 'enddate' + this.id,
                value: WtfGlobal.getDates(false)
            });
            
            this.endDate.on('change',function(obj){
                if (this.startDate.getValue() > this.endDate.getValue()) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 2); // "From Date can not be greater than To Date."
                    this.endDate.reset();
                }
            },this);
            //Calculation of OverDueMonths & TDS Interest is done w.r.t. AsOfDate.
            this.asOfDate = new Wtf.ExDateFieldQtip({
                name: 'asofdate' + this.id,
                format: WtfGlobal.getOnlyDateFormat(),
                anchor: '80%',
                value: new Date().clearTime(true)
            });
            
            this.mastertypeST= this.repId=='11'? new Wtf.data.GroupingStore({
                url:"ACCCombineReports/getIndiaComplianceReportData.do",
                reader: new Wtf.data.KwlJsonReader({//JsonReader
                    root: "data",
                    totalProperty: "count"
                },this.rec),
                groupField:"ordersequence",
                baseParams: {
                    reportid: this.repId
                },
                sortInfo: {
                    field: 'ordersequence',
                    direction: "ASC"
                }
            }) :
            new Wtf.data.JsonStore({
                url:"ACCCombineReports/getIndiaComplianceReportData.do",
                root: "data",
                reader: new Wtf.data.KwlJsonReader({//JsonReader
                    root: "data"
                //                        totalProperty: 'count'
                }),
                baseParams: {
                    reportid: this.repId
                },
                fields:this.createFields(this.obj.data)
            });
                
            this.mastertypeSM=new Wtf.grid.RowSelectionModel({
                singleSelect:true
            });
                
            this.submitBttn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.fetch"),
                tooltip: WtfGlobal.getLocaleText("acc.invReport.fetchTT"),
                id: 'submitRec' + this.id,
                scope: this,
                iconCls: 'accountingbase fetch',
                disabled: false
            });
                
            this.autoNum = "";
            switch(this.repId){
                case Wtf.UnkownDeducteeTypeReportID:
                    this.autoNum = Wtf.autoNum.unknownDeducteeTypeReport;
                    break;
                case Wtf.PANNotAvailableReportID:
                    this.autoNum = Wtf.autoNum.PANNotAvailableReport;
                    break;
                case Wtf.VATComputationReportID:
                    this.autoNum = Wtf.autoNum.VATComputationReport;
                    break; 
                case Wtf.NatureOfPaymentWiseReportID:
                    this.autoNum = Wtf.autoNum.NatureOfPaymentWiseReport;
                    break; 
            }
            this.expButton = new Wtf.exportButton({
                obj: this,
                tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details.',
                filename: this.title,
                menuItem: {
                    csv: false,
                    pdf: false,
                    rowPdf: false,
                    xls: true
                },
                params: {
                    startdate: WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                    enddate: WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                    asOfDate: (this.repId ==  Wtf.NatureOfPaymentWiseReportID ) ? WtfGlobal.convertToGenericDate(this.asOfDate.getValue()): ""
                },
                get: this.autoNum,
                label: WtfGlobal.getLocaleText("acc.ccReport.tab3")
            });
            this.submitBttn.on("click", this.submitHandler, this);
            
            this.tbar2 = new Array();
            this.tbar2.push(WtfGlobal.getLocaleText("acc.common.from"));
            this.tbar2.push(this.startDate);
            this.tbar2.push(WtfGlobal.getLocaleText("acc.common.to"));
            this.tbar2.push(this.endDate);
            this.tbar2.push("-");
            
           this.tbar3 = new Array();
           if(this.repId ==  Wtf.NatureOfPaymentWiseReportID){
                /* Vendor Combo */
                this.personRec = new Wtf.data.Record.create([
                {
                    name: 'accid'
                }, {
                    name: 'accname'
                }, {
                    name: 'acccode'
                },{
                    name: 'taxId'
                }
                ]);
                
                this.vendorAccStore = new Wtf.data.Store({
                    url: "ACCVendor/getVendorsForCombo.do",
                    baseParams: {
                        mode: 2,
                        group: 13,
                        deleted: false,
                        nondeleted: true,
                        common: '1'
                    },
                    reader: new Wtf.data.KwlJsonReader({
                        root: "data",
                        autoLoad: false
                    }, this.personRec)
                });
                this.vendorAccStore.load();
                this.vendorCMB = new Wtf.form.ExtFnComboBox({
                    fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.ven"),
                    hiddenName: "vendor",
                    id: "vendor" + this.id,
                    store: this.vendorAccStore,
                    valueField: 'accid',
                    displayField: 'accname',
                    extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
                    listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:240,
                    allowBlank: true,
                    hirarchical: true,
                    emptyText: WtfGlobal.getLocaleText("acc.msgbox.19"),
                    mode: 'local',
                    typeAheadDelay:30000,
                    extraComparisionField:'acccode',// type ahead search on acccode as well.
                    minChars:1,
                    typeAhead: true,
                    forceSelection: true,
                    selectOnFocus: true,
                    anchor: "50%",
                    triggerAction: 'all',
                    scope: this,
                    width:150
                });
                
                var natureofPaymentRec=new Wtf.data.Record.create([
                {
                    name: 'id'
                },

                {
                    name: 'name'
                },

                {
                    name: 'salespersoncode'
                },
                ]);
                this.natureofPaymentStore=new Wtf.data.Store({
                    reader: new Wtf.data.KwlJsonReader({
                        root: "data"
                    },natureofPaymentRec),
                    url : "ACCMaster/getMasterItems.do",
                    baseParams:{
                        groupid:33,
                        mode:112,
                        moduleIds:""
                    },
                    sortInfo:{
                        field:'salespersoncode',
                        direction:'ASC'
                    }
                });
                this.natureofPaymentStore.load();
                this.natureOfPayment = new Wtf.form.FnRefreshBtn({                    
                    name: 'natureofpayment',
                    store: this.natureofPaymentStore,
                    valueField:'id',
                    displayField:'name',
                    mode: 'local',
                    allowBlank:true,
                    emptyText:WtfGlobal.getLocaleText("acc.nature.of.payment.report.select"),
                    typeAhead: true,                  
                    triggerAction:'all',
                    width:150,
                    listWidth :400,
                    typeAhead:true,
                    scope:this
                });
                
                /* Deductee Type  */
                
                this.typeOfdeducteeTypeStore = new Wtf.data.SimpleStore({
                    fields : ['id', 'name'],
                    data: [
                    ['1','Corporate'],['2','Non-Corporate']
                    ]
                });
                this.typeOfdeducteeTypeCombo = new Wtf.form.FnRefreshBtn({
                    name: "typeofdeducteetype",
                    hiddenName:"typeofdeducteetype",
                    store: this.typeOfdeducteeTypeStore,
                    valueField: 'id',
                    displayField: 'name',
                    mode: 'local',
                    triggerAction: 'all',
                    typeAhead:true,
                    width:120,
                    emptyText: WtfGlobal.getLocaleText('acc.field.activatedeactivatesalesperson.columnheader.selecttype'),//'Select a type',
                    forceSelection: true
                });
                this.tbar2.push(WtfGlobal.getLocaleText("acc.invoiceList.ven"));
                this.tbar2.push(this.vendorCMB);
                this.tbar2.push("-");
                this.tbar2.push(WtfGlobal.getLocaleText("acc.nature.of.payment.report.natureOfPayment"));
                this.tbar2.push(this.natureOfPayment);
                this.tbar2.push("-");
                this.tbar2.push(WtfGlobal.getLocaleText("acc.field.DeducteeCode"));
                this.tbar2.push(this.typeOfdeducteeTypeCombo);
                this.tbar2.push("-");
                this.tbar2.push(WtfGlobal.getLocaleText("acc.CommonReport.asOfDate"));
                this.tbar2.push(this.asOfDate);
                this.tbar2.push("-");
            }
            this.resetBttn = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
                tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
                id: 'btnRec' + this.id,
                scope: this,
                iconCls: getButtonIconCls(Wtf.etype.resetbutton),
                disabled: false
            });
            this.resetBttn.on('click', this.handleResetClick, this);
            this.tbar3.push(this.submitBttn);
            this.tbar3.push("-");
            this.tbar3.push(this.resetBttn);
            this.tbar3.push("-");
            //Export button is available for Unknown Deductee Type Report & PAN Not Aavilable Report.
            if(!Wtf.isEmpty(this.repId) && (this.repId == Wtf.UnkownDeducteeTypeReportID || this.repId == Wtf.PANNotAvailableReportID || this.repId == Wtf.VATComputationReportID || this.repId == Wtf.NatureOfPaymentWiseReportID)){
                this.tbar3.push("-");
                this.tbar3.push(this.expButton);
            }
                
            if(this.repId == '11'){
                this.vatPaymentBttn = new Wtf.Toolbar.Button({
                    text: "VAT Payment",
                    tooltip: "VAT Payment",
                    id: 'vatpayment' + this.id,
                    scope: this,
    //                iconCls: 'accountingbase fetch',
                    disabled: false
                });
                
                this.cstPaymentBttn = new Wtf.Toolbar.Button({
                    text: "CST Payment",
                    tooltip: "CST Payment",
                    id: 'cstpayment' + this.id,
                    scope: this,
    //                iconCls: 'accountingbase fetch',
                    disabled: false
                });

                this.vatPaymentBttn.on("click", function(){
                    this.vatOrCSTPaymentHandler(true); /// true - VAT Payment
                }, this);
                this.cstPaymentBttn.on("click", function(){
                    this.vatOrCSTPaymentHandler(false); /// false - CST Payment
                }, this);

                this.tbar2.push("-");
                this.tbar2.push(this.vatPaymentBttn);
                this.tbar2.push(this.cstPaymentBttn);
            }
            if(this.repId ==  Wtf.NatureOfPaymentWiseReportID){
                this.TDSPaymentBttn = new Wtf.Toolbar.Button({
                    text:WtfGlobal.getLocaleText("acc.TDScomputationreport.TDSPayment"),
                    tooltip: WtfGlobal.getLocaleText("acc.TDScomputationreport.TDSPayment"),
                    id: 'TdsPayment' + this.id,
                    scope: this,
                    handler: this.TDSPaymentHandler
                });
                this.tbar3.push(this.TDSPaymentBttn);
            }
            
            this.groupingView = new Wtf.grid.GroupingView({
                forceFit: true,
                showGroupName: false,
                enableGroupingMenu: false,
                emptyText: '<div class="grid-link-text" >'+WtfGlobal.getLocaleText("acc.common.norec")+'</div>',
                hideGroupedColumn: true
            });
             
            this.topToolbar = new Wtf.Panel({
                tbar: this.tbar2,
                bbar: this.tbar3,
                autoWidth:true,
                autoHeight:true,
                border : false
            });
            if (this.repId != Wtf.NatureOfPaymentWiseReportID) {
                this.tbar2.push.apply(this.tbar2, this.tbar3);
            }
   
            this.grid=new Wtf.grid.GridPanel({
                cm: new Wtf.grid.ColumnModel(this.createColModel(this.obj.data)),
                sm:this.mastertypeSM,
                store:this.mastertypeST,
                tbar: this.repId == Wtf.NatureOfPaymentWiseReportID ? this.topToolbar : this.tbar2,
                autoHeight : true,
                border : false,
                plugins:( this.repId=='11')?this.summary:(this.repId == Wtf.NatureOfPaymentWiseReportID)?this.GridSummary:"",
                view: this.repId=='11'?this.groupingView:"",
                viewConfig:{
                    emptyText:'<div class="grid-link-text" >'+ WtfGlobal.getLocaleText("acc.common.norec") + '</div>',
                    autoDestroy:true,
                    autoFill:true
                }
            });
            this.mastertypeST.load({
                params:{
                    startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                    enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                    asOfDate: (this.repId ==  Wtf.NatureOfPaymentWiseReportID ) ? WtfGlobal.convertToGenericDate(this.asOfDate.getValue()): ""
                }
            });
                
            this.add(this.grid);
            this.doLayout();
            this.grid.on('cellclick',this.onCellClick, this);
        }else{
            msgBoxShow(["Failure","Error while loading Report"], 1);
        }
    }, function(resp){
        msgBoxShow(["Failure","Error while loading Report"], 1);
    })
    
    Wtf.Common_Report.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.Common_Report,Wtf.account.ClosablePanel,{
    closable : true,
    title:this.title,
    autoScroll:true,
    tabTip:this.tabTip,
    onRender:function(config){
        Wtf.Common_Report.superclass.onRender.call(this, config);
    },
    
    createColModel:function(data){
        var colConfig = [];
        if((this.repId == Wtf.UnkownDeducteeTypeReportID || this.repId == Wtf.PANNotAvailableReportID || this.repId ==  Wtf.NatureOfPaymentWiseReportID)) {
            colConfig.push(new Wtf.grid.RowNumberer({
                width: 30
            }));
        }
        for(var columncnt=0; columncnt<data.length ;columncnt++) {
            var colObj = {};
            colObj['header'] = '<div wtf:qtip="'+data[columncnt].columnheader+'">'+data[columncnt].columnheader+'</div>';
            colObj['dataIndex'] = data[columncnt].dataindex;
            colObj['width'] = 80;
            colObj['align'] = "center";
            colObj['sortable'] = false;
            colObj['pdfwidth'] = 75;// To make available columns in Export Window
            if(data[columncnt].hidden){
                colObj['hidden'] = true;
            }
            if(data[columncnt].dataindex != "taxtype" && data[columncnt].dataindex != "ordersequence"){
                colObj['renderer'] = function(val) {
                    return '<div wtf:qtip="'+unescape(val)+'">'+unescape(val)+'</div>';
                }
            };
            if(data[columncnt].dataindex == "ordersequence"){
                colObj['renderer'] = function(val, metadata, record) {
//                    return '<div wtf:qtip="'+unescape(record.data.taxtype)+'">'+unescape(record.data.taxtype)+'</div>';
                    return record.data.taxtype;
                }
            };
            if(data[columncnt].dataindex == "particulars"){
                colObj['renderer'] = function(val, metadata, record) {
//                    if(val=="VAT Payable" || val=="CST Payable"){
                    if(record.data.ordersequence == 3 || record.data.ordersequence == 6){
                        return '<div style="color:red">'+WtfGlobal.getLocaleText("acc.common.total")+" "+val+'</div>'
                    }else{
                        return val;
                    }
                }
            };
            
            if(data[columncnt].dataindex == "taxamount" || data[columncnt].dataindex == "assessablevalue"){
                colObj['align'] = "right";
                colObj['summaryType'] = ['sum'];
                colObj['summaryRenderer'] = function(value, m, rec) {
                    return WtfGlobal.currencySummaryRenderer(value, m, rec);
                }
                colObj['renderer'] = function(value, m, rec) {
                    return WtfGlobal.currencyRenderer(value);
                }
            };
            if(                data[columncnt].dataindex == "tdsAmount" || data[columncnt].dataindex == "unpaidTdsAmount" || data[columncnt].dataindex =="amountPaid" 
                || data[columncnt].dataindex == "tdsInterestAmount" || data[columncnt].dataindex == "unpaidTdsInterestAmount" ||
                data[columncnt].dataindex == "tdsAndInterestAmount" || data[columncnt].dataindex == "unpaidTdsAndUnpaidInterestAmount"  
             ){
                colObj['fixed'] = true;
                colObj['width'] = 100;
                colObj['summaryType'] = ['sum'];
                colObj['summaryRenderer'] = function(value, m, rec) {
                    return '<div class="grid-summary-common"; align="right";>'+WtfGlobal.currencySummaryRenderer(value, m, rec)+'</div>';
                }
                colObj['renderer'] = function(value, m, rec) {
                    return WtfGlobal.currencyRenderer(value);
                }
            }
            if(data[columncnt].dataindex == "vendorcode"){
                colObj['renderer'] = function(val, metadata, record) {
                    return WtfGlobal.linkDeletedRenderer(val,metadata,record);
                }
            };
            colConfig[colConfig.length] = colObj;
        }
        return colConfig;
    },
    TDSPaymentHandler: function () {
        if (this.natureOfPayment && this.typeOfdeducteeTypeCombo) {
            if (this.startDate.getValue() == undefined || this.startDate.getValue() == '') {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.124")], 2);
                return false;
            } else if (this.endDate.getValue() == undefined || this.endDate.getValue() == '') {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.125")], 2);
                return false;
            } else if ((this.natureOfPayment.getValue() == undefined || this.natureOfPayment.getValue() == '') && (this.typeOfdeducteeTypeCombo.getValue() == undefined || this.typeOfdeducteeTypeCombo.getValue() == '')) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.nature.of.payment.report.payment.alert")], 2);
                return false;
            } else if (this.natureOfPayment.getValue() == undefined || this.natureOfPayment.getValue() == '') {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.nature.of.payment.report.payment.alert.nature.of.payment")], 2);
                return false;
            } else if (this.typeOfdeducteeTypeCombo.getValue() == undefined || this.typeOfdeducteeTypeCombo.getValue() == '') {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.nature.of.payment.report.payment.alert.deductee.type")], 2);
                return false;
            }
        } else {
            return false; // if nature of payment and deductee type combo are not present, no payment
        }
        this.CreateTDSAndInterestPaymentWindow();
    },
    CreateTDSAndInterestPaymentWindow: function () {
        this.tdsPaymentType = new Wtf.form.Checkbox({
            boxLabel: " ",
            width: 50,
            inputType: 'radio',
            inputValue: 1,
            name: 'rectype',
            fieldLabel: WtfGlobal.getLocaleText("acc.CommonReport.tdsPayment")
        })
        this.tdsInterestPaymentType = new Wtf.form.Checkbox({
            boxLabel: " ",
            inputType: 'radio',
            name: 'rectype',
            inputValue: 2,
            width: 50,
            fieldLabel: WtfGlobal.getLocaleText("acc.CommonReport.tdsInterestPayment")
        })
        this.bothTDSAndInterestPaymentType = new Wtf.form.Checkbox({
            boxLabel: " ",
            inputType: 'radio',
            name: 'rectype',
            inputValue: 3,
            width: 50,
            fieldLabel: WtfGlobal.getLocaleText("acc.CommonReport.bothTDSandInterestPayment")
        })
        this.TypeForm = new Wtf.form.FormPanel({
            region: 'center',
            autoScroll: true,
            border: false,
            labelWidth: 245,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left: 35px;padding-top: 20px;padding-right: 30px;",
            defaultType: 'textfield',
            items: [this.tdsPaymentType, this.tdsInterestPaymentType, this.bothTDSAndInterestPaymentType]
        });
        this.tdsPaymentType.setValue(true);
        this.submitbtn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.submit"),
            scope: this,
            handler: this.submitTDSPaymentType
        });
        this.cancelbtn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
            scope: this,
            handler: function () {
                this.TDSAndInterestPaymentWindow.close();
            }
        });
        var title = WtfGlobal.getLocaleText("acc.CommonReport.paymentType");
        var msg = WtfGlobal.getLocaleText("acc.CommonReport.selectPaymentType");
        this.TDSAndInterestPaymentWindow = new Wtf.Window({
            title: WtfGlobal.getLocaleText("acc.CommonReport.bothTDSandInterestPayment"),
            closable: true,
            modal: true,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            height: 250,
            width: 380,
            autoScroll: true,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body,
            items: [{
                    region: 'north',
                    height: 75,
                    border: false,
                    bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                    html: getTopHtml(title, msg, "../../images/accounting_image/price-list.gif", false)
                }, {
                    region: 'center',
                    border: false,
                    baseCls: 'bckgroundcolor',
                    layout: 'fit',
                    items: this.TypeForm
                }],
            buttons: [this.submitbtn, this.cancelbtn]
        });
        this.TDSAndInterestPaymentWindow.show();
    },
    submitTDSPaymentType: function () {
        this.TDSAndInterestPaymentWindow.close();
        this.tdsPaymentType = this.bothTDSAndInterestPaymentType.getValue()? 3:( this.tdsInterestPaymentType.getValue() ? 2 : (this.tdsPaymentType.getValue() ? 1 : 4));
        if (this.tdsPaymentType == 2 || this.tdsPaymentType == 3) {
            //TDS Interest Rate must be set before making TDS Interest Payment.
            if(Wtf.account.companyAccountPref.CompanyTDSInterestRate <= 0 ){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.CommonReport.setTDSInterestRateFirst")], 2);
                return false;
            }
            // If TDS Interest Payment is selected then ensure that each vendor has TDS Interest Account mapped.
            var jsonRecords = this.items.items[0].store.data.items;
            for (var i = 0; i < jsonRecords.length; i++) {
                var VendorInterestPayableAccount = jsonRecords[i].json.tdsInterestPayableAccountid;
                var vendorName = jsonRecords[i].json.vendorName;
                if (VendorInterestPayableAccount == undefined || VendorInterestPayableAccount == "") {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.CommonReport.TDSInterestPayableAccountIsNotSet") + "<b>"+ vendorName+"</b>"], 2);
                    return false;
                }
            }
        }
        Wtf.Ajax.requestEx({// request to fetch json of particular report, by ID
            url: "ACCCombineReports/getIndiaComplianceReportData.do",
            params: {
                reportid: this.repId,
                startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                tdsPaymentJsonFlag: true,
                tdsPaymentType: this.tdsPaymentType,
                vendorid: this.vendorCMB ? this.vendorCMB.getValue() : "",
                deducteetype: this.typeOfdeducteeTypeCombo ? this.typeOfdeducteeTypeCombo.getValue() : "",
                nop: this.natureOfPayment ? this.natureOfPayment.getValue() : ""
            }
        }, this,
                function (resp) {
                    if (resp && resp.data && resp.data.length > 0) {
//                        if (resp.data[0].paymentMethodNotSetFlag) {
//                            Wtf.MessageBox.alert("Payment Method", "Please define the Payment Method linked to your Bank Account in the System Control Level", this);
//                            return;
//                        }
                        var TDSPaymentDataParams = {
                            reportid: this.repId,
                            startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                            enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                            tdsPaymentJsonFlag: true,
                            tdsPaymentType: this.tdsPaymentType,
                            vendorid: this.vendorCMB ? this.vendorCMB.getValue() : "",
                            deducteetype: this.typeOfdeducteeTypeCombo ? this.typeOfdeducteeTypeCombo.getValue() : "",
                            nop: this.natureOfPayment ? this.natureOfPayment.getValue() : ""
                        };
                        var winValue = 3;
                        var makePaymentPanel = callPaymentReceiptNew(winValue, false, undefined, undefined, false, TDSPaymentDataParams);
                        makePaymentPanel.on("update", function (obj, paymentid) {
                            Wtf.Ajax.requestEx({// request to fetch json of particular report, by ID
                                url: "ACCCombineReports/updateTDSPaymentFlag.do",
                                params: {
                                    startdate: WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                                    enddate: WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                                    tdsPaymentJsonFlag: true,
                                    tdsPaymentType: this.tdsPaymentType,
                                    paymentid: paymentid,
                                    vendorid: this.vendorCMB ? this.vendorCMB.getValue() : "",
                                    deducteetype: this.typeOfdeducteeTypeCombo ? this.typeOfdeducteeTypeCombo.getValue() : "",
                                    nop: this.natureOfPayment ? this.natureOfPayment.getValue() : "",
                                    tdsInterestRateAtPaymentTime:Wtf.account.companyAccountPref.CompanyTDSInterestRate
                                }
                            }, this, function (resp) {
                                this.submitHandler();//After Save Make Payment, fetch Report again.
                            }, function (resp) {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Erroroccuredatserverside")], 2);
                            });
                        }, this);
                    } else {
                        if (this.grid.getStore().getCount() > 0) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.nature.of.payment.report.payment.alert.no.payment")], 2);
                        } else {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.nature.of.payment.report.payment.alert.empty")], 2);
                        }
                    }
                }, function (resp) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Erroroccuredatserverside")], 2);
        });
    },
    
    onCellClick:function(g,i,j,e){
        var el=e.getTarget("a");
        if(el==null) {
            return;
        }
        var header=g.getColumnModel().getDataIndex(j);
        if(header=="vendorcode"){//Verifying on vendor Code
            this.msgLmt = 30;
            this.businessPerson = "Vendor";//As report is only for Vendor.
            this.jReader = new Wtf.data.KwlJsonReader({
                totalProperty: 'totalCount',
                root: "data"
            }, Wtf.personRec);//rec changed to Wtf.personRec
            this.mainURL="ACC"+this.businessPerson+"CMN/get"+this.businessPerson+"s.do";
            this.vendorStore = new Wtf.data.Store({
                title:this.businessPerson+" Information",
                url:this.mainURL,
                baseParams:{
                    mode:2,
                    group:[13],
                    custAmountDueMoreThanLimit:false
                },
                reader: this.jReader,
                remoteSort:true
            });
            var record ="";
            this.vendorStore.on('load',function () {// Edit Vendor
                var index=this.vendorStore.findBy(function(rec){
                    var parentname=rec.data['acccode'];
                    if(parentname == g.store.data.itemAt(i).data.vendorcode)
                        record = rec;
                    else
                        return false;
                })
                if(!Wtf.isEmpty(record)){
                    var chkURL = "ACCVendorCMN/checkVendorTransactions.do";
                    var  rec=record;
                    Wtf.Ajax.requestEx({
                        url:chkURL,
                        params:{
                            accid:rec.data.accid,
                            openbalance:rec.data.openbalance
                        }
                    },
                    this, function(response, a) {
                        if (!Wtf.isEmpty(this.repId) && this.repId == Wtf.UnkownDeducteeTypeReportID || this.repId == Wtf.PANNotAvailableReportID) {
                            callViewBusinessContactWindow(rec, this.isCustomer)
                        } else {
                            callBusinessContactWindow(true, rec, 'bcwin', false, response.success, false);//Edit Case
                            if (Wtf.getCmp("personalDetailVendorTab") != undefined && Wtf.getCmp("personalDetailVendorTab") != null) {
                                Wtf.getCmp("personalDetailVendorTab").on('update', function() {
                                    this.submitHandler();//After Save Vendor, fetch Report again.
                                }, this);
                            }
                        }
                    }, function(response, a) {
                        if (!Wtf.isEmpty(this.repId) && this.repId == Wtf.UnkownDeducteeTypeReportID || this.repId == Wtf.PANNotAvailableReportID) {
                            callViewBusinessContactWindow(rec, this.isCustomer)
                        } else {
                            callBusinessContactWindow(true, rec, 'bcwin', false);
                        }
                    });
                }
            },this);
            this.vendorStore.load();
        }
    },
    createFields:function(data){
        var fields = [];
        for(var fieldcnt = 0; fieldcnt < data.length; fieldcnt++) {
            var fObj = {};
            fObj['name'] = data[fieldcnt].dataindex;
            fObj['mapping'] = data[fieldcnt].dataindex;
            fObj['type'] = 'string';
            fields[fieldcnt] = fObj;
        }
        return fields;
    },
    createFieldsForGrouping:function(data){
        var fields = [];
        for(var fieldcnt = 0; fieldcnt < data.length; fieldcnt++) {
            var fObj = {
                'name':data[fieldcnt].dataindex
            };
            fields.push(fObj);
        }
        return fields;
    },
    
    submitHandler:function(data){
        if (this.startDate.getValue() > this.endDate.getValue()) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 2); // "From Date can not be greater than To Date."
            this.endDate.markInvalid();
            return;
        }
        var paramsObj=new Object();
        paramsObj.startdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        paramsObj.enddate=WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
         
        if(this.repId == '9'){
            paramsObj.vendorid=this.vendorCMB?this.vendorCMB.getValue():"";
            paramsObj.deducteetype=this.typeOfdeducteeTypeCombo?this.typeOfdeducteeTypeCombo.getValue():"";
            paramsObj.nop=this.natureOfPayment?this.natureOfPayment.getValue():"";
            paramsObj.asOfDate = WtfGlobal.convertToGenericDate(this.asOfDate.getValue());
        }
        this.mastertypeST.load({
            params:paramsObj
        });        
    },
    handleResetClick: function () {
       this.startDate.setValue(WtfGlobal.getDates(true));
       this.endDate.setValue(WtfGlobal.getDates(false));
       if(this.repId == '9'){           
            this.vendorCMB.reset();
            this.typeOfdeducteeTypeCombo.reset();
            this.natureOfPayment.reset();
       }
      this.submitHandler();
    },
    
    vatOrCSTPaymentHandler: function(vatPaymentFlag){ //if vatPaymentFlag = true for VAT Payment and false for CST Payment
         Wtf.Ajax.requestEx({            // request to fetch json of particular report, by ID
            url: "ACCCombineReports/getIndiaComplianceReportData.do",
            params: {
                reportid: this.repId,
                startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                vatpaymentjedetails: vatPaymentFlag,
                cstpaymentjedetails: !vatPaymentFlag
            }
        },this,function(resp){
            if(resp!="" && resp.data!="" && resp.data.length>0){
                var jeDetails = {
                    data : resp.data[0]
                };
                var vatPayableFlag = resp.data[0].vatPayableFlag;
                
                if(resp.data[0].jeDetails.length<=0){
                    var paymentName = vatPaymentFlag ? 'VAT' : 'CST';
                    Wtf.MessageBox.alert(paymentName +" Payment","Balance " + paymentName +" Payable is 0. " + paymentName + " Payment cannot be processed",this);
                    return ;
                }
                if(vatPayableFlag || !vatPaymentFlag){ // VAT Payable or CST Payable - Open Make Payment Form
                    
                    if(resp.data[0].paymentMethodNotSetFlag){
                        Wtf.MessageBox.alert("Payment Method","Please define the Payment Method linked to your Bank Account in the System Control Level",this);
                        return;
                    }
                    
                    var taxPaymentDataParams = {
                        reportid: this.repId,
                        startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                        enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                        makePaymentJsonFlag : true,
                        vatpaymentjedetails: vatPaymentFlag,
                        cstpaymentjedetails: !vatPaymentFlag
                    };
                    
                    var winValue = 3;
                    var makePaymentPanel = callPaymentReceiptNew(winValue, false, undefined, undefined, false, taxPaymentDataParams);
                    makePaymentPanel.on("update", function(obj, paymentid){
                        Wtf.Ajax.requestEx({            // request to fetch json of particular report, by ID
                            url: "ACCCombineReports/updateVATorCSTPayemntFlagOnJEPosting.do",
                            params: {
                                startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                                enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                                vatPaymentFlag: vatPaymentFlag,
                                cstPaymentFlag: !vatPaymentFlag,
                                paymentid: paymentid
                            }
                        },this,function(resp){
                            this.submitHandler();//After Save Make Payment, fetch Report again.
                        }, function(resp){
                        });
                    }, this);
                    
                } else { // For VAT Input Credit Carry Forward - Open JE Form
                    
                    var vatPaymentJEpanel = callJournalEntryTab(false, jeDetails,undefined ,"1", undefined, false,true);
                    vatPaymentJEpanel.on("update", function(obj, jeid){
                        if(vatPaymentFlag  ||  !vatPaymentFlag){ // VAT Payable Flag for VAT Payment  OR   CST Payment
                            //Old code condition :  if(vatPayableFlag  ||  !vatPaymentFlag){ // VAT Payable Flag for VAT Payment  OR   CST Payment
                            Wtf.Ajax.requestEx({            // request to fetch json of particular report, by ID
                                url: "ACCCombineReports/updateVATorCSTPayemntFlagOnJEPosting.do",
                                params: {
                                    startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                                    enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
                                    vatPaymentFlag: vatPaymentFlag,
                                    cstPaymentFlag: !vatPaymentFlag,
                                    journalentryid: jeid
                                }
                            },this,function(resp){
                                this.submitHandler();//After Save JE Form, fetch Report again.
                            }, function(resp){
                            });
                        }
                    }, this);
                }
            }
        }, function(resp){
        });
    }
});
