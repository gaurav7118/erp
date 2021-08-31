DROP FUNCTION if exists exchangerate_calc;

delimiter //
CREATE FUNCTION exchangerate_calc (companyidparam varchar(255), dateparam date, fromcurrencyparam varchar(255), tocurrencyparam varchar(255)) RETURNS TEXT DETERMINISTIC
BEGIN
 
DECLARE v_debit char(1) DEFAULT "";
IF fromcurrencyparam=tocurrencyparam then
return 1;
else 

return (select erd.exchangerate from exchangeratedetails erd  
inner join exchangerate er on erd.exchangeratelink = er.id and er.fromcurrency=fromcurrencyparam and er.tocurrency=tocurrencyparam
inner join (select max(applydate) as maxdate from exchangeratedetails ierd inner join exchangerate ier on ierd.exchangeratelink = ier.id and ier.fromcurrency=fromcurrencyparam and ier.tocurrency=tocurrencyparam where ierd.company=companyidparam and ierd.applydate <= dateparam) as max on erd.applydate=max.maxdate
where erd.company=companyidparam limit 1 
);
END IF;
END//
delimiter ;
