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
Wtf.LoanManagement_Constant = {
    ABSOLUTEVALUE:0,
    MULTIPLEOFSALARY:1,
    UNLIMITED:2
};
Wtf.ManageEligibilityRules = function(config) {
   this.pageSize=15;
   Wtf.apply(this, config);
    this.sm = new Wtf.grid.CheckboxSelectionModel();
    this.cmodel = new Wtf.grid.ColumnModel([
    {
        header: "",
        dataIndex: 'id',
        hidden:true
    },{
        header: WtfGlobal.getLocaleText("acc.loan.field.ruleType"),
        width: 250,
        dataIndex: 'ruletypename'
    },{
        header: WtfGlobal.getLocaleText("acc.loan.field.minAnnualincome"),
        width: 250,
        dataIndex: 'minannualincome'
    },{
        header: WtfGlobal.getLocaleText("acc.loan.field.maxAnnualincome"),
        width: 250,
        dataIndex: 'maxannualincome'
    },{
        header: WtfGlobal.getLocaleText("acc.loan.field.eligibility"),
        width: 250,
        dataIndex: 'eligibility'
    }
    ]);

    this.gridRecord = Wtf.data.Record.create([{
        name: 'id'
    },{
        name: 'ruletype'
    },{
        name: 'ruletypename'
    },{
        name: 'minannualincome'
    },{
        name: 'maxannualincome'
    },{
        name: 'eligibility'
    }
    ]);
    this.gridReader = new Wtf.data.KwlJsonReader({
        root: "data",
        totalProperty:"count"
    }, this.gridRecord);
    this.gridStore = new Wtf.data.Store({
        proxy: new Wtf.data.HttpProxy({
            url: "ACCLoanCMN/getLoanRules.do"
        }),
        reader: this.gridReader

    });
   

    this.grid=new Wtf.grid.GridPanel({
        id:'ManageEligibilityRulesGrid'+this.id,
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
    
    this.ruleTypeStore = new Wtf.data.SimpleStore({
        fields:[{
            name:'id'
        },{
            name:'name'
        }],
        data:[[Wtf.LoanManagement_Constant.ABSOLUTEVALUE,'Absolute Value'],[Wtf.LoanManagement_Constant.MULTIPLEOFSALARY,'Multiple of Annual Salary'],[Wtf.LoanManagement_Constant.UNLIMITED,'Unlimited']]
    });
        
        
    this.ruleTypeCombo = new Wtf.form.ComboBox({
        triggerAction: 'all',
        mode: 'local',
        valueField: 'id',
        displayField: 'name',
        store: this.ruleTypeStore,
        fieldLabel: 'Rule Type',
        typeAhead: true,
        emptyText: WtfGlobal.getLocaleText("acc.product.UpdatePriceRule.RuleTypeCombo"),
        forceSelection: true,
        width:250,
        hiddenName: 'type'                
    });    
    
    this.minAnnualIncome=new Wtf.form.TextField({
        name:"minannualincome",
        id:"minannualincome"+this.id,
        fieldLabel:'Minimum Annual Income',            //WtfGlobal.getLocaleText("acc.field.PaymentAccount"),
        width: 250
    });
    this.maxAnnualIncome=new Wtf.form.TextField({
        name:"maxannualincome",
        id:"maxannualincome"+this.id,
        fieldLabel:'Maximum Annual Income',   //WtfGlobal.getLocaleText("acc.field.PaymentAccount"),
        width: 250
    });
    
    this.multipleofSalary = new Wtf.form.NumberField({
        fieldLabel:'Loan Amount Limit',                           //WtfGlobal.getLocaleText("acc.masterconfig.setupperlimitValue"),
        name: "multipleofsalary",
        width: 250,
        labelWidth:200,
        maxLength:50,
        hidden:false,
        hideLabel:false,
        allowBlank: false

    });
        
    this.ruleTypeCombo.on('select',function(){
        this.UpdateLabel();
    },this);   
      
        
    this.newSuBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.common.submit"),
        iconCls :getButtonIconCls(Wtf.etype.save),
        tooltip :WtfGlobal.getLocaleText("acc.field.Submitthecurrentrule"),
        id: 'ManageEligibilityRulesBtnSubNew' + this.id,
        scope: this
    });
    this.newSuBttn.on('click',this.clickHandle,this);

    this.NewRuleBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.New"),
        iconCls :getButtonIconCls(Wtf.etype.add),
        tooltip :WtfGlobal.getLocaleText("acc.field.Newrule"),
        id: 'ManageEligibilityRulesBtnNew1' + this.id
    });
    this.NewRuleBttn.on('click',this.NewRule,this);

    this.DeleteBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.setupWizard.gridDelete"),
        iconCls :getButtonIconCls(Wtf.etype.deletebutton),
        tooltip :WtfGlobal.getLocaleText("acc.field.Deleteselectedrule"),
        id: 'ManageEligibilityRulesBtnDel' + this.id,
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
    
    var tBtnArr=[];
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.loanDisbursement, Wtf.Perm.loanDisbursement.ruleEdit)) { 
        tBtnArr.push(this.NewRuleBttn);
    }
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.loanDisbursement, Wtf.Perm.loanDisbursement.ruleDelete)) { 
        tBtnArr.push(this.DeleteBttn);
    }
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.loanDisbursement, Wtf.Perm.loanDisbursement.ruleSubmit)) { 
        tBtnArr.push(this.newSuBttn);
    }
    if(!WtfGlobal.EnableDisable(Wtf.UPerm.loanDisbursement, Wtf.Perm.loanDisbursement.ruleupdate)) { 
        tBtnArr.push(this.EditBttn);
    }
    
    var btnArr=[];
    
    
    btnArr.push(this.ruleTypeCombo);
    btnArr.push(this.minAnnualIncome);
    btnArr.push(this.maxAnnualIncome);
    btnArr.push(this.multipleofSalary);
    
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
//            bbar:[this.NewRuleBttn,this.DeleteBttn,this.newSuBttn,this.EditBttn],
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
 
    Wtf.ManageEligibilityRules.superclass.constructor.call(this,{
        autoDestroy:true,
        border: false,
        layout :'border',
        items:[  //this.disbursementForm,
            {
            title : WtfGlobal.getLocaleText("acc.loan.eligibilityrule.create"),
            paging : false,
            autoLoad : false,
            region:"north",
            height:200,
            bodyStyle : "background:#f0f0f0;",
            border: false,
            bbar:tBtnArr,
            layout:"fit",
            items: [{
                border:false,
                layout:'form',
                bodyStyle:'padding:13px 13px 13px 13px',
                labelWidth:160,
                items:btnArr
            }]
        },
        {
            title : WtfGlobal.getLocaleText("acc.loan.eligibilityrule.create"),
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
    this.on('render',this.loadStore,this);
    this.on('show',this.handleshow,this);
    this.grid.on("rowclick", this.rowClickHandle, this);
}

Wtf.extend(Wtf.ManageEligibilityRules, Wtf.Panel, {
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
        this.multipleofSalary.setValue(recData.eligibility);
        this.minAnnualIncome.setValue(recData.minannualincome);
        this.maxAnnualIncome.setValue(recData.maxannualincome);
        this.ruleTypeCombo.setValue(recData.ruletype);
        this.UpdateLabel();
        this.DeleteBttn.enable();
        this.EditBttn.enable();
        this.newSuBttn.disable();
    }, 
    clickHandle:function(){
           var lowerlimit=this.minAnnualIncome.getValue()*1;
            var upperlimit=this.maxAnnualIncome.getValue()*1;
            if(lowerlimit==0 ||upperlimit==0){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.loan.eligibilityrule.zerominmaxincome")], 2);
                return;
            }
            if(upperlimit < lowerlimit){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.loan.eligibilityrule.Addfailure")],2);
                return;
            } 
            if(this.gridStore.getCount()>0){
                for(var i=0;i<this.gridStore.getCount();i++){//Limit Validation
                    var rec=this.gridStore.data.get(i);
                        if((lowerlimit==rec.data.minannualincome ||lowerlimit > rec.data.minannualincome && lowerlimit <= rec.data.maxannualincome)){
//                                        lowerlimit <= rec.data.upperlimit, Added = because Both dates are Inclusive.
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.loan.eligibilityrule.annualincomeError")],2);
                            return;
                        }
                }
            }
            
            Wtf.Ajax.requestEx({
                url:"ACCLoanCMN/saveLoanDisbursementRule.do",
                params: {
                    id: '',
                    ruleType:this.ruleTypeCombo.getValue(),
                    minIncome:this.minAnnualIncome.getValue(),
                    maxIncome:this.maxAnnualIncome.getValue(),
                    eligibility:this.multipleofSalary.getValue()
                    
                    
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
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.Failure"), WtfGlobal.getLocaleText("acc.field.Approvalmessagenotsavesucessfully")], 1);
                }

            },function(){
            
                });
            },
            
    UpdateLabel: function(){ //writen for Label Change
          if(this.ruleTypeCombo.getValue()==Wtf.LoanManagement_Constant.ABSOLUTEVALUE){
              WtfGlobal.showFormElement(this.multipleofSalary);
              WtfGlobal.updateFormLabel(this.multipleofSalary,'Loan Amount Limit :');//WtfGlobal.getLocaleText("acc.masterconfig.percentageValue")
          } else if(this.ruleTypeCombo.getValue()== Wtf.LoanManagement_Constant.MULTIPLEOFSALARY){
              WtfGlobal.showFormElement(this.multipleofSalary);
              WtfGlobal.updateFormLabel(this.multipleofSalary,'Multiple of Salary :'); //WtfGlobal.getLocaleText("acc.masterconfig.percentageValue")
          }else if(this.ruleTypeCombo.getValue()== Wtf.LoanManagement_Constant.UNLIMITED){
              WtfGlobal.hideFormElement(this.multipleofSalary);
          }
    },
    NewRule :function() {
        this.grid.getSelectionModel().clearSelections();
        this.ruleTypeCombo.clearInvalid();
        this.minAnnualIncome.setValue("");
        this.maxAnnualIncome.setValue("");
        this.multipleofSalary.setValue("");
        
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
                url:'ACCLoanCMN/deleteLoanDisbursementRule.do',
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
        
        var lowerlimit=this.minAnnualIncome.getValue()*1;
        var upperlimit=this.maxAnnualIncome.getValue()*1;
        if(lowerlimit==0 ||upperlimit==0){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.loan.eligibilityrule.zerominmaxincome")], 2);
            return;
        }
        if(upperlimit < lowerlimit){
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.loan.eligibilityrule.Addfailure")],2);
            return;
        } 
            
        if(this.gridStore.getCount()>0){
            for(var i=0;i<this.gridStore.getCount();i++){//Limit Validation
                var rec=this.gridStore.data.get(i);
                if((!((record.data.id.trim()) == rec.data.id.trim())) && (((lowerlimit==rec.data.minannualincome ||lowerlimit > rec.data.minannualincome && lowerlimit <= rec.data.maxannualincome))||(upperlimit >= rec.data.minannualincome  && upperlimit <= rec.data.maxannualincome) )){ 
                    //                                        lowerlimit <= rec.data.upperlimit, Added = because Both dates are Inclusive.
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.loan.eligibilityrule.annualincomeError")],2);
                    return;
                }
            }
        }
            
        Wtf.Ajax.requestEx({
           url:"ACCLoanCMN/saveLoanDisbursementRule.do",
            params:{
                id: record.data.id,
                ruleType:this.ruleTypeCombo.getValue(),
                minIncome:this.minAnnualIncome.getValue(),
                maxIncome:this.maxAnnualIncome.getValue(),
                eligibility:this.multipleofSalary.getValue()
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