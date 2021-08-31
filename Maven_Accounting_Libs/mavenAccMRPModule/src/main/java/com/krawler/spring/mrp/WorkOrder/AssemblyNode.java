/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.WorkOrder;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pandurang
 */
public class AssemblyNode {

    private String productId;
    private String parentProductId;
    private WorkOrderComponentDetails workOrderComponentDetails;
    private double minBuildQty;    
    private double maxBuildQty;    
    private double usedBuildQty;
    private double demandBuildQty;
    private double requiredQty;
    private double availableQty;
    private List<AssemblyNode> subProducts;

    public AssemblyNode(String productId, double requiredQty, double availableQty) {
        this(productId, availableQty);
        if (requiredQty > 0) {
            this.requiredQty = requiredQty;
        }
    }

    public AssemblyNode(String productId, double availableQty) {
        this(productId);
        this.availableQty = availableQty;
    }

    public AssemblyNode(String productId) {
        this.productId = productId;
        subProducts = new ArrayList();
        requiredQty = 1;
        workOrderComponentDetails=null;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getParentProductId() {
        return parentProductId;
    }

    public void setParentProductId(String parentProductId) {
        this.parentProductId = parentProductId;
    }

    public WorkOrderComponentDetails getWorkOrderComponentDetails() {
        return workOrderComponentDetails;
    }

    public void setWorkOrderComponentDetails(WorkOrderComponentDetails workOrderComponentDetails) {
        this.workOrderComponentDetails = workOrderComponentDetails;
    }
    
    
    public double getAvailableQty() {
        return availableQty;
    }

    public void setAvailableQty(double availableQty) {
        this.availableQty = availableQty;
    }

    public double getRequiredQty() {
        return requiredQty;
    }

    public void setRequiredQty(double requiredQty) {
        this.requiredQty = requiredQty;
    }

    public List<AssemblyNode> getSubProducts() {
        return subProducts;
    }

    public void addSubProduct(AssemblyNode subProduct) {
        subProducts.add(subProduct);
    }

    
    public void processBuild(int demandQty) {
        double minBuildQty = minBuild(demandQty);
        usedBuildQty = minBuildQty;
        maxBuildQty = minBuildQty;
        fillUsedQty(this); //set Actual Used Quantity
        build(this);// Get Max quantity Used for Build 
    }
    private double minBuild(double demandQty) {
        this.demandBuildQty = demandQty;
        if (!subProducts.isEmpty()) {
            for (AssemblyNode assemblyNode : subProducts) {
                demandQty = this.demandBuildQty - (double) (availableQty / requiredQty);    //ERP-40349 : Values will be treated as double now onward. It ll reflect into Close WO.
                double beforebuildQty = (double) (assemblyNode.getSubProducts().isEmpty() ? assemblyNode.getAvailableQty() : (assemblyNode.minBuild(demandQty)));
                double buildQty = 0;
                if (beforebuildQty == assemblyNode.getAvailableQty()) {
                    buildQty = (double) (((beforebuildQty / assemblyNode.getRequiredQty())*100)/100);   //ERP-40349 : Do not 'ROUND' the values.
                } else {
                    buildQty = (double) (((assemblyNode.getAvailableQty() / assemblyNode.getRequiredQty())*100)/100);
                }

                demandBuildQty = buildQty;
            }
            
            this.minBuildQty = demandBuildQty;

        } else {
            this.minBuildQty = 0;
        }
        return minBuildQty;
    }
  
    private void build(AssemblyNode mainAssemblyNode) {
        if (! mainAssemblyNode.getSubProducts().isEmpty()) {
            for (AssemblyNode assemblyNode :  mainAssemblyNode.getSubProducts()) {
                build(assemblyNode);
                double subBuild=assemblyNode.getMaxBuildQty();
                subBuild +=assemblyNode.getSubProducts().isEmpty() ? 0: assemblyNode.getAvailableQty() ;
                double buildQty = (double)((((subBuild)/ assemblyNode.getRequiredQty())*100)/100); //ERP-40349 : Do not 'ROUND' the values.
                if(mainAssemblyNode.getMaxBuildQty() == 0 || mainAssemblyNode.getMaxBuildQty() > buildQty){
                    mainAssemblyNode.setMaxBuildQty(buildQty);
                }
            }
            for (AssemblyNode assemblyNode :  mainAssemblyNode.getSubProducts()) {
                assemblyNode.setMaxBuildQty((double)(((mainAssemblyNode.getMaxBuildQty() * assemblyNode.getRequiredQty())*100)/100));  //ERP-40349 : Do not 'ROUND' the values.
            }
           
        }else{
            mainAssemblyNode.setMaxBuildQty((double)mainAssemblyNode.getAvailableQty());
        }
       
    }

    private void fillUsedQty(AssemblyNode mainAssemblyNode) {
        for (AssemblyNode assemblyNode : mainAssemblyNode.getSubProducts()) {
            double usedQty = (double)(((mainAssemblyNode.getUsedBuildQty() * assemblyNode.getRequiredQty())*100)/100);  //ERP-40349 : Do not 'ROUND' the values.
            if(usedQty > assemblyNode.getAvailableQty()){
                usedQty = (double)(((usedQty - assemblyNode.getAvailableQty())*100)/100);  //ERP-40349 : Do not 'ROUND' the values.
            }
            assemblyNode.setUsedBuildQty(usedQty);
            if(!assemblyNode.getSubProducts().isEmpty()){
                fillUsedQty(assemblyNode);
            }
        }
    }

    public double getUsedBuildQty() {
        return usedBuildQty;
    }

    public void setUsedBuildQty(double usedBuildQty) {
        this.usedBuildQty = usedBuildQty;
    }

    public double getMinBuildQty() {
        return minBuildQty;
    }

    public void setMinBuildQty(double minBuildQty) {
        this.minBuildQty = minBuildQty;
    }

    public double getMaxBuildQty() {
        return maxBuildQty;
    }

    public void setMaxBuildQty(double maxBuildQty) {
        this.maxBuildQty = maxBuildQty;
    }
    

    
    
}
