/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.common.filters;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.CompanyContextHolder;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.spring.common.KwlCommonBeanUtils;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MultivaluedMap;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import io.jsonwebtoken.Jwts;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

public class RestServiceFilter implements ContainerRequestFilter {

    private static Logger _logger = Logger.getLogger(RestServiceFilter.class.getName());

    private KwlCommonBeanUtils kwlCommonBeanUtils;
    private companyDetailsDAO companyDetailsDAOObj;

    public void setkwlCommonBeanUtils(KwlCommonBeanUtils kwlCommonBeanUtils) {
        this.kwlCommonBeanUtils = kwlCommonBeanUtils;
    }

    public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj) {
        this.companyDetailsDAOObj = companyDetailsDAOObj;
    }

    @Override
    public ContainerRequest filter(ContainerRequest cr) {
        System.out.println("ERP*******inside fitler");
        String restauthapply = ConfigReader.getinstance().get("restauthapply");
        MultivaluedMap<String, String> queryParams = cr.getQueryParameters();
        boolean avoidauthentication=false;
        URI baseURI = cr.getBaseUri();
        String absolutePath = cr.getAbsolutePath().toString();
        String urlVersion = absolutePath.substring(baseURI.toString().length());
        if (!StringUtil.isNullOrEmpty(urlVersion)) {
            String versionString = urlVersion.substring(0, urlVersion.indexOf("/"));
            urlVersion = urlVersion.substring(versionString.length(), urlVersion.length());
        }
        String mediaType = cr.getMediaType() == null ? "" : cr.getMediaType().getType()+"/"+cr.getMediaType().getSubtype();
        String methodtype = cr.getMethod();
        try {
            /* To provide url and avoiding authetication*/
            if (methodtype.equalsIgnoreCase("GET") && Constants.GET_URL_SKIP_AUTH_LIST.contains(urlVersion)) {
                avoidauthentication = true;
            } else if (queryParams != null && !queryParams.isEmpty() && queryParams.containsKey(Constants.RES_REQUEST)) {
                if (queryParams.get(Constants.RES_REQUEST) != null && queryParams.get(Constants.RES_REQUEST).size() > 0) {
                    JSONObject requestObj = new JSONObject(queryParams.getFirst(Constants.RES_REQUEST));
                    if (requestObj.has(Constants.isdefaultHeaderMap) && requestObj.optBoolean(Constants.isdefaultHeaderMap) == true) {
                        avoidauthentication = true;
                    }
                }
            } 
            else if (!mediaType.equals(MediaType.MULTIPART_FORM_DATA)&& methodtype.equalsIgnoreCase("POST")) {
                String inputDataString = null;
                InputStream innew = cr.getEntityInputStream();
                if (innew != null) {
                    inputDataString = StringUtil.getJsonStringFromInputstream(innew);
                    JSONObject requestObj=new JSONObject();
                    JSONArray jArray =new JSONArray();
                    
                    if (!cr.getQueryParameters().containsKey("multiplecompanies")) {
                        requestObj = new JSONObject(inputDataString);
                        if (requestObj.has(Constants.isdefaultHeaderMap) && requestObj.optBoolean(Constants.isdefaultHeaderMap) == true) {
                            avoidauthentication = true;
                        }
                    } else {
                        jArray = new JSONArray(inputDataString);
                        for (int i = 0; i < jArray.length(); i++) {
                            JSONObject jObj = jArray.getJSONObject(i);
                            if (jObj.has(Constants.isdefaultHeaderMap) && jObj.optBoolean(Constants.isdefaultHeaderMap) == true) {
                                avoidauthentication = true;
                            }
                        }
                    }
                }
                byte[] requestEntity = inputDataString.getBytes();
                cr.setEntityInputStream(new ByteArrayInputStream(requestEntity));
            }
        } catch (JSONException ex) {
            Logger.getLogger(RestServiceFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (!StringUtil.isNullOrEmpty(restauthapply) && restauthapply.equals("1") && !avoidauthentication) {
            if (cr.getQueryParameters() == null || !cr.getQueryParameters().containsKey(Constants.REST_AUTH_TOKEN)) {
                /**
                 * TODO : Commented code due to support for mobile apps
                 */
                
                JSONObject response = kwlCommonBeanUtils.getErrorResponse("e14", "Auth token mandatory", null);
                ResponseBuilder builder = Response.status(Response.Status.UNAUTHORIZED).entity(response.toString());
                throw new WebApplicationException(builder.build());
            }
        }
      
        if (!mediaType.equals(MediaType.MULTIPART_FORM_DATA)) {
            String inputStreamData = null;
            InputStream in = cr.getEntityInputStream();
            if (in != null) {
                inputStreamData = StringUtil.getJsonStringFromInputstream(in);
                _logger.log(Level.INFO, "input stream data :{0}", inputStreamData);
                byte[] requestEntity = inputStreamData.getBytes();
                cr.setEntityInputStream(new ByteArrayInputStream(requestEntity));
            }
            if (cr.getQueryParameters().containsKey("multiplecompanies")) {
                try {
                    cr = populateMultipleCompanyContext(cr, inputStreamData);
                } catch (URISyntaxException ex) {
                    Logger.getLogger(RestServiceFilter.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                try {
                    cr = populateCompanyContext(cr, inputStreamData,avoidauthentication);
                } catch (ServiceException ex) {
                    Logger.getLogger(RestServiceFilter.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return cr;
    }

    private String fetchDomainFromToken(String token) {
        System.out.println("*******auth token-> " + token);
        String key = ConfigReader.getinstance().get(Constants.REMOTE_API_KEY);
        String subdomain = null;
        ResponseBuilder builder = null;
        String language = null;
        try {
            Claims claims = Jwts.parser().setSigningKey(key.getBytes()).parseClaimsJws(token).getBody();
            System.out.println("ACCOUNTING-> " + claims);
            if (claims != null && !claims.isEmpty()) {
                if (claims.containsKey(Constants.RES_CDOMAIN) && claims.get(Constants.RES_CDOMAIN) != null) {
                    subdomain = claims.get(Constants.RES_CDOMAIN).toString();
                }
            }
        } catch (ExpiredJwtException ex) {
            JSONObject response = kwlCommonBeanUtils.getErrorResponse("e13", "Auth token expired", language);
            builder = Response.status(Response.Status.UNAUTHORIZED).entity(response.toString());
            throw new WebApplicationException(builder.build());
        } catch (Exception ex) {
            JSONObject response = kwlCommonBeanUtils.getErrorResponse("e12", "Unauthenticated user", language);
            builder = Response.status(Response.Status.UNAUTHORIZED).entity(response.toString());
            throw new WebApplicationException(builder.build());
        }
        System.out.println("*******cdomain inside token-> " + subdomain);
        return subdomain;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    private ContainerRequest populateCompanyContext(ContainerRequest cr, String inputStreamData,boolean avoidauthentication) throws ServiceException {
        String language = null;
        String subdomain = null;
        ResponseBuilder builder = null;
        JSONObject requestObj = null;
        String userid = null;
        String companyid = null;
        try {
            MultivaluedMap<String, String> queryParams = cr.getQueryParameters();
            if (queryParams.containsKey(Constants.language)) {
                language = queryParams.getFirst(Constants.language);
            }
            if (queryParams.containsKey(Constants.RES_REQUEST)) {
                if (queryParams.get(Constants.RES_REQUEST) != null && queryParams.get(Constants.RES_REQUEST).size() > 0) {
                    requestObj = new JSONObject(queryParams.getFirst(Constants.RES_REQUEST));
                    if (requestObj.has(Constants.language)) {
                        language = requestObj.getString(Constants.language);
                    }
                    if (requestObj.has(Constants.RES_CDOMAIN)) {
                        subdomain = requestObj.getString(Constants.RES_CDOMAIN);
                    } else if (requestObj.has(Constants.COMPANY_SUBDOMAIN)) {
                        subdomain = requestObj.getString(Constants.COMPANY_SUBDOMAIN);
                    } else if (requestObj.has(Constants.companyKey)) {
                        companyid = requestObj.getString(Constants.companyKey);
                    } else if (requestObj.has(Constants.useridKey)) {
                        userid = requestObj.getString(Constants.useridKey);
                    }
                }
            } else if (queryParams.containsKey(Constants.RES_CDOMAIN)) {
                subdomain = queryParams.getFirst(Constants.RES_CDOMAIN);
            } else if (queryParams.containsKey(Constants.COMPANY_SUBDOMAIN)) {
                subdomain = queryParams.getFirst(Constants.COMPANY_SUBDOMAIN);
            } else if (queryParams.containsKey(Constants.companyKey)) {
                companyid = queryParams.getFirst(Constants.companyKey);
            } else if (queryParams.containsKey(Constants.useridKey)) {
                userid = queryParams.getFirst(Constants.useridKey);
            }
            if (StringUtil.isNullOrEmpty(userid) && StringUtil.isNullOrEmpty(companyid) && StringUtil.isNullOrEmpty(subdomain) && !StringUtil.isNullOrEmpty(inputStreamData)) {
                requestObj = new JSONObject(inputStreamData);
                if (requestObj.has(Constants.RES_CDOMAIN)) {
                    subdomain = requestObj.getString(Constants.RES_CDOMAIN);
                } else if (requestObj.has(Constants.COMPANY_SUBDOMAIN)) {
                    subdomain = requestObj.getString(Constants.COMPANY_SUBDOMAIN);
                } else if (requestObj.has(Constants.companyKey)) {
                    companyid = requestObj.getString(Constants.companyKey);
                } else if (requestObj.has(Constants.useridKey)) {
                    userid = requestObj.getString(Constants.useridKey);
                }
            }
            if (!avoidauthentication) {
//            if(false){
                String restauthapply = ConfigReader.getinstance().get("restauthapply");
                if (!StringUtil.isNullOrEmpty(restauthapply) && restauthapply.equals("1")) {
                    String cdomainToken = fetchDomainFromToken(queryParams.getFirst(Constants.REST_AUTH_TOKEN));

                    if (!StringUtil.isNullOrEmpty(cdomainToken)) {
                        if (!StringUtil.isNullOrEmpty(subdomain)) {
                            if (!cdomainToken.equals(subdomain)) {
                                JSONObject response = kwlCommonBeanUtils.getErrorResponse("e12", "User not authenticated", language);
                                builder = Response.status(Response.Status.UNAUTHORIZED).entity(response.toString());
                                throw new WebApplicationException(builder.build());
                            }
                        }
                        subdomain = cdomainToken;
                        CompanyContextHolder.setCompanySubdomain(subdomain);
//                        companyid = companyDetailsDAOObj.getCompanyid(subdomain);
//                        cr = populateCompanyId(cr, companyid, inputStreamData);
                    }
                }
            }
            if (!StringUtil.isNullOrEmpty(subdomain)) {
                CompanyContextHolder.setCompanySubdomain(subdomain);
            } else if (!StringUtil.isNullOrEmpty(companyid)) {
                CompanyContextHolder.setCompanyID(companyid);
            } else if (!StringUtil.isNullOrEmpty(userid)) {
                CompanyContextHolder.setUserID(userid);
            } else {
                clearCompanySubdomain();
            }
        } catch (JSONException ex) {
            Logger.getLogger(RestServiceFilter.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        _logger.log(Level.INFO, "Current subdomain : {0}", subdomain);
        return cr;
    }

    private void clearCompanySubdomain() {
        CompanyContextHolder.clearCompanySubdomain();
    }

    private ContainerRequest populateCompanyId(ContainerRequest cr, String companyId, String inputStreamData) throws JSONException {
        MultivaluedMap<String, String> queryParams = cr.getQueryParameters();
        JSONObject requestObj = null;
        boolean companySet = false;

        cr.getQueryParameters().add(Constants.companyKey, companyId);
        if (queryParams.containsKey(Constants.RES_REQUEST) && queryParams.get(Constants.RES_REQUEST) != null && queryParams.get(Constants.RES_REQUEST).size() > 0) {
            requestObj = new JSONObject(queryParams.getFirst(Constants.RES_REQUEST));
            cr.getQueryParameters().remove(Constants.RES_REQUEST);
            cr.getQueryParameters().add(Constants.RES_REQUEST, requestObj.toString());
            companySet = true;
        } else {
            requestObj = new JSONObject();
        }
        requestObj.put(Constants.companyKey, companyId);
        cr.getQueryParameters().add(Constants.RES_REQUEST, requestObj.toString());
        companySet = true;
        if (!companySet) {
            requestObj = new JSONObject(inputStreamData);
            requestObj.put(Constants.companyKey, companyId);
            cr.setEntityInputStream(new ByteArrayInputStream(requestObj.toString().getBytes()));
        }
        return cr;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    private ContainerRequest populateMultipleCompanyContext(ContainerRequest cr, String inputStreamData) throws URISyntaxException {
        String pathOfMultiRest = "";
        try {
            JSONObject inputStreamObj = new JSONObject();
            String request = null;
            cr.getQueryParameters().remove("multiplecompanies");
            String methodType = cr.getMethod();
            if (methodType.equals(Constants.POST)) {
                pathOfMultiRest = "/post";
            } else if (methodType.equals(Constants.GET)) {
                pathOfMultiRest = "/get";
            } else if (methodType.equals(Constants.DELETE)) {
                pathOfMultiRest = "/delete";
            }
            if (!StringUtil.isNullOrEmpty(inputStreamData)) {
                inputStreamObj.put(Constants.data, new JSONArray(inputStreamData));
                inputStreamObj.put("requestedurl", cr.getAbsolutePath().toString());
            } else {
                request = cr.getQueryParameters().get(Constants.RES_REQUEST).toString();
                request = request.substring(1, request.length() - 1);
                String authToken = cr.getQueryParameters().get(Constants.REST_AUTH_TOKEN).toString();
                inputStreamObj.put("requestedurl", cr.getAbsolutePath().toString() + "?"+Constants.RES_REQUEST+"=" + request+"&"+Constants.REST_AUTH_TOKEN+"="+authToken);
            }
            inputStreamObj.put("restmethodtype", methodType);
            inputStreamData = inputStreamObj.toString();
        } catch (JSONException ex) {
            Logger.getLogger(RestServiceFilter.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        URI baseURI = cr.getBaseUri();

        String absolutePath = cr.getAbsolutePath().toString();
        String urlVersion = absolutePath.substring(cr.getBaseUri().toString().length());
        if(!StringUtil.isNullOrEmpty(urlVersion)){
            urlVersion = urlVersion.substring(0, urlVersion.indexOf("/") + 1);
        }
        
        URI requestURI = new URI(cr.getBaseUri().toString()+ urlVersion + "multiple" + pathOfMultiRest);
        cr.setUris(baseURI, requestURI);
        cr.setEntityInputStream(new ByteArrayInputStream(inputStreamData.getBytes()));
        return cr;
    }
}
