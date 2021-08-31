<!--
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
-->

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>



<html xmlns="http://www.w3.org/1999/xhtml">
<head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <title>Deskera - ${model.pageTitle}</title>
        <link href="../style/error.css" rel="stylesheet" type="text/css" />
        <link rel="shortcut icon" href="../images/deskera.png"/>
</head>
<body>
<div id="top">
  <div class="top-wapper"> <a href="../"><div id="logo"></div></a>
    <div>
      <div id="menu">
        <ul>
          <li class="current"><a title="Home" href="./">Home</a></li>
          <li><a title="SignUp" href="http://signup.deskera.com/?ref=error">Sign Up</a></li>
          <li><a title="Blog" href="http://blog.deskera.com/">Blog</a></li>
          <li><a title="Forum" href="http://forum.deskera.com/">Forum</a></li>
          <li ><a title="Feedback" href="http://feedback.deskera.com/">Feedback</a></li>
        </ul>
      </div>
    </div>
  </div>
</div>
<div class="pagenotfound">
  <p style="padding: 0px 0pt 0pt; text-align: center; height: 223px;"><img id="errimg" src="../images/${model.errImgPath}.jpg" width="250" height="235" /></p>
  <p class="error" style="text-align: center; color:#A70808;" id="errmsg">${model.errMsg}</p>
  <div class="content-list" style="width: 91%;">
 <c:choose>
   <c:when test="${model.errReason=='alreadyloggedin' && !empty model.newDomain && !empty model.subdomainFromSession}">
    <p class="error-hd"><strong>You may do the following:</strong></p>
    <ul>
      <li>Click <a href="../jspfiles/signOut2.jsp?n=${model.newDomain}" class="highlight2">here to sign out from logged-in account</a> and continue to sign in to new account, or</li>
      <li>Click <a href="../a/${model.subdomainFromSession}/" class="highlight2">here to go back to already logged-in Deskera account</a></li>
    </ul>
  </c:when>
<c:otherwise>

    <p class="error-hd"><strong>You may not be able to visit this page because of:</strong></p>
    <ul>
      <li>An <span class="highlight2">out-of-date bookmark/favourite</span></li>
      <li>A <span class="highlight2">mis-typed address</span></li>
      <li>You have <span class="highlight2">no access</span> to this page</li>
      <li>An error has occurred while processing your request.</li>
          <li>A link from an <span class="highlight2">out-of-date mail</span></li>
    </ul>
</c:otherwise>
</c:choose>

        <div style="clear:both"></div>
        <p class="error-hd">Please contact your Deskera administrator or write to us at <img src="../images/support-yellow.gif" width="120" height="14" align="absmiddle" /></p>
  </div>
</div>
</body>
</html>

