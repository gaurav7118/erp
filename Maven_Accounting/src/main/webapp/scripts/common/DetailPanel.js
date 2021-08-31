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
Wtf.DetailPanel=function(config)
{
    config.id=config.id2+'AccountingupdownCompo';//CRMupdownCompo
    var readOnly=config.readOnly==true?true:false;
    var fourSpaces = "&nbsp; &nbsp; &nbsp; &nbsp;";
    var detailPanelClassName = "AccountingdetailPanel";//CRMdetailPanel
    var noDataStyle = "margin:3px;color:#000000;";
    var noDataImageDivStyle = "height:auto;display:block;overflow:auto; margin-left:17px;";
    var imgMidVA = "imgMidVA";
    var imgMidVAForDocAndComment = Wtf.isWebKit?"":"imgMidVA";
    //    var recentActivityImagePath = "../../images/recent-Activity.gif";
    //    var recentActivityImageLink = "<a class='"+detailPanelClassName+"' href='#gotoActivity"+config.id2+"' > <img src='"+recentActivityImagePath+"' class='"+imgMidVA+"'/></a>";
    //    var recentActivityLink = "<a class='"+detailPanelClassName+"' href='#gotoActivity"+config.id2+"' wtf:qtip='"+WtfGlobal.getLocaleText({
    //        key:"acc.otherdetail.recentactivity.ttip",
    //        params:[config.moduleName]
    //        })+"'>"+ WtfGlobal.getLocaleText("acc.otherdetail.recentactivity")+"</a>";

    var documentImageLink = "<a class='"+detailPanelClassName+"' href='#gotoDocuments"+config.id2+"' > <img src='../../images/add_file_1.gif' class='"+imgMidVA+"'/></a>";
    var documentLink = "<a class='"+detailPanelClassName+"' href='#gotoDocuments"+config.id2+"' wtf:qtip='"+WtfGlobal.getLocaleText({
        key:"acc.otherdetail.uploadfilehdr.files.ttip",
        params:[config.moduleName]
    })+"'>"+ WtfGlobal.getLocaleText("acc.otherdetail.uploadfilehdr.files")+"</a>";

    var commentImagePath = "../../images/add_comment.png";
    var commentImageLink = "<a class='"+detailPanelClassName+"' href='#gotoComments"+config.id2+"' > <img  src='"+commentImagePath+"' class='"+imgMidVA+"'/></a>";
    var commentLink = "<a class='"+detailPanelClassName+"' href='#gotoComments"+config.id2+"' wtf:qtip='"+WtfGlobal.getLocaleText({
        key:"acc.otherdetail.comments.ttip",
        params:[config.moduleName]
    })+"'>"+ WtfGlobal.getLocaleText("acc.otherdetail.comments")+"</a>";

    //    var subOwnerImageLink = "<a class='"+detailPanelClassName+"' href='#gotoSubOwners"+config.id2+"' > <img src='../../images/owner.gif' class='"+imgMidVA+"'/></a>";
    //    var subOwnerLink = "<a class='"+detailPanelClassName+"' href='#gotoSubOwners"+config.id2+"' wtf:qtip='"+WtfGlobal.getLocaleText({
    //        key:"crm.otherdetail.modowner.ttip",
    //        params:[config.moduleName]
    //        })+"'> "+WtfGlobal.getLocaleText({
    //        key:"crm.otherdetail.modowner",
    //        params:[config.moduleName]
    //        })+"</a>";

        
    var contactsHeader="";
    /*if(config.moduleName=="Account" || config.moduleName=="Contact" ||config.moduleName=="Opportunity" ||config.moduleName=="Lead" || config.moduleName==Wtf.ItemORProduct) {
        config.panTitle = "<div class='dpanellinks' id='topPanel"+config.id2+"'> &nbsp; &nbsp; &nbsp;\n\
                               "+documentImageLink+" \n\
                               "+documentLink+"  "+fourSpaces+"  \n\
                               "+commentImageLink+" \n\
                               "+commentLink+"   "+fourSpaces+"  \n\
                               "+recentActivityImageLink+" \n\
                               "+recentActivityLink+" "+fourSpaces+"  \n\ &nbsp; &nbsp; ";
 //                              "+subOwnerImageLink+" \n\
  //                             "+subOwnerLink+" &nbsp; &nbsp; ";
        if(config.moduleName != "Contact" && config.contactsPermission){
            contactsHeader="&nbsp; &nbsp;  \n\
                <a class='"+detailPanelClassName+"' href='#gotoContacts"+config.id2+"' > <img src='../../images/contactimage.png' class='"+imgMidVA+"'/></a> \n\
                <a class='"+detailPanelClassName+"' href='#gotoContacts"+config.id2+"' wtf:qtip='"+WtfGlobal.getLocaleText("acc.otherdetail.contacts.ttip")+"'>"+WtfGlobal.getLocaleText("acc.CONTACT.plural")+"</a> &nbsp; &nbsp; ";
            config.panTitle+=contactsHeader;
            config.panContactsTitle = "<br><hr/><br><div ><a id='gotoContacts"+config.id2+"' ></a><span class='dpTitleHead'> <img src='../../images/contactimage.png' class='"+imgMidVA+"'/>&nbsp;"+WtfGlobal.getLocaleText("acc.CONTACT.plural")+" :  </span><br><br></div>";
            config.newContactsWithPerm = "<span  class='dpGray'>  &nbsp; &nbsp; <span style=\"color:#000000;  !important;\">{contacts}</span> </span> <br><br>";
            config.noContactsWithPerm = "<span style='"+noDataStyle+"'> <span id='{msgDiv}' style='"+noDataImageDivStyle+"'>No contacts have been added. <a href=\"#\" class='linkCls'  onclick='addContacts(\""+config.id+"\")'>"+WtfGlobal.getLocaleText("acc.detailpanel.addcontactmtytxt")+"</a></span></span><br><br>";
            config.noContacts = "<span style='"+noDataStyle+"'> <span id='{msgDiv}' style='"+noDataImageDivStyle+"'>"+WtfGlobal.getLocaleText("acc.detailpanel.addcontactnocontact")+"<div><br style='clear:both'/></div><div><br style='clear:both'/></div>";
        }
        config.panTitle+="</div> &nbsp;<br> <p><hr/></p>";
        config.panSubOwnersTitle = "<br><hr/><br><div ><a id='gotoSubOwners"+config.id2+"' ></a><span class='dpTitleHead'> <img src='../../images/owner.gif' class='"+imgMidVA+"'/> "+WtfGlobal.getLocaleText({
            key:"acc.otherdetail.modowner",
            params:[config.moduleName]
            })+":  </span><br><br></div>";
        config.newSubOwnersWithPerm = "<span  class='dpGray'>  &nbsp; &nbsp; <span style=\"color:#000000;  !important;\"><b>{mainOwner}</b>{owners}</span> </span> <br><br>";
    }else{
    */    
    config.panTitle = "<div style='padding-top: 10px;'  class='dpanellinks' id='topPanel"+config.id2+"'> &nbsp; &nbsp; &nbsp;\n\
                           "+documentImageLink+" \n\
                            "+documentLink+"  "+fourSpaces+"  \n\
                           "+commentImageLink+" \n\
                          "+commentLink+"   "+fourSpaces+" &nbsp; &nbsp; \n\
                           </div> &nbsp;<br><br> <p><hr/></p>";
    //}
    /*  config.panAccProjectsHeaderTitle=" &nbsp; &nbsp; &nbsp; \n\
                               "+documentImageLink+" \n\
                               "+documentLink+"  "+fourSpaces+"  \n\
                               "+commentImageLink+" \n\
                               "+commentLink+"   "+fourSpaces+"  \n\
                               "+recentActivityImageLink+" \n\
                               "+recentActivityLink+" "+fourSpaces+"  \n\
                               <a class='"+detailPanelClassName+"' href='#gotoaccProjects"+config.id2+"' > <img src='../../images/project.png' class='"+imgMidVA+"'/></a> \n\
                               <a class='"+detailPanelClassName+"' href='#gotoaccProjects"+config.id2+"' wtf:qtip='"+WtfGlobal.getLocaleText({
        key:"acc.detailpanel.projects.ttip",
        params:[config.moduleName]
        })+"'> "+WtfGlobal.getLocaleText("acc.detailpanel.projects")+"</a> &nbsp; &nbsp;  "+contactsHeader;
   */
    //    config.panAccProjectsTitle = "<br><hr/><br><div ><a id='gotoaccProjects"+config.id2+"' ></a><span class='dpTitleHead'> <img src='../../images/project.png' class='"+imgMidVA+"'/>&nbsp;" +WtfGlobal.getLocaleText("crm.detailpanel.projects")+" :  </span><br><br></div>";
    //    config.newAccProjectsWithPerm = "<span  class='dpGray'>  &nbsp; &nbsp; <span style=\"color:#15428B;  !important;\">{projectnames}</span> {deleteimg}</span> <br><br>";
    //    config.noAccProjectsWithPerm = "<span style='"+noDataStyle+"'> <span id='{msgDiv}' style='"+noDataImageDivStyle+"'>"+WtfGlobal.getLocaleText("acc.detailpanel.projects.noprojectmsg")+"<a href=\"#\" class='linkCls'  onclick='addAccProjects(\""+config.id+"\")'>Add a project now.</a></span></span><br><br>";
    if(config.detailPanelFlag){
        config.noAccProjectsWithPerm = "<span style='"+noDataStyle+"'> <span id='{msgDiv}' style='"+noDataImageDivStyle+"'>"+WtfGlobal.getLocaleText("acc.detailpanel.projects.noprojectmsg")+"</span></span><br><br>";
    }
    config.noAccProjects = "<span style='"+noDataStyle+"'> <span id='{msgDiv}' style='"+noDataImageDivStyle+"'>"+WtfGlobal.getLocaleText("acc.detailpanel.projects.noprojectmsg");

    config.panDocTitle = "<div><a id='gotoDocuments"+config.id2+"'></a><span class='dpTitleHead'> <img src='../../images/add_file_1.gif' class='"+imgMidVAForDocAndComment+"'/>&nbsp;"+WtfGlobal.getLocaleText("acc.detailpanel.uploadedfile.title")+"</span><br><br></div>";
    config.panCommTitle = "<br><hr/><br><div><a id='gotoComments"+config.id2+"'></a><span class='dpTitleHead'> <img src='"+commentImagePath+"' class='"+imgMidVAForDocAndComment+"'/>&nbsp;"+WtfGlobal.getLocaleText("acc.otherdetail.comments")+" :  </span><br></div>";
    //    config.panAuditTitle = "<br><hr/><br><div ><a id='gotoActivity"+config.id2+"' ></a><span class='dpTitleHead'> <img src='"+recentActivityImagePath+"' class='"+imgMidVA+"'/>&nbsp;"+WtfGlobal.getLocaleText("acc.otherdetail.recentactivity")+" :  </span><br><br></div>";
        
    //    if(config.moduleName == "Cases"){
    //        config.panChecklistTitle = "<br><hr/><br><div ><a id='gotoChecklist"+config.id2+"' ></a><span class='dpTitleHead'> <img src='"+recentActivityImagePath+"' class='"+imgMidVA+"'/>&nbsp;"+WtfGlobal.getLocaleText("acc.otherdetail.checklist")+" :  </span><br><br></div>";
    //        config.panAssignlistTitle = "<br><hr/><br><div ><a id='gotoAssignlist"+config.id2+"' ></a><span class='dpTitleHead'> <img src='"+recentActivityImagePath+"' class='"+imgMidVA+"'/>&nbsp;"+WtfGlobal.getLocaleText("acc.otherdetail.assignlist")+" :  </span><br><br></div>";
    //    }
    
    if(readOnly){
         config.newDocContentWithPerm = "<span class='dpGray'> &nbsp; &nbsp; &nbsp; {srno})\n\
                 <a style=\"color:#15428B;font-weight:bold; !important;\"href='javascript:void(0)' title='Click to Download {Name} ' onclick='setDldUrl(\"ACCDocumentCMN/downloadDocuments.do?url={docid}&mailattch=true&dtype=attachment\")'>{Name}</a>&nbsp;\n\
                 ( {Size} k) : Uploaded by <span style=\"color:#15428B; !important;\">{uploadedby}</span> , on {uploadedon} </span> <br><br> ";
         config.newDocContentWithNoPerm = "<span class='dpGray' >  &nbsp; &nbsp; &nbsp; {srno}) <span style=\"color:#15428B;font-weight:bold; !important;\"  > {Name} </span> ( {Size}K ) : Uploaded by <span style=\"color:#15428B;  !important;\">{uploadedby}</span> , on {uploadedon} </span> <br><br> ";
         config.newCommContentWithPerm = "<br/><span style=\"color:#15428B;  !important;padding-left:24px;\">  {addedby}</span> <span style=\"color:gray !important;\"> on {postedon} </span>:{comment}<br/>";
    } else {
         config.newDocContentWithPerm = "<span class='dpGray'> &nbsp; &nbsp; &nbsp; {srno})\n\
                 <a style=\"color:#15428B;font-weight:bold; !important;\"href='javascript:void(0)' title='Click to Download {Name} ' onclick='setDldUrl(\"ACCDocumentCMN/downloadDocuments.do?url={docid}&mailattch=true&dtype=attachment\")'>{Name}</a>&nbsp;\n\
                 {deleteimg} ( {Size} k) : Uploaded by <span style=\"color:#15428B; !important;\">{uploadedby}</span> , on {uploadedon} </span> <br><br> ";
         config.newDocContentWithNoPerm = "<span class='dpGray' >  &nbsp; &nbsp; &nbsp; {srno}) <span style=\"color:#15428B;font-weight:bold; !important;\"  > {Name} </span> ( {Size}K ) : Uploaded by <span style=\"color:#15428B;  !important;\">{uploadedby}</span> , on {uploadedon} </span> <br><br> ";
         config.newCommContentWithPerm = "<br/><span style=\"color:#15428B;  !important;padding-left:24px;\">  {addedby}</span> <span style=\"color:gray !important;\"> on {postedon} </span>:{deleteimg}{comment}  <br/>";
    }          
    
    config.newAuditContentWithPerm = "<span  class='dpGray'>  &nbsp; &nbsp; {action} by <span style=\"color:#000000;  !important;\">{user}</span> on {[WtfGlobal.dateTimeRendererTZ(new Date(values.time))]} , "+WtfGlobal.getLocaleText("acc.otherdetail.details")+": </span> {details}  <br><br>";  
//    config.noPerm = "<div style='"+noDataStyle+"padding-left:40px;'> <div id='{msgDiv}' style='"+noDataImageDivStyle+"'>"+ WtfGlobal.getLocaleText("acc.otherdetails.insufficientpermissiontoview")+"</div></div><br><br>";

    config.noDoc = "<span style='"+noDataStyle+"'> <span id='{msgDiv}' style='"+noDataImageDivStyle+"'>"+WtfGlobal.getLocaleText("acc.otherdetails.nofileuploadmsg");
    if(!readOnly) {
       config.noDoc += " <a href=\"#\" class='linkCls'  onclick='callAddDocument(\""+config.id+"\")'>"+WtfGlobal.getLocaleText("acc.otherdetails.adddocnowmsg")+"</a></span></span><br><br><br><br>";    
       config.attachDoc = "<br> <a href=\"#\" class='linkCls'  onclick='callAddDocument(\""+config.id+"\")'>"+WtfGlobal.getLocaleText("acc.otherdetails.adddocnowmsg")+"</a></span><br><br><br>";   
    }
    config.noComment = "<span style='"+noDataStyle+"'> <span id='{msgDiv}' style='"+noDataImageDivStyle+"'>"+WtfGlobal.getLocaleText("acc.otherdetails.nocommentsaddedmsg");
    if(!readOnly) {
       config.noComment += " <a href=\"#\" class='linkCls'  onclick='callAddComment(\""+config.id+"\")'>"+WtfGlobal.getLocaleText("acc.otherdetails.addcommentnow")+"</a></span></span><br><br><br><br><br><br>";    
       config.attachComment = "<br> <a href=\"#\" class='linkCls'  onclick='callAddComment(\""+config.id+"\")'>"+WtfGlobal.getLocaleText("acc.otherdetails.addcommentnow")+"</a></span><br><br><br><br><br>";
    }

    //    config.noActivity = "<span style='"+noDataStyle+"'> <span id='{msgDiv}' style='"+noDataImageDivStyle+"'>"+WtfGlobal.getLocaleText("acc.otherdetails.noactivitydonemsg")+"</span></span><br><br>";
    //    config.selectValid = "<div style='margin:3px;height:90%;width:90%;'> <div id='{msgDiv}' style='height: auto;display:block;overflow:auto; margin-left:17px;'>"+WtfGlobal.getLocaleText("crm.otherdetails.slevalrecmsg")+"</div></div>";
    config.Failed = "<div style='margin:3px;color:red;'> <div id='{msgDiv}' style='"+noDataImageDivStyle+"'>"+WtfGlobal.getLocaleText("acc.otherdetails.err.connectionmsg")+"</div></div>";
    //        
    //    if(config.moduleName == "Cases"){
    //        config.noChecklist = "<span style='"+noDataStyle+"'> <span id='{msgDiv}' style='"+noDataImageDivStyle+"'>"+WtfGlobal.getLocaleText("crm.otherdetails.nochecklistdonemsg")+"</span></span><br><br>";
    //        config.selectValid = "<div style='margin:3px;height:90%;width:90%;'> <div id='{msgDiv}' style='height: auto;display:block;overflow:auto; margin-left:17px;'>"+WtfGlobal.getLocaleText("crm.otherdetails.slevalrecmsg")+"</div></div>";
    //        config.Failed = "<div style='margin:3px;color:red;'> <div id='{msgDiv}' style='"+noDataImageDivStyle+"'>"+WtfGlobal.getLocaleText("crm.otherdetails.failedconnectionmsg")+"</div></div>";
    //
    //        config.noAssignlist = "<span style='"+noDataStyle+"'> <span id='{msgDiv}' style='"+noDataImageDivStyle+"'>"+WtfGlobal.getLocaleText("crm.otherdetails.noassignlistdonemsg")+"</span></span><br><br>";
    //        config.selectValid = "<div style='margin:3px;height:90%;width:90%;'> <div id='{msgDiv}' style='height: auto;display:block;overflow:auto; margin-left:17px;'>"+WtfGlobal.getLocaleText("crm.otherdetails.slevalrecmsg")+"</div></div>";
    //        config.Failed = "<div style='margin:3px;color:red;'> <div id='{msgDiv}' style='"+noDataImageDivStyle+"'>"+WtfGlobal.getLocaleText("crm.otherdetails.failedconnectionmsg")+"</div></div>";
    //    }
    
    //Error Messages
    //
    config.noRecordSelected = "<div style='margin:3px padding-left:40px;color:red;'> <div id='{msgDiv}' style='"+noDataImageDivStyle+"'>"+ WtfGlobal.getLocaleText("acc.otherdetails.err.norecordselected")+"</div></div><br><br>";
    config.moreThan1RecordSelected = "<div style='margin:3px padding-left:40px;color:red;'> <div id='{msgDiv}' style='"+noDataImageDivStyle+"'>"+ WtfGlobal.getLocaleText("acc.otherdetails.err.morerecordselected")+"</div></div><br><br>";
    config.recordNotSavedYet = "<div style='margin:3px padding-left:40px;'> <div id='{msgDiv}' style='"+noDataImageDivStyle+"color:red;'>"+ WtfGlobal.getLocaleText("acc.otherdetails.err.recordnotsavedyet")+"</div></div><br><br>";
       
    
    
    Wtf.DetailPanel.superclass.constructor.call(this,config);
    var acccode=this.acccode;
    var accid=this.accid;
}
Wtf.extend(Wtf.DetailPanel,Wtf.Panel,{
    layout: "fit",
    commentFailed : "<div style='margin:3px;height:20%;width:90%;'><div id='{msgDiv}' style='height: auto;display:block;overflow:auto; margin-left:17px;'>Failed to load Comments.</div></div>",
    docsFailed : "<div style='margin:3px;height:20%;width:90%;'><div id='{msgDiv}' style='height: auto;display:block;overflow:auto; margin-left:17px;'>"+WtfGlobal.getLocaleText("acc.otherdetails.failedtoloadfiles")+"</div></div>",
//    initialMsg : "<div style='margin:3px;height:90%;width:90%;'><div id='{msgDiv}' style='height: auto;display:block;overflow:auto; margin-left:17px;'>"+WtfGlobal.getLocaleText("acc.otherdetails.slevalrecmsg")+"</div></div>",
initialMsg : "<div style='margin:3px;height:90%;width:90%;color:red;'><div id='{msgDiv}' style='height: auto;display:block;overflow:auto; margin-left:17px;'>"+WtfGlobal.getLocaleText("acc.otherdetails.err.recordnotsavedyet")+"</div></div>",
    noPerm_up: "<span style=\"color:#000000; font-weight:bold; !important;\"> "+WtfGlobal.getLocaleText("acc.detailpanel.uploadedfile.title")+"</span><br><br><div style='margin:3px;height:20%;width:20%;'><div id='{msgDiv}' style='height: auto;display:block;overflow:auto; margin-left:10px;'>"+WtfGlobal.getLocaleText("acc.otherdetails.insufficientpermissiontoview")+"</div></div>",
    noPerm_com: "<span style=\"color:#15428B; font-weight:bold; !important;\"> Comments :  </span><br><br><div style='margin:3px;height:20%;width:20%;'><div id='{msgDiv}' style='height: auto;display:block;overflow:auto; margin-left:10px;'>"+WtfGlobal.getLocaleText("acc.otherdetails.insufficientpermissiontoview")+"</div></div>",
    
    initEvents : function(){
        Wtf.DetailPanel.superclass.initEvents.call(this);
        this.body.on('click', this.onClick, this);
    },

    onRender: function(config){
        Wtf.DetailPanel.superclass.onRender.call(this,config);
        this.toolItems = new Array();
        //        this.grid=Wtf.getCmp('cust_grid');
        this.messagePanelContentTemplate = new Wtf.Template(this.initialMsg);
        this.north = {
            region:'north',
            columnWidth:1,
            border: false,
            height:50,
            margins:'2 5 2 0',
            layout:'fit',
            html:this.panTitle
        };
        this.center = {
            region:'center',
            columnWidth:1,
            autoScroll:true,            
            height:148,
            // width:1228,
            //padding-right: 42,
            bodyStyle:"padding-right:10px;padding-left:10px;width:1228px;",
            layout:'fit',
            border: false,
            id:this.id2+"dloadpanelcenter",
            margins:'0 5 0 15',
            html: this.messagePanelContentTemplate.applyTemplate({
                msgDiv: "msgDiv_"

            })
        };

        this.dloadpanel= new Wtf.Panel({
            id:this.id+"downloadpanel",
            closable: true,
            split: true,
            border: false,
            bodyStyle: "background:#FFFFFF;border: solid 4px #5b84ba;",
            layout: "column",
            items: [this.north,this.center]
        });

        this.add(this.dloadpanel);
    },    
    addComment:function(commentId,comment){
//        this.chkModule();
        var mode = "Add";
        if(commentId!=undefined){
            mode = "Edit";
        }
        var commentWin = new Wtf.AddComment({
            idX:this.id2,
            mode: mode,
            comment : comment,
            commentId : commentId,
            grid:this.grid,
            recid:this.accid,
            keyid:this.keyid,
            mapid:this.mapid,
            moduleId:this.moduleID,
            moduleName:this.moduleName,
            custcode:this.acccode,
            //                store:this.Store,
            //                selectedRec:this.selectedRec,
            isDetailPanel:false
        });
        commentWin.show();
    },
    //    addProject:function(projectId,projectName){
    //        var s=this.grid.getSelectionModel().getSelections();
    //        if(s.length==1) {
    //            this.chkModule();
    //            var mode = "Add";
    //            if(projectId!=undefined){
    //                mode = "Edit";
    //            }
    //            var projectwin = new Wtf.AddAccProject({
    //                idX:this.id2,
    //                mode: mode,
    //                projectName : projectName,
    //                projectId : projectId,
    //                grid:this.grid,
    //                recid:this.selRecordid,
    //                keyid:this.keyid,
    //                mapid:this.mapid,
    //                module:this.moduleName,
    //                store:this.Store,
    //                selectedRec:this.selectedRec,
    //                isDetailPanel:false,
    //                contactsPermission:this.contactsPermission
    //            });
    //            projectwin.show();
    //        } else {
    //            WtfComMsgBox(400,0);
    //        }
    //    },
    deleteComment : function(commentId) {
        //        var s=this.grid.getSelectionModel().getSelections();
        //        if(s.length==1) {
//        this.chkModule();
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn){
            if (btn == "yes") {
                Wtf.commonWaitMsgBox(WtfGlobal.getLocaleText("acc.common.deletingcommentmsg"));//"Deleting comment...");
                Wtf.Ajax.requestEx({
                    url:"ACCDocumentCMN/deleteComment.do",
                    params:{
                        id:commentId,
                        moduleName:this.moduleName,
                        custcode:this.acccode
                    }
                },this,
                function(res) {
                    // update total count
                    //                        var selected = this.selectedRec;
                    //                        var rowindex = this.grid.getStore().indexOf(selected);
                    //                        var c = 0;
                    //                        if(selected.get("totalcomment")!=undefined){
                    //                            c = selected.get("totalcomment");
                    //                        }
                    //                        var count = parseInt(c, 10)-1;
                    //                        selected.set("totalcomment",count);
                    //                        this.grid.getSelectionModel().selectRow(rowindex);
                    // end
                        
                    Wtf.updateProgress();
                    Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.success"), res.msg);
                    //                         Wtf.getCmp('cust_grid').getStore().reload();   
                    getDocsAndCommentList(this.selectedRec,this.moduleID,this.id2, true,this.moduleName,this.profileFlag,'email','leadownerid','',0,this.accid);
                    
                },
                function(res) {
                    Wtf.updateProgress();
                    Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.error"), res.msg)
                });

                    
            }
        },this);
            
    //        } else {
    //            WtfComMsgBox(400,0);
    //        }
    },
    //    deleteProject : function(projectId) {
    //      var s= this.grid.getSelectionModel().getSelections();
    //      if(s.lenght == 1) {
    //          this.chkModule();
    //          Wtf.MessageBox().confirm(WtfGlobal.getLocaleText("crm.msgbox.CONFIRMTITLE"), WtfGlobal.getLocaleText("crm.confirmdeletemsg"), function(btn){
    //              if (btn == "yes") {
    //                  Wtf.commonWaitMsgBox(WtfGlobal.getLocaleText("crm.common.deletingcommentmsg"));
    //                  Wtf.Ajax.requestEx({
    //                        url:Wtf.req.springBase+"common/PROJECTIntegration/DeleteAccountProject.do",
    //                        params:{
    //                            projectid:projectId,
    //                            recid:this.selRecordid,
    //                            mapid:this.mapid
    //                            
    //                            
    //                        }
    //                    },this,
    //                    function(res) {
    //                        var selected = this.selectedRec;
    //                        var rowindex = this.grid.getStore().indexOf(selected);
    //                        var obj=null;
    //                        switch(this.moduleName){
    //                            case Wtf.crmmodule.lead:
    //                                obj=Wtf.getCmp(Wtf.moduleWidget.lead);
    //                                break;
    //                            case Wtf.crmmodule.campaign:
    //                                obj=Wtf.getCmp(Wtf.moduleWidget.campaign);
    //                                break;
    //                            case Wtf.crmmodule.account:
    //                                obj=Wtf.getCmp(Wtf.moduleWidget.account);
    //                                break;
    //                            case Wtf.crmmodule.contact:
    //                                obj=Wtf.getCmp(Wtf.moduleWidget.contact);
    //                                break;
    //                            case Wtf.crmmodule.opportunity:
    //                                opportunity=Wtf.getCmp(Wtf.moduleWidget.opportunity);
    //                                break;
    //                            case Wtf.crmmodule.cases:
    //                                obj=Wtf.getCmp(Wtf.moduleWidget.cases);
    //                                break;
    //                            case Wtf.crmmodule.activity:
    //                                obj=Wtf.getCmp(Wtf.moduleWidget.activity);
    //                                break;
    //                            case Wtf.crmmodule.topactivity:
    //                                obj=Wtf.getCmp(Wtf.moduleWidget.topactivity);
    //                                break;
    //                            case Wtf.crmmodule.product:
    //                                obj=Wtf.getCmp(Wtf.moduleWidget.product);
    //                                break;
    //                        }
    //                        if(obj!=null){
    //                            obj.callRequest("","",0);
    //                        } 
    //                        Wtf.refreshUpdatesAll();   
    //                        Wtf.updateProgress();
    //                        ResponseAlert(94);
    //                        getDocsAndCommentList(this.selectedRec, this.keyid,this.id2, true,this.moduleName,this.profileFlag,'email');
    //                    },
    //                    function(res) {
    //                        Wtf.updateProgress();
    //                        ResponseAlert(95);
    //                    });
    //              }
    //          }, this);
    //      }else {
    //            WtfComMsgBox(400,0);
    //        }
    //    },
    deleteDocument : function(documentId,documentName,isshared) {
        //        var s=this.grid.getSelectionModel().getSelections();
        //        if(s.length==1) {
//        this.chkModule();
        var url = "";
        if(isshared==1){
            url = "ACCDocumentCMN/deleteSharedDocuments.do";
        } else {
            url = "ACCDocumentCMN/deleteDocument.do";
        }        
        Wtf.MessageBox.confirm(WtfGlobal.getLocaleText("acc.common.confirm"), WtfGlobal.getLocaleText("acc.tax.msg4"), function(btn){
            if (btn == "yes") {
                Wtf.commonWaitMsgBox(WtfGlobal.getLocaleText("acc.common.deletingcommentmsg"));//"Deleting document...");
                Wtf.Ajax.requestEx({
                    url:url,
                    params:{
                        docid:documentId,
                        custcode:this.acccode,
                        moduleName:this.moduleName,
                        isshared:isshared
                    }
                },this,
                function(res) {
                    Wtf.updateProgress();
                    Wtf.MessageBox.alert(WtfGlobal.getLocaleText("acc.common.success"), res.msg);
//                    getDocsAndCommentList(this.selectedRec,this.moduleID,this.id2, true,this.moduleName,this.profileFlag,'email',this.accid);
                    getDocsAndCommentList(this.selectedRec,this.moduleID,this.id2, true,this.moduleName,this.profileFlag,'email','leadownerid','',0,this.accid);
                },
                function(res) {
                    Wtf.updateProgress();
                    ResponseAlert(95);
                });

                    
            }
        },this);
            
    //        } else {
    //            WtfComMsgBox(400,0);
    //        }
    },
//    showcontacts:function(){
//        var s=this.grid.getSelectionModel().getSelections();
//        if(s.length==1) {
//            this.chkModule();
//            showContacts(this.selectedRec,this.selRecordid,this.moduleScope,false);
//        } else {
//            WtfComMsgBox(400,0);
//        }
//    },

    //    addSubOwners:function(){
    //        var s=this.grid.getSelectionModel().getSelections();
    //        if(s.length==1) {
    //            this.chkModule();
    //            var url;
    //            this.ownerStore = getModuleOwnerStore(this.moduleName);
    //            if(this.moduleName == "Lead") {
    //                url = Wtf.req.springBase+'Lead/action/getExistingLeadOwners.do';
    //            } else if(this.moduleName == "Account") {
    //                url = Wtf.req.springBase+'Account/action/getExistingAccOwners.do';
    //            } else if(this.moduleName == "Contact") {
    //                url = Wtf.req.springBase+'Contact/action/getExistingContactOwners.do';
    //            } else if(this.moduleName == "Opportunity") {
    //                url = Wtf.req.springBase+'Opportunity/action/getExistingOppOwners.do';            
    //            } else if(this.moduleName == Wtf.ItemORProduct) {
    //                url = Wtf.req.springBase+'Product/action/getExistingProductOwners.do';
    //            }
    //            Wtf.Ajax.requestEx({
    //                //                    url: Wtf.req.base + 'crm.jsp',
    //                url : url,
    //                params:{
    //                    leadid:this.selRecordid,
    //                    module:this.moduleName,
    //                    common: 1,
    //                    flag:501/////////////flag for case
    //                }
    //            },this,
    //            function(res) {
    //                this.serverres = res;
    //                if(this.ownerStore.getCount()> 0) {
    //                    this.openAddSubOwners();
    //                } else {
    //                    this.ownerStore.load();
    //                    this.ownerStore.on("load",this.onOwnerStoreLoad,this)
    //                }
    //            },
    //            function(res) {
    //                
    //                });
    //
    //        } else {
    //            WtfComMsgBox(400,0);
    //        }
    //    },

    //    onOwnerStoreLoad : function() {
    //        this.openAddSubOwners();
    //        this.ownerStore.un("load",this.onOwnerStoreLoad,this);
    //    },
    //    openAddSubOwners : function() {
    //        new Wtf.AddSubOwners({
    //            idX:this.id2,
    //            grid:this.grid,
    //            recid:this.selRecordid,
    //            keyid:this.keyid,
    //            mapid:this.mapid,
    //            store:this.Store,
    //            ownerstore : this.ownerStore,
    //            module:this.moduleName,
    //            ownerid:this.ownerid,
    //            ownerinfo:this.serverres,
    //            selectedRec:this.selectedRec,
    //            isDetailPanel:false
    //        }).show();
    //    },
    //    
    //    addAccProjects : function(record){
    //        var s=this.grid.getSelectionModel().getSelections();
    //        if(s.length==1 || record) { 
    //            if(record){
    //                this.selRecordid = record.data.oppid;
    //                this.selectedRec = record;
    //            } else {
    //                this.chkModule();
    //            }
    //            var projectwin = new Wtf.AddAccProject({
    //                idX:this.id2,
    //                grid:this.grid,
    //                recid:this.selRecordid,
    //                keyid:this.keyid,
    //                mapid:this.mapid,
    //                store:this.Store,
    //                module:this.moduleName,
    //                selectedRec:this.selectedRec,
    //                isDetailPanel:false,
    //                contactsPermission:this.contactsPermission
    //            });
    //            projectwin.show();
    //        } else {
    //            WtfComMsgBox(400,0);
    //        }
    //    },
    addComments:function(dat){
        var commentWin = new Wtf.AddComment({
            idX:dat.id,
            grid:dat.grid,
            recid:dat.recid,
            keyid:dat.keyid,
            mapid:dat.mapid,
            store:dat.store,
            record:dat.record
        });
        commentWin.show();
    },
    addContacts : function(){
        Wtf.getCmp(this.id2).showcontacts();
    },
    Addfiles:function() {
        //        var s=Wtf.getCmp('cust_grid').getSelectionModel().getSelections();
        //        if(s.length==1) {
//        this.chkModule();
        var uploadDocWin = new Wtf.UploadFile({
            idX:this.id2,
            grid:this.grid,
            recid:this.accid,
            keyid:this.keyid,
            mapid:this.mapid,
            scope:this,
            moduleName:this.moduleName,
            isDetailPanel:false
        });
        uploadDocWin.show();
    },
    onClick: function(e, target){            
        if(target.className == 'AccountingdetailPanel'){
            e.stopEvent();
            this.scrollToSection(target.href.split('#')[1]);
        } else if(target.parentNode.className == 'AccountingdetailPanel') {
            e.stopEvent();
            this.scrollToSection(target.parentNode.href.split('#')[1]);
        }
    },
    scrollToSection : function(id){
        var el = Wtf.getDom(id);
        if(el){
            var body = Wtf.getCmp(this.center.id).body;
            var top = (Wtf.fly(el).getOffsetsTo(body)[1]) + body.dom.scrollTop;
            body.dom.scrollTop = top-15;
            Wtf.fly(el).next('span').pause(.2).highlight('#8DB2E3', {
                attr:'color'
            });            
        }
    }
    
});

function callAddComment(compoId) {
    Wtf.getCmp(compoId).addComment();
}

function callAddDocument(compoId){
    Wtf.getCmp(compoId).Addfiles();
}
//function addAccProjects(compoId){
//    Wtf.getCmp(compoId).addAccProjects();
//}
function addContacts(compoId){
    Wtf.getCmp(compoId).addContacts();
}

function callDeleteComment(compoId,commentId){
    Wtf.getCmp(compoId).deleteComment(commentId);
}

function callEditComment(compoId,commentId,comment){
    Wtf.getCmp(compoId).addComment(commentId,unescape(comment));
}
function callDeleteDocument(compoId,documentId,documentName, isshared){
    Wtf.getCmp(compoId).deleteDocument(documentId,documentName,isshared);
}
//function callDeleteProject(compoId,projectId){
//    Wtf.getCmp(compoId).deleteProject(projectId);
//}
//function callEditProject(compoId,projectId,projectName){
//    Wtf.getCmp(compoId).addProject(projectId,projectName);
//}