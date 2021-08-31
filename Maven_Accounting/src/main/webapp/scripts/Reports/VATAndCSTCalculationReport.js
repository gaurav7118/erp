/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.  
 */


function callOutPutVATAndCSTCalculationReportLoad(panelId){
    if(!panelId){
        panelId="as";
    }
    var panel=Wtf.getCmp('OutPutVATAndCSTCalculationReport');
    if(panel==null){
        panel = new Wtf.account.VATAndCSTCalculationReport({
            id: 'OutPutVATAndCSTCalculationReport',
            border: false,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            title:"Input VAT & CST Calculation Report", //Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.exciseComputationReport.title"),Wtf.TAB_TITLE_LENGTH),
            tabTip:"Input VAT & CST Calculation Report",//WtfGlobal.getLocaleText("acc.exciseComputationReport.title"),  //Excise Computation Report
            closable : panelId === "as" ,
            isSalesAnnax:false
        });
        Wtf.getCmp(panelId).add(panel);
    }
    if(panelId === "as"){
        Wtf.getCmp(panelId  ).setActiveTab(panel);
    }
    Wtf.getCmp(panelId).doLayout();
}

function callInputVATAndCSTCalculationReportLoad(panelId){
    var panel=Wtf.getCmp('InputVATAndCSTCalculationReport');
    if(panel==null){
        panel = new Wtf.account.VATAndCSTCalculationReport({
            id: 'InputVATAndCSTCalculationReport',
            border: false,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            title:"Output VAT & CST Calculation Report", //Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.exciseComputationReport.title"),Wtf.TAB_TITLE_LENGTH),
            tabTip:"Output VAT & CST Calculation Report",//WtfGlobal.getLocaleText("acc.exciseComputationReport.title"),  //Excise Computation Report
            closable: false,
            isSalesAnnax:true
        });
        Wtf.getCmp(panelId).add(panel);
    }    
    Wtf.getCmp(panelId).doLayout();
}

Wtf.account.VATAndCSTCalculationReport = function(config) {
    this.loadingMask = new Wtf.LoadMask(document.body,{
        msg : WtfGlobal.getLocaleText("acc.msgbox.50")
    });
    this.tbararray = new Array();
    this.fetchButton = new Wtf.Toolbar.Button({
        text: 'Fetch', 
        scope: this,
        tooltip: 'Fetch Report',
        handler: this.fetchReportHandler,
        iconCls:'accountingbase fetch'
    });
    this.startDate= new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.startDate")+'*',
        format:WtfGlobal.getOnlyDateFormat(),
        value:this.getDates(true),
        anchor:'60%',
        name:"startdate",
        allowBlank:false
    });
    this.tbararray.push("From ",this.startDate);
    this.toDate= new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.endDate")+'*',
        format:WtfGlobal.getOnlyDateFormat(),
        value:this.getDates(false),
        anchor:'60%',
        name:"enddate",
        allowBlank:false
    });
    this.startDate.on('change',function(field,newval,oldval){
        if(field.getValue()!='' && this.toDate.getValue()!=''){
            if(field.getValue().getTime()>this.toDate.getValue().getTime()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                field.setValue(oldval);                    
            }
        }
    },this);
    this.toDate.on('change',function(field,newval,oldval){
        if(field.getValue()!='' && this.startDate.getValue()!=''){
            if(field.getValue().getTime()<this.startDate.getValue().getTime()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
                field.setValue(oldval);
            }
        }
    },this);
    this.tbararray.push("To ",this.toDate);
    this.tbararray.push(this.fetchButton);
    
    this.expButton = new Wtf.exportButton({
        obj: this,
        tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details.',
        filename: config.title,
        menuItem: {
            csv: false,
            pdf: false,
            rowPdf: false,
            xls: true
        },
        params: {
            enddate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            startdate:WtfGlobal.convertToGenericDate(this.toDate.getValue()),
            isSalesAnnax:config.isSalesAnnax
        },
        get: Wtf.autoNum.VATAndCSTCalculationReport,
        label: WtfGlobal.getLocaleText("acc.ccReport.tab3")
    });
    this.tbararray.push("-");
    this.tbararray.push(this.expButton);
    
    this.rec = new Wtf.data.Record.create([
    {
        name:"entrydate"
    },

    {
        name:"partyname"
    },

    {
        name:"vattin"
    },

    {
        name:"csttin"
    },

    {
        name:"transcationtype"
    },

    {
        name:"transactionno"
    },

    {
        name:"entryno"
    },

    {
        name:"assessablevalue"
    },

    {
        name:"vatamt"
    },{
        name:"cstamt"
    },{
        name:"taxaccount"  
    }
    
    ]);
    this.moduleHeaderReader = new Wtf.data.KwlJsonReader({
        root:"data",
        totalProperty:"count"
    },this.rec);

    this.moduleHeaderStore = new Wtf.data.GroupingStore({
        url: "ACCCombineReports/getVATAndCSTCalculationReport.do",
        reader:this.moduleHeaderReader,
        baseParams:{
            enddate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
            startdate:WtfGlobal.convertToGenericDate(this.toDate.getValue()),
            isSalesAnnax:config.isSalesAnnax            
        },
        sortInfo:{
            field: 'taxaccount', 
            direction: "ASC"
        },
        groupField:'taxaccount'
    });
        
    this.pagingToolbar = new Wtf.PagingSearchToolbar({
        pageSize: 15,
        id: "pagingtoolbar" + this.id,
        store: this.moduleHeaderStore,
        scope:this,
        //            searchField: this.quickPanelSearch,
        displayInfo: true,
        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), // "No results to display",
        plugins: this.pP = new Wtf.common.pPageSize({
            id: "pPageSize_" + this.id
        })
    });
    this.moduleHeaderStore.load({
        params: {
            start: 0,
            limit: (this.pP && this.pP.combo) ? this.pP.combo.getValue(): 15
        }
    });
    this.rowNo = new Wtf.grid.RowNumberer();
    this.moduleHeaderColumn = new Wtf.grid.ColumnModel([this.rowNo,
    {
        header:"Praticulars",
        dataIndex:"taxaccount",
        width:300,
        pdfwidth:75

    },{
        header:"Date",
        dataIndex:"entrydate",
        width:300,
        pdfwidth:75
    },{
        header:"Party Name",
        dataIndex:"partyname",
        sortable:false,
        width:300,
        pdfwidth:75
    },
    {
        header:"VAT TIN",
        dataIndex:"vattin",
        sortable:false,
        width:300,
        pdfwidth:75

    },    
    {
        header:"CST TIN",
        dataIndex:"csttin",
        sortable:false,
        width:300,
        pdfwidth:75

    },
    {
        header:"Transaction Type",
        dataIndex:"transcationtype",
        sortable:false,
        width:300,
        pdfwidth:75

    },
    {
        header:"Transaction Number",
        dataIndex:"transactionno",
        sortable:false,
        width:300,
        pdfwidth:75

    },
    {
        header:"JE No",
        dataIndex:"entryno",
        sortable:false,
        width:300,
        pdfwidth:75

    },
    {
        header:"Assessable Value",
        dataIndex:"assessablevalue",
        sortable:false,
        width:300,
        pdfwidth:75,
        renderer : function(v,m,rec){
            if(v < 0){
                v= "(<label style='color:red'>"+Math.abs(v)+"</label>)";
            }
            return v;
        },
        summaryType:'sum',
        summaryRenderer: function(value, m, rec) {
            return WtfGlobal.currencySummaryRenderer(value, m, rec);
        }

    },
    {
        header:"VAT Amount",
        dataIndex:"vatamt",
        sortable:false,
        width:300,
        pdfwidth:75,
        renderer : function(v,m,rec){
            if(v < 0){
                v= "(<label style='color:red'>"+Math.abs(v)+"</label>)";
            }
            return v;
        },
        summaryType:'sum',
        summaryRenderer: function(value, m, rec) {
            return WtfGlobal.currencySummaryRenderer(value, m, rec);
        }

    },
    {
        header:"CST Amount",
        dataIndex:"cstamt",
        sortable:false,
        width:300,
        pdfwidth:75,
        summaryType:'sum',
        summaryRenderer: function(value, m, rec) {
            return WtfGlobal.currencySummaryRenderer(value, m, rec);
        }

    }
    ]);
    var groupingView = new Wtf.grid.GroupingView({
        forceFit: true,
        showGroupName: false,
        enableGroupingMenu: false,
        emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec")),
        hideGroupedColumn:true
    });

    var gridSummary = new Wtf.grid.GroupSummary({});
    
    this.grid =new  Wtf.ux.grid.MultiGroupingGrid({
        stripeRows :true,
        store:this.moduleHeaderStore,
        border:false,
        height:500,
        cm:this.moduleHeaderColumn,
        layout: 'fit',       
        plugins:[gridSummary],
        view :groupingView,
        bbar: this.pagingToolbar
        
    });
    this.moduleHeaderStore.on('load', function(store) {
        this.grid.getView().refresh();
    }, this);
    this.moduleHeaderStore.on('loadexception', function(store) {
        this.grid.getView().refresh();
    }, this);

    this.moduleHeaderStore.on('beforeload', function(s, o) {
        if (!o.params) {
            o.params = {};
        }
        o.params.startdate = WtfGlobal.convertToGenericDate(this.startDate.getValue());
        o.params.enddate = WtfGlobal.convertToGenericDate(this.toDate.getValue());
        o.params.limit=(this.pP.combo!=undefined) ? this.pP.combo.value : 15;
        o.params.isSalesAnnax=config.isSalesAnnax;
        this.moduleHeaderStore.baseParams = o.params;
    }, this);
    this.wrapperPanel = new Wtf.Panel({
        border:false,
        layout:"border",
        scope:this,
        items:[
        this.centerPanel = new Wtf.Panel({
            width:'90%',
            region:'center',
            layout:'fit',
            border:false,
            items:[this.grid]
        })
        ]
    });
    
    Wtf.apply(this,{
        defaults:{
            border:false,
            bodyStyle:"background-color:white;"
        },
        items:this.wrapperPanel,
        tbar:this.tbararray,
        bbar:this.bbar1
    },config);    
    Wtf.account.VATAndCSTCalculationReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.VATAndCSTCalculationReport, Wtf.Panel, {
    getDates:function(start){
        var d=new Date();
        if(this.statementType=='BalanceSheet'){
            if(start)
                return new Date('January 1, 1970 00:00:00 AM');
            else
                return d;
        }
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
    onRender: function(config) {
        Wtf.account.VATAndCSTCalculationReport.superclass.onRender.call(this, config);
        this.moduleHeaderStore.load({
            params: {
                start: 0,
                limit: (this.pP && this.pP.combo) ? this.pP.combo.getValue(): 15
            }
        });
    },
    creditAdjustmentWindowHandler : function(){
        var dutypayable = 0;
        
        for(var j=0; j < (this.moduleHeaderStore.data.items.length -1)  ; j++){
            var rec = this.moduleHeaderStore.data.items[j];
            if(rec.data.taxtype == "A. Excise Sales" || rec.data.taxtype == "B. Payable From Previous Period"){
                dutypayable += rec.data.dutyamount;
            }
        //            else if(rec.data.particulars == "GAR 7 Payments"){
        //                dutypayable -= rec.data.dutyamount;
        //            }
        }
        
        var cenvatavailable = 0;
        
        var cenvatInputRec = this.moduleHeaderStore.getAt(this.moduleHeaderStore.find("particulars", "CENVAT Credit on Inputs"));
        var cenvatCapitalGoodsRec = this.moduleHeaderStore.getAt(this.moduleHeaderStore.find("particulars", "CENVAT Credit on Capital Goods"));
        
        if(cenvatInputRec != null && cenvatInputRec != undefined && cenvatInputRec.data.dutyamount != undefined && cenvatInputRec.data.dutyamount != ''){
            cenvatavailable += cenvatInputRec.data.dutyamount
        }
        if(cenvatCapitalGoodsRec != null && cenvatCapitalGoodsRec != undefined && cenvatCapitalGoodsRec.data.dutyamount != undefined && cenvatCapitalGoodsRec.data.dutyamount != ''){
            cenvatavailable +=  cenvatCapitalGoodsRec.data.dutyamount
        }

        this.rn = new Wtf.grid.RowNumberer();
        this.sm = new Wtf.grid.CheckboxSelectionModel({
            });
        this.cm= new Wtf.grid.ColumnModel([this.rn,this.sm,{
            header:"Invoice Number" ,
            width:100,
            dataIndex:'documentnumber'
        },{
            header:"Assessable Value" ,
            width:100,
            dataIndex:'assessablevalue'
        }
        ,{
            header:"Excise Duty" ,
            width:100,
            dataIndex:'exciseduty'
        }
        ]);
       
        this.Rec = new Wtf.data.Record.create([
        {
            name: 'documentnumber'
        }, 

        {
            name: 'documentid'
        }, 

        {
            name: 'currencysymbol'
        }, 

        {
            name: 'currencyid'
        }, 

        {
            name: 'assessablevalue'
        }, 

        {
            name: 'dateofentry'
        }, 

        {
            name: 'exciseduty'
        }, 

        {
            name: 'moduleid'
        }
        ]);
        
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'totalCount'
            },this.Rec),
            url:"ACCCombineReports/getCreditAvailedGoodsReceipt.do",
            baseParams:{
                startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.toDate.getValue()),
                excludeJE:true,
                isExciseInvoice:true
            }
        });
        
        this.store.load();
        
        this.grid = new Wtf.grid.EditorGridPanel({
            clicksToEdit:1,
            store: this.store,
            height:420,
            sm:this.sm,
            width:400,
            scope:this,
            cm: this.cm,
            exciseDutySelected:0,
            selectedRecJson :'',
            dutypayable:dutypayable,
            cenvatavailable:cenvatavailable,
            maxAdjustsmentValue : dutypayable<cenvatavailable ? dutypayable : cenvatavailable,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        this.sm.on('beforerowselect',function(obj,rowIndex,keepExisting,selectedRec){
            var selections = this.grid.getSelectionModel().getSelections();
            var exciseduty= 0.0;
            var finalArr = [];
            for(var i= 0; i< selections.length; i++){
                var rec = selections[i];
                finalArr.push(rec.data);
                if(rec && rec.data){
                    exciseduty += parseFloat(rec.data.exciseduty);
                }
            }
            if(selectedRec && selectedRec.data && selectedRec.data.exciseduty){
                finalArr.push(selectedRec.data);
                exciseduty += parseFloat(selectedRec.data.exciseduty);
            }
            
            if(this.grid.maxAdjustsmentValue && exciseduty <= this.grid.maxAdjustsmentValue){
                this.grid.exciseDutySelected = exciseduty;
                var element = document.getElementById('cenvatcredit');
                element.innerHTML = "<span style='padding-left:10px'><b>Duty Payable : </b>"+ getRoundedAmountValue(this.grid.dutypayable) +"<b></span><span style='padding-left:10px'>CENVAT Available : </b>"+ getRoundedAmountValue(this.grid.cenvatavailable) +"<b><span style='padding-left:10px'>Adjustment Amount : </b>"+ getRoundedAmountValue(exciseduty) +"</span>";
                this.grid.selectedRecJson = finalArr;
                return true;
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.credit.adjust.error.msg")], 2);
                return false;
            }
        },this);
        this.sm.on('rowdeselect',function(obj,rowIndex,record){
            var exciseduty= 0.0;
            if(record && record.data && record.data.exciseduty){
                exciseduty = this.grid.exciseDutySelected;
                exciseduty -= parseFloat(record.data.exciseduty);
                if( exciseduty > 0){
                    this.grid.exciseDutySelected = exciseduty;
                }
                var element = document.getElementById('cenvatcredit');
                element.innerHTML = "<span style='padding-left:10px'><b>Duty Payable : </b>"+ getRoundedAmountValue(this.grid.dutypayable) +"<b></span><span style='padding-left:10px'>CENVAT Available : </b>"+ getRoundedAmountValue(this.grid.cenvatavailable) +"<b><span style='padding-left:10px'>Adjustment Amount : </b>"+ getRoundedAmountValue(exciseduty) +"</span>";
            }
        },this);
        var bodyDesign = "Payable From : "+ this.startDate.getValue().format(WtfGlobal.getOnlyDateFormat()) +" To "+this.toDate.getValue().format(WtfGlobal.getOnlyDateFormat());
        
        this.creditAdjustmentWin = new Wtf.Window({
            title: 'Excise Duties Adjustments',
            height: 400,
            width: 600,
            modal: true,
            resizable: false,
            items : [{
                region: 'north',
                height: 75,
                border: false,
                bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml('Excise Duties Adjustments',bodyDesign, "../../images/accounting_image/price-list.gif", true)
            },{
                region: 'center',
                border: false,
                height:20,
                bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                html: "<div id = 'cenvatcredit'><span style='padding-left:10px'><b>Duty Payable : </b>"+ getRoundedAmountValue(dutypayable) +"<b></span><span style='padding-left:10px'>CENVAT Available : </b>"+ getRoundedAmountValue(cenvatavailable) +"<b><span style='padding-left:10px'>Adjustment Amount : </b>0</span></div>"
            },  this.southPanel=new Wtf.Panel({
                border: false,
                region: 'south',
                id: 'southpan'+this.id,
                bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
                baseCls:'bckgroundcolor',
                layout: 'fit',
                height: 320,
                items:[this.grid]            
            })
            ],
            buttons : [{
                text:'Save',
                scope: this,
                handler: function(){
                    var reportRec = this.moduleHeaderStore.getAt(this.moduleHeaderStore.find("particulars", "Credit Adjustments"));
                    if(this.grid.exciseDutySelected){
                        reportRec.set("dutyamount",this.grid.exciseDutySelected);
                    } else {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.exciseComputationReport.creditadjustment.invoicenotselectedmsg")], 2);
                        return false;
                    }
                    this.updateBalanceExciseDutyPayable();
                    this.creditAdjustmentHandler(reportRec.get("dutyamount"));                    
                    this.creditAdjustmentWin.close();
                }
            }, {
                text:'Close',
                scope: this,
                handler : function(){
                    this.creditAdjustmentWin.close();
                }
            }
            ]
        });
        
        this.creditAdjustmentWin.show();
    },
    creditAdjustmentHandler: function(adjustamount){
        Wtf.Ajax.requestEx({            // request to fetch json of particular report, by ID
            url: "ACCInvoiceCMN/getIndiaComplianceReportData.do",
            params: {
                reportid: "12",//Excise Computation Report: Credit Adjustment
                startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                enddate:WtfGlobal.convertToGenericEndDate(this.toDate.getValue()),
                CreditAdjustmentflag: true,
                selectedIdJson: JSON.stringify(this.grid.selectedRecJson),
                adjustamount:this.grid.exciseDutySelected,
                dutypayable:this.grid.dutypayable
            }
        },this,function(resp, r){
            if(resp!=""){
                Wtf.MessageBox.alert("Credit Adjustment",resp.msg,this);
            }
        }, function(resp){
            if(resp!="" && resp.data!="" && resp.data.length>0){
                
            }
        });
    },
    ExcisePaymentHandler: function(){
        var reportRec = this.moduleHeaderStore.getAt(this.moduleHeaderStore.find("particulars", "Balance Excise Duty Payable"));
        var excisePaymentAmt=reportRec.get("dutyamount");
        
        if(excisePaymentAmt> 0){
            //            Wtf.MessageBox.alert("Payment Method","Please define the Payment Method linked to your Bank Account in the System Control Level",this);

            var taxPaymentDataParams = {
                excisePaymentAmt:excisePaymentAmt,
                excisePaymentFlag : true
            };

            var winValue = 3;
            var makePaymentPanel = callPaymentReceiptNew(winValue, false, undefined, undefined, false, taxPaymentDataParams);
            makePaymentPanel.on("update", function(obj, paymentid){
                Wtf.Ajax.requestEx({            // request to fetch json of particular report, by ID
                    url: "ACCCombineReports/updateVATorCSTPayemntFlagOnJEPosting.do",
                    params: {
                        startdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
                        enddate:WtfGlobal.convertToGenericEndDate(this.toDate.getValue()),
                        excisePaymentFlag: true,
                        paymentid: paymentid
                    }
                },this,function(resp){
                    this.fetchReportHandler();//After Save Make Payment, fetch Report again.
                }, function(resp){
                    });
            }, this);

        } else {
            Wtf.MessageBox.alert("Excise Payment","Balance Excise Duty Payable is 0. Excise Payment cannot be processed",this);
        }
    },
    fetchReportHandler: function(){
        this.moduleHeaderStore.load({
            params: {
                start: 0,
                limit: (this.pP && this.pP.combo) ? this.pP.combo.getValue(): 15
            }
        });
    },
    updateBalanceExciseDutyPayable: function(){
        var dutyamount=0;
        var reportRec = this.moduleHeaderStore.getAt(this.moduleHeaderStore.find("particulars", "Balance Excise Duty Payable"));
        for(var j=0; j < (this.moduleHeaderStore.data.items.length -1)  ; j++){
            var rec = this.moduleHeaderStore.data.items[j];
            if(rec.data.taxtype == "A. Excise Sales" || rec.data.taxtype == "B. Payable From Previous Period"){
                dutyamount += rec.data.dutyamount;
            }else if(rec.data.particulars == "Credit Adjustments" ){
                dutyamount -= rec.data.dutyamount;
            }
        }
        reportRec.set("dutyamount",dutyamount);
    }
});