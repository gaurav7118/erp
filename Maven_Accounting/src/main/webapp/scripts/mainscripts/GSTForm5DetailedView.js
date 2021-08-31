Wtf.account.GSTForm5DetailedView=function(config){
    this.summary = new Wtf.ux.grid.GridSummary();
    this.ReportType=(config.id=="GSTForm5")?3:4;
    this.startDateParams=config.startdate;
    this.endDateParams=config.enddate;
    
    var budgetRecord = Wtf.data.Record.create([
        {
            name: 'taxname'
        }, {
            name: 'transactionid'
        },  {
            name: 'journalentry'
        },  {
            name: 'name'
        },  {
            name: 'taxamount'
        },  {
            name: 'mergedCategoryData'
        },  {
            name: 'box'
        },  {
            name: 'fmt'
        },  {
            name: 'level'
        },{
            name: 'currencyid'
        },{
            name: 'currencysymbol' 
        },{
            name: 'currencyname'
        },{
            name: 'currencycode'
        },{
            name: 'billid'
        },{
            name: 'noteid'
        },{
            name: 'type'
        },{
            name: 'jeid'
        },{
            name: 'transactionexchangerate'
        },{
            name: 'originalamount'
        },{
            name: 'transactioncurrencysymbol'
        },{
            name: 'jedate'
        },{
            name:'remark'
        },{
            name:'memo'
        },{
            name:"DmR"
        },{
            name:"IRR"
        },{
            name:"isLeaseFixedAsset"
        }]);

    this.groupStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            root: "data"                          
        }, budgetRecord),
        baseParams:{
            // nondeleted:true
        },
        url: (Wtf.account.companyAccountPref.countryid=='137')?"ACCReports/getMalasianGSTForm5DetailedView.do":"ACCReports/getGSTForm5DetailedView.do"
    });
    WtfGlobal.setAjaxTimeOutFor30Minutes();

    this.groupStore.on('load', function() {
        WtfGlobal.resetAjaxTimeOut();
        mainPanel.loadMask.hide();
        this.grid.getView().refresh();
        this.expandCollapseGrid("Collapse");
        var rec = this.groupStore.getAt(this.groupStore.getTotalCount()-1);
        this.DmrAndIRRTplTplSummary.overwrite(this.DmrAndIRRTpl.body, {
            DmR : rec.data.DmR,
            IRR : rec.data.IRR
        });
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.MALAYSIA){
            this.checkForProperDateFilterForDmR();
        }
    }, this);
    
    this.groupStore.on('loadexception', function() {
        WtfGlobal.resetAjaxTimeOut();
        mainPanel.loadMask.hide();
        this.grid.getView().refresh();
    }, this);

    this.groupStore.on('beforeload', function() {
        WtfGlobal.setAjaxTimeOutFor30Minutes();
//        mainPanel.loadMask.show();
    }, this);



    this.grid = new Wtf.grid.HirarchicalGridPanel({
        autoScroll:true,
        store: this.groupStore,
        hirarchyColNumber:0,
        columns: [{
            header:'<b>'+WtfGlobal.getLocaleText("acc.report.2")+'</b>',
            dataIndex:'taxname',
            renderer:this.formatAccountName,
            width: 600
        },{
            header:"<div align=center><b>"+WtfGlobal.getLocaleText("acc.field.TransactionID")+"</b></div>",
            dataIndex:'transactionid',
            renderer:function(value,meta,rec){
                meta.attr = "Wtf:qtip='" + value + "' Wtf:qtitle='Transaction No' ";
                if(!value || rec.data.type == 'Tax Adjustment') return "<div style= 'margin:-2px 14px'>"+value+"</div>"; 
                value = WtfGlobal.linkRenderer(value,meta,rec);
                return value;
            }
        },
//        {
//            header:"<div align=center><b>"+WtfGlobal.getLocaleText("acc.contractActivityPanel.Remark")+"</b></div>",
//            dataIndex:'remark'
//        },
        {
            header:"<div align=center><b>"+WtfGlobal.getLocaleText("acc.common.memo")+"</b></div>",
            dataIndex:'memo',
            align:'left',
            renderer: function (val) {
                if (val) {
                    val = "<span wtf:qtip='" + val + "'>" + val + "</span>"
                }
                return val;
            }
        },{
            header:"<div align=center><b>"+WtfGlobal.getLocaleText("acc.je.tabTitle")+"</b></div>",
            dataIndex:'journalentry',
            renderer:WtfGlobal.jERendererForGST
        },{
            header:"<div align=center><b>"+WtfGlobal.getLocaleText("acc.field.TransactionDate")+"</b></div>",
            dataIndex:'jedate',
            renderer : function(val,meta,rec){
                var returnValue=''
                if(val && val!=null && val!=undefined && val != ''){
                    var v = new Date(val);
                    returnValue = v.format( WtfGlobal.getDateFormat());
                }    
                return returnValue;
            }
        },{
            header:"<div align=center><b>"+WtfGlobal.getLocaleText("acc.userAdmin.name")+"</b></div>",
            dataIndex:'name',
            align:'left'
//            renderer:this.formatMoney
        },{
            header:"<div align=center><b>"+((Wtf.account.companyAccountPref.countryid=='137')?WtfGlobal.getLocaleText("acc.field.RM"):WtfGlobal.getLocaleText("acc.field.S$"))+"</b></div>",
            dataIndex:'taxamount',
            renderer:this.formatMoney
        },{
            header:"<div align=left><b>"+WtfGlobal.getLocaleText("acc.setupWizard.curEx")+"</b></div>",
            dataIndex:'transactionexchangerate' ,
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.SINGAPORE
        },{
            header:"<div align=left><b>"+WtfGlobal.getLocaleText("acc.gstreport.amtInTransactionCurrency")+"</b></div>",
            dataIndex:'originalamount',
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.SINGAPORE,
            renderer:this.formatMoneyWithTransactionCurrency
        },{
            header:"<div align=left><b>"+WtfGlobal.getLocaleText("acc.field.BOX")+"</b></div>",
            dataIndex:'box'
        //    renderer:this.formatBox
        }],
        border : false,
        loadMask : new Wtf.LoadMask(document.body, Wtf.apply(this.loadMask)),
        viewConfig: {
            forceFit:true,
            emptyText: '<div class="emptyGridText">' + WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText('acc.common.norec')  + ' <br>' + WtfGlobal.getLocaleText("acc.common.norec.click.fetchbtn"))+'</div>'
        }
    });

    this.grid.on("render", WtfGlobal.autoApplyHeaderQtip);

    this.grid.on('rowclick',this.onRowClickGrid, this);
    this.grid.on('cellclick',this.onCellClick, this);
    this.grid.on('render',function(){
        this.grid.getView().getRowClass=this.getRowClass.createDelegate(this,[this.grid],1);
        this.grid.getView().applyEmptyText();
        // this.expandCollapseGrid("Expand");
    },this);
    
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleidarray: Wtf.MultiEntityReportsModuleIdArray.split(','),
        advSearch: false,
        parentPanelSearch: this,
        ignoreDefaultFields: true,
        isAvoidRedundent: true,
        hideRememberSerch : true,
        isMultiEntity: (Wtf.Countryid == Wtf.Country.SINGAPORE) ? false : true  // flag to fetch only multi entity dimension in advance search.
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);

    // this.grid.on('load',function(){
    //     // this.expandCollapseGrid("Expand");
    // },this);

    // this.grid.getStore().on("load", function(){
    //     // for(var i=0; i< this.grid.getStore().data.length; i++){
    //     //     this.grid.expandRow(this.grid.getView().getRow(i));
    //     // }
    // }, this);    

    this.startDate=new Wtf.ExDateFieldQtip({
        name:'stdate',
        format:WtfGlobal.getOnlyDateFormat(),
        readOnly:false,
        value:this.getDates(true)
    });

    this.endDate=new Wtf.ExDateFieldQtip({
        name:'enddate',
        format:WtfGlobal.getOnlyDateFormat(),
        readOnly:false,
        value:this.getDates(false)
    });
//    this.startDate.on('change',function(field,newval,oldval){
//            if(field.getValue()!='' && this.endDate.getValue()!=''){
//                /*
//                 * Start date not allowed to be earlier than book begining date
//                 */
//               
//                if((Wtf.account.companyAccountPref.countryid=='137') && Wtf.account.companyAccountPref.bbfrom){
//                    var bbDate = Wtf.account.companyAccountPref.bbfrom;
//                    var monthDateStr = bbDate.format('M d');
//                    var openingDocDate = new Date(monthDateStr + ', ' + bbDate.getFullYear() + ' 12:00:00 AM');
//                    if(field.getValue().getTime()<openingDocDate.getTime()){
//                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.malaysianGST.fromDateCanNotOlder")], 2);
//                        field.setValue(oldval);                    
//                    }
//                }
//                if(field.getValue().getTime()>this.endDate.getValue().getTime()){
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
//                    field.setValue(oldval);                    
//                }
//            }
//        },this);
//        
//        this.endDate.on('change',function(field,newval,oldval){
//            if(field.getValue()!='' && this.startDate.getValue()!=''){
//                if(field.getValue().getTime()<this.startDate.getValue().getTime()){
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.ToDateshouldnotbelessthanFromDate")], 2);
//                    field.setValue(oldval);
//                }
//            }
//        },this);
    this.fetchButton = new Wtf.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),  //'Fetch',
        tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
        iconCls:'accountingbase fetch',
        scope:this,
        handler:this.loadStore
    });

    this.detailedView = new Wtf.Button({
        text:WtfGlobal.getLocaleText("acc.field.DetailedView"),
        tooltip: WtfGlobal.getLocaleText("acc.field.ViewGSTForm5indetailedview"),
        iconCls:'advanceSearchButton',
        hidden:true,
        scope:this,
        handler:this.viewDetailHandler
    });
   var menubtn=[];
    this.expCsvButton = new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToCSVTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToCSV")+"</span>",
        scope: this,
        hidden:false,
        handler:function(){
            this.exportWithTemplate()
        }
    });
    menubtn.push(this.expCsvButton);
     this.expxlsButton = new Wtf.Action({
        iconCls:'pwnd '+'exportcsv',
        text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToXLSTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"</span>",
        scope: this,
        hidden:false,
        handler:function(){
            this.exportWithTemplate("xls")
        }
    });
   menubtn.push(this.expxlsButton);
      this.exportButton=new Wtf.Button({
            scope:this,
            iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
            text:WtfGlobal.getLocaleText("acc.common.export"),
            tooltip :WtfGlobal.getLocaleText("acc.common.exportTT"),  //'Export report details',
            menu:menubtn
        });
    this.exportPdfButton = new Wtf.Button({
        id: 'exportGSTForm5PDF',
        text: WtfGlobal.getLocaleText("acc.common.exportToPDF"),
        tooltip: WtfGlobal.getLocaleText("acc.common.pdf.exportTT"), //'Export report details.',
        //text :"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.pdf.exportTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToPDF")+"</span>",
        iconCls:'pwnd exportpdf1',   
        hidden:false,
        scope:this,
        reporttype : 3,
        handler: (Wtf.account.companyAccountPref.countryid=='137')?this.exportGST3ReportHandler:this.exportPDFData
    // menu: {
    //     scope:this,
    //     items: [
    //         {
    //             text: 'Export GST Form 5',
    //             iconCls: 'pwnd exportpdf',
    //             reporttype:3,
    //             scope:this,
    //             handler: this.exportPDFData
    //         }
    //     ]
    // }
    });

    this.printButton = new Wtf.Button({
            iconCls:'pwnd printButtonIcon',
            text :WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
            tooltip :WtfGlobal.getLocaleText("acc.common.printTT"),  //'Print report details.',
            disabled :false,
            hidden :false,
            scope: this,
            handler:function(){
                this.exportWithTemplate("print")
            }
        });    

    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.Expand"),
        tooltip:WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls:'pwnd toggleButtonIcon',
        scope:this,
        handler: function(){
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    });
    
    this.AdvanceSearchBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN"), // "Advanced Search",
        scope: this,
        tooltip: WtfGlobal.getLocaleText("acc.editor.advanceSearchBTN.ttip"), // 'Search for multiple terms in multiple fields.',
        handler: this.configurAdvancedSearch,
        iconCls: "advanceSearchButton",
        hidden: !(Wtf.account.companyAccountPref.isMultiEntity || Wtf.Countryid == Wtf.Country.SINGAPORE)
    });

    this.tbarArr = [WtfGlobal.getLocaleText("acc.common.from"), this.startDate, "-", 
        WtfGlobal.getLocaleText("acc.common.to"), this.endDate, "-", 
        this.fetchButton,'-',this.exportButton,'-',this.exportPdfButton, '-', this.printButton,'-',this.detailedView,'-',this.expandCollpseButton,'-',this.AdvanceSearchBtn];

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
        id:this.id+'Form03DetailedView'+'DmrAndIRRTpl',
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
        this.westPanel = new Wtf.Panel({
            width:'90%',
            region:'center',
            layout:'fit',
            border:false,
            items:this.grid,
            tbar:this.tbarArr
        }),this.southPanel = new Wtf.Panel({
            region: "south",
            layout: "fit",
            width:'90%',
            border:false,
            autoHeight: true,
            hidden:(Wtf.account.companyAccountPref.countryid!=Wtf.Country.MALAYSIA),
            items:[this.DmrAndIRRTpl]
        })
        ]
    });

    Wtf.apply(this,{
        defaults:{border:false,bodyStyle:"background-color:white;"},
        saperate:true,
        statementType:"GSTForm5",
        items:this.wrapperPanel
    },config);        

    Wtf.account.GSTForm5DetailedView.superclass.constructor.call(this,config);

    this.addEvents({
        'journalentry':true
    });

   this.groupStore.on("beforeload", function(s, o) {
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        o.params.stdate = WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        o.params.enddate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        o.params.withoutinventory=Wtf.account.companyAccountPref.withoutinventory;
    }, this);
    /**
     *  Remove Auto-load SDP-11742
     */
//    this.groupStore.load({
//        params:{
//            start:0,
//            limit:30
//        }
//    });
}

Wtf.extend(Wtf.account.GSTForm5DetailedView, Wtf.Panel, {
    onRowClickGrid:function(g,i,e){
        e.stopEvent();
    },        

    onCellClick:function(g,i,j,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var dataindex=g.getColumnModel().getDataIndex(j);
        var formrec = this.grid.getStore().getAt(i);
        if(dataindex == "transactionid"){
            var type=formrec.data['type'];
            var withoutinventory=formrec.data['withoutinventory'];  
            var billid=formrec.data['billid'];
            if (type == "Debit Note" || type == "Credit Note") {
                billid = formrec.data['noteid'];
            }
            if(type == 'Journal Entry'){
                this.callJEReportAndExpandJE(formrec,e);               
            } else {
                viewTransactionTemplate1(type, formrec,withoutinventory,billid);            
            }             
        }else if(dataindex=='journalentry'){
            this.callJEReportAndExpandJE(formrec,e);
        }
    },
    
    getRowClass:function(record,grid){
        var colorCss="";
        switch(record.data["fmt"]){
            case "T":colorCss=" grey-background";break;
            case "B":colorCss=" red-background";break;
            case "H":colorCss=" header-background";break;
            case "A":colorCss=" darkyellow-background";break;
        }
        return grid.getRowClass()+colorCss;
    },    
     expandCollapseGrid : function(btntext){
        if(btntext == WtfGlobal.getLocaleText("acc.field.Collapse")){
            for(var i=0; i< this.grid.getStore().data.length; i++){
                var rec=this.grid.getStore().data.items[i].data;
                if(rec.level >= 1){
                    this.grid.collapseRow(this.grid.getView().getRow(i));
                }
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if(btntext == WtfGlobal.getLocaleText("acc.field.Expand")){
            for(var i=0; i< this.grid.getStore().data.length; i++){
                this.grid.expandRow(this.grid.getView().getRow(i));
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    },
    formatAccountName:function(val,m,rec,i,j,s){
        var fmtVal=val;
        if(rec.data['fmt']=="B"){
            fmtVal='<span style="font-weight:bold;margin-left:0px;padding-left:20px;" unselectable="on"><font size=2px>'+fmtVal+'</font></span>';
        
        }else if(rec.data['fmt']=="radio"){
            fmtVal = '<span style="margin-left:0px;padding-left:40px;" unselectable="on"><font size=2px>' + 
                        '<input type="radio" name="declaration' + m['value']+ '" value="Yes">  Yes   <input type="radio" name="declaration' + m['value']+ '" value="No">   No'
                      '</span>';        

        }else if(rec.data['fmt']=="textbox"){
            fmtVal = '<span style="margin-left:0px;padding-left:40px;" unselectable="on"><font size=2px>' + fmtVal +
            '<input type="text" length=50  id="'+rec.json.id+'">'
            '</span>';   
            
        } else if(rec.data["level"]==0&&rec.data["taxname"]!="") {
            fmtVal='<span style="margin-left:0px;padding-left:40px" unselectable="on"><font size=2px>'+fmtVal+'</span>';
        
        } else if((rec.data["level"]==1 || rec.data["level"]==2 || rec.data["level"]==3)&&rec.data["taxname"]!="") {
            fmtVal='<span style="margin-right:0px;padding-left:60px" unselectable="on"><font size=2px>'+fmtVal+'</font></span>';
        }
        return fmtVal;
    },    

    formatBox:function(val,m,rec,i,j,s){
        var fmtVal=val;
        fmtVal='<span style="padding-left:40px">'+fmtVal+'</span>';
        return fmtVal;
    },  

    formatMoney:function(val,m,rec,i,j,s){
//        var fmtVal=WtfGlobal.currencyRenderer(val);
        var fmtVal=WtfGlobal.withoutRateCurrencySymbolForGSTFM5(val,m,rec);
        return fmtVal;
    },
    formatMoneyWithTransactionCurrency:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.withoutRateCurrencySymbolForGSTFM5withTransactionCurrency(val,m,rec);
        return fmtVal;
    },
    loadStore: function() {
        if(this.startDate.getValue()!='' && this.endDate.getValue()!=''){
                /*
                 * Start date not allowed to be earlier than book begining date
                 */
               
                if((Wtf.account.companyAccountPref.countryid=='137') && Wtf.account.companyAccountPref.bbfrom){
                    var bbDate = Wtf.account.companyAccountPref.bbfrom;
                    var monthDateStr = bbDate.format('M d');
                    var openingDocDate = new Date(monthDateStr + ', ' + bbDate.getFullYear() + ' 12:00:00 AM');
                    if(this.startDate.getValue().getTime()<openingDocDate.getTime()){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.malaysianGST.fromDateCanNotOlder")], 2);
                       return;
                    }
                }
                if(this.startDate.getValue().getTime()>this.endDate.getValue().getTime()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                    return;
                }    
            } 
        this.groupStore.load({
            params: {
                start: 0,
                limit: 30
            }
        });
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.MALAYSIA){
            this.checkForProperDateFilterForDmR();
        }
    },

    viewDetailHandler: function() {
        GSTForm5DetailedView();
    },

    handleResetClick: function() {
        if (this.quickPanelSearch.getValue()) {
            this.quickPanelSearch.reset();
            this.loadStore();
        }
    }, 

    storeloaded: function(store) {
        this.quickPanelSearch.StorageChanged(store);
        if (store.getCount() == 0) {
            if (this.exportButton)
                this.exportButton.disable();
            if (this.printButton)
                this.printButton.disable();
        } else {
            if (this.exportButton)
                this.exportButton.enable();
            if (this.printButton)
                this.printButton.enable();
        }
    },

    getDates: function(start) {
        if (this.startDateParams != undefined && this.startDateParams != "" && start) {
            return this.startDateParams; //return start date when detailed view called from form5 detailed view button 
        }
        if (this.endDateParams != undefined && this.endDateParams != "" && !start) {
            return this.endDateParams; //return end date when detailed view called from form5 detailed view button 
        }
        var d = new Date();
        var monthDateStr = d.format('M d');
        if (Wtf.account.companyAccountPref.fyfrom) {
            monthDateStr = Wtf.account.companyAccountPref.fyfrom.format('M d');
        }
        var fd = new Date(monthDateStr + ', ' + d.getFullYear() + ' 12:00:00 AM');
        if (d < fd) {
            fd = new Date(monthDateStr + ', ' + (d.getFullYear() - 1) + ' 12:00:00 AM');
        }
        if (start) {
            return fd;
        }
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },

    exportWithTemplate:function(type){
        type = type?type:"csv";
        
        var tapReturnDetailedViewDate =this.endDate.getValue()!=undefined?this.endDate.getValue().format('Ymd'):'';
        var tapReturnExportFileName = (type == "print") ? WtfGlobal.getLocaleText("acc.field.GSTTapReturnDetailedView") : WtfGlobal.getLocaleText("acc.field.GSTTapReturnDetailedView") + ' ' + tapReturnDetailedViewDate; 
        var nameDe=document.getElementById('0') !=null? document.getElementById('0').value :"";  //Name of declarant
        var id= document.getElementById('1') !=null? document.getElementById('1').value:"";  //designation id
        var desg=document.getElementById('2') !=null? document.getElementById('2').value:""; ; //Desgination
        var per=document.getElementById('3') !=null? document.getElementById('3').value:""; ; //contact person
        var contact=document.getElementById('4') !=null? document.getElementById('4').value:""; ;//contact no
        var header = "taxname,transactionid,memo,journalentry,jedate,name,taxamount";
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.SINGAPORE){
            header+=",transactionexchangerate,originalamount"
        }
        header+=",box"
        var Currency = "SG Dollar(SGD)";
        var box = "BOX"
        if(Wtf.account.companyAccountPref.countryid=='137'){
            Currency = "Malaysian Ringgit (MYR)";
        }
        var title = WtfGlobal.getLocaleText("acc.report.2") +","+ WtfGlobal.getLocaleText("acc.field.TransactionID") +","+WtfGlobal.getLocaleText("acc.common.memo")+","+ WtfGlobal.getLocaleText("acc.je.tabTitle") +","+WtfGlobal.getLocaleText("acc.field.TransactionDate")+", "+ WtfGlobal.getLocaleText("acc.userAdmin.name")+","+Currency;            
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.SINGAPORE){
            title+=","+WtfGlobal.getLocaleText("acc.setupWizard.curEx")+","+WtfGlobal.getLocaleText("acc.gstreport.amtInTransactionCurrency")
        }
        title+=","+box;
        var exportUrl = (Wtf.account.companyAccountPref.countryid=='137')?"ACCReports/getMalaysianGSTForm5Export.do":"ACCReports/getGSTForm5Export.do";
        var fileName = (Wtf.account.companyAccountPref.countryid == '137') ? tapReturnExportFileName : WtfGlobal.getLocaleText("acc.taxReport.GSTForm5DetailReport");
        var reportName = (Wtf.account.companyAccountPref.countryid=='137')?WtfGlobal.getLocaleText("acc.taxReport.GSTForm3DetailReport"):WtfGlobal.getLocaleText("acc.taxReport.GSTForm5DetailReport");
        var align = "none,none,none,none,none,none,none";
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.SINGAPORE){
            align+=",none,rowcurrencyGstForm";
        }
        align+=",none";
        var url = exportUrl+"?filename="+encodeURIComponent(fileName)+"&filetype="+type
                    +"&stdate="+WtfGlobal.convertToGenericStartDate(this.startDate.getValue())+"&enddate="+WtfGlobal.convertToGenericEndDate(this.endDate.getValue())
                    +"&nondeleted="+true+"&header="+header+
                    "&title="+encodeURIComponent(title)+
                    "&name="+encodeURIComponent(reportName)+"&width=150&get=27&align="+align+"&singleGrid="+true+"&gstF5DetailReport="+true+"&nameDe="+encodeURIComponent(nameDe)+"&id="+encodeURIComponent(id)+"&desg="+encodeURIComponent(desg)+"&per="+encodeURIComponent(per)+"&contact="+encodeURIComponent(contact);
        
        if (this.searchJson != "" && this.searchJson != undefined) {
            url += "&searchJson=" + this.searchJson + "&filterConjuctionCriteria=" + this.filterConjuctionCrit + "&multiEntityId=" + this.objsearchComponent.advGrid.getSearchJSON().searchText;
        }

        if(type == "print") {
           url+="&generatedOnTime="+WtfGlobal.getGeneratedOnTimestamp();
            window.open(url, "mywindow","menubar=1,resizable=1,scrollbars=1");
        } else {
            Wtf.get('downloadframe').dom.src  = url;
        }
        Wtf.get('downloadframe').dom.src = url;
    },    
    
    exportPDFData: function(type) {
        var nameDe=document.getElementById('0') !=null? document.getElementById('0').value :"";  //Name of declarant
        var id= document.getElementById('1') !=null? document.getElementById('1').value:"";  //designation id
        var desg=document.getElementById('2') !=null? document.getElementById('2').value:""; ; //Desgination
        var per=document.getElementById('3') !=null? document.getElementById('3').value:""; ; //contact person
        var contact=document.getElementById('4') !=null? document.getElementById('4').value:""; ;//contact no
        var url = "ACCReports/exportGSTReport.do?stdate=" + WtfGlobal.convertToGenericStartDate(this.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericEndDate(this.endDate.getValue()) + "&reportType=" + type.reporttype + "&withoutinventory=" + Wtf.account.companyAccountPref.withoutinventory +"&nameDe="+encodeURIComponent(nameDe)+"&id="+encodeURIComponent(id)+"&desg="+encodeURIComponent(desg)+"&per="+encodeURIComponent(per)+"&contact="+encodeURIComponent(contact);
        if (this.searchJson != "" && this.searchJson != undefined) {
            url += "&searchJson=" + this.searchJson + "&filterConjuctionCriteria=" + this.filterConjuctionCrit + "&multiEntityId=" + this.objsearchComponent.advGrid.getSearchJSON().searchText;
        }
        Wtf.get('downloadframe').dom.src = url;
    },
    exportGST3ReportHandler:function(){
        var url = "ACCReports/getMalasianGSTForm5DetailedViewExport.do?stdate="+WtfGlobal.convertToGenericStartDate(this.startDate.getValue())+"&enddate="+WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        if (this.searchJson != "" && this.searchJson != undefined) {
            url += "&searchJson=" + this.searchJson + "&filterConjuctionCriteria=" + this.filterConjuctionCrit + "&multiEntityId=" + this.objsearchComponent.advGrid.getSearchJSON().searchText;
        }
        Wtf.get('downloadframe').dom.src = url;
    },
    callJEReportAndExpandJE:function(formrec,e){
        var jeid=formrec.data['jeid']; 
        var jestartdate=this.startDate.getValue();
        var jeentrydate=this.endDate.getValue();
        if(e.target.getAttribute('jedate')!=undefined && e.target.getAttribute('jedate')!="") {  // multiple links in single row
            jeentrydate= new Date(e.target.getAttribute('jedate'));
            jestartdate= new Date(e.target.getAttribute('jedate'));
            jestartdate = new Date(jestartdate.setDate(jeentrydate.getDate()-1));
            jeentrydate = new Date(jeentrydate.setDate(jeentrydate.getDate()+1));
        }
        this.fireEvent('journalentry',jeid,true,this.consolidateFlag,null,null,null,jestartdate, jeentrydate);
    },
    checkForProperDateFilterForDmR : function(){
        var startDate = this.startDate.getValue();
        var firstDate = startDate.getFirstDateOfMonth();
        if(startDate.setHours(0, 0, 0, 0) != firstDate.setHours(0, 0, 0, 0)){
            this.southPanel.hide();  // If start date is not 1st date of that month
        } else {
            var endDate = this.endDate.getValue();
            var lastDate = endDate.getLastDateOfMonth(); 
            if(endDate.setHours(0, 0, 0, 0) != lastDate.setHours(0, 0, 0, 0)){
                this.southPanel.hide();    // If end date is not last date of month
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
        this.groupStore.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            filterConjuctionCriteria: filterConjuctionCriteria
        }
        this.groupStore.load({
            params: {
                start: 0,
                limit: 30
            }
        });
    },
    clearStoreFilter: function () {
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        this.groupStore.baseParams = {
            flag: 1,
            iscustomcolumndata: 0,
            searchJson: this.searchJson,
            filterConjuctionCriteria: this.filterConjuctionCrit
        }
        this.groupStore.load({
            params: {
                start: 0,
                limit: 30
            }
        });
        this.objsearchComponent.hide();
        this.AdvanceSearchBtn.enable();
        this.doLayout();
    },
   /*
    * update detailed view grid data only when detailed view component already initialized
    */
    updateData: function (GSTForm5DetailedViewPanel, startdate, enddate) {
        if (startdate != undefined && startdate != "") {
            GSTForm5DetailedViewPanel.startDate.setValue(startdate);
        }
        if (enddate != undefined && enddate != "") {
            GSTForm5DetailedViewPanel.endDate.setValue(enddate);
        }
         if(this.startDate.getValue()!='' && this.endDate.getValue()!=''){
                /*
                 * Start date not allowed to be earlier than book begining date
                 */
               
                if((Wtf.account.companyAccountPref.countryid=='137') && Wtf.account.companyAccountPref.bbfrom){
                    var bbDate = Wtf.account.companyAccountPref.bbfrom;
                    var monthDateStr = bbDate.format('M d');
                    var openingDocDate = new Date(monthDateStr + ', ' + bbDate.getFullYear() + ' 12:00:00 AM');
                    if(this.startDate.getValue().getTime()<openingDocDate.getTime()){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.malaysianGST.fromDateCanNotOlder")], 2);
                       return;
                    }
                }
                if(this.startDate.getValue().getTime()>this.endDate.getValue().getTime()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
                    return;
                }    
            } 
        this.groupStore.load({
            params: {
                start: 0,
                limit: 30
            }
        });
    }
});