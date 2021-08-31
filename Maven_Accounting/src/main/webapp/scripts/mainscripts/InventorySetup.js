/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
Wtf.account.inventorysetup = function (config){
    this.uPermType=Wtf.UPerm.masterconfig;
    this.permType=Wtf.Perm.masterconfig;
    this.levels=[];
    this.getLevels();
    Wtf.account.inventorysetup.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true,
        'loadMasterGroup' : true
    });
}

Wtf.extend(Wtf.account.inventorysetup,Wtf.Window,{
    draggable:false,
    initComponent:function (){
        Wtf.account.inventorysetup.superclass.initComponent.call(this);
        this.getMasterGrid();
        this.getMasterItemGrid();
        this.masterStore.on('load',function(){this.MasterItemStore.removeAll();},this);
        this.mainPanel = new Wtf.Panel({
            layout:"border",
            border:false,
            items:[
                 this.gridContainer,{
                    border:false,
                    region:'west',
                    layout:'border',
                    split:true,
                    width:200,
                    items:[
                        this.masterLinks
                    ]
                }
            ]
        });
        this.masterSm.on("selectionchange",function(){
            if(this.masterSm.getSelected()){
                this.MasterItemAdd.enable();
                this.masterEdit.enable();
                var recData = this.masterGrid.getSelectionModel().getSelected().data;
                if(recData.fieldtype!="" && (recData.fieldtype!=4 && recData.fieldtype!=7)) {// 4- drop down; 7-multiselect
                    this.grid.show();
                    this.grid.getView().emptyText = WtfGlobal.getLocaleText("acc.field.Youhaveselectedcustomfieldoftype")+ WtfGlobal.getXType(recData.fieldtype);
                    this.grid.getView().refresh();
                    this.MasterItemGrid.hide();
                    this.MasterItemAdd.disable();
                    this.masterEdit.disable();
                } else if (recData.modulename != "" && recData.modulename != undefined){
                    this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                    this.grid.getView().refresh();
                    this.MasterItemStore.proxy.conn.url = "ACCMaster/getMasterItemsForCustomFoHire.do";
                    this.grid.show();
                    this.MasterItemGrid.hide();
                }
                else{
                    this.grid.hide();
                    this.MasterItemGrid.show();
                    this.MasterItemStore.proxy.conn.url = "ACCMaster/getMasterItems.do";
                }
              
                this.MasterItemStore.load({
                    params:{
                        groupid:recData.id,
                        moduleIds:recData.moduleIds,
                        start : 0,
                        limit : 15
                    }
                });
                WtfComMsgBox(29,4,true);
             } else {
                this.MasterItemAdd.disable();
                this.masterEdit.disable();
            }
            this.changeMsg();
        },this);

        this.add(this.mainPanel);
    },
    getLevels:function(){
        Wtf.Ajax.requestEx({
                url:"ACCMaster/getLocationLevels.do"
              
            },this,function(res){
                 for(var i=0;i<res.data.length;i++){
                     this.levels[i]=res.data[i].levelName;
                 }
                this.linkData = {links:[
                        {fn:"callinvfunction('warehouse')",id:"warehouse",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.inventorysetup.warehouseTTip")+"'>"+this.levels[0]+"</span>",viewperm:true},
                       {fn:"callinvfunction('location')",id:"location",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.inventorysetup.locationmasterTTip")+"'>"+this.levels[1]+"</span>",viewperm:true},
                       {fn:"callinvfunction('row')",id:"row",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.inventorysetup.rowTTip")+"'>"+this.levels[2]+"</span>",viewperm:true},
                       {fn:"callinvfunction('rack')",id:"rack",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.inventorysetup.rackTTip")+"'>"+this.levels[3]+"</span>",viewperm:true},
                       {fn:"callinvfunction('bin')",id:"bin",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.inventorysetup.binTTip")+"'>"+this.levels[4]+"</span>",viewperm:true},
                       {fn:"callinvfunction('departments')",id:"departments",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.text.detartmentsTTip")+"'>"+this.levels[5]+"</span>",viewperm:true}
                ]};
               this.tpl.overwrite(this.masterLinks.body, this.linkData);
            },this.genFailureResponse);
    },
    getMasterGrid:function (){
        this.masterRec = new Wtf.data.Record.create([
            {name:"id"},
            {name:"name"},
            {name:"modulename"},
            {name:"fieldtype"},
            {name:"iscustomfield"},
            {name:"moduleIds"},
            {name:"customcolumn"},
            {name:"allmodulenames"}
        ]); 

        this.masterReader = new Wtf.data.KwlJsonReader({
            root:"data"
        },this.masterRec);

        this.masterStore = new Wtf.data.Store({
//            url: Wtf.req.account+'CompanyManager.jsp',
            url:"ACCMaster/getMasterGroups.do",
            reader:this.masterReader,
            baseParams:{
                mode:111,
                isShowCustColumn:(this.onlyMaster==undefined)?true:false,
                isShowDimensiononly:(this.showonlyDimension && this.showonlyDimension!=undefined)?true:false,
                isShowCustomFieldonly:(this.isShowCustomFieldonly && this.isShowCustomFieldonly!=undefined)?true:false
            }
        });

        this.masterStore.load();

        this.masterColumn = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            {
                header:WtfGlobal.getLocaleText("acc.masterConfig.group"),  //"Master Group",
                sortable:true,
                dataIndex:"name"
            },{
                header:WtfGlobal.getLocaleText("acc.field.id"),
                hidden: true,
                dataIndex: 'id'
            },{
                header:WtfGlobal.getLocaleText("acc.field.FieldType"),
                dataIndex: 'iscustomfield',
                renderer: function(val,meta,rec) {
                    if(rec.data.iscustomfield==1){
                        if( rec.data.customcolumn ==1)
                        return WtfGlobal.getLocaleText("acc.field.LineItem") + WtfGlobal.getXTypeLable(rec.data.fieldtype);
                        return WtfGlobal.getLocaleText("acc.field.CustomField") + WtfGlobal.getXTypeLable(rec.data.fieldtype);
                    } else if(rec.data.iscustomfield==0) {
                       if(val=="-")
                         {return "-"}
                    return WtfGlobal.getLocaleText("acc.field.Dimension") + WtfGlobal.getXTypeLable(rec.data.fieldtype);
                    } 
                 }
            },
            {
            header: WtfGlobal.getLocaleText("acc.designerTemplate.ModuleName"),
            dataIndex: 'allmodulenames',
            renderer: function(val) {
                return "<div wtf:qtip=\""+val+"\">"+val+"</div>";
           }
        }
        ]);

        var masterbtn=new Array();
        //Temp fix to show help message. This needs to be changed.
//        this.hiddenBttn = new Wtf.Toolbar.Button({
//            id:"mastergroup"+this.helpmodeid,
//            disabled:true,
//            scope:this
//        });

        this.masterAdd = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.AddMasterGroup"),
            handler:function (){
                this.AddMaster(false);
            },
            iconCls :getButtonIconCls(Wtf.etype.add),
            scope:this
        });

        this.masterEdit = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.EditMasterGroup"),
            handler:function (){
                this.AddMaster(true);
            },
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.edit),
            disabled:true
        });
        this.masterGrid = new Wtf.grid.GridPanel({
//            id:"mastergroup"+this.helpmodeid,
            stripeRows :true,
            sm:this.masterSm = new Wtf.grid.RowSelectionModel({singleSelect:true}),
            region:"north",
            height : 200,
            store:this.masterStore,
            sortable:true,
            border: false,
            autoScroll:true,
            split: true,
            cm:this.masterColumn,
            loadMask:true,
           viewConfig:{
                forceFit:true
            }
        }); 
      
        this.linkData = {links:[
                       {fn:"callinvfunction('warehouse')",id:"warehouse",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.inventorysetup.warehouseTTip")+"'>"+this.levels[0]+"</span>",viewperm:true},
                       {fn:"callinvfunction('location')",id:"location",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.inventorysetup.locationmasterTTip")+"'>"+this.levels[1]+"</span>",viewperm:true},
                       {fn:"callinvfunction('row')",id:"row",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.inventorysetup.rowTTip")+"'>"+this.levels[2]+"</span>",viewperm:true},
                       {fn:"callinvfunction('rack')",id:"rack",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.inventorysetup.rackTTip")+"'>"+this.levels[3]+"</span>",viewperm:true},
                       {fn:"callinvfunction('bin')",id:"bin",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.inventorysetup.binTTip")+"'>"+this.levels[4]+"</span>",viewperm:true},
                       {fn:"callinvfunction('departments')",id:"departments",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.text.detartmentsTTip")+"'>"+this.levels[5]+"</span>",viewperm:true}
                ]};
            
            if(Wtf.isPMSync){//
                if(this.linkData.links)
                    this.linkData.links.push({fn:"saveWIPAndCPAccountSettings()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.wipcpaccount.setting.qtip")+"'> "+WtfGlobal.getLocaleText("acc.accPref.wipcpaccount.setting")+"</span>",viewperm:true})
            }
        this.tpl = new Wtf.XTemplate(
           '<div class ="dashboardcontent linkspanel" style="float:left;width:100%">',
             '<ul id="accMasterSettingPane">',
             '<tpl for="links">',
                '<tpl if="viewperm">',
                    '<li id = "{id}">',
                        '<a onclick="{fn}" href="#" >{text}</a>',
                     '</li>',
                '</tpl>',
             '</tpl>',
             '</ul>',
           '</div>'
       );
        this.masterLinks = new Wtf.Panel({
            region:"center",
            Title:WtfGlobal.getLocaleText("acc.field.Selecttogetinfo"),
//            id:"paymnetConfigure"+this.helpmodeid,
            bodyStyle:'background:white;',
            layout:'fit',
//            height:500,
            border: false,
            split: true,
            loadMask:true
        });
        if(!this.onlyMaster && !this.showonlyDimension && !this.isShowCustomFieldonly){
            this.masterLinks.on('render', function(){
                this.tpl.overwrite(this.masterLinks.body, this.linkData);
            }, this);
        } 

    },
    getMasterItemGrid:function (){
        this.MasterItemRec = new Wtf.data.Record.create([
         {
            name:"id"
        },

        {
            name:"name"
        },

        {
            name: 'parentid'
        },

        {
            name: 'parentname'
        },
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
            name:'movementtype'  
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
        },
        {
           name:'defaultlocation'
        }
        ]);
        
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
        this.movementtypeStore.load();
        this.movementTypeCombo=new Wtf.common.Select({
            name: "movementtype",
            allowBlank: true,
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
        
        this.MasterItemReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty:'count'
        },this.MasterItemRec);
         this.addParentCmbStore = new Wtf.data.Store({
//            url:Wtf.req.account+'CompanyManager.jsp',
           // url:"INVStore/getStoreList.do",//"INVLocation/getLocations.do",
           url:"ACCMaster/getWarehouseItems.do",
             baseParams: {
                isForCustomer:false
            },
            reader:this.MasterItemReader
        });
           this.addParentCmbStore.on("beforeload",function(){
           // this.addParentCmbStore.proxy.conn.url=this.addParentCmbStore.proxy.conn.url="INVStore/getStoreList.do";
            this.addParentCmbStore.proxy.conn.url="ACCMaster/getWarehouseItems.do";
            if(this.parent=='2'){
                this.addParentCmbStore.proxy.conn.url="INVLocation/getLocations.do";   //"INVStore/getStoreList.do";
            }else if(this.parent=='6'){
                this.addParentCmbStore.proxy.conn.url="ACCMaster/getDepartments.do";
            } else if(this.parent=='3' || this.parent=='4' || this.parent=='5'){
                        var currentBaseParams=this.addParentCmbStore.baseParams;
                        currentBaseParams.transType=this.parent=='3' ? 'row' :(this.parent=='4' ? 'rack' : (this.parent=='5' ? 'bin' : ''))
                        this.addParentCmbStore.baseParams=currentBaseParams;
                        this.addParentCmbStore.proxy.conn.url="ACCMaster/getStoreMasters.do";
            }
            
        },this)
         this.addParentCmbStore.load();
        this.MasterItemStore = new Wtf.data.Store({
//            url:Wtf.req.account+'CompanyManager.jsp',
             url:"INVLocation/getLocations.do",
             baseParams: {
                isForCustomer:false
            },
            reader:this.MasterItemReader
        });
        this.MasterItemStore.on("beforeload",function(){
            this.MasterItemStore.proxy.conn.url="INVLocation/getLocations.do";
            
            if(this.transType==Wtf.warehouse){
                this.MasterItemStore.proxy.conn.url="INVStore/getStoreList.do";
            }else if(this.transType=='batch'){
                
            } else if(this.transType=='serial'){
                
            } else if(this.transType=='departments'){
                this.MasterItemStore.proxy.conn.url="ACCMaster/getDepartments.do";
            } else if(this.transType==Wtf.row || this.transType==Wtf.rack || this.transType==Wtf.bin){
                var currentBaseParams=this.MasterItemStore.baseParams;
                currentBaseParams.transType=this.transType;
                this.MasterItemStore.baseParams=currentBaseParams;
                this.MasterItemStore.proxy.conn.url="ACCMaster/getStoreMasters.do";
            }
            if(this.pP!=undefined){
                if(this.pP.combo.value=="All"){
                    var count = this.MasterItemStore.getTotalCount();
                    var rem = count % 5;
                    if(rem == 0){
                        count = count;
                    }else{
                        count = count + (5 - rem);
                    }
                    this.MasterItemStore.paramNames.limit = count;
                }
            }
        },this)
         this.MasterItemStore.load({
             params : {
               start : 0,
               limit : 15
           }
         });
        var cmDefaultWidth = 150;       
        this.MasterItemStore.on('load',this.handleLoad2, this);
        this.MasterItemSm=new Wtf.grid.CheckboxSelectionModel();
        this.MasterItemColumn = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer({width:20}),
//            this.MasterItemSm,
            {
                header: WtfGlobal.getLocaleText("acc.invset.header.1"), //Name
                sortable:true,
                width: cmDefaultWidth,
                dataIndex:"name",
                hidden:true,
                fixed:true
            },{
                header: WtfGlobal.getLocaleText("acc.field.id"),
                hidden: true,
                width: cmDefaultWidth,
                dataIndex:'id',
                fixed:true
            },                                                      //location header
             {
                header: WtfGlobal.getLocaleText("acc.invset.header.2"),//Location Name
                dataIndex: 'name',
                width: cmDefaultWidth,
                hidden: true,
                fixed:true,
                groupable: true
            },{
                header: WtfGlobal.getLocaleText("acc.invset.header.3"),//Stores
                dataIndex: 'stores',
                hidden: true,
                fixed:true,
                width: cmDefaultWidth
            }
            ,{
                header: WtfGlobal.getLocaleText("acc.invset.header.4"),//Stores Id
                dataIndex: 'storeids',
                fixed:true,
                hidden:true
            }
            ,{
                header:WtfGlobal.getLocaleText("acc.invset.header.5"),//Status
                dataIndex: 'isactive',
                hidden: true,
                fixed:true,
                width: cmDefaultWidth,
                renderer:function(v){
                    if(v == true){
                        return "Active"; 
                    }else{
                        return "Deactive"; 
                    }
                }
            }
                                                             
            ,{
                header: WtfGlobal.getLocaleText("acc.invset.header.6"),//Default Location
                dataIndex:"isdefault",
                hidden: true,
                fixed:true,
                width: cmDefaultWidth,
                renderer:function(v){
                    if(v == true){
                        return "Yes"; 
                    }else{
                        return "No"; 
                    }
                }
      
                
            },                                     //warehouse header
             {
                header: WtfGlobal.getLocaleText("acc.invset.header.7"),//Description
                dataIndex: 'description',
                fixed:true,
//                id:'description',
                groupable: true//,
                 //hidden:true
            },{
                header: WtfGlobal.getLocaleText("acc.invset.header.8"),//Analysis Code
                dataIndex: 'code',
                groupable: true,
                fixed:true,
                hidden:true
            },{
                header: WtfGlobal.getLocaleText("acc.invset.header.9"),//Code
                dataIndex: 'abbr',
                groupable: true,
                fixed:true,
                width: cmDefaultWidth//,
                 //hidden:true
            },{
                header: WtfGlobal.getLocaleText("acc.invset.header.10"),//Address
                dataIndex: 'address',
                groupable: true,
                fixed:true,
                width: cmDefaultWidth//,
                 //hidden:true
            },{
                header: WtfGlobal.getLocaleText("acc.invset.header.6"),//Default Location
                dataIndex: 'locationname',
                groupable: true,
                hidden:true,
                fixed:true,
                width: cmDefaultWidth
            },{
                header: WtfGlobal.getLocaleText("acc.invset.header.11"),//Locations
                dataIndex: 'mappedLocations',
                groupable: true,
                sortable:false,
                width: cmDefaultWidth,
                hidden:true,
                fixed:true,
                renderer: function(v){
                    return "<div wtf:qtip=\""+ v +"\" wtf:qtitle=\"Locations\">"+v+"</div>";
                }
            },{
                header: WtfGlobal.getLocaleText("acc.invset.header.12"),//Contact No
                dataIndex: 'contact',
                groupable: true,
                fixed:true,
                width: cmDefaultWidth//,
                 //hidden:true
            },{
                header: WtfGlobal.getLocaleText("acc.invset.header.13"),//Fax No
                dataIndex: 'fax',
                groupable: true,
                fixed:true,
                width: cmDefaultWidth//,
                 //hidden:true
            },{
                header: WtfGlobal.getLocaleText("acc.invset.header.14"),//Store Type
                dataIndex: 'storetypename',
                groupable: true,
                fixed:true,
                width: cmDefaultWidth//,
//                 hidden:true
            },{
                header: WtfGlobal.getLocaleText("acc.invset.header.15"),//Last Day of Week
                dataIndex: 'lastday',
                groupable: true,
                hidden:true,
                fixed:true,
                width: cmDefaultWidth,
                renderer: function(val) {
                    return Date.parseDate("2009-08-16", "Y-m-d").add(Date.DAY, parseInt(val) - 1).format('l');
                }
            },{
                header:WtfGlobal.getLocaleText("acc.invset.header.16"),//Active
                //sortable:true,
                dataIndex:'actstatus',
                autoWidth:true,
                fixed:true,
                align:'center',
                renderer:function(value){
                    if(value=="1"){
                        return "<label style = 'color : green;'>Yes</label>";
                    }else{
                        return "<label style = 'color : red;'>No</label>";
                    }
                }
//                 hidden:true
            },{
                header: WtfGlobal.getLocaleText("acc.invset.header.17"),//Cycle Count Date Check
                dataIndex: 'ccdateallow',
                groupable: true,
                hidden:true,
                fixed:true,
                width: cmDefaultWidth,
                renderer: function(val) {
                    return val==true ?'Enable':'Disable';
                }
            }
            ,{
                header: WtfGlobal.getLocaleText("acc.invset.header.18"),//Previous Cycle Count Check
                dataIndex: 'smccallow',
                groupable: true,
                hidden:true,
                fixed:true,
                width: cmDefaultWidth,
                renderer: function(val) {
                    return val==true ?'Enable':'Disable';
                }
            }
            ,{
                header: WtfGlobal.getLocaleText("acc.invset.header.6"),//Default Location
                dataIndex: 'defaultlocation',
                groupable: true,
                fixed:true,
                width: cmDefaultWidth,
                 hidden:true
               
            }
            ,{
                header: WtfGlobal.getLocaleText("acc.invset.header.19"),//Movement Type
                dataIndex: 'movementtype',
                groupable: true,
                fixed:true,
                width: cmDefaultWidth,
                //renderer:this.getComboRenderer(this.movementTypeCombo),
                 renderer:WtfGlobal.getSelectComboRenderer(this.movementTypeCombo),
                hidden:Wtf.account.companyAccountPref.isMovementWarehouseMapping == true?false:true
               
            }
            ,{
                header: WtfGlobal.getLocaleText("acc.invset.header.20"),//Store Managers
                dataIndex: 'users',
                groupable: true,
                width: cmDefaultWidth
//                hidden:true
                
               
            }
            ,{
                header: WtfGlobal.getLocaleText("acc.invset.header.21"),//Store Executives
                dataIndex: 'executives',
                groupable: true,
                width: cmDefaultWidth
//                hidden:true
               
            }
            
        ]);
        
        this.selectionModel = new Wtf.grid.CheckboxSelectionModel();
        this.MasterItemAdd = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.add"),  //"Add",
            id:"addinvItem",
            handler:function (){
//                   (this.transType=='location' ||this.transType=='warehouse' )?this.submitstore('Add'):this.AddMasterItem(false);
                     (this.transType==Wtf.warehouse )?this.submitstore('Add'): ((this.transType==Wtf.location && Wtf.account.companyAccountPref.activateInventoryTab) ? this.submitstore('Add') : this.AddMasterItem(false) )  ;
            },
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.msg14"),  //{text:"Select an entry from master group to add master item.",dtext:"Select an entry from master group to add master item.", etext:"Add new item details to the selected master group."},
            iconCls :getButtonIconCls(Wtf.etype.add),
            scope:this
        });

        this.MasterItemEdit = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.edit"),  //"Edit",
            id:"editinvItem",
            handler:function (){
//                (this.transType=='location' ||this.transType=='warehouse')?this.submitstore('Edit'):this.AddMasterItem(true);
//                (this.transType=='warehouse')?this.submitstore('Edit'):this.AddMasterItem(true);
                  (this.transType==Wtf.warehouse )?this.submitstore('Edit'): ((this.transType==Wtf.location && Wtf.account.companyAccountPref.activateInventoryTab) ? this.submitstore('Edit') : this.AddMasterItem(true) )  ;
            },
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.msg15"),  //{text:"Select a master item to edit.",dtext:"Select a master item to edit.", etext:"Edit selected master item details."},
            iconCls :getButtonIconCls(Wtf.etype.edit),
            disabled:true
        });

          this.MasterItemDelete = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.delete"),  //"Delete Master Item",
            id:"deleteinvItem",
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.msg16"),  //{text:"Select a master item to delete.",dtext:"Select a master item to delete.", etext:"Delete selected master item details."},
            handler:function (){
                this.DeleteMasterItem();
            },
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.deletebutton),
            disabled:true
        });
        
        
//        this.syncPMData = new Wtf.Toolbar.Button({
//            text:WtfGlobal.getLocaleText("acc.field.Sync"),  //"Add Custom Column",
//            id:"syncinvItem",
//            tooltip:WtfGlobal.getLocaleText("acc.field.SyncItemsfromInventory"),  
//             handler:this.syncPMDataConfirm,
//            scope:this,
//            iconCls :getButtonIconCls(Wtf.etype.sync)
//        });
        this.changeLableBtn = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.mastersetting"),  //"change lable ",
            id:"syncinvItem",
            tooltip:WtfGlobal.getLocaleText("acc.field.mastersettingttp"),  
            handler:this.changeLableHandler,
            scope:this
        });

        this.masterItemSearch = new Wtf.KWLTagSearch({
            emptyText:WtfGlobal.getLocaleText("acc.masterConfig.search1"),  //'Search by Master Item...',
            width: 150,
            id:"masteritemsearchid"
        });
        this.markValid = new Wtf.menu.Item({
            text: (this.transType == Wtf.location) ? WtfGlobal.getLocaleText("acc.invsetup.markValid.activate.loc") : WtfGlobal.getLocaleText("acc.invsetup.markValid.activate.store"),
            tooltip: (this.transType == Wtf.location) ? WtfGlobal.getLocaleText("acc.invsetup.markValid.loc.tt") : WtfGlobal.getLocaleText("acc.invsetup.markValid.store.tt"),
            scope: this,
            iconCls:getButtonIconCls(Wtf.etype.activate),
            disabled:true,
            handler:function(){
                this.setValidityStatus(1);
            }
        });

        this.markInvalid = new Wtf.menu.Item({
            text: (this.transType == Wtf.location) ? WtfGlobal.getLocaleText("acc.invsetup.markInvalid.deactivate.loc") : WtfGlobal.getLocaleText("acc.invsetup.markInvalid.deactivate.store"),
            tooltip: (this.transType == Wtf.location) ? WtfGlobal.getLocaleText("acc.invsetup.markInvalid.loc.tt") : WtfGlobal.getLocaleText("acc.invsetup.markInvalid.store.tt"),
            scope: this,
            iconCls:getButtonIconCls(Wtf.etype.deactivate),
            disabled:true,
            handler:function(){
                var rec = this.MasterItemGrid.selModel.getSelected();
                if(rec.get("isdefault")==true){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("inventorySetup.deactivate.default.location")],0)
                }else{
                    this.setValidityStatus(0);
                }
            }
             
        });
        
        this.statusButton = new Wtf.Toolbar.Button({
            text:(this.transType==Wtf.location)?WtfGlobal.getLocaleText("acc.inventorySetup.locationstatus"):WtfGlobal.getLocaleText("acc.inventorysetup.StoreStatus"),
            iconCls:'pwnd updateStatus',
            id:'statusbtn',
            disabled:true,
            scope: this,
            tooltip:{
                text:(this.transType==Wtf.location)?WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp"):WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.Manage.store"),
            },
            menu:[
            this.markValid,
            this.markInvalid
            ],
            handler:function()
            {
                   this.changestatusbutnMsg();
            }
        });
 
       this.defaultLocationBtn = new Wtf.Button({
            anchor : '90%',
            text: WtfGlobal.getLocaleText("acc.invsetup.set.defaultLocation"),
             disabled:true,
             id:'defaultlocationbtn',
            iconCls :getButtonIconCls(Wtf.etype.edit),
            tooltip: {
                text: WtfGlobal.getLocaleText("acc.invsetup.set.defaultLocation.tt")
            },
            handler: function(){
                if(this.MasterItemSm.getSelections().length==1){
                    var record = this.MasterItemSm.getSelections();
                    this.setDefaultLocationWin(record);
                }
            },
            scope:this
        });
        var MasterItembtn=new Array();
        MasterItembtn.push(this.masterItemSearch);
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.create))
            MasterItembtn.push(this.MasterItemAdd);
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit))
            MasterItembtn.push(this.MasterItemEdit);
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.remove))
            MasterItembtn.push(this.MasterItemDelete);
      
        
//         if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.create))
//            MasterItembtn.push(this.syncPMData)

        
        MasterItembtn.push(this.changeLableBtn);
        if(Wtf.account.companyAccountPref.activateInventoryTab){
            MasterItembtn.push(this.statusButton);
            MasterItembtn.push(this.defaultLocationBtn);
        }
        
        MasterItembtn.push("->");
        
    //    MasterItembtn.push(getHelpButton(this,this.helpmodeid));
        
        var MasterItembbarbtn=new Array();
       
        
        this.MasterItemGrid = new Wtf.grid.GridPanel({
            cls:'vline-on',
            sm:this.MasterItemSm,
            store:this.MasterItemStore,
            id:'invstoremastergrid_setup',
//            region:"center",
//            loadMask:true, // Remove double Loading Mask
            border: false,
            split: true,
            cm:this.MasterItemColumn,
           viewConfig:{
                forceFit:false,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
   
            }
        });
        
         this.summary = new Wtf.ux.grid.GridSummary();
        this.gridContainer=new Wtf.Panel({
             region:"center",
             layout:'fit',
             border:false,
             items:[this.MasterItemGrid],
              tbar:MasterItembtn,
              bbar:this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 15,
                    id: "pagingtoolbar" + this.id,
                    store: this.MasterItemStore,
                    searchField: this.masterItemSearch,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
                })
        });
        this.MasterItemSm.on("selectionchange",function (){
//                Wtf.uncheckSelAllCheckbox1(this.MasterItemSm);
                if(this.transType == Wtf.warehouse){
                    WtfGlobal.enableDisableBtnArr(MasterItembtn, this.MasterItemGrid, [2,5,6], [3]);
                }else if(this.transType == Wtf.location){
                    WtfGlobal.enableDisableBtnArr(MasterItembtn, this.MasterItemGrid, [2,5], [3]);
                }else{
                    WtfGlobal.enableDisableBtnArr(MasterItembtn, this.MasterItemGrid, [2], [3]);
                }
            this.changeMsg();
            this.changestatusbutnMsg();
          if(this.MasterItemSm.getSelections().length == 1) {
                if(this.MasterItemSm.getSelected().get("isactive")==true || this.MasterItemSm.getSelected().get("actstatus")=="1"){
                    this.markValid.disable();
                    this.markInvalid.enable();
                }else{
                    this.markValid.enable();
                    this.markInvalid.disable();
                }
             
            }
        },this);
    },
    changeMsg:function(){
        if(this.MasterItemAdd.disabled==false)
            this.MasterItemAdd.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg3"));
            else
                this.MasterItemAdd.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg4"));
            if(this.MasterItemEdit.disabled==false)
                this.MasterItemEdit.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg5"));
            else
                this.MasterItemEdit.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg6"));
            
            if(this.MasterItemDelete.disabled==false)
                this.MasterItemDelete.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg7"));
            else
                this.MasterItemDelete.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg8"));
    },
    handleLoad2:function(store){
        //       Wtf.uncheckSelAllCheckbox1(this.MasterItemSm);
        Wtf.MessageBox.hide();
        store.paramNames.start = 0;
        store.paramNames.limit=(this.pP!=undefined)?this.pP.combo.value:15 ;
        this.masterItemSearch.StorageChanged(store);
        var p = this.pP.combo.value;
        this.masterItemSearch.setPage(p);

    },
    changestatusbutnMsg:function()
    {if(this.transType==Wtf.warehouse){
          this.markValid.setText(WtfGlobal.getLocaleText("acc.invsetup.markValid.activate.store"));
          this.markInvalid.setText(WtfGlobal.getLocaleText("acc.invsetup.markInvalid.deactivate.store"));
          this.markInvalid.setTooltip(WtfGlobal.getLocaleText("acc.invsetup.markInvalid.store.tt"));
          this.markValid.setTooltip(WtfGlobal.getLocaleText("acc.invsetup.markValid.store.tt"));
          
         }
       else
           {
          this.markValid.setText(WtfGlobal.getLocaleText("acc.invsetup.markValid.loc"));
          this.markInvalid.setText(WtfGlobal.getLocaleText("acc.invsetup.markInvalid.deactivate.loc"));
          this.markInvalid.setTooltip(WtfGlobal.getLocaleText("acc.invsetup.markInvalid.loc.tt"));
          this.markValid.setTooltip(WtfGlobal.getLocaleText("acc.invsetup.markValid.loc.tt"));
         
           }
    },
    submitstore: function(type) {
        var selectedRec = null;
        var editParent="";
        var selRec = this.MasterItemSm.getSelections();
        selectedRec = selRec[0];
        if(type == 'Edit' || type == 'Clone') {
            if(selRec.length == 0) {
                return;
            } else if(selRec.length > 1) {
                return;
            }
        }
        
            if(type == 'Edit' && this.MasterItemSm.hasSelection()){
                editParent=selectedRec.data['parentid']
            } 
            
            var MasterItemTempStore = new Wtf.data.Store();
            this.addParentCmbStore.each(function(record) {
                if (type == 'Edit' && selectedRec.data['id'] == record.data['id']) {
                        
                }
                else
                    MasterItemTempStore.add(record.copy());

            });
            if(!this.parent)  this.parent='0';
            if(Wtf.account.companyAccountPref.activateInventoryTab)
                this.parent='0';
            
            var hideParentCmb= this.parent=='0' ? true : false;
            
        
        
        this.win=Wtf.getCmp('StoreformId'+type+this.id);
        if((this.transType==Wtf.location)){
        if(this.win==null || this.win==undefined){
            this.win=new Wtf.LocationaddWindow({
                title: (type == 'Add') ? WtfGlobal.getLocaleText("acc.common.addlocation") : WtfGlobal.getLocaleText("acc.common.editlocation"),
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
                locStore:this.MasterItemStore,
                resizable: false,
                autoScroll:true
               
            }).show();
        }else{
            this.win.show();   
        }}
        if((this.transType==Wtf.warehouse)){
             if(this.win==null || this.win==undefined){
            this.win=new Wtf.exchangeRecordsGrid({
                title: (type == 'Add') ? WtfGlobal.getLocaleText("acc.common.addStore") : WtfGlobal.getLocaleText("acc.common.editStore"),
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
                parentid:editParent,
                parentStore:MasterItemTempStore,
                setStore:this.MasterItemStore,
                hideParentCmb:hideParentCmb
            }).show();
        }else{
            this.win.show();   
        }
        }
    },
      setValidityStatus:function(mode){
        if(this.MasterItemSm.getCount()==1){
            var msg = null;
            if(mode==1){
                msg = WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.activate.location");
            }else{
                msg = WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.deactivate.location");
                
            }
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), msg, function(btn){
                if(btn=="yes"){
                    Wtf.Ajax.request({
                        method: 'POST',
                        url: mode==1?(this.transType==Wtf.location?'INVLocation/activateLocation.do':'INVStore/activateStore.do'):(this.transType==Wtf.location ?'INVLocation/deactivateLocation.do':'INVStore/deactivateStore.do'),
                        params: ({
                            id:this.transType==Wtf.location ?this.MasterItemSm.getSelections()[0].get("id"):this.MasterItemSm.getSelections()[0].get("store_id"),
                            mode:mode
                        //                            flag:106
                        }),
                        scope: this,
                        success: function(result, req){
                            var obj = eval('(' + result.responseText + ')');
                            if(obj.data.success == true){
                                Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), obj.data.msg,function(btn){
                                    this.MasterItemGrid.getStore().reload();
                                },this);
                            }
                            else{
                                Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.warning"), obj.data.msg,function(btn){
                                    this.MasterItemGrid.getStore().reload();
                                },this);
                            }
                            
                        },
                        failure: function(result, req){
                            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.field.Errorconnectingtoserver"),function(btn){
                                },this);
                        }
                    });
                }
            },this);
        }else{
            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invsetup.alert.sel.single.record"),function(btn){
                },this);
        }

    },
       setDefaultLocationWin:function(record){
        new Wtf.DefaultLoationWin({
            id: "defaultlocationwin",
            border : false,
            title : WtfGlobal.getLocaleText("acc.invsetup.set.defaultLocation"),
            layout : 'fit',
            closable: true,
            records:record,
            width:450,
            height:300,
            modal:true,
            record:record,
            resizable:false
        }).show();
    },
     
    AddMaster:function (isEdit){
         if(this.masterSm.hasSelection())
             var rec=this.masterSm.getSelected();
         var tempTxt=WtfGlobal.getLocaleText("acc.field.thenameoftheMasterGroup");
         Wtf.Msg.show({
            title: (isEdit)?WtfGlobal.getLocaleText("acc.field.EditMasterGroup"):WtfGlobal.getLocaleText("acc.field.AddMasterGroup"),
            msg: (isEdit)?WtfGlobal.getLocaleText("acc.common.edit")+tempTxt:WtfGlobal.getLocaleText("acc.field.Enter")+tempTxt,
            value:(isEdit)?rec.data['name']:'',
            prompt:true,
            buttons:{ok:WtfGlobal.getLocaleText("acc.common.saveBtn"),cancel:WtfGlobal.getLocaleText("acc.common.cancelBtn")},
            width: 300,
            fn:this.saveMasterGroup.createDelegate(this,[(isEdit?rec.data['id']:"")],true)
         });
    },
    AddMasterItem:function (isEdit){
        var editParent="";
        var msg="";
        var groupName=(this.transType==Wtf.location)?"Location":((this.transType=='departments')?'Department':((this.transType==Wtf.row)?'Row':((this.transType==Wtf.rack)?'Rack':((this.transType==Wtf.bin)?'Bin': "Warehouse"))));
        if(groupName=="Location" && !Wtf.account.companyAccountPref.isLocationCompulsory){
            msg=WtfGlobal.getLocaleText("acc.select.isLocationCompulsory")
        }if(groupName=="Warehouse" && !Wtf.account.companyAccountPref.isWarehouseCompulsory){
            msg=WtfGlobal.getLocaleText("acc.select.isWarehouseCompulsory")
        }if(groupName=="Row" && !Wtf.account.companyAccountPref.isRowCompulsory){
            msg=WtfGlobal.getLocaleText("acc.select.isRowCompulsory")
        }if(groupName=="Rack" && !Wtf.account.companyAccountPref.isRackCompulsory){
            msg=WtfGlobal.getLocaleText("acc.select.isRackCompulsory")
        }if(groupName=="Bin" && !Wtf.account.companyAccountPref.isBinCompulsory){
            msg=WtfGlobal.getLocaleText("acc.select.isBinCompulsory")
        }
        if(msg!=''){
            Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.field.AlertMessage"),msg);
            return;
        }
        if(isEdit && this.MasterItemSm.getCount()>1){
            Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.field.EditingError"),WtfGlobal.getLocaleText("acc.field.Canteditmultiplerecordssimultaneously"));
            return;
        }else{
            var rec=null;
            if(isEdit && this.MasterItemSm.hasSelection()){
                rec=this.MasterItemSm.getSelected();
                editParent=rec.data['parentid']
            } 
            
            var MasterItemTempStore = new Wtf.data.Store();
            this.addParentCmbStore.each(function(record) {
                if (isEdit && rec.data['id'] == record.data['id']) {
                        
                }
                else
                    MasterItemTempStore.add(record.copy());

            });
            if(!this.parent)  this.parent='0';
            var hideParentCmb=this.parent=='0' ? true : false;
            this.parentCombo = new Wtf.form.ComboBox({
                triggerAction: 'all',
                hidden: false,
                mode: 'local',
                valueField: 'id',
                hidden:hideParentCmb,
                hideLabel:hideParentCmb,
                displayField: 'name',
                store: MasterItemTempStore,
                fieldLabel: WtfGlobal.getLocaleText("acc.field.SelectParent"),
                value: editParent,
                typeAhead: true,
                forceSelection: true,
                hiddenName: 'parent'
            });

            this.addMasterItemForm = new Wtf.form.FormPanel({
                waitMsgTarget: true,
                border: false,
                region: 'center',
                layout:'form',
                bodyStyle: "background: transparent;",
                style: "background: transparent;padding:20px;",
                labelWidth: 107,
                items: [{
                        xtype: 'textfield',
                        fieldLabel: ((isEdit ? WtfGlobal.getLocaleText("acc.common.edit") + ' ' : WtfGlobal.getLocaleText("acc.masterConfig.common.enterNew") + ' ') + localizedgroupName(groupName)),
                        name: "masteritem",
//                        msgTarget: 'under',
//                        style: "margin-left:30px;",
                        width: 200,
                        maxLength:500,
//                        validator:(!this.paidToFlag)?Wtf.ValidateCustomColumnName:"",
                        validator:Wtf.ValidatePaidReceiveName,
                        value:(isEdit)?rec.data['name']:'',
                        allowBlank: false,
                        id: "masteritemname"
                    },this.parentCombo]
            });     
            
            /*this.saveLocationBtn = new Wtf.Button({
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                scope: this,
                handler: function(button) {
                    var itemName=Wtf.getCmp("masteritemname").getValue();
                    var parentId=this.parentCombo.getValue();
                    if (this.addMasterItemForm.form.isValid()) {
                        this.saveMasterGroupItem("ok",itemName,isEdit,(isEdit?rec.data['id']:""),parentId) 
                    } else {
                        return false;
                    }
                }
            });*/
             
            this.addMasterItemWindow = new Wtf.Window({
                modal: true,
                title: localizedgroupName(groupName),
                iconCls :getButtonIconCls(Wtf.etype.deskera),
                bodyStyle: 'padding:5px;',
                buttonAlign: 'right',
                width: 425,
//        height: 115,
                scope: this,
                items: [{
                        region: 'center',
                        border: false,
                        bodyStyle: 'background:#f1f1f1;font-size:10px;',
                        autoScroll: true,
                        items: [this.addMasterItemForm]
                    }],
                buttons: [{
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                scope: this,
                id:'savebtnid',
                handler: function(button) {
                    var itemName=Wtf.getCmp("masteritemname").getValue();
                    var parentId=this.parentCombo.getValue();
                    if (this.addMasterItemForm.form.isValid()) {
                        this.saveMasterGroupItem("ok",itemName,isEdit,(isEdit?rec.data['id']:""),parentId,rec) 
                    } else {
                        return false;
                    }
                }
            }, {
                        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                        scope: this,
                        handler: function() {
                            this.addMasterItemWindow.close();
                        }
                    }]
            });

            this.addMasterItemWindow.show();

            
          /*  Wtf.Msg.show({
                title: groupName,
                width:300,
                msg: ((isEdit?WtfGlobal.getLocaleText("acc.common.edit")+' ':WtfGlobal.getLocaleText("acc.masterConfig.common.enterNew")+' ')+groupName),
                value:(isEdit)?rec.data['name']:'',
                buttons:{ok:WtfGlobal.getLocaleText("acc.common.saveBtn"),cancel:WtfGlobal.getLocaleText("acc.common.cancelBtn")},
                prompt:true,
                fn:this.saveMasterGroupItem.createDelegate(this,[isEdit,(isEdit?rec.data['id']:""),id,outer],true)
            });*/
        }
    },

    saveMasterGroup: function(btn, txt, id){
        if(btn=="ok"&&txt.replace(/\s+/g, '')!=""){
            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+'CompanyManager.jsp',
                url:"ACCMaster/saveMasterGroup.do",
                params: {
                        mode:113,
                        name:txt,
                        id:id
                   }
            },this,this.genSuccessResponse,this.genFailureResponse);
        }
    },
    saveLocationWarehouse: function(btn,isStore){
        if(btn=="ok"){
            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+'CompanyManager.jsp',
                url:"ACCMaster/getAllInventoryStores.do",
                params: {
                        isStore:this.transType==Wtf.warehouse ? true:false
                   }
            },this,this.genSuccessResponse,this.genFailureResponse);
        }
    },

    saveMasterGroupItem: function(btn, txt, isEdit, id, parentId,rec){
        var callUrl ="INVStore/saveStore.do";
        if(this.transType==Wtf.warehouse)
            callUrl ="INVStore/saveStore.do";//"ACCMaster/saveWarehouseItem.do";
	if(this.transType==Wtf.location  )
            callUrl = "ACCMaster/saveLocationItem.do";                    //             callUrl ="INVLocation/addOrUpdateLocation.do";
        else if(this.transType=='departments')
            callUrl = "ACCMaster/saveDepartmentItem.do";
        else if(this.transType==Wtf.row || this.transType==Wtf.rack || this.transType==Wtf.bin)
            callUrl = "ACCMaster/saveStoreMasterItem.do";
        var groupName=(this.transType==Wtf.location)?"Location":((this.transType=='departments')?'Department':((this.transType==Wtf.row)?'Row':((this.transType==Wtf.rack)?'Rack':((this.transType==Wtf.bin)?'Bin': "Warehouse"))));
        var oldName = "";
        if(rec !== null && rec !== "" && rec !== undefined){
            if(isEdit){
                oldName = rec.data['name'];
            }
        }
        if(btn=="ok"){
           if(txt.replace(/\s+/g, '')!=""){
                Wtf.getCmp('savebtnid').disable();
                Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                     url:callUrl,
                    params: {
                        mode:114,
                        name:txt,
                        id:id,
                        parentid:parentId,
                        isEdit:isEdit,
                        groupName:groupName,
                        oldName:oldName  //Added for audit trial entry
                    }
                },this,this.genSuccessResponse,this.genFailureResponse);
           }else{
               Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),  //'Master Configuration',
                    msg: WtfGlobal.getLocaleText("acc.field.Pleaseenternew")+this.masterStore.getAt(this.masterStore.find('id',masterid)).data['name'],
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.INFO,
                    width: 300,
                    scope: this,
                    fn: function(){
                        if(btn=="ok"){
                            this.AddMasterItem(isEdit,masterid,outer);
                        }
                    }
                 });

           }
        } else if(outer){
           this.destroy();
       }
   },
   
   syncPMDataConfirm : function(){
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.field.SyncPMData"),  //'Master Configuration',
            msg: WtfGlobal.getLocaleText("acc.field.AreyousureyouwanttosyncDatafromInventory"),
            buttons: Wtf.MessageBox.YESNO,
            icon: Wtf.MessageBox.INFO,
            width: 300,
            scope: this,
            fn: function(btn){
                if(btn=="yes"){
                    this.saveLocationWarehouse("ok",true);
                }
            }
        });
        
    },
   syncPMDataFn : function(){
     Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                     url:"ACCAccountCMN/saveProjectMasterItemForCustom.do",
                    params: {
                        mode:114,
                        isProject:true
                    }
                },this,function(response){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),response.msg],response.success*2+1);
                },function(){
                    var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
                });  
   },
   
changeLableHandler: function(){
    Wtf.Ajax.requestEx({
        url:"ACCMaster/chkStoreMasterSettingUsed.do"
    },this,function(response){
        if(response.isStoreMasterSettingUsed){
            var levelRec=new Wtf.data.Record.create([
            {name: 'levelId',mapping:'levelId'},
            {name: 'levelName',mapping:'levelName'}
            ]);
            var levelStore=new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                },levelRec),
                url : "ACCMaster/getLevelNames.do"
            });
            levelStore.load();
            var levelsRec=new Wtf.data.Record.create([
            {name: 'Id',mapping:'Id'},
            {name: 'levelName',mapping:'levelName'},
            {name: 'newLevelName',mapping:'newLevelName'},
            {name: 'parent',mapping:'parent'},
            {name: 'isActivate',mapping:'isActivate'},
            ]);
            this.levelsStore=new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                },levelsRec),
                url : "ACCMaster/getLevels.do"
          
            });
            this.levelsStore.load();
            var cmbLevel= new Wtf.form.FnComboBox({
                //            fieldLabel:WtfGlobal.getLocaleText("acc.je.acc"),
                name:'levelId',
                hiddenName:'levelId',
                store:levelStore,
                valueField:'levelId',
                displayField:'levelName',
                mode: 'local',
                disableKeyFilter:true,
                allowBlank:false,
                triggerAction:'all',
                forceSelection:true,
                typeAhead: true,
                hirarchical:true
            });
            var cm=[{
                header: WtfGlobal.getLocaleText("acc.levelsetting.mastername"),  //"Master Name",
                dataIndex: 'levelName'
            },{
                header: WtfGlobal.getLocaleText("acc.levelsetting.newlevelname"),  //"Field Label",
                dataIndex: 'newLevelName',
                editor: new Wtf.form.TextField({
                    name:'newLevelName'
                })
            },{
                header: WtfGlobal.getLocaleText("acc.dimension.parentCmb"),  //"Parent",
                dataIndex: 'parent',
                renderer:Wtf.comboBoxRenderer(cmbLevel),
                editor:cmbLevel
            },
            this.checkColumn=new Wtf.grid.DepCheckColumn({
                header: WtfGlobal.getLocaleText("acc.levelsetting.activatelevel"),  //"Post Depreciation",
                align:'center',
                hidden:true,
                dataIndex: 'isActivate',
                disabled:false,
                width: 80
            })
                
        
            ];
            new Wtf.account.GridUpdateWindow({
                cm:cm,
                store:this.levelsStore,
                record:this.levelsRec,
                title:WtfGlobal.getLocaleText("acc.levelsetting.inventorymastersettingtitle"),  //'Level Setting Window',
                tabTip:WtfGlobal.getLocaleText("acc.levelsetting.inventorymastersettingtitle"),  //'Level Setting Window',
                //            headerImage:"../../images/accounting_image/Payment-Method.gif",
                id:'changeLevel-win'+this.id,
                mode:32,
                levelSetting:true
            }).show();
        }else{
            var msg=WtfGlobal.getLocaleText("acc.inventorySetup.mastersetting.alert"); 
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        }
    },function(){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    });             
},
   DeleteMasterItem:function (){
       if(this.MasterItemSm.hasSelection()){
           var arrID=[];
           var arrName=[];
           var groupName=(this.transType==Wtf.location)?"Location":((this.transType=='departments')?'Department':((this.transType==Wtf.row)?'Row':((this.transType==Wtf.rack)?'Rack':((this.transType==Wtf.bin)?'Bin': "Warehouse"))));
           var rec = this.MasterItemSm.getSelections();
           for(var i=0;i<this.MasterItemSm.getCount();i++){
               if(groupName=='Warehouse'){
                   arrID.push(rec[i].data['store_id']);
                   arrName.push(rec[i].data['abbr'])
               }else{
                arrID.push(rec[i].data['id']);
                arrName.push(rec[i].data['name'])
               }
           }
       }
       
        var callUrl = "";
        callUrl = "ACCMaster/deleteLocationItem.do";
        if(this.transType==Wtf.warehouse)
            callUrl = "ACCMaster/deleteWarehouseItem.do";
        if(this.transType=='departments')
            callUrl = "ACCMaster/deleteDepartmentItem.do";
        if(this.transType==Wtf.row || this.transType==Wtf.rack || this.transType==Wtf.bin)
            callUrl = "ACCMaster/deleteStoreMasterItem.do";
        Wtf.MessageBox.show({
           title: WtfGlobal.getLocaleText("acc.common.warning"),  //"Warning",
           msg: WtfGlobal.getLocaleText("acc.masterConfig.msg1"),  ///+"<div><b>"+WtfGlobal.getLocaleText("acc.masterConfig.msg1")+"</b></div>",
           width: 380,
           buttons: Wtf.MessageBox.OKCANCEL,
           animEl: 'upbtn',
           icon: Wtf.MessageBox.QUESTION,
           scope:this,
           fn:function(btn){
               if(btn=="ok"){
                    Wtf.Ajax.requestEx({
//                        url:Wtf.req.account+'CompanyManager.jsp',
                        url:callUrl,
                        params: {
                                mode:116,
                                ids:arrID,
                                name:arrName,
                                groupName:groupName
                        }
                    },this,this.genDeleteResponse,this.genFailureResponse);
                }
               // this.close();
            }

        });
    },
    deleteDimension: function(masterid) {
        var callUrl = "ACCMaster/deleteDimension.do";
        var id= this.masterGrid.getSelectionModel().getSelected().data["id"];   
        var moduleIds =this.masterStore.getAt(this.masterStore.find('id',id)).data['moduleIds'];
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.warning"), //"Warning",
            msg: WtfGlobal.getLocaleText("acc.masterConfig.msg1"), ///+"<div><b>"+WtfGlobal.getLocaleText("acc.masterConfig.msg1")+"</b></div>",
            width: 380,
            buttons: Wtf.MessageBox.OKCANCEL,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope: this,
            fn: function(btn) {
                if (btn == "ok") {
                    Wtf.Ajax.requestEx({
//                        url:Wtf.req.account+'CompanyManager.jsp',
                        url: callUrl,
                        params: {
                            mode: 116,
                            groupid: masterid,
                            moduleIds:moduleIds
                        }
                    }, this, this.genSuccessResponse, this.genFailureResponse);
                }
                // this.close();
            }

        });
    },
genDeleteResponse:function(response, opt,outer, masterid){
    if(response.success){
        //            WtfComMsgBox([WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),response.msg],response.success*2+1);
        Wtf.Msg.show({
            title:WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),
            msg: response.msg,
            buttons: Wtf.Msg.OK,
            scope:this,
            icon:Wtf.MessageBox.INFO,
            fn: function(){
                getCompanyAccPref();
                this.MasterItemStore.reload();
            }
        });
    }else{
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);      
    }

},
    genSuccessResponse:function(response, opt,outer, masterid){
        Wtf.getCmp('savebtnid').enable();
        if(response.success){
            Wtf.Msg.show({
            title:WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),
            msg: response.msg,
            buttons: Wtf.Msg.OK,
            scope:this,
            icon:Wtf.MessageBox.INFO,
            fn: function(){
                getCompanyAccPref();
                this.MasterItemStore.reload();
                this.fireEvent('update');//alert(opt.params.toSource())
                if(opt.params.toString().indexOf("mode=113")>=0 || opt.url=='ACCMaster/deleteDimension.do') {
                    this.masterStore.load();
                    if(opt.url=='ACCMaster/deleteDimension.do')
                        loadCustomFieldColModel(undefined, '2,4,6,8,10,12,14,16,18,20,24,30'.split(','))
                }
                if(this.addMasterItemWindow) 
                    this.addMasterItemWindow.close();
            }
        });
        }

     },

    genFailureResponse:function(response){
        Wtf.getCmp('savebtnid').enable();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    addMasterItemOuter:function(isEdit,id,outer){
        if(id!=undefined){
            this.transType=id;
        }
        if(outer){
            this.masterStore.on("load",function(){
                this.AddMasterItem(isEdit,id,outer);
            },this);
        }
        else
            this.AddMasterItem(isEdit,id,outer);
}
});



function masterInvoiceTerms(winid){
    if (Wtf.account.companyAccountPref.avalaraIntegration) {
        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.integration.invoiceTermDisabledForAvalaraMsg"));
    } else {
        winid = (winid==null?"masterInvoiceTerms":winid);
        var p = Wtf.getCmp(winid);
        if(!p){
            new Wtf.InvoiceTermWindow({
               id : 'masterInvoiceTerms',
               isSales:true,
               title :WtfGlobal.getLocaleText("acc.master.invoice.salesterm")
            }).show();
        }
    }
}

function callinvfunction(winid){
    document.getElementById(winid).style.fontWeight="bold";
    Wtf.getCmp("inventorysetup").transType=winid;
    Wtf.getCmp("inventorysetup").MasterItemStore.reload();
    Wtf.getCmp("addinvItem").enable();
    Wtf.getCmp("syncinvItem").enable();
    Wtf.getCmp('defaultlocationbtn').disable();
    Wtf.getCmp('statusbtn').disable();
    Wtf.getCmp('masteritemsearchid').reset();
     switch(winid){
        case "location":
//            Wtf.getCmp('statusbtn').enable();
            Wtf.getCmp('statusbtn').setText(WtfGlobal.getLocaleText("acc.inventorySetup.locationstatus"));
            Wtf.getCmp('statusbtn').setTooltip(WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp"));   
            document.getElementById("warehouse").style.fontWeight="normal";
//            document.getElementById("batch").style.fontWeight="normal";
//            document.getElementById("serial").style.fontWeight="normal";
            document.getElementById("row").style.fontWeight="normal";
            document.getElementById("rack").style.fontWeight="normal";
            document.getElementById("bin").style.fontWeight="normal";
            document.getElementById("departments").style.fontWeight="normal";
            this.levelId=2;
//            this.selectedLevelName=thlevels[0];
            break;
        case "warehouse":
//             Wtf.getCmp('statusbtn').enable();
             Wtf.getCmp('statusbtn').setText(WtfGlobal.getLocaleText("acc.inventorysetup.StoreStatus"));
             Wtf.getCmp('statusbtn').setTooltip(WtfGlobal.getLocaleText("acc.inventorySetup.locationStatusttp.Manage.store")); 
//             Wtf.getCmp('defaultlocationbtn').enable();
            document.getElementById("location").style.fontWeight="normal";
//            document.getElementById("batch").style.fontWeight="normal";
//            document.getElementById("serial").style.fontWeight="normal";
            document.getElementById("row").style.fontWeight="normal";
            document.getElementById("rack").style.fontWeight="normal";
            document.getElementById("bin").style.fontWeight="normal";
            document.getElementById("departments").style.fontWeight="normal";
            this.levelId=1;
            break;
        case "row":
            document.getElementById("location").style.fontWeight="normal";
            document.getElementById("warehouse").style.fontWeight="normal";
//            document.getElementById("batch").style.fontWeight="normal";
//            document.getElementById("serial").style.fontWeight="normal";
            document.getElementById("rack").style.fontWeight="normal";
            document.getElementById("bin").style.fontWeight="normal";
            document.getElementById("departments").style.fontWeight="normal";
            this.levelId=3;
        break;
        case "rack":
            document.getElementById("location").style.fontWeight="normal";
            document.getElementById("warehouse").style.fontWeight="normal";
//            document.getElementById("batch").style.fontWeight="normal";
//            document.getElementById("serial").style.fontWeight="normal";
            document.getElementById("row").style.fontWeight="normal";
            document.getElementById("bin").style.fontWeight="normal";
            document.getElementById("departments").style.fontWeight="normal";
            this.levelId=4;
        break;
        case "bin":
            document.getElementById("location").style.fontWeight="normal";
            document.getElementById("warehouse").style.fontWeight="normal";
//            document.getElementById("batch").style.fontWeight="normal";
//            document.getElementById("serial").style.fontWeight="normal";
            document.getElementById("rack").style.fontWeight="normal";
            document.getElementById("row").style.fontWeight="normal";
            document.getElementById("departments").style.fontWeight="normal";
            this.levelId=5;
        break;
        case "batch":
            document.getElementById("location").style.fontWeight="normal";
            document.getElementById("warehouse").style.fontWeight="normal";
         //   document.getElementById("serial").style.fontWeight="normal";
            document.getElementById("departments").style.fontWeight="normal";
            Wtf.getCmp("addinvItem").disable();
            Wtf.getCmp("syncinvItem").disable();
            break;
        case "serial":
            document.getElementById("location").style.fontWeight="normal";
            document.getElementById("warehouse").style.fontWeight="normal";
        //    document.getElementById("batch").style.fontWeight="normal";  
            document.getElementById("departments").style.fontWeight="normal";  
            Wtf.getCmp("addinvItem").disable();
            Wtf.getCmp("syncinvItem").disable();
            break;
        case "departments":
            document.getElementById("location").style.fontWeight="normal";
            document.getElementById("warehouse").style.fontWeight="normal";
//            document.getElementById("batch").style.fontWeight="normal";
//            document.getElementById("serial").style.fontWeight="normal";
            document.getElementById("bin").style.fontWeight="normal";
            document.getElementById("rack").style.fontWeight="normal";
            document.getElementById("row").style.fontWeight="normal";
//            Wtf.getCmp("addinvItem").disable();
            Wtf.getCmp("syncinvItem").disable();
            this.levelId=6;
    }
    var headercount=Wtf.getCmp("inventorysetup").MasterItemGrid.colModel.config.length;
    if(this.levelId==1)  //2
    {
//        Wtf.getCmp('statusbtn').enable();
        for(var colheader=1;colheader<headercount; colheader++)
        {
            var headername=Wtf.getCmp("inventorysetup").MasterItemGrid.colModel.config[colheader].header;     
            var dataindex=Wtf.getCmp("inventorysetup").MasterItemGrid.colModel.config[colheader].dataIndex;
            if (headername == WtfGlobal.getLocaleText("acc.invset.header.7") || headername == WtfGlobal.getLocaleText("acc.invset.header.9") || headername == WtfGlobal.getLocaleText("acc.invset.header.10") || headername == WtfGlobal.getLocaleText("acc.invset.header.12") || headername == WtfGlobal.getLocaleText("acc.invset.header.13") || headername == WtfGlobal.getLocaleText("acc.invset.header.14") || headername == WtfGlobal.getLocaleText("acc.invset.header.16") || headername == WtfGlobal.getLocaleText("acc.invset.header.20") || headername == WtfGlobal.getLocaleText("acc.invset.header.21") || (headername == WtfGlobal.getLocaleText("acc.invset.header.6") && dataindex == 'defaultlocation')) {
                Wtf.getCmp("inventorysetup").MasterItemGrid.colModel.config[colheader].hidden=false;
            }
            else
            {
                Wtf.getCmp("inventorysetup").MasterItemGrid.colModel.config[colheader].hidden=true;
            }
            }
         Wtf.getCmp("inventorysetup").MasterItemGrid.colModel.config[0].width=20;
    }
    else if(this.levelId==3||this.levelId==4||this.levelId==5||this.levelId==6)
    {
        for(var colheader=1;colheader< headercount;colheader++)
        {
            if( Wtf.getCmp("inventorysetup").MasterItemGrid.colModel.config[colheader].header==WtfGlobal.getLocaleText("acc.invset.header.1") )
            {
                Wtf.getCmp("inventorysetup").MasterItemGrid.colModel.config[colheader].hidden=false;
            }
            else
            {
                Wtf.getCmp("inventorysetup").MasterItemGrid.colModel.config[colheader].hidden=true;
            }   
        }
        Wtf.getCmp("inventorysetup").MasterItemGrid.colModel.config[0].width=2.5;
    }
     if(this.levelId==2)//1
    {
//        Wtf.getCmp('statusbtn').enable();
        for(var colheader=1;colheader<headercount;colheader++)
        {var headername=Wtf.getCmp("inventorysetup").MasterItemGrid.colModel.config[colheader].header;
            if(headername==WtfGlobal.getLocaleText("acc.invset.header.2")||headername==WtfGlobal.getLocaleText("acc.invset.header.3")|| headername==WtfGlobal.getLocaleText("acc.invset.header.5")|| (headername=='Default Location' && Wtf.getCmp("inventorysetup").MasterItemGrid.colModel.config[colheader].dataIndex=='isdefault'))
            {
                Wtf.getCmp("inventorysetup").MasterItemGrid.colModel.config[colheader].hidden=false;
            }
            else
            {
                Wtf.getCmp("inventorysetup").MasterItemGrid.colModel.config[colheader].hidden=true;
            } 
        }
        Wtf.getCmp("inventorysetup").MasterItemGrid.colModel.config[0].width=10;
    }
        Wtf.getCmp("inventorysetup").MasterItemGrid.getView().refresh(true); 
        Wtf.Ajax.requestEx({
        //                        url:Wtf.req.account+'CompanyManager.jsp',
            url: "ACCMaster/getLLevelMappingFrmLevlId.do",
            params: {
                levelId:this.levelId
            }
        }, this,function(res){
            if(res.data.length>0){
             Wtf.getCmp("inventorysetup").parent=res.data[0].parent,
             Wtf.getCmp("inventorysetup").addParentCmbStore.reload();
            }
        }, this.genFailureResponse);
}
function masterPurchaseTerms(winid){
    winid = (winid==null?"masterPurchaseTerms":winid);
    var p = Wtf.getCmp(winid);
    if(!p){
        new Wtf.InvoiceTermWindow({
           id : 'masterPurchaseTerms',
           isSales:false,
           title :WtfGlobal.getLocaleText("acc.master.invoice.purchaseterm")
        }).show();
    }
}
function localizedgroupName(groupName) {
    switch (groupName) {
        case  "Warehouse":
            groupName = WtfGlobal.getLocaleText("acc.inv.loclevel.1");
            break;
        case  "Location":
            groupName = WtfGlobal.getLocaleText("acc.inv.loclevel.2");
            break;
        case  "Row":
            groupName = WtfGlobal.getLocaleText("acc.inv.loclevel.3");
            break;
        case  "Rack":
            groupName = WtfGlobal.getLocaleText("acc.inv.loclevel.4");
            break;
        case  "Bin":
            groupName = WtfGlobal.getLocaleText("acc.inv.loclevel.5");
            break;
        case  "Department":
            groupName = WtfGlobal.getLocaleText("acc.inv.loclevel.6");
            break;
        default:
            groupName;
    }
    return groupName;
}