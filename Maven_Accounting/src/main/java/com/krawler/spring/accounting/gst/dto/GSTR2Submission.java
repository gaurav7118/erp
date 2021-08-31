/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.gst.dto;

import com.krawler.common.admin.Company;
import com.krawler.common.util.StringUtil;
import java.util.Date;

/**
 * Class is used to store GSTR2Submission Data which will be uploaded to GSTN
 * portal. It is used to store transaction wise offline JSON.
 *
 * @author swapnil.khandre
 */
public class GSTR2Submission {

    private String ID;
    private Date creationDate;//
    private String transactionJson;
    private String supplierInvoiceNo;
    private String gstRegNumber;
    private String invoiceid;
    /**
     * b2b-0 cdn-1.
     */
    private int type;
    /**
     * M-Modify, D-Delete, A-Accept, R-Reject, P-Pending. When receiver uploads
     * new invoice/modifies his own invoice, flag is not required
     */
    private String flag;
    private Company company;

    private int month;
    private int year;
    private boolean systemTransaction;
    private String entityid;
    private String jsonToBeUploaded;

    /**
     *
     * @return
     */
    public String getID() {
        return ID;
    }

    /**
     *
     * @param ID
     */
    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     *
     * @return
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     *
     * @param creationDate
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     *
     * @return
     */
    public String getTransactionJson() {
        return transactionJson;
    }

    /**
     *
     * @param transactionJson
     */
    public void setTransactionJson(String transactionJson) {
        this.transactionJson = transactionJson;
    }

    /**
     *
     * @return
     */
    public String getSupplierInvoiceNo() {
        return supplierInvoiceNo;
    }

    /**
     *
     * @param supplierInvoiceNo
     */
    public void setSupplierInvoiceNo(String supplierInvoiceNo) {
        this.supplierInvoiceNo = supplierInvoiceNo;
    }

    /**
     *
     * @return
     */
    public String getFlag() {
        return flag;
    }

    /**
     *
     * @param flag
     */
    public void setFlag(String flag) {
        this.flag = flag;
    }

    /**
     *
     * @return
     */
    public int getType() {
        return type;
    }

    /**
     *
     * @return
     */
    public String getGstRegNumber() {
        return gstRegNumber;
    }

    /**
     *
     * @param gstRegNumber
     */
    public void setGstRegNumber(String gstRegNumber) {
        this.gstRegNumber = gstRegNumber;
    }

    /**
     *
     * @return
     */
    public String getInvoiceid() {
        return invoiceid;
    }

    /**
     *
     * @param invoiceid
     */
    public void setInvoiceid(String invoiceid) {
        this.invoiceid = invoiceid;
    }

    /**
     *
     * @param type
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     *
     * @return
     */
    public Company getCompany() {
        return company;
    }

    /**
     *
     * @param company
     */
    public void setCompany(Company company) {
        this.company = company;
    }

    /**
     *
     * @return
     */
    public int getMonth() {
        return month;
    }

    /**
     *
     * @param month
     */
    public void setMonth(int month) {
        this.month = month;
    }

    /**
     *
     * @return
     */
    public int getYear() {
        return year;
    }

    /**
     *
     * @param year
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     *
     * @return
     */
    public boolean isSystemTransaction() {
        return systemTransaction;
    }

    /**
     *
     * @param systemTransaction
     */
    public void setSystemTransaction(boolean systemTransaction) {
        this.systemTransaction = systemTransaction;
    }

    /**
     *
     * @return
     */
    public String getEntityid() {
        return entityid;
    }

    /**
     *
     * @param entityid
     */
    public void setEntityid(String entityid) {
        this.entityid = entityid;
    }

    /**
     *
     * @param flag
     * @return
     */
    public static String getSubmissionStatus(String flag) {
        String status = "";
        if (StringUtil.isNullOrEmpty(flag)) {
            status = "";
        } else {
            switch (flag) {
                case "N":
                    status = "";
                    break;
                case "P":
                    status = "Pending";
                    break;
                case "A":
                    status = "Accept";
                    break;
                case "M":
                    status = "Modified";
                    break;
                case "R":
                    status = "Reject";
                    break;
                case "D":
                    status = "Delete";
                    break;
                case "G":
                    status = "Add To GSTN";
                    break;
            }
        }
        return status;
    }

    /**
     *
     */
    public static final int B2B = 0;

    /**
     *
     */
    public static final int CDNR = 1;

    /**
     *
     */
    public static final String MODIFIED = "M";

    /**
     *
     */
    public static final String ACCEPT = "A";

    /**
     *
     */
    public static final String ADD_TO_GSTN = "G";

    /**
     *
     */
    public static final String REJECT = "R";

    /**
     *
     */
    public static final String PENDING = "P";

    /**
     *
     * @return
     */
    public String getJsonToBeUploaded() {
        return jsonToBeUploaded;
    }

    /**
     *
     * @param jsonToBeUploaded
     */
    public void setJsonToBeUploaded(String jsonToBeUploaded) {
        this.jsonToBeUploaded = jsonToBeUploaded;
    }

}

