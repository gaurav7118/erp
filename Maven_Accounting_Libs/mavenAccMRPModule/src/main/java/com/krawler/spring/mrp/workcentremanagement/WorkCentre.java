/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.workcentremanagement;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.CostCenter;
import com.krawler.common.admin.InventoryWarehouse;
import com.krawler.common.admin.User;
import com.krawler.hql.accounting.*;
import com.krawler.spring.mrp.labormanagement.LabourWorkCentreMapping;
import com.krawler.spring.mrp.machinemanagement.MachineWorkCenterMapping;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class WorkCentre {
    
    private String ID;
    private SequenceFormat seqformat;
    private String name;
    private String workcenterid;
    private MasterItem workcenterlocation;
    private double workcentercapacity;
    private MasterItem worktype;
    private MasterItem workcentermanager;
    private Company company;
    private CostCenter costcenter;
    private User createdby;
    private User modifiedby;   
    private InventoryWarehouse warehouseid;   
    private MasterItem workcentertype;   
    private int seqnumber;  //Only to store integer part of sequence format
    private boolean autoGenerated; 
    private String datePreffixValue;//Only to store Date Preffix part of sequence format
    private String dateSuffixValue;//Only to store Date Sufefix part of sequence format
    private String dateAfterPreffixValue;//Only to store Date After Prefix part of sequence format
    private Set<MachineWorkCenterMapping> machineworkcentremappings;
    private Set<LabourWorkCentreMapping> labourworkcentremappings;
    private Set<ProductWorkCentreMapping> productworkcentremappings;
    private Set<MaterialWorkCentreMapping> materialworkcentremappings;
    private boolean deleted;//Value 'T'  or 'F'private boolean deleted;//Value 'T'  or 'F'
    private WorkCentreCustomData accWorkCentreCustomData;
//    private Set<Machine> contractMappings;    
//    private Set<Labour> contractMappings;
//    private Set<Material> contractMappings;
//    private Set<Labour> contractMappings;
    
    public static final String WCID= "id";
    public static final String SEQUENCEFORMAT= "seqformat";
    public static final String WORKCENTRENAME= "name";
    public static final String WORKCENTREID= "workcenterid";
    public static final String WORKCENTERLOCATION= "workcenterlocation";
    public static final String WORKCENTERLOCATIONID= "workcenterlocationid";
    public static final String WORKCENTRECAPACITY= "workcentercapacity";
    public static final String WORKTYPE= "worktype";
    public static final String WORKTYPEID= "worktypeid";
    public static final String WORKCENTREMANAGER= "workcentermanager";
    public static final String WORKCENTREMANAGERID= "workcentermanagerid";
    public static final String WORKCENTREMANAGERUSERID= "workcentermanageruserid";
    public static final String COMPANYID= "company";
    public static final String COSTCENTER= "costcenter";
    public static final String COSTCENTERID= "costcenterid";
    public static final String CREATEDBY= "createdby";
    public static final String CREATEDBYID= "createdbyid";
    public static final String WORKCENTRETYPE= "workcentertype";
    public static final String WORKCENTRETYPEID= "workcentertypeid";
    public static final String MACHINENAME= "machinename";
    public static final String MACHINEID= "machineid";
    public static final String MATERIALID= "materialid";
    public static final String MATERIALNAME= "materialname";
    public static final String LABOURID= "labourid";
    public static final String LABOURNAME= "labourname";
    public static final String ROUTECODEID= "routecodeid";
    public static final String CUSTOMERID= "customerid";
    public static final String CUSTOMERNAME= "customer";
    public static final String MODIFIEDBYNAME= "modifiedby";
    public static final String MODIFIEDBYID= "modifiedbyid";
    public static final String WAREHOUSE= "warehouse";
    public static final String WAREHOUSEID= "warehouseid";
    public static final String LABOURWCMAP= "labourWCMapping";
    public static final String MACHINEWCMAP= "machineWCMapping";
    public static final String PRODUCTWCMAP= "productWCMapping";
    public static final String MATERIALWCMAP= "materialWCMapping";
    public static final String PRODUCTID= "productid";
    public static final String PRODUCTNAME= "productname";
    public static final String KEY= "name";

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public CostCenter getCostcenter() {
        return costcenter;
    }

    public void setCostcenter(CostCenter costcenter) {
        this.costcenter = costcenter;
    }

    public User getCreatedby() {
        return createdby;
    }

    public void setCreatedby(User createdby) {
        this.createdby = createdby;
    }

    public User getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(User modifiedby) {
        this.modifiedby = modifiedby;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SequenceFormat getSeqformat() {
        return seqformat;
    }

    public void setSeqformat(SequenceFormat seqformat) {
        this.seqformat = seqformat;
    }

    public double getWorkcentercapacity() {
        return workcentercapacity;
    }

    public void setWorkcentercapacity(double workcentercapacity) {
        this.workcentercapacity = workcentercapacity;
    }

    public String getWorkcenterid() {
        return workcenterid;
    }

    public void setWorkcenterid(String workcenterid) {
        this.workcenterid = workcenterid;
    }

    public MasterItem getWorkcenterlocation() {
        return workcenterlocation;
    }

    public void setWorkcenterlocation(MasterItem workcenterlocation) {
        this.workcenterlocation = workcenterlocation;
    }

    public MasterItem getWorkcentermanager() {
        return workcentermanager;
    }

    public void setWorkcentermanager(MasterItem workcentermanager) {
        this.workcentermanager = workcentermanager;
    }

    public MasterItem getWorkcentertype() {
        return workcentertype;
    }

    public void setWorkcentertype(MasterItem workcentertype) {
        this.workcentertype = workcentertype;
    }

    public MasterItem getWorktype() {
        return worktype;
    }

    public void setWorktype(MasterItem worktype) {
        this.worktype = worktype;
    }

    public InventoryWarehouse getWarehouseid() {
        return warehouseid;
    }

    public void setWarehouseid(InventoryWarehouse warehouseid) {
        this.warehouseid = warehouseid;
    }

    public boolean isAutoGenerated() {
        return autoGenerated;
    }

    public void setAutoGenerated(boolean autoGenerated) {
        this.autoGenerated = autoGenerated;
    }

    public String getDatePreffixValue() {
        return datePreffixValue;
    }

    public void setDatePreffixValue(String datePreffixValue) {
        this.datePreffixValue = datePreffixValue;
    }

    public String getDateSuffixValue() {
        return dateSuffixValue;
    }

    public void setDateSuffixValue(String dateSuffixValue) {
        this.dateSuffixValue = dateSuffixValue;
    }

    public int getSeqnumber() {
        return seqnumber;
    }

    public void setSeqnumber(int seqnumber) {
        this.seqnumber = seqnumber;
    }

    public Set<LabourWorkCentreMapping> getLabourworkcentremappings() {
        return labourworkcentremappings;
    }

    public void setLabourworkcentremappings(Set<LabourWorkCentreMapping> labourworkcentremappings) {
        this.labourworkcentremappings = labourworkcentremappings;
    }

    public Set<MachineWorkCenterMapping> getMachineworkcentremappings() {
        return machineworkcentremappings;
    }

    public void setMachineworkcentremappings(Set<MachineWorkCenterMapping> machineworkcentremappings) {
        this.machineworkcentremappings = machineworkcentremappings;
    }

    public Set<MaterialWorkCentreMapping> getMaterialworkcentremappings() {
        return materialworkcentremappings;
    }

    public void setMaterialworkcentremappings(Set<MaterialWorkCentreMapping> materialworkcentremappings) {
        this.materialworkcentremappings = materialworkcentremappings;
    }

    public Set<ProductWorkCentreMapping> getProductworkcentremappings() {
        return productworkcentremappings;
    }

    public void setProductworkcentremappings(Set<ProductWorkCentreMapping> productworkcentremappings) {
        this.productworkcentremappings = productworkcentremappings;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public WorkCentreCustomData getAccWorkCentreCustomData() {
        return accWorkCentreCustomData;
    }

    public void setAccWorkCentreCustomData(WorkCentreCustomData accWorkCentreCustomData) {
        this.accWorkCentreCustomData = accWorkCentreCustomData;
    }
    
    public String getDateAfterPreffixValue() {
        return dateAfterPreffixValue;
    }

    public void setDateAfterPreffixValue(String dateAfterPreffixValue) {
        this.dateAfterPreffixValue = dateAfterPreffixValue;
    }
}