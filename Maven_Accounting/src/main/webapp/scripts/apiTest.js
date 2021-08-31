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
var http = getHTTPObject();
var LOGIN_PREFIX = "remoteapi.jsp";


function testFunction(action){
    var p = "action=" + action + "&data=" + getTestParam(action);
    http.open('POST', LOGIN_PREFIX, true);
    http.setRequestHeader("Content-length", p.length);
    http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    http.onreadystatechange = handleResponse;
    http.send(p);
}

function getTestParam(action){
    var param = "{}";
    var element="";
    var regEx = /\s/g;
    var email_check=document.apiform.email_check[0].checked;
    var commit_check=document.apiform.commit_check[0].checked;
    var element1="",element2="",element3="",element4="";
    switch(action){
        case 0://Company Exist
            element = document.getElementById('C_E_companyCheck').value;
           if(!element.replace(regEx,"") == ""){
            param = '{companyid:"'+element+'",test:true}';
            }
            break;

        case 1://User Exist
            element = document.getElementById('U_E_userIdCheck').value;
            element1 = document.getElementById('U_E_subdomain').value;
            if(!element.replace(regEx,"") == ""){
                param = '{subdomain:"'+element1+'",userid:"'+element+'",test:true}';
            }else{
                element = document.getElementById('U_E_username').value;
            if(!element.replace(regEx,"") == ""){
                param = '{subdomain:"'+element1+'",username:"'+element+'",test:true}';
                }
            }
            break;

        case 2://Create User
            element1 = document.getElementById('C_U_username').value;
            element2 = document.getElementById('C_U_email').value;
            element3 = document.getElementById('C_U_firstName').value;
            element4 = document.getElementById('C_U_lastName').value;
            var userid = document.getElementById('C_U_userid').value;
            var password = document.getElementById('C_U_password').value;
            var element5=document.getElementById('C_U_companyid').value;
            var role=document.getElementById('C_U_role').value;
            if(!element1.replace(regEx,"")=="" && !element2.replace(regEx,"")=="" && !element3.replace(regEx,"")=="" && !element4.replace(regEx,"")=="" && !element5.replace(regEx,"")=="")
                param = '{username:"'+element1+'",userid:"'+userid+'",password:"'+password+'",fname:"'+element3+'",lname:"'+element4+'",emailid:"'+element2+'",companyid:"'+element5+'",sendmail:'+email_check+',iscommit:'+commit_check+',test:true}';
            break;

       case 3://Create Comapany
            element1 = document.getElementById('C_C_companyname').value;
            element2 = document.getElementById('C_C_email').value;
            element4 = document.getElementById('C_C_firstName').value;
            element5 = document.getElementById('C_C_lastName').value;
            var companyid = document.getElementById('C_C_companyid').value;
            var subdomain = document.getElementById('C_C_subdomain').value;
            var userid = document.getElementById('C_C_userid').value;
            var password = document.getElementById('C_C_password').value;
           var element7 = document.getElementById('C_C_username').value;
           element3 = document.getElementById('C_C_phone').value;
           var element6 = document.getElementById('C_C_address').value;
            if(!element1.replace(regEx,"")=="" && !element2.replace(regEx,"")=="" &&  !element4.replace(regEx,"")=="" &&  !element5.replace(regEx,"")=="" &&  !element7.replace(regEx,"")=="")
              param = '{subdomain:"'+subdomain+'",companyname:"'+element1+'",companyid:"'+companyid+'",emailid:"'+element2+'",fname:"'+element4+'",lname:"'+element5+'",username:"'+element7+'",userid:"'+userid+'",password:"'+password+'",phno:"'+element3+'",address:"'+element6+'",sendmail:"'+email_check+'",iscommit:'+commit_check+',test:true}';
            break;

        case 4://Delete User
            element = document.getElementById('D_U_username').value;
            element1 = document.getElementById('D_U_userid').value;
            element2 = document.getElementById('D_U_subdomain').value;
            if(!element.replace(regEx,"")=="")
              param = '{subdomain:"'+element2+'",username:"'+element+'",iscommit:'+commit_check+',test:true}';
            else{
                if(!element1.replace(regEx,"")=="")
                 param = '{subdomain:"'+element2+'",userid:"'+element1+'",iscommit:'+commit_check+',test:true}';
            }
            break;

        case 5://Assign Role
            element = document.getElementById('A_R_username').value;
            element1 = document.getElementById('A_R_userid').value;
            role=document.getElementById('A_R_role').value;
            if(!element.replace(regEx,"")=="")
              param = '{role:"'+role+'",username:"'+element+'",iscommit:'+commit_check+',test:true}';
             else{
                 if(!element1.replace(regEx,"")=="")
                 param = '{role:"'+role+'",userid:"'+element1+'",iscommit:'+commit_check+',test:true}';
             }
            break;

        case 6://Activate User
            element = document.getElementById('A_userId').value;
            element1 = document.getElementById('A_subdomain').value;
            if(!element.replace(regEx,"") == ""){
                param = '{subdomain:"'+element1+'",userid:"'+element+'",iscommit:'+commit_check+',test:true}';
            }else{
                element = document.getElementById('A_username').value;
            if(!element.replace(regEx,"") == ""){
                param = '{subdomain:"'+element1+'",username:"'+element+'",iscommit:'+commit_check+',test:true}';
                }
            }
            break;

    }
    return param;
}

function handleResponse(){
    if(http.readyState == NORMAL_STATE) {
        if(http.responseText && http.responseText.length > 0) {
            var results = eval("(" + trimStr(http.responseText) + ")");
            var dom = "";
            var responseMessage = "";
            switch(results.action){
                case 0:
                    dom = document.getElementById("companyCheck_result");
                    break;
                case 1:
                    dom = document.getElementById("userCheck_result");
                    break;
                case 2:
                    dom = document.getElementById("userCreate_result");
                    break;
                case 3:
                    dom = document.getElementById("companyCreate_result");
                    break;
                case 4:
                    dom = document.getElementById("deleteUser_result");
                    break;
                case 5:
                    dom = document.getElementById("assignRole_result");
                    break;
                case 6:
                    dom = document.getElementById("activateUser_result");
                    break;
            }
            if(dom !== undefined){
                if(results.success) {
                    switch(results.infocode){
                    case "m01":
                        responseMessage = WtfGlobal.getLocaleText("acc.field.Companyexists");
                        break;
                    case "m02":
                        responseMessage = WtfGlobal.getLocaleText("acc.field.Companydoesn'texist");
                        break;
                    case "m03":
                        responseMessage = WtfGlobal.getLocaleText("acc.field.Userexists");
                        break;
                    case "m04":
                        responseMessage = WtfGlobal.getLocaleText("acc.field.Userdoesn'texist");
                        break;
                    case "m05":
                        responseMessage = WtfGlobal.getLocaleText("acc.field.Usercreatedsuccessfully");
                        break;
                    case "m06":
                        responseMessage = WtfGlobal.getLocaleText("acc.msgbox.7");
                        break;
                    case "m07":
                        responseMessage = WtfGlobal.getLocaleText("acc.field.UserDeletedSuccessfully.");
                        break;
                    case "m08":
                        responseMessage = WtfGlobal.getLocaleText("acc.field.Roleassignedsuccessfully");
                        break;
                    case "m09":
                            responseMessage =WtfGlobal.getLocaleText("acc.field.UserActivatedSuccessfully");
                            break;
                    case "m10":
                            responseMessage = WtfGlobal.getLocaleText("acc.field.UserDeactivatedSuccessfully");
                            break;
                    }
                    dom.innerHTML = responseMessage;
                } else {
                       switch(results.errorcode){
                        case "e01":
                            responseMessage = WtfGlobal.getLocaleText("acc.field.Insufficientdata");
                            break;
                        case "e02":
                            responseMessage = WtfGlobal.getLocaleText("acc.field.Errorconnectingtoserver");
                            break;
                        case "e03":
                            responseMessage = WtfGlobal.getLocaleText("acc.field.Userwithsameusernamealreadyexists");
                            break;
                        case "e04":
                            responseMessage = WtfGlobal.getLocaleText("acc.field.Companydoesnotexist");
                            break;
                        case "e05":
                            responseMessage = WtfGlobal.getLocaleText("acc.field.Errorwhilesendingmail");
                            break;
                        case "e06":
                            responseMessage = WtfGlobal.getLocaleText("acc.field.Userdoesn'texist");
                            break;
                        case "e07":
                            responseMessage = WtfGlobal.getLocaleText("acc.field.UserIdAlreadypresent");
                            break;
                        case "e08":
                            responseMessage = WtfGlobal.getLocaleText("acc.field.CompanyIdAlreadypresent");
                            break;
                        case "e09":
                            responseMessage = WtfGlobal.getLocaleText("acc.field.Email-IDalreadyexists");
                            break;
                    }
                    dom.innerHTML = responseMessage;
                }
            }
        }
    }
}
