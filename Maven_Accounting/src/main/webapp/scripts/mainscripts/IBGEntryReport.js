/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.account.IBGEntryReportPanel=function(config){
    Wtf.apply(this, config);
    
    var buttonArray = new Array();
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
         
    this.endDate1=new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate' + this.id,
        value:WtfGlobal.getDates(false),
        width:50
    });
    
    this.startDate.on('change',function(field,newval,oldval){
            if(field.getValue()!='' && this.endDate.getValue()!=''){
                if(field.getValue().getTime()>this.endDate.getValue().getTime()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                    field.setValue(oldval);                    
                }
            }
        },this);
        
        this.endDate.on('change',function(field,newval,oldval){
            if(field.getValue()!='' && this.startDate.getValue()!=''){
                if(field.getValue().getTime()<this.startDate.getValue().getTime()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
                    field.setValue(oldval);
                }
            }
        },this);
        
    this.fetchBtn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.agedPay.fetch"),  //'Fetch',           
        scope: this,
        id:'fetchBtn'+this.id,
        tooltip: WtfGlobal.getLocaleText("acc.invReport.fetchTT"), 
        handler: this.fetchData,
        iconCls:'accountingbase fetch'
    });
    
   this.accRec=new Wtf.data.Record.create([
       {name: 'accid'},
       {name: 'accname'},
       {name: 'acccode'},
       {name: 'dailyBankLimit'},
       {name:'ibgbanktype'}
   ]);
   
   this.accStore=new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:'count'
        },this.accRec),
        url : "ACCAccountCMN/getAccountsForCombo.do",
        baseParams:{
            mode:2,                
            nondeleted:true,
            ignorecustomers:true,  
            ignorevendors:true,
            ignoreCashAccounts:true,
            ignoreGSTAccounts:true,  
            ignoreGLAccounts:true,
            isIBGAccount:true
        }
    });
    
    this.pmtBank= new Wtf.form.ComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.IBG.SelectBank"),
        name:"pmtmetbank",
        store:this.accStore,
        valueField:'accid',
        displayField:'accname',
        emptyText:WtfGlobal.getLocaleText("acc.IBG.PleaseSelectBank"),
        mode: 'local',
        triggerAction: 'all',     
        typeAhead: true,
        value:1,
        forceSelection: true
    });
    
    this.pmtBank.on('select',this.redesignBottomPanel,this);
    this.ibgTransactionCode = new Wtf.form.ComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.IBG.IBGTransactionCode"),
        store: Wtf.ibgTransactionCodeStore,
        name:'ibgCode',
        displayField:'ibgCode',
        id:'ibgCode'+this.id,
        valueField:'ibgCode',
        value:'20',
        mode: 'local',
        width:50,
        listWidth:50,
        hidden:this.isReceipt || !Wtf.account.companyAccountPref.activateIBG,
        triggerAction: 'all',
        typeAhead:true,
        selectOnFocus:true
    });
    
    buttonArray.push(WtfGlobal.getLocaleText("acc.common.from"),this.startDate,WtfGlobal.getLocaleText("acc.common.to"),this.endDate,"-",WtfGlobal.getLocaleText("coa.masterType.bank"),
    this.pmtBank,"-",WtfGlobal.getLocaleText("acc.IBG.IBGTransactionCode"), this.ibgTransactionCode,this.fetchBtn);
    
    this.record=new Wtf.data.Record.create([
        {name:'billid'},
        {name:'billno'},
        {name:'billdate',type:'date'},
        {name:'paidto'},
        {name:'currencysymbol'},
        {name:'amount', type:'float'},
        {name:'amountinbase', type:'float'},
        {name:'isGIROFileGenerated'},
        {name:'valuedate',type:'date'}
    ]);
    this.store=new Wtf.data.Store({
        reader:new Wtf.data.KwlJsonReader({
            totalProperty:'count',
            root: "data"  
        },this.record),
        url: "ACCReports/getIBGEntryReport.do"
    });

    this.store.on('beforeload',function(s,o){
        o.params.startdate= WtfGlobal.convertToGenericDate(this.startDate.getValue());
        o.params.enddate= WtfGlobal.convertToGenericDate(this.endDate.getValue());
        o.params.bankid= this.pmtBank.getValue();
        o.params.ibgCode = this.ibgTransactionCode.getValue();
        var rec = WtfGlobal.searchRecord(this.accStore, this.pmtBank.getValue(), "accid");
        var bankType = rec.data.ibgbanktype;
        o.params.ibgBank = bankType;
    },this);
    this.store.on('load',function(){
            this.totalAmt.setValue(0);   // Resetting the total amount to 0
    },this);
    this.accStore.load();     
    this.accStore.on('load',function(store){
        if(store.getCount()>0){
            var rec=store.getAt(0);            
            this.pmtBank.setValue(rec.data.accid);
            this.dailyLimit.setValue(rec.data.dailyBankLimit);
            this.store.load({
                params: {
                    start:0,
                    limit:30
                }
            });
//            this.redesignBottomPanel();
        }
    },this);
    
    this.pagingToolbar = new Wtf.PagingSearchToolbar({
                pageSize: 30,
                id: "pagingtoolbar" + this.id,
                store: this.store,           
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"),
                plugins: this.pP = new Wtf.common.pPageSize({
                   id : "pPageSize_"+this.id
                })
    })
       
    this.rowNo=new Wtf.grid.RowNumberer();
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.grid = new Wtf.grid.EditorGridPanel({
         store:this.store,
         sm:this.sm,
         border:false,
         clicksToEdit:1,
         layout:'fit',
         loadMask : true,
         bbar: this.pagingToolbar,
         viewConfig:{forceFit:true, emptyText:WtfGlobal.getLocaleText("acc.field.Norecordstodisplay.")},
         columns:[this.sm,this.rowNo,{
            header :WtfGlobal.getLocaleText("acc.pmList.gridPaymentNo"),
            renderer:WtfGlobal.deletedRenderer,        
            dataIndex: 'billno' ,
            pdfwidth:200
         },{
            header :WtfGlobal.getLocaleText("acc.pmList.Date"), //"Payment Date",
            renderer:WtfGlobal.onlyDateRenderer,
            dataIndex: 'billdate',
            align:'center',
            pdfwidth:200
         },{
            header: WtfGlobal.getLocaleText("acc.mp.paidTo"),  //"Paid To",
            renderer:WtfGlobal.deletedRenderer,
            dataIndex: 'paidto',
            width:100,
            pdfwidth:200
          },{
            header :WtfGlobal.getLocaleText("acc.prList.amtPaid"), //Amount Paid
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            dataIndex: 'amount',
            pdfwidth:200          
          },{
            header :WtfGlobal.getLocaleText("acc.IBG.ValueDate"),
            dataIndex: 'valuedate',
            pdfwidth:200,
            align:'center',
            renderer:WtfGlobal.onlyDateRenderer,
            editor:new Wtf.form.DateField({
                name:'valuedate',
                format:WtfGlobal.getOnlyDateFormat()
            })
          },{
              header :WtfGlobal.getLocaleText("acc.field.ibgBankDetail.isibggenerated"), //isGIROFileGenerated
              renderer:function(v){
                  if(v){
                      return "Yes";
                  }else{
                      return "No";
                  }
              },
              dataIndex: 'isGIROFileGenerated',
              pdfwidth:200
          }]
    });
    
    this.grid.getSelectionModel().on('rowselect',this.selectionChange,this);
    this.grid.getSelectionModel().on('rowdeselect',this.selectionChange,this);
    
    
    Wtf.account.IBGEntryReportPanel.superclass.constructor.call(this,{
        autoDestroy:true,
        border: false,
        layout :'border',
        tbar:buttonArray,
        items:[{
            region:'center',
            layout:'fit',
            border:false,
            items:this.grid    
        }]
    });
}

Wtf.extend(Wtf.account.IBGEntryReportPanel,Wtf.Panel,{
    onRender:function(config){
        this.DBSBank = 0;
        this.CIMBBank = 1;
        this.OCBCBank = 2;
        
        Wtf.account.IBGEntryReportPanel.superclass.onRender.call(this,config);   
        this.createFields();
        this.createForm();
        this.createSouthPanel();
        this.add({
            region: 'south',
            layout: 'fit',
            border: false,
            items: this.southPanel,
            height: 200
        });
    },
    createSouthPanel: function () {
        this.southPanel = new Wtf.Panel({
            border: false,
            autoHeight: true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            layout: 'card',
            activeItem : 0,
            items: [this.DBSFormPanel,this.CIMBFormPanel,this.OCBCFormPanel]
        });
    },
    createForm: function () {
        //DBS Bank
        this.DBSFormPanel = new Wtf.form.FormPanel({
            border: false,
            autoHeight: true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            layout: 'column',
            items: [{
                    columnWidth: .45,
                    border: false,
                    items: [new Wtf.form.FieldSet({
                            title:WtfGlobal.getLocaleText("acc.IBG.ApplyValueDatetoSelectedPayments"),
                            autoHeight:true,
                            width:400,
                            items:[this.valueDate,this.applyButton]
                            }),new Wtf.form.FieldSet({
                            title: WtfGlobal.getLocaleText("acc.IBG.GenerateIBGForAppliedValueDate"),
                            autoHeight: true,
                            width: 400,
                            items: [this.genDate, this.genButton]
                        })]
                }, {
                    columnWidth: .55,
                    border: false,
                    layout: 'form',
                    items: [this.totalAmt, this.dailyLimit]
                }]
        });
        //CIMB Bank
        this.CIMBFormPanel = new Wtf.form.FormPanel({
            border: false,
            autoHeight: true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            layout: 'column',
            items: [{
                    columnWidth: .45,
                    border: false,
                    items: [new Wtf.form.FieldSet({
                            title: WtfGlobal.getLocaleText("acc.cimb.generateGiro"),
                            autoHeight: true,
                            width: 400,
                            items: [this.CIMBGenDate, this.CIMBGenButton]
                        })]
                }, {
                    columnWidth: .55,
                    border: false,
                    layout: 'form',
                    items: [this.purposeCode, this.CIMBTotalAmt]
                }]
        });
        //OCBC Bank
        this.OCBCFormPanel = new Wtf.form.FormPanel({
            border: false,
            autoHeight: true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            layout: 'column',
            items: [{
                    columnWidth: .30,
                    border: false,
                    layout: 'form',
                    items: [this.OCBCValueDate,this.OCBCSubmissionDate, this.OCBCGenButton]
                }, {
                    columnWidth: .30,
                    border: false,
                    layout: 'form',
                    items: [this.OCBCPurposeCode, this.OCBCTotalAmt,this.OCBCTransActionTypeCode]
                }, {
                    columnWidth: .30,
                    border: false,
                    layout: 'form',
                    items: [this.OCBCClearing, this.OCBCBatchNumber, this.OCBCOnBehalfOf, this.OCBCValueTime]
                }]
        });
    },
    createFields: function () {
        
        //DBS Bank Fields.
        this.valueDate = new Wtf.ExDateFieldQtip({
            fieldLabel: WtfGlobal.getLocaleText("acc.IBG.ValueDate"),
            name: 'valuedate' + this.id,
            format: WtfGlobal.getOnlyDateFormat(),
            value: new Date(),
            labelStyle: 'width:120px;'
        });
        this.applyButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.lp.apply"),
            scope: this,
            hidden: WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.apply),
            handler: this.applyDateHandler
        });
        this.genDate = new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.het.49"),
            name: 'date' + this.id,
            format: WtfGlobal.getOnlyDateFormat(),
            value: new Date(),
            labelStyle: 'width:120px;'
        });
        this.genButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.lp.generate"),
            scope: this,
            hidden: WtfGlobal.EnableDisable(Wtf.UPerm.miscellaneous, Wtf.Perm.miscellaneous.generate),
            handler: this.generateIBGHandler
        });
        this.totalAmt = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.IBG.TotalAmountofSelectedRecordsInSGD"),
            readOnly: true,
            allowDecimals: true,
            labelWidth: 300,
            decimalPrecision: 2,
            value: 0,
            maxLength: 16
        });
        this.dailyLimit = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.IBG.DailyLimitofSelectedBank"),
            readOnly: true,
            autoWidth: true,
            value: 0,
            decimalPrecision: 4
        });
        
        //CIMB Bank Fields.
        this.CIMBGenDate = this.genDate.cloneConfig();
        this.CIMBGenButton = this.genButton.cloneConfig();
        this.CIMBTotalAmt = this.totalAmt.cloneConfig({style: "width:160px;"});
        
        this.purposeCode = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.cimb.purposeCode"),
            store: Wtf.cimbPurposeCodeStore,
            name: 'purposeCode',
            displayField: 'name',
            valueField: 'id',
            value: 2,
            mode: 'local',
            triggerAction: 'all',
            typeAhead: true,
            selectOnFocus: true,
            style: "width:143px;",
            scope: this,
            forceSelection: true,
            editable: false
        });
        
        //OCBC Bank Fields.
        this.OCBCPurposeCodeStore = new Wtf.data.SimpleStore({
            fields: [{name: "id"}, {name: "name"}],
            data: [[1, "OTHR"], [2, "SALA"], [3, "COLL"]]
        });
        this.OCBCValueDate = this.valueDate.cloneConfig({
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.ocbcBank.generateIBG.valueDate.qtip") + "'>" + WtfGlobal.getLocaleText("acc.IBG.ValueDate") + "* </span>",
            name: "ocbcValueDate",
            allowBlank:false
        });
        this.OCBCSubmissionDate = this.genDate.cloneConfig({fieldLabel: WtfGlobal.getLocaleText("acc.ocbcBank.generateIBG.submissionDate")});
        this.OCBCGenButton = this.genButton.cloneConfig();
        this.OCBCPurposeCode = this.purposeCode.cloneConfig({value: 1,store:this.OCBCPurposeCodeStore});
        this.OCBCTotalAmt = this.totalAmt.cloneConfig({style: "width:160px;"});
        
        this.OCBCTransActionTypeCode = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.ocbcBank.generateIBG.transactionTypeCode") + "*",
            name: 'ocbcTransActionTypeCode',
            readOnly: true,
            value: "Payment",
            style: "width:160px;",
            scope: this
        });
        this.OCBCClearingStore = new Wtf.data.SimpleStore({
            fields: [{name: "id"}, {name: "name"}],
            data: [[1, "GIRO"], [2, "FAST"]]
        });
        this.OCBCClearing = new Wtf.form.ComboBox({
            fieldLabel: "<span wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.ocbcBank.generateIBG.clearing.qtip") + "\">" + WtfGlobal.getLocaleText("acc.ocbcBank.generateIBG.clearing") + "* </span>",
            store: this.OCBCClearingStore,
            name: 'ocbcClearing',
            displayField: 'name',
            valueField: 'id',
            mode: 'local',
            triggerAction: 'all',
            typeAhead: true,
            selectOnFocus: true,
            allowBlank: false,
            value: 1,
            style: "width:143px;",
            scope: this,
            editable: false,
            listeners: {
                'select': {
                    fn: this.hideShowFieldsApplicableForGIROAndFast,
                    scope: this
                }
            }
        });
        this.OCBCBatchNumber = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.ocbcBank.generateIBG.BatchNumber.qtip") + "\">" + WtfGlobal.getLocaleText("acc.ocbcBank.generateIBG.BatchNumber") + "</span>",
            name: 'ocbcBatchNumber',
            maxLength: 3,
            style: "width:160px;",
            scope: this
        });
        this.OCBCOnBehalfOf = new Wtf.form.TextField({
            fieldLabel: "<span wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.ocbcBank.generateIBG.onBehalfOf.qtip") + "\">" + WtfGlobal.getLocaleText("acc.ocbcBank.generateIBG.onBehalfOf") + "</span>",
            name: 'ocbcOnBehalfOf',
            maxLength: 20,
            style: "width:160px;",
            scope: this
        });
        this.OCBCValueTime = new Wtf.form.TimeField({
            fieldLabel: "<span wtf:qtip=\"" + WtfGlobal.getLocaleText("acc.ocbcBank.generateIBG.valueTime.qtip") + "\">" + WtfGlobal.getLocaleText("acc.ocbcBank.generateIBG.valueTime") + "</span>",
            hidenName: 'ocbcValueTime',
            style: "width:143px;",
            scope: this,
            format: "H:i"
        });
        this.OCBCBatchNumber.on('blur', function (obj) {
            obj.setValue(obj.getValue().trim());
        }, this);
        this.OCBCOnBehalfOf.on('blur', function (obj) {
            obj.setValue(obj.getValue().trim());
        }, this);
    },
    fetchData:function(){
        this.store.load({
            params: {
                start:0,
                limit:this.pP.combo.value,
                startdate : WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                enddate : WtfGlobal.convertToGenericDate(this.endDate.getValue()),
                bankid : this.pmtBank.getValue(),
                ibgCode : this.ibgTransactionCode.getValue()
            }
        });
        this.totalAmt.setValue(0);   // Resetting the total amount to 0
    },
    
    generateIBGHandler:function(){
        
        // getPaymentids from selected records
        var rec = WtfGlobal.searchRecord(this.accStore, this.pmtBank.getValue(), "accid");
        var bankType = rec.data.ibgbanktype;
        if (bankType == Wtf.IBGBanks.OCBCBank) {
            this.generateGIORFileForOCBCBank();
        } else {
            var purposeCode = (this.purposeCode)?this.purposeCode.getRawValue():'';
            var selectedRecArray = this.grid.getSelectionModel().getSelections();
            var payments = "";
            var paymentsOfFutureDate="";
            var isAnyRecordNotMatchWithGenerationDate = true;
            if(selectedRecArray.length>0){
                for(var i=0;i<selectedRecArray.length;i++){
                    if(bankType == Wtf.IBGBanks.DBSBank){
                        if(this.genDate.getValue().format('Y-m-d') == selectedRecArray[i].get('valuedate').format('Y-m-d')){
                            payments+=selectedRecArray[i].get('billid')+",";
                            isAnyRecordNotMatchWithGenerationDate = false;
                        }
                    } else {
                        if(this.CIMBGenDate.getValue().format('Y-m-d') < selectedRecArray[i].get('billdate').format('Y-m-d')){
                            paymentsOfFutureDate+='<b>'+selectedRecArray[i].get('billno')+'</b>'+", ";
                        } else {
                            payments+=selectedRecArray[i].get('billid')+",";
                        }
                        }
                    }
                if(paymentsOfFutureDate != ''){
                    paymentsOfFutureDate = paymentsOfFutureDate.substring(0,paymentsOfFutureDate.length-2);
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.cimb.futurePayments")+'<br>'+paymentsOfFutureDate],0);
                    return;
                }
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")],0);
                return;
            }
            if(bankType == Wtf.IBGBanks.DBSBank){   
                if(isAnyRecordNotMatchWithGenerationDate){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.ibgBankDetail.generation.date")],0);
                }
                if(this.totalAmt.getValue()>this.dailyLimit.getValue()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"),WtfGlobal.getLocaleText("acc.field.ibgBankDetail.totalamount")],0);
                    return;
                }
            }

            if(payments != "" && payments.length>0){
                payments=payments.substring(0,payments.length-1)
            }

            var valueDate = this.genDate.getValue().format('ymd');
            var genDate='';
            if(bankType==Wtf.IBGBanks.DBSBank){
                genDate = this.genDate.getValue().format('Y-m-d');
            } else if(bankType == Wtf.IBGBanks.CIMBBank){
                genDate = WtfGlobal.convertToGenericDate(this.CIMBGenDate.getValue());
            }   

            var accountId = this.pmtBank.getValue();

            // Decide the URL according to selected bank
            var url='';
            if(bankType==Wtf.IBGBanks.DBSBank){
                url='ACCCombineReports/generateIBGFile.do';
            } else if(bankType == Wtf.IBGBanks.CIMBBank) {
                url= 'ACCCombineReports/generateGIORFileForCIMBBank.do';
            }
            Wtf.get('downloadframe').dom.src = url+"?valueDate="+valueDate+"&accountId="+accountId+"&payments="+payments+"&genDate="+genDate+"&purposeCode="+purposeCode; 
        }
    },
    generateGIORFileForOCBCBank: function () {
        var payments = "";
        var paymentsOfFutureDate = "";
        var submissionDate = this.OCBCSubmissionDate.getValue();
        var selectedRecArray = this.grid.getSelectionModel().getSelections();
        if (selectedRecArray.length > 0) {
            for (var i = 0; i < selectedRecArray.length; i++) {
                if (submissionDate.format('Y-m-d') < selectedRecArray[i].get('billdate').format('Y-m-d')) {
                    paymentsOfFutureDate += '<b>' + selectedRecArray[i].get('billno') + '</b>' + ", ";
                } else {
                    payments += selectedRecArray[i].get('billid') + ",";
                }
            }
            if (paymentsOfFutureDate != '') {
                paymentsOfFutureDate = paymentsOfFutureDate.substring(0, paymentsOfFutureDate.length - 2);
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.cimb.futurePayments") + '<br>' + paymentsOfFutureDate], 0);
                return;
            }
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.block"), WtfGlobal.getLocaleText("acc.field.Pleaseselectarecordfirst")], 0);
            return;
        }
        if (payments != "" && payments.length > 0) {
            payments = payments.substring(0, payments.length - 1);
        }
        if (this.OCBCFormPanel.getForm().isValid()) {
            var accountId = this.pmtBank.getValue();
            submissionDate = submissionDate.format('Ymd');
            var valueDate = this.OCBCValueDate.getValue().format('dmY');
            var purposeCode = this.OCBCPurposeCode.getRawValue();
            var clearing = this.OCBCClearing.getRawValue();
            var batchNumber = "";
            var onBehalfOf = "";
            var valueTime = "";
            if (clearing == 'GIRO') {
                batchNumber = this.OCBCBatchNumber.getValue();
                onBehalfOf = this.OCBCOnBehalfOf.getValue();
            } else {
                this.OCBCValueTime.format="Hi";//Value time should be in 24 hour format e.g.2000
                valueTime = this.OCBCValueTime.getValue();
            }
            Wtf.get('downloadframe').dom.src = "ACCCombineReports/generateGIROFileForOCBCBank.do" + "?valueDate=" + valueDate + "&accountId=" + accountId + "&payments=" + payments + "&submissionDate=" + submissionDate + "&purposeCode=" + purposeCode + "&clearing=" + clearing + "&batchNumber=" + batchNumber + "&onBehalfOf=" + onBehalfOf + "&valueTime=" + valueTime;
        } else {
            WtfComMsgBox(2, 2);
        }
    },
    applyDateHandler:function(){
        var selectedRecArray = this.grid.getSelectionModel().getSelections();
        if(selectedRecArray.length>0){
            for(var i=0;i<selectedRecArray.length;i++){
                var valueDate = this.valueDate.getValue();
                var recId = selectedRecArray[i].get('billid');
                selectedRecArray[i].set('valuedate', valueDate);
                
            }
        }
    },
    
    // Hide/Show the fields according to selected bank
    redesignBottomPanel:function(obj,rec){
        var bankType = rec.data.ibgbanktype;
        if(bankType == Wtf.IBGBanks.DBSBank){
            this.southPanel.getLayout().setActiveItem(this.DBSBank);
            this.dailyLimit.setValue(rec.data.dailyBankLimit);
            this.ibgTransactionCode.enable();
            this.grid.getColumnModel().setHidden(6,false);
        } else if(bankType == Wtf.IBGBanks.CIMBBank){
            this.southPanel.getLayout().setActiveItem(this.CIMBBank);
            this.ibgTransactionCode.disable();
            this.grid.getColumnModel().setHidden(6,true);
        } else if (bankType == Wtf.IBGBanks.OCBCBank) {
            WtfGlobal.hideFormElement(this.OCBCValueTime);
            this.southPanel.getLayout().setActiveItem(this.OCBCBank);
            this.ibgTransactionCode.disable();
            this.grid.getColumnModel().setHidden(6,true);
        }
        this.fetchData();
        this.doLayout();
    },
    hideShowFieldsApplicableForGIROAndFast: function (obj, rec, index) {
        if (obj.getValue() == 1) {
            WtfGlobal.showFormElement(this.OCBCBatchNumber);
            WtfGlobal.showFormElement(this.OCBCOnBehalfOf);
            WtfGlobal.hideFormElement(this.OCBCValueTime);
        } else {
            WtfGlobal.hideFormElement(this.OCBCBatchNumber);
            WtfGlobal.hideFormElement(this.OCBCOnBehalfOf);
            WtfGlobal.showFormElement(this.OCBCValueTime);
        }
    },
     selectionChange:function(sm,rowIndex,record){
        var isSelect = sm.getSelected();
        var selectedRecArray = sm.getSelections();
        var totalAmt = 0;
        var rec = WtfGlobal.searchRecord(this.accStore, this.pmtBank.getValue(), "accid");
        var bankType = rec.data.ibgbanktype;
        if(selectedRecArray.length>0){
            for(var i=0;i<selectedRecArray.length;i++){
                totalAmt+= WtfGlobal.conventInDecimalWithoutSymbol(selectedRecArray[i].get('amountinbase'));
            }
        }
        totalAmt = WtfGlobal.conventInDecimalWithoutSymbol(totalAmt);
        if(isSelect){
            this.calculateAmount(sm,rowIndex,record,totalAmt,bankType);
            return;
        }
        if (bankType == Wtf.IBGBanks.DBSBank) {
            this.totalAmt.setValue(totalAmt);
        } else if (bankType == Wtf.IBGBanks.CIMBBank) {
            this.CIMBTotalAmt.setValue(totalAmt);
        } else if (bankType == Wtf.IBGBanks.OCBCBank) {
            this.OCBCTotalAmt.setValue(totalAmt);
        }
    },
    calculateAmount: function(sm,rowIndex,record,totalAmt,bankType){
        var amtInString='';
        var indexOfDecimal=0;
        amtInString = totalAmt+"";
        indexOfDecimal = amtInString.indexOf(".");
        if(indexOfDecimal != -1){
            amtInString = amtInString.substring(0,indexOfDecimal);
        }
        if(amtInString.length>13){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.cimb.totalAmountExceeding")],3);
            sm.deselectRow(rowIndex);
            return;
        }
        if (bankType == Wtf.IBGBanks.DBSBank) {
            this.totalAmt.setValue(totalAmt);
        } else if (bankType == Wtf.IBGBanks.CIMBBank) {
            this.CIMBTotalAmt.setValue(totalAmt);
        } else if (bankType == Wtf.IBGBanks.OCBCBank) {
            this.OCBCTotalAmt.setValue(totalAmt);
        }
    }
     
});
