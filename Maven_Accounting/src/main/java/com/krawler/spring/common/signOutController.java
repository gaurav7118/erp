package com.krawler.spring.common;
import com.krawler.common.admin.User;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.web.resource.Links;
import com.krawler.common.util.URLUtil;
import java.net.URLEncoder;
import com.krawler.common.admin.AuditAction;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.util.Constants;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.servlet.mvc.AbstractController;
/**
 *
 * @author krawler
 */
public class signOutController extends AbstractController {

    private auditTrailDAO auditTrailObj;
    private sessionHandlerImpl sessionHandlerImplObj;
    
    public void setAuditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    public void setSessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj) {
        this.sessionHandlerImplObj = sessionHandlerImplObj;
    }
    @Override
    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {
        String _sO = request.getParameter("type");
        String uri = URLUtil.getPageURL(request, Links.loginpageFull);
        String redirectUri = "";
        try {
            if (request.getSession().getAttribute("initialized") != null && (!StringUtil.isNullOrEmpty(_sO) && _sO.equalsIgnoreCase("signout"))) {
                insertLogoutEntryInAuditTrail(request);
            }
            String logoutUrl = this.getServletContext().getInitParameter(
                    "casServerLogoutUrl");
            if (StringUtil.isNullOrEmpty(logoutUrl)) {
                redirectUri = uri + "login.html";
                if (!StringUtil.isNullOrEmpty(_sO)) {
                    redirectUri += ("?" + _sO);
                }
            } else {
                String subdomain = URLUtil.getDomainName(request);
                redirectUri = logoutUrl
                        + String.format("?url=%s&subdomain=%s", URLEncoder.encode(uri, "UTF-8"), subdomain, _sO);
                if (!StringUtil.isNullOrEmpty(_sO)) {
                    redirectUri += ("&type=" + _sO);
                }
            }
            sessionHandlerImplObj.destroyUserSession(request, response);
            response.sendRedirect(redirectUri);
        } catch (Exception exc) {
            exc.printStackTrace();
        } finally {
            return new ModelAndView("jsonView", "model", "");
        }
    }
     /*
     * a method to insert a logout entry in to audit trail
     */
    private void insertLogoutEntryInAuditTrail(HttpServletRequest request) throws Exception{
                String _sO = request.getParameter("type");
                Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                auditRequestParams.put(Constants.reqHeader, request.getHeader(Constants.reqHeader));
                auditRequestParams.put(Constants.remoteIPAddress, request.getRemoteAddr());
                auditRequestParams.put(Constants.useridKey, sessionHandlerImpl.getUserid(request));
                String msg = "User " + sessionHandlerImpl.getUserFullName(request) + " has logged out successfully";
                auditTrailObj.insertAuditLog(AuditAction.LOGOUT_SUCCESS,msg, auditRequestParams, "0");
    }
}
