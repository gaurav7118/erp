/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.ws.rest.v1;

/**
 *
 * @author krawler
 */

import com.krawler.common.service.ServiceException;
import java.io.InputStream;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.ws.service.InventoryService;
import com.krawler.spring.accounting.ws.service.WSUtilService;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
@Path("/v1/inventory")
public class InventoryRestService {
    
    @Autowired
    WSUtilService wsUtilObj;
    
    @Autowired
    InventoryService inventoryService;

    public InventoryRestService() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @GET
    @Path("/sequenceformat")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getSequenceFormats(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            if (!StringUtil.isNullObject(jobj)) {
                response = inventoryService.getSequenceFormats(jobj);
            } else {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            }
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
    @Path("/store")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
   public Response getStoreList(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            if (!StringUtil.isNullObject(jobj)) {
                response = inventoryService.getStoreList(jobj);
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
    @Path("/userbystore")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
   public Response getUsersfromStore(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            if (!StringUtil.isNullObject(jobj)) {
                response = inventoryService.getUsersfromStore(jobj);
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
    @Path("/cyclecount")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
   public Response getCycleCountList(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            if (!StringUtil.isNullObject(jobj)) {
                response = inventoryService.getCycleCountList(jobj);
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
    @Path("/availablestock")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
   public Response getStoreProductWiseAllAvailableStockDetailList(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            if (!StringUtil.isNullObject(jobj)) {
                response = inventoryService.getStoreProductWiseAllAvailableStockDetailList(jobj);
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
    @Path("/location")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
   public Response getStoreLocations(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            if (!StringUtil.isNullObject(jobj)) {
                response = inventoryService.getStoreLocations(jobj);
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
    @Path("/validate")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
   public Response validateCycleCountDate(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            if (!StringUtil.isNullObject(jobj)) {
                response = inventoryService.validateCycleCountDate(jobj);
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
    @Path("/cyclecountreport")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
   public Response getCycleCountReport(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            if (!StringUtil.isNullObject(jobj)) {
                response = inventoryService.getCycleCountReport(jobj);
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
 
    @POST
    @Path("/cyclecount")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveCycleCount(InputStream incomingData) {
        JSONObject response = new JSONObject();
        JSONObject inputObj = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            try {
                JSONObject resultObj = new JSONObject();
                resultObj.put(Constants.RES_success, true);
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                inputObj = new JSONObject(inputData);
                resultObj.put("data", inputData);
                JSONObject jobj = new JSONObject(inputData);

                response = inventoryService.saveCycleCount(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", inputObj, null);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), inputObj, ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", inputObj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
//    @GET
//    @Path("/extraitems")
//    @Produces(MediaType.APPLICATION_JSON)
//    @Consumes(MediaType.APPLICATION_JSON)
//   public Response getExtraItemList(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
//        JSONObject response = new JSONObject();
//        try {
//            if (!StringUtil.isNullObject(jobj)) {
//                response = inventoryService.getExtraItemList(jobj);
//            } else {
//                response = wsUtilObj.getErrorResponse("e01", jobj,null);
//            }
//        } catch (ServiceException ex) {
//            response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
//        } catch (JSONException ex) {
//            response = wsUtilObj.getErrorResponse("e01", jobj,null);
//        } catch (Exception ex) {
//            response = wsUtilObj.getErrorResponse("erp25", jobj,null);
//        }
//        return Response.status(200).entity(response.toString()).build();
//    }
     
    
    
}
