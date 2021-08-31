
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


Wtf.account.TDSPaymentWindow = function(config){
    this.isReceipt=config.isReceipt,   
    this.accountId=config.accountId,
    this.isEdit=config.isEdit;
    this.invType=config.invType!=undefined?config.invType:"0";// 0= expence Type & 1 = inventory Type | if 0 product column is hide
    this.callFrom=config.callFrom!=undefined?config.callFrom:"";// 0= expence Type & 1 = inventory Type | if 0 product column is hide
    this.appliedGst=config.appliedGst;
    this.parentObj = !Wtf.isEmpty(config.parentObj)?config.parentObj:"";
    this.personInfo = !Wtf.isEmpty(config.personInfo)?config.personInfo:"";
    this.readOnly=config.readOnly;
    this.advancePaymentNop=config.advancePaymentNop;
    this.butnArr = new Array();
    this.selectedAccountName = !Wtf.isEmpty(config.selectedAccountName)?config.selectedAccountName:"";
    if (!this.readOnly) {// do not show submit button in view mode
        this.butnArr.push({
            text: WtfGlobal.getLocaleText("acc.common.submit"), //'Submit',
            scope: this,
            handler: function () {
                this.isSubmitBtnClicked = true;
                this.submitSelectedRecords();
            }
        });
    }
    this.butnArr.push({
        text: this.readOnly?WtfGlobal.getLocaleText("acc.common.close"):WtfGlobal.getLocaleText("acc.CANCELBUTTON"),   //'Cancel',
        scope: this,
        handler: function() {
            this.isSubmitBtnClicked = false;
            this.close();
        }
    });
    Wtf.apply(this,{
        buttons: this.butnArr
    },config);
    Wtf.account.TDSPaymentWindow.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.TDSPaymentWindow, Wtf.Window, {
    height: 560,
    width: 1200,
    modal: true,
    resizable: false,
    iconCls : 'pwnd deskeralogoposition',
    title: 'TDS Payment',
    onRender: function(config){
        Wtf.account.TDSPaymentWindow.superclass.onRender.call(this, config);
        this.createDisplayGrid();  
        
        
        this.creditAccRec = Wtf.data.Record.create([
        {
            name:'accountname',
            mapping:'accname'
        },

        {
            name:'accountid',
            mapping:'accid'
        },

        {
            name:'productaccountid', 
            mapping:'accid'
        },

        {
            name:'acccode'
        },

        {
            name:'groupname'
        }
        //            {name:'level',type:'int'}
        ]);
        this.creditAccountStore = new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
            //            url: Wtf.req.account+'CompanyManager.jsp',
            baseParams:{
                mode:2,
                ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.creditAccRec)
        });
        WtfGlobal.setAjaxTimeOut();
        /* Loaded account Store only when required */
        if(!(!this.isNote ||this.noteTemp) || Wtf.account.companyAccountPref.AllowToMapAccounts){
            this.creditAccountStore.load();
        }
        this.creditAccountStore.on('load',function(store, rec){
            WtfGlobal.resetAjaxTimeOut();
        },this);
        
        this.cmbCreditAccount=new Wtf.form.ExtFnComboBox({
            fieldLabel:"Credit Account",
            hiddenName:'accountid',
            store:this.creditAccountStore,
            minChars:1,
            valueField:'accountid',
            displayField:'accountname',
            forceSelection:true,
            hirarchical:true,
            //                    addNewFn:this.openCOAWindow.createDelegate(this),
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
            mode: 'local',
            typeAheadDelay:30000,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400
        });   
        this.creditAccountStore.load();  
        
        var bodyDesign="";
        switch(this.record.data["type"]){
            case 1:
                bodyDesign+="<span><b>Document Type :</b> Advanced / Deposit Payment</span></br>";
                bodyDesign+="<span><b>Vendor Name :</b> "+this.personInfo.personName+"</span></br>";
                bodyDesign+="<span><b>Deductee Type :</b> "+this.personInfo.deducteetypename+"</span></br>";
                bodyDesign+="<span><b>Residential Status :</b> "+(this.personInfo.residentialstatus == 0 ? " Resident" : 
                        (this.personInfo.residentialstatus == 1 ? "Non-Resident": "Not Available")) +"</span></br>";                 
                bodyDesign+="<span><b>Total Amount :</b> "+this.record.data["enteramount"]+"</span></br>";                               
                break;
            case 2:
                bodyDesign+="<b>Document Type :</b> Invoice Payment</br>";
                bodyDesign+="<b>Document Number :</b> "+this.record.data["documentno"]+"</br>";
                bodyDesign+="<span><b>Vendor Name :</b> "+this.personInfo.personName+"</span></br>";
                bodyDesign+="<span><b>Deductee Type :</b> "+this.personInfo.deducteetypename+"</span></br>";
                bodyDesign+="<span><b>Residential Status :</b> "+(this.personInfo.residentialstatus == 0 ? " Resident" : 
                        (this.personInfo.residentialstatus == 1 ? "Non-Resident": "Not Available")) +"</span></br>";
                bodyDesign+="<b>Total Amount :</b> "+this.record.data["enteramount"]+"</br>";
                break;
                
        }
        if (this.callFrom == "expense" || this.callFrom == "invoice" || this.callFrom == "purchasereturn") { // only for call from expence invoice
                bodyDesign+="<span><b>Vendor Name :</b> "+this.personInfo.name+"</span></br>";
                bodyDesign+="<span><b>Deductee Type :</b> "+this.personInfo.deducteetypename+"</span></br>";
                bodyDesign+="<span><b>Residential Status :</b> "+(this.personInfo.residentialstatus == 0 ? " Resident" : 
                        (this.personInfo.residentialstatus == 1 ? "Non-Resident": "Not Available")) +"</span></br>";
                if(this.callFrom != "invoice" && this.callFrom != "purchasereturn"){
                    bodyDesign+="<span><b>Account :</b> "+this.selectedAccountName+"</span></br>";
                }
        }
        this.add({
            region: 'north',
            height: 125,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml('TDS Payment',bodyDesign, "../../images/accounting_image/price-list.gif", true)
        },  this.southPanel=new Wtf.Panel({
            border: false,
            region: 'south',
            id: 'southpan'+this.id,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls:'bckgroundcolor',
            layout: 'fit',
            height: 320,
            items:[this.grid]            
        }))
    },
    createDisplayGrid:function(){
        this.accRec = Wtf.data.Record.create([
        {
            name:'accountname',
            mapping:'accname'
        },

        {
            name:'accountid',
            mapping:'accid'
        },

        {
            name:'productaccountid', 
            mapping:'accid'
        },

        {
            name:'acccode'
        },

        {
            name:'groupname'
        },

        {
            name:'natureOfPayment'
        }
        //            {name:'level',type:'int'}
        ]);
        this.accountStore = new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
            //            url: Wtf.req.account+'CompanyManager.jsp',
            baseParams:{
                mode:2,
                ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });
        
        this.cmbAccount=new Wtf.form.ExtFnComboBox({
            hiddenName:'accountid',
            //id : 'cmbAccount',
            store:this.accountStore,
            minChars:1,
            valueField:'accountid',
            displayField:'accountname',
            forceSelection:true,
            hirarchical:true,
            //                    addNewFn:this.openCOAWindow.createDelegate(this),
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['acccode','groupname']:['groupname'],
            mode: 'local',
//            typeAheadDelay:30000,
            extraComparisionField:'acccode',// type ahead search on acccode as well.
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400
        });
        this.accountStore2 = new Wtf.data.Store({
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.accRec)
        });;
        this.cmbAccount2 = this.cmbAccount.cloneConfig({
            hiddenName:'tdsaccountid',
//            disabled: this.isEdit,
                    //id : 'cmbAccount2',
            store:this.accountStore2
        });
       this.accountStore.load();
       this.accountStore2.on('load',function(store, rec){
            this.grid.getView().refresh();
        },this);
       this.accountStore2.load();
        var natureofPaymentRec=new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'name'
        },
        
        {
            name: 'accid'
        },

        {
            name: 'salespersoncode'
        },
        ]);
        this.natureofPaymentStore=new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },natureofPaymentRec),
            url : "ACCMaster/getMasterItems.do",
            baseParams:{
                groupid:33,
                mode:112,
                moduleIds:""
            }
        });
        
        this.cmbNatureofPayment=new Wtf.form.ExtFnComboBox({
            hiddenName:'natureofpayment',
            store:this.natureofPaymentStore,
            AddQtipOnExtraFields: true,//To add QTip on extra fields in WtfExtComboBox.js.
            minChars:1,
            valueField:'id',
            displayField:'name',
            forceSelection:true,
            hirarchical:true,
            extraFields:['salespersoncode'],
            mode: 'local',
            typeAheadDelay:30000,
            extraComparisionField:'name',// type ahead search on acccode as well.
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400
        });
        this.natureofPaymentStore.load();
//        this.natureofPaymentStore.on('load', function () { // set value on load NOP
//        }, this);
//        
        this.taxincludeStore = new Wtf.data.SimpleStore({
            fields: [{
                name: "id"
            }, {
                name: "name"
            }],
            data: [[true, "YES"], [false, "NO"]]
        });
        this.cmbTaxinclude = new Wtf.form.ComboBox({
            hiddenName: 'includetax',
            store: this.taxincludeStore,
            valueField: 'id',
            displayField: 'name',
            mode: 'local',
            triggerAction: 'all',
            //value:"NO",
            forceSelection: true
        });
        this.sm = new Wtf.grid.RowNumberer();
        this.cm= new Wtf.grid.ColumnModel([this.sm,{
            header:WtfGlobal.getLocaleText("acc.invoiceList.expand.pName"), // "Product Name" 
            width:100,
            dataIndex:'productname',
            hidden:(this.record.data["type"]!= 2 || this.record.data["invType"] =="0" || this.callFrom == "expense" || this.callFrom == "invoice" || this.callFrom == "purchasereturn")
        },{
            header:"Amount" , // "Nature Of Payment"
            width:100,
            dataIndex:'amount',
            align:'right',
            hidden:this.record.data["type"]!=2,
            renderer :WtfGlobal.currencyRendererSymbol
        },{
            header:"Amount Due" , // "Nature Of Payment"
            width:100,
            dataIndex:'amountdue',
            align:'right',
            hidden:(this.record.data["type"]!=2 || this.callFrom== "expense" || this.callFrom == "invoice" || this.callFrom == "purchasereturn"),
            editor: this.amountdue = new Wtf.form.NumberField({
                name: 'amountdue',
                emptyText:WtfGlobal.getLocaleText("acc.mp.selectDocumentNumber")
            }),
            renderer :WtfGlobal.currencyRendererSymbol
        },{
            header:WtfGlobal.getLocaleText("acc.TDSPaymentWindow.DeducteeType") , // "Deductee Type"
            width:100,
            dataIndex:'deducteetypename'
        },{
            header: (this.callFrom == "expense" || this.callFrom == "invoice" || this.callFrom == "purchasereturn")?"Line Amount":WtfGlobal.getLocaleText("acc.TDSPaymentWindow.AmountPaidorCredited"), // "Amount Paid/Credited"
            width:100,
            dataIndex:'enteramount',
            align:'right',
            editable:false,
            editor: this.amount = new Wtf.form.NumberField({
                name: 'enteramount',
                readOnly: true,//this.isEdit,
                emptyText:WtfGlobal.getLocaleText("acc.mp.selectDocumentNumber")
            }),
            renderer :function(v){
                /*
                 * If  in User Administration > Assign Permission > Display Unit Price & Amount in Purchase Document
                 * If it uncheck we will hide amount and show '*****',  
                 */
                if (!Wtf.dispalyUnitPriceAmountInPurchase) {
                    return Wtf.UpriceAndAmountDisplayValue;
                }else{
                    return WtfGlobal.currencyRendererSymbol(v);
                    
                }   
            }
        }, {
            header:"TDS Assessable Amount ", 
            width: 100,
            align:'right',
            dataIndex: 'tdsAssessableAmount',
            hidden : !(this.record.data["type"]==1 || this.callFrom == "expense" || this.callFrom == "invoice" || this.callFrom == "purchasereturn"),
            editor: this.amount = new Wtf.form.NumberField({
                name: 'tdsAssessableAmount',
                readOnly: false, //this.isEdit,
                allowBlank:false,
                allowNegative: false
//                emptyText: WtfGlobal.getLocaleText("acc.mp.selectDocumentNumber")
            }),
            renderer :function(v){
                /*
                 * If  in User Administration > Assign Permission > Display Unit Price & Amount in Purchase Document
                 * If it uncheck we will hide amount and show '*****',  
                 */
                if (!Wtf.dispalyUnitPriceAmountInPurchase) {
                    return Wtf.UpriceAndAmountDisplayValue;
                }else{
                    return WtfGlobal.currencyRendererSymbol(v);
                    
                }   
            }
        },
        {
            header:"Nature Of Payment" , // "Nature Of Payment"
            width:100,
            dataIndex:'natureofpayment',
            editable:(this.callFrom == "purchasereturn"||!Wtf.isEmpty(this.advancePaymentNop)) ? false : true,
            renderer:Wtf.comboBoxRenderer(this.cmbNatureofPayment),//,
            editor:this.cmbNatureofPayment
//            read:false
                
        },                    
        {
            header:"TDS Rate(%)" , // "Nature Of Payment"
            width:100,
            dataIndex:'tdspercentage',
            align:'right',
            renderer:function (val){
                if(Wtf.isEmpty(val)){
                  return "";
                }else{
                    return val
                }
            }
        },        
        {
            header:WtfGlobal.getLocaleText("acc.TDSPaymentWindow.TDSAmount"), 
            width:100,
            dataIndex:'tdsamount',
            align:'right',
            editor: this.tdsamount = new Wtf.form.NumberField({
                name: 'tdsamount',
                readOnly: true,//this.isEdit,
                emptyText:WtfGlobal.getLocaleText("acc.mp.selectDocumentNumber")
            }),
            editable : false,
            renderer :function(v){
                /*
                 * If  in User Administration > Assign Permission > Display Unit Price & Amount in Purchase Document
                 * If it uncheck we will hide amount and show '*****',  
                 */
                if (!Wtf.dispalyUnitPriceAmountInPurchase) {
                    return Wtf.UpriceAndAmountDisplayValue;
                }else{
                    return WtfGlobal.currencyRendererSymbol(v);
                    
                }   
            }
        },
        {
            header:"TDS Payable Account", // "Account Name"
            width:100,
            editable:false,
            dataIndex:'tdsaccountid',
            renderer:Wtf.comboBoxRenderer(this.cmbAccount2),
            editor:this.cmbAccount2
        }   
        ]);
       
       
        this.Rec = new Wtf.data.Record.create([
        
        {
            name: 'productid'
        },
        {
            name: 'productname'
        },
        {
            name: 'accountid'
        },
        {
            name:'natureofpayment'
        },
        {
            name:'amount' 
        },
        {
            name: 'enteramount' 
        },
        {
            name: 'amountdue' 
        },
        {
            name:'ruleid'  
        },
        {
            name:'rowTaxAmount'
        },
        {
            name:'includetax'
        },
        {
            name: 'tdspercentage' 
        },
        {
            name: 'tdsamount' 
        },
        {
            name:'rowid'
        },
        {
            name:'deducteetypename'
        },
        {
            name:'tdsaccountid'
        },
        {
            name:'tdsAssessableAmount'
        },
        {
            name:'advancePaymentDetails',type:'string'
        }]);
        
        
        var baseparam = {}
        if (this.callFrom == "expense" || this.callFrom == "invoice" || this.callFrom == "purchasereturn") {
            var additionalAmount=this.calculateAdditionalAmount(this.personInfo.natureOfPayment);
            baseparam = {
                bills: this.record.data["rowid"],
                amount: this.callFrom == "invoice" || this.callFrom == "purchasereturn"?this.record.data["amount"]:this.record.data["calamount"],
                tdsAssessableAmount: this.callFrom == "invoice" || this.callFrom == "purchasereturn"?this.record.data["amount"]:this.record.data["calamount"],
                rowdetailid: this.record.data["rowdetailid"],
                documenttype: "",
                natureofPayment: this.advancePaymentNop,
                deducteetype: this.personInfo.deducteetype,
                residentialstatus: this.personInfo.residentialstatus,
                vendorid: this.personInfo.accid,
                isFixedAsset: false,
                isLeaseFixedAsset: false,
                isexpenseinv: true,
                istdsapplicable: true,
                mode: "14",
                creationDate: this.personInfo.upperLimitDate,
                isIngoreExemptLimit:!(this.personInfo.considerExemptLimit=='true'),
                billdate : !Wtf.isEmpty(this.parentObj.billDate)?this.parentObj.billDate.format('Y-m-d'):(new Date()).format('Y-m-d'), // to check exempt Limit
                financialStartDate :WtfGlobal.getDates(true).format('Y-m-d'),
                financialEndDate:WtfGlobal.getDates(false).format('Y-m-d'),
                additionalAmount:additionalAmount
            }
        } else {
            baseparam = {
                bills: this.record.data["rowid"],
                amount: this.record.data["enteramount"],
                tdsAssessableAmount: this.record.data["enteramount"],
                rowdetailid: this.record.data["rowdetailid"],
                documenttype: this.record.data["type"],
                deducteetype: this.personInfo.deducteetype,
                residentialstatus: this.personInfo.residentialstatus,
                vendorid: this.personInfo.vendorId,
                isFixedAsset: false,
                isLeaseFixedAsset: false,
                isexpenseinv: true,
                istdsapplicable: true,
                mode: "14",
                creationDate: this.personInfo.upperLimitDate,
                isIngoreExemptLimit:true,
                financialStartDate :WtfGlobal.getDates(true).format('Y-m-d'),
                billdate : !Wtf.isEmpty(this.parentObj.creationDate)?this.parentObj.creationDate.getValue().format('Y-m-d'):(new Date()).format('Y-m-d'), // to check exempt Limit
                financialEndDate:WtfGlobal.getDates(false).format('Y-m-d'),
                additionalAmount:0.0
            }
        }

        this.url = "ACCVendorPaymentNew/getTDSDetailsAtPayment.do";
        this.store = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty: 'totalCount'
            }, this.Rec),
            url: this.url,
            baseParams: baseparam
        });

        this.store.on('beforeload', function() {
            if (this.loadingMask == undefined) {
                this.loadingMask = new Wtf.LoadMask(document.body, {
                    msg: WtfGlobal.getLocaleText("acc.msgbox.50")
                });
            }
            this.loadingMask.show();
        }, this);

        this.store.on("loadexception", function(arg1,arg2,resp,arg3) {
                this.loadingMask.hide();
            if(!Wtf.isEmpty(resp.responseText)){
                var response = eval('('+resp.responseText+')');
//                this.close();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),response.data.msg],2);
            }else{
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.error"),WtfGlobal.getLocaleText("acc.mp.unableToLoadData")],1);
                }
            }, this);

            // To handle Edit case of TDS Window.
            this.store.load();
            
        this.store.on("load", function() {
            if(this.appliedTDS != undefined && this.appliedTDS != ''){
                    var appliedTDSJson = {
                    data:eval(this.appliedTDS)
                    };
                    this.store.loadData(appliedTDSJson);
                }
                this.loadingMask.hide();
                this.grid.getView().refresh();
            }, this);

        this.grid = new Wtf.grid.EditorGridPanel({
            clicksToEdit:1,
            store: this.store,
            height:420,
            width:400,
            scope:this,
            cm: this.cm,
            readOnly : this.readOnly,
            border : false,
            loadMask : true,
            viewConfig: {
                forceFit:true,
                emptyText:WtfGlobal.getLocaleText("acc.common.norec")
            }
        });
        
        this.grid.on('validateedit', this.handleValidateEdit, this);
        this.grid.on('beforeedit', this.beforeGridCellEdit, this); // add Before Edit Event 
        this.grid.on('render', function(){
            /**
             *  Refresh grid ON grid render event
             */
            this.grid.getView().refresh(); 
        }, this); 
      
    },
    /**
     * Handle Grid cell Event for readOnly Property Value true
     * If read Only true then don't edit grid cell. Used for TDSPayment View Case
     * @param {Grid Cell Object} e
     * @returns {if readonly true return false and dont allow to edit Grid Cell}
     */
    beforeGridCellEdit: function (e) {
        if (this.readOnly) {
            e.cancel = true;
            return;
        }
    },
    submitSelectedRecords : function(){  
        if(this.checkValidGrid()){
            this.close(); 
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),"Please enter required details to proceed."],2);
        }
    },
    checkValidGrid : function(){
        var jsonArray = this.getSelectedRecords();
        var jsonArrayObj = eval(jsonArray);
        for(var i=0;i<jsonArrayObj.length;i++){
            if( Wtf.isEmpty(jsonArrayObj[i].tdsaccountid)){//Wtf.isEmpty(jsonArrayObj[i].accountid) ||
                return false;
            }
        }
        return true;
    },
    getSelectedRecords: function() {
        var arr = [];
        var selectionArray=this.grid.getStore();
        for(var i=0;i<selectionArray.getCount();i++){
                arr.push(this.store.indexOf(selectionArray.getAt(i)));
        }
        var jarray = WtfGlobal.getJSONArrayWithoutEncoding(this.grid, true,arr);   
        var jsonArrayObj = eval(jarray);
        for(var i=0;i<jsonArrayObj.length;i++){
            var noprec="";
            this.natureofPaymentStore.clearFilter();
            var nopindex=this.natureofPaymentStore.find("id",jsonArrayObj[i].natureofpayment);
            if(nopindex!=-1){
                noprec=this.natureofPaymentStore.getAt(nopindex);
            }
            if(!Wtf.isEmpty(noprec)){
                jsonArrayObj[i]['natureofpaymentName'] = noprec.data.name;
            }
        }
        jarray = JSON.stringify(jsonArrayObj);
        return jarray;
    },    
    setRecordForEditCase: function() {
        if(this.appliedGst){
            var index = WtfGlobal.searchRecordIndex(this.store,this.appliedGst, "taxid");
            this.grid.getSelectionModel().selectRow(index);
        }
    } ,
    handleValidateEdit : function (e) {
        /** Handle Validate Grid Edit event
         *  If read Only true then dont edit grid cell. Used for TDSPayment View Case
         */
        if(this.readOnly){
                e.cancel=true;
                return;
        }
        var field = e.field;
        var record = e.record;
        var cellvalue = e.value;
        if(field=="natureofpayment"){
            if(record.get("enteramount")!=undefined && record.get("enteramount")!=""){
                var natureOfPayment = cellvalue;
                if(natureOfPayment != ""){
//                    record.set("natureofpayment", natureOfPayment);
                
                if(this.record.data["type"]==1 || this.record.data["type"]==3 || this.record.data["type"]==4){
                    record.set("rowTaxAmount", 0);
                        record.set("includetax", false);
                    }
                    if(!Wtf.isEmpty(this.personInfo.deducteetype)){
                        if(!Wtf.isEmpty(this.personInfo.residentialstatus)){
                            var result = this.setTDSrateAndAmount(record, natureOfPayment);
                            if(!result){
                                return false;
                            }
                        }else{
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.mp.Residentialstatusisnotset")],2);
                            return false;
                        }   
                    }else{
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.mp.Deducteetypeisnotset")],2);
                        return false;
                    }
                
                } else {
                    var nopdetails="Not Available"
                    this.natureofPaymentStore.clearFilter();
                    var nopstoreat=this.natureofPaymentStore.find("id",natureOfPayment);
                    if(nopstoreat!=-1){
                        nopdetails=this.natureofPaymentStore.getAt(nopstoreat);
                    }
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.warning"),"TDS Rate is not available for below data combination.<br><br>"+
                            "<table border='0'  style='width: 600px;'>"+
                            "<tr>"+
                            "<td><b>Deductee Type : </b>"+(!Wtf.isEmpty(this.personInfo.deducteetypename)?this.personInfo.deducteetypename:"Not Available")+"</td>"+
                            "<td><b>Residential Status : </b>"+(!Wtf.isEmpty(this.personInfo.residentialstatus)?(this.personInfo.residentialstatus==1?"Non-Resident":"Resident"):"Not Available")+"</td>"+
                            "</tr>"+
                            "<tr>"+
                            "<td><b>Nature of Payment : </b>"+(!Wtf.isEmpty(nopdetails)&&!Wtf.isEmpty(nopdetails.data)?nopdetails.data.salespersoncode:"<u>Not Available</u>")+"</td>"+
                            "</tr>"+
                            "</table>");
                    return false;
                }
            }else{
               WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"Please enter Assessable amount first"],2); 
                    record.set("rowTaxAmount", 0);
                    record.set("includetax", "");
                    record.set("accountid", "");
                    record.set("natureofpayment", "");
                    record.set("tdspercentage", "");
                    record.set("tdsamount", "");
                     return false;
            }
        }
        if(field=="enteramount"){
            record.set("rowTaxAmount", 0);
            record.set("includetax", "");
            record.set("accountid", "");
            record.set("natureofpayment", "");
            record.set("tdspercentage", "");
            record.set("tdsamount", "");
        }
        if(field=="tdsAssessableAmount"){// on edit assessable value change TDS amount
            if(record.get("enteramount") < cellvalue){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"TDS Assessable Amount should be less than or equal to Line Amount."],2);
                return false;
            }
            if ((this.callFrom == 'expense' || this.callFrom == "invoice")) { // Exempt Limit not considerd for Advance payment.
                var natureOfPayment = record.get("natureofpayment");
                record.set("tdsAssessableAmount", cellvalue);
                var result = this.setTDSrateAndAmount(record, natureOfPayment);
                if (!result) {
                    return false;
                }
            }else{
                var tdsamount = this.calculateTDSAmount(cellvalue, record.get('tdspercentage'));
                record.set("tdsamount", tdsamount);
            }
        }
    },
    
    setTDSrateAndAmount : function(record, natureOfPayment){
            var nopdetails="Not Available"
            this.natureofPaymentStore.clearFilter();
            var nopstoreat=this.natureofPaymentStore.find("id",natureOfPayment);
            if(nopstoreat!=-1){
               nopdetails=this.natureofPaymentStore.getAt(nopstoreat);
            }
            var tdsPayableAccountId = "";
            if(!Wtf.isEmpty(nopdetails) && !Wtf.isEmpty(nopdetails.data.accid)){
                tdsPayableAccountId = nopdetails.data.accid;
            }
            var billdate = (new Date()).format('Y-m-d');
            if(this.callFrom=='expense' || this.callFrom == "invoice" || this.callFrom == "purchasereturn"){
                billdate = !Wtf.isEmpty(this.parentObj.billDate)?this.parentObj.billDate.format('Y-m-d'):(new Date()).format('Y-m-d');
            }else{
               billdate=!Wtf.isEmpty(this.parentObj.creationDate)?this.parentObj.creationDate.getValue().format('Y-m-d'):(new Date()).format('Y-m-d');
            }
            var additionalAmount=0.0;
            if(this.callFrom == "expense" || this.callFrom == "invoice" || this.callFrom == "purchasereturn"){
                additionalAmount=this.calculateAdditionalAmount(natureOfPayment);        
            }
            Wtf.Ajax.requestEx({            // request to fetch tdsrate and amount as per nature of payment and other attributes
                url: "ACCVendorPaymentCMN/getTDSCalculationDetails.do",
                params: {
                    bills:(this.callFrom == 'expense' || this.callFrom == "invoice") ? this.record.data["rowid"] : "",
                    natureofPayment: natureOfPayment,
                    deducteetype: this.personInfo.deducteetype,
                    residentialstatus: this.personInfo.residentialstatus,
                    amount: record.get("tdsAssessableAmount"),
                    vendorID: (this.callFrom=='expense' || this.callFrom == "invoice" || this.callFrom == "purchasereturn")?this.personInfo.accid:this.personInfo.vendorId,
                    tdsPayableAccount : tdsPayableAccountId,
                    isTDSApplicable:(!Wtf.isEmpty(this.parentObj.parentObj) && !Wtf.isEmpty(this.parentObj.parentObj.record)) ? this.parentObj.parentObj.record.data.isTDSApplicable:"",
                    isIngoreExemptLimit:(this.callFrom=='expense' || this.callFrom == "invoice")?!(this.personInfo.considerExemptLimit=='true'):true,
                    billdate : billdate,
                    financialStartDate :WtfGlobal.getDates(true).format('Y-m-d'),
                    financialEndDate:WtfGlobal.getDates(false).format('Y-m-d'),
                    additionalAmount:additionalAmount
                }
            },this,function(resp){
                if(resp!=""){
                    if(resp.success){
                            record.set("tdspercentage", resp.tdsrate);
                            record.set("tdsamount", resp.tdsamount);
                            record.set("ruleid", resp.ruleid);
                            record.set("natureofpayment", natureOfPayment);
                            record.set("tdsaccountid", tdsPayableAccountId);
                        return true;
                    }else{
//                      WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),"Sorry, TDS rate not available for selected account"],2);
                        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.warning"),"TDS Rate is not available for below data combination.<br><br>"+
                            "<table border='0'  style='width: 600px;'>"+
                            "<tr>"+
                            "<td><b>Deductee Type : </b>"+(!Wtf.isEmpty(this.personInfo.deducteetypename)?this.personInfo.deducteetypename:"Not Available")+"</td>"+
                            "<td><b>Residential Status : </b>"+(!Wtf.isEmpty(this.personInfo.residentialstatus)?(this.personInfo.residentialstatus==1?"Non-Resident":"Resident"):"Not Available")+"</td>"+
                            "</tr>"+
                            "<tr>"+
                            "<td><b>Nature of Payment : </b>"+(!Wtf.isEmpty(nopdetails)&&!Wtf.isEmpty(nopdetails.data)?nopdetails.data.salespersoncode:"<u>Not Available</u>")+"</td>"+
                            "</tr>"+
                            "</table>");
                            return false;
                    }
                }
            }, function(resp){
                });
        },
    calculateTDSAmount:function(amount,rate){
        return Math.ceil((amount*rate)/100);
    },
    calculateAdditionalAmount:function(natureOfPayment){
        var additionalAmount=0.0;
        var gridStore = this.parentObj.parentObj.Grid.getStore();
        for(var i=0;i<gridStore.getCount();i++){
            var rec = gridStore.getAt(i);
            if(!Wtf.isEmpty(rec.data.appliedTDS)){
                var appliedTDS= eval(rec.data.appliedTDS);
                if(natureOfPayment==appliedTDS[0].natureofpayment && this.record.id!=rec.id){
                    additionalAmount+=parseFloat(appliedTDS[0].tdsAssessableAmount);
                }
            }
        }
        return additionalAmount;
    }
   
});  


