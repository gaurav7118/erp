/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.apply(Ext.form.field.VTypes, {
    //  vtype validation function
    reportnamevtype: function(val, field) {
        return ExtGlobal.reportNameTest.test(val);
    },
    // vtype Text property: The error text to display when the validation function returns false
    reportnamevtypeText: ExtGlobal.getLocaleText("acc.CustomReport.invalidReportName"),
    
    daterange : function(val, field) {
        var date = field.parseDate(val);

        if(!date){
            return;
        }
        if (field.startDateField && (!this.dateRangeMax || (date.getTime() != this.dateRangeMax.getTime()))) {
            var start = Ext.getCmp(field.startDateField);
            start.setMaxValue(date);
            this.dateRangeMax = date;
            start.validate();
			
        } 
        else if (field.endDateField && (!this.dateRangeMin || (date.getTime() != this.dateRangeMin.getTime()))) {
            var end = Ext.getCmp(field.endDateField);
            end.setMinValue(date);
            this.dateRangeMin = date;
            end.validate();
			
        }
        /*
		 * Always return true since we're only using this vtype to set the
		 * min/max allowed values (these are tested for after the vtype test)
		 */
        return true;
    }
});