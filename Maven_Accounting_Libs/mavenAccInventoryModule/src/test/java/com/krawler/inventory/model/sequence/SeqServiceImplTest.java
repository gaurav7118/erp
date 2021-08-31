/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.sequence;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.JUnitConstants;
import com.krawler.common.util.Paging;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import java.util.*;
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
public class SeqServiceImplTest {

    public SeqServiceImplTest() {
    }
    @Autowired
    private SeqService instance;
    @Autowired
    private AccountingHandlerDAO accountingHandlerDAOobj;
    @Autowired
    private HibernateTransactionManager txnManager;
    
    TransactionStatus status = null;
    Company company = null;
    User user = null;
    SeqFormat seqFormat = null;
    
    private static final String COMPANY_ID = JUnitConstants.COMPANY_ID;
    private static final String USER_ID = JUnitConstants.COMPANY_ID;
    private static final String SEQUENCE_FORMAT_ID = JUnitConstants.SEQUENCE_FORMAT_ID;
    private static final int MODULE_ID = 2;

    @Before
    public void setUp() throws ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("AccCustomReportServicePropogation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        status = txnManager.getTransaction(def);

        KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), COMPANY_ID);
        company = (Company) companyResult.getEntityList().get(0);

        KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), USER_ID);
        user = (User) userResult.getEntityList().get(0);

        KwlReturnObject sfResult = accountingHandlerDAOobj.getObject(SeqFormat.class.getName(), "4028809554289d5a01542901dd880204");
        seqFormat = (SeqFormat) sfResult.getEntityList().get(0);
    }

    @After
    public void tearDown() {
        instance = null;
        txnManager.rollback(status);
    }

    /**
     * Test of getSeqModule method, of class SeqService.
     */
    @Test
    public void testGetSeqModule_Integer() throws Exception {
        System.out.println("getSeqModule");
        SeqModule result = instance.getSeqModule(MODULE_ID);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetSeqModule_Integer");
        }
    }

    /**
     * Test of getSeqModules method, of class SeqService.
     */
    @Test
    public void testGetSeqModules_4args() throws Exception {
        System.out.println("getSeqModules");
        Boolean isActive = true;
        String searchString = "";
        Paging paging = null;
        List result = instance.getSeqModules(company, isActive, searchString, paging);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetSeqModules_4args");
        }
    }

    /**
     * Test of getSeqModules method, of class SeqService.
     */
    @Test
    public void testGetSeqModules_3args() throws Exception {
        System.out.println("getSeqModules");
        String searchString = "";
        Paging paging = null;
        List result = instance.getSeqModules(company, searchString, paging);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetSeqModules_3args");
        }
    }

    /**
     * Test of getSeqFormat method, of class SeqService.
     */
    @Test
    public void testGetSeqFormat() throws Exception {
        System.out.println("getSeqFormat");
        SeqFormat result = instance.getSeqFormat(SEQUENCE_FORMAT_ID);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetSeqFormat");
        }
    }

    /**
     * Test of checkInvSequenceFormat method, of class SeqService.
     */
    @Test
    public void testCheckInvSequenceFormat() throws Exception {
        System.out.println("checkInvSequenceFormat");
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("companyid", COMPANY_ID);
        requestParams.put("prefix", "SR");
        requestParams.put("prefixDateFormat", "");
        requestParams.put("suffix", "");
        requestParams.put("suffixDateFormat", "");
        requestParams.put("module", "in_stockadjustment");
        List result = instance.checkInvSequenceFormat(requestParams);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testCheckInvSequenceFormat");
        }
    }

    /**
     * Test of deleteInvSequenceFormatNumber method, of class SeqService.
     */
    @Test
    public void testDeleteInvSequenceFormatNumber() throws Exception {
        System.out.println("deleteInvSequenceFormatNumber");
        String id = "40288095541e1cd4015428528186077d";
        String result = instance.deleteInvSequenceFormatNumber(id);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testDeleteInvSequenceFormatNumber");
        }
    }

    /**
     * Test of getDefaultSeqFormat method, of class SeqService.
     */
    @Test
    public void testGetDefaultSeqFormat_Company_SeqModule() throws Exception {
        System.out.println("getDefaultSeqFormat");
        SeqModule seqModule = instance.getSeqModule(MODULE_ID);
        SeqFormat result = instance.getDefaultSeqFormat(company, seqModule);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetDefaultSeqFormat_Company_SeqModule");
        }
    }

    /**
     * Test of getDefaultSeqFormat method, of class SeqService.
     */
    @Test
    public void testGetDefaultSeqFormat_Company_ModuleConst() throws Exception {
        System.out.println("getDefaultSeqFormat");
        SeqFormat result = null;
        for (ModuleConst mc : ModuleConst.values()) {
            result = instance.getDefaultSeqFormat(company, mc);
        }
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetDefaultSeqFormat_Company_ModuleConst");
        }
    }

    /**
     * Test of getActiveSeqFormats method, of class SeqService.
     */
    @Test
    public void testGetActiveSeqFormats() throws Exception {
        System.out.println("getActiveSeqFormats");

        SeqModule seqModule = instance.getSeqModule(MODULE_ID);
        String searchString = "";
        Paging paging = null;
        List result = instance.getActiveSeqFormats(company, seqModule, searchString, paging);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetActiveSeqFormats");
        }
    }

    /**
     * Test of getSeqFormats method, of class SeqService.
     */
    @Test
    public void testGetSeqFormats() throws Exception {
        System.out.println("getSeqFormats");

        SeqModule seqModule = instance.getSeqModule(MODULE_ID);
        String searchString = "";
        Paging paging = null;

        List result = instance.getSeqFormats(company, seqModule, searchString, paging);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetSeqFormats");
        }
    }

    /**
     * Test of addSeqFormat method, of class SeqService.
     */
    @Test
    public void testAddSeqFormat() throws Exception {
        System.out.println("addSeqFormat");
        String uuid = UUID.randomUUID().toString();
        SeqFormat sf = new SeqFormat();
        sf.setId(uuid);
        sf.setActive(true);
        sf.setCompany(company);
        sf.setDefaultFormat(false);
        sf.setNumberOfDigits(4);
        sf.setPrefix(seqFormat.getPrefix());
        sf.setPrefixDateFormat(seqFormat.getPrefixDateFormat());
        sf.setSeparator(seqFormat.getSeparator());
        sf.setSeqModule(seqFormat.getSeqModule());
        sf.setStartFrom(seqFormat.getStartFrom());
        sf.setSuffix(seqFormat.getSuffix());
        sf.setSuffixDateFormat(seqFormat.getSuffixDateFormat());
        instance.addSeqFormat(user, sf);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of setSeqFormatAsDefault method, of class SeqService.
     */
    @Test
    public void testSetSeqFormatAsDefault() throws Exception {
        System.out.println("setSeqFormatAsDefault");
        instance.setSeqFormatAsDefault(user, seqFormat);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of deactivateSeqFormat method, of class SeqService.
     */
    @Test
    public void testDeactivateSeqFormat() throws Exception {
        System.out.println("deactivateSeqFormat");
        instance.deactivateSeqFormat(user, seqFormat);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of activateSeqFormat method, of class SeqService.
     */
    @Test
    public void testActivateSeqFormat() throws Exception {
        System.out.println("activateSeqFormat");
        instance.activateSeqFormat(user, seqFormat);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of getLastUsedSeqNumber method, of class SeqService.
     */
    @Test
    public void testGetLastUsedSeqNumber() throws Exception {
        System.out.println("getLastUsedSeqNumber");
        long result = instance.getLastUsedSeqNumber(seqFormat);
        if (result > -1) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetLastUsedSeqNumber");
        }
    }

    /**
     * Test of getNextSeqNumber method, of class SeqService.
     */
    @Test
    public void testGetNextSeqNumber() throws Exception {
        System.out.println("getNextSeqNumber");
        long result = instance.getNextSeqNumber(seqFormat);
        if (result > -1) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetNextSeqNumber");
        }
    }

    /**
     * Test of getNextFormatedSeqNumber method, of class SeqService.
     */
    @Test
    public void testGetNextFormatedSeqNumber() throws Exception {
        System.out.println("getNextFormatedSeqNumber");
        String result = instance.getNextFormatedSeqNumber(seqFormat);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetNextFormatedSeqNumber");
        }
    }

    /**
     * Test of isExistingSeqNumber method, of class SeqService.
     */
    @Test
    public void testIsExistingSeqNumber() throws Exception {
        System.out.println("isExistingSeqNumber");
        String seqNo = "";
        boolean result = false;
        for (ModuleConst mc : ModuleConst.values()) {
            result = instance.isExistingSeqNumber(seqNo, company, mc);
        }
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of updateSeqNumber method, of class SeqService.
     */
    @Test
    public void testUpdateSeqNumber() throws Exception {
        System.out.println("updateSeqNumber");
        instance.updateSeqNumber(seqFormat);
        assertTrue("Data is returned properly", true);
    }
}
