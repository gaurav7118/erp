/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class StockLedgerSubReport {

    String id = "";
    String prodcode = "";
    String prodname = "";
    String date = "";
    String documentno = "";
    String code = "";
    String party = "";
    Double received = 0.0;
    Double delivered = 0.0;
    Double balance = 0.0;
    Double recvalue = 0.0;
    Double delvalue = 0.0;
    Double stockrate = 0.0;
    Double stockvalue = 0.0;
    Double valuation = 0.0;
    Double quantityOnHand = 0.0;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(Double quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public Double getValuation() {
        return valuation;
    }

    public void setValuation(Double valuation) {
        this.valuation = valuation;
    }


    public Double getStockvalue() {
        return stockvalue;
    }

    public void setStockvalue(Double stockvalue) {
        this.stockvalue = stockvalue;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Double getDelivered() {
        return delivered;
    }

    public void setDelivered(Double delivered) {
        this.delivered = delivered;
    }

    public String getDocumentno() {
        return documentno;
    }

    public void setDocumentno(String documentno) {
        this.documentno = documentno;
    }

    public String getParty() {
        return party;
    }

    public void setParty(String party) {
        this.party = party;
    }

    public String getProdcode() {
        return prodcode;
    }

    public void setProdcode(String prodcode) {
        this.prodcode = prodcode;
    }

    public String getProdname() {
        return prodname;
    }

    public void setProdname(String prodname) {
        this.prodname = prodname;
    }

    public Double getReceived() {
        return received;
    }

    public void setReceived(Double received) {
        this.received = received;
    }

    public Double getStockrate() {
        return stockrate;
    }

    public void setStockrate(Double stockrate) {
        this.stockrate = stockrate;
    }

    public Double getDelvalue() {
        return delvalue;
    }

    public void setDelvalue(Double delvalue) {
        this.delvalue = delvalue;
    }

    public Double getRecvalue() {
        return recvalue;
    }

    public void setRecvalue(Double recvalue) {
        this.recvalue = recvalue;
    }

  
}
