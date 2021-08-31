/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.accounting.ws.rest.v1;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.ws.service.ContractService;
import com.krawler.spring.accounting.ws.service.WSUtilService;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
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

@Component
@Path("/v1/contract")
public class ContractRestService {
    
    public ContractRestService() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }
    private static final Logger _logger = Logger.getLogger(ContractRestService.class.getName());
    @Autowired
    ContractService contractServiceObj;
    
    @Autowired
    WSUtilService wsUtilObj;
    
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContractDetails(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {  
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {                
                response = contractServiceObj.getContractDetails(jobj);        
                response.put("success", true);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", jobj,ex.getMessage());
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", jobj,ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/term")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContractTermDetails(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {        
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                response = contractServiceObj.getContractTermDetails(jobj);
                response.put("success", true);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", jobj,ex.getMessage());
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", jobj,ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }    

    @GET
    @Path("/invoice")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContractInvoiceDetails(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {        
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                response = contractServiceObj.getContractInvoiceDetails(jobj);        
                response.put("success", true);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", jobj,ex.getMessage());
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", jobj,ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }    
    
   /* @GET
    @Path("/replacementInvoice")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContractReplacementInvoiceDetails(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {        
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                jobj = wsUtilObj.populateAdditionalInformation(jobj);
                response = contractServiceObj.getContractReplacementInvoiceDetails(jobj);
                response.put("success", true);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj,ex.getMessage());
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj,ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }    
    
    @GET
    @Path("/maintenanceInvoice")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContractMaintenanceInvoiceDetails(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {        
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                jobj = wsUtilObj.populateAdditionalInformation(jobj);
                response = contractServiceObj.getContractMaintenanceInvoiceDetails(jobj);        
                response.put("success", true);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj,ex.getMessage());
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj,ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }    */
    
    /*@GET
    @Path("/doitems")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContractDOItems(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {        
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                jobj = wsUtilObj.populateAdditionalInformation(jobj);
                response = contractServiceObj.getContractNormalDOItemDetails(jobj);        
                response.put("success", true);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj,ex.getMessage());
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj,ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }    
    */
    @GET
    @Path("/doitem")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContractDOItem(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {        
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                response = contractServiceObj.getContractNormalDOItem(jobj);
                response.put("success", true);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", jobj,ex.getMessage());
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", jobj,ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }    
    /*
    @GET
    @Path("/replacementdoitems")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContractReplacementDOItems(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {        
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                jobj = wsUtilObj.populateAdditionalInformation(jobj);
                response = contractServiceObj.getContractReplacementDOItemDetails(jobj);        
                response.put("success", true);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj,ex.getMessage());
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj,ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }    */

    @GET
    @Path("/doitem-replacement")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContractReplacementDOItem(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {        
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                response = contractServiceObj.getContractReplacementDOItem(jobj);        
                response.put("success", true);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", jobj,ex.getMessage());
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", jobj,ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }    
    
    @GET
    @Path("/agreement")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getContractAgreements(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {        
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                response = contractServiceObj.getContractAgreementDetails(jobj);        
                response.put("success", true);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE,ex.getMessage());                
                response = wsUtilObj.getErrorResponse("e01", jobj,ex.getMessage());
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", jobj,ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }    

    /*@GET
    @Path("/costAgreement")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomerContractCostAgreements(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {        
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                jobj = wsUtilObj.populateAdditionalInformation(jobj);
                response = contractServiceObj.getCustomerContractsCostAgreementDetails(jobj);        
                response.put("success", true);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj,ex.getMessage());
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj,ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }    

    @GET
    @Path("/serviceAgreement")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomerContractServiceAgreements(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {        
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                jobj = wsUtilObj.populateAdditionalInformation(jobj);
                response = contractServiceObj.getContractAgreementDetails(jobj);        
                response.put("success", true);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj,ex.getMessage());
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj,ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }    
    */
    @GET
    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccountContracts(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {        
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj,null);
        } else {
            try {
                response = contractServiceObj.getAccountContractDetails(jobj);        
                response.put("success", true);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", jobj,ex.getMessage());
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj,ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE,ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", jobj,ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    } 
    /* Rest services call from CRM for Contract Attachment */
    @GET
    @Path("/attachemnt")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAttachDocuments(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = contractServiceObj.getAttachDocuments(jobj);
                response.put("success", true);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @POST
    @Path("/contractservice")
    public Response saveContractService(InputStream incomingData) {
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
                response = contractServiceObj.saveContractService(jobj);

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
}
