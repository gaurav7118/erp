/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.spring.accounting.ws.rest.v1;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.ws.service.GroupCompanyService;
import com.krawler.spring.accounting.ws.service.WSUtilService;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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
@Path("/v1/groupcompany")

public class GroupCompanyRestService {
    
    public GroupCompanyRestService() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }
    private static final Logger _logger = Logger.getLogger(GroupCompanyRestService.class.getName());
    @Autowired
    GroupCompanyService groupCompanyService;
    @Autowired
    WSUtilService wsUtilObj;
      
    @POST
    @Path("/convertpotoso")
    @Produces(MediaType.APPLICATION_JSON)
    public Response convertPOtoSO(InputStream incomingData) {
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

                response = groupCompanyService.convertPOtoSO(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, null);
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), inputObj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", inputObj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @POST
    @Path("/convertvendorpaymenttoreceivepayment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response convertMakePaymenttoReceivePayment(InputStream incomingData) {
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

                response = groupCompanyService.convertMakePaymenttoReceivePayment(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, null);
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), inputObj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", inputObj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
     @POST
    @Path("/convertpurchasereturntosalesreturn")
    @Produces(MediaType.APPLICATION_JSON)
    public Response convertPurchaseReturnToSalesReturn(InputStream incomingData) {
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

                response = groupCompanyService.convertPurchaseReturnToSalesReturn(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, null);
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), inputObj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", inputObj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    
    @POST
    @Path("/convertpitosi")
    @Produces(MediaType.APPLICATION_JSON)
    public Response convertPIwithGRNtoSIwithDO(InputStream incomingData) {
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

                response = groupCompanyService.convertPIwithGRNtoSIwithDO(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, null);
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), inputObj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", inputObj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    } 

    @POST
    @Path("/deletereceivepayment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteReceivePayment(InputStream incomingData) {
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

                response = groupCompanyService.deleteReceivePayment(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, null);
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), inputObj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", inputObj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    
    @POST
    @Path("/deletepurchaseorder")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePurchaseOrder(InputStream incomingData) {
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

                response = groupCompanyService.deletePurchaseOrderPermanent(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, null);
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), inputObj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", inputObj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    } 
    
    @POST
    @Path("/deletesalesorder")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSalesOrder(InputStream incomingData) {
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

                response = groupCompanyService.deleteSalesOrders(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, null);
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), inputObj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", inputObj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @POST
    @Path("/deletesalesreturn")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSalesReturn(InputStream incomingData) {
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

                response = groupCompanyService.deleteSalesReturn(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, null);
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), inputObj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", inputObj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
      @POST
    @Path("/deletepurchasereturn")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePurchaseReturnPermanent(InputStream incomingData) {
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

                response = groupCompanyService.deletePurchaseReturnPermanent(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, null);
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), inputObj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", inputObj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @POST
    @Path("/deletemakepayment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteMakePaymentPermanent(InputStream incomingData) {
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

                response = groupCompanyService.deleteMakePaymentPermanent(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, null);
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), inputObj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", inputObj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @POST
    @Path("/deleteinvoiceanddo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteInvoiceandDO(InputStream incomingData) {
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

                response = groupCompanyService.deleteInvoiceandDO(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, null);
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), inputObj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", inputObj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    } 

    @POST
    @Path("/deletevendorinvoiceandgrn")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteVendorInvoiceandGRN(InputStream incomingData) {
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

                response = groupCompanyService.deleteVendorInvoiceandGRN(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, null);
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), inputObj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", inputObj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }  
    
    @POST
    @Path("/convertdebitnotetocreditnote")
    @Produces(MediaType.APPLICATION_JSON)
    public Response convertDebitNotetoCreditNote(InputStream incomingData) {
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

                response = groupCompanyService.convertDebitNotetoCreditNote(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, null);
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), inputObj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", inputObj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
       
    @POST
    @Path("/convertgrntodo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response convertGRNtoDO(InputStream incomingData) {
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
                resultObj.put(Constants.data, inputData);
                JSONObject jobj = new JSONObject(inputData);

                response = groupCompanyService.convertGRNtoDO(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, null);
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), inputObj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", inputObj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
  
    @POST
    @Path("/deletedo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDeliveryOrder(InputStream incomingData) {
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

                response = groupCompanyService.deleteDeliveryOrder(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, null);
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), inputObj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", inputObj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
}
