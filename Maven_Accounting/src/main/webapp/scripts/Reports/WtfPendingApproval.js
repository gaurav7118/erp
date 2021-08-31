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

/*
 * Load Dynamic WtfPendingApproval.js
 */
function callPendingApprovalsForAllModules(isFromGSTForm03) {
    /*
     * isFromGSTForm03 is sent from form 03 generation . Existing message box for
     * showing link for this report will be closed before further report loading 
     */ 
    
    if(isFromGSTForm03){
       Wtf.MessageBox.hide(); 
    }
    var pendingApprovalpanel = Wtf.getCmp("pendingApproval");
    if (pendingApprovalpanel == null) {
        pendingApprovalpanel = new Wtf.PendingApproval({
            id: 'pendingApproval',
            border: false,
            layout: 'fit',
            displayFlag: false,
            closable: true,
            iconCls: 'accountingbase agedrecievable',
            title: Wtf.util.Format.ellipsis(WtfGlobal.getLocaleText("acc.dashboard.PendingApprovalForAllModules"), Wtf.TAB_TITLE_LENGTH),
            tabTip: WtfGlobal.getLocaleText("acc.dashboard.PendingApprovalForAllModules.tabtip")
        });
        Wtf.getCmp('as').add(pendingApprovalpanel);
        
    } 
    Wtf.getCmp('as').setActiveTab(pendingApprovalpanel);
    pendingApprovalpanel.doLayout();
}

//*************************************************************************************************************


/* Component for showing pending approval records for all modules on dashboard.*/

Wtf.PendingApproval = function(config) {
    this.arr = [];
    this.id=config.id;
    this.sm = new Wtf.grid.CheckboxSelectionModel(); 

    this.Store = new Wtf.data.GroupingStore({
        url: "ACCDashboard/getPendingApprovalsForAllModules.do",
        reader: new Wtf.data.KwlJsonReader({
            root: "data",
            totalProperty: 'totalCount'
        })
    });
    
    this.groupStore = new Wtf.data.GroupingStore({
        groupField: ['module']
    });    
    this.groupView = new Wtf.grid.GroupingView({
        forceFit: false,
        showGroupName: false,
        enableGroupingMenu: true,
        hideGroupedColumn: false,
        emptyText: '<div class="emptyGridText">' + WtfGlobal.getLocaleText('account.common.nodatadisplay') + ' <br></div>'
    });
    
    this.gridSummary = new Wtf.grid.GroupSummary();
    
    this.quickPanelSearch = new Wtf.KWLTagSearch({
        emptyText: WtfGlobal.getLocaleText("acc.dimensionsReport.search"),
        width: 150,
        id: "quickSearch" + config.helpmodeid + config.id,
        field: 'billno',
        Store: this.Store
    })
    this.resetBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.reset"), //'Reset',
        tooltip: WtfGlobal.getLocaleText("acc.common.resetTT"), 
        id: 'btnRec',
        scope: this,
        iconCls: getButtonIconCls(Wtf.etype.resetbutton),
        disabled: false
    });
    this.resetBttn.on('click', this.handleResetClick, this);

    this.approveBttn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.cc.24"),
        tooltip: WtfGlobal.getLocaleText("acc.field.ApprovePending"), 
        id: 'approvepending' + this.id,
        scope: this,
        iconCls: this.isRequisition ? "accountingbase prapprove" : getButtonIconCls(Wtf.etype.add),
        disabled: true,
        handler: this.approvePendingTransactions
    });
    this.rejectBttn=new Wtf.Toolbar.Button({
        text:WtfGlobal.getLocaleText("acc.field.Reject"),
        tooltip : WtfGlobal.getLocaleText("acc.field.Rejectpending"),
        id: 'rejectpending' + this.id,
        scope: this,
        iconCls:getButtonIconCls(Wtf.etype.deletebutton),
        disabled :true,
        handler : this.handleReject
    });
    
    this.Store.on('load', function(store) {
        this.storeLoaded();
    }, this);

    this.Store.on('datachanged', function() {
        this.storeLoaded();
    }, this);
    
    this.Store.on("loadexception", function() {
         Wtf.MessageBox.hide();
    }, this)
    this.Store.on('beforeload', function(s,o) {
        if(this.pPageSizeObj!=undefined &&this.pPageSizeObj.combo!=undefined){
            if(this.pPageSizeObj.combo.value=="All"){
                var count;
                if(this.Store!=undefined){
                     count = this.Store.getTotalCount()
                }
                var rem = count % 5;
                if(rem == 0){
                    count = count;
                }else{
                    count = count + (5 - rem);
                }
                s.baseParams.limit = count;
            } else {
                s.baseParams.limit = this.pPageSizeObj.combo.value;
            }
        }
    }, this);
    
    this.tbar1 = [];
    this.tbar1.push(this.quickPanelSearch, "-", this.resetBttn,"-",this.approveBttn,"-",this.rejectBttn);
    this.grid = new Wtf.grid.GridPanel({
        store: this.groupStore,
        sm:this.sm,
        view: this.groupView,
        plugins:[this.gridSummary],
        searchField: "module",
        columns: [],
        border: false,
        layout: 'fit',
        viewConfig: {
            forceFit: true,
            emptyText: WtfGlobal.emptyGridRenderer(WtfGlobal.getLocaleText("acc.common.norec"))
        },
        autoScroll: true,
        loadMask: true
    });
    
    this.gridpan = new Wtf.Panel({
        layout: 'border',
        border: false,
        attachDetailTrigger: true,
        items: [{
                region: 'center',
                border: false,
                layout: "fit",
                autoScroll: true,
                tbar: this.tbar1,
                items: [this.grid],
                bbar: this.pag=new Wtf.PagingSearchToolbar({
                pageSize: 30,
                border : false,
                id : "paggintoolbar_ProductGrid"+this.id,
                store: this.Store,
                searchField: this.quickPanelSearch,
                scope:this,
                plugins : this.pPageSizeObj = new Wtf.common.pPageSize({
                    id : "pPageSize_ProductGrid_"+this.id
                }),
                autoWidth : true,
                displayInfo:true
            }) 
            }]
    });
    Wtf.apply(this, {
        border: false,
        layout: "fit",
        closable:true,
        items: [this.gridpan]
    });
    this.sm.on("selectionchange",this.enableDisableButtons.createDelegate(this),this);
    this.grid.on('cellclick',this.onCellClick, this);

    this.Store.load({
        params: {
            start: 0,
            limit: 30
        }
    });
    Wtf.PendingApproval.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.PendingApproval, Wtf.Panel, {
     handleResetClick:function(){
        if(this.quickPanelSearch.getValue()){
            this.quickPanelSearch.reset();
            this.loadStore();
        }
    },
    loadStore: function() {
        this.Store.load({
            params: {
                start: 0,
                limit: (this.pPageSizeObj!=undefined) ? this.pPageSizeObj.combo.value : 30,
                ss: this.quickPanelSearch.getValue()
            }
        });
    },
    enableDisableButtons: function() {
        var recs = this.grid.getSelectionModel().getSelections();
        this.approveBttn.disable();
        this.rejectBttn.disable();
        if (recs.length == 1) {
            if (this.rejectBttn && !recs[0].data.deleted) {
                this.rejectBttn.enable();    // Reject button will be enabled only if selected record is non-rejected/non-deleted.
            }
            if(this.approveBttn && !recs[0].data.deleted) {
                this.approveBttn.enable();    // Approve button will be enabled only if selected record is non-rejected/non-deleted.                
            }
        }
    },
    storeLoaded: function() {
        var columns = [];
        columns.push(this.sm);
        Wtf.each(this.Store.reader.jsonData.columns, function(column) {
            if (column.dataIndex == "date") {
                column.renderer = function(v, m, rec) {
                    if (!v) {
                        return v;
                    }
                    if (rec != undefined && rec.data.deleted) {
                        v = '<del>' + v.format(WtfGlobal.getOnlyDateFormat()) + '</del>';
                    } else {
                        v = v.format(WtfGlobal.getOnlyDateFormat());
                    }
                    v = '<div>' + v + '</div>';
                    return v;
                };
            }
            if (column.dataIndex == "billno") {
                column.renderer = WtfGlobal.linkDeletedRenderer;
            }
            columns.push(column);
        });
        this.grid.getColumnModel().setConfig(columns);
        var Arr = [];
        Wtf.each(this.Store.reader.jsonData.metaData.fields, function(column) {
            Arr.push(column);
        });
        this.groupStore.removeAll();
        this.groupStore.fields = Arr;
        this.groupStore.add(this.Store.getRange(0, (this.Store.data.items.length - 1)));
        this.grid.getView().refresh();
    },    
    approvePendingTransactions: function() {
        if (this.grid.getSelectionModel().hasSelection() == false) {
            WtfComMsgBox(34, 2);
            return;
        }
        var formRecord = this.grid.getSelectionModel().getSelected();
        var moduleid = formRecord.json.moduleid;
        if(moduleid == Wtf.Acc_Credit_Note_ModuleId || moduleid == Wtf.Acc_Debit_Note_ModuleId){//the way of approving CN/DN has different from other module so here is check
            // For note Against invoice when approval level is final then we need to check for invoices wheather they are used in other transaction during pending or not
            // If they are used and their amound due changed then we need to as per approval type came from response 
            var isCN=(moduleid == Wtf.Acc_Credit_Note_ModuleId)?true:false;
            var URL= isCN?"ACCCreditNote/approvePendingCreditNote.do":"ACCDebitNote/approvePendingDebitNote.do";
            var winTitle=isCN?WtfGlobal.getLocaleText("acc.field.ApprovePendingCreditNote"):WtfGlobal.getLocaleText("acc.field.ApprovePendingDebitNote");
            var Label = isCN?" Credit Note":" Debit Note";
            if(formRecord.json.cntype==1 && formRecord.json.isFinalLevelApproval){
                Wtf.Ajax.requestEx({
                    url:isCN?"ACCCreditNote/checkInvoiceKnockedOffDuringCreditNotePending.do":"ACCDebitNote/checkInvoiceKnockedOffDuringDebitNotePending.do",
                    params: {
                        billid : formRecord.json.billid
                    }
                },this,function(response){
                    if(response.success){
                        var approvalType = response.approvalType;
                        if(approvalType == 1){//Approve As normal Way
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected")+Label+"?",function(btn){
                                if(btn=="yes") {
                                    this.callApprovalRemarkWindow(URL,winTitle,formRecord,approvalType);
                                } else {
                                    return;
                                }
                            }, this);
                        } else if(approvalType == 2){// Approve CN/DN against Invoice As otherwise
                            var msg = "";
                            if(isCN){
                                msg = WtfGlobal.getLocaleText("acc.field.sinceinvoiceofcreditnotefullyutilized");
                            } else {
                                msg = msg = WtfGlobal.getLocaleText("acc.field.sinceinvoiceofdebitnotefullyutilized");
                            }
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), msg+WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected")+Label+"?",function(btn){
                                if(btn=="yes") {
                                    this.callApprovalRemarkWindow(URL,winTitle,formRecord,approvalType);
                                } else {
                                    return;
                                }
                            },this); 
                        } else if(approvalType == 3){ //Approve after Edit record
                            var msg = "";
                            if(isCN){
                                msg = WtfGlobal.getLocaleText("acc.field.sinceinvoiceofcreditnotepartiallyutilized");
                            } else {
                                msg = msg = WtfGlobal.getLocaleText("acc.field.sinceinvoiceofdebitnotepartiallyyutilized");
                            }
                            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), msg+WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected")+Label+"?",function(btn){
                                if(btn=="yes") {
                                    this.editNoteToApprove(formRecord.json.billid,isCN);
                                } else {
                                    return;
                                }
                            },this); 
                        } 
                    }
                },function(response){

                });
            } else {
                Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected")+Label+"?",function(btn){
                    if(btn=="yes") {
                        var approvalType = 1;//Normal Approval
                        this.callApprovalRemarkWindow(URL,winTitle,formRecord,approvalType);
                    } else {
                        return;
                    }
                }, this); 
            }
        } else {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttoapproveselected") + " record?", function(btn) {
                if (btn == "yes") {
                    var URL = "", winTitle = "";
                    if (moduleid == Wtf.Acc_Customer_Quotation_ModuleId) {
                        URL = "ACCSalesOrder/approveCustomerQuotation.do";
                        winTitle = WtfGlobal.getLocaleText("acc.field.ApprovependingCustomerQuotation");

                    } else if (moduleid == Wtf.Acc_Vendor_Quotation_ModuleId || moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId) {
                        URL = "ACCPurchaseOrder/approveVendorQuotation.do";
                        winTitle = (formRecord.json.module == "Asset Vendor Quotation") ? WtfGlobal.getLocaleText("acc.field.ApprovependingAssetVendorQuotation") : WtfGlobal.getLocaleText("acc.field.ApprovependingVendorQuotation");
                    
                    } else if (moduleid == Wtf.Acc_Sales_Order_ModuleId) {
                        URL = "ACCSalesOrderCMN/approveSalesOrder.do";
                        winTitle = WtfGlobal.getLocaleText("acc.field.ApprovependingSalesOrder");

                    } else if (moduleid == Wtf.Acc_Purchase_Order_ModuleId || moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId) {
                        URL = "ACCPurchaseOrder/approvePurchaseOrder.do";
                        winTitle = (formRecord.json.module == "Asset Purchase Order") ? WtfGlobal.getLocaleText("acc.field.ApprovependingAssetPurchaseOrder") : WtfGlobal.getLocaleText("acc.field.ApprovependingPurchaseOrder");

                    } else if (moduleid == Wtf.Acc_GENERAL_LEDGER_ModuleId) {
                        URL = "ACCJournal/approveJournalEntry.do";
                        winTitle = WtfGlobal.getLocaleText("acc.field.ApprovependingJE");

                    } else if (moduleid == Wtf.Acc_Invoice_ModuleId) {
                        URL = "ACCInvoice/approveInvoice.do";
                        winTitle = WtfGlobal.getLocaleText("acc.field.ApprovePendingInvoice");

                    } else if (moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) {
                        URL = "ACCGoodsReceipt/approvegr.do";
                        winTitle = WtfGlobal.getLocaleText("acc.field.ApprovependingVendorInvoice");
                    } else if (moduleid == Wtf.Acc_Purchase_Requisition_ModuleId || moduleid == Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId) {
                        URL = "ACCPurchaseOrder/approvePendingRequisition.do";
                        winTitle = (formRecord.json.module == "Asset Purchase Requisition") ? WtfGlobal.getLocaleText("acc.field.ApprovependingAssetPurchaseRequisition") : WtfGlobal.getLocaleText("acc.field.ApprovePendingpurchaserequisition");
                    
                    } else if (moduleid == Wtf.Acc_Goods_Receipt_ModuleId) {
                        URL = "ACCGoodsReceipt/approveGoodsReceiptOrder.do";
                        winTitle = WtfGlobal.getLocaleText("acc.field.ApprovependingGoodsReceiptOrder");
                    
                    } else if (moduleid == Wtf.Acc_Delivery_Order_ModuleId) {
                        URL = "ACCInvoice/approveDeliveryOrder.do";
                        winTitle = WtfGlobal.getLocaleText("acc.field.ApprovependingDeliveryOrder");
                    } else if(moduleid==Wtf.Acc_Receive_Payment_ModuleId) {
                        URL = "ACCReceiptNew/approvePendingReceivePayment.do";
                        winTitle = WtfGlobal.getLocaleText("acc.field.receivepaymentpending");                        
                    } else if(moduleid==Wtf.Acc_Make_Payment_ModuleId){
                        URL = "ACCVendorPaymentNew/approvePendingMakePayment.do";
                        winTitle = WtfGlobal.getLocaleText("acc.field.makepaymentpending");                        
                    }                    
                    this.callApprovalRemarkWindow(URL,winTitle,formRecord);
                }
            }, this);
        }
    },
    
    callApprovalRemarkWindow : function(URL,winTitle,formRecord,approvalType){
        var formRecords = this.grid.getSelectionModel().getSelections();
        var dataArray = [];
        var rec={};
        var batchupdate = false;
        if (formRecord.json.moduleid == Wtf.Acc_Invoice_ModuleId || formRecord.json.moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) {
            batchupdate = true;
            for (var i = 0; i < formRecords.length; i++) {
                var temp1 = {
                    billid: formRecords[i].json.billid,
                    billno: formRecords[i].data.billno
                }
                dataArray.push(temp1);
            }

            if (dataArray.length > 0) {
                rec.data = JSON.stringify(dataArray);
                rec.isFixedAsset = (formRecord.json.module == "Asset Purchase Requisition" || formRecord.json.module == "Asset Vendor Quotation" || formRecord.json.module == "Asset Purchase Order") ? true : false;
            }
        }
        
        this.remarkWindow = new Wtf.Window({
            height: 270,
            width: 360,
            maxLength: 1000,
            title: winTitle,
            bodyStyle: 'padding:5px;background-color:#f1f1f1;',
            autoScroll: true,
            layout: 'border',
            items: [{
                region: 'north',
                border: false,
                height: 70,
                bodyStyle: 'background-color:#ffffff;border-bottom:1px solid #bfbfbf;',
                html: getTopHtml(winTitle , winTitle + " <b>" + formRecord.data.billno + "</b>", "../../images/link2.jpg", true, "10px 0 0 5px", "7px 0px 0px 10px")
            }, {
                region: 'center',
                border: false,
                layout: 'form',
                bodyStyle: 'padding:5px;',
                items: [this.remarkField = new Wtf.form.TextArea({
                    fieldLabel: WtfGlobal.getLocaleText("acc.field.AddRemark*"),
                    width: 200,
                    height: 100,
                    maxLength: 1024
                })]
            }],
            modal: true,
            buttons: [{
                text: WtfGlobal.getLocaleText("acc.cc.24"),
                id: "approvePendingTransactionsBtn" + this.id,
                scope: this,
                handler: function() {
                    Wtf.getCmp("approvePendingTransactionsBtn" + this.id).disable();
                    
                    rec.remark= this.remarkField.getValue();
                    Wtf.Ajax.requestEx({
                        url: URL,
                        params:batchupdate ? rec: {
                            billid: formRecord.json.billid,
                            billno: formRecord.data.billno,
                            customer: formRecord.data.customer,         //needs in case of SO when Rule is set for SO Credit Limit ERP-38444
                            isDocumentApprovedFromDashboradReport: true, //needs in case of SO when Rule is set for SO Credit Limit ERP-38444
                            remark: this.remarkField.getValue(),
                            approvalType : approvalType,//needs only for CN/DN
                            isFixedAsset: (formRecord.json.module == "Asset Purchase Requisition" || formRecord.json.module == "Asset Vendor Quotation" || formRecord.json.module == "Asset Purchase Order") ? true : false
                        }
                    }, this, this.genSuccessRespApproveTransaction, this.genFailureRespApproveTransaction);
                }
            }, {
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
                scope: this,
                handler: function() {
                    this.remarkWindow.close();
                }
            }]
        });
        this.remarkWindow.show();
    },
    
    genSuccessRespApproveTransaction: function(response) {
        this.remarkWindow.close();
        Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), response.msg, function(btn) {
            if (btn == "ok") {
                this.loadStore();
            }
            if (this.Store.baseParams && this.Store.baseParams.searchJson) {
                this.Store.baseParams.searchJson = "";
            }
            this.Store.on('load', function(store) {
                this.quickPanelSearch.StorageChanged(store);
            }, this);
        }, this);
    },
    genFailureRespApproveTransaction: function(response) {
        Wtf.getCmp("approvePendingTransactionsBtn" + this.id).enable();
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileupdatingstatus")], 2);
    },
    
    editNoteToApprove : function(noteid,isCN){
        var url=isCN?"ACCCreditNote/getCreditNoteMerged.do":"ACCDebitNote/getDebitNoteMerged.do";
        Wtf.Ajax.requestEx({
            url: url,
            params: {
                noteid: noteid,
                pendingapproval: true,
                isFixedAsset:false
            }
        }, this, function(resp) {
            if (resp.data && resp.data[0]) {
                var rec = {};
                rec["data"] = resp.data[0];
                if (rec.data.date) {
                    var date = rec.data.date;
                    var d = Date.parse(date);
                    var datenew = new Date(d);
                    if (datenew) {
                        rec.data.date = datenew;
                    }
                }
                if (rec.data.linkingdate) {
                    var linkingdate = rec.data.linkingdate;
                    var d = Date.parse(linkingdate);
                    var datenew = new Date(d);
                    if (datenew) {
                        rec.data.linkingdate = datenew;
                    }
                }
                var isEdit= true;
                var isEditToApprove = true; 
                var notetabid=(isCN?"EditCreditNote":"EditDebitNote")+ rec.data.noteno; 
                var isLinkedTransaction = rec.data.isLinkedTransaction;
                createNote(notetabid,isEdit,isCN,rec.data.cntype,rec,undefined,false,isLinkedTransaction,isEditToApprove);
            }
        });
    },
    
    handleReject: function() {
        if (this.grid.getSelectionModel().hasSelection() == false) {
            WtfComMsgBox(34, 2);
            return;
        }
        var data = [];
        var arr = [];
        this.recArr = this.grid.getSelectionModel().getSelections();
        this.withInvMode = this.recArr[0].data.withoutinventory;
        WtfGlobal.highLightRowColor(this.grid, this.recArr, true, 0, 2);
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.warning"), WtfGlobal.getLocaleText("acc.field.Areyousureyouwanttorejectselected") + " record?", function(btn) {
            for (var i = 0; i < this.recArr.length; i++) {
                arr.push(this.Store.indexOf(this.recArr[i]));
            }
            this.ajxUrl = "";
            data = this.getJSONArray(this.grid, true, arr);
            var formRecord = this.grid.getSelectionModel().getSelected();
            var moduleid = formRecord.json.moduleid;
            if (moduleid == Wtf.Acc_Customer_Quotation_ModuleId) {
                this.ajxUrl = "ACCSalesOrder/rejectPendingCustomerQuotation.do";

            } else if (moduleid == Wtf.Acc_Vendor_Quotation_ModuleId || moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId) {
                this.ajxUrl = "ACCPurchaseOrder/rejectPendingVendorQuotation.do";

            } else if (moduleid == Wtf.Acc_GENERAL_LEDGER_ModuleId) {
                this.ajxUrl = "ACCJournal/rejectPendingJE.do";

            } else if (moduleid == Wtf.Acc_Sales_Order_ModuleId) {
                this.ajxUrl = "ACCSalesOrder/rejectPendingSalesOrder.do";

            } else if (moduleid == Wtf.Acc_Purchase_Order_ModuleId || moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId) {
                this.ajxUrl = "ACCPurchaseOrder/rejectPendingPurchaseOrder.do";

            } else if (moduleid == Wtf.Acc_Invoice_ModuleId) {
                this.ajxUrl = "ACCInvoice/rejectPendingInvoice.do";

            } else if (moduleid == Wtf.Acc_Vendor_Invoice_ModuleId) {
                this.ajxUrl = "ACCGoodsReceipt/rejectPendingGR.do";
                
            } else if (moduleid == Wtf.Acc_Purchase_Requisition_ModuleId || moduleid == Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId) {
                this.ajxUrl = "ACCPurchaseOrder/rejectPurchaseRequisition.do";

            } else if (moduleid == Wtf.Acc_Goods_Receipt_ModuleId) {
                
                this.ajxUrl = "ACCGoodsReceipt/rejectPendingGRO.do";

            } else if (moduleid == Wtf.Acc_Delivery_Order_ModuleId) {
                
                this.ajxUrl = "ACCInvoice/rejectPendingDO.do";
            } else if (moduleid == Wtf.Acc_Credit_Note_ModuleId) {
                
                this.ajxUrl = "ACCCreditNote/rejectPendingCreditNote.do";
            } else if (moduleid == Wtf.Acc_Debit_Note_ModuleId) {
                
                this.ajxUrl = "ACCDebitNote/rejectPendingDebitNote.do";
            }
            else if (moduleid == Wtf.Acc_Receive_Payment_ModuleId) {
                this.ajxUrl = "ACCReceiptNew/rejectPendingReceivePayment.do";
            }
            else if (moduleid == Wtf.Acc_Make_Payment_ModuleId) {
                this.ajxUrl = "ACCVendorPaymentNew/rejectPendingMakePayment.do";
            }
            Wtf.Ajax.requestEx({
                url: this.ajxUrl,
                params: {
                    data: data,
                    isReject: true,
                    isFixedAsset: (moduleid == Wtf.Acc_FixedAssets_PurchaseRequisition_ModuleId || moduleid == Wtf.Acc_FixedAssets_Vendor_Quotation_ModuleId || moduleid == Wtf.Acc_FixedAssets_Purchase_Order_ModuleId) ? true : false
                }
            }, this, this.genSuccessResponse, this.genFailureResponseReject);
        }, this);
    },
    genFailureResponseReject: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
    },
    genSuccessResponse: function(response) {
        WtfGlobal.resetAjaxTimeOut();
        var superThis = this;
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), response.msg], response.success * 2 + 2, "", "", function(btn) {
            if (btn == "ok") {
                if (response.success) {
                    (function() {
                        superThis.loadStore();
                    }).defer(WtfGlobal.gridReloadDelay(), superThis);
                }
            }
        });
    },
    genFailureResponse:function(response){
        WtfGlobal.resetAjaxTimeOut();
        var msg=WtfGlobal.getLocaleText("acc.common.msg1");  //"Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),msg],2);
    },
    getJSONArray: function(grid, includeLast, idxArr) { 
        var indices = "";
        if (idxArr)
            indices = ":" + idxArr.join(":") + ":";
        var store = grid.getStore();
        var formRecord = grid.getSelectionModel().getSelected();
        var moduleid = formRecord.json.moduleid;
        var arr = [];
        var fields = store.fields;
        var len = store.getCount() - 1;
        if (includeLast)
            len++;
        for (var i = 0; i < len; i++) {
            if (idxArr && indices.indexOf(":" + i + ":") < 0)
                continue;
            var rec = store.getAt(i);
            var recarr = [];
            for (var j = 0; j < fields.length; j++) {
                var value = rec.data[fields[j].name] != undefined ? rec.data[fields[j].name] : '';
                switch (fields[j].type) {
                    case "date":
                        value = "'" + WtfGlobal.convertToGenericDate(value) + "'";
                        break;
                }
                if (fields[j].name != "jeno") {
                    if(moduleid == Wtf.Acc_GENERAL_LEDGER_ModuleId && fields[j].name == 'billid'){
                        recarr.push("journalentryid:" + value);
                        recarr.push("reversejeno:" + "\"" + "" + "\"");
                    } else if(moduleid == Wtf.Acc_GENERAL_LEDGER_ModuleId && fields[j].name == 'billno'){
                        recarr.push("entryno:" + value);
                    } else{
                        recarr.push(fields[j].name + ":" + value);
                    }
                }
            }
            recarr.push("modified:" + rec.dirty);
            arr.push("{" + recarr.join(",") + "}");
        }
        return "[" + arr.join(',') + "]";
    },
    onCellClick: function(g, i, j, e) {
        e.stopEvent();
        var el = e.getTarget("a");
        if (el == null){
            return;
        }
        var header = g.getColumnModel().getDataIndex(j);
        var formRecord = this.grid.getStore().getAt(i);
        if (header == "billno" && formRecord) {
            if(formRecord.json.moduleid==Wtf.Acc_Make_Payment_ModuleId||formRecord.json.moduleid==Wtf.Acc_Receive_Payment_ModuleId){
                Wtf.Ajax.requestEx({
                    url: (formRecord.json.moduleid==Wtf.Acc_Receive_Payment_ModuleId)?'ACCReceiptNew/getSinglePaymentDataToLoad.do':'ACCVendorPaymentNew/getSinglePaymentDataToLoad.do',
                    params: {
                        billid: formRecord.data.billid,
                        ispendingAproval:true,
                        isView:true                    //used to load discount only when view payment from pending payment report as for edit case we do not load the discount
                    }
                }, this,
                function(result, req) {
                    if (result.data.billdate) {
                        var date = result.data.billdate;
                        var d = Date.parse(date);
                        var datenew = new Date(d);
                        if (datenew) {
                            result.data.billdate = datenew;
                        }
                    }
                    if(formRecord.json.moduleid==Wtf.Acc_Receive_Payment_ModuleId){
                        callViewPaymentNew(result, 'ViewReceivePayment', true, this.grid,true);
                    }else{
                        callViewPaymentNew(result, 'ViewPaymentMade', false, this.grid,true);
                    }
                });
            }else{
                WtfGlobal.callViewMode(formRecord.get("billno"),formRecord.json.moduleid,formRecord.json.cntype,true);
            }
        }
    }
});