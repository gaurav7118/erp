Ext.define("Ext.layout.component.ProgressBar",{alias:["layout.progressbar"],extend:"Ext.layout.component.Auto",type:"progressbar",beginLayout:function(D){var B=this,A,C;B.callParent(arguments);if(!D.textEls){C=B.owner.textEl;if(C.isComposite){D.textEls=[];C=C.elements;for(A=C.length;A--;){D.textEls[A]=D.getEl(Ext.get(C[A]))}}else{D.textEls=[D.getEl("textEl")]}}},calculate:function(E){var C=this,A,D,B;C.callParent(arguments);if(Ext.isNumber(B=E.getProp("width"))){B-=E.getBorderInfo().width;D=E.textEls;for(A=D.length;A--;){D[A].setWidth(B)}}else{C.done=false}}})