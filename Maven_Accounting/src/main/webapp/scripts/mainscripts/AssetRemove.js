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
Wtf.assetremove = function(config){
    this.record=config.record;
	this.isWriteOff = config.isWriteOff;
	this.costCenterID = this.record.data['costcenterid'];
	
	Wtf.apply(this,{
        constrainHeader :true,
        buttons: [{
            text: (this.isWriteOff)?WtfGlobal.getLocaleText("acc.rem.158"):WtfGlobal.getLocaleText("acc.rem.159"),
            scope: this,
            handler:this.saveForm.createDelegate(this)
        }, {
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),  //'Cancel',
            scope: this,
            handler:this.closeForm.createDelegate(this)
        }]
    },config);
    Wtf.assetremove.superclass.constructor.call(this, config);
    
    this.addEvents({
        'loadingcomplete':true,
        'update':true,
        'datachanged':true
    });
    
},

Wtf.extend(Wtf.assetremove, Wtf.Window, {

onRender: function(config) {
	
    Wtf.assetremove.superclass.onRender.call(this, config);
    this.createStore();
    this.createfields();
    this.createForm();
    
	var msg = (this.isWriteOff)?WtfGlobal.getLocaleText("acc.fixedAssetList.writeOffAsset"):WtfGlobal.getLocaleText("acc.fixedAssetList.sellAsset");
    this.add({
        region: 'north',
        height: 75,
        border: false,
        bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
        html: getTopHtml(msg,msg,"../../images/accounting_image/Chart-of-Accounts.gif")
    });
    this.add(this.assetForm);
    
    
    Wtf.Ajax.requestEx({
        url:"ACCAccountCMN/getNetAssetValue.do",
        params: {fixedAssetID : this.record.data.accid}
    },this,function(resp){var netAssetValue = resp.netAssetValue + this.record.data.openbalanceinbase;
    						this.balanceSheetvalue.setValue(netAssetValue);});
    
},


createStore:function(){
    
	this.accountRec = Wtf.data.Record.create ([
         {name: 'accid'},
         {name: 'accname'}
    ]);
    
    this.accountStore=new Wtf.data.Store({
         url:"ACCAccountCMN/getAccountsForCombo.do",
         baseParams:{
        	 mode:2,
             nondeleted:true
         },
         reader: new Wtf.data.KwlJsonReader({
             root: "data"
         },this.accountRec)
    });
    
    if(this.isWriteOff)
            this.accountStore.load({
                params:{
                    ignoreCashAccounts:true,
                    ignoreBankAccounts:true,
                    ignoreGSTAccounts:true,  
                    ignorecustomers:true,  
                    ignorevendors:true
                    }
                });
    else
        this.accountStore.load();
},
 

createForm:function(){
    this.assetForm=new Wtf.form.FormPanel({
        region:'center',
        width:400,
        height:350,
        labelWidth:150,
        border:false,
        bodyStyle: "background:transparent; padding: 20px 10px 0px 10px",
        style: "background: transparent;padding-left:15px;",
        defaultType: 'textfield',
        items:[this.balanceSheetvalue,this.Price,this.Memo,this.Account]
    }); 
}, 

createfields:function(){
	this.Price = new Wtf.form.NumberField({
		name:"amount",
        allowBlank:this.isWriteOff,
        allowNegative: false,
        fieldLabel:WtfGlobal.getLocaleText("acc.rem.160"),  //"Selling Price (In Home Currency) *",
        id:"amount",
        maxLength:15,
        decimalPrecision:2,
        minValue: 1,
        width:160,
        hidden: this.isWriteOff,
        hideLabel: this.isWriteOff    
	});
	
	this.balanceSheetvalue = new Wtf.form.NumberField({
		name: "netvalue",
		id: "netvalue",
		fieldLabel:WtfGlobal.getLocaleText("acc.rem.161"),  // "Net Book Value of Asset (In Home Currency)",
		decimalPrecision:2,
		readOnly: true,
		cls:"clearStyle"
	});
	
	this.Account=new Wtf.form.FnComboBox({
        fieldLabel:this.isWriteOff?WtfGlobal.getLocaleText("acc.rem.162"):WtfGlobal.getLocaleText("acc.rem.163"),  //,
        store:this.accountStore,
        width:160,
        name:'accountid',
        hiddenName:'hiddenAccountid',
        valueField:'accid',
        forceSelection: true,
        displayField:'accname',
        allowBlank: false,
        disabled: !this.isWriteOff
    });
	
	this.Memo=new Wtf.form.TextArea({
        fieldLabel: WtfGlobal.getLocaleText("acc.rem.164"),  //'Reason for Write Off*',
        name: 'memo',
        width: 160,
        maxLength:200,
        allowBlank: !this.isWriteOff,
        hidden: !this.isWriteOff,
        hideLabel: !this.isWriteOff
    });
	
	this.Price.on('blur',function(){if(this.Price.getValue() != this.balanceSheetvalue.getValue() && this.Price.getValue() != ""){
    										this.Account.setDisabled(false);this.Account.allowBlank = false; 
    									}else{
    										this.Account.setDisabled(true);this.Account.allowBlank = true;}
	 									
										this.Account.setValue(""); 
	 									
	 									if(!this.isWriteOff  &&  this.Price.getValue() > this.balanceSheetvalue.getValue()){
	 										this.accountStore.load({params:{group:[15]}});
	 									}else if(!this.isWriteOff  &&  this.Price.getValue() < this.balanceSheetvalue.getValue()){
	 										this.accountStore.load({params:{group:[8]}});
	 									}
	 									
	 							   },this);
},

saveForm:function(){

    var isValid = this.assetForm.getForm().isValid();
    
	if(!isValid){
        WtfComMsgBox(2,2);
    }
    else{    
		if(this.isWriteOff){
			WtfGlobal.fetchAutoNumber(0, function(resp){this.autoGenJEno = resp.data;
														this.createSellOffJE(this.record.data.accid, this.Account.getValue(), this.balanceSheetvalue.getValue(), this.balanceSheetvalue.getValue(), "","","","","Fixed Asset Write Off "+" - "+this.Memo.getValue(), "Fixed Asset Write Off");}, this);
		}else{
//			this.sale = true;
//			this.sellingPrice = this.Price.getValue();
//			if(this.sellingPrice != this.balanceSheetvalue.getValue()){
//				if(this.balanceSheetvalue.getValue() > this.sellingPrice)
//					var remainingAmount = this.balanceSheetvalue.getValue() - this.sellingPrice;
//				else	
//					var remainingAmount = this.sellingPrice - this.balanceSheetvalue.getValue();
//				
//				WtfGlobal.fetchAutoNumber(0, function(resp){this.autoGenJEno = resp.data;
//				this.createSellOffJE(this.record.data.accid, this.Account.getValue(), remainingAmount, "Fixed Asset Sell Off Profit or Loss Entry.", "Fixed Asset Profit or Loss after Sale");}, this);
//			}
//			WtfGlobal.fetchAutoNumber(0, function(resp){this.autoGenJEno = resp.data;
//														this.createSellOffJE(this.record.data.accid, Wtf.account.companyAccountPref.cashaccount, this.sellingPrice, "Fixed Asset Sell Off.", "Fixed Asset Sale");}, this);
			
			this.sellingPrice = this.Price.getValue();
			if(this.sellingPrice == this.balanceSheetvalue.getValue()){
				WtfGlobal.fetchAutoNumber(0, function(resp){this.autoGenJEno = resp.data;
															this.createSellOffJE(this.record.data.accid, Wtf.account.companyAccountPref.cashaccount, this.sellingPrice,this.sellingPrice, "","","","", "Fixed Asset Sell Off. (No-Profit / No-Loss)", "Fixed Asset Sale");}, this);
			}
			if(this.sellingPrice != this.balanceSheetvalue.getValue()){
				if(this.balanceSheetvalue.getValue() > this.sellingPrice){
					var remainingAmount = this.balanceSheetvalue.getValue() - this.sellingPrice;
					WtfGlobal.fetchAutoNumber(0, function(resp){this.autoGenJEno = resp.data;
																this.createSellOffJE(this.record.data.accid, Wtf.account.companyAccountPref.cashaccount, this.balanceSheetvalue.getValue(), this.sellingPrice, this.Account.getValue(), "", remainingAmount, "", "Fixed Asset Sell Off - Loss Entry.", "Fixed Asset Loss after Sale");}, this);
				}	
				else{	
					var remainingAmount = this.sellingPrice - this.balanceSheetvalue.getValue();
					WtfGlobal.fetchAutoNumber(0, function(resp){this.autoGenJEno = resp.data;
																this.createSellOffJE(this.record.data.accid, Wtf.account.companyAccountPref.cashaccount, this.balanceSheetvalue.getValue(), this.sellingPrice, "", this.Account.getValue(), "", remainingAmount, "Fixed Asset Sell Off - Profit Entry.", "Fixed Asset Profit after Sale");}, this);
				}
				
			}
		}
		

	    WtfComMsgBox([WtfGlobal.getLocaleText("acc.fixedAssetList.removeAsset"),WtfGlobal.getLocaleText("acc.main.fadel")],0,false);
    }
},

createFixedAssetSellOffJE:function(creditAccount, debitAccount, Amount, memo, JEDmemo){
	var creditJEDetail = '{debit:"'+false+'",accountid:"'+creditAccount+'",description:"'+JEDmemo+'",amount:'+Amount+"}";
	var debitJEDetail = '{debit:"'+true+'",accountid:"'+debitAccount+'",description:"'+JEDmemo+'",amount:'+Amount+"}";
	this.JERecord={costcenter: this.costCenterID,
					currencyid: WtfGlobal.getCurrencyID(),
					entrydate: WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime()),
					entryno: this.autoGenJEno,
					memo: memo,
					detail: "["+debitJEDetail+","+creditJEDetail+"]"
				  };
},

createSellOffJE:function(creditAccount, debitAccount, cAmount, dAmount, lossAccount, profitAccount, lossAmount, profitAmount, memo, JEDmemo){
		if(profitAccount == "" && lossAccount == "")
			this.createFixedAssetSellOffJE(creditAccount, debitAccount, cAmount, memo, JEDmemo);
		else if(profitAccount != "")
			this.createFixedAssetSellOffJEProfit(creditAccount, debitAccount, cAmount, dAmount, profitAccount, profitAmount, memo, JEDmemo);
		else if(lossAccount != "")
			this.createFixedAssetSellOffJELoss(creditAccount, debitAccount, cAmount, dAmount, lossAccount, lossAmount, memo, JEDmemo);
		
		Wtf.Ajax.requestEx({
          url:"ACCJournal/saveJournalEntry.do",
          params: this.JERecord
      },this,this.onSuccess);
},

onSuccess:function(response){
	this.deleteJe = response.id;
	this.deleteAsset();
	
//	if(!this.isWriteOff && this.sale){
//		if(this.sellingPrice != this.balanceSheetvalue.getValue()){
//			if(this.balanceSheetvalue.getValue() > this.sellingPrice)
//				var remainingAmount = this.balanceSheetvalue.getValue() - this.sellingPrice;
//			else	
//				var remainingAmount = this.sellingPrice - this.balanceSheetvalue.getValue();
//			
//			WtfGlobal.fetchAutoNumber(0, function(resp){this.autoGenJEno = resp.data;
//			this.createSellOffJE(this.record.data.accid, this.Account.getValue(), remainingAmount, "Fixed Asset Sell Off Profit or Loss Entry.", "Fixed Asset Profit or Loss after Sale");}, this);
//		}
//		this.sale = false;
//	}
},

deleteAsset:function(){
	Wtf.Ajax.requestEx({
        url:"ACCAccountCMN/removeAsset.do",
        params: {fixedAssetID : this.record.data.accid,
        		 isWriteOff : this.isWriteOff,
        		 deleteJe : this.deleteJe}
    },this,this.onsuccessRemove);
},

onsuccessRemove:function(response){
	Wtf.getCmp("fixedAssetReport").grid.store.reload();
	this.closeForm();
},

closeForm:function(){

//     this.fireEvent("update");
     this.close();
     Wtf.getCmp("fixedAssetReport").store.reload();
},



createFixedAssetSellOffJELoss:function(creditAccount, debitAccount, cAmount, dAmount, lossAccount, lossAmount, memo, JEDmemo){
	var creditJEDetail = '{debit:"'+false+'",accountid:"'+creditAccount+'",description:"'+JEDmemo+'",amount:'+cAmount+"}";
	var debitJEDetail = '{debit:"'+true+'",accountid:"'+debitAccount+'",description:"'+JEDmemo+'",amount:'+dAmount+"}";
	var lossJEDetail = '{debit:"'+true+'",accountid:"'+lossAccount+'",description:"'+JEDmemo+'",amount:'+lossAmount+"}";
	this.JERecord={costcenter: this.costCenterID,
					currencyid: WtfGlobal.getCurrencyID(),
					entrydate: WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime()),
					entryno: this.autoGenJEno,
					memo: memo,
					detail: "["+debitJEDetail+","+creditJEDetail+","+lossJEDetail+"]"
				  };
},

createFixedAssetSellOffJEProfit:function(creditAccount, debitAccount, cAmount, dAmount, profitAccount, profitAmount, memo, JEDmemo){
	var creditJEDetail = '{debit:"'+false+'",accountid:"'+creditAccount+'",description:"'+JEDmemo+'",amount:'+cAmount+"}";
	var debitJEDetail = '{debit:"'+true+'",accountid:"'+debitAccount+'",description:"'+JEDmemo+'",amount:'+dAmount+"}";
	var profitJEDetail = '{debit:"'+false+'",accountid:"'+profitAccount+'",description:"'+JEDmemo+'",amount:'+profitAmount+"}";
	this.JERecord={costcenter: this.costCenterID,
					currencyid: WtfGlobal.getCurrencyID(),
					entrydate: WtfGlobal.convertToGenericDate(Wtf.serverDate.clearTime()),
					entryno: this.autoGenJEno,
					memo: memo,
					detail: "["+debitJEDetail+","+creditJEDetail+","+profitJEDetail+"]"
				  };
}


});