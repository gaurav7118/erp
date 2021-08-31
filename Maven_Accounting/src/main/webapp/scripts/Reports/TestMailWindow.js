/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 */

function callTestMailWindowDynamicLoad() {
    var win = new Wtf.account.TestMailWindow({
        title: WtfGlobal.getLocaleText("acc.field.testEmail"),
        tabTip: WtfGlobal.getLocaleText("acc.field.testEmail"),
        id: "testMailWindow",
        layout: 'border',
        bodyStyle: 'background-color:#f1f1f1',
        width: 700,
        height: (Wtf.isIE ? 545 : 497),
        iconCls: getButtonIconCls(Wtf.etype.deskera),
        modal: true,
        autoScroll: true,
        closable: true,
        resizable: false,
        buttonAlign: 'right',
        renderTo: document.body
    });
    
    win.show();
}

Wtf.account.TestMailWindow = function(config) {
    Wtf.apply(this, config);
    
    this.sendBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.MailWin.send"), // 'Send'
        minWidth: 75
    });
    this.sendBtn.on('click', this.handleSend, this);
    
    this.closeBtn = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.close"), // 'Close'
         minWidth: 75
    });
    this.closeBtn.on('click', this.handleClose, this);
    
    Wtf.apply(this, {
        title: WtfGlobal.getLocaleText("acc.field.testEmail"),
        buttons: [this.sendBtn, this.closeBtn]
    }, config);
    
    Wtf.account.TestMailWindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.TestMailWindow, Wtf.Window, {
    onRender: function(config) {
        this.createForm();
        
        this.add({
            region: 'center',
            border: false,
            baseCls: 'bckgroundcolor',
            items: this.Form
        });
        Wtf.account.TestMailWindow.superclass.onRender.call(this, config);
    },
    
    createForm: function() {
        this.To = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText("acc.common.to") + "*",
            emptyText: ',',
            name: "to",
            allowBlank: false,
            tabIndex: 1,
            minChars: 1,
            anchor: '95%',
            validator: WtfGlobal.validateMultipleEmail
        });
        
        this.EmailMessage = new Wtf.Panel({
            border: false,
            xtype: 'panel',
            bodyStyle: 'padding:0px 0px 10px 65px;',
            html: '<font color="#555555">' + WtfGlobal.getLocaleText("acc.field.Givecomma,aftereachemailaddresstosendmailtoseverals") + '</font>'
        });
        
        this.Subject = new Wtf.form.TextField({
            name: "subject",
            allowBlank: false,
            fieldLabel: WtfGlobal.getLocaleText("acc.MAilWin.sub"), // "Subject",
            maxLength: 100,
            anchor: '95%'
        });
        var initialSubject = WtfGlobal.getLocaleText("acc.field.testEmail") + " - " + subdomain;
        this.Subject.setValue(initialSubject);
        
        this.Message = new Wtf.newHTMLEditor({
            fieldLabel: WtfGlobal.getLocaleText("acc.MAilWin.message") + "*",  //"Message*",
            name: "message",
            id: 'bio',
            xtype: 'htmleditor',
            allowBlank: false,
            anchor: '95%',
            height: 300,
            border: false,
            enableLists: false,
            enableSourceEdit: false,
            enableAlignments: true,
            hideLabel: true
        });
        this.Message.setValue(WtfGlobal.getLocaleText("acc.testEmail.defaultMsg"));
        
        this.Form = new Wtf.form.FormPanel({
            height : 'auto',
            border: false,
            items: [{
                    layout: 'form',
                    bodyStyle: "background: transparent; padding: 20px;",
                    labelWidth: 60,
                    border: false,
                    items: [
                        this.To,
                        this.EmailMessage,
                        this.Subject,
                        this.Message
                    ] 
                }]
        });
    },
    
    handleSend: function() {
        this.sendBtn.disable();
        this.closeBtn.disable();
        
        if (!this.To.isValid() || this.To.getValue().trim() == "") {          
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.MAilWin.msg17")], 2);
            this.sendBtn.enable();
            this.closeBtn.enable();
            return;
        }
        
        if (this.Subject.getValue().trim() == "") {
            Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.MAilWin.msg18"),
            function(btn) {
                if (btn == "yes") {
                    this.sendMail();
                    return true;
                } else {
                    this.sendBtn.enable();
                    this.closeBtn.enable();
                    return false;
                }
            }, this);
        } else {
            this.sendMail();
        }
    },
    
    sendMail: function() {
        var email = "";
        email += this.To.getValue().trim();
        
        var rec = this.Form.getForm().getValues();
        rec.mailingDate = WtfGlobal.convertToGenericDate(new Date());
        rec.emailid = email;
        
        var url = "CommonFunctions/sendTestMail.do";
        Wtf.Ajax.requestEx({
            url: url,
            params: rec
        }, this, this.successResponse, this.failureResponse);
    },
    
    successResponse: function(response) {
        if (response.success) {
            WtfComMsgBox([WtfGlobal.getLocaleText("acc.field.testEmail"), response.msg], 3);
            this.handleClose();
        } else {
            if (response.msg && response.isMsgSizeException) {
                WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"),response.msg], 2);        	 
            } else {
                WtfComMsgBox([this.label,WtfGlobal.getLocaleText("acc.rem.210")],3);
            }
            this.sendBtn.enable();
            this.closeBtn.enable();
        }
    },
    
    failureResponse: function(response) {
        var msg = WtfGlobal.getLocaleText("acc.common.msg1"); // "Failed to make connection with Web Server";
        if (response.msg) {
            msg = response.msg;
        }
        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), msg], 2);
        this.handleClose();
    },
    
    handleClose: function() {
        this.close();
    }
});