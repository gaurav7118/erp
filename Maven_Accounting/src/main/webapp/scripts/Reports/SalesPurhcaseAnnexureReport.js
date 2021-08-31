/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 * SalesPurhcaseAnnexureReport
 */


function callSalesPurhcaseAnnexureLoad(){
    var panel=Wtf.getCmp('SalesPurhcaseAnnexureReport');
    if(panel==null){
        panel = new Wtf.account.SalesPurhcaseAnnexureReport({
            id: 'SalesPurhcaseAnnexureReport',
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

Wtf.account.SalesPurhcaseAnnexureReport = function(config) {
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
    
    this.rec = new Wtf.data.Record.create([
    {
        name:"invoicenos"
    },

    {
        name:"dosalesinvoice"
    },

    {
        name:"tinofPurchase"
    },

    {
        name:"assessableamount"
    },

    {
        name:"taxamount"
    },

    {
        name:"inclusivetax"
    },

    {
        name:"valuecompo42"
    },

    {
        name:"taxfree"
    },

    {
        name:"exempted41"
    },

    {
        name:"labourcharges"
    },

    {
        name:"othercharges"
    },

    {
        name:"grosstotal"
    },

    {
        name:"action"
    },

    {
        name:"returnformno"
    },

    {
        name:"transactioncode"
    },

    {
        name:"transcationdesc"
    }
    ]);
    this.moduleHeaderReader = new Wtf.data.KwlJsonReader({
        root:"data",
        totalProperty:"count"
    },this.rec);

    this.moduleHeaderStore = new Wtf.data.Store({
        url: "ACCCombineReports/getSalesPurhcaseAnnexureReport.do",
        reader:this.moduleHeaderReader,
        baseParams:{
            enddate:WtfGlobal.convertToGenericDate(this.getDates(false)),
            startdate:WtfGlobal.convertToGenericDate(this.getDates(true)),
            isSalesAnnax:true
        }
    });
    
    this.moduleHeaderStore.load();
    
    
    this.colSpan = new Wtf.GroupHeaderGrid({
        rows: [[{
            align: "center", 
            header: "", 
            colspan: 0
        },{
            align: "center", 
            header: "", 
            colspan: 0
        },{
            align: "center", 
            header: "", 
            colspan: 0
        },{
            align: "center", 
            header: "", 
            colspan: 0
        },{
            align: "center", 
            header: "Taxable Value OR Value of Composition u/s 42(3), (3A), (4)", 
            colspan: 2
        }, 

        {
            align: "center", 
            header: "", 
            colspan: 0
        },
        {
            align: "center", 
            header: "", 
            colspan: 0
        },
        {
            align: "center", 
            header: "", 
            colspan: 0
        },
        {
            align: "center", 
            header: "", 
            colspan: 0
        },
        {
            align: "center", 
            header: "", 
            colspan: 0
        },
        {
            align: "center", 
            header: "", 
            colspan: 0
        },
        {
            align: "center", 
            header: "", 
            colspan: 0
        },
        {
            align: "center", 
            header: "", 
            colspan: 0
        } ,      
{
            align: "center", 
            header: "", 
            colspan: 0
        } ,       
{
            align: "center", 
            header: "", 
            colspan: 0
        },        
        {
            align: "center", 
            header: "", 
            colspan: 0
        }        
        ]],
        hierarchicalColMenu: false
    }); 
    this.rowNo = new Wtf.grid.RowNumberer();
    this.moduleHeaderColumn = new Wtf.grid.ColumnModel([this.rowNo,
    {
        header:"Invoice No.",
        dataIndex:"invoicenos",
        width:300,
        summaryType:"min"

    },{
        header:"Date of Sale Invoice",
        dataIndex:"dosalesinvoice",
        sortable:false,
        width:300,
        groupRenderer: WtfGlobal.nameRenderer,
        summaryRenderer:function(){
            return '<div class="grid-summary-common"; align="right";>'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            }

    },
    {
        header:"TIN of Purchaser (If Any )",
        dataIndex:"tinofPurchase",
        sortable:false,
        width:300,
        groupRenderer: WtfGlobal.nameRenderer,
        summaryRenderer:function(){
            return '<div class="grid-summary-common"; align="right";>'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            }

    },    
    {
        header:"Net Rs.",
        dataIndex:"assessableamount",
        sortable:false,
        width:300,
        groupRenderer: WtfGlobal.nameRenderer,
        summaryRenderer:function(){
            return '<div class="grid-summary-common"; align="right";>'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            }

    },
    {
        header:"TAX (If any) Rs.",
        dataIndex:"taxamount",
        sortable:false,
        width:300,
        groupRenderer: WtfGlobal.nameRenderer,
        summaryRenderer:function(){
            return '<div class="grid-summary-common"; align="right";>'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            }

    },
    {
        header:"Value of Inclusive of Tax Rs.",
        dataIndex:"inclusivetax",
        sortable:false,
        width:300,
        groupRenderer: WtfGlobal.nameRenderer,
        summaryRenderer:function(){
            return '<div class="grid-summary-common"; align="right";>'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            }

    },
    {
        header:"Value of Composition u/s 42 (1), (2)",
        dataIndex:"valuecompo42",
        sortable:false,
        width:300,
        groupRenderer: WtfGlobal.nameRenderer,
        summaryRenderer:function(){
            return '<div class="grid-summary-common"; align="right";>'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            }

    },
    {
        header:"Tax Free Sales",
        dataIndex:"taxfree",
        sortable:false,
        width:300,
        groupRenderer: WtfGlobal.nameRenderer,
        summaryRenderer:function(){
            return '<div class="grid-summary-common"; align="right";>'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            }

    },
    {
        header:"Exempted Sales u/s 41 & 8 ",
        dataIndex:"exempted41",
        sortable:false,
        width:300,
        groupRenderer: WtfGlobal.nameRenderer,
        summaryRenderer:function(){
            return '<div class="grid-summary-common"; align="right";>'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            }

    },
    {
        header:"Labour Charges",
        dataIndex:"labourcharges",
        sortable:false,
        width:300,
        groupRenderer: WtfGlobal.nameRenderer,
        summaryRenderer:function(){
            return '<div class="grid-summary-common"; align="right";>'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            }

    },
    {
        header:"Other Charges",
        dataIndex:"othercharges",
        sortable:false,
        width:300,
        groupRenderer: WtfGlobal.nameRenderer,
        summaryRenderer:function(){
            return '<div class="grid-summary-common"; align="right";>'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            }

    },
    {
        header:"Gross Total (Rs.)",
        dataIndex:"grosstotal",
        sortable:false,
        width:300,
        groupRenderer: WtfGlobal.nameRenderer,
        summaryRenderer:function(){
            return '<div class="grid-summary-common"; align="right";>'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            }

    },
    {
        header:"Action",
        dataIndex:"action",
        sortable:false,
        width:300,
        groupRenderer: WtfGlobal.nameRenderer,
        summaryRenderer:function(){
            return '<div class="grid-summary-common"; align="right";>'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            }

    },
    {
        header:"Return Form Number",
        dataIndex:"returnformno",
        sortable:false,
        width:300,
        groupRenderer: WtfGlobal.nameRenderer,
        summaryRenderer:function(){
            return '<div class="grid-summary-common"; align="right";>'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            }

    },
    {
        header:"Transaction Code",
        dataIndex:"transactioncode",
        sortable:false,
        width:300,
        groupRenderer: WtfGlobal.nameRenderer,
        summaryRenderer:function(){
            return '<div class="grid-summary-common"; align="right";>'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
            }

    },
    {
        header:"Description of Transaction type",
        dataIndex:"transcationdesc",
        sortable:false,
        width:300,
        groupRenderer: WtfGlobal.nameRenderer,
        summaryRenderer:function(){
            return '<div class="grid-summary-common"; align="right";>'+WtfGlobal.getLocaleText("acc.common.total")+'</div>'
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
        layout: 'fit',
        viewConfig: {
            forceFit: true,
            emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        plugins: [this.colSpan]        
    });
    this.moduleHeaderStore.on('load', function(store) {
        this.moduleHeaderGrid.getView().refresh();
    }, this);
    this.moduleHeaderStore.on('loadexception', function(store) {
        this.moduleHeaderGrid.getView().refresh();
    }, this);

    this.moduleHeaderStore.on('beforeload', function(s, o) {
        if (!o.params) {
            o.params = {};
        }
        o.params.startdate = WtfGlobal.convertToGenericDate(this.getDates(true));
        o.params.enddate = WtfGlobal.convertToGenericDate(this.getDates(false));
        o.params.isSalesAnnax=true;        
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
    Wtf.account.SalesPurhcaseAnnexureReport.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.SalesPurhcaseAnnexureReport, Wtf.Panel, {
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
        Wtf.account.SalesPurhcaseAnnexureReport.superclass.onRender.call(this, config);
        this.moduleHeaderStore.load();
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