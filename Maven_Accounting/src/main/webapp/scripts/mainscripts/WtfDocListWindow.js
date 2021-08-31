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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

//-----------Chat List Window-----------------
//--------Add to Window------------------
Wtf.DocListWindow = function(config){

    Wtf.apply(this, config);
    Wtf.DocListWindow.superclass.constructor.call(this, config);
};


Wtf.extend(Wtf.DocListWindow, Wtf.Window, {
    initComponent: function(){
        Wtf.DocListWindow.superclass.initComponent.call(this);
    },

    onRender: function(config){
        Wtf.DocListWindow.superclass.onRender.call(this, config);

        this.innerpanel = this.add(new Wtf.Panel({
            layout: 'fit',
            autoScroll:true,
            border:false
        }));

        this.on('show',function(){
            this.fireAjax(this.recid);
   
        },this);


    },
    fireAjax:function(id){

        Wtf.Ajax.requestEx({
            method:'POST',
            url: this.url,
            params:{
                id:id,
                taskid:this.taskid,
                start:0,
                limit:100,
                attachmentIds:this.attachmentIds
            }
        },
        this,
        function(result, req){
            this.showChatList(result);
        },
        function(result, req){
            msgBoxShow(13,0,1);
        }
        );

    },
    addChat:function(userid,username){
        this.usernames.push(username);
        this.userIds.push(userid);
        if(this.innerpanel!=null){
            this.setHeight(this.getSize().height + 20);
            var addPanel = new Wtf.Panel({
                autoHeight: true,
                border:false,
                id:"minChat"+userid,
                html: "<div onclick= \"openChatWin('" + userid + "','"+ this.id + "')\" style='cursor:pointer;'>" +
                "<img src='../../images/Chat.png' style='vertical-align:top;' >" +
                '<span>'+' '+username+'<span>'+
                '</div>'
            })
            this.innerpanel.add(addPanel);
            this.doLayout();

        }
       
    },
    showChatList:function(result){
        
        this.height=100;
        this.setHeight(this.height);
        this.autoScroll=true;
        var cnt = 0;
        var deletedoc=WtfGlobal.getLocaleText("el.deletedoc");
        var reportGridId='';
        var isbatch=false;
        if(this.reportGridId)             //ERP-13011 [SJ]
        {
            reportGridId=this.reportGridId;     
        }
        if(this.isbatch!=undefined && WtfGlobal.convertStringToBoolean(this.isbatch))             //ERP-13011 [SJ]
        {
            isbatch=WtfGlobal.convertStringToBoolean(this.isbatch);     
        }
        for(var i=0;i<result.count;i++){
            this.height=this.height + 20;
            this.setHeight(this.height);
            var selNames = "";
            var html="";
            var selIds = "";
            var img="";
            var fileExt = "";
            var isQuotationSyncedFromCrm=false;                       //declare false to CRM quotation synchronozation flag
            if (result.data[i].docname.indexOf(".") > -1)
                fileExt = result.data[i].docname.substr(result.data[i].docname.lastIndexOf("."));
            if(this.moduleid==Wtf.Acc_Customer_Quotation_ModuleId && result.data[i].crmdocumentid!="")     //check for module id for Customer Quotation and crmdocumentid contains value
            {
                selNames +=result.data[i].crmdocumentid +fileExt;     //saving crm document id and extension for document uploaded from CRM side
                isQuotationSyncedFromCrm=true;                        //Quotation Synchronized from CRM 
            }
            else{
            selNames +=result.data[i].docid +fileExt;                  //File name if document was uploaded from ERP side
            }
            
            img='<div class="pwndbar2 downloadDoc" style="padding: 0px 3px 0px 18px;"/></div>';
            var img1='<div class="pwndbar2 deleteDoc"  style="padding: 0px 3px 0px 14px;"/></div>';
            var viewImg='<div class="pwndbar2 viewDoc"  style="padding: 0px 3px 0px 18px;"/></div>';
            var img2 = "<a href='#' style='text-decoration:none; float:right'  title="+WtfGlobal.getLocaleText("acc.common.view")+" onclick='viewAgreementdocs(\""+this.delurl+result.data[i].docid+"\",\""+result.data[i].docid+"\", \""+result.data[i].docname+"\", \""+this.dispto+"\");'><img style=\"padding: 0px 3px 0px 3px;\" src=\"./images/eye.gif \" /></a>";
            var viewDoc = "";
            if(fileExt.toLocaleLowerCase()==".gif" || fileExt.toLocaleLowerCase()==".png" || fileExt.toLocaleLowerCase()==".jpeg" || fileExt.toLocaleLowerCase()==".jfif"|| fileExt.toLocaleLowerCase()==".jpe" || fileExt.toLocaleLowerCase()==".jpg" || fileExt.toLocaleLowerCase()==".bmp" || fileExt.toLocaleLowerCase()==".pdf" || fileExt.toLocaleLowerCase()==".txt" ){
                viewDoc ="<a href='#' style='float:right'  title='"+WtfGlobal.getLocaleText("acc.invoiceList.viewinnewtab")+"' onclick='openDldUrl(\"" + "../../fdownload.jsp?url=" + selNames + "&view=true&dtype=inline" +"&docname="+result.data[i].docname+"&storeindex="+result.data[i].storeindex + "\")'>"+viewImg+"</a>"; 
            }
            //html="<span style='height:16px; width:16px;'>"+result.data[i].docname+"</span><a href='#' style='text-decoration:none'  title='Download' onclick='setDldUrl(\"" + "PropertyFileDownloadServlet.jsp?url=" + selNames + "&dtype=attachment" +"&docname="+result.data[i].docname+"&storeindex="+result.data[i].storeindex + "\")'>"+img+"</a>";
            if(this.statusID==4 && this.showleaves=='my'){ //if pending then delete on
                var obj ={};
                obj.url = this.delurl+result.data[i].docid;
                obj.docid = result.data[i].docid;
                obj.gridid=this.gridid;
                obj.isbatch= this.isDocReq==undefined?"":this.isDocReq;
                obj.rowIndex=this.docCount;
                obj.isFromReportGrid=true;
                html="<a href='#' id='deletedoc' style='text-decoration:none; float:right' title='"+WtfGlobal.getLocaleText("el.deletedoc")+"' onclick='deleteAttachDoc("+JSON.stringify(obj)+");'>"+img1+"</a>"
                +"<a href='#' style='text-decoration:none;float:right'  title='"+WtfGlobal.getLocaleText("el.downloaddoc")+"' onclick='setDldUrl(\"" + "../../fdownload.jsp?url=" + selNames + "&dtype=attachment" +"&docname="+result.data[i].docname+"&storeindex="+result.data[i].storeindex + "\")'>"+img+"</a>"
                +viewDoc
                +"<span style='height:16px; width:16px;'>"+(++cnt)+".&nbsp;"+result.data[i].docname+"</span>";
            }
            else {
                /*
                 * If read only case then show only attachments name
                 * In read only case Download and View attachments  // SDP-13344
                 */
                if(WtfGlobal.convertStringToBoolean(this.isReadOnly)) {
                    html = "<span style='height:16px; width:16px;'>" + (++cnt) + ".&nbsp;" + result.data[i].docname + "</span>"
                           +"<a href='#' style='float:right'  title='" + WtfGlobal.getLocaleText("acc.invoiceList.downloaddocument") + "' onclick='setDldUrl(\"" + "../../fdownload.jsp?url=" + selNames + "&dtype=attachment" + "&docname=" + result.data[i].docname + "&moduleid=" + this.moduleid + "&isQuotationSyncedFromCrm="+isQuotationSyncedFromCrm + "\")'>" + img + "</a>"
                           + viewDoc; // SDP-13344
                } else {
                    var obj ={};
                    obj.url = this.delurl+result.data[i].docid;
                    obj.docid = result.data[i].docid;
                    obj.gridid=reportGridId;
                    obj.isbatch= isbatch;
                    obj.rowIndex=this.rowIndex;
                    obj.isFromReportGrid=true;
                    html = "<a href='#' id='deletedoc' style='text-decoration:none;display:block; float:right;' title='" + WtfGlobal.getLocaleText("acc.invoiceList.deletedocument") + "' onclick='deleteAttachDoc("+JSON.stringify(obj)+");'>" + img1 + "</a>"  //ERP-13011 [SJ]
                            + "<a href='#' style='float:right'  title='" + WtfGlobal.getLocaleText("acc.invoiceList.downloaddocument") + "' onclick='setDldUrl(\"" + "../../fdownload.jsp?url=" + selNames + "&dtype=attachment" + "&docname=" + result.data[i].docname + "&moduleid=" + this.moduleid + "&isQuotationSyncedFromCrm="+isQuotationSyncedFromCrm + "\")'>" + img + "</a>"
                            + viewDoc
                            + "<span style='height:16px; width:16px;'>" + (++cnt) + ".&nbsp;" + result.data[i].docname + "</span>";
                }
            }
            //+"<a href='#' title='"+WtfGlobal.getLocaleText("acc.invoiceList.downloaddocument")+"' onclick='setDldUrl(\"" + "../../fdownload.jsp?url=" + selNames + "&dtype=attachment" +"&docname="+result.data[i].docname+"\")'><span style='height:16px; width:16px;'>"+(++cnt)+".&nbsp;"+result.data[i].docname+"</span></a>";
            var addPanel = new Wtf.Panel({
                //                               height:20,
                autoHeight: true,
                border:false,
                bodyStyle: "padding: 3px 5px 3px 5px; border-bottom: 1px dotted #A3BAE9;",
                id:"minChat"+result.data[i].docid,
                html: html
            })
            this.innerpanel.add(addPanel);

        }
        this.doLayout();
    },
    DownloadLink:function(a, b, c, d, e, f){
        var selNames = "";
        var selIds = "";
        var fileExt = "";

        if (c.data['docname'].indexOf(".") > -1)
            fileExt = c.data['docname'].substr(c.data['docname'].lastIndexOf("."));
        selNames += c.data['docid'] + fileExt;
        return "<a href='#' title="+WtfGlobal.getLocaleText("acc.field.Download") +"onclick='setDldUrl(\"" + "../../fdownload.jsp?url=" + selNames + "&dtype=attachment" +"&docname="+c.data['docname']+"&storeindex="+c.data['storeindex'] + "\")'><div class=getButtonIcon(Wtf.btype.dldiconwt) style='height:16px; width:16px;'></div></a>";
    }
    
});
//----------end of chat list window-------------
//
//function displayDocList(id, url, gridid, event,creatorid,approverid,docReq,cnt,statusid,showleaves,reportGridId){   
//   if(Wtf.getCmp('DocListWindow'))
//        Wtf.getCmp("DocListWindow").destroy();
//                new Wtf.DocListWindow({
//                                wizard:false,
//                                closeAction : 'hide',
//                                layout: 'fit',
//                                title:WtfGlobal.getLocaleText("acc.invoiceList.attachments"),
//                                shadow:false,
//                bodyStyle: "background-color: white",
//                                closable: true,
//                                width : 250,
//                                heigth:250,
//                                url: url,
//                gridid: gridid,
//                                modal:true,
//                                autoScroll:true,
//                                recid:id,
//                                delurl: "ACCInvoiceCMN/deleteDocument.do?docid=",
//                                id:"DocListWindow",
//                                docCount:cnt,
//                                isDocReq:docReq, 
//                                statusID:statusid, 
//                                showleaves:showleaves, 
//                                dispto:"pmtabpanel",
//                                reportGridId:reportGridId    //ERP-13011 [SJ]
//                            });
//
//   var docListWin = Wtf.getCmp("DocListWindow");
//   var leftoffset =event.pageX-200;
//
//   var topoffset = event.pageY+10;
//
//    if (document.all) {
//        xMousePos = window.event.x+document.body.scrollLeft;
//        yMousePos = window.event.y+document.body.scrollTop;
//        xMousePosMax = document.body.clientWidth+document.body.scrollLeft;
//        yMousePosMax = document.body.clientHeight+document.body.scrollTop;
//        leftoffset=xMousePos-200;//xMousePos;
//        topoffset=yMousePos+120;//yMousePos;
//       
//    }
//   if(docListWin.innerpanel==null||docListWin.hidden==true){
//       docListWin.setPosition(leftoffset, topoffset);
//
//       docListWin.show();
//   }else{
//       docListWin.hide();
//
//   }
//}

