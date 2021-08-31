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

Wtf.MachineManRatio = function(config) {
   this.pageSize=15;
   Wtf.apply(this, config);

//    btnArr.push(this.ruleTypeCombo);
//    btnArr.push(this.minAnnualIncome);
//    btnArr.push(this.maxAnnualIncome);
//    btnArr.push(this.multipleofSalary);
//    
//    this.disbursementForm=new Wtf.form.FormPanel({
//        border: false,
//        layout :'border',
//        items:[{
//            title : WtfGlobal.getLocaleText("acc.loan.eligibilityrule.create"),
//            paging : false,
//            autoLoad : false,
//            region:"north",
//            height:200,
//            bodyStyle : "background:#f0f0f0;",
//            border: false,
//            bbar:[this.NewBttn,this.DeleteBttn,this.newSuBttn,this.EditBttn],
//            layout:"fit",
//            items: [{
//                border:false,
//                layout:'form',
//                bodyStyle:'padding:13px 13px 13px 13px',
//                labelWidth:160,
//                items:btnArr
//            }]
//        },
//        {
//            title : WtfGlobal.getLocaleText("acc.loan.eligibilityrule.create"),
//            paging : false,
//            autoLoad : false,
//            region:"center",
//            layout:'fit',
//            border: false,
//            bbar:this.pg = new Wtf.PagingSearchToolbar({
//                id: 'pgTbarModule' + this.id,
//                pageSize: this.pageSize,
//                store: this.gridStore,
//                displayInfo: true,
//                emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
//                plugins: this.pP3 = new Wtf.common.pPageSize({})
//            }),
//            items:this.grid
//        }]
//
//    });
 
  
    Wtf.MachineManRatio.superclass.constructor.call(this, config);
    
}
Wtf.extend(Wtf.MachineManRatio, Wtf.Panel, {
    onRender:function(config){
    Wtf.MachineManRatio.superclass.onRender.call(this, config);
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.cmodel = new Wtf.grid.ColumnModel([
    {
        header: "",
        dataIndex: 'id',
        hidden:true
    },{
        header: WtfGlobal.getLocaleText("acc.bankReconcile.import.grid.Date"),
        width: 250,
        dataIndex: 'dateForRatio'
    },{
        header: WtfGlobal.getLocaleText("acc.machineManReport.machine"),
        width: 250,
        dataIndex: 'machinename'
    },{
        header: WtfGlobal.getLocaleText("acc.machineManReport.fullTime"),
        width: 250,
        dataIndex: 'fulltimeratio'
    },{
        header: WtfGlobal.getLocaleText("acc.machineManReport.partTime"),
        width: 250,
        dataIndex: 'parttimeratio'
    }
    ]);

    this.gridRecord = Wtf.data.Record.create([{
        name: 'id'
    },{
        name: 'dateForRatio'
    },{
        name: 'machine'
    },{
        name: 'machinename'
    },{
        name: 'fullMachineTime'
    },{
        name: 'fullManTime'
    },{
        name: 'partMachineTime'
    },{
        name: 'partManTime'
    },{
        name: 'fulltimeratio'
    },{
        name: 'parttimeratio'
    }
    ]);
    this.gridReader = new Wtf.data.KwlJsonReader({
        root: "data",
        totalProperty:"count"
    }, this.gridRecord);
    this.gridStore = new Wtf.data.Store({
        proxy: new Wtf.data.HttpProxy({
            url: "ACCMachineMaster/getMachineManRatio.do"
        }),
        reader: this.gridReader

    });
   

    this.grid=new Wtf.grid.GridPanel({
        id:'MachineManRatioGrid'+this.id,
        ds: this.gridStore,
        cm: this.cmodel,
        border: false,
        layout:'fit',
        enableColumnHide: false,
        sm: this.sm,
        trackMouseOver: true,
        height: 280,
        loadMask: {
            msg: WtfGlobal.getLocaleText("acc.msgbox.50")
        },
        viewConfig: {
            forceFit: true
        }
    });
    
    this.loadStore();
    
      this.machineRec = Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'name'
        }
        ]);

    this.machineStore = new Wtf.data.Store({
        url: "ACCMachineMaster/getMachinesForCombo.do",
        reader: new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.machineRec)
    });
    this.machineCombo = new Wtf.form.ExtFnComboBox({
        fieldLabel: WtfGlobal.getLocaleText("acc.machine.machineid"),
        name: 'machine',
        hiddenName: 'machine',
        store: this.machineStore,
        valueField: 'id',
        displayField: 'name',
        mode: 'local',
        allowBlank: false,
        typeAhead: true,
        triggerAction: 'all',
        width:200,
        extraFields:[],
        addCreateOpt:false
    });
    
   
    this.dateForRatio = new Wtf.form.DateField({  // Date 
        fieldLabel: WtfGlobal.getLocaleText("acc.machineMasterGrid.header9")+"*",
        name: 'dateForRatio',
        hiddenName: 'dateofinstallation',
        format: WtfGlobal.getOnlyDateFormat(),
        maxLength: 255,
        width: 200,
        allowBlank: false,
//            anchor: '75%',
        scope: this
    });

     
          this.fullMachineTime = new Wtf.form.TextField({  //Machine Time  
            fieldLabel: WtfGlobal.getLocaleText("acc.machineManRatio.machineTime")+"*",
            emptyText: WtfGlobal.getLocaleText("acc.machineManReport.machineWorkingTime"),
            name: 'fullMachineTime',
            width:200,
            maxLength: 50,
            scope: this,
            allowBlank: false
        });
        
         this.fullManTime = new Wtf.form.TextField({  //Man Time  
            fieldLabel: WtfGlobal.getLocaleText("acc.machineManRatio.manTime")+"*",
            name: 'fullManTime',
            width:200,
            maxLength: 50,
            scope: this,
            allowBlank: false
        });
     
        this.partMachineTime = new Wtf.form.TextField({  //Machine Time  
            fieldLabel: WtfGlobal.getLocaleText("acc.machineManRatio.machineTime")+"*",
            emptyText: WtfGlobal.getLocaleText("acc.machineManReport.machineWorkingTime"),
            name: 'partMachineTime',
            width:200,
            maxLength: 50,
            scope: this,
            allowBlank: false
        });
        
         this.partManTime = new Wtf.form.TextField({  //Man Time  
            fieldLabel: WtfGlobal.getLocaleText("acc.machineManRatio.manTime")+"*",
            name: 'partManTime',
            width:200,
            maxLength: 50,
            scope: this,
            allowBlank: false
        });
     
     
      
      
    this.fullTimeRatioSet = new Wtf.form.FieldSet({
        title: WtfGlobal.getLocaleText("acc.machineManRatio.fullTime"),
        width: 450,
        checkboxName: 'fullTimeRatio',
        style: 'margin-right:30px',
        items: [ this.fullMachineTime,this.fullManTime]
    });
        
    this.partTimeRatioSet = new Wtf.form.FieldSet({
        title: WtfGlobal.getLocaleText("acc.machineManRatio.partTime"),
        width: 450,
        checkboxName: 'partTimeRatio',
        style: 'margin-right:30px',
        items: [this.partMachineTime,this.partManTime]
    });


      
        
    this.newSuBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.submit"),
        iconCls :getButtonIconCls(Wtf.etype.save),
        tooltip :WtfGlobal.getLocaleText("acc.field.Submitthecurrentrule"),
        id: 'MachineManRatioBtnSubNew' + this.id,
        scope: this
    });
    this.newSuBttn.on('click',this.clickHandle,this);

    this.NewBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.New"),
        iconCls :getButtonIconCls(Wtf.etype.add),
        tooltip :WtfGlobal.getLocaleText("acc.field.Newrule"),
        id: 'MachineManRatioBtnNew1' + this.id
    });
    this.NewBttn.on('click',this.NewRule,this);

    this.DeleteBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
        iconCls :getButtonIconCls(Wtf.etype.deletebutton),
        tooltip :WtfGlobal.getLocaleText("acc.field.Deleteselectedrule"),
        id: 'MachineManRatioBtnDel' + this.id,
        scope: this,
        disabled:true
    });
    this.DeleteBttn.on('click',this.deleteMessage,this);

    this.EditBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.update"),
        iconCls :getButtonIconCls(Wtf.etype.resetbutton),
        tooltip :WtfGlobal.getLocaleText("acc.field.Edit&Update"),
        scope: this,
        disabled:true
    });
    this.EditBttn.on('click',this.editRule,this);
    this.grid.on("rowclick", this.rowClickHandle, this);
    this.machineStore.load();
       var btnArr=[];
        btnArr.push(this.dateForRatio);
        btnArr.push(this.machineCombo);
        btnArr.push(this.fullTimeRatioSet);
        btnArr.push(this.partTimeRatioSet);
        
        this.machineManForm=new Wtf.form.FormPanel({
                id:"machineManForm"+this.id,
                autoHeight: true,        
                defaults:{
                    border:false
                },
                items:[{
                    title : WtfGlobal.getLocaleText("acc.machineManReport.subTitletip"),
                    paging : false,
                    autoLoad : false,
                    region:"north",
                    height:300,
                    bodyStyle : "background:#f0f0f0;",
                    border: false,
                    bbar:[this.NewBttn,this.DeleteBttn,this.newSuBttn,this.EditBttn],
                    layout:"fit",
                    items: [{
                        border:false,
                        layout:'form',
                        bodyStyle:'padding:13px 13px 13px 13px',
                        labelWidth:130,
                        items:btnArr
                    }]
                },
                {
                    //            title : WtfGlobal.getLocaleText("acc.machineManReport.subTitletip"),
                    paging : false,
                    autoLoad : false,
                    region:"center",  
                    layout:'fit',
                    border: false,
                    bbar:this.pg = new Wtf.PagingSearchToolbar({
                        id: 'pgTbarModule' + this.id,
                        pageSize: this.pageSize,
                        store: this.gridStore,
                        displayInfo: true,
                        emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"),
                        plugins: this.pP3 = new Wtf.common.pPageSize({})
                    }),
                    items:this.grid
                }]
        });

        this.newPanel = new Wtf.Panel({
            autoScroll: true,
            bodyStyle: ' background: none repeat scroll 0 0 #DFE8F6;',
            region: 'center',
            items: [this.machineManForm]
//            bbar: this.btnArr

        }); 
        this.newPanel.on("resize",function(){
            this.newPanel.doLayout();
        },this);
        this.add(this.newPanel);    
        
        this.on('show',this.handleshow,this);
    },
    handleshow:function(){
        this.items.items[0].ownerCt.doLayout();
    },

    loadStore:function() {
        this.gridStore.load({
            params:{
                start:0,
                limit:this.pageSize
                
            }
        });
    },
    rowClickHandle:function(grid, rowIndex, e){
        var recData=this.gridStore.getAt(rowIndex).data;
        this.machineCombo.setValue(recData.machine);
        this.dateForRatio.setValue(recData.dateForRatio);
        this.fullMachineTime.setValue(recData.fullMachineTime);
        this.fullManTime.setValue(recData.fullManTime);
        this.partMachineTime.setValue(recData.partMachineTime);
        this.partManTime.setValue(recData.partManTime);
//        this.UpdateLabel();
        this.DeleteBttn.enable();
        this.EditBttn.enable();
        this.NewBttn.disable();
        this.newSuBttn.disable();
    }, 
    clickHandle:function(){
        
        var isValidNorthForm = this.machineManForm.getForm().isValid();
        if (!isValidNorthForm) {
            WtfComMsgBox(2, 2);
            return
        }
//           var lowerlimit=this.minAnnualIncome.getValue()*1;
//            var upperlimit=this.maxAnnualIncome.getValue()*1;
//            if(lowerlimit==0 ||upperlimit==0){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.loan.eligibilityrule.zerominmaxincome")], 2);
//                return;
//            }
//            if(upperlimit < lowerlimit){
//                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.loan.eligibilityrule.Addfailure")],2);
//                return;
//            } 
//            if(this.gridStore.getCount()>0){
//                for(var i=0;i<this.gridStore.getCount();i++){//Limit Validation
//                    var rec=this.gridStore.data.get(i);
//                        if((lowerlimit==rec.data.minannualincome ||lowerlimit > rec.data.minannualincome && lowerlimit <= rec.data.maxannualincome)){
////                                        lowerlimit <= rec.data.upperlimit, Added = because Both dates are Inclusive.
//                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.loan.eligibilityrule.annualincomeError")],1);
//                            return;
//                        }
//                }
//            }
            
            Wtf.Ajax.requestEx({
                url:"ACCMachineMaster/saveMachineManRatio.do",
                params: {
                    
                    id: '',
                    machine:this.machineCombo.getValue(),
                    dateForRatio:WtfGlobal.convertToGenericDate(this.dateForRatio.getValue()),
                    fullMachineTime:this.fullMachineTime.getValue(),
                    fullManTime:this.fullManTime.getValue(),
                    partMachineTime:this.partMachineTime.getValue(),
                    partManTime:this.partManTime.getValue()
                    
                    
                }
            },this,
            function(resp){
                if(resp.success == true) {
                    if(resp.isDuplicateflag==1 || resp.isDuplicateflag==-1){
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), resp.msg], 0);
                        this.NewRule();
                        this.loadStore();
                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), resp.msg], 2);
                    }
                } else {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"), WtfGlobal.getLocaleText("acc.field.Errorwhilesavingdata")], 1);
                }

            },function(){
            
                });
            },
            
//    UpdateLabel: function(){ //writen for Label Change
//          if(this.ruleTypeCombo.getValue()==Wtf.LoanManagement_Constant.ABSOLUTEVALUE){
//              WtfGlobal.showFormElement(this.multipleofSalary);
//              WtfGlobal.updateFormLabel(this.multipleofSalary,'Loan Amount Limit :');//WtfGlobal.getLocaleText("acc.masterconfig.percentageValue")
//          } else if(this.ruleTypeCombo.getValue()== Wtf.LoanManagement_Constant.MULTIPLEOFSALARY){
//              WtfGlobal.showFormElement(this.multipleofSalary);
//              WtfGlobal.updateFormLabel(this.multipleofSalary,'Multiple of Salary :'); //WtfGlobal.getLocaleText("acc.masterconfig.percentageValue")
//          }else if(this.ruleTypeCombo.getValue()== Wtf.LoanManagement_Constant.UNLIMITED){
//              WtfGlobal.hideFormElement(this.multipleofSalary);
//          }
//    },
    NewRule :function() {
        this.grid.getSelectionModel().clearSelections();

//        this.ruleTypeCombo.setValue("");
//        this.ruleTypeCombo.clearInvalid();
//        this.minAnnualIncome.setValue("");
//        this.maxAnnualIncome.setValue("");
//        this.multipleofSalary.setValue("");
        
        this.DeleteBttn.disable();
        this.EditBttn.disable();
        this.NewBttn.enable();
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
                url:'ACCMachineMaster/deleteMachineManRatio.do',
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
                    this.loadStore();
                         
                } else if(!resp.success && resp.msg != null) {
                    msgBoxShow([WtfGlobal.getLocaleText("acc.common.error"), resp.msg], Wtf.MessageBox.ERROR);
                }
            });
            this.DeleteBttn.disable();
            this.EditBttn.disable();
        }
    },
    
    editRule : function(obj, e){
        if(this.grid.getSelections().length>0){
            var record = this.grid.getSelectionModel().getSelected();
        }
        
        var isValidNorthForm = this.machineManForm.getForm().isValid();
        if (!isValidNorthForm) {
            WtfComMsgBox(2, 2);
            return
        } 
        
        Wtf.Ajax.requestEx({
           url:"ACCMachineMaster/saveMachineManRatio.do",
            params:{
                id: record.data.id,
                machine:this.machineCombo.getValue(),
                dateForRatio:WtfGlobal.convertToGenericDate(this.dateForRatio.getValue()),
                fullMachineTime:this.fullMachineTime.getValue(),
                fullManTime:this.fullManTime.getValue(),
                partMachineTime:this.partMachineTime.getValue(),
                partManTime:this.partManTime.getValue()
            }
        },
        this,
        function(resp){
            if(resp.success){
                if(resp.isDuplicateflag==1 || resp.isDuplicateflag==-1){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), resp.msg], 0);
                    this.NewRule();
                    this.loadStore();
                }else{
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), resp.msg], 2);
                }
            }                     
        });
    }
});