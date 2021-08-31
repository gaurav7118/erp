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

Ext.ExportInterfaceWindow=function(config) {
    Ext.ExportInterfaceWindow.superclass.constructor.call(this,config);
};

Ext.extend(Ext.ExportInterfaceWindow, Ext.Window, {
    onRender: function (conf) {
        Ext.ExportInterfaceWindow.superclass.onRender.call(this, conf);
    },
    initComponent : function(config) {
        this.winHeight = 440;
        this.operation = (this.type=="print")?"Print":"Export";
        this.topTitle = this.operation=="Print"?ExtGlobal.getLocaleText("acc.rem.28"):(this.type=="pdf"?ExtGlobal.getLocaleText("acc.common.exportToPDF"):(this.type=="xlsx"||this.type=="detailedXls")?ExtGlobal.getLocaleText("acc.common.exportToXLSX"):ExtGlobal.getLocaleText("acc.common.exportToCSV") );  //"Export "+this.type+" file ";
        this.opt = this.operation == "Print" ? ExtGlobal.getLocaleText("acc.rem.28") : ExtGlobal.getLocaleText("acc.rem.132") + " " + (this.type == "detailedXls" ? WtfGlobal.getLocaleText("acc.rem.260") : this.type) + ".";

        var btnArr=[];
        
        btnArr.push({
            header: 'Column',  //"Column",
            sortable:false,
            dataIndex: "title"
        },{
            header: ExtGlobal.getLocaleText("acc.cust.title"),//Title
            dataIndex: "header",
            hidden:true
        },{
            header: ExtGlobal.getLocaleText("acc.field.index"),//index
            dataIndex: "index",
            hidden:true
        },{
            header: ExtGlobal.getLocaleText("acc.field.align"),//align
            dataIndex: "align",
            hidden:true
        },{
            header: ExtGlobal.getLocaleText("acc.exportinterface.width"),//Width
            hidden:((this.type=="pdf")?false:true),
            dataIndex: 'width'
        });
        
        this.exportInCaps =new Ext.form.Checkbox({
            boxLabel: ExtGlobal.getLocaleText("acc.CustomReport.upperCaseCheckBoxLabel"),
            name: 'uppercasechecked',
            style: 'margin:5px;',
            cls : 'custcheckbox',
            width:160
        });

        //Grid Part
        this.colG = Ext.create('Ext.grid.Panel', {
            columns: btnArr,
            selType: 'checkboxmodel',
            viewConfig: {
                deferEmptyText: false,
                forceFit: true,
                emptyText: 'No data Available'
            },
            border: false,
            layout: "fit",
            width : 150,
            height:300,
            forceFit: true,
            frame: true,
            store: this.pdfDs,
            autoScroll: true,
            clicksToEdit: 1
        });
        
        this.headerField = new Ext.form.TextField({
            labelSeparator:'',
            width: 180,
            emptyText:'Empty Text'
        });

        this.colG.on("render", function(obj){
            obj.getSelectionModel().selectAll();
        }, this);
        this.title=this.type=="print"?"Print":"Export",  //this.operation;
        this.iconCls='pwnd deskeraImage';
        this.autoHeight=true;
        this.width= 350;
        this.modal=true;
        this.headerPosition='top';//header at top position
        this.resizable=false;
        this.items= [{
            height: 75,
            border : false,
            cls :'exportFormat1',
            html : getTopHtml(this.topTitle ,this.opt,'../../images/createuser.png',true)
        },{
            cls :'exportFormat1',
            layout: 'fit',
            height:400, 
            width : 338,
            items: [this.colG]
        },this.exportInCaps];
    
        //Previous Button
        this.previousButton=Ext.create('Ext.Button', {
            text:ExtGlobal.getLocaleText("acc.setupWizard.previous"),
            scope:this,
            hidden:((this.type=="pdf")?false:true),
            handler:function() {
                this.hide();
                this.parent.show();
            }
        });
        
        //Export Button of Window
        this.exportColumnsButton=Ext.create('Ext.Button', {
            text: (this.operation == "Print"? ExtGlobal.getLocaleText("acc.common.print"):ExtGlobal.getLocaleText("acc.common.export") ),
            scope: this,
            handler: function() {    
                var selectedBillids="";
                if(this.operation == "Print") {
                    this.headerField.setValue(this.name);
                    var selCol = this.colG.getSelection();    
            
                    if(selCol.length > 0){
                        var header = [];
                        var title = [];
                        var width = [];
                        var indx = [];
                        var align=[];
                        var k = 0;
                        var flag=0;
                
                        for(var i = 0; i < selCol.length; i++) {
                            var recData = selCol[i].data;
                            header.push(recData.header);
                            if(recData.title.indexOf('*')!=-1) {
                                recData.title=recData.title.substring(0,recData.title.length-1);
                            }
                            if(recData.header.indexOf('Custom_')==0 && this.excludeCustomHeaders){
                                title.push(" "); 
                            } else{
                                title.push(encodeURIComponent(recData.title));
                            }
                            width.push(recData.width);
                            indx.push(recData.index);
                            if(recData.align=='')
                                align.push('none');
                            else
                                align.push(recData.align);
                        }
                                                    
                        var docprint=window.open("","",'height=400,width=800');
                        docprint.document.write('<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"');
                        docprint.document.write('</head><style>table {border-collapse: collapse;}table, td, th {border: 1px solid black;}div.scroll {width: 100%; height: 100%; overflow: scroll;} </style><body><div class="scroll"><center>');
                        title=this.config.filename;
                        docprint.document.title=title;
                        docprint.document.write('<p align=center style="font-size:17px" font-family: "Arial"><b>'+title.toString()+'</b></p>');
                        docprint.document.write('<p align=left style="font-size:12px"  font-family: "Arial"><b>Generated On : </b>'+ExtGlobal.getGeneratedOnTimestamp()+" "+'</p>');
                        docprint.document.write('<table><tr>');
                        docprint.document.write('<th height="30"><font face="Arial" size=2;><b>');
                        docprint.document.write("S.No"+'</b>');
                        for(var index=0; index<selCol.length;index++){  //Column Titles
                            var recData1=selCol[index].data;
                            if(recData1.isLineItem!=true)
                            {
                                docprint.document.write('<th height="100"><font face="Arial" size=2;><b> ');
                                
                                docprint.document.write(recData1.title);
                            }
                            docprint.document.write('</b></font></th>');
                        }
                        docprint.document.write('</tr>');
                        for(i=0; i<this.grid.store.data.items.length;i++){  //Column data
                            docprint.document.write('<tr>');
                            docprint.document.write('<td height="25" align="center"><font face="Arial" size=2;>');
                            docprint.document.write(i+1+'</font></td>');
                            for(index=0; index<header.length;index++){
                                var rectData1=selCol[index].data;
                                if(rectData1.header==header[index] && rectData1.isLineItem!=true){
                                    docprint.document.write('<td><font face="Arial" size=2;>');
                                    var val = header[index];
                                    if(this.grid.store.data.items[i].data[val]==undefined){
                                        docprint.document.write("");
                                    } else {
                                        if(Number.isInteger(this.grid.store.data.items[i].data[val])){
                                            docprint.document.write((this.grid.store.data.items[i].data[val]).toFixed(2));
                                        }
                                        else
                                        {
                                            if (this.exportInCaps.getValue()) {
                                                docprint.document.write(this.grid.store.data.items[i].data[val] && (typeof this.grid.store.data.items[i].data[val] == "string") ? this.grid.store.data.items[i].data[val].toUpperCase() : this.grid.store.data.items[i].data[val]);
                                            } else {
                                                docprint.document.write(this.grid.store.data.items[i].data[val]);
                                            }
                                        }
                                    }
                                    docprint.document.write('</font></td>');
                                } 
                            }
                            docprint.document.write('</tr>');
                        }     
                          
                        docprint.document.write('</table></center>');
                        docprint.document.write('<input type="button" value="Print" onClick="window.print()" align="left">');
                        docprint.document.write('</div></body></html>');
                        docprint.document.close();
                    }
                } 
                selCol = this.colG.getSelection();
                if(!this.exportAllRecordsFlag){//if records are selected type change the selcol length
                    selectedBillids= "&billid=" + this.selectedRecordsBillidsParams;
                } else if (this.exportAllRecordsFlag && (this.moduleId == Ext.Acc_Debit_Note_ModuleId || this.moduleId == Ext.Acc_Credit_Note_ModuleId)) {
                    selectedBillids= "&billid=" + this.selectedRecordsBillidsParams;
                }
                
                if(selCol.length > 0){
                    var header = [];
                    var title = [];
                    var width = [];
                    var indx = [];
                    var align=[];
                    var k = 0;
                    var flag=0;
                
                    for(var i = 0; i < selCol.length; i++) {
                        var recData = selCol[i].data;
                        if(this.moduleId != Ext.Acc_Debit_Note_ModuleId && this.moduleId != Ext.Acc_Credit_Note_ModuleId) {
                            header.push(recData.header);
                        } else {
                            if(recData.title == Ext.Notes_Line_Items.Serial_Number) {
                                header.push("S.No.");
                            } else if(recData.title == Ext.Notes_Line_Items.Account && recData.isLineItem ==true) {
                                header.push("accountname");
                            } else if(recData.title == Ext.Notes_Line_Items.Type && recData.isLineItem ==true) {
                                header.push("debit");
                            } else if(recData.title == Ext.Notes_Line_Items.Tax_Percent && recData.isLineItem ==true) {
                                header.push("taxpercent");
                            } else if(recData.title == Ext.Notes_Line_Items.Tax_Amount && recData.isLineItem ==true) {
                                header.push("taxamount");
                            } else if(recData.title == Ext.Notes_Line_Items.Amount && recData.isLineItem ==true) {
                                header.push("totalamount");
                            } else if(recData.title == Ext.Notes_Line_Items.Description && recData.isLineItem ==true) {
                                header.push("description");
                            } else if(recData.isLineItem ==true && recData.custom =="true" ) {
                                header.push("Custom_"+recData.title);
                            } else if(recData.title == Ext.Notes_Line_Items.Invoice_Number  && recData.isLineItem ==true) {
                                header.push("transectionno");
                            } else if(recData.title == Ext.Notes_Line_Items.Creation_Date && recData.isLineItem ==true) {
                                header.push("invcreationdate");
                            } else if(recData.title == Ext.Notes_Line_Items.Due_Date && recData.isLineItem ==true) {
                                header.push("invduedate");
                            } else if(recData.title == Ext.Notes_Line_Items.Linking_Date && recData.isLineItem ==true) {
                                header.push("grlinkdate");
                            } else if(recData.title == Ext.Notes_Line_Items.Invoice_Amount && recData.isLineItem ==true) {
                                header.push("invamount");
                            } else if(recData.title == Ext.Notes_Line_Items.Amount_Due && recData.isLineItem ==true) {
                                header.push("invamountdue");
                            } else {
                                header.push(recData.header);
                            }
                        }
                        //                    if(recData.title.indexOf('(')!=-1) {
                        //                        recData.title=recData.title.substring(0,recData.title.indexOf('(')-1);
                        //                    }
                        if(recData.title.indexOf('*')!=-1) {
                            recData.title=recData.title.substring(0,recData.title.length-1);
                        }
                        if(recData.header.indexOf('Custom_')==0 && this.excludeCustomHeaders){
                            title.push(" "); 
                        } else{
                            title.push(encodeURIComponent(recData.title));
                        }
                        width.push(recData.width);
                        indx.push(recData.index);
                        if(recData.align=='')
                            align.push('none');
                        else
                            align.push(recData.align);
                    }
                    k = indx.length;
                    for(i = 0; i < k; i++) {   //sort based on index
                        for(var j = i+1; j < k; j++) {
                            if(indx[i] > indx[j]) {
                                var temp = header[i];
                                header[i] = header[j];
                                header[j] = temp;

                                temp = title[i];
                                title[i] = title[j];
                                title[j] = temp;

                                temp = width[i];
                                width[i] = width[j];
                                width[j] = temp;

                                temp = align[i];
                                align[i] = align[j];
                                align[j] = temp;
                            }
                        }
                    }
                    if(this.type == "pdf") {
                        var max = Math.floor(820/k);  //820 = total width of pdf page
                        if(k >= (this.pdfDs.getTotalCount()*0.75)) {
                            max = 150;
                        }
                        max=Math.round(max);
                        for(i = 0; i < selCol.length; i++) {
                            if(selCol[i].data["width"] > max) {
                                flag = 1;
                            }
                        }
                        if(flag == 1) {
                            flag = 1;
                            Ext.MessageBox.alert(ExtGlobal.getLocaleText("acc.field.Caution"),ExtGlobal.getLocaleText("acc.field.Themaximumwidthforfieldsis")+max);
                        }
                    }
                    if(flag == 0) {
                        this.close();
                        this.extra=Ext.urlEncode(this.extra);
                        if(this.extra.length>0)
                            this.extra="&"+this.extra;
                        var exportUrl = getExportUrlForCustomReportBuilder(this.reportId);
                        var url = exportUrl+"?";
                        var reportParams="&customReportBuilderFlag=true&reportID="+this.reportId+"&deleted=false&nondeleted=false&isLeaseFixedAsset=false&pendingapproval=false&showRowLevelFieldsflag=false&moduleId="+this.moduleId;
                        var dateParams="";
                        if(this.reportId=="outstanding_consolidated_report"){
                            var startDate=ExtGlobal.convertToGenericStartDate(this.startdate);
                            var endDate=ExtGlobal.convertToGenericEndDate(this.enddate);
                            dateParams="&startdate="+startDate+"&enddate="+endDate+"&curdate="+endDate+"&asofdate="+endDate;
                            this.extra +="&isAged=true";
                        } else{
                            dateParams="&fromDate="+ExtGlobal.convertToGenericDate(this.startdate)+"&toDate="+ExtGlobal.convertToGenericDate(this.enddate); 
                        }
                        
                        if ( this.type != "pdf") {  
                            var parameters = this.mode +reportParams+dateParams+selectedBillids+"&config=" + this.configstr + "&filename=" + encodeURIComponent(this.filename) + "&filetype=" + this.type +"&deleted=false&type=" +this.type
                            + "&header=" + header + "&title=" + title + "&width=" + width + "&get=" + this.get + "&align=" + align + this.paramstring + "&excludeCustomHeaders=" + (this.excludeCustomHeaders ? true:false)+ this.extra ;
                        } else {
                            var parameters = this.mode + "&config=" + this.configstr + "&filename=" + encodeURIComponent(this.filename) + "&filetype=" + this.type + "&accountid=" + this.accountid + "&nondeleted=" + this.nondeleted + "&deleted=" + this.deleted + "&type=" +this.type 
                            + "&header=" + header + "&title=" + title + "&width=" + width + "&get=" + this.get + "&align=" + align + this.paramstring + "&gridconfig=" + this.gridConfig + "&excludeCustomHeaders=" + (this.excludeCustomHeaders ? true:false) + "&moduleId=" + (this.moduleId != undefined ? this.moduleId:'')+this.extra;
                        }
                            
                        if(this.ss!=undefined && this.ss!="") {
                            parameters += "&ss="+this.ss;
                        }
                        
                        if(this.ewayFilter){
                            parameters += "&ewayFilter="+this.ewayFilter;
                        }
                        if(this.searchJson){
                            parameters += "&searchJson="+this.searchJson;
                        }
                        if(this.isreportloaded){
                            parameters += "&isreportloaded="+this.isreportloaded;
                        }
                        if(this.filter){
                            parameters += "&filter="+this.filter;
                        }
                        
                        parameters += "&exportInCaps="+this.exportInCaps.getValue();
                        var resultStr=removeDuplicateParameters(parameters);
                        //url+=resultStr;
                        //Ext.get('downloadframe').dom.src = url;  
                        //console.log(resultStr);
                        postData(url,resultStr)
                    }
                } else {
                    Ext.CustomMsg('Error',  ExtGlobal.getLocaleText("acc.field.Selectatleastonecolumntodisplay"), Ext.Msg.ERROR);
                }  
            }
        });
        
        //Cancel Button
        this.cancelButton=Ext.create('Ext.Button', {
            text: 'Cancel',  //"Cancel",
            scope:this,
            handler: function(){
                this.close();
            }
        });
        
        this.buttons= [this.previousButton,this.exportColumnsButton,this.cancelButton];
        Ext.ExportInterfaceWindow.superclass.initComponent.call(this,config);
    }
});

Ext.exportReportButton=function(config){
    Ext.apply(this, config);
    var mnuBtns=[];
    this.id=config.id;
    //    this.filename = encodeURIComponent(Ext.getCmp('as').getActiveTab().title);
    this.filename = "";
    this.moduleId = config.moduleId;
    var menubutton;

    //if(config.menuItem.csv==true){
    //        this.isPDF = false;
    //        this.isCSV = true;
    //        this.isXLS = false;
    //        mnuBtns.push(this.createButton("csv",config.obj,config.get));
    //    }
    if(config.text!="Print"){
        if(config.menuItem.xlsx==true){
            this.isPDF = false;
            this.isCSV = false;
            this.isXLS = true;
            mnuBtns.push(this.createButton("xlsx",config,config.get));
        }
        menubutton= Ext.create('Ext.menu.Menu', {
            width: 150,
            //        margin: '0 0 10 0',
            plain: true,
            floating: true,
            //        dock:'right',// usually you want this set to True (default)
            items: mnuBtns
        });
    }
    else{
        menubutton= Ext.create('Ext.Button');
           
    }
    //    if(config.menuItem.pdf==true){ 
    //        this.isPDF = true;     
    //        this.isCSV = false;
    //        this.isXLS = false;
    //        mnuBtns.push(this.createButton("pdf",config,config.get));
    //    }

  
    if(menubutton.xtype=="button"){
        var mainConfig={
            button:  menubutton
        };
    }
    else
    {
        var mainConfig={
            menu:  menubutton
        };
    }
    Ext.apply(this,mainConfig,config);
    Ext.exportReportButton.superclass.constructor.call(this,config);
}

Ext.extend(Ext.exportReportButton,Ext.Button,{
    onRender: function (conf) {
        Ext.exportReportButton.superclass.onRender.call(this, conf);
    },
    exportwithCsvXlsx:function(type,config,get,exportAllRecordsFlag){
        var obj=config.obj;
        var columns="";
        var reportId =obj.reportId;
        var filename = obj.title;
        var EwayExport;
        var ewayFilter;
        var searchJson,isreportloaded,filter;
        if(obj.title.indexOf("Report") == -1 && obj.title.indexOf("report") == -1){
            filename = filename + " " +ExtGlobal.getLocaleText("acc.1099.tabTitle").replace("1099","");
        }
        obj.pdfStore =new Ext.data.Store({});
        //obj.pdfStore=this.filPdfStore(obj,obj.reportGrid.getColumns(),type);
        var paramsString = '';
        if(this.params){
            for(var index in this.params) {
                paramsString += "&"+index +"="+this.params[index]+"";
            }
        }
        if (obj.isEWayReport) {
            ewayFilter = obj.statusFilter.getValue();
        } else {
            ewayFilter = " ";
        }
        if(obj.reportStore.proxy.extraParams.searchJson){
            searchJson=obj.reportStore.proxy.extraParams.searchJson;
        }
        if(obj.reportStore.proxy.extraParams.isreportloaded){
            isreportloaded=obj.reportStore.proxy.extraParams.isreportloaded;
        }
        if(obj.reportStore.proxy.extraParams.filter){
            filter=obj.reportStore.proxy.extraParams.filter;
        }
        Ext.Ajax.request({
            url: 'ACCCreateCustomReport/executeCustomReport.do',
            method: 'POST',
            reader: {
                type: 'json',
                rootProperty: "data",
                keepRawData: true,
                totalProperty: 'totalCount'
            },
            async: false,
            scope:this,
            params: {
                reportID: reportId,
                deleted: false,
                nondeleted: false,
                pendingapproval: false,
                showRowLevelFieldsflag: true,
                forExport: true,
                ewayFilter: ewayFilter,
                searchJson: searchJson,
                filter: filter,
                isreportloaded:isreportloaded
            },
            success: function(res, req) {
                var resObj = eval("(" + res.responseText + ")");
                columns=resObj.columns;
                
            }
           
        })
        var jsonGrid =this.genJsonForPdf(obj,columns);
        obj.pdfStore=this.filPdfStore(obj,obj.reportGrid.getColumns(),columns, type);
        
        //var jsonGrid = this.genJsonForPdf(obj,request.currentTarget.response);
        if(!exportAllRecordsFlag){//if records are selected type change the selcol length
            var selection = obj.reportGrid.getSelectionModel().getSelected().items;
            if(selection.length>0){
                var selectedRecordsStr = "";
                for ( var index = 0 ; index < selection.length; index++ ) {
                    if(index ==selection.length-1){
                        selectedRecordsStr +=selection[index].data.billid;
                    }else{
                        selectedRecordsStr +=selection[index].data.billid+",";
                    }
                }
            }else{
                Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"),ExtGlobal.getLocaleText("acc.common.norecordshasbeenselected"),Ext.Msg.INFO);
                this.close();
                return;
            }
        }
        if (exportAllRecordsFlag && (this.moduleId == Ext.Acc_Debit_Note_ModuleId || this.moduleId == Ext.Acc_Credit_Note_ModuleId)) {
            var allRecordsStr = "";
            for (var index = 0; index < obj.reportGrid.store.data.items.length; index++) {
                if (index == obj.reportGrid.store.data.items.length - 1) {
                    allRecordsStr += obj.reportGrid.store.data.items[index].data.billid;
                } else {
                    allRecordsStr += obj.reportGrid.store.data.items[index].data.billid + ",";
                }
            }
            selectedRecordsStr = allRecordsStr;
        }
        if(Ext.getCmp("selectexportwinCsvXlsx")==undefined){
            var expt =new Ext.ExportInterfaceWindow({
                type:type,
                get:get,
                deleted:false,
                name:this.params?(this.params.name!=""?this.params.name:""):"",
                id:"selectexportwinCsvXlsx",
                paramstring:paramsString,
                mode:Ext.urlEncode(obj.reportGrid.getStore().baseParams),
                filename:filename,
                gridConfig : jsonGrid,
                grid:obj.reportGrid,
                pdfDs:obj.pdfStore,
                reportId:obj.reportId,
                startdate:obj.fromDate.getValue(),
                enddate:obj.toDate.getValue(),
                ewayFilter :ewayFilter,
                searchJson: searchJson,
                isreportloaded:isreportloaded,
                filter:filter,
                extra:this.extra||{},
                moduleId:this.moduleId,
                exportAllRecordsFlag:exportAllRecordsFlag,
                selectedRecordsBillidsParams:selectedRecordsStr//to export selected records
            });
            expt.show();
        }
    },
    createButton:function(type,config,get){
        var btn="";
        if(type == "xlsx"){//XLSX
            
            var xlsxMenuButtonArray=[];
            if(config.reportId!="outstanding_consolidated_report"){
                xlsxMenuButtonArray.push({
                    iconCls: 'pwnd exportcsv',
                    text:ExtGlobal.getLocaleText("acc.common.exportXLSXSelectedRecord"),
                    scope:this,
                    handler: function() {
                        this.exportwithCsvXlsx(type,config,get,false);
                    }
                });
            }
            xlsxMenuButtonArray.push({
                iconCls: 'pwnd exportcsv',
                text:ExtGlobal.getLocaleText("acc.common.exportXLSXAllRecord"),
                scope:this,
                handler: function() {
                    this.exportwithCsvXlsx(type,config,get,true);
                }
            })
                 
            var xlsxBtnsMenu= Ext.create('Ext.menu.Menu', {
                width: 180,
                plain: true,
                floating: true,
                items: xlsxMenuButtonArray
            });
            
            btn= {
                text: ExtGlobal.getLocaleText("acc.common.exportToXLSX"),
                iconCls: 'pwnd exportcsv',
                scale: 'small',
                scope:this,
                menu:xlsxBtnsMenu
            }
        }//END OF XLSX 
        
        if(type == "pdf"){//PDF
            btn= {
                text: 'Export to PDF File',
                iconCls: 'pwnd exportpdf',
                //                id: "exportpdf"+this.id,
                scale: 'small',
                scope:this,
                handler: function() {
                    alert('You clicked the PDF button!');
                }
            }
        }
        
        //        if(type == "csv"){//CSV
        //            btn= {
        //                text: 'Export to CSV File',
        //                iconCls: 'pwnd exportcsv',
        //                scale: 'small',
        //                  scope:this,
        //                handler: function() {
        //                    
        ////                    alert('You clicked the CSV button!');
        //                    this.exportwithCsvXlsx(type,config,get);
        //                }
        //            }
        //        }
        return btn;
    },
    

    filPdfStore:function(obj,column,lineColumns, type)
    {
        var start=1;
        var k=1;
        if(this.get==1){
            start=3;
        }         
        
        if(type=="print"){
            lineColumns = undefined;
        }
        for(var i=start ; i<column.length ; i++) { // skip row numberer
            var format="";
            var title;
            var header;
            if(column[i].pdfheader!=undefined)
                header=column[i].pdfheader;
            else 
                header= column[i].text;
            if(column[i].title==undefined)
                title=column[i].dataIndex;
            else
                title=column[i].title;
            obj.newPdfRec = new Ext.data.Record({
                header : title,
                title : header,
                width : column[i].getWidth(),
                align : format,
                isLineItem:false,
                index : k
            });
            obj.pdfStore.insert(obj.pdfStore.getCount(), obj.newPdfRec);
            k++;
        }   
        //For linelevel header
        if(lineColumns!=undefined){
            var format="";
            var title ="S.No.";
            var header = "S.No.";
            obj.newPdfRec = new Ext.data.Record({
                header : title,
                title : header,
                width : 100,
                align : format,
                isLineItem:true,
                index : k
            });
            obj.pdfStore.insert(obj.pdfStore.getCount(), obj.newPdfRec);
            k++;
            start=0;
            for(var i=start ; i<lineColumns.length ; i++) { 
                var format="";
                var title;
                var header;
                if(lineColumns[i].displayName!=undefined)
                    header=lineColumns[i].displayName;
                else 
                    header= lineColumns[i].defaultHeader;
                if(lineColumns[i].dataIndex==undefined)
                    title=lineColumns[i].id;
                else
                    title=lineColumns[i].dataIndex;
                obj.newPdfRec = new Ext.data.Record({
                    header : title,
                    title : header,
                    width : lineColumns[i].width,
                    align : format,
                    isLineItem:lineColumns[i].isLineItem,
                    custom:lineColumns[i].custom,
                    index : k
                });
                obj.pdfStore.insert(obj.pdfStore.getCount(), obj.newPdfRec);
                k++;
            }  
        }
        return obj.pdfStore;
    },
    genJsonForPdf:function(obj,columns){
        var jsondata = [];
        var reportId =obj.reportId
        for(var i=0;i<obj.pdfStore.getCount();i++) {
            var recData = obj.pdfStore.getAt(i).data;
            if(recData.align=="right" && recData.title.indexOf("(")!=-1) {
                recData.title=recData.title.substring(0,recData.title.indexOf("(")-1);
            }
            if(this.get==116){// changed title for trial balance
                var headersplit=recData.header.split("_");
                var recTitle=recData.title;
                var temporary = {
                    header:recData.header,
                    title:encodeURIComponent(recTitle),
                    width:recData.width,
                    align:recData.align
                };
                jsondata.push(temporary);
            } else{
                var temp = {
                    header:recData.header,
                    title:encodeURIComponent(recData.title),
                    width:recData.width,
                    align:recData.align,
                    isLineItem:false
                };
                jsondata.push(temp);
            }
        }
        if(columns!=undefined){
            for(var i=0;i<columns.length;i++ )  {   //if row level column exist
                // header=setRowExpanderHTML(columns,records);
                var temp = {
                    header:columns[i].defaultHeader,
                    title:encodeURIComponent(columns[i].displayName),
                    width:columns[i].width,
                    align:"left",
                    isLineItem:columns[i].isLineItem,
                    custom:columns[i].custom
                };
                jsondata.push(temp);
            }
        }
        return Ext.encode({
            data:jsondata
        });
    }
});
