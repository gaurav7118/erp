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
Wtf.ExportInterface=function(config) {
    Wtf.ExportInterface.superclass.constructor.call(this,config);
};

Wtf.extend(Wtf.ExportInterface, Wtf.Window, {

    initComponent : function(config) {
        this.winHeight = 440;
        if(Wtf.isIE6)
            this.winHeight = 413;

        this.operation = (this.type=="print")?"Print":"Export";
        this.topTitle = this.operation=="Print"?WtfGlobal.getLocaleText("acc.rem.28"):(this.type=="pdf"?WtfGlobal.getLocaleText("acc.common.exportToPDF"):(this.type=="xls"||this.type=="detailedXls")?WtfGlobal.getLocaleText("acc.common.exportToxls"):WtfGlobal.getLocaleText("acc.common.exportToCSV") );  //"Export "+this.type+" file ";
        this.opt = this.operation == "Print" ? WtfGlobal.getLocaleText("acc.rem.28") : WtfGlobal.getLocaleText("acc.rem.132") + " " + (this.type == "detailedXls" ? WtfGlobal.getLocaleText("acc.rem.260") : this.type) + ".";

        /* ERP-34368
         * The checkbox Do not include Account Code with Account Name is not use 
         * Removing The code related to checkbox
         */
//        this.accountCodeCheckBox= new Wtf.form.Checkbox({
//            name:'accountCodeCheckBox',
//            boxLabel:WtfGlobal.getLocaleText("acc.field.DonotincludeAccountCodewithAccountName"),  
//            checked:false,    
//            style: 'margin:5px;',
//            width: 10
//        });

        this.exportAllcolumn= new Wtf.form.Checkbox({
            boxLabel:WtfGlobal.getLocaleText("acc.export.boxLabel.Other.than.above"),
            disabled:this.isDefaultCustomerList || this.isInactiveCustomerListReport || !(this.get==113 || this.get==114 || this.get==198 || this.get==112 || this.get==1110), // disable other than customer,vendor, Account and product and disable in case of 'Default Customer List' Report
            name:'exportallcolumn',
            id:this.id+'exportallcolumn', 
            style: 'margin:5px;',
            cls : 'custcheckbox',
            width: 10
        });

        this.colSM = new Wtf.grid.CheckboxSelectionModel({
            width: 25
        });
        this.colCM = new Wtf.grid.ColumnModel([ this.colSM,{
            header: WtfGlobal.getLocaleText("acc.rem.29"),  //"Column",
                dataIndex: "title"
        },{
                header: WtfGlobal.getLocaleText("acc.cust.title"),
                dataIndex: "header",
            hidden:true
        },{
                header: WtfGlobal.getLocaleText("acc.field.index"),
                dataIndex: "index",
            hidden:true
        },{
                header: WtfGlobal.getLocaleText("acc.field.align"),
                dataIndex: "align",
            hidden:true
        },{
                header: WtfGlobal.getLocaleText("acc.exportinterface.width"),
            hidden:((this.type=="pdf")?false:true),
                dataIndex: 'width',
                editor: new Wtf.form.NumberField({
                    allowBlank: false,
                    maxValue: 850,
                    minValue: 50
                })
            }]);
        this.headerField = new Wtf.form.TextField({
            labelSeparator:'',
            width: 180,
            emptyText: mainPanel.getActiveTab().title
        });
        this.colG = new Wtf.grid.EditorGridPanel({
            store: this.pdfDs,
            border: false,
            layout: "fit",
            width : 328,
//            height:280,
            viewConfig: {
                forceFit: true
            },
            cm: this.colCM,
            autoScroll: true,
            clicksToEdit: 1,
            sm: this.colSM
        });
        this.colG.on("render", function(obj) {
            obj.getSelectionModel().selectAll();
            for (var i = 0; i < obj.getSelectionModel().selections.length; i++) {
                if (obj.getSelectionModel().selections.items[i].data.defaultselectionunchk != undefined && obj.getSelectionModel().selections.items[i].data.defaultselectionunchk) {
                    obj.getSelectionModel().deselectRow(i);
                }
            }

        }, this);
        this.title=this.type=="print"?WtfGlobal.getLocaleText("acc.common.print"):WtfGlobal.getLocaleText("acc.common.export"),  //this.operation;
        this.iconCls='pwnd deskeraImage';
        this.autoHeight=true;
        this.width= 350;
        this.modal=true;
        this.layout="table";
        this.layoutConfig= {
            columns: 1
        };
        this.resizable=false;
        this.items= [{
                height: 75,
            border : false,
            cls :'exportFormat1',
            html : getTopHtml(this.topTitle ,this.opt,'../../images/createuser.png',true)
        },{
            cls :'exportFormat1',
                layout: 'fit',
            height:((this.get!=112) || (this.get==112 && this.filename=="Fixed%20Assets."))?300:250, //full height of this region except for chart of accounts export 
            width : 338,
                items: [this.colG]
        },{            
            cls :'exportFormat1',
                style: 'background:none repeat scroll 0 0 transparent;',
            border:false,
            hidden : ((this.get!=112) || (this.get==112 && this.filename=="Fixed%20Assets.") || this.type=="print"), //show this region only for chart of accounts export 
            layout:'fit'
//            height:40           //ERP-34368                                                                                                                                                                                                                                                                                                                   
//            items:[this.accountCodeCheckBox] //ERP-34368
            }];
        if(this.type!="print" && this.type!="pdf"){//Other than above is disabled for PDF ERP-36724
            this.bbar= ["  ",this.exportAllcolumn];
        }
        this.buttons= [{
            text:WtfGlobal.getLocaleText("acc.setupWizard.previous"),
            scope:this,
            hidden:((this.type=="pdf")?false:true),
            handler:function() {
                    this.hide();
                    this.parent.show();
                }
        },{
            text: (this.operation == "Print"? WtfGlobal.getLocaleText("acc.common.print"):WtfGlobal.getLocaleText("acc.common.export") ),
                scope: this,
            handler: function() {                 
                if(this.exportAllcolumn.getValue()){
                    if(this.compStoreParams){
                        if(this.compStoreParams.length>0) {
                            this.paramstring += "&"+this.compStoreParams;
                        }
                    }
                    var isProductExportByPOST=false;
                    if(this.isProductExport !=undefined && this.isProductExport && this.isProductExportRecordsGreaterThanThousands != undefined && this.isProductExportRecordsGreaterThanThousands){
                        isProductExportByPOST=true; // passing this flag currently for Product report,Sales by item Report if record count is grater than 1000
                        }
                    WtfGlobal.exportAllData(this.get,this.filename,this.type,undefined,undefined,isProductExportByPOST,this.totalRecords,this.paramstring);
                        this.close();
                }else{
                    if(this.operation == "Print") {
                            this.headerField.setValue(this.name);
                        }
                        var selCol = this.colSM.getSelections();
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
                                if (this.get === Wtf.autoNum.profitAndLossMonthlyCustomLayout) {//If Report is Monthly Custom Layout, then set align as 'withoutcurrency' for amounts
                                    if (recData.title.replace(/(<([^>]+)>)/ig, "") == WtfGlobal.getLocaleText("acc.balanceSheet.particulars") || recData.title.replace(/(<([^>]+)>)/ig, "") == WtfGlobal.getLocaleText("acc.coa.accCode")) {
                                        align.push('none');
                                    } else {
                                        align.push('withoutcurrency');
                                    }
                                }
                                else if (recData.align == '')
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
                                Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.field.Caution"),WtfGlobal.getLocaleText("acc.field.Themaximumwidthforfieldsis")+max);
                                }
                            }
                        if(flag == 0) {
                                this.close();
                            this.extra=Wtf.urlEncode(this.extra);
                            if(this.extra.length>0)
                                this.extra="&"+this.extra;
                            if(this.compStoreParams){
                                if(this.compStoreParams.length>0) {
                                    this.extra += "&"+this.compStoreParams;
                                    }
                                }
                                var exportUrl = getExportUrl(this.get, this.consolidateFlag);

                            if(this.get == Wtf.autoNum.Dimension_Based_TrialBalance || this.get == Wtf.autoNum.Dimension_Based_BalanceSheet || this.get == Wtf.autoNum.Dimension_Based_TradingAndProfitLoss){ 
                                    this.gridConfig = "";
                                    this.configstr = "";
                                    title = "";
                                }

                            var url = exportUrl+"?";
                                var urlExcludingParams = "";
                                var paramsWithoutURL = "";
                            var threadflag=Wtf.account.companyAccountPref.downloadDimPLprocess=="1"?true:false;
//                            var paramaters = this.mode+this.extra+"&config="+this.configstr+"&filename="+encodeURIComponent(this.filename)+"&filetype=" + this.type+"&stdate="+this.stdate+"&enddate="+this.enddate+"&accountid="+this.accountid+"&nondeleted="+this.nondeleted+"&deleted="+this.deleted+"&type="+ (this.locationType!=undefined?this.locationType:((this.type=="detailedXls")?this.type+"&dtype=report":''))   //If LocationType is undefined, then sent empty string.
                                if (this.usePostMethod) {   // encodeURIComponent is removed if usePostMethod is true as POST request by defaults encode  All characters  before sent
                                    if (this.type != "pdf") {
                                        if (this.get == Wtf.autoNum.consolidatioBalanceSheetReport || this.get == Wtf.autoNum.consolidatioReport) { 
                                            var paramaters = this.mode + this.extra + "&config=" + this.configstr + "&filename=" + (this.filename) + "&filetype=" + this.type + "&accountid=" + this.accountid + "&nondeleted=" + this.nondeleted + "&deleted=" + this.deleted + "&type=" + (this.locationType != undefined ? this.locationType : ((this.type == "detailedXls") ? this.type + "&dtype=report" : ''))   //If LocationType is undefined, then sent empty string.
                                                + "&header=" + header + "&title=" + (title) + "&width=" + width + "&get=" + this.get + "&align=" + align + this.paramstring + "&excludeCustomHeaders=" + (this.excludeCustomHeaders ? true : false) + "&moduleId=" + (this.moduleId != undefined ? this.moduleId : '');
                                        paramaters = decodeURIComponent(paramaters);
                                        }else{
                                    var paramaters = this.mode + this.extra + "&config=" + this.configstr + "&filename=" + (this.filename) + "&filetype=" + this.type + "&accountid=" + this.accountid + "&nondeleted=" + this.nondeleted + "&deleted=" + this.deleted + "&type=" + (this.locationType != undefined ? this.locationType : ((this.type == "detailedXls") ? this.type + "&dtype=report" : ''))   //If LocationType is undefined, then sent empty string.
                                                + "&header=" + header + "&title=" + (title) + "&width=" + width + "&get=" + this.get + "&align=" + align + this.paramstring + "&excludeCustomHeaders=" + (this.excludeCustomHeaders ? true : false) + "&moduleId=" + (this.moduleId != undefined ? this.moduleId : '');
                                    }
                                    } else {
                                    var paramaters = this.mode + this.extra + "&config=" + this.configstr + "&filename=" + (this.filename) + "&filetype=" + this.type + "&accountid=" + this.accountid + "&nondeleted=" + this.nondeleted + "&deleted=" + this.deleted + "&type=" + (this.locationType != undefined ? this.locationType : ((this.type == "detailedXls") ? this.type + "&dtype=report" : ''))   //If LocationType is undefined, then sent empty string.  
                                                + "&header=" + header + "&title=" + (title) + "&width=" + width + "&get=" + this.get + "&align=" + align + this.paramstring + "&gridconfig=" + this.gridConfig + "&excludeCustomHeaders=" + (this.excludeCustomHeaders ? true : false) + "&moduleId=" + (this.moduleId != undefined ? this.moduleId : '');
                                    }
                                } else {
                                    if (this.type != "pdf") {
                                    var paramaters = this.mode + this.extra + "&config=" + this.configstr + "&filename=" + encodeURIComponent(this.filename) + "&filetype=" + this.type + "&accountid=" + this.accountid + "&nondeleted=" + this.nondeleted + "&deleted=" + this.deleted + "&isExport=true&type=" + (this.locationType != undefined ? this.locationType : ((this.type == "detailedXls") ? this.type + "&dtype=report" : ''))   //If LocationType is undefined, then sent empty string.  //ERP-31550:isExport flag has added
                                                + "&header=" + header + "&title=" + encodeURIComponent(title) + "&width=" + width + "&get=" + this.get + "&align=" + align + this.paramstring + "&excludeCustomHeaders=" + (this.excludeCustomHeaders ? true : false) + "&moduleId=" + (this.moduleId != undefined ? this.moduleId : '');
//                            
                                    } else {
                                    var paramaters = this.mode + this.extra + "&config=" + this.configstr + "&filename=" + encodeURIComponent(this.filename) + "&filetype=" + this.type + "&accountid=" + this.accountid + "&nondeleted=" + this.nondeleted + "&deleted=" + this.deleted + "&type=" + (this.locationType != undefined ? this.locationType : ((this.type == "detailedXls") ? this.type + "&dtype=report" : ''))   //If LocationType is undefined, then sent empty string.  
                                                + "&header=" + header + "&title=" + encodeURIComponent(title) + "&width=" + width + "&get=" + this.get + "&align=" + align + this.paramstring + "&gridconfig=" + this.gridConfig + "&excludeCustomHeaders=" + (this.excludeCustomHeaders ? true : false) + "&moduleId=" + (this.moduleId != undefined ? this.moduleId : '');
                                    }
                                }

                            if(this.ss!=undefined && this.ss!="") {
                                paramaters += "&ss="+this.ss;
                                }
                                /* ERP-34368
                                 * The checkbox Do not include Account Code with Account Name is not use 
                                 * Removing The code related to checkbox
                                 */
//                            if(this.get==112){   //add parameter only for chart of accounts export
//                                paramaters+="&accountCodeNotAdded="+this.accountCodeCheckBox.checked;
//                                }
                            var resultStr=removeDuplicateParameters(paramaters);
                                urlExcludingParams = url;
                                paramsWithoutURL = resultStr;
                            url+=resultStr;

                            if(this.operation == "Print") {
                                url+="&generatedOnTime="+WtfGlobal.getGeneratedOnTimestamp();
                                    if (this.ispendingApproval!=undefined && this.ispendingApproval!="" && this.ispendingApproval!=false) {
                                        url += "&ispendingAproval=true"
                                    }
                                window.open(url, "mywindow","menubar=1,resizable=1,scrollbars=1");
                                }
                            else {
                                if (this.isProductExport && this.isProductExportRecordsGreaterThanThousands) {
                                    Wtf.Ajax.requestEx({
                                        url: url
                                    }, this,
                                    function () {
                                
                                        }, function () {
                              
                                        });
                                } else if(this.usePostMethod){
                                    WtfGlobal.postData(urlExcludingParams,paramsWithoutURL);
                                } else if(this.get == Wtf.autoNum.Dimension_Based_TradingAndProfitLoss && threadflag && this.type=="xls"){
                                    
                                    Wtf.MessageBox.show({
                                        title: WtfGlobal.getLocaleText("acc.common.info"), //'Warning',
                                        msg: WtfGlobal.getLocaleText("acc.generalLedger.msg.downloadTakeTime"),
                                        buttons: Wtf.MessageBox.YESNOCANCEL,
                                        closable : false,
                                        fn:function(btn){
                                            if(btn=="yes") {
                                                url +="&threadflag="+true;
                                                Wtf.get('downloadframe').dom.src = url;
                                            } else if(btn=="no") {
                                                url +="&threadflag="+false;
                                                Wtf.get('downloadframe').dom.src = url;
                            }
                                        },
                                        scope:this,
                                        icon: Wtf.MessageBox.QUESTION
                                    });
                                } else if(this.get == Wtf.autoNum.GroupDetailReport && this.params.exportThreadFlagLedger && (this.type=="xls" || this.type=="detailedXls")){
                                    
                                    Wtf.MessageBox.show({
                                        title: WtfGlobal.getLocaleText("acc.common.info"), //'Warning',
                                        msg: WtfGlobal.getLocaleText("acc.generalLedger.msg.downloadTakeTime"),
                                        buttons: Wtf.MessageBox.YESNOCANCEL,
                                        closable : false,
                                        fn:function(btn){
                                            if(btn=="yes") {
                                                url +="&threadflag="+true;
                                                Wtf.get('downloadframe').dom.src = url;
                                            } else if(btn=="no") {
                                                url +="&threadflag="+false;
                                                Wtf.get('downloadframe').dom.src = url;
                        }
                                        },
                                        scope:this,
                                        icon: Wtf.MessageBox.QUESTION
                                    });
                                }else{
                                    Wtf.get('downloadframe').dom.src = url;
                                }
                            }
                        }
                        } else {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.Selectatleastonecolumntodisplay")], 1);
                        }
                    }
                }
        },{
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //"Cancel",
            scope:this,
            handler: function(){
                    this.close();
                }
            }];
        Wtf.ExportInterface.superclass.initComponent.call(this,config);
    }
});

Wtf.exportButton=function(config){
    var mnuBtns=[];
    var btn=[];
//    this.filename = encodeURIComponent(Wtf.getCmp('as').getActiveTab().title);
    this.filename = "";
    this.excludeCustomHeaders = config.excludeCustomHeaders;
    if(config.moduleid == undefined){
        config.moduleid = config.moduleId;
    };
    this.moduleId = config.moduleId;
    this.usePostMethod = config.usePostMethod!=undefined?config.usePostMethod:false;
    if (config.ispendingApproval) {
        this.ispendingApproval = config.ispendingApproval
    }
    this.isexportGLConfiguredData=config.menuItem.isexportGLConfiguredData?config.menuItem.isexportGLConfiguredData:false;
    if ( config.obj  && config.obj.title ) {
        this.filename = encodeURIComponent(config.obj.title);
    }
     
    if( config.filename ){
        this.filename = encodeURIComponent(config.filename)
    }
    this.isProductExport = false;
    var isSingleRec = false;
    this.detailedXls = false;      
    var companyIDs="9f931b2d-1a19-44c4-b180-1d801f7ec71c,";
    if(config.menuItem.csv==true){
        this.isPDF = false;
        this.isCSV = true;
        this.isXLS = false;
        this.detailedCSV = (config.menuItem.detailedCSV==true)?true:false; 
        mnuBtns.push(this.createButton("csv",config));
    }
    if(config.menuItem.xls==true){
        this.isPDF = false;
        this.isCSV = false;
        this.isXLS = true;
        this.detailedXls = (config.menuItem.detailedXls==true)?true:false
        mnuBtns.push(this.createButton("xls",config));
    }
    if(config.menuItem.pdf==true){ 
        this.isPDF = true;        
        mnuBtns.push(this.createButton("pdf",config));
    }
    if(config.menuItem.subMenu==true){  //This Menu is used in GL,AR,AP,BB,CB,SOA,Stock Ledger,Stock Ageing Report for Export to PDF File
        this.isPDF = false;
        this.isCSV = false;
        this.isXLS = false;
        mnuBtns.push(this.createButton("subMenu",config));
    }
        
    if(config.menuItem.summarySubMenu==true){  //This Menu is used in GL,AR,AP,BB,CB,SOA,Stock Ledger,Stock Ageing Report for Export to PDF File
        this.isPDF = false;
        this.isCSV = false;
        this.isXLS = false;
        mnuBtns.push(this.createButton("summarySubMenu",config));
    }    
    if(config.menuItem.subMenu1==true){ //This Menu is used SOA -Export to PDF-Customer/Vendor Currency
        this.isPDF = false;
        this.isCSV = false;
        this.isXLS = false;
        mnuBtns.push(this.createButton("subMenu1",config));
    }   
    if(config.menuItem.subMenu2==true){ //This Menu is used SOA -Export to PDF-Customer/Vendor Currency - Sort on Date
        this.isPDF = false;
        this.isCSV = false;
        this.isXLS = false;
        mnuBtns.push(this.createButton("subMenu2",config));
    }
    if(config.menuItem.detailPDF==true && (Wtf.templateflag == Wtf.Monzone_templateflag)){
        this.isPDF = false;
        this.isCSV = false;
        this.isXLS = false;
        this.isDetailPDF = true; //Monzone : Sales Invoice Register (Detail)
        this.isSummaryPDF = false;
        this.detailedXls = false;
        mnuBtns.push(this.createButton("detailPDF",config));
    }
    if(config.menuItem.summaryPDF==true && (Wtf.templateflag == Wtf.Monzone_templateflag)){
        this.isPDF = false;
        this.isCSV = false;
        this.isXLS = false;
        this.isSummaryPDF = true; //Monzone : Sales Invoice Register (Summary)
        this.isDetailPDF = false;
        this.detailedXls = false;
        mnuBtns.push(this.createButton("summaryPDF",config));
    }
    if(config.menuItem.CRLetter==true && (config.get==24 ||  config.get==25) && Wtf.templateflag == Wtf.Monzone_templateflag){
        this.CRLetter = true;
        this.isPDF = true;
        this.isCSV = false;
        this.isXLS = false;
        mnuBtns.push(this.createButton("CRLetter",config));//Customer Remainder Letter
    }
     if(config.menuItem.detailedXls==true && config.menuItem.xls != true){
        this.CRLetter = false;
        this.isPDF = false;
        this.isCSV = false;
        this.isXLS = false;
        this.isDetailPDF = false;
        this.isSummaryPDF = false;
        this.detailedXls = true;
        mnuBtns.push(this.createButton("detailedXls",config)); 
    }
    if(config.isProductExport==true){
        this.isProductExport = true;
    }
    this.isProductExportRecordsGreaterThanThousands=false;
    if(config.menuItem.rowPdf==true || config.menuItem.rowPdfPrint){
        isSingleRec = true;
        config.filetype='pdf';
        this.exportMenu = new Wtf.menu.Menu({
            id: "exportmenu" + this.id,
            cls : 'printMenuHeight'
        });
        if(config.get!=Wtf.autoNum.PackingDoList && !((config.get == Wtf.autoNum.DeliveryOrder||config.get==Wtf.autoNum.Invoice||config.get==Wtf.autoNum.PurchaseOrder||config.get==Wtf.autoNum.Quotation || config.get==Wtf.autoNum.GoodsReceiptOrder) && Wtf.templateflag == Wtf.Diamond_Aviation_templateflag)
            && !((config.get==Wtf.autoNum.Invoice||config.get == Wtf.autoNum.DeliveryOrder || config.get == Wtf.autoNum.Quotation) && (Wtf.templateflag == Wtf.F1Recreation_templateflag||Wtf.templateflag == Wtf.F1RecreationLeasing_templateflag)) 
            && !((config.get==Wtf.autoNum.PurchaseReturn||config.get==Wtf.autoNum.SalesReturn||config.get==Wtf.autoNum.DebitNote||config.get==Wtf.autoNum.CreditNote) && (Wtf.templateflag == Wtf.Guan_Chong_templateflag || Wtf.templateflag == Wtf.Guan_ChongBF_templateflag)) //To display multiple buttons in Particular module.
            && !((config.get==Wtf.autoNum.Invoice || config.get==Wtf.autoNum.Quotation) && (Wtf.templateflag == Wtf.RightSpace_templateflag || Wtf.templateflag == Wtf.RightWork_templateflag)) //    ERP-12995 AR Quotation / Invoice Form Templates
            && !((config.get==Wtf.autoNum.PurchaseOrder) && (Wtf.templateflag == Wtf.FastenEnterprises_templateflag || Wtf.templateflag == Wtf.FastenHardwareEngineering_templateflag))
            && !((config.get==Wtf.autoNum.Quotation) && (Wtf.templateflag == Wtf.Monzone_templateflag))
            && !((config.get==Wtf.autoNum.Invoice) && (Wtf.templateflag == Wtf.spaceTec_templateflag))
            && !((config.get==Wtf.autoNum.Invoice) && (Wtf.templateflag >= Wtf.LandPlus_templateflag && Wtf.templateflag <= Wtf.LandPlus_Mobility_templateflag))
            && !((config.get==Wtf.autoNum.Invoice) && (Wtf.templateflag == Wtf.BakerTilly_templateflag_pcs))
            ){
                if(Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA){
                    Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({
                        iconCls: 'pwnd exportpdf',
                        text: (Wtf.templateflag == 1||Wtf.templateflag == 5 )?(config.get==Wtf.autoNum.Invoice?WtfGlobal.getLocaleText("acc.field.SinglePage"):((config.get==Wtf.autoNum.Receipt||config.get==Wtf.autoNum.Payment)?WtfGlobal.getLocaleText("acc.numb.37"):WtfGlobal.getLocaleText("acc.field.DefaultTemplate"))):((Wtf.templateflag == 10 && config.get==Wtf.autoNum.Quotation) ? "Quotation- Sales of Good Sold":(Wtf.templateflag == 15 && config.get==Wtf.autoNum.Invoice)? WtfGlobal.getLocaleText("acc.field.Adhoc"):WtfGlobal.getLocaleText("acc.field.DefaultTemplate")),
                        id:Wtf.Acc_Basic_Template_Id  
                    });// if templateflag=15 and invoice then Adhoc
                }  
        }
        
        if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA){
            addIndianTemplates(config,this);
        }
        
        if(config.get==Wtf.autoNum.Invoice && (Wtf.templateflag >= Wtf.LandPlus_templateflag && Wtf.templateflag <= Wtf.LandPlus_Mobility_templateflag)) {
            for (var i = 1; i < 10; i++) {
                Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({
                    iconCls: 'pwnd exportpdf',
                    text: WtfGlobal.getLocaleText("acc.landplustemplate.type"+i),
                    id: Wtf.LandPlus_templateflag+'_'+i
                });
            }
        }
        
        if(config.get==Wtf.autoNum.Invoice && Wtf.templateflag == Wtf.BakerTilly_templateflag_pcs) {
            for (var i = 1; i <= 2; i++) {
                Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({
                    iconCls: 'pwnd exportpdf',
                    text: WtfGlobal.getLocaleText("acc.BakerTilly.type"+i),
                    id: Wtf.Bakertilly_templateflag+'_'+i
                });
            }
        }
         
        if(config.get==Wtf.autoNum.Invoice && Wtf.account.companyAccountPref.countryid=='137'){            
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.field.Simplified"),
                id: Wtf.Acc_Basic_Template_Id+"Simplified"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.field.Simplified.A7size"),
                id: Wtf.Acc_Basic_Template_Id+"SimplifiedA7"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.field.Simplified.A7sizeWithTax"),
                id: Wtf.Acc_Basic_Template_Id+"SimplifiedA7WithTax"  
            })
        };
            
        if(config.get==Wtf.autoNum.Quotation && Wtf.templateflag == Wtf.Guan_Chong_templateflag){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.field.proformaInvoice"),
                id: Wtf.Acc_Basic_Template_Id+"proInv"  
            })
        };
        
        if(config.get==Wtf.autoNum.Invoice&&((Wtf.templateflag == 1||Wtf.templateflag == 2)||Wtf.templateflag == 5 || Wtf.templateflag == 15 )){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: (Wtf.templateflag == 15 && config.get==Wtf.autoNum.Invoice)? WtfGlobal.getLocaleText("acc.field.Fees"):(Wtf.templateflag == 1||Wtf.templateflag == 5)?WtfGlobal.getLocaleText("acc.field.MultiplePage"):WtfGlobal.getLocaleText("acc.field.PackingList"),
                /*
             * this button id is only used for senwan group in export packing list of Customer Invoice .[Mayur B]
             */
                id: Wtf.Acc_Basic_Template_Id+"P"  
            })
        }; //if templateflag=15 and invoice then Fee

        if(config.get==Wtf.autoNum.Invoice&&(Wtf.templateflag == 1||Wtf.templateflag == 5)){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.field.SquezzSingle"),
                id: Wtf.Acc_Basic_Template_Id+"SSP"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.field.SquezzMulti"),
                id: Wtf.Acc_Basic_Template_Id+"SMP"  
            })
        };
        
        if(config.get==Wtf.autoNum.Quotation && Wtf.templateflag == 10){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text:'Quotation - Services',
                /*
             * this button id is only used for senwan tech for Quotation .[Mayur B]
             */
                id: Wtf.Acc_Basic_Template_Id+"Q"  
            })
        };

        if (config.get == Wtf.autoNum.DeliveryOrder && (Wtf.templateflag == Wtf.F1Recreation_templateflag||Wtf.templateflag == Wtf.F1RecreationLeasing_templateflag)){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: 'Delivery Order',
                id:  Wtf.Acc_Basic_Template_Id+"DOtype1"
            });
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: 'Packing List',
                id:  Wtf.Acc_Basic_Template_Id+"DOtype2"
            });
        }
        
        if (config.get == Wtf.autoNum.DeliveryOrder && Wtf.templateflag == Wtf.Diamond_Aviation_templateflag){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: 'Packing Slip',
                id:  Wtf.Acc_Basic_Template_Id+"DOtype1"
            });
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: 'Delivery Order',
                id:  Wtf.Acc_Basic_Template_Id+"DOtype2"
            });
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: 'Certificate Of Conformance',
                id:  Wtf.Acc_Basic_Template_Id+"DOtype3"
            });
            
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.diamondaviation.package.1"),  //General Overseas Shipping Package
                id:  Wtf.Acc_Basic_Template_Id+"Package1"
            });
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.diamondaviation.package.2"),  //Local Exchange Core Return Package
                id:  Wtf.Acc_Basic_Template_Id+"Package2"
            });
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.diamondaviation.package.3"),  //Local Exchange Sales Package
                id:  Wtf.Acc_Basic_Template_Id+"Package3"
            });
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.diamondaviation.package.12"),  //Local Exchange Proforma Sales Package
                id:  Wtf.Acc_Basic_Template_Id+"Package12"
            });
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.diamondaviation.package.4"),  //Local Outright Sales Package
                id:  Wtf.Acc_Basic_Template_Id+"Package4"
            });
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.diamondaviation.package.5"),  //Local Repair Outsource Package
                id:  Wtf.Acc_Basic_Template_Id+"Package5"
            });
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.diamondaviation.package.6"),  //Local Warranty Reject Package
                id:  Wtf.Acc_Basic_Template_Id+"Package6"
            });
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.diamondaviation.package.7"),  //Overseas Exchange Core Return Package
                id:  Wtf.Acc_Basic_Template_Id+"Package7"
            });
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.diamondaviation.package.8"),  //Overseas Exchange Sales Package
                id:  Wtf.Acc_Basic_Template_Id+"Package8"
            });
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.diamondaviation.package.13"),  //Local Exchange Proforma Sales Package
                id:  Wtf.Acc_Basic_Template_Id+"Package13"
            });
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.diamondaviation.package.9"),  //Overseas Outright Sales Package
                id:  Wtf.Acc_Basic_Template_Id+"Package9"
            });
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.diamondaviation.package.10"),  //Overseas Repair Outsource Package
                id:  Wtf.Acc_Basic_Template_Id+"Package10"
            });
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.diamondaviation.package.11"),  //Overseas Warranty Reject Package
                id:  Wtf.Acc_Basic_Template_Id+"Package11"
            });
        };
        if(config.get==Wtf.autoNum.GoodsReceiptOrder && Wtf.templateflag == Wtf.Diamond_Aviation_templateflag){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.goodsreceiptorder.GRtype0"),
                id: Wtf.Acc_Basic_Template_Id+"GRtype0"
            })
            
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.goodsreceiptorder.GRtype1"),
                id: Wtf.Acc_Basic_Template_Id+"GRtype1"
            })
            
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.goodsreceiptorder.GRtype2"),
                id: Wtf.Acc_Basic_Template_Id+"GRtype2"
            })
        }
        if ((config.get == Wtf.autoNum.SalesOrder || config.get == Wtf.autoNum.Quotation) && Wtf.account.companyAccountPref.activateProfitMargin && !config.obj.isConsignment && !config.obj.isLeaseFixedAsset){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.export.TemplateWithProfitMargin"),
                id:  Wtf.Acc_Basic_Template_Id+"DOtype1"
            });
        };
        if(config.get==Wtf.autoNum.Invoice && Wtf.templateflag == Wtf.Diamond_Aviation_templateflag){
//            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
//                iconCls: 'pwnd exportpdf',
//                text: WtfGlobal.getLocaleText("acc.exportbutton.invoice.type0"),
//                id: Wtf.Acc_Basic_Template_Id+"DAtype0"  
//            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.invoice.type1"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype1"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.invoice.type2"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype2"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.invoice.type3"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype3"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.invoice.type4"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype4"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.invoice.type5"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype5"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.invoice.type6"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype6"  
            })
        };
        
        if(config.get==Wtf.autoNum.Quotation && Wtf.templateflag == Wtf.Tony_FiberGlass_templateflag){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text:WtfGlobal.getLocaleText("acc.exportbutton.invoice.ConcreteFloaring"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype0"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf', 
                text: WtfGlobal.getLocaleText("acc.exportbutton.invoice.PipeSpool"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype1"  
            })
        };
        if(config.get==Wtf.autoNum.Quotation && Wtf.templateflag == Wtf.Diamond_Aviation_templateflag){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.quotation.type0"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype0"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.quotation.type3"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype3"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.quotation.type4"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype4"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.quotation.type1"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype1"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.quotation.type2"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype2"  
            })
        };
        
        if(config.get==Wtf.autoNum.PurchaseOrder && Wtf.templateflag == Wtf.Diamond_Aviation_templateflag){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.purchaseorder.type0"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype0"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.purchaseorder.type1"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype1"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.purchaseorder.type2"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype2"  
            })
        };
        
        if(config.get==Wtf.autoNum.PurchaseOrder && (Wtf.templateflag == Wtf.FastenEnterprises_templateflag||Wtf.templateflag == Wtf.FastenHardwareEngineering_templateflag)){
//            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
//                iconCls: 'pwnd exportpdf',
//                text: WtfGlobal.getLocaleText("acc.exportbutton.fasten.purchaseorder.type0"),
//                id: Wtf.Acc_Basic_Template_Id+"DAtype0"  
//            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.fasten.purchaseorder.type1"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype1"  
            })

/*ERP-16273 Commented as client does not want this templates.*/
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.fasten.purchaseorder.type2"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype2"  
            })
        };
        
        if(config.get==Wtf.autoNum.Invoice && Wtf.templateflag == Wtf.Armada_Rock_Karunia_Transhipment_templateflag){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text:'Government Invoice',// WtfGlobal.getLocaleText("acc.exportbutton.invoice.type1"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype1"  
            })
        };
        if(config.get==Wtf.autoNum.Invoice && Wtf.templateflag == Wtf.KimChey_templateflag){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text:'Default Template (Landscape)',
                id: Wtf.Acc_Basic_Template_Id+"DAtype1"  
            });
            
//            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
//                iconCls: 'pwnd exportpdf',
//                text:'Default Template (Landscape)- with Grid',
//                id: Wtf.Acc_Basic_Template_Id+"DAtype2"  
//            })
        };
        if(config.get==Wtf.autoNum.Quotation && Wtf.templateflag == Wtf.Monzone_templateflag){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text:'Default Template (Canon)',
                id: Wtf.Acc_Basic_Template_Id+"DAtype1"  
            });
            
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text:'Default Template (Epson)',
                id: Wtf.Acc_Basic_Template_Id+"DAtype2"  
            })
        };
        if(config.get==Wtf.autoNum.Quotation && Wtf.templateflag==Wtf.hinsitsu_templateflag){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text:'Quotation Process Review Template',  //WtfGlobal.getLocaleText('acc.exportbutton.customerquotation.type1'),
                id: Wtf.Acc_Basic_Template_Id+"DAtype1"  
            });
        };
        
        if(config.get==Wtf.autoNum.Invoice && (Wtf.templateflag == Wtf.FastenHardwareEngineering_templateflag || Wtf.templateflag == Wtf.FastenEnterprises_templateflag) ){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text:WtfGlobal.getLocaleText('acc.exportbutton.invoice.type7'),
                id: Wtf.Acc_Basic_Template_Id+"DAtype7"  
            })
        };
        if(config.get == Wtf.autoNum.SalesOrder && (Wtf.templateflag == Wtf.FastenEnterprises_templateflag || Wtf.templateflag == Wtf.FastenHardwareEngineering_templateflag)){
                Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text:"Pre-Printed Template",
                id: Wtf.Acc_Basic_Template_Id+"SOType1"  
            });
        }
        if (config.get == Wtf.autoNum.DeliveryOrder && Wtf.templateflag == Wtf.Arklife_templateflag){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text:WtfGlobal.getLocaleText("erp.btn.exportProductcomposition"),// Export Product Composition 
                tooltip :WtfGlobal.getLocaleText("erp.btn.exportProductcomposition"),  // Export Product Composition 
                id: "ProductCompositionExport"
            });
             
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text:WtfGlobal.getLocaleText("expbtn.name"),// Export Invoice Packing List
                tooltip :WtfGlobal.getLocaleText("expbtn.name"),  // Export Invoice Packing List
                id: "exportInvoicepackingList"
            });
            
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({   
                iconCls: 'pwnd exportpdf',
                text:WtfGlobal.getLocaleText("erp.exportbtn.do.perinv"),// Export Permit Invoice List
                tooltip :WtfGlobal.getLocaleText("erp.exportbtn.do.perinv"),  // Export Permit Invoice List
                id: "exportPermitInvoiceListPdf"
            });
        };

        if(config.get==Wtf.autoNum.Invoice && Wtf.templateflag == Wtf.Alfatech_templateFlag){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: 'Default Template2',
                id: Wtf.Acc_Basic_Template_Id+"DAtype1"  
            })
        };
        
        if(config.get==Wtf.autoNum.PackingDoList){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text:WtfGlobal.getLocaleText("erp.exportbtn.ExportPackingListLc"),
                id: Wtf.Acc_Packing_List_Lc_ModuleId
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("erp.export.ExportPackingListNonLc"),
                id: Wtf.Acc_Packing_List_NonLc_ModuleId 
            })
        };
        
        if(config.get==Wtf.autoNum.Invoice && Wtf.templateflag == Wtf.spaceTec_templateflag){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.spacetech.invoice.type1"),
                id: Wtf.Acc_Basic_Template_Id+"P"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.spacetech.invoice.type2"),
                id: Wtf.Acc_Basic_Template_Id+"SSP"  
            })
        };
         
        if(config.get==Wtf.autoNum.Invoice && (Wtf.templateflag == Wtf.F1Recreation_templateflag||Wtf.templateflag == Wtf.F1RecreationLeasing_templateflag)){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.invoice.withdiscount"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype0"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.invoice.withoutdiscount"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype1"  
            })
        };
        
        if(config.get==Wtf.autoNum.Quotation && (Wtf.templateflag == Wtf.F1Recreation_templateflag||Wtf.templateflag == Wtf.F1RecreationLeasing_templateflag)){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.field.DefaultTemplate"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype0"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.proformainvoice.USD"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype1"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.proformainvoice.SGD"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype2"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.f1.type3"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype3"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.f1.type4"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype4"
            })
        };
        
        if((config.get==Wtf.autoNum.Invoice || config.get==Wtf.autoNum.Quotation) && (Wtf.templateflag == Wtf.RightSpace_templateflag || Wtf.templateflag == Wtf.RightWork_templateflag)){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: (config.get==Wtf.autoNum.Invoice?WtfGlobal.getLocaleText("acc.exportbutton.rightspace.invoice"):WtfGlobal.getLocaleText("acc.exportbutton.rightspace.quotation")) +" "+WtfGlobal.getLocaleText("acc.exportbutton.type1"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype1"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: (config.get==Wtf.autoNum.Invoice?WtfGlobal.getLocaleText("acc.exportbutton.rightspace.invoice"):WtfGlobal.getLocaleText("acc.exportbutton.rightspace.quotation")) +" "+WtfGlobal.getLocaleText("acc.exportbutton.type2"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype2"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: (config.get==Wtf.autoNum.Invoice?WtfGlobal.getLocaleText("acc.exportbutton.rightspace.invoice"):WtfGlobal.getLocaleText("acc.exportbutton.rightspace.quotation")) +" "+WtfGlobal.getLocaleText("acc.exportbutton.type3"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype3"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: (config.get==Wtf.autoNum.Invoice?WtfGlobal.getLocaleText("acc.exportbutton.rightspace.invoice"):WtfGlobal.getLocaleText("acc.exportbutton.rightspace.quotation")) +" "+WtfGlobal.getLocaleText("acc.exportbutton.type4"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype4"  
            })
            if (config.get == Wtf.autoNum.Invoice) {//added only for invoice
                Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({
                    iconCls: 'pwnd exportpdf',
                    text: WtfGlobal.getLocaleText("acc.exportbutton.rightspace.invoice") + " " + WtfGlobal.getLocaleText("acc.exportbutton.type5"),
                    id: Wtf.Acc_Basic_Template_Id + "DAtype5"
                })
                Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({
                    iconCls: 'pwnd exportpdf',
                    text: WtfGlobal.getLocaleText("acc.exportbutton.rightspace.invoice") + " " + WtfGlobal.getLocaleText("acc.exportbutton.type6"),
                    id: Wtf.Acc_Basic_Template_Id + "DAtype6"
                })
            }
        };
         
        if((config.get == Wtf.autoNum.PurchaseReturn||config.get==Wtf.autoNum.SalesReturn||config.get==Wtf.autoNum.DebitNote||config.get==Wtf.autoNum.CreditNote) && (Wtf.templateflag == Wtf.Guan_Chong_templateflag || Wtf.templateflag == Wtf.Guan_ChongBF_templateflag)){
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text:WtfGlobal.getLocaleText("acc.exportbutton.WithGST.type0"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype0"  
            })
            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.exportbutton.WithoutGST.type1"),
                id: Wtf.Acc_Basic_Template_Id+"DAtype1"  
            })    
        };

        //       if((config.get==Wtf.autoNum.Receipt||config.get==Wtf.autoNum.Payment)&&(Wtf.templateflag == 1||Wtf.templateflag == 5)){
        //            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
        //                iconCls: 'pwnd exportpdf',
        //                text:"Payment Voucher GL",
        //            /*
        //             * this button id is only used for sms in export Payment Voucher GL of Payment,Receipt.
        //             */
        //                id: Wtf.Acc_Basic_Template_Id+"PaymentGL"  
        //            })
        //        };
        var colModelArray = GlobalCustomTemplateList[config.moduleid];
        var isTemplateflag=colModelArray!=undefined?true:false;
        //        if(isTemplateflag){
        //            for (var cnt = 0; cnt < colModelArray.length; cnt++) {
        //                var id=colModelArray[cnt].templateid;
        //                var name=colModelArray[cnt].templatename;           
        //                Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
        //                    iconCls: 'pwnd exportpdf',
        //                    text: name,
        //                    id: id             
        //                });
        //            }           
        //        }
//        Wtf.menu.MenuMgr.get("exportmenu" + this.id).on('itemclick',function(item) {
//            this.templateId = item.id;
//            this.templatesubtype = item.templatesubtype;
//            this.exportSingleRow(this.obj,config.get,config.menuItem.rowPrint,this.exportRecord)
//        }, this);
//        btn.push(2);
    }
    if(config.menuItem.rowPrint==true || config.menuItem.rowPdfPrint){
        isSingleRec = true; 
        config.filetype='print';
        this.printMenu = new Wtf.menu.Menu({
            id: "printmenu" + this.id,
            cls : 'printMenuHeight'
        });
        var colModArray = GlobalCustomTemplateList[config.moduleid];
        var isTflag=colModArray!=undefined && colModArray.length>0?true:false;
        if(isTflag){
            var insertedTemplates = 0;
            for (var count = 0; count < colModArray.length; count++) {
                var id1=colModArray[count].templateid;
                var name1=colModArray[count].templatename;           
                var templatesubtype1=colModArray[count].templatesubtype;
                if(Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && !addTemplate(id1)){
                    continue;
                }
//                if(config.jobOrderFlow){//for Job Order Flow button
//                    if((templatesubtype1 == Wtf.Subtype_Job_Order || templatesubtype1 == Wtf.Subtype_Job_Order_Label) && (config.moduleid == Wtf.Acc_Sales_Order_ModuleId || config.moduleid == Wtf.Acc_Invoice_ModuleId)){
//                        insertedTemplates++;
//                        if(Wtf.menu.MenuMgr.get("exportmenu" + this.id) !== undefined && (config.menuItem.rowPdfPrint)) {
//                            Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
//                                iconCls: 'pwnd printButtonIcon',
//                                text: name1,
//                                id: id1,
//                                templatesubtype: templatesubtype1,
//                                isDDTemplate: true
//                            });
//                        }
//                    }
//                } else{//for Normal Flow button
//                    if((config.moduleid == Wtf.Acc_Sales_Order_ModuleId || config.moduleid == Wtf.Acc_Invoice_ModuleId) && (templatesubtype1 == Wtf.Subtype_Job_Order || templatesubtype1 == Wtf.Subtype_Job_Order_Label)){
//                        continue;
//                    }
                    insertedTemplates++;
                    if(Wtf.menu.MenuMgr.get("exportmenu" + this.id) !== undefined && (config.menuItem.rowPdfPrint)) {
                        Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                            iconCls: 'pwnd printButtonIcon',
                            text: name1,
                            id: id1,
                            templatesubtype: templatesubtype1,
                            isDDTemplate: true
                        });
                    }
                    Wtf.menu.MenuMgr.get("printmenu" + this.id).add({                  
                        iconCls: 'pwnd printButtonIcon',
                        text: name1,
                        id: id1,
                        templatesubtype: templatesubtype1,
                        isDDTemplate: true
                    });
//                }
            }
            if(insertedTemplates == 0){
                Wtf.menu.MenuMgr.get("printmenu" + this.id).add({                  
                    iconCls: 'pwnd printButtonIcon',
                    text:WtfGlobal.getLocaleText("acc.field.TherearenotemplatesinCustomDesigner"),
                    id: Wtf.No_Template_Id,
                    isDDTemplate: true
                });
                if(Wtf.menu.MenuMgr.get("exportmenu" + this.id) !== undefined && (config.menuItem.rowPdfPrint)) {
                    Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                        iconCls: 'pwnd printButtonIcon',
                        text:WtfGlobal.getLocaleText("acc.field.TherearenotemplatesinCustomDesigner"),
                        id: Wtf.No_Template_Id,
                        isDDTemplate: true
                    });
                }
            }
        }else{
            Wtf.menu.MenuMgr.get("printmenu" + this.id).add({                  
                iconCls: 'pwnd printButtonIcon',
                text:WtfGlobal.getLocaleText("acc.field.TherearenotemplatesinCustomDesigner"),
                id: Wtf.No_Template_Id,
                isDDTemplate: true
            });
            if(Wtf.menu.MenuMgr.get("exportmenu" + this.id) !== undefined && (config.menuItem.rowPdfPrint)) {
                Wtf.menu.MenuMgr.get("exportmenu" + this.id).add({                  
                    iconCls: 'pwnd printButtonIcon',
                    text:WtfGlobal.getLocaleText("acc.field.TherearenotemplatesinCustomDesigner"),
                    id: Wtf.No_Template_Id,
                    isDDTemplate: true
                });
            }
        }
        Wtf.menu.MenuMgr.get("printmenu" + this.id).on('itemclick',function(item) {
            this.templateId = item.id,
            this.templatesubtype = item.templatesubtype,
            this.isDDTemplate = item.isDDTemplate;
            this.exportSingleRow(this.obj,config.get,config.menuItem.rowPrint,this.exportRecord)
        }, this);  
//        Wtf.menu.MenuMgr.get("exportmenu" + this.id).on('itemclick',function(item) {
//            this.templateId = item.id;
//            this.templatesubtype = item.templatesubtype;
//            this.isDDTemplate = item.isDDTemplate;
//            this.exportSingleRow(this.obj,config.get,config.menuItem.rowPrint,this.exportRecord)
//        }, this);  
//        btn.push(3);
    }
    if(Wtf.menu.MenuMgr.get("exportmenu" + this.id) !== undefined && (config.menuItem.rowPdfPrint || config.menuItem.rowPdf)){
        Wtf.menu.MenuMgr.get("exportmenu" + this.id).on('itemclick',function(item) {
            this.templateId = item.id;
            this.templatesubtype = item.templatesubtype;
            this.isDDTemplate = item.isDDTemplate;
            this.filetype='pdf';
            if(item.isDDTemplate){
                this.filetype='print';
            }
            this.exportSingleRow(this.obj,config.get,this.isDDTemplate,this.exportRecord)
        }, this);   
    }
    //    if(config.menuItem.rowPrint==true){
    //        mnuBtns.push(this.createRowButton(config));
    //    }
    if(config.menuItem.print==true){
        mnuBtns.push(this.createPrintButton("print",config));
    }
    if(config.obj.grid!=undefined){
        if(config.isEntrylevel===false){
            config.obj.grid.getSelectionModel().on('selectionchange', function(){
                WtfGlobal.enableDisableBtnArr(mnuBtns, config.obj.grid, btn,[]);
            }, this);
        }
    }
    var mainConfig={
        menu: isSingleRec ? (config.menuItem.rowPdfPrint ? this.exportMenu : (config.filetype=='pdf' ? this.exportMenu : this.printMenu)): mnuBtns
    };
    if(mnuBtns.length==1){
        var fbtn=mnuBtns.pop();
        mainConfig = fbtn.initialConfig;
    }
    Wtf.apply(this,mainConfig,config);
    Wtf.exportButton.superclass.constructor.call(this,config);
}

// Function used to check whether to add template or not
function addTemplate(templateid,obj){
    var addTemplate = true;
    
    // *****Hidding templates STATE WISE*************
    if(Wtf.account.companyAccountPref.stateid != Wtf.StateName.MAHARASHTRA ){
        if(templateid == Wtf.template.RETAIL_INVOICE_MH || templateid == Wtf.template.VAT_INVOICE_MH || templateid == Wtf.template.VAT_DO ){
            addTemplate = false;
        }
    } else{
        if(templateid == Wtf.template.RETAIL_INVOICE || templateid == Wtf.template.VAT_INVOICE){
            addTemplate = false;
        }
    }
    // *****Hidding templates on Checks*************
//    if(Wtf.isExciseApplicable){
//        if(templateid == Wtf.template.VAT_INVOICE || templateid == Wtf.template.VAT_DO || templateid == Wtf.template.RETAIL_INVOICE || templateid == Wtf.template.RETAIL_INVOICE_MH){
//            addTemplate = false;
//        }
//    }
    if(!Wtf.account.companyAccountPref.enablevatcst){
        if( templateid == Wtf.template.VAT_DO){
            addTemplate = false;
        }
    }
    if(!Wtf.account.companyAccountPref.enablevatcst && !Wtf.isSTApplicable){
        if(templateid == Wtf.template.VAT_INVOICE_MH || templateid == Wtf.template.VAT_INVOICE ){
            addTemplate = false;
        }
    }
    if(templateid == Wtf.template.SERVICE_TAX_INVOICE){
        addTemplate = false;
    } 
    if(templateid == Wtf.template.RULE_11_MANUFACTURER && Wtf.registrationType != Wtf.registrationTypeValues.MANUFACTURER){
        addTemplate = false;
    }
    if(templateid == Wtf.template.DEFAULT_TEMPLATE_INVOICE || templateid == Wtf.template.DEFAULT_TEMPLATE_PI || templateid == Wtf.template.DEFAULT_TEMPLATE_PO  || templateid == Wtf.template.DEFAULT_TEMPLATE_CQ ){
        addTemplate = false;
    }
    return addTemplate;
}
Wtf.extend(Wtf.exportButton,Wtf.Toolbar.Button,{
    text:WtfGlobal.getLocaleText("acc.common.export"),  //'Export',
    iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
    createPrintButton: function(type, config) {
        if (config.label == undefined || config.label == null) {
            config.label = "report";
        }
        if (this.moduleId == Wtf.Acc_Receive_Payment_ModuleId) {
            var btnArray = [];
            btnArray.push(new Wtf.Action({
                iconCls: 'pwnd printButtonIcon',
                text: WtfGlobal.getLocaleText("acc.print.selected"),
                scope: this,
                handler: function() {
                    this.exportWithTemplate(this.obj, type, this.get, true)     // true to export only selected records.
                }
            }));
            btnArray.push(new Wtf.Action({
                iconCls: 'pwnd printButtonIcon',
                text: WtfGlobal.getLocaleText("acc.print.All"),
                scope: this,
                handler: function() {
                    this.exportWithTemplate(this.obj, type, this.get)
                }
            }));

            var btn = new Wtf.Action({
                iconCls: 'pwnd printButtonIcon',
                text: ("<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.printTT") + "'>" + WtfGlobal.getLocaleText("acc.common.printToHtml") + "</span>"),
                scope: this,
                menu: btnArray
            });

        } else {
            var btn = new Wtf.Action({
                iconCls: 'pwnd printButtonIcon',
                tooltip: WtfGlobal.getLocaleText("acc.common.printTT"), //{text: "Print "+config.label+" details."},
                scope: this,
                text: WtfGlobal.getLocaleText("acc.common.printToHtml"), //"Print",
                handler: function() {
                    this.exportWithTemplate(this.obj, type, this.get)
                }
            });
        }
        return btn;
    },
   
    createButton:function(type,config){
        if(config.label==undefined || config.label==null)
            config.label="report";
        
        if((type == "detailedCSV" || type == "csv") && this.detailedCSV == true){
            var btnArray = [];
            if(this.detailedCSV == true){
                btnArray.push(new Wtf.Action({
                    iconCls:'pwnd exportcsv',
                    text :"Export to CSV File(Details)",
                    scope: this,
                    handler:function(){
                        if(this.isProductExport){
                            if(this.obj.grid.getStore().totalLength>1000){
                                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.product.export.records.morethan.1000"),function(btn){
                                    if(btn=="yes") {
                                        this.isProductExportRecordsGreaterThanThousands=true;
                                        this.exportWithTemplate(this.obj,"detailedCSV",this.get);
                                    }else{
                                        return
                                    }
                                }, this);
                            }else{
                                this.isProductExportRecordsGreaterThanThousands=false;
                                this.exportWithTemplate(this.obj,"detailedCSV",this.get);
                            }
                        }else{
                            this.isProductExportRecordsGreaterThanThousands=false;
                            this.exportWithTemplate(this.obj,"detailedCSV",this.get)
                        }
                    }
                }))
            }
            if(this.isCSV == true){
                btnArray.push(new Wtf.Action({
                    iconCls:'pwnd exportcsv',
                    text :"Export to CSV File(Summary)",
                    scope: this,
                    handler:function(){
                        if(this.isProductExport){
                            if(this.obj.grid.getStore().totalLength>1000){
                                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.product.export.records.morethan.1000"),function(btn){
                                    if(btn=="yes") {
                                        this.isProductExportRecordsGreaterThanThousands=true;
                                        this.exportWithTemplate(this.obj,type,this.get);
                                    }else{
                                        return
                                    }
                                }, this);
                            }else{
                                this.isProductExportRecordsGreaterThanThousands=false;
                                this.exportWithTemplate(this.obj,type,this.get);
                            }
                        }else{
                            this.isProductExportRecordsGreaterThanThousands=false;
                            this.exportWithTemplate(this.obj,type,this.get)
                        }
                    }
                }))
            }
            btn=new Wtf.Action({
                iconCls:'pwnd '+((this.isPDF||this.isSummaryPDF||this.isDetailPDF)?'exportpdf':'exportcsv'),
                text :("<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToCSVTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToCSV")+"</span>"),
                scope: this,
                menu: btnArray
            });
        }else 
            
            if((type == "detailedXls" || type == "xls") && this.detailedXls == true)/// export details Excel File 
        {
            var btnArray = [];
            if(this.detailedXls == true)
            {
                btnArray.push(new Wtf.Action({
                    iconCls:'pwnd '+((this.isPDF||this.isSummaryPDF||this.isDetailPDF)?'exportpdf':'exportcsv'),
                    text :("<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToXLSTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToXLSdetails")+"</span>"),
                    scope: this,
                    handler:function(){
                       if(this.isProductExport){
                    if(this.obj.grid.getStore().totalLength>1000){
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.product.export.records.morethan.1000"),function(btn){
                            if(btn=="yes") {
                                this.isProductExportRecordsGreaterThanThousands=true;
                                this.exportWithTemplate(this.obj,"detailedXls",this.get);
                            }else{
                                        return
                                    }
                                }, this);
                    }else{
                        this.isProductExportRecordsGreaterThanThousands=false;
                        this.exportWithTemplate(this.obj,"detailedXls",this.get);
                            }
                }else{
                    this.isProductExportRecordsGreaterThanThousands=false;
                    this.exportWithTemplate(this.obj,"detailedXls",this.get)
                                }
                        }
                }))
            }
            if(this.isXLS == true)
            {
                btnArray.push(new Wtf.Action({
                    iconCls:'pwnd '+((this.isPDF||this.isSummaryPDF||this.isDetailPDF)?'exportpdf':'exportcsv'),
                    text :("<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToXLSTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToXLSsummary")+"</span>"),
                    scope: this,
                    handler:function(){
                      if(this.isProductExport){
                    if(this.obj.grid.getStore().totalLength>1000){
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.product.export.records.morethan.1000"),function(btn){
                            if(btn=="yes") {
                                this.isProductExportRecordsGreaterThanThousands=true;
                                this.exportWithTemplate(this.obj,"xls",this.get);
                            }else{
                                        return
                                    }
                                }, this);
                    }else{
                        this.isProductExportRecordsGreaterThanThousands=false;
                        this.exportWithTemplate(this.obj,"xls",this.get);
                            }
                }else{
                    this.isProductExportRecordsGreaterThanThousands=false;
                    this.exportWithTemplate(this.obj,"xls",this.get)
                                }
                        }
                }))
            }
              var btn=new Wtf.Action({
                iconCls:'pwnd '+((this.isPDF||this.isSummaryPDF||this.isDetailPDF)?'exportpdf':'exportcsv'),
                text :("<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToXLSTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"</span>"),
                scope: this,
                menu: btnArray
            });
        }else if (type == 'pdf' && this.moduleId == Wtf.Acc_Sales_Order_ModuleId && Wtf.templateflag == Wtf.hinsitsu_templateflag) {
            var btnArray = [];
            btnArray.push(new Wtf.Action({
                iconCls: 'pwnd ' + ((this.isPDF || this.isSummaryPDF || this.isDetailPDF) ? 'exportpdf' : 'exportcsv'),
                text: ("<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.soCheckList.exportToPDFTT") + "'>" + WtfGlobal.getLocaleText("acc.common.soCheckList.exportToPDF") + "</span>"),
                scope: this,
                handler: function () {
                    this.exportWithTemplate(this.obj,"summaryPDF",this.get);
                                       
        }
            }))
            btnArray.push(new Wtf.Action({
                iconCls: 'pwnd ' + ((this.isPDF || this.isSummaryPDF || this.isDetailPDF) ? 'exportpdf' : 'exportcsv'),
                text: ("<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.DefaultTemplate") + "'>" + WtfGlobal.getLocaleText("acc.field.DefaultTemplate") + "</span>"),
                scope: this,
                handler: function () {
                    this.exportWithTemplate(this.obj,"pdf",1);
                }
            }))
            var btn = new Wtf.Action({
                iconCls: 'pwnd ' + ((this.isPDF || this.isSummaryPDF || this.isDetailPDF) ? 'exportpdf' : 'exportcsv'),
                text: ("<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.soCheckList.exportToPDFTT") + "'>" + WtfGlobal.getLocaleText("acc.common.exportToPDF") + "</span>"),
                scope: this,
                menu: btnArray
            });
        }
//        else if(type == 'pdf'&& (config.reportType == 1) && config.get == Wtf.autoNum.StockAdjustmentRegister){
//             var btnArray = [];
//            btnArray.push(new Wtf.Action({
//                iconCls: 'pwnd ' + ((this.isPDF || this.isSummaryPDF || this.isDetailPDF) ? 'exportpdf' : 'exportcsv'),
//                text: ("<span wtf:qtip='" + WtfGlobal.getLocaleText("Export To PDF File (Details)") + "'>" + WtfGlobal.getLocaleText("Export To PDF File (Details)") + "</span>"),
//                scope: this,
//                get : Wtf.autoNum.StockAdjustmentRegister,
//                handler: function () {
//                    this.exportWithTemplate(this.obj,"detailPDF",this.get);
//                                       
//        }
//            }))
//            btnArray.push(new Wtf.Action({
//                iconCls: 'pwnd ' + ((this.isPDF || this.isSummaryPDF || this.isDetailPDF) ? 'exportpdf' : 'exportcsv'),
//                text: ("<span wtf:qtip='" + WtfGlobal.getLocaleText("Export To PDF File (Summary)") + "'>" + WtfGlobal.getLocaleText("Export To PDF File (Summary)") + "</span>"),
//                scope: this,
//                get : Wtf.autoNum.StockAdjustmentRegister,
//                handler: function () {
//                    this.exportWithTemplate(this.obj,"summaryPDF",1);
//                }
//            }))
//            var btn = new Wtf.Action({
//                iconCls: 'pwnd ' + ((this.isPDF || this.isSummaryPDF || this.isDetailPDF) ? 'exportpdf' : 'exportcsv'),
//                text: ("<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.common.soCheckList.exportToPDFTT") + "'>" + WtfGlobal.getLocaleText("acc.common.exportToPDF") + "</span>"),
//                scope: this,
//                menu: btnArray
//            });
//        }
       else if(type == "subMenu"||type == "subMenu1"||type == "subMenu2" || type == "summarySubMenu"){
            var btnArray = [];
            var btnArrayCombinedSeparate = [];
            
            var btnArrayCombinedLandPort = [];
            btnArrayCombinedLandPort.push(new Wtf.Action({
                iconCls:'pwnd '+'exportpdf',
                text : (config.get==Wtf.autoNum.Invoice || config.get==Wtf.autoNum.GoodsReceipt)?WtfGlobal.getLocaleText("acc.export.invoicesummary"):WtfGlobal.getLocaleText("acc.journalentry.exportpdf.portrait"),
                scope: this,
                handler:function(){
                    this.exportWithTemplate(this.obj,"portrait"+type+"combinedPDF",this.get)
                }
            }))
            btnArrayCombinedLandPort.push(new Wtf.Action({
                iconCls:'pwnd '+'exportpdf',
                text : (config.get==Wtf.autoNum.Invoice || config.get==Wtf.autoNum.GoodsReceipt)?WtfGlobal.getLocaleText("acc.export.invoicedetail"):WtfGlobal.getLocaleText("acc.journalentry.exportpdf.landscape"),
                scope: this,
                handler:function(){
                    this.exportWithTemplate(this.obj,"landscape"+type+"combinedPDF",this.get)
                }
            }))
               
            var btnArraySeparateLandPort = [];
            btnArraySeparateLandPort.push(new Wtf.Action({
                iconCls:'pwnd '+'exportpdf',
                text : 'Portrait',
                scope: this,
                handler:function(){
                    this.exportWithTemplate(this.obj,"portrait"+type+"separateZIP",this.get)
                }
            }))
            btnArraySeparateLandPort.push(new Wtf.Action({
                iconCls:'pwnd '+'exportpdf',
                text : 'landscape',
                scope: this,
                handler:function(){
                    this.exportWithTemplate(this.obj,"landscape"+type+"separateZIP",this.get)
                }
            }))
            btnArrayCombinedSeparate.push(new Wtf.Action({
                iconCls:'pwnd '+'exportpdf',
                text : 'combined',
                scope: this,
                menu: btnArrayCombinedLandPort
                 
            }));
            btnArrayCombinedSeparate.push(new Wtf.Action({
                iconCls:'pwnd '+'exportpdf',
                text : 'Separate',
                scope: this,
                menu: btnArraySeparateLandPort
            }));
//            if(type == "subMenu" && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA ){
//                btnArrayCombinedSeparate = [];
//                btnArrayCombinedSeparate.push(new Wtf.Action({
//                    iconCls:'pwnd '+'exportpdf',
//                    text : WtfGlobal.getLocaleText("acc.field.TherearenotemplatesinExportRecords"),
//                    scope: this
//                }));
//            }
            
            
            
            var btnArrayWithSplitDrBr = [];
            {
                btnArray.push(new Wtf.Action({
                    iconCls:'pwnd '+'exportpdf',
                    text : (config.get==Wtf.autoNum.Invoice || config.get==Wtf.autoNum.GoodsReceipt)?WtfGlobal.getLocaleText("acc.export.invoicesummary"):WtfGlobal.getLocaleText("acc.journalentry.exportpdf.portrait"),
                    scope: this,
                    handler:function(){
                        this.exportWithTemplate(this.obj,"portrait"+type,this.get)
                    }
                }))
                 btnArray.push(new Wtf.Action({
                    iconCls:'pwnd '+'exportpdf',
                    text : (config.get==Wtf.autoNum.Invoice || config.get==Wtf.autoNum.GoodsReceipt)?WtfGlobal.getLocaleText("acc.export.invoicedetail"):WtfGlobal.getLocaleText("acc.journalentry.exportpdf.landscape"),
                    scope: this,
                    handler:function(){
                        this.exportWithTemplate(this.obj,"landscape"+type,this.get)
                    }
                }))
                
                if(Wtf.templateflag == Wtf.GPlus_templateflag && (type == "subMenu1" || type == "subMenu2")) {
                    var btnArrayCombine = [];
                    var btnArraySplit = [];
                    btnArrayCombine.push(new Wtf.Action({
                        iconCls: 'pwnd ' + 'exportpdf',
                        text: (config.get==Wtf.autoNum.Invoice || config.get==Wtf.autoNum.GoodsReceipt) ? WtfGlobal.getLocaleText("acc.export.invoicesummary") : WtfGlobal.getLocaleText("acc.journalentry.exportpdf.portrait"),
                        scope: this,
                        handler: function() {
                            this.exportWithTemplate(this.obj, "portrait"+type+"Combine" , this.get)
                        }
                    }));
                    btnArrayCombine.push(new Wtf.Action({
                        iconCls: 'pwnd ' + 'exportpdf',
                        text: (config.get==Wtf.autoNum.Invoice || config.get==Wtf.autoNum.GoodsReceipt) ? WtfGlobal.getLocaleText("acc.export.invoicedetail") : WtfGlobal.getLocaleText("acc.journalentry.exportpdf.landscape"),
                        scope: this,
                        handler: function() {
                            this.exportWithTemplate(this.obj, "landscape"+type+"Combine", this.get)
                        }
                    }));
                    btnArraySplit.push(new Wtf.Action({
                        iconCls: 'pwnd ' + 'exportpdf',
                        text: (config.get==Wtf.autoNum.Invoice || config.get==Wtf.autoNum.GoodsReceipt) ? WtfGlobal.getLocaleText("acc.export.invoicesummary") : WtfGlobal.getLocaleText("acc.journalentry.exportpdf.portrait"),
                        scope: this,
                        handler: function() {
                            this.exportWithTemplate(this.obj, "portrait"+type+"Split" , this.get)
                        }
                    }));
                    btnArraySplit.push(new Wtf.Action({
                        iconCls: 'pwnd ' + 'exportpdf',
                        text: (config.get==Wtf.autoNum.Invoice || config.get==Wtf.autoNum.GoodsReceipt) ? WtfGlobal.getLocaleText("acc.export.invoicedetail") : WtfGlobal.getLocaleText("acc.journalentry.exportpdf.landscape"),
                        scope: this,
                        handler: function() {
                            this.exportWithTemplate(this.obj, "landscape"+type+"Split", this.get)
                        }
                    }));
                    
                    btnArrayWithSplitDrBr.push(new Wtf.Action({
                        iconCls:'pwnd '+'exportpdf',
                        text : WtfGlobal.getLocaleText("acc.journalentry.exportpdf.CombineDrCr"),
                        scope: this,
                        menu: btnArrayCombine
                    }));
                    btnArrayWithSplitDrBr.push(new Wtf.Action({
                        iconCls:'pwnd '+'exportpdf',
                        text : WtfGlobal.getLocaleText("acc.journalentry.exportpdf.SplitDrCr"),
                        scope: this,
                        menu: btnArraySplit
                    }));
                }
                var btn;
                if((config.get==Wtf.autoNum.CustomerAccountStatement || config.get==Wtf.autoNum.VendorAccountStatement) && Wtf.templateflag != Wtf.GPlus_templateflag) {
                    btn=new Wtf.Action({
                    iconCls:'pwnd '+'exportpdf',
                    text :(type == "subMenu1")?(config.get==Wtf.autoNum.CustomerAccountStatement?WtfGlobal.getLocaleText("acc.common.exportToPDF2"):WtfGlobal.getLocaleText("acc.common.exportToVendorPDF2"))
                         :(type == "subMenu2")?(config.get==Wtf.autoNum.CustomerAccountStatement?WtfGlobal.getLocaleText("acc.common.exportToPDF3"):WtfGlobal.getLocaleText("acc.common.exportToVendorPDF3"))
                         : WtfGlobal.getLocaleText("acc.common.exportToPDF"),
                    scope: this,
                        menu:btnArrayCombinedSeparate
                    }); 
                }else if(type== "summarySubMenu"){
                     btn=new Wtf.Action({
                    iconCls:'pwnd '+'exportpdf',
                    text : WtfGlobal.getLocaleText("acc.common.exportToPDFSummary"),
                    scope: this,
                        menu:btnArrayCombinedLandPort
                    }); 
                }else {
                    btn=new Wtf.Action({
                        iconCls:'pwnd '+'exportpdf',
                        text :(type == "subMenu1")?(config.get==Wtf.autoNum.CustomerAccountStatement?WtfGlobal.getLocaleText("acc.common.exportToPDF2"):WtfGlobal.getLocaleText("acc.common.exportToVendorPDF2"))
                        :(type == "subMenu2")?(config.get==Wtf.autoNum.CustomerAccountStatement?WtfGlobal.getLocaleText("acc.common.exportToPDF3"):WtfGlobal.getLocaleText("acc.common.exportToVendorPDF3"))
                        :(type == "subMenu" && config.get == Wtf.autoNum.GroupDetailReport) ? ("<span wtf:qtip='Export to PDF(Detail)'>Export to PDF(Detail)</span>")
                        : WtfGlobal.getLocaleText("acc.common.exportToPDF"),
                        scope: this,
                    menu: (Wtf.templateflag != Wtf.GPlus_templateflag || type == "subMenu") ? btnArray : btnArrayWithSplitDrBr
                });
            }
            }
        }else{ 
           var btn=new Wtf.Action({
            iconCls:'pwnd '+((this.isPDF||this.isSummaryPDF||this.isDetailPDF)?'exportpdf':'exportcsv'),
            text :this.CRLetter?("<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportCRLettert")+"'>"+WtfGlobal.getLocaleText("acc.common.exportCRLettert")+"</span>"):
            this.isPDF &&(config.get==Wtf.autoNum.VendorAccountStatement || config.get==Wtf.autoNum.CustomerAccountStatement)?("<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.soainvoice")+"'>"+WtfGlobal.getLocaleText("acc.soainvoice")+"</span>"):
            (this.isPDF && config.get == Wtf.autoNum.GroupDetailReport) ? ("<span wtf:qtip='Export to PDF(Summary)'>Export to PDF(Summary)</span>"):
            this.isPDF?("<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToPDFTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToPDF")+"</span>"):
            this.isSummaryPDF?("<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToPDFTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToPDF")+"</span>"): //Mayur B- Export Customer Invoice Summary Register for Monzone 
            this.isDetailPDF?(config.get==Wtf.autoNum.BankReconcilation?("<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.export.brdetailtip")+"'>"+WtfGlobal.getLocaleText("acc.export.br.Unreconciled")+"</span>"):("<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.export.invoicedetailtip")+"'>"+WtfGlobal.getLocaleText("acc.export.invoicedetail")+"</span>")):  //Mayur B- Export Customer/Vendor Invoice Detail Register for Monzone 
            this.isCSV?("<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToCSVTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToCSV")+"</span>"):("<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.common.exportToXLSTT")+"'>"+WtfGlobal.getLocaleText("acc.common.exportToXLS")+"</span>"),
            
            scope: this,
            handler:function(){
                    if (this.isProductExport) {
                        if (this.obj.grid.getStore().totalLength > 1000) {
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.product.export.records.morethan.1000"), function (btn) {
                                if (btn == "yes") {
                                    this.isProductExportRecordsGreaterThanThousands = true;
                                    this.exportWithTemplate(this.obj, type, this.get);
                                } else {
                                    return
                                }
                            }, this);
                        } else {
                            this.isProductExportRecordsGreaterThanThousands = false;
                            this.exportWithTemplate(this.obj, type, this.get);
                        }
                    } else if (this.isConsolidatedReportExport) {
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.export.records.by.thread"), function (btn) {
                            if (btn == "yes") {
                                this.isProductExportRecordsGreaterThanThousands = false;
                                this.exportWithTemplate(this.obj, type, this.get);
                            } else {
                                return
                            }
                        }, this);
                    } else {
                        this.isProductExportRecordsGreaterThanThousands = false;
                        this.exportWithTemplate(this.obj, type, this.get);
                    }
            }
        });
    }
        return btn;
    },
    createRowButton:function(config){
        config.filetype='pdf';
        var t=config.menuItem.rowPdfTitle||"Export Row";
        var btn=new Wtf.Action({
            text :"<span wtf:qtip=' "+t+" "+WtfGlobal.getLocaleText("acc.rem.129")+"'>"+t+" </span>",
            iconCls: 'pwnd exportpdf',
            scope: this,
            disabled:true,
            handler:function(){
                this.exportSingleRow(this.obj,config.get, config.menuItem.rowPrint)
            }
        });
        return btn;
    },
    
    exportWithTemplate:function(obj,type,get,SelectedExportFlag) {              // Send SelectedExportFlag true to export only selected records.
        obj.pdfStore =new Wtf.data.Store({});
        obj.pdfStore=this.filPdfStore(obj,obj.grid.getColumnModel(),type);
        if (type == "detailedXls" || type=="detailedCSV"){   //   Called Only For Export Excel File(Details)
            obj.pdfStore= this.fillExpanderBody(obj,get)
        }
        var paramsString = '';
        if(this.params){
            for(var index in this.params) {
                paramsString += "&"+index +"="+this.params[index]+"";
            }
        }
        var totalRecords=0;
        if(this.isProductExport){
             totalRecords = this.obj.grid.getStore().totalLength;
            paramsString+="&totalProducts="+totalRecords;
        }
        if (SelectedExportFlag != undefined && SelectedExportFlag && obj.grid != undefined) {
            var selectedIds = [];
            if (this.moduleId == Wtf.Acc_Receive_Payment_ModuleId) {
                this.recArr = obj.grid.getSelectionModel().getSelections();
                if (this.recArr.length <= 0) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.common.exportSelectedBlankMessage")], 2);
                    return;
                }
                for (var i = 0; i < this.recArr.length; i++) {
                    selectedIds.push(this.recArr[i].data.billid);
                }
            }
            paramsString += "&billid=" + selectedIds;
        }
        if(type == "pdf"){
            this.isPDF= true;
            this.isDetailPDF = false;
            this.isSummaryPDF = false;
            this.detailedXls = false;
        }else if(type == "detailPDF"){
            this.isPDF= false;
            this.isDetailPDF = true;
            this.isSummaryPDF = false;
            this.detailedXls = false;
        }else if(type =="detailedXls" ) {
            this.isPDF= false;
            this.isDetailPDF = false;
            this.detailedXls = true;
            this.isSummaryPDF = false;
        }else if(type =="summaryPDF" ) {
            this.isPDF= false;
            this.isDetailPDF = false;
            this.detailedXls = false;
            this.isSummaryPDF = true;
        }else if(type =="portrait" || type =="portraitsubMenu" || type =="portraitsubMenu1"|| type =="portraitsubMenu2" || type == "portraitsubMenu1Combine" || 
            type == "portraitsubMenu1Split" || type == "portraitsubMenu2Combine" || type == "portraitsubMenu2Split" ||
            type=="portraitsubMenuseparateZIP" || type=="portraitsubMenu1separateZIP" || type=="portraitsubMenu2separateZIP" ||
            type=="portraitsubMenucombinedPDF" || type=="portraitsubMenu1combinedPDF" || type=="portraitsubMenu2combinedPDF" || type=="portraitsummarySubMenucombinedPDF") {
            this.isPortrait= true;
            this.isLandscape = false;
        }else if(type =="landscape"|| type =="landscapesubMenu"|| type =="landscapesubMenu1"|| type =="landscapesubMenu2" || type == "landscapesubMenu1Combine" ||
               type == "landscapesubMenu1Split" || type == "landscapesubMenu2Combine" || type == "landscapesubMenu2Split" ||
               type=="landscapesubMenuseparateZIP" || type=="landscapesubMenu1separateZIP" || type=="landscapesubMenu2separateZIP" ||
               type=="landscapesubMenucombinedPDF" || type=="landscapesubMenu1combinedPDF " || type=="landscapesubMenu2combinedPDF" || type=="landscapesummarySubMenucombinedPDF") {
            this.isPortrait= false;
            this.isLandscape = true;
        }
        var jsonGrid =this.genJsonForPdf(obj);
        if(type == "pdf"|| type =="CRLetter" || type == "summaryPDF" || type == "detailPDF" ||type == "portrait"||type == "landscape" ||type == "portraitsubMenu" ||type == "landscapesubMenu"||type == "portraitsubMenu1" ||type == "landscapesubMenu1"||type == "portraitsubMenu2" ||type == "landscapesubMenu2"
                || type == "portraitsubMenu1Combine" || type == "portraitsubMenu1Split" || type == "landscapesubMenu1Combine" || type == "landscapesubMenu1Split" || type == "portraitsubMenu2Combine" || type == "portraitsubMenu2Split" || type == "landscapesubMenu2Combine" || type == "landscapesubMenu2Split"
                || type=="portraitsubMenuseparateZIP" || type=="portraitsubMenu1separateZIP" || type=="portraitsubMenu2separateZIP" 
                || type=="portraitsubMenucombinedPDF" || type=="portraitsubMenu1combinedPDF" || type=="portraitsubMenu2combinedPDF"
                || type=="landscapesubMenuseparateZIP" || type=="landscapesubMenu1separateZIP" || type=="landscapesubMenu2separateZIP" 
                || type=="landscapesubMenucombinedPDF" || type=="landscapesubMenu1combinedPDF" || type=="landscapesubMenu2combinedPDF" || type=="portraitsummarySubMenucombinedPDF" || type=="landscapesummarySubMenucombinedPDF")  {
            this.extra = "";
             var orientation="&isLandscape=";
                if(this.isLandscape){
                    orientation+=true
                }else{
                    orientation+=false
                }
            if(this.params&&this.params.agedDetailsFlag!=undefined&&this.params.agedDetailsFlag){
                this.extra=Wtf.urlEncode(this.extra);
                if(this.extra.length>0)
                    this.extra="&"+this.extra;
                //if grid is loaded, then use getStore().lastOptions otherwise this.params which are set.
                var compStoreParams=obj.grid.getStore().lastOptions!=null?Wtf.urlEncode(obj.grid.getStore().lastOptions.params):Wtf.urlEncode(this.params);
                
                if(compStoreParams.length>0) {
                    this.extra += "&"+compStoreParams;
                }
                var exportUrl="";
                switch(this.get){
                    case Wtf.autoNum.AgedPayableWithInv:
                        exportUrl = "ACCGoodsReceiptCMN/getAgeingAnalysisDetailsJasper.do";
                        break;
                    case Wtf.autoNum.AgedPayableWithOutInv:
                        exportUrl = "ACCGoodsReceiptCMN/getAgeingAnalysisDetailsJasper.do";
                        break;
                    case Wtf.autoNum.VendorAgedPayable:
                        exportUrl = "ACCGoodsReceiptCMN/getAgeingAnalysisSummariesedJasper.do";
                        break;
                    case Wtf.autoNum.ExportInvoices:
                        exportUrl = (type =="CRLetter")?"ACCInvoiceCMN/getAgedReceivableJasperCustomerRemainderLetter.do":"ACCInvoiceCMN/getAgeingAnalysisDetailsJasper.do";
                        break;
                    case Wtf.autoNum.getBillingInvoices:
                        exportUrl = "ACCInvoiceCMN/getAgeingAnalysisDetailsJasper.do";
                        break;
                        
                    case Wtf.autoNum.CustomerAgedReceivable:
                        exportUrl = "ACCInvoiceCMN/getAgeingAnalysisSummariesedJasper.do";
                        break;     
                        
                    case Wtf.autoNum.MonthlyCustomerAgedReceivable:
                        exportUrl = "ACCInvoiceCMN/exportMonthlyCustomerAgedReceivable.do";
                        break;
                        
                    case Wtf.autoNum.MonthlyVendorAgedPayable:
                        exportUrl = "ACCGoodsReceiptCMN/exportMonthlyVendorAgedPayable.do";
                        break;   
                    case Wtf.autoNum.FixedAssetReport:
                    case Wtf.autoNum.DisposedAssetsReport:
                        exportUrl = "ACCAsset/exportAssetDetails.do";
                        break;
                    case Wtf.autoNum.CycleCountReport:
                        exportUrl = "INVCycleCount/getCycleCountReport.do";
                        break;
                    case Wtf.autoNum.TransactionInOutReport:
                        exportUrl = "INVGoodsTransfer/getStockMovementList.do";
                        break;
                    case Wtf.autoNum.StockAvailabilityByCustomerWarehouseReport:
                        exportUrl = "ACCInvoiceCMN/getAllUninvoicedConsignmentDetails.do";
                        break;
                    case Wtf.autoNum.StockMovementReport:
                        exportUrl = "INVGoodsTransfer/getDetailedStockMovementList.do";
                        break;
                    case Wtf.autoNum.StoreWiseStockBalanceReport:
                        exportUrl = "INVStockLevel/getStoreWiseStockInventory.do";
                        break;
                     case Wtf.autoNum.StoreWiseStockBalanceSummeryReport:
                        exportUrl = "INVStockLevel/getStoreWiseStockInventorySummary.do";
                        break;
                    case Wtf.autoNum.DateWiseStockTrackingReport:
                        exportUrl = "INVStockLevel/getDateWiseStockInventory.do";
                        break;
                    case Wtf.autoNum.DateWiseBatchStockTrackingReport:
                        exportUrl = "INVStockLevel/getDateWiseBatchStockInventory.do";
                        break;
                    case Wtf.autoNum.BatchwiseStockTrackingReport:
                        exportUrl = "INVStockLevel/getBachWiseStockInventory.do";
                        break;
                    case Wtf.autoNum.StockAdjustmentRegister:
                        exportUrl = "INVStockAdjustment/getStockAdjustmentList.do";
                        break; 
                    case Wtf.autoNum.StockTransferHistoryRegister:
                        exportUrl = "INVGoodsTransfer/getInterStockTransferList.do";
                        break; 
                    case Wtf.autoNum.FullfilledOrdersRegister:
                        exportUrl = "INVGoodsTransfer/getStockRequestList.do";
                        break;      
                    case Wtf.autoNum.FixedAssetDepreciation:
                        exportUrl = "ACCProductCMN/exportAssetDepreciation.do";
                        break; 
                    case Wtf.autoNum.AssetDepreciationReport:
                        exportUrl = "ACCProductCMN/exportAssetDepreciationDetails.do";
                        break;
                        
                    case Wtf.autoNum.CustomerPartyLedgerSummary:
                        exportUrl = "ACCInvoiceCMN/getCustomerPartyLedgerSummariesedJasper.do";
                        break;
            
                    case Wtf.autoNum.CustomerPartyLedgerDetails:
                        exportUrl = "ACCInvoiceCMN/getCustomerPartyLedgerDetailsJasper.do";
                        break;    
                        
                    case Wtf.autoNum.VendorPartyLedgerSummary:
                        exportUrl = "ACCGoodsReceiptCMN/getVendorPartyLedgerSummariesedJasper.do";
                        break;
            
                    case Wtf.autoNum.VendorPartyLedgerDetails:
                        exportUrl = "ACCGoodsReceiptCMN/getVendorPartyLedgerDetailsJasper.do";
                        break; 
                    case Wtf.autoNum.StockReportOnDimension:
                        exportUrl = "ACCProductCMN/getDetailedStockMovementList.do";
                        break;
                    case Wtf.autoNum.inventoryAllStock:
                        exportUrl = "INVStockLevel/getAssetDetailswithStock.do";
                        break;
                    case Wtf.autoNum.StockSummaryReport:
                        exportUrl = "ACCProductCMN/getStockSummaryReport.do";
                        break;
                }
                var url = exportUrl;
                if(this.get == Wtf.autoNum.CustomerPartyLedgerSummary || this.get == Wtf.autoNum.CustomerPartyLedgerDetails || this.get == Wtf.autoNum.VendorPartyLedgerSummary || this.get == Wtf.autoNum.VendorPartyLedgerDetails){
                    this.curdate = this.params != undefined ? this.params.curdate :new Date().format('M d, Y h:m:s A') ;
                    url = exportUrl+"?"+this.mode+this.extra+"&config=&filename="+this.filename+"&filetype="+ this.type+"&stdate="+(this.params?this.params.stdate:"")+"&enddate="+(this.params?this.params.enddate:"")
                    +"&get="+get+"&gridconfig="+jsonGrid+paramsString+"&deleted="+((obj.deleted!=undefined)?obj.deleted:false)+"&nondeleted="+((obj.nondeleted!=undefined)?obj.nondeleted:true)+"&curdate="+this.curdate+"&creditonly=true&mode=18&nondeleted=true&withinventory=true&templateflag="+Wtf.templateflag+"&checkforex="+true;    
                }else{
                    if (this.get == Wtf.autoNum.CustomerAgedReceivable || this.get == Wtf.autoNum.VendorAgedPayable) {
                    this.curdate = new Date().format('M d, Y h:m:s A');
                        url = exportUrl + "?" + this.mode + "&noOfInterval=" + 10 + this.extra + "&config=&filename=" + this.filename + "&filetype=" + this.type + "&enddate=" + (this.params ? this.params.enddate : "") + "&interval=" + ((obj.interval != undefined) ? obj.interval.getValue() : "30") + "&accountid=" + (this.params ? ((this.params.accountid && this.params.accountid != "") ? this.params.accountid : "") : "")
                                + "&get=" + get + "&gridconfig=" + jsonGrid + paramsString + "&deleted=" + ((obj.deleted != undefined) ? obj.deleted : false) + "&nondeleted=" + ((obj.nondeleted != undefined) ? obj.nondeleted : true + "&curdate=" + new Date().format('M d, Y h:m:s A') + "&datefilter=0&ignorezero=true&isdistributive=true&creditonly=true&mode=18&nondeleted=true&withinventory=true&duration=" + ((obj.interval != undefined) ? obj.interval.getValue() : "30")) + "&reportWithoutAging=" + obj.reportWithoutAging + "&templateflag=" + Wtf.templateflag + "&checkforex=" + true;
                    } else {
                        this.curdate = new Date().format('M d, Y h:m:s A');
                        url = exportUrl + "?" + this.mode + "&noOfInterval=" + 10 + this.extra + "&config=&filename=" + this.filename + "&filetype=" + this.type + "&stdate=" + (this.params ? this.params.stdate : "") + "&enddate=" + (this.params ? this.params.enddate : "") + "&interval=" + ((obj.interval != undefined) ? obj.interval.getValue() : "30") + "&accountid=" + (this.params ? ((this.params.accountid && this.params.accountid != "") ? this.params.accountid : "") : "")
                                + "&get=" + get + "&gridconfig=" + jsonGrid + paramsString + "&deleted=" + ((obj.deleted != undefined) ? obj.deleted : false) + "&nondeleted=" + ((obj.nondeleted != undefined) ? obj.nondeleted : true + "&curdate=" + new Date().format('M d, Y h:m:s A') + "&datefilter=0&ignorezero=true&isdistributive=true&creditonly=true&mode=18&nondeleted=true&withinventory=true&duration=" + ((obj.interval != undefined) ? obj.interval.getValue() : "30")) + "&reportWithoutAging=" + obj.reportWithoutAging + "&templateflag=" + Wtf.templateflag + "&checkforex=" + true;
                    }
                }
                
                if(this.ss!=undefined && this.ss!="") {
                    url += "&ss="+this.ss;
                }
                Wtf.get('downloadframe').dom.src = url+orientation; 
            }
            else
            if(this.params&&this.params.statementOfAccountsFlag!=undefined&&this.params.statementOfAccountsFlag && Wtf.templateflag==8 && type == "portraitsubMenu"  )// EXPORT FOR SATS PORTRAIT DESIGN ONLY
            {
                if(this.params.isCustomerSales!=undefined&&this.params.isCustomerSales){
                    
                    var url = "ACCInvoiceCMN/exportSATSCustomerLedgerJasperReport.do?"+this.mode+this.extra+"&config=&filename="+this.filename+"&filetype="+ this.type+"&stdate="+(this.params?this.params.stdate:"")+"&enddate="+(this.params?this.params.enddate:"")+"&interval="+((obj.interval!=undefined) ? obj.interval.getValue() : "30")+"&accountid="+(this.params?((this.params.accountid && this.params.accountid!="")?this.params.accountid:""):"")
                    +"&get="+get+"&gridconfig="+jsonGrid+paramsString+"&deleted="+((obj.deleted!=undefined)?obj.deleted:false)+"&nondeleted="+((obj.nondeleted!=undefined)?obj.nondeleted:true+"&datefilter=0&ignorezero=true&isdistributive=true&creditonly=true&mode=18&nondeleted=true&withinventory=true&duration="+((obj.interval!=undefined) ? obj.interval.getValue() : "30"))+"&templateflag="+Wtf.templateflag;
                }else{
                    var url = "ACCGoodsReceiptCMN/exportSATSVendorLedgerJasperReport.do?"+this.mode+this.extra+"&config=&filename="+this.filename+"&filetype="+ this.type+"&stdate="+(this.params?this.params.stdate:"")+"&enddate="+(this.params?this.params.enddate:"")+"&interval="+((obj.interval!=undefined) ? obj.interval.getValue() : "30")+"&accountid="+(this.params?((this.params.accountid && this.params.accountid!="")?this.params.accountid:""):"")
                    +"&get="+get+"&gridconfig="+jsonGrid+paramsString+"&deleted="+((obj.deleted!=undefined)?obj.deleted:false)+"&nondeleted="+((obj.nondeleted!=undefined)?obj.nondeleted:true+"&datefilter=0&ignorezero=true&isdistributive=true&creditonly=true&mode=18&nondeleted=true&withinventory=true&duration="+((obj.interval!=undefined) ? obj.interval.getValue() : "30"))+"&templateflag="+Wtf.templateflag;
                }
                Wtf.get('downloadframe').dom.src = url+orientation; 
            }else if(this.params&&this.params.statementOfAccountsFlag!=undefined&&this.params.statementOfAccountsFlag)
            {   
                var threadflag=Wtf.account.companyAccountPref.downloadSOAprocess=="1"?true:false;
                
//                if((type=="portraitsubMenuseparateZIP" || type=="portraitsubMenu1separateZIP" || type=="portraitsubMenu2separateZIP"                 
//                || type=="landscapesubMenuseparateZIP" || type=="landscapesubMenu1separateZIP" || type=="landscapesubMenu2separateZIP") && (((this.params.customerIds ==undefined || this.params.customerIds ==""||this.params.customerIds =="All") && this.params.isCustomerSales) ||((this.params.vendorIds ==undefined||this.params.vendorIds ==""||this.params.vendorIds =="All") && !this.params.isCustomerSales))){
//                     WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.soa.exportseparate") ], 2);
//                    return true;
//                }
                if(this.params.isCustomerSales!=undefined&&this.params.isCustomerSales){
                    var url = "ACCInvoiceCMN/exportCustomerLedgerJasperReport.do?";
               }else{
                    var url = "ACCGoodsReceiptCMN/exportVendorLedgerJasperReport.do?";
                }
                
                if(type == "pdf" ){
                    url+="&type=2&isSortedOnCreationDate=false";
                }else if(type == "portraitsubMenu2" ||type == "landscapesubMenu2" || type == "portraitsubMenu2Combine" || type == "landscapesubMenu2Combine" || type=="portraitsubMenu2combinedPDF" || type=="portraitsubMenu2separateZIP" || type=="landscapesubMenu2combinedPDF" || type=="landscapesubMenu2separateZIP"){
                    url+="&type=1&isSortedOnCreationDate=true";
                }else if(type == "portraitsubMenu1" ||type == "landscapesubMenu1" || type == "portraitsubMenu1Combine" || type == "landscapesubMenu1Combine"|| type=="portraitsubMenu1combinedPDF" || type=="portraitsubMenu1separateZIP" || type=="landscapesubMenu1combinedPDF" || type=="landscapesubMenu1separateZIP"){
                    url+="&type=1&isSortedOnCreationDate=false";
                }else if(type == "portraitsubMenu1Split" || type == "landscapesubMenu1Split"){
                    url+="&type=3&isSortedOnCreationDate=false";
                }else if(type == "portraitsubMenu2Split" || type == "landscapesubMenu2Split"){
                    url+="&type=3&isSortedOnCreationDate=true";
                }else{
                    url+="&type=0&isSortedOnCreationDate=false";
                }
                   url+=orientation+paramsString+"&templateflag="+Wtf.templateflag+"&checkforex="+true+"&isLetterHead="+Wtf.account.companyAccountPref.defaultTemplateLogoFlag+"&typeoffile="+type;//+"&reportid="+this.reportid; 

                 if(threadflag){
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.info"), //'Warning',
                        msg: WtfGlobal.getLocaleText("acc.generalLedger.msg.downloadTakeTime"),
                        buttons: Wtf.MessageBox.YESNOCANCEL,
                        closable : false,
                        fn:function(btn){
                            if(btn=="yes") {
                                url +="&threadflag="+true;
                                Wtf.get('downloadframe').dom.src = url;
                            } else if(btn=="no") {
                                url +="&threadflag="+false;
                                Wtf.get('downloadframe').dom.src = url;
                            }
                        },
                        scope:this,
                        icon: Wtf.MessageBox.QUESTION
                    });
                 }else
                {
                    Wtf.get('downloadframe').dom.src = url ;        
                }
        }
            else if(this.params && this.params.isfinancereport!=undefined && this.params.isfinancereport){
                var url = "ACCOtherReports/exportFinanceDetailsJasper.do?"+this.mode+this.extra+"&config=&filename="+this.filename+"&filetype="+ this.type+"&stdate="+(this.params?this.params.stdate:"")+"&startdate="+(this.params?this.params.startdate:"")+"&enddate="+(this.params?this.params.enddate:"")+"&interval="+((obj.interval!=undefined) ? obj.interval.getValue() : "30")+"&accountid="+(this.params?((this.params.accountid && this.params.accountid!="")?this.params.accountid:""):"")
                +"&get="+get+"&gridconfig="+jsonGrid+paramsString+"&deleted="+((obj.deleted!=undefined)?obj.deleted:false)+"&nondeleted="+((obj.nondeleted!=undefined)?obj.nondeleted:true+"&curdate="+new Date().format('M d, Y h:m:s A')+"&datefilter=0&ignorezero=true&isdistributive=true&creditonly=true&mode=18&nondeleted=true&withinventory=true&duration="+((obj.interval!=undefined) ? obj.interval.getValue() : "30"))+"&templateflag="+Wtf.templateflag;
                Wtf.get('downloadframe').dom.src = url; 
            }
            else if(this.params && this.params.isStockLedger!=undefined && this.params.isStockLedger){
                var url = "ACCProductCMN/exportStockLedgerJapser.do?"+this.mode+this.extra+"&config=&filename="+this.filename+"&filetype="+ this.type+"&startdate="+(this.params?this.params.startdate:"")+"&enddate="+(this.params?this.params.enddate:"")+"&interval="+((obj.interval!=undefined) ? obj.interval.getValue() : "30")+"&accountid="+(this.params?((this.params.accountid && this.params.accountid!="")?this.params.accountid:""):"")
                +"&get="+get+"&gridconfig="+jsonGrid+paramsString+"&deleted="+((obj.deleted!=undefined)?obj.deleted:false)+"&nondeleted="+((obj.nondeleted!=undefined)?obj.nondeleted:true+"&curdate="+new Date().format('M d, Y h:m:s A')+"&datefilter=0&ignorezero=true&isdistributive=true&creditonly=true&mode=18&nondeleted=true&withinventory=true&duration="+((obj.interval!=undefined) ? obj.interval.getValue() : "30"))+"&templateflag="+Wtf.templateflag;
                Wtf.get('downloadframe').dom.src = url+orientation; 
            }
            else if(this.params && this.params.isStockAgeing!=undefined && this.params.isStockAgeing){
                var url = "ACCProductCMN/exportStockAgeingJapser.do?"+this.mode+this.extra+"&config=&filename="+this.filename+"&filetype="+ this.type+"&startdate="+(this.params?(this.params.startdate!=undefined?this.params.startdate:""):"")+"&enddate="+(this.params?(this.params.enddate!=undefined?this.params.enddate:""):"")+"&interval="+((obj.interval!=undefined) ? obj.interval.getValue() : "30")+"&accountid="+(this.params?((this.params.accountid && this.params.accountid!="")?this.params.accountid:""):"")
                +"&get="+get+"&gridconfig="+jsonGrid+paramsString+"&deleted="+((obj.deleted!=undefined)?obj.deleted:false)+"&nondeleted="+((obj.nondeleted!=undefined)?obj.nondeleted:true+"&curdate="+new Date().format('M d, Y h:m:s A')+"&datefilter=0&ignorezero=true&isdistributive=true&creditonly=true&mode=18&nondeleted=true&withinventory=true&duration="+((obj.interval!=undefined) ? obj.interval.getValue() : "30"))+"&templateflag="+Wtf.templateflag+"&valuationMethod="+(this.params.valuationMethod!=undefined?this.params.valuationMethod:"");
                Wtf.get('downloadframe').dom.src = url+orientation; 
            }
            else if(this.params && ((this.params.isGeneralLedger!=undefined && this.params.isGeneralLedger)||(this.params.isGroupDetailReport && (this.params.issubGeneralLedger ||  type == "portraitsubMenu" ||type == "landscapesubMenu")))){
                 if(this.params.exportThreadFlagLedger && !(this.params && this.params.issubGeneralLedger!=undefined && this.params.issubGeneralLedger)) {
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.info"), //'Warning',
                        msg: WtfGlobal.getLocaleText("acc.generalLedger.msg.downloadTakeTime"),
                        buttons: Wtf.MessageBox.YESNOCANCEL,
                        closable : false,
                        fn:function(btn){
                           var url = "ACCReports/exportGeneralLedger.do?";
//                            var url = "CommonExportController/exportFile.do?";
                            var filetype = (type == "portraitsubMenu" ||type == "landscapesubMenu")?"pdf":type;
                            var stdate = "&stdate="+(this.params?this.params.stdate:"");
                            var accountIds="";
                            if(this.params.isGroupDetailReport == true){
                                url = "ACCReports/exportGroupDetailReport.do?";
//                                url = "CommonExportController/exportFile.do?";
                                filetype = (type == "portraitsubMenu" ||type == "landscapesubMenu")?"detailedPDF":type;
                                if(obj.startDate && obj.endDate){
                                    stdate = "&startdate="+WtfGlobal.convertToGenericDate(obj.startDate.getValue());
                                    stdate += "&stdate="+WtfGlobal.convertToGenericDate(obj.startDate.getValue());
                                    stdate += "&enddate="+WtfGlobal.convertToGenericDate(obj.endDate.getValue()) ;
                                }
                                if(obj.MultiSelectAccCombo){
                                    accountIds = "&accountIds="+obj.MultiSelectAccCombo.getValue(true).join(",");
                                }
                                if(obj.accountViewTypeCombo){
                                    accountIds += "&showAccountsInGroup="+obj.accountViewTypeCombo.getValue();
                                }
                                if(obj.searchJson){
                                    paramsString += "&searchJson="+obj.searchJson;
                                }
                                if(obj.filterConjuctionCrit){
                                    paramsString += "&filterConjuctionCriteria="+obj.filterConjuctionCrit;
                                }
                                if (obj.MultiSelectCurrencyCombo) {
                                    paramsString += "&currencyIds=" + obj.MultiSelectCurrencyCombo.getValue();
                                }
                                if (obj.balPLTypeCombo) {
                                    paramsString += "&typeid=" + obj.balPLTypeCombo.getValue();
                                }
                                if (obj.excludeTypeCmb) {
                                    paramsString += "&excludePreviousYear=" + obj.excludeTypeCmb.getValue();
                                }
                                if (obj.accountMasterTypeCombo) {
                                    paramsString += "&mastertypeid=" + obj.accountMasterTypeCombo.getValue();
                                }
                                if (obj.typeEditor) {
                                    paramsString += "&acctypes=" + obj.typeEditor.getValue();
                                    paramsString += "&accountTransactionType=" + obj.typeEditor.getValue();
                                }
                            }
                            if(btn=="yes") {
                                url += this.mode+this.extra+"&config=&filename="+this.filename+"&filetype="+ filetype +accountIds + stdate+"&interval="+((obj.interval!=undefined) ? obj.interval.getValue() : "30")+"&accountid="+(this.params?((this.params.accountid && this.params.accountid!="")?this.params.accountid:""):"")
                                +"&get="+get+paramsString+"&deleted="+((obj.deleted!=undefined)?obj.deleted:false)+"&nondeleted="+((obj.nondeleted!=undefined)?obj.nondeleted:true+"&curdate="+new Date().format('M d, Y h:m:s A')+"&datefilter=0&ignorezero=true&isdistributive=true&creditonly=true&mode=18&nondeleted=true&withinventory=true&duration="+((obj.interval!=undefined) ? obj.interval.getValue() : "30"))+"&templateflag="+Wtf.templateflag+"&threadflag="+true;
                                Wtf.get('downloadframe').dom.src = url+orientation+"&module=GeneralLedger"+"&ReportName=General Ledger Report"; 
                            } else if(btn=="no") {
                                url += this.mode+this.extra+"&config=&filename="+this.filename+"&filetype="+ filetype +accountIds + stdate+"&interval="+((obj.interval!=undefined) ? obj.interval.getValue() : "30")+"&accountid="+(this.params?((this.params.accountid && this.params.accountid!="")?this.params.accountid:""):"")
                                +"&get="+get+paramsString+"&deleted="+((obj.deleted!=undefined)?obj.deleted:false)+"&nondeleted="+((obj.nondeleted!=undefined)?obj.nondeleted:true+"&curdate="+new Date().format('M d, Y h:m:s A')+"&datefilter=0&ignorezero=true&isdistributive=true&creditonly=true&mode=18&nondeleted=true&withinventory=true&duration="+((obj.interval!=undefined) ? obj.interval.getValue() : "30"))+"&templateflag="+Wtf.templateflag+"&threadflag="+false;
                                Wtf.get('downloadframe').dom.src = url+orientation+"&module=GeneralLedger"+"&ReportName=General Ledger Report"; 
                            }
                        },
                        scope:this,
                        icon: Wtf.MessageBox.QUESTION
                    });
                } else {
                    if(this.params && this.params.issubGeneralLedger!=undefined && this.params.issubGeneralLedger){
                        if(this.params.mainGroupJSON==undefined || this.params.subGroupJSON==undefined){
                            return false;
                        } else if(this.params.mainGroupJSON==this.params.subGroupJSON){
                            return false;
                        }
                    }
                   var url = "ACCReports/exportGeneralLedger.do?";
//                    var url = "CommonExportController/exportFile.do?";
                    filetype = this.type;
                    if(this.params.isGroupDetailReport){
                        url = "ACCReports/exportGroupDetailReport.do?";
//                        url = "CommonExportController/exportFile.do?";
                        if(obj.startDate){
                            url += "&startdate="+WtfGlobal.convertToGenericDate(obj.startDate.getValue());
                            url += "&stdate="+WtfGlobal.convertToGenericDate(obj.startDate.getValue());
                        }
                        if(obj.endDate){
                            url += "&enddate="+WtfGlobal.convertToGenericDate(obj.endDate.getValue()) ;
                        }
                        if(obj.MultiSelectAccCombo){
                            url += "&accountIds="+obj.MultiSelectAccCombo.getValue(true).join(",");
                        }
                        if(obj.accountViewTypeCombo){
                            url += "&showAccountsInGroup="+obj.accountViewTypeCombo.getValue();
                        }
                        if(obj.searchJson){
                            paramsString += "&searchJson="+obj.searchJson;
                        }
                        if (obj.excludeTypeCmb) {
                            paramsString +="&excludePreviousYear=" + obj.excludeTypeCmb.getValue();
                        }
                        if(obj.filterConjuctionCrit){
                            paramsString += "&filterConjuctionCriteria="+obj.filterConjuctionCrit;
                        }
                        if (obj.MultiSelectCurrencyCombo) {
                            paramsString += "&currencyIds=" + obj.MultiSelectCurrencyCombo.getValue();
                        }
                        if (obj.balPLTypeCombo) {
                            paramsString += "&typeid=" + obj.balPLTypeCombo.getValue();
                        }
                        if (obj.excludeTypeCmb) {
                            paramsString += "&excludePreviousYear=" + obj.excludeTypeCmb.getValue();
                        }
                        if (obj.accountMasterTypeCombo) {
                            paramsString += "&mastertypeid=" + obj.accountMasterTypeCombo.getValue();
                        }
                        if (obj.typeEditor) {
                            paramsString += "&acctypes=" + obj.typeEditor.getValue();
                            paramsString += "&accountTransactionType=" + obj.typeEditor.getValue();
                        }
                        filetype = (type == "portraitsubMenu" ||type == "landscapesubMenu")?"detailedPDF":type;
                    }
                    var paramsWithoutURL = this.mode + this.extra + "&config=&filename=" + this.filename + "&filetype=" + filetype + "&stdate=" + (this.params ? this.params.stdate : "") + "&interval=" + ((obj.interval != undefined) ? obj.interval.getValue() : "30") + "&accountid=" + (this.params ? ((this.params.accountid && this.params.accountid != "") ? this.params.accountid : "") : "")
                            + "&get=" + get + paramsString + "&deleted=" + ((obj.deleted != undefined) ? obj.deleted : false) + "&nondeleted=" + ((obj.nondeleted != undefined) ? obj.nondeleted : true + "&curdate=" + new Date().format('M d, Y h:m:s A') + "&datefilter=0&ignorezero=true&isdistributive=true&creditonly=true&mode=18&nondeleted=true&withinventory=true&duration=" + ((obj.interval != undefined) ? obj.interval.getValue() : "30")) + "&templateflag=" + Wtf.templateflag+"&module=GeneralLedger"+"&ReportName=General Ledger Report";
//                    Wtf.get('downloadframe').dom.src = url + orientation;
                    this.postData(url, paramsWithoutURL + orientation);
                }    
            }
            else if(this.params && this.params.isStockValuationLoc!=undefined && this.params.isStockValuationLoc){
                var url = "ACCProductCMN/exportStockValByLocationJasper.do?"+this.mode+this.extra+"&config=&filename="+this.filename+"&filetype="+ this.type+"&startdateReport="+WtfGlobal.convertToGenericDate(obj.startDate.getValue())+"&enddateReport="+WtfGlobal.convertToGenericDate(obj.endDate.getValue())+"&interval="+((obj.interval!=undefined) ? obj.interval.getValue() : "30")+"&accountid="+(this.params?((this.params.accountid && this.params.accountid!="")?this.params.accountid:""):"")+"&type="+ obj.locTypeEditor.getValue()
                +"&get="+get+"&gridconfig="+jsonGrid+paramsString+"&deleted="+((obj.deleted!=undefined)?obj.deleted:false)+"&nondeleted="+((obj.nondeleted!=undefined)?obj.nondeleted:true+"&curdate="+new Date().format('M d, Y h:m:s A')+"&datefilter=0&ignorezero=true&isdistributive=true&creditonly=true&mode=18&nondeleted=true&withinventory=true&duration="+((obj.interval!=undefined) ? obj.interval.getValue() : "30"))+"&templateflag="+Wtf.templateflag+"&locationid="+obj.locItemEditor.getValue()+"&batchid="+obj.batchEditor.getValue()+"&ss="+obj.quickPanelSearch.getValue();
                Wtf.get('downloadframe').dom.src = url; 
            }
            else if(this.params && this.params.isStockSummary!=undefined && this.params.isStockSummary){
                var url = "ACCProductCMN/exportStockSummaryJasper.do?"+this.mode+this.extra+"&config=&filename="+this.filename+"&filetype="+ this.type+"&type="+ obj.locTypeEditor.getValue() +"&startdateReport="+ WtfGlobal.convertToGenericDate(obj.startDate.getValue())+"&enddateReport="+WtfGlobal.convertToGenericDate(obj.endDate.getValue())+"&interval="+((obj.interval!=undefined) ? obj.interval.getValue() : "30")+"&accountid="+(this.params?((this.params.accountid && this.params.accountid!="")?this.params.accountid:""):"")
                +"&get="+get+"&gridconfig="+jsonGrid+paramsString+"&deleted="+((obj.deleted!=undefined)?obj.deleted:false)+"&nondeleted="+((obj.nondeleted!=undefined)?obj.nondeleted:true+"&curdate="+new Date().format('M d, Y h:m:s A')+"&datefilter=0&ignorezero=true&isdistributive=true&creditonly=true&mode=18&nondeleted=true&withinventory=true&duration="+((obj.interval!=undefined) ? obj.interval.getValue() : "30"))+"&templateflag="+Wtf.templateflag+"&ss="+obj.quickPanelSearch.getValue();
                Wtf.get('downloadframe').dom.src = url; 
            }
            else if(this.params && this.params.isBankBook!=undefined /*&& this.params.isBankBook*/){
                var url = "ACCReports/exportBankBook.do?"+paramsString+"&templateflag="+Wtf.templateflag;
                Wtf.get('downloadframe').dom.src = url+orientation; 
            }
            else if(get == Wtf.autoNum.Receipt && this.params && this.params.isCustomReportForF1Recreation!=undefined && this.params.isCustomReportForF1Recreation){
                var url = "ACCReceiptCMN/exportF1ReceiptReport.do?"+paramsString+"&templateflag="+Wtf.templateflag;
                Wtf.get('downloadframe').dom.src = url;
            }
            else if(this.params && this.params.isinventoryDetails!=undefined && this.params.isinventoryDetails){
                var url = "ACCOtherReports/exportInventoryMovementReportJasper.do?"+paramsString;
                Wtf.get('downloadframe').dom.src = url; 
            }
            else if(Wtf.templateflag == Wtf.Monzone_templateflag && (get == Wtf.autoNum.CreditNote || get == Wtf.autoNum.DebitNote)){
                var url = "ACCInvoiceCMN/exportCreditNoteJasperReportForMonzone.do?"+paramsString+"&templateflag="+Wtf.templateflag;   //Credit and Debit Note register for Monzone
                Wtf.get('downloadframe').dom.src = url;
            }
            else if((this.get == Wtf.autoNum.CreditNote||this.get == Wtf.autoNum.DebitNote) && this.params && this.isPDF){
                var url = "ACCOtherReports/exportCreditNoteRegister.do?"+paramsString+"&templateflag="+Wtf.templateflag+"&isDetailPDF="+this.detailedXls;               
                Wtf.get('downloadframe').dom.src = url; 
            }
            else if(this.get == Wtf.autoNum.Invoice && this.params && this.detailedXls==true && type == "detailedXls"){
                var url = "ACCInvoiceCMN/exportSalesInvoiceRegisterXlxReport.do?"+paramsString+"&templateflag="+Wtf.templateflag+"&isDetailPDF="+this.detailedXls;               
                Wtf.get('downloadframe').dom.src = url; 
            }
            else if(this.get == Wtf.autoNum.DeliveryOrder && this.params && this.detailedXls==true && type == "detailedXls" ){
                var url = "ACCInvoiceCMN/exportDeliveryOrderRegisterXlxReport.do?"+paramsString+"&templateflag="+Wtf.templateflag+"&isDetailPDF="+this.detailedXls;               
                Wtf.get('downloadframe').dom.src = url; 
            }
              else if((this.get==Wtf.autoNum.Invoice || this.get==Wtf.autoNum.GoodsReceipt) && this.params && !this.params.isBasedOnProduct){
                var url="";
                if(this.get==Wtf.autoNum.Invoice){
                    url = "ACCInvoiceCMN/exportSalesInvoiceRegisterReport.do?"+paramsString+"&templateflag="+Wtf.templateflag+orientation+"&dtype=report";
                } else if (this.get==Wtf.autoNum.GoodsReceipt){
                    url = "ACCGoodsReceiptCMN/exportVendorInvoiceRegisterReport.do?"+paramsString+"&templateflag="+Wtf.templateflag+orientation+"&dtype=report";
                }
                Wtf.get('downloadframe').dom.src = url; 
            }
             else if(this.get == Wtf.autoNum.GoodsReceipt && this.params && this.detailedXls==true && type == "detailedXls"){
                var url = "ACCGoodsReceiptCMN/exportVendorInvoiceRegisterXlsReport.do?"+paramsString+"&templateflag="+Wtf.templateflag+"&isDetailPDF="+this.detailedXls+"&filetype=xls";               
                Wtf.get('downloadframe').dom.src = url; 
            }
             else if(this.get == Wtf.autoNum.PurchaseOrder && this.params && this.detailedXls==true && type == "detailedXls"){
                var url = "ACCPurchaseOrderCMN/exportPurchaseOrderRegisterXlsReport.do?"+paramsString+"&templateflag="+Wtf.templateflag+"&isDetailPDF="+this.detailedXls;               
                Wtf.get('downloadframe').dom.src = url; 
            }
            else if(this.get == Wtf.autoNum.GoodsReceipt && this.params && (this.isDetailPDF==true || this.isSummaryPDF==true) && Wtf.templateflag==Wtf.Monzone_templateflag){
                var url = "ACCGoodsReceiptCMN/exportVendorInvoiceRegisterReport.do?"+paramsString+"&templateflag="+Wtf.templateflag+"&isDetailPDF="+this.isDetailPDF;               
                Wtf.get('downloadframe').dom.src = url; 
            }          
            else if((this.get == Wtf.autoNum.agedSummaryBasedOnSalesPerson || this.get == Wtf.autoNum.agedDetailBasedOnSalesPerson) && this.params){
                var url = "ACCInvoiceCMN/exportAgedReportBasedOnSalesPerson.do?"+paramsString+"&templateflag="+Wtf.templateflag+"&isDetailPDF="+(this.get == Wtf.autoNum.agedSummaryBasedOnSalesPerson?false:true)+"&searchJson=" + (obj.searchJson !=undefined ?  obj.searchJson :"")+"&filterConjuctionCriteria="+obj.filterConjuctionCrit+"&ignorezero="+true;
                Wtf.get('downloadframe').dom.src = url;
            }
            else if(get == Wtf.autoNum.BankReconcilation && this.isPDF==true){
                    Wtf.get('downloadframe').dom.src = "ACCReports/exportBankReconciliation.do?&accountid=" + this.exportRecord.accountid + "&stdate=" + this.exportRecord.stdate +"&enddate="+ this.exportRecord.enddate + "&isConcileReport=" + this.exportRecord.isConcileReport+"&ss=" + this.exportRecord.ss+"&dateFilter=" +this.exportRecord.dateFilter+"&isMaintainHistory="+true+"&templateflag="+Wtf.templateflag;
            
            }else if(get == Wtf.autoNum.BankReconcilation && this.isDetailPDF==true){
                    Wtf.get('downloadframe').dom.src = "ACCReports/exportBankReconciliation.do?&accountid=" + this.exportRecord.accountid + "&stdate=" + this.exportRecord.stdate +"&enddate="+ this.exportRecord.enddate + "&isConcileReport=" + this.exportRecord.isConcileReport+"&ss=" + this.exportRecord.ss+"&dateFilter=" +this.exportRecord.dateFilter+"&isMaintainHistory="+false+"&templateflag="+Wtf.templateflag;
            
            } else if(obj.reportID == Wtf.autoNum.PurchaseByVendorReport && type == "pdf"){
                var url = "ACCGoodsReceiptCMN/exportPurchaseByVendor.do?"+paramsString + "&searchJson=" + (obj.searchJson !=undefined ?  obj.searchJson :"")+"&filetype="+type+"&moduleid=" + obj.moduleid +"&filterConjuctionCriteria="+obj.filterConjuctionCrit;               
                Wtf.get('downloadframe').dom.src = url; 
            } else if(this.get== Wtf.autoNum.VendorCustomerPriceListReport && type == "pdf"){ // Price List Report
                var url = "ACCReports/exportCustVenProductsPrice.do?"+paramsString+"&filetype="+type;               
                Wtf.get('downloadframe').dom.src = url; 
            }else if(this.get==Wtf.autoNum.SalesOrder && type=="summaryPDF" && (Wtf.templateflag == Wtf.hinsitsu_templateflag)){
                var url="ACCInvoiceCMN/exportCheckListSO.do?"+paramsString+"&templateflag="+Wtf.templateflag;
                Wtf.get('downloadframe').dom.src = url;
            }else if (this.get == Wtf.autoNum.GroupDetailReport && (type == "portraitsummarySubMenucombinedPDF" || type == "landscapesummarySubMenucombinedPDF")) {
               if (obj.MultiSelectAccCombo) {
                    accountIds = "&accountIds=" + obj.MultiSelectAccCombo.getValue(true).join(",");
                }
                if (obj.searchJson) {
                    paramsString += "&searchJson=" + obj.searchJson;
                }
                if (obj.filterConjuctionCrit) {
                    paramsString += "&filterConjuctionCriteria=" + obj.filterConjuctionCrit;
                }
                if (obj.MultiSelectCurrencyCombo) {
                    paramsString += "&currencyIds=" + obj.MultiSelectCurrencyCombo.getValue();
                }
                if (obj.balPLTypeCombo) {
                    paramsString += "&typeid=" + obj.balPLTypeCombo.getValue();
                }
                if (obj.excludeTypeCmb) {
                    paramsString += "&excludePreviousYear=" + obj.excludeTypeCmb.getValue();
                }
                if (obj.accountMasterTypeCombo) {
                    paramsString += "&mastertypeid=" + obj.accountMasterTypeCombo.getValue();
                }
                if (obj.typeEditor) {
                    paramsString += "&acctypes=" + obj.typeEditor.getValue();
                    paramsString += "&accountTransactionType=" + obj.typeEditor.getValue();
                }

                if (this.params.exportThreadFlagLedger) {
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.info"), //'Warning',
                        msg: WtfGlobal.getLocaleText("acc.generalLedger.msg.downloadTakeTime"),
                        buttons: Wtf.MessageBox.YESNOCANCEL,
                        closable: false,
                        scope: this,
                        fn: function (btn) {

                            if (btn == "yes") {
                                var url = "ACCReports/exportGroupDetailReport.do?" + "&config=&filename=" + this.filename + accountIds + paramsString + "&filetype=pdf&showAccountsInGroup=false&startdate=" + WtfGlobal.convertToGenericDate(obj.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericDate(obj.endDate.getValue()) + "&threadflag=" + true + "&isLandscape=" + this.isLandscape;
//                                var url = "CommonExportController/exportFile.do?" + "&config=&filename=" + this.filename + accountIds + paramsString + "&filetype=pdf&showAccountsInGroup=false&startdate=" + WtfGlobal.convertToGenericDate(obj.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericDate(obj.endDate.getValue()) + "&threadflag=" + true + "&isLandscape=" + this.isLandscape+"&module=GeneralLedger"+"&ReportName=General Ledger Report";
                                Wtf.get('downloadframe').dom.src = url;
                            } else if (btn == "no") {
                                var url = "ACCReports/exportGroupDetailReport.do?" + "&config=&filename=" + this.filename + accountIds + paramsString + "&filetype=pdf&showAccountsInGroup=false&startdate=" + WtfGlobal.convertToGenericDate(obj.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericDate(obj.endDate.getValue()) + "&threadflag=" + false + "&isLandscape=" + this.isLandscape;
//                                var url = "CommonExportController/exportFile.do?" + "&config=&filename=" + this.filename + accountIds + paramsString + "&filetype=pdf&showAccountsInGroup=false&startdate=" + WtfGlobal.convertToGenericDate(obj.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericDate(obj.endDate.getValue()) + "&threadflag=" + true + "&isLandscape=" + this.isLandscape+"&module=GeneralLedger"+"&ReportName=General Ledger Report";
                                Wtf.get('downloadframe').dom.src = url;
                            }
                        }});
                }
                else {
                    var url = "ACCReports/exportGroupDetailReport.do?" + "&config=&filename=" + this.filename + accountIds + paramsString + "&filetype=pdf&showAccountsInGroup=false&startdate=" + WtfGlobal.convertToGenericDate(obj.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericDate(obj.endDate.getValue()) + "&threadflag=" + false + "&isLandscape=" + this.isLandscape;
//                    var url = "CommonExportController/exportFile.do?" + "&config=&filename=" + this.filename + accountIds + paramsString + "&filetype=pdf&showAccountsInGroup=false&startdate=" + WtfGlobal.convertToGenericDate(obj.startDate.getValue()) + "&enddate=" + WtfGlobal.convertToGenericDate(obj.endDate.getValue()) + "&threadflag=" + true + "&isLandscape=" + this.isLandscape+"&module=GeneralLedger"+"&ReportName=General Ledger Report";
                    Wtf.get('downloadframe').dom.src = url;
                }


            }
//            else if(type=="summaryPDF" && this.get == Wtf.autoNum.StockAdjustmentRegister){
//                var url ="INVStockAdjustment/getStockAdjustmentList.do?"+paramsString+"&reporttype=summary";
//                 Wtf.get('downloadframe').dom.src = url;
//            }else if(type=="detailPDF" && this.get == Wtf.autoNum.StockAdjustmentRegister){
//                var url ="INVStockAdjustment/getStockAdjustmentList.do?"+paramsString+"&reporttype=detail";
//                 Wtf.get('downloadframe').dom.src = url;
//            }
            else{
//                if(this.params && this.params.isinventoryDetails!=undefined && !this.params.isinventoryDetails && this.params.noOfMonths > 6){
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.inventoryMovementSummary.exportPdf.alert") ], 2);
//                    return;
//                }
                if(Wtf.getCmp("selectexportwinpdf")==undefined){
                    new Wtf.selectTempWin({                                                                                                                                                                                                                                                                                      
                        type:type,
                        get:get,
                        stdate:this.params? (this.params.stdate? this.params.stdate:"" ) : "",
                        enddate:this.params? (this.params.enddate? this.params.enddate:"") : "",                                                                            
                        ss : (obj.quickSearchTF!=undefined) ? obj.quickSearchTF.getValue() : "",
                        interval:(obj.interval!=undefined) ? obj.interval.getValue() : "30",
                        nondeleted:(obj.nondeleted!=undefined)?obj.nondeleted:true,
                        deleted:(obj.deleted!=undefined)?obj.deleted:false,
                        accountid:this.params?((this.params.accountid && this.params.accountid!="")?this.params.accountid:""):"",
                        extra:this.extra||{},
                        id:"selectexportwinpdf",
                        mode:Wtf.urlEncode(obj.grid.getStore().baseParams),
                        compStoreParams:obj.grid.getStore().lastOptions!=null?Wtf.urlEncode(obj.grid.getStore().lastOptions.params):Wtf.urlEncode(this.params),
                        paramstring:paramsString,
                        isProductExport:this.isProductExport,
                        isProductExportRecordsGreaterThanThousands:this.isProductExportRecordsGreaterThanThousands,
                        totalRecords:totalRecords,
                        filename:(get==220)?encodeURIComponent('TaxesReport'):this.filename,
                        storeToload:obj.pdfStore,
                        gridConfig : jsonGrid,
                        grid:obj.EditorGrid,
                        usePostMethod:this.usePostMethod,
                        json:(obj.searchJson!=undefined)?obj.searchJson:""
                    });
                }
            }
        }else if((type == "csv" ||type == "xls" )&& this.params && this.params.isGeneralLedger!=undefined && this.params.isGeneralLedger){
            if(this.params.exportThreadFlagLedger) {                
                
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.info"), //'Warning',                    
                    msg: WtfGlobal.getLocaleText("acc.generalLedger.msg.downloadTakeTime"),
                    buttons: Wtf.MessageBox.YESNOCANCEL,
                    closable : false,
                    width:600,
                    fn:function(btn){
                        if(btn=="yes") {
                            var url = "ACCReports/exportGeneralLedger.do?"+this.mode+this.extra+"&config=&filename="+this.filename+"&filetype="+type+"&stdate="+(this.params?this.params.stdate:"")+"&startdate="+"&interval="+((obj.interval!=undefined) ? obj.interval.getValue() : "30")+"&accountid="+(this.params?((this.params.accountid && this.params.accountid!="")?this.params.accountid:""):"")
                            +"&get="+get+"&gridconfig="+jsonGrid+paramsString+"&deleted="+((obj.deleted!=undefined)?obj.deleted:false)+"&nondeleted="+((obj.nondeleted!=undefined)?obj.nondeleted:true+"&curdate="+new Date().format('M d, Y h:m:s A')+"&datefilter=0&ignorezero=true&isdistributive=true&creditonly=true&mode=18&nondeleted=true&withinventory=true&duration="+((obj.interval!=undefined) ? obj.interval.getValue() : "30"))+"&templateflag="+Wtf.templateflag+"&threadflag="+true+"&isLedgerPrintCSV="+true;
                            Wtf.get('downloadframe').dom.src = url;
                        } else if(btn=="no") {
                            var url = "ACCReports/exportGeneralLedger.do?"+this.mode+this.extra+"&config=&filename="+this.filename+"&filetype="+ type+"&stdate="+(this.params?this.params.stdate:"")+"&startdate="+"&interval="+((obj.interval!=undefined) ? obj.interval.getValue() : "30")+"&accountid="+(this.params?((this.params.accountid && this.params.accountid!="")?this.params.accountid:""):"")
                            +"&get="+get+"&gridconfig="+jsonGrid+paramsString+"&deleted="+((obj.deleted!=undefined)?obj.deleted:false)+"&nondeleted="+((obj.nondeleted!=undefined)?obj.nondeleted:true+"&curdate="+new Date().format('M d, Y h:m:s A')+"&datefilter=0&ignorezero=true&isdistributive=true&creditonly=true&mode=18&nondeleted=true&withinventory=true&duration="+((obj.interval!=undefined) ? obj.interval.getValue() : "30"))+"&templateflag="+Wtf.templateflag+"&threadflag="+false+"&isLedgerPrintCSV="+true;
                            Wtf.get('downloadframe').dom.src = url;
                        }
                    },
                    scope:this,
                    icon: Wtf.MessageBox.QUESTION
                });
                
                
            } else {
                var url = "ACCReports/exportGeneralLedger.do?"+this.mode+this.extra+"&config=&filename="+this.filename+"&filetype="+type+"&stdate="+(this.params?this.params.stdate:"")+"&startdate="+"&interval="+((obj.interval!=undefined) ? obj.interval.getValue() : "30")+"&accountid="+(this.params?((this.params.accountid && this.params.accountid!="")?this.params.accountid:""):"")
                +"&get="+get+"&gridconfig="+jsonGrid+paramsString+"&deleted="+((obj.deleted!=undefined)?obj.deleted:false)+"&nondeleted="+((obj.nondeleted!=undefined)?obj.nondeleted:true+"&curdate="+new Date().format('M d, Y h:m:s A')+"&datefilter=0&ignorezero=true&isdistributive=true&creditonly=true&mode=18&nondeleted=true&withinventory=true&duration="+((obj.interval!=undefined) ? obj.interval.getValue() : "30"))+"&templateflag="+Wtf.templateflag+"&isLedgerPrintCSV="+true;
                Wtf.get('downloadframe').dom.src = url; 
            }            
        }else if ( Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && Wtf.account.companyAccountPref.stateid==Wtf.StateName.GUJARAT && (get == Wtf.autoNum.Invoice || get == Wtf.autoNum.GoodsReceipt) && (type=="form201AB" || type=="form201C")) {//For Indian Company and only for Gujrat
            var isForm202 = false;
            var startDate = "";
            var endDate = "";
            if(obj.startDate!=undefined && obj.startDate && obj.startDate.getValue()){
                startDate = obj.startDate.getValue().format('Y-m-d');
            }
            if(obj.endDate!=undefined && obj.endDate.getValue()){
                endDate = obj.endDate.getValue().format('Y-m-d');
            }
            if(type=="form201AB"){
                if(obj.endDate!=undefined && obj.startDate!=undefined){
                    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportForm21AInvoiceJasper.do?moduleid="+get+"&templateflag="+Wtf.templateflag+"&isform202="+isForm202+"&startdate=" + startDate+"&enddate=" + endDate;
                }
            }else if(type=="form201C"){
                if(obj.endDate!=undefined && obj.startDate!=undefined){
                    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportForm201CJasper.do?report=FORM_703&filetype=pdf"+"&startdate=" + startDate+"&enddate=" + endDate;
                }
            }
        }else {
            if(this.params && this.params.isGroupDetailReport == true){
                if (obj.MultiSelectCurrencyCombo) {
                    paramsString += "&currencyIds=" + obj.MultiSelectCurrencyCombo.getValue();
                }
                if (obj.balPLTypeCombo) {
                    paramsString += "&typeid=" + obj.balPLTypeCombo.getValue();
                }
                if (obj.excludeTypeCmb) {
                    paramsString += "&excludePreviousYear=" + obj.excludeTypeCmb.getValue();
                }
                if (obj.accountMasterTypeCombo) {
                    paramsString += "&mastertypeid=" + obj.accountMasterTypeCombo.getValue();
                }
                if (obj.typeEditor) {
                    paramsString += "&acctypes=" + obj.typeEditor.getValue();
                    paramsString += "&accountTransactionType=" + obj.typeEditor.getValue();
                }
            }
             
//            paramsString += "&ReportName=General Ledger Report&module=GeneralLedger";
             
            obj.jsonGrid = jsonGrid;
            obj.totalRecords = totalRecords;
            obj.paramsString = paramsString;
            obj.get = get;
            obj.type = type;
            if (obj.endDate != undefined && obj.startDate && this.get != 1139 && obj.startDate.getValue()) {
                startDate = WtfGlobal.convertToGenericDate(obj.startDate.getValue());
            }else if(obj.endDate != undefined && obj.startDate && this.get == 1139 ){
                startDate = WtfGlobal.convertToGenericDate(obj.startDate);
            }
            if (obj.endDate != undefined && this.get != 1139 && obj.endDate.getValue()) {
                endDate = WtfGlobal.convertToGenericDate(obj.endDate.getValue());
            }else if(obj.endDate != undefined && this.get == 1139){
                endDate = WtfGlobal.convertToGenericDate(obj.endDate);
            }
            if(obj.accountViewTypeCombo){
                paramsString += "&showAccountsInGroup="+obj.accountViewTypeCombo.getValue();
            }
            if (this.isexportGLConfiguredData && (type == "detailedCSV" ||type == "detailedXls")) {
                var url = getExportUrl(this.get, this.consolidateFlag);
                var threadflag = Wtf.account.companyAccountPref.downloadSOAprocess == "1" ? true : false;
                var paramaters = this.mode + this.extra + "&get=" + get + "&accountIds=" + obj.MultiSelectAccCombo.getValue(true).join(",") + "&startdate=" + (startDate ? startDate : "")+ "&stdate=" + (startDate ? startDate : "") + "&enddate=" + (endDate != undefined ? endDate : "") + "&config=" + (this.configstr?this.configstr:"") + "&filename=" + (this.filename) + "&filetype=" + type + "&accountid=" + (this.params.accountid?this.params.accountid:"") + "&nondeleted=" + (this.nondeleted?this.nondeleted:"") + "&deleted=" + (this.deleted?this.deleted:"")
                        + "&type=" + (this.locationType != undefined ? this.locationType : ((type == "detailedXls") ? type + "&dtype=report" : ''))+"&moduleId=" + (this.moduleId?this.moduleId:"") + "&params=" + this.params + "&searchJson=" + (obj.searchJson !=undefined ?  obj.searchJson :"")+"&filterConjuctionCriteria="+obj.filterConjuctionCrit  //If LocationType is undefined, then sent empty string.  
                var expObj = {};
                expObj.url = url;
                expObj.scope = this;
                expObj.type = type;
                expObj.threadflag = threadflag;
                expObj.paramaters = paramaters+paramsString;
//                expObj.paramsString = paramsString;
//            if (this.isexportConfiguredData) {
                this.checkConfigurationforExport(obj, expObj);
            } else {
                this.ExportConfiguredcustomdata(obj);
            }
        }
    },
    checkConfigurationforExport: function (obj,expObj) {
        this.isconfigsaved = false;
        Wtf.Ajax.requestEx({
            url: "ACCReports/getExportConfiguredCustomData.do"
        }, this, function (response) {
            if (response.success) {
                this.isconfigsaved = response.isconfigsaved;
                if (this.isconfigsaved && this.params.isGroupDetailReport) {
                    WtfGlobal.exportReportInThread(expObj);
                }else{
                    this.exportCustomDataWindowHandler(expObj);
                }

            }
        }, function (response) {

        });
    },
    exportCustomDataWindowHandler: function (expObj) {
        this.ExportCustomDataWindow = new Wtf.account.ExportCustomdataInFinancialReport({
            title: WtfGlobal.getLocaleText("Configure custom data"),
            resizable: false,
            iconCls: getButtonIconCls(Wtf.etype.deskera),
            modal: true,
            autoScroll: true,
            width: 800,
            height: 550,
            expObj:expObj
//            layout:'border'
        });
        this.ExportCustomDataWindow.show();
    },
    ExportConfiguredcustomdata: function (obj) {
       
            if (Wtf.getCmp("selectexportwincsv") == undefined) {
                if (obj.get == 1113 && (obj.grid.getStore().lastOptions.params.enddate.contains("AM") || obj.grid.getStore().lastOptions.params.enddate.contains("PM"))) {
                    obj.grid.getStore().lastOptions.params.enddate = obj.grid.getStore().lastOptions.params.enddate.substr(0, obj.grid.getStore().lastOptions.params.enddate.length - 11);
                }
                var expt = new Wtf.ExportInterface({
                    type: obj.type,
                    get: obj.get,
                    ss: (obj.quickSearchTF != undefined) ? obj.quickSearchTF.getValue() : "",
                    nondeleted: (obj.nondeleted != undefined) ? obj.nondeleted : "",
                    deleted: (obj.deleted != undefined) ? obj.deleted : "",
                    stdate: this.params ? (this.params.stdate != undefined ? this.params.stdate : "") : "",
                    enddate: this.params ? (this.params.enddate != undefined ? this.params.enddate : "") : "",
                    accountid: this.params ? ((this.params.accountid && this.params.accountid != "") ? this.params.accountid : "") : "",
                    name: this.params ? (this.params.name != "" ? this.params.name : "") : "",
                    extra: this.extra || {},
                    id: "selectexportwincsv",
                    paramstring: (obj.get === 1113) ? ("&startdate=" + this.params.startdate.substr(0, this.params.startdate.length - 11) + "&enddate=" + this.params.enddate) : obj.paramsString,
                    isProductExport: this.isProductExport,
                    isProductExportRecordsGreaterThanThousands: this.isProductExportRecordsGreaterThanThousands,
                    totalRecords: obj.totalRecords,
                    mode: Wtf.urlEncode(obj.grid.getStore().baseParams),
                    compStoreParams: obj.grid.getStore().lastOptions != null ? Wtf.urlEncode(obj.grid.getStore().lastOptions.params) : Wtf.urlEncode(this.params),
                    filename: (obj.get == 220) ? encodeURIComponent('TaxesReport') : this.filename,
                    pdfDs: obj.pdfStore,
                    gridConfig: obj.jsonGrid,
                    grid: obj.EditorGrid,
                    isDefaultCustomerList: this.isDefaultCustomerList,
                    isInactiveCustomerListReport: this.isInactiveCustomerListReport,
                    excludeCustomHeaders: this.excludeCustomHeaders,
                    moduleId: this.moduleId,
                    usePostMethod: this.usePostMethod,
                    params: this.params,
                    ispendingApproval: this.ispendingApproval ? this.ispendingApproval : ""         //to print pending record
                });
                expt.show();
            }
    },
    setParams:function(params){
        if(!this.params)this.params={};
        for(var e in params){
            this.params[e]=params[e];
        }
    },
    fillExpanderBody:function(obj,get){
        var disHtml = "";
        var arr=[];
        var arrheader = [];
        var custArr = [];
        
        if (obj.expandStore != undefined) {
            var israteincludegst = obj.expandStore.getCount() > 0 && obj.expandStore.getAt(0).data.israteIncludingGst ? obj.expandStore.getAt(0).data.israteIncludingGst : false;
        }
        var productTypeText = obj.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pTypeNonInv") : WtfGlobal.getLocaleText("acc.invoiceList.expand.pType");
       
       // Sr NO
       if(get != Wtf.autoNum.VendorAgedPayable &&  get != Wtf.autoNum.CustomerAgedReceivable && get != Wtf.autoNum.accountrevaluationReprot && get != Wtf.autoNum.GroupDetailReport){ 
        arrheader.push('srno');
        arr.push(WtfGlobal.getLocaleText("erp.field.srno"));
        }
       if(get == Wtf.autoNum.SalesReturn || get  == Wtf.autoNum.PurchaseReturn){//Line Details for PR and SR modules
            //Column : Product Id for Inventory
            if(!this.withInvMode){
                arrheader.push('pid');
                arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.PID"));
            }
            //Column : Product Name
            arrheader.push(obj.withInvMode?'productdetail':'productname');
            arr.push(obj.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetailsNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"));
            //Column : Product Description
            if(get == Wtf.autoNum.SalesReturn){
                arrheader.push('description');  //Description
                arr.push(WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"));
            }else if(Wtf.autoNum.PurchaseReturn){
                arrheader.push('desc');
                arr.push(WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"));
             }
            /// Product Type / Permit Number
            if(!obj.isCustomer && !obj.isQuotation && !obj.isOrder && Wtf.account.companyAccountPref.countryid == '203'){
                arrheader.push('permit');
                arr.push(WtfGlobal.getLocaleText("acc.field.PermitNo."));
            }else if(!obj.withInvMode){
                arrheader.push('type');
                arr.push(productTypeText);
            }
            //Quantity
            arrheader.push('quantity');
            arr.push( WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"));//Quantity)
            //Delivered Quantity
            arrheader.push('dquantity');
            arr.push( WtfGlobal.getLocaleText("acc.accPref.returnQuant"));
            if(get == Wtf.autoNum.SalesReturn && Wtf.account.companyAccountPref.calculateproductweightmeasurment){
                arrheader.push('productweightperstockuom');
                arr.push(WtfGlobal.getLocaleText("acc.productList.unitWeightwithkg"));
                arrheader.push('productweightincludingpakagingperstockuom');
                arr.push(WtfGlobal.getLocaleText("acc.productList.unitWeightWithPackagingwithkg"));
                
                arrheader.push('productvolumeperstockuom');
                arr.push(WtfGlobal.getLocaleText("acc.productList.unitVolumeWithCubic"));
                arrheader.push('productvolumeincludingpakagingperstockuom');
                arr.push(WtfGlobal.getLocaleText("acc.productList.unitVolumeWithPackagingwithkg"));
            }
            //Reason
            arrheader.push('remark');
            arr.push( WtfGlobal.getLocaleText("acc.invoice.gridRemark"));

            arr.push( WtfGlobal.getLocaleText("acc.product.displayUoMLabel"));
            arrheader.push('displayUOM');
            //Purchase return -VI/GR  and sales return - Si/DO
            arrheader.push('linkto');
            arr.push((get == Wtf.autoNum.SalesReturn)?(this.isLeaseFixedAsset? WtfGlobal.getLocaleText("acc.field.LeaseDONO"):
                     ((this.isConsignment?WtfGlobal.getLocaleText("acc.field.CDONo"):WtfGlobal.getLocaleText("acc.field.CI/DONo")))):
                     (this.isConsignment?WtfGlobal.getLocaleText("acc.field.CGRNo"):WtfGlobal.getLocaleText("acc.field.VI/GRNo")));
       }else if(get == Wtf.autoNum.DeliveryOrder || get == Wtf.autoNum.GoodsReceiptOrder){
            //Product ID
            if(!obj.withInvMode){
                arrheader.push('pid');
                arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.PID"));
            }
            //Product Name
            arrheader.push(obj.withInvMode?'productdetail':'productname');
            arr.push(obj.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetailsNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"));
            //Column : Product Description
            if(get == Wtf.autoNum.DeliveryOrder){
                arrheader.push('description');  //Description
                arr.push(WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"));
            }else if(Wtf.autoNum.GoodsReceiptOrder){
                arrheader.push('desc');
                arr.push(WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"));
            }
            /// Product Type / Permit Number
            if(!obj.isCustomer && !obj.isQuotation && !obj.isOrder && Wtf.account.companyAccountPref.countryid == '203'){
                arrheader.push('permit');
                arr.push(WtfGlobal.getLocaleText("acc.field.PermitNo."));
            }else if(!obj.withInvMode){
                arrheader.push('type');
                arr.push(productTypeText);
            }
             //Quantity             
            arrheader.push('quantity');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"));
            //Delivered Quantity  /   Received Quantiy
            if(obj.businessPerson =="Customer"){
                arr.push(WtfGlobal.getLocaleText("acc.accPref.deliQuant"));
                 arrheader.push('dquantity');
            } else{
                arr.push(WtfGlobal.getLocaleText("acc.accPref.recQuant"));
                 arrheader.push('dquantity');
            }
            if(get == Wtf.autoNum.DeliveryOrder && Wtf.account.companyAccountPref.calculateproductweightmeasurment){
                arrheader.push('productweightperstockuom');
                arr.push(WtfGlobal.getLocaleText("acc.productList.unitWeightwithkg"));
                arrheader.push('productweightincludingpakagingperstockuom');
                arr.push(WtfGlobal.getLocaleText("acc.productList.unitWeightWithPackagingwithkg"));
                arrheader.push('productvolumeperstockuom');
                arr.push(WtfGlobal.getLocaleText("acc.productList.unitVolumeWithCubic"));
                arrheader.push('productvolumeincludingpakagingperstockuom');
                arr.push(WtfGlobal.getLocaleText("acc.productList.unitVolumeWithPackagingwithkg"));
            }
            if(obj.isLeaseFixedAsset){
                arr.push(WtfGlobal.getLocaleText("acc.field.LoNo")); 
            }else if(obj.isConsignment){
                arr.push(WtfGlobal.getLocaleText("acc.field.CRNo"));
            }else if(obj.businessPerson =="Customer"){
                arr.push(WtfGlobal.getLocaleText("acc.field.CI/SONo"));
            }else {
                arr.push(WtfGlobal.getLocaleText("acc.field.VI/PONo"));
            }
            arrheader.push('linkto')
            //Remarks
            arr.push(WtfGlobal.getLocaleText("acc.invoice.gridRemark"));
            arrheader.push('remark')
            arr.push( WtfGlobal.getLocaleText("acc.product.displayUoMLabel"));
            arrheader.push('displayUOM');
        
        }else if (get == Wtf.autoNum.CreditNote || get == Wtf.autoNum.DebitNote){
            
            arrheader.push('transectionno');
            arr.push(WtfGlobal.getLocaleText("acc.prList.invNo"));
            
            arrheader.push('invcreationdateinuserdateformat');
            arr.push(WtfGlobal.getLocaleText("acc.prList.creDate"));
            
            arrheader.push('invduedateinuserdateformat');
            arr.push(WtfGlobal.getLocaleText("acc.prList.dueDate"));
            
            arrheader.push('currencycodeforinvoice');
            arr.push(WtfGlobal.getLocaleText("acc.common.currencyFilterLable"));
            
            arrheader.push('invamount');
            arr.push(WtfGlobal.getLocaleText("acc.prList.invAmt"));
            
            arrheader.push('invamountdue');
            arr.push(WtfGlobal.getLocaleText("acc.prList.amtDue"));
            
            arrheader.push('srnoforaccount');
            arr.push(WtfGlobal.getLocaleText("erp.field.srno"));
            
            arrheader.push('accountname');
            arr.push(WtfGlobal.getLocaleText("acc.je.acc"));
            
            arrheader.push('taxpercent');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.tax"));
            
            arrheader.push('taxamountforaccount');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"));
            
            arrheader.push('totalamountforaccount');
            arr.push(WtfGlobal.getLocaleText("acc.dnList.gridAmt"));
            
            arrheader.push('description');
            arr.push(WtfGlobal.getLocaleText("acc.cnList.Desc"));
            
        }else if (get == Wtf.autoNum.Receipt ){
            arrheader.push('transectionno');
            arr.push(WtfGlobal.getLocaleText("acc.prList.invNo"));
            
            arrheader.push('creationdate');
            arr.push(WtfGlobal.getLocaleText("acc.prList.creDate"));
            
            arrheader.push('duedate');
            arr.push(WtfGlobal.getLocaleText("acc.prList.dueDate"));
//            
            arrheader.push('totalamount');
            arr.push(WtfGlobal.getLocaleText("acc.prList.invAmt"));
//            
            arrheader.push('amountdue');
            arr.push(WtfGlobal.getLocaleText("acc.prList.amtDue"));
            
            arrheader.push('discountAmount');
            arr.push(WtfGlobal.getLocaleText("acc.prList.discount"));
            
            arrheader.push('amountpaid');
            arr.push(WtfGlobal.getLocaleText("acc.prList.amtRec"));
            
            
            arrheader.push('srnofordebitnote');
            arr.push(WtfGlobal.getLocaleText("erp.field.srno"));
            
            arrheader.push('debitnote');
            arr.push(WtfGlobal.getLocaleText("acc.accPref.autoDN"));
            
            arrheader.push('debitnoteaccountname');
            arr.push(WtfGlobal.getLocaleText("acc.exportdetails.custven"));
            
            arrheader.push('debitnotetotalamount');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.totAmt"));
            
            arrheader.push('debitnoteamountdue');
            arr.push(WtfGlobal.getLocaleText("acc.prList.amtDue"));
            
            arrheader.push('debitenteramount');
            arr.push(WtfGlobal.getLocaleText("acc.pmList.gridAmtPaid"));
            
            arrheader.push('srnoforaccount');
            arr.push(WtfGlobal.getLocaleText("erp.field.srno"));
            
            arrheader.push('accountnamepay');
            arr.push(WtfGlobal.getLocaleText("acc.payMethod.acc"));

            arrheader.push('debit');
            arr.push(WtfGlobal.getLocaleText("acc.je.type"));
            
             arrheader.push('acctaxpercent');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.tax"));
//            
            arrheader.push('taxamountforaccount');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"));
            
            arrheader.push('accamountpaid');
            arr.push(WtfGlobal.getLocaleText("acc.pmList.gridAmtPaid"));
            
            arrheader.push('srnoforadvance');
            arr.push(WtfGlobal.getLocaleText("erp.field.srno"));
            
            arrheader.push('advanceccountname');
            arr.push(WtfGlobal.getLocaleText("acc.het.13"));
            
            arrheader.push('advancetotalamount');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.totAmt"));
            
            arrheader.push('advanceamountdue');
            arr.push(WtfGlobal.getLocaleText("acc.prList.amtDue"));
            
            arrheader.push('paidamount');
            arr.push(WtfGlobal.getLocaleText("acc.pmList.gridAmtPaid"));
            
            arrheader.push('srnoforrefund');
            arr.push(WtfGlobal.getLocaleText("erp.field.srno"));
            
            arrheader.push('fundaccountname');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.ven"));
            
            arrheader.push('fundtransectionno');
            arr.push(WtfGlobal.getLocaleText("acc.het.57"));
            
            arrheader.push('fundtotalamount');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.totAmt"));
            
            arrheader.push('fundamountdue');
            arr.push(WtfGlobal.getLocaleText("acc.prList.amtDue"));
            
            arrheader.push('fundpaidamountOriginal');
            arr.push(WtfGlobal.getLocaleText("acc.pmList.gridAmtPaid"));
            
        }else if (get == Wtf.autoNum.Payment ){
            arrheader.push('transectionno');
            arr.push(WtfGlobal.getLocaleText("acc.prList.invNo"));
            
            arrheader.push('creationdate');
            arr.push(WtfGlobal.getLocaleText("acc.prList.creDate"));
            
            arrheader.push('duedate');
            arr.push(WtfGlobal.getLocaleText("acc.prList.dueDate"));
//            
            arrheader.push('totalamount');
            arr.push(WtfGlobal.getLocaleText("acc.prList.invAmt"));
//            
            arrheader.push('amountdue');
            arr.push(WtfGlobal.getLocaleText("acc.prList.amtDue"));
            
            arrheader.push('discountAmount');
            arr.push(WtfGlobal.getLocaleText("acc.prList.discount"));
            
            arrheader.push('amountpaid');
            arr.push(WtfGlobal.getLocaleText("acc.prList.amtRec"));
            
            
            arrheader.push('srnofordebitnote');
            arr.push(WtfGlobal.getLocaleText("erp.field.srno"));
            
            arrheader.push('debitnote');
            arr.push(WtfGlobal.getLocaleText("acc.accPref.autoCN"));
            
            arrheader.push('debitnoteaccountname');
            arr.push(WtfGlobal.getLocaleText("acc.exportdetails.custven"));
            
            arrheader.push('debitnotetotalamount');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.totAmt"));
            
            arrheader.push('debitnoteamountdue');
            arr.push(WtfGlobal.getLocaleText("acc.prList.amtDue"));
            
            arrheader.push('debitenteramount');
            arr.push(WtfGlobal.getLocaleText("acc.pmList.gridAmtPaid"));
            
            arrheader.push('srnoforaccount');
            arr.push(WtfGlobal.getLocaleText("erp.field.srno"));
            
            arrheader.push('accountnamepay');
            arr.push(WtfGlobal.getLocaleText("acc.payMethod.acc"));
            
            arrheader.push('debit');
            arr.push(WtfGlobal.getLocaleText("acc.je.type"));
            
             arrheader.push('acctaxpercent');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.tax"));
//            
            arrheader.push('taxamountforaccount');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"));
            
            arrheader.push('accamountpaid');
            arr.push(WtfGlobal.getLocaleText("acc.pmList.gridAmtPaid"));
            
            arrheader.push('srnoforadvance');
            arr.push(WtfGlobal.getLocaleText("erp.field.srno"));
            
            arrheader.push('advanceccountname');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.ven"));
            
            arrheader.push('advancetotalamount');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.totAmt"));
            
            arrheader.push('advanceamountdue');
            arr.push(WtfGlobal.getLocaleText("acc.prList.amtDue"));
            
            arrheader.push('paidamount');
            arr.push(WtfGlobal.getLocaleText("acc.pmList.gridAmtPaid"));
            
            arrheader.push('srnoforrefund');
            arr.push(WtfGlobal.getLocaleText("erp.field.srno"));
            
            arrheader.push('fundaccountname');
            arr.push(WtfGlobal.getLocaleText("acc.het.13"));
            
            arrheader.push('fundtransectionno');
            arr.push(WtfGlobal.getLocaleText("acc.het.57"));
            
            arrheader.push('fundtotalamount');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.totAmt"));
            
            arrheader.push('fundamountdue');
            arr.push(WtfGlobal.getLocaleText("acc.prList.amtDue"));
            
            arrheader.push('fundpaidamountOriginal');
            arr.push(WtfGlobal.getLocaleText("acc.pmList.gridAmtPaid"));
            
        }
        else if (get == Wtf.autoNum.customLineDetailsReport) {
            arrheader.push('productId');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.PID"));
            arrheader.push('productName');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"));
            arrheader.push('prdescription');
            arr.push(WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"));
            arrheader.push('quantity');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"));
            arrheader.push('rate');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice"));
            arrheader.push('prdiscountamt');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"));
            arrheader.push('prtaxamt');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"));
            arrheader.push('pramount');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.totAmt"));

            // Custom column
            custArr = WtfGlobal.appendCustomColumn(custArr, GlobalColumnModel[Wtf.Acc_Invoice_ModuleId]);
            for (var cust = 0; cust < custArr.length; cust++) {
                if (custArr[cust].header != undefined) {
                    if (custArr[cust].dataIndex != undefined && custArr[cust].dataIndex != "null")
                        arrheader.push("pr" + custArr[cust].dataIndex);//" header += "<span class='gridRow' wtf:qtip='"+rec.data[custArr[cust].dataIndex]+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec.data[custArr[cust].dataIndex],15)+"&nbsp;</span>";
                    arr.push(custArr[cust].header);
                }
            }
            custArr = [];
            custArr = WtfGlobal.appendCustomColumn(custArr, GlobalColumnModel[Wtf.Acc_Vendor_Invoice_ModuleId]);
            for (var cust = 0; cust < custArr.length; cust++) {
                if (custArr[cust].header != undefined && arr.indexOf(custArr[cust].header) == -1) {
                    if (custArr[cust].dataIndex != undefined && custArr[cust].dataIndex != "null")
                        arrheader.push("pr" + custArr[cust].dataIndex);//" header += "<span class='gridRow' wtf:qtip='"+rec.data[custArr[cust].dataIndex]+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec.data[custArr[cust].dataIndex],15)+"&nbsp;</span>";
                    arr.push(custArr[cust].header);
                }
            }
            // Account Details
            arrheader.push('srnoforexaccount');
            arr.push(WtfGlobal.getLocaleText("erp.field.srno"));
            arrheader.push('exaccountname');
            arr.push(WtfGlobal.getLocaleText("acc.jeList.expandJE.accName"));
            arrheader.push('exdescription');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.description"));
            arrheader.push('examount');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.amt"));
            arrheader.push('exdiscountamt');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"));
            arrheader.push('extaxamt');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"));
            arrheader.push('extotalamt');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.totAmt"));
            custArr = [];
            custArr = WtfGlobal.appendCustomColumn(custArr, GlobalColumnModel[Wtf.Acc_Vendor_Invoice_ModuleId]);
            for (var cust = 0; cust < custArr.length; cust++) {
                if (custArr[cust].header != undefined) {
                    if (custArr[cust].dataIndex != undefined && custArr[cust].dataIndex != "null")
                        arrheader.push("ex" + custArr[cust].dataIndex);//" header += "<span class='gridRow' wtf:qtip='"+rec.data[custArr[cust].dataIndex]+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec.data[custArr[cust].dataIndex],15)+"&nbsp;</span>";
                    arr.push(custArr[cust].header);
                }
            }
            //Journal Entry
            arrheader.push('srnoforcraccount');
            arr.push(WtfGlobal.getLocaleText("erp.field.srno"));
            arrheader.push('jeaccountname');
            arr.push(WtfGlobal.getLocaleText("acc.jeList.expandJE.accName"));
            arrheader.push('jedescription');
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.description"));
            arrheader.push('jeamount');
            arr.push(WtfGlobal.getLocaleText("acc.jeList.expandJE.amtCredit"));
            custArr = [];
            custArr = WtfGlobal.appendCustomColumn(custArr, GlobalColumnModel[Wtf.Acc_GENERAL_LEDGER_ModuleId]);
            for (var cust = 0; cust < custArr.length; cust++) {
                if (custArr[cust].header != undefined) {
                    if (custArr[cust].dataIndex != undefined && custArr[cust].dataIndex != "null")
                        arrheader.push("je" + custArr[cust].dataIndex);//" header += "<span class='gridRow' wtf:qtip='"+rec.data[custArr[cust].dataIndex]+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec.data[custArr[cust].dataIndex],15)+"&nbsp;</span>";
                    arr.push(custArr[cust].header);
                }
            }
        }else if(get == Wtf.autoNum.ContractReport){ //-- Contact Report Line Level Fields 
            arr.push( //-- Headers
                WtfGlobal.getLocaleText("acc.invoiceList.expand.PID"), // "Product ID"
                WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), // "Product Name"
                WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"), // "Quantity"
                WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice"), // "Unit Price"
                WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"), // "Discount"
                WtfGlobal.getLocaleText("acc.invoiceList.expand.amt")); // "Amount""
                     //-- Data Indexes    
            arrheader.push("productcode","productname","quantity","rate","prdiscount","amount");
        }else if(get == Wtf.autoNum.accountrevaluationReprot){ //-- Account Revaluation Report Line Level Fields 
            arr.push('Transaction Id','Date', 'Journal Entry','Type','Currency','Current Rate','Revaluation Rate','Amount','Profit/loss');
                     //-- Data Indexes    
            arrheader.push('billno','date', 'entryno','type','revalcurrencycode', 'currentrate','revalrate','documentamount','subprofitloss');
            
        } else if(get == Wtf.autoNum.VendorAgedPayable || get == Wtf.autoNum.CustomerAgedReceivable){ // Aged Payable Summary Report Currency Wise Details
            //-- Headers
            if (get == Wtf.autoNum.VendorAgedPayable){
                if(Wtf.agedPayableDateFilter == 2){
                    arr.push((obj.receivable ? WtfGlobal.getLocaleText("acc.agedPay.cus") : WtfGlobal.getLocaleText("acc.agedPay.ven")) + "/" + WtfGlobal.getLocaleText("acc.agedPay.accName"),
                            WtfGlobal.getLocaleText("acc.agedPay.gridCurrency"),
                            WtfGlobal.getLocaleText("acc.agedPay.gridCurrent"),
                            (!obj.typeEditor.getValue() ? "" : "0-") + obj.interval.getValue() + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + (!obj.typeEditor.getValue() ? " " + WtfGlobal.getLocaleText("acc.agedPay.before") + " " : ""));
                            
                } else {
                    arr.push((obj.receivable ? WtfGlobal.getLocaleText("acc.agedPay.cus") : WtfGlobal.getLocaleText("acc.agedPay.ven")) + "/" + WtfGlobal.getLocaleText("acc.agedPay.accName"),
                            WtfGlobal.getLocaleText("acc.agedPay.gridCurrency"),
                            WtfGlobal.getLocaleText("acc.agedPay.gridCurrent"),
                            (!obj.typeEditor.getValue() ? "" : "1-") + obj.interval.getValue() + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + (!obj.typeEditor.getValue() ? " " + WtfGlobal.getLocaleText("acc.agedPay.before") + " " : ""));
                }
                
            } else if (get == Wtf.autoNum.CustomerAgedReceivable){
                if(Wtf.agedReceivableDateFilter == 2){
                    arr.push((obj.receivable ? WtfGlobal.getLocaleText("acc.agedPay.cus") : WtfGlobal.getLocaleText("acc.agedPay.ven")) + "/" + WtfGlobal.getLocaleText("acc.agedPay.accName"),
                            WtfGlobal.getLocaleText("acc.agedPay.gridCurrency"),
//                            WtfGlobal.getLocaleText("acc.agedPay.gridCurrent"),
                            (!obj.typeEditor.getValue() ? "" : "0-") + obj.interval.getValue() + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + (!obj.typeEditor.getValue() ? " " + WtfGlobal.getLocaleText("acc.agedPay.before") + " " : ""));
                } else {
                    arr.push((obj.receivable ? WtfGlobal.getLocaleText("acc.agedPay.cus") : WtfGlobal.getLocaleText("acc.agedPay.ven")) + "/" + WtfGlobal.getLocaleText("acc.agedPay.accName"),
                            WtfGlobal.getLocaleText("acc.agedPay.gridCurrency"),
                            WtfGlobal.getLocaleText("acc.agedPay.gridCurrent"),
                            (!obj.typeEditor.getValue() ? "" : "1-") + obj.interval.getValue() + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + (!obj.typeEditor.getValue() ? " " + WtfGlobal.getLocaleText("acc.agedPay.before") + " " : ""));
                            
                }
                
            }
                                
            if (obj.typeEditor.getValue()) {     // exclude if cummulative filter applied          
                for (var noOfInterval = 1; noOfInterval < obj.noOfIntervalCombo.getValue(); noOfInterval++) {  // loop will run for (this.noOfIntervalCombo -1) Times as 1-30 is already added
                    if (noOfInterval == (obj.noOfIntervalCombo.getValue() - 1)) { // To append ">" For Last Column
                        arr.push(">" + (obj.interval.getValue() * (noOfInterval)) + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + " ");
                    } else { // For Column like 31-60 , 61-90 ...
                        arr.push(((obj.interval.getValue() * noOfInterval) + 1) + "-" + (obj.interval.getValue() * (noOfInterval + 1)) + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + " ");
                    }
                }
            } else {
                arr.push(((!obj.typeEditor.getValue() ? "" : (obj.interval.getValue() * 1 + 1) + "-") + (obj.interval.getValue() * 2)) + " " + WtfGlobal.getLocaleText("acc.agedPay.days") + (!obj.typeEditor.getValue() ? " " + WtfGlobal.getLocaleText("acc.agedPay.before") + " " : ""),
                    (obj.typeEditor.getValue() ? ((obj.interval.getValue() * 2 + 1) + "-" + (obj.interval.getValue() * 3)) : (">" + (obj.interval.getValue() * 2)) + " " + WtfGlobal.getLocaleText("acc.agedPay.days")));
            }
             /**
               *Hidded Total column from AP and AR SDP-13193.
               */
            arr.push(WtfGlobal.getLocaleText("acc.common.total") +" "+ WtfGlobal.getLocaleText("acc.fixedAssetList.grid.homCur"));
            //-- Data Indexes  
            arrheader.push("personname_line","currencyname_line");
            if(obj.typeEditor.getValue()){    // exclude if cummulative filter applied     
                
                for (var noOfInterval = 0 ; noOfInterval <= obj.noOfIntervalCombo.getValue() ; noOfInterval++ )
                {
                    arrheader.push("amountdue"+ (noOfInterval + 1) +"_line");                    
                }
                
//                arrheader.push("amountdue5_line", "amountdue6_line", "amountdue7_line", "amountdue8_line");
            } else {
                arrheader.push("amountdue1_line","amountdue2_line","amountdue3_line","amountdue4_line");
            }
           // arrheader.push("total_line","totalinbase_line");
            arrheader.push("totalinbase_line");
            
        }else if(get==Wtf.autoNum.GroupDetailReport){
            
            arrheader.push('d_date');
            arr.push(WtfGlobal.getLocaleText("acc.inventoryList.date"));
            arrheader.push('d_accountname');
            arr.push(WtfGlobal.getLocaleText("acc.coa.gridDoubleEntryMovement"));
            arrheader.push('d_entryno');
            arr.push(WtfGlobal.getLocaleText("acc.field.JournalFolio(J/F)"));
            arrheader.push('c_transactionDetails');
            arr.push(WtfGlobal.getLocaleText("acc.product.description"));
            arrheader.push('transactionSymbol');
            arr.push(WtfGlobal.getLocaleText("Exchange Rate ("+WtfGlobal.getCurrencySymbol()+")"));
            arrheader.push('d_transactionAmount');
            arr.push(WtfGlobal.getLocaleText("acc.je.debitAmt"));
            arrheader.push('d_amount');
            arr.push(WtfGlobal.getLocaleText("acc.field.DebitAmountinBaseCurrency"));
            arrheader.push('c_transactionAmount');
            arr.push(WtfGlobal.getLocaleText("acc.je.creditAmt"));
            arrheader.push('c_amount');
            arr.push(WtfGlobal.getLocaleText("acc.field.CreditAmountinBaseCurrency"));
            
        } else if (get == Wtf.autoNum.TDSChallanControlReport) {
            //To Export TDS Challan Control Report in Detail.
            arrheader.push('vendorName');
            arr.push(WtfGlobal.getLocaleText("acc.ven.name"));
            arrheader.push('vendorPanNo');
            arr.push(WtfGlobal.getLocaleText("acc.field.VendorPANNO"));
            arrheader.push('transactionDate');
            arr.push(WtfGlobal.getLocaleText("acc.field.TransactionDate"));
            arrheader.push('transactionDocumentNo');
            arr.push(WtfGlobal.getLocaleText("acc.field.TransactionID"));
            arrheader.push('amountPaid');
            arr.push(WtfGlobal.getLocaleText("acc.TermSelGrid.TDSAssessableAmount"));
            arrheader.push('tdsRate');
            arr.push(WtfGlobal.getLocaleText("acc.CommonReport.tdsRate"));
            arrheader.push('tdsAmount');
            arr.push(WtfGlobal.getLocaleText("acc.TDSPaymentWindow.TDSAmount"));
            arrheader.push('tdsInterestAmount');
            arr.push(WtfGlobal.getLocaleText("acc.CommonReport.tdsInterestPayment"));
        } else if (get === Wtf.autoNum.PackingReport) {
            //To Export Packing Report in detail.
            //Product List
            arrheader.push("productlistpid");
            arr.push(WtfGlobal.getLocaleText("acc.contractMasterGrid.header8"));
            arrheader.push("productlistproductname");
            arr.push(WtfGlobal.getLocaleText("acc.contractMasterGrid.header7"));
            arrheader.push("productlistdescription");
            arr.push(WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.Description"));
            arrheader.push("productlistunitname");
            arr.push(WtfGlobal.getLocaleText("acc.invoice.gridUOM"));
            arrheader.push("productlisttype");
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.pType"));
            arrheader.push("quantity");
            arr.push(WtfGlobal.getLocaleText("erp.QuantityinDOs"));
            arrheader.push("deliveredquantity");
            arr.push(WtfGlobal.getLocaleText("erp.packingdolist.packingquantity"));
            //Packing DO's Details
            arrheader.push("shipingdosrno");
            arr.push(WtfGlobal.getLocaleText("erp.field.srno"));
            arrheader.push("dono");
            arr.push(WtfGlobal.getLocaleText("erp.DONumber"));
            arrheader.push("shipingdoproductname");
            arr.push(WtfGlobal.getLocaleText("acc.contractMasterGrid.header7"));
            arrheader.push("shipingdodescription");
            arr.push(WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.Description"));
            arrheader.push("shipingdounitname");
            arr.push(WtfGlobal.getLocaleText("acc.invoice.gridUOM"));            
            arrheader.push("quantityindo");
            arr.push(WtfGlobal.getLocaleText("erp.ActualQuantityinDO"));
            arrheader.push("duequantity");
            arr.push(WtfGlobal.getLocaleText("erp.DueQuantityforPacking"));
            arrheader.push("shipquantity");
            arr.push(WtfGlobal.getLocaleText("erp.packingdolist.packingquantity"));
            //Item Packing Details
            arrheader.push("itempackingsrno");
            arr.push(WtfGlobal.getLocaleText("erp.field.srno"));
            arrheader.push("packagename");
            arr.push(WtfGlobal.getLocaleText("erp.Package"));
            arrheader.push("itempackingproductname");
            arr.push(WtfGlobal.getLocaleText("acc.contractMasterGrid.header7"));            
            arrheader.push("productweight");
            arr.push(WtfGlobal.getLocaleText("erp.PackageWeightinKg"));
            arrheader.push("packagequantity");
            arr.push(WtfGlobal.getLocaleText("erp.PackageQuantity"));
            arrheader.push("itemperpackage");
            arr.push(WtfGlobal.getLocaleText("erp.PackagePerQuantity"));
            arrheader.push("totalquantity");
            arr.push(WtfGlobal.getLocaleText("erp.TotalPackageQuantity"));
            arrheader.push("grossweight");
            arr.push(WtfGlobal.getLocaleText("erp.GrossWeight"));
        }else if(get ==Wtf.autoNum.StockAdjustmentRegister){
            arrheader.push("locationName");
            arr.push("Location");
            arrheader.push("batchName");
            arr.push("Batch/Challan No");
            arrheader.push("serialNames");
            arr.push("Serials");
            arrheader.push("quantityL");
            arr.push("Quantity");
        }else{
            if(!obj.withInvMode){
                arrheader.push('pid');
                arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.PID"));
            }
            //Column : Product Name
            arrheader.push(obj.withInvMode?'productdetail':'productname');
            arr.push(obj.withInvMode?WtfGlobal.getLocaleText("acc.invoiceList.expand.pDetailsNonInv"):WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"));
            if(get == Wtf.autoNum.Invoice || get == Wtf.autoNum.SalesOrder || get == Wtf.autoNum.PurchaseOrder){
                arrheader.push('description');  //Description
                arr.push(WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"));
            }else if(obj.isRequisition || obj.isRFQ || Wtf.autoNum.Quotation){
                arrheader.push('desc');
                arr.push(WtfGlobal.getLocaleText("acc.saleByItem.gridProdDesc"));
            }              
            if(!obj.isCustomer && !obj.isQuotation && !obj.isOrder && Wtf.account.companyAccountPref.countryid == '203'){
                arrheader.push('permit');
                arr.push(WtfGlobal.getLocaleText("acc.field.PermitNo."));
            }else if(!obj.withInvMode){
                arrheader.push('type');
                arr.push(productTypeText);
            }
            arrheader.push('quantity');  //Quantity
            arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.qty"));

            if(!obj.withInvMode&&(obj.isOrder&&obj.isCustomer||obj.isOrder&&!obj.isCustomer)){  //balance Quantity
                arrheader.push('balanceQuantity');
                arr.push(WtfGlobal.getLocaleText("acc.field.BalanceQty"));
            }
            /*
             * Added shortfallQuantity in export Excel
             */
            if(obj.isOrder && obj.isCustomer){  
                arrheader.push('shortfallQuantity');
                arr.push(WtfGlobal.getLocaleText("acc.field.ShortfallQty"));
            }
            
            if((obj.moduleid == Wtf.Acc_Invoice_ModuleId || obj.moduleid == Wtf.Acc_Cash_Sales_ModuleId) && Wtf.account.companyAccountPref.calculateproductweightmeasurment){
                arrheader.push('productweightperstockuom');
                arr.push(WtfGlobal.getLocaleText("acc.productList.unitWeightwithkg"));
                arrheader.push('productweightincludingpakagingperstockuom');
                arr.push(WtfGlobal.getLocaleText("acc.productList.unitWeightWithPackagingwithkg"));
                
                arrheader.push('productvolumeperstockuom');
                arr.push(WtfGlobal.getLocaleText("acc.productList.unitVolumeWithCubic"));
                arrheader.push('productvolumeincludingpakagingperstockuom');
                arr.push(WtfGlobal.getLocaleText("acc.productList.unitVolumeWithPackagingwithkg"));
            }
            
            //Unit Price
            if(!obj.isRFQ) {
                arrheader.push((obj.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId || obj.moduleid == Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId) ? 'rate': (obj.isQuotation || obj.isOrder && !obj.withInvMode? (israteincludegst ? 'rateIncludingGst': 'orderrate') : (israteincludegst ? 'rateIncludingGst' : 'rate')));//  header += "<span class='gridRow' style='width: "+widthInPercent+"% ! important;'>"+WtfGlobal.withCurrencyUnitPriceRenderer(rate,true,rec)+"</span>";
                arr.push((israteincludegst ? WtfGlobal.getLocaleText("acc.invoice.gridUnitPriceIncludingGST") : WtfGlobal.getLocaleText("acc.invoiceList.expand.unitPrice")));    
            }
            //Partial Amount
            if((obj.isCustomer && !obj.isQuotation&& !obj.isOrder&&!obj.withInvMode)) {
                arrheader.push('partamount');
                arr.push(WtfGlobal.getLocaleText("acc.field.PartialAmount(%)"));    
            }
            //Discount
            if (!obj.isRequisition && !obj.isRFQ) {
                arrheader.push('prdiscount');
                arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.dsc"));
            }
            //Tax
            if(!obj.isRequisition && !obj.isRFQ){
                arrheader.push('rowTaxAmount');
                arr.push(WtfGlobal.getLocaleText("acc.invoiceList.expand.tax.amount"));
            }
            if(!obj.isRFQ){
                arrheader.push('amountForExcelFile');
                arr.push('Amount');
            }
            if(!obj.withInvMode&&(obj.isOrder&&obj.isCustomer)){  //balance Quantity
                arrheader.push('balanceamount');
                arr.push(WtfGlobal.getLocaleText("acc.field.BalanceAmt"));
            }
            ////--------------------------------------Line Level Linking  Fileds for Line Data------------------------------   
            if(obj.isCustomer && obj.isQuotation && !obj.isOrder){
                arrheader.push('linkto'); 
                arr.push(WtfGlobal.getLocaleText("acc.field.VQ.No"));
            }
            if(!obj.isCustomer && obj.isQuotation && !obj.isOrder){
                arrheader.push('linkto'); 
                arr.push(WtfGlobal.getLocaleText("acc.field.PurchaseRequisition.No"));
            }
            if(obj.isCustomer && !obj.isQuotation && !obj.isOrder){
                if(obj.isLeaseFixedAsset)
                    arr.push(WtfGlobal.getLocaleText("acc.field.LeaseDONO"));
                else if(!obj.isConsignment)
                    arr.push(WtfGlobal.getLocaleText("acc.field.SO/DO/CQNo"));
                arrheader.push('linkto'); 
                  
            }else{
                if(!obj.isCustomer && !obj.isQuotation&& !obj.isOrder && !obj.isRFQ && !obj.isConsignment){
                    arr.push(WtfGlobal.getLocaleText("acc.field.PO/GR/VQNo"));
                    arrheader.push('linkto'); 
                }
                if(obj.isCustomer && !obj.isQuotation && obj.isOrder && !obj.isRequisition && !obj.isConsignment){
                    if(obj.isLeaseFixedAsset)
                        arr.push(WtfGlobal.getLocaleText("acc.field.LQ/RN.NO"));
                    else 
                        arr.push(WtfGlobal.getLocaleText("acc.field.CQ/PO.No"));
                    arrheader.push('linkto');     
                }else  if(!obj.isCustomer && !obj.isQuotation && obj.isOrder && !obj.isRequisition && !obj.isConsignment){
                    arr.push(WtfGlobal.getLocaleText("acc.field.SO/VQ.No"));
                    arrheader.push('linkto'); 
                }
                if(obj.isCustomer && !obj.isQuotation && obj.isOrder && obj.isConsignment){
                    arr.push(WtfGlobal.getLocaleText("acc.field.CNApprovedSerials"));
                    arrheader.push('linkto'); 
                }
                if(obj.isCustomer && !obj.isQuotation && obj.isOrder && obj.isConsignment){
                    arr.push(WtfGlobal.getLocaleText("acc.invoiceList.status"));
                    arrheader.push('linkto'); 
                }
            }
            arr.push(WtfGlobal.getLocaleText("acc.product.displayUoMLabel"));
            arrheader.push('displayUOM');
            /////// ----------------------------------        
            if(obj.isRequisition){
                arrheader.push('approverremark');
                arr.push(WtfGlobal.getLocaleText("acc.field.ApproverRemark"));
            }
            if(obj.isCustomer && !obj.isQuotation && obj.isOrder && obj.isConsignment){
                //Approved Serials
                arrheader.push('approvedserials'); 
                arr.push(WtfGlobal.getLocaleText("acc.field.CNApprovedSerials"));
                //Approval Status
                arrheader.push('approvalstatus'); 
                arr.push(WtfGlobal.getLocaleText("acc.invoiceList.status"));    
            }
     
            // For adding Cost and Margin columns for export details xls
            if (obj.moduleid == Wtf.Acc_Customer_Quotation_ModuleId) {
                // Unit Cost
                if(Wtf.account.companyAccountPref.activateProfitMargin){
                    arrheader.push('vendorunitcost'); 
                    arr.push(WtfGlobal.getLocaleText("acc.field.UnitCost"));
                }
                // Cost
                arrheader.push('cost'); 
                arr.push(WtfGlobal.getLocaleText("acc.field.totalCost"));
                // Margin
                arrheader.push('margin'); 
                arr.push(WtfGlobal.getLocaleText("acc.field.margin"));
            }
        }
        ////---------------------------------- Get Expander Line Level Custome Fields--------------------------------------
        if (get != Wtf.autoNum.customLineDetailsReport && get != Wtf.autoNum.VendorAgedPayable) {
                 custArr = WtfGlobal.appendCustomColumn(custArr,GlobalColumnModel[obj.moduleid]);
        custArr = WtfGlobal.appendCustomColumn(custArr,GlobalColumnModelForProduct[obj.moduleid]);
        for(var cust=0;cust<custArr.length;cust++){
            var headerFlag=false;
            if(custArr[cust].header != undefined ) {
                    if(get != Wtf.autoNum.DeliveryOrder && get != Wtf.autoNum.GoodsReceiptOrder && get != Wtf.autoNum.SalesReturn && get != Wtf.autoNum.PurchaseReturn && get != Wtf.autoNum.CreditNote && get != Wtf.autoNum.DebitNote && get != Wtf.autoNum.Receipt && get != Wtf.autoNum.ContractReport && get != Wtf.autoNum.Payment){
                    if(obj.customizeData!=undefined){
                            for(var j=0;j<obj.customizeData.length;j++){
                                if(custArr[cust].header==obj.customizeData[j].fieldDataIndex && obj.customizeData[j].hidecol){
                                    headerFlag=true;
                                }
                            }
                        }
                }
                if(!headerFlag){
                    if(custArr[cust].dataIndex!=undefined && custArr[cust].dataIndex!="null")
                        arrheader.push(custArr[cust].dataIndex);//" header += "<span class='gridRow' wtf:qtip='"+rec.data[custArr[cust].dataIndex]+"' style='width: "+widthInPercent+"% ! important;'>"+Wtf.util.Format.ellipsis(rec.data[custArr[cust].dataIndex],15)+"&nbsp;</span>";
                    arr.push(custArr[cust].header);
                }
            } 
        }   
        }
     //// --------------------------------  Add to line level header and index to Pdf Store ------------------------------------   
        
        if (this.get == Wtf.autoNum.SalesOrder || this.get == Wtf.autoNum.Invoice || this.get == Wtf.autoNum.CreditNote || this.get == Wtf.autoNum.Receipt || this.get == Wtf.autoNum.PurchaseOrder || this.get == Wtf.autoNum.GoodsReceipt || this.get == Wtf.autoNum.DebitNote || this.get == Wtf.autoNum.Payment
                || this.get == Wtf.autoNum.Quotation || this.get == Wtf.autoNum.DeliveryOrder || this.get == Wtf.autoNum.GoodsReceiptOrder || this.get == Wtf.autoNum.Venquotation || this.get == Wtf.autoNum.Requisition || this.get == Wtf.autoNum.RFQ || this.get == Wtf.autoNum.SalesReturn
                || this.get == Wtf.autoNum.PurchaseReturn || this.get == Wtf.autoNum.ContractReport || this.get == Wtf.autoNum.GoodsPendingOrdersRegister) {
            
            var hideField = false;
            var hideShowFormFieldData = WtfGlobal.getSysGridPreferences(WtfGlobal.getModuleidForExportBtn(obj));
            if (hideShowFormFieldData) {
                hideShowFormFieldData = hideShowFormFieldData.gridPref;
            }
            for (var k = 0; k < arr.length; k++) {
                if (hideShowFormFieldData) {
                    for (var j = 0; j < hideShowFormFieldData.length; j++) {
                        if (arrheader[k] == hideShowFormFieldData[j].fieldId && hideShowFormFieldData[j].isFormField && hideShowFormFieldData[j].isHidden) {
                            hideField = true;
                        }
                    }
                }
                if (!hideField) {
                    obj.newPdfRec = new Wtf.data.Record({
                        header: arrheader[k],
                        title: arr[k],
                        width: 40,
                        align: 'left',
                        index: obj.pdfStore.getCount()
                    });
                    obj.pdfStore.insert(obj.pdfStore.getCount(), obj.newPdfRec);
                }

            }
        } else {
            for (var k = 0; k < arr.length; k++) {
                obj.newPdfRec = new Wtf.data.Record({
                    header: arrheader[k],
                    title: arr[k],
                    width: 40,
                    align: 'left',
                    index: obj.pdfStore.getCount()
                });
                obj.pdfStore.insert(obj.pdfStore.getCount(), obj.newPdfRec);
            }
        }
        return obj.pdfStore;
    },
    filPdfStore:function(obj,column,type)
    {
        var k=1;
        if(this.get==199){// added one column Product Category for PDf Print
            obj.newPdfRec = new Wtf.data.Record({
                header : WtfGlobal.getLocaleText("acc.cust.procategory"),
                title : WtfGlobal.getLocaleText("acc.masterConfig.19"),
                width : 75,
                align : 'left',
                index : k
            });
            obj.pdfStore.insert(obj.pdfStore.getCount(), obj.newPdfRec);
            k++;
        }
         if(this.get==1214 || this.get==1215){// added one column Product ID for Export
            obj.newPdfRec = new Wtf.data.Record({
                header : 'productid',
                title : WtfGlobal.getLocaleText("acc.product.gridProductID"),
                width : 75,
                align : 'left',
                index : k
            });
            obj.pdfStore.insert(obj.pdfStore.getCount(), obj.newPdfRec);
            k++;
        }
         if(this.get==Wtf.autoNum.landedcostreport){// added one column Product ID for Export
            obj.newPdfRec = new Wtf.data.Record({
                header : 'purchaseinvoice',
                title : "Purchase Invoice",
                width : 75,
                align : 'left',
                index : k
            });
            obj.pdfStore.insert(obj.pdfStore.getCount(), obj.newPdfRec);
            k++;
        }
         if(this.get==Wtf.autoNum.SalesCommissionSchemaReport){// added one column Product ID for Export
            obj.newPdfRec = new Wtf.data.Record({
                header : 'schemaValue',
                title : obj.SchemaDimension.getRawValue(),
                width : 75,
                align : 'left',
                index : k
            });
            obj.pdfStore.insert(obj.pdfStore.getCount(), obj.newPdfRec);
            k++;
        }
        var start=1;    //starting index for columns to be inserted into obj.pdfStore. Columns from pdfStore are shown in column-selection-window for export
        if (this.get == Wtf.autoNum.Dimension_Based_BalanceSheet || this.get == Wtf.autoNum.Dimension_Based_TradingAndProfitLoss || this.get == Wtf.autoNum.StockAgeing || obj.isProductQuantityDetails || this.get == Wtf.autoNum.GroupDetailReport || this.get == 1005 || this.get == 1006 || this.get == Wtf.autoNum.SalesCommissionSchemaReport || this.get == Wtf.autoNum.profitAndLossMonthlyCustomLayout || this.get == Wtf.autoNum.GSTR3BReport || this.get == Wtf.autoNum.GSTR3BDetailReport || this.get == Wtf.autoNum.GSTRComputationDetailReport || this.get == Wtf.autoNum.StockLedger) {
            start=0;
        }         
        for(var i=start ; i<column.getColumnCount() ; i++) { // skip row numberer
            var showinPrint=false;
            if(column.config[i].showInPrint==true){
                showinPrint=true;
            }
            if(column.config[i].dataIndex=="salesPersonAgent" || (column.config[i].dataIndex=="pid" && this.get==Wtf.autoNum.StockAgeing)){ //ERP-19693
                column.config[i].hidden=false; 
            }
            if((column.isHidden(i)==true && column.getColumnHeader(i) != "Currency"  && !showinPrint)||column.getColumnHeader(i)==""||column.getDataIndex(i)==""){
                continue;
            }
            else{          
                 if(column.config[i].dataIndex=="salesPersonAgent" && column.config[i].hidden==false){ //ERP-19693
                    column.config[i].hidden=true; 
                }
                if( column.config[i].pdfwidth!=undefined) {
                    var format="";
                    var title;
                    var header;
                    var defaultselectionunchk=false;
                    if(column.getRenderer(i)==WtfGlobal.currencyRenderer || column.getRenderer(i)==WtfGlobal.currencyDeletedRenderer ||column.getRenderer(i)==WtfGlobal.currencySummaryRenderer || column.getRenderer(i)==WtfGlobal.currencyRendererDeletedSymbol || column.getRenderer(i)==WtfGlobal.globalCurrencySymbolforDebit || column.getRenderer(i)==WtfGlobal.globalCurrencySymbolforCredit || column.getRenderer(i)==WtfGlobal.amountRendererForExport) {
                        format= (column.config[i].hidecurrency) ? "withoutcurrency" :  'currency';
                    } else if(column.getRenderer(i)==WtfGlobal.withoutRateCurrencySymbol || column.getRenderer(i)==WtfGlobal.withoutRateCurrencyDeletedSymbol || column.getRenderer(i)==WtfGlobal.withoutRateCurrencySymbolforDebit || column.getRenderer(i)==WtfGlobal.withoutRateCurrencySymbolforCredit) {
                        format= (column.config[i].hidecurrency) ? "withoutrowcurrency" : 'rowcurrency';
                    } else if(column.getRenderer(i)==WtfGlobal.onlyDateRenderer || column.getRenderer(i)==WtfGlobal.onlyDateDeletedRenderer || column.getRenderer(i)==WtfGlobal.onlyDateRendererForDeliveryPlanner) {
                        format='date';
                    } else {
                        if(column.config[i].pdfrenderer!=undefined) {
                            format= (column.config[i].hidecurrency) ? "" : column.config[i].pdfrenderer;
                        }
                    }
                    if(column.config[i].pdfheader!=undefined)
                        header=column.config[i].pdfheader;
                    else 
                        header= column.config[i].header;
                    
                    if(column.config[i].title==undefined)
                        title=column.config[i].dataIndex;
                    else
                        title=column.config[i].title;
                    if (column.config[i].defaultselectionunchk != undefined &&column.config[i].defaultselectionunchk) {  //" column unselected by default when export 
                        defaultselectionunchk = column.config[i].defaultselectionunchk;
                    }
                    obj.newPdfRec = new Wtf.data.Record({
                        header : title,
                        title : header,
                        width : column.config[i].pdfwidth,
                        align : format,
                        index : k,
                        defaultselectionunchk:defaultselectionunchk
                    });
                    obj.pdfStore.insert(obj.pdfStore.getCount(), obj.newPdfRec);
                    k++;
                }        
            }
        }
        if(this.get==Wtf.autoNum.BankReconcilationHistoryDetails){   //Bank Reconiliation Expander Columns Header
            var colArr = this.col;  //Add line level Headers here
            for(var c=0; c<colArr.length; c++){
                var lineObj = colArr[c];
                lineObj.index = k;   
                obj.newPdfRec = new Wtf.data.Record(lineObj);
                obj.pdfStore.insert(obj.pdfStore.getCount(), obj.newPdfRec);
                k++;
            }
        }
        return obj.pdfStore;
    },

    genJsonForPdf:function (obj)
    {
        //this.neeraj = "";
        var jsondata = [];
        if(this.get==115){// added one column in Bank Boook for PDf Print
            var temp1 = {
                header:WtfGlobal.getLocaleText("acc.coa.gridAccountName "),
                title:WtfGlobal.getLocaleText("acc.masterConfig.payMethod.gridBankAccount"),
                width:100,
                align:'center'
            };
            jsondata.push(temp1)
        } 
        for(i=0;i<obj.pdfStore.getCount();i++) {
            var recData = obj.pdfStore.getAt(i).data;
            //        jsondata="{'header':'" + recData.header + "',";
            if(recData.align=="right" && recData.title.indexOf("(")!=-1) {
                recData.title=recData.title.substring(0,recData.title.indexOf("(")-1);
            }
            //        jsondata+="'title':'" + recData.title + "',";
            //        jsondata+="'width':'" + recData.width + "',";
            //        jsondata+="'align':'" + recData.align + "'},";
            
            if(this.get==116){// changed title for trial balance
                var headersplit=recData.header.split("_");
                var recTitle=recData.title;
            
//                if(headersplit[headersplit.length-1]=='open'){
//                    if(recData.title.indexOf('YTD')==0){
//                        recTitle="YTD Opening Amount "+recData.title.substring(4,recData.title.length)
//                    }else{
//                        recTitle="Opening Amount "+recData.title
//                    }
//                }else if(headersplit[headersplit.length-1]=='period'){
//                    if(recData.title.indexOf('YTD')==0){
//                        recTitle="YTD Period Amount "+recData.title.substring(4,recData.title.length)
//                    }else{
//                        recTitle="Period Amount "+recData.title
//                    }
//                }else if(headersplit[headersplit.length-1]=='amount'){
//                    if(recData.title.indexOf('YTD') == 0){
//                        recTitle="YTD Ending Amount " + recData.title.substring(4,recData.title.length)
//                    }else{
//                        recTitle="Ending Amount " + recData.title
//                    }
//                }
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
                    align:recData.align
                };
                jsondata.push(temp);
            }
           
        }
        return Wtf.encode({
            data:jsondata
        });
    },
    
    exportSingleRow:function(obj,get, rowPrint,exportRecord){
        var mode=0;
        var moduleid="";
        var isEdit=false;
        var selRec=null;
        var fileName;
        var selRecData=[];
        var billIds=[];
        var moduletype=[];
        var isExportPayment=false;
        var transactionmodules=[];
        var transactionnumbers=[];
        var pendingapproval=(obj.pendingapproval==undefined?false:obj.pendingapproval);//ERP-12065
        if(this.templateId==Wtf.No_Template_Id){
            return this.templateId
        } else{
            if(exportRecord==undefined){
                
                /* This check is for exporting or printing and viewing template at record level*/
                
                if (obj.sm) {
                    var selData = obj.sm.selections.items;
                    //If module is MRP Work Order then get selected records from grid object
                    if(get == Wtf.autoNum.exportMRPWorkOrder){
                        selData = obj.grid.selModel.selections.items;
                    }
                    if (selData) {
                        for (var i = 0; i < selData.length; i++) {
                            selRecData[i] = selData[i].data;
                            if (get == Wtf.autoNum.CreditNote || get == Wtf.autoNum.BillingCreditNote || get == Wtf.autoNum.DebitNote || get == Wtf.autoNum.BillingDebitNote) {
                                billIds[i] = selRecData[i].noteid;
                            } else {
                                billIds[i] = selRecData[i].billid;
                                if(get == Wtf.autoNum.StockRepairReport && selRecData[i].transactionmodule){
                                    transactionmodules[i] = selRecData[i].transactionmodule;
                                    transactionnumbers[i] = selRecData[i].transactionno;
                                }
                                if(get == Wtf.autoNum.QAApprovalReport && selRecData[i].transactionmodule){
                                    moduletype[0] = selRecData[i].moduletype;
                                    transactionmodules[0] = selRecData[i].transactionmodule;
                                    transactionnumbers[0] = selRecData[i].transactionno;
                                }
                            }
                        }
                    }

                    if (this.templateId == "ProductCompositionExport" && (Wtf.templateflag == 1||Wtf.templateflag == 5 || Wtf.templateflag == 13 ||Wtf.templateflag == Wtf.F1Recreation_templateflag || Wtf.templateflag == Wtf.F1RecreationLeasing_templateflag ||Wtf.templateflag == 23)) {
                        if(selData.length>1){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.productCompositionwarning") ], 2);
                            return;
                        }else{
                            Wtf.Ajax.timeout = 90000000; 
                            Wtf.Ajax.requestEx({
                                url:'ACCGoodsReceiptCMN/getProductCompositionStatus.do',
                                params:{
                                    bills:billIds
                                }
                            },this,function(response){
                                if(response.success){
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"The Product Composition is not defined for the Products "+response.msg ], 2);
                                    return;
                                }
                            },function(response){
                           
                                });
                        }
                    }else  if(this.templateId=="exportInvoicepackingList" && (Wtf.templateflag == 1||Wtf.templateflag == 5 || Wtf.templateflag == 13 ||Wtf.templateflag == Wtf.F1Recreation_templateflag || Wtf.templateflag == Wtf.F1RecreationLeasing_templateflag ||Wtf.templateflag == 23)){
                        if(selData.length>1){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.productCompositionwarning") ], 2);
                            return;
                        }
                    }else if(this.templateId== "exportPermitInvoiceListPdf" && (Wtf.templateflag == 1||Wtf.templateflag == 5 || Wtf.templateflag == 13 ||Wtf.templateflag == Wtf.F1Recreation_templateflag || Wtf.templateflag == Wtf.F1RecreationLeasing_templateflag ||Wtf.templateflag == 23)){
                        var personname=selData[0].data.personname;
                        for(var i=0;i<selData.length;i++){
                            if(selData[i].data.personname!=personname ||selData[i].data.deleted==true ){
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("erp.dopacking.alerttoselectsamecustomer")],2);
                                return;
                            }
                        }
                    }
                
                    var recData=obj.grid.getSelectionModel().getSelected().data;
                    var recStore=obj.grid.getStore();
                    var contraentryflag = false;

                    if (get == Wtf.autoNum.CreditNote || get == Wtf.autoNum.BillingCreditNote || get == Wtf.autoNum.DebitNote || get == Wtf.autoNum.BillingDebitNote) { // ERP-26622
                        
                        if (!recData.isReturnNote && this.templatesubtype == "1") { // If selected record is of default type and template is of return type then show alert
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.236.CN/DNwithoutReturn.alert")],2);
                            return;
                        } 
                    }
                    if(obj.contraentryflag){
                        contraentryflag = true;
                    }
                    if(recData.isadvancepayment)
                    {
                        var recordIndex=recStore.find('advanceid',recData.billid);                        
                        if(recordIndex!=-1)
                        {
                            var actualRecord=recStore.getAt(recordIndex);
                            recData=actualRecord.data;
                        } 
                    }
            
                    var advanceFlag=false;
                    if(recData.advanceid!="")
                        advanceFlag=true;
                } else {
                    /*This check is for printing template from view mode of Invoices.*/
                    if ( !(obj.moduleid === 60) && !(obj.moduleid === 61)) {
                        if(get == Wtf.Bank_Reconciliation_ModuleId){
                            //If bank reconciliation
                            billIds = (obj.accountID != undefined || obj.accountID != null) ? obj.accountID : obj.reconRec.accountid;
                        } else{
                            recData = obj.record.data;
                            billIds = recData.billid;
                        }
                    }

                }
            }
            else{
                recData=exportRecord;
                if(recData.isExportPayment!=undefined && recData.isExportPayment==true){
                    isExportPayment=true;
                }
                if(obj.grid){
                    var recStore=obj.grid.getStore();
                }
                /* This code is for printing at transaction level*/
                if (recData) {
                    if (get == Wtf.autoNum.CreditNote || get == Wtf.autoNum.BillingCreditNote || get == Wtf.autoNum.DebitNote || get == Wtf.autoNum.BillingDebitNote) {
                        billIds = recData.noteid;
                    } else {
                        billIds = recData.billid;
                    }
                }
                
                var contraentryflag = false;
                if(obj.contraentryflag){
                    contraentryflag = true;
                }
                if(recData.isadvancepayment)
                {
                    var recordIndex=recStore.find('advanceid',recData.billid);                       
                    if(recordIndex!=-1)
                    {
                        var actualRecord=recStore.getAt(recordIndex);
                        recData=actualRecord.data;
                    }
                }
                var advanceFlag=false;
                if(recData.advanceid!=undefined&&recData.advanceid!="")
                    recData.isadvancepayment=true;
            }
            if(get==Wtf.autoNum.Invoice||get==Wtf.autoNum.BillingInvoice){
                mode = recData.withoutinventory?Wtf.autoNum.BillingInvoice:Wtf.autoNum.Invoice
                selRec = "&amount="+recData.amount+"&bills="+recData.billid+"&customer="+recData.personname + "&recordids=" + billIds + "&isConsignment="+recData.isConsignment+"&isLeaseFixedAsset="+obj.isLeaseFixedAsset+"&isDraft="+obj.isDraft;
                if(recData.isConsignment){ 
                    fileName="Consignment Sales Invoice "+recData.billno;
                    moduleid=Wtf.Acc_ConsignmentInvoice_ModuleId;
                }
                else {
                    fileName="Invoice "+recData.billno+"_v1";
                    moduleid=Wtf.Acc_Invoice_ModuleId;
                }
            }else if(get==Wtf.autoNum.PurchaseOrder||get==Wtf.autoNum.BillingPurchaseOrder){
                mode = recData.withoutinventory?Wtf.autoNum.BillingPurchaseOrder:Wtf.autoNum.PurchaseOrder
                selRec = "&amount="+recData.amount+"&isexpenseinv="+recData.isexpenseinv+"&bills="+recData.billid + "&recordids=" + billIds;
                if(recData.isConsignment){ 
                    fileName=WtfGlobal.getLocaleText("acc.venconsignment.order")+" "+recData.billno+"_v1";
                    selRec += "&isConsignment=" + true; 
                }else{
                    fileName="Purchase Order "+recData.billno+"_v1";
                }
            }else if(get==Wtf.autoNum.SalesOrder||get==Wtf.autoNum.BillingSalesOrder){
                mode = recData.withoutinventory?Wtf.autoNum.BillingSalesOrder:Wtf.autoNum.SalesOrder
                selRec = "&amount="+recData.amount+"&bills="+recData.billid + "&recordids=" + billIds + "&isConsignment="+recData.isConsignment+"&isLeaseFixedAsset="+obj.isLeaseFixedAsset;
                if(recData.isConsignment){ 
                    fileName=WtfGlobal.getLocaleText("acc.consignment.order")+" "+recData.billno+"_v1";
                    moduleid=Wtf.Acc_ConsignmentRequest_ModuleId;
                //                    isEdit=true;
                }
                else {
                    fileName="Sales Order "+recData.billno+"_v1";
                    moduleid=Wtf.Acc_Sales_Order_ModuleId;
                }
            }else if(get==Wtf.autoNum.GoodsReceipt||get==Wtf.autoNum.BillingGoodsReceipt){
                mode = recData.withoutinventory?Wtf.autoNum.BillingGoodsReceipt:Wtf.autoNum.GoodsReceipt
                selRec = "&amount="+recData.amount+"&isexpenseinv="+recData.isexpenseinv+"&bills="+recData.billid + "&recordids=" + billIds;
                if(recData.isConsignment){ 
                    fileName="Consignment Purchase Invoice "+recData.billno+"_v1";
                }
                else {
                    fileName="Vendor Invoice "+recData.billno+"_v1";
                }
            }else if(get==Wtf.autoNum.CreditNote||get==Wtf.autoNum.BillingCreditNote){
                mode=recData.withoutinventory?Wtf.autoNum.BillingCreditNote:Wtf.autoNum.CreditNote;
                selRec = "&amount="+recData.amount+"&bills="+recData.noteid+"&otherwise="+recData.otherwise + "&recordids=" + billIds ;
                fileName="Credit Note "+recData.noteno;
            }else if(get==Wtf.autoNum.DebitNote||get==Wtf.autoNum.BillingDebitNote){
                mode=recData.withoutinventory?Wtf.autoNum.BillingDebitNote:Wtf.autoNum.DebitNote;
                selRec = "&amount="+recData.amount+"&bills="+recData.noteid+"&otherwise="+recData.otherwise + "&recordids=" + billIds ;
                fileName="Debit Note "+recData.noteno+"_v1";
            }else if(get==Wtf.autoNum.Payment||get==Wtf.autoNum.BillingPayment){
                mode=recData.withoutinventory?Wtf.autoNum.BillingPayment:Wtf.autoNum.Payment;
                selRec = "&amount="+recData.amount+"&bills="+recData.billid+"&customer="+recData.paymentmethod+"&accname="+encodeURIComponent(recData.personname)+"&personid="+recData.personid+"&address="+recData.address+"&advanceFlag="+advanceFlag+"&advanceAmount="+recData.advanceamount + "&recordids=" + billIds + "&isOpeningBalanceTransaction=" + recData.isOpeningBalanceTransaction;
                fileName="Payment Made "+recData.billno;
            }else if(get==Wtf.autoNum.Receipt||get==Wtf.autoNum.BillingReceipt){
                mode=recData.withoutinventory?Wtf.autoNum.BillingReceipt:Wtf.autoNum.Receipt;
                selRec = "&amount="+recData.amount+"&bills="+recData.billid+"&customer="+recData.paymentmethod+"&accname="+encodeURIComponent(recData.personname)+"&personid="+recData.personid+"&address="+recData.address+"&advanceFlag="+advanceFlag+"&advanceAmount="+recData.advanceamount + "&recordids=" + billIds + "&isOpeningBalanceTransaction=" + recData.isOpeningBalanceTransaction;
                fileName="Payment Recieved "+recData.billno;
            }else if(get==Wtf.autoNum.Quotation){
                mode=get;
                selRec = "&amount="+recData.amount+"&bills="+recData.billid + "&recordids=" + billIds+"&isLeaseFixedAsset="+obj.isLeaseFixedAsset ;
                fileName="Quotation "+recData.billno+"_v1";
            }else if(get==Wtf.autoNum.CustomerQuoationVersion){
                mode=get;
                selRec = "&amount="+recData.amount+"&bills="+recData.billid + "&recordids=" + billIds ;
                fileName="Quotation Version "+recData.billno;
            }else if(get==Wtf.autoNum.VendorQuotationVersion){
                mode=get;
                selRec = "&amount="+recData.amount+"&bills="+recData.billid + "&recordids=" + billIds ;
                fileName="Vendor Quotation Version "+recData.billno;
            }else if(get==Wtf.autoNum.Venquotation){
                mode=get;
                selRec = "&amount="+recData.amount+"&bills="+recData.billid + "&recordids=" + billIds ;
                fileName="Quotation "+recData.billno+"_v1";
            }else if(get==Wtf.autoNum.DeliveryOrder ||get=="ProductCompositionExport"){
                mode=get;
                selRec = "&amount="+0+"&bills="+recData.billid + "&recordids=" + billIds +"&isConsignment="+recData.isConsignment+"&isLeaseFixedAsset="+obj.isLeaseFixedAsset ;
                if(recData.isConsignment){ 
                    fileName=WtfGlobal.getLocaleText("acc.Consignment.DO")+" "+recData.billno+"_v1";
                    moduleid=Wtf.Acc_ConsignmentDeliveryOrder_ModuleId;//for consignment request
                    isEdit=true;
                }
                else {
                    fileName="Delivery Order "+recData.billno+"_v1";
                    moduleid=Wtf.Acc_Delivery_Order_ModuleId;
                }
            }else if(get==Wtf.autoNum.DeliveryOrder ||get=="exportInvoicepackingList"){
                mode=get;
                selRec = "&amount="+0+"&bills="+recData.billid + "&recordids=" + billIds ;
                fileName="Invoice packing List "+recData.billno;
            }else if(get==Wtf.autoNum.SalesReturn){
                mode=get;
                selRec = "&amount="+0+"&bills="+recData.billid+ "&recordids=" + billIds +"&isConsignment="+recData.isConsignment+"&isLeaseFixedAsset="+obj.isLeaseFixedAsset;
                if(recData.isConsignment){ 
                    fileName="Consignment Sales Return "+recData.billno+"_v1";
                    moduleid=Wtf.Acc_ConsignmentSalesReturn_ModuleId;
                    isEdit=true;
                }else {
                    fileName="Sales Return"+recData.billno+"_v1";
                    moduleid=Wtf.Acc_Sales_Return_ModuleId;
                }
            }else if(get==Wtf.autoNum.GoodsReceiptOrder){
                mode=get;
                selRec = "&amount="+0+"&bills="+recData.billid + "&recordids=" + billIds ;
                if(recData.isConsignment){ 
                    fileName="Consignment Goods Receipt Order "+recData.billno+"_v1";
                }else{
                    fileName="Goods Receipt "+recData.billno+"_v1";
                }
            }else if(get==Wtf.autoNum.PurchaseReturn){
                mode=get;
                moduleid=Wtf.Acc_Purchase_Return_ModuleId;
                selRec = "&amount="+0+"&bills="+recData.billid+ "&recordids=" + billIds ;
                if(recData.isConsignment){ 
                    fileName="Consignment Purchase Return "+recData.billno+"_v1";
                }
                else {
                    fileName="Purchase Return "+recData.billno+"_v1";
                }
            }else if(get==Wtf.autoNum.JournalEntry){
                mode=Wtf.autoNum.JournalEntry;
                selRec = "&journalentryid="+recData.journalentryid+"&typeValue="+recData.typeValue+"&withoutinventory="+recData.withoutinventory;
                fileName="Payment Made "+recData.billno;
            }else if(get==Wtf.autoNum.Requisition){
                mode=get;
                selRec = "&amount="+0+"&bills="+recData.billid + "&recordids=" + billIds ;
                fileName="Purchase Requisition "+recData.billno;
            }else if(get==Wtf.autoNum.Contract){
                mode=get;
                selRec = "&amount="+0+"&bills="+recData.billid + "&recordids=" + billIds ;
                fileName="Sales Contract "+recData.billno;
            }else if(get==Wtf.autoNum.RFQ){
                mode=get;
                selRec = "&amount="+0+"&bills="+recData.billid + "&recordids=" + billIds ;
                fileName="RFQ "+recData.billno;
            }else if(get==Wtf.autoNum.accountrevaluationReprot){
                mode=get;
                selRec = "&amount="+0+"&bills="+recData.billid + "&recordids=" + billIds ;
                fileName="Account Revaluation";
            } else if (get === Wtf.autoNum.buildAssemblyReport) {
                mode = get;
                selRec = "&recordids=" + billIds;
                fileName = "Build Assembly";
            } else if (get === Wtf.autoNum.exportMRPWorkOrder) {
                //create parameters for print request
                mode = get;
                selRec = "&bills="+recData.billid + "&recordids=" + billIds;
                fileName = "MRP Work Order";
            } else if (get === Wtf.Bank_Reconciliation_ModuleId) {
                //create parameters for print request
                mode = get;
                selRec = "&recordids=" + billIds +"&accountid="+ obj.accountID +"&stdate="+ WtfGlobal.convertToGenericStartDate(obj.startDate.getValue()) +"&enddate="+ WtfGlobal.convertToGenericStartDate(obj.endDate.getValue());
                fileName = "Bank Reconciliation";
            }
            
            fileName = encodeURIComponent(fileName);

            if(this.templateId!=Wtf.Acc_Basic_Template_Id) {
                selRec += "&templateid="+this.templateId;
            }
            if(Wtf.account.companyAccountPref.defaultTemplateLogoFlag) {
                 selRec += "&isLetterHead=true";
            }else{
                 selRec += "&isLetterHead=false";
            }
            if(obj.isFixedAsset) {
                selRec += "&isFixedAsset=" + obj.isFixedAsset;
            }
            if(get != Wtf.autoNum.BankReconcilation)
                selRec = selRec.replace('#',''); 

            if(this.isDDTemplate){ //Document Designer
                //get browser version and send in request
                var userAgent = navigator.userAgent;
                var version = "";
                if(userAgent.lastIndexOf("Firefox/") > -1){
                    version = userAgent.substr(userAgent.lastIndexOf("Firefox/") + "Firefox/".length);
                    version = version.substr(0, version.indexOf("."));
                    selRec += "&browserVersion=" + version;
                }
                
                if (get == Wtf.autoNum.Invoice && this.templateId!=Wtf.Acc_Basic_Template_Id) {   //this.templateId!=Wtf.Acc_Basic_Template_Id+"P"- this condition is only used for senwan group in export packing list of Customer Invoice .[Mayur B]
                    var url="ACCInvoiceCMN/exportSingleInvoice.do";
                    var params = "moduleid="+moduleid+"&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype+"&templatesubtype="+this.templatesubtype;
                    if(this.filetype == 'print') {
                        this.postData(url,params);
//                        window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }    
                }else if (get == Wtf.autoNum.GoodsReceipt && this.templateId != Wtf.Acc_Basic_Template_Id) {
                    var url = "ACCGoodsReceiptCMN/exportSingleGoodsReceipt.do";
                    var params = "moduleid=6&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype;
                    if (selData != undefined && selData.length > 1) {
                        var isSameType = selData[0].data.isexpenseinv;
                        for (var gridRecCount = 1; gridRecCount < selData.length; gridRecCount++) {
                            if (selData[gridRecCount].data.isexpenseinv != isSameType) {
                                break;
                            }
                        }
                        if (selData.length != gridRecCount) {
                            Wtf.MessageBox.confirm("Confirm", WtfGlobal.getLocaleText("acc.alert.invoicePrint"), function (btn) {
                                if (btn == 'yes') {
                                    this.printRecord(url, params);
                                } else
                                    return;
                            }, this);
                        } else {
                            this.printRecord(url, params);
                        }
                    } else {
                        this.printRecord(url, params);
                    }
                }else if (get == Wtf.autoNum.SalesOrder && this.templateId!=Wtf.Acc_Basic_Template_Id ) {
                    var url="ACCSalesOrderCMN/exportSingleSO.do";
                    var params = "moduleid="+moduleid+"&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype+"&isEdit=" +isEdit+"&templatesubtype="+this.templatesubtype;
                    if(this.filetype == 'print') {
                        this.postData(url,params);
//                        window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }    
                }else if (get == Wtf.autoNum.PurchaseOrder && this.templateId!=Wtf.Acc_Basic_Template_Id ) {
                    var url="ACCPurchaseOrderCMN/exportSinglePO.do";
                    var params = "moduleid=18&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype;
                    /*
                     * Check for the selected purchase order is same type or different i.e. Expense or Product  
                     */
                    if (selData != undefined && selData.length > 1) {
                        var isSameType = selData[0].data.isexpenseinv;
                        for (var gridRecCount = 1; gridRecCount < selData.length; gridRecCount++) {
                            if (selData[gridRecCount].data.isexpenseinv != isSameType) {
                                break;
                            }
                        }
                        if (selData.length != gridRecCount) {
                            Wtf.MessageBox.confirm("Confirm", WtfGlobal.getLocaleText("acc.alert.orderPrint"), function (btn) {
                                if (btn == 'yes') {
                                    this.printRecord(url, params);
                                } else
                                    return;
                            }, this);
                        } else {
                            this.printRecord(url, params);
                        }
                    } else {
                        this.printRecord(url, params);
                    }   
                }else if (get == Wtf.autoNum.CreditNote && this.templateId!=Wtf.Acc_Basic_Template_Id) {
                    var url="ACCInvoiceCMN/exportSingleCreditNote.do";
                    var params = "moduleid=12&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype;
                    if(this.filetype == 'print') {
                        this.postData(url,params);
//                        window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }  
                }else if (get == Wtf.autoNum.DebitNote && this.templateId!=Wtf.Acc_Basic_Template_Id) {
                    var url="ACCGoodsReceiptCMN/exportSingleDebitNote.do";
                    var params = "moduleid=10&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype;
                    if(this.filetype == 'print') {
                        this.postData(url,params);
//                        window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }                
                } else if (get == Wtf.autoNum.Quotation && this.templateId!=Wtf.Acc_Basic_Template_Id ) {      
                    var url="ACCInvoiceCMN/exportSingleCustomerQuotation.do";
                    var params = "moduleid=22&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype;
                    if(this.filetype == 'print') {
                        this.postData(url,params);
//                        window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }    
                }else if (get == Wtf.autoNum.Venquotation && this.templateId!=Wtf.Acc_Basic_Template_Id) {
                    var url = "ACCGoodsReceiptCMN/exportSingleVendorQuotation.do";
                    var params = "moduleid=23&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype;
                    if(this.filetype == 'print') {
                        this.postData(url,params);
//                        window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }                
                } else if (get == Wtf.autoNum.DeliveryOrder && this.templateId!=Wtf.Acc_Basic_Template_Id && this.templateId!=Wtf.Acc_Basic_Template_Id+"DOtype1" && this.templateId!=Wtf.Acc_Basic_Template_Id+"DOtype2" && this.templateId!=Wtf.Acc_Basic_Template_Id+"DOtype3") {
                    var url = "ACCInvoiceCMN/exportSingleDeliveryOrder.do";
                    var params = "moduleid="+moduleid+"&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype+ "&isEdit=" +isEdit;
                    if(this.filetype == 'print') {
                        this.postData(url,params);
//                        window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }                
                }else if (get == Wtf.autoNum.GoodsReceiptOrder && this.templateId!=Wtf.Acc_Basic_Template_Id) {
                    var url = "ACCGoodsReceiptCMN/exportSingleGROrder.do";
                    var params =  "moduleid=28&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype;
                    if(this.filetype == 'print') {
                        this.postData(url,params);
//                        window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }                
                }else if (get == Wtf.autoNum.Payment && this.templateId!=Wtf.Acc_Basic_Template_Id) {
                    var url = "ACCVendorPaymentCMN/exportSingleMakePayment.do";
                    var params = "moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype + "&ispendingAproval="+ pendingapproval;
                    if(this.filetype == 'print') {
                        this.postData(url,params);
//                        window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }                
                }else if (get == Wtf.autoNum.Receipt && this.templateId!=Wtf.Acc_Basic_Template_Id&& this.templateId!=Wtf.Acc_Basic_Template_Id+"PaymentGL") {
                    var url = "ACCReceiptCMN/exportSingleReceivePayment.do";
                    var params = "moduleid=16&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype;
                    if(this.filetype == 'print') {
                        this.postData(url,params);
//                        window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }                

                } else if (get == Wtf.autoNum.SalesReturn && this.templateId != Wtf.Acc_Basic_Template_Id) {   //this.templateId!=Wtf.Acc_Basic_Template_Id+"P"- this condition is only used for senwan group in export packing list of Customer Invoice .[Mayur B]
                    var url = "ACCInvoiceCMN/exportSingleSalesReturn.do";
                    var params = "moduleid="+moduleid+"&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype;
                    if (this.filetype == 'print') {
                        this.postData(url,params);
//                        window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }
                } else if (get == Wtf.autoNum.PurchaseReturn && this.templateId != Wtf.Acc_Basic_Template_Id) {   
                    var url = "ACCGoodsReceiptCMN/exportSinglePurchaseReturn.do";
                    var params = "moduleid="+moduleid+"&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype;
                    if (this.filetype == 'print') {
                        this.postData(url,params);
//                        window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }
                }else if (get == Wtf.autoNum.RFQ && this.templateId!=Wtf.Acc_Basic_Template_Id ) {
                    var url="ACCPurchaseOrderCMN/exportSingleRFQ.do";
                    var params = "moduleid=33&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype;
                    if(this.filetype == 'print') {
                        this.postData(url,params);
                    //                        window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }   
                }else if (get == Wtf.autoNum.Requisition && this.templateId!=Wtf.Acc_Basic_Template_Id ) { //ERP-19851
                    var url="ACCPurchaseOrderCMN/exportSinglePR.do";
                    var params = "moduleid=32&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype;
                    if(this.filetype == 'print') {
                        this.postData(url,params);
                    //                        window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }   
                } else if (get == Wtf.autoNum.StockRepairReport && this.templateId!=Wtf.Acc_Basic_Template_Id) {   //this.templateId!=Wtf.Acc_Basic_Template_Id+"P"- this condition is only used for senwan group in export packing list of Customer Invoice .[Mayur B]
                    var url="ACCExportPrintCMN/exportSingleStockRepair.do";
                    var params = "moduleid="+Wtf.autoNum.StockRepairReport+"&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype+"&recordids=" + billIds+"&transactionmodules=" + transactionmodules+"&transactiono="+transactionnumbers;
                    if(this.filetype == 'print') {
                        this.postData(url,params);
//                        window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }    
                } else if (get == Wtf.autoNum.QAApprovalReport && this.templateId!=Wtf.Acc_Basic_Template_Id) {   //this.templateId!=Wtf.Acc_Basic_Template_Id+"P"- this condition is only used for senwan group in export packing list of Customer Invoice .[Mayur B]
                    var url="ACCExportPrintCMN/exportSingleQAApproval.do";
                    var params = "moduleid="+Wtf.autoNum.QAApprovalReport+"&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&contraentryflag=" + contraentryflag + "&filetype=" + this.filetype+"&recordids=" + recData.billid+"&transactionmodules=" + transactionmodules+"&transactiono="+transactionnumbers+"&moduletype="+moduletype+"&transactionid="+obj.rec.data.transactionid;
                    if(this.filetype == 'print') {
                        this.postData(url,params);
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }    
                } else if (get === Wtf.autoNum.buildAssemblyReport) {
                    var url = "ACCProductCMN/exportSingleBuildAssembly.do";
                    var params = "moduleid=" + Wtf.autoNum.buildAssemblyReport + "&filename=" + fileName + "&filetype=" + this.filetype + selRec;// + "&templatesubtype=" + this.templatesubtype;
                    if (this.filetype === 'print') {
                        this.postData(url, params);
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }
                } else if (get === Wtf.autoNum.exportMRPWorkOrder) {
                    //create print request and hit with parameters
                    var url = "ACCWorkOrderCMN/exportSingleMRPWorkOrder.do";
                    var params = "moduleid=" + Wtf.autoNum.exportMRPWorkOrder + "&filename=" + fileName + "&filetype=" + this.filetype + selRec;
                    if (this.filetype === 'print') {
                        this.postData(url, params);
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }
                } else if (get === Wtf.Bank_Reconciliation_ModuleId) {
                    //create print request and hit with parameters
                    var url = "ACCExportRecord/exportBankReconciliationDD.do";
                    var params = "moduleid=" + Wtf.Bank_Reconciliation_ModuleId + "&filename=" + fileName + "&filetype=" + this.filetype + selRec;
                    if (this.filetype === 'print') {
                        this.postData(url, params);
                    } else {
                        Wtf.get('downloadframe').dom.src = url;
                    }
                }
                
                if ( obj.moduleid === Wtf.Acc_Customer_AccountStatement_ModuleId || obj.moduleid == Wtf.Acc_AgedReceivables_Summary_ModuleId) {
                    var url = "ACCReports/printSOACustomerDD.do";
                    var fileName = "SOA_Customer";
                    var paramsString= "";
                    var isSortedOnCreationDate = false;
                    if(this.params){
                        for(var index in this.params) {
                            if ( this.params[index] === undefined  ) {
                            }else {
                                paramsString += "&"+index +"="+this.params[index]+"";
                            }
                        }
                    }
                    isSortedOnCreationDate = obj.groupCombo.getValue() == 1 ?true:false;
                    var params = "moduleid=60&filename=" + fileName + "&templateid="+this.templateId+"&filetype=print" + paramsString+"&templatesubtype="+this.templatesubtype+"&isSortedOnCreationDate="+isSortedOnCreationDate+"&reportid="+obj.reportid;
//                    if ( this.params.customerIds ) {
//                        params += "&customerIds=" + this.params.customerIds;
//                    }
                    this.postData(url,params);
                } else if ( obj.moduleid === Wtf.Acc_Vendor_AccountStatement_ModuleId || obj.moduleid == Wtf.Acc_AgedPayables_Summary_ModuleId ) {
                    var paramsString= "";
                    var isSortedOnCreationDate = false;
                    if(this.params){
                        for(var index in this.params) {
                            if ( this.params[index] === undefined  ) {
                            }else {
                                paramsString += "&"+index +"="+this.params[index]+"";
                            }
                        }
                    }
                    var url = "ACCReports/printSOAVendorDD.do";
                    var fileName = "SOA_Vendor";
                    isSortedOnCreationDate = obj.groupCombo.getValue() == 1 ?true:false;
                    var params = "moduleid=61&filename=" + fileName + "&templateid="+this.templateId+"&filetype=print" + paramsString+"&templatesubtype="+this.templatesubtype+"&isSortedOnCreationDate="+isSortedOnCreationDate+"&reportid="+obj.reportid;
//                    if ( this.params.customerIds ) {
//                        params += "&vendorIds=" + this.params.vendorIds;
//                    }
                    this.postData(url,params);
                } 
                
            }else if(this.isDDTemplate == undefined || (this.filetype=='pdf')){  //Export and Default Template-Jasper
                
                if(get == Wtf.autoNum.BankReconcilation){
                    Wtf.get('downloadframe').dom.src = "ACCReports/exportBankReconciliation.do?&accountid=" + recData.accountid + "&stdate=" + recData.stdate +"&enddate="+ recData.enddate + "&isConcileReport=" + recData.isConcileReport+"&ss=" + recData.ss+"&dateFilter=" +recData.dateFilter;
            
                } else if (get == Wtf.autoNum.Invoice && (Wtf.templateflag == Wtf.Diamond_Aviation_templateflag || Wtf.templateflag == Wtf.Alfatech_templateFlag)){
                    var url="ACCInvoiceCMN/exportDiamondAviationCustomerInvoice.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds+"&templateflag="+Wtf.templateflag + "&dtype=report";
                
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype0"){
                        url+="&type=0";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype1"){
                        url+="&type=1";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype2"){
                        url+="&type=2";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype3"){
                        url+="&type=3";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype4"){
                        url+="&type=4";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype5"){
                        url+="&type=5";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype6"){
                        url+="&type=6";
                    }else{
                        url+="&type=0";
                    }
                    Wtf.get('downloadframe').dom.src = url ;
                } else if (get == Wtf.autoNum.Invoice && Wtf.templateflag == Wtf.Monzone_templateflag ){
                    var url="ACCInvoiceCMN/exportMonzoneTaxInvoice.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds+"&templateflag="+Wtf.templateflag;
                    Wtf.get('downloadframe').dom.src = url ;
                } else if (get == Wtf.autoNum.Invoice && Wtf.templateflag == Wtf.KimChey_templateflag){
                    var url="ACCInvoiceCMN/exportKimCheyInvoice.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds+"&templateflag="+Wtf.templateflag;
                     if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype2"){
                        url+="&type=2";
                    }if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype1"){
                        url+="&type=1";
                    }else {
                        url+="&type=0";
                    }           

                   Wtf.get('downloadframe').dom.src = url ;
                }else if(get==Wtf.autoNum.RFQ){
                    Wtf.get('downloadframe').dom.src = "ACCPurchaseOrderCMN/exportDefaultRFQ.do?mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag;
                }
                else if (get == Wtf.autoNum.Quotation &&  Wtf.account.companyAccountPref.activateProfitMargin && this.templateId == Wtf.Acc_Basic_Template_Id+"DOtype1") {
                    Wtf.get('downloadframe').dom.src = "ACCSalesOrderCMN/exportSBICustomerQoutationJasper.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds ;
                }
                else if (get == Wtf.autoNum.Quotation && (Wtf.templateflag == 2||Wtf.templateflag == 7 ||Wtf.templateflag == 10)) {
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"Q") {
                        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportSenwanGroupSingleCustomerQuotation.do?moduleid=22&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds+"&shortQuoteFlag="+1;  // shortQuoteFlag=1 for short template of senwan tech otherwise it should be 0. [Mayur B]

                    }else{
                        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportSenwanGroupSingleCustomerQuotation.do?moduleid=22&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds +"&shortQuoteFlag="+0;
                    }
                }
                else if (get == Wtf.autoNum.Quotation && (Wtf.templateflag == 12)) {    //TemplateFlag = 12 for Henggaun Customer Quotation with Product Image
                    if(this.templateId==Wtf.Acc_Basic_Template_Id) {
                        Wtf.get('downloadframe').dom.src = "ACCSalesOrderCMN/exportHengguanCustomerQuotationReport.do?moduleid=22&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds+"&shortQuoteFlag="+0;
                    }
                }
                else if (get == Wtf.autoNum.Quotation && Wtf.templateflag == 6) {
                    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportVHQCustomerQuotation.do?moduleid=22&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds ;
                } 
                else if (get == Wtf.autoNum.Quotation && Wtf.templateflag == Wtf.BestSafety_templateflag) {
                    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportCustomerQuotationJasper.do?moduleid=22&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds ;
                }
                else if (get == Wtf.autoNum.Quotation && Wtf.templateflag == Wtf.Tony_FiberGlass_templateflag && (this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype1" || this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype0")) {
                    var url = "ACCInvoiceCMN/exportCustomerQuotationForTonyFibreGlass.do?moduleid=22&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds+"&isExport=true" ;
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype1"){
                        url+="&type=1";// TonyFibreGlass CustomerQuotation  for ConcreteFloorlining
                    }else {
                        url+="&type=0";// TonyFibreGlass Quotation for Pipe Spool"
                    }           
                    Wtf.get('downloadframe').dom.src = url;
                }
                else if (get == Wtf.autoNum.Quotation && Wtf.templateflag == Wtf.Diamond_Aviation_templateflag) {
                    var url="ACCInvoiceCMN/exportDiamondAviationCustomerQuotation.do?moduleid=22&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds + "&dtype=report";
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype0"){
                        url+="&type=0";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype1"){
                        url+="&type=1";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype2"){
                        url+="&type=2";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype3"){
                        url+="&type=3";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype4"){
                        url+="&type=4";
                    }
                    Wtf.get('downloadframe').dom.src = url ;
                }
                else if ((get == Wtf.autoNum.Quotation || get == Wtf.autoNum.Invoice) && (Wtf.templateflag == Wtf.RightSpace_templateflag || Wtf.templateflag == Wtf.RightWork_templateflag)) {
                    var url="ACCInvoiceCMN/exportRightSpaceCustomerQuoteAndInvoice.do?moduleid=22&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds +"&dtype=report" ;
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype0"){
                        url+="&type=0";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype1"){
                        url+="&type=1";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype2"){
                        url+="&type=2";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype3"){
                        url+="&type=3";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype4"){
                        url+="&type=4";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype5"){
                        url+="&type=5";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype6"){
                        url+="&type=6";
                    }
                    Wtf.get('downloadframe').dom.src = url ;
                }
                else if (get == Wtf.autoNum.Quotation && Wtf.templateflag == Wtf.Monzone_templateflag) {
                    var url="ACCInvoiceCMN/exportMonzoneCustomerQuotation.do?moduleid=22&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype1"){
                        url+="&type=1";
                    }else {
                        url+="&type=2";
                    }  
                    Wtf.get('downloadframe').dom.src = url ;
                }
                else if (get == Wtf.autoNum.Quotation && Wtf.templateflag == Wtf.F1Recreation_templateflag) {
                    var url = "ACCInvoiceCMN/exportF1RecreationCustomerQuotation.do?moduleid=22&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds ;
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype1"){
                        url+="&type=1";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype2"){
                        url+="&type=2";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype3"){
                        url+="&type=3";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype4"){
                        url+="&type=4";
                    }else{
                        url+="&type=0";
                    }  
                    Wtf.get('downloadframe').dom.src = url;
                }else if (get == Wtf.autoNum.Quotation && Wtf.templateflag == Wtf.Guan_Chong_templateflag && this.templateId==Wtf.Acc_Basic_Template_Id+"proInv") {
                    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportGuanChongProformaInvoice.do?moduleid=22&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                }
//                else if ((get == Wtf.autoNum.Payment) && Wtf.templateflag == 8 && !contraentryflag && !recData.isadvancepayment && recData.receipttype==1) {
//                    Wtf.get('downloadframe').dom.src = "ACCVendorPaymentCMN/SatsPaymentVoucher.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&againstGLFlag=false"+"&recordids=" + billIds ;
//                }
                else if (get == Wtf.autoNum.CreditNote && Wtf.templateflag == 8 && this.filetype!="print") {
                    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportSATSCreditNoteJasperReport.do?moduleid=12&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag +"&otherwise="+recData.otherwise+"&recordids=" + billIds ;
                }
                //            else if (get == Wtf.autoNum.CreditNote && Wtf.templateflag == Wtf.Guan_Chong_templateflag ) {
                //                Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportGCBCreditNoteJasperReport.do?moduleid=12&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag +"&otherwise="+recData.otherwise+"&recordids=" + billIds ;
                //            }
                else if (get == Wtf.autoNum.DebitNote && Wtf.templateflag == 8 ) {
                    Wtf.get('downloadframe').dom.src = "ACCGoodsReceiptCMN/exportSATSDebitNoteJasperReport.do?moduleid=10&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag +"&otherwise="+recData.otherwise+"&recordids=" + billIds ;
                }
                //            else if (get == Wtf.autoNum.DebitNote && Wtf.templateflag == Wtf.Guan_Chong_templateflag ) {
                //                Wtf.get('downloadframe').dom.src = "ACCGoodsReceiptCMN/exportGCBDebittNoteJasperReport.do?moduleid=12&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag +"&otherwise="+recData.otherwise+"&recordids=" + billIds ;
                //            }
                else if ((get == Wtf.autoNum.Invoice||get == Wtf.autoNum.GoodsReceipt) && Wtf.templateflag == 8 && this.filetype != "print" && recData.invoicetype!="" && recData.invoicetype!=null && recData.invoicetype!=undefined){
                    var url="";
                    if(recData.invoicetype=="ff808081434d75f2014351835fc70003"){    ///invoicetype id in the for adhoc invoice..
                        //if(this.templateId==Wtf.Acc_Basic_Template_Id+"adHOC") {
                        url="Ad-Hoc";   //for export Ad-Hoc Invoice type of Customer Invoice
                    }
                    else if(recData.invoicetype=="ff808081434d75f201435182a6270002") {  // invoicetype id for marin invoice
                        url="Marine";  //for export Marine Invoice type of Customer Invoice.
                    }
                    else if (recData.invoicetype=="ff808081434d75f20143518400630005" || recData.invoicetype=="ff808081434d75f20143518400630008") {
                        url="RetailInFix";  //for export Retail Invoice-Fixed type of Customer Invoice.
                    }
                    else if (recData.invoicetype=="ff808081434d75f20143518438fe0006") {
                        url="RetailInvVar";  // for export Retail Invoice-Variable type of Customer Invoice
                    }
                    else if (recData.invoicetype=="ff808081434d75f201435183b3270004") {
                        url="VisitPassInv";  // for
                    }
                    else if (recData.invoicetype=="ff808081434d75f201435183b3270007") {
                        url="WaterSale";  // for
                    }
                    else if (recData.invoicetype=="ff808081434d75f20143518400630009") {
                        url="SecurityOfficer";  // for
                    }
                    else if (recData.invoicetype=="ff808081434d75f20143518400630010") {
                        url="Event";  // for
                    }
            
            
                    if(get == Wtf.autoNum.Invoice){
                        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportSatsTaxInvoiceJasper.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&invoicetype=" +url+"&recordids=" + billIds ;
                    }else if(get == Wtf.autoNum.GoodsReceipt){
                        Wtf.get('downloadframe').dom.src = "ACCGoodsReceiptCMN/exportSatsVendorTaxInvoiceJasper.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&invoicetype=" +url+"&recordids=" + billIds ;
                    }
                }else if (get == Wtf.autoNum.Quotation && (Wtf.templateflag==Wtf.FastenHardwareEngineering_templateflag || Wtf.templateflag==Wtf.FastenEnterprises_templateflag)) {
                    var url="ACCInvoiceCMN/exportCustomerQuotationForFasten.do";
                    var params = "moduleid=22&mode=" + mode + "&rec=" + selRec + "&isLeaseFixedAsset="+recData.fixedAssetLeaseInvoice+"&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                    this.postData(url,params);
                }
                else if (get == Wtf.autoNum.Quotation && (Wtf.templateflag==Wtf.hinsitsu_templateflag & this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype1")) {   //Hinsitsu CQ - Sample Request Jasper PDF
                    var url="ACCInvoiceCMN/exportHinsitsuCustomerQoutationJasper.do";
                    var params = "moduleid=22&mode=" + mode + "&rec=" + selRec + "&isLeaseFixedAsset="+recData.fixedAssetLeaseInvoice+"&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&pendingapproval="+pendingapproval+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                    this.postData(url,params);
                }
                else if (get == Wtf.autoNum.jwproductsummary && this.templateId==Wtf.Acc_Basic_Template_Id+"JWPSR") {   //Hinsitsu CQ - Sample Request Jasper PDF
                    var url="ACCJobWorkController/exportJWProductSummaryjasperreport.do";
                    var params = "moduleid=22&mode=" + mode + "&rec=" + selRec + "&isLeaseFixedAsset="+recData.fixedAssetLeaseInvoice+"&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&pendingapproval="+pendingapproval+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds+"&startdate=" + WtfGlobal.convertToGenericStartDate(this.obj.startDate.getValue()) +"&enddate=" + WtfGlobal.convertToGenericStartDate(this.obj.endDate.getValue())+"&productid=" + this.obj.productcmb.getValue()+"&customerid=" + this.obj.custmerCmb.getValue();
                    this.postData(url,params);
                }
                else if (this.templateId=="exportInvoicepackingList" && (Wtf.templateflag == 1||Wtf.templateflag == 5 || Wtf.templateflag == 13 ||Wtf.templateflag == Wtf.F1Recreation_templateflag || Wtf.templateflag == Wtf.F1RecreationLeasing_templateflag ||Wtf.templateflag == 23)) {
                    Wtf.get('downloadframe').dom.src = "ACCGoodsReceiptCMN/exportInvoicepackingList.do?moduleid=27&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                }else if (this.templateId== "exportPermitInvoiceListPdf" && (Wtf.templateflag == 1||Wtf.templateflag == 5 || Wtf.templateflag == 13 ||Wtf.templateflag == Wtf.F1Recreation_templateflag || Wtf.templateflag == Wtf.F1RecreationLeasing_templateflag ||Wtf.templateflag == 23)) {
                    Wtf.get('downloadframe').dom.src = "ACCGoodsReceiptCMN/exportPermitInvoiceList.do?mode="+mode+"&filename="+fileName+"&filetype=pdf"+"&templateflag="+Wtf.templateflag+"&billIds="+billIds; ;
                } else  if (this.templateId == "ProductCompositionExport" && (Wtf.templateflag == 1||Wtf.templateflag == 5 || Wtf.templateflag == 13 ||Wtf.templateflag == Wtf.F1Recreation_templateflag || Wtf.templateflag == Wtf.F1RecreationLeasing_templateflag ||Wtf.templateflag == 23)){
                    Wtf.get('downloadframe').dom.src = "ACCGoodsReceiptCMN/exportProductCompositionJasper.do?moduleid=27&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                } 
                else if (get == Wtf.autoNum.DeliveryOrder && (Wtf.templateflag == 1||Wtf.templateflag == 5 || Wtf.templateflag == 13 ||Wtf.templateflag == Wtf.F1Recreation_templateflag || Wtf.templateflag == Wtf.F1RecreationLeasing_templateflag || Wtf.templateflag == 31)) {
                    var url= "ACCGoodsReceiptCMN/exportDeliveryOrderJasper.do?moduleid=27&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds + "&dtype=report";
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"DOtype1"){
                        url+="&type=1";  //for F1Recreation Packing List
                    }else{
                        url+="&type=2";  //for F1Recreation Delivery Order.
                    }
                    Wtf.get('downloadframe').dom.src=url; 
                }else if (get == Wtf.autoNum.DeliveryOrder &&  Wtf.templateflag == Wtf.KimChey_templateflag ) {
                    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportKimCheyDeliveryOrder.do?moduleid=27&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                }else if (get == Wtf.autoNum.DeliveryOrder &&  Wtf.templateflag == Wtf.Monzone_templateflag ) {
                    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportMonzoneDeliveryOrder.do?moduleid=27&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                }else if (get == Wtf.autoNum.DeliveryOrder && (Wtf.templateflag == 7|| Wtf.templateflag == 10)) {
                    var url= "ACCGoodsReceiptCMN/exportPacificTecDeliveryOrderJasper.do?moduleid=27&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds;  
                    Wtf.get('downloadframe').dom.src=url;
                }
                else if (get == Wtf.autoNum.DeliveryOrder && Wtf.templateflag == Wtf.Diamond_Aviation_templateflag && this.templateId !=Wtf.Acc_Basic_Template_Id && this.templateId!=Wtf.Acc_Basic_Template_Id+"DOtype1" && this.templateId!=Wtf.Acc_Basic_Template_Id+"DOtype2" && this.templateId!=Wtf.Acc_Basic_Template_Id+"DOtype3") {
                    var url= "ACCGoodsReceiptCMN/exportDiamondAviationDeliveryOrderPackages.do?moduleid=27&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds + "&dtype=report";
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"Package1"){//for DIAMOND AVIATION Packages
                        url+="&package=1";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"Package2"){//for DIAMOND AVIATION Packages
                        url+="&package=2";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"Package3"){//for DIAMOND AVIATION Packages
                        url+="&package=3";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"Package4"){//for DIAMOND AVIATION Packages
                        url+="&package=4";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"Package5"){//for DIAMOND AVIATION Packages
                        url+="&package=5";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"Package6"){//for DIAMOND AVIATION Packages
                        url+="&package=6";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"Package7"){//for DIAMOND AVIATION Packages
                        url+="&package=7";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"Package8"){//for DIAMOND AVIATION Packages
                        url+="&package=8";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"Package9"){//for DIAMOND AVIATION Packages
                        url+="&package=9";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"Package10"){//for DIAMOND AVIATION Packages
                        url+="&package=10";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"Package11"){//for DIAMOND AVIATION Packages
                        url+="&package=11";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"Package12"){//for DIAMOND AVIATION Packages
                        url+="&package=12";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"Package13"){//for DIAMOND AVIATION Packages
                        url+="&package=13";
                    }     
                    Wtf.get('downloadframe').dom.src=url;
                }
                else if (get == Wtf.autoNum.DeliveryOrder && Wtf.templateflag == Wtf.Diamond_Aviation_templateflag && this.templateId !=Wtf.Acc_Basic_Template_Id) {
                    var url= "ACCGoodsReceiptCMN/exportDiamondAviationDeliveryOrder.do?moduleid=27&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds;  
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"DOtype1"){//for DIAMOND AVIATION PACKAGING SLIP
                        url+="&type=1";      
                        fileName="Packaging Slip"
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DOtype2"){//for DIAMOND AVIATION DELIVERY ORDER
                        url+="&type=2";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DOtype3"){//for DIAMOND AVIATION DELIVERY ORDER
                        url+="&type=3";
                    }     
                    Wtf.get('downloadframe').dom.src=url;
                }
                else if ((get == Wtf.autoNum.Receipt||get==Wtf.autoNum.BillingReceipt) && (Wtf.templateflag == 1||Wtf.templateflag == 5) && !contraentryflag && !recData.isadvancepayment && recData.receipttype!=6 && recData.receipttype!=7 && !Wtf.isNewPaymentStructure) { // Wtf.templateflag =1 then its Fact like templates
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"PaymentGL") {
                        Wtf.get('downloadframe').dom.src = "ACCReceiptCMN/exportPettyCashVoucher.do?moduleid=16&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&againstGLFlag=true"+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                    }else {
                        if(this.templateId==Wtf.Acc_Basic_Template_Id+"PaymentGL") {
                            Wtf.get('downloadframe').dom.src = "ACCReceiptCMN/exportPettyCashVoucher.do?moduleid=16&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&againstGLFlag=true"+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                        }else {
                            Wtf.get('downloadframe').dom.src = "ACCReceiptCMN/exportPettyCashVoucher.do?moduleid=16&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                        }
                    }
                }
                //     else if ((get == Wtf.autoNum.Payment||get==Wtf.autoNum.BillingPayment) && (Wtf.templateflag == 141 || Wtf.templateflag == 142 || Wtf.templateflag == 143 || Wtf.templateflag == 144 || Wtf.templateflag == 145 || Wtf.templateflag == 146) && !contraentryflag && !recData.isadvancepayment && (recData.receipttype==1||recData.receipttype==7||recData.receipttype==9)) { //1=MP against VI,7=Mp against CN,9=MP against GL code.
                //                Wtf.get('downloadframe').dom.src = "ACCVendorPaymentCMN/exportTIDPaymentVoucher.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&againstGLFlag=true"+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                //            } //for TID templates is using new payment structure
                else if ((get == Wtf.autoNum.Payment||get==Wtf.autoNum.BillingPayment) && Wtf.templateflag == 3 && !contraentryflag && !recData.isadvancepayment && (recData.receipttype==9) ) {
                    Wtf.get('downloadframe').dom.src = "ACCVendorPaymentCMN/exportFerrateGroupPaymentVoucher.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&againstGLFlag=true"+"&recordids=" + billIds ;
                }
                else if ((get == Wtf.autoNum.Payment||get==Wtf.autoNum.BillingPayment) && Wtf.templateflag == 3 && !contraentryflag && !recData.isadvancepayment && (recData.receipttype==9) ) {
                    Wtf.get('downloadframe').dom.src = "ACCVendorPaymentCMN/exportFerrateGroupPaymentVoucher.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&againstGLFlag=true"+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                }
                else if ((get == Wtf.autoNum.Payment||get==Wtf.autoNum.BillingPayment) && Wtf.templateflag == 10 && !contraentryflag && !recData.isadvancepayment && (recData.receipttype==9) ) {
                    Wtf.get('downloadframe').dom.src = "ACCVendorPaymentCMN/exportSenwanTecPaymentVoucher.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&againstGLFlag=true"+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                }
                //            else if ((get == Wtf.autoNum.Payment||get==Wtf.autoNum.BillingPayment) && Wtf.templateflag == 4 && !contraentryflag && !recData.isadvancepayment && (recData.receipttype==1||recData.receipttype==7||recData.receipttype==9)) {
                //                Wtf.get('downloadframe').dom.src = "ACCVendorPaymentCMN/exportLSHPaymentVoucher.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&againstGLFlag=true"+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                //            }
                //            else if ((get == Wtf.autoNum.Receipt||get==Wtf.autoNum.BillingReceipt) && Wtf.templateflag == 4 && !contraentryflag && !recData.isadvancepayment && (recData.receipttype==1||recData.receipttype==7||recData.receipttype==9)) {
                //                Wtf.get('downloadframe').dom.src = "ACCReceiptCMN/exportLSHGroupReceipt.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&againstGLFlag=true"+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                //            }
                else if ((get == Wtf.autoNum.Payment||get==Wtf.autoNum.BillingPayment) && (Wtf.templateflag == 1||Wtf.templateflag == 5) && !contraentryflag && !recData.isadvancepayment && recData.receipttype!=6 && recData.receipttype!=7 && !Wtf.isNewPaymentStructure) {
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"PaymentGL") {
                        Wtf.get('downloadframe').dom.src = "ACCVendorPaymentCMN/exportPettyCashVoucher.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&againstGLFlag=true"+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                    }else {
                        Wtf.get('downloadframe').dom.src = "ACCVendorPaymentCMN/exportPettyCashVoucher.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                    }
                }
                else if (get == Wtf.autoNum.JournalEntry && (Wtf.templateflag == 1||Wtf.templateflag == 5) && !contraentryflag) {
                    Wtf.get('downloadframe').dom.src = "ACCReports/exportContraPaymentVoucher.do?moduleid=24&mode=" + mode + "&rec=" + selRec + "&filetype=pdf"+"&companyid="+companyid+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                }
                
                else if (get == Wtf.autoNum.PurchaseOrder && Wtf.templateflag == 2 && !recData.isConsignment) { //unable Export vendor consignment request ERP-9821
                    Wtf.get('downloadframe').dom.src = "ACCPurchaseOrderCMN/exportSenwanGroupPurchaseOrderJasper.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName+"_v1"+"&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds ;
                }
                else if (get == Wtf.autoNum.PurchaseOrder && (Wtf.templateflag == 3 ||Wtf.templateflag == 7)) {
                    Wtf.get('downloadframe').dom.src = "ACCPurchaseOrderCMN/exportFerrateGroupPurchaseOrderJasper.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                }
                else if (get == Wtf.autoNum.PurchaseOrder && (Wtf.templateflag == Wtf.Diamond_Aviation_templateflag ||Wtf.templateflag == Wtf.Alfatech_templateFlag || Wtf.templateflag == Wtf.Tony_FiberGlass_templateflag)) {
                    var url="ACCPurchaseOrderCMN/exportDiamondAviationPurchaseOrderJasper.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds + "&dtype=report";
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype0"){
                        url+="&type=0";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype1"){
                        url+="&type=1";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype2"){
                        url+="&type=2";
                    }
                    Wtf.get('downloadframe').dom.src = url ;
                }
                else if (get == Wtf.autoNum.PurchaseOrder && Wtf.templateflag == Wtf.Monzone_templateflag) {
                    var url="ACCPurchaseOrderCMN/exportMonzonePuchaseOrder.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                    Wtf.get('downloadframe').dom.src = url ;
                }
                else if (get == Wtf.autoNum.SalesOrder && Wtf.templateflag == 6 && this.templateId != Wtf.Acc_Basic_Template_Id+"DOtype1") {
                    Wtf.get('downloadframe').dom.src = "ACCSalesOrderCMN/exportVHQSalesOrderJasper.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds ;
                }
                else if (get == Wtf.autoNum.SalesOrder &&  Wtf.account.companyAccountPref.activateProfitMargin && this.templateId == Wtf.Acc_Basic_Template_Id+"DOtype1") {
                    Wtf.get('downloadframe').dom.src = "ACCSalesOrderCMN/exportSBISalesOrderJasper.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds ;
                }
                else if (get == Wtf.autoNum.Invoice && (Wtf.templateflag == 3 ||Wtf.templateflag == 7 ||Wtf.templateflag == 10|| Wtf.templateflag == 15 )){
                    url = "ACCInvoiceCMN/exportFerrateGroupTaxInvoiceJasper.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                    if(Wtf.templateflag == 15 && this.templateId==Wtf.Acc_Basic_Template_Id+"P") {
                        Wtf.get('downloadframe').dom.src = url +"&isAdhocFlag="+ false;   //for HCIS Fees Tax Invoice
                    }else if(Wtf.templateflag == 15) {
                        Wtf.get('downloadframe').dom.src = url +"&isAdhocFlag="+ true;  //for HCIS Adhoc Tax Invoice
                    }else{
                        Wtf.get('downloadframe').dom.src = url;
                    }
                }else if (get == Wtf.autoNum.GoodsReceipt && (Wtf.templateflag == 3||Wtf.templateflag == 141 || Wtf.templateflag == 142 || Wtf.templateflag == 143 || Wtf.templateflag == 144 || Wtf.templateflag == 145 || Wtf.templateflag == 146)&&!recData.isexpenseinv){
                    Wtf.get('downloadframe').dom.src = "ACCGoodsReceiptCMN/exportFerrateGroupVendorInvoiceJasper.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                }
                else if (get == Wtf.autoNum.Invoice && Wtf.templateflag == 2){
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"P") {
                        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportSenwanCommercialInvoiceJasper.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&isPackingList=" + true+"&recordids=" + billIds ;
                    }
                    else {
                        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportSenwanCommercialInvoiceJasper.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&isPackingList=" + false+"&recordids=" + billIds ;
                    }
                }

                else if (get == Wtf.autoNum.Invoice && (Wtf.templateflag == 1||Wtf.templateflag == 5)) {
                    var url="ACCInvoiceCMN/exportCustomerInvoiceReport.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"P") {
                        Wtf.get('downloadframe').dom.src = url +"&isMultipleFlag=" + true +"&isSqueezeFlag="+ false;
                    }
                    else if(this.templateId==Wtf.Acc_Basic_Template_Id+"SSP") {
                        Wtf.get('downloadframe').dom.src = url +"&isMultipleFlag=" + false +"&isSqueezeFlag="+ true;
                    }
                    else if(this.templateId==Wtf.Acc_Basic_Template_Id+"SMP") {
                        Wtf.get('downloadframe').dom.src = url +"&isMultipleFlag=" + true +"&isSqueezeFlag="+ true;
                    }
                    else {
                        Wtf.get('downloadframe').dom.src = url +"&isMultipleFlag=" + false +"&isSqueezeFlag="+ false;
                    }
                }
                else if (get == Wtf.autoNum.Invoice && Wtf.templateflag == Wtf.spaceTec_templateflag) {
                    var url="ACCInvoiceCMN/exportCustomerInvoiceReport.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"P") {
                        Wtf.get('downloadframe').dom.src = url +"&isLetter=" + true;
                    }
                    else if(this.templateId==Wtf.Acc_Basic_Template_Id+"SSP") {
                        Wtf.get('downloadframe').dom.src = url +"&isLetter=" + false;
                    }
                }
                else if (get == Wtf.autoNum.Invoice && Wtf.templateflag == 4) {
                    var url="ACCInvoiceCMN/exportLSHCustomerInvoiceReport.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds ;
                    Wtf.get('downloadframe').dom.src = url ;
                }
                
                else if (get == Wtf.autoNum.Invoice && Wtf.templateflag == 16) {
                    var url="ACCInvoiceCMN/exportBMCustomerInvoiceReport.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds ;
                    Wtf.get('downloadframe').dom.src = url ;
                }
                else if (get == Wtf.autoNum.Invoice && (Wtf.templateflag == Wtf.F1Recreation_templateflag || Wtf.templateflag == Wtf.F1RecreationLeasing_templateflag)) {//F1 recereation tax invoice jasper
                    var url= "ACCInvoiceCMN/exportFOneCustomerInvoiceReport.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&isPackingList=" + true+"&recordids=" + billIds+"&templateflag="+Wtf.templateflag;
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype0"){
                        url+="&type=0";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype1"){
                        url+="&type=1";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype2"){
                        url+="&type=2";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype3"){
                        url+="&type=3";
                    }
                    Wtf.get('downloadframe').dom.src =url;
                }
                else if (get == Wtf.autoNum.PurchaseOrder && (Wtf.templateflag == 1||Wtf.templateflag == 5) ) {
                    Wtf.get('downloadframe').dom.src = "ACCPurchaseOrderCMN/exportPurchaseOrderJasper.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                }
                else if (get == Wtf.autoNum.PurchaseOrder && (Wtf.templateflag == Wtf.Swatow_templateflag) ) {
                    Wtf.get('downloadframe').dom.src = "ACCPurchaseOrderCMN/exportSwatowPurchaseOrderJasper.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                }
                else if (get == Wtf.autoNum.PurchaseOrder && (Wtf.templateflag == Wtf.F1Recreation_templateflag || Wtf.templateflag == Wtf.F1RecreationLeasing_templateflag)) {
                    Wtf.get('downloadframe').dom.src = "ACCPurchaseOrderCMN/exportF1RecreationPurchaseOrder.do?moduleid=14&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                }else if (get == Wtf.autoNum.Requisition && Wtf.templateflag == Wtf.Guan_Chong_templateflag ) {
                    Wtf.get('downloadframe').dom.src = "ACCPurchaseOrderCMN/exportGCBPurchaseRequisition.do? &mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                }
                else if (get == Wtf.autoNum.Requisition) {
                    Wtf.get('downloadframe').dom.src = "ACCPurchaseOrderCMN/exportPurchaseRequisition.do? &mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                }
                else if((get == Wtf.autoNum.SalesReturn || get == Wtf.autoNum.PurchaseReturn) && (Wtf.templateflag==Wtf.FastenHardwareEngineering_templateflag || Wtf.templateflag==Wtf.FastenEnterprises_templateflag)) { //for Fasten Companies a new method in controller
                    var moduleid = get == Wtf.autoNum.SalesReturn ? 29:31;
                    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportCNDNSRPRJasperForFasten.do?moduleid=" + moduleid + "&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds;
                }
                else if(get == Wtf.autoNum.PurchaseReturn && (Wtf.templateflag== Wtf.Guan_Chong_templateflag || Wtf.templateflag == Wtf.Guan_ChongBF_templateflag) &&  recData.isNoteAlso == true) {//Guan Chong  Sales Return credit note
                    var url="ACCInvoiceCMN/exportPurchaseReturn.do? &mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;                                
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype0"){
                        url+="&type=0";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype1"){
                        url+="&type=1";
                    }
                    Wtf.get('downloadframe').dom.src =url; 
                }
                else if(get == Wtf.autoNum.PurchaseReturn && (Wtf.templateflag==Wtf.BuildMate_templateflag || recData.isNoteAlso == true)) {//Buildamate Purchase return
                    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportPurchaseReturn.do? &mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;                  
                }
                else if(get == Wtf.autoNum.SalesReturn && (Wtf.templateflag== Wtf.Guan_Chong_templateflag || Wtf.templateflag == Wtf.Guan_ChongBF_templateflag) &&  recData.isNoteAlso == true) {//Guan Chong Purchase  Return credit note
                    var url="ACCInvoiceCMN/exportSalesReturnJasper.do? &mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;                                
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype0"){
                        url+="&type=0";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype1"){
                        url+="&type=1";
                    }
                    Wtf.get('downloadframe').dom.src =url; 
                }
                else if(get == Wtf.autoNum.SalesReturn && (Wtf.templateflag==Wtf.Tony_FiberGlass_templateflag || Wtf.templateflag==Wtf.BuildMate_templateflag ||  recData.isNoteAlso == true)) {//Buildmate Sales Return and also default sales return with note & Tonyfiber Sales Return refer ticket ERP-10859
                    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportSalesReturnJasper.do? &mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;                                    
                }
                else if(get == Wtf.autoNum.SalesReturn && Wtf.templateflag==Wtf.Diamond_Aviation_templateflag){
                    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportSalesReturnJasper.do? &mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;                                    
                }
                else if(get == Wtf.autoNum.PurchaseReturn && Wtf.templateflag==Wtf.Diamond_Aviation_templateflag){
                    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportPurchaseReturn.do? &mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;                                    
                }
//                else if(get == Wtf.autoNum.CreditNote && Wtf.templateflag==Wtf.Diamond_Aviation_templateflag && recData.isReturnNote == true){
//                    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportSalesReturnJasper.do? &mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds +"&isNote=true";                                    
//                }
//                else if(get == Wtf.autoNum.DebitNote && Wtf.templateflag==Wtf.Diamond_Aviation_templateflag && recData.isReturnNote == true){
//                    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportPurchaseReturn.do? &mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds +"&isNote=true";                                    
//                }
                else if(get == Wtf.autoNum.Invoice && (Wtf.templateflag == Wtf.Tony_FiberGlass_templateflag || Wtf.templateflag == Wtf.Swatow_templateflag || Wtf.templateflag == Wtf.BakerTilly_templateflag_pcs || Wtf.templateflag == Wtf.Swatow_templateflag || Wtf.templateflag == Wtf.Amcoweld_templateflag)){
                    var url="ACCInvoiceCMN/exportCustomerInvoice.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds+"&templateflag="+Wtf.templateflag+"&pendingapproval="+pendingapproval;//ERP-12065
                    var array = this.templateId.split('_');//splited on '_' as it contains templateflag and type value
                    url += "&type="+array[1];
                    Wtf.get('downloadframe').dom.src = url ;
                }else if(get == Wtf.autoNum.Invoice && (Wtf.templateflag == Wtf.FastenEnterprises_templateflag || Wtf.templateflag == Wtf.FastenHardwareEngineering_templateflag)){
                    var url="ACCInvoiceCMN/exportCustomerInvoiceForFasten.do";
                    var params = "moduleid=2&mode=" + mode + "&rec=" + selRec +"&isLeaseFixedAsset="+recData.fixedAssetLeaseInvoice+"&isFixedAsset="+recData.fixedAssetInvoice + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds+"&templateflag="+Wtf.templateflag;
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype7"){
                        params+="&type=1"; // AR Repair Invoice
                    }else{
                        params+="&type=0"; // Customer Invoice Default Template
                    }
                   this.postData(url,params);
                } else if(get == Wtf.autoNum.Invoice && (Wtf.templateflag == Wtf.Armada_Rock_Karunia_Transhipment_templateflag)){
                    var url="ACCInvoiceCMN/exportARKCustomerInvoice.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds+"&templateflag="+Wtf.templateflag;
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype0"){
                        url+="&type=0";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype1"){
                        url+="&type=1";
                    }
                    Wtf.get('downloadframe').dom.src = url ;
                } else if(get == Wtf.autoNum.Invoice && (Wtf.templateflag >= Wtf.LandPlus_templateflag && Wtf.templateflag <= Wtf.LandPlus_Mobility_templateflag)){
                    var url="ACCInvoiceCMN/exportLANDPLUSCustomerInvoice.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds+"&templateflag="+Wtf.templateflag;
                    var array = this.templateId.split('_');//splited on '_' as it contains templateflag and type value
                    url += "&type="+array[1];
                    Wtf.get('downloadframe').dom.src = url ;
                } else if(get == Wtf.autoNum.DeliveryOrder && (Wtf.templateflag == Wtf.Tony_FiberGlass_templateflag || Wtf.templateflag == Wtf.Amcoweld_templateflag)){
                    var url="ACCInvoiceCMN/exportDefaultDeliveryOrder.do?moduleid=27&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds+"&templateflag="+Wtf.templateflag;
                    Wtf.get('downloadframe').dom.src = url ;
                }else if(get == Wtf.autoNum.PurchaseOrder && (Wtf.templateflag == Wtf.FastenEnterprises_templateflag || Wtf.templateflag == Wtf.FastenHardwareEngineering_templateflag)){
                    var url="ACCInvoiceCMN/exportDeliveryOrderForFasten.do"
                    var params="moduleid=18&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds+"&templateflag="+Wtf.templateflag+"&pendingapproval="+pendingapproval+"&isFixedAsset="+obj.isFixedAsset;//ERP-12065
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype0"){
                        params+="&type=0";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype1"){
                        params+="&type=1";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype2"){
                        params+="&type=2";
                    }
                    this.postData(url,params);
                }else if(get == Wtf.autoNum.DeliveryOrder && (Wtf.templateflag == Wtf.FastenEnterprises_templateflag || Wtf.templateflag == Wtf.FastenHardwareEngineering_templateflag)){
                    var url="ACCInvoiceCMN/exportDeliveryOrderForFasten.do"
                    var params="moduleid=27&mode=" + mode + "&rec=" + selRec+"&isLeaseFixedAsset="+recData.isLeaseFixedAsset+"&isFixedAsset="+recData.isFixedAsset + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds+"&templateflag="+Wtf.templateflag;
                   this.postData(url,params);
                }else if(get == Wtf.autoNum.SalesOrder && (Wtf.templateflag == Wtf.FastenEnterprises_templateflag || Wtf.templateflag == Wtf.FastenHardwareEngineering_templateflag)){
                    var url="ACCInvoiceCMN/exportDeliveryOrderForFasten.do";
                    var params = "moduleid=20&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag;
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"SOType1"){
                        params+="&type=1"; // Pr-Printed
                    }else{
                        params+="&type=0"; // Default Sales Order
                    }
                    this.postData(url,params);
                }else if(get == Wtf.autoNum.SalesOrder && (Wtf.templateflag == Wtf.hinsitsu_templateflag)){
                    var url="ACCInvoiceCMN/exportSalesOrderForHINSITSU.do";
                    var params = "moduleid=20&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag;
                    this.postData(url,params);
                }else if(get == Wtf.autoNum.Venquotation && (Wtf.templateflag == Wtf.Tony_FiberGlass_templateflag || Wtf.templateflag == Wtf.Diamond_Aviation_templateflag )){ 
                    var url="ACCPurchaseOrderCMN/exportVendorQuoataion.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds+"&templateflag="+Wtf.templateflag+"&pendingapproval="+pendingapproval;//ERP-12065
                    Wtf.get('downloadframe').dom.src = url ;
                }
                else if (get == Wtf.autoNum.Payment && Wtf.isNewPaymentStructure){
                    Wtf.get('downloadframe').dom.src = "ACCVendorPaymentCMN/exportDefaultPaymentVoucher.do?moduleid=14&mode=" + mode + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag +"&againstGLFlag=false"+"&templateflag=" + Wtf.templateflag +"&recordids=" + billIds+"&isLetterHead="+Wtf.account.companyAccountPref.defaultTemplateLogoFlag+"&isExportPayment="+isExportPayment;  
                }else if (get == Wtf.autoNum.Receipt && Wtf.isNewPaymentStructure){
                    Wtf.get('downloadframe').dom.src = "ACCVendorPaymentCMN/exportDefaultPaymentVoucher.do?moduleid=16&mode=" + mode + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag +"&againstGLFlag=false"+"&templateflag=" + Wtf.templateflag +"&recordids=" + billIds +"&isLetterHead="+Wtf.account.companyAccountPref.defaultTemplateLogoFlag+"&isExportPayment="+isExportPayment + "&onlyOpeningBalanceTransactionsFlag=" +obj.onlyOpeningBalanceTransactionsFlag; 
                }else if ( get == Wtf.autoNum.GoodsReceiptOrder &&(Wtf.templateflag == Wtf.F1Recreation_templateflag || Wtf.templateflag == Wtf.F1RecreationLeasing_templateflag)) {
                    Wtf.get('downloadframe').dom.src = "ACCGoodsReceiptCMN/exportGoodsReceiptOrderJasperReport.do? &mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;                  
                }else if ( get == Wtf.autoNum.GoodsReceiptOrder &&Wtf.templateflag == Wtf.Diamond_Aviation_templateflag) {
                    var url = "ACCGoodsReceiptCMN/exportDiamondAviationGoodsReceiptOrderJasperReport.do? &mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds + "&dtype=report";                  
                    if(this.templateId==Wtf.Acc_Basic_Template_Id+"GRtype0") {
                        Wtf.get('downloadframe').dom.src = url +"&type=0";
                    }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"GRtype1") {
                        Wtf.get('downloadframe').dom.src = url +"&type=1";
                    } else if(this.templateId==Wtf.Acc_Basic_Template_Id+"GRtype2") {
                        Wtf.get('downloadframe').dom.src = url +"&type=2";
                    }
                }else if ((get == Wtf.autoNum.CreditNote || get == Wtf.autoNum.DebitNote) && !(Wtf.templateflag == Wtf.F1Recreation_templateflag||Wtf.templateflag == Wtf.F1RecreationLeasing_templateflag||Wtf.templateflag == Wtf.Tony_FiberGlass_templateflag ||Wtf.templateflag==Wtf.FastenEnterprises_templateflag||Wtf.templateflag==Wtf.FastenHardwareEngineering_templateflag )) {	//As per discussion with Mayur B. added templateflag check
                    Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportDefaultFormatCreditNoteJasper.do?moduleid=12&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds ;
                }else if ((get == Wtf.autoNum.Invoice || get == Wtf.autoNum.GoodsReceipt) && (this.templateId==Wtf.Acc_Basic_Template_Id+"form201ABC" || this.templateId==Wtf.Acc_Basic_Template_Id+"form202A" || this.templateId==Wtf.Acc_Basic_Template_Id+"vatreturnformGujarat" || this.templateId==Wtf.Acc_Basic_Template_Id+"cstreturnformGujarat" || this.templateId==Wtf.Acc_Basic_Template_Id+"SupplementaryInvoice") && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA ) {//For Indian Companys only
                    var isForm202 = false;
                    if(this.templateId!=Wtf.Acc_Basic_Template_Id+"form201ABC"){
                        if(this.templateId==Wtf.Acc_Basic_Template_Id+"form202A"){
                            isForm202 = true;
                        }
                        var startDate = "";
                        var endDate = "";
                        if(obj.endDate!=undefined && obj.startDate && obj.startDate.getValue()){
                            startDate = obj.startDate.getValue().format('Y-m-d');
                        }
                        if(obj.endDate!=undefined && obj.endDate.getValue()){
                            endDate = obj.endDate.getValue().format('Y-m-d');
                        }
                        if((this.templateId==Wtf.Acc_Basic_Template_Id+"SupplementaryInvoice")){
                            if(recData.issupplementary==1){
                                Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportSupplementaryInvoiceJasper.do?moduleid="+get+"&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&recordids=" + billIds;
                            }else{
                                Wtf.MessageBox.show({
                                    title: WtfGlobal.getLocaleText("acc.common.block"),
                                    msg: WtfGlobal.getLocaleText("acc.field.supplementaryInvoiceMessage"), 
                                    buttons: Wtf.MessageBox.OKCANCEL,
                                    animEl: 'upbtn',
                                    icon: Wtf.MessageBox.INFO
                                });
                            }
                        }else{
                            if(obj.endDate!=undefined && obj.startDate!=undefined){
                                Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportForm21AInvoiceJasper.do?moduleid="+get+"&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag+"&isform202="+isForm202+"&recordids=" + billIds+"&startdate=" + startDate+"&enddate=" + endDate;
                            }else{
                                Wtf.MessageBox.show({
                                    title: WtfGlobal.getLocaleText("acc.common.block"),
                                    msg: WtfGlobal.getLocaleText("acc.field.form201ABMessage"), 
                                    buttons: Wtf.MessageBox.OKCANCEL,
                                    animEl: 'upbtn',
                                    icon: Wtf.MessageBox.INFO
                                });                        
                            }
                        }
                    }
                }else if (get == Wtf.autoNum.DeliveryOrder && (this.templateId==Wtf.Acc_Basic_Template_Id+"form33") && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA ) {
                    if(billIds.length==1){
                        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportForm33Jasper.do?deliveryOrderId="+billIds;
                    }else{
                        Wtf.MessageBox.show({
                            title: WtfGlobal.getLocaleText("acc.common.block"),
                            msg: WtfGlobal.getLocaleText("acc.field.form33msg"), 
                            buttons: Wtf.MessageBox.OKCANCEL,
                            animEl: 'upbtn',
                            icon: Wtf.MessageBox.INFO
                        });
                    }
                }else if (get == Wtf.autoNum.PurchaseOrder && (this.templateId==Wtf.Acc_Basic_Template_Id+"form45") && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA ) {
                    if(billIds.length==1){
                        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportExciseFormERJasper.do?report=Form45&filetype=pdf&purchaseOrderId="+billIds;
                    }else{
                        Wtf.MessageBox.show({
                            title: WtfGlobal.getLocaleText("acc.common.block"),
                            msg: WtfGlobal.getLocaleText("acc.field.form45msg"), 
                            buttons: Wtf.MessageBox.OKCANCEL,
                            animEl: 'upbtn',
                            icon: Wtf.MessageBox.INFO
                        });
                    }
                }else if (get == Wtf.autoNum.PurchaseOrder && (this.templateId==Wtf.Acc_Basic_Template_Id+"ruleno11ForPO") && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA ) {
                    if(billIds.length==1){
                        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportRuleNo11Jasper.do?purchaseOrderId="+billIds;
                    }else{
                        Wtf.MessageBox.show({
                            title: WtfGlobal.getLocaleText("acc.common.block"),
                            msg: WtfGlobal.getLocaleText("acc.field.ruleNo11.msg"), 
                            buttons: Wtf.MessageBox.OKCANCEL,
                            animEl: 'upbtn',
                            icon: Wtf.MessageBox.INFO
                        });
                    }
                }else if ((get == Wtf.autoNum.Invoice || get == Wtf.autoNum.GoodsReceipt) && (this.templateId==Wtf.Acc_Basic_Template_Id+"ruleno11") && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA ) {//For Indian Companys only
                    if(billIds.length==1){
                        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportRuleNo11Jasper.do?invoiceid="+billIds;
                    }else{
                        Wtf.MessageBox.show({
                            title: WtfGlobal.getLocaleText("acc.common.block"),
                            msg: WtfGlobal.getLocaleText("acc.field.ruleNo11.msg"), 
                            buttons: Wtf.MessageBox.OKCANCEL,
                            animEl: 'upbtn',
                            icon: Wtf.MessageBox.INFO
                        });
                    }
                }else if (get == Wtf.autoNum.PurchaseOrder && (this.templateId==Wtf.Acc_Basic_Template_Id+"ruleno11ForPO_dealer") && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA ) {
                    if(billIds.length==1){
                        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportRuleNo11Jasper.do?purchaseOrderId="+billIds+"&type=dealer";
                    }else{
                        Wtf.MessageBox.show({
                            title: WtfGlobal.getLocaleText("acc.common.block"),
                            msg: WtfGlobal.getLocaleText("acc.field.ruleNo11.msg"), 
                            buttons: Wtf.MessageBox.OKCANCEL,
                            animEl: 'upbtn',
                            icon: Wtf.MessageBox.INFO
                        });
                    }
                }else if ((get == Wtf.autoNum.Invoice || get == Wtf.autoNum.GoodsReceipt) && (this.templateId==Wtf.Acc_Basic_Template_Id+"ruleno11_dealer") && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA ) {//For Indian Companys only
                    if(billIds.length==1){
                        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportRuleNo11Jasper.do?invoiceid="+billIds+"&type=dealer";
                    }else{
                        Wtf.MessageBox.show({
                            title: WtfGlobal.getLocaleText("acc.common.block"),
                            msg: WtfGlobal.getLocaleText("acc.field.ruleNo11.msg"), 
                            buttons: Wtf.MessageBox.OKCANCEL,
                            animEl: 'upbtn',
                            icon: Wtf.MessageBox.INFO
                        });
                    }
                }else if ((get == Wtf.autoNum.Invoice || get == Wtf.autoNum.GoodsReceipt) && (this.templateId==Wtf.Acc_Basic_Template_Id+"commercialinvoice") && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA ) {//For Indian Companys only
                    if(billIds.length==1){
                        Wtf.get('downloadframe').dom.src = "ACCInvoiceCMN/exportCommercialInvoiceJasper.do?invoiceid="+billIds;
                    }else{
                        Wtf.MessageBox.show({
                            title: WtfGlobal.getLocaleText("acc.common.block"),
                            msg: WtfGlobal.getLocaleText("acc.field.commercialinvoice.msg"), 
                            buttons: Wtf.MessageBox.OKCANCEL,
                            animEl: 'upbtn',
                            icon: Wtf.MessageBox.INFO
                        });
                    }
                }else if (get == Wtf.autoNum.GoodsReceipt || get == Wtf.autoNum.Payment || get == Wtf.autoNum.Receipt || get == Wtf.autoNum.Invoice || get == Wtf.autoNum.BillingInvoice || get == Wtf.autoNum.BillingReceipt || get == Wtf.autoNum.BillingGoodsReceipt || get == Wtf.autoNum.BillingPayment || get == Wtf.autoNum.BillingSalesOrder || get == Wtf.autoNum.BillingPurchaseOrder || get == Wtf.autoNum.CreditNote || get == Wtf.autoNum.DebitNote
                    || get == Wtf.autoNum.SalesOrder || get == Wtf.autoNum.PurchaseOrder || get == Wtf.autoNum.BillingDebitNote || get == Wtf.autoNum.BillingCreditNote || get == Wtf.autoNum.Quotation || get == Wtf.autoNum.DeliveryOrder || get == Wtf.autoNum.GoodsReceiptOrder || get == Wtf.autoNum.Venquotation || get == Wtf.autoNum.SalesReturn || get == Wtf.autoNum.PurchaseReturn || get==Wtf.autoNum.CustomerQuoationVersion || get==Wtf.autoNum.VendorQuotationVersion) {
                    //               Wtf.get('downloadframe').dom.src = "ACCExportRecord/exportRecords.do?mode="+mode+"&rec="+selRec+"&personid="+recData.personid+"&filename="+Wtf.getCmp('as').getActiveTab().title+"&filetype=pdf";

                    if (rowPrint) {
                        var url = "ACCExportRecord/printRecords.do?mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds +"&templateflag="+Wtf.templateflag;						// File Name Changed     Neeraj
                        window.open(url, "mywindow", "menubar=1,resizable=1,scrollbars=1");
                    } else {
                        url="ACCExportRecord/exportRecords.do?mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds +"&templateflag="+Wtf.templateflag+"&isSelfBilledInvoice="+ recData.selfBilledInvoice+"&isexpenseinv="+recData.isexpenseinv;						// File Name Changed     Neeraj
                        if(this.templateId==Wtf.Acc_Basic_Template_Id+"Simplified"){
                            Wtf.get('downloadframe').dom.src = url +"&issimplifiedtaxinvoice=true";
                        }
                        else if(this.templateId==Wtf.Acc_Basic_Template_Id+"SimplifiedA7"){
                            Wtf.get('downloadframe').dom.src = url +"&issimplifiedtaxinvoicea7=true";
                        }
                        else if(this.templateId==Wtf.Acc_Basic_Template_Id+"SimplifiedA7WithTax"){
                            Wtf.get('downloadframe').dom.src = url +"&issimplifiedtaxinvoicea7withtax=true";
                        }
                        else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype0"){
                            Wtf.get('downloadframe').dom.src = url +"&type=0";
                        }else if(this.templateId==Wtf.Acc_Basic_Template_Id+"DAtype1"){
                            Wtf.get('downloadframe').dom.src = url +"&type=1";
                        } else {
                            Wtf.get('downloadframe').dom.src = url;
                        }
                    }
                }else if(get==Wtf.autoNum.Contract){
                    Wtf.get('downloadframe').dom.src = "ACCSalesOrderCMN/exportSalesContractreport.do?mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&templateflag="+Wtf.templateflag;
                }else if(this.templateId==Wtf.Acc_Packing_List_Lc_ModuleId ||this.templateId==Wtf.Acc_Packing_List_NonLc_ModuleId){
                    url="ACCInvoiceCMN/exportPackingList.do?moduleid=2&mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag+"&recordids=" + billIds+"&templateId="+this.templateId;
                    Wtf.get('downloadframe').dom.src = url ;
                } else {
                    //                Wtf.get('downloadframe').dom.src = "ACCExportInvoice/getExportInv.do?mode="+mode+"&rec="+selRec+"&personid="+recData.personid+"&filename="+Wtf.getCmp('as').getActiveTab().title+"&filetype=pdf";
                    Wtf.get('downloadframe').dom.src = "ACCExportInvoice/getExportInv.do?mode=" + mode + "&rec=" + selRec + "&personid=" + recData.personid + "&filename=" + fileName + "&filetype=pdf&contraentryflag=" + contraentryflag;
                }
            }
            this.templateId = undefined; 
        }
    },
    printRecord:function(url, params){
        if (this.filetype == 'print') {
            this.postData(url, params);
        } else {
            Wtf.get('downloadframe').dom.src = url;
        } 
    },   
    postData:function(url, params){
        var mapForm = document.createElement("form");
        mapForm.target = "mywindow";
        mapForm.method = "post"; 
        mapForm.action = url;
        var params = params;
        var inputs =params.split('&');
        for(var i=0;i<inputs.length;i++){
            var KV_pair = inputs[i].split('=');
            var mapInput = document.createElement("input");
            mapInput.type = "text";
            mapInput.name = KV_pair[0];
            mapInput.value = KV_pair[1];
            mapForm.appendChild(mapInput); 
        }
        document.body.appendChild(mapForm);
        mapForm.submit();
//        var myWindow = window.open(url+"?"+params, "mywindow","menubar=1,resizable=1,scrollbars=1");
//        var div =  myWindow.document.createElement("div");
//        div.innerHTML = "Loading, Please Wait...";
//        myWindow.document.body.appendChild(div);
        mapForm.remove();
    }
});

function addIndianTemplates(config,thisObj){
        
//    if( config.get==Wtf.autoNum.GoodsReceipt){
//        Wtf.menu.MenuMgr.get("exportmenu" + thisObj.id).add({                  
//            iconCls: 'pwnd exportpdf',
//            text: WtfGlobal.getLocaleText("acc.field.form202A"),
//            id: Wtf.Acc_Basic_Template_Id+"form202A"
//        })
//    };
    var count = 0;
    if( (Wtf.isExciseApplicable && Wtf.account.companyAccountPref.enablevatcst) || Wtf.isExciseApplicable ){
        if( (config.get==Wtf.autoNum.Invoice || config.get==Wtf.autoNum.GoodsReceipt)){
            if(config.moduleid!=6 && Wtf.account.companyAccountPref.registrationType == Wtf.registrationTypeValues.DEALER){
                count++;
                Wtf.menu.MenuMgr.get("exportmenu" + thisObj.id).add({                  
                    iconCls: 'pwnd exportpdf',
                    text:"Rule No 11 Invoice - Manufacturer",
                    id: Wtf.Acc_Basic_Template_Id+"ruleno11"
                });
            }
        }
        
        if( (config.get==Wtf.autoNum.Invoice || config.get==Wtf.autoNum.GoodsReceipt)){
            if(config.moduleid!=6 && Wtf.account.companyAccountPref.registrationType == Wtf.registrationTypeValues.DEALER){
                count++;
                Wtf.menu.MenuMgr.get("exportmenu" + thisObj.id).add({                  
                    iconCls: 'pwnd exportpdf',
                    text:"Rule No 11 Invoice - Dealer",
                    id: Wtf.Acc_Basic_Template_Id+"ruleno11_dealer"
                });
            }
        }
            
        if( (config.get==Wtf.autoNum.Invoice || config.get==Wtf.autoNum.GoodsReceipt)){
            if(config.moduleid!=6){
                count++;
                Wtf.menu.MenuMgr.get("exportmenu" + thisObj.id).add({                  
                    iconCls: 'pwnd exportpdf',
                    text:WtfGlobal.getLocaleText("acc.field.commercialinvoice"),
                    id: Wtf.Acc_Basic_Template_Id+"commercialinvoice"
                });
            }
        }
            
        if( config.get==Wtf.autoNum.Invoice){
            count++;
            Wtf.menu.MenuMgr.get("exportmenu" + thisObj.id).add({                  
                iconCls: 'pwnd exportpdf',
                text: WtfGlobal.getLocaleText("acc.field.supplementaryInvoice"),
                id: Wtf.Acc_Basic_Template_Id+"SupplementaryInvoice"
            });
        }
    }
    
    /*var subMenuBtnAnnexure = [];
    subMenuBtnAnnexure.push(new Wtf.Action({
        iconCls:'pwnd '+'exportpdf',
        text : config.get==Wtf.autoNum.Invoice? WtfGlobal.getLocaleText("acc.field.form201A"):WtfGlobal.getLocaleText("acc.field.form201B"),
        scope: thisObj,
        handler: function() {
            this.exportWithTemplate(thisObj.obj, "form201AB", thisObj.get, false)     
        }
    }));
    subMenuBtnAnnexure.push(new Wtf.Action({
        iconCls:'pwnd '+'exportpdf',
        text : WtfGlobal.getLocaleText("acc.field.form201C"),
        scope: thisObj,
        handler: function() {
            this.exportWithTemplate(thisObj.obj, "form201C", thisObj.get, false)     
        }
    }));

    if( (config.get==Wtf.autoNum.Invoice || config.get==Wtf.autoNum.GoodsReceipt) && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && (Wtf.account.companyAccountPref.stateid==Wtf.StateName.GUJARAT)){
        Wtf.menu.MenuMgr.get("exportmenu" + thisObj.id).add({                  
            iconCls: 'pwnd exportpdf',
            text: WtfGlobal.getLocaleText("acc.field.india.VATReportsAnnexures"),
            id: Wtf.Acc_Basic_Template_Id+"form201ABC",
            menu:subMenuBtnAnnexure
        })
    };*/
    
    if(config.get==Wtf.autoNum.DeliveryOrder && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && (Wtf.account.companyAccountPref.stateid==Wtf.StateName.DELHI)){
        count++;
        Wtf.menu.MenuMgr.get("exportmenu" + thisObj.id).add({                  
            iconCls: 'pwnd exportpdf',
            text: WtfGlobal.getLocaleText("acc.field.form33"),
            id: Wtf.Acc_Basic_Template_Id+"form33"
        });
    }
    
   /* if(config.get==Wtf.autoNum.PurchaseOrder && Wtf.account.companyAccountPref.countryid==Wtf.Country.INDIA && (Wtf.account.companyAccountPref.stateid==Wtf.StateName.MAHARASHTRA)){
        count++;
        Wtf.menu.MenuMgr.get("exportmenu" + thisObj.id).add({                  
            iconCls: 'pwnd exportpdf',
            text: WtfGlobal.getLocaleText("acc.field.india.exciseReportsForm45"),
            id: Wtf.Acc_Basic_Template_Id+"form45"
        });
    }
    if (config.get == Wtf.autoNum.PurchaseOrder && Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA ) {
        count++;
        Wtf.menu.MenuMgr.get("exportmenu" + thisObj.id).add({
            iconCls: 'pwnd exportpdf',
            text: WtfGlobal.getLocaleText("acc.field.ruleNo11"),
            id: Wtf.Acc_Basic_Template_Id + "ruleno11ForPO"
        });
    }
    if (config.get == Wtf.autoNum.PurchaseOrder && Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA ) {
        count++;
        Wtf.menu.MenuMgr.get("exportmenu" + thisObj.id).add({
            iconCls: 'pwnd exportpdf',
            text: "Rule No 11 Dealer Excise Invoice",
            id: Wtf.Acc_Basic_Template_Id + "ruleno11ForPO_dealer"
        });
    }*/
    if (config.get == Wtf.autoNum.jwproductsummary && Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA ) {
        count++;
        Wtf.menu.MenuMgr.get("exportmenu" + thisObj.id).add({
            iconCls: 'pwnd exportpdf',
            text: WtfGlobal.getLocaleText("acc.JWProductSummary.report"),
            id: Wtf.Acc_Basic_Template_Id + "JWPSR"
        });
    }
//    if(count == 0){
//        Wtf.menu.MenuMgr.get("exportmenu" + thisObj.id).add({                  
//            iconCls: 'pwnd exportpdf',
//            text:WtfGlobal.getLocaleText("acc.field.TherearenotemplatesinExportRecords"),
//            id: Wtf.No_Template_Id
//        });
//    }

}
