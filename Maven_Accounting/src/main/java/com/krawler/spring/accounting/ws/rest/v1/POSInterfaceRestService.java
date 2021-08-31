/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.ws.rest.v1;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.accounting.ws.service.POSInterfaceService;
import com.krawler.spring.accounting.ws.service.WSUtilService;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.*;
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
@Path("/v1/pos")
public class POSInterfaceRestService {

    @Autowired
    POSInterfaceService posInterfaceService;
    @Autowired
    WSUtilService wsUtilObj;
    
    public POSInterfaceRestService() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }
    
      
    @POST
    @Path("/currency-denominations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveCompanywiseCurrencyDenomination(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = posInterfaceService.saveCompanywiseCurrencyDenomination(jobj);
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
    @Path("/currency-denominations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCompanywiseCurrencyDenomination(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = posInterfaceService.getCompanywiseCurrencyDenomination(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @POST
    @Path("/currency-denominations-delete")
    @Produces(MediaType.APPLICATION_JSON)
      public Response deleteCurrencyDenominations(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = posInterfaceService.deleteCurrencyDenominations(jobj);
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
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRegisterDetails(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = posInterfaceService.getRegisterDetails(jobj);
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
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveOpenandCloseRegisterDetails(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = posInterfaceService.saveOpenandCloseRegisterDetaisls(jobj);
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
    @Path("/invoicedo-receivepayment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveInvoiceDOandRP(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = posInterfaceService.saveInvoiceDOandRP(jobj);
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
    
 
    @DELETE
    @Path("/invoicedo-receivepayment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteInvoiceDOandRP(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = posInterfaceService.deleteInvoiceDOandRP(jobj);
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
    @Path("/cashout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveCashOutDetails(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = posInterfaceService.saveCashOutDetails(jobj);
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
    @Path("/cashout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCashoutReports(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = posInterfaceService.getCashoutReports(jobj);
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
    @Path("/poserpmappingdetails")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getERPPOSMappingDetails(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = posInterfaceService.getERPPOSMappingDetails(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/closebalance")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClosedBalance(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = posInterfaceService.getClosedBalanceDetails(jobj);
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
    @Path("/salesreturn")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveSalesReturnwithCN(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = posInterfaceService.saveSalesReturnwithCN(jobj);
            } catch (JSONException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            } catch (ServiceException ex) {
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            } catch (AccountingException ex) {
                response = wsUtilObj.getErrorResponse("", jobj, ex.getMessage());
            } catch (Exception ex) {
                response = wsUtilObj.getErrorResponse("erp25", jobj, null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @POST
    @Path("/salesorder")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveSalesOrderLinkedAdvanceReceipts(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = posInterfaceService.saveSalesOrderLinkedAdvanceReceipts(jobj);
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
    @Path("/receive-payment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveReceiptPayment(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = posInterfaceService.saveReceivePaymentAgainstInvoice(jobj);
            } catch (JSONException | ServiceException | SessionExpiredException | ParseException ex) {
                response = wsUtilObj.getErrorResponse("e01", jobj, null);
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @POST
    @Path("/advance-receive-payment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveAdvanceReceiptPayment(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = posInterfaceService.saveAdvanceReceiptPayment(jobj);
            } catch (JSONException | ServiceException | SessionExpiredException | ParseException |UnsupportedEncodingException ex) {
                response = wsUtilObj.getErrorResponse("", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @POST
    @Path("/fund-transfer")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveCashOutTransactionDepositType(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = posInterfaceService.saveCashOutTransactionDepositType(jobj);
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
    
}
