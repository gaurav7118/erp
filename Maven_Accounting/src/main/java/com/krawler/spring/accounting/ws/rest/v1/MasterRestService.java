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
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.accounting.ws.service.MasterService;
import com.krawler.spring.accounting.ws.service.WSServiceUtil;
import com.krawler.spring.accounting.ws.service.WSUtilService;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.AccountException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
@Path("/v1/master")
public class MasterRestService {

    @Autowired
    private MasterService masterService;
    @Autowired
    WSUtilService wsUtilObj;

    public MasterRestService() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }

    @GET
    @Path("/product")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProduct(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getProduct(jobj);

            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/inspectiontemplate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInspectionTemplateList(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getInspectionTemplateList(jobj);

            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/product-category")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductCategory(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getProductCategory(jobj);

            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    @GET
    @Path("/warehouses")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWarehouseItems(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getWarehouse(jobj);

            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    @GET
    @Path("/locations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLocations(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getLocation(jobj);

            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    @GET
    @Path("/batches")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNewBatches(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getNewBatches(jobj);

            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    @GET
    @Path("/levels")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLevels(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getLevels(jobj);

            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    @GET
    @Path("/storemasters")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStoreMasters(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getStoreMasters(jobj);

            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    @GET
    @Path("/serials")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNewSerials(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getNewSerials(jobj);

            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    @POST
    @Path("/batchremaningquantitys")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBatchRemaningQuantity(InputStream incomingData) {
        JSONObject response = new JSONObject();
        JSONObject jobj = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            try {
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = masterService.getBatchRemaningQuantity(jobj);

            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/customer")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getCustomers(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getCustomers(jobj);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/asset")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAssetDetails(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getAsset(jobj);
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/tax")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getTax(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getTax(jobj);
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/currency-exchange")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getCurrencyExchange(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getCurrencyExchange(jobj);
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @POST
    @Path("/tax")
    public Response saveTax(InputStream incomingData) {
        JSONObject response = new JSONObject();
        JSONObject jobj = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            try {
                response.put("success", true);                
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                response.put("data", inputData);
                jobj = new JSONObject(inputData);
                response = masterService.saveTax(jobj);

            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }

        return Response.status(200).entity(response.toString()).build();
    }

    @POST
    @Path("/project")
    public Response saveProjectDetails(InputStream incomingData) {
        JSONObject response = new JSONObject();
        JSONObject jobj = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            try {

                response.put("success", true);
//            HashMap companyMap = new HashMap();

                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                response.put("data", inputData);
                jobj = new JSONObject(inputData);
                response = masterService.saveProjectDetails(jobj);

            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/cost-center")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCostCenter(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            response = masterService.getCostCenter(jobj);
            response.put("success", true);
        } catch (JSONException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
        } catch (ServiceException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/cost-center-field-params")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCostCenterFromMaster(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            response = masterService.getCostCenterFromFieldParams(jobj);
            response.put("success", true);
        } catch (JSONException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
        } catch (ServiceException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @DELETE
    @Path("/cost-center")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCostCenter(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.deleteCostCenter(jobj);
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/uom")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUnitOfMeasure(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            response = masterService.getUnitOfMeasure(jobj);
            response.put("success", true);
        } catch (JSONException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
        } catch (ServiceException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/mastergroups")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMasterGroups(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            response = masterService.getMasterGroups(jobj);
        } catch (JSONException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
        } catch (ServiceException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/mastercustoms")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMasterItemsForCustomFoHire(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            response = masterService.getMasterItemsForCustomFoHire(jobj);
        } catch (JSONException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
        } catch (ServiceException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    
    @POST
    @Path("/term")
    public Response saveTerm(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            try {

                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                response.put("data", inputData);
                JSONObject jobj = new JSONObject(inputData);
                response = masterService.saveTerm(jobj);

            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @DELETE
    @Path("/project")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProjectDetails(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.deleteProjectDetails(jobj);
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/term")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTerm(@QueryParam(Constants.RES_REQUEST) JSONObject paramJobj) {
        JSONObject response = new JSONObject();
        if (paramJobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            try {
                response = masterService.getTerm(paramJobj);
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/currency")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCurrency(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        try {
            response = masterService.getAllCurrency(jobj);
        } catch (JSONException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
        } catch (ServiceException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/country")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCountry() {
        JSONObject response = new JSONObject();
        try {
            response = masterService.getAllCountry();
        } catch (JSONException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
        } catch (ServiceException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/timezone")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTimeZone() {
        JSONObject response = new JSONObject();
        try {
            response = masterService.getAllTimeZone();
        } catch (JSONException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
        } catch (ServiceException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/state")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStates(@QueryParam("countryid") String countryid) {
        JSONObject response = new JSONObject();
        if (countryid == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            try {
                response = masterService.getAllStates(countryid);
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/dateformat")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDateFormat() {
        JSONObject response = new JSONObject();
        try {
            response = masterService.getDateFormat();
        } catch (JSONException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
        } catch (ServiceException ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
        } catch (Exception ex) {
            Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
            response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @POST
    @Path("/customer")
    public Response saveCustomer(InputStream incomingData) {
        JSONObject response = new JSONObject();
        JSONObject jobj = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            try {
                response.put("success", true);
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                response.put("data", inputData);
                jobj = new JSONObject(inputData);
                response = masterService.saveCustomer(jobj);

            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }

        return Response.status(200).entity(response.toString()).build();
    }
    
    @DELETE
    @Path("/customer")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCustomer(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.deleteCustomer(jobj);
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @POST
    @Path("/product-replacement")
    public Response saveProductReplacement(InputStream incomingData) {
        JSONObject response = new JSONObject();
        JSONObject jobj = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            try {
                response.put("success", true);
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                response.put("data", inputData);
                jobj = new JSONObject(inputData);
                System.out.println("input-> "+jobj);
                response = masterService.saveProductReplacement(jobj);

            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }

        return Response.status(200).entity(response.toString()).build();
    }

    @POST
    @Path("/product-maintenance")
    public Response saveProductMaintenance(InputStream incomingData) {
        JSONObject response = new JSONObject();
        JSONObject jobj = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            try {
                response.put("success", true);
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                response.put("data", inputData);
                jobj = new JSONObject(inputData);
                System.out.println("input-> "+jobj);
                response = masterService.saveProductMaintenance(jobj);

            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }

        return Response.status(200).entity(response.toString()).build();
    }
    
    @POST
    @Path("/quantityconsumptions")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveInventoryQuantityConsumption(InputStream incomingData) {
        JSONObject response = new JSONObject();
        JSONObject jobj = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            try {
                response.put("success", true);
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
//                response.put("data", inputData);
                jobj = new JSONObject(inputData);
//                System.out.println("input-> " + jobj);
                response = masterService.saveInventoryConsumption(jobj);

            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }

        return Response.status(200).entity(response.toString()).build();
    }

    @DELETE
    @Path("/product-replacement")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProductReplacement(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.deleteProductReplacement(jobj);
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @DELETE
    @Path("/product-maintenance")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProductMaintenance(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.deleteProductMaintenance(jobj);
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/masteritems")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getMasterItems(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getMasterItems(jobj);
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/customcombodata")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response getCustomCombodata(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getCustomCombodata(jobj);
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    
    @GET
    @Path("/default-columns")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDefaultColumns(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                
                response = masterService.getDefaultColumns(jobj);
                
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @POST
    @Path("/payment-milestone")
    public Response savePaymentMileStoneDetails(InputStream incomingData) {
        JSONObject response = new JSONObject();
        JSONObject jobj = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            try {

                response.put(Constants.RES_success, true);

                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                response.put(Constants.RES_data, inputData);
                jobj = new JSONObject(inputData);
                response = masterService.savePaymentMileStoneDetails(jobj);

            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
            @FormDataParam("attachment") InputStream uploadedInputStream,
            @FormDataParam("attachment") FormDataContentDisposition fileDetail) {
        JSONObject response = new JSONObject();
        JSONObject jobj = new JSONObject();
        if (uploadedInputStream == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            try {

                response.put(Constants.RES_success, true);

                response = masterService.uploadImage(uploadedInputStream, fileDetail);

            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @DELETE
    @Path("/deletefile")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUploadedFile(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.deleteUploadedFile(jobj);
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/salesanalysischart")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSalesAnalysisChart(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getSalesAnalysisChart(jobj);
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @POST
    @Path("/customercheckinout")
    public Response saveCustomerCheckInOut(InputStream incomingData) {
        JSONObject response = new JSONObject();
        JSONObject jobj = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            try {
                response.put("success", true);
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                response.put("data", inputData);
                jobj = new JSONObject(inputData);
                response = masterService.saveCustomerCheckInOut(jobj);

            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }

        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/customercheckin")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomerCheckIn(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getCustomerCheckIn(jobj);
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/salessummaryreport")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSalesSummaryReport(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getSalesSummaryReport(jobj);
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", null, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), null, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", null, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/invoice-tax-terms")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendInvoiceTermsToCRM(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                JSONObject jobjTax = masterService.getTax(jobj);
                response.put("taxdata", jobjTax.getJSONArray(Constants.RES_data));
                
//                JSONObject jobjTerm = masterService.getTerm(jobj);
                JSONArray jarr = masterService.getInvoiceTerms(jobj); // RemoteAPI method called (SDP-8924) 
                response.put("termdata", jarr);
                response.put(Constants.RES_success, true);
                
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/entity-custom-data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendEntityCustomDataToCRM(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = masterService.getEntityCustomData(jobj); 
                response.put(Constants.RES_success, true);
                
            } catch (JSONException ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (Exception ex) {
                Logger.getLogger(MasterRestService.class.getName()).log(Level.SEVERE, null, ex);
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }    
}
