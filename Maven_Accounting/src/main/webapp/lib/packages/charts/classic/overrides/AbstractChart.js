Ext.define("Ext.chart.overrides.AbstractChart",{override:"Ext.chart.AbstractChart",updateLegend:function(B,A){var C;this.callParent([B,A]);if(B){C=B.docked;this.addDocked({dock:C,xtype:"panel",shrinkWrap:true,scrollable:true,layout:{type:C==="top"||C==="bottom"?"hbox":"vbox",pack:"center"},items:B,cls:Ext.baseCSSPrefix+"legend-panel"})}},performLayout:function(){if(this.isVisible(true)){return this.callParent()}this.cancelChartLayout();return false},afterComponentLayout:function(C,A,B,D){this.callParent([C,A,B,D]);this.scheduleLayout()},allowSchedule:function(){return this.rendered},onDestroy:function(){this.destroyChart();this.callParent(arguments)}})