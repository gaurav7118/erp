/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.util;

/**
 *
 * @author Vipin Gupta
 */
public class Paging {
    
    private int offset;
    private int limit;
    private int totalRecord;

    public Paging(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }

    public Paging(String offset, String limit) {
        try{
            this.offset = Integer.parseInt(offset);
            this.limit = Integer.parseInt(limit);
        }catch(Exception ex){}
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getTotalRecord() {
        return totalRecord;
    }

    public void setTotalRecord(int totalRecord) {
        this.totalRecord = totalRecord;
    }

    public boolean isValid(){
        boolean valid = false;
        if(limit > 0 && offset >= 0){
            valid = true;
        }
        return valid;
    }
    
}
