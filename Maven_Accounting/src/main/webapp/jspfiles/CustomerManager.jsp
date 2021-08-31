<%@page import="com.krawler.esp.database.*" %>
<%@page import="com.krawler.utils.json.base.*" %>
<%@page import="java.util.*" %>
<%@page import="org.apache.commons.fileupload.servlet.ServletFileUpload" %>
<%@page import="com.krawler.esp.handlers.*" %>
<%@page import="com.krawler.hql.accounting.StaticValues" %>
<jsp:useBean id="sessionbean" scope="session" class="com.krawler.spring.sessionHandler.sessionHandlerImpl" />
<%

    JSONObject jobj=new JSONObject();
    JSONObject obj=new JSONObject();
   boolean isFormSubmit = false;
    if(sessionbean.validateSession(request, response)){
        try {
            int mode;
            HashMap hm=null;
            if(ServletFileUpload.isMultipartContent(request)){
                hm=new FileUploadHandler().getItems(request);
                mode=Integer.parseInt(hm.get("mode").toString());
            }else
                mode = Integer.parseInt(request.getParameter("mode"));

            obj.put("success",true);
         } catch (Exception e) {
            obj.put("success", false);
            obj.put("msg", e.getMessage());
        } finally {
            jobj.put("valid", true);
            if(!isFormSubmit){
                jobj.put("data", obj);
            }else{
                jobj=obj;
            }
        }
    } else {
        sessionbean.destroyUserSession(request, response);
         jobj.put("valid", false);
    }

    out.println(jobj);
%>
