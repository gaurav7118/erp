/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.exportFuctionality;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.BufferedOutputStream;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperPrint;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author krawler
 */
public interface AccExportReportsServiceDAO {

//    public JasperPrint exportCustomerLedgerJasperReport(HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    public JasperPrint exportCustomerLedgerJasperReport(JSONObject request) throws ServiceException, JSONException, SessionExpiredException, ParseException;
//    public JasperPrint exportVendorLedgerJasperReport(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    public JasperPrint exportVendorLedgerJasperReport(JSONObject request) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    public List<Object> exportCreditNoteJasperReport(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    public List<Object> exportCreditNoteJasperReport(HttpServletRequest request, HttpServletResponse response, String billIds) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    public List<Object> exportCreditNoteJasperReportForMonzone(HttpServletRequest request, HttpServletResponse response, List entityList) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    public List<Object> exportDebitNoteJasperReportForMonzone(HttpServletRequest request, HttpServletResponse response, List entityList) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    public JasperPrint exportDefaultFormatCreditNoteJasperReport(HttpServletRequest request, HttpServletResponse response, String billid) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    public JasperPrint exportDefaultFormatDebitNoteJasperReport(HttpServletRequest request, HttpServletResponse response,  String billid) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    public List<Object> exportDebitNoteJasperReport(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    public List<Object> exportDebitNoteJasperReport(HttpServletRequest request, HttpServletResponse response, String billIds) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    public List<Object> exportPaymentReceipt(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportPaymentVoucher(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportContraPaymentVoucher(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportCustomerInvoiceReport(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportLSHCustomerInvoiceReport(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportDiamondAviationCustomerInvoice(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportSenwanCommercialInvoiceJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportFerrateGroupTaxInvoiceJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportForm21AInvoiceJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<JasperPrint> exportExciseFormERJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<JasperPrint> exportForm201CJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<JasperPrint> exportForm33Jasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportRuleNo11Jasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportRuleNo11JasperForInterStockTransfer(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportRuleNo11JasperForPO(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportCommercialInvoiceJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportMalaysianGstTaxInvoiceJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportMalaysianGstCreditDebitNote(HttpServletRequest request,String billid,String companyid,int mode) throws ServiceException, SessionExpiredException;
    public List<Object> exportMalaysianGstNormalCreditDebitNote(HttpServletRequest request,String billid,String companyid,int mode) throws ServiceException, SessionExpiredException;
    public List<Object> exportPurchaseOrderJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportSenwanGroupPurchaseOrderJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportFerrateGroupPurchaseOrderJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportF1RecreationPurchaseOrder(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportDeliveryOrderJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportProductCompositionJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportInvoicepackingList(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportPermitInvoiceList(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportSenwanGroupCustomerQuotation(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportVHQCustomerQuotation(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportPacificTecDeliveryOrderJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportVHQSalesOrderJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportFerrateGroupVendorInvoiceJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportSenwanTecPaymentVoucher(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportTIDPaymentVoucher(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportFerrateGroupPaymentVoucher(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportLSHPaymentVoucher(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportLSHPaymentReceipt(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportSatsTaxInvoiceJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportSATSCreditNoteJasperReport(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportSatsVendorTaxInvoiceJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportSATSDebitNoteJasperReport(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportHengguanCustomerQuotation(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportF1RecreationPurchaseReturn(HttpServletRequest request, HttpServletResponse response,String billid, String DNNumber) throws ServiceException, SessionExpiredException;
    public List<Object> exportDiamondAviationCustomerQuotation(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportCustomerQuotationForFasten(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportBITDeliveryOrderJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportFascinaWindowsDeliveryOrderJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportPurchaseRequisition(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportBMCustomerInvoiceReport(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportFOneCustomerInvoiceReport(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportF1RecreationCustomerQuotation(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportPurchaseReturn(HttpServletRequest request, HttpServletResponse response, String billid, String DNNumber) throws ServiceException, SessionExpiredException;
    public List<Object> exportSalesReturnJasper(HttpServletRequest request, HttpServletResponse response, String billid,String CNNumber ) throws ServiceException, SessionExpiredException;
    public List<Object> exportCNDNSRPRJasperForFasten(HttpServletRequest request, HttpServletResponse response, String billid,String CNNumber,int moduleId) throws ServiceException, SessionExpiredException;
    public List<Object> exportF1SalesReturnReport(HttpServletRequest request, HttpServletResponse response,String billids ,String CNNumber) throws ServiceException, SessionExpiredException;
    public List<Object> exportSalesContractreport(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public void setMessageSource(MessageSource msg);
    public List<Object> exportPackingList(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportGCBCreditNoteJasperReport(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportGCBPurchaseRequisition(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportGuanChongProformaInvoice(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportGCBDebittNoteJasperReport(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException; 
    public List<Object> exportDefaultPaymentVoucher(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportDefaultPaymentVoucher(JSONObject jsonObject) throws ServiceException, SessionExpiredException;
    public List<Object> exportDefaultRFQ(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportDefaultRFQ(JSONObject jsonObject) throws ServiceException, SessionExpiredException;
    public List<Object> exportDiamondAviationPuchaseOrder(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportMonzonePuchaseOrder(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportMonzoneTaxInvoice(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportKimCheyInvoice(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportKimCheyDeliveryOrder(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportMonzoneDeliveryOrder(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportMonzoneCustomerQuotation(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportCustomerInvoice(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportCustomerInvoiceForFasten(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportCustomerQuotationForTonyFibreGlass(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportDeliveryOrder(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportDeliveryOrderForFasten(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportVendorQuotation(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportARKCustomerInvoice(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException ;
    public List<Object> exportGoodsReceiptOrderJasperReport(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException,ParseException,JSONException ;
    public List<Object> exportVendorInvoiceRegisterReport(HttpServletRequest request, HttpServletResponse response, JSONArray tempArray) throws ServiceException, SessionExpiredException,ParseException,JSONException ;
    public List<Object> exportVendorInvoiceRegisterSummaryReport(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException,ParseException,JSONException ;
    public List<Object> exportF1RecreationCreditDebitNote(HttpServletRequest request,HttpServletResponse response,String recbillid) throws ServiceException, SessionExpiredException ;   
    public List<Object> exportSBISalesOrderJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportDiamondAviationDeliveryOrder(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportDiamondAviationDeliveryOrderPackages(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportDiamondAviationGoodsReceiptOrderJasperReport(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException,ParseException,JSONException ;
    public List<Object> exportDiamondAviationGoodsReceiptScrapReportJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException,ParseException,JSONException ;
    public List<Object> exportSBICustomerQoutationJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportCustomerQuotationJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportLANDPLUSCustomerInvoice(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportSalesInvoiceRegisterReport(HttpServletRequest request, HttpServletResponse response,JSONArray tempArray) throws ServiceException, SessionExpiredException;
    public List<Object> exportSalesInvoiceRegisterSummaryReport(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public List<Object> exportDiamondAviationPartsReceiptNote(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, JSONException, SessionExpiredException, ParseException;
    public List<Object> exportRightSpaceCustomerQuoteAndInvoice(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException ;
    public void getPurchaseOrdersForXls(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException ;
    public void exportXlsReport(HttpServletResponse response,String title,String fileName ,JSONArray DataJArr, String companyid) throws ServiceException, SessionExpiredException, JSONException;    
    public void  exportDeliveryOrderXls(HttpServletRequest request, HttpServletResponse response,JSONArray tempArray) ;  
    public List<Object> exportDefaultFormatCreditNoteJasperReportForTonyFibreGlass(HttpServletRequest request, HttpServletResponse response, String billid) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    public List<Object> exportDefaultFormatDebitNoteJasperReportForTonyFibreGlass(HttpServletRequest request, HttpServletResponse response, String billid) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    public List<Object> exportChallanNo281Report(HttpServletRequest request,HttpServletResponse response, Map<String, Object> requiredData)throws ServiceException, SessionExpiredException ;
    public List<Object> exportSwatowPurchaseOrder(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException ;
    public List exportCashReceipt(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException, JSONException;
    public List<Object> exportSupplementaryInvoiceJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
    public void exportSOAtoZIP_FILE(JSONObject request, HttpServletResponse response, boolean iscustmer) throws Exception;
    public void exportSOAtoZIP_FILE(JSONObject request, boolean iscustmer) throws Exception;
    public Map exportForm201AJson(Map requestParams) throws ServiceException;
    public List<Object> exportSalesOrderForHINSITSU(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException ;
    
    //Mobile PDF
    public JSONObject exportSalesReturn(JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    public JSONObject exportSalesReturnWithCN(JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    public JSONObject exportCreditSales(JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    public JSONObject exportCashSales(JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    public JSONObject exportSalesOrder(JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    
    /**
     * Description: This method is used to design the Hinsitsu Specific Jasper Report
     * @param HttpServletRequest request, HttpServletResponse response
     * @return KwlReturnObject
     * @throws ServiceException 
     */
    public List<Object> exportHinsitsuCustomerQoutationJasper(HttpServletRequest request, HttpServletResponse response) throws ServiceException, SessionExpiredException;
}
