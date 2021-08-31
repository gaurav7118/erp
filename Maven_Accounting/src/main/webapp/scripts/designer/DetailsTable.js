Ext.DetailsGridTemplateWindow = function (conf) {
    Ext.apply(this, conf);
    this.addEvents({
        "okClicked": true
    });
    this.createEditor();
    Ext.define('DetailsTableField', {
        extend: 'Ext.data.Model',
        fields: [
            'columnname',
            'displayfield',
            'hidecol',
            'seq',
            'fieldid',
            'coltotal',
            'colwidth',
            'xtype',
            'headerproperty',
            'showtotal',
            'recordcurrency',
            'headercurrency',
            'decimalpoint',
            'colno'
        ]
    });
    //Details Table fields store
    this.detailsTableFieldStore = Ext.create('Ext.data.Store', {
        model: 'DetailsTableField',
        proxy: {
            type: 'ajax',
            url : 'CustomDesign/getDetailsTableFields.do',
            extraParams : {
              moduleId : _CustomDesign_moduleId,
              detailsSubType_id : this.detailsTableSubType_id,
              detailsSubType_value : this.detailsTableSubType_value,
              companyId : companyid
            },
            reader: {
                type: "json",
                root: "data"
            }
        },
        listeners: {
            load: function(store, operation, options){
                store;
            }
        },
        autoLoad: true,
        sorters: [{
            property : 'columnname',
            direction: 'ASC'
        }]
    });
    this.selectField = new Ext.form.field.ComboBox({
        fieldLabel:"Select Field",
        padding:'10 10 10 10',
        displayField:'columnname',
        queryMode: 'local',
        valueField:'fieldid',
        emptyText:'Select a Field',
        store:this.detailsTableFieldStore,
        scope:this
    });
    this.addBtn = Ext.create('Ext.Button', {
        text: 'Add',
        margin:'10 10 10 10',
        id: 'idAddColumnToLineTable',
        scope:this,
        handler:function(){
            if(this.selectField && this.selectField.getValue() != undefined && this.selectField.getValue() != ""){
                var rec = searchRecord(this.detailsTableFieldStore, this.selectField.getValue(), 'fieldid');
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
        
    Ext.DetailsGridTemplateWindow.superclass.constructor.call(this, {
        width: 900,
        height: 500,
        resizable: false,
        bodyStyle: 'padding:15px 5px 0;background-color: #FFFFFF !important;background: #FFFFFF !important',
        title: 'Configure '+this.detailsTableSubType_value+' Details Table ', //"Edit Your Content",
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
                +"<div style='font-size:12px;font-style:bold;float:left;margin:7px 0px 0px 10px;width:100%;position:relative;'><b>"+((this.reportStore.data.items.length > 0)?"Edit "+this.detailsTableSubType_value+" Details Table":"Create "+this.detailsTableSubType_value+" Details Table")+"</b></div>"
                +"<div style='font-size:10px;float:left;margin:5px 0px 10px 10px;width:100%;position:relative;'><ul style='list-style-type:disc;padding-left:15px;'>"
                +"<li>Select a column from <b>Select Field</b> and click on <b>Add</b> to add specific column to "+this.detailsTableSubType_value+" Details Table</li>"
                +"</ul></div></div></div>"
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
                columnWidth : .15,
                border:false,
                items:this.addBlankColumnBtn
            }
            ]
        },
        this.reportGrid
        ],
        buttons: [
            {
                text: "Border Property",
                scope: this,
                handler: this.tableproperty
            },
            {
            text: "OK",
            scope: this,
            handler:function(){
                var totalWidth=0;
                if(Ext.getCmp(selectedElement).isPanel){//To remove space(&nbsp;) from section if empty(No any element inside). //Check for insert Line Item table in empty section
                    Ext.getCmp(selectedElement).update("");
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
                        if(totalWidth != 100){
                            Ext.MessageBox.show({
                                title: 'Alert',
                                msg: 'Sum of widths of details table columns should be equal to 100 ',
                                icon: Ext.MessageBox.INFO,
                                buttons: Ext.MessageBox.OK
                            });
                        } else{
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

Ext.extend(Ext.DetailsGridTemplateWindow, Ext.Window, {
    onRender: function (conf) {
        Ext.DetailsGridTemplateWindow.superclass.onRender.call(this, conf);
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
                    for (var i = 0; i < blankRowColumnNumberArr.length; i++) {
                        if (blankRowColumnNumberArr[i] == newVal) {
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

        var currencyStore = new Ext.data.SimpleStore({      //ERP-24827 : Provide option to set currency symbol or code for line item details
            fields: [{
                name:'typeid',
                type:'int'
            }, 'currency'],
            data :[[0,'Currency Symbol'],[1,'Currency Code'],[2,'Base Currency Symbol'],[3,'Base Currency Code']]
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
                var isAmountColumn = false;
                if (val == '' || val == "false")
                    val = false;
                else
                    val = true;
                if (rec.data.xtype == "2") {
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
            },
            editor:headerCurrencyCombo //Combo editor for currency
        }, {
            header: 'Records with Currency',
            dataIndex: 'recordcurrency',
            width: 80,
            renderer:
            function (val, m, rec) {
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
            renderer: function(val, m, rec){
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
        ];

        this.reportStore = new Ext.create('Ext.data.Store', {
            fields: ['columnname', 'displayfield', 'hidecol', 'seq', 'fieldid', 'coltotal', 'colwidth', 'xtype', 'headerproperty', 'showtotal', 'recordcurrency', 'headercurrency', 'decimalpoint','commaamount','colno'],
            data: detailsTableColumnsObj[this.detailsTableId], // [["Product Name","Product Name",false,'0','1'],["Product Description","Product Description",false,'1','2'],["Rate","Rate",false,'2','3'], ["Quantity","Quantity",false,'3','4'],["Total Amount","Total Amount",false,'4','5']/*,["Campaign","Campaign"]*/],
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
            plugins: [cellEditing]
        });

        this.reportGrid.on('cellclick', this.cellclickhandle, this);
        this.reportGrid.on('beforeedit', this.beforeEdit, this);
        this.reportGrid.on('edit', this.afterEdit, this);
        
    },
    beforeEdit: function (e) {
        if (e.context.field == 'decimalpoint') {            
            var str = e.context.record.data.columnname;
            var str = str.toLowerCase();
            if (e.context.record.data.isNumeric === true) {
                /*
                 * for fields having xtype 1 but we have to apply decimal and comma fields to them
                 */
                return true;
            } else if(e.context.record.data.xtype == "2") {
                /*
                 * for numeric type custom fields decimal precision should be editable
                 * for Default fields with xtype 2
                 */
                return true;
            } else{
                return false;
            }
        }
        if (e.context.field == 'colno') {
            var maxvalue = this.getmaxcolumnno(e);
            e.grid.columns[3].getEditor().setMaxValue(maxvalue);
            return true;
        }
    },
    cellclickhandle: function (scope, td, cellIndex, record, tr, rowIndex, e, eOpts) {
        if (e.getTarget("a[class='setheaderproperty']")) {
            this.record = record;
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
        } else {
            this.record = record;
            this.record.data.displayfield = this.record.data.displayfield.replace(/&nbsp;/g,' ');
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
        }
        if(obj.context.record != null){
            obj.context.record.set("displayfield", obj.context.record.data.displayfield);
            obj.context.record.commit();
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
            modal:true,
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
    getGridConfigSetting: function (detailsTableId) {
        var store = this.reportGrid.getStore();
        detailsTableColumnsObj[detailsTableId] = [];

        var recCount = store.getCount();
        var arr = [];
        for (var cnt = 0; cnt < recCount; cnt++) {
            var record = store.getAt(cnt);
            detailsTableColumnsObj[detailsTableId][cnt] = record.data;
            record.data.displayfield = record.data.displayfield.replace(/\s/g,space);
            if (record.data.displayfield.replace(/\s/g, "") == "") {
                WtfComMsgBox(["Alert", "Please enter a valid display name"], 0)
                return;
            } else if (record.data.hidecol == false && record.data.colwidth.value == "") {
                WtfComMsgBox(["Alert", "Please enter a valid width"], 0)
                return;
            }
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

function openDetailsTableWindowAndSetConfig(containerScope, panelId, isEdit, Posx, Posy, detailsTableColumnsObj, detailsTableParentPanel) {
    isSequnceChanged = false;
    isRowDeleted = false;
    
    var _tw = new Ext.DetailsGridTemplateWindow({
        tableID: 'detailsTableConfig_'+containerScope.detailsTableId,
        detailsTableColumnsObj: detailsTableColumnsObj,
        detailsTableId: containerScope.detailsTableId,
        detailsTableSubType_id: containerScope.detailsTableSubType_id,
        detailsTableSubType_value: containerScope.detailsTableSubType_value
    });
    _tw.on("okClicked", function (obj) {
        var detailsTableId = "";
        if (isEdit == true) {
            detailsTableId = containerScope.detailsTableId;
        } else{
            if(detailsTableCount[containerScope.detailsTableSubType_id] != undefined){
                detailsTableId = containerScope.detailsTableSubType_id+(parseInt(detailsTableCount[containerScope.detailsTableSubType_id])+1);
            } else{
                detailsTableId = containerScope.detailsTableSubType_id+1;
            }
        }
        var valObj = obj.getGridConfigSetting(detailsTableId);
        var jArr = eval(valObj);
        var fontsize="";
        var headerProperties = {};
        var headerStyle = {};
        var rowProperties = {};
        var tableHeaderAttributes = "";
        
        if (detailsTableParentPanel != null) {
            if (detailsTableParentPanel.items) {
                if (detailsTableParentPanel.items.items) {
                    if (detailsTableParentPanel.items.items[0]) {
                        var lineConfig = detailsTableParentPanel.items.items[0];
                        
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
                        if(lineConfig.getEl().dom.children[0].children[1].children[1].cells){
                            var tds = lineConfig.getEl().dom.children[0].children[1].children[1].cells;
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
        var c, r, t,b, firstRow;
        if(isEdit==true){
            if (document.getElementById("detailsTableConfig_"+detailsTableId))
                remove(document.getElementById("detailsTableConfig_"+detailsTableId));
            if (document.getElementById("detailstablecontainer_"+detailsTableId))
                remove(document.getElementById("detailstablecontainer_"+detailsTableId));
        }
        var tableConfig = document.createElement('table');
        r = tableConfig.insertRow(0);
        for(var cnt = 0; cnt < tableHeaderAttributes.length; cnt++){ // Set Header Attribute to New header
            var attrName = tableHeaderAttributes[cnt].nodeName;
            var attrValue = tableHeaderAttributes[cnt].nodeValue;
            r.setAttribute(attrName, attrValue);
        }
        t = tableConfig.insertRow(1);
        tableConfig.setAttribute("class", "sectionclass_element_100");
        tableConfig.setAttribute("id", "detailsTableConfigSectionPanelGrid_"+detailsTableId);
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
            //Table Header
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
                newTH.setAttribute("align", headerpropertyparam.alignment);
                newTH.style.textAlign = headerpropertyparam.alignment;
                newTH.innerHTML = headerpropertyparam.changedlabel.replace(/\b&nbsp;/g," ");
                newTH.setAttribute("label", headerpropertyparam.changedlabel);
            } else {
                newTH.innerHTML = obj.displayfield? obj.displayfield.replace(/\b&nbsp;/g," ") : obj.columnname.replace(/\b&nbsp;/g," ");
                newTH.setAttribute("label", obj.displayfield? obj.displayfield.replace(/\b&nbsp;/g," ") : obj.columnname.replace(/\b&nbsp;/g," "));
            }
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
            newTH.setAttribute("seq", obj.seq);
            newTH.setAttribute("xtype", obj.xtype);
            newTH.setAttribute("headerproperty", obj.headerproperty);
            newTH.setAttribute("fieldid", obj.fieldid);
            newTH.setAttribute("headercurrency", obj.headercurrency);
            newTH.setAttribute("commaamount", obj.commaamount!=""?obj.commaamount:"");
            newTH.setAttribute("recordcurrency", obj.recordcurrency);
            newTH.setAttribute("colno", obj.colno);
            newTH.setAttribute("decimalpoint", obj.decimalpoint != "" ? obj.decimalpoint : "")
            
            newTH.value = obj.fieldid;
            newTH.cellIndex = obj.seq;
            if ( !fontsize ) {
                fontsize = "";
            }
            newTH.style.fontSize = fontsize + "px";
            r.appendChild(newTH);
            //Table Row
            var tabletd = document.createElement('td');
            tabletd.cellIndex = newTH.cellIndex;
            tabletd.setAttribute("cellIndex", newTH.cellIndex);
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
        }
        tableConfig.appendChild(r);
        tableConfig.appendChild(t);
        var PosX = detailsTableParentPanel.cursorCustomX;
        var PosY = detailsTableParentPanel.cursorCustomY;
        if (isEdit == true) {
            PosX = Posx;
            PosY = Posy;
        }
            
        var field = getDetailsTable(PosX, PosY, tableConfig.outerHTML, detailsTableParentPanel,(detailsTableParentPanel.items && detailsTableParentPanel.items.items[0])? detailsTableParentPanel.items.items[0]:detailsTableParentPanel, detailsTableId, isEdit, headerProperties);
        detailsTableParentPanel.add(field);
        initTreePanel();//Refreshing the tree panel after adding Line Item
        detailsTableParentPanel.doLayout();
        
    }, containerScope);
    _tw.show();
}//endOf_openDetailsTableWindowAndSetConfig

function getDetailsTable(PosX, PosY, html, designerPanel, obj, detailsTableId, isEdit, headerProperties) {
    /*When we import template and save it then template was not soving without edit line item table. 
    *Prbolem - jousap format HTML,thats why some extra space and line break add in HTML
    *For Remove that Extra space and line break below Regex use. 
    */
    html = html.replace(/\n*>\s*\n*\s*</g,"><");
    var fontsize="";
    var bold="false";
    var italic="false";
    var underline="false";
    var align="center";
    var bordercolor="";
    var width = 100;
    var marginTop = (obj && obj.marginTop) || (obj && obj.marginTop === 0) ? obj.marginTop : '0px';
    var marginBottom = (obj && obj.marginBottom) || (obj && obj.marginBottom === 0) ? obj.marginBottom : '5px';
    var marginLeft = (obj && obj.marginLeft) || (obj && obj.marginLeft === 0) ? obj.marginLeft : '0px';
    var marginRight = (obj && obj.marginRight) || (obj && obj.marginRight === 0) ? obj.marginRight : '0px';
    var fontfamily = (obj && obj.fontfamily) ? obj.fontfamily : '';
    var consolidatedfield = (obj && obj.consolidatedfield) ? obj.consolidatedfield : '';
    var summationfields = (obj && obj.summationfields) ? obj.summationfields : '';

    width = (obj && obj.tablewidth) ? obj.tablewidth : 100;
    align = (obj && obj.align) ? obj.align : 1;
    fontsize = (obj && obj.fontsize) ? obj.fontsize : 0;
    bordercolor =  (obj && obj.bordercolor) ? obj.bordercolor : "#FFFFFF";
    if (obj && obj.data && obj.data[0]) {
        fontsize = obj.data[0].fontsize ? obj.data[0].fontsize : "";
        bold = obj.data[0].bold ? "true" : "false";
        italic = obj.data[0].italic ? "true" : "false";
        underline = obj.data[0].underline ? "true" : "false";
        bordercolor = obj.data[0].bordercolor ? obj.data[0].bordercolor : "";
        var tempalign=obj.data[0].align?obj.data[0].align:"center";
        if(tempalign=="left" || tempalign===0){
            align=0;
        } else if(tempalign=="center" || tempalign===1 ){
            align=1;
        } else{
            align=2;
        }
    }
    if(headerProperties){
        if(headerProperties.isbold){
            bold=headerProperties.isbold;
        }
        if(headerProperties.isitalic){
            italic=headerProperties.isitalic;
        }
        if(headerProperties.isunderline){
            underline=headerProperties.isunderline;
        }
        if(headerProperties.fontsize != undefined){
            fontsize=headerProperties.fontsize;
        }
        if(headerProperties.tablewidth != undefined){
            width=headerProperties.tablewidth;
        }
        if(headerProperties.marginBottom != undefined){
            marginBottom=headerProperties.marginBottom;
        }
        if(headerProperties.marginLeft != undefined){
            marginLeft=headerProperties.marginLeft;
        }
        if(headerProperties.marginRight != undefined){
            marginRight=headerProperties.marginRight;
        }
        if(headerProperties.marginTop != undefined){
            marginTop=headerProperties.marginTop;
        }
        if(headerProperties.bordercolor){
            bordercolor=headerProperties.bordercolor;
        }
        if(headerProperties.align != undefined){
            align=headerProperties.align;
        }
    }
    // Columns present in old line items
    var columns = [];
    if(obj && !obj.columns && obj.data){
        columns = obj.data[0].lineitems;
        for(var i= 0 ; i < columns.length ; i++){
            columns[i].seq = i+1;
            if(!columns[i].columnname){
                columns[i].columnname = columns[i].label;
            }
            if(!columns[i].displayfield){
                if(obj.data[0].columndata){
                    var columnData = eval("(" + obj.data[0].columndata + ")");
                    for(var j = 0 ; j< columnData.length ; j++){
                        if(columnData[j].columnname == columns[i].columnname){
                            if(columnData[j].displayfield){
                                columns[i].displayfield = columnData[j].displayfield;
                            }
                            break;
                        }
                    }
                }
                if(!columns[i].displayfield){
                    columns[i].displayfield = columns[i].label;
                }
            }
        }
    } else{
        columns = detailsTableColumnsObj[detailsTableId]?detailsTableColumnsObj[detailsTableId]:(obj && obj.columns)?obj.columns:[];
    }
    detailsTableColumnsObj[detailsTableId] = columns;
    var field = Ext.create(Ext.Component, {
        x: PosX,
        y: PosY,
        id: 'detailstablecontainer_'+detailsTableId,
        draggable: false,
        fontsize:fontsize,
        fontfamily:fontfamily,
        bold:bold,
        marginTop : marginTop,
        marginBottom:marginBottom,
        marginLeft:marginLeft,
        marginRight:marginRight,
        consolidatedfield:consolidatedfield,
        summationfields:summationfields,
        italic:italic,
        underline:underline,
        tablewidth:width,
        align:align,
        bordercolor:bordercolor,
        pageSize:obj.pageSize?obj.pageSize:"a4",
        pageOrientation:obj.pageOrientation?obj.pageOrientation:"portrait",
        columns:columns,
        detailsTableSubType_id:obj.detailsTableSubType_id,
        detailsTableSubType_value:obj.detailsTableSubType_value,
        detailsTableId:detailsTableId,
        initDraggable: function () {
            var me = this,
            ddConfig;
            ddConfig = Ext.applyIf({
                el: me.el//,-Reimius, I commented out the next line that delegates the dragger to the header of the window
            }, me.draggable);
            me.dd = new Ext.util.ComponentDragger(this, ddConfig);
            me.relayEvents(me.dd, ['dragstart', 'drag', 'dragend']);
        }, //initDraggable
        containerId: designerPanel.id,
        fieldType: Ext.fieldID.insertDetailsTable,
        resizable: false,
        cls : 'sectionclass_field_without_border sectionclass_element_100',
        ctCls : 'sectionclass_field_container',
        unit: (obj != null || obj != undefined) ? obj.unit : '',
        fieldDefaults: {
            anchor: '100%'
        },
        layout: {
            type: 'vbox',
            align: 'stretch'  // Child items are stretched to full width
        },
        html: html, // this string is compared. Search with 'customize line items
        onRender: function () {
            this.superclass.onRender.call(this);
            addPositionObjectInCollection(this);
            this.el.dom.style.marginTop = marginTop + "px";
            this.el.dom.style.marginBottom = marginBottom  + "px";
            this.el.dom.style.marginLeft = marginLeft  + "px"; 
            this.el.dom.style.marginRight = marginRight  + "px";
            this.el.dom.style.width = width  + "%";
            var table = document.getElementById('detailsTableConfigSectionPanelGrid_'+detailsTableId);
            if(table){
                table.setAttribute("panelid", designerPanel.id);
                table.setAttribute("detailsTableId", detailsTableId);
            }
            if(obj && obj.data){
                if(obj.data[1]){
                    createGlobalFieldsForLineTable(JSON.stringify(obj.data[1]),field,true);
                }
                if(obj.data[2]){
                    createGlobalFieldsForLineTable(JSON.stringify(obj.data[2]),field,false);
                }
            }
            this.el.on('click', function (eventObject, target, arg) {
                var component = designerPanel.queryById(this.id)
                if (component) {
                    eventObject.stopPropagation();
                    if (selectedElement != null || selectedElement != undefined)
                    {
                        if(Ext.get(selectedElement)){
                        Ext.get(selectedElement).removeCls("selected");
                    }
                    }
                    selectedElement = this.id;
                    Ext.getCmp(selectedElement).addClass('selected');
                    createPropertyPanel(selectedElement);
                    setProperty(selectedElement);
                    showElements(selectedElement);
                }
            });
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
                createContextMenu(Ext.fieldID.insertDetailsTable,id);
                contextMenuNew.showAt(event.getXY());
                return false;
            });
        }, //onRender
        listeners: {
            onMouseUp: function (field) {
                field.focus();
            },
            removed: function () {

            },
            afterrender: function () {
                this.el.on('click', function (eventObject, target, arg) {
                    var component = designerPanel.queryById(this.id)
                    if (component && Ext.getCmp('contentImage')) {
                        Ext.getCmp('contentImage').getEl().dom.src = Ext.BLANK_IMAGE_URL;
                        eventObject.stopPropagation();

                    }
                });
            }
        }
    });
    if(!isEdit){
        if(detailsTableCount[obj.detailsTableSubType_id] != undefined){
            detailsTableCount[obj.detailsTableSubType_id] = (parseInt(detailsTableCount[obj.detailsTableSubType_id])+1);
        } else{
            detailsTableCount[obj.detailsTableSubType_id] = 1;
        }
    }
    return field;
}

function createDetailsTablePropertyPanel(ele) {
    var propertyPanelRoot = Ext.getCmp(Ext.ComponenetId.idPropertyPanelRoot);
    var propertyPanel = Ext.getCmp("idPropertyPanel");
        
    if (propertyPanel)
        propertyPanelRoot.remove(propertyPanel.id);

    var elementId = {
        xtype: 'textfield',
        fieldLabel: 'Element ID',
        id: 'idElementId',
        hidden:true,
        readOnly: true
    };

    var topMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Top Margin',
        id: 'idtopmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var bottomMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Bottom Margin',
        id: 'idbottommargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var leftMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Left Margin',
        id: 'idleftmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    };
    var rightMargin = {
        xtype: 'numberfield',
        fieldLabel: 'Right Margin',
        id: 'idrightmargin',
        labelWidth: 100,
        value: 0,
        padding:'2 2 2 2',
        width: 210,
        minLength: 1,
        maxLength: 3,
        maxValue:100,
        minValue:0,
        hidden:false,//type.value == "Auto"
        listeners: {
            change: function (field) {
                updateProperty(selectedElement);
            }
        }
    }
    
    var marginsettings = {
        xtype: 'fieldset',
        title: 'Margin Settings',
        id: 'idmarginsettings',
        width:260,
        items:[
            topMargin, bottomMargin, leftMargin, rightMargin 
        ]
    };
    var width = {
        xtype: 'numberfield',
        fieldLabel: 'Width (%)',
        id: 'idWidth',
        width: 210,
        minValue: 1,
        maxValue: 100,
        value:ele.tablewidth?ele.tablewidth:100,
        listeners: {
            change: function (e) {
                if ( e.getValue() > 100 ) {
                    e.setValue(100);
                    WtfComMsgBox(["Config Error", "Width cannot be greater than 100%."], 0);
                }
                updateProperty(selectedElement);
            }
        }
    };
    var unit = {
        xtype: 'combo',
        fieldLabel: '  Unit',
        id: 'idUnit',
        hidden:true,
        store: Ext.create("Ext.data.Store", {
            fields: ["id", "unit"],
            data: [
            {
                id: "1",
                unit: "px"
            }, {
                id: "2",
                unit: "%"

            }
            ]
        }),
        value: 'px',
        displayField: 'unit',
        valueField: 'id',
        width: 170
    };


    var updateBtn = {
        xtype: 'button',
        id: 'idUpdateBtn',
        text: 'Edit Details Table',
        style: {
            'margin-top': '5px'
        },
        listeners: {
            afterrender: function() {
                createToolTip('idUpdateBtn', 'Edit Details Table', '', 'top', true);
            },
            'click': function ()
            {
                var selectedfield = Ext.getCmp(selectedElement);
                var detailsTableParentPanel = Ext.getCmp(selectedElement).ownerCt;
                
                var Posx=selectedfield.x;
                var Posy=selectedfield.y;
                openDetailsTableWindowAndSetConfig(selectedfield, detailsTableParentPanel.id, true, Posx, Posy, detailsTableColumnsObj, detailsTableParentPanel);
            }
        }
    };
    
    var boldText = {
        xtype: 'checkbox',
        id: 'boldTextLineItemId',
        fieldLabel: 'Bold',
        padding:'2 2 2 2',
        checked: (ele.bold == 'true') ? true : false,
        listeners: {
            change: function (e) {
                updateProperty(selectedElement);
            }
        }
    };
    var italicText = {
        xtype: 'checkbox',
        id: 'italicTextLineItemId',
        fieldLabel: 'Italic',
        padding:'2 2 2 2',
        checked: (ele.italic == 'true') ? true : false,
        listeners: {
            change: function (checkbox, newVal, oldVal) {
                updateProperty(selectedElement);
            }
        }
    };
    
    var underLine = {
        xtype: 'checkbox',
        id: 'underLineTextLineItemId',
        fieldLabel: 'Underline',
        padding:'2 2 2 2',
        checked: (ele.underline == 'true') ? true : false,
        listeners: {
            change: function (checkbox, newVal, oldVal) {
                updateProperty(selectedElement);
            }
        }
    };
    
    var fontstore= Ext.create('Ext.data.Store', {
        fields: ['val', 'name'],
        data : [
        {
            "val":"", 
            "name":"None"
        },
        
        {
            "val":"sans-serif", 
            "name":"Sans-Serif"
        },

        {
            "val":"arial", 
            "name":"Arial"
        },

        {
            "val":"verdana", 
            "name":"Verdana"
        },

        {
            "val":"times new roman", 
            "name":"Times New Roman"
        },

        {
            "val":"tahoma", 
            "name":"Tahoma"
        },

        {
            "val":"calibri", 
            "name":"Calibri"
        },
        {
            "val":"courier new", 
            "name":"Courier New"
        }
        ]
    });
    
    var fontfields = {
        xtype: 'combo',
        fieldLabel: 'Font',
        id: 'fontDetailsTableId',
        displayField: 'name',
        valueField: 'val',
        store: fontstore,
        padding: '2 2 2 2',
        labelWidth: 100,
        width: 210,
        value: (ele != null || ele != undefined) ? ele.fontfamily : "",
        listeners: {
            'select': function (e) {
                updateProperty(selectedElement);
            }
        }
    };
    
    var fontSize = {
        xtype: 'numberfield',
        fieldLabel: 'Font Size',
        id: 'fontSizeLineItemId',
        value: (ele != null || ele != undefined) ? ele.fontsize : 'None', //parseInt to remove the px e.g 250px to 250
        width: 210,
        padding:'2 2 2 2',
        minLength: 1,
        maxLength: 3,
        minValue : 0,
        listeners: {
            'change': function () {
                updateProperty(selectedElement);
            }
        }
    };
    var alignStore;
    alignStore=new Ext.data.Store({
        fields : ['id','align'],
        data : [
        {
            id: 0, 
            align: 'Left'
        },

        {
            id: 1,    
            align: 'Center'
        },

        {
            id: 2, 
            align: 'Right'
        }

        ]
    });
    var alignCombo = new Ext.form.field.ComboBox({
        fieldLabel: 'Align Headers',
        store: alignStore,
        id: 'allignDetailsTableCombo',
        displayField: 'align',
        valueField: 'id',
        labelWidth:100,
        queryMode: 'local',
        width: 210,
        emptyText: 'Select alignment',
        forceSelection: false,
        editable: false,
        triggerAction: 'all',
        value :ele.align,
        padding:'2 2 2 2',
        listeners: {
            'change': function ()
            {
                updateProperty(selectedElement);
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
        title:'Font Settings',
        id:'idfontsettingsfieldset',
        items: [
           boldText,italicText, underLine, fontSize, fontfields
        ]
    };
    
    var headersettings = {
        xtype: 'fieldset',
        width:260,
        title:'Header Settings',
        id:'idheadersettingsfieldset',
        items: [
            alignCombo
        ]
    };
           
    /*
     * Array for consolidated field combo
     */
    var consolidatedFieldRecords = [];
    var summationFieldsRecords = [];
    consolidatedFieldRecords.push({
        id: "",
        name: "None"
    });
    var columnslength = ele.initialConfig.columns.length;
    // Get details table selected fields for consolidated combo (only xtype = 2)
    for(var cnt = 0; cnt < columnslength; cnt++){
        if(ele.initialConfig.columns[cnt].fieldid !== "0"){
            if(ele.initialConfig.columns[cnt].xtype == "1"){
                consolidatedFieldRecords.push({
                    id: ele.initialConfig.columns[cnt].fieldid,
                    name: ele.initialConfig.columns[cnt].columnname,
                    xtype: ele.initialConfig.columns[cnt].xtype
                });
            } else if(ele.initialConfig.columns[cnt].xtype == "2"){
                summationFieldsRecords.push({
                    id: ele.initialConfig.columns[cnt].fieldid,
                    name: ele.initialConfig.columns[cnt].columnname,
                    xtype: ele.initialConfig.columns[cnt].xtype
                });
            }
        }
    }
    // Store for consolidated fields
    var consolidatedfieldstore= Ext.create('Ext.data.Store', {
        fields: ['id', 'name', 'xtype'],
        data : consolidatedFieldRecords
    });
    // Store for summation fields
    var summationfieldsstore= Ext.create('Ext.data.Store', {
        fields: ['id', 'name', 'xtype'],
        data : summationFieldsRecords
    });
    // Combo for consolidated field
    var consolidatedfieldcombo = {
        xtype: 'combo',
        fieldLabel: 'Consolidation On',
        id:'consolidationfieldid',
        displayField: 'name',
        valueField: 'id',
        store: consolidatedfieldstore,
        value: ele != null ? (ele.consolidatedfield != undefined ? ele.consolidatedfield : '') : '',
        padding:'2 2 2 2',
        labelWidth:100,
        width:210,
        listeners: {
            'select': function (e) {
                updateProperty(selectedElement);
            }
        }
    };
    
    var summationFieldsCombo = {
        xtype: 'combo',
        fieldLabel: 'Summation Field(s)',
        id:'summationfieldsid',
        displayField: 'name',
        valueField: 'id',
        emptyText: 'All',
        store: summationfieldsstore,
        value: ele != null ? (ele.summationfields != undefined ? ele.summationfields : '') : '',
        padding:'2 2 2 2',
        labelWidth:100,
        multiSelect:true,
        width:210,
        listeners: {
            'change': function (e, a, b) {
                updateProperty(selectedElement);
            }
        }
    };
    
    var consolidationSettings = {
        xtype: 'fieldset',
        width:260,
        title:'Consolidation Settings',
        id:'idconsolidationsettingsfieldset',
        items: [
            consolidatedfieldcombo, summationFieldsCombo
        ]
    };
    
    propertyPanel = Ext.create("Ext.Panel", {
        autoHeight: true,
        width: 295,//ERP-18300 [Document Designer]- Label for "Edit Line Item" is not visible.
        border: false,
        layout:'column',
        id: 'idPropertyPanel',
        defaults: {
            margin: '7 0 0 10'
        },
        items: [
            updateBtn,elementId,unit,spacesettings,fontsettings,headersettings,marginsettings, consolidationSettings
        ]          
    });

    return propertyPanel;
}
