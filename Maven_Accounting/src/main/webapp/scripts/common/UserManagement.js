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

Wtf.common.ManageUser = function(config){
	this.isEdit = config.isEdit;
	Wtf.apply(this,{
	title:config.isEdit?WtfGlobal.getLocaleText("acc.field.EditUser"):WtfGlobal.getLocaleText("acc.field.CreateUser"),
	id:this.id+'updateProfileWin',
	closable: true,
	modal: true,
	iconCls : getButtonIconCls(Wtf.etype.deskera),
	width: 450,
	height:370,
	autoScroll:true,
	resizable: false,
	layout: 'border',
	buttonAlign: 'right',
	renderTo: document.body,
	buttons: [{
	    text:config.isEdit?WtfGlobal.getLocaleText("acc.field.EditUser"):WtfGlobal.getLocaleText("acc.field.CreateUser"),
	    scope: this,
	    handler:this.saveForm.createDelegate(this)
	},{
	    text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),
	        scope:this,
	        handler:function(){
				this.close();
			}
	    }]
	},config);

    Wtf.common.ManageUser.superclass.constructor.call(this, config);
};

Wtf.extend( Wtf.common.ManageUser, Wtf.Window, {

    onRender: function(config){
        Wtf.common.ManageUser.superclass.onRender.call(this, config);
        this.createUserForm();       
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(this.isEdit?WtfGlobal.getLocaleText("acc.field.EditUser"):WtfGlobal.getLocaleText("acc.field.CreateUser"), this.isEdit?WtfGlobal.getLocaleText("acc.field.Edituserprofile"):WtfGlobal.getLocaleText("acc.field.Useruserprofile"))
        },{
            region: 'center',
            border: false,
            bodyStyle: 'background:#f1f1f1;font-size:10px;',
            autoScroll:true,
            items:this.userinfo
        });
    },
    
    createUserForm:function(){
        this.dfRec = Wtf.data.Record.create ([
            {name:'formatid'},
            {name:'name'}
        ]);
    	this.dfStore=new Wtf.data.Store({
            url:"kwlCommonTables/getAllDateFormats.do",
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.dfRec)
        });
         this.roleRecord = new Wtf.data.Record.create([
            {name: 'roleid'},
            {name: 'rolename'},
            {name:'desc'}
        ]);
        this.roleReader = new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty:"count"
        }, this.roleRecord);    
        this.roleStore = new Wtf.data.Store({
            url:"ACCCommon/getRoleList.do",
            reader:this.roleReader
        });
//        this.roleStore = new Wtf.data.SimpleStore({
//            fields:["id","name"],
//            data:[[1,"Company Administrator"],
//                  [2,"Company User"]  ]
//        });
      
       this.username = new Wtf.form.TextField({
    	   fieldLabel:WtfGlobal.getLocaleText("acc.field.Username")+"*",
           name:'username',
           id:'username',
           readOnly:this.isEdit,
           cls:"readOnly",
           width:220,
           allowBlank:false,
           maxLength:30,
           regex: /^\w[\w|\.]*$/,
           validator:WtfGlobal.validateUserName
       });
       
       this.emailAdd=new Wtf.form.TextField({
    	   fieldLabel:WtfGlobal.getLocaleText("acc.userAdmin.emailAddress")+'*',
           name:'emailid',
           allowBlank:false,
           validator:WtfGlobal.noBlankCheck,
           width:220,
           maxLength:50,
           vtype:'email'
       });
       
       this.fname=new Wtf.form.TextField({fieldLabel: WtfGlobal.getLocaleText("acc.field.FirstName")+'*',
           name: 'fname',
           id:'fname',
           width:220,
           maxLength:50,
           validator:WtfGlobal.noBlankCheck,
           allowBlank:false
       });
       
       this.lname=new Wtf.form.TextField({fieldLabel:WtfGlobal.getLocaleText("acc.profile.lName")+'*',
           name: 'lname',
           id:'lname',
           maxLength:50,
           width:220,
           validator:WtfGlobal.noBlankCheck,
           allowBlank:false
       });
       this.roleCmb= new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.userAdmin.role")+"*",  //Role
            hiddenName:'roleid',
            id : 'rolecombo'+this.id,
            name:'roleid',
            store:this.roleStore,
            valueField:'roleid',
            allowBlank: false,
            displayField:'rolename',
            width:220,
            mode: 'local',
            triggerAction: 'all',
            forceSelection: true//editable : false //Remove Hand icon on hovering combo[SK]
        });
       this.dfCmb= new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.profile.dateFmt")+"*",  //'Date format',
            hiddenName:'formatid',
            name: 'formatid',
            id: 'dfcombo'+this.id,
            store:this.dfStore,
            allowBlank: false,
            valueField:'formatid',
            displayField:'name',
            mode: 'local',
            width:220,
            triggerAction: 'all',
            forceSelection: true//editable : false //Remove Hand icon on hovering combo[SK]
        });
        chktimezoneload();
        this.tzCmb= new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.profile.timeZone")+"*",  //'Timezone',
            hiddenName:'tzid',
            id : 'tzcombo'+this.id,
            name: 'tzid',
            store:Wtf.timezoneStore,
            allowBlank: false,
            valueField:'id',
            displayField:'name',
            mode: 'local',
            width:220,
            triggerAction: 'all',
            forceSelection: true//editable : false //Remove Hand icon on hovering combo[SK]
        });
       this.genderstore = new Wtf.data.SimpleStore({
           fields:['id','gender'],
           data: [
           ['1',WtfGlobal.getLocaleText("acc.field.Male")],//WtfGlobal.getLocaleText("hrms.common.Male")
           ['2',WtfGlobal.getLocaleText("acc.field.Female")]//WtfGlobal.getLocaleText("hrms.common.Female")
           ]
       });
       
       this.gender=new Wtf.form.ComboBox({
           fieldLabel: WtfGlobal.getLocaleText("acc.field.Gender"), //WtfGlobal.getLocaleText("hrms.common.Gender")+"*",
           hiddenName:'gender',
           store:this.genderstore,
           displayField:'gender',
           valueField:'gender',
           forceSelection: true,
           selectOnFocus:true,
           triggerAction: 'all',
           typeAhead:true,
           mode: 'local',
           width:220,
           allowBlank:false
       });
       
       this.contactno=new Wtf.form.TextField({fieldLabel:WtfGlobal.getLocaleText("acc.field.ContactNo."),
           name: 'contactno',
           width:220,
           validationDelay:0,
           maxLength:20,
           id:'contactno',
           xtype:'textfield'
       });
       
       this.address=new Wtf.form.TextArea({fieldLabel:WtfGlobal.getLocaleText("acc.customerList.gridAddress"),
           name: 'address',
           width:220,
           id:'address',
           maxLength:255,                         
           xtype:'textarea'
       });
       
       this.password=new Wtf.form.TextField({fieldLabel:WtfGlobal.getLocaleText("acc.field.Password"),
           name: 'password',
           width:220,
           id:'password',
           maxLength:255,
           readOnly:this.isEdit,
           xtype:'password',
           allowBlank:!this.isEdit,
           hidden:this.isEdit,
           disabled:this.isEdit,
           hideLabel:this.isEdit
       });
       this.roleStore.load();
            this.dfStore.on('load',function(){
                this.setFormValues();
            },this);
            this.dfStore.load();   

       var array = [{
	           name:'userid',
	           id:'userid',
//	           xtype:'hidden'
	           hideLabel:true,
	           hidden:true,
	           value:(this.rec!=null&&this.rec!=undefined)?this.rec.data.userid:""
       		},
       		this.username,
       		this.emailAdd,
       		this.fname,
       		this.lname,
                this.roleCmb,               
                this.dfCmb,
                this.tzCmb,
       		//this.gender,
//       		{
//	           fieldLabel:'User Picture',
//	           name:'userimage',
//	           width:225,
//	           inputType:'file',
//	           id:'userimage',
//	           hidden:Wtf.account.companyAccountPref.standalone?false:true,
//	           hideLabel:Wtf.account.companyAccountPref.standalone?false:true
//       		},
       		this.contactno,
       		this.address//,
       		//this.password
       		];
       
       this.userinfo= new Wtf.form.FormPanel({
	        fileUpload:true,
	        baseParams:{mode:12,formname:"account"},
	        url:this.isEdit?"ProfileHandler/standAloneEditUser.do":"ProfileHandler/standAloneSaveUser.do",
	        region:'center',
	        cls:"visibleDisabled",
	        bodyStyle:"background: transparent;",
	        border:false,
	        style: "background: transparent;padding:20px;",
	        defaultType:'striptextfield',
	        labelWidth:125,
	        items:array,
                scope: this
	      });
              
    },  
        
    saveForm:function(){
        if(!this.userinfo.getForm().isValid()){
        	WtfComMsgBox(2,2);
        }else {
        	this.userinfo.getForm().submit({
            waitMsg:WtfGlobal.getLocaleText("acc.field.SavingUserInformation"), // WtfGlobal.getLocaleText("hrms.common.Savinguserinformation"),
            waitTitle: WtfGlobal.getLocaleText("acc.field.PleaseWait"), //WtfGlobal.getLocaleText("hrms.common.PleaseWait"),
            success:function(f,a){
            	if(!a.result.data.success) {
            		if(a.result.data.duplicateEmail)
            			WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.DuplicateuserEmailID.PleaseEnterdifferentEmailID.")],f.success*2+1);
            		else{
                            var msg = WtfGlobal.getLocaleText("acc.field.Duplicateusername.PleaseEnterdifferentusername.");
                            if(a.result.data.msg)
                                msg = a.result.data.msg;
            		    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],f.success*2+1);
                        }   
            	} else {
	            	this.close();
	            	Wtf.getCmp('usergrid').getStore().reload();
                        Wtf.getCmp('usergrid').getView().refresh();
	            	WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),WtfGlobal.getLocaleText("acc.field.UserInformationsavedSuccessfully.")],f.success*2+1);
            	}
        	},
            failure:function(f,a){
                var msg=WtfGlobal.getLocaleText("acc.common.msg1");
                if(a.result.data.msg)
                msg=a.result.data.msg;
            	WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],f.success*2+1);
        	},
            scope:this
        });
        }	
    },
    
    setFormValues:function(){
    	if(this.isEdit){
     	   this.username.setValue(this.rec.data.username);
     	   this.emailAdd.setValue(this.rec.data.emailid);
     	   this.fname.setValue(this.rec.data.fname);
     	   this.lname.setValue(this.rec.data.lname);
     	   this.contactno.setValue(this.rec.json.contactno);
     	   this.address.setValue(this.rec.data.address);
           this.roleCmb.setValue(this.rec.data.roleid);
           this.dfCmb.setValue(this.rec.json.formatid);
           this.tzCmb.setValue(this.rec.json.tzid);
        }
    }
});



