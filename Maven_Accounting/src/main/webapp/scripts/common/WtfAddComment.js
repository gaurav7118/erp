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
Wtf.AddComment = function(config) {
    Wtf.apply(this, config);

    Wtf.AddComment.superclass.constructor.call(this,{
        title :  this.mode=="Edit"? WtfGlobal.getLocaleText("acc.common.editComment"):WtfGlobal.getLocaleText("acc.activitydetailpanel.addCommentBTN"),//"Add Comment" ,
        closable : true,
        modal : true,
        iconCls : 'pwnd favwinIcon',
        width : 570,
        height: 490,
        resizable :false,
        buttonAlign : 'right',
        buttons :[{
            text : this.mode=="Edit"? WtfGlobal.getLocaleText("acc.common.editComment"):WtfGlobal.getLocaleText("acc.activitydetailpanel.addCommentBTN"),
            scope : this,
            handler:function(){
                var jsondata ={};
                var commentid = this.commentId!=undefined?this.commentId:"";
                var moduleId=this.moduleId;
                var moduleName=this.moduleName;
                var recid=this.recid;
                this.Comment.syncValue()
                var str = this.Comment.getValue();
                if(str==""){
                   Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.comment.notblank"));
                    return;
                }
               jsondata = {
                                leadid : this.recid,
                                mapid : this.mapid,
                                commentid : commentid,
                                comment : str
                            };
                var commentStr=Wtf.encode(jsondata);
                Wtf.commonWaitMsgBox(WtfGlobal.getLocaleText("acc.common.addingcommentloadmsg"));//"Adding comment...");
                Wtf.Ajax.requestEx({
                    url:"ACCDocumentCMN/addComments.do",
                    params:{
                        jsondata:commentStr,
                        moduleId:this.moduleId,   //required for comet request
                        recid:this.recid,
                        moduleName:moduleName,
                        custcode:this.custcode
                    }
                },this,function(res,req){
                    this.close();
                    if(!this.isDetailPanel){

                        if(this.mode=="Add"){
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.comment.added")], 3);
//                            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.comment.added"));
                        }else
                            WtfComMsgBox([WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.comment.edited")], 3);
//                            Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.success"), WtfGlobal.getLocaleText("acc.comment.edited"));
                    }
//                    Wtf.updateProgress();
                     var commentlist = getDocsAndCommentList('', this.moduleId,this.idX,undefined,this.isCustomer?'Customer':'Vendor',undefined,"email",'leadownerid',this.contactsPermission,0,this.recid);

                }, 
                function(res,req) {
                    Wtf.updateProgress();
                    Wtf.Msg.alert(WtfGlobal.getLocaleText("acc.common.error"), WtfGlobal.getLocaleText("acc.comment.failure"));
                    
                });
            }
               
        },{
            text : WtfGlobal.getLocaleText("acc.common.cancelBtn"),//'Cancel',
            scope : this,
            handler : function() {
                this.close();
            }
        }],
        layout : 'border',
        items :[{
            region : 'north',
            height : 75,
            border : false,
            bodyStyle : 'background:white;border-bottom:1px solid #bfbfbf;',
            html :getTopHtml(WtfGlobal.getLocaleText("acc.defaultheader.comment"),this.mode=="Edit"?WtfGlobal.getLocaleText("acc.common.editComment"):WtfGlobal.getLocaleText("acc.activitydetailpanel.addCommentBTN"),"../../images/comment.png")
        },
            this.createCourseForm = new Wtf.form.FormPanel({
                baseCls: 'x-plain',
                region : 'center',
                border : false,
                layout:'fit',
                bodyStyle : 'background:#f1f1f1;font-size:10px;padding:20px 20px 20px 20px;',
                lableWidth : 150,
                autoScroll:false,
                defaultType: 'textfield',
                items : [
                this.Comment = new Wtf.newHTMLEditor({
                    border: false,
                    enableLists: false,
                    enableSourceEdit: false,
                    enableAlignments: true,
                    hiddenflag:false,
                    hideLabel: true
                    
                })]
            })]
    });
    if(this.mode=="Edit" && this.comment!=undefined){
        this.Comment.setValue(this.comment);
    }

}
 
Wtf.extend(Wtf.AddComment, Wtf.Window, {

    });


