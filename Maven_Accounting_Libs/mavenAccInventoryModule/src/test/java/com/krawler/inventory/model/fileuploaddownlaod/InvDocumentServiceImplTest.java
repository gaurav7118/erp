/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.fileuploaddownlaod;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.InventoryModules;
import com.krawler.common.util.JUnitConstants;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import java.util.UUID;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author krawler
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/applicationContextList.xml")
public class InvDocumentServiceImplTest {

    @Autowired
    private InvDocumentService instance;
    @Autowired
    private HibernateTransactionManager txnManager;
    @Autowired
    private AccountingHandlerDAO accountingHandlerDAO;
    
    TransactionStatus status = null;
    Company company = null;

    public InvDocumentServiceImplTest() {
    }

    @Before
    public void setUp() throws ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("InvDocumentServiceTest_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        status = txnManager.getTransaction(def);
        
        KwlReturnObject companyResult = accountingHandlerDAO.getObject(Company.class.getName(), JUnitConstants.COMPANY_ID);
        company = (Company) companyResult.getEntityList().get(0);
    }

    @After
    public void tearDown() {
        instance = null;
        txnManager.rollback(status);
    }

    /**
     * Test of saveInventoryDocuments method, of class InvDocumentService.
     */
    @Test
    public void testSaveInventoryDocuments() throws Exception {
        System.out.println("saveInventoryDocuments");
        HashMap<String, Object> hashMap = new HashMap<>();
        String filename = UUID.randomUUID().toString();
        String fileName = "abc.txt";

        InventoryDocuments document = new InventoryDocuments();
        document.setDocID(filename);
        document.setDocName(fileName);
        document.setDocType("");

        InventoryModules im = InventoryModules.QA_INSPECTION_APPROVAL;
        InventoryDocumentCompMap inventoryDocumentMap = new InventoryDocumentCompMap();
        inventoryDocumentMap.setDocument(document);
        inventoryDocumentMap.setCompany(company);
        inventoryDocumentMap.setModuleWiseId(JUnitConstants.MODULE_WISE_ID);
        if (im != null) {
            inventoryDocumentMap.setModule(im);
        }

        hashMap.put("InventoryDocument", document);
        hashMap.put("InventoryDocumentMapping", inventoryDocumentMap);
        KwlReturnObject result = instance.saveInventoryDocuments(hashMap);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testSaveInventoryDocuments");
        }
    }

    /**
     * Test of getInventoryDocuments method, of class InvDocumentService.
     */
    @Test
    public void testGetInventoryDocuments() throws Exception {
        System.out.println("getInventoryDocuments");
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("modulewiseid", JUnitConstants.MODULE_WISE_ID);
        hashMap.put("modulename", "QA_INSPECTION_APPROVAL");
        hashMap.put("companyid", JUnitConstants.COMPANY_ID);
        hashMap.put("start", "0");
        hashMap.put("limit", "30");
        KwlReturnObject result = instance.getInventoryDocuments(hashMap);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetInventoryDocuments");
        }
    }

    /**
     * Test of deleteInventoryDocument method, of class InvDocumentService.
     */
    @Test
    public void testDeleteInventoryDocument() throws Exception {
        System.out.println("deleteInventoryDocument");
        String docID = "";
        KwlReturnObject result = instance.deleteInventoryDocument(docID);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testDeleteInventoryDocument");
        }
    }
}
