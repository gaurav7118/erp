/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.acc.dm;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.MultiValueAttribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import java.util.Date;
import java.util.List;


public class ExchangeRateDetailInfo implements java.io.Serializable {
    private String ID;
    private Date applyDate;
    private double exchangeRate;
    private String exchangeratelink;
//    public final List<String> filterVals;
//    
//    public ExchangeRateDetailInfo(String ID, Date applyDate, double exchangeRate,String exchangeratelink, List<String> filterVals) {
//        this.carId = carId;
//        this.name = name;
//        this.description = description;
//        this.features = features;
//    }
    
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Date getApplyDate() {
        return applyDate;
    }

    public void setApplyDate(Date applyDate) {
        this.applyDate = applyDate;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getExchangeratelink() {
        return exchangeratelink;
    }

    public void setExchangeratelink(String exchangeratelink) {
        this.exchangeratelink = exchangeratelink;
    }
    
    // -------------------------- Attributes --------------------------
    public static final Attribute<ExchangeRateDetailInfo, String> PK_ID = new SimpleAttribute<ExchangeRateDetailInfo, String>("carId") {
        @Override
        public String getValue(ExchangeRateDetailInfo obj) { 
            return obj.getID(); 
        }
    };
    public static final Attribute<ExchangeRateDetailInfo, String> EXCHANGERATELINK = new SimpleAttribute<ExchangeRateDetailInfo, String>("linkid") {
        @Override
        public String getValue(ExchangeRateDetailInfo obj) { 
            return obj.getID(); 
        }
    };
//    public static final Attribute<ExchangeRateDetailInfo, String> FILTERS = new MultiValueAttribute<ExchangeRateDetailInfo, String>("filterVals") {
//        public List<String> getValues(ExchangeRateDetailInfo obj) { return obj.filterVals; }
//    };
}
