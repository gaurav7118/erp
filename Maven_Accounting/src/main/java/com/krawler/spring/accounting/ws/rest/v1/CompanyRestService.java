/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.ws.rest.v1;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.web.resource.Links;
import com.krawler.spring.accounting.ws.service.CompanyService;
import com.krawler.spring.accounting.ws.service.WSUtilService;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 *
 * @author krawler
 */
@Component
@Path("/v1/company")
public class CompanyRestService {

    @Autowired
    CompanyService companyService;

    @Autowired
    WSUtilService wsUtilObj;

    public CompanyRestService() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @GET
    @Path("/sequence-format")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getSequenceFormat(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            if (!StringUtil.isNullObject(jobj)) {
                response = companyService.getSequenceFormat(jobj);
            } else {
                response = wsUtilObj.getErrorResponse("e01", jobj,null);
            }
        } catch (ServiceException ex) {
            response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
        } catch (JSONException ex) {
            response = wsUtilObj.getErrorResponse("e01", jobj,null);
        } catch (Exception ex) {
            response = wsUtilObj.getErrorResponse("erp25", jobj,null);
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/deactivate")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deactivateCompany(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            if (!StringUtil.isNullObject(jobj)) {
                response = companyService.deactivateCompany(jobj);
            } else {
                response = wsUtilObj.getErrorResponse("e01", jobj,null);
            }
        } catch (ServiceException ex) {
            response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
        } catch (JSONException ex) {
            response = wsUtilObj.getErrorResponse("e01", jobj,null);
        } catch (Exception ex) {
            response = wsUtilObj.getErrorResponse("erp25", jobj,null);
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/defaultfieldsmobileapplication")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getDefaultFieldsforMobileSetup(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            response = companyService.getDefaultFieldsforMobileSetup(jobj);
        } catch (ServiceException ex) {
            response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
        } catch (JSONException ex) {
            response = wsUtilObj.getErrorResponse("e01", jobj, null);
        } catch (Exception ex) {
            response = wsUtilObj.getErrorResponse("erp25", jobj, null);
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserList(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            if (!StringUtil.isNullObject(jobj)) {
                response = companyService.getUserList(jobj);
            } else {
                response = wsUtilObj.getErrorResponse("e01", jobj,null);
            }
        } catch (ServiceException ex) {
            response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
        } catch (JSONException ex) {
            response = wsUtilObj.getErrorResponse("e01", jobj,null);
        } catch (Exception ex) {
            response = wsUtilObj.getErrorResponse("erp25", jobj,null);
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/updates")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getUpdates(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) throws ServiceException, JSONException {
        /**
         * Called from remoteAPI case 9: no usage found till now in crm and apps
         * project. problem facing for executing the code. once usage will find
         * then rework will be done
         */
        JSONObject result = new JSONObject();
        result = companyService.getUpdates(jobj);
        return Response.status(200).entity(result.toString()).build();
    }

    @GET
    @Path("/year-lock")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getYearLock(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            if (!StringUtil.isNullObject(jobj)) {
                response = companyService.getYearLock(jobj);
            } else {
                response = wsUtilObj.getErrorResponse("e01", jobj,null);
            }
        } catch (ServiceException ex) {
            response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
        } catch (JSONException ex) {
            response = wsUtilObj.getErrorResponse("e01", jobj,null);
        } catch (Exception ex) {
            response = wsUtilObj.getErrorResponse("erp25", jobj,null);
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/account/pm")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAccountList(@QueryParam(Constants.RES_REQUEST) JSONObject jobj
    ) {
        JSONObject response = new JSONObject();
        if (!StringUtil.isNullObject(jobj)) {
            try {
                response = companyService.getAccountList(jobj);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        } else {
            response = wsUtilObj.getErrorResponse("e01", jobj, null);
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/nextautosequencenumber")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getNextAutonumber(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (!StringUtil.isNullObject(jobj)) {
            try {
                response = companyService.getNextAutonumber(jobj);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        } else {
            response = wsUtilObj.getErrorResponse("e01", jobj, null);
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveCompany(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null,null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                response.put("success", true);
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                response.put("data", inputData);
                jobj = new JSONObject(inputData);
                System.out.println("jobj :: "+jobj.toString());
                response = companyService.saveCompany(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj,null);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj,null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @POST
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response editUser(InputStream incomingData, @Context HttpServletRequest request) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null,null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                response.put("success", true);
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                response.put("data", inputData);
                jobj = new JSONObject(inputData);
                if (jobj.has("createnew") && jobj.getBoolean("createnew")) {
                    String path = com.krawler.common.util.URLUtil.getDomainURL(jobj.optString(Constants.RES_CDOMAIN), false);
                    String servPath = request.getServletPath();
                    Map<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put(Constants.RES_CDOMAIN, jobj.optString(Constants.RES_CDOMAIN));
                    requestParams.put("path", path);
                    requestParams.put("servPath", servPath);
                    String uri = URLUtil.getPageURLJson(requestParams, Links.loginpageFull);
                    jobj.put(Constants.url, uri);
                    response = companyService.createUser(jobj);
                } else {

                    response = companyService.editUser(jobj);
                    return Response.status(200).entity(response.toString()).build();
                }
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj,null);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj,null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCompany(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                response = companyService.deleteCompany(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj,null);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj,null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/user-permission")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getUserPermissions(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                    response = companyService.getUserPermissions(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj,null);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj,null);
            }
        }
        return Response.status(Response.Status.OK).entity(response.toString()).build();
    }

    @GET
    @Path("/isexist")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response isCompanyExist(@QueryParam(Constants.RES_REQUEST) JSONObject jobj){
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                if (!(jobj.has("cdomain") || jobj.has("companyid"))) {
                    response = wsUtilObj.getErrorResponse("e01", jobj,null);
                } else {
                    response = companyService.isCompanyExists(jobj);
                }
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj,null);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj,null);
            }
        }
        return Response.status(Response.Status.OK).entity(response.toString()).build();
    }
    
    @DELETE
    @Path("/user")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                response = companyService.deleteUser(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj,null);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj,null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }    
    
      @GET
    @Path("/verifylogin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response verifyLogin(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                response = companyService.verifyLogin(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj,null);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj,null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    } 

  @POST
    @Path("/user/activate")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response activateUser(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                response.put("success", true);
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = companyService.activateUser(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @POST
    @Path("/user/deactivate")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deactivateUser(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                response.put("success", true);
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = companyService.deactivateUser(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @POST
    @Path("/user/assign-role")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response assignRole(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                response.put("success", true);
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = companyService.assignRole(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("user/isexist")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response isUserExist(@QueryParam(Constants.RES_REQUEST) JSONObject jobj){
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                if (!((jobj.has("userName") && !jobj.has(Constants.lid) && !StringUtil.isNullOrEmpty(jobj.getString("userName")))
                || (jobj.has("username") && !jobj.has(Constants.lid) && !StringUtil.isNullOrEmpty(jobj.getString("username"))) || (jobj.has(Constants.useridKey) && !StringUtil.isNullOrEmpty(jobj.getString(Constants.useridKey))))) {
                    response = wsUtilObj.getErrorResponse("e01", jobj,null);
                } else {
                    response = companyService.isUserExists(jobj);
                }
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj,null);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj,null);
            }
        }
        return Response.status(Response.Status.OK).entity(response.toString()).build();
    }  
    
    @GET
    @Path("/audittrail")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response auditTrailDetails(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            if (StringUtil.isNullObject(jobj)) {
                response.put(Constants.RES_success, false);
                response.put(Constants.RES_MESSAGE, "Insufficient Data");
            } else {
                if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.companyKey)) || !StringUtil.isNullOrEmpty(jobj.optString(Constants.RES_CDOMAIN))) {
                    if (jobj.optBoolean("selfservice")) {
                        if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.username))) {
                            response = companyService.getAuditTrails(jobj);
                        } else {
                            response.put(Constants.RES_success, false);
                            response.put(Constants.RES_MESSAGE, "username is missing");
                        }
                    } else {
                        if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.useridKey))) {
                            response = companyService.getAuditTrails(jobj);
                        } else {
                            response.put(Constants.RES_success, false);
                            response.put(Constants.RES_MESSAGE, "userid is missing");
                        }
                    }
                } else {
                    response.put(Constants.RES_success, false);
                    response.put(Constants.RES_MESSAGE, "companyid / company subdomain is missing");
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(CompanyRestService.class.getName()).log(Level.SEVERE, null, ex);
            try {
                response.put(Constants.RES_success, false);
                response.put(Constants.RES_MESSAGE, ex.getMessage());
            } catch (JSONException ex1) {
                Logger.getLogger(CompanyRestService.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(CompanyRestService.class.getName()).log(Level.SEVERE, null, ex);
            try {
                response.put(Constants.RES_success, false);
                response.put(Constants.RES_MESSAGE, ex.getMessage());
            } catch (JSONException ex1) {
                Logger.getLogger(CompanyRestService.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(CompanyRestService.class.getName()).log(Level.SEVERE, null, ex);
            try {
                response.put(Constants.RES_success, false);
                response.put(Constants.RES_MESSAGE, ex.getMessage());
            } catch (JSONException ex1) {
                Logger.getLogger(CompanyRestService.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (ParseException ex) {
            Logger.getLogger(CompanyRestService.class.getName()).log(Level.SEVERE, null, ex);
            try {
                response.put(Constants.RES_success, false);
                response.put(Constants.RES_MESSAGE, ex.getMessage());
            } catch (JSONException ex1) {
                Logger.getLogger(CompanyRestService.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        return Response.status(Response.Status.OK).entity(response.toString()).build();
    }
    
    @GET
    @Path("/dateformat")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAllDateFormats(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = companyService.getAllDateFormats(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(Response.Status.OK).entity(response.toString()).build();
    }
    
    @GET
    @Path("/timezone")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAllTimeZones(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = companyService.getAllTimeZones(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(Response.Status.OK).entity(response.toString()).build();
    }

    @GET
    @Path("/user/details/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getAllUserDetails(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
         JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = companyService.getAllUserDetails(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(Response.Status.OK).entity(response.toString()).build();
    }
    
    @POST
    @Path("/user/save")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveUsers(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = companyService.saveUsers(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }  
    
    @GET
    @Path("/url")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUrls(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = companyService.getUrls(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/gstconfiguration")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGSTConfiguration(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                /**
                 * Get GST Configuration : 
                 * 1) Master Data : FiledParams, FieldComboData
                 * 2) Company Line Level Term : LineLevelTerm
                 * 3) Get Product Term Entity Base Rate : EntityBasedLineLevelTerm
                 * 4) Get Product Category GST Rule Map Details : ProductCategoryGstRulesMappping.
                 */
                response = companyService.getGSTConfiguration(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @POST
    @Path("/deskeraproxydetails")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveDeskeraProxyDetails(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null,null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                response.put("success", true);
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                response.put("data", inputData);
                jobj = new JSONObject(inputData);
                System.out.println("jobj :"+jobj.toString());
                response = companyService.saveDeskeraProxyDetails(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj,null);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj,null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    /**
     * Rest method to save password policy.
     * @param incomingData
     * @return 
     */
    @POST
    @Path("/passwordpolicy")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveOrUpdatePasswordPolicy(InputStream incomingData) {
        JSONObject response = new JSONObject();
        JSONObject jobj = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = companyService.saveOrUpdatePasswordPolicy(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    /**
     * Rest Method to returns user role name, roll id and display name in JSON
     * array And Below method moved from permissionHandlerController.
     *
     * @param jobj
     * @return
     */
    @GET
    @Path("/roles")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getRoles(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            if (!StringUtil.isNullObject(jobj)) {
                response = companyService.getRoles(jobj);
            } else {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            }
        } catch (ServiceException ex) {
            response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
        } catch (Exception ex) {
            response = wsUtilObj.getErrorResponse("erp25", jobj, null);
        }
        return Response.status(200).entity(response.toString()).build();
    }

}
