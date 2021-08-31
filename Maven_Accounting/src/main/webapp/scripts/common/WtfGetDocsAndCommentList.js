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
function getDocsAndCommentList(selected, moduleid, id, refresh,module, loadMask, email,ownerid,contactsPermission,selectionLengthFlag,accid,errorcode)
{
    var moduleObj=Wtf.getCmp(id);
    var commentlist="";
    var Recid="";
    var emailid="";
    //    if(selected) {
    //var selected = grid.getSelectionModel().getSelected();
    
    var updownCompE = Wtf.getCmp(id+'AccountingupdownCompo');
      var downUpPanel = Wtf.getCmp(id+"dloadpanelcenter");
    var panTitleTpl = new Wtf.XTemplate(  '' );
    panTitleTpl.overwrite(downUpPanel.body,{});
    var enableContactsButton=true;
    var isMainOwner = false;
    
    if(errorcode=="Error0"){
         DataTpl= new Wtf.XTemplate( updownCompE.noRecordSelected );
        DataTpl.append(downUpPanel.body, {});
        return;
    }
    else if(errorcode=="Error1"){
         DataTpl= new Wtf.XTemplate( updownCompE.moreThan1RecordSelected );
        DataTpl.append(downUpPanel.body, {});
        return;
    }
    else if(errorcode=="Error2"){
         DataTpl= new Wtf.XTemplate( updownCompE.recordNotSavedYet);
        DataTpl.append(downUpPanel.body, {});
        return;
    }
    if(selected){
        Recid = accid;//        Recid = selected.data.accid;
        emailid=selected.data.billingEmail1;
    }else{
        Recid=accid;
    }
            
    var mapid = Recid;
       if(selected || this.Recid!=""){    
        if(loadMask) {
            Wtf.commonWaitMsgBox("Retrieving "+module+" details...");
        }
        if(Wtf.getCmp("tempButton")!=undefined){
            var but=Wtf.getCmp("tempButton");
            but.setTooltip(module+" has been selected to Add/Modify owner");
        }
        Wtf.Ajax.requestEx({
            url:"ACCDocumentCMN/getDetails.do",
            //                    url:"Common/DetailPanel/getDetails.do",
            params:{
                recid:Recid,
                module:moduleid,
                email:emailid,
                flag:256,/////////////flag for case
                detailFlag:module == 'Lead' && loadMask ? true : null,
                mapid:mapid
            }
        },this,
        function(res) {
            if(loadMask) {
                Wtf.updateProgress();
            }
            if(selected){
                selected.data.dpcontent = res;
                if(selected.data.dpcontent.commData!=undefined)
                    commentlist = selected.data.dpcontent.commData.commList;
            }
            overwriteDetailPanel(res, updownCompE, downUpPanel, loadMask,contactsPermission, module);

        },
        function(res) {
            if(loadMask) {
                Wtf.updateProgress();
            }
            var tpl0= new Wtf.XTemplate(  updownCompE.Failed  );
            tpl0.overwrite(downUpPanel.body,{});

        });
    }
    else {
        if(selected.data.dpcontent.commData!=undefined)
            commentlist = selected.data.dpcontent.commData.commList;
        overwriteDetailPanel(selected.data.dpcontent, updownCompE, downUpPanel, loadMask,contactsPermission, module);
    }
    //        }
    //        else{
    //            disableButt(moduleObj);    
    //
    //            var tpl= new Wtf.Template("<div style='margin:3px;height:90%;width:90%;'>", "<div id='{msgDiv}' style='height: auto;display:block;overflow:auto; margin-left:10px;'>Please select a valid record to see the details.</div></div>");
    //            tpl.overwrite(Wtf.getCmp(id+"dloadpanelcenter").body,'');
    //        }
    //    }
    //    else if(moduleObj!=undefined){
    //        disableButt(moduleObj);
    //        var tpl1= new Wtf.Template("<div style='margin:3px;height:90%;width:90%;'>", "<div id='{msgDiv}' style='height: auto;display:block;overflow:auto; margin-left:10px;'>Please select a record to see the details.</div></div>");
    //        if(Wtf.getCmp(id+"dloadpanelcenter")!=undefined)
    //            tpl1.overwrite(Wtf.getCmp(id+"dloadpanelcenter").body,'');
    //    }
    return commentlist;
}
smileyStore = new Array(':)', ':(', ';)', ':D', ';;)', '&gt;:D&lt;', ':-/', ':x', ':&gt;&gt;', ':P', ':-*', '=((', ':-O', 'X(', ':&gt;', 'B-)', ':-S', '#:-S', '&gt;:)', ':((', ':))', ':|', '/:)', '=))', 'O:-)', ':-B', '=;', ':-c');
function smiley(tdiv, emoticon){
    tdiv.innerHTML = tdiv.innerHTML.replace(emoticon, '<img src=images/smiley' + (smileyStore.indexOf(emoticon) +1) + '.gif style=display:inline;vertical-align:text-top;></img>');
}

function parseSmiley(str){
    str = unescape(str);
    var tdiv = document.createElement('div');
    var arr = [];
    arr = str.match(/(:\(\()|(:\)\))|(:\))|(:x)|(:\()|(:P)|(:D)|(;\))|(;;\))|(&gt;:D&lt;)|(:-\/)|(:&gt;&gt;)|(:-\*)|(=\(\()|(:-O)|(X\()|(:&gt;)|(B-\))|(:-S)|(#:-S)|(&gt;:\))|(:\|)|(\/:\))|(=\)\))|(O:-\))|(:-B)|(=;)|(:-c)/g);
    if (arr == null) {
        tdiv.innerHTML = str;
    } else {
        var i;
        tdiv.innerHTML = str;
        for (i = 0; i < arr.length; i++) {
            smiley(tdiv, arr[i]);
        }
    }
    return tdiv.innerHTML;
}
function overwriteDetailPanel(res, updownCompE, downUpPanel, loadMask,contactsPermission, module) {
    
    /*  Create Image Div   */
    var DataTpl = new Wtf.XTemplate(  updownCompE.panImageTitle );
    DataTpl.overwrite(downUpPanel.body,{});
    DataTpl= new Wtf.XTemplate( updownCompE.newImageContentWithPerm );
    DataTpl.append(downUpPanel.body,{});
    
    /*  Create Item Usage Details   */
    if(res.roomData) {
        if(res.roomData.itemDetailsList!=undefined){
            DataTpl = new Wtf.XTemplate(  updownCompE.panItemDetailsTitle );
            DataTpl.append(downUpPanel.body,{});
            
            if(res.roomData.itemDetailsList.length==0){
                DataTpl= new Wtf.XTemplate( updownCompE.noItemDetailsContent );
                DataTpl.append(downUpPanel.body,{});
            } else {
                DataTpl= new Wtf.XTemplate(
                    '<table border="0" width="100%" style="margin-left:33px;padding-right:35%;padding-top:1%;">',
                    '<tr>',
                    '<td style="border-bottom: 2px dotted #cfcfcf;" align="center" width="15%"><span style=\"color:#15428B;  !important;\">Item Serial Number</td>',
                    '<td style="border-bottom: 2px dotted #cfcfcf;" align="center" width="15%"><span style=\"color:#15428B;  !important;\">Room Number</td>',
                    '<td style="border-bottom: 2px dotted #cfcfcf;" align="center" width="20%"><span style=\"color:#15428B;  !important;\">Room Name</td>',
                    '<td style="border-bottom: 2px dotted #cfcfcf;" align="center" width="15%"><span style=\"color:#15428B;  !important;\">Floor</td>',
                    '<td style="border-bottom: 2px dotted #cfcfcf;" align="center" width="15%"><span style=\"color:#15428B;  !important;\">Building Number</td>',
                    '<td style="border-bottom: 2px dotted #cfcfcf;" align="center" width="20%"><span style=\"color:#15428B;  !important;\">Building Name</td>',
                    '</tr>',
                    '</table>'
                    );
                DataTpl.append(downUpPanel.body,{});        
                
                DataTpl= new Wtf.XTemplate(
                    '<table border="0" width="100%" style="margin-left:33px;padding-right:35%;padding-top:1%;">',
                    '<tr>',
                    '<td style="border-bottom: 2px dotted #cfcfcf;" align="center" width="15%">{srno}</td>',
                    '<td style="border-bottom: 2px dotted #cfcfcf;" align="center" width="15%">{roomno}</td>',
                    '<td style="border-bottom: 2px dotted #cfcfcf;" align="center" width="20%">{roomname}</td>',
                    '<td style="border-bottom: 2px dotted #cfcfcf;" align="center" width="15%">{floor}</td>',
                    '<td style="border-bottom: 2px dotted #cfcfcf;" align="center" width="15%">{buildno}</td>',
                    '<td style="border-bottom: 2px dotted #cfcfcf;" align="center" width="20%">{buildname}</td>',
                    '</tr>',
                    '</table>'
                    );
                for(var i3 = 0; i3 < res.roomData.itemDetailsList.length; i3++) {
                    DataTpl.append(downUpPanel.body,res.roomData.itemDetailsList[i3]);
                }
                DataTpl= new Wtf.XTemplate("<br><br>");
                DataTpl.append(downUpPanel.body,{});
            }
        }
    }

    /*  Create Documents Div   */
    DataTpl = new Wtf.XTemplate(  updownCompE.panDocTitle );
    DataTpl.append(downUpPanel.body,{});
    if(res.docData) {
        if(res.docData.docList.length==0){
            DataTpl= new Wtf.XTemplate( updownCompE.noDoc );
            DataTpl.append(downUpPanel.body,{});
        }
        else {
            //            if(res.docData.docPerm) {
            DataTpl= new Wtf.XTemplate( updownCompE.newDocContentWithPerm );
            for( var i1 = 0; i1 < res.docData.docList.length; i1++ ) {
                if(res.docData.docList[i1].uploadedby==this._fullName)
                    res.docData.docList[i1]['deleteimg'] = "<img onclick ='callDeleteDocument(\""+updownCompE.id+"\",\""+res.docData.docList[i1].docid+"\",\""+res.docData.docList[i1].Name+"\",\""+res.docData.docList[i1].isshared+"\")' src='../../images/cancel_16.png' wtf:qtitle='Delete Document' wtf:qtip='Click to delete document.' style='height: 12px; width: 12px;float:right;cursor:pointer' >";
                DataTpl.append(downUpPanel.body,res.docData.docList[i1]);
            }
            DataTpl= new Wtf.XTemplate( updownCompE.attachDoc );
            DataTpl.append(downUpPanel.body,{});
        //            } else {
        //                DataTpl= new Wtf.XTemplate( updownCompE.newDocContentWithNoPerm );
        //                for( var i4 = 0; i4 < res.docData.docList.length; i4++ ) {
        //                    DataTpl.append(downUpPanel.body,res.docData.docList[i4]);
        //                }
        //            }
        }
    } else {
        DataTpl= new Wtf.XTemplate( updownCompE.noPerm );
        DataTpl.append(downUpPanel.body, {});
    }

    /*  Create Comments Div   */
    DataTpl = new Wtf.XTemplate(  updownCompE.panCommTitle );
    DataTpl.append(downUpPanel.body,{});
    if(res.commData) {
        //        if(res.commData.commPerm) {
        if(res.commData.commList.length==0){
            DataTpl= new Wtf.XTemplate( updownCompE.noComment );
            DataTpl.append(downUpPanel.body,{});
        } else {
            DataTpl= new Wtf.XTemplate(  updownCompE.newCommContentWithPerm  );
            for(var i2 = 0; i2 < res.commData.commList.length; i2++) {
                res.commData.commList[i2].comment = parseSmiley(unescape(res.commData.commList[i2].comment));
                if(res.commData.commList[i2].deleteflag)
                    res.commData.commList[i2]['deleteimg'] = "<img onclick ='callDeleteComment(\""+updownCompE.id+"\",\""+res.commData.commList[i2].commentid+"\")' src='../../images/cancel_16.png' wtf:qtitle='Delete Comment' wtf:qtip='Click to delete comment.' style='height: 12px; width: 12px;float:right;cursor:pointer'><img onclick ='callEditComment(\""+updownCompE.id+"\",\""+res.commData.commList[i2].commentid+"\",\""+escape(res.commData.commList[i2].comment)+"\")' src='../../images/comment_edit.png'  wtf:qtitle='Edit Comment' wtf:qtip='Click to edit comment.' style='padding : 0px 20px ;height: 12px; width: 12px;float:right;cursor:pointer'>";
                DataTpl.append(downUpPanel.body,res.commData.commList[i2]);
            }
            DataTpl= new Wtf.XTemplate(  updownCompE.attachComment  );
            DataTpl.append(downUpPanel.body,{});
        }
    //        } else {
    //            DataTpl= new Wtf.XTemplate(  updownCompE.newCommContentWithNoPerm  );
    //            DataTpl.append(downUpPanel.body, {});
    //        }
    } else {
        DataTpl= new Wtf.XTemplate( updownCompE.noPerm );
        DataTpl.append(downUpPanel.body, {});
    }

    /*  Create Recent Activity Div   */
    DataTpl = new Wtf.XTemplate(  updownCompE.panAuditTitle );
    DataTpl.append(downUpPanel.body,{});
    if(res.auditData) {
        if(res.auditData.auditList!=undefined){
            if(res.auditData.auditList.length==0){
                DataTpl= new Wtf.XTemplate( updownCompE.noActivity );
                DataTpl.append(downUpPanel.body,{});
            } else {
                DataTpl= new Wtf.XTemplate(  updownCompE.newAuditContentWithPerm  );
                for(var i3 = 0; i3 < res.auditData.auditList.length; i3++) {
                    res.auditData.auditList[i3].details = unescape(res.auditData.auditList[i3].details);
                    DataTpl.append(downUpPanel.body,res.auditData.auditList[i3]);
                }
            }
        }
    } else {
        DataTpl= new Wtf.XTemplate( updownCompE.noPerm );
        DataTpl.append(downUpPanel.body, {});
    }
    
    //    if(module == "Case"){
    //        /*  Create Checklist Div   */
    //        if(!Wtf.hideChecklistColumn){
    //            DataTpl = new Wtf.XTemplate(  updownCompE.panChecklistTitle );
    //            DataTpl.append(downUpPanel.body,{});
    //            if(res.auditChecklist) {
    //                if(res.auditChecklist.auditChecklistList!=undefined){
    //                    if(res.auditChecklist.auditChecklistList.length==0){
    //                        DataTpl= new Wtf.XTemplate( updownCompE.noChecklist );
    //                        DataTpl.append(downUpPanel.body,{});
    //                    } else {
    //                        DataTpl= new Wtf.XTemplate(  updownCompE.newAuditContentWithPerm  );
    //                        for(var i3 = 0; i3 < res.auditChecklist.auditChecklistList.length; i3++) {
    //                            res.auditChecklist.auditChecklistList[i3].details = unescape(res.auditChecklist.auditChecklistList[i3].details);
    //                            DataTpl.append(downUpPanel.body,res.auditChecklist.auditChecklistList[i3]);
    //                        }
    //                    }
    //                }
    //            } else {
    //                DataTpl= new Wtf.XTemplate( updownCompE.noPerm );
    //                DataTpl.append(downUpPanel.body, {});
    //            }
    //        }
    //        /*  Create Assignlist Div   */
    //        DataTpl = new Wtf.XTemplate(  updownCompE.panAssignlistTitle );
    //        DataTpl.append(downUpPanel.body,{});
    //        if(res.auditAssign) {
    //            if(res.auditAssign.auditAssignList!=undefined){
    //                if(res.auditAssign.auditAssignList.length==0){
    //                    DataTpl= new Wtf.XTemplate( updownCompE.noAssignlist );
    //                    DataTpl.append(downUpPanel.body,{});
    //                } else {
    //                    DataTpl= new Wtf.XTemplate(  updownCompE.newAuditContentWithPerm  );
    //                    for(var i3 = 0; i3 < res.auditAssign.auditAssignList.length; i3++) {
    //                        res.auditAssign.auditAssignList[i3].details = unescape(res.auditAssign.auditAssignList[i3].details);
    //                        DataTpl.append(downUpPanel.body,res.auditAssign.auditAssignList[i3]);
    //                    }
    //                }
    //            }
    //        } else {
    //            DataTpl= new Wtf.XTemplate( updownCompE.noPerm );
    //            DataTpl.append(downUpPanel.body, {});
    //        }
    /*  Create Total activity time Div   */
    //        DataTpl = new Wtf.XTemplate(  updownCompE.panTimeDiff );
    //        DataTpl.append(downUpPanel.body,{});
    //        if(res.totalTime) {
    //            if(res.totalTime.calculatedTotalTime!=undefined){
    //                if(res.totalTime.calculatedTotalTime=="0"){
    //                    DataTpl= new Wtf.XTemplate( updownCompE.NoAssigneeWorkedTime );
    //                    DataTpl.append(downUpPanel.body,{});
    //                } else {
    //                    DataTpl= new Wtf.XTemplate(  updownCompE.newTotalTime  );
    //                    for(var i3 = 0; i3 < res.totalTime.calculatedTotalTime.length; i3++) {
    //                        DataTpl.append(downUpPanel.body,res.totalTime.calculatedTotalTime[i3]);
    //                    }
    //                }
    //            }
    //        } else {
    //            DataTpl= new Wtf.XTemplate( updownCompE.noPerm );
    //            DataTpl.append(downUpPanel.body, {});
    //        }
    //    }
    
    //    if(res.subownersData) { /*  Create Owners Div   */
    //        DataTpl = new Wtf.XTemplate(  updownCompE.panSubOwnersTitle );
    //        DataTpl.append(downUpPanel.body,{});
    //
    //        var ownerList=res.subownersData.ownerList;
    //        if(ownerList.length==0){
    //            if(res.subownersData.addOwnerPerm) {
    //                DataTpl= new Wtf.XTemplate( updownCompE.noSubOwnersWithPerm );
    //                DataTpl.append(downUpPanel.body,{});
    //            }else{
    //                DataTpl= new Wtf.XTemplate( updownCompE.noSubOwnersWithNoPerm );
    //                DataTpl.append(downUpPanel.body,{});
    //            }
    //        }else{
    //            DataTpl= new Wtf.XTemplate(  updownCompE.newSubOwnersWithPerm  );
    //            for(i2 = 0; i2 < ownerList.length; i2++){
    //                ownerList[i2].owners = unescape(ownerList[i2].owners);
    //                DataTpl.append(downUpPanel.body,ownerList[i2]);
    //            }
    //        }
    //    }
    //    if(res.projData && isDemo) { /*  Create Account Projects Div   */
    //        DataTpl = new Wtf.XTemplate(  updownCompE.panAccProjectsTitle );
    //        DataTpl.append(downUpPanel.body,{});
    //
    //        var projList=res.projData.projList;
    //        if(projList.length==0){
    //            if(res.projData.addProjectPerm) {
    //                DataTpl= new Wtf.XTemplate( updownCompE.noAccProjectsWithPerm );
    //                DataTpl.append(downUpPanel.body,{});
    //            }else{
    //                DataTpl= new Wtf.XTemplate( updownCompE.noAccProjects );
    //                DataTpl.append(downUpPanel.body,{});
    //            }
    //        }else{
    //            DataTpl= new Wtf.XTemplate(  updownCompE.newAccProjectsWithPerm  );
    //            for(i2 = 0; i2 < projList.length; i2++){
    //                projList[i2].projectnames = unescape(projList[i2].projectnames);
    //                if(!res.projData.projList[i2].deleteflag)
    //                    res.projData.projList[i2]['deleteimg'] = "<img onclick ='callDeleteProject(\""+updownCompE.id+"\",\""+projList[i2].projectId+"\")' src='../../images/cancel_16.png' wtf:qtitle='Delete Project' wtf:qtip='Click to delete Project.' style='height: 12px; width: 12px;float:right;cursor:pointer'><img onclick ='callEditProject(\""+updownCompE.id+"\",\""+projList[i2].projectId+"\",\""+escape(projList[i2].projectName)+"\")' src='../../images/edit.gif'  wtf:qtitle='Edit Project' wtf:qtip='Click to edit Project.' style='padding : 0px 20px ;height: 12px; width: 12px;float:right;cursor:pointer'>";
    //                    //res.projData.projList[i2]['deleteimg'] = "<img onclick ='callDeleteComment(\""+updownCompE.id+"\",\""+res.commData.commList[i2].commentid+"\")' src='../../images/cancel_16.png' wtf:qtitle='Delete Comment' wtf:qtip='Click to delete comment.' style='height: 12px; width: 12px;float:right;cursor:pointer'><img onclick ='callEditComment(\""+updownCompE.id+"\",\""+res.commData.commList[i2].commentid+"\",\""+escape(res.commData.commList[i2].comment)+"\")' src='../../images/edit.gif'  wtf:qtitle='Edit Comment' wtf:qtip='Click to edit comment.' style='padding : 0px 20px ;height: 12px; width: 12px;float:right;cursor:pointer'>";
    //                DataTpl.append(downUpPanel.body,res.projData.projList[i2]);
    //            }
    //        }
    //    }
    //    if(res.contactsData && contactsPermission) { /*  Create Owners Div   */
    //        DataTpl = new Wtf.XTemplate(  updownCompE.panContactsTitle );
    //        DataTpl.append(downUpPanel.body,{});
    //
    //        var contactList=res.contactsData.contactList;
    //        if(contactList.length==0){
    //            DataTpl= new Wtf.XTemplate( updownCompE.noContacts );
    //            DataTpl.append(downUpPanel.body,{});
    //        }else{
    //            DataTpl= new Wtf.XTemplate(  updownCompE.newContactsWithPerm  );
    //            for(i2 = 0; i2 < contactList.length; i2++){
    //                contactList[i2].contacts = unescape(contactList[i2].contacts);
    //                DataTpl.append(downUpPanel.body,contactList[i2]);
    //            }
    //        }
    //    }
    
    //           For Opportunities

    //    if(module == "Account" ) {
    //
    //        DataTpl = new Wtf.XTemplate(  updownCompE.panOpportunityTitle );
    //        DataTpl.append(downUpPanel.body,{});
    //        if(res.auditOpportunity) {
    //            if(res.auditOpportunity.auditOpportunityList!=undefined){
    //                if(res.auditOpportunity.auditOpportunityList.length==0){
    //                    DataTpl= new Wtf.XTemplate( updownCompE.noOpportunity );
    //                    DataTpl.append(downUpPanel.body,{});
    //                } else {
    //                    DataTpl= new Wtf.XTemplate(  updownCompE.newAuditContentWithPerm  );
    //                    for(var i3 = 0; i3 < res.auditOpportunity.auditOpportunityList.length; i3++) {
    //                        res.auditOpportunity.auditOpportunityList[i3].details = unescape(res.auditOpportunity.auditOpportunityList[i3].details);
    //                        DataTpl.append(downUpPanel.body,res.auditOpportunity.auditOpportunityList[i3]);
    //                    }
    //                }
    //            }
    //        } else {
    //            DataTpl= new Wtf.XTemplate( updownCompE.noPerm );
    //            DataTpl.append(downUpPanel.body, {});
    //        }
    //    
    //    }

       
    /*  Create Email Div   */
    if(loadMask && this.moduleName != "Account" && !Wtf.isStandAlone) {
        DataTpl = new Wtf.XTemplate(  updownCompE.panEmailTitle );
        DataTpl.append(downUpPanel.body,{});
        if(res.emailData) {
            if(res.emailData.emailList.length==0){
                DataTpl= new Wtf.XTemplate(updownCompE.noEmail);
                DataTpl.append(downUpPanel.body,{});
            } else {
                DataTpl= new Wtf.XTemplate(updownCompE.newEmailContentWithPerm);
                for(var i3 = 0; i3 < res.emailData.emailList.length; i3++) {
                    res.emailData.emailList[i3].email = unescape(res.emailData.emailList[i3].email);
                    DataTpl.append(downUpPanel.body,res.emailData.emailList[i3]);
                }
            }
        } else {
            DataTpl= new Wtf.XTemplate(updownCompE.noPerm);
            DataTpl.append(downUpPanel.body, {});
        }
    }
}