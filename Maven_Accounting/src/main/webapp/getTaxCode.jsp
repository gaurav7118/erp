<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.krawler.esp.handlers.ProfileHandler"%>
<%@ page import="com.krawler.common.util.*"%>
<%@ page import="com.krawler.utils.json.base.JSONObject"%>
<%@ page import="com.krawler.utils.json.base.JSONException"%>
<%@ page import="com.krawler.common.session.SessionExpiredException"%>
<%@ page import="com.krawler.common.service.ServiceException"%>
<%@ page import="org.hibernate.Session"%>
<%@ page import="java.text.DateFormat"%>
<%@ page import="org.hibernate.Transaction"%>
<%@ page import="com.krawler.esp.hibernate.impl.HibernateUtil"%>
<%@ page import="java.util.logging.Logger"%>
<%@ page import="java.util.logging.Level"%>

<jsp:useBean id="sessionbean" scope="session" class="com.krawler.spring.sessionHandler.sessionHandlerImpl" />


<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Deskera : Tax ID</title>
<link href="style/taxform.css" rel="stylesheet" type="text/css" />
 <!--[if lte IE 6]>
    <style type="text/css">
       .lavaLampWithImage {
            width:570px;
        }
       .not-more-text {
            width:27%;
            padding-left:195px;
            margin-top:0px;
            padding-top:0px
        }
        .field-lebal {
            margin-left:5px
        }
        .form-four-info {
            height:200px;
        }
        .validation {
            margin-left:9px;
            padding:0px;
            margin-top:16px
        }
        #vcompanydomain {
            margin:0px;
            padding:0px;
        }
	.info img {
	    padding: 1px 0 0 0;
	}
    </style>
<![endif]-->
        <link rel="shortcut icon" href="images/deskera.png"/>
        <script type="text/javascript">
            /*<![CDATA[*/
            var _nA = 6;
            /*]]>*/
        </script>
        <script type="text/javascript" src="lib/jquery.js"></script>

        <script type="text/javascript" src="lib/jquery.lavalamp.js"></script>
        <script type="text/javascript" src="scripts/core/42.js"></script>
        <script type="text/javascript" src="scripts/belongToUs.js"></script>
        <script type="text/javascript">
<%
    Session hSession = null;
    Transaction tx = null;
    String nameStr="";
    String emailStr="";
    String personidStr="";
    String taxidnumberStr="";
    boolean isexpiredStr=true;
    String btnHiddenStr="type='submit'";
    String form1StyleStr="";
    String form2StyleStr="";
    String resmsg="";
	try {

        String isubmit = request.getParameter("isubmit");
        String taxid = request.getParameter("taxid");        
		JSONObject jbj = new JSONObject();
		String personid = request.getParameter("personid");
       	hSession = HibernateUtil.getCurrentSession();
       	tx = hSession.beginTransaction();
		if (!StringUtil.isNullOrEmpty(personid)) {
            if(!StringUtil.isNullOrEmpty(isubmit)) {
                if(isubmit.equals("true")&!StringUtil.isNullOrEmpty(taxid)) {          
                    //resmsg =  ProfileHandler.saveVendorTaxID(hSession, request);
                    //tx.commit();
                    java.util.ArrayList params=new java.util.ArrayList();
                    params.add(taxid);
                    params.add(personid);
                    String SELECT_USER_INFO="update Vendor v set v.taxIDNumber=? " +
                        "where v.ID = ?";
                    int count = HibernateUtil.executeUpdate(hSession, SELECT_USER_INFO, params.toArray());
                    if(count>0)
                        resmsg="Information has been updated successfully";
                    else
                        resmsg="Error";
                }
            }
			jbj = ProfileHandler.getVendorDetails(hSession,request);
            String personname=(String)jbj.get("personname");
            String email=(String)jbj.get("email");
            String taxidnumber=(String)jbj.get("taxidnumber");
            boolean isexpired=(Boolean)jbj.get("isexpired");
            
            if(isexpired){     
                form1StyleStr="style='display: none;'";
                form2StyleStr="style='display: none;'";
                btnHiddenStr="";
                resmsg="Sorry! Given link is Expired.";
            }
            else if(!StringUtil.isNullOrEmpty(taxidnumber)){
                nameStr =  personname;
                emailStr =  email;
                personidStr =  personid;       
                taxidnumberStr = taxidnumber;
                form1StyleStr="style='display: none;'";
                form2StyleStr="";
                btnHiddenStr="";
                if(StringUtil.isNullOrEmpty(isubmit))
                    resmsg="You have already entered your Tax ID.";
            }
            else {
                nameStr =  personname;
                emailStr =  email;
                personidStr =  personid;
                form1StyleStr="";
                form2StyleStr="style='display: none;'";
                isexpiredStr =false;
            }
        }
        tx.commit();
    }catch (JSONException e) {
        if (tx!=null) tx.rollback();
        e.printStackTrace();
        //out.println("{\"valid\":false,\"data\":{}}");
    } catch(Exception sE){
        if (tx!=null) tx.rollback();
        sE.printStackTrace();
        //out.println("{\"valid\":false,\"data\":{}}");
    }
    finally {
        HibernateUtil.closeSession(hSession);
    }
%>


        </script>
</head>
<body class="body-inner">
 <div class="container-main">
  <div class="container1" >
    <div class="wrapper">
      <div class="top-wrapper">
       <a href="http://deskera.com/"> <div class="logo-left"><img src ="images/deskera-logofinal.jpg" /></div> </a>
      </div>
      <div id="banner-signup">
        <div class="sing-up-for-btn"></div>
      </div>
      <div class="middle-wrapper">

        <div class="two-col-right">
            <div id="error-log"></div>
            <div id="ajaxResult"></div>
           <form   action="getTaxCode.jsp" method="post"name="taxform"  >
            <fieldset id="fieldset" <%=form1StyleStr%>>
            <div class="top"></div>

            <div class="one"> <div class ="steps-to-signup"> Enter Your Details </div></div>

            <div class="form-one-info">
              <p>Kindly  verify other details and enter your TAX ID number</p>
              <br/>
              <div id="desk">
                <ul>
                  <li>
                    <div class="left">
                       <label for="Field" id="lblFirstname">Your Name:</label>
                      <span class="text-field">
                        <input name="personname" type="text" class="field1"  id="personname"  readonly="true" value="<%=nameStr%>"/>
                      </span><span class="validation"  id="vname"></span></div>
                    <div class="left">
                    <label for="Field" id="lblLastname">Your Tax ID:</label>
                      <span class="text-field">
                      <input name="taxid" class="field1" type="text" id="taxid"  onchange="changeCheckVal(); " maxlength="15" />
                      </span><span class="validation" id="vtaxid" ></span></div>
                    <div class="left">
                        <label for="Field"id="lblEmail">Email:</label>
                        <span class="text-field">
                        <input name="email" class="field1" type="text"  id = "email" readonly="true" value="<%=emailStr%>"/>
                        </span><span class="validation" id="vemail" ></span></div>
                        <input type="hidden" name="isubmit" id="isubmit" value="true" onchange="responseMsg()"/>
                        <input type="hidden" name="personid" value="<%=personidStr%>" />
                        <input type="hidden" name="isexpiredid" value="<%=isexpiredStr%>" id="isexpiredid" />
                         <input type="hidden" name="resmsgid" value="<%=resmsg%>" id="resmsgid" />
                </li>
             </ul>
            </div>
            </div>
            <div class="btm"></div>
            </fieldset>
            <fieldset id="fieldset"  <%=form1StyleStr%>>
            <div class="top"></div>
            <div class="two"><div class ="steps-to-signup"> Vendor Declaration </div></div>
            <div class="form-four-info">
              <div id="desk">
                <ul>
                  <li>
                    <div style="float:left; width:100%;">
                      <input type="checkbox" name="checkbox" id="checkbox" style="margin-top:3px;"  onclick="activateButton1(); "/>
                      <strong>I agree that the information entered here are correct and best of my knowledge.</strong> <br/>
                      <br/>
                      <div style="width:100%; float:left;">
                        <input name="button" <%=btnHiddenStr%> class="order-sign-up" value =" " id="button" disabled="disabled"   />

                      </div>
                    </div>
                  </li>
                </ul>
              </div>
            </div>            
            <div class="btm"></div>
            </fieldset>
            <fieldset id="fieldset" <%=form2StyleStr%>>
            <div class="top"></div>
 <!--            <div class="form-one-info">
             <div class ="steps-to-signup">Your  Details are:</div>-->
              <br/>
              <div id="desk">
                <ul>
                  <li>
                    <div class="resleft">
                       <label class="reslabel" for="Field" id="lblFirstname">Your Name:</label>
                       <span class="resText"><%=nameStr%></span>
                    </div>
                    <div class="resleft">
                       <label class="reslabel" for="Field" id="lblLastname">Your Tax ID:</label>
                       <span class="resText"><%=taxidnumberStr%></span>
                    </div>
                    <div class="resleft">
                        <label class="reslabel" for="Field"id="lblEmail">Email:</label>
                        <span class="resText"><%=emailStr%></span>
                    </div>
                    <input type="hidden" name="isubmit" id="isubmit" value="true" />
                    <input type="hidden" name="personid" value="<%=personidStr%>" />
                    <input type="hidden" name="isexpiredid" value="<%=isexpiredStr%>" id="isexpiredid" />
                    <input type="hidden" name="resmsgid" value="<%=resmsg%>" id="resmsgid" />
                </li>
             </ul>
            </div>
         <!--   </div>-->
            <div class="btm"></div>
            </fieldset>
          </form>
        </div>

      </div>

    </div>
  </div>


</div>
<script type="text/javascript">
/*<![CDATA[*/
            var locate = window.location.href
            function delineate(index){
                var p = window.location.href.split('?')[1];
                var a = {};
                if(p){ a = p.split('&');
                return a[index].split('=')[1]
                }
            }

            window.onload=function(){
                var isexpiredStr = $('#isexpiredid')[0];
                if(isexpiredStr.value=="true"){
                   var msg=$('#resmsgid')[0];
                  
                    setLoading2(msg.value, 'ajaxResult', 'errorFB');
                }
 	        }
            function activateButton1(e){
                var taxid = $('#taxid')[0];
                if ($("#checkbox:checked").length > 0){
                        if((!taxid.value || trimStr(taxid.value).length == 0 || trimStr(taxid.value).length > 15 || taxid.value.replace(/[^\w\s_\-\'\"]+/g, '') != taxid.value)) {
                              setLoading2("Enter your TAX ID number.", 'ajaxResult', 'errorFB');
                            document.getElementById('isubmit').value='false';
                        }else{
                            $('#button').css({
                            backgroundPosition: 'left bottom'
                            });
                        $('#button').removeAttr("disabled");
                    }
                }
                else {
                    $('#button').css({
                        backgroundPosition: 'left top'
                    });
                    $('#button').attr("disabled", "disabled");
                }
            }
             function changeCheckVal(){
                 var taxid = $('#taxid')[0];
                if ($("#checkbox:checked").length > 0){
                        if(!(!taxid.value || trimStr(taxid.value).length == 0 || trimStr(taxid.value).length > 15 || taxid.value.replace(/[^\w\s_\-\'\"]+/g, '') != taxid.value)) {
                          activateButton1();
                        }
                }
            }
           /*]]>*/

</script>
</body>
</html>
