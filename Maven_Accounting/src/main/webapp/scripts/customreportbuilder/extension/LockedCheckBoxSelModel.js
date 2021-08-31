/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

Ext.define("ReportBuilder.extension.LockedCheckBoxSelModel", {
    extend: "Ext.selection.CheckboxModel",
    lock:false,
    onReconfigure: function(grid, store, columns) {
        if (columns) {
            var view;
            if(this.views[1]!=undefined){
                view = this.lock ? this.views[0]:this.views[1]; 
            }else{
                view = this.views[0];
            }
            this.addCheckbox(view);
        }
    }, 
    toggleUiHeader: function(isChecked) {
        var view = this.views[0],
        headerCt = view.headerCt,
        checkHd = headerCt.child('gridcolumn[isCheckerHd]'),
        cls = this.checkerOnCls;
            
        if(checkHd == null && this.views[1] != undefined){
            checkHd = this.views[1].headerCt.child('gridcolumn[isCheckerHd]');
        }
        if (checkHd) {
            if (isChecked) {
                checkHd.addCls(cls);
            } else {
                checkHd.removeCls(cls);
            }
        }
    }
});