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
Wtf.common.Permissions=function(config){
    Wtf.apply(this, config);
    this.PerRecord=new Wtf.data.Record.create(
        ['featureid','permission']
     );

     this.PerStore = new Wtf.data.Store({
//        url: Wtf.req.base+'UserManager.jsp',
        url:this.rolepermissionflag?"ACCCommon/getGlobalRolePermissions.do":"PermissionHandler/getRolePermissions.do",
        baseParams:{
            mode:7
        },
        reader: new Wtf.data.KwlJsonReader({
            root: 'data'
        },this.PerRecord)
     });
    
    this.roleRecord=new Wtf.data.Record.create(
        ['roleid','rolename']
    );
    this.roleStore = new Wtf.data.Store({
//        url: Wtf.req.base+'UserManager.jsp',
        url:"PermissionHandler/getRoleList.do",
        baseParams:{
            mode:8
        },
        reader: new Wtf.data.KwlJsonReader({
            root: 'data'
        },this.roleRecord)
    });
    this.roleCmb= new Wtf.form.FnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.userAdmin.role"),  //'Role',
        hiddenName:'roleid',
        store:this.roleStore,
        valueField:'roleid',
        displayField:'rolename',
        anchor:'90%',
//        addNewFn:this.openNewRoleWindow.createDelegate(this),
        editable : false
    });
//    this.applyBtn=new Wtf.Button({
//        text:WtfGlobal.getLocaleText("acc.userAdmin.apply"),  //'Apply',
//        scope:this,
//        handler:this.ApplyPermission
//    });
    Wtf.apply(this,{
        buttons:[{
            text:WtfGlobal.getLocaleText("acc.common.reset"), 
            scope:this,
            handler:this.resethandler
        },{
            text:WtfGlobal.getLocaleText("acc.userAdmin.apply"),  //'Apply',
            scope:this,
            handler:this.ApplyPermission
        },{
            text:WtfGlobal.getLocaleText("acc.common.close"),  //'Close',
            scope:this,
            handler:this.cancel
        }]
    },config);
    Wtf.common.Permissions.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true,
        'afterRenderAll': true
    });
}

Wtf.extend( Wtf.common.Permissions,Wtf.Window,{
    title:WtfGlobal.getLocaleText("acc.userAdmin.assignPerm"),  //'Assign Permission',
    id:'AP',
    height:600,
    width:700,
    onRender:function(config){
        this.featureRecord=new Wtf.data.Record.create(
            ['featureid','featurename','displayfeaturename']
        );

        this.featureStore = new Wtf.data.Store({
//            url: Wtf.req.base+'UserManager.jsp',
            url : "PermissionHandler/getFeatureList.do",
            baseParams:{mode:1},
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.featureRecord)
        });
        this.PerStore.on('load',this.checkActivities,this);
        this.featureStore.on('load',this.loadActivities,this);
        this.roleStore.on('load',function(){
            if (this.isEdit&&this.rolepermissionflag) {
                if(this.record.data.roleid==1||this.record.data.roleid==2){
                Wtf.getCmp("rolename").setDisabled(true);
                Wtf.getCmp("description").setDisabled(true);
               }
                Wtf.getCmp("rolename").setValue(this.record.data.rolename);
                Wtf.getCmp("description").setValue(this.record.data.desc);
            }
          this.roleCmb.setValue(this.roleid);
            if(this.roleid==1){ //Role combo disabled for Company Administrator role
                this.roleCmb.setDisabled(this.isadminenabledisable);
            }else{
                this.roleCmb.setDisabled(false);
            }
                this.loadPermissions();
        },this);
        this.roleCmb.on('select',function(){
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.chane.roleperm"), function(btn) {
                if (btn != "yes")
                    return;
                this.loadPermissions();
            }, this)
        }, this);
        Wtf.common.Permissions.superclass.onRender.call(this,config);
        this.featureStore.load();
    },
    
    loadActivities:function(){
        this.ActRecord=new Wtf.data.Record.create(
            ['featureid','activityid','activityname','displayactivityname','alignright','parentid']
        );
        this.ActStore = new Wtf.data.Store({
//            url: Wtf.req.base+'UserManager.jsp',
            url : "PermissionHandler/getActivityList.do",
            baseParams:{mode:2},
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.ActRecord)
        });
        this.ActStore.on('load',this.createWindow,this);
        this.ActStore.load();
    },

    openNewRoleWindow:function(){
        Wtf.Msg.prompt(WtfGlobal.getLocaleText("acc.field.NewRole"), WtfGlobal.getLocaleText("acc.field.Rolename"), function(btn, text){
            if (btn == 'ok')
            if(text.length>0&&text.trim()!=""){
                Wtf.Ajax.requestEx({
//                    url: Wtf.req.base+'UserManager.jsp',
                    url : "PermissionHandler/saveRoleList.do",
                    params: {
                        mode:9,
                        userid:this.userid,
                        displayrolename:text,
                        rolename:text
                    }
                },this,this.genRoleSuccessResponse,this.genRoleFailureResponse);
            }
            else{
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.field.NewRole"),WtfGlobal.getLocaleText("acc.field.NoRolenameentered.Doyouwanttocontinue?"),function(btn){
                if(btn!="yes")
                    this.openNewRoleWindow();
                },this);
            }
        },this);
    },

    loadPermissions: function() {


        for (var i = 0; i < this.featureStore.getCount(); i++) {
            var rec = this.featureStore.getAt(i);
            if (Wtf.getCmp('feature' + rec.data['featureid']) != undefined) {
                Wtf.getCmp('feature' + rec.data['featureid']).destroy();
            }

        }
        this.fCount = this.featureStore.getCount();
        for (var i = 0; i < this.fCount; i++) {
            this.createFeatureSet(i, i % 2);
        }

        this.doLayout();
        if (this.isEdit && this.rolepermissionflag) {
            this.PerStore.load({
                params: {
                    roleid: this.record.data.roleid,
                    isEdit: this.isEdit
                }
            });
        } else if (!this.rolepermissionflag) {
            
            this.PerStore.load({
                params: {
                    roleid: this.roleCmb.getValue(),
                    userid: this.userid,
                    allowedit:(!this.isadminenabledisable&&this.roleid==1&&this.roleid!=this.roleCmb.getValue())?true:false
                }
            });
        } else {
            this.checkActivities();
        }
    },

    createForm:function(){
        this.AssPerForm= new Wtf.FormPanel({
            region:'center',
            cls:'x-panel-body x-panel-body-noheader x-panel-body-noborder',
            bodyStyle: "background: transparent;",
            border:false,
            autoScroll:true,
            bodyBorder:false,
            style: "background: transparent;",
            id:'AssPerForm',
            items:[{
                name:'userid',
                xtype:'hidden'
            },!this.rolepermissionflag?{
                layout:'column',
                style:'padding:20px',
                border:false,
                items:[{
                    columnWidth:.73,
                    layout:'form',
                    border:false,
                    labelWidth:50,
                    items:this.roleCmb
                },{
                    columnWidth:.23,
                    layout:'form',
                    border:false,
                    labelWidth:50,
                     items:{
                        xtype:'button',
                        text:WtfGlobal.getLocaleText("acc.field.DeleteRole"),
                        scope:this,
                        hidden: true,
                        handler:this.deleteRole
                    }
                }]
            }:{
                layout:'column',
                style:'padding:20px',
                border:false,
                items:[{
                    columnWidth:.93,
                    layout:'form',
                    border:false,
                    labelWidth:80,
                     items:{
                        id:'rolename',
                        xtype:'textfield',
                        fieldLabel:WtfGlobal.getLocaleText("acc.common.role"),
                        scope:this,
                        width:350,
                        allowBlank: false,
                        //hidden: true,
                        //handler:this.deleteRole
                    }
                },{
                    columnWidth:.93,
                    layout:'form',
                    border:false,
                    labelWidth:80,
                     items:{
                        id:'description',
                        xtype:'textarea',//
                        fieldLabel:WtfGlobal.getLocaleText("acc.product.description"),
                        scope:this,
                        width:350
                        //hidden: true,
                        //handler:this.deleteRole
                    }
                   // items:this.roleCmb
                }]
            },{
                layout:'column',
                border:false,
                items:[{
                    columnWidth:.48,
                    layout:'form',
                    labelWidth:250,
                    id:this.id+"-col0",
                    border:false
                },{
                    columnWidth:.48,
                    layout:'form',
                    labelWidth:250,
                    id:this.id+"-col1",
                    border:false
                }]
            }]
        });
        this.fCount=this.featureStore.getCount();
        for(var i=0;i<this.fCount;i++){
            this.createFeatureSet(i,i%2);
        }
    },

    createWindow:function(){
        this.createForm();
        var  setpem=this.rolepermissionflag?WtfGlobal.getLocaleText("acc.permission.role"):WtfGlobal.getLocaleText("acc.userAdmin.setPerm");
        var username=this.rolepermissionflag?"":this.userFullName;
        this.MainWinPanel= new Wtf.Panel({
            border:false,
            height:533,
            layout:'border',
            items:[{
                region: 'north',
                height: 75,
                border: false,
                bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml(setpem,this.rolepermissionflag?"":WtfGlobal.getLocaleText("acc.userAdmin.setPermUsers") + " : <b>"+username +"</b>","../../images/accounting_image/role-assign.gif")
            },{
                region:'center',
                border:false,
                layout:'fit',
                bodyStyle: "background: rgb(241, 241, 241);",
                items:[this.AssPerForm]
            }]
        });
        this.add(this.MainWinPanel);
        this.doLayout();
        this.roleStore.load();
    },

    createFeatureSet:function(index,col){
        var rec=this.featureStore.getAt(index);
        var feature=new Wtf.form.FieldSet({
            id:'feature'+rec.data['featureid'],
            xtype:"fieldset",
            style:'margin:10px',
            collapsible:true,
            collapsed:true,
            autoHeight:true,
            title:rec.data['displayfeaturename']
        });
        this.ActStore.filter('featureid',rec.data['featureid']);
        var activityCount = this.ActStore.getCount();
        for(var i=0;i<activityCount;i++){
            this.createActivity(i,feature);
            if(index==(this.fCount-1) && i==(activityCount-1)){ //IF last Feature and Last Activity
                var Arec=this.ActStore.getAt(i);
                Wtf.getCmp("activity"+Arec.data['activityid']).on("render",function(){
                   this.fireEvent('afterRenderAll',this);
                   this.doLayout();
                },this);
            }
        }
        this.ActStore.clearFilter();
        Wtf.getCmp(this.id+"-col"+col).add(feature);
    },

    createActivity:function(index,feature){
        
        var rec=this.ActStore.getAt(index);
        var activity=new Wtf.form.Checkbox({
            fieldLabel: (rec.data.alignright?'...':'')+rec.data['displayactivityname'],
            labelStyle:(rec.data.alignright?'padding-left:25px;width:225px;':'width:250px; font-weight:bold;'),// ,
            name: "act"+rec.data['activityid'],
            id:"activity"+rec.data['activityid'], //dont remove "activity" string
            //We have hidden permission for GL report from Assign Permission Window- SDP-12639 (View Ledger, Export Data ,Print Data)
            hidden: (rec.data['activityid']=='ff80808122f3cb640122f4585b50002b' || rec.data['activityid']=='ff80808122f9dba90122fa4888cf00256' || rec.data['activityid']=='ff80808122f9dba90122fa4888cf00257')  ? true : false,
            hideLabel: (rec.data['activityid']=='ff80808122f3cb640122f4585b50002b' || rec.data['activityid']=='ff80808122f9dba90122fa4888cf00256' || rec.data['activityid']=='ff80808122f9dba90122fa4888cf00257')  ? true : false
//        	checked: true
        });
        feature.add(activity);
    }, 
    enableDisableChild:function(o,newval){
        if(newval){
            this.ActStore.filter('parentid',o.getId().substring(8,o.getId().length));
             for(var x=0;x<this.ActStore.getCount();x++){
                var actRec=this.ActStore.getAt(x);
                var comp=Wtf.getCmp("activity"+actRec.data['activityid']);
     //           Wtf.getCmp("activity"+actRec.data['activityid']).enable();
                Wtf.getCmp("activity"+actRec.data['activityid']).setValue(true);	//Done by neeraj
                }

        }else{
            this.ActStore.filter('parentid',o.getId().substring(8,o.getId().length));
             for( x=0;x<this.ActStore.getCount();x++){
                actRec=this.ActStore.getAt(x);
                comp=Wtf.getCmp("activity"+actRec.data['activityid']);
                comp.setValue(false);
     //           Wtf.getCmp("activity"+actRec.data['activityid']).disable();
                Wtf.getCmp("activity"+actRec.data['activityid']).setValue(false);	//Done by neeraj
            }
        }
        this.ActStore.clearFilter();
    },
    isChecked:function(actRec,index){
        var permRec=this.PerStore.getAt(this.PerStore.find('featureid',actRec.data['featureid']));
        if(permRec==null)return false;
        var permCode=permRec.data['permission'];
        while(index>0){
            permCode=Math.floor(permCode/2);
            index--;
        }
        return permCode%2==1;
    },

    checkActivities:function(){
        var comp;
        var featureids=this.ActStore.collect("featureid");
        var userRole=this.roleCmb.getValue();
//        this.applyBtn.setDisabled(userRole=="1"||userRole=="2");//Don't disable, User can change role to Admin/User and check added server side for don't assigning perms
        for(var i=0;i<featureids.length;i++){
            this.ActStore.filter('featureid',featureids[i]);
            for(var x=0;x<this.ActStore.getCount();x++){
                var actRec=this.ActStore.getAt(x);
                comp=Wtf.getCmp("activity"+actRec.data['activityid']);
                if(comp != undefined){
                    comp.setValue(this.isChecked(actRec,x));
                    comp.setDisabled(userRole=="1"/*||userRole=="2"*/);
                    if(!actRec.data.alignright)	{
                        comp.on('check',this.enableDisableChild,this);		// Comment this                                   
                    }
                }
            }
        }
//        this.ActStore.clearFilter();
//        for(x=0;x<this.ActStore.getCount();x++){
//            actRec=this.ActStore.getAt(x);
//            comp=Wtf.getCmp("activity"+actRec.data['activityid']);
//                if(!actRec.data.alignright)								// Comment this
//                    this.enableDisableChild(comp,comp.getValue());		// Comment this
//        }
    },
    
    ApplyPermission:function(){
        var permCode=[];
        var features=[];
        var rolename;
        var desc
        var formVal=this.AssPerForm.getForm().getValues();
        if (this.rolepermissionflag) {
            rolename = Wtf.getCmp("rolename").getValue();
            rolename = rolename.trim();
             if(rolename==""){
                  WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.rolemanagement.PleaseenterRoleNameFirst")]);
                  return;
             }
             desc = Wtf.getCmp("description").getValue();
        }
        for(var i=0;i<this.featureStore.getCount();i++){
            var feature=this.featureStore.getAt(i).data['featureid'];
            features.push(feature);
            permCode.push(this.getNewFeatureValue(feature,formVal));
        }

        Wtf.Ajax.requestEx({
//            url: Wtf.req.base+'UserManager.jsp',
            url :this.rolepermissionflag?"ACCCommon/setRolePermissions.do":"PermissionHandler/setPermissions.do",
            params: {
                mode:15,
                userid:this.userid,
                roleid:(this.rolepermissionflag&&this.isEdit)?this.record.data.roleid :this.roleCmb.getValue(),
                rolename:rolename,
                description:desc,
                features:features,
                permissions:permCode,
                isEdit:this.isEdit
            }
        },this,this.genSuccessResponse,this.genFailureResponse);
    },
/* Code to calculate user permission code*/
    getNewFeatureValue:function(featureid,formVal){
        var code=0;
        var tmp=1;
        this.ActStore.filter('featureid',featureid);
        if(featureid=='ff80808122f3cb640122f44bd982000f'){
            var t = "";
        }
       /* Looping on Actitvity List(Create , View , Export etc.) */
        for(var i=0;i<this.ActStore.getCount();i++){
            
            var id=this.ActStore.getAt(i).data['activityid'];
            if(eval('('+'formVal.act'+id+')'))
            /* Calculation of Permission Code */   
                code=code+tmp;
            tmp=2*tmp;
        }
        this.ActStore.clearFilter();
        return code;
    }, 
    cancel:function(){
        this.close();
    },
    resethandler:function() {
        this.loadPermissions();
    },
    
    genSuccessResponse: function(response) {
        this.fireEvent('update', this);
        if (response.isExitusername) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), response.msg], response.success * 2 + 1);
            return;
        } else if (response.isExitadmin) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText(response.msg)], response.success * 2 + 1);
            return;
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.userAdmin.setPerm"), response.msg], response.success * 2 + 1);
            this.close();
        }
        WtfGlobal.resetAjaxTimeOut();
        getCompanyAccPref();
        var as = Wtf.getCmp('as');
        if(as){
            as.items.each(function(item) {
            if (!(item.id == "tabdashboard"))
                item.ownerCt.remove(item);
        }, this);
        }
    },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    deleteRole:function(){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.field.Areyousure?"),function(btn){
            if(btn!="yes") return;
            Wtf.Ajax.requestEx({
//                url: Wtf.req.base+'UserManager.jsp',
                url : "PermissionHandler/deleteRole.do",
                params: {
                    mode:10,
                    roleid:this.roleCmb.getValue()
                }
            },this,this.genRoleSuccessResponse,this.genRoleFailureResponse);
        },this);
    },
     genRoleSuccessResponse:function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.userAdmin.setPerm"),response.msg],response.success*2+1);
            this.roleStore.reload();
    },

    genRoleFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    }
});

