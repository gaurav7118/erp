/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.customreports;

import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class EWayRecord {

    public String userGstin;
    public String supplyType;
    public int subSupplyType;
    public String docType;
    public String docNo;
    public String docDate;
    public String fromGstin;
    public String fromTrdName;
    public String fromAddr1;
    public String fromAddr2;
    public String fromPlace;
    public int fromPincode;
    public int fromStateCode;
    public String toGstin;
    public String toTrdName;
    public String toAddr1;
    public String toAddr2;
    public String toPlace;
    public int toPincode;
    public int toStateCode;
    public int actualToStateCode;
    public int actualFromStateCode;
    public double totalValue;
    public double cgstValue;
    public double sgstValue;
    public double igstValue;
    public double cessValue;
    public int transMode;
    public int transDistance;
    public String transporterName;
    public String transporterId;
    public String transDocNo;
    public String transDocDate;
    public String vehicleNo;
    public String vehicleType;
    public double totInvValue;
    public String mainHsnCode;
    public List <EWayRecordDetails> itemList;

    public List<EWayRecordDetails> getItemList() {
        return itemList;
    }

    public void setItemList(List<EWayRecordDetails> itemList) {
        this.itemList = itemList;
    }
 
    public String getUserGstin() {
        return userGstin;
    }

    public void setUserGstin(String userGstin) {
        this.userGstin = userGstin;
    }

    public String getSupplyType() {
        return supplyType;
    }

    public void setSupplyType(String supplyType) {
        this.supplyType = supplyType;
    }

    public int getSubSupplyType() {
        return subSupplyType;
    }

    public void setSubSupplyType(int subSupplyType) {
        this.subSupplyType = subSupplyType;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    public String getDocDate() {
        return docDate;
    }

    public void setDocDate(String docDate) {
        this.docDate = docDate;
    }

    public String getFromGstin() {
        return fromGstin;
    }

    public void setFromGstin(String fromGstin) {
        this.fromGstin = fromGstin;
    }

    public String getFromTrdName() {
        return fromTrdName;
    }

    public void setFromTrdName(String fromTrdName) {
        this.fromTrdName = fromTrdName;
    }

    public String getFromAddr1() {
        return fromAddr1;
    }

    public void setFromAddr1(String fromAddr1) {
        this.fromAddr1 = fromAddr1;
    }

    public String getFromAddr2() {
        return fromAddr2;
    }

    public void setFromAddr2(String fromAddr2) {
        this.fromAddr2 = fromAddr2;
    }

    public String getFromPlace() {
        return fromPlace;
    }

    public void setFromPlace(String fromPlace) {
        this.fromPlace = fromPlace;
    }

    public int getFromPincode() {
        return fromPincode;
    }

    public void setFromPincode(int fromPincode) {
        this.fromPincode = fromPincode;
    }

    public int getFromStateCode() {
        return fromStateCode;
    }

    public void setFromStateCode(int fromStateCode) {
        this.fromStateCode = fromStateCode;
    }

    public String getToGstin() {
        return toGstin;
    }

    public void setToGstin(String toGstin) {
        this.toGstin = toGstin;
    }

    public String getToTrdName() {
        return toTrdName;
    }

    public void setToTrdName(String toTrdName) {
        this.toTrdName = toTrdName;
    }

    public String getToAddr1() {
        return toAddr1;
    }

    public void setToAddr1(String toAddr1) {
        this.toAddr1 = toAddr1;
    }

    public String getToAddr2() {
        return toAddr2;
    }

    public void setToAddr2(String toAddr2) {
        this.toAddr2 = toAddr2;
    }

    public String getToPlace() {
        return toPlace;
    }

    public void setToPlace(String toPlace) {
        this.toPlace = toPlace;
    }

    public int getToPincode() {
        return toPincode;
    }

    public void setToPincode(int toPincode) {
        this.toPincode = toPincode;
    }

    public int getToStateCode() {
        return toStateCode;
    }

    public void setToStateCode(int toStateCode) {
        this.toStateCode = toStateCode;
    }

    public int getActualToStateCode() {
        return actualToStateCode;
    }

    public void setActualToStateCode(int actualToStateCode) {
        this.actualToStateCode = actualToStateCode;
    }

    public int getActualFromStateCode() {
        return actualFromStateCode;
    }

    public void setActualFromStateCode(int actualFromStateCode) {
        this.actualFromStateCode = actualFromStateCode;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    public double getCgstValue() {
        return cgstValue;
    }

    public void setCgstValue(double cgstValue) {
        this.cgstValue = cgstValue;
    }

    public double getSgstValue() {
        return sgstValue;
    }

    public void setSgstValue(double sgstValue) {
        this.sgstValue = sgstValue;
    }

    public double getIgstValue() {
        return igstValue;
    }

    public void setIgstValue(double igstValue) {
        this.igstValue = igstValue;
    }

    public double getCessValue() {
        return cessValue;
    }

    public void setCessValue(double cessValue) {
        this.cessValue = cessValue;
    }

    public int getTransMode() {
        return transMode;
    }

    public void setTransMode(int transMode) {
        this.transMode = transMode;
    }

    public int getTransDistance() {
        return transDistance;
    }

    public void setTransDistance(int transDistance) {
        this.transDistance = transDistance;
    }

    public String getTransporterName() {
        return transporterName;
    }

    public void setTransporterName(String transporterName) {
        this.transporterName = transporterName;
    }

    public String getTransporterId() {
        return transporterId;
    }

    public void setTransporterId(String transporterId) {
        this.transporterId = transporterId;
    }

    public String getTransDocNo() {
        return transDocNo;
    }

    public void setTransDocNo(String transDocNo) {
        this.transDocNo = transDocNo;
    }

    public String getTransDocDate() {
        return transDocDate;
    }

    public void setTransDocDate(String transDocDate) {
        this.transDocDate = transDocDate;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public void setVehicleNo(String vehicleNo) {
        this.vehicleNo = vehicleNo;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public double getTotInvValue() {
        return totInvValue;
    }

    public void setTotInvValue(double totInvValue) {
        this.totInvValue = totInvValue;
    }


    public String getMainHsnCode() {
        return mainHsnCode;
    }

    public void setMainHsnCode(String mainHsnCode) {
        this.mainHsnCode = mainHsnCode;
    }
    
    public JSONArray  getItemListJSONString(){
        JSONArray itemListJSONArray = new JSONArray();
        for (int itr = 0; this.itemList.size() > itr; itr++) {
            try {
                itemListJSONArray.put(new JSONObject(this.itemList.get(itr).toString()));
            } catch (JSONException ex) {
                Logger.getLogger(EWayRecord.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return itemListJSONArray;
    }
    @Override
    public String toString() {
        return "{" + "\"userGstin\":\"" + userGstin + "\",\"supplyType\":\"" + supplyType + "\",\"subSupplyType\":" + subSupplyType + ",\"docType\":\"" + docType + "\",\"docNo\":\"" + docNo + "\",\"docDate\":\"" + docDate + "\",\"fromGstin\":\"" + fromGstin + "\",\"fromTrdName\":\"" + fromTrdName + "\",\"fromAddr1\":\"" + fromAddr1 + "\",\"fromAddr2\":\"" + fromAddr2
                + "\",\"fromPlace\":\"" + fromPlace + "\",\"fromPincode\":" + fromPincode + ",\"fromStateCode\":" + fromStateCode + ",\"toGstin\":\"" + toGstin + "\",\"toTrdName\":\"" + toTrdName + "\",\"toAddr1\":\"" + toAddr1 + "\",\"toAddr2\":\"" + toAddr2 + "\",\"toPlace\":\"" + toPlace + "\",\"toPincode\":" + toPincode + ",\"toStateCode\":" + toStateCode
                + ",\"actualToStateCode\":" + actualToStateCode + ",\"actualFromStateCode\":" + actualFromStateCode + ",\"totalValue\":" + Double.parseDouble(StringUtil.convertToTwoDecimal(totalValue)) + ",\"cgstValue\":" + Double.parseDouble(StringUtil.convertToTwoDecimal(cgstValue)) + ",\"sgstValue\":" + Double.parseDouble(StringUtil.convertToTwoDecimal(sgstValue)) + ",\"igstValue\":" + Double.parseDouble(StringUtil.convertToTwoDecimal(igstValue)) + ",\"cessValue\":" + Double.parseDouble(StringUtil.convertToTwoDecimal(cessValue)) + ",\"transMode\":" + transMode + ",\"transDistance\":" + transDistance + ",\"transporterName\":\"" + transporterName
                + "\",\"transporterId\":\"" + transporterId + "\",\"transDocNo\":\"" + transDocNo + "\",\"transDocDate\":\"" + transDocDate + "\",\"vehicleNo\":\"" + vehicleNo + "\",\"vehicleType\":\"" + vehicleType + "\",\"totInvValue\":" +  Double.parseDouble(StringUtil.convertToTwoDecimal(totInvValue)) + ",\"mainHsnCode\":" + mainHsnCode + ",\"itemList\":" /*+ itemList +"\""*/ + getItemListJSONString() + '}';
    }
    
    

}
