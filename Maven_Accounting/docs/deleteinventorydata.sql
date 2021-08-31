delete from in_attachedserial where attachedbatch in (select id from in_attachedbatch where company = ?);
delete from in_cc_detail where cyclecount in (select id from in_cyclecount where company = ?);
delete from in_cccalendar_frequency where cc_calendarid in (select id from in_cyclecount_calendar where company = ?);
delete from in_consignmentdetails where consignment in (select id from in_consignment where  company = ?);
delete from in_consignment where company = ?;
delete from in_cyclecount where company = ?;
delete from in_cyclecount_calendar where company = ?;
delete from in_cyclecountdraft where company = ?;
delete from in_documents where docid in (select id from in_documentcompmap where company = ?); 
delete from in_documentcompmap where company = ?;
delete from in_goodsrequest where company = ?;
delete from in_inspection_area where inspection_template in (select id from in_inspection_template where company = ?);
delete from in_inspection_template where company = ?;
delete from in_inspection_criteria_detail where inspection_detail in (select id from in_inspection_detail where company = ?);
delete from in_inspection_detail where company = ?;
delete from in_inventoryconfig where company = ?;
delete from in_ist_attachedbatch where istrequest in (select id from in_interstoretransfer where company = ?);
delete from in_ist_detail where istrequest in (select id from in_interstoretransfer where company = ?);
delete from in_ist_stockbuffer where location in(select id from in_location where company = ?);
delete from in_interstoretransfer where company = ?;
delete from in_location where company = ?;
delete from in_packaging where compay = ?;
delete from in_product_frequency where productid in (select id from product where company = ?);
delete from in_product_threshold where company = ?;
delete from in_sa_approval where stock_adjustment in (select id from  in_stockadjustment where company = ?);
delete from in_sa_attachedbatch where stockadjustment  in (select id from  in_stockadjustment where company = ?);
delete from in_sa_detail_approval where stock_adjustment_detail in (select id from  in_sa_detail where stockadjustment  in (select id from  in_stockadjustment where company = ?))
delete from in_sa_detail where stockadjustment  in (select id from  in_stockadjustment where company = ?);
delete from in_seqformat where company = ?;
delete from in_sm_attachedbatch where stockmovement in (select id from in_stockmovement where company = ?);
delete from in_sm_detail where stockmovement in (select id from in_stockmovement where company = ?);
delete from in_sr_attachedbatch where stockrequest in (select id from in_goodsrequest where company =?);
delete from in_sr_detail where stockrequest in (select id from in_goodsrequest where company =?);
delete from in_sr_stockbuffer where stockrequest in (select id from in_goodsrequest where company =?);
delete from in_stock where company = ?;
delete from in_stockadjustment where company = ?;
delete from in_stockadjustmentdraft where company = ?;
delete from in_stockbooking_detail where stockbooking in (select id from in_stockbooking where company = ?);
delete from in_stockbooking where company = ?;
delete from in_stockmovement  where company = ?;
delete from in_stocktransfer_detail_approval where stocktransfer_approval   in (select id from in_stocktransfer_approval where inspector in (select userid from users where company = ?)
delete from in_stocktransfer_approval where inspector in (select userid from users where company = ?)
delete from in_store_executive where userid in (select userid from users where company = ?);
delete from in_store_location where storeid in (select id from in_storemaster where company =?);
delete from in_store_user  where userid in (select userid from users where company = ?);
delete from in_storemaster where company =?;
delete from in_temp_stockadjustmentdetail where store in (select id from in_storemaster where company =?);
delete from in_attachedbatch where company = ?;




# ======== Master Data ===========
# in_cc_attachedbatch;
# in_frequency;
# in_seqmodule;
# in_seqnumber;



