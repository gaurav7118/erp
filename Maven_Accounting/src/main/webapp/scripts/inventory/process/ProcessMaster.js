/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
function callProcessMaster(rec){
    var processMasterForm=Wtf.getCmp("processMasterFormID");
    if(processMasterForm==null){
        processMasterForm = new Wtf.ProcessMasterForm({
            title : "Process Master",
            modal : true,
            id: "processMasterFormID",
            //            iconCls : 'iconwin',
            layout: "fit",
            border:false,
            record:rec,
            width : 400,
            height: 450,
            resizable :false
        });
    }
    processMasterForm.doLayout();
    processMasterForm.show();
}

Wtf.ProcessMasterForm = function (config){
    Wtf.apply(this,config);
    Wtf.ProcessMasterForm.superclass.constructor.call(this,{
        buttons:[
        {
            text:"Close",
            handler:function (){
                this.close();
            },
            scope:this
        }
        ]
    });
}

Wtf.extend(Wtf.ProcessMasterForm,Wtf.Window,{
    initComponent:function (){
        Wtf.ProcessMasterForm.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();

        this.mainPanel = new Wtf.Panel({
            layout:"border",
            items:[
            this.northPanel,
            this.AddProcessInfoPanel
            ]
        });

        this.add(this.mainPanel);
        this.myloadMask = new Wtf.LoadMask(document.body,{
            msg:"Saving data..."
        });
    },
    GetNorthPanel:function (){
        var wintitle='Process Master';
        var windetail='';
        var image='';
        windetail='Add new Process';
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


        this.processName = new Wtf.form.TextField({
            fieldLabel: "Process Name*",
            name:'process',
            emptyText:"Enter Process Name",
            width:200,
            allowBlank:false
        });

        this.record = Wtf.data.Record.create([
        {
            name:"processId"
        },

        {
            name:"process"
        }
        ]);
        this.ds = new Wtf.data.Store({
            baseParams: {
                action: 27
            },
            url: Wtf.req.MachineShopController,
            sortInfo : {
                field : 'process',
                direction : 'ASC'
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data',
                totalProperty:'count'
            },this.record)
        });

        
        this.cmConfig = [new Wtf.grid.RowNumberer(),{
            header: "Process",
            dataIndex: 'process',
           // sortable:true,
            width: 90
        }];

        this.cm = new Wtf.grid.ColumnModel(this.cmConfig);
        this.sm = new Wtf.grid.RowSelectionModel({
            width:25,
            singleSelect:true
        });
        this.processMasterGrid = new Wtf.grid.GridPanel({

            cm:this.cm,
            store:this.ds,
            border:false,
            sm:this.sm,
            viewConfig: {
                forceFit: true,
                emptyText:"<center>No data to display</center>"
            }
        });
        
        this.sm.on('rowselect',function(){
            this.formAction="Edit";
            var rec = this.processMasterGrid.getSelectionModel().getSelected();
            this.processName.setValue(rec.get("process"));
        },this);
        
        //this.ds.load();
        this.ds.on('load',function(){
            this.clearValue();
        },this)
        this.AddProcessMasterForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            bodyStyle:"background-color:#f1f1f1;padding:15px",
            url: Wtf.req.MachineShopController,
            labelWidth:130,
            items:[
                
            this.processName
            ],
            bbar:[
            {
                text:"Save",
                tooltip: {
                    text:"Click to Save"
                },
                handler:function (){
                    this.saveProcessDetails();
                },
                scope:this
            },
            {
                text:"Clear",
                tooltip: {
                    text:"Click to Clear"
                },
                handler:function (){
                    this.clearValue();
                },
                scope:this
            }
            ]
    
        });
        this.formRegion = {
            region:'north',
            height:100,
            border:false,
            layout: 'fit',
            items:[
            this.AddProcessMasterForm
            ]
        }
        this.gridRegion = {
            region:'center',
            border:false,
            layout: 'fit',
            items:[
            this.processMasterGrid
            ]
        }
        this.panelItems = new Array();
      //  if(checktabperms(14, 5) == "edit"){
            this.panelItems.push(this.formRegion);
       // }
        this.panelItems.push(this.gridRegion);
        this.AddProcessInfoPanel = new Wtf.Panel({
            region:"center",
            layout:'border',
            border:false,
            items:this.panelItems
        });
    },
    
    clearValue:function(){
        this.formAction="Add";
        this.processName.setValue("");
    },
    saveProcessDetails:function (){
        
        if(this.AddProcessMasterForm.form.isValid()){
            this.myloadMask.show();
            var rec = this.AddProcessMasterForm.getForm().getValues();
            rec.action = 26;
            rec.formAction = this.formAction=="Add"?"Add":"Edit",
            rec.processId=this.formAction=="Add"?"":this.processMasterGrid.getSelectionModel().getSelected().get("processId");
            //            rec.machineId = this.machineCombo.getValue();
            
            Wtf.Ajax.requestEx({
                url:Wtf.req.MachineShopController,
                params:rec
            },
            this,
            function (res, req){
                this.myloadMask.hide();
                Wtf.MessageBox.show({
                    title:"Status",
                    msg:res.msg,
                    icon:Wtf.MessageBox.INFO,
                    buttons:Wtf.MessageBox.OK
                });
                this.AddProcessMasterForm.getForm().reset();
                this.ds.reload();
            },
            function (req, res){
                this.myloadMask.hide();
                Wtf.MessageBox.show({
                    title:"Status",
                    msg:res.data.msg,
                    icon:Wtf.MessageBox.ERROR,
                    buttons:Wtf.MessageBox.OK
                });
            })
            
            
        //            this.AddProcessMasterForm.form.submit({
        //                params:{
        //                    action:9,
        //                    formAction:this.action=="Add"?"Add":"Edit",
        //                    designationId:this.action=="Add"?"":this.processMasterGrid.getSelectionModel().getSelected().get("id")
        //                },
        //                success:function (response,request){
        //                    Wtf.MessageBox.show({
        //                        title:"Status",
        //                        msg:"Designation details submitted successfully.",
        //                        icon:Wtf.MessageBox.INFO,
        //                        buttons:Wtf.MessageBox.OK
        //                    });
        //                    this.ds.reload();
        //                },
        //                failure:function (response,request){
        //                    Wtf.MessageBox.show({
        //                        title:"Status",
        //                        msg:"Error occurred while submitting designation details.",
        //                        icon:Wtf.MessageBox.ERROR,
        //                        buttons:Wtf.MessageBox.OK
        //                    });
        //                },                
        //                scope:this
        //            });
        }
    }
});