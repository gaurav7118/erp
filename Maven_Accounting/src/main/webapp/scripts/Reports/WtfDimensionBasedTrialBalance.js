


function callDimensionBasedTrialBalanceDynamicLoad(searchStr, filterAppend){
    var panel = Wtf.getCmp("DimensionBasedTrialBalance");
    if(panel==null){
        panel = new Wtf.account.WtfDimensionBasedTrialBalance({
            id : 'DimensionBasedTrialBalance',
            title: WtfGlobal.getLocaleText("erp.DimensionBasedTrialBalance"),
            tabTip:WtfGlobal.getLocaleText("erp.DimensionBasedTrialBalance"),
            topTitle:'<center><font size=4>' + WtfGlobal.getLocaleText("erp.DimensionBasedTrialBalanceReport") + '</font></center>',
            statementType:'TrialBalance',
            border : false,
            closable: true,
            layout: 'fit',
            iconCls:'accountingbase financialreport',
            reportid: Wtf.autoNum.dimensionBasedTrialBalance
        });
        Wtf.getCmp('as').add(panel);
        panel.on('account',viewGroupDetailReport);
    }
    Wtf.getCmp('as').setActiveTab(panel);
    showAdvanceSearch(panel, searchStr, filterAppend);
    Wtf.getCmp('as').doLayout();
    panel.on("activate",function(panel){
        panel.westPanel.setWidth(panel.getInnerWidth()/2);
        panel.doLayout();
    });
}
//********************************************************************
Wtf.account.WtfDimensionBasedTrialBalance=function(config){
    this.total=[0,0];
    this.uPermType=Wtf.UPerm.fstatement;
    this.permType=Wtf.Perm.fstatement;
    this.summaryR = new Wtf.ux.grid.GridSummary();

    this.reader = new Wtf.data.KwlJsonReader2({
        totalProperty: 'totalcount',
        root: "data"
    });

    this.statementType=config.statementType;
    this.reportid=config.reportid;

    this.lStroe = new Wtf.data.Store({
        reader:this.reader,
        baseParams:{
            nondeleted:true
        },
        url: Wtf.req.account+'CompanyManager.jsp'
    });
    this.lStroe.load({
        nondeleted:true
    });
    
    this.rStroe = new Wtf.data.Store({
        reader:this.reader,
        baseParams:{
            nondeleted:true
        },
        url: Wtf.req.account+'CompanyManager.jsp'
    });
        
    this.rStroe.load({
        nondeleted:true
    })
    var columnArr = [];

    columnArr.push({
        header:'<b>'+WtfGlobal.getLocaleText("acc.balanceSheet.particulars")+'</b>',
        dataIndex:'accountname',
        renderer:this.formatAccountName,
        width:200,
        align:'left',
        hidden: false,
        pdfwidth: 200,
        summaryRenderer:function(){
            return WtfGlobal.summaryRenderer(WtfGlobal.getLocaleText("acc.common.total"));
        }.createDelegate(this)
    });

    this.lGrid = new Wtf.grid.HirarchicalGridPanel({
        autoScroll:true,
        store: this.lStroe,
        hirarchyColNumber:0,
        cm:new Wtf.grid.ColumnModel(columnArr),       
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:false,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))+"<br>"+WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.field.pleseselectdimension")),
            deferEmptyText: false
        }
    });
    this.lGrid.on("render", WtfGlobal.autoApplyHeaderQtip);
    this.lGrid.on('rowclick',this.onRowClickLGrid, this);
    
    this.lStroe.on('load',function(store,rec,option){
        if(rec.length==0){
            this.lGrid.getView().refresh(true);  
        }  
    })

    this.rGrid = new Wtf.grid.HirarchicalGridPanel({
        plugins:[this.summaryR],
        autoScroll:true,
        store: this.rStroe,
        hirarchyColNumber:0,
        columns: columnArr,      
        border : false,
        loadMask : true,
        viewConfig: {
            forceFit:false,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))+"<br>"+WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.field.pleseselectdimension")),
            deferEmptyText: false
        }
    });
    
    this.rStroe.on('load',function(store,rec,option){
        if(rec.length==0){
            this.rGrid.getView().refresh(true);  
        }  
    })
    this.rGrid.on("render", WtfGlobal.autoApplyHeaderQtip);
    this.lGrid.view.refresh.defer(1, this.lGrid.view); 
    this.rGrid.on('rowclick',this.onRowClickRGrid, this);

    this.lGrid.on('render',function(){
        this.lGrid.getView().getRowClass=this.getRowClass.createDelegate(this,[this.lGrid],1);
    },this);

    this.rGrid.on('render',function(){
        this.rGrid.getView().getRowClass=this.getRowClass.createDelegate(this,[this.rGrid],1);
    },this);

    this.lGrid.getStore().on("load", function(){
        if(this.expButton)this.expButton.enable();
        if(this.printData)this.printData.enable();
        
        var columns = [];
       Wtf.each(this.lGrid.getStore().reader.jsonData.columns, function(column){
            if(column.editor) {
                var editor  = eval ("(" + column.editor + ")");
                column.editor = editor;
            }
            if(column.renderer) {
                var renderer  = eval ("(" + column.renderer + ")");
                column.renderer = renderer;
            }else if(column.dataIndex!="acccode" && column.dataIndex!="aliascode" && column.dataIndex!="accountname" && column.dataIndex!="acctype"){
                column.renderer =  WtfGlobal.currencyRenderer;
            }
            columns.push(column);
        });
       
        this.lGrid.getColumnModel().setConfig(columns);
        this.lGrid.getColumnModel().on("configchanged", function(){
            alert("configchanged");
        },this);
        this.lGrid.getView().refresh();
        
        this.expandCollapseGrid("Collapse");

    }, this);

    this.rGrid.getStore().on("load", function(){   

        if(this.expButton)this.expButton.enable();
        if(this.printData)this.printData.enable();
        
        var columns = [];
        Wtf.each(this.rGrid.getStore().reader.jsonData.columns, function(column){
            if(column.editor) {
                var editor  = eval ("(" + column.editor + ")");
                column.editor = editor;
            }
            if(column.renderer) {
                var renderer  = eval ("(" + column.renderer + ")");
                column.renderer = renderer;
            }else if(column.dataIndex!="acccode" && column.dataIndex!="aliascode" && column.dataIndex!="accountname" && column.dataIndex!="acctype"){
                column.renderer =  WtfGlobal.currencyRenderer;
            }
            columns.push(column);
        });
       
        this.rGrid.getColumnModel().setConfig(columns);
        this.rGrid.getColumnModel().on("configchanged", function(){
            alert("configchanged");
        },this);
        this.rGrid.getView().refresh();

        this.expandCollapseGrid("Collapse");

    }, this);
    
    // to change the dates
    this.startDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  // 'From',
        name:'startdate',
        format:WtfGlobal.getOnlyDateFormat(),
        value:WtfGlobal.getDates(true)
    });
    
    this.endDate = new Wtf.ExDateFieldQtip({
        fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  // 'To',
        format:WtfGlobal.getOnlyDateFormat(),
        name:'enddate',
        value:WtfGlobal.getDates(false)
    });
    
    this.grid = this.rGrid;
    
    this.objsearchComponent = new Wtf.advancedSearchComponent({
        cm: this.grid.colModel,
        moduleid:101,
        dimensionBasedComparisionReport:true,
        advSearch: false,
        hideRememberSerch: false,
        reportid: this.reportid
    });
    this.objsearchComponent.advGrid.on("filterStore", this.filterStore, this);
    this.objsearchComponent.advGrid.on("clearStoreFilter", this.clearStoreFilter, this);
    
    this.AddDimensionBtn = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("erp.report.SelectDimensions"),
        scope: this,     
        tooltip: WtfGlobal.getLocaleText("erp.report.SelectDimensionsTT"),
        handler: this.configurAdvancedSearch,
        iconCls:getButtonIconCls(Wtf.etype.add)
    });
    
    chkCostCenterload();
    if(Wtf.CostCenterStore.getCount()==0) Wtf.CostCenterStore.on("load", this.setCostCenter, this);
    this.costCenter = new Wtf.form.ComboBox({
        store: Wtf.CostCenterStore,
        name:'costCenterId',
        width:140,
        displayField:'name',
        valueField:'id',
        triggerAction: 'all',
        mode: 'local',
        typeAhead:true,
        value:"",
        selectOnFocus:true,
        forceSelection: true,
        emptyText:WtfGlobal.getLocaleText("acc.rem.9")  //,"Select a Cost Center"
    });
    this.submitBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.fetch"),
        tooltip : WtfGlobal.getLocaleText("acc.invReport.fetchTT"), 
        id: 'submitRec' + this.id,
        scope: this,
        iconCls:'accountingbase fetch',
        disabled :false
    });
    this.submitBttn.on("click", this.fetchData, this);
    this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  
        id: 'btnRec' + this.id,
        scope: this,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);   
    var btnArr=[];

    btnArr.push(
        WtfGlobal.getLocaleText("acc.common.from"),
        this.startDate,
        WtfGlobal.getLocaleText("acc.common.to"),
        this.endDate, 
        WtfGlobal.getLocaleText("acc.common.costCenter"),
        this.costCenter,this.resetBttn,
        this.AddDimensionBtn,this.submitBttn
        );

    this.expandCollpseButton = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.Expand"),
        tooltip:WtfGlobal.getLocaleText("acc.field.Expand/CollapseRecords"),
        iconCls:'pwnd toggleButtonIcon',
        scope:this,
        handler: function(){
            this.expandCollapseGrid(this.expandCollpseButton.getText());
        }
    });

    btnArr.push('-', this.expandCollpseButton);
    
    this.expButton=new Wtf.exportButton({
        obj:this,
        id: "exportReports"+config.helpmodeid,
        text:WtfGlobal.getLocaleText("acc.common.export"),
        tooltip :WtfGlobal.getLocaleText("acc.agedPay.exportTT"),  //'Export report details',
        disabled :true,
        filename:WtfGlobal.getLocaleText("erp.DimensionBasedTrialBalance")+"_v1",
        params:{
            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            searchJson:this.searchJson,
            mode:65,
            nondeleted:true,
            costcenter : this.costCenter.getValue(),
            reportView : this.statementType
        },
        menuItem:{
            csv:true,
            pdf:false,
            rowPdf:false,
            xls:true
        },
        get: Wtf.autoNum.Dimension_Based_TrialBalance
    })
      
    btnArr.push('-', this.expButton);
    
    btnArr.push(this.printData=new Wtf.exportButton({
        obj:this,
        text:WtfGlobal.getLocaleText("acc.common.print"),  //"Print",
        tooltip:WtfGlobal.getLocaleText("acc.common.printTT"),  //"Print Report details",
        filename:WtfGlobal.getLocaleText("erp.DimensionBasedTrialBalance"),
        menuItem:{
            print:true
        },
        disabled :true,
        params:{
            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            searchJson:this.searchJson,
            mode:65,
            nondeleted:true,
            costcenter : this.costCenter.getValue(),
            reportView : this.statementType
        },
        get: Wtf.autoNum.Dimension_Based_TrialBalance
    }));
    

    this.wrapperPanel = new Wtf.Panel({
        border:false,
        layout:"border",
        scope:this,
        items:[this.objsearchComponent,{
            region: 'center',
            layout: 'fit',
            border: false,
            items:[this.westPanel = new Wtf.Panel({
            width:'100%',
            region:'center',
            layout:'fit',
            border:false,
            items:this.lGrid
        }),
        {
            layout:'fit',
            region:'west',
            width:'0%'
        },

        {
            layout:'fit',
            region:'east',
            width:'0%'
        }
        ],
        tbar:btnArr
        }]
    });

    Wtf.apply(this,{
        defaults:{
            border:false,
            bodyStyle:"background-color:white;"
        },
        saperate:true,
        statementType:"Trading",
        items:this.wrapperPanel
//        tbar:btnArr
    },config);

    Wtf.account.WtfDimensionBasedTrialBalance.superclass.constructor.call(this,config);
    this.addEvents({
        'account':true
    });
}

Wtf.extend( Wtf.account.WtfDimensionBasedTrialBalance,Wtf.Panel,{
    onRowClickRGrid:function(g,i,e){
        var el=e.getTarget("a");
        if(el==null)return;
        var accid=this.rGrid.getStore().getAt(i).data['accountid'];

    },
    
    collapseGrids : function() {
        for(var i=0; i< this.lGrid.getStore().data.length; i++){
            this.lGrid.collapseRow(this.lGrid.getView().getRow(i));
        }   
        for(var i=0; i< this.rGrid.getStore().data.length; i++){
            this.rGrid.collapseRow(this.rGrid.getView().getRow(i));
        }
    },
    
    onRowClickLGrid:function(g,i,e){
        e.stopEvent();
        var el=e.getTarget("a");
        if(el==null)return;
        var elementId = el.id;
        if(elementId == 'anc'){
            return;
        }
        var accid=this.lGrid.getStore().getAt(i).data['accountid'];   
//        var fromdate =WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
//        var todate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        var searchJson = (this.objsearchComponent.advGrid != undefined) ? this.objsearchComponent.advGrid.getJsonofStore() : "" ;
        this.fireEvent('account',searchJson,this.filterConjuctionCrit,accid,this.startDate.getValue(),this.endDate.getValue(),null, null,true);
    },

    openLedgerForCellDur:function(i){
        var lGridStore = this.lGrid.getStore();
        var rGridStore = this.rGrid.getStore();
        var accid=lGridStore.getAt(i).data['accountid'];              
        var dimArray = rGridStore.data.items[rGridStore.data.length-1].json["dimensions"]; 
        var dimName = dimArray[j-1]["dimname"];
        var fromdate =WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        var todate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        this.fireEvent('account',accid,fromdate,todate);
    },

    expandCollapseGrid : function(btntext){
        if(btntext == WtfGlobal.getLocaleText("acc.field.Collapse")){
            for(var i=0; i< this.lGrid.getStore().data.length; i++){
                this.lGrid.collapseRow(this.lGrid.getView().getRow(i));
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        } else if(btntext == WtfGlobal.getLocaleText("acc.field.Expand")){
            for(var i=0; i< this.lGrid.getStore().data.length; i++){
                this.lGrid.expandRow(this.lGrid.getView().getRow(i));
            }
            this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Collapse"));
        }
    },

    setCostCenter: function(){
        this.costCenter.setValue("");//Select Default Cost Center as None
        Wtf.CostCenterStore.un("load", this.setCostCenter, this);
    },

    resetFilterAndFetchReport: function(costCenterId, startDate, endDate){
        this.costCenter.setValue(costCenterId);        
        if (this.startDate > this.endDate){
            WtfComMsgBox(1,2);
            return;            
        }        
        this.fetchStatement();
    },

    getRowClass:function(record,grid){
        var colorCss="";
        switch(record.data["fmt"]){
            case "T":
                colorCss=" grey-background";
                break;
            case "B":
                colorCss=" red-background";
                break;
            case "H":
                colorCss=" header-background";
                break;
            case "A":
                colorCss=" darkyellow-background";
                break;
        }
        return grid.getRowClass()+colorCss;
    },

    formatData:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.currencyRenderer(val);
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="") {
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        }
        return fmtVal;
    },

    formatMoney:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.currencyRenderer(val);
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="")
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        return fmtVal;
    },

    fetchStatement:function(){
        
        if(this.startDate.getValue() > this.endDate.getValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.FromDateshouldnotbegreaterthanToDate")], 2);
            return;
        }
        this.expandCollpseButton.setText(WtfGlobal.getLocaleText("acc.field.Expand"));
        WtfComMsgBox(29,4,true); //Show loading mask
        var fromdate =WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
        var todate = WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
        var params={
            stdate:fromdate,
            enddate:todate
        };
        params.searchJson=this.searchJson;
        params.mode=65;
        params.nondeleted=true;
        params.costcenter = this.costCenter.getValue();
        params.reportView = this.statementType;
        
        this.ajxUrl = "ACCReports/getDimesionBasedTrialBalance.do";
        
        this.expButton.setParams({
            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            searchJson:this.searchJson,
            mode:65,
            nondeleted:true,
            costcenter : this.costCenter.getValue(),
            reportView : this.statementType
        });
        this.printData.setParams({
            stdate:WtfGlobal.convertToGenericStartDate(this.startDate.getValue()),
            enddate:WtfGlobal.convertToGenericEndDate(this.endDate.getValue()),
            searchJson:this.searchJson,
            mode:65,
            nondeleted:true,
            costcenter : this.costCenter.getValue(),
            reportView : this.statementType
        });
        
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url:this.ajxUrl,
            params:params
        }, this, this.successCallback, this.failureCallback);
    },

    successCallback:function(response){
        WtfGlobal.resetAjaxTimeOut();
        this.hideLoading();
        if(response.success){    
            this.lGrid.getStore().reader.read(response);
            this.rGrid.getStore().reader.read(response);
            
            var columns = [];
            Wtf.each(this.lGrid.getStore().reader.jsonData.columns, function(column){
                if(column.editor) {
                    var editor  = eval ("(" + column.editor + ")");
                    column.editor = editor;
                }
                if (column.dataIndex == "accountname") {
                    column.renderer = WtfGlobal.linkDeletedRenderer;
                } else if(column.renderer && column.header=="Account Code") {
                    var renderer  = function(v,m,rec){
                        if(rec.data.issummaryvalue){
                            return '<div style="height:30px;margin-top:4px;"><font size=5><b>'+v+'</b></font></div>';
                        }                    
                    };
                    column.renderer = renderer;
                }else if(column.dataIndex!="acccode" && column.dataIndex!="aliascode" && column.dataIndex!="accountname" && column.dataIndex!="acctype"){
                    column.renderer =  WtfGlobal.currencyRenderer;
                }
                columns.push(column);
            });
       
            this.lGrid.getColumnModel().setConfig(columns);
            this.lGrid.getColumnModel().on("configchanged", function(){
                alert("configchanged");
            },this);
        
            this.expandCollapseGrid("Collapse");

            var columns = [];
            Wtf.each(this.rGrid.getStore().reader.jsonData.columns, function(column){
                if(column.editor) {
                    var editor  = eval ("(" + column.editor + ")");
                    column.editor = editor;
                }
                if(column.renderer) {
                    var renderer  = eval ("(" + column.renderer + ")");
                    column.renderer = renderer;
                }
                columns.push(column);
            });
       
            this.rGrid.getColumnModel().setConfig(columns);
            this.rGrid.getColumnModel().on("configchanged", function(){
                alert("configchanged");
            },this);

            this.expandCollapseGrid("Collapse");
        
            this.lGrid.store.loadData(response.data);
            this.rGrid.store.loadData(response.data);
            this.doLayout();
            
            this.doLayout();
            //            if (this.firstTime) {
            this.lGrid.getView().refresh(true);
            this.rGrid.getView().refresh(true);
            //            }
            this.firstTime = false;
            this.collapseGrids();           
        }
    },

    failureCallback:function(response){
        WtfGlobal.resetAjaxTimeOut();
        this.hideLoading();
    },

    hideLoading:function(){
        Wtf.MessageBox.hide();
    },

    getDates:function(start){
        var d=Wtf.serverDate;
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
    
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.AddDimensionBtn.disable();
        this.doLayout();
        
    },
    
    filterStore: function(json, filterConjuctionCriteria) {
        this.searchJson = json;
        this.filterConjuctionCrit = filterConjuctionCriteria;
        this.fetchStatement();
    },
    fetchData: function() {
        if (this.objsearchComponent !=undefined  && this.objsearchComponent.advGrid!=undefined && this.objsearchComponent.advGrid.searchStore!=undefined && !(this.objsearchComponent.advGrid.searchStore.getCount() > 0)) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),  WtfGlobal.getLocaleText("acc.field.pleseselectdimension")], 3);
        }else{
            this.fetchStatement();
        }
    },
    clearStoreFilter: function() {
        this.fetchStatement();
        this.objsearchComponent.hide();
        this.AddDimensionBtn.enable();
        this.doLayout();
    },
    handleResetClick:function(){
        this.startDate.reset();
        this.endDate.reset(); 
        this.costCenter.reset();
        this.searchJson = "";
        this.filterConjuctionCrit = "";
        for(var i=0; i< this.lGrid.getColumnModel().getColumnCount()-1; i++){            
            this.lGrid.getColumnModel().setColumnHeader((i+1),"") ;            
        }
        for(var i=0; i<this.rGrid.getColumnModel().getColumnCount()-1; i++){            
            this.rGrid.getColumnModel().setColumnHeader((i+1),"") ;            
        }
        this.clearStoreFilter();
        this.lStroe.removeAll();
        this.rStroe.removeAll();
        this.clearStoreFilter();
    }
});
