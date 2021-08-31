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

Wtf.common.UpdateProfile = function(config){
    Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.profile.tabTitle"),  //'Update Profile',
        id:'updateProfileWin',
        closable: true,
        modal: true,
        iconCls :getButtonIconCls(Wtf.etype.deskera),
        width: 450,
        height:530,
        resizable: false,
        layout: 'border',
        buttonAlign: 'right',
        renderTo: document.body,
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.common.update"),  //'Update',
            scope: this,
            handler:this.saveForm.createDelegate(this)
        }, {
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            scope: this,
            handler: function(){this.close();}
        }]
    },config);
   
    Wtf.common.UpdateProfile.superclass.constructor.call(this, config);
}

Wtf.extend( Wtf.common.UpdateProfile, Wtf.Window, {

    onRender: function(config){
        Wtf.common.UpdateProfile.superclass.onRender.call(this, config);
        this.createForm();
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText("acc.profile.tabTitle"),WtfGlobal.getLocaleText("acc.profile.tabTitle"))
        },{
            region: 'center',
            border: false,
            bodyStyle: 'background:#f1f1f1;font-size:10px;',
            autoScroll:true,
            items:this.userinfo
        });
    },
    createForm:function(){
        this.dfRec = Wtf.data.Record.create ([
            {name:'formatid'},
            {name:'name'}
        ]);
        this.dfStore=new Wtf.data.Store({
//            url: Wtf.req.base+'UserManager.jsp',
            url:"kwlCommonTables/getAllDateFormats.do",
            baseParams:{
                mode:33,
                newDate: WtfGlobal.convertToGenericDate(new Date())
//                newDate: new Date().toString()
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.dfRec)
        });
        this.dfStore.on('load',this.getRecord,this);
        this.dfCmb= new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.profile.dateFmt"),  //'Date format',
            hiddenName:'formatid',
            store:this.dfStore,
            valueField:'formatid',
            displayField:'name',
            mode: 'local',
            triggerAction: 'all',
            forceSelection: true//editable : false //Remove Hand icon on hovering combo[SK]
        });
        chktimezoneload();
        this.tzCmb= new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.profile.timeZone"),  //'Timezone',
            hiddenName:'tzid',
            store:Wtf.timezoneStore,
            valueField:'id',
            displayField:'name',
            mode: 'local',
            triggerAction: 'all',
            forceSelection: true//editable : false //Remove Hand icon on hovering combo[SK]
        });
        this.userinfo= new Wtf.form.FormPanel({
            fileUpload:true,
            baseParams:{mode:12},
            //url: Wtf.req.base+'UserManager.jsp',
            url : "ACCCommon/saveUsers.do",
            region:'center',
            bodyStyle: "background: transparent;",
            border:false,
            style: "background: transparent;padding:20px;",
            defaultType:'textfield',
            labelWidth:100,
            itemCls:'visibleDisabled',
            defaults:{width:250},
            items:[{
                    name:'userid',
                    xtype:'hidden'
                },this.uname=new Wtf.form.TextField({
                    fieldLabel:WtfGlobal.getLocaleText("acc.profile.userName"),  //'User Name',
                    name:'username',
                    id:'username',
                  //  readOnly:true,
                    disabled:true,
                    allowBlank:false,
                    /**
                     * If company is having self service = 1 then username will be hidden for such company 
                     * ERP-41543
                     */
                    hidden: (Wtf.isSelfService == 1) ? true :false,
                    hideLabel: (Wtf.isSelfService == 1) ? true :false
                }),{
                    fieldLabel:WtfGlobal.getLocaleText("acc.profile.email")+"*",  //'E-Mail',
                    name:'emailid',
                    allowBlank:false,
                    validator:WtfGlobal.validateEmail
                },this.fname=new Wtf.form.TextField({
                    fieldLabel: WtfGlobal.getLocaleText("acc.profile.fName"),  //'First Name*',
                    name: 'fname',
                    maxLength:50,
                    id:'fname',
                    allowBlank:false,
                    regex:Wtf.specialChar
                }),this.lname=new Wtf.form.TextField({
                    fieldLabel: WtfGlobal.getLocaleText("acc.profile.lName"),  //'Last Name',
                    name: 'lname',
                    maxLength:50,
                    id:'lname',
                    regex:Wtf.specialChar
                }),{
                    fieldLabel: WtfGlobal.getLocaleText("acc.profile.userPic"),  //'User Picture',
                    name:'userimage',
                    inputType:'file',
                    id:'userimage'
                },{
                    fieldLabel: WtfGlobal.getLocaleText("acc.profile.contactNo"),  //'Contact Number',
                    name: 'contactno',
                    id:'contactno',
                    maxLength:15,
                    validator: function (value){
                        var numtype=/^[0-9-]{3,25}$/;
                        if (numtype.test(value))
                            return true;
                        return false;
                    }
                },{
                    fieldLabel: WtfGlobal.getLocaleText("acc.profile.address"),  //'Address',
                    name: 'address',
                    id:'address',
                    maxLength:200,
                    xtype:'textarea'
                },{
                    fieldLabel: WtfGlobal.getLocaleText("acc.profile.abtMe"),  //'About Me',
                    name: 'aboutuser',
                    id:'aboutme',
                    maxLength:200,
                    xtype:'textarea'
                },this.dfCmb,this.tzCmb
            ]
        });
        this.dfStore.load();
    },
    getRecord:function(){
        Wtf.Ajax.requestEx({
//            url:Wtf.req.base+"UserManager.jsp",
            url:"ProfileHandler/getAllUserDetails.do",
            params:{
               mode:11,
               lid:Wtf.userid
            }
        },this,this.genSuccessResponse,this.genFailureResponse);

    },
    saveForm:function(){
        this.fname.setValue(this.fname.getValue().trim());
        if(!this.userinfo.getForm().isValid())return;
        this.userinfo.getForm().submit({
            waitMsg:WtfGlobal.getLocaleText("acc.profile.msg1"),  //'Updating User Profile',
            success:function(f,a){this.genSaveSuccessResponse(eval('('+a.response.responseText+')'))},
            failure:function(f,a){
                this.genSaveFailureResponse(eval('('+a.response.responseText+')'))
            },
            scope:this
        });
    },
    genSuccessResponse:function(response){
            this.userinfo.getForm().setValues(response.data[0]);
    },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    genSaveSuccessResponse:function(response){
        if(response.success==true){
            this.updateName();
            updatePreferences();
            this.close();
            if (response.Timezone && response.Timezone == true) {
                document.getElementById("header").style.height = "50px";
                document.getElementById("headTimezone").style.display = "block";
                Wtf.getCmp('viewport').doLayout();
            } else {
                closeTimezonePop();
            }
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.profile.tabTitle"),response.msg],response.success*2+1);
    },

    genSaveFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
//        this.close();
    },

    updateName:function(){
        var name=this.uname.getValue();
        var temp=this.fname.getValue();
        if(temp&&temp.length>0)name=temp;
        temp=this.lname.getValue();
        if(temp&&temp.length>0)name+=" "+temp;
        setFullname(name);
    }
});



