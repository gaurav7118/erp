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
//var LOGIN_PREFIX = 'jspfiles/auth.jsp';
var LOGIN_PREFIX = 'AuthHandler/verifyLogin.do';
var SIGNUP_PREFIX = 'signup.jsp?';
var http = getHTTPObject();
var refid;
/* Delete this function to restore regular signup */
function doDirectLogin(){
    var user = "jane";
    var pass = "1234";
    validateLogin(user, pass);
}

function showNewUserForm(){
    $('#new-user-form').fadeIn('normal')
    setLoading2("&nbsp;", 'ajaxResult', '');
    $('#newuserid').focus();
    return false;
}

function validateLogin(u, p){
    var d = new Date().getTime();
    var p = 't=a&x=' + encodeURI(hex_hmac_sha1(d, p) + '&u=' + encodeURI(u) + '&p=' + encodeURI(hex_sha1(p)) + '&dc=' + encodeURI(d));
    http.open('POST', LOGIN_PREFIX, true);
    http.setRequestHeader("Content-length", p.length);
    http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    http.onreadystatechange = handleHttpValidateLogin;
    http.send(p);
    /*uncomment to enable loading message*/
    //setLoading(1);
    setMsg("Loading", 1);
}

function SetCookie(name, value){
    document.cookie = name + "=" + value + ";path=/;";
}

function getCookie(c_name){
    if (document.cookie.length > 0) {
        c_start = document.cookie.indexOf(c_name + "=");
        if (c_start != -1) {
            c_start = c_start + c_name.length + 1;
            c_end = document.cookie.indexOf(";", c_start);
            if (c_end == -1)
                c_end = document.cookie.length;
            return unescape(document.cookie.substring(c_start, c_end));
        }
    }
    return "";
}

function handleHttpValidateLogin(){
    if (http.readyState == NORMAL_STATE) {
        if (http.responseText && http.responseText.length > 0) {
            var results = eval("(" + trimStr(http.responseText) + ")");
            results = results.data;
            if (results.success == true) {
                SetCookie("lid", results.lid);
                SetCookie("username", results.username);
                SetCookie("superuser", (results.superuser==results.lid));
                SetCookie("admin", (results.superuser==1));
                SetCookie("cid",results.companyid);

                var o = results.roleperms;
                var oS = "[";
                if(o && o.length > 0){
                        for( i = 0; i <  o.length; i++){
                                if(i == 0)
                                        oS += ('"' + o[i].rolegroupid + '"');
                                else
                                        oS += (',"' + o[i].rolegroupid + '"');
                        }
                }
                oS += "]";
                SetCookie("perms", oS);

                var p = results.realroles;
                var oP = "[";
                if(p && p.length > 0){
                        for( j = 0; j <  p.length; j++){
                                if(j == 0)
                                        oP += ('"' + p[j].val + '"');
                                else
                                        oP += (',"' + p[j].val + '"');
                        }
                }
                oP += "]";
                SetCookie("realroles", oP);

                /**
                 * If user has DO and GRN permission then only user can access generateorder.jsp page.
                 * If user has permission then barcodescanner=1 otherwise 0.
                 */    
                if (results && results.perm && results.perm.UPerm.barcodescanner === 1) {
                    window.top.location.href = './generateorder.jsp';
                } else if (results && results.perm && results.perm.UPerm.barcodescanner === 2) {
                    /**
                     * If user has Work Order permission then only user can access startworkorder.jsp page.
                     * If user has permission then barcodescanner=1 otherwise 0.
                     */    
                    window.top.location.href = './startworkorder.jsp';
                } else {
                    redirect();
                }
            }
            else {
                setMsg(results.message, 0);
            }
        }
        else {
            setMsg("An error occurred while connecting to service.", 0);
        }
        setLoading(0);
    }
}

//function redirect(){
//    window.top.location.href = "./";
//}

function redirect(u){
    if (!u || u == undefined) {
        u = "./";
    }
    window.top.location.href = u;
}

function setLoading(status){
//    var lBtn = $('#LoginButton');
//    var pwd = $('#Password')[0];
//    switch (status) {
//        case 0:
//        pwd.value = "";
//        lBtn.disabled = false;
//        break;
//        case 1:
//        lBtn.disabled = true;
//        break;
//    }
}

function lValidateEmpty(){
    var usr = $("#UserName")[0];
    var pwd = $("#Password")[0];
    var usrReq = $("#UserNameRequired")[0];
    var pwdReq = $("#PasswordRequired")[0];
    var bL = (!usr.value || trimStr(usr.value).length == 0);
    var bP = (!pwd.value || pwd.value.length == 0);
    setVisibility(bL, usrReq);
    setVisibility(bP, pwdReq);
    if (!(bL || bP)) {
        validateLogin(trimStr(usr.value), pwd.value);
    }
}

function checkCookie(){
    u = getCookie('username');
    if (!(u == null || u == "")) {
        redirect();
    }
}

function formFocus(){
    var page = window.location.href.split('?')[1];
    if(page !== undefined){
        var params = page.split('&');
        var signupFlag = false;
        for(var pcnt = 0; pcnt < params.length; pcnt++){
            var tparam = params[pcnt].split('=');
            if(tparam[0] == "signup"){
                signupFlag = true;
            } else if(tparam[0] == 'g'){
                refid = tparam[1];
            }
        }
        if(signupFlag){
            showNewUserForm();
        }
        if(page == "timeout"){
            setMsg("Session Timed Out", 0);
        }
    } else {
        var f = $("#loginForm")[0];
        if (f) {
            if (f.UserName.value == null || f.UserName.value == "") {
                f.UserName.focus();
            }
            else {
                f.Password.focus();
            }
        }
    }
}

function handleSignUpUser(){
    if (http.readyState == NORMAL_STATE) {

        if (http.responseText && http.responseText.length > 0) {
            var res = trimStr(http.responseText);
            var msg = "&nbsp;";

            if (res != "" && res != null) {

                var resObj = eval("(" + res + ")");
               
                if (resObj.data =="msg:{succcess: true}") {
                    redirect(resObj.uri);
                }
                else {
                    if (resObj.error =="msg:{companyname failure}") {
                        msg = " Company Name not available";
                    }

                    else
                        if (resObj.error =="msg:{userid failure}") {
                            msg = " Userid not available";
                        }
                    else{
                        msg = " Some error occoured";
                    }
//                    else {
//                        if (resObj.failure == 1) {
//                            msg = "Email ID already registered";
//                        }
//                        else
//                            if (resObj.failure == 2) {
//                                msg = "Domain is already registered";
//                            }
//                            else {
//                                msg = "An error occurred while signing up";
//                            }
//                    }
                    setLoading2(msg, 'ajaxResult', 'errorFB');
                }
            }
        }
        else {
            setLoading2("An error occurred while connecting", 'ajaxResult', 'errorFB');
        }
    }
}

function signUpUser(u, p, eml, cn, cdomain, fn, ad,ci,zp,co){
    var p = 'mode=1&u=' + encodeURI(u) + '&p=' + encodeURI(hex_sha1(p)) + '&e=' + encodeURI(eml) + '&c=' + encodeURI(cn) +
    '&cdomain=' +
    encodeURI(cdomain) +
    '&fname=' +
    encodeURI(fn)+
'&address=' +
    encodeURI(ad)
//    '&city=' +
//    encodeURI(fn)+
//'&zipcode=' +
//    encodeURI(fn)
//'&country=' +
//    encodeURI(fn)

    ;
    if (refid !== undefined) {
        p += '&g=' + encodeURI(refid);
    }
    http.open('POST', SIGNUP_PREFIX, true);
    http.setRequestHeader("Content-length", p.length);
    http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    http.onreadystatechange = handleSignUpUser;
    http.send(p);
    setLoading2("Checking", 'ajaxResult', 'loadingFB');
}

function sValidateEmpty(){
    $("#vmsg").hide("fast");
    var fname = $('#fname')[0];
    var address = $('#address')[0];
//    var city = $('#city')[0];
//    var zipcode = $('#zipcode')[0];
//    var country = $('#country')[0];
    var usr = $('#newuserid')[0];
    var eml = $('#email')[0];
    var p1 = $('#newpassword')[0];
    var p2 = $('#newpassword2')[0];
    var cname = $('#company-name')[0];
    var cdomain = $('#company-domain')[0];

    var fN = (!fname.value || trimStr(fname.value).length == 0 || trimStr(fname.value).length > 32 || fname.value.replace(/[^\w\s_\-\'\"]+/g, '') != fname.value) ? $("#vfname")[0].className = "validation errorimg" : $("#vfname")[0].className = "validation valid";
    var aD = (trimStr(address.value).length > 32 || address.value.replace(/[^\w\s_\-\'\"]+/g, '') != address.value) ? "validation errorimg" :"validation valid";

    //var aD = (!address.value || trimStr(address.value).length == 0 || trimStr(address.value).length > 32 || address.value.replace(/[^\w\s_\-\'\"]+/g, '') != address.value) ? $("#vaddress")[0].className = "validation errorimg" : $("#vaddress")[0].className = "validation valid";
//     var cI = (!city.value || trimStr(city.value).length == 0 || trimStr(city.value).length > 32 || city.value.replace(/[^\w\s_\-\'\"]+/g, '') != city.value) ? $("#city")[0].className = "validation errorimg" : $("#city")[0].className = "validation valid";
//     var zC = (!zipcode.value || trimStr(zipcode.value).length == 0 || trimStr(zipcode.value).length > 32 || zipcode.value.replace(/[^\w\s_\-\'\"]+/g, '') != zipcode.value) ? $("#zipcode")[0].className = "validation errorimg" : $("#zipcode")[0].className = "validation valid";
//      var cO = (!country.value || trimStr(country.value).length == 0 || trimStr(country.value).length > 32 || country.value.replace(/[^\w\s_\-\'\"]+/g, '') != country.value) ? $("#country")[0].className = "validation errorimg" : $("#country")[0].className = "validation valid";
    var cN = (!cname.value || trimStr(cname.value).length == 0 || !cname.value.match(/\w+/g) || trimStr(cname.value).length == 0) ? $("#vcompany")[0].className = "validation errorimg" : $("#vcompany")[0].className = "validation valid";
   // var cDomain = (!cdomain.value || trimStr(cdomain.value).length < 1 || trimStr(cdomain.value).length > 32 || cdomain.value.replace(/[^a-zA-Z 0-9]+/g, '').replace(/[\s]/g, '') != cdomain.value) ? $("#vcompanydomain")[0].className = "validation errorimg" : $("#vcompanydomain")[0].className = "validation valid";
    var bL = (!usr.value || trimStr(usr.value).length == 0 || !usr.value.match(/\w+/) || trimStr(usr.value).length > 32) ? $("#vlogin")[0].className = "validation errorimg" : $("#vlogin")[0].className = "validation valid";
    var bE = (!eml.value || trimStr(eml.value).length == 0 || !(/^([a-zA-Z0-9_\-\.+]+)@(([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})$/).test(eml.value)) ? $("#vemail")[0].className = "validation errorimg" : $("#vemail")[0].className = "validation valid";
    var bP1 = (!p1.value || p1.value.length < 4 || p1.value.length > 16) ? $("#vpass")[0].className = "validation errorimg" : $("#vpass")[0].className = "validation valid";
    var bP2 = (!p2.value || p2.value.length < 4 || p1.value != p2.value) ? $("#vpass1")[0].className = "validation errorimg" : $("#vpass1")[0].className = "validation valid";
    var msg = "";
    if (fN != "validation valid")
        msg = "Enter your first name<br>";

    if (aD != "validation valid")
        msg += "Enter a valid address<br>";
//    if (cI != "validation valid")
//        msg += "Enter your city<br>";
//    if (zC != "validation valid")
//        msg += "Enter your zip Code<br>";
//    if (cO != "validation valid")
//        msg += "Enter your Country name<br>";
    if (cN != "validation valid")
        msg += "Enter your company name<br>";
  //  if (cDomain != "validation valid")
  //      msg += "Enter your company domain<br>";
    if (bL != "validation valid")
        msg += "Enter a username<br>";
    if (bE != "validation valid")
        msg += "Enter your email address <br>";
    if (bP1 != "validation valid")
        msg += "Enter a password<br>";
    else {
        if (bP2 != "validation valid")
            msg += "Passwords do not match";
    }    
    if (msg == "") {
        signUpUser(trimStr(usr.value), p1.value, trimStr(eml.value), trimStr(cname.value), trimStr(cdomain.value), trimStr(fname.value), trimStr(address.value));//,trimStr(city.value),trimStr(zipcode.value),trimStr(country.value));
        clearErrorMsg();
}
    else {
        setErrorMsg(msg, "error");
    }
}

function checkLoginid(u){
    var p = 'mode=0&id=' + encodeURI(u);
    http.open('POST', SIGNUP_PREFIX, true);
    http.setRequestHeader("Content-length", p.length);
    http.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
    http.onreadystatechange = handleHttpCheckLoginid;
    http.send(p);
    setLoading2("Checking", 'ajaxResult', 'loadingFB');
}

function handleHttpCheckLoginid(){
    if (http.readyState == NORMAL_STATE) {
        if (trimStr(http.responseText) == "success") {
            setLoading2("Available", 'ajaxResult', '');
        }
        else{
            setLoading2("Not Available", 'ajaxResult', 'errorFB');
        }
    }
}
function setLoading2(msg, elm, cls){
    $('#' + elm)[0].innerHTML = msg;
    $('#' + elm)[0].className = cls;
}
function updateLabel(val){
    var lblObject = null;
    var valObject = null;
    var delObject = null;
    var udlObject = null;
    var adlObject = null;
    switch (val) {
        case 1:
            valObject = document.getElementById("fname"); //value from
            lblObject = document.getElementById("lblLastname"); //value to
            validateFName();
//            if (valObject && valObject.value) { //check if valObject is not null and its value is not blank
//                lblObject.innerHTML = valObject.value + ", enter your last name:";
//            }
            break;
        case 3:
            validateLoginName();
            valObject = document.getElementById("newuserid"); //value from
            lblObject = document.getElementById("lblpass"); //value to
            if (valObject && valObject.value) { //check if valObject is not null and its value is not blank
                lblObject.innerHTML = valObject.value + ", enter your password:";
            }
            break;
        case 2:
            valObject = document.getElementById("company-name");
            lblObject = document.getElementById("company-domain");
            adlObject = document.getElementById("site-address2");
            validateCompany();
            if (valObject && valObject.value) {
                adlObject.innerHTML = "http://" + lblObject.value + ".deskera.com";
            }
            else
            {
                adlObject.innerHTML = "";
            }
            break;
        case 4:
            validateEmail();
            //alert(valObject + valObject.value);
            valObject = document.getElementById("fname"); //value from
            delObject = document.getElementById("deldetails"); //value to
            if (valObject && valObject.value) { //check if valObject is not null and its value is not blank
                delObject.innerHTML = "Hello " + valObject.value + ", please enter your account details.";
            }
            break;
        case 5:
            validateRePass();
//            valObject = document.getElementById("newuserid"); //value from
//            udlObject = document.getElementById("userdetails"); //value to
//            if (valObject && valObject.value) { //check if valObject is not null and its value is not blank
//                udlObject.innerHTML = "Hello " + valObject.value + ", please enter your company details.";
//            }
            break;
      case 6:
            validateAddress();
            valObject = document.getElementById("fname"); //value from
            lblObject = document.getElementById("address"); //value to
            //if (valObject && valObject.value) { //check if valObject is not null and its value is not blank
               //lblObject.innerHTML = valObject.value + ", enter your address:";
           // }
            break;
    }
}
function position(){
    $('#master').css({
        top: ($(window).height() - $('#master').height()) / 2,
        left: ($(window).width() - $('#master').width()) / 2
    })

    $('#new-user-form').css({
        top: ($(window).height() - $('#new-user-form').height()) / 2 + 40,
        left: ($(window).width() - $('#new-user-form').width()) / 2
    })
}
function validateFName(){
    var fname = $('#fname')[0].value;
    var bL = (!fname || trimStr(fname).length == 0 || trimStr(fname).length > 32 || fname.replace(/[^\w\s_\-\'\"]+/g, '') != fname) ? $("#vfname")[0].className = "validation errorimg" : $("#vfname")[0].className = "validation valid";
}
function validateAddress(){
   var address = $('#address')[0].value;
   if(!address || trimStr(address).length == 0 )
       $("#vaddress")[0].className = "";
   else
    var bL = (!address || trimStr(address).length == 0 || trimStr(address).length > 100 || address.replace(/[^\w\s_\-\'\"]+/g, '') != address) ? $("#vaddress")[0].className = "validation errorimg" : $("#vaddress")[0].className = "validation valid";
}
function validateLName(){
    var lname = $('#lname')[0].value;
    var bL = (!lname || trimStr(lname).length == 0 || trimStr(lname).length > 32 || lname.replace(/[^\w\s_\-\'\"]+/g, '') != lname) ? $("#vlname")[0].className = "validation errorimg" : $("#vlname")[0].className = "validation valid";
}
function validateLoginName(){
    var login = $('#newuserid')[0].value;
    var bL = (!login || trimStr(login).length == 0 || (login.match(/\w+/) != login) || trimStr(login).length > 32) ? $("#vlogin")[0].className = "validation errorimg" : $("#vlogin")[0].className = "validation valid";
}
function validateEmail(){
    var eml = $('#email')[0].value;
    var bE = (!eml || trimStr(eml).length == 0 || !(/^([a-zA-Z0-9_\-\.+]+)@(([0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.)|(([a-zA-Z0-9\-]+\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})$/).test(eml)) ? $("#vemail")[0].className = "validation errorimg" : $("#vemail")[0].className = "validation valid";
}
function validatePass(){
    var p1 = $('#newpassword')[0].value;
    var bP1 = (!p1 || p1.length < 4 || p1.length > 16) ? $("#vpass")[0].className = "validation errorimg" : $("#vpass")[0].className = "validation valid";
}
function validateRePass(){
    var p = $('#newpassword')[0].value;
    var p1 = $('#newpassword2')[0].value;
    var bP2 = (!p1 || p1.length < 4 || p1 != p) ? $("#vpass1")[0].className = "validation errorimg" : $("#vpass1")[0].className = "validation valid";
}
function validateCompany(){
    var cname = $('#company-name')[0].value;
    var cN = (!cname || trimStr(cname).length == 0 || !cname.match(/\w+/g) || trimStr(cname).length > 32) ? $("#vcompany")[0].className = "validation errorimg" : $("#vcompany")[0].className = "validation valid";
    $('#company-domain')[0].value = (cN == "validation errorimg") ? "" : ($('#company-name')[0].value).replace(/[^a-zA-Z 0-9]+/g, '').replace(/[\s]/g, '').toLowerCase();
    if($('#company-domain')[0].value.length>0) {
        $("#vcompanydomain")[0].className = "validation valid";
    }
    else
    {
        $("#vcompanydomain")[0].className = "validation errorimg";
    }
}
function validateDomain(){
    var cdomain = $('#company-domain')[0].value;
    var cN = (!cdomain || trimStr(cdomain).length < 1 || trimStr(cdomain).length > 32 || cdomain.replace(/[^a-zA-Z 0-9]+/g, '').replace(/[\s]/g, '') != cdomain) ? $("#vcompanydomain")[0].className = "validation errorimg" : $("#vcompanydomain")[0].className = "validation valid";
    if(cN == "validation valid") {
        var d = cdomain.toLowerCase();
        $('#company-domain')[0].value = d;
        var adlObject = document.getElementById("site-address2");
        adlObject.innerHTML = "http://" + d + ".deskera.com";
    }
}
function reset_form(){
    $("#signup-form")[0].reset();
    $(".validation").removeClass("valid");
    $(".validation").removeClass("errorimg");
	redirect( "http://apps.deskera.com");
}
function activateButton(e){
    if ($("#checkbox:checked").length > 0) {
        $('#button').css({
            backgroundPosition: 'left top'
        });
        $('#button').removeAttr("disabled");
    }
    else {
        $('#button').css({
            backgroundPosition: 'left bottom'
        });
        $('#button').attr("disabled", "disabled");
    }
}
function setErrorMsg(msg, cls){
    $("#error-log")[0].innerHTML = msg;
    $("#error-log")[0].className = cls;
    $("#error-log").fadeIn("slow");
}
function clearErrorMsg(){
	$("#error-log")[0].innerHTML = "";
    $("#error-log")[0].className = "";
    $("#error-log").fadeIn("slow");
}
$(function(){
    $('#btn-new-user span').hide()

    position()

    $('#tabs-bottom').css({
        position: 'absolute',
        bottom: 12,
        left: 13,
        zIndex: 200
    })

    if (jQuery.browser.version == '6.0') {
        $('#tabs-bottom').css({
            bottom: 11
        })
    }

    $('.container').css({
        position: 'absolute',
        top: 110,
        left: 25
    })

    $("#tabs-bottom li").click(function(obj){
        a = $("#tabs-bottom li:not(.active)")
        $('#tabs-bottom li').removeClass('active')
        $('div.container').removeClass('active')
        $('div.container#container-' + a.attr('id').substring(4, a.attr('id').length)).addClass('active')
        a.addClass('active')
        return false;
    })

    $('#btn-new-user').hover(function(){
        $('#btn-new-user span').fadeIn()
    }, function(){
        $('#btn-new-user span').fadeOut()
    })

    $('#btn-new-user').click(showNewUserForm)

    $('#chkusravail').click(function(){
        var usr = $('#newuserid')[0];
        var usrReq = $('#NewUserNameRequired')[0];
		var bL = (!usr.value || trimStr(usr.value).length == 0);
        setVisibility(bL, usrReq);
        if (!bL) {
            checkLoginid(trimStr(usr.value));
        }
        return false;
    })

    $('#link-hide').click(function(){
        $('#new-user-form').fadeOut('normal')
    })

    $(window).resize(position)
})
