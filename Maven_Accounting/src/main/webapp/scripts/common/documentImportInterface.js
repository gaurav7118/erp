/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

Wtf.documentImportMenuArray = function(obj,moduleName,store,extraParams,extraConfig) {
    var archArray = [];
    var importButton = new Wtf.Action({
        text: (moduleName != "Product")? WtfGlobal.getLocaleText("acc.import.csv") : WtfGlobal.getLocaleText("acc.field.ImportProduct"),  // "Import CSV File" : "Import Product",
        id:'importcsvfile'+obj.id,
        tooltip:{
            text:WtfGlobal.getLocaleText("acc.import.csv")  //'Click to import CSV file.'
        },
        iconCls: 'pwnd importcsv',
        scope: obj,
        handler:function(){
          if(Wtf.getCmp("importDocumentwindow")==undefined){  
                var impWin1 = Wtf.commonDocumentFileImportWindow(obj, moduleName, store, extraParams, extraConfig);
                impWin1.show();
            }
        }
    });
    archArray.push(importButton);

    var importXLS=new Wtf.Action({
        text: WtfGlobal.getLocaleText("acc.import.xls"),  //"Import XLS File",
        tooltip:{
            text:WtfGlobal.getLocaleText("acc.import.xls")  //'Click to import XLS file.'
        },
        iconCls: 'pwnd importxls',
        scope: obj,
        handler:function(){
            if(Wtf.getCmp("importDocumentwindow")==undefined){
                var impWin1 = Wtf.xlsCommonDocumentFileImportWindow(obj,moduleName,store,extraParams, extraConfig);
                impWin1.show();
            }
        }
    });
    
    if (!(moduleName == "Product" || moduleName == "Group" || extraConfig.isExcludeXLS)) {
        archArray.push(importXLS);
    }

    return archArray;
}

Wtf.documentImportMenuButtonA = function(menuArray,obj,modName) {
    var tbarArchive=new Wtf.Toolbar.Button({
        iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
        id: (modName=="Product"? "importProduct9" : (modName=="Quotation"?"importCustomer6"+obj.id:"importVendor7"+obj.id) ),
        tooltip: {
            text: modName=="Customer Invoices"?  WtfGlobal.getLocaleText("acc.field.ImportSalesInvoiceDetails") : (modName=="Product"? "Import Product Details" : ( modName=="Quotation"? WtfGlobal.getLocaleText("acc.rem.53"):WtfGlobal.getLocaleText("acc.rem.54") ))///"Import "+modName+" details"
        },
        scope: obj,
        text:WtfGlobal.getLocaleText("acc.common.import"),  //"Import",
        menu: menuArray
    });
    return tbarArchive;
}

/*-------------------- Function to show Mapping Windows -----------------*/

Wtf.callDocumentMappingInterface = function(mappingParams, prevWindow){
    var mappingWindow = Wtf.getCmp("csvDocumentMappingInterface");
    if(!mappingWindow) {
        this.mapCSV=new Wtf.csvFileDocumentMappingInterface({
            csvheaders:mappingParams.csvheaders,
            modName:mappingParams.modName,
            moduleid:mappingParams.moduleid,
            customColAddFlag:mappingParams.customColAddFlag,
            typeXLSFile:mappingParams.typeXLSFile,
            impWin1:prevWindow,
            delimiterType:mappingParams.delimiterType,
            index:mappingParams.index,
            moduleName:mappingParams.moduleName,
            store:mappingParams.store,
//            contactmapid:this.contactmapid,
//            targetlistPagingLimit:this.targetlistPagingLimit,
            scopeobj:mappingParams.scopeobj,
            cm:mappingParams.cm,
            extraParams:mappingParams.extraParams,
            extraConfig:mappingParams.extraConfig
        }).show();
    } else {
            mappingWindow.impWin1= prevWindow,
            mappingWindow.csvheaders= mappingParams.csvheaders,
            mappingWindow.index= mappingParams.index,
            mappingWindow.extraParams= mappingParams.extraParams,
            mappingWindow.extraConfig= mappingParams.extraConfig
            mappingWindow.show();
        }
    
    if(dojoInitCount<=0){
        dojo.cometd.init("../../bind");
        dojoInitCount++;
    }

    if(mappingParams.typeXLSFile){ //.XLS File Import
        Wtf.getCmp("csvDocumentMappingInterface").on('importfn', function(mappingJSON, index, moduleName, store, scopeobj, extraParams, extraConfig){
            if(extraConfig == undefined) {
                extraConfig={};
            }
            extraConfig['filepath'] = Wtf.getCmp("importDocumentxls").xlsfilename;
            extraConfig['onlyfilename'] = Wtf.getCmp("importDocumentxls").onlyfilename;
            extraConfig['filename'] = Wtf.getCmp("importDocumentxls").onlyfilename;
            extraConfig['sheetindex'] = index;
            extraConfig['moduleName'] = moduleName;
            extraConfig['modName'] = moduleName;
            extraConfig['extraParams'] = extraParams;
            extraConfig['resjson'] = mappingJSON;
            Wtf.ValidateDocumentFileRecords(true, moduleName, store, scopeobj, extraParams, extraConfig);
        },this);
    } else { //.CSV File Import
        Wtf.getCmp("csvDocumentMappingInterface").on('importfn', function(resMapping, delimiterType, moduleName, store, scopObj, extraParams, extraConfig){
            if(extraConfig == undefined) {
                extraConfig={};
            }
            extraConfig['resjson'] = resMapping;
            extraConfig['modName'] = moduleName;
            extraConfig['delimiterType'] = delimiterType;
            extraConfig['extraParams'] = extraParams;
            Wtf.ValidateDocumentFileRecords(false, moduleName, store, scopObj, extraParams, extraConfig);
        },this);
    }
}
/**********************************************************************************************************
 *                              Mapping Window
 **********************************************************************************************************/
Wtf.csvFileDocumentMappingInterface = function(config) {
    Wtf.apply(this, config);
    Wtf.csvFileDocumentMappingInterface.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.csvFileDocumentMappingInterface, Wtf.Window, {
    iconCls : 'importIcon',
    width:850,
    height:570,
    modal:true,
    layout:"fit",
    id:'csvDocumentMappingInterface',
    closable:false,
    initComponent: function() {
        Wtf.csvFileDocumentMappingInterface.superclass.initComponent.call(this);
    },

    onRender: function(config){
        Wtf.csvFileDocumentMappingInterface.superclass.onRender.call(this, config);
        this.addEvents({
            'importfn':true,
            'customColAdd':true
        });
        this.title=this.typeXLSFile?WtfGlobal.getLocaleText(""):WtfGlobal.getLocaleText(""),  //"Map XLS headers" : "Map CSV headers";
        this.mappingJSON = "";
        this.masterItemFields = "";
        this.moduleRefFields = "";
        this.unMappedColumns = "";
        this.isMappingModified = "";

        this.columnRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'configid'},
            {name: 'validatetype'},
            {name: 'customflag'},
            {name: 'columnName'},
            {name: 'pojoName'},
            {name: 'fieldtype'},
            {name: 'isMandatory'},
            {name: 'dataindex'},
            {name: 'isConditionalMandetory'},
            {name: 'qtip'}
        ]);
        this.columnDs = new Wtf.data.GroupingStore({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.columnRec),
             groupField:'fieldtype', 
            sortInfo: {field: "isMandatory", direction:"DESC"},//Move all mandatory columns an top
            url: "ImportRecords/getColumnConfig.do"
        });

        this.columnCm = new Wtf.grid.ColumnModel([
        {
            header: WtfGlobal.getLocaleText("acc.import.col"),  //"Columns",
            dataIndex: "columnName",
            sortable:true,
            hideable: false,
            renderer:function(a,b,c){
                    var qtip="";var style="";
                    if (c.get("isConditionalMandetory")) {
                        style += "color:red;";
                        qtip += c.get("qtip");
                    } else if (c.get("isMandatory")) {
                        style += "font-weight:bold; color:red;";
                        qtip += "Mandatory Field";
                    }
                    return "<span wtf:qtip='"+qtip+"' style='cursor:pointer;"+style+"'>"+a+"</span>";
                }
        },{
            header:"",
            pdfwidth:80,
            dataIndex:'fieldtype',
            hidden:true
            
        }
        ]);
        this.quickSearchTF1 = new Wtf.KWLQuickSearchUseFilter({
            id : 'tableColumn'+this.id,
            width: 140,
            field : "columnName",
            emptyText:WtfGlobal.getLocaleText("acc.import.selCol")  //"Search Table Column "
        });
         var gridSummary = new Wtf.grid.GroupSummary({});
        this.tableColumnGrid = new Wtf.grid.GridPanel({
            ddGroup:"mapColumn",
            enableDragDrop : true,
            store: this.columnDs,
            sm:new Wtf.grid.RowSelectionModel({
                singleSelect:true
            }),
            cm: this.columnCm,
            height:370,
            border : false,
            tbar:[this.quickSearchTF1],
            plugins:[gridSummary],
            loadMask : true,
            view: new Wtf.grid.GroupingView({
                forceFit:true
            })
        });

        this.columnDs.on("load",function(){
            if(this.mappedColsDs.getCount()>0){ //Remove all mapped table columns
                for(var i=0; i<this.mappedColsDs.getCount(); i++){
                    for(var j=0; j<this.columnDs.getCount(); j++){
                        if(this.mappedColsDs.getAt(i).get("id")==this.columnDs.getAt(j).get("id")){
                            this.columnDs.remove(this.columnDs.getAt(j));
                            break;
                        }
                    }
                }
            }
            this.quickSearchTF1.StorageChanged(this.columnDs);
        },this);

        //Mapped Columns Grid
        this.mappedColsData="";
        this.mappedRecord = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'configid'},
            {name: 'validatetype'},
            {name: 'customflag'},
            {
                name: "columnName", type: 'string'
            },
            {
                name: "pojoName", type: 'string'
            },
            {
                name: "isMandatory"
            }
        ]);

        this.mappedColsDs = new Wtf.data.JsonStore({
            jsonData : this.mappedColsData,
            reader: new Wtf.data.JsonReader({
                root:"data"
            }, this.mappedRecord)

        });

        var mappedColsCm = new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText("acc.import.mapdcols"),  //"Mapped Columns",
            dataIndex: 'columnName'
        }]);

        this.mappedColsGrid= new Wtf.grid.GridPanel({
            ddGroup:"restoreColumn",
            enableDragDrop : true,
            sm:new Wtf.grid.RowSelectionModel({
                singleSelect:true
            }),
            store: this.mappedColsDs,
            cm: mappedColsCm,
            height:370,
            border : false,
            tbar:[{xtype:'panel',height:20,border:false}],
            loadMask : true,
            view:new Wtf.grid.GridView({
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.import.drag")  //"Drag and Drop columns here"
            })
        });
        this.quickSearchTF = new Wtf.KWLQuickSearchUseFilter({
            id : 'csvHeader'+this.id,
            width: 140,
            field : "header",
            emptyText:WtfGlobal.getLocaleText("acc.import.selCol")  //"Search "+(this.typeXLSFile?"xls":"csv")+" Headers "
        })
        // CSV header from csv file Grid
        this.csvHeaderDs = new Wtf.data.JsonStore({
            fields: [{
                name:"header"
            },{
                name:"index"
            },{
                name:"isMapped"
            }]
//            sortInfo: {field: "header", direction:"ASC"}
        });
        //loadHeaderData
        this.csvHeaderDs.on("datachanged",function(){
            this.totalHeaders=this.csvHeaderDs.getCount();
        },this);
        this.tempFileHeaderDs = new Wtf.data.JsonStore({ //Copy of header store used for auto mapping
            fields: [{
                name:"header"
            },{
                name:"index"
            }],
            sortInfo: {field: "header", direction:"ASC"}
        });
        var headerName = WtfGlobal.getLocaleText("acc.import.csvhead");  //"CSV Headers";
        var emptyGridText = "CSV Headers from given CSV file";
        if(this.typeXLSFile){
            headerName=WtfGlobal.getLocaleText("acc.import.xlshead");  //"XLS Headers"
            emptyGridText = WtfGlobal.getLocaleText("acc.rem.77");  //"XLS Headers from given CSV file";
        }
        var csvHeaderCm = new Wtf.grid.ColumnModel([{
            header: headerName,
            dataIndex: 'header',
            /**
             * Added Gray render for show mapped record in gray color
             * ERP-34313
             */
            renderer:function(a,b,c){
                var qtip="";var style="";
                if(c.data.isMapped){
                    style += "color:GRAY;";
                    qtip += "";
                }
                return "<span wtf:qtip='"+qtip+"' style='cursor:pointer;"+style+"'>"+a+"</span>";
            }
        }]);
        this.csvHeaderGrid= new Wtf.grid.GridPanel({
            ddGroup:"mapHeader",
            enableDragDrop : true,
            sm:new Wtf.grid.RowSelectionModel({
                singleSelect:true
            }),
            height:370,
            store: this.csvHeaderDs,
            cm: csvHeaderCm,
            border : false,
            loadMask : true,
            tbar:[this.quickSearchTF],
            view:new Wtf.grid.GridView({
                forceFit:true
//                emptyText:emptyGridText
            })
        });


        //Mapped CSV Header Grid
        this.mappedCsvheaders="";
        this.mappedCsvHeaderDs = new Wtf.data.JsonStore({
            fields: [{
                name:"header"
            },{
                name:"index"
            }]
        });
        this.mappedCsvHeaderDs.loadData(this.mappedCsvheaders);
        var mappedCsvHeaderCm = new Wtf.grid.ColumnModel([{
            header: WtfGlobal.getLocaleText("acc.import.mappedhead"),  //"Mapped Headers",
            dataIndex: 'header'
        }]);
        this.mappedCsvHeaderGrid= new Wtf.grid.GridPanel({
            ddGroup:"restoreHeader",
            enableDragDrop : true,
            sm:new Wtf.grid.RowSelectionModel({
                singleSelect:true
            }),
            store: this.mappedCsvHeaderDs,
            cm: mappedCsvHeaderCm,
            height:370,
            border : false,
            loadMask : true,
            tbar:[{xtype:'panel',height:20,border:false}],
            view:new Wtf.grid.GridView({
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.import.drag")  //"Drag and Drop Header here"
            })
        });

        this.add({
            border: false,
            layout : 'border',
            items :[{
                region: 'north',
                border:false,
                height:80,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                items:[{
                    xtype:"panel",
                    border:false,
                    height:70,
                    html:getImportTopHtml(WtfGlobal.getLocaleText("acc.rem.78"),"<ul style='list-style-type:disc;padding-left:15px;'><li>"+WtfGlobal.getLocaleText("acc.rem.145")+"</li><li>"+ WtfGlobal.getLocaleText("acc.rem.144")+"</li></ul>","../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
                }]
            },{
                region: 'center',
                autoScroll: true,
                bodyStyle : 'background:white;font-size:10px;',
                border:false,
                layout:"column",
                items: [
                    {
                        xtype:"panel",
                        columnWidth:.25,
                        border:false,
                        layout:"fit",
                        autoScroll:true,
                   // title:headerName,
                        items:this.csvHeaderGrid
                    },{
                        xtype:"panel",
                        columnWidth:.24,
                        border:false,
                        layout:"fit",
                        autoScroll:true,
                   // title:"Mapped Headers",
                        items:this.mappedCsvHeaderGrid
                    },{
                        xtype:"panel",
                        columnWidth:.24,
                        border:false,
                        layout:"fit",
                        autoScroll:true,
                   // title:"Mapped Columns",
                        items:this.mappedColsGrid
                    },{
                        xtype:"panel",
                        columnWidth:.25,
                        border:false,
                        layout:"fit",
                        autoScroll:true,
                  //  title:"Table Columns",
                        items:this.tableColumnGrid
                    }
                ]
            }
            ],
            buttonAlign: 'right',
            buttons:[{
                text: WtfGlobal.getLocaleText("acc.import.changePref"),  //'Change Preferences',
                minWidth: 80,
                handler: function(){
                        this.impWin1.show();
                        this.hide();
               },
                scope:this
            },{
                text: WtfGlobal.getLocaleText("acc.import.automapcol"),  //'Auto Map Columns',
                minWidth: 80,
                handler: this.autoMapHeaders,
                scope:this
            },{
                text: WtfGlobal.getLocaleText("acc.import.analyzeData"),  //'Analyze Data',
                minWidth: 80,
                handler: function(){
                    var totalmappedHeaders = this.mappedCsvHeaderDs.getCount();
                    var totalMappedColumns = this.mappedColsDs.getCount();
                    if(totalmappedHeaders==0 && totalMappedColumns==0) {
                        WtfImportMsgBox(43);
                    } else {
                        if(this.columnDs.getCount()>0) {
                            for(var i=0; i<this.columnDs.getCount(); i++) {
                                if(this.columnDs.getAt(i).data.isMandatory) {
                                    WtfImportMsgBox(44);
                                    return;
                                }
                            }
                        }
                        if(totalmappedHeaders==totalMappedColumns){
                            this.generateJsonForXML();
                                if(this.typeXLSFile){
    //                                this.fireEvent('importfn',this.mappingJSON,this.index,this.moduleName,this.store,this.contactmapid,this.targetlistPagingLimit,this.scopeobj,this.extraParams, this.extraConfig);
                                    this.fireEvent('importfn',this.mappingJSON,this.index,this.moduleName,this.store,this.scopeobj,this.extraParams, this.extraConfig);
                                }else {
                                    this.fireEvent('importfn',this.mappingJSON, this.delimiterType, this.moduleName, this.store, this.scopeobj, this.extraParams, this.extraConfig);
                                }
                                this.hide();
                            } else {
                            WtfImportMsgBox([WtfGlobal.getLocaleText("acc.import.headerMApping"), WtfGlobal.getLocaleText("acc.import.selCol")], 0);
                        }
                    }
                },
                scope:this
            },{
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
                minWidth: 80,
                handler: function(){
                    closeDocumentImportWindow();
                },
                scope: this
            }]
        });
        
        //to identify which column should be displayed from default header at the time of import
        var subModuleFlag = 0;
        if (this.modName == "Cash Sales" || this.modName == Wtf.Cash_Purchase_List || (this.extraConfig.otherwise != undefined && this.extraConfig.otherwise)||this.extraConfig.customer) {
            subModuleFlag = 1;
        } else if (this.modName == "Customer Invoices" || (this.extraConfig.againstVendor != undefined && this.extraConfig.againstVendor)||this.extraConfig.vendor) {
            subModuleFlag = 2;
        }else if(this.extraConfig.GL){
            subModuleFlag=3;
        }else if(this.extraConfig.isSales!=undefined && this.modName=="GSTTerm" && this.extraConfig.isSales==true){
            subModuleFlag=1;
        }
        /*
         * If we importing the Expense Case Purchase Invoice then need to pass below parameters at JAVA side
         */
        var isExpenseInvoiceImport=false;
        var inCash=false;
        if(this.extraConfig.isExpenseInvoiceImport != undefined  && this.extraConfig.isExpenseInvoiceImport !="" && this.extraConfig.isExpenseInvoiceImport){
           subModuleFlag = 2;
           isExpenseInvoiceImport=true;
           if(this.extraConfig.incash != undefined  && this.extraConfig.incash !="" && this.extraConfig.incash){
               inCash=true;
           }
        }

        this.on("show", function(){//Reload csv and table column grids
            if(!this.headerConfig || this.headerConfig != this.csvheaders){ // Check for new mapping updates
                this.headerConfig = this.csvheaders;
                this.mappedColsDs.removeAll();
                this.mappedCsvHeaderDs.removeAll();
                this.loadHeaderData();
                if(this.columnDs.getCount()>0){
                    this.columnDs.loadData(this.columnDs.reader.jsonData);
                } else {
                    this.columnDs.load({params : {module : this.modName, fetchCustomFields:true, isdocumentimportFlag:true, subModuleFlag:subModuleFlag,isExpenseInvoiceImport:isExpenseInvoiceImport,incash:inCash}});
                }
            }
        });

        //this.isMappingModified: Flag to recall validation function
        this.columnDs.on("add", function(){this.isMappingModified=true;}, this);
        this.mappedColsDs.on("add", function(){this.isMappingModified=true;}, this);
        this.csvHeaderDs.on("add", function(){this.isMappingModified=true;}, this);
        this.mappedCsvHeaderDs.on("add", function(){this.isMappingModified=true;}, this);

        this.on("afterlayout",function(){
            function rowsDiff(store1,store2){
                return diff=store1.getCount()-store2.getCount();
            }

            function unMapRec(atIndex){
                var headerRec = mappedHeaderStore.getAt(atIndex);
                if(headerRec!==undefined){
                    mappedHeaderStore.remove(headerRec);
                    /**
                     * Handle unmapped records color check
                     * ERP-34313
                     */
                    if(mappedHeaderStore.find("index",headerRec.data.index) == -1){
                        headerStore.getAt(headerStore.find("index",headerRec.data.index)).set('isMapped',false);
                        headerGrid.getView().refresh();
                    }
                    headerGrid.getView().refresh();
//                    headerStore.add(headerRec);//Commented to allow Multiple header mapping{SK}
                }

                var columnRec = mappedColumnStore.getAt(atIndex);
                if(columnRec!==undefined){
                    mappedColumnStore.remove(columnRec);
                    columnStore.add(columnRec);
                     //Rearrange table columns
                    columnStore.sort("columnName","ASC");  
                    columnStore.sort("isMandatory","DESC");
                }
            }

            columnStore = this.columnDs;
            columnGrid = this.tableColumnGrid;

            mappedColumnStore = this.mappedColsDs;
            mappedColumGrid = this.mappedColsGrid;

            headerStore = this.csvHeaderDs;
            headerGrid = this.csvHeaderGrid;

            mappedHeaderStore = this.mappedCsvHeaderDs;
            mappedHeaderGrid = this.mappedCsvHeaderGrid;

            // Drag n drop [ Headers -> Mapped Headers ]
            DropTargetEl =  mappedHeaderGrid.getView().el.dom.childNodes[0].childNodes[1];
            DropTarget = new Wtf.dd.DropTarget(DropTargetEl, {
                ddGroup    : 'mapHeader',
//                copy       : true,
                notifyDrop : function(ddSource, e, data){
                    function mapHeader(record, index, allItems) {
                        if(rowsDiff(mappedHeaderStore,mappedColumnStore)==0){
                            if(columnStore.getCount()!=0){
                                /**
                                * Handle mapped records color check
                                * ERP-34313
                                */
                                headerStore.getAt(headerStore.find("index",record.data.index)).data.isMapped=true;
                                headerGrid.getView().refresh();
                                var newHeaderRecord = new Wtf.data.Record(record.data);
                                mappedHeaderStore.add(newHeaderRecord);
//                                ddSource.grid.store.remove(record);//Commented to allow Multiple header mapping{SK}
                            } else {
                                WtfImportMsgBox([WtfGlobal.getLocaleText("acc.import.headerMApping"), WtfGlobal.getLocaleText("acc.import.noCol")], 0);
                            }
                        }else{
                            WtfImportMsgBox([WtfGlobal.getLocaleText("acc.import.headerMApping"), WtfGlobal.getLocaleText("acc.import.prevHead")], 0);
                        }
                    }
                    Wtf.each(ddSource.dragData.selections ,mapHeader);
                    return(true);
                }
            });

            // Drag n drop [ Mapped Headers -> Headers ]
            DropTargetEl =  headerGrid.getView().el.dom.childNodes[0].childNodes[1];
            DropTarget = new Wtf.dd.DropTarget(DropTargetEl, {
                ddGroup    : 'restoreHeader',
//                copy       : true,
                notifyDrop : function(ddSource, e, data){
                    function restoreColumn(record, index, allItems) {
                        unMapRec(ddSource.grid.store.indexOf(record));
                    }
                    Wtf.each(ddSource.dragData.selections ,restoreColumn);
                    return(true);
                }
            });

            // Drag n drop [ columns -> Mapped columns ]
            DropTargetEl =  mappedColumGrid.getView().el.dom.childNodes[0].childNodes[1];
            DropTarget = new Wtf.dd.DropTarget(DropTargetEl, {
                ddGroup    : 'mapColumn',
                copy       : true,
                notifyDrop : function(ddSource, e, data){
                    function mapColumn(record, index, allItems) {
                        if(rowsDiff(mappedHeaderStore,mappedColumnStore)==1){
                            mappedColumnStore.add(record);
                            ddSource.grid.store.remove(record);
                        }else{
                            WtfImportMsgBox([WtfGlobal.getLocaleText("acc.import.headerMApping"), WtfGlobal.getLocaleText("acc.import.selHead")], 0);
                        }
                    }
                    Wtf.each(ddSource.dragData.selections ,mapColumn);
                    return(true);
                }
            });

            // Drag n drop [ Mapped columns -> columns ]
            DropTargetEl =  columnGrid.getView().el.dom.childNodes[0].childNodes[1];
            DropTarget = new Wtf.dd.DropTarget(DropTargetEl, {
                ddGroup    : 'restoreColumn',
                copy       : true,
                notifyDrop : function(ddSource, e, data){
                    function restoreColumn(record, index, allItems) {
                        unMapRec(ddSource.grid.store.indexOf(record));
                    }
                    Wtf.each(ddSource.dragData.selections ,restoreColumn);
                    return(true);
                }
            });
        },this);
    },

    loadHeaderData: function(){
        this.csvHeaderDs.loadData(this.csvheaders);
        this.quickSearchTF.StorageChanged(this.csvHeaderDs);
    },

    autoMapHeaders: function(){
        //Sort columns by name for comparison
        this.columnDs.sort("columnName","ASC");
        this.csvHeaderDs.sort("header","ASC");

        //Clone csv header store
        if(this.csvHeaderDs.getCount()>0){
            if(this.tempFileHeaderDs.getCount()>0){this.tempFileHeaderDs.removeAll();}
            this.tempFileHeaderDs.loadData(this.csvheaders);
        }
        
        //Exact Match
        for(var i=0; i<this.columnDs.getCount(); i++){
            var colrec = this.columnDs.getAt(i);
            var colHeader = colrec.data.columnName;
            colHeader = colHeader.trim();

            for(var j=0; j<this.tempFileHeaderDs.getCount(); j++){
                var csvrec = this.tempFileHeaderDs.getAt(j);
                var csvHeader = csvrec.data.header;
                csvHeader = csvHeader.trim();

                if(colHeader.toLowerCase()==csvHeader.toLowerCase()){
                    this.columnDs.remove(colrec);
                    this.mappedColsDs.add(colrec);

                    this.tempFileHeaderDs.remove(csvrec);
                    this.mappedCsvHeaderDs.add(csvrec);
                    /*
                     * add checked true for Show record is mapped 
                     * ERP-34313
                     */
                    var csvStore = this.csvHeaderGrid.store;
                    csvStore.getAt(csvStore.find("index",csvrec.get('index'))).set('isMapped',true);
                    this.csvHeaderGrid.getView().refresh();
                    i--;//'i' decreamented as count of columnDs store is reduce by 1
                    break;
                }
            }
        }

        //Like Match from Table Columns
        for(i=0; i<this.columnDs.getCount(); i++){
            colrec = this.columnDs.getAt(i);
            colHeader = colrec.data.columnName;
            colHeader = colHeader.trim();
            var regex = new RegExp("^"+colHeader, "i");

            for(j=0; j<this.tempFileHeaderDs.getCount(); j++){
                csvrec = this.tempFileHeaderDs.getAt(j);
                csvHeader = csvrec.data.header;
                csvHeader = csvHeader.trim();

                if(regex.test(csvHeader)){
                    this.columnDs.remove(colrec);
                    this.mappedColsDs.add(colrec);

                    this.tempFileHeaderDs.remove(csvrec);
                    this.mappedCsvHeaderDs.add(csvrec);
                    /*
                     * add checked true for Show record is mapped 
                     * ERP-34313
                     */
                    var csvStore = this.csvHeaderGrid.store;
                    csvStore.getAt(csvStore.find("index",csvrec.get('index'))).set('isMapped',true);
                    this.csvHeaderGrid.getView().refresh();
                    i--;//'i' decreamented as count of columnDs store is reduce by 1
                    break;
                }
            }
        }

        //Like Match from CSV Header
        for(j=0; j<this.tempFileHeaderDs.getCount(); j++){
            csvrec = this.tempFileHeaderDs.getAt(j);
            csvHeader = csvrec.data.header;
            csvHeader = csvHeader.trim();
            regex = new RegExp("^"+csvHeader, "i");

            for(i=0; i<this.columnDs.getCount(); i++){
                colrec = this.columnDs.getAt(i);
                colHeader = colrec.data.columnName;
                colHeader = colHeader.trim();

                if(regex.test(colHeader)){
                    this.columnDs.remove(colrec);
                    this.mappedColsDs.add(colrec);

                    this.tempFileHeaderDs.remove(csvrec);
                    this.mappedCsvHeaderDs.add(csvrec);
                    /*
                     * add checked true for Show record is mapped 
                     * ERP-34313
                     */
                    var csvStore = this.csvHeaderGrid.store;
                    csvStore.getAt(csvStore.find("index",csvrec.get('index'))).set('isMapped',true);
                    this.csvHeaderGrid.getView().refresh();
                    j--;//'j' decreamented as count of csvHeaderDs store is reduce by 1
                    break;
                }
            }
        }

        //Move all mandatory columns an top
        this.columnDs.sort("isMandatory","DESC");

        if(this.mappedColsDs.getCount()==0){    // No matching pairs
            WtfImportMsgBox(52,0);
        }
    },

    generateJsonForXML : function(){
        this.mappingJSON = "";
        this.masterItemFields = "";
        this.moduleRefFields = "";
        this.unMappedColumns = "";
        for(var i=0;i<this.mappedCsvHeaderDs.getCount();i++){
            var columnname = (this.mappedColsDs.getAt(i).get("customflag") == true)? this.mappedColsDs.getAt(i).get("columnName") : this.mappedColsDs.getAt(i).get("pojoName");
            var isLineItem = (this.mappedColsDs.getAt(i).get("fieldtype") == "Line Level Custom  Items")? true : false;
            var isMandatory = (this.mappedColsDs.getAt(i).get("customflag") == true)?this.mappedColsDs.getAt(i).get("isMandatory"):false
            
            this.mappingJSON+="{\"csvindex\":\""+this.mappedCsvHeaderDs.getAt(i).get("index")+"\","+
                                "\"csvheader\":\""+this.mappedCsvHeaderDs.getAt(i).get("header")+"\","+
                                "\"columnname\":\""+columnname+"\","+
                                "\"dataindex\":\""+this.mappedColsDs.getAt(i).get("dataindex")+"\","+
                                "\"customflag\":\""+this.mappedColsDs.getAt(i).get("customflag")+"\","+
                                "\"isLineItem\":\""+isLineItem+"\",";
                                if (this.mappedColsDs.getAt(i).get("customflag") == true) {
                                    this.mappingJSON+="\"isMandatory\":\"" + isMandatory + "\"";
                                }
                                this.mappingJSON+="},";
            var validateType=this.mappedColsDs.getAt(i).get("validatetype");
            if(validateType=="ref" || validateType=="refdropdown"){
                if(this.mappedColsDs.getAt(i).get("configid").trim().length > 0){
                    this.masterItemFields += " "+this.mappedColsDs.getAt(i).get("columnName")+",";
                } else {
                    this.moduleRefFields += " "+this.mappedColsDs.getAt(i).get("columnName")+",";
                }
            }
        }
        this.mappingJSON = this.mappingJSON.substr(0, this.mappingJSON.length-1);
        this.mappingJSON = "{\"root\":["+this.mappingJSON+"]}";

        this.masterItemFields = this.masterItemFields.length>0 ? this.masterItemFields.substr(0, this.masterItemFields.length-1) : this.masterItemFields.trim();
        this.moduleRefFields = this.moduleRefFields.length>0 ? this.moduleRefFields.substr(0, this.moduleRefFields.length-1) : this.moduleRefFields.trim();

        for(i=0;i<this.columnDs.getCount();i++){
            this.unMappedColumns += " "+this.columnDs.getAt(i).get("columnName")+",";
        }
        this.unMappedColumns = this.unMappedColumns.length>0 ? this.unMappedColumns.substr(0, this.unMappedColumns.length-1) : this.unMappedColumns.trim();
    }
});

/*-------------------- Function to show Validate Windows -----------------*/
Wtf.ValidateDocumentFileRecords = function(typeXLSFile, moduleName, store, scopeobj, extraParams, extraConfig){
    var url = "ImportRecords/importRecords.do";
    if(extraConfig == undefined) {
        extraConfig={};
    } else {
        if(extraConfig.url!=undefined){
            url = extraConfig.url;
        }
    }
    extraConfig['moduleName'] = moduleName;
    extraConfig['extraParams'] = extraParams;

    var importParams = {};
    importParams.url = url;
    importParams.extraConfig = extraConfig;
    importParams.extraParams = extraParams;
    importParams.store = store;
    importParams.scopeobj = scopeobj;

    var validateWindow = Wtf.getCmp("IWDocumentValidationWindow");
    if(!validateWindow) {
        new Wtf.IWDocumentValidationWindow({
           title: WtfGlobal.getLocaleText("acc.import.valAnalysis"),  //"Validation Analysis Report",
           prevWindow: Wtf.getCmp("csvDocumentMappingInterface"),
           typeXLSFile: typeXLSFile,
           importParams: importParams
        }).show();
    }else{
        validateWindow.show();
    }
}
/**********************************************************************************************************
 *                              Validation Window
 **********************************************************************************************************/
Wtf.IWDocumentValidationWindow=function(config){
    Wtf.IWDocumentValidationWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.IWDocumentValidationWindow,Wtf.Window,{
    iconCls : 'importIcon',
    width: 750,
    height: 570,
    modal: true,
    layout: "border",
    id: 'IWDocumentValidationWindow',
    closable: false,
    initComponent:function(config){
        Wtf.IWDocumentValidationWindow.superclass.initComponent.call(this,config);
        this.prevButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.import.remapHead"),  //"Remap header",
            scope: this,
            minWidth: 80,
            handler: function(){
                if(this.prevWindow){
                    this.prevWindow.show();
                }
                this.hide();
            }
        });
        this.importButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.import.impData"),  //"Import Data",
            scope: this,
            minWidth: 80,
            handler: this.importRecords
        });
        this.cancelButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //"Cancel",
            scope: this,
            minWidth: 80,
            handler: function(){
                closeDocumentImportWindow();
            }
        });
        this.buttons = [this.prevButton, this.importButton, this.cancelButton];
    },

    onRender:function(config){
        Wtf.IWDocumentValidationWindow.superclass.onRender.call(this,config);

        this.on("show", function(){
            var uploadWindow = Wtf.getCmp("importDocumentwindow");          //check for pref updates
            var mappingWindow = Wtf.getCmp("csvDocumentMappingInterface");  //check for mapping updates
            if((uploadWindow && uploadWindow.isPrefModified) || (mappingWindow && mappingWindow.isMappingModified)){ // Check for new mapping updates
                if(uploadWindow){uploadWindow.isPrefModified=false;}
                if(mappingWindow){mappingWindow.isMappingModified=false;}
                this.validateRecords();
            }
        },this);

        this.northMessage = "<ul style='list-style-type:disc;padding-left:15px;'>" +
                    "<li>"+WtfGlobal.getLocaleText("acc.import.msg1")+"</li>"+
                    "<li>"+WtfGlobal.getLocaleText("acc.import.msg2")+"</li>";

        this.add(this.northPanel= new Wtf.Panel({
            region: 'north',
            height: 70,
            border: false,
            bodyStyle: 'background:white;padding:7px',
            html: getImportTopHtml(WtfGlobal.getLocaleText("acc.import.msg3"), this.northMessage+"</ul>" ,"../../images/import.png", true, "0px", "2px 0px 0px 10px")
        }));

        this.columnRec = new Wtf.data.Record.create([
            "col0","col1","col2","col3","col4","col5","col6","col7","col8","col9",
            "col10","col11","col12","col13","col14","col15","col16","col17","col18","col19",
            "col20","col21","col22","col23","col24","col25","col26","col27","col28","col29",
            "col30","col31","col32","col33","col34","col35","col36","col37","col38","col39",
            "col40","col41","col42","col43","col44","col45","col46","col47","col48","col49",
            "col50","col51","col52","col53","col54","col55","col56","col57","col58","col59",
            "col60","col61","col62","col63","col64","col65","col66","col67","col68","col69",
            "col70","col71","col72","col73","col74","col75","col76","col77","col78","col79",
            "col80","col81","col82","col83","col84","col85","col86","col87","col88","col89",
            "invalidcolumns","validateLog"]);

        this.colsReader = new Wtf.data.JsonReader({
            root: 'data',
            totalProperty: 'count'
        },this.columnRec);

        this.columnDs = new Wtf.data.Store({
            proxy: new Wtf.data.PagingMemoryProxy([]),
            reader: new Wtf.data.JsonReader({
                totalProperty: "count",
                root: "data"
            },this.columnRec)
        });

        this.columnCm = new Wtf.grid.ColumnModel([
        {
            header: " ",
            dataIndex: "col0"
        }
        ]);
        this.sm = new Wtf.grid.RowSelectionModel({singleSelect:true});
        this.gridView = new Wtf.grid.GridView({
//                forceFit:true,
//                emptyText:"All records are valid, click on \"Import\" to continue."
            });
//        this.gridView = new Wtf.ux.grid.BufferView({
//            scrollDelay: false,
//            autoFill: true
//        });
        this.Grid = new Wtf.grid.GridPanel({
            store: this.columnDs,
            sm:this.sm,
            cm: this.columnCm,
            border : true,
            loadMask : true,
            view: this.gridView,
            bbar: this.pag=new Wtf.PagingToolbar({
                pageSize: 30,
                border : false,
                id : "paggintoolbar"+this.id,
                store: this.columnDs,
                plugins : this.pPageSizeObj = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                }),
                autoWidth : true,
                displayInfo: true,
//                displayMsg: "Displaying {0} - {1} of {2} Invalid Records",
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores") //""
            })
        });

        this.sm.on("selectionChange", function(){
            this.updateLogDetails(true);
        }, this);

        this.add({
            region: 'center',
            layout: 'fit',
            border: false,
            autoScroll: true,
            bodyStyle: 'background:white;padding:7px',
            items: this.Grid
        });

        // ERP-32463 - To show Total record, valid record and invalid record count
        this.ValidationRecordCount = new Wtf.Panel({
            border: false,
            bodyStyle: 'padding-top:7px',
            html: "<div>&nbsp</div>"
        });
        this.ValidationDetails = new Wtf.Panel({
            border: false,
            bodyStyle: 'padding-top:7px',
            html: "<div>&nbsp</div>"
        });

        this.progressBar = new Wtf.ProgressBar({
            text:WtfGlobal.getLocaleText("acc.import.msg4"),  //'Validating...',
            hidden: true,
            cls: "x-progress-bar-default"
        });
        this.add(this.ValidationDetails);
        this.add({
            region: 'south',
            autoScroll: true,
            height: 70,
            border: false,
            bodyStyle: 'background:white;padding:0 7px 7px 7px',
            items: [
                this.progressBar,
                this.ValidationRecordCount,
                this.ValidationDetails
            ]
        });
    },

    validateRecords: function(){
        if(this.columnDs.getCount()>0) {        // clear previous validation
            this.columnDs.removeAll();
        }
        this.updateLogDetails(true);
        //  ERP-32463 - Reset validation record count entry to emtpy.
        this.ValidationRecordCount.body.dom.innerHTML= "";
        this.enableDisableButtons(false);
        
        Wtf.Ajax.timeout=900000;
//        Wtf.commonWaitMsgBox("Validating data... It may take few moments...");

        this.validateSubstr = "/ValidateFile/" + this.importParams.extraConfig.filename ;
        dojo.cometd.subscribe(this.validateSubstr, this, "globalInValidRecordsPublishHandler");
        var subModuleFlag = 0;
        if (this.importParams.extraConfig.modName == "Cash Sales" || (this.importParams.extraConfig.otherwise!=undefined && this.importParams.extraConfig.otherwise || this.importParams.extraConfig.customer)) {
            subModuleFlag = 1;
        } else if (this.importParams.extraConfig.modName == "Customer Invoices" || (this.importParams.extraConfig.againstVendor!=undefined && this.importParams.extraConfig.againstVendor)||this.importParams.extraConfig.vendor) {
            subModuleFlag = 2;
        }else if(this.importParams.extraConfig.GL){
            subModuleFlag=3;
        }
        
        /*
         * If we importing the Expense Case Purchase Invoice then need to pass below parameters at JAVA side
         */
        var isExpenseInvoiceImport=false;
        var inCash=false;
        if(this.importParams.extraConfig.isExpenseInvoiceImport != undefined  && this.importParams.extraConfig.isExpenseInvoiceImport !="" && this.importParams.extraConfig.isExpenseInvoiceImport){
           subModuleFlag = 2;
           isExpenseInvoiceImport=true;
        }
        if(this.importParams.extraConfig.incash != undefined  && this.importParams.extraConfig.incash !="" && this.importParams.extraConfig.incash){
          inCash=true;
        }

        Wtf.Ajax.requestEx({
            url: this.importParams.url+'?type=submit&fetchCustomFields=true&do=validateData&subModuleFlag='+subModuleFlag+'&isExpenseInvoiceImport='+isExpenseInvoiceImport+'&incash='+inCash,
            waitMsg :WtfGlobal.getLocaleText("acc.import.msg4"),  //'Validating...',
            scope:this,
            params: this.importParams.extraConfig
        },
        this,
        function (action,res) {
            Wtf.updateProgress();
            if(action.success){
                this.createGrid(action);
                // ERP-32463 - To show Total record, valid record and invalid record count
                var countMsg = "<div>" + WtfGlobal.getLocaleText("acc.import.total.records") + ":</b> " + action.totalrecords + ", <b>" + WtfGlobal.getLocaleText("acc.import.valid.records") + ":</b> " + action.valid + ", <span style = 'color:#F00;'><b>" + WtfGlobal.getLocaleText("acc.import.invalid.records") + ":</b> " + (action.totalrecords - action.valid) + " </span></div>";
                this.ValidationRecordCount.body.dom.innerHTML= countMsg;
                this.enableDisableButtons(true);
            } else {
                WtfImportMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.import.msg14")+action.msg], 1);
            }
            if(action.exceededLimit=="yes"){ // update north panel
                this.northMessage = this.northMessage+"<li>"+WtfGlobal.getLocaleText("acc.import.msg6")+"</li><br/>";
                this.northPanel.body.dom.innerHTML=getImportTopHtml(WtfGlobal.getLocaleText("acc.import.msg7"), this.northMessage+"</ul>","../../images/import.png", true, "0px", "2px 0px 0px 10px")
            }
            this.importParams.extraConfig.exceededLimit = action.exceededLimit;
            Wtf.Ajax.timeout=30000;
        },
        function (action,res) {
            Wtf.updateProgress();
            WtfImportMsgBox(50, 1);
            this.importParams.extraConfig.exceededLimit = action.exceededLimit;
            Wtf.Ajax.timeout=30000;
        });
    },

    importRecords: function(){
        dojo.cometd.unsubscribe(this.validateSubstr);

        Wtf.Ajax.timeout=900000;
        Wtf.commonWaitMsgBox(WtfGlobal.getLocaleText("acc.import.msg7"));
        Wtf.Ajax.requestEx({
            url: this.importParams.url+'?type=submit&do=import',
            waitMsg :'importing...',
            scope:this,
            params: this.importParams.extraConfig
        },
        this,
        function(res) {
            Wtf.updateProgress();
            if(res.success){
                if(res.Module == Wtf.Acc_Product_Master_ModuleId) {
                    Wtf.uomStore.reload();
                }
                if(res.exceededLimit=="yes"){ // Importing data with thread
//                    Wtf.Msg.alert('Success', 'We are now importing your data from the uploaded file.<br/>Depending on the number of records, this process can take anywhere from few minutes to several hours.<br/>A detailed report will be sent to you via email and will also be displayed in the import log after the process is completed.');
                    showImportSummary(true, res);
                } else {
                    if(this.importParams.store!=undefined && res.TLID == undefined) {
                        this.importParams.store.reload();
                    }
                    // StoreManager (Global Store) reload
                    Wtf.globalStorereload(this.importParams.extraConfig);
//                    WtfImportMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), res.msg], 0);
                    showImportSummary(false, res);
                }
                Wtf.Ajax.timeout=30000;
                closeDocumentImportWindow();
            } else { // Failure
                WtfImportMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), res.msg], 1);
            }
        },
        function(res){
            Wtf.updateProgress();
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.import.msg8"),WtfGlobal.getLocaleText("acc.import.msg9")+'<br/>'+WtfGlobal.getLocaleText("acc.import.msg10"));
            Wtf.Ajax.timeout=30000;
            closeDocumentImportWindow();
        })
    },

    createGrid: function(response){
        this.createColumnModel(response);
        
        this.columnDs.proxy.data = response;
        this.columnDs.load({params:{start:0, limit:this.pag.pageSize}});
        this.Grid.reconfigure(this.columnDs, this.columnCm);
        this.updateLogDetails(false);
    },

    createColumnModel: function(response){
        this.columnRec = new Wtf.data.Record.create(response.record);
        this.columnCm = new Wtf.grid.ColumnModel(response.columns);

        var sheetStartIndex = this.importParams.extraConfig.startindex;
        this.columnCm.setRenderer(0, function(val, md, rec){ //Add start index and row no. column [in case of .XLS import with start index > 1]
            if(sheetStartIndex!= undefined){
                val = (val*1) + (sheetStartIndex*1);
            }
            return ""+val;
        });

        for(var j=1; j<this.columnCm.getColumnCount()-1; j++){
            this.columnCm.setRenderer(j, function(val, md, rec, ri, ci, store){ // Add renderer to invalid columns
                var invalidColumns = rec.data.invalidcolumns;
                var columnDataIndex = "col"+ci+",";
                var regex = new RegExp(columnDataIndex);
                if(regex.test(invalidColumns)){
                    return "<div style='color:#F00'>"+val+"</div>";
                } else {
                    return val;
                }
            });
        }

        this.columnCm.setRenderer(this.columnCm.getColumnCount()-1, function(val){ // Add renderer to last column
            return "<span wtf:qtip=\""+val+"\">"+val+"</span>";
        });
        this.Grid.reconfigure(this.columnDs, this.columnCm);
    },

    updateLogDetails: function(onSelection){
        var msg = "";
        if(this.columnDs.getCount()>0) {
            if(this.sm.getCount()==1){
                var rec = this.sm.getSelected();
                var rowNo = rec.data.col0;
                var sheetStartIndex = this.importParams.extraConfig.startindex;
                if(sheetStartIndex!= undefined){
                    rowNo = (rowNo*1) + (sheetStartIndex*1);
                }
                msg = "<div><b> "+WtfGlobal.getLocaleText("acc.import.msg11")+rowNo+":</b><br/>"+
                        ""+rec.data.validateLog + ""+
                      "</div>";
            } else {
                msg = "<div><b>"+WtfGlobal.getLocaleText("acc.import.msg12")+"</b><br/>"+
                WtfGlobal.getLocaleText("acc.import.msg13")+
                      "</div>";
            }
        } else if(this.columnDs.getCount()==0) {
            if(!onSelection){
                msg = WtfGlobal.getLocaleText("acc.import.msg14"); //"All records are valid, Please click \"Import Data\" to continue.";
                this.gridView.emptyText= "<b>"+msg+"</b>";
                this.gridView.refresh();
            }
        }

        this.ValidationDetails.body.dom.innerHTML= msg;
    },

    globalInValidRecordsPublishHandler: function(response){
        var msg = "";
        var res = eval("("+response.data+")");

        if(res.finishedValidation){
            this.enableDisableButtons(true);
            return;
        }
        
        if(res.isHeader){
            this.createColumnModel(res);
        }else{
            if(res.parsedCount){
//                msg = (res.invalidCount==0?"Validated":("Found <b>"+res.invalidCount+"</b> invalid record"+(res.invalidCount>1?"s":"")+" out of top"))+" <b>"+res.parsedCount+"</b> record"+(res.parsedCount>1?"s":"")+" from the file.";
                msg = "Validated <b>"+res.parsedCount+"</b>"+ (res.invalidCount==0?"":(" and found <b>"+res.invalidCount+"</b> invalid record"+(res.invalidCount>1?"s":"")+""))+" out of <b>"+res.fileSize+"</b> record"+(res.fileSize>1?"s":"")+" from the file.";
                this.progressBar.updateProgress(res.parsedCount/res.fileSize, msg);
            }else{
                var newRec = new this.columnRec(res);
                this.columnDs.add(newRec);//if(this.columnDs.getCount()<=this.pag.pageSize)this.columnDs.add(newRec);//this.columnDs.insert(0,newRec);
//                msg = (res.count==0?"Validated":("Found <b>"+res.count+"</b> invalid record"+(res.count>1?"s":"")+" out of top"))+" <b>"+res.totalrecords+"</b> record"+(res.totalrecords>1?"s":"")+" from the file.";
                msg = "Validated <b>"+res.totalrecords+"</b>"+ (res.invalidCount==0?"":(" and found <b>"+res.count+"</b> invalid record"+(res.count>1?"s":"")+""))+" out of <b>"+res.fileSize+"</b> record"+(res.fileSize>1?"s":"")+" from the file.";
                this.progressBar.updateProgress(res.parsedCount/res.fileSize, msg);
                this.gridView.scroller.dom.scrollTop = this.gridView.scroller.dom.scrollHeight-2;
            }
        }
    },
    
    enableDisableButtons: function(enable){
        if(enable){
            this.prevButton.enable();
            this.importButton.enable();
            this.cancelButton.enable();
            this.progressBar.hide();
        }else{
            this.prevButton.disable();
            this.importButton.disable();
            this.cancelButton.disable();
            this.progressBar.show();
            this.progressBar.updateProgress(0,WtfGlobal.getLocaleText("acc.import.msg4"));
            this.gridView.emptyText= "";
            this.gridView.refresh();
        }
    }
});

function showImportSummary(backgroundProcessing, response){
      //as there are only to message coming either success all or failure all
      // to modify these message we did below change
      // Refer ERP-18637 for deratil
        var msg1="";
        if(response.msg=="" || response.msg==undefined){
           msg1=WtfGlobal.getLocaleText("acc.rem.169");
        } else {
           msg1=response.msg; 
        }
//	if(response.msg == "All records are imported successfully.")
//		var msg1 = WtfGlobal.getLocaleText("acc.rem.168");
//	else
//		var msg1 = WtfGlobal.getLocaleText("acc.rem.169");
    var message = "";
    if(backgroundProcessing){
        message = "<div class=\"popup-info\">"+
            "<h2 class=\"blue-h2\">"+WtfGlobal.getLocaleText("acc.import.backProgress1")+"</h2>"+
            "<br/>"+
            "<div class=\"right-bullets\"><span>1</span>"+WtfGlobal.getLocaleText("acc.import.backProgress2")+"</div>"+
            "<div class=\"right-bullets\"><span>2</span>"+WtfGlobal.getLocaleText("acc.import.backProgress3")+"</div>"+
            "<div class=\"right-bullets\"><span>3</span>A detailed report will be sent to you via email and will also be displayed in the <a wtf:qtip=\"Click here to open Import Log\" href=\"#\" onclick=\"linkImportFilesLog()\">"+WtfGlobal.getLocaleText("acc.importLog.importLog")+"</a> after the process is completed.</div>"+
            "<img border=\"0\" src=\"../../images/importWizard/import-log.jpg\"/>"+
        "</div>";
    } else {
        message = "<div class=\"popup-info\">"+
            "<h2 class=\"blue-h2\">"+WtfGlobal.getLocaleText("acc.import.status")+"</h2>"+
            "<br/>"+
            "<div class=\"right-bullets\"><span>1</span>"+msg1+"</div>"+
            "<div class=\"right-bullets\"><span>2</span>"+ WtfGlobal.getLocaleText("acc.rem.90")+" <a wtf:qtip=\""+ WtfGlobal.getLocaleText("acc.rem.91")+"\" href=\"#\" onclick=\"linkImportFilesLog()\">"+WtfGlobal.getLocaleText("acc.importLog.importLog")+"</a>.</div>"+
            "<img border=\"0\" src=\"../../images/importWizard/import-log.jpg\"/>"+
        "</div>";

    }
    var win = new Wtf.Window({
        resizable: true,
        layout: 'border',
        modal:true,
        width: 655,
        height: backgroundProcessing ? 400 : 350,
        iconCls: 'importIcon',
        title: WtfGlobal.getLocaleText("acc.import.status"),   //'Import Status',
        id: "importSummaryWin",
        items: [
                {
                    region:'center',
                    layout:'fit',
                    border:false,
                    bodyStyle: 'background:white;font-size:10px;padding-left:10px;',
                    html: message
                }
        ],
        buttons: [{
            text:WtfGlobal.getLocaleText("acc.import.viewLog"),  //"View Import Log",
            scope: this,
            handler:function() {
                linkImportFilesLog();
            }
        },{
            text:WtfGlobal.getLocaleText("acc.common.close"),  //"Close",
            scope: this,
            handler:function() {
                Wtf.getCmp("importSummaryWin").close();
            }
        }]
    },this).show();
}

function linkImportFilesLog(){
    // After importing any file close the 'Import Summary Window' if open.[SK]
    var SummaryWin = Wtf.getCmp("importSummaryWin");
    if(SummaryWin) {
        if(SummaryWin.isVisible()){
            SummaryWin.close();
        } else {
            SummaryWin.destroy();
        }
    }
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.qanalysis, Wtf.Perm.qanalysis.view))
    	callImportFilesLog();
    else
    	WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.import.impLog"));
}
/*-------------------- Function to show Upload Windows -----------------*/
Wtf.commonDocumentFileImportWindow = function(obj, moduleName, store, extraParams, extraConfig){
    var impWin1 = new Wtf.DocumentUploadFileWindow({
        title: WtfGlobal.getLocaleText("acc.import.csv"),  //'Import CSV File',
        width: 600,
        height: (moduleName == 'Group')?300:400,
        iconCls: 'importIcon',
        obj: obj,
        moduleName: moduleName,
        store: store,
        extraParams: extraParams,
        extraConfig:extraConfig,
        typeXLSFile: false
    });
    return impWin1;
}

Wtf.xlsCommonDocumentFileImportWindow = function(obj,moduleName,store,extraParams, extraConfig) {
    var impWin1 = new Wtf.DocumentUploadFileWindow({
        title: WtfGlobal.getLocaleText("acc.import.xls"),  //'Import XLS File',
        width: 600,
        height: 340,
        iconCls: 'importIcon',
        obj: obj,
        moduleName: moduleName,
        store: store,
        extraParams: extraParams,
        extraConfig:extraConfig,
        typeXLSFile: true
    });
    return impWin1;
}
/*----------------------------------------------------------------------------
--------------------------- commonUploadWindow -------------------------------
------------------------------------------------------------------------------*/
Wtf.DocumentUploadFileWindow=function(config){
    Wtf.DocumentUploadFileWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.DocumentUploadFileWindow, Wtf.Window,{
    id: 'importDocumentwindow',
    layout: "border",
    closable: false,
    resizable: false,
    modal: true,
    iconCls: 'importIcon',
    initComponent:function(config){
        Wtf.DocumentUploadFileWindow.superclass.initComponent.call(this,config);

        this.nextButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.next")+" &nbsp; >>",  //<font size=2> >> </font>",
            scope: this,
            minWidth: 80,
            handler: function(){
                if(this.typeXLSFile){
                    this.uploadXLSFile();
                }else {
                    this.uploadCSVFile();
                }
            }
        });
        this.cancelButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //"Cancel",
            scope: this,
            minWidth: 80,
            handler: function(){
                closeDocumentImportWindow();
            }
        });
        this.buttons = [this.nextButton, this.cancelButton];
    },

    onRender:function(config){
        Wtf.DocumentUploadFileWindow.superclass.onRender.call(this,config);
        this.isPrefModified = false;
        
        this.isGroupImport = false;
        if(this.moduleName == "Group"){
            this.isGroupImport = true;
        }
        if(!(this.moduleName == "Product" || this.moduleName == "Group")) {
            var delimiterStore = new Wtf.data.SimpleStore({
                fields: ['delimiterid','delimiter'],
                data : [
                [0,"Colon"],
                [1,'Comma'],
                [2,'Semicolon']//,
//            [3,'Space'],
//            [4,'Tab']
                ]
            });
        } else {
            var delimiterStore = new Wtf.data.SimpleStore({
                fields: ['delimiterid','delimiter'],
                data : [
                    [1,'Comma']
                ]
            });
        }

        
        this.conowner= new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.rem.88")+" ",  //'Delimiter ',
            hiddenName:'Delimiter',
            store:delimiterStore,
            valueField:'delimiter',
            displayField:'delimiter',
            mode: 'local',
            triggerAction: 'all',
            emptyText:WtfGlobal.getLocaleText("acc.rem.89"),  //'--Select delimiter--',
            typeAhead:true,
            selectOnFocus:true,
            allowBlank:false,
            width: 200,
            itemCls : (this.typeXLSFile)?"hidden-from-item":"",
            hidden: this.typeXLSFile,
            hideLabel: this.typeXLSFile,
            forceSelection: true,
            value:'Comma'
       });
       
      this.masterPreference = new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.import.masterPref1"),  //"For missing entries in dropdown fields",
            autoHeight: true,
            border: false,
            hidden:this.isGroupImport,
            cls: "import-Wiz-fieldset",
            defaultType: 'radio',
            items: [
                this.master0 = new Wtf.form.Radio({
                    checked: true,
                    fieldLabel: '',
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.import.masterPref2"),  //"Ignore entire record",
                    name: 'masterPreference',
                    inputValue: "0"
                }),
                this.master1 = new Wtf.form.Radio({
                    ctCls:"fieldset-item",
                    fieldLabel: '',
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.import.masterPref3"),  //"Ignore entry for that record",
                    name: 'masterPreference',
                    inputValue: "1"
                }),
                this.master2 = new Wtf.form.Radio({
                    ctCls:"fieldset-item1",
                    fieldLabel: '',
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.import.masterPref4"),  //"Add new entry to master record in dropdown",
                    name: 'masterPreference',
                    inputValue: "2"
                })]
        });

        this.dfRec = Wtf.data.Record.create ([
            {name:'formatid'},
            {name:'name'}
        ]);
        this.dfStore=new Wtf.data.Store({
            url:"kwlCommonTables/getAllDateFormats.do",
            baseParams:{
                mode:32,
                newDate: WtfGlobal.convertToGenericDate(new Date())//As per browser's timezone
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.dfRec)
        });
        this.dfStore.load();
        this.dfStore.on('load',function(){
            if(this.dfStore.getCount()>0){
                this.datePreference.setValue("2"); // Default for YYYY-MM-DD
            }
        },this);
        this.datePreference= new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.import.df"),  //'Date Format',
            hiddenName:'dateFormat',
            store:this.dfStore,
            valueField:'formatid',
            displayField:'name',
            mode: 'local',
            triggerAction: 'all',
            width: 200,
            itemCls : (this.typeXLSFile || this.moduleName == "Product")?"hidden-from-item":"",
            hidden: this.typeXLSFile || this.moduleName == "Product" || this.isGroupImport,
            hideLabel: this.typeXLSFile || this.moduleName == "Product" || this.isGroupImport,
//            editable : false,
            forceSelection: true
        });

        this.browseField = new Wtf.form.TextField({
            id:'browseBttn',
            border:false,
            inputType:'file',
            fieldLabel:WtfGlobal.getLocaleText("acc.import.filename")+" ",  //'File name ',
            name: 'test'
        });
        
        var storeageName = "";
        var fileName = "";
        var ModuleName = "";
        
        if (this.moduleName == "Customer Invoices") {
            storeageName = 'download_sample_sales_invoice_list.csv';
            fileName = 'sample_sales_invoice_list';
            ModuleName ='Sample Sales Invoice List';
        } else if (this.moduleName == "Sales Order") {
            storeageName = 'download_sample_sales_order_list.csv';
            fileName = 'sample_sales_order_list';
            ModuleName = this.moduleName;
        } else if (this.moduleName == "Vendor Invoice") {
            storeageName = 'download_sample_purchase_invoice_list.csv';
            fileName = 'sample_purchase_invoice_list';
            ModuleName = this.moduleName;
        } else if (this.moduleName == "Quotation") {
            storeageName = 'download_sample_customer_quotation_list.csv';
            fileName = 'sample_customer_quotation_list';
            ModuleName = this.moduleName;
        } else if (this.moduleName == "Purchase Order") {
            storeageName = 'download_sample_purchase_order_list.csv';
            fileName = 'sample_purchase_order_list';
            ModuleName = this.moduleName;
        } else if (this.moduleName == Wtf.Vendor_Quotation_List) {
            storeageName = 'download_sample_vendor_quotation_list.csv';
            fileName = 'sample_vendor_quotation_list';
            ModuleName = this.moduleName;
        } else if (this.moduleName == Wtf.Delivery_Order_List) {
            storeageName = 'download_sample_delivery_order_list.csv';
            fileName = 'sample_delivery_order_list';
            ModuleName = this.moduleName;
        } else if (this.moduleName == Wtf.Cash_Sales_List) {
            storeageName = 'download_sample_cash_sales_list.csv';
            fileName = 'sample_cash_sales_list';
            ModuleName = this.moduleName;
        } else if (this.moduleName == "Convert Sales Invoice in to Cash Sales") {
            storeageName = 'download_sample_convert_credit_sales_to_cash_sales_list.csv';
            fileName = 'sample_convert_credit_sales_to_cash_sales_list';
            ModuleName = this.moduleName;
        }else if (this.moduleName == Wtf.Goods_Receipt_Order_List) {
            storeageName = 'download_sample_goods_receipt_order_list.csv';
            fileName = 'sample_goods_receipt_order_list';
            ModuleName = this.moduleName;
        } else if (this.extraConfig.otherwise != undefined && this.extraConfig.otherwise) {
            storeageName = 'download_sample_credit_note_otherwise.csv';
            fileName = 'sample_credit_note_otherwise';
            ModuleName = Wtf.Credit_Note_Otherwise;
        } else if (this.extraConfig.againstVendor != undefined && this.extraConfig.againstVendor) {
            storeageName = 'download_sample_credit_note_against_vendor.csv';
            fileName = 'sample_credit_note_against_vendor';
            ModuleName = Wtf.Credit_Note_Against_Vendor;
        } else if (this.moduleName=="Receipt"&&this.extraConfig.customer != undefined && this.extraConfig.customer) {
            /*
             * Download Sample file for Receive Payment Against Customer
             */
            storeageName = 'download_sample_receipt_payment_customer.csv';
            fileName = 'sample_receipt_payment_customer';
            ModuleName = "ReceiptPaymentCustomer";
        } else if (this.moduleName=="Receipt"&&this.extraConfig.vendor != undefined && this.extraConfig.vendor) {
            /*
             * Download Sample file for Receive Payment Against Vendor
             */
            storeageName = 'download_sample_receipt_payment_vendor.csv';
            fileName = 'sample_receipt_payment_vendor';
            ModuleName = "ReceiptPaymentVendor";
        } else if (this.moduleName=="Receipt"&&this.extraConfig.GL != undefined && this.extraConfig.GL) {
            /*
             * Download Sample file for Receive Payment Against GL
             */
            storeageName = 'download_sample_receipt_payment_gl.csv';
            fileName = 'sample_receipt_payment_gl';
            ModuleName = "ReceiptPaymentGL";
        } else if (this.moduleName=="Payment"&&this.extraConfig.customer != undefined && this.extraConfig.customer) {
            /*
             * Download Sample file for make Payment Against Customer
             */
            storeageName = 'download_sample_make_payment_customer.csv';
            fileName = 'sample_make_payment_customer';
            ModuleName = "MakePaymentCustomer";
        } else if (this.moduleName=="Payment"&&this.extraConfig.vendor != undefined && this.extraConfig.vendor) {
            /*
             * Download Sample file for make Payment Against Vendor
             */
            storeageName = 'download_sample_make_payment_vendor.csv';
            fileName = 'sample_make_payment_vendor';
            ModuleName = "MakePaymentVendor";
        } else if (this.moduleName=="Payment"&&this.extraConfig.GL != undefined && this.extraConfig.GL) {
            /*
             * Download Sample file for make Payment Against GL
             */
            storeageName = 'download_sample_make_payment_gl.csv';
            fileName = 'sample_make_payment_gl';
            ModuleName = "MakePaymentGL";
        }else if (this.moduleName=="GSTTerm") {
            /*
             * ERP-32829 
             * Download Sample file for make Payment Against GL
             */
            storeageName = 'download_GSTSetup.csv';
            fileName = 'GSTSetup';            
            ModuleName = this.extraConfig.isSales==true?"GSTOutputTerm":"GSTInputTerm";                        
        } else if (this.moduleName == Wtf.Cash_Purchase_List && this.extraConfig.isExpenseInvoiceImport != undefined && this.extraConfig.isExpenseInvoiceImport) {
            if (this.extraConfig.incash != undefined && this.extraConfig.incash != "" && this.extraConfig.incash) {
                //Expense Cash Purchase sample file parameters
                storeageName = 'download_sample_expense_cash_purchase_list.csv';
                fileName = 'sample_expense_cash_purchase_list';
                ModuleName = "Expense " + this.moduleName;
            } else {
                storeageName = 'download_sample_expense_purchase_list.csv';
                fileName = 'sample_expense_purchase_list';
                ModuleName = "Expense Purchase";
            }
        }else if (this.moduleName == Wtf.Cash_Purchase_List) {
            //Cash Purchase sample file parameters
            storeageName = 'download_sample_cash_purchase_list.csv';
            fileName = 'sample_cash_purchase_list';
            ModuleName = this.moduleName;
        }
    
        var downloadSampleFileLink=new Wtf.XTemplate(
            '<tpl>',
                WtfGlobal.getLocaleText("acc.field.DownloadSampleFile")+" <a class='tbar-link-text' href='#' onClick='javascript: downloadSampleFie(\""+storeageName+"\",\""+fileName+"\",\""+ModuleName+"\")'wtf:qtip=''>"+WtfGlobal.getLocaleText("acc.field.SampleFile")+"</a>",
            '</tpl>'
        );
    
        this.sampleLinkPanel = new Wtf.Panel({
            border: false,
            html: downloadSampleFileLink.apply()
        })

        this.ImportForm = new Wtf.FormPanel({
            width:'90%',
            method :'POST',
            scope: this,
            border:false,
            fileUpload : true,
            waitMsgTarget: true,
            labelWidth: 80,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:15px 0 0 60px;',
            layout: 'form',
            items:[
                this.browseField,
                this.conowner,
                this.datePreference,
                this.masterPreference,
                this.sampleLinkPanel
            ]
        });

        this.add({
                    region:'north',
                    height:70,
                    border : false,
                    bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                    html: getImportTopHtml((this.typeXLSFile?WtfGlobal.getLocaleText("acc.import.xls"):WtfGlobal.getLocaleText("acc.import.csv")), "<ul style='list-style-type:disc;padding-left:15px;'><li>"+(this.typeXLSFile?WtfGlobal.getLocaleText("acc.rem.142"):WtfGlobal.getLocaleText("acc.rem.143"))+"</li><li><b>"+WtfGlobal.getLocaleText("acc.field.Note")+"</b>: "+WtfGlobal.getLocaleText("acc.rem.92")+"</li></ul>","../../images/import.png", true, "5px 0px 0px 0px", "7px 0px 0px 10px")
                });

        this.add({
                    region:'center',
//                    layout:'fit',
                    border:false,
                    bodyStyle: 'background:#f1f1f1;font-size:10px;',
                    items:[
                        this.ImportForm,
                        new Wtf.Panel({
                            border: false,
                            bodyStyle: 'padding:5px;',
                            html: "<b>* "+WtfGlobal.getLocaleText("acc.import.msg15")+"</b> "
                        })]
                })

        this.conowner.on("change", function(){this.isPrefModified=true;},this);
        this.master0.on("change", function(){this.isPrefModified=true;},this);
        this.master1.on("change", function(){this.isPrefModified=true;},this);
        this.master2.on("change", function(){this.isPrefModified=true;},this);
        this.datePreference.on("change", function(){this.isPrefModified=true;},this);
    },

    uploadCSVFile : function(){
        var master = 0;
        if(this.master0.getValue()){
            master = 0;
        } else if(this.master1.getValue()){
            master = 1;
        } else if(this.master2.getValue()){
            master = 2;
        }
        if(!this.browseField.disabled){
            this.nextButton.disable();
            var parsedObject = document.getElementById('browseBttn').value;
            var extension =parsedObject.substr(parsedObject.lastIndexOf(".")+1);
            var patt1 = new RegExp("csv","i");
            var delimiterType=this.conowner.getValue();
            if(delimiterType==undefined || delimiterType==""){
                WtfImportMsgBox(47);
                return;
            }
            if(patt1.test(extension)) {
                if(this.extraConfig == undefined) {
                    this.extraConfig={};
                }
                this.extraConfig['delimiterType'] = this.conowner.getValue();
                this.extraConfig['masterPreference'] = master;
                this.extraConfig['dateFormat'] = this.datePreference.getValue();

                this.ImportForm.form.submit({
                    url:"ImportRecords/importRecords.do?type="+this.moduleName+"&do=getMapCSV&fromdocument=true&delimiterType="+delimiterType,
                    waitMsg :WtfGlobal.getLocaleText("acc.rem.167"),  //'Uploading File...',
                    scope:this,
                    success: function (action, res) {
                        this.nextButton.enable();
                        var resobj = eval( "(" + res.response.responseText.trim() + ")" );
                        if(resobj.data != "") {
                            this.mappingCSVInterface(resobj.Header, resobj, this, delimiterType, this.extraParams, this.extraConfig, this.obj, this.moduleName, this.store);
                        }
                        this.browseField.disable();
                        this.conowner.disable();
                    },
                    failure:function(action, res) {
                        this.nextButton.enable();
                        var resobj = eval( "(" + res.response.responseText.trim() + ")" );
                        WtfImportMsgBox([WtfGlobal.getLocaleText("acc.common.error"),resobj.msg], 1);
                    }

                });
            } else {
                WtfImportMsgBox(48);
                this.nextButton.enable();
            }
        } else {
            var mappingWindow = Wtf.getCmp("csvDocumentMappingInterface");
            if(mappingWindow.extraConfig == undefined) {
                mappingWindow.extraConfig={};
            }
            mappingWindow.extraConfig['delimiterType'] = this.conowner.getValue();
            mappingWindow.extraConfig['masterPreference'] = master;
            mappingWindow.extraConfig['dateFormat'] = this.datePreference.getValue();
            mappingWindow.show();
        }
    },

    mappingCSVInterface: function(Header, res, impWin1, delimiterType, extraParams, extraConfig, obj, moduleName, store) {
       obj.filename=res.FileName;

       if(extraConfig == undefined) {
            extraConfig={};
       }
       extraConfig['delimiterType'] = delimiterType;
       extraConfig['filename'] = res.FileName;

        this.mappingParams = {};
        this.mappingParams.csvheaders = Header;
        this.mappingParams.typeXLSFile = false;
        this.mappingParams.delimiterType = delimiterType;
        this.mappingParams.moduleName = moduleName;
        this.mappingParams.modName = moduleName;
        this.mappingParams.store = store;
        this.mappingParams.cm = obj.gridcm;
        this.mappingParams.extraParams = extraParams;
        this.mappingParams.extraConfig = extraConfig;

        Wtf.callDocumentMappingInterface(this.mappingParams, this);
        this.hide();
    },

    uploadXLSFile: function(){
        var master = 0;
        if(this.master0.getValue()){
            master = 0;
        } else if(this.master1.getValue()){
            master = 1;
        } else if(this.master2.getValue()){
            master = 2;
        }
        if(!this.browseField.disabled){
            this.nextButton.disable();
            var parsedObject = document.getElementById('browseBttn').value;
            var extension =parsedObject.substr(parsedObject.lastIndexOf(".")+1);
            var patt1 = new RegExp("xls","i");
            if(patt1.test(extension)) {
                if(this.extraConfig == undefined) {
                    this.extraConfig={};
                }
                this.extraConfig['masterPreference'] = master;

                this.ImportForm.getForm().submit({
                    url:"ImportRecords/fileUploadXLS.do",
                    waitMsg:WtfGlobal.getLocaleText("acc.rem.167"),  //'Uploading File...',
                    scope:this,
                    success:function(f,a){
                        this.browseField.disable();
                        this.nextButton.enable();
                        this.genUploadResponse(a.request,true,a.response,this.moduleName,this.store, this.obj, this.extraParams, this.extraConfig)
                    },
                    failure:function(f,a){
                        this.nextButton.enable();
                        this.genUploadResponse(a.request,false,a.response,this.moduleName,this.store, this.obj, this.extraParams, this.extraConfig)
                    }
                });
            } else {
                WtfImportMsgBox(48);
                this.nextButton.enable();
            }
        } else {
            var xlsPreviewWindow = Wtf.getCmp("importDocumentxls");
            if(xlsPreviewWindow.extraConfig == undefined) {
                xlsPreviewWindow.extraConfig={};
            }
            xlsPreviewWindow.extraConfig['masterPreference'] = master;
            xlsPreviewWindow.show();
        }
    },

    genUploadResponse: function(req,succeed,res,moduleName,store,obj,extraParams, extraConfig){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        var response=eval('('+res.responseText+')');
        if(succeed){
            succeed=response.lsuccess;
            if(succeed){
                var xlsPreviewWindow = Wtf.getCmp("importDocumentxls");
                if(xlsPreviewWindow) {
                    if(xlsPreviewWindow.isVisible()){
                        xlsPreviewWindow.close();
                    } else {
                        xlsPreviewWindow.destroy();
                    }
                }

                this.win=new Wtf.SheetDocumentViewer1({
                    title: WtfGlobal.getLocaleText("acc.import.availSheet"),  //'Available Sheets',
                    iconCls: 'importIcon',
                    autoScroll:true,
                    plain:true,
                    modal:true,
                    data:response,
                    layout:'border',
                    prevWindow: Wtf.getCmp("importDocumentwindow"),
                    moduleName:moduleName,
                    store:store,
                    obj:obj,
                    extraParams: extraParams,
                    extraConfig: extraConfig
                });
                this.win.show();
                Wtf.getCmp("importDocumentwindow").hide();
            }else{
                msg=response.msg;
                Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.field.FileUpload"),msg);
            }
        }
    }
});


/*----------------------------------------------------------------------------
--------------------------- Imported Files Log  Grid -------------------------
------------------------------------------------------------------------------*/
//--------- function to show tab ----------//
function callImportFilesLog(){
    var panel=Wtf.getCmp('importFilesLog');
    if(panel==null)
    {
        panel = new Wtf.ImportedFilesLog({
            title:WtfGlobal.getLocaleText("acc.importLog.tabTitle"),  //'Imported Files Log',
            tooltip:WtfGlobal.getLocaleText("acc.importLog.tabTip"),
            tabTip:WtfGlobal.getLocaleText("acc.import.status1"),  //'View your imported files in the system from here.',
            closable:true,
            layout: "fit",
            border:false,
            iconCls: 'pwnd projectTabIcon',
            id:"importFilesLog"
        });
        mainPanel.add(panel);
    } else {
        panel.dataStore.reload(); //Reload log if already opened
    }
    mainPanel.setActiveTab(panel);
    mainPanel.doLayout();
}

//--------- Component Code ----------//
Wtf.ImportedFilesLog = function(config) {
    Wtf.apply(this, config);
    Wtf.ImportedFilesLog.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.ImportedFilesLog, Wtf.Panel, {
    onRender: function(config){
        this.startDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.from"),  //'From',
            name:'stdate',
            format:WtfGlobal.getOnlyDateFormat(),
            readOnly:true,
            value: WtfGlobal.getDates(true)
        });

        this.endDate=new Wtf.ExDateFieldQtip({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.to"),  //'To',
            format:WtfGlobal.getOnlyDateFormat(),
            readOnly:true,
            name:'enddate',
            value: WtfGlobal.getDates(false)
        });

        this.fetchButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.fetch"),  //"Fetch",
            tooltip: WtfGlobal.getLocaleText("acc.common.fetchTT"),
            scope:this,
            iconCls:'accountingbase fetch',
            handler:function(){
                if(this.startDate.getValue()>this.endDate.getValue()){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.fxexposure.datechk")], 3); // "From Date can not be greater than To Date."
                    return;
                }
                this.initialLoad();
           }
        }),
        
        this.importButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.import"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.import"),
            iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
            hidden: !Wtf.account.companyAccountPref.showimport,
            handler:function(){
                this.win = new Wtf.ImportFileWindow({
                    title: WtfGlobal.getLocaleText("acc.common.import"),
                    iconCls: 'importIcon',
                    autoScroll:true,
                    modal:true,
                    layout:'border',
                    width:600,
                    height:280
                });
                this.win.show();
           }
        });

        this.columnRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'filename'},
            {name: 'storename'},
            {name: 'failurename'},
            {name: 'log'},
            {name: 'imported'},
            {name: 'total'},
            {name: 'rejected'},
            {name: 'type'},
            {name: 'importon', type:"date"},
            {name: 'module'},
            {name: 'importedby'},
            {name: 'company'},
            {name: 'failurefiletype'}
        ]);

        this.dataStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            }, this.columnRec),
            url: "ImportRecords/getImportLog.do"
        });


        this.dataStore.on("beforeload", function() {

            this.dataStore.baseParams = {
                limit: this.pag.pageSize,
                ss: this.localSearch.getValue(),
                startdate: this.getDates(true).format("Y-m-d H:i:s"),
                enddate: this.getDates(false).format("Y-m-d H:i:s")

            }
        }, this); 
        this.columnCm = new Wtf.grid.ColumnModel([
        new Wtf.grid.RowNumberer({
        	width:40
        }),
        {
            header:WtfGlobal.getLocaleText("acc.importLog.module"),  // "Module",
            dataIndex: "module"
        },{
            header: WtfGlobal.getLocaleText("acc.importLog.fileName"),  //"File Name",
            sortable:true,
            dataIndex: "filename"
        },{
            header: WtfGlobal.getLocaleText("acc.importLog.fileType"),  //"File Type",
            dataIndex: "type",
            renderer: function(val){
                return Wtf.util.Format.capitalize(val);
            }
        },{
            header:WtfGlobal.getLocaleText("acc.importLog.importedBy"),  //"Imported By",
            sortable:true,
            dataIndex:"importedby"
        },{
            header:WtfGlobal.getLocaleText("acc.importLog.importedOn"),  //"Imported On",
            sortable:true,
            dataIndex:"importon",
            renderer : function(val){
                return val.format("Y-m-d H:i:s");
            }
        },{
            header: WtfGlobal.getLocaleText("acc.importLog.totalRecords"),  //"Total Records",
            align: "right",
            dataIndex: "total"
        },{
            header: WtfGlobal.getLocaleText("acc.importLog.importRecords"),  //"Imported Records",
            align: "right",
            dataIndex: "imported"
        },{
            header: WtfGlobal.getLocaleText("acc.importLog.rejectRecords"),  //"Rejected Records",
            align: "right",
            dataIndex: "rejected"
        },{
            header:WtfGlobal.getLocaleText("acc.importLog.importLog"),  //"Import Log",
            sortable:true,
            dataIndex:"log",
            renderer : function(val){
                return "<div wtf:qtip=\""+val+"\">"+val+"</div>";
            }
        },{
            header:WtfGlobal.getLocaleText("acc.importLog.originalFile"),  //"Original File",
            sortable:true,
            dataIndex:"imported",
            align: "center",
            renderer : function(val, a,b,c,d,e,f){
                if(e.getAt(c).data.filename!=''){
                    return "<div class=\"pwnd downloadIcon original\" wtf:qtip=\"Download Original File\" style=\"height:16px;margin-left:60px\">&nbsp;</div>";
                }else{
                    return '';
                }
            }
        },{
            header:WtfGlobal.getLocaleText("acc.importLog.rejectedFile"),  //"Rejected File",
            sortable:true,
            align: "center",
            dataIndex:"rejected",
            renderer : function(val){
                if(val>0){
                    return "<div class=\"pwnd downloadIcon rejected\" wtf:qtip=\"Download Rejected File\" style=\"height:16px;margin-left:60px\">&nbsp;</div>";
                }
                return "";
            }
        }
        ]);
         this.localSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.importLog.searchText"),//'Search by Product Name',
            id:'dfd',
            width: 130,
            field: 'filename',
            Store:this.dataStore
        });
        this.sm = new Wtf.grid.RowSelectionModel({singleSelect: true});
        this.grid = new Wtf.grid.GridPanel({
            store: this.dataStore,
            sm:this.sm,
            cm: this.columnCm,
            border : false,
            loadMask : true,
            view:new Wtf.grid.GridView({
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.importLog.emptyText")  //"No files were imported between selected dates."
            }),
            tbar : [this.localSearch,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,"-",WtfGlobal.getLocaleText("acc.common.to"),this.endDate,"-",this.fetchButton,"-",this.importButton],
            bbar: this.pag=new Wtf.PagingSearchToolbar({
                pageSize: 30,
                border : false,
                id : "paggintoolbar"+this.id,
                store: this.dataStore,
                searchField: this.localSearch,
                plugins : this.pPageSizeObj = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                }),
                autoWidth : true,
                displayInfo:true//,
            })
        });


        this.grid.on('rowclick',this.handleRowClick,this);

        this.sm.on("selectionchange",function(sm){
        },this);

        this.wrapperBody = new Wtf.Panel({
            border: false,
            layout: "fit",
           // tbar : [this.localSearch,WtfGlobal.getLocaleText("acc.common.from"),this.startDate,"-",WtfGlobal.getLocaleText("acc.common.to"),this.endDate,"-",this.fetchButton,"-",this.importButton],
            items : this.grid
        });

        this.initialLoad();
        this.add(this.wrapperBody);
        Wtf.csvFileDocumentMappingInterface.superclass.onRender.call(this, config);
    },

    handleRowClick:function(grid,rowindex,e){
        if(e.getTarget(".original")){
            var rec = this.grid.getSelectionModel().getSelections()[0].data;
            Wtf.get('downloadframe').dom.src = 'ImportRecords/downloadFileData.do?storagename='+rec.storename+'&filename='+rec.filename+'&type='+rec.type;
        } else if(e.getTarget(".rejected")){
            rec = this.grid.getSelectionModel().getSelections()[0].data;
            var filename = rec.filename;
            var storagename = rec.failurename;
            //            var type = rec.type;
//            if (rec.module == "Product"||rec.module == Wtf.Currency_Exchange || rec.module=="Customer" || rec.module=="Vendor") {
                var type = rec.failurefiletype;
                if(type=="" || type==undefined || type==null){
                   type="csv"; 
                }
                filename = filename.substr(0, filename.lastIndexOf(".")) + "."+type.toLowerCase();
                storagename = storagename.substr(0, storagename.lastIndexOf(".")) + "."+type.toLowerCase();
//            } else if (type=="xls" || type=="XLS") {
//                type = "csv";
//                filename = filename.substr(0, filename.lastIndexOf(".")) + ".csv";
//                storagename = storagename.substr(0, storagename.lastIndexOf(".")) + ".csv";
//            }
            Wtf.get('downloadframe').dom.src = 'ImportRecords/downloadFileData.do?storagename='+storagename+'&filename=Failure_'+filename+'&type='+type;
        }
    },

    initialLoad: function(){
        this.dataStore.baseParams = {
            startdate: this.getDates(true).format("Y-m-d H:i:s"),
            enddate: this.getDates(false).format("Y-m-d H:i:s")
        }
        this.dataStore.load({params : {
            start: 0,
            limit: this.pag.pageSize
        }});
    },

    getDefaultDates:function(start){
        var d=Wtf.serverDate;
        if(start){
            d = new Date(d.getFullYear(),d.getMonth(),1);
        }
        return d;
    },

    getDates:function(start){
        var d=Wtf.serverDate;
        if(start){
            d = this.startDate.getValue();
            d = new Date(d.getFullYear(),d.getMonth(),d.getDate(),0,0,0);
        } else {
            d = this.endDate.getValue();
            d = new Date(d.getFullYear(),d.getMonth(),d.getDate(),23,59,59);
        }
        return d;
    }
});





Wtf.SheetDocumentViewer1=function(config){
    Wtf.SheetDocumentViewer1.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.SheetDocumentViewer1,Wtf.Window,{
    id: 'importDocumentxls',
    closable: false,
    width: 750,
    height: 600,
    initComponent:function(config){
        Wtf.SheetDocumentViewer1.superclass.initComponent.call(this,config);
        this.prevButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.import.changePref"),  //"Change Preferences",
            scope: this,
            minWidth: 80,
            handler: function(){
                if(this.prevWindow){
                    this.prevWindow.show();
                }
                this.hide();
            }
        });
        this.nextButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.next")+" >>",
            scope: this,
            minWidth: 80,
            disabled: true,
            id: "nextButton"+this.id,
            handler: function(){
                var mappingWindow = Wtf.getCmp("csvDocumentMappingInterface");
                if(!mappingWindow) { //For first time dump data
                    this.dumpFileData();
                } else { //For second time check any sheet changes to dump data
                    var sheetRec= this.shgrid.getSelectionModel().getSelected();
                    var rowRec= this.shdgrid.getSelectionModel().getSelected();
                    var currSheetIndex = sheetRec.get('index');
                    var currRowIndex = this.shdgrid.getStore().indexOf(rowRec);

                    var prevSheetIndex = mappingWindow.index;
                    var prevRowIndex = mappingWindow.extraConfig.startindex;
                    if(currSheetIndex!=prevSheetIndex || currRowIndex!=prevRowIndex){
                        this.dumpFileData();
                    } else {
                        this.getMappingInterface();
                    }
                }
            }
        });

        this.cancelButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //"Cancel",
            scope: this,
            minWidth: 80,
            handler: function(){
                closeDocumentImportWindow();
            }
        });
        this.buttons = [this.prevButton, this.nextButton, this.cancelButton];
    },

    onRender:function(config){
        Wtf.SheetDocumentViewer1.superclass.onRender.call(this,config);
        this.xlsfilename=this.data.file;
        this.onlyfilename=this.data.filename;
        this.sheetIndex=0;
        this.rowIndex=0;
        this.totalColumns=0;
        for(var x=0;x<this.data.data.length;x++){
            this.data.data[x].srow='1';
        }
        var rec=new Wtf.data.Record.create([
            {name:'name'},{name:'index'},{name:'srow'}
        ])
        var store=new Wtf.data.Store({
            reader: new Wtf.data.JsonReader({
                root: "data"
            },rec),
            data:this.data
        });
        this.shgrid=new Wtf.grid.GridPanel({
            viewConfig:{
                forceFit:true
            },
            columns:[{
                header:WtfGlobal.getLocaleText("acc.import.sheetName"),  //'Sheet Name',
                dataIndex:'name'
            },{
                header:WtfGlobal.getLocaleText("acc.import.startingRow"),  //'Starting Row',
                dataIndex:'srow'
            }],
            store:store
        });

        //Select Default sheet at index 0
        this.shgrid.on("render", function(){
            if(this.shgrid.getStore().getCount()>0){
                this.shgrid.getSelectionModel().selectRow(0);
                this.shgrid.fireEvent("rowclick",this.shgrid,0);
            }
        }, this);

        this.shgrid.on('rowclick',this.showDetail,this);

        var shdrec=new Wtf.data.Record.create([
            {name:'name'}
        ])
        var shdstore=new Wtf.data.Store({
            reader: new Wtf.data.JsonReader({
                root: "fields"
            },shdrec)
        });
        this.shdgrid=new Wtf.grid.GridPanel({
            columns:[],
            store:shdstore
        });
        this.shdgrid.on('rowclick',this.updateStartRow,this);
        this.add({
            region:'north',
            height:70,
            border : false,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getImportTopHtml(WtfGlobal.getLocaleText("acc.import.availSheets"), WtfGlobal.getLocaleText("acc.import.step1")+"<br/> "+WtfGlobal.getLocaleText("acc.import.step2"),"../../images/import.png",true, "0px", "7px 0px 0px 10px")
        });
        this.add({
            region:'center',
            layout:'fit',
            autoScroll:true,
            items:this.shgrid
        });
        this.add({
            region:'south',
            height:320,
            layout:'fit',
            autoScroll:true,
            items:this.shdgrid

        });
    },

    updateStartRow:function(g,i,e){
        if(this.shdgrid.getSelectionModel().getCount()==1){ //Perform on selection of any 1 row.
            var rec = this.shgrid.getSelectionModel().getSelected();
            rec.set('srow',i+1);
            var dt = this.shdgrid.getSelectionModel().getSelected();
            var fieldKeys = dt.fields.keys
            var tmpArray=[];

            for(var i=0 ; i < fieldKeys.length ; i++){
                if(dt.get(dt.fields.keys[i]).trim()!=""){
                    var rec1 = {};
                    var j =i-1;
                    rec1.header = dt.get(dt.fields.keys[i]);
                    rec1.index  = j;
                    if(i>0){
                        tmpArray.push(rec1);
                    }
                }
            }
            this.Header = tmpArray;
        }
    },

    genUploadResponse12:function(req,succeed,res){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        var response=eval('('+res.responseText+')');
        if(succeed){
            msg=response.msg;
            succeed=response.lsuccess;
            this.Header= response.Header;
            this.xlsParserResponse = response;
            if(succeed){
                this.cursheet=response.index;
                var cm=this.createColumnModel1(response.maxcol);
                var store=this.createStore1(response,cm);
                this.shdgrid.reconfigure(store,cm);
                var rowno=this.shgrid.getStore().getAt(this.shgrid.getStore().find('index',this.cursheet)).get('srow');
                if(rowno)
                    this.shdgrid.getSelectionModel().selectRow(rowno-1);
                this.sheetIndex= response.index;
                this.totalColumns= response.maxcol;
                this.rowIndex= response.startrow;

                if(response.maxcol==0 || response.maxrow==0){ // Disable next button of no. of row=0 or columns=0
                    this.nextButton.disable();
                } else {
                    this.nextButton.enable();
                }
            }else{
                Wtf.Msg.alert('File Import',msg);
            }
        }
        this.shdgrid.enable();
    },

    createColumnModel1:function(cols){
        var fields=[new Wtf.grid.RowNumberer()];
        for(var i=1;i<=cols;i++){
            var temp=i;
            var colHeader="";
            while(temp>0){
                temp--;
                colHeader=String.fromCharCode(Math.floor(temp%26)+"A".charCodeAt(0))+colHeader;
                temp=Math.floor(temp/26);
            }
            fields.push({header:colHeader,dataIndex:colHeader});
        }
        return new Wtf.grid.ColumnModel(fields);
    },

    createStore1:function(obj,cm){
        var fields=[];
        for(var x=0;x<cm.getColumnCount();x++){
            fields.push({name:cm.getDataIndex(x)});
        }

        var rec=new Wtf.data.Record.create(fields);
        var store = new Wtf.data.Store({
            reader: new Wtf.data.JsonReader({
                root: "data"
            },rec),
            data:obj
        });

        return store;
    },


    showDetail:function(g,i,e){
        if(this.shgrid.getSelectionModel().getCount()==1){
            Wtf.getCmp("nextButton"+this.id).enable();
            var rec=this.shgrid.getStore().getAt(i);
            if(this.cursheet&&this.cursheet==rec.get('index'))return;
            this.shdgrid.disable();
            this.sheetIndex = rec.get('index');
            Wtf.Ajax.request({
                method: 'POST',
                url: "ImportRecords/importRecords.do?do=getXLSData",
    //            url: 'XLSDataExtractor',
                params:{
                    filename:this.xlsfilename,
                    fromdocument:true,
                    onlyfilename:this.onlyfilename,
                    index:this.sheetIndex
                },
                scope: this,
                success: function(res, req){
                    this.genUploadResponse12(req, true, res);
                },
                failure: function(res, req){
                    this.genUploadResponse12(req, false, res);
                }
            });
        } else {
            Wtf.getCmp("nextButton"+this.id).disable();
        }
    },

    dumpFileData: function(){
        //Create table to dump .xls file data
        var rec1=this.shdgrid.getSelectionModel().getSelected();
        this.rowIndex = this.shdgrid.getStore().indexOf(rec1);
        Wtf.Ajax.timeout=900000;
        Wtf.Ajax.request({
            method: 'POST',
            url: "ImportRecords/importRecords.do?do=dumpXLS",
            params:{
                filename: this.xlsfilename,
                onlyfilename: this.onlyfilename,
                index: this.sheetIndex,
                rowIndex: this.rowIndex,
                totalColumns: this.totalColumns
            },
            scope: this,
            success: function(res, req){
                this.getMappingInterface();
                Wtf.Ajax.timeout=30000;
            },
            failure: function(res, req){
                this.getMappingInterface();
                Wtf.Ajax.timeout=30000;
            }
        });
    },

    getMappingInterface:function(g,i,e){
       var rec=this.shgrid.getSelectionModel().getSelected();
       if(this.extraConfig == undefined) {
            this.extraConfig={};
       }
       this.extraConfig['startindex'] = this.rowIndex;

        this.mappingParams = {};
        this.mappingParams.csvheaders = this.Header;
        this.mappingParams.modName = this.moduleName,
        this.mappingParams.moduleid = this.obj.moduleid,
        this.mappingParams.customColAddFlag = this.obj.customColAddFlag,
        this.mappingParams.typeXLSFile = true,
        this.mappingParams.delimiterType = "";
        this.mappingParams.index = rec.get('index');
        this.mappingParams.moduleName = this.moduleName;
        this.mappingParams.store = this.store;
        this.mappingParams.scopeobj = this.obj;
        this.mappingParams.cm = this.obj.EditorColumnArray;
        this.mappingParams.extraParams = this.extraParams;
        this.mappingParams.extraConfig = this.extraConfig;

       Wtf.callDocumentMappingInterface(this.mappingParams, Wtf.getCmp("importDocumentxls"));
       Wtf.getCmp("importDocumentxls").hide();
    }
});

Wtf.ImportFileWindow=function(config){
    Wtf.ImportFileWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.ImportFileWindow, Wtf.Window,{
    id: 'importFileWindow',
    layout: "border",
    closable: true,
    resizable: false,
    modal: true,
    iconCls: 'importIcon',
    initComponent:function(config){
        Wtf.ImportFileWindow.superclass.initComponent.call(this,config);

        this.importFileButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.import"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.import"),
            iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
            minWidth: 80,
            handler: function(){
                this.uploadFile();
            }
        });
        this.cancelButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //"Cancel",
            scope: this,
            minWidth: 80,
            handler: function(){
                this.close();
            }
        });
        this.buttons = [this.importFileButton, this.cancelButton];
    },

    onRender:function(config){
        Wtf.ImportFileWindow.superclass.onRender.call(this,config);

        var importModuleStore = new Wtf.data.SimpleStore({
            fields: ['importmoduleid','importmodule'],
            data : [
                [1,"Customer Billing"],
                [11,'Customer Shipping'],
                [2,'Product Master'],
                [3,'License Master'],
                [4,'License Master Supplementary 1'],
                [41,'License Master Supplementary 2'],
                [5,'Stock IN (WH-DE)'],
                [51,'Stock IN (WH-SS)'],
                [52,'Stock IN (WH-KS)'],
                [53,'Stock IN (WH-ES)']
            ]
        });

        this.importModuleCombo = new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.importLog.module")+"*",
            store: importModuleStore,
            valueField:'importmoduleid',
            displayField:'importmodule',
            mode: 'local',
            triggerAction: 'all',
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectaModule"),
            selectOnFocus:true,
            allowBlank:false,
            editable:false,
            width: 210,
            forceSelection: true
       });

       this.browseField = new Wtf.form.TextField({
            id:'browseFileBttn',
            border:false,
            inputType:'file',
            fieldLabel:WtfGlobal.getLocaleText("acc.import.filename")+"*",
            name: 'test'
        });

        this.ImportForm = new Wtf.FormPanel({
            width:'90%',
            method :'POST',
            scope: this,
            border:false,
            fileUpload : true,
            waitMsgTarget: true,
            labelWidth: 80,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:15px 0 0 60px;',
            layout: 'form',
            items:[
                this.importModuleCombo,
                this.browseField
            ]
        });

        this.add({
            region:'north',
            height:70,
            border : false,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getImportTopHtml(WtfGlobal.getLocaleText("acc.import.impData"), "<ul style='list-style-type:disc;padding-left:15px;'><li>"+(WtfGlobal.getLocaleText("acc.rem.143"))+"</li><li><b>Note</b>: "+WtfGlobal.getLocaleText("acc.rem.92")+"</li></ul>","../../images/import.png", true, "5px 0px 0px 0px", "7px 0px 0px 10px")
        });

        this.add({
            region:'center',
            border:false,
            bodyStyle: 'background:#f1f1f1;font-size:10px;',
            items:[
                this.ImportForm,
                new Wtf.Panel({
                    border: false,
                    bodyStyle: 'padding:5px;',
                    html: "<b>* "+WtfGlobal.getLocaleText("acc.import.msg15")+"</b> "
                })
            ]
        });

    },
    
    uploadFile : function(){
        if(!this.browseField.disabled){
            var parsedObject = document.getElementById('browseFileBttn').value;
            var extension =parsedObject.substr(parsedObject.lastIndexOf(".")+1);
            var patt1 = new RegExp("csv","i");
            var patt2 = new RegExp("txt","i");
            if(patt1.test(extension) || patt2.test(extension)) {
                if(this.ImportForm.form.isValid()){
                    this.importFileButton.disable();
                    this.ImportForm.form.submit({
                        url:"olympusdataimport/fileUpload.do?cdomain="+subdomain+"&type="+this.importModuleCombo.getValue(),
                        waitMsg :WtfGlobal.getLocaleText("acc.rem.167"),
                        scope:this,
                        success: function (action, res) {
                            var resobj = eval( "(" + res.response.responseText.trim() + ")" );
                            if(resobj.success) {
                                this.importFileButton.enable();
                                this.close();
                                var message = "<div>"+
                                    "<h2 class=\"blue-h2\">"+WtfGlobal.getLocaleText("acc.import.backProgress1")+"</h2>"+
                                    "<br/>"+
                                    "<div class=\"right-bullets\"><span><b>1</b></span>"+WtfGlobal.getLocaleText("acc.import.backProgress2")+"</div>"+
                                    "<div class=\"right-bullets\"><span><b>2</b></span>"+WtfGlobal.getLocaleText("acc.import.backProgress3")+"</div>"+
                                    "<div class=\"right-bullets\"><span><b>3</b></span>"+WtfGlobal.getLocaleText("acc.import.statusmsg")+"</div>"+
                                "</div>";
                                WtfImportMsgBox([WtfGlobal.getLocaleText("acc.import.status"),message], 0);
                                Wtf.Ajax.requestEx({
                                    url:"olympusdataimport/importOlympusFiles.do",
                                    params: {
                                        cdomain: subdomain,
                                        type: this.importModuleCombo.getValue(),
                                        filepath: resobj.filepath
                                    }
                                },this,function(response){

                                });
                            }
                        },
                        failure:function(action, res) {
                            var resobj = eval( "(" + res.response.responseText.trim() + ")" );
                            WtfImportMsgBox([WtfGlobal.getLocaleText("acc.common.error"),resobj.msg], 1);
                        }

                    });
                }
            } else {
                WtfImportMsgBox(48);
                this.importFileButton.enable();
            }
        } else {

        }
    }
});

function WtfImportMsgBox(choice, type) {
    var strobj = [];
    switch (choice) {
        case 1:
            strobj = [WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.rem.78")];
            break;
        case 43:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.79")];
            break;
        case 44:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.80")];
            break;
        case 45:
            strobj = [WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.rem.81")];
            break;
        case 46:
            strobj = [WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.rem.82")];
            break;
        case 47:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.rem.83")];
            break;
        case 48:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.rem.84")];
            break;
        case 50:
            strobj = [WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.rem.85")];
            break;
        case 51:
            strobj = [WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.rem.86")];
            break;
        case 52:
            strobj = [WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.rem.87")];
            break;

        default:
            strobj = [choice[0], choice[1]];
            break;
    }

	var iconType = Wtf.MessageBox.INFO;

    if(type == 0)
        iconType = Wtf.MessageBox.INFO;
	if(type == 1)
	    iconType = Wtf.MessageBox.ERROR;
    else if(type == 2)
        iconType = Wtf.MessageBox.WARNING;
    else if(type == 3)
        iconType = Wtf.MessageBox.INFO;

    Wtf.MessageBox.show({
        title: strobj[0],
        msg: strobj[1],
        buttons: Wtf.MessageBox.OK,
//        animEl: 'mb9',
        icon: iconType
    });
}
Wtf.globalStorereload = function(extraConfig) {
    if(extraConfig.moduleName=="Account") {
//        Wtf.salesAccStore.reload();
    } else if(extraConfig.moduleName=="Customer") {
//        Wtf.customerAccStore.reload();
        if(extraConfig.masterPreference=="2"){
            Wtf.CustomerCategoryStore.reload();
        }
    } else if(extraConfig.moduleName=="Vendor") {
//        Wtf.vendorAccStore.reload();
        if(extraConfig.masterPreference=="2"){
            Wtf.VendorCategoryStore.reload();
        }
    }
}
function closeDocumentImportWindow(){
    destroyWindow("importDocumentwindow");
    destroyWindow("importDocumentxls");
    destroyWindow("csvDocumentMappingInterface");
    destroyWindow("IWDocumentValidationWindow");
}
function destroyWindow(windowId){
    var window = Wtf.getCmp(windowId);
    if(window) {
        if(window.isVisible()){
            window.close();
        } else {
            window.destroy();
        }
    }
}


function getImportTopHtml(text, body,img,isgrid, imagemargin, textmargin){
    if(isgrid===undefined)isgrid=false;
    if(imagemargin===undefined)imagemargin='0';
    if(textmargin===undefined)textmargin='15px 0px 0px 10px';
    if(img===undefined) {
        img = '../../images/import.png';
    }
     var str =  "<div style = 'width:100%;height:100%;position:relative;float:left;'>"
                    +"<div style='float:left;height:100%;width:auto;position:relative;margin:"+imagemargin+";'>"
                    +"<img src = "+img+" style='height:52px;margin:5px;width:40px;'></img>"
                    +"</div>"
                    +"<div style='float:left;height:100%;width:90%;position:relative;'>"
                    +"<div style='font-size:12px;font-style:bold;float:left;margin:"+textmargin+";width:100%;position:relative;'><b>"+text+"</b></div>"
                    +"<div style='font-size:10px;float:left;margin:5px 0px 10px 10px;width:100%;position:relative;'>"+body+"</div>"
                        +(isgrid?"":"<div class='medatory-msg'>* indicates required fields</div>")
                        +"</div>"
                    +"</div>" ;
     return str;
}

function getAccountMapTopHtml(text, body,img,isgrid, imagemargin, textmargin){
    if(isgrid===undefined)isgrid=false;
    if(imagemargin===undefined)imagemargin='0';
    if(textmargin===undefined)textmargin='15px 0px 0px 10px';
    if(img===undefined) {
        img = '../../images/import.png';
    }
     var str =  "<div style = 'width:100%;height:100%;position:relative;float:left;'>"
                    +"<div style='float:left;height:100%;width:auto;position:relative;margin:"+imagemargin+";'>"
                    +"<img src = "+img+" style='height:52px;margin:5px;width:40px;'></img>"
                    +"</div>"
                    +"<div style='float:left;height:100%;width:90%;position:relative;'>"
                    +"<div style='font-size:12px;font-style:bold;float:left;margin:"+textmargin+";width:100%;position:relative;'><b>"+text+"</b></div>"
                    +"<div style='font-size:10px;float:left;margin:5px 0px 10px 10px;width:100%;position:relative;'>"+body+"</div>"
                        +(isgrid?"":"<div class='medatory-msg'>* indicates required fields</div>")
                        +"</div>"
                    +"</div>" ;
     return str;
}

function replaceAll(txt, replace, with_this) {
    return txt.replace(new RegExp(replace, 'g'),with_this);
}

