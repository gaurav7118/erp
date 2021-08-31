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
/*
 * Array to match fields for applying decimal and allow amount in comma for line item table.
 */
var numberFieldsArr = ['quantity','quantity with uom', 'actual quantity with uom', 'delivered quantity with uom', 'received quantity with uom'  
               , 'return quantity with uom','base qty with uom', 'rrp', 'batch sub quantity', 'exchange rate subtotal without discount'  
               , 'transaction balance amount', 'balance qty with uom'];

Ext.grid.CheckColumn = function (config) {
    Ext.apply(this, config);
    if (!this.id)
        this.id = Ext.id();
    this.renderer = Ext.Function.pass(this.renderer, [this]);//this.renderer.createDelegate(this);
};
Ext.grid.CheckColumn.prototype = {
    fyear: 0,
    byear: 0,
    fdate: 0,
    bdate: 0,
    init: function (grid) {
        this.grid = grid;
        this.grid.on('render', function () {
            var view = this.grid.getView();
            view.mainBody.on('mousedown', this.onMouseDown, this);
        }, this);
    },
    onMouseDown: function (e, t) {
        if (t.className && t.className.indexOf('x-grid3-cc-' + this.id) != -1) {
            e.stopEvent();
            var index = this.grid.getView().findRowIndex(t);
            var record = this.grid.store.getAt(index);
            record.set(this.dataIndex, !record.data[this.dataIndex]);
            this.grid.fireEvent("afteredit", {
                grid: this.grid,
                record: record,
                field: this.dataIndex,
                value: !record.data[this.dataIndex],
                originalValue: record.data[this.dataIndex],
                row: index,
                column: 0//Not known
            });
        }
    },
    renderer: function (v, p, record) {
        p.css += ' x-grid3-check-col-td';
        return '<div class="x-grid3-check-col' + (v ? '-on' : '') + ' x-grid3-cc-' + this.id + '">&#160;</div>';
    }
};

Ext.ProductGridTemplateWindow = function (conf) {
    Ext.apply(this, conf);
    this.addEvents({
        "okClicked": true
    });
    this.createEditor();
    this.lineFieldsStore = new Ext.create('Ext.data.Store', {
        fields: ['columnname', 'displayfield', 'hidecol', 'seq', 'fieldid', 'coltotal', 'colwidth', 'xtype', 'headerproperty', 'showtotal', 'recordcurrency', 'headercurrency', 'decimalpoint', 'colno', 'totalname'],
        data: documentLineColumnsArray, // [["Product Name","Product Name",false,'0','1'],["Product Description","Product Description",false,'1','2'],["Rate","Rate",false,'2','3'], ["Quantity","Quantity",false,'3','4'],["Total Amount","Total Amount",false,'4','5']/*,["Campaign","Campaign"]*/],
        autoLoad: true,
        sorters: [
            {
                property : 'columnname',
                direction: 'ASC'
            }
        ]
    });
    this.url = "ACCCurrency/getCurrency.do";
     this.fields = [{
                        name:'typeid',
                        type:'int'
                    }, {
                        name:'currency',
                        type:'string'
                    }];
        this.store = Ext.create('Ext.data.Store', {
        autoLoad: false,
        fields: this.fields,
        proxy: {
            type: 'ajax',
            url: this.url,
            reader: {
                type: 'json',
                rootProperty: "data[\"data\"]",
                keepRawData: true,
                totalProperty: "data[\"count\"]"
            }
        }
    });
    
    var data = "";
    this.store.load();  
        
    this.store.on("load", function() {
            data = this.store.getProxy().getReader().rawData.data.data;
        }, this);
    
    
    
    this.selectField = new Ext.form.field.ComboBox({
        fieldLabel:"Select Field",
        padding:'10 10 10 10',
        displayField:'columnname',
        queryMode: 'local',
        valueField:'fieldid',
        store:this.lineFieldsStore,
        scope:this
    });
    this.addBtn = Ext.create('Ext.Button', {
        text: 'Add',
        margin:'10 10 10 10',
        id: 'idAddColumnToLineTable',
        scope:this,
        handler:function(){
            if(this.selectField && this.selectField.getValue() != undefined && this.selectField.getValue() != ""){
                var rec = searchRecord(this.lineFieldsStore, this.selectField.getValue(), 'fieldid');
                if(rec){
                    if(this.reportStore){
                            var recordPresent = false;
                            this.reportStore.each(function(record){
                                if(record.data.columnname == rec.data.columnname){
                                   recordPresent=true;
                                }
                            });
                            if(recordPresent){
                                Ext.MessageBox.show({
                                    title: 'Alert',
                                    msg: 'Selected column is already present',
                                    icon: Ext.MessageBox.QUESTION,
                                    buttons: Ext.MessageBox.OK
                                });
                            } else{
                                rec.data.seq = this.reportStore.data.items.length + 1;
                                rec.data.hidecol = false;
                                var max = 0;
                                this.reportStore.each(function(record){
                                   max=Math.max(record.data.colno, max);
                                });
                                rec.data.colno = ++max;
                            if(rec.data.columnname == "Rate")
                            {
                                    rec.data.decimalpoint = _unitpriceDecimalPrecision;
                            }else if(rec.data.columnname=="Quantity" || rec.data.columnname =="Quantity With UOM" || rec.data.columnname == "Actual Quantity With UOM" || rec.data.columnname == "Actual Quantity" || rec.data.columnname == "Delivered Quantity With UOM" || rec.data.columnname == "Delivered Quantity" || rec.data.columnname == "Received Quantity With UOM" || rec.data.columnname == "Received Quantity" || rec.data.columnname == "Return Quantity With UOM" || rec.data.columnname == "Return Quantity" || rec.data.columnname =="Base Qty" || rec.data.columnname =="Base Qty With UOM")
                            {
                                    rec.data.decimalpoint = _quantityDecimalPrecision;
                                }else if(rec.data.xtype=="2"){
                                    rec.data.decimalpoint = _amountDecimalPrecision;
                                }
                                this.reportStore.add(rec);
                            }
                        }
                }
            }
        }
    });
    
    this.addFormulaBtn = Ext.create('Ext.Button', {
        text: 'Add Formula',
        margin:'10 10 10 10',
        id: 'idAddFormulaToLineTable',
        scope:this,
        handler: function(){
            this.createFormulaWindow = openFormulaBuilderWindow(Ext.fieldID.insertField, false, false, true);
            this.createFormulaWindow.updateFormulaFieldTextValue = function(){
                var expression=""; 
                var expressionVal="";
                for(var i=0;i<this.operatormeasurefieldsDrop.length;i++){
                    if(this.operatormeasurefieldsDrop[i].isnumber!=undefined && this.operatormeasurefieldsDrop[i].isnumber){
                            expression +=this.operatormeasurefieldsDrop[i].defaultHeader;
                            expressionVal +=this.operatormeasurefieldsDrop[i].defaultHeader;
                        }else if(this.operatormeasurefieldsDrop[i].isnumber!=undefined && !this.operatormeasurefieldsDrop[i].isnumber){
                            expression +=this.operatormeasurefieldsDrop[i].defaultHeader+" ";
                            expressionVal +=this.operatormeasurefieldsDrop[i].defaultHeader+" ";
                        }else{
                            expression +="#"+this.operatormeasurefieldsDrop[i].defaultHeader+"# ";
                            if(this.operatormeasurefieldsDrop[i].customfield){
                                expressionVal +="#"+this.operatormeasurefieldsDrop[i].defaultHeader.replace(/-/g, "!##").replace(/\//g, "$##")+"# ";// replace all '-' and '/' characters from field name with unique identifier
                            } else{
                                expressionVal +="#"+this.operatormeasurefieldsDrop[i].id+"# ";
                            }
                        }
                }
                this.formulaText.setValue(expression);
                this.formulaTextValue = expressionVal;
            };
            this.createFormulaWindow.saveFormula = this.saveRecord.bind(this);
            this.createFormulaWindow.show();
        }
    });

    this.addBlankColumnBtn = Ext.create('Ext.Button', {
        text: 'Add Blank Column',
        margin:'10 10 10 10',
        id: 'idAddBlankColumnToLineTable',
        scope:this,
        handler: function () {
            var blankRec = {
                fieldid: '_blank',
                columnname: 'Blank Column',
                hidecol: false,
                coltotal: false,
                colwidth: 5,
                xtype: '1',
                showtotal: false
            };
            
            if (this.reportStore) {
                var max = 0;
                var blankColumnCounter = 0;
                this.reportStore.each(function (record) {
                    max = Math.max(record.data.colno, max);
                    if (record.data.fieldid == '_blank') {
                        blankColumnCounter = Math.max(record.data.seq, blankColumnCounter);
                    }
                });

                blankRec.columnname = blankRec.displayfield = blankRec.columnname + ' ' + (blankColumnCounter + 1);

                blankRec.seq = (blankColumnCounter + 1);
                blankRec.colno = ++max;

                this.reportStore.add(blankRec);
            }
        }
    });
    
    Ext.ProductGridTemplateWindow.superclass.constructor.call(this, {
        width: 900,
        height: 500,
        resizable: false,
        bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF !important;background: #FFFFFF !important',
        //        iconCls: "pwnd favwinIcon",
//        layout: "fit",
        title: 'Configure Product Items ', //"Edit Your Content",
        modal: true,
        items: [
            {
                border:false,
                height:80,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                items:[{
                    xtype:"panel",
                    border:false,
                    height:70,
                    html:"<div style = 'width:100%;height:100%;position:relative;float:left;'><div style='float:left;height:100%;width:auto;position:relative;margin:5px 0px 0px 0px;'>"
                         +"<img src = ../../images/import.png style='height:52px;margin:5px;width:40px;'></img></div><div style='float:left;height:100%;width:90%;position:relative;'>"
                         +"<div style='font-size:12px;font-style:bold;float:left;margin:7px 0px 0px 10px;width:100%;position:relative;'><b>"+((this.reportStore.data.items.length > 0)?"Edit Line Table":"Create Line Table")+"</b></div>"
                         +"<div style='font-size:10px;float:left;margin:5px 0px 10px 10px;width:100%;position:relative;'><ul style='list-style-type:disc;padding-left:15px;'>"
                         +"<li>Select a column from <b>Select Field</b> and click on <b>Add</b> to add specific column to Line Table</li>"
                         +"<li>Click on <b>Add Formula</b> to create formula and add it as column to Line Table</li></ul></div></div></div>"
                }]
            },
            {
                xtype: 'fieldset',
                title: '',
                anchor: '100%',
                height: 45,
                layout : 'column',
                bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF !important;background: #FFFFFF !important',
                items:[
                    {
                        columnWidth : .33,
                        border:false,
                        items:this.selectField
                    },
                    {
                        columnWidth : .07,
                        border:false,
                        items:this.addBtn
                    },
                    {
                        xtype: 'box',
                        autoEl : {
                            tag : 'hr'
                        },
                        height:'70%'
                    },
                    {
                        columnWidth : .12,
                        border:false,
                        items:this.addFormulaBtn
                    },
                    {
                        xtype: 'box',
                        autoEl : {
                            tag : 'hr'
                        },
                        height:'70%'
                    },
                    {
                        columnWidth : .30,
                        border:false,
                        items:this.addBlankColumnBtn
                    }
                ]
            },
            this.reportGrid
        ],
        buttons: [{
                text: "Summary Table",
                scope: this,
                handler: function(){
                    var store = this.reportGrid.getStore();
                    if(store){
                        var records = store.data.items;
                        var cnt = 0;
                        for(var i=0 ; i < records.length ; i++ ){
                            var rec= records[i];
                            if(rec.data){
                                if(!rec.data.hidecol){
                                    cnt++;
                                }
                            }
                        }
                        if(cnt>0){
                            this.createGlobalTable(cnt);
                        } else{
                            Ext.MessageBox.show({
                                title: 'Alert',
                                msg: 'Please select at least one column',
                                icon: Ext.MessageBox.QUESTION,
                                buttons: Ext.MessageBox.OK
                            });
                        }
                    }
                }
            },{
                text: "Border Property",
                scope: this,
                handler: this.tableproperty
            }, {
                text: "OK",
                scope: this,
                handler:function(){
                var totalWidth=0;
                    if(Ext.getCmp(selectedElement).isPanel){//To remove space(&nbsp;) from section if empty(No any element inside). //Check for insert Line Item table in empty section
                        Ext.getCmp(selectedElement).update("");
                    }
                    if(Ext.borderproperties!= undefined && getEXTComponent('idExtendedBorderBtn')){
                        var borderStyle=Ext.borderproperties.tableproperties.borderstylemode;
                        if(borderStyle=="borderstylemode1"||borderStyle=="borderstylemode2"||borderStyle=="borderstylemode3"){
                            getEXTComponent('idExtendedBorderBtn').disable();
                        }else{
                            getEXTComponent('idExtendedBorderBtn').enable();
                        }
                    }
                    var store = this.reportGrid.getStore();
                    if(store){
                        var records = store.data.items;
                        var cnt = 0;
                        for(var i=0 ; i < records.length ; i++ ){
                            var rec= records[i];
                            if(rec.data){
                                if(!rec.data.hidecol){
                                    cnt++;
                                }
                                totalWidth += parseInt(rec.data.colwidth, 10);
                            }
                        }
                       
                    if(cnt>0 || totalWidth != 100){
                        if(summaryTableJson && summaryTableJson.columnCount > cnt){
                                    Ext.MessageBox.show({
                                        title: 'Alert',
                                        msg: 'Summary table column count should be less than or equal to line items column count ',
                                        icon: Ext.MessageBox.QUESTION,
                                        buttons: Ext.MessageBox.OK
                                    });
                        }else if(totalWidth != 100){
                            Ext.MessageBox.show({
                                title: 'Alert',
                                msg: 'Sum of widths of line table columns should be equal to 100 ',
                                icon: Ext.MessageBox.INFO,
                                buttons: Ext.MessageBox.OK
                            });
                                }
                        else{
                                this.okClicked();
                            }
                        } else{
                            Ext.MessageBox.show({
                                title: 'Alert',
                                msg: 'Please select at least one column',
                                icon: Ext.MessageBox.QUESTION,
                                buttons: Ext.MessageBox.OK
                            });
                        }
                    }
                } 
            }, {
                text: "Cancel",
                scope: this,
                handler: this.cancelClicked
            }]
    });
};
Ext.tablelinegrid = [];

function addSummaryGlobalTable(cnt) {
    Ext.application({
        name: 'summaryGlobalTable',
        appFolder: 'app',
        autoCreateViewport: false,
        controllers: [
            'SummaryTableController'
        ],
        launch: function () {
            console.log("launch the application");
            Ext.create("SummaryGlobalTableForm.view.Form")
                }
    });
    if( Ext.getCmp('columncntid') ){
        Ext.getCmp('columncntid').maxValue = cnt;
    }
}

Ext.define("SummaryGlobalTableForm.view.Form", {
    extend: 'Ext.window.Window',
    alias: 'widget.summaryTableForm',
    requires: ['Ext.form.Panel'],
    title: 'Table Wizard',
    autoShow: true,
    modal:true,              //ERP-19208
    width: 500,
    autoHeight: true,
    layout: 'card',
    closable: true,
    listeners: {
      'close': function () {
          if ( Ext.getCmp('bordercolorproperty') ) {
              Ext.getCmp('bordercolorproperty').close();
          }
      }  
    },
    items: [{
            id: 'card-0',
            xtype: 'configPanel'
        }, {
            id: 'card-1',
            xtype: 'tableConfig'
        }, {
            id: 'card-2',
            html: 'Yes - 3'
        }]
})

Ext.define('summaryGlobalTable.controller.SummaryTableController', {
    extend: 'Ext.app.Controller',
    init: function () {
        console.log("Controller init called");
        this.control({
            'configPanel button[id=continue]': {
                click: this.headerContinue
            },
            'tableConfig button[id=previous]': {
                click: this.headerPrevious
            },
            'tableConfig button[id=submit]': {
                click: this.headerSubmit
            }

        });
    },
    headerContinue: function (button) {
        var panel = button.up('summaryTableForm');
        var colcnt = panel.down('configPanel #columncntid').getValue();
        var rowcnt = panel.down('configPanel #rowcntid').getValue();
        var tablebordern = Ext.globaltablecolor;
//        var res = tablebordern.match(/undefined/g);
        if (Ext.globaltablecolor == undefined || Ext.globaltablecolor== "") {
            tablebordern = "#000000";
        }
        var bordereffect1 = panel.down('configPanel #border1').pressed;
        //        var bordereffect2=panel.down('configPanel #border2').pressed;
        var bordereffect3 = panel.down('configPanel #border3').pressed;
        var horizontalBorderEffect = panel.down('configPanel #horizontalBorder').pressed;
        var verticalBorderEffect = panel.down('configPanel #verticalBorder').pressed;
        var horizontalVerticalBorderEffect = panel.down('configPanel #horizontalVerticalBorder').pressed;
        var widthInType = "%";
        var cellWidth = 100;
        var globalPixelStyle = "";
        var border=true;
        var applyBorderCheckBox= Ext.getCmp('applyBorderCheckBoxid');
        if(applyBorderCheckBox != null){
            border=applyBorderCheckBox.getValue();
        }
        globalPixelStyle = "style='width:"+cellWidth+"%;'";
        if (bordereffect1 == true) {
            var tableHTML = '<table id="summaryTableID" align="center" class="globaltable" ' + globalPixelStyle + ' bordercolor="' + tablebordern + '" cellspacing="0">';
            for (var cnt = 0; cnt < rowcnt; cnt++) {
                tableHTML += '<tr >';
                for (var cnt1 = 0; cnt1 < colcnt; cnt1++) {
                    if(!border){
                        tableHTML += '<td style="border-color:'+tablebordern+' ;" valign="top" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                    } else{
                        tableHTML += '<td style="border: 1px solid ; border-color: '+tablebordern+' ;" valign="top" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                    }
                }
                tableHTML += '</tr>';
            }
            tableHTML += '</table>';
        }
//        else if(bordereffect2==true){
//            var tableHTML = '<table align="center" class="globaltable tablefirst tablelast" border="1" bordercolor="'+tablebordern+'";" cellspacing="0" ;style="border-collapse:collapse;">';
//            for(var cnt=0;cnt<rowcnt;cnt++) {
//                tableHTML += '<tr>';
//                for(var cnt1=0;cnt1<colcnt;cnt1++) {
//                    tableHTML += '<td style="width:'+cellWidth+'px;border-left: medium none; border-right: medium none;" class=""onclick="getval(this)"> R'+cnt+'C'+cnt1+' </td>';
//                }
//                tableHTML += '</tr>';
//            }
//            tableHTML += '</table>';
//        }
        else if (bordereffect3 == true) {

            var tableHTML = '<table id="summaryTableID" align="center" class="globaltable"' + globalPixelStyle + ' bordercolor="' + tablebordern + '" cellspacing="0">';
            for (var cnt = 0; cnt < rowcnt; cnt++) {
                    tableHTML += '<tr>';
                for (var cnt1 = 0; cnt1 < colcnt; cnt1++) {
                    if(!border){
                        tableHTML += '<td style="border-color: '+tablebordern+' ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                    } else{
                        if(cnt==0){
                            tableHTML += '<td style="border: 1px solid;border-left: none ;border-right: none;border-color: '+tablebordern+' ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                        } else if(cnt==(rowcnt-1)){
                            tableHTML += '<td style="border: 1px solid ;border-left: none ;border-right: none ;border-top: none ;border-color: '+tablebordern+' ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                        } else{
                            tableHTML += '<td style="border-color: '+tablebordern+' ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                        }
                    }
                }
                tableHTML += '</tr>';
            }
            tableHTML += '</table>';
        } else if (horizontalBorderEffect == true) {
            var tableHTML = '<table id="summaryTableID" align="center" class="globaltable"' + globalPixelStyle + ' bordercolor="' + tablebordern + '" cellspacing="0">';
            for (var cnt = 0; cnt < rowcnt; cnt++) {
                tableHTML += '<tr>';
                for (var cnt1 = 0; cnt1 < colcnt; cnt1++) {
                    if(!border){
                        tableHTML += '<td style="border-color: '+tablebordern+' ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                    } else{
                        tableHTML += '<td style="border: 1px solid ;border-left: none ;border-right: none ;border-color: '+tablebordern+' ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                    }
                }
                tableHTML += '</tr>';
            }
            tableHTML += '</table>';
        } else if (verticalBorderEffect == true) {
            var tableHTML = '<table id="summaryTableID" align="center" class="globaltable" ' + globalPixelStyle + ' bordercolor="' + tablebordern + '" cellspacing="0">';
            for (var cnt = 0; cnt < rowcnt; cnt++) {
                tableHTML += '<tr>';
                for (var cnt1 = 0; cnt1 < colcnt; cnt1++) {
                    if(!border){
                        tableHTML += '<td style="border-color: '+tablebordern+' ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)" > R' + cnt + 'C' + cnt1 + ' </td>';
                    } else{
                        tableHTML += '<td onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)" style="border: 1px solid ;border-bottom: none ;border-top: none ;border-color: '+tablebordern+' ;"> R' + cnt + 'C' + cnt1 + ' </td>';
                    }
                }
                tableHTML += '</tr>';
            }
            tableHTML += '</table>';
        } else if (horizontalVerticalBorderEffect == true) {
            var tableHTML = '<table id="summaryTableID" align="center" class="globaltable"' + globalPixelStyle + ' bordercolor="' + tablebordern + '" cellspacing="0">';
            for (var cnt = 0; cnt < rowcnt; cnt++) {
                tableHTML += '<tr>';
                for (var cnt1 = 0; cnt1 < colcnt; cnt1++) {
                    if(!border){
                        tableHTML += '<td style="border-color: '+tablebordern+' ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                    } else{
                        if(cnt==0){
                            if(cnt1==0){
                                tableHTML += '<td style="border: 1px solid ;border-top: none ;border-left: none ;border-color: '+tablebordern+' ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                            } else if(cnt1==(colcnt-1)){
                                tableHTML += '<td style="border: 1px solid ;border-top: none ;border-right: none ;border-color: '+tablebordern+' ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                            } else{
                                tableHTML += '<td style="border: 1px solid ;border-top: none ;border-color: '+tablebordern+' ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                            }
                        } else if(cnt==(rowcnt-1)){
                            if(cnt1==0){
                                tableHTML += '<td style="border: 1px solid ;border-bottom: none ;border-left: none ;border-color: '+tablebordern+' ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                            } else if(cnt1==(colcnt-1)){
                                tableHTML += '<td style="border: 1px solid ;border-bottom: none ;border-right: none ;border-color: '+tablebordern+' ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                            } else{
                                tableHTML += '<td style="border: 1px solid ;border-bottom: none ;border-color: '+tablebordern+' ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                            }
                        } else{
                            if(cnt1==0){
                                tableHTML += '<td style="border: 1px solid ;border-left: none ;border-color: '+tablebordern+' ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                            } else if(cnt1==(colcnt-1)){
                                tableHTML += '<td style="border: 1px solid ;border-right: none ;border-color: '+tablebordern+' ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                            } else{
                                tableHTML += '<td style="border: 1px solid ;border-color: '+tablebordern+' ;" onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                            }
                        }
                    }
                }
                tableHTML += '</tr>';
            }
            tableHTML += '</table>';
        } else {
            var tableHTML = "";
            tableHTML = '<table id="summaryTableID" align="center" class="globaltablerepeat"' + globalPixelStyle + ' border="1" bordercolor="' + tablebordern + '"  cellspacing="0"  style="border:1px solid black; cursor:pointer; border-color:' + tablebordern + ';">';
            if(!border){
                tableHTML = '<table align="center" class="globaltablerepeat"' + globalPixelStyle + ' border="0" bordercolor="' + tablebordern + '"  cellspacing="0"  style="border:1px solid black; cursor:pointer; border-color:' + tablebordern + ';">';
            }
            for (var cnt = 0; cnt < rowcnt; cnt++) {
                tableHTML += '<tr>';
                for (var cnt1 = 0; cnt1 < colcnt; cnt1++) {
                    tableHTML += '<td onmouseover="getval1(event, this,'+cnt+','+cnt1+','+rowcnt+','+colcnt+',true)" onmouseleave="destroydiv(this)"> R' + cnt + 'C' + cnt1 + ' </td>';
                }
                tableHTML += '</tr>';
            }
        }
        tableHTML += '</table>';
        panel.down('tableConfig #tablehtml').setValue(tableHTML);
        panel.getLayout().setActiveItem(1);
        panel.doLayout();
    },
    headerPrevious: function (button) {
        var panel = button.up('summaryTableForm');
        panel.getLayout().setActiveItem(0);
    },
    headerSubmit: function (button) {
        var panel = button.up('summaryTableForm');
        var colcnt = panel.down('configPanel #columncntid').getValue();
        var rowcnt = panel.down('configPanel #rowcntid').getValue();
        var tableHTML = Ext.getCmp('paneltable').body.dom.innerHTML;
        var bordereffect1 = panel.down('configPanel #border1').pressed;
        var bordereffect3 = panel.down('configPanel #border3').pressed;
        var horizontalBorderEffect = panel.down('configPanel #horizontalBorder').pressed;
        var verticalBorderEffect = panel.down('configPanel #verticalBorder').pressed;
        var horizontalVerticalBorderEffect = panel.down('configPanel #horizontalVerticalBorder').pressed;
        var jsonObject = {};
        
        var borderStyle = (bordereffect1)?"border1":(bordereffect3)?"border3":(horizontalBorderEffect)?"horizontalBorderEffect":(verticalBorderEffect)?"verticalBorderEffect":(horizontalVerticalBorderEffect)?"horizontalVerticalBorderEffect":"";
        var borderColor = "#" + Ext.globaltablecolor;
        var res = borderColor.match(/undefined/g);
        if (res != null) {
            borderColor = "#000000";
        }
        
        jsonObject["columnCount"]=colcnt;
        jsonObject["rowCount"]=rowcnt;
        jsonObject["borderStyle"]=borderStyle;
        jsonObject["borderColor"]=borderColor;
        jsonObject["html"]=tableHTML;
        summaryTableJson = jsonObject;
        panel.close();
        if ( Ext.getCmp('bordercolorproperty') ) {
            Ext.getCmp('bordercolorproperty').close();
        }
    }

});

Ext.define('summaryGlobalTable.view.configPanel', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.configPanel',
    title: 'Step 1: Configure Table',
    layout: {
        type: 'hbox',
        align: 'stretch'
    },
    bodyStyle: 'background-color:#ffffff;padding:15px 5px 5px 20px;',
    bbar: [{
            xtype: 'button',
            id: 'continue',
            text: 'Next',
            iconCls: 'icon-go'
        }
    ],
    items: [{
            xtype: 'label',
            text: 'Create an empty table with',
            height : 25,
            style : {
                color : 'black',
                textAlign: 'center',
                'font-weight':'bold'
            }
        }, {
            xtype: 'numberfield',
            anchor: '100%',
            name: 'columnscnt',
            itemId: 'columncntid',
            id: 'columncntid',
            fieldLabel: 'Columns',
            value: 1,
            maxValue: 10,
            minValue: 1
        }, {
            xtype: 'numberfield',
            anchor: '100%',
            name: 'rowscnt',
            itemId: 'rowcntid',
            id: 'rowcntid',
            fieldLabel: 'Rows',
            value: 1,
            maxValue: 10,
            minValue: 1
        }, 
        {
//            xtype: 'panel',
//            itemId: 'bordercolor',
//            dataIndex: 'newheaderpropertyaaa',
//            height: 40,
//            border: false,
//            text: 'Choose Color',
//            titleAlign: "Center",
//            titleCollapse: true,
//            anchor: '100%',
//            html: "<a style='color:red'onclick='colorpanel()' href = '#' class=''><b>Table border color</b></a>",
//            scope: this
//            
            xtype: 'panel',
            border: false,
            anchor: '100%',
            layout: 'column',
            items: [{
                    xtype: 'panel',
                    itemId: 'bordercolor',
                    dataIndex: 'newheaderpropertyaaa',
                    height: 40,
                    border: false,
                    text: 'Choose Color',
                    titleAlign: "Center",
                    titleCollapse: true,
                    html: "<span>Table border color</span>",
                    scope: this
                }, {
                    xtype: 'panel',
                    height: 19,
                    id: 'idSelectedTableBorder',
                    border: false,
                    width: 62,
                    bodyStyle: 'margin-left:10px; margin-top:2px ;background-color:#000000;'
                }, {
                    xtype: 'AdvanceColorPicker',
                    id: 'idtablebordercolor',
                    iconCls: 'edit-table-property-icon',
                    width: 80,
                    height: 19,
                    luminanceImg: '../../images/luminance.png',
                    spectrumImg: '../../images/spectrum.png',
                    value: (Ext.globaltablecolor == undefined || Ext.globaltablecolor == "") ? '#000000' : Ext.globaltablecolor,
                    // itemId: 'bordercolor',
                    isBackgroundColor: true,
                    listeners: {
                        scope: this,
                        change: function (obj, newVal) {
                            Ext.globaltablecolor = newVal;
                            Ext.getCmp('idSelectedTableBorder').body.setStyle('background-color', newVal);                            
                        }
                    }
                }],
            scope: this

        },
        {
            fieldLabel: 'Apply Border',
            xtype: 'checkbox',
            name: 'applyBorderCheckBoxid',
            id: 'applyBorderCheckBoxid',
            disabled: false,
            checked : true,
            listeners :{
                    change: function (obj, newvalue, oldvalue) {
                         var borderStyleComponent = Ext.getCmp('borderStyleFieldset');
                         if(borderStyleComponent != null){
                            if(Ext.getCmp('applyBorderCheckBoxid').checked){
                                    borderStyleComponent.enable(true);
                            } else {
                                    borderStyleComponent.disable(true);
                            }
                         } 
                    }
            }
        },
        {
            xtype: 'fieldset',
            columns: 3,
            title: 'BorderStyle',
            anchor: '100%',
            height: 210,
            id : 'borderStyleFieldset',
            bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF;',
            items: [
                {
                    xtype: 'button',
                    enableToggle: true,
                    scale: 'large',
                    scope: this,
                    rowspan: 3,
                    height: 63,
                    width: 74,
                    //                    id:'border1Img',
                    itemId: 'border1',
                    id: 'border1',
                    iconCls: 'border1Img ',
                    cls: 'table,td',
                    enableToggle: true,
                            autoHeight: true,
                    margin: '10 20 30 10',
                    toggleGroup: 'ratings',
                    pressed: true
                },
                //                {
                //                    xtype: 'button',
                //                    enableToggle: true,
                //                    scale : 'large',
                //                    scope : this,
                //                    rowspan: 3,
                //                    height: 63,
                //                    width: 74,
                ////                    id:'border2Img',
                //                    itemId: 'border2',
                //                    iconCls : 'border2Img ',
                //                    enableToggle: true,
                //                    autoHeight : true,
                //
                //                    margin:'10 20 30 20',
                //                    toggleGroup: 'ratings'
                //                },
                {
                    xtype: 'button',
                    enableToggle: true,
                    scale: 'large',
                    rowspan: 3,
                    height: 63,
                    width: 74,
                    itemId: 'border3',
                    id: 'border3',
                    //                    id:'border3Img',
                    iconCls: 'border2Img',
                    scope: this,
                    margin: '10 20 30 10',
                    enableToggle: true,
                            autoHeight: true,
                    toggleGroup: 'ratings'
                },
                {
                    xtype: 'button',
                    enableToggle: true,
                    scale: 'large',
                    scope: this,
                    rowspan: 3,
                    height: 63,
                    width: 74,
                    //                    id:'border1Img',
                    itemId: 'horizontalBorder',
                    id: 'horizontalBorder',
                    iconCls: 'horizontalborderImg',
                    cls: 'table,td',
                    enableToggle: true,
                    autoHeight: true,
                    margin: '10 20 30 10',
                    toggleGroup: 'ratings'
                },
                {
                    xtype: 'button',
                    enableToggle: true,
                    scale: 'large',
                    rowspan: 3,
                    height: 63,
                    width: 74,
                    itemId: 'verticalBorder',
                    id: 'verticalBorder',
                    //                    id:'border3Img',
                    iconCls: 'verticalborderImg',
                    scope: this,
                    margin: '10 20 30 10',
                    enableToggle: true,
                    autoHeight: true,
                    toggleGroup: 'ratings'
                },
                {
                    xtype: 'button',
                    enableToggle: true,
                    scale: 'large',
                    rowspan: 4,
                    height: 63,
                    width: 74,
                    itemId: 'horizontalVerticalBorder',
                    id: 'horizontalVerticalBorder',
                    //                    id:'border3Img',
                    iconCls: 'horizontalverticalborderImg',
                    scope: this,
                    margin: '10 20 30 10',
                    enableToggle: true,
                    autoHeight: true,
                    toggleGroup: 'ratings'
                }]

        }
    ],
    layout: 'auto'
});


Ext.define('summaryGlobalTable.view.tableConfig', {
    extend: 'Ext.Panel',
    alias: 'widget.tableConfig',
    bodyStyle: 'background-color:#ffffff;padding:15px 5px 5px 20px;',
    layout: {
        type: 'vbox',
        flex: 1,
        align: 'stretch'
    },
    bbar: [{
            xtype: 'button',
            id: 'previous',
            text: 'Previous',
            iconCls: 'icon-add'
        },
        {
            xtype: 'button',
            id: 'submit',
            text: 'Create',
            iconCls: 'icon-go'
        }],
    items: [{
            xtype: 'hiddenfield',
            itemId: 'tablehtml',
            id: 'tablehtml',
            name: 'hidden_field_1',
            text: 'value from hidden field'
        }, {
            xtype: 'panel',
            autoWidth: true,
            layout: {
                type: 'vbox',
                flex: 1,
                align: 'stretch'
            },
            height: 400,
            border: false,
            id: 'paneltable'
        }],
    listeners: {
        activate: {
            fn: function (e) {
                Ext.getCmp('paneltable').body.dom.innerHTML = this.items.items[0].rawValue;
            }
        }
    }
});

Ext.extend(Ext.ProductGridTemplateWindow, Ext.Window, {
    onRender: function (conf) {
        Ext.ProductGridTemplateWindow.superclass.onRender.call(this, conf);
        //        this.createEditor();

        //        var _iArr = [this.reportGrid];
        //        this.createVariableStores();

        //        this.add(this.reportGrid);

        
    },
    saveRecord : function(){
        var saveflag = this.createFormulaWindow.validatebeforesave();
        if(saveflag){
            var formula = this.createFormulaWindow.formulaText.value;
            var formulaValue = this.createFormulaWindow.formulaTextValue;
            var colno = this.reportStore.data.items.length + 1;
            var coltotal = false;
            var columnname = this.createFormulaWindow.measureName.value;
            var colwidth = 10;
            var decimalpoint = "2";
            var displayfield = this.createFormulaWindow.measureName.value;
            var fieldid = this.createFormulaWindow.measureName.value;
            var headercurrency = "";
            var headerproperty = "";
            var hidecol = false;
            var recordcurrency = "";
            var seq = this.reportStore.data.items.length + 1;
            var showtotal = false;
            var xtype = "2";
                    
            var rec = new commonModel({
                colno:colno,
                columnname:columnname,
                colwidth:colwidth,
                decimalpoint:decimalpoint,
                displayfield:displayfield,
                fieldid:fieldid,
                headercurrency:headercurrency,
                headerproperty:headerproperty,
                hidecol:hidecol,
                recordcurrency:recordcurrency,
                seq:seq,
                showtotal:showtotal,
                xtype:xtype,
                isformula:true,
                formula:formula,
                formulavalue:formulaValue
            });
            this.reportStore.add(rec);
            this.reportGrid.doLayout();
            this.createFormulaWindow.close();
        }
    },
    createEditor: function () {
       
        this.decimal = new Ext.form.NumberField({
            validateOnBlur: true,
            maxValue: 99,
            minValue: 0,
            maxLength: 15,
            allowBlank: false
        });

        this.columnNo = new Ext.form.NumberField({
            validateOnBlur: true,
            allowBlank: false,
            maxValue: 99,
            minValue: 1,
            scope: this,
            listeners: {
                scope: this,
                change: function (numField, newVal, oldVal, eOpts) {
                    var blankRowColumnNumberArr = this.getBlankRowColumnNumber();
                    for(var i = 0; i < blankRowColumnNumberArr.length; i++) {
                        if(blankRowColumnNumberArr[i] == newVal) {
                            WtfComMsgBox(["Alert", "You cannot merge this column with blank column."], 0);
                            numField.setValue(oldVal);
                            break;
                        }
                    }
                }
            }
        })
        this.hideCol = new Ext.grid.CheckColumn({
            header: 'Hide Column',
            dataIndex: 'hidecol',
            width: 80
        });
        //        this.reportSM = new Ext.grid.CheckboxSelectionModel({singleSelect : false});

        //        this.reportSM.on("rowselect",function(SelectionModel , rowIndex, record) {
        ////                var r = this.grid.store.getAt(rowIndex);
        //            if(record && SelectionModel.fireEvent("beforerowselect", SelectionModel, rowIndex, true) !== false){
        ////                    this.selectRange([rowIndex,0], [rowIndex, this.grid.colModel.getColumnCount()-1], keepExisting, preventViewNotify);
        ////                    this.rowSelections.add(r);
        ////                    if(!preventViewNotify){
        ////                            this.grid.view.onRowSelect(rowIndex);
        ////                    }
        //                    SelectionModel.fireEvent("rowselect", SelectionModel, rowIndex);
        //                    SelectionModel.fireEvent("selectionchange", SelectionModel, SelectionModel.getSelections(),SelectionModel.getSelectedRows(), SelectionModel.getSelectedColumns());
        //            }
        //        },this)

        var currencyStore = new Ext.data.SimpleStore({      //ERP-24827 : Provide option to set currency symbol or code for line item details
            fields: [{name:'typeid',type:'int'}, 'currency'],
            data :[[0,'Currency Symbol'],[1,'Currency Code'],[2,'Base Currency Symbol'],[3,'Base Currency Code']]
         });
         
         var headerCurrencyCombo = new Ext.form.ComboBox({ //Combo for header currency
            store: currencyStore,
            name:'headercurrencytypeid',
            displayField:'currency',
            valueField:'typeid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true,
        });
        
        var recordCurrencyCombo = new Ext.form.ComboBox({ //Combo for record currency
            store: currencyStore,
            name:'recordcurrencytypeid',
            displayField:'currency',
            valueField:'typeid',
            queryMode: 'local',
            typeAhead: true,
            triggerAction: 'all',
            selectOnFocus:true
        });
        
        this.reportCM = [
            {
                header: 'Fields', //"Column",
                dataIndex: "columnname",
                width: 150
            }, {
                header: 'Header Name', //"Display Name",
                dataIndex: "displayfield",
                editor: new Ext.form.TextField({
                    validateOnBlur: true,
                    allowBlank: false
                }),
                width: 150
            } , {
                header: 'Column Width(%)', //"Column Width(%)",
                dataIndex: "colwidth",
                editor: new Ext.form.NumberField({
                    validateOnBlur: true,
                    allowBlank: false,
                    maxValue: 100,
                    minValue: 0
                }),
                width: 80
            },{
                header: 'Column No.', 
                dataIndex: "colno",
                editor: this.columnNo,
                width: 80
            },{
                header: 'Hide Column',
                dataIndex: 'hidecol',
                xtype: 'checkcolumn',
                width: 80,
                hidden:true,
                listeners: {
                     scope:this, 
                    'checkchange' : function (e, rowindex, ischecked){
                        this.hideColumnAfterEdit(this, rowindex, ischecked);
                    }
                }
            }, {
                header: 'Header with Currency',
                dataIndex: 'headercurrency',
                width: 80,
                renderer:
                        function (val, m, rec) {
                            if(rec.data.columnname == "Specific Currency Amount" || rec.data.columnname == "Specific Currency Discount" || rec.data.columnname == "Specific Currency Exchange Rate" || rec.data.columnname == "Specific Currency SubTotal" || rec.data.columnname == "Specific Currency SubTotal-Discount" || rec.data.columnname == "Specific Currency Tax Amount" || rec.data.columnname == "Specific Currency Unit Price") {
                                rec.data.headercurrency = "";
                                headerCurrencyCombo.setValue('');
                                return '';
                            } else {
                            var isAmountColumn = false;
                            if (val == '' || val == "false")
                                val = false;
                            else
                                val = true;
                            if (rec.data.columnname == "Amount" || rec.data.columnname == "Rate" || rec.data.columnname == "Rate Including GST" || rec.data.columnname == "Tax" || rec.data.columnname == "Discount"||rec.data.columnname == "Sub Total"||
                                rec.data.columnname == "Sub Total-Disc" || rec.data.columnname == "Balance Amount in Base Currency" || rec.data.columnname == "Credit Amount" || rec.data.columnname == "Credit Amount in Base Currency" 
                                || rec.data.columnname == "Debit Amount" || rec.data.columnname == "Debit Amount in Base Currency" || rec.data.columnname == "Partial Payment" || rec.data.columnname == "Transaction Balance Amount" || rec.data.columnname == "Original Amount"
                                || rec.data.columnname=="Exchange Rate Unit Price" || rec.data.columnname=="Exchange Rate SubTotal"|| rec.data.columnname=="Exchange Rate Amount" || rec.data.columnname=="Exchange Rate Subtotal without Discount"
                                || rec.data.columnname=="Exchange Rate Tax Amount" || rec.data.columnname=="Exchange Rate Discount" || rec.data.columnname=="Exchange Rate" || rec.data.columnname=="Amount Due" || rec.data.columnname == "Sub Total+Tax") {
//                                return (new Ext.ux.CheckColumn()).renderer(val);
                                isAmountColumn = true;
                            } else if(rec.data.isformula && rec.data.xtype == "2"){
                                isAmountColumn = true;
                            } else {
                                rec.data.headercurrency = "";
                                headerCurrencyCombo.setValue('');
                                return '';
                            }
                            
                            if(isAmountColumn){
                                headerCurrencyCombo.setValue(rec.data.headercurrency); //set selected value to combo
                                if(rec.data.headercurrency !== "" && rec.data.headercurrency != null){
                                    rec.data.displayfield = rec.data.displayfield.replace(/#Currency&nbsp;Symbol#/g, "!##");
                                    rec.data.displayfield = rec.data.displayfield.replace(/#Currency Symbol#/g, "!##");
                                    rec.data.displayfield = rec.data.displayfield.replace(/#Currency&nbsp;Code#/g, "!##");
                                    rec.data.displayfield = rec.data.displayfield.replace(/#Currency Code#/g, "!##");
                                    // Base Currency
                                    rec.data.displayfield = rec.data.displayfield.replace(/#Base&nbsp;Currency&nbsp;Symbol#/g, "!##");
                                    rec.data.displayfield = rec.data.displayfield.replace(/#Base Currency Symbol#/g, "!##");
                                    rec.data.displayfield = rec.data.displayfield.replace(/#Base&nbsp;Currency&nbsp;Code#/g, "!##");
                                    rec.data.displayfield = rec.data.displayfield.replace(/#Base Currency Code#/g, "!##");
                                    
                                    if(rec.data.displayfield.indexOf("!##") == -1){
                                        rec.data.displayfield = rec.data.displayfield + "#" +headerCurrencyCombo.getDisplayValue()+ "#";
                                    } else{
                                        rec.data.displayfield = rec.data.displayfield.replace("!##", "#" +headerCurrencyCombo.getDisplayValue()+ "#");
                                    }
                                } else{
                                    rec.data.displayfield = rec.data.displayfield.replace(/#Currency&nbsp;Symbol#/g, " ");
                                    rec.data.displayfield = rec.data.displayfield.replace(/#Currency Symbol#/g, " ");
                                    rec.data.displayfield = rec.data.displayfield.replace(/#Currency&nbsp;Code#/g, " ");
                                    rec.data.displayfield = rec.data.displayfield.replace(/#Currency Code#/g, " ");
                                    // Base Currency
                                    rec.data.displayfield = rec.data.displayfield.replace(/#Base&nbsp;Currency&nbsp;Symbol#/g, " ");
                                    rec.data.displayfield = rec.data.displayfield.replace(/#Base Currency Symbol#/g,  " ");
                                    rec.data.displayfield = rec.data.displayfield.replace(/#Base&nbsp;Currency&nbsp;Code#/g, " ");
                                    rec.data.displayfield = rec.data.displayfield.replace(/#Base Currency Code#/g, " ");
                                }
                                return (headerCurrencyCombo.getDisplayValue()); //return selected value (Symbol/Code)
                            }
                        }
                        },
               editor:headerCurrencyCombo //Combo editor for currency
            }, {
                header: 'Records with Currency',
                dataIndex: 'recordcurrency',
                width: 80,
                renderer:
                        function (val, m, rec) {
                            if(rec.data.columnname == "Specific Currency Amount" || rec.data.columnname == "Specific Currency Discount" || rec.data.columnname == "Specific Currency Exchange Rate" || rec.data.columnname == "Specific Currency SubTotal" || rec.data.columnname == "Specific Currency SubTotal-Discount" || rec.data.columnname == "Specific Currency Tax Amount" || rec.data.columnname == "Specific Currency Unit Price") {
                                
                                fields[0].mapping = "currencyid";
                                fields[1].mapping = "name";

                                var recordCurrencyStore = new Ext.data.SimpleStore({
                                    fields: fields,
                                    data: data
                                });
                                recordCurrencyCombo.store = recordCurrencyStore;
                                recordCurrencyCombo.setValue(rec.data.recordcurrency);
                                return (recordCurrencyCombo.getDisplayValue());
                                
                            } else {
                                recordCurrencyCombo.store = currencyStore;
                            if (val == '' || val == "false")
                                val = false;
                            else
                                val = true;
                            if (rec.data.columnname == "Amount" || rec.data.columnname == "Rate" || rec.data.columnname == "Rate Including GST" || rec.data.columnname == "Tax" || rec.data.columnname == "Discount"||rec.data.columnname == "Sub Total"||
                                rec.data.columnname == "Sub Total-Disc" || rec.data.columnname == "Balance Amount in Base Currency" || rec.data.columnname == "Credit Amount" || rec.data.columnname == "Credit Amount in Base Currency" 
                                || rec.data.columnname == "Debit Amount" || rec.data.columnname == "Debit Amount in Base Currency" || rec.data.columnname == "Partial Payment" || rec.data.columnname == "Transaction Balance Amount" || rec.data.columnname == "Original Amount"
                                || rec.data.columnname=="Exchange Rate Unit Price" || rec.data.columnname=="Exchange Rate SubTotal"|| rec.data.columnname=="Exchange Rate Amount" || rec.data.columnname=="Exchange Rate Subtotal without Discount"
                                || rec.data.columnname=="Exchange Rate Tax Amount" || rec.data.columnname=="Exchange Rate Discount" || rec.data.columnname=="Exchange Rate" || rec.data.columnname=="Amount Due" || rec.data.columnname == "Sub Total+Tax") {
//                                return (new Ext.ux.CheckColumn()).renderer(val);
                                recordCurrencyCombo.setValue(rec.data.recordcurrency); //set selected value to combo
                                return (recordCurrencyCombo.getDisplayValue()); //return selected value (Symbol/Code)
                            } else if(rec.data.isformula && rec.data.xtype == "2"){
                                recordCurrencyCombo.setValue(rec.data.recordcurrency); //set selected value to combo
                                return (recordCurrencyCombo.getDisplayValue()); //return selected value (Symbol/Code)
                            } else {
                                rec.data.recordcurrency = "";
                                recordCurrencyCombo.setValue('');
                                return '';
                            }
                        }
                        },
               editor:recordCurrencyCombo //Combo editor for currency
            }, {
            header: 'Decimal Points', //"Decimal Points",
            dataIndex: "decimalpoint",
            editor: this.decimal,
            width: 80
            },{
                header: 'Amount in Comma',
                dataIndex: 'commaamount',
                xtype: 'checkcolumn',
                width: 80,
                renderer : function(val, m, rec){
                    if (val == ''|| val == "false" || val == undefined)
                        val= false;
                    else
                        val=true;
                    
                    var str = rec.data.columnname;
                    str = str.toLowerCase();
                    if (numberFieldsArr.indexOf(str) > -1) {
                        /*
                        * for fields having xtype 1 but we have to apply decimal and comma fields to them
                        */
                        return (new Ext.ux.CheckColumn()).renderer(val);
                    } else if(rec.data.xtype == "2"){
                        /*
                        * for numeric type custom fields decimal precision should be editable
                        * for formula field then decimal precision should be editable
                        * for Default fields with xtype 2
                        */
                        return (new Ext.ux.CheckColumn()).renderer(val);
                    } else{
                        return '';
                    }
                }
            }, {
                header: 'Show Total',
                dataIndex: 'showtotal',
                xtype: 'checkcolumn',
                width: 80,
                renderer: function (val, m, rec) {
                    if (val == '' || val == "false" || val == false) {
                        val = false;
                    } else {
                        val = true;
                    }
                    return (new Ext.ux.CheckColumn()).renderer(val);
                }
            }, {
                header: 'Total Name',
                dataIndex: 'totalname',
//                xtype: 'textfield',
                width: 80,
                editor: new Ext.form.TextField()
            }, {
                header: 'Column Total',
                dataIndex: 'coltotal',
                xtype: 'checkcolumn',
                hidden: true,
                width: 80,
                renderer: function (val, m, rec) {
                    if (val == false)
                        return '';
                    else
                        return (new Ext.ux.CheckColumn()).renderer(val);
                }
            }, {
                header: 'Actions', //"Actions",
                dataIndex: 'id',
                width: 100,
                renderer: function (value, css, record, row, column, store) {
                    var actions = "<image src='images/up.png' title='Move Up' onclick=\"beforechangeseq('" + record.get('seq') + "',0, 'customReportConfigGrid')\"/>" +
                            "<image src='images/down.png' style='padding-left:5px' title='Move Down' onclick=\"beforechangeseq('" + record.get('seq') + "',1, 'customReportConfigGrid')\"/>";
                    //                    actions +="<img class='delete' src='images/cancel_16.png' style='padding-left:5px' title='Delete Field'></img>";
                    return actions;
                }
            },
            {
                header: 'Remove',
                width: 100,
                renderer: function (value, css, record, row, column, store) {
                    return "<image src='images/Delete.png' onclick=\"beforedeleteRecord('" + record.get('columnname') + "', 'customReportConfigGrid')\"/>";
                }
            }
//            , this.header = {
//                text: "Header Property",
//                dataIndex: 'headerproperty',
//                scope: this,
//                width: 80,
//                renderer: function (val) {
//                    return "<a href = '#' class='setheaderproperty'> Set Property</a>";
//                }
//            }
        ];

        this.reportStore = new Ext.create('Ext.data.Store', {
            fields: ['columnname', 'displayfield', 'hidecol', 'seq', 'fieldid', 'coltotal', 'colwidth', 'xtype', 'headerproperty', 'showtotal', 'recordcurrency', 'headercurrency', 'decimalpoint','commaamount','colno', 'isformula', 'formula', 'formulavalue', 'totalname'],
            data: documentLineColumns, // [["Product Name","Product Name",false,'0','1'],["Product Description","Product Description",false,'1','2'],["Rate","Rate",false,'2','3'], ["Quantity","Quantity",false,'3','4'],["Total Amount","Total Amount",false,'4','5']/*,["Campaign","Campaign"]*/],
            autoLoad: true
        });
        var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
            clicksToEdit: 1
        });
        this.reportGrid = Ext.create('Ext.grid.Panel', {
            columns: this.reportCM,
            //            region: "center",
            store: this.reportStore,
            clicksToEdit: 1,
            height:280,
            renderTo: this.id,
            id: 'customReportConfigGrid', // don't delete this id
            //            sm: this.reportSM,
//            viewConfig: {
//                forceFit: true
//            },
            //            plugins:[this.hideCol],
            plugins: [cellEditing]
//            layout: 'fit'
        });

        this.reportGrid.on('cellclick', this.cellclickhandle, this);
        this.reportGrid.on('beforeedit', this.beforeEdit, this);
        this.reportGrid.on('edit', this.afterEdit, this);
        this.reportGrid.on('sortchange', this.sortChangeHandle, this);
        
    },
    sortChangeHandle: function(grid,sortInfo){
        var store = grid.grid.getStore(); 
        var recCount = store.getCount();
        for (var cnt = 0; cnt < recCount; cnt++) {
            var record = store.getAt(cnt);
            record.data.seq = cnt+1;
            record.commit();
        }
    },
    beforeEdit: function (e) {
        if (e.context.field == 'decimalpoint') {            
            var str = e.context.record.data.columnname;
            var str = str.toLowerCase();
            /*
             * Check for the String field with Numeric value
             */
            if (numberFieldsArr.indexOf(str) > -1) {
//            if (e.context.record.data.isNumeric === true) {
                /*
                 * for fields having xtype 1 but we have to apply decimal and comma fields to them
                 */
                return true;
            } else if(e.context.record.data.xtype == "2") {
                /*
                 * for numeric type custom fields decimal precision should be editable
                 * for formula field then decimal precision should be editable
                 * for Default fields with xtype 2
                 */
                return true;
            } else{
                return false;
            }
        }
        if (e.context.field == 'colno') {
//            if ( e.context.record.data.hidecol ) {
//                return false;
//            } else {
                var maxvalue = this.getmaxcolumnno(e);
                e.grid.columns[3].getEditor().setMaxValue(maxvalue);
                return true;
//            }
        }
        
       if (e.context.field == 'recordcurrency') {
            if(e.context.record.data.columnname == "Specific Currency Amount" || e.context.record.data.columnname == "Specific Currency Discount" || e.context.record.data.columnname == "Specific Currency Exchange Rate" || e.context.record.data.columnname == "Specific Currency SubTotal" || e.context.record.data.columnname == "Specific Currency SubTotal-Discount" || e.context.record.data.columnname == "Specific Currency Tax Amount" || e.context.record.data.columnname == "Specific Currency Unit Price") {        
                this.fields[0].mapping="currencyid";
                this.fields[1].mapping="name";
        
                var recordCurrencyStore = new Ext.data.SimpleStore({
                    fields: this.fields,
                    data : data
                });

                e.grid.columns[6].getEditor().bindStore(recordCurrencyStore);
        
        
                    
            } else {
                var fields = [{
                    name:'typeid',
                    type:'int'
                }, 'currency'];
                var ystore = new Ext.data.SimpleStore({     
                    fields:fields,
                    data :[[0,'Currency Symbol'],[1,'Currency Code'],[2,'Base Currency Symbol'],[3,'Base Currency Code']]
                });
                e.grid.columns[6].getEditor().bindStore(ystore);
               
            }
            return true;
        }
        if (e.context.field == 'colwidth') {
            if (e.context.record.data.colwidth == 0) {
                Ext.MessageBox.show({
                    title: 'Alert',
                    msg: "Sorry, you cannot update width as column is already merged with other column.",
                    icon: Ext.MessageBox.INFO,
                    buttons: Ext.MessageBox.OK
                });
                return false;
            }
        }
        if (e.context.field == 'totalname') {
            if (e.context.record.data.showtotal != false) {
                return false;
            }
        }
    },
    cellclickhandle: function (scope, td, cellIndex, record, tr, rowIndex, e, eOpts) {
        if (e.getTarget("a[class='setheaderproperty']")) {
            this.record = record;
            //                 for (var rowIndex=0;rowIndex<headerproperties.length;rowIndex++){
//                     record=record.index
//                   var hidetable =record.data.hidecol;
//                    if(hidetable==true){
//                           record.data[++rowIndex]= headerproperties[rowIndex].attributes;
//                    }else{
//                        if(headerproperties[rowIndex].attributes){
//                            record.data.headerproperty=headerproperties[rowIndex].attributes[9].value;
//                        }
//                    else{
//                        record.data[rowIndex]= headerproperties[--rowIndex].attributes;
//                    }
//
//                    }
//}
            var styleRecord;
            if (record.data.headerproperty) {
                styleRecord = eval('(' + record.data.headerproperty + ')');
            }
            var propPanel = new Ext.HeaderWinNew({
                headerVal: record.data.displayfield,
                record: this.record,
                currentrow: rowIndex,
                styleRecord: styleRecord

            });
            var headerbutton = new Ext.Window({
                closable: true,
                title: 'HeaderProperty',
                id: 'headerproperty',
                width: 460,
                height: 470,
                items: propPanel,
                buttons: [{
                        text: "Set",
                        scope: this,
                        handler: function () {
                            var headervalues = getHeaderlineproperties();
                            var headerObj = eval('(' + headervalues + ')');
                            var name = JSON.stringify(headerObj.changedlabel);

                            this.record.set('displayfield', headerObj.changedlabel);
                            this.record.set('headerproperty', headervalues);

                            Ext.tablelinegrid[rowIndex] = this.record.data;
                            record = this.record
                            Ext.getCmp('headerproperty').close();
                        }
                    }]
            })

            headerbutton.show();
            headerbutton.doLayout();
//            var headerbutton= new Ext.HeaderWinNew({
//                headerVal : record.data.displayfield,
//                record : record
//            });
//            headerbutton.show();
//            headerbutton.doLayout();
        } else {
//            this.afterEdit(e);
                this.record = record;
                this.record.data.displayfield = this.record.data.displayfield.replace(/&nbsp;/g,' ');
        }
        
        if (record.data.showtotal == true) {
            record.data.totalname = "";
            scope.refresh();
        }
        
        if (record.data.fieldid == '_blank') {
            if (this.reportCM[cellIndex].header == 'Header Name' || this.reportCM[cellIndex].header == 'Column Width(%)') {
                return true;
            } else {
                return false;
            }
        }
    },
    hideColumnAfterEdit: function(obj,rowindex, ischecked) {
        if(Ext.getCmp('customReportConfigGrid')){
            var grid = Ext.getCmp('customReportConfigGrid');
            var store = grid.getStore();
            if ( ischecked ) {
                  store.data.items[rowindex].data.colno = "";
            } else {
                  store.data.items[rowindex].data.colno = 1;
            }
            grid.getView().refresh();
        }
      
    },
    
    afterEdit: function ( obj ) {
        var fieldnos = obj.context.store.data.items.length;
        var grid = Ext.getCmp('customReportConfigGrid');
        var store = grid.getStore();
        var distinctcolumnarr = [];
        if (obj.context.field == 'colno') {
            for ( var index = 0,index2 = 0 ; index < fieldnos ; index++ ) {
                if ( !obj.context.store.data.items[index].data.hidecol ) {
                    var colno = obj.context.store.data.items[index].data.colno;
                    if ( !this.isinarray(colno, distinctcolumnarr) ) {
                        distinctcolumnarr[index2]=colno;
                        if(store.data.items[index].data.colwidth == 0){
                            store.data.items[index].data.colwidth = 10;
                        }
                        index2++;
                    } else {
                        store.data.items[index].data.colwidth = 0;
                    }
                }
            }
           
            for ( index = 0; index < distinctcolumnarr.length; index++ ) {
                for ( index2 = 0 ; index2 < fieldnos ; index2++ ) {
                    if ( !obj.context.store.data.items[index].data.hidecol ) {
                        if ( obj.context.store.data.items[index2].data.colno == distinctcolumnarr[index]){
//                            alert("df");
                        }
                    }
                }
            }
        } 
        if(obj.context.record != null){
                obj.context.record.set("displayfield", obj.context.record.data.displayfield);
                obj.context.record.commit();
        }
        grid.getView().refresh();
    },
    okClicked: function (obj) {
        if (this.fireEvent("okClicked", this))
            this.close();
    },
    isinarray: function ( value, array ) {
        return array.indexOf(value) > -1;
    },
    cancelClicked: function (obj) {
        this.close();
        summaryTableJson = null;
        isSummaryTableApplied = false;
    },
    createGlobalTable: function(cnt){
       addSummaryGlobalTable(cnt) 
    },
    getmaxcolumnno: function(obj) {
       var fieldnos = obj.context.store.data.items.length;
       var maxno = 0;
       for ( var index = 0 ; index < fieldnos ; index++ ) {
            if ( !obj.context.store.data.items[index].data.hidecol ) {
                maxno++;
            }
       }
       return maxno;
    },
    tableproperty: function (obj) {
        var tablebordermodeconfig="borderstylemode1";
        var tablebordercolor="#000000";
        var isRoundBorder = false
        //*In Edit mode of table property
        if (pagelayoutproperty[1].tableproperties) {
            if (pagelayoutproperty[1].tableproperties != undefined) {
                tablebordermodeconfig = pagelayoutproperty[1].tableproperties.borderstylemode;
                tablebordercolor=pagelayoutproperty[1].tableproperties.bordercolor;
                isRoundBorder=pagelayoutproperty[1].tableproperties.isRoundBorder;
            }
        }
        if (this.fireEvent("tableproperty", this))
            var tablepropPanel = new Ext.tablepanel({
                tablebordermodeconfig: tablebordermodeconfig,
                tablebordercolor:tablebordercolor,
                isRoundBorder: isRoundBorder
            });
        this.tablewin = Ext.create('Ext.window.Window', {
            title: 'Border Property',
            id: 'tablewin',
            width: 550,
            modal:true,              //ERP-19208
            height: 450,
            items: [tablepropPanel],
            buttons: [{
                    text: "Update",
                    scope: this,
                    handler: function () {
                        var colorpickernew = Ext.getCmp('tablebordercolorpicker');
                        if (colorpickernew.getValue() != undefined || colorpickernew.getValue() != "") {
                            var selectednewcolor =colorpickernew.getValue();
                            var res = selectednewcolor.match(/#/g);
                            if (res == null) {
                                selectednewcolor =  selectednewcolor;
                        }
                    }
                        var backgroundcolor="#FFFFFF";
                        if(pagelayoutproperty[1].tableproperties){//if bordercolor already given then assign that bordercolor,backgroundcolor to table
                            if(pagelayoutproperty[1].tableproperties.bordercolor){
                                backgroundcolor= pagelayoutproperty[1].tableproperties.backgroundcolor;
                            }
                        }
                    
                        var border1 = Ext.getCmp('border1Img');
                        var border2 = Ext.getCmp('border2Img');
                        var border3 = Ext.getCmp('border3Img');
                        var border4 = Ext.getCmp('border4Img');
                        var border5 = Ext.getCmp('border5Img');
                        var border6 = Ext.getCmp('border6Img');
                        var border7 = Ext.getCmp('border7Img');
                        var border8 = Ext.getCmp('border8Img');
                        var border9 = Ext.getCmp('border9Img');
                        var border10 = Ext.getCmp('border10Img');
                        var roundBorder = Ext.getCmp("applyRoundBorderCheckBox");
                        var isRoundBorder = false;
                        if ( roundBorder ) {
                            isRoundBorder = roundBorder.getValue();
                        }
                        
                        if (border1.pressed == true) {
                            this.borderstylemode = "borderstylemode1";
                        } else if (border2.pressed == true) {
                            this.borderstylemode = "borderstylemode2";
                        } else if (border3.pressed == true) {
                            this.borderstylemode = "borderstylemode3";
                        } else if (border4.pressed == true) {
                            this.borderstylemode = "borderstylemode4";
                        } else if (border5.pressed == true) {
                            this.borderstylemode = "borderstylemode5";
                        } else if (border6.pressed == true) {
                            this.borderstylemode = "borderstylemode6";
                        } else if (border7.pressed == true) {
                            this.borderstylemode = "borderstylemode7";
                        } else if (border8.pressed == true) {
                            this.borderstylemode = "borderstylemode8";
                        } else if (border9.pressed == true) {
                            this.borderstylemode = "borderstylemode9";
                        }else if (border10.pressed == true) {
                            this.borderstylemode = "borderstylemode10";
                        } else {
                            this.borderstylemode = "borderstylemode1";
                        }
                        pagelayoutproperty[1] = saveTableProperty(selectednewcolor, this.borderstylemode,backgroundcolor,isRoundBorder);

                        var table = Ext.getCmp('tablewin');
                        table.close();
                    }
                }]
        });
        this.tablewin.show();
    },
    headerproperty: function (obj) {
        if (this.fireEvent("headerproperty", this))
//            this.headerwindow.show();
            var propPanel = new Ext.HeaderWinNew({});
        var headerbutton = new Ext.Window({
            closable: true,
            title: 'Header Property',
            width: 450,
            height: 500,
            items: propPanel
        })

        headerbutton.show();
        headerbutton.doLayout();
    },
    getGridConfigSetting: function () {
        var store = this.reportGrid.getStore();
        documentLineColumns = [];

        var recCount = store.getCount();
        var arr = [];
        for (var cnt = 0; cnt < recCount; cnt++) {
            var record = store.getAt(cnt);
            documentLineColumns[cnt] = record.data;
            record.data.displayfield = record.data.displayfield.replace(/\s/g,space);
            if (record.data.displayfield.replace(/\s/g, "") == "") {
                WtfComMsgBox(["Alert", "Please enter a valid display name"], 0)
                return;
            } else if (record.data.hidecol == false && record.data.colwidth.value == "") {
                WtfComMsgBox(["Alert", "Please enter a valid width"], 0)
                return;
            }
//            if (!record.data.hidecol)
            arr.push(store.indexOf(record));
        }
        var jarray = getJSONArray(this.reportGrid, true, arr);
        return jarray;
    },
    
    getBlankRowColumnNumber: function () {
        var blankRowColumnNumberArr = new Array();
        this.reportStore.each(function (record) {
            if (record.data.fieldid == '_blank') {
                blankRowColumnNumberArr.push(record.data.colno);
            }
        });

        return blankRowColumnNumberArr;
    }
});

function beforechangeseq (seq, flag, gridid) {
    var isNO = false;
    if ( isFormattingRowPresent && !isSequnceChanged ) {
        Ext.MessageBox.show({
                title: "Warning",
                msg: "Formatting Row for this table is on. If you change the sequence, Formatting row gets reset. <br/><br/> <b>Do you want to continue ?</b>",
                width: 370,
                buttons: Ext.MessageBox.YESNO,
                animEl: 'mb9',
                fn: function(btn) {
                    if ( btn === "yes") {
                        changeseq(seq, flag, gridid);
                    } else if (btn === "no") {
                        isNO = true;
                    }
                },
                icon: Ext.MessageBox.WARNING
            });
    } else {
        changeseq(seq, flag, gridid);
    }
    if ( isNO ) {
        return false;
    }
}

function changeseq(seq, flag, gridid) {
    
    isSequnceChanged = true;
    var store = Ext.getCmp(gridid).getStore();
    var index1 = store.find('seq', seq);
    var orgseq = seq;
    if (index1 > -1) {
        if (flag == "1") {
            seq++;
        } else if (flag == "0") {
            seq--;
        }
        var record1 = store.getAt(index1);
        var index2 = store.find('seq', seq);
        if (index2 > -1) {
            var record2 = store.getAt(index2);
            store.remove(record1);
            store.remove(record2);
            if (flag == "0") {
                store.insert(index2, record1);
                store.insert(index1, record2);
            } else if (flag == "1") {
                store.insert(index1, record2);
                store.insert(index2, record1);
            }
            record1.set('seq', seq);
            record1.set('colno', seq);
            record2.set('seq', orgseq);
            record2.set('colno', orgseq);
        }
    }
}

function beforedeleteRecord (columnname, gridid) {
    var isNO = false;
    if ( isFormattingRowPresent && !isRowDeleted) {
        Ext.MessageBox.show({
                title: "Warning",
                msg: "Formatting Row for this table is on. On removing a row, Formatting Row gets reset. <br/><br/> <b>Do you want to continue ?</b>",
                width: 370,
                buttons: Ext.MessageBox.YESNO,
                animEl: 'mb9',
                fn: function(btn) {
                    if ( btn === "yes") {
                        deleteRecord(columnname, gridid);
                    } else if (btn === "no") {
                        isNO = true;
                    }
                },
                icon: Ext.MessageBox.WARNING
            });
    } else {
        deleteRecord(columnname, gridid);
    }
    if ( isNO ) {
        return false;
    }
}


function deleteRecord(columnname, gridid) {
    isRowDeleted = true;
    var store = Ext.getCmp(gridid).getStore();
    var index = getIndexByExactMatch(store, columnname, 'columnname');
    var record = store.getAt(index);
    store.remove(record);
    
    for(var i=index; i < store.data.length; i++){
        var rec = store.getAt(i);
        if (rec.data.fieldid != '_blank') {
            rec.data.seq = parseInt(rec.data.seq)-1;
        }
        rec.data.colno = parseInt(rec.data.colno)-1; //ERP-19877 : If we delete a column for line table,its column no is not changed accordingly.
    }
    Ext.getCmp(gridid).getView().refresh();
}

function getJSONArray(grid, includeLast, idxArr) {
    var indices = "";
    if (idxArr)
        indices = ":" + idxArr.join(":") + ":";
    var store = grid.getStore();
    var arr = [];
    //        var fields=store.fields;
    var len = store.getCount() - 1;
    if (includeLast)
        len++;
    for (var i = 0; i < len; i++) {
        if (idxArr && indices.indexOf(":" + i + ":") < 0)
            continue;
        var recarr = [];
        var recData = store.getAt(i).data;
        for (var prop in recData) {
            recarr.push("'" + prop + "':'" + recData[prop] + "'");
        }

        //            for(var j=0;j<fields.length;j++){
        //                var value=rec.data[fields.get(j).name];
        //                recarr.push(fields.get(j).name+":"+value);
        //            }
        arr.push("{" + recarr.join(",") + "}");
    }
    return "[" + arr.join(',') + "]";
}

/** *********************************************************************************************** */
/* 							Ext.TemplateHolder component 											*/
/** *********************************************************************************************** */

function openProdWindowAndSetConfig(containerScope, panelId, isEdit, Posx, Posy, documentLineColumns,lineitemparentpanel) {
    isSequnceChanged = false;
    isRowDeleted = false;
    var _tw = new Ext.ProductGridTemplateWindow({
        tableID: 'itemlistconfig',
        documentLineColumns: documentLineColumns
    });
    _tw.on("okClicked", function (obj) {
        var firstrowcellids = [];
        var lastrowcellids = [];
        var firstrowcells = [];
        var lastrowcells = [];
        var valObj = obj.getGridConfigSetting();
        var jArr = eval(valObj);
        var boldOrNormal = "normal", italicOrNormal = "normal", underlineOrNormal = "", fontsize="", align="center";
        var headerProperties = {};
        var headerStyle = {};
        var rowProperties = {};
        var isGroupingRowPresent = false;
        var isGroupingAfterRowPresent = false;
        var groupingRowHTML= "";
        var groupingAfterRowHTML= "";
        var tableHeaderAttributes = "";
        
        
        if (lineitemparentpanel != null) {
            if (lineitemparentpanel.items) {
                if (lineitemparentpanel.items.items) {
                    if (lineitemparentpanel.items.items[0]) {
                        var lineConfig = lineitemparentpanel.items.items[0];
                        
                        isGroupingRowPresent = lineConfig.includeProductCategory;
                        isGroupingAfterRowPresent = lineConfig.includegroupingRowAfterId;
                        groupingRowHTML = lineConfig.groupingRowHTMl;
                        groupingAfterRowHTML = lineConfig.groupingRowAfterHTML;
                        
                        headerProperties["isbold"] = lineConfig.bold;
                        headerProperties["isitalic"] = lineConfig.italic;
                        headerProperties["isunderline"] = lineConfig.underline;
                        headerProperties["fontsize"] = lineConfig.fontsize;
                        headerProperties["tablewidth"] = lineConfig.tablewidth;
                        headerProperties["marginBottom"] = lineConfig.marginBottom;
                        headerProperties["marginLeft"] = lineConfig.marginLeft;
                        headerProperties["marginRight"] = lineConfig.marginRight;
                        headerProperties["marginTop"] = lineConfig.marginTop;
                        headerProperties["bordercolor"] = lineConfig.bordercolor;
                        headerProperties["align"] = lineConfig.align;
                        tableHeaderAttributes = lineConfig.getEl().dom.children[0].children[1].children[0].attributes;// Taking header attribute
                        if(lineConfig.getEl().dom.children[0].children[1].children[0].cells){
                            var ths = lineConfig.getEl().dom.children[0].children[1].children[0].cells;
                            for(var thCnt = 0 ; thCnt < ths.length; thCnt++){
                                var thCell  = ths[thCnt];
                                headerStyle[thCell.attributes["label"].value]=thCell.style.cssText;
                            }
                        }
                        var isGroupingRow = lineConfig.getEl().dom.children[0].children[1].children[2].attributes.getNamedItem("isGroupingRow") != null ? lineConfig.getEl().dom.children[0].children[1].children[2].attributes.getNamedItem("isGroupingRow").value : "no";
                        var indexToAdd = 0;
                        if ( isGroupingRow === "yes") {
                            indexToAdd++;
                        }
                        if(lineConfig.getEl().dom.children[0].children[1].children[2+indexToAdd].cells){
                            var tds = lineConfig.getEl().dom.children[0].children[1].children[2+indexToAdd].cells;
                            for(var tdCnt = 0 ; tdCnt < tds.length; tdCnt++){
                                var tdCells  = tds[tdCnt].children;
                                for(var cellCnt = 0; cellCnt < tdCells.length; cellCnt++){
                                    var cell = tdCells[cellCnt];
                                    var cellProperties = {};
                                    var mainCell;
                                    //PreText
                                    if(cell.id.indexOf("id1")!= -1){
                                        cellProperties["preText"] = cell.innerHTML;
                                        cellProperties["preTextStyle"] = cell.style.cssText;
                                        cellCnt++;
                                        cell = tdCells[cellCnt]
                                    }
                                    mainCell = cell;
                                    cellProperties["style"] = cell.style.cssText;
                                    //PostText
                                    if(tdCells[cellCnt+1] && tdCells[cellCnt+1].id.indexOf("id2") != -1){
                                        cellCnt++;
                                        cell = tdCells[cellCnt]
                                        cellProperties["postText"] = cell.innerHTML;
                                        cellProperties["postTextStyle"] = cell.style.cssText;
                                    }
                                    rowProperties[mainCell.attributes["label"].value]=cellProperties;
                                }
                                
                            }
                        }
                        if(lineConfig.firstRowCells){
                            firstrowcells = lineConfig.firstRowCells;
                        }
                        if(lineConfig.lastRowCells){
                            lastrowcells = lineConfig.lastRowCells;
                        }
                    }
                }
            }
        }
        if(headerProperties){
            if(headerProperties.isbold && headerProperties.isbold=="true"){
                boldOrNormal="bold";
            }
            if(headerProperties.isitalic && headerProperties.isitalic=="true"){
                italicOrNormal="italic";
            }
            if(headerProperties.isunderline && headerProperties.isunderline=="true"){
                underlineOrNormal="underline";
            }
            if(headerProperties.fontsize != undefined){
                fontsize=headerProperties.fontsize;
            }
            if(headerProperties.align != undefined){
                var tempAlign=headerProperties.align;
                if(tempAlign=="left" || tempAlign===0){
                    align="left";
                } else if(tempAlign=="center" || tempAlign===1 ){
                    align="center";
                } else{
                    align="right";
               }
            }
        }
        //previous code
        //        var tab1Row = document.createElement("ul");
        //        tab1Row.setAttribute("id", "itemlistconfig"+panelId);
        //        tab1Row.setAttribute('style', 'padding-left: inherit;width:100%');
        //                var width = (100 / jArr.length);
        //            var tab2Row = document.createElement("li");
        //            tab2Row.style.textAlign=headerpropertyparam.alignment;
        //            tab2Row.style.backgroundColor=headerpropertyparam.backgroundcolor;
        //            tab2Row.setAttribute("class", "tpl-colname");
        //            var widthInPercent = (obj.colwidth-2)+"%";// decreased 2% because while rendering header in html we used marging of 2%
        //            tab2Row.setAttribute ("bgColor",headerpropertyparam.backgroundcolor);
        //            tab2Row.setAttribute('style', 'width: '+widthInPercent);
        //            tab2Row.setAttribute("colwidth", obj.colwidth);
        //            tab2Row.setAttribute("coltotal", obj.coltotal);
        //            tab2Row.setAttribute("showtotal", obj.showtotal);
        //            tab2Row.setAttribute("seq", obj.seq);
        //            tab2Row.setAttribute("xtype", obj.xtype);
        //            tab2Row.setAttribute("headerproperty", obj.headerproperty);
        //            tab2Row.setAttribute("fieldid", obj.fieldid);
        //            tab2Row.value = obj.fieldid;
        //            tab2Row.innerHTML = decodeURIComponent(obj.displayfield);
        ////            tab1Row.appendChild(tab2Row);
        //                newTH.appendChild(tab2Row);
        //                r.appendChild(newTH);
        //                tab1Row.appendChild(r);
        //                            this.addButtons();
        //                            Ext.DomHelper.insertFirst(tab1Row.outerHTML,this.defaultMenuConfig);
        //        this.setHtml(tab1Row.outerHTML);

        /*******new code--Tabular format-neeraj****/
        var c, r, t,b, firstRow;
        if(isEdit==true){
            if (document.getElementById("itemlistconfig"))
                remove(document.getElementById("itemlistconfig"));
            if (document.getElementById("itemlistcontainer"))
                remove(document.getElementById("itemlistcontainer"));
        }
            var tableConfig = document.createElement('table');
            r = tableConfig.insertRow(0);
            for(var cnt = 0; cnt < tableHeaderAttributes.length; cnt++){ // Set Header Attribute to New header
                var attrName = tableHeaderAttributes[cnt].nodeName;
                var attrValue = tableHeaderAttributes[cnt].nodeValue;
                r.setAttribute(attrName, attrValue);
            }
            firstRow = tableConfig.insertRow(1);
            t = tableConfig.insertRow(2);
            c = tableConfig.insertRow(3);
            b = tableConfig.insertRow(4);
            tableConfig.setAttribute("class", "sectionclass_element_100");
            tableConfig.setAttribute("id", "itemlistconfigsectionPanelGrid");
            tableConfig.setAttribute("panelid", panelId);
            tableConfig.setAttribute("cellSpacing", "0");
            
        if (pagelayoutproperty[1].tableproperties) {
            if (pagelayoutproperty[1].tableproperties != undefined) {
                this.tablebgcolor = pagelayoutproperty[1].tableproperties.bordercolor;
                this.borderstylemode = pagelayoutproperty[1].tableproperties.borderstylemode;
                this.backgroundcolor=pagelayoutproperty[1].tableproperties.backgroundcolor;
                this.isRoundBorder=pagelayoutproperty[1].tableproperties.isRoundBorder;
            }else{
                this.tablebgcolor = '#000000';
                this.backgroundcolor="#FFFFFF";
                this.isRoundBorder = false;
            }
        } else{
            this.tablebgcolor = '#000000'; 
            this.backgroundcolor="#FFFFFF";
            this.isRoundBorder = false;
        }
        
                tableConfig.setAttribute("borderstylemode", this.borderstylemode);
                tableConfig.setAttribute("tcolor", this.tablebgcolor);
                if (this.borderstylemode == "borderstylemode2") {
                    tableConfig.setAttribute("border", '0');
                } else if (this.borderstylemode == "borderstylemode3") {
                    if ( this.isRoundBorder ) {
                        tableConfig.setAttribute("class", "sectionclass_element_100 lineitembordereffect3 tableroundborder");
                        tableConfig.setAttribute("style", "border-color:" + this.tablebgcolor + "; " );
                    } else {
                        tableConfig.setAttribute('style', 'border-left:1px solid ' + this.tablebgcolor + ';border-right:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';border-bottom:1px solid ' + this.tablebgcolor + ';');
                        tableConfig.setAttribute("border", '1');
                    }
                } else if (this.borderstylemode == "borderstylemode4") {
                    tableConfig.setAttribute("border", '0');
                }else if (this.borderstylemode == "borderstylemode5") {
                    tableConfig.setAttribute("border", '1');
                    tableConfig.setAttribute('style', 'border-left:none;border-right:none;border-top:none;border-bottom:1px solid ' + this.tablebgcolor + ';');
                }else if (this.borderstylemode == "borderstylemode6") {
                    if ( this.isRoundBorder ) {
                        tableConfig.setAttribute("class", "sectionclass_element_100 lineitembordereffect6 tableroundborder");
                        tableConfig.setAttribute("style", "border-color:" + this.tablebgcolor + "; " );
                    } else {
                        tableConfig.setAttribute("border", '1');
                        tableConfig.setAttribute('style', 'border-left:1px solid ' + this.tablebgcolor + ';border-right:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';border-bottom:1px solid ' + this.tablebgcolor + ';');
                    }
                }else if (this.borderstylemode == "borderstylemode7") {
                   tableConfig.setAttribute("border", '1');
                   tableConfig.setAttribute('style', 'border-left:none;border-right:none;border-top:none;border-bottom:1px solid ' + this.tablebgcolor + ';');
                }else if (this.borderstylemode == "borderstylemode8") {
                    tableConfig.setAttribute("border", '0');
                }else if (this.borderstylemode == "borderstylemode9") {
                    tableConfig.setAttribute("border", '1');
                    tableConfig.setAttribute('style', 'border-left:none;border-right:none;border-top:none;border-bottom:1px solid ' + this.tablebgcolor + ';');
                }else if (this.borderstylemode == "borderstylemode10") {
                    if ( this.isRoundBorder ) {
                        tableConfig.setAttribute("class", "sectionclass_element_100 lineitembordereffect10 tableroundborder");
                        tableConfig.setAttribute("style", "border-color:" + this.tablebgcolor + "; " );
                    } else {
                        tableConfig.setAttribute("border", '1');
                        tableConfig.setAttribute('style', 'border-left:none;border-right:none;border-top:none;border-bottom:1px solid ' + this.tablebgcolor + ';');
                    }
                }else {
                    this.borderstylemode = 'borderstylemode1';
                    tableConfig.setAttribute("borderstylemode", this.borderstylemode);
                    tableConfig.setAttribute("tcolor", this.tablebgcolor);
//                    tableConfig.setAttribute("border", '1');
                    if ( this.isRoundBorder ) {
                        tableConfig.setAttribute("class", "sectionclass_element_100 lineitembordereffect1 tableroundborder");
                        tableConfig.setAttribute("style", "border-color:" + this.tablebgcolor + "; " );
                    } else {
                        tableConfig.setAttribute('style', 'border-left:1px solid ' + this.tablebgcolor + ';border-right:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';border-bottom:1px solid ' + this.tablebgcolor + ';');
                    }
                }
            if(headerProperties){
                if(headerProperties.tablewidth){
                    var tableStyle = tableConfig.getAttribute('style');
                    tableStyle = "width:"+headerProperties.tablewidth+"%;"+tableStyle;
                    tableConfig.setAttribute('style',tableStyle);
                }
            }
            var columnarr = [];
            var colnoarr = [];
            var fieldcnt = 0;
            for (var cnt = 0,cnt1=0; cnt < jArr.length; cnt++) {
                if ( !(colnoarr.indexOf(jArr[cnt].colno) > -1)) {
                    colnoarr[cnt1] = jArr[cnt].colno
                    columnarr[cnt1]=jArr[cnt];
                    cnt1++;
                }
                
            }
            for ( cnt1=0; cnt1 < columnarr.length; cnt1++) {
                fieldcnt = 0;
                var fields = [];
                for (var cnt = 0; cnt < jArr.length; cnt++) {
                    if ( columnarr[cnt1].colno == jArr[cnt].colno ) {
                        fields[fieldcnt] = cnt;
                        var obj={};
                        columnarr[cnt1]["fields"] = fields;
                        fieldcnt++;
                    }
                }
            }
            for (var cnt = 0; cnt < columnarr.length; cnt++) {
                var obj = columnarr[cnt];
                var newTH = document.createElement('th');
                newTH.setAttribute("onmouseover", "getval1(event, this)");
                newTH.setAttribute("onmouseleave", "destroydiv(this)");
                var label = obj.displayfield? obj.displayfield : obj.columnname;
                label = label.replace(/\b&nbsp;/g," ")
                if(headerStyle && headerStyle[label]){
                    newTH.setAttribute('style',headerStyle[label]);
                }
                if (columnarr[cnt].headerproperty) {
                    var headerpropertyparam = JSON.parse(columnarr[cnt].headerproperty);
                    newTH.setAttribute("bgColor", headerpropertyparam.backgroundcolor);
                    newTH.setAttribute("align", headerpropertyparam.alignment);
                    newTH.style.textAlign = headerpropertyparam.alignment;
                    newTH.innerHTML = headerpropertyparam.changedlabel.replace(/\b&nbsp;/g," ");
                    newTH.setAttribute("label", headerpropertyparam.changedlabel);
                } else {
                    newTH.innerHTML = obj.displayfield? obj.displayfield.replace(/\b&nbsp;/g," ") : obj.columnname.replace(/\b&nbsp;/g," ");
                    newTH.setAttribute("label", obj.displayfield? obj.displayfield.replace(/\b&nbsp;/g," ") : obj.columnname.replace(/\b&nbsp;/g," "));
                }
                //            newTH.setAttribute("class", "tpl-content");
                var widthInPercent = obj.colwidth + "%";// decreased 2% because while rendering header in html we used marging of 2%
                var headerstyle = '';
                if ( headerstyle ) {
                    
                }
                headerstyle = newTH.getAttribute('style');
                if (this.borderstylemode == "borderstylemode2") {
                    newTH.setAttribute('style',headerstyle +'width: ' + widthInPercent + ';border-width:thin;border-bottom:1px solid ' + this.tablebgcolor + ';border-top:1px solid' + this.tablebgcolor + ';border-left:0px none;border-right:0px none;'); // ERP-19569
                } else if (this.borderstylemode == "borderstylemode3") {
                    if ( this.isRoundBorder ) {
                        headerstyle = newTH.getAttribute('style');
                        newTH.setAttribute('style', headerstyle+"width: " + widthInPercent + "; border-color:"+this.tablebgcolor+ " !important; ");
                    } else {
                        headerstyle = newTH.getAttribute('style');    
                        newTH.setAttribute('style', headerstyle+'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom:1px solid ' + this.tablebgcolor + ';border-top:none;');
                    }
                } else if (this.borderstylemode == "borderstylemode4") {
                    headerstyle = newTH.getAttribute('style');
                    newTH.setAttribute('style', headerstyle+'width: ' + widthInPercent + ';border-width:thin;border-left:none; border-right:none;border-bottom:1px solid' + this.tablebgcolor + ';border-top:1px solid' + this.tablebgcolor + ';');
                }else if (this.borderstylemode == "borderstylemode5") {
                    headerstyle = newTH.getAttribute('style');
                    newTH.setAttribute('style', headerstyle+'width: ' + widthInPercent + ';border-width:thin;border-left:none; border-right:none;border-bottom:1px solid' + this.tablebgcolor + ';border-top:none;');
                }else if (this.borderstylemode == "borderstylemode6") {
                    if ( this.isRoundBorder ) {
                        headerstyle = newTH.getAttribute('style');    
                        newTH.setAttribute('style', headerstyle+"width: " + widthInPercent + "; border-color:"+this.tablebgcolor+ " !important; ");
                    } else {
                        headerstyle = newTH.getAttribute('style');    
                        newTH.setAttribute('style', headerstyle+'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom:1px solid ' + this.tablebgcolor + ';border-top:1px solid' + this.tablebgcolor + ';');
                    }
                }else if (this.borderstylemode == "borderstylemode7") {
                    headerstyle = newTH.getAttribute('style');
                    if ( cnt === (columnarr.length-1)) {
                        newTH.setAttribute('style', headerstyle+'width: ' + widthInPercent + ';border-width:thin;border-left:1px solid ' + this.tablebgcolor + '; border-right:1px solid ' + this.tablebgcolor + ';border-bottom:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';');
                    } else {
                        newTH.setAttribute('style', headerstyle+'width: ' + widthInPercent + ';border-width:thin;border-left:1px solid ' + this.tablebgcolor + '; border-right:none; border-bottom:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';');
                    }
                }else if (this.borderstylemode == "borderstylemode8") {
                    headerstyle = newTH.getAttribute('style');
                    if( cnt == 0 ){
                        newTH.setAttribute('style', headerstyle+'width: ' + widthInPercent + ';border-width:thin; border-left:1px solid;border-right:none;border-bottom:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';');
                    } else if( cnt ==(columnarr.length-1) ){
                        newTH.setAttribute('style', headerstyle+'width: ' + widthInPercent + ';border-width:thin;border-right:1px solid;border-left:none; border-bottom:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';');
                    } else{
                        newTH.setAttribute('style', headerstyle+'width: ' + widthInPercent + ';border-width:thin;border-left:none; border-right:none;border-bottom:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';');
                    }
                }else if (this.borderstylemode == "borderstylemode9") {
                    headerstyle = newTH.getAttribute('style');
                    newTH.setAttribute('style', headerstyle+'width: ' + widthInPercent + ';border-width:thin;border-left:none; border-right:none;border-bottom:1px solid' + this.tablebgcolor + ';border-top:1px solid' + this.tablebgcolor + ';');
                }else if (this.borderstylemode == "borderstylemode10") {
                    if ( this.isRoundBorder ) {
                        headerstyle = newTH.getAttribute('style');    
                        newTH.setAttribute('style', headerstyle+"width: " + widthInPercent + "; border-color:"+this.tablebgcolor+ " !important; ");
                    } else {
                        headerstyle = newTH.getAttribute('style');
                        if ( cnt === (columnarr.length-1)) {  /*ERP-19470*/ //check for last TH
                            newTH.setAttribute('style', headerstyle+'width: ' + widthInPercent + ';border-width:thin;border-left:1px solid ' + this.tablebgcolor + '; border-right:1px solid ' + this.tablebgcolor + ';border-bottom:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';'); 
                        } else {
                            newTH.setAttribute('style', headerstyle+'width: ' + widthInPercent + ';border-width:thin;border-left:1px solid ' + this.tablebgcolor + '; border-right:0px none ' + this.tablebgcolor + ';border-bottom:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';'); 
                        }
                    }
                }else{
                    if (this.isRoundBorder) {
                        headerstyle = newTH.getAttribute('style');
                        newTH.setAttribute('style', headerstyle+"width: " + widthInPercent + "; border-color:"+this.tablebgcolor+ " !important; ");
                    } else {
                        headerstyle = newTH.getAttribute('style');
                        newTH.setAttribute('style', headerstyle + 'width: ' + widthInPercent + ';border-width:thin;border-left:1px solid ' + this.tablebgcolor + '; border-right:1px solid ' + this.tablebgcolor + ';border-bottom:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';');
                    }
                }
                
                newTH.setAttribute("colwidth", obj.colwidth);
                newTH.setAttribute("coltotal", obj.coltotal);
                newTH.setAttribute("showtotal", obj.showtotal);
                newTH.setAttribute("totalname", obj.totalname);
                newTH.setAttribute("seq", obj.seq);
                newTH.setAttribute("xtype", obj.xtype);
                newTH.setAttribute("headerproperty", obj.headerproperty);
                newTH.setAttribute("fieldid", obj.fieldid);
                newTH.setAttribute("headercurrency", obj.headercurrency);
                newTH.setAttribute("commaamount", obj.commaamount!=""?obj.commaamount:"");
                newTH.setAttribute("recordcurrency", obj.recordcurrency);
                newTH.setAttribute("colno", obj.colno);
                
                newTH.setAttribute("decimalpoint", obj.decimalpoint != "" ? obj.decimalpoint : "")
                if (obj.columnname == "Quantity with UOM" || obj.columnname == "Actual Quantity With UOM" || obj.columnname == "Delivered Quantity With UOM" || obj.columnname == "Received Quantity With UOM" || obj.columnname == "Return Quantity With UOM") {
                    newTH.setAttribute("basequantitywithuom", true);
                } else {
                    newTH.setAttribute("basequantitywithuom", false);
                }

                newTH.value = obj.fieldid;
                newTH.cellIndex = obj.seq;
//                newTH.setAttribute("align", align);
//                newTH.style.textAlign = align;
//                newTH.style.position = "relative";
                if ( !fontsize ) {
                    fontsize = "";
                }
                newTH.style.fontSize = fontsize + "px";
//                newTH.style.textDecoration = underlineOrNormal;
//                if (headerProperties) {
//                    if (headerProperties.bordercolor) {
//                        r.bgColor = headerProperties.bordercolor;
//                    }
//                }
//                var label = obj.displayfield? obj.displayfield : obj.columnname;
//                if(headerStyle && headerStyle[label]){
//                    newTH.setAttribute('style',headerStyle[label]);
//                }
                r.appendChild(newTH);

               /*Second Row*/
               
                var tabletd = document.createElement('td');
                tabletd.cellIndex = newTH.cellIndex;
                tabletd.setAttribute("cellIndex", newTH.cellIndex);
//                tabletd.innerHTML = "&nbsp;";
                if (this.borderstylemode == "borderstylemode2") {
                    tabletd.setAttribute('style', 'padding:5px; width: ' + widthInPercent + ';border-width: thin;border-bottom:1px solid ' + this.tablebgcolor + ';');
                } else if (this.borderstylemode == "borderstylemode3") {
                    if (  this.isRoundBorder ) {
                        tabletd.setAttribute('style', "border-color:"+this.tablebgcolor+ " !important; ");
                    } else {
                            tabletd.setAttribute('style', 'padding:5px; width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom:1px solid ' + this.tablebgcolor + ';border-top:none;');
                        }
                } else if (this.borderstylemode == "borderstylemode4") {
                    tabletd.setAttribute('style', 'padding:5px; width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom: none;border-top: none;');
                }else if (this.borderstylemode == "borderstylemode5") {
                    tabletd.setAttribute('style', 'padding:5px; width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom: none;border-top: none;');
                }else if (this.borderstylemode == "borderstylemode6") {
                    if ( this.isRoundBorder) {
                        tabletd.setAttribute('style', "border-color:"+this.tablebgcolor+ " !important; ");
                    } else {
                        tabletd.setAttribute('style', 'padding:5px; width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom: none;border-top: none;');
                    }
                }else if (this.borderstylemode == "borderstylemode7") {
                    tabletd.setAttribute('style', 'padding:5px; width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom: none;border-top: none;');
                }else if (this.borderstylemode == "borderstylemode8") {
                    tabletd.setAttribute('style', 'padding:5px; width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom: none;border-top: none;');
                }else if (this.borderstylemode == "borderstylemode9") {
                    tabletd.setAttribute('style', 'padding:5px; width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom:none;border-top: none;');
                }else if (this.borderstylemode == "borderstylemode10") {
                    if ( this.isRoundBorder ) {
                        tabletd.setAttribute('style', "border-color:"+this.tablebgcolor+ " !important; ");
                    } else {
                        if ( cnt === (columnarr.length-1)) {  /*ERP-19470*/ //check for last TD
                            tabletd.setAttribute('style', 'padding:5px; width: ' + widthInPercent + ';border-width:thin;border-left: 1px solid ' + this.tablebgcolor + '; border-right: 1px solid ' + this.tablebgcolor + ';border-bottom:none;border-top: none;');
                        } else{
                            tabletd.setAttribute('style', 'padding:5px; width: ' + widthInPercent + ';border-width:thin;border-left: 1px solid ' + this.tablebgcolor + '; border-right: 0px none ' + this.tablebgcolor + ';border-bottom:none;border-top: none;');
                        }
                    }
                }else {
                    if (this.isRoundBorder) {
                        tabletd.setAttribute('style', "border-color:"+this.tablebgcolor+ " !important; ");
                    } else {
                        tabletd.setAttribute('style', 'padding:5px; width: ' + widthInPercent + ';border-width:thin;border-left:1px solid ' + this.tablebgcolor + '; border-right:1px solid ' + this.tablebgcolor + ';border-bottom:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';');
                    }
                }
                tabletd.setAttribute("colwidth", obj.colwidth);
                for (var fieldcnt = 0 ; fieldcnt < columnarr[cnt].fields.length ; fieldcnt++) {
                    var div = document.createElement('div');
                    var obj1 = jArr[columnarr[cnt].fields[fieldcnt]];
                        if(obj1.xtype == "imageTag"){
                            div.innerHTML = "<img src='../../images/designer/product-image.png' style='height:100%;width:100%;'></img>";
                            div.setAttribute("onclick", "getPropertyPanelForLineItemFields(event,this,false,true)");
                            div.setAttribute("type", "3");
                            div.setAttribute("style", "width:100px;height:80px;");
                        } else{
                            div.innerHTML = jArr[columnarr[cnt].fields[fieldcnt]].columnname;
                            div.setAttribute("onclick", "getPropertyPanelForLineItemFields(event,this)");
                            div.setAttribute("style", "width:100%");
                        }
                        div.setAttribute("label", obj1.displayfield);
                        div.setAttribute("class", "fieldItemDivBorderForLineTable");
                        div.setAttribute("colno", obj1.colno);
                        div.setAttribute("colwidth", obj1.colwidth);
                        div.setAttribute("coltotal", obj1.coltotal);
                        div.setAttribute("showtotal", obj1.showtotal);
                        div.setAttribute("totalname", obj1.totalname);
                        div.setAttribute("seq", obj1.seq);
                        div.setAttribute("xtype", obj1.xtype);
                        div.setAttribute("headerproperty", obj1.headerproperty);
                        div.setAttribute("fieldid", obj1.fieldid);
                        div.setAttribute("headercurrency", obj1.headercurrency);
                        div.setAttribute("recordcurrency", obj1.recordcurrency);
                        div.setAttribute("commaamount", obj1.commaamount);
                        div.setAttribute("colno", obj1.colno);
                        div.setAttribute("columnname", obj1.columnname);
                        div.setAttribute("decimalpoint", obj1.decimalpoint != "" ? obj1.decimalpoint : "")
                        var isFormula = obj1.isformula ? (obj1.isformula == "true" ? true : false) : false;
                        if(isFormula){
                            div.setAttribute("isformula", isFormula);
                            div.setAttribute("formula", obj1.formula ? obj1.formula : "");
                            div.setAttribute("formulavalue", obj1.formulavalue ? obj1.formulavalue : "");
                            div.setAttribute("type", "4");
                        }
                        if (obj1.columnname == "Quantity with UOM" || obj1.columnname == "Actual Quantity With UOM" || obj1.columnname == "Delivered Quantity With UOM" || obj1.columnname == "Received Quantity With UOM" || obj1.columnname == "Return Quantity With UOM") {
                            div.setAttribute("basequantitywithuom", true);
                        } else {
                            div.setAttribute("basequantitywithuom", false);
                        }
                        tabletd.appendChild(div);
                        label = obj1.displayfield? obj1.displayfield : obj1.columnname;
                        if(rowProperties[label]){
                            var properties = rowProperties[label];
                            if(properties.style){
                                div.setAttribute("style", properties.style);
                            }
                            if(properties.preText != undefined){
                                addPrePostText(div,1,properties.preText , properties.preTextStyle);
                            }
                            if(properties.postText != undefined){
                                addPrePostText(div,2, properties.postText, properties.postTextStyle);
                            }
                        }
                }
                t.appendChild(tabletd);
                
                /*Third Row*/
                var tabletd = document.createElement('td');
                tabletd.cellIndex = newTH.cellIndex;
                var lrid = "lastRowid"+cnt;
                lastrowcellids.push(lrid);
                tabletd.setAttribute("cellIndex", newTH.cellIndex);
                tabletd.setAttribute("id", lrid);
//                tabletd.innerHTML = "&nbsp;";
                if (this.borderstylemode == "borderstylemode2") {
                    tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width: thin;border-bottom:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';');
                } else if (this.borderstylemode == "borderstylemode3") {
                    if ( this.isRoundBorder ) {
                        tabletd.setAttribute('style', "border-color:"+this.tablebgcolor+ " !important; ");
                    } else {
                        if(cnt == 0){
                            tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: 1px solid; border-right: none;border-bottom:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';');//ERP-18680 : [Document Designer] Line Item Global rows issue
                        } else if(cnt == columnarr.length-1){
                            tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: 1px solid;border-bottom:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';');//ERP-18680 : [Document Designer] Line Item Global rows issue
                        } else{
                            tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';');
                        }
                    }
                } else if (this.borderstylemode == "borderstylemode4") {
                    tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom: none;border-top: none ' + this.tablebgcolor + ';');//changed 'border-top: none' because ERP-18680 : [Document Designer] Line Item Global rows issue
                }else if (this.borderstylemode == "borderstylemode5") {
                    tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom: 1px solid;border-top: none ' + this.tablebgcolor + ';');//changed 'border-top: none' and 'border-bottom: 1px solid' because ERP-18680 : [Document Designer] Line Item Global rows issue
                }else if (this.borderstylemode == "borderstylemode6") {
                    if ( this.isRoundBorder) {
                        tabletd.setAttribute('style', "border-color:"+this.tablebgcolor+ " !important; ");
                    } else {
                        if(cnt == 0){
                            tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: 1px solid; border-right: none;border-bottom: 1px solid;border-top:none ' + this.tablebgcolor + ';');//ERP-18680 : [Document Designer] Line Item Global rows issue
                        } else if(cnt == columnarr.length-1){
                            tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: 1px solid;border-bottom: 1px solid;border-top:none ' + this.tablebgcolor + ';');//ERP-18680 : [Document Designer] Line Item Global rows issue
                        } else{
                            tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom: 1px solid;border-top:none ' + this.tablebgcolor + ';');//ERP-18680 : [Document Designer] Line Item Global rows issue
                        }
                    }
                }else if (this.borderstylemode == "borderstylemode7") {
                    tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom: 1px solid;border-top:none ' + this.tablebgcolor + ';');//changed 'border-top: none' and 'border-bottom: 1px solid' because ERP-18680 : [Document Designer] Line Item Global rows issue
                }else if (this.borderstylemode == "borderstylemode8") {
                    tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom: none;border-top:none ' + this.tablebgcolor + ';');//changed 'border-top: none' because ERP-18680 : [Document Designer] Line Item Global rows issue
                }else if (this.borderstylemode == "borderstylemode9") {
                   tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom:1px solid;border-top:none ' + this.tablebgcolor + ';');//changed 'border-top: none' and 'border-bottom: 1px solid' because ERP-18680 : [Document Designer] Line Item Global rows issue
                }else if (this.borderstylemode == "borderstylemode10") {
                    if ( this.isRoundBorder ) {
                        tabletd.setAttribute('style', "border-color:"+this.tablebgcolor+ " !important; ");
                    } else {
                            tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: 1px solid ' + this.tablebgcolor + '; border-right: 1px solid ' + this.tablebgcolor + ';border-bottom:1px solid ' + this.tablebgcolor + ';border-top:none;');//changed 'border-bottom: 1px solid' because ERP-18680 : [Document Designer] Line Item Global rows issue
                        }
                }else {
                    if (this.isRoundBorder) {
                        tabletd.setAttribute('style', "border-color:"+this.tablebgcolor+ " !important; ");
                    } else {
                            tabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left:1px solid ' + this.tablebgcolor + '; border-right:1px solid ' + this.tablebgcolor + ';border-bottom:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';');
                        }
                    }
                tabletd.setAttribute("colwidth", obj.colwidth);
                c.appendChild(tabletd);
                
                var firstrowtabletd = document.createElement('td');
                firstrowtabletd.cellIndex = newTH.cellIndex;
                var frid = "firstRowid"+cnt;
                firstrowcellids.push(frid);
                firstrowtabletd.setAttribute("cellIndex", newTH.cellIndex);
                firstrowtabletd.setAttribute("id", frid);
                firstrowtabletd.setAttribute("isLineItemWithPrefix", true);
                if (this.borderstylemode == "borderstylemode2") {
                    firstrowtabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width: thin;border-bottom:1px solid ' + this.tablebgcolor + ';');
                } else if (this.borderstylemode == "borderstylemode3") {
                    if ( this.isRoundBorder ) {
                        firstrowtabletd.setAttribute('style', "border-color:"+this.tablebgcolor+ " !important; ");
                    } else {
                        if(cnt == 0){
                            firstrowtabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: 1px solid; border-right: none;border-bottom:1px solid ' + this.tablebgcolor + ';border-top:none;');//ERP-18680 : [Document Designer] Line Item Global rows issue
                        } else if(cnt == columnarr.length-1){
                            firstrowtabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: 1px solid;border-bottom:1px solid ' + this.tablebgcolor + ';border-top:none;');//ERP-18680 : [Document Designer] Line Item Global rows issue
                        } else{
                            firstrowtabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom:1px solid ' + this.tablebgcolor + ';border-top:none;');//ERP-18680 : [Document Designer] Line Item Global rows issue
                        }                        
                    }
                } else if (this.borderstylemode == "borderstylemode4") {
                    firstrowtabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom:none ' + this.tablebgcolor + ';border-top: none;');//changed 'border-bottom: none' because ERP-18680 : [Document Designer] Line Item Global rows issue
                }else if (this.borderstylemode == "borderstylemode5") {
                    firstrowtabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom:none ' + this.tablebgcolor + ';border-top: none;');//changed 'border-bottom: none' because ERP-18680 : [Document Designer] Line Item Global rows issue
                }else if (this.borderstylemode == "borderstylemode6") {
                    if ( this.isRoundBorder ) {
                        firstrowtabletd.setAttribute('style', "border-color:"+this.tablebgcolor+ " !important; ");
                    } else {
                        if(cnt == 0){
                            firstrowtabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: 1px solid; border-right: none;border-bottom:none ' + this.tablebgcolor + ';border-top: none;');//ERP-18680 : [Document Designer] Line Item Global rows issue
                        } else if(cnt == columnarr.length-1){
                            firstrowtabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: 1px solid;border-bottom:none ' + this.tablebgcolor + ';border-top: none;');//ERP-18680 : [Document Designer] Line Item Global rows issue
                        } else{
                            firstrowtabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom:none ' + this.tablebgcolor + ';border-top: none;');//ERP-18680 : [Document Designer] Line Item Global rows issue
                        }
                    }
                }else if (this.borderstylemode == "borderstylemode7") {
                    firstrowtabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom:none ' + this.tablebgcolor + ';border-top: none;');//changed 'border-bottom: none' because ERP-18680 : [Document Designer] Line Item Global rows issue
                }else if (this.borderstylemode == "borderstylemode8") {
                    firstrowtabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom:none ' + this.tablebgcolor + ';border-top: none;');//changed 'border-bottom: none' because ERP-18680 : [Document Designer] Line Item Global rows issue
                }else if (this.borderstylemode == "borderstylemode9") {
                   firstrowtabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: none; border-right: none;border-bottom:none ' + this.tablebgcolor + ';border-top: none;');//changed 'border-bottom: none' because ERP-18680 : [Document Designer] Line Item Global rows issue
                }else if (this.borderstylemode == "borderstylemode10") {
                    if ( this.isRoundBorder ) {
                        firstrowtabletd.setAttribute('style', "border-color:"+this.tablebgcolor+ " !important; ");
                    } else {
                        firstrowtabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left: 1px solid ' + this.tablebgcolor + '; border-right: 1px solid ' + this.tablebgcolor + ';border-bottom:none ' + this.tablebgcolor + ';border-top:none;');//changed 'border-bottom: none' because ERP-18680 : [Document Designer] Line Item Global rows issue
                    }
                }else {
                    if (this.isRoundBorder) {
                        firstrowtabletd.setAttribute('style', "border-color:"+this.tablebgcolor+ " !important; ");
                    } else {
                        firstrowtabletd.setAttribute('style', 'width: ' + widthInPercent + ';border-width:thin;border-left:1px solid ' + this.tablebgcolor + '; border-right:1px solid ' + this.tablebgcolor + ';border-bottom:1px solid ' + this.tablebgcolor + ';border-top:1px solid ' + this.tablebgcolor + ';');
                    }
                }
                firstrowtabletd.setAttribute("colwidth", obj.colwidth);
                firstRow.appendChild(firstrowtabletd);
        }
            tableConfig.appendChild(r);
            tableConfig.appendChild(firstRow);
            tableConfig.appendChild(t);
            tableConfig.appendChild(c);
            if(summaryTableJson!=null && summaryTableJson!="" && JSON.stringify(summaryTableJson)!="{}"){
                var length = columnarr.length;
                var widthArray = [];
                
                var summaryTable = document.createElement('table');
                summaryTable.innerHTML= summaryTableJson.html;
                
                var columnsCount = summaryTableJson.columnCount;
                var rowsCount = summaryTableJson.rowCount;
                
                for (var cnt = 0; cnt < length; cnt++) {
                    var tdObject = columnarr[cnt];
                    if(cnt < (length-columnsCount)){
                        var tabletd2 = document.createElement('td');
                        tabletd2.innerHTML = "&nbsp;";
                        tabletd2.setAttribute('style', 'border:0px none');
                        if(cnt != length-1){
                            b.appendChild(tabletd2);
                        }
                    } else{
                        widthArray.push(tdObject.colwidth);
                    }
                }
                for (var widthCnt = 0; widthCnt < widthArray.length ; widthCnt++) {
                    for (var i = 0; i < summaryTable.rows.length ; i++) {
                        var row = summaryTable.rows[i];
                        if(row.cells[widthCnt]){
                            var cell =row.cells[widthCnt];
                            var width = 0;
                            // Unable to edit line item if colspan is applied in summary table
                            var colspan =  cell.getAttribute('colSpan');
                            if(colspan>1){
                                for(var colSpanCnt = 0; colSpanCnt < colspan ; colSpanCnt++ ){
                                    if(widthArray[widthCnt+colSpanCnt]){
                                        width += parseInt(widthArray[widthCnt+colSpanCnt]);
                                    }
                                }
                            } else{
                                width = widthArray[widthCnt]
                            }
                            cell.style.width = width+"%";
//                            var style=cell.getAttribute('style');
//                            style += " width: "+ widthArray[widthCnt] +"%;";
//                            cell.setAttribute('style',style);
                        }
                    }
                }
                // copying table data to original table tag
                var tableCopy = summaryTable.outerHTML;
                tableCopy = tableCopy.substring(tableCopy.indexOf("<tbody>"),tableCopy.indexOf("</tbody>"));
                var originalTable = summaryTableJson.html;
                var htmltable = originalTable.substring(0,originalTable.indexOf("<tbody>"));
                htmltable +=tableCopy;
                htmltable += originalTable.substring(originalTable.indexOf("</tbody>"),originalTable.length);
                
                var tabletd1 = document.createElement('td');
                tabletd1.setAttribute('style', 'border:0px none');
                tabletd1.setAttribute('colspan',columnsCount );
                tabletd1.innerHTML = htmltable;
                b.appendChild(tabletd1);
                tableConfig.appendChild(b);
                tableConfig.firstChild.outerHTML = "<tbody><tr></tr></tbody>";
                isSummaryTableApplied = true;
                
            } else{
                isSummaryTableApplied = false;
            }
            
        if (isGroupingRowPresent) {
            if (groupingRowHTML) {
                var row = tableConfig.insertRow(3);
                var domParser = new DOMParser();
                var noOfCellInGroupingField = 0;
                var noOfCellInTable = 0;
                var parsedRow = domParser.parseFromString(groupingRowHTML, "text/xml");
                noOfCellInGroupingField = parsedRow.firstChild.childElementCount;
                noOfCellInTable = r.cells.length;
                var headers = r.cells;
                row.innerHTML = parsedRow.firstChild.innerHTML;
                var attributes = parsedRow.firstChild.attributes;
                for ( var attIndex = 0 ; attIndex < attributes.length ; attIndex++) {
                    row.setAttribute(attributes[attIndex].name, attributes[attIndex].value)
                }
                if ( noOfCellInTable > noOfCellInGroupingField) {
                    for ( var loopIndex = noOfCellInGroupingField ; loopIndex < noOfCellInTable; loopIndex++ ) {
                        var cell = document.createElement("td");
                        var fieldid = headers[loopIndex].attributes.getNamedItem("fieldid")!=null ? headers[loopIndex].attributes.getNamedItem("fieldid").value : ""; 
                        var xtype = headers[loopIndex].attributes.getNamedItem("xtype")!=null ? headers[loopIndex].attributes.getNamedItem("xtype").value : ""; 
                        cell.setAttribute("style","padding:5px;");
                        var div = document.createElement('div');
                        div.setAttribute("class", "fieldItemDivBorder");
                        div.setAttribute("putIn",fieldid);
                        div.setAttribute("xtype",xtype);
                        div.innerHTML = "Grouping Field";
                        div.setAttribute("onClick", "getGroupingFieldPropertyPanel(event,this,0)");
                        cell.appendChild(div);
                        row.appendChild(cell);
                    }
                }
                if ( noOfCellInTable < noOfCellInGroupingField ) {
                    for ( var loopIndex = noOfCellInGroupingField; loopIndex > noOfCellInTable; loopIndex--) {
                        if ( row.cells[loopIndex-1].children[0].attributes.getNamedItem("label") != null) {
                            isGroupingApplied = false;
                            lineitemparentpanel.items.items[0].isGroupingApplied = false;
                        }
                        row.deleteCell(loopIndex-1);
                    }
                }
            }
        }
        if ( !isSequnceChanged && !isRowDeleted ) {
            if (isGroupingAfterRowPresent) {
                if (groupingAfterRowHTML) {
                    var indextoadd = 0;
                    if ( isGroupingRowPresent ) {
                        indextoadd++;
                    }
                    var row = tableConfig.insertRow(4+indextoadd);
                    var domParser = new DOMParser();
                    var noOfCellInGroupingField = 0;
                    var noOfCellInTable = 0;
                    var parsedRow = domParser.parseFromString(groupingAfterRowHTML, "text/xml");
                    noOfCellInGroupingField = parsedRow.firstChild.childElementCount;
                    noOfCellInTable = r.cells.length;
                    var headers = r.cells;
                    row.innerHTML = parsedRow.firstChild.innerHTML;
                    var attributes = parsedRow.firstChild.attributes;
                    for ( var attIndex = 0 ; attIndex < attributes.length ; attIndex++) {
                        row.setAttribute(attributes[attIndex].name, attributes[attIndex].value)
                    }
                    if ( noOfCellInTable > noOfCellInGroupingField) {
                        for ( var loopIndex = noOfCellInGroupingField ; loopIndex < noOfCellInTable; loopIndex++ ) {
                            var cell = document.createElement("td");
                            var fieldid = headers[loopIndex].attributes.getNamedItem("fieldid")!=null ? headers[loopIndex].attributes.getNamedItem("fieldid").value : ""; 
                            var xtype = headers[loopIndex].attributes.getNamedItem("xtype")!=null ? headers[loopIndex].attributes.getNamedItem("xtype").value : ""; 
                            cell.setAttribute("style","padding:5px;");
                            var div = document.createElement('div');
                            div.setAttribute("class", "fieldItemDivBorder");
                            div.setAttribute("putIn",fieldid);
                            div.setAttribute("xtype",xtype);
                            div.innerHTML = "Formatting Field";
                            div.setAttribute("onClick", "getGroupingFieldPropertyPanel(event,this,1)");
                            cell.appendChild(div);
                            row.appendChild(cell);
                        }
                    }
                    if ( noOfCellInTable < noOfCellInGroupingField ) {
                        for ( var loopIndex = noOfCellInGroupingField; loopIndex > noOfCellInTable; loopIndex--) {
                            if ( row.cells[loopIndex-1].children[0].attributes.getNamedItem("label") != null) {
                                isFormattingApplied = false;
                                lineitemparentpanel.items.items[0].isFormattingApplied = false;
                            }
                            row.deleteCell(loopIndex-1);
                        }
                    }
                }
            }
        }
//            var designerPanel = Ext.getCmp(selectedElement);
            var PosX = lineitemparentpanel.cursorCustomX;
            var PosY = lineitemparentpanel.cursorCustomY;
            if (isEdit == true) {
                PosX = Posx;
                PosY = Posy;
            }
            
                var field = getlineitemstable(PosX, PosY, tableConfig.outerHTML, lineitemparentpanel,(lineitemparentpanel.items && lineitemparentpanel.items.items[0])? lineitemparentpanel.items.items[0]:lineitemparentpanel,isSummaryTableApplied,summaryTableJson,headerProperties);
                lineitemparentpanel.add(field);
                initTreePanel();//Refreshing the tree panel after adding Line Item
                lineitemparentpanel.doLayout();
                addColumntoLineItemRows(firstrowcellids,field,true,firstrowcells);
                addColumntoLineItemRows(lastrowcellids,field,false,lastrowcells);
                if ( isGroupingAfterRowPresent && ( isSequnceChanged || isRowDeleted) ) {
                    addGroupingRow(1);
                    isFormattingApplied = false;
                    lineitemparentpanel.items.items[0].isFormattingApplied = false;
                }
    }, containerScope);
    _tw.show();
}//endOf_openProdWindowAndSetConfig

Ext.TemplateHolder = Ext.extend(Ext.Component, {
    defaultMenuConfig: {
        tag: 'ul',
        cls: 'edit-links',
        children: [
            {
                tag: 'li',
                menuname: 'edit',
                cls: 'edit tpl-link',
                html: 'edit'
            },
            {
                tag: 'li',
                menuname: 'remove',
                cls: 'remove tpl-link',
                html: 'remove'
            }
        ]
    },
    onRender: function (ct, position) {
        Ext.TemplateHolder.superclass.onRender.call(this, ct, position);
        this.defaultMenuConfig.cls = "section-menu " + (this.defaultMenuConfig.cls || "");
        this.addButtons();
        this.setHtml(this.bodyHtml);
    },
    addButtons: function () {
        this.elDom = Ext.get(this.el.dom).createChild({
            tag: "div",
            cls: "templateCompCont"
        });
        this.table1 = document.createElement("table");
        this.table1.setAttribute("cellspacing", 0);
        this.table1.setAttribute("width", "100%");
        this.table1.className = "tplBodyHolder";
        var tab1Body = document.createElement("tbody");
        var tab1Row = document.createElement("tr");
        var tab1Data = document.createElement("td");
        tab1Data.setAttribute("align", "center");
        tab1Row.appendChild(tab1Data);
        tab1Body.appendChild(tab1Row);
        this.table1.appendChild(tab1Body);
        var table2 = document.createElement("table");
        table2.setAttribute("cellspacing", 0);
        table2.setAttribute("cellpadding", 0);
        table2.setAttribute("width", "100%");
        var tab2Body = document.createElement("tbody");
        var tab2Row = document.createElement("tr");
        this.contentHolder = document.createElement("td");
        tab2Row.appendChild(this.contentHolder);
        tab2Body.appendChild(tab2Row);
        table2.appendChild(tab2Body);
        tab1Data.appendChild(table2);
        Ext.get(this.contentHolder).addListener("click", this.contentClicked, this);
        this.elDom.appendChild(this.table1);
    },
    appendSectionMenu: function (el, menuConfig) {
        if (menuConfig) {
            menuConfig.cls = "section-menu " + (menuConfig.cls || "");
        }
        Ext.DomHelper.insertFirst(el, menuConfig || this.defaultMenuConfig);
    },
    removeSectionMenu: function (el) {
        var chArr = Ext.DomQuery.select("ul[class*=section-menu]", el);
        for (var i = 0; i < chArr.length; i++) {
            el.removeChild(chArr[i]);
        }
    },
    setHtml: function (html) {
        this.contentHolder.innerHTML = html;
        var sectionArray = Ext.DomQuery.select("*[class*=tpl-content]", this.contentHolder);
        for (var i = 0; i < sectionArray.length; i++) {
            this.appendSectionMenu(sectionArray[i]);
        }
    },
    getHtml: function () {
        var x = this.contentHolder.cloneNode(true);
        var sectionArray = Ext.DomQuery.select("*[class*=tpl-content]", x);
        for (var i = 0; i < sectionArray.length; i++) {
            this.removeSectionMenu(sectionArray[i]);
        }
        return x.innerHTML;
    },
    removeSection: function (sectionEl) {
        sectionEl.parentNode.removeChild(sectionEl);
    },
    contentClicked: function (e) {
        var _to = e.getTarget();
        if (_to.className.indexOf("tpl-content") != -1 || _to.className.indexOf("edit tpl-link") != -1) {
            var contentEl = e.getTarget(".tpl-content").cloneNode(true);
            this.removeSectionMenu(contentEl);
            openProdWindowAndSetConfig(this, this.containerId);
//            var _tw = new Ext.ProductGridTemplateWindow({});
//            _tw.on("okClicked", function(obj){
//                var valObj = obj.getGridConfigSetting();
//                var jArr = eval(valObj);
//                var tab1Row = document.createElement("ul");
//                tab1Row.setAttribute("id", "itemlistconfig");
//                tab1Row.setAttribute('style', 'padding-left: inherit;width:100%');
//                for(var cnt=0; cnt<jArr.length; cnt++) {
//                    var obj = jArr[cnt];
//                    var tab2Row = document.createElement("li");
//                    tab2Row.setAttribute("class", "tpl-colname");
//                    var widthInPercent = obj.colwidth+"%";
//                    tab2Row.setAttribute('style', 'width: '+widthInPercent);
//                    tab2Row.setAttribute("colwidth", obj.colwidth);
//                    tab2Row.setAttribute("coltotal", obj.coltotal);
//                    tab2Row.setAttribute("seq", obj.seq);
//                    tab2Row.value = obj.fieldid;
//                    tab2Row.innerHTML = decodeURIComponent(obj.displayfield);
//                    tab1Row.appendChild(tab2Row);
//                }
//                this.setHtml(tab1Row.outerHTML);
//            }, this);
//            _tw.show();
        }
        if (_to.className.indexOf("remove tpl-link") != -1) {
            var sectionArray = Ext.DomQuery.select("*[class*=tpl-content]", this.contentHolder);
            //            if(sectionArray.length>1){
            var contentEl = e.getTarget(".tpl-content");
            Ext.MessageBox.show({
                title: 'Confirm',
                msg: 'Do you really want to delete this section?', //"Do you really want to delete this section?",
                icon: Ext.MessageBox.QUESTION,
                buttons: Ext.MessageBox.YESNO,
                scope: this,
                fn: function (button) {
                    if (button == 'yes')
                    {
                        this.destroy();
                    } else {
                        return;
                    }
                }
            });
            //            } else {
            //                WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),WtfGlobal.getLocaleText("acc.template.dontdelallsecmsg")]);
            //            }
        }
    },
    getPlainText: function () {
        var htm = this.elDom.dom.innerHTML;
        htm = htm.replace(/<p>/g, "");
        htm = htm.replace(/<\p>/g, "");
        htm = htm.replace(/<P>/g, "");
        htm = htm.replace(/<\P>/g, "");
        htm = htm.replace(/&nbsp;/g, "");
        htm = Ext.util.Format.stripTags(htm);
        return htm;
    },
    applyTemplateTheme: function (theme) {
        if (theme) {

            for (elAttr in theme) {
                var elArr = Ext.DomQuery.select(elAttr, this.table1.parentNode);
                if (elArr.length == 0) {
                    ResponseAlert(99);
                    return;
                } else {
                    for (var i = 0; i < elArr.length; i++) {
                        var el = elArr[i];
                        for (styleAttr in theme[elAttr])
                            el.style[styleAttr] = theme[elAttr][styleAttr];
                    }
                }
            }
        }
    }
});

function onDeleteUpdatePropertyPanel() {
    Ext.getCmp('hidden_fieldID').setValue('');
    Ext.getCmp('setfieldproperty').setValue("-");
//    Ext.getCmp('cancelformattingbtn').hide();
    Ext.getCmp('editformattingbtn').setText('Edit');
}
function isValidFieldSelected() {
    return Ext.getCmp('hidden_fieldID').getValue() != ""
}
function createExtComponent(designerPanel, propertyPanel, fieldTypeId, label, X, Y, extraConfig, selectfieldbordercolor) {


    var field = Ext.create('Ext.Component', {
        x: X,
        y: Y,
        width: extraConfig && extraConfig.width ? extraConfig.width : 80,
        height: extraConfig && extraConfig.height ? extraConfig.height : 20,
        style: {
            borderColor: (selectfieldbordercolor != null && selectfieldbordercolor != '#B5B8C8') ? selectfieldbordercolor : '#B5B8C8',
            borderStyle: 'solid',
            borderWidth: '1px',
            //position:'absolute'
            position: 'relative'

        },
        draggable: true,
        initDraggable: function () {
            var me = this,
                    ddConfig;
            //                             if (!me.header)
            //                             {
            //                                 me.updateHeader(true);
            //                             }
            ddConfig = Ext.applyIf({
                el: me.el//,-Reimius, I commented out the next line that delegates the dragger to the header of the window
                        //delegate: '#' + Ext.escapeId(me.header.id)
            }, me.draggable);
            me.dd = new Ext.util.ComponentDragger(this, ddConfig);
            me.relayEvents(me.dd, ['dragstart', 'drag', 'dragend']);
        }, //initDraggable
        resizable: true,
        autoDestroy: true,
        fieldTypeId: fieldTypeId,
        html: label,
        onRender: function () {
            this.superclass.onRender.call(this);
            //addPositionObjectInCollection(this);
            this.el.on('click', function (eventObject, target, arg) {
                var component = designerPanel.queryById(this.id)
                if (component) {
                    if (Ext.getCmp('contentImage'))
                        Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                    eventObject.stopPropagation();
                    Ext.get(selectedElement).removeCls("selected");
                    selectedElement = this.id;
                    Ext.getCmp(selectedElement).addClass('selected');
                    eventObject.stopPropagation( );
                    createPropertyPanel(selectedElement);
                    showElements(selectedElement);
                    // getPropertyPanel(component, designerPanel,propertyPanel);
                }

            });


        },
        listeners: {
            onMouseUp: function (field) {
                field.focus();
            }
            ,
            removed: function () {
                //onDeleteUpdatePropertyPanel();
                //                            propertyPanel.body.update("<div style='padding:10px;font-weight: bold'>Select field to delete</div>");
            },
            resize: function () {
                /*var selectedfield = Ext.getCmp(Ext.getCmp('hidden_fieldID').getValue());
                 if(selectedfield != null){
                 Ext.getCmp('idWidth').setValue(selectedfield.getWidth());
                 }
                 */
            }

        }
    });
    //field.on('drag',showAlignedComponent, field);
    //field.on('dragend',removeAlignedLine, field);


    return field;
}//createExtComponent()

function createExtFieldSet(designerPanel, propertyPanel, fieldTypeId, label, X, Y) {
    var field = Ext.create('Ext.form.FieldSet', {
        x: X,
        y: Y,
        width: 80,
        height: 100,
        style: {
            borderColor: '#B5B8C8',
            borderStyle: 'solid',
            borderWidth: '1px',
            position: 'absolute'
        },
        draggable: true,
        initDraggable: function () {
            var me = this,
                    ddConfig;
            ddConfig = Ext.applyIf({
                el: me.el//,-Reimius, I commented out the next line that delegates the dragger to the header of the window
                        //delegate: '#' + Ext.escapeId(me.header.id)
            }, me.draggable);
            me.dd = new Ext.util.ComponentDragger(this, ddConfig);
            me.relayEvents(me.dd, ['dragstart', 'drag', 'dragend']);
        }, //initDraggable
        /*draggable: {
         insertProxy: false,
         onDrag: function(e) {
         var el = this.proxy.getEl();
         this.x = el.getX();
         this.y = el.getY();
         },
         endDrag: function(e) {
         panel.setPosition(this.x,this.y);
         alert('asd');
         },
         alignElWithMouse: function() {
         panel.dd.superclass.superclass.alignElWithMouse.apply(panel.dd, arguments);
         this.proxy.sync();
         }
         },*/
        resizable: true,
        fieldTypeId: fieldTypeId,
        //        html : label,
        onRender: function () {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);
            this.el.on('click', function (eventObject, target, arg) {
                var component = designerPanel.queryById(this.id)
                if (component) {
                    getPropertyPanel(component, designerPanel, propertyPanel);
                }
            });
        },
        listeners: {
            onMouseUp: function (field) {
                field.focus();
            }
            /*,
             move: function(field) {
             alert('sd');
             }*/
        }
    });
    field.on('drag', showAlignedComponent, field);
    field.on('dragend', removeAlignedLine, field)
    return field;
}

//function createExtImgComponent(designerPanel, propertyPanel, fieldTypeId, src, X, Y, obj) {
//   var field = Ext.create('Ext.Img', {
//        width: obj ? obj.width : 80,
//        height: obj ? obj.height : 60,
//        x: X,
//        y: Y,
//        draggable: true,
//        elementwidth:(obj!=null || obj!=undefined)?obj.elementwidth:80,
//        unit:(obj!=null || obj!=undefined)?obj.unit:'px',
//        initDraggable: function() {
//            var me = this,
//                    ddConfig;
//            ddConfig = Ext.applyIf({
//                el: me.el//,-Reimius, I commented out the next line that delegates the dragger to the header of the window
//                        //delegate: '#' + Ext.escapeId(me.header.id)
//            }, me.draggable);
//            me.dd = new Ext.util.ComponentDragger(this, ddConfig);
//            me.relayEvents(me.dd, ['dragstart', 'drag', 'dragend']);
//        }, //initDraggable
//        resizable: true,
//        fieldType: fieldTypeId,
//        style: {
//            borderColor: '#B5B8C8',
//            borderStyle: 'solid',
//            borderWidth: '1px'
//        },
//        src: src,
//        onRender: function () {
//            this.superclass.onRender.call(this);
//            // addPositionObjectInCollection(this);
//            this.el.on('click', function (eventObject, target, arg) {
//
//                var component = designerPanel.queryById(this.id)
//                if (component) {
//                    if (Ext.getCmp('contentImage')) {
//                        Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
//                    }
//                    eventObject.stopPropagation();
//                    selectedElement = this.id;
//                    createPropertyPanel(selectedElement);
//                    setProperty(selectedElement);
//                    showElements(selectedElement);
//                }
//            });
//        },
//        listeners: {
//            onMouseUp: function (field) {
//                field.focus();
//            },
//            removed: function () {
//                //onDeleteUpdatePropertyPanel();
//                //                            propertyPanel.body.update("<div style='padding:10px;font-weight: bold'>Select field to delete</div>");
//            }
//        }
//    })
//    //field.on('drag',showAlignedComponent, field);
//    //field.on('dragend',removeAlignedLine, field);
//    return field;
//}


function createExtImgComponent(designerPanel, propertyPanel, fieldTypeId, src, X, Y, obj) {
    var iselementlevel=(obj != null || obj != undefined) ? obj.iselementlevel :false;
    var marginTop = (obj && obj.marginTop) || (obj && obj.marginTop === 0)  ? obj.marginTop  : '0px';
    var marginBottom = (obj && obj.marginBottom) ||(obj && obj.marginBottom === 0) ? obj.marginBottom : '0px';
    var marginLeft = (obj && obj.marginLeft)||(obj && obj.marginLeft === 0) ? obj.marginLeft : '0px';
    var marginRight = (obj && obj.marginRight) ||(obj && obj.marginRight === 0) ? obj.marginRight : '0px';
    var textalignclass = (obj && obj.textalignclass) ? obj.textalignclass : 'imageshiftleft';
    var fieldAlignment = (obj && obj.fieldAlignment) ? obj.fieldAlignment : '1';
    var containerclassfieldalignvalue = (designerPanel.fieldalignment != null || designerPanel.fieldalignment != undefined) ? designerPanel.fieldalignment : "1";
    var containerclass = "sectionclass_field_container";
    if (containerclassfieldalignvalue == "2") {
        containerclass = " sectionclass_field_container_inline";
    }
    var unit = (obj!=null || obj!=undefined)?obj.unit:'px';
    var imageStr = "<img src=" + src +"></img>";
    var field = Ext.create('Ext.Component', {
//        width:'auto',
//        height:'auto',
        x: X,
        y: Y,
        draggable: false,
        elementwidth:(obj && obj.elementwidth )?obj.elementwidth:'auto',
        elementheight:(obj && obj.elementheight)?obj.elementheight:'auto',
        width:  (obj && obj.width )?isNaN(parseInt(obj.width))?'auto':parseInt(obj.width):'auto',
//        height:  (obj && obj.height)?isNaN(parseInt(obj.height))?'auto':parseInt(obj.height):'auto',
        textalignclass:textalignclass,
        marginTop : marginTop,
        marginBottom:marginBottom,
        marginLeft:marginLeft,
        marginRight:marginRight,
        iselementlevel:iselementlevel,
        unit:unit,
        fieldAlign:fieldAlignment,
        cls : 'sectionclass_field_without_border',
        ctCls : containerclass,
        fieldType: fieldTypeId,
        src: src,
        html: imageStr,
        onRender: function () {
            this.superclass.onRender.call(this);
            var imgClass = "image_auto_width_new_blockcls";
            var element = this.el.dom.children[0]; // image
            if ( fieldAlignment === "2" ) {  //fieldAlignment === "2" means inline field alignment
                imgClass = "image_auto_width_new_inlinecls";
                element = this.el.dom;  // component
                element.style.marginLeft = marginLeft + "px";
                element.style.marginRight = marginRight + "px";
            } else {
                if ( textalignclass === "imageshiftleft" ) {
                    element.style.marginRight = "auto";
                    element.style.marginLeft = "0px";
                } else if ( textalignclass === "imageshiftcenter" ) {
                    element.style.marginRight = "auto";
                    element.style.marginLeft = "auto";
                } else if ( textalignclass === "imageshiftright" ) {
                    element.style.marginRight = "0px";
                    element.style.marginLeft = "auto";
                }
            }
            
            element.style.marginTop = marginTop + "px";
            element.style.marginBottom = marginBottom + "px";
            this.addCls(imgClass);
            
            if(obj){
                if (  unit == '%' ) {
//                    if (fieldAlignment == "2"){
//                        this.el.dom.style.width = obj.elementwidth + "%";
//                        this.el.dom.children[0].style.width ="100% !important";
//                    }else{
//                        this.el.dom.children[0].style.width = obj.elementwidth + "%";
//                    }
                    element.style.width = obj.elementwidth + "%";
                }else{
//                    if (fieldAlignment == "2"){
//                        this.el.dom.children[0].style.width = obj.elementwidth + "px !important";
//                    }else{
//                        this.el.dom.children[0].style.width = obj.elementwidth + "px";  
//                    }  
                    element.style.width = obj.elementwidth + "px";
                }
                if (element.nodeName === "IMG" ) {
                    element.style.height = obj.elementheight + "px";
                } else {
                    element.children[0].style.height = obj.elementheight + "px";
                }
            }
            
            
            
            
            
            
//            var sectionclass="image_auto_width_blockcls";
//            if (obj && obj.width != null) {
//                if (obj.width != "" && obj.width != "auto") {
//                    sectionclass="imagesectionclass_field_block";
//                    if (fieldAlignment == "2") {
//                        sectionclass = 'imagesectionclass_field_inline';
//                    }
//                } else {
//                    sectionclass = 'image_auto_width_blockcls';
//                    if (fieldAlignment == "2") {
//                        sectionclass = 'image_auto_width_inlinecls';
//                    }
//                }
//            }
//            this.addCls(sectionclass);
////            if ( fieldAlignment == "1" ) {
////                this.addCls("imagesectionclass_field_block");
////            } else if ( fieldAlignment == "2" ) {
////                this.addCls("imagesectionclass_field_inline");
////            }
//            this.el.dom.style.width = "100% !important";
//            this.el.dom.style.height = "auto !important";
//            if(obj){
//                if (  unit == '%' ) {
//                    if (fieldAlignment == "2"){
//                        this.el.dom.style.width = obj.elementwidth + "%";
//                        this.el.dom.children[0].style.width ="100% !important";
//                    }else{
//                        this.el.dom.children[0].style.width = obj.elementwidth + "%";
//                    }
//                }else{
//                     if (fieldAlignment == "2"){
//                         this.el.dom.children[0].style.width = obj.elementwidth + "px !important";
//                     }else{
//                       this.el.dom.children[0].style.width = obj.elementwidth + "px";  
//                     }  
//                }
//                this.el.dom.children[0].style.height = obj.elementheight + "px";  
//            }
            
            this.el.on('click', function (eventObject, target, arg) {
                if (selectedElement != null || selectedElement != undefined)
                {
                    if(Ext.get(selectedElement)!=null){
                        Ext.get(selectedElement).removeCls("selected");
                    }
                }
                var component = designerPanel.queryById(this.id)
                if (component) {
                    if (Ext.getCmp('contentImage')) {
                        Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                    }
                    eventObject.stopPropagation();
                    selectedElement = this.id;
                    getEXTComponent(selectedElement).addCls("selected");
                    createPropertyPanel(selectedElement);
                    setProperty(selectedElement);
                    showElements(selectedElement);
                }
            });
            this.el.on("contextmenu", function(event, ele) {
                if (selectedElement != null || selectedElement != undefined)
                {
                    if(Ext.get(selectedElement)!=null){
                        Ext.get(selectedElement).removeCls("selected");
                    }
                }
                event.stopEvent();
                createContextMenu(Ext.fieldID.insertImage,ele.parentNode.id);
                contextMenuNew.showAt(event.getXY());
                return false;
            });
        },
        listeners: {
            onMouseUp: function (field) {
                field.focus();
            },
            removed: function () {
            }
        }
    })
    field.addCls(textalignclass);
    return field;
}

function WtfComMsgBox(choice, type, iswait, button,fn) {
    if (iswait == undefined || iswait == null)
        iswait = false;
    var strobj = [];
    var title = "";
    var iconType = Ext.MessageBox.INFO;
    var btnType = Ext.MessageBox.OK;
    strobj = [choice[0], choice[1]];
    if (type == 0)
        iconType = Ext.MessageBox.INFO;
    else if (type == 1)
        iconType = Ext.MessageBox.ERROR;
    else if (type == 2)
        iconType = Ext.MessageBox.WARNING;
    else if (type == 3)
        iconType = Ext.MessageBox.INFO;
    
    if (button == 0)
        btnType = Ext.MessageBox.OK;
    else if (button == 1)
        btnType = Ext.MessageBox.YESNO;

    if (iswait) {
        Ext.MessageBox.show({
            msg: strobj,
            width: 320,
            wait: true,
            title: title,
            waitConfig: {
                interval: 100
            }
        });
    } else {
        Ext.MessageBox.show({
            title: strobj[0],
            msg: strobj[1],
            width: 370,
            buttons: btnType,
            animEl: 'mb9',
            fn: fn,
            icon: iconType
        });
    }
}

function searchRecord(store, ID, idname) {
    var index = store.findBy(function (record) {
        if (record.get(idname) == ID)
            return true;
        else
            return false;
    });
    if (index == -1)
        return null;

    return store.getAt(index);
}
function is_html(htmlstring) {
    return /<(br|basefont|hr|input|source|frame|param|area|meta|!--|col|link|option|base|img|wbr|!DOCTYPE).*?>|<(a|abbr|acronym|address|applet|article|aside|audio|b|bdi|bdo|big|blockquote|body|button|canvas|caption|center|cite|code|colgroup|command|datalist|dd|del|details|dfn|dialog|dir|div|dl|dt|em|embed|fieldset|figcaption|figure|font|footer|form|frameset|head|header|hgroup|h1|h2|h3|h4|h5|h6|html|i|iframe|ins|kbd|keygen|label|legend|li|map|mark|menu|meter|nav|noframes|noscript|object|ol|optgroup|output|p|pre|progress|q|rp|rt|ruby|s|samp|script|section|select|small|span|strike|strong|style|sub|summary|sup|table|tbody|td|textarea|tfoot|th|thead|time|title|tr|track|tt|u|ul|var|video).*?<\/\2>/i.test(htmlstring);
}

function isValidSession(result) {
    if (!result.valid) {
        WtfComMsgBox(["Error", "Session invalid."], 1);
    }
    return result.valid;
}
