/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.entitygst;

/**
 *
 * @author krawler
 */
public interface GSTR3BConstants {

    String DATAINDEX_DOCTYPE = "doctype";
    String DATAINDEX_TRANSACTION_NUMBER = "transactionno";
    String DATAINDEX_GSTIN = "gstin";
    String DATAINDEX_CESS = "cess";
    String DATAINDEX_DATE = "date";
    String DATAINDEX_PERSONNAME = "personname";
    String DATAINDEX_TAXABLE_AMOUNT = "taxableAmt";
    String DATAINDEX_TOTAL_AMOUNT = "totalAmt";
    String DATAINDEX_TOTAL_TAX = "totalTax";
    String DATAINDEX_SGSTAMOUNT = "sgstamt";
    String DATAINDEX_CGSTAMOUNT = "cgstamt";
    String DATAINDEX_IGSTAMOUNT = "igstamt";
    String DATAINDEX_POS = "pos";
    String DATAINDEX_TAX_CLASS_TYPE = "taxclasstype";
    String DETAILED_VIEW_REPORT ="detailView";
    
    String HEADER_DOCTYPE = "Document Type";
    String HEADER_DATE = "DATE";
    String HEADER_PERSONNAME = "Person Name";
    String HEADER_POS = "Place of Supply";
    String HEADER_TRANSACTION_NUMBER = "Transaction Number";
    String HEADER_TAXABLE_AMOUNT = "Taxable Amount";
    String HEADER_IGST = "Integrated Tax Amount";
    String HEADER_CGST = "Central Tax Amount";
    String HEADER_SGST = "State Tax Amount";
    String HEADER_CESS = "Cess Amount";
    String HEADER_TOTAL_TAX = "Total Tax Amount";
    String HEADER_TOTAL_AMOUNT = "Total Amount Incl Tax";
}
