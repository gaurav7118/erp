
DROP PROCEDURE IF EXISTS DeleteCompanyProcedure;
delimiter //

CREATE PROCEDURE DeleteCompanyProcedure(fromdb varchar(50)) BEGIN
  DECLARE _subdomain varchar(50);
  DECLARE cur CURSOR FOR select subdomain from company left join subdomains on subdomain = subdomains.id where subdomains.id is null;

  OPEN cur;

  companyLoop: LOOP
    FETCH cur INTO _subdomain;
        CALL deletecompanydata(fromdb,_subdomain);
  END LOOP companyLoop;

  CLOSE cur;
END//
delimiter ;
