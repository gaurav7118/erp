/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class AssemblySubProductBatchSerialMapping {

    private String id;
    private ProductBatch subproductbatch;
    private BatchSerial mainproductserial;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BatchSerial getMainproductserial() {
        return mainproductserial;
    }

    public void setMainproductserial(BatchSerial mainproductserial) {
        this.mainproductserial = mainproductserial;
    }

    public ProductBatch getSubproductbatch() {
        return subproductbatch;
    }

    public void setSubproductbatch(ProductBatch subproductbatch) {
        this.subproductbatch = subproductbatch;
    }
}
