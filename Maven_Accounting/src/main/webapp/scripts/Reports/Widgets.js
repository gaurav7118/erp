/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
function showWidgetDynamicLoad(consolidateFlag){
    consolidateFlag = consolidateFlag!=undefined?consolidateFlag:false;
    
    
    var panel=Wtf.getCmp('showWidgets');
    if(panel==null){
        panel = new Wtf.account.ShowWidget({
            id: 'showWidgets',
            border: false,
            layout: 'fit',
            iconCls: 'accountingbase agedrecievable',
            title: WtfGlobal.getLocaleText("acc.WidgetView"),
            tabTip:WtfGlobal.getLocaleText("acc.WidgetView"),
            closable: true
        });
        Wtf.getCmp('as').add(panel);
    }
    panel.on("activate",function(panel){
        panel.doLayout();
    });
    Wtf.getCmp('as').setActiveTab(panel);
    Wtf.getCmp('as').doLayout();
}


Wtf.account.ShowWidget = function(config) {
    this.id=config.id;
    this.btnArr = [];
    Wtf.apply(this, config);
    
    Wtf.account.ShowWidget.superclass.constructor.call(this, config);
}

 Wtf.helpDetails = Wtf.extend(Wtf.Component, {

    tplMarkup: ['<div id="fcue-360" class="fcue-outer" style="position: absolute; z-index:2000000; left: 188px; top: 12px;">'+
                '<div class="fcue-inner">'+
                '<div class="fcue-t"></div>'+
                '<div class="fcue-content">'+
                '<a onclick="closeCue();" href="#" id="fcue-close"></a>'+
                '<div class="ft ftnux"><p>'+
                '</p><span id="titlehelp" style="font-weight:bold;">Welcome Help Dialog</span>'+
                '<p></p>'+
                '<span id="titledesc">sssdd</span>'+
                '<div id="helpBttnContainerDiv"><p></p>'+
                '</div>'+
                '</div>'+
                '</div>'+
                '</div>'+
                '<div class="fcue-b">'+
                '<div></div>'+
                '</div>'+
                '<div class="fcue-pnt fcue-pnt-t-r">'+
                '</div>'+
                '</div>'],
    id : 'widgethelpdialog',

    initComponent: function(config) {
        Wtf.helpDetails.superclass.initComponent.call(this, config);
    },

    showHelpWindow: function(x,y,title,desc) {
        if(document.getElementById('fcue-360-mask'))
            document.getElementById('fcue-360-mask').style.display="block";
        this.tpl = new Wtf.Template(this.tplMarkup[0]);
        this.tpl.append(document.body,{});
        document.getElementById('titlehelp').innerHTML = title;
        document.getElementById('titledesc').innerHTML = desc;
        Wtf.get('fcue-360').setXY([x-330,y+30]);
        document.getElementById('fcue-360').style.visibility ="visible";
    }
});

Wtf.extend(Wtf.account.ShowWidget, Wtf.Panel, {
    onRender: function(config) {
        this.leadpan = new Wtf.Panel({
            border: false,
            layout: 'border',
            frame: false,
            items: [{
                id:'widget_container',
                region: 'center',
                xtype: 'portal',
                bodyStyle:"background:white;",
                border: false,
                items: [{
                    columnWidth: .5,
                    style:'padding:10px 1px 10px 10px',
                    cls: 'portletcls',
                    id: 'widget_container_box1',
                    border: false
                },{
                    columnWidth: .5,
                    style:'padding:10px 10px 10px 10px',
                    cls: 'portletcls',
                    id: 'widget_container_box2',
                    border: false
                }]
            }]

        });
        this.add(this.leadpan);
        Wtf.account.ShowWidget.superclass.onRender.call(this, config);
        this.createWidgets();

    },
    
    createWidgets: function(){
        Wtf.Ajax.requestEx({
            url: "ACCOtherReports/getWidgetReportList.do"
        }, this, this.successCallback, this.failureCallback);
    },
    
    successCallback:function(response){
        var url = "";
        var reportid = "";
        var reportTitle = "";
        var expanderMethodName = "";
        var helpText = "";
        var reportcount = 1;
        
        if(response.data.length > 0){
            for(var i=0; i<response.data.length; i++ ){
                url = response.data[i].widgeturl;
                reportid = response.data[i].id;
                reportTitle = response.data[i].name;
                expanderMethodName = response.data[i].methodName;
                helpText = response.data[i].helptext;
                var columnCount = ((reportcount)%2) ? 1:2;
                
                var fullurl = this.replaceGlobalParamsURL(url, reportid);
                helpText = this.replaceDatesofHelpText(helpText, reportid);
                var panel = undefined;
                if(reportid == Wtf.ReportID.MonthlyTradingAndProfitLoss || reportid == Wtf.ReportID.YearlyTradingAndProfitLoss)
                {
                    panel =  this.getWidgetFrameForHierachicalGrid(fullurl,reportid,reportTitle,expanderMethodName,helpText);   
                }else{
                    panel =  this.getWidgetFrame(fullurl,reportid,reportTitle,expanderMethodName,helpText);   
                }
                
                if(Wtf.UserReporRole.URole.roleid==1){
                    this.addWidget(panel,columnCount);
                    reportcount ++;
                }else{
                    for (var userpermcount = 0; userpermcount < Wtf.UserReportPerm.length; userpermcount++){
                        if (Wtf.UserReportPerm[userpermcount] == reportid) {
                            this.addWidget(panel,columnCount);
                            reportcount ++;
                            break;
                        }        
                    }
                }
            }        
        }
        Wtf.getCmp('widget_container').doLayout();
    },
    
    replaceGlobalParamsURL: function(url, reportid){
        if(url.indexOf(Wtf.WidgetGlobalParams.CONSOLIDATE_FLAG)!=-1){
            url = url.replace(Wtf.WidgetGlobalParams.CONSOLIDATE_FLAG,this.consolidateFlag);
        }
        while(url.indexOf(Wtf.WidgetGlobalParams.COMPANY_ID)!=-1){
            url = url.replace(Wtf.WidgetGlobalParams.COMPANY_ID,companyids);
        }
        while(url.indexOf(Wtf.WidgetGlobalParams.GLOBAL_CURRENCY_ID)!=-1){
            url = url.replace(Wtf.WidgetGlobalParams.GLOBAL_CURRENCY_ID,gcurrencyid);
        }
        while(url.indexOf(Wtf.WidgetGlobalParams.LOGIN_ID)!=-1){
            url = url.replace(Wtf.WidgetGlobalParams.LOGIN_ID,loginid);
        }
        while(url.indexOf(Wtf.WidgetGlobalParams.CURRENT_DATE)!=-1){            
            var currDate = WtfGlobal.convertToGenericStartDate(new Date());
            url = url.replace(Wtf.WidgetGlobalParams.CURRENT_DATE, currDate);
        }
        while(url.indexOf(Wtf.WidgetGlobalParams.FIN_START_DATE)!=-1){
            var startDate = WtfGlobal.convertToGenericStartDate(WtfGlobal.getDates(true));
            if(reportid == Wtf.ReportID.YearlyBookingsReport){
                var ffyearDate = new Date(Wtf.account.companyAccountPref.firstfyfrom);
                startDate = WtfGlobal.convertToGenericStartDate(new Date("Jan 01, "+ ffyearDate.getFullYear()+' 12:00:00 AM'));
            }else if(reportid == Wtf.ReportID.YearlyTradingAndProfitLoss){  //first financial year of Company
                var ffyear=new Date(Wtf.account.companyAccountPref.firstfyfrom); 
                startDate = "January, "+ ffyear.getFullYear();
            }else if(reportid == Wtf.ReportID.MonthlyTradingAndProfitLoss || reportid == Wtf.ReportID.MonthlySalesReport || reportid == Wtf.ReportID.MonthlySalesByProduct){  //current year of calender
                startDate = "January, "+ (new Date()).getFullYear();
            }
            url = url.replace(Wtf.WidgetGlobalParams.FIN_START_DATE, startDate);
        }
        while(url.indexOf(Wtf.WidgetGlobalParams.FIN_END_DATE)!=-1){
            var endDate = WtfGlobal.convertToGenericEndDate(WtfGlobal.getDates(false));
            if(reportid == Wtf.ReportID.MonthlyTradingAndProfitLoss || reportid == Wtf.ReportID.YearlyTradingAndProfitLoss || reportid == Wtf.ReportID.MonthlySalesReport || reportid == Wtf.ReportID.MonthlySalesByProduct){
                endDate = "December, "+ (new Date()).getFullYear();
            }
            url = url.replace(Wtf.WidgetGlobalParams.FIN_END_DATE, endDate);
        }
        return url;
    },

    replaceDatesofHelpText: function(helpText, reportid){
        while(helpText.indexOf(Wtf.WidgetGlobalParams.CURRENT_DATE)!=-1){            
            var currDate = (new Date()).format(WtfGlobal.getOnlyDateFormat());
            helpText = helpText.replace(Wtf.WidgetGlobalParams.CURRENT_DATE, currDate);
        }   
        while(helpText.indexOf(Wtf.WidgetGlobalParams.FIN_START_DATE)!=-1){
            var startDate = (WtfGlobal.getDates(true)).format(WtfGlobal.getOnlyDateFormat());
            if(reportid == Wtf.ReportID.YearlyTradingAndProfitLoss || reportid == Wtf.ReportID.YearlyBookingsReport){  //first financial year of Company
                var ffyear=new Date(Wtf.account.companyAccountPref.firstfyfrom) 
                startDate = new Date("Jan 01, "+ ffyear.getFullYear()+' 12:00:00 AM').format(WtfGlobal.getOnlyDateFormat());
            }else if(reportid == Wtf.ReportID.MonthlyTradingAndProfitLoss){  //current year of calender
                startDate = new Date("Jan 01, "+ (new Date()).getFullYear()+' 12:00:00 AM').format(WtfGlobal.getOnlyDateFormat());
            }else if(reportid == Wtf.ReportID.DailySalesReport || reportid == Wtf.ReportID.DailyBookingsReport){  //current month of calender
                startDate = new Date((new Date()).format('M')+" 01, "+ (new Date()).getFullYear()+' 12:00:00 AM').format(WtfGlobal.getOnlyDateFormat());
            }
            helpText = helpText.replace(Wtf.WidgetGlobalParams.FIN_START_DATE, startDate);
        }
        while(helpText.indexOf(Wtf.WidgetGlobalParams.FIN_END_DATE)!=-1){
            var endDate = (WtfGlobal.getDates(false)).format(WtfGlobal.getOnlyDateFormat());
            if(reportid == Wtf.ReportID.MonthlyTradingAndProfitLoss || reportid == Wtf.ReportID.YearlyTradingAndProfitLoss || reportid == Wtf.ReportID.YearlyBookingsReport){
                endDate = new Date("Dec 31, "+ (new Date()).getFullYear()+' 12:00:00 AM').format(WtfGlobal.getOnlyDateFormat());
            }else if(reportid == Wtf.ReportID.DailySalesReport || reportid == Wtf.ReportID.DailyBookingsReport){  //current month of calender
                endDate = new Date((new Date()).format('M')+" "+(new Date()).getDaysInMonth()+", "+ (new Date()).getFullYear()+' 12:00:00 AM').format(WtfGlobal.getOnlyDateFormat());
            }
            helpText = helpText.replace(Wtf.WidgetGlobalParams.FIN_END_DATE, endDate);
        }
        return helpText;
    },

    failureCallback:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],response.success*2+1);
    },

    //This function will create a widget window for report.
    //We need to pass url, reportid and reportTitle as parameter to function getWidgetFrame which return panel to add into widget view.
    
    addWidget: function(panel, colCount){
        var _ID = 'widget_container_box';
        var pl = Wtf.getCmp(_ID + colCount);
        if (pl != null) {
            pl.add(panel);
            pl.doLayout();
            panel.doLayout();
        }
    },
   
    formatAccountName:function(val,m,rec,i,j,s){
        var fmtVal=WtfGlobal.currencyRenderer(val);
        
        if(rec.data['fmt']){
            fmtVal='<font size=2px ><b>'+fmtVal+'</b></font>';
        }
        else if(rec.data["level"]==0&&rec.data["accountname"]!="") {
            fmtVal='<span style="font-weight:bold">'+fmtVal+'</span>';
        }
        return fmtVal;
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
    
    getParamList:function(url){
        var paramString= url.split("?")[1];
        var regex = /[^&?]*?=[^&?]*/g;
        var paramsList= paramString.match(regex);
        return paramsList;
    },
    
    getWidgetFrame:function (url,reportid,reportTitle,expanderMethodName,helpText){
        var finalParam={};
        var paramsList = this.getParamList(url);
        for (var i = 0; i <paramsList.length; i++) {
            var paramRes = paramsList[i];
            var keyValPair = paramRes.split("=");
            finalParam[keyValPair[0]]=keyValPair[1];
        }
        
//        Create a store for widget
        var store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: "totalCount",
                root: "data"
            }),
            baseParams:finalParam,
            url: url.split("?")[0]
        });

//        Create a Grid from Grid Config
        var grid = new Wtf.grid.GridPanel({
            store: store,
            border: false,
            columns: [],
            layout: 'fit',
            loadMask: true
        });
        store.load();
        
//        Create Paging tool bar
        var pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id + reportid,
            store: store,
            searchField: this.quickPanelSearch,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id: "pPageSize_" + this.id + reportid
            })
        })
        
//        handle Store On Load
        store.on('load',function(store) {
            var columns = [];
            columns.push(new Wtf.grid.RowNumberer({width: 25}));
            for ( var i=0; i<store.reader.jsonData.columns.length; i++){
                var rec = store.reader.jsonData.columns[i];
                if(rec.renderer){
                    rec.renderer = eval('('+ rec.renderer +')');
                }
                columns.push(rec);
            };
            grid.getColumnModel().setConfig(columns);
            grid.getView().refresh();
            
            if (store.getCount() < 1) {
                grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                grid.getView().refresh();
            }
        },this);
        
//        buttons to Expand and Close window.
        var tools = [{
            id:'gear',
            qtip:'Open into new Tab',
            handler: function(){
                 eval(expanderMethodName);
            }
        },{
            id:'help',
            qtip:'Help',
            handler: function(e, target, panel){
                var tmp= e.getXY();
                var we = new Wtf.helpDetails();
                we.showHelpWindow(tmp[0],tmp[1],reportTitle,helpText);
            }
        },{
            id:'close',
            qtip:'Close',
            handler: function(e, target, panel){
                panel.ownerCt.remove(panel, true);
            }
        }];
        
//        Create a panel
        panel = new Wtf.Panel({
            id:"widgetPanel"+reportid,
            tools: tools,
            title: reportTitle,
            layout:'fit',
            draggable:{
                onDrag : function(e){
                    var pel = this.proxy.getEl();
                    this.x = pel.getLeft(true);
                    this.y = pel.getTop(true);

                    var s = this.panel.getEl().shadow;
                    if (s) {
                        s.realign(this.x, this.y, pel.getWidth(), pel.getHeight());
                    }
                },
                endDrag : function(e){
                    this.panel.setPosition(this.x, this.y);
                    Wtf.getCmp('widget_container').doLayout();
                }
            },
            height:280,
            bbar: pagingToolbar,
            items: [grid]  
        });
        
        return panel;
    },
    
    getWidgetFrameForHierachicalGrid:function (url,reportid,reportTitle,expanderMethodName,helpText){
//        Create a store for widget
        var store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: "totalCount",
                root: "data"
            }),
            url: url
        });

        var columnArr = [];
        columnArr.push({
            header:'<b>'+WtfGlobal.getLocaleText("acc.balanceSheet.particulars")+'</b>',
            dataIndex:'accountname',
            renderer:this.formatAccountName,
            width:250,
            pdfwidth: 80,
            summaryRenderer:function(){
                return WtfGlobal.summaryRenderer(WtfGlobal.getLocaleText("acc.common.total"));
            }.createDelegate(this)
        });

        for(var i=0; i<18; i++){
            columnArr.push({
                hidden: false,
                dataIndex: 'amount_'+i,      
                renderer:this.formatData,  
                width: 110,
                align:'right',
                style: 'text-align:right'
            });
        };
        
        var grid = new Wtf.grid.HirarchicalGridPanel({
            store: store,
            hirarchyColNumber:0,
            border: false,
            columns: columnArr,
            layout: 'fit',
            autoScroll:true,
            viewConfig: {
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        grid.on("render", WtfGlobal.autoApplyHeaderQtip);
        store.load();
        grid.getStore().on("load", function(){
           
           var store = grid.getStore();
           var monthArray = store.data.items[store.data.length-1].json["months"];

            for(var i=0; i<monthArray.length; i++){            
                grid.getColumnModel().setColumnHeader((i+1), '<div align=center><b>'+monthArray[i]["monthname"]+'</b></div>') ;            
                var column = grid.getColumnModel().getColumnById((i+1));
                column.align= 'right';
                column.style= 'text-align:right';
            }

            var columnCount =  grid.getColumnModel().getColumnCount();
            var monthCount = monthArray.length;

            for(var i=1; i<(1+monthCount); i++){
                grid.getColumnModel().setHidden(i, false) ;
            }        
            
            // show those months with data
            for(var i=(monthCount+1); i<columnCount; i++){
                grid.getColumnModel().setHidden(i,true) ;
            } 

            var lcm = grid.getColumnModel();
            lcm.setRenderer(monthCount, this.formatMoney) ;
            var store1 = grid.getStore();
            grid.reconfigure(store1,lcm);
            
            for(var i=0; i< grid.getStore().data.length; i++){
                grid.collapseRow(grid.getView().getRow(i));
            }
        }, this);
        
//        buttons to Expand and Close window.
        var tools = [{
            id:'gear',
            qtip:'Open into new Tab',
            handler: function(){
                 eval(expanderMethodName);
            }
        },{
            id:'help',
            qtip:'Help',
            handler: function(e, target, panel){
                var tmp= e.getXY();
                var we = new Wtf.helpDetails();
                we.showHelpWindow(tmp[0],tmp[1],reportTitle, helpText);
            }
        },{
            id:'close',
            qtip:'Close',
            handler: function(e, target, panel){
                panel.ownerCt.remove(panel, true);
            }
        }];
        
//        Create a panel
        panel = new Wtf.Panel({
            id:"widgetPanel"+reportid,
            tools: tools,
            title: reportTitle,
            layout:'fit',
            draggable:{
                onDrag : function(e){
                    var pel = this.proxy.getEl();
                    this.x = pel.getLeft(true);
                    this.y = pel.getTop(true);

                    var s = this.panel.getEl().shadow;
                    if (s) {
                        s.realign(this.x, this.y, pel.getWidth(), pel.getHeight());
                    }
                },
                endDrag : function(e){
                    this.panel.setPosition(this.x, this.y);
                    Wtf.getCmp('widget_container').doLayout();
                }
            },
            height:280,
            items: [grid]  
        });
        
        return panel;
    }
});