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

function Showproductdetails(productid,productname,so){
       callViewProductDetails(productid,'View Product',so,productname);
}

Wtf.account.PurchaseRequisitionPanel=function(config){
	this.quotation = config.quotation;
    this.DefaultVendor = config.DefaultVendor;
	this.id=config.id;
	this.titlel = config.title!=undefined?config.title:"null";
    this.currid = config.isRFQ ? config.currencyid : "";
    this.dataLoaded=false;
    this.isExpenseInv=false;
    this.isEdit=config.isEdit;
    this.isFromWO=config.isFromWO;
    this.ifFromShortfall=config.ifFromShortfall;
    this.isDraft = false;
    this.isRequisition = config.isRequisition ? config.isRequisition : false;
    this.isRFQ = config.isRFQ ? config.isRFQ : false;
    this.label=config.label;
    this.copyInv=config.copyInv;
    this.viewGoodReceipt= config.viewGoodReceipt;
    this.readOnly=config.readOnly;
    this.billid=null;
    this.custChange=false;
    this.record=config.record;
    this.pendingapproval = config.pendingapproval!=undefined ? config.pendingapproval : false;
    this.isPOfromSO=config.isPOfromSO;
    this.datechange=0;
    this.oldval="";this.val="";this.pronamearr=[];
    this.changeGridDetails=true;
    this.appendID = true;
    this.mailFlag=false;
    var help=getHelpButton(this,config.heplmodeid);
    this.custUPermType=config.isCustomer?Wtf.UPerm.customer:Wtf.UPerm.vendor;
    this.custPermType=config.isCustomer?Wtf.Perm.customer:Wtf.Perm.vendor;
    this.soUPermType=(config.isCustomer?Wtf.UPerm.invoice:Wtf.UPerm.vendorinvoice);
    this.soPermType=(config.isCustomer?Wtf.Perm.invoice.createso:Wtf.Perm.vendorinvoice.createpo);
    this.productOptimizedFlag=Wtf.account.companyAccountPref.productOptimizedFlag;
    var isbchlFields1=(!config.isCustomer && config.isOrder);
    this.modeName = config.modeName;
    this.isLinkedTransaction = (config.isLinkedTransaction == null || config.isLinkedTransaction == undefined)? false : config.isLinkedTransaction;
    this.heplmodeid=config.heplmodeid;
    this.PR_IDS=config.PR_IDS;
    this.linkedDocumentId="";
    this.originallyLinkedDocuments = '';    
    /* SDP-13487
     * To identify, from where the call has been received to Business Logic Function when user open the draft in edit mode and then save it again. this.isDraft will be false but this.isFromDraftReport will be true when user save the draft as an transaction.
     */
    this.isDraft = false;
    this.isSaveDraftRecord = (this.record!=null && this.record.data!=undefined) ? this.record.data.isDraft : false;
    this.isAutoSeqForEmptyDraft = false;    //SDP-13927 : To identify Old Record No. and Auto Generated No while saving Draft Record in Edit Mode.
    this.isSequenceFormatChangedInEdit = false; //SDP-13923 : This flag has been used to identify whether user has changed Sequence Format in Edit case (Only for Draft Type of record)
    
     var buttonArray = new Array();
     this.saveBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
        tooltip: WtfGlobal.getLocaleText("acc.rem.175"),
        id: "submit" + config.heplmodeid, // + this.id,
        hidden: this.viewGoodReceipt,
        scope: this,
        handler:function() {
            this.isDraft=false;
            this.isSaveDraftRecord = (this.record!=null && this.record.data!=undefined) ? this.record.data.isDraft : false;  //SDP-13487 - When user save the draft as a Transaction then to identify this call is for transaction, we have used this flag. At this time, this.isDraft will be false.
           if(this.isLinkedTransaction){
                this.update();
            }else{
                this.save();
            }
        },
        iconCls: 'pwnd save'
    });
    this.savencreateBttn=new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.saveasdraft"), //'Save as Draft',
        tooltip: WtfGlobal.getLocaleText("acc.rem.222"),
        id: "save" + config.heplmodeid + this.id,
        hidden : this.isRFQ || this.viewGoodReceipt,
        scope: this,
        disabled:(this.isEdit && this.record!=null && this.record.data.isDraft!=undefined && !this.record.data.isDraft),    //ERP-38582 : REOPEN-1
        handler: function() {
            this.isDraft=true;
            this.isSaveDraftRecord = false;  //SDP-13487 - When user save the draft as a Transaction then to identify this call is for transaction, we have used this flag. At this time, this.isDraft will be false.
            this.save();
        },
        iconCls: 'pwnd save'
    });
    buttonArray.push(this.saveBttn,this.savencreateBttn);
    if (isbchlFields1 && this.isEdit) {

        buttonArray.push(this.exportButton = new Wtf.exportButton({
            obj: config.POthisObj,
//            id: "exportReports" + config.helpmodeid + config.id,
            text: WtfGlobal.getLocaleText("acc.common.export"),
            tooltip: WtfGlobal.getLocaleText("acc.common.exportTT"), //'Export report details',
            disabled: false,
            hidden:(this.readOnly || this.isRFQ),   //ERP-13691
            menuItem: {csv: false, pdf: false, rowPdf: (config.isSalesCommissionStmt) ? false : true, rowPdfTitle: WtfGlobal.getLocaleText("acc.rem.39")},
            get: config.POnewtranType
        }));

    }
   if(!this.readOnly){
        buttonArray.push('->');
        buttonArray.push(help);
   }
    Wtf.apply(this, config);
    Wtf.apply(this, {
        bbar: buttonArray
    });

    Wtf.apply(this, config);
    Wtf.apply(this, {
        bbar:buttonArray
      });
    Wtf.account.PurchaseRequisitionPanel.superclass.constructor.call(this,config);
    this.addEvents({
        'update':true
    });
    /**
    * Product Grid not showing properly after Expanding/Collapsing Navigation Panel.
    */
    this.on('resize', function (panel) {
        panel.doLayout();
        if (panel.Grid) {
            panel.Grid.doLayout();
            panel.Grid.getView().refresh();
        }
    }, this);
}

Wtf.extend(Wtf.account.PurchaseRequisitionPanel,Wtf.account.ClosablePanel,{
    autoScroll: true,// layout:'border',//Bug Fixed: 14871[SK]
    bodyStyle: {background:"#DFE8F6 none repeat scroll 0 0"},
    border:'false',
    externalcurrencyrate:0,
    isCurrencyLoad:false,
    currencyid:null,
    custdatechange:false,
    closable : true,
    cash:false,
    layout : 'border',
    isCustomer:false,
    cls : 'southcollapse',
    isCustBill:false,
    isOrder:false,
    fromOrder:false,
    loadRecord:function(){
        if(this.record!=null&&!this.dataLoaded){
            var data=this.record.data;
            this.NorthForm.getForm().loadRecord(this.record);
            if ((!this.copyInv && !this.isPOfromSO && this.isRequisition) || this.viewGoodReceipt || (this.isRFQ && this.isEdit)) {
                if(data.isDraft){
                    if(data.billno!=""){
                        this.Number.setValue(data.billno);  //SDP-13487 : Do not set empty entry no. in edit case of of Draft                                         
                    }
                } else {
                    this.Number.setValue(data.billno);
                }
            }
//            if(this.isRFQ && this.isEdit){
//                this.Number.disable();
//                this.sequenceFormatCombobox.disable();
//            }
            if(this.isPOfromSO){
                this.fromPO.setValue(true);
                this.POStore.proxy.conn.url = "ACCSalesOrderCMN/getSalesOrders.do";
                this.POStore.on("load", function(){
                    if(this.isPOfromSO){
                        this.PO.disable();
                        this.fromPO.disable();
                        this.setTransactionNumber();
                        this.PO.setValue(data.billid);
                    }
                    this.isPOfromSO = false;
                }, this);
                this.POStore.load();                        
                
            }
           if(this.isRFQ){
                var taxcol=this.ProductGrid.getColumnModel().findColumnIndex("prtaxid");
                var taxamt=this.ProductGrid.getColumnModel().findColumnIndex("taxamount");
                this.Grid.getColumnModel().setHidden( taxcol,true) ;
                this.Grid.getColumnModel().setHidden( taxamt,true) ;
                
            }
            if(data && data.PR_IDS){
                this.PR_IDS = data.PR_IDS;
            }
            this.template.setValue(data.templateid);
                this.Currency.setValue(data.currencyid);
            var store=(this.isCustomer?Wtf.customerAccStore:Wtf.vendorAccStore)
            var index=store.findBy( function(rec){
                var parentname=rec.data['accid'];
                if(parentname==data.personid)
                    return true;
                 else
                    return false;
            })
            if(index>=0)
            store.load();
            this.Name.setValue(data.personid?data.personid:"");
            this.Memo.setValue(data.memo);
            this.billTo.setValue(data.billto);
            this.DueDate.setValue(data.duedate);
            this.billDate.setValue(data.date);
            this.perDiscount.setValue(data.ispercentdiscount);
            this.Discount.setValue(data.discountval);
            this.isTaxable.setValue(data.taxincluded);
            this.PORefNo.setValue(data.porefno);    
            if(this.isRFQ){
                this.VendorEmail.setValue(data.othervendoremails);
            }
            if(data.taxid == ""){
            	this.isTaxable.setValue(false);
                this.Tax.setValue("");
                this.Tax.disable();
            }else{
            	this.Tax.setValue(data.taxid);
            	this.isTaxable.setValue(true);
            }
            this.CostCenter.setValue(data.costcenterid);
            this.dataLoaded=true;
            if(this.copyInv||this.isPOfromSO){
//            	this.billDate.setValue(Wtf.serverDate);
                this.billDate.setValue(new Date());
            	this.updateDueDate();
            }
            if(this.isCustomer && this.record.data.partialinv){
                var id=this.Grid.getId();
                var rowindex=this.Grid.getColumnModel().findColumnIndex("partamount");
                this.Grid.getColumnModel().setHidden( rowindex,false) ;
            }
            /*
             * Commented below code because there is no need to show the tax fields in Purchase Requisition
             */
//            var gridID=this.Grid.getId();
//            var taxColumnIndex=this.Grid.getColumnModel().findColumnIndex("prtaxid");
//            var taxAmtColumnIndex=this.Grid.getColumnModel().findColumnIndex("taxamount");
//            if(this.record.data.includeprotax){
//                this.Grid.getColumnModel().setHidden( taxColumnIndex,false) ;
//                this.Grid.getColumnModel().setHidden( taxAmtColumnIndex,false) ;
//            }else{
//                this.Grid.getColumnModel().setHidden( taxColumnIndex,true) ;
//                this.Grid.getColumnModel().setHidden( taxAmtColumnIndex,true) ;
//            }
            if(!this.isRequisition) {
                this.loadTransStore();
                if(!(this.quotation || !this.isCustomer || this.isOrder)){
                    if(this.Name.getValue()!=""){
                      this.billingAddrsStore.load({params:{customerid:this.Name.getValue()}});
                      this.ShippingAddrsStore.load({params:{customerid:this.Name.getValue()}});         
                    }                
                }
            }
        }
        /*
         *Link case disable the following items 
         */
        if(this.isLinkedTransaction && (this.moduleid==Wtf.Acc_Purchase_Requisition_ModuleId || this.moduleid==Wtf.Acc_RFQ_ModuleId)){
            this.DueDate.disable();
            this.Currency.disable();
            this.Number.disable();
            this.billDate.disable();
            if (this.isRFQ) {
                this.Name.disable();
                this.VendorEmail.disable();
            }
        }
    },
    //ERP-9509 : Function not present while calling from INVOICEGRID.js. Returned Blank String. Can override the function in future if required
    getModuleId : function(){
        var moduleId = "";

        return moduleId;
    },
    afterRender:function(config){
        Wtf.account.PurchaseRequisitionPanel.superclass.afterRender.call(this, config);

        if(this.isRequisition && this.isEdit) {
//            this.task = {
//                run: this.setRecordValue.createDelegate(this,[false]),
//                interval: 3000 //1 second
//            }
//            this.taskmagr = new Wtf.util.TaskRunner();
//            this.taskmagr.start(this.task);
//            this.taskmagr = new Wtf.TaskMgr.start({
//                run: this.setRecordValue,
//                interval: 3000
//            });
        //          setTimeout(setRecordValue(), 3000);
        //            this.termds.loadData(response.termdata);
        //            this.currencyStore.loadData(response.currencydata);
        //            if(!(this.isCustBill||this.isExpenseInv)){
        //                this.Grid.priceStore.loadData(response.productdata);}


        }   
    },
    
    setRecordValue : function() {
        this.taskmagr.stop(this.task);
//         if(this.currencyStore.getCount()<=1){
//             callCurrencyExchangeWindow();
//            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.msg1")],2);
//        }
//        else{
//            this.isCurrencyLoad=true;
//            this.applyTemplate(this.currencyStore,0);
//        }
//        this.hideLoading();
        this.loadProductGridForRequisition();
        //            if(this.isEdit && !this.isOrder)
        //                this.loadEditableGrid();
        //            else if(this.isEdit && this.isOrder)
        //            	this.loadEditableGridisOrder();
        this.loadRecord();
    },
    
    onRender:function(config){
        var centerPanel = new Wtf.Panel({
                region : 'center',
                border : false,
                autoScroll : true
            });
        if(this.isCustomer||this.isCustBill||this.isOrder||this.isEdit||this.copyInv) {
            centerPanel.add(this.NorthForm,this.deleteSelectedPanel,this.Grid,this.southPanel);
        } else {
            centerPanel.add(this.NorthForm,this.GridPanel,this.southPanel);
        }
        
        this.add(centerPanel);
//        this.add({
//                border: true,
//                id: 'south' + this.id,
//                region: 'south',
//                //split: true,
//                hidden : this.isCustBill,
//                layout: 'fit',
//                height:130 ,
//                plugins : new Wtf.ux.collapsedPanelTitlePlugin(),
//                collapsibletitle : WtfGlobal.getLocaleText("acc.common.recentrec") + " " +this.businessPerson + " for the Product",
//                title : WtfGlobal.getLocaleText("acc.common.recentrec") + " " +this.businessPerson + " for the Product",
//                collapsible: true,
//                collapsed: true,
//                items : [
//                    this.lastTransPanel
//                ]
//            });
        Wtf.account.PurchaseRequisitionPanel.superclass.onRender.call(this, config);
        this.isClosable=false,
        this.initForClose();
        this.hideFormFields();
        
    },
    /*
  function to hide/show form fields
     */
    hideFormFields:function(){
    if(this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId){
        this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.purchaseRequisition); 
    } else if(this.moduleid == Wtf.Acc_RFQ_ModuleId) {
        this.hideTransactionFormFields(Wtf.account.HideFormFieldProperty.requestForQuotation);
    }
},
    
    hideTransactionFormFields:function(array){
        if(array){
            for(var i=0;i<array.length;i++){
                var fieldArray = array[i];
                if(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id)){
                    if(fieldArray.fieldId=="ShowOnlyOneTime" && ((this.isEdit !=undefined ?this.isEdit:false) || (this.copyInv !=undefined ?this.copyInv:false) || (this.isCopyFromTemplate !=undefined ?this.isCopyFromTemplate:false) || (this.isTemplate !=undefined ?this.isTemplate:false))){
                        continue;
                    }
                    if(fieldArray.isHidden){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hideLabel = fieldArray.isHidden;
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).hidden = fieldArray.isHidden;
                    }
                    /*
                     *'Email note' is set hidden if the Email field is set hidden from company preferences
                     **/
                    if(fieldArray.fieldId =="othervendoremails" && fieldArray.isHidden){
                       this.EmailMessage.hidden=true;                  
                    }
                    /*
                     *'Add Opening Balance Sign' will be shown only if opening balance 
                     *field is not set hidden from company preferences
                     **/
                    if(fieldArray.fieldId =="openbalance" && !fieldArray.isHidden){
                        this.openingBal.addNewFn=this.addOpeningBalance.createDelegate(this);
                    }
                    if(fieldArray.isReadOnly){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).disabled = fieldArray.isReadOnly;
                    }
                    if(fieldArray.isUserManadatoryField && Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel != undefined){
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).allowBlank = !fieldArray.isUserManadatoryField;
                        var fieldLabel="";
                        if(fieldArray.fieldLabelText!="" && fieldArray.fieldLabelText!=null && fieldArray.fieldLabelText!=undefined){
                            fieldLabel= fieldArray.fieldLabelText+" *";
                        }else{
                            fieldLabel=(Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel) + " *";
                        }
                        Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel = fieldLabel;
                    }else{
                        if( fieldArray.fieldLabelText!=null && fieldArray.fieldLabelText!=undefined && fieldArray.fieldLabelText!=""){
                            if(fieldArray.isManadatoryField && fieldArray.isFormField )
                                Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel=fieldArray.fieldLabelText +"*";
                            else
                                Wtf.getCmp(fieldArray.fieldId+this.heplmodeid+this.id).fieldLabel=fieldArray.fieldLabelText;
                        }
                  }
                }
            }
        }
    },
    
    initComponent:function(config){
        Wtf.account.PurchaseRequisitionPanel.superclass.initComponent.call(this,config);
        this.businessPerson=(this.isCustomer?'Customer':'Vendor');
        this.loadCurrFlag = true;
        if(!this.isCustBill){
            this.isCustBill = false;
        }
//        this.term=0;

        this.tplSummary=new Wtf.XTemplate(
            '<div class="currency-view">',
//            '<table width="100%">',
//            '<tpl if="'+(!this.isOrder || this.quotation)+'">',
//            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoice.subTotal")+' </b></td><td text-align=right>{subtotal}</td></tr>',
//            '<tr><td><b>- '+WtfGlobal.getLocaleText("acc.invoice.discount")+' </b></td><td align=right>{discount}</td></tr>',
//            '</table>',
//            '<hr class="templineview">',
//            '</tpl>',
//            '<table width="100%">',
//            '<tr><td><b>'+WtfGlobal.getLocaleText("acc.invoice.amt")+' </b></td><td align=right>{totalamount}</td></tr>',
//            '<tr><td><b>+ '+WtfGlobal.getLocaleText("acc.invoice.Tax")+': </b></td><td align=right>{tax}</td></tr>',
//            '</table>',
//            '<table width="100%">',
//            '</table>',
            '<hr class="templineview">',
            '<table width="100%">',
            '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.invoice.totalAmt")+' </b></td><td align=right>{aftertaxamt}</td></tr>',
            '</table>',
            '<table width="100%">',
            '<tr><td ><b>'+WtfGlobal.getLocaleText("acc.invoice.totalAmtInBase")+' </b></td><td align=right>{totalAmtInBase}</td></tr>',
            '</table>',
            '<hr class="templineview">',
            '<hr class="templineview">',
            '</div>'
        );
        
        /*To show and hide open PO and SO information depending on system preferences check*/
        if(Wtf.account.companyAccountPref.openPOandSO){
            this.productDetailsTplSummary=new Wtf.XTemplate(
                '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',            
                '<div><hr class="templineview"></div>',
                '<div>',
                '<table width="100%">'+
                '<tr>'+
                '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.field.ProductName")+'</b></td><td style="width:55%;"><span wtf:qtip="{productname}">'+Wtf.util.Format.ellipsis('{productname}',60)+'</span></td>'+                   
                '</tr>'+
                '<tr>'+
                '<td><b>'+WtfGlobal.getLocaleText("acc.field.InStock")+': </b></td><td style="width:10%;">{qty}</td>'+
                '</tr>'+
                '<tr>'+
                "<td><b>"+WtfGlobal.getLocaleText("acc.field.OpenPO")+": </b></td><td style='width:10%;'><a href='#' onclick='Showproductdetails(\"{productid}\",\"{productname}\",false)'>{poqty}</a></td>"+  
                '</tr>'+
                '<tr>'+
                "<td><b>"+WtfGlobal.getLocaleText("acc.field.OpenSO")+": </b></td><td style='width:30%;'><a href='#' onclick='Showproductdetails(\"{productid}\",\"{productname}\",true)'>{soqty}</a></td>"+         //provided link on wich we will get product quantity details
                '</tr>'+                       
                '<tr>' +
                "<td><b>"+WtfGlobal.getLocaleText("acc.field.BlockSO")+": </b></td><td style='width:10%;'><a href='#' onclick='callSalesByProductAgainstSalesOrder(true,\"{productid}\")'>{blockqty}</a></td>"+
                '</tr>' +
                '<tr>' +
                '<td><b>'+WtfGlobal.getLocaleText("acc.field.SalableStock")+': </b></td><td style="width:10%;">{salableStock}</td>'+
                '</tr>' +
                '<tr>' +
                '<td><b>'+WtfGlobal.getLocaleText("acc.field.ReserveStock")+': </b></td><td style="width:10%;">{reserveStock}</td>'+
                '</tr>' +
                '</table>'+
                '</div>',            
                '<div><hr class="templineview"></div>',                        
                '</div>'
            );
        }else{
                this.productDetailsTplSummary=new Wtf.XTemplate(
                '<div style="padding: 5px; border: 1px solid rgb(153, 187, 232);">',            
                '<div><hr class="templineview"></div>',
                '<div>',
                '<table width="100%">'+
                '<tr>'+
                '<td style="width:25%;"><b>'+WtfGlobal.getLocaleText("acc.field.ProductName")+'</b></td><td style="width:55%;"><span wtf:qtip="{productname}">'+Wtf.util.Format.ellipsis('{productname}',60)+'</span></td>'+                   
                '</tr>'+
                '<tr>'+
                '<td><b>'+WtfGlobal.getLocaleText("acc.field.InStock")+': </b></td><td style="width:10%;">{qty}</td>'+
                '</tr>'+
                '</table>'+
                '</div>',            
                '<div><hr class="templineview"></div>',                        
                '</div>'
            );
        } 
        
        this.productDetailsTpl=new Wtf.Panel({
            id:'productDetailsTpl'+this.id,
            border:false,
            baseCls:'tempbackgroundview',
            width:'95%',
            hidden:(this.isCustBill)?true:false,
            html:this.productDetailsTplSummary.apply({productname:"&nbsp;&nbsp;&nbsp;&nbsp;",productid:0,qty:0,soqty:0,poqty:0,blockqty:0,salableStock:0,reserveStock:0})
        });
        
        
        
        this.GridRec = Wtf.data.Record.create ([
            {name:'id'},
            {name:'number'}
        ]);

        this.termRec = new Wtf.data.Record.create([
            {name: 'termname'},
            {name: 'termdays'}
        ]);
        this.termds = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data"
            },this.termRec),
    //        url: Wtf.req.account + 'CompanyManager.jsp',
            url : "ACCTerm/getTerm.do",
            baseParams:{
                mode:91
            }
         });

         this.currencyRec = new Wtf.data.Record.create([
            {name: 'currencyid',mapping:'tocurrencyid'},
            {name: 'symbol'},
            {name: 'currencyname',mapping:'tocurrency'},
            {name: 'exchangerate'},
            {name: 'htmlcode'}
         ]);
         this.currencyStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
    //        url:Wtf.req.account+'CompanyManager.jsp'
            url:"ACCCurrency/getCurrencyExchange.do"
         });
         
         this.currencyStoreCMB = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.currencyRec),
    //        url:Wtf.req.account+'CompanyManager.jsp'
            url:"ACCCurrency/getCurrencyExchange.do"
         });

         this.currencyStoreCMB.load();
         
   this.sequenceFormatStoreRec = new Wtf.data.Record.create([
        {
            name: 'id'
        },

        {
            name: 'value'
        },
        {
            name: 'oldflag'
        }
        ]);
        this.sequenceFormatStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.sequenceFormatStoreRec),
            //        url: Wtf.req.account +'CompanyManager.jsp',
            url : "ACCCompanyPref/getSequenceFormatStore.do",
            baseParams:{
                mode:this.modeName,
                isEdit:this.isEdit
            }
        });
        this.sequenceFormatStore.on('load',function(){
        if(this.sequenceFormatStore.getCount()>0){
            if(this.isEdit ||this.copyInv){
                var index=this.sequenceFormatStore.find('id',this.record.data.sequenceformatid);   
                if(index!=-1){
                    this.sequenceFormatCombobox.setValue(this.record.data.sequenceformatid); 
                    this.sequenceFormatCombobox.disable();
                    this.Number.disable();   
                    
                    /*
                     * SDP-13487 : Below piece of code has written to get the next auto sequence no.when draft open in edit mode.
                     */
		    this.draftNo = this.record.data.billno;
                    this.isDraft = (this.record != null && this.record.data != undefined) ? this.record.data.isDraft : false;
                    if (this.isDraft && !this.copyInv && (this.draftNo==null || this.draftNo==undefined || this.draftNo=="")) { //SDP-13927 : In edit case, get the Next Auto No for Draft if it do not have any entryno.
                        this.Number.disable();
                        WtfGlobal.hideFormElement(this.Number);
                        this.getNextSequenceNumber(this.sequenceFormatCombobox);
			this.isAutoSeqForEmptyDraft = true;
                    } else if (this.copyInv) {//for copy NA enable disable number field
                        this.sequenceFormatCombobox.enable();
                        this.getNextSequenceNumber(this.sequenceFormatCombobox);
                    }
                } else {
                    this.sequenceFormatCombobox.setValue("NA"); 
                    if((this.isDraft || this.record.data.isDraft) && this.isEdit){
                        this.sequenceFormatCombobox.enable()    //SDP-13923 : In edit case, if sequence format is NA for draft record then keep Sequence Format Combox enable
                        this.Number.enable();  
                    } else if(this.viewGoodReceipt || this.isEdit){
                      this.sequenceFormatCombobox.disable();
                      if(this.viewGoodReceipt == undefined && this.viewGoodReceipt != true ){
                        this.Number.enable();   
                      }else{
                        this.Number.disable();   
                      }
                    }else{
                      this.Number.enable();  
                    }                      
                }
            } else{
                var count=this.sequenceFormatStore.getCount();
                for(var i=0;i<count;i++){
                    var seqRec=this.sequenceFormatStore.getAt(i)
                    if(seqRec.json.isdefaultformat=="Yes"){
                        this.sequenceFormatCombobox.setValue(seqRec.data.id) 
                        break;
                    }
                }
                if(this.sequenceFormatCombobox.getValue()!=""){
                    this.getNextSequenceNumber(this.sequenceFormatCombobox);
                } else{
                    this.Number.setValue(""); 
                    WtfGlobal.hideFormElement(this.Number);
                }
            }             
         }
        },this);
       this.sequenceFormatStore.load();
         
         var transdate=(this.isEdit?WtfGlobal.convertToGenericDate(this.record.data.date):WtfGlobal.convertToGenericDate(new Date()));

         this.Currency= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.currency.cur")+"*",  //'Currency',
            hiddenName:'currencyid',
            hidden : this.isRFQ,
            hideLabel : this.isRFQ,
            id:"currency"+this.heplmodeid+this.id,
            anchor: '94%',
            //disabled:true,
            store:this.currencyStore,
            valueField:'currencyid',
            allowBlank : this.isRFQ?true:false,
            forceSelection: true,
            displayField:'currencyname',
            scope:this,
            disabled: this.readOnly,
            selectOnFocus:true
        });

        this.Currency.on('select', function(){
        //                    this.currencychanged = true;
        //    this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue()),tocurrencyid:this.Currency.getValue()}});
        this.externalcurrencyrate=0;
        if(this.currencyStore.getCount()<1){
            callCurrencyExchangeWindow();
                    
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+"<b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
            this.Currency.setValue("");
        } else {
            this.updateFormCurrency();
        }
    }, this);
        
        this.Term= new Wtf.form.FnComboBox({
            fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.invoice.creditTerm"):WtfGlobal.getLocaleText("acc.invoice.debitTerm"))+' *',
            itemCls : (this.cash||(this.isEdit&&!this.isOrder)||(!this.isEdit&&this.isOrder))?"hidden-from-item":"",
            hideLabel:this.cash||(this.isEdit&&!this.isOrder)||(!this.isEdit&&this.isOrder)||(this.isEdit && this.isRFQ),
            id:"creditTerm"+this.heplmodeid+this.id,
            hidden:this.cash||(this.isEdit&&!this.isOrder)||(!this.isEdit&&this.isOrder)||(this.isEdit && this.isRFQ),
            hiddenName:'term',
            anchor: '93.5%',
            store:this.termds,
            valueField:'termdays',
            allowBlank:this.cash||(this.isEdit&&!this.isOrder)||(!this.isEdit&&this.isOrder)||(this.isEdit && this.isRFQ),
            emptyText:(this.isCustomer?WtfGlobal.getLocaleText("acc.inv.ct"):WtfGlobal.getLocaleText("acc.inv.dt")),
            forceSelection: true,
            displayField:'termname',
//            addNewFn:this.addCreditTerm.createDelegate(this),
            scope:this,
            listeners:{
                'select':{
                    fn:this.updateDueDate,
                    scope:this
                }
            },
            selectOnFocus:true
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.creditterm, Wtf.Perm.creditterm.edit))
            this.Term.addNewFn=this.addCreditTerm.createDelegate(this);
        this.Name= new Wtf.common.Select({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.ven") , //this.businessPerson+"*",
            hiddenName:this.businessPerson.toLowerCase(),
            id:"customer"+this.heplmodeid+this.id,
            store: this.isCustomer? Wtf.customerAccStore:Wtf.vendorAccStore,
            valueField:'accid',
            displayField:'accname',
            allowBlank:true,
            hideLabel : this.isRequisition ||(this.viewGoodReceipt&& !this.isRFQ),
            hidden:this.isRequisition ||(this.viewGoodReceipt&& !this.isRFQ),
            emptyText:WtfGlobal.getLocaleText("acc.inv.ven") , //'Select a '+this.businessPerson+'...',
            clearTrigger: this.viewGoodReceipt ? false : true,
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            disabled:(this.viewGoodReceipt&& this.isRFQ),
            anchor:"50%",
            multiSelect:true,
            xtype:'select',
            triggerAction:'all'
        });
//        this.Name= new Wtf.common.Select({
//            anchor:"50%",
//            hideLabel : this.isRequisition,
//            hidden:this.isRequisition,
//            fieldLabel: WtfGlobal.getLocaleText("acc.invoiceList.ven") ,
//            name:'accname',
//            store:Wtf.vendorAccStore,
////            hiddenName:'accname',
//            xtype:'select',
//            selectOnFocus:true,
//            forceSelection:true,
//            multiSelect:true,
//            displayField:'accname',
//            valueField:'accid',
//            mode: 'local',
//            triggerAction:'all',
//            typeAhead: true
//        })
//        this.VendorEmail = new Wtf
        this.VendorEmail = new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.field.VendorEmailAddress"),  //'Vendor Invoice Number*',
            name: 'othervendoremails',
//            emptyText : 'Enter Vendor Email Address',
            id:"othervendoremails"+this.heplmodeid+this.id,
            hidden:this.isRequisition ||(this.viewGoodReceipt&& !this.isRFQ),
            hideLabel:this.isRequisition ||(this.viewGoodReceipt&& !this.isRFQ),
            value : '',
            anchor:'50%',
            maxLength:512,
            disabled:(this.viewGoodReceipt&& this.isRFQ),
            scope:this,
            validator:WtfGlobal.validateMultipleEmail,
            allowBlank:this.isRFQ?!(Wtf.account.companyAccountPref.RFQGenerationMail || Wtf.account.companyAccountPref.RFQUpdationMail):true 
        });
        this.EmailMessage = new Wtf.Panel({
            border:false,
            hidden:this.isRequisition ||this. viewGoodReceipt,
            xtype:'panel',
            bodyStyle:'padding:0px 0px 10px 160px;',
            html:'<font color="#555555">'+ WtfGlobal.getLocaleText("acc.mail.seperator.comma") +'</font>'
        })
        // Neeraj
        if(!(this.DefaultVendor==null || this.DefaultVendor==undefined) && !this.isCustomer){
        	this.Name.value = this.DefaultVendor;
        	this.updateData();
        }

        if(!WtfGlobal.EnableDisable(this.custUPermType,this.custPermType.create))
            this.Name.addNewFn=this.addPerson.createDelegate(this,[false,null,this.businessPerson+"window",this.isCustomer],true);
        this.allAccountRec = new Wtf.data.Record.create([
            {name: 'accid'},
            {name: 'accname'},
            {name: 'groupid'},
            {name: 'groupname'},
            {name: 'level'},
            {name: 'leaf'},
            {name: 'openbalance'},
            {name: 'parentid'},
            {name: 'parentname'}
        ]);
        this.allAccountStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                totalProperty:'count',
                root: "data"
            },this.allAccountRec),
    //        url: Wtf.req.account +'CompanyManager.jsp',
            url:"ACCAccountCMN/getAccountsForCombo.do",
            baseParams:{
                mode:2,
                ignoreCashAccounts:true,
                ignoreBankAccounts:true,
                ignoreGSTAccounts:true,  
                ignorecustomers:true,  
                ignorevendors:true,
                nondeleted:true
            }
        });
        this.creditTo= new Wtf.form.FnComboBox({
            fieldLabel:(this.isCustomer?WtfGlobal.getLocaleText("acc.het.101"):WtfGlobal.getLocaleText("acc.field.DebitAccount*")),
            hiddenName:"creditoraccount",
            anchor:"50%",
            store: this.allAccountStore,
            valueField:'accid',
            displayField:'accname',
            hidden:!this.isCustBill,
            hideLabel:!this.isCustBill,
            itemCls : (!this.isCustBill)?"hidden-from-item":"",
            allowBlank:!this.isCustBill||this.isOrder,
            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.accPref.emptyText"),  //'Select an Account...',
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            triggerAction:'all',
//            addNewFn: this.addAccount.createDelegate(this,[this.allAccountStore],true),
            scope:this
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.createcoa))
            this.creditTo.addNewFn=this.addAccount.createDelegate(this,[this.allAccountStore],true);

        this.billTo = new Wtf.form.TextArea({
            fieldLabel: ((this.cash && !this.isCustomer)?WtfGlobal.getLocaleText("acc.invoice.address"):WtfGlobal.getLocaleText("acc.invoice.address")+"*"),//'Bill '+(this.isCustomer?'To':'From')+"*",
            name:'billto',
            id:"address"+this.heplmodeid+this.id,
            hidden:this.isOrder||this.isRequisition ||this. viewGoodReceipt,
            hideLabel:(this.isOrder||this.isCustomer||this.isRequisition)?true:false ||this. viewGoodReceipt,//this.isOrder,
            itemCls : this.isOrder?"hidden-from-item":"",
            allowBlank:((this.cash && !this.isCustomer)?true:this.isOrder||this.isRequisition),
            height:40,  
//            bodyStyle:'margin-top: 10px;',//(this.quotation || !this.isCustomer || this.isOrder)?"":'margin-top: 10px;',
            maxLength: 200,
            width :(this.quotation || !this.isCustomer || this.isOrder)?240: 200,
            emptyText :(this.quotation || !this.isCustomer || this.isOrder)?"": WtfGlobal.getLocaleText("acc.field.EnteraBillingAddress")
        });
        
        this.shipTo = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.invoice.address") +"*",
            name:'shipto',            
            hidden: (this.quotation || !this.isCustomer || this.isOrder),
            hideLabel:true,
            //itemCls : this.isOrder?"hidden-from-item":"",
            //allowBlank:(this.cash?true:this.isOrder),
            height:40,            
//            labelStyle:'margin-top: 10px;padding-left:0px;',            
            allowBlank:(this.quotation || !this.isCustomer || this.isOrder)?true:false,
            maxLength: 200,
            width : 200,
            emptyText :(this.quotation || !this.isCustomer || this.isOrder)?"": WtfGlobal.getLocaleText("acc.field.EnteraShippingAddress")
//            anchor:"50%"
        });

        this.perDiscountStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Percentage',true],['Flat',false]]
        });
        this.fromPOStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Yes',true],['No',false]]
        });
        
        var arrfromLink = new Array();
        
        if(this.isCustomer) {
            arrfromLink.push(['Sales Order',0]);
            if(Wtf.account.companyAccountPref.withinvupdate){
                arrfromLink.push(['Delivery Order',1]);    
            }
        } else {
            arrfromLink.push(['Purchase Order',0]);
            if(Wtf.account.companyAccountPref.withinvupdate){
                arrfromLink.push(['Goods Receipt',1]);    
            }
        }
        
        
        this.fromlinkStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value'}],
            data:arrfromLink
        });
        
        this.vendorInvoice=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.inv.invno"),  //'Vendor Invoice Number*',
            name: 'vendorinvoice',
            id:"vendorInvoiceNo"+this.heplmodeid+this.id,
            hidden:this.label=='Vendor Invoice'?false:true,
            anchor:'50%',
            maxLength:50,
            scope:this,
            allowBlank:this.checkin
        });
        
this.sequenceFormatCombobox = new Wtf.form.ComboBox({            
//        labelSeparator:'',
//        labelWidth:0,
        triggerAction:'all',
        mode: 'local',
        fieldLabel:WtfGlobal.getLocaleText("acc.MissingAutoNumber.SequenceFormat"),
        valueField:'id',
        displayField:'value',
        store:this.sequenceFormatStore,
        disabled:(this.isEdit&&!this.copyInv&&!this.isPOfromSO&&!this.isSOfromPO?true:false),  
        anchor:'50%', //ERP-6599
        typeAhead: true,
        forceSelection: true,
        name:'sequenceformat',
        id:"sequenceformat"+this.heplmodeid+this.id,
        hiddenName:'sequenceformat',
        allowBlank:false,
        listeners:{
            'select':{
                fn:this.getNextSequenceNumber,
                scope:this
            }
        }
            
    });
    this.sequenceFormatCombobox.on('change',this.sequenceFormatChanged,this);   //SDP-13923 : Call the 'sequenceFormatChanged' function on Sequence Format change.    
        
        this.Number=new Wtf.form.TextField({
            fieldLabel:(this.isPOfromSO?WtfGlobal.getLocaleText("acc.accPref.autoPO"):(this.isEdit?this.label:this.label)) + " " + WtfGlobal.getLocaleText("acc.common.number"),  //,  //this.label+' Number*',
            name: 'number',
            disabled:(this.isEdit&&!this.copyInv&&!this.isPOfromSO?true:false) || this.viewGoodReceipt,
            id:this.isEdit || this.copyInv ? "invoiceNo"+this.heplmodeid+this.id:"invoiceNo"+this.heplmodeid, //+this.id,
            anchor:'50%',
            maxLength:50,
            scope:this,
            allowBlank:this.checkin
        });
        this.PORefNo=new Wtf.form.TextField({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.POrefNo"),  //PO Reference Number',
            name: 'porefno',
            hidden:this.isOrder||!this.isCustomer,
            hideLabel:this.isOrder||!this.isCustomer,
            itemCls : (this.isOrder||!this.isCustomer)?"hidden-from-item":"",
            anchor:'50%',
            maxLength:45,
            scope:this

        });
        this.Memo=new Wtf.form.TextArea({
            fieldLabel:Wtf.account.companyAccountPref.descriptionType,  //'Memo/Note',
            name: 'memo',
            id:this.isEdit || this.copyInv? "prmemo"+this.heplmodeid+this.id :"prmemo"+this.heplmodeid+this.id, //+this.id,
            height:40,
            readOnly: this.readOnly,
            anchor:'94%',
            maxLength: this.isRFQ ? 8000 : 2048 ,
            qtip:(this.record==undefined)?' ':this.record.data.memo,
            listeners: {
                render: function(c){
                    Wtf.QuickTips.register({
                        target: c.getEl(),
                        text: c.qtip
                    });
                }
            }

            
        });
        this.Discount=new Wtf.form.NumberField({
            allowNegative:false,
            hidden:this.quotation?false:this.isOrder,
            defaultValue:0,
            hideLabel:this.quotation?false:this.isOrder,
            allowBlank:this.isOrder,
            maxLength: 10,
            anchor:'90%',
            fieldLabel:WtfGlobal.getLocaleText("acc.invoiceList.discount"),  //'Discount',
            name:'discount',
            id:"discount"+this.heplmodeid+this.id,
            listeners:{
                'change':{
                    fn:this.updateSubtotal,
                    scope:this
                }
            }
        });
        this.perDiscount= new Wtf.form.ComboBox({
            labelSeparator:'',
            labelWidth:0,
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            displayField:'name',
            store:this.perDiscountStore,
            hidden:this.quotation?false:this.isOrder,
            hideLabel:true,
            allowBlank:this.isOrder,
            value:false,
            anchor:'97%',
            typeAhead: true,
            forceSelection: true,
            name:'perdiscount',
            hiddenName:'perdiscount',
            listeners:{
                'select':{
                    fn:this.updateSubtotal,
                    scope:this
                }
            }
        });

         this.PORec = Wtf.data.Record.create ([
            {name:'billid'},
            {name:'journalentryid'},
            {name:'entryno'},
            {name:'billto'},
            {name:'discount'},
            {name:'shipto'},
            {name:'mode'},
            {name:'billno'},
            {name:'date', type:'date'},
            {name:'duedate', type:'date'},
            {name:'shipdate', type:'date'},
            {name:'personname'},
            {name:'creditoraccount'},
            {name:'personid'},
            {name:'shipping'},
            {name:'othercharges'},
            {name:'taxid'},
            {name:'currencyid'},
            {name:'amount'},
            {name:'amountdue'},
            {name:'costcenterid'},
            {name:'costcenterName'},
            {name:'memo'},
            {name:'othervendoremails'},
            {name:'includeprotax',type:'boolean'},
            {name:'amountinbase'},
            {name:'currencysymbol'} //Currency Symbol, to show currency symbol on RFQ Grid
        ]);
        this.POStoreUrl = "";
        var closeFlag = true;
        if(this.businessPerson=="Customer"){
            //mode:(this.isCustBill?52:42)
            if(this.isOrder) {
                this.POStoreUrl = "ACCSalesOrderCMN/getQuotations.do"
            } else {
                this.POStoreUrl = this.isCustBill?"ACCSalesOrderCMN/getBillingSalesOrders.do":"ACCSalesOrderCMN/getSalesOrders.do";
            }
        
            
        }else if(this.businessPerson=="Vendor"){
            if(this.isOrder) {
                if(this.isPOfromSO){
                    closeFlag = false;
                }    
                this.POStoreUrl = this.isCustBill?"ACCSalesOrderCMN/getBillingSalesOrders.do":"ACCSalesOrderCMN/getSalesOrders.do";
            } else {
                this.POStoreUrl = this.isCustBill?"ACCPurchaseOrderCMN/getBillingPurchaseOrders.do":"ACCPurchaseOrderCMN/getPurchaseOrders.do";
            }
        }
        this.POStore = new Wtf.data.Store({
            url:this.POStoreUrl,
    //        url: Wtf.req.account+this.businessPerson+'Manager.jsp',
            baseParams:{
                mode:(this.isCustBill?52:42),
                closeflag:closeFlag
            },
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:'count'
            },this.PORec)
        });
        var colModelArray = GlobalColumnModelForReports[this.moduleid];
        if(colModelArray){
           for(var cnt = 0;cnt < colModelArray.length;cnt++){
               var fieldname = colModelArray[cnt].fieldname;
               var newField = new Wtf.data.ExtField({                   // ERP-35175 - Used Wtf.data.ExtField instead of Wtf.data.Field just like used in invoice.
                   name:fieldname.replace(".",""),
//                   sortDir:'ASC',
                   type:colModelArray[cnt].fieldtype == 3 ?  'date' : (colModelArray[cnt].fieldtype == 2?'float':'auto'),
                   dateFormat:colModelArray[cnt].fieldtype == 3 ? 'time' : undefined
               });
               this.POStore.fields.items.push(newField);
               this.POStore.fields.map[fieldname]=newField;
               this.POStore.fields.keys.push(fieldname);
           }
           this.POStore.reader = new Wtf.data.KwlJsonReader(this.POStore.reader.meta, this.POStore.fields.items);
       } 
        this.fromPO= new Wtf.form.ComboBox({
            triggerAction:'all',
            hideLabel:this.cash|| this.quotation || (this.isOrder && this.isCustBill),
            hidden:this.cash|| this.quotation|| (this.isOrder && this.isCustBill),
            mode: 'local',
            valueField:'value',
            displayField:'name',
            store:this.fromPOStore,
            id: "linkToOrder"+this.heplmodeid+this.id,
            fieldLabel:((!this.isCustBill && !this.isOrder && !this.cash)?WtfGlobal.getLocaleText("acc.field.Link"):(this.isOrder && this.isCustomer)? WtfGlobal.getLocaleText("acc.invoice.linkToQuote") :(this.isOrder && !this.isCustomer)? WtfGlobal.getLocaleText("acc.invoice.linkToSO"): (this.isCustomer?WtfGlobal.getLocaleText("acc.invoice.linkToSO"):WtfGlobal.getLocaleText("acc.invoice.linkToPO"))) ,  //"Link to "+(this.isCustomer?"Sales":"Purchase")+" Order",
            allowBlank:this.isOrder,
            value:false,
            width:50,
            typeAhead: true,
            forceSelection: true,
            name:'prdiscount',
            hiddenName:'prdiscount',
            listeners:{
                'select':{
                    fn:this.enablePO,
                    scope:this
                }
            }
        });
        
        
     this.usersRec = new Wtf.data.Record.create([
        {name: 'userid'},
        {name: 'username'},
        {name: 'fname'},
        {name: 'lname'},
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
    if(!(this.quotation || !this.isCustomer || this.isOrder)){
         this.userds.load();    
    }    

    this.users= new Wtf.form.ComboBox({            
            triggerAction:'all',
            mode: 'local',
            selectOnFocus:true,
            valueField:'userid',
            displayField:'fname',
            store:this.userds,                                                 
            anchor: '94%',
            typeAhead: true,
            forceSelection: true,
            fieldLabel:WtfGlobal.getLocaleText("acc.masterConfig.15"),
            emptyText: WtfGlobal.getLocaleText("acc.field.SelectSalesPerson"),
            hideLabel:(this.quotation || !this.isCustomer || this.isOrder),
            hidden:(this.quotation || !this.isCustomer || this.isOrder),                        
            name:'salesPerson',
            hiddenName:'salesPerson'            
        });
        
        this.copyAddress= new Wtf.form.Checkbox({
            name:'copyadress',
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.sameasbillingadd"),  //'Copy Address',
            checked:false,
            hideLabel:(this.quotation || !this.isCustomer || this.isOrder),
            hidden:(this.quotation || !this.isCustomer || this.isOrder),
            cls : 'custcheckbox',
            width: 10
        });
     
        
        this.billingAddrsRec = new Wtf.data.Record.create([{           
            name:'addressvalue'
        },{
            name:'addressname'
        }]);
    
        this.billingAddrsStore = new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.billingAddrsRec),
            url :"ACCCustomer/getAddresses.do",
            baseParams:{
                mode:4,
                isBillingAddress:true
            //customerid:customer
            }
        });
        
        this.billingAddrs = new Wtf.form.ComboBox({
            triggerAction:'all',
            hideLabel:(this.quotation || !this.isCustomer || this.isOrder),
            hidden:(this.quotation || !this.isCustomer || this.isOrder),
            mode: 'local',
            valueField:'addressvalue',
            displayField:'addressname',
            store:this.billingAddrsStore,
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectBillingAddress..."),  //'Select Billing Address...',
            fieldLabel:WtfGlobal.getLocaleText("acc.cust.billingadd"),
            disabled:true,
            //allowBlank:this.isOrder,            
            width:240,
            typeAhead: true,
            forceSelection: true,
            //name:'prdiscount',
            //hiddenName:'prdiscount',
            listeners:{
                'select':{
                    fn:function(c,rec){
                        if(rec.data.addressvalue!=null){
                            this.billTo.setValue(rec.data.addressvalue);
                        }
                    },
                    scope:this
                }
            }
        });
        
        this.ShippingAddrsStore =  new Wtf.data.Store({
            reader: new Wtf.data.KwlJsonReader({
                root: "data",
                totalProperty:"count"
            },this.billingAddrsRec),
            url :"ACCCustomer/getAddresses.do",
            baseParams:{
                mode:4,
                isBillingAddress:false                
            }
        });
        this.shippingAddrs= new Wtf.form.ComboBox({
            triggerAction:'all',
            hideLabel:(this.quotation || !this.isCustomer || this.isOrder),
            hidden:(this.quotation || !this.isCustomer || this.isOrder),
            mode: 'local',
            valueField:'addressvalue',
            displayField:'addressname',
            emptyText:WtfGlobal.getLocaleText("acc.field.SelectShippingAddress..."),  //'Select Shipping Address...',
            store:this.ShippingAddrsStore,            
            disabled:true,
            fieldLabel:WtfGlobal.getLocaleText("acc.customerList.gridShippingAddress"),
            //allowBlank:this.isOrder,            
            width:240,
            typeAhead: true,
            forceSelection: true,
            //            name:'prdiscount',
            //            hiddenName:'prdiscount',
            listeners:{
                'select':{
                    fn:function(c,rec){
                        if(rec.data.addressvalue!=null){
                            this.shipTo.setValue(rec.data.addressvalue);
                        }
                    },
                    scope:this
                }
            }
        });
        
        this.billingAddrsStore.on("load", function(){
            if(this.billingAddrsStore.data.length>0){
                this.billingAddrs.enable();
                this.shippingAddrs.enable();
                this.billingAddrs.setValue(this.billingAddrsStore.data.items[0].data.addressvalue);
                if(!this.isEdit){
                    this.billTo.setValue(this.billingAddrsStore.data.items[0].data.addressvalue);                
                }                
            }                    
        }, this);
    
        this.ShippingAddrsStore.on("load", function(){
            if(this.ShippingAddrsStore.data.length>0){                
                this.shippingAddrs.setValue(this.ShippingAddrsStore.data.items[0].data.addressvalue);
                if(!this.isEdit){
                    this.shipTo.setValue(this.ShippingAddrsStore.data.items[0].data.addressvalue);                
                }    
            }                    
        }, this);
        
        this.copyAddress.on('check',function(o,newval,oldval){
            var val1=newval?this.billTo.getValue():this.shippingAddrs.getValue();
            (newval)?this.shippingAddrs.disable():this.shippingAddrs.enable();
            if(this.isCustomer){
                this.shipTo.setValue(val1);
            }                
        },this);
                 
        this.templateRec = new Wtf.data.Record.create([
            {name: 'tempid'},
            {name: 'tempname'}
        ]);
        
        this.templateStore = new Wtf.data.Store({
            url : "ExportPDF/getAllReportTemplate.do",
            
            method: 'GET',
            baseParams : {
                templatetype : this.doctype
            },
            reader: new Wtf.data.KwlJsonReader({
                root: 'data'
            },this.templateRec)
        });
         this.templateStore.load();
        this.template= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.grid.header.template")+"*",
            hiddenName:"template",
            anchor:"94%",
            store: this.templateStore,
            valueField:'tempid',
            displayField:'tempname',
//            itemCls : (!this.isCustBill)?"hidden-from-item":"",
//            allowBlank:!this.isCustBill||this.isOrder,
//            hirarchical:true,
            emptyText:WtfGlobal.getLocaleText("acc.invoice.grid.template.emptyText"),
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            allowBlank:!(this.doctype==1), 
            hidden:!(this.doctype==1), 
            hideLabel:!(this.doctype==1),      
            selectOnFocus:true,
           // value : this.isEdit ? this.record.data.templatename :'',
            triggerAction:'all',
//            addNewFn: this.addAccount.createDelegate(this,[this.allAccountStore],true),
            scope:this,
            listeners:{
                'change':{
                    fn:this.setTemplateID,
                    scope:this
                }
            }
        });
        this.templateID=new Wtf.form.Hidden({
        	scope:this,
        	value: this.isEdit || this.copyInv ? this.record.data.templateid : ''
        });
//        if(!WtfGlobal.EnableDisable(Wtf.UPerm.coa, Wtf.Perm.coa.create))
        this.template.addNewFn=this.addInvoiceTemplate.createDelegate(this,[this.templateStore],true);
        
        this.includeTaxStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Yes',true],['No',false]]
        });
        
        this.partialInvoiceStore = new Wtf.data.SimpleStore({
            fields:[{name:'name'},{name:'value',type:'boolean'}],
            data:[['Yes',true],['No',false]]
        });
        
        this.includeProTax= new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            displayField:'name',
            store:this.includeTaxStore,
           // id: "linkToOrder"+this.id+this.heplmodeid,
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.productTax"),//"Include Product Tax",
            id:"includeprotax"+this.heplmodeid+this.id,
          //  allowBlank:this.isOrder,
            value:(this.isEdit || this.copyInv?true:false),
            anchor:'94%',
            typeAhead: true,
            forceSelection: true,
            name:'includeprotax',
            hiddenName:'includeprotax',
            listeners:{
                'select':{
                    fn:this.showGridTax,
                    scope:this
                }
            }
        });
        
        this.partialInvoiceCmb= new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            hidden : (this.isCustomer && !this.isCustBill && !this.isOrder && !this.cash) ? false : true,
            hideLabel : (this.isCustomer && !this.isCustBill && !this.isOrder && !this.cash) ? false : true,
            displayField:'name',
            store:this.partialInvoiceStore,
           // id: "linkToOrder"+this.id+this.heplmodeid,
            fieldLabel:WtfGlobal.getLocaleText("acc.field.IsPartialInvoice"),
            id:"isPartialInv"+this.id,
          //  allowBlank:this.isOrder,
            value:false,
            anchor:'94%',
            disabled : true,
            typeAhead: true,
            forceSelection: true,
            name:'partialinv',
            hiddenName:'partialinv',
            listeners:{
                'select':{
                    fn:this.showPartialDiscount,
                    scope:this
                }
            }
        });
        
        this.fromLinkCombo= new Wtf.form.ComboBox({
            name:'fromLinkCombo',
            triggerAction:'all',
            hideLabel:((!this.isCustBill && !this.isOrder && !this.cash &&!this.isRequisition)?false:true) ||this.viewGoodReceipt,
            hidden:((!this.isCustBill && !this.isOrder && !this.cash &&!this.isRequisition)?false:true) ||this.viewGoodReceipt,
            mode: 'local',
            valueField:'value',
            displayField:'name',
            disabled:true,
            store:this.fromlinkStore,                        
            emptyText: Wtf.account.companyAccountPref.withinvupdate? (this.isCustomer? WtfGlobal.getLocaleText("acc.field.SelectSO/DO") : WtfGlobal.getLocaleText("acc.field.SelectPO/GR")) : (this.isCustomer? "Select SO" : "Select PO"),
            fieldLabel:WtfGlobal.getLocaleText("acc.field.Linkto"),  //"Link to "+(this.isCustomer?"Sales":"Purchase")+" Order",
            allowBlank:false,            
//            value:false,            
            typeAhead: true,            
            width:130,
            forceSelection: true,                        
            selectOnFocus:true,           
            scope:this,
            listeners:{
                'select':{
                    fn:this.enableNumber,
                    scope:this
                }
            }
        });

//        this.PO = new Wtf.common.Select(Wtf.applyIf({
//            multiSelect:true,
//            fieldLabel:WtfGlobal.getLocaleText("acc.MailWin.msgPR21")+"*",
//            hiddenName:"ordernumber",
//            id:"orderNumber"+this.heplmodeid+this.id,
//            store: this.POStore,
//            valueField:'billid',
//            hideLabel:this.isRFQ?false:true,
//            hidden:this.isRFQ?false:true,
//            disabled:this.isEdit?true:false,
//            displayField:'billno',
//            emptyText:"Select PR",
//            mode: 'local',
//            typeAhead: true,
//            forceSelection: true,
//            addCreateOpt:true,
//            addNewFn:this.addSelectedDocument.createDelegate(this),
//            width:240,
//            selectOnFocus:true,
//            anchor:"50%",
//            allowBlank:(this.isRFQ && Wtf.account.companyAccountPref.isPRmandatory)?false:true,
//            triggerAction:'all',
//            clearTrigger:this.isEdit?false:true,
//            scope:this,
//            listeners:{
//                'select': function(){
//                    this.PR_IDS = this.PO.getValue();
//                    this.loadProductGridForRFQ();
//                    /*
//                    *	ERM-1037
//                    *	On link document selection send id of linked document to function to restrict linking of future dated document
//                    */
//                    if (!this.readOnly) {
//                        WtfGlobal.checkForFutureDate(this, this.PO.getValue().split(','));
//                    }
//                },
//                'unselect':function(){
//                    this.PR_IDS = this.PO.getValue();
//                    this.loadProductGridForRFQ();
//                },
//                scope:this
//            }
//        }));
        var configCombo ={
            hiddenName:"ordernumber",
            fieldLabel:WtfGlobal.getLocaleText("acc.MailWin.msgPR21")+"*",
            id:"orderNumber"+this.heplmodeid+this.id,
            valueField:'billid',
            displayField:'billno',
            emptyText:"Select PR",
            hideLabel:this.isRFQ?false:true,
            hidden:this.isRFQ?false:true,
            disabled:this.isEdit?true:false,
            addNewFn:this.addSelectedDocument.createDelegate(this),
            allowBlank:(this.isRFQ && Wtf.account.companyAccountPref.isPRmandatory)?false:true,
            clearTrigger:this.isEdit?false:true,
            listeners:{
                'select': function(){
                    this.PR_IDS = this.PO.getValue();
                    this.loadProductGridForRFQ();
                    /*
                    *	ERM-1037
                    *	On link document selection send id of linked document to function to restrict linking of future dated document
                    */
                    if (!this.readOnly) {
                        WtfGlobal.checkForFutureDate(this, this.PO.getValue().split(','));
                    }
                },
                'unselect':function(){
                    this.PR_IDS = this.PO.getValue();
                    this.loadProductGridForRFQ();
                },
                scope:this
            }
        };
        this.PO = CommonERPComponent.commonMultiselectPagingComboBox(this, this.POStore, configCombo);
        this.PO.on("blur", function(combo) {

            /* If user input any value without selecting value from the combo            * 
             * then we do not allow to do so
             */

            validateSelectionOfLinkingCombo(combo);

        }, this);
        
        this.PO.on("clearval",function(){
            if(this.PO.getValue()==""){  
                this.PR_IDS = "";
                this.Grid.getStore().removeAll();            
                this.Grid.addBlankRow();      
                var fieldArr = this.POStore.fields.items;
                for(var fieldCnt=0; fieldCnt < fieldArr.length; fieldCnt++) {
                    var fieldN = fieldArr[fieldCnt];
                    if(Wtf.getCmp(fieldN.name+this.tagsFieldset.id)) {
                        Wtf.getCmp(fieldN.name+this.tagsFieldset.id).setValue('');
                    }
                }
            }
        },this);
        
        this.DueDate= new Wtf.form.DateField({
            fieldLabel: WtfGlobal.getLocaleText("acc.invoice.dueDate"),//'Due Date*',
            name: 'duedate',
            id:"prduedate"+this.heplmodeid+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            allowBlank:false,
            disabled:this.viewGoodReceipt,
            anchor:'94%'
        });

        this.billDate= new Wtf.form.DateField({
            fieldLabel:(this.isPOfromSO?WtfGlobal.getLocaleText("acc.accPref.autoPO"):(this.isEdit?this.label:this.label)) +' '+WtfGlobal.getLocaleText("acc.invoice.date"),
            id:this.isEdit || this.copyInv?"invoiceDate"+this.heplmodeid+this.id:"invoiceDate"+this.heplmodeid+this.id,  //+this.id,
            format:WtfGlobal.getOnlyDateFormat(),
            name: 'billdate',
            disabled:this.viewGoodReceipt,
            anchor:'50%',
            listeners:{
                'change':{
                    fn:this.updateDueDate,
                    scope:this
                }
            },
            allowBlank:false
////            disabled: true
//            maxValue: new Date(Wtf.serverDate),
//            minValue: new Date(Wtf.account.companyAccountPref.fyfrom)
        });
        var isbchlFields= this.isRequisition || this.isRFQ ? true : (this.isCustomer || !this.isOrder ||this.isCustBill || (BCHLCompanyId.indexOf(companyid) == -1));
        this.youtReftxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.yourref.label"), //'Vendor Invoice Number*',
            name: 'poyourref',
            hidden: isbchlFields ,
            hideLabel: isbchlFields ,
            id: "poyourref" + this.heplmodeid + this.id,
            anchor: "50%",
            maxLength: 255,
            scope: this
        });
        this.delydatetxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.delydate.label"), //'Vendor Invoice Number*',
            name: 'delydate',
            hidden: isbchlFields ,
            hideLabel: isbchlFields ,
            id: "delydate" + this.heplmodeid + this.id,
            anchor: '94%',
            maxLength: 255,
            scope: this
        });
        this.delytermtxt = new Wtf.form.TextArea({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.delyterm.label"), //'Vendor Invoice Number*',
            name: 'delyterm',
            hidden: isbchlFields,
            hideLabel:isbchlFields ,
            id: "delyterm" + this.heplmodeid + this.id,
            anchor: "50%",
            height: 40,
            maxLength: 255,
            width: 200,
            scope: this
        });
        this.invoiceTotxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.invoiceto.label"), //'Vendor Invoice Number*',
            name: 'invoiceto',
            hidden:isbchlFields ,
            hideLabel: isbchlFields ,
            id: "invoiceto" + this.heplmodeid + this.id,
            anchor: '50%',
            maxLength: 255,
            scope: this
        });
        this.projecttxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.project.label"), //'Vendor Invoice Number*',
            name: 'project',
            hidden: isbchlFields ,
            hideLabel: isbchlFields ,
            id: "project" + this.heplmodeid + this.id,
            anchor: '94%',
            maxLength: 255,
            scope: this
        });
        this.depttxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.dept.label"), //'Vendor Invoice Number*',
            name: 'podept',
            hidden:isbchlFields ,
            hideLabel: isbchlFields ,
            id: "podept" + this.heplmodeid + this.id,
            anchor: '94%',
            maxLength: 255,
            scope: this
        });
        this.requestortxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.requestor.label"), //'Vendor Invoice Number*',
            name: 'requestor',
            hidden: isbchlFields ,
            hideLabel:isbchlFields ,
            id: "requestor" + this.heplmodeid + this.id,
            anchor: '94%',
            maxLength: 255,
            scope: this
        });
        this.mernotxt = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.inv.merno.label"), //'Vendor Invoice Number*',
            name: 'merno',
            hidden: isbchlFields,
            hideLabel: isbchlFields ,
            id: "merno" + this.heplmodeid + this.id,
            anchor: '94%',
            maxLength: 255,
            scope: this
        });
        chkFormCostCenterload();
        this.CostCenter= new Wtf.form.ExtFnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.common.costCenter"),//"Cost Center",
            hiddenName:"costcenter",
            id:"costcenter"+this.heplmodeid+this.id,
            store: Wtf.FormCostCenterStore,
            valueField:'id',
            displayField:'name',
            extraFields:Wtf.account.companyAccountPref.accountsWithCode?['ccid']:[],
            listWidth:Wtf.account.companyAccountPref.accountsWithCode?500:400,
            isProductCombo:true,
            mode: 'local',
            typeAhead: true,
            forceSelection: true,
            selectOnFocus:true,
            anchor:"50%",
            triggerAction:'all',
            addNewFn:this.addCostCenter,
            scope:this,
            hidden: this.quotation,
            hideLabel: this.quotation
        });

        
        var itemArr={};
            itemArr = [this.sequenceFormatCombobox,this.Number,this.billDate,this.Name,{
                layout:'column',
                border:false,
                defaults:{border:false},items:[ {
                    layout:'form',
                    ctCls : (this.cash||this.isOrder)?"hidden-from-item":"",
                    width:215,
                    items:this.fromPO                        
                },{
                    width:210,
                    layout:'form',
                    labelWidth:50,
                    items:this.fromLinkCombo
               }]},this.PO,this.VendorEmail,this.EmailMessage,{
                layout : 'table',
                border:false,
                items : [
                    {
                        layout : 'form',
                        border:false,
                        items : [this.billingAddrs]
                    },
                    {
                        layout : 'form',
                        border:false,
                        bodyStyle:(this.quotation || !this.isCustomer || this.isOrder)?"":'padding-top:20px;padding-left:5px;',
                        items : [this.billTo]
                    }
                    ]
                   },this.copyAddress,
                {
                layout : 'table',
                border:false,
                items : [
                    {
                        layout : 'form',
                        border:false,
//                        labelWidth:80,
//                        width:210,
                        items : [this.shippingAddrs]
                    },
                    {
                        layout : 'form',
                        border:false,
                        bodyStyle:(this.quotation || !this.isCustomer || this.isOrder)?"":'padding-top:20px;padding-left:5px;',
                        
//                        labelWidth:80,
//                        width:210,
                        items : [this.shipTo]
                    }
                ]
            },this.PORefNo, this.CostCenter,this.youtReftxt,this.delytermtxt,this.invoiceTotxt];
       var ht=150;//(this.isOrder?(Wtf.isIE?150:250):(Wtf.isIE?260:230));
       if(this.isCustBill)ht+=25;
       if(!(this.quotation || !this.isCustomer || this.isOrder))ht+=130;
       //if(!this.isCustBill && !this.isOrder && !this.cash && this.isCustomer)ht+=25;
       

    this.tagsFieldset = new Wtf.account.CreateCustomFields({
        border: false,
        compId:"northForm"+this.id,
        autoHeight: true,
        parentcompId:this.id,
        moduleid: this.moduleid,
        isEdit: this.isEdit || this.copyInv,
//        copyInv: this.copyInv,
        record: this.record,
        isViewMode:this.readOnly
    });
        this.NorthForm=new Wtf.form.FormPanel({
            region:'north',
            autoHeight:true,
            border:false,
            disabledClass:"newtripcmbss",
           // disabled:this.readOnly,
            id:"northForm"+this.id,
            items:[{
                layout:'form',
                baseCls:'northFormFormat',
                labelWidth:155,
                cls:"visibleDisabled",
                items:[{
                    layout:'column',
                    border:false,
                    defaults:{border:false},
                    items:[{
                        layout:'form',
                        columnWidth:0.65,
                        border:false,
                        items:itemArr
                    },{
                        layout:'form',
                        columnWidth:0.35,
                        border:false,
                        items:[this.Term,this.DueDate,/*{
                            itemCls : (this.isOrder && !this.quotation)?"hidden-from-item":"",
                            layout:'column',
                            border:false,
                            defaults:{border:false},
                            items:[{
                                layout:'form',
                                columnWidth:0.65,
                                items:this.Discount
                            },{
                                columnWidth:0.3,
                                layout:'form',
                                items:this.perDiscount
                           }]
                        },*/this.Memo,this.Currency/*,this.includeProTax*/, this.partialInvoiceCmb,this.template,this.templateID,this.users,this.delydatetxt,this.projecttxt,this.depttxt,this.requestortxt,this.mernotxt]
                    }]
            },this.tagsFieldset]
            }]
        });
        var blockSpotRateLink_first = "";
        var blockSpotRateLink_second = "";
        if(!Wtf.account.companyAccountPref.activateToBlockSpotRate && !this.isLinkedTransaction){ // If activateToBlockSpotRate is set then block the Spot Rate Links
            blockSpotRateLink_first = WtfGlobal.getLocaleText("acc.invoice.msg9")+"</div><div style='padding-left:30px;padding-top:5px;padding-bottom:10px;'><a class='tbar-link-text' href='#' onClick='javascript: editInvoiceExchangeRates(\""+this.id+"\",\"{foreigncurrency}\",\"{basecurrency}\",\"{revexchangerate}\",\"foreigntobase\")'wtf:qtip=''>{foreigncurrency} to {basecurrency}</a>";
            blockSpotRateLink_second = WtfGlobal.getLocaleText("acc.invoice.msg9")+"</div> <div style='padding-left:30px;padding-top:5px;'><a class='tbar-link-text' href='#' onClick='javascript: editInvoiceExchangeRates(\""+this.id+"\",\"{basecurrency}\",\"{foreigncurrency}\",\"{exchangerate}\",\"basetoforeign\")'wtf:qtip=''>{basecurrency} to {foreigncurrency}</a></div>";
        }
       this.southCenterTplSummary=new Wtf.XTemplate(
         "<div> &nbsp;</div>",  //Currency:
             '<tpl if="editable==true">',
         "<b>"+WtfGlobal.getLocaleText("acc.invoice.msg8")+"</b>",  //Applied Exchange Rate for the current transaction:
           "<div style='line-height:18px;padding-left:30px;'>1 {foreigncurrency} "+WtfGlobal.getLocaleText("acc.inv.for")+" = {revexchangerate} {basecurrency} "+WtfGlobal.getLocaleText("acc.inv.hom")+". "+
         blockSpotRateLink_first,
         "</div><div style='line-height:18px;padding-left:30px;'>1 {basecurrency} "+WtfGlobal.getLocaleText("acc.inv.hom")+" = {exchangerate} {foreigncurrency} "+WtfGlobal.getLocaleText("acc.inv.for")+". "+    
         blockSpotRateLink_second,
             '</tpl>'
        );
        this.southCenterTpl=new Wtf.Panel({
            border:false,
            hidden:true,//ERP-39784
            html:this.southCenterTplSummary.apply({basecurrency:WtfGlobal.getCurrencyName(),exchangerate:'x',foreigncurrency:"Foreign Currency", editable:false})
        });
        this.southCalTemp=new Wtf.Panel({
            border:false,
            hidden : this.isRFQ,
            baseCls:'tempbackgroundview',
            html:this.tplSummary.apply({subtotal:WtfGlobal.currencyRenderer(0),discount:WtfGlobal.currencyRenderer(0),totalamount:WtfGlobal.currencyRenderer(0),tax:WtfGlobal.currencyRenderer(0),aftertaxamt:WtfGlobal.currencyRenderer(0),totalAmtInBase:WtfGlobal.currencyRenderer(0)})
        });
        this.helpMessage= new Wtf.Button({
            text:WtfGlobal.getLocaleText("acc.dashboard.help"),  //'Help',
            handler:this.helpmessage,
            scope:this,
            tooltip: WtfGlobal.getLocaleText("acc.common.click"),  //'Click for help',
            iconCls: 'help'
        });
        this.addGrid();
        this.isTaxable= new Wtf.form.ComboBox({
            triggerAction:'all',
            mode: 'local',
            valueField:'value',
            displayField:'name',
            id:"includetax"+this.heplmodeid+this.id,
            store:this.fromPOStore,
            listWidth:50,
            fieldLabel:WtfGlobal.getLocaleText("acc.inv.totax"),  //"Include Total Tax",
            allowBlank:this.isOrder,
            value:false,
            width:50,
            hidden:true,
            hideLabel :true,
            typeAhead: true,
            forceSelection: true,
            name:'includetax',
            hiddenName:'includetax',
            listeners:{
                'select':{
                    fn:this.enabletax,
                    scope:this
                }
            }
        });
        this.Tax= new Wtf.form.FnComboBox({
            fieldLabel:WtfGlobal.getLocaleText("acc.invoice.Tax"),  //'Tax',
            id:"tax"+this.heplmodeid+this.id,
            disabled:!this.isEdit,
            hiddenName:'tax',
            anchor: '97%',
            store:this.Grid.taxStore,
            valueField:'prtaxid',
            forceSelection: true,
            hidden:true,
            hideLabel :true,
            displayField:'prtaxname',
//            addNewFn:this.addTax.createDelegate(this),
            scope:this,
            listeners:{
                'select':{
                    fn:this.updateSubtotal,
                    scope:this
                }
            },
            selectOnFocus:true
        });
        if(!WtfGlobal.EnableDisable(Wtf.UPerm.tax, Wtf.Perm.tax.view))
            this.Tax.addNewFn=this.addTax.createDelegate(this);

        this.southEastPanel=new Wtf.Panel({
//                region:'east',
                id : this.id + 'southEastPanel',
                columnWidth: .55,//width:650,
                border:false,
                layout:'column',
                items:[{
                    layout:'form',
                    width:170,
                    labelWidth:100,
                    border:false,
                    items:this.isTaxable
                },{
                    layout:'form',
                    columnWidth:0.4,
                    labelWidth:30,
                    border:false,
                    items:this.Tax
                },{
                    columnWidth:0.6,
                    layout:'form',
                    cls:'bckgroundcolor',
                    bodyStyle:'padding:10px',
                    labelWidth:70,
                    hidden:this.isRFQ ? true : false,
                    items:this.southCalTemp
               }]
            });
        this.southPanel=new Wtf.Panel({
            region:'center',
            border:false,
            style:'padding:0px 10px 10px 10px',
            layout:'column',//layout:'border',//Bug Fixed: 14871[SK] Scrolling issue : changed layout from border to column
            height:Wtf.account.companyAccountPref.openPOandSO?310:210,
            items:[{
                columnWidth: .45,// width: 570,//region:'center',
                border:false,
                items:[this.productDetailsTpl,this.southCenterTpl]
            },this.southEastPanel]
        });
        
        this.deleteSelectedPanel = new Wtf.Panel({
            style: 'padding: 10px 10px 0;',
            border: false,
            autoScroll: true,
            hidden: true,
            items: [{
                    xtype: 'button',
                    id: "deleteButton" + this.heplmodeid + this.id,
                    disabled: true,
                    cls: 'setlocationwarehousebtn',
                    text: WtfGlobal.getLocaleText("Delete Selected"),
                    handler: this.deleteSelectedRecord.createDelegate(this)
                }]
        });
       
        if (!this.readOnly) {
            this.deleteSelectedPanel.show();
        }
        
        this.Grid.on("onselection", function() {
            if (this.Grid.sModel.getCount() >= 1 && !this.isLinkedTransaction) {
                if (Wtf.getCmp("deleteButton" + this.heplmodeid + this.id))
                    Wtf.getCmp("deleteButton" + this.heplmodeid + this.id).enable();
            } else {
                if (Wtf.getCmp("deleteButton" + this.heplmodeid + this.id))
                    Wtf.getCmp("deleteButton" + this.heplmodeid + this.id).disable();
            }
        }, this);
        
        var lastTransPanelId = "";
        if(this.quotation) {
            lastTransPanelId = "quotation";
        } else if(this.isOrder) {
            lastTransPanelId = this.isCustomer ? "salesorder" : "purchaseorder";
        } else if(this.cash){
            lastTransPanelId = this.isCustomer ? "cashsales": "cashpurchase";
        } else {
            lastTransPanelId = this.isCustomer ? "CInvoiceList": "VInvoiceList";
        }
        
        
        this.lastTransPanel = this.isCustomer ? getCustInvoiceTabView(false, lastTransPanelId, '', undefined, true) : getVendorInvoiceTabView(false, lastTransPanelId, '', undefined, true) ;
        
        this.NorthForm.doLayout();
        this.NorthForm.doLayout();
        this.southPanel.doLayout();
        this.POStore.on('load',function(){
            this.PO.setValue(this.PR_IDS);
            this.updateSubtotal();
            /*
            *	ERM-1037
            *	Send id of linked document to function to restrict linking of future dated document
            */
            if (!this.readOnly) {
                WtfGlobal.checkForFutureDate(this, this.PO.getValue().split(','));
            }
        },this)
        this.DueDate.on('blur',this.dueDateCheck,this);
        this.billDate.on('change',this.onDateChange,this);


        this.setTransactionNumber();
        WtfComMsgBox(29,4,true);
        this.isCustomer?chkcustaccload():chkvenaccload();
//        if(!this.isRequisition && !this.isEdit) {
            this.ajxUrl = "CommonFunctions/getInvoiceCreationJson.do";
            var params={
                transactiondate:transdate,
                loadtaxstore:true,
                moduleid :this.moduleid,
    //            loadpricestore: false,//!(this.isCustBill||this.isExpenseInv),
                loadcurrencystore:true,
                loadtermstore:true
    //            loadInventory:this.isCustomer
            }
            Wtf.Ajax.requestEx({url:this.ajxUrl,params:params}, this, this.successCallback, this.failureCallback);
//        } else {
//            this.hideLoading();
//        }
       this.currencyStore.on('load',this.changeTemplateSymbol,this);
       if(!this.isCustBill&&!this.isCustomer&&!this.isOrder&&!this.isEdit&&!this.copyInv){
           this.ProductGrid.on('pricestoreload',function(arr){//alert("1111"+arr.length)
               if(!this.isExpenseInv){
                    this.datechange=1;
                    this.changeCurrencyStore(arr);
               }
           },this);//.createDelegate(this)
       }else if(!this.isCustBill&&!this.isExpenseInv){//alert("2222"+arr.length)
           this.Grid.on('pricestoreload',function(arr){
                this.datechange=1;
                this.changeCurrencyStore(arr);
       }.createDelegate(this),this);}
    },   
    onDateChange:function(a,val,oldval){
        this.val=val;
        this.oldval=oldval;
        this.loadTax(val);
        /*
        *	ERM-1037
        *	On date change send id of linked document to function to restrict linking of future dated document
        */
        var selectedBillIds=this.PO.getValue().toString();
        if (selectedBillIds!= ""){
            var selectedValuesArr = selectedBillIds.split(',');
            WtfGlobal.checkForFutureDate(this,selectedValuesArr);
        }
        this.externalcurrencyrate=0;
        this.custdatechange=true;
        this.datechange=1;
        this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
        if(!(this.isCustBill||this.isExpenseInv)) {
            var affecteduser = this.Name.getValue();
//            this.Grid.loadPriceStoreOnly(val,this.Grid.priceStore, affecteduser);
//            this.Grid.setGridProductValues(true, false);
        }
            
        else{
            this.changeCurrencyStore();
            this.updateSubtotal();
            this.applyCurrencySymbol();
            
            var subtotal,discount,totalamount,tax,aftertaxamt,totalAmtInBase;
            if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                subtotal=discount=totalamount=tax=aftertaxamt=totalAmtInBase=Wtf.UpriceAndAmountDisplayValue;
            } else{
                subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
                discount=WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol)
                totalamount=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol)
                tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
                aftertaxamt=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax(),this.symbol);
                totalAmtInBase=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol());
            }
            
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:discount,totalamount:totalamount,tax:tax,aftertaxamt:aftertaxamt,totalAmtInBase:totalAmtInBase});
        }
   },
    loadTax:function(val){
        this.Grid.taxStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(val)}});
        this.Tax.setValue("");
        this.Grid.getStore().each(function(rec){
            rec.set('prtaxid','')
            rec.set('taxamount',0)
        },this);
    },
    successCallback:function(response){
        if(response.success){
            if(!this.isCustBill&&!this.isCustomer&&!this.isOrder&&!this.isEdit&&!this.copyInv){
                this.ProductGrid.taxStore.loadData(response.taxdata);
                this.ExpenseGrid.taxStore.loadData(response.taxdata);
            }
            else
                this.Grid.taxStore.loadData(response.taxdata);
            this.termds.loadData(response.termdata);
            this.currencyStore.loadData(response.currencydata);
//            if(!(this.isCustBill||this.isExpenseInv)){
//                this.Grid.priceStore.loadData(response.productdata);}
             if(this.currencyStore.getCount()<1){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.msg1")],2);
            }
            else{
                this.isCurrencyLoad=true;
                this.applyTemplate(this.currencyStore,0);
            }
            if(this.cash)
                this.Term.setValue(0);

            if((this.isEdit || this.copyInv) && this.record!=null) {
                this.Tax.setValue(this.record.data.taxid);
            }
//            if(this.isEdit)this.getTerm();
            if(this.isEdit || this.copyInv)this.loadRecord();
            
            if (this.isRFQ) {
                this.POStore.proxy.conn.url = "ACCPurchaseOrderCMN/getRequisitions.do";
                this.POStore.load({params: {isRFQ: true,excludePRUSedBefore:true,currencyid:this.currid,ID:this.PR_IDS != undefined ? this.PR_IDS.join(",") : ""},callback:this.populateCustomData,scope:this});
            }
            this.hideLoading();           
            this.loadDetailsGrid();
//            if(this.isEdit && !this.isOrder)
//                this.loadEditableGrid();
//            else if(this.isEdit && this.isOrder)
//            	this.loadEditableGridisOrder();
//            if(this.isEdit && this.isOrder && !this.isCustomer && (BCHLCompanyId.indexOf(companyid) != -1))
//            	this.loadOtherOrderdetails();
            
        }
    },

    failureCallback:function(response){
         this.hideLoading();
         WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg2")+response.msg], 2);
    },
    loadDetailsGrid:function(){
        if((this.isRequisition && (this.isEdit|| this.copyInv)) ||this.viewGoodReceipt || this.isFromWO || this.ifFromShortfall) {
            this.loadProductGridForRequisition();
        }
        if(this.isRFQ) {
//            this.Memo.setValue(this.PR_MEMOS);
            this.loadProductGridForRFQ();
        }
    },
    populateCustomData: function() {
        WtfGlobal.populateCustomData(this.PR_IDS,this.POStore,this.tagsFieldset)
    },
    hideLoading:function(){Wtf.MessageBox.hide();},
    applyTemplate:function(store,index){
        var editable=this.Currency.getValue()!=WtfGlobal.getCurrencyID()&&this.Currency.getValue()!=""//&&!this.isOrder;
        var exchangeRate = store.getAt(index).data['exchangerate'];
        if(this.externalcurrencyrate>0) {
            exchangeRate = this.externalcurrencyrate;
        } else if((this.isEdit|| this.copyInv) && this.record.data.externalcurrencyrate&&!this.custdatechange){
            var externalCurrencyRate = this.record.data.externalcurrencyrate-0;//??[PS]
            if(externalCurrencyRate>0){
                exchangeRate = externalCurrencyRate;
            }
        }
        var revExchangeRate = 1/(exchangeRate-0);
        revExchangeRate = (Math.round(revExchangeRate*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
        this.southCenterTplSummary.overwrite(this.southCenterTpl.body,{foreigncurrency:store.getAt(index).data['currencyname'],exchangerate:exchangeRate,basecurrency:WtfGlobal.getCurrencyName(),editable:editable,revexchangerate:revExchangeRate
            });
    },

    changeCurrencyStore:function(pronamearr){
        this.pronamearr=pronamearr;
        var currency=this.Currency.getValue();
        if(this.val=="")this.val=this.billDate.getValue();
//        if(currency!=""||this.custChange)
//            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.val),tocurrencyid:this.Currency.getValue()}});
//        else
            this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(this.val)}});
    },
    changeTemplateSymbol:function(){
        if(this.loadCurrFlag && Wtf.account.companyAccountPref.currencyid){
            this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
            this.loadCurrFlag = false;
        }
        
     /*if date of without inventory changes. price store will not be loaded in this case.[PS]*/
        if(this.isCustBill||this.isExpenseInv){
            if(this.currencyStore.getCount()==0){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                this.billDate.setValue("");
            }
            else
              this.updateFormCurrency();
        }


     /*if date of withinventory changes. After price store load. [PS]   */
//     alert(this.datechange+"---"+this.pronamearr.length)
        if(this.datechange==1){
            var str=""
            if(this.pronamearr!=undefined&&this.pronamearr.length>0){
                str+=this.pronamearr[0];
                for(var i=1;i<this.pronamearr.length;i++){
                    str+="</b>, <b>"+ this.pronamearr[i]
                }
            }
            var recResult=WtfGlobal.searchRecord(this.currencyStore,this.Currency.getValue(),"currencyid");
            if(this.Currency.getValue() !="" && recResult == null){
//            if(this.currencyStore.getCount()==0){
                    this.Currency.setValue("");
                    callCurrencyExchangeWindow();
                    str= WtfGlobal.getLocaleText("acc.field.andpriceof")+" <b>"+str+"</b>";
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleasesetthecurrencyrate")+str+WtfGlobal.getLocaleText("acc.field.fortheselecteddate")+"<b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                    this.billDate.setValue("");
                    //                if(this.oldval!=""||this.oldval!=undefined){
                    //                    if(!this.isCustBill)
                    //                        this.Grid.loadPriceStoreOnly(this.oldval,this.Grid.priceStore);
                    //                    this.Grid.taxStore.load({params:{transactiondate:this.oldval}});
                    //                }
            } else {
                    this.updateFormCurrency();
                    if(this.pronamearr!=undefined&&this.pronamearr.length>0){
                        str=WtfGlobal.getLocaleText("acc.field.priceof")+" <b>"+str+"</b>";
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Pleasesetthe")+" "+str+WtfGlobal.getLocaleText("acc.field.fortheselecteddate")+"<b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);}
                    this.Grid.pronamearr=[];
                    this.updateFormCurrency();
            }
            this.datechange=0;
            this.updateSubtotal();
            this.applyCurrencySymbol();
            
            var subtotal,discount,totalamount,tax,aftertaxamt,totalAmtInBase;
            if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                subtotal=discount=totalamount=tax=aftertaxamt=totalAmtInBase=Wtf.UpriceAndAmountDisplayValue;
            } else{
                subtotal=WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol)
                discount=WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol)
                totalamount=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol)
                tax=WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol);
                aftertaxamt=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax(),this.symbol);
                totalAmtInBase=WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol());
            }
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:subtotal,discount:discount,totalamount:totalamount,tax:tax,aftertaxamt:aftertaxamt,totalAmtInBase:totalAmtInBase});
        }
        
//        if(this.currencychanged){
//            if(this.currencyStore.getCount()<1){
//                    callCurrencyExchangeWindow();
//        
//                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+"<b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
//                    this.Currency.setValue("");
//            } else {
//                this.updateFormCurrency();
//            }
//            this.currencychanged = false;
//        }
        
    /*when customer/vendor name changes [PS]*/
        if(this.custChange){
            if(this.currencyStore.getCount()==0){
                callCurrencyExchangeWindow();
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.invoice.msg3")+" <b>"+WtfGlobal.convertToGenericDate(this.val)+"</b>"], 0);
                this.Name.setValue("");
        } else{this.Currency.setValue(this.currencyid)
                this.updateFormCurrency();}
            this.custChange=false;
        }
        this.Grid.pronamearr=[];
    },
    updateFormCurrency:function(){
       this.applyCurrencySymbol();
       if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
           this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:Wtf.UpriceAndAmountDisplayValue,discount:Wtf.UpriceAndAmountDisplayValue,totalamount:Wtf.UpriceAndAmountDisplayValue,tax:Wtf.UpriceAndAmountDisplayValue,aftertaxamt:Wtf.UpriceAndAmountDisplayValue,totalAmtInBase:Wtf.UpriceAndAmountDisplayValue});
       } else{
           this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol),aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol())});
       }       
    },

    getCurrencySymbol:function(){
        var index=null;
//        this.currencyStore.clearFilter(true); //ERP-9962
        var FIND = this.Currency.getValue();
        if(FIND == "" || FIND == undefined || FIND == null) {
            FIND = WtfGlobal.getCurrencyID();
        }
        index=this.currencyStore.findBy( function(rec){
             var parentname=rec.data['currencyid'];
            if(parentname==FIND)
                return true;
             else
                return false
            })
       this.currencyid=this.Currency.getValue();
       return index;
    },

    applyCurrencySymbol:function(){
        var index=this.getCurrencySymbol();
        var rate=this.externalcurrencyrate;
        if(index>=0){
           rate=(rate==""?this.currencyStore.getAt(index).data.exchangerate:rate);
            this.symbol=  this.currencyStore.getAt(index).data.symbol;
            this.Grid.setCurrencyid(this.currencyid,rate,this.symbol,index);
            this.applyTemplate(this.currencyStore,index);
       }
       return this.symbol;
    },
    
    loadProductGridForRFQ : function() {
    if(this.record){
        this.billid=this.record.data.billid;  
        this.Grid.getStore().proxy.conn.url ="ACCPurchaseOrderCMN/getRFQRows.do";
    }
    else{
        this.billid=this.PR_IDS;
        if(this.billid && this.isRFQ){
            WtfGlobal.populateCustomData(this.PR_IDS,this.POStore,this.tagsFieldset);
        }
         
        this.Grid.getStore().proxy.conn.url ="ACCPurchaseOrderCMN/getRequisitionRows.do";   
    }
        this.Grid.getStore().load({params:{bills:this.billid,isRFQ:true, currencyid:this.currid}});
    },

    addSelectedDocument:function(){
        var url = "ACCPurchaseOrderCMN/getRequisitions.do";
        this.showPONumbersGrid(url);
        this.loadProductGridForRFQ();
    },
    showPONumbersGrid: function (url) {
        this.PONumberSelectionWin = new Wtf.account.PONumberSelectionWindow({
            renderTo: document.body,
            height: 500,
            id: this.id + 'PONumbersSelectionWindowDO',
            width: 1200,
            title: WtfGlobal.getLocaleText("acc.gr.DocumentSelectionWindow"),
            layout: 'fit',
            modal: true,
            resizable: false,
            url: url,
            moduleid: this.moduleid,
            Currency:this.currid,
//            columnHeader:this.fromLinkCombo.getRawValue(),
            invoice: this,
            storeBaseParams: this.POStore.baseParams,
            storeParams:this.POStore.lastOptions.params,
            PORec: this.PORec
        });
        this.PONumberSelectionWin.show();
    },
    loadProductGridForRequisition : function() {
        this.billid=this.record.data.billid;
        this.productidstr="";
        if (this.isFromWO) {
            this.Grid.getStore().proxy.conn.url = "ACCWorkOrder/getWorkOrderComponentDetails.do";
            this.productidstr = this.record.data.productidstr
        }else if(this.ifFromShortfall){//load details of shortfall product
            this.Grid.getStore().proxy.conn.url = "ACCWorkOrder/getShortFallProductsDetails.do";
            this.productidstr = this.record.data.productidstr
        } else {
            this.Grid.getStore().proxy.conn.url = "ACCPurchaseOrderCMN/getRequisitionRows.do";
        }
        this.Grid.getStore().load({
            params:{
                bills:this.billid,
                productidstr:this.productidstr
            }
        });
    },
    loadEditableGrid:function(){
    this.StoreUrl = "";
        this.subGridStoreUrl = "";
        if (this.businessPerson=='Customer') {
            this.storeMode = this.isCustBill?16:12;
            this.StoreUrl = this.isCustBill?"ACCInvoiceCMN/getBillingInvoices.do":"ACCInvoiceCMN/getInvoices.do";
            this.subGridStoreUrl = this.isCustBill?"ACCInvoiceCMN/getBillingInvoiceRows.do":"ACCInvoiceCMN/getInvoiceRows.do";
        } else{
            this.storeMode = this.isCustBill?16:12;
            this.StoreUrl = this.isCustBill?"ACCGoodsReceiptCMN/getBillingGoodsReceipts.do":"ACCGoodsReceiptCMN/getGoodsReceipts.do";
            this.subGridStoreUrl = this.isCustBill?"ACCGoodsReceiptCMN/getBillingGoodsReceiptRows.do":"ACCGoodsReceiptCMN/getGoodsReceiptRows.do";
        }
        this.billid=this.record.data.billid;
        var mode=this.isCustBill?17:14;
        this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;
        this.Grid.getStore().load({params:{bills:this.billid,mode:mode}});
    },

    loadEditableGridisOrder:function(){
        this.subGridStoreUrl = "";
            if (!this.isCustomer) {
            	if(!this.isCustBill){
            		if(this.isPOfromSO){                         
                            this.subGridStoreUrl ="ACCSalesOrderCMN/getSalesOrderRows.do";     
                            this.Grid.soLinkFlag = true;
                        }else{
                            this.subGridStoreUrl ="ACCPurchaseOrderCMN/getPurchaseOrderRows.do";
                        }
            	}else{
            		this.subGridStoreUrl = "ACCPurchaseOrderCMN/getBillingPurchaseOrderRows.do";
            	}
            } else{
            	if(!this.isCustBill){
            		this.subGridStoreUrl = "ACCSalesOrderCMN/getSalesOrderRows.do";
            	}else{
            		this.subGridStoreUrl = "ACCSalesOrderCMN/getBillingSalesOrderRows.do";
            	}
            }
            this.billid=this.record.data.billid;
            this.Grid.getStore().proxy.conn.url = this.subGridStoreUrl;
            
            this.Grid.getStore().load({params:{bills:this.billid}});
            
    },
    loadOtherOrderdetails: function() {
        Wtf.Ajax.requestEx({
            url: "ACCPurchaseOrderCMN/getPurchaseOrderOtherDetails.do",
            params: {
                poid: this.record.data.billid
            }
        }, this, function(response) {
            if (response.data && response.data.length > 0) {
                this.youtReftxt.setValue(response.data[0].poyourref);
                this.delydatetxt.setValue(response.data[0].podelydate);
                this.delytermtxt.setValue(response.data[0].podelyterm);
                this.invoiceTotxt.setValue(response.data[0].poinvoiceto);
                this.depttxt.setValue(response.data[0].podept);
                this.projecttxt.setValue(response.data[0].poproject);
                this.requestortxt.setValue(response.data[0].porequestor);
                this.mernotxt.setValue(response.data[0].pomerno);
            }
        }, function(response) {
        });

    },
    addGrid:function(){
             this.ProductGrid=new Wtf.account.ProductDetailsGrid({
                height: 600,//region:'center',//Bug Fixed: 14871[SK]
                layout:'fit',
                title: WtfGlobal.getLocaleText("acc.invoice.inventory"),  //'Inventory',
                border:true,
                isRequisition:this.isRequisition,
                isRFQ : this.isRFQ,
                isQuotation:false,
                moduleid: this.moduleid,
                //cls:'gridFormat',
                helpedit:this.heplmodeid,
                id:this.id+"editproductdetailsgrid",
                viewConfig:{forceFit:true},
                isCustomer:this.isCustomer,
                currencyid:this.currencyid,
                fromOrder:true,
                isOrder:this.isOrder,
                forceFit:true,
                loadMask : true,
                parentObj :this,
                readOnly:this.readOnly,
                prComboId : this.PO.id,
                isEdit:this.isEdit
            });
        
        this.ProductGrid.on("productselect", this.loadTransStore, this);
        this.ProductGrid.on("productdeleted", this.removeTransStore, this);
           this.Grid=new Wtf.account.ProductDetailsGrid({
                height: 330,//region:'center',//Bug Fixed: 14871[SK]
                cls:'gridFormat',
                layout:'fit',
                parentCmpID:this.id,
                id:this.id+"editproductdetailsgrid",
                viewConfig:{forceFit:false},
                record:this.record,
                moduleid: this.moduleid,
                isRequisition:this.isRequisition,
                isRFQ : this.isRFQ,
                isQuotation:false,
                isCustomer:this.isCustomer,
                currencyid:this.currencyid,
                fromPO:this.isOrder,
                fromOrder:true,
                isOrder:this.isOrder,
                forceFit:false,
                editTransaction: this.isEdit,
                copyInv: this.copyInv,
                loadMask : true,
                parentObj :this,
                isLinkedTransaction:this.isLinkedTransaction,
                readOnly:this.readOnly,
                prComboId : this.PO.id,
                isEdit:this.isEdit
            });
        this.Grid.sModel.on("rowselect", this.setProductDetailsTplSummaryOptimised, this);
        this.Grid.sModel.on("rowdeselect", this.resetProductDetailsTplSummary, this);

        this.Grid.on("productselect", this.loadTransStore, this);
        this.Grid.on("productdeleted", this.removeTransStore, this); 

        this.Name.on('change',this.setVendorEmails,this)
        this.NorthForm.on('render',this.setDate,this);
         if(this.readOnly) {
             this.disabledbutton();  //  disabled button in view case
         }  
        this.Grid.on('datachanged',this.updateSubtotal,this);
        this.Grid.getStore().on('load',function(store){
            this.updateSubtotal();
            this.Grid.addBlank(store);
            if(this.isEdit|| this.copyInv){
                if(this.record.data.externalcurrencyrate!=undefined){
                    this.externalcurrencyrate=this.record.data.externalcurrencyrate;
                    this.updateFormCurrency();
                }
            }
            if (!this.readOnly) {
                WtfGlobal.checkForFutureDate(this, this.PO.getValue().split(','));
            }
        }.createDelegate(this),this);

    },
    disabledbutton:function(){
    this.Memo.setDisabled(true);
    this.fromPO.setDisabled(true);    
    this.fromLinkCombo.setDisabled(true);   
    this.PO.setDisabled(true);  
    this.sequenceFormatCombobox.setDisabled(true);  
    this.Number.setDisabled(true);  
    this.billDate.setDisabled(true);  
    this.Name.setDisabled(true);  
    this.PORefNo.setDisabled(true); 
    this.CostCenter.setDisabled(true); 
    this.Term.setDisabled(true); 
    this.DueDate.setDisabled(true); 
    this.Currency.setDisabled(true);  
    this.partialInvoiceCmb.setDisabled(true); 
    this.template.setDisabled(true); 
    this.users.setDisabled(true);      
},

disableComponent:function(){
    this.disabledbutton();
    if(this.saveBttn){
        this.saveBttn.disable();
    }
    if(this.savencreateBttn){
        this.savencreateBttn.disable();
    }
    if(Wtf.getCmp("posttext" + this.id)){
        Wtf.getCmp("posttext" + this.id).disable();
    }
    
    if(Wtf.getCmp("showaddress" + this.id)){
        Wtf.getCmp("showaddress" + this.id).disable(); 
    } 
       
    if(this.Grid){
        var GridStore = this.Grid.getStore();
        var count2 = GridStore.getCount();
        var lastRec2 = GridStore.getAt(count2-1);
        GridStore.remove(lastRec2);
    }
    if(this.GridPanel){
        if(this.modeName=="autocashpurchase" || this.modeName=="autogoodsreceipt"){
            this.ProductGrid.purgeListeners();
        }else{
            this.GridPanel.disable();   
        }

    }else{
        this.Grid.purgeListeners();
    }

    if(this.NorthForm){
        this.NorthForm.disable();
    }

//    if(this.southPanel){
//        this.southPanel.disable();
//    }
    if(this.SouthForm){
       this.SouthForm.disable(); 
    }
     if (Wtf.getCmp("deleteButton" + this.heplmodeid + this.id)){
          Wtf.getCmp("deleteButton" + this.heplmodeid + this.id).disable();
     }
},
    beforeTabChange:function(a,newTab,currentTab){
    	if(currentTab!=null && newTab!=currentTab){
             Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.savdat"),this.isExpenseInv?WtfGlobal.getLocaleText("acc.invoice.msg4"):WtfGlobal.getLocaleText("acc.invoice.msg5"),function(btn){ ///"Switching to "+(this.isExpenseInv?"Inventory":"Expense")+" section will empty the data filled so far in "+(this.isExpenseInv?"Expense":"Inventory")+" section. Do you wish to continue?",function(btn){
              if(btn=="yes") {
                a.suspendEvents();
                a.activate(newTab);
                this.Discount.setValue(0);
                this.isTaxable.setValue(false);
                this.Tax.setValue("");
                this.Tax.disable();
                a.resumeEvents();
                this.onGridChange(newTab,currentTab);
              }
             }.createDelegate(this),this)
            return false;
        }
        else{
            return true;
        }
   },

    onGridChange:function(newTab){
        this.Grid.getStore().removeAll();
        this.Grid.addBlankRow();
            this.Grid = newTab;
            this.Tax.store=this.Grid.taxStore;
            this.isExpenseInv=!this.isExpenseInv; //work fine in case of 2 tabs
//            if(!this.isCustBill&&!this.isExpenseInv&&this.Grid.priceStore.getCount()==0)
//                this.Grid.priceStore.load({params:{transactiondate:WtfGlobal.convertToGenericDate(this.billDate.getValue())}});
            this.applyCurrencySymbol();
            if(this.southCalTemp.body!=undefined){
                if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
                  this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:Wtf.UpriceAndAmountDisplayValue,discount:Wtf.UpriceAndAmountDisplayValue,totalamount:Wtf.UpriceAndAmountDisplayValue,tax:Wtf.UpriceAndAmountDisplayValue,aftertaxamt:Wtf.UpriceAndAmountDisplayValue,totalAmtInBase:Wtf.UpriceAndAmountDisplayValue});      
                } else{
                 this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol),aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol())});      
                }               
            }                 
    },
    caltax:function(){
        var totalamount=this.calTotalAmount();
        var rec= this.Grid.taxStore.getAt(this.Grid.taxStore.find('prtaxid',this.Tax.getValue()));
        var taxamount=(rec==null?0:(totalamount*rec.data["percent"])/100);
        return taxamount;
     },
    addAccount: function(store){
        callCOAWindow(false,null,"coaWin",this.isCustomer,false,false,false,false,false,true);
        Wtf.getCmp("coaWin").on('update',function(){store.reload();},this);
    },
        addOrder:function(){
        var tabid = "ordertab";
        if(this.isCustomer){
            if(this.isOrder){
                tabid = 'quotation';
                callQuotation(false, tabid);
            } else {
//                if(this.isCustBill) {
//                    tabid = "bsalesorder";
//                    callBillingSalesOrder(false,null,tabid);
//                } else 
                {
                    if(this.fromLinkCombo.getValue() == 1) {
                        callDeliveryOrder(false,null, "deliveryorder");
                    } else {
                        tabid = "salesorder";
                        callSalesOrder(false,null,tabid);
                    }
                   
                }
            }
        }else{
//            if(this.isCustBill) {
//                tabid = "bpurchaseorder";
//                callBillingPurchaseOrder(false,null,tabid);
//            } else 
            {
                if(this.isOrder){
                    tabid = "salesorder";
                    callSalesOrder(false,null,tabid);
                } else if(this.fromLinkCombo.getValue() == 1) {
                        callGoodsReceiptDelivery(false,null, "goodsreceiptdelivery");
                    } else {
                        tabid = "purchaseorder";
                        callPurchaseOrder(false,null,tabid);
                    }
            }
        }
        if(Wtf.getCmp(tabid)!=undefined) {
            Wtf.getCmp(tabid).on('update',function(){
                this.POStore.reload();
            },this);
        }
    },
    showGridTax:function(c,rec,val){
        var hide=(val==null||undefined?!rec.data['value']:val) ;
        var id=this.Grid.getId()
        var rowtaxindex=this.Grid.getColumnModel().findColumnIndex("prtaxid");
        var rowtaxamountindex=this.Grid.getColumnModel().findColumnIndex("taxamount");
        this.Grid.getColumnModel().setHidden( rowtaxindex,hide) ;
        this.Grid.getColumnModel().setHidden( rowtaxamountindex,hide) ;
        this.Grid.getStore().each(function(rec){
            if(this.includeProTax && this.includeProTax.getValue() == true
                && (rec.data.prtaxid == "" || rec.data.prtaxid == undefined)) {//In Edit, values are resetting after selection Product level Tax value as No
                if(this.ExpenseGrid && this.ExpenseGrid.isVisible()) {//(!this.isCustBill && !(this.isEdit && !this.isOrder) && !(this.isCustomer||this.isOrder))
                    var index=this.ExpenseGrid.accountStore.find('accountid',rec.data.accountid);
                    var taxid = index > 0 ? this.ExpenseGrid.accountStore.getAt(index).data["acctaxcode"]:"";
                    var taxamount = this.ExpenseGrid.setTaxAmountAfterSelection(rec);
                } else {
                    index=this.ProductGrid.productComboStore.find('productid',rec.data.productid);
                    var acctaxcode = (this.isCustomer)?"salesacctaxcode":"purchaseacctaxcode";
                    taxid = index > 0 ? this.ProductGrid.productComboStore.getAt(index).data[acctaxcode]:"";
                    taxamount = this.ProductGrid.setTaxAmountAfterSelection(rec);
                }

                rec.set('prtaxid',taxid);
                rec.set('taxamount',taxamount);
            } else {
                rec.set('prtaxid','')
                rec.set('taxamount',0)
            }
         },this);
//         if(hide)
             this.updateSubtotal();
    },
    
    showPartialDiscount : function(c,rec,val) {
        var hide=val;
        var id=this.Grid.getId();
        var rowindex=this.Grid.getColumnModel().findColumnIndex("partamount");

        this.Grid.getColumnModel().setHidden( rowindex,hide) ;
        this.Grid.getStore().each(function(rec){
            rec.set('partamount',0)
        },this);
        this.updateSubtotal();
    },
    
     enableNumber:function(c,rec){
        this.loadStore();
        this.fromLinkCombo.enable();
        this.fromPO.setValue(true);
        if(rec.data['value']==0){                        
            this.fromLinkCombo.setValue(0);
            this.POStore.proxy.conn.url = this.isCustomer ? "ACCSalesOrderCMN/getSalesOrders.do" : "ACCPurchaseOrderCMN/getPurchaseOrders.do";
            this.POStore.load();        
            this.PO.enable();
            if(this.partialInvoiceCmb){
                this.partialInvoiceCmb.enable();
            }
        }
        else if(rec.data['value']==1){        
            this.fromLinkCombo.setValue(1);
            this.POStore.proxy.conn.url = this.isCustomer ? "ACCInvoiceCMN/getDeliveryOrdersMerged.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrdersMerged.do";
            this.POStore.load({params:{nondeleted:true}});        
            this.PO.enable(); 
            if(this.partialInvoiceCmb){
                this.partialInvoiceCmb.disable();
                var id=this.Grid.getId();
                var rowindex=this.Grid.getColumnModel().findColumnIndex("partamount");
                this.Grid.getColumnModel().setHidden( rowindex,true) ;
            }
        }        
    },
    
    enablePO:function(c,rec){
        if(rec.data['value']==true){
            if(!this.isCustBill&&!this.isCustomer&&!this.isEdit&&!this.copyInv&&!(this.isOrder&&(!this.isCustomer))){//this.isExpenseInv=false;
                this.GridPanel.setActiveTab(this.ProductGrid);
                this.ExpenseGrid.disable();

            }
            if(!this.isCustBill && !this.isOrder && !this.cash){
                this.fromLinkCombo.enable();
            }else{
                this.POStore.load();
                this.PO.enable();
            }                                                      
            this.fromOrder=true;
        }
        else{
            if(!this.isCustBill && !this.isOrder && !this.cash && this.isCustomer)
            {
                this.fromLinkCombo.disable();
                this.PO.disable();
            }    
            this.loadStore();
            if(!this.isCustBill&&!this.isCustomer&&!this.isEdit&&!this.copyInv){//this.isExpenseInv=false;
                this.ExpenseGrid.enable();
            }    
            this.setDate();
            
            if(this.partialInvoiceCmb){
                this.partialInvoiceCmb.disable();
                var id=this.Grid.getId();
                var rowindex=this.Grid.getColumnModel().findColumnIndex("partamount");
                this.Grid.getColumnModel().setHidden( rowindex,true) ;
            }
        }
        this.currencyStore.load(); 	       // Currency id issue 20018
    },
    enabletax:function(c,rec){
        if(rec.data['value']==true)
           this.Tax.enable();
        else{
            this.Tax.disable();
            this.Tax.setValue("");
        }
        this.updateSubtotal();
    },
/*
 *A function to fill grid with selected PR Numbers
 **/    
populateGridData:function(c,rec){
    var billid=this.PO.getValue();
    if(billid.search(",") >=1){
        rec=this.PO.getValue();
    }else{
        rec=this.POStore.getAt(this.POStore.find('billid',billid));        
        rec=rec.data['billid'];
    }  
    var soLinkFlag = false;
    this.Grid.getStore().proxy.conn.url = 'ACCPurchaseOrderCMN/getRequisitionRows.do';
    this.Grid.loadPOGridStore(rec, soLinkFlag);  
},
    populateData:function(c,rec) {
        this.Grid.fromPO=true;        
       // if(this.isOrder && this.isCustomer && !this.isCustBill){//Temporary check to hide/display product tax for order. Need to fix for Invoices also
            if(rec.data["includeprotax"]){
                this.includeProTax.setValue(true);
                this.showGridTax(null,null,false);
            } else {
                this.includeProTax.setValue(false);
                this.showGridTax(null,null,true);
            }
//        } else {
//            this.includeProTax.setValue(true);
//            this.showGridTax(null,null,false);
//        }
        if(!this.isCustBill && !this.isOrder && !this.cash && this.isCustomer){
            if(this.fromLinkCombo.getValue()==1){
                this.includeProTax.setValue(false);
                this.showGridTax(null,null,true);            
            }
        }        
        this.Memo.setValue(rec.data['memo']);
        this.Name.setValue(rec.data['personid']);
        this.loadTransStore();
        if(rec.data['taxid']!="" && rec.data["taxid"] != "None"){
            this.Tax.enable();
            this.isTaxable.setValue(true);
            this.Tax.setValue(rec.data['taxid']);
        }else{
            this.Tax.disable();
            this.isTaxable.reset();
            this.Tax.reset();
        }
        this.getCreditTo(rec.data.creditoraccount);
        if(this.fromLinkCombo.getValue()==1){
            this.updateData();               
        }else{
            this.Currency.setValue(rec.data['currencyid']);
        }     
        var perstore=this.isCustomer? Wtf.customerAccStore:Wtf.vendorAccStore
        var index = perstore.find('accid',rec.data['personid']);
        if(index != -1){
            var storerec=perstore.getAt(index);
            if(!(this.quotation || !this.isCustomer || this.isOrder)){
                if(this.Name.getValue()!=""){
                  this.billingAddrsStore.load({params:{customerid:this.Name.getValue()}});
                  this.ShippingAddrsStore.load({params:{customerid:this.Name.getValue()}});         
                }                
            }
            this.billTo.setValue(storerec.data['billto']);
            this.Term.setValue(storerec.data['termdays']);
        }        
        this.CostCenter.setValue(rec.data.costcenterid);
        this.updateDueDate();
        var url = "";
		//(this.isCustBill?53:43)
        var soLinkFlag = false;        
        if(!this.isCustBill && !this.isOrder && !this.cash ){
            if(this.fromLinkCombo.getValue()==0){
                url = this.isCustomer ? 'ACCSalesOrderCMN/getSalesOrderRows.do' : 'ACCPurchaseOrderCMN/getPurchaseOrderRows.do';
            } else if(this.fromLinkCombo.getValue()==1){
                url = this.isCustomer ? "ACCInvoiceCMN/getDeliveryOrderRows.do" : "ACCGoodsReceiptCMN/getGoodsReceiptOrderRows.do";
            }
        }
        else{
            if(this.isCustomer){
                if(this.isOrder){
                    url = "ACCSalesOrderCMN/getQuotationRows.do";
                } else {
                    url = this.isCustBill?"ACCSalesOrderCMN/getBillingSalesOrderRows.do":'ACCSalesOrderCMN/getSalesOrderRows.do';
                }
            } else {
                if(this.isOrder){
                    url = this.isCustBill?"ACCSalesOrderCMN/getBillingSalesOrderRows.do":'ACCSalesOrderCMN/getSalesOrderRows.do';
                    soLinkFlag = true;
                } else {
                    url = this.isCustBill?"ACCPurchaseOrderCMN/getBillingPurchaseOrderRows.do":'ACCPurchaseOrderCMN/getPurchaseOrderRows.do';
                }
            }
        }                
	this.Grid.getStore().proxy.conn.url = url;
        this.Grid.loadPOGridStore(rec, soLinkFlag);          
    },

    setVendorEmails:function(c,rec,ind){
        var vendorIds=this.Name.getValue();
        if (this.moduleid == Wtf.Acc_RFQ_ModuleId) {
            this.tagsFieldset.resetCustomComponents();
            var moduleid = this.isCustomer ? Wtf.Acc_Customer_ModuleId : Wtf.Acc_Vendor_ModuleId;
            this.tagsFieldset.setValuesForCustomer(moduleid, vendorIds);
        }
        var billingEmail="";
        var selectedValuesArr = vendorIds.split(',');
        if (selectedValuesArr.length > 0) {

            for (var i = 0; i < selectedValuesArr.length; i++) {

                var record = WtfGlobal.searchRecord(this.Name.store, selectedValuesArr[i], "accid");
                if (record != null && record != undefined && record.data != undefined && record.data.billingEmail != undefined && record.data.billingEmail != "") {
                    if (i == selectedValuesArr.length - 1) {
                        billingEmail = billingEmail + record.data.billingEmail;

                    } else {

                        billingEmail = billingEmail + record.data.billingEmail + ",";
                }

            }

            }
            this.VendorEmail.setValue(billingEmail);

        } else {
            this.VendorEmail.setValue("");
        }
    },
    updateSubtotal:function(a,val){
        if(this.calDiscount())return;
        this.isClosable=false; // Set Closable flag after updating grid data
        this.applyCurrencySymbol();
        if((this.isCustomer && !Wtf.dispalyUnitPriceAmountInSales) || (!this.isCustomer && !Wtf.dispalyUnitPriceAmountInPurchase)) {
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:Wtf.UpriceAndAmountDisplayValue,discount:Wtf.UpriceAndAmountDisplayValue,totalamount:Wtf.UpriceAndAmountDisplayValue,tax:Wtf.UpriceAndAmountDisplayValue,aftertaxamt:Wtf.UpriceAndAmountDisplayValue,totalAmtInBase:Wtf.UpriceAndAmountDisplayValue});
        } else {
            this.tplSummary.overwrite(this.southCalTemp.body,{subtotal:WtfGlobal.addCurrencySymbolOnly(this.Grid.calSubtotal(),this.symbol),discount:WtfGlobal.addCurrencySymbolOnly(this.getDiscount(),this.symbol),totalamount:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount(),this.symbol),tax:WtfGlobal.addCurrencySymbolOnly(this.caltax(),this.symbol),aftertaxamt:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmount()+this.caltax(),this.symbol),totalAmtInBase:WtfGlobal.addCurrencySymbolOnly(this.calTotalAmountInBase(),WtfGlobal.getCurrencySymbol())});
        }                
    },
    getDiscount:function(){
        var disc=this.Discount.getValue();
        var per=this.perDiscount.getValue();
        return isNaN(parseFloat(disc))?0:(per?(disc*this.Grid.calSubtotal())/100:disc);
    },
    calDiscount:function(){
        var disc=this.Discount.getValue();
        var per=this.perDiscount.getValue();
        if(per && disc > 100){
            WtfComMsgBox(28,2);
            this.NorthForm.getForm().setValues({perdiscount:false});
            return true;
        }
        else
            return false;
    },
    calTotalAmount:function(){
        var subtotal=this.Grid.calSubtotal();
        var discount=this.getDiscount();
        return subtotal-discount;
    },
    calTotalAmountInBase:function(){
        var subtotal=this.Grid.calSubtotal(); 
        var discount=this.getDiscount();   
        var taxVal = this.caltax();
        var returnValInOriginalCurr = subtotal-discount+taxVal;
        returnValInOriginalCurr = returnValInOriginalCurr*this.getExchangeRate();
        return returnValInOriginalCurr; 
    },
    getExchangeRate:function(){
        var index=this.getCurrencySymbol();
        var revExchangeRate = 0;
        if(index>=0){
            var exchangeRate = this.currencyStore.getAt(index).data['exchangerate'];
            if(this.externalcurrencyrate>0) {
                exchangeRate = this.externalcurrencyrate;
            }
            this.externalcurrencyrate = exchangeRate;
            revExchangeRate = 1/(exchangeRate);
            revExchangeRate = (Math.round(revExchangeRate*Wtf.Round_Off_Number))/Wtf.Round_Off_Number;
        }
        return revExchangeRate;
    },
    save:function(){
       var incash=false;
        this.Number.setValue(this.Number.getValue().trim());
        this.billTo.setValue(this.billTo.getValue().trim());
        if(!(this.quotation || !this.isCustomer || this.isOrder)){
            this.shipTo.setValue(this.shipTo.getValue().trim());
        }        
        var isValidCustomFields=this.tagsFieldset.checkMendatoryCombo(); // checking mandatory items in custom fields
        if(this.NorthForm.getForm().isValid() && isValidCustomFields){
            if(this.isCustBill){
                for(var datacount=0;datacount<this.Grid.getStore().getCount();datacount++){
                    var creditoracc=this.Grid.getStore().getAt(datacount).data['creditoraccount'];                    
                    if(creditoracc==undefined||creditoracc==""){
                        if(this.Grid.getStore().getAt(datacount).data['productdetail'].length>0){
                            var account=(this.isCustomer)?"Credit account":"Debit account";
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.fxexposure.warning"),WtfGlobal.getLocaleText("acc.field.Pleaseselect")+account], 2);
                            return;
                        } 
                    }            
                }
            }
            // Checking for deactivated products
            var inValidProducts=this.checkForDeActivatedProductsAdded();
            if(inValidProducts!=''){
                inValidProducts = inValidProducts.substring(0, inValidProducts.length-2);
                Wtf.MessageBox.show({
                    title: WtfGlobal.getLocaleText("acc.common.warning"), 
                    msg: WtfGlobal.getLocaleText("acc.common.followingProductsAreDeactivated")+'</br>'+'<b>'+inValidProducts+'<b>',
                    buttons: Wtf.MessageBox.OK,
                    icon: Wtf.MessageBox.WARNING,
                    scope: this,
                    scopeObj :this,
                    fn: function(btn){
                        if(btn=="ok"){
                            return;
                        }
                    }
                });
                return;
            }
            for(var i=0;i<this.Grid.getStore().getCount()-1;i++){// excluding last row
                var quantity=this.Grid.getStore().getAt(i).data['quantity'];
                if(quantity==""||quantity==undefined||quantity<=0){
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.QuantityforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.shouldbegreaterthanZero")], 2);
                    return;
                }
                var rate=this.Grid.getStore().getAt(i).data['rate'];
                if((rate===""||rate==undefined||rate<0)&&!this.isRFQ){
                    var rateIndex = this.Grid.getColumnModel().findColumnIndex("rate");
                    if (this.Grid.getColumnModel().config[rateIndex] != undefined && this.Grid.getColumnModel().config[rateIndex] != null && this.Grid.getColumnModel().config[rateIndex] != "") {
                        if (!this.Grid.getColumnModel().config[rateIndex].hidden) {
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.field.RateforProduct")+" "+this.Grid.getStore().getAt(i).data['productname']+" "+WtfGlobal.getLocaleText("acc.field.cannotbeempty")], 2);
                            return;
                        }
                    }
                }
            }
            
            var count=this.Grid.getStore().getCount();
            if(count<=1){
                WtfComMsgBox(33, 2);
                return;
            }                        
            if(this.getDiscount()>this.Grid.calSubtotal()){
                WtfComMsgBox(12, 2);
                return;
            }
//            var datediff=new Date(this.billDate.getValue()).getElapsed(this.DueDate.getValue());
//            if(datediff==0)
//                  incash=true;
//              else
                  incash=this.cash;
            var rec=this.NorthForm.getForm().getValues();
            rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
            var custFieldArr=this.tagsFieldset.createFieldValuesArray();
            if (custFieldArr.length > 0)
                rec.customfield = JSON.stringify(custFieldArr);
                this.ajxurl = "";
            if(this.isRequisition)                
                this.ajxurl = "ACCPurchaseOrder/saveRequisition.do";
            else 
                this.ajxurl = "ACCPurchaseOrder/saveRFQ.do";
            var currencychange=this.Currency.getValue()!=WtfGlobal.getCurrencyID()&&this.Currency.getValue()!=""&&!this.isOrder;
            var msg=currencychange?WtfGlobal.getLocaleText("acc.field.Currencyrateyouhaveappliedcannotbechanged"):"";           
            var detail = this.Grid.getProductDetails();
            var validLineItem = this.Grid.checkDetails(this.Grid);
            if (validLineItem != "" && validLineItem != undefined) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), (WtfGlobal.getLocaleText("acc.msgbox.lineitem") + validLineItem)], 2);             
                return;
            }
            if(detail == undefined || detail == "[]"){
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"),WtfGlobal.getLocaleText("acc.invoice.msg12")],2);   //"Product(s) details are not valid."
                return;
            }
             if(this.productOptimizedFlag==Wtf.Products_on_Submit ){
                     this.checklastproduct(rec,detail,incash,count);
                }else{
                     this.showConfirmAndSave(rec,detail,incash);
                }
           
        }else{
            WtfComMsgBox(2, 2);
        }
    },   
    checklastproduct:function(rec,detail,incash,count){
    if(this.Grid.getStore().getAt(count-1).data['pid']!="" && this.Grid.getStore().getAt(count-1).data['productid']==""){
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),WtfGlobal.getLocaleText("acc.common.productWithSpecifiedId")+" "+this.Grid.getStore().getAt(count-1).data['pid']+" "+WtfGlobal.getLocaleText("acc.common.productDoesNotExistsOrInDormantState")+". "+WtfGlobal.getLocaleText("acc.accPref.productnotFoundonSave")+'</center>' ,function(btn){
            if(btn=="yes") {
                 this.showConfirmAndSave(rec,detail,incash);
            }else{
                return;
            } 
        },this);                
    }else{
        this.showConfirmAndSave(rec,detail,incash);
    } 
         
},
    checkLimit:function(rec,detail,incash){
        if(!this.quotation && !this.isOrder && !this.cash){
            if(rec!=null&&rec!=undefined&&this.calTotalAmount()!=null)
            {
                rec.totalSUM=this.calTotalAmount()+this.caltax();
                Wtf.Ajax.requestEx({
                    url:"ACC"+this.businessPerson+"CMN/get"+this.businessPerson+"Exceeding"+(this.businessPerson=="Vendor"?"Debit":"Credit")+"Limit.do",
                    params:rec                                                                                                                                            
                },this,function(response){
                    if(response.data && response.data.length > 0){
                        var msg = (this.businessPerson=="Vendor"?"<center>"+WtfGlobal.getLocaleText("acc.cust.debitLimit"):"<center>"+WtfGlobal.getLocaleText("acc.cust.creditLimit"))+" "+WtfGlobal.getLocaleText("acc.field.forthis")+this.businessPerson+" "+WtfGlobal.getLocaleText("acc.field.hasreached")+"</center>"+"<br><br>";
                        var limitMsg = "";
                        for(var i=0; i< response.data.length; i++){
                            var recTemp = response.data[i];
                            limitMsg = (recTemp.name == "" ? "" : "<b>"+this.businessPerson+": </b>" + recTemp.name + ", ") +"<b>"+WtfGlobal.getLocaleText("acc.field.AmountDue1")+" </b>" + recTemp.amountDue + ", <b>"+(this.businessPerson=="Vendor"?WtfGlobal.getLocaleText("acc.cust.debitLimit"):WtfGlobal.getLocaleText("acc.cust.creditLimit"))+": </b>" + recTemp.limit;
                            msg += limitMsg + "<br>";
                        }
                        msg += "<br>"+WtfGlobal.getLocaleText("acc.field.Doyouwishtoproceed")+"</center>";
                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"),msg,function(btn){
                            if(btn!="yes") {
                                return;
                            }
                            this.showConfirmAndSave(rec,detail,incash);
                        },this);
                    }else{
                        this.showConfirmAndSave(rec,detail,incash);
                    }
                },function(response){
                    this.showConfirmAndSave(rec,detail,incash);
                });                
            }else{
                this.showConfirmAndSave(rec,detail,incash);
            }   
        }else{
            this.showConfirmAndSave(rec,detail,incash);
        }
    },
    
    showConfirmAndSave: function(rec,detail,incash) {
        var isRuleExist = false;
        Wtf.Ajax.requestEx({
            url: "ACCPurchaseOrderCMN/isRuleExistsForRequisition.do",
            params: {
                totalAmount: this.calTotalAmount()
            }
        }, this, function(response) {
            if (response.success) {
                isRuleExist = response.isRuleExist;
                if (isRuleExist) {
                    this.finallySave(rec,detail,incash);
                } else {
                    this.checkForBudgetLimitActivated(rec,detail,incash);
                }
            }
        }, this.genFailureResponse);
    },

    getDates:function(start){
        var d=new Date();
        var monthDateStr=d.format('M d');
        if(Wtf.account.companyAccountPref.fyfrom)
            monthDateStr=Wtf.account.companyAccountPref.fyfrom.format('M d');
        var fd=new Date(monthDateStr+', '+d.getFullYear()+' 12:00:00 AM');
        if(d<fd)
            fd=new Date(monthDateStr+', '+(d.getFullYear()-1)+' 12:00:00 AM');
        if(start)
            return fd;

        return fd.add(Date.YEAR, 1).add(Date.DAY, -1);
    },

    loadTransStore : function(productid){
        if(this.Name.getValue() != ""){
            var customer= (this.businessPerson=="Vendor")? "" : this.Name.getValue();
            var vendor= (this.businessPerson=="Vendor")? this.Name.getValue() : "" ;
            if((productid == undefined || productid == "") && this.Grid.getStore().getCount() > 0){
                productid = this.Grid.getStore().getAt(0).get("productid");
            }
            if(productid) {
                this.lastTransPanel.productid = productid;
                this.lastTransPanel.Store.load({
                    params:{
                        start:0,
                        limit:5, 
                        prodfiltercustid:customer,
                        prodfilterventid:vendor,
                        productid : productid
                    }
                });
            }
        }
    },
    
    removeTransStore : function(){
        this.lastTransPanel.Store.removeAll();
    },
    
    updateData:function(){
        var customer= this.Name.getValue();
        if(this.Grid) {
            this.Grid.affecteduser = this.Name.getValue();
        }
        
        this.loadTransStore();
        if(!(this.isCustBill||this.isExpenseInv|| this.isEdit || this.copyInv)) {
            if(!(this.fromPO && this.fromPO.getValue())){
                var val = this.billDate.getValue();
//                this.Grid.loadPriceStoreOnly(val,this.Grid.priceStore, customer);
            }
            
        }
        if(!(this.quotation || !this.isCustomer || this.isOrder)){
            this.billingAddrsStore.load({params:{customerid:customer}});
            this.ShippingAddrsStore.load({params:{customerid:customer}});       
        }
        Wtf.Ajax.requestEx({
            url:"ACC"+this.businessPerson+"/getAddress.do",
//            url:Wtf.req.account+this.businessPerson+'Manager.jsp',
            params:{
                mode:4,
                customerid:customer,
                isBilling : this.isCustBill
            }
        }, this,this.setAddress);       
    },
    
    setAddress:function(response){
        if(response.success){
            this.externalcurrencyrate=0;
            this.custdatechange=true;
            if(!(this.quotation || !this.isCustomer || this.isOrder)){                
            }else{
                this.billTo.setValue(response.billingAddress);
            }           
            this.Currency.setValue(response.currencyid);
            this.currencyid=response.currencyid;
            this.symbol = response.currencysymbol;
            var taxid = response.taxid
            if(taxid != undefined && taxid != ""){
                this.isTaxable.setValue(true);
                this.Tax.enable();
                this.Tax.setValue(taxid);
            } else {
                this.isTaxable.setValue(false);
                this.Tax.setValue('');
                this.Tax.disable();
            }
            this.custChange=true;
            this.changeCurrencyStore();

            if(this.fromPO)					// Currency id issue 20018
            	this.currencyStore.load();
        }
    },
    getTerm:function(val1,val2){
        val1=new Date(this.record.data.date);
        val2=new Date(this.record.data.duedate);
        var msPerDay = 24 * 60 * 60 * 1000
        var termdays = Math.floor((val2-val1)/ msPerDay) ;
        var FIND =termdays;
        var index=this.termds.findBy( function(rec){
             var parentname=rec.data.termdays;
            if(parentname==FIND)
                return true;
             else
                return false
            })
            if(index>=0){
                var  rec=this.termds.getAt(index)
                this.Term.setValue(rec.data.termdays);
            }
    },
    getCreditTo:function(val){
        var index=this.allAccountStore.findBy( function(rec){
             var name=rec.data.accid;
            if(name==val)
                return true;
             else
                return false
            },this)
            if(index>=0)
                this.creditTo.setValue(val);
    },
    updateDueDate:function(a,val){
        var term=null;
        if(this.Term.getValue()!=""&&isNaN(this.Term.getValue())==false){

            term=new Date(this.billDate.getValue()).add(Date.DAY, this.Term.getValue());}
        else
            term=this.billDate.getValue();
        this.NorthForm.getForm().setValues({duedate:term});
        if(this.Grid){
            this.Grid.billDate = this.billDate.getValue()
        }
    },
  
        genSuccessResponse:function(response){
        if(response.success){
           if((this.moduleid == Wtf.Acc_Purchase_Requisition_ModuleId) || (this.moduleid == Wtf.Acc_RFQ_ModuleId)){
               var scope=this;
                if (Wtf.getCmp("PurchaseRequisitionList") != undefined) {
                    Wtf.getCmp("PurchaseRequisitionList").Store.on('load', function() {
                        WtfComMsgBox([scope.titlel, response.msg], response.success * 2 + 2);
                    }, Wtf.getCmp("PurchaseRequisitionList").Store, {
                        single: true
                    });
                } else if (Wtf.getCmp("RequestForQuotation") != undefined) {
                    Wtf.getCmp("RequestForQuotation").Store.on('load', function() {
                        WtfComMsgBox([scope.titlel, response.msg], response.success * 2 + 2);
                    }, Wtf.getCmp("RequestForQuotation").Store, {
                        single: true
                    });
                } else {
                    WtfComMsgBox([scope.titlel, response.msg], response.success * 2 + 2);
                }
                if(this.moduleid==Wtf.Acc_Purchase_Requisition_ModuleId && Wtf.getCmp("draftedPurchaseRequisitionList") != undefined && Wtf.isAutoRefershReportonDocumentSave){
                    Wtf.getCmp("draftedPurchaseRequisitionList").Store.load({
                        params: {
                            start: 0,
                            limit: Wtf.getCmp("draftedPurchaseRequisitionList").pP.combo!= undefined ? Wtf.getCmp("draftedPurchaseRequisitionList").pP.combo.value : 30
                        }
                    });
                }
          }else{
            WtfComMsgBox([this.titlel,response.msg],response.success*2+2);
          }
             this.mailFlag=true;//This flag is used in Wtf.account.ClosablePanel component .mailFlag shows that at the time of creation of PR if we press sav button then whole component will be disabled and at close action of that tab no msg will be displayed.
            if(!(this.isEdit || this.copyInv || this.isFromWO || this.ifFromShortfall)){
                this.lastTransPanel.Store.removeAll();
                this.symbol = WtfGlobal.getCurrencySymbol();
                this.currencyid = WtfGlobal.getCurrencyID();
                this.loadStore();
                this.currencyStore.load(); 
                this.externalcurrencyrate=0; //Reset external exchange rate for new Transaction.
                this.isClosable= true;       //Reset Closable flag to avoid unsaved Message.
                Wtf.dirtyStore.product = true;
                this.isTaxable.setValue(false);
                this.Tax.setValue('');
                this.Tax.disable();

                var customFieldArray = this.tagsFieldset.customFieldArray;  //Reset Custom Fields
                for (var itemcnt = 0; itemcnt < customFieldArray.length; itemcnt++) {
                    var fieldId = customFieldArray[itemcnt].id
                    if (Wtf.getCmp(fieldId) != undefined && customFieldArray[itemcnt].getXType()!='fieldset') {
                        Wtf.getCmp(fieldId).reset();
                    }
                }    
                var checkListCheckBoxesArray = this.tagsFieldset.checkListCheckBoxesArray;  //Reset Check List
                for (var checkitemcnt = 0; checkitemcnt < checkListCheckBoxesArray.length; checkitemcnt++) {
                    var checkfieldId = checkListCheckBoxesArray[checkitemcnt].id
                    if (Wtf.getCmp(checkfieldId) != undefined) {
                        Wtf.getCmp(checkfieldId).reset();
                    }
                } 
                var customDimensionArray = this.tagsFieldset.dimensionFieldArray;  //Reset Custom Dimension
                for (var itemcnt1 = 0; itemcnt1 < customDimensionArray.length; itemcnt1++) {
                    var fieldId1 = customDimensionArray[itemcnt1].id
                    if (Wtf.getCmp(fieldId1) != undefined) {
                        Wtf.getCmp(fieldId1).reset();
                    }
                }     
            }else{
                this.disableComponent();
            }
            //The position of this code is important ERP-34986
        if(this.productDetailsTplSummary && this.Grid.getSelectionModel().getSelections().length == 0){
            this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body, {
                productname: "&nbsp;&nbsp;&nbsp;&nbsp;", 
                productid: 0, 
                qty: 0, 
                soqty: 0, 
                poqty: 0,
                blockqty:0,
                salableStock:0,
                reserveStock:0
            });
        }
                this.fireEvent('update',this);
                if(this.isRFQ){
                    if(Wtf.getCmp("RequestForQuotation")!=null && Wtf.getCmp("RequestForQuotation")!=undefined){//to refresh the grid of PRReport
                        Wtf.getCmp("RequestForQuotation").Store.reload();//for submit
                    }
                }
                if(Wtf.getCmp("PurchaseRequisitionList")!=null && Wtf.getCmp("PurchaseRequisitionList")!=undefined){//to refresh the grid of PRReport
                    Wtf.getCmp("PurchaseRequisitionList").Store.reload();//for save as draft
                    Wtf.getCmp("PurchaseRequisitionListPending").Store.reload();//for submit
                }

        }else if (response.isDuplicateExe) {
            Wtf.MessageBox.hide();
            var label="";
            switch (this.moduleid){
                case Wtf.Acc_Purchase_Requisition_ModuleId:
                    label =WtfGlobal.getLocaleText("acc.requisition.newrequisitionno");
                    break;
                case Wtf.Acc_RFQ_ModuleId:
                    label =WtfGlobal.getLocaleText("acc.RFQ.newrfq");
                    break;
            }
            this.newnowin = new Wtf.Window({
                title: WtfGlobal.getLocaleText("acc.common.success"),
                closable: true,
                iconCls: getButtonIconCls(Wtf.etype.deskera),
                width: 330,
                autoHeight: true,
                modal: true,
                bodyStyle: "background-color:#f1f1f1;",
                closable:false,
                        buttonAlign: 'right',
                items: [new Wtf.Panel({
                        border: false,
                        html: (response.msg.length > 60) ? response.msg : "<br>" + response.msg,
                        height: 50,
                        bodyStyle: "background-color:white; padding: 7px; font-size: 11px; border-bottom: 1px solid #bfbfbf;"
                    }),
                    this.newdoForm = new Wtf.form.FormPanel({
                        labelWidth: 190,
                        border: false,
                        autoHeight: true,
                        bodyStyle: 'padding:10px 5px 3px; ',
                        autoWidth: true,
                        defaultType: 'textfield',
                        items: [this.newdono = new Wtf.form.TextField({
                                fieldLabel: label,
                                allowBlank: false,
                                labelSeparator: '',
                                width: 90,
                                itemCls: 'nextlinetextfield',
                                name: 'newdono',
                                id: 'newdono'
                            })],
                        buttons: [{
                                text: WtfGlobal.getLocaleText("acc.common.saveBtn"),
                                handler: function () {
                                    if (this.newdono.validate()) {
                                        this.Number.setValue(this.newdono.getValue());
                                        this.save();
                                        this.newnowin.close();
        }
    },
                                scope: this
                            }, {
                                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //"Cancel",
                                scope: this,
                                handler: function () {
                                    this.newnowin.close();
                                }
                            }]
                    })]
            });
            this.newnowin.show();
        }else{
            WtfComMsgBox([this.titlel,response.msg],response.success*2+2);
        }
    },

    genFailureResponse:function(response){
        Wtf.MessageBox.hide();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");//"Failed to make connection with Web Server";
        if(response.msg)msg=response.msg;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },

    loadStore:function(){
//        if(!(this.isCustBill||this.isExpenseInv))
//            this.Grid.priceStore.purgeListeners();
        var formatid = this.sequenceFormatCombobox.getValue();    
        this.Grid.getStore().removeAll();
        this.setTransactionNumber();
        this.PO.setDisabled(true);
        this.fromLinkCombo.setDisabled(true);
        this.NorthForm.getForm().reset();
        this.Currency.setValue(Wtf.account.companyAccountPref.currencyid);
        if(this.fromPO){
            this.PO.enable();
            this.fromPO.enable();
        }
        if(this.fromLinkCombo){
            this.fromLinkCombo.setDisabled(true);
            this.fromLinkCombo.clearValue();
        }
        this.fromPO.setValue(false);
         if(!(this.quotation || !this.isCustomer || this.isOrder)){
             this.billingAddrs.disable();
             this.shippingAddrs.disable();
        } 
//        this.POStore.reload();			Code Optimizing :)  Unnecessary Reload removed
        this.Grid.getStore().removeAll();
        
        if(this.partialInvoiceCmb){
            this.partialInvoiceCmb.disable();
            var id=this.Grid.getId();
            var rowindex=this.Grid.getColumnModel().findColumnIndex("partamount");
            if(rowindex != -1){
                this.Grid.getColumnModel().setHidden( rowindex,true);
            }
            
        }
        this.Tax.setValue("");
        this.Tax.setDisabled(true);				// 20148 fixed
        this.isTaxable.setValue(false);
        this.showGridTax(null,null,true);
        this.Grid.symbol=undefined; // To reset currency symbol. BUG Fixed #16202
        this.Grid.updateRow(null);
        this.resetForm = true;
        this.sequenceFormatStore.reload();  //refer ticket ERP-10425
        this.sequenceFormatCombobox.setValue(formatid);
        this.getNextSequenceNumber(this.sequenceFormatCombobox);
//        var date=WtfGlobal.convertToGenericDate(new Date())
//        if(!(this.isCustBill||this.isExpenseInv)) {
//            var affecteduser = this.Name.getValue();
////            this.Grid.loadPriceStoreOnly(new Date(),this.Grid.priceStore, affecteduser);
//        } else
        this.currencyStore.load({params:{mode:201,transactiondate:WtfGlobal.convertToGenericDate(new Date())}});
        this.currencyStore.on("load",function(store){
            if(this.resetForm){
                if(this.currencyStore.getCount()<1){
                    callCurrencyExchangeWindow();
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.invoice.msg1")],2); //"Please set Currency Exchange Rates"
                } else {
                    this.isCurrencyLoad=true;
//                    this.Currency.setValue(WtfGlobal.getCurrencyID());
//                    this.currencyid=WtfGlobal.getCurrencyID();
                    this.applyCurrencySymbol();
                    this.isTaxable.setValue(false);
                    this.showGridTax(null,null,true); 
                    this.isClosable= true;
                    this.Tax.setValue("");
//                    this.applyTemplate(this.currencyStore,0);
                    this.resetForm = false;
                }
            }
        },this);      
},

    setDate:function(){
        var height = 0;
        if(this.isOrder)
            height=210;
        if(this.isOrder && !this.isCustBill && ((BCHLCompanyId.indexOf(companyid) != -1)))
            height=300;
//        if(this.isCustBill){
//        	if(this.isEdit)
//                this.allAccountStore.on('load',this.getCreditTo.createDelegate(this,[this.record.data.crdraccid]),this)
//            this.allAccountStore.load();
//            height+=20;
//        }
//        if(height>=178) this.NorthForm.setHeight(height);

        if(!this.isEdit){
            this.Discount.setValue(0);

//            this.billDate.setValue(Wtf.serverDate);//(new Date());
//            this.DueDate.setValue(Wtf.serverDate);
            this.billDate.setValue(new Date());
            this.DueDate.setValue(new Date());
        }
    },
    addTax:function(){
         var p= callTax("taxwin");
         Wtf.getCmp("taxwin").on('update', function(){this.Grid.taxStore.reload();}, this);
    },
    addCreditTerm:function(){
        callCreditTerm('credittermwin');
        Wtf.getCmp('credittermwin').on('update', function(){this.termds.reload();}, this);
    },
    addPerson:function(isEdit,rec,winid,isCustomer){
        callBusinessContactWindow(isEdit, rec, winid, isCustomer);
        var tabid=this.isCustomer?'contactDetailCustomerTab':'contactDetailVendorTab';
        Wtf.getCmp(tabid).on('update', function(){
           this.isCustomer?Wtf.customerAccStore.reload():Wtf.vendorAccStore.reload();
        }, this);
    },

    addCostCenter:function(){
        callCostCenter('addCostCenterWin');
    },
    
    getNextSequenceNumber:function(a,val){
      if(!(a.getValue()=="NA")){
         WtfGlobal.hideFormElement(this.Number);
         var rec=WtfGlobal.searchRecord(this.sequenceFormatStore, a.getValue(), 'id');
         var oldflag=rec!=null?rec.get('oldflag'):true;
        this.setTransactionNumber(true);
         Wtf.Ajax.requestEx({
            url:"ACCCompanyPref/getNextAutoNumber.do",
            params:{
                from:(this.fromnumber!=undefined) ? this.fromnumber : 1,
                sequenceformat:a.getValue(),
                oldflag:oldflag
            }
        }, this,function(resp){
            if(resp.data=="NA"){
                WtfGlobal.showFormElement(this.Number);
                this.Number.reset();
                this.Number.enable();
            }else {
                this.Number.setValue(resp.data); 
                this.Number.disable();
                WtfGlobal.hideFormElement(this.Number);
            }
            
        });
      } else {
          WtfGlobal.showFormElement(this.Number);
          this.Number.reset();
          if(!this.readOnly){
              this.Number.enable();
          }
      }
    },
    
    setTransactionNumber:function(isSelectNoFromCombo){
        var format="",temp2=''
        this.isDraft = (this.record!=null && this.record.data!=undefined) ? this.record.data.isDraft : false;	//SDP-13487        
        if(this.isRequisition) {
            format = Wtf.account.companyAccountPref.autorequisition;
            temp2=Wtf.autoNum.Requisition;
        } else if(this.isRFQ){
            format = Wtf.account.companyAccountPref.autorequestforquotation;
            temp2=Wtf.autoNum.RFQ;
        }
        if(isSelectNoFromCombo){
            this.fromnumber = temp2;
        } else if(format&&format.length>0){
            WtfGlobal.fetchAutoNumber(temp2, function(resp){if(this.isEdit && !this.isDraft)this.Number.setValue(resp.data)}, this);  //SDP-13487 - When user open the draft in edit mode then due to edit check true, we set empty value to Auto Sequence No. To make it latest Auto Sequence No. we put the this.isDraft check and it will work only for Draft Record.
        }
    },
    dueDateCheck:function(){
        if(this.DueDate.getValue().getTime()<this.billDate.getValue().getTime()){
           WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.invoice.msg11")], 2);    //"The Due Date should be greater than the Order Date."
           this.DueDate.setValue(this.billDate.getValue());
        }
    },

    initForClose:function(){
        this.cascade(function(comp){
            if(comp.isXType('field')){
                comp.on('change', function(){this.isClosable=false;},this);
            }
        },this);
    },
     loadTemplateStore : function() {
        this.templateStore.load();
    },
    addInvoiceTemplate : function(isCreatedNow,tempid) {
        if(isCreatedNow===true){
            this.loadTemplateStore();
            this.templateStore.on("load",function(){
            	this.template.setValue(tempid);
            	this.templateID.setValue(tempid);
            },this)
            	
        }else{
        new Wtf.selectNewTempWin({
            isreport : false,
            tabObj : this,
            templatetype : this.doctype 
        });
        }
    },
    
    update:function(){
     var rec=this.NorthForm.getForm().getValues();
     Wtf.MessageBox.confirm(this.isDraft ? WtfGlobal.getLocaleText("acc.common.saveasdraft") : WtfGlobal.getLocaleText("acc.common.savdat"),this.isDraft ? WtfGlobal.getLocaleText("acc.invoice.msg14") : WtfGlobal.getLocaleText("acc.invoice.msg7"), function(btn) {
        if (btn != "yes") {
            return;
        }
        if (this.isRFQ) {
         this.ajxurl = "ACCPurchaseOrder/updateRequestForQuotation.do";
        } else {
         this.ajxurl = "ACCPurchaseOrder/updateRequisition.do";
        }
        var detail = this.Grid.getProductDetails();
        rec.detail=detail;
        var custFieldArr=this.tagsFieldset.createFieldValuesArray();
        if (custFieldArr.length > 0){
            rec.customfield = JSON.stringify(custFieldArr);
        }
        this.msg = WtfComMsgBox(27,4,true);
        rec.invoiceid=this.billid;
        rec.billdate=WtfGlobal.convertToGenericDate(this.billDate.getValue());
        Wtf.Ajax.requestEx({
            url:this.ajxurl,
            params: rec
        },this,this.genSuccessResponse,this.genFailureResponse);
    }, this);
            
  },
    finallySave: function(rec,detail,incash) {
        var promptmessage = "";
        if (this.isSaveDraftRecord && this.sequenceFormatCombobox.getValue() == "NA" && (this.moduleid==Wtf.Acc_Purchase_Requisition_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId)) {
            promptmessage = WtfGlobal.getLocaleText("acc.draft.saveTheRecordWith.NA.sequenceFormat");
        } else if(this.isDraft){
            promptmessage = WtfGlobal.getLocaleText("acc.invoice.msg14");
        } else {
            promptmessage = WtfGlobal.getLocaleText("acc.invoice.msg7");
        }
        Wtf.MessageBox.confirm(this.isDraft ? WtfGlobal.getLocaleText("acc.common.saveasdraft") : WtfGlobal.getLocaleText("acc.common.savdat"), promptmessage , function(btn) {
            if (btn != "yes") {
                return;
            }
            rec.deletedLinkedDocumentId=this.linkedDocumentId.slice(0,-1);
            rec.taxid = this.Tax.getValue();
            rec.taxamount = this.caltax();
            if (this.isExpenseInv) {
                rec.expensedetail = detail;
                rec.isExpenseInv = this.isExpenseInv;
            } else {
                rec.detail = detail;
            }
            
            this.msg = WtfComMsgBox(27,4,true);
            rec.subTotal = this.Grid.calSubtotal();
            this.applyCurrencySymbol();
            rec.perdiscount = this.perDiscount.getValue();
            rec.currencyid = this.Currency.getValue();
            rec.externalcurrencyrate = this.externalcurrencyrate;
            rec.discount = this.Discount.getValue();
//                rec.vendorinvoice = this.vendorInvoice!=null?this.vendorInvoice.getValue():'';
            rec.number = this.Number.getValue();
            rec.duedate = WtfGlobal.convertToGenericDate(this.DueDate.getValue());
            rec.billdate = WtfGlobal.convertToGenericDate(this.billDate.getValue());
            rec.shipdate = WtfGlobal.convertToGenericDate(this.billDate.getValue());
            rec.invoiceid = this.copyInv || this.isFromWO || this.ifFromShortfall ? "" : this.billid;
            rec.shipaddress = (!(this.quotation || !this.isCustomer || this.isOrder))? this.shipTo.getValue() : this.billTo.getValue();
            rec.mode = (this.isOrder? (this.isCustBill? 51:41) : (this.isCustBill? 13:11));
            rec.incash = incash;
            rec.isdraft = this.isDraft;
            rec.partialinv = (this.partialInvoiceCmb)? this.partialInvoiceCmb.getValue() : false;
            rec.includeprotax = (this.includeProTax)? this.includeProTax.getValue() : false;
            this.sequenceFormatStore.clearFilter(true);
            var seqFormatRec = WtfGlobal.searchRecord(this.sequenceFormatStore, this.sequenceFormatCombobox.getValue(), 'id');
            rec.seqformat_oldflag = seqFormatRec!=null? seqFormatRec.get('oldflag') : false;
            rec.sequenceformat = this.sequenceFormatCombobox.getValue();
            rec.isEdit = this.isEdit;
            rec.copyInv = this.copyInv;
            rec.isSaveDraftRecord = this.isSaveDraftRecord; //SDP-13487
	    rec.isAutoSeqForEmptyDraft = this.isAutoSeqForEmptyDraft;   //SDP-13927 : To identify Old Record No. and Auto Generated No while saving Draft Record in Edit Mode.
            rec.isSequenceFormatChangedInEdit = this.isSequenceFormatChangedInEdit;    //SDP-13923 - This boolean flag has sent to business logic    
            Wtf.Ajax.requestEx({
                url: this.ajxurl,
//                url: Wtf.req.account+this.businessPerson+'Manager.jsp',
                params: rec
            }, this, this.genSuccessResponse, this.genFailureResponse);
        }, this);
    },
    
    checkForBudgetLimitActivated: function(rec,detail,incash) {
        this.BudgetSetForDepartment = 0;
        this.BudgetSetForDepartmentAndProduct = 1;
        this.BudgetSetForDepartmentAndProductCategory = 2;
        
        if (Wtf.account.companyAccountPref.activatebudgetingforPR) { // Approval for budgeting amount is activated
            var budgetType = Wtf.account.companyAccountPref.budgetType;
            if (budgetType == this.BudgetSetForDepartment) { // If budgeting is applied upon Department
                this.checkIfBugetLimitExceedingForDepartment(rec,detail,incash);
            } else if (budgetType == this.BudgetSetForDepartmentAndProduct) { // If budgeting is applied upon Department and specific product
                this.checkIfBugetLimitExceedingForDepartmentAndProduct(rec,detail,incash);
            } else { // If budgeting is applied upon Department and specific category of product
                this.finallySave(rec,detail,incash);
            }
        } else { // Approval flow not activated for budgeting amount
            this.finallySave(rec,detail,incash);
        }
    },
    
    getAllProductAndRate: function() {
        var storeLength = this.Grid.getStore().getCount() - 1;
        var rec;
        var productDetails = [];
        for (var i=0; i<storeLength; i++) {
            var rowObject = new Object();
            rec = this.Grid.getStore().getAt(i);
            rowObject['productId'] = rec.data.productid;
            rowObject['amount'] = rec.data.amount;
            productDetails.push(rowObject);
        }
        return productDetails;
    },
    
    checkIfBugetLimitExceedingForDepartmentAndProduct: function(rec,detail,incash) {
        var productDetails = this.getAllProductAndRate();
        var detail = this.Grid.getProductDetails();
                 
        Wtf.Ajax.requestEx({
            url: "ACCPurchaseOrderCMN/checkIfBugetLimitExceeding.do",
            params: {
//                requisitionTotalAmount: this.calTotalAmount(),
                productDetails: JSON.stringify(productDetails),
                budgetingType: this.BudgetSetForDepartmentAndProduct,
                requisitionDate: WtfGlobal.convertToGenericDate(this.billDate.getValue()),
                currencyID: this.Currency.getValue(),
                detail: detail
            }
        }, this, function(response) {
            if (response.success) {
                var isBudgetExceeding = response.isBudgetExceeding;

                if (isBudgetExceeding) {
                    if (Wtf.account.companyAccountPref.budgetwarnblock == 0) { // Ignore case
                        this.finallySave(rec,detail,incash);
                    } else if (Wtf.account.companyAccountPref.budgetwarnblock == 1) { // Warn case

                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.budgetIsExceedingDoYouWantToContinue"), function(btn) {
                            if (btn == "yes") {
                                this.finallySave(rec,detail,incash);
                            } else {
                                return;
                            }
                        }, this);

                    } else if (Wtf.account.companyAccountPref.budgetwarnblock == 2) { // Block case
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),  WtfGlobal.getLocaleText("acc.field.budgetIsExceedingSoYouCannotProceed")], 2);
                        return;
                    }
                } else {
                    this.finallySave(rec,detail,incash);
                }
            }
        }, this.genFailureResponse);
    },
    
    checkIfBugetLimitExceedingForDepartment: function(rec,detail,incash) {
        
        var globleCustomfield = "";
        var globleCustFieldArr = this.tagsFieldset.createFieldValuesArray();
        if (globleCustFieldArr.length > 0) {
            globleCustomfield = JSON.stringify(globleCustFieldArr);
        }
        var detail = this.Grid.getProductDetails();
                 
        Wtf.Ajax.requestEx({
            url: "ACCPurchaseOrderCMN/checkIfBugetLimitExceeding.do",
            params: {
                requisitionTotalAmount: this.calTotalAmount(),
                budgetingType: this.BudgetSetForDepartment,
                requisitionDate: WtfGlobal.convertToGenericDate(this.billDate.getValue()),
                currencyID: this.Currency.getValue(),
                globleCustomfield: globleCustomfield,
                detail: detail
            }
        }, this, function(response) {
            if (response.success) {
                var isBudgetExceeding = response.isBudgetExceeding;

                if (isBudgetExceeding) {
                    if (Wtf.account.companyAccountPref.budgetwarnblock == 0) { // Ignore case
                        this.finallySave(rec,detail,incash);
                    } else if (Wtf.account.companyAccountPref.budgetwarnblock == 1) { // Warn case

                        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.budgetIsExceedingDoYouWantToContinue"), function(btn) {
                            if (btn == "yes") {
                                this.finallySave(rec,detail,incash);
                            } else {
                                return;
                            }
                        }, this);

                    } else if (Wtf.account.companyAccountPref.budgetwarnblock == 2) { // Block case
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),  WtfGlobal.getLocaleText("acc.field.budgetIsExceedingSoYouCannotProceed")], 2);
                        return;
                    }
                } else {
                    this.finallySave(rec,detail,incash);
                }
            }
        }, this.genFailureResponse);
    },
    
     deleteSelectedRecord: function() {
        var store = this.Grid.getStore();
        var arr = [];
        var arrLinked1 = [];
        var component = "";
        var deletedData = [];
        var value = "";
        var arrLinked = [];
        var arrNotLinked = [];
        var message = "Link Information of ";
        var count = 0;
        var rowindex = 0;
        var isLastProductDeletedFlag = false;
        var isNotLinkedFlag = false;
        var selectedCount = this.Grid.sModel.getCount();

        for (rowindex = 0; rowindex < selectedCount; rowindex++) {

            var record = this.Grid.sModel.getSelections()[rowindex];
            arrLinked[rowindex] = "";
            arrNotLinked[rowindex] = "";

            /* Function is used to check whether selected linked product is last product or not*/

            var lastProductDeleted = false;

            lastProductDeleted = isLinkedProduct(store, record, true);


            if (lastProductDeleted) {
                count++;
                isLastProductDeletedFlag = true;
                if (Wtf.Acc_RFQ_ModuleId == this.moduleid & count == 1) {
                    message += "Purchase Requisition <b>";
                }

                if (this.PO.id) {
                    if (count == 1) {
                        component = Wtf.getCmp(this.PO.id);
                        value = component.getValue();
                        arr = value.split(",");
                        arrLinked1 = value.split(",");
                    }

                    /* Block is used to remove linked document from combo */
                    if (arr.length > 1) {
                        this.linkedDocumentId += record.data.linkid + ",";//appending ID of removed document
                        arr.remove(record.data.linkid);

                    }

                }

                var qty = record.data.quantity;
                qty = (qty == "NaN" || qty == undefined || qty == null) ? 0 : qty;

                if (record.data.copyquantity != undefined) {

                    var newRec = new this.Grid.deleteRec({
                        productid: record.data.productid,
                        productname: record.data.productname,
                        productquantity: qty,
                        productbaseuomrate: record.data.baseuomrate,
                        productbaseuomquantity: record.data.baseuomquantity,
                        productuomid: record.data.uomid,
                        productrate: record.data.rate
                    });
                    deletedData.push(newRec);
                }
                /* Preparing array of linked product*/

                arrLinked[rowindex] = this.Grid.sModel.getSelections()[rowindex];

            } else {
                /* Block is used to delete  line level Product if they have not been linked*/
                var qty = record.data.quantity;
                qty = (qty == "NaN" || qty == undefined || qty == null) ? 0 : qty;

                if (record.data.copyquantity != undefined) {

                    var newRec = new this.Grid.deleteRec({
                        productid: record.data.productid,
                        productname: record.data.productname,
                        productquantity: qty,
                        productbaseuomrate: record.data.baseuomrate,
                        productbaseuomquantity: record.data.baseuomquantity,
                        productuomid: record.data.uomid,
                        productrate: record.data.rate

                    });
                    deletedData.push(newRec);
                }
                isNotLinkedFlag = true;
                arrNotLinked[rowindex] = this.Grid.sModel.getSelections()[rowindex];

            }
        }


        var showPromptFlag = false;
        var linkid = "";
        var message1 = "";
        var countforall = 0;
        /* Checking whether Last Product is deleted from linked documents*/
        if (isLastProductDeletedFlag) {
            for (var j = 0; j < arrLinked1.length; j++) {

                var recordCount = 0;
                var recordCountInLinkedArray = 0;
                var counter = 0;
                /* calculating count of selected linked product*/
                for (var l = 0; l < arrLinked.length; l++) {
                    if (arrLinked[l] != "") {

                        linkid = arrLinked[l].data.linkid;

                        if (linkid == arrLinked1[j]) {
                            if (counter == 0) {
                                counter++;
                                message1 += " <b>" + arrLinked[l].data.linkto + "</b>" + " , ";
                            }
                            recordCountInLinkedArray++;
                        }
                    }
                }
                /* calculating count from store with selected linkid product*/
                store.each(function(rec) {

                    linkid = rec.data.linkid;

                    if (arrLinked1[j] == linkid) {
                        recordCount++;
                    }

                }, this)

                /* checking that all linked product of particular linked transaction is selcted, if yes then showing prompt i.e showPromptFlag=true*/
                if (recordCount - recordCountInLinkedArray == 0) {
                    message += message1;
                    showPromptFlag = true;
                    countforall++;
                } else {
                    arr[j] = arrLinked1[j];
                }
                message1 = "";
            }
            /* Checking whether all linked product is selcted if yes then showing prompt*/
            if (arrLinked1.length == countforall) {

                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.report.requestforquotation.delete.lastlinked")], 2);
                return false;

            }
        }

        /* Block is used to Delete Line level product after confirmation if they linked with any other document */
        if (((isLastProductDeletedFlag && isNotLinkedFlag) || isLastProductDeletedFlag) && showPromptFlag) {
            message = message.slice(0, -3);
            message += "</b> will be removed. </br>" + WtfGlobal.getLocaleText("acc.nee.48")
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), message, function(btn) {
                if (btn != "yes")
                    return;

                /* Deleting linked product by ID*/
                for (var i = 0; i < arrLinked.length; i++) {
                    if (arrLinked[i] != "") {
                        var id = arrLinked[i].id;
                        store.remove(store.getById(id));
                    }

                }
                component.setValue(arr);

                /* Block is used to Delete Line level product if they are not linked with any other document */
                if (isNotLinkedFlag) {

                    /* Deleting linked product by ID*/
                    for (var i = 0; i < arrNotLinked.length; i++) {
                        if (arrNotLinked[i] != "") {
                            var id = arrNotLinked[i].id;
                            store.remove(store.getById(id));
                        }

                    }
                }

                this.Grid.deleteStore.add(deletedData);
                this.Grid.fireEvent('datachanged', this);
                this.Grid.fireEvent('productdeleted', this);
            }, this);
        } else {
            /* Block is used to Delete Line level product if they cannot affect linking information of that document*/

            /* Deleting linked product by ID*/
            for (var i = 0; i < arrLinked.length; i++) {
                if (arrLinked[i] != "") {
                    var id = arrLinked[i].id;
                    store.remove(store.getById(id));
                }

            }

            /* Deleting Non-linked product by ID*/
            for (var i = 0; i < arrNotLinked.length; i++) {
                if (arrNotLinked[i] != "") {
                    var id = arrNotLinked[i].id;
                    store.remove(store.getById(id));
                }

            }
            this.Grid.deleteStore.add(deletedData);
            this.Grid.fireEvent('datachanged', this);
            this.Grid.fireEvent('productdeleted', this);

        }
    },
    checkForDeActivatedProductsAdded:function(){
    var invalidProducts='';
    if(!this.isEdit){
        invalidProducts = this.checkDeactivatedProductsInGrid();
    }
    return invalidProducts;
},
    checkDeactivatedProductsInGrid :function(){
        var inValidProducts=''
        var rec = null;
        var productId = null;
        var productRec = null;
        for(var count=0;count<this.Grid.store.getCount();count++){
            rec = this.Grid.store.getAt(count);
            productId = rec.data.productid;
            if(productId!= undefined && productId != null && productId != ''){
                productRec = WtfGlobal.searchRecord(this.Grid.productComboStore, productId, "productid");
                if(this.isRFQ && !productRec){
                    productRec = rec;
                }
                if(productRec && (productRec.data.hasAccess === false)){
                    inValidProducts+=productRec.data.productname+', ';
                }
            }    
        }
        return inValidProducts; // List of deactivated products
    },
    resetProductDetailsTplSummary: function(scope,rowindex,record) {
        if (this.Grid.getSelectionModel().getSelections().length == 0) {
            this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body, {productname: "&nbsp;&nbsp;&nbsp;&nbsp;", productid: 0, qty: 0, soqty: 0, poqty: 0,blockqty:0,salableStock:0,reserveStock:0});
        } else if (this.Grid.getSelectionModel().getSelections().length == 1) {
            var record = this.Grid.getSelectionModel().getSelected();
//            this.setProductDetailsTplSummary('','',record);
            this.setProductDetailsTplSummaryOptimised('','',record);
    }
    },
      
setProductDetailsTplSummaryOptimised: function(scope,rowindex,record){
        if (this.Grid.getSelectionModel().getSelections().length == 1) {
           var isExpensive = (this.isExpenseInv != null && this.isExpenseInv != undefined) ? this.isExpenseInv : false;
            if (!this.isCustBill && !isExpensive) {
            this.tplRec = Wtf.data.Record.create ([
           {name:'quantity'},
           {name:'openpocount'},
           {name:'sicount'},
           {name:'socount'},
           {name:'type'},
           {name:'opensocount'},
           {name:'lockquantity'},
           {name:'reservestock'},
           {name:'pocountinselecteduom'},
           {name:'socountinselecteduom'},
           {name:'uomname'},
           {name:'availableQtyInSelectedUOM'},
           {name:'blockLooseSell'}
            ]);

                this.tplRecStore = new Wtf.data.Store({
                    reader: new Wtf.data.KwlJsonReader({
                        root: "data",
                        totalProperty:"totalCount"
                    },this.tplRec),
                    baseParams:{
                        ids:record.data.productid,
                        getSOPOflag:true,
                        moduleid:this.moduleid,
                        startdate:WtfGlobal.convertToGenericDate(WtfGlobal.getDates(true)),
                        enddate:    WtfGlobal.convertToGenericDate(WtfGlobal.getDates(false))
                    },
                    url:"ACCProductCMN/getOutstandingPOSOCount.do"
                }); 
                this.tplRecStore.load();
                this.tplRecStore.on('load', function(store) {
                if(store.getTotalCount()>0){
                    var prorec=store.getAt(0);
                        if (prorec != undefined && prorec != -1 && prorec != "") {

                        var  productType;
                        if(prorec.data!=undefined && prorec.data!=null){
                            productType=prorec.data.type;
                        }

                        if(productType!=undefined && productType!=null && productType!="" && productType!="Service"){
                        
                            var availableQuantityInBaseUOM = prorec.data['quantity'];

                            var isBlockLooseSell = prorec.data['blockLooseSell'];

                            var availableQuantityInSelectedUOM = availableQuantityInBaseUOM;

                            var pocountinselecteduom = prorec.data['openpocount'];

                            var socountinselecteduom = prorec.data['opensocount'];
                    
                            var soBlockQuatity =prorec.data['lockquantity'];
                   
                            var salableStock =availableQuantityInBaseUOM+pocountinselecteduom-soBlockQuatity;//Instock + Open PO - Block So
                   
                            var reserveStock =prorec.data['reservestock'];

                            availableQuantityInSelectedUOM = this.calculateqtyforoustandingSOorSI(prorec);
                            if (isBlockLooseSell && record.get('isAnotherUOMSelected')) {//
                                availableQuantityInSelectedUOM = record.get('availableQtyInSelectedUOM');
                        
                                pocountinselecteduom = prorec.data['pocountinselecteduom'];

                                socountinselecteduom = prorec.data['socountinselecteduom'];
                            }

                            var selectedUOMName = '';

                            if (isBlockLooseSell) {
                                selectedUOMName = record.get('uomname');
                            }

                            if (selectedUOMName == undefined || selectedUOMName == null || selectedUOMName == '') {
                                selectedUOMName = prorec.data['uomname'];
                            }

                            this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body, {
                                productid: record.get('productid'),
                                productname: record.get('productname'),
                                qty: parseFloat(getRoundofValue(availableQuantityInSelectedUOM)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                                soqty: parseFloat(getRoundofValue(socountinselecteduom)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                                poqty: parseFloat(getRoundofValue(pocountinselecteduom)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                                blockqty: parseFloat(getRoundofValue(soBlockQuatity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                                salableStock:parseFloat(getRoundofValue(salableStock)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                                reserveStock:parseFloat(getRoundofValue(reserveStock)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName
                            });
                        
                        }else{
                        
                            this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body, {
                                productname: "&nbsp;&nbsp;&nbsp;&nbsp;", 
                                productid: 0, 
                                qty: 0, 
                                soqty: 0, 
                                poqty: 0,
                                blockqty:0,
                                salableStock:0,
                                reserveStock:0
                            });
                        
                        }
                    } else if (record.data.productname != undefined && record.data.productname != -1 && record.data.productname != "") {
                        var availableQuantityInSelectedUOM = Wtf.isEmpty(record.data.availableQtyInSelectedUOM) ? 0 : record.data.availableQtyInSelectedUOM;
                        var pocountinselecteduom = Wtf.isEmpty(record.data.pocountinselecteduom) ? 0 : record.data.pocountinselecteduom;
                        var socountinselecteduom = Wtf.isEmpty(record.data.socountinselecteduom) ? 0 : record.data.socountinselecteduom;
                        var soBlockQuatity =Wtf.isEmpty(record.data.lockquantity) ? 0 : record.data.lockquantity;
                        var reserveStock =Wtf.isEmpty(record.data.reserveStock) ? 0 : record.data.reserveStock;
                        var salableStock =availableQuantityInSelectedUOM+pocountinselecteduom-soBlockQuatity;//Instock + Open PO - Block So
                        var selectedUOMName = record.data.uomname;
                        availableQuantityInSelectedUOM = this.calculateqtyforoustandingSOorSI(record);
                        this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body, {
                            productid: record.data.productid,
                            productname: record.data.productname,
                            qty: parseFloat(getRoundofValue(availableQuantityInSelectedUOM)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                            soqty: parseFloat(getRoundofValue(socountinselecteduom)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                            poqty: parseFloat(getRoundofValue(pocountinselecteduom)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                            blockqty:parseFloat(getRoundofValue(soBlockQuatity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                            salableStock:parseFloat(getRoundofValue(salableStock)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                            reserveStock:parseFloat(getRoundofValue(reserveStock)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName
                        });
                    }
                }else{
                    this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body, {
                        productname: "&nbsp;&nbsp;&nbsp;&nbsp;", 
                        productid: 0, 
                        qty: 0, 
                        soqty: 0, 
                        poqty: 0,
                        blockqty:0,
                        salableStock:0,
                        reserveStock:0
                    });
                }
            }, this);
        }
    }
    },
    setProductDetailsTplSummary: function(scope,rowindex,record){
        if (this.Grid.getSelectionModel().getSelections().length == 1) {
            var isExpensive = (this.isExpenseInv != null && this.isExpenseInv != undefined) ? this.isExpenseInv : false;
            if (!this.isCustBill && !isExpensive) {
                var index = this.Grid.productComboStore.findBy(function(rec) {
                    if (rec.data.productid == record.data.productid)
                        return true;
                    else
                        return false;
});
                var prorec = this.Grid.productComboStore.getAt(index);
                if (prorec != undefined && prorec != -1 && prorec != "") {

                    var availableQuantityInBaseUOM = prorec.data['quantity'];

                    var isBlockLooseSell = prorec.data['blockLooseSell'];

                    var availableQuantityInSelectedUOM = availableQuantityInBaseUOM;

                    var pocountinselecteduom = prorec.data['openpocount'];

                    var socountinselecteduom = prorec.data['opensocount'];
                    
                   var soBlockQuatity =prorec.data['lockquantity'];
                   
                   var salableStock =availableQuantityInBaseUOM+pocountinselecteduom-soBlockQuatity;//Instock + Open PO - Block So
                   
                    var reserveStock =prorec.data['reservestock'];

                    availableQuantityInSelectedUOM = this.calculateqtyforoustandingSOorSI(prorec);
                    if (isBlockLooseSell && record.get('isAnotherUOMSelected')) {//
                        availableQuantityInSelectedUOM = record.get('availableQtyInSelectedUOM');
                        
                        pocountinselecteduom = record.get('pocountinselecteduom');

                        socountinselecteduom = record.get('socountinselecteduom');
                    }

                    var selectedUOMName = '';

                    if (isBlockLooseSell) {
                        selectedUOMName = record.get('uomname');
                    }

                    if (selectedUOMName == undefined || selectedUOMName == null || selectedUOMName == '') {
                        selectedUOMName = prorec.data['uomname'];
                    }

                    this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body, {
                        productid: prorec.data['productid'],
                        productname: prorec.data['productname'],
                        qty: parseFloat(getRoundofValue(availableQuantityInSelectedUOM)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                        soqty: parseFloat(getRoundofValue(socountinselecteduom)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                        poqty: parseFloat(getRoundofValue(pocountinselecteduom)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                        blockqty: parseFloat(getRoundofValue(soBlockQuatity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                        salableStock:parseFloat(getRoundofValue(salableStock)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                        reserveStock:parseFloat(getRoundofValue(reserveStock)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName
                    });
                } else if (record.data.productname != undefined && record.data.productname != -1 && record.data.productname != "") {
                    var availableQuantityInSelectedUOM = Wtf.isEmpty(record.data.availableQtyInSelectedUOM) ? 0 : record.data.availableQtyInSelectedUOM;
                    var pocountinselecteduom = Wtf.isEmpty(record.data.pocountinselecteduom) ? 0 : record.data.pocountinselecteduom;
                    var socountinselecteduom = Wtf.isEmpty(record.data.socountinselecteduom) ? 0 : record.data.socountinselecteduom;
                    var soBlockQuatity =Wtf.isEmpty(record.data.lockquantity) ? 0 : record.data.lockquantity;
                    var reserveStock =Wtf.isEmpty(record.data.reserveStock) ? 0 : record.data.reserveStock;
                    var salableStock =availableQuantityInSelectedUOM+pocountinselecteduom-soBlockQuatity;//Instock + Open PO - Block So
                    var selectedUOMName = record.data.uomname;
                    availableQuantityInSelectedUOM = this.calculateqtyforoustandingSOorSI(record);
                    this.productDetailsTplSummary.overwrite(this.productDetailsTpl.body, {
                        productid: record.data.productid,
                        productname: record.data.productname,
                        qty: parseFloat(getRoundofValue(availableQuantityInSelectedUOM)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                        soqty: parseFloat(getRoundofValue(socountinselecteduom)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                        poqty: parseFloat(getRoundofValue(pocountinselecteduom)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                        blockqty:parseFloat(getRoundofValue(soBlockQuatity)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                        salableStock:parseFloat(getRoundofValue(salableStock)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName,
                        reserveStock:parseFloat(getRoundofValue(reserveStock)).toFixed(Wtf.QUANTITY_DIGIT_AFTER_DECIMAL) + "  " + selectedUOMName
                    });
                }
            }
        }
    },
    calculateqtyforoustandingSOorSI: function (prorec) {
        var availableQuantityInBaseUOM;
        /**
         * If Wtf.Show_all_Products is activated then product store is load and quantity is taken from  "quantity" dataindex otherwise 
         * taken from "availableQtyInSelectedUOM" dataindex.
         */

        availableQuantityInBaseUOM = prorec.data['availableQtyInSelectedUOM'] != undefined ? prorec.data['availableQtyInSelectedUOM'] : 0;
        var availableQuantityInSelectedUOM = availableQuantityInBaseUOM;
        if (prorec.data.type != "Service" && prorec.data.type != 'Non-Inventory Part') {
            if (this.moduleid == Wtf.Acc_Sales_Order_ModuleId) { // Sales Order
                if (prorec.data.socount !== undefined && prorec.data.socount !== "") {
                    if (Wtf.account.companyAccountPref.negativeStockFormulaSO == 1) {
                        /*
                         * Apply Formula for consider Outstanding SO Qty
                         */
                        availableQuantityInSelectedUOM = availableQuantityInSelectedUOM - prorec.data.socount;
                    }
                }
            } else if (this.moduleid == Wtf.Acc_Invoice_ModuleId || this.moduleid == Wtf.Acc_Cash_Sales_ModuleId) { // Sales Invoice/cash sales
                if (prorec.data.sicount != undefined && prorec.data.sicount !== "") {
                    if (Wtf.account.companyAccountPref.negativeStockFormulaSI == 1) {
                        /*
                         * Apply Formula for consider Outstanding SI Qty
                         */
                        availableQuantityInSelectedUOM = availableQuantityInSelectedUOM - prorec.data.sicount;
                    }
                }
            }
        }
        return availableQuantityInSelectedUOM;
    },
    /*
     * SDP-13923
     * This function has been used to check whether user has changed sequence format in Edit case of draft.
     * If user changes the sequence format from "NA" to Auto-Sequence Format then 'this.isSequenceFormatChangedInEdit' flag will be true and this flag has used on java side.
     */
    sequenceFormatChanged : function(combo, newval, oldval) {
        if (this.isEdit && (this.isDraft||this.record.data.isDraft) && (this.moduleid===Wtf.Acc_Purchase_Requisition_ModuleId || this.moduleid==Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId)){
            if (oldval != newval && newval != "NA") {
                this.isSequenceFormatChangedInEdit = true;
                this.isAutoSeqForEmptyDraft = true;
                this.getNextSequenceNumber(combo);
            } else {
                this.isSequenceFormatChangedInEdit = false;
            }
        }        
    }    
});
