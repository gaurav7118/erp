/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.contractmanagement;

import com.krawler.common.admin.Company;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.UnitOfMeasure;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class MRPContractDetails {

    private String ID;
    private int srno;
    private MRPContract mrpcontract;
    private Product product;
    private String description;
    private double quantity;
    private UnitOfMeasure uom;
    private double baseuomquantity;
    private double baseuomrate;
    private double rate;
    private double totalamount;
    private Company company;
    private MasterItem deliverymode;
    private int totalnoofunit;
    private int totalquantity;
    private Date shippingperiodfrom;
    private Date shippingperiodto;
    private int partialshipmentallowed;
    private MasterItem shipmentstatus;
    private String shippingagent;
    private String loadingportcountry;
    private String loadingport;
    private int transshipmentallowed;
    private String dischargeportcountry;
    private String dischargeport;
    private String finaldestination;
    private String postalcode;
    private String budgetfreightcost;
    private String shipmentcontractremarks;
    private String unitweightvalue;
    private String unitweight;
    private String packagingtype;
    private int certificaterequirement;
    private String certificate;
    private String numberoflayers;
    private String heatingpad;
    private String palletloadcontainer;
    private String shippingmarksdetails;
    private String shipmentmode;
    private String percontainerload;
    private String palletmaterial;
    private MasterItem packagingprofiletype;
    private String marking;
    private String drumorbagdetails;
    private String drumorbagsize;
    private MRPContractDetailsCustomData accMRPContractDetailsCustomData;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public MRPContract getMrpcontract() {
        return mrpcontract;
    }

    public void setMrpcontract(MRPContract mrpcontract) {
        this.mrpcontract = mrpcontract;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public double getBaseuomquantity() {
        return baseuomquantity;
    }

    public void setBaseuomquantity(double baseuomquantity) {
        this.baseuomquantity = baseuomquantity;
    }

    public double getBaseuomrate() {
        return baseuomrate;
    }

    public void setBaseuomrate(double baseuomrate) {
        this.baseuomrate = baseuomrate;
    }
    
    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(double totalamount) {
        this.totalamount = totalamount;
    }
    
    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public UnitOfMeasure getUom() {
        return uom;
    }

    public void setUom(UnitOfMeasure uom) {
        this.uom = uom;
    }

    public int getCertificaterequirement() {
        return certificaterequirement;
    }

    public void setCertificaterequirement(int certificaterequirement) {
        this.certificaterequirement = certificaterequirement;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getDrumorbagdetails() {
        return drumorbagdetails;
    }

    public void setDrumorbagdetails(String drumorbagdetails) {
        this.drumorbagdetails = drumorbagdetails;
    }

    public String getDrumorbagsize() {
        return drumorbagsize;
    }

    public void setDrumorbagsize(String drumorbagsize) {
        this.drumorbagsize = drumorbagsize;
    }

    public String getHeatingpad() {
        return heatingpad;
    }

    public void setHeatingpad(String heatingpad) {
        this.heatingpad = heatingpad;
    }

    public String getMarking() {
        return marking;
    }

    public void setMarking(String marking) {
        this.marking = marking;
    }

    public String getNumberoflayers() {
        return numberoflayers;
    }

    public void setNumberoflayers(String numberoflayers) {
        this.numberoflayers = numberoflayers;
    }

    public MasterItem getPackagingprofiletype() {
        return packagingprofiletype;
    }

    public void setPackagingprofiletype(MasterItem packagingprofiletype) {
        this.packagingprofiletype = packagingprofiletype;
    }

    public String getPackagingtype() {
        return packagingtype;
    }

    public void setPackagingtype(String packagingtype) {
        this.packagingtype = packagingtype;
    }

    public String getPalletloadcontainer() {
        return palletloadcontainer;
    }

    public void setPalletloadcontainer(String palletloadcontainer) {
        this.palletloadcontainer = palletloadcontainer;
    }

    public String getPalletmaterial() {
        return palletmaterial;
    }

    public void setPalletmaterial(String palletmaterial) {
        this.palletmaterial = palletmaterial;
    }

    public String getPercontainerload() {
        return percontainerload;
    }

    public void setPercontainerload(String percontainerload) {
        this.percontainerload = percontainerload;
    }

    public String getShipmentmode() {
        return shipmentmode;
    }

    public void setShipmentmode(String shipmentmode) {
        this.shipmentmode = shipmentmode;
    }

    public String getShippingmarksdetails() {
        return shippingmarksdetails;
    }

    public void setShippingmarksdetails(String shippingmarksdetails) {
        this.shippingmarksdetails = shippingmarksdetails;
    }

    public String getUnitweight() {
        return unitweight;
    }

    public void setUnitweight(String unitweight) {
        this.unitweight = unitweight;
    }

    public String getUnitweightvalue() {
        return unitweightvalue;
    }

    public void setUnitweightvalue(String unitweightvalue) {
        this.unitweightvalue = unitweightvalue;
    }

    public String getBudgetfreightcost() {
        return budgetfreightcost;
    }

    public void setBudgetfreightcost(String budgetfreightcost) {
        this.budgetfreightcost = budgetfreightcost;
    }

    public MasterItem getDeliverymode() {
        return deliverymode;
    }

    public void setDeliverymode(MasterItem deliverymode) {
        this.deliverymode = deliverymode;
    }

    public String getDischargeport() {
        return dischargeport;
    }

    public void setDischargeport(String dischargeport) {
        this.dischargeport = dischargeport;
    }

    public String getDischargeportcountry() {
        return dischargeportcountry;
    }

    public void setDischargeportcountry(String dischargeportcountry) {
        this.dischargeportcountry = dischargeportcountry;
    }

    public String getFinaldestination() {
        return finaldestination;
    }

    public void setFinaldestination(String finaldestination) {
        this.finaldestination = finaldestination;
    }

    public String getLoadingport() {
        return loadingport;
    }

    public void setLoadingport(String loadingport) {
        this.loadingport = loadingport;
    }

    public String getLoadingportcountry() {
        return loadingportcountry;
    }

    public void setLoadingportcountry(String loadingportcountry) {
        this.loadingportcountry = loadingportcountry;
    }

    public int getPartialshipmentallowed() {
        return partialshipmentallowed;
    }

    public void setPartialshipmentallowed(int partialshipmentallowed) {
        this.partialshipmentallowed = partialshipmentallowed;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

    public String getShipmentcontractremarks() {
        return shipmentcontractremarks;
    }

    public void setShipmentcontractremarks(String shipmentcontractremarks) {
        this.shipmentcontractremarks = shipmentcontractremarks;
    }

    public MasterItem getShipmentstatus() {
        return shipmentstatus;
    }

    public void setShipmentstatus(MasterItem shipmentstatus) {
        this.shipmentstatus = shipmentstatus;
    }

    public String getShippingagent() {
        return shippingagent;
    }

    public void setShippingagent(String shippingagent) {
        this.shippingagent = shippingagent;
    }

    public Date getShippingperiodfrom() {
        return shippingperiodfrom;
    }

    public void setShippingperiodfrom(Date shippingperiodfrom) {
        this.shippingperiodfrom = shippingperiodfrom;
    }

    public Date getShippingperiodto() {
        return shippingperiodto;
    }

    public void setShippingperiodto(Date shippingperiodto) {
        this.shippingperiodto = shippingperiodto;
    }

    public int getTotalnoofunit() {
        return totalnoofunit;
    }

    public void setTotalnoofunit(int totalnoofunit) {
        this.totalnoofunit = totalnoofunit;
    }

    public int getTotalquantity() {
        return totalquantity;
    }

    public void setTotalquantity(int totalquantity) {
        this.totalquantity = totalquantity;
    }

    public int getTransshipmentallowed() {
        return transshipmentallowed;
    }

    public void setTransshipmentallowed(int transshipmentallowed) {
        this.transshipmentallowed = transshipmentallowed;
    }

    public MRPContractDetailsCustomData getAccMRPContractDetailsCustomData() {
        return accMRPContractDetailsCustomData;
    }

    public void setAccMRPContractDetailsCustomData(MRPContractDetailsCustomData accMRPContractDetailsCustomData) {
        this.accMRPContractDetailsCustomData = accMRPContractDetailsCustomData;
    }
}