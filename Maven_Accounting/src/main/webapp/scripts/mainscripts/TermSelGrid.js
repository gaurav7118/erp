Wtf.account.TermSelGrid = function (config){
    this.isEdit = config.isEdit;
    this.currencySymbol = config.currencySymbol;
    //Flag to indicate whether Avalara integration is enabled and module is enabled for Avalara Integration or not
    this.isModuleForAvalara = (Wtf.account.companyAccountPref.avalaraIntegration && config.gridObj && config.gridObj.isModuleForAvalara) ? true : false;
    Wtf.apply(this, config);
    Wtf.account.TermSelGrid.superclass.constructor.call(this);
}
Wtf.extend(Wtf.account.TermSelGrid,Wtf.Panel,{
    initComponent:function (){
        Wtf.account.TermSelGrid.superclass.initComponent.call(this);
  
        this.isCallFromCompany = this.isCallFromCompany == undefined ? false : this.isCallFromCompany ;
        this.isCNDNOverUnderCharge = this.isCNDNOverUnderCharge == undefined ? false : this.isCNDNOverUnderCharge ;
        this.isViewTemplate=this.isViewTemplate(this);
        this.signStore=new Wtf.data.SimpleStore({
            fields : ['id','sign'],
            data : [['0',WtfGlobal.getLocaleText("acc.field.Minus-")],['1',WtfGlobal.getLocaleText("acc.field.Plus+")]]
        });
        
        this.signCombobox = new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'id',
            displayField:'sign',
            store:this.signStore
        });
        
        this.formulaRec=new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'term'
        }
        ]);
        this.formulaStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.formulaRec),
            url: 'ACCAccount/getIndianTermsCompanyLevel.do',
            baseParams:{
                isSalesOrPurchase:this.checktype(),
                isNewGST:WtfGlobal.GSTApplicableForCompany()==Wtf.GSTStatus.NEW?true:false
            }
        });
        this.formulaStore.on("load",function(store, rec, options){
            var blankObj = new Wtf.data.Record({
                id: "Basic",
                term: "Basic"
            });
            
            this.formulaStore.insert(0,blankObj);
        },this);
        
        this.formulaStore.load();
        
        this.FormulaComboconfig = {
            hiddenName:"formula",
            store: this.formulaStore,
             hidelabel:true,
            valueField:'id',
            displayField:'term',
            emptyText:WtfGlobal.getLocaleText("acc.field.Selectformula"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus:true,                            
            allowBlank:false,
            triggerAction:'all',
            scope:this,
            hidden:true
        };

        this.formulaCombo = new Wtf.common.Select(Wtf.applyIf({
            multiSelect:true,
            hidelabel:true,
            fieldLabel:WtfGlobal.getLocaleText("acc.master.invoiceterm.formula") ,
            forceSelection:true,
            width: 200,
            hidden:true
        //            anchor:'100%'
        },this.FormulaComboconfig));

        this.createFields();
        
        this.termRec =new Wtf.data.Record.create([

        {name: 'id'},
        {name: 'termid'},
        {name:'productentitytermid'},
        {name: 'term'},
        {name: 'termtype'},
        {name: 'glaccount'},
        {name:'payableaccountid'},
        {name:'creditnotavailedaccount'},
        {name:'creditnotavailedaccountname'},
        {name: 'sign'},
        {name: 'formula'},
        {name: 'termformulaids'},
        {name: 'formulaids'},
        {name: 'taxtype'},
        {name: 'taxvalue'},
        {name: 'termamount'},
        {name: 'termpercentage'},
        {name: 'invquantity'},
        {name: 'invAmount'},
        {name: 'recTermAmount'},
        {name: 'OtherTermNonTaxableAmount'},
        {name: 'productid'},
        {name: 'assessablevalue'},
        {name: 'glaccountname'},
        {name:'payableglaccountname'},
        {name: 'accountid'},
        {name: 'isDefault'},
        {name: 'producttermmapid'},
        {name: 'purchasevalueorsalevalue'},
        {name: 'deductionorabatementpercent'},
        {name: 'termsequence'},
        {name: 'formType'},
        {name: 'isadditionaltax'},
        {name: 'includeInTDSCalculation'},
        {name: 'IsOtherTermTaxable'},
        {name: 'isTermTaxable'},
        {name: 'masteritem'},
        {name:  Wtf.GST_CESS_TYPE },
        {name: Wtf.GST_CESS_VALUATION_AMOUNT },
        {name: Wtf.DEFAULT_TERMID }


            ]);
        var groupable=false; // defaule not groupable
        
        if(this.isLineLevel){ // Call from invoiceGrid.js (All terms are in one grid so differenciate by groupping)
            groupable =false; 
        }else{
            if(!Wtf.isEmpty(this.termType)&& (this.termType == Wtf.term.CST||this.termType == Wtf.term.Service_Tax || this.isLineLevel)){ // from productformGroup.js OR from TermSelGrid // Different grid for different Terms
                groupable =true; 
            }
        }
        
        this.termStore = groupable?new Wtf.data.GroupingStore({
            url: 'ACCAccount/getIndianTermsCompanyLevel.do',
            baseParams:{
                isSalesOrPurchase:this.checktype(),                
                isAdditionalTax:this.isAdditionalTax?true:false,
                termType:!Wtf.isEmpty(this.termType)?this.termType:""
            },
            sortInfo: {
                field: 'termsequence',
                direction: 'ASC'
            },
            groupField : !this.isLineLevel && this.termType==Wtf.term.CST?'formType':'termsequence',
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.termRec)
        }):
        this.termStore = new Wtf.data.Store({
            url: 'ACCAccount/getIndianTermsCompanyLevel.do',
            baseParams:{
                isSalesOrPurchase:this.checktype(),                
                isAdditionalTax:this.isAdditionalTax?true:false,
                termType:!Wtf.isEmpty(this.termType)?this.termType:""
            },
            sortInfo: {
                field: 'termsequence',
                direction: 'ASC'
            },
//            groupField : 'termsequence',
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.termRec)
        });
        this.termStore.on("loadexception", function () {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.mp.unableToLoadData")], 1);
        }, this)
        this.FormSelectionStore = new Wtf.data.SimpleStore({
            fields: [{
                name:'id',
                type:'string'
            }, 'name'],
            data :[
            ["1","Without Form"],
            ["2","C Form"],
            ["3","E1 Form"],
            ["4","E2 Form"],
            ["5","F Form"],
            ["6","H Form"],
            ["7","I Form"],
            ["8","J Form"]]
        });
        this.formTypeCombo = new Wtf.form.ComboBox({
            store:this.FormSelectionStore,
            fieldLabel:"Form Type",
            name: 'formType',
            hiddenName: 'formType',
            displayField:'name',
            valueField:'id',
            //            width:183,
            //            listWidth:183,
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true           
        });
        //        this.termStore.on('load',this.setTermRows,this);
        this.accStore.on('load',function(){
            this.itemsgrid.getView().refresh();
        },this);
        if (this.record != undefined){
            var termDetails = [];
            if(this.record.data.LineTermdetails != undefined && this.record.data.LineTermdetails != "" && this.record.data.LineTermdetails != "[]"){
                termDetails = this.record.data.LineTermdetails;
            }else if(!this.isAdditionalTax && (this.record.data.ProductTermSalesMapp != undefined && this.record.data.ProductTermSalesMapp != "") && (this.type!=undefined &&this.type=="productTermSalesGrid")){
                termDetails = this.record.data.ProductTermSalesMapp; 
                if(this.isProduct && Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA){
                    termDetails = this.filterProductJSON(termDetails,this.termType); // Filter Terms as per request from grid
                }
            }else if(this.isAdditionalTax && (this.record.data.ProductTermAdditionalSalesMapp != undefined && this.record.data.ProductTermAdditionalSalesMapp != "") && (this.type!=undefined &&this.type=="productTermSalesGrid")){
                termDetails = this.record.data.ProductTermAdditionalSalesMapp;                 
            }else if(!this.isAdditionalTax &&(this.record.data.ProductTermPurchaseMapp != undefined && this.record.data.ProductTermPurchaseMapp != "") && (this.type!=undefined &&this.type=="productTermPurchaseGrid")){               
                termDetails = this.record.data.ProductTermPurchaseMapp; 
                if(this.isProduct && Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA){
                    termDetails = this.filterProductJSON(termDetails,this.termType);// Filter Terms as per request from grid productFormgGoup.js
                }
            } else if(this.isAdditionalTax && (this.record.data.ProductTermAdditionalPurchaseMapp != undefined && this.record.data.ProductTermAdditionalPurchaseMapp != "") && (this.type!=undefined &&this.type=="productTermPurchaseGrid")){
                termDetails = this.record.data.ProductTermAdditionalPurchaseMapp; 
            }
            this.loadSavedTermDetails(eval(termDetails));            
            if(termDetails.length == 0){
                if(!this.isLineLevel){
                    this.termStore.load();
                }
                // For actual document creation if no Product tax present for selected product then dont load all terms/ tax just leave it blank grid.
                // this condition applied for all Modules (SO,PO,SI,DO,PR,SR,GR,CP,CS etc.)
            }
        } else {
            this.termStore.load();
        }
        
        var currencySymbolLocal = this.currencySymbol;
        this.sm1=new Wtf.grid.CheckboxSelectionModel();
        this.termcm = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            this.sm1,
            {
                header: "<div wtf:qtip='"+ WtfGlobal.getLocaleText("acc.termselgrid.taxdescription") +"'>"+ WtfGlobal.getLocaleText("acc.termselgrid.taxdescription") +"</div>",//"Tax Description"
                dataIndex: 'termtype',
                width:50,
//                groupable: true,
//                editor:this.rowTermTypeCmb,
                hidden:true,
                renderer:Wtf.comboBoxRenderer(this.rowTermTypeCmb)
            },
            {
                header: "<div wtf:qtip='"+ WtfGlobal.getLocaleText("acc.termselgrid.taxdescription") +"'>"+ WtfGlobal.getLocaleText("acc.termselgrid.taxdescription") +"</div>",//"Tax Description"
                dataIndex: 'masteritem',
                width:50,
                hidden:true
            },
            {
                header: WtfGlobal.getLocaleText("acc.termselgrid.taxdescription"),//"Tax Description"
                dataIndex: 'termsequence',
                width:50,
                groupable: this.termType==Wtf.term.Service_Tax || this.isLineLevel, // If service tax OR call from invoiceGrid.js than group by this column (for Linelevel term or Service|SBC|KKC term)
                hidden:true,
                renderer:Wtf.comboBoxRenderer(this.rowTermSequenceCmb)
            },{
                header: "Form Type",
                dataIndex: 'formType',
                width:50,
                groupable: (this.termType==Wtf.term.CST)&&!this.isLineLevel, // // If CST term OR call not from invoiceGrid.js than group by this column (for company level term or product level term)
                hidden:true,
                renderer:Wtf.comboBoxRenderer(this.formTypeCombo)
            },
            {
                header: "<div wtf:qtip='"+ (Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA?WtfGlobal.getLocaleText("acc.termselgrid.taxoradditioncharges"):WtfGlobal.getLocaleText("acc.common.Term")) +"'>"+ (Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA?WtfGlobal.getLocaleText("acc.termselgrid.taxoradditioncharges"):WtfGlobal.getLocaleText("acc.common.Term")) +"</div>",//"Term"
                dataIndex: 'term',
                width:50,
                renderer:this.TermRenderer
            },
            {
                header:"<div wtf:qtip='"+ (Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA?WtfGlobal.getLocaleText("acc.termselgrid.taxtypeoradditionchargestype"):WtfGlobal.getLocaleText("acc.termselgrid.taxtype")) +"'>"+ (Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA?WtfGlobal.getLocaleText("acc.termselgrid.taxtypeoradditionchargestype"):WtfGlobal.getLocaleText("acc.termselgrid.taxtype")) +"</div>",//"Tax Type"
                dataIndex: 'taxtype',
                width:50,
                /**
                 * make column non-editable when Avalara Integration is enabled
                 * this is because user must not be allowed to edit tax details in the window when Avalara Integration is on
                 */
                editor:this.isModuleForAvalara ? "" : this.rowTaxTypeCmb,
                hidden:this.isCallFromCompany,  //Making this column Hidden in Case of System Preferences & will be visible in Transactions..
                renderer:Wtf.comboBoxRendererForCess(this.rowTaxTypeCmb)// for cess types like ( value per thousand + cess %,Value per Thousand or CESS % whichever is higher etc.) for india ERP-37785
            },
            {
                header:"<div wtf:qtip='"+ WtfGlobal.getLocaleText("acc.termselgrid.amount") +"'>"+ WtfGlobal.getLocaleText("acc.termselgrid.amount") +"</div>" ,//"Amount"
                dataIndex: 'taxvalue',
                width:50,
                hidden:this.isCallFromCompany,  //Making this column Hidden in Case of System Preferences & will be visible in Transactions..
                align:'right',
                renderer:function(v,m,rec){
                    if(rec.data.taxtype ===1 && v > 100){ //1= for Percentage : 0 for Flat
                        v = rec.data.termpercentage;
                        rec.data.taxvalue = rec.data.termpercentage;
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.termselgrid.amount.percentage.error")],2);
                    }
                     
                     if(rec.data.discountispercent) {
                         v= v + "%";
                     }/* else {
                         var symbol = WtfGlobal.getCurrencySymbol();
                         if(rec.data['currencysymbol']!=undefined && rec.data['currencysymbol']!=""){
                             symbol = rec.data['currencysymbol'];
                         }

                         v= WtfGlobal.conventInDecimal(v,symbol)
                     }*/
                    return v;
                },
                /**
                 * make column non-editable when Avalara Integration is enabled
                 * this is because user must not be allowed to edit tax details in the window when Avalara Integration is on
                 */
                editor:this.isModuleForAvalara ? "" : this.rowTaxValue
            },
//            {
            //                header: WtfGlobal.getLocaleText("acc.je.acc"),
            //                dataIndex: 'accountid',
            //                editor:this.cmbAccount,
            //                width:50,
            //                renderer:Wtf.getComboNameRenderer(this.cmbAccount)
            ////                hidden:true
            //            },
            {
                header: "<div wtf:qtip='"+ WtfGlobal.getLocaleText("acc.master.invoiceterm.formula") +"'>"+ WtfGlobal.getLocaleText("acc.master.invoiceterm.formula") +"</div>",
                dataIndex: 'formulaids',
                editor:(this.type=='productTermSalesGrid' || this.type=='productTermPurchaseGrid')?"":this.formulaCombo,
                renderer:Wtf.MulticomboBoxRenderer(this.formulaCombo),
                width:50,
                hidden:true
            },{
                header: "<div wtf:qtip='"+ WtfGlobal.getLocaleText("acc.master.invoiceterm.calsign") +"'>"+ WtfGlobal.getLocaleText("acc.master.invoiceterm.calsign") +"</div>",
                dataIndex: 'sign',
                renderer:function(val){
                    if(val=='1')
                        return 'Plus(+)'
                    else
                        return 'Minus(-)'
                },
//                editor:this.signCombobox, // Hide
                width:50,
                hidden:true //(this.type=='productTermSalesGrid' || this.type=='productTermPurchaseGrid')
                },
            {
                header:"<div wtf:qtip='"+ WtfGlobal.getLocaleText("acc.termselgrid.taxamount") +"'>"+ WtfGlobal.getLocaleText("acc.termselgrid.taxamount") +"</div>",//"Tax Amount",
                dataIndex:"termamount",
                align:'right',
                width:50,
                hidden: (this.type == 'productTermSalesGrid' || this.type == 'productTermPurchaseGrid' || this.isCallFromCompany),
                editor: this.isModuleForAvalara ? "" : new Wtf.form.NumberField({
                    xtype: "numberfield",
                    maxLength: 15,
                    allowNegative: false,
                    decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
                }),
                renderer:function(v,m,rec){
                    if (this.hideUnitPriceAmount) {//If user does not have permission to view unit price, then tax amount is also hidden from user
                        return Wtf.UpriceAndAmountDisplayValue;
                    } else {
                        var symbol = (!Wtf.isEmpty(currencySymbolLocal)) ? currencySymbolLocal : WtfGlobal.getCurrencySymbol();
                        v= WtfGlobal.conventInDecimal(v,symbol);
                        m.attr = 'wtf:qtip="'+v+'"';
                        return v;
                    }
                }.createDelegate(this)
            },{
                header:"<div wtf:qtip='"+ WtfGlobal.getLocaleText("acc.invoice.gridQty") +"'>"+ WtfGlobal.getLocaleText("acc.invoice.gridQty") +"</div>",//"Quantity",
                dataIndex:"invquantity",
                align:'right',
                hidden:true,
//                hidden:(this.type=='productTermSalesGrid' || this.type=='productTermPurchaseGrid' || this.isCallFromCompany)?true:false,
                width:50
            },{
                header:"<div wtf:qtip='"+ WtfGlobal.getLocaleText("acc.invoice.gridInvAmt") +"'>"+ WtfGlobal.getLocaleText("acc.invoice.gridInvAmt") +"</div>",//"Invoice Amount",
                dataIndex:"invAmount",
                align:'right',
                hidden:true,
//                hidden:(this.type=='productTermSalesGrid' || this.type=='productTermPurchaseGrid' || this.isCallFromCompany)?true:false,
                width:50
            },{
                header:"<div wtf:qtip='"+ WtfGlobal.getLocaleText("acc.je.acc") +"'>"+ WtfGlobal.getLocaleText("acc.je.acc") +"</div>" ,//'Account'
                dataIndex: 'glaccount',
                /**
                 * make column non-editable when Avalara Integration is enabled
                 * this is because user must not be allowed to edit tax details in the window when Avalara Integration is on
                 */
                editor:this.isModuleForAvalara ? "" : this.cmbAccount,
                width:50,
                renderer:Wtf.comboBoxRenderer(this.cmbAccount)
            },{
                header:"<div wtf:qtip='"+ WtfGlobal.getLocaleText("acc.termsel.PayableAccount") +"'>"+ WtfGlobal.getLocaleText("acc.termsel.PayableAccount") +"</div>" ,//'Account'
                dataIndex: 'payableaccountid',
                /**
                 * make column non-editable when Avalara Integration is enabled
                 * this is because user must not be allowed to edit tax details in the window when Avalara Integration is on
                 */
                editor:this.isModuleForAvalara ? "" : this.cmbpayableAccount,
                width:50,
                renderer:Wtf.comboBoxRenderer(this.cmbpayableAccount)
            },{
                header:"<div wtf:qtip='"+ this.isCallFromCompany ? WtfGlobal.getLocaleText("acc.termselgrid.IsDefault"):WtfGlobal.getLocaleText("acc.termselgrid.IsApplicable") +"'>"+ this.isCallFromCompany ? WtfGlobal.getLocaleText("acc.termselgrid.IsDefault"):WtfGlobal.getLocaleText("acc.termselgrid.IsApplicable") +"</div>" ,
                dataIndex: 'isDefault',
                width:30,
//                hidden:(this.isProduct || this.isCallFromCompany)?false :true,
                hidden:true,
                renderer: function (v, p) {
                    p.css += ' x-grid3-check-col-td';
                    return '<div class="x-grid3-check-col' + (v ? '-on' : '') + ' x-grid3-cc-' + this.id + '">&#160;</div>';
            }
            },{
                header:"<div wtf:qtip='"+ WtfGlobal.getLocaleText("acc.TermSelGrid.includeInTDSCalculation") +"'>"+ WtfGlobal.getLocaleText("acc.TermSelGrid.includeInTDSCalculation") +"</div>" ,
                dataIndex: 'includeInTDSCalculation',
                width:30,
//                hidden:(this.isCallFromCompany && !this.isCustomer)?false :true,
                hidden:true,
                renderer: function (v, p) {
                    p.css += ' x-grid3-check-col-td';
                    return '<div class="x-grid3-check-col' + (v ? '-on' : '') + ' x-grid3-cc-' + this.id + '">&#160;</div>';
                }
            },{
                header:"<div wtf:qtip='"+ WtfGlobal.getLocaleText("acc.master.invoiceterm.isIsOtherTermTaxable") +"'>"+ WtfGlobal.getLocaleText("acc.master.invoiceterm.isIsOtherTermTaxable") +"</div>" ,
                dataIndex: 'isTermTaxable',
                width:30,
                disabled: true,
                hidden:((this.isProduct || this.isCallFromCompany) && this.isOtherChargesTermGrid)?false :true,
                renderer: function (v, p) {
                    p.css += ' x-grid3-check-col-td';
                    return '<div class="x-grid3-check-col' + (v ? '-on' : '') + ' x-grid3-cc-' + this.id + '">&#160;</div>';
                }
            },{
                header:"<div wtf:qtip='"+ WtfGlobal.getLocaleText("acc.termselgrid.assessablevalue") +"'>"+ WtfGlobal.getLocaleText("acc.termselgrid.assessablevalue") +"</div>",//"Assessable Value",
                dataIndex:"assessablevalue",
                align:'right',
                width:50,
                hidden:(this.type=='productTermSalesGrid' || this.type=='productTermPurchaseGrid' || this.isCallFromCompany),
                /**
                 * make column non-editable when Avalara Integration is enabled
                 * this is because user must not be allowed to edit tax details in the window when Avalara Integration is on
                 */
                editor:this.isModuleForAvalara ? "" : new Wtf.form.NumberField({
                    xtype : "numberfield", 
                    maxLength : 15,
                    allowNegative : true,
                    regexText:Wtf.MaxLengthText+"15"
                }),
                renderer:function(v,m,rec){
                    if (this.hideUnitPriceAmount) {//If user does not have permission to view unit price, then assessable amount is also hidden from user
                        return Wtf.UpriceAndAmountDisplayValue;
                    } else {
                        var symbol = (!Wtf.isEmpty(currencySymbolLocal)) ? currencySymbolLocal : WtfGlobal.getCurrencySymbol();
                        v= WtfGlobal.conventInDecimal(v,symbol);
                        m.attr = 'wtf:qtip="'+v+'"';
                        return v;
                    }
                }.createDelegate(this)
            },
            {
                header:WtfGlobal.getLocaleText("acc.masterConfig.costCenter.action"),  //'Action',
                dataIndex: 'deleteRecord',
                width:20,
                /**
                 * hide column when Avalara Integration is enabled
                 * this is because user must not be allowed to edit tax details in the window when Avalara Integration is on
                 *
                 *In CN/DN Overcharge/Undercharge tax can't edit for US country
                 */
                hidden: (this.isModuleForAvalara || this.isCNDNOverUnderCharge || (!this.isCallFromCompany ? (WtfGlobal.isUSCountryAndGSTApplied() && !this.isViewTemplate ? false : true) : false)) ? true : false,
                renderer: function (){
                    return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
                }
            },
            { // Below Column used in Transaction for Calculation of CESS tax in INDIA Country
                header: Wtf.GST_CESS_TYPE, //"Cess Calcualtion Type ID",
                align: 'center',
                width: 50,
                dataIndex: Wtf.GST_CESS_TYPE,
                hidden: true,
                hideable: false
            }, {
                header: Wtf.GST_CESS_VALUATION_AMOUNT, //"Valuation Amount",
                align: 'center',
                width: 50,
                dataIndex: Wtf.GST_CESS_VALUATION_AMOUNT,
                hidden: true,
                hideable: false
            },{
                header: Wtf.DEFAULT_TERMID, //"Default Term id Amount",
                align: 'center',
                width: 50,
                dataIndex: Wtf.DEFAULT_TERMID,
                hidden: true,
                hideable: false
            }]);
        this.gridView1 = new Wtf.grid.GroupingView({
            forceFit:true,
            showGroupName: true,
            enableNoGroups:true, // REQUIRED!
            hideGroupedColumn: false,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        });    
        var gridSummary = new Wtf.grid.GroupSummary({});
        this.itemsgrid =groupable?new Wtf.grid.EditorGridPanel({
            bodyStyle: 'padding:0px',
            layout:'fit',
            region:"center",
            clicksToEdit:1,
            autoScroll:true,
            store: this.termStore,
            emptyText:WtfGlobal.getLocaleText("acc.common.norec"),
            cm:this.termcm,
            deferredRender: false,
            sm:this.sm1,
            border : false,
            stripeRows :true,
            forceFit:true ,
            plugins: [gridSummary],
            viewConfig :this.gridView1 
        }):new Wtf.grid.EditorGridPanel({
            bodyStyle: 'padding:0px',
            layout:'fit',
            region:"center",
            clicksToEdit:1,
            autoScroll:true,
            deferredRender: false,
            store: this.termStore,
            emptyText:WtfGlobal.getLocaleText("acc.common.norec"),
            cm:this.termcm,
            sm:this.sm1,
//            forceFit:true,
            border : false,
            stripeRows :true,
//            plugins: [gridSummary],
            viewConfig :new Wtf.grid.GridView({
                emptyText:WtfGlobal.emptyGridRenderer( WtfGlobal.getLocaleText('acc.common.norec')),
                forceFit:true            
            }) 
         
        });
        
        this.TermPanel = new Wtf.Panel({
            border : false,
            buttonAlign: 'right',
            autoScroll:true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:0px',
            baseCls:'bckgroundcolor',
            layout: 'fit',
            items:[this.itemsgrid]
        });
        this.add(this.TermPanel);
        
        this.itemsgrid.on('afteredit',function(obj) {
            obj.record.set('taxtype',1);
//            if(obj.field=='termamount') {
//                var tempTermPercentage = 0;
//                if(obj.record.data.assessablevalue != undefined && obj.record.data.assessablevalue != 0){
//                    tempTermPercentage = ((obj.value * 100)/obj.record.data.assessablevalue);//To calculate Temporary Term Percentage
//                }
//                obj.record.set('termpercentage',tempTermPercentage);
//            } else
                if(obj.field=='termpercentage' && obj.value==0) {
                obj.record.set('termpercentage','');
            } else if(obj.field=='termpercentage' ) {
                var tempTermAmt = 0;
                if(obj.record.data.assessablevalue != undefined && obj.record.data.assessablevalue != 0){
                    tempTermAmt = (obj.record.data.assessablevalue * (obj.value /100));//To calculate Temporary Term Amount  
                }
                obj.record.set('termamount',tempTermAmt);
            } else if ((obj.field =='taxvalue' || obj.field =='termamount' || obj.field == 'deductionorabatementpercent') && (this.isLineLevel!= undefined && this.isLineLevel)){
                var rec = this.record;
                rec.objField = obj.field;
                if (obj.field == 'termamount') {
                    obj.record.set('taxtype',2);
                }
                var termStoreData = eval(this.getTermDetails());
                
                if (this.parentObj.moduleid == Wtf.Acc_Credit_Note_ModuleId || this.parentObj.moduleid == Wtf.Acc_Debit_Note_ModuleId ||
                        this.parentObj.moduleid == Wtf.Acc_Make_Payment_ModuleId || this.parentObj.moduleid == Wtf.Acc_Receive_Payment_ModuleId) {
                    termStoreData = calculateTermLevelTaxesForPayment(termStoreData, rec);
                    this.termStore.removeAll();
                    this.loadSavedTermDetails(eval(termStoreData));
                    rec.set("LineTermdetails", JSON.stringify(this.getTermDetails()))
                    updateTermDetails(this.gridObj);
                    this.gridObj.fireEvent('datachanged', this);
                } else {
                    if (this.parentObj && this.parentObj.includingGST && this.parentObj.includingGST.getValue() == true) {
                    termStoreData = this.gridObj.calculateTermLevelTaxesInclusive(termStoreData, rec);
                    } else if (!this.parentObj.isFixedAsset) {
                    termStoreData = this.gridObj.calculateTermLevelTaxes(termStoreData, rec);
                    } else {
                    termStoreData = calculateTermLevelTaxes(termStoreData, rec);
                }
                //var termDetails = this.getTermDetails();
                this.termStore.removeAll();
                    this.loadSavedTermDetails(eval(termStoreData));
                rec.set("LineTermdetails", JSON.stringify(this.getTermDetails()))
                    if (typeof this.gridObj.updateTermDetails === "function") {
                    this.gridObj.updateTermDetails(); 
                    } else {
                    updateTermDetails(this.gridObj);   
                }
                this.parentObj.updateSubtotal();
                }

            }else if((obj.field=='glaccount'||obj.field=='formulaids' ||obj.field=='taxtype' || obj.field=='taxvalue') && this.isCallFromCompany){
                var isUsed= false;
                Wtf.Ajax.requestEx({
                    url:"ACCCompanyPrefCMN/IsLineLevelTermEdit.do",
                    params: {
                        termid: obj.record.data.termid,
                        name: ''
                    }
                },this,function(response){
                    if(response.success && response.isUsed && obj.field=='glaccount'){
                        obj.record.set('accountid',obj.originalValue);
                        obj.record.set('glaccount',obj.originalValue);
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.accPref.changeTermAccount")], 2);
                    }else if(response.success && response.isUsed && obj.field=='formulaids'){
                        obj.record.set('formulaids',obj.originalValue);
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.accPref.changeTermAccount")], 2);
                    }else if(response.success && response.isUsed && obj.field=='taxtype'){
                        obj.record.set('taxtype',obj.originalValue);
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.accPref.changeTermAccount")], 2);
                    }else if(response.success && response.isUsed && obj.field=='taxvalue'){
                        obj.record.set('taxvalue',obj.originalValue);
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.accPref.changeTermAccount")], 2);
                    }
                },function(){});
            }
        },this);  
        if (this.isCallFromCompany) {
            this.sm1.on('selectionchange', function () {
                if (this.itemsgrid.selModel.getCount() == 1) {
                    Wtf.getCmp("cloneTerm").enable();
                } else {
                    Wtf.getCmp("cloneTerm").disable();
                }
            }, this);
        }
        this.itemsgrid.on('beforeedit',function(e){
            if(this.isViewTemplate!= undefined && this.isViewTemplate==true){
                return false;
            }
            /**
             *In CN/DN Overcharge/Undercharge tax can't edit for US country
             */
            if(this.isCNDNOverUnderCharge!=undefined && this.isCNDNOverUnderCharge){
                return false;
            }
            if(e.field=='termtype'){
                return false;
            }
            if(e.field=='taxtype'){
                if(typeof this.isProduct!="undefined" && this.isProduct){
                  return false;
            }
                if( typeof this.isLineLevel!="undefined" && this.isLineLevel){
                    return false;
                }
            }
            if(e.field=='taxvalue'){
//                if(e.record.data.termtype!=""){      // ERP-26931  Freeze Amount & Percentage Column
//                    return false;
//                }
                if(typeof this.isProduct!="undefined" && this.isProduct){
                    return false;
                }
                if(typeof this.isLineLevel!="undefined" && this.isLineLevel){
                    return true;
                    //ERP-26204 - Changes in value of tax/ terms at line level
                }
            }
            if(e.field=='formulaids'){
                if(typeof this.isProduct!="undefined" && this.isProduct){
                return false;
            }
                if(typeof this.isLineLevel!="undefined" && this.isLineLevel){
                    return false;
                }
            }
            if(e.field=='sign'){
                if(typeof this.isProduct!="undefined" && this.isProduct){
                    return false;
            }
                if(typeof this.isLineLevel!="undefined" && this.isLineLevel){
                    return false;
                }
            }
            if(e.field=='glaccount' && !this.isCallFromCompany){
                return false;
            }
            if(e.field=='purchasevalueorsalevalue'){
            /*if(typeof this.isProduct!="undefined" && this.isProduct){
                    return false;
                }
                if(typeof this.isLineLevel!="undefined" && this.isLineLevel){
                    return false;
                }*/
            }
            if(e.field=='deductionorabatementpercent'){
            /*if(typeof this.isProduct!="undefined" && this.isProduct){
                    return false;
                }
                if(typeof this.isLineLevel!="undefined" && this.isLineLevel){
                    return false;
                }*/
            }
            if(e.field=='assessablevalue'){
                if(typeof this.isLineLevel!="undefined" && this.isLineLevel){
                    return false;
                }
            }
            if (e.field == 'creditnotavailedaccount') {
                if (e.record != undefined && e.record.data != undefined) {
                    var termname = e.record.data.term.toUpperCase();
                    /* Credit Not Availed account is not available for CESS */
                    if (termname.indexOf("CESS") != -1) {
                        return false;
                    }
                }
            }
        },this);
        this.itemsgrid.on('cellclick', this.handleCellClick, this);
    },
    isViewTemplate: function (config) {
        if (config.parentObj != undefined && ((config.parentObj.isViewTemplate != undefined && config.parentObj.isViewTemplate != false) ||
               config.parentObj.readOnly != undefined && config.parentObj.readOnly != false )) {
            return true;
        } else {
            return false;
        }
    },
    handleCellClick: function (grid, rowIndex, columnIndex, e) {
        this.isCallFromCompany = this.isCallFromCompany == undefined ? false : this.isCallFromCompany
         var event = e;
        if (event.getTarget("div[class='pwnd delete-gridrow']")) {
            if (this.isCallFromCompany) {
                var fieldName = grid.getColumnModel().getDataIndex(columnIndex); // Get field name of column on which you click ringh now
                if (fieldName == 'deleteRecord') {
                    var record = grid.getStore().getAt(rowIndex);
                    if (record != "" && record != undefined) {
                        this.obj.TermGrid.deleteTerm(record);
                    }
                }
        } else {
            // If US country then allowed to delete Tax from Tax Selection Window
                 var record = grid.getStore().getAt(rowIndex);
                if (record != "" && record != undefined) {
                    this.deleteTax(record,grid);
                }
        }
    }    
        if(fieldName == 'isDefault'){//Default Column
            var record = grid.getStore().getAt(rowIndex);  // Get the Record on which you clicked 
            /*[[1,'VAT'],[2,'Excise Duty'],[3,'CST'],[4,'Service Tax'],[5,'Swachh Bharat Cess'],[6,'Krishi Kalyan Cess'],[7,'Others']]
             TaxType && IsFlowON && TermIsNotAlreadyDefault*/
            if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
                if((record.data.termtype == 1 || record.data.termtype == 3) && Wtf.account.companyAccountPref.enablevatcst == false && record.data[fieldName] == false){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.TermSelGrid.activateVATCST")],0);
                }else if(record.data.termtype == 2 && Wtf.isExciseApplicable == false && record.data[fieldName] == false){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.TermSelGrid.activateExciseduty")],0);
                }else if((record.data.termtype == 4 || record.data.termtype == 5 || record.data.termtype == 6 ) && Wtf.isSTApplicable == false && record.data[fieldName] == false){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.TermSelGrid.activateServiceTax")],0);
                }else{
                    record.set(fieldName, !record.data[fieldName]);//to check/uncheck checkbox on user click
                }
            }else{
                record.set(fieldName, !record.data[fieldName]);//to check/uncheck checkbox on user click
            }
            if(!this.isAdditionalTax && !this.isGST){
                for(var k=0; k <= grid.getStore().data.length; k++){
                    var temp = grid.getStore().getAt(k);
                    //select only one type of Tax/Term
                    if(temp != undefined && temp.data.termtype== Wtf.term.CST ){
                        if(temp != undefined && k != rowIndex && record.data.formType == temp.data.formType && record.data.isDefault == temp.data.isDefault ){
                            temp.set(fieldName, false );
                        }
                    }else{
                        if(temp != undefined && k != rowIndex && record.data.termtype == temp.data.termtype && record.data.isDefault == temp.data.isDefault ){
                            temp.set(fieldName, false );
                        }
                    }
                }
            }
        }
        if(fieldName == 'includeInTDSCalculation'){//Include TDS Column
            if(grid.getStore()!=undefined && grid.getStore().getCount()>0){
              var record = grid.getStore().getAt(rowIndex);
              record.set(fieldName, !record.data[fieldName]);
            }
        }

    },
    TermRenderer :function(val, meta, rec, row, col, store) {
        var regex = /(<([^>]+)>)/ig;
        val = val.replace(/(<([^>]+)>)/ig,"");
        var tip = val.replace(/"/g,'&rdquo;');
        //        meta.attr = 'wtf:qtip="'+tip+'"'+'" wtf:qtitle="'+WtfGlobal.getLocaleText("acc.gridproduct.discription")+'"';
        meta.attr = 'wtf:qtip="'+tip+'"';
        return val;
    },
    deleteRenderer:function(v,m,rec){
        return "<div style='margin: auto;' class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    getTermDetails: function() {
//        var arr = [];
//        var includeLast = true;
//        this.termStore.each(function(rec) {
//            arr.push(this.termStore.indexOf(rec));
//        }, this);
//        var jarray = WtfGlobal.getJSONArray(this.itemsgrid, includeLast, arr);
//        return jarray;

        this.recArr = this.itemsgrid.store.data.items;
        var temp = [];
        for(var k=0;k < this.recArr.length;k++){
            temp.push(this.recArr[k].data);
        }
        return temp;
    },
    getTotalTermAmt: function() {
        this.recArr = this.itemsgrid.store.data.items;
        var temp = 0;
        for(var k=0;k < this.recArr.length;k++){
            temp += this.recArr[k].data.termamount;
        }
        return temp;
    },
//    setTermRows : function(obj){
//        var record = "";
//        for(var i = 0;i < obj.data.length;i++){
//            record= obj.data.itemAt(i);
//            if(record.data!= null || record.data != 'undefined'){
////                record.set("invquantity",this.invQuantity);
////                record.set("invAmount",this.invAmount);
//                record.set("productid",	record.data.productid);
//            }
//        }
    //        this.itemsgrid.getView().refresh(true);
    //    },
    saveCompanyTerms:function(){
        var arr = [];
        this.recArr = this.itemsgrid.getStore().getModifiedRecords();
        for (var i = 0; i < this.recArr.length; i++) {
            arr.push(this.itemsgrid.getStore().indexOf(this.recArr[i]));
        }
        var data = WtfGlobal.getJSONArray(this.itemsgrid, true, arr);
        if(data){
            Wtf.Ajax.requestEx({
                url: "ACCAccountCMN/saveTermsAsTaxForIndia.do",
                params: {
                    data: data
                }
            }, this, this.genSuccessResponse, this.genFailureResponse);  
        }
    },
    genSuccessResponse:function(){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.term.update")],0);
    },
    genFailureResponse:function(){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.common.msg1")],2);
    },
    formulaRenderer: function (v, m, rec) {
        if(v){
            var arr = v.split(",");
            var returnValue = "";
            if(arr){
                for(var i = 0; i< arr.length ; i++){
                    if(arr[i].toLowerCase() == 'basic'){
                        returnValue += arr[i];
                    } else{
                        var record = WtfGlobal.searchRecord(Wtf.purchaseTermStore,arr[i],'id');
                        if(record && record.data && record.data.term){
                            returnValue += record.data.term;
                        }
                    }
                    if(i != arr.length-1){
                        returnValue += ", ";
                    }
                }
            }
        }
        return returnValue;
    },
    loadSavedTermDetails: function(termDetails) {
//        var termDetails = [];
//        if (this.record.data.LineTermdetails != undefined && this.record.data.LineTermdetails.length > 1) {
//            termDetails = eval('(' + JSON.stringify(this.record.data.LineTermdetails) + ')');
//        }
        var recordQuantity = termDetails.length;
       
        if (recordQuantity != 0) {
            for (var i=0; i<recordQuantity; i++) {
                var termRecord = termDetails[i];
                var rec = new this.termRec(termRecord);
                rec.beginEdit();
                var fields = this.termStore.fields;
               
                for (var x=0; x<fields.items.length; x++) {
                    var value = termRecord[fields.get(x).name];
                    if (fields.get(x).name == 'termpercentage' && value && value != '') {
                        value = decodeURI(value);
                    }
//                    if (fields.get(x).name == 'assessablevalue' && !this.isEdit ) {
//                    if (fields.get(x).name == 'assessablevalue') {
//                        value = this.record.data.amount;
//                                }
                    if (fields.get(x).name == 'termamount' && value && value != '') {
                        value = decodeURI(value);
                    }
                    rec.set(fields.get(x).name, value);
                }
                rec.endEdit();
                rec.commit();
                this.termStore.add(rec);
            }
        }
    },
    checktype : function (){
        var checkType=false;
        if(this.type!="productTermPurchaseGrid" && !this.isCallFromCompany){
            checkType=true;
        }
        if(this.isCustomer){
            checkType=true;
        }
        return checkType
    },
    
    createFields : function(config){
        
        this.accRec = Wtf.data.Record.create ([
        {
            name:'accountname',
            mapping:'accname'
        },

        {
            name:'accountid',
            mapping:'accid'
        },

        {
            name:'currencyid',
            mapping:'currencyid'
        },

        {
            name:'acccode'
        },

        {
            name:'accname'
        },

        {
            name:'groupname'
        }
        ]);

        this.accStore = new Wtf.data.Store({
//           url: Wtf.req.account+'CompanyManager.jsp',
            url : "ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        this.cmbAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.createTerm.AccountName") +"'>"+ WtfGlobal.getLocaleText("acc.ledger.accName") +"</span >"  ,  //'Account Name'         
//        id:'accountIdForCombo'+config.id ,
            name:'accountid',
            store:this.accStore,
            valueField:'accountid',
            displayField:'accname',
            mode: 'local',
            minChars:1,
            typeAheadDelay:30000,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
//            anchor:'100%',
            width:200,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
            hiddenName:'accountid',
            emptyText:'Select Account',
            allowBlank:false,
            forceSelection:true,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
            triggerAction:'all',
            comboFieldDataIndex:'glaccountname'
                    });
        /**
         * Filter should be cleared because on store is used for two combos.
         */
        this.cmbAccount.on('beforeselect', function (combo, record, index) {
            this.accStore.clearFilter();
        }, this);
            this.cmbpayableAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel: "<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.createTerm.AdvancePayableAccount") +"'>"+"Advance Payable Account" +"</span >"  ,//WtfGlobal.getLocaleText("acc.ledger.accName"),  //'Account Name',
            name:'payableaccountid',
            store:this.accStore,
            valueField:'accountid',
            displayField:'accname',
            mode: 'local',
            minChars:1,
            typeAheadDelay:30000,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
//            anchor:'100%',
            width:200,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
            hiddenName:'payableaccountid',
            emptyText:'Select Account',
            allowBlank:false,
            forceSelection:true,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
            triggerAction:'all',
            comboFieldDataIndex:'payableglaccountname'
        });
        /**
         * Filter should be cleared because on store is used for two combos.
         */
        this.cmbpayableAccount.on('beforeselect', function (combo, record, index) {
            this.accStore.clearFilter();
        }, this);
        this.creditNotAvailedAccount = new Wtf.form.ExtFnComboBox({
            fieldLabel: "<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.createTerm.CreditNotAvailedAccount") +"'>"+"Credit Not Availed Account" +"</span>"  ,
            name:'creditnotavailedaccount',
            store:this.accStore,
            valueField:'accountid',
            displayField:'accname',
            mode: 'local',
            minChars:1,
            typeAheadDelay:30000,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            width:200,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
            hiddenName:'creditnotavailedaccount',
            emptyText:'Select Account',
            allowBlank:false,
            forceSelection:true,
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
            triggerAction:'all',
            comboFieldDataIndex:'creditnotavailedaccountname'
        });
        this.accStore.on("load", function (store, rec, options) {
            if(config != undefined){
                this.cmbAccount.setValue(config.data.glaccount)
            }
        }, this);
        this.accStore.load({
            params: {
                mode: 2 , 
                ignoreAssets: true,
                ignorecustomers: true, 
                ignorevendors: true
            }
        });
        
   /* this.rowTermTypeStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'typeid',
            type:'int'
        }, 'name'],
        data :[[1,'VAT'],[2,'Excise Duty'],[3,'CST'],[4,'Service Tax'],[5,'Swachh Bharat Cess'],[6,'Krishi Kalyan Cess'],[7,'Others']]
    });*/   
    this.rowTermTypeCmb = new Wtf.form.ComboBox({
        store: Wtf.LineTermsMasterStore,
        fieldLabel:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.createTerm.TaxDescription") +"'>"+WtfGlobal.getLocaleText("acc.termselgrid.taxdescription") +"</span>"  ,
        name: 'termtype',
        hiddenName: 'termtype',
        displayField:'name',
        valueField:'typeid',
        width:200,
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus:true
    });    
    this.rowTermSequenceStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'typeid',
            type:'int'
        }, 'name'],
        data :[[1,'Excise Duty'],[2,'Service Tax'],[3,'Swachh Bharat Cess'],[4,'Krishi Kalyan Cess'],[5,'CST'],[6,'VAT']]
    });
    this.rowTermSequenceCmb = new Wtf.form.ComboBox({
        store: this.rowTermSequenceStore,
        fieldLabel: WtfGlobal.getLocaleText("acc.termselgrid.taxdescription"),
        name: 'termtype',
        hiddenName: 'termtype',
        displayField:'name',
        valueField:'typeid',
        width:200,
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus:true
    });
    this.CSTFormTypeCmb = new Wtf.form.ComboBox({
        store:this.FormSelectionStore,
        fieldLabel:"Form Type",
        name: 'formType',
        hiddenName: 'formType',
        displayField:'name',
        valueField:'id',
        width:183,
        listWidth:183,
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus:true           
    });
        
    this.rowTaxTypeStore = new Wtf.data.SimpleStore({
        fields: [{
            name:'typeid',
            type:'int'
        }, 'name'],
        data :[[1,'Percentage'],[0,'Flat'],[2,'NA']]
    });
    this.rowTaxTypeCmb = new Wtf.form.ComboBox({
        store: this.rowTaxTypeStore,
        fieldLabel: WtfGlobal.getLocaleText("acc.termselgrid.taxtype"),
        name: 'taxtype',
        width:200,        
        allowBlank:true,
        hiddenName: 'taxtype',
        hidden:true,   //Making This combo hidden in System preferences.
        hideLabel:true,
        value:1,
        displayField:'name',
        valueField:'typeid',
        mode: 'local',
        triggerAction: 'all',
        selectOnFocus:true
    });
//    this.rowTaxTypeCmb.on('select',this.ChangeLabel,this);
        
    this.rowTaxValue = new Wtf.form.NumberField({
        fieldLabel: WtfGlobal.getLocaleText("acc.termselgrid.amount"),
        name: 'taxvalue',
        allowBlank: true,
        allowNegative: false,
        defaultValue:0,
        hidden:true,   //Making This combo hidden in System preferences.
        hideLabel:true,
        value:0,
        width:200,
        decimalPrecision:(Wtf.account.companyAccountPref.isMultiEntity && (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA || Wtf.account.companyAccountPref.countryid == Wtf.Country.US)) ? Wtf.account.companyAccountPref.columnPref.gstamountdigitafterdecimal : Wtf.AMOUNT_DIGIT_AFTER_DECIMAL
    });
        
        
        

},

//ChangeLabel: function(){
    //        if(!Wtf.isEmpty(this.rowTaxTypeCmb.getValue()) && this.rowTaxTypeCmb.getValue()== 0){
    //            this.rowTaxTypeCmb.reset();
    //        }else if(!Wtf.isEmpty(this.rowTaxTypeCmb.getValue()) && this.rowTaxTypeCmb.getValue()== 1){
    //            this.rowTaxValue.label.update("Hi");
    //        }
//    },
    createProductGridWindow :function(){
        
        this.productCategoryRecord = Wtf.data.Record.create ([
        {
            name:'id'
        },

        {
            name:'name'
        }
        ]);
        this.productCategoryStore = new Wtf.data.Store({
            url: "ACCMaster/getMasterItems.do",
            baseParams:{
                mode:112,
                groupid:19
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productCategoryRecord)
        });
        this.productCategoryStore.load();
        this.productCategoryStore.on('load',this.setValue,this);
        
        this.typeEditor = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.Productcategory"),
            store: this.productCategoryStore,
            displayField:'name',
            valueField:'id',
            mode: 'local',
            triggerAction: 'all',
            typeAhead:true,
            selectOnFocus:true
        });    
        this.productGridRec = Wtf.data.Record.create ([
        {
            name:'productid'
        },
        {
            name:'productname'
        },
        {
            name:'desc'
        },
        {
            name:'category'
        },
        {
            name: 'pid'
        }
        ]);
        this.productStoreGrid = new Wtf.data.GroupingStore({
            url:"ACCProductCMN/getProductsForSelectionGrid.do",
            baseParams:{
                common:'1'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'totalCount'
            },this.productGridRec),
            groupField:"category",
            sortInfo: {
                field: 'category',
                direction: "ASC"
            }
        });
        this.smProductGrid = new Wtf.grid.CheckboxSelectionModel({
            singleSelect: false
        });
        this.columnCm = new Wtf.grid.ColumnModel([
            this.smProductGrid,
            new Wtf.grid.RowNumberer({
                width:40
            }),
            {
                header:WtfGlobal.getLocaleText("acc.cust.Productcategory"),  //"Category",
                dataIndex:'category',
                hidden: true,
                sortable:true,
                groupable:true,
                renderer:function(v){
                    if(!Wtf.isEmpty(v)){
                        return v;
                    }
                    return "none"
                }
            },{
                header: "Product Id",  
                sortable:true,
                dataIndex: "pid",
                width:160
            },{
                header:"Product Name",
                sortable:true,
                dataIndex:"productname",
                width:200
            },
            {
                header:"Product Description",
                sortable:true,
                dataIndex:"desc",
                width:150

            }
            ]);
        this.localSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.productList.searchText"),//'Search by Product Name',
            width: 150,
            field: 'productname',
            Store:this.productStoreGrid
        });
        this.productgrid = new Wtf.grid.GridPanel({
            store: this.productStoreGrid,
            sm:this.smProductGrid,
            cm: this.columnCm,
            border : false,
            loadMask : true,
            view: new Wtf.grid.GroupingView({
                forceFit:true
            }),
            layout : 'fit',
            modal : true,
            tbar : [this.localSearch,'-',WtfGlobal.getLocaleText("acc.cust.Productcategory"),this.typeEditor,{
                xtype:'button',
                text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
                iconCls:'accountingbase fetch',
                scope:this,
                handler:this.loadTypeStore
            }],
            bbar: this.pag=new Wtf.PagingSearchToolbar({
                pageSize: 30,
                border : false,
                id : "paggintoolbar_ProductGrid"+this.id,
                store: this.productStoreGrid,
                searchField: this.localSearch,
                scope:this,
                plugins : this.pPageSizeObj = new Wtf.common.pPageSize({
                    id : "pPageSize_ProductGrid_"+this.id
                }),
                autoWidth : true,
                displayInfo:true
            }) 
        });
        this.productStoreGrid.load({params:{start:0,limit:30}});//ERP-29729
        this.productStoreGrid.on("beforeload", function(){
            var categoryid = this.typeEditor.getValue();
            if(categoryid=='All') {
                this.productStoreGrid.params = {//ERP-29729
                    limit :this.pPageSizeObj.combo.value,
                    ss : this.localSearch.getValue()
                }
            } else {
                this.productStoreGrid.params = {//ERP-29729
                    limit :this.pPageSizeObj.combo.value,
                    ss : this.localSearch.getValue(),
                    categoryid:categoryid
                }
            }
        },this);
        this.ProductDisplayWindow= new Wtf.Window({
            modal: true,
            id:'ProductDisplayWindow',
            title: 'Product Selection Window',
            bodyStyle: 'padding:5px;',
            buttonAlign: 'right',
            layout:"fit",
            width: 650,
            height:500,
            scope: this,
            closable : false,
            items: [this.productgrid],
            buttons:
            [{
                text: 'Save',
                scope:this,
                handler: function()
                {
                    var productSelections = this.productgrid.getSelectionModel().getSelections();
                    this.productList = [];
                    if(productSelections.length>0){
                        for(var i= 0; i <productSelections.length;i++){
                            this.productList.push(productSelections[i].data)
                        }
                        this.ProductDisplayWindow.close();
                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.prod.comboEmptytext")], 2); 
                    }
                }
            },{
                text: 'Cancel',
                scope:this,
                handler: function()
                {
                    this.ProductDisplayWindow.close();
                    this.isApplyOnExistingProduct.setValue(false);
                    this.isApplyOnAllExistingProduct.setValue(false);
                    this.productList = [];
                }
            }]
        });
        this.ProductDisplayWindow.show();
        this.typeEditor.setValue("All");
    },
    setValue:function(){
        var record = new Wtf.data.Record({
            name:'All',
            id:'All'
        });
        var index=this.productCategoryStore.find('name','All');
        if(index==-1){
            this.productCategoryStore.insert(0,record);    
            this.typeEditor.setValue("All");
        }        
        
    },
    loadTypeStore:function(a,rec){
        var categoryid = this.typeEditor.getValue();
        if(categoryid=='All') {
            this.productStoreGrid.load({
                params: {
                    start : 0,
                    limit : this.pPageSizeObj.combo.value,
                    ss :this.localSearch.getValue()
                }
            });
        }
        else {
            this.productStoreGrid.load({
                params:{
                    start : 0,
                    limit : this.pPageSizeObj.combo.value,
                    categoryid:categoryid,
                    ss :this.localSearch.getValue()
                }
            });
        }
    },
    filterProductJSON : function(termDetails,termType){
        var filteredTerm=[];
        for(var i=0;i<termDetails.length;i++){
            var isServiceTax =true;
            if(termType==Wtf.term.Service_Tax && (termDetails[i].termtype==Wtf.term.Service_Tax || termDetails[i].termtype==Wtf.term.Krishi_Kalyan_Cess ||termDetails[i].termtype==Wtf.term.Swachh_Bharat_Cess)){
                isServiceTax=false;
            }
            if(termDetails[i].termtype!=termType && isServiceTax){
                continue;
            }
            filteredTerm.push(termDetails[i]);
        }
        if(termType==Wtf.term.CST){
           filteredTerm.sort(function(a,b){ // sort by form type for grid grouping.
               return a.formType-b.formType;
           }); 
        }
        return filteredTerm;
    },
    deleteTerm: function (record) {
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.gstterm.confirmationMessage"), function (btn) {
            if (btn == 'yes') {
                /**
                 * Not to pass request when No Item is selected.
                 */
                if (record != undefined && record != "") {
                    Wtf.Ajax.requestEx({
                        url: "AccEntityGST/deleteLineLevelTerm.do",
                        params: {
                            termId: record.data.termid,
                            termName : record.data.term
                        }
                    }, this, this.obj.TermGrid.genSuccessResponseClose, this.obj.TermGrid.genFailureResponseClose);
                }
            }
        }, this);
    },
    genSuccessResponseClose: function (response) {
        WtfGlobal.setAjaxTimeOut();
        if(response.success){ 
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Success"),response.msg],0);
            this.obj.GSTfortermGrid.termStore.reload();
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],2);
        }
    },
    genFailureResponseClose: function (response) {
        WtfGlobal.setAjaxTimeOut();
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
            
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    deleteTax:function (record,grid) {
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.gstterm.confirmationMessage"), function (btn) {
            if (btn == 'yes') {
                if (WtfGlobal.isUSCountryAndGSTApplied()) {
                        grid.getStore().remove(record);  
                }
            }
        }, this);
    },    
    createNewTerm : function(config, isGSTTab) {
        this.createFields(config);
        this.CSTFormTypeCmb.winDetails=config;
        this.term=new Wtf.form.TextField({
            fieldLabel:"<span wtf:qtip='"+ WtfGlobal.getLocaleText("acc.createTerm.Term") +"'>"+WtfGlobal.getLocaleText("acc.field.Term") +"</span>"  ,
            name: 'term',
            scope:this,
            //            anchor:'100%', 
            width:200,
            allowBlank:false
        });
        
        this.formulaRec=new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'term'
        }
        ]);
        this.formulaStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.formulaRec),
            url: 'ACCAccount/getIndianTermsCompanyLevel.do',
            baseParams:{
                // isSalesOrPurchase:this.isSales
                isSalesOrPurchase:this.isCustomer,
                isNewGST:WtfGlobal.GSTApplicableForCompany()==Wtf.GSTStatus.NEW?true:false
            }
        });
        this.formulaStore.on("load",function(store, rec, options){
            var blankObj={};
            blankObj['id'] = 'Basic';
            blankObj['term'] = 'Basic';
            var newrec = new this.formulaRec(blankObj);
            this.formulaStore.insert(0,newrec);
            if(config != undefined){
                this.formulaCombo.setValue(config.data.formulaids)
            }else{
                this.formulaCombo.setValue('Basic');
            }
        },this);
        
        this.formulaStore.load();
        
        this.FormulaComboconfig = {
            hiddenName:"formula",
            store: this.formulaStore,
            valueField:'id',
            displayField:'term',
            emptyText:WtfGlobal.getLocaleText("acc.field.Selectformula"),
            mode: 'local',
            typeAhead: true,
            selectOnFocus:true,                            
            allowBlank:false,
            triggerAction:'all',
            scope:this
        };

        this.formulaCombo = new Wtf.common.Select(Wtf.applyIf({
            multiSelect:true,
            fieldLabel:WtfGlobal.getLocaleText("acc.master.invoiceterm.formula") ,
            forceSelection:true,
            width: 200
        //            anchor:'100%'
        },this.FormulaComboconfig));
        this.formulaCombo.hideLabel=true;
         this.formulaCombo.hide();
         
        this.formulaCombo.on('render',function(){             //to avoid editing combo value by user
            this.formulaCombo.setEditable(false); 
        },this);
        this.isApplyOnExistingProduct = new Wtf.form.Checkbox({
            name: 'isApplyOnExistingProduct',
            id: 'isApplyOnExistingProduct',
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.master.invoiceterm.isapplyonproduct.selected") + "'>" + WtfGlobal.getLocaleText("acc.master.invoiceterm.isapplyonproduct.selected") + "</span>", //Apply on Selected existing Product,
            checked: false,
            hideLabel:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            hidden:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            itemCls: "chkboxalign"
        });
        this.isApplyOnAllExistingProduct = new Wtf.form.Checkbox({
            name: 'isApplyOnAllExistingProduct',
            id: 'isApplyOnAllExistingProduct',
            fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.master.invoiceterm.isapplyonproduct.all") + "'>" + WtfGlobal.getLocaleText("acc.master.invoiceterm.isapplyonproduct.all") + "</span>", //Apply on All existing Product,
            checked: false,
            hideLabel:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            hidden:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            itemCls: "chkboxalign"
        });
        this.isApplyOnExistingProduct.on('check', function (o, newval, oldval) {
            if (o.getValue()) {
                this.isApplyOnAllExistingProduct.setValue(false);
                this.isApplyOnAllExistingProduct.disable();
                this.createProductGridWindow();
            } else {
                this.isApplyOnAllExistingProduct.enable();
                this.productList = [];
            }
        }, this);
        this.isApplyOnAllExistingProduct.on('check', function (o, newval, oldval) {
            if (o.getValue()) {
                this.isApplyOnExistingProduct.setValue(false);
                this.isApplyOnExistingProduct.disable();
                this.productList = [];
            } else {
                this.isApplyOnExistingProduct.enable();
            }
        }, this);

        this.isAdditionalTaxcheckbox = new Wtf.form.Checkbox({
            name:'isAdditionalTax',
            id:'isAdditionalTax',
            hideLabel:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            hidden:WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW,
            fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.master.invoiceterm.isadditional") +"'>"+ WtfGlobal.getLocaleText("acc.master.invoiceterm.isadditional")+"</span>",//Apply on existing Product,            
            checked : (this.id!=undefined && this.id== 'TermGridForAdditional')? true :false,
            itemCls:"chkboxalign"
        });       
        this.includeInTDSCalcheckbox = new Wtf.form.Checkbox({
            name:'includeInTDSCalculation',
            id:'includeInTDSCalculation',
            hidden:(!this.isCustomer)?false :true,
            hideLabel:(!this.isCustomer)?false :true,
            fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.TermSelGrid.includeInTDSCalculation") +"'>"+ WtfGlobal.getLocaleText("acc.TermSelGrid.includeInTDSCalculation")+"</span>",//Include In TDS Calculation. existing Product,            
           itemCls:"chkboxalign"
        });
         this.includeInTDSCalcheckbox.hideLabel=true;
          this.includeInTDSCalcheckbox.hide();
      var calulatesignFieldSet = new Wtf.form.FieldSet({
            xtype:'fieldset',
            hidden:(Wtf.account.companyAccountPref.isNewGSTOnly!=undefined && Wtf.account.companyAccountPref.isNewGSTOnly!=null)?Wtf.account.companyAccountPref.isNewGSTOnly:false,
            autoHeight:true,
            width:400,
            items :[{
                fieldLabel: WtfGlobal.getLocaleText("acc.field.CalculateSign"),
                xtype : 'radiogroup',
                vertical: false,
                id:"_sign"+this.id,
                items: [
                {
                    boxLabel: WtfGlobal.getLocaleText("acc.field.Plus+"), 
                    name: 'sign', 
                    inputValue: '1', 
                    checked:config!=undefined?config.data.sign==1?true:false:true
                },

                {
                    boxLabel: WtfGlobal.getLocaleText("acc.field.Minus-"), 
                    name: 'sign', 
                    checked:config!=undefined?config.data.sign==0?true:false:false,
                    inputValue: '0'
                }
                ]    
            }]
        });
        this.IsOtherTermTaxable = new Wtf.form.Checkbox({
            name:'IsOtherTermTaxable',
            id:'IsOtherTermTaxable',
            fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.master.invoiceterm.isIsOtherTermTaxable") +"'>"+ WtfGlobal.getLocaleText("acc.master.invoiceterm.isIsOtherTermTaxable")+"</span>",//If other type term as taxable or not,            
            itemCls:"chkboxalign",
            checked : true
        });
        if(isGSTTab) {
            this.rowTermTypeCmb.value = 7;
            //this.rowTaxValue.decimalPrecision=Wtf.account.companyAccountPref.columnPref.gstamountdigitafterdecimal;
            // If currently Selected tab is "Default GST Master" then tax description will be autopopulated as GST, and Account/Percentage value will be roumded off three digits after decimal point
        }
        var array = [this.rowTermTypeCmb,
        this.CSTFormTypeCmb,
        this.term,
        this.rowTaxTypeCmb,
        this.rowTaxValue,
        this.isAdditionalTaxcheckbox,
        this.IsOtherTermTaxable,
        this.cmbAccount,
        this.cmbpayableAccount,
        this.creditNotAvailedAccount,
        this.formulaCombo,
        this.isApplyOnExistingProduct,            
        this.isApplyOnAllExistingProduct,            
//        this.applyOnExistingProducts,            
        this.includeInTDSCalcheckbox,
        calulatesignFieldSet,
        {
            xtype:'fieldset',
            autoHeight:true,
            hidden : true,
            width:400,
            items :[{
                fieldLabel: WtfGlobal.getLocaleText("acc.cust.category"),
                xtype : 'radiogroup',
                vertical: false,
                id:"_category"+this.id,
                items: [
                {
                    boxLabel: WtfGlobal.getLocaleText("acc.rem.vrnet.196"), 
                    name: 'category', 
                    inputValue: '1'
                },

                {
                    boxLabel: WtfGlobal.getLocaleText("acc.rem.111"), 
                    name: 'category', 
                    inputValue: '0', 
                    checked:true
                }
                ]
            }
            ]
        },{
            xtype:'fieldset',
            autoHeight:true,
            hidden : true,
            width:400,
            items :[{
                fieldLabel: WtfGlobal.getLocaleText("acc.master.invoiceterm.inclusiveofgst"),
                xtype : 'radiogroup',
                vertical: true,
                id:"_includegst"+this.id,
                items: [
                {
                    boxLabel: WtfGlobal.getLocaleText("acc.msgbox.yes"), 
                    name: 'includegst', 
                    inputValue: '1'
                },

                {
                    boxLabel: WtfGlobal.getLocaleText("acc.fxexposure.invno"), 
                    name: 'includegst', 
                    inputValue: '0', 
                    checked:true
                }
                ]    
            }]
        },{
            xtype:'fieldset',
            autoHeight:true,
            hidden : true,
            width:400,
            items :[{
                fieldLabel: WtfGlobal.getLocaleText("acc.field.InclusiveProfitability"),
                xtype : 'radiogroup',
                vertical: false,
                id:"_proft"+this.id,
                items: [
                {
                    boxLabel: WtfGlobal.getLocaleText("acc.msgbox.yes"), 
                    name: 'proft', 
                    inputValue: '1'
                },

                {
                    boxLabel: WtfGlobal.getLocaleText("acc.msgbox.no"), 
                    name: 'proft', 
                    inputValue: '0', 
                    checked:true
                }
                ]
            }]
        },{
            xtype:'fieldset',
            autoHeight:true,
            hidden : true,
            width:400,
            items :[{
                fieldLabel: WtfGlobal.getLocaleText("acc.field.SuppressOfAmount"),
                xtype : 'radiogroup',
                vertical: false,
                id:"_suppressamount"+this.id,
                items: [
                {
                    boxLabel: WtfGlobal.getLocaleText("acc.msgbox.yes"), 
                    name: 'suppressamount', 
                    inputValue: '1'
                },

                {
                    boxLabel: WtfGlobal.getLocaleText("acc.msgbox.no"), 
                    name: 'suppressamount', 
                    inputValue: '0', 
                    checked:true
                }
                ]    
            }]
        }
        ];
        
        this.terminfo = new Wtf.form.FormPanel({
            url: 'ACCAccountCMN/saveTermsAsTaxForIndia.do',
            region:'center',
            bodyStyle:"background: transparent;",
            border:false,
            labelWidth: 140,
            style: "background: transparent;padding:10px;",
            items:array
        });
        this.CSTFormTypeCmb.on('render',function(){
            var rowConfig =this.CSTFormTypeCmb.winDetails;
            if(rowConfig==undefined){
                WtfGlobal.hideFormElement(this.CSTFormTypeCmb);
                this.CSTFormTypeCmb.setValue('1');
                this.CSTFormTypeCmb.allowBlank=true;
            }else{
                if(rowConfig.data.termtype != undefined && rowConfig.data.termtype==Wtf.term.CST){
                    WtfGlobal.showFormElement(this.CSTFormTypeCmb);
                    this.CSTFormTypeCmb.allowBlank=false;
                    this.CSTFormTypeCmb.setValue(rowConfig.data.formType)
                }else{
                    WtfGlobal.hideFormElement(this.CSTFormTypeCmb);
                    this.CSTFormTypeCmb.setValue('1');
                    this.CSTFormTypeCmb.allowBlank=true;
                }
            } 
        },this);
        
        this.IsOtherTermTaxable.on('render', function () {
            if (config == undefined || config == "") {
                WtfGlobal.hideFormElement(this.IsOtherTermTaxable);
            } else if ((config != undefined || config != "") && config.data.termtype != Wtf.term.Others) {
                WtfGlobal.hideFormElement(this.IsOtherTermTaxable);
            }
        }, this);
        this.rowTermTypeCmb.on('select',function(){
            if(this.rowTermTypeCmb!=undefined && this.rowTermTypeCmb.getValue()!="" && this.rowTermTypeCmb.getValue()==Wtf.term.CST){
                WtfGlobal.showFormElement(this.CSTFormTypeCmb);
                this.CSTFormTypeCmb.allowBlank=false;
            }else{
                WtfGlobal.hideFormElement(this.CSTFormTypeCmb);
                this.CSTFormTypeCmb.setValue('1');
                this.CSTFormTypeCmb.allowBlank=true;
            }
            if(this.rowTermTypeCmb!=undefined && this.rowTermTypeCmb.getValue()!="" && this.rowTermTypeCmb.getValue()==Wtf.term.Others){
                WtfGlobal.showFormElement(this.IsOtherTermTaxable);
            }else{ 
                WtfGlobal.hideFormElement(this.IsOtherTermTaxable);
            }
     
        },this);
        
        this.rowTaxValue.on('blur',function(){
            if(this.rowTaxTypeCmb.getValue()!=""){
                if(this.rowTaxTypeCmb.getValue()===1 && this.rowTaxValue.getValue() > 100){ //1= for Percentage : 2 for Flat
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.termselgrid.amount.percentage.error")],2);
                    this.rowTaxValue.reset();
                }
            }
        },this);
        this.rowTaxTypeCmb.on('select',function(){
            this.rowTaxValue.reset(); 
        },this);
        this.createTermBtn = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.field.CreateTerm"),
            scope: this,
            handler:this.saveTermForm.createDelegate(this)
        });

        this.createTemplateWin=new Wtf.Window({
            title: config != undefined?WtfGlobal.getLocaleText("acc.field.cloneTerm"):WtfGlobal.getLocaleText("acc.field.DefineTerm"),
            closable: true,
            modal: true,
            iconCls : getButtonIconCls(Wtf.etype.deskera),
            width: 450,
            height: WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.OLDNEW?380:480,
            autoScroll:true,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body,
            items:[{
                region: 'center',
                border: false,
                bodyStyle: 'background:#f1f1f1;font-size:10px;',
                autoScroll:true,
                items:this.terminfo
            }],
            buttons: [this.createTermBtn, {
                text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope:this,
                handler:function(){
                    this.createTemplateWin.close();
                }
            }]
        });
        if (config != undefined) {

            if (config.data.taxvalue != undefined)
                this.rowTaxValue.setValue(config.data.taxvalue);

            if (config.data.termtype != undefined){
                this.rowTermTypeCmb.setValue(config.data.termtype);
                if(config.data.termtype==3){
                    this.CSTFormTypeCmb.setValue(config.data.formType);  
                }
            }
            if(config.data.termtype==Wtf.term.Others && !config.data.IsOtherTermTaxable){
                this.IsOtherTermTaxable.setValue(config.data.IsOtherTermTaxable);
            }
            if (config.data.taxtype != undefined)
                this.rowTaxTypeCmb.setValue(config.data.taxtype);
            if (config.data.term != undefined)
                this.term.setValue(config.data.term);
            if(config.data.includeInTDSCalculation != undefined){
                this.includeInTDSCalcheckbox.setValue(config.data.includeInTDSCalculation);
            }
        }
        this.createTemplateWin.show();
    },
    saveTermForm : function() {
        this.createTermBtn.disable();
        var StringProductList="";
        if((typeof this.productList == "object" && this.productList )&& this.productList.length>0){
            StringProductList = JSON.stringify(this.productList);
        }
        var isApplyOnAllExistingProduct = this.isApplyOnAllExistingProduct.getValue();
        if (isApplyOnAllExistingProduct != undefined && !Wtf.isEmpty(isApplyOnAllExistingProduct) && isApplyOnAllExistingProduct) {
            StringProductList = "";
        } else {
            isApplyOnAllExistingProduct = false;
        }
        var IndexMaster = this.rowTermTypeCmb.store.find('typeid',this.rowTermTypeCmb.getValue());
        this.masterItemId= '';
        if(IndexMaster>=-1){
            this.masterItemId = this.rowTermTypeCmb.store.getAt(IndexMaster).data.masterid;
        }
        
        this.terminfo.getForm().submit({
            scope: this,
            params:{
                isSalesOrPurchase : this.isCustomer,
                productList : StringProductList,
                masteritem : this.masterItemId,
                IsOtherTermTaxable : this.IsOtherTermTaxable.getValue(),
                isApplyOnAllExistingProduct : isApplyOnAllExistingProduct
            },
            success: function(result,action){
                this.createTermBtn.enable();
                var resultObj = eval('('+action.response.responseText+')');
                if(resultObj.data.success) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.field.Termaddedsuccessfully")],0);
                    this.createTemplateWin.close();
                    this.itemsgrid.store.load();
                    
                    if(Wtf.getCmp("addtionalTax1")){
                        Wtf.getCmp("addtionalTax1").items.items[0].itemsgrid.getStore().load();
                    }
                    if(Wtf.getCmp("addtionalTax2")){
                        Wtf.getCmp("addtionalTax2").items.items[0].itemsgrid.getStore().load();
                    }
                    if(Wtf.getCmp("addtionalTax3")){
                        Wtf.getCmp("addtionalTax3").items.items[0].itemsgrid.getStore().load();
                    }
                    if(Wtf.getCmp("addtionalTax4")){
                        Wtf.getCmp("addtionalTax4").items.items[0].itemsgrid.getStore().load();
                    }
                    if(Wtf.getCmp("addtionalTax5")){
                        Wtf.getCmp("addtionalTax5").items.items[0].itemsgrid.getStore().load();
                    }
                    if(Wtf.getCmp("addtionalTax6")){
                        Wtf.getCmp("addtionalTax6").items.items[0].itemsgrid.getStore().load();
                    }
                    if (Wtf.getCmp("addtionalTax7")) {
                        /**
                         * ERP-32829 
                         */
                        Wtf.getCmp("addtionalTax7").items.items[0].itemsgrid.getStore().load();
                    }
                    
                }else{
                    if(resultObj.data.msg)msg=resultObj.data.msg;
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
                }
            },
            failure: function(frm, action){
                this.createTermBtn.enable();
                var resObj = eval( "(" + action.response.responseText + ")" );
            }
        });
    }
});

function callTermWindow(module,isCallFromCompany){
    if(WtfGlobal.GSTApplicableForCompany()!=Wtf.GSTStatus.NONE){
        callTermForINDIA(module,isCallFromCompany);
    }else{
        callTermWindowNonIndia(module,isCallFromCompany);
    }
}
function callTermForINDIA(module,isCallFromCompany){
    
     this.TermGrid = new Wtf.account.TermSelGrid({
               id: 'TermSelGrid',
               isReceipt: false,
               isCustomer:module=='purchase'?false:true,
               border: false,
               layout:"fit",
               height:500,
               autoScroll:true,
//               cls:'gridFormat',
               region: 'center',
               isCallFromCompany:isCallFromCompany,
               isAdditionalTax:false,
               viewConfig:{
                   forceFit:false
               },
               isEdit:this.isEdit,
               obj: this
           });
     this.TermGridForAdditional = new Wtf.account.TermSelGrid({
               id: 'TermGridForAdditional',
               isReceipt: false,
               isCustomer:module=='purchase'?false:true,
               border: false,
               layout:"fit",
               height:500,
               autoScroll:true,
//               cls:'gridFormat',
               region: 'center',
               isCallFromCompany:isCallFromCompany,
               isAdditionalTax:true,
               viewConfig:{
                   forceFit:false
               },
               isEdit:this.isEdit,
               obj: this
           });
    this.vatTypeforTermGrid = new Wtf.account.TermSelGrid({
        id: 'vatTypeforTermGrid',
        isReceipt: false,
        isCustomer:module=='purchase'?false:true,
        border: false,
        layout:"fit",
        height:500,
        autoScroll:true,
        //               cls:'gridFormat',
        region: 'center',
        isCallFromCompany:isCallFromCompany,
        isAdditionalTax:false,
        termType:Wtf.term.VAT,
        viewConfig:{
            forceFit:false
        },
        isEdit:this.isEdit,
        obj: this
    });
    this.exciseTypefortermGrid = new Wtf.account.TermSelGrid({
        id: 'exciseTypefortermGrid',
        isReceipt: false,
        isCustomer:module=='purchase'?false:true,
        border: false,
        layout:"fit",
        height:500,
        autoScroll:true,
        //               cls:'gridFormat',
        region: 'center',
        isCallFromCompany:isCallFromCompany,
        isAdditionalTax:false,
        termType:Wtf.term.Excise,
        viewConfig:{
            forceFit:false
        },
        isEdit:this.isEdit,
        obj: this
    });
    this.serviceTypefortermGrid = new Wtf.account.TermSelGrid({
        id: 'serviceTypefortermGrid',
        isReceipt: false,
        isCustomer:module=='purchase'?false:true,
        border: false,
        layout:"fit",
        height:500,
        autoScroll:true,
        //               cls:'gridFormat',
        region: 'center',
        isCallFromCompany:isCallFromCompany,
        isAdditionalTax:false,
        termType:Wtf.term.Service_Tax,
        viewConfig:{
            forceFit:false
        },
        isEdit:this.isEdit,
        obj: this
    });
    this.cstTypeforTermGrid = new Wtf.account.TermSelGrid({
        id: 'cstTypeforTermGrid',
        isReceipt: false,
        isCustomer:module=='purchase'?false:true,
        border: false,
        layout:"fit",
        height:500,
        autoScroll:true,
        //               cls:'gridFormat',
        region: 'center',
        isCallFromCompany:isCallFromCompany,
        isAdditionalTax:false,
        termType:Wtf.term.CST,
        viewConfig:{
            forceFit:false
        },
        isEdit:this.isEdit,
        obj: this
    });
    
    this.OtherChargesfortermGrid = new Wtf.account.TermSelGrid({
        id: 'OtherChargesfortermGrid',
        isReceipt: false,
        isCustomer:module=='purchase'?false:true,
        border: false,
        layout:"fit",
        height:500,
        autoScroll:true,
        //               cls:'gridFormat',
        region: 'center',
         termType:Wtf.term.Others,
        isCallFromCompany:isCallFromCompany,
        isOtherChargesTermGrid:true,
        isAdditionalTax:false,
        viewConfig:{
            forceFit:false
        },
        isEdit:this.isEdit,
        obj: this
    });
    /**
     * ERP-32829 
     * @type Wtf.account.TermSelGrid
     */
    this.GSTfortermGrid = new Wtf.account.TermSelGrid({
        id: 'GSTfortermGrid',
        isReceipt: false,
        isCustomer:module=='purchase'?false:true,
        border: false,
        layout:"fit",
        height:500,
        autoScroll:true,
        //               cls:'gridFormat',
        region: 'center',
        termType:Wtf.term.GST,
        isCallFromCompany:isCallFromCompany,
//        isOtherChargesTermGrid:true,
        isAdditionalTax:false,
        viewConfig:{
            forceFit:false
        },
        isEdit:this.isEdit,
        obj: this
    });    
    
    /**
     * @description Create Array of Masters based on Old and new GST
     * @type Wtf.TabPanel
     */
    
    if(WtfGlobal.GSTApplicableForCompany()==Wtf.GSTStatus.OLDNEW){
        /**
         * @description  : For Old Company 
         * @type Wtf.TabPanel|Wtf.TabPanel
         */
    this.taxtab = new Wtf.TabPanel({
        activeTab: 0,
        frame:false, 
        items:[{
            title: 'Excise Duty',
            id:"addtionalTax2",
            items:[this.exciseTypefortermGrid],
            scope:this,
            listeners: {
                activate: function(tab){
                    tab.doLayout();
                },
                deactivate:function(tab){
                    tab.items.items[0].sm1.clearSelections();
                }
            }
        },{
            title: 'Service Tax',
            id:"addtionalTax3",
            items:[this.serviceTypefortermGrid],
            scope:this,
            listeners: {
                activate: function(tab){
                    tab.doLayout();
                },
                deactivate:function(tab){
                    tab.items.items[0].sm1.clearSelections();
                }
            }
        },{
            title: 'CST',
            id:"addtionalTax4",
            items:[this.cstTypeforTermGrid],
            scope:this,
            listeners: {
                activate: function(tab){
                    tab.doLayout();
                },
                deactivate:function(tab){
                    tab.items.items[0].sm1.clearSelections();
                }
            }
        },{
            title: 'VAT',
            id:"addtionalTax5",
            items:[this.vatTypeforTermGrid],
            scope:this,
            listeners: {
                activate: function(tab){
                    tab.doLayout();
                },
                                  deactivate:function(tab){
                    tab.items.items[0].sm1.clearSelections();
                }
            }
        },{
            title: 'Additional Tax',
            id:"addtionalTax1",
            items:[this.TermGridForAdditional],
            scope:this,
            listeners: {
                activate: function(tab){
                    tab.doLayout();
                },
                deactivate:function(tab){
                    tab.items.items[0].sm1.clearSelections();
                }
            }
        },{
            title: 'Other Charges',
            id:"addtionalTax6",
            items:[this.OtherChargesfortermGrid],
            scope:this,
            listeners: {
                activate: function(tab){
                    tab.doLayout();
                },
                deactivate:function(tab){
                    tab.items.items[0].sm1.clearSelections();
                }
            }
        },{
            title: 'Default GST Master',
            id:"addtionalTax7",
            items:[this.GSTfortermGrid],
            scope:this,
            listeners: {
                activate: function(tab){
                    tab.doLayout();
                },
                deactivate:function(tab){
                    tab.items.items[0].sm1.clearSelections();
        }
            }
        }
        ]
    });
    } else {
        /**
         * For New Company show only GST
         */
        this.taxtab = new Wtf.TabPanel({
            activeTab: 0,
            frame: false,
            items: [{
            title: 'Default GST Master',
            id:"addtionalTax7",
            items:[this.GSTfortermGrid],
            scope:this,
            listeners: {
                activate: function(tab){
                    tab.doLayout();
                },
                deactivate:function(tab){
                    tab.items.items[0].sm1.clearSelections();
                }
            }
        }]
        });
    }
   // this.taxtab.setActiveTab(Wtf.getCmp("addtionalTax5"));
    this.taxtab.setActiveTab(0);
    
    var isGSTTab = false;
    this.taxtab.on('tabchange', function (taxTab, tab) {
        if(tab.title == 'Default GST Master') {
            isGSTTab = true;
        } else {
            isGSTTab = false;
        }
    }, this); //if current tab is "Default GST Master" set isGSTTab true
    /**
     * Import Button
     * @type Wtf.Window
     */
            this.moduleName = "GSTTerm";
            var extraConfig = {};
            if(module!='purchase'){
                extraConfig.url= "AccEntityGST/importOutputGSTRuleSetup.do";
            }else{
                extraConfig.url= "AccEntityGST/importInputGSTRuleSetup.do";
            }
            
            var extraParams = this.isCustomer;
            extraConfig.isSales=module=='purchase'?false:true;
             extraConfig.isExcludeXLS=true;
            var importBtnArray = Wtf.documentImportMenuArray(this, this.moduleName,this.termStore, extraParams, extraConfig);
            
            this.importBtn = new Wtf.Action({
                text: WtfGlobal.getLocaleText("acc.common.import"),
                scope: this,
                tooltip: WtfGlobal.getLocaleText("acc.common.import"),
                iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
                menu: importBtnArray
            });
    
    this.Termwindow= new Wtf.Window({
        modal: true,
        id:'termselectionwindow'+module,
        title: module=='purchase' ? WtfGlobal.getLocaleText("acc.termselgrid.Editinputtax") : WtfGlobal.getLocaleText("acc.termselgrid.Editoutputtax"),
        bodyStyle: 'padding:5px;',
        iconCls: getButtonIconCls(Wtf.etype.deskera),
        buttonAlign: 'right',
        layout:"fit",
        width: 1000,
        height:600,               
        scope: this,
        items: [this.taxtab],
        buttons:
        [{
            text: WtfGlobal.getLocaleText("acc.rem.GSTRuleSetUp"),
            scope: this,
            handler: function(){  
                
               var  isOutputTax=module!='purchase'?true:false;
                 callGSTRuleSetup(undefined,isOutputTax);
            }
        },
        {
            text: WtfGlobal.getLocaleText("acc.rem.138"),
            scope: this,
            handler: function(){               
                this.TermGrid.createNewTerm(undefined, isGSTTab);
            }
        },{
            text:WtfGlobal.getLocaleText("acc.product.clone"),
            scope:this,
            disabled:true,
            id:"cloneTerm",
            handler: function(){
                var activeGrid = this.taxtab.getActiveTab().items.items[0];
                if(activeGrid.sm1.getCount()>0){
                    activeGrid.createNewTerm(activeGrid.sm1.getSelected());
                }else if (this.TermGridForAdditional.sm1.getCount() >0){
                    this.TermGridForAdditional.createNewTerm(this.TermGridForAdditional.sm1.getSelected());
                }                        
            }
        },{
            text: 'Save',
            scope:this,
            handler: function()
            {
                this.vatTypeforTermGrid.saveCompanyTerms() 
                this.exciseTypefortermGrid.saveCompanyTerms() 
                this.serviceTypefortermGrid.saveCompanyTerms()  
                this.cstTypeforTermGrid.saveCompanyTerms()
                //                        this.TermGrid.saveCompanyTerms(); // Tab,isAdditional
                this.TermGridForAdditional.saveCompanyTerms(); // Tab,isAdditional
                this.OtherChargesfortermGrid.saveCompanyTerms();
                this.GSTfortermGrid.saveCompanyTerms();
                this.Termwindow.close();
            }
        },{
            text: 'Cancel',
            scope:this,
            handler: function()
            {
                this.Termwindow.close();
            }
        },this.importBtn]
    });
//    this.taxtab.on('tabchange',function(){
//        if(this.taxtab.getActiveTab().title=="Tax/Other Charges"){
////            this.TermGrid.itemsgrid.getView().refresh();
//        }else{
////            this.TermGridForAdditional.itemsgrid.getView().refresh();
//        }
//    },this);
    this.Termwindow.show();
}
function callTermWindowNonIndia(module,isCallFromCompany){
    this.TermGrid = new Wtf.account.TermSelGrid({
        id: 'TermSelGrid',
        isReceipt: false,
        isCustomer:module=='purchase'?false:true,
        border: false,
        layout:"fit",
        height:500,
        autoScroll:true,
        //               cls:'gridFormat',
        region: 'center',
        isCallFromCompany:isCallFromCompany,
        isAdditionalTax:false,
        viewConfig:{
            forceFit:false
        },
        isEdit:this.isEdit,
        obj: this
    });
    this.TermGridForAdditional = new Wtf.account.TermSelGrid({
        id: 'TermGridForAdditional',
        isReceipt: false,
        isCustomer:module=='purchase'?false:true,
        border: false,
        layout:"fit",
        height:500,
        autoScroll:true,
        //               cls:'gridFormat',
        region: 'center',
        isCallFromCompany:isCallFromCompany,
        isAdditionalTax:true,
        viewConfig:{
            forceFit:false
        },
        isEdit:this.isEdit,
        obj: this
    });
    this.taxtab = new Wtf.TabPanel({
        activeTab: 0,
        frame:false, 
        items:[
        { 
            title: 'GST/ Taxes',
            items:[this.TermGrid],
            listeners: {
                activate: function(tab){
                    //   alert(tab.title);
                    tab.doLayout();
                },
                deactivate:function(tab){
                    tab.items.items[0].termStore.load();
                }
            }
        },

        {
            title: 'Additional Taxes',
            id:"addtionalTax",
            items:[this.TermGridForAdditional],
            scope:this,
            listeners: {
                activate: function(tab){
                    //  alert(tab.title);
                    tab.doLayout();
                },
                deactivate:function(tab){
                    tab.items.items[0].termStore.load();
                }
            }
        }
        ]
    });
    this.Termwindow= new Wtf.Window({
        modal: true,
        id:'termselectionwindow'+module,
        title: WtfGlobal.getLocaleText("acc.termselgrid.Edittax"),
        bodyStyle: 'padding:5px;',
        iconCls: getButtonIconCls(Wtf.etype.deskera),
        buttonAlign: 'right',
        layout:"fit",
        width: 1000,
        height:600,               
        scope: this,
        items: [this.taxtab],
        buttons:
        [{
            text: WtfGlobal.getLocaleText("acc.rem.138"),
            scope: this,
            handler: function(){
                this.TermGrid.createNewTerm(undefined);
            }
        },{
            text:WtfGlobal.getLocaleText("acc.product.clone"),
            scope:this,
            disabled:true,
            id:"cloneTerm",
            handler: function(){                        
                if(this.TermGrid.sm1.getCount()>0){
                    this.TermGrid.createNewTerm(this.TermGrid.sm1.getSelected());
                }else if (this.TermGridForAdditional.sm1.getCount() >0){
                    this.TermGridForAdditional.createNewTerm(this.TermGridForAdditional.sm1.getSelected());
                }                        
            }
        },{
            text: 'Save',
            scope:this,
            handler: function()
            {
                this.TermGrid.saveCompanyTerms();
                this.TermGridForAdditional.saveCompanyTerms();
                this.Termwindow.close();
            }
        },{
            text: 'Cancel',
            scope:this,
            handler: function()
            {
                this.Termwindow.close();
            }
        }]
    });
    this.taxtab.on('tabchange',function(){
        if(this.taxtab.getActiveTab().title=="GST/ Taxes"){
            this.TermGrid.itemsgrid.getView().refresh();
        }else{
            this.TermGridForAdditional.itemsgrid.getView().refresh();
        }
    },this);
    this.Termwindow.show();
}
