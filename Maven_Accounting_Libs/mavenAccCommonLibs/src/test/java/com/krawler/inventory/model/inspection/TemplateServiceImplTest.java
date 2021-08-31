/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.inspection;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.JUnitConstants;
import com.krawler.common.util.Paging;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import java.util.List;
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
public class TemplateServiceImplTest {

    @Autowired
    private TemplateService instance;
    @Autowired
    private AccountingHandlerDAO accountingHandlerDAOobj;
    @Autowired
    private HibernateTransactionManager txnManager;
    
    private static String COMPANY_ID = JUnitConstants.COMPANY_ID;
    private static String USER_ID = JUnitConstants.USER_ID;
    private static String TEMPLATE_ID = JUnitConstants.TEMPLATE_ID;
    private static String AREA_ID =  JUnitConstants.AREA_ID;
    
    TransactionStatus status = null;
    Company company = null;
    User user = null;

    public TemplateServiceImplTest() {
    }

    @Before
    public void setUp() throws ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("AccScriptServicePropogation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        status = txnManager.getTransaction(def);

        KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), COMPANY_ID);
        company = (Company) companyResult.getEntityList().get(0);
        KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), USER_ID);
        user = (User) userResult.getEntityList().get(0);
    }

    @After
    public void tearDown() {
        instance = null;
        txnManager.rollback(status);
    }

    /**
     * Test of getInspectionTemplate method, of class TemplateService.
     */
    @Test
    public void testGetInspectionTemplate() throws Exception {
        System.out.println("getInspectionTemplate");
        InspectionTemplate result = instance.getInspectionTemplate(TEMPLATE_ID);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetInspectionTemplate");
        }
    }

    /**
     * Test of getInspectionTemplateByName method, of class TemplateService.
     */
    @Test
    public void testGetInspectionTemplateByName() throws Exception {
        System.out.println("getInspectionTemplateByName");
        String templateName = "Inspect sales";
        InspectionTemplate result = instance.getInspectionTemplateByName(company, templateName);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetInspectionTemplateByName");
        }
    }

    /**
     * Test of getInspectionTemplateList method, of class TemplateService.
     */
    @Test
    public void testGetInspectionTemplateList() throws Exception {
        System.out.println("getInspectionTemplateList");
        String searchString = "";
        Paging paging = null;
        List result = instance.getInspectionTemplateList(company, searchString, paging);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetInspectionTemplateList");
        }
    }

    /**
     * Test of getInspectionArea method, of class TemplateService.
     */
    @Test
    public void testGetInspectionArea() throws Exception {
        System.out.println("getInspectionArea");
        InspectionArea result = instance.getInspectionArea(AREA_ID);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetInspectionArea");
        }
    }

    /**
     * Test of getInspectionAreaByName method, of class TemplateService.
     */
    @Test
    public void testGetInspectionAreaByName() throws Exception {
        System.out.println("getInspectionAreaByName");
        InspectionTemplate iTemplate = instance.getInspectionTemplate(TEMPLATE_ID);
        String areaName = "Test Area";
        InspectionArea result = instance.getInspectionAreaByName(iTemplate, areaName);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetInspectionAreaByName");
        }
    }

    /**
     * Test of getInspectionAreaList method, of class TemplateService.
     */
    @Test
    public void testGetInspectionAreaList() throws Exception {
        System.out.println("getInspectionAreaList");
        InspectionTemplate inspectionTemplate = instance.getInspectionTemplate(TEMPLATE_ID);
        String searchString = "";
        Paging paging = null;
        List result = instance.getInspectionAreaList(inspectionTemplate, searchString, paging);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetInspectionAreaList");
        }
    }

    /**
     * Test of deleteInspectionTemplate method, of class TemplateService.
     */
    @Test
    public void testDeleteInspectionTemplate() throws Exception {
        System.out.println("deleteInspectionTemplate");

        UUID uuid = UUID.randomUUID();
        InspectionTemplate iTemplate = new InspectionTemplate();
        iTemplate.setCompany(company);
        iTemplate.setDescription("Test Description");
        iTemplate.setName("Test Name");
        iTemplate.setId(uuid.toString());
        instance.addTemplate(iTemplate);
        
        instance.deleteInspectionTemplate(iTemplate);
        assertTrue("Data is returned properly", true);

    }

    /**
     * Test of deleteInspectionArea method, of class TemplateService.
     */
    @Test
    public void testDeleteInspectionArea() throws Exception {
        System.out.println("deleteInspectionArea");

        UUID uuid = UUID.randomUUID();
        InspectionTemplate iTemplate = new InspectionTemplate();
        iTemplate.setCompany(company);
        iTemplate.setDescription("Test Description");
        iTemplate.setName("Test Name");
        iTemplate.setId(uuid.toString());
        
        UUID uuid1 = UUID.randomUUID();
        InspectionArea iArea = new InspectionArea();
        iArea.setId(uuid1.toString());
        iArea.setName("Test Area");
        iArea.setInspectionTemplate(iTemplate);
        iArea.setFaults("Test Faults");
        instance.addInspectionArea(iArea);
        
        instance.deleteInspectionArea(iArea);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of addTemplate method, of class TemplateService.
     */
    @Test
    public void testAddTemplate() throws Exception {
        System.out.println("addTemplate");
        UUID uuid = UUID.randomUUID();
        InspectionTemplate iTemplate = new InspectionTemplate();
        iTemplate.setCompany(company);
        iTemplate.setDescription("Test Description");
        iTemplate.setName("Test Name");
        iTemplate.setId(uuid.toString());
        instance.addTemplate(iTemplate);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of updateTemplate method, of class TemplateService.
     */
    @Test
    public void testUpdateTemplate() throws Exception {
        System.out.println("updateTemplate");
        UUID uuid = UUID.randomUUID();
        InspectionTemplate iTemplate = new InspectionTemplate();
        iTemplate.setCompany(company);
        iTemplate.setDescription("Test Description");
        iTemplate.setName("Test Name");
        iTemplate.setId(uuid.toString());
        instance.updateTemplate(iTemplate);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of updateInspectionArea method, of class TemplateService.
     */
    @Test
    public void testUpdateInspectionArea() throws Exception {
        System.out.println("updateInspectionArea");
  
        UUID uuid = UUID.randomUUID();
        InspectionTemplate iTemplate = new InspectionTemplate();
        iTemplate.setCompany(company);
        iTemplate.setDescription("Test Description");
        iTemplate.setName("Test Name");
        iTemplate.setId(uuid.toString());
        
        UUID uuid1 = UUID.randomUUID();
        InspectionArea iArea = new InspectionArea();
        iArea.setId(uuid1.toString());
        iArea.setName("Test Area");
        iArea.setInspectionTemplate(iTemplate);
        iArea.setFaults("Test Faults");
        instance.updateInspectionArea(iArea);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of addInspectionArea method, of class TemplateService.
     */
    @Test
    public void testAddInspectionArea() throws Exception {
        System.out.println("addInspectionArea");
        
        UUID uuid = UUID.randomUUID();
        InspectionTemplate iTemplate = new InspectionTemplate();
        iTemplate.setCompany(company);
        iTemplate.setDescription("Test Description");
        iTemplate.setName("Test Name");
        iTemplate.setId(uuid.toString());
        
        UUID uuid1 = UUID.randomUUID();
        InspectionArea iArea = new InspectionArea();
        iArea.setId(uuid1.toString());
        iArea.setName("Test Area");
        iArea.setInspectionTemplate(iTemplate);
        iArea.setFaults("Test Faults");
        instance.addInspectionArea(iArea);
        assertTrue("Data is returned properly", true);
    }
}
