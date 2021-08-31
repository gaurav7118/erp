DROP FUNCTION if exists fetchaccounts_diffdebit;
delimiter //
CREATE FUNCTION fetchaccounts_diffdebit (accidparam varchar(255), jeidparam varchar(255), jedidparam varchar(255)) RETURNS TEXT DETERMINISTIC
BEGIN
 
DECLARE v_debit char(1) DEFAULT "";


  select distinct debit into v_debit from jedetail where id=jedidparam and account=accidparam;
RETURN  (select GROUP_CONCAT(acc.name) from account acc, jedetail jed where jed.account=acc.id and jed.account <> accidparam and jed.journalentry = jeidparam and jed.debit <> v_debit);

END//
delimiter ;
