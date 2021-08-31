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
Wtf.account.ReconciliationWindow = function(config){
    this.reconRec=(config.reconRec==undefined?"":config.reconRec);
    this.continueBtn = new Wtf.Button({
        text: WtfGlobal.getLocaleText("acc.common.continueBtn"),  //'Continue',
        scope: this,
        disabled : (Wtf.account.companyAccountPref.columnPref.activateBankReconcilitaionDraft == true),
        handler: this.saveData.createDelegate(this)
    })
    Wtf.apply(this,{
        title:WtfGlobal.getLocaleText("acc.dashboard.bankReconciliation"),  //"Bank Reconciliation",
        buttons: [this.continueBtn,{
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            scope: this,
            hidden : (config.reconRec==undefined? false :config.reconRec.isFromReconcilation),
            handler:this.closeWin.createDelegate(this)
        }]
    },config);
    Wtf.account.ReconciliationWindow.superclass.constructor.call(this, config);
    this.addEvents({
        'update':true
    });
}
Wtf.extend( Wtf.account.ReconciliationWindow, Wtf.Window, {

    onRender: function(config){
        var image="../../images/accounting_image/bank-reconciliation.jpg";
        Wtf.account.ReconciliationWindow.superclass.onRender.call(this, config);
        this.createStore();
        this.createForm();
        this.createDraftPanel();
        this.add({
            region: 'north',
            height:75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html:getTopHtml(WtfGlobal.getLocaleText("acc.bankReconcile.tab"),WtfGlobal.getLocaleText("acc.bankReconcile.BRinfo"),image,false)
        },{
            region: 'center',
            border: false,
            baseCls:'bckgroundcolor',
            layout: 'column',
            height:400,
            items:[this.Form,this.draftPanel]
        });
   },
   createStore:function(){
       this.allAccountRec = new Wtf.data.Record.create([
            {name: 'accid'},
            {name: 'accname'},
            {name: 'acccode'},
            {name: 'groupid'},
            {name: 'groupname'}
//            {name: 'level'},
//            {name: 'leaf'},
//            {name: 'openbalance'},
//            {name: 'parentid'},
//            {name: 'parentname'}
        ]);
        this.allAccountStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.allAccountRec),
//            url: Wtf.req.account +'CompanyManager.jsp',
            url : "ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreGLAccounts:true,
                ignoreCashAccounts:true,
                ignoreGSTAccounts:true,  
                ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true
            }
        });
        this.allAccountStore.on('load',this.setAccount,this);        
        this.allAccountStore.load();
   },
   setAccount:function(){
       var value=(this.reconRec==""?"":this.reconRec.accountid)
       this.Account.setValue(value)
   },
    createForm:function(){
        this.Account= new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.account")+"*",
            hiddenName:"accountid",
            anchor:"93%",
            store: this.allAccountStore,
            valueField:'accid',
            displayField:'accname',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode']:[],
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            typeAheadDelay:30000,
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?350:250,
            allowBlank:false,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.mp.selAcc"),  //'Please select an Account...',
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            triggerAction:'all',
            scope:this
        });
       this.startDate= new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.startDate"),
            format:WtfGlobal.getOnlyDateFormat(),
//            value:(this.reconRec==""?this.getDates(true):this.reconRec.startdate),
            value:(this.reconRec && this.reconRec.startdate) ? this.reconRec.startdate :"",
            anchor:'93%',
            name:"startdate",
            allowBlank:true
        });
        this.statementDate= new Wtf.form.DateField({
            fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.statementDate")+'*',
            format:WtfGlobal.getOnlyDateFormat(),
//            value:(this.reconRec==""?this.getDates(false):this.reconRec.statementdate),
            value:(this.reconRec==""?new Date():this.reconRec.statementdate),
            anchor:'93%',
            name:"statementdate",
            allowBlank:false
        });
        this.lastReconciledAmount="-";
        this.lastReconciledDate="-";
        this.reconcileAmountField=new Wtf.form.NumberField({
            allowNegative:true,
            allowBlank:true,
            maxLength: 15,
            width:100,
            anchor:'93%',
            defaultValue:0,
            value:(this.reconRec && this.reconRec.newstatementbalance) ? this.reconRec.newstatementbalance :0,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            fieldLabel:WtfGlobal.getLocaleText("acc.bankreconcileAmount"),  //'Discount',
            name:'reconcileAmount'
        });
        
        
        this.Account.on('select',this.setOpeningBalance,this)
        this.Account.on('select',this.getDraftInfo,this);
        this.startDate.on('change',this.setOpeningBalance,this)
        this.beginingBalance=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.openBalance")+' '+WtfGlobal.getCurrencySymbolForForm(),
            name: 'openingbalance',
            anchor:'93%',
            hidden:true,
            hideLabel:true,
            maxLength:45,
            value:(this.reconRec==""?0:this.reconRec.openingbalance),
            scope:this,
            readOnly:true,
            disabled: true	// Bug No 19950 Fixed 
        });
        this.endingBalance=new Wtf.form.NumberField({
            name:"endingbalance",
//            allowBlank:false,
            hidden:true,
            hideLabel:true,
            fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.endingBalanceNew")+" "+WtfGlobal.getCurrencySymbolForForm()+"*",
            maxLength:15,
            decimalPrecision:Wtf.AMOUNT_DIGIT_AFTER_DECIMAL,
            value:(this.reconRec==""?0:this.reconRec.endingbalance),
            anchor:'93%'
        });
         this.Form=new Wtf.form.FormPanel({
            region:'north',
//            height:(Wtf.isIE)?220:190,
            height:300,
            columnWidth : Wtf.account.companyAccountPref.columnPref.activateBankReconcilitaionDraft ? 0.5 : 1,
            border:false,
            bodyStyle: 'overflow: hidden',
            items:[{
                layout:'form',
                bodyStyle: "background: transparent; padding: 20px;",
                labelWidth:150,
                border:false,
                height:300,
                items:[this.Account,this.startDate,this.statementDate,this.reconcileAmountField,this.beginingBalance,this.endingBalance]
           }]
        });
    },
    getDraftInfo : function(){
        if(Wtf.account.companyAccountPref.columnPref && Wtf.account.companyAccountPref.columnPref.activateBankReconcilitaionDraft){
            Wtf.Ajax.requestEx({
                url:"ACCReconciliation/getBankReconcilationDrafts.do",
                params:{
                    accountid:this.Account.getValue()
                }
            }, this,function (response) {
                if (response.success) {
                    if(response.data && response.data.length >0){
                        this.draftInfo = response.data[0] || {};
                        this.loadDraftInfo();
                    }else{
                        this.draftEmptyTextSummary.overwrite(this.draftPanel.body,{});
                        this.continueBtn.enable();
                    }
                } 
            });
        }
    },
    loadDraftInfo : function(){
        
        try{
            if(this.draftInfo.todate ){
                if(typeof this.draftInfo.todate == "string"){
                    this.draftInfo.todate = new Date(this.draftInfo.todate);
                }
                this.draftInfo.statementdate = this.draftInfo.todate.format(WtfGlobal.getOnlyDateFormat());
            }
            if(this.draftInfo.fromdate){
                 if(typeof this.draftInfo.fromdate == "string"){
                    this.draftInfo.fromdate = new Date(this.draftInfo.fromdate);
                }
                this.draftInfo.startdate = this.draftInfo.fromdate.format(WtfGlobal.getOnlyDateFormat());
            }
            
        }catch(e){
            clog(e)
        }
        
        this.draftInfoSummary.overwrite(this.draftPanel.body,this.draftInfo);
        this.recallBtn = new Wtf.Button({
            text :"Recall",
            scope : this,
            handler : this.recallDraft,
            renderTo :"recall"
        });
        this.deleteBtn = new Wtf.Button({
            renderTo :"delete",
            scope : this,
            handler : this.deleteDraft,
            text :"Delete" 
        });
        this.continueBtn.disable();
    },
    recallDraft : function(){
        var data = this.draftInfo || {};
        this.Account.setValue(data.accountid);
        this.startDate.setValue(data.fromdate);
        this.statementDate.setValue(data.todate);
        this.reconcileAmountField.setValue(data.newstatementbalance);
        
        this.continueBtn.enable();
    },
    deleteDraft : function(){
        Wtf.MessageBox.show({
            title : WtfGlobal.getLocaleText('acc.dashboard.bankReconciliation'),
            msg : WtfGlobal.getLocaleText("acc.invoiceList.deletedocumentmsg") + "</br></br><b>" + WtfGlobal.getLocaleText('acc.customerList.delTT1') + "</b>",
            width : 520,
            closable : false,
            fn : function(btnText){
                if(btnText){
                    if(btnText == "yes"){
                        Wtf.Ajax.requestEx({
                            url:"ACCReconciliation/deleteBankReconcilationDrafts.do",
                            params:{
                                accountid:this.Account.getValue()
                            }
                        }, this,function (response) {
                            if (response.success) {
                                WtfComMsgBox([WtfGlobal.getLocaleText('acc.dashboard.bankReconciliation'),response.msg]);
                                this.draftInfo = {};
                                this.draftEmptyTextSummary.overwrite(this.draftPanel.body,this.draftInfo);
                                this.continueBtn.enable();
                                if(Wtf.getCmp("reconciliationledger")){
                                    Wtf.getCmp("reconciliationledger").getDraftInfoForAllAccounts();
                                }
                            } 
                        });
                    }
                }
            }.createDelegate(this),
            buttons: Wtf.MessageBox.YESNO,
            animEl: 'mb9',
            icon: Wtf.MessageBox.INFO
        });
        
    },
    createDraftPanel : function(){
        
         this.draftInfoSummary=new Wtf.XTemplate(
            '<div><b>Draft Information</b></div>',
            '<hr class="templineview" style="margin-bottom:2px;margin-top: 4px;">',
            '<div class="currency-view">',
            '<table width="100%">',
            '<tbody>',
            '<tr style="margin-bottom:4px;">',
            '<td style="width:45%">Bank Name</td>',
            '<td>:</td>',
            '<td wtf:qtip = "{accountname}">{[Wtf.util.Format.ellipsis(values.accountname|| "",20)]}</td>',
            '</tr>',
            '<tr style="margin-bottom:4px;">',
            '<td style="width:45%">Start Date</td>',
            '<td>:</td>',
            '<td wtf:qtip = "{startdate}">{[Wtf.util.Format.ellipsis(values.startdate || "",20)]}</td>',
            '</tr>',
            '<tr style="margin-bottom:4px;">',
            '<td style="width:45%">Statement Date</td>',
            '<td>:</td>',
            '<td wtf:qtip = "{statementdate}">{[Wtf.util.Format.ellipsis(values.statementdate|| "",20)]}</td>',
            '</tr>',
            '<tr style="margin-bottom:4px;">',
            '<td style="width: 51%;">New Statement Balance</td>',
            '<td>:</td>',
            '<td wtf:qtip = "{newstatementbalance}">{[Wtf.util.Format.ellipsis(values.newstatementbalance|| 0,20)]}</td>',
            '</tr>',
            '<tr style="margin-bottom:4px;">',
            '<td style="width: 51%;">Description</td>',
            '<td>:</td>',
            '<td wtf:qtip = "{description}">{[Wtf.util.Format.ellipsis(values.description|| "",20)]}</td>',
            '</tr>',
            '</tbody></table>',
            '</div>',
            '<hr class="templineview" style="margin-bottom:2px;margin-top: 4px;" >',
            '<div style="display: inline-flex;margin-top: 10px;"><span id = "recall" style="margin-right: 6px;margin-left: 8px;"></span><span id = "delete"></span></div>'
            );
                
         this.draftEmptyTextSummary=new Wtf.XTemplate(
            '<div><b>Draft Information</b></div>',
            '<hr class="templineview" style="margin-bottom:2px;margin-top: 4px;">',
            '<div class="currency-view">',
            '<table width="100%">',
            '<tbody>',
            '<tr style="margin-bottom:4px;">',
            '<td style="width:45%">No drafts present</td>',
            '</tr>',
            '</tbody></table>',
            '</div>'
            );
                
        this.draftPanel = new Wtf.Panel({
//            title : "Drafts",
            height :200,
            border : false,
            bodyStyle: "background: transparent; padding: 20px;",
            columnWidth : 0.5
        });
        this.draftPanel.on("render",function(){
            if(this.reconRec && this.reconRec.draftRec){
                this.draftInfo = this.reconRec.draftRec || {};
                this.loadDraftInfo();
            }else{
                this.draftEmptyTextSummary.overwrite(this.draftPanel.body,{});
            }
        },this);
        
    
    },
    setOpeningBalance:function(a,rec){
        Wtf.Ajax.requestEx({
//            url:Wtf.req.account+'CompanyManager.jsp',
            url:"ACCReports/getAccountOpeningBalance.do",
            params:{
                mode:68,
                stdate:WtfGlobal.convertToGenericDate(this.startDate.getValue()),
                accountid:this.Account.getValue()

            }
        }, this,this.setbalance);
        
        Wtf.Ajax.requestEx({
            url:"ACCReconciliation/getLastReconcileAmountAndDate.do",
            params:{
                accountid:this.Account.getValue()
            }
        }, this,function (response) {
            if (response.success) {
               this.lastReconciledDate=response.lastReconciledDate;
               this.lastReconciledAmount=response.lastReconciledAmount;
            } 
        }, function (response) {
            
        });
    },

    setbalance:function(response){
        if(response.success)
            this.beginingBalance.setValue(response.data.openingbalance);
    },

    closeWin:function(){
         this.fireEvent('cancel',this)
         this.close();
     },

    saveData:function(){
        if(!this.Form.getForm().isValid())
                WtfComMsgBox(2,2);
        else{
            var rec=this.Form.getForm().getValues();
            rec.lastReconciledAmount=this.lastReconciledAmount;
            rec.lastReconciledDate=this.lastReconciledDate;
            
            var data = this.draftInfo || {};
            data.draftRec = this.draftInfo;
            Wtf.apply(data,rec);
            callReconciliationLedger(data)
            this.close();
        }
    },

    genSuccessResponse:function(response){
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.info"),response.msg],response.success*2+1);
         if(response.success) this.fireEvent('update');
        this.store.load();
    },

    genFailureResponse:function(response){
        var msg=WtfGlobal.getLocaleText("acc.common.msg1"); //"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    getDates:function(start){
        var d=new Date();
        if(this.statementType=='BalanceSheet'){
            if(start)
                return new Date('January 1, 1970 00:00:00 AM');
            else
                return d;
        }
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;
        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    }
});
