/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

function callMachineMaster(){
    var mainTabId = Wtf.getCmp("as");
    var machineMaster = Wtf.getCmp("machineMasterTab");
    if(machineMaster == null){
        machineMaster = new Wtf.MachineMasterGrid({
            layout:"fit",
            title:WtfGlobal.getLocaleText("acc.field.machinemaster.title"),
            closable:true,
            border:false,
            id:"machineMasterTab"
        });
        mainTabId.add(machineMaster);
    }
    mainTabId.setActiveTab(machineMaster);
    mainTabId.doLayout();
}

Wtf.MachineMasterForm = function (config){
    Wtf.apply(this,config);
    Wtf.MachineMasterForm.superclass.constructor.call(this,{
        buttons:[
        {
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),
            handler:function (){
               // this.saveMachineDetails();
            },
            scope:this
        },
        {
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            handler:function (){
                this.close();
            },
            scope:this
        }
        ]
    });
}

Wtf.extend(Wtf.MachineMasterForm,Wtf.Window,{
    initComponent:function (){
        Wtf.MachineMasterForm.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        
        this.mainPanel = new Wtf.Panel({
            layout:"border",
            items:[
            this.northPanel,
            this.AddMachineMasterForm
            ]
        });

        this.add(this.mainPanel);
    },
    GetNorthPanel:function (){
        var wintitle=this.action=="Add"?WtfGlobal.getLocaleText("acc.machinemaster.add.wintitle"):WtfGlobal.getLocaleText("acc.machinemaster.edit.wintitle");
        var windetail='';
        var image='';
        windetail=this.action=="Add"?WtfGlobal.getLocaleText("acc.machinemaster.add.windetail"):WtfGlobal.getLocaleText("acc.machinemaster.edit.windetail");
        //image='images/project.gif';
        image="../../images/accounting_image/role-assign.gif";
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(wintitle,windetail,image)
        });
    },
    GetAddEditForm:function (){

        
        this.machineNumber = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machinemaster.fieldLabel.machineNo"),
            name:'machineNumber',
            emptyText: WtfGlobal.getLocaleText("acc.machinemaster.emptyText.machineNo"),
            width:200,
            //            readOnly : true,
            allowBlank:false
        });

        this.description = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machinemaster.fieldLabel.description"),
            emptyText: WtfGlobal.getLocaleText("acc.machinemaster.emptyText.description"),
            name:'description',
            width:200,
            allowBlank:false
        });
        
        this.processRecord = Wtf.data.Record.create([
        {
            name:"processid"
        },
        {
            name:"processname"
        }]);
        this.processStore = new Wtf.data.Store({
            baseParams: {
              //  action: 31
            },
            url:'../../invjson/ProcessMaster_31.json',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },this.processRecord)
        });
        this.processStore.load();
        
        this.processFilter = {
            mode: 'local',
            triggerAction: 'all',
            hiddenName:"processids",
            fieldLabel : WtfGlobal.getLocaleText("acc.machinemaster.fieldLabel.process"),
            typeAhead: true,
            width:200,
            allowBlank:false,
            editable: true,
            store: this.processStore,
            displayField: 'processname',
            valueField:'processid',
            msgTarget: 'side',
            emptyText:WtfGlobal.getLocaleText("acc.machinemaster.emptyText.selproc")
        };

        this.processCombo = new Wtf.common.Select(Wtf.applyIf({
            multiSelect:true,
            id: "processCombo"+this.id
        }, this.processFilter));
        
        this.processStore.on('load',function(){
            if(this.record!=null){
                this.processCombo.setValue(this.record.get("processid"));
            }    
        },this);
        
        this.machineRate = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machinemaster.fieldLabel.rate"),
            name:'machineRate',
            allowBlank:false,
            allowNegative:false,
            readOnly:true,
            decimalPrecision:2,
            value:0,
            minValue: 0,
            width:200
        });
        
        this.assetRecord = Wtf.data.Record.create([
        {
            name:"fa_tag"
        },
        {
            name:"assetName"
        }]);
        this.assetStore = new Wtf.data.Store({
            baseParams: {
              //  action: 65
            },
            url:'../../invjson/ProcessMaster_65.json',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },this.assetRecord)
        });
        this.assetStore.load();
        
        this.fa_Tag = new Wtf.form.ComboBox({
            store:this.assetStore,
            fieldLabel: WtfGlobal.getLocaleText("acc.machinemaster.fieldLabel.FATag"),
            emptyText: WtfGlobal.getLocaleText("acc.machinemaster.emptyText.FATag"),
            valueField: 'fa_tag',
            displayField:'assetName',
            hiddenName:'fa_tag',
            mode:'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            width : 200,
            triggerAction:'all',
            renderer:function(val){
                return '<div wtf:qtip=\"'+val+'\">'+val+'</div>';
            }
                               
        });
           
        this.purchaseCost = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machinemaster.fieldLabel.purchasecost"),
            name:'purchaseCost',
            emptyText: WtfGlobal.getLocaleText("acc.machinemaster.emptyText.purchasecost"),
            allowBlank:false,
            allowNegative:false,
            //decimalPrecision:Wtf.companyPref.priceDecimalPrecision,
            decimalPrecision:2,
            minValue: 1,
            width:200
        });
        
        this.life = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machinemaster.fieldLabel.Life"),
            name:'life',
            emptyText: WtfGlobal.getLocaleText("acc.machinemaster.emptyText.Life"),
            allowBlank:false,
            allowNegative:false,
            decimalPrecision:2,
            minValue: 1,
            width:200
        });
        
        this.hours = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.machinemaster.fieldLabel.Hours"),
            name:'hours',
            emptyText: WtfGlobal.getLocaleText("acc.machinemaster.emptyText.Hours"),
            allowBlank:false,
            minValue: 1,
            width:200
        });
        
        this.myloadMask = new Wtf.LoadMask(document.body,{
            msg:WtfGlobal.getLocaleText("acc.machinemaster.submitting.data")
        });
        
        this.ratio = new Wtf.form.Checkbox({
            id: 'ratio'+this.id,
            bodyStyle: "padding-left: 105px; margin-top:4px;",
            border: false,
            scope:this,
            fieldLabel:WtfGlobal.getLocaleText("acc.machinemaster.fieldLabel.Ratio"),
            name:'ratio',
            listeners:{
                scope:this,
                render:function(c){
                    Wtf.QuickTips.register({
                        target:  c.getEl(),
                        text: WtfGlobal.getLocaleText("acc.machinemaster.fieldLabel.Ratio")
                    });
                }
            }
        });
        
        
        this.formItems = new Array();
        if(this.action=="Add"){
            //            this.sequencer = getNextSequencer(1, this.machineNumber)
            
            //            this.formItems.push(this.sequencer.getSeqFormatCombo());
            this.formItems.push(this.machineNumber);
            this.formItems.push(this.description);
            this.formItems.push(this.processCombo);
            this.formItems.push(this.fa_Tag);
            this.formItems.push(this.purchaseCost);
            this.formItems.push(this.life);
            this.formItems.push(this.hours);
            this.formItems.push(this.machineRate);
            this.formItems.push(this.ratio);
            
        }else{
            this.formItems.push(this.machineNumber);
            this.formItems.push(this.description);
            this.formItems.push(this.processCombo);
            this.formItems.push(this.fa_Tag);
            this.formItems.push(this.purchaseCost);
            this.formItems.push(this.life);
            this.formItems.push(this.hours);
            this.formItems.push(this.machineRate);
            this.formItems.push(this.ratio);
        }
        
        this.AddMachineMasterForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            bodyStyle:"background-color:#f1f1f1;padding:15px",
            url: Wtf.req.MachineShopController,
            labelWidth:130,
            items:this.formItems
        });
        
        this.purchaseCost.on('change', function(){
            var purchaseCost = this.purchaseCost.getValue();
            var life = this.life.getValue();
            var hours = this.hours.getValue();
            var ratePerHours = purchaseCost/(life*hours);
           // this.machineRate.setValue(WtfGlobal.getCurrencyFormatWithoutSymbol(ratePerHours,Wtf.companyPref.priceDecimalPrecision));
            this.machineRate.setValue(WtfGlobal.getCurrencyFormatWithoutSymbol(ratePerHours,2));
        },this);
        
        this.life.on('change', function(){
            var purchaseCost = this.purchaseCost.getValue();
            var life = this.life.getValue();
            var hours = this.hours.getValue();
            var ratePerHours = purchaseCost/(life*hours);
           // this.machineRate.setValue(WtfGlobal.getCurrencyFormatWithoutSymbol(ratePerHours,Wtf.companyPref.priceDecimalPrecision));
            this.machineRate.setValue(WtfGlobal.getCurrencyFormatWithoutSymbol(ratePerHours,2));
        },this);
        
        this.hours.on('change', function(){
            var purchaseCost = this.purchaseCost.getValue();
            var life = this.life.getValue();
            var hours = this.hours.getValue();
            var ratePerHours = purchaseCost/(life*hours);
          //  this.machineRate.setValue(WtfGlobal.getCurrencyFormatWithoutSymbol(ratePerHours,Wtf.companyPref.priceDecimalPrecision));
            this.machineRate.setValue(WtfGlobal.getCurrencyFormatWithoutSymbol(ratePerHours,2));
        },this);
        
        
        if(this.record!=null){
            this.machineNumber.setValue(this.record.get("machineno"));
            this.description.setValue(this.record.get("description"));
            this.machineRate.setValue(this.record.get("rate"));
            this.fa_Tag.setValue(this.record.get("fa_tag"));
            this.purchaseCost.setValue(this.record.get("purchasecost"));
            this.life.setValue(this.record.get("life"));
            this.hours.setValue(this.record.get("hours"));
            this.ratio.setValue(this.record.get("ratio"));
        }
    },
    
    saveMachineDetails:function (){
        
        if(this.AddMachineMasterForm.form.isValid()){
            if(this.purchaseCost.getValue()==0){
                Wtf.MessageBox.show({
                    title:WtfGlobal.getLocaleText("acc.common.info"),
                    msg:WtfGlobal.getLocaleText("acc.machinemaster.msg.purchase.cost"),
                    icon:Wtf.MessageBox.INFO,
                    buttons:Wtf.MessageBox.OK
                });
                return;
            }else if(this.life.getValue()==0){
                Wtf.MessageBox.show({
                    title:WtfGlobal.getLocaleText("acc.common.info"),
                    msg:WtfGlobal.getLocaleText("acc.machinemaster.msg.life"),
                    icon:Wtf.MessageBox.INFO,
                    buttons:Wtf.MessageBox.OK
                });
                return;
            }else if(this.hours.getValue()==0){
                Wtf.MessageBox.show({
                    title:WtfGlobal.getLocaleText("acc.common.info"),
                    msg:WtfGlobal.getLocaleText("acc.machinemaster.msg.hours"),
                    icon:Wtf.MessageBox.INFO,
                    buttons:Wtf.MessageBox.OK
                });
                return;
            }
            this.myloadMask.show();
            this.AddMachineMasterForm.form.submit({
                scope:this,
                params:{
                    action:this.action=="Add"?5:6,
                    machineRatio:this.ratio.getValue(),
                    machineId:this.action=="Add"?"":this.record.get("machineid"),
                    seqRealValue:null,//this.action=="Add"?this.sequencer.getSeqRealValue():null,
                    seqFormatId:null//this.action=="Add"?this.sequencer.getSeqFormatId():null
                },
                success:function (response,request){
                    this.myloadMask.hide();
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.cc.8"),
                        msg: WtfGlobal.getLocaleText("acc.machinemaster.machine.saved.successfully"),
                        icon: Wtf.MessageBox.INFO,
                        buttons: Wtf.MessageBox.OK
                    });
                    if(this.store!=null){
                        this.store.reload();
                    }
                    this.close();
                },
                failure:function (response,request){
                    this.myloadMask.hide();
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.cc.8"),
                        msg: WtfGlobal.getLocaleText("acc.machinemaster.machine.saved.Error"),
                        icon:Wtf.MessageBox.ERROR,
                        buttons:Wtf.MessageBox.OK
                    });
                    this.close();
                },                
                scope:this
            });
        }
    }
   
});



/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
Wtf.MachineMasterGrid = function(config){
    Wtf.MachineMasterGrid.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.MachineMasterGrid, Wtf.Panel, {
    initComponent: function() {
        Wtf.MachineMasterGrid.superclass.initComponent.call(this);
    },
    onRender: function(config) {
        Wtf.MachineMasterGrid.superclass.onRender.call(this, config);

        this.record = Wtf.data.Record.create([
        {
            name:"machineid"
        },

        {
            name:"machineno"
        },

        {
            name:"createdby"
        },

        {
            name:"description"
        },
        {
            name:"rate"
        },
        {
            name:"fa_tag"
        },
        {
            name:"assetName"
        },
        {
            name:"purchasecost"
        },
        {
            name:"life"
        },
        {
            name:"hours"
        },
        {
            name:"ratio"
        },
        {
            name:"processid"
        },
        {
            name:"processname"
        },
        {
            name:"createdon"
        }
        ]);
  /*      this.ds = new Wtf.data.JsonStore({
//            baseParams: {
//                action: 8
//            },
            url: "../../invjson/MachineMaster.json",
            sortInfo: {
                field: 'machineno',
                direction: 'ASC' // or 'DESC' (case sensitive for local sorting)
            },
//            remoteSort:true,
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },this.record)
        });
   */     
  
        
       this.ds = new Wtf.data.JsonStore({
            autoLoad: true,
            url:'../../invjson/MachineMaster.json',
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },this.record)
        }); 


       
//        this.ds.load({
//            params:{
//                start:0,
//                limit:15
//            }
//        });
        this.ds.on("load",function(){
            this.editButton.disable();
        },this);
        
        this.sm = new Wtf.grid.RowSelectionModel({
            width:25,
            singleSelect:true
        });
        
        this.cmConfig = [new Wtf.KWLRowNumberer(),{
            header: WtfGlobal.getLocaleText("acc.machinemaster.header.1"),
            dataIndex: 'machineno',
            //sortable:true,
            width: 90
        },{
            header: WtfGlobal.getLocaleText("acc.machinemaster.header.2"),
            dataIndex: 'description',
            //sortable:true,
            width: 120
        },{
            header: WtfGlobal.getLocaleText("acc.machinemaster.header.3"),
            dataIndex: 'rate',
            //sortable:true,
            width: 40,
            renderer:function(val){
               // return WtfGlobal.getCurrencyFormatWithoutSymbol(val,Wtf.companyPref.priceDecimalPrecision);
                return WtfGlobal.getCurrencyFormatWithoutSymbol(val,2);
            }
        },{
            header: WtfGlobal.getLocaleText("acc.machinemaster.header.4"),
            dataIndex: 'assetName',
            //sortable:true,
            width: 40
        },{
            header: WtfGlobal.getLocaleText("acc.machinemaster.header.5"),
            dataIndex: 'purchasecost',
            //sortable:true,
            width: 60,
            renderer:function(v){
                //var v =WtfGlobal.getCurrencyFormatWithoutSymbol(v,Wtf.companyPref.priceDecimalPrecision);
                var v =WtfGlobal.getCurrencyFormatWithoutSymbol(v,2);
                return '<div class="currency">'+v+'</div>';
            }
        },{
            header: WtfGlobal.getLocaleText("acc.machinemaster.header.6"),
            dataIndex: 'life',
           // sortable:true,
            renderer: WtfGlobal.currencyRendererWithoutSymbol,
            width: 40
        },{
            header: WtfGlobal.getLocaleText("acc.machinemaster.header.7"),
            dataIndex: 'hours',
           // sortable:true,
            renderer: WtfGlobal.currencyRendererWithoutSymbol,
            width: 40
        },{
            header: WtfGlobal.getLocaleText("acc.machinemaster.header.8"),
            dataIndex: 'processname',
            //sortable:false,
            width: 40,
            renderer:function(val){
                return '<div wtf:qtip=\"'+val+'\">'+val+'</div>';
            }
        },{
            header: WtfGlobal.getLocaleText("acc.machinemaster.header.9"),
            dataIndex: 'ratio',
            //sortable:true,
            width: 40,
            renderer :function(val){
                if(val==true){
                    return "True";
                }else{
                    return "False";
                }
            }
        }];

        this.cm = new Wtf.grid.ColumnModel(this.cmConfig);
        this.pagingToolbar = new Wtf.PagingToolbar({
            pageSize: this.pageLimit,
            id: "pagingtoolbar" + this.id,
            store: this.ds,
            displayInfo: true,
            //            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.field.Nodatatodisplay"),
            plugins: this.pP = new Wtf.common.pPageSize({
                id : "pPageSize_"+this.id
            })
        });
        
        this.addButton= new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.add"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.machinemaster.tooltip.addbtn")
            },
            iconCls :getButtonIconCls(Wtf.etype.add),
            id:"add",
            handler:this.addJobRequest,
            scope:this
        });
        this.editButton= new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.edit"),
            tooltip: {
                text:WtfGlobal.getLocaleText("acc.machinemaster.tooltip.editbtn")
            },
            iconCls :getButtonIconCls(Wtf.etype.edit),
            id:"edit",
            handler:this.editJobRequest,
            disabled:true,
            scope:this
        });
        
        this.tbarArray = new Array()
       // if(checktabperms(14, 2) == "edit"){
            this.tbarArray.push(this.addButton);
            this.tbarArray.push("-");
            this.tbarArray.push(this.editButton);
       // }

        this.machineMasterGrid = new Wtf.grid.GridPanel({

            cm:this.cm,
            sm:this.sm,
            store:this.ds,
            //            sm:this.sm,
            viewConfig: {
                forceFit: true,
                emptyText:"<center>"+WtfGlobal.getLocaleText("acc.field.Nodatatodisplay")+"</center>"
            },
            tbar:this.tbarArray,
            bbar:this.pagingToolbar
        });
        
        this.add(this.machineMasterGrid);
        
        this.sm.on("rowselect", function(){
            this.editButton.enable();
        },this);
    },
    addJobRequest:function(){
        
        new Wtf.MachineMasterForm({
            id: "machineMasterForm",
            border : false,
            title : WtfGlobal.getLocaleText("acc.machinemaster.add.wintitle"),
            layout : 'fit',
            closable: true,
            width:450,
            modal:true,
            store: this.ds,
            action:"Add",
            height:430,
            resizable:false
        }).show();
        
        
    },
    editJobRequest:function(){
        
        new Wtf.MachineMasterForm({
            id: "machineMasterForm",
            border : false,
            title : WtfGlobal.getLocaleText("acc.machinemaster.edit.wintitle"),
            layout : 'fit',
            closable: true,
            width:450,
            //record : this.machineMasterGrid.getSelectionModel().getSelected(),
            store: this.ds,
            modal:true,
            action:"Edit",
            height:430,
            resizable:false
        }).show();
        
        
    }

});
