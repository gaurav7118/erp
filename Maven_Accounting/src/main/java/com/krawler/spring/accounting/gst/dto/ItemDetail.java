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

package com.krawler.spring.accounting.gst.dto;
/**
 *
 * @author krawler
 */
public class ItemDetail{
    private int num;                //Item Number	Integer
    private double txval;           //Taxable value of Goods or Service as per invoice.            "type": "number", "multipleOf": 0.01, "maximum": 99999999999.99, "minimum": 0 Decimal(11, 2)
    private double adjustedAmount;           //Adjusted Amount for particular transaction
    private double rt;               //Rate. description : "rate" type : "number" multipleOf : 0.01  Decimal(3, 2)
    private double iamt;            //IGST Amount as per invoice       "type": "number", "multipleOf": 0.01, "maximum": 99999999999.99, "minimum": 0 Decimal(11, 2)
    private double camt;            //IGST Amount as per invoice       "type": "number", "multipleOf": 0.01, "maximum": 99999999999.99, "minimum": 0 Decimal(11, 2)
    private double samt;            //IGST Amount as per invoice       "type": "number", "multipleOf": 0.01, "maximum": 99999999999.99, "minimum": 0 Decimal(11, 2)
    private double csamt;           //IGST Amount as per invoice       "type": "number", "multipleOf": 0.01, "maximum": 99999999999.99, "minimum": 0 Decimal(11, 2)
    private double ad_amt;          //Advance received	Decimal(11, 2)

    //HSN details
    private String hsn_sc;         //HSN of Goods or Services as per Invoice line items,	Alphanumeric (Max length:10)
    private String desc = "";           //Description of goods sold,	string(Max length:30)
    private String uqc = "";            //UQC (Unit of Measure) of goods sold	string(Max length:30)
    private double qty;            //Quantity of goods sold	Decimal(15, 2)
    private double val;            //Total Value	Decimal(15, 2)
    private String adt;   // advance date
    private String anum = "";  // receipt number
    private String gstin = "";  // GSTIN of customer
    private String aid = "";  // receipt UUID
    private String invnum = "";  // Invoice number

    public String getAdt() {
        return adt;
    }

    public void setAdt(String adt) {
        this.adt = adt;
    }

    public String getAnum() {
        return anum;
    }

    public void setAnum(String anum) {
        this.anum = anum;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public double getTxval() {
        return txval;
    }

    public void setTxval(double txval) {
        this.txval = txval;
    }

    public double getRt() {
        return rt;
    }

    public void setRt(double rt) {
        this.rt = rt;
    }

    public double getIamt() {
        return iamt;
    }

    public void setIamt(double iamt) {
        this.iamt = iamt;
    }

    public double getCamt() {
        return camt;
    }

    public void setCamt(double camt) {
        this.camt = camt;
    }

    public double getSamt() {
        return samt;
    }

    public void setSamt(double samt) {
        this.samt = samt;
    }

    public double getCsamt() {
        return csamt;
    }

    public void setCsamt(double csamt) {
        this.csamt = csamt;
    }

    public double getAd_amt() {
        return ad_amt;
    }

    public void setAd_amt(double ad_amt) {
        this.ad_amt = ad_amt;
    }

    public String getHsn_sc() {
        return hsn_sc;
    }

    public void setHsn_sc(String hsn_sc) {
        this.hsn_sc = hsn_sc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUqc() {
        return uqc;
    }

    public void setUqc(String uqc) {
        this.uqc = uqc;
    }

    public double getQty() {
        return qty;
    }

    public void setQty(double qty) {
        this.qty = qty;
    }

    public double getVal() {
        return val;
    }

    public void setVal(double val) {
        this.val = val;
    }

    public double getAdjustedAmount() {
        return adjustedAmount;
    }

    public void setAdjustedAmount(double adjustedAmount) {
        this.adjustedAmount = adjustedAmount;
    }

    public String getInvnum() {
        return invnum;
    }

    public void setInvnum(String invnum) {
        this.invnum = invnum;
    }
}
