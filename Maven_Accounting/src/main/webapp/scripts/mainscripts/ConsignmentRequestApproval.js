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
Wtf.ConsignmentRequestApproval = function(config) {
   this.isrequestapproval=config.isrequestapproval;
   Wtf.apply(this, config);
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.cmodel = new Wtf.grid.ColumnModel([
    {
        header: "",
        dataIndex: 'id',
        hidden:true
    },{
        header: WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalRequestor"),
        width: 250,
        hidden:!this.isrequestapproval,
        dataIndex: 'requestor'
    },{
        header: WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore"),
        width: 250,
        dataIndex: 'store'
    },{
        header: WtfGlobal.getLocaleText("acc.masterConfig.12"),
        width: 250,
        dataIndex: 'location'
    },{
        header: this.isrequestapproval?WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalApprover"):WtfGlobal.getLocaleText("acc.field.ConsignmentQA"),
        width: 250,
        dataIndex: 'approver'
    }
    ]);

    this.gridRecord = Wtf.data.Record.create([{
        name: 'id'
    },{
        name: 'requestorid'
    },{
        name: 'requestor'
    },{
        name: 'storeid'
    },{
        name: 'store'
    },{
        name: 'locationid'
    },{
        name: 'location'
    },{
        name: 'approverid'
    },{
        name: 'approver'
    }
    ]);
    this.gridReader = new Wtf.data.KwlJsonReader({
        root: "data",
        totalProperty:"count"
    }, this.gridRecord);
    this.gridStore = new Wtf.data.Store({
        proxy: new Wtf.data.HttpProxy({
            url: "ACCSalesOrder/getConsignmentApprovalRules.do"
        }),
        reader: this.gridReader,
         baseParams:{
             isrequestapproval:this.isrequestapproval
         }
       
    });
   

    this.grid=new Wtf.grid.GridPanel({
        id:'ConsignmentRequestApprovalGrid'+this.id,
        ds: this.gridStore,
        cm: this.cmodel,
        border: false,
        layout:'fit',
        enableColumnHide: false,
        sm: this.sm,
        trackMouseOver: true,
        loadMask: {
            msg: WtfGlobal.getLocaleText("acc.msgbox.50")
        },
        viewConfig: {
            forceFit: true
        }
    });
    
    
    this.usersRec = new Wtf.data.Record.create([
    {
        name: 'userid'
    },

    {
        name: 'username'
    },

    {
        name: 'fname'
    },

    {
        name: 'lname'
    }
    ]);
    this.userds = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            totalProperty: 'count',
            root: "data"
        },this.usersRec),
        url : this.isrequestapproval ? "ACCAccountCMN/getAllConsignmentRequestApproveRejectPermittedUserList.do" : "ACCAccountCMN/getAllQAApproveRejectPermittedUserList.do"
    });    
    this.userds.load();
    
    
    this.requestorStoreRec = new Wtf.data.Record.create([
    {
        name: 'userid'
    },

    {
        name: 'username'
    },

    {
        name: 'fname'
    },

    {
        name: 'lname'
    }
    ]);
    

    this.requestorStore = new Wtf.data.Store({
        reader: new Wtf.data.KwlJsonReader({
            totalProperty: 'count',
            root: "data"
        },this.requestorStoreRec),
        url : "ProfileHandler/getAllUserDetails.do"
    });
    
    this.requestorStore.load();
    
    this.requestorCombo = new Wtf.form.ComboBox({
        triggerAction:'all',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalRequestor")+'*',
        valueField:'userid',
        displayField:'fname',
        store:this.requestorStore,
        lastQuery:'',
        allowBlank: false,
        typeAhead: true,
        forceSelection: true,
        name:'requestor',
        hiddenName:'requestor',
        width: 250
    });


    this.wareHouseRec = new Wtf.data.Record.create([
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
    }
    ]);
    this.wareHouseReader = new Wtf.data.KwlJsonReader({
        root:"data"
    },this.wareHouseRec);
    this.wareHouseStore = new Wtf.data.Store({
        url:"ACCMaster/getWarehouseItems.do",
        reader:this.wareHouseReader,
        baseParams:{
            customerid:this.customerID,
            isForCustomer:this.isForCustomer,
            includeQAAndRepairStore:true,
            includePickandPackStore:true,
            movementtypeid:(this.movmentType!=undefined && this.movmentType !="")?this.movmentType:""
        }
    });
    this.wareHouseStore.load();

    this.wareHouseCombo = new Wtf.form.ComboBox({
        triggerAction:'all',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalStore")+'*',
        valueField:'id',
        displayField:'name',
        store:this.wareHouseStore,
        lastQuery:'',
        allowBlank: false,
        typeAhead: true,
        forceSelection: true,
        name:'warehouse',
        hiddenName:'warehouse',
        width: 250
    });

    if(Wtf.account.companyAccountPref.activateInventoryTab){
        this.wareHouseCombo.on('select',function(){
            this.locationStore.load({
                params:{
                    storeid:this.wareHouseCombo.getValue()
                }
            });
            this.locationMultiSelect.enable();
        },this);
    }
        
    this.locationRec = new Wtf.data.Record.create([
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
    }
    ]);
    this.locationReader = new Wtf.data.KwlJsonReader({
        root:"data"
    },this.locationRec);
    var locationStoreUrl="ACCMaster/getLocationItems.do"
    if(Wtf.account.companyAccountPref.activateInventoryTab){
        locationStoreUrl="ACCMaster/getLocationItemsFromStore.do";
    }
    this.locationStore = new Wtf.data.Store({
        url:locationStoreUrl,
        reader:this.locationReader
    });
    this.locationStore.load();

    this.locationMultiSelect = new Wtf.common.Select({
        triggerAction:'all',
        mode: 'local',
        multiSelect:true,
        fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.12")+'*',
        valueField:'id',
        displayField:'name',
        lastQuery:'',
        store:this.locationStore,
        typeAhead: true,
        allowBlank: false,
        disabled:true,
        forceSelection: true,
        hirarchical:true,
        name:'location',
        hiddenName:'location',
        width: 250
    });


    this.ApproverCombo = new Wtf.common.Select({
        triggerAction:'all',
        mode: 'local',
        multiSelect:true,
        fieldLabel:this.isrequestapproval?WtfGlobal.getLocaleText("acc.field.ConsignmentRequestApprovalApprover")+'*':WtfGlobal.getLocaleText("acc.field.ConsignmentQA")+'*',
        valueField:'userid',
        displayField:'fname',
        store:this.userds,
        lastQuery:'',
        typeAhead: true,
        allowBlank: false,
        forceSelection: true,
        hirarchical:true,
        name:'approver',
        hiddenName:'approver',
        width: 250
    });
    
    
    this.newSuBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.submit"),
        iconCls :getButtonIconCls(Wtf.etype.save),
        tooltip :WtfGlobal.getLocaleText("acc.field.Submitthecurrentrule"),
        id: 'ConsignmentRequestApprovalBtnSubNew' + this.id,
        scope: this
    });
    this.newSuBttn.on('click',this.clickHandle,this);

    this.NewRuleBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.New"),
        iconCls :getButtonIconCls(Wtf.etype.add),
        tooltip :WtfGlobal.getLocaleText("acc.field.Newrule"),
        id: 'ConsignmentRequestApprovalBtnNew1' + this.id
    });
    this.NewRuleBttn.on('click',this.NewRule,this);

    this.DeleteBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
        iconCls :getButtonIconCls(Wtf.etype.deletebutton),
        tooltip :WtfGlobal.getLocaleText("acc.field.Deleteselectedrule"),
        id: 'ConsignmentRequestApprovalBtnDel' + this.id,
        scope: this,
        disabled:true
    });
    this.DeleteBttn.on('click',this.deleteMessage,this);

    this.EditBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.UpdateRule"),
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        tooltip :WtfGlobal.getLocaleText("acc.field.Updateselectedrule"),
        scope: this,
        disabled:true
    });
    this.EditBttn.on('click',this.editRule,this);
    
    var btnArr=[];
    if(this.isrequestapproval==true){
        btnArr.push(this.requestorCombo);
    }
    btnArr.push(this.wareHouseCombo);
    btnArr.push(this.locationMultiSelect);
    btnArr.push(this.ApproverCombo);
    Wtf.ConsignmentRequestApproval.superclass.constructor.call(this,{
        autoDestroy:true,
        border: false,
        layout :'border',
        items:[{
            title : WtfGlobal.getLocaleText("acc.field.Rule"),
            paging : false,
            autoLoad : false,
            region:"north",
            height:200,
            bodyStyle : "background:#f0f0f0;",
            border: false,
            bbar:[this.NewRuleBttn,this.DeleteBttn,this.newSuBttn,this.EditBttn],
            layout:"fit",
            items: [
            {
                border:false,
                layout:'form',
                bodyStyle:'padding:13px 13px 13px 13px',
                labelWidth:160,
                items:btnArr
            }]
        },{
            title : WtfGlobal.getLocaleText("acc.field.ApprovalRules"),
            paging : false,
            autoLoad : false,
            region:"center",
            layout:'fit',
            border: false,
            bbar:this.pg = new Wtf.PagingSearchToolbar({
                id: 'pgTbarModule' + this.id,
                pageSize: 15,
                store: this.gridStore,
                displayInfo: true,
                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
                plugins: this.pP3 = new Wtf.common.pPageSize({})
            }),
            items:this.grid
        }]
    });
    this.on('render',this.handleRender,this);
    this.on('show',this.handleshow,this);
    this.grid.on("rowclick", this.rowClickHandle, this);
}

Wtf.extend(Wtf.ConsignmentRequestApproval, Wtf.Panel, {
    handleshow:function(){
        this.items.items[0].ownerCt.doLayout();
    },

    handleRender:function() {
        this.gridStore.load({
            params:{
                start:0,
                limit:15,
                isrequestapproval:this.isrequestapproval
                
            }
        });
    },
    rowClickHandle:function(grid, rowIndex, e){
        var recData=this.gridStore.getAt(rowIndex).data;
        if(this.isrequestapproval==true){
            this.requestorCombo.setValue(recData.requestorid);
        }
        this.wareHouseCombo.setValue(recData.storeid);
        this.locationMultiSelect.setValue(recData.locationid);
        this.ApproverCombo.setValue(recData.approverid);
        
        this.DeleteBttn.enable();
        this.EditBttn.enable();
        this.newSuBttn.disable();
    },
    
    clickHandle:function(){
        if(this.isrequestapproval==true){
            if(this.requestorCombo.getValue() == "" || this.wareHouseCombo.getValue() == "" || this.locationMultiSelect.getValue() == "" || this.ApproverCombo.getValue() == ""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.tar.required")], 2);//Please fill required fields first.
                return;
            }
         }else{//this.requestorCombo.getValue() == "" || 
             if( this.wareHouseCombo.getValue() == "" || this.locationMultiSelect.getValue() == "" || this.ApproverCombo.getValue() == ""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.tar.required")], 2);//Please fill required fields first.
                return;
            }
         }
        
            Wtf.Ajax.requestEx({
                url:"ACCSalesOrder/saveConsignmentApprovalRules.do",
                params: {
                    id: '',
                    ruleName: '',
                    requestor: this.isrequestapproval?this.requestorCombo.getValue():"",
                    isrequestapproval:this.isrequestapproval,
                    warehouse: this.wareHouseCombo.getValue(),
                    locations: this.locationMultiSelect.getValue(),
                    approver: this.ApproverCombo.getValue()
                }
            },this,
            function(resp){
                if(resp.success == true) {
                    if(resp.isDuplicateflag==1 || resp.isDuplicateflag==-1){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), resp.msg], 0);
                        this.NewRule();
                        this.gridStore.load({
                            params:{
                                start:0,
                                limit:15,
                                isrequestapproval:this.isrequestapproval
                            }
                        });
                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), resp.msg], 2);
                    }
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"), WtfGlobal.getLocaleText("acc.field.Approvalmessagenotsavesucessfully")], 1);
                }

            },function(){
            
                });
            },

    NewRule :function() {
        this.grid.getSelectionModel().clearSelections();
        
        if(this.isrequestapproval==true){
            this.requestorCombo.setValue("");
            this.requestorCombo.clearInvalid();
        }
        this.wareHouseCombo.setValue("");
        this.wareHouseCombo.clearInvalid();
        this.locationMultiSelect.setValue("");
        this.locationMultiSelect.clearInvalid();
        this.ApproverCombo.setValue("");
        this.ApproverCombo.clearInvalid();
        
        this.DeleteBttn.disable();
        this.EditBttn.disable();
        this.NewRuleBttn.enable();
        this.newSuBttn.enable();
    },

    deleteMessage: function(obj, e){
        Wtf.Msg.show({
            title:WtfGlobal.getLocaleText("acc.field.DeleteRule"),
            msg: WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttodeleteselectedrule"),
            buttons: Wtf.Msg.YESNO,
            fn: this.confirmDelete,
            scope:this,
            animEl: 'elId',
            icon: Wtf.MessageBox.QUESTION
        });
    },

    confirmDelete:function(btn, text){
        if(btn=="yes" && this.grid.getSelections().length>0) {
            var rec = this.grid.getSelectionModel().getSelected();
            Wtf.Ajax.requestEx({
                url:'ACCSalesOrder/deleteConsignmentApprovalRules.do',
                params:{
                    id:rec.data.id
                },
                method:'POST'
            },
            this,
            function(resp){
                if(resp.success){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),resp.msg],2+1);
                    this.NewRule();
                            
                    this.gridStore.load({
                        params:{
                            start:0,
                            limit:15,
                            isrequestapproval:this.isrequestapproval
                        }
                    });
                            
                    this.NewRule();
                         
                } else if(!resp.success && resp.msg != null) {
                    msgBoxShow([WtfGlobal.getLocaleText("acc.common.error"), resp.msg], Wtf.MessageBox.ERROR);
                }
            });
            this.DeleteBttn.disable();
            this.EditBttn.disable();
        }
    },
    
    editRule : function(obj, e){
        
         if(this.isrequestapproval==true){
             if(this.requestorCombo.getValue() == "" || this.wareHouseCombo.getValue() == "" || this.locationMultiSelect.getValue() == "" || this.ApproverCombo.getValue() == ""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.tar.required")], 2);//Please fill required fields first.
                return;
            }
         }else{//this.requestorCombo.getValue() == "" ||
            if( this.wareHouseCombo.getValue() == "" || this.locationMultiSelect.getValue() == "" || this.ApproverCombo.getValue() == ""){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.information"), WtfGlobal.getLocaleText("acc.tar.required")], 2);//Please fill required fields first.
                return;
            }
         }
        

        if(this.grid.getSelections().length>0){
            var rec = this.grid.getSelectionModel().getSelected();
        }
        
        Wtf.Ajax.requestEx({
            url:"ACCSalesOrder/saveConsignmentApprovalRules.do",
            params:{
                id: rec.data.id,
                ruleName: '',
                isrequestapproval:this.isrequestapproval,
                requestor: this.isrequestapproval?this.requestorCombo.getValue():"",
                warehouse: this.wareHouseCombo.getValue(),
                locations: this.locationMultiSelect.getValue(),
                approver: this.ApproverCombo.getValue()
            }
        },
        this,
        function(resp){
            if(resp.success){
                if(resp.isDuplicateflag==1 || resp.isDuplicateflag==-1){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), resp.msg], 0);
                    this.NewRule();
                    this.gridStore.load({
                        params:{
                            start:0,
                            limit:15,
                            isrequestapproval:this.isrequestapproval
                        }
                    });
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), resp.msg], 2);
                }
            }                     
        });
    }
});