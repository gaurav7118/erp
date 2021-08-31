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
Wtf.common.CreateUser=function(config){
    this.record=config.record;
      Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.field.CreateUser"),
        id:'createUserWin',
        modal: true,
        iconCls :getButtonIconCls(Wtf.etype.deskera),
        width: 430,
        height:430,
        resizable: false,
        buttonAlign:"right",
        layout: 'border',
        renderTo: document.body,
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.field.Save"),
            scope:this,
            handler:function(){
                if(!this.userinfo.getForm().isValid())return;
                this.userinfo.getForm().submit({
                    waitMsg: WtfGlobal.getLocaleText("acc.profile.msg1"),
                    success:function(f,a){this.genSuccessResponse(eval('('+a.response.responseText+')'));this.close();},
                    failure:function(f,a){this.genFailureResponse(eval('('+a.response.responseText+')'));this.close();},
                    scope:this
                });
            }
        },{
            text: WtfGlobal.getLocaleText("acc.invoiceList.bt.cancel"),
            scope:this,
            handler:this.cancel
        }]
    },config);
        this.roleRecord=new Wtf.data.Record.create(
        ['roleid','rolename']
    );
    this.roleStore = new Wtf.data.Store({
        url: Wtf.req.base+'UserManager.jsp',
        baseParams:{
            mode:8
        },
        reader: new Wtf.data.KwlJsonReader({
            root: 'data'
        },this.roleRecord)
    });
    this.roleCmb= new Wtf.form.ComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.userAdmin.role"),
        hiddenName:'roleid',
        store:this.roleStore,
        valueField:'roleid',
        displayField:'rolename',
        allowBlank:false,
        mode: 'local',
        width:'75%',
        triggerAction: 'all',
        editable : false
    });
    Wtf.common.CreateUser.superclass.constructor.call(this, config);
    this.roleStore.on('load',this.loadRecord,this);
    this.roleStore.load();
    this.addEvents({
        'save':true
    });
}

Wtf.extend( Wtf.common.CreateUser, Wtf.Window, {
    loadRecord:function(){
        if(this.record!=null)this.userinfo.getForm().loadRecord(this.record);
    },
    onRender: function(config){
        Wtf.common.CreateUser.superclass.onRender.call(this, config);       
        this.createForm();
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText("acc.field.CreateUser"),WtfGlobal.getLocaleText("acc.field.CreateUser"))
        }, {
            region: 'center',
            border: false,
            bodyStyle: 'background:#f1f1f1;font-size:10px;',
            layout: 'fit',  autoScroll:true,
            items:this.userinfo
        });
    },
    createForm:function(){
        this.userinfo= new Wtf.form.FormPanel({
            fileUpload:true,
            baseParams:{mode:12},
            url: Wtf.req.base+'UserManager.jsp',
            region:'center',
         // cls:'x-panel-body x-panel-body-noheader x-panel-body-noborder',
            bodyStyle: "background: transparent;",
            border:false,
            bodyBorder:false,
            style: "background: transparent;padding-left:20px;padding-top: 20px;padding-right: 0px;",
           // width:'100%',
           // height:'100%',
            id:'userinfo',
            defaultType:'textfield',
            items:[{
                name:'userid',
                xtype:'hidden'
            },{
                fieldLabel: WtfGlobal.getLocaleText("acc.auditTrail.gridUser"),
                readOnly:(this.record!=null),
                name:'username',
                maxLength:30,
                width:'75%',
                allowBlank:false
            },{
                fieldLabel: WtfGlobal.getLocaleText("acc.profile.email"),
                name:'emailid',
                allowBlank:false,
                maxLength:50,
                width:'75%',
                validator:WtfGlobal.validateEmail
            },{
                fieldLabel: WtfGlobal.getLocaleText("acc.profile.fName"),
                name: 'fname',
                maxLength:50,
                width:'75%',
                allowBlank:false
            },{
                fieldLabel: WtfGlobal.getLocaleText("acc.field.LastName*"),
                name: 'lname',
                maxLength:50,
                width:'75%',
                allowBlank:false
            },this.roleCmb,{
                fieldLabel: WtfGlobal.getLocaleText("acc.profile.userPic"),
                name:'userimage',
                inputType:'file'
            },{
                fieldLabel: WtfGlobal.getLocaleText("acc.field.ContactNo*"),
                name: 'contactno',
                xtype:"textfield",
                //regex:Wtf.PhoneRegex,
                width:'75%'
            },{
                fieldLabel: WtfGlobal.getLocaleText("acc.userAdmin.Address"),
                name: 'address',
                width:'75%',
                maxLength:225,
                xtype:'textarea'
            }]
        });
    },
    cancel:function(){
       this.close();
    },

    genSuccessResponse:function(response){
        if(response.success==true){
            this.fireEvent('save');
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.UserManagement"),response.msg],response.success*2+1);
     },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.field.FailedtomakeconnectionwithWebServer");
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.agedPay.alert"),msg],2);
    }
});

