Ext.define("Ext.grid.column.Date",{extend:"Ext.grid.column.Column",alias:["widget.datecolumn"],requires:["Ext.Date"],alternateClassName:"Ext.grid.DateColumn",isDateColumn:true,defaultFilterType:"date",producesHTML:false,initComponent:function(){if(!this.format){this.format=Ext.Date.defaultFormat}this.callParent(arguments)},defaultRenderer:function(A){return Ext.util.Format.date(A,this.format)},updater:function(A,B){Ext.fly(A).down(this.getView().innerSelector,true).innerHTML=Ext.grid.column.Date.prototype.defaultRenderer.call(this,B)}})