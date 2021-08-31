Wtf.MalasianGSTTab=function(config){
    this.nxtPointer = 0;
    this.previousPointer = 0;
    this.tabtype=config.tabtype;
    this.reportid = (config.tabtype == 'tapfile' ? Wtf.autoNum.EntityBasedGSTForm03 : (config.tabtype == 'auditfile' ? Wtf.autoNum.EntityBasedGSTAuditFile : Wtf.autoNum.EntityBasedGSTTabReturnFile));
    this.isTapReturn= this.tabtype=='tapreturnfile'?true:false;
    this.loadingMask = new Wtf.LoadMask(document.body,{
        msg : WtfGlobal.getLocaleText("acc.msgbox.50")
    });
    
    this.previousButton = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.setupWizard.previous"),
        id:'prev'+this.id,
        scope: this,
        tooltip:'Previous',
        handler: this.previousHandler
    });
    
    this.nextButton = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.setupWizard.next"),
        id:'next'+this.id,
        scope: this,
        tooltip:'Next',
        handler: this.nextHandler
    });
    
    this.cancelButton = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.CANCELBUTTON"),
        id:'closebtn'+this.id,
        scope: this,
        tooltip:'Cancel',
        handler: this.closeHandler
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


    this.statementDate= new Wtf.form.DateField({
        fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.endDate")+'*',
        format:WtfGlobal.getOnlyDateFormat(),
        value:this.getDates(false),
        anchor:'60%',
        name:"enddate",
        allowBlank:false
    });


        
    this.tbararray.push("To ",this.statementDate);
    this.tbararray.push(this.fetchButton);
    
    var exportReportArray=[];
 if(this.tabtype=='tapreturnfile'){
        exportReportArray.push(this.exportButton = new Wtf.Action({
            text: 'Export Report', 
            scope: this,
            tooltip: 'Export Report',
            handler: Wtf.account.companyAccountPref.isMultiEntity?this.generateGAFForMultiEntity.createDelegate(this,[false,this.isTapReturn]) : this.getValidDateRangeforTAPFile,
            iconCls: 'pwnd exportItem'
        }));
    }
    
    if(this.tabtype=='tapfile'){
        exportReportArray.push(
        this.export3Report = new Wtf.Action({
            text: 'GST-03 PDF Report', 
            scope: this,
            tooltip: 'Export GST-03 PDF Report',
            handler: this.exportGST3ReportHandler,
            iconCls: 'pwnd exportpdf'
        }));
//        exportReportArray.push(this.export3Report = new Wtf.Action({
//            text: 'GST Form 03', 
//            scope: this,
//            tooltip:'GST-03 PDF Report (CUSTOMS DEPARTMENT)', 
//            handler: this.exportGST3CustomDeptHandler,
//            iconCls: 'pwnd exportItem'
//        }));
        
        exportReportArray.push(this.export3CSVReport = new Wtf.Action({
            text: 'GST-03 CSV Report', 
            scope: this,
            tooltip: 'Export GST-03 CSV Report',
            handler: this.exportGST3CSVReportHandler,
            iconCls: 'pwnd exportItem'
        }));
    }
    if(this.tabtype=='auditfile'){
        exportReportArray.push(this.export3XMLReport = new Wtf.Action({
            text: 'GAF (XML) Report', 
            scope: this,
            tooltip: 'Export GST-03 XML Report',
            handler: Wtf.account.companyAccountPref.isMultiEntity ? this.generateGAFForMultiEntity.createDelegate(this,[true]) : this.exportGST3XMLReportHandler,
            iconCls: 'pwnd exportItem'
        }));

        exportReportArray.push(this.export3CSVReport = new Wtf.Action({
            text: 'GAF (Text) Report', 
            scope: this,
            tooltip: 'Export GAF Text Report',
            handler: Wtf.account.companyAccountPref.isMultiEntity ? this.generateGAFForMultiEntity.createDelegate(this,[false]) : this.exportGAFFileHandler,
            iconCls: 'pwnd exportItem'
        }));
    }
    this.exportMenu = new Wtf.Action({
        text: 'Export',
        scope: this,
        tooltip:'Export Report',
        iconCls: 'pwnd exportItem',
        menu:exportReportArray
    });

    this.tbararray.push(this.exportMenu);    
    if(this.tabtype=='tapfile'){
        this.tbararray.push(this.generateGSTForm3=new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.malaysiangst.generateGSTForm3"),
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.malaysiangst.generateGSTForm3"),
            iconCls:'pwnd downloadDoc',
            handler: Wtf.account.companyAccountPref.isMultiEntity ? this.generateGSTForm3HandlerForMultiEntity : this.generateGSTForm3Handler
        }));
    }
    
    this.GSTDetailViewButton = new Wtf.Toolbar.Button({
        text: 'GST Detail View', 
        scope: this,
        tooltip: 'GST Detail View',
        handler: this.GSTDetailViewHandler,
        iconCls:'accountingbase agedpayable'
    });
        this.tbararray.push(this.GSTDetailViewButton);
    this.gstFormGenerationHistoryBtn = new Wtf.Toolbar.Button({
        text: 'GST Form Generation History', 
        scope: this,
        tooltip: 'View GST Form Generation History',
        handler: this.viewGstFormGenerationHistory,
        iconCls:'accountingbase agedpayable'
    });
    if(this.tabtype=='tapfile'){
        this.tbararray.push(this.gstFormGenerationHistoryBtn);
    }
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), // "Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), // 'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton",
        hidden: !Wtf.account.companyAccountPref.isMultiEntity
    });
    this.tbararray.push(this.AdvanceSearchBtn);
    this.moduleHeaderRec = new Wtf.data.Record.create([
        {name:"taxname"},
        {name:"taxamount"},
        {name:"mergedCategoryData"},
        {name:"DmR"},
        {name:"IRR"}
    ]);

    this.moduleHeaderReader = new Wtf.data.KwlJsonReader({
        root:"data",
        totalProperty:"count"
    },this.moduleHeaderRec);

    this.moduleHeaderStore = new Wtf.data.GroupingStore({
        url: "ACCReports/getMalasianGSTForm5.do",
        reader:this.moduleHeaderReader,
        remoteSort:true,
        remoteGroup:true,
        baseParams:{
            enddate:this.getDates(false),
//                limit:30,
//                start:0,
            stdate:this.getDates(true),
            withoutinventory:false
        },
        groupField:"mergedCategoryData",
        sortInfo: {
            field: 'taxname',
            direction: "DESC"
        }
    });


    this.moduleHeaderStore.on('beforeload',function(){
        WtfGlobal.setAjaxTimeOut();
        this.moduleHeaderStore.baseParams.enddate=WtfGlobal.convertToGenericEndDate(this.statementDate.getValue());
        this.moduleHeaderStore.baseParams.stdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());

        this.loadingMask.show();
    },this);

    this.moduleHeaderStore.on('load',function(){
        WtfGlobal.resetAjaxTimeOut();
        this.loadingMask.hide();
        // PREPARED MSIC CODE BREAK UP ARRY TO LOAD IN BREAKDOWN GRID
        var breakUpArray = this.moduleHeaderStore.getAt(this.moduleHeaderStore.getTotalCount()-1);
        var finalArr = [];
        for(var i=0 ; i<breakUpArray.json.length;i++){
            var breakUpRec = breakUpArray.json[i];
            finalArr.push([breakUpRec.code,breakUpRec.outputtax,breakUpRec.percentage]);
        } 
        this.breakStoreStore.loadData(finalArr);
        var rec = this.moduleHeaderStore.getAt(this.moduleHeaderStore.getTotalCount()-2);
        this.DmrAndIRRTplTplSummary.overwrite(this.DmrAndIRRTpl.body, {
            DmR : rec.data.DmR,
            IRR : rec.data.IRR
        });
    },this);

    this.moduleHeaderStore.on('loadexception',function(){
        WtfGlobal.resetAjaxTimeOut();
        this.loadingMask.hide();
    },this);
//    this.moduleHeaderStore.load();

    this.moduleHeaderColumn = new Wtf.grid.ColumnModel([
        {
            header:"Catagory Type",
            hidden:true,
            dataIndex:"mergedCategoryData"

        },{
            header:"Tax",
            dataIndex:"taxname",
            sortable:false,
            groupRenderer: WtfGlobal.nameRenderer

        },{
            header:"Tax Amount",
            dataIndex:"taxamount",
            hidden:false,
            renderer:function(value,m,rec){
                this.clamabletaxAmount;
                var val="";
                if (rec.data.taxname == Wtf.GST_Amount_Claimable) {
                    this.clamabletaxAmount = rec.data.taxamount;
                }
                if(value == 'chkboxval'){
                    if (this.clamabletaxAmount > 0) {  // Disable carryForword checkbox when clamabletaxAmount is less than zero ERP-40245
                        val = '<input type="checkbox" id="carryforwardchk" >';
                    } else {
                        val = '<input type="checkbox" id="carryforwardchk" disabled >';
                    }
                    
                }else{
                    var symbol=((rec==undefined||rec.data.currencysymbol==null||rec.data['currencysymbol']==undefined||rec.data['currencysymbol']=="")?WtfGlobal.getCurrencySymbol():rec.data['currencysymbol']);
                    var v=parseFloat(value);
                    if(isNaN(v)) return value;
                    v= WtfGlobal.conventInDecimal(v,symbol)
                    val = '<div class="currency">'+v+'</div>';
                }
                return val;       
            },
            editor: new Wtf.form.NumberField({
            })
        }
    ]);
    this.groupingView = new Wtf.grid.GroupingView({
        forceFit: true,
        showGroupName: false,
        enableGroupingMenu: true,
//            groupTextTpl: 'grp tpl',
        hideGroupedColumn:true,
        emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec") + "<br>" + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn")),
    });

    this.moduleHeaderGrid = new Wtf.grid.EditorGridPanel({
        store:this.moduleHeaderStore,
        border:false,
//            region:"center",
        height:500,
//            loadMask:true,
//            plugins:checkColumn,
        cm:this.moduleHeaderColumn,
        view: this.groupingView
    });
    this.moduleHeaderGrid.on('beforeedit',this.checkRow,this);
    
    this.moduleHeaderGrid.on("afteredit", this.afterEdit, this);
    
    /**
     * Apply Empty Text.
     */
    this.moduleHeaderGrid.on("render",function(g,l){
        this.moduleHeaderGrid.getView().applyEmptyText();
    },this);

    // Middle Layer Additional Information
    
    this.additionalHeaderRec = new Wtf.data.Record.create([
        {name:"taxname"},
        {name:"taxamount"},
        {name:"mergedCategoryData"}
    ]);

    this.additionalHeaderReader = new Wtf.data.KwlJsonReader({
        root:"data",
        totalProperty:"count"
    },this.additionalHeaderRec);

    this.additionalHeaderStore = new Wtf.data.GroupingStore({
        url: "ACCReports/getGSTForm5Additional.do",
        reader:this.additionalHeaderReader,
        remoteSort:true,
        remoteGroup:true,
        baseParams:{
            enddate:this.getDates(false),
//                limit:30,
//                start:0,
            stdate:this.getDates(true),
            withoutinventory:false
        },
        groupField:"mergedCategoryData",
        sortInfo: {
            field: 'taxname',
            direction: "DESC"
        }
    });

    this.additionalHeaderStore.on('beforeload',function(){
        WtfGlobal.setAjaxTimeOut();
        this.additionalHeaderStore.baseParams.enddate=WtfGlobal.convertToGenericEndDate(this.statementDate.getValue());
        this.additionalHeaderStore.baseParams.stdate=WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
    },this);

//    this.additionalHeaderStore.load();

    this.additionalHeaderColumn = new Wtf.grid.ColumnModel([
        {
            header:"Catagory Type",
            hidden:true,
            dataIndex:"mergedCategoryData"

        },{
            header:"Tax",
            dataIndex:"taxname",
            sortable:true,
            groupRenderer: WtfGlobal.nameRenderer

        },{
            header:"Tax Amount",
            dataIndex:"taxamount",
            hidden:false,
            renderer:WtfGlobal.withoutRateCurrencySymbol,
            editor: new Wtf.form.NumberField({
            })
        }
    ]);

    this.additionalgroupingView = new Wtf.grid.GroupingView({
        forceFit: true,
        showGroupName: false,
        enableGroupingMenu: true,
//            groupTextTpl: 'grp tpl',
        hideGroupedColumn:true
    });

    this.additionalHeaderGrid = new Wtf.grid.EditorGridPanel({
        store:this.additionalHeaderStore,
        border:false,
        hidden:true, 
//            region:"center",
        height:500,    
//            loadMask:true,
//            plugins:checkColumn,
        cm:this.additionalHeaderColumn,
        view: this.additionalgroupingView
    });
        
        
        
        
    // Breakdown Code   
        
    this.summary = new Wtf.ux.grid.GridSummary();

    this.breakStoreRec = new Wtf.data.Record.create([
        {name:"code"},
        {name:"outputtax"},
        {name:"percentage"},
        {name:"recordtype"},
        {name:"recordVal"},
        {name:"mergedCategoryData"}
    ]);

    this.breakStoreRecReader = new Wtf.data.KwlJsonReader({
        root:"data",
        totalProperty:"count"
    },this.breakStoreRec);
  
    this.breakStoreStore = new Wtf.data.SimpleStore({
        fields: [
           {name: 'code'},
           {name: 'outputtax'},
           {name: 'percentage'},
           ]
    });
 
    this.breakStoreGrid = new Wtf.grid.GridPanel({
        store: this.breakStoreStore,
        columns: [
        {
            header:"Code",
            hidden:false,
            dataIndex:"code"          
        },{
            header:"Value of Out Put Tax",
            dataIndex:"outputtax",
            hidden:false,
            renderer:WtfGlobal.withoutRateCurrencySymbol
        },{
            header:"Percentage",
            dataIndex:"percentage",
            hidden:false,
            summaryType:'sum',
            renderer:function(v,m,rec){
                return v+ "%";
            }
        }
        ],
        viewConfig:{
              forceFit:true
        },
        height:500,
        border:false,
        title: WtfGlobal.getLocaleText("acc.malaysiangst.breakDownGrid.title")
    });

    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.moduleHeaderGrid.colModel,
        moduleidarray: Wtf.MultiEntityReportsModuleIdArray.split(','),
        reportid:this.reportid,
        advSearch: false,
        parentPanelSearch: this,
        ignoreDefaultFields: true,
        isAvoidRedundent: true,
        isMultiEntity: true,
        isGSTForm3Generation:(config.tabtype == 'tapfile') ? true:false
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    
    this.DmrAndIRRTplTplSummary = new Wtf.XTemplate(
            '<div class="currency-view">',
            '<hr class="templineview">',
            '<table width="400">', //       
            '<tr><td><b>' + WtfGlobal.getLocaleText("acc.malaysianGst.DmR") + ' : </b></td><td text-align=right> {DmR}</td></tr>',
            '<tr><td><b>' + WtfGlobal.getLocaleText("acc.malaysianGst.IRR") + ' : </b></td><td text-align=right>{IRR}</td></tr>',
            '</table>',
            '<hr class="templineview">',
            '</div>'
            );
    this.DmrAndIRRTpl = new Wtf.Panel({
        id:this.id+'DmrAndIRRTpl',
        border: false,
        height:50,
        baseCls: 'tempbackgroundview paymentformbankbaldiv',
        width: '100%',
        html: this.DmrAndIRRTplTplSummary.apply({
            DmR: "&nbsp;&nbsp;&nbsp;&nbsp;",
            IRR: "&nbsp;&nbsp;&nbsp;&nbsp;"
        })
    });       
    this.wrapperPanel = new Wtf.Panel({
        border:false,
        layout:"border",
        scope:this,
        items:[this.objsearchComponent,
        this.centerPanel = new Wtf.Panel({
            width:'90%',
            region:'center',
            layout:'fit',
            border:false,
            items:[this.moduleHeaderGrid,this.additionalHeaderGrid,this.breakStoreGrid],
            tbar:this.tbararray
        }),
        this.southPanel = new Wtf.Panel({
            region: "south",
            layout: "fit",
            id:this.id+'southPanel',
            width:'90%',
            border:false,
            autoHeight: true,
            items:[this.DmrAndIRRTpl]
        })
        ]
    });
    
    Wtf.apply(this,{
        defaults:{border:false,bodyStyle:"background-color:white;"},
        items:this.wrapperPanel,
        bbar:[this.previousButton, this.nextButton, this.cancelButton]
    },config);        

    Wtf.MalasianGSTTab.superclass.constructor.call(this,config);

}

Wtf.extend(Wtf.MalasianGSTTab, Wtf.Panel, {
    onRender:function(config){
        Wtf.MalasianGSTTab.superclass.onRender.call(this, config);
        if(this.tabtype=='tapfile'){
            this.getValidDateRangeForFileGeneration(false,undefined,true);
        }
        /*
         * SDP-11742 : Stop auto loading of GST form 3
         */
//        this.loadAllStores();
    },
    exportReportHandler:function(){
        var carryForward = document.getElementById("carryforwardchk") != null ? document.getElementById("carryforwardchk").checked : false;
        var url = "ACCReports/exportMalasianGSTForm3CSV.do?stdate="+WtfGlobal.convertToGenericStartDate(this.startDate.getValue())+"&enddate="+WtfGlobal.convertToGenericEndDate(this.statementDate.getValue())+"&carryForward="+carryForward+"&FileType=txt"+"&tapReturnFile="+this.isTapReturn;
        if (this.searchJson != "" && this.searchJson != undefined) {
            url += "&searchJson=" + this.searchJson + "&filterConjuctionCriteria=" + this.filterConjuctionCrit + "&multiEntityValue=" + this.objsearchComponent.advGrid.getSearchJSON().combosearch;
        }
        Wtf.get('downloadframe').dom.src = url;
    },
    exportGST3CSVReportHandler:function(){  //handler defination was missing  so enable to export
        var url = "ACCReports/exportMalasianGSTForm3CSV.do?stdate="+WtfGlobal.convertToGenericStartDate(this.startDate.getValue())+"&enddate="+WtfGlobal.convertToGenericEndDate(this.statementDate.getValue())+"&FileType=csv";
        if (this.searchJson != "" && this.searchJson != undefined) {
            url += "&searchJson=" + this.searchJson + "&filterConjuctionCriteria=" + this.filterConjuctionCrit + "&multiEntityValue=" + this.objsearchComponent.advGrid.getSearchJSON().combosearch;
        }
        Wtf.get('downloadframe').dom.src = url;
    },
     exportGST3XMLReportHandler:function(){  //handler defination was missing  so enable to export
 
        Wtf.MessageBox.alert(WtfGlobal.getLocaleText("Info"), WtfGlobal.getLocaleText("erp.export.longerTimeMsg"), function (btn) {
            var url = "";
            if (btn == "ok") {
                if (Wtf.Countryid == Wtf.Country.MALAYSIA) {
                    url = "AccGST/exportXMLGAFFile.do?stdate=" + WtfGlobal.convertToGenericStartDate(this.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericEndDate(this.statementDate.getValue());
                } else {
                    url = "ACCReports/exportXMLIAFfile.do?stdate=" + WtfGlobal.convertToGenericStartDate(this.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericEndDate(this.statementDate.getValue());
                }
                if (this.searchJson != "" && this.searchJson != undefined) {
                    url += "&searchJson=" + this.searchJson + "&filterConjuctionCriteria=" + this.filterConjuctionCrit + "&multiEntityValue=" + this.objsearchComponent.advGrid.getSearchJSON().combosearch;
                }
                Wtf.get('downloadframe').dom.src = url;
            }
        }, this);
        
    },
    exportGAFFileHandler:function(){  //GAF Text File From Statutory

        Wtf.MessageBox.alert(WtfGlobal.getLocaleText("Info"), WtfGlobal.getLocaleText("erp.export.longerTimeMsg"), function (btn) {
            var url = "";
            if (btn == "ok") {
                if (Wtf.Countryid == Wtf.Country.MALAYSIA) {
                    url = "AccGST/exportTXTGAFFile.do?stdate=" + WtfGlobal.convertToGenericStartDate(this.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericEndDate(this.statementDate.getValue()) + "&reportType=1";
                } else {
                    url = "ACCReports/exportIAFfile.do?stdate=" + WtfGlobal.convertToGenericStartDate(this.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericEndDate(this.statementDate.getValue()) + "&reportType=1";
                }
                if (this.searchJson != "" && this.searchJson != undefined) {
                    url += "&searchJson=" + this.searchJson + "&filterConjuctionCriteria=" + this.filterConjuctionCrit + "&multiEntityValue=" + this.objsearchComponent.advGrid.getSearchJSON().combosearch;
                }
                Wtf.get('downloadframe').dom.src = url;
            }
        },this);
    },
    exportGST3ReportHandler:function(){
        var url = "ACCReports/exportMalasianGSTForm3CSV.do?stdate="+WtfGlobal.convertToGenericStartDate(this.startDate.getValue())+"&enddate="+WtfGlobal.convertToGenericEndDate(this.statementDate.getValue())+"&FileType=pdf";
        if (this.searchJson != "" && this.searchJson != undefined) {
            url += "&searchJson=" + this.searchJson + "&filterConjuctionCriteria=" + this.filterConjuctionCrit + "&multiEntityValue=" + this.objsearchComponent.advGrid.getSearchJSON().combosearch;
        }
        Wtf.get('downloadframe').dom.src = url;
    },
    exportGST3CustomDeptHandler:function(){
        var url = "ACCReports/exportGSTFORM3JASPER.do?stdate="+WtfGlobal.convertToGenericStartDate(this.startDate.getValue())+"&enddate="+WtfGlobal.convertToGenericEndDate(this.statementDate.getValue());
        if (this.searchJson != "" && this.searchJson != undefined) {
            url += "&searchJson=" + this.searchJson + "&filterConjuctionCriteria=" + this.filterConjuctionCrit + "&multiEntityValue=" + this.objsearchComponent.advGrid.getSearchJSON().combosearch;
        }
        Wtf.get('downloadframe').dom.src = url;
    },
  
    fetchReportHandler:function(){
       /*This code to check the date on the Fetch*/
       if(this.startDate.getValue()!='' && this.statementDate.getValue()!=''){
                /*
                 * Start date not allowed to be earlier than book begining date
                 */
                if(Wtf.account.companyAccountPref.bbfrom){
                    var bbDate = Wtf.account.companyAccountPref.bbfrom;
                    var monthDateStr = bbDate.format('M d');
                    var openingDocDate = new Date(monthDateStr + ', ' + bbDate.getFullYear() + ' 12:00:00 AM');
                    if(this.startDate.getValue().getTime()<openingDocDate.getTime()){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.malaysianGST.fromDateCanNotOlder")], 2);
                        return;
                    }
                }
                if(this.startDate.getValue().getTime()>this.statementDate.getValue().getTime()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                    return;
                }
            }
        this.moduleHeaderStore.load();
        
        this.additionalHeaderStore.load();
        
//        this.breakStoreStore.load();
        
        this.moduleHeaderGrid.show();
        this.breakStoreGrid.hide();
        this.additionalHeaderGrid.hide();
        this.previousPointer =0;
        this.nxtPointer=0;
        this.checkForProperDateFilterForDmR();
    },
    
    checkRow:function(obj){
        var rec=obj.record;
        if(obj.field=="taxamount"){
            if(rec.get('taxamount') == 'chkboxval'){
                obj.cancel = true;
                return;
            }
        }
    },
    
    checkRowOfBreakDown:function(obj){
        var rec=obj.record;
        if(obj.field=="code"){
            if(rec.get('recordtype') == 'textfieldValue'){
                obj.cancel = true;
                return;
            }
        }
    },
    
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
    
    afterEdit:function(e){
        var rec = e.record;
//        if (e.field=='newheader'){
//           rec.data[e.field]=WtfGlobal.HTMLStripper(rec.data.newheader)
//        }
    },
    
    nextHandler:function(){
        if(this.nxtPointer == 0){
            this.moduleHeaderGrid.hide();
            this.additionalHeaderGrid.show();
            this.breakStoreGrid.hide();
            this.previousPointer++;
            this.nxtPointer++;
        }else if(this.nxtPointer == 1){
            this.moduleHeaderGrid.hide();
            this.additionalHeaderGrid.hide();
            this.breakStoreGrid.show();
            this.previousPointer++;
            this.nxtPointer++;
        }
        
        if(this.nxtPointer == 2){
            this.cancelButton.setText('Save and Close');
        }else{
            this.cancelButton.setText('Cancel');
        }
        
        this.doLayout();
    },
    
    previousHandler:function(){
        if(this.previousPointer == 1){
            this.moduleHeaderGrid.show();
            this.breakStoreGrid.hide();
            this.additionalHeaderGrid.hide();
            this.previousPointer--;
            this.nxtPointer--;
        }else if(this.previousPointer == 2){
            this.moduleHeaderGrid.hide();
            this.additionalHeaderGrid.show();
            this.breakStoreGrid.hide();
            this.previousPointer--;
            this.nxtPointer--;
        }
        this.cancelButton.setText('Cancel');
        
        this.doLayout();
    },
    
    closeHandler:function(){
        Wtf.getCmp('as').remove(Wtf.getCmp(this.id));
    },
    
    GSTDetailViewHandler:function(){
        NewGSTForm5DetailedView();
    },
    generateGSTForm3Handler:function(){
        var today = new Date();
        var endDate = this.statementDate.getValue();
        if(today.getTime()<endDate.getTime()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.malaysiangst.canNotGenerateFileBeforeEndDate")],2);
            return;
        }
        this.getValidDateRangeForFileGeneration(true); // Function to get appropriate dates for geneating the GST form 3
    },
    generateGSTForm3HandlerForMultiEntity: function () {
        var today = new Date();
        var endDate = this.statementDate.getValue();
        /*
         * Allow only one Entity in advanced search while exporting GST Form03
         */
        if (this.objsearchComponent.isVisible()) {//To check advance search panel is visible or not
            var searchStore = this.objsearchComponent.advGrid.searchStore;
            if (searchStore.getCount() === 1) {//To check search column count is not greater than one
                var searchText = searchStore.data.items[0].data.searchText;
                var combosearch = searchStore.data.items[0].data.combosearch;
                var array = searchText.split(',');
                if (array.length === 1) {//To check count of selected entity
                    Wtf.Ajax.requestEx({//To Check selected entity mapped with GST details or not
                        url: 'AccGST/getEntityDetails.do',
                        params: {
                            multiEntityValue: combosearch
                        }
                    }, this,
                    function (response, req) {
                        if (response.success) {
                            if (response.count !== 1) {
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.entity.form03.entityMap.alert")], 2);
                                return;
                            } else {
                                    this.getValidDateRangeForFileGeneration(false, combosearch);//Reset start and end date for selected entity if initiallly start & end date was set according to another entity
                                    if (today.getTime() < endDate.getTime()) {
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.malaysiangst.canNotGenerateFileBeforeEndDate")], 2);
                                        return;
                                    } else {
                                        this.objsearchComponent.advGrid.doSearch();
                                        this.getValidDateRangeForFileGeneration(true, combosearch); // Function to get appropriate dates for geneating the GST form 3
                                    }
                                    }
                                }
                    }, function (response) {
                        if (!response.success) {
                            var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                        }});
                } else {//You have selected multiple Entity.Please specify a single Entity from Search Text.
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.form03.single.entity.alert")], 2);
                    return;
                }
            } else {//Please select a single Entity from Search Text and Try again to Generate GST Form 03.
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.form03.SearchTerm.alert")], 2);
                return;
            }
        } else {//Multi Entity Flow is activated. Please select the Entity from Advanced Search to Generate GST Form 03.
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.form03.generation.alert")], 2);
            return;
        }
    },
    loadAllStores:function(){
        this.moduleHeaderStore.load();
        this.additionalHeaderStore.load();
//        this.breakStoreStore.load();
    },
    getValidDateRangeForFileGeneration:function(isGenerateFile,multiEntityValue,isOnRenderCall){
        
        Wtf.Ajax.requestEx({
            url: 'AccGST/getValidDateRangeForFileGeneration.do',
            params: {
                multiEntityValue: multiEntityValue !== undefined ? multiEntityValue : ""
            }
        }, this,
        function(response, req) {
            if(response.success){
                if(response.startDate!='' && response.startDate!=undefined && response.startDate!=null && response.endDate!='' && response.endDate!=undefined && response.endDate!=null){
                    if(isGenerateFile){
                        if((this.startDate.getValue()).getTime()!= (new Date(response.startDate)).getTime() || (this.statementDate.getValue()).getTime() != (new Date(response.endDate).getTime())){
                           Wtf.MessageBox.show({
                                title: WtfGlobal.getLocaleText("acc.common.alert"),
                                msg: WtfGlobal.getLocaleText("acc.malaysiangst.startAndEndDateNotValid"),
                                width:450,
                                scope: {
                                    scopeObj:this  
                                },
                                buttons: Wtf.MessageBox.OK,
                                fn: function(btn ,text, option) {
                                    this.scopeObj.startDate.setValue(new Date(response.startDate));
                                    this.scopeObj.statementDate.setValue(new Date(response.endDate));
                                },
                                animEl: 'mb9',
                                icon: Wtf.MessageBox.WARNING
                            });
                           
                        } else{
                            this.checkForPendingTransactions();  // Check for claimable invoices which are not claimed yet and check for un-invoices Delivery orders.
                        }
                    }
                    this.startDate.setValue(new Date(response.startDate));
                    this.statementDate.setValue(new Date(response.endDate));
                }
            }
            if (!isOnRenderCall) {//SDP-11742
                this.loadAllStores();
            }
        },function(response){
            Wtf.MessageBox.hide();
            var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
            this.loadAllStores();
        });
    },
    
    // Check for claimable invoices which are not claimed yet and check for un-invoices Delivery orders.
    checkForDueInvoicesAndDOs:function(){
        var badDebtCalculationDate = this.statementDate.getValue();
        var today = new Date();
        if (today.getTime() < badDebtCalculationDate.getTime()) {//Entity Flow is activated and startdate and enddate are selected according to Entity1 but user goes to generate GST Form3 for Entity2 and Entity2 enddate>todays date
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.malaysiangst.canNotGenerateFileBeforeEndDate")], 2);
            return;
        }
        
        var badDebtPeriod = Wtf.account.companyAccountPref.badDebtProcessingPeriod;
        if(Wtf.account.companyAccountPref.badDebtProcessingPeriodType == Wtf.BadDebtProcessingType.Months){
            badDebtCalculationDate = badDebtCalculationDate.getLastDateOfMonth();
            badDebtCalculationDate = badDebtCalculationDate.add(Date.MONTH, -badDebtPeriod);
        } else {
            badDebtCalculationDate = badDebtCalculationDate.add(Date.DAY, -badDebtPeriod);
        }
        var enddate=this.statementDate.getValue();
        enddate = enddate.add(Date.DAY, -21);
        
        Wtf.Ajax.requestEx({
            url: 'AccGST/checkForDueInvoicesAndDOs.do',
            params: {
                badDebtCalculationDate : WtfGlobal.convertToGenericDate(badDebtCalculationDate),
                nondeleted :true,
                enddate : WtfGlobal.convertToGenericDate(enddate),
                searchJson: this.searchJson,
                filterConjuctionCriteria: this.filterConjuctionCrit
            }
        },this,
        function(response, req) {
            if(response.success){
                var msg='';
                if(response.purchaseInvoiceExists){  // Unclaimed PI found
                    msg += WtfGlobal.getLocaleText("acc.malaysiangst.claimablePINotClaimed")+' '+"<a href='#' onclick='BadDebtPurchaseInvoices(\"BadDebtPurchaseInvoicesReportTabId\",\"true\")'>Click here</a>"+' '+WtfGlobal.getLocaleText("acc.malaysiangst.forBadDebtClaimablePurchaseInvoices")
                } 
                if(response.salesInvoiceExists){      // Unclaimed SI found
                    msg += '<br>'+WtfGlobal.getLocaleText("acc.malaysiangst.claimableSINotClaimed")+' '+"<a href='#' onclick='BadDebtInvoices(\"BadDebtInvoicesReportTabId\",\"true\")'>Click here</a>"+' '+WtfGlobal.getLocaleText("acc.malaysiangst.forBadDebtClaimableSalesInvoices")
                }
                if(response.doExists){               // Uninvoicesd DO found
                    msg += '<br>'+WtfGlobal.getLocaleText("acc.malaysiangst.applicableDONotApplied")+' '+"<a href='#' onclick='getTaxableDeliveryOrdersPanel(\"getTaxableDeliveryOrdersPanel\",\"true\")'>Click here</a>"+' '+WtfGlobal.getLocaleText("acc.malaysiangst.forApplicableDO")
                }
                if(msg != ''){
                    msg += '<br>'+WtfGlobal.getLocaleText("acc.malaysiangst.sureToGenerateFile");
                     Wtf.MessageBox.show({
                                title: WtfGlobal.getLocaleText("acc.common.alert"),
                                msg: msg,
                                width:450,
                                scope: {
                                    scopeObj:this  
                                },
                                buttons: Wtf.MessageBox.YESNO,
                                fn: function(btn ,text, option) {
                                    if(btn == 'yes'){
                                        this.scopeObj.exportGST3CustomDeptHandler(); 
                                    }
                                },
                                animEl: 'mb9',
                                icon: Wtf.MessageBox.WARNING
                            });
                } else {
                    this.exportGST3CustomDeptHandler(); 
                }
            }
        },function(response){
            Wtf.MessageBox.hide();
            var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        });
    },
    
    /*
     * Function for finding pending transactions in selected period.
     */ 
    checkForPendingTransactions : function(){
        var startdate = this.startDate.getValue();
        var enddate = this.statementDate.getValue();
        
        Wtf.Ajax.requestEx({
            url: 'AccGST/checkForPendingTransactions.do',
            params: {
                nondeleted :true,
                startdate : WtfGlobal.convertToGenericDate(startdate),
                enddate : WtfGlobal.convertToGenericDate(enddate),
                searchJson: this.searchJson,
                filterConjuctionCriteria: this.filterConjuctionCrit
            }
        },this,
        function(response, req) {
            if(response.success){                
                if(response.isTransactionExists){  
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.malaysiangst.pendingTransactionsExists")+" "+ "<a href='#' onclick='callPendingApprovalsReport(true)'>"+WtfGlobal.getLocaleText("acc.WoutI.46")+"</a>"],2);
                    return ;
                }else {
                    this.checkForDueInvoicesAndDOs(); 
                }
            }
        },this.genFailureResponse);
    },
    genFailureResponse:function(response){
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    viewGstFormGenerationHistory:function(){
        callGstFormGenerationHistoryReportDynamicLoad();
    },
    checkForProperDateFilterForDmR : function(){
        var startDate = this.startDate.getValue();
        var firstDate = startDate.getFirstDateOfMonth();
        if(startDate.setHours(0, 0, 0, 0) != firstDate.setHours(0, 0, 0, 0)){
            this.southPanel.hide();
        } else {
            var endDate = this.statementDate.getValue();
            var lastDate = endDate.getLastDateOfMonth(); 
            if(endDate.setHours(0, 0, 0, 0) != lastDate.setHours(0, 0, 0, 0)){
                this.southPanel.hide();
            } else {
                this.southPanel.show();
            }
        }
        this.doLayout();
    },
    configurAdvancedSearch: function () {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AdvanceSearchBtn.disable();
        this.doLayout();
    },
    filterStore: function (json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.moduleHeaderStore.baseParams = {
            searchJson: this.searchJson,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.moduleHeaderStore.load();

        this.additionalHeaderStore.baseParams = {
            searchJson: this.searchJson,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.additionalHeaderStore.load();

//        this.breakStoreStore.baseParams = {
//            searchJson: this.searchJson,
//            filterConjuctionCriteria: filterConjuctionCriteria
//        }
//        this.breakStoreStore.load();
    },
    clearStoreFilter: function () {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.moduleHeaderStore.baseParams = {
            searchJson: this.searchJson,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.moduleHeaderStore.load();

        this.additionalHeaderStore.baseParams = {
            searchJson: this.searchJson,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.additionalHeaderStore.load();

//        this.breakStoreStore.baseParams = {
//            searchJson: this.searchJson,
//            filterConjuctionCriteria: this.filterConjuctionCrit
//        }
////        this.breakStoreStore.load();
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
        },
    generateGAFForMultiEntity: function (isXMLFile,isTapReturn) {
        /*
         * If Multi Entity flow is acivated then allow export GAF for single Entity only.
         */
        var msg="";
        if (this.objsearchComponent.isVisible()) {//To check advance search panel is visible or not
            var searchStore = this.objsearchComponent.advGrid.searchStore;
            if (searchStore.getCount() === 1) {//To check search column count is not greater than one
                var searchText = searchStore.data.items[0].data.searchText;
                var combosearch = searchStore.data.items[0].data.combosearch;
                var array = searchText.split(',');
                if (array.length === 1) {//To check count of selected entity
                    Wtf.Ajax.requestEx({//To Check selected entity mapped with GST details or not
                        url: 'AccGST/getEntityDetails.do',
                        params: {
                            multiEntityValue: combosearch
                        }
                    }, this,
                    function (response, req) {
                                if (response.success) {
                                    if (response.count !== 1) {
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.gst.entity.form03.entityMap.alert")], 2);
                                        return;
                                    } else {
                                        if (isTapReturn) {
                                            this.getValidDateRangeforTAPFile(combosearch); //pass multiEntityValue
                                        } else {
                                            if (isXMLFile) {
                                                this.exportGST3XMLReportHandler();
                                            } else {
                                                this.exportGAFFileHandler();
                                            }
                                        }
                                    }
                                }
                    }, function (response) {
                        if (!response.success) {
                            var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                        }});
                } else {//You have selected multiple Entity.Please specify a single Entity from Search Text.
                    msg = isTapReturn ? WtfGlobal.getLocaleText("acc.gst.tap.single.entity.alert") : WtfGlobal.getLocaleText("acc.gst.form03.single.entity.alert");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                    return;
                }
            } else {//Please select a single entity from Search Text and try again to export GAF.
                 msg = isTapReturn ? WtfGlobal.getLocaleText("acc.gst.tap.SearchTerm.alert") : WtfGlobal.getLocaleText("acc.gst.gaf.SearchTerm.alert");
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg], 2);
                return;
            }
        } else {//Multi Entity Flow is activated. Please select the entity from advanced search to export GAF.
            msg = isTapReturn ? WtfGlobal.getLocaleText("acc.gst.tap.export.alert") :  WtfGlobal.getLocaleText("acc.gst.gaf.export.alert");
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
            return;
        }
    },
    
    getValidDateRangeforTAPFile: function (multiEntityValue) {
        var startdate = this.startDate.getValue();
        var enddate = this.statementDate.getValue();
        Wtf.Ajax.requestEx({
            url: 'AccGST/getValidDateRangeForTAPFileGeneration.do',
            params: {
                startdate: WtfGlobal.convertToGenericDate(startdate),
                enddate: WtfGlobal.convertToGenericDate(enddate),
                multiEntityValue: multiEntityValue != undefined ? multiEntityValue : ""
            }
        }, this,
                function (response, req) { //success
                    if (response.success) {
                        if (this.tabtype == 'tapreturnfile') {
                            if (response.startdate != '' && response.startdate != null && response.startdate != undefined && response.enddate != '' && response.enddate != null && response.enddate != undefined) {
                                var msg = "";
                                if (response.submissionCriteria == Wtf.GST_Monthly_Submission) {
                                    msg = WtfGlobal.getLocaleText("acc.gst.tap.monthlydateperiod.alert") + " " + "<b>" + response.startdate + "</b>" + " to " + "<b>" + response.enddate + "</b>" + " " + WtfGlobal.getLocaleText("acc.gst.tap.todate") + WtfGlobal.getLocaleText("acc.msgbox.Doyouwanttoproceed");
                                } else {
                                    msg = WtfGlobal.getLocaleText("acc.gst.tap.quarterlydateperiod.alert") + " " + "<b>" + response.startdate + "</b>" + " to " + "<b>" + response.enddate + "</b>" + " " + WtfGlobal.getLocaleText("acc.gst.tap.todate") + WtfGlobal.getLocaleText("acc.msgbox.Doyouwanttoproceed");
                                }
                                Wtf.MessageBox.show({
                                    title: WtfGlobal.getLocaleText("acc.common.alert"),
                                    msg: msg,
                                    width: 450,
                                    scope: this,
                                    closable: true,
                                    buttons: Wtf.MessageBox.YESNO,
                                    fn: function (btn, text, option) {
                                        if (btn == 'yes') {
                                            this.startDate.setValue(new Date(response.startdate));
                                            this.statementDate.setValue(new Date(response.enddate));
                                            this.loadAllStores();
                                            this.exportReportHandler();
                                        }
                                    },
                                    animEl: 'mb9',
                                    icon: Wtf.MessageBox.WARNING
                                });
                            }
                        }
                    } else {
                        if (!response.success) {
                            if (response.isinvaliddate && response.msg) {
                                msg = response.msg;
                            }
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                        }
                    }
                },
                function (response, req) {//faliure
                    if (!response.success) {
                        var msg = WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                    }
                }
        )
    }

});
