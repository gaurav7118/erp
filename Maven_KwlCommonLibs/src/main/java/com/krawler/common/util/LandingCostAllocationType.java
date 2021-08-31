/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public enum LandingCostAllocationType {

    QUANTITY(0), VALUE(1), WEIGHT(2),MANUAL(3),CUSTOMDUTY(4);
    private int LCAllocationId;
    private static final Map<Integer, LandingCostAllocationType> LCAllocationTypeMap = new HashMap<Integer, LandingCostAllocationType>();

    static {
        for (LandingCostAllocationType data : values()) {
            LCAllocationTypeMap.put(data.getLCAllocationId() , data);
        }
    }   
    LandingCostAllocationType(int LCAllocationId){
        this.LCAllocationId=LCAllocationId;
    }

    public int getLCAllocationId() {
        return LCAllocationId;
    }

    public void setLCAllocationId(int LCAllocationId) {
        this.LCAllocationId = LCAllocationId;
    }

    public static LandingCostAllocationType getByValue(int value) {
        return LCAllocationTypeMap.get(value);
    }
    
    public static double getTotalLanddedCost(int value, Map<String, Double> dataMap) {
                double totLandedCost=dataMap.get("totLandedCost");
                double noEligiableItem=dataMap.get("noEligiableItem");
                double lineItemQty=dataMap.get("lineItemQty");
                double valueOfItem=dataMap.get("valueOfItem");
                double eligiableItemCost=dataMap.get("eligiableItemCost"); 
                double eligiableItemWgt=dataMap.get("eligiableItemWgt"); 
                double itemWght=dataMap.get("itemWght"); 
                double manualProductAmount=dataMap.get("manualProductAmount"); 
                double resultAmt=0.0D;   
        switch (getByValue(value)) {
            case QUANTITY:
                if (noEligiableItem != 0) {
                    resultAmt = (totLandedCost / noEligiableItem) * lineItemQty;
                }
                break;
            case VALUE:
                if (eligiableItemCost != 0) {
                    resultAmt = (valueOfItem / eligiableItemCost) * totLandedCost;
                }
                break;
            case WEIGHT:
                if (eligiableItemWgt != 0) {
                    resultAmt = (itemWght / eligiableItemWgt) * totLandedCost;
                }
                break;
            case CUSTOMDUTY:
            case MANUAL:
                if (manualProductAmount != 0) {
                    resultAmt = manualProductAmount;
                }
                break;
        }
        return resultAmt;
    }
}
