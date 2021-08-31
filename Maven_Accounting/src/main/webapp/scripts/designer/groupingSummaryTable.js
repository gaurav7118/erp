function openGroupingSummaryWindow(parentPanel,obj,isEdit){
    var ele = Ext.getCmp('itemlistcontainer');
    if (ele !== undefined && ele !== null) {
        /*
        * Array for line item number fields combo
        */
        var records = [];
        var columnslength = ele.initialConfig.columns.length;
        // Get line item table selected fields for sorting combo
        for(var cnt = 0; cnt < columnslength; cnt++){
            if(ele.initialConfig.columns[cnt].xtype == "2" && ele.initialConfig.columns[cnt].isformula != true){
                records.push({
                    xtype: ele.initialConfig.columns[cnt].xtype,
                    fieldid: ele.initialConfig.columns[cnt].fieldid,
                    displayfield: ele.initialConfig.columns[cnt].columnname,
                    columnname: ele.initialConfig.columns[cnt].columnname,
                    colwidth: 20,
                    seq: ele.initialConfig.columns[cnt].seq,
                    label: ele.initialConfig.columns[cnt].columnname
                });
            }
        }
        groupingSummaryColumnsArray = records;
        
        var _tw = new Ext.GroupingSummaryTableGridWindow({
            tableID: 'groupingsummarytableconfig',
            groupingSummaryColumns: groupingSummaryColumns,
            obj:obj,
            isEdit:isEdit
        });

        _tw.on("okClicked", function (config) {
            var header, row;
            var cellCounter;
            var valObj = config.getGridConfigSetting();
            var jArr = eval(valObj);
            if(isEdit==true){
                if (document.getElementById("groupingsummarytableconfig"))
                    remove(document.getElementById("groupingsummarytableconfig"));
                if (document.getElementById("idgroupingsummarytablecontainer"))
                    remove(document.getElementById("idgroupingsummarytablecontainer"));
                parentPanel=obj.ownerCt;
            }

            var groupingOnDisplayValue = config.groupingOnDisplayValue;
            var groupingOnValue = config.groupingOnValue;
            obj.groupingOnDisplayValue = groupingOnDisplayValue;
            obj.groupingOnValue = groupingOnValue;

            var groupingSummaryTable = document.createElement('table');
            groupingSummaryTable.setAttribute("class", "groupingsummarytable");
            groupingSummaryTable.setAttribute("id", "idgroupingsummarytable");
            groupingSummaryTable.setAttribute("cellSpacing", "0");
            groupingSummaryTable.setAttribute("onclick", "getGroupingSummaryTablePropertyPanel(event,this)");
            header = groupingSummaryTable.insertRow(0);

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
            row = groupingSummaryTable.insertRow(1);
            for ( cellCounter = 0; cellCounter < jArr.length; cellCounter++ ) {
                var rowCell = document.createElement("td");
                var div = document.createElement('div');
                var dataCellConfig = jArr[cellCounter];
                div.innerHTML = dataCellConfig.columnname;
                if(dataCellConfig.seq == -1){
                    div.setAttribute("onclick", "getPropertyPanelForGroupingSummaryFields(event,this,true)");
                    div.setAttribute("id", dataCellConfig.fieldid);
                }
                var isFormula = dataCellConfig.isformula ? (dataCellConfig.isformula == "true" ? true : false) : false;
                var isSummaryFormula = dataCellConfig.isSummaryFormula ? (dataCellConfig.isSummaryFormula == "true" ? true : false) : false;
                if(isFormula){
                    div.setAttribute("isformula", isFormula);
                    div.setAttribute("issummaryformula", isSummaryFormula);
                    div.setAttribute("formula", dataCellConfig.formula ? dataCellConfig.formula : "");
                    div.setAttribute("formulavalue", dataCellConfig.formulavalue ? dataCellConfig.formulavalue : "");
                    div.setAttribute("type", "4");
                    div.setAttribute("label", dataCellConfig.columnname);
                    div.innerHTML = dataCellConfig.formula;
                }
                div.setAttribute("fieldid", dataCellConfig.fieldid);
                div.setAttribute("functiontype", dataCellConfig.functiontype);
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
            var field = createGroupingSummaryTable(obj,groupingSummaryTable.outerHTML,parentPanel,Ext.fieldID.insertGroupingSummaryTable,isEdit);
            parentPanel.add(field);
            initTreePanel();
        }, obj);
        _tw.show();
    } else{
        Ext.Msg.show({
            title      : 'Grouping Summary Table',
            msg        : 'Grouping Summary Table is applicable if Line Item Table is used in template.',
            width      : 370,
            buttons    : Ext.MessageBox.OK,
            icon       : Ext.Msg.WARNING
        });
    }
}


function createGroupingSummaryTable(obj,tablehtml,designerPanel,fieldTypeId,isEdit) {
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
    var groupingOnDisplayValue = (obj && obj.groupingOnDisplayValue) ? obj.groupingOnDisplayValue : "";
    var groupingOnValue = (obj && obj.groupingOnValue) ? obj.groupingOnValue : "";
    var groupingColPreText = (obj && obj.groupingColPreText) ? obj.groupingColPreText : "";
    var groupingColPostText = (obj && obj.groupingColPostText) ? obj.groupingColPostText : "";

    var columns = groupingSummaryColumns.length?groupingSummaryColumns:(obj && obj.columns)?obj.columns:[]
    if(isEdit!=true && obj.columns!=undefined){
        columns = obj.columns;
    }
   
    groupingSummaryColumns = columns;
    var field = Ext.create('Ext.Component', {
        
        labelhtml:tablehtml,
        width: tableWidth+"%",
        unit:"",
        id:"idgroupingsummarytablecontainer",
        backgroundColor:backgroundColor,
        borderColor:borderColor,
        headerColor:headerColor,
        tableAlign:tableAlign,
        groupingOnDisplayValue:groupingOnDisplayValue,
        groupingOnValue:groupingOnValue,
        groupingColPreText:groupingColPreText,
        groupingColPostText:groupingColPostText,
        marginTop : marginTop,
        marginBottom:marginBottom,
        marginLeft:marginLeft,
        marginRight:marginRight,
        tableWidth:tableWidth,
        bold:bold,
        fontSize:fontSize,
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
           
           this.el.dom.style.marginTop = marginTop;        
           this.el.dom.style.marginBottom = marginBottom;  
           this.el.dom.style.marginLeft = marginLeft;      
           this.el.dom.style.marginRight = marginRight; 
           var table = document.getElementById('idgroupingsummarytable');
            if(table){
                table.setAttribute("panelid", designerPanel.id);
            }
            var rows=table.rows;
            var cells=table.rows[0].cells;
            for( var i = 0; i< cells.length; i++ ){
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
                    } else {
                        cells[i].style.fontSize="";
                    }
                }
            var datacells=table.rows[1].cells;
            for( var i = 0; i< cells.length; i++ ){
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
                } else {
                    datacells[i].style.fontSize="";
                }
                
                var childDiv = datacells[i].children[0];
                var seq = childDiv.getAttribute("seq");
                if(seq != null && seq === "-1"){
                    if(groupingColPreText !== ""){
                        var preTextSpan = document.createElement('span');
                        preTextSpan.innerHTML = groupingColPreText;
                        preTextSpan.setAttribute("id", childDiv.id + "1");
                        preTextSpan.setAttribute("label", "PreText");
                        preTextSpan.setAttribute("type", "1");
                        childDiv.innerHTML = preTextSpan.outerHTML + childDiv.innerHTML;
                        childDiv.isPreText = true;
                    }
                    if(groupingColPostText !== ""){
                        var postTextSpan = document.createElement('span');
                        postTextSpan.innerHTML = groupingColPostText;
                        postTextSpan.setAttribute("id", childDiv.id + "2");
                        postTextSpan.setAttribute("label", "PostText");
                        postTextSpan.setAttribute("type", "2");
                        childDiv.innerHTML = childDiv.innerHTML + postTextSpan.outerHTML;
                        childDiv.isPostText = true;
                    }
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
                createContextMenu(Ext.fieldID.insertGroupingSummaryTable,id);
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


Ext.GroupingSummaryTableGridWindow = function (conf) {
    Ext.apply(this, conf);
    this.addEvents({
        "okClicked": true
    });
    this.createEditor();
    Ext.define('GroupingOnField', {
        extend: 'Ext.data.Model',
        fields: [
            {name: 'key', type: 'string'},
            {name: 'value',  type: 'string'},
        ]
    });
    this.groupingOnStore = Ext.create('Ext.data.Store', {
        model: 'GroupingOnField',
        proxy: {
            type: 'ajax',
            url : 'CustomDesign/getGroupingFields.do',
            extraParams : {
              moduleid : _CustomDesign_moduleId,
              companyid : companyid
            },
            reader: {
                type: 'json',
                root: 'data'
            }
        },
        listeners: {
            load: function(store, operation, options){
                this.insert(0, {key:"none", value:"None"});
                this.insert(1, {key:"productType", value:"Product Type"});
                this.insert(2, {key:"productCategory", value:"Product Category"});
            }
        }
    });
    this.groupingOnStore.load();
    
    //Group On field combo
    this.groupOnField = new Ext.form.field.ComboBox({
        fieldLabel:"Group On",
        labelWidth:70,
        padding:'10 0 0 0',
        displayField:'value',
        queryMode: 'local',
        width:180,
        valueField:'key',
        store:this.groupingOnStore,
        value:conf.obj.groupingOnValue != null ? conf.obj.groupingOnValue : "productType",
        scope:this,
        listeners: {
            "change" : function(ele){
                var gridEle = getEXTComponent("groupingSummaryGrid");
                var groupingCol = gridEle.getStore().find("seq", -1);
                if(groupingCol == -1){
                    var groupEle = this;
                    var rec = new commonModel({
                        colno:gridEle.getStore().data.length + 1,
                        columnname:groupEle.getValue(),
                        colwidth:20,
                        decimalpoint:"",
                        displayfield:groupEle.getDisplayValue(),
                        fieldid:groupEle.getValue(),
                        headercurrency:"",
                        headerproperty:"",
                        hidecol:false,
                        recordcurrency:"",
                        seq:-1,
                        showtotal:false,
                        xtype:"1"
//                        isformula:false,
//                        formula:"",
//                        formulavalue:""
                    });
                    gridEle.getStore().insert(0, rec);
                } else{
                    var groupingColData = gridEle.getStore().getAt(groupingCol);
                    groupingColData.data.columnname = this.getDisplayValue();
                    groupingColData.data.displayfield = this.getDisplayValue();
                    groupingColData.data.fieldid = this.getDisplayValue();
                }
                gridEle.getView().refresh();
                gridEle.doLayout();
            }
        }
    });
    
    if(!this.isEdit){
        var gridEle = getEXTComponent("groupingSummaryGrid");
        var groupEle = this.groupOnField;
        var rec = new commonModel({
            colno:gridEle.getStore().data.length + 1,
            columnname:groupEle.getValue(),
            colwidth:20,
            decimalpoint:"",
            displayfield:groupEle.getDisplayValue(),
            fieldid:groupEle.getValue(),
            headercurrency:"",
            headerproperty:"",
            hidecol:false,
            recordcurrency:"",
            seq:-1,
            showtotal:false,
            xtype:"1"
        });
        gridEle.getStore().insert(0, rec);
        gridEle.getView().refresh();
    }
    //Function field combo
    var functionstore = Ext.create('Ext.data.Store', {
        fields: ['id', 'name'],
        data : [
        {
            "id":"sum", 
            "name":"SUM"
        },
        {
            "id":"average", 
            "name":"AVERAGE"
        },
        {
            "id":"min", 
            "name":"MIN"
        },
        {
            "id":"max", 
            "name":"MAX"
        },
        {
            "id":"count", 
            "name":"COUNT"
        }]
    });
    this.functionField = new Ext.form.field.ComboBox({
        fieldLabel:"Function",
        labelWidth:50,
        padding:'10 0 0 5',
        displayField:'name',
        queryMode: 'local',
        width:140,
        valueField:'id',
        store:functionstore,
        value:conf.obj.functionValue != null ? conf.obj.functionValue : "",
        scope:this
    });
    //Line Item field combo
    this.lineFieldsStore = new Ext.create('Ext.data.Store', {
        fields: ['columnname', 'displayfield', 'hidecol', 'seq', 'fieldid', 'coltotal', 'colwidth', 'xtype', 'headerproperty', 'showtotal', 'recordcurrency', 'headercurrency', 'decimalpoint', 'colno'],
        data: groupingSummaryColumnsArray,
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
        labelWidth:70,
        padding:'10 0 0 0',
        displayField:'columnname',
        queryMode: 'local',
        width:180,
        valueField:'fieldid',
        store:this.lineFieldsStore,
        scope:this
    });
    this.addBtn = Ext.create('Ext.Button', {
        text: 'Add',
        margin:'10 0 0 0',
        id: 'idAddColumnToGroupingSummaryTable',
        scope:this,
        handler:function(){
            if(this.selectField && this.selectField.getValue() != undefined && this.selectField.getValue != ""){
                var rec = searchRecord(this.lineFieldsStore, this.selectField.getValue(), 'fieldid');
                if(rec){
                    if(this.groupingSummaryStore){
                        var recordPresent = false;
                        this.groupingSummaryStore.each(function(record){
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
                        } else if(this.functionField.getValue() == ""){
                           Ext.MessageBox.show({
                                title: 'Info',
                                msg: 'Please select Function field.',
                                icon: Ext.MessageBox.INFO,
                                buttons: Ext.MessageBox.OK
                            }); 
                        } else{
                            rec.data.seq = this.groupingSummaryStore.data.items.length + 1;
                            rec.data.hidecol = false;
                            rec.data.functiontype = this.functionField.getValue();
                            var max = 0;
                            this.groupingSummaryStore.each(function(record){
                                max=Math.max(record.data.colno, max);;
                            });
                            rec.data.colno = ++max;
                           if(rec.data.xtype=="2"){
                                rec.data.decimalpoint = _amountDecimalPrecision;
                            }
                            this.groupingSummaryStore.add(rec);
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
            this.createFormulaWindow = openFormulaBuilderWindowForGroupingSummary(Ext.fieldID.insertField);
            this.createFormulaWindow.updateFormulaFieldTextValue = function(){
                var expression=""; 
                var expressionVal="";
                var isSummaryFormula = false;
                var isLineFormula = false;
                var validFieldFlag = true;
                for(var i=0;i<this.operatormeasurefieldsDrop.length;i++){
                    if(this.operatormeasurefieldsDrop[i].isnumber!=undefined && this.operatormeasurefieldsDrop[i].isnumber){
                            expression +=this.operatormeasurefieldsDrop[i].defaultHeader;
                            expressionVal +=this.operatormeasurefieldsDrop[i].defaultHeader;
                        }else if(this.operatormeasurefieldsDrop[i].isnumber!=undefined && !this.operatormeasurefieldsDrop[i].isnumber){
                            expression +=this.operatormeasurefieldsDrop[i].defaultHeader+" ";
                            expressionVal +=this.operatormeasurefieldsDrop[i].defaultHeader+" ";
                        }else{
                            if(isSummaryFormula && !this.operatormeasurefieldsDrop[i].isSummaryFormula){
                                WtfComMsgBox(["Alert", "You can't select Line Level and Sumary Level fields in one formula."], 0);
                                validFieldFlag = false;
                            } else if(isLineFormula && this.operatormeasurefieldsDrop[i].isSummaryFormula){
                                WtfComMsgBox(["Alert", "You can't select Line Level and Sumary Level fields in one formula."], 0)
                                validFieldFlag = false;
                            } else if(this.operatormeasurefieldsDrop[i].isSummaryFormula){
                                isSummaryFormula = true;
                            } else{
                                isLineFormula = true;
                            }
                            if(validFieldFlag){
                                expression +="#"+this.operatormeasurefieldsDrop[i].defaultHeader+"# ";
                                if(this.operatormeasurefieldsDrop[i].customfield){
                                    expressionVal +="#"+this.operatormeasurefieldsDrop[i].defaultHeader.replace(/-/g, "!##").replace(/\//g, "$##")+"# ";// replace all '-' and '/' characters from field name with unique identifier
                                } else{
                                    expressionVal +="#"+this.operatormeasurefieldsDrop[i].id+"# ";
                                }
                                this.isSummaryFormula = isSummaryFormula;
                            } else{
                                this.operatormeasurefieldsDrop.pop(i);
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
    
    Ext.GroupingSummaryTableGridWindow.superclass.constructor.call(this, {
        width: 800,
        height: 500,
        resizable: false,
        bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF !important;background: #FFFFFF !important',
        title: 'Configure Product Items ', //"Edit Your Content",
        modal: true,
        items: [
        {
            border:false,
            height:100,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            items:[{
                    xtype:"panel",
                    border:false,
                    height:90,
                    html:"<div style = 'width:100%;height:100%;position:relative;float:left;'><div style='float:left;height:100%;width:auto;position:relative;margin:5px 0px 0px 0px;'>"
                         +"<img src = ../../images/import.png style='height:52px;margin:5px;width:40px;'></img></div><div style='float:left;height:100%;width:90%;position:relative;'>"
                         +"<div style='font-size:12px;font-style:bold;float:left;margin:7px 0px 0px 10px;width:100%;position:relative;'><b>"+((this.groupingSummaryStore.data.items.length > 0)?"Edit Grouping Summary Table":"Create Grouping Summary Table")+"</b></div>"
                         +"<div style='font-size:10px;float:left;margin:5px 0px 10px 10px;width:100%;position:relative;'><ul style='list-style-type:disc;padding-left:15px;'>"
                         +"<li>Select a field from <b>Group On</b> to specify product grouping for Grouping Summary Table</li>"
                         +"<li>Select a function type from <b>Function</b> then Select a column from <b>Select Field</b> and click on <b>Add</b> to add specific column to Grouping Summary Table</li>"
                         +"<li>Click on <b>Add Formula</b> button for creating Formula for Grouping Summary Table"
                         +"<li><b>Note :</b> Function field is applicable only for Line Level fields and Formula created using Line Level fields"
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
                columnWidth : .25,
                border:false,
                items:this.groupOnField
            },{
                xtype: 'box',
                autoEl : {
                    tag : 'hr'
                },
                height:'70%'
            },{
                columnWidth : .20,
                border:false,
                items:this.functionField
            },{
                columnWidth : .25,
                border:false,
                items:this.selectField
            },{
                columnWidth : .10,
                border:false,
                items:this.addBtn
            },{
                xtype: 'box',
                autoEl : {
                    tag : 'hr'
                },
                height:'70%'
            },{
                columnWidth : .20,
                border:false,
                items:this.addFormulaBtn
            }
            ]
        },
        this.groupingSummaryGrid
        ],
        buttons: [
        {
            text: "OK",
            scope: this,
            handler:function(){
                var totalWidth=0;
                var store = this.groupingSummaryGrid.getStore();
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
                            msg: 'Sum of widths of Grouping Summary Table columns should be equal to 100. ',
                            icon: Ext.MessageBox.INFO,
                            buttons: Ext.MessageBox.OK
                        });
                    }else{
                        if(Ext.getCmp(selectedElement).isPanel){//To remove space(&nbsp;) from section if empty(No any element inside). //Check for insert Line Item table in empty section
                            Ext.getCmp(selectedElement).update("");
                        }
                        if(this.groupOnField.getValue() == null){
                            Ext.MessageBox.show({
                                title: 'Info',
                                msg: 'Please select Grouping On field.',
                                icon: Ext.MessageBox.INFO,
                                buttons: Ext.MessageBox.OK
                            });
                        } else if(this.functionField.getValue() == null){
                           Ext.MessageBox.show({
                                title: 'Info',
                                msg: 'Please select Function field.',
                                icon: Ext.MessageBox.INFO,
                                buttons: Ext.MessageBox.OK
                            }); 
                        } else{
                            this.groupingOnDisplayValue = this.groupOnField.getDisplayValue();
                            this.groupingOnValue = this.groupOnField.getValue();
                            this.okClicked();
                        }
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


Ext.extend(Ext.GroupingSummaryTableGridWindow, Ext.Window, {
    onRender: function (conf) {
        Ext.GroupingSummaryTableGridWindow.superclass.onRender.call(this, conf);
    },
    saveRecord : function(){
        var saveflag = this.createFormulaWindow.validatebeforesave();
        if(saveflag){
            var formula = this.createFormulaWindow.formulaText.value;
            var formulaValue = this.createFormulaWindow.formulaTextValue;
            var isSummaryFormula = this.createFormulaWindow.isSummaryFormula;
            var colno = this.groupingSummaryStore.data.items.length + 1;
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
            var seq = this.groupingSummaryStore.data.items.length + 1;
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
                isSummaryFormula:isSummaryFormula,
                formula:formula,
                formulavalue:formulaValue
            });
            rec.data.functiontype = "sum";
            this.groupingSummaryStore.add(rec);
            this.groupingSummaryGrid.doLayout();
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
            minValue: 1
        });
        this.columnWidth = new Ext.form.NumberField({
            validateOnBlur: true,
            allowBlank: false,
            maxValue: 100,
            minValue: 1
        });
        
        var currencyStore = new Ext.data.SimpleStore({      //ERP-24827 : Provide option to set currency symbol or code for line item details
            fields: [{name:'typeid',type:'int'}, 'currency'],
            data :[[0,'Currency Symbol'],[1,'Currency Code']]
        });
        
        var headerCurrencyCombo = new Ext.form.ComboBox({ //Combo for header currency
            store: currencyStore,
            name:'headercurrencytypeid',
            displayField:'currency',
            valueField:'typeid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });
        
        var recordCurrencyCombo = new Ext.form.ComboBox({ //Combo for record currency
            store: currencyStore,
            name:'recordcurrencytypeid',
            displayField:'currency',
            valueField:'typeid',
            mode: 'local',
            triggerAction: 'all',
            selectOnFocus:true
        });
        
        //Function field combo
        var functionLineStore = Ext.create('Ext.data.Store', {
            fields: ['id', 'name'],
            data : [
            {
                "id":"sum", 
                "name":"SUM"
            },
            {
                "id":"average", 
                "name":"AVERAGE"
            },
            {
                "id":"min", 
                "name":"MIN"
            },
            {
                "id":"max", 
                "name":"MAX"
            },
            {
                "id":"count", 
                "name":"COUNT"
            }]
        });
        var functionLineField = new Ext.form.field.ComboBox({
            labelWidth:50,
            padding:'10 0 0 5',
            displayField:'name',
            queryMode: 'local',
            width:140,
            valueField:'id',
            store:functionLineStore,
            scope:this
        });
        this.groupingSummaryColModel = [
        {
            header: 'Fields', //"Column",
            dataIndex: "columnname",
            width: 100
        }, {
            header: 'Header Name', //"Display Name",
            dataIndex: "displayfield",
            editor: new Ext.form.TextField({
                validateOnBlur: true,
                allowBlank: false
            }),
            width: 150
        } , {
            header: 'Function', //"Function Type",
            dataIndex: "functiontype",
            width: 100,
            renderer: function (val, m, rec){
                if((rec.data.xtype == "2" || rec.data.xtype == 2) && !rec.data.isSummaryFormula){
                    functionLineField.setValue(rec.data.functiontype); //set selected value to combo
                    return (functionLineField.getDisplayValue()); //return selected value (Symbol/Code)
                } else {
                    rec.data.functiontype = "";
                    functionLineField.setValue('');
                    return '';
                }
            },
            editor: functionLineField
        }, {
            header: 'Column Width(%)', //"Column Width(%)",
            dataIndex: "colwidth",
            editor:this.columnWidth,
            width: 100
        },{
            header: 'Column No.', 
            dataIndex: "colno",
            editor: this.columnNo,
            width: 70
        },{
            header: 'Header with Currency',
            dataIndex: 'headercurrency',
            width: 80,
            renderer: function (val, m, rec) {
                var isAmountColumn = false;
                if (val == '' || val == "false")
                    val = false;
                else
                    val = true;
                if(rec.data.xtype == "2"){
                    isAmountColumn = true;
                } else {
                    rec.data.headercurrency = "";
                    headerCurrencyCombo.setValue('');
                    return '';
                }

                var gridEle = getEXTComponent("groupingSummaryGrid");
                if(isAmountColumn){
                    headerCurrencyCombo.setValue(rec.data.headercurrency); //set selected value to combo
                    if(rec.data.headercurrency !== "" && rec.data.headercurrency != null){
                        rec.data.displayfield = rec.data.displayfield.replace(/#Currency&nbsp;Symbol#/g, "!##");
                        rec.data.displayfield = rec.data.displayfield.replace(/#Currency Symbol#/g, "!##");
                        rec.data.displayfield = rec.data.displayfield.replace(/#Currency&nbsp;Code#/g, "!##");
                        rec.data.displayfield = rec.data.displayfield.replace(/#Currency Code#/g, "!##");

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
                    }
                    return (headerCurrencyCombo.getDisplayValue()); //return selected value (Symbol/Code)
                }
            },
            editor:headerCurrencyCombo //Combo editor for currency
        },{
            header: 'Records with Currency',
            dataIndex: 'recordcurrency',
            width: 100,
            renderer: function (val, m, rec) {
                if (val == '' || val == "false")
                    val = false;
                else
                    val = true;
                if(rec.data.xtype == "2"){
                    recordCurrencyCombo.setValue(rec.data.recordcurrency); //set selected value to combo
                    return (recordCurrencyCombo.getDisplayValue()); //return selected value (Symbol/Code)
                } else {
                    rec.data.recordcurrency = "";
                    recordCurrencyCombo.setValue('');
                    return '';
                }
            },
            editor:recordCurrencyCombo //Combo editor for currency
        },{
            header: 'Decimal Points', //"Decimal Points",
            dataIndex: "decimalpoint",
            editor: this.decimal,
            width: 80
        },{
            header: 'Amount in Comma',
            dataIndex: 'commaamount',
            xtype: 'checkcolumn',
            width: 100,
            renderer : 
            function(val, m, rec){
                if (val == ''|| val == "false" || val == undefined)
                    val = false;
                else
                    val = true;
                if(rec.data.xtype == "2"){
                    return (new Ext.ux.CheckColumn()).renderer(val);
                }
                else{
                    return '';
                }
            }
        },{
            header: 'Actions', //"Actions",
            dataIndex: 'id',
            width: 50,
            renderer: function (value, css, record, row, column, store) {
                var actions = "<image src='images/up.png' title='Move Up' onclick=\"beforechangeseq('" + record.get('seq') + "',0, 'groupingSummaryGrid')\"/>" +
                "<image src='images/down.png' style='padding-left:5px' title='Move Down' onclick=\"beforechangeseq('" + record.get('seq') + "',1, 'groupingSummaryGrid')\"/>";
                return actions;
            }
        },
        {
            header: 'Remove',
            width: 50,
            renderer: function (value, css, record, row, column, store) {
                return "<image src='images/Delete.png' onclick=\"beforedeleteRecord('" + record.get('columnname') + "', 'groupingSummaryGrid')\"/>";
            }
        }
        ];

        this.groupingSummaryStore = new Ext.create('Ext.data.Store', {
            fields: ['columnname', 'displayfield', 'functiontype', 'hidecol', 'seq', 'fieldid', 'coltotal', 'colwidth', 'xtype', 'headerproperty', 'showtotal', 'recordcurrency', 'headercurrency', 'decimalpoint','commaamount','colno', 'isformula', 'formula', 'formulavalue', 'isSummaryFormula'],
            data: groupingSummaryColumns, // [["Product Name","Product Name",false,'0','1'],["Product Description","Product Description",false,'1','2'],["Rate","Rate",false,'2','3'], ["Quantity","Quantity",false,'3','4'],["Total Amount","Total Amount",false,'4','5']/*,["Campaign","Campaign"]*/],
            autoLoad: true
        });
        var cellEditing = Ext.create('Ext.grid.plugin.CellEditing', {
            clicksToEdit: 2
        });
        this.groupingSummaryGrid = Ext.create('Ext.grid.Panel', {
            columns: this.groupingSummaryColModel,
            store: this.groupingSummaryStore,
            clicksToEdit: 2,
            height:250,
            viewConfig: {
                forceFit: true
            },
            renderTo: this.id,
            id: 'groupingSummaryGrid', // don't delete this id
            plugins: [cellEditing]
        });

        this.groupingSummaryGrid.on('cellclick', this.cellclickhandle, this);
        this.groupingSummaryGrid.on('edit', this.afterEdit, this);
        this.groupingSummaryGrid.on('beforeedit', this.beforeEdit, this);
        
    },
    
    beforeEdit: function (e) {
        if (e.context.field == 'decimalpoint') {
            var str = e.context.record.data.columnname;
            var str = str.toLowerCase();
            
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
        var store = this.groupingSummaryGrid.getStore();
        groupingSummaryColumns = [];

        var recCount = store.getCount();
        var arr = [];
        for (var cnt = 0; cnt < recCount; cnt++) {
            var record = store.getAt(cnt);
            groupingSummaryColumns[cnt] = record.data;
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
        var jarray = getJSONArray(this.groupingSummaryGrid, true, arr);
        return jarray;
    }
});

function getGroupingSummaryTablePropertyPanel(event,ele) {
                 
    event.stopPropagation();
    Ext.get(selectedElement).removeCls("selected");
    selectedElement = ele.parentNode.id;
    Ext.getCmp(selectedElement).addClass('selected');
    createGroupingSummaryTablePropertyPanel(ele);
}

function createGroupingSummaryTablePropertyPanel( ele ) {
    var selectedEle = getEXTComponent(selectedElement);
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
                var tableContainer=document.getElementById("idgroupingsummarytablecontainer");
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
                var tableContainer=document.getElementById("idgroupingsummarytablecontainer");
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
                var tableContainer=document.getElementById("idgroupingsummarytablecontainer");
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
                var tableContainer=document.getElementById("idgroupingsummarytablecontainer");
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
                var tableContainer=document.getElementById("idgroupingsummarytablecontainer");
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
    var backgroundColorpicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: selectedEle.backgroundColor ? selectedEle.backgroundColor : '#FFFFFF',        
            id: 'idbackgroundcolorpicker',
        margin:'7 0 0 5',
        listeners: {
            change: function (thiz, newVal) {
                Ext.getCmp('idSelectedbackgroundPanel').body.setStyle('background-color', newVal);
                
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idgroupingsummarytable");
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
        width: 25,
        border: false,
        margin:'8 0 0 0',

    });

    var backgroundColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 250,
        border: false,
        items: [backgroundColorLabel, selectedbackgroundColorPanel, backgroundColorpicker]
    });

   var dataBackgroundColorLabel = Ext.create("Ext.form.Label", {
        text: "Background Color:",
        padding: '5 0 5 0',
        width: 105

    });
    var dataBackgroundColorpicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: selectedEle.databackgroundColor ? selectedEle.databackgroundColor : '#FFFFFF',        
            id: 'iddatabackgroundcolorpicker',
        margin:'7 0 0 5',
        listeners: {
            change: function (thiz, newVal) {
                Ext.getCmp('iddataSelectedbackgroundPanel').body.setStyle('background-color', newVal);
                
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idgroupingsummarytable");
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
        width: 25,
        border: false,
        margin:'8 0 0 0'
    });

    var dataBackgroundColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 250,
        border: false,
        items: [dataBackgroundColorLabel, dataSelectedBackgroundColorPanel, dataBackgroundColorpicker]
    });
    
    var textColorLabel = Ext.create("Ext.form.Label", {
        text: "Text Color:",
        padding: '5 0 5 0',
        width: 105

    });
    var datatextColorpicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: selectedEle.textColor ? selectedEle.textColor : '#FFFFFF',        
            id: 'idtextcolorpicker',
        listeners: {
            change: function (thiz, newVal) {
                Ext.getCmp('idSelectedTextColorPanel').body.setStyle('background-color', newVal);
                
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idgroupingsummarytable");
                var cells=table.rows[0].cells;
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
        width: 25,
        border: false,
        margin: '1 0 0 0'
    });

    var textColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 250,
        border: false,
        items: [textColorLabel, selectedTextColorPanel, datatextColorpicker]
    });
    
    var dataTextColorLabel = Ext.create("Ext.form.Label", {
        text: "Text Color:",
        padding: '5 0 5 0',
        width: 105

    });
    var dataTextColorpicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: selectedEle.dataTextColor ? selectedEle.dataTextColor : '#000000',        
        id: 'colorpickerdata',        
        listeners: {
            change: function (thiz, newVal) {
                Ext.getCmp('iddataSelectedTextColorPanel').body.setStyle('background-color', newVal);
                
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idgroupingsummarytable");
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
        width: 25,
        border: false,
        margin :'1 0 0 0'
    });

    var dataTextColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 250,
        border: false,
        items: [dataTextColorLabel, dataSelectedTextColorPanel, dataTextColorpicker]
    });
    
    var borderColorLabel = Ext.create("Ext.form.Label", {
        text: "Border Color:",
        padding: '5 0 5 0',
        width: 105

    });
    var borderColorpicker = Ext.create('Ext.ux.ColorPicker', {
        luminanceImg: '../../images/luminance.png',
        spectrumImg: '../../images/spectrum.png',
        value: selectedEle.borderColor ? selectedEle.borderColor : '#FFFFFF',        
            id: 'idbordercolorpicker',
        listeners: {
            change: function (thiz, newVal) {
                Ext.getCmp('idSelectedBorderPanel').body.setStyle('background-color', newVal);
                updateProperty(selectedElement);
                var selected = getEXTComponent(selectedElement);
                var table=document.getElementById("idgroupingsummarytable");
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
        width: 25,
        border: false,
        margin:'1 0 0 0',

    });

    var borderColorPanel = Ext.create("Ext.Panel", {
        layout: 'column',
        width: 250,
        border: false,
        items: [borderColorLabel, selectedborderColorPanel, borderColorpicker]
    });

    var updateBtn = Ext.create('Ext.Button', {
        text: 'Edit Global Summary Table',
        id: 'idUpdateBtn',
        style: {
            'float': 'right',
            'margin-right': '30px'
        },
        listeners: {
            'click': function ()
            {
                var globalSummaryTableParentPanel = Ext.getCmp(selectedElement);
                
                var Posx=globalSummaryTableParentPanel.x;
                var Posy=globalSummaryTableParentPanel.y;
                openGroupingSummaryWindow(this,globalSummaryTableParentPanel,true);
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
                var table=document.getElementById("idgroupingsummarytable");
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
                var table=document.getElementById("idgroupingsummarytable");
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
                var table=document.getElementById("idgroupingsummarytable");
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
                var table=document.getElementById("idgroupingsummarytable");
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
                var table=document.getElementById("idgroupingsummarytable");
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
                var table=document.getElementById("idgroupingsummarytable");
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
                var table=document.getElementById("idgroupingsummarytable");
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
                var table=document.getElementById("idgroupingsummarytable");
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
                var table=document.getElementById("idgroupingsummarytable");
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
                var table=document.getElementById("idgroupingsummarytable");
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
                var table=document.getElementById("idgroupingsummarytable");
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
                var table=document.getElementById("idgroupingsummarytable");
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
    
    var spacesettings = {
        xtype: 'fieldset',
        width:260,
        title:'Space Settings',
        id:'idspacesettingsfieldset',
        items: [
        width
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
        spacesettings,fontsettings,datasettings,colorSettings,marginsettings
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
    Ext.getCmp("idEastPanel").setTitle("Global Summary Table Property Panel");
    Ext.getCmp("idEastPanel").add(propertyPanelRoot);
    Ext.getCmp("idEastPanel").doLayout(); 
}

function getPropertyPanelForGroupingSummaryFields(event,div) {
    event.stopPropagation();
    createPropertyPanelForGroupingSummaryFields(div);
//    setPropertyPanelForGroupingSummaryFields(div);
}

function createPropertyPanelForGroupingSummaryFields(div){
    
    var preTextVal = "";
    var postTextVal = "";
    if(div.isPreText != undefined && div.isPreText){
        preTextVal = div.childNodes[0].innerHTML;
    }
    if(div.isPostText != undefined && div.isPostText){
        postTextVal = div.childNodes[div.childNodes.length-1].innerHTML;
    }
    
    var preText = {
        xtype: 'textfield',
        id: 'idpreTextValue',
        fieldLabel: 'Pre-text Value',
        padding: '2 2 2 2',
        value:preTextVal,
        listeners: {
            change: function (e) {
                addPrePostTextInGroupingField(div, e.getValue(), 1); // 1 for pretext
            }
        }
    };
    var postText = {
        xtype: 'textfield',
        id: 'idpostTextValue',
        fieldLabel: 'Post-text Value',
        padding: '2 2 2 2',
        value:postTextVal,
        listeners: {
            change: function (e) {
                addPrePostTextInGroupingField(div, e.getValue(), 2); // 2 for posttext
            }
        }
    };
    var boldText = {
        xtype: 'checkbox',
        id: 'idBoldText',
        fieldLabel: 'Bold',
        checked:div.isBold,
        padding:'2 2 2 2',
        listeners: {
            change: function (checkbox) {
                if(checkbox.getValue()){
                    div.style["font-weight"]="bold";
                    div.isBold = true;
                } else{
                    div.style["font-weight"]="";
                    div.isBold = false;
                }
            }
        }
    };
    var italicText = {
        xtype: 'checkbox',
        id: 'idItalicText',
        fieldLabel: 'Italic',
        checked:div.isItalic,
        padding:'2 2 2 2',
        listeners: {
            change: function (checkbox) {
                if(checkbox.getValue()){
                    div.style["font-style"]="italic";
                    div.isItalic = true;
                } else{
                    div.style["font-style"]="";
                    div.isItalic = false;
                }
            }
        }
    };
    
    var fontSettings = {
        xtype:'fieldset',
        title:'Font Settings',
        autoHeight:true,
        id:'idFontSettingsfieldset',
        width:270,
        items:[
            boldText,italicText
        ]
    };
    
    var prePostTextSettings = {
        xtype:'fieldset',
        title:'Pre/Post-text Settings',
        autoHeight:true,
        id:'idPrePosttextSettingsfieldset',
        width:270,
        items:[
            preText, postText
        ]
    };
    
    var fieldPropertyPanel =  Ext.create("Ext.Panel", {
        width: 300,
        div:div,
        autoHeight:true,
        border: false,
        id: 'idPropertyPanel',
        padding: '5 5 5 5',
        items: [
            prePostTextSettings
        ]
    });
    
    var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);
    var propertyPanel = Ext.getCmp("idPropertyPanel");
    if (propertyPanel){
        propertyPanelRoot.remove(propertyPanel.id);
    }
    propertyPanel = fieldPropertyPanel; 
    propertyPanelRoot.add(propertyPanel);
    Ext.getCmp("idEastPanel").setTitle("Grouping Field Property Panel");
    Ext.getCmp("idEastPanel").add(propertyPanelRoot);
    Ext.getCmp("idEastPanel").doLayout();
}

function addPrePostTextInGroupingField(div, txt, type){
    var id =  div.id+type;
    if (!document.getElementById(id)) {
        var initialdiv = div;
        var initialDivHtml = div.innerHTML;
        var newdiv = document.createElement('span');
        newdiv.innerHTML = txt;
        newdiv.setAttribute("id", id);
        newdiv.setAttribute("label", (type == 1 ? "PreText" : "PostText"));
        newdiv.setAttribute("type", type);
        if (type == 1) {
            div.innerHTML = newdiv.outerHTML + initialDivHtml;
            div.isPreText = true;
        } else {
            div.innerHTML = initialDivHtml + newdiv.outerHTML;
            div.isPostText = true;
        }
    } else{
        if(type == 1){
            var preTextEle = document.getElementById(id);
            preTextEle.innerHTML = txt;
        } else{
            var postTextEle = document.getElementById(id);
            postTextEle.innerHTML = txt;
        }
    }
}

function openFormulaBuilderWindowForGroupingSummary(fieldTypeId){
    this.saveFormulaWindow = Ext.create('FormulaBuilderDocumentDesigner', {
    isSaveAndCreateNew : true,
    moduleId: _CustomDesign_moduleId,
    moduleName:'',
    fieldSelectionGrid:this.fieldSelectionGrid,
    createStore : function(){
        
        var gridStore = Ext.create('Ext.data.Store', {
            id: 'measurFieldGridStoreId',
            model: 'FieldsModelDocumentDesigner',
            timeout : 180000,
            groupField: "columntype"
        });
        var gridEle = getEXTComponent("groupingSummaryGrid");
        var gridEleCurrentStore = gridEle.getStore().data.items;
        var gridEleStore = groupingSummaryColumnsArray;
        // Add Line level numeric fields
        for(var ind = 0; ind < gridEleStore.length; ind++){
            var storeRec = gridEleStore[ind];
            if((storeRec.xtype === "2" || storeRec.xtype === 2) && !storeRec.isformula){
                var rec = new FieldsModelDocumentDesigner({
                    id: storeRec.fieldid,
                    fieldid: storeRec.fieldid,
                    xtype: "2",
                    label: storeRec.displayfield,
                    defaultHeader: storeRec.displayfield.replace(/&nbsp;/g, " "),
                    columntype: "Line Level Fields"
                });
                gridStore.add(rec);
            }
        }
        // Add numeric fields from grouping summary table (Except summary formula)
        for(var ind = 0; ind < gridEleCurrentStore.length; ind++){
            var storeRec = gridEleCurrentStore[ind].data;
            if((storeRec.xtype === "2" || storeRec.xtype === 2) && !storeRec.isSummaryFormula){
                var tempRec = new FieldsModelDocumentDesigner({
                    id: storeRec.fieldid,
                    fieldid: storeRec.fieldid,
                    xtype: "2",
                    label: storeRec.columnname,
                    isSummaryFormula: true,
                    defaultHeader: storeRec.columnname.replace(/&nbsp;/g, " "),
                    columntype: "Summary Fields"
                });
                gridStore.add(tempRec);
            }
        }
        
        return gridStore;
    }
    });
    
    return this.saveFormulaWindow;
}