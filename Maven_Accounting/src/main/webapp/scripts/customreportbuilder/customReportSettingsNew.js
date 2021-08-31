/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.fieldType = {
    textField: 1,
    numberField: 2,
    dateField: 3,
    comboBox: 4,
    multiselect : 7,
    checkbox : 11,
    listBox: 12,
    textArea: 13
}

Ext.moduleCategoryType = {
    Notes: 'Notes',
    Purchase: 'Purchase',
    Reports: 'Reports',
    Sales: 'Sales'
}

Ext.Acc_Debit_Note_ModuleId = 10;
Ext.Acc_Credit_Note_ModuleId = 12;
Ext.Acc_Sales_Return_ModuleId = 29;
Ext.Acc_Invoice_ModuleId = 2;
Ext.Acc_Sales_Order_ModuleId = 20;
Ext.Acc_Customer_Quotation_ModuleId = 22;
Ext.Acc_Vendor_Invoice_ModuleId = 6;
Ext.Acc_Purchase_Order_ModuleId = 18;
Ext.Acc_Vendor_Quotation_ModuleId = 23;
Ext.Acc_Purchase_Requisition_ModuleId = 32;
Ext.Acc_Goods_Receipt_ModuleId = 28;
Ext.Acc_Delivery_Order_ModuleId = 27;
Ext.Acc_Make_Payment_ModuleId = 14;
Ext.Acc_Receive_Payment_ModuleId = 16;
Ext.Acc_Customer_ModuleId_uuid = "09508488-c1d2-102d-b048-001e58a64cb6";
Ext.account = {}
Ext.account.nature={
    Liability:0,
    Asset:1,
    Expences:2,
    Income:3
};

Ext.Notes_Line_Items ={
    Serial_Number: 'S.No.',
    Account: 'Account',
    Type: 'Type', 
    Tax_Percent: 'Tax Percent',
    Tax_Amount: 'Tax Amount',
    Amount: 'Amount',
    Description: 'Description',
    Invoice_Number: 'Invoice No',
    Creation_Date: 'Creation Date',
    Due_Date: 'Due Date',
    Linking_Date: 'Linking Date',
    Invoice_Amount: 'Invoice Amount',
    Amount_Due: 'Amount Due'
};

Ext.Report_List_Ids = {
    Sales_By_Product: 'Sales_By_Product'
};
// Handle session timeout on completion of ajax request
Ext.Ajax.on({
    requestcomplete: function(conn, response, options) {
        if (response != null && response.responseText != null) {
            var resObj = eval("(" + response.responseText + ")");
            if (resObj.msg == "timeout" && (resObj.success == false || resObj.success == "false")) {
                signOut("timeout");
            }
        }
    },
    requestexception: function(conn, response, options, e) {
        if(response.responseText.indexOf("error code [1116]") > -1) {
            Ext.CustomMsg('Information', ExtGlobal.getLocaleText("acc.common.errorDBJoinLimitDisplayMsg"), Ext.Msg.INFO);
        } else {
//            Ext.CustomMsg('Error', ExtGlobal.getLocaleText("acc.common.errorOccuratServerSide"), Ext.Msg.ERROR);
        }
    }
});

Ext.CustomMsg = function(title,msg,icon){
    Ext.Msg.show({
        title      : title,
        msg        : msg,
        buttons    : Ext.MessageBox.OK,
        icon       : icon
    });
}

Array.prototype.getIemtByParam = function(paramPair) {
    var key = Object.keys(paramPair)[0];
    return this.find(function(item){
        return ((item[key] == paramPair[key]) ? true: false)
    });
}

String.prototype.capitalizeFirstLetter = function() {
    return this.charAt(0).toUpperCase() + this.slice(1);
}

function _dC(n) {
    document.cookie = n + "=" + ";path=/;expires=Thu, 01-Jan-1970 00:00:01 GMT";
}

function _r(url) {
    window.top.location.href = url;
}
function signOut(type) {

    Ext.DomainPatt = /[ab]\/([^\/]*)\/(.*)/;
    var _out = "";
    if (type !== undefined && typeof type != "object")
        _out = "?type=" + type;
    _dC('lastlogin');
    _dC('featureaccess');
    _dC('username');
    _dC('lid');
    _dC('companyid');
    var m = Ext.DomainPatt.exec(window.location);
    var _u = '../../error.do';
    if (type == "noaccess" || type == "alreadyloggedin") {
        _u += '?e=' + type;
        if (m && m[1]) {
            _u += '&n=' + m[1];
        }
    }
    else {
        if (m && m[1]) {
            _u = '../../b/' + m[1] + '/signOut.do' + _out;
        }
    }
    _r(_u);

}

function noRecordMsg(billid,header,body,reportID,recordCount) {
    Ext.get('ux-report-row-expander-box-'+billid+"-"+reportID+"-"+recordCount).setHtml("<div style='width: 100%;min-width:1900px;margin-left:50px'><span class='gridHeader'>There are no records to display.</span></div>");//setting html in rowbodytpl
}

function  createColumns(jarray,moduleid) {
    var columns = [], i = 0;
    var globalColumnsCnt=0;
    
    for(i=0;i<jarray.length;i++){
        if (!jarray[i].showasrowexpander)
            globalColumnsCnt++; 
    }
    if(globalColumnsCnt == 0){
        Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.info"),ExtGlobal.getLocaleText("acc.common.selectAtleastoneGlobalColumn"), Ext.Msg.INFO);
    }else{
        columns.push({
            xtype: 'rownumberer',
            autoLock:false
        });
        for (i = 0; i < jarray.length; i++) {
            if(jarray[i].defaultHeader != "flatdiscount"){
                var flex = globalColumnsCnt > reportScrollMinColumn ? "":1,
                align = jarray[i].properties.source.align ? jarray[i].properties.source.align.toLowerCase():"",
                displayName = jarray[i].displayName,
                defaultHeader = jarray[i].defaultHeader,
                dataIndex = jarray[i].id,
                xtype = parseInt(jarray[i].xtype) == Ext.fieldType.numberField ? 'numbercolumn' : '',
                summaryType = jarray[i].summaryType != undefined ? jarray[i].summaryType.toLowerCase() : '',
                rendrer = ExtGlobal.getColumnRenderer(jarray[i]),
                summaryRendrer = ExtGlobal.getSummaryRenderer(jarray[i]),
                isMeasureItem = jarray[i].isMeasureItem != undefined ? jarray[i].isMeasureItem : false,
                filter = isMeasureItem ? "" : ExtGlobal.getFilterTypeforReport(jarray[i],moduleid),
                showasrowexpander = jarray[i].showasrowexpander,
                columnwidth = flex == 1 ? "":220,
                colnum = jarray[i].colnum,
                isgrouping = jarray[i].isgrouping != undefined ? jarray[i].isgrouping : false,
                columnid =jarray[i].id;
            
                if (!showasrowexpander  && displayName!="Discountispersent") {
                    columns.push({
                        text: displayName,
                        align :align,
                        dataIndex: dataIndex,
                        xtype: xtype,
                        width: columnwidth,
                        flex: flex,
                        summaryType: summaryType,
                        renderer: rendrer,
                        summaryRenderer: summaryRendrer,
                        filter: filter,
                        defaultHeader : defaultHeader,
                        sortable: false,
                        colnum: colnum,
                        isgrouping: isgrouping, //Added to fix ERP-35561
                        columnid : columnid //Added to fix ERP-35561
                    });
                }//end of lineitem  
            }
        }
    }
    return columns;
}

function  createPivotColumns(jarray,moduleid) {
    var columns = [], i = 0;
    
    for (i = 0; i < jarray.length; i++) {
        var displayName = jarray[i].displayName;
        var dataIndex = jarray[i].id;
        var xtype = parseInt(jarray[i].xtype);
        var rendrer = ExtGlobal.getColumnRenderer(jarray[i]);
        var rendererType = jarray[i].properties.source.renderer;
        var align = jarray[i].properties.source.align ? jarray[i].properties.source.align.toLowerCase():"";
        var sortIndex = (xtype == Ext.fieldType.dateField) ? "date_" + dataIndex : dataIndex;
        var defaultHeader = jarray[i].defaultHeader;
        var customfield = jarray[i].customfield;
        var properties = jarray[i].properties;
        var colnum = jarray[i].colnum;
        var isforformulabuilder = jarray[i].isforformulabuilder;
        
        columns.push({
            header : displayName,
            dataIndex : dataIndex,
            align : align,
            renderer : rendrer,
            rendererType : rendererType,
            xtype: xtype,
            properties: properties,
            customfield: customfield,
            defaultHeader: defaultHeader,
            colnum: colnum,
            isforformulabuilder: isforformulabuilder,
            sortIndex : sortIndex
        });
        if(xtype == Ext.fieldType.dateField){
            columns.push({
                header : "Year ("+displayName+")",
                align : align,
                dataIndex : "year_" + dataIndex,
                rendererType : "None",
                renderer : function(val, metaData, record){
                    return val;
                },
                sortIndex : sortIndex
            });
            columns.push({
                header : "Month ("+displayName+")",
                dataIndex : "month_" + dataIndex,
                align : align,
                rendererType : "None",
                renderer : function(val, metaData, record){
                    return val;
                },
                sortIndex : sortIndex
            });
            columns.push({
                header : "Day ("+displayName+")",
                align : align,
                dataIndex : "day_" + dataIndex,
                rendererType : "None",
                renderer : function(val, metaData, record){
                    return val;
                },
                sortIndex : sortIndex
            });
        }
    }
    return columns;
}

function expandCollapse(grid ,expandstore,scope) {
    var expander = grid.normalGrid.getPlugin('rowex');
    var store = grid.getStore();
    var billidArr = "";
    var linkedbillidArr = "";
    var recordCountArr = "";
    var isReportStoreRefresh = scope.isReportStoreRefresh ? scope.isReportStoreRefresh:false;
    
    var isExpand = false;
    if (scope.expandCollpseButton.getText() == ExtGlobal.getLocaleText("acc.field.Expand")) {
        isExpand = true;
    }
    var btnText = ExtGlobal.getLocaleText("acc.field.Expand");
    if(isExpand){
        if(isReportStoreRefresh){
            store.each(function(rec) {
                if (rec.data.billid != "" && rec.data.billid != undefined){
                    billidArr += rec.data.billid + ",";
                    recordCountArr += rec.data.recordCount + ",";
                }
                if (rec.data.linkedbillid != "" && rec.data.linkedbillid != undefined){
                    linkedbillidArr += rec.data.linkedbillid + ",";
                }
            }, scope);
            if (billidArr.length != 0) {
                billidArr = billidArr.substring(0, billidArr.length - 1);
                recordCountArr = recordCountArr.substring(0, recordCountArr.length - 1);
            }
            if (linkedbillidArr.length != 0) {
                linkedbillidArr = linkedbillidArr.substring(0, linkedbillidArr.length - 1);                
            }
            expandstore.load({
                params: {
                    reportID: scope.reportId,
                    deleted: false,
                    nondeleted: false,
                    pendingapproval: false,
                    showRowLevelFieldsflag: true,
                    billid: billidArr,
                    linkedbillid: linkedbillidArr,
                    recordCount: recordCountArr,
                    moduleid : scope.moduleid
                }
            });
            scope.isReportStoreRefresh = false;
        }else{
            expander.expandAll();
        }
        btnText = ExtGlobal.getLocaleText("acc.field.Collapse");
    }else{
        expander.collapseAll();
    }
    scope.expandCollpseButton.setText(btnText);
}

function getGroupingFields(columns) {
    var groupFields = "", i = 0;
    for (i = 0; i < columns.length; i++) {
        if (columns[i].isgrouping) {
            groupFields = columns[i].columnid //Changed from id to columnid to fix ERP-35561
        }
    }
    return groupFields;
}

/*Setting Row EXpander Html for it*/
function setRowExpanderHTML(columns, records , gridHeader,moduleid) {
    var arr = [];
    this.serialNumber = 0;
    var arrayLength = columns.length + 1;
    var width=(arrayLength * 100)+ 250;
    var widthInPercent = (100 / arrayLength);

    /*Header Section*/
    var gridHeaderText = ExtGlobal.getLocaleText("acc.common.ProductList");
    if(moduleid == Ext.Acc_Make_Payment_ModuleId || moduleid == Ext.Acc_Receive_Payment_ModuleId ||  moduleid == Ext.Acc_Debit_Note_ModuleId  ||  moduleid == Ext.Acc_Credit_Note_ModuleId ){
        gridHeaderText = gridHeader;
    }
    var header = "<span class='gridHeader'>" + gridHeaderText + "</span>";   //Product List
    header += "<div style='display:table !important;width:" + width + "px'>";
    header += "<span class='gridNo' style='font-weight:bold;'>S.No.&nbsp;&nbsp;&nbsp;</span>";

    for (var i = 0; i < columns.length; i++) {
        if(columns[i].defaultHeader!="discountispercent" && columns[i].defaultHeader!="flatdiscount"){
            var alignTo = columns[i].properties.source.align ? columns[i].properties.source.align.toLowerCase():"left";
            header += "<span class='headerRow' style='width:"+widthInPercent+"%;text-align:"+alignTo+";'>" + columns[i].displayName + "&nbsp;</span>";
            arr.push(columns[i].id);
        }
    }
    header += "</div>";

    //Values Section
    header += "<div style='width:"+width+"px;'><span class='gridLine'></span></div>";
    for (var i = 0; i < records.length; i++) {
        header += " <div style='width:"+width+"px;display:table !important;'>";
        header += "<span class='gridNo'>" + (++this.serialNumber) + ".</span>";
        if(moduleid == Ext.Acc_Make_Payment_ModuleId || moduleid == Ext.Acc_Receive_Payment_ModuleId ||  moduleid == Ext.Acc_Debit_Note_ModuleId  ||  moduleid == Ext.Acc_Credit_Note_ModuleId ){
            var recordData = records[i];
        }else{
            recordData = records[i].data;
        }
        for (var j = 0; j < arr.length; j++) {
            var recordvalue = recordData[arr[j]];
            var recordColumn = columns.getIemtByParam({
                id: arr[j]
                });
            if (recordvalue == undefined || recordvalue === '') {
                recordvalue = '';
            }
            if (recordColumn.xtype == Ext.fieldType.numberField) {
                recordvalue = Ext.util.Format.number(recordvalue, ExtGlobal.getColumnRendererFormat(recordColumn));
                
                var rendererProperty = recordColumn.properties.source.renderer;
                if((recordData.discountispercent=="1" || recordData.discountispercent=="T") && recordColumn.defaultHeader=="Discount"){
                    recordvalue = recordvalue + "%" ;
                }else if(rendererProperty== "Transaction Currency" && recordvalue!=""){     // Transaction level currency renderer
                    var currency = recordData.currencysymbol;
                    var CurrencySymbolWithColumnId=recordData['currencysymbol_'+recordColumn.id.replace(/-/gi,'')];
                    CurrencySymbolWithColumnId =  ((CurrencySymbolWithColumnId != undefined) ? CurrencySymbolWithColumnId : currency);
                    recordvalue= CurrencySymbolWithColumnId != undefined ?CurrencySymbolWithColumnId+ " "+ recordvalue: recordvalue;
                }else if(rendererProperty== "Base Currency" && recordvalue!=""){     // Base currency renderer
                    recordvalue = Ext.pref.CurrencySymbol + " " + recordvalue;
                }else if(recordColumn.defaultHeader== "Quantity" || recordColumn.defaultHeader== "Base Quantity" ||
                         recordColumn.defaultHeader== "Balance Quantity" || recordColumn.defaultHeader== "Actual Quantity" || 
                         recordColumn.defaultHeader== "Received Quantity" || recordColumn.defaultHeader== "Return Quantity" ||
                         recordColumn.defaultHeader== "Delivered Quantity" || recordColumn.defaultHeader== "Base UOM Quantity"){
                    if(recordvalue !="" && recordvalue!=undefined){
                        recordvalue = recordvalue+" "+(recordData.displayUOM!=undefined?recordData.displayUOM:"");
                    }
                }else if(recordColumn.defaultHeader== "Display UOM"){
                    if(recordvalue !="" && recordvalue!=undefined){
                        recordvalue = recordvalue+" "+(recordData.displayUOMMapping!=undefined?recordData.displayUOMMapping:"");
                    }
                }
            }
            alignTo = recordColumn.properties.source.align ? recordColumn.properties.source.align.toLowerCase():"left";
//            header += "<span class=\"gridRow\" style=\"width:" + widthInPercent + "%;text-align:"+alignTo+";\" data-qtip =\""+recordvalue+"\">" + Ext.util.Format.ellipsis(recordvalue.replace(/(<([^>]+)>)/ig,''),30) + "&nbsp;</span>";
            header += "<span class='gridRow' style='text-overflow: ellipsis;overflow: hidden;width:" + widthInPercent + "%;text-align:"+alignTo+";' data-qtip ='"+recordvalue.replace(/'/g,"")+"'>" + Ext.util.Format.ellipsis(recordvalue.replace(/(<([^>]+)>)/ig,''),30) + "&nbsp;</span>";
        }
        header += "</div>";
    }
    var disHtml = "<div class='expanderContainer'>" + header + "</div>";
    return disHtml;
}

function setRowExpanderForNotesHTML(columns, rec,resObj) {
    var arr = [];
    var header="<div style='width: 100%;min-width:1900px;margin-left:50px'><span class='gridHeader'>There are no records to display.</span></div>";
    this.serialNumber = 0;

    //var accColumns = resObj.dataIndexObject.accountDataIndexObj;
    //var invoiceColumns = resObj.dataIndexObject.invoiceDataIndexobj;
    var accColumns = rec[0].data.accountColumns;
    var invoiceColumns = rec[0].data.invoiceColumns;
    //var selectedRowsDHList = resObj.data[1];
    //var selectedRowsCustomDHList = resObj.data[2];
    //console.log(Object.keys(accColumns).length);
    //console.log(Object.keys(invoiceColumns).length);

    /*Account Header Section*/
    var accheader = '';
    var accrecords = rec[0].data.accountDetails;
      if (accrecords !== undefined) {
    if (accColumns !== undefined && accColumns.length > 0) {
        var accarrayLength = accColumns.length + 1;
        var accwidth = (accarrayLength * 100) + 250;
        var accwidthInPercent = (100 / accarrayLength);
        var accgridHeaderText = ExtGlobal.getLocaleText("acc.field.accountDetails");
        accheader = "<span class='gridHeader'>" + accgridHeaderText + "</span>";   //Account Details
        accheader += "<div style='display:table !important;width:" + accwidth + "px'>";
        accheader += "<span class='gridNo' style='font-weight:bold;'>S.No.&nbsp;&nbsp;&nbsp;</span>";
        //for (var key in accColumns) {
        for (var j = 0; j < accColumns.length; j++) {
            //if (accColumns.hasOwnProperty(key)) {
                //var val = accColumns[key];
                //var val = accColumns[j].id;
                var val = accColumns[j];
                var accrecordColumn = columns.getIemtByParam({
                    id: val
                });
                if (accrecordColumn !== undefined && accrecordColumn.defaultHeader !== "discountispercent" && accrecordColumn.defaultHeader !== "flatdiscount") {
                    var alignTo = accrecordColumn.properties.source.align ? accrecordColumn.properties.source.align.toLowerCase() : "left";
                    accheader += "<span class='headerRow' style='width:" + accwidthInPercent + "%;text-align:" + alignTo + ";'>" + accrecordColumn.displayName + "&nbsp;</span>";
                    var isCustom = accrecordColumn.customfield != undefined ? accrecordColumn.customfield : accrecordColumn.custom;
                    if( typeof isCustom === 'string'){
                        isCustom = (isCustom  === "true");
                    }
                    arr.push(isCustom ? "Custom_" + accrecordColumn.defaultHeader : accrecordColumn.defaultHeader);
                }
            //}
        }

        accheader += "</div>";

        /*Account Data Section*/
        
        accheader += "<div style='width:" + accwidth + "px;'><span class='gridLine'></span></div>";
        if (accrecords !== undefined) {
        for (var i = 0; i < accrecords.length; i++) {
            accheader += " <div style='width:" + accwidth + "px;display:table !important;'>";
            accheader += "<span class='gridNo'>" + (++this.serialNumber) + ".</span>";
            var recordData = accrecords[i];
            for (var j = 0; j < arr.length; j++) {
                var recordvalue = recordData[arr[j]];
                var recordColumn = columns.getIemtByParam({
                    defaultHeader: arr[j].replace("Custom_", "")
                });
                if (recordvalue == undefined || recordvalue === '') {
                    recordvalue = '';
                }
                if (recordColumn.xtype == Ext.fieldType.numberField) {
                    recordvalue = Ext.util.Format.number(recordvalue, ExtGlobal.getColumnRendererFormat(recordColumn));

                    var rendererProperty = recordColumn.properties.source.renderer;
                    if ((recordData.discountispercent == "1" || recordData.discountispercent == "T") && recordColumn.defaultHeader == "Discount") {
                        recordvalue = recordvalue + "%";
                    } else if (rendererProperty == "Transaction Currency" && recordvalue != "") {     // Transaction level currency renderer
                        recordvalue = recordData.currencysymbol + " " + recordvalue
                    } else if (rendererProperty == "Base Currency" && recordvalue != "") {     // Base currency renderer
                        recordvalue = Ext.pref.CurrencySymbol + " " + recordvalue;
                    }
                }
                alignTo = recordColumn.properties.source.align ? recordColumn.properties.source.align.toLowerCase() : "left";
                accheader += "<span class='gridRow' style='width:" + accwidthInPercent + "%;text-align:" + alignTo + ";' data-qtip ='" + recordvalue + "'>" + Ext.util.Format.ellipsis(recordvalue.replace(/(<([^>]+)>)/ig, ''), 30) + "&nbsp;</span>";
            }
            accheader += "</div>";
        }
    }

    }
      }

    /*Invoice Header Section*/
    
    arr = [];
    var invheader = '';
    var invrecords = (rec[0].data.invoiceDetails!==null?rec[0].data.invoiceDetails:null);
    if (invrecords !== undefined) {
    if (invoiceColumns !== undefined && Object.keys(invoiceColumns).length > 0) {
        var invarrayLength = Object.keys(invoiceColumns).length + 1;
        var invcwidth = (invarrayLength * 100) + 250;
        var invwidthInPercent = (100 / invarrayLength);
        var invgridHeaderText = ExtGlobal.getLocaleText("acc.field.InvoiceDetails");
        invheader = "<span class='gridHeader'>" + invgridHeaderText + "</span>";   //Account Details
        invheader += "<div style='display:table !important;width:" + invcwidth + "px'>";
        invheader += "<span class='gridNo' style='font-weight:bold;'>S.No.&nbsp;&nbsp;&nbsp;</span>";
        for (var j = 0; j < invoiceColumns.length; j++) {
            //for (var key in invoiceColumns) {
            //if (invoiceColumns.hasOwnProperty(key)) {
            //var val = invoiceColumns[j].id;
            var val = invoiceColumns[j];
            var invrecordColumn = columns.getIemtByParam({
                id: val
            });
            if (invrecordColumn.defaultHeader !== "discountispercent" && invrecordColumn.defaultHeader !== "flatdiscount") {
                var alignTo = invrecordColumn.properties.source.align ? invrecordColumn.properties.source.align.toLowerCase() : "left";
                invheader += "<span class='headerRow' style='width:" + invwidthInPercent + "%;text-align:" + alignTo + ";'>" + invrecordColumn.displayName + "&nbsp;</span>";
                arr.push(invrecordColumn.defaultHeader);
            }
            // }
            //}
        }
    

        invheader += "</div>";

        this.serialNumber = 0;

        /*Invoice Data Section*/
        invheader += "<div style='width:" + invcwidth + "px;'><span class='gridLine'></span></div>";
        if (invrecords !== undefined) {
            for (var i = 0; i < invrecords.length; i++) {
                invheader += " <div style='width:" + invcwidth + "px;display:table !important;'>";
                invheader += "<span class='gridNo'>" + (++this.serialNumber) + ".</span>";
                var recordData = invrecords[i];
                for (var j = 0; j < arr.length; j++) {
                    var recordvalue = recordData[arr[j]];
                    var recordColumn = columns.getIemtByParam({
                        defaultHeader: arr[j]
                    });
                    if (recordvalue == undefined || recordvalue == '') {
                        recordvalue = '';
                    }
                    if (recordColumn.xtype == Ext.fieldType.numberField) {
                        recordvalue = Ext.util.Format.number(recordvalue, ExtGlobal.getColumnRendererFormat(recordColumn));

                        var rendererProperty = recordColumn.properties.source.renderer;
                        if ((recordData.discountispercent == "1" || recordData.discountispercent == "T") && recordColumn.defaultHeader == "Discount") {
                            recordvalue = recordvalue + "%";
                        } else if (rendererProperty == "Transaction Currency" && recordvalue != "") {     // Transaction level currency renderer
                            recordvalue = recordData.currencysymbol + " " + recordvalue
                        } else if (rendererProperty == "Base Currency" && recordvalue != "") {     // Base currency renderer
                            recordvalue = Ext.pref.CurrencySymbol + " " + recordvalue;
                        }
                    }
                    alignTo = recordColumn.properties.source.align ? recordColumn.properties.source.align.toLowerCase() : "left";
                    invheader += "<span class='gridRow' style='width:" + invwidthInPercent + "%;text-align:" + alignTo + ";' data-qtip ='" + recordvalue + "'>" + Ext.util.Format.ellipsis(recordvalue.replace(/(<([^>]+)>)/ig, ''), 30) + "&nbsp;</span>";
                }
                invheader += "</div>";
            }
        }
        }

    }
     if (accrecords == undefined && invrecords == undefined) {
         header="<div style='width: 100%;min-width:1900px;margin-left:50px'><span class='gridHeader'>There are no records to display.</span></div>";
     } else{
         header = "<div class='expanderContainer'>" + accheader + "</div>" + "<div class='expanderContainer'>" + invheader + "</div>";
     }
    var disHtml = "<div class='expanderContainer'>" + header + "</div>";
    return disHtml;
}

function getRecordsforExpander(isrowexpander,recordsToDrop) {
    var elements = [];
    if (isrowexpander) {
        for (var i = 0; i < recordsToDrop.length; i++) {
            var data = recordsToDrop[i];
            if (data.showasrowexpander) {
                elements.push(data);
            }
        }
    } else {
        for (var i = 0; i < recordsToDrop.length; i++) {
            var data = recordsToDrop[i];
            if (!data.showasrowexpander) {
                elements.push(data);
            }
        }
    }
    return elements;
}

function createNewReportTab(id, title, reportid ,moduleid,isreportloaded, isPivot,isDefault, reportName, reportDescription, moduleCategory, reportUrl, parentreportid,isEditMode, record, params) {
    var mainTabPanel = Ext.getCmp('mainTabPanel');
    var isTabExists = mainTabPanel.getChildByElement(id);
        var newTab;
        if (id == "idnewcustomreporttab") {
            newTab = Ext.create('ReportBuilder.view.CreateNewReport', {
                id: id + reportName.replace(/\s/g, '_'),
                title: reportName,
                tooltip: ExtGlobal.getLocaleText("acc.common.createnewCustomReport"),
                closable: true,
                isPivot:isPivot,
                reportNo:"",
                reportName:reportName,
                reportDescription:reportDescription,
                recordsToDrop: [],
                iconCls: 'accountingbase create-report',
                isEWayReport : params.isEWayReport
            });
        } else if(isPivot != undefined && isPivot === "T") {
            newTab = Ext.create('ReportBuilder.view.PivotReport', {
                id: id,
                title: title,
                tooltip: title,
                closable: true,
                reportId: reportid,
                moduleid: moduleid,
                reportRec : record,
                moduleCategory: moduleCategory,
                parentReportId: parentreportid,
                reportUrl: reportUrl,
                params: undefined,      //request params for reportUrl request
                iconCls: 'accountingbase new-report',
                isreportloaded:isreportloaded, //to set true when loading first
                isEWayReport : params.isEWayReport
            });
        } else {
            newTab = Ext.create('ReportBuilder.view.Report', {
                id: id,
                title: title,
                tooltip: title,
                closable: true,
                reportId: reportid,
                moduleid: moduleid,
                moduleCategory: moduleCategory,
                parentReportId: parentreportid,
                reportUrl: reportUrl,
                reportRec : record,
                params: undefined,      //request params for reportUrl request
                isDefault : isDefault,
                iconCls: 'accountingbase new-report',
                isreportloaded:isreportloaded,
                isEWayReport : params.isEWayReport//to set true when loading first
            });
        }
        mainTabPanel.add(newTab).show();
//    }
}

function getTopHtmlReqField(text, body,img,para){
    if(img===undefined || img=='') {
        img = '../../images/createuser.png';
    }
    
    var altImg='../../images/createuser.png';
    var str =  "<div style = 'width:100%;height:100%;position:relative;float:left;'>"
    +"<div style='float:left;height:100%;width:auto;position:relative;'>"
    +"<img src = "+img+" onerror = 'src="+altImg+"' style = ' height: 51px;margin: 9px 2px 0 13px;'></img>"
    +"</div>"
    +"<div style='float:left;height:100%;width:60%;position:relative;'>"
    +"<div style='font-size:12px;font-style:bold;float:left;margin:15px 0px 0px 10px;width:100%;position:relative;'><b>"+text+"</b></div>"
    +"<div style='font-size:10px;float:left;margin:6px 0px 10px 10px;width:100%;position:relative;'>"+body+"</div>";
    
    //    str+="<div style='font-size:10px;margin:60px 0px 20px 245px;width:100%;position:absolute;'>"+para+"</div>";
            
    str+="</div>"+"</div>" ;
    return str;
}


function removeDuplicateParameters(parameters){
    var resultStr = "";
    var result= new Array();
    var keyValuesForParameters;
    /* 
     * For Check first parameter is blank in url.
     * checkBlankParameter=false (If first parameter in url is blank) (ERP-12576 for this issue first parameter is blank)
     * checkBlankParameter=true  (If first parameter in url is not blank)
     */
    var checkBlankParameter=true;  
    keyValuesForParameters=parameters.split('&');
    var len=keyValuesForParameters.length;
    for(var i=0;i<len;i++){
        var parameter;
        parameter=keyValuesForParameters[i];
        var len1=result.length;
        var isPresent=false;
        for(var j=0;j<len1;j++){   //checking for duplicate key value pairs for parameters
            if(result[j]==parameter){
                isPresent=true;
                break; 
            }
        }
        if(!isPresent){
            if(keyValuesForParameters[0]==""){
                checkBlankParameter=false;
            }
            if(i!=0 && !checkBlankParameter){  //not inserting first value because it is blank
                result[i-1]=parameter;
                resultStr+="&"+parameter; 
            }
            /*
             *if First Parameter is blank in url
             */
            if(checkBlankParameter){
                result[i]=parameter;
                resultStr+="&"+parameter; 
            }
                
        }
    }
    return resultStr;
}

function getTopHtml(text, body,img,isgrid,margin){
    if(isgrid===undefined)isgrid=false;
    if(margin===undefined)margin='15px 0px 10px 10px';
    if(img===undefined||img==null) {
        img = '../../images/createuser.png';
    }
    var str =  "<div style = 'width:100%;height:100%;position:relative;float:left;'>"
    +"<div style='float:left;height:100%;width:auto;position:relative;'>"
    +"<img src = "+img+"  class = 'adminWinImg'></img>"
    +"</div>"
    +"<div style='float:left;height:100%;width:80%;position:relative;'>"
    +"<div style='font-size:12px;font-style:bold;float:left;margin:15px 0px 0px 10px;width:100%;position:relative;'><b>"+text+"</b></div>"
    +"<div style='font-size:10px;float:left;margin:15px 0px 10px 10px;width:100%;position:relative;'>"+body+"</div>"
    +(isgrid?"":"<div class='medatory-msg'>"+ExtGlobal.getLocaleText("acc.changePass.reqFields")+"</div>")
    +"</div>"
    +"</div>" ;
    return str;
}

//Export Url on the basis of Moduelid
function getExportUrlForCustomReportBuilder(reportId) {
    var exportUrl = "../../export.jsp";
//    switch(moduleId) {
//        case Ext.Acc_Sales_Order_ModuleId:
//                        exportUrl = "ACCSalesOrderCMN/exportSalesOrder.do";
//            exportUrl = "ACCSalesOrderCMN/exportSalesOrderforCustomReportBuilder.do";
//            break;
//    }
    if(reportId=="outstanding_consolidated_report"){
        exportUrl = "ACCInvoiceCMN/exportConsolidationAgedReceivable.do";
    } else{
        exportUrl = "ACCSalesOrderCMN/exportSalesOrderforCustomReportBuilder.do"; 
    }
    return exportUrl;
}

function getStoreActionMethods() {
    return {
        create: "POST",
        read: "POST",
        update: "POST",
        destroy: "POST"
    };
}
function headerCheck(header) {
    var indx=header.indexOf('(');
    if(indx!=-1) {
        indx=header.indexOf("&#");
        if(indx!=-1)
            header=header.substring(0,header.indexOf('('));
    }
    return header;
}

function getComboFieldStore(fieldid,comboboxname,moduleid,isdefaultfield){
    var url =  "ACCAccountCMN/getCustomCombodata.do";
    var extraParams = {
        mode: 2,
        flag: 1,
        fieldid: fieldid
    }
    var fields=[{name: 'id'},{name: 'name'}];
            
    if(isdefaultfield){
        if(comboboxname=="Shipping Route"){
            url = "ACCMaster/getMasterItems.do";
            extraParams={
                mode:112,   
                groupid:28,
                common:'1'
            }
        }else if (comboboxname == "Customer") {
            url = "ACCCustomer/getCustomersForCombo.do";
            extraParams={
                mode:112,   
                groupid:28,
                common:'1'
            }
            fields[0].mapping="accid";
            fields[1].mapping="accname";
        }else if (comboboxname == "Vendor") {
            url = "ACCVendor/getVendorsForCombo.do";
            extraParams={
                mode:2,
                group:13,
                deleted:false,
                nondeleted:true,
                common:'1'
            }
            fields[0].mapping="accid";
            fields[1].mapping="accname";
        }else if (comboboxname.indexOf("Currency")> -1) {
            url = "ACCCurrency/getCurrencyExchange.do";
            extraParams={
                mode:201,
                common:'1'
            }
            fields[0].mapping="tocurrencyid";
            fields[1].mapping="tocurrency";
        }else if ((comboboxname.indexOf("Sales Person"))> -1) {
            url = "ACCMaster/getMasterItems.do";
            extraParams={
                mode: 112,
                groupid: 15
            }
        }else if (comboboxname.indexOf("Agent")> -1) {
            url = "ACCMaster/getMasterItems.do";
            extraParams={
                mode: 112,
                groupid: 20
            }
        }else if(comboboxname == "Product Brand"){
            url = "ACCMaster/getMasterItems.do";
            extraParams={
                mode: 112,
                groupid: 53
            }
            
        }else if(comboboxname == "Product Category"){
            url = "ACCMaster/getMasterItems.do";
            extraParams={
                mode:112,
                groupid:19
            }
            
        }else if(comboboxname == "Customer Category"){
            url = "ACCMaster/getMasterItems.do";
            extraParams={
                mode:112,
                groupid:7
            }
            
        }else if (comboboxname.indexOf("Tax Name") > -1 || comboboxname == "Product Tax") {
            url = "ACCTax/getTax.do";
            extraParams={
                mode:33,
                common:'1',
                moduleid:moduleid
            }
            fields[0].mapping="taxid";
            fields[1].mapping="taxname";
        }else if (comboboxname.indexOf("Created By") > -1 || comboboxname == "Last Edited By") {
            url = "ProfileHandler/getAllUserDetails.do";
            extraParams={
                mode:11,
                isFromCustomReportBuilder:true
            }
            fields[0].mapping="userid";
            fields[1].mapping="fullname";
        }else if (comboboxname == "Payment Method") {
            url = "ACCPaymentMethods/getPaymentMethods.do";
            extraParams= "";
            fields[0].mapping="methodid";
            fields[1].mapping="methodname";
        }else if (comboboxname == "Paid To") {
            url = "ACCMaster/getMasterItems.do";
            extraParams={
                mode: 112,
                groupid: 17
            }
        }else if (comboboxname == "Received From") {
            url = "ACCMaster/getMasterItems.do";
            extraParams={
                mode: 112,
                groupid: 18
            }
        }else if (comboboxname == "SO No.") {
            url = "ACCLinkData/getLinkedSONo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "DO No.") {
            url = "ACCLinkData/getLinkedDONo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid,
                linkFlag:true
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "CQ No.") {
            url = "ACCLinkData/getLinkedCQNo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "SI No.") {
            url = "ACCLinkData/getLinkedSINo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "VQ No.") {
            url = "ACCLinkData/getLinkedVQNo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "PO No.") {
            url = "ACCLinkData/getLinkedPONo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "GR No.") {
            url = "ACCLinkData/getLinkedGRNo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "PI No.") {
            url = "ACCLinkData/getLinkedPINo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "Store") {
            url = "INVStore/getStoreList.do";
            extraParams={
                isAdvanceSearch:true,
                isActive : true,
                storeTypes : "1",
                requestModuleid:moduleid
            }
            fields[0].mapping="store_id";
            fields[1].mapping="fullname";
        }else if (comboboxname == "PR No.") {
            url = "ACCLinkData/getLinkedPRNo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "Debit Note") {
            url = "ACCLinkData/getLinkedDebitNoteNo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "Credit Note") {
            url = "ACCLinkData/getLinkedCreditNoteNo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "RFQ No.") {
            url = "ACCLinkData/getLinkedRFQNo.do";
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "Invoice No") {
            if(moduleid == 14/*Wtf.Acc_Make_Payment_ModuleId*/){             //Make Payment
                url = "ACCLinkData/getLinkedPINo.do";
            } else if(moduleid == 16/*Wtf.Acc_Receive_Payment_ModuleId*/){   //Receive Payment
                url = "ACCLinkData/getLinkedSINo.do"
            }
            extraParams={
                isAdvanceSearch:true,
                requestModuleid:moduleid
            }
            fields[0].mapping="billid";
            fields[1].mapping="billno";
        }else if (comboboxname == "Account") {
            url = "ACCAccountCMN/getAccountsForCombo.do";
            if(moduleid == Ext.Acc_Sales_Order_ModuleId || moduleid == Ext.Acc_Invoice_ModuleId){  
                extraParams={
                    mode:2,
                    nature : Ext.account.nature.Asset ,
                    ignoreCashAccounts:true,
                    ignoreBankAccounts:true,
                    ignoreGSTAccounts:true,  
                    ignorecustomers:true,  
                    ignorevendors:true,
                    ignore:true,
                    nondeleted:true
                }
            } else if(moduleid == Ext.Acc_Purchase_Order_ModuleId){   
                extraParams={
                    ignoreCashAccounts:true,
                    ignoreBankAccounts:true,
                    ignoreGSTAccounts:true,  
                    ignorecustomers:true, 
                    nature :Ext.account.nature.Liability,
                    ignorevendors:true,
                    nondeleted:true
                }
            }
           
            fields[0].mapping="accid";
            fields[1].mapping="accname";
        }else if (comboboxname == "Product Name") {
            url = "ACCProduct/getProductsForCombo.do";
            var isCustomer = false;
            if(moduleid == Ext.Acc_Sales_Order_ModuleId || moduleid == Ext.Acc_Invoice_ModuleId){
                isCustomer = true;
            }else if(moduleid == Ext.Acc_Purchase_Order_ModuleId){
                isCustomer = false;
            }
            
            extraParams={
                mode:22,
                termSalesOrPurchaseCheck:isCustomer
            }
            fields[0].mapping="productid";
            fields[1].mapping="productname";
            
        }else if(comboboxname == "UOM") {
            url = "ACCUoM/getUnitOfMeasure.do";
            
            extraParams={
                mode:31,
                common:'1'
            }
            fields[0].mapping="uomid";
            fields[1].mapping="uomname";
        }
    }
    
    var store;
    if(comboboxname == "Transaction Type") {    //need static store in case of Transaction Type (ERP-31477)
        store = Ext.create('Ext.data.ArrayStore', {
            fields:['id','name'],
            data: [["Cash","Cash"],
                ["Credit","Credit"],
            ],
            autoLoad: true
        });
    } else {
        store = Ext.create('Ext.data.Store', {
            autoLoad: true,
            fields: fields,
            proxy: {
                type: 'ajax',
                url: url,
                actionMethods : getStoreActionMethods(),
                reader: {
                    type: 'json',
                    rootProperty: 'data["data"]',
                    keepRawData: true,
                    totalProperty: 'data["totalCount"]'
                },
                extraParams: extraParams
            }
        });
    }
    return store;
}

function advanceSearchFormat(d) {
    var monthNames = [ "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" ],
        d2 = monthNames[d.getMonth()] +' '+ d.getDate() +', '+d.getFullYear();
    return d2;
}


function postData(url, params){
        var mapForm = document.createElement("form");
        mapForm.target = "downloadframe";
        mapForm.method = "post"; 
        mapForm.action = url;
        var params = params;
        var inputs =params.split('&');
        for(var i=0;i<inputs.length;i++){
            var KV_pair = inputs[i].split('=');
            var mapInput = document.createElement("input");
            mapInput.type = "text";
            mapInput.name = KV_pair[0];
            mapInput.value = decodeURIComponent(KV_pair[1]);
            mapForm.appendChild(mapInput); 
        }
        document.body.appendChild(mapForm);
        mapForm.submit();
        var div =  document.createElement("div");
        document.body.appendChild(div);
        mapForm.remove();
}

 function htmlDecode(val) {
    var ret = val.replace(/&gt;/g, '>');
    ret = ret.replace(/&lt;/g, '<');
    ret = ret.replace(/&quot;/g, '"');
    ret = ret.replace(/&apos;/g, "'");
    ret = ret.replace(/&amp;/g, '&');
    return ret;
};

function getURLAndParamsForDefaultReport(reportId){
    var params={};
    switch(reportId){
        case "outstanding_consolidated_report":
            params.url = "ACCInvoiceCMN/getConsolidationAgedReceivable.do";
            params.params = {};
            break;
    }        
    return params;
}

function checkReportNameExistance(reportName,successCallback,scope) {
    Ext.Ajax.request({
        url: 'ACCCreateCustomReport/isCustomReportNameExists.do',
        method:"POST",
        scope : scope,
        params: {
            reportName: reportName
        },
        success: function(res, req) {
            var resObj = eval("(" + res.responseText + ")");
            if(resObj.success == true) {
                if(successCallback) {
                    successCallback.call(this,resObj.isReportNameExists);
                }
            }
            else {
                Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), resObj.msg, Ext.Msg.INFO);
            }
        },
        failure: function() {
            Ext.CustomMsg('Error',ExtGlobal.getLocaleText("acc.common.errorOccuratServerSide"), Ext.Msg.ERROR);
        }
    });
}

function CopyReport(reportId,reportName,reportDescription,isEdit){
            Ext.Ajax.request({
                url: 'ACCCreateCustomReport/CopyCustomReport.do',
            method:"POST",
                params: {
                    reportId:reportId,
                    reportName:reportName,
                    reportDescription:reportDescription,
                    isEdit:isEdit
                },
            success: function(res, req) {
                    var resObj = eval("(" + res.responseText + ")");
                    if (resObj.success == true) {
                        Ext.CustomMsg(ExtGlobal.getLocaleText("acc.CustomReport.copyCustomReport"),ExtGlobal.getLocaleText("acc.CustomReport.reportcopiedSuccess")+" "+reportName, Ext.Msg.INFO);
                        Ext.getCmp("idreportlistgrid").getStore().load();
                    } else {
                        Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"),resObj.msg, Ext.Msg.INFO);
                    }
                },
            failure: function() {
                    Ext.CustomMsg('Error', ExtGlobal.getLocaleText("acc.common.errorOccuratServerSide"), Ext.Msg.ERROR);
                }
            });
    }
