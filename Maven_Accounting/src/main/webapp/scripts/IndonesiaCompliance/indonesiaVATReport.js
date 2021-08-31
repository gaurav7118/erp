/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * Author - Rahul A. Bhawar
 */
Wtf.account.indonesiaVATReportWindow = function (config) {
    var windowButtonArray = [];

    windowButtonArray.push(this.save = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.export"), //'Export',
        scope: this,
        handler: this.saveData.createDelegate(this)
    }))
    windowButtonArray.push(this.cancel = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
        scope: this,
        handler: this.closeWin.createDelegate(this)
    }))
    Wtf.apply(this, {
        buttons: windowButtonArray
    }, config);
    Wtf.account.indonesiaVATReportWindow.superclass.constructor.call(this, config);
}
Wtf.extend(Wtf.account.indonesiaVATReportWindow, Wtf.Window, {
    onRender: function (config) {
        Wtf.account.SMTPAuthenticationwindow.superclass.onRender.call(this, config);
        this.createFields();
        this.createForm();
        this.add({
            region: 'north',
            height: 20,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText('acc.indonesia.vat.report.window.header.export'), WtfGlobal.getLocaleText('acc.indonesia.vat.report.window.header.export'), "../../images/esetting.gif")
        }, {
            region: 'center',
            border: false,
            bodyStyle: 'background:#f1f1f1;',
            layout: 'fit',
            items: this.formPanel
        });
    },
    createFields: function () {
        this.startDate = new Wtf.ExDateFieldQtip({
            name: 'stdate',
            fieldLabel: WtfGlobal.getLocaleText("acc.common.from") + "*",
            format: WtfGlobal.getOnlyDateFormat(),
            allowBlank :false,
            value: WtfGlobal.getDates(true)
        });
        this.endDate = new Wtf.ExDateFieldQtip({
            name: 'enddate',
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to") + "*",
            format: WtfGlobal.getOnlyDateFormat(),
            allowBlank :false,
            value: WtfGlobal.getDates(false)
        });
    },
    createForm: function () {
        this.formPanel = new Wtf.form.FormPanel({
            bodyStyle: 'padding:15px',
            layout: 'form',
            height: 80,
            items: [this.startDate, this.endDate]
        });
    },
    saveData: function () {
        if (this.formPanel.form.isValid()) {
            if (this.startDate.getValue() > this.endDate.getValue()) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.msgbox.1")], 2);
                return;
            }
            this.close();
            var url = this.params.reportStoreURL + "?";
            var parameters = "&startdate=" + WtfGlobal.convertToGenericStartDate(this.startDate.getValue());
            parameters += "&enddate=" + WtfGlobal.convertToGenericEndDate(this.endDate.getValue());
            parameters += "&filename=" + WtfGlobal.getLocaleText("acc.statutoryPanel.indonesia.vat.out.report");
            parameters += "&filetype=xls" ;
            parameters += "&get=13366" ;
            Wtf.get('downloadframe').dom.src = url + parameters;
        }
    },
    closeWin: function () {
        this.close();
    }
});
/**
 * Create Indonesia VAT Report object and add it to mainPanel
 */
function indonesiaVATReport(params) {
    if (params) {
        var VATReportObject = Wtf.getCmp(params.reportID);
        if (VATReportObject == null) {
            VATReportObject = new Wtf.account.indonesiaVATReportWindow({
                title: Wtf.util.Format.ellipsis(params.title),
                tabTip: params.titleQtip,
                id: params.reportID,
                closable: true,
                border: false,
                params: params,
                resizable: false,
                layout: 'fit',
                iconCls: 'accountingbase receivepaymentreport'
            });
        }
        VATReportObject.show();
    }
}