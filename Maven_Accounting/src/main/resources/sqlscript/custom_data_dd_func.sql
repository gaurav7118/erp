DROP FUNCTION if exists fetch_custom_data_dd;

delimiter //
CREATE FUNCTION fetch_custom_data_dd (idparam varchar(255)) RETURNS TEXT DETERMINISTIC
BEGIN
 
RETURN (select value from fieldcombodata where id=idparam);

END//
delimiter ;
