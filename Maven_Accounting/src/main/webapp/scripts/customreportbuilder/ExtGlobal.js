/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.pref= JSON.parse(sessionStorage.getItem("wtfPref"));
var showPivot;

Ext.userReportRole = JSON.parse(sessionStorage.getItem("userReportRole"));//User permission map
Ext.ADMIN_ROLE_ID = '1';
var loginid = sessionStorage.getItem("loginid");

ExtGlobal={
    getLocaleText:function(key, basename, def){
        var base=window[basename||"messages"];
        var params=[].concat(key.params||[]);
        key = key.key||key;
        if(base){
            if(base[key]){
                params.splice(0, 0, base[key]);
                //                return String.format.apply(this,params);
                return params.toString();
            //                return this,params[0];
            }else
                console.log("Locale specific text not found for ["+key+"]");
        }else{
            console.log("Locale specific base ("+basename+") not available");
        }
        return def||key;
    },
    getColumnRenderer :function(column){
        var renderer = "";
        var xtype =parseInt(column.xtype);
        if(xtype == Ext.fieldType.numberField){
            var rendererProperty = column.properties.source.renderer;
            renderer = function(val, metaData, record){
                val = Ext.util.Format.number(val, ExtGlobal.getColumnRendererFormat(column));
                    
                if(column.defaultHeader=="Discount" && record!=undefined && record.data!=undefined && (record.data.discountispercent=="1"||record.data.discountispercent=="T")){//For Discount is in percentage
                    if(val !="" && val!=undefined){
                        if(column.isMeasureItem){ 
                            val = val;
                        } else {                           
                            val = val+ "%";
                        }
                    }
                }else if(rendererProperty== "Transaction Currency"){
                    if (val != "" && val != undefined && record != undefined && record.data != undefined) {
                        var currency = record.data.currencysymbol;
//                        var currency = record.data["currencysymbol_"+columnId];
//                        val = ((currency!=undefined) ? (currency + " " + val) : val);
                        var columnId = ((column.id).replace(/-/gi, ''));
                        var currencyForColumn = record.data["currencysymbol_" + columnId];
                        currencyForColumn =  ((currencyForColumn != undefined) ? (currencyForColumn + " " + val) : currency);
                        val = ((currencyForColumn != undefined) ? (currencyForColumn + " " + val) : val);
                    }
                }else if(rendererProperty== "Base Currency"){
                    if(val !="" && val!=undefined){
                        val = Ext.pref.CurrencySymbol+" "+val;
                    }
                }else if(column.defaultHeader== "Quantity" || column.defaultHeader== "Base Quantity" ||
                        column.defaultHeader== "Balance Quantity" || column.defaultHeader== "Actual Quantity" || 
                        column.defaultHeader== "Received Quantity" || column.defaultHeader== "Return Quantity" ||
                        column.defaultHeader== "Delivered Quantity" || column.defaultHeader== "Base UOM Quantity"){
                    if(val !="" && val!=undefined && record!=undefined && record.data!=undefined){
                        var displayUOM = record.data.displayUOM;
                        val = val + " " + ((displayUOM!=undefined) ? (displayUOM) : "");
                    }
                }else if(column.defaultHeader== "Display UOM"){
                    if(val !="" && val!=undefined && record!=undefined && record.data!=undefined){
                        var displayUOMMapping = record.data.displayUOMMapping;
                        val = val + " " + ((displayUOMMapping!=undefined) ? (displayUOMMapping) : "");
                    }
                }
                return val;
            }
        }else{
           renderer = function(val, metaData, record){
//               val = htmlDecode(val);
//                var origVal = val;
                var origVal="";
                if(val !=undefined)
                    origVal = val.replace(/'/g,"");
                if(val !=undefined && metaData !=undefined && metaData.column !=undefined){
                    if( record.data[metaData.column.dataIndex] != undefined && record.data[metaData.column.dataIndex].length > metaData.column.cellWidth)
                    {
                        val = Ext.util.Format.ellipsis(val.replace(/(<([^>]+)>)/ig, ''), metaData.column.cellWidth);
                    } 
                  return "<div data-qtip='"+origVal+"' style='text-overflow: ellipsis;overflow: hidden;width:"+metaData.column.cellWidth+"px;'>"+val+"</div>";
                }
                return val;
            }  
        }
        return renderer;
    },
    getSummaryRenderer :function(column){
        var renderer = "";
        var xtype =parseInt(column.xtype);
        var summaryType = column.summaryType ? column.summaryType.capitalizeFirstLetter():"";
        if(summaryType == "Sum"){
            summaryType = "Total";
        }
        if(xtype == Ext.fieldType.numberField){
            var rendererProperty = column.properties.source.renderer;
            renderer = function(val, metaData, record){
                val = Ext.util.Format.number(val, ExtGlobal.getColumnRendererFormat(column));
                    
                if(rendererProperty== "Base Currency"){
                    if(val !="" && val!=undefined){
                        val = Ext.pref.CurrencySymbol+" "+val;
                    }
                }
                if(val !="" && val!=undefined){
                    val = summaryType+" : "+val;
                }
                return val;
            }
        }else{
            renderer = function(val, metaData, record){
                if(val !="" && val!=undefined){
                    val = summaryType+" : "+val;
                }
                return val;
            }; 
        }
        return renderer;
    },
    getColumnRendererFormat : function(column){
        var format="";
        if(column.xtype == Ext.fieldType.numberField){
            var precision=column.properties.source.precision;
            var isCustom = column.customfield != undefined ? column.customfield : column.custom;
            if( typeof isCustom === 'string'){
                isCustom = (isCustom  === "true");
            }
            format= isCustom ? '0000':'0,000';
            for(var i=0;i<precision;i++){
                if(i==0){
                    format +='.'
                }
                format += '0'
            }
        }
        return format;
    },
    
    setRenderersToPivotColumns: function (columnArr) {
        for (var i = 0; i < columnArr.length; i++) {
            if (columnArr[i].properties != undefined && columnArr[i].renderer == undefined) {
                if (columnArr[i].rendererType != "None") {
                    columnArr[i].renderer = ExtGlobal.getColumnRenderer(columnArr[i]);
                } else {
                    columnArr[i].renderer = function (val, metaData, record) {
                        return val;
                    }
                }
            } else if (columnArr[i].properties == undefined && columnArr[i].rendererType == "None") {//To set renderer for Day, Year and Month fields as norenderer
                columnArr[i].renderer = function (val, metaData, record) {
                    return val;
                }
            //following logic is to keep old reports working for which properties were not set in reportjson. properties were not being set earlier than this logic, so check on properties
            } else if (columnArr[i].properties == undefined && columnArr[i].rendererType === "Base Currency") {
                columnArr[i].renderer = function (val, metaData, record) {
                    if (val != undefined && val !== "") {
                        val = Ext.pref.CurrencySymbol + " " + val;
                    }
                    return val;
                }
            }
        }
        return columnArr;
    },
    
    getFilterType : function(column,moduleid){
        var xtype = parseInt(column.xtype);
        var filterType="";
        var isCustom = column.customfield != undefined ? column.customfield : column.custom;
        if( typeof isCustom === 'string'){
            isCustom = (isCustom  === "true");
        }
        
        if(xtype==Ext.fieldType.numberField){
            filterType = {
                type: "number"
            };
        }else if(xtype==Ext.fieldType.dateField){
            filterType = {
                type: "date",
                dateFormat:'Y-m-d'
            };
        }else if(xtype==Ext.fieldType.checkbox){
            filterType = {
                type: 'boolean',
                yesText: 'True',
                noText: 'False'
            }
        }else if(xtype== Ext.fieldType.comboBox || xtype == Ext.fieldType.multiselect || xtype == Ext.fieldType.listBox){
            var filterStore = getComboFieldStore(column.id , column.defaultHeader, moduleid, !isCustom);
                filterType =  {
                    type: 'customlist',
                    store: filterStore,
                    idField:'id',
                    labelField:'name'
                }
        }else {
                filterType = {
                    type: "string"
                };
            }
        return filterType;
    },
    getFilterTypeforReport : function(column,moduleid){
        var xtype = parseInt(column.xtype);
        var filterType="";
        var isCustom = column.customfield != undefined ? column.customfield : column.custom;
        if( typeof isCustom === 'string'){
            isCustom = (isCustom  === "true");
        }
        var value="";
        if(xtype==Ext.fieldType.numberField){
            filterType = {
                type: "number"
            };
            if(column.filter!=undefined && column.filter!=null){
                if(column.filter.length>0){   
                    var filterrecord=column.filter;
                    var jsonobject={};
                    for(var i=0;i<filterrecord.length;i++){
                        if(filterrecord[i].operator=="lt"){
                            jsonobject.lt=filterrecord[i].value;
                        }else if(filterrecord[i].operator=="gt"){
                            jsonobject.gt=filterrecord[i].value;
                        }else if(filterrecord[i].operator=="eq"){
                            jsonobject.eq=filterrecord[i].value;
                        }
                    }
                    filterType = {
                        type: "number",
                        value:jsonobject
                    };
                }//end of greater than 0
            }//of null check
            
        }else if(xtype==Ext.fieldType.dateField){
            filterType = {
                type: "date",
                dateFormat:'Y-m-d'
            };   
            if(column.filter!=undefined && column.filter!=null){
                if(column.filter.length>0){   
                    var filterrecord=column.filter;
                    var jsonobject={};
                    for(var i=0;i<filterrecord.length;i++){
                        if(filterrecord[i].operator=="lt"){
                            jsonobject.lt=new Date(filterrecord[i].value);
                        }else if(filterrecord[i].operator=="gt"){
                            jsonobject.gt=new Date(filterrecord[i].value);
                        }else if(filterrecord[i].operator=="eq"){
                            jsonobject.eq=new Date(filterrecord[i].value);
                        }
                    }
                    filterType = {
                        type:  "date",
                        dateFormat:'Y-m-d',
                        value:jsonobject
                    };
                }//end of greater than 0
            }//of null check
        }else if(xtype==Ext.fieldType.checkbox){
            
            filterType = {
                type: 'boolean',
                yesText: 'True',
                noText: 'False'
            }
            if(column.filter!=undefined && column.filter!=null){
                if(column.filter.length>0){   
                    var filterrecord=column.filter;
                    for(var i=0;i<filterrecord.length;i++){
                        value=filterrecord[i].value;
                    }
                    filterType = {
                        type: 'boolean',
                        defaultValue : value,///error in extjs 6.0.1
                        value : value,
                        yesText: 'True',
                        noText: 'False'
                    };
                }//end of greater than 0
            }//of null check
        }else if(xtype== Ext.fieldType.comboBox || xtype == Ext.fieldType.multiselect || xtype == Ext.fieldType.listBox){
            var filterStore = getComboFieldStore(column.id , column.defaultHeader, moduleid, !isCustom);
            filterType =  {
                type: 'customlist',
                store: filterStore,
                idField:'id',
                labelField:'name'
            }
        }else {
            filterType = {
                type: "string"
            };
            
            if(column.filter!=undefined && column.filter!=null){
                if(column.filter.length>0){   
                    var filterrecord=column.filter;
                    for(var i=0;i<filterrecord.length;i++){
                        value=filterrecord[i].value;
                    }
                    filterType = {
                        type: "string",
                        value:value
                    };    
                }//end of greater than 0
            }//of null check
        }
        return filterType;
    },
    getFieldType : function(field){
        var fieldType=""
        switch(parseInt(field)){
            case Ext.fieldType.textField :
                fieldType = "Text Field";
                break;
            case Ext.fieldType.numberField :
                fieldType = "Number Field";
                break;
            case Ext.fieldType.dateField :
                fieldType = "Date Field";
                break;
            case Ext.fieldType.comboBox :
                fieldType = "Combo Box";
                break;
            case Ext.fieldType.multiselect :
                fieldType = "Multiselect Combo Box";
                break;
            case Ext.fieldType.checkbox :
                fieldType = "Check Box";
                break;
            case Ext.fieldType.listBox :
                fieldType = "List Box";
                break;
            case Ext.fieldType.textArea :
                fieldType = "Text Area";
                break;
        }
        return fieldType;
    },
    convertToGenericDate:function(value){
        if(!value) return value;
        return Ext.Date.format(value,"Y-m-d");
    },
    HTMLStripper: function(val){
        var str = Ext.util.Format.stripTags(val);
        return str.replace(/"/g, '').trim();
    },
    getXType: function(fieldType){
        switch(fieldType){

            case 1:
                return "textfield";
                break;
            case 2:
                return "numberfield";
                break;
            case 3:
                return "datefield";
                break;
            case 4:
                return "combo";
                break;
            case 5:
                return "timefield";
                break;
            case 11:
                return "checkbox";
                break;
            case 7:
                return "select";
                break;
            case 8:
                return "combo";
                break;
            case 9:
                return "autono";
                break;
            case 12:
                return "fieldset";
                break;
            case 13:
                return "textarea";
                break;
        }
    },
    replaceAll : function(txt, replace, with_this) {
        return txt.replace(new RegExp(replace, 'g'),with_this);
    },
    reportNameTest : /^(\w+ ?)*$/,
    
    //To use this function you need to specify pluginColumns in grid config and all plugin columns should be initially unlocked.
    lockPluginColumns:function(grid, column, eOpts){     
        var me = grid,
        pluginColumns = grid.pluginColumns,
        normalGrid = me.normalGrid,
        lockedGrid = me.lockedGrid,
        normalView = normalGrid.view,
        lockedView = lockedGrid.view,
        normalHCt = normalGrid.headerCt,
        lockedHCt = lockedGrid.headerCt,
        newIndex = 0,
        refreshFlags, ownerCt,activeHd;
            
        normalView.blockRefresh = lockedView.blockRefresh = true;
        if(lockedHCt.getColumnCount() == 2){  // by defualt one plugin column is get locked when we loacked any column.
            for(var i=0 ; i < pluginColumns - 1 ; i++){   // hence lock all remaining plugin columns
                activeHd = normalHCt.items.items[0];
                ownerCt = activeHd.ownerCt;
                
                // We decide which views to refresh. Do not let the grids do it in response to column changes
                // Keep the column in the hierarchy during the move.
                activeHd.ownerCmp = activeHd.ownerCt;
                ownerCt.remove(activeHd, false);
                activeHd.locked = true;
                // Flag to the locked column add listener to do nothing
                if(i==0 && pluginColumns > 2){
                    newIndex = i;
                }else{
                    newIndex = i+1 ;
                }
                lockedHCt.insert(newIndex, activeHd);
                activeHd.ownerCmp = null;
            }
            normalView.blockRefresh = lockedView.blockRefresh = false;
            refreshFlags = me.syncLockedWidth();
            if (refreshFlags[0]) {
                lockedGrid.getView().refreshView();
            }
            if (refreshFlags[1]) {
                normalGrid.getView().refreshView();
            }
        }
    },
    
    //To use this function you need to specify pluginColumns in grid config and all plugin columns should be initially unlocked.
    unlockPluginColumns : function(grid, column, eOpts){
        var me = grid,
        pluginColumns=grid.pluginColumns,
        normalGrid = me.normalGrid,
        lockedGrid = me.lockedGrid,
        normalView = normalGrid.view,
        lockedView = lockedGrid.view,
        normalHCt = normalGrid.headerCt,
        lockedHCt = lockedGrid.headerCt,
        newIndex = 0,
        refreshFlags, activeHd;
            
        normalView.blockRefresh = lockedView.blockRefresh = true;
        if(lockedHCt.getColumnCount() == pluginColumns){
            for (var i=0 ; i < pluginColumns ; i++){
                activeHd = lockedHCt.items.items[0];
                // We decide which views to refresh. Do not let the grids do it in response to column changes
                // Keep the column in the hierarchy during the move.
                // So that grid.isAncestor(column) still returns true, and SpreadsheetModel does not deselect
                activeHd.ownerCmp = activeHd.ownerCt;
                activeHd.ownerCt.remove(activeHd, false);
                activeHd.locked = false;
                
                normalHCt.insert(i, activeHd);
                activeHd.ownerCmp = null;
            }
            
            normalView.blockRefresh = lockedView.blockRefresh = false;
            // syncLockedWidth returns visible column counts for both grids.
            // only refresh what needs refreshing
            refreshFlags = me.syncLockedWidth();
            if (refreshFlags[0]) {
                lockedGrid.getView().refreshView();
            }
            if (refreshFlags[1]) {
                normalGrid.getView().refreshView();
            }
        }
    },
    
    getSeperatorPos: function() {
        return Ext.pref.seperatorpos;
    },
    getGeneratedOnTimestamp : function(){
        var generateOnDate=new Date();
        return Ext.Date.format(generateOnDate,ExtGlobal.getDateFormat());
    },
    
    getDateFormat : function(){
        if(Ext.pref.DateFormat== undefined) {
            return "Y-m-d";
        }
        return Ext.pref.DateFormat ; 
    },
    
    getOnlyDateFormat: function() {
        var pos = ExtGlobal.getSeperatorPos();
        var fmt = ExtGlobal.getDateFormat();
        if(pos<=0)
            return "Y-m-d";
        return fmt.substring(0,pos);
    },
    
    convertToGenericStartDate:function(value){ //this method mainly used to format Report Start Date  
        var d = new Date();
        if(!value) return value;       
        var monthDateStr = Ext.Date.format(value,'M d');
        var startdate=new Date(monthDateStr+', '+value.getFullYear()+' 12:00:00 AM');
        return Ext.Date.format(startdate,"M d, Y h:i:s A");
    },
    
    convertToGenericEndDate:function(value){ //this method mainly used to format Report End Date
        var d = new Date();
        if(!value)return value;
        var monthDateStr= Ext.Date.format(value,'M d');
        var enddate=new Date(monthDateStr+', '+value.getFullYear()+' 11:59:59 PM');
        return Ext.Date.format(enddate,"M d, Y h:i:s A");
    },
    
    baseCurrencyRenderer:function(val, metaData, record){
        if(typeof val ==="number"){
            val = Ext.util.Format.number(val, "0000.00");
        }
        if(val !== "" && val!=undefined){
            val = Ext.pref.CurrencySymbol+" "+val;
        }
        return val;
    },
    
    saveReportConfig: function (scope, isPivot, gridConfigId, state, reportid, column, direction) {
        var currentState = new Object();
        if (isPivot && state) {
            currentState.isCollapsed = scope.isCollapsed;
            currentState.pivotcolumns = state.pivotcolumns;
            currentState.rowSubTotalsPosition = state.rowSubTotalsPosition;
            currentState.colSubTotalsPosition = state.colSubTotalsPosition;
            currentState.rowGrandTotalsPosition = state.rowGrandTotalsPosition;
            currentState.colGrandTotalsPosition = state.colGrandTotalsPosition;
            
            if(column) {
                currentState.sortedColumn = {
                    dataIndex: column.dataIndex,
                    direction: direction
                }
            }
        }

        Ext.Ajax.request({
            scope: this,
            actionMethods: getStoreActionMethods(),
            url: 'ACCCreateCustomReport/saveGridConfig.do',
            params: {
                cid: gridConfigId,
                reportid: reportid,
                state: Ext.encode(currentState)
            },
            success: function (responce) {
                if(responce.responseText) {
                    scope.gridConfigId = Ext.decode(responce.responseText).cid;
                }
            }
        });
    },
    
    getReportConfig: function (scope, reportid) {
        Ext.Ajax.request({
            scope: this,
            actionMethods: getStoreActionMethods(),
            url: 'ACCCreateCustomReport/getGridConfig.do',
            params: {
                reportid: reportid
            },
            success: function (responce) {
                if(responce.responseText) {
                    scope.gridConfigId = Ext.decode(responce.responseText).cid;
                    scope.setReportStateConfig(Ext.decode(responce.responseText).state);
                }
            }
        });
    }
}
//defines chart panel to show charts
Ext.define("Ext.Chart", {
    extend: "Ext.panel.Panel",
    initComponent: function () {        
        if (this.mode == "local") {
            this.loadChart({});
        } else {
            Ext.Ajax.request({
                url: this.dataUrl,
                params: this.params,
                actionMethods: getStoreActionMethods(),
                timeout: 600000,
                scope: this,
                success: function (response, opts) {
                    var resData = Ext.JSON.decode(response.responseText);
                    this.loadChart(resData);
                },
                failure: function () {
                    this.body.dom.innerHTML = "<div class='grid-empty-text disabled-record' style = 'margin-top: 25px;'>" + ExtGlobal.getLocaleText("acc.common.norec") + "</div>";
                }
            });
        }
        this.callParent(arguments);
    },
    
    loadChart: function (resData) {
        if (resData.data != undefined) {
            this.chartdata = resData.data;
        } else {
            this.chartdata = resData;
        }

        if (this.rendered) {
            this.createChart();
        } else {
            this.on("render", function () {
                this.createChart();
            }, this);
        }
    },
    
    createChart: function () {
        var chartId = this.body.id;

        if (this.chartConfig.dataProvider == undefined) {
            this.chartConfig.dataProvider = this.chartdata;
        }

        if (this.chartConfig.dataProvider != undefined && this.chartConfig.dataProvider.length > 0) {
            if (this.mode == "local") {
                AmCharts.makeChart(chartId, this.chartConfig, 100);
            } else {
                AmCharts.makeChart(chartId, this.chartConfig);
            }
        } else {
            this.body.dom.innerHTML = "<div class='grid-empty-text disabled-record' style = 'margin-top: 25px;'>" + ExtGlobal.getLocaleText("acc.common.norec") + "</div>";
        }
    }
});
//Chart container that enables to add tbar, bbar and other functinality to chart panel
Ext.define("Ext.ChartContainer", {
    extend: "Ext.panel.Panel",
    initComponent: function () {    
        this.btnArr = [];
        this.fromDate = new Ext.form.field.Date({
            fieldLabel: ExtGlobal.getLocaleText("acc.common.from"),
            id: 'startdate_' + this.chartData.id,
            labelWidth: 30,
            width: 145,
            format: ExtGlobal.getOnlyDateFormat(),
            value: this.chartData.params.fromDate != undefined ? this.chartData.params.fromDate : financialYearFromDate,
            vtype: 'daterange',
            endDateField: 'enddate_' + this.chartData.id
        });
        this.btnArr.push(this.fromDate);

        this.toDate = new Ext.form.field.Date({
            fieldLabel: ExtGlobal.getLocaleText("acc.common.to"),
            id: 'enddate_' + this.chartData.id,
            labelWidth: 20,
            width: 145,
            format: ExtGlobal.getOnlyDateFormat(),
            value: this.chartData.params.toDate != undefined ? this.chartData.params.toDate : financialYearToDate,
            vtype: 'daterange',
            startDateField: 'startdate_' + this.chartData.id
        });
        this.btnArr.push('-', this.toDate);

        this.fetchBtn = new Ext.Button({
            xtype: 'button',
            text: ExtGlobal.getLocaleText("acc.common.fetch"),
            iconCls: 'accountingbase fetch',
            scope: this,
            tooltip: ExtGlobal.getLocaleText("acc.invReport.fetchTT"),
            handler: function () {
                this.chartData.params.fromDate = ExtGlobal.convertToGenericDate(this.fromDate.getValue());
                this.chartData.params.toDate = ExtGlobal.convertToGenericDate(this.toDate.getValue());
                this.getChart(this.chartData);
            }
        });
        this.btnArr.push('-', this.fetchBtn);

        this.resetBttn = new Ext.Button({
            text: ExtGlobal.getLocaleText("acc.common.reset"),
            scope: this,
            iconCls: "pwnd reset",
            tooltip: ExtGlobal.getLocaleText("acc.common.reset") + " date",
            disabled: false,
            handler: function () {
                this.fromDate.setValue(financialYearFromDate);
                this.toDate.setValue(financialYearToDate);
            }
        });
        this.btnArr.push('-', this.resetBttn);
        
        this.editChartBtn = new Ext.Button({
            text: ExtGlobal.getLocaleText("acc.common.editBtn"),
            scope: this,
            iconCls: this.chartData.iconCls != undefined ? this.chartData.iconCls : 'accountingbase chart',
            tooltip: ExtGlobal.getLocaleText("acc.common.editBtn"),
            disabled: false,
            handler: function () {
                var chartBuilder = Ext.create('ReportBuilder.view.ChartBuilder', {
                    chartId: this.chartData.id,
                    reportId: this.chartData.reportId,
                    chartData: this.chartData,
                    isEdit: true
                });
                chartBuilder.show();
            }
        });
        this.btnArr.push('-', this.editChartBtn, '-');
        
        this.chart = getCustomReportChartPanel(this.chartData);        
        
        Ext.apply(this,{
           tbar : this.btnArr,
           items:[this.chart]
        });
        this.callParent(arguments);
    },
    
    getChart: function(chartData) {
        if (this.chart != undefined) {
            this.chart.destroy();
            this.chart = undefined;
        }
        this.chart = getCustomReportChartPanel(chartData);        
        this.add(this.chart);
    }
});
//create and return the chart panel
function getCustomReportChartPanel(chartData) {
    var chartConfig = addDateRangeText(chartData);
    var params = chartData.params;
    params.titleField = chartData.titleField;
    params.valueField = chartData.valueField;
    params.groupby = chartData.groupby;
    
    if(chartConfig == undefined){
        chartConfig = {};
    }
    
    var panel = new Ext.Chart({
        border: false,
        layout: "fit",
        chartConfig: chartConfig,
        dataUrl: chartData.url,
        params: params
    });
    
    return panel;
}
//create and return chart panel container
function getChartPanelContainer(chartData) {
    var id = chartData.id;
    var chartContainer = Ext.getCmp("CustomChart_" + id);
    
    if (chartContainer) {
        chartContainer.destroy();
        chartContainer = undefined;
    }

        chartContainer = new Ext.ChartContainer({
        id: "CustomChart_" + id,
        iconCls: chartData.iconCls != undefined ? chartData.iconCls : 'accountingbase chart',
            title: chartData.name,
            border: false,
            layout: "fit",
            closable: true,
            chartData: chartData
        });
        
        Ext.getCmp("mainTabPanel").add(chartContainer);
    Ext.getCmp("mainTabPanel").setActiveTab(chartContainer);
    }
    
//adding date range sub title text to chart config
function addDateRangeText(chartData) {
    var fromDate = Ext.Date.format(Ext.Date.parse(chartData.params.fromDate, 'Y-m-d'), ExtGlobal.getOnlyDateFormat());
    var toDate = Ext.Date.format(Ext.Date.parse(chartData.params.toDate, 'Y-m-d'), ExtGlobal.getOnlyDateFormat());
    
    var chartConfig = Ext.JSON.decode(chartData.properties);
    
    var title = Ext.clone(chartConfig.titles[0]);
    title.bold = false;
    title.size = title.size - 2;
    title.text = "From : " + fromDate + " To : " + toDate;
    
    chartConfig.titles.push(title);
    return chartConfig;
}
