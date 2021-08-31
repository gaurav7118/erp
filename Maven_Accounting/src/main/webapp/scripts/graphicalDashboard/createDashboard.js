/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */


function openCreateDashboard() {
    
    var panel = Wtf.getCmp("createDashboard");
            if(panel==null){
        panel = new Wtf.account.CreateDashboard({
            id : 'createDashboard',
            border : false,
//            layout: 'fit',
            title: "Create Dashboard",
            tabTip: "Create Dashboard ", 
            closable: true,
            iconCls:'create-dashboard'
        });
        Wtf.getCmp('as').add(panel);
        panel.on("resize", function(){
            panel.doLayout();
        },this);
        panel.on("activate", function() {
            
            panel.doLayout();
        }, this);
        Wtf.getCmp('as').doLayout(); 
    }
    Wtf.getCmp('as').setActiveTab(panel);
}

Wtf.account.CreateDashboard = function(config) {
    Wtf.apply(this, config);
    this.createDesignDashboard();
    this.createSaveWindow();
    this.createAddWidgetWindow();
    this.createBottomBar();
    this.style = 'background-color: #f0f0f0;';
    this.items = [this.designDashboard];

    Wtf.account.CreateDashboard.superclass.constructor.call(this,config );
}


Wtf.extend(Wtf.account.CreateDashboard, Wtf.account.ClosablePanel,{
    autoScroll :true,
    dashboardJson :{},
    editMenuConfig:{
        tag: 'ul', 
        cls: 'edit-links', 
        children: [
        {
            tag: 'li', 
            menuname:'edit', 
            cls: 'edit tpl-link', 
            html:"Edit"
        }
        ]
    },
    removeMenuConfig:{
        tag: 'ul', 
        cls: 'remove-links', 
        children: [
        {
            tag: 'li', 
            menuname:'remove', 
            cls: 'edit tpl-link', 
            html:"Remove"
        }
        ]
    },
    createBottomBar :function(){
      this.saveBtn = new Wtf.Toolbar.Button({
            text: "Save",
            tooltip: {text: WtfGlobal.getLocaleText("acc.advancesearch.searchBTN.ttip")},//'Add terms to search.'},
            scope:this,
            iconCls : "pwnd save",
            handler :function(){
                this.saveWindow.show();
            }
        });
        this.bbar =[this.saveBtn];
    },
    createSaveWindow :function(){
        var dashboardName = new Wtf.form.TextField({
            fieldLabel:"Dashboard Name",
            name:"dashboardName",
            width:240,
            maxLength:200
        });
        var form =new Wtf.form.FormPanel({
            border:false,
            items:[dashboardName]
        });
        this.saveWindow = new Wtf.Window({
            title :"Save Dashboard",
            closeAction :"hide",
            width : 500,
            bodyStyle:"padding:20px;",
            height : 250,
            modal : true,
            items :[form],
            buttons :[{
                text:"Submit",
                scope:this,
                handler : function(){
                    this.createJson();
                    var json1 = JSON.stringify(this.dashboardJson);
//                    var seen = [];

//                    var json1 = JSON.stringify(this.dashboardJson, function(key, val) {
//                        if (val != null && typeof val == "object") {
//                            if (seen.indexOf(val) >= 0) {
//                                return;
//                            }
//                            seen.push(val);
//                        }
//                        return val;
//                    });
                    Wtf.Ajax.requestEx({
                        url: "ACCUSDashboard/saveDashboard.do",
                        params:{
                            name : dashboardName.getValue(),
                            json : json1 
                        }
                    }, this, function(response) {
                        this.saveWindow.hide();
                        Wtf.Msg.alert('Status', 'Dashboard saved successfully.');
                        Wtf.getCmp("dashboardManager").dashboardList.store.reload();
                        
                        this.saveBtn.disable();
                        this.remove(this.designDashboard);
                        
                        var editLinks = document.getElementsByClassName("edit-links");
                        
                        for(var i=0;i<editLinks.length ; i++){
                            editLinks[i].style.display = "none";
                        }
                        
                        var removeLinks = document.getElementsByClassName("remove-links");
                        
                        for(var i=0;i<removeLinks.length ; i++){
                            removeLinks[i].style.display = "none";
                        }
                        
                        var dashlets = document.getElementsByClassName("dashlet");
                        
                        for(var i=0;i<dashlets.length ; i++){
                            dashlets[i].style.display = "none";
                        }
                    }, function() {});
                }
            }]
        });
    },
    createAddWidgetWindow : function(){
        this.addWidgetWindow = new Wtf.AddWidgetWindow({
            title :"Configuration",
            closeAction :"hide",
            width : 900,
//            bodyStyle:"padding:20px;",
            height : 600,
            modal : true,
            buttons :[{
                text : "Add",
                scope:this,
                handler : this.addWidget
            },{
                text:"Cancel",
                scope:this,
                handler : function(){
                    this.addWidgetWindow.hide();
                }
            }]
        });
    },
    createDesignDashboard:function(){
        this.designDashboard = new Wtf.Panel({
            height:80 ,
            width : "98%",
            border : false,
            cls : "dashlet-container add-row"
        });
        this.designDashboard.on("render",function(){
            var para = document.createElement("p");
            var node = document.createTextNode("Add Section");
            para.appendChild(node);
            
            var paraWithOptions =document.createElement("p");
            paraWithOptions.classList.add("options");
            var rowCol1 = document.createElement('a');
            var linkText = document.createTextNode("+ 1 column");
            rowCol1.appendChild(linkText);
            
            var rowCol2 = document.createElement('a');
            var linkText2 = document.createTextNode("+ 2 columns");
            rowCol2.appendChild(linkText2);
            
            var rowCol3 = document.createElement('a');
            var linkText3 = document.createTextNode("+ 3 columns");
            rowCol3.appendChild(linkText3);
            
            rowCol1.tabIndex =-1;
            rowCol2.tabIndex =-1;
            rowCol3.tabIndex =-1;
            
            rowCol1.onclick = this.addRow1Col.createDelegate(this);
            rowCol2.onclick = this.addRow2Col.createDelegate(this);
            rowCol3.onclick = this.addRow3Col.createDelegate(this);
            
            paraWithOptions.appendChild(rowCol1);
            paraWithOptions.appendChild(rowCol2);
            paraWithOptions.appendChild(rowCol3);
            
            var panelBody = this.designDashboard.body;
            panelBody.appendChild(para);
            panelBody.appendChild(paraWithOptions);
//            panelBody.on("click", this.addRow1Col, this);
        },this);
    },
    addRow1Col : function(){
        var column1 = new Wtf.Panel({
            height:90 ,
            cls:"dashlet",
            border:false,
            html:"<div style='color: #717171;font-size: 12px;margin-top: 10px;text-align: center;'>Add Widget</div>"
                +"<div style=' color: #717171;font-size: 54px;font-weight: bold;padding: 0;text-align: center;'>+</div>"
        });
        column1.on("render",function(){
            column1.body.on("click", function(a,b,c){
                this.openAddWidgetWindow(column1);
            }, this);
        },this);
        
        var row =  new Wtf.Panel({
            height:93 ,
            width : "98%",
            border : false,
            cls : "dashlet-container",
            items : [column1]
        });
        
        row.on("render",function(){
            var removeEl = Wtf.DomHelper.insertFirst(row.el, this.removeMenuConfig);
            removeEl.onclick = this.removeRow.createDelegate(this);
        },this);
        var itemsLength = this.items.length;
        this.insert(itemsLength-1,row);
        this.doLayout();
    },
    addRow2Col : function(){

        var column1 = new Wtf.Panel({
            columnWidth:0.495,
            height:90,
            border : false,
            style :"margin-right: 1%;",
            cls:"dashlet",
            html:"<div style='color: #717171;font-size: 12px;margin-top: 10px;text-align: center;'>Add Widget</div>"
               +"<div style=' color: #717171;font-size: 54px;font-weight: bold;padding: 0;text-align: center;'>+</div>"
        });
        column1.on("render",function(){
            column1.body.on("click", function(a,b,c){
                this.openAddWidgetWindow(column1);
            }, this);
        },this);
            
        var column2 = new Wtf.Panel({
            columnWidth:0.495,
            cls:"dashlet",
            border : false,
            height:90, 
            html:"<div style='color: #717171;font-size: 12px;margin-top: 10px;text-align: center;'>Add Widget</div>"
               +"<div style=' color: #717171;font-size: 54px;font-weight: bold;padding: 0;text-align: center;'>+</div>"
        });
        column2.on("render",function(){
            column2.body.on("click", function(){
                this.openAddWidgetWindow(column2);
            }, this);
        },this);
        
        var row =  new Wtf.Panel({
            height:93 ,
            width : "98%",
            border : false,
            layout :"column",
            cls : "dashlet-container",
            items : [column1,column2]
        });
        row.on("render",function(){
            var removeEl = Wtf.DomHelper.insertFirst(row.el, this.removeMenuConfig);
            removeEl.onclick = this.removeRow.createDelegate(this);
        },this);
        var itemsLength = this.items.length;
        this.insert(itemsLength-1,row);
        this.doLayout();
    },
    addRow3Col : function(){
        
        var column1 = new Wtf.Panel({
            columnWidth:0.330,
            height:90 ,
            style :"margin-right: 1%;",
            cls:"dashlet",
            border:false,
            html:"<div style='color: #717171;font-size: 12px;margin-top: 10px;text-align: center;'>Add Widget</div>"
                +"<div style=' color: #717171;font-size: 54px;font-weight: bold;padding: 0;text-align: center;'>+</div>"
        });
        column1.on("render",function(){
            column1.body.on("click", function(a,b,c){
                this.openAddWidgetWindow(column1);
            }, this);
        },this);
            
        var column2 = new Wtf.Panel({
            columnWidth:0.325,
            height:90 ,
            style :"margin-right: 1%;",
            cls:"dashlet",
            border:false,
            html:"<div style='color: #717171;font-size: 12px;margin-top: 10px;text-align: center;'>Add Widget</div>"
               +"<div style=' color: #717171;font-size: 54px;font-weight: bold;padding: 0;text-align: center;'>+</div>"
        });
        column2.on("render",function(){
            column2.body.on("click", function(){
                this.openAddWidgetWindow(column2);
            }, this);
        },this);
        
        var column3 = new Wtf.Panel({
            columnWidth:0.327,
            height:90 ,
            cls:"dashlet",
            border:false,
            html:"<div style='color: #717171;font-size: 12px;margin-top: 10px;text-align: center;'>Add Widget</div>"
               +"<div style=' color: #717171;font-size: 54px;font-weight: bold;padding: 0;text-align: center;'>+</div>"
        });
        column3.on("render",function(){
            column3.body.on("click", function(){
                this.openAddWidgetWindow(column3);
            }, this);
        },this);
        
        var row =  new Wtf.Panel({
            height:93 ,
            width : "98%",
            border : false,
            layout :"column",
            cls : "dashlet-container",
            items :[column1,column2,column3]
        });
        
        row.on("render",function(){
            var removeEl = Wtf.DomHelper.insertFirst(row.el, this.removeMenuConfig);
            removeEl.onclick = this.removeRow.createDelegate(this);
        },this);
        
        var itemsLength = this.items.length;
        this.insert(itemsLength-1,row);
        this.doLayout();
    },
    createJson : function(){
        var dashboardItems = clone(this.items.items);
        
        var dashboardArr = [];
        for(var cnt =0; cnt<dashboardItems.length-1;cnt++){
            var rowPanel = dashboardItems[cnt].initialConfig;
            rowPanel.rowNum = cnt + 1;
            var rowItems = rowPanel.items;
            var rowItemsConfig=[];
            
            for(var i=0 ; i< rowItems.length;i++){
                if(!(rowItems[i].el.dom.classList.contains("dashlet"))){
                    var col = rowItems[i].initialConfig;
                    col.colNum = i+1;
                    delete col["html"];
                    
                    col.items = rowItems[i].items.items[0].initialConfig;
                    delete col.items["chartConfig"];
                    delete col.items["store"];
                    delete col.items["tbar"];
                    rowItemsConfig.push(col);
                }
            }
            delete rowPanel["items"];
            rowPanel.items = rowItemsConfig;
            dashboardArr.push(rowPanel);
        }
        this.dashboardJson["section"] = dashboardArr;
    },
//    getAddWidgetColumn : function(columnWidth,noOfColumns){
//        var items =[]; 
//        for(var i = 0; i < noOfColumns; i++){
//            var config = {
//                height:80,
//                html:"Column "+i
//            };
//            var panel =new Wtf.Panel(config);
//            
//            panel.on("render",function(){
//                panel.body.on("click", this.openAddWidgetWindow, this);
//            },this);
//            items.push(panel);
//        }
//        return items;
//    },
    removeRow : function(e){
        var row = Wtf.getCmp(e.target.parentNode.id) || Wtf.getCmp(e.target.parentNode.parentNode.id);
        this.remove(row);
        row.destroy();
    },
    openAddWidgetWindow : function(widgetContainer){
        if(widgetContainer.el.dom.classList.contains("dashlet")){
            this.addWidgetWindow.widgetContainer = widgetContainer;
            this.widgetContainer = widgetContainer;
            this.addWidgetWindow.show();
        }
    },
    openEditWidgetWindow : function(e){
        var widgetContainer = Wtf.getCmp(e.target.parentNode.id) || Wtf.getCmp(e.target.parentNode.parentNode.id);
        this.widgetContainer = widgetContainer;
        this.addWidgetWindow.show();
    },
    addWidget : function(){
        var selectedWidget = JSON.clone(this.addWidgetWindow.widgetList.getSelectionModel().getSelected().json);
        
        var widget = createDashboardWidget(selectedWidget);
        
        this.widgetContainer.body.dom.innerHTML="";
        
        if(this.widgetContainer.isEditLinkAdded){
            if(this.widgetContainer.items.items[0]!=undefined){
                var containerItems = this.widgetContainer.items.items[0];
                this.widgetContainer.remove(containerItems);
                containerItems.destroy();
            }
        }else{
            var editEl = Wtf.DomHelper.insertFirst(this.widgetContainer.el, this.editMenuConfig);
            editEl.onclick = this.openEditWidgetWindow.createDelegate(this);
            this.widgetContainer.isEditLinkAdded= true;
        }
	
        if(widget!=undefined){
            this.widgetContainer.add(widget);
            this.widgetContainer.doLayout();
        }
        this.widgetContainer.removeClass("dashlet");
        this.widgetContainer.addClass("widget");
        this.addWidgetWindow.hide();
        if(widget.rendered){
            this.adjustHeight(widget);
        }else{
            widget.on("render",function(){
                this.adjustHeight(widget);
            },this);
        }
    },
    adjustHeight:function(widget){
        var height = widget.height;
//        height = parseInt(height.substring(0,height.indexOf("px")));
        this.widgetContainer.ownerCt.body.dom.style.height = height+20+"px";
        this.widgetContainer.body.dom.style.height = height+"px";
        this.widgetContainer.ownerCt.doLayout();
        this.doLayout();
    }
});


Wtf.AddWidgetWindow = function(config) {
    Wtf.apply(this, config);
    this.createWidgetList();
    this.createPropertyGrid();
    this.items = [{
        columnWidth:0.7,
        height : 535,
        layout:"fit",
        items :[this.widgetList]
    },{
        columnWidth:0.3,
        height : 535,
        layout:"fit",
        items :[this.propertyGrid]
    }];

    Wtf.AddWidgetWindow.superclass.constructor.call(this,config );
}


Wtf.extend(Wtf.AddWidgetWindow, Wtf.Window,{
    layout:"column",
    createWidgetList : function(){
        var searchRecord = Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'},
            {name: 'description'},
            {name: 'type'},
            {name: 'dataUrl'},
            {name: 'params'},
            {name: 'chartname'},
            {name: 'properties'},
            {name: 'subtype'}
        ]);

        var jsonReader = new Wtf.data.JsonReader({
            root: "data",
            totalProperty: 'count'
        }, searchRecord);

        var totalSalesstore = new Wtf.data.Store({
            reader: jsonReader,
            url: "ACCUSDashboard/getWidgets.do",
            baseParams: {
                companyids: companyid,
                consolidateFlag: false,
                creditonly: false,
                dir: 'ASC',
                enddate: 'December, 2016',
                gcurrencyid: Wtf.account.companyAccountPref.currencyid,
                getRepeateInvoice: false,
                mode: 18,
                nondeleted: true,
                stdate: 'January, 2016'
            }
        });
        totalSalesstore.load({
            start : 0,
            limit : 30
        });
        
        this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: totalSalesstore,
            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.agedPay.norec"),  //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            })
        });
        
        var selModel = new Wtf.grid.RowSelectionModel({
            singleSelect:true
        })
        this.widgetList = new Wtf.grid.GridPanel({
            title: 'Widgets',
            layout:"fit",
            store : totalSalesstore,
            columns: [{
                header: "Widget Name", 
                sortable: true, 
                dataIndex: 'name'
            },{
                header: "Description", 
                sortable: true, 
                dataIndex: 'description'
            }],
            viewConfig: {
                forceFit: true
            },
            sm:selModel,
            bbar: this.pagingToolbar
        });
        selModel.on('selectionchange', this.loadPropertyGrid, this);
    },
    createPropertyGrid : function(){
        this.propertyGrid = new Wtf.grid.PropertyGrid({
            title: 'Property Panel',
            layout:"fit",
            customEditors: {
                'Start Time': new Wtf.grid.GridEditor(new Wtf.form.TimeField({
                    selectOnFocus:true
                }))
            },
            source: {
//                "(name)": "My Object",
//                "Created": new Date(Date.parse('10/15/2006')),
//                "Available": false,
//                "Version": .01,
//                "Description": "A test object",
//                'Start Time': '10:00 AM'
            }
        });
    },
    loadPropertyGrid: function(selModel,rowIndex,rec) {
        if (selModel.selections.length > 0) {
            
            var store = new Wtf.data.SimpleStore({
                fields: [
                {name: 'name'}
                ]
            });
                var myData = [
            ['Pie'],
            ['Vertical Bar'],
            ['Horizontal Bar'],
            ['Line']
            ];
            store.loadData(myData);
            
            var source = {};
            var customEditor = {};
            var customEditors = {};
            if(selModel.getSelected().data.properties !=undefined){
                source = selModel.getSelected().data.properties.source || {};
                customEditor = selModel.getSelected().data.properties.customEditor || {};
                for (var prop in customEditor) {
                    customEditors[prop] = new Wtf.grid.GridEditor(new Wtf.form.ComboBox({
                        selectOnFocus:true,
                        mode:"local",
                        displayField : "name",
                        valueField :"name",
                        triggerAction : "all",
                        store:store
                    }));                
                }
            }
//            var sourceConfig = selModel.selections[0].data.properties.sourceConfig;
//            this.newPropertySource = [source, sourceConfig];
            this.propertyGrid.setSource(source);
            this.propertyGrid.customEditors = customEditors;
        }else{
            this.propertyGrid.setSource({});
        }
    }
});

JSON.clone = function(obj){
    return JSON.parse(JSON.stringify(obj));
}
function clone(obj) {
    if (null == obj || "object" != typeof obj) return obj;
    var copy = obj.constructor();
    for (var attr in obj) {
        if (obj.hasOwnProperty(attr)) copy[attr] = obj[attr];
    }
    return copy;
}