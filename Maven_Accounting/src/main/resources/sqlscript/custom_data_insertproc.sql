DROP PROCEDURE if exists insert_custom_data;
delimiter //
CREATE PROCEDURE insert_custom_data (IN accidparam varchar(255), IN companyidparam varchar(60), IN usersessionidparam varchar(60), IN moduleidparam smallint(6), IN startdate varchar(20), IN enddate varchar (20), IN delimiter varchar(10), IN iscustomfield int(1))
BEGIN
 DECLARE v_finished INTEGER DEFAULT 0;
DECLARE v_fl varchar(100) DEFAULT "";
DECLARE v_cn varchar(100) DEFAULT "";
DECLARE v_cn_full varchar(20000) DEFAULT "";
DECLARE v_colvalue varchar(100) DEFAULT "";
DECLARE v_data text DEFAULT "";
DECLARE v_condition varchar (100) DEFAULT "";
DEClARE cd_cursor CURSOR FOR select fieldlabel, colnum from fieldparams where companyid=companyidparam and moduleid=moduleidparam and (fieldtype <> '4' and fieldtype <> '7' and fieldtype <> '12');
DEClARE cd_cursor_customfield CURSOR FOR select fieldlabel, colnum from fieldparams where companyid=companyidparam and moduleid=moduleidparam and (fieldtype <> '4' and fieldtype <> '7' and fieldtype <> '12') and customfield=1;
DEClARE cd_cursor_dimension CURSOR FOR select fieldlabel, colnum from fieldparams where companyid=companyidparam and moduleid=moduleidparam and (fieldtype <> '4' and fieldtype <> '7' and fieldtype <> '12') and customfield=0;
 -- declare NOT FOUND handler
DECLARE CONTINUE HANDLER
        FOR NOT FOUND SET v_finished = 1;


IF iscustomfield = 2 THEN
OPEN cd_cursor;
 
get_cd: LOOP
 
FETCH cd_cursor INTO v_fl,v_cn;
 
 IF v_finished = 1 THEN
 LEAVE get_cd;
 END IF;

SET v_cn_full=CONCAT("CONCAT('",v_fl, delimiter,"',if(col",v_cn," = '',null,col",v_cn,")),",v_cn_full);
select v_cn_full;
 
 END LOOP get_cd;
 
 CLOSE cd_cursor;
END IF;


IF iscustomfield = 1 THEN
OPEN cd_cursor_customfield;
 
get_cd: LOOP
 
FETCH cd_cursor_customfield INTO v_fl,v_cn;
 
 IF v_finished = 1 THEN
 LEAVE get_cd;
 END IF;

SET v_cn_full=CONCAT("CONCAT('",v_fl, delimiter,"',if(col",v_cn," = '',null,col",v_cn,")),",v_cn_full);
select v_cn_full;
 
 END LOOP get_cd;
 
 CLOSE cd_cursor_customfield;
END IF;


IF iscustomfield = 0 THEN
OPEN cd_cursor_dimension;
 
get_cd: LOOP
 
FETCH cd_cursor_dimension INTO v_fl,v_cn;
 
 IF v_finished = 1 THEN
 LEAVE get_cd;
 END IF;

SET v_cn_full=CONCAT("CONCAT('",v_fl, delimiter,"',if(col",v_cn," = '',null,col",v_cn,")),",v_cn_full);
select v_cn_full;
 
 END LOOP get_cd;
 
 CLOSE cd_cursor_dimension;
END IF;

set v_cn_full=substring(v_cn_full,1,length(v_cn_full)-1);
IF v_cn_full != "" THEN
IF moduleidparam = '2' OR moduleidparam = '4'  THEN
SET @s=CONCAT('INSERT ignore into temp_custom_data ( SELECT distinct invd.id, CONCAT_WS(",",',v_cn_full,') ,"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, invoicedetails invd, jedetail jed, invoice inv, journalentry je where jed.account="',accidparam,'" and invd.id=jedcd.recdetailid and je.id=jed.journalentry and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and invd.invoice=inv.id and je.transactionid=inv.id and trim(CONCAT_WS(",",',v_cn_full,')) <> "" ) ;');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

deallocate prepare stmt1;
END IF;

IF moduleidparam = '8' OR moduleidparam = '6' THEN
SET @s=CONCAT('INSERT ignore into temp_custom_data ( SELECT distinct grd.id, CONCAT_WS(",",',v_cn_full,') ,"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, grdetails grd, jedetail jed, goodsreceipt gr, journalentry je where jed.account="',accidparam,'" and grd.id=jedcd.recdetailid and je.id=jed.journalentry and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and grd.goodsreceipt=gr.id and je.transactionid=gr.id and trim(CONCAT_WS(",",',v_cn_full,')) <> "" ) ;');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data ( SELECT grd.id, CONCAT_WS(",",',v_cn_full,') ,"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, expenseggrdetails grd, jedetail jed, goodsreceipt gr, journalentry je where jed.account="',accidparam,'" and grd.id=jedcd.recdetailid and je.id=jed.journalentry and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and grd.goodsreceipt=gr.id and je.transactionid=gr.id and trim(CONCAT_WS(",",',v_cn_full,')) <> "" ) ;');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

deallocate prepare stmt1;
END IF;

IF moduleidparam = '12' THEN
SET @s=CONCAT('INSERT ignore into temp_custom_data ( SELECT jed.id, CONCAT_WS(",",',v_cn_full,'),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, creditnote cn, journalentry je where jed.account="',accidparam,'" and jed.id=jedcd.jedetailid and je.id=jed.journalentry and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and cn.journalentry=je.id and je.typevalue="2" and trim(CONCAT_WS(",",',v_cn_full,')) <> "" ) ;');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data ( SELECT jed.id, CONCAT_WS(",",',v_cn_full,'),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, creditnote cn, jedetail jed, cntaxentry cnt, journalentry je where jed.account="',accidparam,'" and jed.id=jedcd.jedetailid and je.id=jed.journalentry and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and cnt.creditnote = cn.id and cnt.totaljedid=jed.id and je.typevalue < "2" and trim(CONCAT_WS(",",',v_cn_full,')) <> "" ) ;');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

deallocate prepare stmt1;
END IF;

IF moduleidparam = '10' THEN
SET @s=CONCAT('INSERT ignore into temp_custom_data ( SELECT jed.id, CONCAT_WS(",",',v_cn_full,') ,"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, debitnote dn, journalentry je where jed.account="',accidparam,'" and jed.id=jedcd.jedetailid and je.id=jed.journalentry and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and dn.journalentry=je.id and je.typevalue="2" and trim(CONCAT_WS(",",',v_cn_full,')) <> "" ) ;');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data ( SELECT jed.id, CONCAT_WS(",",',v_cn_full,') ,"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, debitnote dn, jedetail jed, dntaxentry dnt, journalentry je where jed.account="',accidparam,'" and jed.id=jedcd.jedetailid and je.id=jed.journalentry and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and dnt.debitnote = dn.id and dnt.totaljedid=jed.id and je.typevalue < "2" and trim(CONCAT_WS(",",',v_cn_full,')) <> "" ) ;');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

deallocate prepare stmt1;
END IF;


IF moduleidparam = '16' THEN
SET @s=CONCAT('INSERT ignore into temp_custom_data ( SELECT distinct jed.id, CONCAT_WS(",",',v_cn_full,') ,"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, receiptdetails rd, jedetail jed, receipt rt, journalentry je where jed.account="',accidparam,'" and rd.id=jedcd.recdetailid and je.id=jed.journalentry and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and rd.receipt=rt.id and je.transactionid=rt.id and trim(CONCAT_WS(",",',v_cn_full,')) <> "" ) ;');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data ( SELECT distinct jed.id, CONCAT_WS(",",',v_cn_full,') ,"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, receiptadvancedetail rad, jedetail jed, receipt rt, journalentry je where jed.account="',accidparam,'" and rad.id=jedcd.recdetailid and je.id=jed.journalentry and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and rad.receipt=rt.id and je.transactionid=rt.id and trim(CONCAT_WS(",",',v_cn_full,')) <> "" ) ;');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data ( SELECT distinct jed.id, CONCAT_WS(",",',v_cn_full,') ,"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, debitnotepayment dnp, jedetail jed, receipt rt, journalentry je where jed.account="',accidparam,'" and dnp.id=jedcd.recdetailid and je.id=jed.journalentry and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and dnp.receiptid=rt.id and je.transactionid=rt.id and trim(CONCAT_WS(",",',v_cn_full,')) <> "" ) ;');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data ( SELECT distinct jed.id, CONCAT_WS(",",',v_cn_full,') ,"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, receiptdetailotherwise rad, jedetail jed, receipt rt, journalentry je where jed.account="',accidparam,'" and rad.id=jedcd.recdetailid and je.id=jed.journalentry and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and rad.receipt=rt.id and je.transactionid=rt.id and trim(CONCAT_WS(",",',v_cn_full,')) <> "" ) ;');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

deallocate prepare stmt1;
END IF;


IF moduleidparam = '14' THEN
SET @s=CONCAT('INSERT ignore into temp_custom_data ( SELECT distinct jed.id, CONCAT_WS(",",',v_cn_full,') ,"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, paymentdetail pd, jedetail jed, payment pt, journalentry je where jed.account="',accidparam,'" and pd.id=jedcd.recdetailid and je.id=jed.journalentry and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and pd.payment=pt.id and je.transactionid=pt.id and trim(CONCAT_WS(",",',v_cn_full,')) <> "" ) ;');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data ( SELECT distinct jed.id, CONCAT_WS(",",',v_cn_full,') ,"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, advancedetail pad, jedetail jed, payment pt, journalentry je where jed.account="',accidparam,'" and pad.id=jedcd.recdetailid and je.id=jed.journalentry and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and pad.payment=pt.id and je.transactionid=pt.id and trim(CONCAT_WS(",",',v_cn_full,')) <> "" ) ;');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data ( SELECT distinct jed.id, CONCAT_WS(",",',v_cn_full,') ,"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, creditnotpayment cnp, jedetail jed, payment pt, journalentry je where jed.account="',accidparam,'" and cnp.id=jedcd.recdetailid and je.id=jed.journalentry and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and cnp.paymentid=pt.id and je.transactionid=pt.id and trim(CONCAT_WS(",",',v_cn_full,')) <> "" ) ;');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data ( SELECT distinct jed.id, CONCAT_WS(",",',v_cn_full,') ,"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, paymentdetailotherwise pdo, jedetail jed, payment pt, journalentry je where jed.account="',accidparam,'" and pdo.id=jedcd.recdetailid and je.id=jed.journalentry and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and pdo.payment=pt.id and je.transactionid=pt.id and trim(CONCAT_WS(",",',v_cn_full,')) <> "" ) ;');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

deallocate prepare stmt1;
END IF;

IF moduleidparam = '24' THEN
SET @s=CONCAT('INSERT ignore into temp_custom_data ( SELECT distinct jed.id, CONCAT_WS(",",',v_cn_full,') ,"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, journalentry je where jed.account="',accidparam,'" and jed.id=jedcd.jedetailid and je.id=jed.journalentry and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and je.transactionid is null and trim(CONCAT_WS(",",',v_cn_full,')) <> "" ) ;');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

deallocate prepare stmt1;
END IF;

END IF;
END//
delimiter ;
