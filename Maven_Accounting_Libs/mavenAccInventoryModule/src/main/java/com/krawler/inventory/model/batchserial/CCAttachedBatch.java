/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.batchserial;

import com.krawler.inventory.model.cyclecount.CycleCount;

/**
 *
 * @author Vipin Gupta
 */
public class CCAttachedBatch extends  AttachedBatch{
    
    private CycleCount cycleCount;

    public CCAttachedBatch() {
    }

    public CCAttachedBatch(CycleCount cycleCount) {
        this.cycleCount = cycleCount;
    }

    public CycleCount getCycleCount() {
        return cycleCount;
    }

    public void setCycleCount(CycleCount cycleCount) {
        this.cycleCount = cycleCount;
    }
    
}
