/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

function callExciseComputationDynamicLoad(){
    var panel=Wtf.getCmp('ExciseComputationReport');
    if(panel==null){
        panel = new Wtf.account.ExciseComputationReport({
            id: 'ExciseComputationReport',
            border: false,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.exciseComputationReport.title"),Wtf.TAB_TITLE_LENGTH),
            tabTip:WtfGlobal.getLocaleText("acc.exciseComputationReport.title"),  //Excise Computation Report
            closable: true
        });
        Wtf.getCmp('as').add(panel);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}
Wtf.account.ExciseComputationReport = function(config) {
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
    
    this.creditAdjustmentButton = new Wtf.Toolbar.Button({
        text: 'Credit Adjustment', 
        scope: this,
        tooltip: 'Credit Adjustment',
        handler: this.creditAdjustmentWindowHandler
    });
    this.tbararray.push(this.creditAdjustmentButton);
    this.ExcisePaymentBttn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.excisecomputationreport.excisePayment"),
        tooltip: WtfGlobal.getLocaleText("acc.excisecomputationreport.excisePayment"),
        id: 'excisepayment' + this.id,
        scope: this,
        handler: this.ExcisePaymentHandler
    });
    this.tbararray.push(this.ExcisePaymentBttn);
    
    /*
     * Provided button to expand or collapse all row details. 
     * We display Particulars,Assessable value,Duty Amount
     */
    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.Collapse"),
        tooltip: WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls: 'pwnd toggleButtonIcon',
        scope: this,
        handler: function () {
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    });
    this.tbararray.push(this.expandCollpseButton);
    this.moduleHeaderRec = new Wtf.data.Record.create([
    {name:"particulars"},
    {name:"taxtype"},
    {name:"accessablevalue"},
    {name:"dutyamount"},
    {name:"account"}
    ]);
    this.moduleHeaderReader = new Wtf.data.KwlJsonReader({
        root:"data",
        totalProperty:"count"
    },this.moduleHeaderRec);

    this.moduleHeaderStore = new Wtf.data.GroupingStore({
        url: "ACCInvoiceCMN/getExciseComputationReport.do",
        reader:this.moduleHeaderReader,
//        remoteSort:true,
//        remoteGroup:true,
        baseParams:{
            enddate:this.getDates(false),
            startdate:this.getDates(true)
        },
        groupField:"taxtype",
        sortInfo: {
            field: 'particulars',
            direction: "DESC"
        }
    });
    this.moduleHeaderStore.on('beforeload',function(){
        this.moduleHeaderStore.baseParams.enddate=WtfGlobal.convertToGenericEndDate(this.toDate.getValue());
        this.moduleHeaderStore.baseParams.startdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        this.loadingMask.show();
    },this);
    this.moduleHeaderStore.on('load',function(){
        this.loadingMask.hide();
        this.updateBalanceExciseDutyPayable();
    },this);
    this.totalValue = new Wtf.Toolbar.TextItem(""); // total value
    this.bbar1 = new Array();
//    this.bbar1.push('->',"<B> Total :</B>",this.totalValue);
    this.moduleHeaderStore.on('datachanged', function(store) {
        var grandTotalInBaseCurrency= 0;  
        var recordindex=store.data.length-1;
        for(var i=0;i <= recordindex;i++){
            if(store.getAt(i).json.dutyamount!=undefined){
                grandTotalInBaseCurrency=parseFloat(grandTotalInBaseCurrency) +  parseFloat(store.getAt(i).json.dutyamount);
            }
        }
            this.totalValue.getEl().innerHTML ="<B>" + WtfGlobal.conventInDecimal(grandTotalInBaseCurrency,WtfGlobal.getCurrencySymbol()) +"</B>\\t"; 
    }, this);
    this.moduleHeaderStore.on('loadexception',function(){
        this.loadingMask.hide();
    },this);
    this.moduleHeaderStore.load();

    this.moduleHeaderColumn = new Wtf.grid.ColumnModel([
    {
        header:"Catagory Type",
        hidden:true,
        dataIndex:"taxtype",
        summaryType:"min"

    },{
        header:WtfGlobal.getLocaleText("acc.exciseComputationReport.Particulars"),
        dataIndex:"particulars",
        sortable:false,
        width:300,
        groupRenderer: WtfGlobal.nameRenderer,
        summaryRenderer:function(){return '<div class="grid-summary-common"; align="right";>'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'}

    },{
        header:"Assessable Value",
        dataIndex:"accessablevalue",
        sortable:false,
        renderer:function(value,m,rec){
            if(rec.data.taxtype == "A. Excise Sales"){//Accessable value column value is to be shown only in case of "A. Excise Sales".
            if(value == 'chkboxval'){
                return '<input type="checkbox">'
            }else{
                var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
                var v=parseFloat(value);
                if(isNaN(v)) return value;
                v= WtfGlobal.conventInDecimal(v,symbol)
                return '<div class="currency">'+v+'</div>';
            }
            }else{
                return "";
            }
        },
        hidecurrency: true,
        summaryType:'sum',
        summaryRenderer: function(value, m, rec) {
            if(rec.data.taxtype == "A. Excise Sales"){//Accessable value column value is to be shown only in case of "A. Excise Sales".
                return WtfGlobal.currencySummaryRenderer(value, m, rec);
            }else{
                return "";
            }
        }

    },{
        header:WtfGlobal.getLocaleText("acc.exciseComputationReport.DutyAmount"),
        dataIndex:"dutyamount",
        hidden:false,
        renderer:function(value,m,rec){
            if(value == 'chkboxval'){
                return '<input type="checkbox">'
            }else{
                var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
                var v=parseFloat(value);
                if(isNaN(v)) return value;
                v= WtfGlobal.conventInDecimal(v,symbol)
                return '<div class="currency">'+v+'</div>';
            }
        },
        hidecurrency: true,
        summaryType:'sum',
        summaryRenderer: function(value, m, rec) {
                return WtfGlobal.currencySummaryRenderer(value, m, rec);
        }
    }
    ]);
    this.groupingView = new Wtf.grid.GroupingView({
        forceFit: true,
        showGroupName: false,
        enableGroupingMenu: true,
        hideGroupedColumn:true
    });

    var gridSummary = new Wtf.grid.GroupSummary({});
    this.moduleHeaderGrid = new Wtf.grid.GridPanel({
        store:this.moduleHeaderStore,
        border:false,
        height:500,
        cm:this.moduleHeaderColumn,
        view: this.groupingView,
        plugins: [gridSummary]
    });
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
            items:[this.moduleHeaderGrid]
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
    Wtf.account.ExciseComputationReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.ExciseComputationReport, Wtf.Panel, {
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

        Wtf.account.ExciseComputationReport.superclass.onRender.call(this, config);
        this.moduleHeaderStore.load();
    },
    creditAdjustmentWindowHandler : function(){
        var dutypayable = 0;
        
        for(var j=0; j < this.moduleHeaderStore.data.items.length  ; j++){
            var rec = this.moduleHeaderStore.data.items[j];
            if(rec.data.taxtype == "A. Excise Sales" || rec.data.taxtype == "B. Payable From Previous Period"){
                 dutypayable += rec.data.dutyamount;
            }else if(rec.data.particulars == "Credit Adjustments" || rec.data.particulars == "Advance Excise Duty Adjustments"){
                dutypayable -= rec.data.dutyamount;
            }
        }
        dutypayable=dutypayable>=0?dutypayable:0;
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
            header:"Principle Input" ,
            width:100,
            dataIndex:'exciseduty'
        }
        ,{
            header:"Capital Goods" ,
            width:100,
            dataIndex:'capitalgoods'
        }
        ,{
            header:"Service Tax Input" ,
            width:100,
            dataIndex:'servicetax'
        }
//        ,
//        {
//            header:"Adjustment Amount" ,
//            width:100,
//            dataIndex:'adjustmentamount',
//            editor: this.amount = new Wtf.form.NumberField({
//                name: 'adjustmentamount',
//                maxValue:dutypayable<cenvatavailable ? dutypayable : cenvatavailable//,
////                allowNegative: false,
////                allowDecimals:true,
////                selectOnFocus:true,
////                allowBlank: true
//            })
//        }  
        ]);
       
        this.Rec = new Wtf.data.Record.create([
            {name: 'documentnumber'}, 
            {name: 'documentid'}, 
            {name: 'currencysymbol'}, 
            {name: 'currencyid'}, 
            {name: 'assessablevalue'}, 
            {name: 'dateofentry'}, 
            {name: 'exciseduty'}, 
            {name: 'capitalgoods'}, 
            {name: 'servicetax'}, 
            {name: 'moduleid'}
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
                this.grid.exciseDutySelected = exciseduty;
                var element = document.getElementById('cenvatcredit');
                element.innerHTML = "<span style='padding-left:10px'><b>Duty Payable : </b>"+ getRoundedAmountValue(this.grid.dutypayable) +"<b></span><span style='padding-left:10px'>CENVAT Available : </b>"+ getRoundedAmountValue(this.grid.cenvatavailable) +"<b><span style='padding-left:10px'>Adjustment Amount : </b>"+ getRoundedAmountValue(exciseduty) +"</span>";
            }
        },this);
        
//        this.Rec = new Wtf.data.Record.create([{
//            name: 'dutyhead'
//        },{
//            name: 'dutypayable'
//        },{
//            name: 'cenvatavailable'
//        },{
//            name: 'adjustmentamount'
//        }]);
//        
//        this.store = new Wtf.data.Store({
//            reader: new Wtf.data.KwlJsonReader({
//                root: "data",
//                totalProperty:'totalCount'
//            },this.Rec),
////            url:"ACCInvoiceCMN/getExciseComputationReport.do",
//            baseParams:{
//                enddate:this.getDates(false),
//                stdate:this.getDates(true)
//            }
//        });
//        
//        var jsonData = {
//            "data": [{"dutyhead": "Basic Excise Duty", "dutypayable": dutypayable, "cenvatavailable": cenvatavailable}]
//        };
//        
//        this.store.loadData(jsonData);
//        
//        this.grid = new Wtf.grid.EditorGridPanel({
//            clicksToEdit:1,
//            store: this.store,
//            height:420,
//            width:400,
//            scope:this,
//            cm: this.cm,
//            border : false,
//            loadMask : true,
//            viewConfig: {
//                forceFit:true,
//                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
//            }
//        });
        
        var bodyDesign = "Payable From : "+ this.getDates(true).format(WtfGlobal.getOnlyDateFormat()) +" To "+this.getDates(false).format(WtfGlobal.getOnlyDateFormat());
        
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
        this.moduleHeaderStore.load();
    },
    updateBalanceExciseDutyPayable: function(){
        var dutyamount=0;
        var reportRec = this.moduleHeaderStore.getAt(this.moduleHeaderStore.find("particulars", "Balance Excise Duty Payable"));
        for(var j=0; j < this.moduleHeaderStore.data.items.length  ; j++){
            var rec = this.moduleHeaderStore.data.items[j];
            if(rec.data.taxtype == "A. Excise Sales" || rec.data.taxtype == "B. Payable From Previous Period"){
                 dutyamount += rec.data.dutyamount;
            }else if(rec.data.particulars == "Credit Adjustments" || rec.data.particulars == "Advance Excise Duty Adjustments"){
                dutyamount -= rec.data.dutyamount;
            }
        }
        reportRec.set("dutyamount",dutyamount>=0?dutyamount:0);
    },
    
    /*
     * ExpandCollapse button handler
     * To expand or collapse all row details
     * If grid rows are already in expand mode then collapse rows and vise versa
     */
    expandCollapseGrid: function (btntext) {
        
        if (btntext == WtfGlobal.getLocaleText("acc.field.Collapse")) {
            /*If button text is collapse then collapse all rows*/
            this.moduleHeaderGrid.getView().collapseAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if (btntext == WtfGlobal.getLocaleText("acc.field.Expand")) {
            /*If button text is expand then expand all rows*/
            this.moduleHeaderGrid.getView().expandAllGroups()
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    }
});
