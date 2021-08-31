function openAgeingDetailsSettingWindow(parentPanel,obj,isEdit){
    var _tw = new Ext.AgeingTableGridWindow({
        tableID: 'ageingtableconfig',
        ageingColumns: ageingColumns
    });
    _tw.on("okClicked", function (config) {
        var header, row;
        var cellCounter;
        var valObj = config.getGridConfigSetting();
        var jArr = eval(valObj);
        if(isEdit==true){
            if (document.getElementById("ageingtableconfig"))
                remove(document.getElementById("ageingtableconfig"));
            if (document.getElementById("idageingtablecontainer"))
                remove(document.getElementById("idageingtablecontainer"));
            parentPanel=obj.ownerCt;
        }
      
        var ageingTable = document.createElement('table');
        ageingTable.setAttribute("class", "ageingtable");
        ageingTable.setAttribute("id", "idageingtable");
        ageingTable.setAttribute("cellSpacing", "0");
        ageingTable.setAttribute("onclick", "getAgeingTablePropertyPanel(event,this)");
        header = ageingTable.insertRow(0);
        
        for ( cellCounter = 0; cellCounter < jArr.length; cellCounter++ ) {
            var headerCell = document.createElement("td");
            var cellConfig = jArr[cellCounter];
            headerCell.innerHTML = cellConfig.displayfield? cellConfig.displayfield : cellConfig.columnname;
            headerCell.setAttribute("header", cellConfig.displayfield? cellConfig.displayfield : cellConfig.columnname)
            headerCell.setAttribute("columnname",cellConfig.fieldid)
            headerCell.style.width= cellConfig.colwidth+"%";
            headerCell.style.padding= "2px";
            headerCell.setAttribute("widthint", cellConfig.colwidth.replace("%",""));
            headerCell.setAttribute("width", cellConfig.colwidth.replace("%",""));
            
            header.appendChild(headerCell);
        }
        row = ageingTable.insertRow(1);
        for ( cellCounter = 0; cellCounter < jArr.length; cellCounter++ ) {
            var rowCell = document.createElement("td");
            var div = document.createElement('div');
            var dataCellConfig = jArr[cellCounter];
            div.innerHTML = jArr[cellCounter].columnname;
            div.setAttribute("label", dataCellConfig.columnname);
            div.setAttribute("class", "fieldItemDivBorder");
            div.setAttribute("seq", dataCellConfig.seq);
            div.setAttribute("decimalpoint", dataCellConfig.decimalpoint != "" ? dataCellConfig.decimalpoint : "2")
            div.setAttribute("recordcurrency", dataCellConfig.recordcurrency);
            div.setAttribute("commaamount", dataCellConfig.commaamount);
            div.setAttribute("style", "margin:5px");
            rowCell.appendChild(div);
            rowCell.style.padding= "2px";
            row.appendChild(rowCell);
        }
        var field = createAgeingDetailsTable(obj,ageingTable.outerHTML,parentPanel,Ext.fieldID.insertAgeingTable,isEdit);
        parentPanel.add(field);
        initTreePanel();
    }, obj);
    _tw.show();
}


function createAgeingDetailsTable(obj,tablehtml,designerPanel,fieldTypeId,isEdit) {
    var marginTop = (obj && obj.marginTop) || (obj && obj.marginTop === 0)  ? obj.marginTop.toString().replace('px','') + 'px'  : '0px'; 
    var marginBottom = (obj && obj.marginBottom) || (obj && obj.marginBottom === 0) ? obj.marginBottom.toString().replace('px','') + 'px' : '5px';
    var marginLeft = (obj && obj.marginLeft)|| (obj && obj.marginLeft === 0) ? obj.marginLeft.toString().replace('px','') + 'px' : '0px'; 
    var marginRight = (obj && obj.marginRight) || (obj && obj.marginRight === 0) ? obj.marginRight.toString().replace('px','') + 'px' : '0px'; //ERP-19449
    var tableWidth = (obj && obj.tableWidth)? obj.tableWidth : 100;
    var borderColor = (obj && obj.borderColor)? obj.borderColor : "#000000";
    var backgroundColor = (obj && obj.backgroundColor)? obj.backgroundColor : "#FFFFFF";
    var databackgroundColor = (obj && obj.databackgroundColor)? obj.databackgroundColor : "#FFFFFF";
    var textColor = (obj && obj.textColor)? obj.textColor : "#000000";
    var dataTextColor = (obj && obj.dataTextColor)? obj.dataTextColor : "#000000";
    var headerColor = (obj && obj.headerColor)? obj.headerColor : "#FFFFFF";
    var tableAlign = (obj && (obj.tableAlign || obj.tableAlign === 0))? obj.tableAlign : 1; 
    var noofintervals=(obj && obj.noofintervals)? obj.noofintervals : 3; 
    var ismulticurrency=(obj && obj.ismulticurrency)? obj.ismulticurrency : false;
    var checklistValue=(obj && obj.checklistValue)? obj.checklistValue : "rating";
    var isCustomerVendorCurrency=(obj && obj.isCustomerVendorCurrency)? obj.isCustomerVendorCurrency : false;
    var isincludecurrent=(obj && obj.isincludecurrent)? obj.isincludecurrent : false;
    var fontFamily = (obj && obj.fontFamily) ? obj.fontFamily : '';
    var align = (obj && obj.align) ? obj.align : "left";
    var fontSize = (obj && obj.fontSize) ? obj.fontSize : 0;
    var bold = (obj && obj.bold) ? obj.bold : "";
    var italic = (obj && obj.italic) ? obj.italic : "";
    var underline = (obj && obj.underline) ? obj.underline : "";
    var aligndata = (obj && obj.aligndata) ? obj.aligndata : "left";
    var fontSizedata = (obj && obj.fontSizedata) ? obj.fontSizedata : 0;
    var fontFamilydata = (obj && obj.fontFamilydata) ? obj.fontFamilydata : '';
    var bolddata = (obj && obj.bolddata) ? obj.bolddata : "";
    var italicdata = (obj && obj.italicdata) ? obj.italicdata : "";
    var underlinedata = (obj && obj.underlinedata) ? obj.underlinedata : "";
    var intervalType = (obj && obj.intervalType) ? obj.intervalType : "";
    var intervalPlaceHolder = (obj && obj.intervalPlaceHolder) ? obj.intervalPlaceHolder : ""; //ERP-28745
    var intervalText = (obj && obj.intervalText) ? obj.intervalText : "";

    var columns = ageingColumns.length?ageingColumns:(obj && obj.columns)?obj.columns:[]
    if(isEdit!=true && obj.columns!=undefined){
        columns = obj.columns;
    }
   
    ageingColumns = columns;
    var field = Ext.create('Ext.Component', {
        
        labelhtml:tablehtml,
        width: tableWidth+"%",
        unit:"",
        id:"idageingtablecontainer",
        backgroundColor:backgroundColor,
        borderColor:borderColor,
        headerColor:headerColor,
        tableAlign:tableAlign,
        interval:30,
        noofintervals:noofintervals,
        ismulticurrency:ismulticurrency,
        checklistValue:checklistValue,
        isCustomerVendorCurrency:isCustomerVendorCurrency,
        isincludecurrent:isincludecurrent,
        marginTop : marginTop,
        marginBottom:marginBottom,
        marginLeft:marginLeft,
        marginRight:marginRight,
        tableWidth:tableWidth,
        bold:bold,
        fontSize:fontSize,
        intervalType:intervalType,
        intervalPlaceHolder:intervalPlaceHolder, //ERP-28745
        intervalText:intervalText,
        italic:italic,
        underline:underline,
        align:align,
        fontFamily:fontFamily,
        aligndata : aligndata,
        fontSizedata : fontSizedata,
        fontFamilydata : fontFamilydata,
        bolddata : bolddata,
        italicdata :italicdata,
        underlinedata : underlinedata,
        databackgroundColor:databackgroundColor,
        textColor:textColor,
        dataTextColor:dataTextColor,
        columns:columns,
        cls : 'fieldItemDivBorder',
        fieldDefaults: {
            anchor: '100%'
        },
        layout: {
            type: 'vbox',
            align: 'stretch'  
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
        resizable: false,
        containerId: designerPanel.id,
        fieldType: fieldTypeId,
        html: tablehtml,
        onRender: function () {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);
            //            if ( fieldAlignment == "2" ) {
            //                this.el.dom.style.display = "inline-table"; 
            //            } else {
            //                this.el.dom.style.display = "inline-block"; 
            //            }
           
           this.el.dom.style.marginTop = marginTop;        
           this.el.dom.style.marginBottom = marginBottom;  
           this.el.dom.style.marginLeft = marginLeft;      
           this.el.dom.style.marginRight = marginRight; 
           var table = document.getElementById('idageingtable');
            if(table){
                table.setAttribute("panelid", designerPanel.id);
            }
            var rows=table.rows;
            var cells=table.rows[0].cells;
            for( var i=0; i< cells.length; i++ ){
                cells[i].style.textDecoration=underline;
                cells[i].style.fontFamily = fontFamily ;
                cells[i].style.fontWeight = bold ;
                cells[i].style.fontStyle = italic ;
                cells[i].style.textAlign = align;
                cells[i].style.paddingLeft="2px";
                cells[i].style.paddingRight="2px";
                cells[i].style.backgroundColor=backgroundColor;
                cells[i].style.color=textColor;
                cells[i].style.borderColor = borderColor;
                    if (fontSize > 0) {
                        cells[i].style.fontSize=fontSize+"px";
                    }
                    else {
                        cells[i].style.fontSize="";
                    }
                }
            var datacells=table.rows[1].cells;
            for( var i=0; i< cells.length; i++ ){
                datacells[i].style.textDecoration=underlinedata;
                datacells[i].style.fontFamily = fontFamilydata ;
                datacells[i].style.fontWeight = bolddata ;
                datacells[i].style.fontStyle = italicdata ;
                datacells[i].style.textAlign = aligndata;
                datacells[i].style.paddingLeft="2px";
                datacells[i].style.paddingRight="2px";
                datacells[i].style.backgroundColor=databackgroundColor;
                datacells[i].style.color=dataTextColor;
                datacells[i].style.borderColor = borderColor;
                if (fontSizedata > 0) {
                    datacells[i].style.fontSize=fontSizedata+"px";
            }
                else {
                    datacells[i].style.fontSize="";
                }
            }
            this.el.on("contextmenu", function (event, ele) {
                if (selectedElement != null || selectedElement != undefined)
                {
                    if(Ext.get(selectedElement)!=null){
                        Ext.get(selectedElement).removeCls("selected");
                    }
                }
                event.stopEvent();
                var id = ele.parentNode.parentNode.parentNode.attributes["panelid"].value;
                selectedElement = id;
                createContextMenu(Ext.fieldID.insertAgeingTable,id);
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
    });
    return field;
}


Ext.AgeingTableGridWindow = function (conf) {
    Ext.apply(this, conf);
    this.addEvents({
        "okClicked": true
    });
    this.createEditor();
    this.lineFieldsStore = new Ext.create('Ext.data.Store', {
        fields: ['columnname', 'displayfield', 'hidecol', 'seq', 'fieldid', 'coltotal', 'colwidth', 'xtype', 'headerproperty', 'showtotal', 'recordcurrency', 'headercurrency', 'decimalpoint', 'colno'],
        data: ageingColumnsArray,
        autoLoad: true,
        sorters: [
        {
            property : 'columnname',
            direction: 'ASC'
        }
        ]
    });
    this.selectField = new Ext.form.field.ComboBox({
        fieldLabel:"Select Field",
        padding:'10 10 10 10',
        displayField:'columnname',
        queryMode: 'local',
        width:250,
        valueField:'fieldid',
        store:this.lineFieldsStore,
        scope:this
    });
    this.addBtn = Ext.create('Ext.Button', {
        text: 'Add',
        margin:'10 10 10 10',
        id: 'idAddColumnToAgeingTable',
        scope:this,
        handler:function(){
            if(this.selectField && this.selectField.getValue() != undefined && this.selectField.getValue != ""){
                var rec = searchRecord(this.lineFieldsStore, this.selectField.getValue(), 'fieldid');
                if(rec){
                    if(this.ageingStore){
                        var recordPresent = false;
                        this.ageingStore.each(function(record){
                            if(record.data.columnname == rec.data.columnname){
                                recordPresent=true;
                            }
                        });
                        //get interval type
                        var intervaltype = getEXTComponent(selectedElement) != undefined ? getEXTComponent(selectedElement).intervalType : "";
                        //If interval type is Months and selected field is "Accrued Balance" then don't allow to add in ageing table'
                        if(rec.data.fieldid == "accruedbalance" && intervaltype == "Months"){
                            Ext.MessageBox.show({
                                title: 'Alert',
                                msg: 'Accrued Balance column can be used only for Days interval.',
                                icon: Ext.MessageBox.QUESTION,
                                buttons: Ext.MessageBox.OK
                            });
                            this.selectField.reset();
                            return false;
                        }
                        if(recordPresent){
                            Ext.MessageBox.show({
                                title: 'Alert',
                                msg: 'Selected column is already present',
                                icon: Ext.MessageBox.QUESTION,
                                buttons: Ext.MessageBox.OK
                            });
                        } else{
                            rec.data.seq = this.ageingStore.data.items.length + 1;
                            rec.data.hidecol = false;
                            var max = 0;
                            this.ageingStore.each(function(record){
                                max=Math.max(record.data.colno, max);;
                            });
                            rec.data.colno = ++max;
                           if(rec.data.xtype=="2"){
                                rec.data.decimalpoint = _amountDecimalPrecision;
                            }
                            this.ageingStore.add(rec);
                        }
                    }
                }
            }
        }
    });
    this.tablename = (_CustomDesign_moduleId == Ext.moduleID.MRP_WORK_ORDER_MODULEID ? "Checklist Table" : "Ageing Table");
    Ext.AgeingTableGridWindow.superclass.constructor.call(this, {
        width: 700,
        height: 500,
        resizable: false,
        bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF !important;background: #FFFFFF !important',
        title: 'Configure '+this.tablename+' ', //"Edit Your Content",
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
                         +"<div style='font-size:12px;font-style:bold;float:left;margin:7px 0px 0px 10px;width:100%;position:relative;'><b>"+((this.ageingStore.data.items.length > 0)?"Edit "+this.tablename:"Create "+this.tablename)+"</b></div>"
                         +"<div style='font-size:10px;float:left;margin:5px 0px 10px 10px;width:100%;position:relative;'><ul style='list-style-type:disc;padding-left:15px;'>"
                         +"<li>Select a column from <b>Select Field</b> and click on <b>Add</b> to add specific column to "+this.tablename+"</li>"
                         +"<li><b>Note :</b> Use \"Accrued Balance\" field only for <b>Days interval</b>.</li></ul></div></div></div>"
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
                columnWidth : .40,
                border:false,
                items:this.selectField
            },
            {
                columnWidth : .60,
                border:false,
                items:this.addBtn
            }
            ]
        },
        this.ageingGrid
        ],
        buttons: [
        {
            text: "OK",
            scope: this,
            handler:function(){
                var totalWidth=0;
                var store = this.ageingGrid.getStore();
                if(store && store.data.length > 0){
                    var records = store.data.items;
                    for(var i=0 ; i < records.length ; i++ ){
                        var rec= records[i];
                        if(rec.data){
                            totalWidth += rec.data.colwidth;
                        }
                    }
                    if(totalWidth != 100){
                        Ext.MessageBox.show({
                            title: 'Info',
                            msg: 'Sum of widths of '+this.tablename+' columns should be equal to 100. ',
                            icon: Ext.MessageBox.INFO,
                            buttons: Ext.MessageBox.OK
                        });
                    }else{
                        if(Ext.getCmp(selectedElement).isPanel){//To remove space(&nbsp;) from section if empty(No any element inside). //Check for insert Line Item table in empty section
                            Ext.getCmp(selectedElement).update("");
                        }
                        this.okClicked();
                    } 
                } else{
                    Ext.MessageBox.show({
                        title: 'Info',
                        msg: 'Please select at least one column',
                        icon: Ext.MessageBox.INFO,
                        buttons: Ext.MessageBox.OK
                    });
                }
            }
        } , {
            text: "Cancel",
            scope: this,
            handler: this.cancelClicked
        }]
    });
};


Ext.extend(Ext.AgeingTableGridWindow, Ext.Window, {
    onRender: function (conf) {
        Ext.AgeingTableGridWindow.superclass.onRender.call(this, conf);
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
            minValue: 1
        });
        this.columnWidth = new Ext.form.NumberField({
            validateOnBlur: true,
            allowBlank: false,
            maxValue: 100,
            minValue: 1
        });
        this.ageingCM = [
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
            editor:this.columnWidth,
            width: 100
        },{
            header: 'Column No.', 
            dataIndex: "colno",
            editor: this.columnNo,
            width: 80
        },{
            header: 'Records with Currency',
            dataIndex: 'recordcurrency',
            xtype: 'checkcolumn',
            hidden: _CustomDesign_moduleId == Ext.moduleID.MRP_WORK_ORDER_MODULEID,
            width: 80,
            renderer:
            function (val, m, rec) {
                if (val == '' || val == "false")
                    val = false;
                else
                    val = true;
                if (rec.data.columnname == "Total" || rec.data.columnname == "Interval" || rec.data.columnname == "Accrued Balance") {
                    return (new Ext.ux.CheckColumn()).renderer(val);
                }
                else {
                    return '';
                }
            }
        },{
            header: 'Decimal Points', //"Decimal Points",
            dataIndex: "decimalpoint",
            editor: this.decimal,
            hidden: _CustomDesign_moduleId == Ext.moduleID.MRP_WORK_ORDER_MODULEID,
            width: 80
        },{
                header: 'Amount in Comma',
                dataIndex: 'commaamount',
                xtype: 'checkcolumn',
                hidden: _CustomDesign_moduleId == Ext.moduleID.MRP_WORK_ORDER_MODULEID,
                width: 80,
                renderer : 
                function(val, m, rec){               
                    if (val == ''|| val == "false" || val == undefined)
                        val= false;
                    else
                        val=true;
                    if(rec.data.columnname == "Total" || rec.data.columnname == "Interval" || rec.data.columnname == "Accrued Balance"){
                        return (new Ext.ux.CheckColumn()).renderer(val);
                    }
                    else{
                        return '';
                    }
                }
            },{
            header: 'Actions', //"Actions",
            dataIndex: 'id',
            width: 100,
            renderer: function (value, css, record, row, column, store) {
                var actions = "<image src='images/up.png' title='Move Up' onclick=\"beforechangeseq('" + record.get('seq') + "',0, 'ageingTableGrid')\"/>" +
                "<image src='images/down.png' style='padding-left:5px' title='Move Down' onclick=\"beforechangeseq('" + record.get('seq') + "',1, 'ageingTableGrid')\"/>";
                return actions;
            }
        },
        {
            header: 'Remove',
            width: 100,
            renderer: function (value, css, record, row, column, store) {
                return "<image src='images/Delete.png' onclick=\"beforedeleteRecord('" + record.get('columnname') + "', 'ageingTableGrid')\"/>";
            }
        }
        ];

        this.ageingStore = new Ext.create('Ext.data.Store', {
            fields: ['columnname', 'displayfield', 'hidecol', 'seq', 'fieldid', 'coltotal', 'colwidth', 'xtype', 'headerproperty', 'showtotal', 'recordcurrency', 'headercurrency', 'decimalpoint','commaamount','colno'],
            data: ageingColumns, // [["Product Name","Product Name",false,'0','1'],["Product Description","Product Description",false,'1','2'],["Rate","Rate",false,'2','3'], ["Quantity","Quantity",false,'3','4'],["Total Amount","Total Amount",false,'4','5']/*,["Campaign","Campaign"]*/],
            autoLoad: true
        });
        var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
            clicksToEdit: 2
        });
        this.ageingGrid = Ext.create('Ext.grid.Panel', {
            columns: this.ageingCM,
            store: this.ageingStore,
            clicksToEdit: 2,
            height:280,
            viewConfig: {
                forceFit: true
            },
            renderTo: this.id,
            id: 'ageingTableGrid', // don't delete this id
            plugins: [cellEditing]
        });

        this.ageingGrid.on('cellclick', this.cellclickhandle, this);
        this.ageingGrid.on('edit', this.afterEdit, this);
        this.ageingGrid.on('beforeedit', this.beforeEdit, this);
        
    },
    
    beforeEdit: function (e) {
        if (e.context.field == 'decimalpoint') {
            var str = e.context.record.data.columnname;
            var str = str.toLowerCase();
            if (str == "interval" || str == "total" || str == "accruedbalance") {
                return true;
            } else {
                return false;
            }
        }
       
    },
   
    cellclickhandle: function (scope, td, cellIndex, record, tr, rowIndex, e, eOpts) {
        this.record = record;
        this.record.data.displayfield = this.record.data.displayfield.replace(/&nbsp;/g,' ');
    },
    
    afterEdit: function ( obj ) {
        var fieldnos = obj.context.store.data.items.length;
        var distinctcolumnarr = [];
        if (obj.context.field == 'colno') {
            for ( var index = 0,index2 = 0 ; index < fieldnos ; index++ ) {
                if ( !obj.context.store.data.items[index].data.hidecol ) {
                    var colno = obj.context.store.data.items[index].data.colno;
                    if ( !this.isinarray(colno, distinctcolumnarr) ) {
                        distinctcolumnarr[index2]=colno;
                        index2++;
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
    getGridConfigSetting: function () {
        var store = this.ageingGrid.getStore();
        ageingColumns = [];

        var recCount = store.getCount();
        var arr = [];
        for (var cnt = 0; cnt < recCount; cnt++) {
            var record = store.getAt(cnt);
            ageingColumns[cnt] = record.data;
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
        var jarray = getJSONArray(this.ageingGrid, true, arr);
        return jarray;
    }
});

function getAgeingTablePropertyPanel(event,ele) {
                 
    event.stopPropagation();
    Ext.get(selectedElement).removeCls("selected");
    selectedElement = ele.parentNode.id;
    Ext.getCmp(selectedElement).addClass('selected');
    createAgeingTablePropertyPanel(ele);
}

function createAgeingTablePropertyPanel( ele ) {
    var tablename = (_CustomDesign_moduleId == Ext.moduleID.MRP_WORK_ORDER_MODULEID ? "Checklist Table" : "Ageing Table");
    var selectedEle=getEXTComponent(selectedElement);
    var topMargin =  new Ext.form.NumberField({
        fieldLabel: 'Top Margin',
        id: 'idtopmargin',
        labelWidth: 100,
        value: selectedEle.marginTop?selectedEle.marginTop:0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (e) {
                var selected = getEXTComponent(selectedElement);
                var tableContainer=document.getElementById("idageingtablecontainer");
                tableContainer.style.marginTop = e.getValue()+"px";
                selected.marginTop = e.getValue();
            }
        }
    });
    var bottomMargin = new Ext.form.NumberField({
        fieldLabel: 'Bottom Margin',
        id: 'idbottommargin',
        labelWidth: 100,
        value: selectedEle.marginBottom?selectedEle.marginBottom:5,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (e) {
                var selected = getEXTComponent(selectedElement);
                var tableContainer=document.getElementById("idageingtablecontainer");
                tableContainer.style.marginBottom = e.getValue()+"px";
                selected.marginBottom = e.getValue();
            }
        }
    });
    var leftMargin = new Ext.form.NumberField({
        fieldLabel: 'Left Margin',
        id: 'idleftmargin',
        labelWidth: 100,
        value: selectedEle.marginLeft?selectedEle.marginLeft:0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (e) {
                var selected = getEXTComponent(selectedElement);
                var tableContainer=document.getElementById("idageingtablecontainer");
                tableContainer.style.marginLeft = e.getValue()+"px";
                selected.marginLeft = e.getValue();
            }
        }
    });
    var rightMargin = new Ext.form.NumberField({
        fieldLabel: 'Right Margin',
        id: 'idrightmargin',
        labelWidth: 100,
        value: selectedEle.marginRight?selectedEle.marginRight:0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (e) {
                var selected = getEXTComponent(selectedElement);
                var tableContainer=document.getElementById("idageingtablecontainer");
                tableContainer.style.marginRight = e.getValue()+"px";
                selected.marginRight = e.getValue();
            }
        }
    });
    
    var marginsettings = {
        xtype: 'fieldset',
        title: 'Margin Settings',
        id: 'idmarginsettings',
        width:260,
        items:[
        topMargin, bottomMargin, leftMargin, rightMargin 
        ]
    };
    
    var width =  new Ext.form.NumberField({
        fieldLabel: 'Width (%)',
        id: 'idWidth',
        width: 210,
        minValue: 1,
        maxValue: 100,
        value:selectedEle.tableWidth?selectedEle.tableWidth:100,
        listeners: {
            change: function (e) {
                if ( e.getValue() > 100 ) {
                    e.setValue(100);
                    WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);
                }
                var selected = getEXTComponent(selectedElement);
                var tableContainer=document.getElementById("idageingtablecontainer");
                tableContainer.style.width = e.getValue()+"%";
                selected.tableWidth = e.getValue();
            }
        }
    });

    var backgroundColorLabel = Ext.create("Ext.form.Label", {
        text: "Background Color:",
        padding: '5 0 5 0',
        width: 105

    });
    /**
     * ERP-29026
     * Provide Rich color Picker for background in Ageing table 
     */
    var backgroundcolorPicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: selectedEle.backgroundColor ? selectedEle.backgroundColor : 'FFFFFF',
        id: 'idbackgroundcolorpicker',
        listeners: {
            change: function (thiz,newVal) {
                Ext.getCmp('idSelectedbackgroundPanel').body.setStyle('background-color', newVal);
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idageingtable");
                var cells=table.rows[0].cells;
                for( var i=0; i< cells.length; i++ ){
                    cells[i].style.backgroundColor=newVal;
                }
                selected.backgroundColor = newVal;
            }

        }
    });    
    var backgroundcolor=selectedEle.backgroundColor ? selectedEle.backgroundColor : 'FFFFFF';
    var selectedbackgroundColorPanel = Ext.create("Ext.Panel", {
        id: 'idSelectedbackgroundPanel',
        height: 20,
        bodyStyle:'background-color:'+backgroundcolor+';',
        width: 30,
        border: false

    });

    var backgroundColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        padding: '5 0 5 0',
        width: 250,
        border: false,
        items: [backgroundColorLabel, selectedbackgroundColorPanel, backgroundcolorPicker]
    });

   var dataBackgroundColorLabel = Ext.create("Ext.form.Label", {
        text: "Background Color:",
        padding: '5 0 5 0',
        width: 105

    });
    /**
     * ERP-29026
     * Provide Rich color Picker for data background of Data in Ageing table.
     */
    var dataBackgroundColorPicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: selectedEle.databackgroundColor ? selectedEle.databackgroundColor : 'FFFFFF',
        id: 'iddatabackgroundcolorpicker',
        listeners: {
            change: function (thiz,newVal) {
                Ext.getCmp('iddataSelectedbackgroundPanel').body.setStyle('background-color', newVal);
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idageingtable");
                var cells=table.rows[1].cells;
                for( var i=0; i< cells.length; i++ ){
                    cells[i].style.backgroundColor=newVal;
                }
                selected.databackgroundColor = newVal;
            }

        }
    });
    var databackgroundcolor=selectedEle.databackgroundColor ? selectedEle.databackgroundColor : 'FFFFFF';
    var dataSelectedBackgroundColorPanel = Ext.create("Ext.Panel", {
        id: 'iddataSelectedbackgroundPanel',
        height: 20,
        bodyStyle:'background-color:'+databackgroundcolor+';',
        width: 30,
        border: false
    });

    var dataBackgroundColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 250,
        border: false,
        items: [dataBackgroundColorLabel, dataSelectedBackgroundColorPanel, dataBackgroundColorPicker]
    });
    
    var textColorLabel = Ext.create("Ext.form.Label", {
        text: "Text Color:",
        padding: '5 0 5 0',
        width: 105

    });
    /**
     * ERP-29026
     * Provide Rich color Picker for Labels in Ageing table
     */
    var textcolorPicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: selectedEle.textColor ? selectedEle.textColor : 'FFFFFF',
        id: 'idtextcolorpicker',
        listeners: {
            change: function (thiz,newVal) {
                Ext.getCmp('idSelectedTextColorPanel').body.setStyle('background-color', newVal);
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idageingtable");
                var cells=table.rows[1].cells;
                for( var i=0; i< cells.length; i++ ){
                    cells[i].style.color=newVal;
                }
                selected.textColor = newVal;
            }

        }
    });
    var textcolor=selectedEle.textColor ? selectedEle.textColor : 'FFFFFF';
    var selectedTextColorPanel = Ext.create("Ext.Panel", {
        id: 'idSelectedTextColorPanel',
        height: 20,
        bodyStyle:'background-color:'+textcolor+';',
        width: 30,
        border: false
    });

    var textColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 250,
        border: false,
        items: [textColorLabel, selectedTextColorPanel, textcolorPicker]
    });
    
    var dataTextColorLabel = Ext.create("Ext.form.Label", {
        text: "Text Color:",
        padding: '5 0 5 0',
        width: 105

    });
    /**
     * ERP-29026
     * Provide Rich color Picker for text Data in Ageing table
     */
    var textDataColorPicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: selectedEle.dataTextColor ? selectedEle.dataTextColor : '000000',
        id: 'iddatatextcolor',
        listeners: {
            change: function (thiz,newVal) {
                Ext.getCmp('iddataSelectedTextColorPanel').body.setStyle('background-color', newVal);
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idageingtable");
                var cells=table.rows[1].cells;
                for( var i=0; i< cells.length; i++ ){
                    cells[i].style.color=newVal;
                }
                selected.dataTextColor = newVal;
            }

        }
    });
    var datatextcolor=selectedEle.dataTextColor ? selectedEle.dataTextColor : 'FFFFFF';
    var dataSelectedTextColorPanel = Ext.create("Ext.Panel", {
        id: 'iddataSelectedTextColorPanel',
        height: 20,
        bodyStyle:'background-color:'+datatextcolor+';',
        width: 30,
        border: false
    });

    var dataTextColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 250,
        padding: '5 0 5 0',
        border: false,
        items: [dataTextColorLabel, dataSelectedTextColorPanel, textDataColorPicker]
    });
    
    var borderColorLabel = Ext.create("Ext.form.Label", {
        text: "Border Color:",
        padding: '5 0 5 0',
        width: 105

    });
    
    /**
     * ERP-29026 
     * Provide Rich color Picker for Border in Ageing table
     */
    var borderColorPicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: selectedEle.dataTextColor ? selectedEle.dataTextColor : '000000',
        id: 'idbordercolorpicker',
        listeners: {
            change: function (thiz,newVal) {
                Ext.getCmp('idSelectedBorderPanel').body.setStyle('background-color', newVal);
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idageingtable");
                var rows=table.rows;
                for(var j=0;j<rows.length;j++){
                    var cells=rows[j].cells;
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.borderColor=newVal;
                    }
                }
                selected.borderColor = newVal;
            }

        }
    });
    var bordercolor=selectedEle.borderColor ? selectedEle.borderColor : 'FFFFFF';
    var selectedborderColorPanel = Ext.create("Ext.Panel", {
        id: 'idSelectedBorderPanel',
        height: 20,
        bodyStyle:'background-color:'+bordercolor+';',
        width: 30,
        border: false

    });

    var borderColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 250,
        border: false,
        items: [borderColorLabel, selectedborderColorPanel, borderColorPicker]
    });

    var updateBtn = Ext.create('Ext.Button', {
        text: 'Edit '+tablename,
        id: 'idUpdateBtn',
        width: 105,
        style: {
            'float': 'right',
            'margin-right': '25px',
            'margin-top': '5px',
            'left':'162px'
        },
        listeners: {
            'click': function ()
            {
                var ageingTableparentpanel = Ext.getCmp(selectedElement);
                
                var Posx=ageingTableparentpanel.x;
                var Posy=ageingTableparentpanel.y;
                openAgeingDetailsSettingWindow(this,ageingTableparentpanel,true);
            }
        }
    });
    
    var setIntervalBtn = Ext.create('Ext.Button', {
        text: 'Set Interval Text',
        id: 'idIntervalTextBtn',
        width: 105,
        style: {
            'float': 'right',
            'margin-bottom': '5px'
        },
        listeners: {
            'click': function (){
                var ageingTableComponent = getEXTComponent(selectedElement);
                ageingTableComponent.noofintervals = getEXTComponent("intervalId").getValue();
                setIntervalTextWindow(ageingTableComponent);
            }
        }
    });
   
    var boldText = new Ext.form.field.Checkbox({
        id: 'boldTextLineItemId',
        fieldLabel: 'Bold',
        padding:'2 2 2 2',
        checked: (selectedEle.bold == 'bold') ? true : false,
        listeners: {
            change: function (e) {
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idageingtable");
                var cells=table.rows[0].cells;
                
                if (e.getValue() == true) {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontWeight="bold";
                    }
                    selected.bold = "bold";
                }
                else {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontWeight="normal";
                    }
                    selected.bold = "normal";
                }
            }
        }
    });
    var italicText = new Ext.form.field.Checkbox({
        id: 'italicTextLineItemId',
        fieldLabel: 'Italic',
        padding:'2 2 2 2',
        checked: (selectedEle.italic == 'italic') ? true : false,
        listeners: {
            change: function (e) {
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idageingtable");
                var cells=table.rows[0].cells;
                
                if (e.getValue() == true) {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontStyle="italic";
                    }
                    selected.italic = "italic";
                }
                else {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontStyle="normal";
                    }
                    selected.italic = "normal";
                }
            }
        }
    });
    
    var underLine = new Ext.form.field.Checkbox({
        id: 'underLineTextLineItemId',
        fieldLabel: 'Underline',
        padding:'2 2 2 2',
        checked: (selectedEle.underline == 'underline') ? true : false,
        listeners: {
            change: function (e) {
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idageingtable");
                var cells=table.rows[0].cells;
                
                if (e.getValue() == true) {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.textDecoration="underline";
                    }
                    selected.underline = "underline";
                }
                else {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.textDecoration="";
                    }
                    selected.underline = "";
                }
            }
        }
    });
    
    
    var pagefontfields = new Ext.form.field.ComboBox({
        xtype: 'combo',
        fieldLabel: 'Font',
        id:'pageFontLineItemId',
        displayField: 'name',
        valueField: 'val',
        store: pagefontstore,
        padding:'2 2 2 2',
        labelWidth:100,
        value:selectedEle.fontFamily ? selectedEle.fontFamily : "",
        width:210,
        listeners: {
            'select': function (e) {
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idageingtable");
                var cells=table.rows[0].cells;
                
                for( var i=0; i< cells.length; i++ ){
                    cells[i].style.fontFamily=e.getValue();
                }
                selected.fontFamily = e.getValue();
            }
        }
    });
    
    var fontSize = new Ext.form.NumberField({
        fieldLabel: 'Font Size',
        id: 'fontSizeLineItemId',
        value: (selectedEle != null || selectedEle != undefined) ? selectedEle.fontSize : 'None', //parseInt to remove the px e.g 250px to 250
        width: 210,
        padding:'2 2 2 2',
        minLength: 1,
        maxLength: 3,
        minValue : 0,
        listeners: {
            'change': function (e) {
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idageingtable");
                var cells=table.rows[0].cells;
                
                if (e.getValue() > 0) {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontSize=e.getValue()+"px";
                    }
                    selected.fontSize = e.getValue();
                }
                else {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontSize="";
                    }
                    selected.fontSize = "";
                }
            }
        }
    });
   
    var alignCombo = new Ext.form.field.ComboBox({
        fieldLabel: 'Text Alignment',
        store: alignStore,
        id: 'allignLineTableCombo',
        displayField: 'align',
        valueField: 'id',
        labelWidth:100,
        queryMode: 'local',
        width: 210,
        emptyText: 'Select alignment',
        forceSelection: false,
        editable: false,
        triggerAction: 'all',
        value :selectedEle.align?selectedEle.align:"left",
        padding:'2 2 2 2',
        listeners: {
            'change': function (e)
            {
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idageingtable");
                var cells=table.rows[0].cells;
                for( var i=0; i< cells.length; i++ ){
                    cells[i].align=e.getValue();
                    cells[i].style.textAlign = e.getValue();
                    cells[i].style.paddingLeft="4px";
                    cells[i].style.paddingRight="4px";
                }
                selected.align = e.getValue();
            }
        }
    });
    
    var dataBoldText = new Ext.form.field.Checkbox({
        id: 'databoldTextId',
        fieldLabel: 'Bold',
        padding:'2 2 2 2',
        checked: (selectedEle.bolddata == 'bold') ? true : false,
        listeners: {
            change: function (e) {
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idageingtable");
                var cells=table.rows[1].cells;
                
                if (e.getValue() == true) {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontWeight="bold";
                        cells[i].children[0].style.fontWeight="bold";
                    }
                    selected.bolddata = "bold";
                }
                else {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontWeight="normal";
                        cells[i].children[0].style.fontWeight="normal";
                    }
                    selected.bolddata = "normal";
                }
            }
        }
    });
    
    var dataItalicText = new Ext.form.field.Checkbox({
        id: 'dataitalicTextId',
        fieldLabel: 'Italic',
        padding:'2 2 2 2',
        checked: (selectedEle.italicdata == 'italic') ? true : false,
        listeners: {
            change: function (e) {
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idageingtable");
                var cells=table.rows[1].cells;
                
                if (e.getValue() == true) {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontStyle="italic";
                        cells[i].children[0].style.fontStyle="italic";
                    }
                    selected.italicdata = "italic";
                }
                else {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontStyle="normal";
                        cells[i].children[0].style.fontStyle="normal";
                    }
                    selected.italicdata = "normal";
                }
            }
        }
    });
    
    var dataUnderLine = new Ext.form.field.Checkbox({
        id: 'dataunderLineTextId',
        fieldLabel: 'Underline',
        padding:'2 2 2 2',
        checked: (selectedEle.underlinedata == 'underline') ? true : false,
        listeners: {
            change: function (e) {
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idageingtable");
                var cells=table.rows[1].cells;
                
                if (e.getValue() == true) {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.textDecoration="underline";
                        cells[i].children[0].style.textDecoration="underline";
                    }
                    selected.underlinedata = "underline";
                }
                else {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.textDecoration="";
                        cells[i].children[0].style.textDecoration="";
                    }
                    selected.underlinedata = "";
                }
            }
        }
    });
    
    
    var datafontfields = new Ext.form.field.ComboBox({
        xtype: 'combo',
        fieldLabel: 'Font',
        id:'dataFontId',
        displayField: 'name',
        valueField: 'val',
        store: pagefontstore,
        padding:'2 2 2 2',
        labelWidth:100,
        value:selectedEle.fontFamilydata ? selectedEle.fontFamilydata : "",
        width:210,
        listeners: {
            'select': function (e) {
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idageingtable");
                var cells=table.rows[1].cells;
                
                for( var i=0; i< cells.length; i++ ){
                    cells[i].style.fontFamily=e.getValue();
                    cells[i].children[0].style.fontFamily=e.getValue();
                }
                selected.fontFamilydata = e.getValue();
            }
        }
    });
    
    var dataFontSize = new Ext.form.NumberField({
        fieldLabel: 'Font Size',
        id: 'datafontSizeId',
        value: (selectedEle != null || selectedEle != undefined) ? selectedEle.fontSizedata : 'None', 
        width: 210,
        padding:'2 2 2 2',
        minLength: 1,
        maxLength: 3,
        minValue : 0,
        listeners: {
            'change': function (e) {
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idageingtable");
                var cells=table.rows[1].cells;
                
                if (e.getValue() > 0) {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontSize=e.getValue()+"px";
                        cells[i].children[0].style.fontSize=e.getValue()+"px";
                    }
                    selected.fontSizedata = e.getValue();
                }
                else {
                    for( var i=0; i< cells.length; i++ ){
                        cells[i].style.fontSize="";
                        cells[i].children[0].style.fontSize="";
                    }
                    selected.fontSizedata = "";
                }
            }
        }
    });
   
    var dataAlignCombo = new Ext.form.field.ComboBox({
        fieldLabel: 'Text Alignment',
        store: alignStore,
        id: 'dataallignComboId',
        displayField: 'align',
        valueField: 'id',
        labelWidth:100,
        queryMode: 'local',
        width: 210,
        emptyText: 'Select alignment',
        forceSelection: false,
        editable: false,
        triggerAction: 'all',
        value :selectedEle.aligndata?selectedEle.aligndata:"left",
        padding:'2 2 2 2',
        listeners: {
            'change': function (e)
            {
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idageingtable");
                var cells=table.rows[1].cells;
                for( var i=0; i< cells.length; i++ ){
                    cells[i].align=e.getValue();
                    cells[i].style.textAlign = e.getValue();
                    cells[i].style.paddingLeft="4px";
                    cells[i].style.paddingRight="4px";
                    cells[i].children[0].align=e.getValue();
                    cells[i].children[0].style.textAlign = e.getValue();
                    cells[i].children[0].style.paddingLeft="4px";
                    cells[i].children[0].style.paddingRight="4px";
                }
                selected.aligndata = e.getValue();
            }
        }
    });
    
    var interval = new Ext.form.NumberField({
        fieldLabel: 'No. of Intervals',
        id: 'intervalId',
        value: (selectedEle != null || selectedEle != undefined) ? selectedEle.noofintervals : 3, //parseInt to remove the px e.g 250px to 250
        width: 210,
        padding:'2 2 2 2',
        maxLength: 2, //ERP-28745
        minValue : 1,
        maxValue : 11, //ERP-28745
        listeners: {
            'change': function (e, newVal, oldVal) {
                var selected = getEXTComponent(selectedElement);
                if(selected.intervalType === "Days" && newVal <= 7){
                    selected.noofintervals = newVal;
                } else if(selected.intervalType === "Days" && newVal > 7){
                    Ext.MessageBox.show({
                        title: 'Alert',
                        msg: 'Selected Interval Type is of "Days", So you can set maximum 7 No. of Intervals.',
                        icon: Ext.MessageBox.QUESTION,
                        buttons: Ext.MessageBox.OK
                    });
                    e.setValue(oldVal);
                } else if(selected.intervalType === "Months" && newVal <= 11){
                    selected.noofintervals = newVal;
                } else if(selected.intervalType === "Months" && newVal > 11){
                    Ext.MessageBox.show({
                        title: 'Alert',
                        msg: 'Selected Interval Type is of "Months", So you can set maximum 11 No. of Intervals.',
                        icon: Ext.MessageBox.QUESTION,
                        buttons: Ext.MessageBox.OK
                    });
                    e.setValue(oldVal);
                }
            }
        }
    });
    
    var multiCurrency =new Ext.form.field.Checkbox({
        fieldLabel: 'Multiple Currency',
        id: 'multiCurrencyId',
        checked: selectedEle.ismulticurrency ? selectedEle.ismulticurrency : false, 
        width: 210,
        padding:'2 2 2 2',
        listeners: {
            'change': function (e) {
                var custCurrEle = getEXTComponent("customerVendorCurrencyId");
                if(custCurrEle.getValue() && e.getValue()){
                    Ext.MessageBox.show({
                        title: 'Alert',
                        msg: 'You can not activate <b>Multiple Currency</b> and <b>Customer Currency</b> checks in one template.',
                        icon: Ext.MessageBox.QUESTION,
                        buttons: Ext.MessageBox.OK
                    });
                    e.setValue(false);
                } else{
                    var selected = getEXTComponent(selectedElement);
                    selected.ismulticurrency = e.getValue();
                }
            }
        }
    });
    
    var customerCurrency =new Ext.form.field.Checkbox({
        fieldLabel: 'Customer/Vendor Currency',
        id: 'customerVendorCurrencyId',
        checked: selectedEle.isCustomerVendorCurrency ? selectedEle.isCustomerVendorCurrency : false, 
        width: 210,
        padding:'2 2 2 2',
        listeners: {
            'change': function (e) {
                var multiCurrEle = getEXTComponent("multiCurrencyId");
                if(multiCurrEle.getValue() && e.getValue()){
                    Ext.MessageBox.show({
                        title: 'Alert',
                        msg: 'You can not activate <b>Multiple Currency</b> and <b>Customer Currency</b> checks in one template.',
                        icon: Ext.MessageBox.QUESTION,
                        buttons: Ext.MessageBox.OK
                    });
                    e.setValue(false);
                } else{
                    var selected = getEXTComponent(selectedElement);
                    selected.isCustomerVendorCurrency = e.getValue();
                }
            }
        }
    });
    
    var current =new Ext.form.field.Checkbox({
        fieldLabel: 'Include Current',
        id: 'currentId',
        checked: selectedEle.isincludecurrent ? selectedEle.isincludecurrent : false, 
        width: 210,
        padding:'2 2 2 2',
        listeners: {
            'change': function (e) {
                var selected = getEXTComponent(selectedElement);
                selected.isincludecurrent = e.getValue();
            }
        }
    });
    
    var spacesettings = {
        xtype: 'fieldset',
        width:260,
        title:'Space Settings',
        id:'idspacesettingsfieldset',
        items: [
        width
        ]
    };
    
    var ageingTableSettings = {
        xtype: 'fieldset',
        width:260,
        hidden:_CustomDesign_moduleId == Ext.moduleID.MRP_WORK_ORDER_MODULEID,
        title:'Ageing Table Settings',
        id:'idageingtablesettingsfieldset',
        items: [
        interval,customerCurrency,multiCurrency,current,setIntervalBtn
        ]
    };
    
    var checklistValueStore = new Ext.data.Store({
        fields : ['id','value'],
        data : [{
            id: 'rating',
            value: 'Rating'
        }, {
            id: 'status',
            value: 'Status'
        }, {
            id: 'description',
            value: 'Description'
        }]
    });
    
    var checklistValueCombo = new Ext.form.field.ComboBox({
        fieldLabel: 'Checklist Value',
        store: checklistValueStore,
        id: 'checklisttValueComboId',
        displayField: 'value',
        valueField: 'id',
        labelWidth:100,
        queryMode: 'local',
        width: 210,
        emptyText: 'Select Value',
        forceSelection: false,
        editable: false,
        triggerAction: 'all',
        value:selectedEle.checklistValue?selectedEle.checklistValue:"rating",
        multiSelect: true,
        padding:'2 2 2 2',
        listeners: {
            'change': function (e)
            {
                var selected = getEXTComponent(selectedElement);
                selected.checklistValue = e.getValue();
            }
        }
    });
    
    var checklistTableSettings = {
        xtype: 'fieldset',
        width:260,
        hidden:_CustomDesign_moduleId != Ext.moduleID.MRP_WORK_ORDER_MODULEID,
        title:'Checklist Table Settings',
        id:'idchecklisttablesettingsfieldset',
        items: [
            checklistValueCombo
        ]
    };
    
    var fontsettings = {
        xtype: 'fieldset',
        width:260,
        title:'Header Settings',
        id:'idheadersettingsfieldset',
        items: [
        boldText,italicText, underLine, fontSize, pagefontfields,alignCombo,textColorPanel,backgroundColorPanel
        ]
    };
    
    var datasettings = {
        xtype: 'fieldset',
        width:260,
        title:'Data Settings',
        id:'iddatasettingsfieldset',
        items: [
        dataBoldText,dataItalicText, dataUnderLine, dataFontSize, datafontfields,dataAlignCombo,dataTextColorPanel,dataBackgroundColorPanel
        ]
    };
    
    var colorSettings = {
        xtype: 'fieldset',
        width:260,
        title:'Color Settings',
        id:'idcolorsettingsfieldset',
        items: [
        borderColorPanel
        ]
    };
       
    var fieldPropertyPanel = Ext.create("Ext.Panel", {
        autoHeight: true,
        width: 295,
        border: false,
        layout:'column',
        id: 'idPropertyPanel',
        defaults: {
            margin: '7 0 0 10'
        },
        items: [
        spacesettings,ageingTableSettings,checklistTableSettings,fontsettings,datasettings,colorSettings,marginsettings
        ],          
        buttons: [updateBtn]
    });
    var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);
    var propertyPanel = Ext.getCmp("idPropertyPanel");
    if (propertyPanel){
        propertyPanelRoot.remove(propertyPanel.id);
    }
    propertyPanel = fieldPropertyPanel; 
    propertyPanelRoot.add(propertyPanel);
    Ext.getCmp("idEastPanel").setTitle(tablename + " Property Panel");
    Ext.getCmp("idEastPanel").add(propertyPanelRoot);
    Ext.getCmp("idEastPanel").doLayout(); 
}

function setIntervalTextWindow(parentComponent) {
    var noOfIntervals = parentComponent.noofintervals;
    
    var intervalTypeStore = Ext.create("Ext.data.Store", {
       fields:[
           'id','value'
       ],
       data:[
           {
               id:'Days',
               value:'Days'
           }
           ,
           {
               id:'Months',
               value:'Months'
           }
       ]
       
    });
    var intervalPlaceHolderStore = Ext.create("Ext.data.Store",{
       fields:[
           'id','value','filter'
       ],
       data:[
           {
               id:'Month',
               value:'Month',
               filter:'Months'
           },
           { //ERP-28745
               id:'MonthNumber',
               value:'Month Number',
               filter:'Months'
           },
           {
               id:'MonthFrom',
               value:'Month From',
               filter:'Months'
           },
           {
               id:'MonthTo',
               value:'Month To',
               filter:'Months'
           },
           {
               id:'From',
               value:'From',
               filter:'Days'
           },
           {
               id:'To',
               value:'To',
               filter:'Days'
           }
       ] 
    });
    var infoPanel = new Ext.Panel({
        width: 500,
        border:false,
        height:60,
        html:"<div style='font-size:11px; margin-top:5px; margin-left:10px;'><b>Set Interval Text<b></div><font size='1'><ul><li>Set Interval text, by selecting interval type, and place holders.</li><li>You can also add your custom text.</li></ul></font>"
    });
    var intervalTypeCombo = new Ext.form.ComboBox({
       fieldLabel: "Interval Type",
       width:300,
       labelWidth:150,
       store:intervalTypeStore,
       displayField:'value',
       valueField:'id',
       value:parentComponent.intervalType,
       margin:'12 2 12 2',
       id:"idIntervalTypeCombo",
       listeners:{
            select: function (combo,rec) {
                var type = rec[0].data.id;
                var phCombo = getEXTComponent("idIntervalPlaceHolders");
                var intervalTextArea = getEXTComponent("idIntervalTextArea");
                if ( phCombo ) {
                    phCombo.getStore().clearFilter(true);
                    phCombo.getStore().load();
                    phCombo.getStore().filter("filter",type);
                }
                
                var textValue; 
                if ( intervalTextArea ) {
                    if ( type == "Days" ) {
                        textValue = intervalTextArea.getValue();
                        textValue = textValue.replace(/#Month#/g,"");
                        textValue = textValue.replace(/#MonthNumber#/g,"");
                        intervalTextArea.setValue(textValue);
                    } else {
                        textValue = intervalTextArea.getValue();
                        textValue = textValue.replace(/#From#/g,"");
                        textValue = textValue.replace(/#To#/g,"");
                        intervalTextArea.setValue(textValue);
                    }
                }
                
                parentComponent.intervalType = type;

           }
       }
    });
    var intervalPlaceHolders = new Ext.form.ComboBox({
       fieldLabel: "Interval Place Holders",
       width:300,
       store:intervalPlaceHolderStore,
       displayField:'value',
       valueField:'id',
       labelWidth:150,
       margin:'12 2 12 2',
       id:"idIntervalPlaceHolders",
       listeners:{
            render: function (combo) {
                var typeCombo = getEXTComponent("idIntervalTypeCombo");
                if (typeCombo) {
                    combo.getStore().clearFilter(true);
                    combo.getStore().load();
                    combo.getStore().filter("filter", typeCombo.getValue());
                }
            },
           select: function(combo,rec) {
               var textField = getEXTComponent("idIntervalTextArea");
               var value = rec[0].data.id;
               if ( textField ) {
                   value = "#"+value+"#";
                   insertAtCursorForTextArea(textField, value);
               }
               var placeHolder = rec[0].data.id; //ERP-28745
               parentComponent.intervalPlaceHolder = placeHolder; //ERP-28745
           }
       }
    });
    var intervaltextArea = new Ext.form.TextArea({
        fieldLabel: "Interval Text",
        width:400,
        labelWidth:150,
        margin:'12 2 12 2',
        value:parentComponent.intervalText,
        id:"idIntervalTextArea"
    });
    var backgroundPanel = new Ext.Panel({
       width:500,
       border:false,
       height:250,
       bodyStyle:"background-color:#f5f5f5;",
       items:[
           intervaltextArea,
           intervalTypeCombo,
           intervalPlaceHolders
       ]
    });
    var intervalTextWindow = new Ext.Window({
        height: 300,
        width:500,
        modal:true,
        resizable:false,
        title:"Interval Text",
        id:"idintervalTextWindow",
        items:[
                infoPanel,
                backgroundPanel
        ],
        buttons:[
            {
                text:"Save",
                handler: function () {
                    var intervalTextArea = getEXTComponent("idIntervalTextArea");
                    parentComponent.intervalText = intervalTextArea.getValue();
                    if(noOfIntervals > 7 && getEXTComponent("idIntervalTypeCombo").getValue() === "Days"){
                        Ext.MessageBox.show({
                            title: 'Alert',
                            msg: 'Selected Interval Type is of "Days", So you can set maximum 7 No. of Intervals.',
                            icon: Ext.MessageBox.QUESTION,
                            buttons: Ext.MessageBox.OK
                        });
                        getEXTComponent("intervalId").setValue(7);
                    }
                    var win = getEXTComponent("idintervalTextWindow");
                    if ( win ) {
                        win.close();
                    }
                }
            },{
                text:"Close",
                handler: function() {
                    var win = getEXTComponent("idintervalTextWindow");
                    if ( win ) {
                        win.close();
                    }
                }
            }
        ]
    });
    intervalTextWindow.show();
}

