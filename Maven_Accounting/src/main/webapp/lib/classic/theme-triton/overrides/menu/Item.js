Ext.define("Ext.theme.triton.menu.Item",{override:"Ext.menu.Item",compatibility:Ext.isIE8,onFocus:function(A){this.callParent([A]);this.repaintIcons()},onFocusLeave:function(A){this.callParent([A]);this.repaintIcons()},privates:{repaintIcons:function(){var C=this.iconEl,B=this.arrowEl,A=this.checkEl;if(C){C.syncRepaint()}if(B){B.syncRepaint()}if(A){A.syncRepaint()}}}})