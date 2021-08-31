Wtf.ReportsListGrid = function(config){
    Wtf.apply(this,config);
            var comboReader = new Wtf.data.Record.create([
                {
                    name: 'searchid',
                    type: 'string'
                },
                {
                    name: 'searchname',
                    type: 'string'
                },
                {
                    name: 'description',
                    type: 'string'
                },
                {
                    name: 'searchstate',
                    type: 'string'
                },
                {
                    name: 'module',
                    type: 'string'
                },
                {
                    name: 'status',
                    type: 'string'
                },
                {
                    name: 'filterAppend',
                    type: 'string'
                },
                {
                    name: 'templateid',
                    type: 'string'
                },
                {
                    name: 'isCustomLayout'
                },
                {
                    name: 'templatetitle'
                }
                ]);
   
    
        this.repStore = new Wtf.data.Store({
            url :'AdvanceSearch/getSavedSearchQueries.do',   //ERP-13659 [SJ]
            root: 'data',           
            remoteSort:true,
           reader: new Wtf.data.KwlJsonReader({
                        root: 'data',
                         totalProperty:"count" //ERP-13659 [SJ]
                    },comboReader)
        });
        this.pagingToolbar = new Wtf.PagingSearchToolbar({ //ERP-13659 [SJ]
        pageSize: 30,
        id: "pagingtoolbar" + this.id,
        store: this.repStore,
        searchField: this.quickPanelSearch,
        displayInfo: true,
        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
        plugins: this.pP = new Wtf.common.pPageSize({
            id : "pPageSize_"+this.id
            })
      });
      this.custRowNo=new Wtf.KWLRowNumberer(); //ERP-13659 [SJ]
        this.cmodel1 = new Wtf.grid.ColumnModel([
            this.custRowNo,
            {
                header: WtfGlobal.getLocaleText("acc.field.ReportName"),
                width: 130,
                dataIndex: 'searchname'
            },{
                header : WtfGlobal.getLocaleText("acc.common.view"),
                dataIndex: 'status',
                width:18,
                renderer:function(value, css, record, row, column, store){
                    return "<img id='AcceptImg' class='add'  style='height:18px; width:18px;' src='images/report.gif' title='View Report '></img>";
                }
            },{
            header : WtfGlobal.getLocaleText("acc.product.gridAction"),
            dataIndex: '',
            width:18,
            renderer: this.deleteRenderer.createDelegate(this)           
            }]);
        Wtf.ReportsListGrid.superclass.constructor.call(this, {
            store: this.repStore,
            cm :this.cmodel1,
            bbar:this.pagingToolbar,  //ERP-13659 [SJ]
            viewConfig: {
                    forceFit: true
            },
            title:Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.field.CustomReportList"), Wtf.TAB_TITLE_LENGTH)
});
};

Wtf.extend(Wtf.ReportsListGrid, Wtf.grid.GridPanel, {   
    deleteRenderer:function(v,m,rec){
        return "<div class='"+getButtonIconCls(Wtf.etype.deletegridrow)+"'></div>";
    },
    
     onRender: function(config) {
        Wtf.ReportsListGrid.superclass.onRender.call(this, config);
        this.repStore.load({    //ERP-13659 [SJ]
            params:{
            start:0,
            limit:this.pP.combo?this.pP.combo.getValue():30
            }
        });
        this.on("cellclick", this.CreateReportTabs, this);
        this.on('rowclick',this.handleRowClick,this);
//        this.on("rowdblclick", this.OpenReportTab, this);
     },
     
    handleRowClick:function(grid,rowindex,e){
    if (this.getSelectionModel().hasSelection()) {
        var id = this.getSelectionModel().getSelected().get("searchid");
        if(e.getTarget(".delete-gridrow")){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.AreyousureyouwanttodeleteselectedcustomReport?"),function(btn){
                if(btn=="yes") {
                        Wtf.Ajax.requestEx({
                        url:"AdvanceSearch/deleteSavedSearchQuery.do",
                            params: {
                            searchid : id,
                            deletemode : true                     
                            }
                    },this,this.genSuccessResponse,this.genFailureResponse);

                    }
                }, this);
            }
        }
    },
    
    genSuccessResponse:function(response){
        if(response.success){
            this.repStore.load({
                params: {
                    start: 0,
                    limit: this.pP.combo ? this.pP.combo.getValue() : 30
                }
            });
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Success"), WtfGlobal.getLocaleText("acc.field.CustomReporthasbeendeletedsuccessfully.")],3);
        }
    },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    },   
    
    OpenReportTab : function(e){
            var id = this.getSelectionModel().getSelected().get("id");
            openReportTab(id, "reportTabPaneltabreport");
    },
    CreateReportTabs: function(obj,row,col,e) {
        var event = e;
        var rowData = this.repStore.getAt(row).data;
        var moduleId=rowData.module;
        var searchstr=rowData.searchstate;
        var filterAppend=rowData.filterAppend;
        if(event.getTarget("img[class='add']")) {
            openAdvanceSearchTab(moduleId,searchstr,filterAppend,rowData);
        }
    }
});

//Custom layouts list component
Wtf.customLayoutList = function(config){
    Wtf.apply(this,config);
    var comboReader = new Wtf.data.Record.create([
    {
        name: 'id',
        type: 'string'
    },
    {
        name: 'name',
        type: 'string'
    },
    {
        name: 'status',
        type:'int'
    },{
        name: 'templatetitle',
        type: 'string'
    },{
        name: 'templateheadings',
        type: 'string'
    },
    {
        name: 'templatetype',
        type:'int'
    },{
        name:'countryname',
        type:'string'
    },{
        name:'countryid',
        type:'string'
    },{
        name:'isdefault',
        type:'boolean'
    }
    ]);
   
    
    this.repStore = new Wtf.data.Store({
        url :'ACCAccount/getPnLTemplates.do',
        root: 'data',
        remoteSort:true,
        baseParams:{
            templatetype:(this.filterTemplate!=undefined)?this.filterTemplate:"",
            isAdminsubdomain: Wtf.companyAccountPref_isAdminSubdomain
        },
        reader: new Wtf.data.KwlJsonReader({
            root: 'data',
            totalProperty:'count'
        },comboReader)
    });
    var columnArr=[];
    columnArr.push(new Wtf.KWLRowNumberer(),
        {
            header: WtfGlobal.getLocaleText("acc.field.CustomTemplateName"),
            width: 150,
            dataIndex: 'name',
            sortable: true
        },{
            header: WtfGlobal.getLocaleText("acc.field.CustomTemplateTitle"),
            width: 150,
            dataIndex: 'templatetitle',
            sortable: true
        },{
            header: WtfGlobal.getLocaleText("acc.field.CustomTemplateHeadings"),
            width: 250,
            hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            dataIndex: 'templateheadings',
            sortable: true
        },{
            header: WtfGlobal.getLocaleText("acc.field.TemplateType"),
            width: 150,
            dataIndex: 'templatetype',
            sortable: true,
            renderer:function(value, css, record, row, column, store){
                if(value == Wtf.templateType.pnl) {
                    return "Profit & Loss";
                } if(value == Wtf.templateType.balanceSheet) {
                    return "Balance Sheet";
                }else if(value == Wtf.templateType.cashFlow) {
                    return "Cash Flow Statement";
                } else {
                    return "Trial Balance";
                }
            }
        });
        
    columnArr.push({
        header : WtfGlobal.getLocaleText("acc.designerTemplate.isDefault"),
        dataIndex : 'isdefault',
        width : 30,
        align : "center",
        sortable : true,
        renderer : function(value, css, record, row, column, store){
            var val = "No";
            if(value == true){
                val = "Yes";
            }
            return val;
        }
    });
        
        if(Wtf.companyAccountPref_isAdminSubdomain){
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.field.header.countryname"),
            dataIndex: 'countryname',
            width: 150,
            sortable: true
        });   
        }
        columnArr.push({
            header : WtfGlobal.getLocaleText("acc.common.edit"),
            dataIndex: '',
            width:18,
            sortable: true,
            renderer:function(value, css, record, row, column, store){
                return "<div class='edit pwnd edit-gridrow'  title="+WtfGlobal.getLocaleText("acc.common.report.edit")+"></div>";
            }
        });        
    if (!Wtf.companyAccountPref_isAdminSubdomain) {
        columnArr.push({
            header: WtfGlobal.getLocaleText("acc.common.view"),
            dataIndex: '',
            width: 18,
            sortable: true,
            renderer: function(value, css, record, row, column, store) {
                return "<div class='view pwnd view-gridrow'  title="+WtfGlobal.getLocaleText("acc.common.report.View")+"></div>";
            }
        });
    }
        columnArr.push({
            header : WtfGlobal.getLocaleText("acc.rem.7"),
            dataIndex: '',
            width:18,
            //align : 'center',
            sortable: true,
            renderer:function(value, css, record, row, column, store){
                return "<div class='delete pwnd delete-gridrow'  title="+WtfGlobal.getLocaleText("acc.common.report.Delete")+"></div>";
            }
        });
    this.cmodel1 = new Wtf.grid.ColumnModel(columnArr);
        /*,{
            header : "Mapping Status",
            dataIndex: 'status',
            width:18,
//            hidden : true,
            sortable: true,
            renderer:function(value, css, record, row, column, store){
                if(value == 0) {
                    return "<span style='color:green'>Complete</span>";
                } else {
                    return "<span style='color:red'>In Progress</span>";
                }
                
            }
        }*/
    
    this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 15,
            id: "pagingtoolbar" + this.id,
            store: this.repStore,
            //searchField: this.quickPanelSearch,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),  //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({id : "pPageSize_"+this.id})
        });
    this.copyTemplateBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.field.CopyTemplate"), 
        tooltip: WtfGlobal.getLocaleText("acc.field.CopyTemplate"),
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.copy),
        disabled: true,
        handler:function(){
            var selRec = this.gridGroupSM.getSelected();
            var window = new Wtf.account.MappingComp({
                closable: true,
                isCopy:true,
                selectedRec:selRec,
                filterTemplate: this.filterTemplate
            });
            window.on("copiedtemplate", this.loadStore, this);
            window.show();
        }
    });
    
    this.editTemplateBtn = new Wtf.Toolbar.Button({
        text: "Edit Template", 
        tooltip: "Edit Template",
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.edit),
        disabled: true,
        handler:function(){
            var selRec = this.gridGroupSM.getSelected();
            var window = new Wtf.account.MappingComp({
                closable: true,
                title : WtfGlobal.getLocaleText("acc.field.EditCustomLayout"),
                isEdit:true,
//                mode : "edit",
                selectedRec:selRec,
                filterTemplate: this.filterTemplate
            });
            window.on("mappingsaved", this.loadStore, this);
            window.show();
        }
    });
    this.syncBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.custom.layout.synclayout"), 
        tooltip: WtfGlobal.getLocaleText("acc.custom.layout.synclayout"),
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.sync),
        hidden: !Wtf.companyAccountPref_isAdminSubdomain,
        disabled: true,
        handler:function(){
            var selRec = this.gridGroupSM.getSelected();
            var SyncDefaultLayoutWin = new Wtf.account.SyncDefaultLayoutComp({
                id:this.id+'SyncDefaultLayoutComp',
                countryid:selRec.data.countryid,
                synctemplateid:selRec.data.id,
                closable: true
            });
            SyncDefaultLayoutWin.show();
        }
    });
    this.gridGroupSM = new Wtf.grid.RowSelectionModel({
        singleSelect: true
    });
    this.gridGroupSM.on("selectionchange",function(){
        if (this.gridGroupSM.getCount() == 1) {
            this.editTemplateBtn.setDisabled(false);
            this.copyTemplateBtn.setDisabled(false);
            this.syncBtn.setDisabled(false);
        } else {
            this.editTemplateBtn.setDisabled(true);
            this.copyTemplateBtn.setDisabled(true);
            this.syncBtn.setDisabled(true);
        }
    },this);
    Wtf.customLayoutList.superclass.constructor.call(this, {
        store: this.repStore,
        cm :this.cmodel1,
        sm: this.gridGroupSM,
        viewConfig: {
            forceFit: true
        },
        loadMask : true,
        title: this.filterTemplate== undefined ? WtfGlobal.getLocaleText("acc.field.CustomLayoutList") :this.filterTemplate==0 ? WtfGlobal.getLocaleText("acc.field.Profit&LossLayout") : (this.filterTemplate==3?WtfGlobal.getLocaleText("acc.field.CashFlowStatementLayout"):WtfGlobal.getLocaleText("acc.field.BalanceSheetLayout")) ,
        tabTip:this.filterTemplate== undefined ? WtfGlobal.getLocaleText("acc.field.CustomLayoutList") :this.filterTemplate==0 ? WtfGlobal.getLocaleText("acc.field.Profit&LossLayout") : (this.filterTemplate==3?WtfGlobal.getLocaleText("acc.field.CashFlowStatementLayout"):WtfGlobal.getLocaleText("acc.field.BalanceSheetLayout")),
        tbar : [{
        xtype:'button',
        text:WtfGlobal.getLocaleText("acc.field.CreateNewTemplate"),
        tooltip:WtfGlobal.getLocaleText("acc.field.CreateNewTemplate"),
       // iconCls:'accountingbase fetch',
        iconCls: 'pwnd add',
        scope:this,
        handler:function(){
            var window = new Wtf.account.MappingComp({
                closable: true,
                filterTemplate:this.filterTemplate
            });
            
            window.on("mappingsaved", this.loadStore, this);
            window.show();
        }
            //this.fetchStatement
    },this.editTemplateBtn,this.copyTemplateBtn,this.syncBtn],
    bbar : this.pagingToolbar
    });
};

Wtf.extend(Wtf.customLayoutList, Wtf.grid.GridPanel, {
    onRender: function(config) {
        Wtf.customLayoutList.superclass.onRender.call(this, config);
        this.loadStore();
        this.on("cellclick", this.OpenReportTab, this);
    //        this.on("rowdblclick", this.OpenReportTab, this);
    },
    loadStore : function(){
        this.repStore.load({
            params : {
                start : 0,
                templatetype:(this.filterTemplate!=undefined)?this.filterTemplate:"",
                limit:this.pP.combo.value
            }
        });
        
        if(Wtf.companyAccountPref_isAdminSubdomain){
              chkcountryload();
        }
    },
    OpenReportTab : function(grid, rowindex, columnindex, event){
        var record = this.getSelectionModel().getSelected()
        var id = record.get("id");
        var templatetype = record.get("templatetype");
        var templatetitle = record.get("templatetitle");
        var templateheadings = record.get("templateheadings");
        var templateName = record.get("name");
        var countryid = record.get("countryid");
        if(event.getTarget("div[class='edit pwnd edit-gridrow']")) {
            var window = new Wtf.account.MappingCompGrouping({
                closable: true,
                templateid : id,                
                mode : 'Add',
                templateName : templateName,
                templatetype : templatetype,
                templatetitle : templatetitle,
                templateheadings : templateheadings,
                countryid:countryid
            });
            window.on("mappingsaved", this.loadStore, this);
            window.show();
        } else if(event.getTarget("div[class='view pwnd view-gridrow']")) {
                var panel = Wtf.getCmp("customLayout"+id);            
                var reportid = Wtf.autoNum.TrialBalance;
                if(panel==null){
                      if(templatetype == Wtf.templateType.trialBalance) {
                          panel = new Wtf.account.TrialBalanceCustomLayout({
                                id : "customLayout"+id,
                                consolidateFlag:false,
                                title:Wtf.util.Format.ellipsis(templatetitle!=''?templatetitle:templateName,Wtf.TAB_TITLE_LENGTH),
                                tabTip:templatetitle!=''?templatetitle:templateName,  //'Trading & Profit/Loss',
                                topTitle:'<center><font size=4>'+Wtf.util.Format.ellipsis(templatetitle!=''?templatetitle:templateName,Wtf.TAB_TITLE_LENGTH)+'</font></center>',                                
                                moduleid:101, //Added module id for Tading Profit and loss search report
                                reportid:reportid,
                                searchJson: "",
                                filterConjuctionCrit:"",
                                templateid : id,
                                templatetype : templatetype,
                                templatetitle:templatetitle!=''?templatetitle:templateName,
                                templateheadings:templateheadings,
                                statementType:'TrialBalance',
                                border : false,
                                closable: true,
                                layout: 'fit',
                                iconCls:'accountingbase financialreport'
                          });
                      } else {
                        var statementType = "";
                        if (templatetype == Wtf.templateType.pnl) {
                            statementType = "TradingAndProfitLoss";
                            reportid =Wtf.autoNum.TradingAndProfitLoss;
                        } else if (templatetype == Wtf.templateType.balanceSheet) {
                            statementType = "BalanceSheet";
                            reportid =Wtf.autoNum.BalanceSheetReportId;
                        } else if (templatetype == Wtf.templateType.cashFlow) {
                            statementType = "CashFlowStatement";
                            reportid =Wtf.autoNum.CashFlowStatement;
                        } else {
                            statementType = "TrialBalance";
                        }
                        panel = new Wtf.account.TradingCustomLayout({
                                id : "customLayout"+id,
                                consolidateFlag:false,
                                title:Wtf.util.Format.ellipsis(templatetitle!=''?templatetitle:templateName,Wtf.TAB_TITLE_LENGTH),
                                tabTip:templatetitle!=''?templatetitle:templateName,  //'Trading & Profit/Loss',
                                topTitle:'<center><font size=4>'+Wtf.util.Format.ellipsis(templatetitle!=''?templatetitle:templateName,Wtf.TAB_TITLE_LENGTH)+'</font></center>',                                
                                moduleid:101, //Added module id for Tading Profit and loss search report
                                reportid:reportid,
                                searchJson: "",
                                filterConjuctionCrit:"",
                                templateid : id,
                                templatetype : templatetype,
                                templatetitle:templatetitle!=''?templatetitle:templateName,
                                templateheadings : templateheadings,
                                statementType:statementType,
                                border : false,
                                closable: true,
                                layout: 'fit',
                                iconCls:'accountingbase financialreport'
                        });
                    }
                    Wtf.getCmp('as').add(panel);
                    panel.on('account',viewGroupDetailReport);
                }
                Wtf.getCmp('as').setActiveTab(panel);

                Wtf.getCmp('as').doLayout();
        } else if(event.getTarget("div[class='delete pwnd delete-gridrow']")) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttodeleteselectedcustomlayouttemplate"),function(btn){
                if(btn=="yes") {
                    Wtf.Ajax.requestEx({
                        url:"ACCAccountCMN/deleteCustomTemplate.do",
                        params: {
                            templateid : id,
                            deletemode : true 
                    
                        }
                    },this,this.genSuccessResponse,this.genFailureResponse);
                    
                }
            }, this);
        } 
    },
    
    genSuccessResponse:function(response){
        if(response.success){
            this.loadStore();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.field.Customlayouttemplatehasbeendeletedsuccessfully.")],3);
        }
        else
        {
            /**
             * To provide alert in case of any exception.
             */
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), response.msg], 2);
        }
    },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    }
});
