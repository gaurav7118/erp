function callStoreMaster(){
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.inventorymasters, Wtf.Perm.inventorymasters.storemaster)) {
        var storeMasterGrid=Wtf.getCmp("storeMasterGrid");
        if(storeMasterGrid==null){
            storeMasterGrid = new Wtf.storeMasterGrid({
                id: "storeMasterGrid",
                border : false,
                title : WtfGlobal.getLocaleText("acc.lp.storemaster"), //Changed
                showArchive:true,
                layout : 'fit',
                style:'backgroud-color:white',
                closable: true,
                modal:true,
                iconCls:getButtonIconCls(Wtf.etype.product)
            // iconCls: getTabIconCls(Wtf.etype.leaveshome)
            });
            Wtf.getCmp('as').add(storeMasterGrid);

        }
        Wtf.getCmp('as').setActiveTab(Wtf.getCmp("storeMasterGrid"));
        Wtf.getCmp('storeMasterGrid').doLayout();
    }else{
        WtfComMsgBox(46,0,false,WtfGlobal.getLocaleText("acc.common.viewing")+" "+"this feature");
    }
}

Wtf.storeMasterGrid = function(config){
    Wtf.storeMasterGrid.superclass.constructor.call(this,config);
};

Wtf.extend(Wtf.storeMasterGrid, Wtf.Panel,{
    onRender : function(config){
        Wtf.storeMasterGrid.superclass.onRender.call(this,config);

        this.sm2 = new Wtf.grid.CheckboxSelectionModel({
            width:25
        });
        this.storesRecord = Wtf.data.Record.create([
        {
            name: 'store_id'
        },

        {
            name: 'description'
        },

        {
            name: 'code'
        },

        {
            name: 'abbr'
        },

        {
            name: 'address'
        },

        {
            name: 'lastday'
        },

        {
            name: 'storeMan'
        },

        {
            name: 'storetypename'
        },

        {
            name: 'storetypeid'
        },

        {
            name: 'ManagerId'
        },

        {
            name: 'roleid'
        },

        {
            name: 'ccdateallow'
        },

        {
            name:'actstatus'
        },

        {
            name:'smccallow'
        },

        {
            name:'locationid'
        },

        {
            name:'locationname'
        },

        {
            name:'contact'
        },

        {
            name:'fax'
        },

        {
            name:'mappedLocations'
        },
        {
            name:'userids'
        },
        {
            name:'users'
        },
        {
            name:'executives'
        },
        {
            name:'executiveids'
        },
        {
            name:'defaultlocation'
        },
        {
            name:'defaultlocationid'
        },
        {
            name:'movementtype'
        },
        {
            name:'vattinnumber'
        },
        
        {
            name:'csttinnumber'
        }

        ]);

        this.groupingView = new Wtf.grid.GroupingView({
            forceFit: false,
            showGroupName: false,
            hideGroupedColumn: false
        });

        this.storesds = new Wtf.data.GroupingStore({
            //            sortInfo: {
            //                field: 'description',
            //                direction: "ASC"
            //            },
            url:  'INVStore/getStoreList.do',
            baseParams:{
                includeQAAndRepairStore: true,
                includePickandPackStore:true,
                isFromStoreMaster:true //to display all stores in inventory masters tab 
            },
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root:'data'
            }, this.storesRecord)
        // remoteSort :true
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
            fieldLabel:"Movement Type",
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
        var cmDefaultWidth = 160;
        this.cm = new Wtf.grid.ColumnModel([ this.sm2 ,
        //            new Wtf.KWLRowNumberer({
        //                width:25
        //            }),

        {
                header: 'Description',
                dataIndex: 'description',
                id:'description',
                groupable: true
            },{
                header: 'Analysis Code',
                dataIndex: 'code',
                groupable: true,
                hidden:true
            },{
                header: 'Code',
                dataIndex: 'abbr',
                groupable: true,
                width: cmDefaultWidth
            },{
                header: 'Address',
                dataIndex: 'address',
                groupable: true,
                width: cmDefaultWidth
            },{
                header: 'Default Location',
                dataIndex: 'locationname',
                groupable: true,
                hidden:true,
                width: cmDefaultWidth
            },{
                header: 'Locations',
                dataIndex: 'mappedLocations',
                groupable: true,
                sortable:false,
                width: cmDefaultWidth,
                hidden:true,
                renderer: function(v){
                    return "<div wtf:qtip=\""+ v +"\" wtf:qtitle=\"Locations\">"+v+"</div>";
                }
            },{
                header: 'Contact No',
                dataIndex: 'contact',
                groupable: true,
                width: cmDefaultWidth
            },{
                header: 'Fax No',
                dataIndex: 'fax',
                groupable: true,
                width: cmDefaultWidth
            },{
                header: 'Store Type',
                dataIndex: 'storetypename',
                groupable: true,
                width: cmDefaultWidth
            },{
                header: 'Last Day of Week',
                dataIndex: 'lastday',
                groupable: true,
                hidden:true,
                width: cmDefaultWidth,
                renderer: function(val) {
                    return Date.parseDate("2009-08-16", "Y-m-d").add(Date.DAY, parseInt(val) - 1).format('l');
                }
            },{
                header:"Active",
                //sortable:true,
                dataIndex:'actstatus',
                autoWidth:true,
                align:'center',
                renderer:function(value){
                    if(value=="1"){
                        return "<label style = 'color : green;'>Yes</label>";
                    }else{
                        return "<label style = 'color : red;'>No</label>";
                    }
                }
            },{
                header: 'Cycle Count Date Check',
                dataIndex: 'ccdateallow',
                groupable: true,
                hidden:true,
                width: cmDefaultWidth,
                renderer: function(val) {
                    return val==true ?'Enable':'Disable';
                }
            }
            ,{
                header: 'Previous Cycle Count Check',
                dataIndex: 'smccallow',
                groupable: true,
                hidden:true,
                width: cmDefaultWidth,
                renderer: function(val) {
                    return val==true ?'Enable':'Disable';
                }
            }
            ,{
                header: 'Default Location',
                dataIndex: 'defaultlocation',
                groupable: true,
                width: cmDefaultWidth
               
            }
            ,{
                header: 'Movement Type',
                dataIndex: 'movementtype',
                groupable: true,
                width: cmDefaultWidth,
                renderer:WtfGlobal.getSelectComboRenderer(this.movementTypeCombo),
                hidden:Wtf.account.companyAccountPref.isMovementWarehouseMapping == true?false:true
               
            }
            ,{
                header: 'Store Managers',
                dataIndex: 'users',
                groupable: true,
                width: cmDefaultWidth
               
            }
            ,{
                header: 'Store Executives',
                dataIndex: 'executives',
                groupable: true,
                width: cmDefaultWidth
               
            }
            ,{
                header: 'VAT TIN Number',
                dataIndex: 'vattinnumber',
                groupable: true,
                width: cmDefaultWidth,
                hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,            
                fixed:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA               
            }
            ,{
                header: 'CST TIN Number',
                dataIndex: 'csttinnumber',
                groupable: true,
                width: cmDefaultWidth,
                hidden:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA,
                fixed:Wtf.account.companyAccountPref.countryid!=Wtf.Country.INDIA
            }
            
            ]);
        this.cm.defaultSortable = true;

        this.storesds.load({
            params: {
                start:0,
                limit:30
            }
        });

        this.createGridButtons();

        this.moduleName = "Store Master";
        var extraConfig = {};
        //        extraConfig.url= "INVStore/importWarehouseData.do";
        extraConfig.url= "INVStore/importWarehousRecords.do";
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

        TbarButtonArray.push("-");
        TbarButtonArray.push(this.newBtn);
        TbarButtonArray.push("-");
        TbarButtonArray.push(this.editBtn);
        TbarButtonArray.push("-");
        TbarButtonArray.push(this.statusButton);
        TbarButtonArray.push("-");
        TbarButtonArray.push(this.defaultLocationBtn);
        
        var BbarButtonArray = new Array();
        //        TbarButtonArray.push("-");
        //        TbarButtonArray.push(this.duplicateAssetBtn);
        //TbarButtonArray.push("-");
        //TbarButtonArray.push(this.countCalBtn);
        //TbarButtonArray.push("-");
        //TbarButtonArray.push(this.MapLocationsBtn);
        BbarButtonArray.push(this.importBtn);
        this.grid1 = new Wtf.KwlEditorGridPanel({
            border: false,
            id:'invstoremastergrid',
            store: this.storesds,
            cm: this.cm,
            sm: this.sm2,
            loadMask : true,
            layout:'fit',
            viewConfig: {
                forceFit: false
            },
            split: true,
            region: 'center',
            view: this.groupingView,
            displayInfo:true,
            searchLabel:"Quick Search",
            searchEmptyText:"Search by Description ",
            serverSideSearch:true,
            tbar:TbarButtonArray,
            bbar:BbarButtonArray
        });

        this.storesDetailPanel = new Wtf.Panel({
            id: 'storesDetailPanel'+this.id,
            region: 'north',
            hidden:true,
            border: false,
            height:'200',
            bodyStyle: {
                background: '#ffffff',
                padding: '7px'
            }
        });

        this.innerpanel = new Wtf.Panel({
            id : 'innerpanel'+this.id,
            layout : 'border',
            cls : 'backcolor',
            border : false,
            viewConfig: {
                forceFit: true
            },
            items:[this.grid1
            ,this.storesDetailPanel]
        });
        
        Wtf.getCmp("paggintoolbar"+this.grid1.id).on('beforerender',function(){
                Wtf.getCmp("paggintoolbar"+this.grid1.id).pageSize=30
        },this);
         
        this.storesds.on('loadexception', function(store) {
            if(store.getTotalCount()) {
                store.removeAll();
            }
        }, this);

        this.remarks = "";
        this.add(this.innerpanel);

        this.sm2.on("selectionchange",function(sm) {
            this.markValid.disable();
            this.markInvalid.disable();
            this.MapLocationsBtn.disable();
            if(sm.getSelections().length == 1) {
                if(sm.getSelected().get("actstatus")=="1"){
                    this.markValid.disable();
                    this.markInvalid.enable();
                }else{
                    this.markValid.enable();
                    this.markInvalid.disable();
                }
                this.MapLocationsBtn.enable();
            }
            if(sm.getSelections().length > 1) {
                this.editBtn.disable();
                this.duplicateAssetBtn.disable();
            } else {
                this.editBtn.enable();
                this.duplicateAssetBtn.enable();
            }
        },this);
    },
    getComboRenderer : function(combo){
        return function(value) {
            var idx = combo.store.find(combo.valueField, value);
            if(idx == -1){
                idx = combo.store.find(combo.displayField, value);
            }
            if(idx == -1)
                return value;
            var rec = combo.store.getAt(idx);
            var valueStr = rec.get(combo.displayField);
            return "<div wtf:qtip=\""+valueStr+"\">"+valueStr+"</div>";
        }
    },
    createGridButtons: function() {
        this.newBtn = new Wtf.Button({
            anchor : '90%',
            text: 'New Store',
            iconCls :getButtonIconCls(Wtf.etype.add),
            id: 'newStore',
            tooltip: {
                text: "Click to add new store"
            },
            handler: function(){
                this.submitstore('Add');
            },
            scope:this
        });

        this.editBtn = new Wtf.Button({
            anchor : '90%',
            text: 'Edit Store',
            iconCls :getButtonIconCls(Wtf.etype.edit),
            tooltip: {
                text: "Click to edit existing store"
            },
            handler: function(){
                if(this.sm2.getSelections().length==1){
                    this.submitstore('Edit');
                } else {
                    WtfComMsgBox(["Alert", "Please select a Store to edit."],3);
                    return;
                }
            },
            scope:this
        });
        this.defaultLocationBtn = new Wtf.Button({
            anchor : '90%',
            text: 'Set Default Location',
            iconCls :getButtonIconCls(Wtf.etype.edit),
            tooltip: {
                text: "Click to Set Default Location"
            },
            handler: function(){
                if(this.sm2.getSelections().length==1){
                    var record = this.sm2.getSelections();
                    this.setDefaultLocationWin(record);
                }
            },
            scope:this
        });
        this.duplicateAssetBtn = new Wtf.Button({
            anchor : '90%',
            text: 'Clone Store',
            id: 'Clone' + this.id,
            tooltip: {
                text: "Click to create new Store from existing asset"
            },
            handler: function(){
                if(this.sm2.getSelections().length==1){
                    this.submitstore('Clone');
                } else {
                    //  msgBoxShow(["Alert", "Please select a Store to Clone."], 1);
                    return;
                }
            },
            scope:this
        });
        this.countCalBtn = new Wtf.Button({
            anchor: '90%',
            text: 'Cycle Count Calendar',
            //            iconCls: 'pwnd Editicon',
            tooltip: {
                text: "Click to manage Cycle Count Calendar"
            },
            handler: function() {
                var mainTabId = Wtf.getCmp("as");
                var cycleCountCalendarTab = Wtf.getCmp("cycleCountCalendarTab");
                if(cycleCountCalendarTab == null) {
                    cycleCountCalendarTab = new Wtf.cycleCountCalendarTab({
                        title: "Cycle Count Calendar",
                        id: "cycleCountCalendarTab",
                        //                        iconCls: 'iconwin',
                        closable: true,
                        border: false,
                        layout: "fit"
                    });
                    mainTabId.add(cycleCountCalendarTab);
                }
                mainTabId.setActiveTab(cycleCountCalendarTab);
                mainTabId.doLayout();
            },
            scope:this
        });
        this.markValid = new Wtf.menu.Item({
            text:'Activate Store',
            tooltip :'Validates a store',
            scope: this,
            iconCls:getButtonIconCls(Wtf.etype.activate),
            disabled:true,
            handler:function(){
                this.setValidityStatus(1,this.storesds);
            }
        });

        this.markInvalid = new Wtf.menu.Item({
            text:'Deactivate Store',
            tooltip :'Invalidates a store',
            scope: this,
            disabled:true,
            iconCls:getButtonIconCls(Wtf.etype.deactivate),
            handler:function(){
                this.setValidityStatus(0,this.storesds);
            }
        });
        
        this.statusButton = new Wtf.Toolbar.Button({
            text:'Store Status',
            iconCls:'pwnd updateStatus',
            scope: this,
            tooltip:{
                text:'Click to Manage store Status'
            },
            menu:[
            this.markValid,
            this.markInvalid
            ]
        });
        this.MapLocationsBtn = new Wtf.Button({
            text:'Map Locations',
            tooltip :{
                text:' Click to map Locations with Store'
            },
            scope: this,
            disabled:true,
            handler: this.mapLocationHandler
        });

    },
    setValidityStatus:function(mode,storesds){
        if(this.sm2.getCount()==1){
            var msg = null;
            if(mode==1){
                msg = "Are you sure you want to activate the selected store?";
            }else{
                msg = "Are you sure you want to deactivate the selected store?";
            }
            Wtf.MessageBox.confirm('Confirm', msg, function (btn) {
                if (btn == "yes") {
                    Wtf.Ajax.requestEx({
                        url: mode == 1 ? 'INVStore/activateStore.do' : 'INVStore/deactivateStore.do',
                        params: {
                            id: this.sm2.getSelections()[0].get("store_id"),
                            mode: mode
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
            Wtf.Msg.alert('Alert', 'Please select a single record',function(btn){
                },this);
        }

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
        
        
        //        Wtf.override(Wtf.form.Field, {
        //            alignErrorIcon : function(){
        //                if(this.el.dom) {
        //                    this.errorIcon.alignTo(this.el, 'tl-tr', [2, 0]);
        //                }
        //            }
        //        });  
        this.win=Wtf.getCmp('StoreformId'+type+this.id);
        
        if(this.win==null || this.win==undefined){
            this.win=new Wtf.exchangeRecordsGrid({
                title: type + " Store",
                floating: true,
                closable : true,
                id:'StoreformId'+type+this.id,
                modal: true,
                autoShow: true,
                iconCls: 'win',
                storerec: selectedRec,
                width: 420,
                action: type,
                orderStore: this.storesds,
                height: 470,
                layout: 'fit',
                //createFor: 'Group',
                createFlag: true,
                resizable: false,
                autoScroll:true,
                parentStore:this.storesds,  //dummy added to avoid js error
                hideParentCmb:true
            }).show();
        }else{
            this.win.show();   
        }
        
    },
        
    mapLocationHandler: function(){
        if(this.grid1.selModel.getCount()==1){
            var rec = this.grid1.selModel.getSelected();
            this.mapLocationWin(this.MapLocationsBtn)
        }else{
        //   Wtf.Msg.alert('Alert', 'Please select a single record', 1)
        }
    },
    mapLocationWin: function(btnObj){
        btnObj.disable();
        var flag = false;
        var rec = this.grid1.selModel.getSelected();
        var imgsrc = "images/createuser.gif";
        var availableds = new Wtf.data.Store({
            url:'jspfiles/inventory/store.jsp',
            reader: new Wtf.data.KwlJsonReader({
                root:'data'
            },['name', 'id', 'isDefault'] ),
            autoLoad : false
        });
        var availablesm = new Wtf.grid.CheckboxSelectionModel();
        var availablecm = new Wtf.grid.ColumnModel([availablesm, {
            header: "Available Locations",
            dataIndex: 'name',
            autoWidth: true
        // sortable: true
        }
        ]);
        var availablegrid = new Wtf.grid.GridPanel({
            store: availableds,
            cm: availablecm,
            sm : availablesm,
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: true
            }
        });
        availableds.on("loadexception", function(){
            if (!flag){
                msgBoxShow(11, 1);
                var _cmp = Wtf.getCmp("MapLocationWithStoreWin");
                if(_cmp !== undefined)
                    _cmp.close();
                flag = true;
            }
        });
        //        availableds.load({
        //            params:{
        //                storeId: rec.get("store_id"),
        //                flag: 116,
        //                mapped: false
        //            }
        //        });
        var selectedds = new Wtf.data.Store({
            url:'jspfiles/inventory/store.jsp',
            reader: new Wtf.data.KwlJsonReader({
                root:'data'
            },['name', 'id', 'isDefault']),
            autoLoad : false
        });
        var selectedsm = new Wtf.grid.CheckboxSelectionModel();
        var selectedcm = new Wtf.grid.ColumnModel([selectedsm, {
            header: "Allocated Locations",
            dataIndex: 'name',
            autoWidth: true
        // sortable: true
        }
        ]);
        var selectedgrid = new Wtf.grid.GridPanel({
            store: selectedds,
            cm: selectedcm,
            sm : selectedsm,
            border: false,
            loadMask: true,
            viewConfig: {
                forceFit: true
            }
        });
        selectedds.on("load", function(obj, recs){
            var gview = selectedgrid.getView();
            for(var cnt=0; cnt < recs.length; cnt++){
                if(recs[cnt].get('isDefault') == true)
                    Wtf.get(gview.getRow(cnt)).addClass("parentRow");
            }
        }, this);
        selectedds.on("loadexception", function(){
            if(!flag){
                msgBoxShow(11, 1);
                var _cmp = Wtf.getCmp("MapLocationWithStoreWin");
                if(_cmp !== undefined)
                    _cmp.close();
                flag = true;
            }
        });
        selectedgrid.on("sortchange",function(th){
            var gview =th.getView();
            var sm=th.getStore();
            for(var cnt=0; cnt < sm.getCount(); cnt++){
                if(sm.getAt(cnt).get('isDefault') == true)
                    Wtf.get(gview.getRow(cnt)).addClass("parentRow");
            }
        });
        //        selectedds.load({
        //            params:{
        //                storeId: rec.get("store_id"),
        //                flag: 116,
        //                mapped: true
        //            }
        //        });
        var manageWin = new Wtf.mappingWindow({
            headerCont:getTopHtml("Map locations with stores", "Map locations with stores",imgsrc),
            selectedgrid: selectedgrid,
            title:"Map locations with stores",
            id: "MapLocationWithStoreWin",
            availablegrid: availablegrid
        });
        manageWin.on("okclicked", this.allocateDepartmentWithCostCenter, this);
        manageWin.on("beforeMoveRight", function(selRecs){
            for(var cnt=0; cnt < selRecs.length; cnt++){
                if(selRecs[cnt].get('isDefault') == true){
                    msgBoxShow(["Map locations", "You cannot drop store default location from store."], 2);
                    return false;
                }
            }
            return true;
        }, this);
        manageWin.on("close", function(){
            btnObj.enable();
        }, this);
        manageWin.show();
    },
    
    allocateDepartmentWithCostCenter: function(window, availableStore, selectedStore){
        var locationArr = new Array();
        selectedStore.each(function(rec){
            locationArr.push(rec.get("id"));
        }, this);

        Wtf.Ajax.requestEx({
            url: "jspfiles/inventory/store.jsp",
            params: {
                flag: 117,
                locations: locationArr.toString(),
                storeId: this.grid1.getSelectionModel().getSelected().get('store_id')
            }
        },
        this,
        function(response, requset){
            msgBoxShow(13, 0);
            window.close();
        //  this.grid1.getStore().reload();
        },
        function(response, requset){
            msgBoxShow(14, 1);
            window.close();
        });
    },
    setDefaultLocationWin:function(record){
        new Wtf.DefaultLoationWin({
            id: "defaultlocationwin",
            border : false,
            title : "Set Default Location",
            layout : 'fit',
            closable: true,
            records:record,
            width:450,
            height:300,
            modal:true,
            record:record,
            resizable:false
        }).show();
    }
});
    
// default location window
Wtf.DefaultLoationWin = function (config){
    Wtf.apply(this,config);
    Wtf.DefaultLoationWin.superclass.constructor.call(this,{
        buttons:[
        {
            text:"Save",
            handler:function (){
                this.saveData();
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

Wtf.extend(Wtf.DefaultLoationWin,Wtf.Window,{
    initComponent:function (){
        Wtf.DefaultLoationWin.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        
        this.mainPanel = new Wtf.Panel({
            layout:"border",
            items:[
            this.northPanel,
            this.AddLocationFormatForm
            ]
        });

        this.add(this.mainPanel);
    },
    GetNorthPanel:function (){
        var wintitle = 'Set Default Location';
        var windetail='';
        var image='';
        windetail='Select Location';
        image='images/createuser.png';
        this.northPanel = new Wtf.Panel({
            region:"north",
            height:85,
            border:false,
            bodyStyle:"background-color:white;padding:8px;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml(wintitle,windetail,image)
        });
    },
    GetAddEditForm:function (){
        this.locationRecord = new Wtf.data.Record.create([{
            name: 'id'
        },{
            name: 'name'
        }]);
   
        this.locationReader = new Wtf.data.KwlJsonReader({
            root: 'data'
        }, this.locationRecord);

        this.locationStore = new Wtf.data.Store({
            sortInfo: {
                field: 'name',
                direction: "ASC"
            },
            url:'INVStore/getStoreLocations.do',
            baseParams:{
                storeid:this.record[0].data["store_id"]
            },
            reader: this.locationReader
        });
            this.locationStore.load();
        
        this.locationStore.on('load',function(){
            if(this.record != undefined){
                this.locationCombo.setValue(this.record[0].data.defaultlocationid)
            }
        },this)
        
        this.locationCombo = new Wtf.form.ComboBox({
            mode: 'local',
            triggerAction: 'all',
            hiddenName:"moduleId",
            fieldLabel : 'Location *',
            typeAhead: true,
            width:200,
            allowBlank:false,
            store: this.locationStore,
            displayField: 'name',
            valueField:'id',
            msgTarget: 'side',
            emptyText:"Select Location"
        });
        
        this.AddLocationFormatForm = new Wtf.form.FormPanel({
            region:"center",
            border:false,
            iconCls:'win',
            bodyStyle:"background-color:#f1f1f1;padding:35px",
            url:"INVSeq/addSeqFormat.do",
            labelWidth:110,
            items:[
            this.locationCombo,
            
            ]
        });
        
    },
    saveData:function(){
        var locationId=this.locationCombo.getValue();
        if(this.AddLocationFormatForm.getForm().isValid()){
            Wtf.Ajax.requestEx({
                url:"INVStore/setStoreDefaultLocation.do",
                params: {
                    storeid:this.record[0].data["store_id"],
                    locationId:locationId
                }
            },
            this,
            function(result, req){
                var msg=result.msg;
                var title="Success";
                if(result.success){
                    WtfComMsgBox([title,msg],0);
                    if(Wtf.getCmp("invstoremastergrid"))
                        Wtf.getCmp("invstoremastergrid").getStore().reload();
                    if(Wtf.getCmp("invstoremastergrid_setup"))
                        Wtf.getCmp("invstoremastergrid_setup").getStore().reload();
                    this.close();
                }
                else if(result.success==false){
                    title="Error";
                    WtfComMsgBox([title,"Some Error occurred."],0);
                    return false;
                }
            },
            function(result, req){
                WtfComMsgBox(["Failure", "Some Error occurred."],3);
                return false;
            });
        } else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.store.locationemtymsg")],2);   //Batch and serial no details are not valid.
            this.locationCombo.markInvalid(WtfGlobal.getLocaleText("acc.store.locationemtymsg"));
            return; 
        }
    }
});

//