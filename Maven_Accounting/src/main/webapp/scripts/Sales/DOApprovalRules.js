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
Wtf.DOApprovalWin=function(config){
    //this.moduleid = config.moduleid;
    this.businessPerson = (config.isCustomer ? 'Customer' : 'Vendor');
    this.isEdit=false;
    this.approvalRuleId="";
    this.productOptimizedFlag = Wtf.account.companyAccountPref.productOptimizedFlag;
    if(this.productOptimizedFlag == undefined || this.productOptimizedFlag == Wtf.Show_all_Products){
        this.productStore=Wtf.productStoreSales;
    }else if(this.productOptimizedFlag==Wtf.Products_on_type_ahead || this.productOptimizedFlag==Wtf.Products_on_Submit){
        this.productStore = Wtf.productStoreSalesOptimized
    }
//    this.record=config.record;
    Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.field.MultiLevelApprovalRules"),
        modal: true,
        iconCls :getButtonIconCls(Wtf.etype.deskera),
        width: 600,
        height:400,
        resizable: false,
        buttonAlign:"right",
//        layout: 'border',
        buttons: [{
            text:WtfGlobal.getLocaleText("acc.common.close"),
            scope:this,
            handler:function(){
                this.close();
            }
        }]
    },config);
    
    Wtf.DOApprovalWin.superclass.constructor.call(this, config);
}

Wtf.extend( Wtf.DOApprovalWin, Wtf.Window, {
    draggable:false,
    loadRecord:function(){
        if(this.record!=null)this.userinfo.getForm().loadRecord(this.record);
    },
    onRender: function(config){
        Wtf.DOApprovalWin.superclass.onRender.call(this, config);   
        this.reqRuleRec = new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'level'},
            {name: 'rule'},
            {name: 'users'},
            {name: 'userids'},
            {name : 'appliedupon'},
            {name : 'discountrule'},
            {name : 'applieduponid'},
            {name : 'ruleproductids'},
            {name : 'creator'}, 
            {name : 'rulecondition'}, 
            {name : 'lowerlimit'},
            {name : 'upperlimit'},
            {name : 'deptwiseapprover'}
        ]);
        this.reqRuleds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.reqRuleRec),
            url : "MultiLevelApprovalControllerCMN/getMultiApprovalRuleData.do",
            params:{
                moduleid : (this.transactionCombo==undefined)?"":this.transactionCombo.getValue()
            },
            scope:this
        });
        this.reqRuleds.load();
        this.requsitionrulegridcm= new Wtf.grid.ColumnModel([new Wtf.grid.CheckboxSelectionModel({
            singleSelect : true
        }),{
            header:WtfGlobal.getLocaleText("acc.field.Level"),
            dataIndex:'level',
            align:'center',
            autoWidth : true,
            renderer:function(value){
                var res = "Level "+value;
                return res;
            }
        },{
            header:WtfGlobal.getLocaleText("acc.field.Approvers"),  //"Year Ending Date",
            align:'left',
            dataIndex:'users',
            autoWidth : true
        },{
            header:WtfGlobal.getLocaleText("acc.jeapproval.appliedupon"),
            dataIndex:'appliedupon',
            align:'center',
            autoWidth : true
        },{
            header:WtfGlobal.getLocaleText("acc.field.RuleProducts"),
            dataIndex:'rule',
            align:'center',
            autoWidth : true           
        },{
            header:WtfGlobal.getLocaleText("acc.field.DiscountRule"),
            dataIndex:'discountrule',
            align:'center',
            autoWidth : true           
        }]);
        this.addRequisitionRule=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.AddRule"),
            iconCls:getButtonIconCls(Wtf.etype.add),
            tooltip: WtfGlobal.getLocaleText("acc.field.AddRule"),  //ERP-25613
            disabled:true
//            handler:this.addRequisitionRule.createDelegate(this,[false])
        });
        this.addRequisitionRule.on('click',function(){
            this.approvalRuleId="";
            this.addRequisitionRules(false);
        },this);
        this.editApprovalRule=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.EditRule"),
            iconCls:getButtonIconCls(Wtf.etype.edit),
            tooltip: WtfGlobal.getLocaleText("acc.field.EditRule"),
            disabled:true
//            handler:this.editApprovalRule.createDelegate(this,[false])
        });
        this.editApprovalRule.on('click',function(){
            this.addRequisitionRules(true);
            this.setApprovalRuleValues();
        },this);
        this.deleteRequisitionRule=new Wtf.Toolbar.Button({
            text:WtfGlobal.getLocaleText("acc.field.DeleteRule"),
            iconCls:getButtonIconCls(Wtf.etype.deletebutton),
            tooltip:WtfGlobal.getLocaleText("acc.field.DeleteRule"),    //ERP-25613
            handler:this.deteleRequisitionRule.createDelegate(this,[false])
        });
        this.pagingToolbar = new Wtf.PagingSearchToolbar({
                    pageSize: 15,
                    id: "pagingtoolbar" + this.id,
                    store: this.reqRuleds,
                    displayInfo: true,
                    emptyMsg: WtfGlobal.getLocaleText("acc.common.nores"), //"No results to display",
                    plugins: this.pP = new Wtf.common.pPageSize({id: "pPageSize_" + this.id})
                });
        this.transactionStore = new Wtf.data.SimpleStore({
            fields:['id','transaction'],
            data: [
            [ Wtf.Acc_Invoice_ModuleId,'Sales Invoice'],
            [ Wtf.Acc_Vendor_Invoice_ModuleId,'Purchase Invoice'],
            [ Wtf.Acc_Delivery_Order_ModuleId,'Delivery Order'],        // Id's are given as per their module Id's . '
            [ Wtf.Acc_Goods_Receipt_ModuleId,'Goods Receipt'],
            [ Wtf.Acc_GENERAL_LEDGER_ModuleId,'Journal Entry'],
            [ Wtf.Acc_Customer_Quotation_ModuleId,' Customer Quotation'],
            [ Wtf.Acc_Vendor_Quotation_ModuleId,' Vendor Quotation'],
            [ Wtf.Acc_Purchase_Requisition_ModuleId,'Purchase Requisition'],
            [ Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId,'Asset Purchase Requisition'],
            [ Wtf.Acc_Sales_Order_ModuleId,'Sales Order'],            
            [ Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId,'Asset Vendor Quotation'],
            [ Wtf.Acc_Purchase_Order_ModuleId,'Purchase Order'],
            [ Wtf.Acc_FixedAssets_Purchase_Order_ModuleId,'Asset Purchase Order'],
            [ Wtf.Acc_Credit_Note_ModuleId,'Credit Note'],
            [ Wtf.Acc_Debit_Note_ModuleId,'Debit Note'],
            [ Wtf.Acc_Make_Payment_ModuleId,'Make Payment'],
            [ Wtf.Acc_Receive_Payment_ModuleId,'Receive Payment']
            ]
        });
      
        this.transactionCombo=new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.approvalRuleFor"), 
            hiddenName:'transaction',
            emptyText : WtfGlobal.getLocaleText("acc.approval.selectTransaction"),
            store:this.transactionStore,
            displayField:'transaction',
            valueField:'id',
            forceSelection: true,
            selectOnFocus:true,
            triggerAction: 'all',
            editable:true,
            typeAhead: true,
            mode: 'local',
            width:120,
            allowBlank:false
        });
        this.transactionCombo.on('select',function(){
           this.addRequisitionRule.enable();
           this.editApprovalRule.disable();
           var addRuleTooltip = "", deleteRuleTooltip = "", editRuleTooltip = "";
           switch(this.transactionCombo.getValue()){
               case Wtf.Acc_Delivery_Order_ModuleId:
                    addRuleTooltip=WtfGlobal.getLocaleText("acc.field.AddRuleInDeliveryOrderRule");
                    editRuleTooltip=WtfGlobal.getLocaleText("acc.field.EditDeliveryOrderApprovalRule");
                    deleteRuleTooltip=WtfGlobal.getLocaleText("acc.field.DeleteRulefromDeliveryOrderRule");
                    break;
               case Wtf.Acc_Goods_Receipt_ModuleId:
                    addRuleTooltip=WtfGlobal.getLocaleText("acc.field.AddRuleInGoodsReceiptRule");
                    editRuleTooltip=WtfGlobal.getLocaleText("acc.field.EditGoodsReceiptApprovalRule");
                    deleteRuleTooltip=WtfGlobal.getLocaleText("acc.field.DeleteRulefromGoodsReceiptRule");
                    break;
               case Wtf.Acc_GENERAL_LEDGER_ModuleId:
                    addRuleTooltip=WtfGlobal.getLocaleText("acc.field.AddRuleInJournalEntryRule");
                    editRuleTooltip=WtfGlobal.getLocaleText("acc.field.AddRuleInDeliveryOrderRule");
                    deleteRuleTooltip=WtfGlobal.getLocaleText("acc.field.DeleteRulefromJournalEntryRule");
                    break;
               case Wtf.Acc_Customer_Quotation_ModuleId:
                    addRuleTooltip=WtfGlobal.getLocaleText("acc.field.AddRuleInCustomerQuotationTooltip");
                    editRuleTooltip=WtfGlobal.getLocaleText("acc.field.EditCustomerQuotationApprovalRule");
                    deleteRuleTooltip=WtfGlobal.getLocaleText("acc.field.DeleteRulefromCustomerQuotationTooltip");
                    break;
               case Wtf.Acc_Vendor_Quotation_ModuleId:
                    addRuleTooltip = WtfGlobal.getLocaleText("acc.field.AddRuleInVendorQuotationTooltip");
                    editRuleTooltip=WtfGlobal.getLocaleText("acc.field.EditVendorQuotationApprovalRule");
                    deleteRuleTooltip=WtfGlobal.getLocaleText("acc.field.DeleteRulefromVendorQuotationTooltip");
                    break;
               case Wtf.Acc_Sales_Order_ModuleId:
                    addRuleTooltip = WtfGlobal.getLocaleText("acc.field.AddRuleInSalesOrderTooltip");
                    editRuleTooltip=WtfGlobal.getLocaleText("acc.field.EditSalesOrderApprovalRule");
                    deleteRuleTooltip=WtfGlobal.getLocaleText("acc.field.DeleteRulefromSalesOrderTooltip");
                    break;
               case Wtf.Acc_Purchase_Order_ModuleId:
                    addRuleTooltip = WtfGlobal.getLocaleText("acc.field.AddRuleInPurchaseOrderTooltip");
                    editRuleTooltip=WtfGlobal.getLocaleText("acc.field.EditPurchaseOrderApprovalRule");
                    deleteRuleTooltip=WtfGlobal.getLocaleText("acc.field.DeleteRulefromPurchaseOrderTooltip");
                    break;
               case Wtf.Acc_FixedAssets_Purchase_Order_ModuleId:
                    addRuleTooltip = WtfGlobal.getLocaleText("acc.field.AddRuleInAssetPurchaseOrderTooltip");
                    editRuleTooltip=WtfGlobal.getLocaleText("acc.field.EditAssetPurchaseOrderApprovalRule");
                    deleteRuleTooltip=WtfGlobal.getLocaleText("acc.field.DeleteRulefromAssetPurchaseOrderTooltip");
                    break;
               case Wtf.Acc_Invoice_ModuleId:
                    addRuleTooltip = WtfGlobal.getLocaleText("acc.field.AddRuleInInvoiceTooltip");
                    editRuleTooltip=WtfGlobal.getLocaleText("acc.field.EditSalesInvoiceApprovalRule");
                    deleteRuleTooltip=WtfGlobal.getLocaleText("acc.field.DeleteRulefromInvoiceTooltip");
                    break;
               case Wtf.Acc_Vendor_Invoice_ModuleId:
                    addRuleTooltip = WtfGlobal.getLocaleText("acc.field.AddRuleVendorInInvoiceTooltip");
                    editRuleTooltip=WtfGlobal.getLocaleText("acc.field.EditPurchaseInvoiceApprovalRule");
                    deleteRuleTooltip=WtfGlobal.getLocaleText("acc.field.DeleteRulefromVendorInvoiceTooltip");
                    break;
               case Wtf.Acc_Credit_Note_ModuleId:
                    addRuleTooltip = WtfGlobal.getLocaleText("acc.field.AddRuleCreditNoteTooltip");
                    editRuleTooltip=WtfGlobal.getLocaleText("acc.field.EditCreditNoteApprovalRule");
                    deleteRuleTooltip=WtfGlobal.getLocaleText("acc.field.DeleteRulefromCreditNoteTooltip");
                    break;
               case Wtf.Acc_Debit_Note_ModuleId:
                    addRuleTooltip = WtfGlobal.getLocaleText("acc.field.AddRuleDebitNoteTooltip");
                    editRuleTooltip=WtfGlobal.getLocaleText("acc.field.EditDebitNoteApprovalRule");
                    deleteRuleTooltip=WtfGlobal.getLocaleText("acc.field.DeleteRulefromDebitNoteTooltip");
                    break;
               case Wtf.Acc_Make_Payment_ModuleId:
                    addRuleTooltip = WtfGlobal.getLocaleText("acc.field.AddRuleMakePaymentTooltip");
                    editRuleTooltip=WtfGlobal.getLocaleText("acc.field.EditMakePaymentApprovalRule");
                    deleteRuleTooltip=WtfGlobal.getLocaleText("acc.field.DeleteRulefromMakePaymentTooltip");
                    break;
               case Wtf.Acc_Purchase_Requisition_ModuleId:
                    addRuleTooltip = WtfGlobal.getLocaleText("acc.field.AddRulePurchaseRequisitionTooltip");
                    editRuleTooltip=WtfGlobal.getLocaleText("acc.field.EditPurchaseRequisitionApprovalRule");
                    deleteRuleTooltip=WtfGlobal.getLocaleText("acc.field.DeleteRulefromPurchaseRequisitionTooltip");
                    break;
               case Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId:
                    addRuleTooltip = WtfGlobal.getLocaleText("acc.field.AddRuleAssetPurchaseRequisitionTooltip");
                    editRuleTooltip=WtfGlobal.getLocaleText("acc.field.EditAssetPurchaseRequisitionApprovalRule");
                    deleteRuleTooltip=WtfGlobal.getLocaleText("acc.field.DeleteRulefromAssetPurchaseRequisitionTooltip");
                    break;
               case Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId:
                    addRuleTooltip = WtfGlobal.getLocaleText("acc.field.AddRuleAssetVendorQuotationTooltip");
                    editRuleTooltip=WtfGlobal.getLocaleText("acc.field.EditAssetVendorQuotationApprovalRule");
                    deleteRuleTooltip=WtfGlobal.getLocaleText("acc.field.DeleteRulefromAssetVendorQuotationTooltip");
                    break;
                case Wtf.Acc_Receive_Payment_ModuleId:
                    addRuleTooltip = WtfGlobal.getLocaleText("acc.field.AddRuleReceivePaymentTooltip");
                    editRuleTooltip=WtfGlobal.getLocaleText("acc.field.EditReceivePaymentApprovalRule");
                    deleteRuleTooltip=WtfGlobal.getLocaleText("acc.field.DeleteRulefromReceivePaymentTooltip");
                    break;
                    
           }
        
           this.addRequisitionRule.setTooltip(addRuleTooltip);
           this.editApprovalRule.setTooltip(editRuleTooltip);
           this.deleteRequisitionRule.setTooltip(deleteRuleTooltip);
           this.reqRuleds.load({
                params:{
                    start:0,
                    limit:this.pagingToolbar.pageSize,
                    moduleid : this.transactionCombo.getValue()
                }
            });
           
        },this);
         this.reqRuleds.on('beforeload', function (store, option) {
            var currentBaseParams = this.reqRuleds.baseParams;
            currentBaseParams.moduleid = this.transactionCombo.getValue();
            this.reqRuleds.baseParams = currentBaseParams;
        }, this);
        this.transactionCombo.on('change',function(){
           
            },this);
       
        this.transactionPanel = new Wtf.form.FormPanel({
            labelWidth: 150,
            border : false,
            height:50,
            bodyStyle:'padding:5px 5px 0',
            autoWidth:true,
            defaults: {
                anchor:'94%'
            },
            defaultType: 'textfield',
            items:[this.transactionCombo]
        })
        this.RuleGrid = new Wtf.grid.GridPanel({
            layout:'fit',
            autoScroll:true,
            height:280,
            width:580,
            store: this.reqRuleds,
            tbar : [this.addRequisitionRule,this.editApprovalRule,this.deleteRequisitionRule],
            cm: this.requsitionrulegridcm,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
            },
            bbar:this.pagingToolbar
        });
        this.RuleGrid.on('rowclick',this.handleRowClick,this);
        this.add(this.transactionPanel);
        this.add(this.RuleGrid);
    },
    handleRowClick: function() {
        var rec = this.RuleGrid.getSelectionModel().getSelected();
        if(this.RuleGrid.getSelectionModel().getCount() > 1) {
            this.editApprovalRule.disable();
        } else {
            this.editApprovalRule.enable();
        }
    },
    addRequisitionRules : function(isEdit) {
        this.isEdit=isEdit;
         var highestLevelExists=0;
         for(var cnt=0;cnt<this.reqRuleds.getCount();cnt++)
            {
                if(this.reqRuleds.getAt(cnt).data.level>highestLevelExists)
                    highestLevelExists=this.reqRuleds.getAt(cnt).data.level;
            }
         this.usersRec = new Wtf.data.Record.create([
            {name: 'userid'},
            {name: 'username'},
            {name: 'fname'},
            {name: 'lname'},
            {name: 'fullname'},
            {name: 'image'},
            {name: 'emailid'},
            {name: 'lastlogin',type: 'date'},
            {name: 'aboutuser'},
            {name: 'address'},
            {name: 'contactno'},
            {name: 'rolename'},
            {name: 'roleid'}
        ]);
        this.userds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty: 'count',
                root: "data"
            },this.usersRec),
            url : "ProfileHandler/getAllUserDetails.do",
            baseParams:{
                mode:11
            }
        });
        this.userds.on('load',function(){
            if(this.isEdit){
                var rec = this.RuleGrid.getSelectionModel().getSelected();
                if(rec!=null && rec!="" && rec!=undefined){
                    if(rec.data.applieduponid==Wtf.ApprovalRules_AppliedUpon.Journal_Entry_Creator){
                        this.jeCreator.setValue(rec.data.creator);
                    }
                    this.userCombo.setValue(rec.data.userids);
                }
            }
        },this);
        this.userCombo= new Wtf.common.Select({
            width:150,
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Approvers"),
            name:'approver',
            store:this.userds,
            hiddenName:'approver',
            xtype:'select',
            selectOnFocus:true,
            forceSelection:true,
            multiSelect:true,
            displayField:'fullname',
            valueField:'userid',
            mode: 'local',
            allowBlank:false,
            triggerAction:'all',
            typeAhead: true
        })
        this.userds.load();
        this.levelstore = new Wtf.data.SimpleStore({
           fields:['id','level'],
           data: [
           ['1','Level 1'],
           ['2','Level 2'],
           ['3','Level 3'],
           ['4','Level 4'],
           ['5','Level 5'],
           ['6','Level 6'],
           ['7','Level 7'],
           ['8','Level 8'],
           ['9','Level 9'],
           ['10','Level 10']
           ]
       });
               for(var count=highestLevelExists+1;count<=9;count++)
                this.levelstore.remove(this.levelstore.getAt(highestLevelExists+1));
              this.levelCombo=new Wtf.form.ComboBox({
           fieldLabel:WtfGlobal.getLocaleText("acc.field.Level"), //WtfGlobal.getLocaleText("hrms.common.Gender")+"*",
           hiddenName:'level',
           store:this.levelstore,
           displayField:'level',
           valueField:'id',
           forceSelection: true,
           selectOnFocus:true,
           triggerAction: 'all',
           typeAhead:true,
           mode: 'local',
           width:220,
           allowBlank:false
       });
       
       this.rulestore = new Wtf.data.SimpleStore({
           fields:['id','rule'],
           data: [
           ['1','is greater than'],
           ['2','is less than'],
           ['3','is equal to'],
           ['4','is in the range']
           ]
       });
       this.ruleCombo=new Wtf.form.ComboBox({
           fieldLabel:WtfGlobal.getLocaleText("acc.field.Rule"), //WtfGlobal.getLocaleText("hrms.common.Gender")+"*",
           hiddenName:'rule',
           store:this.rulestore,
           displayField:'rule',
           valueField:'id',
           forceSelection: true,
           selectOnFocus:true,
           triggerAction: 'all',
           typeAhead:true,
           allowBlank:true,
           mode: 'local',
           width:220
       });
       this.ruleCombo.on("select",function(combo,record,index){
           if(this.ruleCombo.getValue()==4){
               this.ulimit.enable();
               this.ulimit.allowBlank=false;
               this.ulimitPanel.show();               
           }else{
               this.ulimit.allowBlank=true;
               this.ulimitPanel.hide();
           }
       },this)
       
        this.limit = new Wtf.form.NumberField ({
            fieldLabel:WtfGlobal.getLocaleText("acc.ra.value"),
            name:'limitvalue',
            allowBlank:true
        });
        this.ulimit = new Wtf.form.NumberField ({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.UpperLimit"),
            name:'ulimitvalue',
            allowBlank:true,
            disabled : true,
            width:319
        });
        
        this.ulimitPanel = new Wtf.Panel({
            autoWidth:true,
            hidden : true,
            border : false,
            layout:'form',
            labelWidth: 125,
            autoHeight:true,
            defaults: {anchor:'94%'},
            items : [this.ulimit]
       })
       
       this.RulePanel = new Wtf.Panel({
            autoWidth:true,
            hidden : true,
            border : false,
            layout:'form',
            labelWidth: 125,
            autoHeight:true,
            defaults: {anchor:'94%'},
            items : [this.ruleCombo,this.limit]
       })
       
       
       this.isDeptWiseApprover = new Wtf.form.Checkbox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.isapplydeptwiseapprover"),
            name: 'isDeptWiseApprover'
        });
       
       this.isReqRule = new Wtf.form.Checkbox({
            fieldLabel: WtfGlobal.getLocaleText("acc.field.Doyouwanttoapplyconditionalrule?"),
            name: 'isrule'
        })
       
        this.isReqRule.on("check",function(obj,value){
            if(this.transactionCombo.getValue()==24){      // For JE Approval Rule
                if(value) {                                     // For conditional Rules
                    this.appliedUponPanel.show();
                    this.appliedUponPanel.doLayout();
                    this.RulePanel.doLayout();
                    this.ulimitPanel.doLayout();
                    this.requisitionRuleForm.doLayout();
                    this.appliedUpon.allowBlank=false;
                } else {                                  //No conditional rules, all DO & GR will be sent for approval (Status=pending)
                    this.creatorPanel.hide();
                    this.creatorPanel.doLayout();
                    this.appliedUponPanel.hide();
                    this.appliedUponPanel.doLayout();   
                    this.RulePanel.hide();
                    this.ulimitPanel.hide();
                    this.appliedUpon.reset();
                    this.jeCreator.reset();
                    this.RulePanel.doLayout();
                    this.ulimitPanel.doLayout();
                    this.requisitionRuleForm.doLayout();
                    this.appliedUpon.allowBlank=true;
                    this.jeCreator.allowBlank=true;
                    this.ruleCombo.allowBlank = true;
                    this.limit.allowBlank = true;
                    this.ulimit.allowBlank = true;
                    this.categoryPanel.hide();
                }           
            }else if((this.transactionCombo.getValue()==Wtf.Acc_Customer_Quotation_ModuleId || this.transactionCombo.getValue() == Wtf.Acc_Sales_Order_ModuleId ) && Wtf.account.companyAccountPref.activateProfitMargin){  // For Sales Quotaion
                if(value) {                                     // For conditional Rules
                    this.appliedUponPanel.show();
                    this.appliedUponPanel.doLayout();
                    this.RulePanel.doLayout();
                    this.ulimitPanel.doLayout();
                    this.requisitionRuleForm.doLayout();
                    this.appliedUpon.allowBlank=false;
                } else {                                  //No conditional rules, all DO & GR will be sent for approval (Status=pending)
                    this.creatorPanel.hide();
                    this.creatorPanel.doLayout();
                    this.appliedUponPanel.hide();
                    this.appliedUponPanel.doLayout();   
                    this.RulePanel.hide();
                    this.ulimitPanel.hide();
                    this.appliedUpon.reset();
                    this.jeCreator.reset();
                    this.RulePanel.doLayout();
                    this.ulimitPanel.doLayout();
                    this.requisitionRuleForm.doLayout();
                    this.appliedUpon.allowBlank=true;
                    this.jeCreator.allowBlank=true;
                    this.ruleCombo.allowBlank = true;
                    this.limit.allowBlank = true;
                    this.ulimit.allowBlank = true;
                    this.categoryPanel.hide();
                }      
            }else if(this.transactionCombo.getValue()==Wtf.Acc_Sales_Order_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Purchase_Order_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Customer_Quotation_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Vendor_Quotation_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Invoice_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Vendor_Invoice_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Delivery_Order_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Goods_Receipt_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Purchase_Requisition_ModuleId){ // For SO/PO/CQ/VQ/SI/PI
                if(value) {        // For conditional Rules
                    this.appliedUponPanel.show();
                    this.appliedUponPanel.doLayout();
                    this.RulePanel.doLayout();
                    this.ulimitPanel.doLayout();
                    this.requisitionRuleForm.doLayout();
                    this.appliedUpon.allowBlank=false;
                } else {                                //  No conditional rules, all DO & GR will be sent for approval (Status=pending)
                    this.creatorPanel.hide();
                    this.creatorPanel.doLayout();
                    this.appliedUponPanel.hide();
                    this.appliedUponPanel.doLayout();   
                    this.RulePanel.hide();
                    this.ulimitPanel.hide();
                    this.appliedUpon.reset();
                    this.jeCreator.reset();
                    this.productCombo.reset();
                    this.selectProductPanel.hide();
                    this.selectProductPanel.doLayout();
                    this.RulePanel.doLayout();
                    this.ulimitPanel.doLayout();
                    this.requisitionRuleForm.doLayout();
                    this.appliedUpon.allowBlank=true;
                    this.jeCreator.allowBlank=true;
                    this.productCombo.allowBlank=true;
                    this.ruleCombo.allowBlank = true;
                    this.limit.allowBlank = true;
                    this.ulimit.allowBlank = true;
                    this.categoryPanel.hide();
                }
            }else {                                      // For DO and GR Approval Rule
                if(value) {                             // For conditional Rules
                    this.RulePanel.show();
                    this.RulePanel.doLayout();
                    this.ulimitPanel.show();
                    this.ulimitPanel.doLayout();
                    this.ruleCombo.allowBlank = false;
                    this.limit.allowBlank = false;
                    this.ulimit.allowBlank = true;
                    this.appliedUpon.allowBlank=true;
                } else {                                //  No conditional rules, all DO & GR will be sent for approval (Status=pending)
                    this.RulePanel.hide();
                    this.RulePanel.doLayout();
                    this.ulimitPanel.hide();
                    this.ulimitPanel.doLayout();
                    this.ruleCombo.allowBlank = true;
                    this.limit.allowBlank = true;
                    this.ulimit.allowBlank = true;
                }
            }
        },this);
        var appliedUponStoreData = new Array();
        if(this.transactionCombo.getValue()==24){
            appliedUponStoreData.push([Wtf.ApprovalRules_AppliedUpon.Total_Amount,'Total Amount']);
            appliedUponStoreData.push([Wtf.ApprovalRules_AppliedUpon.Journal_Entry_Creator,'Journal Entry Creator']);
        }else if((this.transactionCombo.getValue()==Wtf.Acc_Customer_Quotation_ModuleId || this.transactionCombo.getValue() == Wtf.Acc_Sales_Order_ModuleId )&& Wtf.account.companyAccountPref.activateProfitMargin){
            appliedUponStoreData.push([Wtf.ApprovalRules_AppliedUpon.Total_Amount,'Total Amount']);
            appliedUponStoreData.push([Wtf.ApprovalRules_AppliedUpon.Profit_Margin_Amount,'Profit Margin Amount']);
            if(this.transactionCombo.getValue() == Wtf.Acc_Sales_Order_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Customer_Quotation_ModuleId){
                appliedUponStoreData.push([Wtf.ApprovalRules_AppliedUpon.Specific_Products,'Specific Product(s)']);
                appliedUponStoreData.push([Wtf.ApprovalRules_AppliedUpon.Specific_Products_category,'Specific Product(s) Category']);
                appliedUponStoreData.push([Wtf.ApprovalRules_AppliedUpon.Specific_Products_Discount,'Specific Product(s) Discount']);
            }
        }else if(this.transactionCombo.getValue() == Wtf.Acc_Sales_Order_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Purchase_Order_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Customer_Quotation_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Vendor_Quotation_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Invoice_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Vendor_Invoice_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Delivery_Order_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Goods_Receipt_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Purchase_Requisition_ModuleId){
            appliedUponStoreData.push([Wtf.ApprovalRules_AppliedUpon.Total_Amount,'Total Amount']);
            appliedUponStoreData.push([Wtf.ApprovalRules_AppliedUpon.Specific_Products,'Specific Product(s)']);
            appliedUponStoreData.push([Wtf.ApprovalRules_AppliedUpon.Specific_Products_category,'Specific Product(s) Category']);
            appliedUponStoreData.push([Wtf.ApprovalRules_AppliedUpon.Specific_Products_Discount,'Specific Product(s) Discount']);
        }else{
            appliedUponStoreData.push([Wtf.ApprovalRules_AppliedUpon.Total_Amount,'Total Amount']);
        }
        if (this.transactionCombo.getValue() == Wtf.Acc_Sales_Order_ModuleId) {             //ERM-396
            appliedUponStoreData.push([Wtf.ApprovalRules_AppliedUpon.SO_CREDIT_LIMIT, 'SO Credit Limit']);
        }
        this.appliedUponStore = new Wtf.data.SimpleStore({
            fields:['id','criteria'],
            data: appliedUponStoreData
        });
       this.appliedUpon=new Wtf.form.ComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.AppliedUpon*"), 
            store:this.appliedUponStore,
            displayField:'criteria',
            valueField:'id',
            forceSelection: true,
            selectOnFocus:true,
            triggerAction: 'all',
            editable:false,
            mode: 'local',
            autoWidth:true,
            allowBlank:true
        });
       this.appliedUponPanel = new Wtf.Panel({
            autoWidth:true,
            hidden : true,
            border : false,
            layout:'form',
            labelWidth: 125,
            autoHeight:true,
            defaults: {anchor:'94%'},
            items : [this.appliedUpon]
       })      
       
       this.productStore.on('beforeload',function(s,o){
           WtfGlobal.setAjaxTimeOut();
           if(this.ProductloadingMask==undefined || this.isEdit){
                this.ProductloadingMask = new Wtf.LoadMask(document.body,{
                    msg : WtfGlobal.getLocaleText("acc.msgbox.50")
                    });
                    this.ProductloadingMask.show();
                }
       },this);
       
       this.productStore.on('load', function () {
            WtfGlobal.resetAjaxTimeOut();
            if(this.ProductloadingMask)
                this.ProductloadingMask.hide();
            if (this.isEdit) {
                var rec = this.RuleGrid.getSelectionModel().getSelected();
                if (rec != null && rec != "" && rec != undefined) {
                    if (rec.data.applieduponid == Wtf.ApprovalRules_AppliedUpon.Specific_Products || rec.data.applieduponid == Wtf.ApprovalRules_AppliedUpon.Specific_Products_Discount) {
                        this.productCombo.setValue(rec.data.ruleproductids);
                    }
                }
            }
        }, this);
        this.productStore.on("loadexception",function(){
            WtfGlobal.resetAjaxTimeOut();
            if(this.ProductloadingMask)
                this.ProductloadingMask.hide();
        },this);
        if (this.productOptimizedFlag == undefined || this.productOptimizedFlag == Wtf.Show_all_Products) {
            this.productCombo = new Wtf.form.ExtFnComboBox({
                name: 'pid',
                fieldLabel: WtfGlobal.getLocaleText("acc.field.SelectProducts"),
                store: this.productStore, 
                typeAhead: true,
                isProductCombo: true,
                selectOnFocus: true,
                maxHeight: 250,
                listAlign: "bl-tl?",
                valueField: 'productid',
                displayField: 'pid',
                extraFields: ['productname', 'type'],
                listWidth: 450,
                extraComparisionField: 'pid', // type ahead search on acccode as well.
                lastQuery: '',
                extraComparisionFieldArray: ['pid', 'productname'],
                scope: this,
                hirarchical: true,
                forceSelection: true
            });
        } else {
            this.productCombo = new Wtf.form.ExtFnComboBox({
                name: 'pid',
                fieldLabel: WtfGlobal.getLocaleText("acc.field.SelectProducts"),
                store: this.productStore,
                typeAhead: true,
                selectOnFocus: true,
                isProductCombo: true,
                maxHeight: 250,
                listAlign: "bl-tl?",
                valueField: 'productid', //productid
                displayField: 'pid',
                extraFields: ['productname', 'type'],
                listWidth: 450,
                extraComparisionField: 'pid', // type ahead search on acccode as well.
                mode: 'remote',
                hideTrigger: true,
                scope: this,
                triggerAction: 'all',
                editable: true,
                minChars: 2,
                hirarchical: true,
                hideAddButton: true, //Added this Flag to hide AddNew  Button  
                forceSelection: true
            });
        }
        if (this.productOptimizedFlag == undefined || this.productOptimizedFlag == Wtf.Show_all_Products) {
            this.productStore.load();
        }
         this.productCategoryRecord = Wtf.data.Record.create ([
        {
            name:'id'
        },{
            name:'name'
        }
        ]);    
       this.productCategoryStore = new Wtf.data.Store({
        url: "ACCMaster/getMasterItems.do",
        baseParams:{
                mode:112,
                groupid:19
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.productCategoryRecord)
        });
     
        this.Category = new Wtf.form.ComboBox({
            fieldLabel: WtfGlobal.getLocaleText("acc.cust.Productcategory"),
            store: this.productCategoryStore,
            displayField:'name',
            valueField:'id',
            autoWidth:true,
            listWidth: 200,
            mode: 'local',
            editable:false,
            triggerAction: 'all',
            selectOnFocus:true
        });
        this.productCategoryStore.load();
         this.categoryPanel = new Wtf.Panel({
            autoWidth:true,
            hidden : true,
            border : false,
            layout:'form',
            labelWidth: 125,
            autoHeight:true,
            defaults: {anchor:'94%'},
            items : [this.Category]
       });
       this.Category.on('invalid',function( field,msg){//Set padding for invalid icon
            this.setStyleWidth(field,msg);
        },this);
        this.selectProductPanel = new Wtf.Panel({
            autoWidth:true,
            hidden : true,
            border : false,
            layout:'form',
            labelWidth: 125,
            autoHeight:true,
            defaults: {anchor:'94%'},
            items : [this.productCombo]
        });
   
        this.jeCreator=new Wtf.common.Select({
            fieldLabel:WtfGlobal.getLocaleText("acc.jeapproval.jeCreator"),           
            store:this.userds,   
            displayField:'fname',
            valueField:'userid',
            xtype:'select',
            forceSelection: true,
            selectOnFocus:true,
            multiSelect:true,
            triggerAction: 'all',
            mode: 'local',
            width:200,
            allowBlank:true
        });
        this.creatorPanel = new Wtf.Panel({
            autoWidth:true,
            hidden : true,
            border : false,
            layout:'form',
            labelWidth: 125,
            autoHeight:true,
            defaults: {anchor:'94%'},
            items : [this.jeCreator]
       })
        this.appliedUpon.on('select',function(){
            this.resetFields();
            if(this.appliedUpon.getValue()==Wtf.ApprovalRules_AppliedUpon.Total_Amount){
                this.creatorPanel.hide();
                this.creatorPanel.doLayout();
                this.jeCreator.allowBlank=true;
                this.selectProductPanel.hide();
                this.selectProductPanel.doLayout();
                this.productCombo.allowBlank=true;
                this.RulePanel.show();
                this.RulePanel.doLayout();
                this.ulimitPanel.show();
                this.ulimitPanel.doLayout();
                this.ruleCombo.allowBlank = false;
                this.limit.allowBlank = false;
                this.ulimit.allowBlank = false;
                this.Category.allowBlank=true;
                this.categoryPanel.hide();
                this.selectProductPanel.doLayout();
            } else if(this.appliedUpon.getValue()==Wtf.ApprovalRules_AppliedUpon.Profit_Margin_Amount){// For Rule Applied on Total Profit Margin
                this.creatorPanel.hide();
                this.creatorPanel.doLayout();
                this.jeCreator.allowBlank=true;
                this.selectProductPanel.hide();
                this.selectProductPanel.doLayout();
                this.productCombo.allowBlank=true;
                this.RulePanel.show();
                this.RulePanel.doLayout();
                this.ulimitPanel.show();
                this.ulimitPanel.doLayout();
                this.ruleCombo.allowBlank = false;
                this.limit.allowBlank = false;
                this.ulimit.allowBlank = false;
                this.Category.allowBlank=true;
                this.categoryPanel.hide();
                this.selectProductPanel.doLayout();
                this.productCombo.allowBlank=true
            }else if(this.appliedUpon.getValue()==Wtf.ApprovalRules_AppliedUpon.Specific_Products){// Rule on Specific Product
                this.creatorPanel.hide();
                this.creatorPanel.doLayout();
                this.jeCreator.allowBlank=true;
                this.RulePanel.hide();
                this.RulePanel.doLayout();
                this.ulimitPanel.hide();
                this.ulimitPanel.doLayout();
                this.ruleCombo.allowBlank = true;
                this.limit.allowBlank = true;
                this.ulimit.allowBlank = true;
                this.selectProductPanel.show();
                this.selectProductPanel.doLayout();
                this.Category.allowBlank=true;
                this.categoryPanel.hide();
                this.selectProductPanel.doLayout();
                this.productCombo.allowBlank=false;
            }else if(this.appliedUpon.getValue()==Wtf.ApprovalRules_AppliedUpon.Specific_Products_category){// Rule on Specific Product
                this.creatorPanel.hide();
                this.creatorPanel.doLayout();
                this.jeCreator.allowBlank=true;
                this.RulePanel.hide();
                this.Category.allowBlank=false;
                this.RulePanel.doLayout();
                this.ulimitPanel.hide();
                this.ulimitPanel.doLayout();
                this.ruleCombo.allowBlank = true;
                this.limit.allowBlank = true;
                this.ulimit.allowBlank = true;
                this.selectProductPanel.hide();
                this.selectProductPanel.doLayout();
                this.categoryPanel.show();
                this.selectProductPanel.doLayout();
                this.productCombo.allowBlank=true;
            } else if(this.appliedUpon.getValue()==Wtf.ApprovalRules_AppliedUpon.Specific_Products_Discount){// Rule on Specific Product Discount
                this.creatorPanel.hide();
                this.creatorPanel.doLayout();
                this.jeCreator.allowBlank=true;
                this.selectProductPanel.show();
                this.selectProductPanel.doLayout();
                this.productCombo.allowBlank=false;
                this.RulePanel.show();
                this.RulePanel.doLayout();
                this.ulimitPanel.show();
                this.ulimitPanel.doLayout();
                this.ruleCombo.allowBlank = false;
                this.limit.allowBlank = false;
                this.ulimit.allowBlank = false;
                this.Category.allowBlank=true;
                this.categoryPanel.hide();
                this.selectProductPanel.doLayout();
            }else if (this.appliedUpon.getValue() == Wtf.ApprovalRules_AppliedUpon.SO_CREDIT_LIMIT) {// Rule on SO Credit Limit ERM-396
                this.creatorPanel.hide();
                this.creatorPanel.doLayout();
                this.jeCreator.allowBlank = true;
                this.selectProductPanel.hide();
                this.selectProductPanel.doLayout();
                this.productCombo.allowBlank = true;
                this.RulePanel.hide();
                this.RulePanel.doLayout();
                this.ulimitPanel.hide();
                this.ulimitPanel.doLayout();
                this.ruleCombo.allowBlank = true;
                this.limit.allowBlank = true;
                this.ulimit.allowBlank = true;
                this.Category.allowBlank = true;
                this.categoryPanel.hide();
                this.selectProductPanel.doLayout();
            }else {
                this.creatorPanel.show();
                this.creatorPanel.doLayout();
                this.jeCreator.syncSize();
                this.jeCreator.setWidth(200);
                this.jeCreator.allowBlank=false;
                this.selectProductPanel.hide();
                this.selectProductPanel.doLayout();
                this.productCombo.allowBlank=true;
                this.RulePanel.hide();
                this.RulePanel.doLayout();
                this.ulimitPanel.hide();
                this.ulimitPanel.doLayout();
                this.ruleCombo.allowBlank = true;
                this.limit.allowBlank = true;
                this.ulimit.allowBlank = true;
                this.Category.allowBlank=true;
                this.categoryPanel.hide();
                this.selectProductPanel.doLayout();
            }
        },this);
        this.requisitionRuleForm=new Wtf.form.FormPanel({
//            frame:true,
            url:"MultiLevelApprovalRule/saveMultiApprovalRule.do",
            labelWidth: 125,
            border : false,
            autoHeight:true,
            bodyStyle:'padding:5px 5px 20px',
            autoWidth:true,
            defaults: {anchor:'94%'},
            defaultType: 'textfield',
            items:[this.levelCombo,this.userCombo,this.isDeptWiseApprover,this.isReqRule,this.appliedUponPanel,this.creatorPanel,this.selectProductPanel,this.RulePanel,this.ulimitPanel,this.categoryPanel],
            buttons:[{
                text:WtfGlobal.getLocaleText("acc.common.saveBtn"),
                handler:function(){
                    if(this.requisitionRuleForm.getForm().isValid()) {
                        var appliedUpon = Wtf.ApprovalRules_AppliedUpon.All_Conditions;  // For JE , applieupon will be 1(Total Amount) or 2(Creator) , but for DO and GR , it will be either 1 (total amount) or 0 (applied on all conditions)
                        if(this.transactionCombo.getValue()== Wtf.Acc_GENERAL_LEDGER_ModuleId){
                            appliedUpon = this.appliedUpon.getValue();
                        }else if((this.transactionCombo.getValue() == Wtf.Acc_Sales_Order_ModuleId || this.transactionCombo.getValue() == Wtf.Acc_Customer_Quotation_ModuleId) && Wtf.account.companyAccountPref.activateProfitMargin){
                            appliedUpon = this.appliedUpon.getValue();
                        }else if(this.transactionCombo.getValue() == Wtf.Acc_Sales_Order_ModuleId || this.transactionCombo.getValue() == Wtf.Acc_Purchase_Order_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Customer_Quotation_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Vendor_Quotation_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Invoice_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Vendor_Invoice_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Delivery_Order_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Goods_Receipt_ModuleId || this.transactionCombo.getValue()==Wtf.Acc_Purchase_Requisition_ModuleId){
                            appliedUpon = this.appliedUpon.getValue();
                        }else if(this.isReqRule.getValue()){
                            appliedUpon = Wtf.ApprovalRules_AppliedUpon.Total_Amount;
                        }
                        this.requisitionRuleForm.getForm().submit({
                            waitMsg:WtfGlobal.getLocaleText("acc.field.SavingRule..."),
                            params:{
                                ulimitvalue : this.ulimit.getValue(),
                                moduleid : this.transactionCombo.getValue(),
                                appliedupon : appliedUpon,
                                creator : this.jeCreator.getValue(),
                                productids:this.productCombo.getValue(),
                                productCategory:this.Category.getValue(),
                                ruleid:this.approvalRuleId,
                                deptWiseApprover:this.isDeptWiseApprover.getValue()
                            },
                            scope:this,
                            success:function(f,a){
                                this.Rulewin.close();
                                this.reqRuleds.load({
                                    params:{
                                        start:0,
                                        limit:this.pagingToolbar.pageSize,
                                        moduleid:this.transactionCombo.getValue()
                                    }
                                });
                                var response = eval('('+a.response.responseText+')')
                                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"),response.data.msg],response.success*2+1);
                            },
                            failure:function(f,a){this.Rulewin.close();this.genFailureResponse(eval('('+a.response.responseText+')'))}
                        });
                    }
                },
                scope:this
            },{
                text:WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //"Cancel",
                scope:this,
                handler:function (){
                    this.Rulewin.close();
                }
            }]
        });
        this.requisitionRuleForm.add({xtype:'hidden', name:'featureid'})

        this.Rulewin=new Wtf.Window({
            title: this.isEdit?WtfGlobal.getLocaleText("acc.field.EditRule"):WtfGlobal.getLocaleText("acc.field.AddRule"),
            closable:true,
            iconCls :getButtonIconCls(Wtf.etype.deskera),
//            layout: 'border',
//            border : false,
//            width:300,
            width: 500,
            autoHeight:true,
//            plain:true,
            modal:true,
            buttonAlign : 'right',
            items:this.requisitionRuleForm
        });this.Rulewin.show();
//        if(isEdit)this.form.requisitionRuleForm().loadRecord(rec);
    },
    setStyleWidth:function(field,msg){//Padding for invalid field icon
    field.errorIcon.applyStyles("left:"+(336)+"px;");//Add both width and get left padding for invalid icon
},
    setApprovalRuleValues : function() {
        var rec = this.RuleGrid.getSelectionModel().getSelected();
        if(rec!=null && rec!="" && rec!=undefined){
            this.approvalRuleId=rec.data.id;
            this.levelCombo.setValue(rec.data.level);
            this.isDeptWiseApprover.setValue(rec.data.deptwiseapprover);
            this.levelCombo.disable();
            if(rec.data.applieduponid!=Wtf.ApprovalRules_AppliedUpon.All_Conditions){// If not applied on All Condition
                this.isReqRule.setValue(true)
                this.appliedUpon.setValue(rec.data.applieduponid);
                this.appliedUpon.fireEvent('select');
                if(rec.data.applieduponid==Wtf.ApprovalRules_AppliedUpon.Total_Amount || rec.data.applieduponid==Wtf.ApprovalRules_AppliedUpon.Profit_Margin_Amount || rec.data.applieduponid==Wtf.ApprovalRules_AppliedUpon.Specific_Products_Discount){
                    this.ruleCombo.setValue(rec.data.rulecondition);
                    this.ruleCombo.fireEvent('select');
                    if(rec.data.lowerlimit!="" && rec.data.lowerlimit!=null && rec.data.lowerlimit!=null){
                        this.limit.setValue(rec.data.lowerlimit);
                    }
                    if(rec.data.upperlimit!="" && rec.data.upperlimit!=null && rec.data.upperlimit!=null){
                        this.ulimit.setValue(rec.data.upperlimit);
                    }
                }else if(rec.data.applieduponid==Wtf.ApprovalRules_AppliedUpon.Journal_Entry_Creator){
                    this.jeCreator.setValue(rec.data.creator);
                }
            }
            if (this.productOptimizedFlag == undefined || this.productOptimizedFlag != Wtf.Show_all_Products) {
                if (rec.data.applieduponid == Wtf.ApprovalRules_AppliedUpon.Specific_Products || rec.data.applieduponid == Wtf.ApprovalRules_AppliedUpon.Specific_Products_Discount) {
                    this.productCombo.setValForRemoteStore(rec.data.ruleproductids, rec.data.rule);
                }
            }
            this.productCategoryStore.on('load',function(){
                if(rec.data.applieduponid==Wtf.ApprovalRules_AppliedUpon.Specific_Products_category){
                    this.Category.setValue(rec.data.ruleproductids);
                }                
            },this);
        }
    },
    
    resetFields : function() {
        this.jeCreator.reset();
        this.productCombo.reset();
        this.ruleCombo.reset();
        this.limit.reset();
        this.ulimit.allowBlank=true;
        this.ulimit.setRawValue("");
    },
    
    deteleRequisitionRule : function() {
        var higherLevelRule =false;
        var selectedLevel=this.RuleGrid.getSelectionModel().getSelected().data.level;
        for(var cnt=0;cnt<this.reqRuleds.getCount();cnt++)
            {
                if(this.reqRuleds.getAt(cnt).data.level>selectedLevel) {
                    higherLevelRule=true;break;
                }    
            }
            if(higherLevelRule){
                Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.field.higherLevelRuleSet"));
                return;
        }
        var rec = this.RuleGrid.getSelectionModel().getSelected();
        if(rec) {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttodeleteselectedrule"),function(btn){
            if(btn=="yes") {
                    var formRecord = this.RuleGrid.getSelectionModel().getSelected();
                    Wtf.Ajax.requestEx({
                        url: "MultiLevelApprovalRule/deleteMultiApprovalRule.do",
                        params: {
                            id : formRecord.data.id,
                            level : formRecord.data.level,
                            rule :  formRecord.data.rule
                        }
                    },this,this.genSuccessResponseRule,this.genFailureResponseRule);
                }
            }, this)
        } else {
            WtfComMsgBox(5,2);
        }
    },
    genSuccessResponseRule : function(response){
        if (response.success === true || response.msg != '' ) {
                Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function () {
            this.reqRuleds.load({
                        params: {
                            start: 0,
                            limit: this.pagingToolbar.pageSize,
                            moduleid: this.transactionCombo.getValue()
                }
            });
        }, this);
                }
    },
    genFailureResponseRule : function(response){
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.Erroroccurredwhiledeletingrule")],2);
    }
});

