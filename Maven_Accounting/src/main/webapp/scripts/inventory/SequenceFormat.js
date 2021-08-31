/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

function openMSSequenceFormatTab(){
    openSequenceFormatTab()
}
function openSequenceFormatTab(){
    var main = Wtf.getCmp("masterdatagrid");
    var sequenceFormatTab=Wtf.getCmp("SequenceFormatTabId");
    if(sequenceFormatTab==null){
        sequenceFormatTab = new Wtf.SequenceFormatGrid({
            title : "Sequence Management",
            id: "SequenceFormatTabId",
            layout: "fit",
            closable:true
        });
        main.add(sequenceFormatTab);
    }
    main.setActiveTab(sequenceFormatTab);
    Wtf.getCmp("masterdatagrid").doLayout();
//openDefaultFormatSetupWin();
}

function openDefaultFormatSetupWin(){
    var win = Wtf.getCmp(this.id + "DeafutSeqFormatWizId");
    if(win == null || win == undefined){
        win = new Wtf.DefaultSeqFormatWiz({
            title:'Set Default Sequence Format',
            height:500,
            width: 450,
            layout:'fit',
            border:false,
            modal:true,
            id:this.id + "DeafutSeqFormatWizId"
        });
    }
    win.show();
}

Wtf.SequenceFormatGrid = function(config){
    this.compPrefs=config.compPref;
    this.moduleId=config.moduleId;
    this.moduleName=config.moduleName;
    Wtf.SequenceFormatGrid.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.SequenceFormatGrid, Wtf.Panel, {
    initComponent: function() {
        Wtf.SequenceFormatGrid.superclass.initComponent.call(this);
        if(this.forMS == true){
            this.forMS = true;
        }else{
            this.forMS = false;
        }
    },
    onRender: function(config) {
        Wtf.SequenceFormatGrid.superclass.onRender.call(this, config);

        this.record = Wtf.data.Record.create([
        {
            name:"moduleId"
        },{
            name:"moduleName"
        },{
            name:"seqFormatId"
        },{
            name:"prefix"
        },{
            name:"suffix"
        },{
            name:"prefixDateFormat"
        },{
            name:"suffixDateFormat"
        },{
            name:"separator"
        },{
            name:"numberOfDigits"
        },{
            name:"startFrom"
        },{
            name:"leadingZero"
        },{
            name:"formatedNumber"
        },{
            name:"isActive"
        },{
            name:"isDefault"
        },{
            name:"delete"
        }
        ]);
        this.ds = new Wtf.data.GroupingStore({
            baseParams: {
                moduleId: this.moduleId,
                moduleName:this.moduleName
            },
            url:"INVSeq/getSeqFormats.do",
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },this.record),
            sortInfo:{
                field: 'moduleName', 
                direction: "ASC"
            }
//            groupField:['moduleName']
        });

        this.ds.load({
            params:{
                start:0,
                limit:30//Wtf.companyPref.recperpage
            }
        });
        this.ds.on("load",function(){
            },this);
        
        this.sm = new Wtf.grid.RowSelectionModel({
            width:25,
            singleSelect:true
        });
        var cmDefaultWidth = 120;
        this.cmConfig = [new Wtf.KWLRowNumberer(),{
            dataIndex: 'moduleId',
            hidden:true
        },{
            header: "Module Name",
            dataIndex: 'moduleName',
            width:cmDefaultWidth
        },{
            header: "Sequence Format",
            dataIndex: 'formatedNumber',
            width:cmDefaultWidth
        },{
            header: "Prefix",
            dataIndex: 'prefix',
            width:cmDefaultWidth
        },{
            header: "Prefix Date Format",
            dataIndex: 'prefixDateFormat',
            width:cmDefaultWidth
        },{
            header: "Suffix Date Format",
            dataIndex: 'suffixDateFormat',
            width:cmDefaultWidth
        },{
            header: "Suffix",
            dataIndex: 'suffix',
            width:cmDefaultWidth
        },{
            header: "Separator",
            dataIndex: 'separator',
            width:cmDefaultWidth
        },{
            header: "No. of Digits",
            dataIndex: 'numberOfDigits',
            width:cmDefaultWidth
        },{
            header: "Sequence Start From",
            dataIndex: 'startFrom',
            width:cmDefaultWidth
        },{
            header: "Leading with Zero",
            dataIndex: 'leadingZero',
            hidden:true,
            renderer:function(v){
                if(v == undefined ||v ==""){
                    return "";
                }
                
                if(v == true){
                    return "Yes"; 
                }else{
                    return "No"; 
                }
            }
        },{
            header: "Using Default",
            dataIndex: 'isDefault',
            width:cmDefaultWidth,
            renderer:function(v){
                if(v == undefined ||v ==""){
                    return "";
                }
                
                if(v == true){
                    return "Yes"; 
                }else{
                    return ""; 
                }
            }
        },{
            header: "Status",
            dataIndex: 'isActive',
            width:cmDefaultWidth,
            renderer:function(v){
                if(v == true){
                    return "<span style='color:green;'>Active</span>"; 
                }else{
                    return "<span style='color:red;'>Deactive</span>"; 
                }
            }
            }, {
                header: WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
                dataIndex: 'delete',
                align: 'center',
                renderer: function() {
                    return "<div class='pwnd delete-gridrow' > </div>";
                }
        }];

        this.cm = new Wtf.grid.ColumnModel(this.cmConfig);
        
        this.sequenceMasterRecord = Wtf.data.Record.create([{
            name:"id"
        },{
            name:"name"
        }]);
    
        this.sequenceMasterStore = new Wtf.data.Store({
            baseParams: {
            //                action: 2,
            //                forMS : this.forMS
                moduleId: this.moduleId,
                moduleName:this.moduleName
            },
            url:"INVSeq/getSeqModules.do",
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.sequenceMasterRecord)
        });
        this.sequenceMasterStore.on('load',function(){
            if(this.sequenceMasterStore.getCount() > 0){
                var staticData = new this.sequenceMasterRecord({
                    id:'', 
                    name:"ALL"
                })
                this.sequenceMasterStore.insert(0,staticData);
                this.sequenceMasterCombo.setValue('');
            }    
        },this);
        this.sequenceMasterStore.load();
        
        this.sequenceMasterCombo = new Wtf.form.ComboBox({
            mode: 'local',
            triggerAction: 'all',
            typeAhead: true,
            width:200,
            allowBlank:false,
            store: this.sequenceMasterStore,
            displayField: 'name',
            valueField:'id',
            msgTarget: 'side',
            emptyText:"Select Sequence Module"
        });
        
        this.sequenceMasterCombo.on('select', function(){
            this.ds.load({
                params:{
                    moduleId : this.sequenceMasterCombo.getValue(),
                    start:0,
                    limit:30//Wtf.companyPref.recperpage
                }
            })
        }, this);
        
        this.addButton= new Wtf.Button({
            text:'Add New Sequence Format',
            iconCls:getButtonIconCls(Wtf.etype.add),
            tooltip:'Click to Add New Sequence Format',
            handler: this.addSequenceFormatHandler,
            scope:this
        });
        this.markDefaultButton= new Wtf.Button({
            text:'Mark Default',
            tooltip:'Click to Mark Default Sequence Format',
            handler: this.markAsDefaultHandler,
            iconCls :getButtonIconCls(Wtf.etype.edit),
            scope:this,
            disabled: true
        });
        this.activateButton= new Wtf.Button({
            text:'Activate',
            tooltip:'Click to Activate Sequence Format',
            handler: function(){
                this.activateDeactivateHandler(false)
            },
            iconCls:getButtonIconCls(Wtf.etype.activate),
            scope:this,
            disabled: true
        });
        this.deactivateButton= new Wtf.Button({
            text:'Deactivate',
            tooltip:'Click to Deactivate Sequence Format',
            handler: function(){
                this.activateDeactivateHandler(true)
            },
            scope:this,
            iconCls:getButtonIconCls(Wtf.etype.deactivate),
            disabled: true
        });
        
        this.tbarArray = new Array()
        // if(checktabperms(14, 10) == "edit"){
        if(!this.compPrefs){
        this.tbarArray.push('Module: ',this.sequenceMasterCombo)
        this.tbarArray.push("-")
//            this.tbarArray.push(this.addButton)
        }
        var bbarArray = new Array()
        //   if(checktabperms(14, 10) == "edit"){
        bbarArray.push('-',this.markDefaultButton)
        bbarArray.push('-',this.activateButton)
        bbarArray.push('-',this.deactivateButton)
        // }
//        this.pagingToolbar = new Wtf.PagingToolbar({
//            pageSize:30,// Wtf.companyPref.recperpage,
//            id: "pagingtoolbar" + this.id,
//            store: this.ds,
//            displayInfo: true,
//            displayMsg: 'Displaying {0} - {1} of {2}',
//            emptyMsg: "No results to display",
//            plugins: this.pP = new Wtf.common.pPageSize({
//                id : "pPageSize_"+this.id
//            }),
//            items:bbarArray
//        });
        
        this.sequenceFormatGrid = new Wtf.grid.GridPanel({
            cm:this.cm,
            sm:this.sm,
            store:this.ds,
            tbar:this.tbarArray,
//            bbar:this.pagingToolbar,
            bbar:bbarArray
//            view: new Wtf.grid.GroupingView({
//                forceFit: false,
//                emptyText:"<center>No data to display</center>",
//                hideGroupedColumn: false,
//                groupTextTpl: '{text}'
//            })
        });
        
        this.add(this.sequenceFormatGrid);
        this.sequenceFormatGrid.on('render',function(){
            this.sequenceFormatGrid.doLayout();
        },this);
        
        this.sm.on("selectionchange", this.rowSelectionHandler,this);
        
        this.sequenceFormatGrid.on("cellclick", this.deleteSequence, this);        
        
    },
    
    rowSelectionHandler: function(){
        var count = this.sequenceFormatGrid.selModel.getCount();
        this.markDefaultButton.setDisabled(true);
        this.activateButton.setDisabled(true);
        this.deactivateButton.setDisabled(true);
        
        if(count == 1){
            var rec = this.sequenceFormatGrid.selModel.getSelected();
            var isActive = rec.get('isActive');
            var isDefault = rec.get('isDefault')
            if(isActive == true && isDefault != true){
                this.markDefaultButton.setDisabled(false);
            }
            if(isActive == true){
                this.deactivateButton.setDisabled(false);
            }else if(isActive != true){
                this.activateButton.setDisabled(false);
            }
        }
        
    },
    
    addSequenceFormatHandler : function(){
        this.showSequenceFormatForm("Add");
    },
    markAsDefaultHandler : function(){
        if(this.sequenceFormatGrid.selModel.getCount()==1){
            var rec = this.sequenceFormatGrid.selModel.getSelected();
            if(rec.get('isDefault') != true){
                var seqFormatId = rec.get("seqFormatId");
                this.seqFormatMarkAsDefault(seqFormatId);
            }else{
                Wtf.Msg.alert('Alert', 'Sequence format is already set as default sequence format ', 1)
            }
            
        }else{
            Wtf.Msg.alert('Alert', 'Please select a single record', 1)
        }
    },
    activateDeactivateHandler : function(deactivate){
        if(this.sequenceFormatGrid.selModel.getCount()==1){
            var rec = this.sequenceFormatGrid.selModel.getSelected();
            if(rec.get("isDefault") == true){
                Wtf.Msg.alert('Alert', 'Selected sequence format is the as default sequence format for the selected module. Please set another default sequence format for same module to proceed. ', 1);
                return;
            }else{
                var seqFormatId = rec.get("seqFormatId");
                this.activateDeactivateSeqFormat(seqFormatId, deactivate);
            }
        }else{
            Wtf.Msg.alert('Alert', 'Please select a single record', 1)
        }
    },
    seqFormatMarkAsDefault : function(seqFormatId){
        Wtf.MessageBox.confirm('Confirm', 'Selected sequence format will be set as default sequence format from now onwards for the selected module.<br> Are you sure you want to continue?', function(obj){
            if(obj == "yes"){
                Wtf.Ajax.requestEx({
                    url: "INVSeq/markSequenceFormatAsDefault.do",
                    params:{
                        //                        action:4,
                        seqFormatId: seqFormatId
                    }
                },
                this,
                function(result, response){
                    //var obj = eval('('+result+')');
                    var msg = result.msg;
                    if(!msg){
                        msg = "Default sequence format is set successfully for the selected module."
                    }
                    Wtf.MessageBox.show({
                        title:"Status",
                        msg:msg,
                        icon:Wtf.MessageBox.INFO,
                        buttons:Wtf.MessageBox.OK
                    });
                    if(this.ds!=null){
                        this.ds.reload();
                    }
                },
                function(){
                    Wtf.MessageBox.show({
                        title:"Status",
                        msg:"Error occurred while changing status of sequence format.",
                        icon:Wtf.MessageBox.ERROR,
                        buttons:Wtf.MessageBox.OK
                    });
                }
                );
            }
        }, this)
    },
    
    activateDeactivateSeqFormat : function(seqFormatId, deactivate){
        var actionText = "activate";
        if(deactivate == true){
            actionText = "deactivate";
        }
        Wtf.MessageBox.confirm('Confirm', 'Are you sure want to '+actionText+' the selected sequence format?', function(obj){
            if(obj == "yes"){
                Wtf.Ajax.requestEx({
                    url: "INVSeq/activateDeactivateSequenceFormat.do",
                    params:{
                        action:5,
                        seqFormatId: seqFormatId,
                        deactivate: deactivate
                    }
                },
                this,
                function(result, response){
                    //var obj = eval('('+result+')');
                    var msg = result.msg;
                    if(!msg){
                        msg = "Sequence format is "+actionText+"d successfully."
                    }
                    Wtf.MessageBox.show({
                        title:"Status",
                        msg:msg,
                        icon:Wtf.MessageBox.INFO,
                        buttons:Wtf.MessageBox.OK
                    });
                    if(this.ds!=null){
                        this.ds.reload();
                    }
                },
                function(){
                    Wtf.MessageBox.show({
                        title:"Status",
                        msg:"Error occurred while changing status of sequence format.",
                        icon:Wtf.MessageBox.ERROR,
                        buttons:Wtf.MessageBox.OK
                    });
                }
                );
            }
        }, this)
    },
    showSequenceFormatForm:function(action){
        new Wtf.SequenceFormatForm({
            //            id: "SequenceFormatFormId",
            border : false,
            title : "Add Sequence Format",
            layout : 'fit',
            closable: true,
            width:400,
            height:450,
            forMS: this.forMS,
            modal:true,
            store: this.ds,
            action:action,
            resizable:false
        }).show();
    },
    deleteSequence: function(gd, ri, ci, e) {
        var event = e;
        if (event.target.className == "pwnd delete-gridrow") {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttodeleteselectedsequenceformat"), function(btn) {
                if (btn == "yes") {
                    var isdefaultformat = gd.getStore().getAt(ri).data.isDefault;
                    var isActive = gd.getStore().getAt(ri).data.isActive;
                    if (isdefaultformat === true) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.field.defaultField")], 0);
                        return;
    }
                    if (isActive === true) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.field.activeField")], 0);
                        return;
                    }
                    var sequenceformat = gd.getStore().getAt(ri).data.formatedNumber;
                    var moduleId = gd.getStore().getAt(ri).data.moduleId;
                    var id = gd.getStore().getAt(ri).data.seqFormatId;
                    var prefix = gd.getStore().getAt(ri).data.prefix;
                    var prefixDateFormat = gd.getStore().getAt(ri).data.prefixDateFormat;
                    var suffix = gd.getStore().getAt(ri).data.suffix;
                    var suffixDateFormat = gd.getStore().getAt(ri).data.suffixDateFormat;
                    Wtf.Ajax.requestEx({
                        url: "INVSeq/deleteInvSequenceFormat.do",
                        params: {
                            sequenceformat: sequenceformat,
                            id: id,
                            moduleId: moduleId,
                            prefix: prefix,
                            prefixDateFormat: prefixDateFormat,
                            suffix: suffix,
                            suffixDateFormat: suffixDateFormat
                        }
                    }, this,
                            function(req, res) {
                                var restext = req;
                                if (restext.success) {
                                    this.ds.remove(this.ds.getAt(ri));
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.sequence.format.delete")], 0);
                                    this.ds.reload();
                                } else if (restext.msg == undefined && restext.msg == "") {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.sequence.format.deletefailure")], 1);
                                }
                                else {
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), restext.msg], 1);
                                }

                            },
                            function(req) {
                                var restext = req;
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.companypreferences.cannotDeleteSequenceFormat")], 1);
});
                }
            }, this)
        }
    }
});

Wtf.SequenceFormatForm = function (config){
    this.compPref = config.compPref;
    this.module=config.moduleId;
    this.moduleName=config.moduleName;
    this.parentCmp=config.parentCmp;
    Wtf.apply(this,config);
    Wtf.SequenceFormatForm.superclass.constructor.call(this,{
        buttons:[
        {
            text:"Save",
            handler:function (){
                this.saveSequenceFormat();
            },
            scope:this
        },
        {
            text:"Cancel",
            handler:function (){
                this.close();
            },
            scope:this
        }
        ]
    });
}

Wtf.extend(Wtf.SequenceFormatForm,Wtf.Window,{
    initComponent:function (){
        Wtf.SequenceFormatForm.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        this.GetSouthPanel();
        
        this.mainPanel = new Wtf.Panel({
            layout:"border",
            items:[
            this.northPanel,
            this.AddSequenceFormatForm,
            this.southPanel
            ]
        });

        this.add(this.mainPanel);
    },
    GetNorthPanel:function (){
        var wintitle = 'Add Sequence Format';
        var windetail='';
        var image='';
        windetail='Fill the details to add Sequence Format';
        image='images/project.gif';
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:150,
            border:false,
            layout:'fit',
            //            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            //            html:getTopHtml(wintitle,windetail,image),
            items:[new Wtf.SequenceFormatGrid({
                layout:'fit',
                compPref:this.compPref,
                moduleId:this.module,
                moduleName:this.moduleName
            })]
        });
    },
    GetSouthPanel:function (){
        this.southPanel = new Wtf.Panel({
            region:"south",
            height:20,
            border:false,
            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            html: ""
        });
    },
    
    GetAddEditForm:function (){

        this.sequenceMasterRecord = Wtf.data.Record.create([{
            name:"id"
        },{
            name:"name"
        }]);
    
        this.sequenceMasterStore = new Wtf.data.Store({
            baseParams: {
            //    action: 2,
                //  forMS : this.forMS,
                moduleId:this.module,
                moduleName:this.moduleName
            },
            url:"INVSeq/getSeqModules.do",
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.sequenceMasterRecord)
        });
        
        this.sequenceMasterCombo = new Wtf.form.ComboBox({
            mode: 'local',
            triggerAction: 'all',
            hiddenName:"moduleId",
            fieldLabel : 'Sequence Module',
            typeAhead: true,
            width:200,
            allowBlank:false,
            store: this.sequenceMasterStore,
            displayField: 'name',
            valueField:'id',
            msgTarget: 'side',
            emptyText:"Select Sequence Module"
        });

        this.sequenceMasterStore.on('load',function(s){
            if(s.getCount() > 0){
                this.sequenceMasterCombo.setValue(s.getAt(0).get('id'))
            }
        },this);
        this.sequenceMasterStore.load();
        
        this.prefix = new Wtf.form.TextField({
            fieldLabel: "Prefix",
            name:'prefix',
            maxLength:10,
            width:200
        });
        
        this.suffix = new Wtf.form.TextField({
            fieldLabel: "Suffix",
            name:'suffix',
            maxLength:10,
            width:200
        });
        this.numberOfDigits = new Wtf.form.NumberField({
            fieldLabel: "Number Of Digits*",
            name:'numberOfDigits',
            width:200,
            minValue:1,
            maxValue:10,
            allowBlank:false
        });
        this.startFrom = new Wtf.form.NumberField({
            fieldLabel: "Start From*",
            name:'startFrom',
            width:200,
            maxLength:10,
            minValue : 0,
            allowBlank:false
        });
        
        this.leadingZero = new Wtf.form.Checkbox({
            id: 'leadingZero'+this.id,
            bodyStyle: "padding-left: 105px; margin-top:4px;",
            border: false,
            fieldLabel:"Leading with Zero",
            checked : true
        });
        var separatorStore = new Wtf.data.SimpleStore({
            fields:['id','value'],
            data:[['','None'],['-','-'],['#','#']]
        })
        var today = new Date();
        var dateFormatStore = new Wtf.data.SimpleStore({
            fields:['id','value'],
            data:[
            ['','None'],
            ['YYYY',"YYYY ("+today.format('Y')+")"],
            ['YYYYMM', "YYYYMM ("+today.format('Ym')+")"], 
            ['YYYYMMDD', "YYYYMMDD ("+today.format('Ymd')+")"]
            ]
        })
        this.separator = new Wtf.form.ComboBox({
            mode: 'local',
            triggerAction: 'all',
            hiddenName:"separator",
            fieldLabel : 'Separator',
            typeAhead: true,
            width:200,
            store: separatorStore,
            displayField: 'value',
            valueField:'id',
            msgTarget: 'side',
            value: ''
        });
        this.prefixDateFormat = new Wtf.form.ComboBox({
            mode: 'local',
            triggerAction: 'all',
            hiddenName:"prefixDateFormat",
            fieldLabel : 'Prefix Date Format',
            typeAhead: true,
            width:200,
            store: dateFormatStore,
            displayField: 'value',
            valueField:'id',
            msgTarget: 'side',
            value: ''
        });
        this.suffixDateFormat = new Wtf.form.ComboBox({
            mode: 'local',
            triggerAction: 'all',
            hiddenName:"suffixDateFormat",
            fieldLabel : 'Suffix Date Format',
            typeAhead: true,
            width:200,
            store: dateFormatStore,
            displayField: 'value',
            valueField:'id',
            msgTarget: 'side',
            value: ''
        });
        
        this.AddSequenceFormatForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            iconCls:'win',
            bodyStyle:"background-color:#f1f1f1;padding:15px",
            url:"INVSeq/addSeqFormat.do",
            labelWidth:130,
            items:[
            this.sequenceMasterCombo,
            this.prefix,
            this.prefixDateFormat,
            this.suffix,
            this.suffixDateFormat,
            this.separator,
            this.numberOfDigits,
            this.startFrom
            //                this.leadingZero
            ]
        });
        this.numberOfDigits.on('change', function(form,nv,ov){
            if(nv != '' || nv != null){
                var max = Math.pow(10, nv) - 1;
                this.startFrom.maxValue = max;
            }
            this.refreshPreview();
        }, this);
        this.prefix.on('change', this.refreshPreview, this);
        this.prefixDateFormat.on('change', this.refreshPreview, this);
        this.suffix.on('change', this.refreshPreview, this);
        this.suffixDateFormat.on('change', this.refreshPreview, this);
        this.startFrom.on('change', this.refreshPreview, this);
        this.separator.on('change', this.refreshPreview, this);
        this.leadingZero.on('change', this.refreshPreview, this);
        
    },
    refreshPreview: function(){
        //        if(this.sequenceMasterCombo.getValue() != "" && this.numberOfDigits.getValue() > 0){
        Wtf.Ajax.requestEx({
            url: "INVSeq/previewSeqFormat.do",
            params:{
                moduleId : this.sequenceMasterCombo.getValue(),
                prefix :this.prefix.getValue(),
                prefixDateFormat:this.prefixDateFormat.getValue(),
                suffix:this.suffix.getValue(),
                suffixDateFormat : this.suffixDateFormat.getValue(),
                separator: this.separator.getValue(),
                numberOfDigits: this.numberOfDigits.getValue(),
                startFrom : this.startFrom.getValue(),
                leadingZero : true
            }
        },
        this,
        function(result, response){
            var text = "<div style='text-align:center; font-size:15px; font-weight: bolder'>"+result.msg+"</div>"
            this.southPanel.getEl().update(text);
        },
        function(){
            //exception
            })
    //        }
    },
    saveSequenceFormat:function (){
        this.saveLoadMask = new Wtf.LoadMask(document.body,{
            msg:'Saving...'
        });
        if(this.AddSequenceFormatForm.form.isValid()){
            this.saveLoadMask.show();
            this.AddSequenceFormatForm.form.submit({
                params:{
                    // action:3,
                    leadingZero: true//this.leadingZero.getValue()
                },
                success:function (response,request){
                    this.saveLoadMask.hide();
                    var data = request.result.data
                    if(data.success){
                        Wtf.MessageBox.show({
                            title:"Status",
                            msg:"Sequence format is saved successfully.",
                            icon:Wtf.MessageBox.INFO,
                            buttons:Wtf.MessageBox.OK
                        });
                        this.parentCmp.getAllInventorySequFormat();
                        
                        if(this.store!=null){
                            this.store.reload();
                        }
                        this.close();
                    }else{
                        var msg = data.msg;
                        if(!msg){
                            msg = "Error occurred while saving sequence format details."
                        }
                        Wtf.MessageBox.show({
                            title:"Status",
                            msg:msg,
                            icon:Wtf.MessageBox.ERROR,
                            buttons:Wtf.MessageBox.OK
                        });
                    }
                },
                failure:function (response,request){
                    this.saveLoadMask.hide();
                    var msg = request.result.msg;
                    if(!msg){
                        msg = "Error occurred while saving sequence format details."
                    }
                    Wtf.MessageBox.show({
                        title:"Status",
                        msg:msg,
                        icon:Wtf.MessageBox.ERROR,
                        buttons:Wtf.MessageBox.OK
                    });
                //                    this.close();
                },                
                scope:this
            });
        }
    }
});

////////////////////////////////// Set Default Format Form//////////////


Wtf.DefaultSeqFormatWiz = function (config){
    Wtf.apply(this,config);
    Wtf.DefaultSeqFormatWiz.superclass.constructor.call(this,{
        buttons:[
        {
            text:"Set",
            handler:function (){
                this.saveSequenceFormat();
            },
            scope:this
        },
        {
            text:"Skip",
            handler:function (){
                this.close();
            },
            scope:this
        }
        ]
    });
}

Wtf.extend(Wtf.DefaultSeqFormatWiz,Wtf.Window,{
    initComponent:function (){
        Wtf.DefaultSeqFormatWiz.superclass.initComponent.call(this);
    },
    onRender: function(config) {
        Wtf.DefaultSeqFormatWiz.superclass.onRender.call(this, config);
        this.GetNorthPanel();
        this.GetCenterPanel();
        
        this.mainPanel = new Wtf.Panel({
            layout:"border",
            items:[
            this.northPanel,
            this.smgrid
            ]
        });

        this.add(this.mainPanel);
    },
    GetNorthPanel:function (){
        var wintitle = 'Set Default Sequence Format';
        var windetail='';
        var image='';
        windetail='Following Sequence modules have not any default sequence format. Please set default sequence format for these modules';
        image='images/project.gif';
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:85,
            border:false,
            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(wintitle,windetail,image)
        });
    },
    GetCenterPanel:function (){
        
        this.formatComboEditor = new Wtf.SeqFormatCombo();
        
        var smrecord = Wtf.data.Record.create([
        {
            name:"moduleId"
        },{
            name:"moduleName"
        },{
            name:"seqFormatId"
        }
        ]);
        var smstore = new Wtf.data.Store({
            baseParams: {
                action: 8
            },
            url:"SeqController",
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },smrecord),
            pruneModifiedRecords:true
        });
        
        
        var cmConfig = [
        {
            header: "Module Name",
            dataIndex: 'moduleName'
        },{
            header: "Sequence Format",
            dataIndex: 'seqFormatId',
            editor: this.formatComboEditor,
            renderer: WtfGlobal.getComboRenderer(this.formatComboEditor)
        }];

        var cm = new Wtf.grid.ColumnModel(cmConfig);
        this.smgrid=new Wtf.grid.EditorGridPanel({
            region:'center',
            layout:'fit',
            cm:cm,
            store:smstore,
            border:false,
            loadMask : true,
            clicksToEdit: 1,
            viewConfig:{
                forceFit:false
            }
        });
        this.smgrid.on("cellclick", this.gridCellClick, this);
        smstore.load();
    },
    
    gridCellClick:function(grid, rowIndex, columnIndex, e){
        var fieldname = grid.getColumnModel().getDataIndex(columnIndex);
        if(fieldname == "seqFormatId"){
            var rec = grid.getStore().getAt(rowIndex);
            var store = this.formatComboEditor.store;
            store.baseParams.moduleId=rec.get("moduleId");
            store.reload();
        }
    },
    saveSequenceFormat:function (){
        var recs = this.smgrid.getStore().getModifiedRecords();
        var jArray = new Array();
        for(var i=0 ; i<recs.length ; i++){
            jArray.push(recs[i].get("seqFormatId"));
        }
       
        Wtf.Ajax.requestEx({
            url: "SeqController",
            params:{
                action:9,
                seqFormatIds: jArray.toString()
            }
        },
        this,
        function(result, response){
            var obj = eval('('+result+')');
            this.close();
        },
        function(){
            //exception
            })
    }
});

Wtf.SeqFormatCombo = function(config){
    this.seqNumberField = undefined;
    this.moduleId = null;
    this.width=200;
    this.isView =config.isView;
    Wtf.apply(this,config);
    
    this.SeqFormatRec = Wtf.data.Record.create([
    {
        name:"seqFormatId"
    },{
        name:"formatedNumber"
    },{
        name:"isDefault"
    }]);
    this.SeqFormatStore = new Wtf.data.Store({
        baseParams: {
            action: 6,
            isActive:true,
            moduleId: this.moduleId 
        },
        url:"INVSeq/getSeqFormats.do",
        reader: new Wtf.data.KwlJsonReader({
            root: 'data',
            totalProperty:'count'
        },this.SeqFormatRec)
    });
    
    
    this.SeqFormatStore.on('load',function(ds){
        var newRec=new this.SeqFormatRec({
            seqFormatId:'NA',
            formatedNumber:'NA'
        })
        if(!this.isView){
            WtfGlobal.hideFormElement(this.seqNumberField);
        }
        this.SeqFormatStore.insert(0,newRec);
        for(var i=0;i<ds.data.length;i++){
            if(ds.data.items[i].data.isDefault==true){
                this.setValue(ds.data.items[i].data.seqFormatId)
                
            }
        }
    },this)
    this.SeqFormatStore.load();
    
    Wtf.SeqFormatCombo.superclass.constructor.call(this, {
        fieldLabel:WtfGlobal.getLocaleText("mrp.workorder.entry.sequenceformat"),
        emptyText:WtfGlobal.getLocaleText("acc.common.select"),
        store: this.SeqFormatStore,
        valueField: 'seqFormatId',
        displayField:'formatedNumber',
        mode:'local',
        forceSelection: true,
        selectOnFocus:true,
        triggerAction:'all',
        allowBlank:false,
        renderer:function(val){
            return '<div wtf:qtip=\"'+val+'\">'+val+'</div>';
        }
    });
};
Wtf.extend(Wtf.SeqFormatCombo, Wtf.form.ComboBox, {
    
    initComponent: function() {
        Wtf.SeqFormatCombo.superclass.initComponent.call(this);

    },
    onRender: function(config) {
        Wtf.SeqFormatCombo.superclass.onRender.call(this, config);
        
        this.on("select", function(){
            if(this.seqNumberField){
                this.setNextSeqNumber(this.seqNumberField);
            }
        },this)
        
    },
    setNextSeqNumber : function(sequenceNumberField){ 
        if(!this.getValue() || this.getValue()=="NA"){
            WtfGlobal.showFormElement(this.seqNumberField);
//            this.seqNumberField.reset();
            this.seqNumberField.enable();
            return;
        }else{
             WtfGlobal.hideFormElement(this.seqNumberField);
        }
        this.seqNumberField = sequenceNumberField;
        Wtf.Ajax.requestEx({
            url: "INVSeq/getSeqFormatNextNumber.do",
            params:{
                seqFormatId: this.getValue()
            }
        },
        this,
        function(result, response){
            //            var obj = eval('('+result+')');
            var nextSeqNumber = result.data.formatedSeqNumber;
//            this.seqNumberField.setValue(nextSeqNumber);
        },
        function(){
            //exception
            })

    }
    
})

