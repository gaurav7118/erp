<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.krawler.common.util.StringUtil" %>
<%@ page import="com.krawler.esp.web.resource.Links"%>
<%@ page import="com.krawler.common.util.URLUtil"%>
<%@ page import="java.net.URLEncoder"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.spring.sessionHandler.sessionHandlerImpl"/>
<%
	String subdomain = null;
	String uri = null;
	String redirectUri = null;
	
	String newDomain = request.getParameter("n");

	if(!StringUtil.isNullOrEmpty(newDomain)){
        uri = URLUtil.getPageURL(request, Links.loginpageFull, newDomain);
		subdomain = newDomain;
	}
	else {
		uri = URLUtil.getPageURL(request, Links.loginpageFull);
		subdomain = URLUtil.getDomainName(request);
	}
	
	String logoutUrl = this.getServletContext().getInitParameter("casServerLogoutUrl");
	if( StringUtil.isNullOrEmpty(logoutUrl)){
		redirectUri = uri + "login.html";
	}
	else{
		redirectUri = logoutUrl + String.format("?url=%s&subdomain=%s",URLEncoder.encode(uri, "UTF-8"), subdomain);
	}
	
	sessionbean.destroyUserSession(request, response);
	response.sendRedirect(redirectUri);
%>
