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

Ext.grid.CheckColumn = function(config){
    Ext.apply(this, config);
    if(!this.id)
        this.id = Ext.id();
    this.renderer = Ext.Function.pass(this.renderer, [this]);//this.renderer.createDelegate(this);
};
Ext.grid.CheckColumn.prototype ={
    fyear:0,
    byear:0,
    fdate:0,
    bdate:0,
    init : function(grid){
        this.grid = grid;
        this.grid.on('render', function(){
            var view = this.grid.getView();
            view.mainBody.on('mousedown', this.onMouseDown, this);
        }, this);
    },
    onMouseDown : function(e, t){
        if(t.className && t.className.indexOf('x-grid3-cc-'+this.id) != -1){
            e.stopEvent();
            var index = this.grid.getView().findRowIndex(t);
            var record = this.grid.store.getAt(index);
            record.set(this.dataIndex, !record.data[this.dataIndex]);
            this.grid.fireEvent("afteredit",{
                grid:this.grid,
                record:record,
                field:this.dataIndex,
                value:!record.data[this.dataIndex],
                originalValue:record.data[this.dataIndex],
                row:index,
                column:0//Not known
            });
        }
    },
    renderer : function(v, p, record){
        p.css += ' x-grid3-check-col-td';
        return '<div class="x-grid3-check-col'+(v?'-on':'')+' x-grid3-cc-'+this.id+'">&#160;</div>';
    }
};

Ext.ProductGridTemplateWindow = function(conf) {
    Ext.apply(this, conf);
    this.addEvents({
        "okClicked": true
    });
    this.createEditor();
    Ext.ProductGridTemplateWindow.superclass.constructor.call(this, {
        width: 900,
        height: 500,
        resizable: false,
        //        iconCls: "pwnd favwinIcon",
        layout: "fit",
        title: 'Configure Product Items ',//"Edit Your Content",
        modal: true,
        items : this.reportGrid,
        buttons: [{
            text: "Table Property",
            scope: this,
            handler: this.tableproperty
        },{
            text: "OK",
            scope: this,
            handler: this.okClicked
        }, {
            text: "Cancel",
            scope: this,
            handler: this.cancelClicked
        }]
    });
};
 Ext.tablelinegrid=[];

Ext.extend(Ext.ProductGridTemplateWindow, Ext.Window, {
    onRender: function(conf) {
        Ext.ProductGridTemplateWindow.superclass.onRender.call(this, conf);
    //        this.createEditor();
        
    //        var _iArr = [this.reportGrid];
    //        this.createVariableStores();

    //        this.add(this.reportGrid);

        
    },
    
    createEditor: function(){
        
        this.decimal =new Ext.form.NumberField({
            validateOnBlur: true,
            maxValue:99,
            minValue: 0,
            maxLength:15,
            allowBlank:false
        });
            
        
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
        
        this.reportCM = [
        {
            header: 'Column',//"Column",
            dataIndex: "columnname",
            width: 150
        },{
            header: 'Display Name',//"Display Name",
            dataIndex: "displayfield",
            editor: new Ext.form.TextField({
                validateOnBlur: true,
                allowBlank:false
            }),
            width: 150
        },{
            header: 'Column Width(%)',//"Column Width(%)",
            dataIndex: "colwidth",
            editor: new Ext.form.NumberField({
                validateOnBlur: true,
                allowBlank:false,
                maxValue: 99,
                minValue: 5
            }),
            width: 80
        },{
            header: 'Hide Column',
            dataIndex: 'hidecol',
            xtype: 'checkcolumn',
            width: 80
        }
        ,{
            header: 'Header with Currency',
            dataIndex: 'headercurrency',
            xtype: 'checkcolumn',
            width: 80,
            renderer : 
            function(val, m, rec){               
                if (val == ''|| val == "false")
                    val= false;
                else
                    val=true;
                if(rec.data.columnname=="Amount"|| rec.data.columnname=="Rate" || rec.data.columnname=="Tax"  || rec.data.columnname=="Discount"||rec.data.columnname=="Sub Total"){
                    return (new Ext.ux.CheckColumn()).renderer(val);
                }
                else{
                    return '';
                }
            }
        },{
            header: 'Records with Currency',
            dataIndex: 'recordcurrency',
            xtype: 'checkcolumn',
            width: 80,
            renderer : 
            function(val, m, rec){   
                if (val == ''|| val == "false")
                    val= false;
                else
                    val=true;
                if(rec.data.columnname=="Amount" || rec.data.columnname=="Rate" || rec.data.columnname=="Tax"  || rec.data.columnname=="Discount"||rec.data.columnname=="Sub Total"){
                    return (new Ext.ux.CheckColumn()).renderer(val);
                }
                else{
                    return '';
                }
            }
        },{
            header: 'Decimal Points',//"Decimal Points",
            dataIndex: "decimalpoint",
            editor: this.decimal,
            width: 80
        },{
            header: 'Amount in Comma',
            dataIndex: 'commaamount',
            xtype: 'checkcolumn',
            width: 80,
            renderer : 
            function(val, m, rec){               
                if (val == ''|| val == "false")
                    val= false;
                else
                    val=true;
                if(rec.data.columnname=="Amount"|| rec.data.columnname=="Rate" || rec.data.columnname=="Quantity" || rec.data.columnname=="Tax"  || rec.data.columnname=="Discount" || rec.data.columnname=="Actual Quantity" || rec.data.columnname=="Received Quantity" || rec.data.columnname=="Delivered Quantity" || rec.data.columnname=="Quantity with UOM"
                || rec.data.columnname=="Actual Quantity With UOM" || rec.data.columnname=="Delivered Quantity With UOM" || rec.data.columnname=="Received Quantity With UOM" || rec.data.columnname=="Sub Total"){
                    return (new Ext.ux.CheckColumn()).renderer(val);
                }
                else{
                    return '';
                }
            }
        },{
            header: 'Show Total',
            dataIndex: 'showtotal',
            xtype: 'checkcolumn',
            width: 80,
            renderer : 
            function(val, m, rec){               
                if (val == ''|| val == "false")
                    val= false;
                else
                    val=true;
                if(rec.get("columnname")=="UOM"){
                    return '';
                }
                else{
                return (new Ext.ux.CheckColumn()).renderer(val);
                
            }
                
            }
        },{
            header: 'Column Total',
            dataIndex: 'coltotal',
            xtype: 'checkcolumn',
            hidden : true,
            width: 80,
            renderer : function(val, m, rec) {
                if (val == false)
                    return '';
                else
                    return (new Ext.ux.CheckColumn()).renderer(val);
            }
        },{
            header: 'Actions',//"Actions",
            dataIndex: 'id',
            width:100,
            renderer:function(value, css, record, row, column, store){
                var actions = "<image src='images/up.png' title='Move Up' onclick=\"changeseq('"+record.get('seq')+"',0, 'customReportConfigGrid')\"/>"+
                "<image src='images/down.png' style='padding-left:5px' title='Move Down' onclick=\"changeseq('"+record.get('seq')+"',1, 'customReportConfigGrid')\"/>";
                //                    actions +="<img class='delete' src='images/cancel_16.png' style='padding-left:5px' title='Delete Field'></img>";
                return actions;
            }
        },this.header={
            text: "Header Property",
            dataIndex: 'headerproperty',
            scope: this,
            width: 80,
            renderer : function(val){
                return "<a href = '#' class='setheaderproperty'> Set Property</a>";
            }  
        }];
      
        this.reportStore = new Ext.create('Ext.data.Store', {
            fields:['columnname','displayfield','hidecol', 'seq','fieldid','coltotal','colwidth','xtype','headerproperty', 'showtotal','recordcurrency','headercurrency','decimalpoint','commaamount'],
            data: documentLineColumns,// [["Product Name","Product Name",false,'0','1'],["Product Description","Product Description",false,'1','2'],["Rate","Rate",false,'2','3'], ["Quantity","Quantity",false,'3','4'],["Total Amount","Total Amount",false,'4','5']/*,["Campaign","Campaign"]*/],
            autoLoad: true
        });
        var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
            clicksToEdit: 1
        });
        this.reportGrid = Ext.create('Ext.grid.Panel', {
            columns: this.reportCM,
            //            region: "center",
            store: this.reportStore,
            clicksToEdit :1,
            renderTo:this.id,
            id : 'customReportConfigGrid', // don't delete this id
            //            sm: this.reportSM,
            viewConfig: {
                forceFit:true
            },
            //            plugins:[this.hideCol],
            plugins: [cellEditing],
            layout:'fit'
        });
        
        this.reportGrid.on('cellclick',this.cellclickhandle,this);
        this.reportGrid.on('beforeedit',this.beforeEdit,this);
    },

    beforeEdit :function(e){
        if(e.context.field=='decimalpoint'){
            if(e.context.record.data.columnname=='Amount'||e.context.record.data.columnname=='Rate'||e.context.record.data.columnname=='Tax'||e.context.record.data.columnname=='Quantity'||e.context.record.data.columnname=='Actual Quantity'||e.context.record.data.columnname=='Received Quantity'||e.context.record.data.columnname=='Delivered Quantity'
        || e.context.record.data.columnname=='Quantity With UOM'||e.context.record.data.columnname=='Actual Quantity With UOM'||e.context.record.data.columnname=='Delivered Quantity With UOM'||e.context.record.data.columnname=='Received Quantity With UOM'||e.context.record.data.columnname=='Return Quantity With UOM'||e.context.record.data.columnname=='Return Quantity'||e.context.record.data.columnname=='Sub Total'){
                 return true;
            }else{
                return false;
            }
        }
    },
    cellclickhandle:function(scope,td,cellIndex,record,tr,rowIndex,e,eOpts){
        if(e.getTarget("a[class='setheaderproperty']")) {
            this.record=record;
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
            if(record.data.headerproperty){
                styleRecord = eval('('+record.data.headerproperty+')');
            }
            var propPanel = new Ext.HeaderWin({
                headerVal : record.data.displayfield,
                record :  this.record,
                currentrow:rowIndex,
                styleRecord:styleRecord
                
            });             
            var headerbutton= new Ext.Window({
                closable:true,
                title: 'HeaderProperty',
                id:'headerproperty',
                width:460,
                height:470,
                items :propPanel,
                buttons: [{
                    text: "Set",
                    scope: this,
                    handler: function(){
                        var headervalues=getHeaderlineproperties();
                        var headerObj = eval('('+headervalues+')');
                        var name=JSON.stringify(headerObj.changedlabel);
                        
                        this.record.set('displayfield',headerObj.changedlabel);
                        this.record.set('headerproperty',headervalues);
                        
                        Ext.tablelinegrid[rowIndex]=this.record.data;
                        record=  this.record
                        Ext.getCmp('headerproperty').close();
                    }
                }]
            })
            
            headerbutton.show();
            headerbutton.doLayout();
//            var headerbutton= new Ext.HeaderWin({
//                headerVal : record.data.displayfield,
//                record : record
//            });             
//            headerbutton.show();
//            headerbutton.doLayout();
        }
    },
    okClicked: function(obj) {
        if(this.fireEvent("okClicked", this))
            this.close();
    },
    cancelClicked: function(obj) {
        this.close();
    },
    tableproperty:function(obj){
        if(this.fireEvent("tableproperty",this))
            var tablepropPanel = new Ext.tablepanel({
                tablebordercolorconfig:this.tablebordercolorconfig,
                tablebordermodeconfig:this.tablebordermodeconfig
            }); 
           this.tablewin=Ext.create('Ext.window.Window', {
               title: 'Table Property',
               id:'tablewin',
               width:420,
               height:350,
               items:[tablepropPanel],
                buttons: [{
                    text: "Update",
                    scope: this,
                    handler: function(){
                        var colorpickernew=Ext.getCmp('colorpicker');
                        if(colorpickernew.value!=undefined || colorpickernew.value!="" ){
                            var selectednewcolor=colorpickernew.value; 
                            var res = selectednewcolor.match(/#/g);
                              if(res==null){
                                selectednewcolor='#'+selectednewcolor;
                                  }
                        }
                        var border1= Ext.getCmp('border1Img');
                        var border2= Ext.getCmp('border2Img');
                        var border3 = Ext.getCmp('border3Img');
                        var border4 = Ext.getCmp('border4Img');
                        if(border1.pressed==true){
                            this.borderstylemode="borderstylemode1";
                        }else if(border2.pressed==true){
                            this.borderstylemode="borderstylemode2";
                        }else if(border3.pressed==true){
                            this.borderstylemode="borderstylemode3";
                        }else if(border4.pressed==true){
                            this.borderstylemode="borderstylemode4";
                        }else{
                            this.borderstylemode="";
                        }
                       pagelayoutproperty[1]=saveTableProperty(selectednewcolor,this.borderstylemode);
                        
                         var table= Ext.getCmp('tablewin');
                        table.close();
                    }
                }] 
           });
        this.tablewin.show();
    },
    headerproperty:function(obj){
        if(this.fireEvent("headerproperty",this))
//            this.headerwindow.show();
    var propPanel = new Ext.HeaderWin({});             
        var headerbutton= new Ext.Window({
            closable:true,
            title: 'Header Property',
            width:450,
            height:500,
            items :propPanel
        })
    
        headerbutton.show();
        headerbutton.doLayout();
    },

    getGridConfigSetting : function() {
        var store = this.reportGrid.getStore();
        documentLineColumns = [];
        
        var recCount = store.getCount();
        var arr=[];
        for(var cnt = 0; cnt < recCount; cnt++) {
            var record = store.getAt(cnt);
            documentLineColumns[cnt] = record.data;
            if(record.data.displayfield.trim()==""){
                WtfComMsgBox(["Alert","Please enter a valid display name"], 0)
                return;
            } else if(record.data.hidecol==false && record.data.colwidth.value==""){
                WtfComMsgBox(["Alert","Please enter a valid width"], 0)
                return;
            }
            if(!record.data.hidecol)
                arr.push(store.indexOf(record));
        }
        var jarray=getJSONArray(this.reportGrid,true,arr);
        return jarray;
    }
});

function changeseq(seq,flag, gridid){
    var store =Ext.getCmp(gridid).getStore();
    var index1 = store.find('seq',seq);
    var orgseq = seq;
    if(index1>-1){
        if(flag=="1"){
            seq++;
        }else if(flag=="0"){
            seq--;
        }
        var record1 = store.getAt(index1);
        var index2 = store.find('seq',seq);
        if(index2>-1){
            var record2 = store.getAt(index2);
            store.remove(record1);
            store.remove(record2);
            if(flag=="0"){
                store.insert(index2,record1);
                store.insert(index1,record2);
            }else if(flag=="1"){
                store.insert(index1,record2);
                store.insert(index2,record1);
            }
            record1.set('seq',seq);
            record2.set('seq',orgseq);
        }
    }
}
 
function getJSONArray(grid, includeLast, idxArr){
    var indices="";
    if(idxArr)
        indices=":"+idxArr.join(":")+":";
    var store=grid.getStore();
    var arr=[];
    //        var fields=store.fields;
    var len=store.getCount()-1;
    if(includeLast)len++;
    for(var i=0;i<len;i++){
        if(idxArr&&indices.indexOf(":"+i+":")<0) continue;
        var recarr=[];
        var recData=store.getAt(i).data;
        for (var prop in recData) {
            recarr.push("'"+prop+"':'"+recData[prop]+"'");
        }
            
        //            for(var j=0;j<fields.length;j++){
        //                var value=rec.data[fields.get(j).name];
        //                recarr.push(fields.get(j).name+":"+value);
        //            }
        arr.push("{"+recarr.join(",")+"}");
    }
    return "["+arr.join(',')+"]";
}

/** *********************************************************************************************** */
/* 							Ext.TemplateHolder component 											*/
/** *********************************************************************************************** */

function openProdWindowAndSetConfig(containerScope, panelId,isEdit,Posx,Posy,documentLineColumns) {
    var _tw = new Ext.ProductGridTemplateWindow({
        tableID : 'itemlistconfig',
        documentLineColumns:documentLineColumns
    });
    _tw.on("okClicked", function(obj){
        var valObj = obj.getGridConfigSetting();
        var jArr = eval(valObj);
       
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
        var c, r, t;
        if(document.getElementById("itemlistconfig")+panelId)      
            document.getElementById("itemlistconfig"+panelId).remove();
        if(document.getElementById("itemlistcontainer"+panelId))
            document.getElementById("itemlistcontainer"+panelId).remove();
        if(isEdit==true)
            Ext.tableinsert=false;
            
        if( Ext.tableinsert==true){
            Ext.Msg.alert('LineItems','Sorry only one lineitem is applicable at a time' );
        }else{
        var tableConfig  = document.createElement('table');
        r = tableConfig.insertRow(0);
        t=tableConfig.insertRow(1);
        tableConfig.setAttribute("class", "linetableproperties");
        tableConfig.setAttribute("id", "itemlistconfigsectionPanelGrid");
        tableConfig.setAttribute("cellSpacing","0");
            if(pagelayoutproperty[1].tableproperties){
                if(pagelayoutproperty[1].tableproperties!=undefined){
                    this.tablebgcolor=pagelayoutproperty[1].tableproperties.bordercolor;
                    this.borderstylemode=pagelayoutproperty[1].tableproperties.borderstylemode;
                }
            var res = this.tablebgcolor.match(/#/g);
            if(res==null){
                this.tablebgcolor='#'+this.tablebgcolor;
            }
             tableConfig.setAttribute("borderstylemode", this.borderstylemode);
             tableConfig.setAttribute("tcolor", this.tablebgcolor);
            if(this.borderstylemode=="borderstylemode2"){
                tableConfig.setAttribute("border",'0px solid ');
                tableConfig.setAttribute('style', 'border-top: thin solid'+this.tablebgcolor+';');
            }else if(this.borderstylemode=="borderstylemode3"){
                tableConfig.setAttribute("border",'0px solid');
                tableConfig.setAttribute('style', 'border-color:'+this.tablebgcolor+';border-left: thin solid'+this.tablebgcolor+';border-right: thin solid '+this.tablebgcolor+';border-top: thin solid'+this.tablebgcolor+';');
            }else if(this.borderstylemode=="borderstylemode4"){
                tableConfig.setAttribute("border",'0px solid');
                tableConfig.setAttribute('style', 'border-color:none;border-left:none;border-right:none;border-top:none;');
            }else{
                tableConfig.setAttribute("border",'1px solid ');
                tableConfig.setAttribute('style', 'border-color:'+this.tablebgcolor+';');
            }
        }else{
                this.tablebgcolor='#000000';
                this.borderstylemode='borderstylemode1';
                tableConfig.setAttribute("borderstylemode", this.borderstylemode);
                tableConfig.setAttribute("tcolor", this.tablebgcolor);
                tableConfig.setAttribute("border",'1px solid');
                tableConfig.setAttribute('style', 'border-color:'+this.tablebgcolor+';');
        }
       
        for(var cnt=0; cnt<jArr.length; cnt++) {
            var obj = jArr[cnt];
            var newTH =document.createElement('th');
            if(jArr[cnt].headerproperty){
                var headerpropertyparam=JSON.parse(jArr[cnt].headerproperty);
                newTH.setAttribute("bgColor", headerpropertyparam.backgroundcolor);
                newTH.setAttribute("align", headerpropertyparam.alignment);
                newTH.style.textAlign=headerpropertyparam.alignment;
                newTH.innerHTML=headerpropertyparam.changedlabel;
                newTH.setAttribute("label", headerpropertyparam.changedlabel);
            }else{
                newTH.innerHTML=obj.displayfield;
                newTH.setAttribute("label", obj.displayfield);
            }
            //            newTH.setAttribute("class", "tpl-content");
            var widthInPercent = (obj.colwidth-2)+"%";// decreased 2% because while rendering header in html we used marging of 2%
            if(this.borderstylemode=="borderstylemode2"){
                newTH.setAttribute('style', 'width: '+widthInPercent+';border-width:thin;border-bottom:thin solid '+this.tablebgcolor+';border-top:thin solid'+this.tablebgcolor+';');
            }else if(this.borderstylemode=="borderstylemode3"){
                newTH.setAttribute('style', 'width: '+widthInPercent+';border-width:thin;border-left: none; border-right: none;border-bottom:thin solid'+this.tablebgcolor+';border-top:thin solid'+this.tablebgcolor+';');
            }else if(this.borderstylemode=="borderstylemode4"){
                newTH.setAttribute('style', 'width: '+widthInPercent+';border-width:thin;border-left:none; border-right:none;border-bottom:thin solid'+this.tablebgcolor+';border-top:thin solid'+this.tablebgcolor+';');
            }else{
                if(this.tablebgcolor==undefined){
                    newTH.setAttribute('style', 'width: '+widthInPercent);
                }else{
                    newTH.setAttribute('style', 'width: '+widthInPercent+';border-color:'+this.tablebgcolor+';');
                }
            }
            newTH.setAttribute("colwidth", obj.colwidth);
            newTH.setAttribute("coltotal", obj.coltotal);
            newTH.setAttribute("showtotal", obj.showtotal);
            newTH.setAttribute("commaamount", obj.commaamount!=""?obj.commaamount:"");
            newTH.setAttribute("seq", obj.seq);
            newTH.setAttribute("xtype", obj.xtype);
            newTH.setAttribute("headerproperty", obj.headerproperty);
            newTH.setAttribute("fieldid", obj.fieldid);
            newTH.setAttribute("headercurrency", obj.headercurrency);
            newTH.setAttribute("recordcurrency", obj.recordcurrency);
            newTH.setAttribute("decimalpoint", obj.decimalpoint!=""?obj.decimalpoint:"")
                if(obj.columnname=="Quantity With UOM"||obj.columnname=="Actual Quantity With UOM"||obj.columnname=="Delivered Quantity With UOM"||obj.columnname=="Received Quantity With UOM"){
                    newTH.setAttribute("basequantitywithuom",true);
                }else{
                    newTH.setAttribute("basequantitywithuom",false);
                } 
                if(obj.columnname=="Rate"){
                    newTH.setAttribute("baserate",true);
                }else{
                    newTH.setAttribute("baserate",false);
                } 
                
            newTH.value = obj.fieldid;
            newTH.cellIndex=obj.seq;
            r.appendChild(newTH);
            //             var tabletd=t.insertCell(cnt);;
            var tabletd=document.createElement('td');
            tabletd.cellIndex=newTH.cellIndex;
            tabletd.setAttribute("cellIndex",newTH.cellIndex);
            tabletd.innerHTML="&nbsp;";
            if(this.borderstylemode=="borderstylemode2"){
                tabletd.setAttribute('style', 'width: '+widthInPercent+';border-width: thin;border-bottom:thin solid '+this.tablebgcolor+';border-top:thin solid'+this.tablebgcolor+';');
            }else if(this.borderstylemode=="borderstylemode3"){
                tabletd.setAttribute('style', 'width: '+widthInPercent+';border-width:thin;border-left: none; border-right: none;border-bottom:thin solid '+this.tablebgcolor+';border-top:thin solid '+this.tablebgcolor+';');
            }else if(this.borderstylemode=="borderstylemode4"){
                tabletd.setAttribute('style', 'width: '+widthInPercent+';border-width:thin;border-left: none; border-right: none;border-bottom: none;border-top: none;');
            }else{
                if(this.tablebgcolor==undefined){
                    tabletd.setAttribute('style', 'width: '+widthInPercent);
                }else{
                    tabletd.setAttribute('style', 'width: '+widthInPercent+';border-color:'+this.tablebgcolor+';');
                }
            }
            tabletd.setAttribute("colwidth", obj.colwidth);
            t.appendChild(tabletd);
            tableConfig.appendChild(r);
        }
        tableConfig.appendChild(r);
        tableConfig.appendChild(t);
   
        var designerPanel = Ext.getCmp('sectionPanelGrid');
        var PosX = designerPanel.cursorCustomX;
        var PosY = designerPanel.cursorCustomY;
           if(isEdit==true){
               PosX=Posx;
               PosY=Posy;
           }
           
        var field = getlineitemstable(PosX,PosY,tableConfig.outerHTML,designerPanel,Ext.getCmp('propertypanel'));
        designerPanel.items.add(field);
        Ext.tableinsert=true;
        isEdit=false; 
        designerPanel.doLayout();
}
    }, containerScope);
    _tw.show();
}//endOf_openProdWindowAndSetConfig

Ext.TemplateHolder = Ext.extend(Ext.Component, {
    defaultMenuConfig:{
        tag: 'ul', 
        cls: 'edit-links', 
        children: [
        {
            tag: 'li', 
            menuname:'edit', 
            cls: 'edit tpl-link', 
            html: 'edit'
        },

        {
            tag: 'li', 
            menuname:'remove', 
            cls: 'remove tpl-link', 
            html: 'remove'
        }
        ]
    },
    onRender: function(ct, position){
        Ext.TemplateHolder.superclass.onRender.call(this, ct, position);
        this.defaultMenuConfig.cls = "section-menu "+ (this.defaultMenuConfig.cls||"");
        this.addButtons();
        this.setHtml(this.bodyHtml);
    },
    
    addButtons : function() {
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
        Ext.get(this.contentHolder).addListener("click", this.contentClicked,this);
        this.elDom.appendChild(this.table1);
    },
    
    appendSectionMenu : function(el, menuConfig){
        if(menuConfig){
            menuConfig.cls = "section-menu "+ (menuConfig.cls||"");
        }
        Ext.DomHelper.insertFirst(el, menuConfig || this.defaultMenuConfig);
    },
    removeSectionMenu : function(el){
        var chArr= Ext.DomQuery.select("ul[class*=section-menu]",el);
        for(var i=0;i<chArr.length;i++){
            el.removeChild(chArr[i]);
        }
    },   
    setHtml: function(html){
        this.contentHolder.innerHTML = html;
        var sectionArray = Ext.DomQuery.select("*[class*=tpl-content]", this.contentHolder);
        for( var i =0 ; i< sectionArray.length;i++){
            this.appendSectionMenu(sectionArray[i]);
        }       
    },
    getHtml: function(){
        var x = this.contentHolder.cloneNode(true);
        var sectionArray = Ext.DomQuery.select("*[class*=tpl-content]", x);
        for( var i =0 ; i< sectionArray.length;i++){
            this.removeSectionMenu(sectionArray[i]);
        }
        return x.innerHTML;
    },   
    removeSection:function(sectionEl){
        sectionEl.parentNode.removeChild(sectionEl);
    },
    contentClicked: function(e){
        var _to = e.getTarget();
        if(_to.className.indexOf("tpl-content") != -1 || _to.className.indexOf("edit tpl-link") != -1){
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
        if( _to.className.indexOf("remove tpl-link") != -1){
            var sectionArray = Ext.DomQuery.select("*[class*=tpl-content]", this.contentHolder);
            //            if(sectionArray.length>1){
            var contentEl = e.getTarget(".tpl-content");
            Ext.MessageBox.show({
                title:'Confirm',
                msg:'Do you really want to delete this section?',//"Do you really want to delete this section?",
                icon:Ext.MessageBox.QUESTION,
                buttons:Ext.MessageBox.YESNO,
                scope:this,
                fn:function(button){
                    if(button=='yes')
                    {
                        this.destroy();
                    }else{
                        return;
                    }
                }
            });
        //            } else {
        //                WtfComMsgBox([WtfGlobal.getLocaleText("acc.msg.ALERTTITLE"),WtfGlobal.getLocaleText("acc.template.dontdelallsecmsg")]);
        //            }
        }
    },
    getPlainText: function(){
        var htm = this.elDom.dom.innerHTML;
        htm = htm.replace(/<p>/g, "");
        htm = htm.replace(/<\p>/g, "");
        htm = htm.replace(/<P>/g, "");
        htm = htm.replace(/<\P>/g, "");
        htm = htm.replace(/&nbsp;/g, "");
        htm = Ext.util.Format.stripTags(htm);
        return htm;
    },
    
    applyTemplateTheme: function(theme){
        if(theme) {
        	
            for(elAttr in theme){
                var elArr = Ext.DomQuery.select(elAttr,this.table1.parentNode);
                if(elArr.length==0){
                    ResponseAlert(99);
                    return;
                }else{
                    for(var i=0; i< elArr.length; i++){
                        var el = elArr[i];
                        for(styleAttr in theme[elAttr])
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
    return Ext.getCmp('hidden_fieldID').getValue()!=""
}
function createExtComponent(designerPanel, propertyPanel, fieldTypeId, label, X, Y, extraConfig,selectfieldbordercolor) {
    var field = Ext.create('Ext.Component', {
        x:X,
        y:Y,
        width : extraConfig && extraConfig.width? extraConfig.width : 80,
        height: extraConfig && extraConfig.height? extraConfig.height : 20,
        style : {
            borderColor:(selectfieldbordercolor!=null&&selectfieldbordercolor!='#B5B8C8')?selectfieldbordercolor:'#B5B8C8', 
            borderStyle:'solid', 
            borderWidth:'1px', 
            position:'absolute'
        },
        draggable : true,
        initDraggable: function() {
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
        },//initDraggable
        resizable: true,
        autoDestroy: true,
        fieldTypeId : fieldTypeId,
        html : label,
        onRender: function() {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);
            this.el.on('click', function(eventObject, target, arg) {
                var component = designerPanel.queryById(this.id)
                if(component) {
                    if(Ext.getCmp('contentImage'))
                        Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                    eventObject.stopPropagation();
                    getPropertyPanel(component, designerPanel,propertyPanel);
                }
            });
        },
        listeners: {
            onMouseUp: function(field) {
                field.focus();
            }
            , 
            removed : function() {
                onDeleteUpdatePropertyPanel();
            //                            propertyPanel.body.update("<div style='padding:10px;font-weight: bold'>Select field to delete</div>");
            }
        }
    });
    field.on('drag',showAlignedComponent, field);
    field.on('dragend',removeAlignedLine, field);
    return field;
}//createExtComponent()

function createExtFieldSet(designerPanel, propertyPanel, fieldTypeId, label, X, Y) {
    var field = Ext.create('Ext.form.FieldSet', {
        x:X,
        y:Y,
        width : 80,
        height: 100,
        style : {
            borderColor:'#B5B8C8', 
            borderStyle:'solid', 
            borderWidth:'1px', 
            position:'absolute'
        },
        draggable : true,
        initDraggable: function() {
            var me = this,
            ddConfig;
            ddConfig = Ext.applyIf({
                el: me.el//,-Reimius, I commented out the next line that delegates the dragger to the header of the window
                        //delegate: '#' + Ext.escapeId(me.header.id)
            }, me.draggable);
            me.dd = new Ext.util.ComponentDragger(this, ddConfig);
            me.relayEvents(me.dd, ['dragstart', 'drag', 'dragend']);
        },//initDraggable
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
        fieldTypeId : fieldTypeId,
        //        html : label,
        onRender: function() {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);
            this.el.on('click', function(eventObject, target, arg) {
                var component = designerPanel.queryById(this.id)
                if(component) {
                    getPropertyPanel(component, designerPanel,propertyPanel);
                }
            });
        },
        listeners: {
            onMouseUp: function(field) {
                field.focus();
            }
            /*,
            move: function(field) {
                alert('sd');
            }*/
        }
    });
    field.on('drag',showAlignedComponent, field);
    field.on('dragend',removeAlignedLine, field)
    return field;
}

function createExtImgComponent(designerPanel, propertyPanel, fieldTypeId, src, X, Y, obj) {
    var field = Ext.create('Ext.Img', {
        width: obj ? obj.width : 80,
        height: obj ? obj.height : 60,
        x:X,
        y:Y,
        draggable: true,
        initDraggable: function() {
            var me = this,
            ddConfig;
            ddConfig = Ext.applyIf({
                el: me.el//,-Reimius, I commented out the next line that delegates the dragger to the header of the window
                        //delegate: '#' + Ext.escapeId(me.header.id)
            }, me.draggable);
            me.dd = new Ext.util.ComponentDragger(this, ddConfig);
            me.relayEvents(me.dd, ['dragstart', 'drag', 'dragend']);
        },//initDraggable
        resizable: true,
        fieldTypeId : fieldTypeId,
        style : {
            borderColor:'#B5B8C8', 
            borderStyle:'solid', 
            borderWidth:'1px', 
            position:'absolute'
        },
        src : src,
        onRender: function() {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);
            this.el.on('click', function(eventObject, target, arg) {
                var component = designerPanel.queryById(this.id)
                if(component) {
                    if(Ext.getCmp('contentImage')) {
                        Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                    }
                    eventObject.stopPropagation();
                    getPropertyPanel(component, designerPanel,propertyPanel);
                }
            });
        },
        listeners: {
            onMouseUp: function(field) {
                field.focus();
            }, 
            removed : function() {
                onDeleteUpdatePropertyPanel();
            //                            propertyPanel.body.update("<div style='padding:10px;font-weight: bold'>Select field to delete</div>");
            }
        }
    })
    field.on('drag',showAlignedComponent, field);
    field.on('dragend',removeAlignedLine, field);
    return field;
}

function WtfComMsgBox(choice, type,iswait,feature) {
    if(iswait==undefined || iswait==null)
        iswait=false;
    var strobj = [];
    var title="";
    var iconType = Ext.MessageBox.INFO;
    strobj = [choice[0], choice[1]];
    if(type == 0)
        iconType = Ext.MessageBox.INFO;
    else if(type == 1)
        iconType = Ext.MessageBox.ERROR;
    else if(type == 2)
        iconType = Ext.MessageBox.WARNING;
    else if(type == 3)
        iconType = Ext.MessageBox.INFO;

    if(iswait) {
        Ext.MessageBox.show({
            msg: strobj,
            width:320,
            wait:true,
            title:title,
            waitConfig: {
                interval:100
            }
        });
    } else {
        Ext.MessageBox.show({
            title: strobj[0],
            msg: strobj[1],
            width:370,
            buttons: Ext.MessageBox.OK,
            animEl: 'mb9',
            icon: iconType
        });
    }
}
            
function searchRecord (store, ID, idname) {
    var index =  store.findBy(function(record) {
        if(record.get(idname)==ID)
            return true;
        else
            return false;
    });
    if(index == -1)
        return null;

    return store.getAt(index);
}
function is_html(htmlstring) {
    return /<(br|basefont|hr|input|source|frame|param|area|meta|!--|col|link|option|base|img|wbr|!DOCTYPE).*?>|<(a|abbr|acronym|address|applet|article|aside|audio|b|bdi|bdo|big|blockquote|body|button|canvas|caption|center|cite|code|colgroup|command|datalist|dd|del|details|dfn|dialog|dir|div|dl|dt|em|embed|fieldset|figcaption|figure|font|footer|form|frameset|head|header|hgroup|h1|h2|h3|h4|h5|h6|html|i|iframe|ins|kbd|keygen|label|legend|li|map|mark|menu|meter|nav|noframes|noscript|object|ol|optgroup|output|p|pre|progress|q|rp|rt|ruby|s|samp|script|section|select|small|span|strike|strong|style|sub|summary|sup|table|tbody|td|textarea|tfoot|th|thead|time|title|tr|track|tt|u|ul|var|video).*?<\/\2>/i.test(htmlstring) ;
}
            
function isValidSession(result) {
    if(!result.valid) {
        WtfComMsgBox(["Error","Session invalid."], 1);
    }
    return result.valid;
}
