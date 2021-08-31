/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Ext.define('ReportBuilder.overrides.pivot.matrix.Base', {
    override: 'Ext.pivot.matrix.Base',
    getColumnHeaders: function () {
        var me = this;
        if (!me.model) {
            me.buildModelAndColumns();
        } else {
            me.buildColumnHeaders(true);
        }
        //To remove renderer from leftAxis columns once the renderer has already been applied on values.
        //Renderer on leftAxis columns are already applied when this code executes
        for (var i = 0; i < me.columns.length; i++) {
            if (me.columns[i].leftAxis == true && me.columns[i].grandTotal != true) {
                me.columns[i].dimension.renderer = function (val, metaData, record) {
                    return val;
                };
            }
        }
        return me.columns;
    }
});