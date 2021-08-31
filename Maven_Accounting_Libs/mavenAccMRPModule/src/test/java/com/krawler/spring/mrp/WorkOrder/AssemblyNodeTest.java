/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.WorkOrder;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author krawler
 */
public class AssemblyNodeTest {

    @Test
    public void testGetBuildQuantity() {
        
      // Example : 1
        
        AssemblyNode p1 = new AssemblyNode("P1");
        p1.setParentProductId("");
        
        AssemblyNode p2 = new AssemblyNode("P2", 2, 4);
        p2.setParentProductId(p1.getProductId());
        AssemblyNode p5 = new AssemblyNode("P5",2, 10);
        p5.setParentProductId(p1.getProductId());
        AssemblyNode p6 = new AssemblyNode("P6",2, 14);
        p6.setParentProductId(p1.getProductId());
//        p1.addSubProduct(p2);
//        p1.addSubProduct(p5);
//        p1.addSubProduct(p6);
        
        AssemblyNode p3 = new AssemblyNode("P3",2, 6);
        AssemblyNode p4 = new AssemblyNode("P4",2, 8);
//        p2.addSubProduct(p3);
//        p2.addSubProduct(p4);
        p3.setParentProductId(p2.getProductId());
        p4.setParentProductId(p2.getProductId());
        
        
        List<AssemblyNode> assemblyNodes=new ArrayList<>();
        assemblyNodes.add(p1);
        assemblyNodes.add(p2);
        assemblyNodes.add(p3);
        assemblyNodes.add(p4);
        assemblyNodes.add(p5);
        assemblyNodes.add(p6);

        
        for (AssemblyNode assemblyNode1 : assemblyNodes) {
            for (AssemblyNode assemblyNode2 : assemblyNodes) {
                if (assemblyNode2.getParentProductId().equalsIgnoreCase(assemblyNode1.getProductId())) {
                    assemblyNode1.addSubProduct(assemblyNode2);
                }
            }
        } 
        
        p1.processBuild(10);
        Assert.assertEquals("Checking Used Quantity", 3.0, p1.getUsedBuildQty(), 0);
        Assert.assertEquals("Checking P2 Quantity", 2.0, p2.getUsedBuildQty(), 0);
        Assert.assertEquals("Checking P3 Quantity", 4.0, p3.getUsedBuildQty(), 0);
        Assert.assertEquals("Checking P4 Quantity", 4.0, p4.getUsedBuildQty(), 0);
        Assert.assertEquals("Checking P5 Quantity", 6.0, p5.getUsedBuildQty(), 0);
        Assert.assertEquals("Checking P6 Quantity", 6.0, p6.getUsedBuildQty(), 0);
        
        Assert.assertEquals(" Build Quantity", 3.0, p1.getMaxBuildQty(), 0);
        Assert.assertEquals("Checking Build P2 Quantity", 6.0, p2.getMaxBuildQty(), 0);
        Assert.assertEquals("Checking Build P3 Quantity", 6.0, p3.getMaxBuildQty(), 0);
        Assert.assertEquals("Checking Build P4 Quantity", 6.0, p4.getMaxBuildQty(), 0);
        Assert.assertEquals("Checking Build P5 Quantity", 6.0, p5.getMaxBuildQty(), 0);
        Assert.assertEquals("Checking Build P6 Quantity", 6.0, p6.getMaxBuildQty(), 0);
               
        //Example: 2
//        AssemblyNode p1 = new AssemblyNode("P1");
//        p1.setParentProductId("");
//        
//        AssemblyNode p2 = new AssemblyNode("P2", 1, 4);
//        p2.setParentProductId(p1.getProductId());
//        AssemblyNode p5 = new AssemblyNode("P5",1, 4);
//        p5.setParentProductId(p1.getProductId());
//        AssemblyNode p6 = new AssemblyNode("P6",1, 2);
//        p6.setParentProductId(p1.getProductId());
////        p1.addSubProduct(p2);
////        p1.addSubProduct(p5);
////        p1.addSubProduct(p6);
//        
//        AssemblyNode p3 = new AssemblyNode("P3",1,2);
//        AssemblyNode p4 = new AssemblyNode("P4",1, 2);
////        p2.addSubProduct(p3);
////        p2.addSubProduct(p4);
//        p3.setParentProductId(p6.getProductId());
//        p4.setParentProductId(p6.getProductId());
//        
//        
//        List<AssemblyNode> assemblyNodes=new ArrayList<>();
//        assemblyNodes.add(p1);
//        assemblyNodes.add(p2);
//        assemblyNodes.add(p3);
//        assemblyNodes.add(p4);
//        assemblyNodes.add(p5);
//        assemblyNodes.add(p6);
//
//        
//        for (AssemblyNode assemblyNode1 : assemblyNodes) {
//            for (AssemblyNode assemblyNode2 : assemblyNodes) {
//                if (assemblyNode2.getParentProductId().equalsIgnoreCase(assemblyNode1.getProductId())) {
//                    assemblyNode1.addSubProduct(assemblyNode2);
//                }
//            }
//        } 
//        
//        p1.processBuild(4);
//        Assert.assertEquals("Checking Used Quantity", 4.0, p1.getUsedBuildQty(), 0);
////        Assert.assertEquals("Checking P2 Quantity", 2.0, p2.getUsedBuildQty(), 0);
////        Assert.assertEquals("Checking P3 Quantity", 4.0, p3.getUsedBuildQty(), 0);
////        Assert.assertEquals("Checking P4 Quantity", 4.0, p4.getUsedBuildQty(), 0);
////        Assert.assertEquals("Checking P5 Quantity", 6.0, p5.getUsedBuildQty(), 0);
////        Assert.assertEquals("Checking P6 Quantity", 6.0, p6.getUsedBuildQty(), 0);
//        
////        Assert.assertEquals(" Build Quantity", 4.0, p1.getMaxBuildQty(), 0);
//        Assert.assertEquals("Checking Build P2 Quantity", 4.0, p2.getMaxBuildQty(), 0);
//        Assert.assertEquals("Checking Build P3 Quantity", 2.0, p3.getMaxBuildQty(), 0);
//        Assert.assertEquals("Checking Build P4 Quantity", 2.0, p4.getMaxBuildQty(), 0);
//        Assert.assertEquals("Checking Build P5 Quantity", 4.0, p5.getMaxBuildQty(), 0);
//        Assert.assertEquals("Checking Build P6 Quantity", 4.0, p6.getMaxBuildQty(), 0);
//        
//        
    }
}
