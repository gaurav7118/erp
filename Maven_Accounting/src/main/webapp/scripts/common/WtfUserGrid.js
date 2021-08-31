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
Wtf.common.UserGrid=function(config){

    this.usersRec = new Wtf.data.Record.create([
        {name: 'userid'},
        {name: 'username'},
        {name: 'fname'},
        {name: 'lname'},
        {name: 'image'},
        {name: 'emailid'},
        {name: 'lastlogin',type: 'string'},
        {name: 'aboutuser'},
        {name: 'address'},
        {name: 'contactno'},
        {name: 'rolename'},
        {name: 'roleid'},
        {name: 'department'},
        {name: 'isApprover'},
        {name : 'usergroup'}
    ]);

    this.userds = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            totalProperty: 'count',
            root: "data"
        },this.usersRec),
//        url: Wtf.req.base+'UserManager.jsp',
        url : "ProfileHandler/getAllUserDetails.do",
        baseParams:{
            mode:11,
            usersVisibilityFlow : Wtf.account.companyAccountPref.usersVisibilityFlow
        }
    });
    this.userds.load({params:{start:0,limit:30}});
    WtfComMsgBox(29,4,true);

    this.userds.on('datachanged', function() {
		if (this.pP.combo) {
			var p = this.pP.combo.value;
			this.quickPanelSearch.setPage(p);
		}
             }, this);
    this.userds.on('load',this.storeloaded,this);

//    this.selectionModel = new Wtf.grid.CheckboxSelectionModel();
    this.selectionModel = new Wtf.grid.RadioSelectionModel();
        
    this.gridcm= new Wtf.grid.ColumnModel([this.selectionModel,{
    	header: WtfGlobal.getLocaleText("acc.userAdmin.image"),  //"Image",
        dataIndex: 'image',
        width : 30,
        renderer : function(value){
            if(!value||value == ""){
                value = Wtf.DEFAULT_USER_URL;
            }
            return String.format("<img src='{0}' style='height:18px;width:18px;vertical-align:text-top;'/>",value);
        }
    },{
        header: WtfGlobal.getLocaleText("acc.userAdmin.name"),  //"Name",
        dataIndex: 'fullname',
        autoWidth : true,
        sortable: true,
        groupable: true,
        renderer : function(value,p,record){
            return (record.data["fname"] + " " + record.data["lname"]);
        }
    },{
        header: WtfGlobal.getLocaleText("acc.userAdmin.userName"),  //"User Name",
        dataIndex: 'username',
        autoWidth : true,
        sortable: true,
        groupable: true,
        /**
         * If company is having self service = 1 then username will be hidden for such company 
         * ERP-41543
         */
        hidden: (Wtf.isSelfService == 1) ? true : false
    },{
        header: WtfGlobal.getLocaleText("acc.userAdmin.role"),  //"Role",
        dataIndex: 'rolename',
        autoWidth : true,
        sortable: true,
        groupable: true
    },{
        header :WtfGlobal.getLocaleText("acc.userAdmin.emailAddress"),  //'Email Address',
        dataIndex: 'emailid',
	    autoSize : true,
        sortable: true,
        renderer: WtfGlobal.renderEmailTo,
        groupable: true
    },{
        header :WtfGlobal.getLocaleText("acc.userAdmin.lastLogin"),  //'Last Login',
        dataIndex: 'lastlogin',
//        renderer:WtfGlobal.dateRenderer,
        autoSize : true,
        sortable: true,
        groupable: true
    },{
        header :WtfGlobal.getLocaleText("acc.userAdmin.Address"),  //'Address',
        dataIndex: 'address',
        autoSize : true,
        sortable: true,
        groupable: true
    },{
        header :WtfGlobal.getLocaleText("acc.user.usergroupbtn"),  //'Address',
        dataIndex: 'usergroup',
        autoSize : true,
        hidden: !Wtf.account.companyAccountPref.usersVisibilityFlow,
        sortable: true,
        groupable: true
    }]);

    this.usergrid = new Wtf.grid.GridPanel({
        stripeRows :true,
        layout:'fit',
        store: this.userds,
        id:'usergrid',
        cm: this.gridcm,
        sm : this.selectionModel,
        border : false,
      //  loadMask : true,
        viewConfig: {
            forceFit:true,
            emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        }
    });

    this.resetBttn=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
            tooltip :WtfGlobal.getLocaleText("acc.userAdmin.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
            id: 'btnRec' + this.id,
            scope: this,
            iconCls :getButtonIconCls(Wtf.etype.resetbutton),
            disabled :false
    });
    this.resetBttn.on('click',this.handleResetClick,this);
    
    this.UsergridPanel  = new Wtf.Panel({
        autoLoad : false,
        paging : false,
        layout : 'fit',
        items:[this.usergrid]
    });
    this.btnArr=[this.quickPanelSearch = new Wtf.KWLTagSearch({
                    emptyText:WtfGlobal.getLocaleText("acc.userAdmin.search"),  //'Search by Name',
                    width: 200,
                    field: 'username'
                }),
                this.resetBttn];
    this.enableBtnArrSingleSelect = new Array();
    this.btnArr.push('-');
   if (!WtfGlobal.EnableDisable(Wtf.UPerm.useradmin, Wtf.Perm.useradmin.assperm)) {
    this.assignPermission=new Wtf.Toolbar.Button ({
        text : WtfGlobal.getLocaleText("acc.userAdmin.assignPerm"),  //"Assign Permissions",
        id : "permissions"+this.id,
        allowDomMove:false,
        iconCls :getButtonIconCls(Wtf.etype.permission),
        scope : this,
        tooltip:{text:WtfGlobal.getLocaleText("acc.userAdmin.assignPermTTeText"),dtext:WtfGlobal.getLocaleText("acc.userAdmin.assignPermTTdText")},  //"Select a user and assign permissions for the user with respect to managing vendors, customer and other account settings.", etext:"Select a user and assign permissions for the user with respect to managing vendors, customer and other account settings."},
		disabled: true,
        handler : this.requestPermissions
   })
   
   this.setPrivilege = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.SetAmendingPricePermissions"),
            tooltip: WtfGlobal.getLocaleText("acc.field.SetAmendingPricePermissions"),
            disabled: true,
//            hidden:true,
            scope: this,
            handler: function() {
                    this.privWin = new Wtf.Window({
                        title: WtfGlobal.getLocaleText("acc.field.AmendPriceSetting"),
                        resizable: false,
                        width: 400,
                        height: 370,
                        modal: true,
                        layout: 'border',
                        scope: this,
                        buttons: [{
                            text: WtfGlobal.getLocaleText("acc.msgbox.ok"),
                            scope: this,
                            handler: function() {
                                this.saveAmendingPrice();
                            }
                        },{
                            text: WtfGlobal.getLocaleText("acc.msgbox.cancel"),
                            scope: this,
                            handler: function() {
                                this.privWin.close();
                            }
                        }],
                        items: [{
                            region: 'north',
                            height: 90,
                            border: false,
                            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                            html: getTopHtml(WtfGlobal.getLocaleText("acc.field.AmendPriceSetting"), WtfGlobal.getLocaleText("acc.field.Checkto")+" <b>"+WtfGlobal.getLocaleText("acc.field.Remove")+"</b> "+WtfGlobal.getLocaleText("acc.field.AmendPricePermissionforuser")+" <b>"+this.selectionModel.getSelected().data.fname + " "+ this.selectionModel.getSelected().data.lname +"</b>", "../../images/createuser.png")
                        },{
                            region: 'center',
                            layout: 'form',
                            border: false,
                            labelWidth: 270,
                            bodyStyle: 'background:#f1f1f1;padding:15px',
                            items: [     
                                
                             this.Blockamendingprice=new Wtf.form.Checkbox({
                            fieldLabel:WtfGlobal.getLocaleText("acc.field.blockamendingpriceformincost"),
                            name:"Blockamendingprice",
                            style:"margin-top: 3px;margin-left: -8px;",
                            id:"Blockamendingpriceid"
                      
                        }),{
                                    xtype:'fieldset',
                                    width:(Wtf.isIE)?'91%':'85%',
                                    height:'75%',
                                    title:WtfGlobal.getLocaleText("acc.field.Modules"),
                                    border : false,
                                    labelWidth:150,
                                    items: [{
                                        layout : 'column',
                                        border : false,
                                        items: [{
                                            columnWidth: '.50',
                                            layout : 'form',
                                            border : false,
                                            items : [this.CQCheck=new Wtf.form.Checkbox({
                                                            fieldLabel:WtfGlobal.getLocaleText("acc.dimension.module.12"),
                                                            name:'CQCheck',
                                                            style:'margin-top: 3px;',
                                                            id:'CQCheckid'
                                                     })]
                                     },{
                                            columnWidth: '.50',
                                            layout : 'form',
                                            border : false,
                                            items : [this.VQCheck=new Wtf.form.Checkbox({
                                                    fieldLabel:WtfGlobal.getLocaleText("acc.dimension.module.11"),
                                                    name:'VQCheck',
                                                    style:'margin-top: 5px;',
                                                    id:'VQCheckid'
                                                     })]
                                        }]              
                                    },{
                                        layout : 'column',
                                        border : false,
                                        items: [{
                                            columnWidth: '.50',
                                            layout : 'form',
                                            border : false,
                                            items : [this.SOCheck=new Wtf.form.Checkbox({
                                                        fieldLabel:WtfGlobal.getLocaleText("acc.dimension.module.9"),
                                                        name:'SOCheck',
                                                        style:'margin-top: 5px;',
                                                        id:'SOCheckid'
                                                })]
                                        },{
                                            columnWidth: '.50',
                                            layout : 'form',
                                            border : false,
                                            items : [ this.POCheck=new Wtf.form.Checkbox({
                                                        fieldLabel:WtfGlobal.getLocaleText("acc.dimension.module.10"),
                                                        name:'POCheck',
                                                        style:'margin-top: 5px;',
                                                        id:'POCheckid'
                                        })]
                                        }]              
                                    },{
                                        layout : 'column',
                                        border : false,
                                        items: [{
                                            columnWidth: '.50',
                                            layout : 'form',
                                            border : false,
                                            items : [this.CICheck=new Wtf.form.Checkbox({
                                                        fieldLabel:WtfGlobal.getLocaleText("acc.dimension.module.1"),
                                                        name:'CICheck',
                                                        style:'margin-top: 5px;',
                                                        id:'CICheckid'
                                        })]
                                        },{
                                            columnWidth: '.50',
                                            layout : 'form',
                                            border : false,
                                            items : [this.VICheck=new Wtf.form.Checkbox({
                                                        fieldLabel:WtfGlobal.getLocaleText("acc.dimension.module.2"),
                                                        name:'VICheck',
                                                        style:'margin-top: 5px;',
                                                        labelWidth: 200,
                                                        id:'VICheckid'
                                        })]
                                        }]              
                                    }
                                    ]
                                }]
                        }]
                    });
                   Wtf.Ajax.requestEx({
                        url:'ACCReports/GetUserAmendingPrice.do',
                        params:{
                            userid:this.selectionModel.getSelected().data.userid
                        }
                    },this,
                    function(req,res){
                        var restext=req;
                        if(restext.success){
                                this.CICheck.setValue(restext.data[0].CInvoice); 
                                this.VICheck.setValue(restext.data[0].VInvoice); 
                                this.SOCheck.setValue(restext.data[0].SalesOrder); 
                                this.POCheck.setValue(restext.data[0].PurchaseOrder); 
                                this.VQCheck.setValue(restext.data[0].VendorQuotation); 
                                this.CQCheck.setValue(restext.data[0].CustomerQuotation); 
                                this.Blockamendingprice.setValue(restext.data[0].BlockAmendingPrice); ///set block amend price
                        } 
                    },
                    function(req){
                       
                    }); 
                   this.privWin.show();
                } 
         });
   this.setPermissionsforFilteringReportsData = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.permissionsforFilteringReportsData"),
            tooltip: WtfGlobal.getLocaleText("acc.field.permissionsforFilteringReportsData"),
            disabled: true,
//            hidden:true,
            scope: this,
            handler: function() {
                    this.privWin = new Wtf.Window({
//                        title: WtfGlobal.getLocaleText("acc.field.permissionsforViewingReports"),
                        title: WtfGlobal.getLocaleText("acc.field.permissionsforFilteringReportsData"),
                        resizable: false,
                        width: 520,
                        height: 330,
                        modal: true,
                        layout: 'border',
                        scope: this,
                        buttons: [{
                            text: WtfGlobal.getLocaleText("acc.msgbox.ok"),
                            scope: this,
                            handler: function() {
                                this.savePermissionsforFilteringReportsData();
                            }
                        },{
                            text: WtfGlobal.getLocaleText("acc.msgbox.cancel"),
                            scope: this,
                            handler: function() {
                                this.privWin.close();
                            }
                        }],
                        items: [{
                            region: 'north',
                            height: 110,
                            border: false,
                            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                            html: getTopHtml(WtfGlobal.getLocaleText("acc.field.permissionsforFilteringReportsData"),"<ul style='list-style-type:disc;padding-left:15px;'><li>"+(WtfGlobal.getLocaleText("acc.rem.permissionviewtext1"))+"</li><li>"+WtfGlobal.getLocaleText("acc.rem.permissionviewtext2")+"</li></ul>", "../../images/createuser.png")
                        },{
                            region: 'center',
                            layout: 'form',
                            border: false,
                            labelWidth: 70,
                            bodyStyle: 'background:#f1f1f1;padding:15px',
                            items: [{
                                    xtype:'fieldset',
                                    width:(Wtf.isIE)?'91%':'85%',
                                    height:'75%',
                                    title:WtfGlobal.getLocaleText("acc.field.Modules"),
                                    border : false,
                                    labelWidth:150,
                                    items: [{
                                        layout : 'column',
                                        border : false,
                                        items: [{
                                            columnWidth: '.50',
                                            layout : 'form',
                                            border : false,
                                            items : [this.customerInvoiceCheck=new Wtf.form.Checkbox({
                                                        fieldLabel:WtfGlobal.getLocaleText("acc.dimension.module.1"),
                                                        name:'customerInvoiceCheck',
                                                        style:'margin-top: 5px;',
                                                        id:'customerInvoiceCheckId',
                                                        disabled:true
                                        })]
                                        },{
                                            columnWidth: '.50',
                                            layout : 'form',
                                            border : false,
                                            items : [this.salesOrderCheck=new Wtf.form.Checkbox({
                                                    fieldLabel:WtfGlobal.getLocaleText("acc.dimension.module.9"),
                                                    name:'salesOrderCheck',
                                                    style:'margin-top: 5px;',
                                                    id:'salesOrderCheckId',
                                                    disabled:true
                                                     })]
                                        }]              
                                    },{
                                        layout : 'column',
                                        border : false,
                                        items: [{
                                            columnWidth: '.50',
                                            layout : 'form',
                                            border : false,
                                            items : [this.customerQuotationCheck=new Wtf.form.Checkbox({
                                                        fieldLabel:WtfGlobal.getLocaleText("acc.dimension.module.12"),
                                                        name:'customerQuotationCheck',
                                                        style:'margin-top: 5px;',
                                                        id:'customerQuotationCheckId',
                                                        disabled:true
                                                })]
                                        },{
                                         columnWidth: '.50',
                                            layout : 'form',
                                            border : false,
                                            items : [this.deliveryOrderCheck=new Wtf.form.Checkbox({
                                                        fieldLabel:WtfGlobal.getLocaleText("acc.dimension.module.13"),
                                                        name:'deliveryOrderCheck',
                                                        style:'margin-top: 5px;',
                                                        id:'deliveryOrderCheckId',
                                                        disabled:true
                                                })]   
                                        }]              
                                    }]
                                }]
                        }]
                    });
                   Wtf.Ajax.requestEx({
                        url:'ACCReports/getPermissionsforFilteringReportsData.do',
                        params:{
                            userid:this.selectionModel.getSelected().data.userid
                        }
                    },this,
                        function(req,res){
                            var restext=req;
                            if(restext.success){
                                this.customerInvoiceCheck.setValue(restext.data[0].customerInvoiceCheck); 
                                this.salesOrderCheck.setValue(restext.data[0].salesOrderCheck); 
                                this.customerQuotationCheck.setValue(restext.data[0].customerQuotationCheck); 
                                this.deliveryOrderCheck.setValue(restext.data[0].deliveryOrderCheck);
                                /**
                                 * We are enabling checkboxes in two cases.
                                 * 1. If Company Creator is logged then we enable Permission to View records of all users to give permissions to admin,user,etc.
                                 * 2. If Company Administrator is logged but he is not Company creator then we restrict that user(admin) to give permissions only to Company User,salesperson,etc 
                                 *    but he is not able to give permission to another admin. 
                                 */
                                if(Wtf.account.companyAccountPref.isCompanyCreatorLogged || 
                                    (Wtf.UserReporRole.URole.roleid == Wtf.ADMIN_ROLE_ID && !Wtf.account.companyAccountPref.isCompanyCreatorLogged && this.selectionModel.getSelected().data.roleid != Wtf.ADMIN_ROLE_ID)){//true : If Company Creator logged
                                    this.customerInvoiceCheck.enable();
                                    this.salesOrderCheck.enable();
                                    this.customerQuotationCheck.enable();
                                    this.deliveryOrderCheck.enable();
                                }
                            } else{
                                this.customerInvoiceCheck.setValue(true); //ERP-10873
                                this.salesOrderCheck.setValue(true); 
                                this.customerQuotationCheck.setValue(true); 
                                 this.deliveryOrderCheck.setValue(true);
                                if(Wtf.account.companyAccountPref.isCompanyCreatorLogged || 
                                    (Wtf.UserReporRole.URole.roleid == Wtf.ADMIN_ROLE_ID && !Wtf.account.companyAccountPref.isCompanyCreatorLogged && this.selectionModel.getSelected().data.roleid != Wtf.ADMIN_ROLE_ID)){//true : If Company Creator logged
                                    this.customerInvoiceCheck.enable();
                                    this.salesOrderCheck.enable();
                                    this.customerQuotationCheck.enable();
                                    this.deliveryOrderCheck.enable();
                                }   
                            }
                    },
                    function(req){                      
                    }); 
                   this.privWin.show();
                } 
         });
         
        this.setUserActiveDays = new Wtf.Toolbar.Button ({
            text : WtfGlobal.getLocaleText("acc.field.setUserActiveDays"),  // "Assign Active Days",
            tooltip: WtfGlobal.getLocaleText("acc.field.setUserActiveDays"),  // "Assign Active Days",
            id : "setActiveDays"+this.id,
            allowDomMove:false,
            iconCls :getButtonIconCls(Wtf.etype.permission),
            scope : this,            
            disabled: true,
            handler : this.handleSetActiveDaysForUser
        });
        this.setUserDepartment = new Wtf.Toolbar.Button ({
            text : WtfGlobal.getLocaleText("acc.field.assignuserDepartment"), //'Assign User Departrment', 
            tooltip:WtfGlobal.getLocaleText("acc.field.assignuserDepartment"), 
            id : "setUserDepartment"+this.id,
            allowDomMove:false,
            iconCls :getButtonIconCls(Wtf.etype.permission),
            scope : this,            
            disabled: true,
            handler : this.handleSetDepartmentForUser,
            hidden:!Wtf.account.companyAccountPref.activatebudgetingforPR
        });
        /**
         * Button show if Users visibility is Enable 
         */
        this.userGroup = new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.user.usergroupbtn"), // "Assign Active Days",
            tooltip: WtfGlobal.getLocaleText("acc.user.usergroupbtn"), // "Assign Active Days",
            id: "userGroup" + this.id,
            allowDomMove: false,
            iconCls: getButtonIconCls(Wtf.etype.permission),
            scope: this,
            hidden: !Wtf.account.companyAccountPref.usersVisibilityFlow,
            handler: this.showUsersGroupWin
        });   
    this.assignRolePermission = new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.rolemanagement.RoleManagement"), //WtfGlobal.getLocaleText("acc.userAdmin.assignPerm"), //"Assign Role Permissions",
        id: "AssignRole" + this.id,
        allowDomMove: false,
        iconCls: getButtonIconCls(Wtf.etype.permission),
        scope: this,
        tooltip: {text: 'Assign Role Permission'}, //"Select a user and assign permissions for the user with respect to managing vendors, customer and other account settings.", etext:"Select a user and assign permissions for the user with respect to managing vendors, customer and other account settings."},
       // disabled: true,
        handler: this.requestrolePermissions
    });
   
        this.btnArr.push(this.assignPermission);

    this.btnArr.push(this.getUserManagementBtns());
    this.btnArr.push(this.setPrivilege);
    this.btnArr.push(this.setPermissionsforFilteringReportsData);
    this.btnArr.push(this.setUserActiveDays);
    this.btnArr.push(this.setUserDepartment);
//    if (Wtf.account.companyAccountPref.usersVisibilityFlow) {
        
//    }
    this.enableBtnArrSingleSelect.push(this.btnArr.length-6);
    this.enableBtnArrSingleSelect.push(this.btnArr.length-4);
    this.enableBtnArrSingleSelect.push(this.btnArr.length-3); // Enable/Disable assignPermission Button as single row select
    this.enableBtnArrSingleSelect.push(this.btnArr.length-2); // Enable/Disable assignPermission Button as single row select
    this.enableBtnArrSingleSelect.push(this.btnArr.length-1);
    this.enableBtnArrSingleSelect.push(this.btnArr.length);
    /**
     * ERP-41685
     * isSelfService check used for Hide Role Management button from User Administration.
     * If a company created from Deskera-Cloud-Service.
     */
    if (Wtf.isSelfService != 1) {
        this.btnArr.push(this.assignRolePermission);
    }
    this.btnArr.push(this.userGroup);
}
    //   this.btnArr.push('-');

    this.innerpanel = new Wtf.Panel({
        layout : 'fit',
        cls : 'backcolor',
        border : false,
        items:[this.UsergridPanel ],
        // id : 'innerpanel'+this.id,
        tbar:this.btnArr,
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            //id: "pagingtoolbar" + this.id,
            store: this.userds,
            searchField: this.quickPanelSearch,
            displayInfo: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({id : "pPageSize_"+this.id})
        })
    });

    Wtf.apply(this,{
        layout : "fit",
        defaults:{border:false,bodyStyle:"background: transparent;"},
      //  loadMask:true,
        autoScroll:true,
        items:[this.innerpanel]
    });
this.usergrid.getSelectionModel().on('selectionchange',this.SelChange,this);
    Wtf.common.UserGrid.superclass.constructor.call(this,config);
},

Wtf.extend(Wtf.common.UserGrid,Wtf.Panel,{
     saveAmendingPrice:function(){
        Wtf.Ajax.request({
                method: 'POST',
                url:'ACCReports/AssignAmendingPrice.do',
                scope: this,
                params: {
                    CInvoice: this.CICheck.getValue(),
                    VInvoice:this.VICheck.getValue(),
                    SalesOrder: this.SOCheck.getValue(),
                    PurchaseOrder:this.POCheck.getValue(),
                    VendorQuotation:this.VQCheck.getValue(),   
                    CustomerQuotation: this.CQCheck.getValue(),
                    BlockAmendingPrice:this.Blockamendingprice.getValue(),// save block amending price
                    userid:this.selectionModel.getSelected().data.userid
                },
                success: function() {
                    this.privWin.close();
                   
                }
            });
            },
   showUsersGroupWin: function() {
      new Wtf.UserGroup({
          }).show();
    },
     savePermissionsforFilteringReportsData:function(){
        Wtf.Ajax.request({
                method: 'POST',
                url:'ACCReports/savePermissionsforFilteringReportsData.do',
                scope: this,
                params: {
                    customerInvoiceCheck: this.customerInvoiceCheck.getValue(),   
                    salesOrderCheck: this.salesOrderCheck.getValue(),   
                    customerQuotationCheck: this.customerQuotationCheck.getValue(),   
                    deliveryOrderCheck: this.deliveryOrderCheck.getValue(),   
                    userid:this.selectionModel.getSelected().data.userid
                },
                success: function() {
                    this.privWin.close();                   
                }
            });
     },
    
     SelChange:function(){
         WtfGlobal.enableDisableBtnArr(this.btnArr, this.usergrid, this.enableBtnArrSingleSelect, []);
         WtfGlobal.enableDisableBtnArr(this.menubtns, this.usergrid, this.enableBtnArrSingleSelect1, []);
     },
    handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.userds.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value
                }
            });
             WtfComMsgBox(29,4,true);
        }
    },
    storeloaded:function(store){
        Wtf.MessageBox.hide();
        this.quickPanelSearch.StorageChanged(store);
        if(this.usergrid.getStore().getCount()==0){
            this.usergrid.getView().emptyText=WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"));
            this.usergrid.getView().refresh();
        }
    },
    requestPermissions:function(){
       var record = this.usergrid.getSelectionModel().getSelected();
       var userFullName = record.data['fname'] + " " + record.data['lname'];
       var admincount=0;
       for(var i=0;i<this.usergrid.getStore().getCount();i++){
           var rec=this.usergrid.getStore().getAt(i);
           if(rec.data.roleid=="1"){
              admincount++; 
           }
          }
       var permWindow=new Wtf.common.Permissions({
            title:WtfGlobal.getLocaleText("acc.userAdmin.role&perm"),  //"Roles & Permissions",
            resizable: false,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            roleid:record.data['roleid'],
            userid:record.data['userid'],
            userFullName : userFullName,
            rolepermissionflag:false,
            modal:true,
            isadminenabledisable:admincount>1?false:true
        });
         Wtf.getCmp('AP').on('update',function(){(function(){
            this.usergrid.getStore().reload();
            }).defer(WtfGlobal.gridReloadDelay(),this)},this);
        permWindow.show();
    },
      requestrolePermissions: function() {
        //var record = this.rolemanagement.getSelectionModel().getSelected(); 
        this.roleRecord = new Wtf.data.Record.create([
            {name: 'roleid'},
            {name: 'rolename'},
            {name:'desc'}
        ]);
        this.roleReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        }, this.roleRecord);    
        this.RoleStore = new Wtf.data.Store({
            url:"ACCCommon/getRoleList.do",
            reader:this.roleReader
        });
        this.RoleStore.on("beforeload", function(){
            this.RoleStore.baseParams = {
                ss:this.quickPanelSearch.getValue()
            }
        }, this);
        this.RoleStore.load();
         this.RoleStore.on('datachanged', function() {
           if (this.pP.combo) {
              var p = this.pP.combo.value;
             this.quickPanelSearch.setPage(p);
               }
            }, this); 
      this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText:WtfGlobal.getLocaleText("acc.rem.5")+" "+WtfGlobal.getLocaleText("acc.common.role"),
        width: 150,
        id:"quickSearch",
        field: 'roleid',
        Store:this.RoleStore
     })
      this.resetBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.reset"),  //'Reset',
        tooltip :WtfGlobal.getLocaleText("acc.common.resetTT"),  //'Allows you to add a new search term by clearing existing search terms.',
        id: 'btnRec' + this.id,
        scope: this,
        //hidden:this.isSalesCommissionStmt,
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        disabled :false,
        handler: this.handleResetClick1
    
    });
    this.editBttn=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.common.edit"),  //'Edit',
                tooltip :WtfGlobal.getLocaleText("acc.common.edit"),
                id: 'btnEdit' + this.id,
                scope: this,
                disabled:true,
                iconCls :getButtonIconCls(Wtf.etype.edit),
                handler:this.addpermissionwindow.createDelegate(this,[true])
   })
           this.addbtn=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.common.add"),  //'Edit',
                tooltip :WtfGlobal.getLocaleText("acc.common.add"),
                id: 'btnEdit' + this.id,
                scope: this,
                iconCls :getButtonIconCls(Wtf.etype.add),
                handler:this.addpermissionwindow.createDelegate(this,[false])
        })
           this.deletebtn=new Wtf.Toolbar.Button({
                text:WtfGlobal.getLocaleText("acc.rem.7"),  //'Edit',
                tooltip :WtfGlobal.getLocaleText("acc.rem.7"),
                id: 'btndelete' + this.id,
                scope: this,
                disabled:true,
                iconCls :getButtonIconCls(Wtf.etype.menudelete),
                handler:this.deleteRole
        })
      this.tbar1 = new Array();
                this.bBarBtnArr = new Array();
           
                var extraConfig = {};
                extraConfig.url = "ACCCommon/importPermissionsRoleWise.do";
                var extraParams = "";
                this.importBtnArray = Wtf.importMenuArray(this, "Role Management", this.RoleStore, extraParams, extraConfig);
                this.importButton = Wtf.importMenuButtonA(this.importBtnArray, this, "Role Management");

                this.bBarBtnArr.push(this.importButton);

                this.tbar1.push(this.quickPanelSearch, this.resetBttn, this.addbtn, this.editBttn, this.deletebtn);
           
        this.selectionModelrole = new Wtf.grid.CheckboxSelectionModel({
        singleSelect :false                          //this.isRequisition ? false : true,
        });
        this.gridColumnModelrole=[];
        this.gridColumnModelrole.push(this.selectionModelrole,
            {
             header:WtfGlobal.getLocaleText("acc.common.role"),
             dataIndex: 'rolename',
             width: 50   
            
           },{
            header: WtfGlobal.getLocaleText("acc.taskProgressGrid.materialConsumed.header2"),
            dataIndex: 'desc',
            width: 100,
            renderer: function(value) {
            value = value.replace(/\'/g, "&#39;");
            value = value.replace(/\"/g, "&#34");
            return "<span class=memo_custom  wtf:qtip='" + value + "'>" + Wtf.util.Format.ellipsis(value, 60) + "</span>"
        }
        });
       var panel = Wtf.getCmp("roleuser");
       if(panel==null){
        this.rolemanagement = new Wtf.grid.GridPanel({
        store:this.RoleStore,
        sm:this.selectionModelrole,
        layout:'fit',
        id:'roleuser',
        closable : true,
        title:WtfGlobal.getLocaleText("acc.rolemanagement.RoleManagement"),
        tabTip:WtfGlobal.getLocaleText("acc.rolemanagement.RoleManagement"),
        cm:new Wtf.grid.ColumnModel(this.gridColumnModelrole),
        //plugins:this.expanderforlink,
        iconCls :'accountingbase debitnotereport',
        tbar: this.tbar1,
        scroll: true,
        bbar: this.pagingToolbar = new Wtf.PagingSearchToolbar({
            pageSize: 30,
            //id: "pagingtoolbar" + this.id,
            store: this.RoleStore,
            searchField: this.quickPanelSearch,
            displayInfo: true,
//            displayMsg: 'Displaying records {0} - {1} of {2}',
            emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
            plugins: this.pP = new Wtf.common.pPageSize({id : "pPageSize_"+this.id}),
            items: this.bBarBtnArr
        }),
        viewConfig:{
            forceFit: true,
             emptyText: '<div class="emptyGridText">' + WtfGlobal.getLocaleText('account.common.nodatadisplay') + ' <br></div>'
       }
        });
    }
    Wtf.getCmp('as').add(this.rolemanagement);
    Wtf.getCmp('as').setActiveTab(this.rolemanagement);
    Wtf.getCmp('as').doLayout();
   this.rolemanagement.getSelectionModel().on('selectionchange',function(){
       if(this.rolemanagement.getSelectionModel().getCount()==1){
       this.deletebtn.setDisabled(false);
       this.editBttn.setDisabled(false);
   }
   else{
        this.deletebtn.setDisabled(true);
       this.editBttn.setDisabled(true);
   }
   },this);
   },
     addpermissionwindow:function(isEdit){
          var record = this.rolemanagement.getSelectionModel().getSelected();
            var permWindow=new Wtf.common.Permissions({
            title:WtfGlobal.getLocaleText("acc.userAdmin.role&perm"),  //"Roles & Permissions",
            resizable: false,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
            rolepermissionflag:true,
            record:record,
            isEdit:isEdit,
            //roleid:record.data['roleid'],
            //userid:record.data['userid'],
            //userFullName : userFullName,
            modal:true
        });
         permWindow.on('update',function(){(function(){
            this.rolemanagement.getStore().reload();
            }).defer(WtfGlobal.gridReloadDelay(),this)},this);
        permWindow.show(); 
     },
     deleteRole:function(){
        var rec=this.rolemanagement.getSelectionModel().getSelected();
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.field.Areyousure?"),function(btn){
            if(btn!="yes") return;
            Wtf.Ajax.requestEx({
//                url: Wtf.req.base+'UserManager.jsp',
                url : "ACCCommon/deleteRole.do",
                params: {
                    mode:10,
                    roleid:rec.data.roleid,
                    rolename:rec.data.rolename
                }
            },this,this.genRoleSuccessResponse,this.genRoleFailureResponse);
        },this);
    },
     genRoleSuccessResponse:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.userAdmin.setPerm"),response.msg],response.success*2+1);
            this.RoleStore.reload();
    },

    genRoleFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    handleResetClick1:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.RoleStore.load({
                params: {
                    start:0,
                    limit:this.pP.combo.value
                }
            });
             //WtfComMsgBox(29,4,true);
        }
    },
    handleSetDepartmentForUser : function()
    {
         var record = this.usergrid.getSelectionModel().getSelected();      
        this.userRecord = new Wtf.data.Record.create([
            {name: 'userid'},
            {name: 'fullname'}
        ]);
        this.userReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        }, this.userRecord);    
        this.userStore = new Wtf.data.Store({
            url:"ProfileHandler/getAllUserDetails.do",
            reader:this.userReader
        });
        this.userStore.load();       
        this.userStore.on("load", function(store,accRec) {
         if (record) {
                this.users.setValue(record.data.userid);   
                this.department.setValue(record.data.department); 
            }
        },this);
           this.users = new Wtf.common.Select({
            labelStyle:'width:150px;margin-left: 5px;',
            fieldLabel:WtfGlobal.getLocaleText("acc.UserAdministrator.User")+'*',
            multiSelect:true,
            forceSelection:true,
            name:'users',
            xtype:'select',
            hiddenName:"users",
            valueField:'userid',
            displayField:'fullname',
            store:this.userStore,
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectUser"),
            mode: 'local',
            triggerAction:'all',
            typeAhead: true,
            scope:this,
            allowBlank:false,
            width : 250
        });       
        this.users.on('select',function(combo,userRec,index) {  
            if (userRec.get('userid') == 'All') {   
                combo.clearValue();
                combo.setValue('All');
            } else if (combo.getValue().indexOf('All') >= 0) {  
                combo.clearValue();
                combo.setValue(userRec.get('userid'));               
            }
        }, this);
        Wtf.departmentStore.load();
        Wtf.departmentStore.on("load", function(store,accRec) {                    
            if (record) {
              this.department.setValue(record.data.department);               
            }
        },this);
       
        this.department= new Wtf.form.FnComboBox({
            triggerAction:'all',
            labelStyle:'width:150px;margin-left: 5px;',
            mode: 'local',
            valueField:'id',
            displayField:'name',         
            store:Wtf.departmentStore,
            addNoneRecord: true,
            width : 250,
            forceSelection: true,
            fieldLabel:WtfGlobal.getLocaleText("acc.field.department"),//'Department',
            emptyText:'None',          
            name:'department',
            hiddenName:'department'             
        });
        this.isApprover=new Wtf.form.Checkbox({
           fieldLabel:WtfGlobal.getLocaleText("acc.field.isApprover"),
           name:'isApproverCheck',
           labelStyle:'width:150px;margin-left: 5px;',
           id:'isApproverCheckId',
           disabled:true,
           checked:record.data.isApprover
         });
           
            this.setUserDepartmentWin = new Wtf.Window({
            title: WtfGlobal.getLocaleText("acc.field.assignuserDepartment"),
            resizable: false,
            width: 480,
            height: 200,
            modal: true,
            layout: 'border',
            scope: this,
            buttons: [{
                    text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                    scope: this,
                    handler: function() {
                         this.saveUserDepartment();
                    }
                },{
                    text: WtfGlobal.getLocaleText("acc.msgbox.cancel"),
                    scope: this,
                    handler: function() {
                        this.setUserDepartmentWin.close();
                    }
                }],
            items: [
                   {
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    bodyStyle: 'background:#f1f1f1;padding:15px',
                    items: [{
                            xtype:"form",
                            border:false,
                            id:this.id+"userdepartmentfrm",
                            autoScroll:true,
                            labelWidth:150,
                            items:[this.users,this.department,this.isApprover]
                    }]
                }]
        });  
        this.setUserDepartmentWin.show();
    },
    handleSetActiveDaysForUser : function() {
        var record = this.usergrid.getSelectionModel().getSelected();
        
        this.userRecord = new Wtf.data.Record.create([
            {name: 'userid'},
            {name: 'fullname'}
        ]);
  
        this.userReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        }, this.userRecord);
    
        this.userStore = new Wtf.data.Store({
            url:"ProfileHandler/getAllUserDetails.do",
            reader:this.userReader
        });
        this.userStore.load();
        
        this.userStore.on("load", function(store,accRec) {
            var storeNewRecord = new Wtf.data.Record({
                userid:'All',
                fullname:'All'
            });
            this.userStore.insert( 0,storeNewRecord);
            
            if (record) {
                this.users.setValue(record.data.userid);
            }
        },this);
        
        this.users = new Wtf.common.Select({
            labelStyle:'width:150px;margin-left: 5px;',
            fieldLabel:WtfGlobal.getLocaleText("acc.UserAdministrator.User")+'*',
            multiSelect:true,
            forceSelection:true,
            name:'users',
            xtype:'select',
            hiddenName:"users",
            valueField:'userid',
            displayField:'fullname',
            store:this.userStore,
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectUser"),
            mode: 'local',
            triggerAction:'all',
            typeAhead: true,
            scope:this,
            allowBlank:false,
            width : 260
        });
        
        this.users.on('select',function(combo,userRec,index) { // multiselection in case of all 
            if (userRec.get('userid') == 'All') {  // case of multiple record after all
                combo.clearValue();
                combo.setValue('All');
            } else if (combo.getValue().indexOf('All') >= 0) {  // case of all after record
                combo.clearValue();
                combo.setValue(userRec.get('userid'));
            }
        }, this);
        
        this.moduleStore=new Wtf.data.SimpleStore({
            fields:[
                {name:"moduleid"},
                {name:"name"}
            ],
            data:[
                [Wtf.Acc_Vendor_Invoice_ModuleId,"Vendor Invoice"],
                [Wtf.Acc_Invoice_ModuleId,"Customer Invoice"],
//                [Wtf.Acc_GENERAL_LEDGER_ModuleId,"Journal Entry"],
                [Wtf.Acc_Sales_Order_ModuleId,"Sales Order"],
                [Wtf.Acc_Make_Payment_ModuleId,"Make Payment"],
                [Wtf.Acc_Receive_Payment_ModuleId,"Receive Payment"],
                [Wtf.Acc_Sales_Return_ModuleId,"Sales Return"],
                [Wtf.Acc_Purchase_Order_ModuleId,"Purchase Order"],
                [Wtf.Acc_Delivery_Order_ModuleId,"Delivery Order"],
//                [Wtf.Acc_Goods_Receipt_ModuleId,"Goods Receipt Order"],

//                [Wtf.Acc_Purchase_Return_ModuleId,"Purchase Return"],
//                [Wtf.Acc_Customer_ModuleId,"Customer"],
//                [Wtf.Acc_Vendor_ModuleId,"Vendor"],
//                [Wtf.Acc_Contract_ModuleId,"Contract"],
                [Wtf.Acc_InterStore_ModuleId,"Inter Store Transfer"],
                [Wtf.Acc_InterLocation_ModuleId,"Inter Location Transfer"],
            ]
        });
        
        this.moduleType = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.designerTemplate.ModuleName") + '*',
            labelStyle:'width:120px;margin-left: 5px;',
            name:'modules',
            hiddenName:'modules',
            store:this.moduleStore,
            valueField:'moduleid',
            displayField:'name',
            mode: 'local',
            disableKeyFilter:true,
            allowBlank:false,
            triggerAction:'all',
            forceSelection:true,
            typeAhead: true,
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectaModule")
        });
        
        this.days = new Wtf.form.NumberField({
            fieldLabel: WtfGlobal.getLocaleText("acc.masterConfig.days") + '*',
            name:'days',
            allowNegative:false,
            allowDecimals:false,
            labelStyle:'width:150px;margin-left: 5px;',
            allowBlank:false,
            minValue : 0,
            maxValue : 365,
            disabled :false,
            emptyText :WtfGlobal.getLocaleText("acc.field.EnterDays"),
            width : 260
        });
        
        this.deleteRec = new Wtf.data.Record.create([
            {name: 'rowid'},
            {name: 'moduleid'},
            {name: 'days'}
        ]);
        
        this.deleteStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.deleteRec)                
        });
        
        this.userActiveDaysGridStoreRec = new Wtf.data.Record.create([
            {name: 'rowid'},
            {name: 'moduleid'},
            {name: 'days'}
        ]);
        
        this.userActiveDaysGridStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.userActiveDaysGridStoreRec),
            url : "ACCCompanyPref/getUserActiveDaysDetails.do",
            baseParams: {
                userID : record.data.userid
            }
        });
        
        this.userActiveDaysGridStore.load();
        
        this.userActiveDaysGridcm = new Wtf.grid.ColumnModel([{
                header:WtfGlobal.getLocaleText("acc.importLog.module"),  // "Module",
                dataIndex:'moduleid',
                align:'left',
                autoWidth : true,
                renderer:Wtf.comboBoxRenderer(this.moduleType),
                editor:this.moduleType
            },{
                header:WtfGlobal.getLocaleText("acc.field.ActiveDays"),  // "Active Days",
                dataIndex:'days',
                align:'right',
                autoWidth : true,
                editor:this.days
            },{
                header: WtfGlobal.getLocaleText("acc.masterConfig.costCenter.action"),  // 'Action',
                renderer: this.deleteRenderer.createDelegate(this),
                width:50
            }
        ]);
        
        this.userActiveDaysGrid = new Wtf.grid.EditorGridPanel({
            id:'userActiveDaysGrid'+this.id,
            layout:'fit',
            store: this.userActiveDaysGridStore,
            cm: this.userActiveDaysGridcm,
            clicksToEdit:1,
            autoScroll:true,
            height:150,
            width:450,
//            plugins:[this.checkColumn],
            border : false,
            loadMask : true,
            cls:'vline-on',
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            }
        });
        
        
        this.setUserActiveDaysWin = new Wtf.Window({
            title: WtfGlobal.getLocaleText("acc.field.setUserActiveDays"),  // "Assign Active Days",
            resizable: false,
            width: 550,
            height: 400,
            modal: true,
            layout: 'border',
            scope: this,
            buttons: [{
                    text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                    scope: this,
                    handler: function() {
                        this.saveUserAciveDays();
                    }
                },{
                    text: WtfGlobal.getLocaleText("acc.msgbox.cancel"),
                    scope: this,
                    handler: function() {
                        this.setUserActiveDaysWin.close();
                    }
                }],
            items: [{
                    region: 'north',
                    height: 100,
                    border: false,
                    bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                    html: getTopHtml(WtfGlobal.getLocaleText("acc.field.setUserActiveDays"),WtfGlobal.getLocaleText("acc.field.setUserActiveDaysWinDesc"), "../../images/createuser.png")
                },{
                    region: 'center',
                    layout: 'fit',
                    border: false,
                    bodyStyle: 'background:#f1f1f1;padding:15px',
                    items: [{
                            xtype:"form",
                            border:false,
                            id:this.id+"useractivedaysfrm",
                            autoScroll:true,
                            labelWidth:150,
                            items:[this.users, {
                                    xtype:'fieldset',
                                    width:(Wtf.isIE)?'91%':'85%',
                                    height:'75%',
                                    title:WtfGlobal.getLocaleText("acc.field.selectModulesAndActiveDays"),
                                    border : false,
                                    labelWidth:150,
                                    items: [this.userActiveDaysGrid]
                            }]
                    }]
                }]
        });
        
        this.userActiveDaysGrid.on('afteredit',this.updateRow,this);
        this.userActiveDaysGrid.on('rowclick',this.handleRowClick,this);
        this.userActiveDaysGrid.on('validateedit',this.validateRow,this);
        this.userActiveDaysGrid.getStore().on('load',this.addBlankRow,this);
        
        this.setUserActiveDaysWin.show();
    },
    
    deleteRenderer : function(v,m,rec) {
        var flag = true;
        var cm = this.userActiveDaysGrid.getColumnModel();
        var count = cm.getColumnCount();
        for (var i=0; i < count-1; i++) {
            if (rec.data[cm.getDataIndex(i)].length <= 0) {
                if (i == 1) {
                    flag = true;                
                } else {
                    flag = false;
                    break;
                }
            }
        }
        
        if (flag) {
            var deletegriclass = getButtonIconCls(Wtf.etype.deletegridrow);
            return "<div class='" + deletegriclass + "'></div>";
        }
        return "";
    },
    
    addBlankRow:function(){
        var Record = this.userActiveDaysGridStore.reader.recordType, f = Record.prototype.fields, fi = f.items, fl = f.length;
        var values = {},blankObj={};
        for (var j = 0; j < fl; j++) {
            f = fi[j];
            if(f.name!='rowid') {
                blankObj[f.name]='';
                if(!Wtf.isEmpty(f.defValue))
                    blankObj[f.name]=f.convert((typeof f.defValue == "function"? f.defValue.call() : f.defValue));
            }
        }
        var newrec = new Record(blankObj);
        this.userActiveDaysGridStore.add(newrec);
    },
    
    updateRow : function(obj) {
        if (this.userActiveDaysGridStore.getCount() > 0 && this.userActiveDaysGridStore.getAt(this.userActiveDaysGridStore.getCount()-1).data['moduleid'].length <= 0) {
            return;
        }
        this.addBlankRow();
    },
    
    handleRowClick : function(grid,rowindex,e) {
        if (e.getTarget(".delete-gridrow")) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.nee.48"), function(btn) {
                if (btn != "yes") {
                    return;
                }
                var store = grid.getStore();
                var total = store.getCount();
                var record = store.getAt(rowindex);
                
                var moduleid = record.data.moduleid;
                moduleid = (moduleid == "NaN" || moduleid == undefined || moduleid == null)? 0 : moduleid;
                
                if (record.data.moduleid != undefined) {
                    var deletedData = [];
                    var newRec = new this.deleteRec({
                        rowid:record.data.rowid,
                        moduleid:record.data.moduleid,
                        days:record.data.days
                    });
                    deletedData.push(newRec);
                    this.deleteStore.add(deletedData);
                }
                store.remove(store.getAt(rowindex));
                if (rowindex == total-1) {
                    this.addBlankRow();
                }
            }, this);
        }
    },
    
    validateRow : function(obj) {
        if (obj!=null) {
            if (obj.field=='moduleid') {
                var ismoduleIDAlreadySelected = false;
                
                this.userActiveDaysGrid.getStore().each(function(recr) {
                    var moduleid = recr.get('moduleid');
                    if (moduleid == obj.value) {
                        ismoduleIDAlreadySelected = true;
                        return false;
                    }
                },this);
                
                if (ismoduleIDAlreadySelected) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.moduleNameAlreadySelected")],0); // "Module name already selected."
                    obj.record.set('moduleid',obj.originalValue);
                    return false;
                }
            }
        }
    },
    saveUserDepartment : function() {
        var flag1 = this.formPanel=Wtf.getCmp(this.id+"userdepartmentfrm").form.isValid();      
        if (!flag1) {
            WtfComMsgBox(2,2);
            return;
        }      
        var rec = Wtf.getCmp(this.id+"userdepartmentfrm").form.getValues();             
        Wtf.Ajax.requestEx({
            url:"ProfileHandler/setUserDepartment.do",
            params: rec
        },this,this.successResponse1,this.failureResponse1);
    },   
    
    saveUserAciveDays : function() {
        var flag1 = this.formPanel=Wtf.getCmp(this.id+"useractivedaysfrm").form.isValid();
        var detail = this.getModuleAndActiveDaysDetails();
        if (detail == undefined || detail == "[]") {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.modulesAndActiveDaysDetailsAreNotValid")],2); // "Modules And Active Days details are not valid."
            return;
        }
        
        for (var i=0; i < this.userActiveDaysGrid.getStore().getCount()-1; i++) { // excluding last row
            var moduleid = this.userActiveDaysGrid.getStore().getAt(i).data['moduleid'];
            if (moduleid === '' || moduleid == undefined) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Pleaseselectmoduleforadding")], 2);
                return;
            }
            
            var days = this.userActiveDaysGrid.getStore().getAt(i).data['days'];
            if (days === '' || days == undefined) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.pleaseSelectActiveDays")], 2);
                return;
            }
        }
        
        if (!flag1) {
            WtfComMsgBox(2,2);
            return;
        }
        
        var rec = Wtf.getCmp(this.id+"useractivedaysfrm").form.getValues();
        rec.detail = detail;
        
        Wtf.Ajax.requestEx({
            url:"ACCCompanyPref/setUserActiveDays.do",
            params: rec
        },this,this.successResponse,this.failureResponse);
    },
    
    successResponse : function(response) {
        this.setUserActiveDaysWin.close();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.field.userActiveDaysSavedSuccessfully")],response.success*2+1);
    },
    
    failureResponse : function(response) {
    	WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"),WtfGlobal.getLocaleText("acc.common.msg1")],response.success*2+1);
    },
    successResponse1 : function(response) {
       this.setUserDepartmentWin.close();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.msg.userdepartmentassigned")],response.success*2+1);
    },
    
    failureResponse1: function(response) {
    	WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"),WtfGlobal.getLocaleText("acc.common.msg1")],response.success*2+1);
    },
    getModuleAndActiveDaysDetails : function() {
        var arr = [];
        this.userActiveDaysGridStore.each(function(rec) {
            arr.push(this.userActiveDaysGridStore.indexOf(rec));
        }, this);
        var jarray = WtfGlobal.getJSONArray(this.userActiveDaysGrid, true, arr);
        return jarray;
    },
    
    getUserManagementBtns: function(){
    	this.createUser = new Wtf.Action({
            text:WtfGlobal.getLocaleText("acc.field.CreateUser"),
            tooltip:WtfGlobal.getLocaleText("acc.field.CreateUser"),//WtfGlobal.getLocaleText("hrms.common.documents.tooltip"),
            iconCls:getButtonIconCls(Wtf.etype.menuadd),
            id:this.id+'createUser',
            scope:this,
            handler:function(){
    			new Wtf.common.ManageUser({
    				id:'createUserForm',
    				isEdit:false
    			}).show();
    		}
        });
    	
    	this.editUser = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.field.EditUser"),
            tooltip:WtfGlobal.getLocaleText("acc.field.EditUser"),//WtfGlobal.getLocaleText("hrms.common.documents.tooltip"),
            iconCls:getButtonIconCls(Wtf.etype.menuedit),
            id:this.id+'editUser',
            scope:this,
            disabled:true,
            handler:function(){
    		if(this.selectionModel.getCount()==1){
				new Wtf.common.ManageUser({
					id:'editUserForm',
					isEdit:true,
					rec:this.selectionModel.getSelected()
				}).show();
				}
    		}
        });
    	
    	this.deleteUser = new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.field.Deleteuser"),
            tooltip:WtfGlobal.getLocaleText("acc.field.Deleteuser"),//WtfGlobal.getLocaleText("hrms.common.documents.tooltip"),
            iconCls:getButtonIconCls(Wtf.etype.menudelete),
            id:this.id+'deleteUser',
            scope:this,
            disabled:true,
            handler:this.deleteHandler
        });
    	
        this.resetUserPassword= new Wtf.Action({
            text: WtfGlobal.getLocaleText("acc.field.ResetPassword"),
            tooltip:WtfGlobal.getLocaleText("acc.field.Clicktoresetpassword"),//WtfGlobal.getLocaleText("hrms.common.documents.tooltip"),
            iconCls:getButtonIconCls(Wtf.etype.resetbutton),
            id:this.id+'resetUserPassword',
            scope:this,
            disabled:true,
            handler:this.callResetUserPassword
        })
    	
    	this.menubtns=[this.createUser, this.editUser, this.deleteUser, this.resetUserPassword];
    	
    	this.enableBtnArrSingleSelect1 = new Array();
    	this.enableBtnArrSingleSelect1.push(this.menubtns.length-1);
    	this.enableBtnArrSingleSelect1.push(this.menubtns.length-2);
    	this.enableBtnArrSingleSelect1.push(this.menubtns.length-3);
    	var userManagement= new Wtf.Toolbar.Button({
            text: WtfGlobal.getLocaleText("acc.field.UserManagement"),
            iconCls:getButtonIconCls(Wtf.etype.customer), //getButtonIconCls(Wtf.etype.user),
            id:this.id+'userManagement',
            menu:new Wtf.menu.Menu({
                items:this.menubtns
            }),
    		hidden:!Wtf.account.companyAccountPref.standalone
        });
    	
    	return userManagement;
    },
    
    deleteUser:function() {
    	var arr=[];
    	var data=[];
    	this.recArr = this.usergrid.getSelectionModel().getSelections();
    	for(var i=0;i<this.recArr.length;i++){
    		data.push(this.recArr[i].data.userid);
    	}
    	
    	Wtf.Ajax.requestEx({
	          url:"ProfileHandler/standAloneDeleteUser.do",
	          params:{
	             userids:data
	          }
    		},this,this.genSuccessResponse,this.genFailureResponse);
    },
    
    genSuccessResponse:function(response){
        if(SATSCOMPANY_ID==companyid){
            if(response.valid){
            var superthis=this;
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.field.UserDeletedSuccessfully.")],response.success*2+1,null,null,function(btn){
                if(btn==='ok'){
                    superthis.usergrid.getStore().reload();
                        
                }else{
                    return
                }
            },this);
        } else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.CannotdeleteCompanyCreatororself.")],response.success*2+1);
        }
        }else{
            if(response.success){
    		this.usergrid.getStore().reload();
        	WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg],response.success*2+1);
            }else { 
    	        WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"), response.msg],response.success*2+1);
            }
        }   
    },
    
    genFailureResponse:function(response){
//    	this.usergrid.getStore().reload();
    	WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"),WtfGlobal.getLocaleText("acc.field.Cannotdeleteuser.")],response.success*2+1);
    },
    
    deleteHandler:function(){
    	Wtf.MessageBox.show({
    	       title: WtfGlobal.getLocaleText("acc.common.warning"),
    	       msg: WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttodeletetheselecteduser?"),
    	       width: 350,
    	       buttons: Wtf.MessageBox.OKCANCEL,
    	       animEl: 'upbtn',
    	       icon: Wtf.MessageBox.QUESTION,
    	       scope:this,
    	       fn:function(btn){
    	           if(btn=="ok"){
    	        	   var arr=[];
    	           	var data=[];
    	           	this.recArr = this.usergrid.getSelectionModel().getSelections();
    	           	for(var i=0;i<this.recArr.length;i++){
    	           		data.push(this.recArr[i].data.userid);
    	           	}
    	           	
    	           	Wtf.Ajax.requestEx({
    	       	          url:"ProfileHandler/standAloneDeleteUser.do",
    	       	          params:{
    	       	             userids:data
    	       	          }
    	           		},this,this.genSuccessResponse,this.genFailureResponse);
    	            }
    	        }});
    	
    },
    callResetUserPassword:function(){
    
    this.newPassword= new Wtf.form.TextField({
        fieldLabel: WtfGlobal.getLocaleText("acc.changePass.newPass")+'*',
        inputType:'password',
        allowBlank: false,
        minLength:4,
        maxLength:32,
        name:'newpassword',
        id:'newpassword'
    });
    this.confirmPassword= new Wtf.form.TextField({
        fieldLabel: WtfGlobal.getLocaleText("acc.changePass.retype")+'*',
        inputType:'password',
        allowBlank: false,
        minLength:4,
        maxLength:32,
        name:'confirmpassword',
        initialPassField:'newpassword',
        vtype:'password',
        id:'confirmpassword'
    });
    this.resetForm= new Wtf.form.FormPanel({
        autoWidth:true,
        autoHeight:true,
        border:false,
        bodyStyle:'margin-left:20px ; margin-top:40px ',
        items: [this.newPassword,this.confirmPassword]
    })
    this.resetPasswordWindow=new Wtf.Window({
        id:'resetuserpasswordwindow',
        title:WtfGlobal.getLocaleText("acc.field.ResetPassword"),
        border: false,
        modal: true,
        width:350,
        height:180,
        layout:'fit',
        buttons:[
            {
                text: WtfGlobal.getLocaleText("acc.common.submit"), //'Submit',
                scope: this,
                handler:this.resetPassword
            }, {
                text: WtfGlobal.getLocaleText("acc.CANCELBUTTON"), //'Cancel',
                scope: this,
                handler:this.closeWindow
            }
        ],
        items:this.resetForm,
        iconCls : getButtonIconCls(Wtf.etype.deskera)
    }).show();
},
    closeWindow: function(obj){
        Wtf.getCmp('resetuserpasswordwindow').close();
    },
    resetPassword:function(){
        if(this.resetForm.getForm().isValid()){
        if(this.newPassword.getValue()!=this.confirmPassword.getValue()){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.common.alert")],2);
            return;
        }
        Wtf.MessageBox.show({
            title: WtfGlobal.getLocaleText("acc.common.warning"),
            msg: WtfGlobal.getLocaleText("acc.field.areYouSureYouWantToResetPassword"),
            width: 350,
            buttons: Wtf.MessageBox.OKCANCEL,
            animEl: 'upbtn',
            icon: Wtf.MessageBox.QUESTION,
            scope:this,
            fn:function(btn){
                if(btn=="ok"){
                    Wtf.getCmp('resetuserpasswordwindow').close();
                    var record = this.usergrid.getSelectionModel().getSelected();
                    var userid=record.data.userid;
                    Wtf.Ajax.requestEx({
                        url:"ProfileHandler/standAloneResetUserPassword.do",
                        params:{
                            userId:userid,
                            newPassword:this.newPassword.getValue()
                        }
                    },this,this.genResetPasswordSuccessResponse,this.genResetPasswordFailureResponse);
                }
            }
        });
        }  
    },
    genResetPasswordSuccessResponse:function(response,request){
        var msg=response.msg;
        if(response.success){    
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),msg],response.success*2+1);
        } else{
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.failure"),msg],response.success*2+1);
        }
    },
    genResetPasswordFailureResponse:function(response,request){
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }
});