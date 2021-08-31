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

Wtf.account.SMTPAuthenticationwindow = function(config) {
    var btnArr = [];

    btnArr.push(this.save = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.saveBtn"), //'Save',
        scope: this,
        handler: this.saveData.createDelegate(this)
    }))
    btnArr.push(this.cancel = new Wtf.Toolbar.Button({
        text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),
        scope: this,
        handler: this.closeWin.createDelegate(this)
    }))
    Wtf.apply(this, {
        buttons: btnArr
    }, config);
    Wtf.account.SMTPAuthenticationwindow.superclass.constructor.call(this, config);
}

Wtf.extend(Wtf.account.SMTPAuthenticationwindow, Wtf.Window, {
    onRender: function(config) {
        Wtf.account.SMTPAuthenticationwindow.superclass.onRender.call(this, config);

        this.createFields();
        this.createForm();
        this.add({
            region: 'north',
            height: 75,
            border: false,
            bodyStyle: 'background:white;border-bottom:1px solid #bfbfbf;',
            html: getTopHtml(WtfGlobal.getLocaleText('acc.field.SMTPAuth.header'), WtfGlobal.getLocaleText('acc.field.SMTPAuth.helptext'), "../../images/esetting.gif")
        }, {
            region: 'center',
            border: false,
            bodyStyle: 'background:#f1f1f1;',
            layout: 'fit',
            items: this.formP
        });
    },
    loadRecord: function(store) {
        if (store.getCount() > 0) {
            var rec = store.data.items[0].data;
            this.smtpuser.setValue(rec.smtpusername);
            this.smtppassword.setValue(rec.smtppassword);
            this.smtppath.setValue(rec.smtppath);
            this.smtpport.setValue(rec.smtpport);
        }
    },
    createFields: function() {

        this.storeRec = new Wtf.data.Record.create([
            {
                name: 'smtppath'
            },
            {
                name: 'smtpport'
            },
            {
                name: 'smtppassword'
            },
            {
                name: 'smtpusername'
            }
        ]);
        this.storeReader = new Wtf.data.KwlJsonReader({
            root: "data"
        }, this.storeRec);
        var storeUrl = "ACCCompanyPref/getSMTPAuthenticationDetails.do";
        this.store = new Wtf.data.Store({
            url: storeUrl,
            baseParams: {
                companyid: companyid
            },
            reader: this.storeReader
        });
        this.store.on('load', this.loadRecord, this);
        this.store.load();

        this.smtpuser = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText('acc.field.SMTPAuth.smtpuser'),
            allowBlank:false,
            name: 'smtpusername',
            msgTarget: 'side',
            maxLength: 254,
            labelStyle: 'width:150px;'
        });
        this.smtppassword = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText('acc.field.SMTPAuth.smtppass'),
            allowBlank:false,
            name: 'smtppassword',
            inputType: 'password',
            msgTarget: 'side',
            maxLength: 254,
            labelStyle: 'width:150px;'
        });
        this.smtppath = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText('acc.field.SMTPAuth.smtppath'),
            allowBlank:false,
            name: 'smtppath',
            msgTarget: 'side',
            maxLength: 254,
            labelStyle: 'width:150px;'
        });
        this.smtpport = new Wtf.form.TextField({
            fieldLabel: WtfGlobal.getLocaleText('acc.field.SMTPAuth.smtpport'),
            allowBlank:false,
            allowNegative: false,
            name: 'smtpport',
            msgTarget: 'side',
            maxLength: 254,
            labelStyle: 'width:150px;'
        });
    },
    createForm: function() {
        this.formP = new Wtf.form.FormPanel({
            bodyStyle: 'padding:15px',
            layout: 'form',
            url: "ACCCompanyPref/saveSMTPAuthenticationDetails.do",
            items: [this.smtpuser, this.smtppassword, this.smtppath, this.smtpport]
        });
    },
    saveData: function() {
        if (this.formP.form.isValid()) {
            this.formP.form.submit({
                scope: this,
                params: {
                    companyid: companyid,
                },
                success: function(result, action) {
                    var resultObj = eval('(' + action.response.responseText + ')');
                    if (resultObj.success) {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), resultObj.data.msg], 0);
                        this.close();
                    } else {
                        WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileprocessing")], 1);
                        this.close();
                    }
                },
                failure: function(frm, action) {
                    WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.alert"), WtfGlobal.getLocaleText("acc.field.Erroroccurredwhileprocessing")], 1);
                }
            });
        }
    },
    closeWin: function() {
        this.close();
    }
})