   /* Save 0 value in prerequiste attribute type course   */


Wtf.prereq = function(config) {
    Wtf.apply(this, config);
    this.createLCenterWindow=null;
    this.sendForm=null;

    this.groupingView = new Wtf.grid.GroupingView({
        forceFit: true
    //    groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})'
    });

    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.cmodel = new Wtf.grid.ColumnModel([
        {
            header: "",
            width: 150,
            dataIndex: 'id',
            hidden:true
        },{
            header: WtfGlobal.getLocaleText("acc.field.RuleName"),
            width: 50,
            dataIndex: 'rulename'
        },{
            header: WtfGlobal.getLocaleText("acc.field.DocumentType"),
            width: 75,
            dataIndex: 'transactionType',
            renderer:this.transactionTypeRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.field.FieldType"),
            width: 75,
            dataIndex: 'fieldType',
            renderer:this.typeRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.field.Amount/ProductName"),
            width: 150,
            dataIndex: 'value',
            renderer:this.valueRenderer
        },{
            header: WtfGlobal.getLocaleText("acc.field.ApprovalLevelRequired"),
            width: 150,
            dataIndex: 'approvallevel',
            renderer:function(value){
                return value + " Level";
            }
        }]);

this.gridRecord1 = Wtf.data.Record.create([{
    name: 'id',
    type: 'string'
    },{
    name: 'transactionType',
    type: 'string'
    }, {
    name: 'fieldType',
    type: 'string'
    },{
    name: 'value',
    type: 'string'
    }, {
    name: 'rulename',
    type: 'string'
    }, {
    name: 'productname',
    type: 'string'
    }, {
    name: 'approvallevel',
    type: 'int'
    },{
        name:'discountamount'
    }    

]);

this.gridReader = new Wtf.data.KwlJsonReader({
    root: "data",
    totalProperty:"count",
    remoteGroup:true,
    remoteSort: true
}, this.gridRecord1);

this.gridGroupStore1 = new Wtf.data.GroupingStore({
    proxy: new Wtf.data.HttpProxy({
        url: "CostCenter/getApprovalRules.do"
    }),
    reader: this.gridReader,
    sortInfo: {
        field: 'transactionType',
        direction: "ASC"
    },
    baseParams: {
        companyid:companyid
//        type: 103,
//        courseid: '',
//        ownerFlag:/*(Wtf.realroles.indexOf('46')>-1)?true:*/false
    },
    groupField:'transactionType'
});

this.gridGroupStore = new Wtf.data.GroupingStore({
    proxy: new Wtf.data.HttpProxy({
        url: "CostCenter/getApprovalRules.do" //"jspfiles/admin/acastructure.jsp"
    }),
    reader: this.gridReader,
    sortInfo: {
        field: 'transactionType',
        direction: "ASC"        
    },
    baseParams: {
        companyid:companyid
    },
    groupField:'transactionType'
});

this.grid=new Wtf.grid.GridPanel({
    id:'prereq'+this.id,
    ds: this.gridGroupStore,
    cm: this.cmodel,
    border: false,
    layout:'fit',
    enableColumnHide: false,
    view: this.groupingView,
    sm: this.sm,
    trackMouseOver: true,
    loadMask: {
        msg: WtfGlobal.getLocaleText("acc.msgbox.50")
    }/*,
    viewConfig: {
        forceFit: true
    }*/
});


this.programRecord = Wtf.data.Record.create([{
        name: 'transactionType',
        type: 'string'
    }, {
        name: 'moduleid',
        type: 'string'
    },{
        name: 'courseid',
        type: 'string'
    }
]);

this.programReader = new Wtf.data.JsonReader({
    root: "data"
}, this.programRecord);

this.programStore = new Wtf.data.Store({
    proxy: new Wtf.data.HttpProxy({
        url: "jspfiles/newCourse.jsp"
    }),
    reader: this.programReader
});

this.programStore1 = new Wtf.data.Store({
    proxy: new Wtf.data.HttpProxy({
        url: "jspfiles/newCourse.jsp"
    }),
    reader: this.programReader
});

this.ruleName = new Wtf.form.TextField({
        allowBlank: false,
        fieldLabel:WtfGlobal.getLocaleText("acc.field.RuleName*"),
        width: 250
});

var helpTip = WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.field.Amountgreaterthanprovidedamountwillbeconsideredforthisrule"));

this.ruleValue = new Wtf.form.TextField({
    allowBlank: false,
    fieldLabel:WtfGlobal.getLocaleText("acc.field.EnterAmountLimit")+'*(' + WtfGlobal.getCurrencySymbol() +")"+ helpTip ,
    labelWidth:160,
    width: 250
})
this.chkbox = new Wtf.form.Checkbox({
    fieldLabel:WtfGlobal.getLocaleText("acc.field.Displayinapplicationform")
})
this.ruleTypeStore=new Wtf.data.SimpleStore({
    fields :['abbr', 'ruletype'],
    data:[['1','Exact'],['2','Range'],['4','Duration more than'],['5','Duration less than']]
});
this.dateStore=new Wtf.data.SimpleStore({
    fields :['datetype'],
    data:[['Years'],['Months'],['Weeks'],['Days']]
});

this.fieldType = new Wtf.data.SimpleStore({
   fields :['id','valuefield'],
   data:[['1','Total Amount'],['2','Specific Product(s)'],['3','Specific Product(s) Discount']]
});

this.fieldTypeCombo = new Wtf.form.ComboBox({
    triggerAction: 'all',
    store:this.fieldType,
    mode:'local',
    width: 250,
    listWidth:'250',
    displayField:'valuefield',
    fieldLabel : WtfGlobal.getLocaleText("acc.field.AppliedUpon*"),
    valueField:'id',
    hiddenName :'ruletype',
    emptyText:WtfGlobal.getLocaleText("acc.field.SelectyourPreference"),
//    typeAhead: true,
//    forceSelection: true,
    allowBlank:false
});

this.approvalLevel = new Wtf.data.SimpleStore({
   fields :['level'],
   data:[[1],[2]]
});

this.approvalLevelCombo = new Wtf.form.ComboBox({
    triggerAction: 'all',
    store:this.approvalLevel,
    mode:'local',
    width: 250,
    listWidth:'250',
    displayField:'level',
    fieldLabel : WtfGlobal.getLocaleText("acc.field.ApprovalLevelRequired"),
    valueField:'level',
    hiddenName :'ruletype',
    emptyText:WtfGlobal.getLocaleText("acc.field.Selectapprovallevel"),
    allowBlank:false,
    value : 1
});

this.dateCombo = new Wtf.form.ComboBox({
    triggerAction: 'all',
    store:this.dateStore,
    mode:'local',
    width: '50',
    displayField:'datetype',
    emptyText:WtfGlobal.getLocaleText("acc.field.Selectduration"),
    typeAhead: true,
    forceSelection: true,
    allowBlank:false
});

this.courseCmb = new Wtf.form.ComboBox({
    allowBlank: false,
    typeAhead: true,
    forceSelection: true,
    fieldLabel:WtfGlobal.getLocaleText("acc.field.Pre-reqCourse*"),
    emptyText: WtfGlobal.getLocaleText("acc.field.Selectacourse..."),
    valueField:'moduleid',
    displayField:'transactionType',
    mode:'local',
    store:this.programStore1,
    triggerAction:'all',
    listWidth:'233',
    width: 233
});
this.courseCmb.on('expand', function() {
    var index = this.programStore1.find('moduleid', this.ruleName.getValue());
    if(index != -1) {
        this.prevRec = this.programStore1.getAt(index);
        this.programStore1.remove(this.prevRec);
    }
}, this);
this.courseCmb.on('collapse', function() {
    if(this.prevRec) {
        this.programStore1.insert(this.programStore.find('moduleid', this.prevRec.data.moduleid), this.prevRec);
    }
}, this);
this.attributeRecord = Wtf.data.Record.create([{
        name: 'configid',
        type: 'string'
    },{
        name: 'fieldname',
        type: 'string'
    },{
        name: 'configtype',
        type: 'string'
    }
]);

this.attributeReader = new Wtf.data.KwlJsonReader({
    root: "data"
}, this.attributeRecord);

this.attributeStore = new Wtf.data.Store({
    proxy: new Wtf.data.HttpProxy({
        url: "jspfiles/admin/LacaStructure.jsp"
    }),
    reader: this.attributeReader
});
this.attributeStore.on('load', function(store) {
    store.add(new this.attributeRecord({configid: 'course', fieldname: 'Course'}));
    this.items.items[0].ownerCt.doLayout();
}, this);

this.transactionType = new Wtf.data.SimpleStore({
    fields :['id', 'transactionnametype'],
    data:[
//        ['1','Purchase Order'],
//        ['2','Sales Order'],
        ['3','Purchase Invoice'],
        ['4','Sales Invoice']
    ]
});
    
this.transactionType = new Wtf.form.ComboBox({
    triggerAction: 'all',
    store:this.transactionType,
    mode:'local',
    width: 250,
    listWidth:'250',
    displayField:'transactionnametype',
    fieldLabel : WtfGlobal.getLocaleText("acc.field.Document*"),
    valueField:'id',
//    hiddenName :'configid',
    emptyText:WtfGlobal.getLocaleText("acc.field.SelectaDocument"),
//    typeAhead: true,
//    forceSelection: true,
    allowBlank:false
});
this.exactRec = this.ruleTypeStore.getAt(0);
this.rangeRec = this.ruleTypeStore.getAt(1);
this.minRec = this.ruleTypeStore.getAt(2);
this.maxRec = this.ruleTypeStore.getAt(3);

this.numField=new Wtf.form.NumberField({
    allowDecimals :false,
    allowNegative :false,
    fieldLabel: WtfGlobal.getLocaleText("acc.field.Value*"),
    width: 160,
    name: 'value',
    id:"numField"+this.id,
    allowBlank:false
});

this.newSuBttn=new Wtf.Toolbar.Button({
    text:WtfGlobal.getLocaleText("acc.common.submit"),
    iconCls :getButtonIconCls(Wtf.etype.save),
    tooltip :WtfGlobal.getLocaleText("acc.field.Submitthecurrentrule"),
    id: 'BtnSubNew' + this.id,
    scope: this
});

this.newSuBttn.on('click',this.clickHandle,this);

//this.productList = new Wtf.data.SimpleStore({
//    fields :['productid', 'productname'],
//    data:[['1','Pen'],['2','Note Book']]
//});
    this.productRecord = Wtf.data.Record.create([
    {
        name:'productid',
        type: 'string'
    },{
        name:'pid'
    },{
        name:'type'
    },

    {
        name:'productname',
        type: 'string'
    }
    ]);

    this.productStore = new Wtf.data.Store({
        //            url:Wtf.req.account+'CompanyManager.jsp',
        url:"ACCProduct/getProductsForCombo.do",
        baseParams:{
            mode:22
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.productRecord)
    });
    
    this.productStore.load();
    
this.rangepanel = new Wtf.Panel({
        layout:'column',
        border:false,
        id:'rangepanel0012',
        items:[{
            layout:'form',
            border:false,
            columnWidth:1.0,
            labelWidth:160,
            items:this.range1 = new Wtf.common.Select(Wtf.applyIf({
                width:250,
                listWidth:'250',
                fieldLabel:WtfGlobal.getLocaleText("acc.field.SelectProducts"),
                store:this.productStore,
                name:'productid',
                hiddenName:'productid',
                xtype:'select',
                selectOnFocus:true,
                extraFields:['pid','type'],
                extraComparisionField:'pid',// type ahead search on product id as well.
                extraComparisionFieldArray:['pid','productname'], // search on both pid and name
                listWidth:Wtf.ProductComboListWidth,
                labelWidth:160,
                forceSelection:true,
                multiSelect:true,
                displayField:'productname',
                valueField:'productid',
                mode: 'local',
                triggerAction:'all',
                typeAhead: true,
                allowBlank:false
            }
        ))
        },this.transactionType]
    })
this.valuetxt = new Wtf.Panel({
        border:false,
        height:30,
        layout:"form",
        labelWidth : 160,
        items:this.ruleValue
    })
    this.dateminmax = new Wtf.Panel({
        layout:'column',
        border:false,
        id:'dateminmax',
        items:[{
            layout:'form',
            border:false,
            columnWidth:.27,
            items:this.daterange = new Wtf.form.NumberField({
                width:120,
                fieldLabel:WtfGlobal.getLocaleText("acc.field.SelectProducts"),
                emptyText:WtfGlobal.getLocaleText("acc.field.EnterNo"),
                allowBlank: false,
                allowDecimals:false
            })
        },{
            layout:'fit',
          columnWidth:.12,
            border:false,
            bodyStyle:'padding-left:10px',
            labelWidth:0,
            items:this.dateCombo
        }]
    })

    this.datevalue = new Wtf.Panel({
        border:false,
        layout:"form",
        items:this.dtvalue = new Wtf.form.DateField({
           fieldLabel:WtfGlobal.getLocaleText("acc.inventoryList.date") ,
           width:230,
           allowBlank: false,
            format:'m/d/Y'
        })
    })
    this.daterangepanel = new Wtf.Panel({
        layout:'column',
        border:false,
        id:'daterangepanel',
        items:[{
            layout:'form',
            border:false,
            columnWidth:.27,
            items:this.daterange1 = new Wtf.form.DateField({
                width:'90%',
                fieldLabel:WtfGlobal.getLocaleText("acc.field.DateRange"),
                format:'m/d/Y'
            })
        },{
            layout:'fit',
            columnWidth:.12,
            border:false,
            bodyStyle:'padding-left:10px',
            labelWidth:0,
            items:this.daterange2 = new Wtf.form.DateField({
                format:'m/d/Y'
            })
        }]
    })

this.valuepanel = new Wtf.Panel({
        id:"valuepanel",
        border:false,
        layout:"fit",
        items:[this.valuetxt,this.rangepanel]//,this.daterangepanel,this.datevalue,this.dateminmax]
    })

this.ruleId  = new Wtf.form.Hidden({
    fieldLabel:''
})

//this.ruleId.hide();

this.NewRuleBttn=new Wtf.Toolbar.Button({
    text:WtfGlobal.getLocaleText("acc.field.New"),
    iconCls :getButtonIconCls(Wtf.etype.add),
    tooltip :WtfGlobal.getLocaleText("acc.field.Newrule"),
    id: 'BtnNew1' + this.id
});
this.NewRuleBttn.on('click',this.NewRule,this);

this.DeleteBttn=new Wtf.Toolbar.Button({
    text:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
    iconCls :getButtonIconCls(Wtf.etype.deletebutton),
    tooltip :WtfGlobal.getLocaleText("acc.field.Deleteselectedrule"),
    id: 'BtnDel' + this.id,
    scope: this,
    disabled:true
});
this.DeleteBttn.on('click',this.deleteMessage,this);

this.EditBttn=new Wtf.Toolbar.Button({
    text:WtfGlobal.getLocaleText("acc.field.UpdateRule"),
    iconCls :getButtonIconCls(Wtf.etype.resetbutton),
    tooltip :WtfGlobal.getLocaleText("acc.field.Updateselectedrule"),
//    id: 'BtnDel' + this.id,
    scope: this,
    disabled:true
});
this.EditBttn.on('click',this.editRule,this);

Wtf.prereq.superclass.constructor.call(this,{
    autoDestroy:true,
    border: false,
    layout :'border',
    items:[{
        //                 xtype : 'KWLListPanel',
        title : WtfGlobal.getLocaleText("acc.field.Rule"),
        //iconCls :getButtonIconCls(Wtf.etype.deskera),
        paging : false,
        autoLoad : false,
        region:"north",
        height:250,
        //                  layout:'fit',
        bodyStyle : "background:#f0f0f0;",
        border: false,
        bbar:[this.NewRuleBttn,this.DeleteBttn,this.newSuBttn,this.EditBttn],
        layout:"fit",
        items: [
            {
                border:false,
                layout:'form',
                bodyStyle:'padding:13px 13px 13px 13px',
                labelWidth:160,
                items: [
                    this.ruleName,
                    this.transactionType,
//                    this.courseCmb,
                    this.fieldTypeCombo,
//                    this.valuepanel
                    
                    this.rangepanel,
                    this.valuetxt,
                    this.ruleId,
                    this.approvalLevelCombo
                    
                ]
            }]
        },{
        //                  xtype : 'KWLListPanel',
        title : WtfGlobal.getLocaleText("acc.field.ApprovalRules"),
        //iconCls :getButtonIconCls(Wtf.etype.deskera),
        paging : false,
        autoLoad : false,
        region:"center",
        layout:'fit',
        border: false,
//        tbar: ['Quick Search: ', this.quickPanelSearch = new Wtf.KWLTagSearch({
//            width: 210,
//            emptyText : 'Enter rule name',
//            field:"programname"
//        }),{
////            text:'Set Rules',
////            handler:this.setRules,
////            tooltip :'Set rules for course',
////            scope:this
//        }],
         bbar:this.pg = new Wtf.PagingSearchToolbar({
                    id: 'pgTbarModule' + this.id,
                    pageSize: 15,
//                    searchField:this.quickPanelSearch,
                    store: this.gridGroupStore,
                    displayInfo: true,
                    //displayMsg: WtfGlobal.getLocaleText("acc.rem.116"),
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
                    plugins: this.pP3 = new Wtf.common.pPageSize({})
                }),
        items:this.grid}]
});


    this.gridGroupStore.on("datachanged",function() {
            var p = this.pP3.combo.value;
//            this.quickPanelSearch.setPage(p);
//            this.quickPanelSearch.StorageChanged(this.gridGroupStore);
        }, this);
    this.gridGroupStore.on("load",function(store){
            var p = this.pP3.combo.value;
//            this.quickPanelSearch.setPage(p);
//            this.quickPanelSearch.StorageChanged(store);
            this.loadMask1.hide();
    },this);

     this.gridGroupStore.on('loadexception',function(){
                   this.loadMask1.hide();
     },this);

     Wtf.getCmp('prereq'+this.id).on("render",function(){
            this.loadMask1 = new Wtf.LoadMask(Wtf.getCmp('prereq'+this.id).el.dom, Wtf.apply(this.loadMask1));
            this.loadMask1.show();
        },this);

this.on('render',this.handleRender,this);
this.on('show',this.handleshow,this);
//this.courseCmb.on('render', this.courseCmbRender, this);
this.grid.on("rowclick", this.rowClickHandle, this);
//this.gridGroupStore.on("load",this.handleStoreLoad,this);
this.fieldTypeCombo.on("select",this.ruleTypeSelect,this);
}

Wtf.extend(Wtf.prereq, Wtf.Panel, {
  handleshow:function(){
       this.range1.disable();
//       this.daterangepanel.hide();
//       this.dateminmax.hide();
//       this.datevalue.hide();
       this.ruleValue.enable();
       this.items.items[0].ownerCt.doLayout();
  },

    ruleTypeSelect:function(combo,rec,ind){
        if(this.fieldTypeCombo.getValue() == '1'){// && !this.dateflag && !this.courseflag ) {
            this.range1.disable();
            this.ruleValue.enable();
            this.doLayout();
        }
        else if(this.fieldTypeCombo.getValue() == '2'){// && !this.dateflag && !this.courseflag) {
            this.ruleValue.disable();
            this.range1.enable();
            this.doLayout();
        }else if(this.fieldTypeCombo.getValue() == '3'){
            this.ruleValue.enable();
            this.range1.enable();
            this.doLayout();
        }
    },

        typeRenderer : function(value, css, record, row, column, store){
            if(value == "1") {
                return "Amount";
            } else if(value == "2") {
                return "Product";
            } else  if(value == "3") {
                return "Discount";
            }
        },
        
        valueRenderer : function(value, css, record, row, column, store){
            if(record.data.fieldType == "1") {
                return value;
            } else if(record.data.fieldType == "2") {
                return record.data.productname; //"Product Discount";
            } else  if(record.data.fieldType == "3") {
               return record.data.productname;
            }
        },
        
        transactionTypeRenderer : function(value, css, record, row, column, store){
            if(value == "1") {
                return "Purchase Order";
                
            } else if(value == "2") {
                return "Sales Order"; 
            
            } else if(value == "3") {
                return "Purchase Invoice"; 
            
            } else if(value == "4") {
                return "Sales Invoice"; 
            }
        },
        

    handleRender:function(panelObj) {
        this.range1.disable();
//        this.gridGroupStore.baseParams = {
//            type: 103,
//            courseid:
//        };
//        this.loadStores();
        this.gridGroupStore.load({
            params:{
                start:0,
                limit:15
            }
        });
    },

    loadStores: function() {
        this.programStore.load({
            params:{
                type:'allmodules',
                flag:/*(Wtf.realroles.indexOf('46')>-1)?true:*/false
            }
        });

         this.programStore1.load({
            params:{
                type:'allmodules',
                flag:false
            }
        });

        this.attributeStore.load({
            params:{
                flag:72,
                type:'agent'
            }
        });
    },

    courseCmbRender: function(cmb) {
        cmb.getEl().dom.parentNode.parentNode.parentNode.style.display = "none";
        cmb.removeListener('render', this.courseCmbRender, this);
    },

    rowClickHandle:function(grid, rowIndex, e){
        var rec=this.gridGroupStore.getAt(rowIndex);
        var fieldTypeValue = rec.data.fieldType;
        
        if(fieldTypeValue=='1'){
           this.ruleValue.setValue(rec.data.value);
//           this.ruleValue.enable();
           this.ruleValue.enable();
           this.range1.disable();
           this.range1.setValue("");
           this.range1.clearInvalid();
        }
        
        if(fieldTypeValue=='2'){
            this.range1.setValue(rec.data.value);
            this.range1.enable();
            this.ruleValue.disable();
            this.ruleValue.setValue("");
            this.ruleValue.clearInvalid();
            
        }
        if(fieldTypeValue=='3'){
            this.ruleValue.setValue(rec.data.discountamount);
            this.range1.setValue(rec.data.value);
            this.ruleValue.enable();
            this.range1.enable();
//            this.range1.clearInvalid();
        }
        this.transactionType.setValue(rec.data.transactionType);
        this.ruleName.setValue(rec.data.rulename);
        this.fieldTypeCombo.setValue(fieldTypeValue);
        this.ruleId.setValue(rec.data.id);
        this.approvalLevelCombo.setValue(rec.data.approvallevel);
        
        this.fieldTypeCombo.disable();
        this.transactionType.disable();
        
        this.DeleteBttn.enable();
        this.EditBttn.enable();
//        this.ruleName.enable();
        this.newSuBttn.disable();
        },

        clickHandle:function(){
                var  fieldTypeCombo = this.fieldTypeCombo.getValue();
                var ruleValue;
                var disValue;
                if(fieldTypeCombo == '1'){
                    ruleValue = this.ruleValue.getValue();
                } else if(fieldTypeCombo == '2'){
                    ruleValue = this.range1.getValue();
                }else if(fieldTypeCombo == '3'){
                    ruleValue = this.range1.getValue();
                     disValue = this.ruleValue.getValue();
                }
                
                if(fieldTypeCombo == '2' || fieldTypeCombo == '3'){// in case of Product Selection
                    var isInvalidProductsSelected = WtfGlobal.isInvalidProductsSelected(this.range1);
                    if(isInvalidProductsSelected){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"),WtfGlobal.getLocaleText("acc.select.validproduct")],3);
                        return;
                    }
                }
                
                if(fieldTypeCombo == "" || this.transactionType.getValue() == "" || this.ruleName.getValue() == "" || ruleValue == ""){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.tar.required")], 3);//Please fill required fields first.
                    return;
                }
                Wtf.Ajax.requestEx({
                url:"CostCenter/saveApprovalRules.do",
                params: { 
                    rulename: this.ruleName.getValue(),
                    trasactiontype: this.transactionType.getValue(),
                    fieldtype: fieldTypeCombo,
                    approvallevel : this.approvalLevelCombo.getValue(),
                    value: ruleValue,
                    discountamount: disValue,
                    companyid:companyid,
                    documenttype:this.transactionType.lastSelectionText
                    //                    value3:value3,
                    //                    ruleid:this.ruleid
                }
            },this,
            function(resp){
                if(resp.success == true) {
                    
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), resp.msg], 0);
                    this.NewRule();
                    
                    this.gridGroupStore.baseParams = {
                            companyid:companyid
                        };

                        this.gridGroupStore.load({
                            params:{
                                start:0,
                                limit:15
                            }
                        });                        

            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"), WtfGlobal.getLocaleText("acc.field.Approvalmessagenotsavesucessfully")], 1);
            }
            
        },function(){
        });

    },


        NewRule :function() {
            this.ruleid = "";
            this.grid.getSelectionModel().clearSelections();

            this.fieldTypeCombo.enable();
            this.ruleName.enable();
            this.ruleValue.enable();
            this.transactionType.enable();
            this.fieldTypeCombo.setValue("");
//            this.fieldTypeCombo.fireEvent('select');
            this.ruleName.setValue("");
            this.transactionType.setValue("");
            this.ruleName.setValue("");
            this.range1.setValue("");
            this.ruleValue.setValue("");
            this.ruleValue.clearInvalid();
//            this.range2.setValue("");
            this.ruleName.clearInvalid();
            this.ruleValue.clearInvalid();
            this.fieldTypeCombo.clearInvalid();
            this.transactionType.clearInvalid();
            this.DeleteBttn.disable();
            this.EditBttn.disable();
            this.NewRuleBttn.enable();
            this.newSuBttn.enable();
            this.range1.clearInvalid();
            this.approvalLevelCombo.setValue(1);
        },

        handleStoreLoad:function(store,rec,opt){
            this.quickPanelSearch.StorageChanged(store);
        },

        deleteMessage: function(obj, e){
            Wtf.Msg.show({
                title:WtfGlobal.getLocaleText("acc.field.DeleteRule"),
                msg: WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttodeleteselectedrule"),
                buttons: Wtf.Msg.YESNO,
                fn: this.confirmDelete,
                scope:this,
                animEl: 'elId',
                icon: Wtf.MessageBox.QUESTION
            });
        },

        confirmDelete:function(btn, text){
            var delid = "";
            if(btn=="yes" && this.grid.getSelections().length>0) {
                var rec = this.grid.getSelectionModel().getSelected();
                Wtf.Ajax.requestEx({
                    url:'CostCenter/deleteApprovalRules.do',
                    params:{
                        ruleid : rec.data.id,
                        companyid:companyid,
                        documenttype:this.transactionType.lastSelectionText,
                        approvallevel : rec.data.approvallevel,
                        rulename : rec.data.rulename
                        
//                        type : "136"
                    },
                    method:'POST'},
                this,
                function(resp){
//                    var respobj = eval('('+resp+')');
                    if(resp.success){
//                        if(respobj.success){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.field.Documentapprovalrulehasbeendeletedsuccessfully")],2+1);
                            this.gridGroupStore.baseParams = {
                                companyid:companyid
                            };
                            this.NewRule();
                            
                            this.gridGroupStore.load({
                                params:{
                                    start:0,
                                    limit:15
                                }
                            });
                            
                            this.NewRule();
                         
                    } else if(!respobj.success && respobj.msg != null) {
                        msgBoxShow([WtfGlobal.getLocaleText("acc.common.error"), respobj.msg], Wtf.MessageBox.ERROR);
                }
            },function(resp,req){
        })
        this.DeleteBttn.disable();
        this.EditBttn.disable();
    }
},
       editRule : function(obj, e){
           if(this.grid.getSelections().length>0){
               var rec = this.grid.getSelectionModel().getSelected();
           }
           var  fieldTypeCombo = this.fieldTypeCombo.getValue();
            var ruleValue;
            var disValue;
            if(fieldTypeCombo == '1'){
                ruleValue = this.ruleValue.getValue();
            } else if(fieldTypeCombo == '2'){
                ruleValue = this.range1.getValue();
            }else if(fieldTypeCombo == '3'){
                    ruleValue = this.range1.getValue();
                     disValue = this.ruleValue.getValue();
                }
                
           Wtf.Ajax.requestEx({
                    url:"CostCenter/editApprovalRule.do",
                    params:{
                        ruleid : this.ruleId.getValue(),
                        rulename: this.ruleName.getValue(),
                        trasactiontype: this.transactionType.getValue(),
                        fieldtype: fieldTypeCombo,
                        approvallevel : this.approvalLevelCombo.getValue(),
                        value: ruleValue,
                        discountamount: disValue,
                        documenttype:this.transactionType.lastSelectionText
//                        type : "136"
                    }},
                this,
                function(resp){
                    if(resp.success){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Success"), resp.msg], 0);                        
                        this.NewRule();
                        
                        this.gridGroupStore.baseParams = {
                                companyid:companyid
                         };
                            
                        this.gridGroupStore.load({
                                    params:{
                                        start:0,
                                        limit:15
                                    }
                         });
                    }                     
                })
       }
        
});
