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
package com.krawler.hql.accounting;

import com.krawler.common.admin.*;
import java.util.Date;
import java.util.Set;

public class Account implements Comparable {

    private String ID;
    private Account parent;
//    private Account depreciationAccont;
    private String name;    
    private String description;
    private double openingBalance;
    private double custMinBudget;
    private Group group;
    private boolean deleted;
    private boolean eliminateflag;
    private Company company;
    private KWLCurrency currency;
    private Date creationDate;
    private double life;
    private double salvage;
    private double budget;
    private double presentValue;
    private Set<Account> children;
    private MasterItem category;
    private MasterItem department;
    private MasterItem location;
    private String installation;
    private User user;
    private boolean depreciable;
    private CostCenter costcenter;
    private String taxid;
    private String acccode;
    private boolean headeraccountflag;
    private Long templatepermcode;
    private String crmaccountid;
    private boolean intercompanyflag;
    private MasterItem intercompanytype;
    private AccountCustomData accAccountCustomData;
    private String aliascode;
    private int accounttype; // 0 - PnL, 1 - Balance Sheet
    private int mastertypevalue;//1 - GL, 2 - Cash, 3 - Bank, 4 - Normal GST
    private boolean controlAccounts;
    private boolean IBGBank;
    private boolean activate;
    private String usedIn;      // Save the Purpose where that Account is Tagged 
    private boolean wantToPostJe; // Need to decide either we need to allow to post Manual JE or not

    private Account propagatedAccountID;//to save parent accountid in child company's record
    private int ibgBankType;       // 1 - DBS, 2 - CIMB, 3 - UOB, 4 - OCBC
/*
* ====================== Used for INDIA country =====================
*/
    private String ifsccode; // Used for INDIA country
    private String micrcode; // Used for INDIA country
    private String MVATCode; // Used for INDIA country
    private String bankbranchname;
    private String accountno;
    private String bankbranchaddress;
    private State branchstate;
    private Integer bsrcode;
    private Integer pincode;
    private String defaultaccountID;
/*
* ===============================================================
*/
    private String purchaseType;
    private String salesType;          // This field for DVAT Form 31 

    public Account getPropagatedAccountID() {
        return propagatedAccountID;
    }

    public void setPropagatedAccountID(Account propagatedAccountID) {
        this.propagatedAccountID = propagatedAccountID;
    }

    public String getUsedIn() {
        return usedIn;
    }

    public void setUsedIn(String usedIn) {
        this.usedIn = usedIn;
    }

    public boolean isWantToPostJe() {
        return wantToPostJe;
    }

    public boolean getWantToPostJe() {
        return wantToPostJe;
    }

    public void setWantToPostJe(boolean wantToPostJe) {
        this.wantToPostJe = wantToPostJe;
    }

    public String getAliascode() {
        return aliascode;
    }

    public void setAliascode(String aliascode) {
        this.aliascode = aliascode;
    }

    public boolean isIntercompanyflag() {
        return intercompanyflag;
    }

    public void setIntercompanyflag(boolean intercompanyflag) {
        this.intercompanyflag = intercompanyflag;
    }

    public MasterItem getIntercompanytype() {
        return intercompanytype;
    }

    public void setIntercompanytype(MasterItem intercompanytype) {
        this.intercompanytype = intercompanytype;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public String getAccountName() {
        return getName();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(double openingBalance) {
        this.openingBalance = openingBalance;
    }

    public double getCustMinBudget() {
        return custMinBudget;
    }

    public void setCustMinBudget(double custMinBudget) {
        this.custMinBudget = custMinBudget;
    }

    public Account getParent() {
        return parent;
    }

    public void setParent(Account parent) {
        this.parent = parent;
    }

    public Company getCompany() {
        return company;
    }

    public boolean isHeaderaccountflag() {
        return headeraccountflag;
    }

    public void setHeaderaccountflag(boolean headeraccountflag) {
        this.headeraccountflag = headeraccountflag;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isEliminateflag() {
        return eliminateflag;
    }

    public void setEliminateflag(boolean eliminateflag) {
        this.eliminateflag = eliminateflag;
    }

    public Set<Account> getChildren() {
        return children;
    }

    public void setChildren(Set<Account> children) {
        this.children = children;
    }

    public int compareTo(Object o) {
        return this.name.compareTo(((Account) o).getName());
    }

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public double getLife() {
        return life;
    }

    public void setLife(double life) {
        this.life = life;
    }

    public double getPresentValue() {
        return presentValue;
    }

    public void setPresentValue(double presentValue) {
        this.presentValue = presentValue;
    }

    public double getSalvage() {
        return salvage;
    }

    public void setSalvage(double salvage) {
        this.salvage = salvage;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }

//    public Account getDepreciationAccont() {
//        return depreciationAccont;
//    }
//
//    public void setDepreciationAccont(Account depreciationAccont) {
//        this.depreciationAccont = depreciationAccont;
//    }

    public CostCenter getCostcenter() {
        return costcenter;
    }

    public void setCostcenter(CostCenter costcenter) {
        this.costcenter = costcenter;
    }

    public MasterItem getCategory() {
        return category;
    }

    public void setCategory(MasterItem category) {
        this.category = category;
    }

    public String getTaxid() {
        return taxid;
    }

    public void setTaxid(String taxid) {
        this.taxid = taxid;
    }

    public String getAcccode() {
        return acccode;
    }

    public String getAccountCode() {
        return getAcccode();
    }

    public void setAcccode(String acccode) {
        this.acccode = acccode;
    }

    public String getCrmaccountid() {
        return crmaccountid;
    }

    public void setCrmaccountid(String crmaccountid) {
        this.crmaccountid = crmaccountid;
    }

    public MasterItem getDepartment() {
        return department;
    }

    public void setDepartment(MasterItem department) {
        this.department = department;
    }

    public MasterItem getLocation() {
        return location;
    }

    public void setLocation(MasterItem location) {
        this.location = location;
    }

    public String getInstallation() {
        return installation;
    }

    public void setInstallation(String installation) {
        this.installation = installation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getTemplatepermcode() {
        return templatepermcode;
    }

    public void setTemplatepermcode(Long templatepermcode) {
        this.templatepermcode = templatepermcode;
    }

    public boolean isDepreciable() {
        return depreciable;
    }

    public void setDepreciable(boolean depreciable) {
        this.depreciable = depreciable;
    }

    public AccountCustomData getAccAccountCustomData() {
        return accAccountCustomData;
    }

    public void setAccAccountCustomData(AccountCustomData accAccountCustomData) {
        this.accAccountCustomData = accAccountCustomData;
    }

    public int getAccounttype() {
        return accounttype;
    }

    public void setAccounttype(int accounttype) {
        this.accounttype = accounttype;
    }

    public int getMastertypevalue() {
        return mastertypevalue;
    }

    public void setMastertypevalue(int mastertypevalue) {
        this.mastertypevalue = mastertypevalue;
    }

    public boolean isControlAccounts() {
        return controlAccounts;
    }

    public void setControlAccounts(boolean controlAccounts) {
        this.controlAccounts = controlAccounts;
    }

    public boolean isIBGBank() {
        return IBGBank;
    }

    public void setIBGBank(boolean IBGBank) {
        this.IBGBank = IBGBank;
    }

    public boolean isActivate() {
        return activate;
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    public int getIbgBankType() {
        return ibgBankType;
    }

    public void setIbgBankType(int ibgBankType) {
        this.ibgBankType = ibgBankType;
    }
    public String getIfsccode() {
        return ifsccode;
    }
    public void setIfsccode(String ifsccode) {
        this.ifsccode = ifsccode;
    }

    public String getMVATCode() {
        return MVATCode;
    }

    public void setMVATCode(String MVATCode) {
        this.MVATCode = MVATCode;
    }
    public String getMicrcode() {
        return micrcode;
    }
    public void setMicrcode(String micrcode) {
        this.micrcode = micrcode;
    }
    
    public String getPurchaseType() {
        return purchaseType;
    }

    public void setPurchaseType(String purchaseType) {
        this.purchaseType = purchaseType;
    }
    //====================== Used for INDIA country =====================
    public String getBankbranchaddress() {
        return bankbranchaddress;
    }

    public void setBankbranchaddress(String bankbranchaddress) {
        this.bankbranchaddress = bankbranchaddress;
    }

    public String getBankbranchname() {
        return bankbranchname;
    }

    public void setBankbranchname(String bankbranchname) {
        this.bankbranchname = bankbranchname;
    }

    public String getAccountno() {
        return accountno;
    }

    public void setAccountno(String accountno) {
        this.accountno = accountno;
    }

    public State getBranchstate() {
        return branchstate;
    }

    public void setBranchstate(State branchstate) {
        this.branchstate = branchstate;
    }

    public Integer getBsrcode() {
        return bsrcode;
    }

    public void setBsrcode(Integer bsrcode) {
        this.bsrcode = bsrcode;
    }

    public Integer getPincode() {
        return pincode;
    }

    public void setPincode(Integer pincode) {
        this.pincode = pincode;
    }
 public String getDefaultaccountID() {
        return defaultaccountID;
    }

    public void setDefaultaccountID(String defaultaccountID) {
        this.defaultaccountID = defaultaccountID;
    }

    public String getSalesType() {
        return salesType;
    }

    public void setSalesType(String salesType) {
        this.salesType = salesType;
    }

}
