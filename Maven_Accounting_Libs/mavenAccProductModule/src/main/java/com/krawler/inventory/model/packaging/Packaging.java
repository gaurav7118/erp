/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.packaging;

import com.krawler.common.admin.Company;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.UnitOfMeasure;

/**
 *
 * @author Vipin Gupta
 */
public class Packaging {

    private String id;
    private Company company;
    private UnitOfMeasure casingUoM;
    private UnitOfMeasure innerUoM;
    private UnitOfMeasure stockUoM;
    private double casingUomValue;
    private double innerUomValue;
    private double stockUomValue;

    public Packaging() {
    }

    public Packaging(Company company, UnitOfMeasure casingUoM, UnitOfMeasure innerUoM, UnitOfMeasure stockUoM, double casingUomValue, double innerUomValue, double stockUomValue) {
        this();
        this.company = company;
        this.casingUoM = casingUoM;
        this.innerUoM = innerUoM;
        this.stockUoM = stockUoM;
        this.casingUomValue = Math.abs(casingUomValue);
        this.innerUomValue = Math.abs(innerUomValue);
        this.stockUomValue = Math.abs(stockUomValue);
    }

    public Packaging(Company company, UnitOfMeasure innerUoM, UnitOfMeasure stockUoM, double innerUomValue, double stockUomValue) {
        this();
        this.company = company;
        this.innerUoM = innerUoM;
        this.stockUoM = stockUoM;
        this.innerUomValue = Math.abs(innerUomValue);
        this.stockUomValue = Math.abs(stockUomValue);
    }

    public Packaging(Company company, UnitOfMeasure stockUoM, double stockUomValue) {
        this();
        this.company = company;
        this.stockUoM = stockUoM;
        this.stockUomValue = Math.abs(stockUomValue);
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UnitOfMeasure getCasingUoM() {
        return casingUoM;
    }

    public void setCasingUoM(UnitOfMeasure casingUoM) {
        this.casingUoM = casingUoM;
    }

    public double getCasingUomValue() {
        return casingUomValue;
    }

    public void setCasingUomValue(double casingUomValue) {
        this.casingUomValue = Math.abs(casingUomValue);

    }

    public UnitOfMeasure getInnerUoM() {
        return innerUoM;
    }

    public void setInnerUoM(UnitOfMeasure innerUoM) {
        this.innerUoM = innerUoM;
    }

    public double getInnerUomValue() {
        return innerUomValue;
    }

    public void setInnerUomValue(double innerUomValue) {
        this.innerUomValue = Math.abs(innerUomValue);
    }

    public UnitOfMeasure getStockUoM() {
        return stockUoM;
    }

    public void setStockUoM(UnitOfMeasure stockUoM) {
        this.stockUoM = stockUoM;
    }

    public double getStockUomValue() {
        return stockUomValue;
    }

    public void setStockUomValue(double stockUomValue) {
        this.stockUomValue = Math.abs(stockUomValue);
    }

    public double getStockUomQtyFactor(UnitOfMeasure uom) {
        double stockQtyFactor = 1;
        if (this.stockUoM != null && stockUoM.equals(uom)) {
            stockQtyFactor = 1;
        } else if (this.innerUoM != null && innerUoM.equals(uom)) {
            if (stockUomValue == 0) {
                stockQtyFactor = 1;
            } else {
                stockQtyFactor = stockUomValue;
            }
        } else if (this.casingUoM != null && casingUoM.equals(uom)) {
            double iuv = innerUomValue, suv = stockUomValue;
            if (innerUomValue == 0) {
                iuv = 1;
            }
            if (stockUomValue == 0) {
                suv = 1;
            }
            stockQtyFactor = iuv * suv;
        }
        return stockQtyFactor;
    }

    public double getQuantityInStockUoM(UnitOfMeasure uom, double quantity) {
        double stockQtyFactor = getStockUomQtyFactor(uom);
        return stockQtyFactor * quantity;
    }

    public static String packagingPreview(String casingUomName, double casingUomValue,String innerUoMName, double innerUomValue,String stockUomName, double stockUomValue) {
        StringBuilder packaging = new StringBuilder();
        if (!StringUtil.isNullOrEmpty(casingUomName) && casingUomValue != 0) {
            packaging.append(casingUomValue).append(" ").append(casingUomName).append(" X ");
        }
        if (!StringUtil.isNullOrEmpty(innerUoMName) && innerUomValue != 0) {
            packaging.append(innerUomValue).append(" ").append(innerUoMName).append(" X ");
        }
        if (!StringUtil.isNullOrEmpty(stockUomName) && stockUomValue != 0) {
            packaging.append(stockUomValue).append(" ").append(stockUomName);
        }
        return packaging.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder packaging = new StringBuilder();
        if (this.casingUoM != null && casingUomValue != 0) {
            packaging.append(casingUomValue).append(" ").append(this.casingUoM.getNameEmptyforNA()).append(" X ");
        }
        if (this.innerUoM != null && innerUomValue != 0) {
            packaging.append(innerUomValue).append(" ").append(this.innerUoM.getNameEmptyforNA()).append(" X ");
        }
        if (this.stockUoM != null && stockUomValue != 0) {
            packaging.append(stockUomValue).append(" ").append(this.stockUoM.getNameEmptyforNA());
        }
        return packaging.toString();
    }
}
