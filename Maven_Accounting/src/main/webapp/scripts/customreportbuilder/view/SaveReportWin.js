/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define('ReportBuilder.view.SaveReportWin', {
    extend:'Ext.window.Window',
    xtype:'savereport',
    initComponent: function() {
        this.reportNameField = new Ext.form.field.Text({
            fieldLabel: ExtGlobal.getLocaleText("acc.common.ReportName")+"*",
            xtype: 'textfield',
            name: 'reportName',
            labelWidth: 165,
            width: 385,
            maxLength: 50,
            allowBlank: false,
            validateBlank: true,
            vtype: 'reportnamevtype'
        });
        this.reportDescriptionField = new Ext.form.field.TextArea({
            fieldLabel:ExtGlobal.getLocaleText("acc.common.ReportDescription"),
                name: 'reportDesc',
                xtype: 'textarea',
                labelWidth: 165,
                width: 385,
                maxLength: 255
        });
        
        this.form = new Ext.form.FormPanel({
            padding: '15 15 15 15',
            bodyStyle: "background: transparent;",
            border: false,
            style: "background: transparent;padding:20px;",
            labelWidth: 165,
            items: [this.reportNameField, this.reportDescriptionField]
        });
        
        this.saveBtn = Ext.create('Ext.Button', {
            text: ExtGlobal.getLocaleText("acc.common.saveBtn"), //'Save',,
            itemId: 'save',
            scope: this,
            handler: this.saveHandler
        });
        
        this.saveBtn.on('click',function(){
            this.mask(ExtGlobal.getLocaleText("acc.common.load"));
        },this);
        
        this.cancelBtn=Ext.create('Ext.Button', {
            text: ExtGlobal.getLocaleText("acc.common.cancelBtn"),
            itemId: 'cancel',
            scope: this,
            handler: function() {
                this.hide();
            }
        });
        
        Ext.apply(this, {
            title: ExtGlobal.getLocaleText("acc.common.saveReport"),
            modal: true,
            iconCls: "pwnd favwinIcon",
            width: 540,
            height: 290,
            resizable: false,
            closable: false,
            constrain: true,
            layout: 'border',
            buttonAlign: 'right',
            items: [{
                region: 'north',
                height: 75,
                bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
                html: getTopHtmlReqField( ExtGlobal.getLocaleText("acc.common.saveReport"),  ExtGlobal.getLocaleText("acc.common.saveReport"), '../../images/save.png', 'HTML code and "\\\" character are not allowed')
            }, {
                region: 'center',
                border: false,
                bodyStyle: 'background:#f1f1f1;font-size:10px;border-top:1px solid #bfbfbf;',
                autoScroll: true,
                items: [this.form]
            }],
            buttons: [this.saveBtn,this.cancelBtn]
        });
        
        this.callParent(arguments);
    },
    saveHandler : function(){
        if (this.form.isValid()) {
            this.newreportpanelscope.reportName = this.reportNameField.getValue();
            this.newreportpanelscope.reportDescription = this.reportDescriptionField.getValue();
            this.newreportpanelscope.setTitle(this.reportNameField.getValue());
            this.newreportpanelscope.saveOrUpdateReport(this.isSaveAndCreateNew);
            this.close();
        } else { // display error alert if the data is invalid
            Ext.CustomMsg('Invalid Data', ExtGlobal.getLocaleText("acc.common.providevalidData"), Ext.Msg.INFO);
            this.unmask();
        }
    }
});
