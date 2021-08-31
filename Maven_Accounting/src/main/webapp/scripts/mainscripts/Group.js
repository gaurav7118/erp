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

Wtf.account.Group= function(config){
    Wtf.apply(this,{
        buttons: [{
            text: WtfGlobal.getLocaleText("acc.common.saveBtn"),  //'Save',
            scope: this,
            handler:this.confirmBeforeSave.createDelegate(this)
        }, {
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            scope: this,
            handler:this.closeForm.createDelegate(this)
        }]
    },config);
    this.isEdit=config.isEdit;
    Wtf.account.Group.superclass.constructor.call(this, config);
    this.ispropagatetochildcompanyflag=false;
    this.addEvents({
        'update':true,
        'cancel':true,
        'loadingcomplete':true
    });
}

Wtf.extend(Wtf.account.Group, Wtf.Window, {
    loadRecord:function(){
        if(this.record!=null){
            this.groupForm.getForm().loadRecord(this.record);
                this.checkGroup();
            }           
        this.hideLoading(false);
    },

    onRender: function(config){
        Wtf.account.Group.superclass.onRender.call(this, config);
        this.createFields();
        this.createForm();
        this.createPanel();
        var subtitle=(this.record==null? WtfGlobal.getLocaleText("acc.coa.addNewAccountType"):WtfGlobal.getLocaleText("acc.coa.editAccountType"));  //"Add New ":"Edit ")+  WtfGlobal.getLocaleText("acc.coa.addNewAccountType");  //"Account Type"
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(this.title,subtitle,"../../images/accounting_image/Chart-of-Accounts.gif")
        }, this.centerPanel=new Wtf.Panel({
            region: 'center',
            border: false,
            autoScroll:true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls:'bckgroundcolor',
            layout: 'border',
            items:[this.groupForm,{
                region: 'east',
                border: false,
                width:170,
                layout: 'fit',
                bodyStyle: 'background:#f1f1f1;font-size:10px;',
                items:[this.pan]
            }]
        }));            
    },

    hideLoading:function(val){
        if(!val){
            this.fireEvent("loadingcomplete",this);
            this.groupName.focus();      //To set focus on Name on form load.
        }
    },

    createPanel:function(){
        this.pan=new Wtf.Panel({
            height:100,
            width:150,
            style:"padding:20px 13px 30px 0px;",
            autoScroll:true,
            title:WtfGlobal.getLocaleText("acc.rem.44")  //'Existing Account Types'
        });
        this.pan.hide();
    },

    createFields:function(){
        var parentRec=new Wtf.data.Record.create([
            {name: 'parentid',mapping:'groupid'},
            {name: 'parentname',mapping:'groupname'},
            {name:'nature',type:'int'},
            {name:'affectgp',type:'boolean'},
            {name:'isCostOfGoodsSoldGroup',type:'boolean'},
            {name:'tdsapplicable',type:'boolean'},
            {name:'displayorder',type:'int'},
            {name: 'leaf',type:'boolean'},
            {name: 'level',type:'int'}
        ]);

        this.parentStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },parentRec),
            url:"ACCAccount/getGroups.do",
            baseParams:{
                mode:1,
                groupid:(this.record!=null?this.record.data['groupid']:null)
            }
        });

        this.parentStore.load();
        this.parentStore.on('load',this.loadRecord,this);
        
        this.cmbParent= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.rem.43"),  //'Parent Group',
            hiddenName:'parentid',
            name:'parentid',
            store:this.parentStore,
            valueField:'parentid',
            displayField:'parentname',
            width:150,
            mode: 'local',
//            allowBlank:false,
            disableKeyFilter:true,
            triggerAction:'all',
            typeAhead: true,
            hirarchical:true,
            forceSelection:true,
            itemCls :(Wtf.isChrome) ? "acctypeparentcombo" : ""
        });
               
        this.groupName=new Wtf.form.ExtendedTextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.rem.42"),  //'Type Name*',
            name: 'groupname',
            width:150,
            regex:Wtf.specialChar,
            maxLength:50,
            id:'groupname',
            allowBlank:false,
            listeners:{
                scope:this,
                focus:function(){
                    this.searchText(this.parentStore,this.pan);
                }
            }
        });

        this.natureStore=new Wtf.data.SimpleStore({
            fields:[{name:"id"},{name:"name"}],
            data:[
                [Wtf.account.nature.Asset,"Asset"],
                [Wtf.account.nature.Liability,"Liability"],
                [Wtf.account.nature.Income,"Income"],
                [Wtf.account.nature.Expences,"Expenses"]
            ]
        });

        this.accNature=new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.rem.46"),  //'Nature*',
            hiddenName:'nature',
            name:'nature',
            store:this.natureStore,
            valueField:'id',
            displayField:'name',
            typeAhead: true,
            forceSelection: true,
            width:150,
            allowBlank: false,
            mode: 'local',
            disableKeyFilter:true,
            triggerAction:'all'
        });

        this.affectGP= new Wtf.form.Checkbox({
            fieldLabel: WtfGlobal.getLocaleText("acc.rem.47"),  //"Affect gross profit",
            style: 'margin-top:3px;',
            name: 'affectgp',
            hidden:true,
            hideLabel:true
        });
        
        this.isCostOfGoodsSoldGroup = new Wtf.form.Checkbox({
            fieldLabel: WtfGlobal.getLocaleText("acc.account.group.soldgroup"), //Is a Cost of Goods Sold Group
            style: 'margin-top:3px;',
            name: 'isCostOfGoodsSoldGroup'
        });

        this.enableDisableCombo(true);
        this.groupName.on('change',this.checkDuplicateName ,this);
        this.accNature.on('select',this.checkGroup,this);
        this.cmbParent.on('select',this.onParentSelect,this);
        this.natureStore.on('loadexception',this.hideLoading.createDelegate(this,[false]),this);
        this.parentStore.on('loadexception',this.hideLoading.createDelegate(this,[false]),this);
    },

    searchText:function(parentStore,pan){
        document.getElementById("groupname").onkeyup=function(){
            this.text=Wtf.getCmp("groupname").getValue();
            if(this.text==""){
                pan.hide();
            }
            else{
                parentStore.filterBy(function(rec){
                    var FIND = String(this.text);
                    FIND = new RegExp('^'+Wtf.escapeRe(FIND), 'i');
                    return rec && FIND.test(rec.data.parentname) && !rec.data.deleted;
                },this);

                this.len=parentStore.getCount();
                if(this.len==0){
                    pan.hide();
                }
                for(var i=0;i<this.len;i++){
                    this.presentAcc=parentStore.getAt(i).get("parentname");
                    this.tdata={
                        paname:this.presentAcc
                    }
                    this.tpl=new Wtf.Template('<font size=2> <p>{paname}</p></font>');
                    if(i==0)
                        this.tpl.overwrite(pan.body,this.tdata);
                    else
                        this.tpl.append(pan.body,this.tdata);

                    pan.show();
                }
            }
        };
    },

    checkGroup:function(){
        var isParentForGroup=false;
        var rec=this.cmbParent.store.find("parentid",this.cmbParent.getValue());
        if(rec==-1){
            this.checkAGP();
//            return;
        } else {
            rec = this.cmbParent.store.getAt(rec);
            this.accNature.setValue(rec.data['nature']);
            if (this.affectGP.getValue() != rec.data['affectgp']) {
                this.affectGP.setValue(rec.data['affectgp']);
            }
            isParentForGroup = true;
            if (this.accNature.getValue() == Wtf.account.nature.Expences) {
                this.isCostOfGoodsSoldGroup.setValue(rec.data['isCostOfGoodsSoldGroup']);
                this.isCostOfGoodsSoldGroup.setDisabled(true);
            } else {
                this.isCostOfGoodsSoldGroup.setDisabled(true);
            }
        } 
        
        var recNature=this.accNature.getValue();
        if(recNature==Wtf.account.nature.Asset || recNature==Wtf.account.nature.Liability) {
            this.affectGP.setDisabled(true);
        } else {
            this.affectGP.setDisabled(false);
        }
        if (!isParentForGroup) {
            if (recNature == Wtf.account.nature.Expences) {
                this.isCostOfGoodsSoldGroup.setDisabled(false);
            } else {
                this.isCostOfGoodsSoldGroup.setValue(false);
                this.isCostOfGoodsSoldGroup.setDisabled(true);
            }
        }
    },

    enableDisableCombo:function(disabled){
//        this.accNature.setDisabled(disabled);
        this.affectGP.setDisabled(disabled);
        this.checkGroup();
    },

    checkAGP:function(){
        var rec=this.accNature.getValue();
        if((rec==Wtf.account.nature.Asset || rec==Wtf.account.nature.Liability)&&this.affectGP.getValue()==true)
            this.affectGP.setValue(false);
    },

    onParentSelect:function(){
        var index=this.cmbParent.store.find("parentid",this.cmbParent.getValue());
        if(index!=-1){
            var rec=this.cmbParent.store.getAt(index); 
            if(this.isEdit && rec.data['nature']!=this.accNature.getValue()){               
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.coa.coaGroupAlert")],2);
                this.cmbParent.setValue(this.record.data.parentid);
                if(this.record.data.parentid=="" || this.record.data.parentid==undefined || this.record.data.parentid==null){
                    this.accNature.enable();
                }
                return ;               
            }else{              
                this.accNature.setValue(rec.data['nature']);                
                if(this.affectGP.getValue()!=rec.data['affectgp']) {
                    this.affectGP.setValue(rec.data['affectgp']);
                } 
                this.accNature.disable();
                if(this.accNature.getValue()==Wtf.account.nature.Asset || this.accNature.getValue()==Wtf.account.nature.Liability) {
                    this.affectGP.setDisabled(true);
                } else {
                    this.affectGP.setDisabled(false);
                }
                if (this.accNature.getValue() == Wtf.account.nature.Expences) {
                    this.isCostOfGoodsSoldGroup.setValue(rec.data['isCostOfGoodsSoldGroup']);
                    this.isCostOfGoodsSoldGroup.setDisabled(true);
                } else {
                    this.isCostOfGoodsSoldGroup.setValue(false);
                    this.isCostOfGoodsSoldGroup.setDisabled(true);
                }
            }         
        }
    },
    
    validateCard:function(){
        var flag=true;
        var val;
        //var temp=this.groupForm.getForm().getValues();
        //if(temp.subtype){
            val=this.cmbParent.getValue();
            if(val.length<=0){
                this.cmbParent.markInvalid(WtfGlobal.getLocaleText("acc.product.msg1"));
                flag=false;
            }
        //}
        return flag;
    },

    createForm:function(){
        this.groupForm=new Wtf.form.FormPanel({
            region:'center',
            width:350,
            height:300,
            labelWidth:130,
            border:false,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left:15px;padding-top: 20px;padding-right:10px;",
            defaultType: 'textfield',
            items:[{
                xtype:'hidden',
                name:'groupid'
                }, this.groupName, this.cmbParent, this.accNature, this.affectGP, this.isCostOfGoodsSoldGroup]
        })
    },
    closeForm:function(){
        this.fireEvent('cancel',this);
         this.close();         
    },

    checkDuplicateName:function(o,newval,oldval){
        this.parentStore.clearFilter(true)
        var FIND = this.groupName.getValue().trim();
        FIND =FIND.replace(/\s+/g, '');
         var index=this.parentStore.findBy( function(rec){
             var parentname=rec.data['parentname'].trim();
             parentname=parentname.replace(/\s+/g, '');
             if(parentname==FIND && !rec.data.deleted) // Add non-deleted record check
                return true;
             else
                return false
        })
        if(index>=0){
            WtfComMsgBox(47,2);
            this.groupName.setValue(oldval)
            return;
        }
    },
    confirmBeforeSave: function () {
        if (Wtf.account.companyAccountPref.propagateToChildCompanies) {

            Wtf.MessageBox.show({
                title: WtfGlobal.getLocaleText("acc.common.confirm"),
                msg: WtfGlobal.getLocaleText({key: "acc.savecustomer.propagate.confirmmessage", params: [" Account Group "]}),
                buttons: Wtf.MessageBox.YESNO,
                icon: Wtf.MessageBox.QUESTION,
                width: 300,
                scope: {
                    scopeObject: this
                },
                fn: function (btn) {
                    if (btn == "yes") {
                        this.scopeObject.ispropagatetochildcompanyflag = true;
                    }
                    this.scopeObject.saveForm();
                }
            }, this);

        } else {
            this.saveForm();
        }
    },   
    saveForm:function(){
    	
        this.loadMask1 = new Wtf.LoadMask(this.id, {msg: WtfGlobal.getLocaleText("acc.msgbox.49"), msgCls: "x-mask-loading acc-customer-form-mask"});
        
        Wtf.getCmp(this.id).on("loadingcomplete",function(){
            if(this.loadMask1){
                this.loadMask1.msg = WtfGlobal.getLocaleText("acc.field.Saved...") ;
                this.loadMask1.hide()
        }
            
        },this);

        var flag1=this.groupForm.getForm().isValid();
//        var flag2=this.validateCard();
        if(!flag1){
            WtfComMsgBox(2,2);
        }else{
            var rec=this.groupForm.getForm().getValues();
            rec.mode=10;
            rec.nature=this.accNature.getValue();
            rec.name=this.groupName.getValue();
            rec.isEdit=this.isEdit;
            rec.ispropagatetochildcompanyflag=this.ispropagatetochildcompanyflag;
            /*
             * isCostOfGoodsSoldGroup flag was missing in below case :
             * When we are saving child account and Parent account have Is Cost Of Goods Sold Flag true. 
             * In this case we are setting value to this.isCostOfGoodsSoldGroup check box also we are disabling this checkbox. 
             * So that when we get values of form by this.groupForm.getForm().getValues() disabled values are not getting.
             * So Is Cost Of Goods Sold was not setting to child account.
             * 
             * Because of this balance sheet was not matching.
             **/
            rec.isCostOfGoodsSoldGroup = this.isCostOfGoodsSoldGroup.getValue();
            this.loadMask1.show();
            Wtf.Ajax.requestEx({
//                url: Wtf.req.account+'CompanyManager.jsp',
                url : "ACCAccount/saveGroup.do",
                params: rec                
            },this,this.genSuccessResponse,this.genFailureResponse);
        }
    },

    genSuccessResponse:function(response){
    	if(this.loadMask1){
            this.loadMask1.hide();
        }
        this.fireEvent("loadingcomplete",this);
    	
//        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg],response.success*2+1);
        if(response.success) this.fireEvent('update',this,response.groupID);
        this.close();
    },

    genFailureResponse:function(response){
    	if(this.loadMask1){
            this.loadMask1.hide();
        }
        this.fireEvent("loadingcomplete",this);
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
        this.close();
    }
});
