/* 
 * To change this template, choose Tools | Templates 
 * and open the template in the editor.
 */
//---------------------------------------------------Add Master-----------------------------------------------------
Wtf.AddEditMaster = function (config){
    Wtf.apply(this,config);
    Wtf.AddEditMaster.superclass.constructor.call(this,{
        buttons:[
        {
            text:"Submit",
            handler:function (){
                this.saveProjectDetail();
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

Wtf.extend(Wtf.AddEditMaster,Wtf.Window,{
    initComponent:function (){
        Wtf.AddEditMaster.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();

        this.mainPanel = new Wtf.Panel({
            layout:"border",
            items:[
            this.northPanel,
            this.AddEditForm
            ]
        });

        this.add(this.mainPanel);
    },
    GetNorthPanel:function (){
        var wintitle=this.action+' Master Data';
        var windetail='';
        var image='';
        if(this.action=="Edit"){
            windetail='Edit the master data information';
            image='images/project.gif';
        } else {
            windetail='Fill up the information to add master data';
            image='images/project.gif';
        }
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(wintitle,windetail,image)
        });
    },
    GetAddEditForm:function (){
        this.parentRec = new Wtf.data.Record.create([
        {
            name:"id"
        },

        {
            name:"name"
        }
        ]);

        this.parentReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.parentRec);

        this.parentStore = new Wtf.data.Store({
            url:"jspfiles/inventory/itemHandler.jsp",
            reader:this.parentReader,
            baseParams:{
                flag:24,
                forMachineShop : this.forMachineShop
            }
        });

        this.parentStore.load();

        this.parentCombo1 = new Wtf.form.ComboBox({
            triggerAction:"all",
            mode:"local",
            typeAhead:true,
            forceSelection:true,
            store:this.parentStore,
            displayField:"name",
            width:200,
            valueField:"id",
            fieldLabel:"Parent",
            hiddenName:"parentid"
        });
        this.parentStore.on("load",function (){
            if(this.action == "Edit"){
                if(this.rec.get("parentid") != 0){
                    this.parentCombo1.setValue(this.rec.get("parentid"));
                }
            }
        },this);

        this.AddEditForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            bodyStyle:"background-color:#f1f1f1;padding:15px",
            url:"jspfiles/inventory/itemHandler.jsp",
            items:[
            {
                xtype:"textfield",
                fieldLabel:"Name",
                width:200,
                name:"name",
                allowBlank:false,
                value:(this.action == "Edit")?this.rec.get("name"):""
            },
            (this.parentid != 0)?this.parentCombo1:""
            ]
        });
    },
    saveProjectDetail:function (){
        checkForm(this.AddEditForm);
        if(this.AddEditForm.form.isValid()){
            this.AddEditForm.form.submit({
                params:{
                    flag:26,
                    action:this.action,
                    id:(this.action == "Edit")?this.rec.get("id"):"",
                    forMachineShop : this.forMachineShop
                },
                success:function(){
                    Wtf.MessageBox.show({
                        title:"Status",
                        msg:(this.action == "Edit")?"Master field edited successfully":"Master field added successfully",
                        icon:Wtf.MessageBox.INFO,
                        buttons:Wtf.MessageBox.OK
                    });
                    this.close();
                    this.store.load({
                        params:{
                            start:0,
                            limit:25
                        }
                    })
                },
                failure:function (){
                    Wtf.MessageBox.show({
                        title:"Status",
                        msg:(this.action == "Edit")?"Error while editing master field":"Error while adding master field",
                        icon:Wtf.MessageBox.ERROR,
                        buttons:Wtf.MessageBox.OK
                    });
                },
                scope:this
            })
        }
    }
});

//---------------------------------------------------Add Master Data------------------------------------------------

Wtf.AddEditMasterData = function (config){
    Wtf.apply(this,config);
    Wtf.AddEditMasterData.superclass.constructor.call(this,{
        buttons:[
        {
            text:"Save",
            handler:function (){
                this.saveProjectDetail();
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

Wtf.extend(Wtf.AddEditMasterData,Wtf.Window,{
    initComponent:function (){
        Wtf.AddEditMasterData.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();

        this.mainPanel = new Wtf.Panel({
            layout:"border",
            items:[
            this.northPanel,
            this.AddEditForm
            ]
        });

        this.add(this.mainPanel);
    },
    GetNorthPanel:function (){
        var wintitle=this.action+' Master Data';
        var windetail='';
        var image='';
        if(this.action=="Edit"){
            windetail='Edit the master data information';
            image='images/project.gif';
        } else {
            windetail='Add master data detail';
            image='images/project.gif';
        }
        
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(wintitle,windetail,image)
        });
    },
    GetAddEditForm:function (){
        this.parentRec = new Wtf.data.Record.create([
        {
            name:"id"
        },

        {
            name:"name"
        },

        {
            name:"configid"
        },

        {
            name:"parentid"
        }
            
        ]);

        this.parentReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.parentRec);

        this.parentStore = new Wtf.data.Store({
            url:"jspfiles/inventory/itemHandler.jsp",
            reader:this.parentReader,
            baseParams:{
                flag:25,
                forMachineShop : this.forMachineShop
            }
        });

        this.parentStore.load({
            params:{
                configid:this.parentid
            }
        });

        if(this.parentid == 0){
            this.parentCombo = {
                xtype:"hidden",
                fieldLabel:"Parent",
                name:"parentid",
                value:0
            };
        } else if(this.parentid==1){
            this.parentCombo = {
                xtype:"hidden",
                fieldLabel:"Parent",
                name:"parentid",
                value:11111                                    // AS of now only ordering category is visible so hardcoded ID of parent to dummy cateogry
            };
        } else {
            this.parentCombo = new Wtf.form.ComboBox({
                triggerAction:"all",
                mode:"local",
                typeAhead:true,
                forceSelection:true,
                store:this.parentStore,
                width:200,
                displayField:"name",
                valueField:"id",
                fieldLabel:"Parent",
                hiddenName:"parentid"
            });
        }
        this.parentStore.on("load",function (){
            if(this.action == "Edit"){
                if(this.parentid != (0||1) && this.rec.get("parentid") != 0){                    
                    this.parentCombo.setValue(this.rec.get("parentid"));
                }
            }
        },this);
        
        /**
         * @author Prasad Shinde Date : Tuesday, January 10 2012
         * added a text field-"aliasText" for aliasName 
         * issue : 23301 
         */
        
        
        if(this.configid == 13 || this.configid == 63){
            this.aliasText = new Wtf.form.TextField({
                fieldLabel:"Alias Name ",
                width:200,
                name:"aliasName",
                allowBlank:false,
                value:(this.action == "Edit")?this.rec.get("aliasname"):""
            });
        }
        else
        {
            this.aliasText = {
                xtype:"hidden",
                width:200,
                name:"aliasName"
            };
        }
        
        this.AddEditForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            bodyStyle:"background-color:#f1f1f1;padding:15px",
            url:"jspfiles/inventory/itemHandler.jsp",
            items:[
            {
                xtype:"textfield",
                fieldLabel:"Name",
                width:200,
                allowBlank:false,
                name:"name",
                value:(this.action == "Edit")?this.rec.get("name"):""
            },
            this.aliasText,
            this.parentCombo
            ]
        });
    },
    
    saveProjectDetail:function (){
        checkForm(this.AddEditForm);
        if(this.AddEditForm.form.isValid()){
            if(this.parentCombo.xtype!="hidden") {
                if(this.parentCombo.getValue()==""&&this.configid==5) {
                    this.parentCombo.setValue("11112");
                }
            }
            WtfGlobal.setAjaxTimeOut();
            this.AddEditForm.form.submit({
                params:{
                    flag:27,
                    configid:this.configid,
                    action:this.action,
                    id:(this.action == "Edit")?this.rec.get("id"):"",
                    forMachineShop : this.forMachineShop
                },
                success:function(){
                    WtfGlobal.resetAjaxTimeOut();
                    Wtf.MessageBox.show({
                        title:"Status",
                        msg:(this.action == "Edit")?"Master field edited successfully":"Master field added successfully",
                        icon:Wtf.MessageBox.INFO,
                        buttons:Wtf.MessageBox.OK
                    });
                    this.close();
                    this.store.load({
                        params:{
                            configid:this.configid
                        }
                    });
                },
                failure:function (){
                    Wtf.Ajax.timeout = 30000;
                    Wtf.MessageBox.show({
                        title:"Status",
                        msg:(this.action == "Edit")?"Error while editing master field":"Error while adding master field",
                        icon:Wtf.MessageBox.ERROR,
                        buttons:Wtf.MessageBox.OK
                    });
                },
                scope:this
            })
        }
    }
});

//---------------------------------------------------Add Master Data for Vendors------------------------------------------------

Wtf.AddEditVendorMasterData = function (config){
    Wtf.apply(this,config);
    Wtf.AddEditVendorMasterData.superclass.constructor.call(this,{
        buttons:[
        {
            text:"Submit",
            handler:function (){
                this.saveProjectDetail();
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

Wtf.extend(Wtf.AddEditVendorMasterData,Wtf.Window,{
    initComponent:function (){
        Wtf.AddEditVendorMasterData.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();

        this.mainPanel = new Wtf.Panel({
            layout:"border",
            items:[
            this.northPanel,
            this.AddEditForm
            ]
        });

        this.add(this.mainPanel);
    },
    GetNorthPanel:function (){
        var wintitle='Add Master Data';
        var windetail='Fill up the information to add master data';
        var image='images/project.gif';        
        
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(wintitle,windetail,image)
        });
    },
    GetAddEditForm:function (){       
        
        this.AddEditForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            bodyStyle:"background-color:#f1f1f1;padding:15px",
            url:"jspfiles/inventory/itemHandler.jsp",
            items:[
            {
                xtype:"textfield",
                fieldLabel:"Name",
                width:200,
                allowBlank:false,
                name:"name"
            },
            ]
        });
    },
    
    saveProjectDetail:function (){
        checkForm(this.AddEditForm);
        if(this.AddEditForm.form.isValid()){
            this.AddEditForm.form.submit({
                params:{
                    flag:68,
                    configid:this.configid,
                    forMachineShop : this.forMachineShop
                },
                success:function(){
                    Wtf.MessageBox.show({
                        title:"Status",
                        msg:(this.action == "Edit")?"Master field edited successfully":"Master field added successfully",
                        icon:Wtf.MessageBox.INFO,
                        buttons:Wtf.MessageBox.OK
                    });
                    this.close();
                    this.store.load();
                },
                failure:function (){
                    Wtf.MessageBox.show({
                        title:"Status",
                        msg:(this.action == "Edit")?"Error while editing master field":"Error while adding master field",
                        icon:Wtf.MessageBox.ERROR,
                        buttons:Wtf.MessageBox.OK
                    });
                },
                scope:this
            })
        }
    }
});

