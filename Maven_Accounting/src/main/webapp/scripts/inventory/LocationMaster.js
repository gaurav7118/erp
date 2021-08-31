function callLocationMaster(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventorymasters, Wtf.Perm.inventorymasters.locationmaster)) {
        var LocationMasterGrid=Wtf.getCmp("LocationMasterGrid");
        if(LocationMasterGrid==null){
            LocationMasterGrid = new Wtf.LocationMasterGrid({
                id: "LocationMasterGrid",
                border : false,
                title : WtfGlobal.getLocaleText("acc.lp.locationmaster"), //Changed
                // showArchive:true,
                layout : 'fit',
                style:'backgroud-color:white',
                closable: true,
                iconCls:getButtonIconCls(Wtf.etype.product),
                modal:true
            // iconCls: getTabIconCls(Wtf.etype.leaveshome)
            });
            Wtf.getCmp('as').add(LocationMasterGrid);

        }
        Wtf.getCmp('as').setActiveTab(Wtf.getCmp("LocationMasterGrid"));
        Wtf.getCmp('LocationMasterGrid').doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}

Wtf.LocationMasterGrid = function(config){
    Wtf.LocationMasterGrid.superclass.constructor.call(this,config);
};

Wtf.extend(Wtf.LocationMasterGrid, Wtf.Panel,{
    onRender : function(config){
        Wtf.LocationMasterGrid.superclass.onRender.call(this,config);

        this.sm2 = new Wtf.grid.CheckboxSelectionModel({
            width:25
        });
        this.locationRecord = Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'name'
        },

        {
            name: 'stores'
        },
        
        {
            name: 'storeids'
        },
        {
            name: 'isdefault'
        },
        {
            name: 'isactive'
        }

       

        ]);

      

        this.ds = new Wtf.data.Store({
            id:'dsstore',
            url:  'INVLocation/getLocations.do',
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root:'data'
            }, this.locationRecord)
        // remoteSort :true
        });
        var cmDefaultWidth = 225;
        this.cm = new Wtf.grid.ColumnModel([this.sm2,
        //            new Wtf.KWLRowNumberer({
        //                width:25
        //            }),

        {
                header: WtfGlobal.getLocaleText("acc.invset.header.2"),//Location Name
                dataIndex: 'name',
                id:'description',
                width: cmDefaultWidth,
                groupable: true
            },{
                header: WtfGlobal.getLocaleText("acc.invset.header.3"),//Stores
                dataIndex: 'stores',
                //                width: cmDefaultWidth
                width: 525
            //groupable: true,
            //hidden:true
            
            }
            ,{
                header: WtfGlobal.getLocaleText("acc.invset.header.4"),//Stores Id
                dataIndex: 'storeids',
                //groupable: true,
                hidden:true
            }
            
            ,{
                header:WtfGlobal.getLocaleText("acc.invset.header.16"),//Active
                dataIndex: 'isactive',
                width: cmDefaultWidth,
                renderer:function(v){
                    if(v == true){
                        return "<label style = 'color : green;'>Yes</label>";
                    }else{
                        return "<label style = 'color : red;'>No</label>";
                    }
                }
            }
            
            //            ,{
            //                header: 'Default Location',
            //                dataIndex:"isdefault",
            //                width: cmDefaultWidth,
            //                renderer:function(v){
            //                    if(v == true){
            //                        return "Yes"; 
            //                    }else{
            //                        return "No"; 
            //                    }
            //                }
            //                
            //            }
            
            ]);
        
        this.ds.load({
            params: {
                start:0,
                limit:30
            }
        });

        //this.createGridButtons();
        this.newBtn = new Wtf.Button({
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.common.newLocation"),//New Location
            iconCls :getButtonIconCls(Wtf.etype.add),
            id: 'newlocation',
            tooltip: {
                text: WtfGlobal.getLocaleText("acc.common.newLocation.tt"),//Click to add new Location
            },
            handler: function(){
                this.submitstore('Add');
            },
            scope:this
        });
        //         this.MapLocationsBtn = new Wtf.Button({
        //            text:'Map Locations',
        //            tooltip :{
        //                text:' Click to map Locations with Store'
        //            },
        //            scope: this,
        //            //disabled:true,
        //            handler: this.mapLocationHandler
        //        });
        
        
        this.editBtn = new Wtf.Button({
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.common.editLocation"),//Edit Location
            iconCls :getButtonIconCls(Wtf.etype.edit),
            tooltip: {
                text: WtfGlobal.getLocaleText("acc.common.editLocation.tt")//Click to edit existing Location
            },
            handler: function(){
                if(this.sm2.getSelections().length==1){
                    this.submitstore('Edit');
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.common.loc.select.to.edit")],3);
                    return;
                }
            },
            scope:this
        });
        
        this.markValid = new Wtf.menu.Item({
            text: WtfGlobal.getLocaleText("acc.common.loc.activate"), //Activate Location
            tooltip: WtfGlobal.getLocaleText("acc.common.loc.validates"), //Validates a Location
            scope: this,
            disabled:true,
            iconCls:getButtonIconCls(Wtf.etype.activate),
            handler:function(){
                this.setValidityStatus(1);
            }
        });

        this.markInvalid = new Wtf.menu.Item({
            text: WtfGlobal.getLocaleText("acc.common.loc.deactivate"), //Deactivate Location
            tooltip: WtfGlobal.getLocaleText("acc.common.loc.invalidate"), //Invalidates a Location
            scope: this,
            disabled:true,
            iconCls:getButtonIconCls(Wtf.etype.deactivate),
            handler:function(){
                var rec = this.grid1.selModel.getSelected();
                if(rec.get("isdefault")==true){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.common.loc.deactivate.default.loc")], 0)
                }else{
                    this.setValidityStatus(0);
                }
            }
        });
        
        this.statusButton = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.inventorySetup.locationstatus"), //'Location Status',
            iconCls:'pwnd updateStatus',
            scope: this,
            tooltip:{
                text:WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.Manage.store")//Click to Manage store Status
            },
            menu:[
            this.markValid,
            this.markInvalid
            ]
        });
        
        this.markDefaultButton= new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.markdefault"),//Mark Default
            tooltip:WtfGlobal.getLocaleText("acc.common.markdefault.tt"),//Click to Mark Default Location
            handler: this.markAsDefaultHandler,
            scope:this,
            disabled: true
        });

        this.moduleName = "Location Master";
        var extraConfig = {};
        //        extraConfig.url= "INVStore/importWarehouseData.do";
        extraConfig.url= "INVLocation/importLocationRecords.do";
        var extraParams = "";
        var importBtnArr = Wtf.importMenuArray(this, this.moduleName, this.Store, extraParams, extraConfig);
        
        this.importBtn = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.common.import"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.common.import"),
            iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
            menu: importBtnArr
        });
        var TbarButtonArray = new Array();
        var BbarButtonArray = new Array();

       
        TbarButtonArray.push("-",this.newBtn);
      
        TbarButtonArray.push("-",this.editBtn);
       
        TbarButtonArray.push("-",this.statusButton);
        //        TbarButtonArray.push("-");
        //        TbarButtonArray.push(this.markDefaultButton);
        //        //        TbarButtonArray.push("-");
        //        TbarButtonArray.push(this.duplicateAssetBtn);
        //TbarButtonArray.push("-");
        //TbarButtonArray.push(this.countCalBtn);
        //TbarButtonArray.push("-");
        //TbarButtonArray.push(this.MapLocationsBtn);
        BbarButtonArray.push("-",this.importBtn);
        this.grid1 = new Wtf.KwlEditorGridPanel({
            //border: false,
            id:'location1',
            store: this.ds,
            cm: this.cm,
            sm: this.sm2,
            loadMask : true,
            layout:'fit',
            viewConfig: {
                forceFit: false
            },
            split: true,
            region: 'center',
            // view: this.groupingView,
            displayInfo:true,
            searchLabel: WtfGlobal.getLocaleText("acc.dnList.searchText"),
            searchEmptyText: WtfGlobal.getLocaleText("acc.common.search.byNames"),
            serverSideSearch:true,
            tbar:TbarButtonArray,
            bbar:BbarButtonArray
        });


        this.remarks = "";
        this.add(this.grid1);
        
        this.sm2.on("selectionchange",function(sm) {
            this.markValid.disable();
            this.markInvalid.disable();
            this.markDefaultButton.disable();
            if(sm.getSelections().length == 1) {
                if(sm.getSelected().get("isactive")==true){
                    this.markValid.disable();
                    this.markInvalid.enable();
                }else{
                    this.markValid.enable();
                    this.markInvalid.disable();
                }
               
            }
            if(sm.getSelections().length == 1) {
                if(sm.getSelected().get("isdefault")==false){
                    this.markDefaultButton.enable();   
                }else{
                    this.markDefaultButton.disable();
                }
            }
            if(sm.getSelections().length > 1) {
                this.editBtn.disable();
                
            } else {
                this.editBtn.enable();
            }
        },this);

    //      
    },
    
    setValidityStatus:function(mode){
        if(this.sm2.getCount()==1){
            var msg = null;
            if(mode==1){
                msg = WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.activate.location");
            }else{
                msg = WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.deactivate.location");
                
            }
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), msg, function (btn) {
                if (btn == "yes") {
                    Wtf.Ajax.requestEx({
                        url: mode == 1 ? 'INVLocation/activateLocation.do' : 'INVLocation/deactivateLocation.do',
                        params: {
                            id: this.sm2.getSelections()[0].get("id"),
                            mode: mode
                                    //                            flag:106
                        }
                    }, this, function (result, resp) {
                        //                    var resObj=eval('('+result+')')
                        if (result.success == true) {
                            WtfComMsgBox(["Success", "Status Updated successfully."], 3);

                        } else {
                            WtfComMsgBox(["Warning", result.msg], 2);
                        }
                    });
                }
            }, this);
        }else{
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invsetup.alert.sel.single.record"),function(btn){
                },this);
        }

    },
    
    markAsDefaultHandler : function(){
        if(this.grid1.selModel.getCount()==1){
            var rec = this.grid1.selModel.getSelected();
            if(rec.get('isDefault') != true){
                var locationId = rec.get("id");
                this.locationFormatMarkAsDefault(locationId);
            }else{
                Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.loc.already.setdefault"), 1)
            }
            
        }else{
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invsetup.alert.sel.single.record"), 1)
        }
    },
    
    locationFormatMarkAsDefault : function(locationId){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.loc.setdefault.continue"), function(obj){
            if(obj == "yes"){
                Wtf.Ajax.requestEx({
                    url: "INVLocation/markLocationAsDefault.do",
                    params:{
                        //                        action:4,
                        locationid: locationId
                    }
                },
                this,
                function(result, response){
                    //var obj = eval('('+result+')');
                    var msg = result.msg;
                    if(!msg){
                        msg = WtfGlobal.getLocaleText("acc.loc.setdefault.succes")
                    }
                    Wtf.MessageBox.show({
                        title:WtfGlobal.getLocaleText("acc.common.status"),
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
                        title: WtfGlobal.getLocaleText("acc.common.status"),
                        msg: WtfGlobal.getLocaleText("acc.error.occurred.changing.seqformat"),
                        icon:Wtf.MessageBox.ERROR,
                        buttons:Wtf.MessageBox.OK
                    });
                }
                );
            }
        }, this)
    },
    
    submitstore: function(type) {
        var selectedRec = null;
        var selRec = this.sm2.getSelections();
        selectedRec = selRec[0];
        if(type == 'Edit' || type == 'Clone') {
            if(selRec.length == 0) {
                return;
            } else if(selRec.length > 1) {
                return;
            }
        }
        
        
        this.win=Wtf.getCmp('StoreformId'+type+this.id);
        
        if(this.win==null || this.win==undefined){
            this.win=new Wtf.LocationaddWindow({
                title: (type == 'Add' ? WtfGlobal.getLocaleText("acc.common.add") : WtfGlobal.getLocaleText("acc.common.edit")) + " " + WtfGlobal.getLocaleText("acc.common.location"),
                floating: true,
                closable : true,
                id:'StoreformId'+type+this.id,
                modal: true,
                autoShow: true,
                iconCls: 'win',
                storerec: selectedRec,
                width: 420,
                action: type,
                //orderStore: this.storesds,
                height: 470,
                layout: 'fit',
                //createFor: 'Group',
                createFlag: true,
                resizable: false,
                autoScroll:true
            }).show();
        }else{
            this.win.show();   
        }
        
    }
    
      
    
      
});




/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


Wtf.LocationaddWindow = function(config){
    Wtf.apply(this, config);
    
    Wtf.LocationaddWindow.superclass.constructor.call(this, {
        closable : true,
        modal : true,
        //        iconCls : 'adminWinImg',
        width : 500,
        height:250,
        resizable :false,
        buttonAlign : 'right',
        //layout: 'border',
        buttons :[{
            text : WtfGlobal.getLocaleText("acc.common.saveBtn"),
            scope: this,
            handler:function(){
                this.checkBeforeSave();
            //                this.saveServiceDetails();
            }
        },{
            text : WtfGlobal.getLocaleText("acc.common.cancelBtn"),
            scope: this,
            handler:function(){
                this.close();
            },
            disabled:false
        }]
       
    }
    )
};

Wtf.extend(Wtf.LocationaddWindow, Wtf.Window, {
    onRender: function(config){
        Wtf.LocationaddWindow.superclass.onRender.call(this, config);
 
        this.storeRec = new Wtf.data.Record.create([
        {
            name: 'store_id'
        },
        {
            name: 'fullname'
        },
        {
            name: 'abbr'
        }
        ]);

        this.storeReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:"count"
        },this.storeRec);

        this.Store = new Wtf.data.Store({
            url: 'INVStore/getStoreList.do',
            reader:this.storeReader
        });
        this.Store.load({
            params:{
                isActive : "true",
                includeQAAndRepairStore:true,
                isFromLocationMaster:true, //to display all stores in the inventory masters tab
                includePickandPackStore:true
            }
        });
        
        this.locationid=new Wtf.form.TextField({
            name:"locid",
            fieldLabel:"location id",
            hidden:true,
            hideLabel:true
        });
        
        this.location=new Wtf.form.TextField({
            name:"location",
            fieldLabel: WtfGlobal.getLocaleText("acc.common.locationname") + "*",
            allowBlank:false,
            width:200
        //hideLabel:true
        });
        
        this.storeCombo= new Wtf.common.Select({
            width:200,
            fieldLabel: WtfGlobal.getLocaleText("acc.common.stores") + "*",
            name:'Stores',
            store:this.Store,
            //hiddenName:'approver',
            xtype:'select',
            selectOnFocus:true,
            forceSelection:true,
            multiSelect:true,
            displayField:'fullname',
            valueField:'store_id',
            mode: 'local',
            //allowBlank:false,
            triggerAction:'all',
            typeAhead: true,
            allowBlank:false
        }),
        
        this.Store.on("load", function(){
            if(this.action=="Add"){
                this.storeCombo.setValue("");
            }else{
                this.storeCombo.setValue(this.storerec.get("storeids"));
            }
        },this);
        
        this.locationInfo = new Wtf.form.FormPanel({
            url:"INVLocation/addOrUpdateLocation.do", 
            waitMsgTarget: true,
            method : 'POST',
            autoHeight:true,
            autoScroll:true,
            bodyStyle: "background-color: #f1f1f1;",
            border:false,
            layoutConfig: {
                deferredRender: false
            },
            frame: false,
            labelWidth:150,
            items: [this.location,this.storeCombo
            ]
        });
        
        if(this.action=="Edit"){
            this.location.setValue(this.storerec.get("name")); 
            this.locationid.setValue(this.storerec.get("id"));
            
            this.unselecedStore="";
            this.storeCombo.on('unselect',function(vw,index){
                this.selectedIndex=this.storeCombo.selectedIndex;
                this.unselecedStore+=index.data.store_id+",";
            },this);
            this.storeCombo.on('clearval',function(vw,index){
                this.unselecedStore=this.storerec.get("storeids")+",";
            },this);
            this.storeCombo.on('select',function(vw,index){
                if(this.unselecedStore.indexOf(index.data.store_id)!=-1){
                    this.unselecedStore=this.unselecedStore.replace(index.data.store_id,'');
                }
            },this);
        }
        
       
        
        this.assignTeamPanel = new Wtf.Panel({
      
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
								<b >' + (this.action == 'Add' ? WtfGlobal.getLocaleText("acc.common.addlocation") : WtfGlobal.getLocaleText("acc.common.editlocation")) + '</b></div>\n\
								<div style="margin: 15px 0px 10px 10px; font-size: 10px; float: left; width: 100%; position: relative;" >' + (this.action == 'Add' ? WtfGlobal.getLocaleText("acc.common.location.addnew") : WtfGlobal.getLocaleText("acc.common.loc.edit")) + '\n\
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
                    items : [
                    this.locationInfo
                        
                    ]
                }]
                
            }]
        });


        this.add(this.assignTeamPanel);  

    },
    checkBeforeSave: function() {
        if(this.action == 'Edit'){
            Wtf.Ajax.requestEx(
            {
                url:  'INVStockLevel/getAvailableQtyByStoreLocation.do',
                params: {
                    storeid: this.unselecedStore,
                    locationid: this.storerec.get("id")
                }
            },
            this,
            function(action, response){
                if(action.success == true){
                    var isUsed=action.data[0].isUsed;
                         
                    if(isUsed == true){
                        //                            vw.select(this.selectedIndex, true);
                        //                            this.storeCombo.collapse();
                        this.close();
                        WtfComMsgBox(["Info", action.msg],0);
                        return false;
                    }else{
                        this.saveServiceDetails();
                    }
                        
                        
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.common.error.occurred")],0);
                    return false;
                }
                    
            },
            function(){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.common.error.occurred")],0);
                return false;
            }
            );
        }else{
            this.saveServiceDetails();
        }
       

    },
   
    saveServiceDetails: function() {
        if(!this.locationInfo.form.isValid()) {
            // WtfComMsgBox(["Error",'Please enter mandatory fields.'],0);
            return false;
        } 
        var loc=this.location.getValue();
        if (loc.replace(/\s+/g, '')=== "") {
            /*While creating location and if user user enter only space then system should not allow to save record*/
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleaseenternew")+ ' '+WtfGlobal.getLocaleText("acc.contractActivityPanel.Location") +'.'], 2);
            return;
        }
        if(this.action == 'Add'  || this.action == 'Edit'){
            this.buttons[0].disable();
            this.locationInfo.form.submit({
                
                //url : "INVStore/addOrUpdateLocation" ,
                params: {
                    action:this.action,
                    location:loc,
                    locid: this.action == 'Edit'?this.storerec.get("id"):'',
                    store: this.storeCombo.getValue()
                },
                method: 'POST',
                success: function(result,action){
                    //                 this.loadUpdateCompanyMask.hide();
                    if(action.result.data.success){
                        WtfComMsgBox(["Success", action.result.data.msg],0);
                        if(Wtf.getCmp("location1")!= undefined){
                            Wtf.getCmp("location1").getStore().reload();
                        }
                        if(this.locStore != undefined){
                            this.locStore.reload();
                        }
                        this.close();
                    }else{
                        this.buttons[0].enable();
                        WtfComMsgBox(["Failure", action.result.data.msg],2);
                    }
                    
                },
                failure: function(frm, action){
                //                    this.loadUpdateCompanyMask.hide();
                //                    if(action.failureType == "client")
                //                        WtfComMsgBox(19, 1);
                //                    else{
                //                        var resObj = eval( "(" + action.response.responseText + ")" );
                //                        WtfComMsgBox(20, 1);
                //                    }
                },
                scope:this
           
       
            },
            this
        
            )
          
        }
    }
       
        
    

});

