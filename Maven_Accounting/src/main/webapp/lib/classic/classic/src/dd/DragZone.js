Ext.define("Ext.dd.DragZone",{extend:"Ext.dd.DragSource",constructor:function(C,B){var D=this,A=D.containerScroll;D.callParent([C,B]);if(A){C=D.scrollEl||C;C=Ext.get(C);if(Ext.isObject(A)){C.ddScrollConfig=A}Ext.dd.ScrollManager.register(C)}},getDragData:function(A){return Ext.dd.Registry.getHandleFromEvent(A)},onInitDrag:function(A,B){this.proxy.update(this.dragData.ddel.cloneNode(true));this.onStartDrag(A,B);return true},getRepairXY:function(A){return Ext.fly(this.dragData.ddel).getXY()},destroy:function(){this.callParent();if(this.containerScroll){Ext.dd.ScrollManager.unregister(this.scrollEl||this.el)}}})