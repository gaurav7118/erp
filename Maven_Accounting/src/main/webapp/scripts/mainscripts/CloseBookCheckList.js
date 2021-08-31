/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

function callYearEndCheckList(rec,parent) {
    var win = new Wtf.account.YearEndCheckList({
        title: WtfGlobal.getLocaleText("acc.compref.closebook.checklist"),
        id: "yearendchecklist",
        layout: 'border',
        closable: true,
        modal: true,
        parent: parent,
        iconCls: getButtonIconCls(Wtf.etype.deskera),
        width: 380,
        height: 320,
        record: rec,
        resizable: false,
        buttonAlign: 'right',
        renderTo: document.body
    });
    win.show();
}
Wtf.account.YearEndCheckList = function(config) {
    Wtf.apply(this, {
        buttons: [{
                text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
                scope: this,
                handler: this.saveCheckList
            }, {
                text: WtfGlobal.getLocaleText("acc.common.cancelBtn"), //'Cancel',
                scope: this,
                handler: this.closeForm
            }]
    }, config);
    Wtf.account.YearEndCheckList.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.YearEndCheckList, Wtf.Window, {
    onRender: function(config) {
        Wtf.account.YearEndCheckList.superclass.onRender.call(this, config);
        this.createFields();
        this.createForm();
        var subtitle = "Year End Closing: Checklist";
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(this.title, subtitle, "../../images/accounting_image/Chart-of-Accounts.gif", true)
        }, this.centerPanel = new Wtf.Panel({
            region: 'center',
            border: false,
            autoScroll: true,
            bodyStyle: 'background:#f1f1f1;font-size:10px;padding:10px',
            baseCls: 'bckgroundcolor',
            layout: 'border',
            items: [this.checkListForm]
        }));
    },
    hideLoading: function(val) {

    },
    createFields: function() {
        this.adjustmentForTransactionCompleted = new Wtf.form.Checkbox({
            fieldLabel: WtfGlobal.getLocaleText("acc.compref.closebook.checklist.journal.adjustment.completed") + WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.compref.closebook.checklist.journal.adjustment.completed.helpText")),
            name: 'adjustmentForTransactionCompleted',
            id: 'adjustmentForTransactionCompleted',
            checked: this.record.data.adjustmentForTransactionCompleted
        });
        this.documentRevaluationCompleted = new Wtf.form.Checkbox({
            fieldLabel: WtfGlobal.getLocaleText("acc.compref.closebook.checklist.document.revaluation.completed") + WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.compref.closebook.checklist.document.revaluation.completed.helpText")),
            name: 'documentRevaluationCompleted',
            id: 'documentRevaluationCompleted',
            checked: this.record.data.documentRevaluationCompleted
        });

        this.inventoryAdjustmentCompleted = new Wtf.form.Checkbox({
            fieldLabel:  WtfGlobal.getLocaleText("acc.compref.closebook.checklist.inventory.adjustment.completed") + WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.compref.closebook.checklist.inventory.adjustment.completed.helpText")),
            name: 'inventoryAdjustmentCompleted',
            id: 'inventoryAdjustmentCompleted',
            checked: this.record.data.inventoryAdjustmentCompleted
        });

        this.assetDepreciationPosted = new Wtf.form.Checkbox({
            fieldLabel:  WtfGlobal.getLocaleText("acc.compref.closebook.checklist.asset.depreciation.completed") + WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.compref.closebook.checklist.asset.depreciation.completed.helpText")),
            name: 'assetDepreciationPosted',
            id: 'assetDepreciationPosted',
            checked: this.record.data.assetDepreciationPosted
        });
        
        this.recurringtransactionPosted = new Wtf.form.Checkbox({
            fieldLabel:  WtfGlobal.getLocaleText("acc.compref.closebook.checklist.Checkrecurringdocuments") + WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.compref.closebook.checklist.journal.Checkrecurringdocuments.helpText")),
            name: 'recurringtransactionPosted',
            id: 'recurringtransactionPosted',
            checked: this.record.data.recurringtransactionPosted
        });
        
        this.rectifyopeningbalancePosted = new Wtf.form.Checkbox({
            fieldLabel:  WtfGlobal.getLocaleText("acc.compref.closebook.checklist.Rectifyopeningbalance") + WtfGlobal.addLabelHelp(WtfGlobal.getLocaleText("acc.compref.closebook.checklist.journal.Rectifyopeningbalance.helpText")),
            name: 'rectifyopeningbalancePosted',
            id: 'rectifyopeningbalancePosted',
            checked: this.record.data.rectifyopeningbalancePosted
        });

    },
    createForm: function() {
        this.checkListForm = new Wtf.form.FormPanel({
            region: 'center',
            width: 350,
            height: 300,
            labelWidth: 250,
            border: false,
            bodyStyle: "background: transparent;",
            style: "background: transparent;padding-left:15px;padding-top: 20px;padding-right:10px;",
            items: [this.adjustmentForTransactionCompleted, this.documentRevaluationCompleted, this.inventoryAdjustmentCompleted, this.assetDepreciationPosted,this.recurringtransactionPosted,this.rectifyopeningbalancePosted]
        })
    },
    closeForm: function() {
        this.parent.gridsetvalue(); /* load year lock grid store*/ 
        this.close();
    },
    validateCheckList: function() {
        WtfGlobal.setAjaxTimeOutFor30Minutes();
        Wtf.Ajax.requestEx({
            url: "ACCCompanyPrefCMN/checkYearEndClosingCheckList.do",
            params: {
                startdate: this.record.data.sdate,
                enddate: this.record.data.edate,
                yearid: this.record.data.name
            }
        }, this, function(response) {
            WtfGlobal.resetAjaxTimeOut();
            if (response.success != "" && response.success != null && response.success != undefined && response.success) {
                if ((response.msg != null || response.msg != undefined) && response.msg !== "") {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), response.msg], 2);
                    return;
                } else {
                    var negativeStockPresent = response.data.negativeStockPresent;
                    var isDepreciationNotPosted = response.data.isDepreciationNotPosted;
                    var isPendingDocumentsPresent = response.data.isPendingDocumentsPresent;
                    var isDraftDocumentsPresent = response.data.isDraftDocumentsPresent;
                    var isBookCloseInvalid = response.data.isBookCloseInvalid;
                    var isrecurringtransactionNotPosted = response.data.isrecurringtransactionNotPosted;
                    var isDiffInOpeningBalancePresent = response.data.isDiffInOpeningBalancePresent;
                    var alertMsg=WtfGlobal.getLocaleText("acc.compref.closebook.checklist.Checklistalertmessage");
                    var count=0;
                    if (negativeStockPresent) {
                        count++;
                        alertMsg += count +". " + WtfGlobal.getLocaleText("acc.compref.closebook.checklist.negativestock.present");
                    }
                    if (isDepreciationNotPosted) {
                        count++;
                        alertMsg += "<br>" + count +". " + WtfGlobal.getLocaleText("acc.compref.closebook.checklist.assetdepreciation.not.posted");
                    }
                    if (isPendingDocumentsPresent) {
                        count++;
                        alertMsg += "<br>" + count +". " + WtfGlobal.getLocaleText("acc.compref.closebook.checklist.pending.documents.present");
                    }
                    if(isDraftDocumentsPresent){
                        count++;
                        alertMsg += "<br>" + count +". " + WtfGlobal.getLocaleText("acc.compref.closebook.checklist.draft.documents");
                    }
                    if (isrecurringtransactionNotPosted) {
                        count++;
                        alertMsg += "<br>" + count +". " + WtfGlobal.getLocaleText("acc.compref.closebook.checklist.draft.Somerecurringtransaction");
                    }
                    if(isDiffInOpeningBalancePresent){
                        count++;
                        alertMsg += "<br>" + count +". " + WtfGlobal.getLocaleText("acc.compref.closebook.checklist.opening.balance.present");
                    }
                    if (isBookCloseInvalid) {
                        count++;
                        alertMsg += "<br>" + count +". " + WtfGlobal.getLocaleText("acc.compref.closebook.checklist.please.close.book.sequentially");
                    }
                    if(count>0)
                    {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), alertMsg],2);
                    }
                    else
                    {
                        this.record.data.adjustmentForTransactionCompleted = true;
                        this.record.data.documentRevaluationCompleted = true;
                        this.record.data.inventoryAdjustmentCompleted = true;
                        this.record.data.assetDepreciationPosted = true;
                        this.record.data.recurringtransactionPosted = true;
                        this.record.data.rectifyopeningbalancePosted = true;
                        this.close();
                        return;
                    }
                }
            }
        }, function(response) {
            WtfGlobal.resetAjaxTimeOut();

        });
    },
    saveCheckList: function() {
        if (this.adjustmentForTransactionCompleted.getValue() && this.documentRevaluationCompleted.getValue() && this.inventoryAdjustmentCompleted.getValue() && this.assetDepreciationPosted.getValue()  && this.recurringtransactionPosted.getValue() && this.rectifyopeningbalancePosted.getValue()) {
            this.validateCheckList();
        } else {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),WtfGlobal.getLocaleText("acc.compref.closebook.checklist.all.items.needs.tobe.checked")], 2);
        }
    }
});
