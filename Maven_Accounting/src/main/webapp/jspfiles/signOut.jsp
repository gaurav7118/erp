<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.krawler.common.util.StringUtil" %>
<%@ page import="com.krawler.esp.web.resource.Links"%>
<%@ page import="com.krawler.common.util.URLUtil"%>
<%@page import="java.net.URLEncoder"%>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.spring.sessionHandler.sessionHandlerImpl"/>
<%
	String _sO = request.getParameter("type");
	String uri = URLUtil.getPageURL(request, Links.loginpageFull);
	String redirectUri = "";
	String logoutUrl = this.getServletContext().getInitParameter("casServerLogoutUrl");
	if( StringUtil.isNullOrEmpty(logoutUrl)){
		redirectUri = uri + "login.html";
		if (!StringUtil.isNullOrEmpty(_sO)){
			redirectUri += ("?" + _sO);
		}
	}
	else{
		String subdomain = URLUtil.getDomainName(request);
		redirectUri = logoutUrl + String.format("?url=%s&subdomain=%s",URLEncoder.encode(uri, "UTF-8"), subdomain, _sO);
		if (!StringUtil.isNullOrEmpty(_sO)){
			redirectUri += ("&type=" + _sO);
		}
	}
	sessionbean.destroyUserSession(request, response);
	response.sendRedirect(redirectUri);
%>