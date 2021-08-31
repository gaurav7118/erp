/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.ws.rest.v1;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

@Component
@Path("/v1/multiple")
public class RestMultipleCompaniesSetup {

    @Autowired
    private APICallHandlerService apiCallHandler;

    public RestMultipleCompaniesSetup() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @POST
    @Path("/post")
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeGetServicesMultipleTimes(InputStream incomingData) throws JSONException {
        return executeServicesMultipleTimes(incomingData, Constants.POST);
    }

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Response executePostServicesMultipleTimes(InputStream incomingData) throws JSONException {
        return executeServicesMultipleTimes(incomingData, Constants.GET);
    }

    @DELETE
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeDeleteServicesMultipleTimes(InputStream incomingData) throws JSONException {
        return executeServicesMultipleTimes(incomingData, Constants.DELETE);
    }

    private Response executeServicesMultipleTimes(InputStream incomingData, String methodType) throws JSONException {
        JSONObject response = new JSONObject();
        String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
        JSONObject json = new JSONObject(inputData);
        String requestedUrl = json.getString("requestedurl");
        JSONArray responseArr = new JSONArray();
        try {
            if (!StringUtil.isNullOrEmpty(requestedUrl) && requestedUrl.contains("?request=")) {
                String[] tokens = requestedUrl.split("\\?");
                String endpoint = tokens[0];
                String data = tokens[1].substring(8);
                JSONArray dataArr = new JSONArray(data);
                for (int i = 0; i < dataArr.length(); i++) {
                    String forwardData = dataArr.getString(i);
                    responseArr.put(forwardMethod(endpoint, forwardData, methodType));
                }
            } else {
                String dataString = json.getString(Constants.data);
                JSONArray jArr = new JSONArray(dataString);
                for (int i = 0; i < jArr.length(); i++) {
                    String forwardData = jArr.getString(i);
                    responseArr.put(forwardMethod(requestedUrl, forwardData, methodType));
                }
            }
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(RestMultipleCompaniesSetup.class.getName()).log(Level.SEVERE, null, ex);
        }
        response.put("result", responseArr);
        return Response.status(200).entity(response.toString()).build();
    }

    private JSONObject forwardMethod(String endpoint, String forwardData, String methodType) throws JSONException, ServiceException {
        switch (methodType) {
            case Constants.POST:
                return apiCallHandler.restPostMethod(endpoint, forwardData);
            case Constants.GET:
                return apiCallHandler.restGetMethod(endpoint, forwardData);
            case Constants.DELETE:
                return apiCallHandler.restDeleteMethod(endpoint, forwardData);
            default:
                return null;
        }
    }
}