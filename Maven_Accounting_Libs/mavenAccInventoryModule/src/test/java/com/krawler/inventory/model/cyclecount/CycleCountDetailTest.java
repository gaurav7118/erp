/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.cyclecount;

import com.krawler.hql.accounting.Product;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Vipin Gupta
 */
public class CycleCountDetailTest {
    

    @Test
    public void testVariance() {
        
        Product product = new Product();
        product.setIsSerialForProduct(true);
        CycleCount cycleCount = new CycleCount();
        cycleCount.setProduct(product);
        CycleCountDetail instance = new CycleCountDetail();
//        CycleCountAdjustment instance = new CycleCountAdjustment();
        instance.setCycleCount(cycleCount);
        instance.setSystemQuantity(2);
        instance.setActualQuantity(3);
        instance.setSystemSerials("S1,S3,S4");
        instance.setActualSerials("S1,S2");
        
        
        System.out.println("getAddedQuantityVariance");
        double expResult = 1.0;
        double result = instance.getAddedQuantityVariance();
        Assert.assertEquals(expResult, result, 0.0);
        
        System.out.println("getRemovedQuantityVariance");
        expResult = 2.0;
        result = instance.getRemovedQuantityVariance();
        Assert.assertEquals(expResult, result, 0.0);
        
        System.out.println("getAddedSerialVariance");
        String expRes = "S2";
        String res = instance.getAddedSerialVariance();
        Assert.assertEquals(expRes, res);
        
        System.out.println("getRemovedSerialVariance");
        expRes = "S3,S4";
        res = instance.getRemovedSerialVariance();
        Assert.assertEquals(expRes, res);

    }
    
    public void testSplitCase(){
        System.out.println("testSplitCaseForOne");
        String serials = "s";
        List<String> list = Arrays.asList(serials.split(","));
        String expRes = "[s]";
        String res = list.toString();
        Assert.assertEquals(expRes, res);
    }

    
}
