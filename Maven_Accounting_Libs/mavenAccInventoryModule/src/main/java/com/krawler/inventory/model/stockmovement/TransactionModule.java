/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockmovement;

/**
 *
 * @author Vipin Gupta
 */
public enum TransactionModule {
    //Always add module in last because it is using in DB

    STOCK_REQUEST,
    ISSUE_NOTE,
    INTER_STORE_TRANSFER,
    STOCK_ADJUSTMENT,
    CYCLE_COUNT,
    ERP_GRN,
    ERP_DO,
    ERP_Consignment_DO,
    ERP_PURCHASE_RETURN,
    ERP_SALES_RETURN,
    ERP_PRODUCT,
    INTER_LOCATION_TRANSFER,
    IMPORT, //only for olympus S.M. import
    PRODUCT_BUILD_ASSEMBLY,
    PRODUCT_UNBUILD_ASSEMBLY,
    ERP_Consignment_GR,
    Work_Order;
    
    public String getString() {
        String moduleName = "";
        switch (this) {
            case STOCK_REQUEST:
                moduleName = "Stock Request";
                break;
            case ISSUE_NOTE:
                moduleName = "Issue Note";
                break;
            case INTER_STORE_TRANSFER:
                moduleName = "Inter Store Transfer";
                break;
            case STOCK_ADJUSTMENT:
                moduleName = "Stock Adjustment";
                break;
            case CYCLE_COUNT:
                moduleName = "Cycle Count";
                break;
            case ERP_GRN:
                moduleName = "Goods Reciept Note";
                break;
            case ERP_Consignment_DO:
                moduleName = "Consignment DO";
                break;
            case ERP_Consignment_GR:
                moduleName = "Consignment GRN";
                break;
            case ERP_DO:
                moduleName = "Delivery Order";
                break;
            case ERP_PURCHASE_RETURN:
                moduleName = "Purchase Return";
                break;
            case ERP_SALES_RETURN:
                moduleName = "Sales Return";
                break;
            case ERP_PRODUCT:
                moduleName = "New Product";
                break;
            case INTER_LOCATION_TRANSFER:
                moduleName = "Inter Location Transfer";
                break;
            case IMPORT:
                moduleName = "Import";
                break;
            case PRODUCT_BUILD_ASSEMBLY:
                moduleName = "Product Build Assembly";
                break;
            case PRODUCT_UNBUILD_ASSEMBLY:
                moduleName = "Product Unbuild Assembly";
                break;
            case Work_Order:
                moduleName = "Work Order";
                break;
                
        }
        return moduleName;
    }
}
