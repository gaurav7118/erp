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

Wtf.importMenuArray = function(obj,moduleName,store,extraParams,extraConfig) {
    var archArray = [];
    var importButton = new Wtf.Action({
        text: WtfGlobal.getLocaleText("acc.import.csv"),  // "Import CSV File",
        id:'importcsvfile'+obj.id,
        tooltip:{
            text:WtfGlobal.getLocaleText("acc.import.csv")  //'Click to import CSV file.'
        },
        iconCls: 'pwnd importcsv',
        scope: obj,
        handler:function(){
            if(Wtf.getCmp("importwindow")==undefined){
                var impWin1 = Wtf.commonFileImportWindow(obj, moduleName, store, extraParams, extraConfig);
                impWin1.show();
            }    
        }
    });
    
    if (!(moduleName == Wtf.Currency_Exchange || moduleName == Wtf.Tax_Currency_Exchange || (extraConfig.isAssetsImportFromDoc !== undefined && extraConfig.isAssetsImportFromDoc == true))) {
        archArray.push(importButton);
    }
    
    //ERM-1207
    if (moduleName == Wtf.Bank_Reconciliation) {
        var importBankButton = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.import.bank"), // "Import From Bank",
            id: 'importbank4344' + obj.id,
            tooltip: {
                text: WtfGlobal.getLocaleText("acc.import.bankqtip")  //'Click to import records from bank'
            },
            iconCls: 'pwnd importcsv',
            scope: obj,
            handler: function(){
                this.matchBankRecords();
            }
        });
        archArray.push(importBankButton);
    }
    
    var importExcel=new Wtf.Action({
        text: WtfGlobal.getLocaleText("acc.import.excel"),  //"Import XLS/XLSX File",
        tooltip:{
            text:WtfGlobal.getLocaleText("acc.import.excel")  //'Click to import XLS/XLSX file.'
        },
        iconCls: 'pwnd importxls',
        scope: obj,
        handler:function(){
            if(Wtf.getCmp("importwindow")==undefined){
                 var impWin1 = Wtf.xlsCommonFileImportWindow(obj,moduleName,store,extraParams, extraConfig);
                impWin1.show();
             }
        }
    });
    
    if (!( moduleName == "Product Price List" || moduleName == "Unit of Measure" || moduleName == "Product opening stock" || moduleName == "Assembly Product"  || moduleName == "Product Category" || moduleName == "UOM Schema" ||moduleName == "Store Master"||moduleName=="Location Master"|| moduleName == "Price List - Band"|| moduleName == Wtf.Bank_Reconciliation || moduleName == "Role Management"  || extraConfig.isExcludeXLS || moduleName == Wtf.Inter_Store_Stock_Transfer)) { //    ERP-18462
        archArray.push(importExcel);
    }
    
    return archArray;
}

Wtf.importMenuButtonA = function(menuArray,obj,modName) {
    var tbarArchive=new Wtf.Toolbar.Button({
        iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
        id: (modName=="Product"? "importProduct9" : (modName=="Customer"?"importCustomer6"+obj.id:"importVendor7"+obj.id) ),
        tooltip: {
            text: modName=="Product"? "Import Product Details" : ( modName=="Customer"? WtfGlobal.getLocaleText("acc.rem.53"):(modName=="Bank Reconciliation"?WtfGlobal.getLocaleText("acc.rem.257"):(modName=="Accounts" ? WtfGlobal.getLocaleText("acc.accPref.ImportDetails") :WtfGlobal.getLocaleText("acc.rem.54"))))///"Import "+modName+" details"
        },
        scope: obj,
        text:WtfGlobal.getLocaleText("acc.common.import"),  //"Import",
        menu: menuArray
    });
    return tbarArchive;
}

/*-------------------- Function to show Mapping Windows -----------------*/

Wtf.callMappingInterface = function(mappingParams, prevWindow){
    var mappingWindow = Wtf.getCmp("csvMappingInterface");
    if(!mappingWindow) {
        this.mapCSV=new Wtf.csvFileMappingInterface({
            csvheaders:mappingParams.csvheaders,
            modName:mappingParams.modName,
            moduleid:mappingParams.moduleid,
            customColAddFlag:mappingParams.customColAddFlag,
            typeXLSFile:mappingParams.typeXLSFile,
            impWin1:prevWindow,
            delimiterType:mappingParams.delimiterType,
            index:mappingParams.index,
            moduleName:mappingParams.moduleName,
            updateExistingRecordFlag:mappingParams.updateExistingRecordFlag,
            createUomSchemaTypeFlag: mappingParams.createUomSchemaTypeFlag,
            deleteExistingUomSchemaFlag: mappingParams.deleteExistingUomSchemaFlag,
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
        if(mappingParams!=undefined && mappingParams.updateExistingRecordFlag!=undefined){
            mappingWindow.updateExistingRecordFlag= mappingParams.updateExistingRecordFlag;
        }
        mappingWindow.show();
    }
    
    if(dojoInitCount<=0){
        dojo.cometd.init("../../bind");
        dojoInitCount++;
    }

    if(mappingParams.typeXLSFile){ //.XLS File Import
        Wtf.getCmp("csvMappingInterface").on('importfn', function(mappingJSON, index, moduleName, store, scopeobj, extraParams, extraConfig, updateExistingRecordFlag, createUomSchemaTypeFlag, deleteExistingUomSchemaFlag) {
            if(extraConfig == undefined) {
                extraConfig={};
            }
            extraConfig['filepath'] = Wtf.getCmp("importxls").xlsfilename;
            extraConfig['onlyfilename'] = Wtf.getCmp("importxls").onlyfilename;
            extraConfig['filename'] = Wtf.getCmp("importxls").onlyfilename;
            extraConfig['sheetindex'] = index;
            extraConfig['moduleName'] = moduleName;
            extraConfig['modName'] = moduleName;
            extraConfig['extraParams'] = extraParams;
            extraConfig['resjson'] = mappingJSON;
            extraConfig['updateExistingRecordFlag'] = updateExistingRecordFlag;
            extraConfig['createUomSchemaTypeFlag'] = createUomSchemaTypeFlag;
            extraConfig['deleteExistingUomSchemaFlag'] = deleteExistingUomSchemaFlag;
            Wtf.ValidateFileRecords(true, moduleName, store, scopeobj, extraParams, extraConfig);
        },this);
    } else { //.CSV File Import
        Wtf.getCmp("csvMappingInterface").on('importfn', function(resMapping, delimiterType, moduleName, store, scopObj, extraParams, extraConfig, updateExistingRecordFlag, createUomSchemaTypeFlag, deleteExistingUomSchemaFlag) {
            if(extraConfig == undefined) {
                extraConfig={};
            }
            extraConfig['resjson'] = resMapping;
            extraConfig['modName'] = moduleName;
            extraConfig['delimiterType'] = delimiterType;
            extraConfig['extraParams'] = extraParams;
            extraConfig['updateExistingRecordFlag'] = updateExistingRecordFlag;
            extraConfig['createUomSchemaTypeFlag'] = createUomSchemaTypeFlag;
            extraConfig['deleteExistingUomSchemaFlag'] = deleteExistingUomSchemaFlag;
            Wtf.ValidateFileRecords(false, moduleName, store, scopObj, extraParams, extraConfig);
        },this);
    }
}
/**********************************************************************************************************
 *                              Mapping Window
 **********************************************************************************************************/
Wtf.csvFileMappingInterface = function(config) {
    Wtf.apply(this, config);
    Wtf.csvFileMappingInterface.superclass.constructor.call(this, config);
};

Wtf.extend(Wtf.csvFileMappingInterface, Wtf.Window, {
    iconCls : 'importIcon',
    width:750,
    height:570,
    modal:true,
    layout:"fit",
    id:'csvMappingInterface',
    closable:false,
    initComponent: function() {
        Wtf.csvFileMappingInterface.superclass.initComponent.call(this);
    },

    onRender: function(config){
        Wtf.csvFileMappingInterface.superclass.onRender.call(this, config);
        this.addEvents({
            'importfn':true,
            'customColAdd':true
        });
        this.checkbatchProperties = Wtf.account.companyAccountPref.isWarehouseCompulsory ?  "Warehouse" : "";
        this.checkbatchProperties += Wtf.account.companyAccountPref.isLocationCompulsory ? (this.checkbatchProperties.length > 0 ? "/Location": "Location"): "";
        this.checkbatchProperties += Wtf.account.companyAccountPref.isRowCompulsory? (this.checkbatchProperties.length > 0 ? "/Row": "Row"): "";
        this.checkbatchProperties += Wtf.account.companyAccountPref.isRackCompulsory? (this.checkbatchProperties.length > 0 ? "/Rack": "rack"): "";
        this.checkbatchProperties += Wtf.account.companyAccountPref.isBinCompulsory? (this.checkbatchProperties.length > 0 ? "/Bin": "Bin"): "";
        this.checkbatchProperties += Wtf.account.companyAccountPref.isBatchCompulsory ? (this.checkbatchProperties.length > 0 ? "/Batch": "Batch"): "";
        this.checkbatchProperties += Wtf.account.companyAccountPref.isSerialCompulsory? (this.checkbatchProperties.length > 0 ? "/Serial": "Serial"): "";
        
        this.title=this.typeXLSFile?WtfGlobal.getLocaleText(""):WtfGlobal.getLocaleText(""),  //"Map XLS headers" : "Map CSV headers";
        this.mappingJSON = "";
        this.masterItemFields = "";
        this.moduleRefFields = "";
        this.unMappedColumns = "";
        this.isMappingModified = "";
        //this.isBomlessFile = this.extraConfig.bomlessfile;

        this.columnRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'configid'},
            {name: 'maxLength'},
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
        
        if (this.moduleName == "Journal Entry") {
            this.columnDs = new Wtf.data.GroupingStore({
                reader: new Wtf.data.KwlJsonReader({
                    totalProperty: 'count',
                    root: "data"
                },this.columnRec),
                groupField: 'fieldtype',
                sortInfo: {field: "isMandatory", direction: "DESC"}, // Move all mandatory columns an top
                url: "ImportRecords/getColumnConfig.do"
            });
        } else {
            this.columnDs = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                    root: "data"
                },this.columnRec),
                sortInfo: {field: "isMandatory", direction:"DESC"},//Move all mandatory columns an top
                url: "ImportRecords/getColumnConfig.do"
            });
        }
        
        this.columnCm = new Wtf.grid.ColumnModel([
            {
                header: WtfGlobal.getLocaleText("acc.import.col"),  //"Columns",
                dataIndex: "columnName",
                sortable:true,
                hideable: false,
                renderer: function(a,b,c) {
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
                header: "",
                pdfwidth: 80,
                dataIndex: 'fieldtype',
                hidden: true,
                hideable: false
            }
        ]);
        
        this.quickSearchTF1 = new Wtf.KWLQuickSearchUseFilter({
            id : 'tableColumn'+this.id,
            width: 140,
            field : "columnName",
            emptyText:WtfGlobal.getLocaleText("acc.import.selCol")  //"Search Table Column "
        });
        
        var gridView = "";
        if (this.moduleName == "Journal Entry") {
            gridView = new Wtf.grid.GroupingView({
                forceFit: true
            });
        } else {
            gridView = new Wtf.grid.GridView({
                forceFit: true
            });
        }
        
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
            loadMask : true,
            view: gridView
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
            {name: 'maxLength'},
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

        var mappedColsCm = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(),{
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
            sortable:true,
            hideable: false,
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
        var mappedCsvHeaderCm = new Wtf.grid.ColumnModel([new Wtf.grid.RowNumberer(),{
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
                height:this.moduleName=="Accounts"?130:80,                      //ERP-20762
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                items:[{
                    xtype:"panel",
                    border:false,
                    height:this.moduleName=="Accounts"?120:70,
                    html:this.moduleName=="Accounts"?getImportTopHtml(WtfGlobal.getLocaleText("acc.rem.78"),"<ul style='list-style-type:disc;padding-left:15px;'><li>"+WtfGlobal.getLocaleText("acc.rem.145")+"</li><li>"+ WtfGlobal.getLocaleText("acc.rem.144")+"</li><li>"+WtfGlobal.getLocaleText("acc.rem.255")+"</li></ul>","../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px"):getImportTopHtml(WtfGlobal.getLocaleText("acc.rem.78"),"<ul style='list-style-type:disc;padding-left:15px;'><li>"+WtfGlobal.getLocaleText("acc.rem.145")+"</li><li>"+ WtfGlobal.getLocaleText("acc.rem.144")+"</li></ul>","../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
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
                hidden:this.modName=="Role Management",
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
                                    this.fireEvent('importfn',this.mappingJSON,this.index,this.moduleName,this.store,this.scopeobj,this.extraParams, this.extraConfig,this.updateExistingRecordFlag,this.createUomSchemaTypeFlag,this.deleteExistingUomSchemaFlag);
                                }else {
                                    this.fireEvent('importfn',this.mappingJSON, this.delimiterType, this.moduleName, this.store, this.scopeobj, this.extraParams, this.extraConfig,this.updateExistingRecordFlag,this.createUomSchemaTypeFlag,this.deleteExistingUomSchemaFlag);
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
                    closeImportWindow();
                },
                scope: this
            }]
        });

        this.on("show", function(){//Reload csv and table column grids
            if(!this.headerConfig || this.headerConfig != this.csvheaders){ // Check for new mapping updates
                this.headerConfig = this.csvheaders;
                this.mappedColsDs.removeAll();
                this.mappedCsvHeaderDs.removeAll();
                this.loadHeaderData();
                var fetchCustomFields = (this.modName=="Product" || this.modName=="Assembly Product" || this.modName=="Accounts" || this.moduleName=="Journal Entry" || this.modName=="Customer" || this.modName=="Vendor" || this.modName=="Fixed Asset Group" ||  this.modName=="Opening Fixed Asset Documents" || this.modName=="Opening Sales Invoice" || this.modName=="Opening Customer Credit Note" || this.modName=="Opening Customer Debit Note" || this.modName=="Opening Receipt" || this.modName=="Opening Purchase Invoice" || this.modName=="Opening Payment" || this.modName=="Opening Vendor Credit Note" || this.modName=="Opening Vendor Debit Note" || this.modName==Wtf.OpeningModuleName.openingCustomerSalesOrder || this.modName==Wtf.OpeningModuleName.openingVendorPurchaseOrder) ? true : false; // fetach custom fields for Product and Account Only
                if(this.columnDs.getCount()>0){
                    this.columnDs.loadData(this.columnDs.reader.jsonData);
                } else {
                    var paramObj={module : this.modName,fetchCustomFields:fetchCustomFields,bomlessfile:(this.extraConfig.bomlessfile!=undefined ? this.extraConfig.bomlessfile : false),updateExistingRecordFlag : this.updateExistingRecordFlag};
                     /*
                      * If SO/PO import Opening transaction then put below additional parameters
                     */
                    if (this.extraConfig.isOpeningOrder !== undefined && this.extraConfig.isOpeningOrder !== '' && this.extraConfig.isOpeningOrder) {
                      paramObj.isdocumentimportFlag=true;  
                      paramObj.subModuleFlag=0;  
                    }
                    if(this.extraConfig !== undefined && this.extraConfig.isAssetsImportFromDoc !== undefined && this.extraConfig.isAssetsImportFromDoc){
                        paramObj.isAssetsImportFromDoc=this.extraConfig.isAssetsImportFromDoc;
                    }
                    this.columnDs.load({params :paramObj});
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
            function arrangeMappingNumber(grid, currentRowIndex) {                // use currentRow as no. from which you want to change numbering
                var plannerView = grid.getView();                      // get Grid View
                var length = grid.getStore().getCount();              // get store count or no. of records upto which you want to change numberer
                for (var i = currentRowIndex; i < length; i++)
                    plannerView.getCell(i, 0).firstChild.innerHTML = i + 1;
            }
            
            function unMapRec(atIndex){
                var headerRec = mappedHeaderStore.getAt(atIndex);
                if(headerRec!==undefined){
                    mappedHeaderStore.remove(headerRec);
                    if(mappedHeaderStore.find("index",headerRec.data.index) == -1){
                        headerStore.getAt(headerStore.find("index",headerRec.data.index)).set('isMapped',false);
                        headerGrid.getView().refresh();
                    }
                    arrangeMappingNumber(mappedHeaderGrid, atIndex);
//                    headerStore.add(headerRec);//Commented to allow Multiple header mapping{SK}
                }
                
                var columnRec = mappedColumnStore.getAt(atIndex);
                if(columnRec!==undefined){
                    mappedColumnStore.remove(columnRec);
                    columnStore.add(columnRec);
                     //Rearrange table columns
//                    columnStore.sort("columnName","ASC");  
//                    columnStore.sort("isMandatory","DESC");
                    arrangeMappingNumber(mappedColumGrid, atIndex);
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
            
            importModuleName = this.moduleName;
            enabledBatchProperties = this.checkbatchProperties;

            // Drag n drop [ Headers -> Mapped Headers ]
            DropTargetEl =  mappedHeaderGrid.getView().el.dom.childNodes[0].childNodes[1];
            DropTarget = new Wtf.dd.DropTarget(DropTargetEl, {
                ddGroup    : 'mapHeader',
//                copy       : true,
                notifyDrop : function(ddSource, e, data){
                    function mapHeader(record, index, allItems) {
                        if(rowsDiff(mappedHeaderStore,mappedColumnStore)==0){
                            if(columnStore.getCount()!=0){
                                headerStore.getAt(headerStore.find("index",record.data.index)).set('isMapped',true);
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
                            if(importModuleName == "Product" && record.get("columnName") == "Initial Quantity" && enabledBatchProperties.length > 0){
                                WtfImportMsgBox([WtfGlobal.getLocaleText("acc.import.headerMApping"), "You can not map Initial Quantity column from here because "+enabledBatchProperties+" is enabled at company level. Please use Import Opening Quantity feature to set initial quantity of the products."], 0);
                                return false;
                            }
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
//        this.columnDs.sort("columnName","ASC");
//        this.csvHeaderDs.sort("header","ASC");

        if(this.mappedColsDs.getCount() != this.mappedCsvHeaderDs.getCount()){
            WtfImportMsgBox([WtfGlobal.getLocaleText("acc.import.headerMApping"), WtfGlobal.getLocaleText("acc.import.prevHead")], 0);
            return false;
        }
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
            
            if(this.moduleName == "Product" && colHeader == "Initial Quantity" && this.checkbatchProperties.length > 0 ){
                continue;
            }
            for(var j=0; j<this.tempFileHeaderDs.getCount(); j++){
                var csvrec = this.tempFileHeaderDs.getAt(j);
                var csvHeader = csvrec.data.header;
                csvHeader = csvHeader.trim();
                
                if(colHeader.toLowerCase()==csvHeader.toLowerCase()){
                    this.columnDs.remove(colrec);
                    this.mappedColsDs.add(colrec);

                    this.tempFileHeaderDs.remove(csvrec);
                    this.mappedCsvHeaderDs.add(csvrec);
                    
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
            if(this.moduleName == "Product" && colHeader == "Initial Quantity" && this.checkbatchProperties.length > 0 ){
                continue;
            }
              var index=-1
    
            var regex = new RegExp("^"+colHeader, "i");

            for(j=0; j<this.tempFileHeaderDs.getCount(); j++){
                csvrec = this.tempFileHeaderDs.getAt(j);
                csvHeader = csvrec.data.header;
                csvHeader = csvHeader.trim();
              
                /* Mapping "Permission for Role" column with "Permission Type"*/
                if (this.moduleName == "Role Management") {
                     index=csvHeader.indexOf("for Role");                  
                }

                if(regex.test(csvHeader) || index >=0){
                    this.columnDs.remove(colrec);
                    this.mappedColsDs.add(colrec);

                    this.tempFileHeaderDs.remove(csvrec);
                    this.mappedCsvHeaderDs.add(csvrec);
                    
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

                if(this.moduleName == "Product" && colHeader == "Initial Quantity" && this.checkbatchProperties.length > 0 ){
                    continue;
                }
                if(regex.test(colHeader)){
                    this.columnDs.remove(colrec);
                    this.mappedColsDs.add(colrec);

                    this.tempFileHeaderDs.remove(csvrec);
                    this.mappedCsvHeaderDs.add(csvrec);
                    
                    var csvStore = this.csvHeaderGrid.store;
                    csvStore.getAt(csvStore.find("index",csvrec.get('index'))).set('isMapped',true);
                    this.csvHeaderGrid.getView().refresh();
                    
                    j--;//'j' decreamented as count of csvHeaderDs store is reduce by 1
                    break;
                }
            }
        }

        //Move all mandatory columns an top
//        this.columnDs.sort("isMandatory","DESC");

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
            if(this.modName=="Product" || this.modName=="Assembly Product" || this.modName=="Accounts" || this.modName=="Journal Entry" || this.modName=="Customer" || this.modName=="Vendor" || this.modName=="Opening Fixed Asset Documents" || this.modName==Wtf.OpeningModuleName.openingCustomerSalesOrder || this.modName==Wtf.OpeningModuleName.openingVendorPurchaseOrder){//Added Custom Filed Details for Product
                        var columnname = (this.mappedColsDs.getAt(i).get("customflag") == true)? this.mappedColsDs.getAt(i).get("columnName") : this.mappedColsDs.getAt(i).get("pojoName");
                        var isLineItem = (this.mappedColsDs.getAt(i).get("fieldtype") == "Line Level Custom  Items")? true : false;
                    this.mappingJSON+="{\"csvindex\":\""+this.mappedCsvHeaderDs.getAt(i).get("index")+"\","+
                                        "\"csvheader\":\""+this.mappedCsvHeaderDs.getAt(i).get("header")+"\","+
                                        "\"columnname\":\""+columnname+"\","+
                                        "\"dataindex\":\""+this.mappedColsDs.getAt(i).get("dataindex")+"\","+
                                        "\"customflag\":\""+this.mappedColsDs.getAt(i).get("customflag")+"\","+
                                        "\"validatetype\":\""+this.mappedColsDs.getAt(i).get("validatetype")+"\","+
                                        "\"maxLength\":\""+this.mappedColsDs.getAt(i).get("maxLength")+"\","+
                                        "\"isLineItem\":\""+isLineItem+"\""+
                                        "},";
            }else{//Normal Flow for other than Product and Accounts Import
               
                this.mappingJSON+="{\"csvindex\":\""+this.mappedCsvHeaderDs.getAt(i).get("index")+"\","+
                                "\"csvheader\":\""+this.mappedCsvHeaderDs.getAt(i).get("header")+"\","+
                                 "\"columnname\":\""+this.mappedColsDs.getAt(i).get("pojoName")+"\","+
                                 "\"validatetype\":\""+this.mappedColsDs.getAt(i).get("validatetype")+"\","+
                                 "\"maxLength\":\""+this.mappedColsDs.getAt(i).get("maxLength")+"\","+
                                "\"dataindex\":\""+this.mappedColsDs.getAt(i).get("dataindex")+"\""+
                              "},";                    
            }           
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
Wtf.ValidateFileRecords = function(typeXLSFile, moduleName, store, scopeobj, extraParams, extraConfig){
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
    extraConfig['typeXLSFile'] = typeXLSFile;

    var importParams = {};
    importParams.url = url;
    importParams.extraConfig = extraConfig;
    importParams.extraParams = extraParams;
    importParams.store = store;
    importParams.scopeobj = scopeobj;

    var validateWindow = Wtf.getCmp("IWValidationWindow");
    if(!validateWindow) {
        validateWindow = new Wtf.IWValidationWindow({
           title: WtfGlobal.getLocaleText("acc.import.valAnalysis"),  //"Validation Analysis Report",
           prevWindow: Wtf.getCmp("csvMappingInterface"),
           typeXLSFile: typeXLSFile,
           importParams: importParams
        });
    }
    validateWindow.show();
    validateWindow.updateUpdationNote();
}
/**********************************************************************************************************
 *                              Validation Window
 **********************************************************************************************************/
Wtf.IWValidationWindow=function(config){
    Wtf.IWValidationWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.IWValidationWindow,Wtf.Window,{
    iconCls : 'importIcon',
    width: 750,
    height: 580,
    modal: true,
    layout: "border",
    id: 'IWValidationWindow',
    closable: false,
    initComponent:function(config){
        Wtf.IWValidationWindow.superclass.initComponent.call(this,config);
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
        var isAssetsImportHandler=false;
        if(this.importParams.extraConfig!=undefined && this.importParams.extraConfig.isAssetsImportFromDoc!=undefined && this.importParams.extraConfig.isAssetsImportFromDoc){
            isAssetsImportHandler=true;
        }
        this.importButton = new Wtf.Button({
            text: this.prevWindow.moduleName == Wtf.Bank_Reconciliation? WtfGlobal.getLocaleText("acc.import.MatchRecords") :WtfGlobal.getLocaleText("acc.import.impData"),  //"Match Record":"Import Data",
            scope: this,
            minWidth: 80,
            handler: this.prevWindow.moduleName == Wtf.Bank_Reconciliation? this.matchRecords :(isAssetsImportHandler?this.addAssetDetailByImport:this.importRecords)
        });
        this.cancelButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //"Cancel",
            scope: this,
            minWidth: 80,
            handler: function(){
                closeImportWindow();
            }
        });
        this.buttons = [this.prevButton, this.importButton, this.cancelButton];
    },

    onRender:function(config){
        Wtf.IWValidationWindow.superclass.onRender.call(this,config);

        this.on("show", function(){
            var uploadWindow = Wtf.getCmp("importwindow");          //check for pref updates
            var mappingWindow = Wtf.getCmp("csvMappingInterface");  //check for mapping updates
            if((uploadWindow && uploadWindow.isPrefModified) || (mappingWindow && mappingWindow.isMappingModified)){ // Check for new mapping updates
                if(uploadWindow){uploadWindow.isPrefModified=false;}
                if(mappingWindow){mappingWindow.isMappingModified=false;}
                this.validateRecords();
            }
        },this);

        this.northMessage = "<ul style='list-style-type:disc;padding-left:15px;'>" +
                    "<li>"+WtfGlobal.getLocaleText("acc.import.msg1")+"</li>"+
                    "<li>"+WtfGlobal.getLocaleText("acc.import.msg2")+"</li>"+
                    "<li id='validationprefupdationid' style='display:none;'></li>";
                
        this.add(this.northPanel= new Wtf.Panel({
            region: 'north',
            height: 80,
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
    updateUpdationNote: function(){
        var moduleName = this.importParams.extraConfig.moduleName;
        var updateFlag = this.importParams.extraConfig.updateExistingRecordFlag;
        var northMessage;
        if(updateFlag){ 
            if(moduleName == "Product"){
                northMessage = "Existing products will get update except Product Type, Initial Purchase Price, Sales Price, Activate Batches, Activate Serial No, Activate Warehouse, Activate Location Fields as in case this product is linked with other documents.";
            } else if(moduleName == "Customer"){
                northMessage = "Existing customers will get update except Creation Date Field.";
            } else if(moduleName == "Vendor"){
                northMessage = "Existing vendors will get update except Creation Date Field.";
            }
        }
        var el = document.getElementById('validationprefupdationid');
        if(northMessage){
            el.innerHTML = "<b>Note: </b>"+northMessage;
            el.style = "display:block;";
        }else{
            el.style="display:none;";
        }
    },
    validateRecords: function(){
        if(this.columnDs.getCount()>0) {        // clear previous validation
            this.columnDs.removeAll();
        }
        this.ValidationRecordCount.body.dom.innerHTML= "";
        this.enableDisableButtons(false);
        
        WtfGlobal.setAjaxTimeOutFor30Minutes();
//        Wtf.commonWaitMsgBox("Validating data... It may take few moments...");

        this.validateSubstr = "/ValidateFile/" + this.importParams.extraConfig.filename ;
        dojo.cometd.subscribe(this.validateSubstr, this, "globalInValidRecordsPublishHandler");
        var fetchCustomFields = (this.importParams.extraConfig != undefined && (this.importParams.extraConfig.modName=="Accounts" || this.importParams.extraConfig.modName=="Journal Entry" || this.importParams.extraConfig.modName=="Opening Fixed Asset Documents" || this.importParams.extraConfig.modName=="Product" || this.importParams.extraConfig.modName=="Opening Sales Invoice" || this.importParams.extraConfig.modName=="Opening Customer Credit Note" || this.importParams.extraConfig.modName=="Opening Customer Debit Note" || this.importParams.extraConfig.modName=="Opening Receipt"|| this.importParams.extraConfig.modName=="Opening Purchase Invoice" || this.importParams.extraConfig.modName=="Opening Payment" || this.importParams.extraConfig.modName=="Opening Vendor Credit Note" || this.importParams.extraConfig.modName=="Opening Vendor Debit Note" || this.importParams.extraConfig.modName== Wtf.OpeningModuleName.openingCustomerSalesOrder|| this.importParams.extraConfig.modName==Wtf.OpeningModuleName.openingVendorPurchaseOrder || this.importParams.extraConfig.modName=="Vendor" || this.importParams.extraConfig.modName=="Customer")) ? true : false; // fetach custom fields for Account Only
        var modName = (this.importParams.extraConfig.modName!=undefined && this.importParams.extraConfig.modName!=null) ? this.importParams.extraConfig.modName : "";
        var withoutBOM = (this.importParams.extraConfig.withoutBOM != undefined && this.importParams.extraConfig.withoutBOM != null) ? this.importParams.extraConfig.withoutBOM : false;
        /*
         * If SO/PO import Opening transaction then put below additional parameters
         */
       var subModuleFlag='';
        var isAssetsImportFromDoc=false;
        if (this.importParams.extraConfig !== undefined && this.importParams.extraConfig.isOpeningOrder !== undefined && this.importParams.extraConfig.isOpeningOrder !== '' && this.importParams.extraConfig.isOpeningOrder) {
            subModuleFlag = 0;
        }else if(this.importParams.extraConfig !== undefined && this.importParams.extraConfig.isAssetsImportFromDoc !== undefined && this.importParams.extraConfig.isAssetsImportFromDoc !== '' && this.importParams.extraConfig.isAssetsImportFromDoc){
            isAssetsImportFromDoc=this.importParams.extraConfig.isAssetsImportFromDoc;
        }
        
        Wtf.Ajax.requestEx({
            url: this.importParams.url + '?type=submit&do=validateData&fetchCustomFields=' + fetchCustomFields + '&modName=' + modName + '&withoutBOM=' + withoutBOM + '&subModuleFlag=' + subModuleFlag+ '&isAssetsImportFromDoc=' + isAssetsImportFromDoc,
            waitMsg :WtfGlobal.getLocaleText("acc.import.msg4"),  //'Validating...',
            scope:this,
            params: this.importParams.extraConfig
        },
        this,
        function (action,res) {
            Wtf.updateProgress();
            if(action.success){
                this.createGrid(action);
                var countMsg = "<div>" + WtfGlobal.getLocaleText("acc.import.total.records") + ":</b> " + action.totalrecords + ", <b>" + WtfGlobal.getLocaleText("acc.import.valid.records") + ":</b> " + action.valid + ", <span style = 'color:#F00;'><b>" + WtfGlobal.getLocaleText("acc.import.invalid.records") + ":</b> " + (action.totalrecords - action.valid) + " </span></div>";
                this.ValidationRecordCount.body.dom.innerHTML= countMsg;
                this.enableDisableButtons(true);
                        /*
                         * IF all records are Invalid then we are disable the 'Import Data' Button.
                         */
                        var disableImportDataButton = false;
                        if (res.parsedCount !== undefined && res.invalidCount !== undefined) {
                            if (res.parsedCount == res.invalidCount) {
                                disableImportDataButton = true;
                            }
                        } else {
                            if (action.totalrecords !== undefined && action.totalrecords !== "" && action.valid !== undefined && action.valid !== "") {
                                if (action.totalrecords == (action.totalrecords - action.valid)) {
                                 disableImportDataButton = true;
                                }
                            }
                        }
                        if (disableImportDataButton) {
                            this.importButton.disable();
                        }
            } else {
                WtfImportMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.import.msg5")+action.msg], 1);
            }
            if(action.exceededLimit=="yes"){ // update north panel
                this.northMessage = this.northMessage+"<li>"+WtfGlobal.getLocaleText("acc.import.msg6")+"</li><br/>";
                this.northPanel.body.dom.innerHTML=getImportTopHtml(WtfGlobal.getLocaleText("acc.import.msg7"), this.northMessage+"</ul>","../../images/import.png", true, "0px", "2px 0px 0px 10px")
                Wtf.Msg.alert('Error',"File contains more than "+action.totalrecords+" records. Please upload file with "+action.totalrecords+" records only.");
                this.importButton.disable();
            }
            this.importParams.extraConfig.exceededLimit = action.exceededLimit;
            WtfGlobal.resetAjaxTimeOut();
        },
        function (action,res) {
            Wtf.updateProgress();
            WtfImportMsgBox(50, 1);
            this.importParams.extraConfig.exceededLimit = action.exceededLimit;
            WtfGlobal.resetAjaxTimeOut();
        });
    },

    matchRecords: function(){
        dojo.cometd.unsubscribe(this.validateSubstr);
        closeImportWindow();
        var fetchCustomFields = (this.importParams.extraConfig != undefined && (this.importParams.extraConfig.moduleName == "Accounts" || this.importParams.extraConfig.moduleName == "Customer" || this.importParams.extraConfig.moduleName == "Vendor")) ? true : false; // fetch custom fields for Account Only
        Wtf.Ajax.requestEx({
            url: this.importParams.url+ ((this.importParams.extraConfig != undefined && (this.importParams.extraConfig.moduleName == "Product" || this.importParams.extraConfig.moduleName == "Journal Entry" ) && this.typeXLSFile)? '?type=submit&do=importXLS' : '?type=submit&do=import&fetchCustomFields='+fetchCustomFields),
            waitMsg :'importing...',
            scope:this,
            params: this.importParams.extraConfig
        },
        this,
        function(res) {
            Wtf.updateProgress();
            if(res.success){
                Wtf.globalStorereload(this.importParams.extraConfig);
                callImportBankReconciliationReport(this.importParams.extraConfig.accountid, this.importParams.extraConfig.startdate, this.importParams.extraConfig.enddate,this.importParams.extraConfig.openingBalance,res);
                WtfGlobal.resetAjaxTimeOut();
                closeImportWindow();
            } else { // Failure
                WtfImportMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), res.msg], 1);
            }
        },
        function(res){
            Wtf.updateProgress();
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.import.msg8"),WtfGlobal.getLocaleText("acc.import.msg9")+'<br/>'+WtfGlobal.getLocaleText("acc.import.msg10"));
            WtfGlobal.resetAjaxTimeOut();
            closeImportWindow();
        })  
    },

    importRecords: function(){
        dojo.cometd.unsubscribe(this.validateSubstr);

        WtfGlobal.setAjaxTimeOutFor30Minutes();
        Wtf.commonWaitMsgBox(WtfGlobal.getLocaleText("acc.import.msg7"));
        
        if (this.importParams.extraConfig != undefined && (this.importParams.extraConfig.moduleName == Wtf.Currency_Exchange || this.importParams.extraConfig.moduleName == Wtf.Tax_Currency_Exchange) ) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.import.impData"), WtfGlobal.getLocaleText("acc.import.currencydata"), function(btn) {
                if (btn != "yes") {
                    return;
                }
                else {
                    this.importCommondata();//import data
                }
            },this)
        }
        else {
            this.importCommondata();
        }
        
    },
    addAssetDetailByImport: function () {

        if (this.importDataJson.data.length > 0) {
            if (this.importParams.scopeobj.FADetailsGrid.store && this.importParams.scopeobj.FADetailsGrid.store.getCount() > 0) {
                this.importParams.scopeobj.FADetailsGrid.store.filterBy(function (rec) {
                    if (rec.data.assetId !== '') {
                        return true;
                    } else {
                        return false;
                    }
                }, this);
            }
            var assetLineRec=this.importParams.scopeobj.parentObj.Grid.FADetailsGrid.lineRec;
            for (var cnt = 0; cnt < this.importDataJson.data.length; cnt++) {
                this.importDataJson.data[cnt].currencysymbol = assetLineRec.data.currencysymbol;
                this.importDataJson.data[cnt].cost = this.importParams.scopeobj.parentObj.Grid.FADetailsGrid.getAmountInBase(this.importDataJson.data[cnt].costInForeignCurrency,assetLineRec);
                this.importDataJson.data[cnt].salvageValue = this.importParams.scopeobj.parentObj.Grid.FADetailsGrid.getAmountInBase(this.importDataJson.data[cnt].salvageValueInForeignCurrency,assetLineRec);
            }
            this.importParams.scopeobj.FADetailsGrid.store.loadData(this.importDataJson, true);
            if (this.importParams.scopeobj.FADetailsGrid.store && this.importParams.scopeobj.FADetailsGrid.store.getCount() > 1) {
                this.importParams.scopeobj.FADetailsGrid.store.sort('assetId', 'ASC');
            }
            closeImportWindow();
        }
    },
   importCommondata:function(){
        var fetchCustomFields = (this.importParams.extraConfig != undefined && (this.importParams.extraConfig.moduleName == "Accounts" ||
                this.importParams.extraConfig.moduleName == "Customer" || this.importParams.extraConfig.moduleName == "Vendor" 
                || this.importParams.extraConfig.moduleName=="Opening Sales Invoice" || this.importParams.extraConfig.moduleName=="Opening Customer Credit Note"
                || this.importParams.extraConfig.moduleName=="Opening Customer Debit Note" || this.importParams.extraConfig.moduleName=="Opening Receipt"
                || this.importParams.extraConfig.modName=="Opening Purchase Invoice" || this.importParams.extraConfig.modName=="Opening Payment" 
                || this.importParams.extraConfig.modName=="Opening Vendor Credit Note" || this.importParams.extraConfig.modName=="Opening Vendor Debit Note"
                || this.importParams.extraConfig.modName==Wtf.OpeningModuleName.openingCustomerSalesOrder || this.importParams.extraConfig.modName==Wtf.OpeningModuleName.openingVendorPurchaseOrder))
                ? true : false; // fetach custom fields for Account Only
                
        if (this.importParams.extraConfig != undefined && this.importParams.extraConfig != '') {
            if (this.importParams.extraConfig.isOpeningOrder == undefined || this.importParams.extraConfig.isOpeningOrder == '') {
                this.importParams.extraConfig.isOpeningOrder = false;
            }
        }
        this.prevButton.disable();
        this.importButton.disable();
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        Wtf.Ajax.requestEx({
            url: this.importParams.url+ ((this.importParams.extraConfig != undefined && (this.importParams.extraConfig.moduleName == "Product" || this.importParams.extraConfig.moduleName == "Journal Entry" ) && this.typeXLSFile)? '?type=submit&do=importXLS' : '?type=submit&do=import&fetchCustomFields='+fetchCustomFields),
            waitMsg :'importing...',
            scope:this,
            params: this.importParams.extraConfig
        },
        this,
        function(res) {
            WtfGlobal.resetAjaxTimeOut();
            Wtf.updateProgress();
            if(res.success){
                if(res.Module == Wtf.Acc_Product_Master_ModuleId) {
                    Wtf.uomStore.reload();
                }
                else if(res.Module ==Wtf.Currency_ExchangeRate_Module_Id){  //import currency exchange Rate 
                   var panel = Wtf.getCmp("CurrencyExchangewin");
                   if(panel!='undefined'){
                    panel.grid.store.reload();   
                }
                  }
                else if(res.Module ==Wtf.Tax_Currency_ExchangeRate_Module_Id){  //import currency exchange Rate 
                   var panel = Wtf.getCmp("TaxCurrencyExchangewin");
                   if(panel!='undefined'){
                    panel.grid.store.reload();   
                }
                  }
                if(res.exceededLimit=="yes"){ // Importing data with thread
//                    Wtf.Msg.alert('Success', 'We are now importing your data from the uploaded file.<br/>Depending on the number of records, this process can take anywhere from few minutes to several hours.<br/>A detailed report will be sent to you via email and will also be displayed in the import log after the process is completed.');
                    showImportSummary(true, res);
                } else {
                    if(this.importParams.store!=undefined && res.TLID == undefined) {
//                        this.importParams.store.reload();
                    }
                    // StoreManager (Global Store) reload
                    Wtf.globalStorereload(this.importParams.extraConfig);
//                    WtfImportMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), res.msg], 0);
                    showImportSummary(false, res);
                }
                WtfGlobal.resetAjaxTimeOut();
                closeImportWindow();
            } else { // Failure
                WtfImportMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), res.msg], 1);
            }
        },
        function(res){
            Wtf.updateProgress();
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.import.msg8"),WtfGlobal.getLocaleText("acc.import.msg9")+'<br/>'+WtfGlobal.getLocaleText("acc.import.msg10"));
            WtfGlobal.resetAjaxTimeOut();
            closeImportWindow();
         })  
},

    createGrid: function(response){
        this.createColumnModel(response);
        this.importDataJson=response.validdatajson;
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
                    return "<div style='color:#F00'>"+val?val:""+"</div>";
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
                        ""+ rec.data.validateLog + ""+
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
        var disableImportDataButton=false;
        if(res.parsedCount !== undefined && res.invalidCount !== undefined){
            disableImportDataButton=res.parsedCount==res.invalidCount;
        }

        if (res.finishedValidation && !disableImportDataButton) {
            this.enableDisableButtons(true);
            return;
        } else {
            this.importButton.disable();
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
        height: backgroundProcessing ? 450 : 350,
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
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.importlog, Wtf.Perm.importlog.view))
    	callImportFilesLog();
    else
    	WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+WtfGlobal.getLocaleText("acc.import.impLog"));
}
/*-------------------- Function to show Upload Windows -----------------*/
Wtf.commonFileImportWindow = function(obj, moduleName, store, extraParams, extraConfig){
    var impWin1 = new Wtf.UploadFileWindow({
        title: WtfGlobal.getLocaleText("acc.import.csv"),  //'Import CSV File',
        width: 600,
        height: 475,
        iconCls: 'importIcon',
        obj: obj,
        withoutBOM:extraConfig.withoutBOM!=undefined?(extraConfig.withoutBOM):false,
        moduleName: moduleName,
        store: store,
        extraParams: extraParams,
        extraConfig:extraConfig,
        typeXLSFile: false,
        isBomlessFile : extraConfig.isBomlessFile!=undefined ? (extraConfig.isBomlessFile) : false  //To check whether the file contains BOM data or not.
    });
    return impWin1;
}

Wtf.xlsCommonFileImportWindow = function(obj,moduleName,store,extraParams, extraConfig) {
    var impWin1 = new Wtf.UploadFileWindow({
        title: WtfGlobal.getLocaleText("acc.import.excel"),  //'Import XLS File',
        width: 600,
        height: 425,
        iconCls: 'importIcon',
        obj: obj,
        moduleName: moduleName,
        store: store,
        extraParams: extraParams,
        extraConfig:extraConfig,
        typeXLSFile: true,
        isBomlessFile : extraConfig.isBomlessFile!=undefined ? (extraConfig.isBomlessFile) : false  //To check whether the file contains BOM data or not.
    });
    return impWin1;
}
/*----------------------------------------------------------------------------
--------------------------- commonUploadWindow -------------------------------
------------------------------------------------------------------------------*/
Wtf.UploadFileWindow=function(config){
    Wtf.UploadFileWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.UploadFileWindow, Wtf.Window,{
    id: 'importwindow',
    layout: "border",
    closable: false,
    resizable: false,
    modal: true,
    iconCls: 'importIcon',
    initComponent:function(config){
        Wtf.UploadFileWindow.superclass.initComponent.call(this,config);

        this.nextButton = new Wtf.Button({
            text: WtfGlobal.getLocaleText("acc.common.next")+" &nbsp; >>",  //<font size=2> >> </font>",
            scope: this,
            minWidth: 80,
            isBomlessFile : this.extraConfig.bomlessfile,
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
                closeImportWindow();
            }
        });
        this.buttons = [this.nextButton, this.cancelButton];
    },

    onRender:function(config){
        Wtf.UploadFileWindow.superclass.onRender.call(this,config);
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
        } else if(this.moduleName == "Product"){
            var delimiterStore = new Wtf.data.SimpleStore({
                fields: ['delimiterid','delimiter'],
                data : [
                    [1,'Comma'],
                    [3,'Bar'] //if change this values need to handal in java side
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
       
       this.updateExistingRec = new Wtf.form.Checkbox({
            name:'updateExistingRec',
            fieldLabel:WtfGlobal.getLocaleText("acc.import.updateexistingrecord"),//Update Existing Record
            checked:false,
            labelWidth:150,
            hidden:!(this.moduleName == 'Accounts' || this.moduleName == 'Customer' || this.moduleName == 'Vendor' || this.moduleName == 'Product' || this.moduleName == 'Assembly Product' || this.moduleName == 'Opening Fixed Asset Documents'),
            hideLabel:!(this.moduleName == 'Accounts' || this.moduleName == 'Customer' || this.moduleName == 'Vendor' || this.moduleName == 'Product' || this.moduleName == 'Assembly Product' || this.moduleName == 'Opening Fixed Asset Documents'),
            scope:this,
            cls : 'custcheckbox',
            width: 200
        });
       
       this.createUomSchemaType = new Wtf.form.Checkbox({
            name: 'createUomSchemaType',
            fieldLabel: WtfGlobal.getLocaleText("acc.field.createUOMSchemaTypeIfNotPresent"), // "Create UOM Schema Type If Not Present",
            checked: false,
            labelWidth: 150,
            labelStyle: 'width:250px',
            hidden: !(this.moduleName == 'UOM Schema'),
            hideLabel: !(this.moduleName == 'UOM Schema'),
            scope: this,
            cls : 'custcheckbox',
            width: 200
        });
       
       this.deleteExistingUomSchema = new Wtf.form.Checkbox({
            name: 'deleteExistingUomSchema',
            fieldLabel: WtfGlobal.getLocaleText("acc.field.deleteExistingUOMSchema"), // "Delete Existing UOM Schema",
            checked: false,
            labelWidth: 150,
            labelStyle: 'width:200px',
            hidden: !(this.moduleName == 'UOM Schema'),
            hideLabel: !(this.moduleName == 'UOM Schema'),
            scope: this,
            cls : 'custcheckbox',
            width: 200
        });
       
      this.masterPreference = new Wtf.form.FieldSet({
            title: WtfGlobal.getLocaleText("acc.import.masterPref1"),  //"For missing entries in dropdown fields",
            autoHeight: true,
            border: false,
//            hidden:this.isGroupImport,
            cls: "import-Wiz-fieldset",
            defaultType: 'radio',
            hidden:this.moduleName =="Role Management",
            items: [
                this.master2 = new Wtf.form.Radio({
                    ctCls:"fieldset-item1",
                    checked: true,
                    hideLabel:true,
                    fieldLabel: '',
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.import.masterPref4"),  //"Add new entry to master record in dropdown",
                    name: 'masterPreference',
                    inputValue: "2"
                }),
                this.master1 = new Wtf.form.Radio({
                    ctCls:"fieldset-item",
                    hideLabel:true,
                    fieldLabel: '',
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.import.masterPref3"),  //"Ignore entry for that record",
                    name: 'masterPreference',
                    inputValue: "1"
                }),
                this.master0 = new Wtf.form.Radio({
                    hideLabel:true,
                    fieldLabel: '',
                    labelSeparator: '',
                    boxLabel: WtfGlobal.getLocaleText("acc.import.masterPref2"),  //"Ignore entire record",
                    name: 'masterPreference',
                    inputValue: "0"
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
            itemCls : ((this.typeXLSFile && this.moduleName != "Journal Entry" ) || this.moduleName == "Unit of Measure" || this.moduleName == "Product Category" || this.moduleName == "UOM Schema" || this.moduleName == "Customer Address Details" || this.moduleName == "Customer Category" || this.moduleName == "Vendor Category") ? "hidden-from-item" : "",
            hidden:  (this.typeXLSFile && this.moduleName != "Journal Entry" ) || this.isGroupImport || this.moduleName == "Unit of Measure" || this.moduleName == "Product Category" || this.moduleName == "UOM Schema" || this.moduleName == "Customer Address Details" || this.moduleName == "Customer Category" || this.moduleName == "Vendor Category" || this.moduleName == "Role Management",
            hideLabel: (this.typeXLSFile && this.moduleName != "Journal Entry" ) || this.isGroupImport || this.moduleName == "Unit of Measure" || this.moduleName == "Product Category" || this.moduleName == "UOM Schema" || this.moduleName == "Customer Address Details" || this.moduleName == "Customer Category" || this.moduleName == "Vendor Category" || this.moduleName == "Role Management",
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
        
        // Sample File to download
        var storeageName = "";
        var fileName = "";
        var type = "";
        var ModuleName = "";
        
        if (this.typeXLSFile) {
            type = 'xls'
        } else {
            type = 'csv';
        }
        if (this.moduleName == "Assembly Product" && (this.withoutBOM!=undefined && this.withoutBOM)) {
            storeageName = 'download_sample_assembly_product_withoutBOM.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_assembly_product_withoutBOM';
        }else if (this.moduleName == "Assembly Product") {
            storeageName = 'download_sample_assembly_product.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_assembly_product';
        } else if (this.moduleName == "Journal Entry") {
            if (this.typeXLSFile) {
                storeageName = 'download_sample_journal_entry.xls';
                ModuleName = this.moduleName;
                fileName = 'sample_journal_entry';
            } else {
            storeageName = 'download_sample_journal_entry.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_journal_entry';
            }
        } else if (this.moduleName == "Product Price List") { // ERP-12380 [SJ]
            storeageName = 'download_sample_product_price_import.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_product_price_import';
        } else if (this.moduleName == "Product opening stock") { // ERP-12382 [SJ]
            storeageName = 'download_sample_product_opening_stock_v2.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_product_opening_stock';
        } else if (this.moduleName == "Product") { // ERP-12381 [SJ]
            if (this.typeXLSFile) {
                storeageName = 'download_sample_product_v3.xls';
                ModuleName = this.moduleName;
                fileName = 'sample_product';
            } else {
                storeageName = 'download_sample_product_v3.csv';
                ModuleName = this.moduleName;
                fileName = 'sample_product';
            }
        } else if (this.moduleName == "UOM Schema") {
            storeageName = 'download_sample_uom_schema1.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_uom_schema';
        } else if (this.moduleName == "Price List - Band") {
            storeageName = 'download_sample_price_list_band_price.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_price_list_band_price';
        } else if (this.moduleName == "Product Category") {
            storeageName = 'download_sample_product_category.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_product_category';
        } else if (this.moduleName == "Customer") {
            if (this.typeXLSFile) {
                storeageName = 'download_sample_customer_v3.xls';
                ModuleName = this.moduleName;
                fileName = 'sample_customer';
            } else {
                storeageName = 'download_sample_customer_v3.csv';
                ModuleName = this.moduleName;
                fileName = 'sample_customer';
            }
        } else if (this.moduleName == "Vendor") {
            if (this.typeXLSFile) {
                storeageName = 'download_sample_vendor_v3.xls';
                ModuleName = this.moduleName;
                fileName = 'sample_vendor';
            } else {
                storeageName = 'download_sample_vendor_v3.csv';
                ModuleName = this.moduleName;
                fileName = 'sample_vendor';
            }
        } else if (this.moduleName == "Accounts") {
            if (this.typeXLSFile) {
                storeageName = 'download_sample_coa.xls';
                ModuleName = this.moduleName;
                fileName = 'sample_coa';
            } else {
                storeageName = 'download_sample_coa.csv';
                ModuleName = this.moduleName;
                fileName = 'sample_coa';
            }
        } else if (this.moduleName == "Group") {
             if (this.typeXLSFile) {    //    ERP-18462
                storeageName = 'download_sample_account_groups.xls';
                ModuleName = this.moduleName;
                fileName = 'sample_account_groups';
            }else{            
                storeageName = 'download_sample_account_groups.csv';
                ModuleName = this.moduleName;
                fileName = 'sample_account_groups';
            }
        } else if (this.moduleName == "Unit of Measure") {
            storeageName = 'download_sample_uom.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_uom';
        }else if (this.moduleName == "Fixed Asset Group") {
            storeageName = 'AssetGroup_MAP.csv';
            ModuleName = this.moduleName;
            fileName = 'AssetGroup_MAP';
        }else if (this.moduleName == "Opening Fixed Asset Documents"){
                storeageName = 'Fixed_Asset_Opening.csv';
                ModuleName = this.moduleName;
                fileName = 'Fixed_Asset_Opening';
        } else if (this.moduleName == "Asset GoodsReceiptOrder" && this.extraConfig.isAssetsImportFromDoc) {
                storeageName = 'Fixed_Asset_Goods_Receipt.xls';
                ModuleName = 'Fixed Asset Goods Receipt';
                fileName = 'Fixed_Asset_GoodsReceipt';
        } else if(this.moduleName==Wtf.OpeningModuleName.openingSalesInvoice){
            storeageName = 'download_sample_opening_sales_invoice.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_opening_sales_invoice';                      //fileName without extention.
        } else if(this.moduleName==Wtf.OpeningModuleName.openingPrchaseInvoice){
            storeageName = 'download_sample_opening_purchase_invoice.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_opening_purchase_invoice';                   //fileName without extention
        } else if(this.moduleName==Wtf.OpeningModuleName.openingReceipt){
            storeageName = 'download_sample_opening_receive_payment.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_opening_receive_payment';                  //fileName without extention.
        } else if(this.moduleName==Wtf.OpeningModuleName.openingPayment){
            storeageName = 'download_sample_opening_make_payment.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_opening_make_payment';                   //fileName without extention
        } else if(this.moduleName==Wtf.OpeningModuleName.openingVendorCreditNote){
            storeageName = 'download_sample_opening_vendor_credit_note.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_opening_vendor_credit_note';                //fileName without extention.
        } else if(this.moduleName==Wtf.OpeningModuleName.openingCustomerCreditNote){
            storeageName = 'download_sample_opening_customer_credit_note.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_opening_customer_credit_note';               //fileName without extention.
        } else if(this.moduleName==Wtf.OpeningModuleName.openingVendorDebitNote){
            storeageName = 'download_sample_opening_vendor_debit_note.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_opening_vendor_debit_note';                  //fileName without extention.
        } else if(this.moduleName==Wtf.OpeningModuleName.openingCustomerDebitNote){
            storeageName = 'download_sample_opening_customer_debit_note.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_opening_customer_debit_note';            //fileName without extention.
        }else if(this.moduleName==Wtf.OpeningModuleName.openingCustomerSalesOrder){
            storeageName = 'download_sample_opening_sales_order_list.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_opening_sales_order';            //fileName without extention.
        }else if(this.moduleName==Wtf.OpeningModuleName.openingVendorPurchaseOrder){
            storeageName = 'download_sample_opening_purchase_order_list.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_opening_purchase_order';            //fileName without extention.
        }
        else if(this.moduleName== Wtf.Currency_Exchange){
            storeageName = 'download_sample_currency_exchange.xls';
            ModuleName = this.moduleName;
            fileName = 'sample_currency_exchange';
        }
        else if( this.moduleName== Wtf.Tax_Currency_Exchange){
            storeageName = 'download_sample_tax_currency_exchange.xls';
            ModuleName = this.moduleName;
            fileName = 'sample_tax_currency_exchange';
        }else if (this.moduleName == "Store Master") {            
            storeageName =Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA?'download_sample_store_matser1.csv':'download_sample_store_matserIndia.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_Store_Master';
        }  
        else if (this.moduleName == "Location Master") {
            storeageName = 'download_sample_location_matser1.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_location_master';
        } 
        else if (this.moduleName == Wtf.Bank_Reconciliation) {
            storeageName = 'download_Sample_Bank_Reconciliation.csv';
            ModuleName = this.moduleName;
            fileName = 'Sample_Bank_Reconciliation';
        } else if(this.moduleName == Wtf.DBS_Receiving_Bank_Details){
            storeageName = 'download_Sample_DBS_Receiving_Bank_Details.csv';
            ModuleName = this.moduleName;
            fileName = 'Sample_DBSDetails';
        } else if (this.moduleName == "Customer Address Details") {
            if (this.typeXLSFile) {
                storeageName = 'download_sample_customer_address.csv';
            } else {
                storeageName = 'download_sample_customer_address.xls';
            }
            ModuleName = this.moduleName;
            fileName = 'sample_customer_address';
        } else if (this.moduleName == "Customer Category") {
            if (this.typeXLSFile) {
                storeageName = 'download_sample_customer_category.csv';
            } else {
                storeageName = 'download_sample_customer_category.xls';
            }
            ModuleName = this.moduleName;
            fileName = 'sample_customer_category';
        } else if (this.moduleName == "Vendor Category") {
            if (this.typeXLSFile) {
                storeageName = 'download_sample_vendor_category.csv';
            } else {
                storeageName = 'download_sample_vendor_category.xls';
            }
            ModuleName = this.moduleName;
            fileName = 'sample_vendor_category';
        }else if (this.moduleName == "StockAdjustment") { // ERP-12380 [SJ]
            if (this.typeXLSFile) {
                storeageName = 'download_sample_StockAdjustment_import.xlsx';
            }else {
                storeageName = 'download_sample_StockAdjustment_import.csv';
            }
            ModuleName = this.moduleName;
            fileName = 'sample_StockAdjustment_import';
        }else if (this.moduleName == Wtf.Inter_Store_Stock_Transfer) { // ERM-718 Import for Ist
            if (this.typeXLSFile) {
                storeageName = 'download_sample_StoreStockTransfer_import.xlsx';
            }else {
                storeageName = 'download_sample_StoreStockTransfer_import.csv';
            }
            ModuleName = this.moduleName;
            fileName = 'sample_StoreStockTransfer_import';
        }else if (this.moduleName =="Role Management") {
            storeageName = 'download_sample_user_permission.csv';
            ModuleName = this.moduleName;
            fileName = 'sample_user_permission';
        }else if (this.moduleName==Wtf.EWAY_BILL_IMPORT_MODULENAME) {
            /**
             * Import E-way fileds Sample file 
             */
            storeageName = Wtf.EWAY_BILL_IMPORT_MODULENAME +'.xlsx';
            fileName = 'E-Way Bill details import';            
            ModuleName = Wtf.EWAY_BILL_IMPORT_MODULENAME;                        
        }
        var hideQA=this.moduleName == "StockAdjustment"?Wtf.account.companyAccountPref.isStockInQAFlowActivated?false:true:true;
        this.qaCheck= new Wtf.form.Checkbox({
            checked: false,
            fieldLabel: WtfGlobal.getLocaleText("acc.rem.261"),
            hidden:hideQA,
            hideLabel:hideQA,
            labelStyle: 'width:200px',
            name: 'goThroughQA',
            inputValue: ""
        })
        var downloadSampleFileLink = new Wtf.XTemplate(
            '<tpl>',
                WtfGlobal.getLocaleText("acc.field.DownloadSampleFile")+" <a class='tbar-link-text' href='#' onClick='javascript: downloadSampleFileType(\""+storeageName+"\",\""+fileName+"\",\""+type+"\",\""+ModuleName+"\")'wtf:qtip=''>"+WtfGlobal.getLocaleText("acc.field.SampleFile")+"</a>",
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
            labelWidth: 150,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:15px 0 0 60px;',
            layout: 'form',
            items:[
                this.browseField,
                this.conowner,
                this.datePreference,
                this.updateExistingRec,
                this.createUomSchemaType,
                this.deleteExistingUomSchema,
                this.masterPreference,
                this.qaCheck,
                this.sampleLinkPanel      //Need to add check for Assembly Product only : Download Sample File option for Assembly Product only
            ]
        });

        this.add({
                    region:'north',
                    height:125,
                    border : false,
                    bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf; padding:2px;',
                    html: getImportTopHtml((this.typeXLSFile?WtfGlobal.getLocaleText("acc.import.excel"):WtfGlobal.getLocaleText("acc.import.csv")), "<ul style='list-style-type:disc;padding-left:15px;'><li>"+(this.typeXLSFile?WtfGlobal.getLocaleText("acc.rem.excel"):WtfGlobal.getLocaleText("acc.rem.143"))+"</li><li>"+WtfGlobal.getLocaleText("acc.rem.filelength")+"</li><li>"+(this.moduleName == "Price List - Band" || this.moduleName == "Product Price List" ? WtfGlobal.getLocaleText("acc.import.limit.recordcount_10000.note") : WtfGlobal.getLocaleText("acc.import.limit.recordcount.note"))+"</li><li>"+WtfGlobal.getLocaleText("acc.rem.92")+"</li><li style='color:red;'>"+WtfGlobal.getLocaleText("acc.import.msg15")+"</li></ul>","../../images/import.png", true, "5px 0px 0px 0px", "7px 0px 0px 10px")
                });

        this.add({
                    region:'center',
//                    layout:'fit',
                    border:false,
                    bodyStyle: 'background:#f1f1f1;font-size:10px;',
                    items:[
                        this.ImportForm
//                        new Wtf.Panel({
//                            border: false,
//                            bodyStyle: 'padding:5px;',
//                            html: "<b>* "+WtfGlobal.getLocaleText("acc.import.msg15")+"</b> "
//                        })
]
                })

        this.conowner.on("change", function(){this.isPrefModified=true;},this);
        this.master0.on("change", function(){this.isPrefModified=true;},this);
        this.master1.on("change", function(){this.isPrefModified=true;},this);
        this.master2.on("change", function(){this.isPrefModified=true;},this);
        this.datePreference.on("change", function(){this.isPrefModified=true;},this);
        this.updateExistingRec.on("change", function() {this.isPrefModified = true;}, this);
        this.qaCheck.on("change", function() {
            this.isPrefModified = true;
        }, this);
    },

    uploadCSVFile : function(){
        var master = 0;
        var isQAActivated=false; 
        if(this.master0.getValue()){
            master = 0;
        } else if(this.master1.getValue()){
            master = 1;
        } else if(this.master2.getValue()){
            master = 2;
        }
        if(this.qaCheck.checked && !this.qaCheck.hidden){
            isQAActivated=true;
        }
        if(!this.browseField.disabled){
            this.nextButton.disable();
            var isBomlessFile = this.nextButton.isBomlessFile!=undefined ? this.nextButton.isBomlessFile : false;  //Send this param to Java Side to perform withBOM & withoutBOM File validation.
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
                this.extraConfig['isQAActivated'] = isQAActivated;
                /*
                 * If SO/PO import Opening transaction then put below additional parameters
                */
                var fromdocument = "";
                if (this.extraConfig.isOpeningOrder !== undefined && this.extraConfig.isOpeningOrder !== '' && this.extraConfig.isOpeningOrder) {
                    fromdocument = true;
                }
                this.ImportForm.form.submit({
                    url:"ImportRecords/importRecords.do?type="+this.moduleName+"&do=getMapCSV&delimiterType="+delimiterType+"&isBomlessFile="+isBomlessFile+"&fromdocument="+fromdocument,
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
            var mappingWindow = Wtf.getCmp("csvMappingInterface");
            if(mappingWindow.extraConfig == undefined) {
                mappingWindow.extraConfig={};
            }
            mappingWindow.extraConfig['delimiterType'] = this.conowner.getValue();
            mappingWindow.extraConfig['masterPreference'] = master;
            mappingWindow.extraConfig['dateFormat'] = this.datePreference.getValue();
            mappingWindow.extraConfig['isQAActivated'] = isQAActivated;
            if (mappingWindow.updateExistingRecordFlag != undefined) {
                mappingWindow.updateExistingRecordFlag = this.updateExistingRec.getValue();
            }
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
        this.mappingParams.updateExistingRecordFlag = this.updateExistingRec.getValue();
        this.mappingParams.createUomSchemaTypeFlag = this.createUomSchemaType.getValue();
        this.mappingParams.deleteExistingUomSchemaFlag = this.deleteExistingUomSchema.getValue();
        
        Wtf.callMappingInterface(this.mappingParams, this);
        this.hide();
    },

    uploadXLSFile: function(){
        var master = 0;
        var isQAActivated=false; 
        if(this.master0.getValue()){
            master = 0;
        } else if(this.master1.getValue()){
            master = 1;
        } else if(this.master2.getValue()){
            master = 2;
        }
        if(this.qaCheck.checked && !this.qaCheck.hidden){
            isQAActivated=true;
        }
        this.extraConfig['isQAActivated'] = isQAActivated;
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
                if (this.moduleName == "Journal Entry") {
                    this.extraConfig['dateFormat'] = this.datePreference.getValue();
                }

                this.ImportForm.getForm().submit({
                    url:"ImportRecords/fileUploadXLS.do",
                    waitMsg:WtfGlobal.getLocaleText("acc.rem.167"),  //'Uploading File...',
                    scope:this,
                    success:function(f,a){
                        this.browseField.disable();
                        this.nextButton.enable();
                        var updateExistingRecordFlag = this.updateExistingRec.getValue();
                        var createUomSchemaTypeFlag = this.createUomSchemaType.getValue();
                        var deleteExistingUomSchemaFlag = this.deleteExistingUomSchema.getValue();
                        this.genUploadResponse(a.request,true,a.response,this.moduleName,this.store, this.obj, this.extraParams, this.extraConfig,updateExistingRecordFlag,createUomSchemaTypeFlag,deleteExistingUomSchemaFlag);
                    },
                    failure:function(f,a){
                        this.nextButton.enable();
                        var updateExistingRecordFlag = this.updateExistingRec.getValue();
                        var createUomSchemaTypeFlag = this.createUomSchemaType.getValue();
                        var deleteExistingUomSchemaFlag = this.deleteExistingUomSchema.getValue();
                        this.genUploadResponse(a.request,false,a.response,this.moduleName,this.store, this.obj, this.extraParams, this.extraConfig,updateExistingRecordFlag,createUomSchemaTypeFlag,deleteExistingUomSchemaFlag);
                    }
                });
            } else {
                WtfImportMsgBox(48);
                this.nextButton.enable();
            }
        } else {
            var xlsPreviewWindow = Wtf.getCmp("importxls");
            if(xlsPreviewWindow.extraConfig == undefined) {
                xlsPreviewWindow.extraConfig={};
            }
            xlsPreviewWindow.extraConfig['masterPreference'] = master;
            if (this.moduleName == "Journal Entry") {
                xlsPreviewWindow.extraConfig['dateFormat'] = this.datePreference.getValue();
            }
            if (xlsPreviewWindow.updateExistingRecordFlag!=undefined) {
                xlsPreviewWindow.updateExistingRecordFlag = this.updateExistingRec.getValue();
            }
            xlsPreviewWindow.show();
        }
    },

    genUploadResponse: function(req,succeed,res,moduleName,store,obj,extraParams, extraConfig,updateExistingRecordFlag,createUomSchemaTypeFlag,deleteExistingUomSchemaFlag){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        var response=eval('('+res.responseText+')');
        if(succeed){
            succeed=response.lsuccess;
            if(succeed){
                var xlsPreviewWindow = Wtf.getCmp("importxls");
                if(xlsPreviewWindow) {
                    if(xlsPreviewWindow.isVisible()){
                        xlsPreviewWindow.close();
                    } else {
                        xlsPreviewWindow.destroy();
                    }
                }

                this.win=new Wtf.SheetViewer1({
                    title: WtfGlobal.getLocaleText("acc.import.availSheet"),  //'Available Sheets',
                    iconCls: 'importIcon',
                    autoScroll:true,
                    plain:true,
                    modal:true,
                    data:response,
                    layout:'border',
                    prevWindow: Wtf.getCmp("importwindow"),
                    moduleName:moduleName,
                    store:store,
                    obj:obj,
                    extraParams: extraParams,
                    extraConfig: extraConfig,
                    updateExistingRecordFlag:updateExistingRecordFlag,
                    createUomSchemaTypeFlag: createUomSchemaTypeFlag,
                    deleteExistingUomSchemaFlag: deleteExistingUomSchemaFlag
                });
                this.win.show();
                Wtf.getCmp("importwindow").hide();
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
    }else {
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
                totalProperty:'count',
                root: "data"
            },this.columnRec),
            url: "ImportRecords/getImportLog.do"
        });
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
            renderer : function(val){
                return "<div class=\"pwnd downloadIcon original\" wtf:qtip=\"Download Original File\" style=\"height:16px;\">&nbsp;</div>";
            }
        },{
            header:WtfGlobal.getLocaleText("acc.importLog.rejectedFile"),  //"Rejected File",
            sortable:true,
            align: "center",
            dataIndex:"rejected",
            renderer : function(val){
                if(val>0){
                    return "<div class=\"pwnd downloadIcon rejected\" wtf:qtip=\"Download Rejected File\" style=\"height:16px;\">&nbsp;</div>";
                }
                return "";
            }
        }
        ]);

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
            bbar: this.pag=new Wtf.PagingToolbar({
                pageSize: 30,
                border : false,
                id : "paggintoolbar"+this.id,
                store: this.dataStore,
                plugins : this.pPageSizeObj = new Wtf.common.pPageSize({
                    id : "pPageSize_"+this.id
                }),
                autoWidth : true,
                displayInfo:true//,
//                items:['-',
//                        new Wtf.Toolbar.Button({
//                            text:'Downloads',
//                            width:200,
//                            scope:this,
//                            menu:[
//                                {
//                                    text: 'Original',
//                                    id: 'Original',
//                                    disabled:true,
//                                    scope:this,
//                                    iconCls:"dwnload",
//                                    handler:function(){
//                                        var rec = this.grid.getSelectionModel().getSelections()[0].data;
//                                        Wtf.get('downloadframe').dom.src = Wtf.req.springBase+'common/ImportRecords/downloadFileData.do?storagename='+rec.storename+'&filename='+rec.filename+'&type='+rec.type;
//                                    }
//                                },
//                                {
//                                    text: 'Rejected',
//                                    id: 'Rejected',
//                                    disabled:true,
//                                    scope:this,
//                                    iconCls:"faileddwnload",
//                                    handler:function(){
//                                        var rec = this.grid.getSelectionModel().getSelections()[0].data;
//                                        var filename = rec.filename;
//                                        var storagename = rec.failurename;
//                                        var type = rec.type;
//                                        if(type=="xls"){
//                                            type = "csv";
//                                            filename = filename.substr(0, filename.lastIndexOf(".")) + ".csv";
//                                            storagename = storagename.substr(0, storagename.lastIndexOf(".")) + ".csv";
//                                        }
//                                        Wtf.get('downloadframe').dom.src = Wtf.req.springBase+'common/ImportRecords/downloadFileData.do?storagename='+storagename+'&filename=Failure_'+filename+'&type='+type;
//                                    }
//                                }
//                            ]
//                        })
//                    ]
            })
        });


        this.grid.on('rowclick',this.handleRowClick,this);

        this.sm.on("selectionchange",function(sm){
//            var sels = this.sm.getSelections();
//            if(sels.length==1){
//                Wtf.getCmp("Original").enable();
//                var rec = sels[0];
//                if(rec.data.rejected>1){
//                    Wtf.getCmp("Rejected").enable();
//                }
//            }else{
//                Wtf.getCmp("Original").disable();
//                Wtf.getCmp("Rejected").disable();
//            }
        },this);

        this.wrapperBody = new Wtf.Panel({
            border: false,
            layout: "fit",
            tbar : [WtfGlobal.getLocaleText("acc.common.from"),this.startDate,"-",WtfGlobal.getLocaleText("acc.common.to"),this.endDate,"-",this.fetchButton],
            items : this.grid
        });

        this.initialLoad();
        this.add(this.wrapperBody);
        Wtf.csvFileMappingInterface.superclass.onRender.call(this, config);
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





Wtf.SheetViewer1=function(config){
    Wtf.SheetViewer1.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.SheetViewer1,Wtf.Window,{
    id: 'importxls',
    closable: false,
    width: 750,
    height: 600,
    initComponent:function(config){
        Wtf.SheetViewer1.superclass.initComponent.call(this,config);
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
                var mappingWindow = Wtf.getCmp("csvMappingInterface");
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
                closeImportWindow();
            }
        });
        this.buttons = [this.prevButton, this.nextButton, this.cancelButton];
    },

    onRender:function(config){
        Wtf.SheetViewer1.superclass.onRender.call(this,config);
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

                this.prevButton.enable();
                if(response.maxcol==0 || response.maxrow==0){ // Disable next button of no. of row=0 or columns=0
                    this.nextButton.disable();
                } else {
                    this.nextButton.enable();
                }
            }else{
                Wtf.Msg.alert('File Import',msg);
                this.prevButton.disable();
                this.nextButton.disable();
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
            if(this.cursheet&&this.cursheet==rec.get('index')){
                this.prevButton.enable();
                return;
            }
            this.shdgrid.disable();
            this.sheetIndex = rec.get('index');
            Wtf.Ajax.request({
                method: 'POST',
                url: "ImportRecords/importRecords.do?do=getXLSData",
    //            url: 'XLSDataExtractor',
                params:{
                    filename:this.xlsfilename,
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
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        var mask=new Wtf.LoadMask(Wtf.getBody(), {
            msg:"Please wait..."
        });
        mask.show();
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
                mask.hide();
                this.getMappingInterface();
                WtfGlobal.resetAjaxTimeOut();
            },
            failure: function(res, req){
                mask.hide();
                this.getMappingInterface();
                WtfGlobal.resetAjaxTimeOut();
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
        this.mappingParams.updateExistingRecordFlag = this.updateExistingRecordFlag;
        this.mappingParams.createUomSchemaTypeFlag = this.createUomSchemaTypeFlag;
        this.mappingParams.deleteExistingUomSchemaFlag = this.deleteExistingUomSchemaFlag;

       Wtf.callMappingInterface(this.mappingParams, Wtf.getCmp("importxls"));
       Wtf.getCmp("importxls").hide();
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
function closeImportWindow(){
    destroyWindow("importwindow");
    destroyWindow("importxls");
    destroyWindow("csvMappingInterface");
    destroyWindow("IWValidationWindow");
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

