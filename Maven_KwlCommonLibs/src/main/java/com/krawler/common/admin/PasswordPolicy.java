/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class PasswordPolicy {
    
    private String policyid;
    private Company companyid;
    private int minchar;
    private int maxchar;
    private int minnum;
    private int minalphabet;
    private int specialchar;
    private String defpass;
    private String ppass;
    private int setpolicy;

    /**
     * @return the policyid
     */
    public String getPolicyid() {
        return policyid;
    }

    /**
     * @param policyid the policyid to set
     */
    public void setPolicyid(String policyid) {
        this.policyid = policyid;
    }

    /**
     * @return the minchar
     */
    public int getMinchar() {
        return minchar;
    }

    /**
     * @param minchar the minchar to set
     */
    public void setMinchar(int minchar) {
        this.minchar = minchar;
    }

    /**
     * @return the maxchar
     */
    public int getMaxchar() {
        return maxchar;
    }

    /**
     * @param maxchar the maxchar to set
     */
    public void setMaxchar(int maxchar) {
        this.maxchar = maxchar;
    }

    /**
     * @return the minnum
     */
    public int getMinnum() {
        return minnum;
    }

    /**
     * @param minnum the minnum to set
     */
    public void setMinnum(int minnum) {
        this.minnum = minnum;
    }

    /**
     * @return the minalphabet
     */
    public int getMinalphabet() {
        return minalphabet;
    }

    /**
     * @param minalphabet the minalphabet to set
     */
    public void setMinalphabet(int minalphabet) {
        this.minalphabet = minalphabet;
    }

    /**
     * @return the specialchar
     */
    public int getSpecialchar() {
        return specialchar;
    }

    /**
     * @param specialchar the specialchar to set
     */
    public void setSpecialchar(int specialchar) {
        this.specialchar = specialchar;
    }

    /**
     * @return the defpass
     */
    public String getDefpass() {
        return defpass;
    }

    /**
     * @param defpass the defpass to set
     */
    public void setDefpass(String defpass) {
        this.defpass = defpass;
    }

    /**
     * @return the ppass
     */
    public String getPpass() {
        return ppass;
    }

    /**
     * @param ppass the ppass to set
     */
    public void setPpass(String ppass) {
        this.ppass = ppass;
    }

    /**
     * @return the setpolicy
     */
    public int getSetpolicy() {
        return setpolicy;
    }

    /**
     * @param setpolicy the setpolicy to set
     */
    public void setSetpolicy(int setpolicy) {
        this.setpolicy = setpolicy;
    }

    /**
     * @return the companyid
     */
    public Company getCompanyid() {
        return companyid;
    }

    /**
     * @param companyid the companyid to set
     */
    public void setCompanyid(Company companyid) {
        this.companyid = companyid;
    }
    
}
