/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define('ReportBuilder.view.FormulaBuilder', {
    extend:'Ext.window.Window',
    xtype:'formulabuilder',
    buttonAlign: 'left',
    initComponent: function() {     
        var me=this;
        me.createButtonArray();
        me.createMeasureFieldGrid();
        me.createFunctionsGrid();
        me.createFormulaBuilderPanel();
//        me.createSummaryCalculationPanel();
        if(!this.isEditFlag) {
            this.measurefieldstoDrop=[];//used for storing number of fields used
        }
        if (!this.operatormeasurefieldsDrop) {
            this.operatormeasurefieldsDrop=[];//to set the value of formulatext
        }

        Ext.apply(this, {
            title: "Formula Builder",
            modal: true,
            iconCls: "pwnd favwinIcon",
            width: 625,
            height: 550,
            resizable: false,
            closable: false,
            constrain: true,
            layout: 'border',
            buttonAlign: 'right',
            items: [{
                region: 'north',
                height: 550,
                border:true,
                bodyStyle: 'background:#ffffff;font-size:10px;border-top:1px solid #ffffff;',
                items: [this.formulaBuilderPanel]
            }
        ],
            buttons: [{
                text: this.isEditFlag ?  ExtGlobal.getLocaleText("acc.common.update") : ExtGlobal.getLocaleText("acc.common.saveBtn"), 
                itemId: 'save',
                scope: this,
                handler: function() {
                    if(this.isEditFlag){
                        this.recordsToDrop[this.formulaIndex].defaultHeader =""
                    }
                        var saveflag = this.validatebeforesave();
                        if (saveflag) {
                            if (this.isEditFlag) {
                                columntype: "Default Fields",
                                        this.recordsToDrop[this.formulaIndex].defaultHeader = this.measureName.getValue();
                                if (this.recordsToDrop[this.formulaIndex].defaultHeader == "" || this.recordsToDrop[this.formulaIndex].defaultHeader == undefined) {
                                    this.recordsToDrop[this.formulaIndex].defaultHeader = this.formulaName;
                                }
                                this.recordsToDrop[this.formulaIndex].displayName = this.measureName.getValue();
                                this.recordsToDrop[this.formulaIndex].expressionValue = this.formulaText.getValue();
                                this.recordsToDrop[this.formulaIndex].expression = this.getOperatorExpressions();
                                if (this.recordsToDrop[this.formulaIndex].expression == "") {
                                    this.recordsToDrop[this.formulaIndex].expression = this.formulaExpression;
                                }
                                var properties = "";
                                var columntype = "";
                                if (this.measurefieldstoDrop.length > 0) {
                                    for (var i = 0; i < this.measurefieldstoDrop.length; i++) {//Assiging one of the parameters of Fields
                                        properties = this.measurefieldstoDrop[i].properties;
                                        columntype = this.measurefieldstoDrop[i].columntype;
                                    }
                                } else {//in case the formula builder expression is constant only eg: (2+3), properties needed to be set manually
                                    properties = JSON.parse("{\"sourceConfig\":{\"precision\":{\"editor\":{\"xtype\":\"numberfield\",\"minValue\":0,\"maxValue\":6},\"displayName\":\"Precision\",\"type\":\"number\"},\"sortOrder\":{\"editor\":{\"store\":[\"None\",\"ASC\",\"DESC\"],\"allowBlank\":false,\"xtype\":\"combobox\",\"forceSelection\":true},\"displayName\":\"Sorting Order\"},\"align\":{\"editor\":{\"store\":[\"Left\",\"Center\",\"Right\"],\"allowBlank\":false,\"xtype\":\"combobox\",\"forceSelection\":true},\"displayName\":\"Align Column\"},\"renderer\":{\"editor\":{\"store\":[\"Number\",\"Transaction Currency\",\"Base Currency\"],\"allowBlank\":false,\"xtype\":\"combobox\",\"forceSelection\":true},\"displayName\":\"Renderer\"}},\"source\":{\"precision\":\"2\",\"sortOrder\":\"None\",\"align\":\"Right\",\"renderer\":\"Number\",\"(Column Name)\":\"Total Amount\"}}");
                                }
                                this.recordsToDrop[this.formulaIndex].measurefieldjsonArray = JSON.stringify(this.measurefieldstoDrop);
                                this.recordsToDrop[this.formulaIndex].properties = properties;
                                this.recordsToDrop[this.formulaIndex].columntype = columntype;
                                this.recordsToDrop[this.formulaIndex].operatormeasurefieldsDrop = JSON.stringify(this.operatormeasurefieldsDrop);
                                var store = this.fieldSelectionGrid.getStore();
                                var rec = store.data.items[this.formulaIndex];
                                rec.data = this.recordsToDrop[this.formulaIndex];
                                store.insert(this.formulaIndex, rec);
                            } else {
                                var properties = "";
                                var isMeasureItem = false;
                                var columntype = "";
                                if (this.measurefieldstoDrop.length > 0) {
                                    for (var i = 0; i < this.measurefieldstoDrop.length; i++) {//Assiging one of the parameters of Fields
                                        properties = this.measurefieldstoDrop[i].properties;
                                        columntype = this.measurefieldstoDrop[i].columntype;
                                    }
                                } else {
                                    properties = JSON.parse("{\"sourceConfig\":{\"precision\":{\"editor\":{\"xtype\":\"numberfield\",\"minValue\":0,\"maxValue\":6},\"displayName\":\"Precision\",\"type\":\"number\"},\"sortOrder\":{\"editor\":{\"store\":[\"None\",\"ASC\",\"DESC\"],\"allowBlank\":false,\"xtype\":\"combobox\",\"forceSelection\":true},\"displayName\":\"Sorting Order\"},\"align\":{\"editor\":{\"store\":[\"Left\",\"Center\",\"Right\"],\"allowBlank\":false,\"xtype\":\"combobox\",\"forceSelection\":true},\"displayName\":\"Align Column\"},\"renderer\":{\"editor\":{\"store\":[\"Number\",\"Transaction Currency\",\"Base Currency\"],\"allowBlank\":false,\"xtype\":\"combobox\",\"forceSelection\":true},\"displayName\":\"Renderer\"}},\"source\":{\"precision\":\"2\",\"sortOrder\":\"None\",\"align\":\"Right\",\"renderer\":\"Number\",\"(Column Name)\":\"Total Amount\"}}");
                                }
                            var rec = ReportBuilder.model.FieldsModel.create({
                                columntype :"Default Fields",
                                defaultHeader :this.measureName.getValue(),
                                displayName :this.measureName.getValue(),
                                isforformulabuilder:true,
                                expressionValue:this.formulaText.getValue(),//used for showing tooltip 
                                expression:this.getOperatorExpressions(),
                                xtype:Ext.fieldType.numberField,
                                customfield:false,
                                moduleName:this.moduleName,
                                moduleid:this.moduleId,
                                measurefieldjsonArray:JSON.stringify(this.measurefieldstoDrop),
                                isMeasureItem:isMeasureItem,
                                columntype:columntype,
                                properties:properties,
                                operatormeasurefieldsDrop:JSON.stringify(this.operatormeasurefieldsDrop),
                                isMeasureItem:true
                            });
                        var store =this.fieldSelectionGrid.getStore();
                        var index =store.data.items.length;
                        store.insert(index,rec);
                            this.recordsToDrop.push(rec.data);
                        }
                        this.measurefieldstoDrop = [];
                        this.operatormeasurefieldsDrop = [];
                        this.close();
                    }// end of save
                }
            },
            {
                text: ExtGlobal.getLocaleText("acc.common.cancelBtn"),
                itemId: 'cancel',
                scope: this,
                handler: function() {
                    this.measurefieldstoDrop=[];
                    this.operator="";
                    this.close();
                }
            }]
        });
        this.callParent(arguments);
    },
    createButtonArray:function(){
        this.buttonArray=[];
        this.addButton=Ext.create('Ext.Button', {
            text: '+',
            id: 'addition',
            minWidth : 25,
            minHeight:25, 
            scope:this,
            handler: function() {
//                this.descText.setValue('"Field1"+"Field2"');
                var validateflag=this.validateExpressions();
                if(validateflag){
                        this.operator=this.operator+"+ ";
                        this.pushDummyRecData("+",false);
                        this.updateFormulaFieldTextValue();
                }
            }
        }); 
        this.buttonArray.push(this.addButton);
                
        this.numberone=new Ext.create('Ext.Button', {
            text: "1",
            id: 'numberone',
            scope:this,
            minWidth : 25,
            minHeight:25, 
            handler: function() {
                this.operator=this.operator+"1";
                this.pushDummyRecData("1",true);
                this.updateFormulaFieldTextValue();
            }
        }); 
        this.buttonArray.push(this.numberone);
        
        this.numbertwo=new Ext.create('Ext.Button', {
            text: "2",
            id: 'numbertwo',
            scope:this,
            minWidth : 25,
            minHeight:25, 
            handler: function() {
                this.operator=this.operator+"2";
                this.pushDummyRecData("2",true);
                this.updateFormulaFieldTextValue();
            }
        }); 
        this.buttonArray.push(this.numbertwo);
        
        
        this.numberthree=new Ext.create('Ext.Button', {
            text: "3",
            id: 'numberthree',
            scope:this,
            minWidth : 25,
            minHeight:25, 
            handler: function() {
                this.pushDummyRecData("3",true);
                this.updateFormulaFieldTextValue();
            }
        }); 
        this.buttonArray.push(this.numberthree);
        
        this.subButton=Ext.create('Ext.Button', {
            text: "-",
            id: 'sub',
           minWidth : 25,
            minHeight:25, 
            scope:this,
            handler: function() {
//                this.descText.setValue('"Field1"-"Field2"');
                var validateflag=this.validateExpressions();
                if(validateflag){
                    this.pushDummyRecData("-",false);
                    this.updateFormulaFieldTextValue();
                }
            }
        });
        this.buttonArray.push(this.subButton);
                
        this.numberfour=new Ext.create('Ext.Button', {
            text: "4",
            id: 'numberfour',
            scope:this,
            minWidth : 25,
            minHeight:25, 
            handler: function() {
                this.pushDummyRecData("4",true);
                this.updateFormulaFieldTextValue();
            }
        }); 
        this.buttonArray.push(this.numberfour);   
        
          this.numberfive=new Ext.create('Ext.Button', {
            text: "5",
            id: 'numberfive',
            scope:this,
            minWidth : 25,
            minHeight:25, 
            handler: function() {
                this.pushDummyRecData("5",true);
                this.updateFormulaFieldTextValue();
            }
        }); 
        this.buttonArray.push(this.numberfive);      
        
        this.numbersix=new Ext.create('Ext.Button', {
            text: "6",
            id: 'numbersix',
            scope:this,
            minWidth : 25,
            minHeight:25, 
            handler: function() {
                this.pushDummyRecData("6",true);
                this.updateFormulaFieldTextValue();
            }
        }); 
        this.buttonArray.push(this.numbersix);
        
        this.multiButton=Ext.create('Ext.Button', {
            text: '*',
            id: 'mult',
            minWidth : 25,
            minHeight:25, 
            scope:this,
            handler: function() {
//                this.descText.setValue('"Field1"*"Field2"');
                var validateflag=this.validateExpressions();
                if(validateflag){
                    this.pushDummyRecData("*",false);
                    this.updateFormulaFieldTextValue();
                }
            }
        }); 
        this.buttonArray.push(this.multiButton);
        
         this.numberseven=new Ext.create('Ext.Button', {
            text: "7",
            id: 'numberseven',
            scope:this,
            minWidth : 25,
            minHeight:25, 
            handler: function() {
                this.operator=this.operator+"7";
                this.pushDummyRecData("7",true);
                this.updateFormulaFieldTextValue();
            }
        }); 
        this.buttonArray.push(this.numberseven);
        
        this.numbereight=new Ext.create('Ext.Button', {
            text: "8",
            id: 'numbereight',
            scope:this,
            minWidth : 25,
            minHeight:25, 
            handler: function() {
                this.operator=this.operator+"8";
                this.pushDummyRecData("8",true);
                this.updateFormulaFieldTextValue();
            }
        }); 
        this.buttonArray.push(this.numbereight);
        
        this.numbernine=new Ext.create('Ext.Button', {
            text: "9",
            id: 'numbernine',
            scope:this,
            minWidth : 25,
            minHeight:25, 
            handler: function() {
                this.operator=this.operator+"9";
                this.pushDummyRecData("9",true);
                this.updateFormulaFieldTextValue();
            }
        }); 
        this.buttonArray.push(this.numbernine);
        
        this.divButton=Ext.create('Ext.Button', {
            text: "/",
            id: 'div',
            minWidth : 25,
            minHeight:25, 
            scope:this,
            handler: function() {
//                this.descText.setValue('"Field1"/"Field2"');
                var validateflag=this.validateExpressions();
                if(validateflag){
                    this.operator=this.operator+"/ ";
                    this.pushDummyRecData("/",false);
                    this.updateFormulaFieldTextValue();
                }
            }
        }); 
        this.buttonArray.push(this.divButton);

        this.openroundButton=Ext.create('Ext.Button', {
            text: "(",
            id: 'openround',
            minWidth : 25,
            minHeight:25,
            scope:this,
            handler: function() {
                this.pushDummyRecData("(",false);
                this.updateFormulaFieldTextValue();
            }
        }); 
        this.buttonArray.push(this.openroundButton); 

        this.numberzero=new Ext.create('Ext.Button', {
            text: "0",
            id: 'numberzero',
            scope:this,
            minWidth : 25,
            minHeight:25, 
            handler: function() {
                this.operator=this.operator+"0";
                this.pushDummyRecData("0",true);
                this.updateFormulaFieldTextValue();
            }
        }); 
        this.buttonArray.push(this.numberzero);
        
        this.closeroundButton=Ext.create('Ext.Button', {
            text: ')',
            id: 'closeround',
            minWidth : 25,
            minHeight:25,
            scope:this,
            handler: function() {
                this.pushDummyRecData(")",false);
                this.updateFormulaFieldTextValue();
            }
        }); 
        this.buttonArray.push(this.closeroundButton);
        
        this.BackSpace=new Ext.create('Ext.Button', {
            text: "BkSpc",
            id: 'BkSpc',
            scope:this,
            minWidth : 25,
            minHeight:25, 
            handler: function() {
                var index=this.operatormeasurefieldsDrop.length;
                this.operatormeasurefieldsDrop.pop(index-1);//removing the last element
                this.updateFormulaFieldTextValue();
            }
        }); 
        this.buttonArray.push(this.BackSpace);
        
        this.clearAllButton=new Ext.create('Ext.Button', {
            text: "C",
            id: 'clearformulabutton',
            scope:this,
            minWidth : 25,
            minHeight:25, 
            handler: function() {
                this.formulaText.setValue("");
                this.measurefieldstoDrop=[];
                this.operatormeasurefieldsDrop=[];
            }
        }); 
        this.buttonArray.push(this.clearAllButton);
        
        this.remendButton=Ext.create('Ext.Button', {
            text: '%',
            id: 'remender',
            hidden: true,
            minWidth : 5,
            scope:this,
            handler: function() {
                this.descText.setValue('"Field1"%"Field2"');
            }
        }); 
        this.buttonArray.push(this.remendButton);

        this.colunButton=Ext.create('Ext.Button', {
            text: ":",
            id: 'colun',
            hidden: true,
            minWidth : 5,
            scope:this,
            handler: function() {
                alert('You clicked the colun button!');
            }
        }); 
        this.buttonArray.push(this.colunButton);
        this.andButton=Ext.create('Ext.Button', {
            text: 'AND',
            id: 'and',
            hidden: true,
            scope:this,
            handler: function() {
                alert('You clicked the AND button!');
            }
        }); 
        this.buttonArray.push(this.andButton);
        this.orButton=Ext.create('Ext.Button', {
            text: "OR",
            id: 'or',
            hidden: true,
            scope:this,
            handler: function() {
                alert('You clicked the OR button!');
            }
        }); 
        this.buttonArray.push(this.orButton);
        this.notButton=Ext.create('Ext.Button', {
            text: 'NOT',
            id: 'not',
            scope:this,
            hidden: true,
            handler: function() {
                alert('You clicked the NOT button!');
            }
        }); 
        this.buttonArray.push(this.notButton);
        this.inButton=Ext.create('Ext.Button', {
            text: "IN",
            id: 'in',
            scope:this,
            hidden: true,
            handler: function() {
                alert('You clicked the IN button!');
            }
        }); 
        this.buttonArray.push(this.inButton);
        this.equalButton=Ext.create('Ext.Button', {
            text: '==',
            id: 'equal',
            minWidth : 5,
            scope:this,
            hidden: true,
            handler: function() {
                alert('You clicked the == button!');
            }
        }); 
        this.buttonArray.push(this.equalButton);
        this.notequalButton=Ext.create('Ext.Button', {
            text: "!=",
            id: 'notequal',
            minWidth : 5,
            scope:this,
            hidden: true,
            handler: function() {
                alert('You clicked the != button!');
            }
        });
        this.buttonArray.push(this.notequalButton);
        this.greatterButton=Ext.create('Ext.Button', {
            text: ">",
            id: 'greatter',
            minWidth : 5,
            hidden: true,
            scope:this,
            handler: function() {
                alert('You clicked the > button!');
            }
        }); 
        this.buttonArray.push(this.greatterButton);
        this.lessButton=Ext.create('Ext.Button', {
            text: '<',
            id: 'less',
            minWidth : 5,
            scope:this,
            hidden: true,
            handler: function() {
                alert('You clicked the < button!');
            }
        });  
        this.buttonArray.push(this.lessButton);
        this.greatterequalButton=Ext.create('Ext.Button', {
            text: '>=',
            hidden: true,
            id: 'greatterequal',
            minWidth : 5,
            scope:this,
            handler: function() {
                alert('You clicked the >= button!');
            }
        }); 
        this.buttonArray.push(this.greatterequalButton);
        this.lessequalButton=Ext.create('Ext.Button', {
            text: "<=",
            id: 'lessequal',
            minWidth : 5,
            hidden: true,
            scope:this,
            handler: function() {
                alert('You clicked the <= button!');
            }
        }); 
        this.buttonArray.push(this.lessequalButton);
    },
    createMeasureFieldGrid: function(){
        this.measurFieldGridStore=Ext.create('Ext.data.Store', {
            id: 'measurFieldGridStoreId',
            model: 'ReportBuilder.model.FieldsModel',
            timeout : 180000,
            groupField: "columntype",
            proxy: {
                type: 'ajax',
                url: 'ACCCreateCustomReport/getFiledsData.do',
                actionMethods : getStoreActionMethods(),
                reader: {
                    type: 'json',
                    rootProperty: "data"
                }
            }
        });

        this.measurFieldGrid=Ext.create('Ext.grid.Panel', {
            itemId:'measurFieldGrid',
            border: true,
            scope: this,
            store: this.measurFieldGridStore,
            scrollable: 'y',
            emptyText: 'No Matching Records',
            height: 250,
            width: 370,
            hideHeaders: true,
            style:'padding-bottom:10px;',
            cls:'cc-grid-title',
            features: Ext.create('Ext.grid.feature.Grouping', {
                groupHeaderTpl: '{name} {defaultHeader} ({rows.length} Item{[values.rows.length > 1 ? "s" : ""]})',
                enableGroupingMenu:false
            }),
            columns: [
            {
                text: ExtGlobal.getLocaleText("acc.common.columnName"),
                dataIndex: 'defaultHeader',
                flex: 1,
                sortable: true,
                filter: {
                    type: 'string',
                    itemDefaults: {
                        emptyText: ExtGlobal.getLocaleText("acc.common.searchemptyText")
                    }
                },
                renderer: function(value, metaData, record) {
                    metaData.tdAttr = 'data-qtip="' + value + '"';
                    return value;
                }
            }
            ]
        });
        this.measurFieldGridStore.load();
        this.measurFieldGridStore.on("beforeload", function() {
            this.measurFieldGridStore.proxy.extraParams = {
                id: this.moduleId,
                moduleCategory: this.moduleCategory,
                xtype:Ext.fieldType.numberField,
                isforformulabuilder:true
            }
        }, this);
        
        this.measurFieldGrid.on('celldblclick',function(){//on double click inserting elements in fieldset
            var isFieldNotExist = undefined;// top avoid duplicate field entries in this.measurefieldstoDrop
            var recordsdetails = undefined;
            recordsdetails = this.measurefieldstoDrop.getIemtByParam({
                id: this.measurFieldGrid.getSelectionModel().getSelected().items[0].data.id
            })
            if (recordsdetails == undefined) {
                isFieldNotExist = true;
            }
            if (isFieldNotExist) {
                this.measurefieldstoDrop.push(this.measurFieldGrid.getSelectionModel().getSelected().items[0].data);
            }  
            var selectedfieldforexpression = {
                defaultHeader : this.measurFieldGrid.getSelectionModel().getSelected().items[0].data.defaultHeader,
                id : this.measurFieldGrid.getSelectionModel().getSelected().items[0].data.id
            };
            
            this.operatormeasurefieldsDrop.push(selectedfieldforexpression);
            this.updateFormulaFieldTextValue();
        },this);
    },
    createFunctionsGrid: function(){
        this.functionStore=Ext.create('Ext.data.Store', {
            storeId: 'functionStore',
            fields:[ 'name', 'id', 'formula', 'description'],
            data: []//'Concatinate()','concatinate','Concatinate("TextFieldName1","TextFieldName2")','Concatinate\n\nJoins multiple text strings into a single text string.\n\n']
        });

        this.functionGrid=Ext.create('Ext.grid.Panel', {
            store: Ext.data.StoreManager.lookup('functionStore'),
            columns: [
            {
                dataIndex: 'name', 
                width: 198
            }
            ],
            height: 220,
            width: 200,
            hideHeaders: true
        });
    },
    createFormulaBuilderPanel: function(){
        this.measureName=Ext.create('Ext.form.field.Text',{
            fieldLabel: "Measure Name"+"*",
            name: 'measeureName',
            labelWidth: 100,
            width: 550,
            maxLength: 600,
            value: this.formulaName != undefined ? this.formulaName : "",
            allowBlank: false
        }); 
        
        this.descText=new Ext.create('Ext.form.field.TextArea',{
            name: 'description',
            width: 210,
            height:160,
            editable:false,
            hidden:true
//            emptyText:"No Functions yet."
        });
        
        this.formulaText=Ext.create('Ext.form.field.TextArea',{
            fieldLabel: "Formula"+"*",
            name: 'formulaText',
            id: 'formulaText',
            value: this.formulaExpressionValue != undefined ? this.formulaExpressionValue : "",
            labelWidth: 100,
            width: 550,
            maxLength: 650,
            allowBlank: false,
            validateBlank: true,
            disabled:true,
            disabledCls: "textAreaOpacitycss"
        });
        
        this.validateButton=new Ext.create('Ext.Button', {
            text: "Validate",
            id: 'validate',
            scope:this,
            hidden:true,
            handler: function() {
                this.validatebeforesave();
            }
        }); 
        
//        var calculator = Ext.create('ReportBuilder.extension.Calculator');
        
        this.formulaBuilderPanel = new Ext.panel.Panel({
//            title: "Formula Builder",
            bodyStyle: "background: transparent;",
            border: false,
            style: "background: transparent;padding:20px;",
            labelWidth: 165,
            width: 625,
            height: 500,
            items: [{
                region: 'north',
                height: 50,
                border:false,
                layout : 'column',
                bodyStyle: 'background: transparent;padding:10px;',
                items: [this.measureName]
            },{
                region: 'center',
                height: 85,
                border:false,
                layout : 'column',
                bodyStyle: 'background: transparent;padding:10px;',
                items: [this.formulaText]
            }, {
                region: 'south',
                border: false,
                height: 325,
                bodyStyle: 'background: transparent;padding:10px;',
                layout : 'column',
                items: [{
                    xtype: 'panel',
                    border: false,
                    title: 'Fields :',
//                    style: 'margin-left:54px; color:#ffffff;',
                    style: 'color:#ffffff;',
                    width: 370,
                    height: 365,
                    items: [this.measurFieldGrid,this.validateButton]
                },{
                    xtype: 'panel',
                    border: true,
                    title: 'Operators & Functions :',
                    style: 'padding-left:10px;',
                    bodyCls:'buttons-spacing',
                    width: 180,
                    height: 268,
                    buttonAlign: 'left',
//                    items:[calculator]
                    items: this.buttonArray
                }
//                ,{
//                    xtype: 'panel',
//                    border: false,
//                    title: 'Functions:',
//                    style: 'padding-left:10px;',
//                    width: 220,
//                    height: 350,
//                    items: [this.descText,{
//                        xtype: 'checkboxfield',    
//                        boxLabel: 'Show arguments in Formula',
//                        name: 'showarg',
//                        inputValue: '1',
//                        hidden:true,
//                        style: 'padding-top:20px;',
//                        id: 'showarg'
//                    }]
//                }
            ]
            }]
        });
    },
    validatebeforesave:function(){
        var saveflag=true;
        if(this.formulaText.getValue()!=undefined && this.formulaText.getValue()!='' ){
            var openingbracketscount=this.formulaText.getValue().split('(').length-1;
            var closingbracketscount=this.formulaText.getValue().split(')').length-1;
            if(openingbracketscount>closingbracketscount){ //checking opeing and closing brackets
                Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"),ExtGlobal.getLocaleText("acc.field.CustomReportBuilder.closingBracketsalertmsg"), Ext.Msg.INFO);
                saveflag=false;
            }else if(openingbracketscount<closingbracketscount){
                Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"),ExtGlobal.getLocaleText("acc.field.CustomReportBuilder.openingBracketsalertmsg"), Ext.Msg.INFO);
                saveflag=false;
            }else if(this.formulaText.getValue().charAt(this.formulaText.getValue().length-1)=='+'||this.formulaText.getValue().charAt(this.formulaText.getValue().length-1)=='-'||this.formulaText.getValue().charAt(this.formulaText.getValue().length-1)=='*'||this.formulaText.getValue().charAt(this.formulaText.getValue().length-1)=='/'||this.formulaText.getValue().charAt(this.formulaText.getValue().length-1)=='%'){
                var operand=this.formulaText.getValue().charAt(this.formulaText.getValue().length-1);
                Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"),"Please select the Field or Measure after operand '"+operand+"'", Ext.Msg.INFO);
                saveflag= false;
            }else if(this.measureName.getValue()==''){
                Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"),ExtGlobal.getLocaleText("acc.field.CustomReportBuilder.assignMeasureName"), Ext.Msg.INFO);
                saveflag= false;
            }else if(this.measureName.getValue()!=''){
                for(var i=0;i<this.recordsToDrop.length;i++){//to Check duplicate name
                    if(this.recordsToDrop[i].defaultHeader!=undefined  && this.measureName.getValue().trim()==this.recordsToDrop[i].defaultHeader.trim() && this.measureName.getValue().trim()!= this.formulaName){
                        if(this.isEditFlag) {
                            if(this.recordsToDrop[i].defaultHeader == "") {
                                this.recordsToDrop[i].defaultHeader=this.formulaName;
//                                Ext.CustomMsg(ExtGlobal.getLocaleText("acc.comm/on.alert"), ExtGlobal.getLocaleText("acc.field.CustomReportBuilder.assignMeasureName"), Ext.Msg.INFO);
                            }
                        } else {
                            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"), ExtGlobal.getLocaleText("acc.field.CustomReportBuilder.duplicateMeasureName"), Ext.Msg.INFO);
                        }
                        saveflag=false;
                    }
                }
            }
        }else if(this.formulaText.getValue()==''){
            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"),"Please select the Field or Measure.", Ext.Msg.INFO);
            saveflag=false;
        }
       
        return saveflag;
    },
    validateExpressions:function(){
        var validateflag=true;
        if(this.formulaText!=undefined && this.formulaText.getValue()==''){
            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"),"Please select the Field or Measure first.", Ext.Msg.INFO);
            validateflag=false;
        }else if(this.formulaText.getValue().charAt(this.formulaText.getValue().length-1)=='+'||this.formulaText.getValue().charAt(this.formulaText.getValue().length-1)=='-'||this.formulaText.getValue().charAt(this.formulaText.getValue().length-1)=='*'||this.formulaText.getValue().charAt(this.formulaText.getValue().length-1)=='/'||this.formulaText.getValue().charAt(this.formulaText.getValue().length-1)=='%'){
            var operand=this.formulaText.getValue().charAt(this.formulaText.getValue().length-1);
            Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"),"Please select the Field or Measure after operand '"+operand+"'", Ext.Msg.INFO);
            validateflag=false;
        }
        return validateflag;
    },
    pushDummyRecData:function(defaultHeader,isnumber){
        var recdata={
            defaultHeader:defaultHeader,
            isnumber:isnumber
        }
        this.operatormeasurefieldsDrop.push(recdata);
    },
    updateFormulaFieldTextValue:function(){
       var expression="";
       for(var i=0;i<this.operatormeasurefieldsDrop.length;i++){
           if(this.operatormeasurefieldsDrop[i].isnumber!=undefined && this.operatormeasurefieldsDrop[i].isnumber){
                expression +=this.operatormeasurefieldsDrop[i].defaultHeader;
            }else{
                expression +=this.operatormeasurefieldsDrop[i].defaultHeader+" ";
            }
       }
       this.formulaText.setValue(expression);
    },
    getOperatorExpressions:function(){
        var expression=""; 
        for(var i=0;i<this.operatormeasurefieldsDrop.length;i++){
            if(this.operatormeasurefieldsDrop[i].isnumber!=undefined && this.operatormeasurefieldsDrop[i].isnumber){
                expression +=this.operatormeasurefieldsDrop[i].defaultHeader;
            }else if(this.operatormeasurefieldsDrop[i].isnumber!=undefined && !this.operatormeasurefieldsDrop[i].isnumber)  {
                expression +=this.operatormeasurefieldsDrop[i].defaultHeader+"";
            }else{
                var selectedfield = this.operatormeasurefieldsDrop[i].id;
                selectedfield="PreText_"+selectedfield.replace(/[-]/ig,"_");
                expression +=selectedfield+" ";
            }
        }
       return expression.replace(/[.]/ig,"");
    },
    createSummaryCalculationPanel: function(){
        this.calculationTypeStore = Ext.create('Ext.data.Store', {
            fields: ['id', 'name'],
            data : [{
                "id":"none", 
                "name":"None"
                },
                {
                "id":"countall", 
                "name":"CountAll"
                },
                {
                "id":"countdistinct", 
                "name":"CountDistinct"
                },
                {
                "id":"custom", 
                "name":"Custom"
                },
                {
                "id":"mode", 
                "name":"Mode"
                }]
        });

        this.calculationTypeCombo=Ext.create('Ext.form.ComboBox', {
            fieldLabel: 'Calculation',
            store: this.calculationTypeStore,
            queryMode: 'local',
            displayField: 'name',
            valueField: 'id',
            value:'countall'
        });
        
        this.summeryCalculationPanel = new Ext.panel.Panel({
            title: "Summary Calculation",
            bodyStyle: "background: transparent;",
            border: false,
            style: "background: transparent;padding:20px;",
            labelWidth: 165,
            width: 785,
            height: 452,
            disabled:true,
            items: [this.calculationTypeCombo]
        });
        
    //        this.summeryCalculationPanel.on('show',function(){
    //            alert('Hi need to small');
    //        },this);
    },
    setNewStyle : function(id){
        var c = Ext.getCmp(id).el.dom.children;
        var d = Ext.getCmp(id).el.dom.childNodes;
        if(c){
            c[0].className += "cc-grid-title";
        }else{
            d[0].className += "cc-grid-title";
        }
    },
    addFunction: function() {
                if(this.formulaText!=undefined && this.formulaText.getValue()==''){
                    Ext.CustomMsg(ExtGlobal.getLocaleText("acc.common.alert"),"Please select the Field or Measure first.", Ext.Msg.INFO);
                }else{
                    if(this.formulaText!=undefined){
                        var tmpText="";
                        tmpText=this.formulaText.getValue();
                        this.formulaText.reset();
                        this.formulaText.setValue(tmpText+"+");
                    }
                }
            }
});