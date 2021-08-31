Wtf.account.dealerExciseDetail = function (config){
    Wtf.apply(this, config);
    var hideManufacturerDetails = false;
    if (!Wtf.isEmpty(this.parentObj.defaultNatureOfPurchase)) {
        var indexDNOPAt = Wtf.defaultNatureOfPurchaseStore.find('id', this.parentObj.defaultNatureOfPurchase.getValue())
        if (indexDNOPAt != -1) {
            var DNOPDetails = Wtf.defaultNatureOfPurchaseStore.getAt(indexDNOPAt);
            if (DNOPDetails.data.defaultMasterItem == Wtf.DNOP.From_Agent_of_Manufacturer || DNOPDetails.data.defaultMasterItem == Wtf.DNOP.Manufacturer_Depot || DNOPDetails.data.defaultMasterItem == Wtf.DNOP.Manufacturer) {
                hideManufacturerDetails = true;
            }
        }
    }
    this.termRec =new Wtf.data.Record.create([
    {
        name: 'id'
    },

    {
        name: 'termid'
    },

    {
        name: 'term'
    },

    {
        name: 'termtype'
    },

    {
        name: 'formulaids'
    },

    {
        name: 'taxvalue'
    },

    {
        name: 'termamount'
    },

    {
        name: 'termpercentage'
    },
    {
        name: 'assessablevalue'
    },
    {
        name: 'manufactureTermAmount'
    }
    ]);
    
    var columnModelExcise = new Wtf.grid.ColumnModel([
        new Wtf.grid.RowNumberer(),
        {  
            header:WtfGlobal.getLocaleText("acc.invoice.grid.typeofduty"),//Type of Duty',
            width: 150, 
            sortable: true, 
            align:'center',
            dataIndex: 'term'
        },{
            header: WtfGlobal.getLocaleText("acc.invoice.grid.rateofduty"),//"Rate of Duty", 
            width: 150, 
            dataIndex: 'termpercentage',
            align:'right',
            summaryRenderer: function (value, m, rec) {
                return "<b>Total :</b>";
            }
        },{
            header: WtfGlobal.getLocaleText("acc.invoice.grid.dutyamount"),//"Duty Amount", 
            width: 150,  
            dataIndex: 'termamount',
            align:'right',
            summaryType: 'sum',
//            hidden:hideManufacturerDetails,
            renderer:WtfGlobal.withCurrencyUnitPriceRenderer,
            editor: this.dutyAmount = new Wtf.form.NumberField({
                name: 'termamount',
                emptyText:WtfGlobal.getLocaleText("acc.invoice.grid.addDutyamount")
            }),
            summaryRenderer: function (value, m, rec) {
                return "<b>" +WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"</b>";
            }
        },{
            header:WtfGlobal.getLocaleText("acc.invoice.grid.manuImpdutyamount"),// "Manufacture/Importer Duty Amount", 
            width: 150, 
            dataIndex: 'manufactureTermAmount',
            align:'right',
            summaryType: 'sum',
            hidden:hideManufacturerDetails,
            renderer:WtfGlobal.withCurrencyUnitPriceRenderer,
            editor: this.manuImpDutyAmount = new Wtf.form.NumberField({
                name: 'manufactureTermAmount',
                emptyText:WtfGlobal.getLocaleText("acc.invoice.grid.addManuImpdutyamount")
            }),
            summaryRenderer: function (value, m, rec) {
                return "<b>" +WtfGlobal.conventInDecimal(value,WtfGlobal.getCurrencySymbol())+"</b>";
            }
        }]);

    this.dealerExciseStore = new Wtf.data.Store({
        sortInfo: {
            field: 'term',
            direction: 'ASC'
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.termRec)
    });
    
    var ExciseDetails=eval(this.record.data.dealerExciseDetails);
    var excise_RG23DEntryNumber="";
    var excise_RG23DseqFormat="";
    var excise_detailsId="";
    var excise_seqnumber="";
    var excise_datePreffixValue="";
    var excise_dateSuffixValue="";
    var excise_sequenceformat="";
    var excise_SupplierRG23DEntry="";
    var excise_AssessableValue="";
    var excise_PLARG23DEntry="";
    var excise_ManuAssessableValue="";
    var excise_ManuInvoiceNumber="";
    var excise_ManuInvoiceDate="";
    if(!Wtf.isEmpty(ExciseDetails) && !Wtf.isEmpty(ExciseDetails[0])){
        excise_RG23DEntryNumber=!Wtf.isEmpty(ExciseDetails[0].RG23DEntryNumber)?ExciseDetails[0].RG23DEntryNumber:"";
        excise_RG23DseqFormat=!Wtf.isEmpty(ExciseDetails[0].RG23DseqFormat)?ExciseDetails[0].RG23DseqFormat:"";
        excise_detailsId=!Wtf.isEmpty(ExciseDetails[0].id)?ExciseDetails[0].id:"";
        excise_seqnumber=!Wtf.isEmpty(ExciseDetails[0].seqnumber)?ExciseDetails[0].seqnumber:"";
        excise_datePreffixValue=!Wtf.isEmpty(ExciseDetails[0].datePreffixValue)?ExciseDetails[0].datePreffixValue:"";
        excise_dateSuffixValue=!Wtf.isEmpty(ExciseDetails[0].dateSuffixValue)?ExciseDetails[0].dateSuffixValue:"";
        excise_sequenceformat=!Wtf.isEmpty(ExciseDetails[0].sequenceformat)?ExciseDetails[0].sequenceformat:"";
        excise_SupplierRG23DEntry=!Wtf.isEmpty(ExciseDetails[0].SupplierRG23DEntry)?ExciseDetails[0].SupplierRG23DEntry:"";
        excise_AssessableValue=!Wtf.isEmpty(ExciseDetails[0].AssessableValue)?(ExciseDetails[0].AssessableValue):(this.record!=undefined?this.record.data.amount:"");
        excise_PLARG23DEntry=!Wtf.isEmpty(ExciseDetails[0].PLARG23DEntry)?ExciseDetails[0].PLARG23DEntry:"";
        excise_ManuAssessableValue=!Wtf.isEmpty(ExciseDetails[0].ManuAssessableValue)?(ExciseDetails[0].ManuAssessableValue):(this.record!=undefined?this.record.data.amount:"");
        excise_ManuInvoiceNumber=!Wtf.isEmpty(ExciseDetails[0].ManuInvoiceNumber)?(ExciseDetails[0].ManuInvoiceNumber):"";
        excise_ManuInvoiceDate=!Wtf.isEmpty(ExciseDetails[0].ManuInvoiceDate)?(ExciseDetails[0].ManuInvoiceDate):"";
    }else{
        excise_AssessableValue =this.record.data.amount;
        excise_ManuAssessableValue =this.record.data.amount;
//        if(this.firstRow!=undefined && this.isEdit){
//            var firstRowExciseDetails=eval(this.firstRow.data.dealerExciseDetails);
//            excise_RG23DEntryNumber=!Wtf.isEmpty(firstRowExciseDetails[0].RG23DEntryNumber)?firstRowExciseDetails[0].RG23DEntryNumber:"";
//            excise_seqnumber=!Wtf.isEmpty(firstRowExciseDetails[0].seqnumber)?firstRowExciseDetails[0].seqnumber:"";
//            excise_datePreffixValue=!Wtf.isEmpty(firstRowExciseDetails[0].datePreffixValue)?firstRowExciseDetails[0].datePreffixValue:"";
//            excise_dateSuffixValue=!Wtf.isEmpty(firstRowExciseDetails[0].dateSuffixValue)?firstRowExciseDetails[0].dateSuffixValue:"";
//            excise_sequenceformat=!Wtf.isEmpty(firstRowExciseDetails[0].sequenceformat)?firstRowExciseDetails[0].sequenceformat:"";
//        }
    }  
    
    
    
    if (this.record != undefined){
        var termDetails = [];
        if(this.record.data.dealerExciseTerms != undefined && this.record.data.dealerExciseTerms != "" && this.record.data.dealerExciseTerms != "[]"){
            termDetails = this.record.data.dealerExciseTerms;
        }
        this.loadSavedTermDetails(eval(termDetails),excise_AssessableValue,excise_ManuAssessableValue);            
    }
    
    this.summary = new Wtf.grid.GroupSummary();
    this.GridSummary = new Wtf.ux.grid.GridSummary({});
    var grid = new Wtf.grid.EditorGridPanel({
        store: this.dealerExciseStore,
        cm: columnModelExcise,
        stripeRows: true,
        border : false,
        layout:'fit',
        loadMask : true,
        plugins:this.GridSummary,
        bodyStyle: 'padding:0px',
        emptyText:WtfGlobal.getLocaleText("acc.common.norec"),
        height:250,
        viewConfig:{
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }
    });
    this.defaultNatureofPurchaseID=!Wtf.isEmpty(this.parentObj.defaultNatureOfPurchase.getValue())?this.parentObj.defaultNatureOfPurchase.getValue():"";

    
    this.exciseDealerDetails= new Wtf.Window({
        modal: true,
        closeAction: 'hide',
        closable: false,
        id:'exciseDealerDetails'+this.id,
        title: WtfGlobal.getLocaleText("acc.invoice.grid.supplierExcisedetails"),//"Supplier Excise Details",
        iconCls: getButtonIconCls(Wtf.etype.deskera),
        buttonAlign: 'right',
        autoScroll:true,
        width: 800,
        height:500,               
        scope: this,
        items: [{
            region:"north",
            height:90,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml("Excise Details for : "+this.record.data.productname,((!Wtf.isEmpty(config.productDetails.data)&&!Wtf.isEmpty(config.productDetails.data.hsncode))?"<i>(Tariff Classification: "+config.productDetails.data.hsncode+")</i>":""),'../../images/accounting_image/tax.gif',true)
        },{
            region: "center",
            bodyStyle: 'padding:10px 10px 10px 10px;',
            baseCls:'bckgroundcolor',
            items:[
            this.terminfo = new Wtf.form.FormPanel({
                border:false,
                bodyStyle:"padding-left:10px;",
                labelWidth: 240,
                items:[
                this.RG23DseqFormat= new Wtf.form.TextField({
                    fieldLabel:" "+WtfGlobal.getLocaleText("acc.invoice.grid.rg23dseqFormat"),//"  RG 23D Sequence Format",
                    id:"RG23DseqFormat"+this.id,
                    name:"RG23DseqFormat",
                    width:240,
                    maxLength:200,
                    disabled:true,
                    hidden:this.isEdit?(this.isCopy?false:true):false,
                    hideLabel:this.isEdit?(this.isCopy?false:true):false,
                    value:excise_RG23DseqFormat,
                    sequenceformat:excise_sequenceformat,
                    exciseDetailsId:excise_detailsId,
                    seqnumber:excise_seqnumber,
                    datePreffixValue:excise_datePreffixValue,
                    dateSuffixValue:excise_dateSuffixValue
                }),
                this.RG23DEntryNumber= new Wtf.form.TextField({
                    fieldLabel:" "+WtfGlobal.getLocaleText("acc.invoice.grid.rg23dseqentrynumb"),//"  RG 23D Entry No.",
                    id:"RG23DEntryNumberid"+this.id,
                    name:"RG23DEntryNumber",
                    width:240,
                    maxLength:200,
                    disabled:true,
                    hidden:!(this.isEdit?(this.isCopy?false:true):false),
                    hideLabel:!(this.isEdit?(this.isCopy?false:true):false),
                    value:excise_RG23DEntryNumber,
                    sequenceformat:excise_sequenceformat,
                    exciseDetailsId:excise_detailsId,
                    seqnumber:excise_seqnumber,
                    datePreffixValue:excise_datePreffixValue,
                    dateSuffixValue:excise_dateSuffixValue
                })]
            }),
            this.exciseFieldsetSupplier=new Wtf.form.FieldSet({
                xtype: 'fieldset',
                title:WtfGlobal.getLocaleText("acc.invoice.grid.supplierinvoicedetails"),//"Supplier Invoice Details",
                height:165,
//                hidden:hideManufacturerDetails,
                labelWidth:240,
                items:[
                this.SupplierInvoiceNumber= new Wtf.form.TextField({
                    fieldLabel:"Supplier Invoice Number",
                    name:"SupplierInvoiceNumber",
                    id:"SupplierInvoiceNumberid"+this.id,
                    width:240,
                    disabled:true,
                    maxLength:200,
                    value:this.parentObj!=undefined?this.parentObj.Number.getValue():""
                }),
                this.SupplierInvoiceDate= new Wtf.form.DateField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.invoice.grid.supplierinvoicedate"),//"Supplier Invoice Date",
                    name:"SupplierInvoiceDate",
                    id:"SupplierInvoiceDateid"+this.id,
                    width:240,
                    maxLength:200,
                    disabled:true,
                    format:WtfGlobal.getOnlyDateFormat(),
                    value:this.parentObj!=undefined?this.parentObj.billDate.getValue():undefined
                }),
                this.SupplierRG23DEntry= new Wtf.form.TextField({
                    fieldLabel:hideManufacturerDetails?WtfGlobal.getLocaleText("acc.invoice.grid.PLARG23Dnumb"):WtfGlobal.getLocaleText("acc.invoice.grid.supplierrg23dentrynumb"),//"Supplier RG 23D Entry No.",
                    name:"SupplierRG23DEntry",
                    id:"SupplierRG23DEntryid"+this.id,
                    width:240,
                    disabled:this.viewMode,
                    maxLength:200,
                    value:excise_SupplierRG23DEntry
                }),
                this.AssessableValue= new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.serviceTaxComputationReport.assessableValue"),//"Assessable Value",
                    name:"AssessableValue",
                    id:"AssessableValueid"+this.id,
                    width:240,
                    disabled:this.viewMode,
                    maxLength:200,
                    value:excise_AssessableValue
                })
                ]        
            }),
            this.exciseFieldsetSupplier=new Wtf.form.FieldSet({
                xtype: 'fieldset',
                title:WtfGlobal.getLocaleText("acc.invoice.grid.manuImpInvDetails"),//"Manufacture/Importer Invoice Details",
                height:165,
                labelWidth:240,
                hidden:hideManufacturerDetails,
                items:[
                this.ManuInvoiceNumber= new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.agedPay.gridIno"),//" Invoice Number",
                    name:"InvoiceNumber",
                    id:"InvoiceNumberid"+this.id,
                    width:240,
                    maxLength:200,
                    disabled: this.viewMode,
                    value:excise_ManuInvoiceNumber
                }),
                this.ManuInvoiceDate= new Wtf.form.DateField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.rem.34"),//" Invoice Date",
                    name:"InvoiceDate",
                    id:"InvoiceDate"+this.id,
                    width:240,
                    maxLength:200,
                    disabled:this.viewMode,
//                    disabled:false,
                    format:WtfGlobal.getOnlyDateFormat(),
                    value:excise_ManuInvoiceDate
                }),
                this.PLARG23DEntry= new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.invoice.grid.PLARG23Dnumb"),//"PLA/RG 23D Entry No.",
                    name:"PLARG23DEntry",
                    id:"PLARG23DEntry"+this.id,
                    width:240,
                    disabled:this.viewMode,
                    maxLength:200,
                    value:excise_PLARG23DEntry
                }),{
                    layout:'column',
                    border:false,
                    defaults:{
                        border:false
                    },
                    items:[ {
                        layout:'form',
                        ctCls :"",
                        labelWidth:240,
                        items:[
                        this.billedQty= new Wtf.form.TextField({
                            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.grid.billQty"),//"Billed Qty.",
                            name:"billedQty",
                            id:"billedQty"+this.id,
                            width:200,
                            maxLength:200,
                            disabled:true,
                            value:this.record!=undefined?this.record.data.baseuomquantity:""
                        })]
                    },{
                        layout:'form',
                        ctCls :"",
                        labelWidth:45,
                        bodyStyle:"padding-left:10px;",
                        items:[
                        this.uomPanelValue = new Wtf.Panel({
                            xtype: 'panel', 
                            border: false,
                            html:this.record.data.baseuomname
                        })]
                    }]
                },
                this.ManuAssessableValue= new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.serviceTaxComputationReport.assessableValue"),//"Assessable Value",
                    id:"ManuassessableValue"+this.id,
                    width:240,
                    disabled:this.viewMode,
                    maxLength:200,
                    value:excise_ManuAssessableValue
                })]                
            }),
            grid
            ]
        }],
        buttons:
        [{
            text:WtfGlobal.getLocaleText("acc.het.524"),// 'Save',
            scope:this,
            hidden:this.viewMode,
            handler: function()
            {   
                var arr = [];
                this.recArr = grid.getStore().data.items;
                for (var i = 0; i < this.recArr.length; i++) {
                    if(this.recArr[i] != undefined){
                        arr.push(grid.getStore().indexOf(this.recArr[i]));
                    }
                }
                var data = WtfGlobal.getJSONArray(grid, true, arr);
                var rec=this.record;
                var json=[];
                json.push({
                    "RG23DEntryNumber":this.RG23DEntryNumber.getValue(),
                    "RG23DseqFormat":this.RG23DseqFormat.getValue(),
                    "sequenceformat":Wtf.getCmp('RG23DEntryNumberid'+this.id).sequenceformat,
                    "id":Wtf.getCmp('RG23DEntryNumberid'+this.id).exciseDetailsId,
                    "seqnumber":Wtf.getCmp('RG23DEntryNumberid'+this.id).seqnumber,
                    "datePreffixValue":Wtf.getCmp('RG23DEntryNumberid'+this.id).datePreffixValue,
                    "dateSuffixValue":Wtf.getCmp('RG23DEntryNumberid'+this.id).dateSuffixValue,
                    "SupplierRG23DEntry":this.SupplierRG23DEntry.getValue(),
                    "AssessableValue":this.AssessableValue.getValue(),
                    "PLARG23DEntry":this.PLARG23DEntry.getValue(),
                    "ManuAssessableValue":this.ManuAssessableValue.getValue(),
                    "ManuInvoiceNumber":this.ManuInvoiceNumber.getValue(),
                    "ManuInvoiceDate": !Wtf.isEmpty(this.ManuInvoiceDate.getValue()) ?WtfGlobal.convertToDateOnly(this.ManuInvoiceDate.getValue()): "",
                    "dealerExciseTerms":data,
                    "company":companyid
                });
                rec.set("dealerExciseDetails", JSON.stringify(json));   
                this.exciseDealerDetails.close();
            }
        },{
            text:!this.viewMode?WtfGlobal.getLocaleText('acc.common.close'):WtfGlobal.getLocaleText('acc.field.Cancel'),//'Close/Cancel',
            scope:this,
            handler: function()
            {
                this.exciseDealerDetails.close();
            }
        }]
    });
    this.exciseDealerDetails.show();
    this.AssessableValue.on('blur',function(){
        this.onBlurAssessableValue(grid);
    },this);
    this.ManuAssessableValue.on('blur',function(){
        this.onBlurAssessableValue(grid);
    },this);

    Wtf.account.dealerExciseDetail.superclass.constructor.call(this,config);
}
Wtf.extend(Wtf.account.dealerExciseDetail,Wtf.Panel,{
    onRender:function (config){
        Wtf.account.dealerExciseDetail.superclass.onRender.call(this,config);
        
    },
    loadSavedTermDetails:function(termDetails,assessableValueSupplier,manufactureValueSupplier) {
        var recordQuantity = termDetails.length;
        if (recordQuantity != 0) {
            for (var i=0; i<recordQuantity; i++) {
                var termRecord = termDetails[i];
                if(Wtf.isEmpty(assessableValueSupplier)){
                    assessableValueSupplier=this.record.data.amount; 
                }
                if(Wtf.isEmpty(manufactureValueSupplier)){
                    manufactureValueSupplier=this.record.data.amount; 
                }
                if(termRecord.termtype==Wtf.term.Excise){
                    var rec = new this.termRec(termRecord);
                    rec.beginEdit();
                    var fields = this.dealerExciseStore.fields;
                    if (this.record.data.dealerExciseDetails != "" && this.record.data.dealerExciseDetails != undefined) {
                        var dealerExciseDetails = eval(this.record.data.dealerExciseDetails);
                        if (dealerExciseDetails != "" && dealerExciseDetails != undefined) {
                            var prevDealerExciseTerms = eval(dealerExciseDetails[0].dealerExciseTerms);
                            if (prevDealerExciseTerms != "" && prevDealerExciseTerms != undefined) {
                                var prevManufactureTermAmount = prevDealerExciseTerms[i].manufactureTermAmount;
                                var prevTermAmount = prevDealerExciseTerms[i].termamount;
                            }
                        }
                    }
                    
                    for (var x=0; x<fields.items.length; x++) {
                        var value = termRecord[fields.get(x).name];
                        if (fields.get(x).name == 'termamount') {
                            if (prevTermAmount != undefined && prevTermAmount != "" && prevTermAmount > 0) {
                                value = prevTermAmount;
                            } else if (value && value != '' && (value > 0)) {
                                //In edit case, set only previous saved duty amount.
                                value = getRoundedAmountValue(decodeURI(value));
                            } else if (termRecord.taxtype == 1) {
                                value = getRoundedAmountValue(assessableValueSupplier * termRecord.termpercentage / 100);
                            }
                        }
                        if (fields.get(x).name == 'manufactureTermAmount') {
                            if(termRecord.taxtype==1){
                                if (prevManufactureTermAmount != undefined && prevManufactureTermAmount != "" && prevManufactureTermAmount > 0) {
                                    value = prevManufactureTermAmount;
                                } else if (value && value != '') {
                                    //In edit case, set only previous saved Manufacture/Importer duty amount.
                                    value = getRoundedAmountValue(decodeURI(value));
                                } else {
                                    value = getRoundedAmountValue(manufactureValueSupplier * termRecord.termpercentage / 100);
                                }
                            }else{
                                 value=getRoundedAmountValue(termRecord.termamount);
                            }
                        }
                        if (fields.get(x).name == 'termpercentage' && value && value != '') {
                            value = decodeURI(value);
                        }
                        rec.set(fields.get(x).name, value);
                    }
                    rec.endEdit();
                    rec.commit();
                    this.dealerExciseStore.add(rec);
                }
            }
        }
    },
    onBlurAssessableValue:function(grid) {
        if (this.record != undefined){
            var termDetails = [];
            if(this.record.data.dealerExciseTerms != undefined && this.record.data.dealerExciseTerms != "" && this.record.data.dealerExciseTerms != "[]"){
                termDetails = this.record.data.dealerExciseTerms;
            }
            grid.store.removeAll();
            this.loadSavedTermDetails(eval(termDetails),this.AssessableValue.getValue(),this.ManuAssessableValue.getValue());            
        }
    } 
});
