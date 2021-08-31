Ext.define("Ext.chart.LegendBase",{extend:"Ext.view.View",config:{tpl:['<div class="',Ext.baseCSSPrefix,'legend-container">','<tpl for=".">','<div class="',Ext.baseCSSPrefix,'legend-item">',"<span ",'class="',Ext.baseCSSPrefix,"legend-item-marker {[ values.disabled ? Ext.baseCSSPrefix + 'legend-inactive' : '' ]}\" ",'style="background:{mark};">',"</span>{name}","</div>","</tpl>","</div>"],nodeContainerSelector:"div."+Ext.baseCSSPrefix+"legend-container",itemSelector:"div."+Ext.baseCSSPrefix+"legend-item",docked:"bottom"},setDocked:function(D){var C=this,A=C.ownerCt,B;C.docked=D;switch(D){case"top":case"bottom":C.addCls(Ext.baseCSSPrefix+"horizontal");B="hbox";break;case"left":case"right":C.removeCls(Ext.baseCSSPrefix+"horizontal");B="vbox";break}if(A){A.setDocked(D)}},setStore:function(A){this.bindStore(A)},clearViewEl:function(){this.callParent(arguments);Ext.removeNode(this.getNodeContainer())},onItemClick:function(A,C,B,D){this.callParent(arguments);this.toggleItem(B)}})