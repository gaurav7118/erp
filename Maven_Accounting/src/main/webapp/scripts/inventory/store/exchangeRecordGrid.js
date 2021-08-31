

Wtf.exchangeRecordsGrid=function(config){
    Wtf.apply(this,config);
    Wtf.exchangeRecordsGrid.superclass.constructor.call(this,{
        buttons :[{
            text : WtfGlobal.getLocaleText("acc.common.submit"),
            scope: this,
            handler:function(){
                var delFlag = false;

                if(delFlag == true){
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.common.error"),
                        msg: WtfGlobal.getLocaleText("acc.please.enter.quantity"),
                        buttons: Wtf.MessageBox.OK,
                        animEl: 'mb9',
                        icon: Wtf.MessageBox.ERROR
                    });
                } else {
                    this.saveServiceDetails();
                }

            }
        },{
            text : WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            scope : this,
            handler : function(){
                this.close();
            }
        }]
    });
}

Wtf.extend(Wtf.exchangeRecordsGrid, Wtf.Window, {
    onRender:function(config){
        Wtf.exchangeRecordsGrid.superclass.onRender.call(this,config);

        this.storeTyperec = new Wtf.data.Record.create([{
            name: 'id',
            type: 'string'
        },{
            name: 'name',
            type: 'string'
        }]);

        this.storetypedata = new Wtf.data.Store({
            //url: 'jspfiles/inventory/inventory.jsp?flag=17',
            url :"INVStore/getStoreTypeList.do",
            reader: new Wtf.data.KwlJsonReader({
                root:"data"
            }, this.storeTyperec)
        //            baseParams: {
        //                "propertyid": 32
        //            }
        });
        this.storetypedata.load();
                
        //        this.locationDataRec = new Wtf.data.Record.create([{
        //            name: 'id',
        //            type: 'string'
        //        },{
        //            name: 'name',
        //            type: 'string'
        //        }]);        
        //        this.locationDataStore = new Wtf.data.Store({
        //            //url: 'jspfiles/inventory/inventory.jsp?flag=17',
        //            url :"INVStore/getUserPropertiesValue.do",
        //            reader: new Wtf.data.KwlJsonReader({
        //                root:"data"
        //            }, this.locationDataRec),
        //            baseParams: {
        //                "propertyid": 60
        //            }
        //        });
        //this.locationDataStore.load();
        //        this.lastDayStore = new Wtf.data.SimpleStore({
        //            fields: ['id', 'days'],
        //            data: [['2', 'Monday'], ['3', 'Tuesday'], ['4', 'Wednesday'], ['5', 'Thursday'], ['6', 'Friday'], ['7', 'Saturday'], ['1', 'Sunday']]
        //        });

        this.ccDateAllowStore = new Wtf.data.SimpleStore({
            fields: ['id', 'allow'],
            data: [['1','Enable'] , ['0', 'Disable']]
        });

        this.smccallowStore = new Wtf.data.SimpleStore({
            fields: ['id', 'allow'],
            data: [['1','Enable'] , ['0', 'Disable']]
        });
        
        
        this.storeid=new Wtf.form.TextField({
            name:"store_id",
            fieldLabel:"Store ID",
            hidden:true,
            hideLabel:true
        });
        
        this.abbreviations=new Wtf.form.TextField({
            name:"abbreviations",
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Code")+" *",
            allowBlank: false,
            regex: /[a-zA-Z0-9]+/,
            width:190
        });
        
        this.descriptions=new Wtf.form.TextField({
            name:"descriptions",
            fieldLabel:WtfGlobal.getLocaleText("acc.field.description")+" *",
            allowBlank: false,
            regex: /[a-zA-Z0-9]+/,
            width:190
        });
        
        
        this.address = new Wtf.form.TextArea({
            name: "address",
            allowBlank: false,
            regex:Wtf.validateAddress,
            width: 190,
            height: 50,
            maxLength: 255,
            regex: /[a-zA-Z0-9]+/,
            fieldLabel: WtfGlobal.getLocaleText("acc.common.address")+" *"
        });
        
        this.contact=new Wtf.form.TextField({
            width:190,
            fieldLabel:WtfGlobal.getLocaleText("acc.common.contact"),
            maxLength:30
        });
        
        this.fax=new Wtf.form.TextField({
            width:190,
            fieldLabel:WtfGlobal.getLocaleText("acc.common.FaxNo"),
            maxLength:30
        });
        
        
        this.storetypeCombo=new Wtf.form.ComboBox({
            name:"storetype",
            hiddenName:"storetypeid",
            allowBlank:false,
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Type*"),
            typeAhead : true,
            store:this.storetypedata,
            triggerAction:'all',
            displayField:'name',
            valueField:'id',
            width:190
        });
        
        this.ccdateallowCombo=new Wtf.form.ComboBox({
            name: "ccdateallow",
            allowBlank: false,
            fieldLabel: WtfGlobal.getLocaleText("acc.invset.header.17")+" *",//Cycle Count Date check*
            mode: 'local',
            forceSelection: true,
            typeAhead: true,
            store: this.ccDateAllowStore,
            triggerAction: 'all',
            displayField: 'allow',
            valueField:'id',
            width: 190,
            renderer: function(val) {
                return val==true ?'Enable':'Disable';
            }
        });
         this.parentCombo = new Wtf.form.ComboBox({
                triggerAction: 'all',
                hidden: false,
                mode: 'local',
                valueField: 'id',
                width:190,
                hidden:this.hideParentCmb,
                hideLabel:this.hideParentCmb,
                displayField: 'name',
                store: this.parentStore,
                fieldLabel: WtfGlobal.getLocaleText("acc.field.SelectParent"),
                value: this.parentid ?this.parentid : '',
                typeAhead: true,
                forceSelection: true,
                hiddenName: 'parent'
            });
        this.movementTypeRec=new Wtf.data.Record.create([
        {
            name: 'id'
        },
        {
            name: 'name'
        }]
        );

        this.movementtypeStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            }, this.movementTypeRec),
            url:"ACCMaster/getMasterItems.do",
            baseParams:{
                mode:112,
                groupid:30                    // for movement type
            }
        });
        this.movementtypeStore.on("load", function(){
            if(this.action=="Add"){
                this.movementTypeCombo.setValue("");
            }
            else{
                this.movementTypeCombo.setValue(this.storerec.get("movementtype"));
            }
        }, this);
        this.movementtypeStore.load();
        this.movementTypeCombo=new Wtf.common.Select({
            name: "movementtype",
            allowBlank: true,
            //            hidden:Wtf.account.companyAccountPref.isMovementWarehouseMapping == true?false:true,
            fieldLabel:WtfGlobal.getLocaleText("acc.invset.header.19"),//Movement Type
            mode: 'local',
            forceSelection: true,
            typeAhead: true,
            multiSelect:true,
            store: this.movementtypeStore,
            triggerAction: 'all',
            displayField: 'name',
            valueField:'id',
            width: 190
            
        });
            
        this.userRec = new Wtf.data.Record.create([
        {
            name: 'userid'
        },
        {
            name: 'fullname'
        }
        
        ]);

        this.userReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.userRec);

        this.userStore = new Wtf.data.Store({
            url: 'ProfileHandler/getAllUserDetails.do',
            reader:this.userReader
        });
     
 
        this.userCombo= new Wtf.common.Select({
            width:190,
            fieldLabel:WtfGlobal.getLocaleText("acc.invset.header.20")+" *",//Store Managers *
            name:'users',
            store:this.userStore,
            //hiddenName:'approver',
            xtype:'select',
            selectOnFocus:true,
            forceSelection:true,
            multiSelect:true,
            displayField:'fullname',
            valueField:'userid',
            mode: 'local',
            allowBlank:false,
            triggerAction:'all',
            editable:false ,
            typeAhead: true
        }),
        this.userStore.load({
            params:{
                isActive : "true"
            }
        });
        this.userStore.on("load", function(){
            if(this.action=="Add"){
                this.userCombo.setValue("");
            }
            else{
                this.userCombo.setValue(this.storerec.get("userids"));
            }
        }, this);
        this.executiveStore = new Wtf.data.Store({
            url: 'ProfileHandler/getAllUserDetails.do',
            reader:this.userReader
        });
     
 
        this.executiveCombo= new Wtf.common.Select({
            width:190,
            fieldLabel:WtfGlobal.getLocaleText("acc.invset.header.21")+" *",//Store Executives *
            name:'users',
            store:this.executiveStore,
            //hiddenName:'approver',
            xtype:'select',
            selectOnFocus:true,
            forceSelection:true,
            multiSelect:true,
            displayField:'fullname',
            valueField:'userid',
            mode: 'local',
            allowBlank:false,
            triggerAction:'all',
            typeAhead: true,
            editable:false 
        }),
        this.executiveStore.load({
            params:{
                isActive : "true"
            }
        });
        this.executiveStore.on("load", function(){
            if(this.action=="Add"){
                this.executiveCombo.setValue("");
            }
            else{
                this.executiveCombo.setValue(this.storerec.get("executiveids"));
            }
        }, this);
        // Only For Indian Company it will Display Others Hide - ERP-20992
        this.vat =new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupwizard.vat"),
            id:"store"+"vat",
            width:190,
            maxLength:11,
            invalidText :WtfGlobal.getLocaleText("acc.common.AlphaNumonly"),
            vtype : "alphanum",
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,   
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseenternumber")
        });
      
        
        this.cst =new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.setupwizard.cst"),
            id:"store"+"cst",
            width:190,
            maxLength:11,
            invalidText :WtfGlobal.getLocaleText("acc.common.AlphaNumonly"),
            vtype : "alphanum",
            hidden: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            hideLabel: Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
            emptyText: WtfGlobal.getLocaleText("acc.field.Pleaseenternumber")
        });
        
        //---------------------------------------------------------------------        
        this.storeInfo = new Wtf.form.FormPanel({
            waitMsgTarget: true,
            method : 'POST',
            bodyStyle: "background-color: #f1f1f1;",
            border:false,
            layoutConfig: {
                deferredRender: false
            },
            frame: false,
            labelWidth:150,
            items: [this.storeid,this.abbreviations,this.descriptions,this.address,this.contact,this.fax,this.storetypeCombo ,
            //this.ccdateallowCombo,
            this.movementTypeCombo,
            this.userCombo,this.executiveCombo,this.parentCombo,this.vat,this.cst
            ]
        });
        this.storeInfo.on('render',function(){
            if(!Wtf.account.companyAccountPref.isMovementWarehouseMapping){
                this.storeInfo.remove(this.movementTypeCombo);
            }
        },this)
        this.storetypedata.on("load",function(){
            this.storetypeCombo.setValue((this.action=="Add")?"":this.storerec.get("storetypeid"));
        },this);
        // this.locationDataStore.on("load",function(){
        //    this.storeInfo.findById("locationdata").setValue((this.action=="Add")?"":this.storerec.get("locationid"));
        // },this);
     
        this.storeid.setValue((this.action=="Edit")?this.storerec.get("store_id"):"");
        this.descriptions.setValue((this.action=="Add")?"":this.storerec.get("description"));
        this.abbreviations.setValue((this.action=="Add")?"":this.storerec.get("abbr"));
        if(this.action=="Edit"){
            this.address.setValue(this.storerec.get("address"));
        }
        //this.storeInfo.findById("anacode").setValue((this.action=="Add")?0:((this.storerec.get("code")==""||isNaN(this.storerec.get("code")))?0:this.storerec.get("code")));
        //this.storeInfo.findById("lastdayofweek").setValue((this.action=="Add") ? 1 : this.storerec.get("lastday"));
        //this.ccdateallowCombo.setValue((this.action=="Add") ? true : this.storerec.get("ccdateallow"));
        //this.smccallowCombo.setValue((this.action=="Add") ? true : this.storerec.get("smccallow"));
        this.contact.setValue((this.action=="Add") ? "" : this.storerec.get("contact"));
        this.fax.setValue((this.action=="Add") ? "" : this.storerec.get("fax"));
        this.vat.setValue((this.action=="Add") ? "" :this.storerec.get("vattinnumber"));        
        this.cst.setValue((this.action=="Add") ? "" :this.storerec.get("csttinnumber"));
        this.assignTeamPanel = new Wtf.Panel({
            //                buttonAlign : 'right',
            //                buttons :[{
            //                    text : 'Save',
            //                    scope: this,
            //                    handler:function(){
            //                        var delFlag = false;
            //
            //                        if(delFlag == true){
            //                            Wtf.MessageBox.show({
            //                                title: 'Error',
            //                                msg: 'Please enter quantity for all items',
            //                                buttons: Wtf.MessageBox.OK,
            //                                animEl: 'mb9',
            //                                icon: Wtf.MessageBox.ERROR
            //                            });
            //                        } else {
            //                            this.saveServiceDetails();
            //                        }
            //
            //					}
            //                    },{
            //                    text : 'Cancel',
            //                    scope : this,
            //                    handler : function(){
            //                        this.close();
            //                    }
            //                }],
            layout : 'border',
            items :[{
                region : 'north',
                height : 75,
                border : false,
                bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
                html : '<div style="width: 100%; height: 100%; position: relative; float: left;">\n\
							<div style="float: left; height: 100%; width: auto; position: relative;">\n\
							<img style="margin: 5px; width: 40px; height: 52px;" src="images/createuser.png"/>\n\
							</div><div style="float: left; height: 100%; width: 60%; position: relative;" >\n\
								<div style="margin: 15px 0px 0px 10px; font-size: 12px; float: left; width: 100%; position: relative;">\n\
								<b >' + (this.action == 'Add' ? WtfGlobal.getLocaleText("acc.common.addStore") : WtfGlobal.getLocaleText("acc.common.editStore")) + '</b></div>\n\
								<div style="margin: 15px 0px 10px 10px; font-size: 10px; float: left; width: 100%; position: relative;" >' + (this.action == 'Add' ? WtfGlobal.getLocaleText("acc.common.addStore.new") : WtfGlobal.getLocaleText("acc.common.addStore.edit")) + '\n\
							</div></div></div>'
            },{
                region : 'center',
                border : false,
                bodyStyle : 'background:#f1f1f1;font-size : 10px;padding:20px 20px 20px 20px;',
                layout : 'fit',
                items : [
                {
                    border : false,
                    bodyStyle : 'background:transparent;',
                    // layout : 'border',
                    autoHeight:true,
                    autoScroll:true,
                    items : [
                    this.storeInfo
                        
                    ]
                }]
                
            }]
        });

        this.add(this.assignTeamPanel);
    },


    saveServiceDetails: function() {
        if(!this.storeInfo.form.isValid()) {
            return;
        }
        if(this.userCombo.getValue() === "" || this.userCombo.getValue() == undefined) {
            this.userCombo.setValue("");
            return;
        }
        if(this.executiveCombo.getValue() === "" || this.executiveCombo.getValue() == undefined) {
            this.executiveCombo.setValue("");
            return;
        }
        
        //this.storeid,this.abbreviations,this.descriptions,this.address,this.contact,this.fax,this.storetypeCombo,
        //             this.ccdateallowCombo,this.smccallowCombo

        if(this.action == 'Add' || this.action == 'Clone' || this.action == 'Edit'){
            this.store_id = this.storeid.getValue();
            //this.code = this.storeInfo.findById("anacode").getValue();
            this.abbr = this.abbreviations.getValue();
            this.description = this.descriptions.getValue();
            this.contactno = this.contact.getValue();
            this.faxno = this.fax.getValue();
            this.addr = this.address.getValue();
            this.storeType = this.storetypeCombo.getValue();
            //this.lastDay = this.storeInfo.findById("lastdayofweek").getValue();
            //this.ccdateallowStore= this.ccdateallowCombo.getValue();//for Cc module date filter
            //this.smccallowStore=this.smccallowCombo.getValue();
            // this.locationDataStore=this.storeInfo.findById('locationdata').getValue();
            Wtf.Ajax.requestEx({
                url : this.action == "Edit" ? "INVStore/updateStore.do" :"INVStore/saveStore.do",
                params: {
                    id : this.store_id,
                    //code : this.code,
                    abbreviation : this.abbr,
                    description : this.description,
                    address: this.addr,
                    //lastday: this.lastDay,
                    district : this.district,
                    storeMan:this.storeMan,
                    action:this.action,
                    storeType:this.storeType,
                    movementType:this.movementTypeCombo.getValue(),
                    ccdateallow: this.ccdateallowStore,
                    smccallow:this.smccallowStore,
                    //location:this.locationDataStore,
                    contactno:this.contactno,
                    faxno:this.faxno,
                    users:this.userCombo.getValue(),
                    executives:this.executiveCombo.getValue(),
                    parentId:this.parentCombo.getValue(),
                    vatno:this.vat.getValue(),
                    cstno:this.cst.getValue()
                },
                method: 'POST'
            },
            this,
            function(res){
               
                if(res.success==true){
                    Wtf.Msg.show({
                        title:WtfGlobal.getLocaleText("acc.common.info"),
                        msg: res.msg,
                        buttons: Wtf.Msg.OK,
                        scope:this,
                        icon:Wtf.MessageBox.INFO,
                        fn: function(){
                            this.close();
                            if(Wtf.getCmp('invstoremastergrid')!=undefined){
                                Wtf.getCmp('invstoremastergrid').getStore().reload();
                            }
                            if(this.setStore !=undefined){
                                this.setStore.reload();
                            }
                        }
                    });
                    if(this.stores != undefined){
                        this.stores.load();
                    }
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),res.msg],2);
                    this.buttons[0].enable();
                }
            },
            function(result, req) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.failure"), WtfGlobal.getLocaleText("acc.common.problem.adding.store")], 2); 
            })
          this.buttons[0].disable();
        }
       
        
    }

})


