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
////
/*this.type
 *0-Cash
 *1-Card
 *2-Bank*/


Wtf.account.PayMethodPanel = function(config) {
    this.isReceipt=config.isReceipt;
    Wtf.account.PayMethodPanel.superclass.constructor.call(this, config);
};
Wtf.extend(Wtf.account.PayMethodPanel, Wtf.form.FormPanel,{
    labelWidth:150,
    cls:"visibleDisabled",
    onRender: function(config) {
        this.createStore();
        this.createFields();
        this.add(this.cheque);
        this.expDate.on('change',this.checkExpDate,this);
        Wtf.account.PayMethodPanel.superclass.onRender.call(this, config);
    },

    createStore:function(){
        this.bankRec=new Wtf.data.Record.create([
            {name: 'id'},
            {name: 'name'}]);

        this.bankTypeStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.bankRec),
            url:"ACCMaster/getMasterItems.do",
            baseParams:{
                mode:112,
                groupid:2
            }
        });
        this.bankTypeStore.load();
    },

    createFields:function(){
    this.bank= (!this.isReceipt? new Wtf.form.TextField({
        name:"paymentthrough",
        fieldLabel:WtfGlobal.getLocaleText("acc.nee.47"),  //'Bank From Name*',
        //allowBlank:false,
        anchor: '90%',
        maxLength:50
    }):new Wtf.form.FnComboBox({
        fieldLabel:WtfGlobal.getLocaleText("acc.setupWizard.BankName"),  //'Bank Name*',
        name:"paymentthrough",
        hiddenName:'paymentthrough',
        store:this.bankTypeStore,
        anchor:'90%',
        listWidth:250,
        //allowBlank:false,
        valueField:'id',
        displayField:'name',
        mode: 'local',
        triggerAction:'all',
        forceSelection:true
    }));

        if(!WtfGlobal.EnableDisable(Wtf.UPerm.masterconfig, Wtf.Perm.masterconfig.create))
            this.bank.addNewFn= this.addMaster.createDelegate(this,[2,this.bankTypeStore])

    this.checkNo = new Wtf.form.TextField({
        fieldLabel:WtfGlobal.getLocaleText("acc.nee.46.Cheque"),  //Cashier's Cheque/Cheque Number:",
        //allowBlank:false,
        name: 'chequenumber',
        maxLength:16,
//        maskRe:/^\d+$/,
        vtype : "alphanum",
        allowNegative:false,
        anchor: '80%'
    });
     this.PostDate=new Wtf.form.DateField({
        name:"postdate",
        anchor: '90%',
        allowBlank:false,
        fieldLabel:WtfGlobal.getLocaleText("payment.date.postDate")+"*",
        format:WtfGlobal.getOnlyDateFormat()
    });
    this.cardNo=new Wtf.form.NumberField({
        fieldLabel:WtfGlobal.getLocaleText("acc.field.CardNumber"),
        name:"CardNo",
        maxLength: 16,
        minLength:16,
        anchor: '90%'
    });

    this.description =new Wtf.form.TextArea({
        name:"description",
        height:40,
        anchor: '90%',
        fieldLabel:WtfGlobal.getLocaleText("acc.nee.46.Description"),  //Reference Number/ Description:
        maxLength: 255
    });

    this.expDate=new Wtf.form.DateField({
        name:"expirydate",
        anchor: '90%',
        fieldLabel:WtfGlobal.getLocaleText("acc.field.ExpiryDate"),
        format:WtfGlobal.getOnlyDateFormat()
    });

    this.clearanceDate=new Wtf.form.DateField({
        name:"clearancedate",
        anchor: '80%',
        fieldLabel:WtfGlobal.getLocaleText("acc.bankReconcile.clearanceDate")+"*",//"Clearance Date*",
        format:WtfGlobal.getOnlyDateFormat()
    });

    this.clearanceDate.on('render',function(){
        this.clearanceDate.getEl().up('.x-form-item').applyStyles("margin-top:16px;")
        this.clearanceDate.getEl().up('.x-form-item').setDisplayed(false);
    },this);
    this.clearanceDate.on('change',this.checkForClearanceDateValidation,this);
    this.paymentStatusStore = new Wtf.data.SimpleStore({
        fields: ['statusValue', 'statusName'],
        data :[['Cleared','Cleared'],['Uncleared','Uncleared']]
    });

    this.paymentStatus = new Wtf.form.ComboBox({
        store: this.paymentStatusStore,
        fieldLabel:WtfGlobal.getLocaleText("acc.mp.pmtstatus")+"*",//"Payment Status*",
        name:'paymentstatus',
        displayField:'statusName',
        value:'Uncleared',
        editable:false,
        //allowBlank:false,
        anchor:"80%",
        valueField:'statusValue',
        mode: 'local'
        //triggerAction: 'all'
        /*Changes done to show only uncleared value in combobox (Ticket--> ERM-1004)*/
    });

    this.paymentStatus.on('select',function(combo, record, index){
        if(record.data['statusName']=="Cleared")
            this.clearanceDate.getEl().up('.x-form-item').setDisplayed(true);
        else{
            this.clearanceDate.getEl().up('.x-form-item').setDisplayed(false);
        }
    },this);

    this.paymentStatus.on('change',function(){
        if(this.paymentStatus.getValue()=="Cleared")
            this.clearanceDate.getEl().up('.x-form-item').setDisplayed(true);
        else{
            this.clearanceDate.getEl().up('.x-form-item').setDisplayed(false);
        }
    },this);

    this.nameOnCard = new Wtf.form.TextField({
        name:"nameoncard",
        fieldLabel:WtfGlobal.getLocaleText("acc.field.CardHolderName*"),
        //allowBlank:false,
        anchor: '90%',
        maxLength:50
    });
    this.typeStore = new Wtf.data.SimpleStore({
        fields: [{name:'typeid',type:'int'}, 'name'],
        data :[['0','Master Card'],['1','Visa']]
    });
    this.cardType = new Wtf.form.TextField({
        name:"cardtype",
        maxLength:20,
        fieldLabel:"Card Type:<br><span CLASS=\"x-formsmaller-item\">(eg. Master Card)</font></span>",
        anchor: '90%'
    });

    this.refNo=new Wtf.form.TextField({
        fieldLabel:WtfGlobal.getLocaleText("acc.nee.45"),
        name:"refno",
        maxLength:49,
        vtype : "alphanum",
        //allowBlank:false,
        anchor:'90%',
        allowNegative:false
    });

    this.creditCard=new Wtf.form.FieldSet({
        title:WtfGlobal.getLocaleText("acc.mp.payMethodDetails"),
        id:this.id+'card',
        height:92,
        layout:'column',
        items:[{
            layout:'form',
            columnWidth:0.34,
            border:false,
            items:[this.refNo,this.cardNo]
           },{
            layout:'form',
            columnWidth:0.33,
            border:false,
            items:[this.nameOnCard,this.cardType]
           },{
            layout:'form',
            columnWidth:0.32,
            border:false,
            items:[this.expDate]
           }]
        });
        this.cheque=new Wtf.form.FieldSet({
            title:WtfGlobal.getLocaleText("acc.mp.payMethodDetails"),  //'Cheque Details',
            id:this.id+'cheque',
            bodyStyle:'padding:10px',
            height:100,
            layout:'column',
            defaults:{border:false},
            items:[{
                layout:'form',
                columnWidth:0.34,
                items:[this.checkNo,this.paymentStatus, this.refNo,this.cardNo]
               },{
                layout:'form',
                columnWidth:0.33,
                items:[this.bank,this.clearanceDate, this.nameOnCard,this.cardType]
               },{
                layout:'form',
                columnWidth:0.32,
                items:[this.PostDate,this.description, this.expDate]
               }]
        });
    },
    /*
     *Clearance date should be less than Cheque Date
     */
    checkForClearanceDateValidation:function(field,newval,oldval){
        if(field.getValue()!='' && this.PostDate.getValue()!=''){
            if(field.getValue().getTime()<this.PostDate.getValue().getTime()){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.mprp.clearanceDateCanNotBeLessThanChequedate")], 2);
                field.setValue(oldval);                    
            }
        }
    },
    ShowCheckDetails:function(type, parentObj){
        this.type = type;
        if(type==2){
            this.expDate.setValue(new Date());
            this.checkNo.setValue("");
            this.PostDate.setValue(new Date());
            this.cardType.setValue('0');
            this.refNo.setValue("1");
            this.bank.setValue("");
            this.nameOnCard.setValue("a");
        }else if(type==1){
            this.expDate.setValue(new Date());
            this.refNo.setValue("");
            this.cardType.setValue('');
            this.PostDate.setValue(new Date());
            this.checkNo.setValue("");
            this.bank.setValue("a");
            this.nameOnCard.setValue("");
        }else{
            this.refNo.setValue("1");
            this.cardType.setValue('0');
            this.expDate.setValue(new Date());
            this.bank.setValue("a");
            this.checkNo.setValue("1");
            this.PostDate.setValue(new Date());
            this.nameOnCard.setValue("a");
        }
        this.doLayout();
    },
    GetPaymentFormData:function(){
        var bankname="";
        if(this.type==2){
            var bankindex=this.bankTypeStore.find("id",this.bank.getValue());
            bankname=bankindex<0?"":this.bankTypeStore.getAt(bankindex).data["name"];
        }
        var data="{}";
        switch(this.type){
            case 1:data="{refno:'"+this.refNo.getValue()+"',cardno:'"+this.cardNo.getValue()+"',nameoncard:'"+this.nameOnCard.getValue()+"',cardtype:'"+this.cardType.getValue()+"',expirydate:'"+WtfGlobal.convertToGenericDate(this.expDate.getValue())+"'}";
                break;
            case 2:data="{chequeno:'"+this.checkNo.getValue()+"',bankname:'"+(!this.isReceipt?(escape(this.bank.getValue())):bankname)+"',bankmasteritemid:'"+(!this.isReceipt?null:this.bank.getValue())+"',paymentStatus:'"+this.paymentStatus.getValue()+"',clearanceDate:'"+(this.paymentStatus.getValue()=="Cleared" ? WtfGlobal.convertToGenericDate(this.clearanceDate.getValue()): null)+"',description:"+"'"+escape(this.description.getValue())+"',payDate:"+"'"+ WtfGlobal.convertToGenericDate(this.PostDate.getValue())+"'"+"}";
                break;
        }
        return data;
    },

    addMaster:function(id,store){
        addMasterItemWindow(id);
        Wtf.getCmp('masterconfiguration').on('update', function(){
            store.reload();
        }, this);
    },
    checkExpDate:function(obj,nval,oval){
        if(nval<(new Date()))
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.msgbox.CONFIRMTITLE"),WtfGlobal.getLocaleText("acc.field.CardisexpiredDoyouwishtocontinue"),function(btn){
                if(btn!="yes") {obj.setValue(oval);return};
        },this)
    },
    setNextChequeNumber:function(bankAccountId){
        // set Next Cheque Number
        if(Wtf.account.companyAccountPref.showAutoGeneratedChequeNumber){
            Wtf.Ajax.requestEx({
                url : "ACCVendorPayment/getNextChequeNumber.do",
                params:{
                    bankAccountId:bankAccountId
                }
            },this,
            function(req,res){
                var restext=req;
                if(restext.success){
                    this.checkNo.setValue(restext.nextChequeNumber)
                }
            },
            function(req){

                });
        }
    },
    setBankName:function(val){
        this.bank.setValue(val)
    }
});
