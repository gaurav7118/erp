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
Wtf.account.MasterConfigurator = function (config){
    this.uPermType=Wtf.UPerm.masterconfig;
    this.permType=Wtf.Perm.masterconfig;
    this.isProject=true;
    this.ibgReceivingDetails="";
    this.masterid=config.id;
    this.ibgReceivingDetailsArr=new Array();
    this.ispropagatetochildcompanyflag=false;
    this.isTransationFormEntry = (config.isTransationFormEntry != undefined) ? config.isTransationFormEntry : false;     //ERM-210  added extra check to distinguish [add master item] request between [master configuration] and [transation form]
    this.fieldid = config.fieldid;
    this.parentid = config.parentid;
    this.gstConfigType=config.gstConfigType != undefined?config.gstConfigType:0;  // To provide check for HSN/SAC Code should not be greater than 8 digits (For India) ERM-1092    
    Wtf.account.MasterConfigurator.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true,
        'loadMasterGroup' : true
    });
}

Wtf.extend(Wtf.account.MasterConfigurator,Wtf.Panel,{
    initComponent:function (){
        Wtf.account.MasterConfigurator.superclass.initComponent.call(this);
        chkLandingCostCategoryload();
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
                    width:300,
                    tbar:[this.masterSearch, this.resetBttnMasterGroup],
                    items:[
                         this.masterGrid
                       // this.masterLinks
                    ]
                }
            ]
        });
        this.masterSm.on("selectionchange",function(sm){
            if(this.masterSm.getSelected()){
                if(this.saveMasterItemSequence){
                    this.saveMasterItemSequence.disable();
                }
                if (this.masterGrid.getSelectionModel().getSelected().data["id"] ==Wtf.MasterItems.SALESPERSON) {
//                    this.activateSalesperson.enable();
//                    this.deactivateSalesperson.enable();
                } else {
                    this.activateSalesperson.disable();
                    this.deactivateSalesperson.disable();
                }
                //ERM-210 Added permission add master item button
                if(!WtfGlobal.EnableDisable(Wtf.UPerm.customFieldDimension, Wtf.Perm.customFieldDimension.addMasterItem)) {
                    this.MasterItemAdd.enable();
                }
                this.masterEdit.enable();
                var recData = this.masterGrid.getSelectionModel().getSelected().data;
                
                if(this.masterGrid.getSelectionModel().getSelected().data["mapwithtype"] == '2') {
                   this.syncLMSData.setText(WtfGlobal.getLocaleText("acc.field.SyncPrograms"));   
                   this.syncLMSData.setTooltip(WtfGlobal.getLocaleText("acc.field.SyncProgramstooltip")); 
                   this.syncLMSData.enable();
                   this.mapWithFieldType=2;
                } else if(this.masterGrid.getSelectionModel().getSelected().data["mapwithtype"] == '3') {
                   this.syncLMSData.setText(WtfGlobal.getLocaleText("acc.field.SyncSession"));   
                   this.syncLMSData.setTooltip(WtfGlobal.getLocaleText("acc.field.SyncSessiontooltip")); 
                   this.syncLMSData.enable();
                   this.mapWithFieldType=3;
                } else if(this.masterGrid.getSelectionModel().getSelected().data["mapwithtype"] == '4') {
                   this.syncLMSData.setText(WtfGlobal.getLocaleText("acc.field.SyncCourse"));   
                   this.syncLMSData.setTooltip(WtfGlobal.getLocaleText("acc.field.SyncCoursetooltip")); 
                   this.syncLMSData.enable();
                   this.mapWithFieldType=4;
                } else if(this.masterGrid.getSelectionModel().getSelected().data["mapwithtype"] == '5') {
                   this.syncLMSData.setText(WtfGlobal.getLocaleText("acc.field.SyncCenter"));   
                   this.syncLMSData.setTooltip(WtfGlobal.getLocaleText("acc.field.SyncCentertooltip")); 
                   this.syncLMSData.enable();
                   this.mapWithFieldType=5;
                }else{
                    this.syncLMSData.disable();                  
                }
                if(this.masterGrid.getSelectionModel().getSelected().data["isforproject"] && !this.masterGrid.getSelectionModel().getSelected().data["isfortask"]) {
                   this.syncPMData.setText(WtfGlobal.getLocaleText("acc.field.SyncProjects"));   
                   this.syncPMDatabtn2.setText(WtfGlobal.getLocaleText("acc.field.FullSync"));
                   this.syncPMDatabtn1.enable();
                   this.syncPMDatabtn1.setText(WtfGlobal.getLocaleText("acc.field.PartialSync")); 
                   this.syncPMData.setTooltip(WtfGlobal.getLocaleText("acc.field.SyncProjectsfromProjectManagementforDimensions")); 
                   this.syncPMData.enable();
                   this.isProject=true;
                } else if(!this.masterGrid.getSelectionModel().getSelected().data["isforproject"] && this.masterGrid.getSelectionModel().getSelected().data["isfortask"]) {
                   this.syncPMData.setText(WtfGlobal.getLocaleText("acc.field.SyncPaymentMilestone"));  
                   this.syncPMDatabtn2.setText(WtfGlobal.getLocaleText("acc.field.FullSyncPaymentMilestone")); 
                   this.syncPMDatabtn1.setText(WtfGlobal.getLocaleText("acc.field.PartialSyncPaymentMilestone")); 
                   this.syncPMDatabtn1.disable();
                   this.syncPMData.setTooltip(WtfGlobal.getLocaleText("acc.field.SyncMilestone"));
                   this.syncPMData.enable();
                   this.isProject=false;
                }else{
                    this.syncPMData.disable();                  
                }

                if(this.masterGrid.getSelectionModel().getSelected().data["isforeclaim"]) {
                   this.synceClaimData.enable();
                   this.iseClaim=true;
                } else {
                   this.synceClaimData.disable();
                }
                if (recData.isForSalesCommission) {
                    this.mapSalesCommissionSchema.enable();
                } else {
                    this.mapSalesCommissionSchema.disable();
                }
                if (!recData.iscustomfield && recData.fieldtype == 4) {
                    /**
                     * Enable for dimension and 
                     */
                    if (this.mapUserGroup)
                        this.mapUserGroup.enable();
                } else {
                    if (this.mapUserGroup)
                        this.mapUserGroup.disable();
                }
                // enable disable button here
                
//                if(recData.id == 17 && Wtf.account.companyAccountPref.activateIBG){
//                    //this.ibgDetails.show();
//                }else{
//                    this.ibgDetails.hide();
//                }
               if(recData.id == 19 || recData.id == 57 ){
                    this.syncPOSData.show();
                }else{
                    this.syncPOSData.hide();
                }
//                if (recData.id == 36 || recData.id == 54) {
//                    this.synceProcessSkillData.enable();
//                } else {
//                    this.synceProcessSkillData.disable();
//                }                
                if(recData.fieldtype!="" && (recData.fieldtype!=4 && recData.fieldtype!=7 && recData.fieldtype!=12) && !recData.isformultientity) {// 4- drop down; 7-multiselect
                    this.grid.show();
                    this.grid.getView().emptyText = WtfGlobal.getLocaleText("acc.field.Youhaveselectedcustomfieldoftype")+ WtfGlobal.getXType(recData.fieldtype);
                    this.grid.getView().refresh();
                    this.MasterItemGrid.hide();
                    this.MasterItemAdd.disable();
                    this.masterEdit.disable();
                    this.deleteCustomColumn.enable();
                    this.editDimensionColumn.enable();
                    this.deactivateCustomDimField.enable();
                } else if (recData.modulename != "" && recData.modulename != undefined && !recData.isformultientity){
                    this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                    this.grid.getView().refresh();
                    this.MasterItemStore.proxy.conn.url = "ACCMaster/getMasterItemsForCustomFoHire.do";
                    this.deleteCustomColumn.enable();
                    this.editDimensionColumn.enable();
                    this.deactivateCustomDimField.enable();
                    this.grid.show();
                    this.MasterItemGrid.hide();
                } else if(recData.modulename != "" && recData.modulename != undefined && recData.isformultientity){
                    this.grid.getView().emptyText = WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
                    this.grid.getView().refresh();
                    this.MasterItemStore.proxy.conn.url = "ACCMaster/getMasterItemsForCustomFoHire.do";
                    this.deleteCustomColumn.disable();
                    this.editDimensionColumn.disable();
                    this.deactivateCustomDimField.disable();
                    this.grid.show();
                    this.MasterItemGrid.hide();
                }else{
                    this.grid.hide();
                    this.MasterItemGrid.show();
                    if(recData.id=='16'){
                        this.MasterItemStore.proxy.conn.url = "ACCMaster/getMasterPriceDependentItem.do";
                    }
                    else{
//                        if(recData.id=='17')
//                        {
//                            this.MasterItemAdd.disable();     //master item Button disable for invoice type as it has hardcoded  types
//                        }
                        this.MasterItemStore.proxy.conn.url = "ACCMaster/getMasterItems.do";
                        this.deleteCustomColumn.disable();
                        this.editDimensionColumn.disable();
                    }
                }

                var gstConfigType=recData.gstconfigtype;                 
                /*
                 * this function disables Delete and Edit Button for GST related Default Custom fields                           
                 */
                if (WtfGlobal.GSTApplicableForCompany() == Wtf.GSTStatus.NEW) {
                    this.configureGSTDefaultFields(gstConfigType, 'forMaster');
                    if (recData.id == 62 || recData.id == 63) {
                        /**
                         * Disable Add master item button for GST Reg type and Customer/Vendor Type
                         */
                        this.MasterItemAdd.disable();
                    }              
                }              
                if (Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA && Wtf.isTDSApplicable) {
                    if (recData.id == 33) {
                        /**
                         * Disable Add master item button for NOP
                         */
                        this.MasterItemAdd.disable();
                    }
                }              
                /**
                 * ERP-34235
                 * Product Tax Class Should not be deleted 
                 */
                if (recData.name!=undefined && recData.name!='' && WtfGlobal.GSTApplicableForCompany() == Wtf.GSTStatus.NEW && (recData.name==Wtf.GSTProdCategory || recData.name==Wtf.GSTProdCategory+'*' )) {
                    this.deleteCustomColumn.disable();
                }
                     this.MasterItemStore.load({
                    params:{
                        start: 0,
                        groupid:recData.id,
                        moduleIds:recData.moduleIds,
                        fieldlabel:recData.name,
                        limit:this.pP.combo != undefined ? this.pP.combo.value:30
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
    getMasterGrid:function (){
        this.masterRec = new Wtf.data.Record.create([
            {name:"id"},
            {name:"name"},
            {name:"modulename"},
            {name:"fieldtype"},
            {name:"isforproject"},
  	    {name:"isfortask"},
            {name:"iscustomfield"},
  	    {name:"itemparentid"},
            {name:"moduleIds"},
            {name:"relatedModuleIds"},
            {name:"customcolumn"},
            {name:"isforproject"},
            {name:"isforeclaim"},
            {name:"mapwithtype"},
            {name:"allmodulenames"},
            {name:"activemodulenames"},
            {name:"isformultientity"},
            {name:"isForSalesCommission"},
            {name:"gstconfigtype"},
            {name:"relatedModuleIsAllowEdit"},
            {name:"isessential"}
            
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
        this.masterStore.on('load', this.handleLoad1, this);

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
        //masterbtn.push(this.masterEdit);
        this.masterSearch = new Wtf.MyQuickSearch({
            field: 'name',
            id:"quickSearch"+this.helpmodeid,
            emptyText:WtfGlobal.getLocaleText("acc.masterConfig.search"),  //'Search by Master Group...',
            width: 150
         });
        this.resetBttnMasterGroup = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            id: 'resetBttnMasterGroup'+ this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetBttnMasterGroup.on('click', this.handleResetClickMasterGroup, this);
        this.masterGrid = new Wtf.grid.GridPanel({
//            id:"mastergroup"+this.helpmodeid,
            stripeRows :true,
            sm:this.masterSm = new Wtf.grid.RowSelectionModel({singleSelect:true}),
            region:"center",
           // height : 200,
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
 //removed links  from Master configuration -Issue ID(ERP-3124)       
//        var linkData = {links:[
//                       {fn:"PaymentMethod()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterConfig.msg13")+"'>"+WtfGlobal.getLocaleText("acc.masterConfig.payMethod")+"</span>",viewperm:!WtfGlobal.EnableDisable(Wtf.UPerm.paymentmethod, Wtf.Perm.paymentmethod.view)},
//                       //{fn:"callTax()",text:"Tax",viewperm:true},
//                       {fn:"callCreditTerm()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterConfig.msg9")+"'>"+WtfGlobal.getLocaleText("acc.masterConfig.payTerm")+"</span>",viewperm:!WtfGlobal.EnableDisable(Wtf.UPerm.creditterm, Wtf.Perm.creditterm.view)},
//                       {fn:"callUOM()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterConfig.msg10")+"'>"+WtfGlobal.getLocaleText("acc.masterConfig.uom")+"</span>",viewperm:!WtfGlobal.EnableDisable(Wtf.UPerm.uom, Wtf.Perm.uom.view)},
//                       {fn:"callTax()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterConfig.msg11")+"'>"+WtfGlobal.getLocaleText("acc.masterConfig.taxes")+"</span>",viewperm:!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.view)},
//                       {fn:"callCostCenter()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterConfig.msg12")+"'>"+WtfGlobal.getLocaleText("acc.masterConfig.costCenter")+"</span>",viewperm:true},
//                       {fn:"addTemplete()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterConfig.tempLogo.msg")+"'>"+WtfGlobal.getLocaleText("acc.masterConfig.tempLogo")+"</span>",viewperm:true},
//                       {fn:"addPDFFooter()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterConfig.tempfooter")+"'>"+WtfGlobal.getLocaleText("acc.masterConfig.pdffooterheader")+"</span>",viewperm:true},//issue 31349 [Customize PDF Template]link name in master configuration should be "Customize PDF Template" 
////                       {fn:"callSalesCommission()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterConfig.msg17")+"'>"+WtfGlobal.getLocaleText("acc.masterConfig.salesCommission")+"</span>",viewperm:true},
//                       {fn:"saveCustomizedAgedDuration()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.interval.summary.aged.qtip")+"'> "+WtfGlobal.getLocaleText("acc.interval.summary.aged")+"</span>",viewperm:true},
//                      // {fn:"setApprovelRules()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterconfig.approvelsRules.tooltip")+"'> "+WtfGlobal.getLocaleText("acc.masterconfig.approvelsRules")+"</span>",viewperm:true},
//                     //  {fn:"setDOApprovelRules()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterconfig.doapprovelsRules.tooltip")+"'> "+WtfGlobal.getLocaleText("acc.masterconfig.doapprovelsRules")+"</span>",viewperm:true},
//                    //   {fn:"setGROApprovelRules()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterconfig.groapprovelsRules.tooltip")+"'> "+WtfGlobal.getLocaleText("acc.masterconfig.groapprovelsRules")+"</span>",viewperm:true},
//                       {fn:"templates()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.template.for.records")+"'> "+WtfGlobal.getLocaleText("acc.template.for.records")+"</span>",viewperm:true},
//                       {fn:"masterInvoiceTerms()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.master.invoice.saleterms")+"'> "+WtfGlobal.getLocaleText("acc.master.invoice.saleterms")+"</span>",viewperm:Wtf.account.companyAccountPref.withoutinventory?false:true},
//                       {fn:"masterPurchaseTerms()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.master.invoice.purchaseterms")+"'> "+WtfGlobal.getLocaleText("acc.master.invoice.purchaseterms")+"</span>",viewperm:Wtf.account.companyAccountPref.withoutinventory?false:true},
////                       {fn:"addDeleteNoteType()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterConfig.notetype")+"'>"+WtfGlobal.getLocaleText("acc.masterConfig.notetype")+"</span>",viewperm:true},
//                       {fn:"setNotificationRules()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterConfig.Reminder")+"'>"+WtfGlobal.getLocaleText("acc.masterConfig.Reminder")+"</span>",viewperm:true},
//                       {fn:"inventorySetup()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterConfig.inventorysetup")+"'>"+WtfGlobal.getLocaleText("acc.masterConfig.inventorysetup")+"</span>",viewperm:true},
//                       {fn:"chequeLayoutSetup()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.masterConfig.chequeLayoutSetup")+"'>"+WtfGlobal.getLocaleText("acc.masterConfig.chequeLayoutSetup")+"</span>",viewperm:true}
//                ]};
//            
//            if(Wtf.isPMSync){//
//                if(linkData.links)
//                    linkData.links.push({fn:"saveWIPAndCPAccountSettings()",text:"<span wtf:qtip='"+WtfGlobal.getLocaleText("acc.accPref.wipcpaccount.setting.qtip")+"'> "+WtfGlobal.getLocaleText("acc.accPref.wipcpaccount.setting")+"</span>",viewperm:true})
//            }
//        var tpl = new Wtf.XTemplate(
//           '<div class ="dashboardcontent linkspanel" style="float:left;width:100%">',
//             '<ul id="accMasterSettingPane">',
//             '<tpl for="links">',
//                '<tpl if="viewperm">',
//                    '<li id="wtf-gen215">',
//                        '<a onclick="{fn}" href="#" >{text}</a>',
//                     '</li>',
//                '</tpl>',
//             '</tpl>',
//             '</ul>',
//           '</div>'
//       );
//        this.masterLinks = new Wtf.Panel({
//            region:"center",
//            id:"paymnetConfigure"+this.helpmodeid,
//            bodyStyle:'background:white;',
//            layout:'fit',
////            height:500,
//            border: false,
//            split: true,
//            loadMask:true
//        });
//        if(!this.onlyMaster && !this.showonlyDimension && !this.isShowCustomFieldonly){
//            this.masterLinks.on('render', function(){
//                tpl.overwrite(this.masterLinks.body, linkData);
//            }, this);
//        } 

    },
    getMasterItemGrid:function (){
        this.MasterItemRec = new Wtf.data.Record.create([
            {name:"id"},
            {name:"salespersoncode"},
            {name:"salesPersonContactNumber"},
            {name:"salesPersonAddress"},
            {name:"salesPersonDesignation"},
            {name:"name"},
            {name: 'level',type:'int'},
            {name: 'leaf',type:'boolean'},
            {name: 'parentid'},
            {name: 'isIbgActivItematedForPaidTo'},
	    {name: 'parentmappingid'},	
	    {name: 'type'},
	    {name: 'groupid'},//value to identify group
            {name: 'parentname'},
            {name: 'companyid'},
            {name: 'emailid'},
            {name: 'userid'},
            {name: 'accid'},
            {name: 'username'},
            {name: 'accname'},
            {name: 'typeid'},                 // For Customer/Vendor Category Type
            {name: 'typename'},                // For Customer/Vendor Category Type
            {name: 'ibgReceivingDetails'},
            {name: 'itemdescription'},        // For Multi Select and Drop down Filed Type
            {name: 'driverID'},
            {name: 'isDefaultToPOS'},
            {name: 'parentmappingname'},
            {name: 'activated'},
            {name: 'variancePercentage'},
            {name: 'industryCodeId'},
            {name: 'natureofpaymentdesc'},
            {name: 'natureofpaymentsection'},
            {name: 'typeofdeducteetype'},
            {name: 'vatcommoditycode'},
            {name: 'vatscheduleno'},
            {name: 'vatscheduleserialno'},
            {name: 'vatnotes'},
            {name: 'defaultMasterItem'},
            {name: 'activatedeactivatedimension'},
            {name: 'landingcostcategory'},
            {name: 'landingcostallocationtype'},
            {name: 'salesCommissionSchema'},
            {name: 'usergroup'},
            {name: 'usergroupid'},
            {name: 'BICCode'},
            {name: 'bankCode'},
            {name: 'branchCode'},
            {name: 'sequenceformat'},
            {name: 'isAppendBranchCode'}
            
            
        ]);

        this.MasterItemReader = new Wtf.data.KwlJsonReader({
            root:"data",
            totalProperty: "totalCount"
        },this.MasterItemRec);

        this.MasterItemStore = new Wtf.data.Store({
//            url:Wtf.req.account+'CompanyManager.jsp',
            url:"ACCMaster/getMasterItems.do",
            reader:this.MasterItemReader,
            baseParams:{
                mode:112
            }
        });
        
        
        
        this.MasterItemStore.on('load',this.handleLoad2, this);
        this.MasterItemStore.on('beforeload',this.onBeforeLoad, this);
        this.MasterItemSm=new Wtf.grid.CheckboxSelectionModel();
        this.MasterItemColumn = new Wtf.grid.ColumnModel([
            new Wtf.grid.RowNumberer(),
            this.MasterItemSm,
            {
                header:WtfGlobal.getLocaleText("acc.masterConfig.mi"),  //"Master Items",
                sortable:true,
                renderer:this.formatPredefinedGroup,
                dataIndex:"name",
                width:215
            },{
                header:WtfGlobal.getLocaleText("acc.field.id"),
                hidden: true,
                dataIndex:'id'
            },{
                header:WtfGlobal.getLocaleText("acc.masterConfig.type"),
                dataIndex:'type',
                width:215,
                renderer:function(value){
                    if(value=="0"){
                        return "Number";
                    }else if (value=="1"){
                        return "Drop Down";
                    }else if (value=="2"){
                        return "Formula Using Quantity";
                    }else{
                        return "-";
                    }
                }
            },{
                header:WtfGlobal.getLocaleText("acc.masterConfig.email"),
                dataIndex:'emailid',
                sortable:true,
                hidden:true,
                 width:215
            },{
                header:WtfGlobal.getLocaleText("acc.masterConfig.user"),
                dataIndex:'username',
                sortable:true,
                hidden:true,
                width:215
            },{
                header:WtfGlobal.getLocaleText("acc.field.activatedeactivatesalesperson.columnheader.masterconfiguration"),
                dataIndex:'activated',
                sortable:true,
                hidden:true,
                width:215,
                renderer:function(value){
                    if(value){
                        return WtfGlobal.getLocaleText("acc.masterConfig.activated");
                    }else{
                        return WtfGlobal.getLocaleText("acc.masterConfig.deactivated");
                    }
                }
            },{
                header:WtfGlobal.getLocaleText("acc.masterConfig.description"),
                dataIndex:'natureofpaymentdesc',
                sortable:true,
                hidden:true,
                 width:215
            },{
                header:WtfGlobal.getLocaleText("acc.masterConfig.section"),
                dataIndex:'natureofpaymentsection',
                sortable:true,
                hidden:true,
                width:215
            },{
                header:WtfGlobal.getLocaleText("acc.field.activatedeactivatesalesperson.columnheader.type"),//"Type",
                dataIndex:'typeofdeducteetype',
                sortable:true,
                hidden:true,
                width:215,
                renderer:function(value){
                    if(value=="0"){
                        return WtfGlobal.getLocaleText("acc.masterConfig.corporate");
                    }else if (value=="1"){
                        return WtfGlobal.getLocaleText("acc.masterConfig.noncorporate");
                    }else{
                        return "";
                    }
                }
            },{
                header:WtfGlobal.getLocaleText("acc.field.commoditycode"),//vatcommoditycode
                dataIndex:'vatcommoditycode',
                sortable:true,
                hidden:true,
                width:215
            },{
                header:WtfGlobal.getLocaleText("acc.field.vatscheduleno"),//vatscheduleno
                dataIndex:'vatscheduleno',
                sortable:true,
                hidden:true,
                width:215
            },{
                header:WtfGlobal.getLocaleText("acc.field.vatscheduleserialno"),//vatscheduleserialno
                dataIndex:'vatscheduleserialno',
                sortable:true,
                hidden:true,
                width:215
            },{
                header:WtfGlobal.getLocaleText("acc.field.vatnotes"),//vatnotes
                dataIndex:'vatnotes',
                sortable:true,
                hidden:true,
                width:215
            },{
                header:WtfGlobal.getLocaleText("acc.invoice.landingCostCategory"),//vatnotes
                dataIndex:'landingcostcategory',
                sortable:true,
                hidden:true,
                width:215
            },{
                header:"Allocation Type",//vatnotes
                dataIndex:'landingcostallocationtype',
                sortable:true,
                hidden:true,
                width:215,
                renderer:function(value){
                    var name = "";
                    if (Wtf.Countryid == Wtf.Country.INDIA) {
                        var idx = Wtf.landingCostAllocationTypeStoreIndia.find("id", value);
                        if (idx == -1) {
                            idx = Wtf.landingCostAllocationTypeStoreIndia.find("name", value);
                        }
                        var rec = Wtf.landingCostAllocationTypeStoreIndia.getAt(idx);
                        name = rec != undefined ? rec.data.name : "";
                    } else {
                        var idx = Wtf.landingCostAllocationTypeStore.find("id", value);
                        if (idx == -1) {
                            idx = Wtf.landingCostAllocationTypeStore.find("name", value);
                        }
                        var rec = Wtf.landingCostAllocationTypeStore.getAt(idx);
                        name = rec != undefined ? rec.data.name : "";
                    }
                    return name;
                }
            },{
                header:WtfGlobal.getLocaleText("acc.common.salesPersonCode"),//vatnotes
                dataIndex:'salespersoncode',
                sortable:true,
                hidden:true,
                width:215
            }
            ,{
                header:WtfGlobal.getLocaleText("acc.field.SalesPerson")+" "+WtfGlobal.getLocaleText("acc.salesperson.contact.number"),//vatnotes   
                dataIndex:'salesPersonContactNumber',
                sortable:true,
                hidden:true,
                width:215
            }
            ,{
                header:WtfGlobal.getLocaleText("acc.field.SalesPerson")+" "+WtfGlobal.getLocaleText("acc.invset.header.10"),//vatnotes
                dataIndex:'salesPersonAddress',
                sortable:true,
                hidden:true,
                width:215
            }
            ,{
                header:WtfGlobal.getLocaleText("acc.field.SalesPerson")+" "+WtfGlobal.getLocaleText("acc.salesperson.designation"),//vatnotes
                dataIndex:'salesPersonDesignation',
                sortable:true,
                hidden:true,
                width:215
            },{
                header:WtfGlobal.getLocaleText("acc.common.taxType"),//Tax Type
                dataIndex:'salespersoncode',
                sortable:true,
                hidden:true,
                width:215
            }
        ]);
      
        this.selectionModel = new Wtf.grid.CheckboxSelectionModel();
        this.gridcm= new Wtf.grid.ColumnModel([this.selectionModel,new Wtf.KWLRowNumberer,{
            header:WtfGlobal.getLocaleText("acc.masterConfig.mi"),  //"Master Items",
            dataIndex: 'name',
            sortable:true,
            renderer:this.formatPredefinedGroup,
            pdfwidth:200
        },{
            header:WtfGlobal.getLocaleText("acc.field.id"),
            hidden: true,
            dataIndex:'id'
        },{
            header: WtfGlobal.getLocaleText("acc.common.text.parent"), //"Parent Name",
            dataIndex: 'parentmappingname',
            sortable:true,
            pdfwidth: 200
        },{
            header:WtfGlobal.getLocaleText("acc.field.description"),
            dataIndex:'itemdescription',
            sortable:true,
            width:250,
            renderer : function(val,m,rec) {
                 val = val.replace(/(<([^>]+)>)/ig,"");
                var tip = val.replace(/"/g,'&rdquo;');
                m.attr = 'wtf:qtip="'+"<div style=\'word-wrap:break-word;text-wrap:unrestricted;\'>"+tip+"</div>"+'"'+'" wtf:qtitle="'+WtfGlobal.getLocaleText("acc.field.description")+'"';
                return val;
            }
        },{
            header:WtfGlobal.getLocaleText("acc.sales.salescommission.salesCommissionSchema"),
            dataIndex:"salesCommissionSchema",
            sortable:true,
            hidden:(Wtf.account.companyAccountPref.countryid != Wtf.Country.US)
        },{
            header:WtfGlobal.getLocaleText("acc.user.usergroupbtn"),
            dataIndex:"usergroup",
            sortable:true,
            hidden: !(Wtf.UserReporRole.URole.roleid == Wtf.ADMIN_ROLE_ID && Wtf.account.companyAccountPref.usersVisibilityFlow)
        },{
            header:WtfGlobal.getLocaleText("acc.GIRO.Status"),
            dataIndex:"activatedeactivatedimension",
            sortable:true
        },{
            header:WtfGlobal.getLocaleText("acc.field.Sequence"),//"Sequence",
            dataIndex:"itemsequence",
            renderer: WtfGlobal.itemSequenceRenderer
    }]);
        this.MasterItemAdd = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterConfig.add"),  //"Add Master Item",
            id:"masterItem"+this.helpmodeid,
            handler:function (){
                var masterId=this.masterGrid.getSelectionModel().getSelected().data["id"];
                this.gstConfigType=this.masterGrid.getSelectionModel().getSelected().data["gstconfigtype"];
                if(masterId!='16')
                    this.AddMasterItem(false,masterId,false);
                else
                    this.AddMasterItem(false,masterId,false);
            },
            disabled:true,
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.msg14"),  //{text:"Select an entry from master group to add master item.",dtext:"Select an entry from master group to add master item.", etext:"Add new item details to the selected master group."},
            iconCls :getButtonIconCls(Wtf.etype.menuadd),
            scope:this
        });

        this.MasterItemEdit = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterConfig.edit"),  //"Edit Master Item",
            handler:function (){
                this.AddMasterItem(true,this.masterGrid.getSelectionModel().getSelected().data["id"],false);
                this.gstConfigType=this.masterGrid.getSelectionModel().getSelected().data["gstconfigtype"];
            },
            scope:this,
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.msg15"),  //{text:"Select a master item to edit.",dtext:"Select a master item to edit.", etext:"Edit selected master item details."},
            iconCls :getButtonIconCls(Wtf.etype.menuedit),
            disabled:true
        });

          this.MasterItemDelete = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterConfig.delete"),  //"Delete Master Item",
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.msg16"),  //{text:"Select a master item to delete.",dtext:"Select a master item to delete.", etext:"Delete selected master item details."},
            handler:function (){
                this.confirmBeforeDelete();
            },
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.menudelete),
            disabled:true
        });
        
        this.activateSalesperson = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.masterconfiguration.masterItemActivateactivatesalesperson"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.masterconfiguration.masterItemActivateactivatesalesperson"),
            iconCls: getButtonIconCls(Wtf.etype.activate),
            disabled:true,
            handler: function () {
                this.activateDeactivateSalesperson("Activate");
            }
        });

        this.deactivateSalesperson = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.masterconfiguration.masterItemActivateDeactivatesalesperson"),
            scope: this,
            tooltip: WtfGlobal.getLocaleText("acc.masterconfiguration.masterItemActivateDeactivatesalesperson"),
            iconCls: getButtonIconCls(Wtf.etype.deactivate),
            disabled:true,
            handler: function () {
                this.activateDeactivateSalesperson("Dactivate");
            }
        });
        
        var masterItemArr=[];
        //ERM-210 Added permission add master item button
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.customFieldDimension, Wtf.Perm.customFieldDimension.addMasterItem))
            masterItemArr.push(this.MasterItemAdd);
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit))
            masterItemArr.push(this.MasterItemEdit);
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.remove)) 
            masterItemArr.push(this.MasterItemDelete);
        
        masterItemArr.push(this.activateSalesperson);
        masterItemArr.push(this.deactivateSalesperson);
          
        this.addCustomColumn = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.masterconfig.createCustomColumn"),  //"Add Custom Column",
            hidden:(!this.onlyMaster || !this.isShowCustomFieldonly)&& !this.showMaster,
            tooltip:WtfGlobal.getLocaleText("acc.masterconfig.createCustomColumntooltip"),  
            handler:function (){
                var masterid = "";               
                addCustomColumn(true,false,masterid,this.masterStore);// is Custom column flag
            },
            scope:this,
            iconCls:"customColumnManage"
//            disabled:true
        });
        this.addDimensionColumn = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.masterconfig.createDimension"),  //"Add Dimension",
            hidden:(!this.onlyMaster || !this.showonlyDimension)&&  !this.showMaster,
            tooltip:WtfGlobal.getLocaleText("acc.masterconfig.createDimensiontooltip"),  
            handler:function (){
                var masterid = "";                
                addCustomColumn(false,false,masterid,this.masterStore);// is Custom column flag                
            },
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.add)
//            disabled:true
        });
        
        this.ibgDetails = new Wtf.Toolbar.Button({// if group id is 17(For Paid To Item) then this button will be displayed
            text:WtfGlobal.getLocaleText("acc.ibg.bank.ibg.details"),
            id:'ibgdetails'+this.id,
            scope: this,
            tooltip:WtfGlobal.getLocaleText("acc.ibg.bank.ibg.details"),
            disabled:true,
            hidden:!Wtf.account.companyAccountPref.activateIBG,
            iconCls:getButtonIconCls(Wtf.etype.edit),
            handler:this.viewIBGDetails.createDelegate(this)
        });
        
        this.editDimensionColumn = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.masterconfig.editDimensionOrCustomColumn"),  //"Edit Dimension/Custom field",
            hidden:(!this.onlyMaster || (!this.showonlyDimension && !this.isShowCustomFieldonly)) && !this.showMaster,
            tooltip:WtfGlobal.getLocaleText("acc.masterconfig.editDimensionOrCustomColumntooltip"),  
            handler:function (){
                var iscustomfield = this.masterGrid.getSelectionModel().getSelected().data["iscustomfield"];
                var masterid = this.masterGrid.getSelectionModel().getSelected().data["id"];
                var moduleNames = this.masterGrid.getSelectionModel().getSelected().data["allmodulenames"];
                var moduleID = this.masterGrid.getSelectionModel().getSelected().data["moduleIds"];
                var relatedModuleIds=this.masterGrid.getSelectionModel().getSelected().data["relatedModuleIds"];
//                var relatedModuleIsAllowEdit=this.masterGrid.getSelectionModel().getSelected().data["relatedModuleIsAllowEdit"];
                var name=this.masterGrid.getSelectionModel().getSelected().data["name"];
                if(iscustomfield)
                    addCustomColumn(true,true,masterid,this.masterStore,moduleNames,moduleID,relatedModuleIds,name);// is Custom column flag             
                else
                    addCustomColumn(false,true,masterid,this.masterStore,moduleNames,moduleID,relatedModuleIds,name);// is Custom column flag
                                   
            },
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.edit),
            disabled:true
        });
          this.deleteCustomColumn = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.masterconfig.deleteCustomColumn"),  //"Delete Dimension/Custom fields",
            hidden:(!this.onlyMaster || (!this.showonlyDimension && !this.isShowCustomFieldonly)) && !this.showMaster,
            tooltip:WtfGlobal.getLocaleText("acc.masterconfig.deleteCustomColumntooltip"),  //Delete selected dimension/custom fields
             handler:function (){
                this.deleteDimension(this.masterGrid.getSelectionModel().getSelected().data["id"]);
            },
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.deletebutton),
            disabled:true
        });
        this.deactivateCustomDimField = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.masterconfig.activatedeactivatecustomdimfield"),  //Activate/Deactivate Dimension/Custom fields
            hidden:(!this.onlyMaster || (!this.showonlyDimension && !this.isShowCustomFieldonly)) && !this.showMaster,
            tooltip:WtfGlobal.getLocaleText("acc.masterconfig.activatedeactivatecustomdimfieldtooltip"),  
            handler:function (){
                this.deactivateDimensionField();
            },
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.copy),
            disabled:true
        });
        this.orderCustomDimField = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.masterconfig.orderingOfCustomOrDimensions"),  //Ordering of Custom/Dimensions fields
            tooltip:WtfGlobal.getLocaleText("acc.masterconfig.orderingOfCustomOrDimensions.tooltip"),  
            handler:function (){
                this.orderCustomOrDimensionField();
            },
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.copy)
        });
        this.saveMasterItemSequence = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.masterConfig.saveSequence"),//"Save Sequence",
            tooltip: WtfGlobal.getLocaleText("acc.masterConfig.orderingOfMasterItems.tooltip"),
            iconCls:"pwnd save",
            handler:function (){
                this.saveMasterItemSequenceData();
            },
            scope:this,
            disabled:true
        });
        this.mapSalesCommissionSchema = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.masterconfiguration.mapSalesCommissionSchema"),
            tooltip: WtfGlobal.getLocaleText("acc.masterconfiguration.mapSalesCommissionSchema.tooltip"),
            iconCls: getButtonIconCls(Wtf.etype.add),
            scope: this,
            disabled: true,
            hidden: (Wtf.account.companyAccountPref.countryid != Wtf.Country.US),
            hideLabel: (Wtf.account.companyAccountPref.countryid != Wtf.Country.US),
            handler: this.mapSalesCommissionSchemaToMasterItem.createDelegate(this)
        });
        /**
         * Map User Grp for Users visibility flow
         */
        this.mapUserGroup = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.masterconfiguration.usergrp.mapfcd"),
            tooltip: WtfGlobal.getLocaleText("acc.masterconfiguration.usergrp.mapfcdtip"),
            iconCls: getButtonIconCls(Wtf.etype.add),
            scope: this,
//            disabled: true,
            hidden: !(Wtf.UserReporRole.URole.roleid == Wtf.ADMIN_ROLE_ID && Wtf.account.companyAccountPref.usersVisibilityFlow),
            hideLabel: !(Wtf.UserReporRole.URole.roleid == Wtf.ADMIN_ROLE_ID && Wtf.account.companyAccountPref.usersVisibilityFlow),
            handler: this.mapUsersGroupToMasterItem.createDelegate(this)
        });
        this.activateDimensionArr = new Array();
        //Activate button Master Items fields
        this.acivateDimensionFieldValue = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterconfiguration.ActivateMasterFields"),  
            tooltip:WtfGlobal.getLocaleText("acc.masterconfiguration.ActivateMasterFields"),
            disabled:true,
            scope:this,
            handler:this.acivateDectivateDimensionFieldvalues.createDelegate(this,this.activateDeactivate=["activate"]),
            iconCls: getButtonIconCls(Wtf.etype.activate)
        });
        //Deactivate button Master Items fields
        this.DectivateDimensionFieldValue  = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterconfiguration.DeactivateMasterFields"),  
            tooltip:WtfGlobal.getLocaleText("acc.masterconfiguration.DeactivateMasterFields"),
            disabled:true,
            scope:this,
            handler:this.acivateDectivateDimensionFieldvalues.createDelegate(this,this.activateDeactivate=["deactivate"]),
            iconCls: getButtonIconCls(Wtf.etype.deactivate)
        });
        this.activateDimensionArr.push(this.acivateDimensionFieldValue);
        this.activateDimensionArr.push(this.DectivateDimensionFieldValue);
        //Activate Deactivate button Master Items fields
        this.acivateDectivateDimensionFields = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterconfiguration.ActivateDeactivateMasterFields"),  
            tooltip:WtfGlobal.getLocaleText("acc.masterconfiguration.ActivateDeactivateMasterFields"),
            menu:this.activateDimensionArr,
            disabled:true,
            hidden:true,
            scope:this,
            iconCls:'accountingbase product'
        });
          this.setPrice = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.masterconfig.setPrice"),  //"Set price",
            hidden:SATSCOMPANY_ID!=companyid,
            tooltip:WtfGlobal.getLocaleText("acc.masterconfig.setPricetooltip"),  
             handler:function (){
                  var rec=this.MasterItemSm.getSelected();
                   if(rec.data.type==1) {                  
                        this.setPriceFunction(this.masterGrid.getSelectionModel().getSelected().data["id"]);
                   }else{
                       this.setPriceFormulaFunction(this.masterGrid.getSelectionModel().getSelected().data["id"]);
                   }  
            },
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.add),
            disabled:true
        });
          this.setPriceFormula = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.masterconfig.setPriceFormula"),  //"Set price",
            tooltip:WtfGlobal.getLocaleText("acc.masterconfig.setPriceFormulatooltip"),  
            hidden:true,
             handler:function (){
                this.setPriceFormulaFunction(this.masterGrid.getSelectionModel().getSelected().data["id"]);
            },
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.add),
            disabled:true
        });
         var syncProjectArr=[];
         this.syncPMDatabtn1 = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.field.PartialSync"),
            tooltip:WtfGlobal.getLocaleText("acc.field.PartialSyncToolTip"),  
            handler:this.syncPMDataConfirm.createDelegate(this,this.syncType=["partial"]),
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.sync)
        });
        this.syncPMDatabtn2 = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.field.FullSync"),
            tooltip:WtfGlobal.getLocaleText("acc.field.FullSyncToolTip"),  
            handler:this.syncPMDataConfirm.createDelegate(this,this.syncType=["full"]),
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.sync)
        });
        
        this.syncAllFromLMSBtn = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.field.SyncAllFromLMS"),
            tooltip:WtfGlobal.getLocaleText("acc.field.SyncAllFromLMSToolTip"),  
            handler:this.showsyncAllFromLMSWindow.createDelegate(this),
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.sync)
        });
               
               
        
        syncProjectArr.push(this.syncPMDatabtn2);
        syncProjectArr.push(this.syncPMDatabtn1);

        this.salesCommisionSchema = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.masterConfig.salesCommision"),  //"Set price",
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.salesCommisiontooltip"),  
             handler:function (){
             var rec=this.MasterItemSm.getSelected();
             this.createSalesCommisionSchema(this.masterGrid.getSelectionModel().getSelected().data["id"]);
                  
            },
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.add),
            disabled:true,
            hidden:true
        }); 
        
         var syncProjectArr=[];
         this.syncPMDatabtn1 = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.field.PartialSync"),
            tooltip:WtfGlobal.getLocaleText("acc.field.PartialSyncToolTip"),  
            handler:this.syncPMDataConfirm.createDelegate(this,this.syncType=["partial"]),
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.sync)
        });
        this.syncPMDatabtn2 = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.field.FullSync"),
            tooltip:WtfGlobal.getLocaleText("acc.field.FullSyncToolTip"),  
            handler:this.syncPMDataConfirm.createDelegate(this,this.syncType=["full"]),
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.sync)
        });
        syncProjectArr.push(this.syncPMDatabtn2);
        syncProjectArr.push(this.syncPMDatabtn1);
        this.syncPMData = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.SyncProjects"),  //"Add Custom Column",
            tooltip:WtfGlobal.getLocaleText("acc.field.SyncProjectsfromProjectManagementforDimensions"),  
            disabled:true, 
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.sync),
            menu:syncProjectArr
        });
        this.synceClaimData = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.SyncCostCentersFromeClaims"),  //"Sync Cost Centers",
            tooltip:WtfGlobal.getLocaleText("acc.field.SyncCostCentersfromeClaimforDimensions"),  
            disabled:true, 
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.sync),
            handler:this.synceClaimDataConfirm.createDelegate(this)
        });
//        this.synceProcessSkillData = new Wtf.Toolbar.Button({
//            text: WtfGlobal.getLocaleText("acc.field.SyncProcessandskills"), //"Sync Cost Centers",
//            tooltip: WtfGlobal.getLocaleText("acc.field.SyncProcessandskillstip"),
//            disabled: true,
//            scope: this,
//            iconCls: getButtonIconCls(Wtf.etype.sync),
//            handler: this.syncProcessSkillConfirm.createDelegate(this)
//        });
        this.syncLMSData = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.SyncPrograms"), 
            tooltip:WtfGlobal.getLocaleText("acc.field.SyncPrograms"),  
            disabled:true, 
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.sync),
            handler:this.syncCustomFieldDataFromOtherProjects
         });
        this.syncPOSData = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.DataSyncToPOS"), 
            tooltip:WtfGlobal.getLocaleText("acc.field.SyncPrograms"),  
//            disabled:true,
            hidden:true,
            scope:this,
            iconCls :getButtonIconCls(Wtf.etype.sync),
            handler:this.syncProductsCategoryToPOS
         });
         var ExportImport=[];
         this.ExportCustomFieldsDimentions = new Wtf.Action({
            text:(this.showonlyDimension && this.showonlyDimension!=undefined)?WtfGlobal.getLocaleText("acc.masterConfig.ExportDimensionFields"):(this.isShowCustomFieldonly && this.isShowCustomFieldonly!=undefined)?WtfGlobal.getLocaleText("acc.masterConfig.ExportOnlyCustomFields"):WtfGlobal.getLocaleText("acc.masterConfig.ExportCustomFields"),
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.ExportCustomFields"),  
            handler:this.ExportMasterConfig.createDelegate(this,this.ExportMasterConfigVar=["customfields"]),
            scope:this,
            iconCls:'pwnd exportcsv'
        });
        this.ExportDefaultsFields = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterConfig.ExportDefaultFields"),
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.ExportDefaultFields"),  
            handler:this.ExportMasterConfig.createDelegate(this,this.ExportMasterConfigVar=["defaultfields"]),
            scope:this,
            iconCls:'pwnd exportcsv'
        });
        var exportData = [];
        this.ExportCustomFieldsDimentionsData= new Wtf.Action({
            text:(this.showonlyDimension && this.showonlyDimension!=undefined)?WtfGlobal.getLocaleText("acc.masterConfig.ExportDimensionFieldData"):(this.isShowCustomFieldonly && this.isShowCustomFieldonly!=undefined)?WtfGlobal.getLocaleText("acc.masterConfig.ExportOnlyCustomFieldData"):WtfGlobal.getLocaleText("acc.masterConfig.ExportCustomFieldData"),
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.ExportCustomFieldData"),  
            handler:this.ExportMasterConfig.createDelegate(this,this.ExportMasterConfigVar=["customfielddata","0"]),
            scope:this,
            iconCls:'pwnd exportcsv'
        });
        this.ExportDefaultFieldsData= new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterConfig.ExportDefaultFieldData"),
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.ExportDefaultFieldData"),  
            handler:this.ExportMasterConfig.createDelegate(this,this.ExportMasterConfigVar=["customfielddata","1"]),
            scope:this,
            iconCls:'pwnd exportcsv'
        });
        this.ExportBoth= new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterConfig.ExportBoth"),
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.ExportBothttip"),  
            handler:this.ExportMasterConfig.createDelegate(this,this.ExportMasterConfigVar=["customfielddata","2"]),
            scope:this,
            iconCls:'pwnd exportcsv'
        });
        exportData.push(this.ExportCustomFieldsDimentionsData);
        if ( this.id == "masterconfiguration" ) {
            exportData.push(this.ExportDefaultFieldsData);
            exportData.push(this.ExportBoth);
        }
        this.ExportDataMenu= new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterConfig.ExportData"),
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.ExportData"),  
            scope:this,
            iconCls:'pwnd exportItem',
            menu: exportData
        });
        if ( this.id == "masterconfiguration"  ) {
            ExportImport.push(this.ExportDefaultsFields);
        }
          this.ExportxlsCustomFieldsDimentions = new Wtf.Action({
            text:(this.showonlyDimension && this.showonlyDimension!=undefined)?WtfGlobal.getLocaleText("acc.masterConfig.ExportxlsDimensionFields"):(this.isShowCustomFieldonly && this.isShowCustomFieldonly!=undefined)?WtfGlobal.getLocaleText("acc.masterConfig.ExportxlsOnlyCustomFields"):WtfGlobal.getLocaleText("acc.masterConfig.ExportxlsCustomFields"),
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.ExportxlsCustomFields"),  
            handler:this.ExportMasterConfig.createDelegate(this,[this.ExportMasterConfigVar=["customfields"],"undefined",this.type=["xls"]]),
            scope:this,
            iconCls:'pwnd exportcsv'
        });
        this.ExportxlsDefaultsFields = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterConfig.ExportxlsDefaultFields"),
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.ExportxlsDefaultFields"),  
            handler:this.ExportMasterConfig.createDelegate(this,[this.ExportMasterConfigVar=["defaultfields"],"undefined",this.type=["xls"]]),
            scope:this,
            iconCls:'pwnd exportcsv'
        });
        var exportxlsData = [];
        this.ExportxlsCustomFieldsDimentionsData= new Wtf.Action({
            text:(this.showonlyDimension && this.showonlyDimension!=undefined)?WtfGlobal.getLocaleText("acc.masterConfig.ExportxlsDimensionFieldData"):(this.isShowCustomFieldonly && this.isShowCustomFieldonly!=undefined)?WtfGlobal.getLocaleText("acc.masterConfig.ExportxlsOnlyCustomFieldData"):WtfGlobal.getLocaleText("acc.masterConfig.ExportxlsCustomFieldData"),
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.ExportxlsCustomFieldData"),  
            handler:this.ExportMasterConfig.createDelegate(this,this.ExportMasterConfigVar=["customfielddata","0","xls"]),
            scope:this,
            iconCls:'pwnd exportcsv'
        });
        this.ExportxlsDefaultFieldsData= new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterConfig.ExportxlsDefaultFieldData"),
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.ExportxlsDefaultFieldData"),  
            handler:this.ExportMasterConfig.createDelegate(this,this.ExportMasterConfigVar=["customfielddata","1","xls"]),
            scope:this,
            iconCls:'pwnd exportcsv'
        });
        this.ExportxlsBoth= new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterConfig.ExportxlsBoth"),
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.ExportxlsBothttip"),  
            handler:this.ExportMasterConfig.createDelegate(this,this.ExportMasterConfigVar=["customfielddata","2","xls"]),
            scope:this,
            iconCls:'pwnd exportcsv'
        });
        exportxlsData.push(this.ExportxlsCustomFieldsDimentionsData);
        if ( this.id == "masterconfiguration" ) {
            exportxlsData.push(this.ExportxlsDefaultFieldsData);
            exportxlsData.push(this.ExportxlsBoth);
        }
        this.ExportxlsDataMenu= new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterConfig.ExportxlsData"),
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.ExportxlsData"),  
            scope:this,
            iconCls:'pwnd exportItem',
            menu: exportxlsData
        });
        if ( this.id == "masterconfiguration"  ) {
            ExportImport.push(this.ExportxlsDefaultsFields);
        }
        ExportImport.push(this.ExportCustomFieldsDimentions);
        ExportImport.push(this.ExportxlsCustomFieldsDimentions);
        ExportImport.push(this.ExportDataMenu);
        ExportImport.push(this.ExportxlsDataMenu);
         this.ExportButton = new Wtf.Toolbar.Button({
         text:WtfGlobal.getLocaleText("acc.common.export"),
            tooltip:WtfGlobal.getLocaleText("acc.common.export"),  
            scope:this,
            iconCls: (Wtf.isChrome?'pwnd exportChrome':'pwnd export'),
            menu:ExportImport
        });
        var Import=[];
        this.ImportCustomFieldsDimentions = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterConfig.ImportCustomFields"),
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.ImportCustomFields"),  
            handler:callSettingsImportWin.createDelegate(this,['download_sample_Custom Field.csv','sample_Custom_Field'," Custom Fields/Dimension","ACCAccountCMN/ImportCustomFields.do?",true,'Import Custom Fields']),//Filename must be without extention
            scope:this,
            iconCls: 'pwnd importcsv'
        });
        var importData = [];
        this.ImportCustomFieldsDimentionData = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterConfig.ImportCustomFieldData"),
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.ImportCustomFieldData"),  
            handler:callSettingsImportWin.createDelegate(this,['download_sample_Custom Fields data_v2.csv', 'sample_Custom_Fields_Dimension_data'," Custom Fields/Dimension data","ACCAccountCMN/ImportCustomFieldsData.do?",false,'Import Custom Field Data']),//Filename must be without extention
            scope:this,
            iconCls: 'pwnd importcsv'
        });
        this.ImportDefaultFieldsData = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterConfig.ImportDefaultFieldData"),
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.ImportDefaultFieldData"),  
            handler:callSettingsImportWin.createDelegate(this,['download_sample_Default Fields data.csv','sample_Default_Fields_data'," Default Fields data","ACCAccountCMN/ImportCustomFieldsData.do?",false,'Import Default Field Data']),
            scope:this,
            iconCls: 'pwnd importcsv'
        });
        this.ImportBoth = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterConfig.ImportBoth"),
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.ImportBothttip"),  
            handler:callSettingsImportWin.createDelegate(this,['download_sample_Custom Fields_Dimension data_v2.csv','sample_Default_Custom_Fields_Dimension_data'," Default Fields and Custom Fields/Dimension data","ACCAccountCMN/ImportCustomFieldsData.do?",false,'Import Default and Custom Field Data']),//Sample Filename without extention
            scope:this,
            iconCls: 'pwnd importcsv'
        });
        importData.push(this.ImportCustomFieldsDimentionData);
        if ( this.id == "masterconfiguration"  ) { 
            importData.push(this.ImportDefaultFieldsData);
            importData.push(this.ImportBoth);
        }
        this.ImportDataMenu = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.masterConfig.ImportData"),
            tooltip:WtfGlobal.getLocaleText("acc.masterConfig.ImportData"),  
            //handler:callSettingsImportWin.createDelegate(this,['download_sample_Custom Fields_Dimension data.csv', 'sample_Custom Fields_Dimension data.csv', " Default Fields and Custom Fields/Dimension data","ACCAccountCMN/ImportCustomFieldsData.do?",false]),
            scope:this,
            menu:importData,
            iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import')
        });
        Import.push(this.ImportCustomFieldsDimentions);
        Import.push(this.ImportDataMenu);
        this.ImportButton = new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.import"),
            tooltip:WtfGlobal.getLocaleText("acc.common.import"),  
            scope:this,
            iconCls: (Wtf.isChrome?'pwnd importChrome':'pwnd import'),
            menu:Import
        });
        this.masterItemSearch = new Wtf.KWLTagSearch({
            field: 'name',
            emptyText:WtfGlobal.getLocaleText("acc.masterConfig.search1"),  //'Search by Master Item...',
            width: 150,
            Store: this.MasterItemStore
         });
        this.resetBttn = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
            tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls: getButtonIconCls(Wtf.etype.resetbutton),
            disabled: false
        });
        this.resetBttn.on('click', this.handleResetClick, this);

        this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            id: "pagingtoolbar" + this.id,
            store: this.MasterItemStore,
            searchField: this.masterItemSearch,
//            displayInfo: true,
            emptyMsg: WtfGlobal.getLocaleText("acc.common.norec"), //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({
                id: "pPageSize_" + this.id
            })
        });
        
        var MasterItembtn=new Array();
        MasterItembtn.push(this.masterItemSearch);
        MasterItembtn.push(this.resetBttn);
        if(masterItemArr.length>=1) {
            MasterItembtn.push({
                text:WtfGlobal.getLocaleText("erp.menuitem.MasterItemMenu"),//'Master Item Menu'
                tooltip:WtfGlobal.getLocaleText("erp.masterconfig.menuitem.tooltip"),  //'Click here to add, edit or delete Master Item.'
                id:"managemasteritem"+this.masterid,
                iconCls:'accountingbase product',
                menu:masterItemArr
            });
        }
        
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.create)) {
            MasterItembtn.push(this.addCustomColumn);
            MasterItembtn.push(this.addDimensionColumn);
        }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit)) {            
            MasterItembtn.push(this.editDimensionColumn);
        }        
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.remove))
            MasterItembtn.push(this.deleteCustomColumn);
        
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit)) {            
            MasterItembtn.push(this.setPrice);
 		    MasterItembtn.push(this.setPriceFormula);
        }  
//            MasterItembtn.push(this.ibgDetails);
        
          MasterItembtn.push(this.salesCommisionSchema);       
        
          MasterItembtn.push("->");
        
        MasterItembtn.push(getHelpButton(this,this.helpmodeid));
        
        
        var MasterItembbarbtn=new Array();
        
        MasterItembbarbtn.push(this.pagingToolbar);
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.create) && Wtf.isPMSync)
            MasterItembbarbtn.push(this.syncPMData)
//        MasterItembbarbtn.push(this.synceProcessSkillData)
        
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.create) && Wtf.iseClaimSync) {
            MasterItembbarbtn.push(this.synceClaimData)
        }
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.create) && Wtf.account.companyAccountPref.isLMSIntegration){
            MasterItembbarbtn.push(this.syncLMSData)
        }
        if(!Wtf.account.companyAccountPref.standalone && Wtf.account.companyAccountPref.integrationWithPOS){
            MasterItembbarbtn.push(this.syncPOSData)
        }
            MasterItembbarbtn.push(this.ibgDetails)
        if (Wtf.account.companyAccountPref.isLMSIntegration && Wtf.UserReporRole.URole.roleid==1) {
            MasterItembbarbtn.push(this.syncAllFromLMSBtn);
        }
        if (!WtfGlobal.EnableDisable(this.uPermType, this.permType.exportmasterconfig)) {
            MasterItembbarbtn.push(this.ExportButton);
        }
        if (!WtfGlobal.EnableDisable(this.uPermType, this.permType.importmasterconfig)) {
            MasterItembbarbtn.push(this.ImportButton);
        }
        
        MasterItembbarbtn.push(this.mapSalesCommissionSchema);
        if ((Wtf.UserReporRole.URole.roleid == Wtf.ADMIN_ROLE_ID && Wtf.account.companyAccountPref.usersVisibilityFlow)) {
            /**
             * Button shows to map Matser item to User grp only for admin
             */
            MasterItembbarbtn.push(this.mapUserGroup);
        }
        
        if(!WtfGlobal.EnableDisable(this.uPermType, this.permType.remove))
            MasterItembbarbtn.push(this.deactivateCustomDimField);
        
        MasterItembbarbtn.push(this.orderCustomDimField);
        MasterItembbarbtn.push(this.saveMasterItemSequence);
        MasterItembbarbtn.push(this.acivateDectivateDimensionFields);
        
        this.MasterItemGrid = new Wtf.grid.HirarchicalGridPanel({
            cls:'vline-on',
            sm:this.MasterItemSm,
            store:this.MasterItemStore,
            autoScroll:true,
//            region:"center",
//            loadMask:true, // Remove double Loading Mask
            border: false,
            layout:'fit',
            split: true,
            cm:this.MasterItemColumn,
            hirarchyColNumber:4,
           viewConfig: {
                forceFit:false,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        this.MasterItemGrid.on('cellclick', function () {
            if (this.MasterItemGrid.getSelectionModel().getSelected().data.groupid == Wtf.MasterItems.SALESPERSON) {
                this.activateSalesperson.enable();
                this.deactivateSalesperson.enable();
            } else {
                this.activateSalesperson.disable();
                this.deactivateSalesperson.disable();
            }
        }, this);
        
         this.summary = new Wtf.ux.grid.GridSummary();
          this.grid = new Wtf.grid.HirarchicalGridPanel({
            cls:'vline-on',  
            layout:'fit',
            autoHeight:true,
            hidden:true,
            split: true,
            store: this.MasterItemStore,
            cm: this.gridcm,
            sm:this.selectionModel,
//            autoScroll:true,
            hirarchyColNumber:2,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });  
        this.gridContainer=new Wtf.Panel({
             region:"center",
             layout:'fit',
             autoScroll:true,
             border:false,
             bodyStyle:{"background-color":'white'}, 
             items:[this.MasterItemGrid,this.grid],
              tbar:MasterItembtn,
              bbar:MasterItembbarbtn
        });
          this.grid.on("render", function(grid) {
            WtfGlobal.autoApplyHeaderQtip(grid);
        },this);
        
        this.grid.on('rowclick', function(grid,rowIndex,e){
            if(e.target.className == "pwndBar2 shiftrowupIcon") {
                moveSelectedRowAccordingtoParentChild(this.grid,0);
                this.saveMasterItemSequence.enable();
            }
            if(e.target.className == "pwndBar2 shiftrowdownIcon") {
                moveSelectedRowAccordingtoParentChild(this.grid,1);
                this.saveMasterItemSequence.enable();
            }
            this.acivateDectivateDimensionFields.enable();
        },this);
    
        this.MasterItemSm.on("selectionchange",function (){
//                Wtf.uncheckSelAllCheckbox1(this.MasterItemSm);
               // WtfGlobal.enableDisableBtnArr(MasterItembtn, this.MasterItemGrid, [2], [3]);
//                WtfGlobal.enableDisableBtnArr(masterItemArr, this.MasterItemGrid, [0], [1]);
            // WtfGlobal.enableDisableBtnArr(MasterItembtn, this.grid, [2], [3]);
                if(masterItemArr.length==2){
                    if(!(!WtfGlobal.EnableDisable(this.uPermType, this.permType.create))){
                        WtfGlobal.enableDisableBtnArr(masterItemArr, this.MasterItemGrid, [0], [1]);
                    }
                    if(!(!WtfGlobal.EnableDisable(this.uPermType, this.permType.edit)))
                        WtfGlobal.enableDisableBtnArr(masterItemArr, this.MasterItemGrid, [], [1]);
                    if(!(!WtfGlobal.EnableDisable(this.uPermType, this.permType.remove))) 
                        WtfGlobal.enableDisableBtnArr(masterItemArr, this.MasterItemGrid, [1], []);
                } else if (masterItemArr.length == 1) {         //handle case if only one button in array
                    WtfGlobal.enableDisableBtnArr(masterItemArr, this.MasterItemGrid, [1], [0]);
                } else {
                    if (this.MasterItemSm.getSelected()!=undefined && (this.MasterItemSm.getSelected().data.groupid == "34" || this.MasterItemSm.getSelected().data.groupid == "52" || this.MasterItemSm.getSelected().data.groupid == Wtf.MasterItems.WOSTATUS)) {  // for not deleting the default WO Status
                        this.enableDisableButtonForDeducteeType();
                    }else{
                           WtfGlobal.enableDisableBtnArr(masterItemArr, this.MasterItemGrid, [1], [2]); 
                    }
                }
            if(this.MasterItemSm.getCount()==1){
                var rec=this.MasterItemSm.getSelected();
                if(rec.data.groupid=='15'){//Sales Person ID=15
                    this.salesCommisionSchema.setDisabled(false);
                    this.salesCommisionSchema.setVisible(true);
                }else{
                    this.salesCommisionSchema.setDisabled(true);
                    this.salesCommisionSchema.setVisible(false);
                }
                if(rec.data.type===1 || rec.data.type===0 || rec.data.type==2) {
                    this.setPrice.setDisabled(false);
                    this.setPriceFormula.setDisabled(true);
                }
            }else{
                this.setPrice.setDisabled(true);
                this.setPriceFormula.setDisabled(true);
                this.salesCommisionSchema.setDisabled(true); 
            }
            if(this.masterGrid && this.masterGrid.getSelectionModel().getSelected()){   
                var recData = this.masterGrid.getSelectionModel().getSelected().data;
                if(recData.id == 17 && this.ibgDetails){
                    var selectedArr = this.MasterItemSm.getSelections();
                    var selRec = selectedArr[0];
                    if(selectedArr.length == 1){
//                        if(selRec.get('isIbgActivItematedForPaidTo')==true){// If Paid To Item is selected and its records are IBG active then make it enable
                            this.ibgDetails.enable();
//                        }
                    } else{
                        this.ibgDetails.disable();
                    }
                }
            }
                if (this.MasterItemSm.getSelections().length > 0 && this.masterGrid.getSelectionModel().getSelected().data["id"] ==Wtf.MasterItems.SALESPERSON) {
                    this.activateSalesperson.enable();
                    this.deactivateSalesperson.enable();
                } else {
                    this.activateSalesperson.disable();
                    this.deactivateSalesperson.disable();
                }
                if (WtfGlobal.GSTApplicableForCompany() == Wtf.GSTStatus.NEW) {
                    /*
                     * ERP-36468 Here restricting user from deleting and editing default values of GST Registration Type and GST Customer/ Vendor Type
                     */
                    if (this.masterGrid && this.masterGrid.getSelectionModel().getSelected()) {
                       var recData = this.masterGrid.getSelectionModel().getSelected().data;
                       var isDefaultSelected = false;
                       if (recData.id == 62 || recData.id == 63) {
                        for (var i = 0; i < this.MasterItemSm.getSelections().length; i++) {
                            if (this.MasterItemSm.getSelections()[i].data.defaultMasterItem != "") {
                                isDefaultSelected = true;
                                break;
                            }
                        }
                        if (isDefaultSelected) {
                            this.MasterItemDelete.disable();
                            this.MasterItemEdit.disable();
                          }
                        }
                    }
                }
            
            this.changeMsg();
        },this);
        this.selectionModel.on("selectionchange",function (){
//                Wtf.uncheckSelAllCheckbox1(this.MasterItemSm);
               // WtfGlobal.enableDisableBtnArr(MasterItembtn, this.MasterItemGrid, [2], [3]);
                    WtfGlobal.enableDisableBtnArr(masterItemArr, this.grid, [0], [1]);
                    /*
                     * ERP29434
                     * if condition checks the record available in the grids
                     * 
                     */
                    
            if (this.selectionModel && this.selectionModel.getCount()==1) {
                this.MasterItemDelete.setDisabled(false);
            } else {                
                this.MasterItemDelete.setDisabled(true);
            }
            //ERM-210 Added permission add master item button
            if(!WtfGlobal.EnableDisable(Wtf.UPerm.customFieldDimension, Wtf.Perm.customFieldDimension.addMasterItem)) {
                this.MasterItemAdd.setDisabled(false);
            }
            if(this.MasterItemSm.getCount()==1){
                var rec=this.MasterItemSm.getSelected();
                if(rec.data.type===1 || rec.data.type===0 || rec.data.type==2) {
                    this.setPrice.setDisabled(false);
                    this.setPriceFormula.setDisabled(true);
                }
            }else{
                this.setPrice.setDisabled(true);
                this.setPriceFormula.setDisabled(true);
            }
            var recData = this.masterGrid.getSelectionModel().getSelected().data;  
            /*
             * this function disables Delete and Edit Button for GST related Default Custom fields                           
             */
            var gstConfigType=recData.gstconfigtype;                
                if (WtfGlobal.GSTApplicableForCompany() == Wtf.GSTStatus.NEW){
                    this.configureGSTDefaultFields(gstConfigType,'forMasterValue');
                }
            this.changeMsg();
        },this);
    },
    enableDisableButtonForDeducteeType: function () {
        var count = 0;
        for (var i = 0; i < this.MasterItemSm.getSelections().length; i++) {
            if (this.MasterItemSm.getSelections()[i].data.defaultMasterItem == "") {
                count++;
            } else {
                break;
            }
        }
        if (count == this.MasterItemSm.getSelections().length) {
            if (this.MasterItemSm.getSelections().length > 1) {
                this.MasterItemEdit.setDisabled(true);
            } else {
                this.MasterItemEdit.setDisabled(false);
                this.MasterItemDelete.setDisabled(false);
            }
        } else {
            if (this.MasterItemSm.getSelections().length == 1) {
                this.MasterItemEdit.setDisabled(false);
            } else {
                this.MasterItemEdit.setDisabled(true);
                this.MasterItemDelete.setDisabled(true);
            }
        }
    },
    changeMsg:function(){
        
        //logic to enable and disable activate and deactivate buttons
        var arr=this.grid.getSelectionModel().getSelections();
        var masterrecord = this.masterGrid.getSelectionModel().getSelected();
        /**
         * Showing activate deactivate master item button in both custom 
         * and dimension field
         */    
        if(masterrecord!= undefined){
                this.acivateDectivateDimensionFields.show();
            }else{
                this.acivateDectivateDimensionFields.hide();
            }
        var record = this.grid.getSelectionModel().getSelected();
        if(record){
            this.acivateDectivateDimensionFields.enable();
            if (arr.length == 1) {
                if(record.data.activatedeactivatedimension=='Activated'){
                    this.acivateDimensionFieldValue.disable();
                    this.DectivateDimensionFieldValue.enable();
                }else{
                    this.acivateDimensionFieldValue.enable();
                    this.DectivateDimensionFieldValue.disable();
                }
            }else{
                var isBothMasterItemSelected = false;
                for (var i = 0; i < arr.length; i++) {
                    if (arr[i] && arr[i].data) {
                        if (record.data.activatedeactivatedimension != arr[i].data.activatedeactivatedimension) {
                            isBothMasterItemSelected = true;
                            break;
                        }
                    }
                }
                if (isBothMasterItemSelected) {
                    this.acivateDimensionFieldValue.enable();
                    this.DectivateDimensionFieldValue.enable();
                } else if (record.data.activatedeactivatedimension == 'Activated') {
                    this.acivateDimensionFieldValue.disable();
                    this.DectivateDimensionFieldValue.enable();
                } else {
                    this.acivateDimensionFieldValue.enable();
                    this.DectivateDimensionFieldValue.disable();
                }
            }
        } else {
            this.acivateDectivateDimensionFields.disable();
            this.acivateDimensionFieldValue.disable();
            this.DectivateDimensionFieldValue.disable();
        }
        
        if(this.MasterItemAdd.disabled==false)
            this.MasterItemAdd.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg3"));
            else
                this.MasterItemAdd.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg4"));
            if(this.MasterItemEdit.disabled==false)
                this.MasterItemEdit.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg5"));
            else
                this.MasterItemEdit.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg6"));
            if(this.editDimensionColumn.disabled==false)
                this.editDimensionColumn.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg18"));
            else
                this.editDimensionColumn.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg19"));
            if(this.MasterItemDelete.disabled==false)
                this.MasterItemDelete.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg7"));
            else
                this.MasterItemDelete.setTooltip(WtfGlobal.getLocaleText("acc.masterConfig.msg8"));
    },
    handleLoad1:function(store){
        this.masterSearch.StorageChanged(store);
        this.fireEvent("loadMasterGroup");
        if(this.filterid && this.filterid!=undefined && this.filterid!=""){
         store.clearFilter();
         store.filterBy(function(rec) {
            if((rec.data.id!='20'&&rec.data.id!='15')) {
                return false;
            }else{
                return true;
            }
         });

        }
        else{
        store.clearFilter();
        store.filterBy(function(rec, id) {
            if((Wtf.Countryid !='105' || !Wtf.account.companyAccountPref.enablevatcst) && rec.data.id=='42') {
                return false;
            }else{
                return true;
            }
        });
    }
    },
    onBeforeLoad: function (store, o) {
        if (this.masterGrid && this.masterGrid.getSelectionModel() && this.masterGrid.getSelectionModel().getCount() == 1) {
            if (this.pP.combo != undefined) {
                var recData = this.masterGrid.getSelectionModel().getSelected().data;
                if (!o.params) {
                    o.params = {};
                }
                if (this.pP.combo.value == "All") {
                    var count = store.getTotalCount();
                    var rem = count % 5;
                    if (rem == 0) {
                        count = count;
                    } else {
                        count = count + (5 - rem);
                    }
                    o.params.limit = count;
                } else {
                    o.params.limit = this.pP.combo != undefined ? this.pP.combo.value : 30;
                }

                o.params.groupid = recData.id;
                o.params.moduleIds = recData.moduleIds;
                o.params.fieldlabel = recData.name;
                o.params.ss = this.masterItemSearch.getValue();
            }
        }
    },
    handleLoad2:function(store){
//       Wtf.uncheckSelAllCheckbox1(this.MasterItemSm);
       Wtf.MessageBox.hide();
       
       // For hiding email column other than 'Sales Person'
       var typeColumn = this.MasterItemGrid.colModel.config[2];
       var nameColumn = this.MasterItemGrid.colModel.config[4];
       var emailColumn = this.MasterItemGrid.colModel.config[5];
       var userColumn = this.MasterItemGrid.colModel.config[6];
       var acitveDeacitveColumn = this.MasterItemGrid.colModel.config[7];
       var description = this.MasterItemGrid.colModel.config[8];
       var section = this.MasterItemGrid.colModel.config[9];
       var typeOfDeducteeTypeColumn = this.MasterItemGrid.colModel.config[10];
       var vatCommodityCode = this.MasterItemGrid.colModel.config[11];
       var vatScheduleNo = this.MasterItemGrid.colModel.config[12];
       var vatScheduleSerialNo = this.MasterItemGrid.colModel.config[13];
       var vatNotes = this.MasterItemGrid.colModel.config[14];
       var landingcostcategory = this.MasterItemGrid.colModel.config[15];
       var landingcostallocationtype = this.MasterItemGrid.colModel.config[16];
       var salespersoncode = this.MasterItemGrid.colModel.config[17];
       var salesPersonContactNumber = this.MasterItemGrid.colModel.config[18];
       var salesPersonAddress = this.MasterItemGrid.colModel.config[19];
       var salesPersonDesignation = this.MasterItemGrid.colModel.config[20];
       var taxTypeCode = this.MasterItemGrid.colModel.config[21];
       taxTypeCode.hidden = true;
       
        var masterItemID=this.masterSm.getSelected().data.id;
       landingcostcategory.hidden=true;
       landingcostallocationtype.hidden=true;
       if( masterItemID== 15 || masterItemID==20 || masterItemID==40) {
            emailColumn.hidden = false;
            userColumn.hidden = false;
            typeColumn.hidden=false;
            typeColumn.width=215;
            nameColumn.width=215;            
            nameColumn.hidden = true;
            description.hidden=true;
            section.hidden=true;
            typeOfDeducteeTypeColumn.hidden=true;
            vatCommodityCode.hidden = true;
            vatScheduleNo.hidden = true
            vatScheduleSerialNo.hidden = true
            vatNotes.hidden = true
            if(masterItemID==20 || masterItemID==40){
                acitveDeacitveColumn.hidden =true;
            } else {
                acitveDeacitveColumn.hidden =false;
            }
            if (masterItemID == 15) {
                salespersoncode.hidden = false;
                salesPersonAddress.hidden = false;
                salesPersonContactNumber.hidden = false;
                salesPersonDesignation.hidden = false;
            } else {
                salespersoncode.hidden = true;
                salesPersonAddress.hidden = true;
                salesPersonContactNumber.hidden = true;
                salesPersonDesignation.hidden = true;
            }
            this.MasterItemGrid.getView().refresh(true);
        } else if(masterItemID== 33)
        {
            emailColumn.hidden = true;
            typeColumn.hidden=true;
            nameColumn.hidden=true;
            userColumn.hidden = true;
            acitveDeacitveColumn.hidden=true;
            typeOfDeducteeTypeColumn.hidden=true;
            description.hidden=false;
            section.hidden=false;
            
            vatCommodityCode.hidden = true;
            vatScheduleNo.hidden = true
            vatScheduleSerialNo.hidden = true
            vatNotes.hidden = true
            
            this.MasterItemGrid.getView().refresh(true);
        }else if(masterItemID== 60){
            chkLandingCostCategoryload();
            emailColumn.hidden = true;
            typeColumn.hidden=true;
            nameColumn.hidden=true;
            userColumn.hidden = true;
            acitveDeacitveColumn.hidden=true;
            typeOfDeducteeTypeColumn.hidden=true;
            description.hidden=true;
            section.hidden=true;
            vatCommodityCode.hidden = true;
            vatScheduleNo.hidden = true
            vatScheduleSerialNo.hidden = true
            vatNotes.hidden = true;
            landingcostcategory.hidden=false;
            landingcostallocationtype.hidden=false;
            this.MasterItemGrid.getView().refresh(true);
        }else if(masterItemID== 34){
            typeColumn.hidden=false;
            typeColumn.width=430;
            nameColumn.width=430;
            nameColumn.hidden = true;
            description.hidden=true;
            emailColumn.hidden = true;
            userColumn.hidden = true;
            section.hidden=true;
            typeOfDeducteeTypeColumn.hidden=false;
            acitveDeacitveColumn.hidden =true 
            
            vatCommodityCode.hidden = true;
            vatScheduleNo.hidden = true
            vatScheduleSerialNo.hidden = true
            vatNotes.hidden = true
            
            this.MasterItemGrid.getView().refresh(true);
        }else if(masterItemID== 42){
            vatCommodityCode.hidden=false;
            vatScheduleNo.hidden = false
            vatScheduleSerialNo.hidden = false
            vatNotes.hidden = false
            typeColumn.hidden=false;            
            
            emailColumn.hidden = true;
            nameColumn.hidden=true;
            userColumn.hidden = true;
            acitveDeacitveColumn.hidden=true;
            typeOfDeducteeTypeColumn.hidden=true;
            description.hidden=true;
            section.hidden=true;
            
            this.MasterItemGrid.getView().refresh(true);
        } else if (masterItemID == 65) {
            /* masterItemID = 65 for Tax Type.
             * Displayed Tax Type Code column &
             * Hidden Sales Person related columns.
             */
            taxTypeCode.hidden = false;
            
            emailColumn.hidden = true;
            nameColumn.hidden = true;
            userColumn.hidden = true;
            acitveDeacitveColumn.hidden = true;
            typeOfDeducteeTypeColumn.hidden = true;
            description.hidden = true;
            section.hidden = true;
            salespersoncode.hidden = true;
            salesPersonAddress.hidden = true;
            salesPersonContactNumber.hidden = true;
            salesPersonDesignation.hidden = true;
            
            this.MasterItemGrid.getView().refresh(true);
        }else{
            typeColumn.hidden=false;
            typeColumn.width=430;
            nameColumn.width=430;
            nameColumn.hidden = true;
            description.hidden=true;
            section.hidden=true;
            typeOfDeducteeTypeColumn.hidden=true;
            acitveDeacitveColumn.hidden =true 
            
            vatCommodityCode.hidden = true;
            vatScheduleNo.hidden = true
            vatScheduleSerialNo.hidden = true
            vatNotes.hidden = true
            this.MasterItemGrid.getView().refresh(true);
        }
       
       this.masterItemSearch.StorageChanged(store);


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
    
    viewIBGDetails:function(){
        var recData = this.masterGrid.getSelectionModel().getSelected().data;
        if(recData.id == 17 && this.ibgDetails){}
        this.recArr = this.MasterItemSm.getSelections();
        if(this.MasterItemSm.hasSelection()==false||this.MasterItemSm.getCount()>1){
            WtfComMsgBox(['Information',"Please Select a record first"],3);
            return;
        }
        var rec=this.recArr[0];
        this.callIBGDetailsGrid(rec);
    },
    
    callIBGDetailsGrid:function(rec){
        var ibgDetailsGrid = new Wtf.account.VendorIBGDetailsGrid({
            title:'Receiving Bank Details',
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            isFromMasterConfiguration:true,
            height:500,
            width:700,
            accRec:rec,
            modal:true,
            layout:'border'
        });

        ibgDetailsGrid.show();
    },
    
    AddMasterItem:function (isEdit,id,outer){
        this.ispropagatetochildcompanyflag=false;
        var editParent="";
        var editComboValueDescription="";
        var editParentMapping="", editUserMapping="", editCustVendCategoryType="", editDriverMapping = "",editAccMapping="";
        var selectedGridSelectionModel = this.getSelModel();
        if(isEdit && selectedGridSelectionModel.getCount()>1){
            Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.field.MasterConfig.EditingError"),WtfGlobal.getLocaleText("acc.field.MasterConfig.Canteditmultiplerecordssimultaneously"));
            return;
        }else{
            //ERM-210 Dimensions on transaction level - [Add Master Item]
            var rec=null;
            var groupName = null;
            var moduleIds = null;
            var masterRec = null;
            if(this.isTransationFormEntry) {
                for (var index = 0; index < this.masterStore.getCount(); index++) {
                    masterRec = this.masterStore.getAt(index);
                    moduleIds = masterRec.data.moduleIds;
                    if (moduleIds != undefined && moduleIds != "") {    
                        if(moduleIds.indexOf(this.fieldid) != -1) {
                            groupName = masterRec.data.name;
                            break;
                        }
                    }
                }
            } else {                
                masterRec = WtfGlobal.searchRecord(this.masterStore, id, 'id');
                groupName = masterRec.data['name'];
                moduleIds = masterRec.data['moduleIds'];
            }
            if(isEdit && selectedGridSelectionModel.hasSelection()){
                rec=selectedGridSelectionModel.getSelected();
                editParent=rec.data['parentid']
                editParentMapping=rec.data['parentmappingid']
                editComboValueDescription = rec.data['itemdescription']
               if(this.masterSm.getSelected().data.id == 15 || this.masterSm.getSelected().data.id == 20 || this.masterSm.getSelected().data.id == 40){
                    editUserMapping=rec.data['userid'];
               } 
                if(this.masterSm.getSelected().data.id == 7 || this.masterSm.getSelected().data.id == 8){
                    editCustVendCategoryType = rec.data['typeid'];
                } 
                if(this.masterSm.getSelected().data.id == 57 || this.masterSm.getSelected().data.id == 33 || this.masterSm.getSelected().data.id == 64){
                    editAccMapping=rec.data['accid'];
               } 
                if (this.masterSm.getSelected().data.id == 25) {
                    editDriverMapping = rec.data['driverID'];
                }
            } 
            
            //MRP - Allow master items of Quality Group to be seleted as parent for Quality Parameter
            this.MasterItemParentStore = new Wtf.data.Store({
                url: this.isTransationFormEntry ? "ACCMaster/getMasterItemsForCustomFoHire.do" : "ACCMaster/getMasterItems.do",
                reader:this.MasterItemReader,
                baseParams:{
                    mode:112
                }
            });
                    
            var MasterItemTempStore = new Wtf.data.Store();
            var MasterItemTempIndStore = new Wtf.data.Store();
            var re = new Wtf.data.Record({
                id: "-1",
                name: "None"
            });
            if(Wtf.account.companyAccountPref.countryid == '137'){
                MasterItemTempIndStore.insert(0, re);
                if(masterRec.data.id == '19'){
                    this.MasterItemParentStore.load({
                        params:{
                            groupid:Wtf.MasterConfig_IndustryCode
                        }
                    });
                    this.MasterItemParentStore.on('load', function(){
                        this.MasterItemParentStore.each(function(record) {
                            if (isEdit && rec.data['id'] == record.data['id']) {

                            }else{
                                MasterItemTempIndStore.add(record.copy());
                            }
                        });      
                        if(isEdit){
                            this.industryCodeCmb.setValue(rec.data['industryCodeId']);
                        }
                    }, this);
                }
            }
            MasterItemTempStore.insert(0, re);
            if(masterRec.data.id == Wtf.MasterConfig_Quality_Parameter){
                this.MasterItemParentStore.load({
                    params:{
                        groupid: Wtf.MasterConfig_Quality_Group
                    }
                });
                this.MasterItemParentStore.on('load', function(){
                    this.MasterItemParentStore.each(function(record) {
                        if (isEdit && rec.data['id'] == record.data['id']) {

                        }else{
                            MasterItemTempStore.add(record.copy());
                        }
                    });
                
                    if(isEdit){
                        if (editParent === '' || editParent === undefined) {
                            this.parentCombo.setValue('-1');
                        }else{
                            this.parentCombo.setValue(editParent);
                        }
                    }
                }, this);
            }else{
                //Master item parent selection combo store
                if(this.isTransationFormEntry) {
                    this.MasterItemParentStore.load({
                        params: {
                            groupid: this.fieldid
                        },
                        callback: function (recordArr) {
                            MasterItemTempStore.add(recordArr);
                        },
                        scope: this
                    });
                } else {
                    this.MasterItemStore.each(function(record) {
                        if (isEdit && rec.data['id'] == record.data['id']) {

                        } else {
                            MasterItemTempStore.add(record.copy());
                        }
                    });
                }
            }
            var hideParentCmb=true;
            var hidComboValueDesc = true;
            var hideParentItemCmb=true;
            
            if ((masterRec && masterRec.data['modulename'] != undefined && masterRec.data['modulename'] != "" ) || masterRec.data.id == '19' || masterRec.data.id == Wtf.MasterConfig_Quality_Parameter) {//Group ID 19 - product category or Group ID 55 - Quality Paramter
              hideParentCmb=false;
            }
            if(id=='16'){
                hideParentCmb=true
                groupName="Type";
                if(isEdit ) {
                    rec=this.MasterItemSm.getSelected();
                    editParent=rec.data['type']
                }   
            }
            if(masterRec && masterRec.data['fieldtype'] == 12 ){
                 hideParentCmb=true;
            }
            if(masterRec && masterRec.data['fieldtype'] == 4 || masterRec && masterRec.data['fieldtype'] == 7 ){
                 hidComboValueDesc=false;
            }
             if (masterRec && masterRec.data['itemparentid'] != undefined && masterRec.data['itemparentid'] != "" ) {
                 hideParentItemCmb=false;
            }
            if((WtfGlobal.GSTApplicableForCompany() == Wtf.GSTStatus.NEW) && (masterRec.data.gstconfigtype == Wtf.GST_CONFIG_TYPE.ISFORMULTIENTITY || masterRec.data.gstconfigtype == Wtf.GST_CONFIG_TYPE.ISFORGST)){
                hideParentCmb=true;  // In case of "Entity" parent combo is hidden            
            }
            var hideEmailField = true , hideUserField = true, hideSalesPersonCode = true, hideSalesPersonContactNumber = true, hideSalesPersonAddress = true,hideSalesPersonDesignation = true;
            if(masterRec && (masterRec.data.id == '15' || masterRec.data.id == '20' || masterRec.data.id == '40')) { // Group ID 15 - Sales Person, 20 -Sales Person, 40 - Work center manager
                hideUserField = false;
                hideEmailField = false;
                hideSalesPersonCode = false;
                hideSalesPersonContactNumber = false;
                hideSalesPersonAddress = false;
                hideSalesPersonDesignation =false;
            } else if(masterRec && masterRec.data.id == '23'){// for Assigned to Field
                hideEmailField = false;
            } else if(masterRec && masterRec.data && (masterRec.data.id == '65')) {
                /*
                 * Displayed Tax Type Code field in create case.
                 */
                hideSalesPersonCode = false;
            } 
            var hideCustVendCategoryTypeField = true ;
            if(masterRec && (masterRec.data.id == '7'|| masterRec.data.id == '8')) { // Group ID 7 - Customer Category OR Group ID 8 - Vendor Category
              hideCustVendCategoryTypeField = false;
            }
            
            var hideDefaulToPOS = true  
            if(masterRec.data.id == '19' && Wtf.account.companyAccountPref.integrationWithPOS){
                hideDefaulToPOS = false;
            }else{
                hideDefaulToPOS = true  
            }
             
            var hideDriverField = true;
            if (masterRec && masterRec.data.id == '25') { // Group ID 25 - Vehicle Number
                hideDriverField = false;
            }
            var hideNatureOfPaymentsection = true;
            var hideNatureOfPaymentDesc=true;
            if (masterRec && masterRec.data.id == '33') { // Group ID 33 - Default Nature Of Payment -Indian Company
                hideNatureOfPaymentsection = false;
                hideNatureOfPaymentDesc=false;
            }
            var hideTypeOfdeducteeType=true;
            if (masterRec && masterRec.data.id == '34') { // Group ID 34 - Deductee type -Indian Company
                hideTypeOfdeducteeType = false;
            }
            
             var hideLCCategory = true;
            var hideLCAllocationType=true;
            if (masterRec && masterRec.data.id == '60') { // Group ID 33 - Default Nature Of Payment -Indian Company
                hideLCCategory = false;
                hideLCAllocationType=false;
            }
            var hideBICCode=true;
            var hideBankCode=true;
            var hideBranchCode=true;
            var hideIsAppendBranchCode=true;
            if(masterRec && masterRec.data.id == Wtf.MasterConfig_BankName && Wtf.account.companyAccountPref.activateIBGCollection){
                hideBICCode=false;
                hideBankCode=false;
                hideBranchCode=false;
                hideIsAppendBranchCode=false;
            }
            this.parentComboValueRec = Wtf.data.Record.create ([
                {
                    name:'id'
                },

                {
                    name:'name'
                }
            ]);
            this.parentComboValueStore=new Wtf.data.Store({
                url: "ACCMaster/getMasterItemsForCustomFoHire.do",
                baseParams:{
                    mode:112
                },
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                },this.parentComboValueRec)
            });
          
            this.MSComboconfig = {
                hiddenName:"parentValueid",
                //id:"orderNumber"+this.heplmodeid+this.id,         
                store: this.parentComboValueStore,
                valueField:'id',
                hidden:hideParentItemCmb,
                hideLabel:hideParentItemCmb,
                displayField:'name',
                mode: 'local',
                typeAhead: true,
                selectOnFocus:true,                            
//                allowBlank:false,
                triggerAction:'all',
                scope:this
            };
            //ERM-210 Dimensions on transaction level
            if (this.isTransationFormEntry) {
                //Custom dimention parent combo value store
                this.parentComboValueStore.load({
                    params: {
                        mode: 112,
                        groupid: this.parentid
                    },
                    scope: this
                });
            } else if(!hideParentItemCmb){
                var parentrec=this.masterSm.getSelected();                
                
                this.parentComboValueStore.on('beforeload',function(s,o){
                    if(!o.params)o.params={};
                    var currentBaseParams = this.parentComboValueStore.baseParams;
                    currentBaseParams.groupid=parentrec.data.itemparentid;
                    currentBaseParams.mode=112;
                    this.parentComboValueStore.baseParams=currentBaseParams;
                },this);
                this.parentComboValueStore.load();
            }
            

            this.parentComboValueCombo = new Wtf.common.Select(Wtf.applyIf({
                multiSelect:true,
                //style: "margin-left:30px;",
                labelWidth : '130',
                fieldLabel:WtfGlobal.getLocaleText("acc.dimension.parentCmbVal"),
                forceSelection:true,
                width:200
            },this.MSComboconfig));
             this.parentComboValueStore.on('load',function(s,o){
                if(isEdit){
                    this.parentComboValueCombo.setValue(editParentMapping);
                }
            },this);
            this.parentCombo = new Wtf.form.ComboBox({
                triggerAction: 'all',
//                hidden: false,
                mode: 'local',
                labelWidth : '130',
                valueField: 'id',
                hidden:hideParentCmb,
                hideLabel:hideParentCmb,
                displayField: 'name',
                store: MasterItemTempStore,
                fieldLabel: WtfGlobal.getLocaleText("acc.field.SelectParent"),
                value: editParent,
                width:200,
                typeAhead: true,
                forceSelection: true,
                hiddenName: 'parent'
            });
            this.industryCodeCmb = new Wtf.form.ComboBox({
                triggerAction: 'all',
                mode: 'local',
                name: 'industryCodeId',
                hiddenName: 'industryCodeId',
                labelWidth : '130',
                valueField: 'id',
                displayField: 'name',
                hidden:true,    //  Currently product level industry code functionality  is hidden
                hideLabel:true,
                store: MasterItemTempIndStore,
                fieldLabel:WtfGlobal.getLocaleText("acc.Industry.code"),
                width:200,
                typeAhead: true,
                forceSelection: true
            });
            this.variancePercentage = new Wtf.form.NumberField({
                fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.variancePercentage.toolTip") + "'>" +WtfGlobal.getLocaleText("acc.field.variancePercentage")+ "</span>", // "Variance %",
                name: 'variancePercentage',
                hiddenName: 'variancePercentage',
                allowNegative: false,
                allowBlank: true,
                value: isEdit ? rec.data['variancePercentage'] : 0,
                minValue: 0,
                maxValue: 100,
                width: 200
            });
            
            this.variancePercentage.on('change', function(comp, newValue, oldValue) {
                if (this.variancePercentage.getValue() > 100) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), "Variance % value cannot be greater than 100."],2);
                    this.variancePercentage.setValue(oldValue);
                }
            }, this);
            
            this.appsuserStoreRec = new Wtf.data.Record.create([
            {
                name: 'username'
            },
            {
                name: 'department'
            }          
            ]);
            
            this.userStoreRec = new Wtf.data.Record.create([
            {
                name: 'userid'
            },
            {
                name: 'username'
            },
            {
                name: 'department'
            },
            {
                name: 'emailid'
            },
            {
                name: 'contactno'
            },
            {
                name: 'fullname'
            },
            {
                name: 'designation'
            },
            {
                name: 'address'
            },
            {
                name: 'employeeid'
            }            
            ]);
            this.userStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    totalProperty:'count',
                    root: "data"
                },this.userStoreRec),
                url : "ProfileHandler/getAllUserDetails.do"
            });
            this.userCombo = new Wtf.form.ComboBox({
                triggerAction:'all',
                mode: 'local',
                id:'userComboBox'+this.id,
                fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.user"),
                //style: "margin-left:30px;",
                valueField:'userid',
                value:editUserMapping,
                displayField:'username',
                store:this.userStore,
                width:200,
                hidden: hideUserField,
                hideLabel: hideUserField,
                typeAhead: true,
                forceSelection: true,
                hiddenName:'users'
            });       
            this.userStore.on('load',function(s,o){
                if(isEdit){
                    this.userCombo.setValue(editUserMapping);
                }
            },this);
             this.appsuserStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    root: "data"
                },this.appsuserStoreRec),
                url : "ProfileHandler/getUserDetailsFromApps.do",
                params:{
                    userid :this.userCombo.getValue()
                }
            });
            this.userStore.load();
            this.userCombo.on('select',function(){
                if(this.linkwithUser.getValue()==true && this.userCombo.getValue()!=''){
                    this.appsuserStore.load();
                    this.appsuserStore.on('load', function () {
                        var rec=WtfGlobal.searchRecord(this.userStore,this.userCombo.getValue(),'userid');
                        var appsUserrec=this.appsuserStore.getAt(0);
                         if(rec!=undefined && appsUserrec!=undefined){
                             this.setSalesPersonData(rec,appsUserrec);
                        }
                    },this);
                }
                else if(!isEdit){
                    this.salesPersonCode.reset()
                    Wtf.getCmp("masteritememailid").reset()
                    Wtf.getCmp("masteritemname").reset()
                    this.salesPersonContactNumner.reset()
                    this.salesPersonAddress.reset()
                    this.salesPersonDesignation.reset()
                }
            },this);
            
            this.Rec = new Wtf.data.Record.create([
                {
                    name: 'accname'
                },{
                    name:'acccode'
                },{
                    name:'accid'  
                },{
                    name:'groupname'
            }]);
    
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'totalCount'
            },this.Rec),
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                ignorecustomers	:true,
                ignorevendors	:true,
                mode            :2,
                nondeleted	:true,
                nature: masterRec.data.id == '33' ? 0 : 2
            }
    });
            this.accCombo = new Wtf.form.ComboBox({                        //Account mapped to Cashout Reason for POS
                triggerAction:'all',
                mode: 'local',
                id:'accComboBox'+this.id,
                fieldLabel:WtfGlobal.getLocaleText("acc.het.514") + (masterRec.data.id == '64' ? "*" : ""),
                //style: "margin-left:30px;",
                valueField:'accid',
//                value:editAccMapping,
                displayField:'accname',
                allowBlank: masterRec && (masterRec.data.id == '33' || masterRec.data.id == '64') ? false : true,
                store:this.store,
                width:200,
                hidden: masterRec && (masterRec.data.id == '57' || masterRec.data.id == '33' || masterRec.data.id == '64')?false:true,
                hideLabel: masterRec && (masterRec.data.id == '57' || masterRec.data.id == '33' || masterRec.data.id == '64')?false:true,
                typeAhead: true,
                forceSelection: true,
                hiddenName:'Accounts'
            });       
            this.store.on('load',function(s,o){
                if(isEdit){
                    this.accCombo.setValue(editAccMapping);
                }
            },this);
            this.store.load();
            this.sequenceFormatStoreRec = new Wtf.data.Record.create([
                {name: 'id'},
                {name: 'value'},
                {name: 'oldflag'}
            ]);
            this.sequenceFormatStore = new Wtf.data.Store({
                reader: new Wtf.data.KwlJsonReader({
                    totalProperty: 'count',
                    root: "data"
                }, this.sequenceFormatStoreRec),
                url: "ACCCompanyPref/getSequenceFormatStore.do",
                baseParams: {
                    mode: "autodimensionnumber",
                    masterid: (this.isTransationFormEntry) ? moduleIds.split(",")[0] : (id.length > 15) ? this.masterGrid.getSelectionModel().getSelected().data["id"] : ""
                }
            });
            this.sequenceFormatCombobox = new Wtf.form.ComboBox({
                triggerAction: 'all',
                mode: 'local',
                id: 'sequenceFormatCombobox' + this.id,
                fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.Sequenceformat.tip") + "'>" + WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat") + "</span>",
                valueField: 'id',
                displayField: 'value',
                store: this.sequenceFormatStore,
                width: 200,
                typeAhead: true,
                forceSelection: true,
                name: 'sequenceformat',
                hiddenName: 'sequenceformat',
                allowBlank: false
            });
            this.sequenceFormatStore.load();
            this.sequenceFormatCombobox.on('select',function(combo,record,index){
                if(record.data.value!='NA'){
                    Wtf.getCmp('masteritemname').setValue(record.json.nextno)
                    Wtf.getCmp('masteritemname').disable();
                }else{
                    Wtf.getCmp('masteritemname').enable();
                    Wtf.getCmp('masteritemname').setValue("");
                }
            },this);
            this.sequenceFormatStore.on('load',function(store,records,options){
                if (id.length > 15) {
                    if (!isEdit) {
                        if (records.length > 1) {
                            for (var i = 0; i < records.length; i++) {
                                if (records[i].json.isdefaultformat == "Yes") {
                                    this.sequenceFormatCombobox.setValue(records[i].data.id);
                                    Wtf.getCmp('masteritemname').setValue(records[i].json.nextno)
                                    Wtf.getCmp('masteritemname').disable();
                                }
                            }
                        } else {
                            this.sequenceFormatCombobox.setValue("NA");
                        }
                    } else {
                        rec = selectedGridSelectionModel.getSelected();
                        this.sequenceFormatCombobox.setValue(rec.data.sequenceformat);
                        Wtf.getCmp('masteritemname').setValue(rec.data.name)
                    }
                }
            },this);
            
           this.linkwithUser = new Wtf.form.Checkbox({
                name: 'linkwithUser',
                fieldLabel: WtfGlobal.getLocaleText("acc.salesperson.linkwithUser"), 
                labelStyle:'padding:0px;'
                
            }); 
            this.linkwithUser.on('check',function(){
                if(this.linkwithUser.getValue()==true && this.userCombo.getValue()!=''){
                    //  this.userCombo.enable();
                    this.appsuserStore.load();
                    var appsUserrec;
                    this.appsuserStore.on('load', function () {
                        var rec=WtfGlobal.searchRecord(this.userStore,this.userCombo.getValue(),'userid');
                        appsUserrec=this.appsuserStore.getAt(0);
                        if(rec!=undefined && appsUserrec!=undefined){
                          this.setSalesPersonData(rec,appsUserrec);
                        }
                    },this);
                    
                }
                else if(!isEdit){
                    //this.userCombo.disable();
                    this.salesPersonCode.reset()
                    Wtf.getCmp("masteritememailid").reset()
                    Wtf.getCmp("masteritemname").reset()
                    this.salesPersonContactNumner.reset()
                    this.salesPersonAddress.reset()
                    this.salesPersonDesignation.reset() 
                }
                else{
                    var rec=selectedGridSelectionModel.getSelected();
                    this.salesPersonCode.setValue(rec.data['salespersoncode']);
                    this.salesPersonContactNumner.setValue(rec.data['salesPersonContactNumber']);
                    this.salesPersonAddress.setValue(rec.data['salesPersonAddress']);
                    this.salesPersonDesignation.setValue(rec.data['salesPersonDesignation']);
                    Wtf.getCmp("masteritememailid").setValue(rec.data['emailid']);
                    Wtf.getCmp("masteritemname").setValue(rec.data['name']);
                }
            },this)
            this.salesPersonCode = new Wtf.form.TextField({
                fieldLabel: (masterRec.data.id == '65') ? groupName : groupName + ' ' + WtfGlobal.getLocaleText("acc.salesperson.code") + "*",
                name: 'salespersoncode',
                hiddenName: 'salespersoncode',
                width:200,
                maxLength: (masterRec.data.id == '65') ? 500 : 20,
                //style: "margin-left:30px;",
                allowBlank:hideSalesPersonCode,
                hideLabel:hideSalesPersonCode,
                hidden:hideSalesPersonCode
                        });
            this.comboValueDescription = new Wtf.form.TextArea({
                fieldLabel: WtfGlobal.getLocaleText("acc.field.description"),//Contact Number
                name: 'itemdescription',
//                maxLength:255,
                width:200,
                //style: "margin-left:30px;",
                allowBlank:true,
                value:editComboValueDescription,
                hideLabel:hidComboValueDesc,
                hidden:hidComboValueDesc
            });
            
            this.salesPersonContactNumner = new Wtf.form.TextField({
                fieldLabel: (groupName) + ' ' +WtfGlobal.getLocaleText("acc.salesperson.contact.number"),//Contact Number
                name: 'salesPersonContactNumber',
                hiddenName: 'salesPersonContactNumber',
                width:200,
                maxLength:15,
                //style: "margin-left:30px;",
                allowBlank:true,
                hideLabel:hideSalesPersonContactNumber,
                hidden:hideSalesPersonContactNumber
            });
            
            this.salesPersonAddress = new Wtf.form.TextArea({
                fieldLabel:(groupName) + ' ' +WtfGlobal.getLocaleText("acc.salesperson.address"),//Address
                name:"salesPersonAddress",
                hiddenName: 'salesPersonAddress',
                maxLength:200,
                height:30,
                allowBlank:true,
                allowNegative:false,
                //style: "margin-left:30px;",
                width:200,
                hideLabel:hideSalesPersonAddress,
                hidden:hideSalesPersonAddress
            });
              this.salesPersonDesignation = new Wtf.form.TextField({
                fieldLabel: (groupName) + ' ' +WtfGlobal.getLocaleText("acc.salesperson.designation"),//Designation
                name: 'salesPersonDesignation',
                hiddenName: 'salesPersonDesignation',
                width:200,
                maxLength:1024,
                //style: "margin-left:30px;",
                allowBlank:true,
                hideLabel:hideSalesPersonDesignation,
                hidden:hideSalesPersonDesignation
            });
            
            this.BICCode=new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.uob.receivingBICCode.qtip") + "'>" + WtfGlobal.getLocaleText("acc.uob.receivingBICCode") + "* </span>",
                name: 'uobbiccode',
                hidenName: 'uobbiccode',
                disabled:hideBICCode,
                disabledClass:"newtripcmbss",
                id:"uobbiccode"+this.id,
                width : 200,
                maxLength:11,
                hidden:hideBICCode,
                hideLabel:hideBICCode,
                scope:this,
                allowBlank:false
            }),
            this.BICCode.on('blur',function(obj){
                obj.setValue(obj.getValue().trim());
            },this);
            this.bankCode=new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.masterCoonfig.bankCode") + "'>" + WtfGlobal.getLocaleText("acc.masterCoonfig.bankCode") + "</span>",
                name: 'bankcode',
                hidenName: 'bankcode',
                disabled:hideBankCode,
                disabledClass:"newtripcmbss",
                id:"bankcode"+this.id,
                hidden:hideBankCode,
                hideLabel:hideBankCode,
                width : 200,
                maxLength:20,
                scope:this
            });
            this.bankCode.on('change',function(obj){
                obj.setValue(obj.getValue().replace(/[-\[\]\/\{\}\(\)\*\+\?\\\^\$\|\@\%\#\&\.\,\'\"\;\:\<\>\!\~\`\_]/g, ""));
            },this);
            this.branchCode=new Wtf.form.TextField({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.vendorIBG.BranchCode") + "'>" + WtfGlobal.getLocaleText("acc.vendorIBG.BranchCode") + " </span>",
                name: 'branchcode',
                hidenName: 'branchcode',
                disabled:hideBranchCode,
                disabledClass:"newtripcmbss",
                id:"branchcode"+this.id,
                hidden:hideBranchCode,
                hideLabel:hideBranchCode,
                width : 200,
                maxLength:20,
                scope:this
            });
            this.branchCode.on('change',function(obj){
                obj.setValue(obj.getValue().replace(/[-\[\]\/\{\}\(\)\*\+\?\\\^\$\|\@\%\#\&\.\,\'\"\;\:\<\>\!\~\`\_]/g, ""));
            },this);
            
            this.isAppendBranchCode = new Wtf.form.Checkbox({
                fieldLabel:"<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.vendorIBG.isAppendBranchCodeWithAccountNumber") + "'>" + WtfGlobal.getLocaleText("acc.vendorIBG.isAppendBranchCodeWithAccountNumber") + " </span>",
                name:'isappendbranchcode',
                hidenName: 'isappendbranchcode',
                disabled: hideIsAppendBranchCode,
                disabledClass:"newtripcmbss",
                id:"isappendbranchcode"+this.id,
                hidden: hideIsAppendBranchCode,
                hideLabel: hideIsAppendBranchCode,
                width : 200,
                maxLength:20,
                scope:this
            });
            this.driverCombo = new Wtf.form.ComboBox({
                fieldLabel: WtfGlobal.getLocaleText("acc.field.driver"), // "Driver",
                name: 'name',
                hiddenName: 'driver',
                triggerAction: 'all',
                store: Wtf.driverStore,
                mode: 'local',
                valueField: 'id',
                displayField: 'name',
                width: 200,
                typeAhead: true,
                forceSelection: true,
                hidden: hideDriverField,
                hideLabel: hideDriverField
            });
            if (masterRec && masterRec.data.id == '25') { // Group ID 25 - Vehicle Number
                Wtf.driverStore.load();
                
                Wtf.driverStore.on('load',function() {
                    if (isEdit) {
                        this.driverCombo.setValue(editDriverMapping);
                    }
                },this);
            }
            
            if(isEdit && (id =='15' || id =='20' || id =='40')){
                this.salesPersonCode.setValue(rec.data['salespersoncode']);
                this.salesPersonContactNumner.setValue(rec.data['salesPersonContactNumber']);
                this.salesPersonAddress.setValue(rec.data['salesPersonAddress']);
                this.salesPersonDesignation.setValue(rec.data['salesPersonDesignation']);
            }
            
            if (isEdit && (id == '65')) {
                //To set value in edit case to Tax Type Code - ERP-41555
                this.salesPersonCode.setValue(rec.data['salespersoncode']);
            }
            
            var custVendCategoryTypeArr = new Array();
            if(masterRec && masterRec.data.id == '7'){
                custVendCategoryTypeArr.push(['0','Cash Customer']);
                custVendCategoryTypeArr.push(['1','Credit Customer']);
            }else if(masterRec && masterRec.data.id == '8'){
                custVendCategoryTypeArr.push(['0','Cash Vendor']);
                custVendCategoryTypeArr.push(['1','Credit Vendor']);
            }
            
            this.custVendCategoryTypeStore = new Wtf.data.SimpleStore({
                fields:[{name:'typeid'},{name:'typename'}],
                data:custVendCategoryTypeArr
            });
            
             this.custVendCategoryType = new Wtf.form.ComboBox({
                triggerAction:'all',
                mode: 'local',
                id:'custVendCategoryType'+this.id,
                fieldLabel:WtfGlobal.getLocaleText("acc.field.selectCategoryType"),
                //style: "margin-left:30px;",
                valueField:'typeid',
                value:editCustVendCategoryType,
                displayField:'typename',
                store:this.custVendCategoryTypeStore,
                width:200,
                hidden: hideCustVendCategoryTypeField,
                hideLabel: hideCustVendCategoryTypeField,
                typeAhead: true,
                forceSelection: true,
                hiddenName:'custvendcategorytype'
            }); 
            
            this.masterTypeStore = new Wtf.data.SimpleStore({
                fields: [{
                    name:'typeid',
                    type:'int'
                }, 'name'],
                data :[[0,"Number"],[1,"Drop Down"],[2,"Formula Using Quantity"]]
            });
            this.masterTypeCombo = new Wtf.form.ComboBox({
                triggerAction: 'all',
//                hidden: false,
                mode: 'local',
                disabled:isEdit,
                valueField: 'typeid',
                hidden:(id!='16'),
                hideLabel:(id!='16'),
                displayField: 'name',
                store: this.masterTypeStore,
                fieldLabel: 'Select Type',
                value: (isEdit)?editParent:0,
                typeAhead: true,
                forceSelection: true,
                hiddenName: 'type'
            });  
            
            if(isEdit && id =='17'){
                this.ibgReceivingDetailsArr=rec.data.ibgReceivingDetails!=undefined||rec.data.ibgReceivingDetails!=""?rec.data.ibgReceivingDetails:"";
            }
            
            this.fillIBGDetails = new Wtf.Toolbar.Button({
                text: WtfGlobal.getLocaleText("acc.fillibgDetais"),
                name:'fillIBGDetails',
                tooltip: WtfGlobal.getLocaleText("acc.fillibgDetais"),
                scope: this,
                hidden:!Wtf.account.companyAccountPref.activateIBG || this.isCustomer,
                handler:this.fillIBGDetailsValues
            });
        
            this.isActivateIBG=new Wtf.form.FieldSet({
                title: WtfGlobal.getLocaleText("acc.ibg.bank.info"),
                checkboxToggle: true,
                autoHeight: true,
                disabledClass:"newtripcmbss",
                hidden:true,//!Wtf.account.companyAccountPref.activateIBG || id!='17',// hide if IBG is not activated for this company
                hideLabel:true,//!Wtf.account.companyAccountPref.activateIBG || id!='17',
                autoWidth: true,
                checkboxName: 'activateIBG',
                //style: 'margin-right:30px',
                collapsed: isEdit ? (rec.data['isIbgActivItematedForPaidTo']!=undefined||rec.data['isIbgActivItematedForPaidTo']!=""? !rec.data['isIbgActivItematedForPaidTo']:true):true,
                items:[this.fillIBGDetails]
            });
            this.isDefaultToPOS = new Wtf.form.Checkbox({
            name: 'isDefaultToPOS',
            fieldLabel: WtfGlobal.getLocaleText("acc.field.SetPOSDefaultCategory"), // "Set POS default Category",
            checked: isEdit ?rec.data['isDefaultToPOS']!=undefined||rec.data['isDefaultToPOS']!=""? rec.data['isDefaultToPOS']:false:false,
            ctCls: 'custcheckboxPOS',
            hideLabel:hideDefaulToPOS,
            id : 'combo',
            labelStyle:'padding:0px;',
            hidden:hideDefaulToPOS,
            width: 75
        }); 
            this.natureOfPaymentDesc = new Wtf.form.TextArea({
                fieldLabel: "Description",
                name: 'natureofpaymentdesc',
                hiddenName: 'natureofpaymentdesc',
                width:200,
                maxLength:1024,
                //style: "margin-left:30px;",
                allowBlank:true,
                hideLabel:hideNatureOfPaymentDesc,
                hidden:hideNatureOfPaymentDesc
            });
            this.natureOfPaymentesection = new Wtf.form.TextField({
                fieldLabel: "Section (Code)",
                name: 'natureofpaymentsection',
                hiddenName: 'natureofpaymentsection',
                width:200,
                maxLength:1024,
                //style: "margin-left:30px;",
                allowBlank:true,
                hideLabel:hideNatureOfPaymentsection,
                hidden:hideNatureOfPaymentsection
            });
            if(isEdit && id =='33'){
                this.natureOfPaymentesection.setValue(rec.data['natureofpaymentsection']);
                this.natureOfPaymentDesc.setValue(rec.data['natureofpaymentdesc']);
            }
            
            /*------------ 
            *  Landing Cost Module Default Fields  
            *
            * ERP-20637
            *--------------*/
           chkLandingCostCategoryload();
            this.landingCostCategoryCombo =  new Wtf.form.TextField({
                fieldLabel: WtfGlobal.getLocaleText("acc.invoice.landingCostCategory"), // "Product Brand",
                name: "landingCostCategoryCombo",     
                id: 'landingCostCategoryCombo' + this.id,
                width: 200,
                allowBlank: false,
                hidden:hideLCCategory,
                hideLabel:hideLCCategory              
            });
            this.landingCostAllocationTypeCombo =  new Wtf.form.FnComboBox({
                fieldLabel: "Allocation Type", // "Product Brand",
                name: "landingCostAllocationType",     
                id: 'landingCostAllocationType' + this.id,
                store: (Wtf.Countryid == Wtf.Country.INDIA) ? Wtf.landingCostAllocationTypeStoreIndia : Wtf.landingCostAllocationTypeStore,
                width: 200,
                allowBlank: !Wtf.account.companyAccountPref.isActiveLandingCostOfItem,
                hidden:hideLCAllocationType ,
                hideLabel:hideLCAllocationType,
                valueField: 'id',
                displayField: 'name',
                mode: 'local',
                triggerAction: 'all'
            });
              if(isEdit && id =='60'){
                this.landingCostCategoryCombo.setValue(rec.data['landingcostcategory']);
                this.landingCostAllocationTypeCombo.setValue(rec.data['landingcostallocationtype']);
                this.landingCostAllocationTypeCombo.setDisabled(true);
            }else{
                this.landingCostAllocationTypeCombo.setDisabled(false);
            }
            if(isEdit && id== Wtf.MasterConfig_BankName && Wtf.account.companyAccountPref.activateIBGCollection){
                this.BICCode.setValue(rec.data['BICCode']);
                this.bankCode.setValue(rec.data['bankCode']);
                this.branchCode.setValue(rec.data['branchCode']);
                this.isAppendBranchCode.setValue(rec.data['isAppendBranchCode']);
            }
            
            this.typeOfdeducteeTypeStore = new Wtf.data.SimpleStore({
                fields : ['id', 'name'],
                data: [
                ['0','Corporate'],['1','Non-Corporate']
                ]
            });
            this.typeOfdeducteeTypeCombo = new Wtf.form.FnRefreshBtn({
                fieldLabel: WtfGlobal.getLocaleText("acc.field.activatedeactivatesalesperson.columnheader.type")+"*",//"Type",
                name: "typeofdeducteetype",
                hiddenName:"typeofdeducteetype",
                store: this.typeOfdeducteeTypeStore,
                valueField: 'id',
                displayField: 'name',
                mode: 'local',
                triggerAction: 'all',
                width:200,
                emptyText: WtfGlobal.getLocaleText('acc.field.activatedeactivatesalesperson.columnheader.selecttype'),//'Select a type',
                forceSelection: true,
                allowBlank:hideTypeOfdeducteeType,
                hideLabel:hideTypeOfdeducteeType,
                hidden:hideTypeOfdeducteeType
            });
            if(isEdit && id =='34'){
                if(!Wtf.isEmpty(rec.data.defaultMasterItem)){
                    var msg=WtfGlobal.getLocaleText("acc.masterConfig.cannotDeleteDefault.deducteeType");
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
                    return false;
                }
                this.typeOfdeducteeTypeCombo.setValue(rec.data['typeofdeducteetype']);
            }   
            
            this.vatcommoditycode = new Wtf.form.TextField({
                fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.commoditycode") + "'>" +WtfGlobal.getLocaleText("acc.field.commoditycode")+"*"+ "</span>", // "vat Commodity Code ",
                name: 'vatcommoditycode',
                hiddenName: 'vatcommoditycode',
                allowBlank: false,
                value: isEdit ? rec.data['vatcommoditycode'] : '',
                width: 200,
                maskRe:/[0-9]/,
                regEx:/[0-9]/
            });
            this.vatscheduleno = new Wtf.form.TextField({
                fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.vatscheduleno") + "'>" +WtfGlobal.getLocaleText("acc.field.vatscheduleno")+ "</span>", // "vat Schedule No",
                name: 'vatscheduleno',
                hiddenName: 'vatscheduleno',
                allowBlank: true,
                value: isEdit ? rec.data['vatscheduleno'] : '',
                width: 200,
                maskRe: /[a-zA-Z0-9]/,
                regEx:/[a-zA-Z0-9]/
            });
            this.vatscheduleserialno = new Wtf.form.TextField({
                fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.vatscheduleserialno") + "'>" +WtfGlobal.getLocaleText("acc.field.vatscheduleserialno")+ "</span>", // "vat Schedule Serial No",
                name: 'vatscheduleserialno',
                hiddenName: 'vatscheduleserialno',
                allowBlank: true,
                value: isEdit ? rec.data['vatscheduleserialno'] : '',
                width: 200,
                maskRe: /[a-zA-Z0-9]/,
                regEx:/[a-zA-Z0-9]/
            });
            this.vatnotes = new Wtf.form.TextArea({
                fieldLabel: "<span wtf:qtip='" + WtfGlobal.getLocaleText("acc.field.vatnotes") + "'>" +WtfGlobal.getLocaleText("acc.field.vatnotes")+ "</span>", // "vat Commodity Code %",
                name: 'vatnotes',
                hiddenName: 'vatnotes',
                allowBlank: true,
                value: isEdit ? rec.data['vatnotes'] : '',
                width: 200
//                maskRe: /[a-zA-Z0-9]/
            });
            this.industryCode = new Wtf.form.NumberField({
                fieldLabel: groupName+"*",
                name: 'industryCode',
                hiddenName: 'industryCode',
                allowNegative: false,
                allowBlank: true,
                minValue: 0,
                maxValue: 7,
                width: 200
            });
            
            this.itemArr = []; 
            this.configType=(this.masterGrid.getSelectionModel().getSelected()!=undefined && this.masterGrid.getSelectionModel().getSelected().data.gstconfigtype!=undefined)?this.masterGrid.getSelectionModel().getSelected().data.gstconfigtype:0; //gstconfigtype by default should be 0- (vinod gharge) 
            var isEntityDefaultValueSelcted = false;
            if (isEdit){
                if (this.selectionModel.getSelections().length > 0) {
                    isEntityDefaultValueSelcted = (this.configType == Wtf.GST_CONFIG_TYPE.ISFORMULTIENTITY && this.selectionModel.getSelected().data.name == Wtf.cdomain);
                }
            }
              if (id == '15') { //Group ID 15 - Sales Person
                this.itemArr.push(this.linkwithUser)
            }
            this.itemArr.push(this.userCombo)
            if(Wtf.account.companyAccountPref.countryid == '137'&& masterRec.data.id == '59'){
                this.itemArr.push(this.salesPersonCode,this.natureOfPaymentDesc,this.natureOfPaymentesection,{
                    xtype: 'textfield',
                    fieldLabel: groupName+"*",
                    name: "masteritem",
                    width: 200,
                    maxLength:5,
                    hideLabel:!hideNatureOfPaymentsection,
                    hidden:!hideNatureOfPaymentsection,
                    validator:Wtf.ValidatePaidReceiveName,
                    value:(isEdit)?rec.data['name']:'',
                    allowBlank: !hideNatureOfPaymentsection,
                    id: "masteritemname",
                    maskRe:/[0-9]/,
                    regEx:/[0-9]/
                },this.parentCombo);
            }else{
                if(id.length > 15){
                     this.itemArr.push(this.sequenceFormatCombobox)
                }
                 this.itemArr.push(this.salesPersonCode,this.natureOfPaymentDesc,this.natureOfPaymentesection,{
                    xtype: 'textfield',
                    fieldLabel: (masterRec.data.id == '65') ? groupName + ' ' + WtfGlobal.getLocaleText("acc.salesperson.code") + "*" : groupName+"*",
                    name: "masteritem",
                    //                        msgTarget: 'under',
                    //style: "margin-left:30px;",
                    width: 200,
                    maxLength:500,
                    //                        validator:(!this.paidToFlag)?Wtf.ValidateCustomColumnName:"",
                    hideLabel:!hideNatureOfPaymentsection,
                    hidden:!hideNatureOfPaymentsection,
//                    validator:Wtf.ValidatePaidReceiveName,//remove validator to accept chinese characters 
                    value:(isEdit)?rec.data['name']:'',
                    allowBlank: !hideNatureOfPaymentsection,
                    maskRe:(masterRec && (masterRec.data.fieldtype ==4 || masterRec.data.fieldtype ==7))? Wtf.productNameCommaMaskRe:"",
                    regex:(masterRec && (masterRec.data.fieldtype ==4 || masterRec.data.fieldtype ==7))? Wtf.productNameCommaMaskRe:"",
                    id: "masteritemname",
                    disabled:isEntityDefaultValueSelcted
                },this.parentCombo);
                }
            if (id == '34') { // 34 - Deductee type
                this.itemArr.push(this.typeOfdeducteeTypeCombo);
            }
            if (masterRec.data.id == '19') { // For 'Product Category' add field 'Variance %'
                this.itemArr.push(this.variancePercentage);
                if(Wtf.account.companyAccountPref.countryid == '137'){
                    this.itemArr.push(this.industryCodeCmb)
                }
            }
            if(id=='42'){
                this.itemArr.push(this.vatcommoditycode,this.vatscheduleno,this.vatscheduleserialno,this.vatnotes);
            }
            if(id=='60'){//ERP-20637
                for(var itemi=0;itemi<this.itemArr.length;itemi++){
                   this.itemArr.pop(); 
                }
                this.itemArr.push(this.landingCostCategoryCombo,this.landingCostAllocationTypeCombo);
            }
            if(id==Wtf.MasterConfig_BankName && Wtf.account.companyAccountPref.activateIBGCollection){
                this.itemArr.push(this.BICCode,this.bankCode,this.branchCode,this.isAppendBranchCode);
            }
            if (masterRec.data.isformultientity) {
                this.tagsFieldset = new Wtf.account.CreateCustomFields({
                    border: false,
                    //compId: this.id,
                    autoHeight: true,
                    autoWidth: true,
                   // isViewMode: this.isViewTemplate,
                    parentcompId: this.id,
                    moduleid: 1200,
                    isWindow:true,                    
                    customcolumn: 0,
                    groupname: groupName,
                    isforgstrulemapping: true,
                    dimvalue: isEdit?(this.getSelModel().hasSelection() ? this.getSelModel().getSelected().data.name : ""):"",
                    // isWindow:true,
                    iscallFromTransactionsForm: false,
                    isForMultiEntity: true
                });
            }
            this.itemArr.push(this.parentComboValueCombo,this.masterTypeCombo,this.isActivateIBG,this.comboValueDescription);
               
            if (masterRec.data.isformultientity) {
                this.itemArr.push(this.tagsFieldset)  // if isForMultientity true then push  tagsFieldset
            }
                this.itemArr.push({
                    xtype: 'textfield',
                    fieldLabel: WtfGlobal.getLocaleText("acc.profile.email"),
                    name: "masteritememailid",
                    id: "masteritememailid",
                    value:(isEdit)?rec.data['emailid']:'',
                    hidden:hideEmailField,
                    hideLabel:hideEmailField,
                    //style: "margin-left:30px;",
                    width: 200,
                    maxLength:500,
                    allowBlank: true,
                    vtype:'email'
                },this.isDefaultToPOS,this.accCombo,this.salesPersonContactNumner,this.salesPersonAddress,this.custVendCategoryType,this.salesPersonDesignation,this.driverCombo);
            
            this.addMasterItemForm = new Wtf.form.FormPanel({
                waitMsgTarget: true,
                border: false,
                region: 'center',
                layout:'form',
                bodyStyle: "background: transparent;",
//                border:false,
                style: "background: transparent;padding:20px;",
                labelWidth : '130',
                items: this.itemArr
            });
            this.loadmask = new Wtf.LoadMask(document.body,{msg : WtfGlobal.getLocaleText("acc.msgbox.49")});
            var windowId = this.id + id; //id = id from master, this.id= ID of current component
            var addMasterItemWindow = Wtf.getCmp(windowId);
            if(addMasterItemWindow != undefined){ //If window is already created then show it (ERP-33778)
                this.addMasterItemWindow =  addMasterItemWindow;
            }else{ // if not created create new window
                
                this.addMasterItemWindow = new Wtf.Window({
                    modal: true,
                    id : windowId,
                    title: groupName,
                    iconCls :getButtonIconCls(Wtf.etype.deskera),
                    bodyStyle: 'padding:5px;',
                    buttonAlign: 'right',
                    draggable:false,
                    autoScroll:true,
                    // width: 460,
                    //        height: 115,
                    scope: this,
                    items: [{
                        region: 'center',
                        border: false,
                        bodyStyle: 'background:#f1f1f1;font-size:10px;',
                        autoScroll: true,
                        defaults: {
                            labelWidth: 150
                        },
                        items: [this.addMasterItemForm]
                    }],
                    buttons: [{
                        text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                        scope: this,
                        id: 'addmasteritemsave' + this.id,
                        handler: function(button) {
                            //Wtf.getCmp('addmasteritemsave' + this.id).disable();  
                            var itemName=Wtf.getCmp("masteritemname")?Wtf.getCmp("masteritemname").getValue().trim():"";
                            var emailid=Wtf.getCmp("masteritemname")?Wtf.getCmp("masteritememailid").getValue():"";
                            var accId=this.accCombo.getValue();
                            var parentId=this.parentCombo.getValue();
                            var parentValueid=this.parentComboValueCombo.getValue();
                            var custFieldArr = (masterRec.data.isformultientity)?this.tagsFieldset.createFieldValuesArray(): undefined; // JSON of custom fields is passed in case of isformultientity==true 
                            var userId=this.userCombo.getValue();
                            var custVendCategoryTypeId=this.custVendCategoryType.getValue();
                            // Provided check for HSN/SAC Code should not be greater than 8 digits (For India) ERM-1092
                            if ( WtfGlobal.isIndiaCountryAndGSTApplied() && Wtf.getCmp("masteritemname")!=undefined && (Wtf.getCmp("masteritemname").fieldLabel == Wtf.GSTHSN_SAC_Code + "*") && this.gstConfigType == 5) {
                                    var HSNCode = Wtf.getCmp("masteritemname").getValue().trim();
                                    if (HSNCode.length > Wtf.HSNMaxLength) {
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.product.hsnValidationMsg1")], 2);
                                        return;
                                    }
                                }
                               /*
                                * 
                                * Code for - Description of field Entity should not contain any special characters except A-Za-z0-9#/, &\n-
                                */
                                var fieldLabel =Wtf.getCmp("masteritemname")!= undefined && Wtf.getCmp("masteritemname").fieldLabel!=''?Wtf.getCmp("masteritemname").fieldLabel:'';
                                var index = fieldLabel.lastIndexOf("*");
                                if(index!==-1){
                                var fieldName = fieldLabel.substring(0, index);
                                }
                            if (WtfGlobal.isIndiaCountryAndGSTApplied() && this.gstConfigType == 1 && Wtf.getCmp("masteritemname")!=undefined && (fieldName == Wtf.GSTCustom_Entity) ){
                                var regex = /^[A-Za-z0-9\/#, &\n-]*$/;
                                var description = this.comboValueDescription!=undefined && this.comboValueDescription.getValue()!=''?this.comboValueDescription.getValue():'';
                                if(!regex.test(description)){
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.GST.eway.validation.invalidDescription")], 2);
                                    return;
                                }
                            }
                            /*
                             * this.gstConfigType == 1 is for Entity dimension value Add and Edit
                             */
                            if (WtfGlobal.isIndiaCountryAndGSTApplied() && this.gstConfigType == 1) {
                                var isValid = this.validateEntityCustomFieldsValue();
                                if(!isValid){
                                    return;
                                }
                            }    
                            if (this.addMasterItemForm.form.isValid()) {
                                //                               this.loadmask.show();
                                this.confirmBeforeSave("ok",itemName,isEdit,(isEdit?rec.data['id']:""),id,outer,parentId,parentValueid,moduleIds,emailid,userId,custVendCategoryTypeId,"",accId,custFieldArr); 
                                this.addMasterItemWindow.close();  //To avoid double posting issue
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
            }
            if (masterRec.data.isformultientity) {
                this.addMasterItemWindow.width = 800;
                this.addMasterItemWindow.height = 400; // isFormultientity case where custom data for dimension value is displayed
                this.addMasterItemWindow.doLayout();
            } else {
                this.addMasterItemWindow.width = 460; // normal case 
                this.addMasterItemWindow.doLayout();
            }
//            this.addMasterItemWindow.show();

            if (this.configType == Wtf.GST_CONFIG_TYPE.ISFORMULTIENTITY && this.addMasterItemWindow) {
                this.addMasterItemWindow.on('show', function () {
                    if (this.tagsFieldset && isEdit && (WtfGlobal.isIndiaCountryAndGSTApplied() || WtfGlobal.isUSCountryAndGSTApplied())) {
                        Wtf.getCmp('addmasteritemsave' + this.id).disable();
                        var masterItemName = this.getSelModel().getSelected().data.name;
                        var masterItemId = this.getSelModel().getSelected().data.id;
                        Wtf.Ajax.requestEx({
                            url: "ACCMaster/validateEntityCustomFieldUsage.do",
                            params: {
                                masterItemId: masterItemId,
                                masterItemName: masterItemName
                            }
                        },
                        this,
                                function (response) {
                                    var result = eval(response);
                                    if (result && result.dimensionConfig && typeof result.dimensionConfig == "object" && Object.keys(result.dimensionConfig)) {
                                        var JSONObj = result.dimensionConfig;
                                        for (var i = 0; i < Object.keys(JSONObj).length; i++) {
                                            var key = Object.keys(JSONObj)[i];
                                            if (Wtf.getCmp(key + this.tagsFieldset.id)) {
                                                /*
                                                 * If field is used against enity then It will be disabled so canot be changed..
                                                 */
                                                Wtf.getCmp(key + this.tagsFieldset.id).disable();
                                            }
                                        }
                                    }
                                    Wtf.getCmp('addmasteritemsave' + this.id).enable();
                                },
                                function (response) {
                                    var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
                                });
                    }
                }, this);
            }
            
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
    /**
     * Entity dimension value Add and Edit valudate custom fields
     */
    validateEntityCustomFieldsValue: function () {
        var isValid = true;
        var gstinValue='';
        var customFieldArray = this.tagsFieldset.customFieldArray;
        var customDimensionArray = this.tagsFieldset.dimensionFieldArray;
        if (customFieldArray != null && customFieldArray != undefined && customFieldArray != "") {
            for (var itemcnt = 0; itemcnt < customFieldArray.length; itemcnt++) {
                var fieldId = customFieldArray[itemcnt].id;
                var fieldName = customFieldArray[itemcnt].name;
                var isMandatory = customFieldArray[itemcnt].isessential;
                if (fieldId != undefined && Wtf.getCmp(fieldId) != undefined && customFieldArray[itemcnt].getXType() != 'fieldset') {
                    var cmbValue = Wtf.getCmp(fieldId).getValue();
                    /**
                     * Validate PIN Code field value 
                     */
                    if (fieldName == Wtf.CUSTOM_PIN_CODE) {
                        Wtf.getCmp(fieldId).isValid();
                        if (cmbValue == undefined || cmbValue == "") {
                            Wtf.getCmp(fieldId).markInvalid();
                            isValid = false;
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.custom.pincode.error.txt")], 2);
                        } else if (cmbValue.toString().length != 6) {
                            Wtf.getCmp(fieldId).markInvalid();
                            isValid = false;
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.custom.pincode.error.txt")], 2);
                        }
                    }
                    if (fieldName == Wtf.Custom_GSTIN) {
                        gstinValue= Wtf.getCmp(fieldId).getValue(); // Store GSTIN of entity in variable ERM-1108                      
                    }
                    
                }
            }            
        }
        /*  Code to check whether State Code of Entity's GSTIN number , is valid or not
         *  ERM-1108
         */
        if (customDimensionArray != null && customDimensionArray != undefined && customDimensionArray != "") {
             for (var dimcnt = 0; dimcnt < customDimensionArray.length; dimcnt++) {
                var fieldName=customDimensionArray[dimcnt].name; 
                var fieldId = customDimensionArray[dimcnt].id;
                if(fieldName=="Custom_State"){
                     var fieldStore=Wtf.getCmp(fieldId)!=undefined?Wtf.getCmp(fieldId).store:'';
                     var fieldValue=Wtf.getCmp(fieldId)!=undefined?Wtf.getCmp(fieldId).getValue():'';
                     // find itemdescription i.e. state code from State dropdown and compare it with GSTIN number's first 2 digits.
                     fieldStore.findBy( function(rec){
                        if(rec.data['id'] == fieldValue) {
                            if(rec.data['itemdescription']!=undefined && rec.data['itemdescription']!=''&&rec.data['itemdescription'].length<3){
                                var stateCode=(gstinValue!=undefined && gstinValue!='')?gstinValue.substring(0,2):'';
                                if(rec.data['itemdescription']!=stateCode){
                                    isValid = false;
                                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.GST.eway.validation.entityGSTIN")], 2); 
                                }
                            }
                        }
                    },this)   
        }
    }
 }
        return isValid;
    },
    fillIBGDetailsValues: function(){        
        if(this.ibgReceivingDetails != undefined || this.ibgReceivingDetails!=""){
            this.gridRec = new Wtf.data.Record.create([
            {
                name:'ibgId'
            },

            {
                name:'receivingBankCode'
            },

            {
                name:'receivingBankName'
            },

            {
                name:'receivingBranchCode'
            },

            {
                name:'receivingAccountNumber'
            },

            {
                name:'receivingAccountName'
            },
            ]);
    
            var ibgDetailslen = 0;
            if(this.ibgReceivingDetails && this.ibgReceivingDetails !=''){
                var parsed = JSON.parse(this.ibgReceivingDetails);
                this.ibgReceivingDetailsArr = new Array(parsed);
            }
            if(this.ibgReceivingDetailsArr!=undefined){
                if(this.ibgReceivingDetailsArr.length>0){
                    ibgDetailslen = this.ibgReceivingDetailsArr.length;
                }
                for(var i=0;i<ibgDetailslen;i++){
                    var ibgReceivingDetails=new this.gridRec({
                        ibgId: this.ibgReceivingDetailsArr[0].ibgId!=undefined?this.ibgReceivingDetailsArr[0].ibgId:"",
                        receivingBankCode:this.ibgReceivingDetailsArr[0].receivingBankCode!=undefined?this.ibgReceivingDetailsArr[0].receivingBankCode:"",
                        receivingBankName:this.ibgReceivingDetailsArr[0].receivingBankName!=undefined?this.ibgReceivingDetailsArr[0].receivingBankName:"",
                        receivingBranchCode: this.ibgReceivingDetailsArr[0].receivingBranchCode!=undefined?this.ibgReceivingDetailsArr[0].receivingBranchCode:"",
                        receivingAccountNumber:this.ibgReceivingDetailsArr[0].receivingAccountNumber!=undefined?this.ibgReceivingDetailsArr[0].receivingAccountNumber:"",
                        receivingAccountName:this.ibgReceivingDetailsArr[0].receivingAccountName!=undefined?this.ibgReceivingDetailsArr[0].receivingAccountName:""
                    });
                }   
            }
        }  
    
        this.ibgDetails = new Wtf.account.VendorIBGDetails({
            title:'IBG-Receiving Bank Details',
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            resizable:false,
            isEdit: true,
            layout:'border',
            ibgReceivingDetails:ibgReceivingDetails,
            modal:true,
            height:500,
            width:550
        });
        
        this.ibgDetails.on('datasaved',function(ibgForm, ibgFormJsonObj){
            this.ibgReceivingDetails = ibgFormJsonObj;
        },this);
        
        this.ibgDetails.show();        
    },

    saveMasterGroup: function(btn, txt, id){
        if(btn=="ok"&&txt.replace(/\s+/g, '')!=""){
            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+'CompanyManager.jsp',
                url:"ACCMaster/saveMasterGroup.do",
                params: {
                        mode:113,
                        name:txt,
                        id:id,
                        isIBGActivated:isIBGActivated
                   }
            },this,this.genSuccessResponse,this.genFailureResponse);
        }
    },
    handleResetClick: function (btn, event) {
        this.masterItemSearch.reset();
        
        if(this.masterSm.getSelected()) {   //if no group selected and only resetting goes in infinite loadmask
            this.MasterItemStore.clearFilter(false);
            this.MasterItemStore.reload();
        }
    },
    handleResetClickMasterGroup: function (btn, event) {
        this.masterSearch.reset();
        this.masterStore.clearFilter(false);
        this.masterStore.reload();                
        this.masterItemSearch.reset();//reset masterItemSearch, on mastergroup reset
    },
    ExportMasterConfig:function(ExportMasterConfigVar,exportDataFlag,type){
        var isShowCustColumn=(this.onlyMaster==undefined)?true:false;
        var isShowDimensiononly=(this.showonlyDimension && this.showonlyDimension!=undefined)?true:false;
        var isShowCustomFieldonly=(this.isShowCustomFieldonly && this.isShowCustomFieldonly!=undefined)?true:false;
        if(ExportMasterConfigVar=="customfields"){
                var align="none,none,none,none,none,none,none,none,none,none,none,none,none,none,none,none";
                var filename=isShowDimensiononly?"Dimension(s)":"Custom Field";       
                var exportUrl="ACCMaster/ExportCustomFilds.do";   
                var type=(type=="xls")?"xls":"csv";
                var get=1;
                var requesttype=0;//0- custom fields 1- custom field data
                var  header="fieldlable,maxlength,isessential,fieldtype,combodata,modulename,iseditable,sendnotification,notificationdays,isforproject,isfortask,iscustomfield,iscustomcolumn,relatedmoduleids,parentname,defaultval";
                var nondeleted="true";
                var title="Field Name,Max Length,Is Essential Field,Field Type,Combo data, Module Name, Is Editable,Send Notification, Notification Days,Field For Project, Field For Task,Custom Field-'Yes'/Dimension-'No',Custom Column,Related Module,Parent,Default Value"; 
                var width="75,75,75,75,75,75,75,75,75,75,75,75,75,75,75,75,75,75,75";
                var url = exportUrl+"?filename="+encodeURIComponent(filename)+"+&filetype="+type+"&nondeleted="+nondeleted+"&header="+header+"&title="+encodeURIComponent(title)+"&width="+width+"&get="+get+"&align="+align+"&requesttype="+requesttype+"&isShowCustColumn="+isShowCustColumn+"&isShowDimensiononly="+isShowDimensiononly+"&isShowCustomFieldonly="+isShowCustomFieldonly;
                Wtf.get('downloadframe').dom.src  = url;
        }else if(ExportMasterConfigVar=="customfielddata"){
                var FieldDataalign="none,none,none,none,none,none";
                var FieldDatafilename=isShowDimensiononly?"Dimension(s) Data":"Custom Field Data";       
                var FieldDataexportUrl="ACCMaster/ExportCustomFilds.do";   
                var FieldDatatype=(type=="xls")?"xls":"csv";
                var requesttype=1;
                var FieldDataget=1;
                var exportDataFlag = exportDataFlag;
                var FieldDataheader="data,fieldname,parent,extparentdimen,extparent,ismastergroup";
                var FieldDatanondeleted="true";
                var FieldDatatitle="Master items,Custom fields/Dimension name,Item parent,Parent dimension,Parent dimension value,Is master group item";
                var FieldDatawidth="75,75,75,75,75,75";
                var FieldDataurl = FieldDataexportUrl+"?filename="+encodeURIComponent(FieldDatafilename)+"+&filetype="+FieldDatatype+"&nondeleted="+FieldDatanondeleted+"&header="+FieldDataheader+"&title="+encodeURIComponent(FieldDatatitle)+"&width="+FieldDatawidth+"&get="+FieldDataget+"&align="+FieldDataalign+"&requesttype="+requesttype+"&exportDataFlag="+exportDataFlag+"&isShowCustColumn="+isShowCustColumn+"&isShowDimensiononly="+isShowDimensiononly+"&isShowCustomFieldonly="+isShowCustomFieldonly;
                Wtf.get('downloadframe').dom.src  = FieldDataurl;
        } else if( ExportMasterConfigVar == "defaultfields" ) {
                var align="none,none,none,none";
                var filename="Defalut Field";       
                var exportUrl="ACCMaster/ExportCustomFilds.do";   
                var type=(type=="xls")?"xls":"csv";
                var get=1;
                var requesttype=2;//0- custom fields 1- custom field data 2- Default Fields
                var  header="fieldlable,isessential,combodata,modulename";
                var nondeleted="true";
                var title="Field Name,Is Essential Field,Combo data, Module Name"; 
                var width="75,75,75,75";
                var url = exportUrl+"?filename="+encodeURIComponent(filename)+"+&filetype="+type+"&nondeleted="+nondeleted+"&header="+header+"&title="+encodeURIComponent(title)+"&width="+width+"&get="+get+"&align="+align+"&requesttype="+requesttype;
                Wtf.get('downloadframe').dom.src  = url;
        }
  },
   confirmBeforeSave: function (btn, txt, isEdit, id, masterid, outer,parentId,parentValueid,moduleIds,emailid,userId,custVendCategoryTypeId,designation,accId,custFieldArr) {
        if (Wtf.account.companyAccountPref.propagateToChildCompanies) {

            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.confirm"),
                msg: WtfGlobal.getLocaleText({key: "acc.savecustomer.propagate.confirmmessage", params: [" MasterItem "]}),
                buttons: Wtf.MessageBox.YESNO,
                icon: Wtf.MessageBox.QUESTION,
                width: 300,
                scope: {
                    scopeObject: this
                },
                fn: function (btn1) {
                    if (btn1 == "yes") {
                        this.scopeObject.ispropagatetochildcompanyflag = true;
                    }
                    this.scopeObject.saveMasterGroupItem(btn, txt, isEdit, id, masterid, outer,parentId,parentValueid,moduleIds,emailid,userId,custVendCategoryTypeId,designation,accId);
                }
            }, this);

        } else {
            this.saveMasterGroupItem(btn, txt, isEdit, id, masterid, outer,parentId,parentValueid,moduleIds,emailid,userId,custVendCategoryTypeId,designation,accId,custFieldArr);
        }
    },   
    saveMasterGroupItem: function(btn, txt, isEdit, id, masterid, outer,parentId,parentValueid,moduleIds,emailid,userId,custVendCategoryTypeId,designation,accId,custFieldArr){
        var callUrl = (this.isTransationFormEntry) ? "ACCMaster/saveMasterItemForCustom.do" : "ACCMaster/saveMasterItem.do";
        if(this.masterGrid) {
            var rec = this.masterGrid.getSelectionModel().getSelected();
            if(rec)
                {
                    this.groupname=rec.data.name
                }               
                if (rec && rec.data['modulename'] != undefined && rec.data['modulename'] != "" ) {
                var recData = rec.data;
                callUrl = "ACCMaster/saveMasterItemForCustom.do"
                this.groupname=recData.name,
                this.iscustom=recData.iscustomfield,
                this.customcolumn=recData.customcolumn  
            }
          
        }
      if(masterid=='16'){
           callUrl = "ACCMaster/saveMasterPriceDependentItem.do"
      }
        var isIBGActivated = false;
        if(masterid=='17' && this.ibgReceivingDetailsArr != undefined && this.ibgReceivingDetailsArr.length>0){
            if(!this.isActivateIBG.collapsed){
                this.gridRec = new Wtf.data.Record.create([
                {
                    name:'ibgId'
                },{
                    name:'receivingBankDetailId'
                },{
                    name:'receivingBankCode'
                },{
                    name:'receivingBankName'
                },{
                    name:'receivingBranchCode'
                },{
                    name:'receivingAccountNumber'
                }, {
                    name:'receivingAccountName'
                }]);

                var ibgDetailslen = 0;
                if(this.ibgReceivingDetails && this.ibgReceivingDetails !=''){
                    var parsed = JSON.parse(this.ibgReceivingDetails);
                    this.ibgReceivingDetailsArr=new Array(parsed);
                }
                if(this.ibgReceivingDetailsArr!=undefined){
                    if(this.ibgReceivingDetailsArr.length>0){
                        ibgDetailslen = this.ibgReceivingDetailsArr.length;
                    }
                    for(var i=0;i<ibgDetailslen;i++){
                        this.ibgReceivingDetails=new this.gridRec({
                            ibgId: this.ibgReceivingDetailsArr[0].ibgId!=undefined?this.ibgReceivingDetailsArr[0].ibgId:"",
                            receivingBankDetailId: this.ibgReceivingDetailsArr[0].ibgId!=undefined?this.ibgReceivingDetailsArr[0].ibgId:"",
                            receivingBankCode:this.ibgReceivingDetailsArr[0].receivingBankCode!=undefined?this.ibgReceivingDetailsArr[0].receivingBankCode:"",
                            receivingBankName:this.ibgReceivingDetailsArr[0].receivingBankName!=undefined?this.ibgReceivingDetailsArr[0].receivingBankName:"",
                            receivingBranchCode: this.ibgReceivingDetailsArr[0].receivingBranchCode!=undefined?this.ibgReceivingDetailsArr[0].receivingBranchCode:"",
                            receivingAccountNumber:this.ibgReceivingDetailsArr[0].receivingAccountNumber!=undefined?this.ibgReceivingDetailsArr[0].receivingAccountNumber:"",
                            receivingAccountName:this.ibgReceivingDetailsArr[0].receivingAccountName!=undefined?this.ibgReceivingDetailsArr[0].receivingAccountName:""
                        });
                    }   
                }
            }
        }
        if(masterid=='17' && this.ibgReceivingDetails != undefined && this.ibgReceivingDetails != ""){
            if(this.ibgReceivingDetails != undefined && this.ibgReceivingDetails != ""){
                if(!this.isActivateIBG.collapsed){
                    isIBGActivated = true;
                        rec.ibgReceivingDetails = this.ibgReceivingDetails;
                    }
            }else{
                isIBGActivated = false;
                rec.ibgReceivingDetails = "";
            }
        }
        var salespersoncode = "";
        var salesPersonContactNumber = "";
        var salesPersonAddress = "";
        var salesPersonDesignation = "";
        var itemdescription = "";
        var driverID = "";
        var variancePercentage = 0;
        var industryCodeId = "";
        var sectionCode="";
        var typeOfDeducteeType="";
        var vatcommoditycode='';
        var vatscheduleno='';
        var vatscheduleserialno='';
        var vatnotes='';
        var landingCostCategory='';
        var landingCostAllocationType='';
        var BICCode='';
        var bankCode='';
        var branchCode='';
        var isAppendBranchCode='';
        
        if(this.comboValueDescription && this.comboValueDescription.getValue()){//  
            itemdescription = this.comboValueDescription.getValue().trim();
        }
        if ((masterid == '15' || masterid == '20' || masterid == '40' || masterid == '65') && this.salesPersonCode && this.salesPersonCode.getValue()) {// for sales person and agent, Tax Type
            salespersoncode = this.salesPersonCode.getValue();
            if (salespersoncode.replace(/\s+/g, '') == "") {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleaseenternew") + this.masterStore.getAt(this.masterStore.find('id', masterid)).data['name']+'.'], 2);
                this.salesPersonCode.setValue("");
                return;
            }
        }
        if((masterid=='15' || masterid=='20' || masterid=='40') && this.salesPersonContactNumner && this.salesPersonContactNumner.getValue()){// for sales person and agent
            salesPersonContactNumber = this.salesPersonContactNumner.getValue();
        }
        if((masterid=='15' || masterid=='20' || masterid=='40') && this.salesPersonAddress && this.salesPersonAddress.getValue()){// for sales person and agent
            salesPersonAddress = this.salesPersonAddress.getValue();
        }
        if((masterid=='15' || masterid=='20' || masterid=='40') && this.salesPersonDesignation && this.salesPersonDesignation.getValue()){// for sales person and agent
           salesPersonDesignation =this.salesPersonDesignation.getValue();
        }
        if(masterid=='25' && this.driverCombo && this.driverCombo.getValue()){ // for mapping driver in Vehicle Number
           driverID = this.driverCombo.getValue();
        }
        if(masterid=='19' && this.isDefaultToPOS ){ // for mapping driver in Vehicle Number
           var isDefaultToPOS = this.isDefaultToPOS.getValue();
        }
        if (masterid=='19' && this.variancePercentage && this.variancePercentage.getValue()) { // Variance Percentage for Product Category
            variancePercentage = this.variancePercentage.getValue();
        }
        if (masterid=='19' && this.industryCodeCmb && this.industryCodeCmb.getValue()) { // Variance Percentage for Product Category
            industryCodeId = this.industryCodeCmb.getValue();
        }
        if(masterid=='33'){
              txt= this.natureOfPaymentDesc.getValue().trim();
              sectionCode=this.natureOfPaymentesection.getValue();
        }
        if(masterid=='60'){//ERP-20637
              callUrl = "ACCMaster/saveLandingCostOfCategory.do"
              txt= this.landingCostCategoryCombo.getValue()+"";
            landingCostCategory= this.landingCostCategoryCombo.getValue();
            landingCostAllocationType=this.landingCostAllocationTypeCombo.getValue();
        }
        if(masterid == Wtf.MasterConfig_BankName && Wtf.account.companyAccountPref.activateIBGCollection){
              BICCode = this.BICCode.getValue();
              bankCode = this.bankCode.getValue();
              branchCode = this.branchCode.getValue();
              isAppendBranchCode = this.isAppendBranchCode.getValue();
        
        }
        if(masterid=='34'){
              typeOfDeducteeType=this.typeOfdeducteeTypeCombo.getValue();
        }
        if(masterid=='42'){
              vatcommoditycode=this.vatcommoditycode.getValue();
              vatscheduleno=this.vatscheduleno.getValue();
              vatscheduleserialno=this.vatscheduleserialno.getValue();
              vatnotes=this.vatnotes.getValue();
        }
        

       if(btn=="ok"){
           if(txt.replace(/\s+/g, '')!=""){
                Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                     url:callUrl,
                    params: {
                        mode:114,
                        name:txt,
                        id:id,
                        parentid:parentId,
                        groupid:masterid,
		        typeid:this.masterTypeCombo.getValue(),
			parentValueid:parentValueid,
			itemdescription:itemdescription,
                        moduleIds:moduleIds,
                        isEdit:isEdit,
                        isIBGActivated:isIBGActivated,
                        salespersoncode:salespersoncode,
                        salesPersonContactNumber:salesPersonContactNumber,
                        salesPersonAddress:salesPersonAddress,
                        groupname:this.groupname,
                        iscustom:this.iscustom,
                        customcolumn:this.customcolumn,
                        emailid:emailid,
                        userid:userId,
                        accid:accId,
                        custVendCategoryTypeId : custVendCategoryTypeId,
                        salesPersonDesignation : salesPersonDesignation,
                        driverID: driverID,
                        isDefaultToPOS:isDefaultToPOS,
                        variancePercentage: variancePercentage,
                        industryCodeId: industryCodeId,
                        sectionCode:sectionCode,
                        typeOfDeducteeType:typeOfDeducteeType,
                        vatcommoditycode:vatcommoditycode,
                        vatscheduleno:vatscheduleno,
                        vatscheduleserialno:vatscheduleserialno,
                        vatnotes:vatnotes,
                        ispropagatetochildcompanyflag:this.ispropagatetochildcompanyflag,
                        landingcostcategory:landingCostCategory,
                        landingcostallocationtype:landingCostAllocationType,
                        BICCode : BICCode,
                        bankCode : bankCode,
                        branchCode : branchCode,
                        sequenceformat : this.sequenceFormatCombobox.getValue(),
                        isAppendBranchCode:isAppendBranchCode,
                        customfield: JSON.stringify(custFieldArr)
                    }
                },this,this.genSuccessResponse.createDelegate(this,[outer, masterid],true),this.genFailureResponse);
           }else{
               Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.alert"),  
                    msg: WtfGlobal.getLocaleText("acc.field.Pleaseenternew")+this.masterStore.getAt(this.masterStore.find('id',masterid)).data['name']+'.',
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.WARNING,
                    width: 300,
                    scope: this
//                    fn: function(){
//                        if(btn=="ok"){
//                            this.AddMasterItem(isEdit,masterid,outer);
//                        }
                   // }
                 });
                if (this.loadmask) {
                    this.loadmask.hide();
                }
     
           }
           this.ispropagatetochildcompanyflag=false;
        } else if(outer){
           this.destroy();
       }
   },
    saveMasterDependentItem: function(btn, txt, price, masterid){
        var callUrl = "ACCMaster/saveMasterItemPrice.do";
      var itemId=""
      if(this.MasterItemSm.hasSelection()){
               var rec=this.MasterItemSm.getSelected();
                itemId=rec.data['id']
            } 
       if(btn=="ok"){
           if(txt.replace(/\s+/g, '')!=""){
                Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                     url:callUrl,
                    params: {
                        name:txt,
                        itemid:itemId,
                        itemprice:price
                    }
                },this,this.genSuccessResponsec.reateDelegate(this,[false, masterid],true),this.genFailureResponse);
           }
        } 
   },
   
    showsyncAllFromLMSWindow: function (winid) {
        winid = (winid == null ? "saveWIPAndCPAccountSettingswin" : winid);
        var p = Wtf.getCmp(winid);
        if (!p) {
            new Wtf.account.SyncAllFromLMSWindow({
                title: WtfGlobal.getLocaleText("acc.field.SyncAllFromLMSWin"),
                resizable: false,
                width: 350,
                height:205,
                modal: true,
                layout: 'fit',
                scope: this
            }).show();
        }
     
    },
    
   syncPMDataConfirm : function(syncType){
        var message = "";
        if(!Wtf.account.isCPAndWIPAccountsSET && (BCHLCompanyId.indexOf(companyid) != -1)){
            message = WtfGlobal.getLocaleText("acc.field.YouhavenotsetinformationaboutCP/WIPAccountsWouldlikesetfirst");
           
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.field.SyncPMData"),  //'Master Configuration',
                msg: message,
                buttons: Wtf.MessageBox.YESNO,
                icon: Wtf.MessageBox.INFO,
                width: 400,
                scope: this,
                fn: function(btn){
                    if(btn=="yes"){
                        saveWIPAndCPAccountSettings();
                    }else{
                        Wtf.MessageBox.show({
                            title: WtfGlobal.getLocaleText("acc.field.SyncPMData"),  //'Master Configuration',
                            msg: WtfGlobal.getLocaleText("acc.field.AreyousureyouwanttosyncProjectsfromProjectManagement"),
                            buttons: Wtf.MessageBox.YESNO,
                            icon: Wtf.MessageBox.INFO,
                            width: 300,
                            scope: this,
                            fn: function(btn){
                                if(btn=="yes"){
                                    this.syncPMDataFn(syncType);
                                }
                            }
                        });
                    }
                }
            });
           
        }else{
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.field.SyncPMData"),  //'Master Configuration',
                msg:this.isProject?WtfGlobal.getLocaleText("acc.field.AreyousureyouwanttosyncProjectsfromProjectManagement"):WtfGlobal.getLocaleText("acc.field.AreyousureyouwanttosyncPaymentMilestonefromProjectManagement"),
                buttons: Wtf.MessageBox.YESNO,
                icon: Wtf.MessageBox.INFO,
                width: 300,
                scope: this,
                fn: function(btn){
                    if(btn=="yes"){
                        this.syncPMDataFn(syncType);
                    }
                }
            });
        }
    },
    
    synceClaimDataConfirm: function () {
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.field.SynceClaimData"), //'Master Configuration',
            msg: WtfGlobal.getLocaleText("acc.field.AreyousureyouwanttosyncCostCentersfromeClaim"),
            buttons: Wtf.MessageBox.YESNO,
            icon: Wtf.MessageBox.INFO,
            width: 300,
            scope: this,
            fn: function (btn) {
                if (btn == "yes") {
                    this.synceClaimDataFn();
                }
            }
        });
    },
//      syncProcessSkillConfirm: function () {
//        Wtf.MessageBox.show({
//            title: WtfGlobal.getLocaleText("acc.field.SyncProcessandskills"), //'Master Configuration',
//            msg: WtfGlobal.getLocaleText("acc.field.AreyousureyouwanttosyncProcessSkill"),
//            buttons: Wtf.MessageBox.YESNO,
//            icon: Wtf.MessageBox.INFO,
//            width: 300,
//            scope: this,
//            fn: function (btn) {
//                if (btn == "yes") {
//                    this.synceDataToPMFn();
//                }
//            }
//        });
//    },  
  syncPMDataFn : function(syncType){
     this.loadMask1 = new Wtf.LoadMask(this.id, {msg: WtfGlobal.getLocaleText("acc.msgbox.50"), msgCls: "x-mask-loading pmsync-acc-customer-form-mask"});
     this.loadMask1.show();
     WtfGlobal.setAjaxTimeOut();
     Wtf.Ajax.requestEx({
//                    url: Wtf.req.account+'CompanyManager.jsp',
                     url:"ACCAccountCMN/saveProjectMasterItemForCustom.do",
                    params: {
                        mode:114,
                        isProject:this.isProject,
                        isPartial:syncType
                    }
                },this,function(response){
                    WtfGlobal.resetAjaxTimeOut();
                    this.loadMask1.hide();
                    Wtf.MessageBox.show({
                        title: WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),
                        msg: response.msg,
                        width: 450,
                        scope: {
                            scopeObj: this
                        },
                        fn: function(btn, text, option) {
                            var recData = this.scopeObj.masterGrid.getSelectionModel().getSelected().data;
                            this.scopeObj.MasterItemStore.load({
                                params: {
                                    groupid: recData.id,
                                    moduleIds: recData.moduleIds
                                }
                            });
                        },
                        buttons: Wtf.MessageBox.OK,
                        animEl: 'mb9',
                        icon: Wtf.MessageBox.INFO
                    });

                },function(){
                    this.loadMask1.hide();
                    var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
                });  
   },
   
   synceClaimDataFn: function () {
        this.loadMask1 = new Wtf.LoadMask(document.body, {msg: WtfGlobal.getLocaleText("acc.msgbox.49"), msgCls: "x-mask-loading"});
        this.loadMask1.show();
        
        Wtf.Ajax.requestEx({
            url: "ACCAccountCMN/saveeClaimCostCentersAsDimention.do",
            params: {
                mode: 114,
                iseClaim: this.iseClaim
            }
        }, this, function (response) {
            
            this.loadMask1.hide();
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),
                msg: response.msg,
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.INFO,
                width: 300,
                scope: this,
                fn: function (btn) {
                    if (btn == "ok" && this.masterGrid.getSelectionModel().getSelected()) {
                        var recData = this.masterGrid.getSelectionModel().getSelected().data;
                        this.MasterItemStore.load({
                            params: {
                                groupid: recData.id,
                                moduleIds: recData.moduleIds
                            }
                        });
                    }
                }
            });
        }, function () {
            this.loadMask1.hide();
            var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        });
   },
    synceDataToPMFn: function() {
        Wtf.Ajax.requestEx({
            url: "ACCMaster/sendProcessAndSkillToPM.do",
            params: {
                appid: Wtf.appID.eUnivercity //LMS id is 5
            }
        }, this, function(response) {
            if (response.success) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"), response.msg], response.success * 2 + 1);
            }
        }, function() {
            var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        });
    },
   syncCustomFieldDataFromOtherProjects : function(){
        this.loadMask1 = new Wtf.LoadMask(this.id, {
            msg: WtfGlobal.getLocaleText("acc.msgbox.49"), 
            msgCls: "x-mask-loading acc-customer-form-mask"
        });
        this.loadMask1.show();
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url:"ACCAccountCMN/syncCustomFieldDataFromOtherProjects.do",
            params: {
                mode:114,
                mapWithFieldType:this.mapWithFieldType
            }
        },this,function(response){
            WtfGlobal.resetAjaxTimeOut();
            this.loadMask1.hide();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),response.msg],response.success*2+1);
            var recData = this.masterGrid.getSelectionModel().getSelected().data;
            this.MasterItemStore.load({
                params:{
                    groupid:recData.id,
                    moduleIds:recData.moduleIds
                }
            });
        },function(){
            this.loadMask1.hide();
            var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        });  
    },
   syncAllDataFromOtherProjects : function(){
        this.loadMask1 = new Wtf.LoadMask(this.id, {
            msg: WtfGlobal.getLocaleText("acc.msgbox.49"), 
            msgCls: "x-mask-loading acc-customer-form-mask"
        });
        this.loadMask1.show();
        var mapWithFieldTypeArr="2,3,4,5";
        WtfGlobal.setAjaxTimeOut();
        Wtf.Ajax.requestEx({
            url:"CommonFunctions/SyncAllFromLMS.do",
            params: {
                mode:114,
                mapWithFieldType:mapWithFieldTypeArr,
                requestFlag:this.requestFlag,
                deleted:this.deleted,
                nondeleted: this.nondeleted
            }
        },this,function(response){
            WtfGlobal.resetAjaxTimeOut();
            this.loadMask1.hide();
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),response.msg],response.success*2+1);
        },function(){
            this.loadMask1.hide();
            var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        });  
    },
     syncProductsCategoryToPOS:function(){
        var groupId = this.masterGrid.getSelectionModel().getSelected().json.id;
        this.arrRec = this.MasterItemStore.getRange(0,this.MasterItemStore.getCount()-1);
        WtfGlobal.highLightRowColor(this.MasterItemGrid,this.arrRec,true,0,2);
        if(this.arrRec.length==0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.rem.POS.247")],2);
            this.MasterItemStore.clearFilter();
            return;
        }
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"),
            msg:groupId==57? WtfGlobal.getLocaleText("acc.rem.253"):WtfGlobal.getLocaleText("acc.rem.POS.248"),  //"Shown product categories will be syncronized with other application. Are you sure you want to synchronize the product categories?",
            width: 560,
            buttons: Wtf.MessageBox.OKCANCEL,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope:this,
            fn:function(btn){
                if(btn!="ok"){
                    this.MasterItemStore.clearFilter();
                    var num= (this.MasterItemStore.indexOf(this.arrRec[0]))%2;
                    WtfGlobal.highLightRowColor(this.MasterItemGrid,this.arrRec,false,num,2);
                    return;
                }
                else {
                    WtfGlobal.setAjaxTimeOut();
                    Wtf.Ajax.requestEx({
                        url:"ACCMaster/sendAccMasterItemsToPOS.do",
                        params: {
                            groupid:groupId,	
                            mode:112
                        }
                    },this,this.genSyncSuccessResponse,this.genSyncFailureResponse);
                }
            }
        });
},
genSyncSuccessResponse:function(response){
    WtfGlobal.resetAjaxTimeOut();
    this.arrRec = this.MasterItemStore.getRange(0,this.MasterItemStore.getCount()-1);
    WtfGlobal.highLightRowColor(this.MasterItemGrid,this.arrRec,false,2,true);
        
    this.MasterItemStore.clearFilter();
    if(!response.companyexist)
        this.callSubscriptionWin()
    else if(response.success){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.productList.gridProduct"),response.msg],response.success*2+1);

  
    }

},
genSyncFailureResponse:function(response){
    WtfGlobal.resetAjaxTimeOut();
    this.arrRec = this.MasterItemStore.getRange(0,this.MasterItemStore.getCount()-1);
    WtfGlobal.highLightRowColor(this.MasterItemGrid,this.arrRec,false,2,true);
    this.MasterItemStore.clearFilter();
    var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
    if(response.msg)msg=response.msg;
    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
},
   getSelModel: function(){
                if(this.MasterItemGrid.getSelectionModel().hasSelection())
                {
                    this.selectedModel = this.MasterItemGrid.getSelectionModel();
                }
                else
                {
                    this.selectedModel = this.grid.getSelectionModel();
                }
                return this.selectedModel;
   },
    confirmBeforeDelete: function () {
        if (Wtf.account.companyAccountPref.propagateToChildCompanies) {

            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.confirm"),
                msg: WtfGlobal.getLocaleText("acc.deletemaster.propagate.confirmmessage"),
                buttons: Wtf.MessageBox.YESNO,
                icon: Wtf.MessageBox.QUESTION,
                width: 300,
                scope: {
                    scopeObject: this
                },
                fn: function (btn1) {
                    if (btn1 == "yes") {
                        this.scopeObject.ispropagatetochildcompanyflag = true;
                    }
                    this.scopeObject.DeleteMasterItem();
                }
            }, this);

        } else {
            this.DeleteMasterItem();
        }
    },   
   DeleteMasterItem:function (){
       var selectedGridSelectionModel = this.getSelModel();
       if(selectedGridSelectionModel.hasSelection()){
           var arrID=[];
           var arritem=[];
           var rec = selectedGridSelectionModel.getSelections();
           for(var i=0;i<selectedGridSelectionModel.getCount();i++){
                arrID.push(rec[i].data['id']);
                arritem.push(rec[i].data['name']);
           }
       }
       
        var callUrl = "";
        var rec = this.masterGrid.getSelectionModel().getSelected();
        if (rec.data['modulename'] != "" && rec.data['modulename'] != undefined)
            callUrl = "ACCMaster/deleteMasterCustomItem.do"
        else
            callUrl = "ACCMaster/deleteMasterItem.do"
        var id= this.masterGrid.getSelectionModel().getSelected().data["id"];
        var moduleIds =this.masterStore.getAt(this.masterStore.find('id',id)).data['moduleIds'];
         var masterid=rec.data['id'];
         var iscustom=rec.data['iscustomfield'];
         var customcolumn=rec.data['customcolumn'];
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
                                ids:arrID.toString(),
                                groupid:masterid,
                                moduleIds:moduleIds,
                                groupname:rec.data['name'],
                                name:arritem.toString(),
                                iscustom:iscustom,
                                customcolumn:customcolumn,
                                ispropagatetochildcompanyflag:this.ispropagatetochildcompanyflag
                        }
                    },this,this.genSuccessResponse,this.genFailureResponse);
                }
               // this.close();
                this.ispropagatetochildcompanyflag = false;
            }

        });
    },
    activateDeactivateSalesperson: function (activateDeactivateFlag) {
        var ActivateSalesperson=false;
        if(activateDeactivateFlag == "Activate"){
            ActivateSalesperson=true;
            
        }
       
       var selectedGridSelectionModel = this.getSelModel();
        if (selectedGridSelectionModel.hasSelection()) {
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.confirm"), //"Confirm",
                msg: WtfGlobal.getLocaleText({key: "acc.masterconfiguration.salespersonDeactivateConfirmMsg", params: [ActivateSalesperson ? "Activate" : "Deactivate"]}),
                width: 560,
                buttons: Wtf.MessageBox.OKCANCEL,
                animEl: 'upbtn',
                icon: Wtf.MessageBox.QUESTION,
                scope: this,
                fn: function (btn) {
                    if (btn != "ok") {
                        return;
                    }
                    var arrID = [];
                    var rec = selectedGridSelectionModel.getSelections();
                    for (var i = 0; i < selectedGridSelectionModel.getCount(); i++) {
                        arrID.push(rec[i].data['id']);
                    }

                    Wtf.Ajax.requestEx({
                        url: "ACCMaster/activateDeactivateSalesperson.do",
                        params: {
                            data: arrID,
                            activateDeactivateFlag: ActivateSalesperson, //Send this flag as true when you want to Activate selected  salesperson
                            groupid: 15
                        }
                    }, this, this.genSuccessResponse, this.genFailureResponse);
                }
            });

        }
    },
   setPriceFunction: function(masterid) {
        var record = this.MasterItemSm.getSelections();;
        this.showsetPriceWindow = new Wtf.SetPriceWindow({
            rec:record[0],
            scope:this
        })
        this.showsetPriceWindow.show();

    },
   setPriceFormulaFunction: function(masterid) {
        var record = this.MasterItemSm.getSelections();;
        this.showsetPriceWindow = new Wtf.SetPriceFormulaWindow({
            rec:record[0],
            scope:this
        })
        this.showsetPriceWindow.show();

    },
      createSalesCommisionSchema: function(masterid) {
        var record = this.MasterItemSm.getSelections();
        this.commissionWindow=new Wtf.account.commissionWindow ({
            title: WtfGlobal.getLocaleText("acc.commission.type"),  //"Receipt Type",
            rec:record[0],
            scope:this,
            closable: false,
            modal: true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            width: 380,
            autoScroll:true,
            height: 270,
            resizable: false,
            layout: 'border',
            buttonAlign: 'right',
            renderTo: document.body
        });
        this.commissionWindow.show();
//        this.showsalesCommisionSchemaWindow = new Wtf.salesCommisionSchemaWindow({
//            rec:record[0],
//            scope:this
//        })
//        this.showsalesCommisionSchemaWindow.show();

    },
    deleteDimension: function(masterid) {
        var isessential= this.masterGrid.getSelectionModel().getSelected().data["isessential"];
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.warning"), //"Warning",
            msg: WtfGlobal.getLocaleText("acc.masterConfig.msg1"), ///+"<div><b>"+WtfGlobal.getLocaleText("acc.masterConfig.msg1")+"</b></div>",
            width: 380,
            buttons: Wtf.MessageBox.YESNO,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope: this,
            fn: function(btn) {
                if (btn == "yes") {
                    if(isessential){
                        Wtf.MessageBox.show({
                            title: WtfGlobal.getLocaleText("acc.common.warning"), //"Warning",
                            msg: WtfGlobal.getLocaleText("acc.masterConfig.msg20"), ///+"<div><b>"+WtfGlobal.getLocaleText("acc.masterConfig.msg1")+"</b></div>",
                            width: 380,
                            id:this.id+"_btn",
                            buttons: Wtf.MessageBox.OKCANCEL,
                            animEl: 'upbtn',
                            icon: Wtf.MessageBox.INFO,
                            scope: this,
                            fn: function(button){
                                if (button == "ok") {
                                    this.proceedDelete(masterid);
                                }
                            }
                        });
                    } else {
                        this.proceedDelete(masterid);
                    }
                 
                }
                // this.close();
            }

        });
    },
    
    proceedDelete: function(masterid) {
        var callUrl = "ACCMaster/deleteDimension.do";
        var id= this.masterGrid.getSelectionModel().getSelected().data["id"]; 
        var name= this.masterGrid.getSelectionModel().getSelected().data["name"]; 
        var fieldtype=this.masterGrid.getSelectionModel().getSelected().data["fieldtype"]; 
        var moduleIds =this.masterStore.getAt(this.masterStore.find('id',id)).data['moduleIds'];
        var iscustom= this.masterGrid.getSelectionModel().getSelected().data["iscustomfield"];
        var customcolumn= this.masterGrid.getSelectionModel().getSelected().data["customcolumn"];
        
        if (this.synceClaimData) {   //if cost centers dimention deleted the sync button still remains active, so disabled it
            this.synceClaimData.disable();
        }
        Wtf.Ajax.requestEx({
//                        url:Wtf.req.account+'CompanyManager.jsp',
            url: callUrl,
            params: {
                mode: 116,
                groupid: masterid,
                moduleIds: moduleIds,
                fieldtype: fieldtype,
                groupname: name,
                iscustom: iscustom,
                customcolumn: customcolumn
            }
        }, this, this.genSuccessResponse, this.genFailureResponse);
    },

    deactivateDimensionField:function(){
        var rec =this.masterGrid.getSelectionModel().getSelected();
        var iscustomfield = rec.data["iscustomfield"];
        var masterid = rec.data["id"];
        var moduleNames = rec.data["allmodulenames"];
        var activeModuleNames = rec.data["activemodulenames"];
        var moduleID = rec.data["moduleIds"];
        var relatedModuleIds=rec.data["relatedModuleIds"];
        var name=rec.data["name"];
        var isDeactivate=true;
        if(iscustomfield)
            addCustomColumn(true,false,masterid,this.masterStore,moduleNames,moduleID,relatedModuleIds,name,isDeactivate,activeModuleNames);// is Custom column flag             
        else
            addCustomColumn(false,false,masterid,this.masterStore,moduleNames,moduleID,relatedModuleIds,name,isDeactivate,activeModuleNames);// is Custom column flag
    },
    
    //method to save activate and deactivate dimension values
    acivateDectivateDimensionFieldvalues:function(activateDeactivate){
      var data = [];
      var arr = [];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.grid.getSelectionModel().clearSelections();
      var activateDeactivateDimFlag = false;
        
        if (activateDeactivate == "activate") {
            activateDeactivateDimFlag = true;        //Send this flag as true whenever you want to activate or deactivate master items.  
        }
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.confirm"), //"Confirm",
            msg: activateDeactivateDimFlag ?WtfGlobal.getLocaleText("acc.masterconfiguration.Areyousureyouwantactivatethemasteritem(s)")+" ?":WtfGlobal.getLocaleText("acc.masterconfiguration.Areyousureyouwantdeactivatethemasteritem(s)")+" ?",
            width: 400,
            buttons: Wtf.MessageBox.OKCANCEL,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope: this,
            fn: function(btn) {
                if (btn != "ok") {
                    return;
                }
                for (var i = 0; i < this.recArr.length; i++) {
                    arr.push(this.grid.getStore().indexOf(this.recArr[i]));
                }
                data = WtfGlobal.getJSONArray(this.grid, true, arr);

                Wtf.Ajax.requestEx({
                    url:'ACCMaster/saveActivateDeactivateDimensionFields.do',
                    params: {
                        data: data,
                        activateDeactivateDimFlag:activateDeactivateDimFlag
                    }
                }, this, this.genActivateSuccessResponse, this.genActivateFailureResponse);
            }
        });
    },
    genActivateSuccessResponse:function(response){
        if(response.success){
            var recData = this.masterGrid.getSelectionModel().getSelected().data;
            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"), //'Exchange Rate',
                msg:response.msg ,
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.INFO,
                scope: this,
                fn: function(btn){
                    if(btn=="ok"){
                        if(recData!=undefined && recData!=null){
                            this.MasterItemStore.load({
                                params:{
                                    mode:112,
                                    groupid:recData.id,
                                    moduleIds:recData.moduleIds,
                                    fieldlabel:recData.name,
                                    start:0,
                                    limit:this.pP.combo != undefined ? this.pP.combo.value:30
                                }
                            });
                        }
                    }
                }
            });
        }
    },
    genActivateFailureResponse:function(response){
       var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)
            msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2); 
    },
    mapSalesCommissionSchemaToMasterItem: function () {
        this.recArr = this.grid.getSelectionModel().getSelections();
        var recLength = this.recArr.length;
        if (recLength === 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.salesCommission.mapSalesCommission.alert")], 2);
            return;
        }else {
            var masterItems = "";
            var mappedCommissionSchema = "";
            for (var i = 0; i < recLength; i++) {
                masterItems += this.recArr[i].data.id + ",";
                mappedCommissionSchema = this.recArr[i].data.salesCommissionSchema;
            }
            var index = masterItems.lastIndexOf(",");
            masterItems.substring(1, index);

            this.masterItemsCombo = new Wtf.common.Select(Wtf.apply({
                multiSelect: true,
                fieldLabel: WtfGlobal.getLocaleText("acc.masterConfiguration.masterItems"),
                forceSelection: true
            }, {
                triggerAction: 'all',
                store: this.MasterItemStore,
                mode: 'local',
                valueField: 'id',
                displayField: 'name',
                typeAhead: true,
                name: 'masterItem',
                allowBlank: false,
                width: 200
            }));
            this.masterItemsCombo.setValue(masterItems);

            this.salesCommissionSchemaRec = Wtf.data.Record.create([
                {name: 'id'},
                {name: 'name', mapping: 'schemaMaster'}
            ]);
            this.salesCommisionSchemaStore = new Wtf.data.Store({
                url: "AccSalesCommission/getSalesCommissionSchemaMasters.do",
                reader: new Wtf.data.KwlJsonReader({
                    root: 'data'
                }, this.salesCommissionSchemaRec)
            });
            this.salesCommisionSchemaStore.load();
            this.salesCommisionSchemaCombo = new Wtf.form.ComboBox({
                fieldLabel: WtfGlobal.getLocaleText("acc.masterConfiguration.saleCommissionSchema.select"),
                triggerAction: 'all',
                store: this.salesCommisionSchemaStore,
                mode: 'local',
                valueField: 'id',
                displayField: 'name',
                name: 'salesCommissionSchema',
                allowBlank: false,
                labelWidth: 200,
                width: 200
            });
            this.salesCommisionSchemaStore.on('load', function () {
                var rec = WtfGlobal.searchRecord(this.salesCommisionSchemaCombo.store, mappedCommissionSchema, 'name');
                if (mappedCommissionSchema != undefined && mappedCommissionSchema != "" && rec != undefined) {
                    this.salesCommisionSchemaCombo.setValue(rec.data.id);
                }
            }, this);

            this.mapSalesCommissionForm = new Wtf.form.FormPanel({
                waitMsgTarget: true,
                border: false,
                region: 'center',
                layout: 'form',
                bodyStyle: "background: transparent;",
                style: "background: transparent;padding:20px;",
                labelWidth: '200',
                items: [this.masterItemsCombo, this.salesCommisionSchemaCombo]
            });
            this.mapSalesCommissionWindow = new Wtf.Window({
                modal: true,
                title: "Sales Commission Schema Mapping",
                iconCls: getButtonIconCls(Wtf.etype.deskera),
                bodyStyle: 'padding:5px;',
                buttonAlign: 'right',
                draggable: false,
                width: 425,
                scope: this,
                items: [{
                        region: 'center',
                        border: false,
                        bodyStyle: 'background:#f1f1f1;font-size:10px;',
                        autoScroll: true,
                        items: [this.mapSalesCommissionForm]
                    }],
                buttons: [{
                        text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                        scope: this,
                        handler: function () {
                            if (this.mapSalesCommissionForm.form.isValid()) {
                                Wtf.Ajax.requestEx({
                                    url: "AccSalesCommission/saveSalesCommissionMapping.do",
                                    params: {
                                        masterItem: this.masterItemsCombo.getValue(),
                                        salesCommisionSchema: this.salesCommisionSchemaCombo.getValue()
                                    }
                                }, this, this.genMapSchemaSuccessResponse, this.genMapSchemaFailureResponse);
                            } else {
                                return;
                            }
                        }
                    }, {
                        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                        scope: this,
                        handler: function () {
                            this.mapSalesCommissionWindow.close();
                        }
                    }]
            });
            this.mapSalesCommissionWindow.show();
        }
    },
    /**
     * Function to Map Master Item to Users Group if Users Visibility is ON
     */
    mapUsersGroupToMasterItem: function() {
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.masterRec = this.masterGrid.getSelectionModel().getSelected();
        var fieldname = "";
        if (this.masterRec != undefined && this.masterRec.data != undefined) {
            fieldname = this.masterRec.data.name;
            var index = fieldname.lastIndexOf("*");
            if(index!==-1){
                fieldname=fieldname.substr(0,index);
            }
        }
        var recLength = this.recArr.length;
        if (recLength === 0) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.salesCommission.mapSalesCommission.alert")], 2);
            return;
        } else {
            var masterItemsid = "";
            var masterItemsval = "";
            for (var i = 0; i < recLength; i++) {
                masterItemsid += this.recArr[i].data.id + ",";
                masterItemsval += this.recArr[i].data.name + ",";
            }
            var index = masterItemsid.lastIndexOf(",")
            masterItemsid.substring(1, index);
            var index = masterItemsval.lastIndexOf(",")
            masterItemsval.substring(1, index);

        this.masterItems = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.masterConfiguration.masterItems"),
            name: 'masterItem',
            id: "masterItem" + this.heplmodeid + this.id,
            width : 200,
            maxLength: 255,
            scope: this
        });
        this.masterItems.setValue(masterItemsval);
            this.userGrpRec = Wtf.data.Record.create([
                {name: 'id', mapping: 'groupid'},
                {name: 'name'}
            ]);
            this.userGrpStore = new Wtf.data.Store({
                url: "ACCMaster/getUsersGroup.do",
                reader: new Wtf.data.KwlJsonReader({
                    root: 'data'
                }, this.userGrpRec)
            });
            this.userGrpStore.load();
            this.userGrpCombo = new Wtf.form.ComboBox({
                fieldLabel: WtfGlobal.getLocaleText("acc.masterConfiguration.usergrp.select"),
                triggerAction: 'all',
                store: this.userGrpStore,
                mode: 'local',
                valueField: 'id',
                displayField: 'name',
                name: 'userGrpCombo',
                allowBlank: false,
                labelWidth: 200,
                width: 200
            });
       this.userGrpStore.on('load', function() {
                var rec = this.grid.getSelectionModel().getSelected();
                if (rec != null && rec != "" && rec != undefined) {
                    this.userGrpCombo.setValue(rec.data.usergroupid);
                }
        }, this);
            this.mapUsersGroupForm = new Wtf.form.FormPanel({
                waitMsgTarget: true,
                border: false,
                region: 'center',
                layout: 'form',
                bodyStyle: "background: transparent;",
                style: "background: transparent;padding:20px;",
                labelWidth: '200',
                items: [this.masterItems, this.userGrpCombo]
            });
            this.mapUsersGroupWindow = new Wtf.Window({
                modal: true,
                title: "Users Group Mapping",
                iconCls: getButtonIconCls(Wtf.etype.deskera),
                bodyStyle: 'padding:5px;',
                buttonAlign: 'right',
                draggable: false,
                width: 425,
                scope: this,
                items: [{
                        region: 'center',
                        border: false,
                        bodyStyle: 'background:#f1f1f1;font-size:10px;',
                        autoScroll: true,
                        items: [this.mapUsersGroupForm]
                    }],
                buttons: [{
                        text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                        scope: this,
                        handler: function() {
                            if (this.mapUsersGroupForm.form.isValid()) {
                                Wtf.Ajax.requestEx({
                                    url: "ACCMaster/saveUserGroupFieldComboMapping.do",
                                    params: {
                                        masterItem: masterItemsid,
                                        masterVal: masterItemsval,
                                        userGroup: this.userGrpCombo.getValue(),
                                        fieldname: fieldname
                                    }
                                }, this, this.genMapSchemaSuccessResponse, this.genMapSchemaFailureResponse);
                            } else {
                                return;
                            }
                        }
                    }, {
                        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                        scope: this,
                        handler: function() {
                            this.mapUsersGroupWindow.close();
                        }
                    }]
            });
            this.mapUsersGroupWindow.show();
        }
    },
    genMapSchemaSuccessResponse: function (response) {
        if (response.isSuccess) {
            var titlename=WtfGlobal.getLocaleText("acc.masterconfiguration.mapSalesCommissionSchema");
                var recData = this.masterGrid.getSelectionModel().getSelected().data;
                if(this.mapSalesCommissionWindow){
                    this.mapSalesCommissionWindow.close()
                }
                if(this.mapUsersGroupWindow){
                    this.mapUsersGroupWindow.close()
                    titlename=WtfGlobal.getLocaleText("acc.masterconfiguration.usergrp.mapfcd");
                }
            Wtf.MessageBox.show({
                title: titlename,
                msg: response.msg,
                buttons: Wtf.MessageBox.OK,
                icon: Wtf.MessageBox.INFO,
                scope: this,
                fn: function (btn) {
                    if (btn == "ok") {
                        if (recData != undefined && recData != null) {
                            this.MasterItemStore.load({
                            params: {
                            mode: 112,
                            groupid: recData.id,
                            moduleIds: recData.moduleIds,
                            fieldlabel:recData.name
                            }
                            });
                        }
                    }
                }
            });
        }else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.common.msg1")], 2);
        }
    },
    genMapSchemaFailureResponse: function (response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if (response.msg)
            msg = response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    orderCustomOrDimensionField:function(){
        var customizeViewWin=new Wtf.customizeView({
//            moduleid:moduleId,
            isOrderCustOrDimFields:true,
            isForFormFields:false,
            isForLineFields:true,
            parentHelpModeId:null,
            parentId:null
        });
        customizeViewWin.show();
    },
    
    genSuccessResponsePriceValue:function(response, opt,outer, masterid){
        if(response.success){
             WtfComMsgBox([WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),response.msg],response.success*2+1);
              this.addPriceItemWindow.close();
        }
    },
    genSuccessResponse:function(response, opt,outer, masterid){
        if(this.loadmask){
            this.loadmask.hide();
        }
        if(response.success){
            this.fireEvent('update');//alert(opt.params.toSource())
            if(opt.params.toString().indexOf("mode=113")>=0 || opt.url=='ACCMaster/deleteDimension.do') {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),response.msg],response.success*2+1);
                this.masterStore.load();
                if(opt.url=='ACCMaster/deleteDimension.do')
                    loadCustomFieldColModel(undefined, '2,4,6,8,10,12,14,16,18,20,24,30'.split(','))
            } else{
                Wtf.MessageBox.show({//ERP-11355
                    title: WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),
                    msg: response.msg,
                    width: 380,
                    buttons: Wtf.MessageBox.OK,
                    animEl: 'mb9',
                    icon: Wtf.MessageBox.INFO,
                    scope: this,
                    fn: function(btn) {
                        if(!this.isTransationFormEntry) {
                            if(!outer){
                                var rec=this.masterGrid.getSelectionModel().getSelected();
                                var groupid = rec.data.id;
    //                            (function(){//ERP-11355
                                    if(groupid==6){ //Accounting specific code to reload global stores
                                        Wtf.TitleStore.reload();
                                    } else if(groupid==7){
                                        Wtf.CustomerCategoryStore.reload();
                                    } else if(groupid==8){
                                        Wtf.VendorCategoryStore.reload();
                                    } else if(groupid==9){
                                        //                        Wtf.dirtyStore.assetCategory = true;
                                        Wtf.AssetCategoryStore.reload();
                                    } else if(groupid==10){
                                        Wtf.DOStatusStore.reload();
                                    } else if(groupid==11){
                                        Wtf.GROStatusStore.reload();
                                    } else if(groupid==15){
                                        Wtf.salesPersonStore.reload();
                                    } else if(groupid==17){                       

                                    if(this.ibgReceivingDetails != undefined && this.ibgReceivingDetails != "" && opt.url=='ACCMaster/saveMasterItem.do'){
                                        this.saveIBGReceivingDetails(response.id);
                                    }
                                      Wtf.MPPaidToStore.reload();
                                    } else if(groupid==18){
                                        Wtf.RPReceivedFromStore.reload();
                                    } else if(groupid==19){
                                        Wtf.ProductCategoryStore.reload();
                                    } else if(groupid==20){
                                        Wtf.agentStore.reload();
                                    } else if(groupid==23){
                                        Wtf.assignedToStore.reload();
                                    } else if(groupid==24){
                                        Wtf.workOrderStatusStore.reload();
                                    } else if (groupid == 25) {
                                        Wtf.vehicleStore.reload();
                                    } else if (groupid == 26) {
                                        Wtf.driverStore.reload();
                                    } else if (groupid == 27) {
                                        Wtf.tripStore.reload();
                                    } else if(groupid==28){
                                        Wtf.ShippingRouteStore.reload();
                                    } 
                                      /*
                                       ERP-25020 : Removed Pop alert - "Failed to make connection with web server"
                                       Case : Work center type is taaged in Work Center creation form(Not Saved Form) and simultaneously we are deleting it from Master Configuration.
                                              In this case, wrong message was displayed.
                                              Now in this case, Work Center Type store will be reloaded after deleting value
                                       */
                                      else if(groupid==40) /* Groupid : 40 - For Work Centre Manager */
                                         {
                                         /* Condition to check if Work Centre Creation form is opened. */
                                        if(Wtf.getCmp("workcentreCreationTab")){
                                            var record= this.MasterItemGrid.getSelectionModel().getSelected();
                                            
                                            /*
                                             Code to check if Master Configuration- Work Centre Manager is assigned or not for Work Center Manager in Work centre form after that removing Work Centre Manager in Master Configuration.
                                              If yes , clear the value of combobox(Work Centre Manager) in Work Centre Form   
                                              if No , relaod the store. 
                                              */
                                            if(Wtf.getCmp("workcentreCreationTab").workCentreManager.getValue()==record.data.id){
                                                Wtf.getCmp("workcentreCreationTab").workCentreManager.clearValue();
                                            }else {
                                                Wtf.getCmp("workcentreCreationTab").workCenterManagerStore.load();     
                                            }
                                        }
                                    } else if(groupid==38) /* Groupid : 38 - For Work Centre Type */
                                    {
                                        if(Wtf.getCmp("workcentreCreationTab")){
                                         var record= this.MasterItemGrid.getSelectionModel().getSelected();
                                            if(Wtf.getCmp("workcentreCreationTab").workCentreType.getValue()==record.data.id) {
                                                Wtf.getCmp("workcentreCreationTab").workCentreType.clearValue();
                                            }else{
                                                Wtf.getCmp("workcentreCreationTab").workCenterTypeStore.load();      
                                            }   
                                        }
                                    } else if(groupid==51) /* Groupid : 51 - For Work Centre Location */
                                    {
                                        if(Wtf.getCmp("workcentreCreationTab")){
                                            var record= this.MasterItemGrid.getSelectionModel().getSelected();
                                            if(Wtf.getCmp("workcentreCreationTab").workcentreLocation.getValue()==record.data.id){
                                                Wtf.getCmp("workcentreCreationTab").workcentreLocation.clearValue();
                                            }else{
                                                Wtf.getCmp("workcentreCreationTab").workCenterLocationStore.load();       
                                            }
                                        }
                                    }
                                    if(rec)
                                        this.MasterItemStore.load({
                                            params:{
                                                groupid:groupid
                                            }
                                        });
    //                            }).defer(WtfGlobal.gridReloadDelay(),this);
                            }else{
                                if(masterid==6){ //Accounting specific code to reload global stores
                                    Wtf.TitleStore.reload();
                                } else if(masterid==7){
                                    Wtf.CustomerCategoryStore.reload();
                                } else if(masterid==8){
                                    Wtf.VendorCategoryStore.reload();
                                } else if(masterid==9){
                                    Wtf.AssetCategoryStore.reload();
                                } else if(masterid==10){
                                    Wtf.DOStatusStore.reload();
                                } else if(masterid==11){
                                    Wtf.GROStatusStore.reload();
                                } else if(masterid==15){                    //groupid
                                    Wtf.salesPersonStore.reload();
                                } else if(masterid==17){
                                    Wtf.MPPaidToStore.reload();
                                } else if(masterid==18){
                                    Wtf.RPReceivedFromStore.reload();
                                } else if(masterid==19){
                                    Wtf.ProductCategoryStore.reload();
                                } else if(masterid==20){                    //groupid
                                    Wtf.agentStore.reload();
                                } else if(masterid==23){
                                    Wtf.assignedToStore.reload();
                                } else if(masterid==24){
                                    Wtf.workOrderStatusStore.reload();
                                } else if (masterid == 25) {
                                    Wtf.vehicleStore.reload();
                                } else if (masterid == 26) {
                                    Wtf.driverStore.reload();
                                } else if (masterid == 27) {
                                    Wtf.tripStore.reload();
                                } else if(groupid==28){
                                    Wtf.ShippingRouteStore.reload();
                                } else if(groupid==40){
                                    if(Wtf.getCmp("workcentreCreationTab")){
                                        var record= this.MasterItemGrid.getSelectionModel().getSelected();
                                        if(Wtf.getCmp("workcentreCreationTab").workCentreManager.getValue()==record.data.id){
                                            Wtf.getCmp("workcentreCreationTab").workCentreManager.clearValue();
                                        }else {
                                            Wtf.getCmp("workcentreCreationTab").workCenterManagerStore.load();     
                                        }
                                    }
                                } else if(groupid==38){
                                    if(Wtf.getCmp("workcentreCreationTab")){
                                        var record= this.MasterItemGrid.getSelectionModel().getSelected();
                                        if(Wtf.getCmp("workcentreCreationTab").workCentreType.getValue()==record.data.id) {
                                            Wtf.getCmp("workcentreCreationTab").workCentreType.clearValue();
                                        }else{
                                            Wtf.getCmp("workcentreCreationTab").workCenterTypeStore.load();      
                                        }   
                                    }
                                } else if(groupid==51){
                                    if(Wtf.getCmp("workcentreCreationTab")){
                                        var record= this.MasterItemGrid.getSelectionModel().getSelected();
                                        if(Wtf.getCmp("workcentreCreationTab").workcentreLocation.getValue()==record.data.id){
                                            Wtf.getCmp("workcentreCreationTab").workcentreLocation.clearValue();
                                        }else{
                                            Wtf.getCmp("workcentreCreationTab").workCenterLocationStore.load();       
                                        }
                                    }
                                }
                                this.destroy();
                            }
                        }
                    }
                });
            }
            if(this.addMasterItemWindow) 
            this.addMasterItemWindow.close();
        }
        else{            
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.masterConfig.tabTitle"),response.msg],2);
            this.synceClaimData.enable();   //ERP-29322
        }

    },

    genFailureResponse:function(response){
        if(this.loadmask){
            this.loadmask.hide();
        }
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.synceClaimData.enable();   //ERP-29322
    },

    addMasterItemOuter:function(isEdit,id,outer){
        if(outer){
            this.masterStore.on("load",function(){
                this.AddMasterItem(isEdit,id,outer);
            },this);
        }
        else
            this.AddMasterItem(isEdit,id,outer);
    },
    
    saveIBGReceivingDetails:function(masterItemId){// sends seperate request for saving IBG Details after Master Item saving
        if(this.ibgReceivingDetails != undefined && this.ibgReceivingDetails != ""){
            Wtf.Ajax.requestEx({
                    url:'ACCVendorCMN/saveIBGReceivingBankDetailsJSON.do',
                    params: {
                        ibgReceivingDetails:this.ibgReceivingDetails,
                        masterItemId:masterItemId
                    }
            },this,function(){
                this.ibgReceivingDetails="";
                this.ibgReceivingDetailsArr=[];
            },function(){
                
            });
        }
    },    
    
    configureGSTDefaultFields : function(gstConfigType,call){
        if (gstConfigType != undefined && gstConfigType != "") {
            if (call == 'forMaster') {      
                /*
                 * this call for MasterItem (GST Related Default custom fields) i.e. to disable Edit And Delete Buttons 
                 */
                    if (gstConfigType == Wtf.GST_CONFIG_TYPE.ISFORMULTIENTITY || gstConfigType == Wtf.GST_CONFIG_TYPE.MANDETORY_FIELD || gstConfigType == Wtf.GST_CONFIG_TYPE.ISFORGST || gstConfigType == Wtf.GST_CONFIG_TYPE.CUSTOM_TO_ENTITY || gstConfigType == Wtf.GST_CONFIG_TYPE.HSN_SAC_CODE) {
                        this.deleteCustomColumn.disable();
                    }                      
                    if (gstConfigType == Wtf.GST_CONFIG_TYPE.ISFORMULTIENTITY || gstConfigType == Wtf.GST_CONFIG_TYPE.HSN_SAC_CODE) {
                        this.editDimensionColumn.disable();
                    }
                    if((gstConfigType == Wtf.GST_CONFIG_TYPE.ISFORGST && Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA)  || gstConfigType== Wtf.GST_CONFIG_TYPE.UQC){
                        this.MasterItemAdd.disable();
                    }
            } else if (call == 'forMasterValue') {  
                /*
                 * this call for Values of MasterItem i.e. to disable Edit And Delete Buttons 
                 */
                var isEntityDefaultValueSelcted = false;
                if (this.selectionModel.getSelections().length > 0) {
                    isEntityDefaultValueSelcted = (gstConfigType == Wtf.GST_CONFIG_TYPE.ISFORMULTIENTITY && this.selectionModel.getSelected().data.name == Wtf.cdomain);
                }
                    
                    if (isEntityDefaultValueSelcted || gstConfigType == Wtf.GST_CONFIG_TYPE.ISFORGST) {
                        this.MasterItemDelete.disable();
                    }                                                              
                    if (gstConfigType == Wtf.GST_CONFIG_TYPE.MANDETORY_FIELD || gstConfigType == Wtf.GST_CONFIG_TYPE.ISFORGST) {
                        this.MasterItemEdit.disable();
                    }  
                    if((gstConfigType == Wtf.GST_CONFIG_TYPE.ISFORGST && Wtf.account.companyAccountPref.countryid == Wtf.Country.INDIA) || gstConfigType== Wtf.GST_CONFIG_TYPE.UQC){
                        this.MasterItemAdd.disable();
                    }
        }
        }
    },    
    saveMasterItemSequenceData:function(){
        var json=[];
        for(var i=0;i<this.grid.store.getCount();i++){
            json.push({
                "id":this.grid.store.getAt(i).get("id"),
                "name":this.grid.store.getAt(i).get("name"),
                "itemsequence":i+1
            });
        }
        var groupid = this.masterGrid.getSelectionModel().getSelected().data["id"]
                
        Wtf.Ajax.requestEx({
            url:"ACCAccountCMN/saveMasterItemDataSequence.do",
            params:{
                sequenceData:Wtf.util.JSON.encode(json),
                groupid:groupid
            }
        },
        this,
        function(response){
            if(response.success==true){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg]);
            } else {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
            }
            this.saveMasterItemSequence.setDisabled(true);
        },
        function(response){
            var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
            if(response.msg)msg=response.msg;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        });
    },
    setSalesPersonData:function(rec,appsUserrec){
        this.salesPersonCode.setValue(rec.data['employeeid']);
        var department="";
        if(appsUserrec.data['department']!=''){
            department=appsUserrec.data['department'];
            department=" - "+ department;
        } 
        Wtf.getCmp("masteritemname").setValue(rec.data['fullname']+ department);
        Wtf.getCmp("masteritememailid").setValue(rec.data['emailid']);
        this.salesPersonContactNumner.setValue(rec.data['contactno']);
        this.salesPersonAddress.setValue(rec.data['address']);
        this.salesPersonDesignation.setValue(rec.data['designation']);
    }
});



function masterInvoiceTerms(winid){
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

Wtf.SetPriceWindow = function (config){
    this.addEvents({
        'setAutoNumbers':true
    });
    Wtf.apply(this,config);
    Wtf.SetPriceWindow.superclass.constructor.call(this,{
        buttons:[{
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //"Save",
            scope:this,
            handler:function () {
                if(!this.AddEditForm.form.isValid())
                {
                    return;
                } else {                    
                    arguments[0].disable();
                    var param={
                         name:Wtf.getCmp("itemnameid").getValue(),
                         itemid:this.rec.data['id'],
                         price:Wtf.getCmp("itempriceid").getValue()
                    }
                    Wtf.Ajax.requestEx({
//                        url:Wtf.req.base+'UserManager.jsp',
                        url : "ACCMaster/saveMasterItemPrice.do",
                        params:param
                    },this,
                    function(req,res){
                        var restext=req;
                        if(restext.success){     
                            this.close();
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.setPrice.format.save")],0);                            
                        } else
                        	WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.setPrice.format.failure")],1);

                    },
                    function(req){
                        var restext=req;
                        if(restext.msg !=""){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),restext.msg],1);
                        } else {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.setPrice.format.failure")],1);
                        }
                    });
                }
            }
        }, {
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //"Cancel",
            scope:this,
            handler:function (){
                this.close();
            }
        }]
    });    
}

Wtf.extend(Wtf.SetPriceWindow,Wtf.Window,{
    layout:"border",
    modal:true,
    title:"Manage Tariff", //"Change Password",//WtfGlobal.getLocaleText("acc.changePass.tabTitle")
    id:'setpriceforaccounting',
    width:450,
    height:400,
    resizable:false,
    iconCls: "pwnd deskeralogoposition",
    initComponent:function (){
        Wtf.SetPriceWindow.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        Wtf.form.Field.prototype.msgTarget = 'side';
        this.add(this.northPanel);        
        this.add(this.centerPanel);
        this.add(this.AddEditForm);
    },
    GetNorthPanel:function (){
      this.sequenceFormatRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'value'},
            {name: 'price'}
         ]);
         
      this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
                
            },this.sequenceFormatRec),
            url : "ACCMaster/getMasterItemPrice.do"
         });                 
         this.sequenceFormatStore.load({
            params:{
                 itemid:this.rec.data['id']
            }
        });
        this.sequenceCM= new Wtf.grid.ColumnModel([{
                  header:'Value',
                  dataIndex:'value'
              },{
                  header:'Price'+"("+WtfGlobal.getCurrencyName()+")",
                  dataIndex:'price'
              },{
                  header:'Delete',
                  align:'center',
                  renderer:function(){
//                      return "<div class='pwnd deleteSequenceNo'> </div>";
                      return "<div class='pwnd delete-gridrow' > </div>";
                  }
              }
        ]);
     this.sequencenoGrid = new Wtf.grid.GridPanel({
          store: this.sequenceFormatStore,
          cm:this.sequenceCM,
          height:300,
          viewConfig:{
              forceFit:true,
              emptyText:"No record found"
          }
      })     
    this.sequencenoGrid.on("cellclick", this.deleteSequence, this);
     this.northPanel = new Wtf.Panel({
            region:"north",
            height:75,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml("Manage Tariff Structure ","Manage Tariff Structure for '"+this.rec.data['name']+"'" ,'../../images/createuser.png',false,'0px 0px 0px 0px')
        });
        this.centerPanel = new Wtf.Panel({
            region:"center",
            items:[this.sequencenoGrid]
        })
    },    
    GetAddEditForm:function (){
        
          this.titlePanel = new Wtf.Panel({
            border:false,
            bodyStyle:"padding-bottom: 5px;text-align: center;",
            html:"<b>Enter fields and click on save to add new tier</b>"
        });
        this.AddEditForm = new Wtf.form.FormPanel({
            region:"south",
            border:false,
            height:100,
            labelWidth:135,
//            defaults:{width:200,allowBlank:false},
//            defaultType : 'textfield',
            bodyStyle:"background-color:#f1f1f1;padding: 20px 35px 35px;",
            items:[this.titlePanel,{
                        xtype: 'textfield',
                        fieldLabel:WtfGlobal.getLocaleText("acc.masterconfig.setPriceFieldItem")+"*",
                        name: "itemname",
//                        msgTarget: 'under',
                        width: 200,
                        maxLength:50,
                        validator:Wtf.ValidateTariffName,
//                        value:(isEdit)?rec.data['name']:'',
                        allowBlank: false,
                        id: "itemnameid"
                    },{
                        xtype: 'numberfield',
                        fieldLabel:WtfGlobal.getLocaleText("acc.masterconfig.setPriceFieldItemPrice")+"("+WtfGlobal.getCurrencyName()+")*",
                        name: "itemprice",
//                        msgTarget: 'under',
                        width: 200,
                        maxLength:50,
                        allowBlank: false,
                        id: "itempriceid"
                    }]
        });
    },
    deleteSequence:function(gd, ri, ci, e) {
        var event = e;
        if(event.target.className == "pwnd delete-gridrow") {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), "Are you sure you want to delete selected Price format?",function(btn){
            if(btn=="yes") {            
            var itemid = gd.getStore().getAt(ri).data.id;
            Wtf.Ajax.requestEx({
//                        url:Wtf.req.base+'UserManager.jsp',
                        url : "ACCMaster/deleteMasterItemPrice.do",
                        params:{
                           itempriceid:itemid
                        }
                    },this,
                    function(req,res){
                        var restext=req;
                        if(restext.success){
                            this.sequenceFormatStore.remove(this.sequenceFormatStore.getAt(ri));
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.setPrice.format.delete")],0);                            
                        } else 
                        	WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.setPrice.format.deletefailure")],1);

                    },
                    function(req){
                        var restext=req;
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.setPrice.format.deletefailure")],1);
                    });
        }

    }, this)
    }
   }
});





Wtf.SetPriceFormulaWindow = function (config){
    this.addEvents({
        'setAutoNumbers':true
    });
    Wtf.apply(this,config);
    Wtf.SetPriceFormulaWindow.superclass.constructor.call(this,{
        buttons:[{
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //"Save",
            scope:this,
            handler:function () {
                if(!this.AddEditForm.form.isValid())
                {
                    return;
                } else {                    
                    arguments[0].disable();
                    var param={
                         lowerlimitvalue:Wtf.getCmp("lowerlimitvalueid").getValue(),
                         upperlimitvalue:Wtf.getCmp("upperlimitvalueid").getValue(),
                         basevalue:Wtf.getCmp("basevalueid").getValue(),
                         incvalue:Wtf.getCmp("incvalueid").getValue(),
                         itemid:this.rec.data['id']
                    }
                    Wtf.Ajax.requestEx({
//                        url:Wtf.req.base+'UserManager.jsp',
                        url : "ACCMaster/saveMasterItemPriceFormula.do",
                        params:param
                    },this,
                    function(req,res){
                        var restext=req;
                        if(restext.success){     
                            this.close();
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.setPriceFormula.format.save")],0);                            
                        } else
                        	WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.setPriceFormula.format.failure")],1);

                    },
                    function(req){
                        var restext=req;
                        if(restext.msg !=""){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),restext.msg],1);
                        } else {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.setPriceFormula.format.failure")],1);
                        }
                    });
                }
            }
        }, {
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //"Cancel",
            scope:this,
            handler:function (){
                this.close();
            }
        }]
    });    
}

Wtf.extend(Wtf.SetPriceFormulaWindow,Wtf.Window,{
    layout:"border",
    modal:true,
    title:"Manage Tariff", //"Change Password",//WtfGlobal.getLocaleText("acc.changePass.tabTitle")
    id:'setpriceforaccounting',
    width:550,
    height:500,
    resizable:false,
    iconCls: "pwnd deskeralogoposition",
    initComponent:function (){
        Wtf.SetPriceFormulaWindow.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        Wtf.form.Field.prototype.msgTarget = 'side';
        this.add(this.northPanel);        
        this.add(this.centerPanel);
        this.add(this.AddEditForm);
    },
    GetNorthPanel:function (){
      this.sequenceFormatRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'lowerlimit'},
            {name: 'upperlimit'},
            {name: 'base'},
            {name: 'increment'}
         ]);
         
      this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
                
            },this.sequenceFormatRec),
            url : "ACCMaster/getMasterItemPriceFormula.do"
         });                 
         this.sequenceFormatStore.load({
            params:{
                 itemid:this.rec.data['id']
            }
        });
        this.sequenceCM= new Wtf.grid.ColumnModel([{
                  header:'Lower Limit',
                  dataIndex:'lowerlimit'
              },{
                  header:'Upper Limit',
                  dataIndex:'upperlimit'
              },{
                  header:'Base Price'+"("+WtfGlobal.getCurrencyName()+")",
                  dataIndex:'base'
              },{
                  header:'Increment Price'+"("+WtfGlobal.getCurrencyName()+")",
                  dataIndex:'increment'
              },{
                  header:'Delete',
                  align:'center',
                  renderer:function(){
//                      return "<div class='pwnd deleteSequenceNo'> </div>";
                      return "<div class='pwnd delete-gridrow' > </div>";
                  }
              }
        ]);
     this.sequencenoGrid = new Wtf.grid.GridPanel({
          store: this.sequenceFormatStore,
          cm:this.sequenceCM,
          height:200,
          viewConfig:{
//              forceFit:true,
              emptyText:"No record found"
          }
      })     
    this.sequencenoGrid.on("cellclick", this.deleteSequence, this);
     this.northPanel = new Wtf.Panel({
            region:"north",
            height:90,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml("Manage Tariff Structure ","Manage Tariff Structure for '"+this.rec.data['name']+"'<br> All limits are inclusive." ,'../../images/createuser.png',false,'0px 0px 0px 0px')
        });
        this.centerPanel = new Wtf.Panel({
            region:"center",
            items:[this.sequencenoGrid]
        })
    },    
    GetAddEditForm:function (){
        this.titlePanel = new Wtf.Panel({
            border:false,
            bodyStyle:"padding-bottom: 5px;text-align: center;",
            html:"<b>Enter fields and click on save to add new tier</b>"
        });
        
        this.AddEditForm = new Wtf.form.FormPanel({
            region:"south",
            border:false,
            height:150,
            labelWidth:200,
//            defaults:{width:200,allowBlank:false},
            defaultType : 'textfield',
            bodyStyle:"background-color:#f1f1f1;padding:15px 35px 35px",
            items:[this.titlePanel,{
                        xtype: 'numberfield',
                        fieldLabel:WtfGlobal.getLocaleText("acc.masterconfig.setlowerlimitValue"),
                        name: "lowerlimitvalue",
//                        msgTarget: 'under',
                        labelWidth:200,
                        width: 200,
                        maxLength:50,
                        allowBlank: false,
                        id: "lowerlimitvalueid"
                    },{
                        xtype: 'numberfield',
                        fieldLabel:WtfGlobal.getLocaleText("acc.masterconfig.setupperlimitValue"),
                        name: "upperlimitvalue",
//                        msgTarget: 'under',
                        width: 200,
                        labelWidth:200,
                        maxLength:50,
                        allowBlank: false,
                        id: "upperlimitvalueid"
                    },{
                        xtype: 'numberfield',
                        fieldLabel:WtfGlobal.getLocaleText("acc.masterconfig.setBaseValue")+"("+WtfGlobal.getCurrencyName()+")*",
                        name: "basevalue",
//                        msgTarget: 'under',
                        width: 200,
                        labelWidth:200,
                        maxLength:50,
                        allowBlank: false,
                        id: "basevalueid"
                    },{
                        xtype: 'numberfield',
                        fieldLabel:WtfGlobal.getLocaleText("acc.masterconfig.setIncValue")+"("+WtfGlobal.getCurrencyName()+")*",
                        name: "incvalue",
                        labelWidth:200,
//                        msgTarget: 'under',
                        width: 200,
                        maxLength:50,
                        allowBlank: false,
                        id: "incvalueid"
                    }]
        });
    },
    deleteSequence:function(gd, ri, ci, e) {
        var event = e;
        if(event.target.className == "pwnd delete-gridrow") {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), "Are you sure you want to delete selected Price format?",function(btn){
            if(btn=="yes") {            
            var itemid = gd.getStore().getAt(ri).data.id;
            Wtf.Ajax.requestEx({
//                        url:Wtf.req.base+'UserManager.jsp',
                        url : "ACCMaster/deleteMasterItemPriceFormula.do",
                        params:{
                           itempriceid:itemid
                        }
                    },this,
                    function(req,res){
                        var restext=req;
                        if(restext.success){
                            this.sequenceFormatStore.remove(this.sequenceFormatStore.getAt(ri));
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.setPrice.format.delete")],0);                            
                        } else 
                        	WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.setPrice.format.deletefailure")],1);

                    },
                    function(req){
                        var restext=req;
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.setPrice.format.deletefailure")],1);
                    });
        }

    }, this)
    }
   }
});

Wtf.salesCommisionSchemaWindow = function (config){
    this.commissionType=config.type;
    this.addEvents({
        'setAutoNumbers':true
    });
    this.selectedRecordId = "";
    Wtf.apply(this,config);
    Wtf.salesCommisionSchemaWindow.superclass.constructor.call(this,{
        buttons:[this.saveButton = new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.common.saveBtn"),  //"Save",
            scope:this,
            minWidth:80,
            handler:function () {
                if(!this.AddEditForm.form.isValid())
                {
                    WtfComMsgBox(2,2);
                    return;
                } else {
                     var categoryid="";
                    var productid="";
                    var gridStore=this.sequencenoGrid.getStore();
                    var lowerlimit=this.lowerLimit.getValue();
                    var upperlimit=this.upperLimit.getValue();
                    if(this.commissionType==2){
                             categoryid=this.productCategory.getValue();
                    }
                    if(this.commissionType==4){//For Specific Product rule
                        productid=this.product.getValue();
                    }
                    if(this.commissionType==3){
                        if(upperlimit < lowerlimit){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.masterConfig.Salescommissio.MaxDays.Addfailure")],2);
                            return;
                        }
                    }
                    var percentage=this.percentageTypeCombo.getValue();
                    var amount=this.percentageValue.getValue();                   
                        if(gridStore.getCount()>0){
                            for(var i=0;i<gridStore.getCount();i++){//Limit Validation
                                var rec=gridStore.data.get(i);
                                 if(this.commissionType==1 || this.commissionType==3){
                                    if(!(this.isEdit && this.selectedRecordId == rec.data.id) && (lowerlimit==rec.data.lowerlimit ||lowerlimit > rec.data.lowerlimit && lowerlimit <= rec.data.upperlimit)){
//                                        lowerlimit <= rec.data.upperlimit, Added = because Both dates are Inclusive.
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.master.SalsCommissin.limitError")],1);
                                        return;
                                    }
                                 }else if(this.commissionType==2){//For Brand Commission type.
                                     if(!(this.isEdit && this.selectedRecordId == rec.data.id) && rec.data.categoryid==categoryid){
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.master.SalsCommissin.brandselError")],1);
                                        return;
                                    }
                                     
                                 } if(this.commissionType==4){ //For Specific Product only
                                     if(!(this.isEdit && this.selectedRecordId == rec.data.id) && rec.data.productid==productid){
                                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("For selected Product Rule is already set.if you want to reset rule delete current rule for this Product.")],2);
                                        return;
                                    }
                                     
                                 }
                            }
                        }
                        if((this.commissionType==1 || this.commissionType==3) && percentage==1 && amount >100){ //Percentage Validation
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.master.SalsCommissin.perError")],1);
                                    return;
                        }                
                    this.saveButton.disable();

                    var param={
                         lowerlimit:lowerlimit,
                         upperlimit:upperlimit,
                         percentage:this.percentageTypeCombo.getValue(),
                         amount:amount,
                         categoryid:categoryid,
                         commissiontype:this.commissionType,
                         productid:productid,
                         id:(this.isEdit)?this.selectedRecordId:"",
                         itemid:this.rec.data['id']
                    }
                    Wtf.Ajax.requestEx({
                        url : "ACCMaster/saveSalesComissionScehma.do",
                        params:param
                    },this,
                    function(req,res){
                        var restext=req;
                        this.saveButton.enable();
                        if(restext.success){
                            this.resetData();
                            this.reloadGridStore();
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.master.SalsCommissin.Save")],0);                            
                        } else{
                        	WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.master.SalsCommissin.error")],1);
                        }

                    },
                    function(req){
                        var restext=req;
                        this.saveButton.enable();
                        if(restext.msg !=""){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),restext.msg],1);
                        } else {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.master.SalsCommissin.error")],1);
                        }
                    });
                }
            }
        }),{
            text:WtfGlobal.getLocaleText("acc.common.reset"),  //"Reset",
            scope:this,
            handler:this.resetData
        }, {
            text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //"Cancel",
            scope:this,
            handler:function (){
                this.close();
            }
        }]
    });    
}

Wtf.extend(Wtf.salesCommisionSchemaWindow,Wtf.Window,{
    layout:"border",
    modal:true,
    title:"Sales Commision Schema", //"Change Password",//WtfGlobal.getLocaleText("acc.changePass.tabTitle")
    id:'setpriceforaccounting',
    width:650,
    height:500,
    resizable:false,
    iconCls: "pwnd deskeralogoposition",
    initComponent:function (){
        Wtf.salesCommisionSchemaWindow.superclass.initComponent.call(this);
        this.GetNorthPanel();
        this.GetAddEditForm();
        Wtf.form.Field.prototype.msgTarget = 'side';
        this.add(this.northPanel);        
        this.add(this.centerPanel);
        this.add(this.AddEditForm);
    },
    GetNorthPanel:function (){
      this.sequenceFormatRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'lowerlimit'},
            {name: 'upperlimit'},
            {name: 'percentagetype'},
            {name: 'percentagevalue'},
            {name: 'amount'},
            {name: 'commissiontype'},
            {name: 'categoryid'},
            {name: 'categoryname'},
            {name:'pid'},
            {name:'productname'},
            {name:'productid'}
         ]);
         
      this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
                
            },this.sequenceFormatRec),
            url : "ACCMaster/getSalesComissionScehma.do"
         });                 
         this.sequenceFormatStore.load({
            params:{
                 itemid:this.rec.data['id'],
                 commissiontype:this.commissionType
            }
        });
       this.percentageTypeStore = new Wtf.data.SimpleStore({
            fields:[{
                name:'id'
            },{
                name:'name'
            }],
            data:[['1','Percentage'],['2','Flat']]
        });
       
         this.itemRec = Wtf.data.Record.create([
            {name:"id"},
            {name:"name"},
            {name:"modulename"},
            {name:"fieldtype"},
            {name:"parentid"},
            {name:"leaf"},
            {name:"fieldtype"},
            {name:'level',type:'int'}
        ]);
        var baseparam = {
            mode:112,
            groupid:19
            
        };
        this.itemStore = new Wtf.data.Store({
              url:"ACCMaster/getMasterItems.do",
            baseParams:baseparam,
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.itemRec)
        }); 
        this.itemStore.load();
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
        url:"ACCProduct/getProductsForCombo.do",
        baseParams:{
            mode:22
        },
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        },this.productRecord)
    });
    if(this.commissionType==4){
    this.productStore.load();
    }
        this.sequenceCM= new Wtf.grid.ColumnModel([{
                  header:(this.commissionType==1)?'Lower Limit':'Min Days',
                  hidden:(this.commissionType==1 || this.commissionType==3)?false:true,// for Amount and Payment Term
                  align:'right',
                  dataIndex:'lowerlimit'
              },{
                  header:'Brand/Product Category',
                  hidden:(this.commissionType==1 || this.commissionType==3 ||this.commissionType == 4)?true:false,
                  dataIndex:'categoryname'
              },{
                  header:'Product Name',
                  hidden:(this.commissionType ==1 || this.commissionType==2 ||this.commissionType==3)?true:false,
                  dataIndex:'productname'        
              },{
                  header:(this.commissionType==1)?'Upper Limit':'Max Days',
                  hidden:(this.commissionType==1 || this.commissionType==3)?false:true,
                  align:'right',
                  dataIndex:'upperlimit'
              },{
                  header:'Commission Type',
                   hidden:(this.commissionType==1 || this.commissionType==3)?false:true,
                  dataIndex:'percentagevalue'
              },{
                  header:this.commissionType==1?'Percentage/Amount'+"("+WtfGlobal.getCurrencyName()+")":WtfGlobal.getLocaleText("acc.masterconfig.percentageValue"),
                  align:'right',
                  dataIndex:'amount'
              },{
                  header:'Delete',
                  align:'center',
                  width:(this.commissionType==1 || this.commissionType==3)?70:40,
                  renderer:function(){
//                      return "<div class='pwnd deleteSequenceNo'> </div>";
                      return "<div class='pwnd delete-gridrow' style='margin-left:40px;'> </div>";
                  }
              }
        ]);
     this.sequencenoGrid = new Wtf.grid.GridPanel({
          store: this.sequenceFormatStore,
          cm:this.sequenceCM,
          height:200,
          viewConfig:{
              forceFit:true,
              emptyText:"No record found"
          }
      })     
    this.sequencenoGrid.on("cellclick", this.deleteSequence, this);
     this.northPanel = new Wtf.Panel({
            region:"north",
            height:90,
            border:false,
            bodyStyle:"background:white;border-bottom:1px solid #bfbfbf;",
            html:getTopHtml("Sales Commission Schema ","Manage Sales Commission Schema '"+this.rec.data['name'],'../../images/createuser.png',false,'0px 0px 0px 0px')   //            html:getTopHtml("Sales Commision Schema ","Manage Sales Commision Schema '"+this.rec.data['name'],'../../images/createuser.png',false,'0px 0px 0px 0px')   //

        });
        this.centerPanel = new Wtf.Panel({
            region:"center",
            items:[this.sequencenoGrid]
        })
    }, 
    GetAddEditForm:function (){
        this.titlePanel = new Wtf.Panel({
            border:false,
            bodyStyle:"padding-bottom: 5px;text-align: center;"
//            html:"<b>Enter fields and click on save to add new tier</b>"
        });
        
        this.percentageTypeCombo = new Wtf.form.ComboBox({
                triggerAction: 'all',
//                hidden: false,
                mode: 'local',
                valueField: 'id',
                displayField: 'name',
                store: this.percentageTypeStore,
                fieldLabel: 'Select Comission Type*',   

                typeAhead: true,
                forceSelection: true,
                hiddenName: 'type'                
            });    
       if(this.commissionType==2 || this.commissionType==3 ||this.commissionType==4){// for brand or payment term
           this.percentageTypeCombo.setValue(1);
           this.percentageTypeCombo.disable();
       }
       
      this.percentageTypeCombo.on('select',function(){//writen for Label Change
          if(this.percentageTypeCombo.getValue()==1){
              WtfGlobal.updateFormLabel(this.percentageValue,WtfGlobal.getLocaleText("acc.masterconfig.percentageValue")+"*:");
          }else{
              WtfGlobal.updateFormLabel(this.percentageValue,WtfGlobal.getLocaleText("acc.masterconfig.amount")+"("+WtfGlobal.getCurrencyName()+")*:");
          }
      },this);
        this.productCategory = new Wtf.form.ComboBox({
                triggerAction: 'all',
//                hidden: false,
                mode: 'local',
                allowBlank: (this.commissionType==1 || this.commissionType==3 || this.commissionType==4)?true:false,
                valueField: 'id',
                displayField: 'name',
                store: this.itemStore,
                fieldLabel: 'Select Brand/Product Category*',
                typeAhead: true,
                forceSelection: true,
                hiddenName: 'type',
                hidden:(this.commissionType==1 || this.commissionType==3 || this.commissionType==4)?true:false,
                hideLabel:(this.commissionType==1 || this.commissionType==3|| this.commissionType == 4)==1?true:false
            });
            this.product =new Wtf.form.ExtFnComboBox({
                width:250,
                listWidth:'250',
                fieldLabel:WtfGlobal.getLocaleText("acc.field.SelectProducts"),
                allowBlank: (this.commissionType==1 ||this.commissionType==2 || this.commissionType==3)?true:false,
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
                hidden:(this.commissionType==1 ||this.commissionType==2 || this.commissionType==3)?true:false,
                hideLabel:(this.commissionType==1 ||this.commissionType==2 || this.commissionType==3)==1?true:false
            });
            
        this.lowerLimit = new Wtf.form.NumberField({
            fieldLabel:((this.commissionType==1)?WtfGlobal.getLocaleText("acc.masterconfig.setlowerlimitValue"):"Min Days")+"*",
            name: "lowerlimit",
            hidden:(this.commissionType==1 || this.commissionType==3)?false:true,
            hideLabel:(this.commissionType==1 || this.commissionType==3)?false:true,
            //                        msgTarget: 'under',
            labelWidth:200,
            width: 200,
            maxLength:50,
            allowBlank: (this.commissionType==1 || this.commissionType==3)?false:true,
            id: "lowerlimitid"
        });
            
        this.upperLimit = new Wtf.form.NumberField({
            fieldLabel:((this.commissionType==1)?WtfGlobal.getLocaleText("acc.masterconfig.setupperlimitValue"):"Max Days"),
            name: "upperlimit",
            //                        msgTarget: 'under',
            width: 200,
            labelWidth:200,
            maxLength:50,
            hidden:(this.commissionType==1 || this.commissionType==3)?false:true,
            hideLabel:(this.commissionType==1 || this.commissionType==3)?false:true,
            allowBlank: (this.commissionType==1 || this.commissionType==3)?false:true,
            id: "upperlimitid"
        });
            
        this.percentageValue = new Wtf.form.NumberField({
            fieldLabel:(WtfGlobal.getLocaleText("acc.masterconfig.percentageValue")),//"("+WtfGlobal.getCurrencyName()+")*")
            name: "amount",
            labelWidth:200,
            //                        msgTarget: 'under',
            width: 200,
            maxLength:50,
            allowBlank: false,
            id: "amountid"
        });
        
        this.AddEditForm = new Wtf.form.FormPanel({
            region:"south",
            border:false,
            height:150,
            labelWidth:200,
//            defaults:{width:200,allowBlank:false},
            defaultType : 'textfield',
            bodyStyle:"background-color:#f1f1f1;padding:15px 35px 35px",
            items:[this.titlePanel,
                this.productCategory,
                this.product,
                this.lowerLimit,
                this.upperLimit,
                this.percentageTypeCombo,
                this.percentageValue
                ]
        });
    },
    deleteSequence:function(gd, ri, ci, e) {
        var event = e;
        if(event.target.className == "pwnd delete-gridrow") {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), "Are you sure you want to delete selected Schema?",function(btn){
            if(btn=="yes") {            
            var itemid = gd.getStore().getAt(ri).data.id;
            Wtf.Ajax.requestEx({
//                        url:Wtf.req.base+'UserManager.jsp',
                        url : "ACCMaster/deleteSalesComissionScehma.do",
                        params:{
                           itempriceid:itemid
                        }
                    },this,
                    function(req,res){
                        var restext=req;
                        if(restext.success){
                            this.sequenceFormatStore.remove(this.sequenceFormatStore.getAt(ri));
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.masterConfig.Salescommissio.Delete")],0);                            
                        } else 
                        	WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.masterConfig.Salescommissio.Deletefailure")],1);

                    },
                    function(req){
                        var restext=req;
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.masterConfig.Salescommissio.Deletefailure")],1);
                    });
        }

    }, this),
    this.resetData();
    }else{
        this.editForm(gd, ri);
    }
   },
   
    editForm:function(gd, ri) {
       
        this.productCategory.setValue(gd.getStore().getAt(ri).data.categoryid);
        this.product.setValue(gd.getStore().getAt(ri).data.productid);
        this.lowerLimit.setValue(gd.getStore().getAt(ri).data.lowerlimit);
        this.upperLimit.setValue(gd.getStore().getAt(ri).data.upperlimit);
        this.percentageTypeCombo.setValue(gd.getStore().getAt(ri).data.percentagetype);
        this.percentageValue.setValue(gd.getStore().getAt(ri).data.amount);
       
        this.isEdit=true;
        this.selectedRecordId = gd.getStore().getAt(ri).data.id;
    },
    
    reloadGridStore:function(){
        this.sequencenoGrid.getStore().reload();
    },
    
resetData:function(){
    if(this.productCategory)
            this.productCategory.reset();
        if(this.product)
            this.product.reset();
        if(this.lowerLimit)
            this.lowerLimit.reset();
        if(this.upperLimit)
            this.upperLimit.reset();
        if(this.percentageTypeCombo)
            this.percentageTypeCombo.reset();
        if(this.percentageValue)
            this.percentageValue.reset();
        this.isEdit=false;
        this.selectedRecordId = "";
    }
});


