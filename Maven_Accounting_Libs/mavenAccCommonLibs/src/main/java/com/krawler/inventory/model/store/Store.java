/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.store;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.inventory.model.cyclecount.Week;
import com.krawler.inventory.model.location.Location;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
public class Store implements Serializable {

    private String id;
    private String abbreviation;
    private String description;
    private String address;
    private String contactNo;
    private String faxNo;
    private boolean defaultStore;
    private boolean active;
    private StoreType storeType;
    private Set<String> movementTypeSet;
    private Company company;
    private Date createdOn;
    private Date modifiedOn;
    private User createdBy;
    private User modifiedBy;
    private boolean ccDateAllow;
    private boolean smccAllow;
    private Week lastDayOfWeek;
    private Set<Location> locationSet;
    private Set<User> storeManagerSet;
    private Set<User> storeExecutiveSet;
    private Location defaultLocation;
    private String parentId;
    private String VATTINnumber;// Used for INDIA country
    private String CSTTINnumber;// Used for INDIA country
    
    public Store() {
        this.locationSet = new HashSet();
        this.storeManagerSet = new HashSet();
        this.lastDayOfWeek = Week.SUNDAY;
        this.ccDateAllow = true;
        this.smccAllow = true;
        this.active = true;
    }

    public Store(String abbreviation, String description, String address, StoreType storeType, Company company) {
        this();
        this.abbreviation = abbreviation;
        this.description = description;
        this.address = address;
        this.storeType = storeType;
        this.company = company;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Set<Location> getLocationSet() {
        return locationSet;
    }

    public void setLocationSet(Set<Location> locationSet) {
        this.locationSet = locationSet;
    }

    public Set<User> getStoreManagerSet() {
        return storeManagerSet;
    }

    public void setStoreManagerSet(Set<User> storeManagerSet) {
        this.storeManagerSet = storeManagerSet;
    }

    public String getAbbreviation() {
        return abbreviation.toUpperCase();
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFaxNo() {
        return faxNo;
    }

    public void setFaxNo(String faxNo) {
        this.faxNo = faxNo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Date modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public StoreType getStoreType() {
        return storeType;
    }

    public void setStoreType(StoreType storeType) {
        this.storeType = storeType;
    }

    public boolean isCcDateAllow() {
        return ccDateAllow;
    }

    public void setCcDateAllow(boolean ccDateAllow) {
        this.ccDateAllow = ccDateAllow;
    }

    public boolean isSmccAllow() {
        return smccAllow;
    }

    public void setSmccAllow(boolean smccAllow) {
        this.smccAllow = smccAllow;
    }

    public Week getLastDayOfWeek() {
        return lastDayOfWeek;
    }

    public void setLastDayOfWeek(Week lastDayOfWeek) {
        this.lastDayOfWeek = lastDayOfWeek;
    }

    public User getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(User modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public boolean isDefaultStore() {
        return defaultStore;
    }

    public void setDefaultStore(boolean defaultStore) {
        this.defaultStore = defaultStore;
    }

    public Set<User> getStoreExecutiveSet() {
        return storeExecutiveSet;
    }

    public void setStoreExecutiveSet(Set<User> storeExecutiveSet) {
        this.storeExecutiveSet = storeExecutiveSet;
    }

    public Location getDefaultLocation() {
        return defaultLocation;
    }

    public void setDefaultLocation(Location defaultLocation) {
        this.defaultLocation = defaultLocation;
    }

    public Set<String> getMovementTypeSet() {
        return movementTypeSet;
    }

    public void setMovementTypeSet(Set<String> movementTypeSet) {
        this.movementTypeSet = movementTypeSet;
    }
/*-------------------- Only for Indian Company Other set as empty --------------------------*/
    public String getCSTTINnumber() {
        return CSTTINnumber;
    }

    public void setCSTTINnumber(String CSTTINnumber) {
        this.CSTTINnumber = CSTTINnumber;
    }

    public String getVATTINnumber() {
        return VATTINnumber;
    }

    public void setVATTINnumber(String VATTINnumber) {
        this.VATTINnumber = VATTINnumber;
    }
/*--------------------------------------------------------------------------------------*/
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (this.abbreviation != null) {
            sb.append(this.abbreviation);
        }
        if (sb.length() > 0 && this.description != null) {
            sb.append(" - ").append(this.description);
        } else {
            sb.append(this.description);
        }
        return sb.toString().trim();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Store other = (Store) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
}
