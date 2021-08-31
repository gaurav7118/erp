/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;
import com.krawler.common.admin.State;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class ExciseDetailsInvoice {
    
    private String id;
    private String supplier;
    private String supplierTINSalesTaxNo;
    private String supplierExciseRegnNo;
    private String cstnumber;
    private String supplierRange;
    private String supplierCommissioneRate;
    private String supplierAddress;
    private String supplierImporterExporterCode;
    private String supplierDivision;
    private String manufacturerName;
    private String manufacturerExciseregnNo;
    private String manufacturerRange;
    private String manufacturerCommissionerate;
    private String manufacturerDivision;
    private String manufacturerAddress;
    private String manufacturerImporterexporterCode;
    private GoodsReceipt goodsreceipt;
    private String supplierstate;
    private String registrationType;
    private String unitname;
    private String ECCNo;

    public String getECCNo() {
        return ECCNo;
    }

    public void setECCNo(String ECCNo) {
        this.ECCNo = ECCNo;
    }

    public String getRegistrationType() {
        return registrationType;
    }

    public void setRegistrationType(String registrationType) {
        this.registrationType = registrationType;
    }

    public String getUnitname() {
        return unitname;
    }

    public void setUnitname(String unitname) {
        this.unitname = unitname;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }  
    public GoodsReceipt getGoodsreceipt() {
        return goodsreceipt;
    }

    public void setGoodsreceipt(GoodsReceipt goodsreceipt) {
        this.goodsreceipt = goodsreceipt;
    }
    
    public String getManufacturerAddress() {
        return manufacturerAddress;
    }

    public void setManufacturerAddress(String manufacturerAddress) {
        this.manufacturerAddress = manufacturerAddress;
    }

    public String getManufacturerCommissionerate() {
        return manufacturerCommissionerate;
    }

    public void setManufacturerCommissionerate(String manufacturerCommissionerate) {
        this.manufacturerCommissionerate = manufacturerCommissionerate;
    }

    public String getManufacturerDivision() {
        return manufacturerDivision;
    }

    public void setManufacturerDivision(String manufacturerDivision) {
        this.manufacturerDivision = manufacturerDivision;
    }

    public String getManufacturerExciseregnNo() {
        return manufacturerExciseregnNo;
    }

    public void setManufacturerExciseregnNo(String manufacturerExciseregnNo) {
        this.manufacturerExciseregnNo = manufacturerExciseregnNo;
    }

    public String getManufacturerImporterexporterCode() {
        return manufacturerImporterexporterCode;
    }

    public void setManufacturerImporterexporterCode(String manufacturerImporterexporterCode) {
        this.manufacturerImporterexporterCode = manufacturerImporterexporterCode;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        this.manufacturerName = manufacturerName;
    }

    public String getManufacturerRange() {
        return manufacturerRange;
    }

    public void setManufacturerRange(String manufacturerRange) {
        this.manufacturerRange = manufacturerRange;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getSupplierAddress() {
        return supplierAddress;
    }

    public void setSupplierAddress(String supplierAddress) {
        this.supplierAddress = supplierAddress;
    }

    public String getSupplierCommissioneRate() {
        return supplierCommissioneRate;
    }

    public void setSupplierCommissioneRate(String supplierCommissioneRate) {
        this.supplierCommissioneRate = supplierCommissioneRate;
    }

    public String getSupplierDivision() {
        return supplierDivision;
    }

    public void setSupplierDivision(String supplierDivision) {
        this.supplierDivision = supplierDivision;
    }

    public String getSupplierExciseRegnNo() {
        return supplierExciseRegnNo;
    }

    public void setSupplierExciseRegnNo(String supplierExciseRegnNo) {
        this.supplierExciseRegnNo = supplierExciseRegnNo;
    }

    public String getCstnumber() {
        return cstnumber;
    }

    public void setCstnumber(String cstnumber) {
        this.cstnumber = cstnumber;
    }

    public String getSupplierImporterExporterCode() {
        return supplierImporterExporterCode;
    }

    public void setSupplierImporterExporterCode(String supplierImporterExporterCode) {
        this.supplierImporterExporterCode = supplierImporterExporterCode;
    }

    public String getSupplierRange() {
        return supplierRange;
    }

    public void setSupplierRange(String supplierRange) {
        this.supplierRange = supplierRange;
    }

    public String getSupplierTINSalesTaxNo() {
        return supplierTINSalesTaxNo;
    }

    public void setSupplierTINSalesTaxNo(String supplierTINSalesTaxNo) {
        this.supplierTINSalesTaxNo = supplierTINSalesTaxNo;
    }

    public String getSupplierstate() {
        return supplierstate;
    }

    public void setSupplierstate(String supplierstate) {
        this.supplierstate = supplierstate;
    }
    
    
}
