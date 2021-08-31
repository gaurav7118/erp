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
Wtf.UploadFile = function(config) {
    Wtf.apply(this, config);

    Wtf.UploadFile.superclass.constructor.call(this,{
        title: WtfGlobal.getLocaleText("doc.upload.title"),//"Upload Document",
        id: "uploadFileWindow",
        modal: true,
        resizable: false,
        iconCls : 'pwnd favwinIcon',
        width : 400,
        height: 100,
        buttonAlign : 'right',
        items: [this.uploadWin=new Wtf.form.FormPanel({
            frame:true,
            method : 'POST',
            fileUpload : true,
            waitMsgTarget: true,
            //            url: "crm/common/Document/addDocuments.do?fileAdd=true&mapid="+this.mapid+"",
            url: "ACCDocumentCMN/attachDocuments.do?fileAdd=true&mapid="+this.mapid+"&recid="+this.recid+"&moduleid="+this.scope.moduleID,
            id: this.id+"fileUploadFromPanel",
            border: false,
            items: [{
                border: false,
                id: "fileAddressField",
                xtype: "textfield",
                inputType: 'file',
                fieldLabel: WtfGlobal.getLocaleText("acc.field.Document*"),//"Document",
                name: "document"
            },{
                xtype: "hidden",
                name: "refid",
                value: this.recid
            }]
        })],
        buttons: [{
            text:  WtfGlobal.getLocaleText("acc.uploadbtn"),//"Upload",
            handler:function()
            {
                var idx = this.idX;
                var keyid = this.keyid;
                //            var grid = this.grid;
                var fname=Wtf.getCmp("fileAddressField").getValue();
                var upwin=Wtf.getCmp("uploadFileWindow");
                if(Wtf.getCmp("fileAddressField").getValue() != ""){
                    Wtf.commonWaitMsgBox("Adding file...");
                    this.uploadWin.form.submit({
                        params: {
                            flag: 83,
                            type: 1
                        },
                        scope:this,
                        success: function(a,b,c){
                            Wtf.updateProgress();
                            upwin.close();
                            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.document.added"));
                            var commentlist = getDocsAndCommentList('', this.scope.moduleID,this.idX,undefined,this.isCustomer?'Customer':'Vendor',undefined,"email",'leadownerid',this.contactsPermission,0,this.recid);
                        },
                        failure: function(){
                            ResponseAlert(42);
                            upwin.close();
                            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.document.failure"));
                        }
                    });
                } else {
                    ResponseAlert(43);
                }
            },
            scope: this
        },{
            text: WtfGlobal.getLocaleText("acc.common.cancelBtn"),//"Cancel",
            scope:this,
            handler: function(){
                this.close();
            }
        }]
    });
}

Wtf.extend(Wtf.UploadFile, Wtf.Window, {

    });


Wtf.attachFile = function(config) {
    Wtf.apply(this, config);

    Wtf.attachFile.superclass.constructor.call(this,{
        title: WtfGlobal.getLocaleText("crm.upload.title"),//"Upload Document",
        id: "uploadFileWindow",
        modal: true,
        resizable: false,
        iconCls : 'pwnd favwinIcon',
        width : 400,
        height: 100,
        buttonAlign : 'right',
        items: [this.uploadWin=new Wtf.form.FormPanel({
            frame:true,
            method : 'POST',
            fileUpload : true,
            waitMsgTarget: true,
            url: "Common/MailIntegration/mailIntegrate.do?module=Emails&action=EmailUIAjax&emailUIAction=uploadAttachment&to_pdf=true",
            id: this.id+"fileUploadFromPanel",
            border: false,
            items: [{
                border: false,
                id: "fileAddressField",
                xtype: "textfield",
                inputType: 'file',
                fieldLabel: WtfGlobal.getLocaleText("crm.importlog.header.file"),
                name: "email_attachment"
            }]
        })],
        buttons: [{
            text:WtfGlobal.getLocaleText("crm.uploadbtn"),// "Upload",
            handler:function()
            {
                var upwin=Wtf.getCmp("uploadFileWindow");
                if(Wtf.getCmp("fileAddressField").getValue() != ""){
                    Wtf.commonWaitMsgBox("Attaching file...");
                    this.uploadWin.form.submit({
                        params: {
                            module : 'Emails',
                            action : 'EmailUIAjax',
                            emailUIAction : 'uploadAttachment',
                            to_pdf : true
                        },
                        scope:this,
                        success: function(a,b,c){
                            Wtf.updateProgress();
                            var obj = eval('('+b.response.responseText+')');
                            if(obj.success) {
                                var data = eval('('+obj.data.data+')');
                                if(data.errmsg) {
                                    Wtf.MessageBox.alert(WtfGlobal.getLocaleText("crm.uploadwin.filesixemsgtitle"), data.errmsg);
                                } else {
                                    var info = eval('('+obj.data.data+')');
                                    this.scope.onSuccessAttached(info, true);
                                }
                            }
                            upwin.close();
                        },
                        failure: function(){
                            ResponseAlert(42);
                            upwin.close();
                        }
                    });
                } else {
                    ResponseAlert(43);
                }
            },
            scope: this
        },{
            text: WtfGlobal.getLocaleText("crm.CANCELBUTTON"),//"Cancel",
            scope:this,
            handler: function(){
                this.close();
            }
        }]
    });
}

Wtf.extend(Wtf.attachFile, Wtf.Window, {

    });
