DROP PROCEDURE if exists insert_custom_data_dd;
delimiter //
CREATE PROCEDURE insert_custom_data_dd (IN accidparam varchar(255), IN companyidparam varchar(60), IN usersessionidparam varchar(60), IN moduleidparam smallint(6), IN startdate varchar(20), IN enddate varchar (20), IN delimiter varchar(10), IN iscustomfield int(1))
BEGIN
 
 DECLARE v_finished INTEGER DEFAULT 0;
        DECLARE v_fl varchar(100) DEFAULT "";
        DECLARE v_cn varchar(100) DEFAULT "";
	DECLARE v_where text DEFAULT "";

 -- declare cursor for employee email
 DEClARE cd_cursor CURSOR FOR select fieldlabel, colnum from fieldparams where companyid=companyidparam and moduleid=moduleidparam and (fieldtype = '4' or fieldtype = '7' or fieldtype = '12');
 DEClARE cd_cursor_customfield CURSOR FOR select fieldlabel, colnum from fieldparams where companyid=companyidparam and moduleid=moduleidparam and (fieldtype = '4' or fieldtype = '7' or fieldtype = '12') and customfield=1;
DEClARE cd_cursor_dimension CURSOR FOR select fieldlabel, colnum from fieldparams where companyid=companyidparam and moduleid=moduleidparam and (fieldtype = '4' or fieldtype = '7' or fieldtype = '12') and customfield=0;

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

SET v_where=CONCAT(v_where,' OR jedcd.col',v_cn,'=dd.id');


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

SET v_where=CONCAT(v_where,' OR jedcd.col',v_cn,'=dd.id');


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

SET v_where=CONCAT(v_where,' OR jedcd.col',v_cn,'=dd.id');


 END LOOP get_cd;
 
 CLOSE cd_cursor_dimension;
END IF;

set v_where=substring(v_where,4,length(v_where));


IF v_where !="" then
set v_where=CONCAT("(",v_where,")");

IF moduleidparam = '2' OR moduleidparam = '4'  THEN
SET @s=CONCAT('INSERT ignore into temp_custom_data_dd (SELECT invd.id,group_concat(CONCAT_WS("',delimiter,'",fp.fieldlabel, dd.value)),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, journalentry je, fieldcombodata dd, fieldparams fp, invoicedetails invd, invoice inv where jed.account="',accidparam,'" and ',v_where,' and fp.id=dd.fieldid and inv.id=je.transactionid and invd.invoice=inv.id and jed.journalentry=je.id and jedcd.recdetailid=invd.id and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and trim(dd.value) <> "" group by invd.id);');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

deallocate prepare stmt1;
END IF;

IF moduleidparam = '6' OR moduleidparam = '8'  THEN
SET @s=CONCAT('INSERT ignore into temp_custom_data_dd (SELECT grd.id,group_concat(CONCAT_WS("',delimiter,'",fp.fieldlabel, dd.value)),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, journalentry je, fieldcombodata dd, fieldparams fp, grdetails grd, goodsreceipt gr where jed.account="',accidparam,'" and ',v_where,' and fp.id=dd.fieldid and gr.id=je.transactionid and grd.goodsreceipt=gr.id and jed.journalentry=je.id and jedcd.recdetailid=grd.id and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and trim(dd.value) <> "" group by grd.id);');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data_dd (SELECT grd.id,group_concat(CONCAT_WS("',delimiter,'",fp.fieldlabel, dd.value)),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, journalentry je, fieldcombodata dd, fieldparams fp, expenseggrdetails grd, goodsreceipt gr where jed.account="',accidparam,'" and ',v_where,' and fp.id=dd.fieldid and gr.id=je.transactionid and grd.goodsreceipt=gr.id and jed.journalentry=je.id and jedcd.recdetailid=grd.id and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and trim(dd.value) <> "" group by grd.id);');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

deallocate prepare stmt1;
END IF;

IF moduleidparam = '12' THEN
SET @s=CONCAT('INSERT ignore into temp_custom_data_dd (SELECT jed.id,GROUP_CONCAT(CONCAT_WS("',delimiter,'",fp.fieldlabel, dd.value)),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, journalentry je, fieldcombodata dd, fieldparams fp, creditnote cn where jed.account="',accidparam,'" and ',v_where,' and fp.id=dd.fieldid and cn.journalentry=je.id and jed.journalentry=je.id and je.typevalue ="2" and jed.id=jedcd.jedetailid and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and trim(dd.value) <> "" group by cn.id);');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data_dd (SELECT jed.id,GROUP_CONCAT(CONCAT_WS("',delimiter,'",fp.fieldlabel, dd.value)),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, journalentry je, fieldcombodata dd, fieldparams fp, cntaxentry cnt, creditnote cn where jed.account="',accidparam,'" and ',v_where,' and fp.id=dd.fieldid and cn.id=cnt.creditnote and jed.journalentry=je.id and jed.id=jedcd.jedetailid and cnt.totaljedid=jed.id and je.typevalue <> "2" and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and trim(dd.value) <> "" group by cn.id);');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

deallocate prepare stmt1;
END IF;


IF moduleidparam = '10' THEN
SET @s=CONCAT('INSERT ignore into temp_custom_data_dd (SELECT jed.id,GROUP_CONCAT(CONCAT_WS("',delimiter,'",fp.fieldlabel, dd.value)),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, journalentry je, fieldcombodata dd, fieldparams fp, debitnote dn where jed.account="',accidparam,'" and ',v_where,' and fp.id=dd.fieldid and dn.journalentry=je.id and jed.journalentry=je.id and je.typevalue ="2" and jed.id=jedcd.jedetailid and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and trim(dd.value) <> "" group by dn.id);');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data_dd (SELECT jed.id,GROUP_CONCAT(CONCAT_WS("',delimiter,'",fp.fieldlabel, dd.value)),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, journalentry je, fieldcombodata dd, fieldparams fp, dntaxentry dnt, debitnote dn where jed.account="',accidparam,'" and ',v_where,' and fp.id=dd.fieldid and dn.id=dnt.debitnote and jed.journalentry=je.id and jed.id=jedcd.jedetailid and dnt.totaljedid=jed.id and je.typevalue <> "2" and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and trim(dd.value) <> "" group by dn.id);');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

deallocate prepare stmt1;
END IF;

IF moduleidparam = '16' THEN
SET @s=CONCAT('INSERT ignore into temp_custom_data_dd (SELECT jed.id,group_concat(CONCAT_WS("',delimiter,'",fp.fieldlabel, dd.value)),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, journalentry je, fieldcombodata dd, fieldparams fp, receiptdetails rd, receipt rt where jed.account="',accidparam,'" and ',v_where,' and fp.id=dd.fieldid and rt.id=je.transactionid and rd.receipt=rt.id and jed.journalentry=je.id and jedcd.recdetailid=rd.id and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and trim(dd.value) <> "" group by rd.id);');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data_dd (SELECT jed.id,group_concat(CONCAT_WS("',delimiter,'",fp.fieldlabel, dd.value)),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, journalentry je, fieldcombodata dd, fieldparams fp, receiptadvancedetail rad, receipt rt where jed.account="',accidparam,'" and ',v_where,' and fp.id=dd.fieldid and rt.id=je.transactionid and rad.receipt=rt.id and jed.journalentry=je.id and jedcd.recdetailid=rad.id and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and trim(dd.value) <> "" group by rad.id);');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data_dd (SELECT jed.id,group_concat(CONCAT_WS("',delimiter,'",fp.fieldlabel, dd.value)),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, journalentry je, fieldcombodata dd, fieldparams fp, debitnotepayment dnp, receipt rt where jed.account="',accidparam,'" and ',v_where,' and fp.id=dd.fieldid and rt.id=je.transactionid and dnp.receiptid=rt.id and jed.journalentry=je.id and jedcd.recdetailid=dnp.id and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and trim(dd.value) <> "" group by dnp.id);');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data_dd (SELECT jed.id,group_concat(CONCAT_WS("',delimiter,'",fp.fieldlabel, dd.value)),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, journalentry je, fieldcombodata dd, fieldparams fp, receiptdetailotherwise rad, receipt rt where jed.account="',accidparam,'" and ',v_where,' and fp.id=dd.fieldid and rt.id=je.transactionid and rad.receipt=rt.id and jed.journalentry=je.id and jedcd.recdetailid=rad.id and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and trim(dd.value) <> "" group by rad.id);');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

deallocate prepare stmt1;
END IF;


IF moduleidparam = '14' THEN
SET @s=CONCAT('INSERT ignore into temp_custom_data_dd (SELECT jed.id,group_concat(CONCAT_WS("',delimiter,'",fp.fieldlabel, dd.value)),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, journalentry je, fieldcombodata dd, fieldparams fp, paymentdetail pd, payment pt where jed.account="',accidparam,'" and ',v_where,' and fp.id=dd.fieldid and pt.id=je.transactionid and pd.payment=pt.id and jed.journalentry=je.id and jedcd.recdetailid=pd.id and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and trim(dd.value) <> "" group by pd.id);');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data_dd (SELECT jed.id,group_concat(CONCAT_WS("',delimiter,'",fp.fieldlabel, dd.value)),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, journalentry je, fieldcombodata dd, fieldparams fp, advancedetail pad, payment pt where jed.account="',accidparam,'" and ',v_where,' and fp.id=dd.fieldid and pt.id=je.transactionid and pad.payment=pt.id and jed.journalentry=je.id and jedcd.recdetailid=pad.id and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and trim(dd.value) <> "" group by pad.id);');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data_dd (SELECT jed.id,group_concat(CONCAT_WS("',delimiter,'",fp.fieldlabel, dd.value)),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, journalentry je, fieldcombodata dd, fieldparams fp, creditnotpayment cnp, payment pt where jed.account="',accidparam,'" and ',v_where,' and fp.id=dd.fieldid and pt.id=je.transactionid and cnp.paymentid=pt.id and jed.journalentry=je.id and jedcd.recdetailid=cnp.id and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and trim(dd.value) <> "" group by cnp.id);');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

SET @s=CONCAT('INSERT ignore into temp_custom_data_dd (SELECT jed.id,group_concat(CONCAT_WS("',delimiter,'",fp.fieldlabel, dd.value)),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, journalentry je, fieldcombodata dd, fieldparams fp, paymentdetailotherwise pdo, payment pt where jed.account="',accidparam,'" and ',v_where,' and fp.id=dd.fieldid and pt.id=je.transactionid and pdo.payment=pt.id and jed.journalentry=je.id and jedcd.recdetailid=pdo.id and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and trim(dd.value) <> "" group by pdo.id);');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

deallocate prepare stmt1;
END IF;

IF moduleidparam = '24' THEN
SET @s=CONCAT('INSERT ignore into temp_custom_data_dd (SELECT jed.id,group_concat(CONCAT_WS("',delimiter,'",fp.fieldlabel, dd.value)),"',usersessionidparam,'",',moduleidparam,' FROM accjedetailcustomdata jedcd, jedetail jed, journalentry je, fieldcombodata dd, fieldparams fp where jedcd.jedetailid=jed.id and jed.account="',accidparam,'" and ',v_where,' and fp.id=dd.fieldid and je.transactionid is not null and jed.journalentry=je.id and je.entrydate >= "',startdate,'" and je.entrydate <= "',enddate,'" and trim(dd.value) <> "" group by jed.id);');
select @s;
PREPARE stmt1 from @s;
execute stmt1 ;

deallocate prepare stmt1;
END IF;

END IF;
END//
delimiter ;
