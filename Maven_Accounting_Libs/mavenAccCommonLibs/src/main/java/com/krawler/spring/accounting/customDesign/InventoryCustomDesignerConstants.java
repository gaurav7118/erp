/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.spring.accounting.customDesign;

import java.util.HashMap;

public class InventoryCustomDesignerConstants{
    public static final String INVENTORYTRANSACTIONNUMBER = "inventorytransactionnumber";
    public static final String INVENTORYREASON = "inventoryreason";
    public static final String INVENTORYDATE = "inventorydate";
    public static final String FROMSTORENAME = "fromstorename";
    public static final String TOSTORENAME = "tostorename";
    
    public static HashMap<String, String> CustomDesignStockRepairExtraFieldsMap = new HashMap<String, String>();
    static{
        CustomDesignStockRepairExtraFieldsMap.put(CustomDesignerConstants.FromStore, "{label:'From Store',xtype:'1'}");
        CustomDesignStockRepairExtraFieldsMap.put(CustomDesignerConstants.ToStore, "{label:'To Store',xtype:'1'}");
        CustomDesignStockRepairExtraFieldsMap.put(InventoryCustomDesignerConstants.FROMSTORENAME, "{label:'From Store Name',xtype:'1'}");
        CustomDesignStockRepairExtraFieldsMap.put(InventoryCustomDesignerConstants.TOSTORENAME, "{label:'To Store Name',xtype:'1'}");
        CustomDesignStockRepairExtraFieldsMap.put(InventoryCustomDesignerConstants.INVENTORYTRANSACTIONNUMBER, "{label:'Stock Repair Number',xtype:'1'}");
        CustomDesignStockRepairExtraFieldsMap.put(CustomDesignerConstants.Createdby, "{label:'Created By',xtype:'1'}");
        CustomDesignStockRepairExtraFieldsMap.put(InventoryCustomDesignerConstants.INVENTORYDATE, "{label:'Stock Repair Date',xtype:'1'}");
        CustomDesignStockRepairExtraFieldsMap.put(CustomDesignerConstants.TotalQuantity, "{label:'Total Quantity',xtype:'2'}");
        CustomDesignStockRepairExtraFieldsMap.put(CustomDesignerConstants.PageNumberField, "{label:'Page Number',xtype:'1'}");
    }
}