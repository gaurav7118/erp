/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
function loadCustomWidgetReportList(id, check, isCash) {
    var reportList = Wtf.getCmp("customWidgetReportList");
    if (reportList == null) {
        reportList = new Wtf.CustomWidgetReportList({
            id: "customWidgetReportList",
            closable: true,
            title:"Custom Widget Reports",
            tabTip:"Widget Reports",
            iconCls : "accountingbase widget-report-list",
            modal: true
        });

        Wtf.getCmp('as').add(reportList);
    }
    Wtf.getCmp('as').setActiveTab(reportList);
    Wtf.getCmp('as').doLayout();

}

Wtf.CustomWidgetReportList = Wtf.extend(Wtf.Panel, {
    initComponent:function() {
        this.createCustomWidgetReportWin();
        this.createListGrid();

        Wtf.apply(this, {
            layout: 'fit',
            items :[this.reportList]
        }); 

        Wtf.CustomWidgetReportList.superclass.initComponent.apply(this, arguments);

    },
    createCustomWidgetReportWin : function(){
        this.CustomWidgetReportWin = new Wtf.CreateCustomWidgetReport({
            title:"Create New"
        });
        
        this.CustomWidgetReportWin.on("aftersave",function(win,res){
            this.reportList.getStore().reload();
        },this);
    },
    createListGrid : function(){
        var record = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'reportname'},
            {name: 'customreports'},
            {name: 'searchcriteria'},
            {name: 'filterappend'},
            {name: 'createdby'}
        ]);

        var jsonReader = new Wtf.data.JsonReader({
            root: "data",
            totalProperty: 'count'
        }, record);

        var liststore = new Wtf.data.Store({
            reader: jsonReader,
            url: "ACCCreateCustomReport/getCustomWidgetReports.do",
            baseParams: {
                companyid: companyid,
                ignorepivot : true
            }
        });
        liststore.load({
            start : 0,
            limit : 30
        });
        
        liststore.on("beforeload",function(){
            this.loadingMask.show();
        },this);
        
        liststore.on("load",function(){
            this.loadingMask.hide();
            //SDP-9472 Job to date profit report is blank
            //This is temporary solution, must have to fix this issue later.
            if (isProdBuild) {
                ScriptMgr.load({
                    scripts: ['../../scripts/Reports/SalesCommissionOnDimension.js'],
                    callback: function () {
                        Wtf.ReportScriptLoadedFlag.salesprofitReport = true
                    }
                });
            }
        },this);
        
        this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: liststore,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.agedPay.norec"),  //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            })
        });
        var tbarArray =[];
        
        this.newTabButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.create.title"),
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.add),
            handler : function(){
                this.CustomWidgetReportWin.show();
            }
        });
        tbarArray.push(this.newTabButton);
        
        this.deleteReportBtn = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.delete"),
            scope: this,
            disabled: true,
            iconCls :getButtonIconCls(Wtf.etype.deletebutton),
            handler : function(){
                Wtf.Msg.confirm(WtfGlobal.getLocaleText('acc.common.confirm'), WtfGlobal.getLocaleText("acc.reportbuilder.deletereportmsg")+"</br></br><b>"+WtfGlobal.getLocaleText('acc.customerList.delTT1')+"</b>", function(btn){
                    if(btn=="yes"){
                        this.deleteReports();
                    }
                }, this);
            }
        });
        
        tbarArray.push("-",this.deleteReportBtn);
        
        var selModel = new Wtf.grid.CheckboxSelectionModel({
            singleSelect:true
        });
    
        this.reportList = new Wtf.grid.GridPanel({
            layout:"fit",
            store : liststore,
            columns: [selModel,
            {
                header: "Report Name", 
                dataIndex: 'reportname'
            },

            {
                header: "Description", 
                dataIndex: 'description',
                hidden :true
            },

            {
                header: "Created By", 
                dataIndex: 'createdby'
            },

            {
                header: "Delete", 
                dataIndex: 'deleted',
                hidden :true
            },
            {
                header : WtfGlobal.getLocaleText("acc.wtfTrans.vvi"),
                dataIndex: 'status',
                width:30,
                renderer:function(){
                    return "<img id='AcceptImg' class='ViewR'  style='height:18px; width:18px;' src='images/report.gif' title="+WtfGlobal.getLocaleText("acc.field.ViewReport")+"></img>";
                }
            }
            ],
            viewConfig: {
                forceFit: true
            },
            sm: selModel,
            bbar: this.pagingToolbar,
            tbar:tbarArray
        });
        
        this.reportList.on("render",function(){
            this.loadingMask = new Wtf.LoadMask(this.reportList.body, {
                msg : WtfGlobal.getLocaleText("acc.msgbox.50")
            });
        },this);
        
        this.reportList.on("cellclick",this.viewReport, this);
        this.reportList.getSelectionModel().on("selectionchange", this.enableDisableButtons, this);
    },
    
    viewReport: function(obj,row,col,event) {
        if(event.getTarget("img[class='ViewR']")) {
            loadCustomWidgetReport(obj.getStore().getAt(row));
        }
    },
    
    enableDisableButtons: function(selModel, eOpts) {
        var selected = selModel.getSelections();
        if (selected.length > 0) {
            this.deleteReportBtn.enable();
        } else {
            this.deleteReportBtn.disable();
        }
    },
    
    deleteReports: function() {
        var count = 0;
        var reportIds = [];
        var reportListGrid = this.reportList;
        var selectedReports = reportListGrid.getSelectionModel().getSelections();
        for (count = 0; count < selectedReports.length; count++) {
            reportIds.push("'" + selectedReports[count].data.id + "'");
        }
//        reportIds = reportIds.substring(0, reportIds.length - 1);
        
        Wtf.Ajax.requestEx({
            url: 'ACCCreateCustomReport/deleteCustomWidgetReport.do',
            method:"POST",
            params: {
                reportIds: reportIds.join(",")
            }
        },this, function(res, req) {
            //                res = eval("(" + res.responseText + ")");
            if (res.success == true) {
                WtfComMsgBox(["Success",res.msg],0)
                reportListGrid.getStore().reload();
            //                    var currentPage = reportListGrid.getStore().currentPage;
            //                    if((reportCount > selectedReports.length  && currentPage > 0)||(currentPage == 1)){
            //                        reportListGrid.getStore().loadPage(currentPage);
            //                    }else if(currentPage > 1){
            //                        reportListGrid.getStore().loadPage(currentPage-1);
            //                    }
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),res.msg],2)
            }
        },function() {
            WtfComMsgBox(['Error',WtfGlobal.getLocaleText("acc.common.errorOccuratServerSide")],1)
        });
    }
});


Wtf.CreateCustomWidgetReport = Wtf.extend(Wtf.Window, {
    iconCls: "pwnd deskeralogoposition",
    initComponent:function() {
        this.addEvents({
            'aftersave':true 
        });
        this.createCustomReportList();
        this.createSelectedReportGrid();
        
        this.objsearchComponent = new Wtf.advancedSearchComponent({
            cm: this.customReportsList.colModel,
            moduleid: 102 ,
            hideRememberSerch : true,
            hideSearchBttn : true,
            hideCloseBttn : true,
            ignoreDefaultFields : true,
            height : 200,
            advSearch: false
        });
        
        this.configurAdvancedSearch();
        this.reportName = new Wtf.form.TextField({
            width : 300,
            allowBlank : false,
            fieldLabel :" Report Name* "
        });
        
        var northPanel = new Wtf.Panel({
            region: "north",
            height: 80,
            border: false,
            bodyStyle: "background:white;border-bottom:1px solid #bfbfbf;",
            html: getTopHtml("Create Custom Widget Report ", "Configure Widget Report", '../../images/save.png', false, '0px 0px 0px 0px')
        });
        var centerPanel = new Wtf.Panel({
            region: "center",
            bodyStyle:"padding: 20px;",
            items: [{
                layout:"form",
                bodyStyle : "margin-bottom:10px;",
                border:false,
//                bodyStyle :"padding:10px;",
                items :[this.reportName]
            },{
                xtype:"fieldset",
                height : 180,
                width : 850,
                title:"Please select search criteria",
                items :[this.objsearchComponent]
            },{
                xtype:"fieldset",
                height : 270,
                width : 850,
                layout : "column",
                title:"Please drag and drop at least one report",
                items :[{
                        columnWidth : 0.5,
                        border : false,
                        items :[this.customReportsList]
                },{
                        columnWidth : 0.5,
                        border : false,
                        items :[this.selectedReportsList]
                }]
            }]
        });

        Wtf.apply(this, {
            width : 900,
            modal : true,
            constrain: true,
            closeAction:"hide",
            resizable : false,
            bodyStyle:"background-color:#f1f1f1;",
            height : 670,
            layout : "border",
            items :[northPanel,centerPanel],
            buttons : [{
                text :"Save",
                handler : this.saveWidgetReport.createDelegate(this)
            },{
                text :"Reset",
                handler : this.clearSelections.createDelegate(this)
            },{
                text :"Cancel",
                scope : this,
                handler:function(){
                    this.hide();
                }
            }]
        }); 

        Wtf.CreateCustomWidgetReport.superclass.initComponent.apply(this, arguments);

    },
    configurAdvancedSearch: function() {
        this.objsearchComponent.show();
        this.objsearchComponent.advGrid.advSearch = true;
        this.objsearchComponent.advGrid.getComboData();
        this.doLayout();
       
    },
    createCustomReportList : function(){
        this.customReportRec =  new Wtf.data.Record.create([
        {           
            name:'id'
        },{           
            name:'name'
        },{
            name:'description'
        },{
            name:'methodName'
        },{
            name:'moduleid' 
        },{
            name:'isdefault' 
        }]);

        this.jsonReader = new Wtf.data.JsonReader({
            root: "data[\"data\"]",
            totalProperty: 'data[\"count\"]'
        }, this.customReportRec);

        var customReportStore = new Wtf.data.Store({
            url: "ACCReports/getReports.do",
            reader: this.jsonReader,
            baseParams:{
                roleid:Wtf.UserReporRole.URole.roleid,
                userid:loginid,
                isCustomWidgetReport : true
            }
        });
        customReportStore.load({
            start : 0,
            limit : 30
        });
        
        customReportStore.on("beforeload",function(){
            this.loadingMask.show();
        },this);
        
        customReportStore.on("load",function(){
            this.loadingMask.hide();
        },this);
        
        this.quickPanelSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.rem.5")+" Report name",
            width: 170,
            Store:customReportStore
        })
        
        this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            searchField: this.quickPanelSearch,
            id: "pagingtoolbar" + this.id,
            store: customReportStore,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.agedPay.norec"),  //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            })
        });
        
        this.customReportsList = new Wtf.grid.GridPanel({
            layout:"fit",
            height : 230,
            ddGroup :"selectReport",
            enableDragDrop : true,
            title : "Report List",
            store : customReportStore,
            tbar :[this.quickPanelSearch],
            columns: [new Wtf.grid.RowNumberer({width: 25}),
            {
                header: "Report", 
                sortable: true, 
                dataIndex: 'name'
            },

            {
                header: "Module", 
                sortable: true, 
                dataIndex: 'moduleid',
                renderer : function(val){
                   val = WtfGlobal.getModuleName(parseInt(val));
                   return val;
                }
            },{
                header: "Default /Custom", 
                sortable: true, 
                dataIndex: 'isdefault',
                renderer : function(val){
                    var newVal ="";
                    if(val){
                        newVal = "Default";
                    }else{
                        newVal = "Custom";
                    }
                    return newVal;
                }
            }],
            viewConfig: {
                forceFit: true
            },
            bbar: this.pagingToolbar,
            sm : new Wtf.grid.RowSelectionModel({singleSelect:true})
        });
        
        this.customReportsList.on("render",function(){
            this.loadingMask = new Wtf.LoadMask(this.customReportsList.body, {
                msg : WtfGlobal.getLocaleText("acc.msgbox.50")
            });
        },this);
        this.customReportsList.on("afterlayout",function(){
            this.createDropZone(this.customReportsList,"deselectReport");
        },this);
    },
    createSelectedReportGrid : function(){

        var selectedReportStore = new Wtf.data.Store({
            reader: this.jsonReader
        });
        
        this.selectedReportsList = new Wtf.grid.GridPanel({
            layout:"fit",
            height : 230,
            enableDragDrop : true,
            ddGroup :"deselectReport",
            title : "Selected Reports",
            store : selectedReportStore,
            columns: [new Wtf.grid.RowNumberer({width: 25}),
            {
                header: "Selected Report", 
                sortable: true, 
                dataIndex: 'name'
            },

            {
                header: "Module", 
                sortable: true, 
                dataIndex: 'moduleid',
                renderer : function(val){
                   val = WtfGlobal.getModuleName(parseInt(val));
                   return val;
                }
            },{
                header: "Default /Custom", 
                sortable: true, 
                dataIndex: 'isdefault',
                renderer : function(val){
                    var newVal ="";
                    if(val){
                        newVal = "Default";
                    }else{
                        newVal = "Custom";
                    }
                    return newVal;
                }
            }],
            viewConfig: {
                forceFit: true
            },
            sm : new Wtf.grid.RowSelectionModel({singleSelect:true})
        });
        
        this.selectedReportsList.on("afterlayout",function(){
            this.createDropZone(this.selectedReportsList,"selectReport");
        },this);
        
    },
    createDropZone: function(config, ddgroup) {
        var dropTarget = new Wtf.dd.DropTarget(config.getView().el.dom.childNodes[0].childNodes[1], {
            ddGroup: ddgroup,
            scope : this,
            notifyDrop : function(ddSource, e, data){
                function mapHeader(record, index, allItems) {
                    var isExist = false;
                    var mappedHeaderStore = this.scope.customReportsList.store;
                    if(ddgroup == "selectReport"){
                        mappedHeaderStore = this.scope.selectedReportsList.store;
                    }
                    var headerAccID = record.data.id
                    var mapStoreCount = mappedHeaderStore.data.items.length;
                    if(ddgroup == "selectReport"){
                        for(var i=0; i<mapStoreCount; i++){
                            var mappedRec = mappedHeaderStore.data.items[i].data;
                            if(headerAccID == mappedRec.id){
                                isExist = true;
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),mappedRec.reportname+" is already selected." ],2);
                            }
                        }
                    }
                    if(!isExist){
                        var newHeaderRecord = new Wtf.data.Record(record.data);
                        if(ddgroup == "deselectReport"){
                            ddSource.grid.store.remove(record);
                            ddSource.grid.getView().refresh();
                        }else{
                            mappedHeaderStore.add(newHeaderRecord);
                        }
                    }
                    ddSource.grid.getSelectionModel().clearSelections();
                }
                Wtf.each(ddSource.dragData.selections ,mapHeader.createDelegate(this));
                return(true);
            }
        },this);
    },
    
    saveWidgetReport : function(){
        var reportName = this.reportName.getValue();
        var searchJSON = this.objsearchComponent.advGrid.getJsonofStore();
        var appendCase = this.objsearchComponent.advGrid.appendCaseCombo.getValue();
        var selectedReports =  this.getSelectedReports();
        
        if(this.reportName.isValid() && selectedReports.length > 0 && reportName.trim().length > 0){
            Wtf.Ajax.requestEx({
                url: "ACCCreateCustomReport/saveCustomWidgetReports.do",
                method : "POST",
                params : {
                    reportname : reportName.trim(),
                    searchcriteria : JSON.stringify(searchJSON),
                    customreports : JSON.stringify(selectedReports),
                    filterappend : appendCase
                }
            }, this, function(res,req){
                if(res.success){
                    WtfComMsgBox(["Success","Report saved succcessfully."],0);
                    this.hide();
                    this.clearSelections();
                    this.fireEvent('aftersave',this,res);
                }else{
                     WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText(res.MESSAGE_KEY)],0);
                }
            }, function (){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Error"],1)
            });
        }else if(!this.reportName.isValid() || reportName.trim().length ==0){
            this.reportName.markInvalid("Please enter report name.");
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Please enter report name."],2);
        }else if(selectedReports.length == 0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Please select at least one custom report."],2);
        }
    },
    
    getSelectedReports : function(){
        var reportsArray = [];
        var report = {};
        var selectedRecords = this.selectedReportsList.getStore().getRange();
        for(var i = 0; i<selectedRecords.length ; i++){
            report = {
                "id" : selectedRecords[i].data.id,
                "moduleid" : selectedRecords[i].data.moduleid,
                "reportname" : selectedRecords[i].data.reportname || selectedRecords[i].data.name,
                "methodName" : selectedRecords[i].data.methodName,
                "isdefault" :selectedRecords[i].data.isdefault
            }
            reportsArray.push(report);
        }
        return reportsArray;
    },
    clearSelections : function(){
        this.reportName.setValue("");
        this.reportName.clearInvalid();
        this.selectedReportsList.getStore().removeAll();
        this.objsearchComponent.advGrid.getStore().removeAll();
    }
    
});
