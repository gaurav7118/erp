DROP PROCEDURE IF EXISTS deletedata;
delimiter //
CREATE PROCEDURE deletedata(IN pattern varchar(100))
BEGIN
    delete from temp_custom_data;
    delete from temp_custom_data_dd;
    delete from temp_gl_details;
END //
delimiter ;