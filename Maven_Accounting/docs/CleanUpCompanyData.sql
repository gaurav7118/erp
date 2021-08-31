DROP PROCEDURE IF EXISTS cleanupcompanydata;
delimiter //
CREATE PROCEDURE cleanupcompanydata(fromdb varchar(50),dmn varchar(50))
BEGIN
SET @x_subdomain = dmn;

SET foreign_key_checks = 0;

    IF fromdb<>'' THEN
       SET fromdb = concat(fromdb,'.');
    END IF;

SET @sql_text = concat('SELECT DISTINCT companyid into @x_companyid from ',fromdb,'company where subdomain =?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_subdomain;DEALLOCATE PREPARE stmt;
	
--         SET @sql_text = concat('DELETE FROM ',fromdb,'.croneschedule');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        # For India Compliance
        # No company column but used as foreign key.



    SET @sql_text = concat('DELETE FROM ',fromdb,'landingcostdetailmapping WHERE expenseinvoiceid IN ( SELECT id FROM ',fromdb,'goodsreceipt WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'receiptadvancedetailstermmap where entityterm in (select id from entitybasedlineleveltermsrate where linelevelterms in (select id from linelevelterms where company = ?))');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'receiptdetails  WHERE receipt IN ( SELECT id FROM ',fromdb,'receipt WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'receiptlinking  WHERE docid IN ( SELECT id FROM ',fromdb,'receipt WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'tdsdetails  WHERE advancedetail IN ( SELECT id FROM ',fromdb,'advancedetail WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'advancedetail  WHERE payment IN ( SELECT id FROM ',fromdb,'payment WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'receiptdetails  WHERE receipt IN ( SELECT id FROM ',fromdb,'receipt WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'receipt  WHERE deposittojedetail IN ( SELECT id FROM ',fromdb,'jedetail WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
       
        SET @sql_text = concat('DELETE FROM ',fromdb,'jedetail  WHERE journalEntry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ? and transactionModuleid=16)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

        SET @sql_text = concat('DELETE FROM ',fromdb,'receiptadvancedetail  WHERE receipt IN ( SELECT id FROM ',fromdb,'receipt WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

        SET @sql_text = concat('DELETE FROM ',fromdb,'receiptdetailotherwise  WHERE receipt IN ( SELECT id FROM ',fromdb,'receipt WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'payment  WHERE deposittojedetail IN ( SELECT id FROM ',fromdb,'jedetail WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'bankreconciliationdetail  WHERE journalEntry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ? and transactionModuleid=16)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'bankunreconciliationdetail  WHERE journalEntry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ? and transactionModuleid=16)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'journalentry  WHERE company = ? and transactionModuleid=16');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'receipt  WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
 
#-------------------------------------------------------------Receive Payment---------------------------------------------------------

        SET @sql_text = concat('DELETE FROM ',fromdb,'invoicelinking  WHERE docid IN ( SELECT id FROM ',fromdb,'invoice WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'srdetails  WHERE dodetails IN ( SELECT id FROM ',fromdb,'dodetails WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'dodetails  WHERE cidetails IN ( SELECT id FROM ',fromdb,'invoicedetails WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'srdetails  WHERE cidetails IN ( SELECT id FROM ',fromdb,'invoicedetails WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'invoicedetails  WHERE invoice IN ( SELECT id FROM ',fromdb,'invoice WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'invoicelinking  WHERE docid IN ( SELECT id FROM ',fromdb,'invoice WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'linkdetailreceipt  WHERE invoice IN ( SELECT id FROM ',fromdb,'invoice WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'deliveryplanner  WHERE referencenumber IN ( SELECT id FROM ',fromdb,'invoice WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'invoice  WHERE parentinvoice  is not null and company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'invoice  WHERE  company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'jedetail  WHERE journalEntry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ? and transactionModuleid=2)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'prdetails  WHERE grdetails IN ( SELECT id FROM ',fromdb,'grodetails WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'grodetails  WHERE videtails IN ( SELECT id FROM ',fromdb,'grdetails WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'prdetails  WHERE videtails IN ( SELECT id FROM ',fromdb,'grdetails WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'grdetails  WHERE goodsreceipt IN ( SELECT id FROM ',fromdb,'goodsreceipt WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'tdsjemapping where company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;        
        SET @sql_text = concat('DELETE FROM ',fromdb,'paymentdetail  WHERE goodsReceipt IN ( SELECT id FROM ',fromdb,'goodsreceipt WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'goodsreceipt  WHERE centry IN ( SELECT id FROM ',fromdb,'jedetail WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'jedetail  WHERE journalEntry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ?  and transactionModuleid=2)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
        SET @sql_text = concat('DELETE FROM ',fromdb,'journalentry  WHERE company = ?  and transactionModuleid=2');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'receiptinvoicejemapping where company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;


#---------------------------------------- Sales Invoice ---------------------

      SET @sql_text = concat('DELETE FROM ',fromdb,'paymentdetail  WHERE goodsReceipt IN ( SELECT id FROM ',fromdb,'goodsreceipt WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

      SET @sql_text = concat('DELETE FROM ',fromdb,'expenseggrdetails  WHERE goodsreceipt IN ( SELECT id FROM ',fromdb,'goodsreceipt WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
      SET @sql_text = concat('DELETE FROM ',fromdb,'grodetails  WHERE videtails IN ( SELECT id FROM ',fromdb,'grdetails  WHERE goodsreceipt IN ( SELECT id FROM ',fromdb,'goodsreceipt WHERE company = ?))');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
      SET @sql_text = concat('DELETE FROM ',fromdb,'grdetails  WHERE goodsreceipt IN ( SELECT id FROM ',fromdb,'goodsreceipt WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
     SET @sql_text = concat('DELETE FROM ',fromdb,'linkdetailpayment  WHERE goodsReceipt IN ( SELECT id FROM ',fromdb,'goodsreceipt WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

     SET @sql_text = concat('DELETE FROM ',fromdb,'goodsreceipt  WHERE parentinvoice is not null and  company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;      
     SET @sql_text = concat('DELETE FROM ',fromdb,'goodsreceipt  WHERE company = ? ');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
      SET @sql_text = concat('DELETE FROM ',fromdb,'jedetail  WHERE journalEntry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ? and transactionModuleid=6)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
      SET @sql_text = concat('DELETE FROM ',fromdb,'journalentry  WHERE company = ?  and transactionModuleid=6');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

#--------------------------------------------Vendor Invoice-------------------------------------------------------------------
    SET @sql_text = concat('DELETE FROM ',fromdb,'purchaseinvoiceurd_jedetail WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'paymentdetailotherwise  WHERE payment IN ( SELECT id FROM ',fromdb,'payment WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'paymentlinking  WHERE docid IN ( SELECT id FROM ',fromdb,'payment WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'advancedetailtermmap where advanceDetail in (select id from ',fromdb,'advancedetail where payment in (select id from ',fromdb,'payment where company = ?))');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'advancedetail  WHERE payment IN ( SELECT id FROM ',fromdb,'payment WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'goodsreceiptdetailpaymentmapping where company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'payment  WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'bankreconciliationdetail  WHERE journalEntry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ? and transactionModuleid=14)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'jedetail  WHERE journalEntry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ? and transactionModuleid=14)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'bankunreconciliationdetail  WHERE journalEntry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ? and transactionModuleid=14)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'journalentry  WHERE company = ?  and transactionModuleid=14');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;


#-------------------------------------------Payment---------------------------------------------------------------------------------
    SET @sql_text = concat('DELETE FROM ',fromdb,'jedetail  WHERE journalEntry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ? and transactionModuleid=8)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'bankreconciliationdetail  WHERE journalEntry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ? and transactionModuleid=8)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'journalentry  WHERE company = ?  and transactionModuleid=8');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

#----------------------------------------------Cash Purcahse------------------------------------------------------------------------
    SET @sql_text = concat('DELETE FROM ',fromdb,'jedetail  WHERE journalEntry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ? and transactionModuleid=6)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'bankreconciliationdetail  WHERE journalEntry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ? and transactionModuleid=6)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'journalentry  WHERE company = ?  and transactionModuleid=6');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;


#-----------------------------------------------------Cash Sales-------------------------------------------------------------------
    SET @sql_text = concat('DELETE FROM ',fromdb,'debitnotedetailtermmap where debitnotetaxentry in (select id from dntaxentry where debitnote in (select id from debitnote where company = ?))');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'debitnoteinvoicemappinginfo where debitnote in (select id from debitnote  where company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'dndetailsgst where company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'dndetails  WHERE debitNote IN ( SELECT id FROM ',fromdb,'debitnote WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'debitnote  WHERE purchasereturn IN ( SELECT id FROM ',fromdb,'purchasereturn WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'debitnote  WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'jedetail  WHERE journalEntry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ? and transactionModuleid=10)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   
   SET @sql_text = concat('DELETE FROM ',fromdb,'journalentry  WHERE company = ?  and transactionModuleid=10');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   
#--------------------------------------------------Debit Note-----------------------------------------------------------------
   SET @sql_text = concat('DELETE FROM ',fromdb,'cndetails  WHERE creditNote IN ( SELECT id FROM ',fromdb,'creditnote WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'creditnotelinking  WHERE docid IN ( SELECT id FROM ',fromdb,'creditnote WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'jedetail  WHERE journalEntry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ? and transactionModuleid=12)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'creditnotedetailtermmap where creditnotetaxentry in (select id from cntaxentry where creditnote in (select id from creditnote where company = ?))');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'creditnoteinvoicemappinginfo where creditnote in (select id from creditnote  where company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'creditnote  WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'journalentry  WHERE company = ?  and transactionModuleid=12');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   
   SET @sql_text = concat('DELETE FROM ',fromdb,'grodetails  WHERE grorder IN ( SELECT id FROM ',fromdb,'grorder WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'grorder  WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   



   SET @sql_text = concat('DELETE FROM ',fromdb,'cndetails  WHERE creditNote IN ( SELECT id FROM ',fromdb,'creditnote WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'docontractmapping  WHERE salesorder IN ( SELECT id FROM ',fromdb,'salesorder WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'dodetails  WHERE sodetails IN ( SELECT id FROM ',fromdb,'sodetails WHERE company = ? )');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'sodetails  WHERE salesorder IN ( SELECT id FROM ',fromdb,'salesorder WHERE company = ? )');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'solinking  WHERE docid IN ( SELECT id FROM ',fromdb,'salesorder WHERE company = ? )');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'salesorder  WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   
#----------------------------------------------------------Sales order--------------------------------------------------
   SET @sql_text = concat('DELETE FROM ',fromdb,'docontractmapping  WHERE deliveryorder IN ( SELECT id FROM ',fromdb,'deliveryorder WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'itemdetail  WHERE packingdetails IN ( SELECT id FROM ',fromdb,'packingdetails WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'packingdodetails  WHERE packingdetails is not null and company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'packingdetails  WHERE dodetailid IN ( SELECT id FROM ',fromdb,'dodetails WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'shippingdeliverydetails  WHERE dod IN ( SELECT id FROM ',fromdb,'dodetails WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'dodetails  WHERE deliveryorder IN ( SELECT id FROM ',fromdb,'deliveryorder WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
  SET @sql_text = concat('DELETE FROM ',fromdb,'dolinking  WHERE docid IN ( SELECT id FROM ',fromdb,'deliveryorder WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'deliveryorder  WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   

#----------------------------------------------------------------Purchase Order-----------------------------------------
    SET @sql_text = concat('DELETE FROM ',fromdb,'purchaseorderversiondetailstermmap where povdetails IN (select id FROM ',fromdb,'poversiondetails where company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'purchaseorderversiontermmap where purchaseorderversion IN (select id FROM ',fromdb,'purchaseorderversion where company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'purchaseorderversiondetailcustomdata where company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'purchaseorderversion where company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;    
    SET @sql_text = concat('DELETE FROM ',fromdb,'purchaseorderversioncustomdata where company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'poversiondetails where company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;


    SET @sql_text = concat('DELETE FROM ',fromdb,'expensepoversiondetailcustomdata where company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'expensepoversiondetails where company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
   SET @sql_text = concat('DELETE FROM ',fromdb,'podetails  WHERE purchaseorder IN ( SELECT id FROM ',fromdb,'purchaseorder WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
  SET @sql_text = concat('DELETE FROM ',fromdb,'securitygatedetails  WHERE podetail IN ( SELECT id FROM ',fromdb,'podetails WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
 SET @sql_text = concat('DELETE FROM ',fromdb,'expensepodetails  WHERE purchaseorder IN ( SELECT id FROM ',fromdb,'purchaseorder WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
 SET @sql_text = concat('DELETE FROM ',fromdb,'purchaseorder  WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
#----------------------------------------------------------------Customer Quotation-----------------------------------------  
 SET @sql_text = concat('DELETE FROM ',fromdb,'quotationdetails  WHERE quotation IN ( SELECT id FROM ',fromdb,'quotation WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
 SET @sql_text = concat('DELETE FROM ',fromdb,'cqlinking  WHERE docid IN ( SELECT id FROM ',fromdb,'quotation WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
 SET @sql_text = concat('DELETE FROM ',fromdb,'quotationversiondetails  WHERE quotationversion IN ( SELECT id FROM ',fromdb,'quotationversion WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
 SET @sql_text = concat('DELETE FROM ',fromdb,'quotationversion  WHERE quotation IN ( SELECT id FROM ',fromdb,'quotation WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
 SET @sql_text = concat('DELETE FROM ',fromdb,'quotation  WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

#---------------------------------------------------------------------------Vendor Quotation----------------------------------------------
 SET @sql_text = concat('DELETE FROM ',fromdb,'vendorquotationdetails  WHERE vendorquotation IN ( SELECT id FROM ',fromdb,'vendorquotation WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
 SET @sql_text = concat('DELETE FROM ',fromdb,'vqlinking  WHERE docid IN ( SELECT id FROM ',fromdb,'vendorquotation WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
 SET @sql_text = concat('DELETE FROM ',fromdb,'vendorquotationversiondetails  WHERE quotationversion IN ( SELECT id FROM ',fromdb,'vendorquotationversion WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
 SET @sql_text = concat('DELETE FROM ',fromdb,'vendorquotationversion  WHERE quotation IN ( SELECT id FROM ',fromdb,'vendorquotation WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
 SET @sql_text = concat('DELETE FROM ',fromdb,'vendorquotation  WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;


#--------------------------------------------------------------------Sales Return------------------------------------------------------------
SET @sql_text = concat('DELETE FROM ',fromdb,'srdetails  WHERE salesreturn IN ( SELECT id FROM ',fromdb,'salesreturn WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
SET @sql_text = concat('DELETE FROM ',fromdb,'salesreturnlinking  WHERE docid IN ( SELECT id FROM ',fromdb,'salesreturn WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
SET @sql_text = concat('DELETE FROM ',fromdb,'salesreturn  WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

#-------------------------------------------------------------------Purchase Return---------------------------------------------------
SET @sql_text = concat('DELETE FROM ',fromdb,'prdetails  WHERE purchasereturn IN ( SELECT id FROM ',fromdb,'purchasereturn WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
SET @sql_text = concat('DELETE FROM ',fromdb,'purchasereturnlinking  WHERE docid IN ( SELECT id FROM ',fromdb,'purchasereturn WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
SET @sql_text = concat('DELETE FROM ',fromdb,'purchasereturn  WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

#--------------------------------------------------------------------Purchase Requisition--------------------
SET @sql_text = concat('DELETE FROM ',fromdb,'purchaserequisitiondetail  WHERE purchaserequisition IN ( SELECT id FROM ',fromdb,'purchaserequisition WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
SET @sql_text = concat('DELETE FROM ',fromdb,'purchaserequisitionlinking  WHERE docid IN ( SELECT id FROM ',fromdb,'purchaserequisition WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
SET @sql_text = concat('DELETE FROM ',fromdb,'purchaserequisition  WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;


#-----------------------------------------------------------------------------------------------------------

SET @sql_text = concat('DELETE FROM ',fromdb,'serialdocumentmapping  WHERE serialid IN ( SELECT id FROM ',fromdb,'newbatchserial WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

#----------------------------------------------------------New Queries------------------------------------------------------------------------

SET @sql_text = concat('DELETE FROM ',fromdb,'assetdepreciationdetail  WHERE journalentry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
SET @sql_text = concat('DELETE FROM ',fromdb,'in_sm_detail WHERE  stockmovement IN ( SELECT id FROM ',fromdb,'in_stockmovement  WHERE product IN ( SELECT id FROM ',fromdb,'product WHERE company = ?))');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

SET @sql_text = concat('DELETE FROM ',fromdb,'in_stockmovement  WHERE product IN ( SELECT id FROM ',fromdb,'product WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
SET @sql_text = concat('DELETE FROM ',fromdb,'in_stock  WHERE product IN ( SELECT id FROM ',fromdb,'product WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;


SET @sql_text = concat('DELETE FROM ',fromdb,'itemdetail WHERE  packingdetails IN ( SELECT id FROM ',fromdb,'packingdetails  WHERE dodetailid IN  ( SELECT id FROM ',fromdb,'dodetails WHERE product IN ( SELECT id FROM ',fromdb,'product WHERE company = ?)))');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
SET @sql_text = concat('DELETE FROM ',fromdb,'packingdetails WHERE  dodetailid IN ( SELECT id FROM ',fromdb,'dodetails  WHERE product IN ( SELECT id FROM ',fromdb,'product WHERE company = ?))');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
SET @sql_text = concat('DELETE FROM ',fromdb,'dodetails  WHERE product IN ( SELECT id FROM ',fromdb,'product WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;


SET @sql_text = concat('DELETE FROM ',fromdb,'deliveryorder  WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;


SET @sql_text = concat('DELETE FROM ',fromdb,'inventory  WHERE product IN ( SELECT id FROM ',fromdb,'product WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
SET @sql_text = concat('DELETE FROM ',fromdb,'locationbatchdocumentmapping WHERE  batchmapid IN ( SELECT id FROM ',fromdb,'newproductbatch  WHERE product IN ( SELECT id FROM ',fromdb,'product WHERE company = ?))');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
SET @sql_text = concat('DELETE FROM ',fromdb,'serialdocumentmapping WHERE  serialid IN ( SELECT id FROM ',fromdb,'newbatchserial  WHERE product IN ( SELECT id FROM ',fromdb,'product WHERE company = ?))');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
SET @sql_text = concat('DELETE FROM ',fromdb,'newbatchserial  WHERE product IN ( SELECT id FROM ',fromdb,'product WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
SET @sql_text = concat('DELETE FROM ',fromdb,'newproductbatch  WHERE product IN ( SELECT id FROM ',fromdb,'product WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

SET @sql_text = concat('DELETE FROM ',fromdb,'in_ist_detail WHERE  istrequest IN ( SELECT id FROM ',fromdb,'in_interstoretransfer  WHERE product IN ( SELECT id FROM ',fromdb,'product WHERE company = ?))');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
SET @sql_text = concat('DELETE FROM ',fromdb,'dodistmapping WHERE  ist IN ( SELECT id FROM ',fromdb,'in_interstoretransfer WHERE product IN ( SELECT id FROM ',fromdb,'product WHERE company = ?))');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
SET @sql_text = concat('DELETE FROM ',fromdb,'in_interstoretransfer  WHERE product IN ( SELECT id FROM ',fromdb,'product WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;



SET @sql_text = concat('DELETE FROM ',fromdb,'jedetail WHERE  journalentry IN ( SELECT id FROM ',fromdb,'journalentry  WHERE id IN  ( SELECT inventoryje FROM ',fromdb,'in_stockadjustment WHERE product IN ( SELECT id FROM ',fromdb,'product WHERE company = ?)))');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

SET @sql_text = concat('DELETE FROM ',fromdb,'in_sa_detail WHERE  stockadjustment IN ( SELECT id FROM ',fromdb,'in_stockadjustment  WHERE product IN ( SELECT id FROM ',fromdb,'product WHERE company = ?))');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

SET @sql_text = concat('DELETE FROM ',fromdb,'in_stockadjustment  WHERE product IN ( SELECT id FROM ',fromdb,'product WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

SET @sql_text = concat('DELETE FROM ',fromdb,'journalentry  WHERE id IN ( SELECT inventoryje FROM ',fromdb,'in_stockadjustment WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

SET @sql_text = concat('update ',fromdb,'product set availablequantity =0  where company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
       





SET @sql_text = concat('DELETE FROM ',fromdb,'productbuild  WHERE journalentry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
SET @sql_text = concat('DELETE FROM ',fromdb,'jedetail  WHERE journalEntry IN ( SELECT id FROM ',fromdb,'journalentry WHERE company = ?)');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

SET @sql_text = concat('DELETE FROM ',fromdb,'pricelist  WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

    SET @sql_text = concat('DELETE FROM ',fromdb,'productdiscountmapping where companyid = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'discountmaster where company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'multientitydimesioncustomdata where fcdid in (select id from fieldcombodata where fieldid in (select id from fieldparams where companyid = ?))');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'importfilecolumnmapping WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;
    SET @sql_text = concat('DELETE FROM ',fromdb,'importfiledetails WHERE company = ?');PREPARE stmt FROM @sql_text;EXECUTE stmt using @x_companyid;DEALLOCATE PREPARE stmt;

SET foreign_key_checks = 1;
SELECT 1; # Please don't delete this line. It is used for deletecompany() method in remoteapi.java to execute Native SQL query.
END//
delimiter ;
