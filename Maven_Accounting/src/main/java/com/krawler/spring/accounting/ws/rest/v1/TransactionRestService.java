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
import com.krawler.spring.accounting.ws.service.*;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
//Jasper imports for Testing Purpose
//import net.sf.jasperreports.engine.JasperCompileManager;
//import net.sf.jasperreports.engine.design.JasperDesign;
//import net.sf.jasperreports.engine.export.JRPdfExporter;
//import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
//import net.sf.jasperreports.engine.xml.JRXmlLoader;
//import java.awt.image.BufferedImage;
//import com.krawler.common.util.URLUtil;
//import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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

@Component
@Path("/v1/transaction")
public class TransactionRestService {

    public TransactionRestService() {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
    }
    private static final Logger _logger = Logger.getLogger(TransactionRestService.class.getName());
    @Autowired
    TransactionService transactionServiceObj;
    
    @Autowired
    WSUtilService wsUtilObj;

    @POST
    @Path("/lead-invoice-receipt")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveTransactions(InputStream incomingData) {
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

                response = transactionServiceObj.saveTransactions(jobj);
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
    @Path("/invoice")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveInvoice(InputStream incomingData){
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

                response = transactionServiceObj.saveInvoice(jobj);
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
    @Path("/deliveryorder")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveDeliveryOrder(InputStream incomingData) {
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

                response = transactionServiceObj.saveDeliveryOrder(jobj);
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

    @GET
    @Path("/account")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccount(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.getAccountList(jobj);
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
    
    @GET
    @Path("/accounts-id-name")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAccountsIdName(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.getAccountsIdNameList(jobj);
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

    @GET
    @Path("/journal-entry")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getJournalEntry(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {

                response = transactionServiceObj.getJournalEntry(jobj);
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
    @Path("/journal-entry")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveJournalEntry(InputStream incomingData) {
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

                response = transactionServiceObj.saveJournalEntry(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, ex.getMessage());
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
    @Path("/receipt")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveReceipt(InputStream incomingData) {
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

                response = transactionServiceObj.saveReceiptPayment(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, ex.getMessage());
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
    @Path("/receive-payment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveReceiptPayment(InputStream incomingData) {
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

                response = transactionServiceObj.saveReceiptPayment(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", inputObj, ex.getMessage());
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

    @GET
    @Path("/invoice")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInvoice(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.getInvoice(jobj);
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/productprice")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIndividualProductPrice(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.getIndividualProductPrice(jobj);
                response.put(Constants.RES_success, response.optBoolean(Constants.RES_success, false));
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            }catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }

    @GET
    @Path("/payment-method")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPaymentMethod(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {

                response = transactionServiceObj.getPaymentMethod(jobj);
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

    @DELETE
    @Path("/invoice")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteInvoice(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {

                response = transactionServiceObj.deleteInvoice(jobj);
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

    @GET
    @Path("/salesorder")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSalesOrder(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {

                response = transactionServiceObj.getSalesOrder(jobj);
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
    @Path("/creditnote")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveCreditNote(InputStream incomingData) throws JSONException, ServiceException, SessionExpiredException, UnsupportedEncodingException {
        JSONObject response = new JSONObject();
        String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
        JSONObject jobj = new JSONObject(inputData);
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.saveCreditNote(jobj);
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
    
    @GET
    @Path("/creditnote")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCreditNote(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {

                response = transactionServiceObj.getCreditNote(jobj);
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
    
    @DELETE
    @Path("/salesreturn")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSalesReturn(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();

        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.deleteSalesReturn(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), jobj, ex.getMessage());
            }catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/salesreturn")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSalesReturn(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.getSalesReturn(jobj);
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

    
    @DELETE
    @Path("/salesorder")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteSalesOrder(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.deleteSalesOrder(jobj);
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
    @Path("/salesreturn")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveSalesReturn(InputStream incomingData) throws JSONException, ServiceException, SessionExpiredException, UnsupportedEncodingException {
        JSONObject response = new JSONObject();
        String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
        JSONObject jobj = new JSONObject(inputData);
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.saveSalesReturn(jobj);
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
    @Path("/salesorder")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveSalesOrder(InputStream incomingData) throws JSONException, ServiceException, SessionExpiredException, UnsupportedEncodingException {
        JSONObject response = new JSONObject();
        String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
        JSONObject jobj = new JSONObject(inputData);
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.saveSalesOrder(jobj);
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
    
    @GET
    @Path("/quotation")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQuotation(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {

                response = transactionServiceObj.getQuotations(jobj);
                response.put(Constants.RES_success, true);
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

    @DELETE
    @Path("/quotation")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteQuotation(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {

                response = transactionServiceObj.deleteQuotation(jobj);
                response.put(Constants.RES_success, true);
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
    
    @GET
    @Path("/linkedquotations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCQLinkedInTransaction(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {

                response = transactionServiceObj.getCQLinkedInTransaction(jobj);
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
    
    @GET
    @Path("/vendor-quotation")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVendorQuotation(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {

                response = transactionServiceObj.getVendorQuotations(jobj);
                response.put(Constants.RES_success, true);
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

    @GET
    @Path("/crmquotation/invoice")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInvoiceDetailFromCRMQuotation(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {

                response = transactionServiceObj.getInvoiceDetailfromCRMQuotation(jobj);
                response.put(Constants.RES_success, true);
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
    @Path("/salaryje")
    @Produces(MediaType.APPLICATION_JSON)
    public Response postSalaryJE(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = transactionServiceObj.postSalaryJE(jobj);
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
    @Path("/reverse-salaryje")
    @Produces(MediaType.APPLICATION_JSON)
    public Response postReverseSalaryJE(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = transactionServiceObj.postReverseSalaryJE(jobj);
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
    @Path("/cash-revenue")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCashRevenueTask(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.getCashRevenueTask(jobj);
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
    
    @GET
    @Path("/cash-and-purchase-revenue")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCashAndPurchaseRevenue(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                if(jobj.has("data")){
                    jobj = new JSONObject(jobj.optString("data"));
                }
                response = transactionServiceObj.getCashAndPurchaseRevenue(jobj);
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
    
    @GET
    @Path("/vendor-invoice")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVendorInvoicesReport(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                if(jobj.has(Constants.RES_data)){
                    jobj = new JSONObject(jobj.optString("data"));
                }
                response = transactionServiceObj.getVendorInvoicesReport(jobj).getJSONObject(Constants.RES_data);
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
    @GET
    @Path("/customer-invoice")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomerInvoicesReport(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                if(jobj.has("data")){
                    jobj = new JSONObject(jobj.optString("data"));
                }
                response = transactionServiceObj.getCustomerInvoicesReport(jobj);
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
    @Path("/amountje")
    @Produces(MediaType.APPLICATION_JSON)
    public Response postAmountJE(InputStream incomingData) {
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = transactionServiceObj.postAmountJE(jobj);
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
    @Path("/incidentcase")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveIncidentCase(InputStream incomingData){
        JSONObject response = new JSONObject();
        if (incomingData == null) {
            response = wsUtilObj.getErrorResponse("erp24", null, null);
        } else {
            JSONObject jobj = new JSONObject();
            try {
                String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
                jobj = new JSONObject(inputData);
                response = transactionServiceObj.saveIncidentCase(jobj);
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
    @Path("/incidentcase")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteIncidentCase(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {

                response = transactionServiceObj.deleteIncidentCase(jobj);
                response.put(Constants.RES_success, true);
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
    
    @GET
    @Path("/incidentcase")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIncidentCase(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {

                response = transactionServiceObj.getIncidentCase(jobj);
                response.put(Constants.RES_success, true);
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
    
    @GET
    @Path("/incidentchart")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getIncidentChart(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {

                response = transactionServiceObj.getIncidentChart(jobj);
                response.put(Constants.RES_success, true);
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
    @Path("/payment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response savePayment(InputStream incomingData) throws JSONException, ServiceException, SessionExpiredException, UnsupportedEncodingException {
        JSONObject response = new JSONObject();
        String inputData = StringUtil.getJsonStringFromInputstream(incomingData);
        JSONObject jobj = new JSONObject(inputData);
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.savePayment(jobj);
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
    
    @GET
    @Path("/salesreturnsummaryreport")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSalesReturnSummaryReport(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {

                response = transactionServiceObj.getSalesReturnSummaryReport(jobj);
                response.put(Constants.RES_success, true);
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
    
    @GET
    @Path("/salesbycustomer")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSalesByCustomer(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {

                response = transactionServiceObj.getSalesByCustomer(jobj);
                response.put(Constants.RES_success, true);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            }  catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/receipt")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReceipts(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.getReceipts(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
 
    @GET
    @Path("/payments")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPayments(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.getPayments(jobj);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }
    
    @GET
    @Path("/template")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDesignTemplateList(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {

                response = transactionServiceObj.getDesignTemplateList(jobj);
                response.put(Constants.RES_success, true);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }   
    
    @GET
    @Path("/deliveryorder")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeliveryOrder(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.getDeliveryOrderMerged(jobj);
                response.put(Constants.RES_success, true);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }   
      
    @GET
    @Path("/printtemplate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response printDocumentDesignerTemplate(@QueryParam(Constants.RES_REQUEST) JSONObject jobj,@Context HttpServletResponse servletresponse, @HeaderParam("User-Agent") String userAgent) {
        JSONObject response = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                //get browser version and put in json object
                if(!StringUtil.isNullOrEmpty(userAgent) && userAgent.lastIndexOf("Firefox/") > -1){
                    String version = "";
                    version = userAgent.substring(userAgent.lastIndexOf("Firefox/") + "Firefox/".length());
                    version = version.substring(0, version.indexOf("."));
                    jobj.put("browserVersion", version);
                }
                response = transactionServiceObj.printDocumentDesignerTemplate(jobj,servletresponse);
                response.put(Constants.RES_success, true);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", jobj, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", jobj, ex.getMessage());
            }
        }
        return Response.status(200).entity(response.toString()).build();
    }   
    
    @GET
    @Path("/exportmobilepdfimages")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response exportMobilePDFImages(@QueryParam(Constants.RES_REQUEST) JSONObject jobj, @Context HttpServletRequest request, @Context HttpServletResponse servletresponse) {
        JSONObject response = new JSONObject();
        ByteArrayOutputStream baos = null;
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                System.out.println("TransactionRestService.exportMobilePDFImages() call started....");
                baos = transactionServiceObj.exportMobilePDFImages(jobj);   
                response.put(Constants.RES_success, true);
                System.out.println("TransactionRestService.exportMobilePDFImages() call ended....");
                //Test Code
//                BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
//                ImageIO.write(bufferedImage, "png", new File("/home/krawler/store/Accounting/image.png"));                          
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
        return Response.ok(baos.toByteArray()).build();
    }
    
    /**
     * Save Goods Receipt
     * @param incomingData
     * @return 
     */
    @POST
    @Path("/goodsreceipt")
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveGoodsReceipt(InputStream incomingData) {
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

                response = transactionServiceObj.saveGoodsReceipt(jobj);
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
    
    @GET
    @Path("/purchaseorder")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPurchaseOrder(@QueryParam(Constants.RES_REQUEST) JSONObject requestParams) {
        JSONObject response = new JSONObject();
        if (requestParams == null) {
            response = wsUtilObj.getErrorResponse("erp24", requestParams, null);
        } else {
            try {
                response = transactionServiceObj.getPurchaseOrder(requestParams);
                response.put(Constants.RES_success, true);
            } catch (JSONException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("e01", requestParams, ex.getMessage());
            } catch (ServiceException ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse(ex.getCode(), requestParams, ex.getMessage());
            } catch (Exception ex) {
                _logger.log(Level.SEVERE, ex.getMessage());
                response = wsUtilObj.getErrorResponse("erp25", requestParams, ex.getMessage());
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

                response = transactionServiceObj.deleteDeliveryOrdersJSON(jobj);
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
    
    
    @DELETE
    @Path("/creditnote")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteCreditNote(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        JSONObject inputObj = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.deleteCreditNoteJSON(jobj);
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
    @DELETE
    @Path("/makepayment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteMakePayment(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        JSONObject inputObj = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.deleteMakePaymentJSON(jobj);
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
    @DELETE
    @Path("/receivepayment")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteReceivePayment(@QueryParam(Constants.RES_REQUEST) JSONObject jobj) {
        JSONObject response = new JSONObject();
        JSONObject inputObj = new JSONObject();
        if (jobj == null) {
            response = wsUtilObj.getErrorResponse("erp24", jobj, null);
        } else {
            try {
                response = transactionServiceObj.deleteReceivePayment(jobj);
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
